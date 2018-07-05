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
package org.nanoframework.toolkit.scan;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.scan.vfs.ResolverUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.NonNull;

/**
 * 扫描组件，并返回符合要求的集合
 * @author yanghe
 * @since 1.0
 */
public class ClassScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassScanner.class);

    private static Set<Class<?>> classes;

    /**
     * 返回所有带有参数中的注解的类
     * @param annotationClass 注解类
     * @return 过滤后的类
     */
    public static Set<Class<?>> filter(Class<? extends Annotation> annotationClass) {
        if (classes == null) {
            return Collections.emptySet();
        }

        if (classes.size() > 0) {
            var annClasses = new LinkedHashSet<Class<?>>();
            classes.stream().filter(clz -> clz.isAnnotationPresent(annotationClass))
                    .forEach(clz -> annClasses.add(clz));
            return annClasses;

        }

        return Collections.emptySet();
    }

    public static void scan(String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            LOGGER.warn("没有设置packageName, 跳过扫描");
            return;
        }

        if (classes == null) {
            classes = new HashSet<>();
        }

        classes.addAll(getClasses(packageName));
    }

    public static void clear() {
        if (classes != null) {
            classes.clear();
        }
    }

    /**
     * Return a set of all classes contained in the given package.
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    private static Set<Class<?>> getClasses(String packageName) {
        return getClasses(new ResolverUtil.IsA(Object.class), packageName);
    }

    /**
     * Return a set of all classes contained in the given package that match with the given test requirement.
     * @param test the class filter on the given package.
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    private static Set<Class<?>> getClasses(@NonNull ResolverUtil.Test test, @NonNull String packageName) {
        return new ResolverUtil<Object>().find(test, packageName).getClasses();
    }
}
