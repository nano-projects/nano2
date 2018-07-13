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
package org.nanoframework.spi.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarFile;

import org.nanoframework.spi.exception.SPIException;
import org.nanoframework.toolkit.lang.ArrayUtils;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.ResourceUtils;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.properties.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author yanghe
 * @since 1.4.8
 */
public class SPILoader {
    private static Logger LOGGER = LoggerFactory.getLogger(SPILoader.class);

    private static String SPI_DIR = "META-INF/nano/spi";

    private static Map<Class<?>, List<SPIMapper>> SPI_MAPPERS = Maps.newHashMap();

    private static AtomicBoolean LOADED = new AtomicBoolean(false);

    private static ReentrantLock LOCK = new ReentrantLock();

    private static Set<JarFile> JAR_FILES = Sets.newHashSet();

    /** */
    protected SPILoader() {

    }

    private static void loading() {
        var lock = LOCK;
        SPILoader loader = null;
        Map<String, List<InputStream>> streams = null;
        try {
            lock.lock();
            loader = new SPILoader();
            Enumeration<URL> resources;
            try {
                resources = loader.getResources();
            } catch (Throwable e) {
                throw new SPIException("加载资源异常: " + e.getMessage(), e);
            }

            SPIResource spiResource;
            try {
                spiResource = loader.getSPIResource(resources);
            } catch (Throwable e) {
                throw new SPIException("获取SPI资源文件异常: " + e.getMessage(), e);
            }

            var spiMappers = new HashMap<Class<?>, List<SPIMapper>>();
            loader.getSPIMapper(spiResource.getFiles(), spiMappers);

            streams = spiResource.getStreams();
            loader.getSPIMapperWithStream(streams, spiMappers);
            loader.sortSPIMapper(spiMappers);

            SPI_MAPPERS.clear();
            SPI_MAPPERS.putAll(spiMappers);
            LOADED.set(true);
        } finally {
            if (loader != null) {
                if (streams != null) {
                    loader.closeStream(streams.values());
                }

                loader.closeJarFile();
            }

            lock.unlock();
        }
    }

    /**
     * @return 获取所有已被加载的SPI配置关系
     */
    public static Map<Class<?>, List<SPIMapper>> spis() {
        if (!LOADED.get()) {
            loading();
        }

        return Collections.unmodifiableMap(SPI_MAPPERS);
    }

    /**
     * @param cls Class
     * @return 获取指定Class对应的所有已被加载的SPI配置信息
     */
    public static List<SPIMapper> spis(Class<?> cls) {
        var mappers = spis().get(cls);
        if (CollectionUtils.isEmpty(mappers)) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(mappers);
    }

    /**
     * @param spiCls Class
     * @return 获取指定Class对应的所有的已被加载的SPI类名信息
     */
    public static Set<String> spiNames(Class<?> spiCls) {
        if (!LOADED.get()) {
            loading();
        }

        var spiMappers = SPI_MAPPERS.get(spiCls);
        if (!CollectionUtils.isEmpty(spiMappers)) {
            var spiNames = new LinkedHashSet<String>();
            spiMappers.forEach(spiMapper -> spiNames.add(spiMapper.getName()));
            return Collections.unmodifiableSet(spiNames);
        }

        return Collections.emptySet();
    }

    /**
     * @return 获取SPI资源配置文件信息
     * @throws IOException 读取SPI资源异常
     */
    protected Enumeration<URL> getResources() throws IOException {
        var loader = SPILoader.class.getClassLoader();
        if (loader != null) {
            return loader.getResources(SPI_DIR);
        } else {
            return ClassLoader.getSystemResources(SPI_DIR);
        }
    }

    /**
     * @param resources SPI资源配置文件信息
     * @return SPI资源
     * @throws URISyntaxException URI格式异常
     * @throws MalformedURLException URL格式异常
     * @throws IOException SPI信息读取异常
     */
    protected SPIResource getSPIResource(Enumeration<URL> resources)
            throws URISyntaxException, MalformedURLException, IOException {
        if (resources != null) {
            var files = new ArrayList<File>();
            var streams = new HashMap<String, List<InputStream>>();
            while (resources.hasMoreElements()) {
                var url = resources.nextElement();
                if (!ResourceUtils.isJarURL(url)) {
                    findSPIFiles(url, files);
                } else {
                    findSPIFilesWithJar(url, streams);
                }
            }

            return SPIResource.create(files, streams);
        }

        return SPIResource.EMPTY;
    }

