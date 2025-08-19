package ee.carlrobert.codegpt.metrics;

import java.util.concurrent.CompletableFuture;

/**
 * 效能度量服务接口
 * 为Kotlin代码提供统一的API接口
 */
public interface MetricsService {
    
    /**
     * 记录代码补全效能指标
     * 
     * @param language 编程语言
     * @param suggestedCode 建议的代码
     * @param accepted 是否被接受
     * @param processingTime 处理时间（毫秒）
     */
    void recordCodeCompletionMetrics(String language, String suggestedCode, boolean accepted, long processingTime);
    
    /**
     * 记录聊天代码生成效能指标
     * 
     * @param generatedCode 生成的代码
     * @param applied 是否被应用
     * @param processingTime 处理时间（毫秒）
     * @param sessionId 会话ID
     */
    void recordChatCodeGenerationMetrics(String generatedCode, boolean applied, long processingTime, String sessionId);
    
    /**
     * 记录用户评分
     * 
     * @param actionId 操作ID
     * @param rating 评分（1-5）
     * @param feedback 反馈信息
     */
    void recordUserRating(String actionId, int rating, String feedback);
    
    /**
     * 生成效能分析报告
     * 
     * @return 异步报告生成任务
     */
    CompletableFuture<ProductivityReport> generateProductivityReport();
    
    /**
     * 获取最新报告
     * 
     * @return 最新的效能报告，如果没有则为null
     */
    ProductivityReport getLastReport();
    
    /**
     * 获取最后分析时间
     * 
     * @return 最后分析时间
     */
    java.time.LocalDateTime getLastAnalysisTime();
    
    /**
     * 导出效能数据
     * 
     * @param format 导出格式（json, csv, summary）
     * @return 导出的数据内容
     */
    String exportMetricsData(String format);
    
    /**
     * 清理旧数据
     * 
     * @param daysToKeep 保留天数
     */
    void cleanupOldData(int daysToKeep);
    
    /**
     * 检查服务是否可用
     * 
     * @return 服务是否可用
     */
    boolean isServiceAvailable();
}
