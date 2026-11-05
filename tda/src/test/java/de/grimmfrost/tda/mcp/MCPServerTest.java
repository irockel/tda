package de.grimmfrost.tda.mcp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class MCPServerTest {

    private ByteArrayOutputStream buffer;

    @BeforeEach
    public void setUp() {
        buffer = new ByteArrayOutputStream();
        MCPServer.setOutput(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    public void tearDown() {
        MCPServer.setOutput(System.out);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

    private JsonObject singleReply(String line) {
        buffer.reset();
        MCPServer.handleLine(line);
        String out = output();
        assertFalse(out.isEmpty(), "Expected a reply on stdout for " + line);
        assertEquals(1, out.split("\n").length, "Expected exactly one reply for " + line);
        return JsonParser.parseString(out).getAsJsonObject();
    }

    @Test
    public void testPingReturnsEmptyResult() {
        JsonObject reply = singleReply("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"ping\"}");
        assertEquals(2, reply.get("id").getAsInt());
        assertTrue(reply.getAsJsonObject("result").entrySet().isEmpty());
        assertFalse(reply.has("error"));
    }

    @Test
    public void testNotificationGetsNoReply() {
        MCPServer.handleLine("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        assertEquals("", output());
    }

    @Test
    public void testUnknownMethodErrorGoesToOutput() {
        JsonObject reply = singleReply("{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"resources/list\"}");
        assertEquals(3, reply.get("id").getAsInt());
        assertEquals(-32601, reply.getAsJsonObject("error").get("code").getAsInt());
    }

    @Test
    public void testUnknownToolErrorGoesToOutput() {
        JsonObject reply = singleReply("{\"jsonrpc\":\"2.0\",\"id\":4,\"method\":\"tools/call\","
                + "\"params\":{\"name\":\"no_such_tool\",\"arguments\":{}}}");
        assertEquals(4, reply.get("id").getAsInt());
        assertTrue(reply.has("error"));
    }

    @Test
    public void testStringIdIsEchoedBack() {
        JsonObject reply = singleReply("{\"jsonrpc\":\"2.0\",\"id\":\"abc\",\"method\":\"tools/list\"}");
        assertTrue(reply.get("id").getAsJsonPrimitive().isString());
        assertEquals("abc", reply.get("id").getAsString());
        assertTrue(reply.getAsJsonObject("result").has("tools"));

        reply = singleReply("{\"jsonrpc\":\"2.0\",\"id\":\"123\",\"method\":\"ping\"}");
        assertTrue(reply.get("id").getAsJsonPrimitive().isString());
        assertEquals("123", reply.get("id").getAsString());
    }

    @Test
    public void testStringIdOnErrorDoesNotThrow() {
        JsonObject reply = singleReply("{\"jsonrpc\":\"2.0\",\"id\":\"abc\",\"method\":\"resources/list\"}");
        assertEquals("abc", reply.get("id").getAsString());
        assertTrue(reply.has("error"));
    }

    @Test
    public void testInvalidJsonReturnsParseError() {
        JsonObject reply = singleReply("{not json");
        assertTrue(reply.get("id").isJsonNull());
        assertEquals(-32700, reply.getAsJsonObject("error").get("code").getAsInt());
    }
}
