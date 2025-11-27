package org.example.dynamicgraph.designv1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.example.dynamicgraph.designv1.graph.DynamicGraphBuilder;
import org.example.dynamicgraph.designv1.graph.DynamicGraphBuilder.DynamicAgentState;
import org.example.dynamicgraph.designv1.node.NodeRegistry;
import org.example.dynamicgraph.designv1.node.impl.IntentRecognitionNode;
import org.example.dynamicgraph.designv1.node.impl.LogNode;
import org.example.dynamicgraph.designv1.node.impl.ResponseNode;
import org.example.dynamicgraph.designv1.node.impl.SetValueNode;
import org.example.dynamicgraph.designv1.node.impl.TransformNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 动态图功能测试
 * 
 * 测试各种图结构：线性图、条件分支图、多节点图、自定义状态等
 */
class DynamicGraphTest {

    private DynamicGraphBuilder builder;

    @BeforeEach
    void setUp() {
        // 创建节点注册表，预注册带参数的节点
        NodeRegistry nodeRegistry = new NodeRegistry();
        
        // 意图识别节点（无参数）
        nodeRegistry.register("intentRecognition", new IntentRecognitionNode());
        
        // 各种响应节点（预设不同模板）
        nodeRegistry.register("welcomeReply", new ResponseNode("欢迎您！"));
        nodeRegistry.register("weatherReply", new ResponseNode("今天晴天"));
        nodeRegistry.register("defaultReply", new ResponseNode("我不太明白"));
        nodeRegistry.register("orderHandler", new ResponseNode("正在处理您的订单"));
        nodeRegistry.register("weatherHandler", new ResponseNode("正在查询天气"));
        nodeRegistry.register("welcomeHandler", new ResponseNode("您好！很高兴为您服务"));
        nodeRegistry.register("chatHandler", new ResponseNode("让我们聊聊吧"));
        nodeRegistry.register("greetA", new ResponseNode("您好，欢迎使用租户A的服务！", "message"));
        nodeRegistry.register("respondB", new ResponseNode("租户B验证通过", "message"));
        nodeRegistry.register("processComplete", new ResponseNode("处理完成", "status"));
        
        // 设置值节点（预设不同 key/value）
        nodeRegistry.register("setResult", new SetValueNode("result", "第一步完成"));
        nodeRegistry.register("setData", new SetValueNode("data", "hello world"));
        nodeRegistry.register("setStatus", new SetValueNode("status", "completed"));
        nodeRegistry.register("setCurrent1", new SetValueNode("current", "step1"));
        nodeRegistry.register("setCurrent2", new SetValueNode("current", "step2"));
        nodeRegistry.register("setCurrent3", new SetValueNode("current", "step3"));
        nodeRegistry.register("setVerified", new SetValueNode("verified", "true"));
        nodeRegistry.register("setDone", new SetValueNode("result", "done"));
        
        // 转换节点（预设不同操作）
        nodeRegistry.register("toUpper", new TransformNode("result", "result", "upper"));
        nodeRegistry.register("dataToUpper", new TransformNode("data", "data", "upper"));
        nodeRegistry.register("reverse", new TransformNode("data", "data", "reverse"));
        nodeRegistry.register("processInput", new TransformNode("userInput", "processed", "upper"));
        
        // 日志节点（用于测试 appender 模式）
        nodeRegistry.register("log1", new LogNode("步骤1完成"));
        nodeRegistry.register("log2", new LogNode("步骤2完成"));
        nodeRegistry.register("log3", new LogNode("步骤3完成"));

        builder = new DynamicGraphBuilder(nodeRegistry, new ObjectMapper());
    }

    // ==================== 1. 基础线性图 ====================

    @Test
    @DisplayName("线性图：A -> B -> END")
    void testLinearGraph() throws Exception {
        String json = """
            {
              "id": "linear-graph",
              "name": "Linear Graph",
              "entryNode": "step1",
              "nodes": [
                { "id": "step1", "componentType": "setResult" },
                { "id": "step2", "componentType": "toUpper" }
              ],
              "edges": [
                { "from": "step1", "to": "step2" },
                { "from": "step2", "to": "__end__" }
              ]
            }
            """;

        CompiledGraph<DynamicAgentState> graph = builder.buildFromJson(json);
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());
        Optional<DynamicAgentState> result = graph.invoke(Map.of());

