# TDA MCP Server Documentation

This document provides instructions on how to use the Thread Dump Analysis (TDA) MCP server. 
The MCP server allows you to perform headless analysis of Java thread dumps using the Model Context Protocol.

## Prerequisites

- **Java 11 or higher**: The project is compiled with Java 11.
- **Maven**: To build the project from source.

## Installation and Build

To build the fat JAR containing the MCP server, run:

```bash
mvn clean package -DskipTests
```

This will create `target/tda-2.5.jar`.

## Running the MCP Server

The MCP server can be started using the following command:

```bash
java -Djava.awt.headless=true -jar target/tda-2.5.jar --mcp
```

- `-Djava.awt.headless=true`: Ensures that no AWT/Swing components are initialized.
- `--mcp`: Tells TDA to start in MCP server mode instead of launching the GUI.

## Integration

### Junie

To use the TDA MCP server with Junie, add it to your tools configuration:

```json
{
  "mcpServers": {
    "tda": {
      "command": "java",
      "args": [
        "-Djava.awt.headless=true",
        "-jar",
        "/absolute/path/to/tda/target/tda-2.5.jar",
        "--mcp"
      ]
    }
  }
}
```

### Cursor

To integrate with Cursor, go to **Settings > Models > MCP** and click **+ Add New MCP Server**.

- **Name**: `TDA`
- **Type**: `command`
- **Command**:
  ```bash
  java -Djava.awt.headless=true -jar /absolute/path/to/tda/target/tda-2.5.jar --mcp
  ```

## Available Tools

The MCP server exposes the following tools:

### `parse_log`
Parses a log file containing Java thread dumps.
- **Arguments**:
  - `path` (string, required): The absolute path to the log file.
- **Description**: This tool must be called first to load the thread dumps into memory. It supports standard JDK thread dumps and some other formats.

### `get_summary`
Returns a summary of all parsed thread dumps.
- **Arguments**: None
- **Description**: Provides an index, name, timestamp, thread count, and deadlock count for each dump found in the log file.

### `check_deadlocks`
Checks for deadlocks in the parsed thread dumps.
- **Arguments**: None
- **Description**: Returns information about any deadlocks detected by the JVM in any of the parsed dumps.

### `find_long_running`
Identifies threads that appear in multiple consecutive thread dumps.
- **Arguments**: None
- **Description**: Compares threads across all loaded dumps and identifies those that remain in the same state/stack trace, indicating potential performance issues or "hung" threads.

### `clear`
Resets the server state.
- **Arguments**: None
- **Description**: Clears the internal thread store, allowing you to parse a new log file without interference from previous data.

## Troubleshooting

- **Path issues**: Ensure you use absolute paths for the JAR file and the log files you want to parse.
- **Headless mode**: If you see errors related to `java.awt.HeadlessException`, double-check that `-Djava.awt.headless=true` is set.
- **Permissions**: Make sure the user running the MCP server has read permissions for the log files.
