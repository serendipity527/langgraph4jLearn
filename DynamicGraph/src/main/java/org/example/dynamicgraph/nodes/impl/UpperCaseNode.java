package org.example.dynamicgraph.nodes.impl;

import org.example.dynamicgraph.core.DynamicState;
import org.example.dynamicgraph.nodes.DynamicNode;

import java.util.Map;

/**
 * 字符串转大写节点
 */
public class UpperCaseNode implements DynamicNode {
    
    @Override
    public Map<String, Object> execute(Map<String, Object> state, Map<String, Object> params) {
        Object dataObj = state.get(DynamicState.DATA_KEY);
        String data = dataObj != null ? dataObj.toString() : "";
        
        // 转大写
        String upperData = data.toUpperCase();
        
        System.out.println("[UpperCase] 处理: '" + data + "' -> '" + upperData + "'");
        
        // 更新数据
        return Map.of(
            DynamicState.DATA_KEY, upperData,
            DynamicState.MESSAGES_KEY, "执行了大写转换"
        );
    }
    
    @Override
    public String getNodeType() {
        return "upperCaseNode";
    }
}
