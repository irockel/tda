/*
 * DumpParserFactoryTest.java
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
 * $Id: DumpParserFactoryTest.java,v 1.5 2008-02-15 09:05:04 irockel Exp $
 */
package de.grimmfrost.tda.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author irockel
 */
public class DumpParserFactoryTest {
    
    @BeforeEach
    protected void setUp() throws Exception {
    }

    @AfterEach
    protected void tearDown() throws Exception {
    }

    /**
     * Test of get method, of class de.grimmfrost.tda.DumpParserFactory.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        
        DumpParserFactory result = DumpParserFactory.get();
        assertNotNull(result);                
    }

    /**
     * Test of getDumpParserForVersion method, of class de.grimmfrost.tda.DumpParserFactory.
     */
    @Test
    public void testGetDumpParserForSunLogfile() throws FileNotFoundException {
        System.out.println("getDumpParserForVersion");
        
        InputStream dumpFileStream = new FileInputStream("src/test/resources/test.log");
        Map threadStore = null;
        DumpParserFactory instance = DumpParserFactory.get();
        
        DumpParser result = instance.getDumpParserForLogfile(dumpFileStream, threadStore, false, 0);
        assertNotNull(result);
        
        assertTrue(result instanceof SunJDKParser);
    }

    /**
     * Test of getDumpParserForVersion method, of class de.grimmfrost.tda.DumpParserFactory.
     */
    @Test
    public void testGetDumpParserForBeaLogfile() throws FileNotFoundException {
        System.out.println("getDumpParserForVersion");
        
        InputStream dumpFileStream = new FileInputStream("src/test/resources/jrockit_15_dump.txt");
        Map threadStore = null;
        DumpParserFactory instance = DumpParserFactory.get();
        
        DumpParser result = instance.getDumpParserForLogfile(dumpFileStream, threadStore, false, 0);
        assertNotNull(result);
        
        assertTrue(result instanceof BeaJDKParser);
    }    
}
