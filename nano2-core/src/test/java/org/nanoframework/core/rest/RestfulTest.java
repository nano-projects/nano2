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
package org.nanoframework.core.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nanoframework.core.plugins.PluginLoader;
import org.nanoframework.core.rest.enums.HttpType;
import org.nanoframework.core.web.http.URLContext;
import org.nanoframework.toolkit.scan.ClassScanner;

/**
 * @author yanghe
 * @since 2.0.0
 */
class RestfulTest {

    @BeforeAll
    static void setup() {
        ClassScanner.scan(RestfulTest.class);
        new PluginLoader().init(new ServletConfig() {

            @Override
            public String getServletName() {
                return null;
            }

            @Override
            public ServletContext getServletContext() {
                return null;
            }

            @Override
            public String getInitParameter(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        });
    }

    @Test
    void invokeTest() {
        var route = Routes.route();
        var url = "/test/hello";
        var mapper = route.lookup(url, HttpType.GET);
        assertNotNull(mapper);
        assertEquals(route.invoke(mapper), "world");

        var mapper1 = route.lookup(url, HttpType.POST);
        assertNull(mapper1);

        var context = URLContext.of("/test/say?name=abc");
        var mapper2 = route.lookup(context.getContext(), HttpType.GET);
        assertNotNull(mapper2);
        assertEquals("say abc", route.invoke(mapper2, context.getParameter()));

        var context1 = URLContext.of("/test/say/123?name=abc");
        var mapper3 = route.lookup(context1.getContext(), HttpType.GET);
        assertNotNull(mapper3);
        assertEquals("say 123abc", route.invoke(mapper3, context1.getParameter()));

    }
}
