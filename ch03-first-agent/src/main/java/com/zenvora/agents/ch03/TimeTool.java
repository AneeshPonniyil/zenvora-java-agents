package com.zenvora.agents.ch03;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/** Reports the current time. The clock is injected so tests can fix it. */
public final class TimeTool implements ToolBox.Tool {

    private final Clock clock;

    public TimeTool(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String name() {
        return "currentTime";
    }

    @Override
    public String description() {
        return "Returns the current UTC time in ISO-8601 format. No arguments.";
    }

    @Override
    public String execute(Map<String, Object> args) {
        return Instant.now(clock).toString();
    }
}
