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
package org.nanoframework.aop.interceptor;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.nanoframework.aop.MethodNames;
import org.nanoframework.aop.annotation.BeforeMore;
import org.nanoframework.beans.Globals;

import com.google.inject.Injector;

/**
 * @author yanghe
 * @since 1.0
 */
public class BeforeMoreInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        var beforeMore = invocation.getMethod().getAnnotation(BeforeMore.class);
        var befores = beforeMore.value();
        var map = new LinkedHashMap<Method, Object>();
        for (var before : befores) {
            var method = before.value().getMethod(MethodNames.BEFORE.value(), MethodInvocation.class);
            var instance = Globals.get(Injector.class).getInstance(before.value());
            map.put(method, instance);
        }

        for (var iter = map.entrySet().iterator(); iter.hasNext();) {
            var entry = iter.next();
            entry.getKey().invoke(entry.getValue(), invocation);
        }

        return invocation.proceed();
    }
}
