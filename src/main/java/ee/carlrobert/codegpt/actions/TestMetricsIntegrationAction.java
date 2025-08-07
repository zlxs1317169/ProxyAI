package ee.carlrobert.codegpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import ee.carlrobert.codegpt.metrics.MetricsIntegrationTest;
import org.jetbrains.annotations.NotNull;

/**
 * 测试效能度量集成的Action
 * 用于验证度量收集是否正常工作
 */
public class TestMetricsIntegrationAction extends AnAction {
    
    public TestMetricsIntegrationAction() {
        super("测试效能度量集成", "运行测试验证效能度量收集是否正常工作", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 显示确认对话框
        int result = Messages.showYesNoDialog(
            "此操作将运行效能度量集成测试，包括：\n" +
            "• 测试聊天功能度量收集\n" +
            "• 测试代码补全度量收集\n" +
            "• 验证完整的度量收集链路\n" +
            "• 生成测试数据用于验证\n\n" +
            "测试结果将在控制台输出。\n" +
            "是否继续？",
            "确认运行度量集成测试",
            Messages.getQuestionIcon()
        );
        
        if (result != Messages.YES) {
            return;
        }
        
        // 在后台线程中运行测试
        ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "运行效能度量集成测试", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setText("正在运行效能度量集成测试...");
                    indicator.setIndeterminate(true);
                    
                    // 运行测试
                    MetricsIntegrationTest.runAllTests();
                    
                    // 在UI线程中显示完成消息
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showInfoMessage(
                            "效能度量集成测试已完成！\n\n" +
                            "请查看控制台输出了解详细结果。\n" +
                            "如果测试显示数据更新，说明度量收集正常工作。\n" +
                            "如果数据没有变化，请检查控制台中的诊断信息。\n\n" +
                            "您也可以打开提效统计面板查看测试生成的数据。",
                            "测试完成"
                        );
                    });
                    
                } catch (Exception ex) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showErrorDialog(
                            "运行效能度量集成测试时发生错误：\n" + ex.getMessage() + "\n\n" +
                            "请查看控制台了解详细错误信息。",
                            "测试失败"
                        );
                    });
                    ex.printStackTrace();
                }
            }
        });
    }
}