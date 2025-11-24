package org.example.langgraph4jlearn.agent.graph;

import lombok.extern.slf4j.Slf4j;
import org.example.langgraph4jlearn.agent.state.SimpleAgentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleAgent测试类
 */
@Slf4j
class SimpleAgentTest {
    
    private SimpleAgent simpleAgent;
    
    @BeforeEach
    void setUp() {
        simpleAgent = new SimpleAgent();
    }
    
    @Test
    void testExecute() throws Exception {
        log.info("开始测试SimpleAgent.execute()");
        
        String initialMessage = "开始测试！";
        simpleAgent.execute(initialMessage);
        
        log.info("SimpleAgent.execute()测试完成");
    }
    
    @Test
    void testExecuteAndGetState() throws Exception {
        log.info("开始测试SimpleAgent.executeAndGetState()");
        
        String initialMessage = "你好世界！";
        SimpleAgentState finalState = simpleAgent.executeAndGetState(initialMessage);
        
        assertNotNull(finalState, "最终状态不应为空");
        assertNotNull(finalState.messages(), "消息列表不应为空");
        assertFalse(finalState.messages().isEmpty(), "消息列表不应为空列表");
        
        log.info("最终状态消息数量: {}", finalState.messages().size());
        log.info("所有消息: {}", finalState.messages());
        
        // 验证消息内容
        assertTrue(finalState.messages().stream()
                .anyMatch(msg -> msg.contains("欢迎")), 
                "应包含欢迎消息");
        assertTrue(finalState.messages().stream()
                .anyMatch(msg -> msg.contains("准备开始工作")), 
                "应包含响应消息");
        
        log.info("SimpleAgent.executeAndGetState()测试完成");
    }
}
