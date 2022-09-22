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

package com.xcal.api.service;

import com.xcal.api.config.AppProperties;
import com.xcal.api.entity.Project;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.entity.User;
import com.xcal.api.repository.OrchestrationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrchestrationService {

    @NonNull ScanTaskService scanTaskService;
    @NonNull ScanFileService scanFileService;
    @NonNull OrchestrationRepository orchestrationRepository;

    @NonNull AppProperties appProperties;

    public void deleteAllFilesRelatedToProject(Project project) {
        log.debug("[deleteAllFilesRelatedToProject] project: {}", project);
        switch (appProperties.getFileDeleteOption()){
            case ALL:
                List<ScanTask> scanTasks = this.scanTaskService.findByProject(project);
                this.scanFileService.deleteProjectIdFolder(project);
                this.scanFileService.deleteScanTasksFolder(scanTasks);
                break;
            case SCAN_FILE:
                this.scanFileService.deleteFileOfProject(project);
                break;
            case NONE:
                break;
            default:
                break;
        }
    }

    public void deleteAllInProject(Project project, boolean deleteRecord){
        log.debug("[deleteAllInProject] project: {}, deleteRecord: {}", project, deleteRecord);
        deleteAllFilesRelatedToProject(project);
        this.orchestrationRepository.deleteProject(project, deleteRecord);
    }

    public void deleteAllInScanTask(ScanTask scanTask, boolean deleteRecord, User currentUser){
        log.debug("[deleteAllInScanTask] scanTask: {}, deleteRecord: {}", scanTask, deleteRecord);
        switch (appProperties.getFileDeleteOption()) {
            case ALL:
                this.scanFileService.deleteScanTaskFolder(scanTask);
                break;
            case SCAN_FILE:
                this.scanFileService.deleteFileOfScanTask(scanTask);
                break;
            case NONE:
                break;
            default:
                break;
        }
        this.orchestrationRepository.deleteScanTask(scanTask, deleteRecord, currentUser);
    }
}
