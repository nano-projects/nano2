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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nanoframework.toolkit.lang.ArrayUtils;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.scan.annotation.Scan;
import org.nanoframework.toolkit.scan.vfs.ResolverUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.NonNull;

/**
 * 扫描组件，并返回符合要求的集合.
 * @author yanghe
 * @since 1.0
 */
public final class ClassScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassScanner.class);

    private static Set<Class<?>> CLASSES = new HashSet<>();

    private ClassScanner() {

    }

    /**
     * 返回所有带有参数中的注解的类.
     * @param annotationClass 注解类
     * @return 过滤后的类
     */
    public static Set<Class<?>> filter(Class<? extends Annotation> annotationClass) {
        if (CLASSES.size() > 0) {
            var annClasses = new LinkedHashSet<Class<?>>();
            CLASSES.stream().filter(clz -> clz.isAnnotationPresent(annotationClass))
                    .forEach(clz -> annClasses.add(clz));
            return annClasses;
        }

        return Collections.emptySet();
    }

    /**
     * @return 已经被扫描的类数量
     */
    public static long loadedClassSize() {
        if (CollectionUtils.isEmpty(CLASSES)) {
            return 0;
        }

        return CLASSES.size();
    }

    /**
     * 根据给定的包名进行类扫描.
     * @param packageName 包路径
     */
    public static void scan(String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            LOGGER.warn("没有设置packageName, 跳过扫描");
            return;
        }

        CLASSES.addAll(getClasses(packageName));
    }

    /**
     * 根据Class进行类扫描，如果该类设置了类级注解{@link Scan}, 并且设置了包配置，则使用配置的包路径进行扫描，并且对Class自身包路径进行扫描. 如果Class没有配置类级注解{@link Scan},
     * 则使用Class自身包路径进行扫描.
     * @param cls 扫描基础类
     */
    public static void scan(Class<?> cls) {
        if (cls.isAnnotationPresent(Scan.class)) {
            var scan = cls.getAnnotation(Scan.class);
            var packages = scan.value();
            var clsPkg = cls.getPackageName();
            if (ArrayUtils.isNotEmpty(packages)) {
                var exsits = new AtomicBoolean();
                Arrays.stream(packages).map(pkg -> {
                    if (StringUtils.equals(pkg, clsPkg)) {
                        exsits.set(true);
                    }

                    return pkg;
                }).forEach(pkg -> scan(pkg));

                if (!exsits.get()) {
                    scan(clsPkg);
                }
            } else {
                scan(clsPkg);
            }
        } else {
            scan(cls.getPackageName());
        }
    }

    /**
     * 清理已扫描的类缓存.
     */
    public static void clear() {
        CLASSES.clear();
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
