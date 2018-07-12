/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nanoframework.core.rest.path;

import java.util.Comparator;
import java.util.Map;

/**
 * @author Juergen Hoeller
 * @see AntPathMatcher
 * @since 1.2
 */
public interface PathMatcher {

    /**
     * @param path the path
     * @return boolean
     */
    boolean isPattern(String path);

    /**
     * @param pattern the pattern
     * @param path the path
     * @return boolean
     */
    boolean match(String pattern, String path);

    /**
     * @param pattern the pattern
     * @param path the path
     * @return boolean
     */
    boolean matchStart(String pattern, String path);

    /**
     * @param pattern the pattern
     * @param path the path
     * @return String
     */
    String extractPathWithinPattern(String pattern, String path);

    /**
     * @param pattern the pattern
     * @param path the path
     * @return Map
     */
    Map<String, String> extractUriTemplateVariables(String pattern, String path);

    /**
     * @param path the path
     * @return Comparator
     */
    Comparator<String> getPatternComparator(String path);

    /**
     * @param pattern1 the pattern1
     * @param pattern2 the pattern2
     * @return String
     */
    String combine(String pattern1, String pattern2);

}
