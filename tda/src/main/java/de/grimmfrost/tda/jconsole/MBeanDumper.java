/**
 * Thread Dump Analysis Tool, parses Thread Dump input and displays it as tree
 *
 * This file is part of TDA - Thread Dump Analysis Tool.
 *
 * TDA is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * TDA is distributed in the hope that it will be useful,h
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * TDA should have received a copy of the Lesser GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: MBeanDumper.java,v 1.16 2010-04-01 08:58:24 irockel Exp $
 */
package de.grimmfrost.tda.jconsole;

import java.io.IOException;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.swing.JOptionPane;

/**
 * Request a Thread Dump via the given MBeanServerConnection, can only be
 * call in jconsole with proper jmx stuff available.
 * 
 * @author irockel
 */
public class MBeanDumper {
    private static int CONNECT_RETRIES = 10;
    
    private MBeanServerConnection server;
    private ThreadMXBean tmbean;
    private ObjectName objname;
    
    private String dumpPrefix = "\nFull thread dump ";
    
    // default - JDK 6+ VM
    private String findDeadlocksMethodName = "findDeadlockedThreads";
    private boolean canDumpLocks = true;
    private String javaVersion;

    /**
     * Constructs a ThreadMonitor object to get thread information
     * in a remote JVM.
     */
    public MBeanDumper(MBeanServerConnection server) throws IOException {
       setMBeanServerConnection(server);
       try {
           objname = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            // should not reach here
            InternalError ie = new InternalError(e.getMessage());
            ie.initCause(e);
            throw ie;
       }
       parseMBeanInfo(); 
    }
    
    private void setDumpPrefix() {
        try {
            RuntimeMXBean rmbean = (RuntimeMXBean) ManagementFactory.newPlatformMXBeanProxy(server,
                                            ManagementFactory.RUNTIME_MXBEAN_NAME,
                                            RuntimeMXBean.class);
            dumpPrefix += rmbean.getVmName() + " " + rmbean.getVmVersion() + "\n";
            javaVersion = rmbean.getVmVersion();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }       

    /**
     * Prints the thread dump information to System.out.
     */
    public String threadDump() {
        StringBuilder dump = new StringBuilder();
        int retries = 0;
        while(retries < CONNECT_RETRIES) {
            try {
                if (canDumpLocks) {
                    if (tmbean.isObjectMonitorUsageSupported() &&
                            tmbean.isSynchronizerUsageSupported()) {
                        /*
                     * Print lock info if both object monitor usage 
                     * and synchronizer usage are supported.
                     * This sample code can be modified to handle if 
                     * either monitor usage or synchronizer usage is supported.
                     */
                        dumpThreadInfoWithLocks(dump);
                    }
                } else {
                    dumpThreadInfo(dump);
                }
                // finished
                retries = CONNECT_RETRIES;
            } catch (NullPointerException npe) {
                if (retries >= CONNECT_RETRIES) {
                    JOptionPane.showMessageDialog(null,
                                    "Error requesting dump using the JMX Connection. Remote VM returned nothing.\n" +
                                    "You can try to reconnect or just simply try to request a dump again.",                                    
                                    "Error during requesting Dump", JOptionPane.ERROR_MESSAGE);
                    
                    // return empty string;
                    return("");
                }
                try {
                    // workaround for unstable connections.
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                //System.out.println("retrying " + retries);
                retries++;
            }
        }
        dump.append("\n<EndOfDump>\n\n");
        
        return(dump.toString());
    }
    
    /**
     * create dump date similar to format used by 1.6 VMs
     * @return dump date (e.g. 2007-10-25 08:00:00)
     */
    private String getDumpDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        return(sdfDate.format(new Date()));
    }

    private void dumpThreadInfo(StringBuilder dump) {
       dump.append(getDumpDate());
       dump.append(dumpPrefix);
       dump.append("\n");
       long[] tids = tmbean.getAllThreadIds();
       ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
       for (int i = 0; i < tinfos.length; i++) {
           ThreadInfo ti = tinfos[i];
           printThreadInfo(ti, dump);
       }
    }

    /**
     * Prints the thread dump information with locks info to System.out.
     */
    private void dumpThreadInfoWithLocks(StringBuilder dump) {
       dump.append(getDumpDate());
       dump.append(dumpPrefix);
       dump.append("\n");

       ThreadInfo[] tinfos = tmbean.dumpAllThreads(true, true);
       for (int i = 0; i < tinfos.length; i++) {
           ThreadInfo ti = tinfos[i];
           printThreadInfo(ti, dump);
           LockInfo[] syncs = ti.getLockedSynchronizers();
           printLockInfo(syncs, dump);
       }
       dump.append("\n");
    }

    private static String INDENT = "    ";

    private void printThreadInfo(ThreadInfo ti, StringBuilder dump) {
       // print thread information
       printThread(ti, dump);

       // print stack trace with locks
       StackTraceElement[] stacktrace = ti.getStackTrace();
       MonitorInfo[] monitors = ti.getLockedMonitors();
       for (int i = 0; i < stacktrace.length; i++) {
           StackTraceElement ste = stacktrace[i];
           dump.append(INDENT + "at " + ste.toString());
           dump.append("\n");
           for (int j = 1; j < monitors.length; j++) {
               MonitorInfo mi = monitors[j];
               if (mi.getLockedStackDepth() == i) {
                   dump.append(INDENT + "  - locked " + mi);
                   dump.append("\n");
               }
           }
       }
       dump.append("\n");
    }
                                                                                
    private void printThread(ThreadInfo ti, StringBuilder dump) {
       StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\"" +
                                            " nid=" + ti.getThreadId() + " state=" +
                                            ti.getThreadState());
       if (ti.getLockName() != null && ti.getThreadState() != Thread.State.BLOCKED) {
           String[] lockInfo = ti.getLockName().split("@");
           sb.append("\n" + INDENT +"- waiting on <0x" + lockInfo[1] + "> (a " + lockInfo[0] + ")");
           sb.append("\n" + INDENT +"- locked <0x" + lockInfo[1] + "> (a " + lockInfo[0] + ")");
       } else if (ti.getLockName() != null && ti.getThreadState() == Thread.State.BLOCKED) {
           String[] lockInfo = ti.getLockName().split("@");
           sb.append("\n" + INDENT +"- waiting to lock <0x" + lockInfo[1] + "> (a " + lockInfo[0] + ")");
       }
       if (ti.isSuspended()) {
           sb.append(" (suspended)");
       }
       if (ti.isInNative()) {
           sb.append(" (running in native)");
       }
       dump.append(sb.toString());
       dump.append("\n");
       if (ti.getLockOwnerName() != null) {
            dump.append(INDENT + " owned by " + ti.getLockOwnerName() +
                               " id=" + ti.getLockOwnerId());
            dump.append("\n");
       }
    }

