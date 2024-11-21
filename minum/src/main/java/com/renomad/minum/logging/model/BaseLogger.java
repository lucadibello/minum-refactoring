package com.renomad.minum.logging.model;

import com.renomad.minum.logging.ThrowingSupplier;
import com.renomad.minum.queue.ActionQueue;
import com.renomad.minum.queue.model.IActionQueue;
import com.renomad.minum.state.Constants;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import static com.renomad.minum.utils.StringUtils.showWhiteSpace;
import static com.renomad.minum.utils.TimeUtils.getTimestampIsoInstant;

public class BaseLogger implements ILogger<ILoggingLevel> {

    /**
     * The {@link ActionQueue} that handles all
     * our messages thread-safely by taking
     * them off the top of a queue.
     */
    private final IActionQueue loggingActionQueue;
    private final ExecutorService executorService;
    private final String name;
    private final HashSet<ILoggingLevel> activeLogLevels;

    public BaseLogger(Constants constants, ExecutorService executorService, String name, IActionQueue loggingActionQueue) {
        this(constants.logLevels, executorService, name, null);
    }

    public BaseLogger(Collection<ILoggingLevel> logLevels, ExecutorService executorService, String name, IActionQueue loggingActionQueue) {
        this.executorService = executorService;
        this.name = name;
        this.loggingActionQueue = loggingActionQueue;
        this.activeLogLevels = new HashSet<>(logLevels);
    }

    // Copy constructor
    public BaseLogger(BaseLogger logger) {
        this(logger.getActiveLogLevels(), logger.getExecutorService(), logger.getName(), logger.getLoggingActionQueue());
    }

    @Override
    public void stop() {
        this.loggingActionQueue.stop();
        this.executorService.shutdownNow();
    }

    @Override
    public boolean enableLogLevel(ILoggingLevel level) {
        return activeLogLevels.add(level);
    }

    @Override
    public boolean disableLogLevel(ILoggingLevel level) {
        return activeLogLevels.remove(level);
    }

    @Override
    public boolean isEnabled(ILoggingLevel level) {
        return  activeLogLevels.contains(level);
    }

    public Collection<ILoggingLevel> getActiveLogLevels() {
        return activeLogLevels;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public IActionQueue getLoggingActionQueue() {
        return loggingActionQueue;
    }

    @Override
    public void log(ThrowingSupplier<String, Exception> msg, ILoggingLevel loggingLevel) {
        if(isEnabled(loggingLevel)) {
            String receivedMessage;
            try {
                receivedMessage = msg.get();
            } catch (Exception ex) {
                receivedMessage = "EXCEPTION DURING GET: " + ex;
            }
            String finalReceivedMessage = receivedMessage;
            if (loggingActionQueue == null || loggingActionQueue.isClosed()) {
                Object[] args = new Object[]{getTimestampIsoInstant(), loggingLevel.name(), showWhiteSpace(finalReceivedMessage)};
                System.out.printf("%s\t%s\t%s%n", args);
            } else {
                loggingActionQueue.enqueue("Logger#logHelper(" + receivedMessage + ")", () -> {
                    Object[] args = new Object[]{getTimestampIsoInstant(), loggingLevel.name(), showWhiteSpace(finalReceivedMessage)};
                    System.out.printf("%s\t%s\t%s%n", args);
                });
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
