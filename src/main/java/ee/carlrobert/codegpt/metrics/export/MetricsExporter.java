package ee.carlrobert.codegpt.metrics.export;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * Productivity Metrics Export Tool
 * Supports exporting metrics data to CSV, JSON and HTML formats
 */
public class MetricsExporter extends AnAction {

    private static final Logger LOG = Logger.getInstance(MetricsExporter.class);

    public MetricsExporter() {
        super("Export Productivity Metrics", "Export productivity metrics data to CSV, JSON or HTML format", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        try {
            // Get ProductivityMetrics instance
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            if (metrics == null) {
                showError(project, "Cannot get metrics data", "Cannot get ProductivityMetrics instance");
                return;
            }

            // Show export options dialog
            showExportDialog(project, metrics);

        } catch (Exception ex) {
            LOG.error("Error exporting metrics data", ex);
            showError(project, "Export failed", "Error exporting metrics data: " + ex.getMessage());
        }
    }

    /**
     * Show export options dialog
     */
    private void showExportDialog(Project project, ProductivityMetrics metrics) {
        // Create dialog
        JDialog dialog = new JDialog((JFrame) null, "Export Productivity Metrics", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);

        // Create panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add title
        JLabel titleLabel = new JLabel("Select Export Options");
        titleLabel.setFont(titleLabel.getFont().deriveFont(16.0f));
        titleLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        // Add time range selection
        JPanel timeRangePanel = new JPanel();
        timeRangePanel.setLayout(new BoxLayout(timeRangePanel, BoxLayout.X_AXIS));
        timeRangePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JLabel timeRangeLabel = new JLabel("Time Range: ");
        String[] timeRanges = {"Last 7 days", "Last 30 days", "Last 90 days", "All data"};
        JComboBox<String> timeRangeComboBox = new JComboBox<>(timeRanges);

        timeRangePanel.add(timeRangeLabel);
        timeRangePanel.add(timeRangeComboBox);
        panel.add(timeRangePanel);
        panel.add(Box.createVerticalStrut(10));

        // Add format selection
        JPanel formatPanel = new JPanel();
        formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.X_AXIS));
        formatPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JLabel formatLabel = new JLabel("Export Format: ");
        String[] formats = {"CSV", "JSON", "HTML Report"};
        JComboBox<String> formatComboBox = new JComboBox<>(formats);

        formatPanel.add(formatLabel);
        formatPanel.add(formatComboBox);
        panel.add(formatPanel);
        panel.add(Box.createVerticalStrut(10));

        // Add data type selection
        JPanel dataTypePanel = new JPanel();
        dataTypePanel.setLayout(new BoxLayout(dataTypePanel, BoxLayout.Y_AXIS));
        dataTypePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        dataTypePanel.setBorder(BorderFactory.createTitledBorder("Include Data Types"));

        JCheckBox codeCompletionCheckBox = new JCheckBox("Code Completion Data", true);
        JCheckBox chatCodeCheckBox = new JCheckBox("Chat Code Generation Data", true);
        JCheckBox timeSavingCheckBox = new JCheckBox("Time Saving Data", true);
        JCheckBox debuggingCheckBox = new JCheckBox("Debugging Metrics Data", true);
        JCheckBox codeQualityCheckBox = new JCheckBox("Code Quality Data", true);
        JCheckBox learningCheckBox = new JCheckBox("Learning Efficiency Data", true);
        JCheckBox dailyStatsCheckBox = new JCheckBox("Daily Statistics Data", true);

        dataTypePanel.add(codeCompletionCheckBox);
        dataTypePanel.add(chatCodeCheckBox);
        dataTypePanel.add(timeSavingCheckBox);
        dataTypePanel.add(debuggingCheckBox);
        dataTypePanel.add(codeQualityCheckBox);
        dataTypePanel.add(learningCheckBox);
        dataTypePanel.add(dailyStatsCheckBox);

        panel.add(dataTypePanel);
        panel.add(Box.createVerticalStrut(20));

        // Add buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JButton cancelButton = new JButton("Cancel");
        JButton exportButton = new JButton("Export");

        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(exportButton);

        panel.add(buttonPanel);

        // Set button actions
        cancelButton.addActionListener(e -> dialog.dispose());

        exportButton.addActionListener(e -> {
            dialog.dispose();

            // Get selected options
            String timeRange = (String) timeRangeComboBox.getSelectedItem();
            String format = (String) formatComboBox.getSelectedItem();

            // Create data options
            ExportOptions options = new ExportOptions();
            options.includeCodeCompletion = codeCompletionCheckBox.isSelected();
            options.includeChatCode = chatCodeCheckBox.isSelected();
            options.includeTimeSaving = timeSavingCheckBox.isSelected();
            options.includeDebugging = debuggingCheckBox.isSelected();
            options.includeCodeQuality = codeQualityCheckBox.isSelected();
            options.includeLearning = learningCheckBox.isSelected();
            options.includeDailyStats = dailyStatsCheckBox.isSelected();

            // Calculate days
            int days = 0;
            if ("Last 7 days".equals(timeRange)) {
                days = 7;
            } else if ("Last 30 days".equals(timeRange)) {
                days = 30;
            } else if ("Last 90 days".equals(timeRange)) {
                days = 90;
            } else {
                days = 365; // All data, use a large value
            }

            // Execute export
            exportData(project, metrics, format, days, options);
        });

