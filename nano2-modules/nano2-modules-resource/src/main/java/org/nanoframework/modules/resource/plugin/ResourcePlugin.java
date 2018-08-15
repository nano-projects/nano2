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
package org.nanoframework.modules.resource.plugin;

import org.nanoframework.beans.Globals;
import org.nanoframework.modules.resource.annotation.Resource;
import org.nanoframework.spi.def.Plugin;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.scan.ClassScanner;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class ResourcePlugin implements Plugin {

    @Override
    public boolean load() {
        var injector = Globals.get(Injector.class);
        ClassScanner.filter(Resource.class).forEach(cls -> {
            var resource = cls.getAnnotation(Resource.class);
            if (!resource.lazy()) {
                var name = resource.value();
                if (StringUtils.isNotBlank(name)) {
                    injector.getInstance(Key.get(cls, Names.named(name)));
                } else {
                    injector.getInstance(cls);
                }
            }
        });

        return true;
    }

    @Override
    public void destroy() {

    }

}
