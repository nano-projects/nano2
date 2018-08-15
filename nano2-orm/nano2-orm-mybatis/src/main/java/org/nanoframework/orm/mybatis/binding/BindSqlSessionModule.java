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
package org.nanoframework.orm.mybatis.binding;

import static com.google.inject.name.Names.named;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.nanoframework.orm.mybatis.GlobalSqlSession;
import org.nanoframework.spi.annotation.Order;
import org.nanoframework.spi.def.Module;

import com.google.inject.Binder;

/**
 * @author yanghe
 * @since 1.3.16
 */
@Order(2200)
public class BindSqlSessionModule implements Module {
    private static final String JDBC_NAMED_PRIFIX = "mybatis:";

    @Override
    public void configure(Binder binder) {
        GlobalSqlSession.keys().forEach(jdbcName -> {
            var manager = GlobalSqlSession.get(jdbcName);
            var named = JDBC_NAMED_PRIFIX + jdbcName;

            binder.bind(SqlSession.class).annotatedWith(named(named)).toInstance(manager);
            binder.bind(SqlSessionManager.class).annotatedWith(named(named)).toInstance(manager);
        });
    }

    @Override
    public List<Module> load() {
        return List.of(this);
    }

    @Override
    public void destroy() {

    }

}
