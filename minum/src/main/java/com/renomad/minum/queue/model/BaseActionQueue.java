package com.renomad.minum.queue.model;

import com.renomad.minum.utils.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class BaseActionQueue implements IActionQueue {
    private final String name;
    private final ExecutorService executorService;
    private final LinkedBlockingQueue<RunnableWithDescription> queue;
    private boolean stop = false;
    private boolean isStoppedStatus = false;
    private Thread queueThread;

    // Define custom logger for this class
    public BaseActionQueue(String name, ExecutorService executorService) {
        this.name = name;
        this.executorService = executorService;
        this.queue = new LinkedBlockingQueue<>();

        // TODO: Create logger!
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public IActionQueue initialize() {
        Runnable centralLoop = () -> {
            Thread.currentThread().setName(name);
            this.queueThread = Thread.currentThread();
            try {
                while (true) {
                    runAction();
                }
            } catch (InterruptedException ex) {
                /*
                this is what we expect to happen.
                once this happens, we just continue on.
                this only gets called when we are trying to shut everything
                down cleanly
                 */
                logMessage(String.format("ActionQueue for %s is stopped.", name));
                Thread.currentThread().interrupt();
            }
        };
        executorService.submit(centralLoop);
        return this;
    }


    @Override
    public void enqueue(String description, ThrowingRunnable action) {
        if (!stop) {
            queue.add(new RunnableWithDescription(action, description));
        } else {
            throw new UtilsException(String.format("failed to enqueue %s - ActionQueue \"%s\" is stopped", description, this.name));
        }
    }

    @Override
    public void stop(int count, int sleepTime) {
        String timestamp = TimeUtils.getTimestampIsoInstant();
        logMessage(String.format("Stopping queue %s", this));
        stop = true;
        for (int i = 0; i < count; i++) {
            if (queue.isEmpty()) return;
            logMessage(String.format("Queue not yet empty, has %d elements. waiting...", queue.size()));
            MyThread.sleep(sleepTime);
        }
        isStoppedStatus = true;
        logMessage(String.format("Queue %s has %d elements left but we're done waiting.  Queue toString: %s", this, queue.size(), queue));
    }

    @Override
    public LinkedBlockingQueue<RunnableWithDescription> getQueue() {
        return new LinkedBlockingQueue<>(queue);
    }

    @Override
    public boolean isStopped() {
        return isStoppedStatus;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public Thread getQueueThread() {
        return queueThread;
    }

    // Template methods!
    protected abstract void runAction() throws InterruptedException;
    protected abstract void logMessage(String message);

}
