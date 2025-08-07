package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

/**
 * 度量集成诊断工具
 * 用于检查MetricsIntegration的初始化状态和数据采集路径
 */
public class MetricsIntegrationDiagnostic {
    
    private static final Logger LOG = Logger.getInstance(MetricsIntegrationDiagnostic.class);
    
    public static void main(String[] args) {
        System.out.println("=== ProxyAI度量集成诊断工具 ===");
        
        try {
            // 1. 检查MetricsIntegration实例
            System.out.println("1. 检查MetricsIntegration实例...");
            MetricsIntegration integration = MetricsIntegration.getInstance();
            
            if (integration != null) {
                System.out.println("✓ 成功获取MetricsIntegration实例");
                
                // 2. 检查初始化状态
                System.out.println("2. 检查初始化状态...");
                boolean isInitialized = integration.isInitialized();
                System.out.println(isInitialized ? 
                    "✓ MetricsIntegration已初始化" : 
                    "✗ MetricsIntegration未初始化");
                
                // 3. 检查MetricsCollector
                System.out.println("3. 检查MetricsCollector...");
                MetricsCollector collector = integration.getMetricsCollector();
                System.out.println(collector != null ? 
                    "✓ MetricsCollector可用" : 
                    "✗ MetricsCollector不可用");
                
                // 4. 检查设置
                System.out.println("4. 检查度量设置...");
                checkMetricsSettings();
                
                // 5. 测试数据采集路径
                System.out.println("5. 测试数据采集路径...");
                testDataCollectionPath(integration);
                
            } else {
                System.out.println("✗ 无法获取MetricsIntegration实例");
            }
            
            // 6. 检查SafeMetricsCollector
            System.out.println("6. 检查SafeMetricsCollector...");
            testSafeMetricsCollector();
            
            // 7. 检查数据持久化
            System.out.println("7. 检查数据持久化...");
            checkDataPersistence();
            
        } catch (Exception e) {
            System.err.println("✗ 诊断过程中出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查度量设置
     */
    private static void checkMetricsSettings() {
        try {
            // 尝试通过反射获取设置
            Class<?> settingsClass = Class.forName("ee.carlrobert.codegpt.settings.metrics.MetricsSettings");
            Object settingsInstance = settingsClass.getMethod("getInstance").invoke(null);
            
            boolean metricsEnabled = (boolean) settingsClass.getMethod("isMetricsEnabled").invoke(settingsInstance);
            boolean onlyTrackAIUsage = (boolean) settingsClass.getMethod("isOnlyTrackAIUsage").invoke(settingsInstance);
            boolean autoDetectionEnabled = (boolean) settingsClass.getMethod("isAutoDetectionEnabled").invoke(settingsInstance);
            
            System.out.println("  - 度量启用状态: " + (metricsEnabled ? "已启用" : "已禁用"));
            System.out.println("  - 仅跟踪AI使用: " + (onlyTrackAIUsage ? "是" : "否"));
            System.out.println("  - 自动检测启用: " + (autoDetectionEnabled ? "已启用" : "已禁用"));
            
            if (!metricsEnabled) {
                System.out.println("✗ 度量系统已禁用，这是数据未被采集的主要原因");
            }
            
            if (onlyTrackAIUsage && !autoDetectionEnabled) {
                System.out.println("⚠️ 已设置仅跟踪AI使用，但自动检测未启用，可能导致数据未被正确识别");
            }
            
        } catch (Exception e) {
            System.out.println("✗ 无法检查度量设置: " + e.getMessage());
        }
    }
    
    /**
     * 测试数据采集路径
     */
    private static void testDataCollectionPath(MetricsIntegration integration) {
        try {
            // 1. 测试直接调用MetricsIntegration
            System.out.println("  - 测试直接调用MetricsIntegration...");
            integration.recordAICompletion("java", "// 测试代码", true, 100L);
            System.out.println("    ✓ 直接调用成功");
            
            // 2. 测试通过SafeMetricsCollector调用
            System.out.println("  - 测试通过SafeMetricsCollector调用...");
            SafeMetricsCollector.safeRecordAICompletion("java", "// 测试代码", true, 100L);
            System.out.println("    ✓ 通过SafeMetricsCollector调用成功");
            
            // 3. 检查数据是否被记录
            System.out.println("  - 检查数据是否被记录...");
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
            
            System.out.println("    总生成行数: " + report.totalLinesGenerated);
            if (report.totalLinesGenerated > 0) {
                System.out.println("    ✓ 数据已被记录");
            } else {
                System.out.println("    ✗ 数据未被记录");
            }
            
        } catch (Exception e) {
            System.out.println("✗ 测试数据采集路径时出错: " + e.getMessage());
        }
    }
    
    /**
     * 测试SafeMetricsCollector
     */
    private static void testSafeMetricsCollector() {
        try {
            // 1. 测试safeRecordAICompletion
            SafeMetricsCollector.safeRecordAICompletion("test", "// 测试代码", true, 100L);
            System.out.println("  ✓ safeRecordAICompletion调用成功");
            
            // 2. 测试safeStartChatSession
            String sessionId = "test-session-" + System.currentTimeMillis();
            SafeMetricsCollector.safeStartChatSession(sessionId, "test");
            System.out.println("  ✓ safeStartChatSession调用成功");
            
            // 3. 测试safeRecordAIResponse
            SafeMetricsCollector.safeRecordAIResponse(sessionId, "测试响应", "// 测试代码");
            System.out.println("  ✓ safeRecordAIResponse调用成功");
            
        } catch (Exception e) {
            System.out.println("✗ 测试SafeMetricsCollector时出错: " + e.getMessage());
        }
    }
    
    /**
     * 检查数据持久化
     */
    private static void checkDataPersistence() {
        try {
            // 1. 获取ProductivityMetrics实例
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            
            if (metrics != null) {
                System.out.println("  ✓ 成功获取ProductivityMetrics实例");
                
                // 2. 检查State对象
                Object state = metrics.getState();
                System.out.println("  " + (state != null ? "✓ State对象可用" : "✗ State对象不可用"));
                
                // 3. 检查XML文件
                String userHome = System.getProperty("user.home");
                String[] possiblePaths = {
                    userHome + "/.config/JetBrains/IntelliJIdea*/options/proxyai-productivity-metrics.xml",
                    userHome + "/AppData/Roaming/JetBrains/IntelliJIdea*/options/proxyai-productivity-metrics.xml",
                    userHome + "/.IntelliJIdea*/config/options/proxyai-productivity-metrics.xml"
                };
                
                System.out.println("  - 检查可能的XML文件位置:");
                for (String path : possiblePaths) {
                    System.out.println("    " + path);
                }
                
                // 4. 尝试强制保存
                System.out.println("  - 尝试强制保存数据...");
                // 这里我们不能直接调用保存方法，因为它是由平台管理的
                System.out.println("    ⚠️ 数据保存由IntelliJ平台管理，无法手动触发");
                
            } else {
                System.out.println("  ✗ 无法获取ProductivityMetrics实例");
            }
            
        } catch (Exception e) {
            System.out.println("✗ 检查数据持久化时出错: " + e.getMessage());
        }
    }
}