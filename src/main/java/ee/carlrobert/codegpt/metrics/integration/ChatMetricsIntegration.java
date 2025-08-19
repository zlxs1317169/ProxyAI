package ee.carlrobert.codegpt.metrics.integration;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;
import ee.carlrobert.codegpt.metrics.SafeMetricsCollector;
import ee.carlrobert.codegpt.conversations.message.Message;
import ee.carlrobert.llm.client.openai.completion.ErrorDetails;

/**
 * 聊天功能的指标集成，用于收集与聊天相关的效能度量指标
 */
public class ChatMetricsIntegration {
    private static final Logger LOG = Logger.getInstance(ChatMetricsIntegration.class);
    private static final String ACTION_TYPE = "CHAT_INTERACTION";
    
    private ChatMetricsIntegration() {
        // 工具类，不允许实例化
    }
    
    /**
     * 记录聊天消息发送开始
     */
    public static ProductivityMetrics recordChatMessageStart(Project project, String conversationId, Message message) {
        ProductivityMetrics metrics = SafeMetricsCollector.safelyStartMetrics(
                project, 
                "chat_message_" + conversationId, 
                ACTION_TYPE
        );
        
        if (metrics != null) {
            metrics.addAdditionalData("conversationId", conversationId);
            // metrics.addAdditionalData("messageType", message.getType());
            // metrics.addAdditionalData("messageLength", message.getContent().length());
            
            if (message.getReferencedFilePaths() != null && !message.getReferencedFilePaths().isEmpty()) {
                metrics.addAdditionalData("referencedFiles", message.getReferencedFilePaths());
            }
        }
        
        return metrics;
    }
    
    /**
     * 记录聊天消息接收完成
     */
    public static void recordChatMessageComplete(
            Project project, 
            ProductivityMetrics metrics, 
            String responseContent, 
            int tokenCount) {
        
        if (metrics != null) {
                                                metrics.setTotalTokenCount(tokenCount);
            metrics.addAdditionalData("responseLength", responseContent.length());
            SafeMetricsCollector.safelyCompleteMetrics(project, metrics, true);
        }
    }
    
    /**
     * 记录聊天消息错误
     */
    public static void recordChatMessageError(
            Project project, 
            ProductivityMetrics metrics, 
            ErrorDetails error) {
        
        if (metrics != null) {
            String errorMessage = error != null ? error.getMessage() : "Unknown error";
            SafeMetricsCollector.safelyCompleteMetrics(project, metrics, false, errorMessage);
        }
    }
    
    /**
     * 记录聊天会话创建
     */
    public static void recordConversationCreated(Project project, String conversationId, String conversationType) {
        ProductivityMetrics metrics = SafeMetricsCollector.safelyStartMetrics(
                project,
                "conversation_created",
                ACTION_TYPE
        );
        
        if (metrics != null) {
            metrics.addAdditionalData("conversationId", conversationId);
            metrics.addAdditionalData("conversationType", conversationType);
            SafeMetricsCollector.safelyCompleteMetrics(project, metrics, true);
        }
    }
    
    /**
     * 记录聊天会话关闭
     */
    public static void recordConversationClosed(Project project, String conversationId, int messageCount) {
        ProductivityMetrics metrics = SafeMetricsCollector.safelyStartMetrics(
                project,
                "conversation_closed",
                ACTION_TYPE
        );
        
        if (metrics != null) {
            metrics.addAdditionalData("conversationId", conversationId);
            metrics.addAdditionalData("messageCount", messageCount);
            SafeMetricsCollector.safelyCompleteMetrics(project, metrics, true);
        }
    }
}