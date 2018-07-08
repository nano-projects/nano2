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

import static org.nanoframework.modules.config.listener.DefaultConfigChangeListener.add;

import java.lang.reflect.Field;
import java.util.Map;

import org.nanoframework.beans.format.ClassCast;
import org.nanoframework.modules.base.listener.AbstractTypeListener;
import org.nanoframework.modules.config.ConfigMapper;
import org.nanoframework.modules.config.annotation.Value;
import org.nanoframework.modules.config.exception.ConfigException;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class ConfigTypeListener extends AbstractTypeListener<Value> {
    private Map<String, ConfigChangeListener> listeners = Maps.newConcurrentMap();

    @Override
    protected Class<? extends Value> type() {
        return Value.class;
    }

    @Override
    protected void init(Value value, Object instance, Field field) {
        add(ConfigMapper.create(value, instance, field));

        var config = ConfigService.getConfig(value.namespace());
        if (!listeners.containsKey(value.namespace())) {
            var listener = new DefaultConfigChangeListener();
            listeners.put(value.namespace(), listener);
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

}
