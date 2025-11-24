package org.example.langgraph4jlearn.demo;

import lombok.extern.slf4j.Slf4j;
import org.example.langgraph4jlearn.agent.graph.MedicalAgent;

/**
 * 医疗健康多智能体演示类
 * 可以直接运行查看效果
 */
@Slf4j
public class MedicalAgentDemo {
    
    public static void main(String[] args) {
        try {
            log.info("医疗健康多智能体系统演示");
            log.info("本系统包含9个智能体，可以处理预问诊、药品查询、健康计划等场景\n");
            
            MedicalAgent agent = new MedicalAgent();
            
            // 场景1：预问诊
            demonstrateScenario(agent, "场景1：预问诊咨询", 
                    "我最近总是感觉胃痛，特别是吃完饭后更明显，还伴有恶心的症状");
            
            Thread.sleep(2000);
            
            // 场景2：药品查询
            demonstrateScenario(agent, "场景2：药品信息查询", 
                    "请问布洛芬缓释胶囊有什么作用？怎么服用？有什么注意事项？");
            
            Thread.sleep(2000);
            
            // 场景3：健康计划
            demonstrateScenario(agent, "场景3：健康管理计划", 
                    "我是一名程序员，长期久坐，想制定一个健康管理计划来改善身体状况");
            
        } catch (Exception e) {
            log.error("演示过程中出现异常", e);
        }
    }
    
    /**
     * 演示单个场景
     */
    private static void demonstrateScenario(MedicalAgent agent, String scenarioName, String query) {
        try {
            log.info("\n" + "=".repeat(100));
            log.info("{}开始", scenarioName);
            log.info("=".repeat(100));
            log.info("用户问题: {}\n", query);
            
            agent.executeAndGetState(query);
            
            log.info("\n{} 完成", scenarioName);
            log.info("=".repeat(100) + "\n");
            
        } catch (Exception e) {
            log.error("场景执行失败: {}", scenarioName, e);
        }
    }
}
