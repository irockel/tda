package de.grimmfrost.tda.mcp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;
import de.grimmfrost.tda.*;

public class HeadlessAnalysisProviderTest {

    @Test
    public void testDeadlockDetection() throws Exception {
        HeadlessAnalysisProvider provider = new HeadlessAnalysisProvider();
        // Using existing test resource
        String logPath = "src/test/resources/deadlock.log";
        File logFile = new File(logPath);
        if (!logFile.exists()) {
            System.out.println("[DEBUG_LOG] Skip test, deadlock.log not found");
            return;
        }

        provider.parseLogFile(logPath);
        List<String> deadlocks = provider.checkForDeadlocks();
        
        boolean found = false;
        for (String msg : deadlocks) {
            if (msg.contains("Deadlock found")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Should find deadlocks in deadlock.log");
    }

    @Test
    public void testSummary() throws Exception {
        HeadlessAnalysisProvider provider = new HeadlessAnalysisProvider();
        String logPath = "src/test/resources/hpdump.log";
        File logFile = new File(logPath);
        if (!logFile.exists()) {
             System.out.println("[DEBUG_LOG] Skip test, hpdump.log not found");
             return;
        }

        provider.parseLogFile(logPath);
        List<Map<String, Object>> summary = provider.getDumpsSummary();
        assertEquals(2, summary.size());
        assertTrue(summary.get(0).get("name").toString().contains("Dump"));
    }

    @Test
    public void testVirtualThreadAnalysis() throws Exception {
        HeadlessAnalysisProvider provider = new HeadlessAnalysisProvider();
        String logPath = "src/test/resources/carrier_stuck.log";
        File logFile = new File(logPath);
        if (!logFile.exists()) {
             System.out.println("[DEBUG_LOG] Skip test, carrier_stuck.log not found");
             return;
        }

        provider.parseLogFile(logPath);
        List<String> results = provider.analyzeVirtualThreads();
        
        boolean found = false;
        for (String msg : results) {
            if (msg.contains("Stuck carrier thread")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Should find stuck carrier thread in carrier_stuck.log");
    }

    @Test
    public void testNativeThreadAnalysis() throws Exception {
        HeadlessAnalysisProvider provider = new HeadlessAnalysisProvider();
        String logPath = "src/test/resources/java21dump.log";
        File logFile = new File(logPath);
        if (!logFile.exists()) {
             System.out.println("[DEBUG_LOG] Skip test, java21dump.log not found");
             return;
        }

        provider.parseLogFile(logPath);
        List<Map<String, String>> nativeThreads = provider.getNativeThreads(0);
        
        assertFalse(nativeThreads.isEmpty(), "Should find native threads in java21dump.log");
        
        boolean foundSpecific = false;
        for (Map<String, String> thread : nativeThreads) {
            if (thread.get("threadName").contains("main") && 
                thread.get("nativeMethod").contains("java.net.PlainSocketImpl.socketAccept(java.base@21.0.2/Native Method)")) {
                foundSpecific = true;
                break;
            }
        }
        assertTrue(foundSpecific, "Should find specific native method with library info");
    }

    @Test
    public void testZombieThreadAnalysis() throws Exception {
        HeadlessAnalysisProvider provider = new HeadlessAnalysisProvider();
        String logPath = "src/test/resources/jstack_dump.log";
        File logFile = new File(logPath);
        if (!logFile.exists()) {
             System.out.println("[DEBUG_LOG] Skip test, jstack_dump.log not found");
             return;
        }

        provider.parseLogFile(logPath);
        
        // The original jstack_dump.log has no zombies.
        List<Map<String, String>> results = (List) provider.getZombieThreads();
        assertTrue(results.isEmpty(), "Should report no zombie threads for clean dump");

        // Now we need a dump with zombies. We can manually create a temporary file.
        File tempFile = File.createTempFile("zombie", ".log");
        java.nio.file.Files.write(tempFile.toPath(), ("2026-01-20 17:29:40\n" +
                "Full thread dump OpenJDK 64-Bit Server VM (21.0.9+10-LTS mixed mode, sharing):\n" +
                "\n" +
                "Threads class SMR info:\n" +
                "_java_thread_list=0x000000087e826560, length=2, elements={\n" +
                "0x000000010328e320, 0x00000001deadbeef\n" +
                "}\n" +
                "\n" +
                "\"Reference Handler\" #9 [30467] daemon prio=10 os_prio=31 cpu=0.44ms elapsed=25574.11s tid=0x000000010328e320 nid=30467 waiting on condition  [0x000000016e7c2000]\n" +
                "   java.lang.Thread.State: RUNNABLE\n").getBytes());

        try {
            provider.clear();
            provider.parseLogFile(tempFile.getAbsolutePath());
            results = (List) provider.getZombieThreads();
            
            boolean found = false;
            for (Map<String, String> msg : results) {
                if ("0x00000001deadbeef".equals(msg.get("address"))) {
                    found = true;
                    assertEquals("2026-01-20 17:29:40", msg.get("timestamp"));
                    assertNotNull(msg.get("dumpName"));
                    break;
                }
            }
            assertTrue(found, "Should find zombie thread 0x00000001deadbeef with timestamp");
        } finally {
            tempFile.delete();
        }
    }
}
