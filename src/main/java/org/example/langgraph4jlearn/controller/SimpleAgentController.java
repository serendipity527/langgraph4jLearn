package org.example.langgraph4jlearn.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.langgraph4jlearn.agent.graph.SimpleAgent;
import org.example.langgraph4jlearn.agent.state.SimpleAgentState;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * SimpleAgent控制器
 * 提供REST API来测试SimpleAgent
 */
@Slf4j
@RestController
@RequestMapping("/api/simple-agent")
public class SimpleAgentController {
    
    private final SimpleAgent simpleAgent;
    
    public SimpleAgentController() {
        this.simpleAgent = new SimpleAgent();
    }
    
    /**
     * 执行SimpleAgent
     * 
     * POST /api/simple-agent/execute
     * Body: {"message": "你好"}
     */
    @PostMapping("/execute")
    public Map<String, Object> execute(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "默认消息");
        
        log.info("收到执行请求，消息: {}", message);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            SimpleAgentState finalState = simpleAgent.executeAndGetState(message);
            
            response.put("success", true);
            response.put("messages", finalState.messages());
            response.put("messageCount", finalState.messages().size());
            
        } catch (Exception e) {
            log.error("执行SimpleAgent失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 健康检查
     * 
     * GET /api/simple-agent/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("agent", "SimpleAgent");
        response.put("description", "一个最简单的LangGraph4j Agent示例");
        return response;
    }
}
