/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.modules.httpclient.config;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.nanoframework.beans.BaseEntity;
import org.nanoframework.beans.Globals;
import org.nanoframework.modules.httpclient.HttpClient;
import org.nanoframework.modules.httpclient.annotation.Http;
import org.nanoframework.spi.exception.SPIException;
import org.nanoframework.spi.support.SPILoader;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.ReflectUtils;
import org.nanoframework.toolkit.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

import lombok.Getter;

/**
 * @author yanghe
 * @since 1.4.10
 */
@Getter
public class HttpConfigure extends BaseEntity {
    /** */
    public static final String TIME_TO_LIVE = "context.httpclient.time.to.live";

    /** */
    public static final String TIME_UNIT = "context.httpclient.timeunit";

    /** */
    public static final String MAX_TOTAL = "context.httpclient.max.total";

    /** */
    public static final String MAX_PER_ROUTE = "context.httpclient.default.max.per.route";

    /** */
    public static final String CHARSET = "context.httpclient.charset";

    /**
     * 默认HttpClient实现.
     */
    public static final String DEFAULT_SPI_NAME = "default";

    /**
     * 超时时间.
     */
    public static final String DEFAULT_TIME_TO_LIVE = "5000";

    /**
     * 超时时间单位.
     */
    public static final String DEFAULT_TIME_UNIT = "MILLISECONDS";

    /**
     * 最大连接数.
     */
    public static final String DEFAULT_MAX_TOTAL = "1024";

    /**
     * 最大并发连接数.
     */
    public static final String DEFAULT_MAX_PER_ROUTE = "512";

    /**
     * 字符集.
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    private static final long serialVersionUID = 7466575473562761447L;

    private static final ConcurrentMap<String, HttpClient> CLIENTS = Maps.newConcurrentMap();

    private final String spi;

    private final long timeToLive;

    private final TimeUnit tunit;

    private final int maxTotal;

    private final int maxPerRoute;

    private final Charset charset;

    /**
     * Default Constructor.
     */
    public HttpConfigure() {
        this(Long.parseLong(System.getProperty(TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE)));
    }

    /**
     * @param timeToLive 超时时间
     */
    public HttpConfigure(final long timeToLive) {
        this(timeToLive, Charset.forName(System.getProperty(CHARSET, DEFAULT_CHARSET)));
    }

    /**
     * @param timeToLive 超时时间
     * @param charset 字符集
     */
    public HttpConfigure(final long timeToLive, final Charset charset) {
        this(DEFAULT_SPI_NAME, timeToLive, TimeUnit.valueOf(System.getProperty(TIME_UNIT, DEFAULT_TIME_UNIT)),
                Integer.parseInt(System.getProperty(MAX_TOTAL, DEFAULT_MAX_TOTAL)),
                Integer.parseInt(System.getProperty(MAX_PER_ROUTE, DEFAULT_MAX_PER_ROUTE)), charset);
    }

    /**
     * @param spi SPI名称
     * @param timeToLive 超时时间
     * @param tunit 超时时间单位
     * @param maxTotal 最大连接数
     * @param maxPerRoute 最大并发连接数
     * @param charset 字符集
     */
    public HttpConfigure(final String spi, final long timeToLive, final TimeUnit tunit, final int maxTotal,
            final int maxPerRoute, final Charset charset) {
        this.spi = spi;
        this.timeToLive = timeToLive;
        this.tunit = tunit;
        this.maxTotal = maxTotal;
        this.maxPerRoute = maxPerRoute;
        this.charset = charset;
    }

    public HttpConfigure(Http http) {
        this(http.spi(), http.timeToLive(), http.tunit(), http.maxTotal(), http.maxPerRoute(),
                Charset.forName(http.charset()));
    }

    /**
     * @return HttpClient
     */
    @SuppressWarnings("unchecked")
    public HttpClient get() {
        var key = toString();
        if (CLIENTS.containsKey(key)) {
            return CLIENTS.get(key);
        } else {
            var injector = Globals.get(Injector.class);
            var spis = SPILoader.spis(HttpClient.class);
            if (CollectionUtils.isNotEmpty(spis)) {
                for (var spi : spis) {
                    if (StringUtils.equals(spi.getName(), this.spi)) {
                        var instance = spi.getInstance();
                        if (HttpClient.class.isAssignableFrom(instance)) {
                            var client = (HttpClient) ReflectUtils.newInstance(spi.getInstance(), this);
                            if (injector != null) {
                                injector.injectMembers(client);
                            }

                            CLIENTS.put(key, client);
                            return client;
                        } else {
                            throw new SPIException(String.format("无效的SPI定义: HttpClient is not assignable from %s",
                                    spi.getInstanceClsName()));
                        }
                    }
                }
            }

            throw new SPIException("无效的SPI定义");
        }
    }

    public static void clear() throws IOException {
        var clients = CLIENTS.values();
        for (var client : clients) {
            client.close();
        }

        CLIENTS.clear();
    }

    @Override
    public String toString() {
        return DigestUtils.md5Hex(JSON.toJSONString(this));
    }

}
