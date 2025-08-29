package ee.carlrobert.codegpt.metrics.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * 指标Web服务器，提供REST API和Web界面用于可视化展示指标数据
 */
@Service
public final class MetricsWebServer {
    private static final Logger LOG = Logger.getInstance(MetricsWebServer.class);
    private static final int DEFAULT_PORT = 8090;
    
    private final Gson gson;
    private final Project project;
    private Javalin app;
    private int port = DEFAULT_PORT;
    private boolean running = false;
    
    public MetricsWebServer(Project project) {
        this.project = project;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    public static MetricsWebServer getInstance(Project project) {
        return project.getService(MetricsWebServer.class);
    }
    
    public synchronized void start() {
        if (running) {
            LOG.info("指标Web服务器已经在运行中");
            return;
        }
        
        try {
            app = Javalin.create(config -> {
                // 配置静态文件服务
                config.staticFiles.add(staticFiles -> {
                    staticFiles.directory = "/web";
                    staticFiles.location = Location.CLASSPATH;
                    staticFiles.hostedPath = "/";
                });
                config.showJavalinBanner = false;
                config.plugins.enableDevLogging(); // 启用开发日志
            });
            
            // 配置路由
            configureRoutes();
            
            // 启动服务器
            app.start(port);
            running = true;
            LOG.info("指标Web服务器已启动，端口: " + port);
            LOG.info("访问地址: " + getWebUrl());
            LOG.info("静态文件目录: /web");
            
        } catch (Exception e) {
            LOG.error("启动指标Web服务器失败: " + e.getMessage(), e);
            running = false;
            app = null;
        }
    }
    
    public synchronized void stop() {
        if (!running || app == null) {
            return;
        }
        
        try {
            app.stop();
            running = false;
            LOG.info("指标Web服务器已停止");
        } catch (Exception e) {
            LOG.error("停止指标Web服务器失败", e);
        }
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public String getWebUrl() {
        return "http://localhost:" + port;
    }
    
    public int getPort() {
        return port;
    }
    
    private void configureRoutes() {
        // API路由
        app.get("/api/metrics", this::getAllMetrics);
        app.get("/api/metrics/summary", this::getMetricsSummary);
        app.get("/api/metrics/actions", this::getActionTypes);
        app.get("/api/metrics/models", this::getModelNames);
        
        // 主页
        app.get("/", ctx -> ctx.redirect("/index.html"));
    }
    
    private void getAllMetrics(Context ctx) {
        try {
            // 从 MetricsCollector 获取真实的指标数据
            List<Map<String, Object>> metricsList = new ArrayList<>();
            
            try {
                // 优先从数据库读取数据，如果失败则从内存读取
                ee.carlrobert.codegpt.metrics.MetricsDatabaseManager dbManager = 
                    ee.carlrobert.codegpt.metrics.MetricsDatabaseManager.getInstance();
                
                // 尝试从数据库获取数据
                List<ee.carlrobert.codegpt.metrics.ProductivityMetrics> completedMetrics = 
                    loadMetricsFromDatabase(dbManager);
                
                // 如果数据库没有数据，则从内存获取
                if (completedMetrics.isEmpty()) {
                    ee.carlrobert.codegpt.metrics.MetricsCollector collector = 
                        ee.carlrobert.codegpt.metrics.MetricsCollector.getInstance(project);
                    completedMetrics = collector.getCompletedMetrics();
                    LOG.info("从内存获取指标数据，数量: " + completedMetrics.size());
                } else {
                    LOG.info("从数据库获取指标数据，数量: " + completedMetrics.size());
                }
                
                for (ee.carlrobert.codegpt.metrics.ProductivityMetrics metrics : completedMetrics) {
                    Map<String, Object> metric = new HashMap<>();
                    metric.put("id", metrics.getId());
                    metric.put("actionId", metrics.getActionId());
                    metric.put("actionType", metrics.getActionType());
                    metric.put("modelName", metrics.getModelName());
                    metric.put("linesGenerated", metrics.getLinesGenerated());
                    metric.put("linesAccepted", metrics.getLinesAccepted());
                    metric.put("acceptanceRate", metrics.getAcceptanceRate());
                    metric.put("responseTime", metrics.getResponseTime());
                    metric.put("timestamp", metrics.getStartTime());
                    metric.put("successful", metrics.isSuccessful());
                    metric.put("sessionId", metrics.getSessionId());
                    metric.put("programmingLanguage", metrics.getProgrammingLanguage());
                    metric.put("projectType", metrics.getProjectType());
                    metric.put("userRating", metrics.getUserRating());
                    metric.put("errorMessage", metrics.getErrorMessage());
                    metric.put("additionalData", metrics.getAdditionalData());
                    
                    metricsList.add(metric);
                }
                
                LOG.info("返回真实指标数据，数量: " + metricsList.size());
            } catch (Exception e) {
                LOG.warn("获取真实指标数据失败，返回空列表: " + e.getMessage());
            }
            
            ctx.json(metricsList);
        } catch (Exception e) {
            LOG.error("获取指标数据失败", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }
    
    private void getMetricsSummary(Context ctx) {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            try {
                ee.carlrobert.codegpt.metrics.MetricsCollector collector = 
                    ee.carlrobert.codegpt.metrics.MetricsCollector.getInstance(project);
                List<ee.carlrobert.codegpt.metrics.ProductivityMetrics> completedMetrics = 
                    collector.getCompletedMetrics();
                
                // 基础统计
                int totalRequests = completedMetrics.size();
                int successfulRequests = (int) completedMetrics.stream().mapToInt(m -> m.isSuccessful() ? 1 : 0).sum();
                double averageProcessingTime = completedMetrics.stream().mapToLong(m -> m.getResponseTime()).average().orElse(0.0) / 1000.0;
                double averageAcceptanceRate = completedMetrics.stream().mapToDouble(m -> m.getAcceptanceRate()).average().orElse(0.0);
                int totalLinesGenerated = completedMetrics.stream().mapToInt(m -> m.getLinesGenerated()).sum();
                
                summary.put("totalRequests", totalRequests);
                summary.put("successfulRequests", successfulRequests);
                summary.put("averageProcessingTime", averageProcessingTime);
                summary.put("averageAcceptanceRate", averageAcceptanceRate);
                summary.put("totalLinesGenerated", totalLinesGenerated);
                summary.put("totalTimeSaved", averageProcessingTime * totalRequests / 3600.0); // 估算节省时间
                summary.put("averageEfficiencyGain", averageAcceptanceRate);
                
                // 动作类型统计
                Map<String, Integer> actionTypeCounts = new HashMap<>();
                for (ee.carlrobert.codegpt.metrics.ProductivityMetrics metrics : completedMetrics) {
                    String actionType = metrics.getActionType();
                    if (actionType != null) {
                        actionTypeCounts.put(actionType, actionTypeCounts.getOrDefault(actionType, 0) + 1);
                    }
                }
                summary.put("actionTypeCounts", actionTypeCounts);
                
                // 编程语言统计
                Map<String, Integer> languageCounts = new HashMap<>();
                for (ee.carlrobert.codegpt.metrics.ProductivityMetrics metrics : completedMetrics) {
                    String language = metrics.getProgrammingLanguage();
                    if (language != null) {
                        languageCounts.put(language, languageCounts.getOrDefault(language, 0) + 1);
                    }
                }
                summary.put("languageCounts", languageCounts);
                
                // 模型使用统计
                Map<String, Integer> modelCounts = new HashMap<>();
                for (ee.carlrobert.codegpt.metrics.ProductivityMetrics metrics : completedMetrics) {
                    String modelName = metrics.getModelName();
                    if (modelName != null) {
                        modelCounts.put(modelName, modelCounts.getOrDefault(modelName, 0) + 1);
                    }
                }
                summary.put("modelCounts", modelCounts);
                
                // 时间趋势数据（最近7天）
                List<Map<String, Object>> dailyStats = new ArrayList<>();
                for (int i = 6; i >= 0; i--) {
                    String date = java.time.LocalDate.now().minusDays(i).toString();
                    Map<String, Object> dayStat = new HashMap<>();
                    dayStat.put("date", date);
                    
                    // 计算当天的统计数据
                    long dayStart = java.time.LocalDate.now().minusDays(i).atStartOfDay()
                        .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                    long dayEnd = dayStart + 24 * 60 * 60 * 1000;
                    
                    int dayRequests = 0;
                    int dayLinesGenerated = 0;
                    double dayTimeSaved = 0.0;
                    
                    for (ee.carlrobert.codegpt.metrics.ProductivityMetrics metrics : completedMetrics) {
                        if (metrics.getStartTime() >= dayStart && metrics.getStartTime() < dayEnd) {
                            dayRequests++;
                            dayLinesGenerated += metrics.getLinesGenerated();
                            dayTimeSaved += metrics.getResponseTime() / 1000.0 / 3600.0;
                        }
                    }
                    
                    dayStat.put("requests", dayRequests);
                    dayStat.put("linesGenerated", dayLinesGenerated);
                    dayStat.put("timeSaved", dayTimeSaved);
                    dailyStats.add(dayStat);
                }
                summary.put("dailyStats", dailyStats);
                
                LOG.info("返回真实摘要数据，总请求数: " + totalRequests);
            } catch (Exception e) {
                LOG.warn("获取真实摘要数据失败，返回默认值: " + e.getMessage());
                // 返回默认值
                summary.put("totalRequests", 0);
                summary.put("successfulRequests", 0);
                summary.put("averageProcessingTime", 0.0);
                summary.put("averageAcceptanceRate", 0.0);
                summary.put("totalLinesGenerated", 0);
                summary.put("totalTimeSaved", 0.0);
                summary.put("averageEfficiencyGain", 0.0);
                summary.put("actionTypeCounts", new HashMap<>());
                summary.put("languageCounts", new HashMap<>());
                summary.put("modelCounts", new HashMap<>());
                summary.put("dailyStats", new ArrayList<>());
            }
            
            ctx.json(summary);
        } catch (Exception e) {
            LOG.error("获取指标摘要失败", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }
    
    private void getActionTypes(Context ctx) {
        try {
            // 返回预定义的操作类型
            String[] actionTypes = {"code_completion", "chat", "code_generation", "refactoring"};
            ctx.json(actionTypes);
        } catch (Exception e) {
            LOG.error("获取操作类型失败", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }
    
    private void getModelNames(Context ctx) {
        try {
            // 返回预定义的模型名称
            String[] modelNames = {"gpt-4", "gpt-3.5-turbo", "claude-3", "gemini-pro"};
            ctx.json(modelNames);
        } catch (Exception e) {
            LOG.error("获取模型名称失败", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 从数据库加载指标数据
     */
    private List<ee.carlrobert.codegpt.metrics.ProductivityMetrics> loadMetricsFromDatabase(
            ee.carlrobert.codegpt.metrics.MetricsDatabaseManager dbManager) {
        
        List<ee.carlrobert.codegpt.metrics.ProductivityMetrics> metricsList = new ArrayList<>();
        
        try {
            // 检查数据库管理器是否有效
            if (dbManager == null) {
                LOG.warn("数据库管理器为空，无法加载数据");
                return metricsList;
            }
            
            LOG.info("尝试从数据库加载指标数据...");
            
            // 尝试从数据库加载数据
            // 由于MetricsDatabaseManager目前只支持保存功能，我们暂时返回空列表
            // 这样可以避免编译错误，同时为后续功能扩展预留接口
            
            // 注意：这里返回空列表是临时的解决方案
            // 当需要从数据库读取数据时，可以：
            // 1. 在MetricsDatabaseManager中添加loadAllMetrics()方法
            // 2. 或者直接在这里实现数据库查询逻辑
            
            LOG.info("数据库加载完成，返回空列表（数据库查询功能待实现）");
            
        } catch (Exception e) {
            LOG.warn("从数据库加载指标数据失败: " + e.getMessage(), e);
        }
        
        return metricsList;
    }
}