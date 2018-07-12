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
package org.nanoframework.toolkit.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

/**
 * 系统运行时功能扩展类.
 * @author yanghe
 * @since 1.0
 */
public final class RuntimeUtils {
    /** 当前应用进程PID. */
    public static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

    /** 当前应用宿主机操作系统. */
    public static final String OSNAME = System.getProperty("os.name");

    /** 当前应用宿主机CPU核心数. */
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors() + 1;

    private RuntimeUtils() {

    }

    /**
     * 杀死当前系统进行.
     * @throws IOException IO异常
     */
    public static void killProcess() throws IOException {
        if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
            Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", String.format("kill -9 %s", PID) });
        } else if (OSNAME.indexOf("Windows") > -1) {
            Runtime.getRuntime().exec(String.format("cmd /c taskkill /pid %s /f ", PID));
        }
    }

    /**
     * 根据进程号杀死对应的进程.
     * @param pid 进程号
     * @throws IOException IO异常
     */
    public static void killProcess(String pid) throws IOException {
        if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
            Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", String.format("kill -9 %s", pid) });
        } else if (OSNAME.indexOf("Windows") > -1) {
            Runtime.getRuntime().exec(String.format("cmd /c taskkill /pid %s /f ", pid));
        }
    }

    /**
     * 根据进程号优雅退出进程.
     * @param pid 进程号
     * @throws IOException IO异常
     */
    public static void exitProcess(String pid) throws IOException {
        if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
            Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", String.format("kill -15 %s", pid) });
        } else if (OSNAME.indexOf("Windows") > -1) {
            Runtime.getRuntime().exec(String.format("cmd /c taskkill /pid %s /f ", pid));
        }
    }

    /**
     * 根据进程号查询该进程是否存在.
     * @param pid 进程号
     * @return 查询结果
     * @throws IOException IO异常
     */
    public static boolean existsProcess(String pid) throws IOException {
        if (StringUtils.isBlank(pid)) {
            return false;
        }

        var exsits = false;
        Process process = null;
        String result = null;
        if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
            process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", String.format("ps -f -p %s", pid) });

            try (var in = process.getInputStream(); var input = new BufferedReader(new InputStreamReader(in))) {
                while ((result = input.readLine()) != null) {
                    if (StringUtils.isNotEmpty(result) && result.indexOf(pid) > -1) {
                        exsits = true;
                    }
                }
            }
        } else if (OSNAME.indexOf("Windows") > -1) {
            process = Runtime.getRuntime()
                    .exec(String.format("cmd /c Wmic Process where ProcessId=\"%s\" get ExecutablePath ", pid));

            try (var in = process.getInputStream(); var input = new BufferedReader(new InputStreamReader(in))) {
                while ((result = input.readLine()) != null) {
                    if (StringUtils.isNotEmpty(result) && result.indexOf("No Instance(s) Available") < 0) {
                        exsits = true;
                    }
                }
            }
        }

        return exsits;
    }

    /**
     * 判断当前运行的系统是否是Windows.
     * @return boolean
     */
    public static boolean isWindows() {
        return OSNAME.contains("Windows");
    }

    /**
     * 根据Class获取该Class所在的磁盘路径.
     * @param clz 查询的类
     * @return 返回该类的所在位置
     */
    public static String getPath(Class<?> clz) {
        var runJarPath = clz.getProtectionDomain().getCodeSource().getLocation().getPath();
        var tmpPath = runJarPath.substring(0, runJarPath.lastIndexOf('/'));
        if (tmpPath.endsWith("/lib")) {
            tmpPath = tmpPath.replace("/lib", "");
        }

        return tmpPath.substring(isWindows() ? 1 : 0, tmpPath.lastIndexOf('/')) + '/';
    }

    /**
     * 获取运行时中的所有Jar文件.
     * @return List
     * @throws IOException if I/O error occur
     */
    public static List<JarFile> classPaths() throws IOException {
        var paths = System.getProperty("java.class.path").split(":");
        if (paths.length > 0) {
            var jars = new ArrayList<JarFile>(paths.length);
            for (var path : paths) {
                if (!path.endsWith("jar")) {
                    continue;
                }

                jars.add(new JarFile(new File(path)));
            }

            return jars;
        }

        return Collections.emptyList();
    }

}
