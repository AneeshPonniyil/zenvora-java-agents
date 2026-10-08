# CLAUDE.md: instructions for Claude Code in this repo

## What this is
Companion code for the book "Production-Ready AI Agents with Java". One Maven module per
chapter, plus `common/` for shared code. Readers will run this code, so correctness matters.

## Stack
- Java 21, Maven multi-module, JUnit 5.
- Spring Boot with Spring AI as the main framework; LangChain4j shown side by side where useful.
- Framework versions are pinned per chapter module. Do not upgrade versions without being asked.

## Commands
- Build and test everything: `mvn -B -ntp verify`
- One module: `mvn -B -ntp -pl common -am verify`

## Rules
- Tests must use `MockLlmClient`. Never call a real model in automated tests.
- Never commit secrets. API keys come from environment variables only (see `.env.example`).
- Tools in the demo are read-only or fake. Never connect demo code to real systems.
- Prefer small, focused changes. After each change, run the build and fix failures.
- When a fix changes behavior, say so, so the matching book chapter can be updated.
- Every code listing used in the book must compile and pass tests before it is copied into a chapter.

## Conventions
- Package root: `com.zenvora.agents`. Module packages: `com.zenvora.agents.<module>`.
- Use records for data, interfaces for seams (model, tools, trace sinks).
- Keep public APIs small and documented with short Javadoc.
