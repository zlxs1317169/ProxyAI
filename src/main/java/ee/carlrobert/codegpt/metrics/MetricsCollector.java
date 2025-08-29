package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.metrics.export.MetricsExporter;
import ee.carlrobert.codegpt.metrics.integration.CodeCompletionMetricsIntegration;
import ee.carlrobert.codegpt.metrics.integration.ChatMetricsIntegration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 核心指标收集服务，负责收集、处理和存储效能度量指标
 */
@Service
public final class MetricsCollector {
    private static final Logger LOG = Logger.getInstance(MetricsCollector.class);
    private final ConcurrentMap<String, ProductivityMetrics> activeMetrics = new ConcurrentHashMap<>();
    private final List<ProductivityMetrics> completedMetrics = new ArrayList<>();
    private final String sessionId;
    private final Project project;
    private boolean metricsEnabled = true;

    public MetricsCollector(Project project) {
        this.project = project;
        this.sessionId = UUID.randomUUID().toString();
        LOG.info("Metrics collector initialized with session ID: " + sessionId);
    }

    public static MetricsCollector getInstance(Project project) {
        return project.getService(MetricsCollector.class);
    }

    public ProductivityMetrics startMetrics(String actionId, String actionType) {
        if (!metricsEnabled) {
            return null;
        }

        try {
            ProductivityMetrics metrics = new ProductivityMetrics(actionId, actionType);
            metrics.setSessionId(sessionId);
            String metricsId = UUID.randomUUID().toString();
            activeMetrics.put(metricsId, metrics);
            LOG.debug("Started metrics collection for action: " + actionId);
            return metrics;
        } catch (Exception e) {
            LOG.warn("Failed to start metrics collection", e);
            return null;
        }
    }

    public void completeMetrics(ProductivityMetrics metrics, boolean successful, String errorMessage) {
        if (metrics == null || !metricsEnabled) {
            return;
        }

        try {
            metrics.complete();
            if (successful) {
                metrics.markSuccessful();
            } else {
                metrics.markFailed(errorMessage);
            }
            
            completedMetrics.add(metrics);
            activeMetrics.values().remove(metrics);
            
            // 异步保存指标数据
            // MetricsExporter.getInstance(project).exportMetrics(metrics);
            // 暂时注释掉，因为 MetricsExporter 没有 getInstance 方法
            
            LOG.debug("Completed metrics collection for action: " + metrics.getActionId());
        } catch (Exception e) {
            LOG.warn("Failed to complete metrics collection", e);
        }
    }

