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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.nanoframework.toolkit.lang.ObjectUtils.toObjectArray;

import org.junit.jupiter.api.Test;

/**
 * @author yanghe
 * @since 2.0.0
 */
class ObjectUtilsTest {

    @Test
    void toObjectArrayTest() {
        var empty = toObjectArray(null);
        assertNotNull(empty);
        assertArrayEquals(empty, new Object[0]);

        Object objArray = new Object[] {1, 2, 3 };
        assertEquals(toObjectArray(objArray), objArray);

        assertThrows(IllegalArgumentException.class, () -> toObjectArray(new Object()));

        assertArrayEquals(toObjectArray(new int[0]), new Object[0]);
        assertArrayEquals(toObjectArray(new int[0]), new Integer[0]);

        assertArrayEquals(toObjectArray(new int[] {1, 2, 3 }), new Integer[] {1, 2, 3 });
    }

}
