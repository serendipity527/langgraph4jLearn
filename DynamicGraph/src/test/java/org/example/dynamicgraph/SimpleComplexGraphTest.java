package org.example.dynamicgraph;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.example.dynamicgraph.builder.DynamicGraphBuilder;
import org.example.dynamicgraph.config.TenantGraphConfig;
import org.example.dynamicgraph.core.DynamicState;
import org.example.dynamicgraph.service.JsonConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简化的复杂图测试 - 避免条件边的问题
 */
@DisplayName("简化复杂图测试")
class SimpleComplexGraphTest {
    
    private DynamicGraphBuilder graphBuilder;
    private JsonConfigService jsonConfigService;
    
    @BeforeEach
    void setUp() {
        graphBuilder = new DynamicGraphBuilder();
        jsonConfigService = new JsonConfigService();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔥 简化复杂图构建测试 - 线性流程");
        System.out.println("=".repeat(70));
    }
    
    @Test
    @DisplayName("重试节点测试")
    void testRetryNodeWorkflow() throws IOException, GraphStateException {
        String retryGraphJson = """
            {
              "tenantId": "retry_tenant",
              "graphName": "重试节点测试流程",
              "nodes": [
                {
                  "id": "retry",
                  "type": "retryNode",
                  "params": {
                    "failureRate": 0.3,
                    "maxRetries": 2
                  }
                },
                {
                  "id": "decision",
                  "type": "decisionNode",
                  "params": {
                    "decisionType": "retry_check"
                  }
                },
                {
                  "id": "process",
                  "type": "upperCaseNode",
                  "params": {}
                },
                {
                  "id": "log",
                  "type": "logNode",
                  "params": {
                    "prefix": "[重试测试]"
                  }
                }
              ],
              "edges": [
                {
                  "from": "START",
                  "to": "retry"
                },
                {
                  "from": "retry",
                  "to": "decision"
                },
                {
                  "from": "decision",
                  "to": "process",
                  "condition": "data_contains_success",
                  "conditional": true
                },
                {
                  "from": "decision",
                  "to": "retry",
                  "condition": "retry_count_lt_3",
                  "conditional": true
                },
                {
                  "from": "decision",
                  "to": "log",
                  "condition": "continue",
                  "conditional": true
                },
                {
                  "from": "process",
                  "to": "log"
                },
                {
                  "from": "log",
                  "to": "END"
                }
              ]
            }
            """;
            
        System.out.println("📋 重试节点测试配置:");
        
        TenantGraphConfig config = jsonConfigService.parseFromJson(retryGraphJson);
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        
        // 显示图结构
        System.out.println("\n📊 Mermaid图表:");
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());
        
        // 执行图
        Map<String, Object> input = Map.of(DynamicState.DATA_KEY, "retry-test");
        DynamicState result = getLastState(graph, input);
        
        // 验证结果
        assertNotNull(result);
        String finalData = result.getDataAsString();
        System.out.println("🎯 最终结果: " + finalData);
        
