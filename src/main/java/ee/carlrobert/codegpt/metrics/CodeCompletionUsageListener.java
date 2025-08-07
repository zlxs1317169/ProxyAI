package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import ee.carlrobert.codegpt.settings.metrics.MetricsSettings;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 代码补全使用监听器
 * 监听用户的编辑行为，识别可能的代码补全使用情况
 */
public class CodeCompletionUsageListener implements DocumentListener {

    private static final Logger LOG = Logger.getInstance(CodeCompletionUsageListener.class);
    
    // 存储最近的文档变更，用于检测代码补全的使用
    private final ConcurrentHashMap<Document, DocumentChangeInfo> recentChanges = new ConcurrentHashMap<>();
    
    // 检测阈值
    private static final int MIN_INSERTION_LENGTH = 10;
    private static final int MIN_LINES_FOR_COMPLETION = 2;

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        try {
            // 检查是否启用了自动检测
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings == null || !settings.isMetricsEnabled() || !settings.isAutoDetectionEnabled()) {
                return;
            }
            
            // 如果启用了"仅跟踪真实AI使用"模式，则不进行自动检测
            if (settings.isOnlyTrackAIUsage()) {
                return;
            }

            Document document = event.getDocument();
            
            // 只处理插入事件
            if (event.getNewLength() <= event.getOldLength()) {
                return;
            }

            String insertedText = event.getNewFragment().toString();
            
            // 创建变更信息
            DocumentChangeInfo changeInfo = new DocumentChangeInfo(
                document,
                insertedText,
                System.currentTimeMillis(),
                getFileName(document),
                getLanguage(document)
            );

            // 存储变更信息
            recentChanges.put(document, changeInfo);

            // 检测是否可能是代码补全
            if (isPossibleCodeCompletion(changeInfo)) {
                recordPossibleCodeCompletion(changeInfo);
            }

        } catch (Exception e) {
            LOG.warn("处理文档变更事件时发生错误", e);
        }
    }

    private boolean isPossibleCodeCompletion(DocumentChangeInfo changeInfo) {
        try {
            String text = changeInfo.newFragment;
            
            if (text.length() < MIN_INSERTION_LENGTH) {
                return false;
            }

            if (!containsCodePatterns(text)) {
                return false;
            }

            if (text.split("\n").length < MIN_LINES_FOR_COMPLETION) {
                return false;
            }

            return true;
            
        } catch (Exception e) {
            LOG.warn("判断代码补全时发生错误", e);
            return false;
        }
    }

    private boolean containsCodePatterns(String text) {
        return text.contains("{") || text.contains("}") || 
               text.contains("(") || text.contains(")") ||
               text.contains(";") || text.contains("=") ||
               text.matches(".*\\b(if|for|while|class|function|def|public|private)\\b.*");
    }

    private void recordPossibleCodeCompletion(DocumentChangeInfo changeInfo) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                LOG.info("检测到可能的代码补全使用: 文件=" + changeInfo.fileName + 
                        ", 语言=" + changeInfo.language + 
                        ", 文本长度=" + changeInfo.newFragment.length());
                
                recordCodeCompletionMetrics(changeInfo);
                
            } catch (Exception e) {
                LOG.warn("记录代码补全使用时发生错误", e);
            }
        });
    }

    private void recordCodeCompletionMetrics(DocumentChangeInfo changeInfo) {
        try {
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                int linesGenerated = changeInfo.newFragment.split("\n").length;
                int acceptedLines = linesGenerated;
                long processingTime = 50L;
                
                metrics.recordCodeCompletion(changeInfo.language, linesGenerated, acceptedLines, processingTime);
            }
        } catch (Exception e) {
            LOG.warn("记录代码补全指标时发生错误", e);
        }
    }

    private String getFileName(Document document) {
        try {
            VirtualFile file = FileDocumentManager.getInstance().getFile(document);
            return file != null ? file.getName() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getLanguage(Document document) {
        try {
            VirtualFile file = FileDocumentManager.getInstance().getFile(document);
            if (file != null) {
                String extension = file.getExtension();
                if (extension != null) {
                    switch (extension.toLowerCase()) {
                        case "java": return "java";
                        case "py": return "python";
                        case "js": return "javascript";
                        case "ts": return "typescript";
                        case "kt": return "kotlin";
                        default: return extension;
                    }
                }
            }
            return "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static class DocumentChangeInfo {
        public final Document document;
        public final String newFragment;
        public final long timestamp;
        public final String fileName;
        public final String language;

        public DocumentChangeInfo(Document document, String newFragment, long timestamp, String fileName, String language) {
            this.document = document;
            this.newFragment = newFragment;
            this.timestamp = timestamp;
            this.fileName = fileName;
            this.language = language;
        }
    }
}