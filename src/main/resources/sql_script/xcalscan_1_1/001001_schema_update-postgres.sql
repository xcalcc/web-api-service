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

CREATE TABLE IF NOT EXISTS xcalibyte."issue_diff"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    scan_task_id                    UUID              NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    baseline_scan_task_id           UUID              NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    issue_id                        UUID              NULL REFERENCES xcalibyte."issue" (id) ON DELETE CASCADE,
    baseline_issue_key              VARCHAR(2048)     NULL,
    issue_key                       VARCHAR(2048)     NULL,
    checksum                        VARCHAR(255)      NULL,
    type                            VARCHAR(255)      NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_issueDiff_scanTask ON xcalibyte."issue_diff" (scan_task_id);
CREATE INDEX IF NOT EXISTS idx_issueDiff_scanTaskBaseline ON xcalibyte."issue_diff" (baseline_scan_task_id);
CREATE INDEX IF NOT EXISTS idx_issueDiff_issueChecksum ON xcalibyte."issue_diff" (issue_id, checksum);
CREATE INDEX IF NOT EXISTS idx_issueDiff_issueKey ON xcalibyte."issue_diff" (issue_key);
