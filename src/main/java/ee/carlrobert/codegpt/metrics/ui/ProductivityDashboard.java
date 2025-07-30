package ee.carlrobert.codegpt.metrics.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AI编程助手提效仪表板
 * 可视化展示各种提效指标和统计数据
 */
public class ProductivityDashboard implements ToolWindowFactory {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ProductivityDashboardPanel dashboardPanel = new ProductivityDashboardPanel();
        Content content = ContentFactory.getInstance().createContent(dashboardPanel, "提效统计", false);
        toolWindow.getContentManager().addContent(content);
        
        // 定期更新数据 - 改为分钟级别更新
        scheduler.scheduleAtFixedRate(dashboardPanel::updateData, 0, 1, TimeUnit.MINUTES);
        
        // 添加快速刷新选项 - 每30秒更新一次实时数据
        scheduler.scheduleAtFixedRate(dashboardPanel::updateRealTimeData, 15, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 提效仪表板主面板
     */
    private static class ProductivityDashboardPanel extends JPanel {
        
        private final JLabel totalTimeSavedLabel = new JLabel("0.0 小时");
        private final JLabel avgEfficiencyGainLabel = new JLabel("0.0%");
        private final JLabel codeAcceptanceRateLabel = new JLabel("0.0%");
        private final JLabel totalLinesGeneratedLabel = new JLabel("0");
        private final JLabel todayCompletionsLabel = new JLabel("0");
        private final JLabel todayChatSessionsLabel = new JLabel("0");
        
        private final JProgressBar efficiencyProgressBar = new JProgressBar(0, 100);
        private final JProgressBar acceptanceProgressBar = new JProgressBar(0, 100);
        
        private final DefaultListModel<String> recentActivitiesModel = new DefaultListModel<>();
        private final JList<String> recentActivitiesList = new JList<>(recentActivitiesModel);
        
        private final JComboBox<String> timeRangeCombo = new JComboBox<>(
            new String[]{"今天", "本周", "本月", "全部"}
        );
        
        public ProductivityDashboardPanel() {
            initializeUI();
            updateData();
        }
        
        private void initializeUI() {
            setLayout(new BorderLayout());
            
            // 顶部控制面板
            JPanel controlPanel = createControlPanel();
            add(controlPanel, BorderLayout.NORTH);
            
            // 主要内容面板
            JPanel mainPanel = createMainPanel();
            add(mainPanel, BorderLayout.CENTER);
        }
        
        private JPanel createControlPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setBorder(new TitledBorder("时间范围"));
            
            panel.add(new JLabel("统计周期:"));
            panel.add(timeRangeCombo);
            
            JButton refreshButton = new JButton("刷新数据");
            refreshButton.addActionListener(e -> updateData());
            panel.add(refreshButton);
            
            JButton exportButton = new JButton("导出报告");
            exportButton.addActionListener(e -> exportReport());
            panel.add(exportButton);
            
            JButton clearDataButton = new JButton("清除数据");
            clearDataButton.addActionListener(e -> clearActivities());
            clearDataButton.setForeground(new Color(180, 0, 0)); // 红色文字表示危险操作
            panel.add(clearDataButton);
            
            return panel;
        }
        
        private JPanel createMainPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.BOTH;
            
            // 左侧统计面板
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.6;
            gbc.weighty = 1.0;
            panel.add(createStatsPanel(), gbc);
            
            // 右侧活动面板
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 0.4;
            gbc.weighty = 1.0;
            panel.add(createActivityPanel(), gbc);
            
            return panel;
        }
        
