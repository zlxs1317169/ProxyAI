package ee.carlrobert.codegpt.settings.metrics;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import ee.carlrobert.codegpt.metrics.ProductivityMetrics;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 提效度量设置配置页面
 */
public class MetricsSettingsConfigurable implements Configurable {
    
    private MetricsSettingsComponent settingsComponent;
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "提效度量";
    }
    
    @Override
    public @Nullable JComponent createComponent() {
        settingsComponent = new MetricsSettingsComponent();
        return settingsComponent.getPanel();
    }
    
    @Override
    public boolean isModified() {
        MetricsSettings settings = MetricsSettings.getInstance();
        return settingsComponent.isMetricsEnabled() != settings.isMetricsEnabled() ||
               settingsComponent.isAutoExportEnabled() != settings.isAutoExportEnabled() ||
               settingsComponent.getExportInterval() != settings.getExportInterval() ||
               settingsComponent.isDetailedLoggingEnabled() != settings.isDetailedLoggingEnabled() ||
               settingsComponent.isAutoDetectionEnabled() != settings.isAutoDetectionEnabled() ||
               settingsComponent.isOnlyTrackAIUsage() != settings.isOnlyTrackAIUsage();
    }
    
    @Override
    public void apply() throws ConfigurationException {
        MetricsSettings settings = MetricsSettings.getInstance();
        settings.setMetricsEnabled(settingsComponent.isMetricsEnabled());
        settings.setAutoExportEnabled(settingsComponent.isAutoExportEnabled());
        settings.setExportInterval(settingsComponent.getExportInterval());
        settings.setDetailedLoggingEnabled(settingsComponent.isDetailedLoggingEnabled());
        settings.setAutoDetectionEnabled(settingsComponent.isAutoDetectionEnabled());
        settings.setOnlyTrackAIUsage(settingsComponent.isOnlyTrackAIUsage());
        
        // 如果用户选择了"仅跟踪真实AI使用"，显示提示信息
        if (settingsComponent.isOnlyTrackAIUsage()) {
            JOptionPane.showMessageDialog(settingsComponent.getPanel(),
                "已启用\"仅跟踪真实AI使用\"模式。\n" +
                "现在只有在真正使用ProxyAI功能时才会记录统计数据，\n" +
                "这将提供更准确的效能度量结果。",
                "设置已保存",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    @Override
    public void reset() {
        MetricsSettings settings = MetricsSettings.getInstance();
        settingsComponent.setMetricsEnabled(settings.isMetricsEnabled());
        settingsComponent.setAutoExportEnabled(settings.isAutoExportEnabled());
        settingsComponent.setExportInterval(settings.getExportInterval());
        settingsComponent.setDetailedLoggingEnabled(settings.isDetailedLoggingEnabled());
        settingsComponent.setAutoDetectionEnabled(settings.isAutoDetectionEnabled());
        settingsComponent.setOnlyTrackAIUsage(settings.isOnlyTrackAIUsage());
    }
    
    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}