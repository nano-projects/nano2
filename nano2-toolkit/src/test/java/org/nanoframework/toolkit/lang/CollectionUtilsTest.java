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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.nanoframework.toolkit.lang.CollectionUtils.isEmpty;
import static org.nanoframework.toolkit.lang.CollectionUtils.isNotEmpty;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * @author yanghe
 * @since 2.0.0
 */
class CollectionUtilsTest {

    @Test
    void isEmptyTest() {
        assertTrue(isEmpty((List<?>) null));
        assertTrue(isEmpty(List.of()));
        assertFalse(isEmpty(List.of(1)));

        assertFalse(isNotEmpty((List<?>) null));
        assertFalse(isNotEmpty(List.of()));
        assertTrue(isNotEmpty(List.of(1)));

        assertTrue(isEmpty((Map<?, ?>) null));
        assertTrue(isEmpty(Map.of()));
        assertFalse(isEmpty(Map.of("k", "v")));

        assertFalse(isNotEmpty((Map<?, ?>) null));
        assertFalse(isNotEmpty(Map.of()));
        assertTrue(isNotEmpty(Map.of("k", "v")));
    }

}
