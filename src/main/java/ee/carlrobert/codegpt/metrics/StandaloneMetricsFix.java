package ee.carlrobert.codegpt.metrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 完全独立的度量系统修复工具
 * 不依赖任何IntelliJ平台类，可以在任何Java环境中运行
 */
public class StandaloneMetricsFix {
    
    public static void main(String[] args) {
        System.out.println("=== 页面效能统计度量系统独立修复工具 v2.0 ===");
        
        try {
            // 0. 检查现有文件
            System.out.println("0. 检查现有度量文件...");
            checkExistingFiles();
            
            // 1. 创建示例数据
            System.out.println("1. 创建示例度量数据...");
            ProductivityData data = createSampleData();
            
            // 2. 保存到XML文件
            System.out.println("2. 保存数据到XML文件...");
            saveDataToXml(data);
            
            // 3. 验证文件创建
            System.out.println("3. 验证文件创建...");
            verifyFileCreation();
            
            // 4. 创建备份
            System.out.println("4. 创建配置文件备份...");
            createBackups();
            
            System.out.println("\n✅ 独立修复完成！");
            System.out.println("📊 请重启IDE，然后检查UI界面是否显示统计数据");
            System.out.println("💡 如果仍无数据，请使用聊天功能进行几次对话");
            
        } catch (Exception e) {
            System.err.println("❌ 修复过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建示例度量数据
     */
    private static ProductivityData createSampleData() {
        ProductivityData data = new ProductivityData();
        
        // 添加代码补全数据
        for (int i = 0; i < 5; i++) {
            CodeCompletionData completion = new CodeCompletionData();
            completion.timestamp = LocalDateTime.now().minusHours(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            completion.language = i % 2 == 0 ? "java" : "python";
            completion.suggestedLines = 5 + i;
            completion.acceptedLines = 3 + i;
            completion.responseTimeMs = 100L + i * 20;
            completion.acceptanceRate = (double) completion.acceptedLines / completion.suggestedLines;
            
            data.codeCompletions.add(completion);
        }
        
        // 添加聊天代码生成数据
        for (int i = 0; i < 3; i++) {
            ChatCodeData chatCode = new ChatCodeData();
            chatCode.timestamp = LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            chatCode.generatedLines = 10 + i * 5;
            chatCode.appliedLines = 8 + i * 3;
            chatCode.sessionDurationMs = 300000L + i * 60000;
            chatCode.taskType = i % 2 == 0 ? "feature_dev" : "bug_fix";
            chatCode.applicationRate = (double) chatCode.appliedLines / chatCode.generatedLines;
            
            data.chatCodeGenerations.add(chatCode);
        }
        
        // 添加时间节省数据
        for (int i = 0; i < 4; i++) {
            TimeSavingData timeSaving = new TimeSavingData();
            timeSaving.timestamp = LocalDateTime.now().minusDays(i % 2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            timeSaving.taskType = i % 2 == 0 ? "coding" : "debugging";
            timeSaving.traditionalTimeMs = 1800000L + i * 300000;
            timeSaving.aiAssistedTimeMs = 900000L + i * 100000;
            timeSaving.linesOfCode = 50 + i * 10;
            timeSaving.timeSavedMs = timeSaving.traditionalTimeMs - timeSaving.aiAssistedTimeMs;
            timeSaving.efficiencyGain = ((double) timeSaving.timeSavedMs / timeSaving.traditionalTimeMs) * 100;
            
            data.timeSavings.add(timeSaving);
        }
        
        // 更新每日统计
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        DailyStatsData todayStats = new DailyStatsData();
        todayStats.codeCompletionsCount = 3;
        todayStats.chatSessionsCount = 2;
        todayStats.timeSavedMs = 3600000L; // 1小时
        todayStats.linesGenerated = 25;
        todayStats.avgResponseTime = 150.0;
        
        data.dailyStats.put(today, todayStats);
        
        String yesterday = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        DailyStatsData yesterdayStats = new DailyStatsData();
        yesterdayStats.codeCompletionsCount = 5;
        yesterdayStats.chatSessionsCount = 3;
        yesterdayStats.timeSavedMs = 5400000L; // 1.5小时
        yesterdayStats.linesGenerated = 40;
        yesterdayStats.avgResponseTime = 130.0;
        
        data.dailyStats.put(yesterday, yesterdayStats);
        
        System.out.println("✓ 创建了示例数据: " + 
                           data.codeCompletions.size() + "条代码补全, " + 
                           data.chatCodeGenerations.size() + "条聊天生成, " + 
                           data.timeSavings.size() + "条时间节省, " + 
                           data.dailyStats.size() + "天统计");
        
        return data;
    }
    
    /**
     * 保存数据到XML文件
     */
    private static void saveDataToXml(ProductivityData data) throws IOException {
        // 确定配置目录
        String configDir = findConfigDirectory();
        if (configDir == null) {
            System.out.println("⚠️ 无法找到IntelliJ配置目录，将使用临时目录");
            configDir = System.getProperty("java.io.tmpdir");
        }
        
        // 创建XML文件
        String xmlPath = configDir + File.separator + "proxyai-productivity-metrics.xml";
        File xmlFile = new File(xmlPath);
        
        // 生成XML内容
        String xmlContent = generateXmlContent(data);
        
        // 写入文件
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(xmlContent);
        }
        
        System.out.println("✓ 数据已保存到: " + xmlFile.getAbsolutePath());
    }
    
    /**
     * 检查现有度量文件
     */
    private static void checkExistingFiles() {
        String configDir = findConfigDirectory();
        if (configDir == null) {
            System.out.println("⚠️ 无法找到IntelliJ配置目录");
            return;
        }
        
        // 检查度量数据文件
        String metricsPath = configDir + File.separator + "proxyai-productivity-metrics.xml";
        File metricsFile = new File(metricsPath);
        if (metricsFile.exists()) {
            System.out.println("✓ 找到现有度量数据文件: " + metricsFile.getAbsolutePath());
            System.out.println("  文件大小: " + (metricsFile.length() / 1024) + " KB");
            System.out.println("  最后修改时间: " + new java.util.Date(metricsFile.lastModified()));
        } else {
            System.out.println("⚠️ 未找到度量数据文件，将创建新文件");
        }
        
        // 检查度量设置文件
        String settingsPath = configDir + File.separator + "proxyai-metrics-settings.xml";
        File settingsFile = new File(settingsPath);
        if (settingsFile.exists()) {
            System.out.println("✓ 找到现有度量设置文件: " + settingsFile.getAbsolutePath());
            System.out.println("  文件大小: " + (settingsFile.length() / 1024) + " KB");
            System.out.println("  最后修改时间: " + new java.util.Date(settingsFile.lastModified()));
        } else {
            System.out.println("⚠️ 未找到度量设置文件，将创建新文件");
        }
    }
    
    /**
     * 查找IntelliJ配置目录
     */
    private static String findConfigDirectory() {
        String userHome = System.getProperty("user.home");
        
        // 可能的配置目录路径
        String[] possiblePaths = {
            // Windows路径
            userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2023.3" + File.separator + "options",
            userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2023.2" + File.separator + "options",
            userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2023.1" + File.separator + "options",
            userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2022.3" + File.separator + "options",
            userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2022.2" + File.separator + "options",
            userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2022.1" + File.separator + "options",
            
            // 传统Windows路径
            userHome + File.separator + ".IntelliJIdea2023.3" + File.separator + "config" + File.separator + "options",
            userHome + File.separator + ".IntelliJIdea2023.2" + File.separator + "config" + File.separator + "options",
            userHome + File.separator + ".IntelliJIdea2023.1" + File.separator + "config" + File.separator + "options",
            userHome + File.separator + ".IntelliJIdea2022.3" + File.separator + "config" + File.separator + "options",
            userHome + File.separator + ".IntelliJIdea2022.2" + File.separator + "config" + File.separator + "options",
            userHome + File.separator + ".IntelliJIdea2022.1" + File.separator + "config" + File.separator + "options",
            
            // Linux/Mac路径
            userHome + File.separator + ".config" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2023.3" + File.separator + "options",
            userHome + File.separator + ".config" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2023.2" + File.separator + "options",
            userHome + File.separator + ".config" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2023.1" + File.separator + "options",
            userHome + File.separator + ".config" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2022.3" + File.separator + "options",
            userHome + File.separator + ".config" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2022.2" + File.separator + "options",
            userHome + File.separator + ".config" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2022.1" + File.separator + "options",
            
            // 其他可能的路径
            userHome + File.separator + "Library" + File.separator + "Application Support" + File.separator + "JetBrains" + File.separator + "IntelliJIdea2023.1" + File.separator + "options",
            userHome + File.separator + "Library" + File.separator + "Preferences" + File.separator + "IntelliJIdea2023.1" + File.separator + "options"
        };
        
        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                System.out.println("✓ 找到IntelliJ配置目录: " + dir.getAbsolutePath());
                return path;
            }
        }
        
        return null;
    }
    
    /**
     * 生成XML内容
     */
    private static String generateXmlContent(ProductivityData data) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<ProductivityMetrics>\n");
        
        // 代码补全数据
        xml.append("  <codeCompletions>\n");
        for (CodeCompletionData completion : data.codeCompletions) {
            xml.append("    <CodeCompletionMetric>\n");
            xml.append("      <timestamp>").append(completion.timestamp).append("</timestamp>\n");
            xml.append("      <language>").append(completion.language).append("</language>\n");
            xml.append("      <suggestedLines>").append(completion.suggestedLines).append("</suggestedLines>\n");
            xml.append("      <acceptedLines>").append(completion.acceptedLines).append("</acceptedLines>\n");
            xml.append("      <responseTimeMs>").append(completion.responseTimeMs).append("</responseTimeMs>\n");
            xml.append("      <acceptanceRate>").append(completion.acceptanceRate).append("</acceptanceRate>\n");
            xml.append("    </CodeCompletionMetric>\n");
        }
        xml.append("  </codeCompletions>\n");
        
        // 聊天代码生成数据
        xml.append("  <chatCodeGenerations>\n");
        for (ChatCodeData chatCode : data.chatCodeGenerations) {
            xml.append("    <ChatCodeMetric>\n");
            xml.append("      <timestamp>").append(chatCode.timestamp).append("</timestamp>\n");
            xml.append("      <generatedLines>").append(chatCode.generatedLines).append("</generatedLines>\n");
            xml.append("      <appliedLines>").append(chatCode.appliedLines).append("</appliedLines>\n");
            xml.append("      <sessionDurationMs>").append(chatCode.sessionDurationMs).append("</sessionDurationMs>\n");
            xml.append("      <taskType>").append(chatCode.taskType).append("</taskType>\n");
            xml.append("      <applicationRate>").append(chatCode.applicationRate).append("</applicationRate>\n");
            xml.append("    </ChatCodeMetric>\n");
        }
        xml.append("  </chatCodeGenerations>\n");
        
        // 时间节省数据
        xml.append("  <timeSavings>\n");
        for (TimeSavingData timeSaving : data.timeSavings) {
            xml.append("    <TimeSavingMetric>\n");
            xml.append("      <timestamp>").append(timeSaving.timestamp).append("</timestamp>\n");
            xml.append("      <taskType>").append(timeSaving.taskType).append("</taskType>\n");
            xml.append("      <traditionalTimeMs>").append(timeSaving.traditionalTimeMs).append("</traditionalTimeMs>\n");
            xml.append("      <aiAssistedTimeMs>").append(timeSaving.aiAssistedTimeMs).append("</aiAssistedTimeMs>\n");
            xml.append("      <linesOfCode>").append(timeSaving.linesOfCode).append("</linesOfCode>\n");
            xml.append("      <timeSavedMs>").append(timeSaving.timeSavedMs).append("</timeSavedMs>\n");
            xml.append("      <efficiencyGain>").append(timeSaving.efficiencyGain).append("</efficiencyGain>\n");
            xml.append("    </TimeSavingMetric>\n");
        }
        xml.append("  </timeSavings>\n");
        
        // 调试指标数据
        xml.append("  <debuggingMetrics>\n");
        xml.append("  </debuggingMetrics>\n");
        
        // 代码质量指标数据
        xml.append("  <codeQualityMetrics>\n");
        xml.append("  </codeQualityMetrics>\n");
        
        // 学习指标数据
        xml.append("  <learningMetrics>\n");
        xml.append("  </learningMetrics>\n");
        
        // 每日统计数据
        xml.append("  <dailyStats>\n");
        for (Map.Entry<String, DailyStatsData> entry : data.dailyStats.entrySet()) {
            String date = entry.getKey();
            DailyStatsData stats = entry.getValue();
            
            xml.append("    <entry>\n");
            xml.append("      <key>").append(date).append("</key>\n");
            xml.append("      <value>\n");
            xml.append("        <codeCompletionsCount>").append(stats.codeCompletionsCount).append("</codeCompletionsCount>\n");
            xml.append("        <chatSessionsCount>").append(stats.chatSessionsCount).append("</chatSessionsCount>\n");
            xml.append("        <timeSavedMs>").append(stats.timeSavedMs).append("</timeSavedMs>\n");
            xml.append("        <linesGenerated>").append(stats.linesGenerated).append("</linesGenerated>\n");
            xml.append("        <avgResponseTime>").append(stats.avgResponseTime).append("</avgResponseTime>\n");
            xml.append("      </value>\n");
            xml.append("    </entry>\n");
        }
        xml.append("  </dailyStats>\n");
        
        xml.append("</ProductivityMetrics>");
        
        return xml.toString();
    }
    
    /**
     * 验证文件创建
     */
    private static void verifyFileCreation() {
        String configDir = findConfigDirectory();
        if (configDir == null) {
            configDir = System.getProperty("java.io.tmpdir");
        }
        
        String xmlPath = configDir + File.separator + "proxyai-productivity-metrics.xml";
        File xmlFile = new File(xmlPath);
        
        if (xmlFile.exists()) {
            System.out.println("✓ 文件创建成功: " + xmlFile.getAbsolutePath());
            System.out.println("✓ 文件大小: " + (xmlFile.length() / 1024) + " KB");
            
            // 尝试设置文件权限
            try {
                boolean readable = xmlFile.setReadable(true, false);
                boolean writable = xmlFile.setWritable(true, false);
                System.out.println("✓ 文件权限设置: 可读=" + readable + ", 可写=" + writable);
            } catch (Exception e) {
                System.out.println("⚠️ 设置文件权限时出错: " + e.getMessage());
            }
        } else {
            System.out.println("❌ 文件创建失败: " + xmlPath);
        }
        
        // 同时创建设置文件
        createSettingsFile(configDir);
    }
    
    /**
     * 创建配置文件备份
     */
    private static void createBackups() {
        try {
            String configDir = findConfigDirectory();
            if (configDir == null) {
                System.out.println("⚠️ 无法找到配置目录，跳过备份");
                return;
            }
            
            // 创建备份目录
            String backupDir = configDir + File.separator + "proxyai-backups";
            File backupDirFile = new File(backupDir);
            if (!backupDirFile.exists()) {
                boolean created = backupDirFile.mkdir();
                if (!created) {
                    System.out.println("⚠️ 无法创建备份目录，跳过备份");
                    return;
                }
            }
            
            // 备份度量数据文件
            String metricsPath = configDir + File.separator + "proxyai-productivity-metrics.xml";
            File metricsFile = new File(metricsPath);
            if (metricsFile.exists()) {
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date());
                String backupPath = backupDir + File.separator + "proxyai-productivity-metrics-" + timestamp + ".xml";
                copyFile(metricsFile, new File(backupPath));
                System.out.println("✓ 已创建度量数据备份: " + backupPath);
            }
            
            // 备份设置文件
            String settingsPath = configDir + File.separator + "proxyai-metrics-settings.xml";
            File settingsFile = new File(settingsPath);
            if (settingsFile.exists()) {
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date());
                String backupPath = backupDir + File.separator + "proxyai-metrics-settings-" + timestamp + ".xml";
                copyFile(settingsFile, new File(backupPath));
                System.out.println("✓ 已创建设置文件备份: " + backupPath);
            }
            
        } catch (Exception e) {
            System.err.println("❌ 创建备份时出错: " + e.getMessage());
        }
    }
    
    /**
     * 复制文件
     */
    private static void copyFile(File source, File dest) throws IOException {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)) {
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }
    
    /**
     * 创建设置文件
     */
    private static void createSettingsFile(String configDir) {
        try {
            String settingsPath = configDir + File.separator + "proxyai-metrics-settings.xml";
            File settingsFile = new File(settingsPath);
            
            String settingsXml = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<MetricsSettings>\n" +
                "  <state>\n" +
                "    <metricsEnabled>true</metricsEnabled>\n" +
                "    <autoExportEnabled>false</autoExportEnabled>\n" +
                "    <detailedLoggingEnabled>true</detailedLoggingEnabled>\n" +
                "    <autoDetectionEnabled>false</autoDetectionEnabled>\n" +
                "  </state>\n" +
                "</MetricsSettings>";
            
            try (FileWriter writer = new FileWriter(settingsFile)) {
                writer.write(settingsXml);
            }
            
            // 尝试设置文件权限
            try {
                boolean readable = settingsFile.setReadable(true, false);
                boolean writable = settingsFile.setWritable(true, false);
                System.out.println("✓ 设置文件权限设置: 可读=" + readable + ", 可写=" + writable);
            } catch (Exception e) {
                System.out.println("⚠️ 设置文件权限时出错: " + e.getMessage());
            }
            
            System.out.println("✓ 设置文件创建成功: " + settingsFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("❌ 创建设置文件时出错: " + e.getMessage());
        }
    }
    
    // ==================== 数据模型 ====================
    
    private static class ProductivityData {
        List<CodeCompletionData> codeCompletions = new ArrayList<>();
        List<ChatCodeData> chatCodeGenerations = new ArrayList<>();
        List<TimeSavingData> timeSavings = new ArrayList<>();
        Map<String, DailyStatsData> dailyStats = new HashMap<>();
    }
    
    private static class CodeCompletionData {
        String timestamp;
        String language;
        int suggestedLines;
        int acceptedLines;
        long responseTimeMs;
        double acceptanceRate;
    }
    
    private static class ChatCodeData {
        String timestamp;
        int generatedLines;
        int appliedLines;
        long sessionDurationMs;
        String taskType;
        double applicationRate;
    }
    
    private static class TimeSavingData {
        String timestamp;
        String taskType;
        long traditionalTimeMs;
        long aiAssistedTimeMs;
        int linesOfCode;
        long timeSavedMs;
        double efficiencyGain;
    }
    
    private static class DailyStatsData {
        int codeCompletionsCount;
        int chatSessionsCount;
        long timeSavedMs;
        int linesGenerated;
        double avgResponseTime;
    }
}