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
package org.nanoframework.modules.aop.module;

import java.util.List;

import javax.servlet.ServletConfig;

import org.nanoframework.modules.aop.annotation.After;
import org.nanoframework.modules.aop.annotation.AfterMore;
import org.nanoframework.modules.aop.annotation.Before;
import org.nanoframework.modules.aop.annotation.BeforeAndAfter;
import org.nanoframework.modules.aop.annotation.BeforeAndAfterMore;
import org.nanoframework.modules.aop.annotation.BeforeMore;
import org.nanoframework.modules.aop.interceptor.AfterInterceptor;
import org.nanoframework.modules.aop.interceptor.AfterMoreInterceptor;
import org.nanoframework.modules.aop.interceptor.BeforeAndAfterInterceptor;
import org.nanoframework.modules.aop.interceptor.BeforeAndAfterMoreInterceptor;
import org.nanoframework.modules.aop.interceptor.BeforeInterceptor;
import org.nanoframework.modules.aop.interceptor.BeforeMoreInterceptor;
import org.nanoframework.spi.def.Module;

import com.google.inject.Binder;
import com.google.inject.matcher.Matchers;

/**
 * @author yanghe
 * @since 1.1
 */
public class AOPModule implements Module {

    @Override
    public void configure(final Binder binder) {
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Before.class), new BeforeInterceptor());
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(After.class), new AfterInterceptor());
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(BeforeAndAfter.class),
                new BeforeAndAfterInterceptor());

        // Interceptor More
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(BeforeMore.class), new BeforeMoreInterceptor());
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(AfterMore.class), new AfterMoreInterceptor());
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(BeforeAndAfterMore.class),
                new BeforeAndAfterMoreInterceptor());
    }

    @Override
    public List<Module> load() {
        return List.of(this);
    }

    @Override
    public void config(final ServletConfig config) {

    }

    @Override
    public void destroy() {

    }
}
