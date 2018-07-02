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
import org.nanoframework.aop.annotation.BeforeAndAfterMore;
import org.nanoframework.beans.Globals;

import com.google.inject.Injector;

/**
 * @author yanghe
 * @since 1.0
 */
public class BeforeAndAfterMoreInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        var beforeAndAfterMore = invocation.getMethod().getAnnotation(BeforeAndAfterMore.class);
        var beforeMap = new LinkedHashMap<Method, Object>();
        var afterMap = new LinkedHashMap<Method, Object>();
        for (var beforeAndAfter : beforeAndAfterMore.value()) {
            var instance = Globals.get(Injector.class).getInstance(beforeAndAfter.value());
            var beforeMethod = beforeAndAfter.value().getMethod(MethodNames.BEFORE.value(), MethodInvocation.class);
            var afterMethod = beforeAndAfter.value().getMethod(MethodNames.AFTER.value(), MethodInvocation.class,
                    Object.class);
            beforeMap.put(beforeMethod, instance);
            afterMap.put(afterMethod, instance);
        }

        Object obj = null;
        try {
            for (var iter = beforeMap.entrySet().iterator(); iter.hasNext();) {
                var entry = iter.next();
                entry.getKey().invoke(entry.getValue(), invocation);
            }

            return obj = invocation.proceed();
        } catch (Throwable e) {
            obj = e;
            throw e;

        } finally {
            for (var iter = afterMap.entrySet().iterator(); iter.hasNext();) {
                var entry = iter.next();
                entry.getKey().invoke(entry.getValue(), invocation, obj);
            }
        }
    }

}
