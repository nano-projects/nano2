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
package org.nanoframework.core.boot;

import java.util.ArrayList;
import java.util.Collections;
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
import org.nanoframework.spi.annotation.Child;
import org.nanoframework.spi.def.Module;
import org.nanoframework.spi.def.Plugin;
import org.nanoframework.spi.def.exception.PluginLoaderException;
import org.nanoframework.spi.support.SPILoader;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.properties.PropertiesLoader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class BootLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootLoader.class);

    /** */
    protected ServletConfig config;

    /** */
    protected ServletContext context;

    private Map<Integer, List<Module>> modules = Maps.newHashMap();

    private List<Plugin> plugins = Lists.newArrayList();

    /**
     * @param servlet HttpServlet
     */
    public void init(HttpServlet servlet) {
        init(servlet.getServletConfig(), servlet.getServletContext());
    }

    /**
     * @param config ServletConfig
     */
    public void init(ServletConfig config) {
        init(config, null);
    }

    /**
     * @param config ServletConfig
     * @param context ServletContext
     */
    public void init(ServletConfig config, ServletContext context) {
        this.config = config;
        this.context = context;

        try {
            initProperties();
            initRootInjector();
            initModules();
            initPlugins();
            addShutdownHook();
        } catch (Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }
    }

    /**
     * 初始化基本属性配置.
     */
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
            LOGGER.warn(e.getMessage());
        }

        LOGGER.info("加载主配置完成, 耗时: {}ms", System.currentTimeMillis() - time);
    }

    /**
     * 初始化根依赖注入.
     */
    protected void initRootInjector() {
        var injector = Guice.createInjector();
        Globals.set(Injector.class, injector);
        Globals.set(Injector.class, injector.createChildInjector(new SPIModule()));
    }

    /**
     * 加载模块.
     * @throws Throwable 加载模块异常
     */
    protected void initModules() throws Throwable {
        var moduleNames = SPILoader.spiNames(Module.class);
        if (!CollectionUtils.isEmpty(moduleNames)) {
            var injector = Globals.get(Injector.class);
            moduleNames.forEach(moduleName -> {
                var module = injector.getInstance(Key.get(Module.class, Names.named(moduleName)));
                var isChild = module.getClass().isAnnotationPresent(Child.class);
                if (isChild) {
                    addModules(modules, 1, module);
                } else {
                    addModules(modules, 0, module);
                }
            });

            loadModules(modules);
        }
    }

    private void addModules(Map<Integer, List<Module>> modules, Integer child, Module module) {
        if (modules.containsKey(child)) {
            modules.get(child).add(module);
        } else {
            modules.put(child, Lists.newArrayList(module));
        }
    }

    private void loadModules(Map<Integer, List<Module>> loadingModules) throws Throwable {
        var keys = loadingModules.keySet().stream().collect(Collectors.toList());
        Collections.sort(keys);
        for (var key : keys) {
            var modules = loadingModules.get(key);
            if (!CollectionUtils.isEmpty(modules)) {
                var mdus = new ArrayList<Module>();
                for (var module : modules) {
                    module.config(config);
                    mdus.addAll(module.load());
                }

                if (!CollectionUtils.isEmpty(mdus)) {
                    if (key.intValue() == 0) {
                        mdus.add(0, new SPIModule());
                        Globals.set(Injector.class, Guice.createInjector(mdus));
                    } else {
                        Globals.set(Injector.class, Globals.get(Injector.class).createChildInjector(mdus));
                    }
                }
            }
        }
    }

    /**
     * 加载插件.
     * @throws Throwable 加载插件异常
     */
    protected void initPlugins() throws Throwable {
        var pluginNames = SPILoader.spiNames(Plugin.class);
        if (CollectionUtils.isNotEmpty(pluginNames)) {
            var injector = Globals.get(Injector.class);
            for (var pluginName : pluginNames) {
                var plugin = injector.getInstance(Key.get(Plugin.class, Names.named(pluginName)));
                plugin.config(config);
                if (plugin.load()) {
                    LOGGER.info("Loading Plugin: {}", plugin.getClass().getName());
                }

                plugins.add(plugin);
            }
        }
    }

    private void addShutdownHook() {
        var shutdown = new Thread(() -> destroy());
        shutdown.setName("BootLoader-ShutdownHook-Thread");
        Runtime.getRuntime().addShutdownHook(shutdown);
    }

    public void destroy() {
        for (var modules : this.modules.values()) {
            if (CollectionUtils.isNotEmpty(modules)) {
                for (var module : modules) {
                    module.destroy();
                }
            }

            modules.clear();
        }

        for (var plugin : plugins) {
            plugin.destroy();
        }

        this.modules.clear();
        plugins.clear();
    }
}
