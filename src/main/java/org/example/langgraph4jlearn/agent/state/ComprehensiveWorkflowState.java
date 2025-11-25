package org.example.langgraph4jlearn.agent.state;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.*;

/**
 * 综合工作流状态类
 * 展示 LangGraph4j 的状态管理知识点：
 * - State Schema (状态结构定义)
 * - Channels (通道)
 * - Reducer (归约器 / 状态合并策略)
 * - Annotated State (带注解的状态)
 * - State Update (状态更新机制)
 */
public class ComprehensiveWorkflowState extends AgentState {
    
    // ========== 状态键常量 ==========
    public static final String USER_INPUT = "user_input";           // 用户输入
    public static final String MESSAGES = "messages";               // 消息列表（使用 Appender）
    public static final String CURRENT_STEP = "current_step";       // 当前步骤
    public static final String STEP_COUNT = "step_count";           // 步骤计数
    public static final String INTENT = "intent";                   // 识别的意图
    public static final String REQUIRES_APPROVAL = "requires_approval";  // 是否需要审批
    public static final String APPROVAL_STATUS = "approval_status"; // 审批状态
    public static final String HUMAN_FEEDBACK = "human_feedback";   // 人工反馈
    public static final String TOOL_RESULTS = "tool_results";       // 工具执行结果（使用 Appender）
    public static final String RETRY_COUNT = "retry_count";         // 重试次数（用于演示循环）
    public static final String MAX_RETRIES = "max_retries";         // 最大重试次数
    public static final String EXECUTION_LOG = "execution_log";     // 执行日志（使用 Appender）
    public static final String FINAL_RESULT = "final_result";       // 最终结果
    public static final String ERROR_MESSAGE = "error_message";     // 错误消息
    public static final String WORKFLOW_STATUS = "workflow_status"; // 工作流状态
    public static final String METADATA = "metadata";               // 元数据
    
    /**
     * 定义状态 Schema
     * 
     * 知识点：
     * - Channels.appender(): 追加式更新，新值添加到列表中
     * - 未定义 Channel 的字段使用默认的 lastValue 行为（新值覆盖旧值）
     */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            // 消息列表：使用 Appender Channel，新消息追加到列表
            MESSAGES, Channels.appender(ArrayList::new),
            // 工具执行结果：使用 Appender Channel
            TOOL_RESULTS, Channels.appender(ArrayList::new),
            // 执行日志：使用 Appender Channel
            EXECUTION_LOG, Channels.appender(ArrayList::new)
    );
    
    /**
     * 构造函数
     */
    public ComprehensiveWorkflowState(Map<String, Object> initData) {
        super(initData);
    }
    
    // ========== 状态访问器方法 ==========
    
    public String userInput() {
        return this.<String>value(USER_INPUT).orElse("");
    }
    
    @SuppressWarnings("unchecked")
    public List<String> messages() {
        return this.<List<String>>value(MESSAGES).orElse(new ArrayList<>());
    }
    
    public String currentStep() {
        return this.<String>value(CURRENT_STEP).orElse("init");
    }
    
    public Integer stepCount() {
        return this.<Integer>value(STEP_COUNT).orElse(0);
    }
    
    public String intent() {
        return this.<String>value(INTENT).orElse("unknown");
    }
    
    public Boolean requiresApproval() {
        return this.<Boolean>value(REQUIRES_APPROVAL).orElse(false);
    }
    
    public String approvalStatus() {
        return this.<String>value(APPROVAL_STATUS).orElse("pending");
    }
    
    public Optional<String> humanFeedback() {
        return value(HUMAN_FEEDBACK);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> toolResults() {
        return this.<List<String>>value(TOOL_RESULTS).orElse(new ArrayList<>());
    }
    
    public Integer retryCount() {
        return this.<Integer>value(RETRY_COUNT).orElse(0);
    }
    
    public Integer maxRetries() {
        return this.<Integer>value(MAX_RETRIES).orElse(3);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> executionLog() {
        return this.<List<String>>value(EXECUTION_LOG).orElse(new ArrayList<>());
    }
    
    public String finalResult() {
        return this.<String>value(FINAL_RESULT).orElse("");
    }
    
    public String errorMessage() {
        return this.<String>value(ERROR_MESSAGE).orElse("");
    }
    
    public String workflowStatus() {
        return this.<String>value(WORKFLOW_STATUS).orElse("running");
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> metadata() {
        return this.<Map<String, Object>>value(METADATA).orElse(new HashMap<>());
    }
    
    @Override
    public String toString() {
        return String.format(
            "ComprehensiveWorkflowState{step=%s, intent=%s, approval=%s, retries=%d/%d, status=%s}",
            currentStep(), intent(), approvalStatus(), retryCount(), maxRetries(), workflowStatus()
        );
    }
}
