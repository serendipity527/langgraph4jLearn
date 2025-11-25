package org.example.langgraph4jlearn.config;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.example.langgraph4jlearn.agent.graph.SimpleAgent;
import org.example.langgraph4jlearn.agent.graph.MedicalAgent;
import org.example.langgraph4jlearn.agent.graph.MedicalAssistantGraph;
import org.example.langgraph4jlearn.agent.graph.ComprehensiveWorkflowGraph;
import org.example.langgraph4jlearn.demo.HumanInLoopDemo;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Slf4j
@Configuration
public class LangGraphStudioConfiguration extends LangGraphStudioConfig {

    @Override
    public Map<String, LangGraphStudioServer.Instance> instanceMap() {
        log.info("初始化 LangGraph Studio 配置...");
        
        try {
            // 创建 SimpleAgent
            SimpleAgent simpleAgent = new SimpleAgent();
            MemorySaver saver1 = new MemorySaver();
            LangGraphStudioServer.Instance simpleAgentInstance = 
                LangGraphStudioServer.Instance.builder()
                    .title("Simple Agent - 简单对话Agent")
                    .addInputStringArg("message")
                    .graph(simpleAgent.getGraph())
                    .compileConfig(CompileConfig.builder()
                        .checkpointSaver(saver1)
                        .build())
                    .build();
            log.info("SimpleAgent 已注册到 LangGraph Studio");
            
            // 创建 MedicalAgent
            MedicalAgent medicalAgent = new MedicalAgent();
            MemorySaver saver2 = new MemorySaver();
            LangGraphStudioServer.Instance medicalAgentInstance = 
                LangGraphStudioServer.Instance.builder()
                    .title("Medical Agent - 医疗健康多智能体")
                    .addInputStringArg("userQuery")
                    .graph(medicalAgent.getGraph())
                    .compileConfig(CompileConfig.builder()
                        .checkpointSaver(saver2)
                        .build())
                    .build();
            log.info("MedicalAgent 已注册到 LangGraph Studio");
            
            // 创建 MedicalAssistantGraph (阶段2)
            MedicalAssistantGraph medicalAssistantGraph = new MedicalAssistantGraph();
            MemorySaver saver3 = new MemorySaver();
            LangGraphStudioServer.Instance medicalAssistantInstance = 
                LangGraphStudioServer.Instance.builder()

                    .title("Medical Assistant Stage2 - 意图识别与紧急分诊")
                    .addInputStringArg("userQuery")
                    .graph(medicalAssistantGraph.getGraph())

                    .compileConfig(CompileConfig.builder()
                        .checkpointSaver(saver3)
                        .build())
                    .build();
            log.info("MedicalAssistantGraph (阶段2) 已注册到 LangGraph Studio");
            
            // 创建 HumanInLoop 工作流
            MemorySaver saver4 = new MemorySaver();
            LangGraphStudioServer.Instance humanInLoopInstance = 
                LangGraphStudioServer.Instance.builder()
                    .title("Human In Loop - 人在回路演示")
                    .addInputStringArg("messages")
                    .graph(HumanInLoopDemo.buildWorkflow())
                    .compileConfig(CompileConfig.builder()
                        .checkpointSaver(saver4)
                        .interruptBefore("human_feedback")  // 在human_feedback节点前中断
                        .build())
                    .build();
            log.info("HumanInLoop 工作流已注册到 LangGraph Studio");
            
            // 创建 Medical Assistant with Human-in-Loop (免责声明确认)
            MedicalAssistantGraph medicalAssistant = new MedicalAssistantGraph();
            MemorySaver saver5 = new MemorySaver();
            LangGraphStudioServer.Instance medicalAssistantHITLInstance = 
                LangGraphStudioServer.Instance.builder()
                    .title("Medical Assistant HITL - 免责声明人在回路")
                    .addInputStringArg("userQuery")
                    .graph(medicalAssistant.getGraph())
                    .compileConfig(CompileConfig.builder()
                        .checkpointSaver(saver5)
                        .interruptBefore("user_consent")  // 在用户同意前中断
                        .build())
                    .build();
            log.info("Medical Assistant HITL 已注册到 LangGraph Studio");
            
            // 创建 Comprehensive Workflow - 综合示例(含子图)
            ComprehensiveWorkflowGraph comprehensiveWorkflow = new ComprehensiveWorkflowGraph();
            MemorySaver saver6 = new MemorySaver();
            LangGraphStudioServer.Instance comprehensiveInstance =
                LangGraphStudioServer.Instance.builder()
                    .title("Comprehensive Workflow - 综合示例(含子图)")
                    .addInputStringArg("user_input")
                    .graph(comprehensiveWorkflow.getGraph())
                    .compileConfig(CompileConfig.builder()
                        .checkpointSaver(saver6)
                        .interruptAfter("approval_request")
                        .build())
                    .build();
            log.info("Comprehensive Workflow (含子图) 已注册到 LangGraph Studio");
            
            // 返回实例映射
            return Map.of(
                "simple-agent", simpleAgentInstance,
                "medical-agent", medicalAgentInstance,
                "medical-assistant-stage2", medicalAssistantInstance,
                "human-in-loop", humanInLoopInstance,
                "medical-assistant-hitl", medicalAssistantHITLInstance,
                "comprehensive-workflow", comprehensiveInstance
            );
            
        } catch (Exception e) {
            log.error("初始化 LangGraph Studio 配置失败", e);
            throw new RuntimeException("无法初始化 Studio 配置", e);
        }
    }
}
