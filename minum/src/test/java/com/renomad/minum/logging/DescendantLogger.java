package com.renomad.minum.logging;

import com.renomad.minum.logging.model.ThrowingSupplier;
import com.renomad.minum.utils.MyThread;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is an example of a custom logger, exercised in the tests.
 * This should give a good basis for your own custom logger if needed.
 */
public class DescendantLogger extends BaseLogger<CustomLoggingLevel> {

    public static final int MAX_CACHE_SIZE = 30;
    private final Queue<String> recentLogLines;
    private final ReentrantLock loggingLock;

    public DescendantLogger(BaseLogger<CustomLoggingLevel> logger) {
        super(logger);
        this.recentLogLines = new TestLoggerQueue(MAX_CACHE_SIZE);
        this.enableLogLevel(CustomLoggingLevel.REQUEST);
        this.loggingLock = new ReentrantLock();
    }

    // NOTE: Custom log method!
    public void logRequests(ThrowingSupplier<String, Exception> msg) {
        loggingLock.lock();
        try {
            addToCache(msg);
            super.log(msg, CustomLoggingLevel.REQUEST);
        } finally {
            loggingLock.unlock();
        }
    }

    /**
     * Keeps a record of the recently-added log messages, which is
     * useful for some tests.
     */
    private void addToCache(ThrowingSupplier<String, Exception> msg) {
        // put log messages into the tail of the queue
        String message = extractMessage(msg);
        String safeMessage = message == null ? "(null message)" : message;
        recentLogLines.add(safeMessage);
    }

    /**
     * A helper to get the string message value out of a
     * {@link ThrowingSupplier}
     */
    private String extractMessage(ThrowingSupplier<String, Exception> msg) {
        String receivedMessage;
        try {
            receivedMessage = msg.get();
        } catch (Exception ex) {
            receivedMessage = "EXCEPTION DURING GET: " + ex;
        }
        return receivedMessage;
    }

    /**
     * Given a string that may have whitespace chars, render it in a way we can see
     */
    static String showWhiteSpace(String msg) {
        if (msg == null) return "(NULL)";
        if (msg.isEmpty()) return "(EMPTY)";

        // if we have tabs, returns, newlines in the text, show them
        String text = msg
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n");

        if (text.isBlank()) return "(BLANK)";
        return text;
    }
    /**
     * Whether the given string exists in the log messages. May
     * exist multiple times.
     * @param value a string to search in the log
     * @param lines how many lines back to examine
     * @return whether this string was found, even if there
     *      were multiple places it was found.
     */
    public boolean doesMessageExist(String value, int lines) {
        if (! findMessage(value, lines, recentLogLines).isEmpty()) {
            return true;
        } else {
            List<String> logsBeingSearched = logLinesToSearch(lines, recentLogLines);
            throw new TestLoggerException(value + " was not found in \n\t" + String.join("\n\t", logsBeingSearched));
        }
    }

    /**
     * Whether the given string exists in the log messages. May
     * exist multiple times.
     * @param value a string to search in the log
     * @return whether or not this string was found, even if there
     * were multiple places it was found.
     */
    public boolean doesMessageExist(String value) {
        return doesMessageExist(value, 3);
    }


    static List<String> findMessage(String value, int lines, Queue<String> recentLogLines) {
        if (lines > MAX_CACHE_SIZE) {
            throw new TestLoggerException(String.format("Can only get up to %s lines from the log", MAX_CACHE_SIZE));
        }
        if (lines <= 0) {
            throw new TestLoggerException("number of recent log lines must be a positive number");
        }
        MyThread.sleep(20);
        var lineList = logLinesToSearch(lines, recentLogLines);
        return lineList.stream().filter(x -> x.toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT))).toList();
    }


    private static List<String> logLinesToSearch(int lines, Queue<String> recentLogLines) {
        var fromIndex = Math.max(recentLogLines.size() - lines, 0);
        return recentLogLines.stream().toList().subList(fromIndex, recentLogLines.size());
    }
}
