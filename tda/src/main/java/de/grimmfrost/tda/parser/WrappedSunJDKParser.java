/*
 * WrappedSunJDKParser.java
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
 */
package de.grimmfrost.tda.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import de.grimmfrost.tda.utils.DateMatcher;

public class WrappedSunJDKParser extends SunJDKParser {

  /**
   * Creates a new instance of WrappedSunJDKParser: A SunJDKParser reading a log
   * file created by the Tanuki Service Wrapper.
   */
  public WrappedSunJDKParser(BufferedReader bis, Map<String, Map<String, String>> threadStore, int lineCounter,
                             boolean withCurrentTimeStamp, int startCounter, DateMatcher dm) {
    super(bis, threadStore, lineCounter, withCurrentTimeStamp, startCounter, dm);
  }

  /**
   * check if the passed logline contains the beginning of a sun jdk thread
   * dump.
   *
   * @param logLine the line of the logfile to test
   * @return true, if the start of a sun thread dump is detected.
   */
  public static boolean checkForSupportedThreadDump(String logLine) {
    return logLine.startsWith("INFO   | jvm ")
           && logLine.trim().contains(" | Full thread dump");
  }

  protected String getNextLine() throws IOException {
    return getBis().readLine().substring(42);
  }


}
