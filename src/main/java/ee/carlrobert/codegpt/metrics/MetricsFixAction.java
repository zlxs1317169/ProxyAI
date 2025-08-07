package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * 度量系统修复操作
 * 提供IDE菜单入口来修复度量系统问题
 */
public class MetricsFixAction extends AnAction {
    
    public MetricsFixAction() {
        super("修复度量系统", "诊断并修复ProxyAI度量系统的数据收集问题", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 显示确认对话框
        int result = Messages.showYesNoDialog(
            e.getProject(),
            "此操作将诊断并修复ProxyAI度量系统的数据收集问题。\n\n" +
            "修复内容包括：\n" +
            "• 检查并修复设置配置\n" +
            "• 重新初始化核心服务\n" +
            "• 生成测试数据验证功能\n" +
            "• 验证修复结果\n\n" +
            "是否继续？",
            "ProxyAI度量系统修复",
            "开始修复",
            "取消",
            Messages.getQuestionIcon()
        );
        
        if (result != Messages.YES) {
            return;
        }
        
        // 在后台任务中执行修复
        ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "修复ProxyAI度量系统", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                
                try {
                    indicator.setText("正在诊断问题...");
                    indicator.setFraction(0.1);
                    
                    // 执行快速诊断
                    String diagnosis = QuickFixRunner.quickDiagnose();
                    
                    indicator.setText("正在修复设置问题...");
                    indicator.setFraction(0.3);
                    
                    // 执行完整修复
                    ApplicationManager.getApplication().runReadAction(() -> {
                        QuickFixRunner.runQuickFix();
                    });
                    
                    indicator.setText("修复完成，正在验证...");
                    indicator.setFraction(0.9);
                    
                    Thread.sleep(1000); // 给系统一点时间来应用更改
                    
                    indicator.setFraction(1.0);
                    
                    // 在EDT中显示结果
                    ApplicationManager.getApplication().invokeLater(() -> {
                        String finalDiagnosis = QuickFixRunner.quickDiagnose();
                        
                        Messages.showInfoMessage(
                            e.getProject(),
                            "ProxyAI度量系统修复完成！\n\n" +
                            "修复前状态：\n" + diagnosis + "\n" +
                            "修复后状态：\n" + finalDiagnosis + "\n" +
                            "建议：重启IDE以确保所有更改完全生效。\n" +
                            "然后使用AI功能并查看ProxyAI-Metrics工具窗口。",
                            "修复完成"
                        );
                    });
                    
                } catch (Exception ex) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showErrorDialog(
                            e.getProject(),
                            "修复过程中发生错误：\n" + ex.getMessage() + "\n\n" +
                            "请查看IDE日志获取详细信息。",
                            "修复失败"
                        );
                    });
                }
            }
        });
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // 始终启用此操作
        e.getPresentation().setEnabledAndVisible(true);
    }
}