package com.renomad.minum.logging.model;

/**
 * a functional interface used in {@link ICanonicalLogger}, allows exceptions
 * to bubble up.
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception>{

    T get() throws E;

}