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
package org.nanoframework.logging;

/**
 * 日志适配基础接口.
 * @author yanghe
 * @since 2.0.0
 */
public interface Logger {

    /**
     * 是否开启Error日志级别.
     * @return 如果日志级别在Error以上，则返回true
     */
    boolean isErrorEnabled();

    /**
     * @param message 异常消息
     * @param cause 异常
     */
    void error(String message, Throwable cause);

    /**
     * @param message 异常消息
     */
    void error(String message);

    /**
     * @param message 异常消息
     * @param args 消息参数列表
     */
    void error(String message, Object... args);

    /**
     * @param cause 异常
     */
    void error(Throwable cause);

    /**
     * 是否开启Warn日志级别.
     * @return 如果日志级别在Warn以上，则返回true
     */
    boolean isWarnEnabled();

    /**
     * @param message 警告消息
     */
    void warn(String message);

    /**
     * @param message 警告消息
     * @param cause 异常
     */
    void warn(String message, Throwable cause);

    /**
     * @param message 警告消息
     * @param args 消息参数列表
     */
    void warn(String message, Object... args);

    /**
     * @param cause 异常
     */
    void warn(Throwable cause);

    /**
     * 是否开启Info日志级别.
     * @return 如果日志级别在Info以上，则返回true
     */
    boolean isInfoEnabled();

    /**
     * @param message 提醒消息
     */
    void info(String message);

    /**
     * @param message 提醒消息
     * @param args 消息参数列表
     */
    void info(String message, Object... args);

    /**
     * @param cause 异常
     */
    void info(Throwable cause);

    /**
     * @param message 提醒消息
     * @param cause 异常
     */
    void info(String message, Throwable cause);

    /**
     * 是否开启Debug日志级别.
     * @return 如果日志级别在Debug以上，则返回true
     */
    boolean isDebugEnabled();

    /**
     * @param message 调试消息
     */
    void debug(String message);

    /**
     * @param message 调试消息
     * @param cause 异常
     */
    void debug(String message, Throwable cause);

    /**
     * @param message 调试消息
     * @param args 消息参数列表
     */
    void debug(String message, Object... args);

    /**
     * @param cause 异常
     */
    void debug(Throwable cause);

    /**
     * 是否开启Trace日志级别.
     * @return 如果日志级别在Trace以上，则返回true
     */
    boolean isTraceEnabled();

    /**
     * @param message 输出消息
     */
    void trace(String message);

    /**
     * @param message 输出消息
     * @param args 消息参数列表
     */
    void trace(String message, Object... args);

    /**
     * @param cause 异常
     */
    void trace(Throwable cause);

    /**
     * @param message 输出消息
     * @param cause 异常
     */
    void trace(String message, Throwable cause);

}
