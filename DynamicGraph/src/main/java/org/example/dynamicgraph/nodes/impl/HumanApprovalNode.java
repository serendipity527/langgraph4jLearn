package org.example.dynamicgraph.nodes.impl;

import org.example.dynamicgraph.core.DynamicState;
import org.example.dynamicgraph.nodes.DynamicNode;

import java.util.Map;
import java.util.Scanner;

/**
 * 人工审批节点 - 需要人工干预确认
 */
public class HumanApprovalNode implements DynamicNode {
    
    private static final Scanner scanner = new Scanner(System.in);
    
    @Override
    public Map<String, Object> execute(Map<String, Object> state, Map<String, Object> params) {
        String data = (String) state.get(DynamicState.DATA_KEY);
        boolean autoApprove = (Boolean) params.getOrDefault("autoApprove", false);
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🤖 需要人工审批");
        System.out.println("当前数据: " + data);
        System.out.println("请选择操作:");
        System.out.println("1. 批准 (approve)");
        System.out.println("2. 拒绝 (reject)"); 
        System.out.println("3. 需要修改 (modify)");
        System.out.println("=".repeat(50));
        
        String decision;
        if (autoApprove) {
            // 自动审批模式（用于测试）
            decision = "approve";
            System.out.println("🤖 自动审批: " + decision);
        } else {
            // 实际人工输入
            System.out.print("请输入决定 (approve/reject/modify): ");
            decision = scanner.nextLine().trim().toLowerCase();
        }
        
        switch (decision) {
            case "approve":
                System.out.println("✅ 审批通过");
                return Map.of(
                    DynamicState.DATA_KEY, data + "-approved",
                    DynamicState.MESSAGES_KEY, "人工审批通过"
                );
            case "reject":
                System.out.println("❌ 审批拒绝");
                return Map.of(
                    DynamicState.DATA_KEY, data + "-rejected",
                    DynamicState.MESSAGES_KEY, "人工审批拒绝"
                );
            case "modify":
                System.out.println("🔧 需要修改");
                return Map.of(
                    DynamicState.DATA_KEY, data + "-modify",
                    DynamicState.MESSAGES_KEY, "需要修改后重新提交"
                );
            default:
                System.out.println("⚠️ 无效输入，默认拒绝");
                return Map.of(
                    DynamicState.DATA_KEY, data + "-rejected",
                    DynamicState.MESSAGES_KEY, "无效输入，默认拒绝"
                );
        }
    }
    
    @Override
    public String getNodeType() {
        return "humanApprovalNode";
    }
}
