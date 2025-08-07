package ee.carlrobert.codegpt.settings.service.codegpt

import com.intellij.openapi.components.*
import ee.carlrobert.codegpt.settings.models.ModelRegistry

@Service
@State(
    name = "CodeGPT_CodeGPTServiceSettings_280",
    storages = [Storage("CodeGPT_CodeGPTServiceSettings_280.xml")]
)
class CodeGPTServiceSettings :
    SimplePersistentStateComponent<CodeGPTServiceSettingsState>(CodeGPTServiceSettingsState())

class CodeGPTServiceSettingsState : BaseState() {
    var chatCompletionSettings by property(CodeGPTServiceChatCompletionSettingsState())
    var codeCompletionSettings by property(CodeGPTServiceCodeCompletionSettingsState())
    var nextEditsEnabled by property(true)
}

class CodeGPTServiceChatCompletionSettingsState : BaseState() {
    var model by string(ModelRegistry.GPT_4_1_MINI)
}

class CodeGPTServiceCodeCompletionSettingsState : BaseState() {
    var codeCompletionsEnabled by property(true)
    var model by string(ModelRegistry.QWEN_2_5_32B_CODE)
}
