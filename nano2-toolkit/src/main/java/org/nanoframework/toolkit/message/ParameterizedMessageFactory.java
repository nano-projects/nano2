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

import org.nanoframework.toolkit.message.support.ParameterizedMessage;

/**
 * Enables use of <code>{}</code> parameter markers in message strings.
 * <p>
 * Creates {@link ParameterizedMessage} instances for {@link #newMessage(String, Object...)}.
 * </p>
 * <p>
 * This class is immutable.
 * </p>
 * @author yanghe
 * @since 1.4.0
 */
public final class ParameterizedMessageFactory extends AbstractMessageFactory {
    /**
     * Instance of StringFormatterMessageFactory.
     */
    public static final ParameterizedMessageFactory INSTANCE = new ParameterizedMessageFactory();

    private static final long serialVersionUID = 1L;

    /**
     * Creates {@link ParameterizedMessage} instances.
     * @param message The message pattern.
     * @param params The message parameters.
     * @return The Message.
     *
     * @see MessageFactory#newMessage(String, Object...)
     */
    @Override
    public Message newMessage(final String message, final Object... params) {
        return new ParameterizedMessage(message, params);
    }
}
