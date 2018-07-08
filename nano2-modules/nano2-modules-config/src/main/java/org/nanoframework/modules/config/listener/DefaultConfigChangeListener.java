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
package org.nanoframework.modules.config.listener;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nanoframework.beans.format.ClassCast;
import org.nanoframework.logging.Logger;
import org.nanoframework.logging.LoggerFactory;
import org.nanoframework.modules.config.ConfigMapper;
import org.nanoframework.toolkit.lang.CollectionUtils;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class DefaultConfigChangeListener implements ConfigChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigChangeListener.class);

    private static final Map<String, Map<String, List<ConfigMapper>>> MAPPER = Maps.newConcurrentMap();

    @Override
    public void onChange(ConfigChangeEvent event) {
        var keys = event.changedKeys();
        if (CollectionUtils.isNotEmpty(keys)) {
            keys.stream().filter(MAPPER::containsKey).forEach(key -> {
                var ns = MAPPER.get(key);
                var change = event.getChange(key);
                var cms = ns.get(change.getNamespace());
                if (CollectionUtils.isNotEmpty(cms)) {
                    change(cms, change.getNewValue());
                }
            });
        }
    }

    private void change(List<ConfigMapper> cms, String value) {
        cms.forEach(cm -> {
            var field = cm.getField();
            var fieldType = field.getType().getName();
            try {
                field.set(cm.getInstance(), ClassCast.cast(value, fieldType));
                LOGGER.debug("配置变更通知: key = {}, value = {}", cm.getKey(), value);
            } catch (Throwable e) {
                LOGGER.error(String.format("设置配置异常: %s", e.getMessage()), e);
            }
        });
    }

    public static void add(ConfigMapper cm) {
        var key = cm.getKey();
        if (!MAPPER.containsKey(key)) {
            MAPPER.put(key, Maps.newConcurrentMap());
        }

        var ns = MAPPER.get(key);
        var namespace = cm.getNamespace();
        if (!ns.containsKey(namespace)) {
            ns.put(namespace, Collections.synchronizedList(Lists.newArrayList()));
        }

        ns.get(namespace).add(cm);
    }
}
