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
package org.nanoframework.core.web.cookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.secure.CipherExecutor;
import org.nanoframework.toolkit.secure.NoOpCipherExecutor;

/**
 * @author yanghe
 * @since 1.3.7
 */
public class DefaultCookieValueManager implements CookieValueManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCookieValueManager.class);

    private static final char COOKIE_FIELD_SEPARATOR = '@';

    private static final int COOKIE_FIELDS_LENGTH = 3;

    /** The cipher exec that is responsible for encryption and signing of the cookie. */
    private final CipherExecutor cipherExecutor;

    /**
     * Instantiates a new Cas cookie value manager. Set the default cipher to do absolutely nothing.
     */
    public DefaultCookieValueManager() {
        this(new NoOpCipherExecutor());
    }

    /**
     * Instantiates a new Cas cookie value manager.
     * @param cipherExecutor the cipher executor
     */
    public DefaultCookieValueManager(CipherExecutor cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
        LOGGER.debug("Using cipher [{} to encrypt and decode the cookie", this.cipherExecutor.getClass());
    }

    @Override
    public String buildCookieValue(String givenCookieValue, HttpServletRequest request) {
        var buf = new StringBuilder(givenCookieValue == null ? "" : givenCookieValue);
        var remoteAddr = request.getRemoteAddr();
        if (StringUtils.isBlank(remoteAddr)) {
            throw new IllegalStateException("Request does not specify a remote address");
        }

        buf.append(COOKIE_FIELD_SEPARATOR);
        buf.append(remoteAddr);

        var userAgent = request.getHeader("user-agent");
        if (StringUtils.isBlank(userAgent)) {
            throw new IllegalStateException("Request does not specify a user-agent");
        }

        buf.append(COOKIE_FIELD_SEPARATOR);
        buf.append(userAgent);

        var res = buf.toString();
        LOGGER.debug("Encoding cookie value [{}]", res);
        return this.cipherExecutor.encode(res);
    }

    @Override
    public String obtainCookieValue(Cookie cookie, HttpServletRequest request) {
        if (cookie == null) {
            LOGGER.debug("Not found cookie.");
            return null;
        }

        return obtainCookieValue(cookie.getName(), cookie.getValue(), request);
    }

    @Override
    public String obtainCookieValue(String name, String value, HttpServletRequest request) {
        if (StringUtils.isBlank(value)) {
            LOGGER.debug("Cookie value is empty.");
            return value;
        }

        var cookieValue = this.cipherExecutor.decode(value);
        LOGGER.debug("Decoded cookie value is [{}]", cookieValue);
        if (StringUtils.isBlank(cookieValue)) {
            LOGGER.debug("Retrieved decoded cookie value is blank. Failed to decode cookie [{}]", name);
            return null;
        }

        var cookieParts = cookieValue.split(String.valueOf(COOKIE_FIELD_SEPARATOR));
        if (cookieParts.length != COOKIE_FIELDS_LENGTH) {
            throw new IllegalStateException("Invalid cookie. Required fields are missing");
        }

        var obtainValue = cookieParts[0];
        var remoteAddr = cookieParts[1];
        var userAgent = cookieParts[2];

        if (StringUtils.isBlank(obtainValue) || StringUtils.isBlank(remoteAddr) || StringUtils.isBlank(userAgent)) {
            throw new IllegalStateException("Invalid cookie. Required fields are empty");
        }

        if (!remoteAddr.equals(request.getRemoteAddr())) {
            throw new IllegalStateException(
                    "Invalid cookie. Required remote address does not match " + request.getRemoteAddr());
        }

        if (!userAgent.equals(request.getHeader("user-agent"))) {
            throw new IllegalStateException(
                    "Invalid cookie. Required user-agent does not match " + request.getHeader("user-agent"));
        }

        return obtainValue;
    }
}
