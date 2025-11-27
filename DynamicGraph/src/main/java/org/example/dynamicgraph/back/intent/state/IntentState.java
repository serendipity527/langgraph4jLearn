package org.example.dynamicgraph.back.intent.state;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 意图识别流程状态
 */
public class IntentState extends AgentState {

    public static final String INPUT_KEY = "input";
    public static final String INTENT_KEY = "intent";
    public static final String OUTPUT_KEY = "output";
    public static final String MESSAGES_KEY = "messages";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
        INPUT_KEY, Channels.base(() -> ""),
        INTENT_KEY, Channels.base(() -> ""),
        OUTPUT_KEY, Channels.base(() -> ""),
        MESSAGES_KEY, Channels.appender(ArrayList::new)
    );

    public IntentState(Map<String, Object> initData) {
        super(initData);
    }

    public String getInput() {
        return this.<String>value(INPUT_KEY).orElse("");
    }

    public String getIntent() {
        return this.<String>value(INTENT_KEY).orElse("");
    }

    public String getOutput() {
        return this.<String>value(OUTPUT_KEY).orElse("");
    }

    public List<String> getMessages() {
        return this.<List<String>>value(MESSAGES_KEY).orElse(new ArrayList<>());
    }
}
