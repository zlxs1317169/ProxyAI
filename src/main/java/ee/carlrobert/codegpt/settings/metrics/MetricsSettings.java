package ee.carlrobert.codegpt.settings.metrics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 提效度量设置服务
 */
@Service
@State(name = "MetricsSettings", storages = @Storage("proxyai-metrics-settings.xml"))
public final class MetricsSettings implements PersistentStateComponent<MetricsSettings.State> {
    
    private State state = new State();
    
    public static MetricsSettings getInstance() {
        return ApplicationManager.getApplication().getService(MetricsSettings.class);
    }
    
    // 设置方法
    public boolean isMetricsEnabled() {
        return state.metricsEnabled;
    }
    
    public void setMetricsEnabled(boolean enabled) {
        state.metricsEnabled = enabled;
    }
    
    public boolean isAutoExportEnabled() {
        return state.autoExportEnabled;
    }
    
    public void setAutoExportEnabled(boolean enabled) {
        state.autoExportEnabled = enabled;
    }
    
    public int getExportInterval() {
        return state.exportInterval;
    }
    
    public void setExportInterval(int interval) {
        state.exportInterval = interval;
    }
    
    public boolean isDetailedLoggingEnabled() {
        return state.detailedLoggingEnabled;
    }
    
    public void setDetailedLoggingEnabled(boolean enabled) {
        state.detailedLoggingEnabled = enabled;
    }
    
    public String getExportPath() {
        return state.exportPath;
    }
    
    public void setExportPath(String path) {
        state.exportPath = path;
    }
    
    public boolean isNotificationEnabled() {
        return state.notificationEnabled;
    }
    
    public void setNotificationEnabled(boolean enabled) {
        state.notificationEnabled = enabled;
    }
    
    // 状态持久化
    @Override
    public @Nullable State getState() {
        return state;
    }
    
    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
    
    /**
     * 设置状态类
     */
    public static class State {
        public boolean metricsEnabled = true;
        public boolean autoExportEnabled = false;
        public int exportInterval = 24; // 小时
        public boolean detailedLoggingEnabled = false;
        public String exportPath = "";
        public boolean notificationEnabled = true;
    }
}