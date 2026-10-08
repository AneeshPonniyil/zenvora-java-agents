package com.zenvora.agents.ch03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zenvora.agents.common.ChatMessage;
import com.zenvora.agents.common.InMemoryTraceSink;
import com.zenvora.agents.common.MockLlmClient;
import com.zenvora.agents.common.TraceEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgentLoopTest {

    private static final String CALL_ADD = "{\"action\":\"tool\",\"tool\":\"add\",\"args\":{\"a\":17,\"b\":25}}";
    private static final String FINAL_42 = "{\"action\":\"final\",\"answer\":\"42\"}";

    private final InMemoryTraceSink sink = new InMemoryTraceSink();
    private final ToolBox tools = new ToolBox().register(new AddTool());

    private AgentLoop agent(MockLlmClient llm, int maxSteps) {
        return new AgentLoop(llm, tools, sink, maxSteps);
    }

    private List<TraceEvent.Type> types() {
        return sink.events().stream().map(TraceEvent::type).toList();
    }

    @Test
    void answersImmediatelyWhenNoToolIsNeeded() {
        var llm = new MockLlmClient(FINAL_42);

        var result = agent(llm, 5).run("What is 17 + 25?");

        assertEquals(AgentLoop.Status.FINISHED, result.status());
        assertEquals("42", result.answer());
        assertEquals(1, result.steps());
        assertEquals(List.of(TraceEvent.Type.MODEL, TraceEvent.Type.RESULT), types());
    }

    @Test
    void runsAToolFeedsTheResultBackAndThenFinishes() {
        var llm = new MockLlmClient(CALL_ADD, FINAL_42);

        var result = agent(llm, 5).run("What is 17 + 25?");

        assertEquals(AgentLoop.Status.FINISHED, result.status());
        assertEquals(2, result.steps());
        assertEquals(List.of(TraceEvent.Type.MODEL, TraceEvent.Type.TOOL,
                TraceEvent.Type.MODEL, TraceEvent.Type.RESULT), types());

        // The second model call must have seen the tool result.
        List<ChatMessage> secondCall = llm.calls().get(1);
        ChatMessage lastMessage = secondCall.get(secondCall.size() - 1);
        assertEquals(ChatMessage.Role.USER, lastMessage.role());
        assertTrue(lastMessage.content().contains("Tool result for add: 42"));
    }

    @Test
    void tokenTotalMatchesTheTrace() {
        var result = agent(new MockLlmClient(CALL_ADD, FINAL_42), 5).run("go");
        assertEquals(result.tokens(), sink.totalTokens());
    }

    @Test
    void unknownToolIsReportedToTheModelAndTheLoopContinues() {
        var llm = new MockLlmClient("{\"action\":\"tool\",\"tool\":\"nope\"}", FINAL_42);

        var result = agent(llm, 5).run("go");

        assertEquals(AgentLoop.Status.FINISHED, result.status());
        List<ChatMessage> secondCall = llm.calls().get(1);
        assertTrue(secondCall.get(secondCall.size() - 1).content().contains("ERROR: Unknown tool: nope"));
    }

    @Test
    void stopsAtTheStepLimitInsteadOfLoopingForever() {
        var llm = new MockLlmClient(CALL_ADD, CALL_ADD, CALL_ADD, CALL_ADD, CALL_ADD);

        var result = agent(llm, 3).run("go");

        assertEquals(AgentLoop.Status.MAX_STEPS_REACHED, result.status());
        assertNull(result.answer());
        assertEquals(3, result.steps());
        assertEquals(3, llm.calls().size());
    }

    @Test
    void anInvalidReplyIsRepairedOnce() {
        var llm = new MockLlmClient("I think the answer is 42", FINAL_42);

        var result = agent(llm, 5).run("go");

        assertEquals(AgentLoop.Status.FINISHED, result.status());
        assertEquals(TraceEvent.Type.GUARDRAIL, sink.events().get(0).type());
        List<ChatMessage> secondCall = llm.calls().get(1);
        assertTrue(secondCall.get(secondCall.size() - 1).content().startsWith("Your reply was not valid"));
    }

    @Test
    void repeatedInvalidOutputStopsTheRun() {
        var llm = new MockLlmClient("nope", "still nope", "no again", FINAL_42);

        var result = agent(llm, 10).run("go");

        assertEquals(AgentLoop.Status.INVALID_OUTPUT, result.status());
        assertEquals(3, result.steps());
    }

    @Test
    void rejectsANonPositiveStepLimit() {
        assertThrows(IllegalArgumentException.class, () -> agent(new MockLlmClient(), 0));
    }
}
