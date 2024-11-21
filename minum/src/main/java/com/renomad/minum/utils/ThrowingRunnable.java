package com.renomad.minum.utils;

import com.renomad.minum.logging.LoggingLevel;
import com.renomad.minum.logging.model.ILogger;
import com.renomad.minum.logging.model.ILoggingLevel;

/**
 * This exists so that we are able to slightly better manage
 * exceptions in a highly threaded system.  Here's the thing:
 * exceptions stop bubbling up at the thread invocation. If we
 * don't take care to deal with that in some way, we can easily
 * just lose the information.  Something could be badly broken and
 * we could be totally oblivious to it.  This interface is to
 * alleviate that situation.
 */
@FunctionalInterface
public interface ThrowingRunnable {

    /**
     * The reason this throws an exception is so that lambdas
     * don't need to try-catch their thrown exceptions when they
     * use this.
     */
    void run() throws Exception;

    static Runnable throwingRunnableWrapper(ThrowingRunnable throwingRunnable, ILogger<ILoggingLevel> logger) {
        return () -> {
            try {
                throwingRunnable.run();
            } catch (Exception ex) {
                logger.log(() -> StacktraceUtils.stackTraceToString(ex), LoggingLevel.ASYNC_ERROR);
            }
        };
    }
}