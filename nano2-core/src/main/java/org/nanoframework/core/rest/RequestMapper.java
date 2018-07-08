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
package org.nanoframework.core.rest;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.nanoframework.beans.BaseEntity;
import org.nanoframework.core.rest.enums.RequestMethod;
import org.nanoframework.toolkit.lang.ArrayUtils;

import com.google.common.collect.Lists;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 组件映射类，存储实例化对象、对象类、方法.
 * @author yanghe
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class RequestMapper extends BaseEntity {
    private static final long serialVersionUID = 6571078157462085564L;

    private Object instance;

    private Class<?> cls;

    private Method method;

    /** @since 1.2 */
    private RequestMethod[] requestMethods = new RequestMethod[] {RequestMethod.GET, RequestMethod.POST };

    private Map<String, String> param;

    public String[] getRequestMethodStrs() {
        if (ArrayUtils.isNotEmpty(requestMethods)) {
            return Arrays.stream(requestMethods).map(RequestMethod::name).toArray(String[]::new);
        }

        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public boolean hasMethod(RequestMethod method) {
        if (ArrayUtils.isNotEmpty(requestMethods)) {
            return Lists.newArrayList(requestMethods).contains(method);
        }

        return true;
    }

}
