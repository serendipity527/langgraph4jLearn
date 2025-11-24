package org.example.langgraph4jlearn.demo;

import lombok.extern.slf4j.Slf4j;
import org.example.langgraph4jlearn.agent.graph.MedicalAssistantGraph;
import org.example.langgraph4jlearn.agent.state.MedicalSystemState;
import org.example.langgraph4jlearn.enums.SystemStage;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MedicalAssistantStage1Demo {
    
    public static void main(String[] args) throws Exception {
        log.info("========================================");
        log.info("医疗AI助手 - 阶段1演示：免责声明与用户同意");
        log.info("========================================\n");
        
        MedicalAssistantGraph graph = new MedicalAssistantGraph();
        
        // 场景1：用户同意条款
        log.info("\n【场景1：用户同意条款】");
        Map<String, Object> scenario1 = new HashMap<>();
        scenario1.put(MedicalSystemState.USER_QUERY, "我最近感冒了，想咨询一下");
        scenario1.put(MedicalSystemState.USER_CONSENTED, true);
        scenario1.put(MedicalSystemState.STAGE, SystemStage.DISCLAIMER);
        
        MedicalSystemState result1 = graph.execute(scenario1);
        log.info("\n【场景1结果】");
        log.info("最终阶段: {}", result1.stage());
        log.info("免责声明已展示: {}", result1.disclaimerShown());
        log.info("用户已同意: {}", result1.userConsented());
        log.info("消息历史: {}", result1.messages());
        
        // 场景2：用户拒绝条款
        log.info("\n\n【场景2：用户拒绝条款】");
        Map<String, Object> scenario2 = new HashMap<>();
        scenario2.put(MedicalSystemState.USER_QUERY, "我想咨询健康问题");
        scenario2.put(MedicalSystemState.USER_CONSENTED, false);
        scenario2.put(MedicalSystemState.STAGE, SystemStage.DISCLAIMER);
        
        MedicalSystemState result2 = graph.execute(scenario2);
        log.info("\n【场景2结果】");
        log.info("最终阶段: {}", result2.stage());
        log.info("免责声明已展示: {}", result2.disclaimerShown());
        log.info("用户已同意: {}", result2.userConsented());
        log.info("响应内容: {}", result2.response());
        log.info("消息历史: {}", result2.messages());
        
        log.info("\n========================================");
        log.info("阶段1演示完成");
        log.info("========================================");
    }
}
