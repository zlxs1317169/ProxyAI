package ee.carlrobert.codegpt.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import ee.carlrobert.codegpt.metrics.MetricsDataValidator;
import org.jetbrains.annotations.NotNull;

/**
 * 验证指标收集系统的调试操作
 */
public class ValidateMetricsAction extends AnAction {
    
    public ValidateMetricsAction() {
        super("验证指标收集系统", "手动触发指标收集系统验证", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            // 触发验证
            MetricsDataValidator validator = MetricsDataValidator.getInstance();
            if (validator != null) {
                validator.triggerValidation();
                
                // 显示通知
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("proxyai.notification.group")
                    .createNotification(
                        "指标验证已启动",
                        "正在验证数据收集系统状态，请查看控制台输出",
                        NotificationType.INFORMATION
                    )
                    .notify(e.getProject());
            } else {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("proxyai.notification.group")
                    .createNotification(
                        "验证失败",
                        "无法获取指标验证器实例",
                        NotificationType.ERROR
                    )
                    .notify(e.getProject());
            }
            
        } catch (Exception ex) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("proxyai.notification.group")
                .createNotification(
                    "验证出错",
                    "验证过程中发生错误: " + ex.getMessage(),
                    NotificationType.ERROR
                )
                .notify(e.getProject());
        }
    }
}