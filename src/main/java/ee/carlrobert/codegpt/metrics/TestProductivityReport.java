package ee.carlrobert.codegpt.metrics;

/**
 * 测试ProductivityMetrics的报告生成功能
 */
public class TestProductivityReport {
    
    public static void main(String[] args) {
        System.out.println("=== 测试ProductivityMetrics报告生成 ===");
        
        // 创建一个测试实例
        ProductivityMetrics metrics = new ProductivityMetrics();
        metrics.setActionType("code_completion");
        metrics.setLinesGenerated(50);
        metrics.setAcceptanceRate(85.0);
        metrics.setResponseTime(3000); // 3秒
        metrics.setProcessingTime(15000); // 15秒
        metrics.setUserRating(4);
        metrics.markSuccessful();
        
        System.out.println("创建的指标实例:");
        System.out.println("- 动作类型: " + metrics.getActionType());
        System.out.println("- 生成行数: " + metrics.getLinesGenerated());
        System.out.println("- 接受率: " + metrics.getAcceptanceRate() + "%");
        System.out.println("- 响应时间: " + metrics.getResponseTime() + "ms");
        System.out.println("- 处理时间: " + metrics.getProcessingTime() + "ms");
        System.out.println("- 用户评分: " + metrics.getUserRating());
        System.out.println("- 是否成功: " + metrics.isSuccessful());
        
        // 测试getProductivityReport方法
        System.out.println("\n=== 测试getProductivityReport方法 ===");
        try {
            ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
            if (report != null) {
                System.out.println("✅ 报告生成成功!");
                System.out.println("- 分析时间: " + report.analysisTime);
                System.out.println("- 总行数: " + report.totalLinesGenerated);
                System.out.println("- 平均接受率: " + report.avgCodeAcceptanceRate + "%");
                System.out.println("- 节省时间: " + report.totalTimeSavedHours + " 小时");
                System.out.println("- 效率提升: " + report.avgEfficiencyGain);
                System.out.println("- 总体评分: " + report.overallScore);
                System.out.println("- 效率等级: " + report.efficiencyLevel);
                System.out.println("- 摘要: " + report.summary);
            } else {
                System.out.println("❌ 报告生成失败: 返回null");
            }
        } catch (Exception e) {
            System.out.println("❌ 报告生成异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 测试getDailyStats方法
        System.out.println("\n=== 测试getDailyStats方法 ===");
        try {
            ProductivityMetrics.DailyProductivityStats stats = metrics.getDailyStats("2024-01-01");
            if (stats != null) {
                System.out.println("✅ 每日统计生成成功!");
                System.out.println("- 日期: " + stats.date);
                System.out.println("- 总请求数: " + stats.totalRequests);
                System.out.println("- 成功请求数: " + stats.successfulRequests);
                System.out.println("- 总行数: " + stats.totalLinesGenerated);
                System.out.println("- 平均接受率: " + stats.averageAcceptanceRate + "%");
                System.out.println("- 代码补全次数: " + stats.codeCompletionsCount);
                System.out.println("- 聊天会话次数: " + stats.chatSessionsCount);
                System.out.println("- 节省时间: " + stats.timeSavedMs + "ms");
            } else {
                System.out.println("❌ 每日统计生成失败: 返回null");
            }
        } catch (Exception e) {
            System.out.println("❌ 每日统计生成异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 测试getState方法
        System.out.println("\n=== 测试getState方法 ===");
        try {
            ProductivityMetrics.State state = metrics.getState();
            if (state != null) {
                System.out.println("✅ 状态获取成功!");
                System.out.println("- 代码补全列表: " + state.codeCompletions.size() + " 项");
                System.out.println("- 聊天代码生成列表: " + state.chatCodeGenerations.size() + " 项");
                System.out.println("- 时间节省列表: " + state.timeSavings.size() + " 项");
                System.out.println("- 每日统计映射: " + state.dailyStats.size() + " 项");
            } else {
                System.out.println("❌ 状态获取失败: 返回null");
            }
        } catch (Exception e) {
            System.out.println("❌ 状态获取异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
}