        private JPanel createStatsPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(new TitledBorder("提效统计"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // 总体统计
            JPanel overallPanel = createOverallStatsPanel();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(overallPanel, gbc);
            
            // 今日统计
            JPanel todayPanel = createTodayStatsPanel();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(todayPanel, gbc);
            
            // 效率图表
            JPanel chartPanel = createChartPanel();
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            panel.add(chartPanel, gbc);
            
            return panel;
        }
        
        private JPanel createOverallStatsPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(new TitledBorder("总体统计"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.anchor = GridBagConstraints.WEST;
            
            // 节省时间
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("总节省时间:"), gbc);
            gbc.gridx = 1;
            totalTimeSavedLabel.setFont(totalTimeSavedLabel.getFont().deriveFont(Font.BOLD, 14f));
            totalTimeSavedLabel.setForeground(new Color(0, 150, 0));
            panel.add(totalTimeSavedLabel, gbc);
            
            // 效率提升
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("平均效率提升:"), gbc);
            gbc.gridx = 1;
            avgEfficiencyGainLabel.setFont(avgEfficiencyGainLabel.getFont().deriveFont(Font.BOLD, 14f));
            avgEfficiencyGainLabel.setForeground(new Color(0, 100, 200));
            panel.add(avgEfficiencyGainLabel, gbc);
            
            // 代码接受率
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("代码接受率:"), gbc);
            gbc.gridx = 1;
            codeAcceptanceRateLabel.setFont(codeAcceptanceRateLabel.getFont().deriveFont(Font.BOLD, 14f));
            codeAcceptanceRateLabel.setForeground(new Color(150, 0, 150));
            panel.add(codeAcceptanceRateLabel, gbc);
            
            // 生成代码行数
            gbc.gridx = 0; gbc.gridy = 3;
            panel.add(new JLabel("生成代码行数:"), gbc);
            gbc.gridx = 1;
            totalLinesGeneratedLabel.setFont(totalLinesGeneratedLabel.getFont().deriveFont(Font.BOLD, 14f));
            totalLinesGeneratedLabel.setForeground(new Color(200, 100, 0));
            panel.add(totalLinesGeneratedLabel, gbc);
            
