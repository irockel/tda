/*
 * SunJDKParser.java
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
 */
package de.grimmfrost.tda.parser;

import de.grimmfrost.tda.TDA;
import de.grimmfrost.tda.model.*;
import de.grimmfrost.tda.utils.DateMatcher;
import de.grimmfrost.tda.utils.HistogramTableModel;
import de.grimmfrost.tda.utils.IconFactory;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * Parses SunJDK Thread Dumps. Also parses SAP and HP Dumps.
 * Needs to be closed after use (so inner stream is closed).
 *
 * @author irockel
 */
public class SunJDKParser extends AbstractDumpParser {

    private MutableTreeNode nextDump = null;
    private Map threadStore = null;
    private int counter = 1;
    private int lineCounter = 0;
    private boolean foundClassHistograms = false;
    private boolean withCurrentTimeStamp = false;

    /** 
     * Creates a new instance of SunJDKParser 
     */
    public SunJDKParser(BufferedReader bis, Map threadStore, int lineCounter, boolean withCurrentTimeStamp, int startCounter, DateMatcher dm) {
        super(bis, dm);
        this.threadStore = threadStore;
        this.withCurrentTimeStamp = withCurrentTimeStamp;
        this.lineCounter = lineCounter;
        this.counter = startCounter;
    }

    /**
     * returns true if at least one more dump available, already loads it
     * (this will be returned on next call of parseNext)
     */
    public boolean hasMoreDumps() {
        nextDump = parseNext();
        return (nextDump != null);
    }

    /**
     * @return true, if a class histogram was found and added during parsing.
     */
    public boolean isFoundClassHistograms() {
        return (foundClassHistograms);
    }

    private String normalizeTitle(String title) {
        if (title == null) {
            return null;
        }
        String normalizedTitle = title;
        if (normalizedTitle.contains(" cpu=") && normalizedTitle.contains(" elapsed=")) {
            normalizedTitle = normalizedTitle.replaceAll(" cpu=[\\d\\.]+ms", "").replaceAll(" elapsed=[\\d\\.]+s", "");
        }
        return normalizedTitle;
    }

