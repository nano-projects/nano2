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
package org.nanoframework.modules.logging.module;

import java.net.URI;
import java.util.List;

import javax.servlet.ServletConfig;

import org.nanoframework.spi.def.Module;
import org.nanoframework.spi.def.exception.PluginLoaderException;
import org.nanoframework.toolkit.lang.ResourceUtils;
import org.nanoframework.toolkit.lang.StringUtils;

import com.google.inject.Binder;

/**
 * @author yanghe
 * @since 1.3.7
 */
public class Log4j2Module implements Module {
    private static final String LOG4J2 = "log4j2";

    @Override
    public void configure(Binder binder) {
        if (StringUtils.isNotBlank(LOG4J2)) {
            try {
                var url = this.getClass().getResource(LOG4J2);
                if (url != null && load0(url.toURI())) {
                    return;
                }

                var file = ResourceUtils.getFile(LOG4J2);
                if (file != null && load0(file.toURI())) {
                    return;
                }

                var uri = ResourceUtils.getURL(LOG4J2).toURI();
                if (uri != null && load0(uri)) {
                    return;
                }
            } catch (Throwable e) {
                throw new PluginLoaderException(e.getMessage(), e);
            }
        }
    }

    private boolean load0(URI resource) {
        if (resource != null) {
            try {
                var logManager = Class.forName("org.apache.logging.log4j.LogManager");
                var context = logManager.getMethod("getContext", boolean.class).invoke(logManager, Boolean.FALSE);
                var loggerContext = Class.forName("org.apache.logging.log4j.core.LoggerContext");
                loggerContext.getMethod("setConfigLocation", URI.class).invoke(context, resource);
                return true;
            } catch (Throwable e) {
                if (!(e instanceof ClassNotFoundException)) {
                    throw new PluginLoaderException(e.getMessage(), e);
                }

                return false;
            }
        }

        return false;
    }

    @Override
    public List<Module> load() {
        return List.of(this);
    }

    @Override
    public void config(ServletConfig config) {

    }

    @Override
    public void destroy() {

    }

}
