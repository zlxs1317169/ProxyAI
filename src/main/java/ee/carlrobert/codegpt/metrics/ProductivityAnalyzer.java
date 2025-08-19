package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;
import ee.carlrobert.codegpt.metrics.SoftwareEngineerMetrics;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 效能指标分析器
 * 分析软件工程师使用ProxyAI的效能数据，生成综合评估报告
 */
public class ProductivityAnalyzer {
    
    private static final Logger LOG = Logger.getInstance(ProductivityAnalyzer.class);
    
    private final List<ProductivityMetrics> metricsData;
    private final LocalDateTime analysisTime;
    
    public ProductivityAnalyzer(List<ProductivityMetrics> metricsData) {
        this.metricsData = new ArrayList<>(metricsData);
        this.analysisTime = LocalDateTime.now();
    }
    
    /**
     * 生成综合效能分析报告
     */
    public ProductivityReport generateComprehensiveReport() {
        try {
            ProductivityReport report = new ProductivityReport();
            report.setAnalysisTime(analysisTime);
            report.setTotalMetrics(metricsData.size());
            
            // 基础统计
            report.setBasicStats(calculateBasicStats());
            
            // 分类指标分析
            report.setCodeGenerationAnalysis(analyzeCodeGeneration());
            report.setCodeQualityAnalysis(analyzeCodeQuality());
            report.setDevelopmentSpeedAnalysis(analyzeDevelopmentSpeed());
            report.setProblemSolvingAnalysis(analyzeProblemSolving());
            report.setLearningCurveAnalysis(analyzeLearningCurve());
            report.setCollaborationAnalysis(analyzeCollaboration());
            report.setInnovationAnalysis(analyzeInnovation());
            report.setCostEffectivenessAnalysis(analyzeCostEffectiveness());
            report.setUserExperienceAnalysis(analyzeUserExperience());
            report.setSystemPerformanceAnalysis(analyzeSystemPerformance());
            
            // 综合评分
            report.setOverallScore(calculateOverallScore(report));
            
            // 趋势分析
            report.setTrendAnalysis(analyzeTrends());
            
            // 改进建议
            report.setImprovementSuggestions(generateImprovementSuggestions(report));
            
            LOG.info("效能分析报告生成完成，综合评分: " + report.getOverallScore());
            return report;
            
        } catch (Exception e) {
            LOG.error("生成效能分析报告时发生错误", e);
            return createErrorReport();
        }
    }
    
    /**
     * 计算基础统计数据
     */
    private BasicStats calculateBasicStats() {
        BasicStats stats = new BasicStats();
        
        if (metricsData.isEmpty()) {
            return stats;
        }
        
        // 成功率统计
        long successfulCount = metricsData.stream()
                .mapToLong(m -> m.isSuccessful() ? 1 : 0)
                .sum();
        stats.setSuccessRate((double) successfulCount / metricsData.size());
        
        // 响应时间统计
        DoubleSummaryStatistics responseTimeStats = metricsData.stream()
                .mapToDouble(m -> m.getResponseTime())
                .filter(t -> t > 0)
                .summaryStatistics();
        
        stats.setAverageResponseTime(responseTimeStats.getAverage());
        stats.setMinResponseTime(responseTimeStats.getMin());
        stats.setMaxResponseTime(responseTimeStats.getMax());
        
        // Token使用统计
        DoubleSummaryStatistics tokenStats = metricsData.stream()
                .mapToDouble(m -> m.getTotalTokenCount())
                .filter(t -> t > 0)
                .summaryStatistics();
        
        stats.setAverageTokenUsage(tokenStats.getAverage());
        stats.setTotalTokenUsage(tokenStats.getSum());
        
        // 代码生成统计
        DoubleSummaryStatistics linesStats = metricsData.stream()
                .mapToDouble(m -> m.getLinesGenerated())
                .filter(t -> t > 0)
                .summaryStatistics();
        
        stats.setAverageLinesGenerated(linesStats.getAverage());
        stats.setTotalLinesGenerated(linesStats.getSum());
        
        // 接受率统计
        double totalAcceptanceRate = metricsData.stream()
                .mapToDouble(m -> m.getAcceptanceRate())
                .filter(r -> r > 0)
                .average()
                .orElse(0.0);
        stats.setAverageAcceptanceRate(totalAcceptanceRate);
        
        return stats;
    }
    
