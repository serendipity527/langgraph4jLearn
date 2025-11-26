package org.example.dynamicgraph;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.NodeOutput;
import org.example.dynamicgraph.intent.builder.IntentGraphBuilder;
import org.example.dynamicgraph.intent.state.IntentState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 动态图构建测试 - 通过JSON配置 + Spring Bean动态构建图
 */
@SpringBootTest
@DisplayName("动态意图图构建测试")
class IntentDynamicGraphTest {

    @Autowired
    private IntentGraphBuilder graphBuilder;

    private static final String GRAPH_CONFIG_JSON = """
        {
          "meta": {
            "graphId": "intent_flow_001",
            "description": "基于意图识别的分流逻辑"
          },
          "settings": {
            "entryPoint": "intentRecognition"
          },
          "nodes": [
            {
              "id": "intentRecognition",
              "component": "IntentRecognitionService"
            },
            {
              "id": "welcomeScene",
              "component": "WelcomeService"
            },
            {
              "id": "chatScene",
              "component": "ChatService"
            },
            {
              "id": "followUpScene",
              "component": "FollowUpService"
            }
          ],
          "edges": [
            { "from": "welcomeScene", "to": "END" },
            { "from": "chatScene", "to": "END" },
            { "from": "followUpScene", "to": "END" }
          ],
          "conditionalEdges": [
            {
              "from": "intentRecognition",
              "router": "CheckStateRouter",
              "paths": {
                "CHAT": "chatScene",
                "WELCOME": "welcomeScene",
                "FOLLOWUP": "followUpScene"
              }
            }
          ]
        }
        """;

    @Test
    @DisplayName("通过JSON配置动态构建图")
    void testBuildGraphFromJson() throws Exception {
        // 从JSON构建图
        CompiledGraph<IntentState> graph = graphBuilder.buildFromJson(GRAPH_CONFIG_JSON);

        // 打印Mermaid图
        System.out.println("\n📊 Mermaid 流程图:");
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).content());

        // 测试不同意图
        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试1: WELCOME场景");
        System.out.println("=".repeat(60));
        testWithInput(graph, "你好，我是新用户");

        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试2: CHAT对话");
        System.out.println("=".repeat(60));
        testWithInput(graph, "今天天气怎么样？");

        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试3: FollowUP场景");
        System.out.println("=".repeat(60));
        testWithInput(graph, "请继续帮我跟进上次的问题");
    }

    private void testWithInput(CompiledGraph<IntentState> graph, String input) {
        System.out.println("📥 输入: " + input);

        Map<String, Object> inputMap = Map.of(IntentState.INPUT_KEY, input);

        IntentState lastState = null;
        for (NodeOutput<IntentState> output : graph.stream(inputMap)) {
            lastState = output.state();
            System.out.println("  节点: " + output.node());
        }

        assertNotNull(lastState);
        System.out.println("📤 输出: " + lastState.getOutput());
        System.out.println("📝 消息: " + lastState.getMessages());
        assertFalse(lastState.getOutput().isEmpty(), "应该有输出结果");
    }
}
