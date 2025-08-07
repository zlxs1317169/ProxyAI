package ee.carlrobert.codegpt.toolwindow.chat.ui.textarea;

import static com.intellij.openapi.editor.EditorKind.MAIN_EDITOR;
import static java.lang.String.format;

import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.Strings;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import ee.carlrobert.codegpt.EncodingManager;
import ee.carlrobert.codegpt.conversations.Conversation;
import ee.carlrobert.codegpt.psistructure.ClassStructureSerializer;
import ee.carlrobert.codegpt.settings.service.FeatureType;
import ee.carlrobert.codegpt.settings.service.ModelSelectionService;
import ee.carlrobert.codegpt.settings.configuration.ConfigurationSettings;
import ee.carlrobert.codegpt.settings.prompts.PromptsSettings;
import ee.carlrobert.codegpt.settings.service.ServiceType;
import ee.carlrobert.codegpt.toolwindow.chat.structure.data.PsiStructureRepository;
import ee.carlrobert.codegpt.util.coroutines.CoroutineDispatchers;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.JPanel;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TotalTokensPanel extends JPanel {

  private final EncodingManager encodingManager = EncodingManager.getInstance();
  private final TotalTokensDetails totalTokensDetails;
  private final JBLabel label;

  public TotalTokensPanel(
      Conversation conversation,
      @Nullable String highlightedText,
      Disposable parentDisposable,
      PsiStructureRepository psiStructureRepository
  ) {
    super(new FlowLayout(FlowLayout.LEADING, 0, 0));
    this.totalTokensDetails = createTokenDetails(conversation, highlightedText);
    this.label = getLabel(totalTokensDetails);

    new PsiStructureTotalTokenProvider(
        parentDisposable,
        ClassStructureSerializer.INSTANCE,
        encodingManager,
        new CoroutineDispatchers(),
        psiStructureRepository,
        psiTokens -> {
          if (ConfigurationSettings.getState().getChatCompletionSettings()
              .getPsiStructureEnabled()) {
            updatePsiTokenCount(psiTokens);
          } else {
            updatePsiTokenCount(0);
          }
          return Unit.INSTANCE;
        }
    );

    setBorder(JBUI.Borders.empty(4));
    setOpaque(false);
    add(getContextHelpIcon(totalTokensDetails));
    add(Box.createHorizontalStrut(4));
    add(label);
    addSelectionListeners(parentDisposable);
  }

  private void addSelectionListeners(Disposable parentDisposable) {
    var editorFactory = EditorFactory.getInstance();
    for (var editor : editorFactory.getAllEditors()) {
      addEditorSelectionListener(editor);
    }

    editorFactory.addEditorFactoryListener(new EditorFactoryListener() {
      @Override
      public void editorCreated(@NotNull EditorFactoryEvent event) {
        addEditorSelectionListener(event.getEditor());
      }
    }, parentDisposable);
  }

  private void addEditorSelectionListener(Editor editor) {
    if (MAIN_EDITOR.equals(editor.getEditorKind())) {
      editor.getSelectionModel().addSelectionListener(getSelectionListener());
    }
  }

  private SelectionListener getSelectionListener() {
    return new SelectionListener() {
      @Override
      public void selectionChanged(@NotNull SelectionEvent e) {
        updateHighlightedTokens(e.getEditor().getDocument().getText(e.getNewRange()));
      }
    };
  }

  public TotalTokensDetails getTokenDetails() {
    return totalTokensDetails;
  }

  public void update() {
    update(totalTokensDetails.getTotal());
  }

  public void update(int total) {
    label.setText(getLabelHtml(total));
  }

  public void updatePsiTokenCount(int psiTokenCount) {
    totalTokensDetails.setPsiTokens(psiTokenCount);
    update();
  }

  public void updateConversationTokens(Conversation conversation) {
    totalTokensDetails.setConversationTokens(encodingManager.countConversationTokens(conversation));
    update();
  }

  public void updateUserPromptTokens(String userPrompt) {
    totalTokensDetails.setUserPromptTokens(encodingManager.countTokens(userPrompt));
    update();
  }

  public void updateHighlightedTokens(String highlightedText) {
    totalTokensDetails.setHighlightedTokens(encodingManager.countTokens(highlightedText));
    update();
  }

  public void updateReferencedFilesTokens(List<String> includedFileContents) {
    totalTokensDetails.setReferencedFilesTokens(
        encodingManager.countTokens(Strings.join(includedFileContents, "\n")));
    update();
  }

  private TotalTokensDetails createTokenDetails(
      Conversation conversation,
      @Nullable String highlightedText) {
    var tokenDetails = new TotalTokensDetails(
        encodingManager.countTokens(PromptsSettings.getSelectedPersonaSystemPrompt()));
    tokenDetails.setConversationTokens(encodingManager.countConversationTokens(conversation));
    if (highlightedText != null) {
      tokenDetails.setHighlightedTokens(encodingManager.countTokens(highlightedText));
    }
    return tokenDetails;
  }

  private JBLabel getContextHelpIcon(TotalTokensDetails totalTokensDetails) {
    var iconLabel = new JBLabel(General.ContextHelp);
    iconLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        var html = new LinkedHashMap<>(Map.of(
            "System Prompt", totalTokensDetails.getSystemPromptTokens(),
            "Conversation Tokens", totalTokensDetails.getConversationTokens(),
            "Input Tokens", totalTokensDetails.getUserPromptTokens(),
            "Highlighted Tokens", totalTokensDetails.getHighlightedTokens(),
            "Referenced Files Tokens", totalTokensDetails.getReferencedFilesTokens(),
            "Dependency Structure Tokens", totalTokensDetails.getPsiTokens()))
            .entrySet().stream()
            .map(entry -> format(
                "<p style=\"margin: 0; padding: 0;\"><small>%s: <strong>%d</strong></small></p>",
                entry.getKey(),
                entry.getValue()))
            .collect(Collectors.joining());
        iconLabel.setToolTipText(getIconToolTipText(html));
      }
    });
    return iconLabel;
  }

  private String getIconToolTipText(String html) {
    if (ModelSelectionService.getInstance().getServiceForFeature(FeatureType.CHAT) != ServiceType.OPENAI) {
      return """
          <html>
          <body style="margin: 0; padding: 0;">
          %s
          <p style="margin-top: 8px;">
          <small>
          <strong>Note:</strong> Output values might vary across different large language models
          due to variations in their encoding methods.
          </small>
          </p>
          </body>
          </html>""".formatted(html);
    }
    return "<html" + html + "</html>";
  }

  private String getLabelHtml(int total) {
    return format("<html><small>Tokens: <strong>%d</strong></small></html>", total);
  }

  private JBLabel getLabel(TotalTokensDetails totalTokensDetails) {
    return new JBLabel(getLabelHtml(totalTokensDetails.getTotal()));
  }
}