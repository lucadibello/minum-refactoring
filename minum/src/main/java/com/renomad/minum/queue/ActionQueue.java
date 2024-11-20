package com.renomad.minum.queue;

import com.renomad.minum.logging.CanonicalLogger;
import com.renomad.minum.logging.model.ILogger;
import com.renomad.minum.queue.model.BaseActionQueue;
import com.renomad.minum.queue.model.IActionQueue;
import com.renomad.minum.state.Context;
import com.renomad.minum.utils.*;

import java.util.concurrent.*;

/**
 * This class provides the ability to pop items into
 * a queue thread-safely and know they'll happen later.
 * <p>
 * For example, this is helpful for minum.logging, or passing
 * functions to a minum.database.  It lets us run a bit faster,
 * since the I/O actions are happening on a separate
 * thread and the only time required is passing the
 * function of what we want to run later.
 */
public final class ActionQueue extends BaseActionQueue {
    /**
     * See the {@link ActionQueue} description for more detail. This
     * constructor will build your new action queue and handle registering
     * it with a list of other action queues in the {@link Context} object.
     * @param name give this object a unique, explanatory name.
     */
    public ActionQueue(String name, Context context) {
        this(name, context.getExecutorService());
        context.getActionQueueState().offerToQueue(this);
    }

    public ActionQueue(String name, Context context, Collect<LoggingLevel> logLevels) {
        this(name, context.getExecutorService());
        context.getActionQueueState().offerToQueue(this);
    }

    public ActionQueue(String name, ExecutorService executorService) {
        super(name, executorService);
    }

    /**
     * This will prevent any new actions being
     * queued (by setting the stop flag to true and thus
     * causing an exception to be thrown
     * when a call is made to [enqueue]) and will
     * block until the queue is empty.
     */
    @Override
    public void stop() {
        stop(5, 20);
    }

    @Override
    protected void runAction() throws InterruptedException {
        RunnableWithDescription action = getQueue().take();
        action.run();
    }

    @Override
    protected void logMessage(String message) {
        // FIXME: We need to find a solution for logging.
    }
}