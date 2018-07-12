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
package org.nanoframework.toolkit.consts;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yanghe
 * @since 1.2
 */
public final class Charsets {
    /**
     * Seven-bit ASCII. ISO646-US. The Basic Latin block of the Unicode character set.
     */
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    /**
     * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1.
     */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /**
     * Eight-bit UCS Transformation Format.
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Sixteen-bit UCS Transformation Format, big-endian byte order.
     */
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");

    /**
     * Sixteen-bit UCS Transformation Format, little-endian byte order.
     */
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");

    /**
     * Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark.
     */
    public static final Charset UTF_16 = Charset.forName("UTF-16");

    /**
     * GBK Transformation Format.
     */
    public static final Charset GBK = Charset.forName("GBK");

    /**
     * GB2312 Transformation Format.
     */
    public static final Charset GB2312 = Charset.forName("GB2312");

    private static final Logger LOGGER = LoggerFactory.getLogger(Charsets.class);

    private Charsets() {

    }

    /**
     * Returns a Charset, if possible the Charset for the specified {@code charsetName}, otherwise (if the specified
     * {@code charsetName} is {@code null} or not supported) this method returns the platform default Charset.
     * @param charsetName name of the preferred charset or {@code null}
     * @return a Charset, not null.
     */
    public static Charset getSupportedCharset(String charsetName) {
        return getSupportedCharset(charsetName, Charset.defaultCharset());
    }

    /**
     * Returns a Charset, if possible the Charset for the specified {@code charsetName}, otherwise (if the specified
     * {@code charsetName} is {@code null} or not supported) this method returns the platform default Charset.
     * @param charsetName name of the preferred charset or {@code null}
     * @param defaultCharset returned if {@code charsetName} is null or is not supported.
     * @return a Charset, never null.
     */
    public static Charset getSupportedCharset(String charsetName, Charset defaultCharset) {
        Charset charset = null;
        if (charsetName != null && Charset.isSupported(charsetName)) {
            charset = Charset.forName(charsetName);
        }

        if (charset == null) {
            charset = defaultCharset;
            if (charsetName != null) {
                LOGGER.warn("Charset {} is not supported for layout, using {}", charsetName, charset.displayName());
            }
        }

        return charset;
    }

}
