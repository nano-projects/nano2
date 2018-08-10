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
package org.nanoframework.modules.base.listener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * @author yanghe
 * @since 2.0.0
 */
public abstract class AbstractTypeListener<T extends Annotation> implements CloseableTypeListener {

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        var cls = type.getRawType();
        var annoType = type();
        var fields = fields(Lists.newArrayList(), cls);
        fields.stream().filter(field -> field.isAnnotationPresent(annoType)).forEach(field -> {
            var anno = field.getAnnotation(annoType);
            encounter.register(new MembersInjector<Object>() {
                @Override
                public void injectMembers(Object instance) {
                    field.setAccessible(true);
                    init(anno, cls, instance, field);
                }
            });
        });

        var methods = methods(Lists.newArrayList(), cls);
        methods.stream().filter(method -> method.isAnnotationPresent(annoType)).forEach(method -> {
            var anno = method.getAnnotation(annoType);
            encounter.register(new MembersInjector<Object>() {
                @Override
                public void injectMembers(Object instance) {
                    method.setAccessible(true);
                    init(anno, cls, instance, method);
                }
            });
        });
    }

    /**
     * @return 设置注解类
     */
    protected abstract Class<? extends T> type();

    /**
     * 初始化自定义Field注解依赖注入.
     * @param annotation 注解类
     * @param instance 实现对象
     * @param field 对象属性
     */
    protected void init(T annotation, Class<?> type, Object instance, Field field) {

    }

    /**
     * 初始化自定义Field注解依赖注入.
     * @param annotation 注解类
     * @param instance 实现对象
     * @param field 对象属性
     */
    protected void init(T annotation, Class<?> type, Object instance, Method method) {

    }

    /**
     * @param fields 当前类及继承类中所有的属性
     * @param cls 监听类
     * @return 监听类中的所有属性Field
     */
    private List<Field> fields(List<Field> fields, Class<?> cls) {
        fields.addAll(List.of(cls.getDeclaredFields()));
        var superCls = cls.getSuperclass();
        if (superCls == null) {
            return fields;
        }

        return fields(fields, superCls);
    }

    private List<Method> methods(List<Method> methods, Class<?> cls) {
        methods.addAll(Arrays.asList(cls.getDeclaredMethods()));
        if (cls.getSuperclass() == null) {
            return methods;
        }

        return methods(methods, cls.getSuperclass());
    }
}
