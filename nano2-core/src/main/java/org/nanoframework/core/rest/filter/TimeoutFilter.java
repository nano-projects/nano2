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
package org.nanoframework.core.rest.filter;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.nanoframework.beans.Globals;
import org.nanoframework.core.concurrent.Executor;
import org.nanoframework.core.rest.annotation.Route;
import org.nanoframework.core.rest.exception.NotFoundMockException;
import org.nanoframework.core.rest.invoker.Filter;
import org.nanoframework.core.rest.invoker.Invoker;
import org.nanoframework.core.web.filter.RouteFilter.HttpContext.HttpContextCopies;
import org.nanoframework.spi.annotation.Order;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Order(10000)
public class TimeoutFilter extends Filter {

    @Override
    protected Object proceed(Invoker invoker) throws Throwable {
        var method = invoker.getMethod();
        var route = method.getAnnotation(Route.class);
        var timeout = route.timeout();
        try {
            return submit(route).get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return mock(method);
        }
    }

    private Future<Object> submit(Route route) {
        var context = new HttpContextCopies();
        return executor(route).submit(new Callable<>() {
            @Override
            public Object call() throws Exception {
                try {
                    context.copy();
                    return doNext();
                } catch (Throwable e) {
                    throw new Exception(e.getMessage(), e);
                } finally {
                    context.clear();
                }
            }
        });
    }

    private Executor executor(Route route) {
        var executor = route.executor();
        try {
            return Globals.get(Injector.class).getInstance(Key.get(Executor.class, Names.named(executor)));
        } catch (Throwable e) {
            throw new IllegalArgumentException(String.format("未定义线程池扩展: %s", executor));
        }
    }

    private Object mock(Method method) throws TimeoutException {
        try {
            return mock0(method);
        } catch (Throwable e) {
            if (e instanceof NotFoundMockException) {
                throw new TimeoutException("服务调用超时");
            }

            throw e;
        }
    }
}
