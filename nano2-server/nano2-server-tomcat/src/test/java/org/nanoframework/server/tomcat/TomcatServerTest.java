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
package org.nanoframework.server.tomcat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nanoframework.beans.Globals;
import org.nanoframework.core.web.http.HttpStatusCode;
import org.nanoframework.modules.httpclient.annotation.Http;
import org.nanoframework.toolkit.scan.ClassScanner;
import org.nanoframework.toolkit.scan.annotation.Scan;

import com.alibaba.fastjson.JSON;
import com.google.inject.Injector;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Scan
class TomcatServerTest {

    @Http
    private org.nanoframework.modules.httpclient.HttpClient client;

    @BeforeAll
    static void setup() throws Throwable {
        var server = TomcatServer.server(TomcatServerTest.class);
        server.bootstrap("start");
        Thread.sleep(5000);
    }

    @Test
    void bootTest() {
        Globals.get(Injector.class).injectMembers(this);
        var res = client.post("http://localhost:7000", Map.of("a", "b"));
        assertNotNull(res);
        assertEquals(res.getStatusCode(), HttpStatusCode.SC_OK);
        assertEquals(res.getBody(), "\n\nOK");
        assertEquals(ClassScanner.loadedClassSize(), 8);
    }

    @Test
    void bodyTest() {
        Globals.get(Injector.class).injectMembers(this);
        var res = client.post("http://localhost:7000/test/say/123?name=abc",
                JSON.toJSONString(Map.of("value", "body")));
        assertNotNull(res);
        assertEquals(res.getStatusCode(), HttpStatusCode.SC_OK);
        assertEquals(res.getBody(), "say 123abcbody");

        res = client.post("http://localhost:7000/test/say/234/123?name=abc",
                JSON.toJSONString(Map.of("value", "body")));
        assertNotNull(res);
        assertEquals(res.getStatusCode(), HttpStatusCode.SC_OK);
        assertEquals(res.getBody(), "say 234abcbody");
    }

    @Test
    void voidTest() {
        Globals.get(Injector.class).injectMembers(this);
        var res = client.get("http://localhost:7000/test");
        assertNotNull(res);
        assertEquals(res.getStatusCode(), HttpStatusCode.SC_OK);
    }
}
