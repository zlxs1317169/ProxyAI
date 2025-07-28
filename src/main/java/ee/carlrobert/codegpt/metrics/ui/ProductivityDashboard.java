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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
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
        
        // 定期更新数据
        scheduler.scheduleAtFixedRate(dashboardPanel::updateData, 0, 30, TimeUnit.SECONDS);
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
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    // 显示错误信息
                    JOptionPane.showMessageDialog(this, 
                        "更新数据时发生错误: " + e.getMessage(), 
                        "错误", JOptionPane.ERROR_MESSAGE);
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
            
            // 添加一些示例活动记录
            recentActivitiesModel.addElement("✅ 代码补全 - Java类方法 (2分钟前)");
            recentActivitiesModel.addElement("💬 AI聊天 - 调试帮助 (5分钟前)");
            recentActivitiesModel.addElement("🔧 代码重构 - 性能优化 (10分钟前)");
            recentActivitiesModel.addElement("📝 提交信息生成 (15分钟前)");
            recentActivitiesModel.addElement("🎯 变量命名建议 (20分钟前)");
        }
        
        private void clearActivities() {
            int result = JOptionPane.showConfirmDialog(this,
                "确定要清空所有活动记录吗？",
                "确认清空",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                recentActivitiesModel.clear();
            }
        }
        
        private void showActivityDetail() {
            String selected = recentActivitiesList.getSelectedValue();
            if (selected != null) {
                JOptionPane.showMessageDialog(this,
                    "活动详情:\n" + selected + "\n\n" +
                    "时间: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
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
                
                StringBuilder reportText = new StringBuilder();
                reportText.append("=== ProxyAI 提效统计报告 ===\n");
                reportText.append("生成时间: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
                
                reportText.append("📊 总体统计:\n");
                reportText.append("- 总节省时间: ").append(String.format("%.1f", report.totalTimeSavedHours)).append(" 小时\n");
                reportText.append("- 平均效率提升: ").append(String.format("%.1f", report.avgEfficiencyGain)).append("%\n");
                reportText.append("- 代码接受率: ").append(String.format("%.1f", report.avgCodeAcceptanceRate * 100)).append("%\n");
                reportText.append("- 生成代码行数: ").append(report.totalLinesGenerated).append(" 行\n\n");
                
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
                
                // 显示报告
                JTextArea textArea = new JTextArea(reportText.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(600, 400));
                
                JOptionPane.showMessageDialog(this, scrollPane, "提效统计报告", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "导出报告时发生错误: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
