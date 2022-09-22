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

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    private DateUtil() {
    }

    /***
     * convert date to correct timezone using offset in offsetInMins
     * Valid offsetInMins value: -720 to +840  (ie. -12 hours to +14 hours)
     * eg. China +8 hour is +480 minutes
     * @param date
     * @param offsetInMins
     * @return The localized Date object
     */
    public static Date dateToTimeZone(Date date, int offsetInMins) {
        if (date == null) {
            return null;
        }

        if (offsetInMins<-720 || offsetInMins>840) {
            throw new IllegalArgumentException("Invalid time zone:"+offsetInMins);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, offsetInMins);
        return calendar.getTime();
    }
}
