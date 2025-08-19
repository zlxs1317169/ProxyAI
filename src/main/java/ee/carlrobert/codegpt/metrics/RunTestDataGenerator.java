package ee.carlrobert.codegpt.metrics;

/**
 * 运行测试数据生成器
 * 这个类可以直接在IDE中运行，无需通过Action注册
 */
public class RunTestDataGenerator {
    
    public static void main(String[] args) {
        System.out.println("=== 效能度量测试数据生成器 ===");
        
        try {
            // 尝试获取ProductivityMetrics实例
            System.out.println("1. 尝试获取ProductivityMetrics实例...");
            ProductivityMetrics metrics = ProductivityMetrics.getInstance();
            
            if (metrics != null) {
                System.out.println("✓ 成功获取ProductivityMetrics实例");
                
                // 生成测试数据
                System.out.println("2. 生成测试数据...");
                generateTestData(metrics);
                
                System.out.println("✓ 测试数据生成完成");
                System.out.println("请打开效能度量面板查看数据");
            } else {
                System.out.println("❌ 无法获取ProductivityMetrics实例");
                System.out.println("这可能是因为当前不在IDE环境中运行");
                System.out.println("请在IDE中使用TestDataGenerator类");
            }
        } catch (Exception e) {
            System.err.println("❌ 生成测试数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 生成测试数据
     */
    private static void generateTestData(ProductivityMetrics metrics) {
        java.util.Random random = new java.util.Random();
        
        // 生成代码补全数据
        System.out.println("- 生成代码补全数据...");
        String[] languages = {"java", "kotlin", "python", "javascript", "typescript"};
        for (int i = 0; i < 10; i++) {
            String language = languages[random.nextInt(languages.length)];
            int suggestedLines = 5 + random.nextInt(20);
            int acceptedLines = Math.max(1, (int)(suggestedLines * (0.5 + random.nextDouble() * 0.5)));
            long responseTime = 50 + random.nextInt(200);
            
            metrics.recordCodeCompletion(language, suggestedLines, acceptedLines, responseTime);
        }
        
        // 生成聊天代码生成数据
        System.out.println("- 生成聊天代码生成数据...");
        String[] taskTypes = {"feature_dev", "bug_fix", "refactor", "explain"};
        for (int i = 0; i < 5; i++) {
            String taskType = taskTypes[random.nextInt(taskTypes.length)];
            int generatedLines = 10 + random.nextInt(50);
            int appliedLines = Math.max(1, (int)(generatedLines * (0.6 + random.nextDouble() * 0.4)));
            long sessionDuration = 60000 + random.nextInt(300000); // 1-6分钟
            
            metrics.recordChatCodeGeneration(generatedLines, appliedLines, sessionDuration, taskType);
        }
        
        // 生成时间节省数据
        System.out.println("- 生成时间节省数据...");
        for (int i = 0; i < 5; i++) {
            String taskType = i % 2 == 0 ? "coding" : "debugging";
            long traditionalTime = 1800000 + random.nextInt(3600000); // 30-90分钟
            long aiAssistedTime = (long)(traditionalTime * (0.3 + random.nextDouble() * 0.4)); // 节省60-70%
            int linesOfCode = 50 + random.nextInt(200);
            
            metrics.recordTimeSaving(taskType, traditionalTime, aiAssistedTime, linesOfCode);
        }
        
        // 生成调试时间节省数据
        System.out.println("- 生成调试时间节省数据...");
        String[] issueTypes = {"logic_error", "syntax_error", "performance_issue", "memory_leak"};
        for (int i = 0; i < 3; i++) {
            String issueType = issueTypes[random.nextInt(issueTypes.length)];
            long debugTimeWithoutAI = 1200000 + random.nextInt(2400000); // 20-60分钟
            long debugTimeWithAI = (long)(debugTimeWithoutAI * (0.4 + random.nextDouble() * 0.3)); // 节省30-60%
            
            metrics.recordDebuggingTimeSaving(debugTimeWithoutAI, debugTimeWithAI, issueType);
        }
        
        // 生成代码质量改进数据
        System.out.println("- 生成代码质量改进数据...");
        String[] metricTypes = {"complexity", "coverage", "maintainability"};
        String[] improvementTypes = {"refactor", "optimize", "review"};
        for (int i = 0; i < 3; i++) {
            String metricType = metricTypes[random.nextInt(metricTypes.length)];
            String improvementType = improvementTypes[random.nextInt(improvementTypes.length)];
            double beforeValue = 50 + random.nextDouble() * 30;
            double afterValue = beforeValue + (10 + random.nextDouble() * 20);
            
            metrics.recordCodeQualityImprovement(metricType, beforeValue, afterValue, improvementType);
        }
        
        // 生成学习活动数据
        System.out.println("- 生成学习活动数据...");
        String[] topicTypes = {"new_framework", "debugging", "best_practices", "language_feature"};
        for (int i = 0; i < 4; i++) {
            String topicType = topicTypes[random.nextInt(topicTypes.length)];
            int questionsAsked = 3 + random.nextInt(8);
            int conceptsLearned = 2 + random.nextInt(5);
            long learningTime = 600000 + random.nextInt(1800000); // 10-40分钟
            
            metrics.recordLearningActivity(topicType, questionsAsked, conceptsLearned, learningTime);
        }
        
        // 生成历史数据（过去几天）
        System.out.println("- 生成历史数据...");
        for (int daysAgo = 1; daysAgo <= 7; daysAgo++) {
            // 为过去的日期生成一些数据
            java.time.LocalDate pastDate = java.time.LocalDate.now().minusDays(daysAgo);
            String dateStr = pastDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            
            // 手动更新每日统计
            ProductivityMetrics.DailyProductivityStats stats = metrics.getDailyStats(dateStr);
            stats.codeCompletionsCount = 5 + random.nextInt(15);
            stats.chatSessionsCount = 2 + random.nextInt(5);
            stats.timeSavedMs = (1800000 + random.nextInt(5400000)); // 30-180分钟
                                        stats.totalLinesGenerated = 50 + random.nextInt(200);
            stats.avgResponseTime = 100 + random.nextDouble() * 150;
            
            // 尝试通过反射设置日期数据
            try {
                java.lang.reflect.Field stateField = ProductivityMetrics.class.getDeclaredField("state");
                stateField.setAccessible(true);
                Object stateObj = stateField.get(metrics);
                
                java.lang.reflect.Field dailyStatsField = stateObj.getClass().getDeclaredField("dailyStats");
                dailyStatsField.setAccessible(true);
                Object dailyStatsObj = dailyStatsField.get(stateObj);
                
                if (dailyStatsObj instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, ProductivityMetrics.DailyProductivityStats> dailyStats = 
                        (java.util.Map<String, ProductivityMetrics.DailyProductivityStats>) dailyStatsObj;
                    dailyStats.put(dateStr, stats);
                }
            } catch (Exception e) {
                System.out.println("  无法通过反射设置历史数据: " + e.getMessage());
            }
        }
        
        System.out.println("\n已生成测试数据：");
        System.out.println("- 代码补全: 10条");
        System.out.println("- 聊天代码生成: 5条");
        System.out.println("- 时间节省: 5条");
        System.out.println("- 调试时间节省: 3条");
        System.out.println("- 代码质量改进: 3条");
        System.out.println("- 学习活动: 4条");
        System.out.println("- 历史数据: 7天");
    }
}