/*
 * Copyright 2015-2018 the original author or authors.
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
package org.nanoframework.modules.logging;

import java.lang.reflect.Constructor;

import org.nanoframework.modules.logging.exception.LoggerException;

/**
 * 日志系统匹配工厂.
 * @author yanghe
 * @since 2.0.0
 */
public final class LoggerFactory {
    private static final String LOG4J2_LOGGER = "org.apache.logging.log4j.Logger";

    private static final String SLF4J_LOGGRE = "org.slf4j.Logger";

    private static final String JDK_LOGGER = "java.util.logging.Logger";

    private static final String LOG4J2_IMPL_CLASS_NAME = "org.nanoframework.modules.logging.support.Log4j2Impl";

    private static final String SLF4J_IMPL_CLASS_NAME = "org.nanoframework.modules.logging.support.SLF4JImpl";

    private static final String JDK14_LOGGING_IMPL_CLASS_NAME = "org.nanoframework.modules.logging.support.Jdk14LoggingImpl";

    private static final String NO_LOGGING_IMPL_CLASS_NAME = "org.nanoframework.modules.logging.support.NoLoggingImpl";

    private static Constructor<?> LOGGER_CONSTRUCTOR;

    static {
        // 优先选择log4j2,而非Apache Common Logging. 因为后者无法设置真实Log调用者的信息
        tryImplementation(LOG4J2_LOGGER, LOG4J2_IMPL_CLASS_NAME);
        tryImplementation(SLF4J_LOGGRE, SLF4J_IMPL_CLASS_NAME);
        tryImplementation(JDK_LOGGER, JDK14_LOGGING_IMPL_CLASS_NAME);
        tryNoLoggingImplementation();

    }

    private LoggerFactory() {

    }

    private static synchronized void tryImplementation(String testClassName, String implClassName) {
        if (LOGGER_CONSTRUCTOR != null) {
            return;
        }

        try {
            Resources.forName(testClassName);
            LOGGER_CONSTRUCTOR = Resources.forName(implClassName).getConstructor(new Class[] {String.class });

            var declareClass = LOGGER_CONSTRUCTOR.getDeclaringClass();
            if (!Logger.class.isAssignableFrom(declareClass)) {
                LOGGER_CONSTRUCTOR = null;
            }

            try {
                if (LOGGER_CONSTRUCTOR != null) {
                    LOGGER_CONSTRUCTOR.newInstance(LoggerFactory.class.getName());
                }
            } catch (Throwable e) {
                LOGGER_CONSTRUCTOR = null;
            }
        } catch (Throwable e) {
            // skip
        }
    }

    private static synchronized void tryNoLoggingImplementation() {
        if (LOGGER_CONSTRUCTOR == null) {
            try {
                LOGGER_CONSTRUCTOR = Resources.forName(NO_LOGGING_IMPL_CLASS_NAME).getConstructor(String.class);
            } catch (Throwable e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    /**
     * @param cls 绑定日志类
     * @return 日志实现
     */
    public static Logger getLogger(Class<?> cls) {
        return getLogger(cls.getName());
    }

    /**
     * @param name 绑定日志名称
     * @return 日志实现
     */
    public static Logger getLogger(String name) {
        try {
            return (Logger) LOGGER_CONSTRUCTOR.newInstance(name);
        } catch (Throwable cause) {
            String message = String.format("获取日志实现异常: %s. Cause: %s", name, cause.getMessage());
            throw new LoggerException(message, cause);
        }
    }

    protected static synchronized void selectJdk14Logging() {
        try {
            Resources.forName(JDK_LOGGER);
            LOGGER_CONSTRUCTOR = Resources.forName(JDK14_LOGGING_IMPL_CLASS_NAME)
                    .getConstructor(new Class[] {String.class });
        } catch (Throwable e) {
            // ignore
        }
    }

    protected static synchronized void selectLog4j2Logging() {
        try {
            Resources.forName(LOG4J2_LOGGER);
            LOGGER_CONSTRUCTOR = Resources.forName(LOG4J2_IMPL_CLASS_NAME).getConstructor(new Class[] {String.class });
        } catch (Throwable e) {
            // ignore
        }
    }

    protected static synchronized void selectNoLogging() {
        try {
            LOGGER_CONSTRUCTOR = Resources.forName(NO_LOGGING_IMPL_CLASS_NAME)
                    .getConstructor(new Class[] {String.class });
        } catch (final Throwable e) {
            // ignore
        }
    }

}
