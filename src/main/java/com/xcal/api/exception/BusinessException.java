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

import com.xcal.api.entity.ScanTask;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BusinessException extends Exception {

    public static final int DEFAULT_RETRY_INTERVAL_MS = 60000; //1 min

    public enum SP_SETUP {
        SET_PROJ_CONF(0),
        SET_CR_PROJ(1),
        SET_SCM_SRC(2);
        private int val;

        SP_SETUP(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    public enum SP_PREPROC {
        PRE_PREP_SRC(8),
        PRE_GET_SRC(9),
        PRE_BUILD(10),
        PRE_UP_FILE(11);
        private int val;

        SP_PREPROC(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    public enum SP_PROC {
        PROC_XVSA_MERGE(16),
        PROC_DIFF(17),
        PROC_CSF(18);
        private int val;

        SP_PROC(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    public enum SP_POSTPROC {
        POST_INJECT_DB(24),
        POST_HISTORY(25),
        POST_COLL_RES(26),
        POST_RPT_RES(27);
        private int val;

        SP_POSTPROC(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    public enum STATUS {
        SUCC(0),
        COND_SUCC(1),
        FAILED(2),
        FATAL(3),
        CANCEL(4);
        private int val;

        STATUS(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable e) {
        super(message, e);
    }

    private static Map<String, String> statusMap = new HashMap();

    /* Initialize map*/
    static {
        statusMap.put(BusinessException.STATUS.FAILED.name(), ScanTask.Status.FAILED.name());
        statusMap.put(BusinessException.STATUS.FATAL.name(), ScanTask.Status.FAILED.name());
        statusMap.put(BusinessException.STATUS.CANCEL.name(), ScanTask.Status.TERMINATED.name());
    }

    public static String mapServiceStatusToScanTaskStatus(String status) {
        if (statusMap.containsKey(status)) {
            return statusMap.get(status);
        } else {
            log.warn("[mapServiceStatusToScanTaskStatus] no match key found for:{}", status);
            return ScanTask.Status.FAILED.name();
        }
    }

}
