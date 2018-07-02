/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.core.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.nanoframework.beans.Globals;
import org.nanoframework.core.spi.test.SpiLazyService;
import org.nanoframework.core.spi.test.SpiService;
import org.nanoframework.core.spi.test.impl.TestLazyServiceImpl;
import org.nanoframework.core.spi.test.impl.TestService2Impl;
import org.nanoframework.core.spi.test.impl.TestServiceImpl;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

/**
 * @author yanghe
 * @since 1.4.8
 */
class SPITest {
    private List<String> spiFiles = List.of("org.nanoframework.core.spi.test.SpiService",
            "org.nanoframework.core.spi.test.SpiLazyService", "org.nanoframework.core.spi.test.NotSpiService",
            "org.nanoframework.core.spi.test.SpiNotImplService");

    @Inject
    @Named("testService")
    private SpiService testService;

    @Inject
    @Named("TestService2Impl")
    private SpiService testService2;

    @Inject
    @Named("testLazyService")
    private SpiLazyService testLazyService;

    @Test
    void getResourcesTest() throws IOException, URISyntaxException {
        var loader = new SPILoader();
        var urls = loader.getResources();
        assertTrue(urls.hasMoreElements());
    }

    @Test
    void getSPIFilesTest() throws URISyntaxException, IOException {
        var loader = new SPILoader();
        var resource = loader.getSPIResource(loader.getResources());
        assertTrue(resource != SPIResource.EMPTY);
        var files = resource.getFiles();
        for (var spiFile : files) {
            // Test SPI file only.
            if (spiFile.getAbsolutePath().indexOf("test-classes") > -1) {
                assertTrue(spiFiles.contains(spiFile.getName()));
            }
        }

        var streams = resource.getStreams();
        assertTrue(streams.isEmpty());
    }

    @Test
    void spiTest() {
        var injector = Guice.createInjector();
        Globals.set(Injector.class, injector);
        try {
            injector.createChildInjector(new SPIModule(), binder -> binder.requestInjection(this));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        assertNotNull(testService);
        assertTrue(testService.getClass() == TestServiceImpl.class);
        assertEquals(testService.echo(), "Echo TestService");

        assertNotNull(testService2);
        assertTrue(testService2.getClass() == TestService2Impl.class);
        assertEquals(testService2.echo(), "Echo TestService 2");

        assertNotNull(testLazyService);
        assertTrue(testLazyService.getClass() == TestLazyServiceImpl.class);
        assertEquals(testLazyService.echo(), "Echo Lazy TestService");
    }
}
