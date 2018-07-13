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
package org.nanoframework.beans.format;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.nanoframework.toolkit.lang.ObjectUtils;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.time.DateFormat;
import org.nanoframework.toolkit.time.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * 类型转换处理类.
 * @author yanghe
 * @since 2.0.0
 */
public final class ClassCast {
    private static final String INTEGER_OBJ = "java.lang.Integer";

    private static final String L_INTEGER_OBJ = "[Ljava.lang.Integer;";

    private static final String INT = "int";

    private static final String LONG_OBJ = "java.lang.Long";

    private static final String L_LONG_OBJ = "[Ljava.lang.Long;";

    private static final String LONG = "long";

    private static final String DOUBLE_OBJ = "java.lang.Double";

    private static final String L_DOUBLE_OBJ = "[Ljava.lang.Double;";

    private static final String DOUBLE = "double";

    private static final String FLOAT_OBJ = "java.lang.Float";

    private static final String L_FLOAT_OBJ = "[Ljava.lang.Float;";

    private static final String FLOAT = "float";

    private static final String STRING = "java.lang.String";

    private static final String DATE_UTIL = "java.util.Date";

    private static final String DATE_SQL = "java.sql.Date";

    private static final String TIMESTAMP = "java.sql.Timestamp";

    private static final String BOOLEAN_OBJ = "java.lang.Boolean";

    private static final String BOOLEAN = "boolean";

    private static final String L_BOOLEAN_OBJ = "[Ljava.lang.Boolean;";

    private static final List<String> BOOLEAN_TRUE_VALUES = List.of("1", "YES", "Y", "TRUE");

    private static final List<String> BOOLEAN_FALSE_VALUES = List.of("0", "NO", "N", "FALSE");

    private ClassCast() {

    }

    /**
     * 根据Class进行转换，转换简单数据类型.
     * @param value 值
     * @param typeName 类型
     * @return 返回转换后的值
     */
    public static Object cast(String value, String typeName) {
        if (value == null) {
            return null;
        }

        try {
            switch (typeName) {
                case INTEGER_OBJ:
                case INT:
                    return Integer.valueOf(value);

                case LONG_OBJ:
                case LONG:
                    return Long.valueOf(value);

                case DOUBLE_OBJ:
                case DOUBLE:
                    return Double.valueOf(value);

                case FLOAT_OBJ:
                case FLOAT:
                    return Float.valueOf(value);

                case BOOLEAN_OBJ:
                case BOOLEAN:
                    if (BOOLEAN_TRUE_VALUES.contains(value.toUpperCase())) {
                        return Boolean.TRUE;
                    } else if (BOOLEAN_FALSE_VALUES.contains(value.toUpperCase())) {
                        return Boolean.FALSE;
                    } else {
                        return Boolean.valueOf(value);
                    }

                case STRING:
                    return value;

                case DATE_UTIL:
                case DATE_SQL:
                    return DateFormat.parse(value, Pattern.DATETIME);

                case TIMESTAMP:
                    return new Timestamp(DateFormat.parse(value, Pattern.TIMESTAMP).getTime());

                default:
                    return JSON.parseObject(value, Class.forName(typeName));

            }
        } catch (ClassCastException | ClassNotFoundException | ParseException e) {
            throw new org.nanoframework.beans.format.exception.ClassCastException(e.getMessage(), e);

        }

    }

