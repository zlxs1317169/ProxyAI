package ee.carlrobert.codegpt.codecompletions

import com.intellij.codeInsight.inline.completion.InlineCompletionEvent
import com.intellij.codeInsight.inline.completion.InlineCompletionHandler
import com.intellij.codeInsight.inline.completion.InlineCompletionRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import ee.carlrobert.codegpt.metrics.integration.CodeCompletionMetricsIntegration

/**
 * 代码补全接受监听器
 * 监听用户实际接受代码补全的行为并记录指标
 */
class CodeCompletionAcceptanceListener {
    
    companion object {
        private val logger = thisLogger()
        
        /**
         * 记录用户接受代码补全的行为
         */
        fun recordAcceptance(editor: Editor, acceptedText: String, acceptanceType: String) {
            try {
                val project = editor.project ?: return
                
                ApplicationManager.getApplication().executeOnPooledThread {
                    try {
                        // 获取文件语言
                        val language = getLanguageFromEditor(editor)
                        
                        // 记录接受的代码补全
                        CodeCompletionMetricsIntegration.recordCodeCompletionMetrics(
                            editor, acceptedText, true, 0L
                        )
                        
                        logger.info("记录代码补全接受: 语言=$language, 文本长度=${acceptedText.length}, 类型=$acceptanceType")
                        
                    } catch (e: Exception) {
                        logger.warn("记录代码补全接受时发生错误", e)
                    }
                }
                
            } catch (e: Exception) {
                logger.warn("处理代码补全接受事件时发生错误", e)
            }
        }
        
        /**
         * 记录用户拒绝代码补全的行为
         */
        fun recordRejection(editor: Editor, rejectedText: String, rejectionReason: String) {
            try {
                val project = editor.project ?: return
                
                ApplicationManager.getApplication().executeOnPooledThread {
                    try {
                        // 获取文件语言
                        val language = getLanguageFromEditor(editor)
                        
                        // 记录拒绝的代码补全
                        CodeCompletionMetricsIntegration.recordCodeCompletionMetrics(
                            editor, rejectedText, false, 0L
                        )
                        
                        logger.info("记录代码补全拒绝: 语言=$language, 文本长度=${rejectedText.length}, 原因=$rejectionReason")
                        
                    } catch (e: Exception) {
                        logger.warn("记录代码补全拒绝时发生错误", e)
                    }
                }
                
            } catch (e: Exception) {
                logger.warn("处理代码补全拒绝事件时发生错误", e)
            }
        }
        
        private fun getLanguageFromEditor(editor: Editor): String {
            return try {
                val virtualFile = editor.virtualFile
                val extension = virtualFile?.extension
                mapExtensionToLanguage(extension)
            } catch (e: Exception) {
                "unknown"
            }
        }
        
        private fun mapExtensionToLanguage(extension: String?): String {
            return when (extension?.lowercase()) {
                "java" -> "java"
                "kt" -> "kotlin"
                "py" -> "python"
                "js", "ts" -> "javascript"
                "cpp", "cc", "cxx" -> "cpp"
                "c" -> "c"
                "go" -> "go"
                "rs" -> "rust"
                "php" -> "php"
                "rb" -> "ruby"
                "swift" -> "swift"
                else -> extension ?: "unknown"
            }
        }
    }
}