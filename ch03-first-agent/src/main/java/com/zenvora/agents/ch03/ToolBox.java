package com.zenvora.agents.ch03;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The only tools the agent may use. Anything not registered here cannot run, and failures come back
 * as data the model can read, not as exceptions that crash the loop.
 */
public final class ToolBox {

    public interface Tool {
        String name();

        String description();

        String execute(Map<String, Object> args) throws Exception;
    }

    public record Result(boolean ok, String content) {}

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public ToolBox register(Tool tool) {
        if (tools.putIfAbsent(tool.name(), tool) != null) {
            throw new IllegalArgumentException("Duplicate tool name: " + tool.name());
        }
        return this;
    }

    /** One line per tool, used in the system prompt. */
    public String describe() {
        return tools.values().stream()
                .map(t -> "- " + t.name() + ": " + t.description())
                .collect(Collectors.joining("\n"));
    }

    public Result call(String name, Map<String, Object> args) {
        Tool tool = tools.get(name);
        if (tool == null) {
            return new Result(false, "Unknown tool: " + name);
        }
        try {
            return new Result(true, tool.execute(args));
        } catch (Exception e) {
            return new Result(false, "Tool failed: " + e.getMessage());
        }
    }
}
