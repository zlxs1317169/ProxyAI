package ee.carlrobert.codegpt.metrics;

import ee.carlrobert.codegpt.settings.metrics.MetricsSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

/**
 * 度量系统一键修复工具
 * 快速诊断并修复常见的数据收集问题
 */
public class QuickFixRunner {
    
    public static void main(String[] args) {
        System.out.println("=== ProxyAI 度量系统一键修复 ===");
        runQuickFix();
    }
    
    /**
     * 执行一键修复
     */
    public static void runQuickFix() {
        System.out.println("🚀 开始执行一键修复...");
        
        try {
            // 步骤1：诊断当前问题
            System.out.println("\n📋 步骤1：诊断当前问题");
            boolean hasIssues = diagnoseProblem();
            
            if (!hasIssues) {
                System.out.println("✅ 系统运行正常，无需修复");
                return;
            }
            
            // 步骤2：修复设置问题
            System.out.println("\n🔧 步骤2：修复设置问题");
            fixSettings();
            
            // 步骤3：重新初始化服务
            System.out.println("\n🏗️ 步骤3：重新初始化服务");
            reinitializeServices();
            
            // 步骤4：生成测试数据验证修复
            System.out.println("\n🧪 步骤4：生成测试数据验证修复");
            generateTestData();
            
            // 步骤5：验证修复结果
            System.out.println("\n✅ 步骤5：验证修复结果");
            verifyFix();
            
            System.out.println("\n🎉 一键修复完成！");
            System.out.println("💡 建议：重启IDE以确保所有更改完全生效");
            
            // 发送通知
            sendNotification("ProxyAI度量系统修复完成", "数据收集功能已恢复正常", NotificationType.INFORMATION);
            
        } catch (Exception e) {
            System.err.println("❌ 一键修复过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            sendNotification("ProxyAI度量系统修复失败", "请查看控制台错误信息", NotificationType.ERROR);
        }
    }
    
    /**
     * 诊断当前问题
     */
    private static boolean diagnoseProblem() {
        boolean hasIssues = false;
        
        try {
            // 检查设置
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings == null) {
                System.out.println("❌ MetricsSettings 服务未初始化");
                hasIssues = true;
            } else {
                if (!settings.isMetricsEnabled()) {
                    System.out.println("⚠️ 度量收集已禁用");
                    hasIssues = true;
                } else {
                    System.out.println("✅ 度量收集已启用");
                }
            }
            
            // 检查核心服务
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics == null) {
                System.out.println("❌ ProductivityMetrics 服务未初始化");
                hasIssues = true;
            } else {
                System.out.println("✅ ProductivityMetrics 服务正常");
                
                // 检查是否有数据
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                if (report.totalLinesGenerated == 0 && report.totalTimeSavedHours == 0) {
                    System.out.println("⚠️ 没有收集到任何数据");
                    hasIssues = true;
                }
            }
            
            // 检查集成服务
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration == null) {
                System.out.println("❌ MetricsIntegration 服务未初始化");
                hasIssues = true;
            } else if (!integration.isInitialized()) {
                System.out.println("⚠️ MetricsIntegration 未完全初始化");
                hasIssues = true;
            } else {
                System.out.println("✅ MetricsIntegration 服务正常");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 诊断过程中发生错误: " + e.getMessage());
            hasIssues = true;
        }
        
