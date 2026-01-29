package de.grimmfrost.tda.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;
import javax.swing.SwingUtilities;

/**
 * Manages logging for TDA.
 */
public class LogManager {
    private static final Logger LOGGER = Logger.getLogger("de.grimmfrost.tda");
    private static boolean initialized = false;
    private static String logFilePath = null;

    private static class ErrorHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
                SwingUtilities.invokeLater(() -> {
                    StatusBar statusBar = StatusBar.getInstance();
                    if (statusBar != null) {
                        statusBar.showErrorIndicator();
                    }
                });
            }
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}
    }

    /**
     * Initializes logging. Should be called early in the application lifecycle.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }

        try {
            String logDir = getLogDirectory();
            File dir = new File(logDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File logFile = new File(dir, "tda.log");
            logFilePath = logFile.getAbsolutePath();

            FileHandler fileHandler = new FileHandler(logFilePath, 1024 * 1024, 5, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            Logger rootLogger = Logger.getLogger("de.grimmfrost.tda");
            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.INFO);
            
            // Also log to console for MCP as it might be useful for some clients (though MCP uses stdout for protocol)
            // But we should be careful not to pollute stdout if it's used for MCP protocol.
            // MCPServer uses System.out for JSON-RPC. Logging to System.err is safer.
            
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }
            
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.WARNING);
            rootLogger.addHandler(consoleHandler);

            rootLogger.addHandler(new ErrorHandler());

            initialized = true;
            LOGGER.info("Logging initialized. Log file: " + logFilePath);
        } catch (IOException e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
        }
    }

    public static String getLogFilePath() {
        return logFilePath;
    }

    private static String getLogDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            String appData = System.getenv("LOCALAPPDATA");
            if (appData != null) {
                return appData + File.separator + "TDA" + File.separator + "Logs";
            }
            return userHome + File.separator + "AppData" + File.separator + "Local" + File.separator + "TDA" + File.separator + "Logs";
        } else if (os.contains("mac")) {
            return userHome + File.separator + "Library" + File.separator + "Logs" + File.separator + "TDA";
        } else {
            // Linux/Unix
            return userHome + File.separator + ".tda" + File.separator + "logs";
        }
    }
    
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}
