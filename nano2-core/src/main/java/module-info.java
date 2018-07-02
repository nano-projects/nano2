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
/**
 * @author yanghe
 * @since 2.0.0
 */
open module org.nanoframework.core {
    exports org.nanoframework.core.spi;

    exports org.nanoframework.core.properties;

    exports org.nanoframework.core.properties.exception;

    exports org.nanoframework.core.spi.annotation;

    exports org.nanoframework.core.servlet;

    exports org.nanoframework.core.config;

    exports org.nanoframework.core.spi.exception;

    exports org.nanoframework.core.plugins.exception;

    exports org.nanoframework.core.plugins;

    requires com.google.common;

    requires com.google.guice;

    requires javax.inject;

    requires javax.servlet.api;

    requires lombok;

    requires org.nanoframework.beans;

    requires org.nanoframework.logging;

    requires org.nanoframework.toolkit;
}