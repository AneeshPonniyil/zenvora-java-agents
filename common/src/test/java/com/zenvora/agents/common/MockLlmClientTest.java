package com.zenvora.agents.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MockLlmClientTest {

    @Test
    void returnsScriptedRepliesInOrder() {
        var client = new MockLlmClient("first", "second");
        var msgs = List.of(ChatMessage.user("hello"));

        assertEquals("first", client.chat(msgs).content());
        assertEquals("second", client.chat(msgs).content());
    }

    @Test
    void recordsEveryCall() {
        var client = new MockLlmClient("ok");
        client.chat(List.of(ChatMessage.system("be brief"), ChatMessage.user("hi")));

        assertEquals(1, client.calls().size());
        assertEquals(2, client.calls().get(0).size());
    }

    @Test
    void failsLoudlyWhenScriptIsExhausted() {
        var client = new MockLlmClient();
        assertThrows(IllegalStateException.class,
                () -> client.chat(List.of(ChatMessage.user("hi"))));
    }

    @Test
    void reportsPositiveTokenCounts() {
        var client = new MockLlmClient("a short reply");
        var response = client.chat(List.of(ChatMessage.user("a short question")));

        assertTrue(response.inputTokens() > 0);
        assertTrue(response.outputTokens() > 0);
        assertEquals(response.inputTokens() + response.outputTokens(), response.totalTokens());
    }
}
