package com.zenvora.agents.ch02;

import com.zenvora.agents.common.AppConfig;
import com.zenvora.agents.common.ChatMessage;
import com.zenvora.agents.common.ChatResponse;
import com.zenvora.agents.common.LlmClient;
import com.zenvora.agents.common.MockLlmClient;
import java.util.List;

/** Run this class from your IDE. With DEMO_MOCK=true (the default) no API key or network is needed. */
public final class Ch02Demo {

    public static void main(String[] args) {
        LlmClient client = AppConfig.mockMode()
                ? new MockLlmClient("(mock) A token is a small chunk of text that a model reads and writes.")
                : new AnthropicHttpClient(AnthropicConfig.fromEnv());

        ChatResponse reply = client.chat(List.of(
                ChatMessage.system("Answer in one sentence."),
                ChatMessage.user("What is a token in a large language model?")));

        System.out.println(reply.content());
        System.out.printf("tokens in=%d out=%d total=%d%n",
                reply.inputTokens(), reply.outputTokens(), reply.totalTokens());
    }
}
