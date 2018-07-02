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

/**
 * @author yanghe
 * @since 2.0.0
 */
public final class ArrayUtils {

    private ArrayUtils() {

    }

    /**
     * 判断数组是否为空.
     * @param el 数组
     * @param <E> 数组对象类型
     * @return 如果数组为null或者长度为0，则返回true
     */
    public static <E> boolean isEmpty(E[] el) {
        return el == null || el.length == 0 ? true : false;
    }

    /**
     * 判断数组是否为空.
     * @param el 数组
     * @param <E> 数组对象类型
     * @return 如果数组为null或者长度为0，则返回false
     */
    public static <E> boolean isNotEmpty(E[] el) {
        return !isEmpty(el);
    }
}
