package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ProxyAI效能度量监控系统
 * 实时监控度量收集的状态和性能
 */
@Service
public final class MetricsMonitor {
    
    private static final Logger LOG = Logger.getInstance(MetricsMonitor.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // 统计计数器
    private final AtomicInteger chatSessionsCount = new AtomicInteger(0);
    private final AtomicInteger codeCompletionsCount = new AtomicInteger(0);
    private final AtomicInteger aiResponsesCount = new AtomicInteger(0);
    private final AtomicLong totalCodeLinesGenerated = new AtomicLong(0);
    private final AtomicLong totalTimeSaved = new AtomicLong(0);
    
    // 错误统计
    private final AtomicInteger metricsErrors = new AtomicInteger(0);
    private final ConcurrentHashMap<String, Integer> errorTypes = new ConcurrentHashMap<>();
    
    // 性能统计
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicInteger responseCount = new AtomicInteger(0);
    
    // 活动会话
    private final ConcurrentHashMap<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    
    // 最近活动记录
    private final java.util.concurrent.ConcurrentLinkedQueue<ActivityRecord> recentActivities = 
        new java.util.concurrent.ConcurrentLinkedQueue<>();
    
    public static MetricsMonitor getInstance() {
        return ApplicationManager.getApplication().getService(MetricsMonitor.class);
    }
    
    /**
     * 初始化监控系统
     */
    public void initialize() {
        // 启动定期监控任务
        scheduler.scheduleAtFixedRate(this::performHealthCheck, 1, 5, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::generateStatusReport, 10, 10, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::cleanupOldData, 30, 30, TimeUnit.MINUTES);
        
        LOG.info("ProxyAI效能度量监控系统已启动");
        recordActivity("SYSTEM", "监控系统启动", "系统初始化完成");
    }
    
    /**
     * 记录聊天会话开始
     */
    public void recordChatSessionStart(String sessionId, String taskType) {
        chatSessionsCount.incrementAndGet();
        
        SessionInfo session = new SessionInfo();
        session.sessionId = sessionId;
        session.type = "CHAT";
        session.taskType = taskType;
        session.startTime = LocalDateTime.now();
        session.status = "ACTIVE";
        
        activeSessions.put(sessionId, session);
        recordActivity("CHAT", "聊天会话开始", "任务类型: " + taskType);
        
        LOG.info("聊天会话开始: " + sessionId + " (类型: " + taskType + ")");
    }
    
    /**
     * 记录AI响应
     */
    public void recordAIResponse(String sessionId, String response, int codeLines) {
        aiResponsesCount.incrementAndGet();
        totalCodeLinesGenerated.addAndGet(codeLines);
        
        SessionInfo session = activeSessions.get(sessionId);
        if (session != null) {
            session.responsesCount++;
            session.totalCodeLines += codeLines;
            session.lastActivityTime = LocalDateTime.now();
        }
        
        recordActivity("AI_RESPONSE", "AI响应生成", codeLines + " 行代码");
        
        LOG.info("AI响应记录: " + sessionId + " (代码行数: " + codeLines + ")");
    }
    
    /**
     * 记录代码补全
     */
    public void recordCodeCompletion(String language, int suggestedLines, int acceptedLines, long responseTime) {
        codeCompletionsCount.incrementAndGet();
        totalCodeLinesGenerated.addAndGet(acceptedLines);
        totalResponseTime.addAndGet(responseTime);
        responseCount.incrementAndGet();
        
        recordActivity("CODE_COMPLETION", "代码补全", 
            language + " - " + acceptedLines + "/" + suggestedLines + " 行");
        
        LOG.info("代码补全记录: " + language + " (接受: " + acceptedLines + "/" + suggestedLines + " 行)");
    }
    
    /**
     * 记录时间节省
     */
    public void recordTimeSaving(long timeSavedMs) {
        totalTimeSaved.addAndGet(timeSavedMs);
        
        double timeSavedMinutes = timeSavedMs / 1000.0 / 60.0;
        recordActivity("TIME_SAVING", "时间节省", String.format("%.1f 分钟", timeSavedMinutes));
        
        LOG.info("时间节省记录: " + String.format("%.1f", timeSavedMinutes) + " 分钟");
    }
    
    /**
     * 记录错误
     */
    public void recordError(String errorType, String errorMessage, Exception exception) {
        metricsErrors.incrementAndGet();
        errorTypes.put(errorType, errorTypes.getOrDefault(errorType, 0) + 1);
        
        recordActivity("ERROR", "系统错误", errorType + ": " + errorMessage);
        
        LOG.warn("度量收集错误: " + errorType + " - " + errorMessage, exception);
    }
    
    /**
     * 结束会话
     */
    public void endSession(String sessionId) {
        SessionInfo session = activeSessions.remove(sessionId);
        if (session != null) {
            session.endTime = LocalDateTime.now();
            session.status = "COMPLETED";
            
            long durationMs = java.time.temporal.ChronoUnit.MILLIS.between(session.startTime, session.endTime);
            recordActivity("SESSION_END", "会话结束", 
                "持续时间: " + String.format("%.1f", durationMs / 1000.0 / 60.0) + " 分钟");
            
            LOG.info("会话结束: " + sessionId + " (持续: " + String.format("%.1f", durationMs / 1000.0 / 60.0) + " 分钟)");
        }
    }
    
    /**
     * 获取实时统计
     */
    public MonitoringStats getRealTimeStats() {
        MonitoringStats stats = new MonitoringStats();
        stats.chatSessionsCount = chatSessionsCount.get();
        stats.codeCompletionsCount = codeCompletionsCount.get();
        stats.aiResponsesCount = aiResponsesCount.get();
        stats.totalCodeLinesGenerated = totalCodeLinesGenerated.get();
        stats.totalTimeSavedMs = totalTimeSaved.get();
        stats.metricsErrors = metricsErrors.get();
        stats.activeSessionsCount = activeSessions.size();
        
        // 计算平均响应时间
        int responses = responseCount.get();
        stats.averageResponseTimeMs = responses > 0 ? totalResponseTime.get() / responses : 0;
        
        return stats;
    }
    
    /**
     * 获取最近活动
     */
    public java.util.List<ActivityRecord> getRecentActivities(int limit) {
        java.util.List<ActivityRecord> activities = new java.util.ArrayList<>();
        int count = 0;
        
        for (ActivityRecord activity : recentActivities) {
            if (count >= limit) break;
            activities.add(activity);
            count++;
        }
        
        return activities;
    }
    
    /**
     * 获取活动会话信息
     */
    public java.util.List<SessionInfo> getActiveSessions() {
        return new java.util.ArrayList<>(activeSessions.values());
    }
    
    /**
     * 执行健康检查
     */
    private void performHealthCheck() {
        try {
            MonitoringStats stats = getRealTimeStats();
            
            // 检查系统健康状态
            boolean isHealthy = true;
            java.util.List<String> issues = new java.util.ArrayList<>();
            
            // 检查错误率
            if (stats.metricsErrors > 10) {
                isHealthy = false;
                issues.add("度量收集错误过多: " + stats.metricsErrors);
            }
            
            // 检查响应时间
            if (stats.averageResponseTimeMs > 5000) {
                isHealthy = false;
                issues.add("平均响应时间过长: " + stats.averageResponseTimeMs + "ms");
            }
            
            // 检查活动会话数量
            if (stats.activeSessionsCount > 50) {
                issues.add("活动会话数量较多: " + stats.activeSessionsCount);
            }
            
            // 记录健康检查结果
            String healthStatus = isHealthy ? "健康" : "异常";
            recordActivity("HEALTH_CHECK", "系统健康检查", healthStatus);
            
            if (!isHealthy) {
                LOG.warn("系统健康检查发现问题: " + String.join(", ", issues));
            } else {
                LOG.info("系统健康检查通过");
            }
            
        } catch (Exception e) {
            recordError("HEALTH_CHECK", "健康检查失败", e);
        }
    }
    
    /**
     * 生成状态报告
     */
    private void generateStatusReport() {
        try {
            MonitoringStats stats = getRealTimeStats();
            
            StringBuilder report = new StringBuilder();
            report.append("\n=== ProxyAI效能度量监控报告 ===\n");
            report.append("时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            report.append("聊天会话: ").append(stats.chatSessionsCount).append(" 次\n");
            report.append("代码补全: ").append(stats.codeCompletionsCount).append(" 次\n");
            report.append("AI响应: ").append(stats.aiResponsesCount).append(" 次\n");
            report.append("生成代码: ").append(stats.totalCodeLinesGenerated).append(" 行\n");
            report.append("节省时间: ").append(String.format("%.1f", stats.totalTimeSavedMs / 1000.0 / 3600.0)).append(" 小时\n");
            report.append("活动会话: ").append(stats.activeSessionsCount).append(" 个\n");
            report.append("平均响应时间: ").append(stats.averageResponseTimeMs).append(" ms\n");
            report.append("系统错误: ").append(stats.metricsErrors).append(" 次\n");
            
            if (!errorTypes.isEmpty()) {
                report.append("错误类型分布:\n");
                errorTypes.forEach((type, count) -> 
                    report.append("  - ").append(type).append(": ").append(count).append(" 次\n"));
            }
            
            report.append("================================\n");
            
            System.out.println(report.toString());
            LOG.info("状态报告已生成");
            
        } catch (Exception e) {
            recordError("STATUS_REPORT", "生成状态报告失败", e);
        }
    }
    
    /**
     * 清理过期数据
     */
    private void cleanupOldData() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(2);
            
            // 清理过期的活动记录
            recentActivities.removeIf(activity -> activity.timestamp.isBefore(cutoff));
            
            // 清理长时间无活动的会话
            activeSessions.entrySet().removeIf(entry -> {
                SessionInfo session = entry.getValue();
                LocalDateTime lastActivity = session.lastActivityTime != null ? 
                    session.lastActivityTime : session.startTime;
                return lastActivity.isBefore(cutoff);
            });
            
            LOG.info("过期数据清理完成");
            
        } catch (Exception e) {
            recordError("CLEANUP", "清理过期数据失败", e);
        }
    }
    
