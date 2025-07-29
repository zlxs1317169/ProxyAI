package ee.carlrobert.codegpt.metrics;

import com.intellij.codeInsight.inline.completion.InlineCompletionEvent;
import com.intellij.codeInsight.inline.completion.InlineCompletionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import ee.carlrobert.codegpt.metrics.integration.CodeCompletionMetricsIntegration;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代码补全使用监听器
 * 监听用户的编辑行为，识别可能的代码补全使用情况
 */
public class CodeCompletionUsageListener implements DocumentListener {
    
    private static final Logger LOG = Logger.getInstance(CodeCompletionUsageListener.class);
    
    // 存储最近的文档变更，用于检测代码补全的使用
    private final ConcurrentHashMap<Document, DocumentChangeInfo> recentChanges = new ConcurrentHashMap<>();
    
    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        try {
            Document document = event.getDocument();
            VirtualFile file = FileDocumentManager.getInstance().getFile(document);
            
            if (file == null) return;
            
            // 记录文档变更信息
            DocumentChangeInfo changeInfo = new DocumentChangeInfo();
            changeInfo.timestamp = LocalDateTime.now();
            changeInfo.offset = event.getOffset();
            changeInfo.oldLength = event.getOldLength();
            changeInfo.newLength = event.getNewLength();
            changeInfo.newFragment = event.getNewFragment().toString();
            changeInfo.fileName = file.getName();
            changeInfo.language = getLanguageFromFile(file);
            
            recentChanges.put(document, changeInfo);
            
            // 检测是否可能是代码补全
            if (isPossibleCodeCompletion(changeInfo)) {
                recordPossibleCodeCompletion(changeInfo);
            }
            
            // 清理旧的变更记录
            cleanupOldChanges();
            
        } catch (Exception e) {
            LOG.warn("处理文档变更事件时发生错误", e);
        }
    }
    
    /**
     * 判断是否可能是代码补全
     */
    private boolean isPossibleCodeCompletion(DocumentChangeInfo changeInfo) {
        // 检测特征：
        // 1. 插入了多行代码
        // 2. 插入的内容包含常见的代码结构
        // 3. 插入速度很快（通常代码补全是瞬间插入的）
        
        String newText = changeInfo.newFragment;
        
        // 检查是否插入了多行代码
        if (newText.contains("\n") && newText.trim().length() > 10) {
            return true;
        }
        
        // 检查是否包含常见的代码结构
        if (containsCodeStructures(newText)) {
            return true;
        }
        
        // 检查是否是大量文本的快速插入
        if (newText.length() > 20 && changeInfo.oldLength == 0) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查文本是否包含代码结构
     */
    private boolean containsCodeStructures(String text) {
        // 检查常见的代码模式
        return text.contains("{") && text.contains("}") ||
               text.contains("(") && text.contains(")") ||
               text.contains("function") ||
               text.contains("class") ||
               text.contains("if") ||
               text.contains("for") ||
               text.contains("while") ||
               text.contains("import") ||
               text.contains("return");
    }
    
    /**
     * 记录可能的代码补全使用
     */
    private void recordPossibleCodeCompletion(DocumentChangeInfo changeInfo) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                // 假设这是被接受的代码补全
                // 在实际场景中，我们可能需要更复杂的逻辑来确定是否真的是代码补全
                
                LOG.info("检测到可能的代码补全使用: 文件=" + changeInfo.fileName + 
                        ", 语言=" + changeInfo.language + 
                        ", 文本长度=" + changeInfo.newFragment.length());
                
                // 这里我们可以记录指标，但需要一个Editor实例
                // 由于DocumentListener中没有直接的Editor引用，我们记录基本信息
                recordCodeCompletionUsage(changeInfo);
                
            } catch (Exception e) {
                LOG.warn("记录代码补全使用时发生错误", e);
            }
        });
    }
    
    /**
     * 记录代码补全使用指标
     */
    private void recordCodeCompletionUsage(DocumentChangeInfo changeInfo) {
        try {
            MetricsIntegration metricsIntegration = MetricsIntegration.getInstance();
            if (metricsIntegration != null && metricsIntegration.isInitialized()) {
                metricsIntegration.recordAICompletion(
                    changeInfo.language,
                    changeInfo.newFragment,
                    true, // 假设是被接受的
                    0L // 响应时间未知
                );
            }
        } catch (Exception e) {
            LOG.warn("记录代码补全指标时发生错误", e);
        }
    }
    
    /**
     * 清理旧的变更记录
     */
    private void cleanupOldChanges() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        recentChanges.entrySet().removeIf(entry -> 
            entry.getValue().timestamp.isBefore(cutoff));
    }
    
    /**
     * 从文件获取编程语言
     */
    private String getLanguageFromFile(VirtualFile file) {
        String extension = file.getExtension();
        if (extension == null) return "unknown";
        
        switch (extension.toLowerCase()) {
            case "java": return "java";
            case "kt": return "kotlin";
            case "py": return "python";
            case "js": case "ts": return "javascript";
            case "cpp": case "cc": case "cxx": return "cpp";
            case "c": return "c";
            case "go": return "go";
            case "rs": return "rust";
            case "php": return "php";
            case "rb": return "ruby";
            case "swift": return "swift";
            default: return extension;
        }
    }
    
    /**
     * 文档变更信息
     */
    private static class DocumentChangeInfo {
        LocalDateTime timestamp;
        int offset;
        int oldLength;
        int newLength;
        String newFragment;
        String fileName;
        String language;
    }
}