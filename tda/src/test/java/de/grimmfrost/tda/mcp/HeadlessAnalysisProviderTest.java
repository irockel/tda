package de.grimmfrost.tda.mcp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.io.File;

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
}
