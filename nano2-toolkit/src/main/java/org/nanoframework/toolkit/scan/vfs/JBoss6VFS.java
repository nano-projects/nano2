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
package org.nanoframework.toolkit.scan.vfs;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link VFS} implementation that works with the VFS API provided by JBoss 6.
 * @author Ben Gunter
 * @since 2.0.0
 */
public class JBoss6VFS extends AbstractVFS {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverUtil.class);

    /** A class that mimics a tiny subset of the JBoss VirtualFile class. */
    private static final class VirtualFile {
        private static Class<?> VIRTUAL_FILE;

        private static Method GET_PATH_NAME_RELATIVE_TO;

        private static Method GET_CHILDREN_RECURSIVELY;

        private Object virtualFile;

        private VirtualFile(Object virtualFile) {
            this.virtualFile = virtualFile;
        }

        private String getPathNameRelativeTo(VirtualFile parent) {
            try {
                return invoke(GET_PATH_NAME_RELATIVE_TO, virtualFile, parent.virtualFile);
            } catch (IOException e) {
                // This exception is not thrown by the called method
                LOGGER.error("This should not be possible. VirtualFile.getPathNameRelativeTo() threw IOException.");
                return null;
            }
        }

        private List<VirtualFile> getChildren() throws IOException {
            List<?> objects = invoke(GET_CHILDREN_RECURSIVELY, virtualFile);
            List<VirtualFile> children = new ArrayList<VirtualFile>(objects.size());
            for (Object object : objects) {
                children.add(new VirtualFile(object));
            }
            return children;
        }
    }

    /** A class that mimics a tiny subset of the JBoss VFS class. */
    private static class VFS {
        private static Class<?> VFS;

        private static Method GET_CHILD;

        private static VirtualFile getChild(URL url) throws IOException {
            Object o = invoke(GET_CHILD, VFS, url);
            return o == null ? null : new VirtualFile(o);
        }
    }

    /** Flag that indicates if this VFS is valid for the current environment. */
    private static Boolean VALID;

    /** Find all the classes and methods that are required to access the JBoss 6 VFS. */
    protected static synchronized void initialize() {
        if (VALID == null) {
            // Assume valid. It will get flipped later if something goes wrong.
            VALID = Boolean.TRUE;

            // Look up and verify required classes
            VFS.VFS = checkNotNull(getClass("org.jboss.vfs.VFS"));
            VirtualFile.VIRTUAL_FILE = checkNotNull(getClass("org.jboss.vfs.VirtualFile"));

            // Look up and verify required methods
            VFS.GET_CHILD = checkNotNull(getMethod(VFS.VFS, "getChild", URL.class));
            VirtualFile.GET_CHILDREN_RECURSIVELY = checkNotNull(
                    getMethod(VirtualFile.VIRTUAL_FILE, "getChildrenRecursively"));
            VirtualFile.GET_PATH_NAME_RELATIVE_TO = checkNotNull(
                    getMethod(VirtualFile.VIRTUAL_FILE, "getPathNameRelativeTo", VirtualFile.VIRTUAL_FILE));

            // Verify that the API has not changed
            checkReturnType(VFS.GET_CHILD, VirtualFile.VIRTUAL_FILE);
            checkReturnType(VirtualFile.GET_CHILDREN_RECURSIVELY, List.class);
            checkReturnType(VirtualFile.GET_PATH_NAME_RELATIVE_TO, String.class);
        }
    }

    /**
     * Verifies that the provided object reference is null. If it is null, then this VFS is marked as invalid for the
     * current environment.
     * @param <T> type T
     * @param object The object reference to check for null.
     * @return T
     */
    protected static <T> T checkNotNull(T object) {
        if (object == null) {
            setInvalid();
        }

        return object;
    }

    /**
     * Verifies that the return type of a method is what it is expected to be. If it is not, then this VFS is marked as
     * invalid for the current environment.
     * @param method The method whose return type is to be checked.
     * @param expected A type to which the method's return type must be assignable.
     * @see Class#isAssignableFrom(Class)
     */
    protected static void checkReturnType(Method method, Class<?> expected) {
        if (method != null && !expected.isAssignableFrom(method.getReturnType())) {
            LOGGER.error("Method " + method.getClass().getName() + '.' + method.getName() + "(..) should return "
                    + expected.getName() + " but returns " //
                    + method.getReturnType().getName() + " instead.");
            setInvalid();
        }
    }

    /** Mark this {@link VFS} as invalid for the current environment. */
    protected static void setInvalid() {
        if (JBoss6VFS.VALID != null && JBoss6VFS.VALID) {
            LOGGER.debug("JBoss 6 VFS API is not available in this environment.");
            JBoss6VFS.VALID = Boolean.FALSE;
        }
    }

    static {
        initialize();
    }

    @Override
    public boolean isValid() {
        return VALID;
    }

    @Override
    public List<String> list(URL url, String path) throws IOException {
        var directory = VFS.getChild(url);
        if (directory == null) {
            return Collections.emptyList();
        }

        if (!path.endsWith("/")) {
            path += '/';
        }

        var children = directory.getChildren();
        var names = new ArrayList<String>(children.size());
        for (var vf : children) {
            var relative = vf.getPathNameRelativeTo(directory);
            names.add(path + relative);
        }

        return names;
    }
}
