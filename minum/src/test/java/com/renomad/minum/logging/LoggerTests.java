package com.renomad.minum.logging;

import com.renomad.minum.logging.model.LoggingLevel;
import com.renomad.minum.queue.ActionQueue;
import com.renomad.minum.state.Constants;
import com.renomad.minum.state.Context;
import com.renomad.minum.utils.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static com.renomad.minum.testing.TestFramework.*;

public class LoggerTests {
    private Context context;
    private TestLogger logger;

    @Before
    public void init() {
        context = buildTestingContext("TestLogger tests");
        logger = (TestLogger) context.getLogger();
    }

    @After
    public void cleanup() {
        shutdownTestingContext(context);
    }


    /**
     * The {@link ActionQueue} is what enables the log to send its
     * messages for later output.  It's critical.  But, there are situations
     * where the queue would not be available - primarily, 1) when the system
     * has just started up, and 2) When it's shutting down.  During those phases,
     * parts of the system are being spun up or shut down.
     * <br>
     * This tests where the LoggingActionQueue is stopped
     */
    @Test
    public void testLogger_EdgeCase_LoggingActionQueueStopping() {
        Properties props = new Properties();
        var constants = new Constants(props);
        var testQueue = new ActionQueue("test queue", context, constants.logLevels);
        var local = new CanonicalLogger(constants.logLevels, null, "test logger", testQueue);
        testQueue.stop(0,0);
        local.log(() -> "testing...", LoggingLevel.AUDIT);

        var ex = assertThrows(TestLoggerException.class, () -> logger.findFirstMessageThatContains("testing..."));
        assertTrue(ex.getMessage().contains("testing... was not found in"));
    }

    /**
     * When writing logs, if there is whitespace it is helpful to convert it
     * to a more apparent form, otherwise it could be overlooked. For example,
     * an entirely blank string is: (BLANK)
     */
    @Test
    public void testShowWhiteSpace() {
        assertEquals(StringUtils.showWhiteSpace(" "), "(BLANK)");
        assertEquals(StringUtils.showWhiteSpace(""), "(EMPTY)");
        assertEquals(StringUtils.showWhiteSpace(null), "(NULL)");
        assertEquals(StringUtils.showWhiteSpace("\t\r\n"), "\\t\\r\\n");
    }

    /**
     * This is a sample of code for enabling and disabling the TRACE
     * level of logging.
     */
    @Test
    public void testEnableAndDisableTrace() {
        logger.logTrace(() -> "You can't see me!");
        boolean status = logger.enableLogLevel(LoggingLevel.TRACE);
        assertTrue(status);
        logger.logTrace(() -> "But you can see this.");
        status = logger.disableLogLevel(LoggingLevel.TRACE);
        assertTrue(status);
        logger.logTrace(() -> "You can't see me!");
    }

    /**
     * Users may want to extend logger to add new logging levels
     * they can control.  Here is an example.
     */
    @Test
    public void testUsingDescendantLogger() {
        DescendantLogger descendantLogger = new DescendantLogger(context.getLogger());
        descendantLogger.logRequests(() -> "Incoming request from 123.123.123.123: foo foo");
        assertTrue((descendantLogger).doesMessageExist("Incoming request from 123.123.123.123: foo foo"));
    }
}
