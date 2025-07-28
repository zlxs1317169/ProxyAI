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
               settingsComponent.isDetailedLoggingEnabled() != settings.isDetailedLoggingEnabled();
    }
    
    @Override
    public void apply() throws ConfigurationException {
        MetricsSettings settings = MetricsSettings.getInstance();
        settings.setMetricsEnabled(settingsComponent.isMetricsEnabled());
        settings.setAutoExportEnabled(settingsComponent.isAutoExportEnabled());
        settings.setExportInterval(settingsComponent.getExportInterval());
        settings.setDetailedLoggingEnabled(settingsComponent.isDetailedLoggingEnabled());
    }
    
    @Override
    public void reset() {
        MetricsSettings settings = MetricsSettings.getInstance();
        settingsComponent.setMetricsEnabled(settings.isMetricsEnabled());
        settingsComponent.setAutoExportEnabled(settings.isAutoExportEnabled());
        settingsComponent.setExportInterval(settings.getExportInterval());
        settingsComponent.setDetailedLoggingEnabled(settings.isDetailedLoggingEnabled());
    }
    
    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}