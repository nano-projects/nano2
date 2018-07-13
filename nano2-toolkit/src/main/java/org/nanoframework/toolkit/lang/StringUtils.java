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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * @author yanghe
 * @since 2.0.0
 */
public final class StringUtils {
    /**
     * 空字符串.
     */
    public static final String EMPTY = "";

    private StringUtils() {

    }

    /**
     * 判断字符串是否为空.
     * @param text 字符串
     * @return 如果字符串为null或者长度为0，则返回true
     */
    public static boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }

    /**
     * 判断字符串是否为空.
     * @param text 字符串
     * @return 如果字符串为null或者长度为0，则返回false
     */
    public static boolean isNotEmpty(String text) {
        return !isEmpty(text);
    }

    /**
     * 判断字符串是否空白.
     * @param text 字符串
     * @return 如果字符串为null或者trim后的长度为0，则返回true
     */
    public static boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

    /**
     * 判断字符串是否空白.
     * @param text 字符串
     * @return 如果字符串为null或者trim后的长度为0，则返回false
     */
    public static boolean isNotBlank(String text) {
        return !isBlank(text);
    }

    /**
     * 判断字符串是否有字符.
     * @param text 字符串
     * @return 如果字符串有字符，则返回true
     */
    public static boolean hasLength(CharSequence text) {
        return (text != null && text.length() > 0);
    }

    /**
     * 判断字符串是否有字符.
     * @param text 字符串
     * @return 如果字符串有字符，则返回true
     */
    public static boolean hasLength(String text) {
        return hasLength((CharSequence) text);
    }

    /**
     * 判断字符串是否有字符，且不是空格.
     * @param text 字符串
     * @return 如果字符串有字符且不是空格，则返回true
     */
    public static boolean hasText(CharSequence text) {
        if (!hasLength(text)) {
            return false;
        }

        var len = text.length();
        for (var i = 0; i < len; i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断字符串是否有字符，且不是空格.
     * @param text 字符串
     * @return 如果字符串有字符且不是空格，则返回true
     */
    public static boolean hasText(String text) {
        return hasText((CharSequence) text);
    }

    /**
     * 判断2个字符串equals.
     * @param text1 字符串1
     * @param text2 字符串2
     * @return 如果字符串1 不等于 字符串2，则返回true
     */
    public static boolean notEquals(String text1, String text2) {
        return !equals(text1, text2);
    }

    /**
     * 判断2个字符串equals.
     * @param text1 字符串1
     * @param text2 字符串2
     * @return 如果字符串1 equals 字符串2
     */
    public static boolean equals(String text1, String text2) {
        if (text1 == null && text2 == null) {
            return true;
        } else if (text1 == null || text2 == null) {
            return false;
        }

        return text1.equals(text2);
    }

    /**
     * 判断字符串{text}是否以{prefix}开头.
     * @param text 原文本
     * @param prefix 对比文本
     * @return 如果text的开头和prefix一致，则返回true
     */
    public static boolean startsWith(String text, String prefix) {
        if (text == null || prefix == null) {
            return false;
        }

        if (text.length() < prefix.length()) {
            return false;
        }

        return text.startsWith(prefix);
    }

    /**
     * 判断字符串{text}是否以{prefix}开头，比较时不区分大小写.
     * @param text 原文本
     * @param prefix 对比文本
     * @return 如果text的开头和prefix一致，则返回true
     */
    public static boolean startsWithIgnoreCase(String text, String prefix) {
        if (text == null || prefix == null) {
            return false;
        }

        if (text.length() < prefix.length()) {
            return false;
        }

        return text.substring(0, prefix.length()).toLowerCase().equals(prefix.toLowerCase());
    }

    /**
     * 替换所有匹配的字符串.
     * @param inString {@code String} to examine
     * @param oldPattern {@code String} to replace
     * @param newPattern {@code String} to insert
     * @return a {@code String} with the replacements
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (isEmpty(inString) || isEmpty(oldPattern) || newPattern == null) {
            return inString;
        }

        var builder = new StringBuilder();
        var pos = 0;
        var index = inString.indexOf(oldPattern);
        var patLen = oldPattern.length();
        while (index >= 0) {
            builder.append(inString.substring(pos, index));
            builder.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }

        builder.append(inString.substring(pos));
        return builder.toString();
    }

    /**
     * 字符串小写化.
     * @param text 字符串文本
     * @return 小写后的字符串文本
     */
    public static String lowerCase(String text) {
        if (isEmpty(text)) {
            return text;
        }

        return text.toLowerCase();
    }

    // Joining
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No separator is added to the joined String. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null)            = null
     * StringUtils.join([])              = ""
     * StringUtils.join([null])          = ""
     * StringUtils.join(["a", "b", "c"]) = "abc"
     * StringUtils.join([null, "", "a"]) = "a"
     * </pre>
     *
     * @param <T> the specific type of values to join together
     * @param elements the values to join together, may be null
     * @return the joined String, {@code null} if null array input
     * @since 2.0
     * @since 3.0 Changed signature to use varargs
     */
    @SuppressWarnings("unchecked")
    public static <T> String join(final T... elements) {
        return join(elements, null);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
     * StringUtils.join(["a", "b", "c"], null) = "abc"
     * StringUtils.join([null, "", "a"], ';')  = ";;a"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null array input
     * @since 2.0
     */
    public static String join(final Object[] array, final char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final long[] array, final char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final int[] array, final char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final short[] array, final char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final byte[] array, final char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final char[] array, final char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final float[] array, final char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final double[] array, final char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
     * StringUtils.join(["a", "b", "c"], null) = "abc"
     * StringUtils.join([null, "", "a"], ';')  = ";;a"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @param startIndex the first index to start joining from. It is an error to pass in an end index past the end of
     *            the array
     * @param endIndex the index to stop joining from (exclusive). It is an error to pass in an end index past the end
     *            of the array
     * @return the joined String, {@code null} if null array input
     * @since 2.0
     */
    public static String join(final Object[] array, final char separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        var noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        var buf = new StringBuilder(noOfItems * 16);
        for (var i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @param startIndex the first index to start joining from. It is an error to pass in an end index past the end of
     *            the array
     * @param endIndex the index to stop joining from (exclusive). It is an error to pass in an end index past the end
     *            of the array
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final long[] array, final char separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        var noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        var buf = new StringBuilder(noOfItems * 16);
        for (var i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @param startIndex the first index to start joining from. It is an error to pass in an end index past the end of
     *            the array
     * @param endIndex the index to stop joining from (exclusive). It is an error to pass in an end index past the end
     *            of the array
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final int[] array, final char separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        var noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        var buf = new StringBuilder(noOfItems * 16);
        for (var i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @param startIndex the first index to start joining from. It is an error to pass in an end index past the end of
     *            the array
     * @param endIndex the index to stop joining from (exclusive). It is an error to pass in an end index past the end
     *            of the array
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final byte[] array, final char separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        var noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        var buf = new StringBuilder(noOfItems * 16);
        for (var i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @param startIndex the first index to start joining from. It is an error to pass in an end index past the end of
     *            the array
     * @param endIndex the index to stop joining from (exclusive). It is an error to pass in an end index past the end
     *            of the array
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final short[] array, final char separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        var noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        var buf = new StringBuilder(noOfItems * 16);
        for (var i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @param startIndex the first index to start joining from. It is an error to pass in an end index past the end of
     *            the array
     * @param endIndex the index to stop joining from (exclusive). It is an error to pass in an end index past the end
     *            of the array
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final char[] array, final char separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        var noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        var buf = new StringBuilder(noOfItems * 16);
        for (var i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @param startIndex the first index to start joining from. It is an error to pass in an end index past the end of
     *            the array
     * @param endIndex the index to stop joining from (exclusive). It is an error to pass in an end index past the end
     *            of the array
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final double[] array, final char separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        var noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        var buf = new StringBuilder(noOfItems * 16);
        for (var i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by
     * empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtils.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use
     * @param startIndex the first index to start joining from. It is an error to pass in an end index past the end of
     *            the array
     * @param endIndex the index to stop joining from (exclusive). It is an error to pass in an end index past the end
     *            of the array
     * @return the joined String, {@code null} if null array input
     * @since 3.2
     */
    public static String join(final float[] array, final char separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        var noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        var buf = new StringBuilder(noOfItems * 16);
        for (var i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. A {@code null} separator is the same as an empty String ("").
     * Null objects or empty strings within the array are represented by empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *)                = null
     * StringUtils.join([], *)                  = ""
     * StringUtils.join([null], *)              = ""
     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtils.join(["a", "b", "c"], null)  = "abc"
     * StringUtils.join(["a", "b", "c"], "")    = "abc"
     * StringUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, {@code null} if null array input
     */
    public static String join(final Object[] array, final String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. A {@code null} separator is the same as an empty String ("").
     * Null objects or empty strings within the array are represented by empty strings.
     * </p>
     *
     * <pre>
     * StringUtils.join(null, *, *, *)                = null
     * StringUtils.join([], *, *, *)                  = ""
     * StringUtils.join([null], *, *, *)              = ""
     * StringUtils.join(["a", "b", "c"], "--", 0, 3)  = "a--b--c"
     * StringUtils.join(["a", "b", "c"], "--", 1, 3)  = "b--c"
     * StringUtils.join(["a", "b", "c"], "--", 2, 3)  = "c"
     * StringUtils.join(["a", "b", "c"], "--", 2, 2)  = ""
     * StringUtils.join(["a", "b", "c"], null, 0, 3)  = "abc"
     * StringUtils.join(["a", "b", "c"], "", 0, 3)    = "abc"
     * StringUtils.join([null, "", "a"], ',', 0, 3)   = ",,a"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @param separator the separator character to use, null treated as ""
     * @param startIndex the first index to start joining from.
     * @param endIndex the index to stop joining from (exclusive).
     * @return the joined String, {@code null} if null array input; or the empty string if
     *         {@code endIndex - startIndex <= 0}. The number of joined entries is given by
     *         {@code endIndex - startIndex}
     * @throws ArrayIndexOutOfBoundsException ife<br>
     *             {@code startIndex < 0} or <br>
     *             {@code startIndex >= array.length()} or <br>
     *             {@code endIndex < 0} or <br>
     *             {@code endIndex > array.length()}
     */
    public static String join(final Object[] array, String separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = EMPTY;
        }

        // endIndex - startIndex > 0: Len = NofStrings *(len(firstString) + len(separator))
        // (Assuming that all Strings are roughly equally long)
        var noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }

        var buf = new StringBuilder(noOfItems * 16);
        for (var i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided {@code Iterator} into a single String containing the provided elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the iteration are
     * represented by empty strings.
     * </p>
     * <p>
     * See the examples here: {@link #join(Object[],char)}.
     * </p>
     * @param iterator the {@code Iterator} of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null iterator input
     * @since 2.0
     */
    public static String join(final Iterator<?> iterator, final char separator) {
        // handle null, zero and one elements before building a buffer
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return EMPTY;
        }
        var first = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(first);
        }

        // two or more elements
        var buf = new StringBuilder(256); // Java default is 16, probably too small
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            buf.append(separator);
            var obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }

        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided {@code Iterator} into a single String containing the provided elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. A {@code null} separator is the same as an empty String ("").
     * </p>
     * <p>
     * See the examples here: {@link #join(Object[],String)}.
     * </p>
     * @param iterator the {@code Iterator} of values to join together, may be null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, {@code null} if null iterator input
     */
    public static String join(final Iterator<?> iterator, final String separator) {
        // handle null, zero and one elements before building a buffer
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return EMPTY;
        }
        var first = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(first);
        }

        // two or more elements
        var buf = new StringBuilder(256); // Java default is 16, probably too small
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            var obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided {@code Iterable} into a single String containing the provided elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the iteration are
     * represented by empty strings.
     * </p>
     * <p>
     * See the examples here: {@link #join(Object[],char)}.
     * </p>
     * @param iterable the {@code Iterable} providing the values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null iterator input
     * @since 2.3
     */
    public static String join(final Iterable<?> iterable, final char separator) {
        if (iterable == null) {
            return null;
        }
        return join(iterable.iterator(), separator);
    }

    /**
     * <p>
     * Joins the elements of the provided {@code Iterable} into a single String containing the provided elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. A {@code null} separator is the same as an empty String ("").
     * </p>
     * <p>
     * See the examples here: {@link #join(Object[],String)}.
     * </p>
     * @param iterable the {@code Iterable} providing the values to join together, may be null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, {@code null} if null iterator input
     * @since 2.3
     */
    public static String join(final Iterable<?> iterable, final String separator) {
        if (iterable == null) {
            return null;
        }
        return join(iterable.iterator(), separator);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into a {@code String} array.
     * @param str the input {@code String}
     * @param delimiter the delimiter between elements (this is a single delimiter, rather than a bunch individual
     *            delimiter characters)
     * @return an array of the tokens in the list
     */
    public static String[] delimitedListToStringArray(String str, String delimiter) {
        return delimitedListToStringArray(str, delimiter, null);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into a {@code String} array.
     * @param str the input {@code String}
     * @param delimiter the delimiter between elements (this is a single delimiter, rather than a bunch individual
     *            delimiter characters)
     * @param charsToDelete a set of characters to delete; useful for deleting unwanted line breaks: e.g. "\r\n\f" will
     *            delete all new lines and line feeds in a {@code String}
     * @return an array of the tokens in the list
     */
    public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
        if (str == null) {
            return new String[0];
        }

        if (delimiter == null) {
            return new String[] {str };
        }

        var result = new ArrayList<String>();
        if (EMPTY.equals(delimiter)) {
            for (var i = 0; i < str.length(); i++) {
                result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
            }
        } else {
            var pos = 0;
            int delPos;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                pos = delPos + delimiter.length();
            }

            if (str.length() > 0 && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(deleteAny(str.substring(pos), charsToDelete));
            }
        }

        return toStringArray(result);
    }

    /**
     * Delete any character in a given {@code String}.
     * @param inString the original {@code String}
     * @param charsToDelete a set of characters to delete. E.g. "az\n" will delete 'a's, 'z's and new lines.
     * @return the resulting {@code String}
     */
    public static String deleteAny(String inString, String charsToDelete) {
        if (isEmpty(inString) || isEmpty(charsToDelete)) {
            return inString;
        }

        var builder = new StringBuilder();
        for (var i = 0; i < inString.length(); i++) {
            var c = inString.charAt(i);
            if (charsToDelete.indexOf(c) == -1) {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    /**
     * Copy the given {@code Collection} into a {@code String} array.
     * <p>
     * The {@code Collection} must contain {@code String} elements only.
     * @param collection the {@code Collection} to copy
     * @return the {@code String} array ({@code null} if the supplied {@code Collection} was {@code null})
     */
    public static String[] toStringArray(Collection<String> collection) {
        if (collection == null) {
            return null;
        }

        return collection.toArray(new String[collection.size()]);
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g. CSV).
     * <p>
     * Useful for {@code toString()} implementations.
     * @param coll the {@code Collection} to convert
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim) {
        return collectionToDelimitedString(coll, delim, EMPTY, EMPTY);
    }

    /**
     * Convert a {@link Collection} to a delimited {@code String} (e.g. CSV).
     * <p>
     * Useful for {@code toString()} implementations.
     * @param coll the {@code Collection} to convert
     * @param delim the delimiter to use (typically a ",")
     * @param prefix the {@code String} to start each element with
     * @param suffix the {@code String} to end each element with
     * @return the delimited {@code String}
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
        if (CollectionUtils.isEmpty(coll)) {
            return EMPTY;
        }

        var builder = new StringBuilder();
        var iter = coll.iterator();
        while (iter.hasNext()) {
            builder.append(prefix).append(iter.next()).append(suffix);
            if (iter.hasNext()) {
                builder.append(delim);
            }
        }

        return builder.toString();
    }

    /**
     * Parse the given {@code localeString} value into a Locale.
     * <p>
     * This is the inverse operation of Locale#toString Locale's toString.
     * @param str the locale {@code String}, following {@code Locale's}
     * @param delimiters the delimiters
     * @param trimTokens the trimTokens
     * @param ignoreEmptyTokens the ignoreEmptyTokens {@code toString()} format ("en", "en_UK", etc); also accepts
     *            spaces as separators, as an alternative to underscores
     * @return a corresponding {@code Locale} instance
     * @throws IllegalArgumentException in case of an invalid locale specification
     */
    public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
            boolean ignoreEmptyTokens) {

        if (str == null) {
            return null;
        }

        var st = new StringTokenizer(str, delimiters);
        var tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            var token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }

            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }

        return toStringArray(tokens);
    }
}
