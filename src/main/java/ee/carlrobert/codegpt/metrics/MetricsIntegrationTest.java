package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;

/**
 * 效能度量集成测试
 * 用于验证度量收集链路是否正常工作
 */
public class MetricsIntegrationTest {
    
    private static final Logger LOG = Logger.getInstance(MetricsIntegrationTest.class);
    
    /**
     * 测试聊天功能的度量收集
     */
    public static void testChatMetricsCollection() {
        try {
            System.out.println("=== 测试聊天功能度量收集 ===");
            
            // 模拟聊天会话
            String sessionId = "test-chat-session-" + System.currentTimeMillis();
            String taskType = "code_generation";
            
            // 1. 开始聊天会话
            SafeMetricsCollector.safeStartChatSession(sessionId, taskType);
            System.out.println("✓ 聊天会话已开始: " + sessionId);
            
            // 2. 模拟AI响应和代码生成
            String aiResponse = "这是一个Java类的示例：\n\n```java\npublic class TestClass {\n    public void testMethod() {\n        System.out.println(\"Hello World\");\n    }\n}\n```\n\n这个类包含了一个简单的方法。";
            String extractedCode = extractCodeFromMessage(aiResponse);
            
            SafeMetricsCollector.safeRecordAIResponse(sessionId, aiResponse, extractedCode);
            System.out.println("✓ AI响应已记录");
            System.out.println("  - 响应长度: " + aiResponse.length() + " 字符");
            System.out.println("  - 提取的代码: " + extractedCode.length() + " 字符");
            System.out.println("  - 代码行数: " + countLines(extractedCode));
            
            // 3. 等待一秒让数据处理完成
            Thread.sleep(1000);
            
            // 4. 检查统计数据
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
            
            System.out.println("=== 统计结果 ===");
            System.out.println("生成代码行数: " + report.totalLinesGenerated);
            System.out.println("代码接受率: " + String.format("%.2f%%", report.avgCodeAcceptanceRate * 100));
            System.out.println("节省时间: " + String.format("%.2f", report.totalTimeSavedHours) + " 小时");
            System.out.println("效率提升: " + String.format("%.2f%%", report.avgEfficiencyGain));
            
            if (report.totalLinesGenerated > 0) {
                System.out.println("✅ 聊天功能度量收集正常工作！");
                return;
            } else {
                System.out.println("❌ 聊天功能度量收集可能有问题");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 测试聊天度量收集时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试代码补全功能的度量收集
     */
    public static void testCodeCompletionMetricsCollection() {
        try {
            System.out.println("\n=== 测试代码补全度量收集 ===");
            
            // 模拟代码补全
            String language = "java";
            String completionText = "public void testMethod() {\n    System.out.println(\"Test\");\n    return;\n}";
            boolean accepted = true;
            long responseTime = 150L;
            
            // 记录代码补全
            SafeMetricsCollector.safeRecordAICompletion(language, completionText, accepted, responseTime);
            System.out.println("✓ 代码补全已记录");
            System.out.println("  - 语言: " + language);
            System.out.println("  - 补全文本长度: " + completionText.length() + " 字符");
            System.out.println("  - 代码行数: " + countLines(completionText));
            System.out.println("  - 是否接受: " + accepted);
            System.out.println("  - 响应时间: " + responseTime + "ms");
            
            // 等待数据处理
            Thread.sleep(1000);
            
            // 检查统计数据
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
            
            System.out.println("=== 统计结果 ===");
            System.out.println("生成代码行数: " + report.totalLinesGenerated);
            System.out.println("代码接受率: " + String.format("%.2f%%", report.avgCodeAcceptanceRate * 100));
            
            if (report.totalLinesGenerated > 0) {
                System.out.println("✅ 代码补全度量收集正常工作！");
            } else {
                System.out.println("❌ 代码补全度量收集可能有问题");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 测试代码补全度量收集时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试完整的度量收集链路
     */
    public static void testCompleteMetricsChain() {
        try {
            System.out.println("\n=== 测试完整度量收集链路 ===");
            
            // 获取测试前的基准数据
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            ProductivityMetrics.ProductivityReport beforeReport = metrics.getProductivityReport(1);
            
            System.out.println("测试前统计:");
            System.out.println("  - 生成代码行数: " + beforeReport.totalLinesGenerated);
            System.out.println("  - 代码接受率: " + String.format("%.2f%%", beforeReport.avgCodeAcceptanceRate * 100));
            
            // 执行多个测试操作
            testChatMetricsCollection();
            testCodeCompletionMetricsCollection();
            
            // 再次测试聊天功能
            String sessionId2 = "test-session-2-" + System.currentTimeMillis();
            SafeMetricsCollector.safeStartChatSession(sessionId2, "debugging");
            
            String debugResponse = "这里是修复bug的代码：\n\n```python\ndef fix_bug():\n    try:\n        result = process_data()\n        return result\n    except Exception as e:\n        log_error(e)\n        return None\n```";
            SafeMetricsCollector.safeRecordAIResponse(sessionId2, debugResponse, extractCodeFromMessage(debugResponse));
            
            // 等待所有数据处理完成
            Thread.sleep(2000);
            
            // 获取测试后的数据
            ProductivityMetrics.ProductivityReport afterReport = metrics.getProductivityReport(1);
            
            System.out.println("\n=== 最终测试结果 ===");
            System.out.println("测试后统计:");
            System.out.println("  - 生成代码行数: " + afterReport.totalLinesGenerated);
            System.out.println("  - 代码接受率: " + String.format("%.2f%%", afterReport.avgCodeAcceptanceRate * 100));
            System.out.println("  - 节省时间: " + String.format("%.2f", afterReport.totalTimeSavedHours) + " 小时");
            System.out.println("  - 效率提升: " + String.format("%.2f%%", afterReport.avgEfficiencyGain));
            
            // 检查数据是否有变化
            boolean dataChanged = afterReport.totalLinesGenerated > beforeReport.totalLinesGenerated;
            
            if (dataChanged) {
                System.out.println("✅ 度量收集链路正常工作！数据已更新");
                System.out.println("  - 新增代码行数: " + (afterReport.totalLinesGenerated - beforeReport.totalLinesGenerated));
            } else {
                System.out.println("❌ 度量收集链路可能有问题，数据没有变化");
                
                // 诊断问题
                System.out.println("\n=== 问题诊断 ===");
                System.out.println("1. 检查MetricsIntegration是否初始化:");
                MetricsIntegration integration = MetricsIntegration.getInstance();
                System.out.println("   - 已初始化: " + integration.isInitialized());
                
                System.out.println("2. 检查ProductivityMetrics状态:");
                System.out.println("   - 实例存在: " + (metrics != null));
                
                System.out.println("3. 建议检查:");
                System.out.println("   - SafeMetricsCollector的异常日志");
                System.out.println("   - MetricsIntegration的初始化状态");
                System.out.println("   - ProductivityMetrics的数据存储");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 测试完整度量链路时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从消息中提取代码块（复制自ToolWindowCompletionResponseEventListener）
     */
    private static String extractCodeFromMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
        StringBuilder codeBuilder = new StringBuilder();
        String[] lines = message.split("\n");
        boolean inCodeBlock = false;
        
        for (String line : lines) {
            if (line.trim().startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                continue;
            }
            
            if (inCodeBlock) {
                codeBuilder.append(line).append("\n");
            }
        }
        
        return codeBuilder.toString().trim();
    }
    
    /**
     * 计算代码行数
     */
    private static int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\\r?\\n").length;
    }
    
    /**
     * 运行所有测试
     */
    public static void runAllTests() {
        System.out.println("🧪 开始ProxyAI效能度量集成测试...\n");
        
        try {
            testCompleteMetricsChain();
            
            System.out.println("\n🎉 所有测试完成！");
            System.out.println("如果看到数据更新，说明度量收集正常工作。");
            System.out.println("如果数据没有变化，请检查上面的诊断信息。");
            
        } catch (Exception e) {
            System.err.println("❌ 运行测试时发生严重错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}