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
package org.nanoframework.orm.jdbc.pool;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.orm.jdbc.binding.GlobalJdbcManager;
import org.nanoframework.orm.jdbc.binding.JdbcManager;
import org.nanoframework.orm.jdbc.config.JdbcConfig;
import org.nanoframework.orm.jdbc.config.TomcatJdbcConfig;
import org.nanoframework.toolkit.lang.CollectionUtils;

import com.google.common.collect.Maps;

/**
 * @author yanghe
 * @since 1.3.6
 */
public class TomcatJdbcPool implements Pool {
    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatJdbcPool.class);

    private final ConcurrentMap<String, DataSource> dataSources = Maps.newConcurrentMap();

    private Class<?> cls;
    {
        try {
            cls = Class.forName("org.apache.tomcat.jdbc.pool.DataSource");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Unload class [ org.apache.tomcat.jdbc.pool.DataSource ]");
        }
    }

    public TomcatJdbcPool(Collection<JdbcConfig> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            throw new IllegalArgumentException("无效的JDBC配置");
        }

        if (CollectionUtils.isNotEmpty(dataSources)) {
            closeAndClear();
        }

        configs.stream().map(config -> (TomcatJdbcConfig) config).forEach(config -> {
            DataSource dataSource;
            try {
                dataSource = (DataSource) cls.getConstructor().newInstance();
            } catch (final Throwable e) {
                throw new RuntimeException("Instance Constructor Exception: " + cls.getName());
            }

            try {
                cls.getMethod("setDriverClassName", String.class).invoke(dataSource, config.getDriver());
                cls.getMethod("setUrl", String.class).invoke(dataSource, config.getUrl());
                cls.getMethod("setUsername", String.class).invoke(dataSource, config.getUserName());
                cls.getMethod("setPassword", String.class).invoke(dataSource, config.getPasswd());

                var initialSize = config.getInitialSize();
                if (initialSize != null) {
                    cls.getMethod("setInitialSize", int.class).invoke(dataSource, initialSize);
                }

                var minIdle = config.getMinIdle();
                if (minIdle != null) {
                    cls.getMethod("setMinIdle", int.class).invoke(dataSource, minIdle);
                }

                var maxWait = config.getMaxWait();
                if (maxWait != null) {
                    cls.getMethod("setMaxWait", int.class).invoke(dataSource, maxWait);
                }

                var maxActive = config.getMaxActive();
                if (maxActive != null) {
                    cls.getMethod("setMaxActive", int.class).invoke(dataSource, maxActive);
                }

                var testWhileIdle = config.getTestWhileIdle();
                if (testWhileIdle != null) {
                    cls.getMethod("setTestWhileIdle", boolean.class).invoke(dataSource, testWhileIdle);
                }

                var testOnBorrow = config.getTestOnBorrow();
                if (testOnBorrow != null) {
                    cls.getMethod("setTestOnBorrow", boolean.class).invoke(dataSource, testOnBorrow);
                }

                var validtionInterval = config.getValidationInterval();
                if (validtionInterval != null) {
                    cls.getMethod("setValidationInterval", long.class).invoke(dataSource, validtionInterval);
                }

                var timeBetweenEvictionRunsMillis = config.getTimeBetweenEvictionRunsMillis();
                if (timeBetweenEvictionRunsMillis != null) {
                    cls.getMethod("setTimeBetweenEvictionRunsMillis", int.class).invoke(dataSource,
                            timeBetweenEvictionRunsMillis);
                }

                var logAbandoned = config.getLogAbandoned();
                if (logAbandoned != null) {
                    cls.getMethod("setLogAbandoned", boolean.class).invoke(dataSource, logAbandoned);
                }

                var removeAbandoned = config.getRemoveAbandoned();
                if (removeAbandoned != null) {
                    cls.getMethod("setRemoveAbandoned", boolean.class).invoke(dataSource, removeAbandoned);
                }

                var removeAbandonedTimeout = config.getRemoveAbandonedTimeout();
                if (removeAbandonedTimeout != null) {
                    cls.getMethod("setRemoveAbandonedTimeout", int.class).invoke(dataSource, removeAbandonedTimeout);
                }

                var minEvictableIdleTimeMillis = config.getMinEvictableIdleTimeMillis();
                if (minEvictableIdleTimeMillis != null) {
                    cls.getMethod("setMinEvictableIdleTimeMillis", int.class).invoke(dataSource,
                            minEvictableIdleTimeMillis);
                }

                var jdbcInterceptors = config.getJdbcInterceptors();
                if (jdbcInterceptors != null) {
                    cls.getMethod("setJdbcInterceptors", String.class).invoke(dataSource, jdbcInterceptors);
                }

                var jmxEnabled = config.getJmxEnabled();
                if (jmxEnabled != null) {
                    cls.getMethod("setJmxEnabled", boolean.class).invoke(dataSource, jmxEnabled);
                }
            } catch (final Throwable e) {
                throw new IllegalArgumentException("设置参数异常: " + e.getMessage());
            }

            dataSources.put(config.getEnvironmentId(), dataSource);

            /** 创建并设置全局Jdbc管理类 */
            GlobalJdbcManager.set(config.getEnvironmentId(), JdbcManager.newInstance(config, dataSource));
        });
    }

    @Override
    public void closeAndClear() {
        dataSources.forEach((envId, dataSource) -> {
            try {
                cls.getMethod("close").invoke(dataSource);
            } catch (Throwable e) {}
        });

        dataSources.clear();
    }

    @Override
    public DataSource getPool(String envId) {
        return dataSources.get(envId);
    }

}
