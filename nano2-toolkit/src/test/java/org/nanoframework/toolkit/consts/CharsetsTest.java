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
package org.nanoframework.toolkit.consts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

/**
 * @author yanghe
 * @since 2.0.0
 */
class CharsetsTest {

    @Test
    void test() {
        Charset utf8 = Charsets.getSupportedCharset("UTF-8");
        assertEquals(Charsets.UTF_8, utf8);

        Charset def = Charsets.getSupportedCharset("abc");
        assertEquals(Charset.defaultCharset(), def);
    }

}
