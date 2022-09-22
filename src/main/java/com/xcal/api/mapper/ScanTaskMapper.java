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

package com.xcal.api.mapper;

import com.xcal.api.entity.v3.ScanTask;
import com.xcal.api.entity.v3.ScanTaskLog;
import com.xcal.api.model.payload.v3.ScanTaskIdResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ScanTaskMapper {

    List<ScanTaskLog> searchScanTaskLog(@Param("projectId") UUID projectId,
                                        @Param("targetRangeStartDate") Date targetRangeStartDate,
                                        @Param("targetRangeEndDate") Date targetRangeEndDate,
                                        @Param("commitIdPattern") String commitIdPattern,
                                        @Param("isDsrProject") Boolean isDsrProject,
                                        @Param("ruleSets") List<String> ruleSets,
                                        @Param("repoActions") List<String> repoActions,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    Long getScanTaskLogCount(@Param("projectId") UUID projectId,
                             @Param("targetRangeStartDate") Date targetRangeStartDate,
                             @Param("targetRangeEndDate") Date targetRangeEndDate,
                             @Param("commitIdPattern") String commitIdPattern,
                             @Param("isDsrProject") Boolean isDsrProject,
                             @Param("ruleSets") List<String> ruleSets,
                             @Param("repoActions") List<String> repoActions);


    ScanTaskIdResponse getScanTaskIdResponse(@Param("projectId") UUID projectId,
                                             @Param("commitId") String commitId
    );

    Optional<ScanTask> getScanTaskById(@Param("projectId") UUID projectId);

    Optional<ScanTask> getLastScanTaskByProjectId(@Param("projectId") UUID projectId, @Param("status") String status);

    Optional<ScanTask> getLastScanTaskByScanTask(@Param("projectId") UUID projectId, @Param("scanTaskId") UUID scanTaskId, @Param("status") String status);

    Optional<ScanTask> getFirstScanTaskWithBaseline(@Param("projectId") UUID projectId);

    Optional<String> getScanTaskIdFromProjectAndCommitId(@Param("projectId") UUID projectId, @Param("commitId") String commitId, @Param("status") String status);

    Optional<String> getCommitIdByScanTaskId(@Param("scanTaskId") UUID scanTaskId);

}