    /**
     * 分析代码生成效能
     */
    private MetricAnalysis analyzeCodeGeneration() {
        MetricAnalysis analysis = new MetricAnalysis(SoftwareEngineerMetrics.CODE_GENERATION_EFFICIENCY);
        
        if (metricsData.isEmpty()) {
            return analysis;
        }
        
        // 计算代码生成相关指标
        double avgLinesGenerated = metricsData.stream()
                .mapToDouble(m -> m.getLinesGenerated())
                .filter(l -> l > 0)
                .average()
                .orElse(0.0);
        
        double avgAcceptanceRate = metricsData.stream()
                .mapToDouble(m -> m.getAcceptanceRate())
                .filter(r -> r > 0)
                .average()
                .orElse(0.0);
        
        double avgGenerationTime = metricsData.stream()
                .mapToDouble(m -> m.getProcessingTime())
                .filter(t -> t > 0)
                .average()
                .orElse(0.0);
        
        analysis.setMetricValue("lines_generated", avgLinesGenerated);
        analysis.setMetricValue("acceptance_rate", avgAcceptanceRate);
        analysis.setMetricValue("generation_time", avgGenerationTime);
        
        // 计算效能评分
        double score = calculateCodeGenerationScore(avgLinesGenerated, avgAcceptanceRate, avgGenerationTime);
        analysis.setScore(score);
        
        return analysis;
    }
    
    /**
     * 分析代码质量
     */
    private MetricAnalysis analyzeCodeQuality() {
        MetricAnalysis analysis = new MetricAnalysis(SoftwareEngineerMetrics.CODE_QUALITY);
        
        if (metricsData.isEmpty()) {
            return analysis;
        }
        
        // 基于成功率和接受率评估代码质量
        double successRate = metricsData.stream()
                .mapToDouble(m -> m.isSuccessful() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);
        
        double acceptanceRate = metricsData.stream()
                .mapToDouble(m -> m.getAcceptanceRate())
                .filter(r -> r > 0)
                .average()
                .orElse(0.0);
        
        analysis.setMetricValue("syntax_correctness", successRate);
        analysis.setMetricValue("semantic_accuracy", acceptanceRate);
        analysis.setMetricValue("best_practices", (successRate + acceptanceRate) / 2);
        
        double score = (successRate * 0.4 + acceptanceRate * 0.6) * 100;
        analysis.setScore(score);
        
        return analysis;
    }
    
    /**
     * 分析开发速度
     */
    private MetricAnalysis analyzeDevelopmentSpeed() {
        MetricAnalysis analysis = new MetricAnalysis(SoftwareEngineerMetrics.DEVELOPMENT_SPEED);
        
        if (metricsData.isEmpty()) {
            return analysis;
        }
        
        // 计算开发速度相关指标
        double avgResponseTime = metricsData.stream()
                .mapToDouble(m -> m.getResponseTime())
                .filter(t -> t > 0)
                .average()
                .orElse(0.0);
        
        double avgProcessingTime = metricsData.stream()
                .mapToDouble(m -> m.getProcessingTime())
                .filter(t -> t > 0)
                .average()
                .orElse(0.0);
        
        analysis.setMetricValue("time_to_first_result", avgResponseTime);
        analysis.setMetricValue("total_development_time", avgProcessingTime);
        
        // 基于响应时间计算开发速度评分
        double score = calculateDevelopmentSpeedScore(avgResponseTime, avgProcessingTime);
        analysis.setScore(score);
        
        return analysis;
    }
    
    /**
     * 分析问题解决效能
     */
    private MetricAnalysis analyzeProblemSolving() {
        MetricAnalysis analysis = new MetricAnalysis(SoftwareEngineerMetrics.PROBLEM_SOLVING_EFFICIENCY);
        
        if (metricsData.isEmpty()) {
            return analysis;
        }
        
        // 计算问题解决相关指标
        double successRate = metricsData.stream()
                .mapToDouble(m -> m.isSuccessful() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);
        
        double avgRetryCount = metricsData.stream()
                .mapToDouble(m -> m.getRetryCount())
                .average()
                .orElse(0.0);
        
        analysis.setMetricValue("solution_accuracy", successRate);
        analysis.setMetricValue("debugging_efficiency", 1.0 / (1.0 + avgRetryCount));
        
        double score = (successRate * 0.7 + (1.0 / (1.0 + avgRetryCount)) * 0.3) * 100;
        analysis.setScore(score);
        
        return analysis;
    }
    
