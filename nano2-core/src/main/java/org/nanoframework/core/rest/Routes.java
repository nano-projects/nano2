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
package org.nanoframework.core.rest;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.nanoframework.beans.BaseEntity;
import org.nanoframework.core.rest.annotation.RequestMapping;
import org.nanoframework.core.rest.enums.RequestMethod;
import org.nanoframework.core.rest.exception.RouteException;
import org.nanoframework.core.rest.path.AntPathMatcher;
import org.nanoframework.core.rest.path.PathMatcher;
import org.nanoframework.core.rest.path.UrlPathHelper;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.toolkit.lang.ArrayUtils;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.StringUtils;

import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.NonNull;

/**
 * @author yanghe
 * @since 1.4.2
 */
public class Routes {
    private static final Logger LOGGER = LoggerFactory.getLogger(Routes.class);

    private static final Routes INSTANCE = new Routes();

    private final Map<String, Map<RequestMethod, RequestMapper>> mappers = Maps.newLinkedHashMap();

    private final PathMatcher pathMatcher = new AntPathMatcher();

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private Routes() {

    }

    public static Routes route() {
        return INSTANCE;
    }

    public RequestMapper lookup(String url, RequestMethod requestMethod) {
        var mappers = this.mappers.get(url);
        if (CollectionUtils.isNotEmpty(mappers)) {
            var mapper = mappers.get(requestMethod);
            if (mapper != null) {
                return mapper;
            }
        }

        var matchingPatterns = this.mappers.keySet().stream()
                .filter(registeredPattern -> pathMatcher.match(registeredPattern, url)).collect(Collectors.toList());

        var patternComparator = pathMatcher.getPatternComparator(url);
        String bestPatternMatch = null;
        if (!matchingPatterns.isEmpty()) {
            Collections.sort(matchingPatterns, patternComparator);
            LOGGER.debug("Matching patterns for request [{}] are {}", url, matchingPatterns);
            bestPatternMatch = matchingPatterns.get(0);
        }

        return lookup(url, requestMethod, bestPatternMatch, matchingPatterns, patternComparator);
    }

    protected RequestMapper lookup(String url, RequestMethod requestMethod, String bestPatternMatch,
            List<String> matchingPatterns, Comparator<String> patternComparator) {
        if (bestPatternMatch != null) {
            var mappers = this.mappers.get(bestPatternMatch);
            if (mappers == null) {
                if (!bestPatternMatch.endsWith("/")) {
                    return null;
                }

                mappers = this.mappers.get(bestPatternMatch.substring(0, bestPatternMatch.length() - 1));
                if (mappers == null) {
                    return null;
                }
            }

            var mapper = mappers.get(requestMethod);
            if (mapper == null) {
                return null;
            }

            var uriTemplateVariables = new LinkedHashMap<String, String>();
            for (var matchingPattern : matchingPatterns) {
                if (patternComparator.compare(bestPatternMatch, matchingPattern) == 0) {
                    var vars = pathMatcher.extractUriTemplateVariables(matchingPattern, url);
                    var decodedVars = urlPathHelper.decodePathVariables(vars);
                    uriTemplateVariables.putAll(decodedVars);
                }
            }

            mapper.setParam(uriTemplateVariables);
            return mapper;
        }

        return null;
    }

    public void register(String url, Map<RequestMethod, RequestMapper> mappers) {
        if (CollectionUtils.isEmpty(mappers)) {
            return;
        }

        mappers.keySet().forEach(requestMethod -> {
            var mapped = lookup(url, requestMethod);
            if (mapped != null) {
                throw new RouteException("Duplicate Restful-style URL definition: " + url);
            }
        });

        var mappedMapper = this.mappers.get(url);
        if (mappedMapper != null) {
            mappers.forEach((requestMethod, mapper) -> {
                if (mappedMapper.containsKey(requestMethod)) {
                    throw new RouteException(
                            "Duplicate Restful-style URL definition: " + url + " of method [ " + requestMethod + " ]");
                } else {
                    mappedMapper.put(requestMethod, mapper);
                }
            });
        } else {
            this.mappers.put(url, mappers);
        }
    }

    public void clear() {
        this.mappers.clear();
    }

