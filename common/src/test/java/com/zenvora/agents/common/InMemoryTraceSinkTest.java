package com.zenvora.agents.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class InMemoryTraceSinkTest {

    @Test
    void keepsEventsInEmissionOrderAndSumsTokens() {
        var sink = new InMemoryTraceSink();
        sink.emit(TraceEvent.of("run-1", TraceEvent.Type.MODEL, "Classify", "critical", 400));
        sink.emit(TraceEvent.of("run-1", TraceEvent.Type.TOOL, "getMetrics", "{}", 0));
        sink.emit(TraceEvent.of("run-1", TraceEvent.Type.RESULT, "Done", "ok", 100));

        assertEquals(3, sink.events().size());
        assertEquals(TraceEvent.Type.MODEL, sink.events().get(0).type());
        assertEquals(TraceEvent.Type.RESULT, sink.events().get(2).type());
        assertEquals(500, sink.totalTokens());
    }

    @Test
    void rejectsNegativeTokens() {
        assertThrows(IllegalArgumentException.class,
                () -> TraceEvent.of("run-1", TraceEvent.Type.MODEL, "x", "y", -1));
    }
}
