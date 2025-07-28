package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 提效指标数据收集器
 * 自动收集开发过程中的各种指标数据
 */
public class MetricsCollector implements StartupActivity {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, SessionData> activeSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TypingSession> typingSessions = new ConcurrentHashMap<>();
    
    @Override
    public void runActivity(@NotNull Project project) {
        initializeMetricsCollection(project);
        startPeriodicReporting();
    }
    
    /**
     * 初始化指标收集
     */
    private void initializeMetricsCollection(Project project) {
        try {
            // 监听文件编辑活动
            project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, 
                new FileEditorManagerListener() {
                    @Override
                    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        startEditingSession(file.getName(), getFileLanguage(file));
                    }
                    
                    @Override
                    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        endEditingSession(file.getName());
                    }
                    
                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        if (event.getNewFile() != null) {
                            switchEditingSession(event.getOldFile(), event.getNewFile());
                        }
                    }
                });
        } catch (Exception e) {
            System.err.println("初始化度量收集时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 开始编辑会话
     */
    private void startEditingSession(String fileName, String language) {
        SessionData session = new SessionData();
        session.fileName = fileName;
        session.language = language;
        session.startTime = LocalDateTime.now();
        session.lastActivityTime = LocalDateTime.now();
        
        activeSessions.put(fileName, session);
    }
    
    /**
     * 结束编辑会话
     */
    private void endEditingSession(String fileName) {
        SessionData session = activeSessions.remove(fileName);
        if (session != null) {
            long sessionDuration = ChronoUnit.MILLIS.between(session.startTime, LocalDateTime.now());
            
            // 记录编程会话数据
            ProductivityMetrics.getInstance().recordTimeSaving(
                "coding_session",
                estimateTraditionalTime(session.linesAdded, session.language),
                sessionDuration,
                session.linesAdded
            );
        }
    }
    
    /**
     * 切换编辑会话
     */
    private void switchEditingSession(VirtualFile oldFile, VirtualFile newFile) {
        if (oldFile != null) {
            pauseEditingSession(oldFile.getName());
        }
        if (newFile != null) {
            resumeEditingSession(newFile.getName(), getFileLanguage(newFile));
        }
    }
    
    /**
     * 暂停编辑会话
     */
    private void pauseEditingSession(String fileName) {
        SessionData session = activeSessions.get(fileName);
        if (session != null) {
            session.pausedTime = LocalDateTime.now();
        }
    }
    
    /**
     * 恢复编辑会话
     */
    private void resumeEditingSession(String fileName, String language) {
        SessionData session = activeSessions.get(fileName);
        if (session != null && session.pausedTime != null) {
            long pauseDuration = ChronoUnit.MILLIS.between(session.pausedTime, LocalDateTime.now());
            session.totalPauseTime += pauseDuration;
            session.pausedTime = null;
        } else {
            startEditingSession(fileName, language);
        }
    }
    
    /**
     * 记录打字活动
     * 简化版本，避免复杂的文档监听器依赖
     */
    public void recordTypingActivity(String fileName, int charactersChanged, boolean isNewLine) {
        try {
            if (fileName == null) return;
            
            TypingSession typing = typingSessions.computeIfAbsent(fileName, k -> new TypingSession());
            typing.lastTypingTime = LocalDateTime.now();
            typing.charactersTyped += charactersChanged;
            
            // 检测是否是新增代码行
            if (isNewLine) {
                typing.linesAdded++;
                
                // 更新会话数据
                SessionData session = activeSessions.get(fileName);
                if (session != null) {
                    session.linesAdded++;
                    session.lastActivityTime = LocalDateTime.now();
                }
            }
        } catch (Exception e) {
            System.err.println("记录打字活动时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * AI代码补全使用记录
     */
    public void recordAICompletionUsage(String language, String completionText, boolean accepted, long responseTime) {
        int suggestedLines = completionText.split("\n").length;
        int acceptedLines = accepted ? suggestedLines : 0;
        
        ProductivityMetrics.getInstance().recordCodeCompletion(
            language, suggestedLines, acceptedLines, responseTime
        );
        
        // 如果接受了补全，更新打字效率
        if (accepted) {
            updateTypingEfficiency(language, suggestedLines, responseTime);
        }
    }
    
    /**
     * AI聊天代码生成记录
     */
    public void recordAIChatCodeGeneration(String generatedCode, String appliedCode, 
                                         long sessionDuration, String taskType) {
        int generatedLines = generatedCode.split("\n").length;
        int appliedLines = appliedCode.split("\n").length;
        
        ProductivityMetrics.getInstance().recordChatCodeGeneration(
            generatedLines, appliedLines, sessionDuration, taskType
        );
    }
    
    /**
     * 调试时间记录
     */
    public void recordDebuggingSession(long startTime, long endTime, boolean usedAI, String issueType) {
        long debugDuration = endTime - startTime;
        
        if (usedAI) {
            // 估算不使用AI的调试时间（基于历史数据）
            long estimatedTraditionalTime = estimateTraditionalDebuggingTime(debugDuration, issueType);
            
            ProductivityMetrics.getInstance().recordDebuggingTimeSaving(
                estimatedTraditionalTime, debugDuration, issueType
            );
        }
    }
    
    /**
     * 代码质量改进记录
     */
    public void recordCodeQualityImprovement(String metricType, double before, double after, String improvementType) {
        ProductivityMetrics.getInstance().recordCodeQualityImprovement(
            metricType, before, after, improvementType
        );
    }
    
    /**
     * 时间节省记录
     */
    public void recordTimeSaving(String taskType, long traditionalTimeMs, long aiAssistedTimeMs, int linesOfCode) {
        ProductivityMetrics.getInstance().recordTimeSaving(
            taskType, traditionalTimeMs, aiAssistedTimeMs, linesOfCode
        );
    }
    
    /**
     * 学习活动记录
     */
    public void recordLearningActivity(String topic, int questionsAsked, long learningTime) {
        // 基于问题数量估算学到的概念数
        int conceptsLearned = Math.max(1, questionsAsked / 2);
        
        ProductivityMetrics.getInstance().recordLearningActivity(
            topic, questionsAsked, conceptsLearned, learningTime
        );
    }
    
    /**
     * 启动定期报告
     */
    private void startPeriodicReporting() {
        // 每小时生成一次统计报告
        scheduler.scheduleAtFixedRate(this::generateHourlyReport, 1, 1, TimeUnit.HOURS);
        
        // 每天生成一次详细报告
        scheduler.scheduleAtFixedRate(this::generateDailyReport, 24, 24, TimeUnit.HOURS);
    }
    
    /**
     * 生成小时报告
     */
    private void generateHourlyReport() {
        try {
            // 清理过期的会话数据
            cleanupExpiredSessions();
            
            // 计算当前活跃度
            int activeSessions = this.activeSessions.size();
            int activeTypingSessions = (int) typingSessions.values().stream()
                .filter(t -> ChronoUnit.MINUTES.between(t.lastTypingTime, LocalDateTime.now()) < 10)
                .count();
            
            // 可以发送通知或记录到日志
            System.out.println(String.format("小时报告 - 活跃编辑会话: %d, 活跃打字会话: %d", 
                activeSessions, activeTypingSessions));
        } catch (Exception e) {
            System.err.println("生成小时报告时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 生成每日报告
     */
    private void generateDailyReport() {
        try {
            ProductivityMetrics.ProductivityReport report = 
                ProductivityMetrics.getInstance().getProductivityReport(1);
            
            System.out.println("=== 每日AI编程助手提效报告 ===");
            System.out.println(String.format("代码接受率: %.2f%%", report.avgCodeAcceptanceRate * 100));
            System.out.println(String.format("节省时间: %.2f 小时", report.totalTimeSavedHours));
            System.out.println(String.format("效率提升: %.2f%%", report.avgEfficiencyGain));
            System.out.println(String.format("生成代码行数: %d", report.totalLinesGenerated));
        } catch (Exception e) {
            System.err.println("生成每日报告时发生错误: " + e.getMessage());
        }
    }
    
    // ==================== 辅助方法 ====================
    
    private String getFileLanguage(VirtualFile file) {
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
            default: return extension;
        }
    }
    
    private String getFileNameFromEvent(DocumentEvent event) {
        try {
            VirtualFile file = FileDocumentManager.getInstance().getFile(event.getDocument());
            if (file != null) {
                return file.getName();
            }
        } catch (Exception e) {
            // 忽略错误，返回默认值
        }
        return "unknown_file";
    }
    
    private long estimateTraditionalTime(int linesOfCode, String language) {
        // 基于编程语言和代码行数估算传统编程时间
        // 这里使用一些经验值，实际项目中可以基于历史数据训练模型
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
    
    private long estimateTraditionalDebuggingTime(long actualTime, String issueType) {
        // 基于问题类型估算传统调试时间
        double multiplier;
        switch (issueType.toLowerCase()) {
            case "syntax_error": multiplier = 1.5; break;
            case "logic_error": multiplier = 2.0; break;
            case "performance_issue": multiplier = 3.0; break;
            case "integration_issue": multiplier = 2.5; break;
            default: multiplier = 2.0;
        }
        
        return (long) (actualTime * multiplier);
    }
    
    private void updateTypingEfficiency(String language, int linesGenerated, long responseTime) {
        // 更新打字效率统计
        // 可以用于计算AI补全对打字速度的提升
    }
    
    private void cleanupExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minus(2, ChronoUnit.HOURS);
        
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().lastActivityTime.isBefore(cutoff));
        
        typingSessions.entrySet().removeIf(entry -> 
            entry.getValue().lastTypingTime.isBefore(cutoff));
    }
    
    // ==================== 数据模型 ====================
    
    private static class SessionData {
        String fileName;
        String language;
        LocalDateTime startTime;
        LocalDateTime lastActivityTime;
        LocalDateTime pausedTime;
        long totalPauseTime;
        int linesAdded;
        int linesDeleted;
        int charactersTyped;
    }
    
    private static class TypingSession {
        LocalDateTime lastTypingTime = LocalDateTime.now();
        int charactersTyped;
        int linesAdded;
        double typingSpeed; // 字符/分钟
    }
}