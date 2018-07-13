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
package org.nanoframework.core.web.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.core.web.filter.RouteFilter.HttpContext;

/**
 * @author yanghe
 * @since 1.3.7
 */
public class EnvironmentFilter extends AbstractFilter {

    @Override
    protected boolean invoke(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpContext.set(Map.of(HttpServletRequest.class, request, HttpServletResponse.class, response));
        return true;
    }

}
