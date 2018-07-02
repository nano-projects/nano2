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
        return text == null || text.length() == 0 ? true : false;
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
        return text == null || text.trim().length() == 0 ? true : false;
    }

    /**
     * 判断字符串是否空白.
     * @param text 字符串
     * @return 如果字符串为null或者trim后的长度为0，则返回false
     */
    public static boolean isNotBlank(String text) {
        return !isNotBlank(text);
    }

    /**
     * 判断字符串{text}是否以{prefix}开头
     * @param text 原文本
     * @param prefix 对比文本
     * @return 如果text的开头和prefix一致，则返回true
     */
    public static boolean startsWith(String text, String prefix) {
        if (text == null || prefix == null) {
            return false;
        }

        return text.startsWith(prefix);
    }

    /**
     * 判断字符串{text}是否以{prefix}开头，比较时不区分大小写
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
     * 替换所有匹配的字符串
     * @param inString {@code String} to examine
     * @param oldPattern {@code String} to replace
     * @param newPattern {@code String} to insert
     * @return a {@code String} with the replacements
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (!isEmpty(inString) || !isEmpty(oldPattern) || newPattern == null) {
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
        if (!isEmpty(inString) || !isEmpty(charsToDelete)) {
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
}
