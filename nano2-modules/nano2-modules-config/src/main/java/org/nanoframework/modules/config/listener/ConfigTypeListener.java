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
package org.nanoframework.modules.config.listener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.nanoframework.beans.Globals;
import org.nanoframework.beans.format.ClassCast;
import org.nanoframework.modules.base.listener.AbstractTypeListener;
import org.nanoframework.modules.base.listener.NotifyListener;
import org.nanoframework.modules.config.ConfigMapper;
import org.nanoframework.modules.config.annotation.Value;
import org.nanoframework.modules.config.exception.ConfigException;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.toolkit.lang.ArrayUtils;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.RuntimeUtils;
import org.nanoframework.toolkit.lang.StringUtils;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class ConfigTypeListener extends AbstractTypeListener<Value> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigTypeListener.class);

    private Map<String, DefaultConfigChangeListener> listeners = Maps.newConcurrentMap();

    @Override
    protected Class<? extends Value> type() {
        return Value.class;
    }

    @Override
    protected void init(Value value, Class<?> type, Object instance, Field field) {
        DefaultConfigChangeListener.add(ConfigMapper.create(value, instance, field));

        var config = ConfigService.getConfig(value.namespace());
        var key = value.value();
        var initValue = initValue(value, config);

        try {
            field.set(instance, ClassCast.cast(initValue, field.getType().getName()));
        } catch (Throwable e) {
            throw new ConfigException(String.format("设置置配异常: %s, message: %s", key, e.getMessage()), e);
        }

        addListener(value, config, initValue);
    }

    @Override
    protected void init(Value value, Class<?> type, Object instance, Method method) {
        DefaultConfigChangeListener.add(ConfigMapper.create(value, instance, method));

        var config = ConfigService.getConfig(value.namespace());
        var key = value.value();
        var initValue = initValue(value, config);

        try {
            method.invoke(instance, ClassCast.cast(initValue, method.getParameters()[0].getType().getName()));
        } catch (Throwable e) {
            throw new ConfigException(String.format("设置置配异常: %s, message: %s", key, e.getMessage()), e);
        }

        addListener(value, config, initValue);
    }

    private String initValue(Value value, Config config) {
        var key = value.value();
        var initValue = config.getProperty(key, System.getProperty(key, value.defaultValue()));
        if (value.required() && StringUtils.isEmpty(initValue)) {
            throw new ConfigException(String.format("未设置配置: %s", key));
        }

        return initValue;
    }

    private void addListener(Value value, Config config, String initValue) {
        var ns = value.namespace();
        if (!listeners.containsKey(ns)) {
            var listener = new DefaultConfigChangeListener(ns);
            listeners.put(ns, listener);
            config.addChangeListener(listener);
        } else {
            listeners.get(ns).init(value, initValue);
        }
    }

    @Override
    public void close() throws IOException {
        if (CollectionUtils.isNotEmpty(listeners)) {
            DefaultConfigChangeListener.clear();
        }
    }

    private static class DefaultConfigChangeListener implements ConfigChangeListener {
        private static final Map<String, Map<String, List<ConfigMapper>>> MAPPER = Maps.newConcurrentMap();

        private static final Executor EXECUTOR = Executors.newFixedThreadPool(RuntimeUtils.AVAILABLE_PROCESSORS,
                runnable -> {
                    var thread = new Thread(runnable);
                    thread.setName("NotifyListener-Thread-" + thread.getId());
                    return thread;
                });

        private Map<String, NotifyListener> listeners = Maps.newHashMap();

        private String namespace;

        public DefaultConfigChangeListener(String namespace) {
            this.namespace = namespace;
        }

        public void init(Value value, String initValue) {
            var injector = Globals.get(Injector.class);
            var listeners = value.listeners();
            if (ArrayUtils.isNotEmpty(listeners)) {
                Arrays.stream(listeners).forEach(listenerName -> {
                    if (StringUtils.isNotBlank(listenerName)) {
                        var listener = injector.getInstance(Key.get(NotifyListener.class, Names.named(listenerName)));
                        EXECUTOR.execute(() -> listener.notify(value.value(), initValue));
                        this.listeners.putIfAbsent(listenerName, listener);
                    }
                });
            }
        }

        @Override
        public void onChange(ConfigChangeEvent event) {
            if (!StringUtils.equals(namespace, event.getNamespace())) {
                return;
            }

            var keys = event.changedKeys();
            if (CollectionUtils.isNotEmpty(keys)) {
                keys.stream().filter(MAPPER::containsKey).forEach(key -> {
                    var ns = MAPPER.get(key);
                    var change = event.getChange(key);
                    var cms = ns.get(change.getNamespace());
                    if (CollectionUtils.isNotEmpty(cms)) {
                        var changeType = change.getChangeType();
                        if (changeType == PropertyChangeType.DELETED) {
                            removeValue(cms);
                            notifyRemoveEvent(listenerNames(cms), key);
                            LOGGER.warn("配置中心配置已被删除，本地设置为默认值: {}", key);
                        } else {
                            var newValue = change.getNewValue();
                            change(cms, newValue);
                            notifyChangeEvent(listenerNames(cms), key, newValue);
                            LOGGER.debug("配置变更通知: key = {}, value = {}", key, newValue);
                        }
                    }
                });
            }
        }

        private void removeValue(List<ConfigMapper> cms) {
            cms.forEach(cm -> {
                removeFieldValue(cm);
                removeMethodValue(cm);
            });
        }

        private void removeFieldValue(ConfigMapper cm) {
            try {
                var key = cm.getKey();
                var field = cm.getField();
                if (field != null) {
                    var value = field.getAnnotation(Value.class);
                    var defaultValue = System.getProperty(key, value.defaultValue());
                    field.set(cm.getInstance(), ClassCast.cast(defaultValue, field.getType().getName()));
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("设置属性配置异常: %s", e.getMessage()), e);
            }
        }

        private void removeMethodValue(ConfigMapper cm) {
            try {
                var key = cm.getKey();
                var method = cm.getMethod();
                if (method != null) {
                    var value = method.getAnnotation(Value.class);
                    var defaultValue = System.getProperty(key, value.defaultValue());
                    method.invoke(cm.getInstance(),
                            ClassCast.cast(defaultValue, method.getParameters()[0].getType().getName()));
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("设置方法配置异常: %s", e.getMessage()), e);
            }
        }

        private void change(List<ConfigMapper> cms, String value) {
            cms.forEach(cm -> {
                changeFieldValue(cm, value);
                changeMethodValue(cm, value);
            });
        }

        private void changeFieldValue(ConfigMapper cm, String value) {
            var field = cm.getField();
            if (field != null) {
                var fieldType = field.getType().getName();
                try {
                    field.set(cm.getInstance(), ClassCast.cast(value, fieldType));
                } catch (Throwable e) {
                    LOGGER.error(String.format("设置属性配置异常: %s", e.getMessage()), e);
                }
            }
        }

        private void changeMethodValue(ConfigMapper cm, String value) {
            var method = cm.getMethod();
            if (method != null) {
                try {
                    method.invoke(cm.getInstance(),
                            ClassCast.cast(value, method.getParameters()[0].getType().getName()));
                } catch (Throwable e) {
                    LOGGER.error(String.format("设置方法配置异常: %s", e.getMessage()), e);
                }
            }
        }

        private Set<String> listenerNames(List<ConfigMapper> cms) {
            if (CollectionUtils.isEmpty(cms)) {
                return Collections.emptySet();
            }

            Set<String> listeners = Sets.newHashSet();
            cms.stream().map(ConfigMapper::getField).filter(field -> field != null).map(field -> {
                var listens = field.getAnnotation(Value.class).listeners();
                if (ArrayUtils.isEmpty(listens)) {
                    return Collections.emptySet();
                }

                return Sets.newHashSet(listens);
            }).forEach(listens -> listens.forEach(listen -> listeners.add((String) listen)));

            cms.stream().map(ConfigMapper::getMethod).filter(method -> method != null).map(method -> {
                var listens = method.getAnnotation(Value.class).listeners();
                if (ArrayUtils.isEmpty(listens)) {
                    return Collections.emptySet();
                }

                return Sets.newHashSet(listeners);
            }).forEach(listens -> listens.forEach(listen -> listeners.add((String) listen)));
            return listeners;
        }

        private void notifyRemoveEvent(Set<String> listenerNames, String key) {
            listenerNames.forEach(name -> {
                if (listeners.containsKey(name)) {
                    var listener = listeners.get(name);
                    EXECUTOR.execute(() -> listener.remove(key));
                }
            });
        }

        private void notifyChangeEvent(Set<String> listenerNames, String key, String value) {
            listenerNames.forEach(name -> {
                if (listeners.containsKey(name)) {
                    var listener = listeners.get(name);
                    EXECUTOR.execute(() -> listener.notify(key, value));
                }
            });
        }

        private static void add(ConfigMapper cm) {
            var key = cm.getKey();
            if (!MAPPER.containsKey(key)) {
                MAPPER.put(key, Maps.newConcurrentMap());
            }

            var ns = MAPPER.get(key);
            var namespace = cm.getNamespace();
            if (!ns.containsKey(namespace)) {
                ns.put(namespace, Collections.synchronizedList(Lists.newArrayList()));
            }

            ns.get(namespace).add(cm);
        }

        private static void clear() {
            if (CollectionUtils.isNotEmpty(MAPPER)) {
                MAPPER.values().forEach(ns -> {
                    if (CollectionUtils.isNotEmpty(ns)) {
                        var configs = ns.values();
                        if (CollectionUtils.isNotEmpty(configs)) {
                            configs.clear();
                        }
                    }

                    ns.clear();
                });

                MAPPER.clear();
            }
        }
    }
}
