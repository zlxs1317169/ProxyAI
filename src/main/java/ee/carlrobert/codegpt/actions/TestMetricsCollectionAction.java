package ee.carlrobert.codegpt.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.metrics.MetricsCollector;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;
import ee.carlrobert.codegpt.metrics.SafeMetricsCollector;
import org.jetbrains.annotations.NotNull;

/**
 * 测试指标收集系统的Action
 */
public class TestMetricsCollectionAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 测试基本指标收集
        testBasicMetricsCollection(project);
        
        // 显示通知
        Notifications.Bus.notify(
            new Notification(
                "proxyai.notification.group",
                "ProxyAI 指标收集测试",
                "指标收集测试已完成，请查看日志获取详细信息",
                NotificationType.INFORMATION
            ),
            project
        );
    }

    private void testBasicMetricsCollection(Project project) {
        // 测试成功场景
        ProductivityMetrics successMetrics = SafeMetricsCollector.safelyStartMetrics(
            project,
            "test_action_success",
            "TEST_ACTION"
        );
        
        if (successMetrics != null) {
            successMetrics.setModelName("TestModel");
                                             successMetrics.setTotalTokenCount(100);
            successMetrics.addAdditionalData("testKey", "testValue");
            
            // 模拟一些处理时间
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            
            SafeMetricsCollector.safelyCompleteMetrics(project, successMetrics, true);
        }
        
        // 测试失败场景
        ProductivityMetrics failureMetrics = SafeMetricsCollector.safelyStartMetrics(
            project,
            "test_action_failure",
            "TEST_ACTION"
        );
        
        if (failureMetrics != null) {
            failureMetrics.setModelName("TestModel");
            failureMetrics.setTotalTokenCount(50);
            failureMetrics.addAdditionalData("errorType", "testError");
            
            // 模拟一些处理时间
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            
            SafeMetricsCollector.safelyCompleteMetrics(
                project, 
                failureMetrics, 
                false, 
                "测试错误场景"
            );
        }
        
        // 输出收集到的指标数量
        int metricsCount = MetricsCollector.getInstance(project).getCompletedMetrics().size();
        System.out.println("已收集指标数量: " + metricsCount);
    }
}