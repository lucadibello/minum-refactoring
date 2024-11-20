package com.renomad.minum.logging.model;

/**
 * Logging code interface
 */
public interface ILogger<T extends Enum<?>> {
    /**
     * When we are shutting down the system it is necessary to
     * explicitly stop the logger.
     *
     * <p>
     * The logger has to stand apart from the rest of the system,
     * or else we'll have circular dependencies.
     * </p>
     */
    void stop();

    boolean enableLogLevel(T level);

    boolean disableLogLevel(T level);

    boolean isEnabled(T level);

    void log(ThrowingSupplier<String, Exception> msg, T level);

    /**
     * Get the name of the logger
     */
    String getName();
}
