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
package org.nanoframework.logging.mxbean;

import java.util.Set;

/**
 * @author yanghe
 * @since 2.0.0
 */
public interface AnalysisLoggerMXBean {
    /**
     * JMX监控的对象名称.
     */
    String OBJECT_NAME = "org.nanoframework:type=AnalysisLogger";

    /**
     * @return 获取异常信息集合
     */
    Set<String> getErrorKeys();

    /**
     * @return 获取警告信息集合
     */
    Set<String> getWarnKeys();

    /**
     * @return 获取提醒信息集合
     */
    Set<String> getInfoKeys();

    /**
     * @return 获取调试信息集合
     */
    Set<String> getDebugKeys();

    /**
     * @return 获取输出信息集合
     */
    Set<String> getTraceKeys();

    /**
     * @return 异常日志数量
     */
    long getErrorCount();

    /**
     * @return 警告日志数量
     */
    long getWarnCount();

    /**
     * @return 提醒日志数量
     */
    long getInfoCount();

    /**
     * @return 调试日志数量
     */
    long getDebugCount();

    /**
     * @return 输出日志数量
     */
    long getTraceCount();

    /**
     * 重置日志监控统计信息.
     */
    void reset();

    /**
     * @return 异常日志总数
     */
    long getErrorTotal();

    /**
     * @return 警告日志总数
     */
    long getWarnTotal();

    /**
     * @return 提醒日志总数
     */
    long getInfoTotal();

    /**
     * @return 调试日志总数
     */
    long getDebugTotal();

    /**
     * @return 输出日志总数
     */
    long getTraceTotal();

    /**
     * 重置所有监控统计信息.
     */
    void resetAll();

}
