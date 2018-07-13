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
package org.nanoframework.core.web.http;

import java.util.Map;

import org.nanoframework.beans.BaseEntity;
import org.nanoframework.toolkit.lang.CollectionUtils;

import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;

/**
 * Http 返回消息对象
 * @author yanghe
 * @since 1.0
 */
@Getter
public class ResultMap extends BaseEntity {
    private static final long serialVersionUID = -4525859189036534494L;

    /** 描述. */
    private final String info;

    /** 状态码. */
    private final int status;

    /** 消息内容. */
    private final String message;

    /** 消息结果. */
    @Setter
    private Map<?, ?> values;

    public static final String INFO = "info";

    public static final String STATUS = "status";

    public static final String MESSAGE = "message";

    public static final String VALUES = "values";

    private ResultMap(int status, String message, String info) {
        this.status = status;
        this.message = message;
        this.info = info;
    }

    /**
     * 创建消息对象
     * @param status 状态码
     * @param message 消息内容
     * @param info 描述
     * @return new ResultMap instance
     */
    public static ResultMap create(int status, String message, String info) {
        return new ResultMap(status, message, info);
    }

    public static ResultMap create(String message, HttpStatus status) {
        return new ResultMap(status.code, message, status.info);
    }

    public static ResultMap create(Map<String, Object> map) {
        if (CollectionUtils.isNotEmpty(map)) {
            var info = (String) map.get(INFO);
            var status = (int) map.get(STATUS);
            var message = (String) map.get(MESSAGE);
            return create(status, message, info);
        }

        throw new IllegalArgumentException("The parameter 'map' must be not empty.");
    }

    public <K, V> Builder<K, V> builder() {
        return new Builder<>(this);
    }

    public static class Builder<K, V> {
        private final ResultMap res;

        private final Map<K, V> map;

        private Builder(final ResultMap res) {
            this.res = res;
            this.map = Maps.newConcurrentMap();
        }

        public Builder<K, V> put(final K key, final V value) {
            map.put(key, value);
            return this;
        }

        public Builder<K, V> putAll(final Map<K, V> map) {
            this.map.putAll(map);
            return this;
        }

        public Builder<K, V> putIfAbsent(final K key, final V value) {
            map.putIfAbsent(key, value);
            return this;
        }

        public ResultMap build() {
            this.res.values = map;
            return res;
        }
    }
}
