package ee.carlrobert.codegpt.metrics;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

/**
 * 指标收集验证器
 * 用于验证指标收集系统是否正常工作
 */
public class MetricsCollectionValidator {
    
    private static final Logger LOG = Logger.getInstance(MetricsCollectionValidator.class);
    
    /**
     * 验证指标收集系统状态
     */
    public static void validateMetricsCollection(Project project) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                StringBuilder status = new StringBuilder();
                status.append("ProxyAI 指标收集系统状态检查:\n\n");
                
                // 检查MetricsIntegration
                MetricsIntegration integration = MetricsIntegration.getInstance();
                if (integration != null && integration.isInitialized()) {
                    status.append("✓ 指标集成服务: 已初始化\n");
                } else {
                    status.append("✗ 指标集成服务: 未初始化\n");
                }
                
                // 检查ProductivityMetrics
                try {
                    ProductivityMetrics metrics = ProductivityMetrics.getInstance();
                    if (metrics != null) {
                        status.append("✓ 生产力指标服务: 已启动\n");
                        
                        // 获取报告测试
                        ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                        status.append("✓ 指标报告生成: 正常\n");
                        status.append("  - 总代码行数: ").append(report.totalLinesGenerated).append("\n");
                        status.append("  - 代码接受率: ").append(String.format("%.2f%%", report.avgCodeAcceptanceRate * 100)).append("\n");
                    } else {
                        status.append("✗ 生产力指标服务: 未启动\n");
                    }
                } catch (Exception e) {
                    status.append("✗ 生产力指标服务: 异常 - ").append(e.getMessage()).append("\n");
                }
                
                // 检查MetricsCollector
                if (integration != null && integration.getMetricsCollector() != null) {
                    status.append("✓ 指标收集器: 已创建\n");
                } else {
                    status.append("✗ 指标收集器: 未创建\n");
                }
                
                // 测试指标记录
                try {
                    if (integration != null && integration.isInitialized()) {
                        integration.recordAICompletion("java", "System.out.println(\"test\");", true, 100L);
                        status.append("✓ 指标记录测试: 成功\n");
                    }
                } catch (Exception e) {
                    status.append("✗ 指标记录测试: 失败 - ").append(e.getMessage()).append("\n");
                }
                
                status.append("\n系统已准备好收集用户使用数据！");
                
                // 显示通知
                ApplicationManager.getApplication().invokeLater(() -> {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("proxyai.notification.group")
                        .createNotification("指标收集系统状态", status.toString(), NotificationType.INFORMATION)
                        .notify(project);
                });
                
                LOG.info("指标收集系统验证完成:\n" + status.toString());
                
            } catch (Exception e) {
                LOG.error("验证指标收集系统时发生错误", e);
            }
        });
    }
    
    /**
     * 模拟一些测试数据
     */
    public static void generateTestMetrics(Project project) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                MetricsIntegration integration = MetricsIntegration.getInstance();
                if (integration == null || !integration.isInitialized()) {
                    LOG.warn("指标系统未初始化，无法生成测试数据");
                    return;
                }
                
                // 生成一些测试的代码补全数据
                String[] languages = {"java", "python", "javascript", "kotlin"};
                String[] codeSnippets = {
                    "public void test() {\n    System.out.println(\"Hello\");\n}",
                    "def hello():\n    print(\"Hello World\")",
                    "function hello() {\n    console.log(\"Hello\");\n}",
                    "fun hello() {\n    println(\"Hello\")\n}"
                };
                
                for (int i = 0; i < 10; i++) {
                    String language = languages[i % languages.length];
                    String code = codeSnippets[i % codeSnippets.length];
                    boolean accepted = i % 3 != 0; // 大约67%的接受率
                    long responseTime = 50 + (long)(Math.random() * 200); // 50-250ms
                    
                    integration.recordAICompletion(language, code, accepted, responseTime);
                    
                    Thread.sleep(100); // 避免过快生成
                }
                
                // 生成一些聊天数据
                integration.recordAIChatGeneration(
                    "// 生成的代码\nclass Example {\n    public void method() {}\n}",
                    "class Example {\n    public void method() {}\n}",
                    5000L,
                    "code_generation"
                );
                
                LOG.info("已生成测试指标数据");
                
                // 显示通知
                ApplicationManager.getApplication().invokeLater(() -> {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("proxyai.notification.group")
                        .createNotification("测试数据生成", "已生成测试指标数据，可以查看指标面板", NotificationType.INFORMATION)
                        .notify(project);
                });
                
            } catch (Exception e) {
                LOG.error("生成测试指标数据时发生错误", e);
            }
        });
    }
}