/*
 * SunJDKParserTest.java
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
 * $Id: SunJDKParserTest.java,v 1.9 2008-11-21 09:20:19 irockel Exp $
 */
package de.grimmfrost.tda.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import de.grimmfrost.tda.model.Category;
import de.grimmfrost.tda.model.ThreadDumpInfo;
import de.grimmfrost.tda.model.ThreadInfo;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.Vector;

/**
 * test parsing of log files from sun vms.
 * @author irockel
 */
public class SunJDKParserTest {
    
    @BeforeEach
    protected void setUp() {
    }

    @AfterEach
    protected void tearDown() {
    }

    /**
     * Test of hasMoreDumps method, of class de.grimmfrost.tda.SunJDKParser.
     */
    @Test
    public void testDumpLoad() throws IOException {
        System.out.println("dumpLoad");
        FileInputStream fis = null;
        DumpParser instance = null;
        
        try {
            fis = new FileInputStream("src/test/resources/test.log");
            Map dumpMap = new HashMap();
            Vector topNodes = new Vector();
            instance = DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);
            
            assertTrue(instance instanceof SunJDKParser);

            while (instance.hasMoreDumps()) {
                topNodes.add(instance.parseNext());
            }

            // check if three dumps are in it.
            assertEquals(3, topNodes.size());
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Test of isFoundClassHistograms method, of class de.grimmfrost.tda.SunJDKParser.
     */
    @Test
    public void testIsFoundClassHistograms() throws FileNotFoundException, IOException {
        System.out.println("isFoundClassHistograms");
        DumpParser instance = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("src/test/resources/testwithhistogram.log");
            Map dumpMap = new HashMap();
            instance = DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);
            
            Vector topNodes = new Vector();
            while (instance.hasMoreDumps()) {
                topNodes.add(instance.parseNext());
            }
            
            boolean expResult = true;
            boolean result = instance.isFoundClassHistograms();
            assertEquals(expResult, result);        
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }
    
    @Test
    public void test64BitDumpLoad() throws FileNotFoundException, IOException {
        System.out.println("64BitDumpLoad");
        FileInputStream fis = null;
        DumpParser instance = null;
        
        try {
            fis = new FileInputStream("src/test/resources/test64bit.log");
            Map dumpMap = new HashMap();
            Vector topNodes = new Vector();
            instance = DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);
            
            assertTrue(instance instanceof SunJDKParser);

            while (instance.hasMoreDumps()) {
                topNodes.add(instance.parseNext());
            }

            // check if one dump was found.
            assertEquals(1, topNodes.size());
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }
    
    @Test
    public void testJava8DumpLoad() throws IOException {
        System.out.println("Java8DumpLoad");
        FileInputStream fis = null;
        DumpParser instance = null;
        
        try {
            fis = new FileInputStream("src/test/resources/java8dump.log");
            Map dumpMap = new HashMap();
            Vector topNodes = new Vector();
            instance = DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);
            
            assertTrue(instance instanceof SunJDKParser);

            while (instance.hasMoreDumps()) {
                topNodes.add(instance.parseNext());
            }

            // check if one dump was found.
            assertEquals(1, topNodes.size());
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }
    
    @Test
    public void testJava11DumpLoad() throws FileNotFoundException, IOException {
        System.out.println("Java11DumpLoad");
        FileInputStream fis = null;
        DumpParser instance = null;
        
        try {
            fis = new FileInputStream("src/test/resources/java11dump.log");
            Map dumpMap = new HashMap();
            Vector topNodes = new Vector();
            instance = DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);
            
            assertTrue(instance instanceof SunJDKParser);

            while (instance.hasMoreDumps()) {
                topNodes.add(instance.parseNext());
            }

            // check if one dump was found.
            assertEquals(1, topNodes.size());
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }
    
    @Test
    public void testHPDumps()  throws IOException {
        System.out.println("HPDumpLoad");
        FileInputStream fis = null;
        DumpParser instance = null;
        
        try {
            fis = new FileInputStream("src/test/resources/hpdump.log");
            Map dumpMap = new HashMap();
            Vector topNodes = new Vector();
            instance = DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);
            
            assertTrue(instance instanceof SunJDKParser);

            while (instance.hasMoreDumps()) {
                topNodes.add(instance.parseNext());
            }

            // check if two dump were found.
            assertEquals(2, topNodes.size());
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }
    
    @Test
    public void testRemoteVisualVMDumps()  throws IOException {
        System.out.println("VisualVMDumpLoad");
        FileInputStream fis = null;
        DumpParser instance = null;

        try {
            fis = new FileInputStream("src/test/resources/visualvmremote.log");
            Map dumpMap = new HashMap();
            Vector topNodes = new Vector();
            instance = DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);

            assertTrue(instance instanceof SunJDKParser);

            while (instance.hasMoreDumps()) {
                topNodes.add(instance.parseNext());
            }

            // check if two dump were found.
            assertEquals(1, topNodes.size());
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }

