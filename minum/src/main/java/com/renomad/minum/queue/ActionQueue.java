package com.renomad.minum.queue;

import com.renomad.minum.logging.model.ILoggingLevel;
import com.renomad.minum.queue.model.BaseActionQueue;
import com.renomad.minum.state.Context;

import java.util.Collection;
import java.util.concurrent.*;

/**
 * FIXME: complete this comment
 * Concrete implementation of the {}
 */
public final class ActionQueue extends BaseActionQueue {

    /**
     * See the {@link ActionQueue} description for more detail. This
     * constructor will build your new action queue and handle registering
     * it with a list of other action queues in the {@link Context} object.
     * @param name give this object a unique, explanatory name.
     */
    public ActionQueue(String name, Context context) {
        super(name, context);
        context.getActionQueueState().offerToQueue(this);
    }

    public ActionQueue(String name, ExecutorService executorService, Collection<ILoggingLevel> logLevels) {
        super(name, executorService, logLevels);
    }
}