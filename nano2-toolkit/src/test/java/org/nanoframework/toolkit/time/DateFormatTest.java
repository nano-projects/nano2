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
package org.nanoframework.toolkit.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

/**
 * @author yanghe
 * @since 2.0.0
 */
class DateFormatTest {

    @Test
    void getWithStringPatternTest() throws InterruptedException, ExecutionException {
        var pattern = "yyyy";
        var format = DateFormat.get(pattern);
        assertEquals(System.identityHashCode(format), System.identityHashCode(DateFormat.get(pattern)));

        var executor = Executors.newSingleThreadExecutor();
        var format2 = executor.submit(() -> DateFormat.get(pattern)).get();
        assertNotEquals(System.identityHashCode(format), System.identityHashCode(format2));

        executor.shutdown();
    }

    @Test
    void getWithPatternTest() {
        var date = Pattern.DATE;
        var format = DateFormat.get(date);
        assertEquals(format.toPattern(), date.get());
    }

    @Test
    void formatWithPatternTest() {
        formatTest(DateFormat.format(new Date(), Pattern.DATE));
    }

    @Test
    void formatWithStringPatternTest() {
        formatTest(DateFormat.format(new Date(), "yyyy-MM-dd"));
    }

    @Test
    void formatWithTimePatternTest() {
        formatTest(DateFormat.format(System.currentTimeMillis(), Pattern.DATE));
    }

    @Test
    void formatWithTimeStringPatternTest() {
        formatTest(DateFormat.format(System.currentTimeMillis(), "yyyy-MM-dd"));
    }

    private void formatTest(String date) {
        var c = Calendar.getInstance();
        var now = MessageFormat.format("{0}-{1}-{2}", String.valueOf(c.get(Calendar.YEAR)),
                String.format("%02d", c.get(Calendar.MONTH) + 1), String.format("%02d", c.get(Calendar.DAY_OF_MONTH)));
        assertEquals(date, now);
    }

    @Test
    void parseTest() throws ParseException {
        DateFormat.setTimeZone(Pattern.DATE, TimeZone.getTimeZone("Asia/Shanghai"));

        var time = 1514736000000L;
        var date = DateFormat.parse("2018-01-01", Pattern.DATE);
        assertEquals(date.getTime(), time);
    }

    @Test
    void parseWithStringPatternTest() throws ParseException {
        DateFormat.setTimeZone(Pattern.DATE, TimeZone.getTimeZone("Asia/Shanghai"));

        var time = 1514736000000L;
        var date = DateFormat.parse("2018-01-01", "yyyy-MM-dd");
        assertEquals(date.getTime(), time);
    }

}
