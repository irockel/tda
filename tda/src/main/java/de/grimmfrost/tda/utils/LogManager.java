package de.grimmfrost.tda.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        // Check if running in a test environment (e.g., Maven/Surefire)
        boolean isTestEnv = System.getProperty("surefire.real.class.path") != null || 
                           System.getProperty("junit.jupiter.execution.parallel.enabled") != null ||
                           System.getProperty("java.class.path").contains("junit-platform-launcher");

        if (isTestEnv) {
            // In test environment, don't use the custom file-based logging.
            // Let Maven handle the logging.
            initialized = true;

            Logger rootLogger = Logger.getLogger("de.grimmfrost.tda");
            rootLogger.setUseParentHandlers(false);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.WARNING);
            consoleHandler.setFormatter(new CompactFormatter());
            rootLogger.addHandler(consoleHandler);

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
            fileHandler.setFormatter(new CompactFormatter());
            fileHandler.setLevel(Level.ALL);

            Logger rootLogger = Logger.getLogger("de.grimmfrost.tda");
            rootLogger.setUseParentHandlers(false);
            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.INFO);
            
            // Also log to console for MCP as it might be useful for some clients (though MCP uses stdout for protocol)
            // But we should be careful not to pollute stdout if it's used for MCP protocol.
            // MCPServer uses System.out for JSON-RPC. Logging to System.err is safer.
            
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.WARNING);
            consoleHandler.setFormatter(new CompactFormatter());
            rootLogger.addHandler(consoleHandler);

            rootLogger.addHandler(new ErrorHandler());

            initialized = true;
            LOGGER.info("Logging initialized. Log file: " + logFilePath);
        } catch (IOException e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
        }
    }

    static class CompactFormatter extends Formatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFormat.format(new Date(record.getMillis())));
            sb.append(" ");
            sb.append(formatLevel(record.getLevel()));
            sb.append(" [");
            sb.append(getSimpleClassName(record.getSourceClassName()));
            sb.append(".");
            sb.append(record.getSourceMethodName());
            sb.append("] ");
            sb.append(formatMessage(record));
            sb.append("\n");
            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw);
                } catch (Exception ex) {
                    // ignore
                }
            }
            return sb.toString();
        }

        private String formatLevel(Level level) {
            if (level == Level.SEVERE) return "SEV";
            if (level == Level.WARNING) return "WRN";
            if (level == Level.INFO) return "INF";
            if (level == Level.CONFIG) return "CFG";
            if (level == Level.FINE) return "FIN";
            if (level == Level.FINER) return "FNR";
            if (level == Level.FINEST) return "FST";
            return level.getName().substring(0, Math.min(3, level.getName().length())).toUpperCase();
        }

        private String getSimpleClassName(String className) {
            if (className == null) return "unknown";
            int lastDot = className.lastIndexOf('.');
            if (lastDot >= 0) {
                return className.substring(lastDot + 1);
            }
            return className;
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
