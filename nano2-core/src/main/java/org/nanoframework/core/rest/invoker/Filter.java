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
package org.nanoframework.core.rest.invoker;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.nanoframework.toolkit.lang.ReflectUtils;

/**
 * @author yanghe
 * @since 2.0.0
 */
public abstract class Filter {

    private Filter next;

    private MethodInvocation invocation;

    private Invoker invoker;

    final Object doFilter(MethodInvocation invocation) throws Throwable {
        this.invocation = invocation;
        this.invoker = new Invoker() {
            @Override
            public Class<?> getType() {
                var clsName = invocation.getThis().getClass().getName();
                var index = clsName.indexOf("$$EnhancerByGuice$$");
                if (index > -1) {
                    return ReflectUtils.loadClass(clsName.substring(0, index));
                } else {
                    return ReflectUtils.loadClass(clsName);
                }
            }

            @Override
            public Method getMethod() {
                return invocation.getMethod();
            }

            @Override
            public Object[] getArguments() {
                return invocation.getArguments();
            }

            @Override
            public Object getThis() {
                return invocation.getThis();
            }

            @Override
            public AccessibleObject getStaticPart() {
                return invocation.getStaticPart();
            }

        };

        return proceed(invoker);
    }

    protected final Object doNext() throws Throwable {
        if (next != null) {
            return next.proceed(invoker);
        } else {
            return invocation.proceed();
        }
    }

    protected abstract Object proceed(Invoker invoker) throws Throwable;

    final void next(Filter next) {
        this.next = next;
    }
}
