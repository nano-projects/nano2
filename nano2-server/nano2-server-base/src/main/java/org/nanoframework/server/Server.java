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
package org.nanoframework.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;

import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.server.enums.Commands;
import org.nanoframework.server.enums.Mode;
import org.nanoframework.server.exception.ServerException;
import org.nanoframework.toolkit.consts.Version;
import org.nanoframework.toolkit.lang.RuntimeUtils;
import org.nanoframework.toolkit.lang.StringUtils;

/**
 * @author yanghe
 * @since 2.0.0
 */
public interface Server {
    Logger LOGGER = LoggerFactory.getLogger(Server.class);

    String CONFIG = "/META-INF/server.properties";

    String ROOT = "server.root";

    String MODE = "server.mode";

    String VERSION = "server.version";

    void startServer();

    String pid();

    Properties context();

    default void bootstrap(String... args) {
        if (args.length > 0) {
            var mode = mode();
            var cmd = cmd(args, mode);
            switch (cmd) {
                case START:
                    startServerDaemon();
                    break;
                case STOP:
                    stopServer();
                    break;
                case VERSION:
                    version();
                    break;
                case HELP:
                    usage();
                    break;
            }
        } else {
            usage();
        }
    }

    private void startServerAndWatchPID() {
        writePid2File();
        startServer();
    }

    private void stopServer() {
        try {
            var pid = readPidFile();
            if (StringUtils.isNotBlank(pid)) {
                if (RuntimeUtils.existsProcess(pid)) {
                    RuntimeUtils.exitProcess(pid);
                    return;
                }

                return;
            }

            throw new ServerException(String.format("没有找到%s文件", pid()));
        } catch (Throwable e) {
            if (e instanceof ServerException) {
                throw (ServerException) e;
            }

            throw new ServerException(String.format("停止Server异常: %s", e.getMessage()), e);
        } finally {
            var file = new File(pid());
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void startServerDaemon() {
        Executors.newFixedThreadPool(1, (runnable) -> {
            var server = new Thread(runnable);
            server.setName("Server Deamon: " + System.currentTimeMillis());
            return server;
        }).execute(() -> startServerAndWatchPID());
    }

    private Commands cmd(String[] args, Mode mode) {
        Commands cmd;
        try {
            cmd = Commands.valueOf(args[0].toUpperCase());
        } catch (Throwable e) {
            if (mode == Mode.DEV) {
                cmd = Commands.START;
            } else {
                throw new ServerException("Unknown command in args list");
            }
        }

        return cmd;
    }

    private Mode mode() {
        try {
            return Mode.valueOf(context().getProperty(MODE, Mode.PROD.name()));
        } catch (Throwable e) {
            return Mode.PROD;
        }
    }

    private void writePid2File() {
        try {
            var pid = RuntimeUtils.PID;
            var file = new File(pid());
            var mode = mode();
            if (file.exists()) {
                if (mode == Mode.PROD || mode == Mode.QA) {
                    LOGGER.error(String.format("服务已启动或异常退出，请先删除%s文件后重试", pid()));
                    System.exit(1);
                } else if (RuntimeUtils.existsProcess(readPidFile())) {
                    LOGGER.error("服务已启动，请先停止原进程");
                    System.exit(1);
                } else {
                    file.delete();
                }
            }

            file.createNewFile();
            file.deleteOnExit();
            watcherPid(file);

            try (var writer = new FileWriter(file, false)) {
                writer.write(pid);
                writer.flush();
            }
        } catch (Throwable e) {
            if (e instanceof ServerException) {
                throw (ServerException) e;
            }

            throw new ServerException(e.getMessage(), e);
        }
    }

    private void watcherPid(File pid) throws IOException {
        var watcher = FileSystems.getDefault().newWatchService();
        var path = Paths.get(".");
        path.register(watcher, StandardWatchEventKinds.ENTRY_DELETE);

        Executors.newFixedThreadPool(1, (runnable) -> {
            var tomcat = new Thread(runnable);
            tomcat.setName("PID Watcher: " + System.currentTimeMillis());
            return tomcat;
        }).execute(() -> {
            try {
                for (;;) {
                    var watchKey = watcher.take();
                    var events = watchKey.pollEvents();
                    for (var event : events) {
                        var fileName = ((Path) event.context()).toFile().getAbsolutePath();
                        if (StringUtils.equals(pid.getAbsolutePath(), fileName)) {
                            LOGGER.info(String.format("%s已被删除，应用进入退出流程", pid()));
                            System.exit(0);
                        }
                    }

                    watchKey.reset();
                }
            } catch (InterruptedException e) {
                LOGGER.info("Stoped File Watcher");
            }
        });
    }

    private String readPidFile() {
        try {
            var file = new File(pid());
            if (file.exists()) {
                try (var input = new FileInputStream(file); Scanner scanner = new Scanner(input)) {
                    StringBuilder builder = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        builder.append(scanner.nextLine());
                    }

                    return builder.toString();
                }
            }

            return StringUtils.EMPTY;
        } catch (Throwable e) {
            throw new ServerException("Read PID file error: " + e.getMessage());
        }
    }

    private void version() {
        var versionBuilder = new StringBuilder();
        versionBuilder.append("NanoFramework Version: ");
        versionBuilder.append(Version.CURRENT);
        versionBuilder.append('\n');

        var context = context();
        var appContext = context.getProperty(ROOT, "");
        var appVersion = context.getProperty(VERSION, "0.0.0");
        versionBuilder.append("Application[");
        versionBuilder.append(appContext);
        versionBuilder.append("] Version: ");
        versionBuilder.append(appVersion);
        versionBuilder.append('\n');
        System.out.println(versionBuilder.toString());
    }

    private void usage() {
        var usageBuilder = new StringBuilder();
        usageBuilder.append("Usage: \n\n");
        usageBuilder.append("    ./bootstrap.sh command\n\n");
        usageBuilder.append("The commands are: \n");
        usageBuilder.append("    start        Start Application on Tomcat Server\n");
        usageBuilder.append("    stop         Stop Application\n");
        usageBuilder.append("    version      Show the NanoFramwork and Application version\n\n");
        usageBuilder.append("Use \"./bootstrap.sh help\" for more information about a command.\n");
        System.out.println(usageBuilder.toString());
    }
}
