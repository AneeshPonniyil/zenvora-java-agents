# ch03-first-agent

Chapter 3 module: an agent loop written by hand, with no AI framework.

- `AgentLoop`: ask the model, run the chosen tool, feed the result back, stop on answer or limit
- `AgentDecision` / `DecisionParser`: the model's reply as a sealed type (tool call or final answer)
- `ToolBox`: the allowlist of tools; failures come back as data
- `AddTool`, `TimeTool`: two tiny example tools
- `Ch03Demo`: run from your IDE; mock mode (default) needs no key

Build and test: `mvn -B -ntp -pl ch03-first-agent -am verify`
