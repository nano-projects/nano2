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
package org.nanoframework.modules.sentinel.filter;

import org.nanoframework.beans.Globals;
import org.nanoframework.core.rest.annotation.Mock;
import org.nanoframework.core.rest.annotation.Route;
import org.nanoframework.core.rest.invoker.Filter;
import org.nanoframework.core.rest.invoker.Invoker;
import org.nanoframework.core.rest.mock.Mocker;
import org.nanoframework.modules.sentinel.exception.SentinelBlockException;
import org.nanoframework.modules.sentinel.exception.SentinelException;

import com.alibaba.csp.sentinel.SphO;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Singleton
public class SentinelFilter extends Filter {

    @Override
    protected Object proceed(Invoker invoker) throws Throwable {
        var type = invoker.getType();
        var method = invoker.getMethod();
        String route;
        if (type.isAnnotationPresent(Route.class)) {
            route = type.getAnnotation(Route.class).value() + method.getAnnotation(Route.class).value();
        } else {
            route = method.getAnnotation(Route.class).value();
        }

        if (SphO.entry(route)) {
            try {
                return doNext();
            } finally {
                SphO.exit();
            }
        } else {
            if (method.isAnnotationPresent(Mock.class)) {
                var mock = method.getAnnotation(Mock.class);
                var injector = Globals.get(Injector.class);
                try {
                    var mocker = injector.getInstance(Key.get(Mocker.class, Names.named(mock.value())));
                    return mocker.mock(invoker);
                } catch (Throwable e) {
                    throw new SentinelException(e.getMessage(), e);
                }
            }

            throw new SentinelBlockException("当前服务已被降级");
        }

    }

}
