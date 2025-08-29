package ee.carlrobert.codegpt.actions;

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

/**
 * 诊断Web服务器状态的Action
 */
public class DiagnoseWebServerAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            showNotification("诊断失败", "无法获取项目实例", NotificationType.ERROR);
            return;
        }

        diagnoseWebServer(project);
    }

    private void diagnoseWebServer(Project project) {
        StringBuilder diagnosis = new StringBuilder();
        diagnosis.append("=== Web服务器诊断报告 ===\n\n");

        try {
            // 1. 检查设置
            diagnosis.append("1. 检查设置配置:\n");
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings != null) {
                diagnosis.append("   ✅ MetricsSettings 获取成功\n");
                diagnosis.append("   - Web服务器启用: ").append(settings.isWebServerEnabled()).append("\n");
                diagnosis.append("   - Web服务器端口: ").append(settings.getWebServerPort()).append("\n");
            } else {
                diagnosis.append("   ❌ MetricsSettings 获取失败\n");
            }

            // 2. 检查Web服务器实例
            diagnosis.append("\n2. 检查Web服务器实例:\n");
            MetricsWebServer webServer = MetricsWebServer.getInstance(project);
            if (webServer != null) {
                diagnosis.append("   ✅ Web服务器实例获取成功\n");
                diagnosis.append("   - 运行状态: ").append(webServer.isRunning()).append("\n");
                diagnosis.append("   - 当前端口: ").append(webServer.getPort()).append("\n");
                diagnosis.append("   - 访问地址: ").append(webServer.getWebUrl()).append("\n");
            } else {
                diagnosis.append("   ❌ Web服务器实例获取失败\n");
            }

            // 3. 检查端口占用
            diagnosis.append("\n3. 检查端口占用:\n");
            int port = settings != null ? settings.getWebServerPort() : 8090;
            if (isPortAvailable(port)) {
                diagnosis.append("   ✅ 端口 ").append(port).append(" 可用\n");
            } else {
                diagnosis.append("   ❌ 端口 ").append(port).append(" 被占用\n");
            }

            // 4. 尝试启动Web服务器
            diagnosis.append("\n4. 尝试启动Web服务器:\n");
            if (webServer != null && !webServer.isRunning()) {
                try {
                    webServer.start();
                    if (webServer.isRunning()) {
                        diagnosis.append("   ✅ Web服务器启动成功\n");
                        diagnosis.append("   - 访问地址: ").append(webServer.getWebUrl()).append("\n");
                        diagnosis.append("   - API接口: ").append(webServer.getWebUrl()).append("/api/metrics").append("\n");
                    } else {
                        diagnosis.append("   ❌ Web服务器启动失败\n");
                    }
                } catch (Exception e) {
                    diagnosis.append("   ❌ Web服务器启动异常: ").append(e.getMessage()).append("\n");
                }
            } else if (webServer != null && webServer.isRunning()) {
                diagnosis.append("   ℹ️ Web服务器已在运行中\n");
            }

            // 5. 测试连接
            diagnosis.append("\n5. 测试连接:\n");
            if (webServer != null && webServer.isRunning()) {
                if (testWebServerConnection(webServer.getWebUrl())) {
                    diagnosis.append("   ✅ Web服务器连接测试成功\n");
                } else {
                    diagnosis.append("   ❌ Web服务器连接测试失败\n");
                }
            }

        } catch (Exception e) {
            diagnosis.append("\n❌ 诊断过程中发生错误: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        // 显示诊断结果
        showNotification("Web服务器诊断完成", diagnosis.toString(), NotificationType.INFORMATION);
        
        // 在控制台输出详细诊断信息
        System.out.println(diagnosis.toString());
    }

    private boolean isPortAvailable(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 1000);
            return false; // 端口被占用
        } catch (Exception e) {
            return true; // 端口可用
        }
    }

    private boolean testWebServerConnection(String url) {
        try {
            // 简单的连接测试
            String host = url.replace("http://", "").split(":")[0];
            int port = Integer.parseInt(url.split(":")[2].split("/")[0]);
            
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 3000);
                return true;
            }
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
