package com.renomad.minum.queue.model;

import com.renomad.minum.logging.CanonicalLogger;
import com.renomad.minum.logging.model.ILoggingLevel;
import com.renomad.minum.state.Context;
import com.renomad.minum.utils.*;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This abstract class provides the ability to pop items into
 * a queue thread-safely and know they'll happen later.
 * <p>
 * For example, this is helpful for minum.logging, or passing
 * functions to a minum.database.  It lets us run a bit faster,
 * since the I/O actions are happening on a separate
 * thread and the only time required is passing the
 * function of what we want to run later.
 */
public abstract class BaseActionQueue implements IActionQueue {
    private final String name;
    private final ExecutorService executorService;
    private final LinkedBlockingQueue<RunnableWithDescription> queue;
    private Thread queueThread;
    private final CanonicalLogger logger;
    private boolean isShuttingDown = false;
    private boolean isClosed = false;

    /**
     * See the {@link BaseActionQueue} description for more detail. This
     * constructor will build your new action queue and handle registering
     * it with a list of other action queues in the {@link Context} object.
     * @param name give this object a unique, explanatory name.
     * @param context application context containing a valid logger and
     *                ExecutorService instance.
     */
    public BaseActionQueue(String name, Context context) {
        this.name = name;
        this.queue = new LinkedBlockingQueue<>();
        this.executorService = context.getExecutorService();
        this.logger = context.getLogger();
    }

    public BaseActionQueue(String name, ExecutorService executorService, Collection<ILoggingLevel> logLevels) {
        this.name = name;
        this.executorService = executorService;
        this.queue = new LinkedBlockingQueue<>();
        this.logger = new CanonicalLogger(logLevels, executorService, name, this);
    }

    private void runNextAction() throws InterruptedException {
        RunnableWithDescription action = queue.take();
        try {
            action.run();
        } catch (Exception e) {
            logger.logAsyncError(() -> StacktraceUtils.stackTraceToString(e));
        }
    }

    // Regarding the InfiniteLoopStatement - indeed, we expect that the while loop
    // below is an infinite loop unless there's an exception thrown, that's what it is.
    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public IActionQueue initialize() {
        Runnable centralLoop = () -> {
            Thread.currentThread().setName(name);
            this.queueThread = Thread.currentThread();
            try {
                while (true) {
                    runNextAction();
                }
            } catch (InterruptedException ex) {
                /*
                this is what we expect to happen.
                once this happens, we just continue on.
                this only gets called when we are trying to shut everything
                down cleanly
                 */
                this.logger.logDebug(() -> String.format("%s LoggingActionQueue for %s is stopped.%n", TimeUtils.getTimestampIsoInstant(), name));
                Thread.currentThread().interrupt();
            }
        };
        executorService.submit(centralLoop);
        return this;
    }

    /**
     * Adds something to the queue to be processed.
     * <p>
     *     Here is an example use of .enqueue:
     * </p>
     * <p>
     * <pre>
     * {@code   actionQueue.enqueue("Write person file to disk at " + filePath, () -> {
     *             Files.writeString(filePath, pf.serialize());
     *         });}
     * </pre>
     * </p>
     */
    @Override
    public void enqueue(String description, ThrowingRunnable action) {
        if (!isClosed()) {
            queue.add(new RunnableWithDescription(action, description));
        } else {
            throw new UtilsException(String.format("failed to enqueue %s - ActionQueue \"%s\" is stopped", description, this.name));
        }
    }

    /**
     * Stops the action queue
     * @param count how many loops to wait before we crash it closed
     * @param sleepTime how long to wait in milliseconds between loops
     */
    @Override
    public void stop(int count, int sleepTime) {
        logger.logDebug(() -> String.format("%s Stopping queue %s%n", TimeUtils.getTimestampIsoInstant(), this));
        isShuttingDown = true;
        for (int i = 0; i < count; i++) {
            if (queue.isEmpty()) return;
            logger.logDebug(() -> String.format("%s Queue not yet empty, has %d elements. waiting...%n", TimeUtils.getTimestampIsoInstant(), queue.size()));
            MyThread.sleep(sleepTime);
        }
        isClosed = true;
        logger.logDebug(() -> String.format("%s Queue %s has %d elements left but we're done waiting.  Queue toString: %s", TimeUtils.getTimestampIsoInstant(), this, queue.size(), queue));
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

    /**
     * This method is used to get a copy of the current list of actions
     * in the queue.
     * @return LinkedBlockingQueue of RunnableWithDescription objects
     *        that are currently in the queue.
     */
    @Override
    public LinkedBlockingQueue<RunnableWithDescription> getQueue() {
        return new LinkedBlockingQueue<>(queue);
    }

    @Override
    public Thread getQueueThread() {
        return this.queueThread;
    }

    @Override
    public boolean isClosed() {
        return this.isShuttingDown || this.isClosed;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
