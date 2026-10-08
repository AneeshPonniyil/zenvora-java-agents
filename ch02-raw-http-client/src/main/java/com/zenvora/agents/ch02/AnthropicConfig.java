package com.zenvora.agents.ch02;

import com.zenvora.agents.common.AppConfig;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/** Settings for the raw HTTP client. The API key is never printed by {@link #toString()}. */
public record AnthropicConfig(String apiKey, String model, int maxTokens, URI baseUri, Duration timeout) {

    /** Starting default. Check the provider's model list and override with MODEL_NAME as needed. */
    public static final String DEFAULT_MODEL = "claude-haiku-4-5-20251001";

    public AnthropicConfig {
        Objects.requireNonNull(apiKey, "apiKey");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(baseUri, "baseUri");
        Objects.requireNonNull(timeout, "timeout");
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be > 0");
        }
    }

    public static AnthropicConfig fromEnv() {
        return new AnthropicConfig(
                AppConfig.require("MODEL_API_KEY"),
                AppConfig.get("MODEL_NAME", DEFAULT_MODEL),
                Integer.parseInt(AppConfig.get("MODEL_MAX_TOKENS", "1024")),
                URI.create(AppConfig.get("MODEL_BASE_URL", "https://api.anthropic.com")),
                Duration.ofSeconds(60));
    }

    @Override
    public String toString() {
        return "AnthropicConfig[model=" + model + ", maxTokens=" + maxTokens
                + ", baseUri=" + baseUri + ", apiKey=***]";
    }
}
