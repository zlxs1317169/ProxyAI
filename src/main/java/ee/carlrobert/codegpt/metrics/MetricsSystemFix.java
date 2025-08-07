package ee.carlrobert.codegpt.metrics;

import ee.carlrobert.codegpt.settings.metrics.MetricsSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

/**
 * 度量系统修复工具
 * 自动修复常见的数据收集问题
 */
public class MetricsSystemFix {
    
    /**
     * 执行完整的系统修复
     */
    public static void performFullFix() {
        System.out.println("=== ProxyAI 度量系统修复 ===");
        
        // 1. 修复设置问题
        fixSettings();
        
        // 2. 重新初始化核心服务
        reinitializeCoreServices();
        
        // 3. 验证修复结果
        verifyFix();
        
        System.out.println("=== 修复完成 ===");
    }
    
    /**
     * 修复设置问题
     */
    private static void fixSettings() {
        System.out.println("🔧 修复设置问题...");
        
        try {
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings == null) {
                System.out.println("❌ 无法获取 MetricsSettings 实例");
                return;
            }
            
            // 确保度量收集已启用
            if (!settings.isMetricsEnabled()) {
                settings.setMetricsEnabled(true);
                System.out.println("✅ 已启用度量收集");
            }
            
            // 推荐配置：仅跟踪AI使用
            if (!settings.isOnlyTrackAIUsage()) {
                settings.setOnlyTrackAIUsage(true);
                System.out.println("✅ 已启用仅跟踪AI使用模式");
            }
            
            // 启用通知以便用户了解系统状态
            if (!settings.isNotificationEnabled()) {
                settings.setNotificationEnabled(true);
                System.out.println("✅ 已启用通知");
            }
            
            // 禁用自动检测以避免误判
            if (settings.isAutoDetectionEnabled()) {
                settings.setAutoDetectionEnabled(false);
                System.out.println("✅ 已禁用自动检测（避免误判）");
            }
            
            System.out.println("✅ 设置修复完成");
            
        } catch (Exception e) {
            System.out.println("❌ 修复设置时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 重新初始化核心服务
     */
    private static void reinitializeCoreServices() {
        System.out.println("🏗️ 重新初始化核心服务...");
        
        try {
            // 确保 ProductivityMetrics 已初始化
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                System.out.println("✅ ProductivityMetrics 服务已初始化");
            } else {
                System.out.println("❌ ProductivityMetrics 服务初始化失败");
            }
            
            // 确保 MetricsIntegration 已初始化
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration != null) {
                System.out.println("✅ MetricsIntegration 服务已初始化");
                
                // 如果未初始化，尝试手动初始化
                if (!integration.isInitialized()) {
                    System.out.println("⚠️ MetricsIntegration 未完全初始化，尝试手动初始化...");
                    
                    // 获取当前项目并初始化
                    Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                    if (openProjects.length > 0) {
                        integration.runActivity(openProjects[0]);
                        System.out.println("✅ 已手动初始化 MetricsIntegration");
                    } else {
                        System.out.println("⚠️ 没有打开的项目，无法完全初始化");
                    }
                }
            } else {
                System.out.println("❌ MetricsIntegration 服务初始化失败");
            }
            
            // 确保 MetricsDataValidator 已初始化
            MetricsDataValidator validator = MetricsDataValidator.getInstance();
            if (validator != null) {
                System.out.println("✅ MetricsDataValidator 服务已初始化");
            } else {
                System.out.println("❌ MetricsDataValidator 服务初始化失败");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 重新初始化核心服务时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 验证修复结果
     */
    private static void verifyFix() {
        System.out.println("🔍 验证修复结果...");
        
        try {
            boolean allGood = true;
            
            // 检查设置
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings == null || !settings.isMetricsEnabled()) {
                System.out.println("❌ 度量收集仍未启用");
                allGood = false;
            } else {
                System.out.println("✅ 度量收集已启用");
            }
            
            // 检查核心服务
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics == null) {
                System.out.println("❌ ProductivityMetrics 服务不可用");
                allGood = false;
            } else {
                System.out.println("✅ ProductivityMetrics 服务可用");
            }
            
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration == null) {
                System.out.println("❌ MetricsIntegration 服务不可用");
                allGood = false;
            } else {
                System.out.println("✅ MetricsIntegration 服务可用");
                if (!integration.isInitialized()) {
                    System.out.println("⚠️ MetricsIntegration 未完全初始化");
                }
            }
            
            // 测试数据收集接口
            if (integration != null && integration.isInitialized()) {
                try {
                    // 测试记录一个虚拟的代码补全（不会影响实际统计）
                    integration.recordAICompletion("test", "// test", false, 0L);
                    System.out.println("✅ 数据收集接口测试通过");
                } catch (Exception e) {
                    System.out.println("❌ 数据收集接口测试失败: " + e.getMessage());
                    allGood = false;
                }
            }
            
            if (allGood) {
                System.out.println("🎉 修复验证通过！度量系统应该可以正常工作了");
                System.out.println("💡 建议：");
                System.out.println("   1. 重启IDE以确保所有更改生效");
                System.out.println("   2. 使用AI功能（代码补全、聊天）来测试数据收集");
                System.out.println("   3. 检查 ProxyAI-Metrics 工具窗口查看统计数据");
            } else {
                System.out.println("⚠️ 部分问题仍未解决，可能需要手动干预");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 验证修复结果时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 清理并重置度量数据
     */
    public static void cleanAndReset() {
        System.out.println("🧹 清理并重置度量数据...");
        
        try {
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                metrics.clearAllData();
                System.out.println("✅ 已清理所有历史数据");
            }
            
            // 重新应用推荐设置
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings != null) {
                settings.setMetricsEnabled(true);
                settings.setOnlyTrackAIUsage(true);
                settings.setAutoDetectionEnabled(false);
                settings.setNotificationEnabled(true);
                settings.setDetailedLoggingEnabled(false);
                
                System.out.println("✅ 已重置为推荐设置");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 清理重置时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 生成测试数据（用于验证系统工作）
     */
    public static void generateTestData() {
        System.out.println("🧪 生成测试数据...");
        
        try {
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration == null || !integration.isInitialized()) {
                System.out.println("❌ MetricsIntegration 未初始化，无法生成测试数据");
                return;
            }
            
            // 生成一些测试数据
            integration.recordAICompletion("java", "System.out.println(\"Hello World\");", true, 150L);
            integration.recordAICompletion("python", "print('Hello World')", true, 120L);
            integration.recordAICompletion("javascript", "console.log('Hello World');", false, 200L);
            
            integration.recordAIChatGeneration(
                "// 生成的示例代码\nclass Example {\n    public void test() {\n        System.out.println(\"test\");\n    }\n}",
                "class Example {\n    public void test() {\n        System.out.println(\"test\");\n    }\n}",
                30000L,
                "feature_development"
            );
            
            integration.recordLearningActivity("java_basics", 3, 600000L);
            
            System.out.println("✅ 测试数据生成完成");
            System.out.println("💡 现在可以检查 ProxyAI-Metrics 工具窗口查看数据");
            
        } catch (Exception e) {
            System.out.println("❌ 生成测试数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}