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
package org.nanoframework.logging.plugin;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.servlet.ServletConfig;

import org.nanoframework.spi.sys.Plugin;
import org.nanoframework.toolkit.exception.PluginLoaderException;
import org.nanoframework.toolkit.lang.ResourceUtils;
import org.nanoframework.toolkit.lang.StringUtils;

/**
 * @author yanghe
 * @since 1.3.7
 */
public class Log4j2Plugin implements Plugin {
    private static final String DEFAULT_LOG4J2_PARAMETER_NAME = "log4j2";
    private String log4j2;

    @Override
    public boolean load() throws Throwable {
        if (StringUtils.isNotBlank(log4j2)) {
            return load(log4j2);
        }

        return false;
    }

    private boolean load(final String name) throws Throwable {
        final URL url = this.getClass().getResource(name);
        if (url != null && load0(url.toURI())) {
            return true;
        }

        final File file = ResourceUtils.getFile(name);
        if (file != null && load0(file.toURI())) {
            return true;
        }

        final URI uri = ResourceUtils.getURL(name).toURI();
        if (uri != null && load0(uri)) {
            return true;
        }

        return false;
    }

    protected boolean load0(final URI resource) {
        if (resource != null) {
            try {
                final Class<?> logManager = Class.forName("org.apache.logging.log4j.LogManager");
                final Object context = logManager.getMethod("getContext", boolean.class).invoke(logManager, Boolean.FALSE);
                final Class<?> loggerContext = Class.forName("org.apache.logging.log4j.core.LoggerContext");
                loggerContext.getMethod("setConfigLocation", URI.class).invoke(context, resource);
                return true;
            } catch (final Throwable e) {
                if (!(e instanceof ClassNotFoundException)) {
                    throw new PluginLoaderException(e.getMessage(), e);
                }

                return false;
            }
        }

        return false;
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {
        log4j2 = config.getInitParameter(DEFAULT_LOG4J2_PARAMETER_NAME);
    }

}
