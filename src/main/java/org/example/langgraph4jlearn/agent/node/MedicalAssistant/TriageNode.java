package org.example.langgraph4jlearn.agent.node.MedicalAssistant;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.langgraph4jlearn.agent.state.MedicalSystemState;
import org.example.langgraph4jlearn.enums.SystemStage;

import java.util.Map;

@Slf4j
public class TriageNode implements NodeAction<MedicalSystemState> {
    
    private static final String[] EMERGENCY_KEYWORDS = {
        "胸痛", "呼吸困难", "窒息", "大出血", "昏迷", "意识不清",
        "严重外伤", "骨折", "中毒", "休克", "突然晕倒", "抽搐",
        "剧烈腹痛", "高烧不退", "呕血", "便血", "心梗", "脑梗"
    };
    
    private static final String[] CONSULTATION_KEYWORDS = {
        "咨询", "问诊", "症状", "不舒服", "感冒", "发烧", "咳嗽",
        "头痛", "肚子疼", "拉肚子", "便秘", "失眠"
    };
    
    private static final String[] DRUG_KEYWORDS = {
        "药", "吃什么药", "用药", "药品", "处方", "副作用", "药物"
    };
    
    private static final String[] HEALTH_PLAN_KEYWORDS = {
        "健康", "锻炼", "运动", "饮食", "营养", "保健", "养生",
        "减肥", "增重", "调理"
    };
    
    @Override
    public Map<String, Object> apply(MedicalSystemState state) {
        log.info("=== 意图识别/分诊节点执行 ===");
        
        String userQuery = state.userQuery();
        log.info("用户查询: {}", userQuery);
        
        // 1. 首先检测是否为紧急情况
        boolean isEmergency = detectEmergency(userQuery);
        
        if (isEmergency) {
            log.warn("检测到紧急情况！");
            return Map.of(
                    MedicalSystemState.IS_EMERGENCY, true,
                    MedicalSystemState.INTENT, "emergency",
                    MedicalSystemState.STAGE, SystemStage.EMERGENCY_CHECK,
                    MedicalSystemState.MESSAGES, "系统检测到可能的紧急情况"
            );
        }
        
        // 2. 非紧急情况，进行意图分类
        String intent = classifyIntent(userQuery);
        log.info("识别意图: {}", intent);
        
        return Map.of(
                MedicalSystemState.IS_EMERGENCY, false,
                MedicalSystemState.INTENT, intent,
                MedicalSystemState.STAGE, SystemStage.CORE_PROCESSING,
                MedicalSystemState.MESSAGES, "意图识别完成: " + intent,
                MedicalSystemState.CONTEXT, "分诊结果: " + intent
        );
    }
    
    private boolean detectEmergency(String query) {
        if (query == null || query.isEmpty()) {
            return false;
        }
        
        String lowerQuery = query.toLowerCase();
        for (String keyword : EMERGENCY_KEYWORDS) {
            if (lowerQuery.contains(keyword)) {
                log.warn("发现紧急关键词: {}", keyword);
                return true;
            }
        }
        return false;
    }
    
    private String classifyIntent(String query) {
        if (query == null || query.isEmpty()) {
            return "general_consultation";
        }
        
        String lowerQuery = query.toLowerCase();
        
        // 检查药品相关
        for (String keyword : DRUG_KEYWORDS) {
            if (lowerQuery.contains(keyword)) {
                return "drug_consultation";
            }
        }
        
        // 检查健康计划相关
        for (String keyword : HEALTH_PLAN_KEYWORDS) {
            if (lowerQuery.contains(keyword)) {
                return "health_plan";
            }
        }
        
        // 检查问诊相关
        for (String keyword : CONSULTATION_KEYWORDS) {
            if (lowerQuery.contains(keyword)) {
                return "medical_consultation";
            }
        }
        
        // 默认为一般咨询
        return "general_consultation";
    }
}
