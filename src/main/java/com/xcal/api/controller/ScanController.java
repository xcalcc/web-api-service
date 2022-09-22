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
import com.google.common.collect.ImmutableMap;
import com.xcal.api.entity.*;
import com.xcal.api.entity.v3.IssueGroup;
import com.xcal.api.entity.v3.ScanTaskLog;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.ScanTaskDto;
import com.xcal.api.model.payload.*;
import com.xcal.api.model.payload.v3.ScanTaskIdResponse;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.*;
import com.xcal.api.util.*;
import io.opentracing.Tracer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/scan_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Scan Service")
public class ScanController {

    @NonNull ScanFileService scanFileService;
    @NonNull ScanTaskService scanTaskService;
    @NonNull IssueService issueService;
    @NonNull ScanStatusService scanStatusService;
    @NonNull MeasureService measureService;
    @NonNull ProjectService projectService;
    @NonNull UserService userService;
    @NonNull OrchestrationService orchestrationService;
    @NonNull AsyncJobService asyncJobService;
    @NonNull KafkaTemplate<String, String> kafkaTemplate;
    @NonNull PerformanceService performanceService;
    @NonNull Tracer tracer;
    @NonNull ObjectMapper om;
    @NonNull I18nService i18nService;

    /**
     * @param id uuid of the project
     * @return scanTask
     */
    @PostMapping("/project/{id}/scan_task")
    @Deprecated
    @ApiOperation(value = "Add new scan",
            nickname = "addScanTask",
            notes = "Add scan task, i.e. Start a new scan",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanTaskDto> addScanTask(@PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[addScanTask] id: {}, username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(this.tracer, TracerUtil.Tag.PROJECT_ID, id);

        Project project = this.projectService.findById(id)
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));

        ScanTask scanTask;
        Optional<ScanTask> optionalScanTask = this.scanTaskService.getLatestRunningScanTask(project);
        if (optionalScanTask.isPresent()) {
            scanTask = optionalScanTask.get();
        } else {
            // Start now is true, start the scan immediately
            scanTask = this.scanTaskService.addScanTask(project, new ArrayList<>(), true, userPrincipal.getUsername());
            scanTaskService.prepareAndCallScan(scanTask, scanTask.getProjectConfig(), userPrincipal.getUsername());
        }

        TracerUtil.setTags(this.tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId())
                .put(TracerUtil.Tag.PROJECT_ID, scanTask.getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());

        ScanTaskDto result = ScanTaskService.convertScanTaskToDto(scanTask);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * @param id     uuid of the project
     * @param status start/pending
     * @return scanTask
     */
    @PostMapping("/project/{id}/scan_task/{status}")
    @Deprecated
    @ApiOperation(value = "Add new scan task",
            nickname = "addScanTask",
            notes = "Add scan task to database, start this new scan right now or not",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanTaskDto> addScanTaskWithStatus(@PathVariable UUID id, @PathVariable String status, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[addScanTaskWithStatus] id: {}, status: {}, username: {}", id, status, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);

        Project project = this.projectService.findById(id)
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));

