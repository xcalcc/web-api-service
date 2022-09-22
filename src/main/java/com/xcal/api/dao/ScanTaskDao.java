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

package com.xcal.api.dao;


import com.xcal.api.entity.v3.ScanTask;
import com.xcal.api.entity.v3.ScanTaskLog;
import com.xcal.api.model.payload.v3.ScanTaskIdResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScanTaskDao {

    Page<ScanTaskLog> searchScanTaskLog(UUID projectId, Date targetRangeStartDate, Date targetRangeEndDate, String commitIdPattern, Boolean isDsrProject, List<String> ruleSets, List<String> repoActions, Pageable pageable);

    public Long getScanTaskLogCount(
            UUID projectId, Date targetRangeStartDate, Date targetRangeEndDate, String commitIdPattern, Boolean isDsrProject,
            List<String> ruleSets, List<String> repoActions
    );

    ScanTaskIdResponse getScanTaskIdResponse(
            UUID projectId,
            String commitId
    );

    Optional<ScanTask> getScanTaskById(UUID projectId);

    Optional<ScanTask> getLastScanTaskByProjectId(UUID projectId);

    Optional<ScanTask> getLastScanTaskByProjectId(UUID projectId, String status);

    Optional<ScanTask> getLastScanTaskByScanTask(UUID projectId, UUID scanTaskId, String status);

    Optional<ScanTask> getFirstScanTaskWithBaseline( UUID projectId);

    Optional<String> getScanTaskIdFromProjectAndCommitId( UUID projectId, String commitId, String status);

    Optional<String> getCommitIdByScanTaskId( UUID scanTaskId);
}
