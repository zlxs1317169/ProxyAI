package ee.carlrobert.codegpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import ee.carlrobert.codegpt.metrics.config.MetricsDatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 测试数据库连接的Action
 */
public class TestDatabaseConnectionAction extends AnAction {
    
    public TestDatabaseConnectionAction() {
        super("测试数据库连接");
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("无法获取项目信息", "错误");
            return;
        }
        
        try {
            testDatabaseConnection();
        } catch (Exception ex) {
            Messages.showErrorDialog("测试数据库连接时发生错误: " + ex.getMessage(), "错误");
        }
    }
    
    /**
     * 测试数据库连接
     */
    private void testDatabaseConnection() {
        StringBuilder result = new StringBuilder();
        result.append("开始测试数据库连接...\n\n");
        
        try {
            // 获取数据库配置
            MetricsDatabaseConfig dbConfig = MetricsDatabaseConfig.getInstance();
            result.append("1. 检查数据库配置...\n");
            
            if (dbConfig.isValid()) {
                result.append("   ✓ 配置有效\n");
                result.append("   URL: ").append(dbConfig.getDbUrl()).append("\n");
                result.append("   用户: ").append(dbConfig.getDbUser()).append("\n");
                result.append("   驱动: ").append(dbConfig.getDbDriver()).append("\n");
            } else {
                result.append("   ✗ 配置无效\n");
                Messages.showErrorDialog("数据库配置无效，请检查配置", "配置错误");
                return;
            }
            
            // 测试驱动加载
            result.append("\n2. 测试驱动加载...\n");
            try {
                Class.forName(dbConfig.getDbDriver());
                result.append("   ✓ MySQL驱动加载成功\n");
            } catch (ClassNotFoundException e) {
                result.append("   ✗ MySQL驱动加载失败: ").append(e.getMessage()).append("\n");
                Messages.showErrorDialog("MySQL驱动加载失败，请检查依赖配置", "驱动错误");
                return;
            }
            
            // 测试数据库连接
            result.append("\n3. 测试数据库连接...\n");
            try (Connection conn = DriverManager.getConnection(
                    dbConfig.getDbUrl(), 
                    dbConfig.getDbUser(), 
                    dbConfig.getDbPassword())) {
                
                result.append("   ✓ 数据库连接成功\n");
                result.append("   数据库产品: ").append(conn.getMetaData().getDatabaseProductName()).append("\n");
                result.append("   数据库版本: ").append(conn.getMetaData().getDatabaseProductVersion()).append("\n");
                result.append("   连接URL: ").append(conn.getMetaData().getURL()).append("\n");
                
                // 测试表是否存在
                result.append("\n4. 检查表结构...\n");
                try (var stmt = conn.createStatement();
                     var rs = stmt.executeQuery("SHOW TABLES LIKE 'productivity_metrics'")) {
                    
                    if (rs.next()) {
                        result.append("   ✓ productivity_metrics表存在\n");
                    } else {
                        result.append("   ⚠️ productivity_metrics表不存在，将自动创建\n");
                    }
                }
                
                result.append("\n✓ 所有测试通过！数据库连接正常。\n");
                
            } catch (Exception e) {
                result.append("   ✗ 数据库连接失败: ").append(e.getMessage()).append("\n");
                result.append("\n请检查以下项目：\n");
                result.append("1. MySQL服务是否正在运行\n");
                result.append("2. 数据库是否存在: proxyai_metrics\n");
                result.append("3. 用户名和密码是否正确\n");
                result.append("4. 网络连接是否正常\n");
                result.append("5. 防火墙设置是否允许连接\n");
                
                Messages.showErrorDialog(result.toString(), "连接失败");
                return;
            }
            
        } catch (Exception e) {
            result.append("\n✗ 测试过程中发生错误: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }
        
        // 显示测试结果
        Messages.showInfoMessage(result.toString(), "数据库连接测试结果");
    }
}
