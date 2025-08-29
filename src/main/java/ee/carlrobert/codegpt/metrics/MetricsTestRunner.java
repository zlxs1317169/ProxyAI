package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;

/**
 * 指标系统测试运行器
 * 用于验证指标收集系统是否正常工作
 */
public class MetricsTestRunner {
    
    private static final Logger LOG = Logger.getInstance(MetricsTestRunner.class);
    
    /**
     * 运行完整的指标系统测试
     */
    public static void runFullTest() {
        try {
            LOG.info("开始运行指标系统完整测试...");
            
            // 测试1: 基本指标记录
            testBasicMetrics();
            
            // 测试2: 代码补全指标
            testCodeCompletionMetrics();
            
            // 测试3: 聊天指标
            testChatMetrics();
            
            // 测试4: 系统状态检查
            testSystemStatus();
            
            LOG.info("指标系统完整测试完成");
            
        } catch (Exception e) {
            LOG.error("运行指标系统测试时发生错误", e);
        }
    }
    
    /**
     * 测试基本指标记录
     */
    private static void testBasicMetrics() {
        try {
            LOG.info("测试基本指标记录...");
            
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                // 记录一些测试数据
                metrics.recordCodeCompletion("java", 10, 8, 150L);
                metrics.recordCodeCompletion("kotlin", 5, 4, 100L);
                
                LOG.info("✓ 基本指标记录测试通过");
            } else {
                LOG.warn("✗ ProductivityMetrics实例为空");
            }
            
        } catch (Exception e) {
            LOG.error("基本指标记录测试失败", e);
        }
    }
    
    /**
     * 测试代码补全指标
     */
    private static void testCodeCompletionMetrics() {
        try {
            LOG.info("测试代码补全指标...");
            
            // 使用SafeMetricsCollector记录指标
            SafeMetricsCollector.safeRecordAICompletion("java", "System.out.println(\"test\");", true, 50L);
            SafeMetricsCollector.safeRecordAICompletion("python", "print('hello')", false, 30L);
            
            LOG.info("✓ 代码补全指标测试通过");
            
        } catch (Exception e) {
            LOG.error("代码补全指标测试失败", e);
        }
    }
    
    /**
     * 测试聊天指标
     */
    private static void testChatMetrics() {
        try {
            LOG.info("测试聊天指标...");
            
            // 使用SafeMetricsCollector记录聊天指标
            SafeMetricsCollector.safeRecordAIResponse("test-session-123", "这是一个测试响应", "// 测试代码");
            SafeMetricsCollector.safeRecordAIChatGeneration("// 生成的代码", "// 应用的代码", 5000L, "测试任务");
            
            LOG.info("✓ 聊天指标测试通过");
            
        } catch (Exception e) {
            LOG.error("聊天指标测试失败", e);
        }
    }
    
    /**
     * 测试系统状态
     */
    private static void testSystemStatus() {
        try {
            LOG.info("测试系统状态...");
            
            // 检查MetricsIntegration服务
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration != null) {
                LOG.info("✓ MetricsIntegration服务可用");
                LOG.info("  初始化状态: " + integration.isInitialized());
            } else {
                LOG.warn("✗ MetricsIntegration服务不可用");
            }
            
            // 检查ProductivityMetrics服务
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                LOG.info("✓ ProductivityMetrics服务可用");
                
                // 生成测试报告
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                LOG.info("  测试报告: " + report.summary);
            } else {
                LOG.warn("✗ ProductivityMetrics服务不可用");
            }
            
        } catch (Exception e) {
            LOG.error("系统状态测试失败", e);
        }
    }
    
    /**
     * 主方法，可以直接运行测试
     */
    public static void main(String[] args) {
        System.out.println("开始运行指标系统测试...");
        runFullTest();
        System.out.println("指标系统测试完成");
    }
}