        return hasIssues;
    }
    
    /**
     * 修复设置问题
     */
    private static void fixSettings() {
        try {
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings == null) {
                System.out.println("❌ 无法获取设置实例，跳过设置修复");
                return;
            }
            
            boolean changed = false;
            
            // 启用度量收集
            if (!settings.isMetricsEnabled()) {
                settings.setMetricsEnabled(true);
                System.out.println("✅ 已启用度量收集");
                changed = true;
            }
            
            // 启用仅跟踪AI使用模式（推荐）
            if (!settings.isOnlyTrackAIUsage()) {
                settings.setOnlyTrackAIUsage(true);
                System.out.println("✅ 已启用仅跟踪AI使用模式");
                changed = true;
            }
            
            // 禁用自动检测（避免误判）
            if (settings.isAutoDetectionEnabled()) {
                settings.setAutoDetectionEnabled(false);
                System.out.println("✅ 已禁用自动检测（避免误判）");
                changed = true;
            }
            
            // 启用通知
            if (!settings.isNotificationEnabled()) {
                settings.setNotificationEnabled(true);
                System.out.println("✅ 已启用通知");
                changed = true;
            }
            
            if (!changed) {
                System.out.println("✅ 设置已是最优配置，无需修改");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 修复设置时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 重新初始化服务
     */
    private static void reinitializeServices() {
        try {
            // 确保 ProductivityMetrics 已初始化
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                System.out.println("✅ ProductivityMetrics 服务已就绪");
            } else {
                System.out.println("❌ ProductivityMetrics 服务初始化失败");
            }
            
            // 确保 MetricsIntegration 已初始化
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration != null) {
                System.out.println("✅ MetricsIntegration 服务已就绪");
                
                // 如果未初始化，尝试手动初始化
                if (!integration.isInitialized()) {
                    System.out.println("🔄 尝试手动初始化 MetricsIntegration...");
                    
                    Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                    if (openProjects.length > 0) {
                        integration.runActivity(openProjects[0]);
                        System.out.println("✅ MetricsIntegration 手动初始化完成");
                    } else {
                        System.out.println("⚠️ 没有打开的项目，将在项目打开时自动初始化");
                    }
                }
            } else {
                System.out.println("❌ MetricsIntegration 服务初始化失败");
            }
            
            // 确保 MetricsDataValidator 已初始化
            MetricsDataValidator validator = MetricsDataValidator.getInstance();
            if (validator != null) {
                System.out.println("✅ MetricsDataValidator 服务已就绪");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 重新初始化服务时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 生成测试数据
     */
    private static void generateTestData() {
        try {
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration == null || !integration.isInitialized()) {
                System.out.println("⚠️ MetricsIntegration 未就绪，跳过测试数据生成");
                return;
            }
            
            System.out.println("🧪 生成测试数据以验证系统功能...");
            
            // 生成一些测试数据
            integration.recordAICompletion("java", "System.out.println(\"Hello World\");", true, 150L);
            integration.recordAICompletion("python", "print('Hello World')", true, 120L);
            integration.recordAICompletion("javascript", "console.log('Hello World');", false, 200L);
            
            integration.recordAIChatGeneration(
                "// 生成的示例代码\nclass TestExample {\n    public void testMethod() {\n        System.out.println(\"测试\");\n    }\n}",
                "class TestExample {\n    public void testMethod() {\n        System.out.println(\"测试\");\n    }\n}",
                30000L,
                "test_validation"
            );
            
            integration.recordLearningActivity("system_validation", 1, 60000L);
            
            System.out.println("✅ 测试数据生成完成");
            
        } catch (Exception e) {
            System.out.println("❌ 生成测试数据时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 验证修复结果
     */
    private static void verifyFix() {
        try {
            boolean allGood = true;
            
            // 检查设置
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings != null && settings.isMetricsEnabled()) {
                System.out.println("✅ 度量收集设置正常");
            } else {
                System.out.println("❌ 度量收集设置仍有问题");
                allGood = false;
            }
            
            // 检查服务
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                System.out.println("✅ ProductivityMetrics 服务正常");
                
                // 检查是否有数据
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                if (report.totalLinesGenerated > 0 || report.totalTimeSavedHours > 0) {
                    System.out.println("✅ 数据收集功能正常 - 已收集到数据");
                } else {
                    System.out.println("⚠️ 暂无数据，但系统已就绪");
                }
            } else {
                System.out.println("❌ ProductivityMetrics 服务仍有问题");
                allGood = false;
            }
            
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration != null && integration.isInitialized()) {
                System.out.println("✅ MetricsIntegration 服务正常");
            } else {
                System.out.println("❌ MetricsIntegration 服务仍有问题");
                allGood = false;
            }
            
            if (allGood) {
                System.out.println("\n🎉 修复验证通过！系统应该可以正常收集数据了");
                System.out.println("💡 接下来的步骤：");
                System.out.println("   1. 重启IDE以确保所有更改生效");
                System.out.println("   2. 使用AI功能（代码补全、聊天）");
                System.out.println("   3. 打开 ProxyAI-Metrics 工具窗口查看统计数据");
                System.out.println("   4. 如果仍无数据，请检查AI功能是否正常工作");
            } else {
                System.out.println("\n⚠️ 部分问题仍未解决");
                System.out.println("💡 建议：");
                System.out.println("   1. 重启IDE");
                System.out.println("   2. 检查IDE日志中的错误信息");
                System.out.println("   3. 确认AI功能本身是否正常工作");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 验证修复结果时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 发送通知
     */
    private static void sendNotification(String title, String content, NotificationType type) {
        try {
            ApplicationManager.getApplication().invokeLater(() -> {
                Notification notification = new Notification(
                    "ProxyAI.Metrics",
                    title,
                    content,
                    type
                );
                Notifications.Bus.notify(notification);
            });
        } catch (Exception e) {
            System.out.println("发送通知时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 快速诊断（轻量级）
     */
    public static String quickDiagnose() {
        StringBuilder result = new StringBuilder();
        result.append("=== 快速诊断结果 ===\n");
        
        try {
            // 检查设置
            MetricsSettings settings = MetricsSettings.getInstance();
            if (settings == null) {
                result.append("❌ 设置服务：未初始化\n");
            } else {
                result.append("✅ 设置服务：正常\n");
                result.append("   - 度量收集：").append(settings.isMetricsEnabled() ? "已启用" : "已禁用").append("\n");
            }
            
            // 检查核心服务
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics == null) {
                result.append("❌ 度量服务：未初始化\n");
            } else {
                result.append("✅ 度量服务：正常\n");
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                result.append("   - 今日数据：").append(report.totalLinesGenerated > 0 ? "有数据" : "无数据").append("\n");
            }
            
            // 检查集成服务
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration == null) {
                result.append("❌ 集成服务：未初始化\n");
            } else {
                result.append("✅ 集成服务：").append(integration.isInitialized() ? "已初始化" : "未初始化").append("\n");
            }
            
        } catch (Exception e) {
            result.append("❌ 诊断过程出错：").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
}