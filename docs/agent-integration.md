# Multi-Agent Integration Guide: TDA Thread Dump Analysis

This document provides setup instructions, configuration recipes, file locations, and invocation patterns for integrating the **TDA Thread Dump Analysis Agent Skill** (`tda-thread-dump-analysis`) and **MCP Server** across modern AI coding agents.

---

## 🧭 Agent Capability & Integration Matrix

| Agent | Architecture | Skill Discovery Path | Primary Invocation Method |
| :--- | :--- | :--- | :--- |
| **Junie** (JetBrains) | Native MCP + Skills | `.junie/skills/` or `.agents/skills/` | MCP tool calls (`parse_log`, `get_summary`, ...) |
| **OpenCode** | Native MCP + Skills | `.agents/skills/` or `.opencode/skills/` | MCP tool calls |
| **Claude Code** (Anthropic) | Native MCP + Skills | `.claude-plugin/` or `.agents/skills/` | MCP tool calls via plugin / marketplace |
| **OpenAI Codex CLI** | Native MCP + Skills | `.codex-plugin/` or `.agents/skills/` | MCP tool calls via plugin / `AGENTS.md` |
| **Cursor / Windsurf** | Native MCP | `.cursor/mcp.json` / workspace skills | MCP tool calls |

---

## 1. JetBrains Junie

Junie supports both native MCP servers and Agent Skills following the open `agentskills.io` standard.

### MCP Configuration
Create or update `.junie/mcp.json` in your project root (or globally at `~/.junie/mcp.json`):

```json
{
  "mcpServers": {
    "tda": {
      "command": "java",
      "args": [
        "-Djava.awt.headless=true",
        "-jar",
        "/absolute/path/to/tda.jar",
        "--mcp"
      ]
    }
  }
}
```

### Skill Registration
Ensure the skill is placed in either:
- Project level: `.agents/skills/tda-thread-dump-analysis/` (or symlinked to `.junie/skills/tda-thread-dump-analysis/`)
- User level: `~/.junie/skills/tda-thread-dump-analysis/`

### Junie System Instructions
Add the following guardrail to `.junie/instructions.md` (or `~/.junie/instructions.md`):

```markdown
### Java Thread Dump Analysis
When asked to analyze Java thread dumps or files matching *.tdump, *.log, or catalina.out:
1. NEVER run cat, read_file, or editor tools directly on thread dump files.
2. Activate the `tda-thread-dump-analysis` skill.
3. Call `parse_log(path=...)` followed by `get_summary()`, `check_deadlocks()`, and `analyze_virtual_threads()`.
```

---

## 2. OpenCode

OpenCode discovers tools through standard MCP configuration and supports skills in `.agents/skills/`.

### MCP Configuration
Add the TDA server to `~/.config/opencode/opencode.json` (global) or `.opencode/config.json` (project):

```json
{
  "$schema": "https://opencode.ai/config.schema.json",
  "mcp": {
    "servers": {
      "tda": {
        "command": "java",
        "args": [
          "-Djava.awt.headless=true",
          "-jar",
          "/absolute/path/to/tda.jar",
          "--mcp"
        ]
      }
    }
  }
}
```

### Skill Location
OpenCode scans `.agents/skills/` in the workspace root automatically:
```bash
# Verify skill presence
ls -la .agents/skills/tda-thread-dump-analysis/SKILL.md
```

---

## 3. Anthropic Claude Code

Claude Code integrates MCP servers via its CLI manager and recognizes Agent Skills placed in `.claude/skills/` or `.agents/skills/`.

### Adding TDA to Claude Code
Run the `claude mcp add` command from your terminal:

```bash
claude mcp add tda -- java -Djava.awt.headless=true -jar /absolute/path/to/tda.jar --mcp
```

Or configure `~/.claude/settings.json` directly:

```json
{
  "mcpServers": {
    "tda": {
      "command": "java",
      "args": [
        "-Djava.awt.headless=true",
        "-jar",
        "/absolute/path/to/tda.jar",
        "--mcp"
      ]
    }
  }
}
```

### Skill Discovery
Symlink or copy the skill into Claude's skill search directory:

```bash
mkdir -p .claude/skills
ln -s ../../.agents/skills/tda-thread-dump-analysis .claude/skills/tda-thread-dump-analysis
```

---

## 4. OpenAI Codex CLI

