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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.security.TokenProvider;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AsyncScanService {

    @Value("${app.scan.service.url}")
    public String scanServiceUrl;

    @Value("${app.scan.volume.path}")
    public String scanVolumePath;

    @Value("${app.auth.scan-token-expiration-msec}")
    public Long scanTokenExpirationMsec;

    @Value("${app.scan.job-queue-name}")
    public String jobQueueName;

    @NonNull ScanStatusService scanStatusService;
    @NonNull FileStorageService fileStorageService;
    @NonNull UserService userService;
    @NonNull TokenProvider tokenProvider;
    @NonNull ObjectMapper om;
    @NonNull Tracer tracer;

    public Map<String, String> checkAndUpdateScanConfig(ProjectConfig projectConfig, String currentUsername) throws AppException {
        log.debug("[checkAndUpdateScanConfig] projectConfig, id: {}, currentUsername: {}", projectConfig.getId(), currentUsername);
        Map<String, String> scanConfigMap = projectConfig.getAttributes().stream()
                .filter(attribute -> VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN == attribute.getType()).collect(
                        Collectors.toMap(ProjectConfigAttribute::getName, ProjectConfigAttribute::getValue));

        String jobQueueName = scanConfigMap.getOrDefault(VariableUtil.JOB_QUEUE_NAME, this.jobQueueName);
        log.debug("[checkAndUpdateScanConfig] before process, jobQueueName: {}", jobQueueName);
        if(StringUtils.isBlank(jobQueueName)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] jobQueueName must not be blank", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));
        }
        if(!jobQueueName.startsWith(VariableUtil.PUBLIC_QUEUE_PREFIX)) {
            jobQueueName = StringUtils.join(currentUsername, "_", jobQueueName);
        }

        if(!Pattern.compile(VariableUtil.VALID_JOB_QUEUE_NAME_PATTERN).matcher(jobQueueName).matches()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] jobQueueName: {}. " +
                            "Valid characters are the ASCII alphanumerics, '.', '_', and '-'", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, jobQueueName));
        }

        scanConfigMap.put(VariableUtil.JOB_QUEUE_NAME, jobQueueName);
        log.debug("[checkAndUpdateScanConfig] after process, scanConfig: {}, currentUsername: {}", scanConfigMap, currentUsername);
        return scanConfigMap;
    }

    public Map<String, String> constructVcsScanParam(ScanTask scanTask, Map<String, String> scanParams, FileStorage.Type fileStorageType, String vcsToken, String branch,
                                                     String baselineBranch, String commitId, String baselineCommitId, String ref, String username) {
        log.debug("[constructVcsScanParam] scanTask, id: {}, scanParams: {}, fileStorageType: {}, vcsToken: {}, branch: {}, baselineBranch: {}, commitId: {}, baselineCommitId: {}, ref: {}, username: {}",
                scanTask.getId(), scanParams, fileStorageType, vcsToken, branch, baselineBranch, commitId, baselineCommitId, ref, username);

        scanParams.put("sourceCodeAddress", scanTask.getSourceRoot());
        if(StringUtils.isNotBlank(vcsToken)) {
            scanParams.put("vcsToken", vcsToken);
        }
        if(StringUtils.isNotBlank(branch)) {
            scanParams.put("branch", branch);
        }
        if(StringUtils.isNotBlank(baselineBranch)) {
            scanParams.put("baselineBranch", baselineBranch);
        }
        if(StringUtils.isNotBlank(commitId)) {
            scanParams.put("commitId", commitId);
        }
        if(StringUtils.isNotBlank(baselineCommitId)) {
            scanParams.put("baselineCommitId", baselineCommitId);
        }
        if(StringUtils.isNotBlank(ref)) {
            scanParams.put("ref", ref);
        }
        if(StringUtils.isNotBlank(username)) {
            scanParams.put("username", username);
        }
        return scanParams;
    }

    /**
     * @param scanTask scanTask object
     * @param sourceCodePath source code path
     * @param preprocessPath pre_process path
     * @param scanFilePath scan engine work path, for now the value is /share/scan/{scanTaskId}
     * @param fileStorageType file storage type
     * @param fileId the uploaded project's file info id
     * @param uploadSource whether need to upload source code to server
     * @param sourceStorageName the value is agent/gitlab/github/volume_upload
     * @param scanConfig scan config content
     * @param token web service authentication token
     * @param vcsToken version control system personal access token
     * @param branch version control system branch info
     * @param baselineBranch baseline version control system branch info
     * @param commitId commit id
     * @param baselineCommitId baseline commit id
     * @param ref version control system reference
     * @param username username of gerrit temporarily
     * @return an Map type object which contains all the necessary key/values for the scan
     * @throws AppException
     */
    //TODO: consider to extract a object to contains all the parameters.
    public Map<String, String> constructScanParam(ScanTask scanTask, Path sourceCodePath, Path preprocessPath, Path scanFilePath, FileStorage.Type fileStorageType,
                                                  UUID fileId, boolean uploadSource, String sourceStorageName, Map<String, String> scanConfig, String token, String vcsToken, String branch,
                                                  String baselineBranch, String commitId, String baselineCommitId, String ref, String username) throws AppException {
        log.debug("[constructScanParam] scanTask, id: {}, sourceCodePath: {}, preprocessPath: {}, scanFilePath: {}, fileStorageType: {}, fileId: {}, uploadSource: {}, sourceStorageName: {}, scanConfig: {}, token: {}, vcsToken: {}, branch: {}, " +
                        "baselineBranch: {}, commitId: {}, baselineCommitId: {}, ref: {}, username: {}", scanTask.getId(), sourceCodePath, preprocessPath, scanFilePath, fileStorageType, fileId, uploadSource, sourceStorageName, scanConfig, token, vcsToken, branch,
                baselineBranch, commitId, baselineCommitId, ref, username);

        Map<String, String> scanParams = new HashMap<>();

        scanParams.put("projectUUID", scanTask.getProject().getId().toString());
        scanParams.put("projectId", scanTask.getProject().getProjectId());
        scanParams.put("projectConfigId", scanTask.getProjectConfig().getId().toString());
        scanParams.put("scanTaskId", scanTask.getId().toString());

        if ((sourceCodePath != null) && StringUtils.isNotBlank(sourceCodePath.toString())) {
            scanParams.put("sourceCodePath", sourceCodePath.toString());
        }
        if ((preprocessPath == null) || StringUtils.isBlank(preprocessPath.toString())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_PREPROCESSPATH.unifyErrorCode, AppException.ErrorCode.E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_PREPROCESSPATH.messageTemplate);
        }
        scanParams.put("preprocessPath", preprocessPath.toString());
        if ((scanFilePath == null) || StringUtils.isBlank(scanFilePath.toString())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_SCANFILEPATH.unifyErrorCode, AppException.ErrorCode.E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_SCANFILEPATH.messageTemplate);
        }
        scanParams.put("scanFilePath", scanFilePath.toString());
        if (Arrays.asList(FileStorage.Type.GITHUB, FileStorage.Type.GITLAB, FileStorage.Type.GERRIT, FileStorage.Type.GITLAB_V3).contains(fileStorageType)) {
            scanParams = this.constructVcsScanParam(scanTask, scanParams, fileStorageType, vcsToken, branch, baselineBranch, commitId, baselineCommitId, ref, username);
        }
        if (fileId != null) {
            scanParams.put("sourceCodeFileId", fileId.toString());
        }
        if (uploadSource) {
            scanParams.put("uploadSource", Boolean.toString(uploadSource));
        }

        if (StringUtils.isBlank(sourceStorageName)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_SOURCESTORAGENAME.unifyErrorCode, AppException.ErrorCode.E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_SOURCESTORAGENAME.messageTemplate);
        }
        if (StringUtils.isBlank(token)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_TOKEN.unifyErrorCode, AppException.ErrorCode.E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_TOKEN.messageTemplate);
        }
        scanParams.put("sourceStorageName", sourceStorageName);
        scanParams.put("sourceStorageType", fileStorageType.name());
        scanParams.put("token", token);
        if(scanConfig != null) {
            scanParams.putAll(scanConfig);
        }
        return scanParams;
    }

    public Map<String, String> prepareForScan(ScanTask scanTask, ProjectConfig projectConfig, String currentUsername) throws AppException {
        log.debug("[prepareForScan] scanTask, id: {}, projectConfig, id: {}, currentUsername: {}", scanTask.getId(), projectConfig.getId(), currentUsername);

        // get token
        Optional<User> userOptional = this.userService.findByUsernameOrEmail(currentUsername);
        User user = userOptional.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] user: {}", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, currentUsername)));
        String token = tokenProvider.createToken(user.getId().toString(), this.scanTokenExpirationMsec);

        String relativeSourcePath = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH, null);
        if (StringUtils.isBlank(relativeSourcePath)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] invalid relativeSourcePath, projectConfigId: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, projectConfig.getId()));
        }
        String relativeBuildPath = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH, null);
        if (StringUtils.isBlank(relativeBuildPath)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] invalid relativeBuildPath, projectConfigId: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, projectConfig.getId()));
        }

        String sourceStorageName = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME, null);
        if (StringUtils.isBlank(sourceStorageName)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] sourceStorageName should not be blank", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));
        }
        FileStorage fileStorage = this.fileStorageService.findByName(sourceStorageName)
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] sourceStorageName: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, sourceStorageName)));

        Path sourceCodePath = null;
        Path preprocessPath;
        UUID fileId = null;
        boolean uploadSource = false;
        String vcsToken = null;
        String branch = null;
        String baselineBranch = null;
        String commitId = null;
        String baselineCommitId = null;
        String ref = null;
        String username = null;

        // Use /share/scan/{scan_task_id} as scanFilePath for all work mode
        // Agent mode, use relative build path as absolute preprocessPath
        // github/gitlab/gerrit/upload mode, use relative build path as relative preprocessPath
        switch (fileStorage.getFileStorageType()) {
            case AGENT:
                sourceCodePath = new File(relativeSourcePath).toPath();
                preprocessPath = new File(relativeBuildPath).toPath();
                uploadSource = Boolean.valueOf(projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_SOURCE, null));
                break;
            case VOLUME:
                if (fileStorage.getName().equals(VariableUtil.VOLUME_UPLOAD)) {
                    String uploadFileInfoId = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_FILE_INFO_ID, null);
                    if (StringUtils.isBlank(uploadFileInfoId)) {
                        throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                                CommonUtil.formatString("[{}] invalid uploadFileInfoId value, projectConfigId: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, projectConfig.getId()));
                    }
                    fileId = UUID.fromString(uploadFileInfoId);
                    preprocessPath = new File(relativeBuildPath).toPath();
                } else {
                    throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_SCANTASK_PREPAREFORSCAN_ONLY_SUPPORT_UPLOAD.unifyErrorCode,
                            CommonUtil.formatString("[{}] fileStorage: {}", AppException.ErrorCode.E_API_SCANTASK_PREPAREFORSCAN_ONLY_SUPPORT_UPLOAD.messageTemplate, fileStorage));
                }
                break;
            case GITLAB:
            case GITLAB_V3:
            case GITHUB:
            case GERRIT:
                preprocessPath = new File(relativeBuildPath).toPath();
                vcsToken = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.VCS_TOKEN, null);
                branch = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BRANCH, null);
                baselineBranch = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_BRANCH, null);
                commitId = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID, null);
                baselineCommitId = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID, null);
                ref = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.REF, null);
                if(fileStorage.getFileStorageType() == FileStorage.Type.GERRIT) {
                    username = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.USERNAME, null);
                }
                break;
            default:
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_SCANTASK_PREPAREFORSCAN_INVALID_FILESTORAGE_TYPE.unifyErrorCode,
                            CommonUtil.formatString("[{}] fileStorage: {}", AppException.ErrorCode.E_API_SCANTASK_PREPAREFORSCAN_INVALID_FILESTORAGE_TYPE.messageTemplate, fileStorage));
        }

        Path scanFilePath = new File(this.scanVolumePath, scanTask.getId().toString()).toPath();

        Map<String, String> scanConfig = checkAndUpdateScanConfig(projectConfig, currentUsername);
        return constructScanParam(scanTask, sourceCodePath, preprocessPath, scanFilePath, fileStorage.getFileStorageType(), fileId, uploadSource, sourceStorageName, scanConfig, token, vcsToken, branch, baselineBranch, commitId, baselineCommitId, ref, username);
    }

    @Async
    public void prepareAndCallScan(ScanTask scanTask, ProjectConfig projectConfig, String currentUsername) throws AppException {
        log.debug("[prepareAndCallScan] scanTask, id: {}, projectConfig, id: {}, currentUsername: {}", scanTask.getId(), projectConfig.getId(), currentUsername);
        log.debug("[prepareAndCallScan] Execute method asynchronously. Thread is {}", Thread.currentThread().getName());

        try {
            Map<String, String> requestBody = prepareForScan(scanTask, projectConfig, currentUsername);

            RequestBody body;
            try {
                body = RequestBody.create(MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE), om.writeValueAsString(requestBody));
            } catch (JsonProcessingException e) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_SCANTASK_CALLSCAN_CREATEBODY_FAILED.unifyErrorCode,
                        CommonUtil.formatString("[{}] requestBody: {}", AppException.ErrorCode.E_API_SCANTASK_CALLSCAN_CREATEBODY_FAILED.messageTemplate, requestBody),e);
            }
            Request.Builder requestBuilder = new Request.Builder();
            String scanServiceLink = CommonUtil.formatString("http://{}/api/scan_task_service/v2", scanServiceUrl);

            Span span = tracer.buildSpan("[ScanService:startScan]").start();
            try (Scope ignored = tracer.activateSpan(span)) {
                Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CLIENT);
                Tags.HTTP_METHOD.set(tracer.activeSpan(), "POST");
                Tags.HTTP_URL.set(tracer.activeSpan(), scanServiceLink);
                tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS, new RequestBuilderCarrier(requestBuilder));

                Request request = requestBuilder
                        .url(scanServiceLink)
                        .post(body)
                        .build();

                this.scanStatusService.saveScanTaskStatusLog(scanTask, ScanTask.Status.PROCESSING, ScanTaskStatusLog.Stage.PENDING,
                        ScanTaskStatusLog.Status.PROCESSING, 50.0, null, "begin to call scan service", currentUsername);

                OkHttpClient client = new OkHttpClient();
                Response response;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    log.error("[prepareAndCallScan] error message: {}: {}", e.getClass(), e.getMessage());
                    log.error("[prepareAndCallScan] error stack trace info: {}", Arrays.toString(e.getStackTrace()));
                    throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_SCANTASK_CALLSCAN_EXECUTE_FAILED.unifyErrorCode,
                            CommonUtil.formatString("[{}]  request: {}", AppException.ErrorCode.E_API_SCANTASK_CALLSCAN_EXECUTE_FAILED.messageTemplate, request),e);
                }
                if (response.code() == HttpStatus.SC_OK) {
                    log.info("[prepareAndCallScan] call scan service success， init scan task pipeline success and publish to scan task mq");
                    this.scanStatusService.saveScanTaskStatusLog(scanTask, ScanTask.Status.PROCESSING, ScanTaskStatusLog.Stage.SCAN_QUEUE_PRESCAN,
                            ScanTaskStatusLog.Status.PROCESSING, 10.0, null, "init scan task pipeline success and publish it to scan task mq", currentUsername);
                } else {
                    log.error("[prepareAndCallScan] call scan service failed, error message: {}", response.message());
                    this.scanStatusService.saveScanTaskStatusLog(scanTask, ScanTask.Status.FAILED, ScanTaskStatusLog.Stage.SCAN_QUEUE_PRESCAN,
                            ScanTaskStatusLog.Status.FAILED, 100.0,  AppException.ErrorCode.E_API_SCANTASK_CALLSCAN_EXECUTE_FAILED.unifyErrorCode, CommonUtil.formatString("[{}]", AppException.ErrorCode.E_API_SCANTASK_CALLSCAN_EXECUTE_FAILED.messageTemplate), currentUsername);
                }
                response.close();
            } finally {
                span.finish();
            }
        } catch (AppException e) {
            log.error("[prepareAndCallScan] {}: {}", e.getClass(), e.getMessage());
            log.error("[prepareAndCallScan] {}", e.getStackTraceString());
            this.scanStatusService.saveScanTaskStatusLog(scanTask, ScanTask.Status.FAILED, ScanTaskStatusLog.Stage.PENDING,
                    ScanTaskStatusLog.Status.FAILED, 100.0,  AppException.ErrorCode.E_API_SCANTASK_CALLSCAN_EXECUTE_FAILED.unifyErrorCode, e.getMessage(), currentUsername);
        }
    }

    static class RequestBuilderCarrier implements io.opentracing.propagation.TextMap {
        private final Request.Builder builder;

        RequestBuilderCarrier(Request.Builder builder) {
            this.builder = builder;
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            throw new UnsupportedOperationException("carrier is write-only");
        }

        @Override
        public void put(String key, String value) {
            builder.addHeader(key, value);
        }
    }

}