        // Show dialog
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    /**
     * Export data
     */
    private void exportData(Project project, ProductivityMetrics metrics, String format, int days, ExportOptions options) {
        try {
            // Choose save location
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            descriptor.setTitle("Choose Export Directory");

            VirtualFile selectedDir = FileChooser.chooseFile(descriptor, project, null);
            if (selectedDir == null) {
                return; // User canceled selection
            }

            String dirPath = selectedDir.getPath();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "proxyai_metrics_" + timestamp;

            // Export data based on format
            String filePath;
            switch (format) {
                case "CSV":
                    filePath = exportToCsv(metrics, dirPath, fileName, days, options);
                    break;
                case "JSON":
                    filePath = exportToJson(metrics, dirPath, fileName, days, options);
                    break;
                case "HTML Report":
                    filePath = exportToHtml(metrics, dirPath, fileName, days, options);
                    break;
                default:
                    showError(project, "Export Failed", "Unsupported export format: " + format);
                    return;
            }

            // Show success notification
//            Notifications.Bus.notify(new Notification(
//                "ee.carlrobert.codegpt.notifications",
//                "Export Successful",
//                "Productivity metrics data has been successfully exported to: " + filePath,
//                NotificationType.INFORMATION
//            ));

        } catch (Exception e) {
            LOG.error("Error exporting data", e);
            showError(project, "Export Failed", "Error exporting data: " + e.getMessage());
        }
    }

    /**
     * Export to CSV format
     */
    private String exportToCsv(ProductivityMetrics metrics, String dirPath, String fileName, int days, ExportOptions options) throws IOException {
        String filePath = dirPath + File.separator + fileName + ".csv";

        try (FileWriter writer = new FileWriter(filePath)) {
            // Write CSV header
            writer.write("DataType,Date,Metric1,Metric2,Metric3,Metric4,Metric5\n");

            // Get report data
            ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(days);

            // Write code completion data
            if (options.includeCodeCompletion && metrics.getState().codeCompletions != null) {
                for (Object completion : metrics.getState().codeCompletions) {
                    // Get data via reflection
                    String timestamp = getFieldValue(completion, "timestamp");
                    String language = getFieldValue(completion, "language");
                    int suggestedLines = getIntFieldValue(completion, "suggestedLines");
                    int acceptedLines = getIntFieldValue(completion, "acceptedLines");
                    long responseTimeMs = getLongFieldValue(completion, "responseTimeMs");

                    writer.write(String.format("CodeCompletion,%s,%s,%d,%d,%d,\n",
                        timestamp, language, suggestedLines, acceptedLines, responseTimeMs));
                }
            }

            // Write chat code generation data
            if (options.includeChatCode && metrics.getState().chatCodeGenerations != null) {
                for (Object chatCode : metrics.getState().chatCodeGenerations) {
                    String timestamp = getFieldValue(chatCode, "timestamp");
                    int generatedLines = getIntFieldValue(chatCode, "generatedLines");
                    int appliedLines = getIntFieldValue(chatCode, "appliedLines");
                    long sessionDurationMs = getLongFieldValue(chatCode, "sessionDurationMs");
                    String taskType = getFieldValue(chatCode, "taskType");

                    writer.write(String.format("ChatCodeGeneration,%s,%d,%d,%d,%s,\n",
                        timestamp, generatedLines, appliedLines, sessionDurationMs, taskType));
                }
            }

            // Write time saving data
            if (options.includeTimeSaving && metrics.getState().timeSavings != null) {
                for (Object timeSaving : metrics.getState().timeSavings) {
                    String timestamp = getFieldValue(timeSaving, "timestamp");
                    String taskType = getFieldValue(timeSaving, "taskType");
                    long traditionalTimeMs = getLongFieldValue(timeSaving, "traditionalTimeMs");
                    long aiAssistedTimeMs = getLongFieldValue(timeSaving, "aiAssistedTimeMs");
                    int linesOfCode = getIntFieldValue(timeSaving, "linesOfCode");

                    writer.write(String.format("TimeSaving,%s,%s,%d,%d,%d,\n",
                        timestamp, taskType, traditionalTimeMs, aiAssistedTimeMs, linesOfCode));
                }
            }

            // Write daily statistics data
            if (options.includeDailyStats && metrics.getState().dailyStats != null) {
                // Get daily statistics data
                Map<String, ProductivityMetrics.DailyProductivityStats> dailyStats = metrics.getState().dailyStats;

                for (Map.Entry<String, ProductivityMetrics.DailyProductivityStats> entry : dailyStats.entrySet()) {
                    String date = entry.getKey();
                    ProductivityMetrics.DailyProductivityStats stats = entry.getValue();

                    writer.write(String.format("DailyStats,%s,%d,%d,%d,%d,%.2f\n",
                        date, stats.codeCompletionsCount, stats.chatSessionsCount,
                        stats.timeSavedMs, stats.totalLinesGenerated, stats.avgResponseTime));
                }
            }
        }

        return filePath;
    }

