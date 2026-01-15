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
package de.grimmfrost.tda;

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
    private Analyzer dumpAnalyzer;
    
    private Category waitingThreads;
    private Category sleepingThreads;
    private Category lockingThreads;
    private Category monitors;
    private Category monitorsWithoutLocks;
    private Category blockingMonitors;
    private Category threads;
    private Category deadlocks;
    private HeapInfo heapInfo;
    
    
    ThreadDumpInfo(String name, int lineCount) {
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
        StringBuffer statData = new StringBuffer("<body bgcolor=\"#ffffff\"><font face=System " +
                "><table border=0><tr bgcolor=\"#dddddd\"><td><font face=System " +
                ">Overall Thread Count</td><td width=\"150\"></td><td><b><font face=System>");
        statData.append(getThreads() == null? 0 : getThreads().getNodeCount());
        statData.append("</b></td></tr>\n\n<tr bgcolor=\"#eeeeee\"><td><font face=System" +
                ">Overall Monitor Count</td><td></td><td><b><font face=System>");
        statData.append(getMonitors() == null? 0 : getMonitors().getNodeCount());
        statData.append("</b></td></tr>\n\n<tr bgcolor=\"#dddddd\"><td><font face=System " +
                ">Number of threads waiting for a monitor</td><td></td><td><b><font face=System>");
        statData.append(getWaitingThreads() == null? 0 : getWaitingThreads().getNodeCount());
        statData.append("</b></td></tr>\n\n<tr bgcolor=\"#eeeeee\"><td><font face=System " +
                ">Number of threads locking a monitor</td><td></td><td><b><font face=System size>");
        statData.append(getLockingThreads() == null? 0 : getLockingThreads().getNodeCount());
        statData.append("</b></td></tr>\n\n<tr bgcolor=\"#dddddd\"><td><font face=System " +
                ">Number of threads sleeping on a monitor</td><td></td><td><b><font face=System>");
        statData.append(getSleepingThreads() == null? 0 : getSleepingThreads().getNodeCount());
        statData.append("</b></td></tr>\n\n<tr bgcolor=\"#eeeeee\"><td><font face=System " +
                ">Number of deadlocks</td><td></td><td><b><font face=System>");
        statData.append(getDeadlocks() == null? 0 : getDeadlocks().getNodeCount());
        statData.append("</b></td></tr>\n\n<tr bgcolor=\"#dddddd\"><td><font face=System " +
                    ">Number of Monitors without locking threads</td><td></td><td><b><font face=System>");
        statData.append(getMonitorsWithoutLocks()    == null? 0 : getMonitorsWithoutLocks().getNodeCount());
        statData.append("</b></td></tr>");
        
        // add hints concerning possible hot spots found in this thread dump.
        statData.append(getDumpAnalyzer().analyzeDump());
        
        if(getHeapInfo() != null) {
            statData.append(getHeapInfo());
        }

        statData.append("</table>");

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
        StringBuffer statData = new StringBuffer("<body bgcolor=\"ffffff\"><table border=0 bgcolor=\"#dddddd\"><tr><td><font face=System" +
                ">Threads locking monitor</td><td><b><font face=System>");
        statData.append(locks);
        statData.append("</b></td></tr>\n\n<tr bgcolor=\"#eeeeee\"><td>");
        statData.append("<font face=System>Threads sleeping on monitor</td><td><b><font face=System>");
        statData.append(sleeps);
        statData.append("</b></td></tr>\n\n<tr><td>");
        statData.append("<font face=System>Threads waiting to lock monitor</td><td><b><font face=System>");
        statData.append(waits);
        statData.append("</b></td></tr>\n\n");
        if (locks == 0) {
            statData.append("<tr bgcolor=\"#ffffff\"<td></td></tr>");
            // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5086475
            statData.append("<tr bgcolor=\"#cccccc\"><td><font face=System> " +
                    "<p>This monitor doesn't have a thread locking it. This means one of the following is true:</p>" +
                    "<ul><li>a VM Thread is holding it." +
                    "<li>This lock is a <tt>java.util.concurrent</tt> lock and the thread holding it is not reported in the stack trace " +
                    "because the JVM option -XX:+PrintConcurrentLocks is not present." +
                    "<li>This lock is a custom java.util.concurrent lock either not based off of" +
                    " <tt>AbstractOwnableSynchronizer</tt> or not setting the exclusive owner when a lock is granted.</ul>");
            statData.append("If you see many monitors having no locking thread (and the latter two conditions above do " +
                    "not apply), this usually means the garbage collector is running.<br>");
            statData.append("In this case you should consider analyzing the Garbage Collector output. If the dump has many monitors with no locking thread<br>");
            statData.append("a click on the <a href=\"dump://\">dump node</a> will give you additional information.<br></td></tr>");
        }
        if (areALotOfWaiting(waits)) {
            statData.append("<tr bgcolor=\"#ffffff\"<td></td></tr>");
            statData.append("<tr bgcolor=\"#cccccc\"><td><font face=System " +
                    "<p>A lot of threads are waiting for this monitor to become available again.</p><br>");
            statData.append("This might indicate a congestion. You also should analyze other locks blocked by threads waiting<br>");
            statData.append("for this monitor as there might be much more threads waiting for it.<br></td></tr>");
        }
        statData.append("</table>");

        return (statData.toString());
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
