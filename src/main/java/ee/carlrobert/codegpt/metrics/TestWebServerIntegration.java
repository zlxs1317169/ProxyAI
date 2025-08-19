package ee.carlrobert.codegpt.metrics;

import ee.carlrobert.codegpt.metrics.web.MetricsWebServer;
import ee.carlrobert.codegpt.settings.metrics.MetricsSettings;

/**
 * 测试Web服务器集成
 */
public class TestWebServerIntegration {
    
    public static void main(String[] args) {
        System.out.println("=== 测试Web服务器集成 ===");
        
        try {
            // 测试设置
            System.out.println("🔧 测试设置...");
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings != null) {
                System.out.println("✅ MetricsSettings 获取成功");
                System.out.println("- Web服务器启用: " + settings.isWebServerEnabled());
                System.out.println("- Web服务器端口: " + settings.getWebServerPort());
            } else {
                System.out.println("❌ MetricsSettings 获取失败");
            }
            
            // 测试ProductivityMetrics
            System.out.println("\n📊 测试ProductivityMetrics...");
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                System.out.println("✅ ProductivityMetrics 获取成功");
                
                // 测试报告生成
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                if (report != null) {
                    System.out.println("✅ 报告生成成功");
                    System.out.println("- 总行数: " + report.totalLinesGenerated);
                    System.out.println("- 节省时间: " + report.totalTimeSavedHours + " 小时");
                    System.out.println("- 效率等级: " + report.efficiencyLevel);
                } else {
                    System.out.println("❌ 报告生成失败");
                }
            } else {
                System.out.println("❌ ProductivityMetrics 获取失败");
            }
            
            System.out.println("\n=== 测试完成 ===");
            System.out.println("🎯 如果所有测试都通过，Web服务器应该能够正常启动");
            System.out.println("🌐 启动后可以通过浏览器访问: http://localhost:8090");
            
        } catch (Exception e) {
            System.err.println("❌ 测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
