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
package org.nanoframework.toolkit.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.nanoframework.toolkit.properties.exception.LoaderException;

/**
 * @author yanghe
 * @since 2.0.0
 */
class PropertiesLoaderTest {

    @Test
    void loadTest() {
        var p = PropertiesLoader.load("/test.properties");
        assertEquals(p.getProperty("message"), "Hello World");

        var p1 = PropertiesLoader.load("classpath:test.properties");
        assertEquals(p1.getProperty("message"), "Hello World");
    }

    @Test
    void loadExceptionTest() {
        assertThrows(LoaderException.class, () -> PropertiesLoader.load("/test1.properties"));
    }

    @Test
    void loadWithInputNullTest() {
        assertThrows(NullPointerException.class, () -> PropertiesLoader.load((InputStream) null));
    }

    @Test
    void loadContextTest() {
        PropertiesLoader.loadContext("/context.properties");
        var p = PropertiesLoader.PROPERTIES.get("/test.properties");
        assertNotNull(p);
        assertEquals(p.getProperty("message"), "Hello World");
    }
}