    /**
     * Export to JSON format
     */
    private String exportToJson(ProductivityMetrics metrics, String dirPath, String fileName, int days, ExportOptions options) throws IOException {
        String filePath = dirPath + File.separator + fileName + ".json";

        try (FileWriter writer = new FileWriter(filePath)) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");

            // Add metadata
            json.append("  \"metadata\": {\n");
            json.append("    \"exportDate\": \"").append(new Date()).append("\",\n");
            json.append("    \"days\": ").append(days).append(",\n");
            json.append("    \"version\": \"1.0\"\n");
            json.append("  },\n");

            // Add summary data
            ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(days);
            json.append("  \"summary\": {\n");
            json.append("    \"totalLinesGenerated\": ").append(report.totalLinesGenerated).append(",\n");
            json.append("    \"avgCodeAcceptanceRate\": ").append(report.avgCodeAcceptanceRate).append(",\n");
            json.append("    \"totalTimeSavedHours\": ").append(report.totalTimeSavedHours).append(",\n");
            json.append("    \"avgEfficiencyGain\": ").append(report.avgEfficiencyGain).append("\n");
            json.append("  },\n");

            // Add detailed data
            boolean hasDetailData = false;

            // Code completion data
            if (options.includeCodeCompletion && metrics.getState().codeCompletions != null && !metrics.getState().codeCompletions.isEmpty()) {
                hasDetailData = true;
                json.append("  \"codeCompletions\": [\n");

                boolean first = true;
                for (Object completion : metrics.getState().codeCompletions) {
                    if (!first) json.append(",\n");
                    first = false;

                    json.append("    {\n");
                    json.append("      \"timestamp\": \"").append(getFieldValue(completion, "timestamp")).append("\",\n");
                    json.append("      \"language\": \"").append(getFieldValue(completion, "language")).append("\",\n");
                    json.append("      \"suggestedLines\": ").append(getIntFieldValue(completion, "suggestedLines")).append(",\n");
                    json.append("      \"acceptedLines\": ").append(getIntFieldValue(completion, "acceptedLines")).append(",\n");
                    json.append("      \"responseTimeMs\": ").append(getLongFieldValue(completion, "responseTimeMs")).append(",\n");
                    json.append("      \"acceptanceRate\": ").append(getDoubleFieldValue(completion, "acceptanceRate")).append("\n");
                    json.append("    }");
                }

                json.append("\n  ],\n");
            }

            // Chat code generation data
            if (options.includeChatCode && metrics.getState().chatCodeGenerations != null && !metrics.getState().chatCodeGenerations.isEmpty()) {
                hasDetailData = true;
                json.append("  \"chatCodeGenerations\": [\n");

                boolean first = true;
                for (Object chatCode : metrics.getState().chatCodeGenerations) {
                    if (!first) json.append(",\n");
                    first = false;

                    json.append("    {\n");
                    json.append("      \"timestamp\": \"").append(getFieldValue(chatCode, "timestamp")).append("\",\n");
                    json.append("      \"generatedLines\": ").append(getIntFieldValue(chatCode, "generatedLines")).append(",\n");
                    json.append("      \"appliedLines\": ").append(getIntFieldValue(chatCode, "appliedLines")).append(",\n");
                    json.append("      \"sessionDurationMs\": ").append(getLongFieldValue(chatCode, "sessionDurationMs")).append(",\n");
                    json.append("      \"taskType\": \"").append(getFieldValue(chatCode, "taskType")).append("\",\n");
                    json.append("      \"applicationRate\": ").append(getDoubleFieldValue(chatCode, "applicationRate")).append("\n");
                    json.append("    }");
                }

                json.append("\n  ],\n");
            }

            // Time saving data
            if (options.includeTimeSaving && metrics.getState().timeSavings != null && !metrics.getState().timeSavings.isEmpty()) {
                hasDetailData = true;
                json.append("  \"timeSavings\": [\n");

                boolean first = true;
                for (Object timeSaving : metrics.getState().timeSavings) {
                    if (!first) json.append(",\n");
                    first = false;

                    json.append("    {\n");
                    json.append("      \"timestamp\": \"").append(getFieldValue(timeSaving, "timestamp")).append("\",\n");
                    json.append("      \"taskType\": \"").append(getFieldValue(timeSaving, "taskType")).append("\",\n");
                    json.append("      \"traditionalTimeMs\": ").append(getLongFieldValue(timeSaving, "traditionalTimeMs")).append(",\n");
                    json.append("      \"aiAssistedTimeMs\": ").append(getLongFieldValue(timeSaving, "aiAssistedTimeMs")).append(",\n");
                    json.append("      \"linesOfCode\": ").append(getIntFieldValue(timeSaving, "linesOfCode")).append(",\n");
                    json.append("      \"timeSavedMs\": ").append(getLongFieldValue(timeSaving, "timeSavedMs")).append(",\n");
                    json.append("      \"efficiencyGain\": ").append(getDoubleFieldValue(timeSaving, "efficiencyGain")).append("\n");
                    json.append("    }");
                }

                json.append("\n  ],\n");
            }

            // Daily statistics data
            if (options.includeDailyStats && metrics.getState().dailyStats != null && !metrics.getState().dailyStats.isEmpty()) {
                hasDetailData = true;
                json.append("  \"dailyStats\": {\n");

                boolean first = true;
                for (Map.Entry<String, ProductivityMetrics.DailyProductivityStats> entry : metrics.getState().dailyStats.entrySet()) {
                    if (!first) json.append(",\n");
                    first = false;

                    String date = entry.getKey();
                    ProductivityMetrics.DailyProductivityStats stats = entry.getValue();

                    json.append("    \"").append(date).append("\": {\n");
                    json.append("      \"codeCompletionsCount\": ").append(stats.codeCompletionsCount).append(",\n");
                    json.append("      \"chatSessionsCount\": ").append(stats.chatSessionsCount).append(",\n");
                    json.append("      \"timeSavedMs\": ").append(stats.timeSavedMs).append(",\n");
                    json.append("      \"linesGenerated\": ").append(stats.totalLinesGenerated).append(",\n");
                    json.append("      \"avgResponseTime\": ").append(stats.avgResponseTime).append("\n");
                    json.append("    }");
                }

                json.append("\n  }\n");
            } else if (hasDetailData) {
                // Remove the last comma
                json.setLength(json.length() - 2);
                json.append("\n");
            }

            json.append("}");
            writer.write(json.toString());
        }

