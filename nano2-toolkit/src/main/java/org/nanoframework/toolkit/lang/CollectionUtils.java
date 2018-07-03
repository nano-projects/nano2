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
package org.nanoframework.toolkit.lang;

import java.util.Collection;
import java.util.Map;

/**
 * @author yanghe
 * @since 2.0.0
 */
public final class CollectionUtils {

    private CollectionUtils() {

    }

    /**
     * 判断是否为空集合
     * @param collection 集合对象
     * @param <E> 集合对象类型
     * @return 如果集合为null或者empty，则返回true
     */
    public static <E> boolean isEmpty(Collection<E> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断是否为空集合
     * @param collection 集合对象
     * @param <E> 集合对象类型
     * @return 如果集合为null或者empty，则返回false
     */
    public static <E> boolean isNotEmpty(Collection<E> collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断是否为空集合
     * @param map 集合对象
     * @param <K> 集合键类型
     * @param <V> 集合值类型
     * @return 如果集合为null或者empty，则返回true
     */
    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断是否为空集合
     * @param map 集合对象
     * @param <K> 集合键类型
     * @param <V> 集合值类型
     * @return 如果集合为null或者empty，则返回false
     */
    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }
}
