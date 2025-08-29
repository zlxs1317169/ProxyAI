package ee.carlrobert.codegpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import ee.carlrobert.codegpt.metrics.MetricsDatabaseManager;

/**
 * 清除指标数据的Action
 */
public class ClearMetricsDataAction extends AnAction {
    
    public ClearMetricsDataAction() {
        super("清除指标数据");
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("无法获取项目信息", "错误");
            return;
        }
        
        try {
            // 确认清除操作
            int result = Messages.showYesNoDialog(
                "确定要清除所有指标数据吗？\n\n此操作将删除数据库中的所有指标记录，无法恢复！",
                "确认清除指标数据",
                "清除",
                "取消",
                Messages.getQuestionIcon()
            );
            
            if (result == Messages.YES) {
                // 执行清除操作
                MetricsDatabaseManager.getInstance().clearAllMetrics();
                
                // 显示成功消息
                Messages.showInfoMessage(
                    "指标数据已成功清除！\n\n所有测试生成的指标数据已被删除。",
                    "清除完成"
                );
            }
            
        } catch (Exception ex) {
            Messages.showErrorDialog(
                "清除指标数据时发生错误: " + ex.getMessage(),
                "错误"
            );
        }
    }
}
