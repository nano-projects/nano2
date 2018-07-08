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
package org.nanoframework.core.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.nanoframework.beans.Globals;
import org.nanoframework.core.config.InitParameter;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.spi.SPIModule;
import org.nanoframework.spi.annotation.Level;
import org.nanoframework.spi.def.Module;
import org.nanoframework.spi.def.Plugin;
import org.nanoframework.spi.def.exception.PluginLoaderException;
import org.nanoframework.spi.support.SPILoader;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.properties.PropertiesLoader;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class PluginLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class);

    protected ServletConfig config;

    protected ServletContext context;

    public void init(HttpServlet servlet) {
        init(servlet.getServletConfig(), servlet.getServletContext());
    }

    public void init(ServletConfig config) {
        init(config, null);
    }

    public void init(ServletConfig config, ServletContext context) {
        this.config = config;
        this.context = context;

        try {
            initProperties();
            initRootInjector();
            initModules();
            initPlugins();
        } catch (Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }
    }

    protected void initProperties() {
        var time = System.currentTimeMillis();
        try {
            var context = config.getInitParameter(InitParameter.CONTEXT);
            if (StringUtils.isNotBlank(context)) {
                PropertiesLoader.loadContext(context);
            } else {
                PropertiesLoader.loadContext(InitParameter.DEFAULT_CONTEXT);
            }
        } catch (Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }

        LOGGER.info("加载主配置完成, 耗时: {}ms", System.currentTimeMillis() - time);
    }

    protected void initRootInjector() {
        var injector = Guice.createInjector();
        Globals.set(Injector.class, injector);
        Globals.set(Injector.class, injector.createChildInjector(new SPIModule()));
    }

    protected void initModules() throws Throwable {
        var moduleNames = SPILoader.spiNames(Module.class);
        if (!CollectionUtils.isEmpty(moduleNames)) {
            var injector = Globals.get(Injector.class);
            var modules = new HashMap<Integer, List<Module>>();
            moduleNames.forEach(moduleName -> {
                var module = injector.getInstance(Key.get(Module.class, Names.named(moduleName)));
                var level = module.getClass().getAnnotation(Level.class);
                if (level != null) {
                    addModules(modules, level.value(), module);
                } else {
                    addModules(modules, 0, module);
                }
            });

            loadModules(modules);
        }
    }

    protected void addModules(Map<Integer, List<Module>> modules, Integer level, Module module) {
        if (modules.containsKey(level)) {
            modules.get(level).add(module);
        } else {
            modules.put(level, Lists.newArrayList(module));
        }
    }

    protected void loadModules(Map<Integer, List<Module>> loadingModules) throws Throwable {
        var levels = loadingModules.keySet().stream().collect(Collectors.toList());
        Collections.sort(levels);
        for (var level : levels) {
            var modules = loadingModules.get(level);
            if (!CollectionUtils.isEmpty(modules)) {
                var mdus = new ArrayList<Module>();
                for (var module : modules) {
                    module.config(config);
                    mdus.addAll(module.load());
                }

                if (!CollectionUtils.isEmpty(mdus)) {
                    if (level.intValue() == 0) {
                        mdus.add(0, new SPIModule());
                        Globals.set(Injector.class, Guice.createInjector(mdus));
                    } else {
                        Globals.set(Injector.class, Globals.get(Injector.class).createChildInjector(mdus));
                    }
                }
            }
        }
    }

    protected void initPlugins() throws Throwable {
        var pluginNames = SPILoader.spiNames(Plugin.class);
        if (!CollectionUtils.isEmpty(pluginNames)) {
            var injector = Globals.get(Injector.class);
            for (var pluginName : pluginNames) {
                var plugin = injector.getInstance(Key.get(Plugin.class, Names.named(pluginName)));
                plugin.config(config);
                if (plugin.load()) {
                    LOGGER.info("Loading Plugin: {}", plugin.getClass().getName());
                }
            }
        }
    }
}
