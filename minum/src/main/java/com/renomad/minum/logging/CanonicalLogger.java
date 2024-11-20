package com.renomad.minum.logging;

import com.renomad.minum.logging.model.LoggingLevel;
import com.renomad.minum.logging.model.ThrowingSupplier;
import com.renomad.minum.queue.model.IActionQueue;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

/**
 * Implementation of {@link BaseLogger}
 */
public class CanonicalLogger extends BaseLogger<LoggingLevel> {

    public CanonicalLogger(Collection<LoggingLevel> loggingLevels, ExecutorService executorService, String name) {
        this(loggingLevels, executorService, name, null);
    }

    CanonicalLogger(Collection<LoggingLevel> loggingLevels, ExecutorService executorService, String name, IActionQueue loggingActionQueue) {
        super(loggingLevels, executorService, name, loggingActionQueue);
    }

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
