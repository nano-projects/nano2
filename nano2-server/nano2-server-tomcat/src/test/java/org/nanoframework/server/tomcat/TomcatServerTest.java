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

import java.net.URI;

import org.junit.jupiter.api.Test;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse.BodyHandler;

/**
 * @author yanghe
 * @since 2.0.0
 */
class TomcatServerTest {

    @Test
    void bootTest() throws Throwable {
        var server = TomcatServer.server();
        server.bootstrap("start");
        Thread.sleep(1000);
        var res = HttpClient.newHttpClient().send(HttpRequest.newBuilder(new URI("http://localhost:7000")).build(),
                BodyHandler.asString());
        assertNotNull(res);
        assertEquals(res.statusCode(), 200);
        assertEquals(res.body(), "\n\nOK");
    }
}
