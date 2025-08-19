package ee.carlrobert.codegpt.metrics.integration;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;
import ee.carlrobert.codegpt.metrics.SafeMetricsCollector;
import ee.carlrobert.llm.client.openai.completion.ErrorDetails;

/**
 * 代码补全功能的指标集成，用于收集与代码补全相关的效能度量指标
 */
public class CodeCompletionMetricsIntegration {
    private static final Logger LOG = Logger.getInstance(CodeCompletionMetricsIntegration.class);
    private static final String ACTION_TYPE = "CODE_COMPLETION";
    
    private CodeCompletionMetricsIntegration() {
        // 工具类，不允许实例化
    }
    
    /**
     * 记录代码补全请求开始
     */
    public static ProductivityMetrics recordCompletionStart(
            Project project, 
            String filePath, 
            int offset, 
            String prefix) {
        
        ProductivityMetrics metrics = SafeMetricsCollector.safelyStartMetrics(
                project,
                "code_completion_" + filePath.hashCode() + "_" + offset,
                ACTION_TYPE
        );
        
        if (metrics != null) {
            metrics.addAdditionalData("filePath", filePath);
            metrics.addAdditionalData("offset", offset);
            metrics.addAdditionalData("prefix", prefix);
        }
        
        return metrics;
    }
    
    /**
     * 记录代码补全请求完成
     */
    public static void recordCompletionComplete(
            Project project,
            ProductivityMetrics metrics,
            String suggestion,
            int tokenCount,
            boolean accepted) {
        
        if (metrics != null) {
                            metrics.setTotalTokenCount(tokenCount);
            metrics.addAdditionalData("suggestionLength", suggestion.length());
            metrics.addAdditionalData("accepted", accepted);
            SafeMetricsCollector.safelyCompleteMetrics(project, metrics, true);
        }
    }
    
    /**
     * 记录代码补全请求错误
     */
    public static void recordCompletionError(
            Project project,
            ProductivityMetrics metrics,
            ErrorDetails error) {
        
        if (metrics != null) {
            String errorMessage = error != null ? error.getMessage() : "Unknown error";
            SafeMetricsCollector.safelyCompleteMetrics(project, metrics, false, errorMessage);
        }
    }
    
    /**
     * 记录代码补全接受事件
     */
    public static void recordCompletionAccepted(
            Project project,
            String filePath,
            String acceptedText,
            String completionType) {
        
        ProductivityMetrics metrics = SafeMetricsCollector.safelyStartMetrics(
                project,
                "completion_accepted",
                ACTION_TYPE
        );
        
        if (metrics != null) {
            metrics.addAdditionalData("filePath", filePath);
            metrics.addAdditionalData("acceptedTextLength", acceptedText.length());
            metrics.addAdditionalData("completionType", completionType);
            SafeMetricsCollector.safelyCompleteMetrics(project, metrics, true);
        }
    }
    
    /**
     * 记录代码补全拒绝事件
     */
    public static void recordCompletionRejected(
            Project project,
            String filePath,
            String rejectedText) {
        
        ProductivityMetrics metrics = SafeMetricsCollector.safelyStartMetrics(
                project,
                "completion_rejected",
                ACTION_TYPE
        );
        
        if (metrics != null) {
            metrics.addAdditionalData("filePath", filePath);
            metrics.addAdditionalData("rejectedTextLength", rejectedText.length());
            SafeMetricsCollector.safelyCompleteMetrics(project, metrics, true);
        }
    }
    
    /**
     * 记录代码补全指标（用于 Kotlin 集成）
     */
    public static void recordCodeCompletionMetrics(
            com.intellij.openapi.editor.Editor editor,
            String text,
            boolean accepted,
            long processingTime) {
        
        try {
            Project project = editor.getProject();
            if (project == null) {
                return;
            }
            
            String filePath = editor.getVirtualFile() != null ? 
                editor.getVirtualFile().getPath() : "unknown";
            
            ProductivityMetrics metrics = SafeMetricsCollector.safelyStartMetrics(
                    project,
                    "code_completion_" + System.currentTimeMillis(),
                    ACTION_TYPE
            );
            
            if (metrics != null) {
                metrics.setProcessingTime(processingTime);
                metrics.setLinesGenerated(text.split("\n").length);
                metrics.setLinesAccepted(accepted ? text.split("\n").length : 0);
                metrics.setAcceptanceRate(accepted ? 100.0 : 0.0);
                metrics.setProgrammingLanguage(detectLanguageFromEditor(editor));
                metrics.setFileExtension(getFileExtension(editor));
                metrics.setContextSize(String.valueOf(editor.getDocument().getTextLength()));
                
                SafeMetricsCollector.safelyCompleteMetrics(project, metrics, true);
            }
            
        } catch (Exception e) {
            LOG.warn("Failed to record code completion metrics", e);
        }
    }
    
    /**
     * 从编辑器检测编程语言
     */
    private static String detectLanguageFromEditor(com.intellij.openapi.editor.Editor editor) {
        try {
            com.intellij.openapi.vfs.VirtualFile file = editor.getVirtualFile();
            if (file != null) {
                String extension = file.getExtension();
                return mapExtensionToLanguage(extension);
            }
        } catch (Exception e) {
            LOG.warn("Failed to detect language from editor", e);
        }
        return "unknown";
    }
    
    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(com.intellij.openapi.editor.Editor editor) {
        try {
            com.intellij.openapi.vfs.VirtualFile file = editor.getVirtualFile();
            if (file != null) {
                return file.getExtension();
            }
        } catch (Exception e) {
            LOG.warn("Failed to get file extension", e);
        }
        return "unknown";
    }
    
    /**
     * 将文件扩展名映射到编程语言
     */
    private static String mapExtensionToLanguage(String extension) {
        if (extension == null) {
            return "unknown";
        }
        
        switch (extension.toLowerCase()) {
            case "java": return "java";
            case "kt": return "kotlin";
            case "py": return "python";
            case "js":
            case "ts": return "javascript";
            case "cpp":
            case "cc":
            case "cxx": return "cpp";
            case "c": return "c";
            case "go": return "go";
            case "rs": return "rust";
            case "php": return "php";
            case "rb": return "ruby";
            case "swift": return "swift";
            default: return extension;
        }
    }
}