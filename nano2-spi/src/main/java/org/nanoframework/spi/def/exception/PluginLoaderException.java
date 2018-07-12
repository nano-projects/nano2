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
package org.nanoframework.spi.def.exception;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class PluginLoaderException extends RuntimeException {
    private static final long serialVersionUID = -3388677411268525198L;

    /** */
    public PluginLoaderException() {
        super();
    }

    /**
     * @param message 异常信息
     */
    public PluginLoaderException(String message) {
        super(message);
    }

    /**
     * @param message 异常信息
     * @param cause 异常
     */
    public PluginLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause 异常
     */
    public PluginLoaderException(Throwable cause) {
        super(cause);
    }

}
