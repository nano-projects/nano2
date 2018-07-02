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
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.AtomicLongMap;

/**
 * @author yanghe
 * @since 2.0.0
 */
public abstract class AbstractAnalysisLogger implements AnalysisLoggerMXBean {
    private static final AtomicLongMap<String> ERROR = AtomicLongMap.create();

    private static final AtomicLongMap<String> WARN = AtomicLongMap.create();

    private static final AtomicLongMap<String> INFO = AtomicLongMap.create();

    private static final AtomicLongMap<String> DEBUG = AtomicLongMap.create();

    private static final AtomicLongMap<String> TRACE = AtomicLongMap.create();

    private static final String DEFAULT_LOGGER_NAME = "nil";

    private String loggerName = DEFAULT_LOGGER_NAME;

    @Override
    public Set<String> getErrorKeys() {
        return ERROR.asMap().keySet();
    }

    @Override
    public Set<String> getWarnKeys() {
        return WARN.asMap().keySet();
    }

    @Override
    public Set<String> getInfoKeys() {
        return INFO.asMap().keySet();
    }

    @Override
    public Set<String> getDebugKeys() {
        return DEBUG.asMap().keySet();
    }

    @Override
    public Set<String> getTraceKeys() {
        return TRACE.asMap().keySet();
    }

    @Override
    public long getErrorCount() {
        return ERROR.get(loggerName);
    }

    @Override
    public long getWarnCount() {
        return WARN.get(loggerName);
    }

    @Override
    public long getInfoCount() {
        return INFO.get(loggerName);
    }

    @Override
    public long getDebugCount() {
        return DEBUG.get(loggerName);
    }

    @Override
    public long getTraceCount() {
        return TRACE.get(loggerName);
    }

    @Override
    public void reset() {
        ERROR.put(loggerName, 0);
        WARN.put(loggerName, 0);
        INFO.put(loggerName, 0);
        DEBUG.put(loggerName, 0);
        TRACE.put(loggerName, 0);
    }

    @Override
    public long getErrorTotal() {
        return total(ERROR);
    }

    @Override
    public long getWarnTotal() {
        return total(WARN);
    }

    @Override
    public long getInfoTotal() {
        return total(INFO);
    }

    @Override
    public long getDebugTotal() {
        return total(DEBUG);
    }

    @Override
    public long getTraceTotal() {
        return total(TRACE);
    }

    @Override
    public void resetAll() {
        ERROR.clear();
        WARN.clear();
        INFO.clear();
        DEBUG.clear();
        TRACE.clear();
    }

    /**
     * @param analysis 统计集合
     * @return 日志总数
     */
    protected long total(AtomicLongMap<String> analysis) {
        if (analysis == null || analysis.isEmpty()) {
            return 0;
        }

        var total = new AtomicLong();
        analysis.asMap().values().forEach(total::addAndGet);
        return total.get();
    }

    /**
     * Error日志计数.
     */
    public void incrementError() {
        ERROR.incrementAndGet(loggerName);
    }

    /**
     * Warn日志计数.
     */
    public void incrementWarn() {
        WARN.incrementAndGet(loggerName);
    }

    /**
     * Info日志计数.
     */
    public void incrementInfo() {
        INFO.incrementAndGet(loggerName);
    }

    /**
     * Debug日志计数.
     */
    public void incrementDebug() {
        DEBUG.incrementAndGet(loggerName);
    }

    /**
     * Trace日志计数.
     */
    public void incrementTrace() {
        TRACE.incrementAndGet(loggerName);
    }

    public void setLoggerName(final String loggerName) {
        this.loggerName = loggerName;
    }

    public String getLoggerName() {
        return loggerName;
    }
}
