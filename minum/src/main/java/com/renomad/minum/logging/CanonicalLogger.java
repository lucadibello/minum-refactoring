package com.renomad.minum.logging;

import com.renomad.minum.logging.model.BaseLogger;
import com.renomad.minum.logging.model.ILogger;
import com.renomad.minum.logging.model.ILoggingLevel;
import com.renomad.minum.queue.ActionQueue;
import com.renomad.minum.state.Constants;
import com.renomad.minum.queue.model.IActionQueue;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Implementation of {@link ILogger}
 */
public class CanonicalLogger extends BaseLogger {
    /**
     * Constructor
     * @param constants used for determining enabled log levels
     * @param executorService provides thread handling for the logs, used to
     *                        build a {@link ActionQueue}
     * @param name sets a name on the {@link ActionQueue} to aid debugging, to
     *             help distinguish queues.
     */
    public CanonicalLogger(Constants constants, ExecutorService executorService, String name) {
        this(constants, executorService, name, null);
    }

    public CanonicalLogger(Constants constants, ExecutorService executorService, String name, IActionQueue loggingActionQueue) {
        this(constants.logLevels, executorService, name, loggingActionQueue);
    }

    public CanonicalLogger(Collection<ILoggingLevel> logLevels, ExecutorService executorService, String name, IActionQueue loggingActionQueue) {
        // this tricky code exists so that a user has the option to create a class extended
        // from this one, and can construct it with the logger instance, making it possible
        // to inject the running action queue.  This enables us to continue using the same
        // action queue amongst descendant classes.
        super(logLevels, executorService, name, Objects.requireNonNullElseGet(
                loggingActionQueue, () -> new ActionQueue(
                        "loggerPrinter" + name,
                        executorService,
                        logLevels
                ).initialize()));
    }

    /**
     * A constructor meant for use by descendant classes
     * @param canonicalLogger an existing instance of a running logger, needed in order to have the
     *               descendant logger using the same {@link IActionQueue}, which is
     *               necessary so logs don't interleave with each other.
     */
    public CanonicalLogger(CanonicalLogger canonicalLogger) {
        super(canonicalLogger);
    }

    // Some utility methods for logging canonical log levels (DEBUG, TRACE, AUDIT, ASYNC_ERROR)

    public void logDebug(ThrowingSupplier<String, Exception> msg) {
        log(msg, LoggingLevel.DEBUG);
    }

    public void logTrace(ThrowingSupplier<String, Exception> msg) {
        log(msg, LoggingLevel.TRACE);
    }

    public void logAudit(ThrowingSupplier<String, Exception> msg) {
        log(msg, LoggingLevel.AUDIT);
    }

    public void logAsyncError(ThrowingSupplier<String, Exception> msg) {
        log(msg, LoggingLevel.ASYNC_ERROR);
    }
}
