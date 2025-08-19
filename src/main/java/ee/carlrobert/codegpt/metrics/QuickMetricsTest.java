package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;

/**
 * 快速指标测试工具
 * 用于验证指标收集系统的基本功能
 */
public class QuickMetricsTest {
    
    private static final Logger LOG = Logger.getInstance(QuickMetricsTest.class);
    
    /**
     * 运行快速测试
     */
    public static void runQuickTest() {
        try {
            LOG.info("=== 开始快速指标测试 ===");
            
            // 1. 测试ProductivityMetrics服务
            testProductivityMetrics();
            
            // 2. 测试SafeMetricsCollector
            testSafeMetricsCollector();
            
            // 3. 测试指标设置
            testMetricsSettings();
            
            LOG.info("=== 快速指标测试完成 ===");
            
        } catch (Exception e) {
            LOG.error("快速指标测试失败", e);
        }
    }
    
    private static void testProductivityMetrics() {
        try {
            LOG.info("测试ProductivityMetrics...");
            
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                LOG.info("✓ ProductivityMetrics服务可用");
                
                // 测试基本记录功能
                metrics.recordCodeCompletion("java", 3, 2, 80L);
                LOG.info("✓ 代码补全记录测试成功");
                
                // 测试报告生成
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                if (report != null) {
                    LOG.info("✓ 报告生成成功，总生成行数: " + report.totalLinesGenerated + 
                            ", 代码接受率: " + String.format("%.2f%%", report.avgCodeAcceptanceRate * 100));
                } else {
                    LOG.warn("⚠️ 报告生成返回null");
                }
            } else {
                LOG.error("❌ ProductivityMetrics服务不可用");
            }
            
        } catch (Exception e) {
            LOG.error("ProductivityMetrics测试失败", e);
        }
    }
    
    private static void testSafeMetricsCollector() {
        try {
            LOG.info("测试SafeMetricsCollector...");
            
            // 测试安全收集器的各种方法
            SafeMetricsCollector.safeRecordAICompletion("java", "test code", true, 100L);
            LOG.info("✓ 安全AI补全记录测试成功");
            
            SafeMetricsCollector.safeStartChatSession("test-session", "test");
            LOG.info("✓ 安全聊天会话开始测试成功");
            
            SafeMetricsCollector.safeRecordAIResponse("test-session", "response", "code");
            LOG.info("✓ 安全AI响应记录测试成功");
            
        } catch (Exception e) {
            LOG.error("SafeMetricsCollector测试失败", e);
        }
    }
    
    private static void testMetricsSettings() {
        try {
            LOG.info("测试MetricsSettings...");
            
            ee.carlrobert.codegpt.settings.metrics.MetricsSettings settings = 
                ee.carlrobert.codegpt.settings.metrics.MetricsSettings.getInstance();
            
            if (settings != null) {
                LOG.info("✓ MetricsSettings服务可用");
                LOG.info("  - 指标收集启用: " + settings.isMetricsEnabled());
                LOG.info("  - 自动导出启用: " + settings.isAutoExportEnabled());
            } else {
                LOG.error("❌ MetricsSettings服务不可用");
            }
            
        } catch (Exception e) {
            LOG.error("MetricsSettings测试失败", e);
        }
    }
}