package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;

/**
 * 简单的指标测试类
 * 用于验证基本的指标收集功能是否正常
 */
public class SimpleMetricsTest {
    
    private static final Logger LOG = Logger.getInstance(SimpleMetricsTest.class);
    
    /**
     * 测试基本的指标收集功能
     */
    public static void testBasicMetrics() {
        try {
            LOG.info("开始测试基本指标收集功能");
            
            // 测试ProductivityMetrics初始化
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                LOG.info("✓ ProductivityMetrics 初始化成功");
                
                // 测试基本的指标记录
                metrics.recordCodeCompletion("java", 5, 3, 100L);
                LOG.info("✓ 代码补全指标记录成功");
                
                // 测试报告生成
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                LOG.info("✓ 指标报告生成成功: 总行数=" + report.totalLinesGenerated);
                
            } else {
                LOG.warn("✗ ProductivityMetrics 初始化失败");
            }
            
            // 测试MetricsIntegration
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration != null) {
                LOG.info("✓ MetricsIntegration 获取成功");
            } else {
                LOG.warn("✗ MetricsIntegration 获取失败");
            }
            
            LOG.info("基本指标收集功能测试完成");
            
        } catch (Exception e) {
            LOG.error("测试基本指标收集功能时发生错误", e);
        }
    }
    
    /**
     * 测试安全指标收集器
     */
    public static void testSafeMetricsCollector() {
        try {
            LOG.info("开始测试安全指标收集器");
            
            // 测试安全的AI补全记录
            SafeMetricsCollector.safeRecordAICompletion("java", "System.out.println(\"test\");", true, 50L);
            LOG.info("✓ 安全AI补全记录测试成功");
            
            // 测试安全的聊天会话开始
            SafeMetricsCollector.safeStartChatSession("test-session-123", "test");
            LOG.info("✓ 安全聊天会话开始测试成功");
            
            // 测试安全的AI响应记录
            SafeMetricsCollector.safeRecordAIResponse("test-session-123", "测试响应", "// 测试代码");
            LOG.info("✓ 安全AI响应记录测试成功");
            
            LOG.info("安全指标收集器测试完成");
            
        } catch (Exception e) {
            LOG.error("测试安全指标收集器时发生错误", e);
        }
    }
}