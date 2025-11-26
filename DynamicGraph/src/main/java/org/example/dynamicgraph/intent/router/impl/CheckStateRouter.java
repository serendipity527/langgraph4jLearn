package org.example.dynamicgraph.intent.router.impl;

import org.example.dynamicgraph.intent.router.GraphRouter;
import org.example.dynamicgraph.intent.state.IntentState;
import org.springframework.stereotype.Component;

/**
 * 状态检查路由器 - 根据intent字段进行路由
 */
@Component("CheckStateRouter")
public class CheckStateRouter implements GraphRouter<IntentState> {

    @Override
    public String route(IntentState state) {
        String intent = state.getIntent();
        System.out.println("🔀 条件路由 - 当前意图: " + intent);
        return intent;
    }
}
