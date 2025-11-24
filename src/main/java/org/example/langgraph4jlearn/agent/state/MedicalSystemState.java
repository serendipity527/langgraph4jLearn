package org.example.langgraph4jlearn.agent.state;

import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.example.langgraph4jlearn.enums.SystemStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MedicalSystemState extends org.bsc.langgraph4j.state.AgentState {
    
    public static final String STAGE = "stage";
    public static final String USER_QUERY = "userQuery";
    public static final String USER_CONSENTED = "userConsented";
    public static final String DISCLAIMER_SHOWN = "disclaimerShown";
    public static final String MESSAGES = "messages";
    public static final String IS_EMERGENCY = "isEmergency";
    public static final String INTENT = "intent";
    public static final String CONTEXT = "context";
    public static final String RESPONSE = "response";
    
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES, Channels.appender(ArrayList::new)
    );
    
    public MedicalSystemState(Map<String, Object> initData) {
        super(initData);
    }
    
    public SystemStage stage() {
        return this.<SystemStage>value(STAGE).orElse(SystemStage.DISCLAIMER);
    }
    
    public String userQuery() {
        return this.<String>value(USER_QUERY).orElse("");
    }
    
    public Boolean userConsented() {
        return this.<Boolean>value(USER_CONSENTED).orElse(false);
    }
    
    public Boolean disclaimerShown() {
        return this.<Boolean>value(DISCLAIMER_SHOWN).orElse(false);
    }
    
    public List<String> messages() {
        return this.<List<String>>value(MESSAGES).orElse(List.of());
    }
    
    public Boolean isEmergency() {
        return this.<Boolean>value(IS_EMERGENCY).orElse(false);
    }
    
    public String intent() {
        return this.<String>value(INTENT).orElse("");
    }
    
    public String context() {
        return this.<String>value(CONTEXT).orElse("");
    }
    
    public String response() {
        return this.<String>value(RESPONSE).orElse("");
    }
}
