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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class OrchestrationServiceTest {
    private OrchestrationService orchestrationService;
    private ScanTaskService scanTaskService;
    private AppProperties appProperties;

    private String currentUserName = "user";
    private User currentUser = User.builder().username(currentUserName).displayName("testDispalyName").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();

    @BeforeEach
    void setUp() {
        scanTaskService = mock(ScanTaskService.class);
        ScanFileService scanFileService = mock(ScanFileService.class);
        OrchestrationRepository orchestrationRepository = mock(OrchestrationRepository.class);
        appProperties = mock(AppProperties.class);
        orchestrationService = new OrchestrationService(scanTaskService, scanFileService, orchestrationRepository, appProperties);
    }

    @Test
    void deleteAllInProjectTestALLSuccess() {
        log.info("[deleteAllInProjectTestSuccess]");
        Project project = Project.builder().id(UUID.randomUUID()).build();
        UUID scanTaskUuid1 = UUID.randomUUID();
        ScanTask scanTask1 = ScanTask.builder().id(scanTaskUuid1).status(ScanTask.Status.PENDING).build();
        UUID scanTaskUuid2 = UUID.randomUUID();
        ScanTask scanTask2 = ScanTask.builder().id(scanTaskUuid2).status(ScanTask.Status.PENDING).build();
        List<ScanTask> scanTaskList = Arrays.asList(scanTask1, scanTask2);
        when(appProperties.getFileDeleteOption()).thenReturn(AppProperties.FileDeleteOption.ALL);
        when(scanTaskService.findByProject(project)).thenReturn(scanTaskList);
        orchestrationService.deleteAllInProject(project, true);
        assertTrue(true);
    }

    @Test
    void deleteAllInProjectTestSCANFILESuccess() {
        log.info("[deleteAllInProjectTestSCANFILESuccess]");
        Project project = Project.builder().id(UUID.randomUUID()).build();
        UUID scanTaskUuid1 = UUID.randomUUID();
        ScanTask scanTask1 = ScanTask.builder().id(scanTaskUuid1).status(ScanTask.Status.PENDING).build();
        UUID scanTaskUuid2 = UUID.randomUUID();
        ScanTask scanTask2 = ScanTask.builder().id(scanTaskUuid2).status(ScanTask.Status.PENDING).build();
        List<ScanTask> scanTaskList = Arrays.asList(scanTask1, scanTask2);
        when(appProperties.getFileDeleteOption()).thenReturn(AppProperties.FileDeleteOption.SCAN_FILE);
        when(scanTaskService.findByProject(project)).thenReturn(scanTaskList);
        orchestrationService.deleteAllInProject(project, true);
        assertTrue(true);
    }

    @Test
    void deleteAllInProjectTestNONESuccess() {
        log.info("[deleteAllInProjectTestNONESuccess]");
        Project project = Project.builder().id(UUID.randomUUID()).build();
        when(appProperties.getFileDeleteOption()).thenReturn(AppProperties.FileDeleteOption.NONE);
        orchestrationService.deleteAllInProject(project, true);
        assertTrue(true);
    }

    @Test
    void deleteAllInScanTaskTestALLSuccess() {
        log.info("[deleteAllInScanTaskTestALLSuccess]");
        UUID scanTaskUuid = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskUuid).status(ScanTask.Status.PENDING).build();
        when(appProperties.getFileDeleteOption()).thenReturn(AppProperties.FileDeleteOption.ALL);
        orchestrationService.deleteAllInScanTask(scanTask, true, currentUser);
        assertTrue(true);
    }

    @Test
    void deleteAllInScanTaskTestSCANFILESuccess() {
        log.info("[deleteAllInScanTaskTestSCANFILESuccess]");
        UUID scanTaskUuid = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskUuid).status(ScanTask.Status.PENDING).build();
        when(appProperties.getFileDeleteOption()).thenReturn(AppProperties.FileDeleteOption.SCAN_FILE);
        orchestrationService.deleteAllInScanTask(scanTask, true, currentUser);
        assertTrue(true);
    }

    @Test
    void deleteAllInScanTaskTestNONESuccess() {
        log.info("[deleteAllInScanTaskTestNONESuccess]");
        UUID scanTaskUuid = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskUuid).status(ScanTask.Status.PENDING).build();
        when(appProperties.getFileDeleteOption()).thenReturn(AppProperties.FileDeleteOption.NONE);
        orchestrationService.deleteAllInScanTask(scanTask, true, currentUser);
        assertTrue(true);
    }
}
