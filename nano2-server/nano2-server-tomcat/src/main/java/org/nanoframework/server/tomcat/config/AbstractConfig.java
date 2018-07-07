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
package org.nanoframework.server.tomcat.config;

import java.util.Arrays;

import org.nanoframework.beans.BaseEntity;
import org.nanoframework.toolkit.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author yanghe
 * @since 2.0.0
 */
public abstract class AbstractConfig extends BaseEntity {
    private static final long serialVersionUID = 2578839220433405594L;

    protected static BaseEntity DEF;

    public void merge(BaseEntity conf) {
        if (DEF != null) {
            merge0(DEF);
        }

        if (conf != null) {
            merge0(conf);
        }
    }

    private void merge0(BaseEntity conf) {
        Arrays.stream(attributeNames()).forEach(key -> {
            var value = conf.attributeValue(key);
            if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                setAttributeValue(key, value);
            }
        });
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append('\n');
        builder.append(key());
        builder.append(" = { \n");
        Arrays.stream(attributeNames()).forEach(key -> {
            var value = this.attributeValue(key);
            builder.append("  \"");
            builder.append(key);
            builder.append("\": ");
            builder.append(JSON.toJSONString(value, SerializerFeature.WriteDateUseDateFormat));
            builder.append(", \n");
        });

        builder.append('}');
        return builder.toString();
    }

    public abstract String key();
}
