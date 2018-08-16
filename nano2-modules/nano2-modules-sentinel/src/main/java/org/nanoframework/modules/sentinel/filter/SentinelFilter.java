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

import java.lang.reflect.Method;

import org.nanoframework.core.rest.annotation.Route;
import org.nanoframework.core.rest.exception.NotFoundMockException;
import org.nanoframework.core.rest.invoker.Filter;
import org.nanoframework.core.rest.invoker.Invoker;
import org.nanoframework.modules.sentinel.exception.SentinelBlockException;

import com.alibaba.csp.sentinel.SphO;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class SentinelFilter extends Filter {

    @Override
    protected Object proceed(Invoker invoker) throws Throwable {
        if (SphO.entry(route(invoker))) {
            try {
                return doNext();
            } finally {
                SphO.exit();
            }
        } else {
            return mock(invoker.getMethod());
        }
    }

    private String route(Invoker invoker) {
        var type = invoker.getType();
        var method = invoker.getMethod();
        if (type.isAnnotationPresent(Route.class)) {
            return type.getAnnotation(Route.class).value() + method.getAnnotation(Route.class).value();
        } else {
            return method.getAnnotation(Route.class).value();
        }
    }

    private Object mock(Method method) {
        try {
            return mock0(method);
        } catch (Throwable e) {
            if (e instanceof NotFoundMockException) {
                throw new SentinelBlockException("当前服务已被降级");
            }

            throw e;
        }
    }

}
