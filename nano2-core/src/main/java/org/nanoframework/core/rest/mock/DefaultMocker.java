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
package org.nanoframework.core.rest.mock;

import java.util.Map;

import org.nanoframework.core.rest.invoker.Invoker;
import org.nanoframework.core.web.http.HttpStatusCode;

import com.google.inject.Singleton;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Singleton
public class DefaultMocker implements Mocker {

    // private static final String MESSAGE = "{\"error\": {\"code\": 502, \"message\": \"服务已降级\"}}";
    //
    // @Override
    // public Object mock(Invoker invoker) {
    // return MESSAGE;
    // }

    @Override
    public Object mock(Invoker invoker) {
        return Map.of("error", Map.of("code", HttpStatusCode.SC_BAD_GATEWAY, "message", "服务已降级"));
    }

}
