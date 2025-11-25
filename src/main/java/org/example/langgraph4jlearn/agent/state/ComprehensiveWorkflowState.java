package org.example.langgraph4jlearn.agent.state;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 综合工作流状态 - 展示 State Schema 和 Channels
 */
public class ComprehensiveWorkflowState extends AgentState {
    
    public static final String USER_INPUT = "user_input";
    public static final String CURRENT_STEP = "current_step";
    public static final String STEP_COUNT = "step_count";
    public static final String INTENT = "intent";
    public static final String REQUIRES_APPROVAL = "requires_approval";
    public static final String APPROVAL_STATUS = "approval_status";
    public static final String HUMAN_FEEDBACK = "human_feedback";
    public static final String RETRY_COUNT = "retry_count";
    public static final String MAX_RETRIES = "max_retries";
    public static final String WORKFLOW_STATUS = "workflow_status";
    public static final String FINAL_RESULT = "final_result";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String MESSAGES = "messages";
    public static final String TOOL_RESULTS = "tool_results";
    public static final String EXECUTION_LOG = "execution_log";
    public static final String HEALTH_QUERY = "health_query";
    public static final String HEALTH_CATEGORY = "health_category";
    public static final String HEALTH_ADVICE = "health_advice";
    
    // Schema: 只定义 Appender 通道，其他使用默认 lastValue
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES, Channels.appender(ArrayList::new),
            TOOL_RESULTS, Channels.appender(ArrayList::new),
            EXECUTION_LOG, Channels.appender(ArrayList::new)
    );
    
    public ComprehensiveWorkflowState(Map<String, Object> initData) {
        super(initData);
    }
    
    public String userInput() {
        return this.<String>value(USER_INPUT).orElse("");
    }
    
    public String currentStep() {
        return this.<String>value(CURRENT_STEP).orElse("");
    }
    
    public int stepCount() {
        return this.<Integer>value(STEP_COUNT).orElse(0);
    }
    
    public String intent() {
        return this.<String>value(INTENT).orElse("unknown");
    }
    
    public boolean requiresApproval() {
        return this.<Boolean>value(REQUIRES_APPROVAL).orElse(false);
    }
    
    public String approvalStatus() {
        return this.<String>value(APPROVAL_STATUS).orElse("pending");
    }
    
    public Optional<String> humanFeedback() {
        return value(HUMAN_FEEDBACK);
    }
    
    public int retryCount() {
        return this.<Integer>value(RETRY_COUNT).orElse(0);
    }
    
    public int maxRetries() {
        return this.<Integer>value(MAX_RETRIES).orElse(3);
    }
    
    public String workflowStatus() {
        return this.<String>value(WORKFLOW_STATUS).orElse("initialized");
    }
    
    public String finalResult() {
        return this.<String>value(FINAL_RESULT).orElse("");
    }
    
    public String errorMessage() {
        return this.<String>value(ERROR_MESSAGE).orElse("");
    }
    
    public String healthQuery() {
        return this.<String>value(HEALTH_QUERY).orElse("");
    }
    
    public String healthCategory() {
        return this.<String>value(HEALTH_CATEGORY).orElse("general");
    }
    
    public String healthAdvice() {
        return this.<String>value(HEALTH_ADVICE).orElse("");
    }
    
    public List<String> messages() {
        return this.<List<String>>value(MESSAGES).orElse(new ArrayList<>());
    }
    
    public List<String> toolResults() {
        return this.<List<String>>value(TOOL_RESULTS).orElse(new ArrayList<>());
    }
    
    public List<String> executionLog() {
        return this.<List<String>>value(EXECUTION_LOG).orElse(new ArrayList<>());
    }
}
