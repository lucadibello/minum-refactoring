package com.renomad.minum.logging;

import com.renomad.minum.logging.model.BaseLogger;
import com.renomad.minum.logging.model.ILoggingLevel;

import java.util.Collection;

public class DefaultLogger extends BaseLogger {
    public DefaultLogger(Collection<ILoggingLevel> logLevels, String name) {
        super(logLevels, null, name, null);
    }
}
