package com.zenvora.agents.ch03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DecisionParserTest {

    @Test
    void parsesAFinalAnswer() {
        var d = DecisionParser.parse("{\"action\":\"final\",\"answer\":\"done\"}");
        assertEquals(new AgentDecision.FinalAnswer("done"), d);
    }

    @Test
    void parsesAToolCallWithArguments() {
        var d = DecisionParser.parse("{\"action\":\"tool\",\"tool\":\"add\",\"args\":{\"a\":1,\"b\":2}}");
        var call = assertInstanceOf(d);
        assertEquals("add", call.tool());
        assertEquals(1, call.args().get("a"));
    }

    @Test
    void toleratesCodeFencesAndSurroundingText() {
        var d = DecisionParser.parse("Sure!\n```json\n{\"action\":\"final\",\"answer\":\"ok\"}\n```");
        assertEquals(new AgentDecision.FinalAnswer("ok"), d);
    }

    @Test
    void missingArgsBecomeAnEmptyMap() {
        var call = assertInstanceOf(DecisionParser.parse("{\"action\":\"tool\",\"tool\":\"currentTime\"}"));
        assertTrue(call.args().isEmpty());
    }

    @Test
    void rejectsGarbageAndIncompleteDecisions() {
        assertThrows(InvalidDecisionException.class, () -> DecisionParser.parse("hello"));
        assertThrows(InvalidDecisionException.class, () -> DecisionParser.parse("{\"action\":\"dance\"}"));
        assertThrows(InvalidDecisionException.class, () -> DecisionParser.parse("{\"action\":\"final\"}"));
        assertThrows(InvalidDecisionException.class, () -> DecisionParser.parse("{\"action\":\"tool\"}"));
        assertThrows(InvalidDecisionException.class, () -> DecisionParser.parse("{not json}"));
    }

    private static AgentDecision.CallTool assertInstanceOf(AgentDecision d) {
        assertTrue(d instanceof AgentDecision.CallTool, "expected a tool call but got " + d);
        return (AgentDecision.CallTool) d;
    }
}