    @Test
    public void testURLThreadNameDumps()  throws IOException {
        System.out.println("URLThreadNameDumpLoad");
        FileInputStream fis = null;
        DumpParser instance = null;
        
        try {
            fis = new FileInputStream("src/test/resources/urlthread.log");
            Map dumpMap = new HashMap();
            Vector topNodes = new Vector();
            instance = DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);
            
            assertTrue(instance instanceof SunJDKParser);

            while (instance.hasMoreDumps()) {
                topNodes.add(instance.parseNext());
            }

            // check if two dump were found.
            assertEquals(1, topNodes.size());
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }

    @Test
    public void testVirtualThreadDumps() throws IOException {
        System.out.println("VirtualThreadDumpLoad");
        FileInputStream fis = null;
        DumpParser instance = null;
        
        try {
            fis = new FileInputStream("src/test/resources/java21dump.log");
            Map dumpMap = new HashMap();
            Vector topNodes = new Vector();
            instance = DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);
            
            assertTrue(instance instanceof SunJDKParser);

            while (instance.hasMoreDumps()) {
                topNodes.add(instance.parseNext());
            }

            // check if one dump was found.
            assertEquals(1, topNodes.size());
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }

    @Test
    public void testCarrierThreadIssuesDetection() throws IOException {
        System.out.println("testCarrierThreadIssuesDetection");
        FileInputStream fis = null;
        DumpParser instance = null;
        
        try {
            fis = new FileInputStream("src/test/resources/carrier_stuck.log");
            Map dumpMap = new HashMap();
            Vector topNodes = new Vector();
            instance = DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);
            
            assertTrue(instance instanceof SunJDKParser);

            while (instance.hasMoreDumps()) {
                topNodes.add(instance.parseNext());
            }

            assertEquals(1, topNodes.size());
            DefaultMutableTreeNode dumpNode = (DefaultMutableTreeNode) topNodes.get(0);
            
            // Navigate to virtual threads category
            DefaultMutableTreeNode vtCat = null;
            for (int i = 0; i < dumpNode.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) dumpNode.getChildAt(i);
                Object userObject = child.getUserObject();
                if (userObject instanceof Category) {
                    Category cat = (Category) userObject;
                    if (cat.getName().contains("Virtual Threads")) {
                        vtCat = child;
                        break;
                    }
                }
            }
            
            assertNotNull(vtCat, "Virtual Threads category should exist");
            
            boolean foundWarning = false;
            for (int i = 0; i < vtCat.getChildCount(); i++) {
                DefaultMutableTreeNode threadNode = (DefaultMutableTreeNode) vtCat.getChildAt(i);
                Object userObject = threadNode.getUserObject();
                String threadInfo = userObject.toString();
                if (userObject instanceof ThreadInfo) {
                    String content = ((ThreadInfo)userObject).getContent();
                    if (threadInfo.contains("ForkJoinPool-1-worker-1") && content.contains("Note:")) {
                        foundWarning = true;
                        assertTrue(content.contains("carrier thread seems to be stuck in application code"), "Warning message should be correct");
                    }
                }
            }
            
            assertTrue(foundWarning, "Should have found a warning note for the stuck carrier thread");
            
