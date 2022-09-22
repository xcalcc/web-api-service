/*
   Copyright (C) 2019-2022 Xcalibyte (Shenzhen) Limited.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.xcal.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DateUtilTest {

    @Test
    void dateToTimeZone_lessThanNegative720_IllegalArgumentException() {
        Date date = new Date(2021, 1, 3);
        assertThrows(IllegalArgumentException.class, () -> DateUtil.dateToTimeZone(date, -800));

    }

    @Test
    void dateToTimeZone_greaterThan840_IllegalArgumentException() {
        Date date = new Date(2021, 1, 3);
        assertThrows(IllegalArgumentException.class, () -> DateUtil.dateToTimeZone(date, 900));
    }

    @Test
    void dateToTimeZone_equal840_IllegalArgumentException() {
        Date date = new Date(2021, 1, 3);
        Date timezoneDate = DateUtil.dateToTimeZone(date, 840);
        assertEquals(14,timezoneDate.getHours());
        assertEquals(0,timezoneDate.getMinutes());
    }

    @Test
    void dateToTimeZone_equalNegative720_IllegalArgumentException() {
        Date date = new Date(2021, 1, 3);
        Date timezoneDate = DateUtil.dateToTimeZone(date, -720);
        assertEquals(12,timezoneDate.getHours());
        assertEquals(0,timezoneDate.getMinutes());
    }

    @Test
    void dateToTimeZone_equal60_IllegalArgumentException() {
        Date date = new Date(2021, 1, 3);
        Date timezoneDate = DateUtil.dateToTimeZone(date, +60);
        assertEquals(1,timezoneDate.getHours());
        assertEquals(0,timezoneDate.getMinutes());
    }

    @Test
    void dateToTimeZone_equal480China_IllegalArgumentException() {
        Date date = new Date(2021, 1, 3);
        Date timezoneDate = DateUtil.dateToTimeZone(date, +480);
        assertEquals(8,timezoneDate.getHours());
        assertEquals(0,timezoneDate.getMinutes());
    }

}