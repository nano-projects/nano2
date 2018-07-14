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
package org.nanoframework.spi.def;

import javax.servlet.ServletConfig;

/**
 * @author yanghe
 * @since 1.1
 */
public interface Plugin {
    /**
     * @return 加载插件，如果加载成功则返回true
     */
    boolean load();

    /**
     * @param config Servlet配置
     */
    void config(ServletConfig config);
}
