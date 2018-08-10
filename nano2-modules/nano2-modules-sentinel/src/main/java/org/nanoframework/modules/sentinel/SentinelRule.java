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
package org.nanoframework.modules.sentinel;

import org.nanoframework.beans.BaseEntity;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Getter
@Setter
public class SentinelRule extends BaseEntity {
    private static final long serialVersionUID = 728239344800011682L;

    private FlowRule flow;

    private DegradeRule degrade;

    private SystemRule system;

}
