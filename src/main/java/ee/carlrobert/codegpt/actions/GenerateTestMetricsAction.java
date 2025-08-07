package ee.carlrobert.codegpt.actions;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * 生成测试指标数据的调试操作
 */
public class GenerateTestMetricsAction extends AnAction {
    
    private final Random random = new Random();
    
    public GenerateTestMetricsAction() {
        super("生成测试指标数据", "生成一些测试用的指标数据来验证系统功能", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            
            // 生成代码补全测试数据
            String[] languages = {"java", "python", "javascript", "typescript", "kotlin"};
            for (int i = 0; i < 5; i++) {
                String language = languages[random.nextInt(languages.length)];
                int suggestedLines = 3 + random.nextInt(12); // 3-15行
                int acceptedLines = Math.max(1, suggestedLines - random.nextInt(3)); // 接受大部分
                long responseTime = 50L + random.nextInt(200); // 50-250ms
                
                metrics.recordCodeCompletion(language, suggestedLines, acceptedLines, responseTime);
            }
            
            // 生成聊天代码生成测试数据
            String[] taskTypes = {"bug_fix", "feature_dev", "refactor", "explain", "optimize"};
            for (int i = 0; i < 3; i++) {
                int generatedLines = 8 + random.nextInt(15); // 8-23行
                int appliedLines = Math.max(1, generatedLines - random.nextInt(3)); // 应用大部分
                long sessionDuration = 20000L + random.nextInt(40000); // 20-60秒
                String taskType = taskTypes[random.nextInt(taskTypes.length)];
                
                metrics.recordChatCodeGeneration(generatedLines, appliedLines, sessionDuration, taskType);
            }
            
            // 生成时间节省测试数据
            for (int i = 0; i < 2; i++) {
                String taskType = taskTypes[random.nextInt(taskTypes.length)];
                long traditionalTime = 300000L + random.nextInt(600000); // 5-15分钟
                long aiAssistedTime = traditionalTime / 2 + random.nextInt((int)(traditionalTime / 4)); // AI辅助节省时间
                int linesOfCode = 20 + random.nextInt(50);
                
                metrics.recordTimeSaving(taskType, traditionalTime, aiAssistedTime, linesOfCode);
            }
            
            showNotification(e, "测试数据生成完成", 
                "已生成测试指标数据:\n" +
                "- 5个代码补全记录\n" +
                "- 3个聊天代码生成记录\n" +
                "- 2个时间节省记录", 
                NotificationType.INFORMATION);
            
            System.out.println("✅ 测试指标数据生成完成");
            
        } catch (Exception ex) {
            showNotification(e, "操作失败", 
                "生成测试数据时发生错误: " + ex.getMessage(), 
                NotificationType.ERROR);
        }
    }
    
    /**
     * 显示通知
     */
    private void showNotification(AnActionEvent e, String title, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("proxyai.notification.group")
            .createNotification(title, content, type)
            .notify(e.getProject());
    }
}