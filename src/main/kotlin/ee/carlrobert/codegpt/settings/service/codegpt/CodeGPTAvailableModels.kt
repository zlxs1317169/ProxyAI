package ee.carlrobert.codegpt.settings.service.codegpt

import ee.carlrobert.codegpt.Icons
import ee.carlrobert.llm.client.codegpt.PricingPlan
import ee.carlrobert.llm.client.codegpt.PricingPlan.*
import javax.swing.Icon

object CodeGPTAvailableModels {

    val DEFAULT_CODE_MODEL = CodeGPTModel("Qwen 2.5 Coder", "qwen-2.5-32b-code", Icons.Qwen, ANONYMOUS)

    @JvmStatic
    val ALL_CHAT_MODELS: List<CodeGPTModel> = listOf(
        CodeGPTModel("o4-mini", "o4-mini", Icons.OpenAI, INDIVIDUAL),
        CodeGPTModel("GPT-4.1", "gpt-4.1", Icons.OpenAI, INDIVIDUAL),
        CodeGPTModel("GPT-4.1 Mini", "gpt-4.1-mini", Icons.OpenAI, ANONYMOUS),
        CodeGPTModel("Claude Sonnet 4 (thinking)", "claude-4-sonnet-thinking", Icons.Anthropic, INDIVIDUAL),
        CodeGPTModel("Claude Sonnet 4", "claude-4-sonnet", Icons.Anthropic, INDIVIDUAL),
        CodeGPTModel("Gemini 2.5 Pro", "gemini-pro-2.5", Icons.Google, INDIVIDUAL),
        CodeGPTModel("Gemini 2.5 Flash", "gemini-flash-2.5", Icons.Google, ANONYMOUS),
        CodeGPTModel("DeepSeek R1", "deepseek-r1", Icons.DeepSeek, INDIVIDUAL),
        CodeGPTModel("DeepSeek V3", "deepseek-v3", Icons.DeepSeek, FREE),
        CodeGPTModel("Qwen3 Coder", "qwen3-coder", Icons.Qwen, FREE),
    )

    @JvmStatic
    val ALL_CODE_MODELS: List<CodeGPTModel> = listOf(
        DEFAULT_CODE_MODEL,
        CodeGPTModel("GPT-3.5 Turbo Instruct", "gpt-3.5-turbo-instruct", Icons.OpenAI, FREE),
    )

    @JvmStatic
    fun findByCode(code: String?): CodeGPTModel? {
        return ALL_CHAT_MODELS.union(ALL_CODE_MODELS).firstOrNull { it.code == code }
    }
}

data class CodeGPTModel(
    val name: String,
    val code: String,
    val icon: Icon,
    val pricingPlan: PricingPlan
)
