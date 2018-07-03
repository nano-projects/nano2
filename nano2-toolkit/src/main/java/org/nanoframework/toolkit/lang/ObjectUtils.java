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
}
