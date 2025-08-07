package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import ee.carlrobert.codegpt.settings.metrics.MetricsSettings;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 增强的指标收集启动活动
 * 确保所有数据收集组件正确初始化并开始工作
 */
public class EnhancedMetricsStartupActivity implements StartupActivity {
    
    @Override
    public void runActivity(@NotNull Project project) {
        try {
            System.out.println("=== ProxyAI 数据收集系统启动 [" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] ===");
            
            // 1. 检查设置状态
            if (!isMetricsEnabled()) {
                System.out.println("⚠️ 指标收集已禁用，跳过初始化");
                return;
            }
            
            // 2. 初始化核心服务
            initializeCoreServices();
            
            // 3. 初始化数据收集器
            initializeDataCollector(project);
            
            // 4. 初始化验证器
            initializeValidator();
            
            // 5. 运行初始验证
            runInitialValidation();
            
            System.out.println("✅ ProxyAI 数据收集系统启动完成");
            
        } catch (Exception e) {
            System.err.println("❌ 数据收集系统启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查指标收集是否启用
     */
    private boolean isMetricsEnabled() {
        try {
            MetricsSettings settings = MetricsSettings.getInstance();
            return settings != null && settings.isMetricsEnabled();
        } catch (Exception e) {
            System.err.println("检查指标设置时发生错误: " + e.getMessage());
            return true; // 默认启用
        }
    }
    
    /**
     * 初始化核心服务
     */
    private void initializeCoreServices() {
        try {
            System.out.println("🔧 初始化核心服务...");
            
            // 初始化ProductivityMetrics
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                System.out.println("✓ ProductivityMetrics 服务已启动");
                
                // 测试基本功能
                ProductivityMetrics.ProductivityReport testReport = metrics.getProductivityReport(1);
                if (testReport != null) {
                    System.out.println("✓ 指标报告生成功能正常");
                }
            } else {
                System.err.println("❌ ProductivityMetrics 服务启动失败");
            }
            
        } catch (Exception e) {
            System.err.println("初始化核心服务时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 初始化数据收集器
     */
    private void initializeDataCollector(Project project) {
        try {
            System.out.println("📊 初始化数据收集器...");
            
            MetricsSettings settings = MetricsSettings.getInstance();
            
            if (settings != null && settings.isOnlyTrackAIUsage()) {
                System.out.println("✅ 启用精确跟踪模式 - 只跟踪真实AI使用");
                // 初始化AIUsageTracker而不是MetricsCollector
                AIUsageTracker tracker = AIUsageTracker.getInstance();
                if (tracker != null) {
                    System.out.println("✓ AIUsageTracker 已启动");
                } else {
                    System.err.println("❌ AIUsageTracker 创建失败");
                }
            } else {
                System.out.println("⚠️ 启用传统收集模式 - 可能包含自动检测");
                // 使用传统的MetricsCollector
                MetricsCollector collector = new MetricsCollector();
                if (collector != null) {
                    collector.runActivity(project);
                    System.out.println("✓ MetricsCollector 已启动");
                } else {
                    System.err.println("❌ MetricsCollector 创建失败");
                }
            }
            
        } catch (Exception e) {
            System.err.println("初始化数据收集器时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 初始化验证器
     */
    private void initializeValidator() {
        try {
            System.out.println("🔍 初始化数据验证器...");
            
            MetricsDataValidator validator = MetricsDataValidator.getInstance();
            if (validator != null) {
                System.out.println("✓ MetricsDataValidator 已启动");
            }
            
        } catch (Exception e) {
            System.err.println("初始化验证器时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 运行初始验证
     */
    private void runInitialValidation() {
        try {
            System.out.println("🧪 运行初始系统验证...");
            
            // 延迟5秒后运行验证，确保所有组件都已初始化
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    MetricsDataValidator validator = MetricsDataValidator.getInstance();
                    if (validator != null) {
                        validator.triggerValidation();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.err.println("运行初始验证时发生错误: " + e.getMessage());
                }
            }).start();
            
        } catch (Exception e) {
            System.err.println("启动初始验证时发生错误: " + e.getMessage());
        }
    }
}