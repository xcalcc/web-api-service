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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.google.common.collect.ImmutableMap;
import com.xcal.api.config.AppProperties;
import com.xcal.api.dao.*;
import com.xcal.api.entity.*;
import com.xcal.api.entity.v3.IssueFile;
import com.xcal.api.entity.v3.IssueGroup;
import com.xcal.api.entity.v3.IssueGroupSrcSinkFilePath;
import com.xcal.api.entity.v3.Trace;
import com.xcal.api.exception.AppException;
import com.xcal.api.exception.BusinessException;
import com.xcal.api.exception.FormatException;
import com.xcal.api.model.CompareIssueObject;
import com.xcal.api.model.dto.IssueDiffDto;
import com.xcal.api.model.dto.IssueDto;
import com.xcal.api.model.dto.v3.RuleInfoDto;
import com.xcal.api.model.dto.v3.RuleListResponseDto;
import com.xcal.api.model.payload.*;
import com.xcal.api.model.payload.v3.ScanTaskIdResponse;
import com.xcal.api.repository.IssueDiffRepository;
import com.xcal.api.repository.IssueRepository;
import com.xcal.api.repository.IssueTraceRepository;
import com.xcal.api.repository.ProjectRepository;
import com.xcal.api.service.v3.ProjectServiceV3;
import com.xcal.api.service.v3.RuleServiceV3;
import com.xcal.api.util.*;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IssueService {

    public static final String BUILTIN_RULE_SET_CODE = "X";
    public static final String CERT_RULE_SET_CODE = "S";
    public static final String MISRA_RULE_SET_CODE = "M";
    public static final String MANDATORY_LEVEL = "M";
    public static final String REQUIRED_LEVEL = "R";
    public static final String ADVISORY_LEVEL = "A";
    @NonNull RuleService ruleService;
    @NonNull UserService userService;
    @NonNull ScanTaskService scanTaskService;
    @NonNull ProjectService projectService;
    @NonNull EmailService emailService;
    @NonNull SettingService settingService;
    @NonNull AppProperties appProperties;
    @NonNull I18nService i18nService;


    @NonNull IssueRepository issueRepository;
    @NonNull IssueTraceRepository issueTraceRepository;
    @NonNull IssueDiffRepository issueDiffRepository;

    @NonNull ProjectRepository projectRepository;

    @NonNull ScanFileService scanFileService;
    @NonNull FileService fileService;
    @NonNull AsyncJobService asyncJobService;
    @NonNull RuleServiceV3 ruleServiceV3;

    @NonNull Tracer tracer;

    @NonNull IssueFileDao issueFileDao;
    @NonNull IssueStringDao issueStringDao;
    @NonNull IssueGroupDao issueGroupDao;
    @NonNull IssueDao issueMapperDao;
    @NonNull ScanTaskDao scanTaskDao;
    @NonNull ObjectMapper om;
    private final ObjectMapper cborOm = new ObjectMapper(new CBORFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @NonNull KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    public Integer saveBatchSize;

    @Value("${scan.archive-result}")
    boolean archiveScanResult;

    @Value("${enable-csv-scan-result}")
    boolean enableCsvImport;

    @Value("${app.scan.volume.path}")
    public String scanVolumePath;

    @Value("${recovery-policy.path}")
    public String recoveryPolicyPath;

    @Value("${execution.mode}")
    public String executionMode;


    public static String SEARCH_VAL_DELIMITER;
    @Value("${search-value-delimiter}")
    public void setSearchValDelimiter(String searchValDelimiter){
        IssueService.SEARCH_VAL_DELIMITER=searchValDelimiter;
    }
    private static final String RULE_DELIMITER = ";";
    private static final String FILE_DELIMITER = ";";

    public Set<String> convertRuleWhiteListToSet(String rulesString) {
        Set<String> ruleWhiteList = new HashSet<>();
        String[] rules = rulesString.split(RULE_DELIMITER);
        for (String rule : rules) {
            ruleWhiteList.add(rule.trim());
        }
        return ruleWhiteList;
    }


    private static final String ISSUE_ASSIGN_TEMPLATE_PREFIX = "assign-issues-email";

    //TODO:Raymond, remove after DB refactor phase 2
    private static boolean useCSVOnly = false;

    /***
     * subscribe to Kafka and update status
     * @param jsonString
     */
    @Transactional(rollbackFor = Exception.class)
    @KafkaListener(groupId = "scan-task-group", topics = "proc-done")
    public void asyncUpdateProcStatus(String jsonString) {
        log.info("[asyncUpdateProcStatus] start, jsonString: {}", jsonString);
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String scanTaskIdString = jsonObject.getString("scanTaskId");
            String status = jsonObject.getString("status");

            if (!status.equals(BusinessException.STATUS.SUCC.name())) {
                log.debug("[asyncUpdateProcStatus] message is non SUCC status, continue to process");
                UUID scanTaskId = UUID.fromString(scanTaskIdString);

                String updateStatus = BusinessException.mapServiceStatusToScanTaskStatus(status);

                UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder()
                        .id(scanTaskId)
                        .stage(ScanTaskStatusLog.Stage.SCANNING.name())
                        .status(updateStatus)
                        .unifyErrorCode("")
                        .message("")
                        .percentage(100.0)
                        .build();
                scanTaskService.updateScanTaskStatus(updateScanTaskRequest, "System");

            } else {
                log.debug("[asyncUpdateProcStatus] message is SUCC status, will be skipped");
            }

        } catch (Exception e) {
            log.error("[asyncUpdateProcStatus] error while updating status from PROC", e);

        } finally {
            log.info("[asyncUpdateProcStatus] end");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @KafkaListener(groupId = "scan-task-group", topics = "scan-task-topic")
    public void asyncImportIssueToScanTask(String jobId) throws IOException, AppException {

        log.info("[asyncImportIssueToScanTask] jobId: {}", jobId);
        Span rootSpan = tracer.buildSpan("asyncImportIssueToScanTask-KafkaListener").withTag("jobId", jobId).start();

        Optional<AsyncJob> asyncJobOptional = this.asyncJobService.findAsyncJobById(UUID.fromString(jobId));
        if (!asyncJobOptional.isPresent()) {
            log.warn("[asyncImportIssueToScanTask] AsyncJob does not exist, jobId: {}", jobId);
            rootSpan.log("[asyncImportIssueToScanTask] AsyncJob does not exist");
            rootSpan.finish();
            return;
        }

        AsyncJob asyncJob = asyncJobOptional.get();
        ScanTask scanTask = null;
        try {
            if (asyncJob.getStatus() == AsyncJob.Status.RUNNING) {
                log.info("[asyncImportIssueToScanTask] AsyncJob is in RUNNING state already, jobId: {}", jobId);
                rootSpan.log("[asyncImportIssueToScanTask] AsyncJob is in RUNNING state already");
                rootSpan.finish();
                return;
            }

            if (asyncJob.getStatus() == AsyncJob.Status.COMPLETED) {
                log.info("[asyncImportIssueToScanTask] AsyncJob is in COMPLETED state already, jobId: {}", jobId);
                rootSpan.log("[asyncImportIssueToScanTask] AsyncJob is in COMPLETED state already");
                rootSpan.finish();
                return;
            }

            asyncJob.setStatus(AsyncJob.Status.RUNNING);
            this.asyncJobService.updateAsyncJob(asyncJob);

            AsyncJob.IssueJobInfo info = this.om.readValue(asyncJob.getInfo(), AsyncJob.IssueJobInfo.class);
            log.info("[asyncImportIssueToScanTask] jobInfo: {}", info);

            Optional<ScanTask> scanTaskOptional = this.scanTaskService.findById(UUID.fromString(info.getScanTask()));
            if (!scanTaskOptional.isPresent()) {
                log.error("[asyncImportIssueToScanTask] invalid scan task");
                rootSpan.log("[asyncImportIssueToScanTask] invalid scan task");
                rootSpan.finish();
                asyncJob.setResult("invalid scan task");
                asyncJob.setStatus(AsyncJob.Status.FAILED);
                this.asyncJobService.updateAsyncJob(asyncJob);
                return;
            }

            scanTask = scanTaskOptional.get();
            String username = info.getUsername();
            Integer step = info.getStep();
            rootSpan.setTag("scanTask", scanTask.getId().toString());
            rootSpan.setTag("step", step);

            //Validation
            if (scanTask == null) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate);
            }

            Project project = scanTask.getProject();
            if (project == null) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate);
            }

            ProjectConfig projectConfig
                    = scanTask.getProjectConfig();
            if (projectConfig == null) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate);
            }

            List<ProjectConfigAttribute> projectConfigAttributeList = projectConfig.getAttributes();
            if (projectConfigAttributeList == null) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJ_CONFIG_ATTR_CMD_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_PROJ_CONFIG_ATTR_CMD_NOT_FOUND.messageTemplate);
            }

            if (step == 0) {
                log.info("[asyncImportIssueToScanTask] Start step 0");
                if (info.getIssueFile().isEmpty()) {
                    log.error("[asyncImportIssueToScanTask] invalid issue file path");
                    rootSpan.log("[asyncImportIssueToScanTask] invalid issue file path");
                    rootSpan.finish();
                    asyncJob.setResult("invalid issue file path");
                    asyncJob.setStatus(AsyncJob.Status.FAILED);
                    this.asyncJobService.updateAsyncJob(asyncJob);
                    return;
                }

                //TODO: Raymond, Remove after testing
                String issueFile = info.getIssueFile();
                rootSpan.setTag("issueFile", issueFile);

                if (useCSVOnly && !issueFile.endsWith(CsfReader.FILE_EXTENSION)) { //for skipping original json result
                    log.debug("not csv, skip ");
                    rootSpan.log("[asyncImportIssueToScanTask] not csv, skip");
                    rootSpan.finish();
                    asyncJob.setResult("CSF only mode. Skipped");
                    asyncJob.setStatus(AsyncJob.Status.COMPLETED);
                    this.asyncJobService.updateAsyncJob(asyncJob);
                    return;
                } else if (issueFile.endsWith(CsfReader.FILE_EXTENSION)) {
                    log.debug("CSV: " + issueFile + " " + scanTask.getId());
                    if (!enableCsvImport) {
                        log.warn("This is under development!");
                        rootSpan.log("[asyncImportIssueToScanTask] This is under development!");
                        rootSpan.finish();
                        asyncJob.setResult("CSF is under development");
                        asyncJob.setStatus(AsyncJob.Status.FAILED);
                        this.asyncJobService.updateAsyncJob(asyncJob);
                        return;
                    }
                } else {
                    log.debug("Other format: " + issueFile + " " + scanTask.getId());
                }

                File inputFile = new File(info.getIssueFile());
                if (!inputFile.exists()) {
                    log.error("[asyncImportIssueToScanTask] issue file not exists");
                    rootSpan.log("[asyncImportIssueToScanTask] issue file not exists");
                    rootSpan.finish();
                    asyncJob.setResult("issue file not exists");
                    asyncJob.setStatus(AsyncJob.Status.FAILED);
                    this.asyncJobService.updateAsyncJob(asyncJob);
                    return;
                }

                if (issueFile.endsWith(CsfReader.FILE_EXTENSION)) {
                    //Handle csf file
                    int retryForRecoverable = 0;
                    int retryForUnexpectedIssue = 0;
                    int retryInterval = BusinessException.DEFAULT_RETRY_INTERVAL_MS;
                    try {
                        JSONObject policyJson = PolicyLoader.loadPolicy(recoveryPolicyPath);
                        JSONObject importPolicyJson = policyJson.getJSONObject("import");
                        JSONObject EA_RETRY = importPolicyJson.getJSONObject("EA_RETRY");
                        retryForRecoverable = EA_RETRY.getInt("retryForRecoverable");
                        retryForUnexpectedIssue = EA_RETRY.getInt("retryForUnexpectedIssue");
                        retryInterval = EA_RETRY.getInt("retryInterval");
                        log.debug("POSTPROC_sync_import: retry policy retryForRecoverable:" + retryForRecoverable + " retryForUnexpectedIssue:" + retryForUnexpectedIssue);
                    } catch (IOException | JSONException e) {
                        log.error("POSTPROC_sync_import: error while loading recovery policy:" + e.getMessage(), e);
                    }

                    while (retryForRecoverable >= 0 && retryForUnexpectedIssue >= 0) {
                        try {
                            //TODO: Raymond, get file info id from kafka
                            importCsf(rootSpan, scanTask, project, projectConfigAttributeList, inputFile, null, username);
                            break;
                        } catch (PersistenceException e) {
                            //Recoverable Exception
                            log.warn("POSTPROC_sync_import: error while importCsf: retryForRecoverable:" + retryForRecoverable, e);
                            if (retryForRecoverable <= 0) { //reach retry limit
                                log.error("POSTPROC_sync_import: reached retry limit. retryForRecoverable:" + retryForRecoverable, e);
                                throw e;
                            }
                            retryForRecoverable--;
                            try {
                                Thread.sleep(retryInterval);
                            } catch (InterruptedException interruptedException) {
                                log.error("POSTPROC_sync_import: interruptedException:", interruptedException);
                            }
                        } catch (Exception e) {
                            //Unexpected Exception
                            log.warn("POSTPROC_sync_import: error while importCsf: retryForUnexpectedIssue:" + retryForUnexpectedIssue, e);
                            if (retryForUnexpectedIssue <= 0) { //reach retry limit
                                log.error("POSTPROC_sync_import: reached retry limit. retryForUnexpectedIssue:" + retryForUnexpectedIssue, e);
                                throw e;
                            }
                            retryForUnexpectedIssue--;
                            try {
                                Thread.sleep(retryInterval);
                            } catch (InterruptedException interruptedException) {
                                log.error("POSTPROC_sync_import: interruptedException:", interruptedException);
                            }
                        }
                    }


                } else {
                    //process json
                    log.info("[asyncImportIssueToScanTask] Other format used");

                    //load result from file to object
                    ImportScanResultRequest importScanResultRequest = this.processScanResultFile(inputFile, scanTask, username);

                    //import issue to database
                    List<Issue> issues = this.importIssueToScanTask(scanTask, importScanResultRequest, username);

                    //prepare ruleset information and update summary
                    List<RuleSet> ruleSets = new ArrayList<>();
                    for (ImportScanResultRequest.RuleSet ruleSet : importScanResultRequest.getRuleSets()) {
                        this.ruleService.getRuleSetByNameAndVersion(ruleSet.getRuleSet(), ruleSet.getRuleSetVersion()).ifPresent(ruleSets::add);
                    }


                    log.info("[asyncImportIssueToScanTask] import succeed, id: {}, issues: {}", scanTask.getId(), issues.size());
                    this.scanTaskService.updateScanSummary(scanTask, issues, ruleSets);

                    //archive and follow up
                    if (this.archiveScanResult) {
                        ImportIssueResponse importIssueResponse = this.constructImportIssueResponse(scanTask, issues);
                        FileInfo fileInfo = this.saveImportIssueResponseToFile(importIssueResponse, username);
                        this.scanTaskService.updateScanSummary(scanTask, "fileInfo.id.scanResult", String.valueOf(fileInfo.getId()));
                    }
                }


                asyncJob.setResult("import issue to scan task succeed");
                asyncJob.setStatus(AsyncJob.Status.COMPLETED);
                this.asyncJobService.updateAsyncJob(asyncJob);


                log.info("[asyncImportIssueToScanTask] End step 0");
            }


            if (step == 1) {
                log.info("[asyncImportIssueToScanTask] Start step 1");
                Optional<ScanTask> baselineScanTaskOptional = this.scanTaskService.findById(UUID.fromString(info.getBaselineScanTask()));
                if (!baselineScanTaskOptional.isPresent()) {
                    asyncJob.setResult("invalid baseline scan task");
                    asyncJob.setStatus(AsyncJob.Status.FAILED);
                    this.asyncJobService.updateAsyncJob(asyncJob);
                    return;
                }

                File inputFixedIssueFile = null;
                File inputNewIssueFile = null;

                if (!info.getFixedIssueFile().isEmpty()) {
                    inputFixedIssueFile = new File(info.getFixedIssueFile());
                    if (!inputFixedIssueFile.exists()) {
                        asyncJob.setResult("fixed issue file not exists");
                        asyncJob.setStatus(AsyncJob.Status.FAILED);
                        this.asyncJobService.updateAsyncJob(asyncJob);
                        return;
                    }
                }

                if (!info.getNewIssueFile().isEmpty()) {
                    inputNewIssueFile = new File(info.getNewIssueFile());
                    if (!inputNewIssueFile.exists()) {
                        asyncJob.setResult("new issue file not exists");
                        asyncJob.setStatus(AsyncJob.Status.FAILED);
                        this.asyncJobService.updateAsyncJob(asyncJob);
                        return;
                    }
                }

                ImportIssueDiffRequest importIssueDiffRequest = this.processIssueDiffFile(inputFixedIssueFile, inputNewIssueFile, baselineScanTaskOptional.get(), scanTask, username);
                this.importIssueDiff(importIssueDiffRequest, username);

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

                asyncJob.setResult("import issue diff to scan task succeed");
                asyncJob.setStatus(AsyncJob.Status.COMPLETED);
                this.asyncJobService.updateAsyncJob(asyncJob);

                log.info("[asyncImportIssueToScanTask] End step 1");
            }

            if (info.getUpdateScanTaskRequest() != null) {
                TracerUtil.setTag(tracer, TracerUtil.Tag.SCAN_TASK_ID, scanTask.getId());
                scanTaskService.updateScanTaskStatus(info.getUpdateScanTaskRequest(), username);
                TracerUtil.setTags(tracer, ImmutableMap.<TracerUtil.Tag, Object>builder()
                        .put(TracerUtil.Tag.PROJECT_ID, project.getId())
                        .put(TracerUtil.Tag.SCAN_TASK_STATUS, scanTask.getStatus())
                        .build());
                asyncJob.setResult("update scan task status succeed");
                asyncJob.setStatus(AsyncJob.Status.COMPLETED);
                this.asyncJobService.updateAsyncJob(asyncJob);
            }

            JSONObject postProcDoneKafkaMessage = generateKafkaJsonMessage(scanTask, BusinessException.STATUS.SUCC.name());

            asyncJob.setResult("import issue to scan task succeed");
            asyncJob.setStatus(AsyncJob.Status.COMPLETED);
            this.asyncJobService.updateAsyncJob(asyncJob);
            log.debug("[importCsf] Start sending message to kafka");
            this.kafkaTemplate.send("postproc-done", postProcDoneKafkaMessage.toString());
            log.debug("[importCsf] End sending message to kafka");

        } catch (IOException e) {
            log.error("[asyncImportIssueToScanTask] IOException:" + e.getMessage(), e);
            rootSpan.log(String.format("[asyncImportIssueToScanTask] exception: %s", e.getMessage()));
            rootSpan.finish();
            asyncJob.setResult("IOException");
            asyncJob.setStatus(AsyncJob.Status.FAILED);
            this.asyncJobService.updateAsyncJob(asyncJob);

            if (scanTask != null) {
                try {
                    scanTaskService.updateScanTaskStatus(scanTask, ScanTaskStatusLog.Stage.PENDING, ScanTaskStatusLog.Status.FAILED, 100.0,
                            AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.messageTemplate, scanTask.getModifiedBy());
                } catch (AppException appException) {
                    log.error("[handleAsyncPrepareAndCallScan] unexpected exception: {}", appException.getStackTraceString());
                }

                JSONObject postProcDoneKafkaMessage = generateKafkaJsonMessage(scanTask, BusinessException.STATUS.FAILED.name());

                asyncJob.setResult("import issue diff to scan task succeed");
                asyncJob.setStatus(AsyncJob.Status.FAILED);
                this.asyncJobService.updateAsyncJob(asyncJob);
                log.debug("[importCsf] Start sending message to kafka");
                this.kafkaTemplate.send("postproc-done", postProcDoneKafkaMessage.toString());
                log.debug("[importCsf] End sending message to kafka");
            }
            throw e;
        } catch (Exception e) {
            log.error("[asyncImportIssueToScanTask] Unexpected exception", e);
            rootSpan.log("[asyncImportIssueToScanTask] Unexpected exception: " + e.getMessage());
            asyncJob.setResult(e.getLocalizedMessage());
            asyncJob.setStatus(AsyncJob.Status.FAILED);
            this.asyncJobService.updateAsyncJob(asyncJob);

            if (scanTask != null) {
                try {
                    scanTaskService.updateScanTaskStatus(scanTask, ScanTaskStatusLog.Stage.PENDING, ScanTaskStatusLog.Status.FAILED, 100.0,
                            AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.messageTemplate, scanTask.getModifiedBy());
                } catch (AppException appException) {
                    log.error("[handleAsyncPrepareAndCallScan] unexpected exception: {}", appException.getStackTraceString());
                }

                JSONObject postProcDoneKafkaMessage = generateKafkaJsonMessage(scanTask, BusinessException.STATUS.FAILED.name());

                asyncJob.setResult("import issue file with error: " + e.getMessage());
                asyncJob.setStatus(AsyncJob.Status.FAILED);
                this.asyncJobService.updateAsyncJob(asyncJob);
                log.debug("[importCsf] Start sending message to kafka");
                this.kafkaTemplate.send("postproc-done", postProcDoneKafkaMessage.toString());
                log.debug("[importCsf] End sending message to kafka");
            }
            throw e;
        } finally {
            if (rootSpan != null) {
                rootSpan.finish();
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void importCsf(Span rootSpan, ScanTask scanTask, Project project, List<ProjectConfigAttribute> projectConfigAttributeList, File inputFile, UUID fileInfoId, String username) throws IOException, AppException {

        log.debug("[importCsf] Start, projectId:{}, scanTask:{}", project.getId(), scanTask.getId());
        rootSpan.log("[importCsf] Csv scanTasklogFile used, scanTaskId:");

        log.debug("[importCsf] Start suggest to perform GC");
        System.gc();
        log.debug("[importCsf] End suggest to perform GC");

        boolean haveDsr = ProjectServiceV3.isScanTaskDsr(scanTask);
        boolean needDsr = project.getNeedDsr() != null ? project.getNeedDsr() : false;
        if (!haveDsr && needDsr) {
            //If the project expect dsr but no dsr.
            log.error("[importCsf] Project need dsr, but not dsr. projectId:{}, currentScanTask:{}", project.getId(), scanTask.getId());
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_SCANTASK_WITHOUT_DSR.unifyErrorCode,
                    AppException.ErrorCode.E_API_SCANTASK_WITHOUT_DSR.messageTemplate);
        }


        log.info("[importCsf] Open and import prepare issue for import to database");
        rootSpan.log("[importCsf] Open and import prepare issue for import to database");

        try (RandomAccessFile issueRandomAccessFile = new RandomAccessFile(inputFile, "r")) {

            CsfReader csfReader = CsfReader.getInstance(CsfReader.getVersion(issueRandomAccessFile));
            Date currentDate = new Date();

            // Validate Magic
            String supportedMagic = csfReader.getSupportedMagic();
            String fileMagic = csfReader.getMagic(issueRandomAccessFile);
            if (!supportedMagic.equals(fileMagic)) {
                log.error("[importCsf] Invalid Csf, magic not matched, supportedMagic: {} , fileMagic: {}", supportedMagic, fileMagic);
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_FORMAT.unifyErrorCode,
                        AppException.ErrorCode.E_API_FILE_COMMON_INVALID_FORMAT.messageTemplate);
            }

            // Validate Version
            String supportedVersion = csfReader.getSupportedVersion();
            String fileVersion = csfReader.getVersion(issueRandomAccessFile);
            if (!supportedVersion.equals(fileVersion)) {
                log.warn("[importCsf] Unsupported csf version, supportedVersion: {} , fileVersion: {}", supportedVersion, fileVersion);
            }

            long issueKeyTableStartOffset = csfReader.getIssueKeyTableStartOffset(issueRandomAccessFile);

            //get project config for getting file blacklist and rule whitelist
            Optional<ProjectConfig> projectConfig = projectService.getLatestActiveProjectConfigByProject(scanTask.getProject());

            //prepare file blacklist
            Set<Integer> fileBlacklistSet = new HashSet<>();
            String fileBlacklistString = "";
            String[] fileBlacklist = null;
            if (projectConfig.isPresent()) {
                fileBlacklistString = projectConfig.get().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.FILE_BLACK_LIST, "");
                fileBlacklist = Arrays.stream(fileBlacklistString.split(FILE_DELIMITER)).filter(s -> s.trim().length() > 0).toArray(String[]::new);
            }


            //Insert File path
            long filePathTableStartOffset = csfReader.getFilePathTableStartOffset(issueRandomAccessFile);
            long filePathTableEndOffset = csfReader.getFilePathTableEndOffset(issueRandomAccessFile);
            Span insertIssueFileSpan = this.tracer.buildSpan("asyncImportIssueFileToScanTask")
                    .asChildOf(rootSpan)
                    .withTag("filePathTableStartOffset", filePathTableStartOffset)
                    .withTag("filePathTableEndOffset", filePathTableEndOffset)
                    .start();
            insertIssueFile(scanTask, csfReader, issueRandomAccessFile, filePathTableStartOffset, filePathTableEndOffset, fileBlacklist, fileBlacklistSet/*output*/);
            insertIssueFileSpan.finish();

            //Insert Issue String
            long stringTableStartOffset = csfReader.getStringTableStartOffset(issueRandomAccessFile);
            long stringTableEndOffset = csfReader.getStringTableEndOffset(issueRandomAccessFile);
            Span insertIssueStringSpan = this.tracer.buildSpan("asyncImportIssueStringToScanTask")
                    .asChildOf(rootSpan)
                    .withTag("stringTableStartOffset", stringTableStartOffset)
                    .withTag("stringTableEndOffset", stringTableEndOffset)
                    .start();
            insertIssueString(scanTask, csfReader, issueRandomAccessFile, stringTableStartOffset, stringTableEndOffset);
            insertIssueStringSpan.finish();


            //get whitelist from scan config and convert to set
            String whitelistString = "";
            if (projectConfig.isPresent()) {
                whitelistString = projectConfig.get().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.RULE_WHITE_LIST, "");
            }
            Set<String> ruleCodeWhitelistSet = convertRuleWhiteListToSet(whitelistString);
            Map<String, String> misraComplianceMap = new HashMap<>(); // <csv_code, compliance>
            //get rule info from rule service and add rule(csv code) in whitelist to set
            RuleListResponseDto ruleListResponseDto = this.ruleServiceV3.getAllRuleInfo(Locale.ENGLISH);
            Set<String> csvCodeWhiteListSet = new HashSet<>();
            List<RuleInfoDto> ruleInfos = ruleListResponseDto.getRuleInfoDtoList();
            if (ruleInfos != null) {

                //get white list
                for (RuleInfoDto ruleInfo : ruleInfos) {
                    if (ruleInfo.getRuleCode() == null) { // no rule code
                        continue;
                    }
                    if (!ruleCodeWhitelistSet.contains(ruleInfo.getRuleCode())) { // not in whitelist
                        continue;
                    }
                    if (ruleInfo.getCodes() != null) { // in whitelist, add csv code
                        for (String csvCode : ruleInfo.getCodes()) {
                            csvCodeWhiteListSet.add(csvCode);
                        }
                    }
                }

                //get for misra compliance map
                log.debug("[importCsf] ruleInfos.size()", ruleInfos.size());
                for (RuleInfoDto ruleInfo : ruleInfos) {
                    if (ruleInfo.getRuleSet() == null || ruleInfo.getRuleSet().getId() == null) {
                        continue;
                    }

                    if (ruleInfo.getCodes() == null) {
                        continue;
                    }

                    if (ruleInfo.getCompliance() == null) {
                        continue;
                    }

                    if (ruleInfo.getRuleSet().getId().equals(MISRA_RULE_SET_CODE)) {
                        ruleInfo.getCodes().forEach(csvCode -> misraComplianceMap.put(csvCode, ruleInfo.getCompliance()));
                    }

                }

            } else {
                log.warn("[importCsf] Error while getting ruleList from rule service. ruleInfos is null");
            }

            // Insert New Issue Group
            log.info("[importCsf] Insert New Issue Group");
            long issueGroupTableStartOffset = csfReader.getNewIssueGroupTableStartOffset(issueRandomAccessFile);
            long issueGroupTableEndOffset = csfReader.getNewIssueGroupTableEndOffset(issueRandomAccessFile);
            Span insertIssueGroupSpan = this.tracer.buildSpan("asyncImportIssueGroupToScanTask")
                    .asChildOf(rootSpan)
                    .withTag("issueGroupTableStartOffset", issueGroupTableStartOffset)
                    .withTag("issueGroupTableEndOffset", issueGroupTableEndOffset)
                    .start();
            insertIssueGroup(scanTask, csfReader, issueRandomAccessFile, issueGroupTableStartOffset, issueGroupTableEndOffset, issueKeyTableStartOffset, currentDate, csvCodeWhiteListSet, fileBlacklistSet, misraComplianceMap);
            insertIssueGroupSpan.finish();


            // Insert Changed Issue Group
            log.info("[importCsf] Insert changed Issue Group");
            long partiallyChangedIssueGroupTableStartOffset = csfReader.getPartiallyChangedIssueGroupTableStartOffset(issueRandomAccessFile);
            long partiallyChangedIssueGroupTableEndOffset = csfReader.getPartiallyChangedIssueGroupTableEndOffset(issueRandomAccessFile);
            Span partiallyChangedInsertIssueGroupSpan = this.tracer.buildSpan("asyncImportIssueGroupToScanTask")
                    .asChildOf(rootSpan)
                    .withTag("partiallyChangedIssueGroupTableStartOffset", partiallyChangedIssueGroupTableStartOffset)
                    .withTag("partiallyChangedIssueGroupTableEndOffset", partiallyChangedIssueGroupTableEndOffset)
                    .start();
            insertIssueGroup(scanTask, csfReader, issueRandomAccessFile, partiallyChangedIssueGroupTableStartOffset, partiallyChangedIssueGroupTableEndOffset, issueKeyTableStartOffset, currentDate, csvCodeWhiteListSet, fileBlacklistSet, misraComplianceMap);
            partiallyChangedInsertIssueGroupSpan.finish();

            // Insert Existing Issue Group
            log.info("[importCsf] Insert Existing Issue Group");
            long existingIssueGroupTableStartOffset = csfReader.getExistingIssueGroupTableStartOffset(issueRandomAccessFile);
            long existingIssueGroupTableEndOffset = csfReader.getExistingIssueGroupTableEndOffset(issueRandomAccessFile);
            Span existingInsertIssueGroupSpan = this.tracer.buildSpan("asyncImportExistingIssueGroupToScanTask")
                    .asChildOf(rootSpan)
                    .withTag("existingIssueGroupTableStartOffset", existingIssueGroupTableStartOffset)
                    .withTag("existingIssueGroupTableEndOffset", existingIssueGroupTableEndOffset)
                    .start();
            insertIssueGroup(scanTask, csfReader, issueRandomAccessFile, existingIssueGroupTableStartOffset, existingIssueGroupTableEndOffset, issueKeyTableStartOffset, currentDate, csvCodeWhiteListSet, fileBlacklistSet, misraComplianceMap);
            existingInsertIssueGroupSpan.finish();

            // clone Fixed Issue Group
            log.info("[importCsf] Insert Fixed Issue Group");
            long fixedIssueGroupStartOffset = csfReader.getFixedIssueGroupTableStartOffset(issueRandomAccessFile);
            long fixedIssueGroupEndOffset = csfReader.getFixedIssueGroupTableEndOffset(issueRandomAccessFile);
            Span insertFixedIssueGroupSpan = this.tracer.buildSpan("asyncImportIssueGroupToScanTask")
                    .asChildOf(rootSpan)
                    .withTag("issueGroupTableStartOffset", partiallyChangedIssueGroupTableStartOffset)
                    .withTag("issueGroupTableEndOffset", partiallyChangedIssueGroupTableEndOffset)
                    .start();
            insertFixedIssueGroup(scanTask, csfReader, issueRandomAccessFile, fixedIssueGroupStartOffset, fixedIssueGroupEndOffset, issueKeyTableStartOffset, currentDate);
            insertFixedIssueGroupSpan.finish();


            // Insert N and L Issue
            log.info("[importCsf] Insert New and Changed Issue");
            long issueTableStartOffset = csfReader.getIssueTableStartOffset(issueRandomAccessFile);
            long issueTableEndOffset = csfReader.getIssueTableEndOffset(issueRandomAccessFile);
            Span insertIssueSpan = this.tracer.buildSpan("asyncImportIssueToScanTask")
                    .asChildOf(rootSpan)
                    .withTag("issueTableStartOffset", issueTableStartOffset)
                    .withTag("issueTableEndOffset", issueTableEndOffset)
                    .start();
            insertIssue(scanTask, csfReader, issueRandomAccessFile, issueTableStartOffset, issueTableEndOffset);
            insertIssueSpan.finish();

            // Insert E Issue
            log.info("[importCsf] Insert Existing Issue");
            long existingIssueTableStartOffset = csfReader.getExistingIssueTableStartOffset(issueRandomAccessFile);
            long existingIssueTableEndOffset = csfReader.getExistingIssueTableEndOffset(issueRandomAccessFile);
            Span insertExistingIssueSpan = this.tracer.buildSpan("asyncImportExistingIssueToScanTask")
                    .asChildOf(rootSpan)
                    .withTag("existingIssueTableStartOffset", existingIssueTableStartOffset)
                    .withTag("existingIssueTableEndOffset", existingIssueTableEndOffset)
                    .start();
            insertIssue(scanTask, csfReader, issueRandomAccessFile, existingIssueTableStartOffset, existingIssueTableEndOffset);
            insertExistingIssueSpan.finish();

        }

        // assign issues to users
        ProjectConfig projectConfig = scanTask.getProjectConfig();
        String baselineCommitId = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID, null);
        ScanTaskIdResponse baselineScanTaskIdResponse= scanTaskDao.getScanTaskIdResponse(project.getId(), baselineCommitId); //get baseline scan task
        if(baselineScanTaskIdResponse!=null) {
            issueGroupDao.assignAllFromBaseline(scanTask.getId(), baselineScanTaskIdResponse.getScanTaskId());
        }

        try {//Optional action to import file info
            File fileInfoFile = null;
            if (fileInfoId != null) {
                Resource resource = this.fileService.getFileAsResource(fileInfoId);
                fileInfoFile = resource.getFile();
            } else {
                log.warn("[importCsf] fileInfoId is null");
                // TODO: will use new file service later
                fileInfoFile = new File(scanVolumePath + File.separator + scanTask.getId() + File.separator + VariableUtil.FILE_INFO_NAME);
            }
            if (fileInfoFile != null && fileInfoFile.exists()) {
                log.debug("[importCsf] Start importing file info: " + fileInfoFile.getName());
                this.scanFileService.importFileInfoToScanTask(scanTask, fileInfoFile, username);
                log.debug("[importCsf] End importing file info: " + fileInfoFile.getName());
            } else {
                log.warn("[importCsf] file info file does not exist: " + fileInfoFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("[importCsf] error occur while importing file info",e);
        }


        // Make summary during issue
        log.debug("[importCsf] Prepare summary");
        Span updateScanSummarySpan = this.tracer.buildSpan("updateScanSummary")
                .asChildOf(rootSpan)
                .start();
        this.scanTaskService.updateSummary(scanTask);
        updateScanSummarySpan.finish();

        Span updateScanTaskStatus = this.tracer.buildSpan("updateScanTaskStatus")
                .asChildOf(rootSpan)
                .start();
        scanTaskService.updateScanTaskStatus(scanTask, ScanTaskStatusLog.Stage.SCAN_COMPLETE,
                ScanTaskStatusLog.Status.COMPLETED, 100.0, null, "upload scanTasklogFile info and scan result successful", username);
        updateScanTaskStatus.finish();

        Span updateProjectSummarySpan = this.tracer.buildSpan("updateProjectSummary")
                .asChildOf(rootSpan)
                .start();
        this.projectService.updateProjectSummary(project.getId());
        updateProjectSummarySpan.finish();


        // Update project
        if (haveDsr == true) {
            project.setNeedDsr(true);
        }

        // Update cicd state
        projectService.updateProjectCicdState(scanTask, VariableUtil.ProjectConfigAttributeTypeName.NEXT_STATE_ON_SUCCESS);

        // Update baseline commit id
        projectService.setProjectBaselineOnCD(scanTask, project);

        projectRepository.save(project);
        projectRepository.flush();

        log.debug("[importCsf] Start suggest to perform GC");
        System.gc();
        log.debug("[importCsf] End suggest to perform GC");

    }



    private void deleteAllIssueGroup(Span rootSpan, Project project) {
        UUID projectId = project.getId();
        log.info("[importCsf] delete all issue group under the project:{}", projectId);
        Span insertIssueGroupSpan = this.tracer.buildSpan("deleteIssueGroupByProjectId")
                .withTag("projectId", projectId.toString())
                .asChildOf(rootSpan)
                .start();
        issueGroupDao.deleteIssueGroupByProjectId(projectId);
        insertIssueGroupSpan.finish();
    }

    public JSONObject generateKafkaJsonMessage(ScanTask scanTask, String status) {
        JSONObject postProcDoneKafkaMessage = new JSONObject();
        postProcDoneKafkaMessage.put("scanTaskId", scanTask.getId().toString());
        postProcDoneKafkaMessage.put("status", status);
        postProcDoneKafkaMessage.put("dateTime", (new Date().getTime()));
        postProcDoneKafkaMessage.put("source", "POSTPROC");
        return postProcDoneKafkaMessage;
    }


    private void insertIssue(ScanTask scanTask, CsfReader csfReader, RandomAccessFile issueRandomAccessFile, long issueTableStartOffset, long issueTableEndOffset) throws IOException {
        //Block to insert issue
        log.info("[insertIssue] Start reading issue one by one offset: {} - {} ", issueTableStartOffset, issueTableEndOffset);
        List<com.xcal.api.entity.v3.Issue> insertIssuePaths = new ArrayList();

        issueRandomAccessFile.seek(issueTableStartOffset);
        BufferedReader brRafReader = new BufferedReader(new FileReader(issueRandomAccessFile.getFD()));
        String issueLine = null;

        long realPosition = issueTableStartOffset;

        int csfCount = 0;
        int databaseCount = 0;

        while ((issueLine = brRafReader.readLine()) != null && realPosition < issueTableEndOffset
        ) {
            realPosition += issueLine.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
            String[] issueCols = csfReader.getColsFromLine(issueLine);

            JSONArray tracePath = new JSONArray();

            //Add node in issue
            try {
                tracePath = csfReader.getTracePathJSONArray(issueCols, tracePath);
            } catch (FormatException e) {
                log.warn("[insertIssue] Error getting trace path for issue at offset: {} , reason: {}", realPosition, e.getMessage(), e);
            }

            com.xcal.api.entity.v3.Issue issuePath = com.xcal.api.entity.v3.Issue.builder()
                    .scanTaskId(scanTask.getId())
                    .issueGroupId(csfReader.getIssuePathIssueGroupUniqueId(issueCols))
                    .traceCount(Integer.valueOf(csfReader.getIssuePathNumNode(issueCols)))
                    .tracePath(tracePath.toString())
                    .certainty(csfReader.getIssuePathCertainty(issueCols))
                    .status(null)
                    .dsr(null)
                    .build();

            insertIssuePaths.add(issuePath);
            csfCount++;

            if (insertIssuePaths.size() >= saveBatchSize) {
                databaseCount += batchInsertList(insertIssuePaths);
            }

        }

//        }//end while

        if (!insertIssuePaths.isEmpty()) {
            databaseCount += batchInsertList(insertIssuePaths);
        }

        log.info("[insertIssue] End inserting to database. count in csf:{} , count inserted to DB:{}", csfCount, databaseCount);
    }

    /**
     * This method is for inserting issue Group for New status
     *
     * @param scanTask
     * @param csfReader
     * @param issueRandomAccessFile
     * @param issueGroupTableStartOffset
     * @param issueGroupTableEndOffset
     * @param issueKeyTableStartOffset
     * @throws IOException
     */

    public void insertIssueGroup(ScanTask scanTask, CsfReader csfReader, RandomAccessFile issueRandomAccessFile, long issueGroupTableStartOffset, long issueGroupTableEndOffset, long issueKeyTableStartOffset, Date currentTime, Set<String> csvCodeWhiteListSet, Set<Integer> fileIdBlacklistSet, Map<String, String> misraComplianceMap) throws IOException {
        //block to insert issue group
        log.info("[insertIssueGroup] Start reading issue group one by one offset: {} - {} ", issueGroupTableStartOffset, issueGroupTableEndOffset);
        List<IssueGroup> issueGroupList = new ArrayList();
        Set<String> issueGroupIdSet = new HashSet<>();

        UUID projectUUID = scanTask.getProject().getId();
        issueRandomAccessFile.seek(issueGroupTableStartOffset);
        BufferedReader brRafReader = new BufferedReader(
                new FileReader(issueRandomAccessFile.getFD()));
        String issueGroupLine = null;
        long realPosition = issueGroupTableStartOffset;

        int csfCount = 0;
        int databaseCount = 0;

        while ((issueGroupLine = brRafReader.readLine()) != null && realPosition < issueGroupTableEndOffset) {
            realPosition += issueGroupLine.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;

            String[] issueGroupCols = csfReader.getColsFromLine(issueGroupLine);

            String csvCode = csfReader.getIssueGroupRuleName(issueGroupCols);
            String issueGroupId = csfReader.getIssueGroupUniqueId(issueGroupCols);
            if (!csvCodeWhiteListSet.isEmpty() && !csvCodeWhiteListSet.contains(csvCode)) {
                log.warn("[insertIssueGroup] Skipped as issue group: {} rule code is not in whitelist: {}", issueGroupId, csvCode);
                continue;
            }

            int srcFilePathOffset = Integer.parseInt(csfReader.getIssueGroupSourceFileNameOffset(issueGroupCols));
            if (!fileIdBlacklistSet.isEmpty() && fileIdBlacklistSet.contains(srcFilePathOffset)) {
                log.warn("[insertIssueGroup] Skipped as issue group: {} src file path id: {} is in file blacklist", issueGroupId, srcFilePathOffset);
                continue;
            }

            int sinkFilePathOffset = Integer.parseInt(csfReader.getIssueGroupSinkFileNameOffset(issueGroupCols));
            if (!fileIdBlacklistSet.isEmpty() && fileIdBlacklistSet.contains(sinkFilePathOffset)) {
                log.warn("[insertIssueGroup] Skipped as issue group: {} sink file path id: {} is in file blacklist", issueGroupId, sinkFilePathOffset);
                continue;
            }

            int criticality = 0;
            if (misraComplianceMap.containsKey(csvCode)) {
                String compliance = misraComplianceMap.get(csvCode);
                if (compliance == null) {
                    log.warn("[insertIssueGroup] compliance is null, default value be 1", compliance);
                    criticality = 1;
                } else if (compliance.equals(MANDATORY_LEVEL)) {
                    criticality = 9;
                } else if (compliance.equals(REQUIRED_LEVEL)) {
                    criticality = 6;
                } else if (compliance.equals(ADVISORY_LEVEL)) {
                    criticality = 3;
                } else {
                    log.warn("[insertIssueGroup] compliance not found: {}, default value be 1", compliance);
                    criticality = 1;
                }

            } else {
                criticality = csfReader.getIssueGroupCriticality(issueGroupCols);
            }

            IssueGroup issueGroup = IssueGroup.builder()
                    .projectId(projectUUID)
                    .id(csfReader.getIssueGroupUniqueId(issueGroupCols))
                    .severity(csfReader.getIssueGroupServerityString(issueGroupCols))
                    .scanTaskId(scanTask.getId())
                    .occurScanTaskId(scanTask.getId())

                    //source
                    .srcFilePathId(srcFilePathOffset)
                    .srcLineNo(Integer.parseInt(csfReader.getIssueGroupSourceLineNumber(issueGroupCols)))
                    .srcColumnNo(Integer.parseInt(csfReader.getIssueGroupSourceColNum(issueGroupCols)))
                    .srcMessageId(Integer.parseInt(csfReader.getIssueGroupSourceMessage(issueGroupCols)))
                    //sink
                    .sinkFilePathId(sinkFilePathOffset)
                    .sinkLineNo(Integer.parseInt(csfReader.getIssueGroupSinkLineNumber(issueGroupCols)))
                    .sinkColumnNo(Integer.parseInt(csfReader.getIssueGroupSinkColNum(issueGroupCols)))
                    .sinkMessageId(Integer.parseInt(csfReader.getIssueGroupSinkMessage(issueGroupCols)))

                    .functionNameId(Integer.parseInt(csfReader.getIssueGroupFunctionNameOffset(issueGroupCols)))
                    .variableNameId(Integer.parseInt(csfReader.getIssueGroupVariableNameOffset(issueGroupCols)))
                    .ruleSet(csfReader.getIssueGroupRuleSet(issueGroupCols))
                    .ruleCode(csvCode)
                    .complexity(Integer.valueOf(csfReader.getIssueGroupAccComplexityString(issueGroupCols)))
                    .likelihood(csfReader.getIssueGroupLikelihoodString(issueGroupCols))
                    .remediationCost(csfReader.getIssueGroupCostString(issueGroupCols))
                    .certainty(csfReader.getIssueGroupCertainty(issueGroupCols))
                    .criticality(criticality)
                    .category(csfReader.getIssueGroupDftCatName(issueGroupCols))
                    .issueCount(Integer.valueOf(csfReader.getIssueGroupNumDft(issueGroupCols)))
                    .avgTraceCount(csfReader.getIssueGroupAvgNoNode(issueGroupCols))
                    .dsr(csfReader.getIssueGroupStatus(issueGroupCols))

                    .occurTime(currentTime)
                    .status("ACTIVE")
                    .build();

            csfCount++;
            if(!issueGroupIdSet.contains(issueGroup.getId())) {
                issueGroupIdSet.add(issueGroup.getId());
                issueGroupList.add(issueGroup);
            } else {
                log.warn("[insertIssueGroup] csf file contains multiple issues with same issueGroup id, to avoid inserting db failure, only insert the first appear issueGroup with same issueGroup id. " +
                        "ignored issueGroup: {}", issueGroup);
            }

            if (issueGroupList.size() >= saveBatchSize) {
                //save and reset
                databaseCount += batchInsertIssueGroupList(issueGroupList);
                issueGroupList.clear();
            }

        }//end while loop


        //insert the remaining issue group
        if (!issueGroupList.isEmpty()) {
            databaseCount += batchInsertIssueGroupList(issueGroupList);
            issueGroupList.clear();
        }

        log.info("[insertIssueGroup] End inserting to database. count in csf:{} , count inserted to DB:{}", csfCount, databaseCount);
    }


    public void updatePIssueGroupAndRemoveIssue(ScanTask scanTask, CsfReader csfReader, RandomAccessFile issueRandomAccessFile, long issueGroupTableStartOffset, long issueGroupTableEndOffset, long issueKeyTableStartOffset, Date currentTime) throws IOException {
        //block to insert issue group
        log.info("[updatePIssueGroupAndRemoveIssue] Start reading issue group one by one offset: {} - {} ", issueGroupTableStartOffset, issueGroupTableEndOffset);

        List<IssueGroup> updateIssueGroupList = new ArrayList();

        UUID projectUUID = scanTask.getProject().getId();
        issueRandomAccessFile.seek(issueGroupTableStartOffset);
        BufferedReader brRafReader = new BufferedReader(
                new FileReader(issueRandomAccessFile.getFD()));
        String issueGroupLine = null;
        long realPosition = issueGroupTableStartOffset;

        int csfCount = 0;
        int databaseCount = 0;

        while ((issueGroupLine = brRafReader.readLine()) != null && realPosition < issueGroupTableEndOffset) {
            realPosition += issueGroupLine.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;

            String[] issueGroupCols = csfReader.getColsFromLine(issueGroupLine);

            IssueGroup issueGroup = IssueGroup.builder()
                    .projectId(projectUUID)
                    .id(csfReader.getIssueGroupUniqueId(issueGroupCols))
                    .severity(csfReader.getIssueGroupServerityString(issueGroupCols))
                    .scanTaskId(scanTask.getId())
                    .occurScanTaskId(scanTask.getId())
                    //source
                    .srcFilePathId(Integer.parseInt(csfReader.getIssueGroupSourceFileNameOffset(issueGroupCols)))
                    .srcLineNo(Integer.parseInt(csfReader.getIssueGroupSourceLineNumber(issueGroupCols)))
                    .srcColumnNo(Integer.parseInt(csfReader.getIssueGroupSourceColNum(issueGroupCols)))
                    .srcMessageId(Integer.parseInt(csfReader.getIssueGroupSourceMessage(issueGroupCols)))
                    //sink
                    .sinkFilePathId(Integer.parseInt(csfReader.getIssueGroupSinkFileNameOffset(issueGroupCols)))
                    .sinkLineNo(Integer.parseInt(csfReader.getIssueGroupSinkLineNumber(issueGroupCols)))
                    .sinkColumnNo(Integer.parseInt(csfReader.getIssueGroupSinkColNum(issueGroupCols)))
                    .sinkMessageId(Integer.parseInt(csfReader.getIssueGroupSinkMessage(issueGroupCols)))

                    .functionNameId(Integer.parseInt(csfReader.getIssueGroupFunctionNameOffset(issueGroupCols)))
                    .variableNameId(Integer.parseInt(csfReader.getIssueGroupVariableNameOffset(issueGroupCols)))
                    .ruleSet(csfReader.getIssueGroupRuleSet(issueGroupCols))
                    .ruleCode(csfReader.getIssueGroupRuleName(issueGroupCols))
                    .complexity(Integer.valueOf(csfReader.getIssueGroupAccComplexityString(issueGroupCols)))
                    .criticality(csfReader.getIssueGroupCriticality(issueGroupCols))
                    .category(csfReader.getIssueGroupDftCatName(issueGroupCols))
                    .likelihood(csfReader.getIssueGroupLikelihoodString(issueGroupCols))
                    .remediationCost(csfReader.getIssueGroupCostString(issueGroupCols))
                    .certainty(csfReader.getIssueGroupCertainty(issueGroupCols))
                    .issueCount(Integer.valueOf(csfReader.getIssueGroupNumDft(issueGroupCols)))
                    .avgTraceCount(csfReader.getIssueGroupAvgNoNode(issueGroupCols))
                    .dsr(csfReader.getIssueGroupStatus(issueGroupCols))
                    .occurTime(currentTime)
                    .status("ACTIVE")
                    .build();

            updateIssueGroupList.add(issueGroup);
            csfCount++;

            if (updateIssueGroupList.size() >= saveBatchSize) {
                databaseCount += batchUpdateIssueGroupList(scanTask.getProject().getId(), updateIssueGroupList);
                updateIssueGroupList.clear();
            }

        }//end while loop

        //update the remaining issue group
        if (!updateIssueGroupList.isEmpty()) {
            databaseCount += batchUpdateIssueGroupList(scanTask.getProject().getId(), updateIssueGroupList);
            updateIssueGroupList.clear();
        }


        log.info("[updatePIssueGroupAndRemoveIssue] End updating to database. count in csf:{} , count updated to DB:{}", csfCount, databaseCount);
    }


    public void insertFixedIssueGroup(ScanTask scanTask, CsfReader csfReader, RandomAccessFile issueRandomAccessFile, long fixedIssueGroupStartOffset, long fixedIssueGroupEndOffset, long issueKeyTableStartOffset, Date currentDate) throws IOException, AppException {
        //block to insert issue group
        log.info("[insertFixedIssueGroup] Start reading issue group one by one offset: {} - {} ", fixedIssueGroupStartOffset, fixedIssueGroupEndOffset);
        List<IssueGroup> updateIssueGroupList = new ArrayList();
        Set<String> issueGroupIdSet = new HashSet<>();

        issueRandomAccessFile.seek(fixedIssueGroupStartOffset);
        BufferedReader brRafReader = new BufferedReader(
                new FileReader(issueRandomAccessFile.getFD()));
        String fixedUniqueId = null;
        long realPosition = fixedIssueGroupStartOffset;

        int csfCount = 0;
        int databaseCount = 0;

        while ((fixedUniqueId = brRafReader.readLine()) != null && realPosition < fixedIssueGroupEndOffset) {
            realPosition += fixedUniqueId.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;

            String finalFixedUniqueId = fixedUniqueId;
            IssueGroup occurIssueGroup = this.issueGroupDao.getIssueGroupByIdAndDsrType(scanTask.getProject().getId(), fixedUniqueId, Arrays.asList("E", "P", "N"))
                    .orElseThrow(() -> new AppException(
                            AppException.LEVEL_ERROR,
                            AppException.ERROR_CODE_DATA_NOT_FOUND,
                            HttpURLConnection.HTTP_NOT_FOUND,
                            AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                            CommonUtil.formatString(
                                    "[{}] projectId: {}, issueGroupId: {}",
                                    AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate,
                                    scanTask.getProject().getId(), finalFixedUniqueId
                            )
                    ));
            log.debug("[insertFixedIssueGroup] first find the existing or new issue group whose info will be used to fill into fixed issue group info: {}, scanTaskId: {}", occurIssueGroup.getId(), occurIssueGroup.getScanTaskId());

            //Add fixed issue group
            //Since fixed issue group has no issue detail, so use existing/new issue detail to fill it
            IssueGroup issueGroup = IssueGroup.builder()
                    .id(fixedUniqueId)
                    .projectId(scanTask.getProject().getId())
                    .scanTaskId(scanTask.getId())
                    .occurScanTaskId(occurIssueGroup.getScanTaskId())
                    .occurTime(occurIssueGroup.getOccurTime())
                    .fixedScanTaskId(scanTask.getId())
                    .fixedTime(currentDate)
                    .ruleSet(occurIssueGroup.getRuleSet())
                    .ruleCode(occurIssueGroup.getRuleCode())
                    .complexity(occurIssueGroup.getComplexity())
                    .criticality(occurIssueGroup.getCriticality())
                    .category(occurIssueGroup.getCategory())
                    .likelihood(occurIssueGroup.getLikelihood())
                    .remediationCost(occurIssueGroup.getRemediationCost())
                    .certainty(occurIssueGroup.getCertainty())
                    .issueCount(occurIssueGroup.getIssueCount())
                    .avgTraceCount(occurIssueGroup.getAvgTraceCount())
                    .status(occurIssueGroup.getStatus())
                    .dsr("F")
                    .assigneeId(occurIssueGroup.getAssigneeId())
                    .assigneeDisplayName(occurIssueGroup.getAssigneeDisplayName())
                    .assigneeEmail(occurIssueGroup.getAssigneeEmail())
                    .build();

            csfCount++;
            if(!issueGroupIdSet.contains(issueGroup.getId())) {
                issueGroupIdSet.add(issueGroup.getId());
                updateIssueGroupList.add(issueGroup);
            } else {
                log.warn("[insertFixedIssueGroup] csf file contains multiple issues with same issueGroup id, to avoid inserting db failure, only insert the first appear issueGroup with same issueGroup id. " +
                        "ignored issueGroup: {}", issueGroup);
            }

            if (updateIssueGroupList.size() >= saveBatchSize) {
                //save and reset
                databaseCount += batchInsertIssueGroupList(updateIssueGroupList);
                updateIssueGroupList.clear();
            }

        }//end while loop

        //insert the remaining issue group
        if (!updateIssueGroupList.isEmpty()) {
            databaseCount += batchInsertIssueGroupList(updateIssueGroupList);
            updateIssueGroupList.clear();
        }

        log.info("[insertFixedIssueGroup] End inserting to database. count in csf:{} , count updated to DB:{}", csfCount, databaseCount);
    }


    public void insertIssueString(ScanTask scanTask, CsfReader csfReader, RandomAccessFile issueRandomAccessFile, long stringTableStartOffset, long stringTableEndOffset) throws IOException {
        //block to insert issue string
        log.info("[insertIssueString] Start reading issue string one by one offset: {} - {} ", stringTableStartOffset, stringTableEndOffset);
        List<com.xcal.api.entity.v3.IssueString> issueStringInsertList = new ArrayList();

        long beginningOffset = 2;
        issueRandomAccessFile.seek(stringTableStartOffset + beginningOffset);

        BufferedReader brRafReader = new BufferedReader(
                new FileReader(issueRandomAccessFile.getFD()));
        String line = null;

        long realPosition = stringTableStartOffset + beginningOffset;

        int lastOffset = (int) beginningOffset;

        int csfCount = 0;
        int databaseCount = 0;

        while ((line = brRafReader.readLine()) != null && realPosition < stringTableEndOffset
        ) {
            realPosition += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;

            int localOffset = (int) (realPosition - stringTableStartOffset);
            ScanTaskStringIdV3 id = ScanTaskStringIdV3.builder().scanTaskId(scanTask.getId()).id(localOffset).build();

            com.xcal.api.entity.v3.IssueString issueFileV3 = com.xcal.api.entity.v3.IssueString.builder()
                    .scanTaskId(scanTask.getId())
                    .id(lastOffset)
                    .str(line)
                    .build();

            issueStringInsertList.add(issueFileV3);
            csfCount++;

            if (issueStringInsertList.size() >= saveBatchSize) {
                databaseCount += batchInsertAndResetIssueStringList(issueStringInsertList);

            }

            lastOffset = localOffset;
        }//end while


        if (!issueStringInsertList.isEmpty()) {
            databaseCount += batchInsertAndResetIssueStringList(issueStringInsertList);
        }

        log.info("[insertIssueString] End inserting to database. count in csf:{} , count inserted to DB:{}", csfCount, databaseCount);
    }

    /***
     * Insert issue file to database and output path id blacklist
     * @param scanTask
     * @param csfReader
     * @param issueRandomAccessFile
     * @param filePathTableStartOffset
     * @param filePathTableEndOffset
     * @param fileBlacklist Input a blacklist
     * @param filePathIdBlackListSet A set will be filled in a set of pathId in blacklist
     * @throws IOException
     */
    public void insertIssueFile(ScanTask scanTask, CsfReader csfReader, RandomAccessFile issueRandomAccessFile, long filePathTableStartOffset, long filePathTableEndOffset,String[] fileBlacklist, Set<Integer> filePathIdBlackListSet) throws IOException {
        //block to insert file path
        log.info("[insertIssueFile] Start reading file path one by one, offset: {} - {} ", filePathTableStartOffset, filePathTableEndOffset);
        List<IssueFile> issueFileInsertList = new ArrayList();

        long beginningOffset = 2;
        issueRandomAccessFile.seek(filePathTableStartOffset + beginningOffset);

        BufferedReader brRafReader = new BufferedReader(
                new FileReader(issueRandomAccessFile.getFD()));
        String line = null;
        long realPosition = filePathTableStartOffset + beginningOffset;

        int lastOffset = (int) beginningOffset;

        int csfCount = 0;
        int databaseCount = 0;

        while ((line = brRafReader.readLine()) != null && realPosition < filePathTableEndOffset
        ) {
            realPosition += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;

            int localOffset = (int) (realPosition - filePathTableStartOffset);

            //Add to id to blacklist
            String finalLine = line;
            if (fileBlacklist != null && Arrays.stream(fileBlacklist).anyMatch(pattern -> StringUtil.wildCardMatch(finalLine, pattern))) {
                filePathIdBlackListSet.add(lastOffset);
            }

            IssueFile issueFileV3 = IssueFile.builder()
                    .scanTaskId(scanTask.getId())
                    .id(lastOffset)
                    .path(line)
                    .build();

            issueFileInsertList.add(issueFileV3);
            csfCount++;

            if (issueFileInsertList.size() >= saveBatchSize) {
                databaseCount += batchInsertAndResetIssueFileList(issueFileInsertList);

            }

            lastOffset = localOffset;
        }//end while

        if (!issueFileInsertList.isEmpty()) {
            databaseCount += batchInsertAndResetIssueFileList(issueFileInsertList);
        }
        log.info("[insertIssueFile] End inserting. count in csf:{} , count inserted to DB:{}", csfCount, databaseCount);
    }


    public static int getOffset(BufferedReader bufferedReader) {
        Field field = null;
        int result = 0;
        try {
            field = BufferedReader.class.getDeclaredField("nextChar");
            field.setAccessible(true);
            result = (Integer) field.get(bufferedReader);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("This should not be occured", e);
        } finally {
            field.setAccessible(false);
        }
        return result;
    }



    public int batchInsertAndResetIssueFileList(List<IssueFile> issueFileInsertList) {
        log.debug("[batchInsertAndResetList] {} issue file will save to database", issueFileInsertList.size());
        int count = issueFileDao.batchInsertIssueFile(issueFileInsertList);
        issueFileInsertList.clear();
        return count;
    }

    public int batchInsertAndResetIssueStringList(List<com.xcal.api.entity.v3.IssueString> issueStringInsertList) {
        log.debug("[batchInsertAndResetList] {} issue string will save to database", issueStringInsertList.size());
        int count = issueStringDao.batchInsertIssueString(issueStringInsertList);
        issueStringInsertList.clear();
        return count;
    }

    public int batchUpdateIssueGroupList(UUID projectId, List<IssueGroup> updateList) {
        log.debug("[batchUpdateAndResetIssueGroupList] {} issue group will save to database", updateList.size());
        return issueGroupDao.batchUpdateIssueGroup(projectId, updateList);

    }


    public int batchUpdateIssueGroupListToFixed(UUID projectId, List<IssueGroup> updateList) {
        log.debug("[batchUpdateAndResetIssueGroupList] {} issue group will save to database", updateList.size());
        return issueGroupDao.batchUpdateIssueGroupToFixed(projectId, updateList);

    }


    public int batchDeleteIssuePathListWithIssueGroups(List<IssueGroup> issueGroupList) {
        log.debug("[batchDeleteIssuePathList] issue paths of {} issue group will bet deleted", issueGroupList.size());
        return issueMapperDao.batchHardDeleteIssueByIssueGroup(issueGroupList);
    }

    public int batchInsertIssueGroupList(List<IssueGroup> issueGroupList) {
        log.debug("[batchInsertAndResetIssueGroupList] {} issue group will save to database", issueGroupList.size());
        int count = issueGroupDao.batchInsertIssueGroup(issueGroupList);
        return count;
    }


    public int batchInsertList(List<com.xcal.api.entity.v3.Issue> insertList) {
        log.debug("[batchInsertAndResetIssuePathList] {} issue paths will save to database", insertList.size());
        int count = 0;

        issueMapperDao.batchInsertIssueWithFaultTolerance(insertList);

        insertList.clear();
        return count;
    }

    public ImportScanResultRequest readScanResultFile(ObjectMapper objectMapper, File file, ScanTask scanTask, String currentUsername) throws AppException {
        log.debug("[readScanResultFile] objectMapper: {}, fileName: {}, scanTask, id: {}, currentUsername: {}", objectMapper, file.getName(), scanTask.getId(), currentUsername);
        ImportScanResultRequest importScanResultRequest;
        try (InputStream is = new FileInputStream(file)) {
            importScanResultRequest = objectMapper.readValue(is, ImportScanResultRequest.class);
        } catch (IOException e) {
            log.error("[readScanResultFile] error message: {}: {}", e.getClass(), e.getMessage());
            scanTaskService.updateScanTaskStatus(scanTask, ScanTaskStatusLog.Stage.IMPORT_RESULT, ScanTaskStatusLog.Status.PROCESSING, 100.0,
                    null, "import result failed, invalid file.", currentUsername);
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_ISSUE_IMPORTISSUE_INVALID_FILE.unifyErrorCode,
                    CommonUtil.formatString("[{}] fileName: {}", AppException.ErrorCode.E_API_ISSUE_IMPORTISSUE_INVALID_FILE.messageTemplate, file.getName()), e);
        }
        return importScanResultRequest;
    }

    public ImportScanResultRequest processScanResultFile(File file, ScanTask scanTask, String currentUsername) throws AppException {
        log.info("[processScanResultFile] fileName: {}, scanTask, id: {}, currentUsername: {}", file.getName(), scanTask.getId(), currentUsername);
        ImportScanResultRequest result;
        String suffix = StringUtils.substringAfter(file.getName(), ".").toLowerCase();
        switch (suffix) {
            case VariableUtil.JSON_DEFAULT_SUFFIX:
            case VariableUtil.JSON_ABBR_SUFFIX:
            case VariableUtil.JSON_STANDAND_SUFFIX:
                result = readScanResultFile(om, file, scanTask, currentUsername);
                break;
            case VariableUtil.CBOR_ABBR_SUFFIX:
            case VariableUtil.CBOR_STANDAND_SUFFIX:
                result = readScanResultFile(cborOm, file, scanTask, currentUsername);
                break;
            case VariableUtil.ZIP_STANDAND_SUFFIX:
            case VariableUtil.GZIP_ABBR_SUFFIX:
            case VariableUtil.GZIP_STANDAND_SUFFIX:
                // Extract the archive file to a folder named with scan task id in the path where archive file places
                // Find the file in the folder(only one scan result file now) and call processScanResultFile
                // Delete the new created folder
                File decompressFolder = fileService.decompressFile(file.getParent(), scanTask.getId().toString(), file.getName());
                // Package tgz archive on Mac OS will contain a hidden file. Knock out the hidden file here.
                File[] listOfFiles = decompressFolder.listFiles(f -> !f.isHidden());
                if (listOfFiles == null || listOfFiles.length != 1) {
                    throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_ISSUE_IMPORTISSUE_INVALID_FILE.unifyErrorCode,
                            CommonUtil.formatString("[{}] Archive should have one(only) scan result file", AppException.ErrorCode.E_API_ISSUE_IMPORTISSUE_INVALID_FILE.messageTemplate));
                }
                result = processScanResultFile(listOfFiles[0], scanTask, currentUsername);

                log.debug("[processScanResultFile] begin to delete file {}", decompressFolder.getPath());
                boolean isFileDeleted = FileUtils.deleteQuietly(decompressFolder);
                log.debug("[processScanResultFile] is file {} deleted: {}", decompressFolder.getPath(), isFileDeleted);
                break;
            default:
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_ISSUE_IMPORTISSUE_INVALID_FILE.unifyErrorCode,
                        CommonUtil.formatString("[{}] unsupported file format: {}", AppException.ErrorCode.E_API_ISSUE_IMPORTISSUE_INVALID_FILE.messageTemplate, suffix));
        }
        return result;
    }

    public ImportIssueDiffRequest processIssueDiffFile(File fixedIssueFile, File newIssueFile, ScanTask baselineScanTask, ScanTask scanTask, String currentUsername) throws AppException {
        ImportIssueDiffRequest importIssueDiffRequest = ImportIssueDiffRequest.builder()
                .baselineScanTaskId(baselineScanTask.getId())
                .scanTaskId(scanTask.getId())
                .fixedIssue(new ArrayList<>())
                .fixedIssuePath(new ArrayList<>())
                .newIssue(new ArrayList<>())
                .newIssuePath(new ArrayList<>())
                .build();

        if (fixedIssueFile != null) {
            ImportScanResultRequest fixedIssueRequest = this.processScanResultFile(fixedIssueFile, baselineScanTask, currentUsername);
            List<Issue> fixedIssues = this.retrieveIssuesFromImportScanResultRequest(baselineScanTask, fixedIssueRequest, currentUsername);
            for (Issue issue : fixedIssues) {
                importIssueDiffRequest.getFixedIssuePath().addAll(issue.getIssueTraces().stream()
                        .map(trace -> ImportIssueDiffRequest.IssueDiff.builder()
                                .issueKey(trace.getIssue().getIssueKey())
                                .checksum(trace.getChecksum())
                                .build())
                        .collect(Collectors.toList()));
                if (!this.issueRepository.findByScanTaskAndIssueKey(scanTask, issue.getIssueKey()).isPresent()) {
                    importIssueDiffRequest.getFixedIssue().add(ImportIssueDiffRequest.IssueDiff.builder()
                            .issueKey(issue.getIssueKey())
                            .checksum(issue.getChecksum())
                            .build());
                }
            }
        }

        if (newIssueFile != null) {
            ImportScanResultRequest newIssueRequest = this.processScanResultFile(newIssueFile, scanTask, currentUsername);
            List<Issue> newIssues = this.retrieveIssuesFromImportScanResultRequest(scanTask, newIssueRequest, currentUsername);
            for (Issue issue : newIssues) {
                importIssueDiffRequest.getNewIssuePath().addAll(issue.getIssueTraces().stream()
                        .map(trace -> ImportIssueDiffRequest.IssueDiff.builder()
                                .issueKey(trace.getIssue().getIssueKey())
                                .checksum(trace.getChecksum())
                                .build())
                        .collect(Collectors.toList()));
                if (!this.issueRepository.findByScanTaskAndIssueKey(baselineScanTask, issue.getIssueKey()).isPresent()) {
                    importIssueDiffRequest.getNewIssue().add(ImportIssueDiffRequest.IssueDiff.builder()
                            .issueKey(issue.getIssueKey())
                            .checksum(issue.getChecksum())
                            .build());
                }
            }
        }

        return importIssueDiffRequest;
    }

    public List<Issue> importIssueToScanTask(ScanTask scanTask, ImportScanResultRequest importScanResultRequest, String currentUsername) {
        log.info("[importIssueToScanTask] scanTask, id: {}, currentUsername: {}", scanTask.getId(), currentUsername);
        List<Issue> issues;
        issues = this.retrieveIssuesFromImportScanResultRequest(scanTask, importScanResultRequest, currentUsername);
        Map<String, Issue> issuesMap = issues.stream().collect(Collectors.toMap(Issue::getIssueKey, issue -> issue));
        List<Issue> existingIssues = this.findByScanTask(scanTask);
        Map<String, Issue> existingIssuesMap = existingIssues.stream().collect(Collectors.toMap(Issue::getIssueKey, issue -> issue));
        existingIssuesMap.keySet().forEach(issuesMap::remove);
        log.debug("[importIssueToScanTask] {} issues will save to database", issuesMap.size());
        if (!issuesMap.isEmpty()) {
            for (List<Issue> saveList : ListUtils.partition(new ArrayList<>(issuesMap.values()), saveBatchSize)) {
                issueRepository.saveAll(saveList);
                this.issueRepository.flush();
            }
        }
        return issues;
    }

    public ImportIssueDiffResponse importIssueDiff(ImportIssueDiffRequest importIssueDiffRequest, String currentUsername) throws AppException {
        log.info("[importIssueToScanTask] importIssueDiffRequest, baselineScanTask: {}, scanTask: {}, currentUsername: {}",
                importIssueDiffRequest.getBaselineScanTaskId(), importIssueDiffRequest.getScanTaskId(), currentUsername);

        UUID baselineScanTaskId = importIssueDiffRequest.getBaselineScanTaskId();
        ScanTask baselineScanTask = this.scanTaskService.findById(baselineScanTaskId).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, baselineScanTaskId)));

        UUID scanTaskId = importIssueDiffRequest.getScanTaskId();
        ScanTask scanTask = this.scanTaskService.findById(scanTaskId).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, scanTaskId)));

        ImportIssueDiffResponse importIssueDiffResponse = ImportIssueDiffResponse.builder()
                .baselineScanTask(convertScanTaskToImportIssueDiffResponseScanTask(baselineScanTask))
                .scanTask(convertScanTaskToImportIssueDiffResponseScanTask(scanTask))
                .build();

        importIssueDiffResponse.getFixedIssue().addAll(this.processDiff(baselineScanTask, scanTask, IssueDiff.Type.FIXED, importIssueDiffRequest.getFixedIssue(), currentUsername));
        importIssueDiffResponse.getFixedIssuePath().addAll(this.processDiff(baselineScanTask, scanTask, IssueDiff.Type.FIXED_PATH, importIssueDiffRequest.getFixedIssuePath(), currentUsername));
        importIssueDiffResponse.getNewIssue().addAll(this.processDiff(baselineScanTask, scanTask, IssueDiff.Type.NEW, importIssueDiffRequest.getNewIssue(), currentUsername));
        importIssueDiffResponse.getNewIssuePath().addAll(this.processDiff(baselineScanTask, scanTask, IssueDiff.Type.NEW_PATH, importIssueDiffRequest.getNewIssuePath(), currentUsername));

        return importIssueDiffResponse;
    }

    private List<ImportIssueDiffResponse.IssueDiff> processDiff(ScanTask baselineScanTask, ScanTask scanTask, IssueDiff.Type type, List<ImportIssueDiffRequest.IssueDiff> diffRequests, String currentUsername) {
        Date now = new Date();
        List<ImportIssueDiffResponse.IssueDiff> diffResponses = new ArrayList<>();
        List<IssueDiff> diffs = new ArrayList<>();
        String commitId = scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID, null);
        String baselineCommitId = scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID, null);


        for (ImportIssueDiffRequest.IssueDiff issueDiff : diffRequests) {
            ScanTask srcScanTask = (type == IssueDiff.Type.FIXED) || (type == IssueDiff.Type.FIXED_PATH) ? baselineScanTask : scanTask;
            Optional<Issue> issueOptional = this.issueRepository.findByScanTaskAndIssueKey(srcScanTask, issueDiff.getIssueKey());

            ImportIssueDiffResponse.IssueDiff diffResponse = ImportIssueDiffResponse.IssueDiff.builder()
                    .issueId(issueDiff.getId())
                    .checksum(issueDiff.getChecksum())
                    .build();
            if (issueOptional.isPresent()) {
                IssueDiff diff = IssueDiff.builder()
                        .baselineScanTask(baselineScanTask)
                        .scanTask(scanTask)
                        .type(type)
                        .issue(issueOptional.get())
                        .checksum(issueDiff.getChecksum())
                        .createdBy(currentUsername)
                        .createdOn(now)
                        .modifiedBy(currentUsername)
                        .modifiedOn(now)
                        .build();

                if (Arrays.asList(IssueDiff.Type.FIXED, IssueDiff.Type.FIXED_PATH).contains(type)) {
                    issueOptional.map(Issue::getIssueKey).ifPresent(diff::setBaselineIssueKey);
                }
                if (Arrays.asList(IssueDiff.Type.NEW, IssueDiff.Type.NEW_PATH).contains(type)) {
                    issueOptional.map(Issue::getIssueKey).ifPresent(diff::setIssueKey);
                }

                IssueDiff dbIssueDiff = this.issueDiffRepository.save(diff);
                diffResponse.setResult("SUCCESS");
                diffResponse.setId(dbIssueDiff.getId());
                if (StringUtils.isNotBlank(dbIssueDiff.getIssueKey())) {
                    diffResponse.setIssueKey(dbIssueDiff.getIssueKey());
                }
                if (StringUtils.isNotBlank(dbIssueDiff.getBaselineIssueKey())) {
                    diffResponse.setBaselineIssueKey(dbIssueDiff.getBaselineIssueKey());
                }
                diffs.add(dbIssueDiff);
            } else {
                log.error("[processDiff] issue not found, id: {}", issueDiff.getId());
                diffResponse.setResult("FAILED");
            }
            diffResponses.add(diffResponse);
        }
        this.issueDiffRepository.flush();
        Map<String, String> summary = new HashMap<>();
        if (StringUtils.isNotBlank(commitId)) {
            summary.put("diff.commitId", commitId);
            summary.put("commitId", commitId);
        }
        if (StringUtils.isNotBlank(baselineCommitId)) {
            summary.put("diff.baselineCommitId", baselineCommitId);
        }
        summary.put("diff.baselineScanTaskId", String.valueOf(baselineScanTask.getId()));
        List<IssueDiff> currentDiffs;
        String typeString;
        switch (type) {
            case NEW:
                typeString = "new";
                currentDiffs = diffs.stream().filter(d -> d.getType() == IssueDiff.Type.NEW).collect(Collectors.toList());
                break;
            case NEW_PATH:
                typeString = "newPath";
                currentDiffs = diffs.stream().filter(d -> d.getType() == IssueDiff.Type.NEW_PATH).collect(Collectors.toList());
                break;
            case FIXED:
                typeString = "fixed";
                currentDiffs = diffs.stream().filter(d -> d.getType() == IssueDiff.Type.FIXED).collect(Collectors.toList());
                break;
            case FIXED_PATH:
                typeString = "fixedPath";
                currentDiffs = diffs.stream().filter(d -> d.getType() == IssueDiff.Type.FIXED_PATH).collect(Collectors.toList());
                break;
            default:
                typeString = "unknown";
                currentDiffs = new ArrayList<>();
                break;
        }
        summary.put("diff.issue." + typeString, String.valueOf(currentDiffs.size()));
        Map<RuleInformation, List<IssueDiff>> diffRuleMap = currentDiffs.stream().collect(Collectors.groupingBy(issueDiff -> issueDiff.getIssue().getRuleInformation()));

        // Count by rule
        // diffMapByVul, Map key: vul, value: RuleInformation list that exist in currentDiffs
        final Map<String, List<RuleInformation>> diffMapByVul = diffRuleMap.keySet().stream().collect(Collectors.groupingBy(RuleInformation::getVulnerable));
        diffMapByVul.forEach((vul, value) -> summary.put("diff.issue.vul." + vul + "." + typeString,
                String.valueOf(currentDiffs.stream().filter(diff -> value.contains(diff.getIssue().getRuleInformation())).count())));

        // Count by priority
        // diffMapByVul, Map key: priority, value: RuleInformation list that exist in currentDiffs
        final Map<RuleInformation.Priority, List<RuleInformation>> diffMapByPriority = diffRuleMap.keySet().stream().collect(Collectors.groupingBy(RuleInformation::getPriority));
        diffMapByPriority.forEach((priority, value) -> summary.put("diff.issue.priority." + priority.name() + "." + typeString,
                String.valueOf(currentDiffs.stream().filter(diff -> value.contains(diff.getIssue().getRuleInformation())).count())));

        // Count by severity
        // diffMapByVul, Map key: severity, value: RuleInformation list that exist in currentDiffs
        final Map<RuleInformation.Severity, List<RuleInformation>> diffMapBySeverity = diffRuleMap.keySet().stream().collect(Collectors.groupingBy(RuleInformation::getSeverity));
        diffMapBySeverity.forEach((severity, value) -> summary.put("diff.issue.severity." + severity.name() + "." + typeString,
                String.valueOf(currentDiffs.stream().filter(diff -> value.contains(diff.getIssue().getRuleInformation())).count())));

        // count by ruleSet
        // diffMapByVul, Map key: ruleSetName, value: RuleInformation list that exist in currentDiffs
        Map<String, List<RuleInformation>> diffMapByRuleSet = diffRuleMap.keySet().stream().collect(Collectors.groupingBy(ri -> ri.getRuleSet().getName()));
        diffMapByRuleSet.forEach((ruleSetName, value) -> summary.put("diff.issue." + ruleSetName + "." + typeString,
                String.valueOf(currentDiffs.stream().filter(diff -> value.contains(diff.getIssue().getRuleInformation())).count())));

        for (Map.Entry<String, List<RuleInformation>> setEntry : diffMapByRuleSet.entrySet()) {
            // Count by rule
            final Map<String, List<RuleInformation>> diffMapByRuleSetVul = setEntry.getValue().stream().collect(Collectors.groupingBy(RuleInformation::getVulnerable));
            diffMapByRuleSetVul.forEach((vul, value) -> summary.put("diff.issue." + setEntry.getKey() + ".vul." + vul + "." + typeString,
                    String.valueOf(currentDiffs.stream().filter(diff -> value.contains(diff.getIssue().getRuleInformation())).count())));

            // Count by priority
            final Map<RuleInformation.Priority, List<RuleInformation>> diffMapByRuleSetPriority = setEntry.getValue().stream().collect(Collectors.groupingBy(RuleInformation::getPriority));
            diffMapByRuleSetPriority.forEach((priority, value) -> summary.put("diff.issue." + setEntry.getKey() + ".priority." + priority.name() + "." + typeString,
                    String.valueOf(currentDiffs.stream().filter(diff -> value.contains(diff.getIssue().getRuleInformation())).count())));

            // Count by severity
            final Map<RuleInformation.Severity, List<RuleInformation>> diffMapByRuleSetSeverity = setEntry.getValue().stream().collect(Collectors.groupingBy(RuleInformation::getSeverity));
            diffMapByRuleSetSeverity.forEach((severity, value) -> summary.put("diff.issue." + setEntry.getKey() + ".severity." + severity.name() + "." + typeString,
                    String.valueOf(currentDiffs.stream().filter(diff -> value.contains(diff.getIssue().getRuleInformation())).count())));
        }
        this.scanTaskService.updateScanTaskSummary(scanTask, summary);
        return diffResponses;
    }

    public static ImportIssueDiffResponse.ScanTask convertScanTaskToImportIssueDiffResponseScanTask(ScanTask scanTask) {
        log.trace("[convertScanTaskToImportIssueDiffResponseScanTask] ScanTask: {}", scanTask);
        ImportIssueDiffResponse.ScanTask result = ImportIssueDiffResponse.ScanTask.builder()
                .id(scanTask.getId())
                .projectUuid(scanTask.getProject().getId())
                .projectId(scanTask.getProject().getProjectId())
                .projectName(scanTask.getProject().getName())
                .status(scanTask.getStatus().name())
                .sourceRoot(scanTask.getSourceRoot())
                .createdBy(scanTask.getCreatedBy())
                .createdOn(scanTask.getCreatedOn())
                .modifiedBy(scanTask.getModifiedBy())
                .modifiedOn(scanTask.getModifiedOn())
                .build();
        log.trace("[convertScanTaskToImportIssueDiffResponseScanTask] result: {}", result);
        return result;
    }

    public List<Issue> retrieveIssuesFromImportScanResultRequest(ScanTask scanTask, ImportScanResultRequest importScanResultRequest, String currentUsername) {
        log.info("[retrieveIssuesFromImportScanResultRequest] scanTask, id: {}, engine: {}, engineVersion: {}, currentUsername: {}", scanTask.getId(), importScanResultRequest.getEngine(), importScanResultRequest.getEngineVersion(), currentUsername);
        Map<String, ScanFile> scanFileMap = this.retrieveScanFileMapFromResult(scanTask, importScanResultRequest.getFileInfos());
        Map<String, ImportScanResultRequest.FileInfo> fileInfoMap = this.retrieveFileInfoMapFromResult(importScanResultRequest.getFileInfos());

        String scanEngineName = StringUtils.defaultIfEmpty(importScanResultRequest.getEngine(), "Xcalibyte");
        String scanEngineVersion = StringUtils.defaultIfEmpty(importScanResultRequest.getEngineVersion(), "1.0");
        Map<String, RuleInformation> ruleInformationMap = this.retrieveRuleInformationMap(scanEngineName, scanEngineVersion);

        Map<String, Issue> issueMap = new LinkedHashMap<>();
        Date now = new Date();
        for (ImportScanResultRequest.Issue requestIssue : importScanResultRequest.getIssues()) {
            String issueKey = requestIssue.getKey();
            Issue issue = issueMap.get(issueKey);
            if (issue == null) {
                ScanFile scanFile = scanFileMap.get(requestIssue.getFileId());
                RuleInformation ruleInformation = null;
                String checkRuleInformationKey = RuleService.getRuleCodeKey(scanEngineName, requestIssue.getRuleSet(), requestIssue.getRuleCode());
                if (ruleInformationMap.containsKey(requestIssue.getErrorCode())) {
                    ruleInformation = ruleInformationMap.get(requestIssue.getErrorCode());
                } else if (ruleInformationMap.containsKey(checkRuleInformationKey)) {
                    ruleInformation = ruleInformationMap.get(checkRuleInformationKey);
                } else if (ruleInformationMap.containsKey(requestIssue.getRuleCode())) {
                    ruleInformation = ruleInformationMap.get(requestIssue.getRuleCode());
                }
                String message = requestIssue.getMessage();
                if (ruleInformation != null && (StringUtils.isBlank(message) || StringUtils.equalsIgnoreCase("@@issueMessage@@", message))) {
                    message = ruleInformation.getMessageTemplate();
                }

                // Since severity is related to spotbugs issues, not with spotbugs rule metadata. And we have no attributes in issues in v file to represent it.
                // Temporarily use errorCode to keep the severity issues for spotbugs severity.
                String severity = null;
                if (scanEngineName.toLowerCase().contains(ScanEngine.EngineType.SPOTBUGS.toString().toLowerCase())) {
                    if (StringUtils.isBlank(requestIssue.getErrorCode())) {
                        log.error("[retrieveIssuesFromImportScanResultRequest] Spotbugs issue's error code should not be blank, requestIssue: {}", requestIssue);
                    }
                    severity = requestIssue.getErrorCode();
                }

                if (ruleInformation != null) {
                    issue = Issue.builder()
                            .issueKey(requestIssue.getKey())
                            .ruleInformation(ruleInformation)
                            .issueCode(ruleInformation.getRuleCode() != null ? ruleInformation.getRuleCode() : requestIssue.getErrorCode())
                            .scanTask(scanTask)
                            .seq(StringUtils.leftPad(String.valueOf(1 + issueMap.size()), 5, '0'))
                            .severity(ruleInformation.getSeverity() == null ? Issue.Severity.valueOf(severity) : Issue.Severity.valueOf(ruleInformation.getSeverity().toString()))
                            .scanFile(scanFile)
                            .lineNo(requestIssue.getStartLineNo())
                            .columnNo(requestIssue.getStartColumnNo())
                            .functionName(requestIssue.getFunctionName())
                            .variableName(requestIssue.getVariableName())
                            .message(message)
                            .status(Issue.Status.ACTIVE)
                            .action(Issue.Action.PENDING)
                            .createdBy(currentUsername).createdOn(now)
                            .modifiedBy(currentUsername).modifiedOn(now)
                            .build();
                    List<IssueAttribute> attributes = this.retrieveIssueAttributes(requestIssue, issue, ruleInformation);
                    issue.setAttributes(attributes);
                    if (scanFile != null) {
                        issue.setFilePath(scanFile.getStorePath());
                    } else {
                        log.warn("[retrieveIssuesFromImportScanResultRequest] scanFile is null. issue, key: {}, currentUsername: {}", issue.getIssueKey(), currentUsername);
                        if (fileInfoMap.get(requestIssue.getFileId()) == null) {
                            log.error("[retrieveIssuesFromImportScanResultRequest] no corresponding file info, requestIssue: {}", requestIssue);
                        } else {
                            issue.setFilePath(fileInfoMap.get(requestIssue.getFileId()).getPath());
                        }
                    }
                    issueMap.put(issueKey, issue);
                } else {
                    log.error("[retrieveIssuesFromImportScanResultRequest] there is no map for requestIssue: {}, please add it to RuleInformation", requestIssue);
                }
            }
            if (issue != null) {
                // issueTraceMap before add new traces
                Map<String, List<IssueTrace>> issueTraceMap = issue.getIssueTraces().stream().collect(Collectors.groupingBy(IssueTrace::getChecksum));
                List<IssueTrace> issueTraces = this.retrieveTracePathFromImportScanResultRequest(fileInfoMap, scanFileMap, requestIssue, issue, currentUsername);
                // get the checksum from the issueTraces
                String checksum = issueTraces.stream().map(IssueTrace::getChecksum).distinct().findFirst().orElse(null);
                //only add to the list if the checksum is not exist
                if (StringUtils.isNotBlank(checksum) && !issueTraceMap.containsKey(checksum)) {
                    issue.getIssueTraces().addAll(issueTraces);
                }
            }
        }
        issueMap.values().forEach(issue -> {
            this.updateNumberOfTraceAttribute(issue);
            this.updateComplexityAttribute(issue);
        });
        this.updateComplexityAttribute(issueMap.values());

        return new ArrayList<>(issueMap.values());
    }

    private void updateNumberOfTraceAttribute(Issue issue) {
        log.trace("[updateNumberOfTraceAttribute] issue key: {}", issue.getIssueKey());
        long numberOfTraceSet = issue.getIssueTraces().stream().map(IssueTrace::getChecksum).distinct().count();
        this.updateAttribute(issue, VariableUtil.IssueAttributeName.NO_OF_TRACE_SET, String.valueOf(numberOfTraceSet));
    }

    private void updateComplexityAttribute(Issue issue) {
        log.trace("[updateComplexityAttribute] issue key: {}", issue.getIssueKey());
        Map<String, Optional<Double>> complexityMap = issue.getIssueTraces().stream()
                .collect(Collectors.groupingBy(IssueTrace::getChecksum, Collectors.mapping(issueTrace -> NumberUtils.toDouble(issueTrace.getComplexity(), 0.0), Collectors.maxBy(Double::compare))));
        List<Double> complexities = complexityMap.values().stream().map(complexityOptional -> complexityOptional.orElse(0.0)).collect(Collectors.toList());
        Double max = complexities.stream().max(Double::compare).orElse(0.0);
        Double min = complexities.stream().min(Double::compare).orElse(0.0);
        long numberOfTraceSet = issue.getIssueTraces().stream().map(IssueTrace::getChecksum).distinct().count();
        // complexity for an issue is "the number of paths in the issue" x "max complexity for all paths in the issue"
        final double complexity = numberOfTraceSet * max;
        this.updateAttribute(issue, VariableUtil.IssueAttributeName.COMPLEXITY, String.valueOf(complexity));
        this.updateAttribute(issue, VariableUtil.IssueAttributeName.COMPLEXITY_MAX, String.valueOf(max));
        this.updateAttribute(issue, VariableUtil.IssueAttributeName.COMPLEXITY_MIN, String.valueOf(min));
        issue.setComplexity(String.valueOf(complexity));
    }

    private void updateComplexityAttribute(Collection<Issue> issues) {
        log.trace("[updateComplexityAttribute] issues size: {}", issues.size());
        final double max = issues.stream().map(i -> NumberUtils.toDouble(i.getComplexity(), 0.0)).max(Double::compare).orElse(0.0);
        final double min = issues.stream().map(i -> NumberUtils.toDouble(i.getComplexity(), 0.0)).min(Double::compare).orElse(0.0);

        issues.forEach(i -> {
            double complexityRate;
            double complexity = NumberUtils.toDouble(i.getComplexity(), 0.0);
            if (max == min) {
                complexityRate = 10.0;
            } else {
                complexityRate = (complexity - min) / (max - min) * 10.0;
            }
            this.updateAttribute(i, VariableUtil.IssueAttributeName.COMPLEXITY_RATE, String.valueOf(complexityRate));
        });
    }

    private void updateAttribute(Issue issue, VariableUtil.IssueAttributeName attribute, String value) {
        log.trace("[updateAttribute] issue, id: {}, attribute: {}, value: {}", issue.getId(), attribute, value);
        Optional<IssueAttribute> attributeOptional = issue.getAttributes().stream().filter(issueAttribute -> attribute == issueAttribute.getName()).findFirst();
        IssueAttribute issueAttribute;
        if (attributeOptional.isPresent()) {
            issueAttribute = attributeOptional.get();
        } else {
            issueAttribute = IssueAttribute.builder().issue(issue).name(attribute).build();
            issue.getAttributes().add(issueAttribute);
        }
        issueAttribute.setValue(value);
    }

    private List<IssueAttribute> retrieveIssueAttributes(ImportScanResultRequest.Issue requestIssue, final Issue issue, RuleInformation ruleInformation) {
        List<IssueAttribute> attributes = new ArrayList<>();
        String certainty;
        if (StringUtils.isNotBlank(requestIssue.getCertainty())) {
            certainty = requestIssue.getCertainty();
        } else {
            certainty = ruleInformation.getCertainty().name();
        }
        String vulnerable;
        if (StringUtils.isNotBlank(requestIssue.getRuleCode())) {
            vulnerable = requestIssue.getRuleCode();
        } else {
            vulnerable = ruleInformation.getVulnerable();
        }
        String severity;
        if (ruleInformation.getSeverity() == null) {
            severity = requestIssue.getErrorCode();
        } else {
            severity = ruleInformation.getSeverity().toString();
        }
        String priority;
        if (ruleInformation.getPriority() == null) {
            priority = requestIssue.getErrorCode();
        } else {
            priority = ruleInformation.getPriority().toString();
        }
        attributes.add(IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value(ruleInformation.getRuleCode()).build());
        attributes.add(IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CERTAINTY).value(certainty).build());
        attributes.add(IssueAttribute.builder().name(VariableUtil.IssueAttributeName.VULNERABLE).value(vulnerable).build());
        attributes.add(IssueAttribute.builder().name(VariableUtil.IssueAttributeName.SEVERITY).value(severity).build());
        attributes.add(IssueAttribute.builder().name(VariableUtil.IssueAttributeName.PRIORITY).value(priority).build());
        if (StringUtils.isNotBlank(requestIssue.getErrorCode())) {
            attributes.add(IssueAttribute.builder().name(VariableUtil.IssueAttributeName.ERROR_CODE).value(requestIssue.getErrorCode()).build());
        }
        if (ruleInformation.getLikelihood() != null) {
            attributes.add(IssueAttribute.builder().name(VariableUtil.IssueAttributeName.LIKELIHOOD).value(ruleInformation.getLikelihood().toString()).build());
        }
        if (ruleInformation.getRemediationCost() != null) {
            attributes.add(IssueAttribute.builder().name(VariableUtil.IssueAttributeName.REMEDIATION_COST).value(ruleInformation.getRemediationCost().toString()).build());
        }
        if (ruleInformation.getCategory() != null) {
            attributes.add(IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CATEGORY).value(ruleInformation.getCategory()).build());
        }
        attributes.forEach(issueAttribute -> issueAttribute.setIssue(issue));
        return attributes;
    }


    public Map<String, RuleInformation> retrieveRuleInformationMap(String engineName, String engineVersion) {
        log.info("[retrieveRuleInformationMap] engineName: {}, engineVersion: {}", engineName, engineVersion);
        Map<String, RuleInformation> result;
        List<RuleInformation> ruleInformationList = this.ruleService.getRuleInformation(engineName, engineVersion, null, null);
        if (ruleInformationList.isEmpty() && StringUtils.contains(engineVersion, "-")) {
            engineVersion = StringUtils.substringBefore(engineVersion, "-");
            result = retrieveRuleInformationMap(engineName, engineVersion);
        } else {
            result = ruleInformationList.stream().collect(Collectors.toMap(RuleService::getRuleCodeKey, ri -> ri));
        }
        return result;
    }

    public List<IssueTrace> retrieveTracePathFromImportScanResultRequest(Map<String, ImportScanResultRequest.FileInfo> fileInfoMap, Map<String, ScanFile> scanFileMap, ImportScanResultRequest.Issue requestIssue, Issue issue, String currentUsername) {
        log.trace("[retrieveTracePathFromImportScanResultRequest] issue, issueKey: {}, currentUsername: {}", issue.getIssueKey(), currentUsername);

        List<IssueTrace> issueTraces = new ArrayList<>();
        List<ImportScanResultRequest.Issue.TracePath> requestTracePaths = requestIssue.getTracePaths();
        int counter = 1;
        Date now = new Date();

        String lastKey = null;
        for (ImportScanResultRequest.Issue.TracePath tp : requestTracePaths) {
            String issueTraceKey = tp.getFileId() + "-" + tp.getStartLineNo() + "-" + tp.getStartColumnNo() + "-" + tp.getFunctionName() + "-" + tp.getVariableName() + "-" + tp.getMessage();
            // Compare the just last Key, If same, dispose that
            if (!StringUtils.equalsIgnoreCase(lastKey, issueTraceKey)) {
                ScanFile scanFile = scanFileMap.get(tp.getFileId());

                IssueTrace issueTrace = IssueTrace.builder()
                        .issue(issue)
                        .seq(counter)
                        .scanFile(scanFile)
                        .lineNo(tp.getStartLineNo())
                        .columnNo(tp.getStartColumnNo())
                        .functionName(tp.getFunctionName())
                        .variableName(tp.getVariableName())
                        .checksum(null)
                        .message(tp.getMessage())
                        .createdBy(currentUsername).createdOn(now)
                        .modifiedBy(currentUsername).modifiedOn(now)
                        .build();
                if (scanFile != null) {
                    issueTrace.setFilePath(scanFile.getStorePath());
                } else {
                    log.warn("[retrieveTracePathFromImportScanResultRequest] scanFile is null. issue key: {}, trace path, counter:{}, currentUsername: {}", issue.getIssueKey(), counter, currentUsername);
                    if (fileInfoMap.get(tp.getFileId()) != null) {
                        issueTrace.setFilePath(fileInfoMap.get(tp.getFileId()).getPath());
                    } else {
                        log.error("[retrieveTracePathFromImportScanResultRequest] no trace path file info. trace path, counter:{}, currentUsername: {}", counter, currentUsername);
                    }
                }
                issueTraces.add(issueTrace);
                counter++;
                lastKey = issueTraceKey;
            }
        }
        // checksum used for an almost unique md5 to set issue trace is in same set
//        final String checksum = DigestUtils.md5Hex(issueTraces.toString());
        final String checksum = DigestUtils.md5Hex(issueTraces.stream().map(trace -> trace.getLineNo().toString()).collect(Collectors.joining("-")));
        final double complexity = NumberUtils.toDouble(requestIssue.getComplexity(), 0);
        issueTraces.forEach(it -> {
            it.setChecksum(checksum);
            it.setComplexity(String.valueOf(complexity));
        });
        return issueTraces;
    }


    public Map<String, ScanFile> retrieveScanFileMapFromResult(ScanTask scanTask, List<ImportScanResultRequest.FileInfo> requestFileInfos) {
        log.info("[retrieveScanFileMapFromResult] requestFileInfos size: {}", requestFileInfos.size());
        Map<String, ScanFile> scanFileMap = new HashMap<>();
        List<ScanFile> scanFiles = this.scanTaskService.findScanFileByScanTask(scanTask);
        Map<String, ScanFile> sfMap = scanFiles.stream().filter(sf -> sf.getFileInfo() != null).collect(Collectors.toMap(ScanFile::getStorePath, sf -> sf));

        for (ImportScanResultRequest.FileInfo requestFileInfo : requestFileInfos) {
            String scanFilePath = requestFileInfo.getPath();
            if (sfMap.containsKey(scanFilePath)) {
                scanFileMap.put(requestFileInfo.getFileId(), sfMap.get(scanFilePath));
            }
        }
        return scanFileMap;
    }

    public Map<String, ImportScanResultRequest.FileInfo> retrieveFileInfoMapFromResult(List<ImportScanResultRequest.FileInfo> requestFileInfos) {
        log.info("[retrieveFileInfoMapFromResult] requestFileInfos size: {}", requestFileInfos.size());

        return requestFileInfos.stream().collect(Collectors.toMap(ImportScanResultRequest.FileInfo::getFileId, fi -> fi));
    }

    public Page<Issue> listIssueInScanTask(ScanTask scanTask, Pageable pageable) {
        log.info("[listIssueInScanTask] scanTask: {}, pageable: {}", scanTask, pageable);
        return this.issueRepository.searchIssue(scanTask, null, null, null, new HashMap<>(), new HashMap<>(), new ArrayList<>(), new ArrayList<>(), pageable);
    }

    public Issue assignIssue(Issue issue, UUID userId, String currentUsername) throws AppException {
        log.info("[assignIssue] issueId: {}, userId: {}, currentUsername: {}", issue.getId(), userId, currentUsername);
        User user = this.userService.findById(userId).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}: ", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, userId)));
        issue.setAssignTo(user);
        issue.setModifiedBy(currentUsername);
        issue.setModifiedOn(new Date());
        issue = this.issueRepository.saveAndFlush(issue);

        return issue;
    }

    public Optional<Issue> findById(UUID id) {
        log.debug("[findById] id: {}", id);
        return this.issueRepository.findById(id);
    }

    public Optional<Issue> findByIdsWithWholeObject(UUID id) {
        log.info("[findByIdWithWholeObject] id: {}", id);
        return this.issueRepository.findByIdWithWholeObject(id);
    }

    public List<Issue> findByScanTask(ScanTask scanTask) {
        log.info("[findByScanTask] scanTask: {}", scanTask);
        return this.issueRepository.findByScanTask(scanTask);
    }

    public List<Issue> findByProject(Project project) {
        log.info("[findByProject] project: {}", project);
        return this.issueRepository.findByScanTaskProject(project);
    }

    public Issue updateIssueStatus(Issue issue, String status, String currentUsername) throws AppException {
        log.info("[updateIssueStatus] id: {}, status: {}, currentUsername: {}", issue.getId(), status, currentUsername);
        Issue.Status issueStatus = EnumUtils.getEnum(Issue.Status.class, status);
        if (issueStatus == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.unifyErrorCode,
                    CommonUtil.formatString("[{}] status: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.messageTemplate, status));
        }
        issue.setStatus(issueStatus);
        return this.updateIssue(issue, currentUsername);
    }

    public Issue updateIssueSeverity(Issue issue, String severity, String currentUsername) throws AppException {
        log.info("[updateIssueSeverity] id: {}, severity: {}, currentUsername: {}", issue.getId(), severity, currentUsername);
        Issue.Severity issueSeverity = EnumUtils.getEnum(Issue.Severity.class, severity);
        if (issueSeverity == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_ISSUE_COMMON_INVALID_SEVERITY.unifyErrorCode,
                    CommonUtil.formatString("[{}] value: {}", AppException.ErrorCode.E_API_ISSUE_COMMON_INVALID_SEVERITY.messageTemplate, Issue.Severity.values(), severity));
        }
        issue.setSeverity(issueSeverity);
        return this.updateIssue(issue, currentUsername);
    }

    public Issue updateIssueAction(Issue issue, String action, String currentUsername) throws AppException {
        log.info("[updateIssueAction] id: {}, action: {}, currentUsername: {}", issue.getId(), action, currentUsername);
        Issue.Action issueAction = EnumUtils.getEnum(Issue.Action.class, action);
        if (issueAction == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_ISSUE_UPDATEISSUE_INVALID_ACTION.unifyErrorCode,
                    CommonUtil.formatString("[{}] action: {}: ", AppException.ErrorCode.E_API_ISSUE_UPDATEISSUE_INVALID_ACTION.messageTemplate, action));
        }
        issue.setAction(issueAction);
        return this.updateIssue(issue, currentUsername);
    }

    public Issue updateIssue(Issue issue, String currentUsername) {
        log.info("[updateIssue] id: {}, currentUsername: {}", issue.getId(), currentUsername);

        issue.setModifiedBy(currentUsername);
        issue.setModifiedOn(new Date());
        return this.issueRepository.saveAndFlush(issue);
    }

    public void sendIssuesToUsers(AssignIssuesRequest assignIssuesRequest, User currentUser, Locale locale, String currentUsername) throws AppException {
        log.info("[sendIssuesToUsers] issues size: {}, locale: {}, currentUsername: {}", assignIssuesRequest.getAssignIssues().size(), locale.toLanguageTag(), currentUsername);
        List<Issue> issues = new ArrayList<>();
        assignIssuesRequest.getAssignIssues().forEach(i -> this.issueRepository.findByIdWithWholeObject(i.getIssueId()).ifPresent(issues::add));
        if (issues.stream().anyMatch(i -> i.getAssignTo() == null)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_EMAIL_COMMON_UNASSIGNED_ISSUE.unifyErrorCode, AppException.ErrorCode.E_API_EMAIL_COMMON_UNASSIGNED_ISSUE.messageTemplate);
        }
        Map<User, List<Issue>> issuesMap = issues.stream().collect(Collectors.groupingBy(Issue::getAssignTo));
        ScanTask scanTask = issues.get(0).getScanTask();
        for (Map.Entry<User, List<Issue>> entry : issuesMap.entrySet()) {
            this.sendAssignIssueEmail(scanTask, entry.getValue(), entry.getKey().getId(), currentUser, locale, currentUsername);
        }
    }

    public List<Issue> assignIssuesToUsers(AssignIssuesRequest assignIssuesRequest, String currentUsername) throws AppException {
        log.info("[assignIssuesToUsers] assignIssuesRequest: {}, currentUsername: {}", assignIssuesRequest, currentUsername);
        Date now = new Date();
        List<Issue> list = new ArrayList<>();
        Map<UUID, User> userCache = new HashMap<>();
        List<AssignIssuesRequest.AssignIssue> assignIssueList = assignIssuesRequest.getAssignIssues();
        for (AssignIssuesRequest.AssignIssue assignIssue : assignIssueList) {
            Issue issue = this.findById(assignIssue.getIssueId()).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] id: {}: ", AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate, assignIssue.getIssueId())));
            User user;
            if (userCache.containsKey(assignIssue.getUserId())) {
                user = userCache.get(assignIssue.getUserId());
            } else {
                user = this.userService.findById(assignIssue.getUserId()).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] id: {}: ", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, assignIssue.getUserId())));
                userCache.put(assignIssue.getUserId(), user);
            }

            issue.setAssignTo(user);
            issue.setModifiedBy(currentUsername);
            issue.setModifiedOn(now);
            list.add(issue);
        }

        if (!list.isEmpty()) {
            for (List<Issue> saveList : ListUtils.partition(list, saveBatchSize)) {
                issueRepository.saveAll(saveList);
                this.issueRepository.flush();
            }
        }

        return list;
    }

    public void sendAssignIssueEmail(Issue issue, UUID userId, User currentUser, Locale locale, String currentUserName) throws AppException {
        log.info("[sendAssignIssueEmail] issueId: {}, locale: {}, currentUserName: {}", issue.getId(), locale, currentUserName);

        User user = this.userService.findById(userId)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] userId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate,
                                userId
                        )
                ));

        Map<String, Object> model = new HashMap<>();
        model.put("userDisplayName", user.getDisplayName());
        model.put("assignerDisplayName", currentUser.getDisplayName());
        model.put("projectName", issue.getScanTask().getProject().getName());
        model.put("scanStartAt", issue.getScanTask().getScanStartAt());
        model.put("issue", this.convertIssueToDto(issue, locale));
        model.put("uiHost", String.format(
                "%s://%s:%d",
                appProperties.getUiProtocol(),
                appProperties.getUiHost(),
                appProperties.getUiPort()
        ));

        AppProperties.Mail mailSetting = this.settingService.getEmailServerConfiguration();
        SendEmailRequest request = SendEmailRequest.builder()
                .from(mailSetting.getFrom())
                .to(user.getEmail())
                .subject(CommonUtil.formatString(
                        (locale == Locale.SIMPLIFIED_CHINESE) ?
                                AppProperties.EMAIL_SUBJECT_PATTERN_ASSIGN_ISSUE_ZH_CN :
                                AppProperties.EMAIL_SUBJECT_PATTERN_ASSIGN_ISSUE,
                        mailSetting.getPrefix(),
                        currentUser.getDisplayName(),
                        issue.getScanTask().getProject().getName()
                ))
                .templateName((locale == Locale.SIMPLIFIED_CHINESE) ?
                        AppProperties.EMAIL_TEMPLATE_PREFIX_ASSIGN_ISSUE_ZH_CN :
                        AppProperties.EMAIL_TEMPLATE_PREFIX_ASSIGN_ISSUE)
                .model(model)
                .build();

        this.emailService.sendTemplateMail(request, currentUserName);
    }

    public void sendAssignIssueEmail(ScanTask scanTask, List<Issue> issues, UUID userId, User currentUser, Locale locale, String currentUsername) throws AppException {
        log.info("[sendAssignIssueEmail] scanTask: {}, issues size: {}, locale: {}, currentUsername: {}", scanTask.getId(), issues.size(), locale.toLanguageTag(), currentUsername);
        User user = this.userService.findById(userId).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}: ", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, userId)));

        String scanTaskOwnerDisplayName = this.userService.findByUsername(scanTask.getCreatedBy()).map(User::getDisplayName).orElse(scanTask.getCreatedBy());
        Map<String, Object> model = new HashMap<>();
        model.put("displayName", user.getDisplayName());
        model.put("assignUsername", currentUsername);
        model.put("scanTaskOwnerDisplayName", scanTaskOwnerDisplayName);
        model.put("currentUserDisplayName", currentUser.getDisplayName());
        model.put("projectName", scanTask.getProject().getName());
        model.put("issueNumber", issues.size());
        model.put("scanDate", scanTask.getScanStartAt());
        model.put("uiHost", (appProperties.getUiPort() == 80) ? appProperties.getUiHost() : String.format("%s:%d", appProperties.getUiHost(), appProperties.getUiPort()));
        model.put("projectId", scanTask.getProject().getId());

        List<IssueDto> issueDtos = this.convertIssuesToDto(issues, Locale.ENGLISH);
        List<IssueDto> issueDtosCn = this.convertIssuesToDto(issues, Locale.SIMPLIFIED_CHINESE);
        model.put("issues", issueDtos);
        model.put("issuesCn", issueDtosCn);
        model.put("actionMap", retrieveActionMap());
        model.put("severityMap", retrieveSeverityMap());

        AppProperties.Mail mailSetting = settingService.getEmailServerConfiguration();
        String subject = CommonUtil.formatString((Locale.SIMPLIFIED_CHINESE == locale ? "{} {} 分配了{}项任务给你 {}" : "{} {} has assigned {} defects to you {}"), mailSetting.getPrefix(),
                currentUser.getDisplayName(), issues.size(), scanTask.getProject().getName());
        SendEmailRequest request = SendEmailRequest.builder().subject(subject)
                .templateName(ISSUE_ASSIGN_TEMPLATE_PREFIX)
                .model(model)
                .to(user.getEmail())
                .from(mailSetting.getFrom())
                .build();

        emailService.sendTemplateMail(request, currentUsername);
    }

    private static Map<String, String> retrieveActionMap() {
        log.debug("[retrieveActionMap]");
        Map<String, String> actionMap = new HashMap<>();
        actionMap.put("EN_PENDING", "PENDING");
        actionMap.put("EN_OPEN", "OPEN");
        actionMap.put("EN_CONFIRMED", "CONFIRMED");
        actionMap.put("EN_FALSE_POSITIVE", "FALSE_POSITIVE");
        actionMap.put("EN_WAIVED", "WAIVED");
        actionMap.put("EN_CRITICAL", "CRITICAL");

        actionMap.put("CN_PENDING", "待定");
        actionMap.put("CN_OPEN", "开启");
        actionMap.put("CN_CONFIRMED", "确认");
        actionMap.put("CN_FALSE_POSITIVE", "误报");
        actionMap.put("CN_WAIVED", "忽略");
        actionMap.put("CN_CRITICAL", "危急");
        return actionMap;
    }

    private static Map<String, String> retrieveSeverityMap() {
        log.debug("[retrieveSeverityMap]");
        Map<String, String> severityMap = new HashMap<>();

        severityMap.put("EN_LOW", "LOW");
        severityMap.put("EN_MEDIUM", "MEDIUM");
        severityMap.put("EN_HIGH", "HIGH");
        severityMap.put("EN_CRITICAL", "CRITICAL");

        severityMap.put("CN_LOW", "低");
        severityMap.put("CN_MEDIUM", "中");
        severityMap.put("CN_HIGH", "高");
        severityMap.put("CN_CRITICAL", "危急");
        return severityMap;
    }

    public FileInfo saveImportIssueResponseToFile(ImportIssueResponse importIssueResponse, String currentUsername) throws AppException {
        log.info("[saveImportIssueResponseToFile] scanTask, id: {}, issues, size: {}", importIssueResponse.getScanTaskId(), importIssueResponse.getIssues().size());

        FileInfo fileInfo = fileService.writeObjectToFile(importIssueResponse, importIssueResponse.getScanTaskId().toString() + "-result", ".view", currentUsername);
        log.trace("[saveImportIssueResponseToFile] fileInfo: {}", fileInfo);
        return fileInfo;
    }

    public ImportIssueResponse retrieveScanResultFromScanSummary(ScanTask scanTask, Locale locale) throws AppException {
        ImportIssueResponse importIssueResponse = this.retrieveScanResultFromScanSummary(scanTask);
        this.updateImportIssueResponseWithLocale(importIssueResponse, locale);
        return importIssueResponse;
    }

    private void updateImportIssueResponseWithLocale(ImportIssueResponse importIssueResponse, Locale locale) {
        final Map<String, I18nMessage> i18nMessageMap = this.retrieveI18nMessageMapByImportIssueResponse(importIssueResponse, locale);
        importIssueResponse.getIssues().forEach(issue -> {
            issue.setMessage(I18nService.formatString(issue.getMessage(), i18nMessageMap));
            issue.getIssueTraceInfos().forEach(traceInfo -> {
                traceInfo.setMessage(I18nService.formatString(traceInfo.getMessage(), getImportIssueTraceMessageReplacementMap(traceInfo.getIssueTraces()), i18nMessageMap));
                traceInfo.getIssueTraces().forEach(trace -> trace.setMessage(I18nService.formatString(trace.getMessage(), i18nMessageMap)));
            });
        });
    }

    public ImportIssueResponse retrieveScanResultFromScanSummary(ScanTask scanTask) throws AppException {
        ImportIssueResponse importIssueResponse;
        String scanResultId = scanTask.getSummary().get("fileInfo.id.scanResult");
        if (scanResultId != null) {
            UUID id = UUID.fromString(scanResultId);
            Resource resource = this.fileService.getFileAsResource(id);
            try {
                importIssueResponse = om.readValue(resource.getFile(), ImportIssueResponse.class);
            } catch (IOException e) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_DTO_INVALID_CONTENT.unifyErrorCode,
                        CommonUtil.formatString("[{}] scan result file id: {}", AppException.ErrorCode.E_API_COMMON_DTO_INVALID_CONTENT.messageTemplate, scanResultId), e);
            }
        } else {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_DTO_INVALID_CONTENT.unifyErrorCode,
                    CommonUtil.formatString("[{}] scan result file id not found", AppException.ErrorCode.E_API_COMMON_DTO_INVALID_CONTENT.messageTemplate));
        }
        return importIssueResponse;
    }

    public ImportIssueResponse constructImportIssueResponse(ScanTask scanTask, List<Issue> issues) {
        log.info("[constructImportIssueResponse] scanTask, id:{}, issues, size: {}", scanTask.getId(), issues.size());
        ImportIssueResponse importIssueResponse = ImportIssueResponse.builder().scanTaskId(scanTask.getId()).summary(scanTask.getSummary()).build();
        importIssueResponse.setIssues(issues.stream().map(IssueService::convertIssueToIssueOfImportIssueResponse).collect(Collectors.toList()));
        return importIssueResponse;
    }

    public List<IssueDto> convertIssuesToDto(List<Issue> issues, Locale locale) {
        log.debug("[convertIssuesToDto] issues size: {}", issues.size());
        Map<String, I18nMessage> i18nMessageMap = this.retrieveI18nMessageMapByIssues(issues, locale);
        return issues.stream().map(issue -> convertIssueToDto(issue, i18nMessageMap)).collect(Collectors.toList());
    }

    public Map<String, I18nMessage> retrieveI18nMessageMapByIssue(Issue issue, Locale locale) {
        log.debug("[retrieveI18nMessageMapByIssue] issue code: {}, locale: {}", issue.getRuleInformation().getRuleCode(), locale);
        List<String> keyList = new ArrayList<>();
        String suffix = CommonUtil.formatString("{}.{}.{}.{}",
                issue.getRuleInformation().getRuleSet().getScanEngine().getName(),
                issue.getRuleInformation().getRuleSet().getName(),
                issue.getRuleInformation().getRuleSet().getVersion(),
                issue.getRuleInformation().getRuleCode());
        keyList.add("rule.name." + suffix);
        keyList.add("rule.description." + suffix);
        keyList.add("rule.detail." + suffix);
        List<I18nMessage> i18nMessages = this.i18nService.getI18nMessagesByKeys(keyList, locale);
        i18nMessages.addAll(this.i18nService.getI18nMessageByKeyPrefix("rule." + suffix + ".", locale));
        i18nMessages.addAll(this.i18nService.getI18nMessageByKeyPrefix("path.msg.", locale));
        i18nMessages.addAll(this.i18nService.getI18nMessageByKeyPrefix("msg_template.", locale));
        // Convert the list to map with messageKey as key and itself as value, ignore the duplicated entry
        return i18nMessages.stream().collect(Collectors.toMap(I18nMessage::getKey, Function.identity(), (existing, replacement) -> existing));
    }

    public Map<String, I18nMessage> retrieveI18nMessageMapByIssues(List<Issue> issues, Locale locale) {
        log.debug("[retrieveI18nMessageMapByIssues] issue size: {}, locale: {}", issues.size(), locale);
        List<RuleInformation> ruleInformationList = issues.stream().map(Issue::getRuleInformation).distinct().collect(Collectors.toList());
        List<I18nMessage> i18nMessages = this.ruleService.getI18nMessagesByRuleInformationList(ruleInformationList, locale);
        i18nMessages.addAll(this.i18nService.getI18nMessageByKeyPrefix("path.msg.", locale));
        i18nMessages.addAll(this.i18nService.getI18nMessageByKeyPrefix("msg_template.", locale));
        return i18nMessages.stream().collect(Collectors.toMap(I18nMessage::getKey, message -> message));
    }

    public Map<String, I18nMessage> retrieveI18nMessageMapByImportIssueResponse(ImportIssueResponse importIssueResponse, Locale locale) {
        log.debug("[updateImportIssueResponseWithLocaleMessage] importIssueResponse issue size: {}, locale: {}", importIssueResponse.getIssues().size(), locale);
        List<UUID> ruleInformationIds = importIssueResponse.getIssues().stream().map(ImportIssueResponse.Issue::getRuleInformationId).distinct().collect(Collectors.toList());
        List<RuleInformation> ruleInformationList = this.ruleService.findByIds(ruleInformationIds);
        List<I18nMessage> i18nMessages = this.ruleService.getI18nMessagesByRuleInformationList(ruleInformationList, locale);
        i18nMessages.addAll(this.i18nService.getI18nMessageByKeyPrefix("path.msg.", locale));
        i18nMessages.addAll(this.i18nService.getI18nMessageByKeyPrefix("msg_template.", locale));
        return i18nMessages.stream().collect(Collectors.toMap(I18nMessage::getKey, message -> message));
    }

    public IssueDto convertIssueToDto(Issue issue, Locale locale) {
        return this.convertIssueToDto(issue, new ArrayList<>(), locale);
    }

    public IssueDto convertIssueToDto(Issue issue, List<IssueTrace> issueTraces, Locale locale) {
        log.debug("[convertIssueToDto] issue code: {}, issueTrace size: {}, locale: {}", issue.getRuleInformation().getRuleCode(), issueTraces.size(), locale);
        Map<String, I18nMessage> i18nMessageMap = this.retrieveI18nMessageMapByIssue(issue, locale);
        return convertIssueToDto(issue, issueTraces, i18nMessageMap);
    }

    private static List<ImportIssueResponse.Issue.IssueTraceInfo> fillIssueTraceInfosOfIssueOfImportIssueResponse(Issue issue, List<IssueTrace> issueTraces) {
        log.trace("[fillIssueTraceInfosOfIssueOfImportIssueResponse] issue code: {}, issueTrace size: {}", issue.getRuleInformation().getRuleCode(), issueTraces.size());
        List<ImportIssueResponse.Issue.IssueTraceInfo> issueTraceInfos = new ArrayList<>();
        Map<String, List<IssueTrace>> issueTracesMap = issueTraces.stream().collect(Collectors.groupingBy(IssueTrace::getChecksum));
        if (issueTracesMap.keySet().size() > 0) {
            double maxComplexity = issue.getFirstAttribute(VariableUtil.IssueAttributeName.COMPLEXITY_MAX).map(attr -> NumberUtils.toDouble(attr.getValue(), 0.00)).orElse(0.0);
            double minComplexity = issue.getFirstAttribute(VariableUtil.IssueAttributeName.COMPLEXITY_MIN).map(attr -> NumberUtils.toDouble(attr.getValue(), 0.00)).orElse(0.0);
            for (Map.Entry<String, List<IssueTrace>> entry : issueTracesMap.entrySet()) {
                double traceComplexityRate;
                double complexity;
                if (maxComplexity == minComplexity) {
                    complexity = maxComplexity;
                    traceComplexityRate = 10.0;
                } else {
                    complexity = entry.getValue().stream().map(issueTrace -> NumberUtils.toDouble(issueTrace.getComplexity(), 0.0)).findFirst().orElse(0.0);
                    traceComplexityRate = (complexity - minComplexity) / (maxComplexity - minComplexity) * 10.0;
                }

                ImportIssueResponse.Issue.IssueTraceInfo issueTraceInfo = ImportIssueResponse.Issue.IssueTraceInfo.builder()
                        .id(entry.getKey())
                        .noOfTrace(entry.getValue().size())
                        .issueTraces(entry.getValue().stream().map(IssueService::convertIssueTraceToIssueTraceOfIssueOfImportIssueResponse).collect(Collectors.toList()))
                        .complexity(complexity)
                        .complexityRate(traceComplexityRate)
                        .message(issue.getRuleInformation().getMessageTemplate())
                        .build();
                issueTraceInfos.add(issueTraceInfo);
            }
        }
        return issueTraceInfos;
    }

    private static ImportIssueResponse.Issue convertIssueToIssueOfImportIssueResponse(Issue issue, List<IssueTrace> issueTraces) {
        log.trace("[convertIssueToIssueOfImportIssueResponse] issue code: {}, issueTrace size: {}", issue.getRuleInformation().getRuleCode(), issueTraces.size());
        UUID scanFileId = null;
        String relativePath;
        String scanFilePath;
        if (issue.getScanFile() != null) {
            scanFileId = issue.getScanFile().getId();
            relativePath = issue.getScanFile().getProjectRelativePath();
            scanFilePath = issue.getScanFile().getStorePath();
        } else {
            relativePath = issue.getFilePath();
            scanFilePath = issue.getFilePath();
        }

        String certainty = issue.getFirstAttribute(VariableUtil.IssueAttributeName.CERTAINTY).map(IssueAttribute::getValue)
                .orElse(issue.getRuleInformation().getCertainty() != null ? issue.getRuleInformation().getCertainty().toString() : null);
        Double complexityRate = issue.getFirstAttribute(VariableUtil.IssueAttributeName.COMPLEXITY_RATE)
                .map(attr -> NumberUtils.toDouble(attr.getValue(), 0.0))
                .orElse(0.0);

        ImportIssueResponse.Issue issueOfImportIssueResponse = ImportIssueResponse.Issue.builder()
                .id(issue.getId())
                .ruleInformationId(issue.getRuleInformation().getId())
                .issueKey(issue.getIssueKey())
                .seq(issue.getSeq())
                .issueCategory(issue.getRuleInformation().getCategory())
                .ruleSet(issue.getRuleInformation().getRuleSet().getName())
                .vulnerable(issue.getRuleInformation().getVulnerable())
                .certainty(certainty)
                .issueCode(issue.getIssueCode())
                .severity(issue.getSeverity() != null ? issue.getSeverity().toString() : null)
                .priority(issue.getRuleInformation().getPriority() != null ? issue.getRuleInformation().getPriority().toString() : null)
                .likelihood(issue.getRuleInformation().getLikelihood() != null ? issue.getRuleInformation().getLikelihood().toString() : null)
                .remediationCost(issue.getRuleInformation().getRemediationCost() != null ? issue.getRuleInformation().getRemediationCost().toString() : null)
                .scanFileId(scanFileId)
                .relativePath(relativePath)
                .scanFilePath(scanFilePath)
                .lineNo(issue.getLineNo())
                .columnNo(issue.getColumnNo())
                .functionName(issue.getFunctionName())
                .variableName(issue.getVariableName())
                .complexity(issue.getComplexity())
                .complexityRate(complexityRate)
                .checksum(issue.getChecksum())
                .message(issue.getMessage())
                .issueTraceInfos(fillIssueTraceInfosOfIssueOfImportIssueResponse(issue, issueTraces))
                .build();
        log.trace("[convertIssueToIssueOfImportIssueResponse] issueOfImportIssueResponse: {}", issueOfImportIssueResponse);
        return issueOfImportIssueResponse;
    }

    public static IssueDiffDto convertIssueDiffToDto(IssueDiff issueDiff, Map<String, I18nMessage> i18nMessageMap) {
        log.trace("[convertIssueDiffToDto] IssueDiff: {}", issueDiff);
        UUID scanFileId = null;
        String relativePath;
        String scanFilePath;
        if (issueDiff.getIssue().getScanFile() != null) {
            scanFileId = issueDiff.getIssue().getScanFile().getId();
            relativePath = issueDiff.getIssue().getScanFile().getProjectRelativePath();
            scanFilePath = issueDiff.getIssue().getScanFile().getStorePath();
        } else {
            relativePath = issueDiff.getIssue().getFilePath();
            scanFilePath = issueDiff.getIssue().getFilePath();
        }
        String certainty = issueDiff.getIssue().getFirstAttribute(VariableUtil.IssueAttributeName.CERTAINTY).map(IssueAttribute::getValue)
                .orElse(Optional.of(issueDiff).map(IssueDiff::getIssue).map(Issue::getRuleInformation).map(RuleInformation::getCertainty).map(RuleInformation.Certainty::toString).orElse(null));
        IssueDiffDto result = IssueDiffDto.builder()
                .id(issueDiff.getId())
                .baselineScanTaskId(Optional.of(issueDiff).map(IssueDiff::getBaselineScanTask).map(ScanTask::getId).orElse(null))
                .scanTaskId(Optional.of(issueDiff).map(IssueDiff::getScanTask).map(ScanTask::getId).orElse(null))
                .issueId(issueDiff.getIssue().getId())
                .issueKey(issueDiff.getIssue().getIssueKey())
                .checksum(issueDiff.getChecksum())
                .type(issueDiff.getType().name())
                .issueCategory(issueDiff.getIssue().getRuleInformation().getCategory())
                .ruleSet(issueDiff.getIssue().getRuleInformation().getRuleSet().getName())
                .vulnerable(issueDiff.getIssue().getRuleInformation().getVulnerable())
                .certainty(certainty)
                .issueCode(issueDiff.getIssue().getIssueCode())
                .critical(Issue.Action.CRITICAL == issueDiff.getIssue().getAction() ? issueDiff.getIssue().getAction().toString() : null)
                .severity(issueDiff.getIssue().getSeverity() != null ? issueDiff.getIssue().getSeverity().toString() : null)
                .likelihood(issueDiff.getIssue().getRuleInformation().getLikelihood() != null ? issueDiff.getIssue().getRuleInformation().getLikelihood().toString() : null)
                .remediationCost(issueDiff.getIssue().getRuleInformation().getRemediationCost() != null ? issueDiff.getIssue().getRuleInformation().getRemediationCost().toString() : null)
                .scanFileId(scanFileId)
                .relativePath(relativePath)
                .scanFilePath(scanFilePath)
                .lineNo(issueDiff.getIssue().getLineNo())
                .columnNo(issueDiff.getIssue().getColumnNo())
                .functionName(issueDiff.getIssue().getFunctionName())
                .variableName(issueDiff.getIssue().getVariableName())
                .issue(convertIssueToDto(issueDiff.getIssue(), i18nMessageMap))
                .createdBy(issueDiff.getCreatedBy())
                .createdOn(issueDiff.getCreatedOn())
                .modifiedBy(issueDiff.getModifiedBy())
                .modifiedOn(issueDiff.getModifiedOn())
                .build();
        log.trace("[convertIssueDiffToDto] result: {}", result);
        return result;
    }

    public static ImportIssueResponse.Issue convertIssueToIssueOfImportIssueResponse(Issue issue) {
        return convertIssueToIssueOfImportIssueResponse(issue, issue.getIssueTraces());
    }

    public static IssueDto convertIssueToDto(Issue issue, Map<String, I18nMessage> i18nMessageMap) {
        return convertIssueToDto(issue, new ArrayList<>(), i18nMessageMap);
    }

    public static IssueDto convertIssueToDto(Issue issue, List<IssueTrace> issueTraces, Map<String, I18nMessage> i18nMessageMap) {
        log.trace("[convertIssueToDto] issue id: {}, issue code: {}", issue.getId(), issue.getIssueCode());
        UUID scanFileId = null;
        String relativePath;
        String scanFilePath;
        if (issue.getScanFile() != null) {
            scanFileId = issue.getScanFile().getId();
            relativePath = issue.getScanFile().getProjectRelativePath();
            scanFilePath = issue.getScanFile().getStorePath();
        } else {
            relativePath = issue.getFilePath();
            scanFilePath = issue.getFilePath();
        }

        String ruleInformationName = I18nService.formatString(issue.getRuleInformation().getName(), null, i18nMessageMap);
        String ruleInformationDescription = I18nService.formatString(issue.getRuleInformation().getDescription(), null, i18nMessageMap);
        String ruleInformationDetail = I18nService.formatString(issue.getRuleInformation().getDetail(), null, i18nMessageMap);
        // only replace once, get message
        String issueMessage = I18nService.formatString(issue.getMessage(), null, i18nMessageMap, 1);

        String certainty = issue.getFirstAttribute(VariableUtil.IssueAttributeName.CERTAINTY).map(IssueAttribute::getValue)
                .orElse(issue.getRuleInformation().getCertainty() != null ? issue.getRuleInformation().getCertainty().toString() : null);
        Double complexityRate = issue.getFirstAttribute(VariableUtil.IssueAttributeName.COMPLEXITY_RATE)
                .map(attr -> NumberUtils.toDouble(attr.getValue(), 0.0))
                .orElse(0.0);
        IssueDto issueDto = IssueDto.builder()
                .id(issue.getId())
                .ruleInformation(
                        IssueDto.RuleInformation.builder()
                                .id(issue.getRuleInformation().getId())
                                .ruleSet(issue.getRuleInformation().getRuleSet().getName())
                                .ruleSetVersion(issue.getRuleInformation().getRuleSet().getVersion())
                                .ruleSetDisplayName(issue.getRuleInformation().getRuleSet().getDisplayName())
                                .scanEngineName(issue.getRuleInformation().getRuleSet().getScanEngine().getName())
                                .scanEngineVersion(issue.getRuleInformation().getRuleSet().getScanEngine().getVersion())
                                .ruleCode(issue.getRuleInformation().getRuleCode())
                                .category(issue.getRuleInformation().getCategory())
                                .vulnerable(issue.getRuleInformation().getVulnerable())
                                .name(ruleInformationName)
                                .certainty(issue.getRuleInformation().getCertainty() != null ? issue.getRuleInformation().getCertainty().name() : null)
                                .priority(issue.getRuleInformation().getPriority() != null ? issue.getRuleInformation().getPriority().name() : null)
                                .severity(issue.getSeverity() != null ? issue.getSeverity().name() : null)
                                .likelihood(issue.getRuleInformation().getLikelihood() != null ? issue.getRuleInformation().getLikelihood().name() : null)
                                .remediationCost(issue.getRuleInformation().getRemediationCost() != null ? issue.getRuleInformation().getRemediationCost().name() : null)
                                .language(issue.getRuleInformation().getLanguage())
                                .url(issue.getRuleInformation().getUrl())
                                .detail(ruleInformationDetail)
                                .description(ruleInformationDescription)
                                .attributes(issue.getRuleInformation().getAttributes())
                                .build()
                )
                .issueKey(issue.getIssueKey())
                .seq(issue.getSeq())
                .issueCategory(issue.getRuleInformation().getCategory())
                .ruleSet(issue.getRuleInformation().getRuleSet().getName())
                .vulnerable(issue.getRuleInformation().getVulnerable())
                .certainty(certainty)
                .issueCode(issue.getIssueCode())
                .issueName(ruleInformationName)
                .critical(Issue.Action.CRITICAL == issue.getAction() ? issue.getAction().toString() : null)
                .severity(issue.getSeverity() != null ? issue.getSeverity().toString() : null)
                .likelihood(issue.getRuleInformation().getLikelihood() != null ? issue.getRuleInformation().getLikelihood().toString() : null)
                .remediationCost(issue.getRuleInformation().getRemediationCost() != null ? issue.getRuleInformation().getRemediationCost().toString() : null)
                .scanFileId(scanFileId)
                .relativePath(relativePath)
                .scanFilePath(scanFilePath)
                .lineNo(issue.getLineNo())
                .columnNo(issue.getColumnNo())
                .functionName(issue.getFunctionName())
                .variableName(issue.getVariableName())
                .complexity(issue.getComplexity())
                .complexityRate(complexityRate)
                .checksum(issue.getChecksum())
                .message(issueMessage)
                .ignored(issue.getIgnored())
                .status(issue.getStatus().toString())
                .action(issue.getAction().toString())
                .createdBy(issue.getCreatedBy())
                .createdOn(issue.getCreatedOn())
                .modifiedBy(issue.getModifiedBy())
                .modifiedOn(issue.getModifiedOn())
                .assignTo(issue.getAssignTo() != null ?
                        IssueDto.AssignTo.builder()
                                .id(issue.getAssignTo().getId())
                                .displayName(issue.getAssignTo().getDisplayName())
                                .email(issue.getAssignTo().getEmail())
                                .build() : null)
                .issueTraces(new ArrayList<>())
                .issueAttributes(new ArrayList<>())
                .build();

        Map<String, List<IssueTrace>> issueTracesMap = issueTraces.stream().sorted(Comparator.comparingInt(IssueTrace::getSeq)).collect(Collectors.groupingBy(IssueTrace::getChecksum));
        Optional<String> firstChecksum = issueTracesMap.keySet().stream().sorted().findFirst();
        if (firstChecksum.isPresent()) {
            List<IssueTrace> it = issueTracesMap.get(firstChecksum.get());
            for (IssueTrace issueTrace : it) {
                issueDto.getIssueTraces().add(convertIssueTraceToDto(issueTrace, i18nMessageMap));
            }
            double maxComplexity = issue.getFirstAttribute(VariableUtil.IssueAttributeName.COMPLEXITY_MAX).map(attr -> NumberUtils.toDouble(attr.getValue(), 0.00)).orElse(0.0);
            double minComplexity = issue.getFirstAttribute(VariableUtil.IssueAttributeName.COMPLEXITY_MIN).map(attr -> NumberUtils.toDouble(attr.getValue(), 0.00)).orElse(0.0);
            for (Map.Entry<String, List<IssueTrace>> entry : issueTracesMap.entrySet()) {
                double traceComplexityRate;
                double complexity;
                if (maxComplexity == minComplexity) {
                    complexity = maxComplexity;
                    traceComplexityRate = 10.0;
                } else {
                    complexity = entry.getValue().stream().map(issueTrace -> NumberUtils.toDouble(issueTrace.getComplexity(), 0.0)).findFirst().orElse(0.0);
                    traceComplexityRate = (complexity - minComplexity) / (maxComplexity - minComplexity) * 10.0;
                }
                String ruleInformationMessageTemplate = I18nService.formatString(issue.getRuleInformation().getMessageTemplate(),
                        getIssueTraceMessageReplacementMap(entry.getValue()), i18nMessageMap);
                IssueDto.IssueTraceInfo issueTraceInfo = IssueDto.IssueTraceInfo.builder()
                        .id(entry.getKey())
                        .noOfTrace(entry.getValue().size())
                        .issueTraces(entry.getValue().stream().map(trace -> IssueService.convertIssueTraceToDto(trace, i18nMessageMap)).collect(Collectors.toList()))
                        .complexity(complexity)
                        .complexityRate(traceComplexityRate)
                        .message(ruleInformationMessageTemplate)
                        .build();
                issueDto.getIssueTraceInfos().add(issueTraceInfo);
            }
        }

        for (IssueAttribute issueAttribute : issue.getAttributes()) {
            IssueDto.IssueAttribute ia = IssueDto.IssueAttribute.builder().name(EnumUtils.getEnumIgnoreCase(VariableUtil.IssueAttributeName.class, issueAttribute.getName().name())).value(issueAttribute.getValue()).build();
            issueDto.getIssueAttributes().add(ia);
        }
        return issueDto;
    }

    public static ImportIssueResponse.Issue.IssueTrace convertIssueTraceToIssueTraceOfIssueOfImportIssueResponse(IssueTrace issueTrace) {
        log.trace("[convertIssueTraceToIssueTraceOfIssueOfImportIssueResponse] issueTrace, id: {}", issueTrace.getId());
        UUID traceScanFileId = null;
        String traceRelativePath;
        String traceScanFilePath;
        Long scanFileSize = 0L;
        Integer scanFileNoOfLines = 0;
        if (issueTrace.getScanFile() != null) {
            traceScanFileId = issueTrace.getScanFile().getId();
            traceRelativePath = issueTrace.getScanFile().getProjectRelativePath();
            traceScanFilePath = issueTrace.getScanFile().getStorePath();
            if (issueTrace.getScanFile().getFileInfo() != null) {
                scanFileSize = issueTrace.getScanFile().getFileInfo().getFileSize();
                scanFileNoOfLines = issueTrace.getScanFile().getFileInfo().getNoOfLines();
            }
        } else {
            traceRelativePath = issueTrace.getFilePath();
            traceScanFilePath = issueTrace.getFilePath();
        }
        ImportIssueResponse.Issue.IssueTrace issueTraceOfImportIssueResponse = ImportIssueResponse.Issue.IssueTrace.builder()
                .id(issueTrace.getId())
                .seq(issueTrace.getSeq())
                .scanFileId(traceScanFileId)
                .relativePath(traceRelativePath)
                .scanFilePath(traceScanFilePath)
                .lineNo(issueTrace.getLineNo())
                .columnNo(issueTrace.getColumnNo())
                .functionName(issueTrace.getFunctionName())
                .variableName(issueTrace.getVariableName())
                .checksum(issueTrace.getChecksum())
                .message(issueTrace.getMessage())
                .scanFileSize(scanFileSize)
                .scanFileNoOfLines(scanFileNoOfLines)
                .build();
        log.trace("[convertIssueTraceToIssueTraceOfIssueOfImportIssueResponse] ImportIssueResponse.Issue.IssueTrace: {}", issueTraceOfImportIssueResponse);
        return issueTraceOfImportIssueResponse;
    }

    private static IssueDto.IssueTrace convertIssueTraceToDto(IssueTrace issueTrace, Map<String, I18nMessage> i18nMessageMap) {
        log.trace("[convertIssueTraceToDto] issueTrace, id: {}", issueTrace.getId());
        UUID traceScanFileId = null;
        String traceRelativePath;
        String traceScanFilePath;
        Long scanFileSize = 0L;
        Integer scanFileNoOfLines = 0;
        if (issueTrace.getScanFile() != null) {
            traceScanFileId = issueTrace.getScanFile().getId();
            traceRelativePath = issueTrace.getScanFile().getProjectRelativePath();
            traceScanFilePath = issueTrace.getScanFile().getStorePath();
            if (issueTrace.getScanFile().getFileInfo() != null) {
                scanFileSize = issueTrace.getScanFile().getFileInfo().getFileSize();
                scanFileNoOfLines = issueTrace.getScanFile().getFileInfo().getNoOfLines();
            }
        } else {
            traceRelativePath = issueTrace.getFilePath();
            traceScanFilePath = issueTrace.getFilePath();
        }
        String message = I18nService.formatString(issueTrace.getMessage(), i18nMessageMap);
        IssueDto.IssueTrace itDto = IssueDto.IssueTrace.builder()
                .id(issueTrace.getId())
                .seq(issueTrace.getSeq())
                .scanFileId(traceScanFileId)
                .relativePath(traceRelativePath)
                .scanFilePath(traceScanFilePath)
                .lineNo(issueTrace.getLineNo())
                .columnNo(issueTrace.getColumnNo())
                .functionName(issueTrace.getFunctionName())
                .variableName(issueTrace.getVariableName())
                .checksum(issueTrace.getChecksum())
                .message(message)
                .scanFileSize(scanFileSize)
                .scanFileNoOfLines(scanFileNoOfLines)
                .build();
        log.trace("[convertIssueTraceToDto] IssueDto.IssueTrace: {}", itDto);
        return itDto;
    }

    public List<IssueSummaryResponse.AssignSummary> findIssueSummaryCountByUser(UUID scanTaskId) {
        log.info("[findIssueSummaryCountByUser] scanTaskId: {}", scanTaskId);
        return issueRepository.findIssueSummaryCountByUser(scanTaskId);
    }

    public Page<Issue> searchIssue(
            ScanTask scanTask,
            UUID ruleSetId,
            String ruleSetName,
            String seq,
            List<SearchIssueRequest.IssueAttribute> requestIssueAttributes,
            List<SearchIssueRequest.RuleInformationAttribute> requestRuleInformationAttributes,
            List<UUID> ruleInformationIds,
            List<UUID> scanFileIds,
            SearchIssueRequest.SearchIssueType searchIssueType,
            Pageable pageable
    ) {
        Map<VariableUtil.IssueAttributeName, List<String>> issueAttributeMap = new EnumMap<>(VariableUtil.IssueAttributeName.class);
        for (SearchIssueRequest.IssueAttribute attr : requestIssueAttributes) {
            VariableUtil.IssueAttributeName enumAttribute = EnumUtils.getEnumIgnoreCase(VariableUtil.IssueAttributeName.class, attr.getName());
            if (enumAttribute != null) {
                issueAttributeMap.put(enumAttribute, attr.getValues());
            }
        }

        Map<VariableUtil.RuleAttributeTypeName, List<String>> ruleAttributeMap = new EnumMap<>(VariableUtil.RuleAttributeTypeName.class);
        for (SearchIssueRequest.RuleInformationAttribute attr : requestRuleInformationAttributes) {
            VariableUtil.RuleAttributeTypeName enumAttribute = EnumUtils.getEnumIgnoreCase(VariableUtil.RuleAttributeTypeName.class, attr.getName());
            if (enumAttribute != null) {
                ruleAttributeMap.put(enumAttribute, attr.getValues());
            }
        }

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    pageable.getSort().and(Sort.by(Sort.Direction.ASC, "seq"))
            );
        }

        List<ScanFile> scanFiles = this.scanFileService.findByScanFileIds(scanFileIds);

        Page<Issue> issues = new RestResponsePage<>();
        switch (searchIssueType) {
            case ONLY_PROJECT:
                issues = this.issueRepository.searchIssueOnlyProjectFile(
                        scanTask,
                        ruleSetId,
                        ruleSetName,
                        seq,
                        issueAttributeMap,
                        ruleAttributeMap,
                        ruleInformationIds,
                        scanFiles,
                        pageable
                );
                break;
            case ONLY_NON_PROJECT:
                if (scanFileIds.isEmpty()) {
                    issues = this.issueRepository.searchIssueOnlyNonProjectFile(
                            scanTask,
                            ruleSetId,
                            ruleSetName,
                            seq,
                            issueAttributeMap,
                            ruleAttributeMap,
                            ruleInformationIds,
                            pageable
                    );
                }
                break;
            default:
                issues = this.issueRepository.searchIssue(
                        scanTask,
                        ruleSetId,
                        ruleSetName,
                        seq,
                        issueAttributeMap,
                        ruleAttributeMap,
                        ruleInformationIds,
                        scanFiles,
                        pageable
                );
                break;
        }

        return issues;
    }

    public IssueStatisticsResponse calcIssueStatistics(ScanTask scanTask, List<ScanFile> scanFiles) {
        List<Issue> issueList = CollectionUtils.isEmpty(scanFiles)
                ? this.issueRepository.findByScanTask(scanTask)
                : this.issueRepository.findByScanTaskAndScanFileIn(scanTask, scanFiles);

        Map<String, IssueStatisticsResponse.RuleSetStatistics> ruleSetStatistics = new HashMap<>();
        for (Issue issue : issueList) {
            String ruleSetName = issue.getRuleInformation().getRuleSet().getName();
            if (!ruleSetStatistics.containsKey(ruleSetName)) {
                ruleSetStatistics.put(ruleSetName, new IssueStatisticsResponse.RuleSetStatistics());
            }

            String priorityName = issue.getRuleInformation().getPriority().toString();
            IssueStatisticsResponse.RuleSetStatistics statistics = ruleSetStatistics.get(ruleSetName);
            statistics.addPriority(priorityName, 1);

            String ruleCode = issue.getRuleInformation().getRuleCode();
            if (!statistics.getRule().containsKey(ruleCode)) {
                statistics.getRule().put(ruleCode, new IssueStatisticsResponse.RuleStatistics());
            }

            statistics.getRule().get(ruleCode).addPriority(priorityName, 1);
        }

        return IssueStatisticsResponse.builder()
                .issueCount(issueList.size())
                .ruleSet(ruleSetStatistics)
                .build();
    }

    public Long countIssueByAction(ScanTask scanTask, Issue.Action... actions) {
        log.info("[countIssueByAction] scanTask, id: {}, actions: {}", scanTask.getId(), actions);
        return this.issueRepository.countByScanTaskAndActionIn(scanTask, Arrays.asList(actions));
    }

    public Long countIssueByAction(ScanTask scanTask, RuleSet ruleSet, Issue.Action... actions) {
        log.info("[countIssueByAction] scanTask, id: {}, ruleSetId: {}, actions: {}", scanTask.getId(), ruleSet.getId(), actions);
        return this.issueRepository.countByScanTaskAndRuleInformationRuleSetAndActionIn(scanTask, ruleSet, Arrays.asList(actions));
    }

    public Long countIssueByRuleInformationAndAction(ScanTask scanTask, List<RuleInformation> ruleInformation, Issue.Action... actions) {
        log.info("[countIssueByAction] scanTask, id: {}, ruleInformation size: {}, actions: {}", scanTask.getId(), ruleInformation.size(), actions);
        return this.issueRepository.countByScanTaskAndRuleInformationInAndActionIn(scanTask, ruleInformation, Arrays.asList(actions));
    }

    public Long countIssueByPriority(ScanTask scanTask, RuleSet ruleSet, RuleInformation.Priority... priorities) {
        log.info("[countIssueByAction] scanTask, id: {}, ruleSetId: {}, priorities: {}", scanTask.getId(), ruleSet.getId(), priorities);
        return this.issueRepository.countByScanTaskAndRuleInformationRuleSetAndRuleInformationPriorityIn(scanTask, ruleSet, Arrays.asList(priorities));
    }

    public Long countIssueByPriorityAndAction(ScanTask scanTask, RuleSet ruleSet, RuleInformation.Priority priority, Issue.Action action) {
        log.info("[countIssueByAction] scanTask, id: {}, ruleSetId: {}, priority: {}, action: {}", scanTask.getId(), ruleSet.getId(), priority, action);
        return this.issueRepository.countByScanTaskAndRuleInformationRuleSetAndRuleInformationPriorityAndAction(scanTask, ruleSet, priority, action);
    }

    public Long countIssueByRuleInformation(ScanTask scanTask, List<RuleInformation> ruleInformation) {
        log.info("[countIssueByAction] scanTask, id: {}, ruleInformation size: {}", scanTask.getId(), ruleInformation.size());
        return this.issueRepository.countByScanTaskAndRuleInformationIn(scanTask, ruleInformation);
    }

    public CompareScanResultResponse compareScanResult(UUID fromScanTaskId, UUID toScanTaskId) {
        log.info("[compareScanResult] from scan task id : {}, to scan task id : {}", fromScanTaskId, toScanTaskId);
        List<CompareIssueObject> fromCompareIssueObject = issueRepository.findCompareIssueObjectByScanTaskId(fromScanTaskId);
        List<CompareIssueObject> toCompareIssueObject = issueRepository.findCompareIssueObjectByScanTaskId(toScanTaskId);
        List<String> fromIssueKeys = fromCompareIssueObject.stream().map(CompareIssueObject::getIssueKey).collect(Collectors.toList());
        List<String> toIssueKeys = toCompareIssueObject.stream().map(CompareIssueObject::getIssueKey).collect(Collectors.toList());
        List<UUID> newIssueIds = toCompareIssueObject.stream().filter(ik -> !fromIssueKeys.contains(ik.getIssueKey())).map(CompareIssueObject::getId).collect(Collectors.toList());
        List<UUID> fixedIssueIds = fromCompareIssueObject.stream().filter(ik -> !toIssueKeys.contains(ik.getIssueKey())).map(CompareIssueObject::getId).collect(Collectors.toList());
        return CompareScanResultResponse.builder().newIssueIds(newIssueIds).fixedIssueIds(fixedIssueIds).build();
    }

    public List<Issue> findIssuesByIds(List<UUID> issueIds) {
        log.info("[findIssuesByIds] issue ids: {}", issueIds);
        return issueRepository.findByIdIn(issueIds);
    }

    public List<IssueTrace> findByIssueAndChecksum(Issue issue, String checksum) {
        log.info("[findIssuesByIds] issue ids: {}, checksum: {}", issue.getId(), checksum);
        return this.issueTraceRepository.findByIssueAndChecksum(issue, checksum);
    }

    private static Map<String, String> getIssueTraceMessageReplacementMap(List<IssueTrace> issueTraces) {
        Map<String, String> result = new HashMap<>();
        if (issueTraces.size() > 0) {
            int sourceSeq = issueTraces.stream().map(IssueTrace::getSeq).min(Integer::compareTo).orElse(0);
            int sinkSeq = issueTraces.stream().map(IssueTrace::getSeq).max(Integer::compareTo).orElse(0);
            log.trace("[getIssueTraceMessageReplacementMap] issueTraces, size: {}, sourceSeq: {}, sinkSeq: {}", issueTraces.size(), sourceSeq, sinkSeq);
            for (IssueTrace issueTrace : issueTraces) {
                if (sourceSeq == issueTrace.getSeq()) {
                    result.put("ss.filename", StringUtils.defaultString(issueTrace.getFilePath(), "${msg_template.unknown_filename}"));
                    result.put("ss.file", StringUtils.defaultString(issueTrace.getFilePath(), "${msg_template.unknown_file}"));
                    result.put("ss.line", StringUtils.defaultString(String.valueOf(issueTrace.getLineNo()), "${msg_template.unknown_line}"));
                    result.put("ss.func", StringUtils.defaultString(issueTrace.getFunctionName(), "${msg_template.unknown_function}"));
                    result.put("ss.var", StringUtils.defaultString(issueTrace.getVariableName(), "${msg_template.unknown_variable}"));
                }
                if (sinkSeq == issueTrace.getSeq()) {
                    result.put("se.filename", StringUtils.defaultString(issueTrace.getFilePath(), "${msg_template.unknown_filename}"));
                    result.put("se.file", StringUtils.defaultString(issueTrace.getFilePath(), "${msg_template.unknown_file}"));
                    result.put("se.line", StringUtils.defaultString(String.valueOf(issueTrace.getLineNo()), "${msg_template.unknown_line}"));
                    result.put("se.func", StringUtils.defaultString(issueTrace.getFunctionName(), "${msg_template.unknown_function}"));
                    result.put("se.var", StringUtils.defaultString(issueTrace.getVariableName(), "${msg_template.unknown_variable}"));
                }
                String prefix = "s" + issueTrace.getSeq();
                result.put(prefix + ".filename", StringUtils.defaultString(issueTrace.getFilePath(), "${msg_template.unknown_filename}"));
                result.put(prefix + ".file", StringUtils.defaultString(issueTrace.getFilePath(), "${msg_template.unknown_file}"));
                result.put(prefix + ".line", StringUtils.defaultString(String.valueOf(issueTrace.getLineNo()), "${msg_template.unknown_line}"));
                result.put(prefix + ".func", StringUtils.defaultString(issueTrace.getFunctionName(), "${msg_template.unknown_function}"));
                result.put(prefix + ".var", StringUtils.defaultString(issueTrace.getVariableName(), "${msg_template.unknown_variable}"));
            }
        }
        return result;
    }

    private static Map<String, String> getImportIssueTraceMessageReplacementMap(List<ImportIssueResponse.Issue.IssueTrace> issueTraces) {
        Map<String, String> result = new HashMap<>();
        if (issueTraces.size() > 0) {
            int sourceSeq = issueTraces.stream().map(ImportIssueResponse.Issue.IssueTrace::getSeq).min(Integer::compareTo).orElse(0);
            int sinkSeq = issueTraces.stream().map(ImportIssueResponse.Issue.IssueTrace::getSeq).max(Integer::compareTo).orElse(0);
            log.trace("[getIssueTraceMessageReplacementMap] issueTraces, size: {}, sourceSeq: {}, sinkSeq: {}", issueTraces.size(), sourceSeq, sinkSeq);
            for (ImportIssueResponse.Issue.IssueTrace issueTrace : issueTraces) {
                if (sourceSeq == issueTrace.getSeq()) {
                    result.put("ss.filename", StringUtils.defaultString(issueTrace.getScanFilePath(), "${msg_template.unknown_filename}"));
                    result.put("ss.file", StringUtils.defaultString(issueTrace.getScanFilePath(), "${msg_template.unknown_file}"));
                    result.put("ss.line", StringUtils.defaultString(String.valueOf(issueTrace.getLineNo()), "${msg_template.unknown_line}"));
                    result.put("ss.func", StringUtils.defaultString(issueTrace.getFunctionName(), "${msg_template.unknown_function}"));
                    result.put("ss.var", StringUtils.defaultString(issueTrace.getVariableName(), "${msg_template.unknown_variable}"));
                }
                if (sinkSeq == issueTrace.getSeq()) {
                    result.put("se.filename", StringUtils.defaultString(issueTrace.getScanFilePath(), "${msg_template.unknown_filename}"));
                    result.put("se.file", StringUtils.defaultString(issueTrace.getScanFilePath(), "${msg_template.unknown_file}"));
                    result.put("se.line", StringUtils.defaultString(String.valueOf(issueTrace.getLineNo()), "${msg_template.unknown_line}"));
                    result.put("se.func", StringUtils.defaultString(issueTrace.getFunctionName(), "${msg_template.unknown_function}"));
                    result.put("se.var", StringUtils.defaultString(issueTrace.getVariableName(), "${msg_template.unknown_variable}"));
                }
                String prefix = "s" + issueTrace.getSeq();
                result.put(prefix + ".filename", StringUtils.defaultString(issueTrace.getScanFilePath(), "${msg_template.unknown_filename}"));
                result.put(prefix + ".file", StringUtils.defaultString(issueTrace.getScanFilePath(), "${msg_template.unknown_file}"));
                result.put(prefix + ".line", StringUtils.defaultString(String.valueOf(issueTrace.getLineNo()), "${msg_template.unknown_line}"));
                result.put(prefix + ".func", StringUtils.defaultString(issueTrace.getFunctionName(), "${msg_template.unknown_function}"));
                result.put(prefix + ".var", StringUtils.defaultString(issueTrace.getVariableName(), "${msg_template.unknown_variable}"));
            }
        }
        return result;
    }

    public List<IssueDiff> getIssueDiff(ScanTask scanTask) {
        return this.issueDiffRepository.findByScanTask(scanTask);
    }

    public List<IssueTrace> findIssueTraceByIssueGroupByChecksum(Issue issue, Pageable pageable) {
        return this.issueTraceRepository.findByIssueGroupByChecksum(issue.getId(), pageable.getOffset(), pageable.getPageSize());
    }

    public long countIssueTraceByIssueGroupByChecksum(Issue issue) {
        return this.issueTraceRepository.countByIssueGroupByChecksum(issue.getId());
    }

    public Optional<IssueGroup> findIssueGroupStartWith(String prefix) {
        return this.issueGroupDao.findIssueGroupStartWith(prefix);
    }

    public Set<String> findFilePathContainsIssue(ScanTask scanTask) throws AppException {
        log.info("[findFilePathContainsIssue] scanTask, id: {}", scanTask.getId());
        Set<String> filePaths = new HashSet<>();

        // get src/sink file paths from issue group
        List<IssueGroupSrcSinkFilePath> issueGroupSrcSinkFilePathList = issueGroupDao.getIssueGroupSrcSinkFilePathListByScanTaskId(scanTask.getId());
        log.info("[findFilePathContainsIssue] issue group src sink file path list size: {}", issueGroupSrcSinkFilePathList.size());
        for(IssueGroupSrcSinkFilePath issueGroupSrcSinkFilePath: issueGroupSrcSinkFilePathList) {
            if (StringUtils.isNotBlank(issueGroupSrcSinkFilePath.getSrcRelativePath())) {
                filePaths.add(issueGroupSrcSinkFilePath.getSrcRelativePath().replaceAll("^(\\$[ht])?/", ""));
            }
            if (StringUtils.isNotBlank(issueGroupSrcSinkFilePath.getSinkRelativePath())) {
                filePaths.add(issueGroupSrcSinkFilePath.getSinkRelativePath().replaceAll("^(\\$[ht])?/", ""));
            }
        }

        // get file paths from issue trace path: only for cross file scan.
        if(EnumUtils.getEnumIgnoreCase(VariableUtil.ScanMode.class, scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.SCAN_MODE, "")) == VariableUtil.ScanMode.CROSS) {
            List<com.xcal.api.entity.v3.Issue> issues = this.issueMapperDao.findByScanTaskId(scanTask.getId());
            List<IssueFile> issueFiles = this.issueFileDao.getIssueFileList(scanTask.getId());
            Map<Integer, String> issueFileMap = issueFiles.stream().collect(Collectors.toMap(IssueFile::getId, IssueFile::getPath));

            ObjectMapper om = new ObjectMapper();
            JavaType type = om.getTypeFactory().constructParametricType(List.class, Trace.class);

            for(com.xcal.api.entity.v3.Issue issue: issues) {
                try {
                    List<Trace> tracePath = om.readValue(issue.getTracePath(), type);
                    for(Trace trace: tracePath) {
                        filePaths.add(issueFileMap.getOrDefault(trace.getFileId(), "").replaceAll("^(\\$[ht])?/", ""));
                    }
                } catch (IOException e) {
                    log.error("[findFilePathContainsIssue] parse issue trace path failed: {}: {}", e.getClass(), e.getMessage());
                    log.error("[findFilePathContainsIssue] error stack trace info: {}", Arrays.toString(e.getStackTrace()));
                    // TODO: need to add new error code here
                    throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.unifyErrorCode,
                            CommonUtil.formatString("[{}]  issue: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.messageTemplate, issue.getTracePath()),e);
                }
            }
        }

        log.debug("[findFilePathContainsIssue] number of files that contains issue: {}", filePaths.size());

        return filePaths;
    }

    @Async
    public void deleteSourceCodeFileWithoutIssue(ScanTask scanTask, String currentUsername) {
        log.info("[deleteSourceCodeFileWithoutIssue] scanTaskId: {}, currentUsername: {}", scanTask.getId(), currentUsername);

        List<ScanFile> scanFiles = scanFileService.findScanFileByScanTaskAndType(scanTask, ScanFile.Type.FILE);
        log.debug("[deleteSourceCodeFileWithoutIssue] size of scan file path list: {}", scanFiles.size());
        Set<String> scanFilePaths = scanFiles.stream().map(scanFile -> scanFile.getProjectRelativePath()).collect(Collectors.toSet());
        log.debug("[deleteSourceCodeFileWithoutIssue] size of scan file path set: {}", scanFilePaths.size());

        Set<String> filePaths = null;
        try {
            filePaths = this.findFilePathContainsIssue(scanTask);
        } catch (AppException e) {
            log.error("[deleteSourceCodeFileWithoutIssue] find file path contains issue failed: {} {}", e.getErrorCode(), e.getStackTraceString());
            e.printStackTrace();
        }
        log.debug("[deleteSourceCodeFileWithoutIssue] size of file path set contains issue: {}", filePaths.size());


        scanFilePaths.removeAll(filePaths);
        log.debug("[deleteSourceCodeFileWithoutIssue] after remove file path set contains issue, size of scan file path without issue set: {}", scanFilePaths.size());

        List<FileInfo> fileInfoList = scanFiles.stream().filter(scanFile -> scanFilePaths.contains(scanFile.getProjectRelativePath())).map(scanFile -> scanFile.getFileInfo()).collect(Collectors.toList());
        for(FileInfo fileInfo: fileInfoList) {
            try {
                fileService.deleteFile(fileInfo, currentUsername);
            } catch (AppException e) {
                log.error("[deleteSourceCodeFileWithoutIssue] delete source code file failed: {} {}", e.getErrorCode(), e.getStackTraceString());
                e.printStackTrace();
            }
        }
    }


}
