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
import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * @author yanghe
 * @since 2.0.0
 */
public abstract class AbstractTypeListener<T extends Annotation> implements TypeListener {

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        var fields = fields(Lists.newArrayList(), type.getRawType());
        var annoType = type();
        fields.stream().filter(field -> field.isAnnotationPresent(annoType)).forEach(field -> {
            var anno = field.getAnnotation(annoType);
            encounter.register(new MembersInjector<Object>() {
                @Override
                public void injectMembers(Object instance) {
                    field.setAccessible(true);
                    init(anno, instance, field);
                }
            });
        });
    }

    protected abstract Class<? extends T> type();

    protected abstract void init(T annotation, Object instance, Field field);

    /**
     * @param fields 当前类及继承类中所有的属性
     * @param cls 监听类
     * @return 监听类中的所有属性Field
     */
    private List<Field> fields(final List<Field> fields, final Class<?> cls) {
        fields.addAll(List.of(cls.getDeclaredFields()));
        var superCls = cls.getSuperclass();
        if (superCls == null) {
            return fields;
        }

        return fields(fields, superCls);
    }
}