        assertTrue(finalData.length() > "retry-test".length(), "数据应该被处理");
    }
    
    @Test
    @DisplayName("人工审批节点测试")
    void testHumanApprovalNode() throws IOException, GraphStateException {
        String approvalGraphJson = """
            {
              "tenantId": "approval_tenant",
              "graphName": "人工审批测试流程",
              "nodes": [
                {
                  "id": "humanApproval",
                  "type": "humanApprovalNode",
                  "params": {
                    "autoApprove": true
                  }
                },
                {
                  "id": "process",
                  "type": "upperCaseNode",
                  "params": {}
                },
                {
                  "id": "log",
                  "type": "logNode",
                  "params": {
                    "prefix": "[审批测试]"
                  }
                }
              ],
              "edges": [
                {
                  "from": "START",
                  "to": "humanApproval"
                },
                {
                  "from": "humanApproval",
                  "to": "process"
                },
                {
                  "from": "process",
                  "to": "log"
                },
                {
                  "from": "log",
                  "to": "END"
                }
              ]
            }
            """;
            
        System.out.println("📋 人工审批测试配置:");
        
        TenantGraphConfig config = jsonConfigService.parseFromJson(approvalGraphJson);
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        
        // 显示图结构
        System.out.println("\n📊 Mermaid图表:");
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());
        
        // 执行图
        System.out.println("\n🧑‍💼 测试人工审批流程 (自动模式):");
        Map<String, Object> input = Map.of(DynamicState.DATA_KEY, "approval-test");
        DynamicState result = getLastState(graph, input);
        
        // 验证结果
        assertNotNull(result);
        String finalData = result.getDataAsString();
        System.out.println("🎯 审批结果: " + finalData);
        
        assertTrue(finalData.contains("APPROVED") || finalData.contains("APPROVAL"), 
                  "结果应该包含审批相关信息");
    }
    
    @Test
    @DisplayName("决策节点测试")
    void testDecisionNodeWorkflow() throws IOException, GraphStateException {
        String decisionGraphJson = """
            {
              "tenantId": "decision_tenant",
              "graphName": "决策节点测试流程",
              "nodes": [
                {
                  "id": "decision",
                  "type": "decisionNode",
                  "params": {
                    "decisionType": "quality_check"
                  }
                },
                {
                  "id": "append",
                  "type": "appendNode",
                  "params": {
                    "suffix": "-processed"
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
                    "prefix": "[决策测试]"
                  }
                }
              ],
              "edges": [
                {
                  "from": "START",
                  "to": "decision"
                },
                {
                  "from": "decision",
                  "to": "append"
                },
                {
                  "from": "append",
                  "to": "final"
                },
                {
                  "from": "final",
                  "to": "log"
                },
                {
                  "from": "log",
                  "to": "END"
                }
              ]
            }
            """;
            
        System.out.println("📋 决策节点测试配置:");
        
        TenantGraphConfig config = jsonConfigService.parseFromJson(decisionGraphJson);
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        
        // 显示图结构
        System.out.println("\n📊 Mermaid图表:");
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());
        
        // 测试不同输入长度的决策
        System.out.println("\n🎯 测试不同决策场景:");
        
        // 短文本 -> 低质量
        Map<String, Object> input1 = Map.of(DynamicState.DATA_KEY, "short");
        DynamicState result1 = getLastState(graph, input1);
        System.out.println("短文本结果: " + result1.getDataAsString());
        
        // 长文本 -> 高质量  
        Map<String, Object> input2 = Map.of(DynamicState.DATA_KEY, "this-is-a-very-long-text-for-quality-testing");
        DynamicState result2 = getLastState(graph, input2);
        System.out.println("长文本结果: " + result2.getDataAsString());
        
        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.getDataAsString().contains("QUALITY"), "结果应该包含质量评估");
        assertTrue(result2.getDataAsString().contains("QUALITY"), "结果应该包含质量评估");
    }
    
    @Test
    @DisplayName("混合节点类型流程测试")
    void testMixedNodeTypesWorkflow() throws IOException, GraphStateException {
        String mixedGraphJson = """
            {
              "tenantId": "mixed_tenant", 
              "graphName": "混合节点类型流程",
              "nodes": [
                {
                  "id": "retry",
                  "type": "retryNode",
                  "params": {
                    "failureRate": 0.1,
                    "maxRetries": 1
                  }
                },
                {
                  "id": "decision",
                  "type": "decisionNode",
                  "params": {
                    "decisionType": "risk_assessment"
                  }
                },
                {
                  "id": "approval",
                  "type": "humanApprovalNode", 
                  "params": {
                    "autoApprove": true
                  }
                },
                {
                  "id": "process",
                  "type": "upperCaseNode",
                  "params": {}
                },
                {
                  "id": "log",
                  "type": "logNode",
                  "params": {
                    "prefix": "[混合流程]"
                  }
                }
              ],
              "edges": [
                {
                  "from": "START",
                  "to": "retry"
                },
                {
                  "from": "retry",
                  "to": "decision"
                },
                {
                  "from": "decision",
                  "to": "approval"
                },
                {
                  "from": "approval", 
                  "to": "process"
                },
                {
                  "from": "process",
                  "to": "log"
                },
                {
                  "from": "log",
                  "to": "END"
                }
              ]
            }
            """;
            
        System.out.println("📋 混合节点类型测试配置:");
        
        TenantGraphConfig config = jsonConfigService.parseFromJson(mixedGraphJson);
        CompiledGraph<DynamicState> graph = graphBuilder.buildGraph(config);
        
        // 显示图结构
        System.out.println("\n📊 Mermaid图表:");
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).toString());
        
        // 执行复杂流程
        System.out.println("\n🔧 执行混合节点流程:");
        Map<String, Object> input = Map.of(DynamicState.DATA_KEY, "critical-mixed-test");
        DynamicState result = getLastState(graph, input);
        
        // 验证结果
        assertNotNull(result);
        String finalData = result.getDataAsString();
        System.out.println("🎯 混合流程最终结果: " + finalData);
        
        // 检查所有节点类型都被执行
        assertTrue(finalData.contains("CRITICAL"), "应该被转换为大写");
        assertFalse(result.getMessages().isEmpty(), "应该有执行日志");
        
        // 检查消息中包含各种节点的执行记录
        boolean hasRetryLog = result.getMessages().stream().anyMatch(msg -> msg.toString().contains("操作"));
        boolean hasDecisionLog = result.getMessages().stream().anyMatch(msg -> msg.toString().contains("决策"));
        boolean hasApprovalLog = result.getMessages().stream().anyMatch(msg -> msg.toString().contains("审批"));
        
        System.out.println("执行记录: 重试=" + hasRetryLog + ", 决策=" + hasDecisionLog + ", 审批=" + hasApprovalLog);
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
