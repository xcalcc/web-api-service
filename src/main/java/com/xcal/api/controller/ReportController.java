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

import com.xcal.api.entity.Project;
import com.xcal.api.entity.RuleSet;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.v3.ExternalCsvReportRequest;
import com.xcal.api.model.payload.v3.ReportPDFResponse;
import com.xcal.api.model.payload.v3.ReportRequest;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.*;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.TracerUtil;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Tracer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/report_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Report Service")
public class ReportController {

    private final ReportService reportService;

    private final ScanTaskService scanTaskService;

    private final UserService userService;

    private final RuleService ruleService;

    private final ProjectService projectService;

    private final ModelMapper modelMapper;

    private final Tracer tracer;

    /**
     * @param id            scan task id
     * @param userPrincipal user principal
     * @return Pdf report
     * @throws AppException when report generate error
     */
    @ApiOperation(
            value = "Issue Summary Report",
            nickname = "issueSummaryReport",
            notes = "Issue Summary Report"
    )
    @GetMapping("/issue_summary/scan_task/{id}")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Resource> issueSummaryReport(
            HttpServletRequest request,
            @PathVariable UUID id,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[issueSummaryReport] scanTaskId: {}, principal username: {}",
                id,
                userPrincipal.getUsername()
        );
        ScanTask scanTask = this.scanTaskService.findById(id)
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
        TracerUtil.setTag(this.tracer, TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId());
        this.userService.checkAccessRightOrElseThrow(
                scanTask,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                scanTask.getId()
                        )
                ));
        RuleSet ruleSet;
        Optional<RuleSet> ruleSetOptional = this.ruleService.getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersion(
                "Xcalibyte",
                "1",
                "BUILTIN",
                "1"
        );
        if (ruleSetOptional.isPresent()) {
            ruleSet = ruleSetOptional.get();
        } else {
            ruleSet = this.ruleService.findRuleSet(PageRequest.of(0, 100)).get().findFirst()
                    .orElseThrow(() -> new AppException(
                            AppException.LEVEL_WARN,
                            AppException.ERROR_CODE_DATA_NOT_FOUND,
                            HttpURLConnection.HTTP_NOT_FOUND,
                            AppException.ErrorCode.E_API_RULE_COMMON_NOT_FOUND.unifyErrorCode,
                            AppException.ErrorCode.E_API_RULE_COMMON_NOT_FOUND.messageTemplate
                    ));
        }
        Resource resource = this.reportService.generateIssueSummaryReport(scanTask, ruleSet, locale, userPrincipal.getUser());
        return this.generateResourceResponse(request, resource);
    }

    /**
     * @param id            scan task id
     * @param ruleSetId     rule set id
     * @param userPrincipal user principal
     * @return Pdf report
     * @throws AppException when report generate error
     */
    @ApiOperation(
            value = "Issue Summary Report",
            nickname = "issueSummaryReport",
            notes = "Issue Summary Report"
    )
    @GetMapping("/issue_summary/scan_task/{id}/rule_set/{ruleSetId}")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Resource> issueSummaryReport(
            HttpServletRequest request,
            @PathVariable UUID id,
            @PathVariable UUID ruleSetId,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[issueSummaryReport] scanTaskId: {}, ruleSetId: {}, principal username: {}",
                id,
                ruleSetId,
                userPrincipal.getUsername()
        );
        ScanTask scanTask = this.scanTaskService.findById(id)
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
        TracerUtil.setTag(this.tracer, TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId());
        this.userService.checkAccessRightOrElseThrow(
                scanTask,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                scanTask.getId()
                        )
                ));
        RuleSet ruleSet = this.ruleService.findRuleSetById(ruleSetId)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_RULE_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] ruleSetId: {}",
                                AppException.ErrorCode.E_API_RULE_COMMON_NOT_FOUND.messageTemplate,
                                ruleSetId
                        )
                ));
        Resource resource = this.reportService.generateIssueSummaryReport(scanTask, ruleSet, locale, userPrincipal.getUser());
        return this.generateResourceResponse(request, resource);
    }

    /**
     * @param format        report format
     * @param userPrincipal user principal
     * @return Pdf report
     * @throws AppException when report generate error
     */
    @ApiOperation(
            value = "Export Issue Report",
            nickname = "exportIssueReport",
            notes = "Export Issue Report"
    )
    @PostMapping("/issue_report/format/{format}/type/{reportType}/delta/{delta}")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Resource> exportIssueReport(
            HttpServletRequest request,
            @PathVariable String format,
            @PathVariable String reportType,
            @PathVariable boolean delta,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody ReportRequest reportRequest,
            @RequestParam(value = "timezoneInMins", required = false, defaultValue = "0") Integer timezoneInMins
    ) throws AppException {
        log.info(
                "[exportIssueReport] scanTaskId: {}, format: {}, principal username: {}",
                reportRequest.getScanTaskId(),
                format,
                userPrincipal.getUsername()
        );
        UUID scanTaskId = reportRequest.getScanTaskId();
        if (EnumUtils.getEnumIgnoreCase(ReportService.Format.class, format) != ReportService.Format.CSV) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_BAD_REQUEST,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    AppException.ErrorCode.E_API_FILE_COMMON_INVALID_FORMAT.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] format: {}, only csv is supported now",
                            AppException.ErrorCode.E_API_FILE_COMMON_INVALID_FORMAT.messageTemplate,
                            format
                    )
            );
        }
        ScanTask scanTask = this.scanTaskService.findById(scanTaskId)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                scanTaskId
                        )
                ));
        TracerUtil.setTag(this.tracer, TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId());
        this.userService.checkAccessRightOrElseThrow(
                scanTask,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                scanTask.getId()
                        )
                ));


        //Prepare projectId if not provided
        if (reportRequest.getProjectId() == null) {
            if (reportRequest.getScanTaskId() != null) {
                reportRequest.setProjectId(scanTask.getProject().getId());
            }
        }

        Project project = this.projectService.findById(reportRequest.getProjectId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                                reportRequest.getProjectId()
                        )
                ));

        this.userService.checkAccessRightOrElseThrow(
                project,
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
                                project.getId()
                        )
                )
        );


        Resource resource = this.reportService.generateIssueCsvReport(scanTask, reportRequest, reportType, delta, timezoneInMins, true, locale);
        if ((resource == null) || !resource.exists()) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_INTERNAL_ERROR,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] scanTaskId: {}, format: {}",
                            AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.messageTemplate,
                            scanTask.getId(),
                            format
                    )
            );
        }
        return this.generateResourceResponse(request, resource);
    }


    /**
     * @param userPrincipal user principal
     * @return Pdf report
     * @throws AppException when report generate error
     */
    @ApiOperation(
            value = "Export CSV Issue Report",
            nickname = "exportCsvIssueReport",
            notes = "Export CSV Issue Report for external users' API call"
    )
    @PostMapping("/issue_report/csv/type/{reportType}")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Resource> exportCsvIssueReport(
            HttpServletRequest request,
            @PathVariable String reportType,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody ExternalCsvReportRequest reportRequest,
            @RequestParam(value = "timezoneInMins", required = false, defaultValue = "0") Integer timezoneInMins
    ) throws AppException {
        log.info(
                "[exportCsvIssueReport] getProjectId: {}, principal username: {}",
                reportRequest.getProjectId(),
                userPrincipal.getUsername()
        );

        if(StringUtils.isBlank(reportRequest.getProjectId())){
            throw new AppException(
                    AppException.LEVEL_WARN,
                    AppException.ERROR_CODE_DATA_NOT_FOUND,
                    HttpURLConnection.HTTP_NOT_FOUND,
                    AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString(
                            "projectId is {}",
                            AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                            reportRequest.getProjectId()
                    )
            );
        }

        Project project = this.projectService.findByProjectId(reportRequest.getProjectId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                                reportRequest.getProjectId()
                        )
                ));

        //
        Optional<ScanTask> scanTaskOptional = null;
        boolean delta = false;
        if(!StringUtils.isBlank(reportRequest.getCommitId())){
            UUID scanTaskId = scanTaskService.getScanTaskIdResponse(project.getId(), reportRequest.getCommitId()).getScanTaskId();
            if(scanTaskId==null){
                new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                reportRequest.getProjectId()
                        )
                );
            }
            scanTaskOptional = scanTaskService.findById(scanTaskId);
            delta = true;
        }else{
            //get latest scan task
            scanTaskOptional = scanTaskService.getLatestCompletedScanTaskByProject(project);
            delta = false;
        }

        ScanTask scanTask = scanTaskOptional.orElseThrow(() -> new AppException(
                AppException.LEVEL_WARN,
                AppException.ERROR_CODE_DATA_NOT_FOUND,
                HttpURLConnection.HTTP_NOT_FOUND,
                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString(
                        "[{}] projectId: {}",
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                        reportRequest.getProjectId()
                )
        ));


        TracerUtil.setTag(this.tracer, TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId());
        this.userService.checkAccessRightOrElseThrow(
                scanTask,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                scanTask.getId()
                        )
                ));

        this.userService.checkAccessRightOrElseThrow(
                project,
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
                                project.getId()
                        )
                )
        );



        Resource resource = this.reportService.generateExternalIssueCsvReport(project, scanTask, reportRequest, reportType, delta, timezoneInMins, locale);
        if ((resource == null) || !resource.exists()) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_INTERNAL_ERROR,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] scanTaskId: {}",
                            AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.messageTemplate,
                            scanTask.getId()
                    )
            );
        }
        return this.generateResourceResponse(request, resource);
    }


    /**
     * @param format        report format
     * @param userPrincipal user principal
     * @return Pdf report
     * @throws AppException when report generate error
     */
    @ApiOperation(
            value = "Export Issue Report",
            nickname = "exportIssueReport",
            notes = "Export Issue Report"
    )
    @GetMapping("/issue_report/format/{format}")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Resource> exportIssueReport(
            HttpServletRequest request,
            @PathVariable String format,

            @RequestParam UUID scanTaskId,
            @RequestParam String reportType,
            @RequestParam boolean delta,
            @CurrentUser UserPrincipal userPrincipal,
            Locale locale,
            @RequestParam(value = "timezoneInMins", required = false, defaultValue = "0") Integer timezoneInMins
    ) throws AppException {
        log.info(
                "[exportIssueReport] scanTaskId: {}, reportType:{}, format: {}, principal username: {}",
                scanTaskId,
                reportType,
                format,
                userPrincipal.getUsername()
        );

        ReportRequest searchIssueGroupRequest = ReportRequest.builder()
                .scanTaskId(scanTaskId)
                .build();

        List<String> ruleSetFilterList = new ArrayList<>();
        if(reportType.equals(VariableUtil.ReportType.SINGLE.name())){
            ruleSetFilterList.add(IssueService.BUILTIN_RULE_SET_CODE);
            ruleSetFilterList.add(IssueService.CERT_RULE_SET_CODE);
        }else if(reportType.equals(VariableUtil.ReportType.CROSS.name())){
            ruleSetFilterList.add(IssueService.BUILTIN_RULE_SET_CODE);
            ruleSetFilterList.add(IssueService.CERT_RULE_SET_CODE);
        }else if(reportType.equals(VariableUtil.ReportType.MISRA.name())){
            ruleSetFilterList.add(IssueService.MISRA_RULE_SET_CODE);
        }
        searchIssueGroupRequest.setRuleSets(ruleSetFilterList);

        if (EnumUtils.getEnumIgnoreCase(ReportService.Format.class, format) != ReportService.Format.CSV) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_BAD_REQUEST,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    AppException.ErrorCode.E_API_FILE_COMMON_INVALID_FORMAT.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] format: {}, only csv is supported now",
                            AppException.ErrorCode.E_API_FILE_COMMON_INVALID_FORMAT.messageTemplate,
                            format
                    )
            );
        }
        ScanTask scanTask = this.scanTaskService.findById(scanTaskId)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                scanTaskId
                        )
                ));
        TracerUtil.setTag(this.tracer, TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId());
        this.userService.checkAccessRightOrElseThrow(
                scanTask,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                scanTask.getId()
                        )
                ));


        //Prepare projectId if not provided
        if (searchIssueGroupRequest.getProjectId() == null) {
            if (searchIssueGroupRequest.getScanTaskId() != null) {
                searchIssueGroupRequest.setProjectId(scanTask.getProject().getId());
            }
        }

        Project project = this.projectService.findById(searchIssueGroupRequest.getProjectId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                                searchIssueGroupRequest.getProjectId()
                        )
                ));

        this.userService.checkAccessRightOrElseThrow(
                project,
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
                                project.getId()
                        )
                )
        );


        Resource resource = this.reportService.generateIssueCsvReport(scanTask, searchIssueGroupRequest, reportType, delta, timezoneInMins, true, locale);
        if ((resource == null) || !resource.exists()) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_INTERNAL_ERROR,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] scanTaskId: {}, format: {}",
                            AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.messageTemplate,
                            scanTask.getId(),
                            format
                    )
            );
        }
        return this.generateResourceResponse(request, resource);
    }

    private ResponseEntity<Resource> generateResourceResponse(HttpServletRequest request, Resource resource) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (StringUtils.isNotBlank(contentType)) {
                mediaType = MediaType.parseMediaType(contentType);
            }
        } catch (IOException ex) {
            log.info("[generateResourceResponse] Cloud not determine file type. filename: {}", resource.getFilename());
        }
        return ResponseEntity.ok().contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", resource.getFilename()))
                .body(resource);
    }


    @ApiOperation(
            value = "get data for pdf report",
            nickname = "getDataForPdfReport",
            notes = "Get Data For Pdf Report"
    )
    @PostMapping("/issue_report/pdf")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<ReportPDFResponse> getPDFReport(
            HttpServletRequest request,
            @RequestBody SearchIssueGroupRequest searchIssueGroupRequest,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[getPDFReport] filter: {}principal username: {}",
                searchIssueGroupRequest,
                userPrincipal.getUsername()
        );

        UUID scanTaskId = searchIssueGroupRequest.getScanTaskId();

        ScanTask scanTask = this.scanTaskService.findById(scanTaskId)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                scanTaskId
                        )
                ));
        TracerUtil.setTag(this.tracer, TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId());
        this.userService.checkAccessRightOrElseThrow(
                scanTask,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                scanTask.getId()
                        )
                ));


        //Prepare projectId if not provided
        if (searchIssueGroupRequest.getProjectId() == null) {
            if (searchIssueGroupRequest.getScanTaskId() != null) {
                searchIssueGroupRequest.setProjectId(scanTask.getProject().getId());
            }
        }

        return ResponseEntity.ok()
                .body(reportService.getPDFReport(searchIssueGroupRequest));
    }
}
