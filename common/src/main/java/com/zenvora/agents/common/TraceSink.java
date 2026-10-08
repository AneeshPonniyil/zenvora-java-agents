package com.zenvora.agents.common;

/** Receives trace events. Implementations: in-memory (tests), logging, OpenTelemetry, SSE. */
public interface TraceSink {

    void emit(TraceEvent event);
}
