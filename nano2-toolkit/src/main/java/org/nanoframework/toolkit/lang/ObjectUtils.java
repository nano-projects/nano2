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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author yanghe
 * @since 2.0.0
 */
public final class ObjectUtils {

    private ObjectUtils() {

    }

    /**
     * Convert the given array (which may be a primitive array) to an object array (if necessary of primitive wrapper
     * objects).
     * <p>
     * A {@code null} source value will be converted to an empty Object array.
     * @param source the (potentially primitive) array
     * @return the corresponding object array (never {@code null})
     * @throws IllegalArgumentException if the parameter is not an array
     */
    public static Object[] toObjectArray(Object source) {
        if (source == null) {
            return new Object[0];
        }

        if (source instanceof Object[]) {
            return (Object[]) source;
        }

        if (!source.getClass().isArray()) {
            throw new IllegalArgumentException("Source is not an array: " + source);
        }

        var length = Array.getLength(source);
        if (length == 0) {
            return new Object[0];
        }

        var wrapperType = Array.get(source, 0).getClass();
        var newArray = (Object[]) Array.newInstance(wrapperType, length);
        for (int i = 0; i < length; i++) {
            newArray[i] = Array.get(source, i);
        }

        return newArray;
    }

    public static long isNull(Object... objs) {
        return Arrays.stream(objs).filter(obj -> obj == null).count();
    }

    public static long isNotNull(Object... objs) {
        return Arrays.stream(objs).filter(obj -> obj != null).count();
    }

    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }

        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }

        if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        }

        if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        }

        // else
        return false;
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 判断目标值是否存在与源列表中
     * @param target 源
     * @param source 目标
     * @return 返回是否存在结果 true=存在，false=不存在
     */
    public static final boolean isInList(Object target, Object... source) {
        if (target == null) {
            return false;
        }

        if (source != null && source.length > 0) {
            for (var src : source) {
                if (target.equals(src)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断目标值是否存在与源列表中
     * @param target 源
     * @param source 目标
     * @return 返回是否存在结果 true=存在，false=不存在
     */
    public static final boolean isInList(Object target, String... source) {
        if (target == null) {
            return false;
        }

        if (source != null && source.length > 0) {
            for (var src : source) {
                if (StringUtils.isEmpty(src)) {
                    return false;
                }

                if (target.equals(src.trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 正则表达式比较target是否在regExs内
     * @param target 源
     * @param regExs 正则列表
     * @return 返回是否存在结果 true=存在，false=不存在
     */
    public static final boolean isInListByRegEx(String target, String... regExs) {
        if (StringUtils.isBlank(target)) {
            return false;
        }

        if (regExs != null && regExs.length > 0) {
            for (var regEx : regExs) {
                if (StringUtils.isBlank(regEx)) {
                    continue;
                }

                if (Pattern.compile(regEx).matcher(target).find()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static final boolean isInListByRegEx(String target, Set<String> regExs) {
        if (CollectionUtils.isEmpty(regExs)) {
            return false;
        }

        return isInListByRegEx(target, regExs.toArray(new String[regExs.size()]));
    }

    public static final boolean isInEndWiths(String target, String... source) {
        if (target == null) {
            return false;
        }

        if (source != null && source.length > 0) {
            for (var suffix : source) {
                if (target.endsWith(suffix)) {
                    return true;
                }
            }
        }

        return false;
    }
}
