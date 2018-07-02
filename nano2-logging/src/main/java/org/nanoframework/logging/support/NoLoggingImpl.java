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
package org.nanoframework.logging.support;

import org.nanoframework.logging.Logger;
import org.nanoframework.logging.mxbean.AbstractAnalysisLogger;
import org.nanoframework.toolkit.message.MessageFactory;
import org.nanoframework.toolkit.message.ParameterizedMessageFactory;
import org.nanoframework.toolkit.time.DateFormat;
import org.nanoframework.toolkit.time.Pattern;

import lombok.NonNull;

/**
 * @author yanghe
 * @since 1.0
 */
public class NoLoggingImpl extends AbstractAnalysisLogger implements Logger {
    private static final String FQCN = NoLoggingImpl.class.getName();

    private static final String ERROR_LEVEL = "[ERROR]";

    private static final String WARN_LEVEL = "[WARN ]";

    private static final String DEBUG_LEVEL = "[DEBUG]";

    private static final String INFO_LEVEL = "[INFO ]";

    private static final String TRACE_LEVEL = "[TRACE]";

    private static final int LEVEL = 0;

    private static final int DATETIME = 1;

    private static final int CLASS_NAME = 2;

    private static final int METHOD_NAME = 3;

    private static final int FILE_NAME = 4;

    private static final int LINE_NUMBER = 5;

    private static final int MESSAGE_ARGS_INDEX = 6;

    private MessageFactory messageFactory = ParameterizedMessageFactory.INSTANCE;

    public NoLoggingImpl(@NonNull String loggerName) {
        setLoggerName(loggerName);
    }

    protected String message(String level, String message, Object... args) {
        var stacks = Thread.currentThread().getStackTrace();
        var line = stackLine(stacks);

        var newArgs = new Object[6 + args.length];
        newArgs[LEVEL] = level;
        newArgs[DATETIME] = DateFormat.format(System.currentTimeMillis(), Pattern.DATETIME);
        newArgs[CLASS_NAME] = stacks[line].getClassName();
        newArgs[METHOD_NAME] = stacks[line].getMethodName();
        newArgs[FILE_NAME] = stacks[line].getFileName();
        newArgs[LINE_NUMBER] = stacks[line].getLineNumber();
        for (var idx = 0; idx < args.length; idx++) {
            newArgs[MESSAGE_ARGS_INDEX + idx] = args[idx];
        }

        return messageFactory.newMessage("{} {} {}.{}({}:{}) >>> " + message, newArgs).getFormattedMessage();
    }

    protected int stackLine(StackTraceElement[] stacks) {
        var invokeIndex = 0;
        for (var idx = 0; idx < stacks.length; idx++) {
            if (FQCN.equals(stacks[idx].getClassName()) && (idx + 1 < stacks.length)
                    && !FQCN.equals(stacks[idx + 1].getClassName())) {
                invokeIndex = idx + 1;
                break;
            }
        }

        return invokeIndex;
    }

    protected void outputStack(Throwable cause) {
        if (cause != null) {
            outputStack(cause.getMessage(), cause);
        }
    }

    protected void outputStack(String message, Throwable cause) {
        outputStack(message, cause, false);
    }

    protected void outputStack(String message, Throwable cause, boolean caused) {
        if (cause != null) {
            var stacks = cause.getStackTrace();
            if (caused) {
                System.out.println("Caused by: " + cause.getClass().getName() + ": "
                        + (message == null ? cause.getMessage() : message));
                var stack = stacks[0];
                System.out.println("\tat " + stack.getClassName() + '.' + stack.getMethodName() + '('
                        + stack.getFileName() + ':' + stack.getLineNumber() + ')');
                System.out.println("\t... " + (stacks.length - 1) + " more");
            } else {
                System.out
                        .println(cause.getClass().getName() + ": " + (message == null ? cause.getMessage() : message));
                for (var stack : stacks) {
                    System.out.println("\tat " + stack.getClassName() + '.' + stack.getMethodName() + '('
                            + stack.getFileName() + ':' + stack.getLineNumber() + ')');
                }
            }

        }

        var parentCause = cause.getCause();
        if (parentCause != null) {
            outputStack(parentCause.getMessage(), parentCause, true);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    public void error(String message, Throwable cause) {
        error(message);
        outputStack(message, cause);
    }

    public void error(String message) {
        System.out.println(message(ERROR_LEVEL, message));
        incrementError();
    }

    @Override
    public void error(String message, Object... args) {
        System.out.println(message(ERROR_LEVEL, message, args));
        incrementError();
    }

    @Override
    public void error(Throwable cause) {
        error(cause.getMessage(), cause);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    public void warn(String message) {
        System.out.println(message(WARN_LEVEL, message));
        incrementWarn();
    }

    @Override
    public void warn(String message, Throwable cause) {
        warn(message);
        outputStack(message, cause);
    }

    @Override
    public void warn(String message, Object... args) {
        System.out.println(message(WARN_LEVEL, message, args));
        incrementWarn();
    }

    @Override
    public void warn(Throwable cause) {
        warn(cause.getMessage(), cause);
    }

    public boolean isDebugEnabled() {
        return true;
    }

    public void debug(String message) {
        System.out.println(message(DEBUG_LEVEL, message));
        incrementDebug();
    }

    public void debug(String message, Throwable cause) {
        debug(message);
        outputStack(message, cause);
    }

    @Override
    public void debug(String message, Object... args) {
        System.out.println(message(DEBUG_LEVEL, message, args));
        incrementDebug();
    }

    @Override
    public void debug(Throwable cause) {
        debug(cause.getMessage(), cause);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String message) {
        System.out.println(message(INFO_LEVEL, message));
        incrementInfo();
    }

    @Override
    public void info(String message, Object... args) {
        System.out.println(message(INFO_LEVEL, message, args));
        incrementInfo();
    }

    @Override
    public void info(Throwable cause) {
        info(cause.getMessage(), cause);
    }

    @Override
    public void info(String message, Throwable cause) {
        info(message);
        outputStack(message, cause);
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(String message) {
        System.out.println(message(TRACE_LEVEL, message));
        incrementTrace();
    }

    @Override
    public void trace(String message, Object... args) {
        System.out.println(message(TRACE_LEVEL, message, args));
        incrementTrace();
    }

    @Override
    public void trace(Throwable cause) {
        trace(cause.getMessage(), cause);
    }

    @Override
    public void trace(String message, Throwable cause) {
        trace(message);
        outputStack(message, cause);
    }

}
