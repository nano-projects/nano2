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

    static {
        var conf = new ExecutorConfig();
        conf.name = "tomcatThreadPool";
        conf.daemon = Boolean.TRUE;
        conf.namePrefix = "tomcat-exec-";
        conf.maxThreads = 200;
        conf.minSpareThreads = 25;
        conf.maxIdleTime = 60_000;
        conf.maxQueueSize = Integer.MAX_VALUE;
        conf.prestartminSpareThreads = Boolean.FALSE;
        conf.threadRenewalDelay = 1000L;
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
