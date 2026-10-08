# ch02-raw-http-client

Chapter 2 module: a model client built on the JDK HTTP client (plus Jackson for JSON), a
local-server test that needs no network or API key, and a cosine similarity helper for embeddings.

- `AnthropicHttpClient`: sends POST /v1/messages and parses the reply
- `AnthropicConfig`: settings from environment variables; never prints the API key
- `LlmException`: carries the HTTP status
- `CosineSimilarity`: compares two embedding vectors
- `Ch02Demo`: run from your IDE; mock mode (default) needs no key

Environment variables for real calls (set `DEMO_MOCK=false`):
`MODEL_API_KEY` (required), `MODEL_NAME`, `MODEL_MAX_TOKENS`, `MODEL_BASE_URL`.

Build and test: `mvn -B -ntp -pl ch02-raw-http-client -am verify`
