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
package org.nanoframework.beans;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nanoframework.beans.exception.EntityException;
import org.nanoframework.beans.format.ClassCast;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.StringUtils;

import com.alibaba.fastjson.JSON;

/**
 * 基础实体类，实体类功能扩展辅助类.
 * @author yanghe
 * @since 2.0.0
 */
public abstract class BaseEntity implements Cloneable, Serializable {
    private static final long serialVersionUID = 3188627488044889912L;

    private static transient List<String> FILTER_FIELD_NAMES = List.of("names", "cls", "methods", "fields");

    /** 当前对象的所有Method. */
    protected transient Map<String, Method> methods = paramMethods();

    /** 当前对象的所有Field. */
    protected transient Map<String, Field> fields = paramFields();

    private transient String[] names;

    /**
     * 获取所有属性名.
     * @return 返回属性数组
     */
    public String[] attributeNames() {
        if (names != null) {
            return names;
        }

        names = fields.keySet().toArray(new String[fields.size()]);
        return names;
    }

    /**
     * 根据属性名获取该属性的值.
     * @param <T> 参数类型
     * @param fieldName 属性名
     * @return 返回该属性的值
     */
    @SuppressWarnings("unchecked")
    public <T> T attributeValue(String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("属性名不能为空");
        }

        try {
            if (fields.containsKey(fieldName)) {
                var field = fields.get(fieldName);
                var fieldGetName = parGetName(field.getName());
                if (hasMethodName(fieldGetName)) {
                    return (T) methods.get(fieldGetName).invoke(this);
                }
            } else {
                throw new NoSuchFieldException("无效的属性名称: " + fieldName);
            }
        } catch (Throwable e) {
            throw new EntityException(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 根据属性名获取该属性的值.
     * @param <T> 参数类型
     * @param fieldName 属性名
     * @param defaultValue 默认值，当field获取的值为null时选用defaulValue的值
     * @return 返回该属性的值
     */
    public <T> T attributeValue(String fieldName, T defaultValue) {
        T value = attributeValue(fieldName);
        return value == null ? (T) defaultValue : value;
    }

    /**
     * 设置属性值, 默认不区分大小写.
     * @param fieldName 属性名
     * @param value 属性值
     */
    public void setAttributeValue(String fieldName, Object value) {
        setAttributeValue(fieldName, value, false);
    }

    /**
     * 设置属性值.
     * @param fieldName 属性名
     * @param value 属性值
     * @param isCase 区分大小写，true时区分大小写，默认false
     */
    public void setAttributeValue(String fieldName, Object value, boolean isCase) {
        if (StringUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("属性名不能为空");
        }

        try {
            if (fields.containsKey(fieldName)) {
                var field = fields.get(fieldName);
                if ((!isCase && fieldName.toUpperCase().equals(field.getName().toUpperCase()))
                        || (isCase && fieldName.equals(field.getName()))) {
                    var fieldSetName = parSetName(field.getName());
                    if (hasMethodName(fieldSetName)) {
                        var typeName = field.getType().getName();
                        methods.get(fieldSetName).invoke(this, ClassCast.cast(value, typeName));
                    }
                }
            } else {
                throw new NoSuchFieldException("无效的属性名: " + fieldName);
            }
        } catch (Throwable e) {
            throw new EntityException(e.getMessage(), e);
        }
    }

    /**
     * 检查是否有方法.
     * @param methods 方法集
     * @param methodName 方法名
     * @return boolean
     */
    private boolean hasMethodName(String methodName) {
        return methods.containsKey(methodName);
    }

    /**
     * get+属性名.
     * @param fieldName 属性名
     * @return String
     */
    protected String parGetName(String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            return null;
        }

        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * set+属性名.
     * @param fieldName 属性名
     * @return String
     */
    protected String parSetName(String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            return null;
        }

        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 将实体类转换成Map.
     * @return Map
     */
    public Map<String, Object> beanToMap() {
        var beanToMap = new HashMap<String, Object>();
        for (var key : attributeNames()) {
            var value = attributeValue(key);
            if (value != null) {
                beanToMap.put(key, value);
            }
        }

        return beanToMap;
    }

    /**
     * 将Map对象转换成实体类对象.
     * @param <T> 参数类型
     * @param beanMap 符合实体规范的Map
     * @param beanType 实体类
     * @return 转换后的实体类
     */
    public static <T extends BaseEntity> T mapToBean(Map<String, Object> beanMap, Class<T> beanType) {
        if (beanType == null) {
            throw new EntityException("beanType不能为空");
        }

        if (beanMap == null) {
            return null;
        }

        try {
            var bean = (T) beanType.getConstructor().newInstance();
            beanMap.forEach((key, value) -> bean.setAttributeValue(key, value));
            return bean;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new EntityException(e.getMessage(), e);
        }
    }

    /**
     * 将Map对象集合转换成实体类对象集合.
     * @param <T> 参数类型
     * @param beanMaps 符合实体规范的Map集合
     * @param beanType 实体类
     * @return 转换后的实体类集合
     */
    public static <T extends BaseEntity> List<T> mapToBeans(List<Map<String, Object>> beanMaps, Class<T> beanType) {
        if (CollectionUtils.isEmpty(beanMaps)) {
            return Collections.emptyList();
        }

        var beans = new ArrayList<T>(beanMaps.size());
        for (var beanMap : beanMaps) {
            beans.add(mapToBean(beanMap, beanType));
        }

        return beans;
    }

    /**
     * 获取对象所有方法.
     * @param cls 对象类
     * @return 实体类方法列表
     */
    public static Map<String, Method> paramMethods(Class<?> cls) {
        var map = new LinkedHashMap<String, Method>();
        allMethods(new ArrayList<>(), cls).stream().filter(method -> !Modifier.isFinal(method.getModifiers()))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .forEach(method -> map.put(method.getName(), method));
        return map;
    }

    /**
     * 获取实体类所有方法.
     * @return 实体类方法列表
     */
    protected Map<String, Method> paramMethods() {
        return paramMethods(getClass());
    }

    /**
     * 递归获取当前类及父类中的所有方法.
     * @param allMethods 实体类方法集合
     * @param clazz 当前类或父类
     * @return 新实体类方法集合
     */
    protected static List<Method> allMethods(List<Method> allMethods, Class<?> clazz) {
        allMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        if (clazz.getSuperclass() == null) {
            return allMethods;
        }

        return allMethods(allMethods, clazz.getSuperclass());
    }

    /**
     * 获取对象所有属性.
     * @param cls 对象类
     * @return 实体类方法列表
     */
    public static Map<String, Field> paramFields(Class<?> cls) {
        var map = new LinkedHashMap<String, Field>();
        allFields(new ArrayList<>(), cls).stream().filter(field -> !Modifier.isFinal(field.getModifiers()))
                .filter(field -> !Modifier.isStatic(field.getModifiers())).filter(BaseEntity::filterField)
                .forEach(field -> map.put(field.getName(), field));
        return map;
    }

    /**
     * 获取实体类的所有属性.
     * @return 实体类属性列表
     */
    protected Map<String, Field> paramFields() {
        return paramFields(getClass());
    }

    /**
     * 递归获取当前类及父类中的所有属性.
     * @param allFields 实体类属性集合
     * @param clazz 当前类或父类
     * @return 新实体类属性集合
     */
    protected static List<Field> allFields(List<Field> allFields, Class<?> clazz) {
        allFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() == null) {
            return allFields;
        }

        return allFields(allFields, clazz.getSuperclass());
    }

    /**
     * @param field Field
     * @return 判断是否内置属性，如果是则返回false
     */
    protected static boolean filterField(Field field) {
        return !FILTER_FIELD_NAMES.contains(field.getName());
    }

    /**
     * @return 当前对象所有的Method
     */
    public Collection<Method> methods() {
        return methods.values();
    }

    /**
     * @return 当前对象所有的Field
     */
    public Collection<Field> fields() {
        return fields.values();
    }

    @Override
    public final BaseEntity clone() {
        try {
            return (BaseEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new EntityException("Clone Not Supported Exception: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
