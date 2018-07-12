/*
 * Copyright 2002-2015 the original author or authors.
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nanoframework.toolkit.lang.StringUtils;

import lombok.Getter;
import lombok.NonNull;

/**
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @since 16.07.2003
 */
public class AntPathMatcher implements PathMatcher {
    /** */
    public static final String DEFAULT_PATH_SEPARATOR = "/";

    private static final int CACHE_TURNOFF_THRESHOLD = 65536;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?\\}");

    private String pathSeparator;

    private PathSeparatorPatternCache pathSeparatorPatternCache;

    private boolean trimTokens = true;

    private volatile Boolean cachePatterns;

    private final Map<String, String[]> tokenizedPatternCache = new ConcurrentHashMap<String, String[]>(256);

    private final Map<String, AntPathStringMatcher> stringMatcherCache = new ConcurrentHashMap<String, AntPathStringMatcher>(
            256);

    private boolean caseSensitive;

    /** */
    public AntPathMatcher() {
        this.pathSeparator = DEFAULT_PATH_SEPARATOR;
        this.pathSeparatorPatternCache = new PathSeparatorPatternCache(DEFAULT_PATH_SEPARATOR);
    }

    /**
     * @param caseSensitive 区分大小写
     */
    public AntPathMatcher(boolean caseSensitive) {
        this();
        this.caseSensitive = caseSensitive;
    }

    /**
     * @param pathSeparator 路由分隔符
     */
    public AntPathMatcher(@NonNull String pathSeparator) {
        this.pathSeparator = pathSeparator;
        this.pathSeparatorPatternCache = new PathSeparatorPatternCache(pathSeparator);
    }

    /**
     * @param pathSeparator 路由分隔符
     */
    public void setPathSeparator(String pathSeparator) {
        this.pathSeparator = (pathSeparator != null ? pathSeparator : DEFAULT_PATH_SEPARATOR);
        this.pathSeparatorPatternCache = new PathSeparatorPatternCache(this.pathSeparator);
    }

    /**
     * @param trimTokens the trimTokens
     */
    public void setTrimTokens(boolean trimTokens) {
        this.trimTokens = trimTokens;
    }

    /**
     * @param cachePatterns the cachePatterns
     */
    public void setCachePatterns(boolean cachePatterns) {
        this.cachePatterns = cachePatterns;
    }

    private void deactivatePatternCache() {
        this.cachePatterns = Boolean.FALSE;
        this.tokenizedPatternCache.clear();
        this.stringMatcherCache.clear();
    }

    @Override
    public boolean isPattern(String path) {
        return (path.indexOf('*') != -1 || path.indexOf('?') != -1);
    }

    @Override
    public boolean match(String pattern, String path) {
        return doMatch(pattern, path, true, null);
    }

    @Override
    public boolean matchStart(String pattern, String path) {
        return doMatch(pattern, path, false, null);
    }

    /**
     * @param pattern the pattern
     * @param path the path
     * @param fullMatch the fullMatch
     * @param uriTemplateVariables the uriTemplateVariables
     * @return boolean
     */
    protected boolean doMatch(String pattern, String path, boolean fullMatch,
            Map<String, String> uriTemplateVariables) {
        if (path.startsWith(this.pathSeparator) != pattern.startsWith(this.pathSeparator)) {
            return false;
        }

        var pattDirs = tokenizePattern(pattern);
        var pathDirs = tokenizePath(path);
        var pattIdxStart = 0;
        var pattIdxEnd = pattDirs.length - 1;
        var pathIdxStart = 0;
        var pathIdxEnd = pathDirs.length - 1;

        // Match all elements up to the first **
        while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            var pattDir = pattDirs[pattIdxStart];
            if ("**".equals(pattDir)) {
                break;
            }
            if (!matchStrings(pattDir, pathDirs[pathIdxStart], uriTemplateVariables)) {
                return false;
            }
            pattIdxStart++;
            pathIdxStart++;
        }

        if (pathIdxStart > pathIdxEnd) {
            // Path is exhausted, only match if rest of pattern is * or **'s
            if (pattIdxStart > pattIdxEnd) {
                return (pattern.endsWith(this.pathSeparator) ? path.endsWith(this.pathSeparator)
                        : !path.endsWith(this.pathSeparator));
            }

            if (!fullMatch) {
                return true;
            }

            if (pattIdxStart == pattIdxEnd && pattDirs[pattIdxStart].equals("*") && path.endsWith(this.pathSeparator)) {
                return true;
            }

            for (var i = pattIdxStart; i <= pattIdxEnd; i++) {
                if (!pattDirs[i].equals("**")) {
                    return false;
                }
            }

            return true;
        } else if (pattIdxStart > pattIdxEnd) {
            // String not exhausted, but pattern is. Failure.
            return false;
        } else if (!fullMatch && "**".equals(pattDirs[pattIdxStart])) {
            // Path start definitely matches due to "**" part in pattern.
            return true;
        }

        // up to last '**'
        while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            var pattDir = pattDirs[pattIdxEnd];
            if (pattDir.equals("**")) {
                break;
            }

            if (!matchStrings(pattDir, pathDirs[pathIdxEnd], uriTemplateVariables)) {
                return false;
            }

            pattIdxEnd--;
            pathIdxEnd--;
        }

        if (pathIdxStart > pathIdxEnd) {
            // String is exhausted
            for (var i = pattIdxStart; i <= pattIdxEnd; i++) {
                if (!pattDirs[i].equals("**")) {
                    return false;
                }
            }

            return true;
        }

        while (pattIdxStart != pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            var patIdxTmp = -1;
            for (var i = pattIdxStart + 1; i <= pattIdxEnd; i++) {
                if (pattDirs[i].equals("**")) {
                    patIdxTmp = i;
                    break;
                }
            }

            if (patIdxTmp == pattIdxStart + 1) {
                // '**/**' situation, so skip one
                pattIdxStart++;
                continue;
            }

            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            var patLength = (patIdxTmp - pattIdxStart - 1);
            var strLength = (pathIdxEnd - pathIdxStart + 1);
            var foundIdx = -1;

            strLoop: for (var i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    String subPat = pattDirs[pattIdxStart + j + 1];
                    String subStr = pathDirs[pathIdxStart + i + j];
                    if (!matchStrings(subPat, subStr, uriTemplateVariables)) {
                        continue strLoop;
                    }
                }
                foundIdx = pathIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            pattIdxStart = patIdxTmp;
            pathIdxStart = foundIdx + patLength;
        }

        for (var i = pattIdxStart; i <= pattIdxEnd; i++) {
            if (!pattDirs[i].equals("**")) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param pattern the pattern
     * @return tokens
     */
    protected String[] tokenizePattern(String pattern) {
        String[] tokenized = null;
        var cachePatterns = this.cachePatterns;
        if (cachePatterns == null || cachePatterns.booleanValue()) {
            tokenized = this.tokenizedPatternCache.get(pattern);
        }

        if (tokenized == null) {
            tokenized = tokenizePath(pattern);
            if (cachePatterns == null && this.tokenizedPatternCache.size() >= CACHE_TURNOFF_THRESHOLD) {
                // Try to adapt to the runtime situation that we're encountering:
                // There are obviously too many different patterns coming in here...
                // So let's turn off the cache since the patterns are unlikely to be reoccurring.
                deactivatePatternCache();
                return tokenized;
            }

            if (cachePatterns == null || cachePatterns.booleanValue()) {
                this.tokenizedPatternCache.put(pattern, tokenized);
            }
        }

        return tokenized;
    }

    /**
     * @param path the path
     * @return tokens
     */
    protected String[] tokenizePath(String path) {
        return StringUtils.tokenizeToStringArray(path, this.pathSeparator, this.trimTokens, true);
    }

    private boolean matchStrings(String pattern, String str, Map<String, String> uriTemplateVariables) {
        var token = str;
        if (!caseSensitive) {
            if (!pattern.startsWith("{") && !pattern.endsWith("}")) {
                token = StringUtils.lowerCase(str);
            }
        }

        return getStringMatcher(pattern).matchStrings(token, uriTemplateVariables);
    }

    /**
     * @param pattern the pattern
     * @return AntPathStringMatcher
     */
    protected AntPathStringMatcher getStringMatcher(String pattern) {
        AntPathStringMatcher matcher = null;
        var cachePatterns = this.cachePatterns;
        if (cachePatterns == null || cachePatterns.booleanValue()) {
            matcher = this.stringMatcherCache.get(pattern);
        }

        if (matcher == null) {
            matcher = new AntPathStringMatcher(pattern);
            if (cachePatterns == null && this.stringMatcherCache.size() >= CACHE_TURNOFF_THRESHOLD) {
                // Try to adapt to the runtime situation that we're encountering:
                // There are obviously too many different patterns coming in here...
                // So let's turn off the cache since the patterns are unlikely to be reoccurring.
                deactivatePatternCache();
                return matcher;
            }

            if (cachePatterns == null || cachePatterns.booleanValue()) {
                this.stringMatcherCache.put(pattern, matcher);
            }
        }

        return matcher;
    }

    @Override
    public String extractPathWithinPattern(String pattern, String path) {
        var patternParts = StringUtils.tokenizeToStringArray(pattern, this.pathSeparator, this.trimTokens, true);
        var pathParts = StringUtils.tokenizeToStringArray(path, this.pathSeparator, this.trimTokens, true);
        var builder = new StringBuilder();
        var pathStarted = false;
        for (var segment = 0; segment < patternParts.length; segment++) {
            var patternPart = patternParts[segment];
            if (patternPart.indexOf('*') > -1 || patternPart.indexOf('?') > -1) {
                for (; segment < pathParts.length; segment++) {
                    if (pathStarted || (segment == 0 && !pattern.startsWith(this.pathSeparator))) {
                        builder.append(this.pathSeparator);
                    }

                    builder.append(pathParts[segment]);
                    pathStarted = true;
                }
            }
        }

        return builder.toString();
    }

    @Override
    public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
        var variables = new LinkedHashMap<String, String>();
        var result = doMatch(pattern, path, true, variables);
        if (!result) {
            throw new IllegalStateException(String.format("Pattern \"%s\" is not a match for \"%s\"", pattern, path));
        }

        return variables;
    }

    @Override
    public String combine(String pattern1, String pattern2) {
        if (!StringUtils.hasText(pattern1) && !StringUtils.hasText(pattern2)) {
            return "";
        }

        if (!StringUtils.hasText(pattern1)) {
            return pattern2;
        }

        if (!StringUtils.hasText(pattern2)) {
            return pattern1;
        }

        var pattern1ContainsUriVar = (pattern1.indexOf('{') != -1);
        if (!pattern1.equals(pattern2) && !pattern1ContainsUriVar && match(pattern1, pattern2)) {
            // /* + /hotel -> /hotel ; "/*.*" + "/*.html" -> /*.html
            // However /user + /user -> /usr/user ; /{foo} + /bar -> /{foo}/bar
            return pattern2;
        }

        // /hotels/* + /booking -> /hotels/booking
        // /hotels/* + booking -> /hotels/booking
        if (pattern1.endsWith(this.pathSeparatorPatternCache.getEndsOnWildCard())) {
            return concat(pattern1.substring(0, pattern1.length() - 2), pattern2);
        }

        // /hotels/** + /booking -> /hotels/**/booking
        // /hotels/** + booking -> /hotels/**/booking
        if (pattern1.endsWith(this.pathSeparatorPatternCache.getEndsOnDoubleWildCard())) {
            return concat(pattern1, pattern2);
        }

        var starDotPos1 = pattern1.indexOf("*.");
        if (pattern1ContainsUriVar || starDotPos1 == -1 || StringUtils.equals(".", this.pathSeparator)) {
            // simply concatenate the two patterns
            return concat(pattern1, pattern2);
        }

        var ext1 = pattern1.substring(starDotPos1 + 1);
        var dotPos2 = pattern2.indexOf('.');
        var file2 = (dotPos2 == -1 ? pattern2 : pattern2.substring(0, dotPos2));
        var ext2 = (dotPos2 == -1 ? "" : pattern2.substring(dotPos2));
        var ext1All = (StringUtils.equals(ext1, ".*") || StringUtils.equals(ext1, ""));
        var ext2All = (StringUtils.equals(ext2, ".*") || StringUtils.equals(ext2, ""));
        if (!ext1All && !ext2All) {
            throw new IllegalArgumentException("Cannot combine patterns: " + pattern1 + " vs " + pattern2);
        }
        String ext = (ext1All ? ext2 : ext1);
        return file2 + ext;
    }

    private String concat(String path1, String path2) {
        var path1EndsWithSeparator = path1.endsWith(this.pathSeparator);
        var path2StartsWithSeparator = path2.startsWith(this.pathSeparator);

        if (path1EndsWithSeparator && path2StartsWithSeparator) {
            return path1 + path2.substring(1);
        } else if (path1EndsWithSeparator || path2StartsWithSeparator) {
            return path1 + path2;
        } else {
            return path1 + this.pathSeparator + path2;
        }
    }

    @Override
    public Comparator<String> getPatternComparator(String path) {
        return new AntPatternComparator(path);
    }

    /**
     * @author yanghe
     * @since 2.0.0
     */
    protected static class AntPathStringMatcher {

        private static final Pattern GLOB_PATTERN = Pattern
                .compile("\\?|\\*|\\{((?:\\{[^/]+?\\}|[^/{}]|\\\\[{}])+?)\\}");

        private static final String DEFAULT_VARIABLE_PATTERN = "(.*)";

        private final Pattern pattern;

        private final List<String> variableNames = new LinkedList<String>();

        /**
         * @param pattern the pattern
         */
        public AntPathStringMatcher(String pattern) {
            var patternBuilder = new StringBuilder();
            var m = GLOB_PATTERN.matcher(pattern);
            var end = 0;
            while (m.find()) {
                patternBuilder.append(quote(pattern, end, m.start()));
                var match = m.group();
                if ("?".equals(match)) {
                    patternBuilder.append('.');
                } else if ("*".equals(match)) {
                    patternBuilder.append(".*");
                } else if (match.startsWith("{") && match.endsWith("}")) {
                    var colonIdx = match.indexOf(':');
                    if (colonIdx == -1) {
                        patternBuilder.append(DEFAULT_VARIABLE_PATTERN);
                        this.variableNames.add(m.group(1));
                    } else {
                        var variablePattern = match.substring(colonIdx + 1, match.length() - 1);
                        patternBuilder.append('(');
                        patternBuilder.append(variablePattern);
                        patternBuilder.append(')');
                        var variableName = match.substring(1, colonIdx);
                        this.variableNames.add(variableName);
                    }
                }
                end = m.end();
            }

            patternBuilder.append(quote(pattern, end, pattern.length()));
            this.pattern = Pattern.compile(patternBuilder.toString());
        }

        private String quote(String s, int start, int end) {
            if (start == end) {
                return "";
            }

            return Pattern.quote(s.substring(start, end));
        }

        /**
         * @param str the str
         * @param uriTemplateVariables the uriTemplateVariables
         * @return boolean
         */
        public boolean matchStrings(String str, Map<String, String> uriTemplateVariables) {
            Matcher matcher = this.pattern.matcher(str);
            if (matcher.matches()) {
                if (uriTemplateVariables != null) {
                    // SPR-8455
                    if (this.variableNames.size() != matcher.groupCount()) {
                        throw new IllegalArgumentException("The number of capturing groups in the pattern segment "
                                + this.pattern
                                + " does not match the number of URI template variables it defines, which can occur if "
                                + " capturing groups are used in a URI template regex. Use non-capturing groups instead.");
                    }

                    for (var i = 1; i <= matcher.groupCount(); i++) {
                        var name = this.variableNames.get(i - 1);
                        var value = matcher.group(i);
                        uriTemplateVariables.put(name, value);
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * @author yanghe
     * @since 2.0.0
     */
    protected static class AntPatternComparator implements Comparator<String> {
        private final String path;

        /**
         * @param path the path
         */
        public AntPatternComparator(String path) {
            this.path = path;
        }

        @Override
        public int compare(String pattern1, String pattern2) {
            var info1 = new PatternInfo(pattern1);
            var info2 = new PatternInfo(pattern2);

            if (info1.isLeastSpecific() && info2.isLeastSpecific()) {
                return 0;
            } else if (info1.isLeastSpecific()) {
                return 1;
            } else if (info2.isLeastSpecific()) {
                return -1;
            }

            var pattern1EqualsPath = pattern1.equals(path);
            var pattern2EqualsPath = pattern2.equals(path);
            if (pattern1EqualsPath && pattern2EqualsPath) {
                return 0;
            } else if (pattern1EqualsPath) {
                return -1;
            } else if (pattern2EqualsPath) {
                return 1;
            }

            if (info1.isPrefixPattern() && info2.getDoubleWildcards() == 0) {
                return 1;
            } else if (info2.isPrefixPattern() && info1.getDoubleWildcards() == 0) {
                return -1;
            }

            if (info1.getTotalCount() != info2.getTotalCount()) {
                return info1.getTotalCount() - info2.getTotalCount();
            }

            if (info1.getLength() != info2.getLength()) {
                return info2.getLength() - info1.getLength();
            }

            if (info1.getSingleWildcards() < info2.getSingleWildcards()) {
                return -1;
            } else if (info2.getSingleWildcards() < info1.getSingleWildcards()) {
                return 1;
            }

            if (info1.getUriVars() < info2.getUriVars()) {
                return -1;
            } else if (info2.getUriVars() < info1.getUriVars()) {
                return 1;
            }

            return 0;
        }

        /**
         * @author yanghe
         * @since 2.0.0
         */
        @Getter
        private static class PatternInfo {

            private final String pattern;

            private int uriVars;

            private int singleWildcards;

            private int doubleWildcards;

            private boolean catchAllPattern;

            private boolean prefixPattern;

            private Integer length;

            /**
             * @param pattern the pattern
             */
            PatternInfo(String pattern) {
                this.pattern = pattern;
                if (this.pattern != null) {
                    initCounters();
                    this.catchAllPattern = StringUtils.equals(this.pattern, "/**");
                    this.prefixPattern = !this.catchAllPattern && this.pattern.endsWith("/**");
                }
                if (this.uriVars == 0) {
                    this.length = (this.pattern != null ? this.pattern.length() : 0);
                }
            }

            /** */
            protected void initCounters() {
                int pos = 0;
                while (pos < this.pattern.length()) {
                    if (this.pattern.charAt(pos) == '{') {
                        this.uriVars++;
                        pos++;
                    } else if (this.pattern.charAt(pos) == '*') {
                        if (pos + 1 < this.pattern.length() && this.pattern.charAt(pos + 1) == '*') {
                            this.doubleWildcards++;
                            pos += 2;
                        } else if (pos > 0 && !this.pattern.substring(pos - 1).equals(".*")) {
                            this.singleWildcards++;
                            pos++;
                        } else {
                            pos++;
                        }
                    } else {
                        pos++;
                    }
                }
            }

            /**
             * @return boolean
             */
            public boolean isLeastSpecific() {
                return (this.pattern == null || this.catchAllPattern);
            }

            /**
             * @return int
             */
            public int getTotalCount() {
                return this.uriVars + this.singleWildcards + (2 * this.doubleWildcards);
            }

            /**
             * @return Returns the length of the given pattern, where template variables are considered to be 1 long.
             */
            public int getLength() {
                if (this.length == null) {
                    this.length = VARIABLE_PATTERN.matcher(this.pattern).replaceAll("#").length();
                }
                return this.length;
            }
        }
    }

    /**
     * @author yanghe
     * @since 2.0.0
     */
    @Getter
    private static class PathSeparatorPatternCache {

        private final String endsOnWildCard;

        private final String endsOnDoubleWildCard;

        /**
         * @param pathSeparator the pathSeparator
         */
        PathSeparatorPatternCache(String pathSeparator) {
            this.endsOnWildCard = pathSeparator + '*';
            this.endsOnDoubleWildCard = pathSeparator + "**";
        }
    }

}
