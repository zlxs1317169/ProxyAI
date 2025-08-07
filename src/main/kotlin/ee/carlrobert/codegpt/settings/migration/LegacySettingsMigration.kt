package ee.carlrobert.codegpt.settings.migration

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import ee.carlrobert.codegpt.settings.GeneralSettings
import ee.carlrobert.codegpt.settings.models.ModelRegistry
import ee.carlrobert.codegpt.settings.models.ModelSettings
import ee.carlrobert.codegpt.settings.models.ModelSettingsState
import ee.carlrobert.codegpt.settings.service.FeatureType
import ee.carlrobert.codegpt.settings.service.ServiceType
import ee.carlrobert.codegpt.settings.service.anthropic.AnthropicSettings
import ee.carlrobert.codegpt.settings.service.codegpt.CodeGPTServiceSettings
import ee.carlrobert.codegpt.settings.service.custom.CustomServicesSettings
import ee.carlrobert.codegpt.settings.service.google.GoogleSettings
import ee.carlrobert.codegpt.settings.service.llama.LlamaSettings
import ee.carlrobert.codegpt.settings.service.ollama.OllamaSettings
import ee.carlrobert.codegpt.settings.service.openai.OpenAISettings
import ee.carlrobert.llm.client.google.models.GoogleModel

object LegacySettingsMigration {

    private val logger = thisLogger()

    fun migrateIfNeeded(): ModelSettingsState? {
        return try {
            val generalState = GeneralSettings.getCurrentState()
            val selectedService = generalState.selectedService
            
            if (selectedService != null) {
                generalState.selectedService = null
                createMigratedState(selectedService)
            } else {
                null
            }
        } catch (exception: Exception) {
            logger.error("Failed to migrate legacy settings", exception)
            null
        }
    }

    private fun createMigratedState(selectedService: ServiceType): ModelSettingsState {
        return ModelSettingsState().apply {
            val chatModel = getLegacyChatModelForService(selectedService)
            
            setModelSelection(FeatureType.CHAT, chatModel, selectedService)
            setModelSelection(FeatureType.AUTO_APPLY, chatModel, selectedService)
            setModelSelection(FeatureType.COMMIT_MESSAGE, chatModel, selectedService)
            setModelSelection(FeatureType.EDIT_CODE, chatModel, selectedService)
            setModelSelection(FeatureType.LOOKUP, chatModel, selectedService)

            val codeModel = getLegacyCodeModelForService(selectedService)
            setModelSelection(FeatureType.CODE_COMPLETION, codeModel, selectedService)
            
            if (selectedService == ServiceType.PROXYAI) {
                setModelSelection(FeatureType.NEXT_EDIT, ModelRegistry.ZETA, ServiceType.PROXYAI)
            } else {
                setModelSelection(FeatureType.NEXT_EDIT, null, selectedService)
            }
        }
    }

    private fun getLegacyChatModelForService(serviceType: ServiceType): String {
        return try {
            when (serviceType) {
                ServiceType.PROXYAI -> {
                    val settings = service<CodeGPTServiceSettings>()
                    settings.state.chatCompletionSettings.model ?: ModelRegistry.GEMINI_FLASH_2_5
                }

                ServiceType.OPENAI -> {
                    OpenAISettings.getCurrentState().model ?: ModelRegistry.GPT_4_1
                }

                ServiceType.ANTHROPIC -> {
                    AnthropicSettings.getCurrentState().model ?: ModelRegistry.CLAUDE_SONNET_4_20250514
                }

                ServiceType.GOOGLE -> {
                    val settings = service<GoogleSettings>()
                    settings.state.model ?: GoogleModel.GEMINI_2_5_PRO.code
                }

                ServiceType.OLLAMA -> {
                    val settings = service<OllamaSettings>()
                    settings.state.model ?: ModelRegistry.LLAMA_3_2
                }

                ServiceType.LLAMA_CPP -> {
                    val llamaSettings = LlamaSettings.getCurrentState()
                    if (llamaSettings.isUseCustomModel) {
                        llamaSettings.customLlamaModelPath
                    } else {
                        llamaSettings.huggingFaceModel.name
                    }
                }

                ServiceType.CUSTOM_OPENAI -> {
                    val customServicesSettings = service<CustomServicesSettings>()
                    val services = customServicesSettings.state.services
                    
                    val activeServiceName = customServicesSettings.state.active.name
                    if (!activeServiceName.isNullOrBlank()) {
                        activeServiceName
                    } else {
                        services.map { it.name }.lastOrNull()?.takeIf { it.isNotBlank() } ?: "Default"
                    }
                }

                ServiceType.MISTRAL -> {
                    ModelRegistry.DEVSTRAL_MEDIUM_2507
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to get legacy chat model for $serviceType", e)
            getDefaultModelForService(serviceType)
        }
    }

    private fun getLegacyCodeModelForService(serviceType: ServiceType): String? {
        return try {
            when (serviceType) {
                ServiceType.PROXYAI -> {
                    service<CodeGPTServiceSettings>().state.codeCompletionSettings.model
                }

                ServiceType.OPENAI -> {
                    ModelRegistry.GPT_3_5_TURBO_INSTRUCT
                }

                ServiceType.ANTHROPIC -> {
                    null
                }

                ServiceType.GOOGLE -> {
                    null
                }

                ServiceType.OLLAMA -> {
                    service<OllamaSettings>().state.model
                }

                ServiceType.LLAMA_CPP -> {
                    val llamaSettings = LlamaSettings.getCurrentState()
                    if (llamaSettings.isUseCustomModel) {
                        llamaSettings.customLlamaModelPath
                    } else {
                        llamaSettings.huggingFaceModel.name
                    }
                }

                ServiceType.CUSTOM_OPENAI -> {
                    service<CustomServicesSettings>().state.services
                        .map { it.name }
                        .lastOrNull() ?: ""
                }

                ServiceType.MISTRAL -> {
                    ModelRegistry.CODESTRAL_LATEST
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to get legacy code model for $serviceType", e)
            null
        }
    }

    private fun getDefaultModelForService(serviceType: ServiceType): String {
        return when (serviceType) {
            ServiceType.PROXYAI -> ModelRegistry.GEMINI_FLASH_2_5
            ServiceType.OPENAI -> ModelRegistry.GPT_4O
            ServiceType.ANTHROPIC -> ModelRegistry.CLAUDE_SONNET_4_20250514
            ServiceType.GOOGLE -> ModelRegistry.GEMINI_2_0_FLASH
            ServiceType.MISTRAL -> ModelRegistry.DEVSTRAL_MEDIUM_2507
            ServiceType.OLLAMA -> ModelRegistry.LLAMA_3_2
            ServiceType.LLAMA_CPP -> ModelRegistry.LLAMA_3_2_3B_INSTRUCT
            ServiceType.CUSTOM_OPENAI -> {
                // For Custom OpenAI, try to use the active service name if available
                // If not available, use a placeholder that won't break model selection
                try {
                    val customServicesSettings = service<CustomServicesSettings>()
                    val activeService = customServicesSettings.state.active
                    activeService?.name?.takeIf { it.isNotBlank() } ?: "Custom OpenAI"
                } catch (e: Exception) {
                    logger.warn("Could not access CustomServicesSettings for default model, using placeholder", e)
                    "Custom OpenAI"
                }
            }
        }
    }
}