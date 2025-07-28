package ee.carlrobert.codegpt.metrics.integration;

import ee.carlrobert.codegpt.metrics.MetricsIntegration;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * 代码补全提效度量集成
 * 提供简单易用的API来集成提效度量到现有的代码补全功能中
 */
public class CodeCompletionMetricsIntegration {

    // 定义常量以提高可读性和可维护性
    private static final long SECONDS_PER_LINE_MANUAL_EDIT = 30L; // 假设每行手动编辑需要30秒
    private static final long MILLIS_PER_SECOND = 1000L; // 毫秒转换因子
    private static final int EDIT_DISTANCE_WEIGHT_FACTOR = 50; // 编辑距离的权重因子

    /**
     * 记录代码补全度量数据
     * 在代码补全提供者中调用此方法来收集度量数据
     *
     * @param editor 编辑器实例
     * @param completionText 补全的代码文本
     * @param wasAccepted 是否被用户接受
     * @param responseTimeMs 响应时间（毫秒）
     */
    public static void recordCodeCompletionMetrics(Editor editor, String completionText,
                                                 boolean wasAccepted, long responseTimeMs) {
        try {
            // 获取文件语言
            String language = getLanguageFromEditor(editor);

            // 记录度量数据
            MetricsIntegration metricsIntegration = MetricsIntegration.getInstance();
            if (metricsIntegration != null && metricsIntegration.isInitialized()) {
                metricsIntegration.recordAICompletion(language, completionText, wasAccepted, responseTimeMs);
            }

        } catch (Exception e) {
            // 度量收集不应影响正常功能，只记录错误日志
            System.err.println("记录代码补全度量时发生错误: " + e.getMessage());
        }
    }

    /**
     * 记录内联补全度量
     *
     * @param language 编程语言
     * @param suggestion 建议的代码
     * @param accepted 是否被接受
     * @param processingTime 处理时间
     */
    public static void recordInlineCompletionMetrics(String language, String suggestion,
                                                   boolean accepted, long processingTime) {
        try {
            MetricsIntegration metricsIntegration = MetricsIntegration.getInstance();
            if (metricsIntegration != null && metricsIntegration.isInitialized()) {
                metricsIntegration.recordAICompletion(language, suggestion, accepted, processingTime);
            }
        } catch (Exception e) {
            System.err.println("记录内联补全度量时发生错误: " + e.getMessage());
        }
    }

    /**
     * 记录多行编辑建议度量
     *
     * @param language 编程语言
     * @param originalCode 原始代码
     * @param suggestedCode 建议的代码
     * @param applied 是否被应用
     * @param processingTime 处理时间
     */
    public static void recordMultiLineEditMetrics(String language, String originalCode,
                                                String suggestedCode, boolean applied,
                                                long processingTime) {
        try {
            MetricsIntegration metricsIntegration = MetricsIntegration.getInstance();
            if (metricsIntegration != null && metricsIntegration.isInitialized()) {
                // 计算建议的代码行数
                int suggestedLines = (int) suggestedCode.lines().count(); // 使用 lines().count() 更健壮

                // 记录代码补全度量
                metricsIntegration.recordAICompletion(language, suggestedCode, applied, processingTime);

                // 如果应用了建议，记录时间节省
                if (applied && metricsIntegration.getMetricsCollector() != null) {
                    long estimatedManualTime = estimateManualEditTime(originalCode, suggestedCode);
                    metricsIntegration.getMetricsCollector().recordTimeSaving(
                        "multi_line_edit", estimatedManualTime, processingTime, suggestedLines
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("记录多行编辑度量时发生错误: " + e.getMessage());
        }
    }

    private static String getLanguageFromEditor(Editor editor) {
        try {
            VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
            if (file != null) {
                String extension = file.getExtension();
                return mapExtensionToLanguage(extension);
            }
        } catch (Exception e) {
            // 忽略错误，返回默认值
        }
        return "unknown";
    }

    private static String mapExtensionToLanguage(String extension) {
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

    private static long estimateManualEditTime(String originalCode, String suggestedCode) {
        // 简单的时间估算逻辑
        // 步骤 2: 优化行数计算
        long originalLines = originalCode.lines().count();
        long suggestedLines = suggestedCode.lines().count();

        // 步骤 3: 改进编辑距离权重
        long changedLines = Math.abs(suggestedLines - originalLines) +
                          calculateEditDistance(originalCode, suggestedCode) / EDIT_DISTANCE_WEIGHT_FACTOR;

        // 步骤 1: 引入常量
        return changedLines * SECONDS_PER_LINE_MANUAL_EDIT * MILLIS_PER_SECOND; // 转换为毫秒
    }

    private static int calculateEditDistance(String s1, String s2) {
        // 简化的编辑距离计算
        return Math.abs(s1.length() - s2.length());
    }
}