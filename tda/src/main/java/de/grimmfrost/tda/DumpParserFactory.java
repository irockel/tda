/*
 * DumpParserFactory.java
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

package de.grimmfrost.tda;

import de.grimmfrost.tda.utils.DateMatcher;
import de.grimmfrost.tda.utils.PrefManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Factory for the dump parsers.
 *
 * @author irockel
 */
public class DumpParserFactory {
    private static DumpParserFactory instance = null;
    
    /** 
     * singleton private constructor 
     */
    private DumpParserFactory() {
    }
    
    /**
     * get the singleton instance of the factory
     * @return singleton instance
     */
    public static DumpParserFactory get() {
        if(instance == null) {
            instance = new DumpParserFactory();
        }
        
        return(instance);
    }
    
    /**
     * parses the given logfile for thread dumps and return a proper jdk parser (either for Sun VM's or
     * for JRockit/Bea VM's) and initializes the DumpParser with the stream.
     * @param dumpFileStream the file stream to use for dump parsing.
     * @param threadStore the map to store the found thread dumps.
     * @param withCurrentTimeStamp only used by SunJDKParser for running in JConsole-Plugin-Mode,  it then uses
     *                             the current time stamp instead of a parsed one.
     * @return a proper dump parser for the given log file, null if no proper parser was found.
     */
    public DumpParser getDumpParserForLogfile(InputStream dumpFileStream, Map threadStore, boolean withCurrentTimeStamp, int startCounter) {
        BufferedReader bis = null;
        int readAheadLimit = PrefManager.get().getStreamResetBuffer();
        int lineCounter = 0;
        DumpParser currentDumpParser = null;
            
        try {
            bis = new BufferedReader(new InputStreamReader(dumpFileStream));
            
            // reset current dump parser
            DateMatcher dm = new DateMatcher();
            while (bis.ready() && (currentDumpParser == null)) {
                bis.mark(readAheadLimit);
                String line = bis.readLine();
                dm.checkForDateMatch(line);
                if (WrappedSunJDKParser.checkForSupportedThreadDump(line)) {
                  currentDumpParser = new WrappedSunJDKParser(bis, threadStore, lineCounter, withCurrentTimeStamp, startCounter, dm);
                } else if(SunJDKParser.checkForSupportedThreadDump(line)) {
                    currentDumpParser = new SunJDKParser(bis, threadStore, lineCounter, withCurrentTimeStamp, startCounter, dm);
                } else if(BeaJDKParser.checkForSupportedThreadDump(line)) {
                    currentDumpParser = new BeaJDKParser(bis, threadStore, lineCounter, dm);
                }
                lineCounter++;
            }
            //System.out.println("Selected Dump Parser: " + currentDumpParser.getClass().getName());
            if ((currentDumpParser != null) && (bis != null)) {
                bis.reset();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return currentDumpParser;
    }    
}
