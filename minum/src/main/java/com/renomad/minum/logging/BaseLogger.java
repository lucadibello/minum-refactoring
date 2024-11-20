package com.renomad.minum.logging;

import com.renomad.minum.logging.model.ILogger;
import com.renomad.minum.logging.model.ThrowingSupplier;
import com.renomad.minum.queue.ActionQueue;
import com.renomad.minum.queue.model.IActionQueue;
import com.renomad.minum.utils.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;

import static com.renomad.minum.utils.TimeUtils.getTimestampIsoInstant;

public abstract class BaseLogger<T extends Enum<?>> implements ILogger<T> {

    /**
     * The {@link ActionQueue} that handles all
     * our messages thread-safely by taking
     * them off the top of a queue.
     */
    private final IActionQueue loggingActionQueue;
    private final ExecutorService executorService;
    private final String name;
    private final HashSet<T> activeLogLevels;

    /**
     * Constructor
     * @param loggingLevels a list of log levels to enable
     * @param executorService provides thread handling for the logs, used to
     *                        build a {@link ActionQueue}
     * @param name sets a name on the {@link ActionQueue} to aid debugging, to
     *             help distinguish queues.
     */
    public BaseLogger(List<T> loggingLevels, ExecutorService executorService, String name) {
        this(loggingLevels, executorService, name, null);
    }

    public BaseLogger(Collection<T> loggingLevels, ExecutorService executorService, String name, IActionQueue loggingActionQueue) {
        this.executorService = executorService;
        this.name = name;
        this.loggingActionQueue = Objects.requireNonNullElseGet(
                loggingActionQueue, () -> new ActionQueue(
                        "loggerPrinter" + name,
                        executorService
                ).initialize());
        this.activeLogLevels = new HashSet<>(loggingLevels);
    }

    public BaseLogger(BaseLogger<T> logger) {
        this(logger.getActiveLogLevels(), logger.getExecutorService(), logger.getName(), logger.getLoggingActionQueue());
    }

    /**
     * Logs helpful debugging information
     * @param msg a lambda for what is to be logged.  example: () -> "Hello"
     * @param level the level of the log
     */
    @Override
    public void log(ThrowingSupplier<String, Exception> msg, T level) {
        if (isEnabled(level)) {
            String receivedMessage;
            try {
                receivedMessage = msg.get();
            } catch (Exception ex) {
                receivedMessage = "EXCEPTION DURING GET: " + ex;
            }
            String finalReceivedMessage = receivedMessage;
            IActionQueue queue = getLoggingActionQueue();
            if (queue == null || queue.isStopped()) {
                Object[] args = new Object[]{getTimestampIsoInstant(), level.name(), StringUtils.showWhiteSpace(finalReceivedMessage)};
                System.out.printf("%s\t%s\t%s%n", args);
            } else {
                queue.enqueue("Logger#logHelper(" + receivedMessage + ")", () -> {
                    Object[] args = new Object[]{getTimestampIsoInstant(), level.name(), StringUtils.showWhiteSpace(finalReceivedMessage)};
                    System.out.printf("%s\t%s\t%s%n", args);
                });
            }
        }
    }

    @Override
    public void stop() {
        this.loggingActionQueue.stop();
        this.executorService.shutdownNow();
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * This method can be used to adjust the active log levels.
     * If the boolean value is true, that level of logging is enabled.
     */
    protected Collection<T> getActiveLogLevels() {
        return activeLogLevels;
    }

    /**
     * This method is used to get the {@link ActionQueue} that
     * is used to handle all the logging messages.
     */
    protected ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * This method is used to get the {@link ActionQueue} that
     * is used to handle all the logging messages.
     */
    protected IActionQueue getLoggingActionQueue() {
        return loggingActionQueue;
    }

    /**
     * This method is used to enable a specific log level
     *
     * @param level the log level to enable
     * @return true if the log level was enabled, false if it was already enabled
     */
    public boolean enableLogLevel(T level) {
        return activeLogLevels.add(level);
    }

    /**
     * This method is used to disable a specific log level
     * @param level the log level to disable
     * @return true if the log level was disabled, false if it was already disabled
     */
    public boolean disableLogLevel(T level) {
        return activeLogLevels.remove(level);
    }

    /**
     * This method is used to check if a specific log level is enabled
     * @param level the log level to check
     * @return true if the log level is enabled, false if it is disabled
     */
    public boolean isEnabled(T level) {
        return activeLogLevels.contains(level);
    }
}
