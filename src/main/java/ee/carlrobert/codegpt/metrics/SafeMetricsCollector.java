package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

/**
 * 安全的指标收集工具类，提供静态方法以确保即使在指标收集失败的情况下也不会影响主要功能
 */
public class SafeMetricsCollector {
    private static final Logger LOG = Logger.getInstance(SafeMetricsCollector.class);

    private SafeMetricsCollector() {
        // 工具类，不允许实例化
    }

    /**
     * 安全地记录操作开始
     */
    public static ProductivityMetrics safelyStartMetrics(Project project, String actionId, String actionType) {
        if (project == null) {
            return null;
        }

        try {
            return MetricsCollector.getInstance(project).startMetrics(actionId, actionType);
        } catch (Exception e) {
            LOG.warn("Failed to safely start metrics", e);
            return null;
        }
    }

    /**
     * 安全地记录操作完成
     */
    public static void safelyCompleteMetrics(Project project, ProductivityMetrics metrics, boolean successful) {
        safelyCompleteMetrics(project, metrics, successful, null);
    }

    /**
     * 安全地记录操作完成，包含错误信息
     */
    public static void safelyCompleteMetrics(Project project, ProductivityMetrics metrics, boolean successful, String errorMessage) {
        if (project == null || metrics == null) {
            return;
        }

        try {
            MetricsCollector.getInstance(project).completeMetrics(metrics, successful, errorMessage);
        } catch (Exception e) {
            LOG.warn("Failed to safely complete metrics", e);
        }
    }

    /**
     * 安全地添加额外数据
     */
    public static void safelyAddAdditionalData(ProductivityMetrics metrics, String key, Object value) {
        if (metrics == null) {
            return;
        }

        try {
            metrics.addAdditionalData(key, value);
        } catch (Exception e) {
            LOG.warn("Failed to safely add additional data to metrics", e);
        }
    }
    
    /**
     * 安全地开始聊天会话并记录指标
     */
    public static ProductivityMetrics safeStartChatSession(String sessionId, String chatType) {
        try {
            ProductivityMetrics metrics = new ProductivityMetrics("chat_session_" + sessionId, "chat");
            metrics.addAdditionalData("chat_type", chatType);
            metrics.addAdditionalData("session_id", sessionId);
            return metrics;
        } catch (Exception e) {
            LOG.warn("Failed to safely start chat session metrics", e);
            return null;
        }
    }
    
    /**
     * 安全地结束聊天会话并记录指标
     */
    public static void safeEndChatSession(Project project, ProductivityMetrics metrics, boolean successful) {
        if (project == null || metrics == null) {
            return;
        }

        try {
            metrics.complete();
            if (successful) {
                metrics.markSuccessful();
            } else {
                metrics.markFailed("Chat session ended with errors");
            }
            
            MetricsCollector.getInstance(project).completeMetrics(metrics, successful, null);
        } catch (Exception e) {
            LOG.warn("Failed to safely end chat session metrics", e);
        }
    }
    
    /**
     * 安全地记录代码补全使用情况
     */
    public static ProductivityMetrics safeTrackCodeCompletion(Project project, String filePath, String language) {
        if (project == null) {
            return null;
        }

        try {
            ProductivityMetrics metrics = MetricsCollector.getInstance(project)
                .startMetrics("code_completion", "completion");
            metrics.addAdditionalData("file_path", filePath);
            metrics.addAdditionalData("language", language);
            return metrics;
        } catch (Exception e) {
            LOG.warn("Failed to safely track code completion", e);
            return null;
        }
    }
    
    /**
     * 安全地记录代码补全指标（用于 Kotlin 集成）
     */
    public static void safeRecordCodeCompletionMetrics(
            com.intellij.openapi.editor.Editor editor,
            String text,
            boolean accepted,
            long processingTime) {
        
        try {
                                    // CodeCompletionMetricsIntegration.recordCodeCompletionMetrics(editor, text, accepted, processingTime);
                        // 暂时注释掉，因为 CodeCompletionMetricsIntegration 类不存在
        } catch (Exception e) {
            LOG.warn("Failed to safely record code completion metrics", e);
        }
    }
    
    /**
     * 安全地记录代码补全请求
     */
    public static void recordCodeCompletionRequest(Project project) {
        try {
            ProductivityMetrics metrics = safelyStartMetrics(project, "code_completion_request", "CODE_COMPLETION_REQUEST");
            if (metrics != null) {
                metrics.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                safelyCompleteMetrics(project, metrics, true);
            }
        } catch (Exception e) {
            LOG.warn("Failed to record code completion request", e);
        }
    }
    
    /**
     * 安全地记录代码补全错误
     */
    public static void recordCodeCompletionError(Project project, String errorMessage) {
        try {
            ProductivityMetrics metrics = safelyStartMetrics(project, "code_completion_error", "CODE_COMPLETION_ERROR");
            if (metrics != null) {
                metrics.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                safelyCompleteMetrics(project, metrics, false, errorMessage);
            }
        } catch (Exception e) {
            LOG.warn("Failed to record code completion error", e);
        }
    }
    
    /**
     * 安全地记录AI响应
     */
    public static void safeRecordAIResponse(String sessionId, String response, String extractedCode) {
        try {
            // 这里应该记录AI响应相关的指标
            // 暂时只是记录日志
            LOG.info("AI Response recorded for session: " + sessionId + ", response length: " + 
                    (response != null ? response.length() : 0) + ", code length: " + 
                    (extractedCode != null ? extractedCode.length() : 0));
        } catch (Exception e) {
            LOG.warn("Failed to record AI response", e);
        }
    }
    
    /**
     * 安全地记录AI聊天代码生成
     */
    public static void safeRecordAIChatGeneration(String generatedCode, String appliedCode, long sessionDuration, String taskType) {
        try {
            // 这里应该记录AI聊天代码生成相关的指标
            // 暂时只是记录日志
            LOG.info("AI Chat Code Generation recorded: generated=" + 
                    (generatedCode != null ? generatedCode.length() : 0) + 
                    ", applied=" + (appliedCode != null ? appliedCode.length() : 0) + 
                    ", duration=" + sessionDuration + "ms, taskType=" + taskType);
        } catch (Exception e) {
            LOG.warn("Failed to record AI chat code generation", e);
        }
    }
    
    /**
     * 安全地记录AI补全（用于兼容性）
     */
    public static void safeRecordAICompletion(String language, String completionText, boolean accepted, long responseTime) {
        try {
            // 这里应该记录AI补全相关的指标
            // 暂时只是记录日志
            LOG.info("AI Completion recorded: language=" + language + 
                    ", text length=" + (completionText != null ? completionText.length() : 0) + 
                    ", accepted=" + accepted + ", responseTime=" + responseTime + "ms");
        } catch (Exception e) {
            LOG.warn("Failed to record AI completion", e);
        }
    }
}