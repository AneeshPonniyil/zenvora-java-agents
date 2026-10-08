package com.zenvora.agents.ch03;

import com.zenvora.agents.ch02.AnthropicConfig;
import com.zenvora.agents.ch02.AnthropicHttpClient;
import com.zenvora.agents.common.AppConfig;
import com.zenvora.agents.common.LlmClient;
import com.zenvora.agents.common.MockLlmClient;
import com.zenvora.agents.common.TraceSink;
import java.time.Clock;

/** Run from your IDE. With DEMO_MOCK=true (the default) a scripted model plays the part of the LLM. */
public final class Ch03Demo {

    public static void main(String[] args) {
        LlmClient llm = AppConfig.mockMode()
                ? new MockLlmClient(
                        "{\"action\":\"tool\",\"tool\":\"add\",\"args\":{\"a\":17,\"b\":25}}",
                        "{\"action\":\"final\",\"answer\":\"17 + 25 = 42.\"}")
                : new AnthropicHttpClient(AnthropicConfig.fromEnv());

        ToolBox tools = new ToolBox()
                .register(new AddTool())
                .register(new TimeTool(Clock.systemUTC()));

        TraceSink printer = e -> System.out.printf("[%s] %s | %s%n", e.type(), e.title(), e.detail());

        AgentLoop agent = new AgentLoop(llm, tools, printer, 5);
        AgentLoop.Result result = agent.run("What is 17 + 25?");

        System.out.printf("%nstatus=%s steps=%d tokens=%d%nanswer=%s%n",
                result.status(), result.steps(), result.tokens(), result.answer());
    }
}
