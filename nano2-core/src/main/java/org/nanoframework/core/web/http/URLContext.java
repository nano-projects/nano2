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
package org.nanoframework.core.web.http;

import static org.nanoframework.toolkit.lang.StringUtils.EMPTY;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nanoframework.server.Server;
import org.nanoframework.toolkit.lang.StringUtils;

import com.google.common.collect.Lists;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 请求地址解析后对象，拆解GET请求中的context和参数列表.
 * @author yanghe
 * @since 1.3.7
 */
@Getter
@Setter
@Builder
public class URLContext {

    /** URI串. */
    private String context;

    /** 带有';'的URI拆分内容. */
    private String[] special;

    /** 参数列表. */
    private Map<String, Object> parameter;

    public String getNoRootContext() {
        var root = System.getProperty(Server.ROOT);
        return context == null ? EMPTY : StringUtils.isEmpty(root) ? context : context.replaceFirst(root, EMPTY);
    }

    @SuppressWarnings("unchecked")
    public static URLContext of(String url) {
        if (StringUtils.isEmpty(url)) {
            throw new NullPointerException("URL不能为空");
        }

        var tokens = url.split("[?]");
        var token = tokens[0];
        var context = URLContext.builder().context(token).build();

        // 拆分参数列表
        if (tokens.length > 1) {
            var keyValue = new HashMap<String, Object>();
            var params = tokens[1].split("&");
            if (params.length > 0) {
                var hasArray = false;
                for (var param : params) {
                    var kv = param.split("=");
                    if (kv[0].endsWith("[]")) {
                        hasArray = true;
                        var values = (List<String>) keyValue.get(kv[0]);
                        if (values == null) {
                            values = Lists.newArrayList();
                        }

                        values.add(kv[1]);
                        keyValue.put(kv[0], values);
                    } else {
                        keyValue.put(kv[0].toLowerCase(), kv[1]);
                    }
                }

                if (hasArray) {
                    keyValue.entrySet().stream().filter(entry -> entry.getKey().endsWith("[]")).forEach(entry -> {
                        keyValue.put(entry.getKey(), ((List<String>) entry.getValue()).toArray(new String[0]));
                    });
                }
            }

            context.setParameter(keyValue);
        } else {
            context.setParameter(Collections.emptyMap());
        }

        return context;
    }
}
