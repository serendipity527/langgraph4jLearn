package org.example.dynamicgraph.nodes.impl;

import org.example.dynamicgraph.core.DynamicState;
import org.example.dynamicgraph.nodes.DynamicNode;

import java.util.Map;

/**
 * 字符串追加节点
 */
public class AppendNode implements DynamicNode {
    
    @Override
    public Map<String, Object> execute(Map<String, Object> state, Map<String, Object> params) {
        Object dataObj = state.get(DynamicState.DATA_KEY);
        String data = dataObj != null ? dataObj.toString() : "";
        
        // 获取要追加的文本，默认为"-suffix"
        String suffix = (String) params.getOrDefault("suffix", "-suffix");
        
        String newData = data + suffix;
        
        System.out.println("[Append] 处理: '" + data + "' -> '" + newData + "'");
        
        // 更新数据
        return Map.of(
            DynamicState.DATA_KEY, newData,
            DynamicState.MESSAGES_KEY, "执行了字符串追加: " + suffix
        );
    }
    
    @Override
    public String getNodeType() {
        return "appendNode";
    }
}
