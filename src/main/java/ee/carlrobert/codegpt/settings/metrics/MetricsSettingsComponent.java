package ee.carlrobert.codegpt.settings.metrics;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 提效度量设置组件
 */
public class MetricsSettingsComponent {
    
    private final JPanel mainPanel;
    private final JBCheckBox metricsEnabledCheckBox = new JBCheckBox("启用提效度量收集");
    private final JBCheckBox autoExportEnabledCheckBox = new JBCheckBox("自动导出报告");
    private final JSpinner exportIntervalSpinner = new JSpinner(new SpinnerNumberModel(24, 1, 168, 1));
    private final JBCheckBox detailedLoggingCheckBox = new JBCheckBox("启用详细日志记录");
    private final JBCheckBox autoDetectionCheckBox = new JBCheckBox("启用自动检测代码补全");
    private final JBCheckBox onlyTrackAIUsageCheckBox = new JBCheckBox("仅跟踪真实AI使用");
    
    // Web服务器设置
    private final JBCheckBox webServerEnabledCheckBox = new JBCheckBox("启用Web服务器");
    private final JSpinner webServerPortSpinner = new JSpinner(new SpinnerNumberModel(8090, 1024, 65535, 1));
    
    private final JButton clearDataButton = new JButton("清空所有数据");
    private final JButton exportReportButton = new JButton("立即导出报告");
    private final JButton viewStatsButton = new JButton("查看统计信息");
    
    private final JLabel totalTimeSavedLabel = new JLabel("0.0 小时");
    private final JLabel totalCompletionsLabel = new JLabel("0");
    private final JLabel avgEfficiencyLabel = new JLabel("0.0%");
    
