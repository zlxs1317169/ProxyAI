package ee.carlrobert.codegpt.toolwindow.chat.ui.textarea;

import static ee.carlrobert.codegpt.CodeGPTKeys.CODEGPT_USER_DETAILS;
import static ee.carlrobert.codegpt.settings.service.ServiceType.ANTHROPIC;
import static ee.carlrobert.codegpt.settings.service.ServiceType.CUSTOM_OPENAI;
import static ee.carlrobert.codegpt.settings.service.ServiceType.GOOGLE;
import static ee.carlrobert.codegpt.settings.service.ServiceType.LLAMA_CPP;
import static ee.carlrobert.codegpt.settings.service.ServiceType.MISTRAL;
import static ee.carlrobert.codegpt.settings.service.ServiceType.OLLAMA;
import static ee.carlrobert.codegpt.settings.service.ServiceType.OPENAI;
import static ee.carlrobert.codegpt.settings.service.ServiceType.PROXYAI;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import ee.carlrobert.codegpt.Icons;
import ee.carlrobert.codegpt.completions.llama.LlamaModel;
import ee.carlrobert.codegpt.settings.models.ModelRegistry;
import ee.carlrobert.codegpt.settings.models.ModelSelection;
import ee.carlrobert.codegpt.settings.models.ModelSettings;
import ee.carlrobert.codegpt.settings.models.ModelSettingsConfigurable;
import ee.carlrobert.codegpt.settings.service.FeatureType;
import ee.carlrobert.codegpt.settings.service.ModelChangeNotifier;
import ee.carlrobert.codegpt.settings.service.ModelChangeNotifierAdapter;
import ee.carlrobert.codegpt.settings.service.ServiceType;
import ee.carlrobert.codegpt.settings.service.custom.CustomServiceSettingsState;
import ee.carlrobert.codegpt.settings.service.custom.CustomServicesSettings;
import ee.carlrobert.codegpt.settings.service.google.GoogleSettings;
import ee.carlrobert.codegpt.settings.service.llama.LlamaSettings;
import ee.carlrobert.codegpt.settings.service.ollama.OllamaSettings;
import ee.carlrobert.codegpt.toolwindow.ui.CodeGPTModelsListPopupAction;
import ee.carlrobert.codegpt.toolwindow.ui.ModelListPopup;
import ee.carlrobert.llm.client.codegpt.PricingPlan;
import ee.carlrobert.llm.client.google.models.GoogleModel;
import ee.carlrobert.llm.client.openai.completion.OpenAIChatCompletionModel;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModelComboBoxAction extends ComboBoxAction {

  private static final Logger LOG = Logger.getInstance(ModelComboBoxAction.class);

  private final Consumer<ServiceType> onModelChange;
  private final Project project;
  private final List<ServiceType> availableProviders;
  private final boolean showConfigureModels;

  public ModelComboBoxAction(
      Project project,
      Consumer<ServiceType> onModelChange,
      ServiceType selectedService) {
    this(project, onModelChange, selectedService, Arrays.asList(ServiceType.values()), true);
  }

  public ModelComboBoxAction(
      Project project,
      Consumer<ServiceType> onModelChange,
      ServiceType selectedProvider,
      List<ServiceType> availableProviders,
      boolean showConfigureModels) {
    this.project = project;
    this.onModelChange = onModelChange;
    this.availableProviders = availableProviders;
    this.showConfigureModels = showConfigureModels;
    setSmallVariant(true);
    updateTemplatePresentation(selectedProvider);

    var messageBus = ApplicationManager.getApplication().getMessageBus().connect();
    messageBus.subscribe(
        ModelChangeNotifier.getTopic(),
        new ModelChangeNotifierAdapter() {
          @Override
          public void chatModelChanged(@NotNull String newModel, @NotNull ServiceType serviceType) {
            updateTemplatePresentation(serviceType);
          }
        });
  }

  public JComponent createCustomComponent(@NotNull String place) {
    return createCustomComponent(getTemplatePresentation(), place);
  }

  @NotNull
  @Override
  public JComponent createCustomComponent(
      @NotNull Presentation presentation,
      @NotNull String place) {
    ComboBoxButton button = createComboBoxButton(presentation);
    button.setForeground(
        EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground());
    button.setBorder(null);
    button.putClientProperty("JButton.backgroundColor", new Color(0, 0, 0, 0));
    return button;
  }

  @Override
  protected JBPopup createActionPopup(DefaultActionGroup group, @NotNull DataContext context,
      @Nullable Runnable disposeCallback) {
    ListPopup popup = new ModelListPopup(group, context);
    if (disposeCallback != null) {
      popup.addListener(new JBPopupListener() {
        @Override
        public void onClosed(@NotNull LightweightWindowEvent event) {
          disposeCallback.run();
        }
      });
    }
    popup.setShowSubmenuOnHover(true);
    return popup;
  }

  private AnAction[] getCodeGPTModelActions(Project project, Presentation presentation) {
    var userDetails = CODEGPT_USER_DETAILS.get(project);
    var registry = ModelRegistry.getInstance();
    return registry.getProxyAIChatModelsForPricingPlan(
            userDetails == null ? null : userDetails.getPricingPlan()).stream()
        .map(model -> createCodeGPTModelAction(model, presentation))
        .toArray(AnAction[]::new);
  }

  @Override
  protected @NotNull DefaultActionGroup createPopupActionGroup(JComponent button) {
    var presentation = ((ComboBoxButton) button).getPresentation();
    var actionGroup = new DefaultActionGroup();

    actionGroup.addSeparator("Cloud");

    if (availableProviders.contains(PROXYAI)) {
      var proxyAIGroup = DefaultActionGroup.createPopupGroup(() -> "ProxyAI");
      proxyAIGroup.getTemplatePresentation().setIcon(Icons.DefaultSmall);
      proxyAIGroup.addAll(getCodeGPTModelActions(project, presentation));
      actionGroup.add(proxyAIGroup);
    }

    if (availableProviders.contains(ANTHROPIC)) {
      var anthropicGroup = DefaultActionGroup.createPopupGroup(() -> "Anthropic");
      anthropicGroup.getTemplatePresentation().setIcon(Icons.Anthropic);
      anthropicGroup.add(
          createAnthropicModelAction(ModelRegistry.CLAUDE_OPUS_4_20250514, presentation));
      anthropicGroup.add(
          createAnthropicModelAction(ModelRegistry.CLAUDE_SONNET_4_20250514, presentation));
      actionGroup.add(anthropicGroup);
    }

    if (availableProviders.contains(OPENAI)) {
      var openaiGroup = DefaultActionGroup.createPopupGroup(() -> "OpenAI");
      openaiGroup.getTemplatePresentation().setIcon(Icons.OpenAI);
      List.of(
              OpenAIChatCompletionModel.O_4_MINI,
              OpenAIChatCompletionModel.O_3,
              OpenAIChatCompletionModel.O_3_MINI,
              OpenAIChatCompletionModel.O_1_PREVIEW,
              OpenAIChatCompletionModel.O_1_MINI,
              OpenAIChatCompletionModel.GPT_4_1,
              OpenAIChatCompletionModel.GPT_4_1_MINI,
              OpenAIChatCompletionModel.GPT_4_1_NANO,
              OpenAIChatCompletionModel.GPT_4_O,
              OpenAIChatCompletionModel.GPT_4_O_MINI,
              OpenAIChatCompletionModel.GPT_4_0125_128k)
          .forEach(model -> openaiGroup.add(createOpenAIModelAction(model, presentation)));
      actionGroup.add(openaiGroup);
    }

    if (availableProviders.contains(CUSTOM_OPENAI)) {
      List<CustomServiceSettingsState> services = ApplicationManager.getApplication()
          .getService(CustomServicesSettings.class)
          .getState()
          .getServices();

      var customGroup = DefaultActionGroup.createPopupGroup(() -> "Custom OpenAI");
      customGroup.getTemplatePresentation().setIcon(Icons.OpenAI);
      services.forEach(model ->
          customGroup.add(createCustomOpenAIModelAction(model, presentation))
      );
      actionGroup.add(customGroup);
    }

    if (availableProviders.contains(GOOGLE)) {
      var googleGroup = DefaultActionGroup.createPopupGroup(() -> "Google");
      googleGroup.getTemplatePresentation().setIcon(Icons.Google);
      List.of(
              GoogleModel.GEMINI_2_5_PRO_PREVIEW,
              GoogleModel.GEMINI_2_5_FLASH_PREVIEW,
              GoogleModel.GEMINI_2_5_PRO,
              GoogleModel.GEMINI_2_0_PRO_EXP,
              GoogleModel.GEMINI_2_0_FLASH_THINKING_EXP,
              GoogleModel.GEMINI_2_0_FLASH,
              GoogleModel.GEMINI_1_5_PRO)
          .forEach(model -> googleGroup.add(createGoogleModelAction(model, presentation)));
      actionGroup.add(googleGroup);
    }

    if (availableProviders.contains(MISTRAL)) {
      var mistralGroup = DefaultActionGroup.createPopupGroup(() -> "Mistral");
      mistralGroup.getTemplatePresentation().setIcon(Icons.Mistral);
      List.of(
              ModelRegistry.DEVSTRAL_MEDIUM_2507,
              ModelRegistry.MISTRAL_LARGE_2411,
              ModelRegistry.CODESTRAL_LATEST)
          .forEach(model -> mistralGroup.add(createMistralModelAction(model, presentation)));
      actionGroup.add(mistralGroup);
    }

    if (availableProviders.contains(LLAMA_CPP) || availableProviders.contains(OLLAMA)) {
      actionGroup.addSeparator("Offline");

      if (availableProviders.contains(LLAMA_CPP)) {
        actionGroup.add(createLlamaModelAction(presentation));
      }

      if (availableProviders.contains(OLLAMA)) {
        var ollamaGroup = DefaultActionGroup.createPopupGroup(() -> "Ollama");
        ollamaGroup.getTemplatePresentation().setIcon(Icons.Ollama);
        ApplicationManager.getApplication()
            .getService(OllamaSettings.class)
            .getState()
            .getAvailableModels()
            .forEach(model ->
                ollamaGroup.add(createOllamaModelAction(model, presentation)));
        actionGroup.add(ollamaGroup);
      }
    }

    if (showConfigureModels) {
      actionGroup.addSeparator();
      actionGroup.add(new DumbAwareAction("Configure Models", "", AllIcons.General.Settings) {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
          ShowSettingsUtil.getInstance().showSettingsDialog(
              e.getProject(),
              ModelSettingsConfigurable.class
          );
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
          return ActionUpdateThread.BGT;
        }
      });
    }

    return actionGroup;
  }

  @Override
  protected boolean shouldShowDisabledActions() {
    return true;
  }

  private void updateTemplatePresentation(ServiceType selectedService) {
    var application = ApplicationManager.getApplication();
    var templatePresentation = getTemplatePresentation();
    var chatModel = application.getService(ModelSettings.class).getState()
        .getModelSelection(FeatureType.CHAT);
    var modelCode = chatModel != null ? chatModel.getModel() : null;

    switch (selectedService) {
      case PROXYAI:
        var proxyAIModel = ModelRegistry.getInstance().getProxyAIChatModels().stream()
            .filter(it -> modelCode != null && it.getModel().equals(modelCode))
            .findFirst();
        templatePresentation.setIcon(
            proxyAIModel.map(ModelSelection::getIcon).orElse(Icons.CodeGPTModel));
        templatePresentation.setText(
            proxyAIModel.map(ModelSelection::getDisplayName).orElse("Unknown"));
        break;
      case OPENAI:
        templatePresentation.setIcon(Icons.OpenAI);
        var openAIModelName = ModelRegistry.getInstance().getModelDisplayName(OPENAI, modelCode);
        templatePresentation.setText(openAIModelName);
        break;
      case CUSTOM_OPENAI:
        ModelRegistry.getInstance().getCustomOpenAIModels().stream()
            .filter(it -> Objects.requireNonNull(modelCode).equals(it.getModel()))
            .findFirst()
            .ifPresent(selection -> {
              templatePresentation.setIcon(Icons.OpenAI);
              templatePresentation.setText(selection.getModel());
            });
        break;
      case ANTHROPIC:
        templatePresentation.setIcon(Icons.Anthropic);
        var anthropicModelName = ModelRegistry.getInstance()
            .getModelDisplayName(ANTHROPIC, modelCode);
        templatePresentation.setText(anthropicModelName);
        break;
      case LLAMA_CPP:
        templatePresentation.setText(getLlamaCppPresentationText());
        templatePresentation.setIcon(Icons.Llama);
        break;
      case OLLAMA:
        templatePresentation.setIcon(Icons.Ollama);
        templatePresentation.setText(application.getService(OllamaSettings.class)
            .getState()
            .getModel());
        break;
      case GOOGLE:
        templatePresentation.setText(getGooglePresentationText());
        templatePresentation.setIcon(Icons.Google);
        break;
      case MISTRAL:
        templatePresentation.setText(getMistralPresentationText());
        templatePresentation.setIcon(Icons.Mistral);
        break;
      default:
        break;
    }
  }

  private String getGooglePresentationText() {
    var model = ApplicationManager.getApplication().getService(GoogleSettings.class)
        .getState()
        .getModel();
    return ModelRegistry.getInstance().getModelDisplayName(GOOGLE, model);
  }

  private String getLlamaCppPresentationText() {
    var huggingFaceModel = LlamaSettings.getCurrentState().getHuggingFaceModel();
    var llamaModel = LlamaModel.findByHuggingFaceModel(huggingFaceModel);
    return String.format("%s (%dB)",
        llamaModel.getLabel(),
        huggingFaceModel.getParameterSize());
  }


  private AnAction createModelAction(
      ServiceType serviceType,
      String label,
      Icon icon,
      Presentation comboBoxPresentation,
      Runnable onModelChanged) {
    return new DumbAwareAction(label, "", icon) {
      @Override
      public void update(@NotNull AnActionEvent event) {
        var presentation = event.getPresentation();
        presentation.setEnabled(!presentation.getText().equals(comboBoxPresentation.getText()));
      }

      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        if (onModelChanged != null) {
          onModelChanged.run();
        }
        handleModelChange(serviceType);
      }

      @Override
      public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
      }
    };
  }

  private void handleModelChange(ServiceType serviceType) {
    updateTemplatePresentation(serviceType);
    onModelChange.accept(serviceType);
  }

  private AnAction createCodeGPTModelAction(ModelSelection model,
      Presentation comboBoxPresentation) {
    var selected = model.getDisplayName().equals(comboBoxPresentation.getText());
    var locked = shouldLockModel(model);
    return new CodeGPTModelsListPopupAction(model.getDisplayName(), model.getModel(),
        model.getIcon() != null ? model.getIcon() : Icons.CodeGPTModel,
        model.getPricingPlan() != null ? model.getPricingPlan() : PricingPlan.ANONYMOUS,
        locked, selected, () -> {
      var application = ApplicationManager.getApplication();
      application
          .getService(ModelSettings.class)
          .setModel(FeatureType.CHAT, model.getModel(), PROXYAI);

      handleModelChange(PROXYAI);
    });
  }

  private boolean shouldLockModel(ModelSelection model) {
    var userDetails = CODEGPT_USER_DETAILS.get(project);
    if (userDetails != null) {
      if (userDetails.getPricingPlan() == PricingPlan.INDIVIDUAL) {
        return false;
      }
      if (userDetails.getPricingPlan() == PricingPlan.FREE) {
        return model.getPricingPlan() == PricingPlan.INDIVIDUAL;
      }
    }
    return model.getPricingPlan() != PricingPlan.ANONYMOUS;
  }

  private AnAction createOllamaModelAction(String model, Presentation comboBoxPresentation) {
    return createModelAction(OLLAMA, model, Icons.Ollama, comboBoxPresentation,
        () -> {
          var application = ApplicationManager.getApplication();
          application
              .getService(OllamaSettings.class)
              .getState()
              .setModel(model);
          application
              .getService(ModelSettings.class)
              .setModel(FeatureType.CHAT, model, OLLAMA);
        });
  }

  private AnAction createOpenAIModelAction(
      OpenAIChatCompletionModel model,
      Presentation comboBoxPresentation) {
    var modelName = ModelRegistry.getInstance().getModelDisplayName(OPENAI, model.getCode());
    return createModelAction(
        OPENAI,
        modelName,
        Icons.OpenAI,
        comboBoxPresentation,
        () -> ApplicationManager.getApplication().getService(ModelSettings.class)
            .setModel(FeatureType.CHAT, model.getCode(), OPENAI));
  }

  private AnAction createCustomOpenAIModelAction(
      CustomServiceSettingsState model,
      Presentation comboBoxPresentation) {
    return createModelAction(
        CUSTOM_OPENAI,
        model.getName(),
        Icons.OpenAI,
        comboBoxPresentation,
        () -> ApplicationManager.getApplication().getService(ModelSettings.class)
            .setModel(FeatureType.CHAT, model.getName(), CUSTOM_OPENAI));
  }

  private AnAction createGoogleModelAction(GoogleModel model, Presentation comboBoxPresentation) {
    var modelName = ModelRegistry.getInstance().getModelDisplayName(GOOGLE, model.getCode());
    return createModelAction(
        GOOGLE,
        modelName,
        Icons.Google,
        comboBoxPresentation,
        () -> ApplicationManager.getApplication().getService(ModelSettings.class)
            .setModel(FeatureType.CHAT, model.getCode(), GOOGLE));
  }

  private AnAction createAnthropicModelAction(
      String modelCode,
      Presentation comboBoxPresentation) {
    var modelName = ModelRegistry.getInstance().getModelDisplayName(ANTHROPIC, modelCode);
    return createModelAction(
        ANTHROPIC,
        modelName,
        Icons.Anthropic,
        comboBoxPresentation,
        () -> ApplicationManager.getApplication().getService(ModelSettings.class)
            .setModel(FeatureType.CHAT, modelCode, ANTHROPIC));
  }

  private AnAction createLlamaModelAction(Presentation comboBoxPresentation) {
    return createModelAction(
        LLAMA_CPP,
        getLlamaCppPresentationText(),
        Icons.Llama,
        comboBoxPresentation,
        () -> ApplicationManager.getApplication().getService(ModelSettings.class)
            .setModel(FeatureType.CHAT,
                LlamaSettings.getCurrentState().getHuggingFaceModel().getCode(), LLAMA_CPP));
  }

  private AnAction createMistralModelAction(String modelCode, Presentation comboBoxPresentation) {
    var modelName = ModelRegistry.getInstance().getModelDisplayName(MISTRAL, modelCode);
    return createModelAction(
        MISTRAL,
        modelName,
        Icons.Mistral,
        comboBoxPresentation,
        () -> ApplicationManager.getApplication().getService(ModelSettings.class)
            .setModel(FeatureType.CHAT, modelCode, MISTRAL));
  }

  private String getMistralPresentationText() {
    var chatModel = ApplicationManager.getApplication().getService(ModelSettings.class).getState()
        .getModelSelection(FeatureType.CHAT);
    var modelCode = chatModel != null ? chatModel.getModel() : null;
    return ModelRegistry.getInstance().getModelDisplayName(MISTRAL, modelCode);
  }
}
