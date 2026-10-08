# Production-Ready AI Agents with Java: companion code

Companion repository for the book. Each chapter has its own Maven module, and `common/`
holds shared types (model interface, mock model, trace events).

## Requirements
- JDK 21 or newer
- Maven 3.9+
- Docker (only for chapters that need Postgres or other services)

## Quick start
```bash
mvn -B -ntp verify
```
This builds `common/` and runs its tests. No API key is needed: tests use a mock model.

## Running with a real model
Copy `.env.example` to `.env`, set `DEMO_MOCK=false` and your key, and follow the chapter's
instructions. Real calls cost money, so keep mock mode on while experimenting.

## Layout
- `common/`: `LlmClient`, `MockLlmClient`, `TraceEvent`, `TraceSink`, `AppConfig`
- `docs/CHAPTER_MAP.md`: which module belongs to which chapter
- `docker-compose.yml`: local services for later chapters
- `.github/workflows/ci.yml`: builds and tests every module on each push

## Versions
Library versions in this ecosystem change quickly. Each chapter module pins the versions it
was tested with and states them in the chapter. Do not assume newer versions behave the same.

## License
Add your chosen license here before publishing (and state what buyers may do with the code).
