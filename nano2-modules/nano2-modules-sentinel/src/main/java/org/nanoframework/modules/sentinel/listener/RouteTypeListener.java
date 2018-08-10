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
package org.nanoframework.modules.sentinel.listener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.nanoframework.beans.Globals;
import org.nanoframework.core.rest.annotation.Route;
import org.nanoframework.modules.base.listener.AbstractTypeListener;
import org.nanoframework.modules.base.listener.NotifyListener;
import org.nanoframework.modules.config.exception.ConfigException;
import org.nanoframework.modules.sentinel.annotation.Sentinel;
import org.nanoframework.toolkit.lang.ArrayUtils;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.RuntimeUtils;
import org.nanoframework.toolkit.lang.StringUtils;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class RouteTypeListener extends AbstractTypeListener<Route> {
    private static final String PREFIX =  "Sentinel:";
    private Map<String, DefaultConfigChangeListener> listeners = Maps.newConcurrentMap();

    @Override
    protected Class<? extends Route> type() {
        return Route.class;
    }

    @Override
    protected void init(Route route, Class<?> type, Object instance, Method method) {
        if (method.isAnnotationPresent(Sentinel.class)) {
            var sentinel = method.getAnnotation(Sentinel.class);
            var config = ConfigService.getConfig(sentinel.namespace());
            var key = PREFIX;
            if (type.isAnnotationPresent(Route.class)) {
                var clsRoute = type.getAnnotation(Route.class);
                key += clsRoute.value() + route.value();
            } else {
                key += route.value();
            }

            DefaultConfigChangeListener.add(key);
            var initValue = config.getProperty(key, System.getProperty(key, sentinel.defaultValue()));
            if (sentinel.required() && StringUtils.isEmpty(initValue)) {
                throw new ConfigException(String.format("未设置配置: %s", key));
            }

            var ns = sentinel.namespace();
            if (!listeners.containsKey(ns)) {
                var listener = new DefaultConfigChangeListener(sentinel, key, initValue);
                listeners.put(ns, listener);
                config.addChangeListener(listener);
            }
        }
    }

    @Override
    public void close() throws IOException {
        DefaultConfigChangeListener.clear();
    }

    private static class DefaultConfigChangeListener implements ConfigChangeListener {
        private static final Set<String> KEYS = Sets.newConcurrentHashSet();

        private static final Executor EXECUTOR = Executors.newFixedThreadPool(RuntimeUtils.AVAILABLE_PROCESSORS,
                runnable -> {
                    var thread = new Thread(runnable);
                    thread.setName("NotifyListener-Thread-" + thread.getId());
                    return thread;
                });

        private Map<String, NotifyListener> listeners = Maps.newHashMap();

        public DefaultConfigChangeListener(Sentinel sentinel, String key, String initValue) {
            var injector = Globals.get(Injector.class);
            var listeners = sentinel.listeners();
            if (ArrayUtils.isNotEmpty(listeners)) {
                Arrays.stream(listeners).forEach(listenerName -> {
                    if (StringUtils.isNotBlank(listenerName)) {
                        var listener = injector.getInstance(Key.get(NotifyListener.class, Names.named(listenerName)));
                        EXECUTOR.execute(() -> listener.notify(key, initValue));
                        this.listeners.putIfAbsent(listenerName, listener);
                    }
                });
            }
        }

        @Override
        public void onChange(ConfigChangeEvent event) {
            var keys = event.changedKeys();
            if (CollectionUtils.isNotEmpty(keys)) {
                keys.stream().filter(KEYS::contains).forEach(key -> {
                    var change = event.getChange(key);
                    var changeType = change.getChangeType();
                    if (changeType == PropertyChangeType.DELETED) {
                        listeners.values().forEach(listener -> EXECUTOR.execute(() -> listener.remove(key)));
                    } else {
                        listeners.values().forEach(
                                listener -> EXECUTOR.execute(() -> listener.notify(key, change.getNewValue())));
                    }
                });
            }
        }

        private static void add(String key) {
            KEYS.add(key);
        }

        private static void clear() {
            KEYS.clear();
        }
    }
}
