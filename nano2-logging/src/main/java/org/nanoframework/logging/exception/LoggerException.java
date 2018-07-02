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
package org.nanoframework.logging.exception;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class LoggerException extends RuntimeException {
    private static final long serialVersionUID = -8628934047050622843L;

    /**
     * @param message 异常消息
     */
    public LoggerException(String message) {
        super(message);
    }

    /**
     * @param message 异常消息
     * @param cause 异常
     */
    public LoggerException(String message, Throwable cause) {
        super(message, cause);
    }
}
