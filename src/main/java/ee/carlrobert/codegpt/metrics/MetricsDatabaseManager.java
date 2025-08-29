package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;
import ee.carlrobert.codegpt.metrics.config.MetricsDatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 指标数据库管理器
 * 负责将指标数据存储到MySQL数据库
 */
public class MetricsDatabaseManager {
    private static final Logger LOG = Logger.getInstance(MetricsDatabaseManager.class);
    private static volatile MetricsDatabaseManager instance;
    private static final Object lock = new Object();
    
    private MetricsDatabaseManager() {}
    
    public static MetricsDatabaseManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new MetricsDatabaseManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 保存指标数据到数据库
     */
    public void saveMetrics(ProductivityMetrics metrics) {
        if (metrics == null) {
            return;
        }
        
        try {
            // 获取数据库配置
            MetricsDatabaseConfig dbConfig = MetricsDatabaseConfig.getInstance();
            if (!dbConfig.isValid()) {
                LOG.warn("数据库配置无效，无法保存指标数据");
                return;
            }
            
            // 连接数据库
            try (Connection conn = DriverManager.getConnection(
                    dbConfig.getDbUrl(), 
                    dbConfig.getDbUser(), 
                    dbConfig.getDbPassword())) {
                
                // 准备SQL语句
                String sql = """
                    INSERT INTO productivity_metrics (
                        id, action_id, action_type, model_name, session_id, user_id,
                        start_time, end_time, response_time, processing_time,
                        input_token_count, output_token_count, total_token_count, token_cost,
                        lines_generated, lines_accepted, lines_rejected, acceptance_rate,
                        successful, error_message, retry_count, quality_score,
                        programming_language, project_type, file_extension, context_size,
                        user_experience, user_rating, feedback,
                        memory_usage, cpu_usage, network_latency,
                        session_duration, response_length, code_length, code_density,
                        concepts_learned, learning_efficiency,
                        additional_data, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    int paramIndex = 1;
                    
                    // 基础信息
                    pstmt.setString(paramIndex++, metrics.getId());
                    pstmt.setString(paramIndex++, metrics.getActionId());
                    pstmt.setString(paramIndex++, metrics.getActionType());
                    pstmt.setString(paramIndex++, metrics.getModelName());
                    pstmt.setString(paramIndex++, metrics.getSessionId());
                    pstmt.setString(paramIndex++, metrics.getUserId());
                    
                    // 时间指标
                    pstmt.setLong(paramIndex++, metrics.getStartTime());
                    pstmt.setLong(paramIndex++, metrics.getEndTime());
                    pstmt.setLong(paramIndex++, metrics.getResponseTime());
                    pstmt.setLong(paramIndex++, metrics.getProcessingTime());
                    
                    // AI交互指标
                    pstmt.setInt(paramIndex++, metrics.getInputTokenCount());
                    pstmt.setInt(paramIndex++, metrics.getOutputTokenCount());
                    pstmt.setInt(paramIndex++, metrics.getTotalTokenCount());
                    pstmt.setDouble(paramIndex++, metrics.getTokenCost());
                    
                    // 代码质量指标
                    pstmt.setInt(paramIndex++, metrics.getLinesGenerated());
                    pstmt.setInt(paramIndex++, metrics.getLinesAccepted());
                    pstmt.setInt(paramIndex++, metrics.getLinesRejected());
                    pstmt.setDouble(paramIndex++, metrics.getAcceptanceRate());
                    
                    // 效能指标
                    pstmt.setBoolean(paramIndex++, metrics.isSuccessful());
                    pstmt.setString(paramIndex++, metrics.getErrorMessage());
                    pstmt.setInt(paramIndex++, metrics.getRetryCount());
                    pstmt.setString(paramIndex++, metrics.getQualityScore());
                    
                    // 上下文信息
                    pstmt.setString(paramIndex++, metrics.getProgrammingLanguage());
                    pstmt.setString(paramIndex++, metrics.getProjectType());
                    pstmt.setString(paramIndex++, metrics.getFileExtension());
                    pstmt.setString(paramIndex++, metrics.getContextSize());
                    
                    // 用户行为指标
                    pstmt.setString(paramIndex++, metrics.getUserExperience());
                    pstmt.setInt(paramIndex++, metrics.getUserRating());
                    pstmt.setString(paramIndex++, metrics.getFeedback());
                    
                    // 系统性能指标
                    pstmt.setLong(paramIndex++, metrics.getMemoryUsage());
                    pstmt.setDouble(paramIndex++, metrics.getCpuUsage());
                    pstmt.setString(paramIndex++, metrics.getNetworkLatency());
                    
                    // 新增指标字段
                    pstmt.setLong(paramIndex++, metrics.getSessionDuration());
                    pstmt.setInt(paramIndex++, metrics.getResponseLength());
                    pstmt.setInt(paramIndex++, metrics.getCodeLength());
                    pstmt.setDouble(paramIndex++, metrics.getCodeDensity());
                    pstmt.setInt(paramIndex++, metrics.getConceptsLearned());
                    pstmt.setDouble(paramIndex++, metrics.getLearningEfficiency());
                    
                    // 扩展数据
                    pstmt.setString(paramIndex++, metrics.getAdditionalDataAsJson());
                    
                    // 时间戳
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    pstmt.setTimestamp(paramIndex++, now);
                    pstmt.setTimestamp(paramIndex++, now);
                    
                    // 执行插入
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        LOG.info("指标数据成功保存到数据库: " + metrics.getActionId());
                    } else {
                        LOG.warn("指标数据保存失败，没有行被影响: " + metrics.getActionId());
                    }
                }
            }
            
        } catch (Exception e) {
            LOG.error("保存指标数据到数据库失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清除所有指标数据
     */
    public void clearAllMetrics() {
        try {
            MetricsDatabaseConfig dbConfig = MetricsDatabaseConfig.getInstance();
            if (!dbConfig.isValid()) {
                LOG.warn("数据库配置无效，无法清除指标数据");
                return;
            }
            
            try (Connection conn = DriverManager.getConnection(
                    dbConfig.getDbUrl(), 
                    dbConfig.getDbUser(), 
                    dbConfig.getDbPassword())) {
                
                try (Statement stmt = conn.createStatement()) {
                    int rowsAffected = stmt.executeUpdate("DELETE FROM productivity_metrics");
                    LOG.info("成功清除 " + rowsAffected + " 条指标数据");
                }
            }
            
        } catch (Exception e) {
            LOG.error("清除指标数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取指标数据总数
     */
    public int getMetricsCount() {
        try {
            MetricsDatabaseConfig dbConfig = MetricsDatabaseConfig.getInstance();
            if (!dbConfig.isValid()) {
                return 0;
            }
            
            try (Connection conn = DriverManager.getConnection(
                    dbConfig.getDbUrl(), 
                    dbConfig.getDbUser(), 
                    dbConfig.getDbPassword())) {
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM productivity_metrics")) {
                    
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            
        } catch (Exception e) {
            LOG.error("获取指标数据总数失败: " + e.getMessage(), e);
        }
        return 0;
    }
    
    /**
     * 获取代码补全接受率
     */
    public double getCodeCompletionAcceptanceRate() {
        try {
            MetricsDatabaseConfig dbConfig = MetricsDatabaseConfig.getInstance();
            if (!dbConfig.isValid()) {
                return 0.0;
            }
            
            try (Connection conn = DriverManager.getConnection(
                    dbConfig.getDbUrl(), 
                    dbConfig.getDbUser(), 
                    dbConfig.getDbPassword())) {
                
                String sql = """
                    SELECT 
                        CASE 
                            WHEN SUM(lines_generated) > 0 
                            THEN SUM(lines_accepted) * 100.0 / SUM(lines_generated)
                            ELSE 0 
                        END as acceptance_rate
                    FROM productivity_metrics 
                    WHERE action_type = 'CODE_COMPLETION'
                    """;
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    if (rs.next()) {
                        return rs.getDouble("acceptance_rate");
                    }
                }
            }
            
        } catch (Exception e) {
            LOG.error("获取代码补全接受率失败: " + e.getMessage(), e);
        }
        return 0.0;
    }
    
    /**
     * 获取平均响应时间
     */
    public double getAverageResponseTime() {
        try {
            MetricsDatabaseConfig dbConfig = MetricsDatabaseConfig.getInstance();
            if (!dbConfig.isValid()) {
                return 0.0;
            }
            
            try (Connection conn = DriverManager.getConnection(
                    dbConfig.getDbUrl(), 
                    dbConfig.getDbUser(), 
                    dbConfig.getDbPassword())) {
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT AVG(response_time) FROM productivity_metrics")) {
                    
                    if (rs.next()) {
                        return rs.getDouble(1);
                    }
                }
            }
            
        } catch (Exception e) {
            LOG.error("获取平均响应时间失败: " + e.getMessage(), e);
        }
        return 0.0;
    }
}
