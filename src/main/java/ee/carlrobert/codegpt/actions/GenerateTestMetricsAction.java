package ee.carlrobert.codegpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import ee.carlrobert.codegpt.metrics.MetricsCollectionValidator;
import org.jetbrains.annotations.NotNull;

/**
 * 生成测试指标数据的操作
 */
public class GenerateTestMetricsAction extends AnAction {
    
    public GenerateTestMetricsAction() {
        super("生成测试指标数据", "为ProxyAI指标系统生成一些测试数据", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            MetricsCollectionValidator.generateTestMetrics(project);
        }
    }
}