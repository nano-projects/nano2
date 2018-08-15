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
package org.nanoframework.orm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nanoframework.beans.Globals;
import org.nanoframework.spi.annotation.Order;
import org.nanoframework.spi.def.Module;
import org.nanoframework.spi.def.exception.PluginLoaderException;
import org.nanoframework.spi.support.SPILoader;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.properties.PropertiesLoader;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 1.1
 */
@Order(2000)
public class DataSourceModule implements Module {

    @Override
    public List<Module> load() {
        var injector = Globals.get(Injector.class);
        var names = SPILoader.spiNames(DataSourceLoader.class);
        if (CollectionUtils.isNotEmpty(names)) {
            var modules = new ArrayList<Module>();
            names.forEach(name -> {
                var loader = injector.getInstance(Key.get(DataSourceLoader.class, Names.named(name)));
                try {
                    PropertiesLoader.PROPERTIES.putAll(loader.getProperties());
                    modules.addAll(loader.getModules());
                } catch (final Throwable e) {
                    if (!(e instanceof ClassNotFoundException)) {
                        throw new PluginLoaderException(e.getMessage(), e);
                    }
                }
            });

            return modules;
        }

        return Collections.emptyList();
    }

    @Override
    public void configure(final Binder binder) {

    }

    @Override
    public void destroy() {

    }

}
