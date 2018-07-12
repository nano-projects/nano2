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
package org.nanoframework.server.tomcat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.server.Server;
import org.nanoframework.server.tomcat.config.ConnectorConfig;
import org.nanoframework.server.tomcat.config.ExecutorConfig;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.properties.PropertiesLoader;
import org.nanoframework.toolkit.scan.ClassScanner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * @author yanghe
 * @since 2.0.0
 */
public final class TomcatServer extends Tomcat implements Server {
    public static final String READY = "org.apache.catalina.startup.READY";

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatServer.class);

    private static final String DEFAULT_TOMCAT_BASE_TEMP_DIR = "tomcat-base";

    private static final String TOMCAT_PID_FILE = "tomcat.pid";

    protected String resourceBase = "webRoot";

    protected String defaultWebXml = "/META-INF/tomcat/web.xml";

    protected File globalWebXml = new File(resourceBase + "/WEB-INF/default.xml");

    private Properties server;

    static {
        System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
    }

    private TomcatServer() throws Throwable {
        super();
        load(CONFIG);
        init(server.getProperty(ROOT), null);
    }

    private TomcatServer(String path) throws Throwable {
        super();
        load(path);
        init(server.getProperty(ROOT), null);
    }

    public static TomcatServer server(Class<?> cls) throws Throwable {
        ClassScanner.scan(cls);
        return new TomcatServer();
    }

    public static TomcatServer server(Class<?> cls, String path) throws Throwable {
        ClassScanner.scan(cls);
        return new TomcatServer(path);
    }

    protected void load(String path) {
        server = PropertiesLoader.load(path);
    }

    protected void init(String contextRoot, String resourceBase) throws ServletException, IOException {
        setBaseDir(Files.createTempDirectory(DEFAULT_TOMCAT_BASE_TEMP_DIR).toString());
        if (StringUtils.isNotEmpty(resourceBase)) {
            this.resourceBase = resourceBase;
        }

        initExecutor();
        initConnector();

        var conf = new ContextConfig();
        var base = new File(this.resourceBase);
        if (!base.exists()) {
            base.mkdirs();
        }

        var ctx = (StandardContext) this.addWebapp(getHost(), contextRoot, base.getAbsolutePath(),
                (LifecycleListener) conf);
        createGlobalXml();
        conf.setDefaultWebXml(globalWebXml.getAbsolutePath());

        Arrays.stream(ctx.findLifecycleListeners()).filter(listen -> listen instanceof DefaultWebXmlListener)
                .forEach(listen -> ctx.removeLifecycleListener(listen));

        ctx.setParentClassLoader(TomcatServer.class.getClassLoader());

        // Disable TLD scanning by default
        if (System.getProperty(Constants.SKIP_JARS_PROPERTY) == null
                && System.getProperty(Constants.SCAN_JARS_PROPERTY) == null) {
            LOGGER.debug("disabling TLD scanning");
            ((StandardJarScanFilter) ctx.getJarScanner().getJarScanFilter()).setTldSkip("*");
        }
    }

    protected void initExecutor() {
        var type = new TypeReference<ExecutorConfig>() {};
        var conf = new ExecutorConfig(JSON.parseObject(server.getProperty(ExecutorConfig.TOMCAT_EXECUTOR), type));
        LOGGER.debug(conf.toString());
        getService().addExecutor(conf.init());
    }

    @SuppressWarnings("rawtypes")
    protected void initConnector() {
        var type = new TypeReference<ConnectorConfig>() {};
        var conf = new ConnectorConfig(JSON.parseObject(server.getProperty(ConnectorConfig.TOMCAT_CONNECTOR), type));
        LOGGER.debug(conf.toString());
        var connector = conf.init();
        var service = getService();
        var executor = service.getExecutor(conf.getExecutor());
        ((AbstractProtocol) connector.getProtocolHandler()).setExecutor(executor);
        setConnector(connector);
    }

    protected void createGlobalXml() throws IOException {
        if (!globalWebXml.exists()) {
            var dir = globalWebXml.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            try (var scanner = new Scanner(TomcatServer.class.getResourceAsStream(defaultWebXml));
                    var writer = new FileWriter(globalWebXml.getAbsolutePath(), false)) {
                while (scanner.hasNextLine()) {
                    writer.write(scanner.nextLine() + '\n');
                }

                writer.flush();
            }
        }
    }

    @Override
    public void startServer() {
        try {
            this.start();
            System.setProperty(READY, "true");
            this.getServer().await();
        } catch (Throwable e) {
            LOGGER.error("Bootstrap server error: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    @Override
    public String pid() {
        return TOMCAT_PID_FILE;
    }

    @Override
    public Properties context() {
        return server;
    }
}
