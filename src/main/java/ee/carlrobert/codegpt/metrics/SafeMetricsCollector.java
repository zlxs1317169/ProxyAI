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
            // 创建新的指标实例并存储到数据库
            ProductivityMetrics metrics = new ProductivityMetrics("code_completion", "CODE_COMPLETION");
            
            String language = "unknown";
            if (editor != null && editor.getDocument() != null) {
                String fileName = editor.getDocument().toString();
                if (fileName.contains(".java")) language = "java";
                else if (fileName.contains(".kt")) language = "kotlin";
                else if (fileName.contains(".py")) language = "python";
                else if (fileName.contains(".js")) language = "javascript";
            }
            
            // 设置指标数据
            metrics.setProgrammingLanguage(language);
            metrics.setLinesGenerated(text.length());
            metrics.setLinesAccepted(accepted ? text.length() : 0);
            metrics.setLinesRejected(accepted ? 0 : text.length());
            metrics.setResponseTime(processingTime);
            metrics.setSuccessful(accepted);
            
            // 存储到数据库
            MetricsDatabaseManager.getInstance().saveMetrics(metrics);
            
            LOG.info("代码补全指标记录: language=" + language + ", 接受: " + accepted + ", 处理时间: " + processingTime + "ms");
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
            // 创建新的指标实例并存储到数据库
            ProductivityMetrics metrics = new ProductivityMetrics("ai_response", "AI_RESPONSE");
            metrics.setSessionId(sessionId);
            metrics.setResponseLength(response != null ? response.length() : 0);
            metrics.setCodeLength(extractedCode != null ? extractedCode.length() : 0);
            metrics.setSuccessful(true);
            
            // 存储到数据库
            MetricsDatabaseManager.getInstance().saveMetrics(metrics);
            
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
            // 创建新的指标实例并存储到数据库
            ProductivityMetrics metrics = new ProductivityMetrics("ai_chat_generation", "AI_CHAT_GENERATION");
            metrics.setLinesGenerated(generatedCode != null ? generatedCode.length() : 0);
            metrics.setLinesAccepted(appliedCode != null ? appliedCode.length() : 0);
            metrics.setSessionDuration(sessionDuration);
            metrics.setSuccessful(true);
            metrics.addAdditionalData("task_type", taskType);
            
            // 存储到数据库
            MetricsDatabaseManager.getInstance().saveMetrics(metrics);
            
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
            // 创建新的指标实例并存储到数据库
            ProductivityMetrics metrics = new ProductivityMetrics("ai_completion", "AI_COMPLETION");
            metrics.setProgrammingLanguage(language);
            metrics.setLinesGenerated(completionText != null ? completionText.length() : 0);
            metrics.setLinesAccepted(accepted ? (completionText != null ? completionText.length() : 0) : 0);
            metrics.setLinesRejected(accepted ? 0 : (completionText != null ? completionText.length() : 0));
            metrics.setResponseTime(responseTime);
            metrics.setSuccessful(accepted);
            
            // 存储到数据库
            MetricsDatabaseManager.getInstance().saveMetrics(metrics);
            
            LOG.info("AI Completion recorded: language=" + language + 
                    ", text length=" + (completionText != null ? completionText.length() : 0) + 
                    ", accepted=" + accepted + ", responseTime=" + responseTime + "ms");
        } catch (Exception e) {
            LOG.warn("Failed to record AI completion", e);
        }
    }
}