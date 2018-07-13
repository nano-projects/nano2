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
import org.nanoframework.core.rest.annotation.Route;
import org.nanoframework.core.rest.enums.HttpType;
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
public final class Routes {
    private static final Logger LOGGER = LoggerFactory.getLogger(Routes.class);

    private static final Routes INSTANCE = new Routes();

    private final Map<String, Map<HttpType, RouteMapper>> mappers = Maps.newLinkedHashMap();

    private final PathMatcher pathMatcher = new AntPathMatcher();

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private Routes() {

    }

    /**
     * @return Routes
     */
    public static Routes route() {
        return INSTANCE;
    }

    public Object invoke(final RouteMapper mapper) {
        return invoke(mapper, Maps.newHashMap());
    }

    public Object invoke(RouteMapper mapper, Map<String, Object> parameter, Object... objs) {
        if (mapper == null) {
            throw new RouteException("未找到路由资源");
        }

        try {
            var param = mapper.getParam();
            if (CollectionUtils.isNotEmpty(param)) {
                parameter.putAll(param);
            }

            var instance = mapper.getInstance();
            var method = mapper.getMethod();
            var bind = mapper.bind(method, parameter, objs);
            return method.invoke(instance, bind);
        } catch (Throwable e) {
            if (e instanceof RouteException) {
                throw (RouteException) e;
            }

            throw invokeThrow(e);
        }
    }

    private RouteException invokeThrow(Throwable e) {
        Throwable tmp = e;
        Throwable cause;
        while ((cause = tmp.getCause()) != null) {
            if (cause instanceof RouteException) {
                return (RouteException) cause;
            } else {
                tmp = cause;
            }
        }

        return new RouteException(tmp.getMessage(), tmp);
    }

    /**
     * @param url 路由地址
     * @param type 请求类型
     * @return 路由配置
     */
    public RouteMapper lookup(String url, HttpType type) {
        var mappers = this.mappers.get(url);
        if (CollectionUtils.isNotEmpty(mappers)) {
            var mapper = mappers.get(type);
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

        return lookup(url, type, bestPatternMatch, matchingPatterns, patternComparator);
    }

    private RouteMapper lookup(String url, HttpType type, String bestPatternMatch,
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

            var mapper = mappers.get(type);
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

    /**
     * @param url 路由地址
     * @param mappers 路由配置
     */
    public void register(String url, Map<HttpType, RouteMapper> mappers) {
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

    /**
     * 清理路由配置.
     */
    public void clear() {
        this.mappers.clear();
    }

    /**
     * @param instance 服务对象
     * @param methods 服务方法列表
     * @param annotated 注解配置
     * @param url 路由地址
     * @return 根据给定的信息进行路由匹配
     */
    public Map<String, Map<HttpType, RouteMapper>> matchers(Object instance, Method[] methods,
            Class<? extends Route> annotated, String url) {
        if (ArrayUtils.isEmpty(methods)) {
            return Collections.emptyMap();
        }

        var routes = new HashMap<String, Map<HttpType, RouteMapper>>();
        Arrays.stream(methods).filter(method -> filterMethod(method, annotated))
                .map(method -> routeDefine(instance, method, annotated, url))
                .forEach(routeRes -> routeDefine0(routeRes, routes));

        return routes;
    }

    private boolean filterMethod(Method method, Class<? extends Route> annotated) {
        if (method.isAnnotationPresent(annotated)) {
            var mapping = method.getAnnotation(annotated);
            if (mapping != null && StringUtils.isNotBlank(mapping.value())) {
                return true;
            }
        }

        return false;
    }

    private RouteEntity routeDefine(Object instance, Method method, Class<? extends Route> annotated, String url) {
        var mapping = method.getAnnotation(annotated);
        var mapper = RouteMapper.builder().instance(instance).cls(instance.getClass()).method(method)
                .types(mapping.type()).build();
        var mappers = new HashMap<HttpType, RouteMapper>();
        var types = mapper.getTypes();
        for (var requestMethod : types) {
            mappers.put(requestMethod, mapper);
        }

        var route = (url + mapping.value());
        var newRoute = execRoutePath(route);
        LOGGER.debug("Route define: {}.{}:{} {}", instance.getClass().getName(), method.getName(), newRoute,
                List.of(types));
        return new RouteEntity(newRoute, mappers);
    }

    private String execRoutePath(String route) {
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

    private void routeDefine0(RouteEntity route, Map<String, Map<HttpType, RouteMapper>> routes) {
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

    private void putRoute(RouteEntity route, Map<String, Map<HttpType, RouteMapper>> routes) {
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

    private boolean isIntersectionRequestMethod(@NonNull Set<HttpType> before, @NonNull Set<HttpType> after) {
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
    protected static class RouteEntity extends BaseEntity {
        private static final long serialVersionUID = 4937587574776102818L;

        private final String route;

        private final Map<HttpType, RouteMapper> mappers;

        /**
         * @param route the route
         * @param mappers the mappers
         */
        public RouteEntity(String route, Map<HttpType, RouteMapper> mappers) {
            this.route = route;
            this.mappers = mappers;
        }

    }
}