    private void findSPIFiles(URL url, List<File> files) throws URISyntaxException {
        var uri = url.toURI();
        File file;
        try {
            file = new File(uri);
        } catch (Throwable e) {
            LOGGER.error("无效的文件路径: {}", uri);
            return;
        }

        if (file.exists()) {
            var spiFiles = file.listFiles(f -> !f.isDirectory());
            if (ArrayUtils.isNotEmpty(spiFiles)) {
                Arrays.stream(spiFiles).forEach(files::add);
            }
        }
    }

    private void findSPIFilesWithJar(URL url, Map<String, List<InputStream>> streams)
            throws FileNotFoundException, MalformedURLException, IOException {
        var jarFile = new JarFile(ResourceUtils.getFile(ResourceUtils.extractJarFileURL(url)));
        JAR_FILES.add(jarFile);
        var entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            var entry = entries.nextElement();
            var fileName = entry.getName().replace('\\', '/');
            if (StringUtils.startsWith(fileName, SPI_DIR) && !entry.isDirectory()) {
                var fileNameSection = fileName.split("/");
                var spiFileName = fileNameSection[fileNameSection.length - 1];
                if (!streams.containsKey(spiFileName)) {
                    streams.put(spiFileName, Lists.newArrayList(jarFile.getInputStream(entry)));
                } else {
                    streams.get(spiFileName).add(jarFile.getInputStream(entry));
                }
            }
        }
    }

    private void getSPIMapper(List<File> spiFiles, Map<Class<?>, List<SPIMapper>> spiMappers) {
        if (!CollectionUtils.isEmpty(spiFiles)) {
            spiFiles.forEach(file -> {
                Properties define = PropertiesLoader.load(file.getAbsolutePath());
                bindSPI(define, file.getName(), spiMappers);
            });
        }
    }

    private void getSPIMapperWithStream(Map<String, List<InputStream>> stream,
            Map<Class<?>, List<SPIMapper>> spiMappers) {
        stream.entrySet().forEach(entry -> {
            var spiClsName = entry.getKey();
            var spiStreams = entry.getValue();
            if (!CollectionUtils.isEmpty(spiStreams)) {
                spiStreams.forEach(spiStream -> {
                    Properties define = PropertiesLoader.load(spiStream);
                    bindSPI(define, spiClsName, spiMappers);
                });
            }
        });
    }

    private void bindSPI(Properties define, String spiClsName, Map<Class<?>, List<SPIMapper>> spiMappers) {
        var spiCls = getSPIClass(spiClsName);
        if (spiCls == null) {
            return;
        }

        define.keySet().forEach(name -> {
            var spiName = (String) name;
            var instanceClsName = define.getProperty(spiName);
            try {
                Class<?> instanceCls;
                if (StringUtils.isNotBlank(instanceClsName)) {
                    instanceCls = Class.forName(instanceClsName);
                } else {
                    instanceCls = Class.forName(spiName);
                    spiName = instanceCls.getSimpleName();
                    LOGGER.debug("默认SPI定义: {} = {}", spiName, instanceCls.getName());
                }

                if (spiCls.isAssignableFrom(instanceCls)) {
                    var spiMapper = SPIMapper.create(spiCls, spiName, instanceCls);
                    if (!spiMappers.containsKey(spiCls)) {
                        spiMappers.put(spiCls, Lists.newArrayList(spiMapper));
                    } else {
                        spiMappers.get(spiCls).add(spiMapper);
                    }
                } else {
                    LOGGER.warn("无法加载类: {}, 未实现接口 {}", instanceClsName, spiClsName);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.warn("未定义SPI实现类: {} = {}", name, instanceClsName);
            }
        });
    }

    private Class<?> getSPIClass(String spiClsName) {
        try {
            return Class.forName(spiClsName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private void sortSPIMapper(Map<Class<?>, List<SPIMapper>> spiMappers) {
        spiMappers.forEach((spiCls, spis) -> {
            Collections.sort(spis, (before, after) -> {
                var beforeOrder = before.getOrder();
                var afterOrder = after.getOrder();
                if (beforeOrder > afterOrder) {
                    return 1;
                } else if (beforeOrder < afterOrder) {
                    return -1;
                }

                return 0;
            });
        });
    }

    private void closeStream(Collection<List<InputStream>> streams) {
        if (!CollectionUtils.isEmpty(streams)) {
            streams.forEach(spiStream -> {
                spiStream.forEach(stream -> {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        LOGGER.warn("关闭JarEntry资源异常: {}", e.getMessage());
                    }
                });
            });
        }
    }

    private void closeJarFile() {
        if (!CollectionUtils.isEmpty(JAR_FILES)) {
            JAR_FILES.forEach(jarFile -> {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    LOGGER.warn("关闭JarFile资源异常: {}", e.getMessage());
                }
            });

            JAR_FILES.clear();
        }
    }
}
