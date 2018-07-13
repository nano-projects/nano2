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
package org.nanoframework.core.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.toolkit.lang.StringUtils;

import com.google.common.collect.Lists;

/**
 * @author yanghe
 * @since 1.2
 */
public class CrossOriginFilter implements Filter {
    public static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = "Access-Control-Request-Method";

    public static final String ACCESS_CONTROL_REQUEST_HEADERS_HEADER = "Access-Control-Request-Headers";

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";

    public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";

    public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";

    public static final String ACCESS_CONTROL_MAX_AGE_HEADER = "Access-Control-Max-Age";

    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";

    public static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";

    public static final String ALLOWED_ORIGINS_PARAM = "allowedOrigins";

    public static final String ALLOWED_METHODS_PARAM = "allowedMethods";

    public static final String ALLOWED_HEADERS_PARAM = "allowedHeaders";

    public static final String PREFLIGHT_MAX_AGE_PARAM = "preflightMaxAge";

    public static final String ALLOW_CREDENTIALS_PARAM = "allowCredentials";

    public static final String EXPOSED_HEADERS_PARAM = "exposedHeaders";

    public static final String OLD_CHAIN_PREFLIGHT_PARAM = "forwardPreflight";

    public static final String CHAIN_PREFLIGHT_PARAM = "chainPreflight";

    private static final Logger LOGGER = LoggerFactory.getLogger(CrossOriginFilter.class);

    private static final String ORIGIN_HEADER = "Origin";

    private static final String ANY_ORIGIN = "*";

    private static final Set<String> SIMPLE_HTTP_METHODS = Set.of("GET", "POST", "HEAD");

    private boolean anyOriginAllowed;

    private List<String> allowedOrigins = Lists.newArrayList();

    private List<String> allowedMethods = Lists.newArrayList();

    private List<String> allowedHeaders = Lists.newArrayList();

    private List<String> exposedHeaders = Lists.newArrayList();

    private int preflightMaxAge;

    private boolean allowCredentials;

    private boolean chainPreflight;

