package com.zenvora.agents.ch03;

import java.util.Map;

/** What the model wants to do next. The compiler forces every switch over this type to handle both cases. */
public sealed interface AgentDecision permits AgentDecision.CallTool, AgentDecision.FinalAnswer {

    record CallTool(String tool, Map<String, Object> args) implements AgentDecision {}

    record FinalAnswer(String text) implements AgentDecision {}
}
