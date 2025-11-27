package org.example.dynamicgraph.back.factory;

import org.example.dynamicgraph.back.config.EdgeConfig;
import org.example.dynamicgraph.back.config.NodeConfig;
import org.example.dynamicgraph.back.config.TenantGraphConfig;
import org.example.dynamicgraph.back.service.JsonConfigService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 测试配置工厂
 * 提供预定义的租户配置用于测试
 */
public class TestConfigFactory {
    
    /**
     * 租户A配置：简单串行处理
     * 流程：START -> 转大写 -> 打印日志 -> END
     */
    public static TenantGraphConfig createTenantAConfig() {
        List<NodeConfig> nodes = List.of(
            new NodeConfig("upperCase", "upperCaseNode"),
            new NodeConfig("logger", "logNode", Map.of("prefix", "[租户A]"))
        );
        
        List<EdgeConfig> edges = List.of(
            new EdgeConfig("START", "upperCase"),
            new EdgeConfig("upperCase", "logger"),
            new EdgeConfig("logger", "END")
        );
        
        return new TenantGraphConfig("tenant_A", "简单处理流程", nodes, edges);
    }
    
    /**
     * 租户B配置：带文本处理
     * 流程：START -> 追加后缀 -> 转大写 -> 打印日志 -> END
     */
    public static TenantGraphConfig createTenantBConfig() {
        List<NodeConfig> nodes = List.of(
            new NodeConfig("append", "appendNode", Map.of("suffix", "-test")),
            new NodeConfig("upperCase", "upperCaseNode"),
            new NodeConfig("logger", "logNode", Map.of("prefix", "[租户B]"))
        );
        
        List<EdgeConfig> edges = List.of(
            new EdgeConfig("START", "append"),
            new EdgeConfig("append", "upperCase"),
            new EdgeConfig("upperCase", "logger"),
            new EdgeConfig("logger", "END")
        );
        
        return new TenantGraphConfig("tenant_B", "文本处理流程", nodes, edges);
    }
    
    /**
     * 租户C配置：最简单流程（仅日志）
     * 流程：START -> 打印日志 -> END
     */
    public static TenantGraphConfig createTenantCConfig() {
        List<NodeConfig> nodes = List.of(
            new NodeConfig("logger", "logNode", Map.of("prefix", "[租户C-简单]"))
        );
        
        List<EdgeConfig> edges = List.of(
            new EdgeConfig("START", "logger"),
            new EdgeConfig("logger", "END")
        );
        
        return new TenantGraphConfig("tenant_C", "最简流程", nodes, edges);
    }
    
    /**
     * 从JSON字符串创建租户A配置
     */
    public static TenantGraphConfig createTenantAFromJson() throws IOException {
        JsonConfigService jsonService = new JsonConfigService();
        String json = """
            {
              "tenantId": "tenant_A_JSON",
              "graphName": "JSON驱动的简单流程",
              "nodes": [
                {
                  "id": "upperCase",
                  "type": "upperCaseNode",
                  "params": {}
                },
                {
                  "id": "logger",
                  "type": "logNode",
                  "params": {
                    "prefix": "[租户A-JSON]"
                  }
                }
              ],
              "edges": [
                {
                  "from": "START",
                  "to": "upperCase"
                },
                {
                  "from": "upperCase",
                  "to": "logger"
                },
                {
                  "from": "logger",
                  "to": "END"
                }
              ]
            }
            """;
        return jsonService.parseFromJson(json);
    }
    
    /**
     * 从JSON字符串创建租户B配置
     */
    public static TenantGraphConfig createTenantBFromJson() throws IOException {
        JsonConfigService jsonService = new JsonConfigService();
        String json = """
            {
              "tenantId": "tenant_B_JSON",
              "graphName": "JSON驱动的文本处理流程",
              "nodes": [
                {
                  "id": "append",
                  "type": "appendNode",
                  "params": {
                    "suffix": "-json-suffix"
                  }
                },
                {
                  "id": "upperCase",
                  "type": "upperCaseNode",
                  "params": {}
                },
                {
                  "id": "logger",
                  "type": "logNode",
                  "params": {
                    "prefix": "[租户B-JSON]"
                  }
                }
              ],
              "edges": [
                {
                  "from": "START",
                  "to": "append"
                },
                {
                  "from": "append",
                  "to": "upperCase"
                },
                {
                  "from": "upperCase",
                  "to": "logger"
                },
                {
                  "from": "logger",
                  "to": "END"
                }
              ]
            }
            """;
        return jsonService.parseFromJson(json);
    }
}
