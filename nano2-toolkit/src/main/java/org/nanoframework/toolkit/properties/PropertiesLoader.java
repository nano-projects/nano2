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
package org.nanoframework.toolkit.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.nanoframework.toolkit.consts.Charsets;
import org.nanoframework.toolkit.io.support.ClassPathResource;
import org.nanoframework.toolkit.lang.ResourceUtils;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.properties.exception.LoaderException;

/**
 * 属性文件操作公有类，负责对属性文件进行读写操作.
 * @author yanghe
 * @since 1.0
 */
public final class PropertiesLoader {
    /**
     * 属性文件集合.
     */
    public static final Map<String, Properties> PROPERTIES = new HashMap<>();

    private static final String REGEX = ";";

    private static final String CONTEXT = "context";

    private PropertiesLoader() {

    }

    /**
     * 根据路径加载属性文件.
     * @param path 属性文件路径
     * @return Properties
     */
    public static Properties load(String path) {
        try {
            InputStream input = null;
            try {
                input = new ClassPathResource(path).getInputStream();
            } catch (final IOException e) {
                // ignore
            }

            final Properties properties;
            if (input != null) {
                properties = PropertiesLoader.load(input);
            } else {
                properties = PropertiesLoader.load(ResourceUtils.getFile(path));
            }

            return properties;
        } catch (IOException e) {
            throw new LoaderException("加载属性文件异常: " + e.getMessage(), e);
        }
    }

    /**
     * 通过输入流加载属性文件.
     * @param input 文件输入流
     * @return 返回加载后的Properties
     */
    public static Properties load(InputStream input) {
        if (input == null) {
            throw new LoaderException("输入流为空");
        }

        try (var reader = new InputStreamReader(input, Charsets.UTF_8)) {
            var prop = new Properties();
            prop.load(reader);
            return prop;
        } catch (IOException e) {
            throw new LoaderException("加载属性文件异常: " + e.getMessage());
        }
    }

    /**
     * 通过文件加载属性文件.
     * @param file 输入文件
     * @return 返回加载后的Properties
     * @throws LoaderException Loader异常
     * @throws IOException IO异常
     */
    private static Properties load(File file) throws LoaderException, IOException {
        if (file == null) {
            throw new LoaderException("文件对象为空");
        }

        var prop = new Properties();
        try (var reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
            prop.load(reader);
        }

        return prop;
    }

    /**
     * 加载属性文件.
     * @param contextPath 文件相对路径
     * @param loadContext 是否加载context
     * @throws LoaderException 加载异常
     * @throws IOException IO异常
     */
    public static void load(String contextPath, boolean loadContext) throws LoaderException, IOException {
        var prop = load(contextPath);
        prop.forEach((key, value) -> System.setProperty((String) key, (String) value));
        PROPERTIES.put(contextPath, prop);

        if (loadContext) {
            var context = prop.getProperty(CONTEXT);
            if (StringUtils.isNotEmpty(context)) {
                var ctxs = context.split(REGEX);
                if (ctxs.length > 0) {
                    Arrays.stream(ctxs).filter(StringUtils::isNotBlank).forEach(ctx -> {
                        var properties = load(ctx);
                        if (properties != null) {
                            PROPERTIES.put(ctx, properties);
                        } else {
                            throw new LoaderException(ctx + ": 无法加载此属性文件!");
                        }
                    });
                }
            }
        }
    }

}
