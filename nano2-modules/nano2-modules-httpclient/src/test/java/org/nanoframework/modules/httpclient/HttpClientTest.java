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
package org.nanoframework.modules.httpclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nanoframework.beans.Globals;
import org.nanoframework.core.rest.enums.HttpType;
import org.nanoframework.modules.base.module.TypeListenerModule;
import org.nanoframework.modules.httpclient.annotation.Http;
import org.nanoframework.modules.httpclient.config.HttpConfigure;
import org.nanoframework.modules.httpclient.support.HttpClientImpl;
import org.nanoframework.modules.httpclient.test.TestHttpClientImpl;
import org.nanoframework.spi.SPIModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author yanghe
 * @since 1.3.10
 */
public class HttpClientTest {

    @Http
    private HttpClient client;

    @Http(spi = "test")
    private HttpClient testClient;

    @BeforeAll
    static void initRootInjector() {
        var injector = Guice.createInjector();
        Globals.set(Injector.class, injector);
        Globals.set(Injector.class, injector.createChildInjector(new SPIModule()));
    }

    @AfterAll
    static void destory() throws IOException {
        Globals.remove(Injector.class);
        HttpConfigure.clear();
    }

    @Test
    public void httpGetTest() throws IOException {
        var httpClient = Guice.createInjector().getInstance(HttpClient.class);
        var response = httpClient.process(HttpType.GET, "https://www.baidu.com");
        assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    public void generalHttpTest() throws IOException {
        var c1 = new HttpConfigure().get();
        var c2 = new HttpConfigure().get();
        assertTrue(c1 == c2);
    }

    @Test
    public void fieldInjectTest() {
        var injector = Guice.createInjector(new TypeListenerModule());
        injector.injectMembers(this);
        assertNotNull(client);
        assertTrue(client instanceof HttpClientImpl);

        assertNotNull(testClient);
        assertTrue(testClient instanceof TestHttpClientImpl);
    }
}
