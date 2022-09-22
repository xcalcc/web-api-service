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

package com.xcal.api.repository;

import com.xcal.api.entity.Project;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.entity.ScanTaskStatusLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScanTaskStatusLogRepository extends JpaRepository<ScanTaskStatusLog, UUID> {

    List<ScanTaskStatusLog> findByScanTaskAndStageAndStatusAndPercentage(ScanTask scanTask, ScanTaskStatusLog.Stage stage, ScanTaskStatusLog.Status status, Double percentage);
    Optional<ScanTaskStatusLog> findFirst1ByScanTask(ScanTask scanTask, Sort sort);

    Optional<ScanTaskStatusLog> findFirst1ByScanTaskProject(Project project,Sort sort);

    List<ScanTaskStatusLog> findByScanTaskProject(Project project);

    List<ScanTaskStatusLog> findByScanTaskId(UUID scanTaskId,Sort sort);
}
