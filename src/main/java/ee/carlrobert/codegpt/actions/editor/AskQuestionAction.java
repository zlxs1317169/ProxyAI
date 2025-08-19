package ee.carlrobert.codegpt.actions.editor;

import static java.lang.String.format;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;
import ee.carlrobert.codegpt.Icons;
import ee.carlrobert.codegpt.completions.CompletionRequestUtil;
import ee.carlrobert.codegpt.conversations.message.Message;
import ee.carlrobert.codegpt.toolwindow.chat.ChatToolWindowContentManager;
import ee.carlrobert.codegpt.ui.UIUtil;
import ee.carlrobert.codegpt.metrics.SafeMetricsCollector;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.Nullable;

public class AskQuestionAction extends BaseEditorAction {

  private static String previousUserPrompt = "";

  AskQuestionAction() {
    super(Icons.QuestionMark);
  }

  @Override
  protected void actionPerformed(Project project, Editor editor, String selectedText) {
    if (selectedText != null && !selectedText.isEmpty()) {
      var dialog = new CustomPromptDialog(previousUserPrompt);
      if (dialog.showAndGet()) {
        previousUserPrompt = dialog.getUserPrompt();
        var formattedCode =
            CompletionRequestUtil.formatCode(selectedText, editor.getVirtualFile().getPath());
        var message = new Message(format("%s\n\n%s", previousUserPrompt, formattedCode));
        SwingUtilities.invokeLater(() -> {
            // 开始聊天会话并记录指标
            String sessionId = java.util.UUID.randomUUID().toString();
            // ProductivityMetrics metrics = SafeMetricsCollector.safeStartChatSession(sessionId, "code_question");
            // if (metrics != null) {
            //     metrics.addAdditionalData("selected_text_length", selectedText.length());
            //     metrics.addAdditionalData("file_path", editor.getVirtualFile().getPath());
            //     metrics.addAdditionalData("file_type", editor.getVirtualFile().getFileType().getName());
            //     metrics.addAdditionalData("prompt_length", previousUserPrompt.length());
            //     
            //     // 将指标对象存储到项目服务中，以便在响应时完成指标收集
            //     // MetricsCollector.getInstance(project).storeActiveMetrics(sessionId, metrics);
            // }
            
            project.getService(ChatToolWindowContentManager.class).sendMessage(message);
        });
      }
    }
  }

  public static class CustomPromptDialog extends DialogWrapper {

    private final JTextArea userPromptTextArea;

    public CustomPromptDialog(String previousUserPrompt) {
      super(true);
      this.userPromptTextArea = new JTextArea(previousUserPrompt);
      this.userPromptTextArea.setCaretPosition(previousUserPrompt.length());
      setTitle("Custom Prompt");
      setSize(400, getRootPane().getPreferredSize().height);
      init();
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
      return userPromptTextArea;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
      userPromptTextArea.setLineWrap(true);
      userPromptTextArea.setWrapStyleWord(true);
      userPromptTextArea.setMargin(JBUI.insets(5));
      UIUtil.addShiftEnterInputMap(userPromptTextArea, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          clickDefaultButton();
        }
      });

      return FormBuilder.createFormBuilder()
          .addComponent(UI.PanelFactory.panel(userPromptTextArea)
              .withLabel("Prefix:")
              .moveLabelOnTop()
              .withComment("Example: Find bugs in the following code")
              .createPanel())
          .getPanel();
    }

    public String getUserPrompt() {
      return userPromptTextArea.getText();
    }
  }
}
