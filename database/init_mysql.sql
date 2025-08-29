-- ProxyAI 效能度量系统 MySQL 数据库初始化脚本
-- 版本: 2.0 (修复MySQL兼容性问题)
-- 创建日期: 2024年12月
-- 兼容性: MySQL 5.7+, MySQL 8.0+
-- 
-- 重要说明:
-- 1. 此脚本已修复所有MySQL兼容性问题
-- 2. 索引创建使用标准语法，兼容所有MySQL版本
-- 3. 如果索引已存在会报错，但不影响主要功能
-- 4. 建议在运行前备份现有数据库
-- 
-- 创建数据库和用户

-- 创建数据库
CREATE DATABASE IF NOT EXISTS proxyai_metrics 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER IF NOT EXISTS 'proxyai_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'proxyai_password';
CREATE USER IF NOT EXISTS 'proxyai_user'@'%' IDENTIFIED WITH mysql_native_password BY 'proxyai_password';

-- 授予权限
GRANT ALL PRIVILEGES ON proxyai_metrics.* TO 'proxyai_user'@'localhost';
GRANT ALL PRIVILEGES ON proxyai_metrics.* TO 'proxyai_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 使用数据库
USE proxyai_metrics;

-- 创建主指标表
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
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
    network_latency VARCHAR(100),
    
    -- 扩展数据
    additional_data JSON,
    
    -- 新增指标字段
    session_duration BIGINT,
    response_length INT,
    code_length INT,
    code_density DOUBLE,
    concepts_learned INT,
    learning_efficiency DOUBLE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建效能分析表
