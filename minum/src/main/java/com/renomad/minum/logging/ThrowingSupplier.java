package com.renomad.minum.logging;

import com.renomad.minum.logging.model.ILogger;

/**
 * a functional interface used in {@link ILogger}, allows exceptions
 * to bubble up.
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception>{

    T get() throws E;

}