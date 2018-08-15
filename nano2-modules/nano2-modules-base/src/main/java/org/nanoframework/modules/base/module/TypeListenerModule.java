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
package org.nanoframework.modules.base.module;

import java.io.IOException;
import java.util.List;

import org.nanoframework.beans.Globals;
import org.nanoframework.modules.base.listener.CloseableTypeListener;
import org.nanoframework.spi.annotation.Order;
import org.nanoframework.spi.def.Module;
import org.nanoframework.spi.support.SPILoader;
import org.nanoframework.toolkit.lang.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Order(-9998)
public class TypeListenerModule implements Module {
    private List<CloseableTypeListener> listeners = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    @Override
    public void configure(Binder binder) {
        var spis = SPILoader.spis(CloseableTypeListener.class);
        if (CollectionUtils.isNotEmpty(spis)) {
            var injector = Globals.get(Injector.class);
            spis.forEach(spi -> {
                var listener = (CloseableTypeListener) injector.getInstance(spi.getInstance());
                binder.bindListener(Matchers.any(), listener);
                listeners.add(listener);
            });
        }
    }

    @Override
    public List<Module> load() {
        return List.of(this);
    }

    @Override
    public void destroy() {
        for (var listener : listeners) {
            try {
                listener.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
