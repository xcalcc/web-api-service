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

import com.xcal.api.service.HouseKeepService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HouseKeepConfig {

    @NonNull HouseKeepService houseKeepService;

    @Scheduled(cron = "${housekeep.expired-file.schedule.cron}")
    public void scheduledHouseKeepingTask() {
        try {
            log.info("[scheduledHouseKeepingTask] Start");
            houseKeepService.houseKeepFileSystemByAge();
            houseKeepService.houseKeepScanData();
            houseKeepService.houseKeepNotReferencedFileInfo();
            log.info("[scheduledHouseKeepingTask] End");
        } catch (Exception e) {
            log.error("[scheduledHouseKeepingTask] Exception occurred", e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startupHouseKeep() {
        try {
            log.info("[startupHouseKeep] Start");
            houseKeepService.houseKeepFileSystemByAge();
            houseKeepService.houseKeepScanData();
            log.info("[startupHouseKeep] End");
        } catch (Exception e) {
            log.error("[startupHouseKeep] Exception occurred", e);
        }
    }

    /***
     * Not require to set to fail when recovery is ready
     * */
    @Deprecated
    public void startupScanTaskHouseKeep() {
        try {
            log.info("[startupScanTaskHouseKeep] Start");

            int updatedPendingCount = houseKeepService.houseKeepScanTasks("PENDING", "FAILED");
            log.info("[startupScanTaskHouseKeep] {} record updated from PENDING to FAILED", updatedPendingCount);

            int updatedProcessingCount = houseKeepService.houseKeepScanTasks("PROCESSING", "FAILED");
            log.info("[startupScanTaskHouseKeep] {} record updated from PROCESSING to FAILED", updatedProcessingCount);

            log.info("[startupScanTaskHouseKeep] End");
        } catch (Exception e) {
            log.error("[startupScanTaskHouseKeep] Exception occurred", e);
        }
    }

}
