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
package org.nanoframework.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.nanoframework.config.annotation.Value;
import org.nanoframework.config.module.ConfigModule;

import com.google.inject.Guice;
import com.google.inject.ProvisionException;

/**
 * 单元测试需要依赖Apollo服务，可以从Github上clone Quick Start的资源库来快速搭建环境，步骤如下：
 * 
 * <pre>
 * 资源仓库：{@link https://github.com/nobodyiam/apollo-build-scripts}
 * 需要按照文档，预先配置好MySQL库
 * ~$ git clone git@github.com:nobodyiam/apollo-build-scripts.git
 * ~$ cd apollo-build-scripts
 * ~$ ./demo.sh &
 * </pre>
 * 
 * @author yanghe
 * @since 2.0.0
 */
class ConfigTest {

    @Value("nano2.config.test.key")
    private String key;

    @Value(value = "nano2.config.test.public.key", namespace = "TEST1.BC")
    private Map<String, String> map;

    @Value(value = "nano2.config.test.def-value", defaultValue = "is default value")
    private String defVaule;

    @Value(value = "nano2.config.test.def-value", defaultValue = "is default value", required = true)
    private String defVaule2;

    @Value(value = "nano2.config.test.notfound", required = true)
    private String notfound;

    @Test
    void loadConfigTest() throws InterruptedException {
        var injector = Guice.createInjector(new ConfigModule());
        assertThrows(ProvisionException.class, () -> injector.injectMembers(this));
        assertNotNull(key);
        assertEquals(key, "hello world");

        assertNotNull(map);
        assertTrue(!map.isEmpty());
        assertEquals(map.get("hello"), "world");

        assertNotNull(defVaule);
        assertEquals(defVaule, "is default value");

        assertNotNull(defVaule2);
        assertEquals(defVaule2, "is default value");
    }
}
