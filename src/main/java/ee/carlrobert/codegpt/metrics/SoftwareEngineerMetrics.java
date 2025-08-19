package ee.carlrobert.codegpt.metrics;

import java.util.Arrays;
import java.util.List;

/**
 * 软件工程师效能指标定义
 * 定义了通过ProxyAI使用大模型时的各种效能度量指标
 */
public enum SoftwareEngineerMetrics {
    
    // 代码生成效能指标
    CODE_GENERATION_EFFICIENCY("代码生成效能", "code_generation", Arrays.asList(
        "lines_generated", "lines_accepted", "acceptance_rate", "generation_time"
    )),
    
    // 代码质量指标
    CODE_QUALITY("代码质量", "code_quality", Arrays.asList(
        "syntax_correctness", "semantic_accuracy", "best_practices", "code_complexity"
    )),
    
    // 开发速度指标
    DEVELOPMENT_SPEED("开发速度", "dev_speed", Arrays.asList(
        "time_to_first_result", "iteration_speed", "total_development_time", "feature_completion_rate"
    )),
    
    // 问题解决效能指标
    PROBLEM_SOLVING_EFFICIENCY("问题解决效能", "problem_solving", Arrays.asList(
        "time_to_solution", "solution_accuracy", "debugging_efficiency", "error_resolution_rate"
    )),
    
    // 学习曲线指标
    LEARNING_CURVE("学习曲线", "learning_curve", Arrays.asList(
        "new_concept_understanding", "tool_adoption_rate", "skill_improvement", "knowledge_retention"
    )),
    
    // 协作效能指标
    COLLABORATION_EFFICIENCY("协作效能", "collaboration", Arrays.asList(
        "code_review_efficiency", "documentation_quality", "knowledge_sharing", "team_communication"
    )),
    
    // 创新效能指标
    INNOVATION_EFFICIENCY("创新效能", "innovation", Arrays.asList(
        "creative_solutions", "optimization_ideas", "architecture_improvements", "best_practice_adoption"
    )),
    
    // 成本效益指标
    COST_EFFECTIVENESS("成本效益", "cost_effectiveness", Arrays.asList(
        "token_usage", "api_cost", "time_savings", "productivity_gain"
    )),
    
    // 用户体验指标
    USER_EXPERIENCE("用户体验", "user_experience", Arrays.asList(
        "ease_of_use", "response_time", "accuracy", "satisfaction_rating"
    )),
    
    // 系统性能指标
    SYSTEM_PERFORMANCE("系统性能", "system_performance", Arrays.asList(
        "memory_usage", "cpu_utilization", "network_latency", "response_time"
    ));

    private final String displayName;
    private final String metricKey;
    private final List<String> subMetrics;

    SoftwareEngineerMetrics(String displayName, String metricKey, List<String> subMetrics) {
        this.displayName = displayName;
        this.metricKey = metricKey;
        this.subMetrics = subMetrics;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMetricKey() {
        return metricKey;
    }

    public List<String> getSubMetrics() {
        return subMetrics;
    }

    /**
     * 获取所有指标类型
     */
    public static List<SoftwareEngineerMetrics> getAllMetrics() {
        return Arrays.asList(values());
    }

    /**
     * 根据指标键获取指标类型
     */
    public static SoftwareEngineerMetrics getByKey(String key) {
        return Arrays.stream(values())
                .filter(metric -> metric.getMetricKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取指标权重（用于计算综合效能评分）
     */
    public double getWeight() {
        switch (this) {
            case CODE_GENERATION_EFFICIENCY:
                return 0.25; // 25% 权重
            case CODE_QUALITY:
                return 0.20; // 20% 权重
            case DEVELOPMENT_SPEED:
                return 0.20; // 20% 权重
            case PROBLEM_SOLVING_EFFICIENCY:
                return 0.15; // 15% 权重
            case LEARNING_CURVE:
                return 0.10; // 10% 权重
            case COLLABORATION_EFFICIENCY:
                return 0.05; // 5% 权重
            case INNOVATION_EFFICIENCY:
                return 0.03; // 3% 权重
            case COST_EFFECTIVENESS:
                return 0.01; // 1% 权重
            case USER_EXPERIENCE:
                return 0.01; // 1% 权重
            case SYSTEM_PERFORMANCE:
                return 0.00; // 不计入总分，仅监控
            default:
                return 0.00;
        }
    }

    /**
     * 获取指标描述
     */
    public String getDescription() {
        switch (this) {
            case CODE_GENERATION_EFFICIENCY:
                return "衡量AI辅助代码生成的效率和准确性，包括生成行数、接受率等";
            case CODE_QUALITY:
                return "评估AI生成代码的质量，包括语法正确性、语义准确性、最佳实践遵循等";
            case DEVELOPMENT_SPEED:
                return "测量使用AI工具后开发速度的提升，包括首次结果时间、迭代速度等";
            case PROBLEM_SOLVING_EFFICIENCY:
                return "评估AI辅助问题解决的效率，包括解决时间、准确性、调试效率等";
            case LEARNING_CURVE:
                return "跟踪开发者对AI工具的掌握程度，包括概念理解、工具采用率等";
            case COLLABORATION_EFFICIENCY:
                return "衡量AI工具对团队协作的促进作用，包括代码审查、文档质量等";
            case INNOVATION_EFFICIENCY:
                return "评估AI工具对创新思维的激发作用，包括创意解决方案、架构改进等";
            case COST_EFFECTIVENESS:
                return "分析AI工具使用的成本效益，包括token使用量、时间节省等";
            case USER_EXPERIENCE:
                return "评估AI工具的用户体验质量，包括易用性、响应时间、满意度等";
            case SYSTEM_PERFORMANCE:
                return "监控AI工具的系统性能表现，包括内存使用、CPU利用率等";
            default:
                return "未知指标类型";
        }
    }

    /**
     * 获取指标目标值（理想状态）
     */
    public String getTargetValue() {
        switch (this) {
            case CODE_GENERATION_EFFICIENCY:
                return "接受率 > 80%, 生成时间 < 5秒";
            case CODE_QUALITY:
                return "语法正确率 100%, 最佳实践遵循率 > 90%";
            case DEVELOPMENT_SPEED:
                return "开发速度提升 > 30%, 首次结果时间 < 10秒";
            case PROBLEM_SOLVING_EFFICIENCY:
                return "问题解决时间减少 > 40%, 准确率 > 85%";
            case LEARNING_CURVE:
                return "新概念理解时间 < 1小时, 工具采用率 > 70%";
            case COLLABORATION_EFFICIENCY:
                return "代码审查效率提升 > 25%, 文档质量评分 > 4.0/5.0";
            case INNOVATION_EFFICIENCY:
                return "创意解决方案数量增加 > 20%, 架构改进采纳率 > 60%";
            case COST_EFFECTIVENESS:
                return "时间节省 > 25%, ROI > 300%";
            case USER_EXPERIENCE:
                return "用户满意度 > 4.5/5.0, 响应时间 < 3秒";
            case SYSTEM_PERFORMANCE:
                return "内存使用 < 2GB, CPU利用率 < 50%";
            default:
                return "未定义";
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