    /**
     * 根据Class进行转换，转换简单数据类型.
     * @param value 值
     * @param typeName 类型
     * @return 返回转换后的值
     */
    public static Object cast(Object value, String typeName) {
        if (value == null) {
            return null;
        }

        if (StringUtils.isEmpty(typeName)) {
            throw new IllegalArgumentException("类型名不能为空");
        }

        try {
            switch (typeName) {
                case INTEGER_OBJ:
                    if (value instanceof Integer) {
                        return value;
                    } else if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).intValue();
                    } else {
                        return new BigDecimal(String.valueOf(value)).intValue();
                    }
                case L_INTEGER_OBJ:
                    var values = ObjectUtils.toObjectArray(value);
                    var ints = new Integer[values.length];
                    for (var idx = 0; idx < ints.length; idx++) {
                        if (values[idx] == null || "".equals(values[idx])) {
                            ints[idx] = null;
                        } else {
                            ints[idx] = new BigDecimal(String.valueOf(values[idx])).intValue();
                        }
                    }

                    return ints;
                case LONG_OBJ:
                    if (value instanceof Long) {
                        return value;
                    } else if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).longValue();
                    } else {
                        return new BigDecimal(String.valueOf(value)).longValue();
                    }
                case L_LONG_OBJ:
                    values = ObjectUtils.toObjectArray(value);
                    var longs = new Long[values.length];
                    for (var idx = 0; idx < longs.length; idx++) {
                        if (values[idx] == null || "".equals(values[idx])) {
                            longs[idx] = null;
                        } else {
                            longs[idx] = new BigDecimal(String.valueOf(values[idx])).longValue();
                        }
                    }

                    return longs;
                case DOUBLE_OBJ:
                    if (value instanceof Double) {
                        return value;
                    } else if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).doubleValue();
                    } else {
                        return new BigDecimal(String.valueOf(value)).doubleValue();
                    }
                case L_DOUBLE_OBJ:
                    values = ObjectUtils.toObjectArray(value);
                    var doubles = new Double[values.length];
                    for (int idx = 0; idx < doubles.length; idx++) {
                        if (values[idx] == null || "".equals(values[idx])) {
                            doubles[idx] = null;
                        } else {
                            doubles[idx] = new BigDecimal(String.valueOf(values[idx])).doubleValue();
                        }
                    }

                    return doubles;
                case FLOAT_OBJ:
                    if (value instanceof Float) {
                        return value;
                    } else if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).floatValue();
                    } else {
                        return new BigDecimal(String.valueOf(value)).floatValue();
                    }
                case L_FLOAT_OBJ:
                    values = ObjectUtils.toObjectArray(value);
                    var floats = new Float[values.length];
                    for (var idx = 0; idx < floats.length; idx++) {
                        if (values[idx] == null || "".equals(values[idx])) {
                            floats[idx] = null;
                        } else {
                            floats[idx] = new BigDecimal(String.valueOf(values[idx])).floatValue();
                        }
                    }

                    return floats;
                case BOOLEAN_OBJ:
                    if (value instanceof Boolean) {
                        return value;
                    } else {
                        return Boolean.valueOf(String.valueOf(value));
                    }
                case L_BOOLEAN_OBJ:
                    values = ObjectUtils.toObjectArray(value);
                    var booleans = new Boolean[values.length];
                    for (var idx = 0; idx < booleans.length; idx++) {
                        if (values[idx] == null || "".equals(values[idx])) {
                            booleans[idx] = null;
                        } else {
                            booleans[idx] = Boolean.valueOf(String.valueOf(values[idx]));
                        }
                    }

                    return booleans;
                case DATE_UTIL:
                case DATE_SQL:
                    if (value instanceof Date) {
                        return value;
                    } else {
                        return parseDate(String.valueOf(value));
                    }
                case TIMESTAMP:
                    if (value instanceof Date) {
                        return new Timestamp(((Date) value).getTime());
                    } else {
                        return new Timestamp(parseDate(String.valueOf(value)).getTime());
                    }
                case INT:
                case LONG:
                case DOUBLE:
                case FLOAT:
                    throw new ClassCastException("只支持对对象数据类型的转换，不支持基本数据类型的转换");

                default:
                    return castDefault(value, typeName);
            }
        } catch (Throwable e) {
            throw new org.nanoframework.beans.format.exception.ClassCastException(e.getMessage(), e);
        }
    }

    private static Object castDefault(Object value, String typeName) throws ClassNotFoundException {
        String newType;
        if (typeName.startsWith("[L") && typeName.endsWith(";")) {
            newType = typeName.substring(2, typeName.length() - 1);
        } else {
            newType = typeName;
        }

        if (value instanceof String[]) {
            var cls = Class.forName(newType);
            var type = new TypeReference<Object>() {
                public Type getType() {
                    return cls;
                };
            };

            var array = (String[]) value;
            var objs = (Object[]) Array.newInstance(cls, array.length);
            int idx = 0;
            for (var val : array) {
                if (String.class == cls) {
                    objs[idx] = (String) val;
                } else {
                    objs[idx] = JSON.parseObject(val, type);
                }

                idx++;
            }

            return objs;
        } else if (value instanceof String) {
            var cls = Class.forName(typeName);
            if (cls == String.class) {
                return value;
            }

            return JSON.parseObject((String) value, cls);
        }

        return value;
    }

    /**
     * @param date 时间格式字符串
     * @return 时间对象
     * @throws ParseException 时间转换异常
     */
    public static Date parseDate(String date) throws ParseException {
        if (StringUtils.isEmpty(date)) {
            return null;
        }

        String pattern = "yyyy-MM-dd";
        if (date.indexOf(':') > 0) {
            if (date.indexOf('.') > 0) {
                pattern = "yyyy-MM-dd HH:mm:ss.SSS";
            } else {
                pattern = "yyyy-MM-dd HH:mm:ss";
            }
        }

        return DateFormat.parse(date, pattern);
    }

}
