package de.grimmfrost.tda.mcp;

import de.grimmfrost.tda.*;
import de.grimmfrost.tda.utils.DateMatcher;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.*;
import java.util.*;

/**
 * Headless analysis provider for TDA.
 */
public class HeadlessAnalysisProvider {
    private Map<String, Map> threadStore = new HashMap<>();
    private List<DefaultMutableTreeNode> topNodes = new ArrayList<>();
    private String currentLogFile;

    public void parseLogFile(String filePath) throws IOException {
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

        // AbstractDumpParser.findLongRunningThreads is protected or package private? 
        // No, it is public in AbstractDumpParser but SunJDKParser might override it.
        // Actually it's public in AbstractDumpParser.
        
        // We need a parser instance to call findLongRunningThreads
        // Let's use the first one if possible or just any SunJDKParser
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

    public void clear() {
        threadStore.clear();
        topNodes.clear();
        currentLogFile = null;
    }
}
