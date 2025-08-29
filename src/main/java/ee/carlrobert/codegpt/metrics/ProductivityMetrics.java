package ee.carlrobert.codegpt.metrics;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 核心指标数据模型，用于存储与大模型交互的软件工程师效能度量指标
 */
public class ProductivityMetrics {
    // 基础标识信息
    private String id;
    private String actionId;
    private String actionType;
    private String modelName;
    private String sessionId;
    private String userId;
    
    // 时间相关指标
    private long startTime;
    private long endTime;
    private long responseTime;
    private long processingTime;
    
    // AI交互指标
    private int inputTokenCount;
    private int outputTokenCount;
    private int totalTokenCount;
    private double tokenCost;
    
    // 代码质量指标
    private int linesGenerated;
    private int linesAccepted;
    private int linesRejected;
    private double acceptanceRate;
    
    // 效能指标
    private boolean successful;
    private String errorMessage;
    private int retryCount;
    private String qualityScore;
    
    // 上下文信息
    private String programmingLanguage;
    private String projectType;
    private String fileExtension;
    private String contextSize;
    
    // 用户行为指标
    private String userExperience;
    private int userRating;
    private String feedback;
    
    // 系统性能指标
    private long memoryUsage;
    private double cpuUsage;
    private String networkLatency;
    
    // 新增指标字段
    private long sessionDuration;
    private int responseLength;
    private int codeLength;
    private double codeDensity;
    private int conceptsLearned;
    private double learningEfficiency;
    
    // 扩展数据
    private Map<String, Object> additionalData;
    
    // 时间戳
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 静态实例（单例模式）
    private static volatile ProductivityMetrics instance;
    private static final Object lock = new Object();

    public ProductivityMetrics() {
        this.id = UUID.randomUUID().toString();
        this.startTime = Instant.now().toEpochMilli();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.additionalData = new HashMap<>();
        this.retryCount = 0;
        this.userRating = 0;
    }

    public ProductivityMetrics(String actionId, String actionType) {
        this();
        this.actionId = actionId;
        this.actionType = actionType;
    }

