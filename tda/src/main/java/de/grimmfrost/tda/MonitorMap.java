/*
 * MonitorMap.java
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
 * $Id: MonitorMap.java,v 1.7 2008-11-21 21:17:51 irockel Exp $
 */

package de.grimmfrost.tda;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * map for saving monitor-thread relation in a thread dump.
 *
 * @author irockel
 */
public class MonitorMap implements Serializable {
    
    /**
     * A "LOCK_THREAD" is one that has locked the monitor and that has not
     * released the lock (by calling "Object.wait()").
     */
    public static final int LOCK_THREAD_POS = 0;
    /**
     * A "WAIT_THREAD" is one that is blocked waiting for a monitor to be
     * available.
     */
    public static final int WAIT_THREAD_POS = 1;
    /**
     * A "SLEEP_THREAD" is one that holds the monitor but has released it and is
     * asleep in Object.wait(), waiting to be notified.
     */
    public static final int SLEEP_THREAD_POS = 2;
    
    private Map monitorMap = null;    
    
    /** 
     * Creates a new instance of MonitorMap 
     */
    public MonitorMap() {
    }
    
    public void addToMonitorMap(String key, Map[] objectSet) {
        if(monitorMap == null) {
            monitorMap = new HashMap();
        }
        
        monitorMap.put(key, objectSet);
    }
    
    public boolean hasInMonitorMap(String key) {
        return(monitorMap != null && monitorMap.containsKey(key));
    }
    
    public Map[] getFromMonitorMap(String key) {
        return(monitorMap != null && hasInMonitorMap(key)? (Map[])monitorMap.get(key) : null);
    }
    
    public void addWaitToMonitor(String key, String waitThread, String threadContent) {
        addToMonitorValue(key, WAIT_THREAD_POS, waitThread, threadContent);
    }
    
    public void addLockToMonitor(String key, String lockThread, String threadContent) {
        addToMonitorValue(key, LOCK_THREAD_POS, lockThread, threadContent);
    }
    
    public void addSleepToMonitor(String key, String sleepThread, String threadContent) {
        addToMonitorValue(key, SLEEP_THREAD_POS, sleepThread, threadContent);
    }
    
    private void addToMonitorValue(String key, int pos, String threadTitle, String thread) {
        Map[] objectSet = null;

        if(hasInMonitorMap(key)) {
            objectSet = getFromMonitorMap(key);
        } else {
            objectSet = new HashMap[3];
            objectSet[0] = new HashMap();
            objectSet[1] = new HashMap();
            objectSet[2] = new HashMap();
            addToMonitorMap(key, objectSet);
        }
        
        objectSet[pos].put(threadTitle, thread);
    }
    
    public void parseAndAddThread(String line, String threadTitle, String currentThread) {
        if (line == null) {
            return;
        }
        if((line.indexOf('<') > 0)) {
            String monitor = line.substring(line.indexOf('<'));
            if (line.trim().startsWith("- waiting to lock") || line.trim().startsWith("- parking to wait")) {
                addWaitToMonitor(monitor, threadTitle, currentThread);
            } else if (line.trim().startsWith("- waiting on")) {
                addSleepToMonitor(monitor, threadTitle, currentThread);
            } else {
                addLockToMonitor(monitor, threadTitle, currentThread);
            }
        } else if(line.indexOf('@') > 0) {
            String monitor = "<" + line.substring(line.indexOf('@')+1) + "> (a " +
                    line.substring(line.lastIndexOf(' '),line.indexOf('@')) + ")";
            if (line.trim().startsWith("- waiting to lock") || line.trim().startsWith("- parking to wait")) {
                addWaitToMonitor(monitor, threadTitle, currentThread);
            } else if (line.trim().startsWith("- waiting on")) {
                addSleepToMonitor(monitor, threadTitle, currentThread);
            } else {
                addLockToMonitor(monitor, threadTitle, currentThread);
            }
        }
    }
    
    public Iterator iterOfKeys() {
        return(monitorMap == null? null : monitorMap.keySet().iterator());
    }
    
    public int size() {
        return(monitorMap == null? 0: monitorMap.size());
    }
    
    
}
