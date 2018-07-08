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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanoframework.modules.logging.mxbean.AbstractAnalysisLogger;

import lombok.NonNull;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class Jdk14LoggingImpl extends AbstractAnalysisLogger implements org.nanoframework.modules.logging.Logger {
    private Logger logger;

    /**
     * @param loggerName 日志名称
     */
    public Jdk14LoggingImpl(@NonNull String loggerName) {
        logger = Logger.getLogger(loggerName);
        setLoggerName(loggerName);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public void error(String message, Throwable cause) {
        logger.logp(Level.SEVERE, getLoggerName(), methodName(), message, cause);
        incrementError();
    }

    @Override
    public void error(String message) {
        logger.logp(Level.SEVERE, getLoggerName(), methodName(), message);
        incrementError();
    }

    @Override
    public void error(String message, Object... args) {
        logger.logp(Level.SEVERE, getLoggerName(), methodName(), message, args);
        incrementError();
    }

    @Override
    public void error(Throwable cause) {
        logger.logp(Level.SEVERE, getLoggerName(), methodName(), cause.getMessage(), cause);
        incrementError();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public void warn(String message, Object... args) {
        logger.logp(Level.WARNING, getLoggerName(), methodName(), message, args);
        incrementWarn();
    }

    @Override
    public void warn(Throwable cause) {
        logger.logp(Level.WARNING, getLoggerName(), methodName(), cause.getMessage(), cause);
        incrementWarn();
    }

    @Override
    public void warn(String message) {
        logger.logp(Level.WARNING, getLoggerName(), methodName(), message);
        incrementWarn();
    }

    @Override
    public void warn(String message, Throwable cause) {
        logger.logp(Level.WARNING, getLoggerName(), methodName(), message, cause);
        incrementWarn();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public void info(String message) {
        logger.logp(Level.INFO, getLoggerName(), methodName(), message);
        incrementInfo();
    }

    @Override
    public void info(String message, Object... args) {
        logger.logp(Level.INFO, getLoggerName(), methodName(), message, args);
        incrementInfo();
    }

    @Override
    public void info(Throwable cause) {
        logger.logp(Level.INFO, getLoggerName(), methodName(), cause.getMessage(), cause);
        incrementInfo();
    }

    @Override
    public void info(String message, Throwable cause) {
        logger.logp(Level.INFO, getLoggerName(), methodName(), message, cause);
        incrementInfo();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    @Override
    public void debug(String message) {
        logger.logp(Level.FINE, getLoggerName(), methodName(), message);
        incrementDebug();
    }

    @Override
    public void debug(String message, Throwable cause) {
        logger.logp(Level.FINE, getLoggerName(), methodName(), message, cause);
        incrementDebug();
    }

    @Override
    public void debug(String message, Object... args) {
        logger.logp(Level.FINE, getLoggerName(), methodName(), message, args);
        incrementDebug();
    }

    @Override
    public void debug(Throwable cause) {
        logger.logp(Level.FINE, getLoggerName(), methodName(), cause.getMessage(), cause);
        incrementDebug();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.ALL);
    }

    @Override
    public void trace(String message) {
        logger.logp(Level.ALL, getLoggerName(), methodName(), message);
        incrementTrace();
    }

    @Override
    public void trace(String message, Object... args) {
        logger.logp(Level.ALL, getLoggerName(), methodName(), message, args);
        incrementTrace();
    }

    @Override
    public void trace(Throwable cause) {
        logger.logp(Level.ALL, getLoggerName(), methodName(), cause.getMessage(), cause);
        incrementTrace();
    }

    @Override
    public void trace(String message, Throwable cause) {
        logger.logp(Level.ALL, getLoggerName(), methodName(), message, cause);
        incrementTrace();
    }

    private String methodName() {
        return Thread.currentThread().getStackTrace()[1].getMethodName();
    }
}
