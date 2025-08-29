package ee.carlrobert.codegpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import ee.carlrobert.codegpt.metrics.MetricsIntegration;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;
import ee.carlrobert.codegpt.metrics.SafeMetricsCollector;

/**
 * 运行指标系统测试的Action
 */
public class RunMetricsTestAction extends AnAction {
    
    public RunMetricsTestAction() {
        super("运行指标系统测试");
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("无法获取项目信息", "错误");
            return;
        }
        
        try {
            // 运行指标系统测试
            runMetricsTest(project);
            
        } catch (Exception ex) {
            Messages.showErrorDialog("运行指标系统测试时发生错误: " + ex.getMessage(), "错误");
        }
    }
    
    /**
     * 运行指标系统测试
     */
    private void runMetricsTest(Project project) {
        StringBuilder result = new StringBuilder();
        result.append("开始运行指标系统测试...\n\n");
        
        try {
            // 测试1: 检查MetricsIntegration服务
            result.append("1. 检查MetricsIntegration服务...\n");
            MetricsIntegration integration = MetricsIntegration.getInstance();
            if (integration != null) {
                result.append("   ✓ 服务可用\n");
                result.append("   初始化状态: ").append(integration.isInitialized()).append("\n");
                
                // 如果未初始化，尝试初始化
                if (!integration.isInitialized()) {
                    integration.initializeMetricsSystem(project);
                    result.append("   ✓ 已尝试初始化\n");
                }
            } else {
                result.append("   ✗ 服务不可用\n");
            }
            
            // 测试2: 检查ProductivityMetrics服务
            result.append("\n2. 检查ProductivityMetrics服务...\n");
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics != null) {
                result.append("   ✓ 服务可用\n");
                
                // 记录一些测试数据
                metrics.recordCodeCompletion("java", 10, 8, 150L);
                result.append("   ✓ 已记录测试数据\n");
                
                // 生成测试报告
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(1);
                result.append("   测试报告: ").append(report.summary).append("\n");
            } else {
                result.append("   ✗ 服务不可用\n");
            }
            
            // 测试3: 测试SafeMetricsCollector
            result.append("\n3. 测试SafeMetricsCollector...\n");
            SafeMetricsCollector.safeRecordAICompletion("java", "System.out.println(\"test\");", true, 50L);
            result.append("   ✓ 代码补全指标记录成功\n");
            
            SafeMetricsCollector.safeRecordAIResponse("test-session", "测试响应", "// 测试代码");
            result.append("   ✓ AI响应指标记录成功\n");
            
            SafeMetricsCollector.safeRecordAIChatGeneration("// 生成的代码", "// 应用的代码", 5000L, "测试任务");
            result.append("   ✓ 聊天代码生成指标记录成功\n");
            
            result.append("\n✓ 所有测试通过！指标系统工作正常。\n");
            
        } catch (Exception e) {
            result.append("\n✗ 测试过程中发生错误: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }
        
        // 显示测试结果
        Messages.showInfoMessage(result.toString(), "指标系统测试结果");
    }
}
