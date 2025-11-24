package org.example.langgraph4jlearn.agent.graph;

import lombok.extern.slf4j.Slf4j;
import org.example.langgraph4jlearn.agent.state.MedicalAgentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MedicalAgent测试类
 * 测试三种不同的场景：预问诊、药品查询、健康计划
 */
@Slf4j
class MedicalAgentTest {
    
    private MedicalAgent medicalAgent;
    
    @BeforeEach
    void setUp() {
        medicalAgent = new MedicalAgent();
    }
    
    /**
     * 测试预问诊场景
     */
    @Test
    void testConsultationScenario() throws Exception {
        log.info("\n\n");
        log.info("=" .repeat(80));
        log.info("测试场景1：预问诊");
        log.info("=" .repeat(80));
        
        String query = "我最近总是感觉胃痛，特别是吃完饭后更明显";
        MedicalAgentState finalState = medicalAgent.executeAndGetState(query);
        
        // 验证
        assertNotNull(finalState, "最终状态不应为空");
        assertEquals("预问诊", finalState.intent(), "意图应该是预问诊");
        assertNotNull(finalState.consultationInfo(), "预问诊信息不应为空");
        assertNotNull(finalState.riskAssessment(), "风险评估不应为空");
        assertNotNull(finalState.finalResult(), "最终结果不应为空");
        assertEquals("医生推荐", finalState.conversionType(), "转化类型应该是医生推荐");
        assertNotNull(finalState.conversionResult(), "转化结果不应为空");
        
        log.info("\n测试场景1完成！\n");
    }
    
    /**
     * 测试药品查询场景
     */
    @Test
    void testDrugInfoScenario() throws Exception {
        log.info("\n\n");
        log.info("=" .repeat(80));
        log.info("测试场景2：药品查询");
        log.info("=" .repeat(80));
        
        String query = "请问阿司匹林肠溶片有什么作用，怎么服用？";
        MedicalAgentState finalState = medicalAgent.executeAndGetState(query);
        
        // 验证
        assertNotNull(finalState, "最终状态不应为空");
        assertEquals("药品", finalState.intent(), "意图应该是药品");
        assertNotNull(finalState.drugInfo(), "药品信息不应为空");
        assertNotNull(finalState.riskAssessment(), "风险评估不应为空");
        assertNotNull(finalState.finalResult(), "最终结果不应为空");
        assertEquals("药品购买", finalState.conversionType(), "转化类型应该是药品购买");
        assertNotNull(finalState.conversionResult(), "转化结果不应为空");
        
        log.info("\n测试场景2完成！\n");
    }
    
    /**
     * 测试健康计划场景
     */
    @Test
    void testHealthPlanScenario() throws Exception {
        log.info("\n\n");
        log.info("=" .repeat(80));
        log.info("测试场景3：健康计划");
        log.info("=" .repeat(80));
        
        String query = "我想制定一个健康管理计划，改善我的生活习惯";
        MedicalAgentState finalState = medicalAgent.executeAndGetState(query);
        
        // 验证
        assertNotNull(finalState, "最终状态不应为空");
        assertEquals("健康计划", finalState.intent(), "意图应该是健康计划");
        assertNotNull(finalState.healthPlan(), "健康计划不应为空");
        assertNotNull(finalState.riskAssessment(), "风险评估不应为空");
        assertNotNull(finalState.finalResult(), "最终结果不应为空");
        assertEquals("健康保健", finalState.conversionType(), "转化类型应该是健康保健");
        assertNotNull(finalState.conversionResult(), "转化结果不应为空");
        
        log.info("\n测试场景3完成！\n");
    }
    
    /**
     * 测试完整流程（不带断言，仅展示输出）
     */
    @Test
    void testCompleteFlow() throws Exception {
        log.info("\n\n");
        log.info("=" .repeat(80));
        log.info("测试完整流程展示");
        log.info("=" .repeat(80));
        
        String[] queries = {
            "我头疼发烧，应该怎么办？",
            "感冒药有哪些，怎么选择？",
            "如何制定适合上班族的健康计划？"
        };
        
        for (int i = 0; i < queries.length; i++) {
            log.info("\n\n【查询 {}】{}", i + 1, queries[i]);
            medicalAgent.executeAndGetState(queries[i]);
            
            if (i < queries.length - 1) {
                Thread.sleep(1000); // 暂停1秒，便于观察输出
            }
        }
        
        log.info("\n完整流程测试完成！\n");
    }
}
