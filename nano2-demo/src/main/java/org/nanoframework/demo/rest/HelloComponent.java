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
package org.nanoframework.demo.rest;

import org.nanoframework.core.rest.annotation.Mock;
import org.nanoframework.core.rest.annotation.Param;
import org.nanoframework.core.rest.annotation.Restful;
import org.nanoframework.core.rest.annotation.Route;
import org.nanoframework.core.rest.enums.HttpType;
import org.nanoframework.modules.config.annotation.Value;
import org.nanoframework.modules.sentinel.annotation.Sentinel;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Restful
@Route("/v1/hello")
@Slf4j
public class HelloComponent {

    @Value("nano2-demo.hello.value")
    private String value;

    private String value1;

    @Value("nano2-demo.hello.value")
    private void setValueOfMethod(String value) {
        this.value1 = value;
    }

    @Route(type = HttpType.GET)
    @Sentinel
    @Mock
    public Object hello() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }

        return "Hello World, " + value + ", " + value1;
    }

    @Route(value = "/timeout", type = HttpType.GET, timeout = 1000)
    @Sentinel
    @Mock
    public Object timeout(@Param("time") Long time) throws InterruptedException {
        try {
            Thread.sleep(time);
            return "timeout test";
        } catch (Throwable e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}
