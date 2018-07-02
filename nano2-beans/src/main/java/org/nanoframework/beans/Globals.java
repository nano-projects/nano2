/*
 * Copyright 2015-2016 the original author or authors.
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
package org.nanoframework.beans;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

/**
 * 全局变量，针对一些全局的属性做统一管理.
 * @author yanghe
 * @since 1.0
 */
public final class Globals {
    private static final ConcurrentMap<Class<?>, Object> GLOBALS = Maps.newConcurrentMap();

    private Globals() {

    }

    public static void set(final Class<?> clz, final Object global) {
        GLOBALS.put(clz, global);
    }

    @SuppressWarnings("unchecked")
    public static final <T> T get(final Class<T> clz) {
        return (T) GLOBALS.get(clz);
    }

}
