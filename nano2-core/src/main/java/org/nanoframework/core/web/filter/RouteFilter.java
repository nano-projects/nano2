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
package org.nanoframework.core.web.filter;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.core.rest.Routes;
import org.nanoframework.core.rest.enums.HttpType;
import org.nanoframework.core.rest.exception.RouteException;
import org.nanoframework.core.web.http.URLContext;
import org.nanoframework.core.web.mvc.Model;
import org.nanoframework.core.web.mvc.support.RedirectModel;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.toolkit.consts.ContentType;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import lombok.NonNull;

/**
 * Http请求拦截器 <br>
 * 此拦截器为NanoFramework框架的请求主入口 <br>
 * 如果调用的URI存在与组件映射表中的话，则可以调用组件的服务。<br>
 * 如果不存在的话，则请求后续的内容.
 * @author yanghe
 * @since 1.0
 */
public class RouteFilter extends AbstractFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteFilter.class);

    @Override
    protected boolean invoke(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        var context = create((HttpServletRequest) request);
        var method = ((HttpServletRequest) request).getMethod();
        var mapper = Routes.route().lookup(context.getNoRootContext(), HttpType.valueOf(method));

        Writer out = null;
        if (mapper != null) {
            try {
                if (!validHttpType(response, out, mapper, method)) {
                    return false;
                }

                var model = new RedirectModel();
                HttpContext.set(Map.of(HttpServletRequest.class, request, HttpServletResponse.class, response,
                        Model.class, model, URLContext.class, context));

                var value = Routes.route().invoke(mapper, context.getParameter(), request, response, model, context);
                process(request, response, out, context, value, model);
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
                response.setContentType(ContentType.APPLICATION_JSON);
                if (out == null) {
                    out = response.getWriter();
                }

                out.write(JSON.toJSONString(error(e)));
            } finally {
                if (out != null) {
                    out.flush();
                    out.close();
                }

                HttpContext.clear();
            }

            return false;
        }

        return true;
    }

    /**
     * @author yanghe
     * @since 1.3.5
     */
    public static class HttpContext {
        private static ThreadLocal<Map<Class<?>, Object>> CONTEXT = new ThreadLocal<>();

        protected static void set(Map<Class<?>, Object> context) {
            clear();
            CONTEXT.set(Maps.newLinkedHashMap(context));
        }

        protected static void clear() {
            var ctx = CONTEXT.get();
            if (ctx != null) {
                ctx.clear();
                CONTEXT.remove();
            }
        }

        @SuppressWarnings("unchecked")
        public static <T> T get(@NonNull Class<T> type) {
            var context = CONTEXT.get();
            if (context != null) {
                return (T) context.get(type);
            }

            throw new NullPointerException("未设置Class: " + type.getName());
        }

        public static class HttpContextCopies {
            private final Map<Class<?>, Object> context;

            private final Thread parent;

            public HttpContextCopies() {
                context = CONTEXT.get();
                parent = Thread.currentThread();
            }

            public void copy() {
                if (Thread.currentThread() == parent) {
                    throw new RouteException("Context复制不能在同一线程进行");
                }

                HttpContext.set(context);
            }

            public void clear() {
                HttpContext.clear();
            }
        }
    }

}
