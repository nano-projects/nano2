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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.orm.jdbc.binding.GlobalJdbcManager;
import org.nanoframework.orm.jdbc.binding.JdbcManager;
import org.nanoframework.orm.jdbc.config.DruidJdbcConfig;
import org.nanoframework.orm.jdbc.config.JdbcConfig;
import org.nanoframework.toolkit.lang.CollectionUtils;

/**
 * @author yanghe
 * @since 1.2
 */
public class DruidPool implements Pool {
    private static final Logger LOGGER = LoggerFactory.getLogger(DruidPool.class);

    private final ConcurrentMap<String, DataSource> dataSources = new ConcurrentHashMap<>();

    private Class<?> cls;
    {
        try {
            cls = Class.forName("com.alibaba.druid.pool.DruidDataSource");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Unload class [ com.alibaba.druid.pool.DruidDataSource ]");
        }
    }

    public DruidPool(Collection<JdbcConfig> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            throw new IllegalArgumentException("无效的JDBC配置");
        }

        if (dataSources != null && !dataSources.isEmpty()) {
            closeAndClear();
        }

        List<DruidJdbcConfig> druidJdbcConfigs = new ArrayList<>();
        configs.forEach(config -> druidJdbcConfigs.add((DruidJdbcConfig) config));
        configs.stream().map(config -> (DruidJdbcConfig) config).forEach(config -> {
            DataSource ds;
            try {
                ds = (DataSource) cls.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Instance Constructor Exception: " + cls.getName());
            }

            try {
                cls.getMethod("setDriverClassName", String.class).invoke(ds, config.getDriver());
                cls.getMethod("setUrl", String.class).invoke(ds, config.getUrl());
                cls.getMethod("setUsername", String.class).invoke(ds, config.getUserName());
                cls.getMethod("setPassword", String.class).invoke(ds, config.getPasswd());

                var initalSize = config.getInitialSize();
                if (initalSize != null) {
                    cls.getMethod("setInitialSize", int.class).invoke(ds, initalSize);
                }

                var maxActive = config.getMaxActive();
                if (maxActive != null) {
                    cls.getMethod("setMaxActive", int.class).invoke(ds, maxActive);
                }

                var maxIdle = config.getMaxIdle();
                if (maxIdle != null) {
                    cls.getMethod("setMaxIdle", int.class).invoke(ds, maxIdle);
                }

                var minIdle = config.getMinIdle();
                if (minIdle != null) {
                    cls.getMethod("setMinIdle", int.class).invoke(ds, minIdle);
                }

                var maxWait = config.getMaxWait();
                if (maxWait != null) {
                    cls.getMethod("setMaxWait", long.class).invoke(ds, maxWait);
                }

                var removeAbandoned = config.getRemoveAbandoned();
                if (removeAbandoned != null) {
                    cls.getMethod("setRemoveAbandoned", boolean.class).invoke(ds, removeAbandoned);
                }

                var removeAbandonedTimeout = config.getRemoveAbandonedTimeout();
                if (removeAbandonedTimeout != null) {
                    cls.getMethod("setRemoveAbandonedTimeout", int.class).invoke(ds, removeAbandonedTimeout);
                }

                var timeBetweenEvictionRunsMillis = config.getTimeBetweenEvictionRunsMillis();
                if (timeBetweenEvictionRunsMillis != null) {
                    cls.getMethod("setTimeBetweenEvictionRunsMillis", long.class).invoke(ds,
                            timeBetweenEvictionRunsMillis);
                }

                var minEvictableIdleTimeMillis = config.getMinEvictableIdleTimeMillis();
                if (minEvictableIdleTimeMillis != null) {
                    cls.getMethod("setMinEvictableIdleTimeMillis", long.class).invoke(ds, minEvictableIdleTimeMillis);
                }

                var validationQuery = config.getValidationQuery();
                if (validationQuery != null) {
                    cls.getMethod("setValidationQuery", String.class).invoke(ds, validationQuery);
                }

                var testWhileIdle = config.getTestWhileIdle();
                if (testWhileIdle != null) {
                    cls.getMethod("setTestWhileIdle", boolean.class).invoke(ds, testWhileIdle);
                }

                var testOnBorrow = config.getTestOnBorrow();
                if (testOnBorrow != null) {
                    cls.getMethod("setTestOnBorrow", boolean.class).invoke(ds, testOnBorrow);
                }

                var testOnReturn = config.getTestOnReturn();
                if (testOnReturn != null) {
                    cls.getMethod("setTestOnReturn", boolean.class).invoke(ds, testOnReturn);
                }

                var poolPreparedStatements = config.getPoolPreparedStatements();
                if (poolPreparedStatements != null) {
                    cls.getMethod("setPoolPreparedStatements", boolean.class).invoke(ds, poolPreparedStatements);
                }

                var maxPoolPreparedStatementPerConnectionSize = config.getMaxPoolPreparedStatementPerConnectionSize();
                if (maxPoolPreparedStatementPerConnectionSize != null) {
                    cls.getMethod("setMaxPoolPreparedStatementPerConnectionSize", int.class).invoke(ds,
                            maxPoolPreparedStatementPerConnectionSize);
                }

                var filters = config.getFilters();
                if (filters != null) {
                    cls.getMethod("setFilters", String.class).invoke(ds, filters);
                }

            } catch (Throwable e) {
                throw new IllegalArgumentException("设置参数异常: " + e.getMessage());
            }

            dataSources.put(config.getEnvironmentId(), ds);

            /** 创建并设置全局Jdbc管理类 */
            GlobalJdbcManager.set(config.getEnvironmentId(), JdbcManager.newInstance(config, ds));
        });
    }

    @Override
    public void closeAndClear() {
        dataSources.forEach((envId, dataSource) -> {
            try {
                cls.getMethod("close").invoke(dataSource);
            } catch (Exception e) {}
        });

        dataSources.clear();
    }

    @Override
    public DataSource getPool(String envId) {
        return dataSources.get(envId);
    }
}
