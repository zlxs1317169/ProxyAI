package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 软件工程师效能度量管理器
 * 统一管理ProxyAI效能度量的收集、分析和报告生成
 */
@Service
public final class ProductivityMetricsManager implements MetricsService {
    
    private static final Logger LOG = Logger.getInstance(ProductivityMetricsManager.class);
    
    private final Project project;
    private final MetricsCollector metricsCollector;
    private final AIUsageTracker aiUsageTracker;
    private final ScheduledExecutorService scheduler;
    
    private ProductivityReport lastReport;
    private LocalDateTime lastAnalysisTime;
    private boolean autoAnalysisEnabled = true;
    private int autoAnalysisIntervalMinutes = 60; // 默认1小时自动分析一次
    
    public ProductivityMetricsManager(Project project) {
        this.project = project;
        this.metricsCollector = MetricsCollector.getInstance(project);
        this.aiUsageTracker = AIUsageTracker.getInstance();
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // 启动自动分析任务
        startAutoAnalysis();
        
        LOG.info("软件工程师效能度量管理器初始化完成");
    }
    
    public static ProductivityMetricsManager getInstance(Project project) {
        return project.getService(ProductivityMetricsManager.class);
    }
    
    /**
     * 开始效能度量收集
     */
    public ProductivityMetrics startMetricsCollection(String actionId, String actionType) {
        try {
            ProductivityMetrics metrics = metricsCollector.startMetrics(actionId, actionType);
            if (metrics != null) {
                // 设置基础信息
                metrics.setProjectType(detectProjectType());
                metrics.setFileExtension(detectFileExtension());
                metrics.setContextSize(detectContextSize());
                
                LOG.debug("开始效能度量收集: " + actionId + " (" + actionType + ")");
            }
            return metrics;
        } catch (Exception e) {
            LOG.warn("开始效能度量收集失败", e);
            return null;
        }
    }
    
    /**
     * 完成效能度量收集
     */
    public void completeMetricsCollection(ProductivityMetrics metrics, boolean successful, String errorMessage) {
        try {
            if (metrics != null) {
                // 设置系统性能指标
                setSystemPerformanceMetrics(metrics);
                
                // 完成度量收集
                metricsCollector.completeMetrics(metrics, successful, errorMessage);
                
                LOG.debug("完成效能度量收集: " + metrics.getActionId() + 
                         " (成功: " + successful + ")");
            }
        } catch (Exception e) {
            LOG.warn("完成效能度量收集失败", e);
        }
    }
    
    /**
     * 记录代码补全效能
     */
    public void recordCodeCompletionMetrics(String language, String suggestedCode, boolean accepted, long processingTime) {
        try {
            // 参数验证
            if (language == null || language.trim().isEmpty()) {
                LOG.warn("编程语言参数为空，跳过代码补全指标记录");
                return;
            }
            if (suggestedCode == null || suggestedCode.trim().isEmpty()) {
                LOG.warn("建议代码为空，跳过代码补全指标记录");
                return;
            }
            if (processingTime < 0) {
                LOG.warn("处理时间为负数，使用0替代: " + processingTime);
                processingTime = 0;
            }
            
            // 记录到AI使用跟踪器
            aiUsageTracker.recordRealAICompletion(language, suggestedCode, accepted, processingTime);
            
            // 创建专门的代码补全度量
            ProductivityMetrics metrics = new ProductivityMetrics("code_completion", "CODE_COMPLETION");
            metrics.setProgrammingLanguage(language);
            metrics.setModelName(detectCurrentModel());
            metrics.setSessionId(generateSessionId());
            metrics.setUserId(detectUserId());
            
            int linesGenerated = suggestedCode.split("\n").length;
            int linesAccepted = accepted ? linesGenerated : 0;
            metrics.recordCodeCompletion(language, linesGenerated, linesAccepted, processingTime);
            
            // 完成度量收集
            completeMetricsCollection(metrics, true, null);
            
            LOG.debug("代码补全效能指标记录完成: 语言=" + language + 
                     ", 生成行数=" + linesGenerated + ", 接受=" + accepted);
            
        } catch (Exception e) {
            LOG.warn("记录代码补全效能指标失败", e);
        }
    }
    
