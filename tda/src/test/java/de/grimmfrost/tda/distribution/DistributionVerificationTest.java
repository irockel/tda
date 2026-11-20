package de.grimmfrost.tda.distribution;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class DistributionVerificationTest {

    private static Path repoRoot;
    private static Path launcherPath;

    @BeforeAll
    public static void setUp() {
        repoRoot = locateRepoRoot();
        assertNotNull(repoRoot, "Failed to locate repository root directory");
        launcherPath = repoRoot.resolve("scripts").resolve("tda_launcher.py");
    }

    private static Path locateRepoRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve(".claude-plugin")) || Files.exists(current.resolve("skills"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    private JsonObject readJsonObject(Path path) throws Exception {
        assertTrue(Files.isRegularFile(path), "File does not exist: " + path);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static String getPython3Executable() {
        try {
            Process process = new ProcessBuilder("python3", "-c", "import sys; print(sys.executable)").start();
            if (process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0) {
                return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            }
        } catch (Exception ignored) {
        }
        return "python3";
    }

    private static boolean isPython3Available() {
        try {
            Process process = new ProcessBuilder("python3", "--version").start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    public void testClaudePluginManifest() throws Exception {
        Path manifestPath = repoRoot.resolve(".claude-plugin").resolve("plugin.json");
        JsonObject data = readJsonObject(manifestPath);

        assertEquals("tda-thread-dump-analysis", data.get("name").getAsString());
        assertTrue(Pattern.compile("^\\d+\\.\\d+\\.\\d+").matcher(data.get("version").getAsString()).find(),
                "Version must follow semantic versioning");
        assertFalse(data.get("description").getAsString().trim().isEmpty(), "Description must not be empty");
        assertEquals("LGPL-2.1", data.get("license").getAsString());

        assertTrue(data.has("skills") && data.get("skills").isJsonArray(), "skills must be an array");
        JsonArray skills = data.getAsJsonArray("skills");
        assertFalse(skills.isEmpty(), "skills array must not be empty");

        for (JsonElement skillElem : skills) {
            Path skillDir = repoRoot.resolve(skillElem.getAsString());
            assertTrue(Files.isDirectory(skillDir), "Skill directory must exist: " + skillDir);
            assertTrue(Files.isRegularFile(skillDir.resolve("SKILL.md")), "Missing SKILL.md in: " + skillDir);
        }
    }

    @Test
    public void testClaudeMarketplaceManifest() throws Exception {
        Path marketplacePath = repoRoot.resolve(".claude-plugin").resolve("marketplace.json");
        JsonObject data = readJsonObject(marketplacePath);

        assertEquals("tda-marketplace", data.get("name").getAsString());
        assertTrue(data.has("plugins") && data.get("plugins").isJsonArray(), "plugins must be an array");
        JsonArray plugins = data.getAsJsonArray("plugins");
        assertFalse(plugins.isEmpty(), "plugins array must not be empty");

        JsonObject pluginEntry = plugins.get(0).getAsJsonObject();
        assertEquals("tda-thread-dump-analysis", pluginEntry.get("name").getAsString());
        JsonObject source = pluginEntry.getAsJsonObject("source");
        assertEquals("github", source.get("source").getAsString());
        assertEquals("irockel/tda", source.get("repo").getAsString());
    }

    @Test
    public void testPiCliBridgeRemoved() {
        Path clientScript = repoRoot.resolve(".agents").resolve("skills")
                .resolve("tda-thread-dump-analysis").resolve("scripts").resolve("tda_client.py");
        assertFalse(Files.exists(clientScript), "Obsolete tda_client.py should be removed");
    }

    @Test
    public void testCodexPluginManifest() throws Exception {
        Path manifestPath = repoRoot.resolve(".codex-plugin").resolve("plugin.json");
        JsonObject data = readJsonObject(manifestPath);

        assertEquals("tda-thread-dump-analysis", data.get("name").getAsString());
        assertTrue(Pattern.compile("^\\d+\\.\\d+\\.\\d+").matcher(data.get("version").getAsString()).find());
        assertFalse(data.get("description").getAsString().trim().isEmpty());

        String skillsPath = data.get("skills").getAsString();
        assertTrue(Files.exists(repoRoot.resolve(skillsPath)), "Codex skills path does not exist: " + skillsPath);

        JsonObject mcpServers = data.getAsJsonObject("mcpServers");
        assertNotNull(mcpServers, "Missing mcpServers in codex manifest");
        assertTrue(mcpServers.has("tda"), "mcpServers missing 'tda'");
        JsonObject tdaServer = mcpServers.getAsJsonObject("tda");
        assertEquals("python3", tdaServer.get("command").getAsString());

        JsonArray args = tdaServer.getAsJsonArray("args");
        boolean hasLauncher = false;
        for (JsonElement arg : args) {
            if (arg.getAsString().contains("tda_launcher.py")) {
                hasLauncher = true;
                break;
            }
        }
        assertTrue(hasLauncher, "Codex mcpServers args must reference tda_launcher.py");
    }

    @Test
    public void testRootMcpManifest() throws Exception {
        Path manifestPath = repoRoot.resolve(".mcp.json");
        JsonObject data = readJsonObject(manifestPath);

        JsonObject mcpServers = data.getAsJsonObject("mcpServers");
        assertNotNull(mcpServers, "Missing mcpServers in .mcp.json");
        assertTrue(mcpServers.has("tda"), "Missing 'tda' server in .mcp.json");

        JsonObject tda = mcpServers.getAsJsonObject("tda");
        assertEquals("python3", tda.get("command").getAsString());
        JsonArray args = tda.getAsJsonArray("args");
        boolean hasLauncher = false;
        for (JsonElement arg : args) {
            if (arg.getAsString().contains("tda_launcher.py")) {
                hasLauncher = true;
                break;
            }
        }
        assertTrue(hasLauncher, "Root .mcp.json must reference tda_launcher.py");
    }

    @Test
    public void testCursorMcpManifest() throws Exception {
        Path manifestPath = repoRoot.resolve(".cursor").resolve("mcp.json");
        JsonObject data = readJsonObject(manifestPath);

        JsonObject mcpServers = data.getAsJsonObject("mcpServers");
        assertNotNull(mcpServers, "Missing mcpServers in .cursor/mcp.json");
        assertTrue(mcpServers.has("tda"), "Missing 'tda' server in .cursor/mcp.json");

        JsonObject tda = mcpServers.getAsJsonObject("tda");
        assertEquals("python3", tda.get("command").getAsString());
        JsonArray args = tda.getAsJsonArray("args");
        boolean hasLauncher = false;
        for (JsonElement arg : args) {
            if (arg.getAsString().contains("tda_launcher.py")) {
                hasLauncher = true;
                break;
            }
        }
        assertTrue(hasLauncher, ".cursor/mcp.json must reference tda_launcher.py");
    }

    @Test
    public void testAgentSkillsSpecification() throws Exception {
        List<Path> skillFiles = Arrays.asList(
                repoRoot.resolve("skills").resolve("tda-thread-dump-analysis").resolve("SKILL.md"),
                repoRoot.resolve(".agents").resolve("skills").resolve("tda-thread-dump-analysis").resolve("SKILL.md")
        );

        for (Path skillPath : skillFiles) {
            assertTrue(Files.isRegularFile(skillPath), "Missing SKILL.md: " + skillPath);
            String content = Files.readString(skillPath, StandardCharsets.UTF_8);

            assertTrue(content.startsWith("---\n"), "SKILL.md must start with --- frontmatter");
            String[] parts = content.split("---", 3);
            assertTrue(parts.length >= 3, "Frontmatter must have opening and closing ---");

            String frontmatter = parts[1];
            assertTrue(frontmatter.contains("name: tda-thread-dump-analysis"), "Missing skill name");
            assertTrue(frontmatter.contains("description:"), "Missing skill description");

            String[] words = frontmatter.trim().split("\\s+");
            assertTrue(words.length < 80, "Frontmatter exceeds token budget (" + words.length + " words)");
        }
    }

    @Test
    public void testLauncherHelpAndCheck() throws Exception {
        assumeTrue(isPython3Available(), "python3 is not available in test environment");
        assertTrue(Files.isRegularFile(launcherPath), "Missing launcher script: " + launcherPath);
        assertTrue(Files.isExecutable(launcherPath), "Launcher script must be executable");

        // Test --help
        Process helpProc = new ProcessBuilder("python3", launcherPath.toString(), "--help")
                .redirectErrorStream(true)
                .start();
        String helpOutput = new String(helpProc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(helpProc.waitFor(10, TimeUnit.SECONDS));
        assertEquals(0, helpProc.exitValue());
        assertTrue(helpOutput.contains("TDA Universal Launcher"));

        // Test --check
        Process checkProc = new ProcessBuilder("python3", launcherPath.toString(), "--check")
                .redirectErrorStream(true)
                .start();
        String checkOutput = new String(checkProc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(checkProc.waitFor(10, TimeUnit.SECONDS));
        assertEquals(0, checkProc.exitValue());
        assertTrue(checkOutput.contains("Check OK: Java found"));
        assertTrue(checkOutput.contains("Check OK: TDA jar resolved"));
    }

    @Test
    public void testMcpHandshakeAndToolsList() throws Exception {
        assumeTrue(isPython3Available(), "python3 is not available in test environment");

        Process proc = new ProcessBuilder("python3", launcherPath.toString()).start();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {

            // 1. initialize
            String initReq = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2024-11-05\"}}\n";
            writer.write(initReq);
            writer.flush();

            String initLine = reader.readLine();
            assertNotNull(initLine, "No response received for initialize request");
            JsonObject initResp = JsonParser.parseString(initLine).getAsJsonObject();
            assertEquals(1, initResp.get("id").getAsInt());
            assertEquals("tda-mcp-server", initResp.getAsJsonObject("result").getAsJsonObject("serverInfo").get("name").getAsString());

            // 2. tools/list
            String toolsReq = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}\n";
            writer.write(toolsReq);
            writer.flush();

            String toolsLine = reader.readLine();
            assertNotNull(toolsLine, "No response received for tools/list request");
            JsonObject toolsResp = JsonParser.parseString(toolsLine).getAsJsonObject();
            assertEquals(2, toolsResp.get("id").getAsInt());

            JsonArray tools = toolsResp.getAsJsonObject("result").getAsJsonArray("tools");
            List<String> toolNames = new ArrayList<>();
            for (JsonElement tool : tools) {
                toolNames.add(tool.getAsJsonObject().get("name").getAsString());
            }

            List<String> expectedTools = Arrays.asList(
                    "parse_log",
                    "get_summary",
                    "check_deadlocks",
                    "analyze_virtual_threads",
                    "find_long_running",
                    "get_native_threads",
                    "get_zombie_threads"
            );

            for (String expected : expectedTools) {
                assertTrue(toolNames.contains(expected), "Missing tool in tools/list: " + expected);
            }
        } finally {
            proc.destroyForcibly();
            proc.waitFor(5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testMissingJavaActionableError() throws Exception {
        assumeTrue(isPython3Available(), "python3 is not available in test environment");

        Path emptyDir = Files.createTempDirectory("empty_path");
        try {
            String pythonBin = getPython3Executable();
            ProcessBuilder pb = new ProcessBuilder(pythonBin, launcherPath.toString(), "--check");
            Map<String, String> env = pb.environment();
            env.put("PATH", emptyDir.toString());
            env.put("HOME", Files.createTempDirectory("empty_home").toString());
            pb.redirectErrorStream(true);

            Process proc = pb.start();
            String output = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(proc.waitFor(10, TimeUnit.SECONDS));

            assertNotEquals(0, proc.exitValue());
            assertTrue(output.contains("Java runtime ('java') was not found in PATH"));
            assertTrue(output.contains("TDA requires Java 11 or higher"));
        } finally {
            deleteRecursively(emptyDir);
        }
    }

    @Test
    public void testAirGappedOfflineError() throws Exception {
        assumeTrue(isPython3Available(), "python3 is not available in test environment");

        Path tempDir = Files.createTempDirectory("tda_offline_test");
        try {
            Path launcherCopy = tempDir.resolve("tda_launcher.py");
            Files.copy(launcherPath, launcherCopy);

            ProcessBuilder pb = new ProcessBuilder("python3", launcherCopy.toString(), "--check");
            pb.directory(tempDir.toFile());
            Map<String, String> env = pb.environment();
            env.put("HOME", tempDir.toString());
            env.put("http_proxy", "http://127.0.0.1:9");
            env.put("https_proxy", "http://127.0.0.1:9");
            pb.redirectErrorStream(true);

            Process proc = pb.start();
            String output = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(proc.waitFor(10, TimeUnit.SECONDS));

            assertNotEquals(0, proc.exitValue());
            assertTrue(output.contains("In offline or network air-gapped environments"));
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private static void deleteRecursively(Path path) {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (Exception ignored) {
                            }
                        });
            } catch (Exception ignored) {
            }
        }
    }
}