    public void setMetricsEnabled(boolean enabled) {
        this.metricsEnabled = enabled;
        LOG.info("Metrics collection " + (enabled ? "enabled" : "disabled"));
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public List<ProductivityMetrics> getCompletedMetrics() {
        return new ArrayList<>(completedMetrics);
    }

    public void clearCompletedMetrics() {
        completedMetrics.clear();
    }

    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * 存储活跃指标（用于兼容性）
     */
    public void storeActiveMetrics(String sessionId, ProductivityMetrics metrics) {
        if (metrics != null && metricsEnabled) {
            String metricsId = UUID.randomUUID().toString();
            activeMetrics.put(metricsId, metrics);
            LOG.debug("Stored active metrics for session: " + sessionId);
        }
    }
    
    /**
     * 记录打字活动（用于兼容性）
     */
    public void recordTypingActivity(String fileName, int linesTyped, boolean isAIAssisted) {
        if (!metricsEnabled) {
            return;
        }
        
        try {
            ProductivityMetrics metrics = new ProductivityMetrics("typing_activity", "TYPING");
            metrics.addAdditionalData("fileName", fileName);
            metrics.addAdditionalData("linesTyped", linesTyped);
            metrics.addAdditionalData("isAIAssisted", isAIAssisted);
            metrics.complete();
            metrics.markSuccessful();
            completedMetrics.add(metrics);
            LOG.debug("Recorded typing activity: " + fileName + ", lines: " + linesTyped);
        } catch (Exception e) {
            LOG.warn("Failed to record typing activity", e);
        }
    }
    
    /**
     * 记录AI补全使用情况（用于兼容性）
     */
    public void recordAICompletionUsage(String language, String completionText, boolean accepted, long processingTime) {
        if (!metricsEnabled) {
            return;
        }
        
        try {
            ProductivityMetrics metrics = new ProductivityMetrics("ai_completion_usage", "AI_COMPLETION");
            metrics.recordCodeCompletion(language, completionText.length(), accepted ? completionText.length() : 0, processingTime);
            metrics.complete();
            metrics.markSuccessful();
            completedMetrics.add(metrics);
            LOG.info("AI补全使用记录: " + language + ", 接受: " + accepted + ", 响应时间: " + processingTime + "ms");
        } catch (Exception e) {
            LOG.warn("Failed to record AI completion usage", e);
        }
    }
    
    /**
     * 运行活动（用于兼容性）
     */
    public void runActivity(Project project) {
        if (!metricsEnabled) {
            return;
        }
        
        try {
            LOG.info("Running metrics activity for project: " + project.getName());
            // 这里可以添加具体的活动逻辑
        } catch (Exception e) {
            LOG.warn("Failed to run metrics activity", e);
        }
    }
    
    /**
     * 记录AI聊天代码生成（用于兼容性）
     */
    public void recordAIChatCodeGeneration(String generatedCode, String appliedCode, long sessionDuration, String taskType) {
        if (!metricsEnabled) {
            return;
        }
        
        try {
            ProductivityMetrics metrics = new ProductivityMetrics("ai_chat_code_generation", "AI_CHAT_CODE");
            metrics.addAdditionalData("generatedCodeLength", generatedCode != null ? generatedCode.length() : 0);
            metrics.addAdditionalData("appliedCodeLength", appliedCode != null ? appliedCode.length() : 0);
            metrics.addAdditionalData("sessionDuration", sessionDuration);
            metrics.addAdditionalData("taskType", taskType);
            metrics.complete();
            metrics.markSuccessful();
            completedMetrics.add(metrics);
            LOG.debug("Recorded AI chat code generation: " + taskType);
        } catch (Exception e) {
            LOG.warn("Failed to record AI chat code generation", e);
        }
    }
    
    /**
     * 记录调试会话（用于兼容性）
     */
    public void recordDebuggingSession(long startTime, long endTime, boolean usedAI, String issueType) {
        if (!metricsEnabled) {
            return;
        }
        
        try {
            ProductivityMetrics metrics = new ProductivityMetrics("debugging_session", "DEBUGGING");
            metrics.addAdditionalData("startTime", startTime);
            metrics.addAdditionalData("endTime", endTime);
            metrics.addAdditionalData("duration", endTime - startTime);
            metrics.addAdditionalData("usedAI", usedAI);
            metrics.addAdditionalData("issueType", issueType);
            metrics.complete();
            metrics.markSuccessful();
            completedMetrics.add(metrics);
            LOG.debug("Recorded debugging session: " + issueType);
        } catch (Exception e) {
            LOG.warn("Failed to record debugging session", e);
        }
    }
    
    /**
     * 记录代码质量改进（用于兼容性）
     */
    public void recordCodeQualityImprovement(String metricType, double before, double after, String improvementType) {
        if (!metricsEnabled) {
            return;
        }
        
        try {
            ProductivityMetrics metrics = new ProductivityMetrics("code_quality_improvement", "QUALITY_IMPROVEMENT");
            metrics.addAdditionalData("metricType", metricType);
            metrics.addAdditionalData("beforeValue", before);
            metrics.addAdditionalData("afterValue", after);
            metrics.addAdditionalData("improvement", after - before);
            metrics.addAdditionalData("improvementType", improvementType);
            metrics.complete();
            metrics.markSuccessful();
            completedMetrics.add(metrics);
            LOG.debug("Recorded code quality improvement: " + metricType);
        } catch (Exception e) {
            LOG.warn("Failed to record code quality improvement", e);
        }
    }
    
    /**
     * 记录学习活动（用于兼容性）
     */
    public void recordLearningActivity(String topic, int questionsAsked, long learningTime) {
        if (!metricsEnabled) {
            return;
        }
        
        try {
            ProductivityMetrics metrics = new ProductivityMetrics("learning_activity", "LEARNING");
            metrics.addAdditionalData("topic", topic);
            metrics.addAdditionalData("questionsAsked", questionsAsked);
            metrics.addAdditionalData("learningTime", learningTime);
            metrics.complete();
            metrics.markSuccessful();
            completedMetrics.add(metrics);
            LOG.debug("Recorded learning activity: " + topic);
        } catch (Exception e) {
            LOG.warn("Failed to record learning activity", e);
        }
    }
}