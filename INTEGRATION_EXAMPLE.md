# ProxyAI 提效度量系统集成示例

## 快速集成指南

### 1. 在代码补全功能中集成度量收集

假设你有一个现有的代码补全提供者，可以这样集成：

```java
// 在你的代码补全提供者中
public class YourCodeCompletionProvider {
    
    public void provideCompletion(CompletionParameters parameters, CompletionResultSet result) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 原有的代码补全逻辑
            String completionText = generateAICompletion(parameters);
            
            // 用户是否接受了补全
            boolean wasAccepted = userAcceptedCompletion(completionText);
            
            // 记录提效度量
            long responseTime = System.currentTimeMillis() - startTime;
            CodeCompletionMetricsIntegration.recordCodeCompletionMetrics(
                parameters.getEditor(), 
                completionText, 
                wasAccepted, 
                responseTime
            );
            
        } catch (Exception e) {
            // 处理异常
        }
    }
}
```

### 2. 在聊天功能中集成度量收集

```java
// 在你的聊天服务中
public class YourChatService {
    
    public void startNewChatSession(String sessionId, String taskType) {
        // 开始聊天会话度量收集
        ChatMetricsIntegration.startChatSession(sessionId, taskType);
    }
    
    public void handleUserMessage(String sessionId, String userMessage) {
        // 记录用户提问
        ChatMetricsIntegration.recordUserQuestion(sessionId, userMessage, "");
    }
    
    public void handleAIResponse(String sessionId, String aiResponse, String generatedCode) {
        // 记录AI响应和代码生成
        ChatMetricsIntegration.recordAIResponse(sessionId, aiResponse, generatedCode);
    }
    
    public void handleCodeApplication(String sessionId, String appliedCode) {
        // 记录代码应用
        ChatMetricsIntegration.recordCodeApplication(sessionId, appliedCode, "manual_apply");
    }
    
    public void endChatSession(String sessionId) {
        // 结束聊天会话
        ChatMetricsIntegration.endChatSession(sessionId);
    }
}
```

### 3. 在调试功能中集成度量收集

```java
// 在调试辅助功能中
public class YourDebuggingAssistant {
    
    public void startDebuggingSession(String issueType) {
        long startTime = System.currentTimeMillis();
        
        // 调试逻辑...
        boolean resolvedWithAI = performAIAssistedDebugging();
        
        long endTime = System.currentTimeMillis();
        
        // 记录调试效率
        MetricsIntegration.getInstance().recordDebuggingSession(
            startTime, endTime, resolvedWithAI, issueType
        );
    }
}
```

## 查看提效统计

### 方法1: 通过工具窗口
1. 打开 `View → Tool Windows → ProxyAI-Metrics`
2. 查看实时的提效统计数据
3. 点击"导出报告"生成详细分析

### 方法2: 通过设置页面
1. 打开 `File → Settings → ProxyAI → 提效度量`
2. 查看总体统计信息
3. 点击"查看统计信息"获取详细数据

### 方法3: 程序化获取数据
```java
// 获取提效报告
ProductivityMetrics.ProductivityReport report = 
    ProductivityMetrics.getInstance().getProductivityReport(30); // 最近30天

System.out.println("总节省时间: " + report.totalTimeSavedHours + " 小时");
System.out.println("平均效率提升: " + report.avgEfficiencyGain + "%");
System.out.println("代码接受率: " + (report.avgCodeAcceptanceRate * 100) + "%");
```

## 自定义度量指标

如果你想添加自定义的度量指标：

```java
// 扩展ProductivityMetrics类
public void recordCustomMetric(String metricName, Object data) {
    // 自定义度量逻辑
    CustomMetric metric = new CustomMetric();
    metric.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    metric.metricName = metricName;
    metric.data = data;
    
    // 存储到状态中
    state.customMetrics.add(metric);
}

// 在你的代码中使用
MetricsIntegration.getInstance().recordCustomMetric("custom_feature_usage", usageData);
```

## 配置选项

在设置中可以配置：

- **启用/禁用度量收集**: 完全控制是否收集数据
- **自动导出报告**: 设置定期自动生成报告
- **详细日志记录**: 启用更详细的活动日志
- **数据清理**: 一键清空所有历史数据

## 注意事项

1. **性能影响**: 度量收集设计为轻量级，对IDE性能影响极小
2. **隐私保护**: 所有数据仅存储在本地，不会上传到任何服务器
3. **异常处理**: 度量收集失败不会影响正常的AI功能使用
4. **数据准确性**: 建议定期检查度量数据的合理性

## 故障排除

如果遇到问题：

1. **检查服务状态**: 确认MetricsIntegration服务已正确初始化
2. **查看日志**: 检查IDE日志中的相关错误信息
3. **重置数据**: 在设置中清空数据后重新开始收集
4. **联系支持**: 通过GitHub Issues报告问题

这个提效度量系统将帮助你量化AI编程助手的真实价值，为持续优化提供数据支持。