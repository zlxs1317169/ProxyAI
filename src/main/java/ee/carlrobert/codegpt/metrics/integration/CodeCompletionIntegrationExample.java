package ee.carlrobert.codegpt.metrics.integration;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import ee.carlrobert.codegpt.metrics.MetricsIntegration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代码补全功能提效度量集成示例
 * 展示如何在代码补全功能中集成提效度量
 * 
 * 注意：这是一个示例实现，展示了如何集成度量收集
 * 实际的代码补全功能需要根据具体实现进行调整
 */
public class CodeCompletionIntegrationExample {
    
    private static final ConcurrentHashMap<String, CompletionSession> activeCompletionSessions = new ConcurrentHashMap<>();
    
    /**
     * 开始代码补全会话
     * 当用户触发代码补全时调用
     */
    public static void startCompletionSession(String sessionId, Editor editor, String context) {
        try {
            CompletionSession session = new CompletionSession();
            session.sessionId = sessionId;
            session.startTime = LocalDateTime.now();
            session.editor = editor;
            session.context = context;
            session.language = getLanguageFromEditor(editor);
            session.cursorPosition = editor.getCaretModel().getOffset();
            
            activeCompletionSessions.put(sessionId, session);
            
            System.out.println("🚀 代码补全会话开始: " + sessionId + " (" + session.language + ")");
            
        } catch (Exception e) {
            System.err.println("开始代码补全会话时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 记录AI补全建议
     * 当AI返回补全建议时调用
     */
    public static void recordCompletionSuggestion(String sessionId, String suggestion, 
                                                 int suggestionRank, double confidence) {
        CompletionSession session = activeCompletionSessions.get(sessionId);
        if (session != null) {
            CompletionSuggestion suggestionObj = new CompletionSuggestion();
            suggestionObj.text = suggestion;
            suggestionObj.rank = suggestionRank;
            suggestionObj.confidence = confidence;
            suggestionObj.timestamp = LocalDateTime.now();
            suggestionObj.lines = countLines(suggestion);
            
            session.suggestions.add(suggestionObj);
            session.totalSuggestedLines += suggestionObj.lines;
            
            System.out.println("💡 AI补全建议: " + suggestionObj.lines + " 行代码 (置信度: " + 
                String.format("%.2f", confidence) + ")");
        }
    }
    
    /**
     * 记录用户接受补全
     * 当用户接受AI建议时调用
     */
    public static void recordCompletionAccepted(String sessionId, String acceptedText, 
                                              int suggestionIndex, String acceptanceType) {
        CompletionSession session = activeCompletionSessions.get(sessionId);
        if (session != null) {
            session.acceptedSuggestions++;
            session.acceptedLines += countLines(acceptedText);
            session.lastAcceptanceTime = LocalDateTime.now();
            session.acceptanceType = acceptanceType; // "full", "partial", "modified"
            
            // 立即记录补全度量
            recordCompletionMetrics(session, acceptedText, true);
            
            System.out.println("✅ 用户接受补全: " + countLines(acceptedText) + " 行代码 (" + acceptanceType + ")");
        }
    }
    
    /**
     * 记录用户拒绝补全
     * 当用户拒绝或忽略AI建议时调用
     */
    public static void recordCompletionRejected(String sessionId, String rejectedText, String rejectionReason) {
        CompletionSession session = activeCompletionSessions.get(sessionId);
        if (session != null) {
            session.rejectedSuggestions++;
            session.rejectionReasons.put(rejectionReason, 
                session.rejectionReasons.getOrDefault(rejectionReason, 0) + 1);
            
            // 记录拒绝的补全度量
            recordCompletionMetrics(session, rejectedText, false);
            
            System.out.println("❌ 用户拒绝补全: " + rejectionReason);
        }
    }
    
    /**
     * 结束代码补全会话
     * 当补全会话完成时调用
     */
    public static void endCompletionSession(String sessionId) {
        CompletionSession session = activeCompletionSessions.remove(sessionId);
        if (session != null) {
            session.endTime = LocalDateTime.now();
            recordFinalCompletionMetrics(session);
            
            System.out.println("🏁 代码补全会话结束: " + sessionId + 
                " (接受率: " + String.format("%.1f", session.getAcceptanceRate() * 100) + "%)");
        }
    }
    
    /**
     * 记录补全性能指标
     * 记录响应时间、准确性等性能指标
     */
    public static void recordCompletionPerformance(String sessionId, long responseTimeMs, 
                                                  double accuracy, String modelUsed) {
        CompletionSession session = activeCompletionSessions.get(sessionId);
        if (session != null) {
            session.responseTimeMs = responseTimeMs;
            session.accuracy = accuracy;
            session.modelUsed = modelUsed;
            
            // 分析性能
            analyzeCompletionPerformance(session);
        }
    }
    
    /**
     * 记录用户编辑行为
     * 记录用户在接受补全后的编辑行为
     */
    public static void recordPostCompletionEdit(String sessionId, String editType, 
                                               String originalText, String editedText) {
        CompletionSession session = activeCompletionSessions.get(sessionId);
        if (session != null) {
            PostCompletionEdit edit = new PostCompletionEdit();
            edit.editType = editType; // "modify", "delete", "extend"
            edit.originalText = originalText;
            edit.editedText = editedText;
            edit.timestamp = LocalDateTime.now();
            
            session.postCompletionEdits.add(edit);
            
            // 分析编辑模式
            analyzeEditPattern(session, edit);
        }
    }
    
    private static void recordCompletionMetrics(CompletionSession session, String completionText, boolean accepted) {
        try {
            long responseTime = session.responseTimeMs > 0 ? session.responseTimeMs : 
                ChronoUnit.MILLIS.between(session.startTime, LocalDateTime.now());
            
            MetricsIntegration metricsIntegration = MetricsIntegration.getInstance();
            if (metricsIntegration.isInitialized()) {
                metricsIntegration.recordAICompletion(
                    session.language,
                    completionText,
                    accepted,
                    responseTime
                );
            }
        } catch (Exception e) {
            System.err.println("记录代码补全度量时发生错误: " + e.getMessage());
        }
    }
    
    private static void recordFinalCompletionMetrics(CompletionSession session) {
        try {
            long totalDuration = ChronoUnit.MILLIS.between(session.startTime, session.endTime);
            
            MetricsIntegration metricsIntegration = MetricsIntegration.getInstance();
            if (metricsIntegration.isInitialized()) {
                // 记录时间节省
                long traditionalTime = estimateTraditionalCodingTime(session.acceptedLines, session.language);
                if (traditionalTime > totalDuration) {
                    // 记录时间节省 - 使用现有的方法
                    long timeSaved = traditionalTime - totalDuration;
                    System.out.println("✅ 代码补全节省时间: " + timeSaved + " ms");
                }
                
                // 记录学习活动（如果用户查看了多个建议）
                if (session.suggestions.size() > 1) {
                    metricsIntegration.recordLearningActivity(
                        "code_completion_exploration",
                        session.suggestions.size(),
                        totalDuration
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("记录最终补全度量时发生错误: " + e.getMessage());
        }
    }
    
    private static void analyzeCompletionPerformance(CompletionSession session) {
        try {
            // 性能分析
            if (session.responseTimeMs < 500) {
                System.out.println("⚡ 补全响应速度优秀: " + session.responseTimeMs + "ms");
            } else if (session.responseTimeMs < 2000) {
                System.out.println("⏱️ 补全响应速度良好: " + session.responseTimeMs + "ms");
            } else {
                System.out.println("🐌 补全响应较慢: " + session.responseTimeMs + "ms");
            }
            
            // 准确性分析
            if (session.accuracy > 0.8) {
                System.out.println("🎯 补全准确性很高: " + String.format("%.1f", session.accuracy * 100) + "%");
            } else if (session.accuracy > 0.6) {
                System.out.println("📊 补全准确性良好: " + String.format("%.1f", session.accuracy * 100) + "%");
            } else {
                System.out.println("📉 补全准确性需要改进: " + String.format("%.1f", session.accuracy * 100) + "%");
            }
            
        } catch (Exception e) {
            System.err.println("分析补全性能时发生错误: " + e.getMessage());
        }
    }
    
    private static void analyzeEditPattern(CompletionSession session, PostCompletionEdit edit) {
        try {
            // 分析用户编辑模式
            switch (edit.editType) {
                case "modify":
                    System.out.println("✏️ 用户修改了AI补全内容");
                    break;
                case "delete":
                    System.out.println("🗑️ 用户删除了部分AI补全内容");
                    break;
                case "extend":
                    System.out.println("➕ 用户扩展了AI补全内容");
                    break;
            }
            
            // 计算编辑距离
            int editDistance = calculateEditDistance(edit.originalText, edit.editedText);
            if (editDistance > 0) {
                System.out.println("📝 编辑距离: " + editDistance + " 个字符");
            }
            
        } catch (Exception e) {
            System.err.println("分析编辑模式时发生错误: " + e.getMessage());
        }
    }
    
    // ==================== 辅助方法 ====================
    
    private static String getLanguageFromEditor(Editor editor) {
        try {
            VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
            if (file != null) {
                String extension = file.getExtension();
                if (extension != null) {
                    switch (extension.toLowerCase()) {
                        case "java": return "java";
                        case "kt": return "kotlin";
                        case "py": return "python";
                        case "js": case "ts": return "javascript";
                        case "cpp": case "cc": case "cxx": return "cpp";
                        case "c": return "c";
                        case "go": return "go";
                        case "rs": return "rust";
                        default: return extension;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取编程语言时发生错误: " + e.getMessage());
        }
        return "unknown";
    }
    
    private static int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\\r?\\n").length;
    }
    
    private static long estimateTraditionalCodingTime(int linesOfCode, String language) {
        // 基于编程语言和代码行数估算传统编程时间
        double linesPerMinute;
        switch (language.toLowerCase()) {
            case "java": case "kotlin": linesPerMinute = 3.0; break;
            case "python": linesPerMinute = 4.0; break;
            case "javascript": linesPerMinute = 3.5; break;
            case "cpp": case "c": linesPerMinute = 2.5; break;
            default: linesPerMinute = 3.0;
        }
        
        return (long) (linesOfCode / linesPerMinute * 60 * 1000); // 转换为毫秒
    }
    
    private static int calculateEditDistance(String original, String edited) {
        // 简单的编辑距离计算（Levenshtein距离的简化版本）
        if (original == null || edited == null) {
            return 0;
        }
        
        int lengthDiff = Math.abs(original.length() - edited.length());
        int commonLength = Math.min(original.length(), edited.length());
        int differences = 0;
        
        for (int i = 0; i < commonLength; i++) {
            if (original.charAt(i) != edited.charAt(i)) {
                differences++;
            }
        }
        
        return differences + lengthDiff;
    }
    
    // ==================== 数据模型 ====================
    
    private static class CompletionSession {
        String sessionId;
        LocalDateTime startTime;
        LocalDateTime endTime;
        LocalDateTime lastAcceptanceTime;
        
        Editor editor;
        String context;
        String language;
        int cursorPosition;
        
        java.util.List<CompletionSuggestion> suggestions = new java.util.ArrayList<>();
        java.util.List<PostCompletionEdit> postCompletionEdits = new java.util.ArrayList<>();
        java.util.Map<String, Integer> rejectionReasons = new java.util.HashMap<>();
        
        int totalSuggestedLines;
        int acceptedSuggestions;
        int rejectedSuggestions;
        int acceptedLines;
        
        long responseTimeMs;
        double accuracy;
        String modelUsed;
        String acceptanceType;
        
        public double getAcceptanceRate() {
            int totalSuggestions = acceptedSuggestions + rejectedSuggestions;
            return totalSuggestions > 0 ? (double) acceptedSuggestions / totalSuggestions : 0.0;
        }
    }
    
    private static class CompletionSuggestion {
        String text;
        int rank;
        double confidence;
        LocalDateTime timestamp;
        int lines;
    }
    
    private static class PostCompletionEdit {
        String editType;
        String originalText;
        String editedText;
        LocalDateTime timestamp;
    }
}