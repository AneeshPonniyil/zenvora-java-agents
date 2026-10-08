package com.zenvora.agents.common;

import java.time.Instant;
import java.util.Objects;

/**
 * One step of an agent run (model call, tool call, retrieval, guardrail, approval, result).
 * The same event shape is logged, traced, and later streamed to the demo UI.
 */
public record TraceEvent(String runId, Type type, String title, String detail, Instant at, int tokens) {

    public enum Type { MODEL, RETRIEVAL, TOOL, GUARDRAIL, APPROVAL, RESULT }

    public TraceEvent {
        Objects.requireNonNull(runId, "runId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(detail, "detail");
        Objects.requireNonNull(at, "at");
        if (tokens < 0) {
            throw new IllegalArgumentException("tokens must be >= 0");
        }
    }

    public static TraceEvent of(String runId, Type type, String title, String detail, int tokens) {
        return new TraceEvent(runId, type, title, detail, Instant.now(), tokens);
    }
}
