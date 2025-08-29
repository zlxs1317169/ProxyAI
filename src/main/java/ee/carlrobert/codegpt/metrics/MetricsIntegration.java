package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 提效度量系统集成服务
 * 负责初始化和管理整个度量系统
 */
@Service
public final class MetricsIntegration {
    
    private MetricsCollector metricsCollector;
    private boolean isInitialized = false;
    
    public static MetricsIntegration getInstance() {
        return ApplicationManager.getApplication().getService(MetricsIntegration.class);
    }
    
    /**
     * 初始化度量系统
     */
    public void initializeMetricsSystem(Project project) {
        if (isInitialized) {
            return;
        }
        
        try {
            // 初始化数据收集器
            metricsCollector = new MetricsCollector(project);
            
            // 初始化度量服务
            ProductivityMetrics.getInstance();
            
            isInitialized = true;
            System.out.println("ProxyAI 提效度量系统已启动");
            
        } catch (Exception e) {
            System.err.println("初始化提效度量系统时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取度量收集器实例
     */
    public MetricsCollector getMetricsCollector() {
        return metricsCollector;
    }
    
    /**
     * 记录AI代码补全使用
     */
    public void recordAICompletion(String language, String completionText, boolean accepted, long responseTime) {
        if (metricsCollector != null) {
            metricsCollector.recordAICompletionUsage(language, completionText, accepted, responseTime);
        }
    }
    
    /**
     * 记录AI聊天代码生成
     */
    public void recordAIChatGeneration(String generatedCode, String appliedCode, long sessionDuration, String taskType) {
        if (metricsCollector != null) {
            metricsCollector.recordAIChatCodeGeneration(generatedCode, appliedCode, sessionDuration, taskType);
        }
    }
    
    /**
     * 记录调试会话
     */
    public void recordDebuggingSession(long startTime, long endTime, boolean usedAI, String issueType) {
        if (metricsCollector != null) {
            metricsCollector.recordDebuggingSession(startTime, endTime, usedAI, issueType);
        }
    }
    
    /**
     * 记录代码质量改进
     */
    public void recordCodeQualityImprovement(String metricType, double before, double after, String improvementType) {
        if (metricsCollector != null) {
            metricsCollector.recordCodeQualityImprovement(metricType, before, after, improvementType);
        }
    }
    
    /**
     * 记录学习活动
     */
    public void recordLearningActivity(String topic, int questionsAsked, long learningTime) {
        if (metricsCollector != null) {
            metricsCollector.recordLearningActivity(topic, questionsAsked, learningTime);
        }
    }
    
    /**
     * 获取提效报告
     */
    public ProductivityMetrics.ProductivityReport getProductivityReport(int days) {
        return ProductivityMetrics.getInstance().getProductivityReport(days);
    }
    
    /**
     * 检查系统是否已初始化
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}