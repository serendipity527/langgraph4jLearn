package org.example.langgraph4jlearn.config;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.example.langgraph4jlearn.agent.graph.SimpleAgent;
import org.example.langgraph4jlearn.agent.graph.MedicalAgent;
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
            
            // 返回实例映射
            return Map.of(
                "simple-agent", simpleAgentInstance,
                "medical-agent", medicalAgentInstance
            );
            
        } catch (Exception e) {
            log.error("初始化 LangGraph Studio 配置失败", e);
            throw new RuntimeException("无法初始化 Studio 配置", e);
        }
    }
}
