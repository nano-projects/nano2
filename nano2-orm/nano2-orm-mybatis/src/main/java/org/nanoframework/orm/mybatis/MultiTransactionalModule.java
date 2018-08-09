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
package org.nanoframework.orm.mybatis;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.not;

import java.util.List;

import javax.servlet.ServletConfig;

import org.nanoframework.spi.def.Module;

import com.google.inject.AbstractModule;

/**
 * @author yanghe
 * @since 1.2
 */
public class MultiTransactionalModule extends AbstractModule implements Module {

    @Override
    protected void configure() {
        var interceptor = new MultiTransactionalMethodInterceptor();
        requestInjection(interceptor);

        var annotatedElement = annotatedWith(MultiTransactional.class);
        bindInterceptor(any(), annotatedElement, interceptor);
        bindInterceptor(annotatedElement, not(annotatedElement), interceptor);
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
