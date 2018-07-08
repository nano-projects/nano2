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
package org.nanoframework.core.rest.plugin;

import javax.servlet.ServletConfig;

import org.nanoframework.beans.Globals;
import org.nanoframework.core.rest.Routes;
import org.nanoframework.core.rest.annotation.RequestMapping;
import org.nanoframework.core.rest.annotation.Restful;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.spi.def.Plugin;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.scan.ClassScanner;

import com.google.inject.Injector;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class RestfulPlugin implements Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulPlugin.class);

    @Override
    public boolean load() throws Throwable {
        var classes = ClassScanner.filter(Restful.class);
        LOGGER.info("Restful API size: {}", classes.size());

        if (CollectionUtils.isNotEmpty(classes)) {
            var injector = Globals.get(Injector.class);
            classes.forEach(cls -> {
                LOGGER.info("Inject Restful API Class: {}", cls.getName());
                var instance = injector.getInstance(cls);
                var methods = cls.getMethods();
                var mapping = cls.isAnnotationPresent(RequestMapping.class)
                        ? cls.getAnnotation(RequestMapping.class).value()
                        : StringUtils.EMPTY;
                var mappers = Routes.route().matchers(instance, methods, RequestMapping.class, mapping);
                mappers.forEach((url, mapper) -> Routes.route().register(url, mapper));
            });
        }

        return false;
    }

    @Override
    public void config(ServletConfig config) throws Throwable {

    }

}
