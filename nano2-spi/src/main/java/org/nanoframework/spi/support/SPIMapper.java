/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.spi.support;

import org.nanoframework.beans.BaseEntity;
import org.nanoframework.spi.annotation.Lazy;
import org.nanoframework.spi.annotation.Order;

import lombok.Getter;
import lombok.NonNull;

/**
 * @author yanghe
 * @since 1.4.8
 */
@Getter
@SuppressWarnings({"rawtypes", "unchecked" })
public class SPIMapper extends BaseEntity {
    private static final long serialVersionUID = -2120348787803208033L;

    private static final Integer DEFAULT_ORDER = 0;

    private final Class spi;

    private final String spiClsName;

    private final String name;

    private final Class instance;

    private final String instanceClsName;

    private final Boolean lazy;

    private final Integer order;

    private SPIMapper(@NonNull Class spi, @NonNull String name, @NonNull Class instance) {
        this.spi = spi;
        this.spiClsName = spi.getName();
        this.name = name;
        this.instance = instance;
        this.instanceClsName = instance.getName();
        this.lazy = instance.isAnnotationPresent(Lazy.class);
        if (instance.isAnnotationPresent(Order.class)) {
            var order = (Order) instance.getAnnotation(Order.class);
            this.order = order.value();
        } else {
            this.order = DEFAULT_ORDER;
        }
    }

    public static SPIMapper create(Class spi, String name, Class instance) {
        return new SPIMapper(spi, name, instance);
    }

}
