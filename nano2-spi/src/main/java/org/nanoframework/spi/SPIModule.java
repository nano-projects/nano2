/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.spi;

import java.util.List;

import javax.servlet.ServletConfig;

import org.nanoframework.beans.Globals;
import org.nanoframework.spi.annotation.Lazy;
import org.nanoframework.spi.def.Module;
import org.nanoframework.spi.support.SPILoader;
import org.nanoframework.spi.support.SPIProvider;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.name.Names;
/**
 * @author yanghe
 * @since 1.4.8
 */
public class SPIModule implements Module {
    private static Logger LOGGER = LoggerFactory.getLogger(SPIModule.class);

    @SuppressWarnings({"unchecked"})
    @Override
    public void configure(Binder binder) {
        var spiMappers = SPILoader.spis();
        if (!CollectionUtils.isEmpty(spiMappers)) {
            var injector = Globals.get(Injector.class);
            spiMappers.forEach((spiCls, spis) -> {
                if (!spiCls.isAnnotationPresent(Lazy.class)) {
                    spis.forEach(spi -> {
                        var spiClsName = spi.getSpiClsName();
                        var name = spi.getName();
                        var instanceClsName = spi.getInstanceClsName();
                        if (!spi.getLazy()) {
                            var instance = injector.getInstance(spi.getInstance());
                            binder.bind(spi.getSpi()).annotatedWith(Names.named(name)).toInstance(instance);
                            LOGGER.debug("绑定即时SPI, 接口定义: {}, 绑定名称: {}, 实现类: {}", spiClsName, name, instanceClsName);
                        } else {
                            binder.bind(spi.getSpi()).annotatedWith(Names.named(spi.getName()))
                                    .toProvider(new SPIProvider(spi));
                            LOGGER.debug("绑定延时SPI, 接口定义: {}, 绑定名称: {}, 实现类: {}", spiClsName, name, instanceClsName);
                        }
                    });
                } else {
                    spis.forEach(spi -> {
                        var spiClsName = spi.getSpiClsName();
                        var name = spi.getName();
                        var instanceClsName = spi.getInstanceClsName();
                        binder.bind(spi.getSpi()).annotatedWith(Names.named(spi.getName()))
                                .toProvider(new SPIProvider(spi));
                        LOGGER.debug("绑定延时SPI, 接口定义: {}, 绑定名称: {}, 实现类: {}", spiClsName, name, instanceClsName);
                    });
                }
            });
        }
    }

    @Override
    public List<Module> load() throws Throwable {
        return null;
    }

    @Override
    public void config(ServletConfig config) throws Throwable {

    }

}