    public void init(FilterConfig config) throws ServletException {
        var allowedOriginsConfig = config.getInitParameter(ALLOWED_ORIGINS_PARAM);
        if (allowedOriginsConfig == null) {
            allowedOriginsConfig = ANY_ORIGIN;
        }

        var allowedOrigins = allowedOriginsConfig.split(",");
        for (String allowedOrigin : allowedOrigins) {
            allowedOrigin = allowedOrigin.trim();
            if (allowedOrigin.length() > 0) {
                if (ANY_ORIGIN.equals(allowedOrigin)) {
                    this.anyOriginAllowed = true;
                    this.allowedOrigins.clear();
                    break;
                }

                this.allowedOrigins.add(allowedOrigin);
            }

        }

        var allowedMethodsConfig = config.getInitParameter(ALLOWED_METHODS_PARAM);
        if (allowedMethodsConfig == null) {
            allowedMethodsConfig = "GET,POST,HEAD";
        }

        this.allowedMethods.addAll(Arrays.asList(allowedMethodsConfig.split(",")));

        var allowedHeadersConfig = config.getInitParameter(ALLOWED_HEADERS_PARAM);
        if (allowedHeadersConfig == null) {
            allowedHeadersConfig = "X-Requested-With,Content-Type,Accept,Origin";
        }

        this.allowedHeaders.addAll(Arrays.asList(allowedHeadersConfig.split(",")));

        var preflightMaxAgeConfig = config.getInitParameter(PREFLIGHT_MAX_AGE_PARAM);
        if (preflightMaxAgeConfig == null) {
            preflightMaxAgeConfig = "1800";
        }

        try {
            this.preflightMaxAge = Integer.parseInt(preflightMaxAgeConfig);
        } catch (NumberFormatException x) {
            LOGGER.info("Cross-origin filter, could not parse '{}' parameter as integer: {}",
                    new Object[] {PREFLIGHT_MAX_AGE_PARAM, preflightMaxAgeConfig });
        }

        var allowedCredentialsConfig = config.getInitParameter(ALLOW_CREDENTIALS_PARAM);
        if (allowedCredentialsConfig == null) {
            allowedCredentialsConfig = "true";
        }

        this.allowCredentials = Boolean.parseBoolean(allowedCredentialsConfig);

        var exposedHeadersConfig = config.getInitParameter(EXPOSED_HEADERS_PARAM);
        if (exposedHeadersConfig == null) {
            exposedHeadersConfig = StringUtils.EMPTY;
        }

        this.exposedHeaders.addAll(Arrays.asList(exposedHeadersConfig.split(",")));

        var chainPreflightConfig = config.getInitParameter(OLD_CHAIN_PREFLIGHT_PARAM);
        if (chainPreflightConfig != null) {
            LOGGER.warn("DEPRECATED CONFIGURATION: Use chainPreflight instead of forwardPreflight", new Object[0]);
        } else {
            chainPreflightConfig = config.getInitParameter(CHAIN_PREFLIGHT_PARAM);
        }

        if (chainPreflightConfig == null) {
            chainPreflightConfig = "true";
        }

        this.chainPreflight = Boolean.parseBoolean(chainPreflightConfig);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder().append("Cross-origin filter configuration: allowedOrigins = ")
                    .append(allowedOriginsConfig).append(", ").append(ALLOWED_METHODS_PARAM).append(" = ")
                    .append(allowedMethodsConfig).append(", ").append(ALLOWED_HEADERS_PARAM).append(" = ")
                    .append(allowedHeadersConfig).append(", ").append(PREFLIGHT_MAX_AGE_PARAM).append(" = ")
                    .append(preflightMaxAgeConfig).append(", ").append(ALLOW_CREDENTIALS_PARAM).append(" = ")
                    .append(allowedCredentialsConfig).append(", ").append(EXPOSED_HEADERS_PARAM).append(" = ")
                    .append(exposedHeadersConfig).append(", ").append(CHAIN_PREFLIGHT_PARAM).append(" = ")
                    .append(chainPreflightConfig).toString(), new Object[0]);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        handle((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var origin = request.getHeader(ORIGIN_HEADER);
        if ((origin != null) && (isEnabled(request))) {
            if (originMatches(origin)) {
                if (isSimpleRequest(request)) {
                    LOGGER.debug("Cross-origin request to {} is a simple cross-origin request",
                            new Object[] {request.getRequestURI() });
                    handleSimpleResponse(request, response, origin);
                } else if (isPreflightRequest(request)) {
                    LOGGER.debug("Cross-origin request to {} is a preflight cross-origin request",
                            new Object[] {request.getRequestURI() });
                    handlePreflightResponse(request, response, origin);
                    if (this.chainPreflight) {
                        LOGGER.debug("Preflight cross-origin request to {} forwarded to application",
                                new Object[] {request.getRequestURI() });
                    }

                    return;
                } else {
                    LOGGER.debug("Cross-origin request to {} is a non-simple cross-origin request",
                            new Object[] {request.getRequestURI() });
                    handleSimpleResponse(request, response, origin);
                }
            } else {
                LOGGER.debug(new StringBuilder().append("Cross-origin request to ").append(request.getRequestURI())
                        .append(" with origin ").append(origin).append(" does not match allowed origins ")
                        .append(this.allowedOrigins).toString(), new Object[0]);
            }
        }

        chain.doFilter(request, response);
    }

    protected boolean isEnabled(HttpServletRequest request) {
        for (var connections = request.getHeaders("Connection"); connections.hasMoreElements();) {
            var connection = (String) connections.nextElement();
            if ("Upgrade".equalsIgnoreCase(connection)) {
                for (var upgrades = request.getHeaders("Upgrade"); upgrades.hasMoreElements();) {
                    var upgrade = (String) upgrades.nextElement();
                    if ("WebSocket".equalsIgnoreCase(upgrade)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean originMatches(String originList) {
        if (this.anyOriginAllowed) {
            return true;
        }

        if (originList.trim().length() == 0) {
            return false;
        }

        var origins = originList.split(" ");
        for (var origin : origins) {
            if (origin.trim().length() != 0) {
                for (var allowedOrigin : this.allowedOrigins) {
                    if (allowedOrigin.contains(ANY_ORIGIN)) {
                        var matcher = createMatcher(origin, allowedOrigin);
                        if (matcher.matches()) {
                            return true;
                        }
                    } else if (allowedOrigin.equals(origin)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Matcher createMatcher(String origin, String allowedOrigin) {
        var regex = parseAllowedWildcardOriginToRegex(allowedOrigin);
        var pattern = Pattern.compile(regex);
        return pattern.matcher(origin);
    }

    private String parseAllowedWildcardOriginToRegex(String allowedOrigin) {
        return allowedOrigin.replace(".", "\\.").replace(ANY_ORIGIN, ".*");
    }

    private boolean isSimpleRequest(HttpServletRequest request) {
        var method = request.getMethod();
        if (SIMPLE_HTTP_METHODS.contains(method)) {
            return request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null;
        }

        return false;
    }

    private boolean isPreflightRequest(HttpServletRequest request) {
        var method = request.getMethod();
        if (!"OPTIONS".equalsIgnoreCase(method)) {
            return false;
        }

        if (request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null) {
            return false;
        }

        return true;
    }

    private void handleSimpleResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);

        if (this.allowCredentials) {
            response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        }

        if (!this.exposedHeaders.isEmpty()) {
            response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, commify(this.exposedHeaders));
        }
    }

    private void handlePreflightResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
        var methodAllowed = isMethodAllowed(request);
        if (!methodAllowed) {
            return;
        }

        var headersAllowed = areHeadersAllowed(request);
        if (!headersAllowed) {
            return;
        }

        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);

        if (this.allowCredentials) {
            response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        }

        if (this.preflightMaxAge > 0) {
            response.setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, String.valueOf(this.preflightMaxAge));
        }

        response.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, commify(this.allowedMethods));
        response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, commify(this.allowedHeaders));
    }

    private boolean isMethodAllowed(HttpServletRequest request) {
        var accessControlRequestMethod = request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER);
        LOGGER.debug("{} is {}", new Object[] {ACCESS_CONTROL_REQUEST_METHOD_HEADER, accessControlRequestMethod });
        var result = false;
        if (accessControlRequestMethod != null) {
            result = this.allowedMethods.contains(accessControlRequestMethod);
        }

        LOGGER.debug(
                new StringBuilder().append("Method {} is").append(result ? "" : " not")
                        .append(" among allowed methods {}").toString(),
                new Object[] {accessControlRequestMethod, this.allowedMethods });
        return result;
    }

    private boolean areHeadersAllowed(HttpServletRequest request) {
        var accessControlRequestHeaders = request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS_HEADER);
        LOGGER.debug("{} is {}", new Object[] {ACCESS_CONTROL_REQUEST_HEADERS_HEADER, accessControlRequestHeaders });
        var result = true;
        if (StringUtils.isNotBlank(accessControlRequestHeaders)) {
            var headers = accessControlRequestHeaders.split(",");
            for (var header : headers) {
                var headerAllowed = false;
                for (var allowedHeader : this.allowedHeaders) {
                    if (header.trim().equalsIgnoreCase(allowedHeader.trim())) {
                        headerAllowed = true;
                        break;
                    }
                }

                if (!headerAllowed) {
                    result = false;
                    break;
                }
            }
        }

        LOGGER.debug(
                new StringBuilder().append("Headers [{}] are").append(result ? "" : " not")
                        .append(" among allowed headers {}").toString(),
                new Object[] {accessControlRequestHeaders, this.allowedHeaders });
        return result;
    }

    private String commify(List<String> strings) {
        var buf = new StringBuilder();
        for (var i = 0; i < strings.size(); i++) {
            if (i > 0) {
                buf.append(',');
            }

            buf.append(strings.get(i));
        }

        return buf.toString();
    }

    public void destroy() {
        this.anyOriginAllowed = false;
        this.allowedOrigins.clear();
        this.allowedMethods.clear();
        this.allowedHeaders.clear();
        this.preflightMaxAge = 0;
        this.allowCredentials = false;
    }
}