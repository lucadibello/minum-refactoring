package com.renomad.minum.exception;

import com.renomad.minum.utils.Invariants;

/**
 * An exception specific to our invariants.  See {@link Invariants}
 */
@SuppressWarnings("serial")
public final class InvariantException extends RuntimeException {
    public InvariantException(String msg) {
        super(msg);
    }
}