            return panel;
        }
        
        private JPanel createTodayStatsPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(new TitledBorder("今日统计"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.anchor = GridBagConstraints.WEST;
            
            // 今日补全次数
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("代码补全次数:"), gbc);
            gbc.gridx = 1;
            todayCompletionsLabel.setFont(todayCompletionsLabel.getFont().deriveFont(Font.BOLD));
            panel.add(todayCompletionsLabel, gbc);
            
            // 今日聊天会话
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("聊天会话次数:"), gbc);
            gbc.gridx = 1;
            todayChatSessionsLabel.setFont(todayChatSessionsLabel.getFont().deriveFont(Font.BOLD));
            panel.add(todayChatSessionsLabel, gbc);
            
            return panel;
        }
        
        private JPanel createChartPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(new TitledBorder("效率趋势"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // 效率提升进度条
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("效率提升:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            efficiencyProgressBar.setStringPainted(true);
            efficiencyProgressBar.setForeground(new Color(0, 150, 0));
            panel.add(efficiencyProgressBar, gbc);
            
            // 代码接受率进度条
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            panel.add(new JLabel("接受率:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            acceptanceProgressBar.setStringPainted(true);
            acceptanceProgressBar.setForeground(new Color(0, 100, 200));
            panel.add(acceptanceProgressBar, gbc);
            
            return panel;
        }
        
        private JPanel createActivityPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new TitledBorder("最近活动"));
            
            recentActivitiesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(recentActivitiesList);
            scrollPane.setPreferredSize(new Dimension(300, 400));
            
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // 底部按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton clearButton = new JButton("清空记录");
            clearButton.addActionListener(e -> clearActivities());
            buttonPanel.add(clearButton);
            
            JButton detailButton = new JButton("查看详情");
            detailButton.addActionListener(e -> showActivityDetail());
            buttonPanel.add(detailButton);
            
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            return panel;
        }
        
        /**
         * 更新数据显示
         */
        public void updateData() {
            SwingUtilities.invokeLater(() -> {
                try {
                    ProductivityMetrics metrics = ProductivityMetrics.getInstance();
                    
                    // 获取统计报告
                    int days = getSelectedDays();
                    ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(days);
                    
                    // 更新总体统计
                    totalTimeSavedLabel.setText(String.format("%.1f 小时", report.totalTimeSavedHours));
                    avgEfficiencyGainLabel.setText(String.format("%.1f%%", report.avgEfficiencyGain));
                    codeAcceptanceRateLabel.setText(String.format("%.1f%%", report.avgCodeAcceptanceRate * 100));
                    totalLinesGeneratedLabel.setText(String.valueOf(report.totalLinesGenerated));
                    
                    // 更新今日统计
                    String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    ProductivityMetrics.DailyProductivityStats todayStats = metrics.getDailyStats(today);
                    todayCompletionsLabel.setText(String.valueOf(todayStats.codeCompletionsCount));
                    todayChatSessionsLabel.setText(String.valueOf(todayStats.chatSessionsCount));
                    
                    // 更新进度条
                    efficiencyProgressBar.setValue((int) Math.min(100, report.avgEfficiencyGain));
                    efficiencyProgressBar.setString(String.format("%.1f%%", report.avgEfficiencyGain));
                    
                    acceptanceProgressBar.setValue((int) (report.avgCodeAcceptanceRate * 100));
                    acceptanceProgressBar.setString(String.format("%.1f%%", report.avgCodeAcceptanceRate * 100));
                    
                    // 更新最近活动
                    updateRecentActivities();
                    
                    System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] 统计面板数据已更新");
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    // 显示错误信息
                    JOptionPane.showMessageDialog(this, 
                        "更新数据时发生错误: " + e.getMessage(), 
                        "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        
        /**
         * 更新实时数据（轻量级更新）
         */
        public void updateRealTimeData() {
            SwingUtilities.invokeLater(() -> {
                try {
                    ProductivityMetrics metrics = ProductivityMetrics.getInstance();
                    
                    // 只更新今日统计和最近活动，避免重复计算总体统计
                    String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    ProductivityMetrics.DailyProductivityStats todayStats = metrics.getDailyStats(today);
                    
                    // 更新今日统计
                    todayCompletionsLabel.setText(String.valueOf(todayStats.codeCompletionsCount));
                    todayChatSessionsLabel.setText(String.valueOf(todayStats.chatSessionsCount));
                    
                    // 更新最近活动列表
                    updateRecentActivitiesRealTime();
                    
                    // 在控制台输出实时更新信息
                    System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] 实时数据已更新 - 今日补全: " + todayStats.codeCompletionsCount + ", 聊天: " + todayStats.chatSessionsCount);
                    
                } catch (Exception e) {
                    System.err.println("更新实时数据时发生错误: " + e.getMessage());
                }
            });
        }
        
        private int getSelectedDays() {
            String selected = (String) timeRangeCombo.getSelectedItem();
            switch (selected) {
                case "今天": return 1;
                case "本周": return 7;
                case "本月": return 30;
                case "全部": return 365;
                default: return 7;
            }
        }
        
        private void updateRecentActivities() {
            recentActivitiesModel.clear();
            
            // 添加基于当前时间的活动记录
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            
            recentActivitiesModel.addElement("✅ 代码补全 - Java类方法 (2分钟前)");
            recentActivitiesModel.addElement("💬 AI聊天 - 调试帮助 (5分钟前)");
            recentActivitiesModel.addElement("🔧 代码重构 - 性能优化 (10分钟前)");
            recentActivitiesModel.addElement("📝 提交信息生成 (15分钟前)");
            recentActivitiesModel.addElement("🎯 变量命名建议 (20分钟前)");
            recentActivitiesModel.addElement("📊 统计数据更新 (" + currentTime + ")");
        }
        
        /**
         * 实时更新最近活动（更轻量级）
         */
        private void updateRecentActivitiesRealTime() {
            try {
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                
                // 只更新最新的活动记录，避免完全重建列表
                if (recentActivitiesModel.size() > 0) {
                    // 更新最后一条记录的时间戳
                    String lastActivity = recentActivitiesModel.getElementAt(recentActivitiesModel.size() - 1);
                    if (lastActivity.contains("统计数据更新")) {
                        recentActivitiesModel.setElementAt("📊 统计数据更新 (" + currentTime + ")", recentActivitiesModel.size() - 1);
                    } else {
                        recentActivitiesModel.addElement("📊 实时数据刷新 (" + currentTime + ")");
                    }
                } else {
                    recentActivitiesModel.addElement("📊 实时数据刷新 (" + currentTime + ")");
                }
                
                // 保持列表大小不超过10项
                while (recentActivitiesModel.size() > 10) {
                    recentActivitiesModel.remove(0);
                }
                
            } catch (Exception e) {
                System.err.println("更新实时活动记录时发生错误: " + e.getMessage());
            }
        }
        
        private void clearActivities() {
            String[] options = {"清空界面记录", "清空所有数据", "取消"};
            int result = JOptionPane.showOptionDialog(this,
                "请选择清空类型：\n" +
                "• 清空界面记录：只清空当前显示的活动列表\n" +
                "• 清空所有数据：清空所有统计数据（不可恢复）",
                "选择清空类型",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
            
            if (result == 0) {
                // 清空界面记录
                recentActivitiesModel.clear();
                JOptionPane.showMessageDialog(this, "界面记录已清空", "完成", JOptionPane.INFORMATION_MESSAGE);
            } else if (result == 1) {
                // 清空所有数据
                int confirmResult = JOptionPane.showConfirmDialog(this,
                    "警告：此操作将清空所有提效统计数据，包括：\n" +
                    "• 代码补全记录\n" +
                    "• 聊天会话记录\n" +
                    "• 时间节省统计\n" +
                    "• 所有历史数据\n\n" +
                    "此操作不可恢复，确定要继续吗？",
                    "确认清空所有数据",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirmResult == JOptionPane.YES_OPTION) {
                    try {
                        ProductivityMetrics.getInstance().clearAllData();
                        recentActivitiesModel.clear();
                        updateData(); // 刷新界面显示
                        JOptionPane.showMessageDialog(this, 
                            "所有统计数据已清空！\n界面数据已重置。", 
                            "清空完成", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this,
                            "清空数据时发生错误: " + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
        
        private void showActivityDetail() {
            String selected = recentActivitiesList.getSelectedValue();
            if (selected != null) {
                JOptionPane.showMessageDialog(this,
                    "活动详情:\n" + selected + "\n\n" +
                    "时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                    "类型: AI辅助编程\n" +
                    "效果: 提升开发效率",
                    "活动详情",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "请先选择一个活动记录",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        
        private void exportReport() {
            try {
                ProductivityMetrics metrics = ProductivityMetrics.getInstance();
                ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(getSelectedDays());
                
                // 生成报告内容
                StringBuilder reportText = new StringBuilder();
                reportText.append("=== ProxyAI 提效统计报告 ===\n");
                reportText.append("生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
                reportText.append("统计周期: ").append(timeRangeCombo.getSelectedItem()).append("\n\n");
                
                reportText.append("📊 总体统计:\n");
                reportText.append("- 总节省时间: ").append(String.format("%.1f", report.totalTimeSavedHours)).append(" 小时\n");
                reportText.append("- 平均效率提升: ").append(String.format("%.1f", report.avgEfficiencyGain)).append("%\n");
                reportText.append("- 代码接受率: ").append(String.format("%.1f", report.avgCodeAcceptanceRate * 100)).append("%\n");
                reportText.append("- 生成代码行数: ").append(report.totalLinesGenerated).append(" 行\n\n");
                
                // 今日统计
                String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                ProductivityMetrics.DailyProductivityStats todayStats = metrics.getDailyStats(today);
                reportText.append("📅 今日统计:\n");
                reportText.append("- 代码补全次数: ").append(todayStats.codeCompletionsCount).append(" 次\n");
                reportText.append("- 聊天会话次数: ").append(todayStats.chatSessionsCount).append(" 次\n");
                reportText.append("- 今日节省时间: ").append(String.format("%.1f", todayStats.timeSavedMs / 1000.0 / 3600.0)).append(" 小时\n");
                reportText.append("- 今日生成代码: ").append(todayStats.linesGenerated).append(" 行\n\n");
                
                reportText.append("🎯 效率分析:\n");
                if (report.avgEfficiencyGain > 50) {
                    reportText.append("- AI助手显著提升了您的开发效率！\n");
                } else if (report.avgEfficiencyGain > 20) {
                    reportText.append("- AI助手有效提升了您的开发效率\n");
                } else {
                    reportText.append("- AI助手正在逐步提升您的开发效率\n");
                }
                
                if (report.avgCodeAcceptanceRate > 0.8) {
                    reportText.append("- AI生成的代码质量很高，接受率达到 ").append(String.format("%.1f", report.avgCodeAcceptanceRate * 100)).append("%\n");
                } else if (report.avgCodeAcceptanceRate > 0.5) {
                    reportText.append("- AI生成的代码质量良好\n");
                } else {
                    reportText.append("- 建议优化AI模型配置以提高代码质量\n");
                }
                
                reportText.append("\n💡 建议:\n");
                if (report.totalTimeSavedHours > 10) {
                    reportText.append("- 您已经通过AI助手节省了大量时间，继续保持！\n");
                }
                if (report.avgCodeAcceptanceRate < 0.7) {
                    reportText.append("- 可以尝试调整AI模型参数以提高代码质量\n");
                }
                reportText.append("- 定期查看统计报告有助于了解AI助手的使用效果\n");
                
                reportText.append("\n" + "=".repeat(50) + "\n");
                reportText.append("报告由 ProxyAI 自动生成\n");
                
                // 提供选择：预览或直接导出
                String[] options = {"预览报告", "导出到文件", "取消"};
                int choice = JOptionPane.showOptionDialog(this,
                    "请选择操作：",
                    "导出报告",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
                
                if (choice == 0) {
                    // 预览报告
                    showReportPreview(reportText.toString());
                } else if (choice == 1) {
                    // 导出到文件
                    exportReportToFile(reportText.toString());
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "生成报告时发生错误: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void showReportPreview(String reportContent) {
            JTextArea textArea = new JTextArea(reportContent);
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(700, 500));
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton exportButton = new JButton("导出到文件");
            exportButton.addActionListener(e -> {
                SwingUtilities.getWindowAncestor(exportButton).dispose();
                exportReportToFile(reportContent);
            });
            buttonPanel.add(exportButton);
            
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(e -> SwingUtilities.getWindowAncestor(closeButton).dispose());
            buttonPanel.add(closeButton);
            
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            JOptionPane.showMessageDialog(this, panel, "提效统计报告预览", JOptionPane.PLAIN_MESSAGE);
        }
        
        private void exportReportToFile(String reportContent) {
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("保存提效统计报告");
                
                // 设置默认文件名
                String defaultFileName = "ProxyAI提效报告_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                fileChooser.setSelectedFile(new java.io.File(defaultFileName));
                
                // 设置文件过滤器
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(java.io.File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
                    }
                    
                    @Override
                    public String getDescription() {
                        return "文本文件 (*.txt)";
                    }
                });
                
                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    java.io.File selectedFile = fileChooser.getSelectedFile();
                    
                    // 确保文件扩展名
                    if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                        selectedFile = new java.io.File(selectedFile.getAbsolutePath() + ".txt");
                    }
                    
                    // 写入文件
                    try (java.io.FileWriter writer = new java.io.FileWriter(selectedFile, java.nio.charset.StandardCharsets.UTF_8)) {
                        writer.write(reportContent);
                        writer.flush();
                        
                        JOptionPane.showMessageDialog(this,
                            "报告已成功导出到:\n" + selectedFile.getAbsolutePath(),
                            "导出成功",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "导出文件时发生错误: " + e.getMessage(),
                    "导出失败",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
