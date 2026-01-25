# Copilot Review Instructions: TDA - Thread Dump Analyzer

You are reviewing **TDA (Thread Dump Analyzer)**, a Java-based desktop application using **Swing**. TDA is designed to parse and visualize complex Java thread dumps to identify deadlocks, resource contention, and performance bottlenecks.

## Core Technical Principles

### 1. Concurrency & Swing (EDT)
- **Responsiveness:** Ensure that long-running parsing or analysis tasks are NEVER executed on the Event Dispatch Thread (EDT). Use `SwingWorker` or an equivalent background execution mechanism.
- **UI Updates:** Ensure all updates to Swing components are wrapped in `SwingUtilities.invokeLater` if triggered from background threads.

### 2. Memory & Performance
- **Large Files:** Thread dumps can be massive (hundreds of MBs). Prefer streaming and incremental parsing over loading entire files into memory.
- **Object Lifecycle:** Watch for memory leaks in listeners and static collections, especially when opening and closing multiple dump files.

### 3. Parsing Logic (The Core)
- **Accuracy:** The parsing logic for thread states (RUNNABLE, BLOCKED, WAITING) must strictly follow JVM specifications.
- **Robustness:** Handle malformed or truncated thread dumps gracefully without crashing the UI. Provide meaningful error messages to the user.

### 4. Swing UI Best Practices
- **Look & Feel:** Maintain consistency with existing UI components.
- **Layouts:** Prefer `MigLayout` or `GridBagLayout` for complex forms to ensure resizability. Avoid absolute positioning.
- **Accessibility:** Ensure components have appropriate tooltips and mnemonic keys where applicable.

## Review Focus Areas
- **Deadlock Detection:** Double-check the logic that identifies circular dependencies in monitor locks.
- **Regex Performance:** Ensure that regular expressions used for log parsing are optimized and protected against Catastrophic Backtracking.
- **Clean Code:** Enforce Java 11+ coding standards (or the specific version TDA uses), focusing on readability and modularity.

## Communication Style
- Be concise and technical.
- If suggesting a change, explain the impact on performance or thread safety.
- Use code snippets for refactoring suggestions.