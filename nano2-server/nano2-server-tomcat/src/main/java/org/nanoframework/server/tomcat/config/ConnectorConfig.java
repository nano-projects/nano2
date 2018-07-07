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

    static {
        var conf = new ConnectorConfig();
        conf.port = 7000;
        conf.protocol = "org.apache.coyote.http11.Http11Nio2Protocol";
        conf.connectionTimeout = 20_000L;
        conf.redirectPort = 7443;
        conf.executor = "tomcatThreadPool";
        conf.enableLookups = Boolean.FALSE;
        conf.acceptCount = 100;
        conf.maxPostSize = 10 * 1024 * 1024;
        conf.compression = "on";
        conf.disableUploadTimeout = Boolean.TRUE;
        conf.compressionMinSize = 2 * 1024;
        conf.noCompressionUserAgents = "gozilla, traviata";
        conf.acceptorThreadCount = 2;
        conf.compressableMimeType = "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript";
        conf.uriEncoding = "UTF-8";
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
