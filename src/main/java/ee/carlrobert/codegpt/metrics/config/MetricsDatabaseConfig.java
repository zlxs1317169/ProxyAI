package ee.carlrobert.codegpt.metrics.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 指标系统数据库配置
 * 管理MySQL数据库连接参数
 */
@State(
    name = "ProxyAI.MetricsDatabaseConfig",
    storages = @Storage("proxyai-metrics-db-config.xml")
)
public class MetricsDatabaseConfig implements PersistentStateComponent<MetricsDatabaseConfig.State> {
    
    private static final Logger LOG = Logger.getInstance(MetricsDatabaseConfig.class);
    
    public static class State {
        public String dbUrl = "jdbc:mysql://localhost:3306/proxyai_metrics?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useUnicode=true";
        public String dbUser = "root";
        public String dbPassword = "root";
        public String dbDriver = "com.mysql.cj.jdbc.Driver";
        public int connectionTimeout = 30000;
        public int maxPoolSize = 10;
        public boolean autoCreateTables = true;
    }
    
    private State myState = new State();
    
    public static MetricsDatabaseConfig getInstance() {
        return com.intellij.openapi.application.ApplicationManager.getApplication()
            .getService(MetricsDatabaseConfig.class);
    }
    
    @Nullable
    @Override
    public State getState() {
        return myState;
    }
    
    @Override
    public void loadState(@NotNull State state) {
        myState = state;
        LOG.info("指标数据库配置已加载");
    }
    
    // Getters
    public String getDbUrl() { return myState.dbUrl; }
    public String getDbUser() { return myState.dbUser; }
    public String getDbPassword() { return myState.dbPassword; }
    public String getDbDriver() { return myState.dbDriver; }
    public int getConnectionTimeout() { return myState.connectionTimeout; }
    public int getMaxPoolSize() { return myState.maxPoolSize; }
    public boolean isAutoCreateTables() { return myState.autoCreateTables; }
    
    // Setters
    public void setDbUrl(String dbUrl) { 
        myState.dbUrl = dbUrl; 
        LOG.info("数据库URL已更新: " + dbUrl);
    }
    
    public void setDbUser(String dbUser) { 
        myState.dbUser = dbUser; 
        LOG.info("数据库用户已更新: " + dbUser);
    }
    
    public void setDbPassword(String dbPassword) { 
        myState.dbPassword = dbPassword; 
        LOG.info("数据库密码已更新");
    }
    
    public void setDbDriver(String dbDriver) { 
        myState.dbDriver = dbDriver; 
        LOG.info("数据库驱动已更新: " + dbDriver);
    }
    
    public void setConnectionTimeout(int connectionTimeout) { 
        myState.connectionTimeout = connectionTimeout; 
        LOG.info("连接超时已更新: " + connectionTimeout + "ms");
    }
    
    public void setMaxPoolSize(int maxPoolSize) { 
        myState.maxPoolSize = maxPoolSize; 
        LOG.info("最大连接池大小已更新: " + maxPoolSize);
    }
    
    public void setAutoCreateTables(boolean autoCreateTables) { 
        myState.autoCreateTables = autoCreateTables; 
        LOG.info("自动创建表已" + (autoCreateTables ? "启用" : "禁用"));
    }
    
    /**
     * 获取完整的数据库连接URL
     */
    public String getFullDbUrl() {
        return myState.dbUrl + "&connectTimeout=" + myState.connectionTimeout;
    }
    
    /**
     * 验证配置是否有效
     */
    public boolean isValid() {
        return myState.dbUrl != null && !myState.dbUrl.isEmpty() &&
               myState.dbUser != null && !myState.dbUser.isEmpty() &&
               myState.dbPassword != null && !myState.dbPassword.isEmpty() &&
               myState.dbDriver != null && !myState.dbDriver.isEmpty();
    }
    
    /**
     * 重置为默认配置
     */
    public void resetToDefaults() {
        myState = new State();
        LOG.info("数据库配置已重置为默认值");
    }
}
