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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class ScanMetricTest {

    ScanMetric scanMetric;

    @BeforeEach
    void setUp() {
        scanMetric = new ScanMetric();
    }

    @Test
    void handleProjectScanMetrics_Success() {
        log.info("[handleProjectScanMetrics_Success]");
        scanMetric.handleProjectScanMetricsInc();
        Assertions.assertEquals(1, scanMetric.getProjectScanProcessCount().get());
        scanMetric.handleProjectScanMetricsInc();
        scanMetric.handleProjectScanMetricsInc();
        Assertions.assertEquals(3, scanMetric.getProjectScanProcessCount().get());

        scanMetric.handleProjectScanMetricsInc();
        scanMetric.handleProjectScanMetricsInc();
        scanMetric.handleProjectScanMetricsInc();
        scanMetric.handleProjectScanMetricsInc();
        Assertions.assertEquals(7, scanMetric.getProjectScanProcessCount().get());
        scanMetric.handleProjectScanMetricsDec();
        scanMetric.handleProjectScanMetricsDec();
        Assertions.assertEquals(5, scanMetric.getProjectScanProcessCount().get());
    }

    @Test
    void handleProjectScanMetrics_DecreaseLowerThanZero() {
        log.info("[handleProjectScanMetrics_DecreaseLowerThanZero]");
        scanMetric.handleProjectScanMetricsInc();
        Assertions.assertEquals(1, scanMetric.getProjectScanProcessCount().get());
        scanMetric.handleProjectScanMetricsDec();
        scanMetric.handleProjectScanMetricsDec();
        Assertions.assertEquals(0, scanMetric.getProjectScanProcessCount().get());

        scanMetric.handleProjectScanMetricsInc();
        scanMetric.handleProjectScanMetricsDec();
        scanMetric.handleProjectScanMetricsDec();
        scanMetric.handleProjectScanMetricsDec();
        Assertions.assertEquals(0, scanMetric.getProjectScanProcessCount().get());

        scanMetric.handleProjectScanMetricsDec();
        scanMetric.handleProjectScanMetricsDec();
        scanMetric.handleProjectScanMetricsInc();
        scanMetric.handleProjectScanMetricsInc();
        scanMetric.handleProjectScanMetricsInc();
        scanMetric.handleProjectScanMetricsInc();
        Assertions.assertEquals(4, scanMetric.getProjectScanProcessCount().get());
    }

    @Test
    void handleScanLineNumMetrics_Success() {
        log.info("[handleScanLineNumMetrics_Success]");

        scanMetric.handleScanLineNumMetricsInc(100);
        Assertions.assertEquals(100, scanMetric.getScanLineNum().get());
        scanMetric.handleScanLineNumMetricsInc(200);
        scanMetric.handleScanLineNumMetricsInc(50);
        Assertions.assertEquals(350, scanMetric.getScanLineNum().get());

        scanMetric.handleScanLineNumMetricsDec(90);
        Assertions.assertEquals(260, scanMetric.getScanLineNum().get());
        scanMetric.handleScanLineNumMetricsDec(200);
        scanMetric.handleScanLineNumMetricsInc(400);
        Assertions.assertEquals(460, scanMetric.getScanLineNum().get());
    }

    @Test
    void handleScanLineNumMetrics_DecreaseLowerThanZero() {
        log.info("[handleScanLineNumMetrics_DecreaseLowerThanZero]");

        scanMetric.handleScanLineNumMetricsInc(100);
        Assertions.assertEquals(100, scanMetric.getScanLineNum().get());
        scanMetric.handleScanLineNumMetricsInc(200);
        scanMetric.handleScanLineNumMetricsInc(50);
        Assertions.assertEquals(350, scanMetric.getScanLineNum().get());

        scanMetric.handleScanLineNumMetricsDec(90);
        Assertions.assertEquals(260, scanMetric.getScanLineNum().get());
        scanMetric.handleScanLineNumMetricsDec(300);
        scanMetric.handleScanLineNumMetricsInc(400);
        Assertions.assertEquals(400, scanMetric.getScanLineNum().get());
    }

    @Test
    void handleScanFileSizeMetrics_Success() {
        log.info("[handleScanFileSizeMetrics_Success]");

        scanMetric.handleScanFileSizeMetricsInc(100L);
        Assertions.assertEquals(100, scanMetric.getScanFileSize().get());
        scanMetric.handleScanFileSizeMetricsInc(200L);
        scanMetric.handleScanFileSizeMetricsInc(50L);
        Assertions.assertEquals(350, scanMetric.getScanFileSize().get());

        scanMetric.handleScanFileSizeMetricsDec(90L);
        Assertions.assertEquals(260, scanMetric.getScanFileSize().get());
        scanMetric.handleScanFileSizeMetricsDec(200L);
        scanMetric.handleScanFileSizeMetricsInc(400L);
        Assertions.assertEquals(460, scanMetric.getScanFileSize().get());
    }

    @Test
    void handleScanFileSizeMetrics_DecreaseLowerThanZero() {
        log.info("[handleScanFileSizeMetrics_DecreaseLowerThanZero]");

        scanMetric.handleScanFileSizeMetricsInc(100L);
        Assertions.assertEquals(100L, scanMetric.getScanFileSize().get());
        scanMetric.handleScanFileSizeMetricsInc(200L);
        scanMetric.handleScanFileSizeMetricsInc(50L);
        Assertions.assertEquals(350, scanMetric.getScanFileSize().get());

        scanMetric.handleScanFileSizeMetricsDec(90L);
        Assertions.assertEquals(260, scanMetric.getScanFileSize().get());
        scanMetric.handleScanFileSizeMetricsDec(300L);
        scanMetric.handleScanFileSizeMetricsInc(400L);
        Assertions.assertEquals(400, scanMetric.getScanFileSize().get());
    }
}