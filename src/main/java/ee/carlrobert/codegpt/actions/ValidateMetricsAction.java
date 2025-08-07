package ee.carlrobert.codegpt.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import ee.carlrobert.codegpt.metrics.MetricsDataValidator;
import ee.carlrobert.codegpt.metrics.MetricsIntegrationTest;
import ee.carlrobert.codegpt.metrics.SafeMetricsCollector;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;
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
        // 显示选择对话框
        String[] options = {"系统验证", "集成测试", "快速测试", "取消"};
        int choice = Messages.showDialog(
            "请选择要执行的操作：\n\n" +
            "• 系统验证：验证度量系统的基本状态\n" +
            "• 集成测试：运行完整的度量收集测试\n" +
            "• 快速测试：快速验证度量收集是否工作\n",
            "度量系统验证",
            options,
            0,
            Messages.getQuestionIcon()
        );
        
        switch (choice) {
            case 0: // 系统验证
                performSystemValidation(e);
                break;
            case 1: // 集成测试
                performIntegrationTest(e);
                break;
            case 2: // 快速测试
                performQuickTest(e);
                break;
            default: // 取消
                return;
        }
    }
    
    private void performSystemValidation(@NotNull AnActionEvent e) {
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
    
    private void performIntegrationTest(@NotNull AnActionEvent e) {
        try {
            System.out.println("=== 开始效能度量集成测试 ===");
            
            // 运行集成测试
            MetricsIntegrationTest.runAllTests();
            
            NotificationGroupManager.getInstance()
                .getNotificationGroup("proxyai.notification.group")
                .createNotification(
                    "集成测试完成",
                    "效能度量集成测试已完成，请查看控制台输出了解详细结果",
                    NotificationType.INFORMATION
                )
                .notify(e.getProject());
                
        } catch (Exception ex) {
            System.err.println("集成测试失败: " + ex.getMessage());
            ex.printStackTrace();
            
            NotificationGroupManager.getInstance()
                .getNotificationGroup("proxyai.notification.group")
                .createNotification(
                    "集成测试失败",
                    "运行集成测试时发生错误: " + ex.getMessage(),
                    NotificationType.ERROR
                )
                .notify(e.getProject());
        }
    }
    
    private void performQuickTest(@NotNull AnActionEvent e) {
        try {
            System.out.println("=== 开始快速度量测试 ===");
            
            // 获取测试前的数据
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            ProductivityMetrics.ProductivityReport beforeReport = metrics.getProductivityReport(1);
            
            System.out.println("测试前数据:");
            System.out.println("  - 生成代码行数: " + beforeReport.totalLinesGenerated);
            System.out.println("  - 代码接受率: " + String.format("%.2f%%", beforeReport.avgCodeAcceptanceRate * 100));
            
            // 模拟聊天会话
            String sessionId = "quick-test-" + System.currentTimeMillis();
            SafeMetricsCollector.safeStartChatSession(sessionId, "quick_test");
            
            String testResponse = "这是一个测试响应：\n\n```java\npublic class QuickTest {\n    public void test() {\n        System.out.println(\"Quick test\");\n    }\n}\n```\n\n测试完成。";
            String extractedCode = extractCodeFromMessage(testResponse);
            
            SafeMetricsCollector.safeRecordAIResponse(sessionId, testResponse, extractedCode);
            
            System.out.println("已记录测试数据:");
            System.out.println("  - 会话ID: " + sessionId);
            System.out.println("  - 提取的代码行数: " + countLines(extractedCode));
            
            // 等待数据处理
            Thread.sleep(1000);
            
            // 获取测试后的数据
            ProductivityMetrics.ProductivityReport afterReport = metrics.getProductivityReport(1);
            
            System.out.println("测试后数据:");
            System.out.println("  - 生成代码行数: " + afterReport.totalLinesGenerated);
            System.out.println("  - 代码接受率: " + String.format("%.2f%%", afterReport.avgCodeAcceptanceRate * 100));
            
            boolean dataChanged = afterReport.totalLinesGenerated > beforeReport.totalLinesGenerated;
            
            String resultMessage;
            NotificationType notificationType;
            
            if (dataChanged) {
                resultMessage = "✅ 快速测试成功！度量收集正常工作\n" +
                              "新增代码行数: " + (afterReport.totalLinesGenerated - beforeReport.totalLinesGenerated);
                notificationType = NotificationType.INFORMATION;
                System.out.println("✅ 快速测试成功！度量收集正常工作");
            } else {
                resultMessage = "❌ 快速测试失败！度量收集可能有问题\n" +
                              "数据没有变化，请检查控制台输出";
                notificationType = NotificationType.WARNING;
                System.out.println("❌ 快速测试失败！度量收集可能有问题");
            }
            
            NotificationGroupManager.getInstance()
                .getNotificationGroup("proxyai.notification.group")
                .createNotification(
                    "快速测试完成",
                    resultMessage,
                    notificationType
                )
                .notify(e.getProject());
                
        } catch (Exception ex) {
            System.err.println("快速测试失败: " + ex.getMessage());
            ex.printStackTrace();
            
            NotificationGroupManager.getInstance()
                .getNotificationGroup("proxyai.notification.group")
                .createNotification(
                    "快速测试失败",
                    "运行快速测试时发生错误: " + ex.getMessage(),
                    NotificationType.ERROR
                )
                .notify(e.getProject());
        }
    }
    
    private String extractCodeFromMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
        StringBuilder codeBuilder = new StringBuilder();
        String[] lines = message.split("\n");
        boolean inCodeBlock = false;
        
        for (String line : lines) {
            if (line.trim().startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                continue;
            }
            
            if (inCodeBlock) {
                codeBuilder.append(line).append("\n");
            }
        }
        
        return codeBuilder.toString().trim();
    }
    
    private int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\\r?\\n").length;
    }
}