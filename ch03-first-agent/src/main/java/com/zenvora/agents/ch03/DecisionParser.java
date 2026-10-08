package com.zenvora.agents.ch03;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Turns the model's text into an {@link AgentDecision}. It looks for the first JSON object in the
 * reply, so a reply wrapped in a code fence or surrounded by a sentence still works.
 */
public final class DecisionParser {

    private static final ObjectMapper JSON = new ObjectMapper();

    private DecisionParser() {}

    public static AgentDecision parse(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new InvalidDecisionException("No JSON object found in the reply");
        }

        JsonNode node;
        try {
            node = JSON.readTree(raw.substring(start, end + 1));
        } catch (JsonProcessingException e) {
            throw new InvalidDecisionException("Invalid JSON: " + e.getOriginalMessage());
        }

        String action = node.path("action").asText("");
        return switch (action) {
            case "final" -> {
                String answer = node.path("answer").asText("");
                if (answer.isBlank()) {
                    throw new InvalidDecisionException("A final decision needs a non-empty \"answer\"");
                }
                yield new AgentDecision.FinalAnswer(answer);
            }
            case "tool" -> {
                String tool = node.path("tool").asText("");
                if (tool.isBlank()) {
                    throw new InvalidDecisionException("A tool decision needs a \"tool\" name");
                }
                JsonNode argsNode = node.get("args");
                Map<String, Object> args = (argsNode != null && argsNode.isObject())
                        ? JSON.convertValue(argsNode, new TypeReference<Map<String, Object>>() {})
                        : Map.of();
                yield new AgentDecision.CallTool(tool, args);
            }
            default -> throw new InvalidDecisionException("Unknown action: \"" + action + "\"");
        };
    }
}
