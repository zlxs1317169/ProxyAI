package ee.carlrobert.codegpt.actions.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.actions.ActionType;
import ee.carlrobert.codegpt.actions.editor.EditorActionsUtil;
import ee.carlrobert.codegpt.metrics.MetricsCollector;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;
import ee.carlrobert.codegpt.telemetry.TelemetryAction;
import org.jetbrains.annotations.NotNull;

public class ClearChatWindowAction extends DumbAwareAction {

  private final Runnable onActionPerformed;

  public ClearChatWindowAction(Runnable onActionPerformed) {
    super("Clear Window", "Clears a chat window", AllIcons.General.Reset);
    this.onActionPerformed = onActionPerformed;
    EditorActionsUtil.registerAction(this);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Project project = event.getProject();
    ProductivityMetrics metrics = null;
    
    // 开始度量收集
    if (project != null) {
      try {
        MetricsCollector collector = MetricsCollector.getInstance(project);
        metrics = collector.startMetrics("clear_chat_window", "CLEAR_CHAT_WINDOW");
        if (metrics != null) {
          metrics.addAdditionalData("actionType", "UI_ACTION");
          metrics.addAdditionalData("component", "ChatWindow");
        }
      } catch (Exception e) {
        // 忽略度量收集错误，不影响主要功能
      }
    }
    
    try {
      onActionPerformed.run();
      
      // 标记成功
      if (project != null && metrics != null) {
        try {
          MetricsCollector.getInstance(project).completeMetrics(metrics, true, null);
        } catch (Exception e) {
          // 忽略度量收集错误
        }
      }
    } catch (Exception e) {
      // 标记失败
      if (project != null && metrics != null) {
        try {
          MetricsCollector.getInstance(project).completeMetrics(metrics, false, e.getMessage());
        } catch (Exception ex) {
          // 忽略度量收集错误
        }
      }
      throw e;
    } finally {
      TelemetryAction.IDE_ACTION.createActionMessage()
          .property("action", ActionType.CLEAR_CHAT_WINDOW.name())
          .send();
    }
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }
}
