/**
 * class Browser Copyright (C) 1999-2001 Fredrik Ehnbom <fredde@gjt.org>
 * available at
 * <http://www.gjt.org/servlets/JCVSlet/show/gjt/org/gjt/fredde/util/net/Browser.java/HEAD>
 * used under the terms of the GNU public license
 *
 * Launches the default browser of the current OS with the supplied URL.
 *
 * $Id: Browser.java,v 1.1 2007-05-03 09:18:07 irockel Exp $
 */
package de.grimmfrost.tda.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * helper class for launching the default browser
 */
public class Browser { 
    private static final Logger LOGGER = LogManager.getLogger(Browser.class);
    
    /**
     * Starts the default browser for the current platform.
     *
     * @param url The link to point the browser to.
     */
    public static void open(String url) throws InterruptedException, IOException {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        // Try java.awt.Desktop first (standard Java API)
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
                return;
            } catch (URISyntaxException | IOException e) {
                LOGGER.log(Level.WARNING, "Failed to open browser via Desktop API, falling back to manual command", e);
            }
        }

        // Fallback to manual commands
        String cmd = null;
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            cmd = "rundll32 url.dll,FileProtocolHandler " + maybeFixupURLForWindows(url);
        } else if (os.contains("mac")) {
            cmd = "open " + url;
        } else {
            // Unix/Linux fallback
            if(System.getenv("BROWSER") != null) {
                cmd = System.getenv("BROWSER") + " " + url;
            } else {
                // Try common linux browser launchers
                String[] launchers = {"xdg-open", "gnome-open", "kfmclient", "firefox", "google-chrome"};
                for (String launcher : launchers) {
                    try {
                        if (Runtime.getRuntime().exec(new String[]{"which", launcher}).waitFor() == 0) {
                            cmd = launcher + " " + url;
                            break;
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }

        if (cmd != null) {
            Runtime.getRuntime().exec(cmd);
        } else {
            LOGGER.log(Level.SEVERE, "Could not find a way to open URL: " + url);
        }
    }
    
    /**
     * If the default browser is Internet Explorer 5.0 or greater,
     * the URL.DLL program fails if the url ends with .htm or .html .
     * This problem is described by Microsoft at
     * http://support.microsoft.com/support/kb/articles/Q283/2/25.ASP
     * Of course, their suggested workaround is to use the classes from the
     * microsoft Java SDK, but fortunately another workaround does exist.
     * If you alter the url slightly so it no longer ends with ".htm",
     * the URL can launch successfully. The logic here appends a null query
     * string onto the end of the URL if none is already present, or
     * a bogus query parameter if there is already a query string ending in
     * ".htm"
     */
    private static String maybeFixupURLForWindows(String url) {
        // plain filenames (e.g. c:\some_file.html or \\server\filename) do
        // not need fixing.
        if (url == null || url.length() < 2 || url.charAt(0) == '\\' || url.charAt(1) == ':')
            return url;
        String lower_url = url.toLowerCase();
        int i = badEndings.length;
        while (i-- > 0)
            if (lower_url.endsWith(badEndings[i]))
                return fixupURLForWindows(url);
        return url;
    }
    
    static final String[] badEndings = { ".htm", ".html", ".htw", ".mht", ".cdf", ".mhtml", ".stm" };
    
    private static String fixupURLForWindows(String url) {
        if (url.indexOf('?') == -1)
            return url + "?";
        else return url + "&workaroundStupidWindowsBug";
    }
    
    /**
     * Checks if the OS is windows.
     *
     * @return true if it is, false if it's not.
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
