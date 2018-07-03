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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.nanoframework.toolkit.lang.ResourceUtils.extractJarFileURL;
import static org.nanoframework.toolkit.lang.ResourceUtils.getFile;
import static org.nanoframework.toolkit.lang.ResourceUtils.getURL;
import static org.nanoframework.toolkit.lang.ResourceUtils.isJarURL;
import static org.nanoframework.toolkit.lang.ResourceUtils.isUrl;
import static org.nanoframework.toolkit.lang.StringUtils.EMPTY;

import java.io.FileNotFoundException;
import java.net.URL;

import org.junit.jupiter.api.Test;

/**
 * @author yanghe
 * @since 2.0.0
 */
class ResourceUtilsTest {

    @Test
    void isUrlTest() {
        assertFalse(isUrl(null));
        assertFalse(isUrl(EMPTY));
        assertFalse(isUrl("classpath"));
        assertTrue(isUrl("classpath:"));
        assertTrue(isUrl("classpath:resources"));
        assertFalse(isUrl("nanoframework.org"));
        assertTrue(isUrl("http://nanoframework.org"));
    }

    @Test
    void getURLTest() {
        assertDoesNotThrow(() -> getURL("classpath"));
        assertDoesNotThrow(() -> getURL("classpath:test.properties"));
        assertThrows(FileNotFoundException.class, () -> getURL("classpath:notfound"));
        assertDoesNotThrow(() -> getURL("http://nanoframework.org"));
    }

    @Test
    void getFileTest() {
        assertDoesNotThrow(() -> getFile("classpath"));
        var file = assertDoesNotThrow(() -> getFile("classpath:test.properties"));
        assertNotNull(file);
        assertTrue(file.exists());
        assertThrows(FileNotFoundException.class, () -> getFile("classpath:notfound"));

        file = assertDoesNotThrow(
                () -> getFile(String.format("file://%s", getURL("src/test/resources/test.properties").getPath())));
        assertNotNull(file);
        assertTrue(file.exists());
    }

    @Test
    void getFileDescTest() {
        var url = assertDoesNotThrow(() -> getURL("src/test/resources/test.properties"));
        assertDoesNotThrow(() -> getFile(url, "message"));

        var uri = assertDoesNotThrow(() -> url.toURI());
        assertDoesNotThrow(() -> getFile(uri));

        var url2 = assertDoesNotThrow(() -> new URL("http://nanoframework.org"));
        assertThrows(FileNotFoundException.class, () -> getFile(url2, "message"));

        var uri2 = assertDoesNotThrow(() -> url2.toURI());
        assertThrows(FileNotFoundException.class, () -> getFile(uri2, "message"));
    }

    @Test
    void jarURLTest() {
        assertFalse(isJarURL(assertDoesNotThrow(() -> getURL(""))));
        var classpath = "file://" + assertDoesNotThrow(() -> getURL("src/test/resources/test.jar").getPath())
                + "!/org/nanoframework/toolkit/lang/ResourceUtils.class";
        var url = assertDoesNotThrow(() -> new URL(classpath));
        assertDoesNotThrow(() -> extractJarFileURL(url));

        var url2 = assertDoesNotThrow(
                () -> new URL("file://" + assertDoesNotThrow(() -> getURL("src/test/resources/test.jar").getPath())));
        assertEquals(assertDoesNotThrow(() -> extractJarFileURL(url2)), url2);
    }
}
