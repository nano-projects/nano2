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
import org.nanoframework.core.plugins.exception.PluginLoaderException;
import org.nanoframework.core.properties.PropertiesLoader;
import org.nanoframework.core.spi.SPILoader;
import org.nanoframework.core.spi.SPIModule;
import org.nanoframework.core.spi.annotation.Level;
import org.nanoframework.logging.Logger;
import org.nanoframework.logging.LoggerFactory;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 2.0.0
 */
@SuppressWarnings("exports")
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
            initComponent();

        } catch (Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }
    }

    private void initProperties() {
        var time = System.currentTimeMillis();
        try {
            var context = config.getInitParameter(InitParameter.CONTEXT);
            if (StringUtils.isNotBlank(context)) {
                PropertiesLoader.load(context, true);
            } else {
                PropertiesLoader.load(InitParameter.DEFAULT_CONTEXT, true);
            }
        } catch (Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }

        LOGGER.info("加载主配置完成, 耗时: {}ms", System.currentTimeMillis() - time);
    }

    private void initRootInjector() {
        var injector = Guice.createInjector();
        Globals.set(Injector.class, injector);
        Globals.set(Injector.class, injector.createChildInjector(new SPIModule()));
    }

    private void initModules() throws Throwable {
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

    private void addModules(Map<Integer, List<Module>> modules, Integer level, Module module) {
        if (modules.containsKey(level)) {
            modules.get(level).add(module);
        } else {
            modules.put(level, Lists.newArrayList(module));
        }
    }

    private void loadModules(Map<Integer, List<Module>> loadingModules) throws Throwable {
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

    private void initPlugins() throws Throwable {
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

    private void initComponent() throws Throwable {
        var time = System.currentTimeMillis();
        LOGGER.info("Starting inject component");
        // Components.load();
        LOGGER.info("Inject Component complete, times: {}ms", System.currentTimeMillis() - time);
    }
}
