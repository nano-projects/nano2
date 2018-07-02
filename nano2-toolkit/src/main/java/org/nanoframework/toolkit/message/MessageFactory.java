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
package org.nanoframework.toolkit.message;

/**
 * Creates messages. Implementations can provide different message format syntaxes.
 * @author yanghe
 * @see ParameterizedMessageFactory
 * @since 1.4.0
 */
public interface MessageFactory {

    /**
     * Creates a new message based on an Object.
     *
     * @param message
     *            a message object
     * @return a new message
     */
    Message newMessage(Object message);

    /**
     * Creates a new message based on a String.
     *
     * @param message
     *            a message String
     * @return a new message
     */
    Message newMessage(String message);

    /**
     * Creates a new parameterized message.
     *
     * @param message
     *            a message template, the kind of message template depends on the implementation.
     * @param params
     *            the message parameters
     * @return a new message
     * @see ParameterizedMessageFactory
     */
    Message newMessage(String message, Object... params);
}
