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
package org.nanoframework.core.rest.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.nanoframework.core.rest.enums.HttpType;

/**
 * 组件服务地址映射注解.
 * @author yanghe
 * @since 2.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Route {

    /**
     * @return 路由地址
     */
    String value() default "";

    /**
     * @return 请求类型列表，默认请求级别为GET
     */
    HttpType[] type() default {HttpType.GET };

    /**
     * @return 服务调用超时时间，单位毫秒
     */
    long timeout() default 5000;

    /**
     * @return 请求异常Mock处理，可以通过{@code org.nanoframework.core.rest.mock.Mocker}扩展点进行扩展
     */
    Mock mock() default @Mock;

    /**
     * @return 线程池隔离，默认使用default线程池(CachedThreadPool). <br>
     *         可以通过{@code org.nanoframework.core.concurrent.Executor}扩展点进行扩展.
     */
    String executor() default "default";
}