    /**
     * 分析学习曲线
     */
    private MetricAnalysis analyzeLearningCurve() {
        MetricAnalysis analysis = new MetricAnalysis(SoftwareEngineerMetrics.LEARNING_CURVE);
        
        if (metricsData.size() < 2) {
            return analysis;
        }
        
        // 按时间排序，分析学习趋势
        List<ProductivityMetrics> sortedMetrics = metricsData.stream()
                .sorted(Comparator.comparing(m -> m.getCreatedAt()))
                .collect(Collectors.toList());
        
        // 计算前后期效能对比
        int midPoint = sortedMetrics.size() / 2;
        List<ProductivityMetrics> earlyMetrics = sortedMetrics.subList(0, midPoint);
        List<ProductivityMetrics> lateMetrics = sortedMetrics.subList(midPoint, sortedMetrics.size());
        
        double earlySuccessRate = earlyMetrics.stream()
                .mapToDouble(m -> m.isSuccessful() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);
        
        double lateSuccessRate = lateMetrics.stream()
                .mapToDouble(m -> m.isSuccessful() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);
        
        double improvement = lateSuccessRate - earlySuccessRate;
        analysis.setMetricValue("skill_improvement", improvement);
        analysis.setMetricValue("tool_adoption_rate", lateSuccessRate);
        
        double score = (improvement * 0.6 + lateSuccessRate * 0.4) * 100;
        analysis.setScore(Math.max(0, score));
        
        return analysis;
    }
    
    /**
     * 分析协作效能
     */
    private MetricAnalysis analyzeCollaboration() {
        MetricAnalysis analysis = new MetricAnalysis(SoftwareEngineerMetrics.COLLABORATION_EFFICIENCY);
        
        // 协作效能需要更多上下文信息，这里基于现有数据做基础评估
        double score = 70.0; // 默认中等水平
        analysis.setScore(score);
        
        return analysis;
    }
    
    /**
     * 分析创新效能
     */
    private MetricAnalysis analyzeInnovation() {
        MetricAnalysis analysis = new MetricAnalysis(SoftwareEngineerMetrics.INNOVATION_EFFICIENCY);
        
        // 创新效能需要更复杂的分析，这里基于接受率做基础评估
        double avgAcceptanceRate = metricsData.stream()
                .mapToDouble(m -> m.getAcceptanceRate())
                .filter(r -> r > 0)
                .average()
                .orElse(0.0);
        
        double score = avgAcceptanceRate * 80; // 基于接受率评估创新性
        analysis.setScore(score);
        
        return analysis;
    }
    
    /**
     * 分析成本效益
     */
    private MetricAnalysis analyzeCostEffectiveness() {
        MetricAnalysis analysis = new MetricAnalysis(SoftwareEngineerMetrics.COST_EFFECTIVENESS);
        
        if (metricsData.isEmpty()) {
            return analysis;
        }
        
        // 计算成本效益相关指标
        double totalTokenUsage = metricsData.stream()
                .mapToDouble(m -> m.getTotalTokenCount())
                .sum();
        
        double avgTokenCost = metricsData.stream()
                .mapToDouble(m -> m.getTokenCost())
                .sum();
        
        analysis.setMetricValue("token_usage", totalTokenUsage);
        analysis.setMetricValue("api_cost", avgTokenCost);
        
        // 基于token使用效率计算成本效益评分
        double score = calculateCostEffectivenessScore(totalTokenUsage, avgTokenCost);
        analysis.setScore(score);
        
        return analysis;
    }
    
