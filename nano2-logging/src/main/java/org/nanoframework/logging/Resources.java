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
package org.nanoframework.logging;

/**
 * A class to simplify access to resources through the classloader.
 * @author yanghe
 * @since 2.0.0
 */
public final class Resources {

    private static ClassLoader DEFAULT_CLASSLOADER;

    private Resources() {

    }

    /**
     * Returns the default classloader (may be null).
     * @return The default classloader
     */
    public static ClassLoader getDefaultClassLoader() {
        return DEFAULT_CLASSLOADER;
    }

    /**
     * Sets the default classloader.
     * @param defaultClassLoader - the new default ClassLoader
     */
    public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        Resources.DEFAULT_CLASSLOADER = defaultClassLoader;
    }

    /**
     * Loads a class.
     * @param className - the class to load
     * @return The loaded class
     * @throws ClassNotFoundException If the class cannot be found (duh!)
     */
    public static Class<?> forName(String className) throws ClassNotFoundException {
        Class<?> cls = null;
        try {
            cls = getClassLoader().loadClass(className);
        } catch (Throwable e) {
            // Ignore. Failsafe below.
        }

        if (cls == null) {
            cls = Class.forName(className);
        }

        return cls;
    }

    private static ClassLoader getClassLoader() {
        if (DEFAULT_CLASSLOADER != null) {
            return DEFAULT_CLASSLOADER;
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }

}
