package org.example.dynamicgraph;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.example.dynamicgraph.builder.DynamicGraphBuilder;
import org.example.dynamicgraph.config.TenantGraphConfig;
import org.example.dynamicgraph.core.DynamicState;
import org.example.dynamicgraph.factory.TestConfigFactory;
import org.example.dynamicgraph.service.JsonConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON驱动的动态图测试
 * 验证真正的JSON配置解析和图构建
 */
@DisplayName("JSON驱动动态图测试")
class JsonDrivenGraphTest {
    
    private DynamicGraphBuilder graphBuilder;
    private JsonConfigService jsonConfigService;
    
    @BeforeEach
    void setUp() {
        graphBuilder = new DynamicGraphBuilder();
        jsonConfigService = new JsonConfigService();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔥 JSON驱动图构建测试");
        System.out.println("=".repeat(60));
    }
    
    @Test
    @DisplayName("JSON字符串解析测试 - 租户A")
    void testJsonStringParsing_TenantA() throws IOException, GraphStateException {
        // 1. 从JSON字符串解析配置
        TenantGraphConfig config = TestConfigFactory.createTenantAFromJson();
        
        System.out.println("📋 解析的JSON配置:");
        System.out.println(jsonConfigService.toJson(config));
        
        // 2. 验证配置解析正确
        assertEquals("tenant_A_JSON", config.getTenantId());
        assertEquals("JSON驱动的简单流程", config.getGraphName());
        assertEquals(2, config.getNodes().size());
        assertEquals(3, config.getEdges().size());
        
        // 3. 构建并执行图
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());


        Map<String, Object> input = Map.of(DynamicState.DATA_KEY, "json-test");
        DynamicState result = getLastState(graph, input);
        
        // 4. 验证结果
        assertEquals("JSON-TEST", result.getDataAsString());
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.toString().contains("[租户A-JSON]")));
    }
    
    @Test
    @DisplayName("JSON字符串解析测试 - 租户B")
    void testJsonStringParsing_TenantB() throws IOException, GraphStateException {
        // 1. 从JSON字符串解析配置
        TenantGraphConfig config = TestConfigFactory.createTenantBFromJson();
        
        System.out.println("📋 解析的JSON配置:");
        System.out.println(jsonConfigService.toJson(config));
        
        // 2. 验证配置解析正确
        assertEquals("tenant_B_JSON", config.getTenantId());
        assertEquals("JSON驱动的文本处理流程", config.getGraphName());
        assertEquals(3, config.getNodes().size());
        assertEquals(4, config.getEdges().size());
        
        // 3. 构建并执行图
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());


        Map<String, Object> input = Map.of(DynamicState.DATA_KEY, "json");
        DynamicState result = getLastState(graph, input);
        
        // 4. 验证结果
        assertEquals("JSON-JSON-SUFFIX", result.getDataAsString());
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.contains("[租户B-JSON]")));
    }
    
    @Test
    @DisplayName("JSON配置往返转换测试")
    void testJsonRoundTrip() throws IOException {
        // 1. 创建原始配置
        TenantGraphConfig original = TestConfigFactory.createTenantAFromJson();
        
        // 2. 转换为JSON字符串
        String json = jsonConfigService.toJson(original);
        System.out.println("🔄 JSON字符串:");
        System.out.println(json);
        
        // 3. 从JSON字符串重新解析
        TenantGraphConfig parsed = jsonConfigService.parseFromJson(json);
        
        // 4. 验证往返转换后数据一致
        assertEquals(original.getTenantId(), parsed.getTenantId());
        assertEquals(original.getGraphName(), parsed.getGraphName());
        assertEquals(original.getNodes().size(), parsed.getNodes().size());
        assertEquals(original.getEdges().size(), parsed.getEdges().size());
        
        // 验证第一个节点的详细信息
        assertEquals(original.getNodes().get(0).getId(), parsed.getNodes().get(0).getId());
        assertEquals(original.getNodes().get(0).getType(), parsed.getNodes().get(0).getType());
    }
    
    @Test
    @DisplayName("JSON vs 硬编码配置对比测试")
    void testJsonVsHardcodedConfig() throws IOException, GraphStateException {
        System.out.println("🆚 JSON配置 vs 硬编码配置对比");
        
        // 1. JSON配置
        TenantGraphConfig jsonConfig = TestConfigFactory.createTenantAFromJson();
        CompiledGraph<DynamicState> jsonGraph = graphBuilder.buildGraph(jsonConfig);
        
        // 2. 硬编码配置
        TenantGraphConfig hardcodedConfig = TestConfigFactory.createTenantAConfig();
        CompiledGraph<DynamicState> hardcodedGraph = graphBuilder.buildGraph(hardcodedConfig);
        
        // 3. 使用相同输入测试
        String testInput = "compare";
        Map<String, Object> input = Map.of(DynamicState.DATA_KEY, testInput);
        
        DynamicState jsonResult = getLastState(jsonGraph, input);
        DynamicState hardcodedResult = getLastState(hardcodedGraph, input);
        
        // 4. 验证两种配置方式都能正常工作
        assertEquals("COMPARE", jsonResult.getDataAsString());
        assertEquals("COMPARE", hardcodedResult.getDataAsString());
        
        System.out.println("✅ JSON配置结果: " + jsonResult.getDataAsString());
        System.out.println("✅ 硬编码配置结果: " + hardcodedResult.getDataAsString());
        System.out.println("🎉 两种配置方式结果一致！");
    }
    
    @Test
    @DisplayName("复杂JSON配置解析测试")
    void testComplexJsonConfig() throws IOException, GraphStateException {
        // 复杂的JSON配置字符串
        String complexJson = """
            {
              "tenantId": "complex_tenant",
              "graphName": "复杂JSON驱动流程",
              "nodes": [
                {
                  "id": "step1",
                  "type": "appendNode",
                  "params": {
                    "suffix": "-step1"
                  }
                },
                {
                  "id": "step2",
                  "type": "appendNode",
                  "params": {
                    "suffix": "-step2"
                  }
                },
                {
                  "id": "final",
                  "type": "upperCaseNode",
                  "params": {}
                },
                {
                  "id": "log",
                  "type": "logNode",
                  "params": {
                    "prefix": "[复杂流程]"
                  }
                }
              ],
              "edges": [
                {"from": "START", "to": "step1"},
                {"from": "step1", "to": "step2"},
                {"from": "step2", "to": "final"},
                {"from": "final", "to": "log"},
                {"from": "log", "to": "END"}
              ]
            }
            """;
        
        // 解析并执行
        TenantGraphConfig config = jsonConfigService.parseFromJson(complexJson);
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());

        Map<String, Object> input = Map.of(DynamicState.DATA_KEY, "complex");
        DynamicState result = getLastState(graph, input);
        
        // 验证复杂流程执行结果
        assertEquals("COMPLEX-STEP1-STEP2", result.getDataAsString());
        System.out.println("🏗️ 复杂流程执行结果: " + result.getDataAsString());
    }
    
    /**
     * 辅助方法：获取图执行的最终状态
     */
    private DynamicState getLastState(CompiledGraph<DynamicState> graph, Map<String, Object> input) {
        DynamicState lastState = null;
        for (NodeOutput<DynamicState> output : graph.stream(input)) {
            lastState = output.state();
        }
        return lastState;
    }
}
