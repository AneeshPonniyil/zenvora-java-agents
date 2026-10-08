package com.zenvora.agents.ch02;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.zenvora.agents.common.ChatMessage;
import com.zenvora.agents.common.ChatResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests the client against a tiny local HTTP server. No network, no API key, no cost. */
class AnthropicHttpClientTest {

    private static final String OK_BODY =
            "{\"content\":[{\"type\":\"text\",\"text\":\"Hello\"}],"
                    + "\"usage\":{\"input_tokens\":12,\"output_tokens\":3}}";

    private final ObjectMapper json = new ObjectMapper();
    private final AtomicReference<String> lastBody = new AtomicReference<>();
    private final AtomicReference<Headers> lastHeaders = new AtomicReference<>();
    private volatile int status = 200;
    private volatile String responseBody = OK_BODY;
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/messages", exchange -> {
            lastBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            lastHeaders.set(exchange.getRequestHeaders());
            byte[] out = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("content-type", "application/json");
            exchange.sendResponseHeaders(status, out.length);
            try (var os = exchange.getResponseBody()) {
                os.write(out);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private AnthropicHttpClient client() {
        var config = new AnthropicConfig("test-key", "test-model", 256,
                URI.create("http://127.0.0.1:" + server.getAddress().getPort()), Duration.ofSeconds(5));
        return new AnthropicHttpClient(config);
    }

    @Test
    void sendsRequiredHeadersAndBodyAndParsesReply() throws Exception {
        ChatResponse reply = client().chat(List.of(
                ChatMessage.system("Be brief."),
                ChatMessage.user("Hi")));

        assertEquals("Hello", reply.content());
        assertEquals(12, reply.inputTokens());
        assertEquals(3, reply.outputTokens());

        assertEquals("test-key", lastHeaders.get().getFirst("x-api-key"));
        assertEquals("2023-06-01", lastHeaders.get().getFirst("anthropic-version"));
        assertEquals("application/json", lastHeaders.get().getFirst("content-type"));

        JsonNode body = json.readTree(lastBody.get());
        assertEquals("test-model", body.get("model").asText());
        assertEquals(256, body.get("max_tokens").asInt());
        assertEquals("Be brief.", body.get("system").asText());
        assertEquals(1, body.get("messages").size());
        assertEquals("user", body.get("messages").get(0).get("role").asText());
        assertEquals("Hi", body.get("messages").get(0).get("content").asText());
    }

    @Test
    void joinsMultipleTextBlocks() {
        responseBody = "{\"content\":[{\"type\":\"text\",\"text\":\"Hel\"},"
                + "{\"type\":\"text\",\"text\":\"lo\"}],\"usage\":{\"input_tokens\":1,\"output_tokens\":1}}";

        assertEquals("Hello", client().chat(List.of(ChatMessage.user("Hi"))).content());
    }

    @Test
    void mapsHttpErrorsToLlmExceptionWithoutLeakingTheKey() {
        status = 429;
        responseBody = "{\"type\":\"error\",\"error\":{\"type\":\"rate_limit_error\",\"message\":\"slow down\"}}";

        LlmException e = assertThrows(LlmException.class,
                () -> client().chat(List.of(ChatMessage.user("Hi"))));

        assertEquals(429, e.status());
        assertFalse(e.getMessage().contains("test-key"));
    }

    @Test
    void rejectsToolMessagesUntilChapterFive() {
        assertThrows(IllegalArgumentException.class,
                () -> client().chat(List.of(ChatMessage.tool("result"))));
    }

    @Test
    void configToStringHidesTheApiKey() {
        var config = new AnthropicConfig("super-secret", "m", 10,
                URI.create("http://localhost"), Duration.ofSeconds(1));

        assertFalse(config.toString().contains("super-secret"));
    }
}
