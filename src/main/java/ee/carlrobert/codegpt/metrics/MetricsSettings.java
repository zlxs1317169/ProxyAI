package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 效能度量系统配置管理
 * 管理各种配置选项，支持持久化存储
 */
@State(
    name = "ProxyAI.MetricsSettings",
    storages = @Storage("proxyai-metrics-settings.xml")
)
public class MetricsSettings implements PersistentStateComponent<MetricsSettings.State> {
    
    private static final Logger LOG = Logger.getInstance(MetricsSettings.class);
    
    public static class State {
        public boolean metricsEnabled = true;
        public boolean autoAnalysisEnabled = true;
        public int autoAnalysisIntervalMinutes = 60;
        public boolean collectSystemMetrics = true;
        public boolean collectUserBehaviorMetrics = true;
        public boolean collectCodeQualityMetrics = true;
        public int dataRetentionDays = 90;
        public boolean exportMetricsOnShutdown = false;
        public String exportFormat = "json";
        public boolean enableRealTimeMonitoring = true;
        public boolean enablePerformanceAlerts = false;
        public double performanceAlertThreshold = 70.0;
        public boolean enablePrivacyMode = false;
        public boolean anonymizeUserData = false;
    }
    
    private State myState = new State();
    
    public static MetricsSettings getInstance() {
        return ServiceManager.getService(MetricsSettings.class);
    }
    
    @Nullable
    @Override
    public State getState() {
        return myState;
    }
    
    @Override
    public void loadState(@NotNull State state) {
        myState = state;
        LOG.info("效能度量配置已加载");
    }
    
    // 基础配置
    public boolean isMetricsEnabled() {
        return myState.metricsEnabled;
    }
    
    public void setMetricsEnabled(boolean enabled) {
        myState.metricsEnabled = enabled;
        LOG.info("效能度量收集已" + (enabled ? "启用" : "禁用"));
    }
    
    public boolean isAutoAnalysisEnabled() {
        return myState.autoAnalysisEnabled;
    }
    
    public void setAutoAnalysisEnabled(boolean enabled) {
        myState.autoAnalysisEnabled = enabled;
        LOG.info("自动效能分析已" + (enabled ? "启用" : "禁用"));
    }
    
    public int getAutoAnalysisIntervalMinutes() {
        return myState.autoAnalysisIntervalMinutes;
    }
    
    public void setAutoAnalysisIntervalMinutes(int minutes) {
        if (minutes < 15) minutes = 15; // 最小15分钟
        if (minutes > 1440) minutes = 1440; // 最大24小时
        myState.autoAnalysisIntervalMinutes = minutes;
        LOG.info("自动效能分析间隔已设置为" + minutes + "分钟");
    }
    
    // 数据收集配置
    public boolean isCollectSystemMetrics() {
        return myState.collectSystemMetrics;
    }
    
    public void setCollectSystemMetrics(boolean enabled) {
        myState.collectSystemMetrics = enabled;
        LOG.info("系统性能指标收集已" + (enabled ? "启用" : "禁用"));
    }
    
    public boolean isCollectUserBehaviorMetrics() {
        return myState.collectUserBehaviorMetrics;
    }
    
    public void setCollectUserBehaviorMetrics(boolean enabled) {
        myState.collectUserBehaviorMetrics = enabled;
        LOG.info("用户行为指标收集已" + (enabled ? "启用" : "禁用"));
    }
    
    public boolean isCollectCodeQualityMetrics() {
        return myState.collectCodeQualityMetrics;
    }
    
    public void setCollectCodeQualityMetrics(boolean enabled) {
        myState.collectCodeQualityMetrics = enabled;
        LOG.info("代码质量指标收集已" + (enabled ? "启用" : "禁用"));
    }
    
    // 数据管理配置
    public int getDataRetentionDays() {
        return myState.dataRetentionDays;
    }
    
    public void setDataRetentionDays(int days) {
        if (days < 7) days = 7; // 最小7天
        if (days > 365) days = 365; // 最大1年
        myState.dataRetentionDays = days;
        LOG.info("数据保留期限已设置为" + days + "天");
    }
    