    public Map<String, Map<RequestMethod, RequestMapper>> matchers(Object instance, Method[] methods,
            Class<? extends RequestMapping> annotated, String url) {
        if (ArrayUtils.isEmpty(methods)) {
            return Collections.emptyMap();
        }

        var routes = new HashMap<String, Map<RequestMethod, RequestMapper>>();
        Arrays.stream(methods).filter(method -> filterMethod(method, annotated))
                .map(method -> routeDefine(instance, method, annotated, url))
                .forEach(routeRes -> routeDefine0(routeRes, routes));

        return routes;
    }

    protected boolean filterMethod(Method method, Class<? extends RequestMapping> annotated) {
        if (method.isAnnotationPresent(annotated)) {
            var mapping = method.getAnnotation(annotated);
            if (mapping != null && StringUtils.isNotBlank(mapping.value())) {
                return true;
            }
        }

        return false;
    }

    protected Route routeDefine(Object instance, Method method, Class<? extends RequestMapping> annotated, String url) {
        var mapping = method.getAnnotation(annotated);
        var mapper = RequestMapper.builder().instance(instance).cls(instance.getClass()).method(method)
                .requestMethods(mapping.method()).build();
        var mappers = new HashMap<RequestMethod, RequestMapper>();
        var requestMethods = mapper.getRequestMethods();
        for (var requestMethod : requestMethods) {
            mappers.put(requestMethod, mapper);
        }

        var route = (url + mapping.value());
        var newRoute = execRoutePath(route);
        LOGGER.debug("Route define: {}.{}:{} {}", instance.getClass().getName(), method.getName(), newRoute,
                List.of(requestMethods));
        return new Route(newRoute, mappers);
    }

    protected String execRoutePath(String route) {
        var rtks = route.split("/");
        var routeBuilder = new StringBuilder();
        for (var rtk : rtks) {
            if (StringUtils.isEmpty(rtk)) {
                continue;
            }

            if (rtk.startsWith("{") && rtk.endsWith("}")) {
                routeBuilder.append('/');

                var idx = rtk.indexOf(':');
                if (idx > 0) {
                    routeBuilder.append(StringUtils.lowerCase(rtk.substring(0, idx)));
                    routeBuilder.append(rtk.substring(idx));
                } else {
                    routeBuilder.append(rtk);
                }
            } else if ((rtk.startsWith("{") && !rtk.endsWith("}")) || (!rtk.startsWith("{") && rtk.endsWith("}"))) {
                throw new IllegalArgumentException("Invalid route definition: " + route);
            } else {
                routeBuilder.append('/');
                routeBuilder.append(StringUtils.lowerCase(rtk));
            }
        }

        return routeBuilder.toString();
    }

    protected void routeDefine0(Route route, Map<String, Map<RequestMethod, RequestMapper>> routes) {
        var routeURL = route.getRoute();
        if (!CollectionUtils.isEmpty(route.getMappers()) && routes.containsKey(routeURL)) {
            var before = route.getMappers().keySet();
            var after = routes.get(routeURL).keySet();
            if (!isIntersectionRequestMethod(before, after)) {
                putRoute(route, routes);
            } else {
                throw new RouteException(routeURL);
            }
        } else {
            putRoute(route, routes);
        }
    }

    private void putRoute(Route route, Map<String, Map<RequestMethod, RequestMapper>> routes) {
        var url = route.getRoute();
        var mapper = route.getMappers();
        var mappers = routes.get(url);
        if (mappers == null) {
            routes.put(url, mapper);
        } else {
            mappers.putAll(mapper);
            routes.put(url, mappers);
        }
    }

    private boolean isIntersectionRequestMethod(@NonNull Set<RequestMethod> before, @NonNull Set<RequestMethod> after) {
        for (var bf : before) {
            for (var af : after) {
                if (bf == af) {
                    return true;
                }
            }
        }

        return false;
    }

    @Getter
    protected static class Route extends BaseEntity {
        private static final long serialVersionUID = 4937587574776102818L;

        private final String route;

        private final Map<RequestMethod, RequestMapper> mappers;

        public Route(String route, Map<RequestMethod, RequestMapper> mappers) {
            this.route = route;
            this.mappers = mappers;
        }

    }
}
