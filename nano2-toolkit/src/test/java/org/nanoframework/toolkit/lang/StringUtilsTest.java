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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.nanoframework.toolkit.lang.StringUtils.EMPTY;
import static org.nanoframework.toolkit.lang.StringUtils.collectionToDelimitedString;
import static org.nanoframework.toolkit.lang.StringUtils.deleteAny;
import static org.nanoframework.toolkit.lang.StringUtils.delimitedListToStringArray;
import static org.nanoframework.toolkit.lang.StringUtils.isBlank;
import static org.nanoframework.toolkit.lang.StringUtils.isEmpty;
import static org.nanoframework.toolkit.lang.StringUtils.isNotBlank;
import static org.nanoframework.toolkit.lang.StringUtils.isNotEmpty;
import static org.nanoframework.toolkit.lang.StringUtils.replace;
import static org.nanoframework.toolkit.lang.StringUtils.startsWith;
import static org.nanoframework.toolkit.lang.StringUtils.startsWithIgnoreCase;
import static org.nanoframework.toolkit.lang.StringUtils.toStringArray;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @author yanghe
 * @since 2.0.0
 */
class StringUtilsTest {

    @Test
    void emptyTest() {
        assertTrue(isEmpty(null));
        assertTrue(isEmpty(""));
        assertFalse(isEmpty(" "));
        assertFalse(isEmpty("a"));
        assertFalse(isEmpty(" a"));
        assertFalse(isEmpty("a "));
        assertFalse(isEmpty(" a "));

        assertFalse(isNotEmpty(null));
        assertFalse(isNotEmpty(""));
        assertTrue(isNotEmpty(" "));
        assertTrue(isNotEmpty("a"));
        assertTrue(isNotEmpty(" a"));
        assertTrue(isNotEmpty("a "));
        assertTrue(isNotEmpty(" a "));
    }

    @Test
    void blankTest() {
        assertTrue(isBlank(null));
        assertTrue(isBlank(""));
        assertTrue(isBlank(" "));
        assertFalse(isBlank("a"));
        assertFalse(isBlank("a "));
        assertFalse(isBlank(" a"));
        assertFalse(isBlank(" a "));

        assertFalse(isNotBlank(null));
        assertFalse(isNotBlank(""));
        assertFalse(isNotBlank(" "));
        assertTrue(isNotBlank("a"));
        assertTrue(isNotBlank("a "));
        assertTrue(isNotBlank(" a"));
        assertTrue(isNotBlank(" a "));
    }

    @Test
    void equalsTest() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals("", null));
        assertFalse(StringUtils.equals(null, ""));
        assertFalse(StringUtils.equals("abc", "bcd"));
        assertFalse(StringUtils.equals("Abc", "abc"));
        assertTrue(StringUtils.equals("abc", "abc"));
    }

    @Test
    void startsWithTest() {
        assertFalse(startsWith(null, "a"));
        assertFalse(startsWith("abc", null));
        assertFalse(startsWith(null, null));
        assertFalse(startsWith("abc", "b"));
        assertFalse(startsWith("abc", "A"));
        assertFalse(startsWith("abc", "abcdef"));
        assertTrue(startsWith("abc", "a"));
    }

    @Test
    void startsWithIgnoreCaseTest() {
        assertFalse(startsWithIgnoreCase(null, "a"));
        assertFalse(startsWithIgnoreCase("abc", null));
        assertFalse(startsWithIgnoreCase(null, null));
        assertFalse(startsWithIgnoreCase("abc", "b"));
        assertFalse(startsWithIgnoreCase("abc", "abcdef"));
        assertTrue(startsWithIgnoreCase("abc", "A"));
        assertTrue(startsWithIgnoreCase("abc", "a"));
    }

    @Test
    void replaceTest() {
        var text = "String replace test";
        var newText = "String replaced test";
        var oldPattern = "replace";
        var newPattern = "replaced";

        assertEquals(replace(EMPTY, oldPattern, newPattern), EMPTY);
        assertEquals(replace(text, EMPTY, newPattern), text);
        assertEquals(replace(text, oldPattern, EMPTY), "String  test");

        assertEquals(replace(null, oldPattern, newPattern), null);
        assertEquals(replace(text, null, newPattern), text);
        assertEquals(replace(text, oldPattern, null), text);

        assertEquals(replace(text, oldPattern, newPattern), newText);
    }

    @Test
    void deleteAnyTest() {
        var text = "String delete any test";
        var delChars = "t";
        var delText = "Sring delee any es";

        assertEquals(deleteAny(null, delChars), null);
        assertEquals(deleteAny(text, null), text);
        assertEquals(deleteAny(EMPTY, delChars), EMPTY);
        assertEquals(deleteAny(text, EMPTY), text);
        assertEquals(deleteAny(text, delChars), delText);
    }

    @Test
    void delimitedListToStringArrayTest() {
        var emptyArray = new String[0];
        var text = "Delimited list to string array test";
        var textCharArray = new String[] {"D", "e", "l", "i", "m", "i", "t", "e", "d", " ", "l", "i", "s", "t", " ",
                "t", "o", " ", "s", "t", "r", "i", "n", "g", " ", "a", "r", "r", "a", "y", " ", "t", "e", "s", "t" };
        var textArray = new String[] {"Delimited", "list", "to", "string", "array", "test" };
        var textNoTArray = new String[] {"Delimied", "lis", "o", "sring", "array", "es" };

        assertArrayEquals(delimitedListToStringArray(null, EMPTY), emptyArray);
        assertArrayEquals(delimitedListToStringArray(EMPTY, " "), emptyArray);
        assertArrayEquals(delimitedListToStringArray(text, null), new String[] {text });
        assertArrayEquals(delimitedListToStringArray(text, EMPTY), textCharArray);
        assertArrayEquals(delimitedListToStringArray(text, " "), textArray);
        assertArrayEquals(delimitedListToStringArray(text, " ", "t"), textNoTArray);
    }

    @Test
    void toStringArrayTest() {
        assertNull(toStringArray(null));
        assertArrayEquals(toStringArray(List.of("a", "b", "c")), new String[] {"a", "b", "c" });
    }

    @Test
    void collectionToDelimitedStringTest() {
        var list = List.of("a", "b", "c", "d", "e");
        assertEquals(collectionToDelimitedString(Collections.emptyList(), EMPTY), EMPTY);
        assertEquals(collectionToDelimitedString(Collections.emptyList(), null), EMPTY);
        assertEquals(collectionToDelimitedString(list, EMPTY), "abcde");
        assertEquals(collectionToDelimitedString(list, ","), "a,b,c,d,e");
        assertEquals(collectionToDelimitedString(list, ",", "'", "'"), "'a','b','c','d','e'");
    }
}
