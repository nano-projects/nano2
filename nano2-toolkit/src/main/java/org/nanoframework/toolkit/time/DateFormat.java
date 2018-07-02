/*
 * Copyright 2015-2016 the original author or authors.
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
package org.nanoframework.toolkit.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author yanghe
 * @since 1.0
 */
public final class DateFormat {
    private static final ConcurrentMap<String, ThreadLocal<SimpleDateFormat>> FORMAT_MAP = new ConcurrentHashMap<>();

    private DateFormat() {

    }

    /**
     * 获取时间转换对象.
     * @param pattern 时间格式
     * @return 时间转换对象
     */
    public static SimpleDateFormat get(String pattern) {
        SimpleDateFormat format;
        var formatLocal = FORMAT_MAP.get(pattern);
        if (formatLocal == null) {
            formatLocal = new ThreadLocal<>();
            format = new SimpleDateFormat(pattern);
            formatLocal.set(format);
            FORMAT_MAP.put(pattern, formatLocal);
        }

        format = formatLocal.get();
        if (format == null) {
            format = new SimpleDateFormat(pattern);
            formatLocal.set(format);
        }

        return format;
    }

    /**
     * 根据时间格式枚举获取时间转换对象.
     * @param pattern 时间格式枚举
     * @return 时间转换对象
     */
    public static SimpleDateFormat get(Pattern pattern) {
        return get(pattern.get());
    }

    /**
     * 将时间对象转成时间格式的字符串.
     * @param date 时间对象
     * @param pattern 时间格式枚举
     * @return 时间型字符串
     */
    public static String format(Date date, Pattern pattern) {
        return get(pattern).format(date);
    }

    /**
     * 将时间对象转成时间格式的字符串.
     * @param date 时间对象
     * @param pattern 时间格式
     * @return 时间型字符串
     */
    public static String format(Date date, String pattern) {
        return get(pattern).format(date);
    }

    /**
     * 将时间对象转成时间格式的字符串.
     * @param date 时间类型的对象
     * @param pattern 时间格式枚举
     * @return 时间型字符串
     */
    public static String format(Object date, Pattern pattern) {
        return get(pattern).format(date);
    }

    /**
     * 将时间对象转成时间格式的字符串.
     * @param date 时间类型的对象
     * @param pattern 时间格式字符串
     * @return 时间型字符串
     */
    public static String format(Object date, String pattern) {
        return get(pattern).format(date);
    }

    /**
     * 将时间型字符串转成时间对象.
     * @param <T> 参数类型
     * @param date 时间型字符串
     * @param pattern 时间格式枚举
     * @return 时间对象
     * @throws ParseException 时间转换异常
     */
    @SuppressWarnings("unchecked")
    public static <T extends Date> T parse(String date, Pattern pattern) throws ParseException {
        return (T) get(pattern).parse(date);
    }

    /**
     * 将时间型字符串转成时间对象.
     * @param <T> 参数类型
     * @param date 时间型字符串
     * @param pattern 时间格式字符串
     * @return 时间对象
     * @throws ParseException 时间转换异常
     */
    @SuppressWarnings("unchecked")
    public static <T extends Date> T parse(String date, String pattern) throws ParseException {
        return (T) get(pattern).parse(date);
    }
}
