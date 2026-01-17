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
package de.grimmfrost.tda;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import junit.framework.*;
import java.util.Map;
import java.util.Vector;

/**
 * test parsing of log files from sun vms.
 * @author irockel
 */
public class SunJDKParserTest extends TestCase {
    
    public SunJDKParserTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SunJDKParserTest.class);
        
        return suite;
    }

    /**
     * Test of hasMoreDumps method, of class de.grimmfrost.tda.SunJDKParser.
     */
    public void testDumpLoad() throws FileNotFoundException, IOException {
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
    
    public void testJava8DumpLoad() throws FileNotFoundException, IOException {
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
    
    public void testHPDumps()  throws FileNotFoundException, IOException {
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
    
    public void testRemoteVisualVMDumps()  throws FileNotFoundException, IOException {
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

    public void testURLThreadNameDumps()  throws FileNotFoundException, IOException {
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

    public void testVirtualThreadDumps() throws FileNotFoundException, IOException {
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

    public void testLongRunningDetectionWithVariableFields() throws FileNotFoundException, IOException {
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
            assertTrue("Should have children", root.getChildCount() > 0);
            DefaultMutableTreeNode resultNode = (DefaultMutableTreeNode) root.getChildAt(0);
            
            // We expect at least 2 long running threads ("C2 CompilerThread0" and "VM Periodic Task Thread")
            assertTrue("Should find at least one long running thread, found: " + resultNode.getChildCount(), resultNode.getChildCount() > 0);
            
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
