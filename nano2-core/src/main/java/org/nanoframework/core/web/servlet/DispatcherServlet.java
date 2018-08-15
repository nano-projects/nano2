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
package org.nanoframework.core.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.nanoframework.core.boot.BootLoader;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;

/**
 * 核心入口，用于项目启动时加载相关组件，初始化依赖注入等.
 * @author yanghe
 * @since 2.0.0
 */
public class DispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 1685855250487831145L;

    private Logger LOGGER = LoggerFactory.getLogger(DispatcherServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            new BootLoader();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }
    }
}
