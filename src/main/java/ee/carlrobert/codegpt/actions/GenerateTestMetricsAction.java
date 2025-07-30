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
            if (metrics == null) {
                showNotification(e, "错误", "无法获取ProductivityMetrics实例", NotificationType.ERROR);
                return;
            }
            
            // 生成测试数据
            generateTestData(metrics);
            
            showNotification(e, "测试数据已生成", 
                "已成功生成测试指标数据，可以在提效统计面板中查看", 
                NotificationType.INFORMATION);
            
        } catch (Exception ex) {
            showNotification(e, "生成失败", 
                "生成测试数据时发生错误: " + ex.getMessage(), 
                NotificationType.ERROR);
        }
    }
    
    /**
     * 生成测试指标数据
     */
    private void generateTestData(ProductivityMetrics metrics) {
        // 生成代码补全数据
        for (int i = 0; i < 10; i++) {
            String[] languages = {"java", "python", "javascript", "kotlin", "cpp"};
            String language = languages[random.nextInt(languages.length)];
            int suggestedLines = random.nextInt(10) + 1;
            int acceptedLines = random.nextInt(suggestedLines + 1);
            long responseTime = random.nextInt(500) + 50;
            
            metrics.recordCodeCompletion(language, suggestedLines, acceptedLines, responseTime);
        }
        
        // 生成聊天代码生成数据
        for (int i = 0; i < 5; i++) {
            String[] taskTypes = {"bug_fix", "feature_dev", "refactor", "explain", "optimize"};
            String taskType = taskTypes[random.nextInt(taskTypes.length)];
            int generatedLines = random.nextInt(50) + 10;
            int appliedLines = random.nextInt(generatedLines + 1);
            long sessionDuration = random.nextInt(300000) + 30000; // 30秒到5分钟
            
            metrics.recordChatCodeGeneration(generatedLines, appliedLines, sessionDuration, taskType);
        }
        
        // 生成时间节省数据
        for (int i = 0; i < 8; i++) {
            String[] taskTypes = {"coding", "debugging", "refactoring", "documentation", "testing"};
            String taskType = taskTypes[random.nextInt(taskTypes.length)];
            long traditionalTime = random.nextInt(1800000) + 300000; // 5分钟到30分钟
            long aiAssistedTime = (long) (traditionalTime * (0.3 + random.nextDouble() * 0.4)); // 30%-70%的时间
            int linesOfCode = random.nextInt(100) + 10;
            
            metrics.recordTimeSaving(taskType, traditionalTime, aiAssistedTime, linesOfCode);
        }
        
        // 生成调试时间节省数据
        for (int i = 0; i < 3; i++) {
            String[] issueTypes = {"syntax_error", "logic_error", "performance_issue", "integration_issue"};
            String issueType = issueTypes[random.nextInt(issueTypes.length)];
            long debugTimeWithAI = random.nextInt(600000) + 60000; // 1-10分钟
            long debugTimeWithoutAI = (long) (debugTimeWithAI * (1.5 + random.nextDouble() * 1.5)); // 1.5-3倍时间
            
            metrics.recordDebuggingTimeSaving(debugTimeWithoutAI, debugTimeWithAI, issueType);
        }
        
        // 生成代码质量改进数据
        for (int i = 0; i < 4; i++) {
            String[] metricTypes = {"complexity", "coverage", "maintainability", "performance"};
            String[] improvementTypes = {"refactor", "optimize", "review", "cleanup"};
            String metricType = metricTypes[random.nextInt(metricTypes.length)];
            String improvementType = improvementTypes[random.nextInt(improvementTypes.length)];
            double beforeValue = 50 + random.nextDouble() * 30; // 50-80
            double afterValue = beforeValue + random.nextDouble() * 20; // 改进后的值
            
            metrics.recordCodeQualityImprovement(metricType, beforeValue, afterValue, improvementType);
        }
        
        // 生成学习活动数据
        for (int i = 0; i < 6; i++) {
            String[] topicTypes = {"new_framework", "debugging", "best_practices", "algorithm", "design_pattern"};
            String topicType = topicTypes[random.nextInt(topicTypes.length)];
            int questionsAsked = random.nextInt(10) + 1;
            int conceptsLearned = Math.max(1, questionsAsked / 2 + random.nextInt(3));
            long learningTime = random.nextInt(1800000) + 300000; // 5-30分钟
            
            metrics.recordLearningActivity(topicType, questionsAsked, conceptsLearned, learningTime);
        }
        
        System.out.println("✅ 已生成测试指标数据:");
        System.out.println("- 代码补全记录: 10条");
        System.out.println("- 聊天代码生成: 5条");
        System.out.println("- 时间节省记录: 8条");
        System.out.println("- 调试时间节省: 3条");
        System.out.println("- 代码质量改进: 4条");
        System.out.println("- 学习活动记录: 6条");
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