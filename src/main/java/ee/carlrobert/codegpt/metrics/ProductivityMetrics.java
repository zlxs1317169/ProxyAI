package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI编程助手提效度量核心服务
 * 统计和分析AI助手对开发效率的提升效果
 */
@Service
@State(name = "ProductivityMetrics", storages = @Storage("proxyai-productivity-metrics.xml"))
public final class ProductivityMetrics implements PersistentStateComponent<ProductivityMetrics.State> {
    
    private State state = new State();
    
    public static ProductivityMetrics getInstance() {
        return ApplicationManager.getApplication().getService(ProductivityMetrics.class);
    }
    
    // ==================== 代码生成效率指标 ====================
    
    /**
     * 记录代码补全使用情况
     */
    public void recordCodeCompletion(String language, int suggestedLines, int acceptedLines, long responseTimeMs) {
        CodeCompletionMetric metric = new CodeCompletionMetric();
        metric.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        metric.language = language;
        metric.suggestedLines = suggestedLines;
        metric.acceptedLines = acceptedLines;
        metric.responseTimeMs = responseTimeMs;
        metric.acceptanceRate = acceptedLines > 0 ? (double) acceptedLines / suggestedLines : 0.0;
        
        state.codeCompletions.add(metric);
        updateDailyStats();
    }
    
    /**
     * 记录聊天代码生成
     */
    public void recordChatCodeGeneration(int generatedLines, int appliedLines, long sessionDurationMs, String taskType) {
        ChatCodeMetric metric = new ChatCodeMetric();
        metric.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        metric.generatedLines = generatedLines;
        metric.appliedLines = appliedLines;
        metric.sessionDurationMs = sessionDurationMs;
        metric.taskType = taskType; // "bug_fix", "feature_dev", "refactor", "explain"
        metric.applicationRate = generatedLines > 0 ? (double) appliedLines / generatedLines : 0.0;
        
        state.chatCodeGenerations.add(metric);
        updateDailyStats();
    }
    
    // ==================== 时间节省指标 ====================
    
    /**
     * 记录传统编码时间 vs AI辅助编码时间
     */
    public void recordTimeSaving(String taskType, long traditionalTimeMs, long aiAssistedTimeMs, int linesOfCode) {
        TimeSavingMetric metric = new TimeSavingMetric();
        metric.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        metric.taskType = taskType;
        metric.traditionalTimeMs = traditionalTimeMs;
        metric.aiAssistedTimeMs = aiAssistedTimeMs;
        metric.linesOfCode = linesOfCode;
        metric.timeSavedMs = traditionalTimeMs - aiAssistedTimeMs;
        metric.efficiencyGain = traditionalTimeMs > 0 ? 
            ((double) (traditionalTimeMs - aiAssistedTimeMs) / traditionalTimeMs) * 100 : 0.0;
        
        state.timeSavings.add(metric);
        updateDailyStats();
    }
    
    /**
     * 记录调试时间节省
     */
    public void recordDebuggingTimeSaving(long debugTimeWithoutAI, long debugTimeWithAI, String issueType) {
        DebuggingMetric metric = new DebuggingMetric();
        metric.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        metric.debugTimeWithoutAI = debugTimeWithoutAI;
        metric.debugTimeWithAI = debugTimeWithAI;
        metric.issueType = issueType;
        metric.timeSavedMs = debugTimeWithoutAI - debugTimeWithAI;
        
        state.debuggingMetrics.add(metric);
        updateDailyStats();
    }
    
    // ==================== 代码质量指标 ====================
    
    /**
     * 记录代码质量改进
     */
    public void recordCodeQualityImprovement(String metricType, double beforeValue, double afterValue, String improvementType) {
        CodeQualityMetric metric = new CodeQualityMetric();
        metric.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        metric.metricType = metricType; // "complexity", "coverage", "maintainability"
        metric.beforeValue = beforeValue;
        metric.afterValue = afterValue;
        metric.improvementType = improvementType; // "refactor", "optimize", "review"
        metric.improvementPercentage = beforeValue > 0 ? 
            ((afterValue - beforeValue) / beforeValue) * 100 : 0.0;
        
        state.codeQualityMetrics.add(metric);
        updateDailyStats();
    }
    
    // ==================== 学习效率指标 ====================
    
    /**
     * 记录学习和知识获取
     */
    public void recordLearningActivity(String topicType, int questionsAsked, int conceptsLearned, long learningTimeMs) {
        LearningMetric metric = new LearningMetric();
        metric.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        metric.topicType = topicType; // "new_framework", "debugging", "best_practices"
        metric.questionsAsked = questionsAsked;
        metric.conceptsLearned = conceptsLearned;
        metric.learningTimeMs = learningTimeMs;
        metric.learningEfficiency = learningTimeMs > 0 ? 
            (double) conceptsLearned / (learningTimeMs / 1000.0 / 60.0) : 0.0; // 概念/分钟
        
        state.learningMetrics.add(metric);
        updateDailyStats();
    }
    
    // ==================== 综合分析方法 ====================
    
    /**
     * 获取每日提效统计
     */
    public DailyProductivityStats getDailyStats(String date) {
        return state.dailyStats.getOrDefault(date, new DailyProductivityStats());
    }
    