        if (!EnumUtils.isValidEnumIgnoreCase(ScanTask.Status.class, status)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASKSTATUS_COMMON_INVALID_STATUS.unifyErrorCode, AppException.ErrorCode.E_API_SCANTASKSTATUS_COMMON_INVALID_STATUS.messageTemplate);
        }

        ScanTask scanTask;
        Optional<ScanTask> optionalScanTask = scanTaskService.getLatestRunningScanTask(project);
        if (optionalScanTask.isPresent()) {
            scanTask = optionalScanTask.get();
        } else {
            boolean startNow = StringUtils.equalsIgnoreCase(status, ScanTask.Status.START.toString());
            scanTask = this.scanTaskService.addScanTask(project, new ArrayList<>(), startNow, userPrincipal.getUsername());
            if (startNow) {
                scanTaskService.prepareAndCallScan(scanTask, scanTask.getProjectConfig(), userPrincipal.getUsername());
            }
        }

        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId())
                .put(TracerUtil.Tag.PROJECT_ID, scanTask.getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());

        ScanTaskDto result = ScanTaskService.convertScanTaskToDto(scanTask);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * @return scan task
     */
    @PostMapping("/scan_task")
    @ApiOperation(value = "Add new scan",
            nickname = "addScanTaskWithBody",
            notes = "Add scan task, i.e. Start a new scan",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanTaskDto> addScanTask(@RequestBody AddScanTaskRequest addScanTaskRequest, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[addScanTask] addScanTaskRequest: {}, username: {}", addScanTaskRequest, userPrincipal.getUsername());
        UUID projectId = Optional.of(addScanTaskRequest).map(AddScanTaskRequest::getProjectId).orElseThrow(() ->
                new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_SCANTASK_ADDSCAN_EMPTY_PROJECT_UUID.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_SCANTASK_ADDSCAN_EMPTY_PROJECT_UUID.messageTemplate)));
        Project project = this.projectService.findById(projectId)
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, projectId)));
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, project.getId());
        this.userService.checkAccessRightOrElseThrow(project, userPrincipal.getUser(), false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, projectId)));

        ScanTask scanTask;
        Optional<ScanTask> optionalScanTask = scanTaskService.getLatestRunningScanTask(project);
        if (optionalScanTask.isPresent()) {
            scanTask = optionalScanTask.get();
        } else {
            scanTask = this.scanTaskService.addScanTask(project, addScanTaskRequest.getAttributes(), addScanTaskRequest.getStartNow(), userPrincipal.getUsername());
            if (addScanTaskRequest.getStartNow()) {
                scanTaskService.prepareAndCallScan(scanTask, scanTask.getProjectConfig(), userPrincipal.getUsername());
            }
        }

        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId())
                .put(TracerUtil.Tag.PROJECT_ID, scanTask.getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());

        ScanTaskDto result = ScanTaskService.convertScanTaskToDto(scanTask);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * @param id uuid of scanTask
     * @return scan task
     */
    @GetMapping("/scan_task/{id}")
    @ApiOperation(value = "Get scan task by id",
            nickname = "getScanTask",
            notes = "Retrieve the scan task with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanTaskDto> getScanTask(
            @ApiParam(value = "uuid of the task", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getScanTask] id: {}, username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_TASK_ID, id);
        Optional<ScanTask> optionalScanTask = scanTaskService.findById(id);
        ScanTask scanTask = optionalScanTask.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}]id: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));
        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.PROJECT_ID, scanTask.getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());
        ScanTaskDto result = ScanTaskService.convertScanTaskToDto(scanTask);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * @param id scan task id
     * @return ScanTask
     */
    @DeleteMapping("/scan_task/{id}")
    @ApiOperation(value = "Stop a started scan",
            nickname = "stopScanTask",
            notes = "Stop the scan with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanTaskDto> stopScan(
            @ApiParam(value = "uuid of the scanTask", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[stopScan] id: {}, username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_TASK_ID, id);
        ScanTask scanTask = scanTaskService.stopScan(id, userPrincipal.getUsername());
        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.PROJECT_ID, scanTask.getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());
        ScanTaskDto result = ScanTaskService.convertScanTaskToDto(scanTask);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    @DeleteMapping("/scan_task/{id}/issues/scan_files")
    @Deprecated
    @ApiOperation(value = "Delete scan task",
            nickname = "deleteScanTask",
            notes = "Delete the scan with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteScanTask(
            @ApiParam(value = "uuid of the scanTask", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @RequestParam(defaultValue = "Y") String deleteScanTaskRecord, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[deleteScanTask] id: {}, deleteScanTaskRecord: {}, username: {}", id, deleteScanTaskRecord, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_TASK_ID, id);
        Optional<ScanTask> scanTaskOptional = scanTaskService.findById(id);
        ScanTask scanTask = scanTaskOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));
        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.PROJECT_ID, scanTask.getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());
        this.orchestrationService.deleteAllInScanTask(scanTask, StringUtils.equalsIgnoreCase(deleteScanTaskRecord, "Y"), userPrincipal.getUser());
        return ResponseEntity.noContent().build();
    }

    /**
     * @param updateScanTaskRequest updateScanTaskRequest
     * @return ScanTask
     */
    @PutMapping("/scan_task/{id}")
    @ApiOperation(value = "Update scan task by id",
            nickname = "updateScanTask",
            notes = "Update the scan with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanTaskDto> updateScanTaskStatus(
            @ApiParam(value = "uuid of the scanTask", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID id,
            @Valid @ValidErrorCode(AppException.ErrorCode.E_API_SCANTASKSTATUS_UPDATE_VALIDATE_FAIL) @RequestBody UpdateScanTaskRequest updateScanTaskRequest, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateScanTaskStatus] id: {}, updateScanTaskRequest: {}, username: {}", id, updateScanTaskRequest, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_TASK_ID, id);

        if (!id.equals(updateScanTaskRequest.getId())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_SCANTASK_UPDATE_INCONSISTENT.unifyErrorCode,
                    CommonUtil.formatString("[{}] id: {}, updateScanTaskRequest id: {}", AppException.ErrorCode.E_API_SCANTASK_UPDATE_INCONSISTENT.messageTemplate, id, updateScanTaskRequest.getId()));
        }
        ScanTask scanTask = scanTaskService.updateScanTaskStatus(updateScanTaskRequest, userPrincipal.getUsername());

        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.PROJECT_ID, scanTask.getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());

        ScanTaskDto result = ScanTaskService.convertScanTaskToDto(scanTask);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    @PutMapping("/scan_task/{id}/async")
    @ApiOperation(
            value = "Async update scan task by id",
            nickname = "asyncUpdateScanTaskStatus",
            notes = "Async update scan task by id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> asyncUpdateScanTaskStatus(
            @ApiParam(value = "uuid of the scanTask", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID id,
            @Valid @ValidErrorCode(AppException.ErrorCode.E_API_SCANTASKSTATUS_UPDATE_VALIDATE_FAIL) @RequestBody UpdateScanTaskRequest updateScanTaskRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[asyncUpdateScanTaskStatus] id: {}, updateScanTaskRequest: {}, username: {}", id, updateScanTaskRequest, userPrincipal.getUsername());
        if (!id.equals(updateScanTaskRequest.getId())) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_INCORRECT_PARAM,
                    HttpURLConnection.HTTP_CONFLICT,
                    AppException.ErrorCode.E_API_SCANTASK_UPDATE_INCONSISTENT.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] id: {}, updateScanTaskRequest id: {}",
                            AppException.ErrorCode.E_API_SCANTASK_UPDATE_INCONSISTENT.messageTemplate,
                            id,
                            updateScanTaskRequest.getId()
                    )
            );
        }

        Optional<ScanTask> scanTaskOptional = this.scanTaskService.findById(id);
        ScanTask scanTask = scanTaskOptional
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                id
                        )
                ));

        AsyncJob asyncJob;

        try {
            String username = userPrincipal.getUsername();
            Date now = new Date();

            AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                    .username(username)
                    .scanTask(scanTask.getId().toString())
                    .baselineScanTask("")
                    .issueFile("")
                    .fixedIssueFile("")
                    .newIssueFile("")
                    .updateScanTaskRequest(updateScanTaskRequest)
                    .step(2)
                    .build();

            asyncJob = this.asyncJobService.addAsyncJob(AsyncJob.builder()
                    .name(String.format("scan-task-%s", scanTask.getId().toString()))
                    .info(this.om.writeValueAsString(info))
                    .status(AsyncJob.Status.CREATED)
                    .createdBy(username)
                    .createdOn(now)
                    .modifiedBy(username)
                    .modifiedOn(now)
                    .build());

            this.kafkaTemplate.send("scan-task-topic", asyncJob.getId().toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_INTERNAL_ERROR,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.name(),
                    e.getLocalizedMessage(),
                    e
            );
        }

        Map<String, Object> result = new HashMap<>();
        result.put("scan_task", ScanTaskService.convertScanTaskToDto(scanTask));
        result.put("import_status", asyncJob.getStatus().toString());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
    }

    /**
     * @param id uuid of project
     * @return ScanTask
     */
    @PutMapping("/project/{id}/scan_task")
    @ApiOperation(value = "Update Scan status by project id with latest started scan task",
            nickname = "updateScanStatusByProjectId",
            notes = "Update scan status by project id with latest started scan task",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanTaskDto> updateScanStatusByProjectId(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @RequestBody UpdateScanTaskRequest updateScanTaskRequest, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateScanStatusByProjectId] id: {}, updateScanTaskRequest: {}, username: {}", id, updateScanTaskRequest, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);
        Optional<Project> projectOptional = this.projectService.findById(id);
        Project project = projectOptional.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));

        ScanTask scanTask = scanTaskService.updateScanTaskStatus(project, updateScanTaskRequest, userPrincipal.getUsername());

        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());

        ScanTaskDto result = ScanTaskService.convertScanTaskToDto(scanTask);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * @param id project uuid
     * @return ScanStatusResponse
     */
    @GetMapping("/project/{id}/scan_task")
    @ApiOperation(value = "Get the latest scan status by project uuid",
            nickname = "getLatestScanStatusByProjectId",
            notes = "Get the latest scan status by project uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanStatusResponse> getLatestScanStatusByProjectId(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getLatestScanStatusByProjectId] id: {}, username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);

        Project project = this.projectService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        this.userService.checkAccessRightOrElseThrow(project, userPrincipal.getUser(), true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));
        ScanTaskStatusLog scanTaskStatusLog = scanStatusService.getLatestScanStatusByProject(project);

        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.SCAN_TASK_ID, scanTaskStatusLog.getScanTask().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTaskStatusLog.getScanTask().getStatus())
                .build());

        ScanStatusResponse scanStatusResponse = this.scanStatusService.convertScanTaskStatusLogToResponse(scanTaskStatusLog, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(scanStatusResponse);
    }

    /**
     * @param id project uuid
     * @return ScanStatusResponse
     */
    @GetMapping("/scan_task/{id}/status")
    @ApiOperation(value = "Get the status by scan task id",
            nickname = "getScanStatusByScanTaskId",
            notes = "Get the status by scan task id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanStatusResponse> getScanStatusByScanTaskId(
            @ApiParam(value = "uuid of the scan task", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getScanStatusByScanTaskId] id: {}, username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_TASK_ID, id);

        ScanTask scanTask = scanTaskService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));
        this.userService.checkAccessRightOrElseThrow(scanTask, userPrincipal.getUser(), true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));

        ScanTaskStatusLog scanTaskStatusLog = scanStatusService.getLatestScanStatus(scanTask);

        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.PROJECT_ID, scanTaskStatusLog.getScanTask().getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_ID, scanTaskStatusLog.getScanTask().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTaskStatusLog.getScanTask().getStatus())
                .build());

        ScanStatusResponse scanStatusResponse = this.scanStatusService.convertScanTaskStatusLogToResponse(scanTaskStatusLog, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(scanStatusResponse);
    }

    /**
     * @param id project uuid
     * @return ScanTask with pageable
     */
    @GetMapping("/project/{id}/scan_task/ids")
    @ApiOperation(value = "Get the latest complected scan task ids by project uuid",
            nickname = "getLatestCompletedScanTaskByProjectId",
            notes = "Get the latest complected scan task list by project uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ScanTaskDto>> getLatestCompletedScanTaskByProjectId(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @PageableDefault(size = 2) Pageable pageable, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getLatestCompletedScanTaskByProjectId] id: {}, pageable: {}, username: {}", id, pageable, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);
        Project project = this.projectService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));

        this.userService.checkAccessRightOrElseThrow(project, userPrincipal.getUser(), true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(Sort.by(Sort.Direction.DESC, "modifiedOn")));
        }
        Page<ScanTask> scanTasks = scanTaskService.getLatestCompletedScanTaskByProject(project, pageable);
        Page<ScanTaskDto> result = scanTasks.map(ScanTaskService::convertScanTaskToDto);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * Get latest scan summary by scan task id
     * @param id Scan task id
     * @return SummaryResponse
     */
    @RequestMapping(value = "/scan_task/{id}/scan_summary", method={RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "Get Scan summary",
            nickname = "getScanSummary",
            notes = "Get the summary of the scan with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SummaryResponse> getScanSummary(
            @ApiParam(value = "uuid of the scan task", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID id,
            @RequestBody Optional<SearchIssueGroupRequest> searchIssueGroupRequestOptional,
            @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getScanSummary] scan task id: {}, username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_TASK_ID, id);
        ScanTask scanTask = this.scanTaskService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));
        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.PROJECT_ID, scanTask.getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());

        //prepare filter
        SearchIssueGroupRequest searchIssueGroupRequest=SearchIssueGroupRequest.builder().build();
        if(searchIssueGroupRequestOptional.isPresent()){
            searchIssueGroupRequest=searchIssueGroupRequestOptional.get();
        }
        searchIssueGroupRequest.setScanTaskId(id);
        //fill in project id if not provided
        if (searchIssueGroupRequest.getProjectId() == null) {
            searchIssueGroupRequest.setProjectId(scanTask.getProject().getId());
        }

        SummaryResponse summaryResponse = this.measureService.retrieveScanSummary(scanTask,searchIssueGroupRequest);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(summaryResponse);
    }

    /**
     * Get latest scan summary by project
     * @param id project id
     * @return SummaryResponse
     */
    @RequestMapping(value = "/project/{id}/scan_summary", method={RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "Get latest completed scan summary by project",
            nickname = "getLatestScanSummaryByProject",
            notes = "Get latest completed scan summary by project uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SummaryResponse> getLatestScanSummaryByProject(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID id,
            @RequestBody Optional<SearchIssueGroupRequest> searchIssueGroupRequestOptional,
            @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getLatestScanSummaryByProject] project  id: {}, username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);

        Project project = this.projectService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        Optional<ScanTask> completedScanTaskOptional = this.scanTaskService.getLatestCompletedScanTaskByProject(project);
        Optional<ScanTask> latestScanTaskOptional = this.scanTaskService.getLatestScanTask(project);
        SummaryResponse summaryResponse;
        // 1.Completed scan task is not empty(with scan success history):
        //    latest scan task has the same scan task id. Indicate that the project is not scanning now.
        //    with different scan task id, the project is scanning or the latest scan failed.
        // 2.Completed scan task is empty(no scan success history):
        //    latest scan task is empty. Indicate that the project has no scan history at all.
        //    latest scan task is not empty. Indicate that the project is scanning and has never scan successfully till now.
        if (completedScanTaskOptional.isPresent()) {
            TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                    .put(TracerUtil.Tag.SCAN_TASK_ID, completedScanTaskOptional.get().getId())
                    .put(TracerUtil.Tag.SCAN_TASK_STATUS, completedScanTaskOptional.get().getStatus())
                    .build());
            if (!latestScanTaskOptional.isPresent()) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_SCANTASK_SUMMARY_DATA_INCONSISTENT.unifyErrorCode,
                        CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_SUMMARY_DATA_INCONSISTENT.messageTemplate, completedScanTaskOptional.get().getId()));
            }

            //prepare filter
            SearchIssueGroupRequest searchIssueGroupRequest=SearchIssueGroupRequest.builder().build();
            if(searchIssueGroupRequestOptional.isPresent()){
                searchIssueGroupRequest=searchIssueGroupRequestOptional.get();
            }
            searchIssueGroupRequest.setProjectId(id);
            searchIssueGroupRequest.setScanTaskId(completedScanTaskOptional.get().getId());

            summaryResponse = this.measureService.retrieveScanSummary(completedScanTaskOptional.get(), latestScanTaskOptional.get(),searchIssueGroupRequest);
        } else {
            // Completed scan task is empty
            if (latestScanTaskOptional.isPresent()) {
                ScanTask latestScanTask = latestScanTaskOptional.get();
                SummaryResponse.ScanTaskSummary latestScanTaskSummary = SummaryResponse.ScanTaskSummary.builder()
                        .scanTaskId(latestScanTask.getId())
                        .commitId(latestScanTask.getCreatedOn() != null ? latestScanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID, null) : null)
                        .createdAt(latestScanTask.getCreatedOn() != null ? latestScanTask.getCreatedOn().getTime() : null)
                        .scanStartAt(latestScanTask.getScanStartAt() != null ? latestScanTask.getScanStartAt().getTime() : null)
                        .scanEndAt(latestScanTask.getScanEndAt() != null ? latestScanTask.getScanEndAt().getTime() : null)
                        .lastModifiedAt(latestScanTask.getModifiedOn() != null ? latestScanTask.getModifiedOn().getTime() : null)
                        .status(latestScanTask.getStatus().name())
                        .build();
                summaryResponse = SummaryResponse.builder().issueSummary(SummaryResponse.IssueSummary.builder().build()).latestScanTask(latestScanTaskSummary).build();
            } else {
                // Both completed scan task and latest scan task is empty
                summaryResponse = SummaryResponse.builder().issueSummary(SummaryResponse.IssueSummary.builder().build()).build();
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(summaryResponse);
    }

    /**
     * @param fromScanTaskId uuid of the scan task compare from
     * @param toScanTaskId   uuid of the scan task compare to
     * @return CompareScanResultResponse
     */
    @GetMapping("/scan_task/{fromScanTaskId}/compare/{toScanTaskId}")
    @Deprecated
    @ApiOperation(value = "compare scan task result by two scan task id",
            nickname = "compareScanResult",
            notes = "Retrieve the scan task compare result with two scan task id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompareScanResultResponse> compareScanResult(
            @ApiParam(value = "uuid of the scan task compare from", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID fromScanTaskId
            , @ApiParam(value = "uuid of the scan task compare to", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID toScanTaskId
            , @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[compareScanResult] id of the scan task compare from: {},id of the scan task compare to: {},username: {}", fromScanTaskId, toScanTaskId, userPrincipal.getUsername());
        Optional<ScanTask> optionalFromScanTask = scanTaskService.findById(fromScanTaskId);
        ScanTask fromScanTask = optionalFromScanTask.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, fromScanTaskId)));
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, fromScanTask.getProject().getId());
        Optional<ScanTask> optionalToScanTask = scanTaskService.findById(toScanTaskId);
        ScanTask toScanTask = optionalToScanTask.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, toScanTaskId)));
        if (!fromScanTask.getProject().getId().equals(toScanTask.getProject().getId())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_SCANTASK_CAN_NOT_COMPARE_DIFF_PROJECT.unifyErrorCode, AppException.ErrorCode.E_API_SCANTASK_CAN_NOT_COMPARE_DIFF_PROJECT.messageTemplate);
        }
        userService.checkAccessRightOrElseThrow(fromScanTask, userPrincipal.getUser(), true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, fromScanTask.getId())));
        userService.checkAccessRightOrElseThrow(toScanTask, userPrincipal.getUser(), true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, toScanTask.getId())));
        CompareScanResultResponse compareScanResultResponse = issueService.compareScanResult(fromScanTaskId, toScanTaskId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(compareScanResultResponse);
    }

    /**
     * @param id         project uuid
     * @param statusList list of ScanTask status to filter
     * @return ScanTaskDto with pageable
     */
    @GetMapping("/project/{id}/scan_tasks")
    @ApiOperation(value = "Get all scan tasks by project uuid and filter by status",
            nickname = "getScanTasksByStatus",
            notes = "Get all scan tasks by project uuid and filter by status",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ScanTaskDto>> getScanTasksByStatus(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID id
            , @ApiParam(value = "scan task status list ", example = "PENDING,PROCESSING,COMPLETED,FAILED,TERMINATED") @RequestParam(value = "statusList", required = false) List<String> statusList
            , @SortDefault(sort = ScanTask_.MODIFIED_ON, direction = Sort.Direction.DESC) Pageable pageable, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getScanTasksByStatus] project id: {}, status: {} username: {}", id, statusList != null ? statusList.toString() : null, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);
        Project project = this.projectService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        this.userService.checkAccessRightOrElseThrow(project, userPrincipal.getUser(), false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));
        List<String> invalidStatus = new ArrayList<>();
        if (statusList != null) {
            invalidStatus = statusList.stream().filter(status -> !EnumUtils.isValidEnum(ScanTask.Status.class, status)).collect(Collectors.toList());
        }
        if (!invalidStatus.isEmpty()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.unifyErrorCode,
                    CommonUtil.formatString("[{}]{} must have any value among {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.messageTemplate, invalidStatus, ScanTask.Status.values()));
        }
        List<ScanTask.Status> scanTaskStatusList;
        if (statusList == null) {
            scanTaskStatusList = Arrays.asList(ScanTask.Status.values());
        } else {
            scanTaskStatusList = statusList.stream().map(status -> EnumUtils.getEnum(ScanTask.Status.class, status)).collect(Collectors.toList());
        }
        Page<ScanTask> scanTasks = scanTaskService.getScanTaskByProjectAndStatus(project, scanTaskStatusList, pageable);
        Page<ScanTaskDto> result = scanTasks.map(ScanTaskService::convertScanTaskToDto);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    @ApiOperation(value = "Upload the diagnostic file",
            nickname = "uploadDiagnosticFile",
            notes = "upload the diagnostic file to file system",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PostMapping("/scan_task/{id}/diagnostic_info")
    public ResponseEntity<String> uploadDiagnosticFile(@RequestParam("upload_file") @NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL) MultipartFile file,
                                                       @RequestParam(value = "file_checksum", required = false) String checksum,
                                                       @ApiParam(value = "uuid of the scan task", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID id,
                                                       @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[uploadDiagnosticFile] file size: {}, file name: {}, checksum: {}, principal username: {}", file.getSize(), file.getName(), checksum, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_TASK_ID, id);

        ScanTask scanTask = scanTaskService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));

        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.PROJECT_ID, scanTask.getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());

        String filename = performanceService.saveLogFile(scanTask, file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(filename);
    }


    @ApiOperation(value = "download diagnostic info",
            nickname = "downloadDiagnosticInfo",
            notes = "download diagnostic info",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            response = ResponseEntity.class)
    @GetMapping("/scan_task/{id}/diagnostic_info")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Resource> downloadDiagnosticInfo(@ApiParam(value = "uuid of the scan task", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")@PathVariable UUID id) throws Exception {
        log.info("[downloadDiagnosticInfo] scanTask id: {}", id);
        TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_TASK_ID, id);
        ScanTask scanTask = scanTaskService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));
        TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                .put(TracerUtil.Tag.PROJECT_ID, scanTask.getProject().getId())
                .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                .build());
        Resource resource = performanceService.getDownloadFilePath(scanTask);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", resource.getFilename()));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }


    @ApiOperation(value = "search scan task",
            nickname = "searchScanTask",
            notes = "search scan task",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            response = ResponseEntity.class)
    @PostMapping("/scan_task/search")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Page<ScanTaskDto>> searchScanTask(@RequestBody SearchScanTaskRequest searchScanTaskRequest, Pageable pageable, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[searchScanTask] searchScanTaskRequest : {}, pageable:{}", searchScanTaskRequest, pageable);
        Project project = this.projectService.findById(searchScanTaskRequest.getProjectId()).orElseThrow(() ->
                new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, searchScanTaskRequest.getProjectId())));
        this.userService.checkAccessRightOrElseThrow(project, userPrincipal.getUser(), false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, searchScanTaskRequest.getProjectId())));

        List<ScanTask.Status> status = searchScanTaskRequest.getStatus().stream().map(s -> EnumUtils.getEnumIgnoreCase(ScanTask.Status.class, s)).collect(Collectors.toList());
        List<ProjectConfigAttribute> existAttributes = searchScanTaskRequest.getExistAttributes().stream().map(attribute -> ProjectConfigAttribute.builder()
                .type(EnumUtils.getEnumIgnoreCase(VariableUtil.ProjectConfigAttributeTypeName.Type.class, attribute.getType()))
                .name(attribute.getName())
                .build()).collect(Collectors.toList());
        List<ProjectConfigAttribute> equalAttributes = searchScanTaskRequest.getEqualAttributes().stream().map(attribute -> ProjectConfigAttribute.builder()
                .type(EnumUtils.getEnumIgnoreCase(VariableUtil.ProjectConfigAttributeTypeName.Type.class, attribute.getType()))
                .name(attribute.getName())
                .value(attribute.getValue())
                .build()).collect(Collectors.toList());
        Sort sort = pageable.getSort();
        if (sort.getOrderFor("createdOn") == null) {
            sort = sort.and(Sort.by(Sort.Direction.DESC, "createdOn"));
        }
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<ScanTask> scanTasks = this.scanTaskService.searchScanTask(project, status, existAttributes, equalAttributes, pageable);
        Page<ScanTaskDto> result = scanTasks.map(ScanTaskService::convertScanTaskToDto);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    @ApiOperation(value = "search scan task log",
            nickname = "searchScanTask log",
            notes = "search scan task log",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            response = ResponseEntity.class)
    @PostMapping("/scan_task_log")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Page<ScanTaskLog>> searchScanTaskLog(@RequestBody SearchScanTaskLogRequest searchScanTaskLogRequest, Pageable pageable, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[searchScanTaskLog] SearchScanTaskLogRequest : {}, pageable:{}", searchScanTaskLogRequest, pageable);
        Project project = this.projectService.findById(searchScanTaskLogRequest.getProjectId()).orElseThrow(() ->
                new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, searchScanTaskLogRequest.getProjectId())));
        this.userService.checkAccessRightOrElseThrow(project, userPrincipal.getUser(), false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, searchScanTaskLogRequest.getProjectId())));


        Sort sort = pageable.getSort();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<ScanTaskLog> scanTaskLog = this.scanTaskService.searchScanTaskLog(searchScanTaskLogRequest.getProjectId(),
                searchScanTaskLogRequest.getTargetRangeStartDate(),
                searchScanTaskLogRequest.getTargetRangeEndDate(),
                searchScanTaskLogRequest.getCommitIdPattern(),
                searchScanTaskLogRequest.getRuleSets(),
                searchScanTaskLogRequest.getRepoActions(),
                pageable);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(scanTaskLog);
    }


    @ApiOperation(value = "search scan task log",
            nickname = "searchScanTask log",
            notes = "search scan task log",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            response = ResponseEntity.class)
    @GetMapping("/project/{projectId}/commit/{commitId}")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<ScanTaskIdResponse> getScanTaskId(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID projectId,
            @ApiParam(value = "commit id of source code control tool such as git", example = "2227a0f3bedefc9292283d65ccf6cca11f1334c2") @PathVariable String commitId, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getScanTaskId] projectId : {}, commitId:{}", projectId, commitId);

        ScanTaskIdResponse scanTaskIdResponse = scanTaskService.getScanTaskIdResponse(projectId, commitId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(scanTaskIdResponse);
    }

    @ApiOperation(
            value = "exchange commit id by scan id",
            nickname = "exchangeCommitIdByScanId",
            notes = "exchange commit id by scan id",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            response = ResponseEntity.class
    )
    @GetMapping("/commit_id/scan_id/{scanId}")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<String> exchangeCommitIdByScanId(
            @ApiParam(value = "scan id which used as issue group id prefix", example = "DBH3O") @PathVariable String scanId,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[exchangeCommitIdByScanId] scanId: {}", scanId);
        IssueGroup issueGroup = this.issueService.findIssueGroupStartWith(scanId)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanId: {}",
                                AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate,
                                scanId
                        )
                ));
        ScanTask scanTask = this.scanTaskService.findById(issueGroup.getOccurScanTaskId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                issueGroup.getOccurScanTaskId()
                        )
                ));
        String commitId = scanTask.getProjectConfig().getAttributes().stream()
                .filter(attr -> StringUtils.equals(attr.getName(), "commitId"))
                .map(ProjectConfigAttribute::getValue)
                .findFirst()
                .orElse("");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(commitId);
    }

}
