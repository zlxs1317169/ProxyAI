package ee.carlrobert.codegpt.metrics.integration;

import ee.carlrobert.codegpt.metrics.MetricsIntegration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天功能提效度量集成示例
 * 展示如何在聊天功能中集成提效度量
 */
public class ChatMetricsIntegration {
    
    private static final ConcurrentHashMap<String, ChatSession> activeChatSessions = new ConcurrentHashMap<>();
    
    /**
     * 开始聊天会话
     */
    public static void startChatSession(String sessionId, String taskType) {
        ChatSession session = new ChatSession();
        session.sessionId = sessionId;
        session.taskType = taskType;
        session.startTime = LocalDateTime.now();
        session.questionsAsked = 0;
        
        activeChatSessions.put(sessionId, session);
    }
    
    /**
     * 记录用户提问
     */
    public static void recordUserQuestion(String sessionId, String question, String context) {
        ChatSession session = activeChatSessions.get(sessionId);
        if (session != null) {
            session.questionsAsked++;
            session.lastActivityTime = LocalDateTime.now();
            
            // 分析问题类型
            String questionType = analyzeQuestionType(question);
            session.questionTypes.put(questionType, 
                session.questionTypes.getOrDefault(questionType, 0) + 1);
        }
    }
    
    /**
     * 记录AI响应和代码生成
     */
    public static void recordAIResponse(String sessionId, String response, String generatedCode) {
        ChatSession session = activeChatSessions.get(sessionId);
        if (session != null) {
            session.aiResponses++;
            session.lastActivityTime = LocalDateTime.now();
            
            if (generatedCode != null && !generatedCode.trim().isEmpty()) {
                session.totalGeneratedCode += generatedCode;
                session.codeGenerationCount++;
                
                // 立即记录代码生成度量
                recordCodeGenerationMetrics(session, generatedCode);
                
                System.out.println("✅ 聊天代码生成已记录: " + countLines(generatedCode) + " 行代码");
            }
        }
    }
    
    private static int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\\r?\\n").length;
    }
    
    /**
     * 记录代码应用
     */
    public static void recordCodeApplication(String sessionId, String appliedCode, String applicationType) {
        ChatSession session = activeChatSessions.get(sessionId);
        if (session != null) {
            session.totalAppliedCode += appliedCode;
            session.codeApplicationCount++;
            session.applicationTypes.put(applicationType,
                session.applicationTypes.getOrDefault(applicationType, 0) + 1);
            
            // 实时记录代码生成度量
            recordCodeGenerationMetrics(session, appliedCode);
        }
    }
    
    /**
     * 结束聊天会话
     */
    public static void endChatSession(String sessionId) {
        ChatSession session = activeChatSessions.remove(sessionId);
        if (session != null) {
            session.endTime = LocalDateTime.now();
            recordFinalSessionMetrics(session);
        }
    }
    
    /**
     * 记录学习活动
     */
    public static void recordLearningActivity(String sessionId, String topic) {
        ChatSession session = activeChatSessions.get(sessionId);
        if (session != null) {
            session.learningTopics.add(topic);
            
            // 记录学习度量
            long learningTime = ChronoUnit.MILLIS.between(session.startTime, LocalDateTime.now());
            MetricsIntegration metricsIntegration = MetricsIntegration.getInstance();
            if (metricsIntegration.isInitialized()) {
                metricsIntegration.recordLearningActivity(topic, session.questionsAsked, learningTime);
            }
        }
    }
    
    /**
     * 记录调试会话
     */
    public static void recordDebuggingSession(String sessionId, String issueType, boolean resolved) {
        ChatSession session = activeChatSessions.get(sessionId);
        if (session != null && "debugging".equals(session.taskType)) {
            long debugTime = ChronoUnit.MILLIS.between(session.startTime, LocalDateTime.now());
            
            MetricsIntegration metricsIntegration = MetricsIntegration.getInstance();
            if (metricsIntegration.isInitialized()) {
                metricsIntegration.recordDebuggingSession(
                    session.startTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                    LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                    true, // 使用了AI
                    issueType
                );
            }
        }
    }
    
    private static void recordCodeGenerationMetrics(ChatSession session, String appliedCode) {
        try {
            long sessionDuration = ChronoUnit.MILLIS.between(session.startTime, LocalDateTime.now());
            
            MetricsIntegration metricsIntegration = MetricsIntegration.getInstance();
            if (metricsIntegration.isInitialized()) {
                metricsIntegration.recordAIChatGeneration(
                    session.totalGeneratedCode,
                    appliedCode,
                    sessionDuration,
                    session.taskType
                );
            }
        } catch (Exception e) {
            System.err.println("记录聊天代码生成度量时发生错误: " + e.getMessage());
        }
    }
    
    private static void recordFinalSessionMetrics(ChatSession session) {
        try {
            long totalDuration = ChronoUnit.MILLIS.between(session.startTime, session.endTime);
            
            MetricsIntegration metricsIntegration = MetricsIntegration.getInstance();
            if (metricsIntegration.isInitialized()) {
                // 记录最终的聊天代码生成度量
                if (!session.totalGeneratedCode.isEmpty()) {
                    metricsIntegration.recordAIChatGeneration(
                        session.totalGeneratedCode,
                        session.totalAppliedCode,
                        totalDuration,
                        session.taskType
                    );
                }
                
                // 记录学习活动
                if (!session.learningTopics.isEmpty()) {
                    for (String topic : session.learningTopics) {
                        metricsIntegration.recordLearningActivity(topic, session.questionsAsked, totalDuration);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("记录最终会话度量时发生错误: " + e.getMessage());
        }
    }
    
    private static String analyzeQuestionType(String question) {
        String lowerQuestion = question.toLowerCase();
        
        if (lowerQuestion.contains("debug") || lowerQuestion.contains("error") || lowerQuestion.contains("bug")) {
            return "debugging";
        } else if (lowerQuestion.contains("how") || lowerQuestion.contains("what") || lowerQuestion.contains("explain")) {
            return "learning";
        } else if (lowerQuestion.contains("optimize") || lowerQuestion.contains("improve") || lowerQuestion.contains("refactor")) {
            return "optimization";
        } else if (lowerQuestion.contains("implement") || lowerQuestion.contains("create") || lowerQuestion.contains("write")) {
            return "implementation";
        } else if (lowerQuestion.contains("review") || lowerQuestion.contains("check") || lowerQuestion.contains("analyze")) {
            return "review";
        } else {
            return "general";
        }
    }
    
    /**
     * 聊天会话数据模型
     */
    private static class ChatSession {
        String sessionId;
        String taskType;
        LocalDateTime startTime;
        LocalDateTime endTime;
        LocalDateTime lastActivityTime;
        
        int questionsAsked;
        int aiResponses;
        int codeGenerationCount;
        int codeApplicationCount;
        
        String totalGeneratedCode = "";
        String totalAppliedCode = "";
        
        java.util.Map<String, Integer> questionTypes = new java.util.HashMap<>();
        java.util.Map<String, Integer> applicationTypes = new java.util.HashMap<>();
        java.util.Set<String> learningTopics = new java.util.HashSet<>();
    }
}