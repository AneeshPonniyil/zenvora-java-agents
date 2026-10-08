package com.zenvora.agents.common;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Scripted model for deterministic tests and the offline demo.
 * Returns the queued replies in order and records every call it receives.
 */
public final class MockLlmClient implements LlmClient {

    private final Queue<String> replies = new ArrayDeque<>();
    private final List<List<ChatMessage>> calls = new ArrayList<>();

    public MockLlmClient(String... scriptedReplies) {
        Collections.addAll(replies, scriptedReplies);
    }

    @Override
    public synchronized ChatResponse chat(List<ChatMessage> messages) {
        calls.add(List.copyOf(messages));
        String reply = replies.poll();
        if (reply == null) {
            throw new IllegalStateException("MockLlmClient has no scripted replies left");
        }
        int in = messages.stream().mapToInt(m -> estimateTokens(m.content())).sum();
        return new ChatResponse(reply, in, estimateTokens(reply));
    }

    /** Every request received so far, oldest first. */
    public synchronized List<List<ChatMessage>> calls() {
        return List.copyOf(calls);
    }

    /** Rough estimate (about 4 characters per token). Good enough for demos and tests only. */
    static int estimateTokens(String text) {
        return Math.max(1, text.length() / 4);
    }
}
