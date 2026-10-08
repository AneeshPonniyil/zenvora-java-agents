package com.zenvora.agents.common;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Keeps events in memory so tests can assert on exactly what the agent did. */
public final class InMemoryTraceSink implements TraceSink {

    private final List<TraceEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void emit(TraceEvent event) {
        events.add(event);
    }

    public List<TraceEvent> events() {
        return List.copyOf(events);
    }

    public int totalTokens() {
        return events.stream().mapToInt(TraceEvent::tokens).sum();
    }
}
