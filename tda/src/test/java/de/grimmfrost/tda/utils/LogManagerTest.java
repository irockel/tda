package de.grimmfrost.tda.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LogManagerTest {
    @Test
    public void testInitInTestEnvironment() {
        // Since we are running in JUnit, LogManager.init() should detect it and bypass
        LogManager.init();
        
        // If it was bypassed, logFilePath should remain null
        assertNull(LogManager.getLogFilePath(), "Log file path should be null in test environment");
    }
}