    /**
     * 分析用户体验
     */
    private MetricAnalysis analyzeUserExperience() {
        MetricAnalysis analysis = new MetricAnalysis(SoftwareEngineerMetrics.USER_EXPERIENCE);
        
        if (metricsData.isEmpty()) {
            return analysis;
        }
        
        // 计算用户体验相关指标
        double avgUserRating = metricsData.stream()
                .mapToDouble(m -> m.getUserRating())
                .filter(r -> r > 0)
                .average()
                .orElse(0.0);
        
        double avgResponseTime = metricsData.stream()
                .mapToDouble(m -> m.getResponseTime())
                .filter(t -> t > 0)
                .average()
                .orElse(0.0);
        
        analysis.setMetricValue("satisfaction_rating", avgUserRating);
        analysis.setMetricValue("response_time", avgResponseTime);
        
        double score = (avgUserRating * 0.7 + calculateResponseTimeScore(avgResponseTime) * 0.3) * 20;
        analysis.setScore(score);
        
        return analysis;
    }
    
    /**
     * 分析系统性能
     */
    private MetricAnalysis analyzeSystemPerformance() {
        MetricAnalysis analysis = new MetricAnalysis(SoftwareEngineerMetrics.SYSTEM_PERFORMANCE);
        
        if (metricsData.isEmpty()) {
            return analysis;
        }
        
        // 计算系统性能相关指标
        double avgMemoryUsage = metricsData.stream()
                .mapToDouble(m -> m.getMemoryUsage())
                .filter(m -> m > 0)
                .average()
                .orElse(0.0);
        
        double avgCpuUsage = metricsData.stream()
                .mapToDouble(m -> m.getCpuUsage())
                .filter(c -> c > 0)
                .average()
                .orElse(0.0);
        
        analysis.setMetricValue("memory_usage", avgMemoryUsage);
        analysis.setMetricValue("cpu_utilization", avgCpuUsage);
        
        // 系统性能不计入总分，仅监控
        analysis.setScore(0.0);
        
        return analysis;
    }
    
    /**
     * 计算综合效能评分
     */
    private double calculateOverallScore(ProductivityReport report) {
        double totalScore = 0.0;
        double totalWeight = 0.0;
        
        // 根据权重计算综合评分
        Map<SoftwareEngineerMetrics, MetricAnalysis> analyses = new HashMap<>();
        analyses.put(SoftwareEngineerMetrics.CODE_GENERATION_EFFICIENCY, report.getCodeGenerationAnalysis());
        analyses.put(SoftwareEngineerMetrics.CODE_QUALITY, report.getCodeQualityAnalysis());
        analyses.put(SoftwareEngineerMetrics.DEVELOPMENT_SPEED, report.getDevelopmentSpeedAnalysis());
        analyses.put(SoftwareEngineerMetrics.PROBLEM_SOLVING_EFFICIENCY, report.getProblemSolvingAnalysis());
        analyses.put(SoftwareEngineerMetrics.LEARNING_CURVE, report.getLearningCurveAnalysis());
        analyses.put(SoftwareEngineerMetrics.COLLABORATION_EFFICIENCY, report.getCollaborationAnalysis());
        analyses.put(SoftwareEngineerMetrics.INNOVATION_EFFICIENCY, report.getInnovationAnalysis());
        analyses.put(SoftwareEngineerMetrics.COST_EFFECTIVENESS, report.getCostEffectivenessAnalysis());
        analyses.put(SoftwareEngineerMetrics.USER_EXPERIENCE, report.getUserExperienceAnalysis());
        
        for (Map.Entry<SoftwareEngineerMetrics, MetricAnalysis> entry : analyses.entrySet()) {
            SoftwareEngineerMetrics metric = entry.getKey();
            MetricAnalysis analysis = entry.getValue();
            
            if (analysis != null && analysis.getScore() > 0) {
                double weight = metric.getWeight();
                totalScore += analysis.getScore() * weight;
                totalWeight += weight;
            }
        }
        
        return totalWeight > 0 ? totalScore / totalWeight : 0.0;
    }
    
