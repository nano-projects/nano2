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
package org.nanoframework.config.listener;

import static org.nanoframework.config.listener.DefaultConfigChangeListener.add;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.nanoframework.beans.format.ClassCast;
import org.nanoframework.config.ConfigMapper;
import org.nanoframework.config.annotation.Value;
import org.nanoframework.config.exception.ConfigException;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class ConfigTypeListener implements TypeListener {
    private Map<String, ConfigChangeListener> listeners = Maps.newConcurrentMap();

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        var fields = fields(Lists.newArrayList(), type.getRawType());
        fields.stream().filter(field -> field.isAnnotationPresent(Value.class)).forEach(field -> {
            var value = field.getAnnotation(Value.class);
            encounter.register(new MembersInjector<Object>() {
                @Override
                public void injectMembers(Object instance) {
                    add(ConfigMapper.create(value, instance, field));
                    initValue(value, instance, field);
                }
            });
        });
    }

    /**
     * @param fields 当前类及继承类中所有的属性
     * @param cls 监听类
     * @return 监听类中的所有属性Field
     */
    private List<Field> fields(final List<Field> fields, final Class<?> cls) {
        fields.addAll(List.of(cls.getDeclaredFields()));
        var superCls = cls.getSuperclass();
        if (superCls == null) {
            return fields;
        }

        return fields(fields, superCls);
    }

    private void initValue(Value value, Object instance, Field field) {
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
