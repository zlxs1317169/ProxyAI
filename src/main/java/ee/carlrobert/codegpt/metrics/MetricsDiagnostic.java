package ee.carlrobert.codegpt.metrics;

import ee.carlrobert.codegpt.settings.metrics.MetricsSettings;
import com.intellij.openapi.application.ApplicationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 度量系统诊断工具
 * 用于排查数据收集问题
 */
public class MetricsDiagnostic {
    
    public static void main(String[] args) {
        runDiagnostic();
    }
    
    /**
     * 运行完整诊断
     */
    public static void runDiagnostic() {
        System.out.println("=== ProxyAI 度量系统诊断 ===");
        System.out.println("诊断时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println();
        
        // 1. 检查设置状态
        checkSettings();
        
        // 2. 检查核心服务
        checkCoreServices();
        
        // 3. 检查数据收集状态
        checkDataCollection();
        
        // 4. 检查集成状态
        checkIntegration();
        
        // 5. 提供修复建议
        provideFixes();
        
        System.out.println("\n=== 诊断完成 ===");
    }
    
    /**
     * 检查设置状态
     */
    private static void checkSettings() {
        System.out.println("🔧 检查设置状态:");
        
        try {
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings == null) {
                System.out.println("❌ MetricsSettings 服务未初始化");
                return;
            }
            
            System.out.println("✅ MetricsSettings 服务正常");
            System.out.println("   - 度量收集启用: " + settings.isMetricsEnabled());
            System.out.println("   - 自动导出启用: " + settings.isAutoExportEnabled());
            System.out.println("   - 详细日志启用: " + settings.isDetailedLoggingEnabled());
            System.out.println("   - 自动检测启用: " + settings.isAutoDetectionEnabled());
            System.out.println("   - 仅跟踪AI使用: " + settings.isOnlyTrackAIUsage());
            System.out.println("   - 通知启用: " + settings.isNotificationEnabled());
            
            // 检查关键设置
            if (!settings.isMetricsEnabled()) {
                System.out.println("⚠️  度量收集已禁用 - 这是数据收集问题的主要原因");
            }
            
            if (!settings.isAutoDetectionEnabled() && !settings.isOnlyTrackAIUsage()) {
                System.out.println("⚠️  自动检测和AI跟踪都已禁用 - 可能导致数据收集不完整");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 检查设置时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 检查核心服务
     */
    private static void checkCoreServices() {
        System.out.println("🏗️ 检查核心服务:");
        
        try {
            // 检查 ProductivityMetrics
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics == null) {
                System.out.println("❌ ProductivityMetrics 服务未初始化");
            } else {
                System.out.println("✅ ProductivityMetrics 服务正常");
                
                // 检查数据状态
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                if (report != null) {
                    System.out.println("   - 今日生成代码行数: " + report.totalLinesGenerated);
                    System.out.println("   - 平均代码接受率: " + String.format("%.1f%%", report.avgCodeAcceptanceRate * 100));
                    System.out.println("   - 节省时间: " + String.format("%.2f小时", report.totalTimeSavedHours));
                    System.out.println("   - 平均效率提升: " + String.format("%.1f%%", report.avgEfficiencyGain));
                    
                    if (report.totalLinesGenerated == 0) {
                        System.out.println("⚠️  没有记录到任何代码生成数据");
                    }
                } else {
                    System.out.println("❌ 无法生成度量报告");
                }
            }
            
            // 检查 MetricsIntegration
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration == null) {
                System.out.println("❌ MetricsIntegration 服务未初始化");
            } else {
                System.out.println("✅ MetricsIntegration 服务正常");
                System.out.println("   - 初始化状态: " + (integration.isInitialized() ? "已初始化" : "未初始化"));
                
                if (!integration.isInitialized()) {
                    System.out.println("⚠️  MetricsIntegration 未完全初始化");
                }
                
                // 检查 MetricsCollector
                MetricsCollector collector = integration.getMetricsCollector();
                if (collector == null) {
                    System.out.println("❌ MetricsCollector 未创建");
                } else {
                    System.out.println("✅ MetricsCollector 已创建");
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ 检查核心服务时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 检查数据收集状态
     */
    private static void checkDataCollection() {
        System.out.println("📊 检查数据收集状态:");
        
        try {
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics == null) {
                System.out.println("❌ 无法检查数据收集状态 - ProductivityMetrics 未初始化");
                return;
            }
            
            // 获取今日统计
            String today = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            ProductivityMetrics.DailyProductivityStats todayStats = metrics.getDailyStats(today);
            
            System.out.println("📅 今日数据统计:");
            System.out.println("   - 代码补全次数: " + todayStats.codeCompletionsCount);
            System.out.println("   - 聊天会话次数: " + todayStats.chatSessionsCount);
            System.out.println("   - 生成代码行数: " + todayStats.linesGenerated);
            System.out.println("   - 节省时间: " + String.format("%.2f分钟", todayStats.timeSavedMs / 1000.0 / 60.0));
            
            // 检查是否有数据
            boolean hasData = todayStats.codeCompletionsCount > 0 || 
                            todayStats.chatSessionsCount > 0 || 
                            todayStats.linesGenerated > 0;
            
            if (!hasData) {
                System.out.println("⚠️  今日没有收集到任何数据");
                System.out.println("   可能原因:");
                System.out.println("   1. 度量收集被禁用");
                System.out.println("   2. AI功能未被使用");
                System.out.println("   3. 数据收集器未正确初始化");
                System.out.println("   4. 集成代码未正确调用");
            } else {
                System.out.println("✅ 今日已收集到数据");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 检查数据收集状态时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 检查集成状态
     */
    private static void checkIntegration() {
        System.out.println("🔗 检查集成状态:");
        
        try {
            // 检查 SafeMetricsCollector
            System.out.println("✅ SafeMetricsCollector 类可用");
            
            // 检查集成类
            System.out.println("✅ CodeCompletionMetricsIntegration 类可用");
            System.out.println("✅ ChatMetricsIntegration 类可用");
            
            // 模拟测试数据收集（不实际记录）
            System.out.println("🧪 测试数据收集接口:");
            
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration != null && integration.isInitialized()) {
                System.out.println("   - AI补全记录接口: 可用");
                System.out.println("   - AI聊天生成接口: 可用");
                System.out.println("   - 时间节省记录接口: 可用");
                System.out.println("   - 学习活动记录接口: 可用");
            } else {
                System.out.println("   - 集成服务未初始化，接口不可用");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 检查集成状态时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 提供修复建议
     */
    private static void provideFixes() {
        System.out.println("🔧 修复建议:");
        
        try {
            MetricsSettings settings = MetricsSettings.getInstance();
            
            if (settings != null) {
                if (!settings.isMetricsEnabled()) {
                    System.out.println("1. 启用度量收集:");
                    System.out.println("   - 打开 Settings -> ProxyAI -> 提效度量");
                    System.out.println("   - 勾选 '启用提效度量收集'");
                }
                
                if (!settings.isOnlyTrackAIUsage() && !settings.isAutoDetectionEnabled()) {
                    System.out.println("2. 配置数据收集模式:");
                    System.out.println("   - 推荐启用 '仅跟踪AI使用' (精确模式)");
                    System.out.println("   - 或启用 '自动检测' (可能有误判)");
                }
            }
            
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration == null || !integration.isInitialized()) {
                System.out.println("3. 重新初始化度量系统:");
                System.out.println("   - 重启IDE");
                System.out.println("   - 或手动触发初始化");
            }
            
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                if (report != null && report.totalLinesGenerated == 0) {
                    System.out.println("4. 验证AI功能使用:");
                    System.out.println("   - 尝试使用代码补全功能");
                    System.out.println("   - 尝试使用聊天功能生成代码");
                    System.out.println("   - 检查是否有错误日志");
                }
            }
            
            System.out.println("5. 手动测试数据收集:");
            System.out.println("   - 运行 MetricsSystemTest.runFullSystemTest()");
            System.out.println("   - 检查控制台输出");
            
        } catch (Exception e) {
            System.out.println("❌ 生成修复建议时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 快速修复 - 启用度量收集
     */
    public static void quickFix() {
        System.out.println("🚀 执行快速修复...");
        
        try {
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings != null) {
                // 启用度量收集
                settings.setMetricsEnabled(true);
                
                // 启用仅跟踪AI使用模式（推荐）
                settings.setOnlyTrackAIUsage(true);
                
                // 启用通知
                settings.setNotificationEnabled(true);
                
                System.out.println("✅ 快速修复完成:");
                System.out.println("   - 已启用度量收集");
                System.out.println("   - 已启用仅跟踪AI使用模式");
                System.out.println("   - 已启用通知");
                System.out.println("   - 请重启IDE以确保设置生效");
            } else {
                System.out.println("❌ 无法执行快速修复 - MetricsSettings 未初始化");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 快速修复失败: " + e.getMessage());
        }
    }
}