package com.zenvora.agents.ch03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ToolBoxTest {

    private final ToolBox box = new ToolBox()
            .register(new AddTool())
            .register(new TimeTool(Clock.fixed(Instant.parse("2026-01-02T03:04:05Z"), ZoneOffset.UTC)));

    @Test
    void runsARegisteredTool() {
        var r = box.call("add", Map.of("a", 17, "b", 25));
        assertTrue(r.ok());
        assertEquals("42", r.content());
    }

    @Test
    void usesTheInjectedClock() {
        assertEquals("2026-01-02T03:04:05Z", box.call("currentTime", Map.of()).content());
    }

    @Test
    void unknownToolsComeBackAsErrorData() {
        var r = box.call("deleteEverything", Map.of());
        assertFalse(r.ok());
        assertTrue(r.content().contains("Unknown tool"));
    }

    @Test
    void toolFailuresComeBackAsErrorDataNotExceptions() {
        var r = box.call("add", Map.of("a", "seventeen", "b", 25));
        assertFalse(r.ok());
        assertTrue(r.content().contains("must be numbers"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        assertThrows(IllegalArgumentException.class, () -> box.register(new AddTool()));
    }

    @Test
    void describeListsEveryTool() {
        String text = box.describe();
        assertTrue(text.contains("- add:"));
        assertTrue(text.contains("- currentTime:"));
    }
}
