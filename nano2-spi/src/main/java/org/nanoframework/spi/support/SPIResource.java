/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.spi.support;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nanoframework.beans.BaseEntity;

import lombok.Getter;

/**
 * @author yanghe
 * @since 1.4.8
 */
@Getter
public final class SPIResource extends BaseEntity {
    /** 空资源. */
    public static final SPIResource EMPTY = SPIResource.create(Collections.emptyList(), Collections.emptyMap());

    private static final long serialVersionUID = 2184606147032384544L;

    private final List<File> files;

    private final Map<String, List<InputStream>> streams;

    private SPIResource(List<File> files, Map<String, List<InputStream>> streams) {
        this.files = Collections.unmodifiableList(files);
        this.streams = Collections.unmodifiableMap(streams);
    }

    /**
     * @param files SPI资源文件列表
     * @param streams SPI资源流列表
     * @return SPI资源
     */
    public static SPIResource create(List<File> files, Map<String, List<InputStream>> streams) {
        return new SPIResource(files, streams);
    }
}
