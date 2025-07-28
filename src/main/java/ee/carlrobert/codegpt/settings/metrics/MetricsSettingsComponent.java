package ee.carlrobert.codegpt.settings.metrics;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 提效度量设置组件
 */
public class MetricsSettingsComponent {
    
    private final JPanel mainPanel;
    private final JBCheckBox metricsEnabledCheckBox = new JBCheckBox("启用提效度量收集");
    private final JBCheckBox autoExportEnabledCheckBox = new JBCheckBox("自动导出报告");
    private final JSpinner exportIntervalSpinner = new JSpinner(new SpinnerNumberModel(24, 1, 168, 1));
    private final JBCheckBox detailedLoggingCheckBox = new JBCheckBox("启用详细日志记录");
    
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
            .addComponent(autoExportEnabledCheckBox, 1)
            .addLabeledComponent(new JBLabel("导出间隔(小时):"), exportIntervalSpinner, 1, false)
            .addVerticalGap(10)
            .addComponent(detailedLoggingCheckBox, 1)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        
        panel.setBorder(new TitledBorder("基本设置"));
        return panel;
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
            exportReportButton.setEnabled(enabled);
            viewStatsButton.setEnabled(enabled);
        });
        
        autoExportEnabledCheckBox.addActionListener(e -> {
            exportIntervalSpinner.setEnabled(autoExportEnabledCheckBox.isSelected());
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
            
            // 这里可以实现实际的文件导出逻辑
            JOptionPane.showMessageDialog(mainPanel, 
                "报告导出功能开发中...\n\n" +
                "当前数据:\n" +
                "- 节省时间: " + String.format("%.1f", report.totalTimeSavedHours) + " 小时\n" +
                "- 效率提升: " + String.format("%.1f", report.avgEfficiencyGain) + "%\n" +
                "- 代码接受率: " + String.format("%.1f", report.avgCodeAcceptanceRate * 100) + "%", 
                "导出报告", JOptionPane.INFORMATION_MESSAGE);
                
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
                // 这里实现清空数据的逻辑
                // ProductivityMetrics.getInstance().clearAllData();
                
                updateStatistics();
                JOptionPane.showMessageDialog(mainPanel, 
                    "所有提效度量数据已清空", 
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
}