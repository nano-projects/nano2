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
package org.nanoframework.toolkit.lang;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * @author yanghe
 * @since 1.3.7
 */
public final class ReflectUtils {

    private ReflectUtils() {

    }

    /**
     * Attempts to create a class from a String.
     * @param <T> the class type
     * @param className the name of the class to create.
     * @return the class. CANNOT be NULL.
     * @throws IllegalArgumentException if the className does not exist.
     */
    public static <T> Class<T> loadClass(String className) throws IllegalArgumentException {
        try {
            return convert(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(className + " class not found.");
        }
    }

    /**
     * 对类进行泛型转换.
     * @param <T> the newInstance type
     * @param cls 原Class
     * @return 泛型Class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> convert(Class<?> cls) {
        return (Class<T>) cls;
    }

    /**
     * 对Type进行泛型转换.
     * @param <T> the newInstance type
     * @param type Type
     * @return 泛型Class
     */
    public static <T> Class<T> convert(Type type) {
        return loadClass(type.getTypeName());
    }

    /**
     * Creates a new instance of the given class by passing the given arguments to the constructor.
     * @param <T> the newInstance type
     * @param className Name of class to be created.
     * @param args Constructor arguments.
     * @return New instance of given class.
     */
    public static <T> T newInstance(String className, Object... args) {
        return newInstance(ReflectUtils.<T>loadClass(className), args);
    }

    /**
     * Creates a new instance of the given class by passing the given arguments to the constructor.
     * @param <T> the newInstance type
     * @param clazz Class of instance to be created.
     * @param args Constructor arguments.
     * @return New instance of given class.
     */
    public static <T> T newInstance(Class<T> clazz, Object... args) {
        var argClasses = new Class[args.length];
        for (var i = 0; i < args.length; i++) {
            argClasses[i] = args[i].getClass();
        }
        try {
            return clazz.getConstructor(argClasses).newInstance(args);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error creating new instance of " + clazz, e);
        }
    }

    /**
     * Gets the property descriptor for the named property on the given class.
     * @param clazz Class to which property belongs.
     * @param propertyName Name of property.
     * @return Property descriptor for given property or null if no property with given name exists in given class.
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) {
        try {
            return getPropertyDescriptor(Introspector.getBeanInfo(clazz), propertyName);
        } catch (IntrospectionException e) {
            throw new RuntimeException("Failed getting bean info for " + clazz, e);
        }
    }

    /**
     * Gets the property descriptor for the named property from the bean info describing a particular class to which
     * property belongs.
     * @param info Bean info describing class to which property belongs.
     * @param propertyName Name of property.
     * @return Property descriptor for given property or null if no property with given name exists.
     */
    public static PropertyDescriptor getPropertyDescriptor(BeanInfo info, String propertyName) {
        for (var i = 0; i < info.getPropertyDescriptors().length; i++) {
            PropertyDescriptor pd = info.getPropertyDescriptors()[i];
            if (pd.getName().equals(propertyName)) {
                return pd;
            }
        }
        return null;
    }

    /**
     * Sets the given property on the target JavaBean using bean instrospection.
     * @param propertyName Property to set.
     * @param value Property value to set.
     * @param target Target java bean on which to set property.
     */
    public static void setProperty(String propertyName, Object value, Object target) {
        try {
            setProperty(propertyName, value, target, Introspector.getBeanInfo(target.getClass()));
        } catch (IntrospectionException e) {
            throw new RuntimeException("Failed getting bean info on target JavaBean " + target, e);
        }
    }

    /**
     * Sets the given property on the target JavaBean using bean instrospection.
     * @param propertyName Property to set.
     * @param value Property value to set.
     * @param target Target JavaBean on which to set property.
     * @param info BeanInfo describing the target JavaBean.
     */
    public static void setProperty(String propertyName, Object value, Object target, BeanInfo info) {
        try {
            var pd = getPropertyDescriptor(info, propertyName);
            pd.getWriteMethod().invoke(target, value);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error setting property " + propertyName, e.getCause());
        } catch (Exception e) {
            throw new RuntimeException("Error setting property " + propertyName, e);
        }
    }
}
