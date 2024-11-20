package com.renomad.minum.logging;

import com.renomad.minum.queue.ActionQueue;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

public final class DefaultLogger<T extends Enum<?>> extends BaseLogger<T> {
    public DefaultLogger(Collection<T> loggingLevels, ExecutorService executorService, String name, ActionQueue loggingActionQueue) {
        // FIXME: Pass the logging levels to the action queue
        super(loggingLevels, executorService, name, loggingActionQueue);
    }
}
