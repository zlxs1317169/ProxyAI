package ee.carlrobert.codegpt.codecompletions

import com.intellij.codeInsight.inline.completion.elements.InlineCompletionElement
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionGrayTextElement
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import ee.carlrobert.codegpt.CodeGPTKeys
import ee.carlrobert.codegpt.codecompletions.edit.GrpcClientService
import ee.carlrobert.codegpt.metrics.SafeMetricsCollector
import ee.carlrobert.codegpt.settings.GeneralSettings
import ee.carlrobert.codegpt.settings.configuration.ConfigurationSettings
import ee.carlrobert.codegpt.settings.service.FeatureType
import ee.carlrobert.codegpt.settings.service.ModelSelectionService
import ee.carlrobert.codegpt.settings.service.ServiceType
import ee.carlrobert.codegpt.settings.service.codegpt.CodeGPTServiceSettings
import ee.carlrobert.codegpt.treesitter.CodeCompletionParserFactory
import ee.carlrobert.codegpt.ui.OverlayUtil.showNotification
import ee.carlrobert.llm.client.openai.completion.ErrorDetails
import ee.carlrobert.llm.completion.CompletionEventListener
import ee.carlrobert.service.PartialCodeCompletionResponse
import kotlinx.coroutines.channels.ProducerScope
import okhttp3.sse.EventSource
import java.util.concurrent.atomic.AtomicBoolean

class CodeCompletionEventListener(
    private val editor: Editor,
    private val channel: ProducerScope<InlineCompletionElement>
) : CompletionEventListener<String> {

    companion object {
        private val logger = thisLogger()
    }

    private val cancelled = AtomicBoolean(false)
    private val messageBuilder = StringBuilder()
    private var firstLine: String? = null
    private val firstLineSent = AtomicBoolean(false)
    private val cursorOffset = runReadAction { editor.caretModel.offset }
    private val prefix = editor.document.getText(TextRange(0, cursorOffset))
    private val suffix =
        editor.document.getText(TextRange(cursorOffset, editor.document.textLength))
    private val cache = editor.project?.service<CodeCompletionCacheService>()
    private val startTime = System.currentTimeMillis()

    override fun onOpen() {
        setLoading(true)
    }

    override fun onMessage(message: String, eventSource: EventSource) {
        if (cancelled.get()) {
            return
        }

        messageBuilder.append(message)

        trySendFirstLine(eventSource)
    }

    fun isNotAllowed(completion: String): Boolean {
        if (completion.contains("No newline at end of file")) {
            return true
        } else if (completion.trim().startsWith("+")) {
            return true
        }
        return false
    }

    private fun extractUpToRelevantNewline(message: String): String? {
        if (message.isEmpty()) return null
        val firstNewline = message.indexOf('\n')
        if (firstNewline == -1) return null
        return if (firstNewline == 0) {
            val secondNewline = message.indexOf('\n', 1)
            if (secondNewline != -1) {
                message.substring(0, secondNewline)
            } else {
                message
            }
        } else {
            message.substring(0, firstNewline)
        }
    }

    private fun trySendFirstLine(eventSource: EventSource) {
        if (firstLine != null) {
            return
        }

        var newLine = extractUpToRelevantNewline(messageBuilder.toString())
        if (newLine != null && !firstLineSent.get()) {
            val formattedLine = CodeCompletionFormatter(editor).format(newLine)

            if (isNotAllowed(formattedLine)) {
                cancelled.set(true)
                eventSource.cancel()
                return
            }

            runInEdt {
                channel.trySend(InlineCompletionGrayTextElement(formattedLine))
            }
            firstLineSent.set(true)
            firstLine = newLine
        }
    }

    override fun onComplete(finalResult: StringBuilder) {
        try {
            CodeGPTKeys.REMAINING_CODE_COMPLETION.set(editor, null)
            CodeGPTKeys.REMAINING_PREDICTION_RESPONSE.set(editor, null)

            if (cancelled.get() || finalResult.isEmpty()) {
                // 记录未接受的代码补全
                recordCompletionMetrics(finalResult.toString(), false)
                return
            }

            if (firstLineSent.get() && firstLine != null) {
                val remainingContent = finalResult.removePrefix(firstLine!!).toString()
                if (remainingContent.trim().isEmpty()) {
                    // 记录部分接受的代码补全
                    recordCompletionMetrics(firstLine ?: "", true)
                    return
                }

                val parsedContent = parseOutput(firstLine + remainingContent)
                if (parsedContent.isNotEmpty()) {
                    cache?.setCache(prefix, suffix, parsedContent)

                    CodeGPTKeys.REMAINING_CODE_COMPLETION.set(
                        editor,
                        PartialCodeCompletionResponse.newBuilder()
                            .setPartialCompletion(parsedContent.removePrefix(firstLine ?: ""))
                            .build()
                    )

                    // 记录接受的代码补全
                    recordCompletionMetrics(parsedContent, true)
                }
            } else {
                val formattedLine = CodeCompletionFormatter(editor).format(finalResult.toString())
                if (isNotAllowed(formattedLine)) {
                    recordCompletionMetrics(formattedLine, false)
                    return
                }

                val parsedContent = parseOutput(formattedLine)
                if (parsedContent.isNotEmpty()) {
                    cache?.setCache(prefix, suffix, parsedContent)
                    runInEdt {
                        channel.trySend(InlineCompletionGrayTextElement(parsedContent))
                    }

                    // 记录接受的代码补全
                    recordCompletionMetrics(parsedContent, true)
                }
            }
        } finally {
            handleCompleted()
        }
    }

    override fun onCancelled(messageBuilder: StringBuilder) {
        cancelled.set(true)
        // 记录取消的代码补全
        recordCompletionMetrics(messageBuilder.toString(), false)
        handleCompleted()
    }

    override fun onError(error: ErrorDetails, ex: Throwable) {
        val isCodeGPTService =
            service<ModelSelectionService>().getServiceForFeature(FeatureType.CODE_COMPLETION) == ServiceType.PROXYAI
        if (isCodeGPTService && "RATE_LIMIT_EXCEEDED" == error.code) {
            service<CodeGPTServiceSettings>().state
                .codeCompletionSettings
                .codeCompletionsEnabled = false
        }

        if (ex.message == null || (ex.message != null && ex.message != "Canceled")) {
            showNotification(error.message, NotificationType.ERROR)
            logger.error(error.message, ex)
        }

        setLoading(false)
    }

    private fun handleCompleted() {
        setLoading(false)

        if (messageBuilder.isEmpty()) {
            editor.project?.service<GrpcClientService>()?.getNextEdit(
                editor,
                prefix + suffix,
                runReadAction { editor.caretModel.offset })
        }
    }

    private fun setLoading(loading: Boolean) {
        editor.project?.let {
            CompletionProgressNotifier.update(it, loading)
        }
    }

    private fun parseOutput(input: String): String {
        if (!service<ConfigurationSettings>().state.codeCompletionSettings.treeSitterProcessingEnabled) {
            return input
        }

        return CodeCompletionParserFactory
            .getParserForFileExtension(editor.virtualFile.extension)
            .parse(prefix, suffix, input)
            .trimEnd()
    }

    /**
     * 记录代码补全指标
     */
    private fun recordCompletionMetrics(completionText: String, accepted: Boolean) {
        val responseTime = System.currentTimeMillis() - startTime
        SafeMetricsCollector.safeRecordCodeCompletionMetrics(
            editor, completionText, accepted, responseTime
        )
    }
}