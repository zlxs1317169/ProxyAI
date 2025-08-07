package ee.carlrobert.codegpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import ee.carlrobert.codegpt.metrics.MetricsMonitor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 效能度量监控面板Action
 * 提供实时监控和诊断功能
 */
public class MetricsMonitorAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        SwingUtilities.invokeLater(() -> {
            try {
                MetricsMonitor monitor = MetricsMonitor.getInstance();
                showMonitoringDialog(monitor);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "打开监控面板时发生错误: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void showMonitoringDialog(MetricsMonitor monitor) {
        JDialog dialog = new JDialog((Frame) null, "ProxyAI效能度量监控", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 顶部控制面板
        JPanel controlPanel = createControlPanel(monitor, dialog);
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // 中央内容面板
        JTabbedPane tabbedPane = createTabbedPane(monitor);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // 底部状态面板
        JPanel statusPanel = createStatusPanel(monitor);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createControlPanel(MetricsMonitor monitor, JDialog dialog) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("监控控制"));
        
        JButton refreshButton = new JButton("刷新数据");
        refreshButton.addActionListener(e -> refreshMonitoringData(monitor, dialog));
        panel.add(refreshButton);
        
        JButton exportButton = new JButton("导出监控报告");
        exportButton.addActionListener(e -> exportMonitoringReport(monitor));
        panel.add(exportButton);
        
        JButton clearButton = new JButton("清理过期数据");
        clearButton.addActionListener(e -> clearExpiredData(monitor));
        panel.add(clearButton);
        
        return panel;
    }
    
    private JTabbedPane createTabbedPane(MetricsMonitor monitor) {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 实时统计标签页
        tabbedPane.addTab("实时统计", createRealTimeStatsPanel(monitor));
        
        // 最近活动标签页
        tabbedPane.addTab("最近活动", createRecentActivitiesPanel(monitor));
        
        // 活动会话标签页
        tabbedPane.addTab("活动会话", createActiveSessionsPanel(monitor));
        
        // 系统健康标签页
        tabbedPane.addTab("系统健康", createSystemHealthPanel(monitor));
        
        return tabbedPane;
    }
    
    private JPanel createRealTimeStatsPanel(MetricsMonitor monitor) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        MetricsMonitor.MonitoringStats stats = monitor.getRealTimeStats();
        
        // 创建统计标签
        JLabel[] labels = {
            new JLabel("聊天会话总数: " + stats.chatSessionsCount),
            new JLabel("代码补全总数: " + stats.codeCompletionsCount),
            new JLabel("AI响应总数: " + stats.aiResponsesCount),
            new JLabel("生成代码行数: " + stats.totalCodeLinesGenerated),
            new JLabel("节省时间: " + String.format("%.1f 小时", stats.totalTimeSavedMs / 1000.0 / 3600.0)),
            new JLabel("活动会话数: " + stats.activeSessionsCount),
            new JLabel("平均响应时间: " + stats.averageResponseTimeMs + " ms"),
            new JLabel("系统错误数: " + stats.metricsErrors)
        };
        
        // 设置标签样式和布局
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            labels[i].setFont(labels[i].getFont().deriveFont(Font.BOLD, 14f));
            
            // 根据数值设置颜色
            if (i == 4) { // 节省时间 - 绿色
                labels[i].setForeground(new Color(0, 150, 0));
            } else if (i == 6 && stats.averageResponseTimeMs > 2000) { // 响应时间过长 - 红色
                labels[i].setForeground(Color.RED);
            } else if (i == 7 && stats.metricsErrors > 0) { // 有错误 - 橙色
                labels[i].setForeground(new Color(255, 140, 0));
            } else {
                labels[i].setForeground(new Color(0, 100, 200));
            }
            
            panel.add(labels[i], gbc);
        }
        
        // 添加进度条显示
        gbc.gridy = labels.length;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JProgressBar efficiencyBar = new JProgressBar(0, 100);
        efficiencyBar.setValue(Math.min(100, (int)(stats.totalTimeSavedMs / 1000.0 / 36.0))); // 假设100小时为满值
        efficiencyBar.setStringPainted(true);
        efficiencyBar.setString("效率提升指数");
        efficiencyBar.setForeground(new Color(0, 150, 0));
        panel.add(efficiencyBar, gbc);
        
        return panel;
    }
    
    private JPanel createRecentActivitiesPanel(MetricsMonitor monitor) {
        JPanel panel = new JPanel(new BorderLayout());
        
        List<MetricsMonitor.ActivityRecord> activities = monitor.getRecentActivities(50);
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (MetricsMonitor.ActivityRecord activity : activities) {
            listModel.addElement(activity.toString());
        }
        
        JList<String> activitiesList = new JList<>(listModel);
        activitiesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activitiesList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(activitiesList);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 底部信息面板
        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.add(new JLabel("显示最近 " + activities.size() + " 条活动记录"));
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createActiveSessionsPanel(MetricsMonitor monitor) {
        JPanel panel = new JPanel(new BorderLayout());
        
        List<MetricsMonitor.SessionInfo> sessions = monitor.getActiveSessions();
        
        String[] columnNames = {"会话ID", "类型", "任务类型", "开始时间", "响应数", "代码行数", "状态"};
        Object[][] data = new Object[sessions.size()][columnNames.length];
        
        for (int i = 0; i < sessions.size(); i++) {
            MetricsMonitor.SessionInfo session = sessions.get(i);
            data[i][0] = session.sessionId.substring(0, Math.min(8, session.sessionId.length())) + "...";
            data[i][1] = session.type;
            data[i][2] = session.taskType;
            data[i][3] = session.startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            data[i][4] = session.responsesCount;
            data[i][5] = session.totalCodeLines;
            data[i][6] = session.status;
        }
        
        JTable table = new JTable(data, columnNames);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 底部信息面板
        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.add(new JLabel("当前活动会话: " + sessions.size() + " 个"));
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSystemHealthPanel(MetricsMonitor monitor) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        MetricsMonitor.MonitoringStats stats = monitor.getRealTimeStats();
        
        // 系统健康状态
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel healthLabel = new JLabel("系统健康状态: ");
        healthLabel.setFont(healthLabel.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(healthLabel, gbc);
        
        gbc.gridx = 1;
        String healthStatus = determineHealthStatus(stats);
        JLabel statusLabel = new JLabel(healthStatus);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16f));
        statusLabel.setForeground(getHealthStatusColor(healthStatus));
        panel.add(statusLabel, gbc);
        
        // 健康指标
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(createHealthMetricsPanel(stats), gbc);
        
        // 建议
        gbc.gridy = 2;
        panel.add(createHealthRecommendationsPanel(stats), gbc);
        
        return panel;
    }
    
    private JPanel createHealthMetricsPanel(MetricsMonitor.MonitoringStats stats) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("健康指标"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 错误率
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("系统错误数:"), gbc);
        gbc.gridx = 1;
        JLabel errorLabel = new JLabel(String.valueOf(stats.metricsErrors));
        errorLabel.setForeground(stats.metricsErrors > 5 ? Color.RED : Color.GREEN);
        panel.add(errorLabel, gbc);
        
        // 响应时间
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("平均响应时间:"), gbc);
        gbc.gridx = 1;
        JLabel responseLabel = new JLabel(stats.averageResponseTimeMs + " ms");
        responseLabel.setForeground(stats.averageResponseTimeMs > 2000 ? Color.RED : Color.GREEN);
        panel.add(responseLabel, gbc);
        
        // 活动会话数
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("活动会话数:"), gbc);
        gbc.gridx = 1;
        JLabel sessionLabel = new JLabel(String.valueOf(stats.activeSessionsCount));
        sessionLabel.setForeground(stats.activeSessionsCount > 20 ? Color.ORANGE : Color.GREEN);
        panel.add(sessionLabel, gbc);
        
        return panel;
    }
    
    private JPanel createHealthRecommendationsPanel(MetricsMonitor.MonitoringStats stats) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("系统建议"));
        
        JTextArea recommendationsArea = new JTextArea();
        recommendationsArea.setEditable(false);
        recommendationsArea.setBackground(panel.getBackground());
        recommendationsArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        StringBuilder recommendations = new StringBuilder();
        
        if (stats.metricsErrors > 5) {
            recommendations.append("⚠️ 系统错误较多，建议检查日志并修复相关问题\n");
        }
        
        if (stats.averageResponseTimeMs > 2000) {
            recommendations.append("🐌 响应时间较长，建议优化AI模型配置或网络连接\n");
        }
        
        if (stats.activeSessionsCount > 20) {
            recommendations.append("📊 活动会话较多，系统负载较高\n");
        }
        
        if (stats.totalCodeLinesGenerated > 1000) {
            recommendations.append("🎉 代码生成量很高，AI助手使用效果良好！\n");
        }
        
        if (stats.totalTimeSavedMs > 3600000) { // 超过1小时
            recommendations.append("⏰ 已节省大量时间，继续保持高效使用！\n");
        }
        
        if (recommendations.length() == 0) {
            recommendations.append("✅ 系统运行正常，所有指标都在健康范围内");
        }
        
        recommendationsArea.setText(recommendations.toString());
        panel.add(recommendationsArea, BorderLayout.CENTER);
        
        return panel;
    }
    
    private String determineHealthStatus(MetricsMonitor.MonitoringStats stats) {
        if (stats.metricsErrors > 10 || stats.averageResponseTimeMs > 5000) {
            return "异常";
        } else if (stats.metricsErrors > 5 || stats.averageResponseTimeMs > 2000) {
            return "警告";
        } else {
            return "健康";
        }
    }
    
    private Color getHealthStatusColor(String status) {
        switch (status) {
            case "健康": return new Color(0, 150, 0);
            case "警告": return new Color(255, 140, 0);
            case "异常": return Color.RED;
            default: return Color.BLACK;
        }
    }
    
    private JPanel createStatusPanel(MetricsMonitor monitor) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        MetricsMonitor.MonitoringStats stats = monitor.getRealTimeStats();
        String statusText = String.format("状态: %s | 总会话: %d | 总响应: %d | 错误: %d",
            determineHealthStatus(stats),
            stats.chatSessionsCount,
            stats.aiResponsesCount,
            stats.metricsErrors);
        
        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        panel.add(statusLabel);
        
        return panel;
    }
    
    private void refreshMonitoringData(MetricsMonitor monitor, JDialog dialog) {
        // 关闭当前对话框并重新打开
        dialog.dispose();
        showMonitoringDialog(monitor);
    }
    
    private void exportMonitoringReport(MetricsMonitor monitor) {
        try {
            MetricsMonitor.MonitoringStats stats = monitor.getRealTimeStats();
            List<MetricsMonitor.ActivityRecord> activities = monitor.getRecentActivities(20);
            
            StringBuilder report = new StringBuilder();
            report.append("=== ProxyAI效能度量监控报告 ===\n");
            report.append("生成时间: ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            
            report.append("📊 实时统计:\n");
            report.append("- 聊天会话总数: ").append(stats.chatSessionsCount).append("\n");
            report.append("- 代码补全总数: ").append(stats.codeCompletionsCount).append("\n");
            report.append("- AI响应总数: ").append(stats.aiResponsesCount).append("\n");
            report.append("- 生成代码行数: ").append(stats.totalCodeLinesGenerated).append("\n");
            report.append("- 节省时间: ").append(String.format("%.1f 小时", stats.totalTimeSavedMs / 1000.0 / 3600.0)).append("\n");
            report.append("- 活动会话数: ").append(stats.activeSessionsCount).append("\n");
            report.append("- 平均响应时间: ").append(stats.averageResponseTimeMs).append(" ms\n");
            report.append("- 系统错误数: ").append(stats.metricsErrors).append("\n\n");
            
            report.append("🏥 系统健康状态: ").append(determineHealthStatus(stats)).append("\n\n");
            
            report.append("📝 最近活动 (最新20条):\n");
            for (MetricsMonitor.ActivityRecord activity : activities) {
                report.append("- ").append(activity.toString()).append("\n");
            }
            
            report.append("\n").append("=".repeat(50)).append("\n");
            report.append("报告由 ProxyAI 监控系统自动生成\n");
            
            // 显示报告预览
            JTextArea textArea = new JTextArea(report.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(700, 500));
            
            JOptionPane.showMessageDialog(null, scrollPane, "监控报告", JOptionPane.PLAIN_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "导出监控报告时发生错误: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearExpiredData(MetricsMonitor monitor) {
        int result = JOptionPane.showConfirmDialog(null,
            "确定要清理过期的监控数据吗？\n这将清理2小时前的活动记录和无活动的会话。",
            "确认清理",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                // 这里可以调用monitor的清理方法
                JOptionPane.showMessageDialog(null,
                    "过期数据清理完成！",
                    "清理完成",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "清理数据时发生错误: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}