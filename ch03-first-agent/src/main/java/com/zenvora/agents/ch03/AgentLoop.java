package com.zenvora.agents.ch03;

import com.zenvora.agents.common.ChatMessage;
import com.zenvora.agents.common.ChatResponse;
import com.zenvora.agents.common.LlmClient;
import com.zenvora.agents.common.TraceEvent;
import com.zenvora.agents.common.TraceSink;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A complete agent in one class: ask the model what to do, run the tool it picked, show it the result,
 * and repeat until it answers or a limit is hit. The model proposes; this code decides.
 */
public final class AgentLoop {

    public enum Status { FINISHED, MAX_STEPS_REACHED, INVALID_OUTPUT }

    public record Result(String runId, Status status, String answer, int steps, int tokens) {}

    private static final int MAX_CONSECUTIVE_INVALID = 2;
    private static final int MAX_DETAIL_CHARS = 300;

    private final LlmClient llm;
    private final ToolBox tools;
    private final TraceSink trace;
    private final int maxSteps;

    public AgentLoop(LlmClient llm, ToolBox tools, TraceSink trace, int maxSteps) {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps must be > 0");
        }
        this.llm = llm;
        this.tools = tools;
        this.trace = trace;
        this.maxSteps = maxSteps;
    }

    public Result run(String goal) {
        String runId = UUID.randomUUID().toString();
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt()));
        messages.add(ChatMessage.user(goal));

        int steps = 0;
        int tokens = 0;
        int invalidInARow = 0;

        while (steps < maxSteps) {
            steps++;
            ChatResponse reply = llm.chat(List.copyOf(messages));
            tokens += reply.totalTokens();
            messages.add(ChatMessage.assistant(reply.content()));

            AgentDecision decision;
            try {
                decision = DecisionParser.parse(reply.content());
            } catch (InvalidDecisionException e) {
                emit(runId, TraceEvent.Type.GUARDRAIL, "Invalid model output", e.getMessage(), reply.totalTokens());
                invalidInARow++;
                if (invalidInARow > MAX_CONSECUTIVE_INVALID) {
                    emit(runId, TraceEvent.Type.RESULT, "Stopped", "Model kept returning invalid output", 0);
                    return new Result(runId, Status.INVALID_OUTPUT, null, steps, tokens);
                }
                messages.add(ChatMessage.user("Your reply was not valid: " + e.getMessage()
                        + ". Reply with exactly one JSON object, as described."));
                continue;
            }
            invalidInARow = 0;

            switch (decision) {
                case AgentDecision.FinalAnswer done -> {
                    emit(runId, TraceEvent.Type.MODEL, "Model gave a final answer", done.text(), reply.totalTokens());
                    emit(runId, TraceEvent.Type.RESULT, "Finished", done.text(), 0);
                    return new Result(runId, Status.FINISHED, done.text(), steps, tokens);
                }
                case AgentDecision.CallTool call -> {
                    emit(runId, TraceEvent.Type.MODEL, "Model chose tool: " + call.tool(),
                            String.valueOf(call.args()), reply.totalTokens());
                    ToolBox.Result result = tools.call(call.tool(), call.args());
                    emit(runId, TraceEvent.Type.TOOL, "Tool " + call.tool() + (result.ok() ? " ok" : " failed"),
                            result.content(), 0);
                    // Observations go back as user messages. TOOL messages arrive with native tool calling in Chapter 5.
                    messages.add(ChatMessage.user("Tool result for " + call.tool() + ": "
                            + (result.ok() ? result.content() : "ERROR: " + result.content())));
                }
            }
        }

        emit(runId, TraceEvent.Type.RESULT, "Stopped", "Step limit of " + maxSteps + " reached", 0);
        return new Result(runId, Status.MAX_STEPS_REACHED, null, steps, tokens);
    }

    private String systemPrompt() {
        return """
                You are an agent that completes a goal, one step at a time, using tools.
                Reply with exactly one JSON object and nothing else.

                To use a tool:  {"action":"tool","tool":"<tool name>","args":{ ... }}
                To finish:      {"action":"final","answer":"<your answer>"}

                Available tools:
                """ + tools.describe() + """


                Rules:
                - Use a tool only when you need it.
                - After a tool result, decide the next step.
                - Finish as soon as you can answer.
                """;
    }

    private void emit(String runId, TraceEvent.Type type, String title, String detail, int tokens) {
        String clipped = detail.length() <= MAX_DETAIL_CHARS ? detail : detail.substring(0, MAX_DETAIL_CHARS) + "...";
        trace.emit(TraceEvent.of(runId, type, title, clipped, tokens));
    }
}
