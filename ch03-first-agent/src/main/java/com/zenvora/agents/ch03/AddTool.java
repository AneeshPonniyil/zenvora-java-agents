package com.zenvora.agents.ch03;

import java.util.Map;

/** Adds two numbers. Deliberately tiny: the point is the loop, not the tool. */
public final class AddTool implements ToolBox.Tool {

    @Override
    public String name() {
        return "add";
    }

    @Override
    public String description() {
        return "Adds two numbers. Arguments: {\"a\": number, \"b\": number}";
    }

    @Override
    public String execute(Map<String, Object> args) {
        if (!(args.get("a") instanceof Number a) || !(args.get("b") instanceof Number b)) {
            throw new IllegalArgumentException("Arguments 'a' and 'b' must be numbers");
        }
        double sum = a.doubleValue() + b.doubleValue();
        if (sum == Math.rint(sum) && Math.abs(sum) < 1e15) {
            return Long.toString((long) sum);
        }
        return Double.toString(sum);
    }
}
