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

package com.xcal.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.config.WithMockCustomUser;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.ScanTaskDto;
import com.xcal.api.model.payload.*;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.service.*;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ScanControllerTest {

    @NonNull
    private final MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private ScanTaskService scanTaskService;

    @MockBean
    private IssueService issueService;

    @MockBean
    private ScanStatusService scanStatusService;

    @MockBean
    private OrchestrationService orchestrationService;

    @MockBean
    private MeasureService measureService;

    @MockBean
    private UserService userService;

    @MockBean
    private PerformanceService performanceService;

    @NonNull
    ObjectMapper om;

    @NonNull
    Tracer tracer;

    private final UUID scanTaskUUID = UUID.randomUUID();
    private final UUID projectUUID = UUID.randomUUID();
    private final UUID projectConfigUUID = UUID.randomUUID();
    private final UUID storageId = UUID.fromString("11111111-1111-1111-1111-111111111113");

    private final String adminUsername = "admin";
    private final ScanTaskStatusLog.Stage stage = ScanTaskStatusLog.Stage.PENDING;
    private final ScanTask.Status status = ScanTask.Status.PENDING;
    private final ScanTaskStatusLog.Status scanTaskStatusLogStatus = ScanTaskStatusLog.Status.PENDING;
    private final Double percentage = 10.0;
    private final String message = "testing";
    private final String sourceRoot = "/benchmark/c_testcase";
    private final String projectId = "project1";

    private final String projectName = "testProject";
    private final Project.Status projectStatus = Project.Status.ACTIVE;
    private final String projectCreateBy = "projectCreateBy test user";

    private final Project project = Project.builder().id(projectUUID).projectId(projectId)
            .name(projectName).status(projectStatus).createdBy(projectCreateBy).build();

    private ProjectConfig projectConfig;
    private final ScanTask scanTask = ScanTask.builder()
            .id(scanTaskUUID)
            .sourceRoot(sourceRoot)
            .status(status)
            .createdBy(adminUsername)
            .modifiedBy(adminUsername)
            .project(project)
            .build();

    private final ScanTaskDto scanTaskDto = ScanTaskDto.builder().id(scanTaskUUID)
            .sourceRoot(sourceRoot)
            .status(status.name())
            .createdBy(adminUsername)
            .modifiedBy(adminUsername)
            .projectUuid(projectUUID)
            .projectId(projectId)
            .build();

    @BeforeEach
    void setUp() {
        projectConfig = ProjectConfig.builder().id(projectConfigUUID).name("test_config_name")
                .attributes(new ArrayList<>())
                .status(ProjectConfig.Status.ACTIVE).project(project).build();
        List<ProjectConfigAttribute> projectConfigAttributes = new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                        .value("agentUser").value("xc5").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                        .value("agentAddress").value("dev_agent_service").build()));
        projectConfig.setAttributes(projectConfigAttributes);
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_WithValidParams_ShouldSuccess() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        FileStorage fileStorage = FileStorage.builder().id(storageId).name("agent").fileStorageType(FileStorage.Type.AGENT).
                fileStorageHost("/share/src").status(FileStorage.Status.PENDING).build();

        ScanTask expectedScanTask = ScanTask.builder()
                .id(scanTaskUUID)
                .project(project)
                .projectConfig(projectConfig)
                .sourceRoot(sourceRoot)
                .status(status)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .project(project)
                .build();

        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));
        when(fileStorageService.findByName(any())).thenReturn(Optional.of(fileStorage));
        when(scanTaskService.getLatestRunningScanTask(project)).thenReturn(Optional.empty());
        when(scanTaskService.getFileStorage(projectConfig)).thenReturn(fileStorage);
        when(scanTaskService.addScanTask(argThat(arg -> project.getId() == arg.getId()), anyList(), anyBoolean(), eq(adminUsername))).thenReturn(expectedScanTask);
        doNothing().when(scanTaskService).prepareAndCallScan(eq(scanTask), argThat(arg -> projectConfig.getId() == arg.getId()), eq(adminUsername));

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task", projectUUID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((expectedScanTask.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(expectedScanTask.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sourceRoot").value(expectedScanTask.getSourceRoot()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy").value(expectedScanTask.getCreatedBy()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modifiedBy").value(expectedScanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_ProjectNotFound_ShouldReturnNotFoundError() throws Exception {
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task", projectUUID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_ActiveProjectConfigNotFound_ShouldReturnNotFoundError() throws Exception {
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.empty());

        when(scanTaskService.addScanTask(any(Project.class), anyList(), anyBoolean(), anyString())).thenThrow(
                new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate, project.getId())));
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task", projectUUID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_ProjectConfigContentMissingScanTypeKeyValuePair_ShouldReturnInternalError() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task", projectUUID))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_OfflineAgentScanType_ShouldReturnInternalError() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("offline_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task", projectUUID))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_ProjectConfigContentMissingSourceStorageNameKeyValuePair_ShouldReturnInternalError() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task", projectUUID))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_WithRunningScanTaskExists_ShouldReturnRunningScanTask() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        FileStorage fileStorage = FileStorage.builder().id(storageId).name("agent").fileStorageType(FileStorage.Type.AGENT).
                fileStorageHost("/share/src").status(FileStorage.Status.PENDING).build();

        when(scanTaskService.getLatestRunningScanTask(project)).thenReturn(Optional.of(scanTask));
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));
        when(scanTaskService.getFileStorage(projectConfig)).thenReturn(fileStorage);
        when(fileStorageService.findByName(any())).thenReturn(Optional.of(fileStorage));

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task", projectUUID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((scanTask.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(scanTask.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sourceRoot").value(scanTask.getSourceRoot()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy").value(scanTask.getCreatedBy()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modifiedBy").value(scanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTaskWithStatus_WithValidParamsAndPendingStatus_ShouldSuccess() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        FileStorage fileStorage = FileStorage.builder().id(storageId).name("agent").fileStorageType(FileStorage.Type.AGENT).
                fileStorageHost("/share/src").status(FileStorage.Status.PENDING).build();

        ScanTask expectedScanTask = ScanTask.builder()
                .id(scanTaskUUID)
                .sourceRoot(sourceRoot)
                .status(status)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .project(project)
                .build();

        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));
        when(fileStorageService.findByName(any())).thenReturn(Optional.of(fileStorage));
        when(scanTaskService.getLatestRunningScanTask(project)).thenReturn(Optional.empty());
        when(scanTaskService.addScanTask(argThat(arg -> project.getId() == arg.getId()), anyList(), anyBoolean(), eq(adminUsername))).thenReturn(expectedScanTask);
        doNothing().when(scanTaskService).prepareAndCallScan(eq(scanTask), argThat(arg -> projectConfig.getId() == arg.getId()), eq(adminUsername));

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task/pending", projectUUID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((expectedScanTask.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(expectedScanTask.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sourceRoot").value(expectedScanTask.getSourceRoot()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy").value(expectedScanTask.getCreatedBy()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modifiedBy").value(expectedScanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTaskWithStatus_WithValidParamsAndStartStatus_ShouldSuccess() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        FileStorage fileStorage = FileStorage.builder().id(storageId).name("volume_src").
                fileStorageHost("/share/src").status(FileStorage.Status.PENDING).build();

        ScanTask expectedScanTask = ScanTask.builder()
                .id(scanTaskUUID)
                .sourceRoot(sourceRoot)
                .status(status)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .project(project)
                .build();

        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));
        when(fileStorageService.findByName(any())).thenReturn(Optional.of(fileStorage));
        when(scanTaskService.addScanTask(argThat(arg -> project.getId() == arg.getId()), anyList(), anyBoolean(), eq(adminUsername))).thenReturn(expectedScanTask);
        when(scanTaskService.getLatestRunningScanTask(project)).thenReturn(Optional.empty());

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task/start", projectUUID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((expectedScanTask.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(expectedScanTask.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sourceRoot").value(expectedScanTask.getSourceRoot()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy").value(expectedScanTask.getCreatedBy()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modifiedBy").value(expectedScanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTaskWithStatus_ProjectNotFound_ShouldReturnNotFoundError() throws Exception {
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task/start", projectUUID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTaskStatus_OfflineAgentScanTypeWithStartStatus_ShouldReturnInternalError() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("offline_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task/start", projectUUID))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTaskStatus_ProjectConfigContentMissingSourceStorageNameKeyValuePair_ShouldReturnInternalError() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task", projectUUID))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTaskStatus_WithRunningScanTaskExists_ShouldReturnRunningScanTask() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        FileStorage fileStorage = FileStorage.builder().id(storageId).name("volume_src").
                fileStorageHost("/share/src").status(FileStorage.Status.PENDING).build();

        when(scanTaskService.getLatestRunningScanTask(project)).thenReturn(Optional.of(scanTask));
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));
        when(fileStorageService.findByName(any())).thenReturn(Optional.of(fileStorage));

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task/start", projectUUID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((scanTask.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(scanTask.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sourceRoot").value(scanTask.getSourceRoot()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy").value(scanTask.getCreatedBy()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modifiedBy").value(scanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTaskStatus_WithInvalidStatusValue_ShouldReturnNotFoundError() throws Exception {
        projectConfig.getAttributes().addAll(new ArrayList<>(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build())));

        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(Optional.of(projectConfig));
        when(fileStorageService.findByName(any())).thenReturn(Optional.empty());

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/scan_service/v2/project/{id}/scan_task/test", projectUUID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getScanTaskWithValidUUIDIsOk() throws Exception {
        ScanTask expectedScanTask = ScanTask.builder().id(scanTaskUUID)
                .sourceRoot(sourceRoot)
                .status(status)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .project(Project.builder().id(projectUUID).projectId(projectId).status(Project.Status.ACTIVE).build())
                .build();

        when(scanTaskService.findById(eq(scanTaskUUID))).thenReturn(Optional.of(expectedScanTask));
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{uuid}", scanTaskUUID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((expectedScanTask.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(expectedScanTask.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sourceRoot").value(expectedScanTask.getSourceRoot()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy").value(expectedScanTask.getCreatedBy()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modifiedBy").value(expectedScanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getScanTaskWithInvalidUUIDShouldReturnNotFound() throws Exception {
        when(scanTaskService.findById(eq(scanTaskUUID))).thenReturn(Optional.empty());
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{uuid}", scanTaskUUID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateScanTaskStatusWhereUUIDParamEqualToUUIDGetFromUpdateScanTaskRequestIsOK() throws Exception {
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder()
                .id(scanTaskUUID)
                .stage(stage.toString())
                .status(status.toString())
                .percentage(percentage)
                .message(message)
                .build();

        ScanTask expectedScanTask = ScanTask.builder()
                .id(scanTaskUUID)
                .sourceRoot(sourceRoot)
                .status(status)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .project(Project.builder().id(projectUUID).projectId(projectId).status(Project.Status.ACTIVE).build())
                .build();

        when(scanTaskService.updateScanTaskStatus(eq(updateScanTaskRequest), any())).thenReturn(expectedScanTask);
        this.mockMvc.perform(MockMvcRequestBuilders
                .put("/api/scan_service/v2/scan_task/{uuid}", scanTaskUUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateScanTaskRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((expectedScanTask.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(expectedScanTask.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sourceRoot").value(expectedScanTask.getSourceRoot()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy").value(expectedScanTask.getCreatedBy()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modifiedBy").value(expectedScanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateScanTaskStatusWhereUUIDParamDifferentFromUUIDGetFromUpdateScanTaskRequestShouldReturnConflictError() throws Exception {
        UUID UUID1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID UUID2 = UUID.fromString("11111111-1111-1111-1112-111111111111");
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder()
                .id(UUID1)
                .stage(stage.toString())
                .status(status.toString())
                .percentage(percentage)
                .message(message)
                .build();

        this.mockMvc.perform(MockMvcRequestBuilders
                .put("/api/scan_service/v2/scan_task/{uuid}", UUID2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateScanTaskRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateScanTaskStatusByValidProjectIdIsOK() throws Exception {
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder()
                .id(scanTaskUUID)
                .stage(stage.toString())
                .status(status.toString())
                .percentage(percentage)
                .message(message)
                .build();

        ScanTask expectedScanTask = ScanTask.builder()
                .id(scanTaskUUID)
                .sourceRoot(sourceRoot)
                .status(status)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .project(Project.builder().id(projectUUID).projectId(projectId).status(Project.Status.ACTIVE).build())
                .build();

        Project project = Project.builder().id(projectUUID).projectId(projectId).status(Project.Status.ACTIVE).build();
        when(projectService.findById(project.getId())).thenReturn(Optional.of(project));
        when(scanTaskService.updateScanTaskStatus(eq(project), eq(updateScanTaskRequest), any())).thenReturn(expectedScanTask);
        this.mockMvc.perform(MockMvcRequestBuilders
                .put("/api/scan_service/v2/project/{id}/scan_task", project.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateScanTaskRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((expectedScanTask.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(expectedScanTask.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sourceRoot").value(expectedScanTask.getSourceRoot()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy").value(expectedScanTask.getCreatedBy()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modifiedBy").value(expectedScanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateScanTaskStatusByInvalidProjectIdShouldReturnNotFound() throws Exception {
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder()
                .id(scanTaskUUID)
                .stage(stage.toString())
                .status(status.toString())
                .percentage(percentage)
                .message(message)
                .build();

        Project project = Project.builder().id(projectUUID).projectId(projectId).status(Project.Status.ACTIVE).build();
        when(projectService.findById(project.getId())).thenReturn(Optional.empty());
        this.mockMvc.perform(MockMvcRequestBuilders
                .put("/api/scan_service/v2/project/{id}/scan_task", project.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateScanTaskRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getLatestScanStatusByProjectId() throws Exception {
        ScanTaskStatusLog scanTaskStatusLog = ScanTaskStatusLog.builder()
                .id(UUID.randomUUID())
                .scanTask(scanTask)
                .stage(stage)
                .status(scanTaskStatusLogStatus)
                .percentage(percentage)
                .build();
        ScanStatusResponse expectedScanStatusResponse = ScanStatusResponse.builder()
                .projectId(projectUUID)
                .scanTaskId(scanTaskUUID)
                .stage(stage.toString())
                .status(status.toString())
                .percentage(percentage)
                .message(message)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(scanStatusService.getLatestScanStatusByProject(project)).thenReturn(scanTaskStatusLog);
        when(scanStatusService.convertScanTaskStatusLogToResponse(scanTaskStatusLog, Locale.ENGLISH)).thenReturn(expectedScanStatusResponse);
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_task", projectUUID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").value(expectedScanStatusResponse.getProjectId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.scanTaskId").value(expectedScanStatusResponse.getScanTaskId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stage").value(expectedScanStatusResponse.getStage()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(expectedScanStatusResponse.getStatus()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.percentage").value(expectedScanStatusResponse.getPercentage()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(expectedScanStatusResponse.getMessage()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getLatestScanStatusByProjectId_ProjectNotExist__ProjectNotFound() throws Exception {
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_task", projectUUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void stopScan_Request_ReturnScanTaskDto() throws Exception {
        log.info("[stopScan_Request_ReturnScanTaskDto]");
        when(scanTaskService.stopScan(eq(scanTaskUUID), any())).thenReturn(scanTask);
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/scan_service/v2/scan_task/{id}", scanTaskUUID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(scanTaskDto.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectUuid").value(scanTaskDto.getProjectUuid().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").value(scanTaskDto.getProjectId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(scanTaskDto.getStatus()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sourceRoot").value(scanTaskDto.getSourceRoot()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy").value(scanTaskDto.getCreatedBy()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modifiedBy").value(scanTaskDto.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteScanTask_ScanTaskNotFound_ThrowException() throws Exception {
        log.info("[deleteScanTask_ScanTaskNotFound_ThrowException]");
        when(scanTaskService.findById(scanTask.getId())).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/scan_service/v2/scan_task/{id}/issues/scan_files", scanTaskUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value("DATA_NOT_FOUND"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteScanTask_InputScanTaskId_ReturnHttpSuccessAndNothing() throws Exception {
        log.info("[deleteScanTask_InputScanTaskId_ReturnHttpSuccessAndNothing]");
        when(scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        doNothing().when(orchestrationService).deleteAllInScanTask(eq(scanTask), eq(true), any(User.class));
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/scan_service/v2/scan_task/{id}/issues/scan_files", scanTaskUUID).param("deleteScanTaskRecord", "Y")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getLatestCompletedScanTaskByProjectId_ProjectNotFound_ThrowException() throws Exception {
        log.info("[getLatestCompletedScanTaskByProjectId_ProjectNotFound_ThrowException]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_task/ids", projectUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value("DATA_NOT_FOUND"));
    }

    @Test
    @WithMockCustomUser()
    void getLatestCompletedScanTaskByProjectId_InsufficientPrivilege_ThrowException() throws Exception {
        log.info("[getLatestCompletedScanTaskByProjectId_InsufficientPrivilege_ThrowException]");
        when(projectService.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, project.getId())))
                .when(userService).checkAccessRightOrElseThrow(eq(project), any(), anyBoolean(), any());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_task/ids", projectUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getLatestCompletedScanTaskByProjectId_ReturnPageOfScanTaskDto() throws Exception {
        log.info("[getLatestCompletedScanTaskByProjectId_ReturnPageOfScanTaskDto]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(Project.builder().id(projectUUID).build()));
        Pageable pageable0 = PageRequest.of(0, 1);
        when(scanTaskService.getLatestCompletedScanTaskByProject(any(Project.class), any(Pageable.class))).thenReturn(new RestResponsePage<>(Collections.singletonList(scanTask), pageable0, 1));
        mockMvc.perform(get("/api/scan_service/v2/project/{id}/scan_task/ids", projectUUID)
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(scanTask.getId().toString()))
                .andExpect(jsonPath("$.content[0].projectUuid").value(scanTask.getProject().getId().toString()))
                .andExpect(jsonPath("$.content[0].projectId").value(scanTask.getProject().getProjectId()))
                .andExpect(jsonPath("$.content[0].status").value(scanTask.getStatus().name()))
                .andExpect(jsonPath("$.content[0].sourceRoot").value(scanTask.getSourceRoot()))
                .andExpect(jsonPath("$.content[0].createdBy").value(scanTask.getCreatedBy()))
                .andExpect(jsonPath("$.content[0].modifiedBy").value(scanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getLatestCompletedScanTaskByProjectId_withSort_ReturnPageOfScanTaskDto() throws Exception {
        log.info("[getLatestCompletedScanTaskByProjectId_withSort_ReturnPageOfScanTaskDto]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(Project.builder().id(projectUUID).build()));
        Pageable pageable0 = PageRequest.of(0, 1, Sort.by("scanTaskId"));
        when(scanTaskService.getLatestCompletedScanTaskByProject(any(Project.class), any(Pageable.class))).thenReturn(new RestResponsePage<>(Collections.singletonList(scanTask), pageable0, 1));
        mockMvc.perform(get("/api/scan_service/v2/project/{id}/scan_task/ids", projectUUID)
                .param("page", String.valueOf(pageable0.getPageNumber()))
                .param("size", String.valueOf(pageable0.getPageSize()))
                .param("sort", String.valueOf(pageable0.getSort())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(scanTask.getId().toString()))
                .andExpect(jsonPath("$.content[0].projectUuid").value(scanTask.getProject().getId().toString()))
                .andExpect(jsonPath("$.content[0].projectId").value(scanTask.getProject().getProjectId()))
                .andExpect(jsonPath("$.content[0].status").value(scanTask.getStatus().name()))
                .andExpect(jsonPath("$.content[0].sourceRoot").value(scanTask.getSourceRoot()))
                .andExpect(jsonPath("$.content[0].createdBy").value(scanTask.getCreatedBy()))
                .andExpect(jsonPath("$.content[0].modifiedBy").value(scanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser()
    void getScanSummary_InputScanTaskId_ReturnSummaryResponse() throws Exception {
        log.info("[getScanSummary_InputScanTaskId_ReturnSummaryResponse]");
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.of(scanTask));
        when(measureService.retrieveScanSummary(argThat(project -> StringUtils.equalsIgnoreCase(scanTaskUUID.toString(), scanTask.getId().toString())), any())).thenReturn(SummaryResponse.builder().scanTaskId(scanTask.getId()).build());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{id}/scan_summary", scanTaskUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scanTaskId").value(scanTask.getId().toString()));
    }

    @Test
    @WithMockCustomUser()
    void getScanSummary_ScanTaskNotExist_ScanTaskNotFound() throws Exception {
        log.info("[getScanSummary_ScanTaskNotExist_ScanTaskNotFound]");
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{id}/scan_summary", scanTaskUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getLatestScanSummaryByProject_ProjectNotFound_ThrowException() throws Exception {
        log.info("[getLatestScanSummaryByProject_ProjectNotFound_ThrowException]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_summary", projectUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value("DATA_NOT_FOUND"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getLatestScanSummaryByProject_CompletedScanTaskIsPresentLatestScanTaskIsEmpty_ThrowException() throws Exception {
        log.info("[getLatestScanSummaryByProject_CompletedScanTaskIsPresentLatestScanTaskIsEmpty_ThrowException]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(Project.builder().id(projectUUID).build()));
        when(scanTaskService.getLatestCompletedScanTaskByProject(argThat(p -> p.getId() == project.getId()))).thenReturn(Optional.of(scanTask));
        when(scanTaskService.getLatestScanTask(argThat(p -> p.getId() == project.getId()))).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_summary", projectUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_INTERNAL_ERROR));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getLatestScanSummaryByProject_ProjectHasCompletedScanTask_ReturnSummaryResponse() throws Exception {
        log.info("[getLatestScanSummaryByProject_ProjectHasCompletedScanTask_ReturnSummaryResponse]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(Project.builder().id(projectUUID).build()));
        when(scanTaskService.getLatestCompletedScanTaskByProject(argThat(p -> p.getId() == project.getId()))).thenReturn(Optional.of(scanTask));
        when(scanTaskService.getLatestScanTask(argThat(p -> p.getId() == project.getId()))).thenReturn(Optional.of(scanTask));
        when(measureService.retrieveScanSummary(argThat(sc -> sc.getId() == scanTask.getId()),
                argThat(sc -> sc.getId() == scanTask.getId()),any())).thenReturn(SummaryResponse.builder().scanTaskId(scanTask.getId()).build());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_summary", projectUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scanTaskId").value(scanTask.getId().toString()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getLatestScanSummaryByProject_ProjectHaveScanTaskWithoutCompletedScanTask_ReturnSummaryResponseHaveLatestScanTaskWithoutLatestCompletedScanTask() throws Exception {
        log.info("[getLatestScanSummaryByProject_ProjectHaveScanTaskWithoutCompletedScanTask_ReturnSummaryResponseWithLatestScanTask]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(Project.builder().id(projectUUID).build()));
        when(scanTaskService.getLatestCompletedScanTaskByProject(argThat(p -> p.getId() == project.getId()))).thenReturn(Optional.empty());
        when(scanTaskService.getLatestScanTask(argThat(p -> p.getId() == project.getId()))).thenReturn(Optional.of(scanTask));
        when(measureService.retrieveScanSummary(argThat(sc -> sc.getId() == scanTask.getId()),
                argThat(sc -> sc.getId() == scanTask.getId()),any())).thenReturn(SummaryResponse.builder().scanTaskId(scanTask.getId()).build());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_summary", projectUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestScanTask.scanTaskId").value(scanTask.getId().toString()))
                .andExpect(jsonPath("$.latestCompleteScanTask").value(IsNull.nullValue()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getLatestScanSummaryByProject_ProjectWithoutScanTaskHistory_ReturnSummaryResponseWithEmptyInfo() throws Exception {
        log.info("[getLatestScanSummaryByProject_ProjectWithoutScanTaskHistory_ReturnSummaryResponse]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(Project.builder().id(projectUUID).build()));
        when(scanTaskService.getLatestCompletedScanTaskByProject(Project.builder().id(projectUUID).build())).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_summary", projectUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scanTaskId").value(IsNull.nullValue()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void compareScanResult_FromScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[compareScanResult_FromScanTaskNotFound_ThrowAppException]");
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{id}/compare/{id}", scanTaskUUID, UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void compareScanResult_ToScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[compareScanResult_ToScanTaskNotFound_ThrowAppException]");
        UUID toScanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.of(scanTask));
        when(scanTaskService.findById(toScanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{id}/compare/{id}", scanTaskUUID, toScanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void compareScanResult_differentProject_ThrowAppException() throws Exception {
        log.info("[compareScanResult_differentProject_ThrowAppException]");
        UUID toScanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.of(scanTask));
        when(scanTaskService.findById(toScanTaskId)).thenReturn(Optional.of(ScanTask.builder().project(Project.builder().id(UUID.randomUUID()).build()).build()));
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{id}/compare/{id}", scanTaskUUID, toScanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser()
    void compareScanResult_InsufficientPrivilegeOnFromScanTask_ThrowAppException() throws Exception {
        log.info("[compareScanResult_InsufficientPrivilegeOnFromScanTask_ThrowAppException]");
        UUID toScanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.of(scanTask));
        when(scanTaskService.findById(toScanTaskId)).thenReturn(Optional.of(ScanTask.builder().createdBy("user").project(scanTask.getProject()).build()));
        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, scanTask.getId())))
                .when(userService).checkAccessRightOrElseThrow(eq(scanTask), any(), eq(true), any());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{id}/compare/{id}", scanTaskUUID, toScanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser()
    void compareScanResult_InsufficientPrivilegeOnToScanTask_ThrowAppException() throws Exception {
        log.info("[compareScanResult_InsufficientPrivilegeOnToScanTask_ThrowAppException]");
        UUID toScanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.of(ScanTask.builder().id(scanTaskUUID).createdBy("user").project(Project.builder().id(scanTask.getProject().getId()).createdBy("user").build()).build()));
        when(scanTaskService.findById(toScanTaskId)).thenReturn(Optional.of(scanTask));
        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, toScanTaskId)))
                .when(userService).checkAccessRightOrElseThrow(eq(scanTask), any(), eq(true), any());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{id}/compare/{id}", scanTaskUUID, toScanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void compareScanResult_SameProject_ReturnCompareScanResultResponse() throws Exception {
        log.info("[compareScanResult_SameProject_ReturnCompareScanResultResponse]");
        UUID toScanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.of(scanTask));
        when(scanTaskService.findById(toScanTaskId)).thenReturn(Optional.of(ScanTask.builder().project(Project.builder().id(projectUUID).projectId(projectId).build()).build()));
        UUID newIssueId1 = UUID.randomUUID();
        UUID newIssueId2 = UUID.randomUUID();
        UUID fixIssueId1 = UUID.randomUUID();
        UUID fixIssueId2 = UUID.randomUUID();
        when(issueService.compareScanResult(scanTaskUUID, toScanTaskId)).thenReturn(CompareScanResultResponse.builder().newIssueIds(Arrays.asList(newIssueId1, newIssueId2)).fixedIssueIds(Arrays.asList(fixIssueId1, fixIssueId2)).build());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{id}/compare/{id}", scanTaskUUID, toScanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newIssueIds[0]").value(newIssueId1.toString()))
                .andExpect(jsonPath("$.newIssueIds[1]").value(newIssueId2.toString()))
                .andExpect(jsonPath("$.fixedIssueIds[0]").value(fixIssueId1.toString()))
                .andExpect(jsonPath("$.fixedIssueIds[1]").value(fixIssueId2.toString()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void compareScanResult_FromTaskNoPrivilege_ThrowException() throws Exception {
        log.info("[compareScanResult_SameProject_ReturnCompareScanResultResponse]");
        UUID toScanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.of(scanTask));
        when(scanTaskService.findById(toScanTaskId)).thenReturn(Optional.of(ScanTask.builder().project(Project.builder().id(projectUUID).projectId(projectId).build()).build()));
        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, scanTaskUUID)))
                .when(this.userService).checkAccessRightOrElseThrow(argThat((ScanTask st) -> scanTaskUUID.equals(st.getId())), any(User.class), anyBoolean(), any());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{id}/compare/{id}", scanTaskUUID, toScanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void compareScanResult_ToTaskNoPrivilege_ThrowException() throws Exception {
        log.info("[compareScanResult_SameProject_ReturnCompareScanResultResponse]");
        UUID toScanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.of(scanTask));
        when(scanTaskService.findById(toScanTaskId)).thenReturn(Optional.of(ScanTask.builder().id(toScanTaskId).project(Project.builder().id(projectUUID).projectId(projectId).build()).build()));
        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, scanTaskUUID)))
                .when(this.userService).checkAccessRightOrElseThrow(argThat((ScanTask st) -> toScanTaskId.equals(st.getId())), any(User.class), anyBoolean(), any());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/scan_task/{id}/compare/{id}", scanTaskUUID, toScanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getScanTasksByStatus_ProjectNotFound_ThrowAppException() throws Exception {
        log.info("[getScanTasksByStatus_ProjectNotFound_ThrowAppException]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_tasks", projectUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void getScanTasksByStatus_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[getScanTasksByStatus_InsufficientPrivilege_ThrowAppException]");
        when(projectService.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, project.getId())))
                .when(userService).checkAccessRightOrElseThrow(eq(project), any(), anyBoolean(), any());
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/scan_service/v2/project/{id}/scan_tasks", project.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getScanTasksByStatus_InputInvalidStatus_ThrowAppException() throws Exception {
        log.info("[getScanTasksByStatus_InputInvalidStatus_ThrowAppException]");
        Project project = Project.builder().id(projectUUID).build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        mockMvc.perform(get("/api/scan_service/v2/project/{id}/scan_tasks", projectUUID)
                .param("statusList", "PENDING, PROCESSING,xxxx"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getScanTasksByStatus_NonInputStatus_ReturnPageOfScanTaskDto() throws Exception {
        log.info("[getScanTasksByStatus_NonInputStatus_ReturnPageOfScanTaskDto]");
        Project project = Project.builder().id(projectUUID).build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        Pageable pageable0 = PageRequest.of(0, 1);
        when(scanTaskService.getScanTaskByProjectAndStatus(eq(project), eq(Arrays.asList(ScanTask.Status.values())), any(Pageable.class))).thenReturn(new RestResponsePage<>(Collections.singletonList(scanTask), pageable0, 1));
        mockMvc.perform(get("/api/scan_service/v2/project/{id}/scan_tasks", projectUUID)
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(scanTask.getId().toString()))
                .andExpect(jsonPath("$.content[0].projectUuid").value(scanTask.getProject().getId().toString()))
                .andExpect(jsonPath("$.content[0].projectId").value(scanTask.getProject().getProjectId()))
                .andExpect(jsonPath("$.content[0].status").value(scanTask.getStatus().name()))
                .andExpect(jsonPath("$.content[0].sourceRoot").value(scanTask.getSourceRoot()))
                .andExpect(jsonPath("$.content[0].createdBy").value(scanTask.getCreatedBy()))
                .andExpect(jsonPath("$.content[0].modifiedBy").value(scanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getScanTasksByStatus_InputStatus_ReturnPageOfScanTaskDto() throws Exception {
        log.info("[getScanTasksByStatus_InputStatus_ReturnPageOfScanTaskDto]");
        Project project = Project.builder().id(projectUUID).build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        Pageable pageable0 = PageRequest.of(0, 1);
        when(scanTaskService.getScanTaskByProjectAndStatus(eq(project), eq(Arrays.asList(ScanTask.Status.PENDING, ScanTask.Status.PROCESSING)), any(Pageable.class))).thenReturn(new RestResponsePage<>(Collections.singletonList(scanTask), pageable0, 1));
        mockMvc.perform(get("/api/scan_service/v2/project/{id}/scan_tasks", projectUUID)
                .param("statusList", "PENDING, PROCESSING")
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(scanTask.getId().toString()))
                .andExpect(jsonPath("$.content[0].projectUuid").value(scanTask.getProject().getId().toString()))
                .andExpect(jsonPath("$.content[0].projectId").value(scanTask.getProject().getProjectId()))
                .andExpect(jsonPath("$.content[0].status").value(scanTask.getStatus().name()))
                .andExpect(jsonPath("$.content[0].sourceRoot").value(scanTask.getSourceRoot()))
                .andExpect(jsonPath("$.content[0].createdBy").value(scanTask.getCreatedBy()))
                .andExpect(jsonPath("$.content[0].modifiedBy").value(scanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void uploadDiagnosticFile_Success() throws Exception {
        log.info("[uploadDiagnosticFile_Upload_Success]");
        String fileName = "xcalagent_temp";
        Path tempFilePath = Files.createTempFile(fileName, ".log");
        String content = "[  0%] Building C object Source/kwsys/CMakeFiles/cmsys_c.dir/ProcessUNIX.c.o \n" +
                "[  1%] Building C object Source/kwsys/CMakeFiles/cmsys_c.dir/Base64.c.o";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.of(ScanTask.builder().id(scanTaskUUID).status(ScanTask.Status.PROCESSING).createdBy("user").project(Project.builder().id(scanTask.getProject().getId()).createdBy("user").build()).build()));
        when(performanceService.saveLogFile(any(), any())).thenReturn(file.getName());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("upload_file", file.getAbsolutePath(), MediaType.APPLICATION_OCTET_STREAM.toString(), new FileInputStream(file));
        mockMvc.perform(multipart("/api/scan_service/v2/scan_task/{id}/diagnostic_info", scanTaskUUID)
                .file(mockMultipartFile)
                .param("file_checksum", "1234")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(new StringStartsWith(fileName)));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void uploadDiagnosticFile_ScanTaskNotExist_NotFoundException() throws Exception {
        log.info("[uploadDiagnosticFile_ScanTaskNotExist_NotFoundException]");
        String fileName = "xcalagent_temp";
        Path tempFilePath = Files.createTempFile(fileName, ".log");
        String content = "[  0%] Building C object Source/kwsys/CMakeFiles/cmsys_c.dir/ProcessUNIX.c.o \n" +
                "[  1%] Building C object Source/kwsys/CMakeFiles/cmsys_c.dir/Base64.c.o";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.empty());
        when(performanceService.saveLogFile(any(), any())).thenReturn(file.getName());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("upload_file", file.getAbsolutePath(), MediaType.APPLICATION_OCTET_STREAM.toString(), new FileInputStream(file));
        mockMvc.perform(multipart("/api/scan_service/v2/scan_task/{id}/diagnostic_info", scanTaskUUID)
                .file(mockMultipartFile)
                .param("file_checksum", "1234")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void downloadDiagnosticInfo_Success() throws Exception {
        log.info("[downloadDiagnosticInfo_Success]");
        String fileName = "xcalagent_temp";
        Path tempFilePath = Files.createTempFile(fileName, ".log");
        String content = "abc123";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(file.getName(), file.getAbsolutePath(), MediaType.APPLICATION_OCTET_STREAM.toString(), new FileInputStream(file));
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.of(ScanTask.builder().id(scanTaskUUID).status(ScanTask.Status.COMPLETED).createdBy("user").project(Project.builder().id(scanTask.getProject().getId()).createdBy("user").build()).build()));
        when(performanceService.getDownloadFilePath(any())).thenReturn(mockMultipartFile.getResource());
        MvcResult result = mockMvc.perform(get("/api/scan_service/v2/scan_task/{id}/diagnostic_info", scanTaskUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM.toString(), result.getResponse().getContentType());
        assertEquals(content, result.getResponse().getContentAsString());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void downloadDiagnosticInfo_ScanTaskNotExist_NotFoundException() throws Exception {
        log.info("[downloadDiagnosticInfo_ScanTaskNotExist_NotFoundException]");
        when(scanTaskService.findById(scanTaskUUID)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/scan_service/v2/scan_task/{id}/diagnostic_info", scanTaskUUID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_Success() throws Exception {
        List<AddScanTaskRequest.Attribute> attributes = new ArrayList<>();
        AddScanTaskRequest addScanTaskRequest = AddScanTaskRequest.builder()
                .projectId(projectUUID)
                .attributes(attributes)
                .startNow(true)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(scanTaskService.getLatestRunningScanTask(project)).thenReturn(Optional.empty());
        when(scanTaskService.addScanTask(eq(project), eq(attributes), eq(true), anyString())).thenReturn(scanTask);
        mockMvc.perform(post("/api/scan_service/v2/scan_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(addScanTaskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(scanTask.getId().toString()))
                .andExpect(jsonPath("$.projectUuid").value(scanTask.getProject().getId().toString()))
                .andExpect(jsonPath("$.projectId").value(scanTask.getProject().getProjectId()))
                .andExpect(jsonPath("$.status").value(scanTask.getStatus().name()))
                .andExpect(jsonPath("$.sourceRoot").value(scanTask.getSourceRoot()))
                .andExpect(jsonPath("$.createdBy").value(scanTask.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(scanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_ScanTaskExist() throws Exception {
        List<AddScanTaskRequest.Attribute> attributes = new ArrayList<>();
        AddScanTaskRequest addScanTaskRequest = AddScanTaskRequest.builder()
                .projectId(projectUUID)
                .attributes(attributes)
                .startNow(true)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(scanTaskService.getLatestRunningScanTask(project)).thenReturn(Optional.of(scanTask));
        mockMvc.perform(post("/api/scan_service/v2/scan_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(addScanTaskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(scanTask.getId().toString()))
                .andExpect(jsonPath("$.projectUuid").value(scanTask.getProject().getId().toString()))
                .andExpect(jsonPath("$.projectId").value(scanTask.getProject().getProjectId()))
                .andExpect(jsonPath("$.status").value(scanTask.getStatus().name()))
                .andExpect(jsonPath("$.sourceRoot").value(scanTask.getSourceRoot()))
                .andExpect(jsonPath("$.createdBy").value(scanTask.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(scanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_ProjectNotExist() throws Exception {
        List<AddScanTaskRequest.Attribute> attributes = new ArrayList<>();
        AddScanTaskRequest addScanTaskRequest = AddScanTaskRequest.builder()
                .projectId(projectUUID)
                .attributes(attributes)
                .startNow(true)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/scan_service/v2/scan_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(addScanTaskRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND))
                .andExpect(jsonPath("$.unifyErrorCode").value(AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_ProjectUUIDNotExist() throws Exception {
        List<AddScanTaskRequest.Attribute> attributes = new ArrayList<>();
        AddScanTaskRequest addScanTaskRequest = AddScanTaskRequest.builder()
                .attributes(attributes)
                .startNow(true)
                .build();
        mockMvc.perform(post("/api/scan_service/v2/scan_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(addScanTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INCORRECT_PARAM))
                .andExpect(jsonPath("$.unifyErrorCode").value(AppException.ErrorCode.E_API_SCANTASK_ADDSCAN_EMPTY_PROJECT_UUID.unifyErrorCode));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addScanTask_NoProjectPermission() throws Exception {
        List<AddScanTaskRequest.Attribute> attributes = new ArrayList<>();
        AddScanTaskRequest addScanTaskRequest = AddScanTaskRequest.builder()
                .projectId(projectUUID)
                .attributes(attributes)
                .startNow(true)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_SCANTASK_ADDSCAN_EMPTY_PROJECT_UUID.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_SCANTASK_ADDSCAN_EMPTY_PROJECT_UUID.messageTemplate)))
                .when(userService).checkAccessRightOrElseThrow(eq(project), any(), anyBoolean(), any());
        mockMvc.perform(post("/api/scan_service/v2/scan_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(addScanTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INCORRECT_PARAM))
                .andExpect(jsonPath("$.unifyErrorCode").value(AppException.ErrorCode.E_API_SCANTASK_ADDSCAN_EMPTY_PROJECT_UUID.unifyErrorCode));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void searchScanTask_Success() throws Exception {
        List<SearchScanTaskRequest.Attribute> existAttributes = Arrays.asList(
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("ex_key1").build(),
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("ex_key2").build(),
                SearchScanTaskRequest.Attribute.builder().type("SCAN").name("ex_key3").build()
        );
        List<SearchScanTaskRequest.Attribute> equalAttributes = Arrays.asList(
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("eq_key1").value("eq_value1").build(),
                SearchScanTaskRequest.Attribute.builder().type("SCAN").name("eq_key2").value("eq_value2").build()
        );
        List<String> status = Arrays.asList("COMPLETED", "FAILED");
        SearchScanTaskRequest searchScanTaskRequest = SearchScanTaskRequest.builder()
                .projectId(projectUUID)
                .status(status)
                .existAttributes(existAttributes)
                .equalAttributes(equalAttributes)
                .build();
        Pageable pageable = PageRequest.of(0, 1);
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(scanTaskService.getLatestRunningScanTask(project)).thenReturn(Optional.of(scanTask));
        when(scanTaskService.searchScanTask(argThat((Project p) -> project.getId().equals(p.getId())), anyList(), anyList(), anyList(), any(Pageable.class))).thenReturn(new RestResponsePage<>(Collections.singletonList(scanTask), pageable, 1));
        mockMvc.perform(post("/api/scan_service/v2/scan_task/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchScanTaskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(scanTask.getId().toString()))
                .andExpect(jsonPath("$.content[0].projectUuid").value(scanTask.getProject().getId().toString()))
                .andExpect(jsonPath("$.content[0].projectId").value(scanTask.getProject().getProjectId()))
                .andExpect(jsonPath("$.content[0].status").value(scanTask.getStatus().name()))
                .andExpect(jsonPath("$.content[0].sourceRoot").value(scanTask.getSourceRoot()))
                .andExpect(jsonPath("$.content[0].createdBy").value(scanTask.getCreatedBy()))
                .andExpect(jsonPath("$.content[0].modifiedBy").value(scanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void searchScanTask_SuccessWithSort() throws Exception {
        List<SearchScanTaskRequest.Attribute> existAttributes = Arrays.asList(
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("ex_key1").build(),
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("ex_key2").build(),
                SearchScanTaskRequest.Attribute.builder().type("SCAN").name("ex_key3").build()
        );
        List<SearchScanTaskRequest.Attribute> equalAttributes = Arrays.asList(
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("eq_key1").value("eq_value1").build(),
                SearchScanTaskRequest.Attribute.builder().type("SCAN").name("eq_key2").value("eq_value2").build()
        );
        List<String> status = Arrays.asList("COMPLETED", "FAILED");
        SearchScanTaskRequest searchScanTaskRequest = SearchScanTaskRequest.builder()
                .projectId(projectUUID)
                .status(status)
                .existAttributes(existAttributes)
                .equalAttributes(equalAttributes)
                .build();
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createdOn")));
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(scanTaskService.getLatestRunningScanTask(project)).thenReturn(Optional.of(scanTask));
        when(scanTaskService.searchScanTask(argThat((Project p) -> project.getId().equals(p.getId())), anyList(), anyList(), anyList(), any(Pageable.class))).thenReturn(new RestResponsePage<>(Collections.singletonList(scanTask), pageable, 1));
        mockMvc.perform(post("/api/scan_service/v2/scan_task/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchScanTaskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(scanTask.getId().toString()))
                .andExpect(jsonPath("$.content[0].projectUuid").value(scanTask.getProject().getId().toString()))
                .andExpect(jsonPath("$.content[0].projectId").value(scanTask.getProject().getProjectId()))
                .andExpect(jsonPath("$.content[0].status").value(scanTask.getStatus().name()))
                .andExpect(jsonPath("$.content[0].sourceRoot").value(scanTask.getSourceRoot()))
                .andExpect(jsonPath("$.content[0].createdBy").value(scanTask.getCreatedBy()))
                .andExpect(jsonPath("$.content[0].modifiedBy").value(scanTask.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void searchScanTask_ProjectNotExist() throws Exception {
        List<SearchScanTaskRequest.Attribute> existAttributes = Arrays.asList(
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("ex_key1").build(),
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("ex_key2").build(),
                SearchScanTaskRequest.Attribute.builder().type("SCAN").name("ex_key3").build()
        );
        List<SearchScanTaskRequest.Attribute> equalAttributes = Arrays.asList(
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("eq_key1").value("eq_value1").build(),
                SearchScanTaskRequest.Attribute.builder().type("SCAN").name("eq_key2").value("eq_value2").build()
        );
        List<String> status = Arrays.asList("COMPLETED", "FAILED");
        SearchScanTaskRequest searchScanTaskRequest = SearchScanTaskRequest.builder()
                .projectId(projectUUID)
                .status(status)
                .existAttributes(existAttributes)
                .equalAttributes(equalAttributes)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/scan_service/v2/scan_task/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchScanTaskRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND))
                .andExpect(jsonPath("$.unifyErrorCode").value(AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void searchScanTask_NoProjectPermission() throws Exception {
        List<SearchScanTaskRequest.Attribute> existAttributes = Arrays.asList(
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("ex_key1").build(),
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("ex_key2").build(),
                SearchScanTaskRequest.Attribute.builder().type("SCAN").name("ex_key3").build()
        );
        List<SearchScanTaskRequest.Attribute> equalAttributes = Arrays.asList(
                SearchScanTaskRequest.Attribute.builder().type("PROJECT").name("eq_key1").value("eq_value1").build(),
                SearchScanTaskRequest.Attribute.builder().type("SCAN").name("eq_key2").value("eq_value2").build()
        );
        List<String> status = Arrays.asList("COMPLETED", "FAILED");
        SearchScanTaskRequest searchScanTaskRequest = SearchScanTaskRequest.builder()
                .projectId(projectUUID)
                .status(status)
                .existAttributes(existAttributes)
                .equalAttributes(equalAttributes)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        when(scanTaskService.getLatestRunningScanTask(project)).thenReturn(Optional.of(scanTask));
        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, searchScanTaskRequest.getProjectId())))
                .when(userService).checkAccessRightOrElseThrow(argThat((Project p) -> project.getId().equals(p.getId())), any(), anyBoolean(), any());
        mockMvc.perform(post("/api/scan_service/v2/scan_task/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchScanTaskRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.unifyErrorCode").value(AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode));
    }
}
