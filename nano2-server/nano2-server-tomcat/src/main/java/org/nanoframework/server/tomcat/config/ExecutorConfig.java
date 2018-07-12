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
package org.nanoframework.server.tomcat.config;

import org.apache.catalina.core.StandardThreadExecutor;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Getter
@Setter
public class ExecutorConfig extends AbstractConfig {
    public static final String TOMCAT_EXECUTOR = "context.tomcat.executor";

    private static final long serialVersionUID = 4483458534269995105L;

    private static final String DEFAULT_NAME = "tomcatThreadPool";

    private static final Boolean DEFAULT_DAEMON = Boolean.TRUE;

    private static final String DEFAULT_NAME_PREFIX = "tomcat-exec-";

    private static final Integer DEFAULT_MAX_THREADS = 200;

    private static final Integer DEFAULT_MIN_SPARE_THREADS = 25;

    private static final Integer DEFAULT_MAX_IDLE_TIME = 60_000;

    private static final Integer DEFAULT_MAX_QUEUE_SIZE = Integer.MAX_VALUE;

    private static final Boolean DEFAULT_PRESTARTMIN_SPARE_THREADS = Boolean.FALSE;

    private static final Long DEFAULT_THREAD_RENEWAL_DELAY = 1000L;

    static {
        var conf = new ExecutorConfig();
        conf.name = DEFAULT_NAME;
        conf.daemon = DEFAULT_DAEMON;
        conf.namePrefix = DEFAULT_NAME_PREFIX;
        conf.maxThreads = DEFAULT_MAX_THREADS;
        conf.minSpareThreads = DEFAULT_MIN_SPARE_THREADS;
        conf.maxIdleTime = DEFAULT_MAX_IDLE_TIME;
        conf.maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
        conf.prestartminSpareThreads = DEFAULT_PRESTARTMIN_SPARE_THREADS;
        conf.threadRenewalDelay = DEFAULT_THREAD_RENEWAL_DELAY;
        DEF = conf;
    }

    private String name;

    private Boolean daemon;

    private String namePrefix;

    private Integer maxThreads;

    private Integer minSpareThreads;

    private Integer maxIdleTime;

    private Integer maxQueueSize;

    private Boolean prestartminSpareThreads;

    private Long threadRenewalDelay;

    private ExecutorConfig() {

    }

    public ExecutorConfig(final ExecutorConfig conf) {
        this.merge(conf);
    }

    public StandardThreadExecutor init() {
        var executor = new StandardThreadExecutor();
        executor.setName(name);
        executor.setDaemon(daemon);
        executor.setNamePrefix(namePrefix);
        executor.setMaxThreads(maxThreads);
        executor.setMinSpareThreads(minSpareThreads);
        executor.setMaxIdleTime(maxIdleTime);
        executor.setMaxQueueSize(maxQueueSize);
        executor.setPrestartminSpareThreads(prestartminSpareThreads);
        executor.setThreadRenewalDelay(threadRenewalDelay);
        return executor;
    }

    @Override
    public String key() {
        return TOMCAT_EXECUTOR;
    }
}
