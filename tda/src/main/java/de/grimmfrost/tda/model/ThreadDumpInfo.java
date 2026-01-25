/*
 * ThreadDumpInfo.java
 *
 * This file is part of TDA - Thread Dump Analysis Tool.
 *
 * TDA is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * TDA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with TDA; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: ThreadDumpInfo.java,v 1.11 2008-08-13 15:52:19 irockel Exp $
 */
package de.grimmfrost.tda.model;

import de.grimmfrost.tda.parser.Analyzer;

/**
 * Thread Dump Information Node. It stores structural data about the thread dump
 * and provides methods for generating html information for displaying infos about
 * the thread dump.
 * 
 * @author irockel
 */
public class ThreadDumpInfo extends AbstractInfo {
    private int logLine;
    private int overallThreadsWaitingWithoutLocksCount;
    
    private String startTime;
    private String overview;
    private String smrInfo;
    private Analyzer dumpAnalyzer;
    
    private Category waitingThreads;
    private Category sleepingThreads;
    private Category lockingThreads;
    private Category monitors;
    private Category monitorsWithoutLocks;
    private Category virtualThreads;
    private Category blockingMonitors;
    private Category threads;
    private Category deadlocks;
    private HeapInfo heapInfo;
    
    
    public ThreadDumpInfo(String name, int lineCount) {
        setName(name);
        this.logLine = lineCount;
    }
    
    /**
     * get the log line where to find the starting
     * point of this thread dump in the log file
     * @return starting point of thread dump in logfile, 0 if none set.
     */
    public int getLogLine() {
        return logLine;
    }

    /**
     * set the log line where to find the dump in the logfile.
     * @param logLine
     */
    public void setLogLine(int logLine) {
        this.logLine = logLine;
    }
    
    /**
     * get the approx. start time of the dump represented by this
     * node.
     * @return start time as string, format may differ as it is just
     *         parsed from the log file.
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * set the start time as string, can be of any format.
     * @param startTime the start time as string.
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * get the overview information of this thread dump.
     * @return overview information.
     */
    public String getOverview() {
        if(overview == null) {
            createOverview();
        }
        return overview;
    }

    /**
     * creates the overview information for this thread dump.
     */
    private void createOverview() {
        int threadsCount = getThreads() == null ? 0 : getThreads().getNodeCount();
        int monitorsCount = getMonitors() == null ? 0 : getMonitors().getNodeCount();
        int waitingCount = getWaitingThreads() == null ? 0 : getWaitingThreads().getNodeCount();
        int lockingCount = getLockingThreads() == null ? 0 : getLockingThreads().getNodeCount();
        int sleepingCount = getSleepingThreads() == null ? 0 : getSleepingThreads().getNodeCount();
        int deadlocksCount = getDeadlocks() == null ? 0 : getDeadlocks().getNodeCount();
        int monitorsNoLockCount = getMonitorsWithoutLocks() == null ? 0 : getMonitorsWithoutLocks().getNodeCount();

        String bgColor = "#ffffff";
        String textColor = "#333333";
        String headerColor = "#2c3e50";
        String borderColor = "#dee2e6";
        String chartBgColor = "#f8f9fa";
        String tableAltRowColor = "#fcfcfc";
        String hintBgColor = "#fff3cd";
        String hintBorderColor = "#ffeeba";
        String hintTextColor = "#856404";

        StringBuilder statData = new StringBuilder();
        statData.append("<html><body style=\"background-color: ").append(bgColor).append("; font-family: sans-serif; margin: 20px; color: ").append(textColor).append(";\">");
        
        statData.append("<h2 style=\"color: ").append(headerColor).append("; border-bottom: 2px solid #3498db; padding-bottom: 10px;\">Thread Dump Overview</h2>");

        // Thread State Distribution (Visual Chart)
        if (threadsCount > 0) {
            // ... (keep state extraction logic as is)
            java.util.Map<String, Integer> stateDistribution = new java.util.HashMap<>();
            Category threadsCat = getThreads();
            for (int i = 0; i < threadsCount; i++) {
                javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) threadsCat.getNodeAt(i);
                ThreadInfo ti = (ThreadInfo) node.getUserObject();
                String[] tokens = ti.getTokens();
                String state = "UNKNOWN";
                if (tokens != null) {
                    if (tokens.length >= 7) {
                        state = tokens.length == 3 ? tokens[2] : "OTHER";
                    } else if (tokens.length == 3) {
                        state = tokens[2];
                    }
                }
                
                if ("UNKNOWN".equals(state) || "OTHER".equals(state)) {
                    String name = ti.getName();
                    if (name.contains("state=")) {
                        int start = name.indexOf("state=") + 6;
                        int end = name.indexOf(' ', start);
                        state = end > start ? name.substring(start, end) : name.substring(start);
                    } else if (ti.getContent().contains("java.lang.Thread.State: ")) {
                        String content = ti.getContent();
                        int start = content.indexOf("java.lang.Thread.State: ") + 24;
                        int end = content.indexOf('\n', start);
                        state = content.substring(start, end).trim();
                        if (state.indexOf(' ') > 0) {
                            state = state.substring(0, state.indexOf(' '));
                        }
                    }
                }
                
                state = state.toUpperCase();
                stateDistribution.put(state, stateDistribution.getOrDefault(state, 0) + 1);
            }

            statData.append("<div style=\"margin-bottom: 25px; padding: 15px; background-color: ").append(chartBgColor).append("; border-radius: 8px; border: 1px solid ").append(borderColor).append(";\">");
            statData.append("<h4 style=\"margin-top: 0; color: ").append(textColor).append(";\">Thread State Distribution (").append(threadsCount).append(" threads)</h4>");
            statData.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"height: 30px; border: 1px solid ").append(borderColor).append("; border-radius: 4px; overflow: hidden;\"><tr>");
            