OpenAI Codex CLI detects skills located in `.agents/skills/` and configures MCP servers via TOML.

### MCP Configuration
Add the server entry to `~/.codex/config.toml` or workspace `.codex/config.toml`:

```toml
[mcp_servers.tda]
command = "java"
args = ["-Djava.awt.headless=true", "-jar", "/absolute/path/to/tda.jar", "--mcp"]
```

### Skill Registration (`AGENTS.md`)
Add the skill to your project's `AGENTS.md`:

```markdown
## Active Skills
- `tda-thread-dump-analysis`: Located at `.agents/skills/tda-thread-dump-analysis`. Use for all Java thread dump investigations and JVM freeze diagnostics.
```

---

## 5. Cursor & Windsurf

### Cursor Configuration
Add to `.cursor/mcp.json` in your workspace root (or use `scripts/tda_launcher.py` for automated bootstrap):

```json
{
  "mcpServers": {
    "tda": {
      "command": "python3",
      "args": [
        "scripts/tda_launcher.py"
      ],
      "env": {
        "JAVA_OPTS": "-Djava.awt.headless=true"
      }
    }
  }
}
```

Add the following rule to `.cursorrules`:
```markdown
When encountering Java thread dumps (*.tdump, *.log):
- Use the TDA MCP tools (parse_log, get_summary, check_deadlocks, analyze_virtual_threads).
- Do not cat or open raw thread dumps directly in editor.
```

### Windsurf Configuration
Add to `~/.codeium/windsurf/mcp_config.json`:

```json
{
  "mcpServers": {
    "tda": {
      "command": "java",
      "args": [
        "-Djava.awt.headless=true",
        "-jar",
        "/absolute/path/to/tda.jar",
        "--mcp"
      ]
    }
  }
}
```

---

## 6. Herdr (Multi-Agent Orchestrator)

**Herdr** orchestrates teams of agents running in parallel panes connected through `herdr-link`.

### Architecture Pattern in Herdr

```
                ┌──────────────────────────────────┐
                │   Herdr Orchestrator / Lead      │
                │   (Task coordination & triage)   │
                └─────────────────┬────────────────┘
                                  │
                 Delegates thread dump investigation
                                  │
                ┌─────────────────▼────────────────┐
                │        Investigator Worker       │
                │ (Claude Code, Junie, OpenCode)   │
                └─────────────────┬────────────────┘
                                  │
                   Accesses shared .agents/skills/
                                  │
                                  ▼
                         [Native MCP Mode]
                   `parse_log`, `check_deadlocks`
```

### Shared Skill Setup
In a Herdr multi-agent workspace, place the skill in the workspace root under `.agents/skills/tda-thread-dump-analysis/`. All agent panes share access to this directory.

### Orchestration Recipe: Delegating Analysis
When the primary Herdr orchestrator detects a performance issue or receives an incident ticket with a thread dump:

1. **Lead Pane Prompt**:
   ```
   pane-2: Investigate the thread dump at /var/log/app/incident-dump.log using skill 'tda-thread-dump-analysis'.
   Report: (1) Deadlock status, (2) Virtual thread carrier pinning, (3) Blocked thread count.
   ```
2. **Worker Pane Execution**:
   The worker agent activates the MCP tools (`parse_log`, `get_summary`, `check_deadlocks`, `analyze_virtual_threads`).
3. **Worker Pane Response**:
   Returns an executive summary back to the orchestrator pane without leaking megabytes of raw stack traces into the Herdr orchestrator's context window.

---

## 🔧 Environment & Troubleshooting

### Memory Configuration for Large Production Dumps
For analyzing multi-hundred-megabyte thread dump files, increase the maximum Java heap allocation using `-Xmx`:

```json
{
  "args": [
    "-Djava.awt.headless=true",
    "-Xmx2g",
    "-jar",
    "/path/to/tda.jar",
    "--mcp"
  ]
}
```

### Verifying Stdio Communication
Test that the MCP server responds cleanly to JSON-RPC initialization without standard output pollution:

```bash
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05"}}' | java -Djava.awt.headless=true -jar /path/to/tda.jar --mcp
```

Expected output:
```json
{"jsonrpc":"2.0","id":1,"result":{"protocolVersion":"2024-11-05","capabilities":{"tools":{}},"serverInfo":{"name":"tda-mcp-server","version":"3.2.0"}}}
```