    /**
     * 获取单例实例（线程安全）
     */
    public static ProductivityMetrics getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ProductivityMetrics();
                }
            }
        }
        return instance;
    }

    /**
     * 重置单例实例（用于测试）
     */
    public static void resetInstance() {
        synchronized (lock) {
            instance = null;
        }
    }

    /**
     * 完成指标收集
     */
    public void complete() {
        this.endTime = Instant.now().toEpochMilli();
        this.responseTime = this.endTime - this.startTime;
        this.updatedAt = LocalDateTime.now();
        
        // 计算接受率
        if (linesGenerated > 0) {
            this.acceptanceRate = (double) linesAccepted / linesGenerated;
        }
        
        // 计算总token数
        this.totalTokenCount = inputTokenCount + outputTokenCount;
    }

    /**
     * 标记成功
     */
    public void markSuccessful() {
        this.successful = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记失败
     */
    public void markFailed(String errorMessage) {
        this.successful = false;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 记录代码补全指标
     */
    public void recordCodeCompletion(String language, int linesGenerated, int linesAccepted, long processingTime) {
        this.programmingLanguage = language;
        this.linesGenerated = linesGenerated;
        this.linesAccepted = linesAccepted;
        this.linesRejected = linesGenerated - linesAccepted;
        this.processingTime = processingTime;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 记录聊天代码生成指标
     */
    public void recordChatCodeGeneration(int linesGenerated, int linesAccepted, long processingTime, String sessionId) {
        this.linesGenerated = linesGenerated;
        this.linesAccepted = linesAccepted;
        this.linesRejected = linesGenerated - linesAccepted;
        this.processingTime = processingTime;
        this.sessionId = sessionId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置用户评分
     */
    public void setUserRating(int rating) {
        this.userRating = Math.max(1, Math.min(5, rating)); // 限制在1-5之间
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 添加扩展数据
     */
    public void addAdditionalData(String key, Object value) {
        this.additionalData.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置创建时间（用于兼容性）
     */
    public void setCreatedAt(java.sql.Timestamp timestamp) {
        if (timestamp != null) {
            this.createdAt = timestamp.toLocalDateTime();
        }
    }
    
    /**
     * 记录时间节省
     */
    public void recordTimeSaving(String taskType, long traditionalTime, long aiAssistedTime, int linesOfCode) {
        this.addAdditionalData("taskType", taskType);
        this.addAdditionalData("traditionalTime", traditionalTime);
        this.addAdditionalData("aiAssistedTime", aiAssistedTime);
        this.addAdditionalData("linesOfCode", linesOfCode);
        this.addAdditionalData("timeSaved", traditionalTime - aiAssistedTime);
        this.addAdditionalData("efficiencyGain", ((double) (traditionalTime - aiAssistedTime) / traditionalTime) * 100);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 记录调试时间节省（用于兼容性）
     */
    public void recordDebuggingTimeSaving(long debugTimeWithoutAI, long debugTimeWithAI, String issueType) {
        this.addAdditionalData("issueType", issueType);
        this.addAdditionalData("debugTimeWithoutAI", debugTimeWithoutAI);
        this.addAdditionalData("debugTimeWithAI", debugTimeWithAI);
        this.addAdditionalData("timeSaved", debugTimeWithoutAI - debugTimeWithAI);
        this.addAdditionalData("efficiencyGain", ((double) (debugTimeWithoutAI - debugTimeWithAI) / debugTimeWithoutAI) * 100);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 记录代码质量改进（用于兼容性）
     */
    public void recordCodeQualityImprovement(String metricType, double before, double after, String improvementType) {
        this.addAdditionalData("metricType", metricType);
        this.addAdditionalData("beforeValue", before);
        this.addAdditionalData("afterValue", after);
        this.addAdditionalData("improvement", after - before);
        this.addAdditionalData("improvementType", improvementType);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 记录学习活动（用于兼容性）
     */
    public void recordLearningActivity(String topic, int questionsAsked, long learningTime) {
        this.addAdditionalData("topic", topic);
        this.addAdditionalData("questionsAsked", questionsAsked);
        this.addAdditionalData("learningTime", learningTime);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 记录学习活动（重载版本，用于兼容性）
     */
    public void recordLearningActivity(String topic, int questionsAsked, int conceptsLearned, long learningTime) {
        this.addAdditionalData("topic", topic);
        this.addAdditionalData("questionsAsked", questionsAsked);
        this.addAdditionalData("conceptsLearned", conceptsLearned);
        this.addAdditionalData("learningTime", learningTime);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 清除所有数据（用于兼容性）
     */
    public void clearAllData() {
        // 这里应该清除所有指标数据
        // 暂时只是记录日志
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 获取状态信息（用于兼容性）
     */
    public State getState() {
        return new State();
    }
    
    /**
     * 获取生产力报告（用于兼容性）
     */
    public ProductivityReport getProductivityReport(int days) {
        // 创建一个默认的报告，避免返回 null
        ProductivityReport report = new ProductivityReport();
        
        // 设置基本的时间信息
        report.analysisTime = java.time.LocalDateTime.now().toString();
        
        // 从当前实例中提取数据
        if (this.linesGenerated > 0) {
            report.totalLinesGenerated = this.linesGenerated;
        }
        
        if (this.acceptanceRate > 0) {
            report.avgCodeAcceptanceRate = this.acceptanceRate;
        }
        
        // 计算时间节省（基于响应时间和处理时间）
        if (this.responseTime > 0 && this.processingTime > 0) {
            // 假设AI帮助节省了处理时间
            long timeSavedMs = Math.max(0, this.processingTime - this.responseTime);
            report.totalTimeSavedHours = timeSavedMs / (1000.0 * 60.0 * 60.0);
        }
        
        // 计算效率提升
        if (this.responseTime > 0) {
            report.avgEfficiencyGain = Math.max(0, 100.0 - (this.responseTime / 1000.0));
        }
        
        // 设置总体评分
        report.overallScore = calculateSimpleScore();
        
        // 根据评分设置效率等级
        if (report.overallScore >= 80) {
            report.efficiencyLevel = "Excellent";
        } else if (report.overallScore >= 60) {
            report.efficiencyLevel = "Good";
        } else if (report.overallScore >= 40) {
            report.efficiencyLevel = "Fair";
        } else {
            report.efficiencyLevel = "Poor";
        }
        
        // 生成摘要
        if (report.totalLinesGenerated > 0) {
            report.summary = String.format("Generated %d lines of code with %.1f%% acceptance rate. " +
                "Time saved: %.2f hours. Overall efficiency: %s", 
                report.totalLinesGenerated, 
                report.avgCodeAcceptanceRate, 
                report.totalTimeSavedHours, 
                report.efficiencyLevel);
        } else {
            report.summary = "No significant activity recorded yet.";
        }
        
        return report;
    }
    
    /**
     * 计算简单的总体评分
     */
    private double calculateSimpleScore() {
        double score = 0.0;
        
        // 基于代码生成量
        if (linesGenerated > 0) {
            score += Math.min(30, linesGenerated * 2); // 最多30分
        }
        
        // 基于接受率
        if (acceptanceRate > 0) {
            score += acceptanceRate * 0.3; // 最多30分
        }
        
        // 基于响应时间
        if (responseTime > 0 && responseTime < 5000) {
            score += 30; // 快速响应
        } else if (responseTime < 15000) {
            score += 20; // 中等响应
        } else if (responseTime < 30000) {
            score += 10; // 慢响应
        }
        
        // 基于用户评分
        score += userRating * 6; // 30分权重
        
        return Math.min(100, score);
    }
    
    /**
     * 获取每日统计（用于兼容性）
     */
    public DailyProductivityStats getDailyStats(String date) {
        // 创建一个默认的每日统计，避免返回 null
        DailyProductivityStats stats = new DailyProductivityStats(date);
        
        // 从当前实例中提取数据
        if (this.linesGenerated > 0) {
            stats.totalLinesGenerated = this.linesGenerated;
        }
        
        if (this.acceptanceRate > 0) {
            stats.averageAcceptanceRate = this.acceptanceRate;
        }
        
        if (this.responseTime > 0) {
            stats.averageResponseTime = this.responseTime;
            stats.avgResponseTime = this.responseTime;
        }
        
        // 设置基本计数
        if (this.actionType != null) {
            if (this.actionType.contains("completion")) {
                stats.codeCompletionsCount = 1;
            } else if (this.actionType.contains("chat")) {
                stats.chatSessionsCount = 1;
            }
        }
        
        // 设置请求统计
        stats.totalRequests = 1;
        if (this.successful) {
            stats.successfulRequests = 1;
        }
        
        // 计算时间节省
        if (this.responseTime > 0 && this.processingTime > 0) {
            stats.timeSavedMs = Math.max(0, this.processingTime - this.responseTime);
        }
        
        return stats;
    }
    
    /**
     * 内部状态类（用于兼容性）
     */
    public static class State {
        public java.util.List<Object> codeCompletions;
        public java.util.List<Object> chatCodeGenerations;
        public java.util.List<Object> timeSavings;
        public java.util.Map<String, DailyProductivityStats> dailyStats;
        
        public State() {
            this.codeCompletions = new java.util.ArrayList<>();
            this.chatCodeGenerations = new java.util.ArrayList<>();
            this.timeSavings = new java.util.ArrayList<>();
            this.dailyStats = new java.util.HashMap<>();
        }
    }
    
    /**
     * 每日生产力统计类（用于兼容性）
     */
    public static class DailyProductivityStats {
        public String date;
        public int totalRequests;
        public int successfulRequests;
        public double averageResponseTime;
        public int totalLinesGenerated;
        public double averageAcceptanceRate;
        public int codeCompletionsCount;
        public int chatSessionsCount;
        public long timeSavedMs;
        public double avgResponseTime;
        
        public DailyProductivityStats(String date) {
            this.date = date;
            this.totalRequests = 0;
            this.successfulRequests = 0;
            this.averageResponseTime = 0.0;
            this.totalLinesGenerated = 0;
            this.averageAcceptanceRate = 0.0;
            this.codeCompletionsCount = 0;
            this.chatSessionsCount = 0;
            this.timeSavedMs = 0L;
            this.avgResponseTime = 0.0;
        }
    }
    
    /**
     * 生产力报告类（用于兼容性）
     */
    public static class ProductivityReport {
        public String analysisTime;
        public int totalMetrics;
        public double overallScore;
        public String efficiencyLevel;
        public String summary;
        public int totalLinesGenerated;
        public double avgCodeAcceptanceRate;
        public double totalTimeSavedHours;
        public double avgEfficiencyGain;
        
        public ProductivityReport() {
            this.analysisTime = java.time.LocalDateTime.now().toString();
            this.totalMetrics = 0;
            this.overallScore = 0.0;
            this.efficiencyLevel = "Unknown";
            this.summary = "No data available";
            this.totalLinesGenerated = 0;
            this.avgCodeAcceptanceRate = 0.0;
            this.totalTimeSavedHours = 0.0;
            this.avgEfficiencyGain = 0.0;
        }
    }

    /**
     * 获取格式化的创建时间
     */
    public String getFormattedCreatedTime() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取格式化的响应时间
     */
    public String getFormattedResponseTime() {
        if (responseTime > 0) {
            if (responseTime < 1000) {
                return responseTime + "ms";
            } else {
                return String.format("%.2fs", responseTime / 1000.0);
            }
        }
        return "N/A";
    }

    /**
     * 获取效能评分
     */
    public double getEfficiencyScore() {
        double score = 0.0;
        
        // 基于接受率
        if (acceptanceRate > 0) {
            score += acceptanceRate * 40; // 40分权重
        }
        
        // 基于响应时间
        if (responseTime > 0 && responseTime < 5000) {
            score += 30; // 快速响应
        } else if (responseTime < 15000) {
            score += 20; // 中等响应
        } else if (responseTime < 30000) {
            score += 10; // 慢响应
        }
        
        // 基于用户评分
        score += userRating * 6; // 30分权重
        
        return Math.min(100, score);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getActionId() { return actionId; }
    public void setActionId(String actionId) { this.actionId = actionId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }

    public long getProcessingTime() { return processingTime; }
    public void setProcessingTime(long processingTime) { this.processingTime = processingTime; }

    public int getInputTokenCount() { return inputTokenCount; }
    public void setInputTokenCount(int inputTokenCount) { this.inputTokenCount = inputTokenCount; }

    public int getOutputTokenCount() { return outputTokenCount; }
    public void setOutputTokenCount(int outputTokenCount) { this.outputTokenCount = outputTokenCount; }

    public int getTotalTokenCount() { return totalTokenCount; }
    public void setTotalTokenCount(int totalTokenCount) { this.totalTokenCount = totalTokenCount; }

    public double getTokenCost() { return tokenCost; }
    public void setTokenCost(double tokenCost) { this.tokenCost = tokenCost; }

    public int getLinesGenerated() { return linesGenerated; }
    public void setLinesGenerated(int linesGenerated) { this.linesGenerated = linesGenerated; }

    public int getLinesAccepted() { return linesAccepted; }
    public void setLinesAccepted(int linesAccepted) { this.linesAccepted = linesAccepted; }

    public int getLinesRejected() { return linesRejected; }
    public void setLinesRejected(int linesRejected) { this.linesRejected = linesRejected; }

    public double getAcceptanceRate() { return acceptanceRate; }
    public void setAcceptanceRate(double acceptanceRate) { this.acceptanceRate = acceptanceRate; }

    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public String getQualityScore() { return qualityScore; }
    public void setQualityScore(String qualityScore) { this.qualityScore = qualityScore; }

    public String getProgrammingLanguage() { return programmingLanguage; }
    public void setProgrammingLanguage(String programmingLanguage) { this.programmingLanguage = programmingLanguage; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }

    public String getContextSize() { return contextSize; }
    public void setContextSize(String contextSize) { this.contextSize = contextSize; }

    public String getUserExperience() { return userExperience; }
    public void setUserExperience(String userExperience) { this.userExperience = userExperience; }

    public int getUserRating() { return userRating; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public long getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(long memoryUsage) { this.memoryUsage = memoryUsage; }

    public double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }

    public String getNetworkLatency() { return networkLatency; }
    public void setNetworkLatency(String networkLatency) { this.networkLatency = networkLatency; }

    // 新增指标字段的getter和setter
    public long getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(long sessionDuration) { this.sessionDuration = sessionDuration; }
    
    public int getResponseLength() { return responseLength; }
    public void setResponseLength(int responseLength) { this.responseLength = responseLength; }
    
    public int getCodeLength() { return codeLength; }
    public void setCodeLength(int codeLength) { this.codeLength = codeLength; }
    
    public double getCodeDensity() { return codeDensity; }
    public void setCodeDensity(double codeDensity) { this.codeDensity = codeDensity; }
    
    public int getConceptsLearned() { return conceptsLearned; }
    public void setConceptsLearned(int conceptsLearned) { this.conceptsLearned = conceptsLearned; }
    
    public double getLearningEfficiency() { return learningEfficiency; }
    public void setLearningEfficiency(double learningEfficiency) { this.learningEfficiency = learningEfficiency; }

    public Map<String, Object> getAdditionalData() { return additionalData; }
    public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("ProductivityMetrics{actionId='%s', actionType='%s', modelName='%s', " +
                "responseTime=%dms, linesGenerated=%d, linesAccepted=%d, acceptanceRate=%.2f, " +
                "successful=%s, efficiencyScore=%.1f}",
                actionId, actionType, modelName, responseTime, linesGenerated, linesAccepted, 
                acceptanceRate, successful, getEfficiencyScore());
    }
    
    /**
     * 记录代码生成指标
     */
    public void recordCodeGeneration(int linesGenerated, int linesApplied, long sessionDuration) {
        try {
            this.linesGenerated = linesGenerated;
            this.linesAccepted = linesApplied;
            this.sessionDuration = sessionDuration;
            
            // 计算接受率
            if (linesGenerated > 0) {
                this.acceptanceRate = (double) linesApplied / linesGenerated;
            }
            
            updateMetrics();
            System.out.println("代码生成记录: 生成行数=" + linesGenerated + 
                             ", 应用行数=" + linesApplied + 
                             ", 会话时长=" + sessionDuration + "ms");
        } catch (Exception e) {
            System.err.println("记录代码生成指标时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 记录AI响应指标
     */
    public void recordAIResponse(int responseLength, int codeLength) {
        try {
            this.responseLength = responseLength;
            this.codeLength = codeLength;
            
            // 计算代码密度
            if (responseLength > 0) {
                this.codeDensity = (double) codeLength / responseLength;
            }
            
            updateMetrics();
            System.out.println("AI响应记录: 响应长度=" + responseLength + 
                             ", 代码长度=" + codeLength + 
                             ", 代码密度=" + String.format("%.2f", codeDensity));
        } catch (Exception e) {
            System.err.println("记录AI响应指标时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 更新指标数据
     */
    private void updateMetrics() {
        this.updatedAt = LocalDateTime.now();
        this.endTime = Instant.now().toEpochMilli();
        if (this.startTime > 0) {
            this.responseTime = this.endTime - this.startTime;
        }
    }
    
    /**
     * 获取扩展数据作为JSON字符串
     */
    public String getAdditionalDataAsJson() {
        try {
            if (additionalData == null || additionalData.isEmpty()) {
                return "{}";
            }
            // 简单的JSON格式转换
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : additionalData.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue() != null ? entry.getValue().toString() : "").append("\"");
                first = false;
            }
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
    

}