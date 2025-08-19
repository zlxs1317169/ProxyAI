package ee.carlrobert.codegpt.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.metrics.web.MetricsWebServer;
import org.jetbrains.annotations.NotNull;

/**
 * 切换指标Web服务器状态的Action
 */
public class ToggleMetricsWebServerAction extends ToggleAction {

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return false;
        }
        
        return MetricsWebServer.getInstance(project).isRunning();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        
        MetricsWebServer webServer = MetricsWebServer.getInstance(project);
        
        if (state) {
            webServer.start();
            if (webServer.isRunning()) {
                showNotification(
                    project,
                    "指标Web服务器已启动",
                    "访问地址: " + webServer.getWebUrl(),
                    NotificationType.INFORMATION
                );
            }
        } else {
            webServer.stop();
            showNotification(
                project,
                "指标Web服务器已停止",
                "",
                NotificationType.INFORMATION
            );
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        
        Presentation presentation = e.getPresentation();
        Project project = e.getProject();
        
        if (project == null) {
            presentation.setEnabled(false);
            return;
        }
        
        boolean isRunning = MetricsWebServer.getInstance(project).isRunning();
        presentation.setText(isRunning ? "停止指标Web服务器" : "启动指标Web服务器");
    }
    
    private void showNotification(Project project, String title, String content, NotificationType type) {
        Notifications.Bus.notify(
            new Notification(
                "proxyai.notification.group",
                title,
                content,
                type
            ),
            project
        );
    }
}