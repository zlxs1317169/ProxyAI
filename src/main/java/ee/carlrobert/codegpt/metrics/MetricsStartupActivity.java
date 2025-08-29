package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 指标系统启动活动
 * 确保指标收集系统在项目启动时正确初始化
 */
public class MetricsStartupActivity implements StartupActivity {
    
    private static final Logger LOG = Logger.getInstance(MetricsStartupActivity.class);
    
    @Override
    public void runActivity(@NotNull Project project) {
        try {
            // 延迟初始化指标系统，避免影响IDE启动速度
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    // 等待一段时间确保其他服务已初始化
                    Thread.sleep(2000);
                    
                    // 初始化MetricsIntegration服务
                    MetricsIntegration integration = MetricsIntegration.getInstance();
                    if (integration != null) {
                        integration.initializeMetricsSystem(project);
                        LOG.info("ProxyAI 指标收集系统已在项目中启动: " + project.getName());
                    } else {
                        LOG.warn("无法获取MetricsIntegration服务实例");
                    }
                    
                } catch (Exception e) {
                    LOG.warn("初始化指标系统时发生错误", e);
                }
            });
            
        } catch (Exception e) {
            LOG.error("启动指标系统活动时发生错误", e);
        }
    }
}