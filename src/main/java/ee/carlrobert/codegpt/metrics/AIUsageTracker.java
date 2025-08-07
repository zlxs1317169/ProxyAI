package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import ee.carlrobert.codegpt.settings.metrics.MetricsSettings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI使用跟踪器
 * 只跟踪真正的AI功能使用，避免误判普通编辑为AI使用
 */
@Service
public final class AIUsageTracker {
    
    private static final Logger LOG = Logger.getInstance(AIUsageTracker.class);
    
    // 跟踪当前活跃的AI会话
    private final ConcurrentHashMap<String, AISession> activeSessions = new ConcurrentHashMap<>();
    
    // 统计计数器
    private final AtomicInteger dailyCompletions = new AtomicInteger(0);
    private final AtomicInteger dailyChatSessions = new AtomicInteger(0);
    
    public static AIUsageTracker getInstance() {
        return ApplicationManager.getApplication().getService(AIUsageTracker.class);
    }
    
    /**
     * 记录真实的AI代码补全使用
     * 只有在确实使用AI补全功能时才调用此方法
     */
    public void recordRealAICompletion(String language, String suggestedCode, boolean accepted, long processingTime) {
        try {
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings == null || !settings.isMetricsEnabled()) {
                return;
            }
            
            // 记录到ProductivityMetrics
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                int linesGenerated = suggestedCode.split("\n").length;
                int acceptedLines = accepted ? linesGenerated : 0;
                
                metrics.recordCodeCompletion(language, linesGenerated, acceptedLines, processingTime);
                
                // 更新计数器
                dailyCompletions.incrementAndGet();
                
                LOG.info("记录真实AI代码补全: 语言=" + language + 
                        ", 生成行数=" + linesGenerated + 
                        ", 接受=" + accepted + 
                        ", 处理时间=" + processingTime + "ms");
            }
            
        } catch (Exception e) {
            LOG.warn("记录AI代码补全时发生错误", e);
        }
    }
    
    /**
     * 记录真实的AI聊天代码生成
     * 只有在确实使用AI聊天功能时才调用此方法
     */
    public void recordRealAIChatGeneration(String generatedCode, boolean applied, long processingTime, String sessionId) {
        try {
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings == null || !settings.isMetricsEnabled()) {
                return;
            }
            
            // 记录到ProductivityMetrics
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                int linesGenerated = generatedCode.split("\n").length;
                int appliedLines = applied ? linesGenerated : 0;
                
                metrics.recordChatCodeGeneration(linesGenerated, appliedLines, processingTime, sessionId);
                
                // 更新计数器
                dailyChatSessions.incrementAndGet();
                
                LOG.info("记录真实AI聊天代码生成: 会话=" + sessionId + 
                        ", 生成行数=" + linesGenerated + 
                        ", 应用=" + applied + 
                        ", 处理时间=" + processingTime + "ms");
            }
            
        } catch (Exception e) {
            LOG.warn("记录AI聊天代码生成时发生错误", e);
        }
    }
    
    /**
     * 开始AI会话跟踪
     */
    public void startAISession(String sessionId, String type) {
        try {
            AISession session = new AISession(sessionId, type, LocalDateTime.now());
            activeSessions.put(sessionId, session);
            
            LOG.info("开始AI会话跟踪: " + sessionId + " (类型: " + type + ")");
            
        } catch (Exception e) {
            LOG.warn("开始AI会话跟踪时发生错误", e);
        }
    }
    
    /**
     * 结束AI会话跟踪
     */
    public void endAISession(String sessionId) {
        try {
            AISession session = activeSessions.remove(sessionId);
            if (session != null) {
                LOG.info("结束AI会话跟踪: " + sessionId + 
                        " (持续时间: " + session.getDurationMinutes() + "分钟)");
            }
            
        } catch (Exception e) {
            LOG.warn("结束AI会话跟踪时发生错误", e);
        }
    }
    
    /**
     * 获取今日统计数据
     */
    public DailyStats getTodayStats() {
        return new DailyStats(
            dailyCompletions.get(),
            dailyChatSessions.get(),
            activeSessions.size()
        );
    }
    
    /**
     * 重置日统计数据（通常在每日开始时调用）
     */
    public void resetDailyStats() {
        dailyCompletions.set(0);
        dailyChatSessions.set(0);
        LOG.info("已重置日统计数据");
    }
    
    /**
     * 检查是否有活跃的AI会话
     */
    public boolean hasActiveAISessions() {
        return !activeSessions.isEmpty();
    }
    
    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * 清空所有AI使用跟踪数据
     */
    public void clearAllData() {
        try {
            // 清空活跃会话
            activeSessions.clear();
            
            // 重置计数器
            dailyCompletions.set(0);
            dailyChatSessions.set(0);
            
            LOG.info("已清空所有AI使用跟踪数据");
            
        } catch (Exception e) {
            LOG.warn("清空AI使用跟踪数据时发生错误", e);
        }
    }
    
    /**
     * AI会话信息
     */
    private static class AISession {
        private final String sessionId;
        private final String type;
        private final LocalDateTime startTime;
        
        public AISession(String sessionId, String type, LocalDateTime startTime) {
            this.sessionId = sessionId;
            this.type = type;
            this.startTime = startTime;
        }
        
        public long getDurationMinutes() {
            return java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public String getType() {
            return type;
        }
        
        public LocalDateTime getStartTime() {
            return startTime;
        }
    }
    
    /**
     * 日统计数据
     */
    public static class DailyStats {
        public final int completionsCount;
        public final int chatSessionsCount;
        public final int activeSessionsCount;
        
        public DailyStats(int completionsCount, int chatSessionsCount, int activeSessionsCount) {
            this.completionsCount = completionsCount;
            this.chatSessionsCount = chatSessionsCount;
            this.activeSessionsCount = activeSessionsCount;
        }
        
        @Override
        public String toString() {
            return String.format("今日统计: 代码补全=%d次, 聊天会话=%d次, 活跃会话=%d个", 
                completionsCount, chatSessionsCount, activeSessionsCount);
        }
    }
}