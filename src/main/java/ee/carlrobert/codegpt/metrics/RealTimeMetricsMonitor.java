package ee.carlrobert.codegpt.metrics;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实时度量监控工具
 * 用于监控和显示实时的度量数据采集情况
 */
public class RealTimeMetricsMonitor extends AnAction {
    
    private static final Logger LOG = Logger.getInstance(RealTimeMetricsMonitor.class);
    private static final AtomicInteger aiCompletionCount = new AtomicInteger(0);
    private static final AtomicInteger aiChatCount = new AtomicInteger(0);
    private static boolean isMonitoring = false;
    
    public RealTimeMetricsMonitor() {
        super("实时度量监控", "监控实时的度量数据采集情况", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        
        if (isMonitoring) {
            Notifications.Bus.notify(new Notification(
                "ProxyAI.Metrics",
                "监控已在运行",
                "度量监控已经在运行中，请查看现有窗口。",
                NotificationType.INFORMATION
            ));
            return;
        }
        
        try {
            // 安装拦截器
            installMetricsInterceptors();
            
            // 显示监控窗口
            showMonitoringDialog(project);
            
        } catch (Exception ex) {
            LOG.error("启动度量监控时出错", ex);
            Notifications.Bus.notify(new Notification(
                "ProxyAI.Metrics",
                "启动监控失败",
                "启动度量监控时出错: " + ex.getMessage(),
                NotificationType.ERROR
            ));
        }
    }
    
    /**
     * 安装度量拦截器
     */
    private void installMetricsInterceptors() {
        try {
            // 拦截SafeMetricsCollector方法
            // 注意：这里使用的是一种模拟方式，实际上我们无法真正拦截这些方法
            // 在真实环境中，需要使用字节码操作库如ByteBuddy或AspectJ
            
            // 模拟拦截，实际上只是记录一下我们尝试了这个操作
            LOG.info("尝试安装度量拦截器");
            
            // 重置计数器
            aiCompletionCount.set(0);
            aiChatCount.set(0);
            
            // 标记为正在监控
            isMonitoring = true;
            
        } catch (Exception e) {
            LOG.error("安装度量拦截器时出错", e);
            throw new RuntimeException("安装度量拦截器时出错: " + e.getMessage(), e);
        }
    }
    
    /**
     * 显示监控对话框
     */
    private void showMonitoringDialog(Project project) {
        JDialog dialog = new JDialog((Frame) null, "ProxyAI实时度量监控", false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建状态面板
        JPanel statusPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel statusLabel = new JLabel("监控状态: 活动");
        statusLabel.setForeground(Color.GREEN.darker());
        statusPanel.add(statusLabel);
        
        JLabel timeLabel = new JLabel("开始时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        statusPanel.add(timeLabel);
        
        JLabel completionCountLabel = new JLabel("AI补全记录: 0");
        statusPanel.add(completionCountLabel);
        
        JLabel chatCountLabel = new JLabel("AI聊天记录: 0");
        statusPanel.add(chatCountLabel);
        
        JLabel settingsLabel = new JLabel("度量设置: 检查中...");
        statusPanel.add(settingsLabel);
        
        JLabel integrationLabel = new JLabel("集成状态: 检查中...");
        statusPanel.add(integrationLabel);
        
        panel.add(statusPanel, BorderLayout.NORTH);
        
        // 创建日志区域
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton testButton = new JButton("生成测试数据");
        JButton checkButton = new JButton("检查设置");
        JButton closeButton = new JButton("关闭");
        
        buttonPanel.add(testButton);
        buttonPanel.add(checkButton);
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置按钮动作
        testButton.addActionListener(e -> {
            try {
                logArea.append("生成测试数据...\n");
                
                // 生成测试数据
                SafeMetricsCollector.safeRecordAICompletion("java", "System.out.println(\"test\");", true, 100L);
                aiCompletionCount.incrementAndGet();
                completionCountLabel.setText("AI补全记录: " + aiCompletionCount.get());
                
                String sessionId = "test-session-" + System.currentTimeMillis();
                SafeMetricsCollector.safeStartChatSession(sessionId, "test");
                SafeMetricsCollector.safeRecordAIResponse(sessionId, "测试响应", "// 测试代码");
                aiChatCount.incrementAndGet();
                chatCountLabel.setText("AI聊天记录: " + aiChatCount.get());
                
                logArea.append("测试数据生成完成\n");
                
            } catch (Exception ex) {
                logArea.append("生成测试数据时出错: " + ex.getMessage() + "\n");
            }
        });
        
        checkButton.addActionListener(e -> {
            try {
                logArea.append("检查度量设置...\n");
                
                // 检查MetricsSettings
                try {
                    Class<?> settingsClass = Class.forName("ee.carlrobert.codegpt.settings.metrics.MetricsSettings");
                    Object settingsInstance = settingsClass.getMethod("getInstance").invoke(null);
                    
                    boolean metricsEnabled = (boolean) settingsClass.getMethod("isMetricsEnabled").invoke(settingsInstance);
                    boolean onlyTrackAIUsage = (boolean) settingsClass.getMethod("isOnlyTrackAIUsage").invoke(settingsInstance);
                    
                    settingsLabel.setText("度量设置: " + (metricsEnabled ? "已启用" : "已禁用"));
                    if (!metricsEnabled) {
                        settingsLabel.setForeground(Color.RED);
                    }
                    
                    logArea.append("度量启用状态: " + (metricsEnabled ? "已启用" : "已禁用") + "\n");
                    logArea.append("仅跟踪AI使用: " + (onlyTrackAIUsage ? "是" : "否") + "\n");
                    
                } catch (Exception ex) {
                    settingsLabel.setText("度量设置: 无法检查");
                    settingsLabel.setForeground(Color.RED);
                    logArea.append("检查度量设置时出错: " + ex.getMessage() + "\n");
                }
                
                // 检查MetricsIntegration
                try {
                    MetricsIntegration integration = MetricsIntegration.getInstance();
                    boolean isInitialized = integration != null && integration.isInitialized();
                    
                    integrationLabel.setText("集成状态: " + (isInitialized ? "已初始化" : "未初始化"));
                    if (!isInitialized) {
                        integrationLabel.setForeground(Color.RED);
                    }
                    
                    logArea.append("集成初始化状态: " + (isInitialized ? "已初始化" : "未初始化") + "\n");
                    
                } catch (Exception ex) {
                    integrationLabel.setText("集成状态: 无法检查");
                    integrationLabel.setForeground(Color.RED);
                    logArea.append("检查集成状态时出错: " + ex.getMessage() + "\n");
                }
                
            } catch (Exception ex) {
                logArea.append("检查设置时出错: " + ex.getMessage() + "\n");
            }
        });
        
        closeButton.addActionListener(e -> {
            isMonitoring = false;
            dialog.dispose();
        });
        
        // 设置窗口关闭事件
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isMonitoring = false;
            }
        });
        
        // 启动定时更新
        Timer timer = new Timer(1000, e -> {
            timeLabel.setText("运行时间: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
        timer.start();
        
        // 自动检查设置
        ApplicationManager.getApplication().invokeLater(() -> {
            checkButton.doClick();
        });
        
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
    
    /**
     * 记录AI补全
     */
    public static void recordAICompletion(String language, String completionText, boolean accepted, long responseTime) {
        aiCompletionCount.incrementAndGet();
        LOG.info("记录AI补全: language=" + language + ", accepted=" + accepted + ", responseTime=" + responseTime);
    }
    
    /**
     * 记录AI聊天
     */
    public static void recordAIChat(String sessionId, String response, String generatedCode) {
        aiChatCount.incrementAndGet();
        LOG.info("记录AI聊天: sessionId=" + sessionId + ", codeLength=" + (generatedCode != null ? generatedCode.length() : 0));
    }
}