package com.zenvora.agents.ch02;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zenvora.agents.common.ChatMessage;
import com.zenvora.agents.common.ChatResponse;
import com.zenvora.agents.common.LlmClient;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A model client built on the JDK's own HTTP client, with no AI framework.
 * It talks to the Anthropic Messages API: POST /v1/messages with an x-api-key header.
 * Verify header names and the version string against the provider's current documentation.
 */
public final class AnthropicHttpClient implements LlmClient {

    private static final String API_VERSION = "2023-06-01";
    private static final int MAX_ERROR_BODY_CHARS = 500;

    private final AnthropicConfig config;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper();

    public AnthropicHttpClient(AnthropicConfig config) {
        this.config = config;
        this.http = HttpClient.newBuilder().connectTimeout(config.timeout()).build();
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        try {
            String body = buildBody(messages);
            HttpRequest request = HttpRequest.newBuilder(config.baseUri().resolve("/v1/messages"))
                    .timeout(config.timeout())
                    .header("content-type", "application/json")
                    .header("x-api-key", config.apiKey())
                    .header("anthropic-version", API_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                throw new LlmException(
                        "Model call failed with HTTP " + response.statusCode() + ": "
                                + truncate(response.body()),
                        response.statusCode());
            }
            return parse(response.body());
        } catch (IOException e) {
            throw new LlmException("Network or JSON error calling model: " + e.getMessage(), -1, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmException("Model call interrupted", -1, e);
        }
    }

    /** Builds the JSON request body. System messages go in the top-level "system" field. */
    String buildBody(List<ChatMessage> messages) throws JsonProcessingException {
        ObjectNode root = json.createObjectNode();
        root.put("model", config.model());
        root.put("max_tokens", config.maxTokens());

        String system = messages.stream()
                .filter(m -> m.role() == ChatMessage.Role.SYSTEM)
                .map(ChatMessage::content)
                .collect(Collectors.joining("\n\n"));
        if (!system.isEmpty()) {
            root.put("system", system);
        }

        ArrayNode array = root.putArray("messages");
        for (ChatMessage m : messages) {
            switch (m.role()) {
                case SYSTEM -> { /* already handled above */ }
                case USER -> array.addObject().put("role", "user").put("content", m.content());
                case ASSISTANT -> array.addObject().put("role", "assistant").put("content", m.content());
                case TOOL -> throw new IllegalArgumentException(
                        "TOOL messages are introduced in Chapter 5");
            }
        }
        return json.writeValueAsString(root);
    }

    /** Joins all text blocks of the reply and reads the token usage. */
    ChatResponse parse(String body) throws JsonProcessingException {
        JsonNode root = json.readTree(body);
        StringBuilder text = new StringBuilder();
        for (JsonNode block : root.path("content")) {
            if ("text".equals(block.path("type").asText())) {
                text.append(block.path("text").asText());
            }
        }
        JsonNode usage = root.path("usage");
        return new ChatResponse(
                text.toString(),
                usage.path("input_tokens").asInt(),
                usage.path("output_tokens").asInt());
    }

    private static String truncate(String s) {
        return s.length() <= MAX_ERROR_BODY_CHARS ? s : s.substring(0, MAX_ERROR_BODY_CHARS) + "...";
    }
}
