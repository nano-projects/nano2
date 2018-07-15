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
package org.nanoframework.orm.mybatis.plugin;

import java.util.HashMap;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.DataSourceException;
import org.nanoframework.orm.jdbc.config.TomcatJdbcConfig;
import org.nanoframework.toolkit.lang.StringUtils;

/**
 * @author yanghe
 * @since 1.3.6
 */
public class TomcatJdbcPoolDataSourceFactory extends AbstractDataSourceFactory {
    private Class<?> cls;

    public TomcatJdbcPoolDataSourceFactory() {
        try {
            cls = Class.forName("org.apache.tomcat.jdbc.pool.DataSource");
            this.dataSource = (DataSource) cls.getConstructor().newInstance();
        } catch (Throwable e) {
            throw new DataSourceException(e.getMessage(), e);
        }
    }

    @Override
    public void setProperties(Properties properties) {
        try {
            var map = new HashMap<String, Object>();
            properties.forEach((key, obj) -> {
                var value = (String) obj;
                if (StringUtils.isNotEmpty(value) && (value).startsWith("${") && (value).endsWith("}")) {
                    return;
                }

                map.put((String) key, value);
            });

            var config = TomcatJdbcConfig.mapToBean(map, TomcatJdbcConfig.class);
            if (cls != null) {
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
            } else {
                throw new DataSourceException("Unknown class [ org.apache.tomcat.jdbc.pool.DataSource ]");
            }
        } catch (Throwable e) {
            throw new DataSourceException(e.getMessage(), e);
        }
    }
}
