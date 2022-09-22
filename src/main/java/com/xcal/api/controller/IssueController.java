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
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.IssueDiffDto;
import com.xcal.api.model.dto.IssueDto;
import com.xcal.api.model.payload.*;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.*;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.VariableUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/issue_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Issue Service")
public class IssueController {
    @NonNull ImportService importService;
    @NonNull IssueService issueService;
    @NonNull UserService userService;
    @NonNull ProjectService projectService;
    @NonNull RuleService ruleService;
    @NonNull FileService fileService;
    @NonNull ScanTaskService scanTaskService;
    @NonNull ScanFileService scanFileService;
    @NonNull OrchestrationService orchestrationService;
    @NonNull AsyncJobService asyncJobService;
    @NonNull KafkaTemplate<String, String> kafkaTemplate;
    @NonNull ObjectMapper om;

    @Value("${scan.archive-result}")
    private boolean archiveScanResult;

    /**
     * @param id uuid of project
     * @return List of AssignSummary
     */
    @GetMapping("/project/{id}/issue_assignsummary")
    @Deprecated
    @ApiOperation(value = "Get issue assign summary by user",
            nickname = "getIssueSummaryCountByUser",
            notes = "get the issue summary assign by User",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<IssueSummaryResponse.AssignSummary>> getIssueSummaryCountByUser(
            @ApiParam(value = "uuid of the scan project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getIssueSummaryCountByUser] id: {}, principal username: {}", id, userPrincipal.getUsername());
        Optional<Project> projectOptional = this.projectService.findById(id);
        Project project = projectOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        Optional<ScanTask> optionalScanTask = scanTaskService.getLatestCompletedScanTaskByProject(project);
        List<IssueSummaryResponse.AssignSummary> assignSummaries;
        if (optionalScanTask.isPresent()) {
            ScanTask scanTask = optionalScanTask.get();
            this.userService.checkAccessRightOrElseThrow(scanTask, userPrincipal.getUser(), true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                    CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));
            assignSummaries = issueService.findIssueSummaryCountByUser(scanTask.getId());
        } else {
            assignSummaries = new ArrayList<>();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(assignSummaries);
    }

    /**
     * @param id uuid of project
     * @return List of the AssignSummary
     */
    @GetMapping("/scan_task/{id}/issue_assignsummary")
    @Deprecated
    @ApiOperation(value = "Get issue assign summary by user",
            nickname = "countIssueByUser",
            notes = "get the issue summary assign by User",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<IssueSummaryResponse.AssignSummary>> countIssueByUser(
            @ApiParam(value = "uuid of the scan project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[countIssueByUser] id: {}, principal username: {}", id, userPrincipal.getUsername());
        Optional<ScanTask> optionalScanTask = scanTaskService.findById(id);
        ScanTask scanTask = optionalScanTask.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));
        this.userService.checkAccessRightOrElseThrow(scanTask, userPrincipal.getUser(), true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));
        List<IssueSummaryResponse.AssignSummary> assignSummaries = issueService.findIssueSummaryCountByUser(scanTask.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(assignSummaries);
    }

    @ApiOperation(value = "List Issue in scan task",
            nickname = "listIssueInScanTask",
            notes = "List issues with paging, default page size 20, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @GetMapping("/scan_task/{id}/issues")
    public ResponseEntity<Page<IssueDto>> listIssueInScanTask(@PathVariable UUID id, Pageable pageable, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[listIssueInScanTask] id: {}, pageable: {}, principal username: {}", id, pageable, userPrincipal.getUsername());
        Optional<ScanTask> scanTaskOptional = this.scanTaskService.findById(id);
        ScanTask scanTask = scanTaskOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));

