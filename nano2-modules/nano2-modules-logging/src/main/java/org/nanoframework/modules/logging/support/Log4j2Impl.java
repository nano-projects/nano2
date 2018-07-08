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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.nanoframework.modules.logging.mxbean.AbstractAnalysisLogger;

import lombok.NonNull;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class Log4j2Impl extends AbstractAnalysisLogger implements org.nanoframework.modules.logging.Logger {
    private static String FQCN = Log4j2Impl.class.getName();

    private ExtendedLogger logger;

    /**
     * @param logger 日志实现
     */
    public Log4j2Impl(@NonNull Logger logger) {
        this.logger = (ExtendedLogger) logger;
        setLoggerName(logger.getName());
    }

    /**
     * @param loggerName 日志名称
     */
    public Log4j2Impl(@NonNull String loggerName) {
        logger = (ExtendedLogger) LogManager.getLogger(loggerName);
        setLoggerName(loggerName);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabled(Level.ERROR);
    }

    @Override
    public void error(String message, Throwable cause) {
        logger.logIfEnabled(FQCN, Level.ERROR, null, message, cause);
        incrementError();
    }

    @Override
    public void error(String message) {
        logger.logIfEnabled(FQCN, Level.ERROR, null, message);
        incrementError();
    }

    @Override
    public void error(String message, Object... args) {
        logger.logIfEnabled(FQCN, Level.ERROR, null, message, args);
        incrementError();
    }

    @Override
    public void error(Throwable cause) {
        logger.logIfEnabled(FQCN, Level.ERROR, null, cause.getMessage(), cause);
        incrementError();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabled(Level.WARN);
    }

    @Override
    public void warn(String message) {
        logger.logIfEnabled(FQCN, Level.WARN, null, message);
        incrementWarn();
    }

    @Override
    public void warn(String message, Throwable cause) {
        logger.logIfEnabled(FQCN, Level.WARN, null, message, cause);
        incrementWarn();
    }

    @Override
    public void warn(String message, Object... args) {
        logger.logIfEnabled(FQCN, Level.WARN, null, message, args);
        incrementWarn();
    }

    @Override
    public void warn(Throwable cause) {
        logger.logIfEnabled(FQCN, Level.WARN, null, cause.getMessage(), cause);
        incrementWarn();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isEnabled(Level.DEBUG);
    }

    @Override
    public void debug(String message) {
        logger.logIfEnabled(FQCN, Level.DEBUG, null, message);
        incrementDebug();
    }

    @Override
    public void debug(String message, Throwable cause) {
        logger.logIfEnabled(FQCN, Level.DEBUG, null, message, cause);
        incrementDebug();
    }

    @Override
    public void debug(String message, Object... args) {
        logger.logIfEnabled(FQCN, Level.DEBUG, null, message, args);
        incrementDebug();
    }

    @Override
    public void debug(Throwable cause) {
        logger.logIfEnabled(FQCN, Level.DEBUG, null, cause.getMessage(), cause);
        incrementDebug();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isEnabled(Level.INFO);
    }

    @Override
    public void info(String message) {
        logger.logIfEnabled(FQCN, Level.INFO, null, message);
        incrementInfo();
    }

    @Override
    public void info(String message, Object... args) {
        logger.logIfEnabled(FQCN, Level.INFO, null, message, args);
        incrementInfo();
    }

    @Override
    public void info(Throwable cause) {
        logger.logIfEnabled(FQCN, Level.INFO, null, cause.getMessage(), cause);
        incrementInfo();
    }

    @Override
    public void info(String message, Throwable cause) {
        logger.logIfEnabled(FQCN, Level.INFO, null, message, cause);
        incrementInfo();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isEnabled(Level.TRACE);
    }

    @Override
    public void trace(String message) {
        logger.logIfEnabled(FQCN, Level.TRACE, null, message);
        incrementTrace();
    }

    @Override
    public void trace(String message, Object... args) {
        logger.logIfEnabled(FQCN, Level.TRACE, null, message, args);
        incrementTrace();
    }

    @Override
    public void trace(Throwable cause) {
        logger.logIfEnabled(FQCN, Level.TRACE, null, cause.getMessage(), cause);
        incrementTrace();
    }

    @Override
    public void trace(String message, Throwable cause) {
        logger.logIfEnabled(FQCN, Level.TRACE, null, message, cause);
        incrementTrace();
    }

    @Override
    public String toString() {
        return logger.toString();
    }
}
