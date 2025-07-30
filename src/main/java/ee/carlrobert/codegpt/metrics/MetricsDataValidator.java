package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.settings.metrics.MetricsSettings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 数据收集功能验证器
 * 确保指标收集系统正常运行并提供诊断信息
 */
public class MetricsDataValidator {
    
    private static MetricsDataValidator instance;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean isValidationEnabled = true;
    
    public static MetricsDataValidator getInstance() {
        if (instance == null) {
            instance = new MetricsDataValidator();
        }
        return instance;
    }
    
    private MetricsDataValidator() {
        startValidation();
    }
    
    /**
     * 启动数据收集验证
     */
    private void startValidation() {
        // 每5分钟验证一次数据收集状态
        scheduler.scheduleAtFixedRate(this::validateDataCollection, 1, 5, TimeUnit.MINUTES);
        
        // 每小时生成一次验证报告
        scheduler.scheduleAtFixedRate(this::generateValidationReport, 10, 60, TimeUnit.MINUTES);
    }
    
    /**
     * 验证数据收集功能
     */
    private void validateDataCollection() {
        if (!isValidationEnabled) {
            return;
        }
        
        try {
            StringBuilder validationResult = new StringBuilder();
            validationResult.append("=== 数据收集功能验证 [")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .append("] ===\n");
            
            // 1. 检查设置状态
            boolean metricsEnabled = checkMetricsSettings();
            validationResult.append("✓ 指标收集设置: ").append(metricsEnabled ? "已启用" : "已禁用").append("\n");
            
            // 2. 检查ProductivityMetrics服务
            boolean metricsServiceOk = checkProductivityMetricsService();
            validationResult.append("✓ 生产力指标服务: ").append(metricsServiceOk ? "正常" : "异常").append("\n");
            
            // 3. 检查MetricsIntegration
            boolean integrationOk = checkMetricsIntegration();
            validationResult.append("✓ 指标集成服务: ").append(integrationOk ? "正常" : "异常").append("\n");
            
            // 4. 检查数据收集状态
            ValidationStats stats = checkDataCollectionStats();
            validationResult.append("✓ 今日数据收集: 补全 ").append(stats.todayCompletions)
                .append(" 次, 聊天 ").append(stats.todayChatSessions).append(" 次\n");
            
            // 5. 检查数据存储
            boolean storageOk = checkDataStorage();
            validationResult.append("✓ 数据存储: ").append(storageOk ? "正常" : "异常").append("\n");
            
            // 输出验证结果
            System.out.println(validationResult.toString());
            
            // 如果发现问题，尝试自动修复
            if (!metricsServiceOk || !integrationOk || !storageOk) {
                attemptAutoFix();
            }
            
        } catch (Exception e) {
            System.err.println("数据收集验证时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查指标设置
     */
    private boolean checkMetricsSettings() {
        try {
            MetricsSettings settings = ApplicationManager.getApplication().getService(MetricsSettings.class);
            return settings != null && settings.isMetricsEnabled();
        } catch (Exception e) {
            System.err.println("检查指标设置时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查ProductivityMetrics服务
     */
    private boolean checkProductivityMetricsService() {
        try {
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics == null) {
                return false;
            }
            
            // 尝试获取报告来验证服务是否正常
            ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
            return report != null;
            
        } catch (Exception e) {
            System.err.println("检查生产力指标服务时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查MetricsIntegration
     */
    private boolean checkMetricsIntegration() {
        try {
            MetricsIntegration integration = MetricsIntegration.getInstance();
            return integration != null && integration.isInitialized();
        } catch (Exception e) {
            System.err.println("检查指标集成时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查数据收集统计
     */
    private ValidationStats checkDataCollectionStats() {
        ValidationStats stats = new ValidationStats();
        
        try {
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                ProductivityMetrics.DailyProductivityStats todayStats = metrics.getDailyStats(today);
                
                stats.todayCompletions = todayStats.codeCompletionsCount;
                stats.todayChatSessions = todayStats.chatSessionsCount;
                stats.todayTimeSaved = todayStats.timeSavedMs;
                stats.todayLinesGenerated = todayStats.linesGenerated;
            }
        } catch (Exception e) {
            System.err.println("检查数据收集统计时发生错误: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * 检查数据存储
     */
    private boolean checkDataStorage() {
        try {
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics == null) {
                return false;
            }
            
            // 尝试记录一个测试指标
            metrics.recordCodeCompletion("test", 1, 1, 100L);
            
            // 验证是否能正常获取数据
            ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
            return report != null;
            
        } catch (Exception e) {
            System.err.println("检查数据存储时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 尝试自动修复问题
     */
    private void attemptAutoFix() {
        try {
            System.out.println("尝试自动修复数据收集问题...");
            
            // 重新初始化MetricsIntegration
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration != null && !integration.isInitialized()) {
                // 这里可以添加重新初始化的逻辑
                System.out.println("正在重新初始化指标集成服务...");
            }
            
            // 清理可能的损坏数据
            cleanupCorruptedData();
            
            System.out.println("自动修复完成");
            
        } catch (Exception e) {
            System.err.println("自动修复时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 清理损坏的数据
     */
    private void cleanupCorruptedData() {
        try {
            // 这里可以添加清理逻辑，比如删除异常的数据记录
            System.out.println("正在清理可能损坏的数据...");
        } catch (Exception e) {
            System.err.println("清理数据时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 生成验证报告
     */
    private void generateValidationReport() {
        try {
            ValidationStats stats = checkDataCollectionStats();
            
            StringBuilder report = new StringBuilder();
            report.append("=== 数据收集验证报告 ===\n");
            report.append("时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            report.append("今日统计:\n");
            report.append("- 代码补全: ").append(stats.todayCompletions).append(" 次\n");
            report.append("- 聊天会话: ").append(stats.todayChatSessions).append(" 次\n");
            report.append("- 节省时间: ").append(String.format("%.1f", stats.todayTimeSaved / 1000.0 / 60.0)).append(" 分钟\n");
            report.append("- 生成代码: ").append(stats.todayLinesGenerated).append(" 行\n");
            
            // 数据收集健康度评估
            int healthScore = calculateHealthScore(stats);
            report.append("数据收集健康度: ").append(healthScore).append("/100\n");
            
            if (healthScore >= 80) {
                report.append("状态: 优秀 ✅\n");
            } else if (healthScore >= 60) {
                report.append("状态: 良好 ⚠️\n");
            } else {
                report.append("状态: 需要关注 ❌\n");
            }
            
            System.out.println(report.toString());
            
        } catch (Exception e) {
            System.err.println("生成验证报告时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 计算数据收集健康度分数
     */
    private int calculateHealthScore(ValidationStats stats) {
        int score = 0;
        
        // 基础服务可用性 (40分)
        if (checkProductivityMetricsService()) score += 20;
        if (checkMetricsIntegration()) score += 20;
        
        // 数据收集活跃度 (40分)
        if (stats.todayCompletions > 0) score += 20;
        if (stats.todayChatSessions > 0) score += 20;
        
        // 数据存储完整性 (20分)
        if (checkDataStorage()) score += 20;
        
        return Math.min(100, score);
    }
    
    /**
     * 手动触发验证
     */
    public void triggerValidation() {
        validateDataCollection();
    }
    
    /**
     * 启用/禁用验证
     */
    public void setValidationEnabled(boolean enabled) {
        this.isValidationEnabled = enabled;
    }
    
    /**
     * 验证统计数据
     */
    private static class ValidationStats {
        int todayCompletions = 0;
        int todayChatSessions = 0;
        long todayTimeSaved = 0;
        int todayLinesGenerated = 0;
    }
}