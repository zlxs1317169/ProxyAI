package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import ee.carlrobert.codegpt.metrics.web.MetricsWebServer;
import ee.carlrobert.codegpt.settings.metrics.MetricsSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Web服务器启动活动
 * 在项目启动时自动启动指标Web服务器
 */
public class WebServerStartupActivity implements StartupActivity {
    
    private static final Logger LOG = Logger.getInstance(WebServerStartupActivity.class);
    
    @Override
    public void runActivity(@NotNull Project project) {
        try {
            // 延迟启动Web服务器，避免影响IDE启动速度
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    // 等待一段时间确保其他服务已初始化
                    Thread.sleep(3000);
                    
                    initializeWebServer(project);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.warn("Web服务器启动被中断", e);
                } catch (Exception e) {
                    LOG.error("启动Web服务器时发生错误", e);
                }
            });
            
        } catch (Exception e) {
            LOG.error("启动Web服务器活动时发生错误", e);
        }
    }
    
    /**
     * 初始化Web服务器
     */
    private void initializeWebServer(Project project) {
        try {
            LOG.info("🌐 初始化指标Web服务器...");
            
            // 检查设置是否启用Web服务器
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings != null && !settings.isWebServerEnabled()) {
                LOG.info("⚠️ Web服务器已禁用，跳过初始化");
                return;
            }
            
            // 获取Web服务器实例
            MetricsWebServer webServer = MetricsWebServer.getInstance(project);
            if (webServer != null) {
                // 设置端口（从设置中读取或使用默认值）
                int port = settings != null ? settings.getWebServerPort() : 8090;
                webServer.setPort(port);
                
                // 启动Web服务器
                webServer.start();
                
                if (webServer.isRunning()) {
                    LOG.info("✅ Web服务器启动成功，端口: " + port);
                    LOG.info("🌐 访问地址: " + webServer.getWebUrl());
                    LOG.info("📊 指标数据API: " + webServer.getWebUrl() + "/api/metrics");
                    LOG.info("📈 数据摘要: " + webServer.getWebUrl() + "/api/metrics/summary");
                    
                    // 在控制台输出访问信息
                    System.out.println("🎉 ProxyAI Web服务器启动成功!");
                    System.out.println("🌐 访问地址: " + webServer.getWebUrl());
                    System.out.println("📊 指标数据: " + webServer.getWebUrl() + "/api/metrics");
                    System.out.println("📈 数据摘要: " + webServer.getWebUrl() + "/api/metrics/summary");
                    System.out.println("🔧 动作类型: " + webServer.getWebUrl() + "/api/metrics/actions");
                    System.out.println("🤖 模型使用: " + webServer.getWebUrl() + "/api/metrics/models");
                    
                } else {
                    LOG.error("❌ Web服务器启动失败");
                    System.err.println("❌ Web服务器启动失败");
                }
            } else {
                LOG.error("❌ 无法获取Web服务器实例");
                System.err.println("❌ 无法获取Web服务器实例");
            }
            
        } catch (Exception e) {
            LOG.error("初始化Web服务器时发生错误", e);
            System.err.println("❌ 初始化Web服务器时发生错误: " + e.getMessage());
        }
    }
}
