package com.zenvora.agents.common;

import java.util.Objects;

/** One message in a conversation with a model. */
public record ChatMessage(Role role, String content) {

    public enum Role { SYSTEM, USER, ASSISTANT, TOOL }

    public ChatMessage {
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(content, "content");
    }

    public static ChatMessage system(String text)    { return new ChatMessage(Role.SYSTEM, text); }
    public static ChatMessage user(String text)      { return new ChatMessage(Role.USER, text); }
    public static ChatMessage assistant(String text) { return new ChatMessage(Role.ASSISTANT, text); }
    public static ChatMessage tool(String text)      { return new ChatMessage(Role.TOOL, text); }
}
