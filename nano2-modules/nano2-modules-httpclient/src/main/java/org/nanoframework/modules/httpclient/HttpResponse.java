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
package org.nanoframework.modules.httpclient;

import org.nanoframework.beans.BaseEntity;

import lombok.Getter;

/**
 * @author yanghe
 * @since 1.3.3
 */
@Getter
public class HttpResponse extends BaseEntity {
    public static final HttpResponse EMPTY = create(0, "", "", 0, 0);

    private static final long serialVersionUID = -3709502418094416380L;

    private final int statusCode;

    private final String reasonPhrase;

    private final String body;

    private final long send;

    private final long receive;

    private final long response;

    public HttpResponse(int statusCode, String reasonPhrase, String body, long send, long receive) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.body = body;
        this.send = send;
        this.receive = receive;
        this.response = receive - send;
    }

    public static HttpResponse create(int statusCode, String reasonPhrase, String body, long send, long receive) {
        return new HttpResponse(statusCode, reasonPhrase, body, send, receive);
    }
}
