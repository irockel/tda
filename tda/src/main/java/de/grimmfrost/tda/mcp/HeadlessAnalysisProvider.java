package de.grimmfrost.tda.mcp;

import de.grimmfrost.tda.*;
import de.grimmfrost.tda.model.Category;
import de.grimmfrost.tda.model.ThreadDumpInfo;
import de.grimmfrost.tda.model.ThreadInfo;
import de.grimmfrost.tda.parser.DumpParser;
import de.grimmfrost.tda.parser.DumpParserFactory;
import de.grimmfrost.tda.parser.SunJDKParser;
import de.grimmfrost.tda.utils.DateMatcher;
import de.grimmfrost.tda.utils.LogManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Headless analysis provider for TDA.
 */
public class HeadlessAnalysisProvider {
    private static final Logger LOGGER = LogManager.getLogger(HeadlessAnalysisProvider.class);
    private final Map<String, Map<String, String>> threadStore = new HashMap<>();
    private final List<DefaultMutableTreeNode> topNodes = new ArrayList<>();
    private String currentLogFile;

    public void parseLogFile(String filePath) throws IOException {
        LOGGER.info("Parsing log file: " + filePath);
        this.currentLogFile = filePath;
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Log file not found: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            DumpParser parser = DumpParserFactory.get().getDumpParserForLogfile(fis, threadStore, false, 0);
            if (parser == null) {
                throw new IOException("No suitable parser found for log file: " + filePath);
            }

            while (parser.hasMoreDumps()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) parser.parseNext();
                topNodes.add(node);
            }
        }
    }

    public List<Map<String, Object>> getDumpsSummary() {
        List<Map<String, Object>> summary = new ArrayList<>();
        for (int i = 0; i < topNodes.size(); i++) {
            DefaultMutableTreeNode node = topNodes.get(i);
            ThreadDumpInfo tdi = (ThreadDumpInfo) node.getUserObject();
            Map<String, Object> info = new HashMap<>();
            info.put("index", i);
            info.put("name", tdi.getName());
            info.put("time", tdi.getStartTime());
            info.put("threadCount", tdi.getThreads() != null ? tdi.getThreads().getNodeCount() : 0);
            info.put("deadlockCount", tdi.getDeadlocks() != null ? tdi.getDeadlocks().getNodeCount() : 0);
            if (tdi.getSmrInfo() != null) {
                info.put("smrInfo", tdi.getSmrInfo());
            }
            summary.add(info);
        }
        return summary;
    }

    public List<String> checkForDeadlocks() {
        List<String> results = new ArrayList<>();
        for (DefaultMutableTreeNode node : topNodes) {
            ThreadDumpInfo tdi = (ThreadDumpInfo) node.getUserObject();
            if (tdi.getDeadlocks() != null && tdi.getDeadlocks().getNodeCount() > 0) {
                results.add("Deadlock found in dump '" + tdi.getName() + "': " + tdi.getDeadlocks().getName());
            }
        }
        if (results.isEmpty()) {
            results.add("No deadlocks found in " + topNodes.size() + " dumps.");
        }
        return results;
    }

    public List<String> findLongRunningThreads() {
        if (topNodes.size() < 2) {
            return Collections.singletonList("At least two dumps are required to find long running threads.");
        }

        // Using the same logic as in TDA.findLongRunningThreads but adapted for headless
        DefaultMutableTreeNode mergeRoot = (DefaultMutableTreeNode) topNodes.get(0).getParent();
        // Wait, topNodes are parsed next, they might not have a common parent unless we add them to one.
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        for (DefaultMutableTreeNode node : topNodes) {
            root.add(node);
        }

        TreePath[] paths = new TreePath[topNodes.size()];
        for (int i = 0; i < topNodes.size(); i++) {
            paths[i] = new TreePath(topNodes.get(i).getPath());
        }

        // AbstractDumpParser.findLongRunningThreads is public in AbstractDumpParser.
        
        // We need a parser instance to call findLongRunningThreads
        // Let's use any SunJDKParser
        SunJDKParser dummyParser = new SunJDKParser(null, threadStore, 0, false, 0, new DateMatcher());
        
        DefaultMutableTreeNode longRunningRoot = new DefaultMutableTreeNode("Long Running Threads");
        dummyParser.findLongRunningThreads(longRunningRoot, threadStore, paths, topNodes.size(), null);

        List<String> results = new ArrayList<>();
        if (longRunningRoot.getChildCount() == 0) {
            results.add("No long running threads found across " + topNodes.size() + " dumps.");
        } else {
            for (int i = 0; i < longRunningRoot.getChildCount(); i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) longRunningRoot.getChildAt(i);
                results.add("Long running thread: " + node.getUserObject().toString());
            }
        }
        return results;
    }

    public List<String> analyzeVirtualThreads() {
        List<String> results = new ArrayList<>();
        int totalStuck = 0;
        for (DefaultMutableTreeNode node : topNodes) {
            ThreadDumpInfo tdi = (ThreadDumpInfo) node.getUserObject();
            int stuckCarrierThreads = 0;
            Category threadsCat = tdi.getThreads();
            if (threadsCat != null) {
                int threadCount = threadsCat.getNodeCount();
                for (int i = 0; i < threadCount; i++) {
                    DefaultMutableTreeNode threadNode = (DefaultMutableTreeNode) threadsCat.getNodeAt(i);
                    ThreadInfo ti = (ThreadInfo) threadNode.getUserObject();
                    if (ti.getContent().contains("carrier thread seems to be stuck in application code")) {
                        stuckCarrierThreads++;
                        results.add("Stuck carrier thread in dump '" + tdi.getName() + "': " + ti.getName());
                    }
                }
            }
            totalStuck += stuckCarrierThreads;
        }

        if (totalStuck == 0) {
            results.add("No virtual threads with stuck carrier threads detected in " + topNodes.size() + " dumps.");
        }
        return results;
    }

    public List<Map<String, String>> getNativeThreads(int dumpIndex) {
        if (dumpIndex < 0 || dumpIndex >= topNodes.size()) {
            throw new IllegalArgumentException("Invalid dump index: " + dumpIndex);
        }

        List<Map<String, String>> nativeThreads = new ArrayList<>();
        DefaultMutableTreeNode dumpNode = topNodes.get(dumpIndex);
        ThreadDumpInfo tdi = (ThreadDumpInfo) dumpNode.getUserObject();
        
        collectNativeThreads(tdi.getThreads(), nativeThreads);
        collectNativeThreads(tdi.getVirtualThreads(), nativeThreads);
        
        return nativeThreads;
    }

    public List<Map<String, String>> getZombieThreads() {
        List<Map<String, String>> results = new ArrayList<>();
        for (DefaultMutableTreeNode node : topNodes) {
            ThreadDumpInfo tdi = (ThreadDumpInfo) node.getUserObject();
            List<String> unresolved = tdi.getUnresolvedSmrAddresses();
            if (unresolved != null && !unresolved.isEmpty()) {
                for (String addr : unresolved) {
                    Map<String, String> entry = new HashMap<>();
                    entry.put("address", addr);
                    entry.put("dumpName", tdi.getName());
                    entry.put("timestamp", tdi.getStartTime() != null ? tdi.getStartTime() : "unknown");
                    results.add(entry);
                }
            }
        }
        return results;
    }

    private void collectNativeThreads(Category cat, List<Map<String, String>> nativeThreads) {
        if (cat != null) {
            int threadCount = cat.getNodeCount();
            for (int i = 0; i < threadCount; i++) {
                DefaultMutableTreeNode threadNode = cat.getNodeAt(i);
                if (threadNode != null) {
                    ThreadInfo ti = (ThreadInfo) threadNode.getUserObject();
                    String content = ti.getContent();
                    
                    if (content.contains("Native Method")) {
                        Map<String, String> threadMap = new HashMap<>();
                        threadMap.put("threadName", ti.getName());
                        
                        // Extract native method and library info
                        // e.g., at java.net.PlainSocketImpl.socketAccept(java.base@21.0.2/Native Method)
                        String[] lines = content.split("\n");
                        for (String line : lines) {
                            if (line.contains("Native Method")) {
                                int atIdx = line.indexOf("at ");
                                if (atIdx >= 0) {
                                    String methodPart = line.substring(atIdx + 3).trim();
                                    threadMap.put("nativeMethod", methodPart);
                                    break;
                                }
                            }
                        }
                        nativeThreads.add(threadMap);
                    }
                }
            }
        }
    }

    public void clear() {
        threadStore.clear();
        topNodes.clear();
        currentLogFile = null;
    }
}