    public boolean isExportMetricsOnShutdown() {
        return myState.exportMetricsOnShutdown;
    }
    
    public void setExportMetricsOnShutdown(boolean enabled) {
        myState.exportMetricsOnShutdown = enabled;
        LOG.info("关闭时导出指标已" + (enabled ? "启用" : "禁用"));
    }
    
    public String getExportFormat() {
        return myState.exportFormat;
    }
    
    public void setExportFormat(String format) {
        if (format == null || format.trim().isEmpty()) {
            format = "json";
        }
        if (!format.matches("^(json|csv|summary)$")) {
            format = "json";
        }
        myState.exportFormat = format;
        LOG.info("导出格式已设置为" + format);
    }
    
    // 监控配置
    public boolean isEnableRealTimeMonitoring() {
        return myState.enableRealTimeMonitoring;
    }
    
    public void setEnableRealTimeMonitoring(boolean enabled) {
        myState.enableRealTimeMonitoring = enabled;
        LOG.info("实时监控已" + (enabled ? "启用" : "禁用"));
    }
    
    public boolean isEnablePerformanceAlerts() {
        return myState.enablePerformanceAlerts;
    }
    
    public void setEnablePerformanceAlerts(boolean enabled) {
        myState.enablePerformanceAlerts = enabled;
        LOG.info("性能告警已" + (enabled ? "启用" : "禁用"));
    }
    
    public double getPerformanceAlertThreshold() {
        return myState.performanceAlertThreshold;
    }
    
    public void setPerformanceAlertThreshold(double threshold) {
        if (threshold < 0) threshold = 0;
        if (threshold > 100) threshold = 100;
        myState.performanceAlertThreshold = threshold;
        LOG.info("性能告警阈值已设置为" + threshold);
    }
    
    // 隐私配置
    public boolean isEnablePrivacyMode() {
        return myState.enablePrivacyMode;
    }
    
    public void setEnablePrivacyMode(boolean enabled) {
        myState.enablePrivacyMode = enabled;
        LOG.info("隐私模式已" + (enabled ? "启用" : "禁用"));
    }
    
    public boolean isAnonymizeUserData() {
        return myState.anonymizeUserData;
    }
    
    public void setAnonymizeUserData(boolean enabled) {
        myState.anonymizeUserData = enabled;
        LOG.info("用户数据匿名化已" + (enabled ? "启用" : "禁用"));
    }
    
    /**
     * 重置所有配置为默认值
     */
    public void resetToDefaults() {
        myState = new State();
        LOG.info("效能度量配置已重置为默认值");
    }
    
    /**
     * 获取配置摘要
     */
    public String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("效能度量系统配置摘要\n");
        summary.append("====================\n");
        summary.append("指标收集: ").append(isMetricsEnabled() ? "启用" : "禁用").append("\n");
        summary.append("自动分析: ").append(isAutoAnalysisEnabled() ? "启用" : "禁用").append("\n");
        summary.append("分析间隔: ").append(getAutoAnalysisIntervalMinutes()).append(" 分钟\n");
        summary.append("系统指标: ").append(isCollectSystemMetrics() ? "启用" : "禁用").append("\n");
        summary.append("用户行为: ").append(isCollectUserBehaviorMetrics() ? "启用" : "禁用").append("\n");
        summary.append("代码质量: ").append(isCollectCodeQualityMetrics() ? "启用" : "禁用").append("\n");
        summary.append("数据保留: ").append(getDataRetentionDays()).append(" 天\n");
        summary.append("导出格式: ").append(getExportFormat()).append("\n");
        summary.append("实时监控: ").append(isEnableRealTimeMonitoring() ? "启用" : "禁用").append("\n");
        summary.append("性能告警: ").append(isEnablePerformanceAlerts() ? "启用" : "禁用").append("\n");
        summary.append("告警阈值: ").append(getPerformanceAlertThreshold()).append("\n");
        summary.append("隐私模式: ").append(isEnablePrivacyMode() ? "启用" : "禁用").append("\n");
        summary.append("数据匿名: ").append(isAnonymizeUserData() ? "启用" : "禁用").append("\n");
        
        return summary.toString();
    }
}