    /**
     * parse the next thread dump from the stream passed with the constructor.
     * @return null if no more thread dumps were found.
     */
    public MutableTreeNode parseNext() {
        if (nextDump != null) {
            MutableTreeNode tmpDump = nextDump;
            nextDump = null;
            return (tmpDump);
        }
        boolean retry = false;
        String line = null;

        do {
            DefaultMutableTreeNode threadDump = null;
            ThreadDumpInfo overallTDI = null;
            DefaultMutableTreeNode catMonitors = null;
            DefaultMutableTreeNode catMonitorsLocks = null;
            DefaultMutableTreeNode catThreads = null;
            DefaultMutableTreeNode catLocking = null;
            DefaultMutableTreeNode catSleeping = null;
            DefaultMutableTreeNode catWaiting = null;
            DefaultMutableTreeNode catVirtualThreads = null;

            try {
                Map threads = new HashMap();
                overallTDI = new ThreadDumpInfo("Dump No. " + counter++, 0);
                if (withCurrentTimeStamp) {
                    overallTDI.setStartTime((new Date(System.currentTimeMillis())).toString());
                }
                threadDump = new DefaultMutableTreeNode(overallTDI);

                catThreads = new DefaultMutableTreeNode(new TableCategory("Threads", IconFactory.THREADS));
                threadDump.add(catThreads);

                catWaiting = new DefaultMutableTreeNode(new TableCategory("Threads waiting for Monitors", IconFactory.THREADS_WAITING));

                catSleeping = new DefaultMutableTreeNode(new TableCategory("Threads sleeping on Monitors", IconFactory.THREADS_SLEEPING));

                catLocking = new DefaultMutableTreeNode(new TableCategory("Threads locking Monitors", IconFactory.THREADS_LOCKING));

                catVirtualThreads = new DefaultMutableTreeNode(new TableCategory("Virtual Threads", IconFactory.THREADS));

                // create category for monitors with disabled filtering.
                // NOTE:  These strings are "magic" in that the methods
                // TDA#displayCategory and TreeCategory#getCatComponent both
                // checks these literal strings and the behavior differs.
                catMonitors = new DefaultMutableTreeNode(new TreeCategory("Monitors", IconFactory.MONITORS, false));
                catMonitorsLocks = new DefaultMutableTreeNode(new TreeCategory("Monitors without locking thread", IconFactory.MONITORS_NOLOCKS, false));

                String title = null;
                String dumpKey = null;
                StringBuffer content = null;
                boolean inLocking = false;
                boolean inSleeping = false;
                boolean inWaiting = false;
                boolean isVirtualThread = false;
                int threadCount = 0;
                int waiting = 0;
                int locking = 0;
                int sleeping = 0;
                int virtualThreads = 0;
                boolean locked = true;
                boolean finished = false;
                MonitorMap mmap = new MonitorMap();
                Stack monitorStack = new Stack();
                long startTime = 0;
                int singleLineCounter = 0;
                boolean concurrentSyncsFlag = false;
                Matcher matched = getDm().getLastMatch();

                while (getBis().ready() && !finished) {
                    line = getNextLine();
                    lineCounter++;
                    singleLineCounter++;
                    if (locked) {
                        if (line.indexOf("Full thread dump") >= 0) {
                            locked = false;
                            if (!withCurrentTimeStamp) {
                                overallTDI.setLogLine(lineCounter);

                                if (startTime != 0) {
                                    startTime = 0;
                                } else if (matched != null && matched.matches()) {

                                    String parsedStartTime = matched.group(1);
                                    if (!getDm().isDefaultMatches() && isMillisTimeStamp()) {
                                        try {
                                            // the factor is a hack for a bug in oc4j timestamp printing (pattern timeStamp=2342342340)
                                            if (parsedStartTime.length() < 13) {
                                                startTime = Long.parseLong(parsedStartTime) * (long) Math.pow(10, 13 - parsedStartTime.length());
                                            } else {
                                                startTime = Long.parseLong(parsedStartTime);
                                            }
                                        } catch (NumberFormatException nfe) {
                                            startTime = 0;
                                        }
                                        if (startTime > 0) {
                                            overallTDI.setStartTime((new Date(startTime)).toString());
                                        }
                                    } else {
                                        overallTDI.setStartTime(parsedStartTime);
                                    }
                                    parsedStartTime = null;
                                    matched = null;
                                    getDm().resetLastMatch();
                                }
                            }
                            dumpKey = overallTDI.getName();
                        } else if (!getDm().isPatternError() && (getDm().getRegexPattern() != null)) {
                            Matcher m = getDm().checkForDateMatch(line);
                            if (m != null) {
                                matched = m;
                            }
                        }
                    } else {
                        if (line.startsWith("\"")) {
                            // We are starting a group of lines for a different thread
                            // First, flush state for the previous thread (if any)
                            concurrentSyncsFlag = false;
                            String stringContent = content != null ? content.toString() : null;
                            if (title != null) {
                                String threadContentStr = content.toString();
                                threads.put(normalizeTitle(title), threadContentStr);
                                content.append("</pre></pre>");
                                addToCategory(catThreads, title, null, threadContentStr, singleLineCounter, true);
                                
                                // Add to virtual threads category if applicable
                                if (isVirtualThread) {
                                    addToCategory(catVirtualThreads, title, null, threadContentStr, singleLineCounter, true);
                                }
                                
                                threadCount++;

                                if (inWaiting) {
                                    addToCategory(catWaiting, title, null, threadContentStr, singleLineCounter, true);
                                    inWaiting = false;
                                    waiting++;
                                }
                                if (inSleeping) {
                                    addToCategory(catSleeping, title, null, threadContentStr, singleLineCounter, true);
                                    inSleeping = false;
                                    sleeping++;
                                }
                                if (inLocking) {
                                    addToCategory(catLocking, title, null, threadContentStr, singleLineCounter, true);
                                    inLocking = false;
                                    locking++;
                                }
                                if (isVirtualThread) {
                                    virtualThreads++;
                                    // Reset isVirtualThread here as we are moving to next thread
                                    isVirtualThread = false;
                                }
                            }
                            singleLineCounter = 0;
                            while (!monitorStack.empty()) {
                                mmap.parseAndAddThread((String) monitorStack.pop(), title, content.toString());
                            }

                            // Second, initialize state for this new thread
                            title = line;
                            content = new StringBuffer("<body bgcolor=\"ffffff\"><pre><font size=" + TDA.getFontSizeModifier(-1) + ">");
                            content.append(normalizeTitle(line));
                            content.append("\n");
                            
                            // Reset and check if this is a virtual thread
                            isVirtualThread = (line.indexOf("virtual") > 0) || (line.indexOf("Virtual Thread") > 0);
                            
                        } else if (line.indexOf("at ") >= 0) {
                            content.append(line);
                            content.append("\n");
                        } else if (line.indexOf("java.lang.Thread.State") >= 0) {
                            content.append(line);
                            content.append("\n");
                            if (title.indexOf("t@") > 0) {
                                // in this case the title line is missing state informations
                                String state = line.substring(line.indexOf(':') + 1).trim();
                                if (state.indexOf(' ') > 0) {
                                    title += " state=" + state.substring(0, state.indexOf(' '));
                                } else {
                                    title += " state=" + state;
                                }
                            }
                        } else if (line.indexOf("Locked ownable synchronizers:") >= 0) {
                            concurrentSyncsFlag = true;
                            content.append(line);
                            content.append("\n");
                        } else if (line.indexOf("- waiting on") >= 0) {
                            content.append(linkifyMonitor(line));
                            monitorStack.push(line);
                            inSleeping = true;
                            content.append("\n");
                        } else if (line.indexOf("- parking to wait") >= 0) {
                            content.append(linkifyMonitor(line));
                            monitorStack.push(line);
                            inSleeping = true;
                            content.append("\n");
                        } else if (line.indexOf("- waiting to") >= 0) {
                            content.append(linkifyMonitor(line));
                            monitorStack.push(line);
                            inWaiting = true;
                            content.append("\n");
                        } else if (line.indexOf("- locked") >= 0) {
                            content.append(linkifyMonitor(line));
                            inLocking = true;
                            monitorStack.push(line);
                            content.append("\n");
                        } else if (line.indexOf("Carrying virtual thread") >= 0) {
                            // Handle virtual thread information
                            content.append("<font color=\"#0000ff\"><b>");
                            content.append(line);
                            content.append("</b></font>");
                            content.append("\n");
                            
                            // Check if carrier thread has issues (like being stuck in application code)
                            String contentStr = content.toString();
                            int atIdx = contentStr.indexOf("at ");
                            if (atIdx > 0) {
                                String stackTrace = contentStr.substring(atIdx);
                                String[] lines = stackTrace.split("\n");
                                boolean foundAppCode = false;
                                for (String stackLine : lines) {
                                    if (stackLine.contains("at ") 
                                            && !stackLine.contains("java.lang.VirtualThread.run")
                                            && !stackLine.contains("java.util.concurrent.ForkJoinPool")
                                            && !stackLine.contains("java.util.concurrent.ForkJoinWorkerThread")
                                            && !stackLine.contains("java.base@")
                                            && !stackLine.contains("jdk.internal")) {
                                        foundAppCode = true;
                                        break;
                                    }
                                }
                                if (foundAppCode) {
                                    content.append("<font color=\"#ff0000\"><b>Note: carrier thread seems to be stuck in application code.</b></font>\n");
                                }
                            }

                            // Mark this platform thread as carrying a virtual thread
                            isVirtualThread = true;
                            
                        } else if (line.indexOf("- ") >= 0) {
                            if (concurrentSyncsFlag) {
                                content.append(linkifyMonitor(line));
                                monitorStack.push(line);
                            } else {
                                content.append(line);
                            }
                            content.append("\n");
                        }

                        // last thread reached?
                        if ((line.indexOf("\"Suspend Checker Thread\"") >= 0)
                                || (line.indexOf("\"VM Periodic Task Thread\"") >= 0)
                                || (line.indexOf("<EndOfDump>") >= 0)) {
                            finished = true;
                            getBis().mark(getMarkSize());
                            if ((checkForDeadlocks(threadDump)) == 0) {
                                // no deadlocks found, set back original position.
                                getBis().reset();
                            }

                            getBis().mark(getMarkSize());

                            if (!checkThreadDumpStatData(overallTDI)) {
                                // no statistical data found, set back original position.
                                getBis().reset();
                            }

                            getBis().mark(getMarkSize());
                            if (!(foundClassHistograms = checkForClassHistogram(threadDump))) {
                                getBis().reset();
                            }
                        }
                    }
                }
                // last thread
                if (title != null) {
                    String threadContentStr = content.toString();
                    threads.put(normalizeTitle(title), threadContentStr);
                    content.append("</pre></pre>");
                    addToCategory(catThreads, title, null, threadContentStr, singleLineCounter, true);
                    
                    // Add to virtual threads category if applicable
                    if (isVirtualThread) {
                        addToCategory(catVirtualThreads, title, null, threadContentStr, singleLineCounter, true);
                    }

                    // Add to custom categories if applicable
                    // addCustomCategories(title, threadContentStr, singleLineCounter);
                    
                    threadCount++;

                    if (inWaiting) {
                        addToCategory(catWaiting, title, null, threadContentStr, singleLineCounter, true);
                        inWaiting = false;
                        waiting++;
                    }
                    if (inSleeping) {
                        addToCategory(catSleeping, title, null, threadContentStr, singleLineCounter, true);
                        inSleeping = false;
                        sleeping++;
                    }
                    if (inLocking) {
                        addToCategory(catLocking, title, null, threadContentStr, singleLineCounter, true);
                        inLocking = false;
                        locking++;
                    }
                    if (isVirtualThread) {
                        virtualThreads++;
                        isVirtualThread = false;
                    }
                }
                singleLineCounter = 0;
                while (!monitorStack.empty()) {
                    mmap.parseAndAddThread((String) monitorStack.pop(), title, content.toString());
                }

                int monitorCount = mmap.size();

                int monitorsWithoutLocksCount = 0;
                // dump monitors 
                if (mmap.size() > 0) {
                    int[] result = dumpMonitors(catMonitors, catMonitorsLocks, mmap);
                    monitorsWithoutLocksCount = result[0];
                    overallTDI.setOverallThreadsWaitingWithoutLocksCount(result[1]);

                }

                // display nodes with stuff to display
                if (waiting > 0) {
                    overallTDI.setWaitingThreads((Category) catWaiting.getUserObject());
                    threadDump.add(catWaiting);
                }

                if (sleeping > 0) {
                    overallTDI.setSleepingThreads((Category) catSleeping.getUserObject());
                    threadDump.add(catSleeping);
                }

                if (locking > 0) {
                    overallTDI.setLockingThreads((Category) catLocking.getUserObject());
                    threadDump.add(catLocking);
                }

                if (virtualThreads > 0) {
                    overallTDI.setVirtualThreads((Category) catVirtualThreads.getUserObject());
                    ((Category) catVirtualThreads.getUserObject()).setName(((Category) catVirtualThreads.getUserObject()) + " (" + virtualThreads + " Virtual Threads)");
                    threadDump.add(catVirtualThreads);
                }

                if (monitorCount > 0) {
                    overallTDI.setMonitors((Category) catMonitors.getUserObject());
                    threadDump.add(catMonitors);
                }

                if (monitorsWithoutLocksCount > 0) {
                    overallTDI.setMonitorsWithoutLocks((Category) catMonitorsLocks.getUserObject());
                    threadDump.add(catMonitorsLocks);
                }
                overallTDI.setThreads((Category) catThreads.getUserObject());

                ((Category) catThreads.getUserObject()).setName(((Category) catThreads.getUserObject()) + " (" + threadCount + " Threads overall)");
                ((Category) catWaiting.getUserObject()).setName(((Category) catWaiting.getUserObject()) + " (" + waiting + " Threads waiting)");
                ((Category) catSleeping.getUserObject()).setName(((Category) catSleeping.getUserObject()) + " (" + sleeping + " Threads sleeping)");
                ((Category) catLocking.getUserObject()).setName(((Category) catLocking.getUserObject()) + " (" + locking + " Threads locking)");
                ((Category) catMonitors.getUserObject()).setName(((Category) catMonitors.getUserObject()) + " (" + monitorCount + " Monitors)");
                ((Category) catMonitorsLocks.getUserObject()).setName(((Category) catMonitorsLocks.getUserObject()) + " (" + monitorsWithoutLocksCount
                        + " Monitors)");

                // add thread dump to passed dump store.
                if ((threadCount > 0) && (dumpKey != null)) {
                    threadStore.put(dumpKey.trim(), threads);
                }

                // check custom categories
                addCustomCategories(threadDump);

                return (threadCount > 0 ? threadDump : null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error during parsing of a found thread dump, skipping to next one!\n"
                        + "Check for possible broken dumps, sometimes, stream flushing mixes the logged data.\n"
                        + "Error Message is \"" + e.getLocalizedMessage() + "\". \n"
                        + (line != null ? "Last line read was \"" + line + "\". \n" : ""),
                        "Error during Parsing Thread Dump", JOptionPane.ERROR_MESSAGE);
                retry = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (retry);

        return (null);
    }

    /**
     * add a monitor link for monitor navigation
     * @param line containing monitor
     */
    private String linkifyMonitor(String line) {
        if (line != null && line.indexOf('<') >= 0) {
            String begin = line.substring(0, line.indexOf('<'));
            String monitor = line.substring(line.indexOf('<'), line.indexOf('>') + 1);
            String end = line.substring(line.indexOf('>') + 1);
            monitor = monitor.replaceAll("<", "<a href=\"monitor://" + monitor + "\">&lt;");
            monitor = monitor.substring(0, monitor.length() - 1) + "&gt;</a>";
            return (begin + monitor + end);
        } else if (line != null && line.indexOf('@') >= 0) {
            String begin = line.substring(0, line.indexOf('@') + 1);
            String monitor = line.substring(line.indexOf('@'));
            monitor = monitor.replaceAll("@", "@<a href=\"monitor://<" + monitor.substring(1) + ">\">");
            monitor = monitor.substring(0, monitor.length() - 1) + "</a>";
            return (begin + monitor);
        } else {
            return (line);
        }
    }

    /**
     * add a monitor link for monitor navigation
     * @param line containing monitor
     */
    private String linkifyDeadlockInfo(String line) {
        if (line != null && line.indexOf("Ox") >= 0) {
            String begin = line.substring(0, line.indexOf("0x"));
            int objectBegin = line.lastIndexOf("0x");
            int monitorBegin = line.indexOf("0x");
            String monitorHex = line.substring(monitorBegin, monitorBegin + 10);

            String monitor = line.substring(objectBegin, objectBegin + 10);
            String end = line.substring(line.indexOf("0x") + 10);

            monitor = "<a href=\"monitor://<" + monitor + ">\">" + monitorHex + "</a>";
            return (begin + monitor + end);
        } else {
            return (line);
        }
    }

    /**
     * checks for the next class histogram and adds it to the tree node passed
     * @param threadDump which tree node to add the histogram.
     */
    private boolean checkForClassHistogram(DefaultMutableTreeNode threadDump) throws IOException {
        HistogramTableModel classHistogram = parseNextClassHistogram(getBis());

        if (classHistogram.getRowCount() > 0) {
            addHistogramToDump(threadDump, classHistogram);
        }

        return (classHistogram.getRowCount() > 0);
    }

    private void addHistogramToDump(DefaultMutableTreeNode threadDump, HistogramTableModel classHistogram) {
        DefaultMutableTreeNode catHistogram;
        HistogramInfo hi = new HistogramInfo("Class Histogram of Dump", classHistogram);
        catHistogram = new DefaultMutableTreeNode(hi);
        threadDump.add(catHistogram);
    }

    /**
     * parses the next class histogram found in the stream, uses the max check
     * lines option to check how many lines to parse in advance.
     * <p>
     * This could be called from parseLoggcFile, which is outside our normal
     * calling stream. Thus, we have to pass in the BufferedReader. However, to
     * handle a WrappedSunJDKParser, we have to use getNextLine() if possible.
     *
     * @param bis the stream to read.
     */
    private HistogramTableModel parseNextClassHistogram(BufferedReader bis) throws IOException {
        boolean finished = false;
        boolean found = false;
        HistogramTableModel classHistogram = new HistogramTableModel();
        int maxLinesCounter = 0;

        boolean isNormalBis = bis == getBis();

        while (bis.ready() && !finished) {
            String line = (isNormalBis) ? getNextLine().trim() : bis.readLine().trim();
            if (!found && !line.equals("")) {
                if (line.startsWith("num   #instances    #bytes  class name")) {
                    found = true;
                } else if (maxLinesCounter >= getMaxCheckLines()) {
                    finished = true;
                } else {
                    maxLinesCounter++;
                }
            } else if (found) {
                if (line.startsWith("Total ")) {
                    // split string.
                    String newLine = line.replaceAll("(\\s)+", ";");
                    String[] elems = newLine.split(";");
                    classHistogram.setBytes(Long.parseLong(elems[2]));
                    classHistogram.setInstances(Long.parseLong(elems[1]));
                    finished = true;
                } else if (!line.startsWith("-------")) {
                    // removed blank, breaks splitting using blank...
                    String newLine = line.replaceAll("<no name>", "<no-name>");

                    // split string.
                    newLine = newLine.replaceAll("(\\s)+", ";");
                    String[] elems = newLine.split(";");

                    if (elems.length == 4) {
                        classHistogram.addEntry(elems[3].trim(), Integer.parseInt(elems[2].trim()),
                                Integer.parseInt(elems[1].trim()));
                    } else {
                        classHistogram.setIncomplete(true);
                        finished = true;
                    }

                }
            }
        }

        return (classHistogram);
    }

    /**
     * Heap
    PSYoungGen      total 6656K, used 3855K [0xb0850000, 0xb0f50000, 0xb4130000)
    eden space 6144K, 54% used [0xb0850000,0xb0b97740,0xb0e50000)
    from space 512K, 97% used [0xb0ed0000,0xb0f4c5c0,0xb0f50000)
    to   space 512K, 0% used [0xb0e50000,0xb0e50000,0xb0ed0000)
    PSOldGen        total 15552K, used 13536K [0x94130000, 0x95060000, 0xb0850000)
    object space 15552K, 87% used [0x94130000,0x94e68168,0x95060000)
    PSPermGen       total 16384K, used 13145K [0x90130000, 0x91130000, 0x94130000)
    object space 16384K, 80% used [0x90130000,0x90e06610,0x91130000)
    
     * @param tdi the thread dump info to check
     * @return true if heap data was found
     * @throws java.io.IOException
     */
    private boolean checkThreadDumpStatData(ThreadDumpInfo tdi) throws IOException {
        boolean finished = false;
        boolean found = false;
        StringBuilder hContent = new StringBuilder();
        int heapLineCounter = 0;
        int lines = 0;

        while (getBis().ready() && !finished) {
            String line = getNextLine();
            if (!found && !line.isEmpty()) {
                if (line.trim().startsWith("Heap")) {
                    found = true;
                } else if (lines >= getMaxCheckLines()) {
                    finished = true;
                } else {
                    lines++;
                }
            } else if (found) {
                if (heapLineCounter < 7) {
                    hContent.append(line).append("\n");
                } else {
                    finished = true;
                }
                heapLineCounter++;
            }
        }
        if (hContent.length() > 0) {
            tdi.setHeapInfo(new HeapInfo(hContent.toString()));
        }


        return (found);
    }

    /**
     * check if any dead lock information is logged in the stream
     * @param threadDump which tree node to add the histogram.
     */
    private int checkForDeadlocks(DefaultMutableTreeNode threadDump) throws IOException {
        boolean finished = false;
        boolean found = false;
        int deadlocks = 0;
        int lineCounter = 0;
        StringBuffer dContent = new StringBuffer();
        TreeCategory deadlockCat = new TreeCategory("Deadlocks", IconFactory.DEADLOCKS);
        DefaultMutableTreeNode catDeadlocks = new DefaultMutableTreeNode(deadlockCat);
        boolean first = true;

        while (getBis().ready() && !finished) {
            String line = getNextLine();

            if (!found && !line.equals("")) {
                if (line.trim().startsWith("Found one Java-level deadlock")) {
                    found = true;
                    dContent.append("<body bgcolor=\"ffffff\"><font size=").append(TDA.getFontSizeModifier(-1)).append("><b>");
                    dContent.append("Found one Java-level deadlock");
                    dContent.append("</b><hr></font><pre>\n");
                } else if (lineCounter >= getMaxCheckLines()) {
                    finished = true;
                } else {
                    lineCounter++;
                }
            } else if (found) {
                if (line.startsWith("Found one Java-level deadlock")) {
                    if (dContent.length() > 0) {
                        deadlocks++;
                        addToCategory(catDeadlocks, "Deadlock No. " + (deadlocks), null, dContent.toString(), 0, false);
                    }
                    dContent = new StringBuffer();
                    dContent.append("</pre><b><font size=").append(TDA.getFontSizeModifier(-1)).append(">");
                    dContent.append("Found one Java-level deadlock");
                    dContent.append("</b><hr></font><pre>\n");
                    first = true;
                } else if ((line.indexOf("Found") >= 0) && (line.endsWith("deadlocks.") || line.endsWith("deadlock."))) {
                    finished = true;
                } else if (line.startsWith("=======")) {
                    // ignore this line
                } else if (line.indexOf(" monitor 0x") >= 0) {
                    dContent.append(linkifyDeadlockInfo(line));
                    dContent.append("\n");
                } else if (line.indexOf("Java stack information for the threads listed above") >= 0) {
                    dContent.append("</pre><br><font size=").append(TDA.getFontSizeModifier(-1)).append("><b>");
                    dContent.append("Java stack information for the threads listed above");
                    dContent.append("</b><hr></font><pre>");
                    first = true;
                } else if ((line.indexOf("- waiting on") >= 0)
                        || (line.indexOf("- waiting to") >= 0)
                        || (line.indexOf("- locked") >= 0)
                        || (line.indexOf("- parking to wait") >= 0)) {

                    dContent.append(linkifyMonitor(line));
                    dContent.append("\n");

                } else if (line.trim().startsWith("\"")) {
                    dContent.append("</pre>");
                    if (first) {
                        first = false;
                    } else {
                        dContent.append("<br>");
                    }
                    dContent.append("<b><font size=").append(TDA.getFontSizeModifier(-1)).append("><code>");
                    dContent.append(line);
                    dContent.append("</font></code></b><pre>");
                } else {
                    dContent.append(line);
                    dContent.append("\n");
                }
            }
        }
        if (dContent.length() > 0) {
            deadlocks++;
            addToCategory(catDeadlocks, "Deadlock No. " + (deadlocks), null, dContent.toString(), 0, false);
        }

        if (deadlocks > 0) {
            threadDump.add(catDeadlocks);
            ((ThreadDumpInfo) threadDump.getUserObject()).setDeadlocks((TreeCategory) catDeadlocks.getUserObject());
            deadlockCat.setName("Deadlocks (" + deadlocks + (deadlocks == 1 ? " deadlock)" : " deadlocks)"));
        }

        return (deadlocks);
    }

    /**
     * dump the monitor information
     * @param catMonitors
     * @param catMonitorsLocks
     * @param mmap
     * @return
     */
    private int[] dumpMonitors(DefaultMutableTreeNode catMonitors, DefaultMutableTreeNode catMonitorsLocks, MonitorMap mmap) {
        Iterator iter = mmap.iterOfKeys();
        int monitorsWithoutLocksCount = 0;
        int overallThreadsWaiting = 0;
        while (iter.hasNext()) {
            String monitor = (String) iter.next();
            Map[] threads = mmap.getFromMonitorMap(monitor);
            ThreadInfo mi = new ThreadInfo(monitor, null, "", 0, null);
            DefaultMutableTreeNode monitorNode = new DefaultMutableTreeNode(mi);

            // first the locks
            Iterator iterLocks = threads[MonitorMap.LOCK_THREAD_POS].keySet().iterator();
            int locks = 0;
            int sleeps = 0;
            int waits = 0;
            while (iterLocks.hasNext()) {
                String thread = (String) iterLocks.next();
                String stackTrace = (String) threads[MonitorMap.LOCK_THREAD_POS].get(thread);
                if (threads[MonitorMap.SLEEP_THREAD_POS].containsKey(thread)) {
                    createNode(monitorNode, "locks and sleeps on monitor: " + thread, null, stackTrace, 0);
                    sleeps++;
                } else if (threads[MonitorMap.WAIT_THREAD_POS].containsKey(thread)) {
                    createNode(monitorNode, "locks and waits on monitor: " + thread, null, stackTrace, 0);
                    sleeps++;
                } else {
                    createNode(monitorNode, "locked by " + thread, null, stackTrace, 0);
                }
                locks++;
            }

            Iterator iterWaits = threads[MonitorMap.WAIT_THREAD_POS].keySet().iterator();
            while (iterWaits.hasNext()) {
                String thread = (String) iterWaits.next();
                if (!threads[MonitorMap.LOCK_THREAD_POS].containsKey(thread)) {
                    createNode(monitorNode, "waits on monitor: " + thread, null, (String) threads[MonitorMap.WAIT_THREAD_POS].get(thread), 0);
                    waits++;
                }
            }

            mi.setContent(ThreadDumpInfo.getMonitorInfo(locks, waits, sleeps));
            mi.setName(mi.getName() + ":    " + (sleeps) + " Thread(s) sleeping, " + (waits) + " Thread(s) waiting, " + (locks) + " Thread(s) locking");
            if (ThreadDumpInfo.areALotOfWaiting(waits)) {
                mi.setALotOfWaiting(true);
            }
            mi.setChildCount(monitorNode.getChildCount());

            ((Category) catMonitors.getUserObject()).addToCatNodes(new DefaultMutableTreeNode(mi));
            if (locks == 0) {
                monitorsWithoutLocksCount++;
                overallThreadsWaiting += waits;
                ((Category) catMonitorsLocks.getUserObject()).addToCatNodes(new DefaultMutableTreeNode(mi));
            }
        }
        return new int[]{monitorsWithoutLocksCount, overallThreadsWaiting};
    }

    private void renormalizeThreadDepth(DefaultMutableTreeNode threadNode1) {
        for (Enumeration e = threadNode1.children(); e.hasMoreElements();) {
            DefaultMutableTreeNode monitorNode2 = (DefaultMutableTreeNode) e.nextElement();
            for (int ii = 0; ii < monitorNode2.getChildCount(); ii++) {
                renormalizeMonitorDepth(monitorNode2, ii);
            }
        }
    }

    private void renormalizeMonitorDepth(DefaultMutableTreeNode monitorNode, int index) {
        // First, remove all duplicates of the item at index "index"
        DefaultMutableTreeNode threadNode1 = (DefaultMutableTreeNode) monitorNode.getChildAt(index);
        ThreadInfo mi1 = (ThreadInfo) threadNode1.getUserObject();
        int i = index + 1;
        while (i < monitorNode.getChildCount()) {
            DefaultMutableTreeNode threadNode2 = (DefaultMutableTreeNode) monitorNode.getChildAt(i);
            ThreadInfo mi2 = (ThreadInfo) threadNode2.getUserObject();
            if (mi1.getName().equals(mi2.getName())) {
                if (threadNode2.getChildCount() > 0) {
                    threadNode1.add((DefaultMutableTreeNode) threadNode2.getFirstChild());
                    monitorNode.remove(i);
                    continue;
                }
            }
            i++;
        }

        // Second, recurse into item "index"
        renormalizeThreadDepth(threadNode1);
    }

    private boolean checkForDuplicateThreadItem(Map directChildMap, DefaultMutableTreeNode node1) {
        ThreadInfo mi1 = (ThreadInfo) node1.getUserObject();
        String name1 = mi1.getName();

        for (Iterator iter2 = directChildMap.entrySet().iterator(); iter2.hasNext();) {
            DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) ((Map.Entry) iter2.next()).getValue();
            if (node1 == node2) {
                continue;
            }

            ThreadInfo mi2 = (ThreadInfo) node2.getUserObject();
            if (name1.equals(mi2.getName()) && node2.getChildCount() > 0) {
                node1.add((MutableTreeNode) node2.getFirstChild());
                iter2.remove();
                return true;
            }
        }

        return false;
    }

    private int fillBlockingThreadMaps(MonitorMap mmap, Map directChildMap) {
        int blockedThread = 0;
        for (Iterator iter = mmap.iterOfKeys(); iter.hasNext();) {
            String monitor = (String) iter.next();
            Map[] threads = mmap.getFromMonitorMap(monitor);

            // Only one thread can really be holding this monitor, so find the thread
            String threadLine = getLockingThread(threads);
            ThreadInfo tmi = new ThreadInfo("Thread - " + threadLine, null, "", 0, null);
            DefaultMutableTreeNode threadNode = new DefaultMutableTreeNode(tmi);

            ThreadInfo mmi = new ThreadInfo("Monitor - " + monitor, null, "", 0, null);
            DefaultMutableTreeNode monitorNode = new DefaultMutableTreeNode(mmi);
            threadNode.add(monitorNode);

            // Look over all threads blocked on this monitor
            for (Iterator iterWaits = threads[MonitorMap.WAIT_THREAD_POS].keySet().iterator(); iterWaits.hasNext();) {
                String thread = (String) iterWaits.next();
                // Skip the thread that has this monitor locked
                if (!threads[MonitorMap.LOCK_THREAD_POS].containsKey(thread)) {
                    blockedThread++;
                    createNode(monitorNode, "Thread - " + thread, null, (String) threads[MonitorMap.WAIT_THREAD_POS].get(thread), 0);
                }
            }

            String blockingStackFrame = (String) threads[MonitorMap.LOCK_THREAD_POS].get(threadLine);
            tmi.setContent(blockingStackFrame);
            mmi.setContent("This monitor (" + linkifyMonitor(monitor)
                    + ") is held in the following stack frame:\n\n" + blockingStackFrame);

            // If no-one is blocked on or waiting for this monitor, don't show it
            if (monitorNode.getChildCount() > 0) {
                directChildMap.put(monitor, threadNode);
            }
        }
        return blockedThread;
    }

    private String getLockingThread(Map[] threads) {
        int lockingThreadCount = threads[MonitorMap.LOCK_THREAD_POS].keySet().size();
        if (lockingThreadCount == 1) {
            return (String) threads[MonitorMap.LOCK_THREAD_POS].keySet().iterator().next();
        }

        for (Iterator iterLocks = threads[MonitorMap.LOCK_THREAD_POS].keySet().iterator(); iterLocks.hasNext();) {
            String thread = (String) iterLocks.next();
            if (!threads[MonitorMap.SLEEP_THREAD_POS].containsKey(thread)) {
                return thread;
            }
        }

        return "";
    }

    /**
     * parses a loggc file stream and reads any found class histograms and adds the to the dump store
     * @param loggcFileStream the stream to read
     * @param root the root node of the dumps.
     */
    public void parseLoggcFile(InputStream loggcFileStream, DefaultMutableTreeNode root) {
        BufferedReader bis = new BufferedReader(new InputStreamReader(loggcFileStream));
        Vector histograms = new Vector();

        try {
            while (bis.ready()) {
                bis.mark(getMarkSize());
                String nextLine = bis.readLine();
                if (nextLine.startsWith("num   #instances    #bytes  class name")) {
                    bis.reset();
                    histograms.add(parseNextClassHistogram(bis));
                }
            }

            // now add the found histograms to the tree.
            for (int i = histograms.size() - 1; i >= 0; i--) {
                DefaultMutableTreeNode dump = getNextDumpForHistogram(root);
                if (dump != null) {
                    addHistogramToDump(dump, (HistogramTableModel) histograms.get(i));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * generate thread info token for table view.
     * @param name the thread info.
     * @return thread tokens.
     */
    public String[] getThreadTokens(String name) {
        String[] tokens = null;
        
        if (name.indexOf("prio") > 0) {
            tokens = new String[7];
            
            // Initialize all tokens to empty strings to prevent null values
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = "";
            }

            tokens[0] = name.substring(1, name.lastIndexOf('"') == 0 ? 
                    name.length() - 1 : 
                    name.lastIndexOf('"'));
            tokens[1] = name.indexOf("daemon") > 0 ? "Daemon" : "Task";

            String strippedToken = name.substring(name.lastIndexOf('"') + 1);

            if (strippedToken.contains("tid=")) {
                int tidIndex = strippedToken.indexOf("tid=") - 1;
                int osPrioIndex = strippedToken.indexOf("os_prio=") - 1;
                int cpuIndex = strippedToken.indexOf("cpu=") - 1;
                int endIndex = osPrioIndex > 0 ? osPrioIndex : cpuIndex > 0 ? cpuIndex : tidIndex;
                tokens[2] = strippedToken.substring(strippedToken.indexOf("prio=") + 5, endIndex);                
            } else {
                tokens[2] = strippedToken.substring(strippedToken.indexOf("prio=") + 5);
            }

            if ((strippedToken.contains("tid=")) && (strippedToken.contains("nid="))) {
                tokens[3] = String.valueOf(Long.parseLong(strippedToken.substring(strippedToken.indexOf("tid=") + 6,
                        strippedToken.indexOf("nid=") - 1), 16));
            } else if (strippedToken.contains("tid=")) {
                // Handle virtual thread format: tid=0x0000fffef57b0000  [0x0000fffeb1eae000]
                int tidStart = strippedToken.indexOf("tid=") + 6; // Skip "tid=0x"
                String tidSubstring = strippedToken.substring(tidStart);
                
                // Find the end of the tid value (at space, bracket, or end of string)
                int tidEnd = tidSubstring.length();
                int spaceIndex = tidSubstring.indexOf(' ');
                int bracketIndex = tidSubstring.indexOf('[');
                
                if (spaceIndex >= 0) {
                    tidEnd = Math.min(tidEnd, spaceIndex);
                }
                if (bracketIndex >= 0) {
                    tidEnd = Math.min(tidEnd, bracketIndex);
                }
                
                String tidValue = tidSubstring.substring(0, tidEnd).trim();
                tokens[3] = String.valueOf(Long.parseLong(tidValue, 16));
            }

            // default for token 6 is:
            tokens[6] = "<no address range>";

            if ((strippedToken.contains("nid=")) && (strippedToken.indexOf(" ", strippedToken.indexOf("nid="))) >= 0) {
                if (strippedToken.indexOf("nid=0x") > 0) { // is hexadecimal
                    String nidToken = strippedToken.substring(strippedToken.indexOf("nid=") + 6,
                            strippedToken.indexOf(" ", strippedToken.indexOf("nid=")));
                    tokens[4] = String.valueOf(Long.parseLong(nidToken, 16));
                } else { // is decimal
                    String nidToken = strippedToken.substring(strippedToken.indexOf("nid=") + 4,
                            strippedToken.indexOf(" ", strippedToken.indexOf("nid=")));
                    tokens[4] = nidToken;
                }

                if (strippedToken.indexOf('[') > 0) {
                    if (strippedToken.indexOf("lwp_id=") > 0) {
                        tokens[5] = strippedToken.substring(strippedToken.indexOf(" ", strippedToken.indexOf("lwp_id=")) + 1, strippedToken.indexOf('[',
                                strippedToken.indexOf("lwp_id=")) - 1);
                    } else {
                        tokens[5] = strippedToken.substring(strippedToken.indexOf(" ", strippedToken.indexOf("nid=")) + 1, strippedToken.indexOf('[',
                                strippedToken.indexOf("nid=")) - 1);
                    }
                    tokens[6] = strippedToken.substring(strippedToken.indexOf('['));
                } else {
                    tokens[5] = strippedToken.substring(strippedToken.indexOf(" ", strippedToken.indexOf("nid=")) + 1);
                }
            } else if (strippedToken.indexOf("nid=") >= 0) {
                String nidToken = strippedToken.substring(strippedToken.indexOf("nid=") + 6);
                // nid is at the end.
                if (nidToken.indexOf("0x") > 0) { // is hexadecimal
                    tokens[4] = String.valueOf(Long.parseLong(nidToken, 16));
                } else {
                    tokens[4] = nidToken;
                }
            }
        } else {
            tokens = new String[3];
            
            // Initialize all tokens to empty strings to prevent null values
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = "";
            }
            
            tokens[0] = name.substring(1, name.lastIndexOf('"') == 0 ? 
                    name.length() - 1 : 
                    name.lastIndexOf('"'));
            if (name.indexOf("nid=") > 0) {
                tokens[1] = name.substring(name.indexOf("nid=") + 4, name.indexOf("state=") - 1);
                tokens[2] = name.substring(name.indexOf("state=") + 6);
            } else if (name.indexOf("t@") > 0) {
                tokens[1] = name.substring(name.indexOf("t@") + 2, name.indexOf("state=") - 1);
                tokens[2] = name.substring(name.indexOf("state=") + 6);
            } else if (name.indexOf("id=") > 0) {
                tokens[1] = name.substring(name.indexOf("id=") + 3, name.indexOf(" in"));
                tokens[2] = name.substring(name.indexOf(" in") + 3);
            } else {
                tokens[1] = "";
                tokens[2] = "";
            }
        }

        return (tokens);
    }

    /**
     * check if the passed logline contains the beginning of a sun jdk thread
     * dump.
     * @param logLine the line of the logfile to test
     * @return true, if the start of a sun thread dump is detected.
     */
    public static boolean checkForSupportedThreadDump(String logLine) {
        return (logLine.trim().indexOf("Full thread dump") >= 0);
    }

    protected String getNextLine() throws IOException {
        return getBis().readLine();
    }
}