    /**
     * 分析趋势
     */
    private TrendAnalysis analyzeTrends() {
        TrendAnalysis trendAnalysis = new TrendAnalysis();
        
        if (metricsData.size() < 2) {
            return trendAnalysis;
        }
        
        // 按时间分组分析趋势
        Map<String, List<ProductivityMetrics>> dailyMetrics = metricsData.stream()
                .collect(Collectors.groupingBy(m -> 
                    m.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        
        // 计算每日效能趋势
        List<DailyTrend> dailyTrends = dailyMetrics.entrySet().stream()
                .map(entry -> {
                    DailyTrend trend = new DailyTrend();
                    trend.setDate(entry.getKey());
                    
                    List<ProductivityMetrics> dayMetrics = entry.getValue();
                    double dailyScore = dayMetrics.stream()
                            .mapToDouble(m -> m.getEfficiencyScore())
                            .average()
                            .orElse(0.0);
                    
                    trend.setEfficiencyScore(dailyScore);
                    trend.setMetricsCount(dayMetrics.size());
                    
                    return trend;
                })
                .sorted(Comparator.comparing(DailyTrend::getDate))
                .collect(Collectors.toList());
        
        trendAnalysis.setDailyTrends(dailyTrends);
        
        // 计算整体趋势
        if (dailyTrends.size() >= 2) {
            DailyTrend firstDay = dailyTrends.get(0);
            DailyTrend lastDay = dailyTrends.get(dailyTrends.size() - 1);
            
            double trendSlope = (lastDay.getEfficiencyScore() - firstDay.getEfficiencyScore()) / 
                               (dailyTrends.size() - 1);
            
            trendAnalysis.setOverallTrend(trendSlope > 0 ? "上升" : trendSlope < 0 ? "下降" : "稳定");
            trendAnalysis.setTrendStrength(Math.abs(trendSlope));
        }
        
        return trendAnalysis;
    }
    
    /**
     * 生成改进建议
     */
    private List<String> generateImprovementSuggestions(ProductivityReport report) {
        List<String> suggestions = new ArrayList<>();
        
        // 基于各项指标分析生成改进建议
        if (report.getCodeGenerationAnalysis().getScore() < 70) {
            suggestions.add("代码生成效能较低，建议优化提示词质量，提高AI理解准确性");
        }
        
        if (report.getCodeQualityAnalysis().getScore() < 80) {
            suggestions.add("代码质量有待提升，建议加强代码审查，提高最佳实践遵循率");
        }
        
        if (report.getDevelopmentSpeedAnalysis().getScore() < 75) {
            suggestions.add("开发速度需要提升，建议优化工作流程，减少等待时间");
        }
        
        if (report.getProblemSolvingAnalysis().getScore() < 70) {
            suggestions.add("问题解决效能不足，建议提高问题描述质量，减少重试次数");
        }
        
        if (report.getLearningCurveAnalysis().getScore() < 60) {
            suggestions.add("学习曲线较陡，建议加强培训，提高工具使用熟练度");
        }
        
        if (report.getUserExperienceAnalysis().getScore() < 80) {
            suggestions.add("用户体验需要改善，建议优化界面设计，提高响应速度");
        }
        
        // 如果没有具体问题，提供通用建议
        if (suggestions.isEmpty()) {
            suggestions.add("整体效能表现良好，建议继续保持当前工作方式");
            suggestions.add("可以尝试探索更多AI功能，进一步提升开发效率");
        }
        
        return suggestions;
    }
    
    // 辅助计算方法
    private double calculateCodeGenerationScore(double linesGenerated, double acceptanceRate, double generationTime) {
        double score = 0.0;
        
        // 基于接受率
        score += acceptanceRate * 50;
        
        // 基于生成时间（越快越好）
        if (generationTime < 5000) score += 30;
        else if (generationTime < 15000) score += 20;
        else if (generationTime < 30000) score += 10;
        
        // 基于生成行数（适中最好）
        if (linesGenerated >= 5 && linesGenerated <= 50) score += 20;
        else if (linesGenerated > 0) score += 10;
        
        return Math.min(100, score);
    }
    
    private double calculateDevelopmentSpeedScore(double responseTime, double processingTime) {
        double score = 0.0;
        
        // 基于响应时间
        if (responseTime < 3000) score += 50;
        else if (responseTime < 8000) score += 30;
        else if (responseTime < 15000) score += 20;
        else if (responseTime < 30000) score += 10;
        
        // 基于处理时间
        if (processingTime < 10000) score += 50;
        else if (processingTime < 30000) score += 30;
        else if (processingTime < 60000) score += 20;
        else if (processingTime < 120000) score += 10;
        
        return Math.min(100, score / 2);
    }
    
    private double calculateCostEffectivenessScore(double totalTokenUsage, double totalCost) {
        double score = 100.0;
        
        // 基于token使用效率
        if (totalTokenUsage > 10000) score -= 20;
        if (totalTokenUsage > 50000) score -= 20;
        
        // 基于成本控制
        if (totalCost > 10.0) score -= 20;
        if (totalCost > 50.0) score -= 20;
        
        return Math.max(0, score);
    }
    
    private double calculateResponseTimeScore(double responseTime) {
        if (responseTime < 3000) return 100.0;
        else if (responseTime < 8000) return 80.0;
        else if (responseTime < 15000) return 60.0;
        else if (responseTime < 30000) return 40.0;
        else return 20.0;
    }
    
    /**
     * 创建错误报告
     */
    private ProductivityReport createErrorReport() {
        ProductivityReport report = new ProductivityReport();
        report.setAnalysisTime(analysisTime);
        report.setOverallScore(0.0);
        report.setErrorMessage("生成报告时发生错误，请检查数据完整性");
        return report;
    }
    
    // 内部数据类
    public static class BasicStats {
        private double successRate;
        private double averageResponseTime;
        private double minResponseTime;
        private double maxResponseTime;
        private double averageTokenUsage;
        private double totalTokenUsage;
        private double averageLinesGenerated;
        private double totalLinesGenerated;
        private double averageAcceptanceRate;
        
        // Getters and Setters
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public double getMinResponseTime() { return minResponseTime; }
        public void setMinResponseTime(double minResponseTime) { this.minResponseTime = minResponseTime; }
        
        public double getMaxResponseTime() { return maxResponseTime; }
        public void setMaxResponseTime(double maxResponseTime) { this.maxResponseTime = maxResponseTime; }
        
        public double getAverageTokenUsage() { return averageTokenUsage; }
        public void setAverageTokenUsage(double averageTokenUsage) { this.averageTokenUsage = averageTokenUsage; }
        
        public double getTotalTokenUsage() { return totalTokenUsage; }
        public void setTotalTokenUsage(double totalTokenUsage) { this.totalTokenUsage = totalTokenUsage; }
        
        public double getAverageLinesGenerated() { return averageLinesGenerated; }
        public void setAverageLinesGenerated(double averageLinesGenerated) { this.averageLinesGenerated = averageLinesGenerated; }
        
        public double getTotalLinesGenerated() { return totalLinesGenerated; }
        public void setTotalLinesGenerated(double totalLinesGenerated) { this.totalLinesGenerated = totalLinesGenerated; }
        
        public double getAverageAcceptanceRate() { return averageAcceptanceRate; }
        public void setAverageAcceptanceRate(double averageAcceptanceRate) { this.averageAcceptanceRate = averageAcceptanceRate; }
    }
    
    public static class MetricAnalysis {
        private final SoftwareEngineerMetrics metricType;
        private final Map<String, Double> metricValues;
        private double score;
        
        public MetricAnalysis(SoftwareEngineerMetrics metricType) {
            this.metricType = metricType;
            this.metricValues = new HashMap<>();
            this.score = 0.0;
        }
        
        public void setMetricValue(String key, double value) {
            metricValues.put(key, value);
        }
        
        public double getMetricValue(String key) {
            return metricValues.getOrDefault(key, 0.0);
        }
        
        public SoftwareEngineerMetrics getMetricType() { return metricType; }
        public Map<String, Double> getMetricValues() { return metricValues; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
    
    public static class TrendAnalysis {
        private List<DailyTrend> dailyTrends;
        private String overallTrend;
        private double trendStrength;
        
        public List<DailyTrend> getDailyTrends() { return dailyTrends; }
        public void setDailyTrends(List<DailyTrend> dailyTrends) { this.dailyTrends = dailyTrends; }
        
        public String getOverallTrend() { return overallTrend; }
        public void setOverallTrend(String overallTrend) { this.overallTrend = overallTrend; }
        
        public double getTrendStrength() { return trendStrength; }
        public void setTrendStrength(double trendStrength) { this.trendStrength = trendStrength; }
    }
    
    public static class DailyTrend {
        private String date;
        private double efficiencyScore;
        private int metricsCount;
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public double getEfficiencyScore() { return efficiencyScore; }
        public void setEfficiencyScore(double efficiencyScore) { this.efficiencyScore = efficiencyScore; }
        
        public int getMetricsCount() { return metricsCount; }
        public void setMetricsCount(int metricsCount) { this.metricsCount = metricsCount; }
    }
}