            String[] commonStates = {"RUNNABLE", "WAITING", "TIMED_WAITING", "BLOCKED", "PARKING"};
            String[] colors = {"#28a745", "#ffc107", "#fd7e14", "#dc3545", "#6f42c1"};
            
            int accounted = 0;
            for (int i = 0; i < commonStates.length; i++) {
                int count = stateDistribution.getOrDefault(commonStates[i], 0);
                if (count > 0) {
                    double percent = (count * 100.0) / threadsCount;
                    statData.append("<td width=\"").append(percent).append("%\" bgcolor=\"").append(colors[i])
                            .append("\" title=\"").append(commonStates[i]).append(": ").append(count).append("\"></td>");
                    accounted += count;
                }
            }
            
            int otherCount = threadsCount - accounted;
            if (otherCount > 0) {
                double percent = (otherCount * 100.0) / threadsCount;
                statData.append("<td width=\"").append(percent).append("%\" bgcolor=\"#6c757d\" title=\"OTHER: ").append(otherCount).append("\"></td>");
            }
            statData.append("</tr></table>");
            
            // Legend
            statData.append("<div style=\"margin-top: 10px; font-size: 11px;\">");
            for (int i = 0; i < commonStates.length; i++) {
                int count = stateDistribution.getOrDefault(commonStates[i], 0);
                if (count > 0) {
                    statData.append("<span style=\"display: inline-block; width: 10px; height: 10px; background-color: ").append(colors[i]).append("; margin-right: 4px;\"></span>")
                            .append(commonStates[i]).append(" (").append(count).append(")&nbsp;&nbsp;&nbsp;");
                }
            }
            if (otherCount > 0) {
                statData.append("<span style=\"display: inline-block; width: 10px; height: 10px; background-color: #6c757d; margin-right: 4px;\"></span>")
                        .append("OTHER (").append(otherCount).append(")");
            }
            statData.append("</div></div>");
        }

        // Statistics Table
        statData.append("<table width=\"100%\" style=\"border-collapse: collapse; margin-bottom: 20px;\">");
        
        statData.append("<tr>");
        statData.append("<td width=\"50%\" style=\"padding: 8px; border-bottom: 1px solid ").append(borderColor).append("; background-color: ").append(tableAltRowColor).append(";\"><b>Overall Monitor Count:</b> ").append(monitorsCount).append("</td>");
        statData.append("<td width=\"50%\" style=\"padding: 8px; border-bottom: 1px solid ").append(borderColor).append("; background-color: ").append(tableAltRowColor).append(";\"><b>Deadlocks:</b> <span style=\"").append(deadlocksCount > 0 ? "color: #dc3545; font-weight: bold;" : "").append("\">").append(deadlocksCount).append("</span></td>");
        statData.append("</tr>");
        
        if (getSmrInfo() != null) {
            statData.append("<tr>");
            statData.append("<td colspan=\"2\" style=\"padding: 8px; border-bottom: 1px solid ").append(borderColor).append(";\"><b>Threads class SMR info:</b><br><pre style=\"margin: 5px; font-size: 11px; white-space: pre-wrap;\">").append(getSmrInfo()).append("</pre></td>");
            statData.append("</tr>");
        }
        
        statData.append("<tr>");
        statData.append("<td style=\"padding: 8px; border-bottom: 1px solid ").append(borderColor).append(";\"><b>Threads locking:</b> ").append(lockingCount).append("</td>");
        statData.append("<td style=\"padding: 8px; border-bottom: 1px solid ").append(borderColor).append(";\"><b>Monitors without locking:</b> ").append(monitorsNoLockCount).append("</td>");
        statData.append("</tr>");

        statData.append("<tr>");
        statData.append("<td style=\"padding: 8px; border-bottom: 1px solid ").append(borderColor).append("; background-color: ").append(tableAltRowColor).append(";\"><b>Threads waiting:</b> ").append(waitingCount).append("</td>");
        statData.append("<td style=\"padding: 8px; border-bottom: 1px solid ").append(borderColor).append("; background-color: ").append(tableAltRowColor).append(";\"><b>Threads sleeping:</b> ").append(sleepingCount).append("</td>");
        statData.append("</tr>");
        
        statData.append("</table>");

        // Hints and Heap Info
        String hints = getDumpAnalyzer().analyzeDump();
        if (hints != null && !hints.isEmpty()) {
            statData.append("<div style=\"margin-top: 20px; padding: 15px; background-color: ").append(hintBgColor).append("; border: 1px solid ").append(hintBorderColor).append("; border-radius: 8px; color: ").append(hintTextColor).append(";\">");
            statData.append("<h4 style=\"margin-top: 0;\">Analysis Hints</h4>");
            statData.append("<table border=\"0\" width=\"100%\" style=\"color: ").append(hintTextColor).append(";\">").append(hints).append("</table>");
            statData.append("</div>");
        }
        
        if (getHeapInfo() != null) {
            statData.append("<div style=\"margin-top: 20px; padding: 15px; background-color: #e2e3e5; border: 1px solid ").append(borderColor).append("; border-radius: 8px;\">");
            statData.append("<h4 style=\"margin-top: 0;\">Heap Information</h4>");
            statData.append(getHeapInfo());
            statData.append("</div>");
        }

        statData.append("</body></html>");

        setOverview(statData.toString());
    }

    
    /**
     * generate a monitor info node from the given information.
     * @param locks how many locks are on this monitor?
     * @param waits how many threads are waiting for this monitor?
     * @param sleeps how many threads have a lock on this monitor and are sleeping?
     * @return a info node for the monitor.
     */
    public static String getMonitorInfo(int locks, int waits, int sleeps ) {
        String bgColor = "#ffffff";
        String textColor = "#333333";
        String tableBgColor = "#f8f9fa";
        String borderColor = "#dee2e6";
        String altRowColor = "#f2f2f2";
        String alertBgColor = "#f8d7da";
        String alertBorderColor = "#dc3545";
        String alertTextColor = "#721c24";
        String noticeBgColor = "#e9ecef";
        String noticeBorderColor = "#6c757d";

        StringBuilder statData = new StringBuilder();
        statData.append("<html><body style=\"background-color: ").append(bgColor).append("; font-family: sans-serif; margin: 10px; color: ").append(textColor).append(";\">");
        statData.append("<table width=\"100%\" style=\"border-collapse: collapse; background-color: ").append(tableBgColor).append("; border: 1px solid ").append(borderColor).append(";\">");
        
        addMonitorStatRow(statData, "Threads locking monitor", locks, bgColor);
        addMonitorStatRow(statData, "Threads sleeping on monitor", sleeps, altRowColor);
        addMonitorStatRow(statData, "Threads waiting to lock monitor", waits, bgColor);
        
        statData.append("</table>");

        if (locks == 0) {
            statData.append("<div style=\"margin-top: 15px; padding: 10px; background-color: ").append(noticeBgColor).append("; border-left: 5px solid ").append(noticeBorderColor).append("; font-size: 12px;\">");
            statData.append("<p><b>No locking thread detected.</b> Possible reasons:</p>");
            statData.append("<ul><li>A VM Thread is holding it.</li>");
            statData.append("<li>It is a <tt>java.util.concurrent</tt> lock and -XX:+PrintConcurrentLocks is missing.</li>");
            statData.append("<li>It is a custom lock not based on <tt>AbstractOwnableSynchronizer</tt>.</li></ul>");
            statData.append("<p>If many monitors have no locking thread, the garbage collector might be running.</p>");
            statData.append("<p>Check the <a href=\"dump://\">dump node</a> for more info.</p>");
            statData.append("</div>");
        }
        
        if (areALotOfWaiting(waits)) {
            statData.append("<div style=\"margin-top: 15px; padding: 10px; background-color: ").append(alertBgColor).append("; border-left: 5px solid ").append(alertBorderColor).append("; color: ").append(alertTextColor).append("; font-size: 12px;\">");
            statData.append("<p><b>High congestion!</b> A lot of threads are waiting for this monitor.</p>");
            statData.append("<p>Analyze other blocked locks as well, as there might be a chain of waiting threads.</p>");
            statData.append("</div>");
        }
        
        statData.append("</body></html>");

        return statData.toString();
    }

    private static void addMonitorStatRow(StringBuilder sb, String label, int value, String bgColor) {
        sb.append("<tr style=\"background-color: ").append(bgColor).append(";\">");
        sb.append("<td style=\"padding: 8px; border-bottom: 1px solid #dee2e6;\">").append(label).append("</td>");
        sb.append("<td style=\"padding: 8px; border-bottom: 1px solid #dee2e6; text-align: right;\"><b>").append(value).append("</b></td>");
        sb.append("</tr>");
    }
    
    /**
     * checks if a lot of threads are waiting
     * @param waits the wait to check
     * @return true if a lot of threads are waiting.
     */
    public static boolean areALotOfWaiting(int waits) {
        return(waits > 5);
    }
    
    /**
     * set the overview information of this thread dump.
     * @param overview the infos to be displayed (in html)
     */
    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Category getWaitingThreads() {
        return waitingThreads;
    }

    public void setWaitingThreads(Category waitingThreads) {
        this.waitingThreads = waitingThreads;
    }

    public Category getSleepingThreads() {
        return sleepingThreads;
    }

    public void setSleepingThreads(Category sleepingThreads) {
        this.sleepingThreads = sleepingThreads;
    }

    public Category getLockingThreads() {
        return lockingThreads;
    }

    public void setLockingThreads(Category lockingThreads) {
        this.lockingThreads = lockingThreads;
    }

    public Category getMonitors() {
        return monitors;
    }

    public void setMonitors(Category monitors) {
        this.monitors = monitors;
    }

    public Category getVirtualThreads() {
        return virtualThreads;
    }

    public void setVirtualThreads(Category virtualThreads) {
        this.virtualThreads = virtualThreads;
    }

    public Category getBlockingMonitors() {
      return blockingMonitors;
    }

    public void setBlockingMonitors(Category blockingMonitors) {
      this.blockingMonitors = blockingMonitors;
    }

    public Category getMonitorsWithoutLocks() {
        return monitorsWithoutLocks;
    }

    public void setMonitorsWithoutLocks(Category monitorsWithoutLocks) {
        this.monitorsWithoutLocks = monitorsWithoutLocks;
    }
    
    public Category getThreads() {
        return threads;
    }

    public void setThreads(Category threads) {
        this.threads = threads;
    }

    public Category getDeadlocks() {
        return deadlocks;
    }

    public void setDeadlocks(Category deadlocks) {
        this.deadlocks = deadlocks;
    }

    private Analyzer getDumpAnalyzer() {
        if(dumpAnalyzer == null) {
            setDumpAnalyzer(new Analyzer(this));
        }
        return dumpAnalyzer;
    }

    private void setDumpAnalyzer(Analyzer dumpAnalyzer) {
        this.dumpAnalyzer = dumpAnalyzer;
    }
    
    public int getOverallThreadsWaitingWithoutLocksCount() {
        return overallThreadsWaitingWithoutLocksCount;
    }

    public void setOverallThreadsWaitingWithoutLocksCount(int overallThreadsWaitingWithoutLocksCount) {
        this.overallThreadsWaitingWithoutLocksCount = overallThreadsWaitingWithoutLocksCount;
    }
    
    /**
     * add given category to the custom category.
     * @param cat
     */
    public void addToCustomCategories(Category cat) {
        
    }

    /**
     * get the set heap info
     * @return the set heap info object (only available if the thread
     *         dump is from Sun JDK 1.6 so far.
     */
    public HeapInfo getHeapInfo() {
        return(heapInfo);
    }

    /**
     * set the heap information for this thread dump.
     * @param value the heap information as string.
     */
    public void setHeapInfo(HeapInfo value) {
        heapInfo = value;
    }

    /**
     * get the SMR info of this thread dump.
     * @return SMR info.
     */
    public String getSmrInfo() {
        return smrInfo;
    }

    /**
     * set the SMR info of this thread dump.
     * @param smrInfo the SMR info.
     */
    public void setSmrInfo(String smrInfo) {
        this.smrInfo = smrInfo;
    }

    /**
     * string representation of this node, is used to displayed the node info
     * in the tree.
     * @return the thread dump information (one line).
     */
    public String toString() {
        StringBuffer postFix = new StringBuffer();
        if(logLine > 0) {
            postFix.append(" at line " + getLogLine());
        }
        if(startTime != null) {
            postFix.append(" around " + startTime);
        }
        return(getName() +  postFix);
    }
    

}
