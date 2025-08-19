package ee.carlrobert.codegpt.metrics;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 效能分析报告
 * 存储软件工程师使用ProxyAI的效能分析结果
 */
public class ProductivityReport {
    
    // 报告基本信息
    private LocalDateTime analysisTime;
    private int totalMetrics;
    private double overallScore;
    private String errorMessage;
    
    // 基础统计数据
    private ProductivityAnalyzer.BasicStats basicStats;
    
    // 分类指标分析
    private ProductivityAnalyzer.MetricAnalysis codeGenerationAnalysis;
    private ProductivityAnalyzer.MetricAnalysis codeQualityAnalysis;
    private ProductivityAnalyzer.MetricAnalysis developmentSpeedAnalysis;
    private ProductivityAnalyzer.MetricAnalysis problemSolvingAnalysis;
    private ProductivityAnalyzer.MetricAnalysis learningCurveAnalysis;
    private ProductivityAnalyzer.MetricAnalysis collaborationAnalysis;
    private ProductivityAnalyzer.MetricAnalysis innovationAnalysis;
    private ProductivityAnalyzer.MetricAnalysis costEffectivenessAnalysis;
    private ProductivityAnalyzer.MetricAnalysis userExperienceAnalysis;
    private ProductivityAnalyzer.MetricAnalysis systemPerformanceAnalysis;
    
    // 趋势分析
    private ProductivityAnalyzer.TrendAnalysis trendAnalysis;
    
    // 改进建议
    private List<String> improvementSuggestions;
    
    public ProductivityReport() {
        this.analysisTime = LocalDateTime.now();
        this.totalMetrics = 0;
        this.overallScore = 0.0;
    }
    
    /**
     * 获取效能等级
     */
    public String getEfficiencyLevel() {
        if (overallScore >= 90) return "优秀";
        else if (overallScore >= 80) return "良好";
        else if (overallScore >= 70) return "中等";
        else if (overallScore >= 60) return "及格";
        else return "需要改进";
    }
    
    /**
     * 获取效能等级颜色
     */
    public String getEfficiencyLevelColor() {
        if (overallScore >= 90) return "#28a745"; // 绿色
        else if (overallScore >= 80) return "#17a2b8"; // 蓝色
        else if (overallScore >= 70) return "#ffc107"; // 黄色
        else if (overallScore >= 60) return "#fd7e14"; // 橙色
        else return "#dc3545"; // 红色
    }
    
