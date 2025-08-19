package ee.carlrobert.codegpt.actions.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import ee.carlrobert.codegpt.actions.ActionType;
import ee.carlrobert.codegpt.actions.editor.EditorActionsUtil;
import ee.carlrobert.codegpt.telemetry.TelemetryAction;
import ee.carlrobert.codegpt.metrics.SafeMetricsCollector;
import org.jetbrains.annotations.NotNull;

public class CreateNewConversationAction extends AnAction {

  private final Runnable onCreate;

  public CreateNewConversationAction(Runnable onCreate) {
    super("Create New Chat", "Create new chat", AllIcons.General.Add);
    this.onCreate = onCreate;
    EditorActionsUtil.registerAction(this);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    try {
      var project = event.getProject();
      if (project != null) {
        // 记录新对话创建指标
        String sessionId = java.util.UUID.randomUUID().toString();
        // ProductivityMetrics metrics = SafeMetricsCollector.safeStartChatSession(sessionId, "new_conversation");
        // if (metrics != null) {
        //     metrics.addAdditionalData("project_name", project.getName());
        //     metrics.addAdditionalData("creation_time", System.currentTimeMillis());
        //     
        //     // 将指标对象存储到项目服务中，以便在对话结束时完成指标收集
        //     // MetricsCollector.getInstance(project).storeActiveMetrics(sessionId, metrics);
        // }
        
        onCreate.run();
      }
    } finally {
      TelemetryAction.IDE_ACTION.createActionMessage()
          .property("action", ActionType.CREATE_NEW_CHAT.name())
          .send();
    }
  }
}
