/*
 * AppInfo.java
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
 */
package de.grimmfrost.tda.utils;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * provides static application information like name and version
 * @author irockel
 */
public class AppInfo {
    private static final Logger LOGGER = LogManager.getLogger(AppInfo.class);
    private static final String APP_SHORT_NAME = "TDA";
    private static final String APP_FULL_NAME = "Thread Dump Analyzer";
    private static String VERSION = "unknown";
    
    private static final String COPYRIGHT = "2006-2026";

    static {
        try (InputStream is = AppInfo.class.getResourceAsStream("/de/grimmfrost/tda/version.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                VERSION = props.getProperty("version", "unknown");
            }
        } catch (Exception e) {
            // fallback to unknown or log error
            LOGGER.log(Level.SEVERE, "Failed to load version properties", e);
        }
    }
    
    /**
     * get info text for status bar if no real info is displayed.
     */
    public static String getStatusBarInfo() {
        return(APP_SHORT_NAME + " - " + APP_FULL_NAME + " " + VERSION);
    }
    
    public static String getAppInfo() {
        return(APP_SHORT_NAME + " - " + APP_FULL_NAME);
    }
    
    public static String getVersion() {
        return(VERSION);
    }
    
    public static String getCopyright() {
        return(COPYRIGHT);
    }
}