        assertTrue(result.isPresent());
        assertEquals("第一步完成".toUpperCase(), result.get().getString("result"));
        System.out.println("✅ 线性图测试通过: " + result.get().getString("result"));
    }

    // ==================== 2. 条件分支图 ====================

    @Test
    @DisplayName("条件分支图：根据意图路由到不同节点")
    void testConditionalBranchGraph() throws Exception {
        String json = """
            {
              "id": "conditional-graph",
              "name": "Conditional Branch Graph",
              "entryNode": "recognize",
              "nodes": [
                { "id": "recognize", "componentType": "intentRecognition" },
                { "id": "welcomeReply", "componentType": "welcomeReply" },
                { "id": "weatherReply", "componentType": "weatherReply" },
                { "id": "defaultReply", "componentType": "defaultReply" }
              ],
              "edges": [
                { "from": "welcomeReply", "to": "__end__" },
                { "from": "weatherReply", "to": "__end__" },
                { "from": "defaultReply", "to": "__end__" }
              ],
              "conditionalEdges": [
                {
                  "from": "recognize",
                  "conditionKey": "intent",
                  "routes": { "WELCOME": "welcomeReply", "WEATHER": "weatherReply" },
                  "defaultTarget": "defaultReply"
                }
              ]
            }
            """;

        CompiledGraph<DynamicAgentState> graph = builder.buildFromJson(json);

        // 测试欢迎意图
        Optional<DynamicAgentState> r1 = graph.invoke(Map.of("input", "你好"));
        assertEquals("WELCOME", r1.get().getString("intent"));
        assertEquals("欢迎您！", r1.get().getString("response"));
        System.out.println("✅ 欢迎分支: " + r1.get().getString("response"));

        // 测试天气意图
        Optional<DynamicAgentState> r2 = graph.invoke(Map.of("input", "天气怎么样"));
        assertEquals("WEATHER", r2.get().getString("intent"));
        assertEquals("今天晴天", r2.get().getString("response"));
        System.out.println("✅ 天气分支: " + r2.get().getString("response"));

        // 测试默认路由
        Optional<DynamicAgentState> r3 = graph.invoke(Map.of("input", "随便说点啥"));
        assertEquals("CHAT", r3.get().getString("intent"));
        assertEquals("我不太明白", r3.get().getString("response"));
        System.out.println("✅ 默认分支: " + r3.get().getString("response"));
    }

    // ==================== 3. 多步骤处理链 ====================

    @Test
    @DisplayName("多步骤处理链：A -> B -> C -> D -> END")
    void testMultiStepChain() throws Exception {
        String json = """
            {
              "id": "chain-graph",
              "name": "Multi-Step Chain",
              "entryNode": "init",
              "nodes": [
                { "id": "init", "componentType": "setData" },
                { "id": "toUpper", "componentType": "dataToUpper" },
                { "id": "reverse", "componentType": "reverse" },
                { "id": "final", "componentType": "setStatus" }
              ],
              "edges": [
                { "from": "init", "to": "toUpper" },
                { "from": "toUpper", "to": "reverse" },
                { "from": "reverse", "to": "final" },
                { "from": "final", "to": "__end__" }
              ]
            }
            """;

        CompiledGraph<DynamicAgentState> graph = builder.buildFromJson(json);
        Optional<DynamicAgentState> result = graph.invoke(Map.of());

        assertTrue(result.isPresent());
        // "hello world" -> upper -> "HELLO WORLD" -> reverse -> "DLROW OLLEH"
        assertEquals("DLROW OLLEH", result.get().getString("data"));
        assertEquals("completed", result.get().getString("status"));
        System.out.println("✅ 多步骤链测试通过: data=" + result.get().getString("data"));
    }

    // ==================== 4. 自定义状态 Schema ====================

    @Test
    @DisplayName("自定义状态 Schema：appender 模式测试")
    void testCustomStateSchema() throws Exception {
        String json = """
            {
              "id": "schema-graph",
              "name": "Custom Schema Graph",
              "entryNode": "step1",
              "stateSchema": {
                "logs": { "type": "appender" },
                "current": { "type": "value" }
              },
              "nodes": [
                { "id": "step1", "componentType": "setCurrent1" },
                { "id": "step2", "componentType": "setCurrent2" },
                { "id": "step3", "componentType": "setCurrent3" }
              ],
              "edges": [
                { "from": "step1", "to": "step2" },
                { "from": "step2", "to": "step3" },
                { "from": "step3", "to": "__end__" }
              ]
            }
            """;

        CompiledGraph<DynamicAgentState> graph = builder.buildFromJson(json);
        Optional<DynamicAgentState> result = graph.invoke(Map.of());

        assertTrue(result.isPresent());
        // current 是 value 类型，只保留最后的值
        assertEquals("step3", result.get().getString("current"));
        System.out.println("✅ 自定义 Schema 测试通过: current=" + result.get().getString("current"));
    }

    // ==================== 5. 复杂条件路由 ====================

    @Test
    @DisplayName("复杂条件路由：多个条件值")
    void testComplexConditionalRouting() throws Exception {
        String json = """
            {
              "id": "complex-routing",
              "name": "Complex Routing Graph",
              "entryNode": "classify",
              "nodes": [
                { "id": "classify", "componentType": "intentRecognition" },
                { "id": "orderHandler", "componentType": "orderHandler" },
                { "id": "weatherHandler", "componentType": "weatherHandler" },
                { "id": "welcomeHandler", "componentType": "welcomeHandler" },
                { "id": "chatHandler", "componentType": "chatHandler" }
              ],
              "edges": [
                { "from": "orderHandler", "to": "__end__" },
                { "from": "weatherHandler", "to": "__end__" },
                { "from": "welcomeHandler", "to": "__end__" },
                { "from": "chatHandler", "to": "__end__" }
              ],
              "conditionalEdges": [
                {
                  "from": "classify",
                  "conditionKey": "intent",
                  "routes": {
                    "ORDER": "orderHandler",
                    "WEATHER": "weatherHandler",
                    "WELCOME": "welcomeHandler",
                    "CHAT": "chatHandler"
                  }
                }
              ]
            }
            """;

        CompiledGraph<DynamicAgentState> graph = builder.buildFromJson(json);

        // 订单意图
        Optional<DynamicAgentState> r1 = graph.invoke(Map.of("input", "我要查订单"));
        assertEquals("ORDER", r1.get().getString("intent"));
        assertEquals("正在处理您的订单", r1.get().getString("response"));
        System.out.println("✅ 订单路由: " + r1.get().getString("response"));

        // 聊天意图
        Optional<DynamicAgentState> r2 = graph.invoke(Map.of("input", "今天心情不错"));
        assertEquals("CHAT", r2.get().getString("intent"));
        assertEquals("让我们聊聊吧", r2.get().getString("response"));
        System.out.println("✅ 聊天路由: " + r2.get().getString("response"));
    }

    // ==================== 6. 带初始输入的图 ====================

    @Test
    @DisplayName("带初始输入的图：处理用户输入")
    void testGraphWithInput() throws Exception {
        String json = """
            {
              "id": "input-graph",
              "name": "Input Processing Graph",
              "entryNode": "process",
              "nodes": [
                { "id": "process", "componentType": "processInput" },
                { "id": "respond", "componentType": "processComplete" }
              ],
              "edges": [
                { "from": "process", "to": "respond" },
                { "from": "respond", "to": "__end__" }
              ]
            }
            """;

        CompiledGraph<DynamicAgentState> graph = builder.buildFromJson(json);
        
        // 传入用户输入
        Optional<DynamicAgentState> result = graph.invoke(Map.of("userInput", "test message"));

        assertTrue(result.isPresent());
        assertEquals("TEST MESSAGE", result.get().getString("processed"));
        assertEquals("处理完成", result.get().getString("status"));
        System.out.println("✅ 输入处理测试通过: processed=" + result.get().getString("processed"));
    }

    // ==================== 7. 单节点图 ====================

    @Test
    @DisplayName("单节点图：最简单的图结构")
    void testSingleNodeGraph() throws Exception {
        String json = """
            {
              "id": "single-node",
              "name": "Single Node Graph",
              "entryNode": "only",
              "nodes": [
                { "id": "only", "componentType": "setDone" }
              ],
              "edges": [
                { "from": "only", "to": "__end__" }
              ]
            }
            """;

        CompiledGraph<DynamicAgentState> graph = builder.buildFromJson(json);
        Optional<DynamicAgentState> result = graph.invoke(Map.of());

        assertTrue(result.isPresent());
        assertEquals("done", result.get().getString("result"));
        System.out.println("✅ 单节点图测试通过");
    }

    // ==================== 8. 多租户模拟 ====================

    @Test
    @DisplayName("多租户模拟：不同租户使用不同图配置")
    void testMultiTenantSimulation() throws Exception {
        // 租户 A 的图配置
        String tenantAJson = """
            {
              "id": "tenant-a-graph",
              "name": "Tenant A Graph",
              "entryNode": "greet",
              "nodes": [
                { "id": "greet", "componentType": "greetA" }
              ],
              "edges": [
                { "from": "greet", "to": "__end__" }
              ]
            }
            """;

        // 租户 B 的图配置（不同流程）
        String tenantBJson = """
            {
              "id": "tenant-b-graph",
              "name": "Tenant B Graph",
              "entryNode": "check",
              "nodes": [
                { "id": "check", "componentType": "setVerified" },
                { "id": "respond", "componentType": "respondB" }
              ],
              "edges": [
                { "from": "check", "to": "respond" },
                { "from": "respond", "to": "__end__" }
              ]
            }
            """;

        // 构建两个租户的图
        CompiledGraph<DynamicAgentState> graphA = builder.buildFromJson(tenantAJson);
        CompiledGraph<DynamicAgentState> graphB = builder.buildFromJson(tenantBJson);

        // 执行租户 A
        Optional<DynamicAgentState> resultA = graphA.invoke(Map.of());
        assertEquals("您好，欢迎使用租户A的服务！", resultA.get().getString("message"));
        System.out.println("✅ 租户A: " + resultA.get().getString("message"));

        // 执行租户 B
        Optional<DynamicAgentState> resultB = graphB.invoke(Map.of());
        assertEquals("true", resultB.get().getString("verified"));
        assertEquals("租户B验证通过", resultB.get().getString("message"));
        System.out.println("✅ 租户B: " + resultB.get().getString("message"));
    }

    // ==================== 9. Schema 配置测试：appender vs value ====================

    @Test
    @DisplayName("Schema appender 模式：多节点输出追加到同一字段")
    void testSchemaAppenderMode() throws Exception {
        // 配置 logs 为 appender 模式，每个节点的输出会追加而非覆盖
        String json = """
            {
              "id": "appender-test",
              "name": "Appender Mode Test",
              "entryNode": "step1",
              "stateSchema": {
                "logs": { "type": "appender" }
              },
              "nodes": [
                { "id": "step1", "componentType": "log1" },
                { "id": "step2", "componentType": "log2" },
                { "id": "step3", "componentType": "log3" }
              ],
              "edges": [
                { "from": "step1", "to": "step2" },
                { "from": "step2", "to": "step3" },
                { "from": "step3", "to": "__end__" }
              ]
            }
            """;

        CompiledGraph<DynamicAgentState> graph = builder.buildFromJson(json);
        Optional<DynamicAgentState> result = graph.invoke(Map.of());

        assertTrue(result.isPresent());
        
        // 因为 logs 是 appender 模式，三个节点的输出会被追加
        @SuppressWarnings("unchecked")
        List<String> logs = result.get().<List<String>>value("logs").orElse(List.of());
        
        System.out.println("✅ logs 内容: " + logs);
        assertEquals(3, logs.size());
        assertTrue(logs.contains("步骤1完成"));
        assertTrue(logs.contains("步骤2完成"));
        assertTrue(logs.contains("步骤3完成"));
    }

    @Test
    @DisplayName("Schema value 模式（默认）：后面的输出覆盖前面的")
    void testSchemaValueMode() throws Exception {
        // 不配置 stateSchema，或配置为 value 模式，后面的输出会覆盖前面的
        String json = """
            {
              "id": "value-test",
              "name": "Value Mode Test",
              "entryNode": "step1",
              "nodes": [
                { "id": "step1", "componentType": "log1" },
                { "id": "step2", "componentType": "log2" },
                { "id": "step3", "componentType": "log3" }
              ],
              "edges": [
                { "from": "step1", "to": "step2" },
                { "from": "step2", "to": "step3" },
                { "from": "step3", "to": "__end__" }
              ]
            }
            """;

        CompiledGraph<DynamicAgentState> graph = builder.buildFromJson(json);
        Optional<DynamicAgentState> result = graph.invoke(Map.of());

        assertTrue(result.isPresent());
        
        // 因为 logs 没有配置为 appender，每次都是覆盖，最终只有最后一个
        @SuppressWarnings("unchecked")
        List<String> logs = result.get().<List<String>>value("logs").orElse(List.of());
        
        System.out.println("✅ logs 内容 (value 模式): " + logs);
        // value 模式下，只保留最后一个节点的输出
        assertEquals(1, logs.size());
        assertEquals("步骤3完成", logs.get(0));
    }
}
