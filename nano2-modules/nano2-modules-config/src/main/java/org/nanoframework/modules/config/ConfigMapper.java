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
package org.nanoframework.modules.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.nanoframework.modules.config.annotation.Value;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Getter
@RequiredArgsConstructor
public class ConfigMapper {
    private final String key;

    private final String namespace;

    private final boolean required;

    private final Object instance;

    private final Method method;

    private final Field field;

    public static ConfigMapper create(@NonNull Value value, @NonNull Object instance, @NonNull Method method) {
        var count = method.getParameterCount();
        if (count != 1) {
            throw new IllegalArgumentException(String.format("无效的参数列表长度: %s", method.getName()));
        }

        return new ConfigMapper(value.value(), value.namespace(), value.required(), instance, method, null);
    }

    public static ConfigMapper create(@NonNull Value value, @NonNull Object instance, @NonNull Field field) {
        return new ConfigMapper(value.value(), value.namespace(), value.required(), instance, null, field);
    }
}
