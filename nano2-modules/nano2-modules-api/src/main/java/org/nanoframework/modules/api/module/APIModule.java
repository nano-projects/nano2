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
package org.nanoframework.modules.api.module;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;

import org.nanoframework.modules.api.annotation.API;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.spi.def.Module;
import org.nanoframework.toolkit.lang.ArrayUtils;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.scan.ClassScanner;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 1.4.2
 */
@SuppressWarnings({"unchecked", "rawtypes" })
public class APIModule implements Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(APIModule.class);

    @Override
    public void configure(Binder binder) {
        bind(binder);
    }

    protected void bind(Binder binder) {
        var bindMap = new HashMap<Class, List<Class>>();
        ClassScanner.filter(API.class).forEach(cls -> {
            if (cls.isInterface()) {
                LOGGER.warn("Ignore interface API of {}", cls.getName());
                return;
            }

            var itfs = cls.getInterfaces();
            if (ArrayUtils.isEmpty(itfs)) {
                LOGGER.warn("Ignore no interface implement API of {}", cls.getName());
                return;
            }

            Arrays.stream(itfs).forEach(itf -> {
                var implList = bindMap.get(itf);
                if (implList == null) {
                    implList = Lists.newArrayList();
                    bindMap.put(itf, implList);
                }

                implList.add(cls);
            });
        });

        bind(binder, bindMap);
    }

    protected void bind(Binder binder, Map<Class, List<Class>> bindMap) {
        bindMap.forEach((itf, impls) -> {
            if (impls.size() == 1) {
                var cls = impls.get(0);
                var apiName = ((API) cls.getAnnotation(API.class)).value();
                if (StringUtils.isNotBlank(apiName)) {
                    binder.bind(itf).annotatedWith(Names.named(apiName)).to(cls);
                } else {
                    binder.bind(itf).to(cls);
                }

                LOGGER.debug("Binding {} to {}", itf.getName(), cls.getName());
            } else {
                bindWithName(binder, itf, impls);
            }
        });
    }

    protected void bindWithName(Binder binder, Class itf, List<Class> impls) {
        impls.forEach(cls -> {
            var apiName = ((API) cls.getAnnotation(API.class)).value();
            String name;
            if (StringUtils.isNotBlank(apiName)) {
                name = apiName;
            } else {
                var clsName = cls.getSimpleName();
                name = clsName.substring(0, 1).toLowerCase() + clsName.substring(1, clsName.length());
            }

            binder.bind(itf).annotatedWith(Names.named(name)).to(cls);
            LOGGER.debug("Binding {} to {} with name {}", itf.getName(), cls.getName(), name);
        });
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
