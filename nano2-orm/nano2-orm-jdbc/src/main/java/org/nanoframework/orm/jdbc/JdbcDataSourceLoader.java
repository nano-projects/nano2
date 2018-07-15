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
package org.nanoframework.orm.jdbc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.nanoframework.orm.DataSourceLoader;
import org.nanoframework.orm.ORMType;
import org.nanoframework.orm.PoolType;
import org.nanoframework.orm.jdbc.config.DruidJdbcConfig;
import org.nanoframework.orm.jdbc.config.JdbcConfig;
import org.nanoframework.orm.jdbc.config.TomcatJdbcConfig;

import lombok.NonNull;

/**
 * @author yanghe
 * @since 1.2
 */
public class JdbcDataSourceLoader extends DataSourceLoader {
    private Map<PoolType, Map<String, JdbcConfig>> configAggs = new HashMap<>();

    public JdbcDataSourceLoader() {
        load();
        toModule();
    }

    @Override
    public void load() {
        load0(ORMType.JDBC);
    }

    @Override
    public void toConfig(@NonNull Properties properties) {
        var poolType = poolType(properties);
        switch (poolType) {
            case DRUID:
                toConfig(new DruidJdbcConfig(properties), poolType);
                break;
            case TOMCAT_JDBC_POOL:
                toConfig(new TomcatJdbcConfig(properties), poolType);
                break;
        }
    }

    private void toConfig(JdbcConfig config, PoolType poolType) {
        var configs = configAggs.get(poolType);
        if (configs == null) {
            configs = new LinkedHashMap<String, JdbcConfig>();
            configs.put(config.getEnvironmentId(), config);
            configAggs.put(poolType, configs);
        } else {
            configs.put(config.getEnvironmentId(), config);
        }
    }

    @Override
    public void toModule() {
        for (var item : configAggs.entrySet()) {
            modules.add(new JdbcModule(item.getValue(), item.getKey()));
        }
    }
}
