package org.example.langgraph4jlearn.config;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.example.langgraph4jlearn.agent.graph.SimpleAgent;
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
            
            // 创建 MemorySaver 作为 CheckpointSaver
            MemorySaver saver = new MemorySaver();
            
            // 构建 Studio Instance
            LangGraphStudioServer.Instance simpleAgentInstance = 
                LangGraphStudioServer.Instance.builder()
                    .title("Simple Agent - 简单对话Agent")
                    .addInputStringArg("message")  // 添加输入参数
                    .graph(simpleAgent.getGraph())  // 注册图
                    .compileConfig(CompileConfig.builder()
                        .checkpointSaver(saver)  // 必须配置 CheckpointSaver
                        .build())
                    .build();
            
            log.info("SimpleAgent 已注册到 LangGraph Studio");
            
            // 返回实例映射，key 是实例 ID
            return Map.of(
                "simple-agent", simpleAgentInstance
            );
            
        } catch (Exception e) {
            log.error("初始化 LangGraph Studio 配置失败", e);
            throw new RuntimeException("无法初始化 Studio 配置", e);
        }
    }
}
