package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;

/**
 * 效能度量服务工厂
 * 提供便捷的方法来获取MetricsService实例
 */
public class MetricsServiceFactory {
    
    private static final Logger LOG = Logger.getInstance(MetricsServiceFactory.class);
    
    /**
     * 获取效能度量服务实例
     * 
     * @param project 项目实例
     * @return MetricsService实例，如果获取失败则返回null
     */
    public static MetricsService getMetricsService(Project project) {
        try {
            if (project == null || project.isDisposed()) {
                LOG.warn("项目实例为空或已销毁，无法获取效能度量服务");
                return null;
            }
            
            ProductivityMetricsManager manager = ProductivityMetricsManager.getInstance(project);
            if (manager == null) {
                LOG.warn("无法获取ProductivityMetricsManager实例");
                return null;
            }
            
            if (!manager.isServiceAvailable()) {
                LOG.warn("效能度量服务不可用");
                return null;
            }
            
            return manager;
            
        } catch (Exception e) {
            LOG.error("获取效能度量服务失败", e);
            return null;
        }
    }
    
    /**
     * 检查效能度量服务是否可用
     * 
     * @param project 项目实例
     * @return 服务是否可用
     */
    public static boolean isMetricsServiceAvailable(Project project) {
        try {
            MetricsService service = getMetricsService(project);
            return service != null && service.isServiceAvailable();
        } catch (Exception e) {
            LOG.debug("检查效能度量服务可用性失败", e);
            return false;
        }
    }
    
    /**
     * 安全地记录代码补全指标
     * 
     * @param project 项目实例
     * @param language 编程语言
     * @param suggestedCode 建议的代码
     * @param accepted 是否被接受
     * @param processingTime 处理时间（毫秒）
     */
    public static void safeRecordCodeCompletionMetrics(Project project, String language, 
                                                     String suggestedCode, boolean accepted, long processingTime) {
        try {
            MetricsService service = getMetricsService(project);
            if (service != null) {
                service.recordCodeCompletionMetrics(language, suggestedCode, accepted, processingTime);
            }
        } catch (Exception e) {
            LOG.debug("安全记录代码补全指标失败", e);
        }
    }
    
    /**
     * 安全地记录聊天代码生成指标
     * 
     * @param project 项目实例
     * @param generatedCode 生成的代码
     * @param applied 是否被应用
     * @param processingTime 处理时间（毫秒）
     * @param sessionId 会话ID
     */
    public static void safeRecordChatCodeGenerationMetrics(Project project, String generatedCode, 
                                                         boolean applied, long processingTime, String sessionId) {
        try {
            MetricsService service = getMetricsService(project);
            if (service != null) {
                service.recordChatCodeGenerationMetrics(generatedCode, applied, processingTime, sessionId);
            }
        } catch (Exception e) {
            LOG.debug("安全记录聊天代码生成指标失败", e);
        }
    }
    
    /**
     * 安全地记录用户评分
     * 
     * @param project 项目实例
     * @param actionId 操作ID
     * @param rating 评分（1-5）
     * @param feedback 反馈信息
     */
    public static void safeRecordUserRating(Project project, String actionId, int rating, String feedback) {
        try {
            MetricsService service = getMetricsService(project);
            if (service != null) {
                service.recordUserRating(actionId, rating, feedback);
            }
        } catch (Exception e) {
            LOG.debug("安全记录用户评分失败", e);
        }
    }
}