    public MetricsSettingsComponent() {
        mainPanel = createMainPanel();
        setupEventHandlers();
        updateStatistics();
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 基本设置面板
        JPanel basicSettingsPanel = createBasicSettingsPanel();
        panel.add(basicSettingsPanel, BorderLayout.NORTH);
        
        // 统计信息面板
        JPanel statsPanel = createStatisticsPanel();
        panel.add(statsPanel, BorderLayout.CENTER);
        
        // 操作按钮面板
        JPanel actionsPanel = createActionsPanel();
        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBasicSettingsPanel() {
        JPanel panel = FormBuilder.createFormBuilder()
            .addComponent(metricsEnabledCheckBox, 1)
            .addVerticalGap(10)
            .addComponent(onlyTrackAIUsageCheckBox, 1)
            .addComponent(autoDetectionCheckBox, 1)
            .addVerticalGap(10)
            .addComponent(autoExportEnabledCheckBox, 1)
            .addLabeledComponent(new JBLabel("导出间隔(小时):"), exportIntervalSpinner, 1, false)
            .addVerticalGap(10)
            .addComponent(detailedLoggingCheckBox, 1)
            .addVerticalGap(10)
            .addComponent(webServerEnabledCheckBox, 1)
            .addLabeledComponent(new JBLabel("Web服务器端口:"), webServerPortSpinner, 1, false)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        
        panel.setBorder(new TitledBorder("基本设置"));
        
        // 添加提示信息
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(panel, BorderLayout.CENTER);
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("<html><small><b>建议：</b>启用\"仅跟踪真实AI使用\"以获得更准确的统计数据</small></html>");
        infoLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(infoLabel);
        wrapperPanel.add(infoPanel, BorderLayout.SOUTH);
        
        return wrapperPanel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("统计概览"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 总节省时间
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("总节省时间:"), gbc);
        gbc.gridx = 1;
        totalTimeSavedLabel.setFont(totalTimeSavedLabel.getFont().deriveFont(Font.BOLD));
        totalTimeSavedLabel.setForeground(new Color(0, 150, 0));
        panel.add(totalTimeSavedLabel, gbc);
        
        // 总补全次数
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("代码补全次数:"), gbc);
        gbc.gridx = 1;
        totalCompletionsLabel.setFont(totalCompletionsLabel.getFont().deriveFont(Font.BOLD));
        totalCompletionsLabel.setForeground(new Color(0, 100, 200));
        panel.add(totalCompletionsLabel, gbc);
        
        // 平均效率提升
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("平均效率提升:"), gbc);
        gbc.gridx = 1;
        avgEfficiencyLabel.setFont(avgEfficiencyLabel.getFont().deriveFont(Font.BOLD));
        avgEfficiencyLabel.setForeground(new Color(150, 0, 150));
        panel.add(avgEfficiencyLabel, gbc);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("操作"));
        
        panel.add(viewStatsButton);
        panel.add(exportReportButton);
        panel.add(clearDataButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // 启用/禁用相关控件
        metricsEnabledCheckBox.addActionListener(e -> {
            boolean enabled = metricsEnabledCheckBox.isSelected();
            autoExportEnabledCheckBox.setEnabled(enabled);
            exportIntervalSpinner.setEnabled(enabled && autoExportEnabledCheckBox.isSelected());
            detailedLoggingCheckBox.setEnabled(enabled);
            autoDetectionCheckBox.setEnabled(enabled);
            onlyTrackAIUsageCheckBox.setEnabled(enabled);
            exportReportButton.setEnabled(enabled);
            viewStatsButton.setEnabled(enabled);
        });
        
        autoExportEnabledCheckBox.addActionListener(e -> {
            exportIntervalSpinner.setEnabled(autoExportEnabledCheckBox.isSelected());
        });
        
        // 互斥选项处理
        onlyTrackAIUsageCheckBox.addActionListener(e -> {
            if (onlyTrackAIUsageCheckBox.isSelected()) {
                autoDetectionCheckBox.setSelected(false);
            }
        });
        
        autoDetectionCheckBox.addActionListener(e -> {
            if (autoDetectionCheckBox.isSelected()) {
                onlyTrackAIUsageCheckBox.setSelected(false);
                // 显示警告
                JOptionPane.showMessageDialog(mainPanel,
                    "警告：启用自动检测可能会将普通编辑误判为AI代码补全，\n" +
                    "建议使用\"仅跟踪真实AI使用\"选项以获得更准确的数据。",
                    "警告",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // 按钮事件
        viewStatsButton.addActionListener(e -> showDetailedStatistics());
        exportReportButton.addActionListener(e -> exportReport());
        clearDataButton.addActionListener(e -> clearAllData());
    }
    
    private void updateStatistics() {
        try {
            ProductivityMetrics.ProductivityReport report = 
                ProductivityMetrics.getInstance().getProductivityReport(30); // 最近30天
            
            totalTimeSavedLabel.setText(String.format("%.1f 小时", report.totalTimeSavedHours));
            totalCompletionsLabel.setText(String.valueOf(report.totalLinesGenerated));
            avgEfficiencyLabel.setText(String.format("%.1f%%", report.avgEfficiencyGain));
            
        } catch (Exception e) {
            totalTimeSavedLabel.setText("数据加载失败");
            totalCompletionsLabel.setText("--");
            avgEfficiencyLabel.setText("--");
        }
    }
    
    private void showDetailedStatistics() {
        try {
            ProductivityMetrics.ProductivityReport report = 
                ProductivityMetrics.getInstance().getProductivityReport(30);
            
            StringBuilder stats = new StringBuilder();
            stats.append("=== 详细统计信息 (最近30天) ===\n\n");
            stats.append("📊 总体数据:\n");
            stats.append("- 总节省时间: ").append(String.format("%.1f", report.totalTimeSavedHours)).append(" 小时\n");
            stats.append("- 平均效率提升: ").append(String.format("%.1f", report.avgEfficiencyGain)).append("%\n");
            stats.append("- 代码接受率: ").append(String.format("%.1f", report.avgCodeAcceptanceRate * 100)).append("%\n");
            stats.append("- 生成代码行数: ").append(report.totalLinesGenerated).append(" 行\n\n");
            
            stats.append("🎯 效率分析:\n");
            if (report.avgEfficiencyGain > 50) {
                stats.append("- 🎉 AI助手显著提升了您的开发效率！\n");
            } else if (report.avgEfficiencyGain > 20) {
                stats.append("- ✅ AI助手有效提升了您的开发效率\n");
            } else {
                stats.append("- 📈 AI助手正在逐步提升您的开发效率\n");
            }
            
            // 显示统计信息
            JTextArea textArea = new JTextArea(stats.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));
            
            JOptionPane.showMessageDialog(mainPanel, scrollPane, "详细统计信息", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, 
                "获取统计信息时发生错误: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportReport() {
        try {
            ProductivityMetrics.ProductivityReport report = 
                ProductivityMetrics.getInstance().getProductivityReport(30);
            
            // 实现文件导出逻辑
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出效能报告");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setSelectedFile(new java.io.File("ProxyAI_效能报告_" + 
                java.time.LocalDate.now().toString() + ".txt"));
            
            int userSelection = fileChooser.showSaveDialog(mainPanel);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                
                // 生成详细报告内容
                StringBuilder reportContent = new StringBuilder();
                reportContent.append("ProxyAI 效能度量报告\n");
                reportContent.append("生成时间: ").append(java.time.LocalDateTime.now()).append("\n");
                reportContent.append("统计周期: 最近30天\n\n");
                
                reportContent.append("=== 总体统计 ===\n");
                reportContent.append("总节省时间: ").append(String.format("%.1f", report.totalTimeSavedHours)).append(" 小时\n");
                reportContent.append("平均效率提升: ").append(String.format("%.1f", report.avgEfficiencyGain)).append("%\n");
                reportContent.append("代码接受率: ").append(String.format("%.1f", report.avgCodeAcceptanceRate * 100)).append("%\n");
                reportContent.append("生成代码行数: ").append(report.totalLinesGenerated).append(" 行\n\n");
                
                reportContent.append("=== 效率分析 ===\n");
                if (report.avgEfficiencyGain > 50) {
                    reportContent.append("🎉 AI助手显著提升了您的开发效率！\n");
                } else if (report.avgEfficiencyGain > 20) {
                    reportContent.append("✅ AI助手有效提升了您的开发效率\n");
                } else {
                    reportContent.append("📈 AI助手正在逐步提升您的开发效率\n");
                }
                
                reportContent.append("\n=== 建议 ===\n");
                reportContent.append("- 继续使用AI代码补全功能以提高编程效率\n");
                reportContent.append("- 定期查看效能统计以了解改进情况\n");
                reportContent.append("- 启用\"仅跟踪真实AI使用\"以获得更准确的数据\n");
                
                // 写入文件
                try (java.io.FileWriter writer = new java.io.FileWriter(fileToSave)) {
                    writer.write(reportContent.toString());
                }
                
                JOptionPane.showMessageDialog(mainPanel, 
                    "报告已成功导出到:\n" + fileToSave.getAbsolutePath(), 
                    "导出成功", JOptionPane.INFORMATION_MESSAGE);
            }
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, 
                "导出报告时发生错误: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearAllData() {
        int result = JOptionPane.showConfirmDialog(mainPanel,
            "确定要清空所有提效度量数据吗？\n此操作不可撤销！",
            "确认清空数据",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                // 实现清空数据的逻辑
                ProductivityMetrics metrics = ProductivityMetrics.getInstance();
                
                // 清空所有统计数据
                metrics.clearAllData();
                
                // 同时清空AIUsageTracker的数据
                ee.carlrobert.codegpt.metrics.AIUsageTracker aiTracker = 
                    ee.carlrobert.codegpt.metrics.AIUsageTracker.getInstance();
                if (aiTracker != null) {
                    aiTracker.clearAllData();
                }
                
                // 更新界面显示
                updateStatistics();
                
                JOptionPane.showMessageDialog(mainPanel, 
                    "所有提效度量数据已清空！\n" +
                    "- 代码补全记录已清空\n" +
                    "- 聊天代码生成记录已清空\n" +
                    "- 时间节省记录已清空\n" +
                    "- AI使用跟踪数据已清空", 
                    "操作完成", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "清空数据时发生错误: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Getter和Setter方法
    public JPanel getPanel() {
        return mainPanel;
    }
    
    public boolean isMetricsEnabled() {
        return metricsEnabledCheckBox.isSelected();
    }
    
    public void setMetricsEnabled(boolean enabled) {
        metricsEnabledCheckBox.setSelected(enabled);
    }
    
    public boolean isAutoExportEnabled() {
        return autoExportEnabledCheckBox.isSelected();
    }
    
    public void setAutoExportEnabled(boolean enabled) {
        autoExportEnabledCheckBox.setSelected(enabled);
    }
    
    public int getExportInterval() {
        return (Integer) exportIntervalSpinner.getValue();
    }
    
    public void setExportInterval(int interval) {
        exportIntervalSpinner.setValue(interval);
    }
    
    public boolean isDetailedLoggingEnabled() {
        return detailedLoggingCheckBox.isSelected();
    }
    
    public void setDetailedLoggingEnabled(boolean enabled) {
        detailedLoggingCheckBox.setSelected(enabled);
    }
    
    public boolean isAutoDetectionEnabled() {
        return autoDetectionCheckBox.isSelected();
    }
    
    public void setAutoDetectionEnabled(boolean enabled) {
        autoDetectionCheckBox.setSelected(enabled);
    }
    
    public boolean isOnlyTrackAIUsage() {
        return onlyTrackAIUsageCheckBox.isSelected();
    }
    
    public void setOnlyTrackAIUsage(boolean enabled) {
        onlyTrackAIUsageCheckBox.setSelected(enabled);
    }
}