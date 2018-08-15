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
package org.nanoframework.core.rest.invoker;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.nanoframework.beans.Globals;
import org.nanoframework.core.rest.annotation.Route;
import org.nanoframework.core.rest.exception.RouteException;
import org.nanoframework.spi.annotation.Order;
import org.nanoframework.spi.def.Module;
import org.nanoframework.spi.support.SPILoader;
import org.nanoframework.toolkit.lang.CollectionUtils;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class RouteInvokeModule implements Module {

    private Filter chain;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void configure(Binder binder) {
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Route.class), invocation -> {
            init();
            if (chain != null) {
                return chain.doFilter(invocation);
            } else {
                return invocation.proceed();
            }
        });
    }

    private void init() {
        if (!initialized.get()) {
            try {
                var injector = Globals.get(Injector.class);
                var names = SPILoader.spiNames(Filter.class);
                if (CollectionUtils.isNotEmpty(names)) {
                    var filters = names.stream()
                            .map(name -> injector.getInstance(Key.get(Filter.class, Names.named(name))))
                            .collect(Collectors.toList());
                    sort(filters);
                    chain(filters);
                }
            } catch (Throwable e) {
                throw new RouteException(e.getMessage(), e);
            } finally {
                initialized.set(true);
            }
        }
    }

    private void sort(List<Filter> filters) {
        filters.sort((before, after) -> {
            Integer beforeOrder = 0;
            Integer afterOrder = 0;
            var beforeCls = before.getClass();
            var afterCls = after.getClass();
            if (beforeCls.isAnnotationPresent(Order.class)) {
                beforeOrder = beforeCls.getAnnotation(Order.class).value();
            }

            if (afterCls.isAnnotationPresent(Order.class)) {
                afterOrder = afterCls.getAnnotation(Order.class).value();
            }

            return beforeOrder.compareTo(afterOrder);
        });
    }

    private void chain(List<Filter> filters) {
        Filter filter = null;
        for (var f : filters) {
            if (filter == null) {
                filter = f;
                chain = f;
            } else {
                filter.next(f);
                filter = f;
            }
        }
    }

    @Override
    public List<Module> load() {
        return List.of(this);
    }

    @Override
    public void destroy() {

    }

}
