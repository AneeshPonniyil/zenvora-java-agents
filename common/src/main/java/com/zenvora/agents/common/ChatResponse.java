package com.zenvora.agents.common;

/** A model reply plus token counts, so cost can be tracked from day one. */
public record ChatResponse(String content, int inputTokens, int outputTokens) {

    public int totalTokens() {
        return inputTokens + outputTokens;
    }
}
