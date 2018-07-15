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
 * @since 1.3.6
 */
@Getter
@Setter
public class TomcatJdbcConfig extends JdbcConfig {
    private static final long serialVersionUID = -841473595983679697L;

    @Property("tomcat.jdbc.pool.initialSize")
    private Integer initialSize;

    @Property("tomcat.jdbc.pool.minIdle")
    private Integer minIdle;

    @Property("tomcat.jdbc.pool.maxWait")
    private Integer maxWait;

    @Property("tomcat.jdbc.pool.maxActive")
    private Integer maxActive;

    @Property("tomcat.jdbc.pool.testWhileIdle")
    private Boolean testWhileIdle;

    @Property("tomcat.jdbc.pool.testOnBorrow")
    private Boolean testOnBorrow;

    @Property("tomcat.jdbc.pool.validationQuery")
    private String validationQuery;

    @Property("tomcat.jdbc.pool.testOnReturn")
    private Boolean testOnReturn;

    @Property("tomcat.jdbc.pool.validationInterval")
    private Long validationInterval;

    @Property("tomcat.jdbc.pool.timeBetweenEvictionRunsMillis")
    private Integer timeBetweenEvictionRunsMillis;

    @Property("tomcat.jdbc.pool.logAbandoned")
    private Boolean logAbandoned;

    @Property("tomcat.jdbc.pool.removeAbandoned")
    private Boolean removeAbandoned;

    @Property("tomcat.jdbc.pool.removeAbandonedTimeout")
    private Integer removeAbandonedTimeout;

    @Property("tomcat.jdbc.pool.minEvictableIdleTimeMillis")
    private Integer minEvictableIdleTimeMillis;

    @Property("tomcat.jdbc.pool.jdbcInterceptors")
    private String jdbcInterceptors;

    @Property("tomcat.jdbc.pool.jmxEnabled")
    private Boolean jmxEnabled;

    public TomcatJdbcConfig() {

    }

    public TomcatJdbcConfig(@NonNull Properties properties) {
        this.setProperties(properties);
    }

}
