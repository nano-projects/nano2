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
package org.nanoframework.toolkit.io;

import java.util.LinkedList;

import org.nanoframework.toolkit.lang.StringUtils;

/**
 * @author yanghe
 * @since 2.0.0
 */
public final class FileUtils {
    private static final String FOLDER_SEPARATOR = "/";

    private static final char FOLDER_SEPARATOR_CHAR = '/';

    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

    private static final String TOP_PATH = "..";

    private static final String CURRENT_PATH = ".";

    private FileUtils() {

    }

    /**
     * Normalize the path by suppressing sequences like "path/.." and inner simple dots.
     * <p>
     * The result is convenient for path comparison. For other uses, notice that Windows separators ("\") are replaced
     * by simple slashes.
     * @param path the original path
     * @return the normalized path
     */
    public static String cleanPath(String path) {
        if (path == null) {
            return null;
        }

        var pathToUse = StringUtils.replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);
        var prefixIndex = pathToUse.indexOf(':');
        var prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.contains("/")) {
                prefix = "";
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            }
        }

        if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
            prefix = prefix + FOLDER_SEPARATOR_CHAR;
            pathToUse = pathToUse.substring(1);
        }

        var pathArray = StringUtils.delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
        var pathElements = new LinkedList<String>();
        var tops = 0;

        for (var i = pathArray.length - 1; i >= 0; i--) {
            var element = pathArray[i];
            if (TOP_PATH.equals(element)) {
                // Registering top path found.
                tops++;
            } else if (!CURRENT_PATH.equals(element)) {
                if (tops > 0) {
                    // Merging path element with element corresponding to top path.
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.add(0, element);
                }
            }
        }

        // Remaining top paths need to be retained.
        for (var i = 0; i < tops; i++) {
            pathElements.add(0, TOP_PATH);
        }

        return prefix + StringUtils.collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
    }

    /**
     * Apply the given relative path to the given path, assuming standard Java folder separation (i.e. "/" separators).
     * @param path the path to start from (usually a full file path)
     * @param relativePath the relative path to apply (relative to the full file path above)
     * @return the full file path that results from applying the relative path
     */
    public static String applyRelativePath(String path, String relativePath) {
        var separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR_CHAR);
        if (separatorIndex != -1) {
            var newPath = path.substring(0, separatorIndex);
            if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
                newPath += FOLDER_SEPARATOR_CHAR;
            }

            return newPath + relativePath;
        } else {
            return relativePath;
        }
    }

    /**
     * Extract the filename from the given path, e.g. {@code "mypath/myfile.txt" -&gt; "myfile.txt"}.
     * @param path the file path (may be {@code null})
     * @return the extracted filename, or {@code null} if none
     */
    public static String getFilename(String path) {
        if (path == null) {
            return null;
        }

        var separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR_CHAR);
        return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
    }
}
