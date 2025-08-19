package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.codecompletions.CodeCompletionService;
import ee.carlrobert.codegpt.metrics.integration.CodeCompletionMetricsIntegration;
import ee.carlrobert.llm.client.openai.completion.ErrorDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代码补全使用情况监听器，用于收集代码补全相关的指标
 */
public class CodeCompletionUsageListener {
    private static final Logger LOG = Logger.getInstance(CodeCompletionUsageListener.class);
    private static final Map<String, ProductivityMetrics> activeCompletionMetrics = new ConcurrentHashMap<>();
    
    private CodeCompletionUsageListener() {
        // 工具类，不允许实例化
    }
    
    /**
     * 记录代码补全请求开始
     */
    public static void recordCompletionRequest(
            Project project,
            Editor editor,
            String prefix,
            int offset) {
        
        if (project == null || editor == null) {
            return;
        }
        
        try {
            Document document = editor.getDocument();
            String filePath = editor.getVirtualFile() != null 
                    ? editor.getVirtualFile().getPath() 
                    : "unknown";
            
            ProductivityMetrics metrics = CodeCompletionMetricsIntegration.recordCompletionStart(
                    project, 
                    filePath, 
                    offset, 
                    prefix
            );
            
            if (metrics != null) {
                String requestId = generateRequestId(filePath, offset);
                activeCompletionMetrics.put(requestId, metrics);
            }
        } catch (Exception e) {
            LOG.warn("记录代码补全请求失败", e);
        }
    }
    
    /**
     * 记录代码补全请求完成
     */
    public static void recordCompletionResponse(
            Project project,
            Editor editor,
            int offset,
            String suggestion,
            int tokenCount) {
        
        if (project == null || editor == null) {
            return;
        }
        
        try {
            String filePath = editor.getVirtualFile() != null 
                    ? editor.getVirtualFile().getPath() 
                    : "unknown";
            
            String requestId = generateRequestId(filePath, offset);
            ProductivityMetrics metrics = activeCompletionMetrics.remove(requestId);
            
            if (metrics != null) {
                CodeCompletionMetricsIntegration.recordCompletionComplete(
                        project, 
                        metrics, 
                        suggestion, 
                        tokenCount, 
                        false
                );
            }
        } catch (Exception e) {
            LOG.warn("记录代码补全响应失败", e);
        }
    }
    
    /**
     * 记录代码补全请求错误
     */
    public static void recordCompletionError(
            Project project,
            Editor editor,
            int offset,
            ErrorDetails error) {
        
        if (project == null || editor == null) {
            return;
        }
        
        try {
            String filePath = editor.getVirtualFile() != null 
                    ? editor.getVirtualFile().getPath() 
                    : "unknown";
            
            String requestId = generateRequestId(filePath, offset);
            ProductivityMetrics metrics = activeCompletionMetrics.remove(requestId);
            
            if (metrics != null) {
                CodeCompletionMetricsIntegration.recordCompletionError(
                        project, 
                        metrics, 
                        error
                );
            }
        } catch (Exception e) {
            LOG.warn("记录代码补全错误失败", e);
        }
    }
    
    /**
     * 记录代码补全接受事件
     */
    public static void recordCompletionAccepted(
            Project project,
            Editor editor,
            String acceptedText,
            String completionType) {
        
        if (project == null || editor == null) {
            return;
        }
        
        try {
            String filePath = editor.getVirtualFile() != null 
                    ? editor.getVirtualFile().getPath() 
                    : "unknown";
            
            CodeCompletionMetricsIntegration.recordCompletionAccepted(
                    project, 
                    filePath, 
                    acceptedText, 
                    completionType
            );
        } catch (Exception e) {
            LOG.warn("记录代码补全接受事件失败", e);
        }
    }
    
    private static String generateRequestId(String filePath, int offset) {
        return filePath + "_" + offset;
    }
}