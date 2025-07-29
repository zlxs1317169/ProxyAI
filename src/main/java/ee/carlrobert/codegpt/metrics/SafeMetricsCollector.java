package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;

/**
 * 安全的指标收集器包装器
 * 确保指标收集错误不会影响插件的正常功能
 */
public class SafeMetricsCollector {
    
    private static final Logger LOG = Logger.getInstance(SafeMetricsCollector.class);
    
    /**
     * 安全地记录AI代码补全使用
     */
    public static void safeRecordAICompletion(String language, String completionText, boolean accepted, long responseTime) {
        try {
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration != null && integration.isInitialized()) {
                integration.recordAICompletion(language, completionText, accepted, responseTime);
            }
        } catch (Exception e) {
            LOG.warn("记录AI补全指标时发生错误，但不影响正常功能", e);
        }
    }
    
    /**
     * 安全地记录AI聊天代码生成
     */
    public static void safeRecordAIChatGeneration(String generatedCode, String appliedCode, long sessionDuration, String taskType) {
        try {
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration != null && integration.isInitialized()) {
                integration.recordAIChatGeneration(generatedCode, appliedCode, sessionDuration, taskType);
            }
        } catch (Exception e) {
            LOG.warn("记录AI聊天生成指标时发生错误，但不影响正常功能", e);
        }
    }
    
    /**
     * 安全地开始聊天会话
     */
    public static void safeStartChatSession(String sessionId, String taskType) {
        try {
            ee.carlrobert.codegpt.metrics.integration.ChatMetricsIntegration.startChatSession(sessionId, taskType);
        } catch (Exception e) {
            LOG.warn("开始聊天会话指标收集时发生错误，但不影响正常功能", e);
        }
    }
    
    /**
     * 安全地记录AI响应
     */
    public static void safeRecordAIResponse(String sessionId, String response, String generatedCode) {
        try {
            ee.carlrobert.codegpt.metrics.integration.ChatMetricsIntegration.recordAIResponse(sessionId, response, generatedCode);
        } catch (Exception e) {
            LOG.warn("记录AI响应指标时发生错误，但不影响正常功能", e);
        }
    }
    
    /**
     * 安全地记录代码补全指标
     */
    public static void safeRecordCodeCompletionMetrics(com.intellij.openapi.editor.Editor editor, String completionText, boolean accepted, long responseTime) {
        try {
            ee.carlrobert.codegpt.metrics.integration.CodeCompletionMetricsIntegration.recordCodeCompletionMetrics(
                editor, completionText, accepted, responseTime
            );
        } catch (Exception e) {
            LOG.warn("记录代码补全指标时发生错误，但不影响正常功能", e);
        }
    }
}