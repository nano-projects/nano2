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
package org.nanoframework.core.rest.tests;

import java.util.Map;

import org.nanoframework.core.rest.annotation.Body;
import org.nanoframework.core.rest.annotation.Param;
import org.nanoframework.core.rest.annotation.PathVariable;
import org.nanoframework.core.rest.annotation.Restful;
import org.nanoframework.core.rest.annotation.Route;
import org.nanoframework.core.rest.enums.HttpType;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Restful
@Route("/test")
public class RouteService {

    @Route(value = "/hello", type = HttpType.GET)
    public String hello() {
        return "world";
    }

    @Route(value = "/say", type = HttpType.GET)
    public String say(@Param("name") String name) {
        return "say " + name;
    }

    @Route(value = "/say/{id}", type = HttpType.GET)
    public String say(@PathVariable("id") String id, @Param("name") String name) {
        return "say " + id + name;
    }

    @Route(value = "/say/{id}", type = HttpType.POST)
    public String say(@PathVariable("id") String id, @Param("name") String name, @Body Map<String, String> map) {
        return "say " + id + name + map.toString();
    }
}
