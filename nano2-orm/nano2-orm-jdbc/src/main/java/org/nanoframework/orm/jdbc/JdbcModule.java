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

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.not;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nanoframework.orm.PoolType;
import org.nanoframework.orm.jdbc.binding.JdbcTransactional;
import org.nanoframework.orm.jdbc.binding.JdbcTransactionalMethodInterceptor;
import org.nanoframework.orm.jdbc.config.JdbcConfig;
import org.nanoframework.spi.def.Module;

import com.google.inject.AbstractModule;

/**
 * @author yanghe
 * @since 1.2
 */
public class JdbcModule extends AbstractModule implements Module {

    private Map<String, JdbcConfig> configs;

    private PoolType poolType;

    public JdbcModule(Map<String, JdbcConfig> configs, PoolType poolType) {
        this.configs = configs == null ? Collections.emptyMap() : configs;
        this.poolType = poolType == null ? PoolType.DRUID : poolType;
    }

    @Override
    protected void configure() {
        JdbcAdapter.newInstance(configs.values(), poolType, this.getClass());

        var interceptor = new JdbcTransactionalMethodInterceptor();
        requestInjection(interceptor);

        var annotatedElement = annotatedWith(JdbcTransactional.class);
        bindInterceptor(any(), annotatedElement, interceptor);
        bindInterceptor(annotatedElement, not(annotatedElement), interceptor);

    }

    @Override
    public List<Module> load() {
        return List.of(this);
    }

    @Override
    public void destroy() {

    }

}
