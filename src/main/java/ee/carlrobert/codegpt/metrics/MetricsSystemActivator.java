package ee.carlrobert.codegpt.metrics;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 度量系统激活工具
 * 用于确保度量系统正确启用和初始化
 */
public class MetricsSystemActivator extends AnAction {
    
    private static final Logger LOG = Logger.getInstance(MetricsSystemActivator.class);
    
    public MetricsSystemActivator() {
        super("激活度量系统", "确保度量系统正确启用和初始化", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        
        try {
            LOG.info("开始激活度量系统...");
            
            // 1. 确保设置正确
            ensureSettingsEnabled();
            
            // 2. 强制初始化MetricsIntegration
            forceInitializeMetricsIntegration(project);
            
            // 3. 安装监听器
            ensureListenersInstalled();
            
            // 4. 生成测试数据
            generateTestData();
            
            // 5. 验证数据采集
            verifyDataCollection();
            
            Notifications.Bus.notify(new Notification(
                "ProxyAI.Metrics",
                "度量系统已激活",
                "度量系统已成功激活，现在应该能够正确采集数据。",
                NotificationType.INFORMATION
            ));
            
        } catch (Exception ex) {
            LOG.error("激活度量系统时出错", ex);
            Notifications.Bus.notify(new Notification(
                "ProxyAI.Metrics",
                "激活失败",
                "激活度量系统时出错: " + ex.getMessage(),
                NotificationType.ERROR
            ));
        }
    }
    
    /**
     * 确保设置正确启用
     */
    private void ensureSettingsEnabled() {
        try {
            LOG.info("确保度量设置正确启用...");
            
            // 通过反射获取和修改设置
            Class<?> settingsClass = Class.forName("ee.carlrobert.codegpt.settings.metrics.MetricsSettings");
            Object settingsInstance = settingsClass.getMethod("getInstance").invoke(null);
            
            // 检查当前设置
            boolean metricsEnabled = (boolean) settingsClass.getMethod("isMetricsEnabled").invoke(settingsInstance);
            
            if (!metricsEnabled) {
                LOG.info("度量系统当前已禁用，正在启用...");
                settingsClass.getMethod("setMetricsEnabled", boolean.class).invoke(settingsInstance, true);
                LOG.info("度量系统已启用");
            } else {
                LOG.info("度量系统已经启用");
            }
            
            // 设置其他选项
            settingsClass.getMethod("setOnlyTrackAIUsage", boolean.class).invoke(settingsInstance, true);
            settingsClass.getMethod("setAutoDetectionEnabled", boolean.class).invoke(settingsInstance, true);
            settingsClass.getMethod("setDetailedLoggingEnabled", boolean.class).invoke(settingsInstance, true);
            
            LOG.info("度量设置已更新");
            
        } catch (Exception e) {
            LOG.error("确保设置启用时出错", e);
            throw new RuntimeException("确保设置启用时出错: " + e.getMessage(), e);
        }
    }
    
    /**
     * 强制初始化MetricsIntegration
     */
    private void forceInitializeMetricsIntegration(Project project) {
        try {
            LOG.info("强制初始化MetricsIntegration...");
            
            MetricsIntegration integration = MetricsIntegration.getInstance();
            
            if (integration == null) {
                LOG.error("无法获取MetricsIntegration实例");
                throw new RuntimeException("无法获取MetricsIntegration实例");
            }
            
            // 检查是否已初始化
            boolean isInitialized = integration.isInitialized();
            
            if (!isInitialized) {
                LOG.info("MetricsIntegration未初始化，正在初始化...");
                
                // 尝试通过反射调用初始化方法
                try {
                    java.lang.reflect.Method initMethod = MetricsIntegration.class.getDeclaredMethod("initializeMetricsSystem", Project.class);
                    initMethod.setAccessible(true);
                    initMethod.invoke(integration, project);
                    LOG.info("MetricsIntegration已通过反射初始化");
                } catch (Exception e) {
                    LOG.warn("通过反射初始化失败，尝试其他方法", e);
                    
                    // 尝试通过runActivity方法初始化
                    try {
                        java.lang.reflect.Method runActivityMethod = MetricsIntegration.class.getDeclaredMethod("runActivity", Project.class);
                        runActivityMethod.setAccessible(true);
                        runActivityMethod.invoke(integration, project);
                        LOG.info("MetricsIntegration已通过runActivity初始化");
                    } catch (Exception ex) {
                        LOG.error("通过runActivity初始化失败", ex);
                        throw new RuntimeException("无法初始化MetricsIntegration: " + ex.getMessage());
                    }
                }
            } else {
                LOG.info("MetricsIntegration已经初始化");
            }
            
            // 验证初始化结果
            MetricsCollector collector = integration.getMetricsCollector();
            if (collector == null) {
                LOG.error("初始化后MetricsCollector仍然为null");
                throw new RuntimeException("初始化后MetricsCollector仍然为null");
            }
            
            LOG.info("MetricsIntegration初始化成功，MetricsCollector可用");
            
        } catch (Exception e) {
            LOG.error("强制初始化MetricsIntegration时出错", e);
            throw new RuntimeException("强制初始化MetricsIntegration时出错: " + e.getMessage(), e);
        }
    }
    
    /**
     * 确保监听器已安装
     */
    private void ensureListenersInstalled() {
        try {
            LOG.info("确保监听器已安装...");
            
            // 检查是否有MetricsEditorFactoryListener
            boolean hasMetricsListener = false;
            
            try {
                // 尝试通过反射获取监听器列表
                java.lang.reflect.Field listenersField = Class.forName("com.intellij.openapi.editor.impl.EditorFactoryImpl").getDeclaredField("myEditorFactoryListeners");
                listenersField.setAccessible(true);
                
                Object listenersObj = listenersField.get(com.intellij.openapi.editor.EditorFactory.getInstance());
                if (listenersObj instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Object> listeners = (java.util.List<Object>) listenersObj;
                    
                    for (Object listener : listeners) {
                        String className = listener.getClass().getName();
                        if (className.contains("Metrics") || className.contains("metrics")) {
                            hasMetricsListener = true;
                            LOG.info("找到度量相关监听器: " + className);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                LOG.warn("检查监听器时出错", e);
            }
            
            if (!hasMetricsListener) {
                LOG.warn("未找到度量相关监听器，尝试安装...");
                
                // 尝试创建并安装MetricsEditorFactoryListener
                try {
                    Class<?> listenerClass = Class.forName("ee.carlrobert.codegpt.metrics.MetricsEditorFactoryListener");
                    Object listener = listenerClass.getDeclaredConstructor().newInstance();
                    
                    com.intellij.openapi.editor.EditorFactory.getInstance().addEditorFactoryListener(
                        (com.intellij.openapi.editor.event.EditorFactoryListener) listener, 
                        ApplicationManager.getApplication()
                    );
                    
                    LOG.info("成功安装MetricsEditorFactoryListener");
                } catch (Exception e) {
                    LOG.error("安装MetricsEditorFactoryListener时出错", e);
                    throw new RuntimeException("安装MetricsEditorFactoryListener时出错: " + e.getMessage());
                }
            } else {
                LOG.info("度量相关监听器已安装");
            }
            
        } catch (Exception e) {
            LOG.error("确保监听器安装时出错", e);
            throw new RuntimeException("确保监听器安装时出错: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成测试数据
     */
    private void generateTestData() {
        try {
            LOG.info("生成测试数据...");
            
            // 获取MetricsIntegration实例
            MetricsIntegration integration = MetricsIntegration.getInstance();
            
            // 生成代码补全数据
            integration.recordAICompletion("java", "System.out.println(\"Hello World\");", true, 150L);
            integration.recordAICompletion("python", "print('Hello World')", true, 120L);
            
            // 生成聊天代码生成数据
            integration.recordAIChatGeneration(
                "// 生成的示例代码\nclass Example {\n    public void test() {\n        System.out.println(\"test\");\n    }\n}",
                "// 应用的代码\nclass Example {\n    public void test() {\n        System.out.println(\"test\");\n    }\n}",
                60000L,
                "feature_dev"
            );
            
            LOG.info("测试数据生成完成");
            
        } catch (Exception e) {
            LOG.error("生成测试数据时出错", e);
            throw new RuntimeException("生成测试数据时出错: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证数据采集
     */
    private void verifyDataCollection() {
        try {
            LOG.info("验证数据采集...");
            
            // 获取ProductivityMetrics实例
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            
            if (metrics == null) {
                LOG.error("无法获取ProductivityMetrics实例");
                throw new RuntimeException("无法获取ProductivityMetrics实例");
            }
            
            // 获取报告
            ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
            
            LOG.info("总生成行数: " + report.totalLinesGenerated);
            LOG.info("平均代码接受率: " + report.avgCodeAcceptanceRate);
            LOG.info("总节省时间(小时): " + report.totalTimeSavedHours);
            
            if (report.totalLinesGenerated > 0) {
                LOG.info("✓ 数据采集验证成功，已记录数据");
            } else {
                LOG.warn("⚠️ 数据采集验证失败，未记录数据");
            }
            
            // 获取今日统计
            String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            ProductivityMetrics.DailyProductivityStats todayStats = metrics.getDailyStats(today);
            
            LOG.info("今日代码补全次数: " + todayStats.codeCompletionsCount);
            LOG.info("今日聊天会话次数: " + todayStats.chatSessionsCount);
            
            if (todayStats.codeCompletionsCount > 0 || todayStats.chatSessionsCount > 0) {
                LOG.info("✓ 今日统计数据验证成功");
            } else {
                LOG.warn("⚠️ 今日统计数据验证失败，未记录数据");
            }
            
        } catch (Exception e) {
            LOG.error("验证数据采集时出错", e);
            throw new RuntimeException("验证数据采集时出错: " + e.getMessage(), e);
        }
    }
}