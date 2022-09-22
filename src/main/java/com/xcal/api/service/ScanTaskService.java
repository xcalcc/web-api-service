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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.dao.ProjectDao;
import com.xcal.api.dao.ScanTaskDao;
import com.xcal.api.entity.*;
import com.xcal.api.entity.v3.IssueGroup;
import com.xcal.api.entity.v3.ScanTaskLog;
import com.xcal.api.exception.AppException;
import com.xcal.api.mapper.ScanTaskSummaryMapper;
import com.xcal.api.metric.ScanMetric;
import com.xcal.api.model.dto.ScanTaskDto;
import com.xcal.api.model.payload.AddScanTaskRequest;
import com.xcal.api.model.payload.UpdateScanTaskRequest;
import com.xcal.api.model.payload.ValidationResult;
import com.xcal.api.model.payload.v3.ScanTaskIdResponse;
import com.xcal.api.repository.*;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.TracerUtil;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScanTaskService {

    static final String GIT_URL = "gitUrl";

    @NonNull ObjectMapper om;
    @NonNull FileStorageRepository fileStorageRepository;
    @NonNull ProjectService projectService;
    @NonNull ProjectConfigRepository projectConfigRepository;
    @NonNull ScanTaskRepository scanTaskRepository;
    @NonNull ScanTaskStatusLogRepository scanTaskStatusLogRepository;
    @NonNull ScanFileRepository scanFileRepository;
    @NonNull FileService fileService;
    @NonNull FileStorageService fileStorageService;
    @NonNull ScanFileService scanFileService;
    @NonNull AsyncScanService asyncScanService;
    @NonNull ScanStatusService scanStatusService;
    @NonNull GitlabService gitlabService;
    @NonNull GithubService githubService;
    @NonNull GerritService gerritService;
    @NonNull LicenseService licenseService;
    @NonNull MeasureService measureService;
    @NonNull PerformanceService performanceService;
    @NonNull NotifyService notifyService;
    @NonNull ScanMetric scanMetric;
    @NonNull Tracer tracer;
    @NonNull ScanTaskSummaryMapper scanTaskSummaryMapper;
    @NonNull ScanTaskDao scanTaskDao;
    @NonNull ProjectDao projectDao;

    public Optional<ScanTask> findById(UUID scanTaskId) {
        log.debug("[findById] scanTaskId: {}", scanTaskId);
        return this.scanTaskRepository.findById(scanTaskId);
    }

    public ScanTask update(ScanTask scanTask) {
        log.debug("[update] scanTask id: {}", scanTask.getId());
        return this.scanTaskRepository.saveAndFlush(scanTask);
    }

    public ScanTask updateScanSummary(ScanTask scanTask, String key, String value) {
        MeasureService.updateScanSummary(scanTask, key, value);
        return this.update(scanTask);
    }

    public ScanTask updateScanTaskSummary(ScanTask scanTask, Map<String, String> summary) {
        MeasureService.updateScanTaskSummary(scanTask, summary);
        return this.update(scanTask);
    }

    public ScanTask updateScanSummaryWithIssueGroup(ScanTask scanTask, List<IssueGroup> issueGroups) throws AppException {
        MeasureService.updateScanSummaryWithIssueGroup(scanTask, issueGroups);
        return this.update(scanTask);
    }

    public ScanTask updateScanSummary(ScanTask scanTask, List<Issue> issues, List<RuleSet> ruleSets) throws AppException {
        MeasureService.updateScanSummary(scanTask, issues, ruleSets);
        return this.update(scanTask);
    }

    public Optional<ScanTask> getPreviousCompletedScanTaskByScanTask(ScanTask scanTask) {
        log.debug("[getPreviousCompletedScanTaskByScanTask] scanTask: {}", scanTask);
        return this.scanTaskRepository.findFirst1ByProjectAndStatusAndModifiedOnLessThan(
                scanTask.getProject(),
                ScanTask.Status.COMPLETED,
                scanTask.getModifiedOn(),
                Sort.by(Sort.Order.desc(ScanTask_.MODIFIED_ON))
        );
    }

    public Optional<ScanTask> getLatestCompletedScanTaskByProject(Project project) {
        log.debug("[getLatestCompletedScanTaskByProject] project: {}", project);
        return this.scanTaskRepository.findFirst1ByProjectAndStatus(project, ScanTask.Status.COMPLETED, Sort.by(Sort.Order.desc(ScanTask_.MODIFIED_ON)));
    }

    public Optional<ScanTask> findPreviousByProjectAndScanTaskAndStatus(UUID projectUUID, UUID scanTaskId, ScanTask.Status status) {
        log.debug("[findPreviousByProjectAndScanTaskAndStatus] projectUUID: {}, scanTaskId: {}, status: {} ", projectUUID, scanTaskId, status);
        return this.scanTaskRepository.findPreviousByProjectAndScanTaskAndStatus(projectUUID, scanTaskId, status);
    }

    public Page<ScanTask> getLatestCompletedScanTaskByProject(Project project, Pageable pageable) {
        log.debug("[getLatestCompletedScanTaskByProject] project: {}", project);
        return this.scanTaskRepository.findByProjectAndStatusIn(project, Collections.singletonList(ScanTask.Status.COMPLETED), pageable);
    }

    public Page<ScanTask> getScanTaskByProjectAndStatus(Project project, List<ScanTask.Status> statusList, Pageable pageable) {
        log.debug("[getScanTaskByProjectAndStatus] project: {}", project);
        return this.scanTaskRepository.findByProjectAndStatusIn(project, statusList, pageable);
    }

    public Optional<ScanTask> getLatestScanTaskByProjectId(UUID id) throws AppException {
        log.debug("[getLatestScanTaskByProjectId] id: {}", id);
        Optional<Project> optionalProject = this.projectService.findById(id);
        Project project = optionalProject.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        return this.scanTaskRepository.findFirst1ByProject(project, Sort.by(Sort.Order.desc(ScanTask_.MODIFIED_ON)));
    }

    public Optional<ScanTask> getLatestScanTask(Project project) {
        log.debug("[getLatestScanTask] project: {}", project);
        return this.scanTaskRepository.findFirst1ByProject(project, Sort.by(Sort.Order.desc(ScanTask_.MODIFIED_ON)));
    }

    public Optional<ScanTask> getLatestRunningScanTask(Project project) {
        log.debug("[getLatestRunningScanTask] project: {}", project);
        Optional<ScanTask> optionalScanTask = this.getLatestScanTask(project);

        ScanTask result = null;
        // Check if there is a scanTask running. If yes, return this scanTask directly.
        if (optionalScanTask.isPresent()) {
            ScanTask scanTask = optionalScanTask.get();
            if (Arrays.asList(ScanTask.Status.PENDING, ScanTask.Status.START, ScanTask.Status.PROCESSING).contains(scanTask.getStatus())) {
                log.debug("[getLatestRunningScanTask] scanTask: {} is running, cannot create new scanTask", scanTask);
                result = scanTask;
            }
        }
        return Optional.ofNullable(result);
    }

    public Optional<ScanTaskStatusLog> getLatestScanTaskStatusLog(ScanTask scanTask) {
        log.debug("[getLatestScanTaskStatusLog] scanTask: {}", scanTask);
        return this.scanTaskStatusLogRepository.findFirst1ByScanTask(scanTask, Sort.by(Sort.Order.desc(ScanTask_.MODIFIED_ON)));
    }

    private String getGitUrl(ProjectConfig projectConfig) throws AppException {
        log.debug("[getGitUrl] projectConfig, id: {}", projectConfig.getId());
        String gitUrl = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL, null);
        if (StringUtils.isBlank(gitUrl)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_GIT_GITURL_NOT_BLANK.unifyErrorCode,
                    CommonUtil.formatString("[{}] projectConfig, id: {}", AppException.ErrorCode.E_API_GIT_GITURL_NOT_BLANK.messageTemplate, projectConfig.getId()));
        }
        return gitUrl;
    }

    public FileStorage getFileStorage(ProjectConfig projectConfig) throws AppException {
        log.debug("[getFileStorage] projectConfig, id: {}", projectConfig.getId());

        String sourceStorageName = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME, null);
        if (StringUtils.isBlank(sourceStorageName)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] sourceStorageName should not be blank", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));
        }
        FileStorage fileStorage = this.fileStorageService.findByName(sourceStorageName)
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] sourceStorageName: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, sourceStorageName)));
        log.trace("[getFileStorage] fileStorage, id: {}, type: {}, name: {}", fileStorage.getId(), fileStorage.getFileStorageType().name(), fileStorage.getName());
        return fileStorage;
    }

    public String getLatestCommitId(ProjectConfig projectConfig, FileStorage fileStorage) throws AppException {
        log.debug("[getLatestCommitId] projectConfig, id: {}, fileStorage, id: {}", projectConfig.getId(), fileStorage.getId());
        String commitId = null;
        String vcsToken;
        String branch;
        String gitUrl;
        log.debug("[getLatestCommitId] fileStorage type is {}", fileStorage.getFileStorageType());
        Map<String, String> attributes = ProjectService.prepareAttributeMapFromProjectConfig(projectConfig);
        switch (fileStorage.getFileStorageType()) {
            case GITLAB:
            case GITLAB_V3:
                gitUrl = getGitUrl(projectConfig);
                vcsToken = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.VCS_TOKEN, null);
                branch = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BRANCH, "master");    // may improve later if problem occurs.
                commitId = gitlabService.getLatestCommitId(fileStorage, gitUrl, branch, vcsToken);
                break;
            case GITHUB:
                gitUrl = getGitUrl(projectConfig);
                vcsToken = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.VCS_TOKEN, null);
                commitId = githubService.getLatestCommitId(gitUrl, vcsToken);
                break;
            case GERRIT:
                commitId = gerritService.getLatestCommitId(fileStorage.getFileStorageHost(), attributes);
                break;
            case GIT:
            default:
                log.error("[getLatestCommitId] should not be here. fileStorage type: {}", fileStorage.getFileStorageType());
        }
        return commitId;
    }

    public String getScanTaskSourceRoot(ProjectConfig projectConfig) throws AppException {
        log.debug("[getScanTaskSourceRoot] projectConfig, id: {}", projectConfig);
        String relativeSourcePath = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH, null);
        if (StringUtils.isBlank(relativeSourcePath)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] relativeSourcePath should not be blank", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));
        }
        FileStorage fileStorage = this.getFileStorage(projectConfig);

        String sourceRoot = null;
        log.debug("[getScanTaskSourceRoot] fileStorage type is {}", fileStorage.getFileStorageType());
        switch (fileStorage.getFileStorageType()) {
            case GIT:
            case SVN:
                break;
            case VOLUME:
                sourceRoot = new File(fileStorage.getFileStorageHost(), relativeSourcePath).toPath().toString();
                break;
            case AGENT:
                sourceRoot = new File(relativeSourcePath).toPath().toString();
                break;
            case GITLAB:
            case GITLAB_V3:
            case GITHUB:
            case GERRIT:
                sourceRoot = getGitUrl(projectConfig);
                break;
            default:
                log.error("[getScanTaskSourceRoot] should not be here");
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.messageTemplate);
        }

        return sourceRoot;
    }

    public void prepareAndCallScan(ScanTask scanTask, ProjectConfig projectConfig, String currentUsername) throws AppException {
        log.debug("[prepareAndCallScan] scanTask, id: {}, projectConfig, id: {}, currentUsername: {}", scanTask.getId(), projectConfig.getId(), currentUsername);

        this.scanStatusService.saveScanTaskStatusLog(scanTask, ScanTaskStatusLog.Stage.PENDING,
                ScanTaskStatusLog.Status.START, 0.0, null,
                "Start process the input from UI", currentUsername);

        this.asyncScanService.prepareAndCallScan(scanTask, projectConfig, currentUsername);
    }

    public ScanTask updateScanTaskStatus(Project project, UpdateScanTaskRequest updateScanTaskRequest, String currentUsername) throws AppException {
        log.debug("[updateScanTaskStatus] project, id: {}, updateScanTaskRequest: {}, currentUsername: {}", project.getId(), updateScanTaskRequest, currentUsername);
        ScanTask scanTask = this.scanTaskRepository.findFirst1ByProject(project, Sort.by(Sort.Order.desc(ScanTask_.MODIFIED_ON)))
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, project.getId())));

        if (!scanTask.getId().equals(updateScanTaskRequest.getId())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_SCANTASK_UPDATE_INCONSISTENT.unifyErrorCode,
                    CommonUtil.formatString("[{}] scanTask id: {}, updateScanTaskRequest id: {}", AppException.ErrorCode.E_API_SCANTASK_UPDATE_INCONSISTENT.messageTemplate, scanTask.getId(), updateScanTaskRequest.getId()));
        }
        return updateScanTaskStatus(updateScanTaskRequest, currentUsername);
    }


    public ScanTask updateScanTaskStatus(UpdateScanTaskRequest updateScanTaskRequest, String currentUsername) throws AppException {
        log.debug("[updateScanTaskStatus] updateScanTaskRequest: {}, currentUsername: {}", updateScanTaskRequest, currentUsername);

        if (EnumUtils.getEnum(ScanTaskStatusLog.Stage.class, updateScanTaskRequest.getStage()) == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_SCANTASKSTATUS_COMMON_INVALID_STAGE.unifyErrorCode,
                    CommonUtil.formatString("[{}] value: {}", AppException.ErrorCode.E_API_SCANTASKSTATUS_COMMON_INVALID_STAGE.messageTemplate, updateScanTaskRequest.getStage()));
        }
        if (!EnumUtils.isValidEnum(ScanTaskStatusLog.Status.class, updateScanTaskRequest.getStatus())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.unifyErrorCode,
                    CommonUtil.formatString("[{}] value: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.messageTemplate, updateScanTaskRequest.getStatus()));
        }

        ScanTask scanTask = findById(updateScanTaskRequest.getId()).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, updateScanTaskRequest.getId())));

        return updateScanTaskStatus(scanTask, EnumUtils.getEnum(ScanTaskStatusLog.Stage.class, updateScanTaskRequest.getStage()),
                EnumUtils.getEnum(ScanTaskStatusLog.Status.class, updateScanTaskRequest.getStatus()),
                updateScanTaskRequest.getPercentage(), updateScanTaskRequest.getUnifyErrorCode(), updateScanTaskRequest.getMessage(), currentUsername);
    }

    public ScanTask updateScanTaskStatus(ScanTask scanTask, ScanTaskStatusLog.Stage stage, ScanTaskStatusLog.Status status, Double percentage, String unifyErrorCode, String message, String currentUsername) throws AppException {
        log.info("[updateScanTaskStatus] scanTask, id: {}, stage: {}, status: {}, percentage: {}, unifyErrorCode: {}, message: {}, currentUsername: {}", scanTask.getId(), stage, status, percentage, unifyErrorCode, message, currentUsername);
        if (Arrays.asList(ScanTask.Status.FAILED, ScanTask.Status.TERMINATED).contains(scanTask.getStatus())) {
            log.warn("[updateScanTaskStatus] ScanTask is ended, ignore this update.  ScanTask id: {}, status: {} ", scanTask.getId(), scanTask.getStatus());

        } else if (Arrays.asList(ScanTask.Status.COMPLETED).contains(scanTask.getStatus())) {
            //TODO: This should not happen and should not do update project summary. Please merge with above finish cases
            log.warn("[updateScanTaskStatus] ScanTask is completed, ignore this update.  ScanTask id: {}", scanTask.getId());
            this.projectService.updateProjectSummary(scanTask.getProject(), scanTask);
        } else {
            List<ScanTaskStatusLog> scanTaskStatusLogs = this.scanTaskStatusLogRepository.findByScanTaskAndStageAndStatusAndPercentage(scanTask, stage, status, percentage);
            if (!scanTaskStatusLogs.isEmpty()) {
                ScanTaskStatusLog scanTaskStatusLog = scanTaskStatusLogs.get(0);
                //insert same log information for same scanTask
                log.info("[updateScanTaskStatus] scanTaskStatusLog already exist, scanTaskId: {}, stage: {}, status: {}, percentage: {}", scanTask.getId(), scanTaskStatusLog.getStage(), scanTaskStatusLog.getStatus(), scanTaskStatusLog.getPercentage());
            }

            this.scanStatusService.saveScanTaskStatusLog(scanTask, stage, status, percentage, unifyErrorCode, message, currentUsername);
            this.updateScanTaskStatus(scanTask, status.toString(), currentUsername);
        }
        return scanTask;
    }

    public ScanTask updateScanTaskStatus(ScanTask scanTask, String status, String currentUsername) throws AppException {
        log.info("[updateScanTaskStatus] scanTask，id: {}, status: {}, currentUsername: {}", scanTask.getId(), status, currentUsername);
        if (EnumUtils.getEnum(ScanTask.Status.class, status) == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.unifyErrorCode,
                    CommonUtil.formatString("[{}] value: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.messageTemplate, status));
        }

        if (Arrays.asList(ScanTask.Status.COMPLETED, ScanTask.Status.FAILED, ScanTask.Status.TERMINATED).contains(scanTask.getStatus())) {
            return scanTask;
        }

        scanTask.setStatus(EnumUtils.getEnum(ScanTask.Status.class, status));
        scanTask.setModifiedBy(currentUsername);
        Date now = new Date();
        scanTask.setModifiedOn(now);
        if (Arrays.asList(ScanTask.Status.COMPLETED.toString(), ScanTask.Status.FAILED.toString(), ScanTask.Status.TERMINATED.toString()).contains(status)) {
            scanTask.setScanEndAt(now);
            TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_END_AT, now.getTime());
            performanceService.collectPerformanceData(scanTask);
            if(Arrays.asList(ScanTask.Status.FAILED.toString(), ScanTask.Status.TERMINATED.toString()).contains(status)) {
                log.info("[updateScanTaskStatus] scan failed/terminated, remove source code file or decompressed source code file directly since they are useless. scanTaskId: {}", scanTask.getId());
                scanFileService.deleteSourceCodeFile(scanTask);
            }
            notifyService.notifyScanResult(scanTask);
        }
        return this.scanTaskRepository.saveAndFlush(scanTask);
    }

    //When scanTask is completed, failed or terminated, return it right now.
    //Otherwise, TODO: tell python web service to stop the scanning.
    //update scanTask status to terminated and add a scanTaskStatusLog info.
    public ScanTask stopScan(UUID id, String currentUsername) throws AppException {
        log.info("[stopScan] id: {}, currentUsername: {}", id, currentUsername);
        Optional<ScanTask> scanTaskOptional = findById(id);

        ScanTask scanTask = scanTaskOptional.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, id)));

        if (Arrays.asList(ScanTask.Status.COMPLETED, ScanTask.Status.FAILED, ScanTask.Status.TERMINATED).contains(scanTask.getStatus())) {
            return scanTask;
        }

        //TODO: send request to python web service to stop the scanning. Python web service only need scanTaskId to stop scanning?
        scanTask = updateScanTaskStatus(scanTask, ScanTask.Status.TERMINATED.toString(), currentUsername);
        Optional<ScanTaskStatusLog> optionalScanTaskStatusLog = this.scanTaskStatusLogRepository.findFirst1ByScanTask(scanTask, Sort.by(Sort.Order.desc(ScanTaskStatusLog_.MODIFIED_ON)));

        ScanTask finalScanTask = scanTask;
        ScanTaskStatusLog scanTaskStatusLog = optionalScanTaskStatusLog.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASKSTATUS_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTask: {}", AppException.ErrorCode.E_API_SCANTASKSTATUS_COMMON_NOT_FOUND.messageTemplate, finalScanTask)));

        this.scanStatusService.saveScanTaskStatusLog(scanTask, scanTaskStatusLog.getStage(),
                ScanTaskStatusLog.Status.TERMINATED, scanTaskStatusLog.getPercentage(), AppException.ErrorCode.E_API_SCANTASK_TERMINATED_BY_USER.unifyErrorCode,
                AppException.ErrorCode.E_API_SCANTASK_TERMINATED_BY_USER.messageTemplate, currentUsername);

        return scanTask;
    }

    /**
     * Update summary using SQL
     *
     * @param scanTask
     */
    public void updateSummary(ScanTask scanTask) {
        scanTaskSummaryMapper.deleteScanTaskSummaryByScanTaskId(scanTask.getId());
        scanTaskSummaryMapper.insertScanTaskSummary(scanTask.getId(), scanTask.getProject().getId());
    }

    public List<ScanFile> findScanFileByScanTask(ScanTask scanTask) {
        log.info("[findScanFileByScanTask] scanTask: {}", scanTask);
        return this.scanFileRepository.findByScanTask(scanTask);
    }

    public List<FileInfo> findFileInfoByScanTask(ScanTask scanTask) {
        log.info("[findFileInfoByScanTask] scanTask: {}", scanTask);
        return this.fileService.findByScanTask(scanTask);
    }

    public List<ScanTask> findByProject(Project project) {
        log.info("[findByProject] project: {}", project);
        return this.scanTaskRepository.findByProject(project);
    }

    public List<ScanTask> findAll() {
        log.info("[findAll]");
        return this.scanTaskRepository.findAll();
    }

    public static ScanTaskDto convertScanTaskToDto(ScanTask scanTask) {
        log.trace("[convertScanTaskToDto] ScanTask: {}", scanTask);
        ScanTaskDto result = ScanTaskDto.builder()
                .id(scanTask.getId())
                .projectUuid(scanTask.getProject().getId())
                .projectId(scanTask.getProject().getProjectId())
                .projectName(scanTask.getProject().getName())
                .projectConfigId(Optional.of(scanTask).map(ScanTask::getProjectConfig).map(ProjectConfig::getId).orElse(null))
                .status(scanTask.getStatus().name())
                .sourceRoot(scanTask.getSourceRoot())
                .createdBy(scanTask.getCreatedBy())
                .createdOn(scanTask.getCreatedOn())
                .modifiedBy(scanTask.getModifiedBy())
                .modifiedOn(scanTask.getModifiedOn())
                .build();
        log.trace("[convertScanTaskToDto] result: {}", result);
        return result;
    }

    /***
     *
     * @param project
     * @param attributes
     * @param startNow True for UI scan. False for else
     * @param currentUsername
     * @return
     * @throws AppException
     */
    public ScanTask addScanTask(Project project, List<AddScanTaskRequest.Attribute> attributes, boolean startNow, String currentUsername) throws AppException {
        log.info("[addScanTask] project, id: {}, attributes size: {}, startNow: {}, currentUsername: {}", project.getId(), attributes.size(), startNow, currentUsername);
        ProjectConfig projectConfig = this.projectService.getLatestActiveProjectConfigByProject(project)
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate, project.getId())));

        String scanType = projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE).map(ProjectConfigAttribute::getValue)
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));

        // When scanning project from web UI, the value of ScanTask.Status is START.
        // When scanning project from GUI, the value of ScanTask.Status is PENDING.
        // Project created by GUI will mark the scanType to offline_agent value. This kind project is not allowed to scan from web UI.
        if (StringUtils.equalsIgnoreCase(scanType, VariableUtil.OFFLINE_AGENT) && startNow) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_SCANTASK_ADDSCAN_INVALID_OPERATION.unifyErrorCode,
                    CommonUtil.formatString("[{}] project whose scanType value is offline_agent is not allowed to scan from web UI", AppException.ErrorCode.E_API_SCANTASK_ADDSCAN_INVALID_OPERATION.messageTemplate));
        }

        // Map the requested attribute to new projectConfigAttributes, If exist, will create a new projectConfig for the scan
        List<ProjectConfigAttribute> projectConfigAttributes = attributes.stream().map(
                attribute -> ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.Type.valueOf(attribute.getType()))
                        .name(attribute.getName())
                        .value(attribute.getValue()).build())
                .collect(Collectors.toList());

        FileStorage fileStorage = this.getFileStorage(projectConfig);
        // if project is Git, add commit id to the project config
        if (Arrays.asList(FileStorage.Type.GITLAB, FileStorage.Type.GITLAB_V3, FileStorage.Type.GITHUB, FileStorage.Type.GERRIT)
                .contains(fileStorage.getFileStorageType())) {
            String commitId = this.getLatestCommitId(projectConfig, fileStorage);
            // if commit id is not in the attributes param
            boolean hasCommitIdInAttributeParam = projectConfigAttributes.stream().anyMatch(attr -> VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue.equals(attr.getName())
                    && VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.type.equals(attr.getType()));
            log.debug("[addScanTask] hasCommitIdInAttributeParam: {}", hasCommitIdInAttributeParam);
            // if the project config have no commit id and param list have no commit id
            if (!hasCommitIdInAttributeParam) {
                projectConfigAttributes.add(ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue)
                        .value(commitId).build());
            }
            // only add BASELINE_COMMIT_ID to the config for gerrit
            if (fileStorage.getFileStorageType() == FileStorage.Type.GERRIT) {
                boolean hasBaselineBranchInAttributeParam = projectConfigAttributes.stream().anyMatch(attr -> VariableUtil.ProjectConfigAttributeTypeName.BASELINE_BRANCH.nameValue.equals(attr.getName())
                        && VariableUtil.ProjectConfigAttributeTypeName.BASELINE_BRANCH.type.equals(attr.getType()));
                log.debug("[addScanTask] hasBaselineBranchInAttributeParam: {}", hasBaselineBranchInAttributeParam);
                // put the commit id from the branch to the as the baselineCommitIds, if baseline commit id is not in the attributes param
                if (hasBaselineBranchInAttributeParam) {
                    projectConfigAttributes.add(ProjectConfigAttribute.builder()
                            .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                            .name(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID.nameValue)
                            .value(commitId).build());
                }
            }
        }
        ScanTask scanTask = this.addScanTask(projectConfig, projectConfigAttributes, currentUsername);
        log.trace("[addScanTask] scanTask: {}", scanTask);
        return scanTask;
    }

    public ScanTask addScanTask(ProjectConfig projectConfig, String currentUsername) throws AppException {
        log.info("[addScanTask] projectConfig, id: {}, currentUsername: {}", projectConfig.getId(), currentUsername);
        return this.addScanTask(projectConfig, new ArrayList<>(), currentUsername);
    }

    public ScanTask addScanTask(ProjectConfig projectConfig, List<ProjectConfigAttribute> attributes, String currentUsername) throws AppException {
        log.info("[addScanTask] projectConfig, id: {}, attributes: {}, currentUsername: {}", projectConfig.getId(), attributes, currentUsername);

        ValidationResult validationResult = licenseService.checkLicense();
        if (validationResult.getStatus() != ValidationResult.Status.SUCCESS) {
            throw validationResult.getException();
        }
        if (!attributes.isEmpty()) {
            ProjectConfig newProjectConfig = this.projectService.cloneProjectConfigWithNewAttribute(projectConfig, attributes, ProjectConfig.Status.ONE_OFF, currentUsername);
            projectConfig = this.projectService.saveProjectConfig(newProjectConfig, currentUsername);
        }

        Date now = new Date();
        ScanTask scanTask = ScanTask.builder().project(projectConfig.getProject())
                .projectConfig(projectConfig)
                .status(ScanTask.Status.PENDING)
                .sourceRoot(getScanTaskSourceRoot(projectConfig))
                .createdBy(currentUsername)
                .createdOn(now)
                .scanStartAt(now)
                .modifiedBy(currentUsername)
                .modifiedOn(now).build();

        ScanTask dbScanTask = this.update(scanTask);
        TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_TASK_ID, dbScanTask.getId().toString());

        this.scanStatusService.saveScanTaskStatusLog(scanTask, ScanTaskStatusLog.Stage.PENDING,
                ScanTaskStatusLog.Status.PENDING, 0.0, null,
                "Scan task added", currentUsername);
        return dbScanTask;
    }

    public Page<ScanTask> searchScanTask(Project project, List<ScanTask.Status> status, List<ProjectConfigAttribute> existAttributes, List<ProjectConfigAttribute> equalAttributes, Pageable pageable) {
        log.info("[searchScanTask] project, id: {}, status: {}, existAttributes size: {}, equalAttributes size: {}, pageable: {}", Optional.ofNullable(project).map(Project::getId).orElse(null), status,
                Optional.ofNullable(existAttributes).map(List::size).orElse(null), Optional.ofNullable(equalAttributes).map(List::size).orElse(null), pageable);
        return this.scanTaskRepository.searchScanTask(project, status, existAttributes, equalAttributes, pageable);
    }

    public int updateScanTaskStatus(String fromStatus, String toStatus) {
        return scanTaskRepository.updateStatus(fromStatus, toStatus);
    }

    /***
     * Search for scan task log.
     * If the project is a dsr project, show return all dsr scan(with baseline commit id and commit id)
     * and the one before first dsr happen.
     * If the project is not a dsr project, return only the last completed scan.
     * @param projectId
     * @param targetRangeStartDate
     * @param targetRangeEndDate
     * @param commitIdPattern
     * @param pageable
     * @return
     */
    public Page<ScanTaskLog> searchScanTaskLog(UUID projectId, Date targetRangeStartDate, Date targetRangeEndDate, String commitIdPattern, List<String> ruleSets, List<String> repoActions,  Pageable pageable) {
        log.info("[searchScanTask] project, projectId: {}, targetDate:{}, targetRangeStartDate:{}, targetRangeEndDate:{}, commitIdPattern:{},  pageable: {}", projectId, pageable);

        Project project = projectDao.getProject(projectId);
        Boolean isDsrProject = project.getNeedDsr() == null ? false : project.getNeedDsr();
        return scanTaskDao.searchScanTaskLog(projectId, targetRangeStartDate, targetRangeEndDate, commitIdPattern, isDsrProject, ruleSets, repoActions, pageable);
    }


    public UUID getExtraScanTaskIdByProjectIdAndDsr(UUID projectId, Boolean isDsrProject) {
        UUID extraScanTaskId = null;
        if (isDsrProject) {
            //the completed scan task before first baseline
            Optional<com.xcal.api.entity.v3.ScanTask> firstScanTaskWithBaseline = scanTaskDao.getFirstScanTaskWithBaseline(projectId);
            if (firstScanTaskWithBaseline.isPresent()) {
                System.out.println("present");
                Optional<com.xcal.api.entity.v3.ScanTask> firstBaselineScanTask = scanTaskDao.getLastScanTaskByScanTask(projectId, firstScanTaskWithBaseline.get().getId(), "COMPLETED");
                if (firstBaselineScanTask.isPresent()) {
                    System.out.println(firstBaselineScanTask.get().getId());
                    extraScanTaskId = firstBaselineScanTask.get().getId();
                }
            }else{
                System.out.println("Not present");
            }
        } else {
            //get last completed scan task
            Optional<com.xcal.api.entity.v3.ScanTask> lastCompletedScanTask = scanTaskDao.getLastScanTaskByProjectId(projectId, "COMPLETED");
            if (lastCompletedScanTask.isPresent()) {
                extraScanTaskId = lastCompletedScanTask.get().getId();
            }
        }
        return extraScanTaskId;
    }

    public ScanTaskIdResponse getScanTaskIdResponse(
            UUID projectId,
            String commitId
    ) {
        log.info("[getScanTaskIdResponse] project, projectId: {}, commitId: {}", projectId, commitId);
        return scanTaskDao.getScanTaskIdResponse(projectId, commitId);
    }
}
