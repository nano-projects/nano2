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
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Scanner;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.core.rest.RouteMapper;
import org.nanoframework.core.rest.enums.HttpType;
import org.nanoframework.core.rest.exception.RouteException;
import org.nanoframework.core.web.http.HttpStatus;
import org.nanoframework.core.web.http.ResultMap;
import org.nanoframework.core.web.http.URLContext;
import org.nanoframework.core.web.mvc.Model;
import org.nanoframework.core.web.mvc.View;
import org.nanoframework.toolkit.consts.Charsets;
import org.nanoframework.toolkit.consts.ContentType;
import org.nanoframework.toolkit.lang.ArrayUtils;
import org.nanoframework.toolkit.lang.ObjectUtils;
import org.nanoframework.toolkit.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author yanghe
 * @since 1.2
 */
public abstract class AbstractFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(Charsets.UTF_8.name());
        response.setCharacterEncoding(Charsets.UTF_8.name());

        if (invoke((HttpServletRequest) request, (HttpServletResponse) response)) {
            chain.doFilter(request, response);
        }
    }

    protected abstract boolean invoke(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException;

    @Override
    public void destroy() {

    }

    protected boolean validHttpType(ServletResponse response, Writer out, RouteMapper mapper, String type)
            throws IOException {
        if (!mapper.hasType(HttpType.valueOf(type))) {
            response.setContentType(ContentType.APPLICATION_JSON);
            out = response.getWriter();
            out.write(JSON.toJSONString(HttpStatus.BAD_REQUEST.to(
                    String.format("不支持此请求类型(%s)，仅支持类型(%s)", type, StringUtils.join(mapper.getTypeValues(), " / ")))));
            return false;
        }

        return true;
    }

    protected void process(ServletRequest request, ServletResponse response, Writer out, URLContext urlContext,
            Object ret, Model model) throws IOException, ServletException {
        if (ret instanceof View) {
            ((View) ret).redirect(model.get(), (HttpServletRequest) request, (HttpServletResponse) response);
        } else if (ret instanceof String) {
            response.setContentType(ContentType.APPLICATION_JSON);
            out = response.getWriter();
            out.write((String) ret);
        } else if (ret instanceof Void) {
            return;
        } else if (ret != null) {
            response.setContentType(ContentType.APPLICATION_JSON);
            out = response.getWriter();
            /** 跨域JSONP的Ajax请求支持 */
            var callback = urlContext.getParameter().get("callback");
            var value = JSON.toJSONString(ret, SerializerFeature.WriteDateUseDateFormat);
            if (ObjectUtils.isNotEmpty(callback)) {
                out.write(String.format("%s(%s)", callback, value));
            } else {
                out.write(value);
            }
        } else {
            response.setContentType(ContentType.APPLICATION_JSON);
            out = response.getWriter();
            out.write(HttpStatus.BAD_REQUEST.to().toString());
        }
    }

    protected URLContext create(final HttpServletRequest request) throws IOException {
        var parameter = new HashMap<String, Object>();
        request.getParameterMap().forEach((key, value) -> {
            if (value.length > 0) {
                if (key.endsWith("[]")) {
                    parameter.put(key.toLowerCase(), value);
                } else {
                    parameter.put(key.toLowerCase(), value[0]);
                }
            }
        });

        var contentType = request.getContentType();
        if (StringUtils.isBlank(contentType) || StringUtils.equals(contentType.split(";")[0],
                ContentType.APPLICATION_FORM_URLENCODED.split(";")[0])) {
            try (var scanner = new Scanner(request.getInputStream())) {
                while (scanner.hasNextLine()) {
                    var line = scanner.nextLine();
                    var kvs = line.split("&");
                    for (var kv : kvs) {
                        if (StringUtils.isBlank(kv)) {
                            continue;
                        }

                        var keyValue = kv.split("=");
                        var key = StringUtils.lowerCase(keyValue[0]);
                        var value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], Charsets.UTF_8.name())
                                : StringUtils.EMPTY;
                        if (value.length() > 0) {
                            if (key.endsWith("[]")) {
                                var values = (String[]) parameter.get(key);
                                if (values == null) {
                                    parameter.put(key, new String[] {value });
                                } else {
                                    parameter.put(key, ArrayUtils.add(values, value));
                                }
                            } else {
                                parameter.put(key, value);
                            }
                        }
                    }
                }
            }
        }

        var uri = URLDecoder.decode(((HttpServletRequest) request).getRequestURI(), Charsets.UTF_8.name());
        var urlContext = URLContext.builder().context(uri).parameter(parameter).build();
        var uris = uri.split(";");
        if (uris.length > 1) {
            urlContext.setContext(uris[0]);
            var specials = new String[uris.length - 1];
            System.arraycopy(uris, 1, specials, 0, specials.length);
            urlContext.setSpecial(specials);
        }

        return urlContext;
    }

    protected ResultMap error(Throwable e) {
        if (e instanceof RouteException) {
            return HttpStatus.BAD_REQUEST.to(e.getMessage());
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR.to(e.getMessage());
        }
    }

}
