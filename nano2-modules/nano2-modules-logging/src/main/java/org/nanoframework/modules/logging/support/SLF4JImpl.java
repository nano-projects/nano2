/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.modules.logging.support;

import org.nanoframework.modules.logging.mxbean.AbstractAnalysisLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import lombok.NonNull;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class SLF4JImpl extends AbstractAnalysisLogger implements org.nanoframework.modules.logging.Logger {

    private static String FQNC = SLF4JImpl.class.getName();

    private static Logger TEST_LOGGER = LoggerFactory.getLogger(SLF4JImpl.class);

    static {
        if (!(TEST_LOGGER instanceof LocationAwareLogger)) {
            throw new UnsupportedOperationException(TEST_LOGGER.getClass() + " is not a suitable logger");
        }
    }

    private LocationAwareLogger logger;

    /**
     * @param logger 日志实现
     */
    public SLF4JImpl(@NonNull LocationAwareLogger logger) {
        this.logger = logger;
        setLoggerName(logger.getName());
    }

    /**
     * @param loggerName 日志名称
     */
    public SLF4JImpl(@NonNull String loggerName) {
        this.logger = (LocationAwareLogger) LoggerFactory.getLogger(loggerName);
        setLoggerName(loggerName);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String message, Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.ERROR_INT, message, null, cause);
        incrementError();
    }

    @Override
    public void error(String message) {
        logger.log(null, FQNC, LocationAwareLogger.ERROR_INT, message, null, null);
        incrementError();
    }

    @Override
    public void error(String message, Object... args) {
        logger.log(null, FQNC, LocationAwareLogger.ERROR_INT, message, args, null);
        incrementError();
    }

    @Override
    public void error(Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.ERROR_INT, cause.getMessage(), null, cause);
        incrementError();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String message) {
        logger.log(null, FQNC, LocationAwareLogger.WARN_INT, message, null, null);
        incrementWarn();
    }

    @Override
    public void warn(String message, Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.WARN_INT, message, null, cause);
        incrementWarn();
    }

    @Override
    public void warn(String message, Object... args) {
        logger.log(null, FQNC, LocationAwareLogger.WARN_INT, message, args, null);
        incrementWarn();
    }

    @Override
    public void warn(Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.WARN_INT, cause.getMessage(), null, cause);
        incrementWarn();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String message) {
        logger.log(null, FQNC, LocationAwareLogger.INFO_INT, message, null, null);
        incrementInfo();
    }

    @Override
    public void info(String message, Object... args) {
        logger.log(null, FQNC, LocationAwareLogger.INFO_INT, message, args, null);
        incrementInfo();
    }

    @Override
    public void info(Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.INFO_INT, cause.getMessage(), null, cause);
        incrementInfo();
    }

    @Override
    public void info(String message, Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.INFO_INT, message, null, cause);
        incrementInfo();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String message) {
        logger.log(null, FQNC, LocationAwareLogger.DEBUG_INT, message, null, null);
        incrementDebug();
    }

    @Override
    public void debug(String message, Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.ERROR_INT, message, null, cause);
        incrementDebug();
    }

    @Override
    public void debug(String message, Object... args) {
        logger.log(null, FQNC, LocationAwareLogger.DEBUG_INT, message, args, null);
        incrementDebug();
    }

    @Override
    public void debug(Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.DEBUG_INT, cause.getMessage(), null, cause);
        incrementDebug();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String message) {
        logger.log(null, FQNC, LocationAwareLogger.TRACE_INT, message, null, null);
        incrementWarn();
    }

    @Override
    public void trace(String message, Object... args) {
        logger.log(null, FQNC, LocationAwareLogger.TRACE_INT, message, args, null);
        incrementWarn();
    }

    @Override
    public void trace(Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.TRACE_INT, cause.getMessage(), null, cause);
        incrementWarn();
    }

    @Override
    public void trace(String message, Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.TRACE_INT, message, null, cause);
        incrementWarn();
    }

}
