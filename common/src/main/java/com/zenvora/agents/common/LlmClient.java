package com.zenvora.agents.common;

import java.util.List;

/**
 * Minimal model abstraction used by the book's examples.
 * Chapter modules provide real implementations (raw HTTP, Spring AI, LangChain4j);
 * tests and the demo use {@link MockLlmClient}.
 */
public interface LlmClient {

    ChatResponse chat(List<ChatMessage> messages);
}