        this.userService.checkAccessRightOrElseThrow(scanTask.getProject(), userPrincipal.getUser(), false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));

        Page<Issue> issues = this.issueService.listIssueInScanTask(scanTask, pageable);
        RestResponsePage<IssueDto> result = RestResponsePage.<IssueDto>builder()
                .content(this.issueService.convertIssuesToDto(issues.getContent(), locale))
                .pageable(issues.getPageable())
                .total(issues.getTotalElements())
                .build();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
    }

    @ApiOperation(value = "Get all Issue diff in scan task",
            nickname = "getAllIssueDiffInScanTask",
            notes = "Get all Issue diff in scan task",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @GetMapping("/scan_task/{id}/issue_diff")
    public ResponseEntity<List<IssueDiffDto>> getAllIssueDiffInScanTask(@PathVariable UUID id, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getAllIssueDiffInScanTask] id: {}, principal username: {}", id, userPrincipal.getUsername());
        ScanTask scanTask = this.scanTaskService.findById(id).orElseThrow(() ->
                new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));

        this.userService.checkAccessRightOrElseThrow(scanTask.getProject(), userPrincipal.getUser(), false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));

        List<IssueDiff> issueDiffs = this.issueService.getIssueDiff(scanTask);
        Map<String, I18nMessage> i18nMessageMap = this.issueService.retrieveI18nMessageMapByIssues(issueDiffs.stream().map(IssueDiff::getIssue).collect(Collectors.toList()), locale);
        List<IssueDiffDto> result = issueDiffs.stream().map(issueDiff -> IssueService.convertIssueDiffToDto(issueDiff, i18nMessageMap)).collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
    }

    @ApiOperation(
            value = "Sync import Issues to scan task",
            nickname = "syncImportIssueToScanTask",
            notes = "Sync import issues to scan task",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PostMapping("/scan_task/{id}/issues")
    public ResponseEntity<ImportIssueResponse> syncImportIssueToScanTask(
            @ApiParam(value = "uuid of the scan task", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID id,
            @RequestParam("upload_file") MultipartFile file,
            @RequestParam(value = "file_info_id", required = false) UUID fileInfoId,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException, IOException {
        log.info("[syncImportIssueToScanTask] id: {}, file isEmpty: {}, archive: {}, principal username: {}", id, file.isEmpty(), this.archiveScanResult, userPrincipal.getUsername());
        File inputFile = this.fileService.getLocalTempFile(file);

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

        if (!Arrays.asList(ScanTask.Status.COMPLETED, ScanTask.Status.FAILED, ScanTask.Status.TERMINATED).contains(scanTask.getStatus())) {
            importService.syncImportScanResult(inputFile, scanTask, fileInfoId, userPrincipal.getUsername());
        }

        if(StringUtils.isBlank(scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL, null))
            && Boolean.valueOf(scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_SOURCE, null))) {
            log.debug("[syncImportIssueToScanTask] no gitUrl in project config and uploadSource is true, delete the source code file without issue");
            issueService.deleteSourceCodeFileWithoutIssue(scanTask, userPrincipal.getUsername());
        }

        ImportIssueResponse importIssueResponse = ImportIssueResponse.builder().scanTaskId(scanTask.getId()).build();
        log.info("[syncImportIssueToScanTask] API End. Returning result to http client. id: {}", id);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(importIssueResponse);
    }

    @ApiOperation(
            value = "Async import Issues to scan task",
            nickname = "asyncImportIssueToScanTask",
            notes = "Async import issues to scan task",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PostMapping("/scan_task/{id}/issues_async")
    public ResponseEntity<Map<String, Object>> asyncImportIssueToScanTask(
            @ApiParam(value = "uuid of the scan task", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID id,
            @RequestParam("upload_file") MultipartFile file,
            @RequestParam(value = "file_checksum", required = false) String checksum,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[asyncImportIssueToScanTask] id: {}, file isEmpty: {}, checksum: {}, principal username: {}", id, file.isEmpty(), checksum, userPrincipal.getUsername());
        File inputFile = this.fileService.getLocalTempFile(file);

        if (StringUtils.isNotBlank(checksum)) {
            boolean isFileOk = fileService.checkIntegrityWithCrc32(inputFile, checksum);
            if (!isFileOk) {
                throw new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_INCONSISTENT,
                        HttpURLConnection.HTTP_CONFLICT,
                        AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] expectedChecksum: {}",
                                AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate,
                                checksum
                        )
                );
            }
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
                    .issueFile(inputFile.getAbsolutePath())
                    .fixedIssueFile("")
                    .newIssueFile("")
                    .step(0)
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

            log.info("[asyncImportIssueToScanTask] Before sending scan task topic to kafka: {}", asyncJob.getId().toString());
            this.kafkaTemplate.send("scan-task-topic", asyncJob.getId().toString());
            log.info("[asyncImportIssueToScanTask] After sending scan task topic to kafka: {}", asyncJob.getId().toString());
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
        log.info("[asyncImportIssueToScanTask] Return result to http client");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
    }

    @ApiOperation(value = "Retrieve scan result from scan scan summary",
            nickname = "retrieveScanResultFromScanSummary",
            notes = "The raw imported issues that stored in scan summary")
    @Deprecated
    @GetMapping("/scan_task/{id}/scan_summary/scan_result")
    public ResponseEntity<ImportIssueResponse> retrieveScanResultFromScanSummary(@PathVariable UUID id, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[retrieveScanResultFromScanSummary] id: {}, principal username: {}", id, userPrincipal.getUsername());
        ScanTask scanTask = this.scanTaskService.findById(id).orElseThrow(() ->
                new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));

        this.userService.checkAccessRightOrElseThrow(scanTask.getProject(), userPrincipal.getUser(), true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));

        ImportIssueResponse importIssueResponse = this.issueService.retrieveScanResultFromScanSummary(scanTask, locale);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(importIssueResponse);
    }

    @ApiOperation(
            value = "Import Issues diff",
            nickname = "importIssueDiff",
            notes = "Import issues diff",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Deprecated
    @PostMapping("/scan_task/{id}/{baselineId}/issue_diff")
    public ResponseEntity<ImportIssueDiffResponse> importIssueDiff(
            @PathVariable UUID id,
            @PathVariable UUID baselineId,
            @RequestParam(value = "fixed_issue_file", required = false) MultipartFile fixedIssueFile,
            @RequestParam(value = "new_issue_file", required = false) MultipartFile newIssueFile,
            @RequestParam(value = "fixed_issue_checksum", required = false) String fixedIssueChecksum,
            @RequestParam(value = "new_issue_checksum", required = false) String newIssueChecksum,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[importIssueDiff] id: {}, baselineId: {}, principal username: {}", id, baselineId, userPrincipal.getUsername());
        File inputFixedIssueFile = null;
        File inputNewIssueFile = null;

        if (fixedIssueFile != null) {
            inputFixedIssueFile = this.fileService.getLocalTempFile(fixedIssueFile);
            if (StringUtils.isNotBlank(fixedIssueChecksum)) {
                boolean isFileOk = fileService.checkIntegrityWithCrc32(inputFixedIssueFile, fixedIssueChecksum);
                if (!isFileOk) {
                    throw new AppException(
                            AppException.LEVEL_ERROR,
                            AppException.ERROR_CODE_DATA_INCONSISTENT,
                            HttpURLConnection.HTTP_CONFLICT,
                            AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                            CommonUtil.formatString(
                                    "[{}] expectedChecksum: {}",
                                    AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate,
                                    fixedIssueChecksum
                            )
                    );
                }
            }
        }

        if (newIssueFile != null) {
            inputNewIssueFile = this.fileService.getLocalTempFile(newIssueFile);
            if (StringUtils.isNotBlank(newIssueChecksum)) {
                boolean isFileOk = fileService.checkIntegrityWithCrc32(inputNewIssueFile, newIssueChecksum);
                if (!isFileOk) {
                    throw new AppException(
                            AppException.LEVEL_ERROR,
                            AppException.ERROR_CODE_DATA_INCONSISTENT,
                            HttpURLConnection.HTTP_CONFLICT,
                            AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                            CommonUtil.formatString(
                                    "[{}] expectedChecksum: {}",
                                    AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate,
                                    newIssueChecksum
                            )
                    );
                }
            }
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

        Optional<ScanTask> baselineScanTaskOptional = this.scanTaskService.findById(baselineId);
        ScanTask baselineScanTask = baselineScanTaskOptional
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] baselineScanTaskId: {}",
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                baselineId
                        )
                ));

        ImportIssueDiffRequest importIssueDiffRequest = this.issueService.processIssueDiffFile(inputFixedIssueFile, inputNewIssueFile, baselineScanTask, scanTask, userPrincipal.getUsername());
        ImportIssueDiffResponse importIssueDiffResponse = this.issueService.importIssueDiff(importIssueDiffRequest, userPrincipal.getUsername());

        if (inputFixedIssueFile != null) {
            log.debug("[asyncImportIssueToScanTask] begin to delete file {}", inputFixedIssueFile.getPath());
            boolean isFileDeleted = FileUtils.deleteQuietly(inputFixedIssueFile);
            log.debug("[asyncImportIssueToScanTask] is file {} deleted: {}", inputFixedIssueFile.getPath(), isFileDeleted);
        }

        if (inputNewIssueFile != null) {
            log.debug("[asyncImportIssueToScanTask] begin to delete file {}", inputNewIssueFile.getPath());
            boolean isFileDeleted = FileUtils.deleteQuietly(inputNewIssueFile);
            log.debug("[asyncImportIssueToScanTask] is file {} deleted: {}", inputNewIssueFile.getPath(), isFileDeleted);
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(importIssueDiffResponse);
    }

    @ApiOperation(
            value = "Async import Issue diff",
            nickname = "asyncImportIssueDiff",
            notes = "Async import issue diff",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Deprecated
    @PostMapping("/scan_task/{id}/{baselineId}/issue_diff_async")
    public ResponseEntity<Map<String, Object>> asyncImportIssueDiff(
            @PathVariable UUID id,
            @PathVariable UUID baselineId,
            @RequestParam(value = "fixed_issue_file", required = false) MultipartFile fixedIssueFile,
            @RequestParam(value = "new_issue_file", required = false) MultipartFile newIssueFile,
            @RequestParam(value = "fixed_issue_checksum", required = false) String fixedIssueChecksum,
            @RequestParam(value = "new_issue_checksum", required = false) String newIssueChecksum,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[asyncImportIssueDiff] id: {}, principal username: {}", id, userPrincipal.getUsername());
        String fixedIssueFilePath = "";
        String newIssueFilePath = "";

        if (fixedIssueFile != null) {
            File inputFixedIssueFile = this.fileService.getLocalTempFile(fixedIssueFile);
            if (StringUtils.isNotBlank(fixedIssueChecksum)) {
                boolean isFileOk = fileService.checkIntegrityWithCrc32(inputFixedIssueFile, fixedIssueChecksum);
                if (!isFileOk) {
                    throw new AppException(
                            AppException.LEVEL_ERROR,
                            AppException.ERROR_CODE_DATA_INCONSISTENT,
                            HttpURLConnection.HTTP_CONFLICT,
                            AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                            CommonUtil.formatString(
                                    "[{}] expectedChecksum: {}",
                                    AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate,
                                    fixedIssueChecksum
                            )
                    );
                }
            }
            fixedIssueFilePath = inputFixedIssueFile.getAbsolutePath();
        }

        if (newIssueFile != null) {
            File inputNewIssueFile = this.fileService.getLocalTempFile(newIssueFile);
            if (StringUtils.isNotBlank(newIssueChecksum)) {
                boolean isFileOk = fileService.checkIntegrityWithCrc32(inputNewIssueFile, newIssueChecksum);
                if (!isFileOk) {
                    throw new AppException(
                            AppException.LEVEL_ERROR,
                            AppException.ERROR_CODE_DATA_INCONSISTENT,
                            HttpURLConnection.HTTP_CONFLICT,
                            AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                            CommonUtil.formatString(
                                    "[{}] expectedChecksum: {}",
                                    AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate,
                                    newIssueChecksum
                            )
                    );
                }
            }
            newIssueFilePath = inputNewIssueFile.getAbsolutePath();
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

        Optional<ScanTask> baselineScanTaskOptional = this.scanTaskService.findById(baselineId);
        ScanTask baselineScanTask = baselineScanTaskOptional
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] baselineScanTaskId: {}",
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
                    .baselineScanTask(baselineScanTask.getId().toString())
                    .issueFile("")
                    .fixedIssueFile(fixedIssueFilePath)
                    .newIssueFile(newIssueFilePath)
                    .step(1)
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

    @ApiOperation(value = "Delete Issue from scan task",
            nickname = "deleteIssueFromScanTask",
            notes = "Delete issues from the scan task")
    @Deprecated
    @DeleteMapping("/scan_task/{id}/issues")
    @RolesAllowed("ROLE_XCALADMIN")
    public ResponseEntity<Void> deleteIssueFromScanTask(@PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[deleteIssueFromScanTask] id: {}, principal username: {}", id, userPrincipal.getUsername());
        Optional<ScanTask> scanTaskOptional = this.scanTaskService.findById(id);
        ScanTask scanTask = scanTaskOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND,AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));
        this.orchestrationService.deleteAllInScanTask(scanTask, false, userPrincipal.getUser());
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(
            value = "Assign issue to a user",
            nickname = "assignIssue",
            notes = "Assign issue to a user, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Deprecated
    @PostMapping("/issue/{id}/user/{userId}")
    public ResponseEntity<IssueDto> assignIssue(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[assignIssue] id: {}, userId: {}, principal username: {}", id, userId, userPrincipal.getUsername());
        Issue issue = this.issueService.findById(id)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] issueId: {}",
                                AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate,
                                id
                        )
                ));
        this.userService.checkAccessRightOrElseThrow(
                issue,
                userPrincipal.getUser(),
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] issueId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                id
                        )
                ));
        issue = this.issueService.assignIssue(issue, userId, userPrincipal.getUsername());
        IssueDto issueDto = this.issueService.convertIssueToDto(issue, locale);
        this.issueService.sendAssignIssueEmail(issue, userId, userPrincipal.getUser(), locale, userPrincipal.getUsername());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(issueDto);
    }

    @ApiOperation(value = "Update issue status",
            nickname = "updateIssueStatus",
            notes = "Update issues status, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @PostMapping("/issue/{id}/status/{status}")
    public ResponseEntity<IssueDto> updateIssueStatus(@PathVariable UUID id, @PathVariable String status, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateIssueStatus] id: {}, status: {}, principal username: {}", id, status, userPrincipal.getUsername());
        IssueDto result;
        Optional<Issue> issueOptional = this.issueService.findById(id);
        Issue issue = issueOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate, id)));

        this.userService.checkAccessRightOrElseThrow(issue, userPrincipal.getUser(), () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));

        issue = this.issueService.updateIssueStatus(issue, status, userPrincipal.getUsername());
        result = this.issueService.convertIssueToDto(issue, locale);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
    }

    @ApiOperation(value = "Update issue severity",
            nickname = "updateIssueSeverity",
            notes = "Update issues severity",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @PostMapping("/issue/{id}/severity/{severity}")
    public ResponseEntity<IssueDto> updateIssueSeverity(@PathVariable UUID id, @PathVariable String severity, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateIssueSeverity] id: {}, severity: {}, principal username: {}", id, severity, userPrincipal.getUsername());
        IssueDto result;
        Optional<Issue> issueOptional = this.issueService.findById(id);
        Issue issue = issueOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate, id)));

        this.userService.checkAccessRightOrElseThrow(issue, userPrincipal.getUser(), () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));

        issue = this.issueService.updateIssueSeverity(issue, severity, userPrincipal.getUsername());
        result = this.issueService.convertIssueToDto(issue, locale);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
    }

    @ApiOperation(value = "Update issue action",
            nickname = "updateIssueAction",
            notes = "Update issues action, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @PostMapping("/issue/{id}/action/{action}")
    public ResponseEntity<IssueDto> updateIssueAction(@PathVariable UUID id, @PathVariable String action, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateIssueStatus] id: {}, action: {}, principal username: {}", id, action, userPrincipal.getUsername());
        IssueDto result;
        Optional<Issue> issueOptional = this.issueService.findById(id);
        Issue issue = issueOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate, id)));

        this.userService.checkAccessRightOrElseThrow(issue, userPrincipal.getUser(), () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));

        issue = this.issueService.updateIssueAction(issue, action, userPrincipal.getUsername());

        result = this.issueService.convertIssueToDto(issue, locale);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
    }

    /**
     * @param id id of the issue
     * @return Issue DTO
     */
    @ApiOperation(value = "Get issue by id",
            nickname = "getIssueById",
            notes = "Retrieve the issue with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @GetMapping("/issue/{id}")
    public ResponseEntity<IssueDto> getIssueById(
            @ApiParam(value = "uuid of the issue", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getIssueById] id: {}, username: {}", id, userPrincipal.getUsername());
        IssueDto result;
        Optional<Issue> issueOptional = this.issueService.findById(id);
        Issue issue = issueOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate, id)));

        this.userService.checkAccessRightOrElseThrow(issue, userPrincipal.getUser(), () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));

        result = this.issueService.convertIssueToDto(issue, issue.getIssueTraces(), locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * @param id id of the issue
     * @param checksum id of the issue trace set
     * @return Issue DTO
     */
    @GetMapping("/issue/{id}/issue_trace_set/{checksum}")
    @Deprecated
    @ApiOperation(value = "Get issue by id",
            nickname = "getIssueById",
            notes = "Retrieve the issue with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IssueDto> getIssueByIdAndChecksum(
            @ApiParam(value = "uuid of the issue", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @PathVariable String checksum, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getIssueByIdAndChecksum] id: {}, checksum: {}, username: {}", id, checksum, userPrincipal.getUsername());
        IssueDto result;
        Optional<Issue> issueOptional = this.issueService.findById(id);
        Issue issue = issueOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate, id)));

        this.userService.checkAccessRightOrElseThrow(issue, userPrincipal.getUser(), () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));

        List<IssueTrace> issueTraces;
        issueTraces = this.issueService.findByIssueAndChecksum(issue, checksum);

        result = this.issueService.convertIssueToDto(issue, issueTraces, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    @ApiOperation(value = "Assign issues to user",
            nickname = "assignIssuesToUsers",
            notes = "Assign issues to users, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @PostMapping("/issues/users")
    public ResponseEntity<Void> assignIssuesToUsers(@RequestBody AssignIssuesRequest assignIssueRequest, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[assignIssuesToUsers] issueUserMapList: {}, principal username: {}", assignIssueRequest, userPrincipal.getUsername());
        issueService.assignIssuesToUsers(assignIssueRequest, userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Send assign issues to user",
            nickname = "sendIssuesToUsers",
            notes = "Send assign issues to users, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @PostMapping("/issues/users/email")
    public ResponseEntity<Void> sendIssuesToUsers(@RequestBody AssignIssuesRequest assignIssueRequest, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[sendIssuesToUsers] issueUserMapList: {}, locale: {}, principal username: {}", assignIssueRequest, locale.toLanguageTag(), userPrincipal.getUsername());
        issueService.sendIssuesToUsers(assignIssueRequest, userPrincipal.getUser(), locale, userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(
            value = "Search Issue in ScanTask",
            nickname = "searchIssueInScanTask",
            notes = "Search Issue in ScanTask, default page size 20, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Deprecated
    @PostMapping("/search_issue")
    public ResponseEntity<Page<IssueDto>> searchIssue(
            @RequestBody SearchIssueRequest searchIssueRequest,
            Pageable pageable,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[searchIssue] searchIssueRequest: {}, pageable: {}, username: {}",
                searchIssueRequest,
                pageable,
                userPrincipal.getUsername()
        );

        Optional<ScanTask> scanTaskOptional = Optional.empty();
        if (searchIssueRequest.getScanTaskId() != null) {
            scanTaskOptional = this.scanTaskService.findById(searchIssueRequest.getScanTaskId());
        } else if (searchIssueRequest.getProjectId() != null) {
            Project project = this.projectService.findById(searchIssueRequest.getProjectId())
                    .orElseThrow(() -> new AppException(
                            AppException.LEVEL_WARN,
                            AppException.ERROR_CODE_DATA_NOT_FOUND,
                            HttpURLConnection.HTTP_NOT_FOUND,
                            AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                            CommonUtil.formatString(
                                    "[{}] projectId: {}",
                                    AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                    searchIssueRequest.getProjectId()
                            )
                    ));
            scanTaskOptional = this.scanTaskService.getLatestCompletedScanTaskByProject(project);
        }

        Page<Issue> issues = new RestResponsePage<>();
        if (scanTaskOptional.isPresent()) {
            ScanTask scanTask = scanTaskOptional.get();
            this.userService.checkAccessRightOrElseThrow(
                    scanTask.getProject(),
                    userPrincipal.getUser(),
                    false,
                    () -> new AppException(
                            AppException.LEVEL_ERROR,
                            AppException.ERROR_CODE_UNAUTHORIZED,
                            HttpURLConnection.HTTP_FORBIDDEN,
                            AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                            CommonUtil.formatString(
                                    "[{}] projectId: {}",
                                    AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                    scanTask.getProject().getId()
                            )
                    ));
            String seq = Optional.ofNullable(searchIssueRequest.getSeq())
                    .map(s -> StringUtils.leftPad(s, 5, '0'))
                    .orElse(null);
            issues = this.issueService.searchIssue(
                    scanTask,
                    searchIssueRequest.getRuleSetId(),
                    searchIssueRequest.getRuleSetName(),
                    seq,
                    searchIssueRequest.getIssueAttributes(),
                    searchIssueRequest.getRuleInformationAttributes(),
                    searchIssueRequest.getRuleInformationIds(),
                    searchIssueRequest.getScanFileIds(),
                    searchIssueRequest.getSearchIssueType(),
                    pageable
            );
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(RestResponsePage.<IssueDto>builder()
                        .content(this.issueService.convertIssuesToDto(issues.getContent(), locale))
                        .pageable(issues.getPageable())
                        .total(issues.getTotalElements())
                        .build());
    }

    /**
     * @param listIssueRequest ListIssueRequest contain list of issue id to be search
     * @return Issue DTO
     */
    @ApiOperation(value = "Get issue by list of issue id",
            nickname = "getIssueByIds",
            notes = "Retrieve the issue list with the corresponding issue id list",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @PostMapping("/issues")
    public ResponseEntity<List<IssueDto>> getIssueByIds(
            @ApiParam(value = "ListIssueRequest contain list of issue id to be search", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a,c40029be-eda6-4d62-b1ef-d05e2e91a72a") @RequestBody ListIssueRequest listIssueRequest, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getIssueByIds] id: {}, username: {}", listIssueRequest.getIssueIds(), userPrincipal.getUsername());
        List<Issue> issues = issueService.findIssuesByIds(listIssueRequest.getIssueIds());
        for (Issue issue : issues) {
            userService.checkAccessRightOrElseThrow(issue, userPrincipal.getUser(), () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                    CommonUtil.formatString("[{}] issueId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, issue.getId())));
        }
        List<IssueDto> issueDtoList = this.issueService.convertIssuesToDto(issues, locale);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(issueDtoList);
    }

    @Deprecated
    @PostMapping("/issue_statistics")
    public ResponseEntity<IssueStatisticsResponse> calcIssueStatistics(
            @RequestBody IssueStatisticsRequest issueStatisticsRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[calcIssueStatistics] request: {}, username: {}", issueStatisticsRequest, userPrincipal.getUsername());
        ScanTask scanTask = this.scanTaskService.findById(issueStatisticsRequest.getScanTaskId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] id: {}",
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                issueStatisticsRequest.getScanTaskId()
                        )
                ));
        List<ScanFile> scanFileList = this.scanFileService.findByScanFileIds(issueStatisticsRequest.getScanFileIds());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(this.issueService.calcIssueStatistics(scanTask, scanFileList));
    }

    @Deprecated
    @GetMapping("/issue/{id}/traces")
    public ResponseEntity<Page<IssueDto.IssueTraceInfo>> getIssueTraces(
            @PathVariable UUID id,
            Pageable pageable,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[getIssueTraces] id: {}, pageable: {}, username: {}", id, pageable, userPrincipal.getUsername());
        Issue issue = this.issueService.findById(id)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] issueId: {}",
                                AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate,
                                id
                        )
                ));
        this.userService.checkAccessRightOrElseThrow(
                issue,
                userPrincipal.getUser(),
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] issueId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                id
                        )
                ));
        List<IssueTrace> issueTraces = this.issueService.findIssueTraceByIssueGroupByChecksum(issue, pageable);
        IssueDto issueDto = this.issueService.convertIssueToDto(issue, issueTraces, locale);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(new PageImpl<>(
                        issueDto.getIssueTraceInfos(),
                        pageable,
                        this.issueService.countIssueTraceByIssueGroupByChecksum(issue)
                ));
    }

}
