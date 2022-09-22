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

import com.xcal.api.entity.Project;
import com.xcal.api.entity.ProjectConfig;
import com.xcal.api.entity.ProjectConfigAttribute;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.exception.AppException;
import com.xcal.api.exception.BusinessException;
import com.xcal.api.util.*;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ImportService {

    public static final String PHASE_POST_INJECT_DB = "POST_INJECT_DB";
    public static final String POSTPROC_DONE = "postproc-done";

    @NonNull ScanTaskService scanTaskService;
    @NonNull IssueService issueService;
    @NonNull Tracer tracer;

    @Value("${app.scan.volume.path}")
    public String scanVolumePath;

    @Value("${recovery-policy.path}")
    public String recoveryPolicyPath;


    @Transactional(propagation = Propagation.REQUIRES_NEW) //Only rollback for runtime exception
    public void syncImportScanResult(File inputFile, ScanTask scanTask, UUID fileInfoId, String username) throws AppException, IOException {

        long startTime = System.nanoTime();
        //Validation
        if (scanTask == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate);
        }

        Project project = scanTask.getProject();
        if (project == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate);
        }

        ProjectConfig projectConfig = scanTask.getProjectConfig();
        if (projectConfig == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate);
        }

        List<ProjectConfigAttribute> projectConfigAttributeList = projectConfig.getAttributes();
        if (projectConfigAttributeList == null || projectConfigAttributeList.isEmpty()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJ_CONFIG_ATTR_CMD_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_PROJ_CONFIG_ATTR_CMD_NOT_FOUND.messageTemplate);
        }

        String scanTaskIdString = scanTask != null ? scanTask.getId().toString() : "";
        String scanTasklogFilePath = scanVolumePath + "/" + scanTaskIdString + "/scan_task.log";
        try (E2EFileLogger e2eLogger = new E2EFileLogger(scanTasklogFilePath, scanTaskIdString, true)) {
            try {
                e2eLogger.writeLine(PHASE_POST_INJECT_DB + ",POST /api/issue_service/v2/scan_task/{id}/issues , id(scanTaskId):"+scanTask.getId()+" file_info_id:"+fileInfoId+" upload_file:"+inputFile.getAbsolutePath());
                e2eLogger.writeLine(PHASE_POST_INJECT_DB + ",init");

                Runtime runtime = Runtime.getRuntime();
                e2eLogger.writeLine(String.format("%s,Memory total=%.02fG maxMemory=%.02fG freeMemory=%.02fG",
                        PHASE_POST_INJECT_DB,
                        ResourceUtil.byteToGB(runtime.totalMemory()),
                        ResourceUtil.byteToGB(runtime.maxMemory()),
                        ResourceUtil.byteToGB(runtime.freeMemory())
                        ));

                        Span rootSpan = tracer.activeSpan();
                        issueService.importCsf(rootSpan, scanTask, project, projectConfigAttributeList, inputFile, fileInfoId, username);

            } catch (Exception e) {
                e2eLogger.writeLine(PHASE_POST_INJECT_DB + ", error while POSTPROC error=" + CsvUtil.removeComma(e.toString()) + " message=" + CsvUtil.removeComma(e.getMessage()==null?"":e.getMessage()));
                throw e;

            } finally {
                long endTime = System.nanoTime();
                double elapseTime = (endTime - startTime) / 1e6;
                e2eLogger.writeLine(PHASE_POST_INJECT_DB + ",fini,Elapse Time( "+elapseTime+" mSec )");

            }

        } // end e2eLogger try block
    }

}
