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

package com.xcal.api.metric;

import io.micrometer.core.instrument.Metrics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component("scanMetric")
@Getter
public class ScanMetric {

    private final AtomicInteger projectScanProcessCount;
    private final AtomicInteger scanLineNum;
    private final AtomicLong scanFileSize;

    public ScanMetric() {
        projectScanProcessCount = Metrics.gauge("xcal.project.scan.process.count", new AtomicInteger(0));
        scanLineNum = Metrics.gauge("xcal.scan.lineNum", new AtomicInteger(0));
        scanFileSize = Metrics.gauge("xcal.scan.fileSize", new AtomicLong(0));
    }
    public void handleProjectScanMetricsInc() {
        projectScanProcessCount.addAndGet(1);
        log.info("[handleProjectScanMetricsInc] project scan process instance count: {}", projectScanProcessCount);
    }


    public void handleProjectScanMetricsDec() {
        int val = projectScanProcessCount.addAndGet(-1);
        if (val < 0) {
            projectScanProcessCount.set(0);
        }
        log.info("[handleProjectScanMetricsDec] project scan process instance count: {}", projectScanProcessCount);
    }


    public void handleScanLineNumMetricsInc(int lineNum) {
        scanLineNum.addAndGet(lineNum);
        log.info("[handleScanLineNumMetricsInc] scan line number: {}", scanLineNum);
    }


    public void handleScanLineNumMetricsDec(int lineNum) {
        int val = scanLineNum.addAndGet(-lineNum);
        if (val < 0) {
            scanLineNum.set(0);
        }
        log.info("[handleScanLineNumMetricsDec] scan line number: {}", scanLineNum);
    }


    public void handleScanFileSizeMetricsInc(Long fileSize) {
        scanFileSize.addAndGet(fileSize);
        log.info("[handleScanFileSizeMetricsInc] scan file size: {}", scanFileSize);
    }


    public void handleScanFileSizeMetricsDec(Long fileSize) {
        long val = scanFileSize.addAndGet(-fileSize);
        if (val < 0) {
            scanFileSize.set(0);
        }
        log.info("[handleScanLineNumMetricsDec] scan file size: {}", scanFileSize);
    }
}