        return filePath;
    }

    /**
     * Export to HTML report
     */
    private String exportToHtml(ProductivityMetrics metrics, String dirPath, String fileName, int days, ExportOptions options) throws IOException {
        String filePath = dirPath + File.separator + fileName + ".html";

        try (FileWriter writer = new FileWriter(filePath)) {
            StringBuilder html = new StringBuilder();

            // HTML header
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"en\">\n");
            html.append("<head>\n");
            html.append("  <meta charset=\"UTF-8\">\n");
            html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            html.append("  <title>ProxyAI Productivity Metrics Report</title>\n");
            html.append("  <style>\n");
            html.append("    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; color: #333; }\n");
            html.append("    .container { max-width: 1200px; margin: 0 auto; padding: 20px; }\n");
            html.append("    .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }\n");
            html.append("    .summary { background-color: #f8f9fa; border-radius: 5px; padding: 20px; margin: 20px 0; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }\n");
            html.append("    .chart-container { display: flex; flex-wrap: wrap; justify-content: space-between; margin: 20px 0; }\n");
            html.append("    .chart { width: 48%; background-color: white; border-radius: 5px; padding: 15px; margin-bottom: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }\n");
            html.append("    .data-section { background-color: white; border-radius: 5px; padding: 20px; margin: 20px 0; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }\n");
            html.append("    table { width: 100%; border-collapse: collapse; margin: 15px 0; }\n");
            html.append("    th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }\n");
            html.append("    th { background-color: #f2f2f2; }\n");
            html.append("    tr:hover { background-color: #f5f5f5; }\n");
            html.append("    .footer { text-align: center; margin-top: 30px; padding: 20px; color: #666; font-size: 0.9em; }\n");
            html.append("    .highlight { color: #2980b9; font-weight: bold; }\n");
            html.append("    .stat-card { display: inline-block; width: 18%; background-color: white; border-radius: 5px; padding: 15px; margin: 10px; text-align: center; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }\n");
            html.append("    .stat-value { font-size: 24px; font-weight: bold; color: #2980b9; margin: 10px 0; }\n");
            html.append("    .stat-label { font-size: 14px; color: #666; }\n");
            html.append("    @media (max-width: 768px) { .chart { width: 100%; } .stat-card { width: 45%; } }\n");
            html.append("  </style>\n");
            html.append("  <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
            html.append("</head>\n");
            html.append("<body>\n");

            // Report header
            html.append("  <div class=\"header\">\n");
            html.append("    <h1>ProxyAI Productivity Metrics Report</h1>\n");
            html.append("    <p>Generated on: ").append(new Date()).append("</p>\n");
            html.append("    <p>Data range: Last ").append(days).append(" days</p>\n");
            html.append("  </div>\n");

            html.append("  <div class=\"container\">\n");

            // Summary data
            ProductivityMetrics.ProductivityReport report = metrics.getProductivityReport(days);
            html.append("    <div class=\"summary\">\n");
            html.append("      <h2>Productivity Summary</h2>\n");
            html.append("      <div style=\"display: flex; flex-wrap: wrap; justify-content: center;\">\n");

            html.append("        <div class=\"stat-card\">\n");
            html.append("          <div class=\"stat-label\">Generated Lines</div>\n");
            html.append("          <div class=\"stat-value\">").append(report.totalLinesGenerated).append("</div>\n");
            html.append("        </div>\n");

            html.append("        <div class=\"stat-card\">\n");
            html.append("          <div class=\"stat-label\">Code Acceptance Rate</div>\n");
            html.append("          <div class=\"stat-value\">").append(String.format("%.1f%%", report.avgCodeAcceptanceRate * 100)).append("</div>\n");
            html.append("        </div>\n");

            html.append("        <div class=\"stat-card\">\n");
            html.append("          <div class=\"stat-label\">Time Saved</div>\n");
            html.append("          <div class=\"stat-value\">").append(String.format("%.1f", report.totalTimeSavedHours)).append(" hrs</div>\n");
            html.append("        </div>\n");

            html.append("        <div class=\"stat-card\">\n");
            html.append("          <div class=\"stat-label\">Efficiency Gain</div>\n");
            html.append("          <div class=\"stat-value\">").append(String.format("%.1f%%", report.avgEfficiencyGain)).append("</div>\n");
            html.append("        </div>\n");

            html.append("      </div>\n");
            html.append("    </div>\n");

            // Chart area
            html.append("    <div class=\"chart-container\">\n");

            // Daily statistics charts
            if (options.includeDailyStats && metrics.getState().dailyStats != null && !metrics.getState().dailyStats.isEmpty()) {
                html.append("      <div class=\"chart\">\n");
                html.append("        <h3>Daily Code Generation</h3>\n");
                html.append("        <canvas id=\"dailyStatsChart\"></canvas>\n");
                html.append("      </div>\n");

                html.append("      <div class=\"chart\">\n");
                html.append("        <h3>Daily Time Saving</h3>\n");
                html.append("        <canvas id=\"timeSavingChart\"></canvas>\n");
                html.append("      </div>\n");
            }

            // Code acceptance rate charts
            if (options.includeCodeCompletion && metrics.getState().codeCompletions != null && !metrics.getState().codeCompletions.isEmpty()) {
                html.append("      <div class=\"chart\">\n");
                html.append("        <h3>Code Acceptance Rate Trend</h3>\n");
                html.append("        <canvas id=\"acceptanceRateChart\"></canvas>\n");
                html.append("      </div>\n");

                html.append("      <div class=\"chart\">\n");
                html.append("        <h3>Programming Language Distribution</h3>\n");
                html.append("        <canvas id=\"languageDistributionChart\"></canvas>\n");
                html.append("      </div>\n");
            }

            html.append("    </div>\n");

            // Code completion data table
            if (options.includeCodeCompletion && metrics.getState().codeCompletions != null && !metrics.getState().codeCompletions.isEmpty()) {
                html.append("    <div class=\"data-section\">\n");
                html.append("      <h2>Code Completion Details</h2>\n");
                html.append("      <table>\n");
                html.append("        <thead>\n");
                html.append("          <tr>\n");
                html.append("            <th>Time</th>\n");
                html.append("            <th>Language</th>\n");
                html.append("            <th>Suggested Lines</th>\n");
                html.append("            <th>Accepted Lines</th>\n");
                html.append("            <th>Response Time</th>\n");
                html.append("            <th>Acceptance Rate</th>\n");
                html.append("          </tr>\n");
                html.append("        </thead>\n");
                html.append("        <tbody>\n");

                for (Object completion : metrics.getState().codeCompletions) {
                    String timestamp = getFieldValue(completion, "timestamp");
                    String language = getFieldValue(completion, "language");
                    int suggestedLines = getIntFieldValue(completion, "suggestedLines");
                    int acceptedLines = getIntFieldValue(completion, "acceptedLines");
                    long responseTimeMs = getLongFieldValue(completion, "responseTimeMs");
                    double acceptanceRate = getDoubleFieldValue(completion, "acceptanceRate");

                    html.append("          <tr>\n");
                    html.append("            <td>").append(timestamp).append("</td>\n");
                    html.append("            <td>").append(language).append("</td>\n");
                    html.append("            <td>").append(suggestedLines).append("</td>\n");
                    html.append("            <td>").append(acceptedLines).append("</td>\n");
                    html.append("            <td>").append(responseTimeMs).append(" ms</td>\n");
                    html.append("            <td>").append(String.format("%.1f%%", acceptanceRate * 100)).append("</td>\n");
                    html.append("          </tr>\n");
                }

                html.append("        </tbody>\n");
                html.append("      </table>\n");
                html.append("    </div>\n");
            }

            // Chat code generation data table
            if (options.includeChatCode && metrics.getState().chatCodeGenerations != null && !metrics.getState().chatCodeGenerations.isEmpty()) {
                html.append("    <div class=\"data-section\">\n");
                html.append("      <h2>Chat Code Generation Details</h2>\n");
                html.append("      <table>\n");
                html.append("        <thead>\n");
                html.append("          <tr>\n");
                html.append("            <th>Time</th>\n");
                html.append("            <th>Task Type</th>\n");
                html.append("            <th>Generated Lines</th>\n");
                html.append("            <th>Applied Lines</th>\n");
                html.append("            <th>Session Duration</th>\n");
                html.append("            <th>Application Rate</th>\n");
                html.append("          </tr>\n");
                html.append("        </thead>\n");
                html.append("        <tbody>\n");

                for (Object chatCode : metrics.getState().chatCodeGenerations) {
                    String timestamp = getFieldValue(chatCode, "timestamp");
                    String taskType = getFieldValue(chatCode, "taskType");
                    int generatedLines = getIntFieldValue(chatCode, "generatedLines");
                    int appliedLines = getIntFieldValue(chatCode, "appliedLines");
                    long sessionDurationMs = getLongFieldValue(chatCode, "sessionDurationMs");
                    double applicationRate = getDoubleFieldValue(chatCode, "applicationRate");

                    html.append("          <tr>\n");
                    html.append("            <td>").append(timestamp).append("</td>\n");
                    html.append("            <td>").append(taskType).append("</td>\n");
                    html.append("            <td>").append(generatedLines).append("</td>\n");
                    html.append("            <td>").append(appliedLines).append("</td>\n");
                    html.append("            <td>").append(sessionDurationMs).append(" ms</td>\n");
                    html.append("            <td>").append(String.format("%.1f%%", applicationRate * 100)).append("</td>\n");
                    html.append("          </tr>\n");
                }

                html.append("        </tbody>\n");
                html.append("      </table>\n");
                html.append("    </div>\n");
            }

            // Time saving data table
            if (options.includeTimeSaving && metrics.getState().timeSavings != null && !metrics.getState().timeSavings.isEmpty()) {
                html.append("    <div class=\"data-section\">\n");
                html.append("      <h2>Time Saving Details</h2>\n");
                html.append("      <table>\n");
                html.append("        <thead>\n");
                html.append("          <tr>\n");
                html.append("            <th>Time</th>\n");
                html.append("            <th>Task Type</th>\n");
                html.append("            <th>Traditional Time</th>\n");
                html.append("            <th>AI-Assisted Time</th>\n");
                html.append("            <th>Lines of Code</th>\n");
                html.append("            <th>Time Saved</th>\n");
                html.append("            <th>Efficiency Gain</th>\n");
                html.append("          </tr>\n");
                html.append("        </thead>\n");
                html.append("        <tbody>\n");

                for (Object timeSaving : metrics.getState().timeSavings) {
                    String timestamp = getFieldValue(timeSaving, "timestamp");
                    String taskType = getFieldValue(timeSaving, "taskType");
                    long traditionalTimeMs = getLongFieldValue(timeSaving, "traditionalTimeMs");
                    long aiAssistedTimeMs = getLongFieldValue(timeSaving, "aiAssistedTimeMs");
                    int linesOfCode = getIntFieldValue(timeSaving, "linesOfCode");
                    long timeSavedMs = getLongFieldValue(timeSaving, "timeSavedMs");
                    double efficiencyGain = getDoubleFieldValue(timeSaving, "efficiencyGain");

                    html.append("          <tr>\n");
                    html.append("            <td>").append(timestamp).append("</td>\n");
                    html.append("            <td>").append(taskType).append("</td>\n");
                    html.append("            <td>").append(formatTime(traditionalTimeMs)).append("</td>\n");
                    html.append("            <td>").append(formatTime(aiAssistedTimeMs)).append("</td>\n");
                    html.append("            <td>").append(linesOfCode).append("</td>\n");
                    html.append("            <td>").append(formatTime(timeSavedMs)).append("</td>\n");
                    html.append("            <td>").append(String.format("%.1f%%", efficiencyGain)).append("</td>\n");
                    html.append("          </tr>\n");
                }

                html.append("        </tbody>\n");
                html.append("      </table>\n");
                html.append("    </div>\n");
            }

            // Footer
            html.append("    <div class=\"footer\">\n");
            html.append("      <p>Generated by ProxyAI Productivity Metrics Exporter</p>\n");
            html.append("      <p>© ").append(LocalDate.now().getYear()).append(" ProxyAI</p>\n");
            html.append("    </div>\n");

            html.append("  </div>\n");

            // JavaScript for charts
            html.append("  <script>\n");

            // Prepare data for charts
            if (options.includeDailyStats && metrics.getState().dailyStats != null && !metrics.getState().dailyStats.isEmpty()) {
                // Sort dates
                String[] dates = metrics.getState().dailyStats.keySet().toArray(new String[0]);
                java.util.Arrays.sort(dates);

                StringBuilder dateLabels = new StringBuilder("[");
                StringBuilder linesData = new StringBuilder("[");
                StringBuilder sessionsData = new StringBuilder("[");
                StringBuilder timeSavedData = new StringBuilder("[");

                for (String date : dates) {
                    ProductivityMetrics.DailyProductivityStats stats = metrics.getState().dailyStats.get(date);

                    dateLabels.append("'").append(date).append("',");
                    linesData.append(stats.totalLinesGenerated).append(",");
                    sessionsData.append(stats.chatSessionsCount + stats.codeCompletionsCount).append(",");
                    timeSavedData.append(stats.timeSavedMs / 3600000.0).append(","); // Convert to hours
                }

                // Remove trailing commas
                if (dateLabels.charAt(dateLabels.length() - 1) == ',') {
                    dateLabels.setLength(dateLabels.length() - 1);
                    linesData.setLength(linesData.length() - 1);
                    sessionsData.setLength(sessionsData.length() - 1);
                    timeSavedData.setLength(timeSavedData.length() - 1);
                }

                dateLabels.append("]");
                linesData.append("]");
                sessionsData.append("]");
                timeSavedData.append("]");

                // Daily stats chart
                html.append("    // Daily code generation chart\n");
                html.append("    const dailyStatsCtx = document.getElementById('dailyStatsChart').getContext('2d');\n");
                html.append("    new Chart(dailyStatsCtx, {\n");
                html.append("      type: 'bar',\n");
                html.append("      data: {\n");
                html.append("        labels: ").append(dateLabels).append(",\n");
                html.append("        datasets: [{\n");
                html.append("          label: 'Lines Generated',\n");
                html.append("          data: ").append(linesData).append(",\n");
                html.append("          backgroundColor: 'rgba(54, 162, 235, 0.5)',\n");
                html.append("          borderColor: 'rgba(54, 162, 235, 1)',\n");
                html.append("          borderWidth: 1\n");
                html.append("        }, {\n");
                html.append("          label: 'Sessions',\n");
                html.append("          data: ").append(sessionsData).append(",\n");
                html.append("          backgroundColor: 'rgba(255, 99, 132, 0.5)',\n");
                html.append("          borderColor: 'rgba(255, 99, 132, 1)',\n");
                html.append("          borderWidth: 1\n");
                html.append("        }]\n");
                html.append("      },\n");
                html.append("      options: {\n");
                html.append("        responsive: true,\n");
                html.append("        scales: {\n");
                html.append("          y: {\n");
                html.append("            beginAtZero: true\n");
                html.append("          }\n");
                html.append("        }\n");
                html.append("      }\n");
                html.append("    });\n");

                // Time saving chart
                html.append("    // Time saving chart\n");
                html.append("    const timeSavingCtx = document.getElementById('timeSavingChart').getContext('2d');\n");
                html.append("    new Chart(timeSavingCtx, {\n");
                html.append("      type: 'line',\n");
                html.append("      data: {\n");
                html.append("        labels: ").append(dateLabels).append(",\n");
                html.append("        datasets: [{\n");
                html.append("          label: 'Time Saved (hours)',\n");
                html.append("          data: ").append(timeSavedData).append(",\n");
                html.append("          backgroundColor: 'rgba(75, 192, 192, 0.2)',\n");
                html.append("          borderColor: 'rgba(75, 192, 192, 1)',\n");
                html.append("          borderWidth: 2,\n");
                html.append("          tension: 0.1,\n");
                html.append("          fill: true\n");
                html.append("        }]\n");
                html.append("      },\n");
                html.append("      options: {\n");
                html.append("        responsive: true,\n");
                html.append("        scales: {\n");
                html.append("          y: {\n");
                html.append("            beginAtZero: true\n");
                html.append("          }\n");
                html.append("        }\n");
                html.append("      }\n");
                html.append("    });\n");
            }

            // Code acceptance rate chart
            if (options.includeCodeCompletion && metrics.getState().codeCompletions != null && !metrics.getState().codeCompletions.isEmpty()) {
                // Prepare language distribution data
                html.append("    // Language distribution chart\n");
                html.append("    const languageData = {};\n");
                html.append("    const acceptanceRates = [];\n");
                html.append("    const acceptanceDates = [];\n");

                html.append("    for (let i = 0; i < ").append(metrics.getState().codeCompletions.size()).append("; i++) {\n");
                html.append("      // This is a placeholder. In a real implementation, we would use the actual data\n");
                html.append("      // from the metrics.getState().codeCompletions list\n");
                html.append("      const completion = {};\n");
                html.append("      completion.language = document.querySelectorAll('table tbody tr')[i]?.cells[1]?.textContent || 'Unknown';\n");
                html.append("      completion.acceptanceRate = parseFloat(document.querySelectorAll('table tbody tr')[i]?.cells[5]?.textContent || '0') / 100;\n");
                html.append("      completion.timestamp = document.querySelectorAll('table tbody tr')[i]?.cells[0]?.textContent || '';\n");

                html.append("      if (!languageData[completion.language]) {\n");
                html.append("        languageData[completion.language] = 1;\n");
                html.append("      } else {\n");
                html.append("        languageData[completion.language]++;\n");
                html.append("      }\n");

                html.append("      acceptanceRates.push(completion.acceptanceRate);\n");
                html.append("      acceptanceDates.push(completion.timestamp);\n");
                html.append("    }\n");

                html.append("    // Language distribution chart\n");
                html.append("    const languageLabels = Object.keys(languageData);\n");
                html.append("    const languageCounts = languageLabels.map(lang => languageData[lang]);\n");
                html.append("    const languageColors = languageLabels.map((_, i) => {\n");
                html.append("      const hue = (i * 137) % 360;\n");
                html.append("      return `hsl(${hue}, 70%, 60%)`;\n");
                html.append("    });\n");

                html.append("    const languageDistributionCtx = document.getElementById('languageDistributionChart').getContext('2d');\n");
                html.append("    new Chart(languageDistributionCtx, {\n");
                html.append("      type: 'pie',\n");
                html.append("      data: {\n");
                html.append("        labels: languageLabels,\n");
                html.append("        datasets: [{\n");
                html.append("          data: languageCounts,\n");
                html.append("          backgroundColor: languageColors,\n");
                html.append("          borderWidth: 1\n");
                html.append("        }]\n");
                html.append("      },\n");
                html.append("      options: {\n");
                html.append("        responsive: true,\n");
                html.append("        plugins: {\n");
                html.append("          legend: {\n");
                html.append("            position: 'right'\n");
                html.append("          }\n");
                html.append("        }\n");
                html.append("      }\n");
                html.append("    });\n");

                // Acceptance rate chart
                html.append("    // Acceptance rate chart\n");
                html.append("    const acceptanceRateCtx = document.getElementById('acceptanceRateChart').getContext('2d');\n");
                html.append("    new Chart(acceptanceRateCtx, {\n");
                html.append("      type: 'line',\n");
                html.append("      data: {\n");
                html.append("        labels: acceptanceDates,\n");
                html.append("        datasets: [{\n");
                html.append("          label: 'Code Acceptance Rate',\n");
                html.append("          data: acceptanceRates,\n");
                html.append("          backgroundColor: 'rgba(153, 102, 255, 0.2)',\n");
                html.append("          borderColor: 'rgba(153, 102, 255, 1)',\n");
                html.append("          borderWidth: 2,\n");
                html.append("          tension: 0.1\n");
                html.append("        }]\n");
                html.append("      },\n");
                html.append("      options: {\n");
                html.append("        responsive: true,\n");
                html.append("        scales: {\n");
                html.append("          y: {\n");
                html.append("            beginAtZero: true,\n");
                html.append("            max: 1,\n");
                html.append("            ticks: {\n");
                html.append("              callback: function(value) {\n");
                html.append("                return (value * 100) + '%';\n");
                html.append("              }\n");
                html.append("            }\n");
                html.append("          }\n");
                html.append("        }\n");
                html.append("      }\n");
                html.append("    });\n");
            }

            html.append("  </script>\n");
            html.append("</body>\n");
            html.append("</html>\n");

            writer.write(html.toString());
        }

        return filePath;
    }

    /**
     * Helper method to format time in a human-readable format
     * Converts milliseconds to appropriate time unit (ms, sec, min, hrs)
     */
    private String formatTime(long timeMs) {
        if (timeMs < 1000) {
            return timeMs + " ms";
        } else if (timeMs < 60000) {
            return String.format("%.1f sec", timeMs / 1000.0);
        } else if (timeMs < 3600000) {
            return String.format("%.1f min", timeMs / 60000.0);
        } else {
            return String.format("%.1f hrs", timeMs / 3600000.0);
        }
    }

    /**
     * Get field value using reflection
     */
    private String getFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            LOG.warn("Failed to get field value: " + fieldName, e);
            return "";
        }
    }

    /**
     * Get int field value using reflection
     */
    private int getIntFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(obj);
        } catch (Exception e) {
            LOG.warn("Failed to get int field value: " + fieldName, e);
            return 0;
        }
    }

    /**
     * Get long field value using reflection
     */
    private long getLongFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getLong(obj);
        } catch (Exception e) {
            LOG.warn("Failed to get long field value: " + fieldName, e);
            return 0L;
        }
    }

    /**
     * Get double field value using reflection
     */
    private double getDoubleFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getDouble(obj);
        } catch (Exception e) {
            LOG.warn("Failed to get double field value: " + fieldName, e);
            return 0.0;
        }
    }

    /**
     * Show error notification
     */
    private void showError(Project project, String title, String message) {
//        Notifications.Bus.notify(new Notification(
//            "ee.carlrobert.codegpt.notifications",
//            title,
//            message,
//            NotificationType.ERROR
//        ));
    }

    /**
     * Export options class
     */
    private static class ExportOptions {
        boolean includeCodeCompletion = true;
        boolean includeChatCode = true;
        boolean includeTimeSaving = true;
        boolean includeDebugging = true;
        boolean includeCodeQuality = true;
        boolean includeLearning = true;
        boolean includeDailyStats = true;
    }
}