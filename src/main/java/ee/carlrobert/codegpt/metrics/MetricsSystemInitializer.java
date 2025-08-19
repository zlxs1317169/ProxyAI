package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import ee.carlrobert.codegpt.metrics.export.MetricsExporter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * 指标系统初始化器，在项目启动时初始化指标收集系统
 * 专门针对软件工程师使用ProxyAI的效能度量进行优化
 */
public class MetricsSystemInitializer implements StartupActivity {
    private static final Logger LOG = Logger.getInstance(MetricsSystemInitializer.class);
    private static final String DB_URL = "jdbc:h2:./proxyai_metrics";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    @Override
    public void runActivity(@NotNull Project project) {
        try {
            // 初始化数据库
            initializeDatabase();
            
            // 初始化指标收集器
            MetricsCollector.getInstance(project);
            
            LOG.info("ProxyAI软件工程师效能度量系统初始化完成");
        } catch (Exception e) {
            LOG.error("ProxyAI软件工程师效能度量系统初始化失败", e);
        }
    }

    private void initializeDatabase() {
        try {
            // 显式加载H2数据库驱动
            LOG.info("正在加载H2数据库驱动...");
            Class.forName("org.h2.Driver");
            LOG.info("H2数据库驱动加载成功");
            
            // 验证驱动是否可用
            if (!isDriverAvailable()) {
                throw new RuntimeException("H2数据库驱动不可用，请检查依赖配置");
            }
            
            LOG.info("正在连接数据库: " + DB_URL);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement()) {
                
                LOG.info("数据库连接成功，开始创建表结构...");
                
                // 创建主指标表 - 存储所有效能度量数据
                String createMainTableSQL = """
                    CREATE TABLE IF NOT EXISTS productivity_metrics (
                        id VARCHAR(255) PRIMARY KEY,
                        action_id VARCHAR(255),
                        action_type VARCHAR(100),
                        model_name VARCHAR(100),
                        session_id VARCHAR(255),
                        user_id VARCHAR(255),
                        
                        -- 时间相关指标
                        start_time BIGINT,
                        end_time BIGINT,
                        response_time BIGINT,
                        processing_time BIGINT,
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,
                        
                        -- AI交互指标
                        input_token_count INT,
                        output_token_count INT,
                        total_token_count INT,
                        token_cost DOUBLE,
                        
                        -- 代码质量指标
                        lines_generated INT,
                        lines_accepted INT,
                        lines_rejected INT,
                        acceptance_rate DOUBLE,
                        
                        -- 效能指标
                        successful BOOLEAN,
                        error_message TEXT,
                        retry_count INT,
                        quality_score VARCHAR(50),
                        
                        -- 上下文信息
                        programming_language VARCHAR(100),
                        project_type VARCHAR(100),
                        file_extension VARCHAR(20),
                        context_size VARCHAR(50),
                        
                        -- 用户行为指标
                        user_experience VARCHAR(100),
                        user_rating INT,
                        feedback TEXT,
                        
                        -- 系统性能指标
                        memory_usage BIGINT,
                        cpu_usage DOUBLE,
                        network_latency VARCHAR(50),
                        
                        -- 扩展数据
                        additional_data TEXT
                    )
                    """;
                
                stmt.execute(createMainTableSQL);
                LOG.info("主指标表创建成功");
                
                // 创建效能分析表 - 存储分析结果
                String createAnalysisTableSQL = """
                    CREATE TABLE IF NOT EXISTS productivity_analysis (
                        id VARCHAR(255) PRIMARY KEY,
                        analysis_time TIMESTAMP,
                        total_metrics INT,
                        overall_score DOUBLE,
                        
                        -- 分类指标评分
                        code_generation_score DOUBLE,
                        code_quality_score DOUBLE,
                        development_speed_score DOUBLE,
                        problem_solving_score DOUBLE,
                        learning_curve_score DOUBLE,
                        collaboration_score DOUBLE,
                        innovation_score DOUBLE,
                        cost_effectiveness_score DOUBLE,
                        user_experience_score DOUBLE,
                        system_performance_score DOUBLE,
                        
                        -- 基础统计
                        success_rate DOUBLE,
                        average_response_time DOUBLE,
                        average_acceptance_rate DOUBLE,
                        total_token_usage BIGINT,
                        total_lines_generated INT,
                        
                        -- 趋势信息
                        overall_trend VARCHAR(50),
                        trend_strength DOUBLE,
                        
                        -- 改进建议
                        improvement_suggestions TEXT,
                        
                        -- 元数据
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """;
                
                stmt.execute(createAnalysisTableSQL);
                LOG.info("效能分析表创建成功");
                
                // 创建每日趋势表 - 存储每日效能趋势数据
                String createDailyTrendsTableSQL = """
                    CREATE TABLE IF NOT EXISTS daily_trends (
                        id VARCHAR(255) PRIMARY KEY,
                        date VARCHAR(20),
                        efficiency_score DOUBLE,
                        metrics_count INT,
                        success_rate DOUBLE,
                        average_response_time DOUBLE,
                        average_acceptance_rate DOUBLE,
                        total_token_usage BIGINT,
                        total_lines_generated INT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """;
                
                stmt.execute(createDailyTrendsTableSQL);
                LOG.info("每日趋势表创建成功");
                
                // 创建用户会话表 - 跟踪用户使用会话
                String createUserSessionsTableSQL = """
                    CREATE TABLE IF NOT EXISTS user_sessions (
                        session_id VARCHAR(255) PRIMARY KEY,
                        user_id VARCHAR(255),
                        session_type VARCHAR(100),
                        start_time TIMESTAMP,
                        end_time TIMESTAMP,
                        duration_minutes BIGINT,
                        total_actions INT,
                        successful_actions INT,
                        total_tokens BIGINT,
                        total_cost DOUBLE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """;
                
                stmt.execute(createUserSessionsTableSQL);
                LOG.info("用户会话表创建成功");
                
                // 创建模型使用统计表 - 跟踪不同AI模型的使用情况
                String createModelUsageTableSQL = """
                    CREATE TABLE IF NOT EXISTS model_usage_stats (
                        id VARCHAR(255) PRIMARY KEY,
                        model_name VARCHAR(100),
                        usage_count INT,
                        total_tokens BIGINT,
                        total_cost DOUBLE,
                        average_response_time DOUBLE,
                        success_rate DOUBLE,
                        average_acceptance_rate DOUBLE,
                        last_used TIMESTAMP,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """;
                
                stmt.execute(createModelUsageTableSQL);
                LOG.info("模型使用统计表创建成功");
                
                // 创建编程语言统计表 - 跟踪不同编程语言的效能表现
                String createLanguageStatsTableSQL = """
                    CREATE TABLE IF NOT EXISTS language_stats (
                        id VARCHAR(255) PRIMARY KEY,
                        programming_language VARCHAR(100),
                        usage_count INT,
                        total_lines_generated INT,
                        average_acceptance_rate DOUBLE,
                        average_response_time DOUBLE,
                        success_rate DOUBLE,
                        last_used TIMESTAMP,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """;
                
                stmt.execute(createLanguageStatsTableSQL);
                LOG.info("编程语言统计表创建成功");
                
                // 创建索引以提高查询性能
                createIndexes(stmt);
                
                LOG.info("软件工程师效能度量数据库初始化完成");
                
            } catch (Exception e) {
                LOG.error("数据库连接或表创建失败: " + e.getMessage(), e);
                throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
            }
        } catch (ClassNotFoundException e) {
            LOG.error("H2数据库驱动类未找到，请确保已添加H2依赖: " + e.getMessage(), e);
            throw new RuntimeException("H2数据库驱动未找到，请检查项目依赖配置", e);
        } catch (Exception e) {
            LOG.error("软件工程师效能度量数据库初始化失败: " + e.getMessage(), e);
            throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
        }
    }
    
    private void createIndexes(Statement stmt) throws Exception {
        // 主指标表索引
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_metrics_action_type ON productivity_metrics(action_type)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_metrics_start_time ON productivity_metrics(start_time)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_metrics_session_id ON productivity_metrics(session_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_metrics_user_id ON productivity_metrics(user_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_metrics_model_name ON productivity_metrics(model_name)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_metrics_programming_language ON productivity_metrics(programming_language)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_metrics_successful ON productivity_metrics(successful)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_metrics_created_at ON productivity_metrics(created_at)");
        
        // 分析表索引
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_analysis_time ON productivity_analysis(analysis_time)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_analysis_overall_score ON productivity_analysis(overall_score)");
        
        // 每日趋势表索引
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_daily_trends_date ON daily_trends(date)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_daily_trends_efficiency_score ON daily_trends(efficiency_score)");
        
        // 用户会话表索引
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_sessions_start_time ON user_sessions(start_time)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_sessions_session_type ON user_sessions(session_type)");
        
        // 模型使用统计表索引
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_model_usage_model_name ON model_usage_stats(model_name)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_model_usage_last_used ON model_usage_stats(last_used)");
        
        // 编程语言统计表索引
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_language_stats_language ON language_stats(programming_language)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_language_stats_last_used ON language_stats(last_used)");
        
        LOG.info("数据库索引创建完成");
    }
    
    /**
     * 检查H2数据库驱动是否可用
     */
    private boolean isDriverAvailable() {
        try {
            // 尝试获取驱动信息
            java.sql.Driver driver = DriverManager.getDriver(DB_URL);
            return driver != null;
        } catch (Exception e) {
            LOG.warn("无法验证H2驱动可用性: " + e.getMessage());
            return false;
        }
    }
}