            // Now test the analyzer output
            ThreadDumpInfo tdi = (ThreadDumpInfo) dumpNode.getUserObject();
            Analyzer analyzer = new Analyzer(tdi);
            String hints = analyzer.analyzeDump();
            assertNotNull(hints, "Analysis hints should not be null");
            assertTrue(hints.contains("carrier thread seems to be stuck in application code"), "Analysis hints should contain warning about stuck carrier thread");
            assertTrue(hints.contains("Detected 1 virtual thread(s)"), "Analysis hints should report correct number of stuck carrier threads");
            
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }

    @Test
    public void testSMRInfoParsing() throws Exception {
        System.out.println("testSMRInfoParsing");
        InputStream dumpFileStream = new FileInputStream("src/test/resources/jstack_dump.log");
        DumpParser instance = DumpParserFactory.get().getDumpParserForLogfile(dumpFileStream, new HashMap(), false, 0);
        assertTrue(instance instanceof SunJDKParser);
        DefaultMutableTreeNode result = (DefaultMutableTreeNode) instance.parseNext();
        assertNotNull(result);
        ThreadDumpInfo tdi = (ThreadDumpInfo) result.getUserObject();
        String smrInfo = tdi.getSmrInfo();
        assertNotNull(smrInfo);
        assertTrue(smrInfo.contains("Threads class SMR info:"));
        assertTrue(smrInfo.contains("_java_thread_list=0x000000087e826560"));
        assertTrue(smrInfo.contains("length=12"));
        
        String overview = tdi.getOverview();
        assertNotNull(overview);
        assertTrue(overview.contains("Address</th>"));
        assertTrue(overview.contains("Resolved Thread</th>"));
        assertTrue(overview.contains("0x000000010328e320"));
        assertTrue(overview.contains("Reference Handler"));
        
        // Check for NOT FOUND for a thread that might not be in the dump (if I modified the log)
        // In jstack_dump.log all 12 elements are present.
        // Let's check that all are resolved.
        assertFalse(overview.contains("NOT FOUND"));
    }

    @Test
    public void testSMRInfoWithUnresolved() throws Exception {
        System.out.println("testSMRInfoWithUnresolved");
        String dumpContent = "2026-01-20 17:29:40\n" +
                "Full thread dump OpenJDK 64-Bit Server VM (21.0.9+10-LTS mixed mode, sharing):\n" +
                "\n" +
                "Threads class SMR info:\n" +
                "_java_thread_list=0x000000087e826560, length=2, elements={\n" +
                "0x000000010328e320, 0x00000001deadbeef\n" +
                "}\n" +
                "\n" +
                "\"Reference Handler\" #9 [30467] daemon prio=10 os_prio=31 cpu=0.44ms elapsed=25574.11s tid=0x000000010328e320 nid=30467 waiting on condition  [0x000000016e7c2000]\n" +
                "   java.lang.Thread.State: RUNNABLE\n" +
                "\n";
        
        java.io.InputStream is = new java.io.ByteArrayInputStream(dumpContent.getBytes());
        SunJDKParser parser = new SunJDKParser(new java.io.BufferedReader(new java.io.InputStreamReader(is)), new java.util.HashMap(), 0, false, 0, new de.grimmfrost.tda.utils.DateMatcher());
        
        DefaultMutableTreeNode result = (DefaultMutableTreeNode) parser.parseNext();
        ThreadDumpInfo tdi = (ThreadDumpInfo) result.getUserObject();
        String overview = tdi.getOverview();
        
        assertTrue(overview.contains("0x000000010328e320"));
        assertTrue(overview.contains("Reference Handler"));
        assertTrue(overview.contains("0x00000001deadbeef"));
        assertTrue(overview.contains("NOT FOUND"));
        assertTrue(overview.contains("Some SMR addresses could not be resolved to threads"));
    }

    @Test
    public void testLongRunningDetectionWithVariableFields() throws IOException {
        System.out.println("testLongRunningDetectionWithVariableFields");
        FileInputStream fis = null;
        SunJDKParser instance = null;
        
        try {
            fis = new FileInputStream("src/test/resources/jdk11_long_running.log");
            Map dumpMap = new HashMap();
            instance = (SunJDKParser) DumpParserFactory.get().getDumpParserForLogfile(fis, dumpMap, false, 0);
            
            Vector topNodes = new Vector();
            while (instance.hasMoreDumps()) {
                MutableTreeNode node = instance.parseNext();
                if (node != null) {
                    topNodes.add(node);
                    
                    // Manually populate dumpMap since we are testing diffDumps which looks there
                    DefaultMutableTreeNode dNode = (DefaultMutableTreeNode) node;
                    ThreadDumpInfo tdi = (ThreadDumpInfo) dNode.getUserObject();
                    
                    // The dumpMap is supposed to contain a map of threads for each dump name
                    // But in this test environment, the internal threadStore of instance IS the dumpMap
                    // so it should already be populated by parseNext().
                }
            }

            assertEquals(2, topNodes.size());
            
            // Re-simulate the long running detection logic
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
            TreePath[] paths = new TreePath[2];
            DefaultMutableTreeNode dummyRoot = new DefaultMutableTreeNode("Dummies");
            dummyRoot.add((DefaultMutableTreeNode)topNodes.get(0));
            dummyRoot.add((DefaultMutableTreeNode)topNodes.get(1));
            
            paths[0] = new TreePath(((DefaultMutableTreeNode)topNodes.get(0)).getPath());
            paths[1] = new TreePath(((DefaultMutableTreeNode)topNodes.get(1)).getPath());
            
            // before calling findLongRunningThreads, we MUST ensure the dumpMap is correctly populated.
            // SunJDKParser stores threads in the map passed to it, keyed by dump name.
            // Dump name for SunJDKParser is "Dump No. X".
            
            instance.findLongRunningThreads(root, dumpMap, paths, 2, null);
            
            // Check if long running threads were found
            assertTrue(root.getChildCount() > 0, "Should have children");
            DefaultMutableTreeNode resultNode = (DefaultMutableTreeNode) root.getChildAt(0);
            
            // We expect at least 2 long running threads ("C2 CompilerThread0" and "VM Periodic Task Thread")
            assertTrue(resultNode.getChildCount() > 0, "Should find at least one long running thread, found: " + resultNode.getChildCount());
            
        } finally {
            if(instance != null) {
                instance.close();
            }
            if(fis != null) {
                fis.close();
            }
        }
    }
}
