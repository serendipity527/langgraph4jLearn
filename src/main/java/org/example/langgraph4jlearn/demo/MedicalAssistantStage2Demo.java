package org.example.langgraph4jlearn.demo;

import lombok.extern.slf4j.Slf4j;
import org.example.langgraph4jlearn.agent.graph.MedicalAssistantGraph;
import org.example.langgraph4jlearn.agent.state.MedicalSystemState;
import org.example.langgraph4jlearn.enums.SystemStage;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MedicalAssistantStage2Demo {
    
    public static void main(String[] args) throws Exception {
        log.info("========================================");
        log.info("医疗AI助手 - 阶段2演示：意图识别与紧急分诊");
        log.info("========================================\n");
        
        MedicalAssistantGraph graph = new MedicalAssistantGraph();
        
        // 场景1：紧急情况 - 胸痛
        log.info("\n【场景1：紧急情况 - 胸痛】");
        Map<String, Object> scenario1 = new HashMap<>();
        scenario1.put(MedicalSystemState.USER_QUERY, "我突然胸痛，呼吸困难，该怎么办？");
        scenario1.put(MedicalSystemState.USER_CONSENTED, true);
        scenario1.put(MedicalSystemState.STAGE, SystemStage.DISCLAIMER);
        
        MedicalSystemState result1 = graph.execute(scenario1);
        log.info("\n【场景1结果】");
        log.info("最终阶段: {}", result1.stage());
        log.info("是否紧急: {}", result1.isEmergency());
        log.info("识别意图: {}", result1.intent());
        log.info("响应内容: \n{}", result1.response());
        log.info("消息历史: {}", result1.messages());
        
        // 场景2：紧急情况 - 严重外伤
        log.info("\n\n【场景2：紧急情况 - 严重外伤】");
        Map<String, Object> scenario2 = new HashMap<>();
        scenario2.put(MedicalSystemState.USER_QUERY, "我遇到车祸，大出血，怎么处理？");
        scenario2.put(MedicalSystemState.USER_CONSENTED, true);
        scenario2.put(MedicalSystemState.STAGE, SystemStage.DISCLAIMER);
        
        MedicalSystemState result2 = graph.execute(scenario2);
        log.info("\n【场景2结果】");
        log.info("最终阶段: {}", result2.stage());
        log.info("是否紧急: {}", result2.isEmergency());
        log.info("识别意图: {}", result2.intent());
        log.info("响应内容: \n{}", result2.response());
        
        // 场景3：普通咨询 - 感冒症状
        log.info("\n\n【场景3：普通咨询 - 感冒症状】");
        Map<String, Object> scenario3 = new HashMap<>();
        scenario3.put(MedicalSystemState.USER_QUERY, "我最近感冒了，有点咳嗽和发烧，想咨询一下");
        scenario3.put(MedicalSystemState.USER_CONSENTED, true);
        scenario3.put(MedicalSystemState.STAGE, SystemStage.DISCLAIMER);
        
        MedicalSystemState result3 = graph.execute(scenario3);
        log.info("\n【场景3结果】");
        log.info("最终阶段: {}", result3.stage());
        log.info("是否紧急: {}", result3.isEmergency());
        log.info("识别意图: {}", result3.intent());
        log.info("上下文: {}", result3.context());
        log.info("消息历史: {}", result3.messages());
        
        // 场景4：药品咨询
        log.info("\n\n【场景4：药品咨询】");
        Map<String, Object> scenario4 = new HashMap<>();
        scenario4.put(MedicalSystemState.USER_QUERY, "感冒应该吃什么药？有什么副作用吗？");
        scenario4.put(MedicalSystemState.USER_CONSENTED, true);
        scenario4.put(MedicalSystemState.STAGE, SystemStage.DISCLAIMER);
        
        MedicalSystemState result4 = graph.execute(scenario4);
        log.info("\n【场景4结果】");
        log.info("最终阶段: {}", result4.stage());
        log.info("是否紧急: {}", result4.isEmergency());
        log.info("识别意图: {}", result4.intent());
        log.info("上下文: {}", result4.context());
        
        // 场景5：健康计划咨询
        log.info("\n\n【场景5：健康计划咨询】");
        Map<String, Object> scenario5 = new HashMap<>();
        scenario5.put(MedicalSystemState.USER_QUERY, "我想制定一个健康的饮食和锻炼计划");
        scenario5.put(MedicalSystemState.USER_CONSENTED, true);
        scenario5.put(MedicalSystemState.STAGE, SystemStage.DISCLAIMER);
        
        MedicalSystemState result5 = graph.execute(scenario5);
        log.info("\n【场景5结果】");
        log.info("最终阶段: {}", result5.stage());
        log.info("是否紧急: {}", result5.isEmergency());
        log.info("识别意图: {}", result5.intent());
        log.info("上下文: {}", result5.context());
        
        log.info("\n========================================");
        log.info("阶段2演示完成");
        log.info("========================================");
    }
}
