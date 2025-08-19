package ee.carlrobert.codegpt.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.diagnostics.GrpcConnectionDiagnostic;
import org.jetbrains.annotations.NotNull;

/**
 * gRPC连接状态通知器
 */
public class GrpcConnectionNotifier {
    
    private static final String NOTIFICATION_GROUP_ID = "proxyai.notification.group";
    
    /**
     * 显示gRPC连接失败通知
     */
    public static void notifyConnectionFailure(Project project, String errorMessage) {
        Notification notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(
                "ProxyAI gRPC连接失败",
                "代码补全功能暂时不可用: " + errorMessage,
                NotificationType.WARNING
            );
        
        // 添加诊断操作
        notification.addAction(new NotificationAction("诊断连接问题") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                new GrpcConnectionDiagnostic().actionPerformed(e);
                notification.expire();
            }
        });
        
        // 添加重试操作
        notification.addAction(new NotificationAction("重试连接") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                // 触发连接重试逻辑
                notification.expire();
            }
        });
        
        notification.notify(project);
    }
    
    /**
     * 显示gRPC连接恢复通知
     */
    public static void notifyConnectionRestored(Project project) {
        Notification notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(
                "ProxyAI连接已恢复",
                "代码补全功能现已可用",
                NotificationType.INFORMATION
            );
        
        notification.notify(project);
    }
}