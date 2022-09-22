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

import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.ScanStatusResponse;
import com.xcal.api.repository.ProjectRepository;
import com.xcal.api.repository.ScanTaskRepository;
import com.xcal.api.repository.ScanTaskStatusLogRepository;
import com.xcal.api.util.VariableUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class ScanStatusServiceTest {
    private ScanStatusService scanStatusService;
    private ProjectRepository projectRepository;
    private ScanTaskRepository scanTaskRepository;
    private ScanTaskStatusLogRepository scanTaskStatusLogRepository;
    private I18nService i18nService;

    private String currentUserName = "user";
    private UUID projectUUID = UUID.fromString("11111111-1111-1111-1110-111111111111");
    private String projectId = "test project id";
    private String projectName = "testProject";
    private Project.Status projectStatus = Project.Status.ACTIVE;
    private String projectCreateBy = "projectCreateBy test user";
    private Project project = Project.builder().id(projectUUID).projectId(projectId).name(projectName).status(projectStatus).createdBy(projectCreateBy).build();
    private UUID scanTaskId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private ScanTask scanTask1 = ScanTask.builder().id(scanTaskId1).status(ScanTask.Status.PENDING).build();

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        scanTaskRepository = mock(ScanTaskRepository.class);
        scanTaskStatusLogRepository = mock(ScanTaskStatusLogRepository.class);
        i18nService = mock(I18nService.class);
        scanStatusService = new ScanStatusService(projectRepository, scanTaskRepository, scanTaskStatusLogRepository,i18nService);
        scanStatusService.scanStageNumber = 5;
    }

    @Test
    void getLatestScanStatusByProjectIdTestScanTaskStatusLogNotFoundFail() {
        log.info("[getLatestScanStatusByProjectIdTestScanTaskStatusLogNotFoundFail]");
        when(scanTaskStatusLogRepository.findFirst1ByScanTaskProject(project, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> scanStatusService.getLatestScanStatusByProject(project));
    }

    @Test
    void saveScanTaskStatusLogTestScanTaskByScanTaskSuccess() throws AppException {
        log.info("[saveScanTaskStatusLogTestScanTaskByScanTaskSuccess]");
        ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.FAILED).project(project).build();
        ScanTaskStatusLog scanTaskStatusLog = ScanTaskStatusLog.builder().percentage(0.0).stage(ScanTaskStatusLog.Stage.IMPORT_RESULT).status(ScanTaskStatusLog.Status.FAILED)
                .message("preparing source code").createdBy(currentUserName).createdOn(new Date()).build();
        when(scanTaskStatusLogRepository.findFirst1ByScanTask(scanTask, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(scanTaskStatusLog));
        when(scanTaskRepository.saveAndFlush(scanTask)).thenReturn(scanTask);
        when(scanTaskStatusLogRepository.saveAndFlush(any())).thenReturn(scanTaskStatusLog);
        ScanTaskStatusLog resultScanTaskStatusLog = scanStatusService.saveScanTaskStatusLog(scanTask, ScanTask.Status.COMPLETED, scanTaskStatusLog.getStage(), scanTaskStatusLog.getStatus(), scanTaskStatusLog.getPercentage(), null, scanTaskStatusLog.getMessage(), currentUserName);
        assertEquals(scanTaskStatusLog.getPercentage(), resultScanTaskStatusLog.getPercentage());
        assertEquals(scanTaskStatusLog.getStage(), resultScanTaskStatusLog.getStage());
        assertEquals(scanTaskStatusLog.getStatus(), resultScanTaskStatusLog.getStatus());
        assertEquals(scanTaskStatusLog.getMessage(), resultScanTaskStatusLog.getMessage());
        assertEquals(scanTaskStatusLog.getCreatedBy(), resultScanTaskStatusLog.getCreatedBy());
        assertEquals(scanTaskStatusLog.getCreatedOn(), resultScanTaskStatusLog.getCreatedOn());
    }

    @Test
    void saveScanTaskStatusLogTestScanTaskStatusLogIsNullSuccess() {
        log.info("[saveScanTaskStatusLogTestScanTaskStatusLogIsNullSuccess]");
        ScanTaskStatusLog scanTaskStatusLog = ScanTaskStatusLog.builder().percentage(0.0).stage(ScanTaskStatusLog.Stage.IMPORT_RESULT).status(ScanTaskStatusLog.Status.FAILED)
                .message("preparing source code").createdBy(currentUserName).createdOn(new Date()).build();
        when(scanTaskStatusLogRepository.findFirst1ByScanTask(scanTask1, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(ScanTaskStatusLog.builder().stage(ScanTaskStatusLog.Stage.PENDING).build()));
        when(scanTaskStatusLogRepository.saveAndFlush(any())).thenReturn(scanTaskStatusLog);
        ScanTaskStatusLog resultScanTaskStatusLog = scanStatusService.saveScanTaskStatusLog(scanTask1, ScanTaskStatusLog.Stage.FETCH_SOURCE, ScanTaskStatusLog.Status.FAILED, 0.0, null, "preparing source code", currentUserName);
        assertEquals(scanTaskStatusLog, resultScanTaskStatusLog);
    }

    @Test
    void convertScanTaskStatusLogToResponse_NormalCase() throws AppException {
        ProjectConfig projectConfig = ProjectConfig.builder().build();
        List<ProjectConfigAttribute> attributes = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue)
                        .value("c++").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue)
                        .value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.BRANCH.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.BRANCH.nameValue)
                        .value("dev").build()
        );
        projectConfig.setAttributes(attributes);

        ScanTask scanTask = ScanTask.builder()
                .id(UUID.randomUUID())
                .project(project)
                .projectConfig(projectConfig)
                .status(ScanTask.Status.PROCESSING).build();
        String statusMessage = "Test Message";
        ScanTaskStatusLog scanTaskStatusLog = ScanTaskStatusLog.builder()
                .scanTask(scanTask)
                .stage(ScanTaskStatusLog.Stage.FETCH_PRE_PROCESS_INFO)
                .percentage(50.0)
                .message(statusMessage)
                .status(ScanTaskStatusLog.Status.PROCESSING)
                .build();
        Locale locale = Locale.ENGLISH;
        String localizedStatusMessage = "Localized message";
        ScanStatusResponse expectedResult = ScanStatusResponse.builder()
                .stage(ScanTaskStatusLog.Stage.FETCH_PRE_PROCESS_INFO.name())
                .status(ScanTaskStatusLog.Status.PROCESSING.name())
                .percentage(ScanTaskStatusLog.Stage.FETCH_PRE_PROCESS_INFO.start + (ScanTaskStatusLog.Stage.FETCH_PRE_PROCESS_INFO.range * 0.5))
                .projectId(project.getId())
                .message(localizedStatusMessage)
                .build();

        when(i18nService.formatString(anyString(), any(Locale.class))).thenAnswer(i -> i.getArguments()[0]);
        when(i18nService.formatString(endsWith(scanTaskStatusLog.getMessage()), any(Locale.class)))
                .thenReturn(localizedStatusMessage);

        ScanStatusResponse scanStatusResponse = this.scanStatusService.convertScanTaskStatusLogToResponse(scanTaskStatusLog, locale);

        assertEquals(expectedResult.getMessage(), scanStatusResponse.getMessage());
        assertEquals(expectedResult.getPercentage(), scanStatusResponse.getPercentage());
        assertEquals(expectedResult.getStage(), scanStatusResponse.getStage());
        assertEquals(expectedResult.getStatus(), scanStatusResponse.getStatus());
    }

    @Test
    void convertScanTaskStatusLogToResponse_CompletedCase() throws AppException {
        ProjectConfig projectConfig = ProjectConfig.builder().build();
        List<ProjectConfigAttribute> attributes = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue)
                        .value("c++").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue)
                        .value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.BRANCH.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.BRANCH.nameValue)
                        .value("dev").build()
        );
        projectConfig.setAttributes(attributes);

        ScanTask scanTask = ScanTask.builder()
                .id(UUID.randomUUID())
                .project(project)
                .projectConfig(projectConfig)
                .status(ScanTask.Status.COMPLETED).build();
        String statusMessage = "Test Message";
        ScanTaskStatusLog scanTaskStatusLog = ScanTaskStatusLog.builder()
                .scanTask(scanTask)
                .stage(ScanTaskStatusLog.Stage.SCAN_COMPLETE)
                .percentage(100.0)
                .message(statusMessage)
                .status(ScanTaskStatusLog.Status.COMPLETED)
                .build();
        Locale locale = Locale.ENGLISH;
        String localizedStatusMessage = "Localized message";
        ScanStatusResponse expectedResult = ScanStatusResponse.builder()
                .stage(ScanTaskStatusLog.Stage.SCAN_COMPLETE.name())
                .status(ScanTaskStatusLog.Status.COMPLETED.name())
                .percentage(ScanTaskStatusLog.Stage.SCAN_COMPLETE.start + (ScanTaskStatusLog.Stage.SCAN_COMPLETE.range * 1))
                .projectId(project.getId())
                .message(localizedStatusMessage)
                .build();

        when(i18nService.formatString(anyString(), any(Locale.class))).thenAnswer(i -> i.getArguments()[0]);
        when(i18nService.formatString(endsWith(scanTaskStatusLog.getMessage()), any(Locale.class)))
                .thenReturn(localizedStatusMessage);

        ScanStatusResponse scanStatusResponse = this.scanStatusService.convertScanTaskStatusLogToResponse(scanTaskStatusLog, locale);

        assertEquals(expectedResult.getMessage(), scanStatusResponse.getMessage());
        assertEquals(expectedResult.getPercentage(), scanStatusResponse.getPercentage());
        assertEquals(expectedResult.getStage(), scanStatusResponse.getStage());
        assertEquals(expectedResult.getStatus(), scanStatusResponse.getStatus());
    }

    @Test
    void convertScanTaskStatusLogToResponse_FailedCase() throws AppException {
        ProjectConfig projectConfig = ProjectConfig.builder().build();
        List<ProjectConfigAttribute> attributes = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue)
                        .value("c++").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue)
                        .value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.BRANCH.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.BRANCH.nameValue)
                        .value("dev").build()
        );
        projectConfig.setAttributes(attributes);

        ScanTask scanTask = ScanTask.builder()
                .id(UUID.randomUUID())
                .project(project)
                .projectConfig(projectConfig)
                .status(ScanTask.Status.FAILED).build();
        String statusMessage = "Test Message";
        ScanTaskStatusLog scanTaskStatusLog = ScanTaskStatusLog.builder()
                .scanTask(scanTask)
                .stage(ScanTaskStatusLog.Stage.FETCH_PRE_PROCESS_INFO)
                .percentage(50.0)
                .message(statusMessage)
                .status(ScanTaskStatusLog.Status.FAILED)
                .build();
        Locale locale = Locale.ENGLISH;
        String localizedStatusMessage = "Localized message";
        ScanStatusResponse expectedResult = ScanStatusResponse.builder()
                .stage(ScanTaskStatusLog.Stage.FETCH_PRE_PROCESS_INFO.name())
                .status(ScanTaskStatusLog.Status.FAILED.name())
                .percentage(ScanTaskStatusLog.Stage.FETCH_PRE_PROCESS_INFO.start + (ScanTaskStatusLog.Stage.FETCH_PRE_PROCESS_INFO.range * 0.5))
                .projectId(project.getId())
                .message(localizedStatusMessage)
                .build();

        when(i18nService.formatString(anyString(), any(Locale.class))).thenAnswer(i -> i.getArguments()[0]);
        when(i18nService.formatString(endsWith(scanTaskStatusLog.getMessage()), any(Locale.class)))
                .thenReturn(localizedStatusMessage);

        ScanStatusResponse scanStatusResponse = this.scanStatusService.convertScanTaskStatusLogToResponse(scanTaskStatusLog, locale);

        assertEquals(expectedResult.getMessage(), scanStatusResponse.getMessage());
        assertEquals(expectedResult.getPercentage(), scanStatusResponse.getPercentage());
        assertEquals(expectedResult.getStage(), scanStatusResponse.getStage());
        assertEquals(expectedResult.getStatus(), scanStatusResponse.getStatus());
    }

    @Test
    void convertScanTaskStatusLogToResponse_TernimatedCase() throws AppException {
        ProjectConfig projectConfig = ProjectConfig.builder().build();
        List<ProjectConfigAttribute> attributes = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue)
                        .value("c++").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue)
                        .value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.BRANCH.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.BRANCH.nameValue)
                        .value("dev").build()
        );
        projectConfig.setAttributes(attributes);

        ScanTask scanTask = ScanTask.builder()
                .id(UUID.randomUUID())
                .project(project)
                .projectConfig(projectConfig)
                .status(ScanTask.Status.TERMINATED).build();
        String statusMessage = "Test Message";
        ScanTaskStatusLog scanTaskStatusLog = ScanTaskStatusLog.builder()
                .scanTask(scanTask)
                .stage(ScanTaskStatusLog.Stage.FETCH_SOURCE)
                .percentage(50.0)
                .message(statusMessage)
                .status(ScanTaskStatusLog.Status.TERMINATED)
                .build();
        Locale locale = Locale.ENGLISH;
        String localizedStatusMessage = "Localized message";
        ScanStatusResponse expectedResult = ScanStatusResponse.builder()
                .stage(ScanTaskStatusLog.Stage.FETCH_SOURCE.name())
                .status(ScanTaskStatusLog.Status.TERMINATED.name())
                .percentage(ScanTaskStatusLog.Stage.FETCH_SOURCE.start + (ScanTaskStatusLog.Stage.FETCH_SOURCE.range * 0.5))
                .projectId(project.getId())
                .message(localizedStatusMessage)
                .build();

        when(i18nService.formatString(anyString(), any(Locale.class))).thenAnswer(i -> i.getArguments()[0]);
        when(i18nService.formatString(endsWith(scanTaskStatusLog.getMessage()), any(Locale.class)))
                .thenReturn(localizedStatusMessage);

        ScanStatusResponse scanStatusResponse = this.scanStatusService.convertScanTaskStatusLogToResponse(scanTaskStatusLog, locale);

        assertEquals(expectedResult.getMessage(), scanStatusResponse.getMessage());
        assertEquals(expectedResult.getPercentage(), scanStatusResponse.getPercentage());
        assertEquals(expectedResult.getStage(), scanStatusResponse.getStage());
        assertEquals(expectedResult.getStatus(), scanStatusResponse.getStatus());
    }
}
