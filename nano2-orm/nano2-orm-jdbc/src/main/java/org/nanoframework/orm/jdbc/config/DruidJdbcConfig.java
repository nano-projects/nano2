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
package org.nanoframework.orm.jdbc.config;

import java.util.Properties;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * @author yanghe
 * @since 1.2
 */
@Getter
@Setter
public class DruidJdbcConfig extends JdbcConfig {
    private static final long serialVersionUID = -565746278164485851L;

    @Property("druid.initialSize")
    private Integer initialSize;

    @Property("druid.maxActive")
    private Integer maxActive;

    @Property("druid.maxIdle")
    private Integer maxIdle;

    @Property("druid.minIdle")
    private Integer minIdle;

    @Property("druid.maxWait")
    private Long maxWait;

    @Property("druid.removeAbandoned")
    private Boolean removeAbandoned;

    @Property("druid.removeAbandonedTimeout")
    private Integer removeAbandonedTimeout;

    @Property("druid.timeBetweenEvictionRunsMillis")
    private Long timeBetweenEvictionRunsMillis;

    @Property("druid.minEvictableIdleTimeMillis")
    private Long minEvictableIdleTimeMillis;

    @Property("druid.validationQuery")
    private String validationQuery;

    @Property("druid.testWhileIdle")
    private Boolean testWhileIdle;

    @Property("druid.testOnBorrow")
    private Boolean testOnBorrow;

    @Property("druid.testOnReturn")
    private Boolean testOnReturn;

    @Property("druid.poolPreparedStatements")
    private Boolean poolPreparedStatements;

    @Property("druid.maxPoolPreparedStatementPerConnectionSize")
    private Integer maxPoolPreparedStatementPerConnectionSize;

    @Property("druid.filters")
    private String filters;

    public DruidJdbcConfig() {

    }

    public DruidJdbcConfig(@NonNull Properties properties) {
        this.setProperties(properties);
    }

}
