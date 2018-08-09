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
package org.nanoframework.modules.httpclient.listener;

import java.io.IOException;
import java.lang.reflect.Field;

import org.nanoframework.modules.base.listener.AbstractTypeListener;
import org.nanoframework.modules.httpclient.annotation.Http;
import org.nanoframework.modules.httpclient.config.HttpConfigure;
import org.nanoframework.modules.httpclient.exception.HttpClientException;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class HttpClientTypeListener extends AbstractTypeListener<Http> {

    @Override
    protected Class<? extends Http> type() {
        return Http.class;
    }

    @Override
    protected void init(Http http, Object instance, Field field) {
        try {
            field.set(instance, new HttpConfigure(http).get());
        } catch (Throwable e) {
            throw new HttpClientException(String.format("设置置配异常: %s", e.getMessage()), e);
        }
    }

    @Override
    public void close() throws IOException {
        HttpConfigure.clear();
    }

}
