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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.nanoframework.beans.BaseEntity;
import org.nanoframework.beans.format.ClassCast;
import org.nanoframework.core.rest.annotation.Body;
import org.nanoframework.core.rest.annotation.Param;
import org.nanoframework.core.rest.annotation.PathVariable;
import org.nanoframework.core.rest.enums.HttpType;
import org.nanoframework.core.rest.enums.ValueConstants;
import org.nanoframework.core.rest.exception.BindParamException;
import org.nanoframework.core.web.http.ReadStream;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.toolkit.lang.ArrayUtils;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.ObjectUtils;
import org.nanoframework.toolkit.lang.StringUtils;

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
public class RouteMapper extends BaseEntity {
    private static final long serialVersionUID = 6571078157462085564L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteMapper.class);

    private Object instance;

    private Class<?> cls;

    private Method method;

    private HttpType[] types = new HttpType[] {HttpType.GET, HttpType.POST };

    private Map<String, String> param;

    /**
     * @return 请求类型列表
     */
    public String[] getTypeValues() {
        if (ArrayUtils.isNotEmpty(types)) {
            return Arrays.stream(types).map(HttpType::name).toArray(String[]::new);
        }

        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    /**
     * @param type 请求类型
     * @return 判断当前路由是否支持此请求类型
     */
    public boolean hasType(HttpType type) {
        if (ArrayUtils.isNotEmpty(types)) {
            return Lists.newArrayList(types).contains(type);
        }

        return true;
    }

    public Object[] bind(Method method, Map<String, Object> params, Object... objs) {
        var lowerCaseParams = new HashMap<String, Object>();
        if (CollectionUtils.isNotEmpty(params)) {
            params.forEach((key, value) -> lowerCaseParams.put(key.toLowerCase(), value));
        }

        var parameters = method.getParameters();
        if (ArrayUtils.isNotEmpty(parameters)) {
            var values = new ArrayList<Object>();
            var bodyCount = new AtomicInteger();
            for (var parameter : parameters) {
                var type = parameter.getType();
                var param = parameter.getAnnotation(Param.class);
                var pathVariable = parameter.getAnnotation(PathVariable.class);
                var body = parameter.getAnnotation(Body.class);
                if (ObjectUtils.isNotNull(param, pathVariable, body) > 1) {
                    throw new BindParamException("参数绑定不能同时多种类型");
                }

                if (param != null) {
                    bindWithParam(param, lowerCaseParams, values, type);
                } else if (pathVariable != null) {
                    bindWithPathVariable(pathVariable, lowerCaseParams, values, type);
                } else if (body != null) {
                    if (bodyCount.incrementAndGet() > 1) {
                        throw new BindParamException("不能同时绑定多个@Body");
                    }

                    bindWithBody(values, type);
                } else if (objs != null && objs.length > 0) {
                    bindWithOther(values, type, objs);
                }

            }

            return values.toArray(new Object[values.size()]);
        }

        return null;

    }

    private void bindWithParam(Param param, Map<String, Object> lowerCaseParams, List<Object> values, Class<?> type) {
        var value = param.value();
        var paramValue = lowerCaseParams.get(value.toLowerCase());
        if (paramValue == null && StringUtils.notEquals(param.defaultValue(), ValueConstants.DEFAULT_NONE)) {
            paramValue = param.defaultValue();
        }

        if (param.required()
                && (paramValue == null || (paramValue instanceof String && StringUtils.isEmpty((String) paramValue)))) {
            throw new BindParamException("参数:[" + value + "]为必填项，但是获取的参数值为空.");
        }

        try {
            values.add(ClassCast.cast(paramValue, type.getName()));
        } catch (org.nanoframework.beans.format.exception.ClassCastException e) {
            LOGGER.error(e.getMessage(), e);
            throw new BindParamException(String.format("类型转换异常: 数据类型 [ %s ], 值 [ %s ]", type.getName(), paramValue));
        }
    }

    private void bindWithPathVariable(PathVariable pathVariable, Map<String, Object> lowerCaseParams,
            List<Object> values, Class<?> type) {
        var paramValue = lowerCaseParams.get(pathVariable.value().toLowerCase());
        if (paramValue != null) {
            try {
                values.add(ClassCast.cast(paramValue, type.getName()));
            } catch (org.nanoframework.beans.format.exception.ClassCastException e) {
                LOGGER.error(e.getMessage(), e);
                throw new BindParamException(
                        String.format("类型转换异常: 数据类型 [ %s ], 值 [ %s ]", type.getName(), paramValue));
            }
        } else {
            throw new BindParamException("Restful风格参数:[" + pathVariable.value().toLowerCase() + "]为必填项，但是获取的参数值为空.");
        }
    }

    private void bindWithBody(List<Object> values, Class<?> type) {
        try {
            values.add(ClassCast.cast(ReadStream.read(), type.getName()));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new BindParamException("读取参数流异常");
        }
    }

    private void bindWithOther(List<Object> values, Class<?> type, Object... objs) {
        for (var obj : objs) {
            if (type.isInstance(obj)) {
                values.add(obj);
                break;
            }
        }
    }
}
