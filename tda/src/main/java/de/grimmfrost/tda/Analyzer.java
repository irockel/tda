/*
 * Analyzer.java
 *
 * This file is part of TDA - Thread Dump Analysis Tool.
 *
 * Foobar is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: Analyzer.java,v 1.3 2008-01-28 09:29:34 irockel Exp $
 */

package de.grimmfrost.tda;

import de.grimmfrost.tda.utils.PrefManager;
import java.io.Serializable;

/**
 * analyze the given thread dump.
 * 
 * @author irockel
 */
public class Analyzer implements Serializable {
    ThreadDumpInfo tdi;
    
    /**
     * generate a dump analyzer for the given thread dump.
     * @param tdi the thread dump to analyze.
     */
    public Analyzer(ThreadDumpInfo tdi) {
        this.tdi = tdi;
    }
    
    /**
     * analyze the given data and generate htmlified hints
     * @return html-text containing hints, empty strings if no hints can
     *         be given.
     */
    public String analyzeDump() {
        // check for possible hot spots concerning this thread dump
        
        StringBuffer statData = new StringBuffer();
        int deadlocks = tdi.getDeadlocks() == null ? 0 : tdi.getDeadlocks().getNodeCount();
        int threadCount = tdi.getThreads() == null ? 0 : tdi.getThreads().getNodeCount();
        int waiting = tdi.getWaitingThreads() == null ? 0 : tdi.getWaitingThreads().getNodeCount();
        int sleeping = tdi.getSleepingThreads() == null ? 0 : tdi.getSleepingThreads().getNodeCount();
        
        int overallThreadsWaitingWithoutLocks = tdi.getOverallThreadsWaitingWithoutLocksCount();
        int monitorsWithoutLocksCount = tdi.getMonitorsWithoutLocks() == null ? 0 : tdi.getMonitorsWithoutLocks().getNodeCount();
        
        String altRowColor = "#cccccc";
        String textColor = "#333333";
        String linkColor = "#0056b3";

        // check if a lot of threads are in state "waiting"
        if ((deadlocks == 0) && (threadCount > 0) && ((waiting / (threadCount / 100.0)) > 10.0)) {
            statData.append("<tr bgcolor=\"#ffffff\"<td></td></tr>");
            statData.append("<tr bgcolor=\"").append(altRowColor).append("\"><td colspan=2><font face=System color=\"").append(textColor).append("\"><p>")
                    .append((int) (waiting / (threadCount / 100.0))).append("% of all threads are waiting for a monitor to become available again.</p><br>");
            statData.append("This might indicate a congestion or even a deadlock. If a monitor doesn't have a locking thread, it might be<br>");
            statData.append("hold by some external resource or system thread. You should check the <a style=\"color: ").append(linkColor).append(";\" href=\"wait://\">waiting threads</a>.<br></td></tr>");
        } else if (deadlocks > 0) {
            statData.append("<tr bgcolor=\"#ffffff\"<td></td></tr>");
            statData.append("<tr bgcolor=\"").append(altRowColor).append("\"><td colspan=2><font face=System color=\"").append(textColor).append("\"><p>The JVM has detected ")
                    .append(deadlocks).append(" deadlock(s) in the thread dump. You should check the <br><a style=\"color: ").append(linkColor).append(";\" href=\"dead://\">deadlocks</a> for further information.</p><br>");
        }

        // check if a lot of threads are in state "waiting"
        if ((threadCount > 0) && ((sleeping / (threadCount / 100.0)) > 25.0)) {
            statData.append("<tr bgcolor=\"#ffffff\"<td></td></tr>");
            statData.append("<tr bgcolor=\"").append(altRowColor).append("\"><td colspan=2><font face=System color=\"").append(textColor).append("\"><p>")
                    .append((int) (sleeping / (threadCount / 100.0))).append("% of all threads are sleeping on a monitor.</p><br>");
            statData.append("This might indicate they are waiting for some external resource (e.g. database) which is overloaded<br>");
            statData.append("or not available or are just waiting to get to do something (idle threads).<br>");
            statData.append("You should check the <a style=\"color: ").append(linkColor).append(";\" href=\"sleep://\">sleeping threads</a> with a filter excluding all idle threads.</td></tr>");
        }

        // display an info if there are monitors without locking threads
        if (monitorsWithoutLocksCount > 0) {
            statData.append("<tr bgcolor=\"#ffffff\"<td></td></tr>");
            statData.append("<tr bgcolor=\"").append(altRowColor).append("\"><td colspan=2><font face=System color=\"").append(textColor).append("\"><p>This thread dump contains monitors without a locking thread information.<br>");
            statData.append("This means, the monitor is hold by a system thread or some external resource.</p<br>");
            statData.append("You should check the monitors without locking threads for more information.<br></td></tr>");
        }

        // check for indications for running garbage collector
        if ((threadCount > 0) && (overallThreadsWaitingWithoutLocks / (threadCount / 100.0) > 50.0)) {
            statData.append("<tr bgcolor=\"#ffffff\"<td></td></tr>");
            statData.append("<tr bgcolor=\"").append(altRowColor).append("\"><td colspan=2><font face=System color=\"").append(textColor).append("\"><p>")
                    .append((int) (overallThreadsWaitingWithoutLocks / (threadCount / 100.0))).append("% of all threads are waiting for a monitor without a application ");
            statData.append("thread holding it.<br> This indicates a congestion. It is very likely the garbage collector is running ");
            statData.append("and is blocking the monitors.</p<br>");
            statData.append("You should check the monitors without locking threads for more information on the blocked threads.<br>");
            statData.append("You also should analyze the garbage collector behaviour. Go to the ");
            statData.append("<a style=\"color: ").append(linkColor).append(";\" href=\"http://www.tagtraum.com/gcviewer.html\">GCViewer-Homepage</a> for more<br>");
            statData.append(" information on how to do this.</td></tr>");
        }

        // check for stuck carrier threads
        if (threadCount > 0) {
            int stuckCarrierThreads = 0;
            Category threadsCat = tdi.getThreads();
            for (int i = 0; i < threadCount; i++) {
                javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) threadsCat.getNodeAt(i);
                ThreadInfo ti = (ThreadInfo) node.getUserObject();
                if (ti.getContent().contains("carrier thread seems to be stuck in application code")) {
                    stuckCarrierThreads++;
                }
            }
            if (stuckCarrierThreads > 0) {
                statData.append("<tr bgcolor=\"#ffffff\"<td></td></tr>");
                statData.append("<tr bgcolor=\"").append(altRowColor).append("\"><td colspan=2><font face=System color=\"").append(textColor).append("\"><p>")
                        .append("Detected ").append(stuckCarrierThreads).append(" virtual thread(s) where the carrier thread seems to be stuck in application code.</p><br>");
                statData.append("This might indicate \"pinning\" or long-running operations on a virtual thread that prevent it from yielding.<br>");
                statData.append("A Carrier Thread is usually named something like 'ForkJoinPool-x-Worker'.<br>");
                statData.append("Check the <a style=\"color: ").append(linkColor).append(";\" href=\"dump://\">thread dump</a> for threads with this warning.</td></tr>");
            }
        }
        
        return statData.toString();
    }
}
