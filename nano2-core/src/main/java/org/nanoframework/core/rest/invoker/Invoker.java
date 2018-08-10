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

import org.aopalliance.intercept.Joinpoint;

/**
 * @author yanghe
 * @since 2.0.0
 */
public interface Invoker {

    Class<?> getType();

    /**
     * Gets the method being called.
     * <p>
     * This method is a frienly implementation of the {@link Joinpoint#getStaticPart()} method (same result).
     * @return the method being called.
     */
    Method getMethod();

    /**
     * Get the arguments as an array object. It is possible to change element values within this array to change the
     * arguments.
     * @return the argument of the invocation
     */
    Object[] getArguments();

    /**
     * Returns the object that holds the current joinpoint's static part.
     * <p>
     * For instance, the target object for an invocation.
     * @return the object (can be null if the accessible object is static).
     */
    Object getThis();

    /**
     * Returns the static part of this joinpoint.
     * <p>
     * The static part is an accessible object on which a chain of interceptors are installed.
     */
    AccessibleObject getStaticPart();
}