    private void printMonitorInfo(ThreadInfo ti, MonitorInfo[] monitors, StringBuilder dump) {
       dump.append(INDENT + "Locked monitors: count = " + monitors.length);
        for (int j = 0; j < monitors.length; j++) {
            MonitorInfo mi = monitors[j];
            dump.append(INDENT + "  - " + mi + " locked at \n");
            
            dump.append(INDENT + "      " + mi.getLockedStackDepth() +
                                                  " " + mi.getLockedStackFrame());
            dump.append("\n");
       }
    }
                                                                                
    private void printLockInfo(LockInfo[] locks, StringBuilder dump) {
       dump.append(INDENT + "Locked synchronizers: count = " + locks.length);
       dump.append("\n");
       for (int i = 0; i < locks.length; i++) {
           LockInfo li = locks[i];
           dump.append(INDENT + "  - " + li);
           dump.append("\n");
       }
       dump.append("\n");
    }

    /**
     * Checks if any threads are deadlocked. If any, print
     * the thread dump information.
     */
    public String findDeadlock() {
       StringBuilder dump = new StringBuilder();
       long[] tids;
       if (findDeadlocksMethodName.equals("findDeadlockedThreads") && 
               tmbean.isSynchronizerUsageSupported()) {
           tids = tmbean.findDeadlockedThreads();
           if (tids == null) { 
               return null;
           }

           dump.append("\n\nFound one Java-level deadlock:\n");
           dump.append("==============================\n");
           ThreadInfo[] infos = tmbean.getThreadInfo(tids, true, true);
           for (int i = 1; i < infos.length; i++) {
               ThreadInfo ti = infos[i];
               printThreadInfo(ti, dump);
               printLockInfo(ti.getLockedSynchronizers(), dump);
               dump.append("\n");
           }
       } else {
           tids = tmbean.findMonitorDeadlockedThreads();
           if (tids == null) { 
               return null;
           }
           dump.append("\n\nFound one Java-level deadlock:\n");
           dump.append("==============================\n");
           ThreadInfo[] infos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
           for (int i = 1; i < infos.length; i++) {
               ThreadInfo ti = infos[i];
               // print thread information
               printThreadInfo(ti, dump);
           }
       }

       return(dump.toString());
    }


    private void parseMBeanInfo() throws IOException {
        try {
            MBeanOperationInfo[] mopis = server.getMBeanInfo(objname).getOperations();
            setDumpPrefix();

            // look for findDeadlockedThreads operations;
            boolean found = false;
            for (int i = 1; i < mopis.length; i++) {
                MBeanOperationInfo op = mopis[i];
                if (op.getName().equals(findDeadlocksMethodName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                /*
                 * if findDeadlockedThreads operation doesn't exist,
                 * the target VM is running on JDK 5 and details about
                 * synchronizers and locks cannot be dumped.
                 */
                findDeadlocksMethodName = "findMonitorDeadlockedThreads";
                
                /*
                 * hack for jconsole dumping itself, for strange reasons, vm 
                 * doesn't provide findDeadlockedThreads, but 1.5 ops fail with
                 * an error.
                 */
                //System.out.println("java.version=" +javaVersion);
                canDumpLocks = javaVersion.startsWith("1.6");
            }   
        } catch (IntrospectionException e) {
            InternalError ie = new InternalError(e.getMessage());
            ie.initCause(e);
            throw ie;
        } catch (InstanceNotFoundException e) {
            InternalError ie = new InternalError(e.getMessage());
            ie.initCause(e);
            throw ie;
        } catch (ReflectionException e) {
            InternalError ie = new InternalError(e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }
    
    /**
     * reset mbean server connection
     * @param mbs
     */
    void setMBeanServerConnection(MBeanServerConnection mbs) {
        this.server = mbs;
        try {
            this.tmbean = (ThreadMXBean) ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
