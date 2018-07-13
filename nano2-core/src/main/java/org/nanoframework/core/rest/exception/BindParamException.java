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
package org.nanoframework.core.rest.exception;

/**
 * 绑定参数异常类.
 * @author yanghe
 * @since 2.0.0
 */
public class BindParamException extends RouteException {
    private static final long serialVersionUID = 2491490102693997402L;

    public BindParamException() {

    }

    public BindParamException(String message) {
        super(message);

    }

    public BindParamException(String message, Throwable cause) {
        super(message, cause);

    }

}