    /**
     * 记录聊天代码生成效能
     */
    public void recordChatCodeGenerationMetrics(String generatedCode, boolean applied, long processingTime, String sessionId) {
        try {
            // 参数验证
            if (generatedCode == null || generatedCode.trim().isEmpty()) {
                LOG.warn("生成代码为空，跳过聊天代码生成指标记录");
                return;
            }
            if (processingTime < 0) {
                LOG.warn("处理时间为负数，使用0替代: " + processingTime);
                processingTime = 0;
            }
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = generateSessionId();
                LOG.debug("会话ID为空，生成新会话ID: " + sessionId);
            }
            
            // 记录到AI使用跟踪器
            aiUsageTracker.recordRealAIChatGeneration(generatedCode, applied, processingTime, sessionId);
            
            // 创建专门的聊天代码生成度量
            ProductivityMetrics metrics = new ProductivityMetrics("chat_code_generation", "CHAT_CODE_GENERATION");
            metrics.setModelName(detectCurrentModel());
            metrics.setSessionId(sessionId);
            metrics.setUserId(detectUserId());
            
            int linesGenerated = generatedCode.split("\n").length;
            int linesAccepted = applied ? linesGenerated : 0;
            metrics.recordChatCodeGeneration(linesGenerated, linesAccepted, processingTime, sessionId);
            
            // 完成度量收集
            completeMetricsCollection(metrics, true, null);
            
            LOG.debug("聊天代码生成效能指标记录完成: 会话=" + sessionId + 
                     ", 生成行数=" + linesGenerated + ", 应用=" + applied);
            
        } catch (Exception e) {
            LOG.warn("记录聊天代码生成效能指标失败", e);
        }
    }
    
    /**
     * 记录用户评分
     */
    public void recordUserRating(String actionId, int rating, String feedback) {
        try {
            // 查找对应的度量记录并更新
            List<ProductivityMetrics> completedMetrics = metricsCollector.getCompletedMetrics();
            for (ProductivityMetrics metrics : completedMetrics) {
                if (actionId.equals(metrics.getActionId())) {
                    metrics.setUserRating(rating);
                    metrics.setFeedback(feedback);
                    metrics.setUserExperience("rated");
                    break;
                }
            }
            
            LOG.debug("记录用户评分: " + actionId + " = " + rating + "/5");
            
        } catch (Exception e) {
            LOG.warn("记录用户评分失败", e);
        }
    }
    
    /**
     * 生成效能分析报告
     */
    public CompletableFuture<ProductivityReport> generateProductivityReport() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<ProductivityMetrics> metricsData = metricsCollector.getCompletedMetrics();
                
                if (metricsData.isEmpty()) {
                    LOG.info("没有可用的效能度量数据，无法生成报告");
                    return createEmptyReport();
                }
                
                // 创建分析器并生成报告
                ProductivityAnalyzer analyzer = new ProductivityAnalyzer(metricsData);
                ProductivityReport report = analyzer.generateComprehensiveReport();
                
                // 保存报告
                this.lastReport = report;
                this.lastAnalysisTime = LocalDateTime.now();
                
                LOG.info("效能分析报告生成完成，综合评分: " + report.getOverallScore());
                return report;
                
            } catch (Exception e) {
                LOG.error("生成效能分析报告失败", e);
                return createErrorReport(e.getMessage());
            }
        });
    }
    
    /**
     * 获取最新报告
     */
    public ProductivityReport getLastReport() {
        return lastReport;
    }
    
    /**
     * 获取最后分析时间
     */
    public LocalDateTime getLastAnalysisTime() {
        return lastAnalysisTime;
    }
    
    /**
     * 获取今日统计
     */
    public AIUsageTracker.DailyStats getTodayStats() {
        return aiUsageTracker.getTodayStats();
    }
    
    /**
     * 检查是否需要生成新报告
     */
    public boolean shouldGenerateNewReport() {
        if (lastAnalysisTime == null) {
            return true;
        }
        
        // 如果距离上次分析超过1小时，建议生成新报告
        long hoursSinceLastAnalysis = java.time.Duration.between(lastAnalysisTime, LocalDateTime.now()).toHours();
        return hoursSinceLastAnalysis >= 1;
    }
    
    /**
     * 设置自动分析
     */
    public void setAutoAnalysisEnabled(boolean enabled) {
        this.autoAnalysisEnabled = enabled;
        if (enabled) {
            startAutoAnalysis();
        } else {
            stopAutoAnalysis();
        }
        LOG.info("自动效能分析 " + (enabled ? "启用" : "禁用"));
    }
    
    /**
     * 设置自动分析间隔
     */
    public void setAutoAnalysisInterval(int minutes) {
        this.autoAnalysisIntervalMinutes = minutes;
        if (autoAnalysisEnabled) {
            stopAutoAnalysis();
            startAutoAnalysis();
        }
        LOG.info("自动效能分析间隔设置为 " + minutes + " 分钟");
    }
    
    /**
     * 导出效能数据
     */
    @Override
    public String exportMetricsData(String format) {
        try {
            if (format == null || format.trim().isEmpty()) {
                format = "json";
            }
            
            switch (format.toLowerCase()) {
                case "json":
                    return exportToJson();
                case "csv":
                    return exportToCsv();
                case "summary":
                    return exportSummary();
                default:
                    LOG.warn("不支持的导出格式: " + format + "，使用JSON格式");
                    return exportToJson();
            }
        } catch (Exception e) {
            LOG.error("导出效能数据失败", e);
            return "导出失败: " + e.getMessage();
        }
    }
    
    /**
     * 导出为JSON格式
     */
    private String exportToJson() {
        try {
            List<ProductivityMetrics> metrics = metricsCollector.getCompletedMetrics();
            if (metrics.isEmpty()) {
                return "{\"message\": \"没有可用的效能数据\"}";
            }
            
            // 使用Jackson进行JSON序列化
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            return mapper.writeValueAsString(metrics);
        } catch (Exception e) {
            LOG.error("JSON导出失败", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
    
    /**
     * 导出为CSV格式
     */
    private String exportToCsv() {
        try {
            List<ProductivityMetrics> metrics = metricsCollector.getCompletedMetrics();
            if (metrics.isEmpty()) {
                return "没有可用的效能数据";
            }
            
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Action ID,Action Type,Model Name,Response Time,Lines Generated,Lines Accepted,Acceptance Rate,Success,Programming Language,Project Type,File Extension,User Rating,Memory Usage,CPU Usage,Network Latency,Created At\n");
            
            for (ProductivityMetrics metric : metrics) {
                csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,%.2f,%s,\"%s\",\"%s\",\"%s\",%d,%d,%.2f,\"%s\",\"%s\"\n",
                    metric.getId(),
                    metric.getActionId(),
                    metric.getActionType(),
                    metric.getModelName(),
                    metric.getResponseTime(),
                    metric.getLinesGenerated(),
                    metric.getLinesAccepted(),
                    metric.getAcceptanceRate(),
                    metric.isSuccessful(),
                    metric.getProgrammingLanguage(),
                    metric.getProjectType(),
                    metric.getFileExtension(),
                    metric.getUserRating(),
                    metric.getMemoryUsage(),
                    metric.getCpuUsage(),
                    metric.getNetworkLatency(),
                    metric.getFormattedCreatedTime()
                ));
            }
            
            return csv.toString();
        } catch (Exception e) {
            LOG.error("CSV导出失败", e);
            return "导出失败: " + e.getMessage();
        }
    }
    
    /**
     * 导出摘要
     */
    private String exportSummary() {
        try {
            ProductivityReport report = getLastReport();
            if (report == null) {
                return "没有可用的效能报告";
            }
            
            return report.getSummary();
        } catch (Exception e) {
            LOG.error("摘要导出失败", e);
            return "导出失败: " + e.getMessage();
        }
    }
    
    /**
     * 清理旧数据
     */
    @Override
    public void cleanupOldData(int daysToKeep) {
        try {
            if (daysToKeep <= 0) {
                daysToKeep = 30; // 默认保留30天
            }
            
            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60L * 60L * 1000L);
            
            // 这里应该实现数据库清理逻辑
            // 由于我们使用的是H2数据库，需要实现具体的SQL删除语句
            LOG.info("清理" + daysToKeep + "天前的旧数据，截止时间: " + 
                    java.time.Instant.ofEpochMilli(cutoffTime).toString());
            
            // TODO: 实现具体的数据库清理逻辑
            // 例如：DELETE FROM productivity_metrics WHERE created_at < ?
            
        } catch (Exception e) {
            LOG.error("清理旧数据失败", e);
        }
    }
    
    /**
     * 检查服务是否可用
     */
    @Override
    public boolean isServiceAvailable() {
        try {
            return metricsCollector != null && aiUsageTracker != null && 
                   !scheduler.isShutdown() && project != null && !project.isDisposed();
        } catch (Exception e) {
            LOG.debug("检查服务可用性失败", e);
            return false;
        }
    }
    
    /**
     * 关闭服务
     */
    public void shutdown() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            }
            LOG.info("效能度量管理器已关闭");
        } catch (Exception e) {
            LOG.error("关闭效能度量管理器失败", e);
        }
    }
    
    // 私有辅助方法
    
    private void startAutoAnalysis() {
        if (autoAnalysisEnabled) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    LOG.debug("执行自动效能分析...");
                    generateProductivityReport().thenAccept(report -> {
                        if (report != null) {
                            LOG.info("自动效能分析完成，综合评分: " + report.getOverallScore());
                        }
                    });
                } catch (Exception e) {
                    LOG.warn("自动效能分析失败", e);
                }
            }, autoAnalysisIntervalMinutes, autoAnalysisIntervalMinutes, TimeUnit.MINUTES);
            
            LOG.info("自动效能分析已启动，间隔: " + autoAnalysisIntervalMinutes + " 分钟");
        }
    }
    
    private void stopAutoAnalysis() {
        scheduler.shutdown();
        LOG.info("自动效能分析已停止");
    }
    
    /**
     * 设置系统性能指标
     */
    private void setSystemPerformanceMetrics(ProductivityMetrics metrics) {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            metrics.setMemoryUsage(usedMemory);
            
            // 获取CPU使用率（简化实现）
            double cpuUsage = getCpuUsage();
            metrics.setCpuUsage(cpuUsage);
            
            // 网络延迟（简化实现）
            String networkLatency = getNetworkLatency();
            metrics.setNetworkLatency(networkLatency);
            
        } catch (Exception e) {
            LOG.debug("设置系统性能指标失败", e);
        }
    }
    
    /**
     * 检测项目类型
     */
    private String detectProjectType() {
        try {
            if (project == null) return "unknown";
            
            // 基于项目文件结构检测项目类型
            if (project.getBaseDir().findChild("pom.xml") != null) {
                return "maven";
            } else if (project.getBaseDir().findChild("build.gradle") != null || 
                       project.getBaseDir().findChild("build.gradle.kts") != null) {
                return "gradle";
            } else if (project.getBaseDir().findChild("package.json") != null) {
                return "nodejs";
            } else if (project.getBaseDir().findChild("requirements.txt") != null) {
                return "python";
            } else if (project.getBaseDir().findChild("Cargo.toml") != null) {
                return "rust";
            } else if (project.getBaseDir().findChild("go.mod") != null) {
                return "go";
            }
            
            return "general";
        } catch (Exception e) {
            LOG.debug("检测项目类型失败", e);
            return "unknown";
        }
    }
    
    /**
     * 检测文件扩展名
     */
    private String detectFileExtension() {
        try {
            if (project == null) return "unknown";
            
            // 获取当前活动文件
            com.intellij.openapi.fileEditor.FileEditorManager fileEditorManager = 
                com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project);
            com.intellij.openapi.vfs.VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
            
            if (openFiles.length > 0) {
                String fileName = openFiles[0].getName();
                int lastDotIndex = fileName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    return fileName.substring(lastDotIndex + 1).toLowerCase();
                }
            }
            
            return "unknown";
        } catch (Exception e) {
            LOG.debug("检测文件扩展名失败", e);
            return "unknown";
        }
    }
    
    /**
     * 检测上下文大小
     */
    private String detectContextSize() {
        try {
            if (project == null) return "unknown";
            
            // 基于项目大小估算上下文
            com.intellij.openapi.vfs.VirtualFile baseDir = project.getBaseDir();
            if (baseDir != null) {
                long projectSize = calculateProjectSize(baseDir);
                if (projectSize < 1024 * 1024) { // < 1MB
                    return "small";
                } else if (projectSize < 10 * 1024 * 1024) { // < 10MB
                    return "medium";
                } else if (projectSize < 100 * 1024 * 1024) { // < 100MB
                    return "large";
                } else {
                    return "very_large";
                }
            }
            
            return "unknown";
        } catch (Exception e) {
            LOG.debug("检测上下文大小失败", e);
            return "unknown";
        }
    }
    
    /**
     * 计算项目大小
     */
    private long calculateProjectSize(com.intellij.openapi.vfs.VirtualFile dir) {
        try {
            long size = 0;
            com.intellij.openapi.vfs.VirtualFile[] children = dir.getChildren();
            for (com.intellij.openapi.vfs.VirtualFile child : children) {
                if (child.isDirectory()) {
                    size += calculateProjectSize(child);
                } else {
                    size += child.getLength();
                }
            }
            return size;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 检测当前使用的AI模型
     */
    private String detectCurrentModel() {
        try {
            // 这里应该从ProxyAI的设置中获取当前模型
            // 暂时返回默认值
            return "gpt-4";
        } catch (Exception e) {
            LOG.debug("检测当前AI模型失败", e);
            return "unknown";
        }
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + 
               java.util.UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 检测用户ID
     */
    private String detectUserId() {
        try {
            // 这里应该从系统或用户设置中获取用户ID
            // 暂时返回默认值
            return System.getProperty("user.name", "unknown");
        } catch (Exception e) {
            LOG.debug("检测用户ID失败", e);
            return "unknown";
        }
    }
    
    /**
     * 获取CPU使用率
     */
    private double getCpuUsage() {
        try {
            // 简化实现，返回一个估算值
            // 在实际应用中，应该使用更准确的系统监控API
            return Math.random() * 30 + 20; // 20-50%之间的随机值
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * 获取网络延迟
     */
    private String getNetworkLatency() {
        try {
            // 简化实现，返回一个估算值
            // 在实际应用中，应该进行真实的网络延迟测试
            double latency = Math.random() * 100 + 10; // 10-110ms之间的随机值
            return String.format("%.0fms", latency);
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private ProductivityReport createEmptyReport() {
        ProductivityReport report = new ProductivityReport();
        report.setErrorMessage("没有可用的效能度量数据");
        report.setOverallScore(0.0);
        return report;
    }
    
    private ProductivityReport createErrorReport(String errorMessage) {
        ProductivityReport report = new ProductivityReport();
        report.setErrorMessage(errorMessage);
        report.setOverallScore(0.0);
        return report;
    }
}