    /**
     * 获取报告摘要
     */
    public String getSummary() {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            return "报告生成失败: " + errorMessage;
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("效能分析报告摘要\n");
        summary.append("================\n");
        summary.append("分析时间: ").append(analysisTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        summary.append("数据样本: ").append(totalMetrics).append(" 条记录\n");
        summary.append("综合评分: ").append(String.format("%.1f", overallScore)).append("/100 (").append(getEfficiencyLevel()).append(")\n");
        
        if (basicStats != null) {
            summary.append("成功率: ").append(String.format("%.1f%%", basicStats.getSuccessRate() * 100)).append("\n");
            summary.append("平均响应时间: ").append(String.format("%.0fms", basicStats.getAverageResponseTime())).append("\n");
            summary.append("平均接受率: ").append(String.format("%.1f%%", basicStats.getAverageAcceptanceRate() * 100)).append("\n");
        }
        
        if (trendAnalysis != null && trendAnalysis.getOverallTrend() != null) {
            summary.append("整体趋势: ").append(trendAnalysis.getOverallTrend()).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * 获取详细分析结果
     */
    public String getDetailedAnalysis() {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            return "无法生成详细分析: " + errorMessage;
        }
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("详细效能分析\n");
        analysis.append("============\n\n");
        
        // 代码生成效能
        if (codeGenerationAnalysis != null) {
            analysis.append("1. 代码生成效能: ").append(String.format("%.1f", codeGenerationAnalysis.getScore())).append("/100\n");
            analysis.append("   - 平均生成行数: ").append(String.format("%.1f", codeGenerationAnalysis.getMetricValue("lines_generated"))).append("\n");
            analysis.append("   - 平均接受率: ").append(String.format("%.1f%%", codeGenerationAnalysis.getMetricValue("acceptance_rate") * 100)).append("\n");
            analysis.append("   - 平均生成时间: ").append(String.format("%.0fms", codeGenerationAnalysis.getMetricValue("generation_time"))).append("\n\n");
        }
        
        // 代码质量
        if (codeQualityAnalysis != null) {
            analysis.append("2. 代码质量: ").append(String.format("%.1f", codeQualityAnalysis.getScore())).append("/100\n");
            analysis.append("   - 语法正确性: ").append(String.format("%.1f%%", codeQualityAnalysis.getMetricValue("syntax_correctness") * 100)).append("\n");
            analysis.append("   - 语义准确性: ").append(String.format("%.1f%%", codeQualityAnalysis.getMetricValue("semantic_accuracy") * 100)).append("\n");
            analysis.append("   - 最佳实践遵循: ").append(String.format("%.1f%%", codeQualityAnalysis.getMetricValue("best_practices") * 100)).append("\n\n");
        }
        
        // 开发速度
        if (developmentSpeedAnalysis != null) {
            analysis.append("3. 开发速度: ").append(String.format("%.1f", developmentSpeedAnalysis.getScore())).append("/100\n");
            analysis.append("   - 首次结果时间: ").append(String.format("%.0fms", developmentSpeedAnalysis.getMetricValue("time_to_first_result"))).append("\n");
            analysis.append("   - 总开发时间: ").append(String.format("%.0fms", developmentSpeedAnalysis.getMetricValue("total_development_time"))).append("\n\n");
        }
        
        // 问题解决效能
        if (problemSolvingAnalysis != null) {
            analysis.append("4. 问题解决效能: ").append(String.format("%.1f", problemSolvingAnalysis.getScore())).append("/100\n");
            analysis.append("   - 解决方案准确性: ").append(String.format("%.1f%%", problemSolvingAnalysis.getMetricValue("solution_accuracy") * 100)).append("\n");
            analysis.append("   - 调试效率: ").append(String.format("%.1f", problemSolvingAnalysis.getMetricValue("debugging_efficiency"))).append("\n\n");
        }
        
        // 学习曲线
        if (learningCurveAnalysis != null) {
            analysis.append("5. 学习曲线: ").append(String.format("%.1f", learningCurveAnalysis.getScore())).append("/100\n");
            analysis.append("   - 技能提升: ").append(String.format("%.1f", learningCurveAnalysis.getMetricValue("skill_improvement"))).append("\n");
            analysis.append("   - 工具采用率: ").append(String.format("%.1f%%", learningCurveAnalysis.getMetricValue("tool_adoption_rate") * 100)).append("\n\n");
        }
        
        // 协作效能
        if (collaborationAnalysis != null) {
            analysis.append("6. 协作效能: ").append(String.format("%.1f", collaborationAnalysis.getScore())).append("/100\n\n");
        }
        
        // 创新效能
        if (innovationAnalysis != null) {
            analysis.append("7. 创新效能: ").append(String.format("%.1f", innovationAnalysis.getScore())).append("/100\n\n");
        }
        
        // 成本效益
        if (costEffectivenessAnalysis != null) {
            analysis.append("8. 成本效益: ").append(String.format("%.1f", costEffectivenessAnalysis.getScore())).append("/100\n");
            analysis.append("   - Token使用量: ").append(String.format("%.0f", costEffectivenessAnalysis.getMetricValue("token_usage"))).append("\n");
            analysis.append("   - API成本: $").append(String.format("%.2f", costEffectivenessAnalysis.getMetricValue("api_cost"))).append("\n\n");
        }
        
        // 用户体验
        if (userExperienceAnalysis != null) {
            analysis.append("9. 用户体验: ").append(String.format("%.1f", userExperienceAnalysis.getScore())).append("/100\n");
            analysis.append("   - 满意度评分: ").append(String.format("%.1f/5.0", userExperienceAnalysis.getMetricValue("satisfaction_rating"))).append("\n");
            analysis.append("   - 响应时间: ").append(String.format("%.0fms", userExperienceAnalysis.getMetricValue("response_time"))).append("\n\n");
        }
        
        // 系统性能
        if (systemPerformanceAnalysis != null) {
            analysis.append("10. 系统性能 (监控指标)\n");
            analysis.append("    - 内存使用: ").append(String.format("%.0fMB", systemPerformanceAnalysis.getMetricValue("memory_usage") / 1024 / 1024)).append("\n");
            analysis.append("    - CPU利用率: ").append(String.format("%.1f%%", systemPerformanceAnalysis.getMetricValue("cpu_utilization"))).append("\n\n");
        }
        
        return analysis.toString();
    }
    
    /**
     * 获取改进建议
     */
    public String getImprovementSuggestionsText() {
        if (improvementSuggestions == null || improvementSuggestions.isEmpty()) {
            return "暂无改进建议";
        }
        
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("改进建议\n");
        suggestions.append("========\n");
        
        for (int i = 0; i < improvementSuggestions.size(); i++) {
            suggestions.append(i + 1).append(". ").append(improvementSuggestions.get(i)).append("\n");
        }
        
        return suggestions.toString();
    }
    
    /**
     * 获取趋势分析
     */
    public String getTrendAnalysisText() {
        if (trendAnalysis == null) {
            return "暂无趋势数据";
        }
        
        StringBuilder trends = new StringBuilder();
        trends.append("趋势分析\n");
        trends.append("========\n");
        trends.append("整体趋势: ").append(trendAnalysis.getOverallTrend() != null ? trendAnalysis.getOverallTrend() : "未知").append("\n");
        trends.append("趋势强度: ").append(String.format("%.2f", trendAnalysis.getTrendStrength())).append("\n\n");
        
        if (trendAnalysis.getDailyTrends() != null && !trendAnalysis.getDailyTrends().isEmpty()) {
            trends.append("每日效能趋势:\n");
            for (ProductivityAnalyzer.DailyTrend trend : trendAnalysis.getDailyTrends()) {
                trends.append("  ").append(trend.getDate())
                      .append(": ").append(String.format("%.1f", trend.getEfficiencyScore()))
                      .append(" (").append(trend.getMetricsCount()).append(" 条记录)\n");
            }
        }
        
        return trends.toString();
    }
    
    /**
     * 导出为JSON格式
     */
    public String toJson() {
        // 这里可以集成Jackson或其他JSON库来生成标准JSON
        // 为了简化，这里返回一个基本的JSON字符串
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"analysisTime\": \"").append(analysisTime.toString()).append("\",\n");
        json.append("  \"totalMetrics\": ").append(totalMetrics).append(",\n");
        json.append("  \"overallScore\": ").append(overallScore).append(",\n");
        json.append("  \"efficiencyLevel\": \"").append(getEfficiencyLevel()).append("\",\n");
        json.append("  \"efficiencyLevelColor\": \"").append(getEfficiencyLevelColor()).append("\",\n");
        
        if (basicStats != null) {
            json.append("  \"basicStats\": {\n");
            json.append("    \"successRate\": ").append(basicStats.getSuccessRate()).append(",\n");
            json.append("    \"averageResponseTime\": ").append(basicStats.getAverageResponseTime()).append(",\n");
            json.append("    \"averageAcceptanceRate\": ").append(basicStats.getAverageAcceptanceRate()).append("\n");
            json.append("  },\n");
        }
        
        json.append("  \"summary\": \"").append(getSummary().replace("\n", "\\n")).append("\"\n");
        json.append("}");
        
        return json.toString();
    }
    
    // Getters and Setters
    public LocalDateTime getAnalysisTime() { return analysisTime; }
    public void setAnalysisTime(LocalDateTime analysisTime) { this.analysisTime = analysisTime; }
    
    public int getTotalMetrics() { return totalMetrics; }
    public void setTotalMetrics(int totalMetrics) { this.totalMetrics = totalMetrics; }
    
    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public ProductivityAnalyzer.BasicStats getBasicStats() { return basicStats; }
    public void setBasicStats(ProductivityAnalyzer.BasicStats basicStats) { this.basicStats = basicStats; }
    
    public ProductivityAnalyzer.MetricAnalysis getCodeGenerationAnalysis() { return codeGenerationAnalysis; }
    public void setCodeGenerationAnalysis(ProductivityAnalyzer.MetricAnalysis codeGenerationAnalysis) { this.codeGenerationAnalysis = codeGenerationAnalysis; }
    
    public ProductivityAnalyzer.MetricAnalysis getCodeQualityAnalysis() { return codeQualityAnalysis; }
    public void setCodeQualityAnalysis(ProductivityAnalyzer.MetricAnalysis codeQualityAnalysis) { this.codeQualityAnalysis = codeQualityAnalysis; }
    
    public ProductivityAnalyzer.MetricAnalysis getDevelopmentSpeedAnalysis() { return developmentSpeedAnalysis; }
    public void setDevelopmentSpeedAnalysis(ProductivityAnalyzer.MetricAnalysis developmentSpeedAnalysis) { this.developmentSpeedAnalysis = developmentSpeedAnalysis; }
    
    public ProductivityAnalyzer.MetricAnalysis getProblemSolvingAnalysis() { return problemSolvingAnalysis; }
    public void setProblemSolvingAnalysis(ProductivityAnalyzer.MetricAnalysis problemSolvingAnalysis) { this.problemSolvingAnalysis = problemSolvingAnalysis; }
    
    public ProductivityAnalyzer.MetricAnalysis getLearningCurveAnalysis() { return learningCurveAnalysis; }
    public void setLearningCurveAnalysis(ProductivityAnalyzer.MetricAnalysis learningCurveAnalysis) { this.learningCurveAnalysis = learningCurveAnalysis; }
    
    public ProductivityAnalyzer.MetricAnalysis getCollaborationAnalysis() { return collaborationAnalysis; }
    public void setCollaborationAnalysis(ProductivityAnalyzer.MetricAnalysis collaborationAnalysis) { this.collaborationAnalysis = collaborationAnalysis; }
    
    public ProductivityAnalyzer.MetricAnalysis getInnovationAnalysis() { return innovationAnalysis; }
    public void setInnovationAnalysis(ProductivityAnalyzer.MetricAnalysis innovationAnalysis) { this.innovationAnalysis = innovationAnalysis; }
    
    public ProductivityAnalyzer.MetricAnalysis getCostEffectivenessAnalysis() { return costEffectivenessAnalysis; }
    public void setCostEffectivenessAnalysis(ProductivityAnalyzer.MetricAnalysis costEffectivenessAnalysis) { this.costEffectivenessAnalysis = costEffectivenessAnalysis; }
    
    public ProductivityAnalyzer.MetricAnalysis getUserExperienceAnalysis() { return userExperienceAnalysis; }
    public void setUserExperienceAnalysis(ProductivityAnalyzer.MetricAnalysis userExperienceAnalysis) { this.userExperienceAnalysis = userExperienceAnalysis; }
    
    public ProductivityAnalyzer.MetricAnalysis getSystemPerformanceAnalysis() { return systemPerformanceAnalysis; }
    public void setSystemPerformanceAnalysis(ProductivityAnalyzer.MetricAnalysis systemPerformanceAnalysis) { this.systemPerformanceAnalysis = systemPerformanceAnalysis; }
    
    public ProductivityAnalyzer.TrendAnalysis getTrendAnalysis() { return trendAnalysis; }
    public void setTrendAnalysis(ProductivityAnalyzer.TrendAnalysis trendAnalysis) { this.trendAnalysis = trendAnalysis; }
    
    public List<String> getImprovementSuggestions() { return improvementSuggestions; }
    public void setImprovementSuggestions(List<String> improvementSuggestions) { this.improvementSuggestions = improvementSuggestions; }
    
    @Override
    public String toString() {
        return getSummary();
    }
}
