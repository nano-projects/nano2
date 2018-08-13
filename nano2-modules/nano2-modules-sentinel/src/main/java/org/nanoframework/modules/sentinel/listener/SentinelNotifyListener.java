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
package org.nanoframework.modules.sentinel.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.nanoframework.modules.base.listener.NotifyListener;
import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.modules.sentinel.SentinelRule;
import org.nanoframework.toolkit.lang.CollectionUtils;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

/**
 * @author yanghe
 * @since 2.0.0
 */
@Singleton
public class SentinelNotifyListener implements NotifyListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SentinelNotifyListener.class);

    private final Map<String, SentinelRule> rules = Maps.newConcurrentMap();

    private final AtomicBoolean changed = new AtomicBoolean(false);

    private final ReentrantLock lock = new ReentrantLock();

    private long delay = 5000;

    private final Timer timer = new Timer("RuleManagerListener-Timer", true);

    public SentinelNotifyListener() {
        createScheduler();
    }

    private void createScheduler() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    update();
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }, new Date(), delay);
    }

    private void clearTimer() {
        timer.cancel();
    }

    @Override
    public void notify(String key, String value) {
        var lock = this.lock;
        try {
            lock.lock();
            var rule = JSON.parseObject(value, SentinelRule.class);
            if (rule != null) {
                rules.put(key, rule);
            }

            changed.set(true);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(String key) {
        var lock = this.lock;
        try {
            lock.lock();
            rules.remove(key);
            changed.set(true);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    private void update() {
        if (changed.get()) {
            var lock = this.lock;
            try {
                lock.lock();
                if (CollectionUtils.isNotEmpty(rules)) {
                    var flows = new ArrayList<FlowRule>();
                    var degrades = new ArrayList<DegradeRule>();
                    var systems = new ArrayList<SystemRule>();
                    rules.forEach((key, rule) -> {
                        var url = key.substring(RouteTypeListener.KEY_PREFIX.length(), key.length());
                        var flow = rule.getFlow();
                        if (flow != null) {
                            if (StringUtils.isBlank(flow.getResource())) {
                                flow.setResource(url);
                            }

                            flows.add(flow);
                        }

                        var degrade = rule.getDegrade();
                        if (degrade != null) {
                            if (StringUtils.isBlank(degrade.getResource())) {
                                degrade.setResource(url);
                            }

                            degrades.add(degrade);
                        }

                        var system = rule.getSystem();
                        if (system != null) {
                            systems.add(system);
                        }
                    });

                    if (CollectionUtils.isNotEmpty(flows)) {
                        FlowRuleManager.loadRules(flows);
                    }

                    if (CollectionUtils.isNotEmpty(degrades)) {
                        DegradeRuleManager.loadRules(degrades);
                    }

                    if (CollectionUtils.isNotEmpty(systems)) {
                        SystemRuleManager.loadRules(systems);
                    }

                    LOGGER.info("同步Sentinel配置结束");
                }
            } finally {
                changed.set(false);
                lock.unlock();
            }
        }
    }

    public void setDelay(long delay) {
        if (delay <= 1000) {
            throw new IllegalArgumentException("定时任务执行时间不能小于1000ms");
        }

        this.delay = delay;
        clearTimer();
        createScheduler();
    }

}
