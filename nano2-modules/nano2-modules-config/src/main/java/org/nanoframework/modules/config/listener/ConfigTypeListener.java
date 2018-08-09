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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nanoframework.beans.format.ClassCast;
import org.nanoframework.modules.base.listener.AbstractTypeListener;
import org.nanoframework.modules.config.ConfigMapper;
import org.nanoframework.modules.config.annotation.Value;
import org.nanoframework.modules.config.exception.ConfigException;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.toolkit.lang.CollectionUtils;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
    protected void init(Value value, Object instance, Field field) {
        DefaultConfigChangeListener.add(ConfigMapper.create(value, instance, field));

        var config = ConfigService.getConfig(value.namespace());
        var ns = value.namespace();
        if (!listeners.containsKey(ns)) {
            var listener = new DefaultConfigChangeListener();
            listeners.put(ns, listener);
            config.addChangeListener(listener);
        }

        var key = value.value();
        var v = config.getProperty(key, System.getProperty(key, value.defaultValue()));
        if (value.required() && StringUtils.isEmpty(v)) {
            throw new ConfigException(String.format("未设置配置: %s", key));
        }

        try {
            field.set(instance, ClassCast.cast(v, field.getType().getName()));
        } catch (Throwable e) {
            throw new ConfigException(String.format("设置置配异常: %s, message: %s", key, e.getMessage()), e);
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

        @Override
        public void onChange(ConfigChangeEvent event) {
            var keys = event.changedKeys();
            if (CollectionUtils.isNotEmpty(keys)) {
                keys.stream().filter(MAPPER::containsKey).forEach(key -> {
                    var ns = MAPPER.get(key);
                    var change = event.getChange(key);
                    var cms = ns.get(change.getNamespace());
                    if (CollectionUtils.isNotEmpty(cms)) {
                        change(cms, change.getNewValue());
                    }
                });
            }
        }

        private void change(List<ConfigMapper> cms, String value) {
            cms.forEach(cm -> {
                var field = cm.getField();
                var fieldType = field.getType().getName();
                try {
                    field.set(cm.getInstance(), ClassCast.cast(value, fieldType));
                    LOGGER.debug("配置变更通知: key = {}, value = {}", cm.getKey(), value);
                } catch (Throwable e) {
                    LOGGER.error(String.format("设置配置异常: %s", e.getMessage()), e);
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
