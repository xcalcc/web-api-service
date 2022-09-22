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

package com.xcal.api.exception;

import com.xcal.api.entity.Issue;
import com.xcal.api.entity.IssueAttribute;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.util.VariableUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
class BusinessExceptionTest {
    @BeforeEach
    void setup() {

    }

    @Test
    void mapServiceStatusToScanTaskStatus_failed_failed() {
        Assertions.assertEquals(ScanTask.Status.FAILED.name(),BusinessException.mapServiceStatusToScanTaskStatus(BusinessException.STATUS.FAILED.name()));
    }

    @Test
    void mapServiceStatusToScanTaskStatus_fatal_failed() {
        Assertions.assertEquals(ScanTask.Status.FAILED.name(),BusinessException.mapServiceStatusToScanTaskStatus(BusinessException.STATUS.FATAL.name()));
    }

    @Test
    void mapServiceStatusToScanTaskStatus_cancel_terminated() {
        Assertions.assertEquals(ScanTask.Status.TERMINATED.name(),BusinessException.mapServiceStatusToScanTaskStatus(BusinessException.STATUS.CANCEL.name()));
    }

    @Test
    void mapServiceStatusToScanTaskStatus_other_failed() {
        Assertions.assertEquals(ScanTask.Status.FAILED.name(),BusinessException.mapServiceStatusToScanTaskStatus("other string"));
    }

}
