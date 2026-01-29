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
import java.util.HashMap;
import java.util.Map;

/**
 * test if the dump parser factory selects the right dump parser for the provided log files.
 * @author irockel
 */
public class DumpParserFactoryTest {
    
    @BeforeEach
    protected void setUp() {
    }

    @AfterEach
    protected void tearDown() {
    }

    /**
     * Test of get method, of class de.grimmfrost.tda.DumpParserFactory.
     */
    @Test
    public void testGet() {
        DumpParserFactory result = DumpParserFactory.get();
        assertNotNull(result);                
    }

    /**
     * Test of getDumpParserForVersion method, of class de.grimmfrost.tda.DumpParserFactory.
     */
    @Test
    public void testGetDumpParserForSunLogfile() throws FileNotFoundException {
        InputStream dumpFileStream = new FileInputStream("src/test/resources/test.log");
        Map<String, Map<String, String>> threadStore = new HashMap<>();
        DumpParserFactory instance = DumpParserFactory.get();
        
        DumpParser result = instance.getDumpParserForLogfile(dumpFileStream, threadStore, false, 0);
        assertNotNull(result);

        assertInstanceOf(SunJDKParser.class, result);
    }

    /**
     * Test of getDumpParserForVersion method, of class de.grimmfrost.tda.DumpParserFactory.
     */
    @Test
    public void testGetDumpParserForJSONLogfile() throws FileNotFoundException {
        InputStream dumpFileStream = new FileInputStream("src/test/resources/intellij_dump.json");
        Map<String, Map<String, String>> threadStore = new HashMap<>();
        DumpParserFactory instance = DumpParserFactory.get();

        DumpParser result = instance.getDumpParserForLogfile(dumpFileStream, threadStore, false, 0);
        assertNotNull(result);

        assertInstanceOf(JCmdJSONParser.class, result);
    }
}
