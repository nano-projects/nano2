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
package org.nanoframework.orm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.orm.jdbc.DataSourceException;
import org.nanoframework.spi.def.Module;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.properties.PropertiesLoader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.NonNull;

/**
 * @author yanghe
 * @since 1.2
 */
public abstract class DataSourceLoader {
    public static final String JDBC_ENVIRONMENT_ID = "JDBC.environment.id";

    public static final String MYBATIS_ENVIRONMENT_ID = "mybatis.environment.id";

    public static final String MAPPER_PACKAGE_ROOT = "mapper.package.root";

    public static final String MAPPER_PACKAGE_NAME = "mapper.package.name";

    public static final String MAPPER_PACKAGE_JDBC = "mapper.package.jdbc";

    public static final String MAPPER_PACKAGE_HELPER = "mapper.package.helper";

    public static final String JDBC_POOL_TYPE = "JDBC.pool.type";

    public static final String MAPPER_PACKAGE_TYPE_ALIAS = "mapper.package.typeAlias";

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceLoader.class);

    protected Collection<Module> modules = Lists.newArrayList();

    protected Map<String, Properties> newLoadProperties = Maps.newHashMap();

    public abstract void load();

    public abstract void toConfig(Properties properties);

    public abstract void toModule();

    protected void load0(ORMType ormType) {
        for (var properties : PropertiesLoader.PROPERTIES.values()) {
            var source = getProperties(properties.getProperty(MAPPER_PACKAGE_JDBC), ormType);
            if (source != null) {
                toConfig(source);
            }

            var mapperPackageRoot = properties.getProperty(MAPPER_PACKAGE_ROOT);
            if (StringUtils.isNotBlank(mapperPackageRoot)) {
                var roots = mapperPackageRoot.split(",");
                Arrays.stream(roots).forEach(root -> {
                    var multiSource = getProperties(properties.getProperty(MAPPER_PACKAGE_JDBC + '.' + root), ormType);
                    if (multiSource != null) {
                        toConfig(multiSource);
                    }
                });
            }
        }
    }

    protected ORMType ormType(@NonNull Properties properties) {
        var jdbcMode = StringUtils.isNotBlank(properties.getProperty(JDBC_ENVIRONMENT_ID));
        var mybatisMode = StringUtils.isNotBlank(properties.getProperty(MYBATIS_ENVIRONMENT_ID));
        if (jdbcMode && mybatisMode) {
            throw new DataSourceException("不能同时设置属性JDBC.environment.id和mybatis.environment.id");
        }

        if (jdbcMode) {
            return ORMType.JDBC;
        }

        if (mybatisMode) {
            return ORMType.MYBATIS;
        }

        throw new DataSourceException("未指定数据源名称，必须设置属性JDBC.environment.id或mybatis.environment.id");
    }

    protected PoolType poolType(Properties properties) {
        try {
            return PoolType.valueOf(properties.getProperty(JDBC_POOL_TYPE));
        } catch (final Throwable e) {
            LOGGER.warn("未设置有效的JDBC.pool.type, 现在使用默认的连接池: DRUID");
            return PoolType.DRUID;
        }
    }

    protected Properties getProperties(final String path, final ORMType ormType) {
        if (StringUtils.isNotBlank(path)) {
            var properties = PropertiesLoader.PROPERTIES.get(path);
            if (properties == null) {
                properties = PropertiesLoader.load(path);
                if (properties != null) {
                    newLoadProperties.put(path, properties);
                } else {
                    throw new DataSourceException("数据源没有配置或配置错误: " + path);
                }
            }

            var type = ormType(properties);
            if (type == ormType) {
                return properties;
            }
        }

        return null;
    }

    public Map<String, Properties> getProperties() {
        return newLoadProperties;
    }

    public Collection<Module> getModules() {
        return modules;
    }
}