CREATE TABLE IF NOT EXISTS productivity_analysis (
    id VARCHAR(255) PRIMARY KEY,
    analysis_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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
    avg_response_time DOUBLE,
    avg_acceptance_rate DOUBLE,
    total_lines_generated INT,
    total_lines_accepted INT,
    
    -- 时间分布
    peak_hours VARCHAR(100),
    weekly_trends JSON,
    monthly_growth DOUBLE,
    
    -- 用户行为分析
    most_used_languages JSON,
    common_task_types JSON,
    user_satisfaction_trends JSON,
    
    -- 系统性能分析
    avg_memory_usage BIGINT,
    avg_cpu_usage DOUBLE,
    performance_bottlenecks JSON,
    
    -- 扩展数据
    additional_insights JSON,
    recommendations TEXT,
    next_actions TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户会话表
CREATE TABLE IF NOT EXISTS user_sessions (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255),
    session_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_end TIMESTAMP NULL,
    session_duration BIGINT,
    
    -- 会话统计
    total_actions INT DEFAULT 0,
    successful_actions INT DEFAULT 0,
    failed_actions INT DEFAULT 0,
    
    -- 会话类型
    session_type VARCHAR(100),
    project_context VARCHAR(255),
    ide_version VARCHAR(100),
    
    -- 性能指标
    avg_response_time DOUBLE,
    memory_usage_pattern JSON,
    cpu_usage_pattern JSON,
    
    -- 扩展数据
    session_metadata JSON,
    notes TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建索引以提高查询性能（兼容老版本MySQL）
-- 注意：如果索引已存在，会报错，但不影响主要功能
-- 主指标表索引
CREATE INDEX idx_metrics_action_type ON productivity_metrics(action_type);
CREATE INDEX idx_metrics_start_time ON productivity_metrics(start_time);
CREATE INDEX idx_metrics_session_id ON productivity_metrics(session_id);
CREATE INDEX idx_metrics_user_id ON productivity_metrics(user_id);
CREATE INDEX idx_metrics_model_name ON productivity_metrics(model_name);
CREATE INDEX idx_metrics_programming_language ON productivity_metrics(programming_language);
CREATE INDEX idx_metrics_successful ON productivity_metrics(successful);
CREATE INDEX idx_metrics_created_at ON productivity_metrics(created_at);

-- 分析表索引
CREATE INDEX idx_analysis_time ON productivity_analysis(analysis_time);
CREATE INDEX idx_analysis_overall_score ON productivity_analysis(overall_score);

-- 用户会话表索引
CREATE INDEX idx_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_sessions_start_time ON user_sessions(session_start);
CREATE INDEX idx_sessions_type ON user_sessions(session_type);

-- 创建每日趋势表（用于跟踪每日效能变化）
CREATE TABLE IF NOT EXISTS daily_trends (
    id VARCHAR(255) PRIMARY KEY,
    date DATE,
    total_actions INT,
    successful_actions INT,
    failed_actions INT,
    efficiency_score DOUBLE,
    average_response_time DOUBLE,
    total_lines_generated INT,
    total_lines_accepted INT,
    most_used_language VARCHAR(100),
    peak_hours VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建模型使用统计表（跟踪不同AI模型的使用情况）
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建编程语言统计表（跟踪不同编程语言的效能表现）
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 为新增表创建索引
-- 每日趋势表索引
CREATE INDEX idx_daily_trends_date ON daily_trends(date);
CREATE INDEX idx_daily_trends_efficiency_score ON daily_trends(efficiency_score);

-- 模型使用统计表索引
CREATE INDEX idx_model_usage_model_name ON model_usage_stats(model_name);
CREATE INDEX idx_model_usage_last_used ON model_usage_stats(last_used);

-- 编程语言统计表索引
CREATE INDEX idx_language_stats_language ON language_stats(programming_language);
CREATE INDEX idx_language_stats_last_used ON language_stats(last_used);

-- 插入一些示例数据用于测试
INSERT INTO productivity_metrics (
    id, action_id, action_type, model_name, session_id, user_id,
    start_time, end_time, response_time, processing_time,
    lines_generated, lines_accepted, acceptance_rate, successful,
    programming_language, created_at
) VALUES (
    'test-metric-001', 'test-action-001', 'CODE_COMPLETION', 'gpt-4', 'test-session-001', 'test-user-001',
    UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000 + 150, 150, 100,
    10, 8, 0.8, true,
    'java', NOW()
) ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 显示创建的表
SHOW TABLES;

-- 显示表结构
DESCRIBE productivity_metrics;
DESCRIBE productivity_analysis;
DESCRIBE user_sessions;
DESCRIBE daily_trends;
DESCRIBE model_usage_stats;
DESCRIBE language_stats;

-- 显示用户权限
SHOW GRANTS FOR 'proxyai_user'@'localhost';

-- 显示索引信息
SHOW INDEX FROM productivity_metrics;
SHOW INDEX FROM productivity_analysis;
SHOW INDEX FROM user_sessions;
SHOW INDEX FROM daily_trends;
SHOW INDEX FROM model_usage_stats;
SHOW INDEX FROM language_stats;

-- ========================================
-- 使用说明和故障排除
-- ========================================
-- 
-- 1. 数据库连接测试
--    使用以下命令测试连接:
--    mysql -u proxyai_user -p proxyai_metrics
-- 
-- 2. 如果遇到索引创建错误
--    错误信息: "Duplicate key name 'idx_xxx'"
--    解决方案: 这是正常的，表示索引已存在，可以忽略
-- 
-- 3. 验证数据表创建
--    运行: SELECT COUNT(*) FROM productivity_metrics;
--    应该返回: 1 (测试数据)
-- 
-- 4. 检查表结构
--    运行: SHOW TABLES;
--    应该显示: 6个表
-- 
-- 5. 常见问题解决
--    - 如果用户创建失败: 检查MySQL版本和权限
--    - 如果表创建失败: 检查数据库是否存在
--    - 如果索引创建失败: 检查表是否已存在
-- 
-- 6. 性能优化建议
--    - 定期清理旧数据: DELETE FROM productivity_metrics WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
--    - 定期分析表: ANALYZE TABLE productivity_metrics;
--    - 监控表大小: SELECT table_name, ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)' FROM information_schema.tables WHERE table_schema = 'proxyai_metrics';
-- 
-- 脚本执行完成！
-- 现在可以启动ProxyAI插件，指标系统将自动开始收集数据
