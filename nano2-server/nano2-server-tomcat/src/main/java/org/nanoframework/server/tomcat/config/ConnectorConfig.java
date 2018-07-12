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
package org.nanoframework.server.tomcat.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.http2.Http2Protocol;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Getter
@Setter
public class ConnectorConfig extends AbstractConfig {
    public static final String TOMCAT_CONNECTOR = "server.tomcat.connector";

    private static final long serialVersionUID = -5280315541834101307L;

    private static final Integer DEFAULT_PORT = 7000;

    private static final String DEFAULT_PROTOCOL = "org.apache.coyote.http11.Http11Nio2Protocol";

    private static final Long DEFAULT_CONNECTION_TIMEOUT = 20_000L;

    private static final Integer DEFAULT_REDIRECT_PORT = 7433;

    private static final String DEFAULT_EXECUTOR = "tomcatThreadPool";

    private static final Boolean DEFAULT_ENABLE_LOOKUPS = Boolean.FALSE;

    private static final Integer DEFAULT_ACCEPT_COUNT = 100;

    private static final Integer DEFAULT_MAX_POST_SIZE = 10 * 1024 * 1024;

    private static final String DEFAULT_COMPRESSION = "on";

    private static final Boolean DEFAULT_DISABLE_UPLOAD_TIMEOUT = Boolean.TRUE;

    private static final Integer DEFAULT_COMPRESSION_MIN_SIZE = 2 * 1024;

    private static final String DEFAULT_NO_COMPRESSION_USER_AGENTS = "gozilla, traviata";

    private static final Integer DEFAULT_ACCEPTOR_THREAD_COUNT = 2;

    private static final String DEFAULT_COMPRESSABLE_MIME_TYPE = "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml";

    private static final String DEFAULT_URI_ENCODING = "UTF-8";

    static {
        var conf = new ConnectorConfig();
        conf.port = DEFAULT_PORT;
        conf.protocol = DEFAULT_PROTOCOL;
        conf.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        conf.redirectPort = DEFAULT_REDIRECT_PORT;
        conf.executor = DEFAULT_EXECUTOR;
        conf.enableLookups = DEFAULT_ENABLE_LOOKUPS;
        conf.acceptCount = DEFAULT_ACCEPT_COUNT;
        conf.maxPostSize = DEFAULT_MAX_POST_SIZE;
        conf.compression = DEFAULT_COMPRESSION;
        conf.disableUploadTimeout = DEFAULT_DISABLE_UPLOAD_TIMEOUT;
        conf.compressionMinSize = DEFAULT_COMPRESSION_MIN_SIZE;
        conf.noCompressionUserAgents = DEFAULT_NO_COMPRESSION_USER_AGENTS;
        conf.acceptorThreadCount = DEFAULT_ACCEPTOR_THREAD_COUNT;
        conf.compressableMimeType = DEFAULT_COMPRESSABLE_MIME_TYPE;
        conf.uriEncoding = DEFAULT_URI_ENCODING;
        conf.upgradeProtocol = new Http2Protocol();
        DEF = conf;
    }

    private Integer port;

    private String protocol;

    private Long connectionTimeout;

    private Integer redirectPort;

    private String executor;

    private Boolean enableLookups;

    private Integer acceptCount;

    private Integer maxPostSize;

    private String compression;

    private Boolean disableUploadTimeout;

    private Integer compressionMinSize;

    private String noCompressionUserAgents;

    private Integer acceptorThreadCount;

    private String compressableMimeType;

    private String uriEncoding;

    private UpgradeProtocol upgradeProtocol;

    private ConnectorConfig() {

    }

    public ConnectorConfig(final ConnectorConfig conf) {
        this.merge(conf);
    }

    public Connector init() {
        var connector = new Connector(protocol);
        connector.setPort(port);
        connector.setAsyncTimeout(connectionTimeout);
        connector.setRedirectPort(redirectPort);
        connector.setAttribute("executor", executor);
        connector.setEnableLookups(enableLookups);
        connector.setAttribute("acceptCount", acceptCount);
        connector.setMaxPostSize(maxPostSize);
        connector.setAttribute("compression", compression);
        connector.setAttribute("disableUploadTimeout", disableUploadTimeout);
        connector.setAttribute("noCompressionUserAgents", noCompressionUserAgents);
        connector.setAttribute("acceptorThreadCount", acceptorThreadCount);
        connector.setAttribute("compressableMimeType", compressableMimeType);
        connector.setURIEncoding(uriEncoding);
        connector.addUpgradeProtocol(upgradeProtocol);
        return connector;
    }

    @Override
    public String key() {
        return TOMCAT_CONNECTOR;
    }
}
