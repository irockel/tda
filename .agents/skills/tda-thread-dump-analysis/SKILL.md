---
name: tda-thread-dump-analysis
description: Analyze Java thread dumps to diagnose deadlocks, thread contention, CPU spikes, carrier thread pinning (Project Loom virtual threads), and SMR zombie threads using TDA's headless MCP engine or CLI bridge. Trigger when encountering thread dump files (.tdump, .log, .out, jstack output) or when diagnosing JVM hangs, performance degradation, and thread exhaustion.
---

# TDA Thread Dump Analysis Skill

This skill provides automated, high-precision Java thread dump diagnostics powered by the TDA (Thread Dump Analyzer) headless engine.

## 🛑 Critical Context Guardrail

> **NEVER read, cat, or open raw thread dump files directly.**
>
> Production thread dumps can range from tens of thousands of lines to hundreds of megabytes. Dumping raw log contents into the agent context window exhausts token budgets, causes truncation, and degrades reasoning capability.
>
> **Always** delegate log parsing and diagnostic queries to TDA via **MCP tools** or the bundled **CLI bridge script** (`scripts/tda_client.py`).

---

## ⚡ Activation Triggers

Activate this skill when:
- Investigating Java application freezes, high CPU utilization, or severe response latency.
- Analyzing log files containing thread dumps (`jstack`, `kill -3`, or `Thread.dump_to_file`).
- Diagnosing suspected deadlocks, database connection pool exhaustion, or thread starvation.
- Auditing Java 19+ / 21+ Virtual Threads for carrier thread pinning issues.
- Investigating HotSpot SMR (Safe Memory Relocation) zombie thread leaks.

---

## 🔄 Dual-Mode Execution

TDA can be invoked in two equivalent modes depending on agent capabilities:

| Execution Mode | Supported Agents | How It Runs |
| :--- | :--- | :--- |
| **Native MCP Mode** | Junie, Claude Code, OpenCode, Codex | Directly call MCP tools (`parse_log`, `get_summary`, `check_deadlocks`, etc.) |
| **CLI Bridge Mode** | Pi Agent (OpenClaw), bash/terminal agents | Run terminal commands via `python3 <skill-dir>/scripts/tda_client.py <command>` |

---

## 📋 The 6-Step Diagnostic Protocol

Follow this structured sequence to triage and diagnose thread issues:

```
[1. parse_log] ──> [2. get_summary] ──> [3. check_deadlocks]
                                                │
[6. drill_down] <── [5. find_long_running] <── [4. analyze_virtual_threads]
```

### Step 1: Parse and Ingest Log File
Initialize TDA with the absolute path to the target log file. This loads and indexes all dumps in the log.
- **MCP Tool**: `parse_log(path="/absolute/path/to/threaddump.log")`
- **CLI Bridge**: `python3 scripts/tda_client.py parse /absolute/path/to/threaddump.log`

### Step 2: High-Level Inventory & Dump Summary
Retrieve an overview of all thread dumps present in the log, including dump index, timestamp, thread count, deadlock count, and SMR info.
- **MCP Tool**: `get_summary()`
- **CLI Bridge**: `python3 scripts/tda_client.py summary`
- **What to look for**:
  - How many dumps exist? (Multiple dumps spaced apart are needed for long-running thread analysis).
  - Thread count trend: Is the thread count exploding across dumps (thread leak)?
  - Immediate deadlock counts reported in dump headers.

### Step 3: Immediate Deadlock Assessment
Check whether any thread dump contains mutual exclusion deadlocks.
- **MCP Tool**: `check_deadlocks()`
- **CLI Bridge**: `python3 scripts/tda_client.py deadlocks`
- **Diagnostic Action**:
  - If deadlocks exist: Identify the cycle (e.g. Thread A holds Lock 1 and waits for Lock 2; Thread B holds Lock 2 and waits for Lock 1).
  - Report the involved threads, monitor addresses, and exact stack trace lines.

### Step 4: Virtual Thread Carrier Pinning (Java 19+ / 21+)
Analyze whether virtual threads have pinned their underlying carrier platform threads.
- **MCP Tool**: `analyze_virtual_threads()`
- **CLI Bridge**: `python3 scripts/tda_client.py virtual-threads`
- **Diagnostic Action**:
  - Pinned carrier threads (e.g. `ForkJoinPool-1-worker-*`) occur when code enters a `synchronized` block/method or executes native JNI calls while running on a virtual thread.
  - Pinning prevents the carrier thread from being released to execute other virtual threads, leading to severe throughput collapse.

### Step 5: Long-Running Thread & Starvation Analysis
Detect threads that remain active in identical stack frames across consecutive dumps (requires at least 2 dumps).
- **MCP Tool**: `find_long_running()`
- **CLI Bridge**: `python3 scripts/tda_client.py long-running`
- **Diagnostic Action**:
  - Distinguish between idle pool threads (`TIMED_WAITING` on `LinkedBlockingQueue.poll`) and stuck threads (blocked on database sockets, HTTP calls without timeout, or infinite computation loops).

### Step 6: Targeted Drill-Down & SMR Analysis
- **Native Threads**: Inspect threads executing in native code (JNI, OS system calls, database network drivers):
  - **MCP Tool**: `get_native_threads(dump_index=0)`
  - **CLI Bridge**: `python3 scripts/tda_client.py native-threads 0`
- **SMR Zombie Threads**: Inspect unresolved SMR addresses:
  - **MCP Tool**: `get_zombie_threads()`
  - **CLI Bridge**: `python3 scripts/tda_client.py zombies`

### Reset / Clear
Reset the in-memory thread store before analyzing a new file or starting a fresh run:
- **MCP Tool**: `clear()`
- **CLI Bridge**: `python3 scripts/tda_client.py clear`

---

## 🧠 Diagnostic Synthesis & Root Cause Analysis

When presenting your findings, structure the report with the following sections:

1. **Executive Summary**: State of the JVM (Healthy / Degraded / Deadlocked / Leaking).
2. **Critical Findings**:
   - Deadlocks (if any) with full cycle and stack traces.
   - Pinned virtual threads or carrier thread starvation.
   - Hot lock monitors and longest waiting thread queues.
3. **Thread State Distribution**: Breakdown of `RUNNABLE`, `BLOCKED`, `WAITING`, and `TIMED_WAITING` threads across dumps.
4. **Root Cause Identification**:
   - *Symptom vs Cause*: Do not confuse waiting threads with the blocker. Focus on the thread **holding** the contended monitor or the thread stuck on external I/O.
5. **Actionable Remediation**:
   - For deadlocks: Enforce lock ordering, use lock timeouts (`tryLock`), or consolidate locks.
   - For virtual thread pinning: Replace `synchronized` blocks with `java.util.concurrent.locks.ReentrantLock`.
   - For connection exhaustion: Increase pool capacity or audit connection leak points (missing `try-with-resources`).

---

## 📚 Deep Technical Reference

For comprehensive theoretical foundations on JVM thread states, monitor locking graphs, carrier thread unmounting mechanics, and SMR hazard pointers, consult:
- `references/diagnostics.md`
