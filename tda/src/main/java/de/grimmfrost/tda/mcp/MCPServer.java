package de.grimmfrost.tda.mcp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.grimmfrost.tda.utils.LogManager;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MCP Server for TDA thread dump analysis.
 */
public class MCPServer {
    private static final Logger LOGGER = LogManager.getLogger(MCPServer.class);
    private static final Gson gson = new Gson();
    // error replies must keep "id": null (e.g. for parse errors)
    private static final Gson errorGson = new GsonBuilder().serializeNulls().create();
    private static final HeadlessAnalysisProvider provider = new HeadlessAnalysisProvider();
    private static PrintStream out = System.out;

    public static void main(String[] args) {
        LogManager.init();
        System.setProperty("java.awt.headless", "true");
        
        // Output capabilities to stderr for debugging/logging if needed, 
        // but the main communication is via stdout.
        
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            handleLine(scanner.nextLine());
        }
    }

    /**
     * Process a single JSON-RPC message and write the reply (if any) to stdout.
     */
    static void handleLine(String line) {
        if (line.trim().isEmpty()) return;
        JsonObject request;
        try {
            request = JsonParser.parseString(line).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error parsing MCP request", e);
            sendError(JsonNull.INSTANCE, -32700, "Parse error");
            return;
        }
        if (!request.has("method")) return;
        // requests without an id are notifications and must not be answered
        boolean isNotification = !request.has("id");
        try {
            String method = request.get("method").getAsString();

            // Handle MCP Lifecycle methods
            if ("initialize".equals(method)) {
                handleInitialize(request);
                return;
            }
            if ("tools/list".equals(method)) {
                handleListTools(request);
                return;
            }
            if ("tools/call".equals(method)) {
                handleCallTool(request);
                return;
            }
            if ("ping".equals(method)) {
                sendResponse(request.get("id"), new JsonObject());
                return;
            }
            if (isNotification) return;

            // Fallback to old generic JSON-RPC for backward compatibility if needed
            JsonObject params = request.getAsJsonObject("params");
            Object result = handleGenericRequest(method, params);
            sendResponse(request.get("id"), result);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing MCP request", e);
            if (!isNotification) {
                int code = e instanceof UnsupportedOperationException ? -32601 : -32000;
                sendError(request.get("id"), code, e.getMessage());
            }
        }
    }

    static void setOutput(PrintStream output) {
        out = output;
    }

    private static void handleInitialize(JsonObject request) {
        JsonObject result = new JsonObject();
        result.addProperty("protocolVersion", "2024-11-05");
        JsonObject capabilities = new JsonObject();
        capabilities.add("tools", new JsonObject());
        result.add("capabilities", capabilities);
        JsonObject serverInfo = new JsonObject();
        serverInfo.addProperty("name", "tda-mcp-server");
        serverInfo.addProperty("version", "3.2.0");
        result.add("serverInfo", serverInfo);
        
        sendResponse(request.get("id"), result);
    }

    private static void handleListTools(JsonObject request) {
        JsonObject result = new JsonObject();
        List<JsonObject> tools = new ArrayList<>();
        
        tools.add(createTool("parse_log", "Parses a log file containing Java thread dumps. This has to be the first action for a log file containing thread dumps!",
            createProperty("path", "string", "The absolute path to the log file.")));
            
        tools.add(createTool("get_summary", "Returns a summary of all parsed thread dumps.", new JsonObject()));
        
        tools.add(createTool("check_deadlocks", "Checks for deadlocks in the parsed thread dumps.", new JsonObject()));
        
        tools.add(createTool("find_long_running", "Identifies threads that appear in multiple consecutive thread dumps.", new JsonObject()));
        
        tools.add(createTool("analyze_virtual_threads", "Detects virtual threads where the carrier thread is stuck in application code.", new JsonObject()));
        
        tools.add(createTool("get_native_threads", "Returns a list of all threads currently in a native method for a specific thread dump.", 
            createProperty("dump_index", "integer", "The index of the thread dump as retrieved from get_summary.")));

        tools.add(createTool("get_zombie_threads", "Returns a list of zombie threads (SMR addresses that could not be resolved to any thread).", new JsonObject()));

        result.add("tools", gson.toJsonTree(tools));
        sendResponse(request.get("id"), result);
    }

    private static JsonObject createTool(String name, String description, JsonObject inputSchema) {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", name);
        tool.addProperty("description", description);
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", inputSchema);
        tool.add("inputSchema", schema);
        return tool;
    }

    private static JsonObject createProperty(String name, String type, String description) {
        JsonObject props = new JsonObject();
        JsonObject prop = new JsonObject();
        prop.addProperty("type", type);
        prop.addProperty("description", description);
        props.add(name, prop);
        return props;
    }

    private static void handleCallTool(JsonObject request) throws IOException {
        JsonObject params = request.getAsJsonObject("params");
        String name = params.get("name").getAsString();
        JsonObject arguments = params.getAsJsonObject("arguments");
        
        Object resultData = handleGenericRequest(name, arguments);
        
        JsonObject result = new JsonObject();
        List<JsonObject> content = new ArrayList<>();
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", gson.toJson(resultData));
        content.add(textContent);
        result.add("content", gson.toJsonTree(content));
        
        sendResponse(request.get("id"), result);
    }

    private static Object handleGenericRequest(String method, JsonObject params) throws IOException {
        switch (method) {
            case "parse_log":
                String path = params.get("path").getAsString();
                provider.clear();
                provider.parseLogFile(path);
                return "Successfully parsed log file: " + path;
            case "get_summary":
                return provider.getDumpsSummary();
            case "check_deadlocks":
                return provider.checkForDeadlocks();
            case "find_long_running":
                return provider.findLongRunningThreads();
            case "analyze_virtual_threads":
                return provider.analyzeVirtualThreads();
            case "get_native_threads":
                int dumpIndex = params.get("dump_index").getAsInt();
                return provider.getNativeThreads(dumpIndex);
            case "get_zombie_threads":
                return provider.getZombieThreads();
            case "clear":
                provider.clear();
                return "Cleared thread store.";
            default:
                throw new UnsupportedOperationException("Unknown method: " + method);
        }
    }

    private static void sendResponse(JsonElement id, Object result) {
        if (id == null) return;
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", id);
        response.add("result", gson.toJsonTree(result));
        out.println(gson.toJson(response));
    }

    private static void sendError(JsonElement id, int code, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", id);
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);
        response.add("error", error);
        out.println(errorGson.toJson(response));
    }
}
