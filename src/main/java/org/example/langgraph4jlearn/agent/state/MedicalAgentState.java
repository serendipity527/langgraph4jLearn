package org.example.langgraph4jlearn.agent.state;

import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 医疗健康智能体状态类
 * 用于多智能体医疗健康咨询系统
 */
public class MedicalAgentState extends org.bsc.langgraph4j.state.AgentState {
    
    // 状态键常量
    public static final String USER_QUERY = "userQuery";           // 用户原始查询
    public static final String INTENT = "intent";                  // 用户意图
    public static final String MESSAGES = "messages";              // 消息历史
    public static final String CONSULTATION_INFO = "consultationInfo"; // 预问诊信息
    public static final String DRUG_INFO = "drugInfo";            // 药品信息
    public static final String HEALTH_PLAN = "healthPlan";        // 健康计划
    public static final String RISK_ASSESSMENT = "riskAssessment"; // 风险评估
    public static final String FINAL_RESULT = "finalResult";      // 最终结果
    public static final String CONVERSION_TYPE = "conversionType"; // 转化类型
    public static final String CONVERSION_RESULT = "conversionResult"; // 转化结果
    
    /**
     * 定义状态Schema
     * 注意：只需要为需要特殊行为（如追加）的字段定义Channel
     * 其他字段可以直接在Map中操作，使用默认的lastValue行为
     */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES, Channels.appender(ArrayList::new)
    );
    
    /**
     * 构造函数
     */
    public MedicalAgentState(Map<String, Object> initData) {
        super(initData);
    }
    
    // Getter方法
    public String userQuery() {
        return this.<String>value(USER_QUERY).orElse("");
    }
    
    public String intent() {
        return this.<String>value(INTENT).orElse("");
    }
    
    public List<String> messages() {
        return this.<List<String>>value(MESSAGES).orElse(List.of());
    }
    
    public String consultationInfo() {
        return this.<String>value(CONSULTATION_INFO).orElse("");
    }
    
    public String drugInfo() {
        return this.<String>value(DRUG_INFO).orElse("");
    }
    
    public String healthPlan() {
        return this.<String>value(HEALTH_PLAN).orElse("");
    }
    
    public String riskAssessment() {
        return this.<String>value(RISK_ASSESSMENT).orElse("");
    }
    
    public String finalResult() {
        return this.<String>value(FINAL_RESULT).orElse("");
    }
    
    public String conversionType() {
        return this.<String>value(CONVERSION_TYPE).orElse("");
    }
    
    public String conversionResult() {
        return this.<String>value(CONVERSION_RESULT).orElse("");
    }
}
