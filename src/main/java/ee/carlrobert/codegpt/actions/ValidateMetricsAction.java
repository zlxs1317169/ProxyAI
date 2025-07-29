package ee.carlrobert.codegpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.metrics.MetricsCollectionValidator;
import ee.carlrobert.codegpt.metrics.SimpleMetricsTest;
import org.jetbrains.annotations.NotNull;

/**
 * 验证指标收集系统的操作
 */
public class ValidateMetricsAction extends AnAction {
    
    public ValidateMetricsAction() {
        super("验证指标收集系统", "检查ProxyAI指标收集系统的状态", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            // 先运行简单测试
            SimpleMetricsTest.testBasicMetrics();
            SimpleMetricsTest.testSafeMetricsCollector();
            
            // 然后运行完整验证
            MetricsCollectionValidator.validateMetricsCollection(project);
        }
    }
}