    /**
     * 记录活动
     */
    private void recordActivity(String type, String action, String details) {
        ActivityRecord activity = new ActivityRecord();
        activity.timestamp = LocalDateTime.now();
        activity.type = type;
        activity.action = action;
        activity.details = details;
        
        recentActivities.offer(activity);
        
        // 保持活动记录数量在合理范围内
        while (recentActivities.size() > 100) {
            recentActivities.poll();
        }
    }
    
    /**
     * 关闭监控系统
     */
    public void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            LOG.info("ProxyAI效能度量监控系统已关闭");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // ==================== 数据模型 ====================
    
    public static class MonitoringStats {
        public int chatSessionsCount;
        public int codeCompletionsCount;
        public int aiResponsesCount;
        public long totalCodeLinesGenerated;
        public long totalTimeSavedMs;
        public int metricsErrors;
        public int activeSessionsCount;
        public long averageResponseTimeMs;
    }
    
    public static class ActivityRecord {
        public LocalDateTime timestamp;
        public String type;
        public String action;
        public String details;
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s - %s", 
                timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                type, action, details);
        }
    }
    
    public static class SessionInfo {
        public String sessionId;
        public String type;
        public String taskType;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public LocalDateTime lastActivityTime;
        public String status;
        public int responsesCount;
        public int totalCodeLines;
    }
}