    /**
     * 获取周期性提效报告
     */
    public ProductivityReport getProductivityReport(int days) {
        ProductivityReport report = new ProductivityReport();
        
        // 计算代码生成效率
        double avgAcceptanceRate = state.codeCompletions.stream()
            .mapToDouble(m -> m.acceptanceRate)
            .average().orElse(0.0);
        
        // 计算时间节省
        long totalTimeSaved = state.timeSavings.stream()
            .mapToLong(m -> m.timeSavedMs)
            .sum();
        
        // 计算平均效率提升
        double avgEfficiencyGain = state.timeSavings.stream()
            .mapToDouble(m -> m.efficiencyGain)
            .average().orElse(0.0);
        
        report.avgCodeAcceptanceRate = avgAcceptanceRate;
        report.totalTimeSavedHours = totalTimeSaved / 1000.0 / 3600.0;
        report.avgEfficiencyGain = avgEfficiencyGain;
        report.totalLinesGenerated = state.codeCompletions.stream()
            .mapToInt(m -> m.acceptedLines)
            .sum();
        
        return report;
    }
    
    /**
     * 更新每日统计
     */
    private void updateDailyStats() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        DailyProductivityStats stats = state.dailyStats.computeIfAbsent(today, k -> new DailyProductivityStats());
        
        // 更新今日统计数据
        stats.codeCompletionsCount = (int) state.codeCompletions.stream()
            .filter(m -> m.timestamp.startsWith(today))
            .count();
        
        stats.chatSessionsCount = (int) state.chatCodeGenerations.stream()
            .filter(m -> m.timestamp.startsWith(today))
            .count();
        
        stats.timeSavedMs = state.timeSavings.stream()
            .filter(m -> m.timestamp.startsWith(today))
            .mapToLong(m -> m.timeSavedMs)
            .sum();
        
        stats.linesGenerated = state.codeCompletions.stream()
            .filter(m -> m.timestamp.startsWith(today))
            .mapToInt(m -> m.acceptedLines)
            .sum();
    }
    
    /**
     * 清除所有统计数据
     */
    public void clearAllData() {
        state.codeCompletions.clear();
        state.chatCodeGenerations.clear();
        state.timeSavings.clear();
        state.debuggingMetrics.clear();
        state.codeQualityMetrics.clear();
        state.learningMetrics.clear();
        state.dailyStats.clear();
        
        System.out.println("所有提效统计数据已清除");
    }
    
    /**
     * 清除指定日期范围的数据
     */
    public void clearDataByDateRange(String startDate, String endDate) {
        state.codeCompletions.removeIf(m -> isInDateRange(m.timestamp, startDate, endDate));
        state.chatCodeGenerations.removeIf(m -> isInDateRange(m.timestamp, startDate, endDate));
        state.timeSavings.removeIf(m -> isInDateRange(m.timestamp, startDate, endDate));
        state.debuggingMetrics.removeIf(m -> isInDateRange(m.timestamp, startDate, endDate));
        state.codeQualityMetrics.removeIf(m -> isInDateRange(m.timestamp, startDate, endDate));
        state.learningMetrics.removeIf(m -> isInDateRange(m.timestamp, startDate, endDate));
        
        // 清除对应日期的每日统计
        state.dailyStats.entrySet().removeIf(entry -> 
            entry.getKey().compareTo(startDate) >= 0 && entry.getKey().compareTo(endDate) <= 0);
        
        System.out.println("已清除 " + startDate + " 到 " + endDate + " 的统计数据");
    }
    
    private boolean isInDateRange(String timestamp, String startDate, String endDate) {
        String date = timestamp.substring(0, 10); // 提取日期部分 YYYY-MM-DD
        return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
    }
    
    // ==================== 数据模型 ====================
    
    public static class State {
        public List<CodeCompletionMetric> codeCompletions = new ArrayList<>();
        public List<ChatCodeMetric> chatCodeGenerations = new ArrayList<>();
        public List<TimeSavingMetric> timeSavings = new ArrayList<>();
        public List<DebuggingMetric> debuggingMetrics = new ArrayList<>();
        public List<CodeQualityMetric> codeQualityMetrics = new ArrayList<>();
        public List<LearningMetric> learningMetrics = new ArrayList<>();
        public Map<String, DailyProductivityStats> dailyStats = new ConcurrentHashMap<>();
    }
    
    public static class CodeCompletionMetric {
        public String timestamp;
        public String language;
        public int suggestedLines;
        public int acceptedLines;
        public long responseTimeMs;
        public double acceptanceRate;
    }
    
    public static class ChatCodeMetric {
        public String timestamp;
        public int generatedLines;
        public int appliedLines;
        public long sessionDurationMs;
        public String taskType;
        public double applicationRate;
    }
    
    public static class TimeSavingMetric {
        public String timestamp;
        public String taskType;
        public long traditionalTimeMs;
        public long aiAssistedTimeMs;
        public int linesOfCode;
        public long timeSavedMs;
        public double efficiencyGain;
    }
    
    public static class DebuggingMetric {
        public String timestamp;
        public long debugTimeWithoutAI;
        public long debugTimeWithAI;
        public String issueType;
        public long timeSavedMs;
    }
    
    public static class CodeQualityMetric {
        public String timestamp;
        public String metricType;
        public double beforeValue;
        public double afterValue;
        public String improvementType;
        public double improvementPercentage;
    }
    
    public static class LearningMetric {
        public String timestamp;
        public String topicType;
        public int questionsAsked;
        public int conceptsLearned;
        public long learningTimeMs;
        public double learningEfficiency;
    }
    
    public static class DailyProductivityStats {
        public int codeCompletionsCount;
        public int chatSessionsCount;
        public long timeSavedMs;
        public int linesGenerated;
        public double avgResponseTime;
    }
    
    public static class ProductivityReport {
        public double avgCodeAcceptanceRate;
        public double totalTimeSavedHours;
        public double avgEfficiencyGain;
        public int totalLinesGenerated;
        public Map<String, Double> taskTypeEfficiency = new HashMap<>();
        public Map<String, Integer> languageUsage = new HashMap<>();
    }
    
    @Override
    public @Nullable State getState() {
        return state;
    }
    
    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
}