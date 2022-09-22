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

package com.xcal.api.config;

import com.xcal.api.entity.FileInfo;
import com.xcal.api.service.FileService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskSchedulerConfig {

    @Value("${housekeep.temp-file.retain-period.max.msec:43200000}")
    private int tempFileRetainPeriodMaxMsec;

    @NonNull
    private final FileService fileService;

    @Scheduled(fixedDelayString = "${housekeep.temp-file.fixed-delay.msec}", initialDelayString = "${housekeep.temp-file.initial-delay.msec}")
    public void scheduleDeleteTempTypeFileInfoTask() {
        log.info("[scheduleDeleteTempTypeFileInfoTask]");
        fileService.deleteFileInfoBeforeMilliseconds(FileInfo.Type.TEMP, tempFileRetainPeriodMaxMsec);
    }
}
