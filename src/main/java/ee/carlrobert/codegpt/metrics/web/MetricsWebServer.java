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
                config.staticFiles.add(staticFiles -> {
                    staticFiles.directory = "/web";
                    staticFiles.location = Location.CLASSPATH;
                });
                config.showJavalinBanner = false;
            });
            
            configureRoutes();
            
            app.start(port);
            running = true;
            LOG.info("指标Web服务器已启动，端口: " + port);
        } catch (Exception e) {
            LOG.error("启动指标Web服务器失败", e);
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
            // 返回模拟的指标数据数组
            List<Map<String, Object>> metricsList = new ArrayList<>();
            
            // 添加一些模拟的指标数据
            for (int i = 1; i <= 5; i++) {
                Map<String, Object> metric = new HashMap<>();
                metric.put("id", "metric_" + i);
                metric.put("actionType", i % 2 == 0 ? "code_completion" : "chat");
                metric.put("modelName", "gpt-4");
                metric.put("linesGenerated", 10 + i * 5);
                metric.put("acceptanceRate", 85.0 + i * 2);
                metric.put("responseTime", 1000 + i * 200);
                metric.put("timestamp", System.currentTimeMillis() - i * 3600000);
                metric.put("successful", true);
                metric.put("userId", "user_" + (i % 3 + 1));
                metric.put("sessionId", "session_" + i);
                metric.put("programmingLanguage", i % 3 == 0 ? "java" : "kotlin");
                metric.put("projectType", "web_application");
                metric.put("userRating", 4 + (i % 2));
                
                metricsList.add(metric);
            }
            
            ctx.json(metricsList);
        } catch (Exception e) {
            LOG.error("获取指标数据失败", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }
    
    private void getMetricsSummary(Context ctx) {
        try {
            // 返回模拟的摘要数据
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRequests", 25);
            summary.put("successfulRequests", 23);
            summary.put("averageProcessingTime", 2.5);
            summary.put("averageAcceptanceRate", 87.5);
            summary.put("totalLinesGenerated", 150);
            summary.put("totalTimeSaved", 12.5);
            summary.put("averageEfficiencyGain", 35.2);
            
            // 动作类型统计
            Map<String, Integer> actionTypeCounts = new HashMap<>();
            actionTypeCounts.put("code_completion", 15);
            actionTypeCounts.put("chat", 8);
            actionTypeCounts.put("code_generation", 2);
            summary.put("actionTypeCounts", actionTypeCounts);
            
            // 编程语言统计
            Map<String, Integer> languageCounts = new HashMap<>();
            languageCounts.put("java", 12);
            languageCounts.put("kotlin", 8);
            languageCounts.put("python", 3);
            languageCounts.put("javascript", 2);
            summary.put("languageCounts", languageCounts);
            
            // 模型使用统计
            Map<String, Integer> modelCounts = new HashMap<>();
            modelCounts.put("gpt-4", 18);
            modelCounts.put("gpt-3.5-turbo", 5);
            modelCounts.put("claude-3", 2);
            summary.put("modelCounts", modelCounts);
            
            // 时间趋势数据（最近7天）
            List<Map<String, Object>> dailyStats = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                Map<String, Object> dayStat = new HashMap<>();
                dayStat.put("date", java.time.LocalDate.now().minusDays(i).toString());
                dayStat.put("requests", 3 + (i % 3));
                dayStat.put("linesGenerated", 20 + (i * 5));
                dayStat.put("timeSaved", 1.5 + (i * 0.3));
                dailyStats.add(dayStat);
            }
            summary.put("dailyStats", dailyStats);
            
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
}