package ee.carlrobert.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.metrics.web.MetricsWebServer;
import ee.carlrobert.codegpt.settings.metrics.MetricsSettings;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Web服务器监控Action
 * 实时监控Web服务器状态并提供自动修复功能
 */
public class WebServerMonitorAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            showNotification("监控失败", "无法获取项目实例", NotificationType.ERROR);
            return;
        }

        monitorAndFixWebServer(project);
    }

    private void monitorAndFixWebServer(Project project) {
        StringBuilder report = new StringBuilder();
        report.append("=== Web服务器监控报告 ===\n\n");

        try {
            // 1. 检查设置
            report.append("1. 检查设置配置:\n");
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings != null) {
                report.append("   ✅ MetricsSettings 获取成功\n");
                report.append("   - Web服务器启用: ").append(settings.isWebServerEnabled()).append("\n");
                report.append("   - Web服务器端口: ").append(settings.getWebServerPort()).append("\n");
            } else {
                report.append("   ❌ MetricsSettings 获取失败\n");
            }

            // 2. 检查Web服务器实例
            report.append("\n2. 检查Web服务器实例:\n");
            MetricsWebServer webServer = MetricsWebServer.getInstance(project);
            if (webServer != null) {
                report.append("   ✅ Web服务器实例获取成功\n");
                report.append("   - 运行状态: ").append(webServer.isRunning()).append("\n");
                report.append("   - 当前端口: ").append(webServer.getPort()).append("\n");
                report.append("   - 访问地址: ").append(webServer.getWebUrl()).append("\n");
            } else {
                report.append("   ❌ Web服务器实例获取失败\n");
            }

            // 3. 检查端口占用
            report.append("\n3. 检查端口占用:\n");
            int port = settings != null ? settings.getWebServerPort() : 8090;
            boolean portAvailable = isPortAvailable(port);
            if (portAvailable) {
                report.append("   ✅ 端口 ").append(port).append(" 可用\n");
            } else {
                report.append("   ❌ 端口 ").append(port).append(" 被占用\n");
            }

            // 4. 检查Web服务器响应
            report.append("\n4. 检查Web服务器响应:\n");
            if (webServer != null && webServer.isRunning()) {
                boolean responseOk = testWebServerResponse(webServer.getWebUrl());
                if (responseOk) {
                    report.append("   ✅ Web服务器响应正常\n");
                } else {
                    report.append("   ❌ Web服务器响应异常\n");
                }
            } else {
                report.append("   ⚠️ Web服务器未运行，无法测试响应\n");
            }

            // 5. 自动修复尝试
            report.append("\n5. 自动修复尝试:\n");
            if (webServer != null && !webServer.isRunning()) {
                try {
                    report.append("   🔧 尝试启动Web服务器...\n");
                    webServer.start();
                    
                    if (webServer.isRunning()) {
                        report.append("   ✅ Web服务器启动成功\n");
                        
                        // 等待一下再测试响应
                        Thread.sleep(2000);
                        boolean responseOk = testWebServerResponse(webServer.getWebUrl());
                        if (responseOk) {
                            report.append("   ✅ 启动后响应测试成功\n");
                        } else {
                            report.append("   ⚠️ 启动后响应测试失败\n");
                        }
                    } else {
                        report.append("   ❌ Web服务器启动失败\n");
                    }
                } catch (Exception e) {
                    report.append("   ❌ 启动异常: ").append(e.getMessage()).append("\n");
                }
            } else if (webServer != null && webServer.isRunning()) {
                report.append("   ℹ️ Web服务器已在运行中\n");
            }

            // 6. 最终状态检查
            report.append("\n6. 最终状态检查:\n");
            if (webServer != null) {
                report.append("   - 运行状态: ").append(webServer.isRunning()).append("\n");
                if (webServer.isRunning()) {
                    report.append("   - 访问地址: ").append(webServer.getWebUrl()).append("\n");
                    report.append("   - 主页: ").append(webServer.getWebUrl()).append("/\n");
                    report.append("   - API: ").append(webServer.getWebUrl()).append("/api/metrics\n");
                }
            }

        } catch (Exception e) {
            report.append("\n❌ 监控过程中发生错误: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        // 显示监控结果
        showNotification("Web服务器监控完成", report.toString(), NotificationType.INFORMATION);
        
        // 在控制台输出详细报告
        System.out.println(report.toString());
    }

    private boolean isPortAvailable(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 1000);
            return false; // 端口被占用
        } catch (Exception e) {
            return true; // 端口可用
        }
    }

    private boolean testWebServerResponse(String baseUrl) {
        try {
            // 测试主页
            URL url = new URL(baseUrl + "/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200 || responseCode == 302) { // 200 OK 或 302 Redirect
                return true;
            }
            
            // 测试API接口
            url = new URL(baseUrl + "/api/metrics");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            responseCode = connection.getResponseCode();
            return responseCode == 200;
            
        } catch (Exception e) {
            return false;
        }
    }

    private void showNotification(String title, String content, NotificationType type) {
        Notifications.Bus.notify(
            new Notification(
                "proxyai.notification.group",
                title,
                content,
                type
            )
        );
    }
}
