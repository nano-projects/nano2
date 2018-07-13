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
package org.nanoframework.beans;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 全局变量，针对一些全局的属性做统一管理.
 * @author yanghe
 * @since 2.0.0
 */
public final class Globals {
    private static final ConcurrentMap<Class<?>, Object> GLOBALS = new ConcurrentHashMap<>();

    private Globals() {

    }

    /**
     * 设置对象单例映射.
     * @param clz Type
     * @param global Instance
     */
    public static void set(Class<?> clz, Object global) {
        GLOBALS.put(clz, global);
    }

    /**
     * @param clz Type
     * @param <T> 泛型类型
     * @return 获取单例对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clz) {
        return (T) GLOBALS.get(clz);
    }

    /**
     * @param cls Type
     * @param <T> 泛型类型
     * @return 移除单例对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T remove(Class<?> cls) {
        return (T) GLOBALS.remove(cls);
    }
}
