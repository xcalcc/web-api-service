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
import com.xcal.api.entity.ScanTask;
import com.xcal.api.entity.ScanTaskStatusLog;
import com.xcal.api.entity.ScanTaskStatusLog_;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.ScanStatusResponse;
import com.xcal.api.repository.ProjectRepository;
import com.xcal.api.repository.ScanTaskRepository;
import com.xcal.api.repository.ScanTaskStatusLogRepository;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.VariableUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScanStatusService {

    @Value("${app.scan.stage.number}")
    public Integer scanStageNumber;

    @NonNull ProjectRepository projectRepository;

    @NonNull ScanTaskRepository scanTaskRepository;

    @NonNull ScanTaskStatusLogRepository scanTaskStatusLogRepository;

    @NonNull I18nService i18nService;

    public ScanTaskStatusLog getLatestScanStatusByProject(Project project) throws AppException {
        log.debug("[getLatestScanStatusByProject] projectId: {}", project.getId());
        return this.scanTaskStatusLogRepository.findFirst1ByScanTaskProject(project,Sort.by(Sort.Order.desc(ScanTaskStatusLog_.MODIFIED_ON)))
                .orElseThrow(()-> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASKSTATUS_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_SCANTASKSTATUS_COMMON_NOT_FOUND.messageTemplate, project.getId())));
    }

    public ScanTaskStatusLog getLatestScanStatus(ScanTask scanTask) throws AppException {
        log.debug("[getLatestScanStatusByProject] scanTaskId: {}", scanTask.getId());
        return this.scanTaskStatusLogRepository.findFirst1ByScanTask(scanTask,Sort.by(Sort.Order.desc(ScanTaskStatusLog_.MODIFIED_ON)))
                .orElseThrow(()-> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASKSTATUS_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_SCANTASKSTATUS_COMMON_NOT_FOUND.messageTemplate, scanTask.getId())));
    }

    private double calculatePercentage(ScanTaskStatusLog.Stage stage, double percentage) {
        log.debug("[calculatePercentage] stage: {}, percentage: {}", stage, percentage);
        return stage.start + (stage.range * percentage / 100);
    }

    public ScanStatusResponse convertScanTaskStatusLogToResponse(ScanTaskStatusLog scanTaskStatusLog, Locale locale) throws AppException {
        log.debug("[convertScanTaskStatusLogToResponse] scanTaskStatusLog id: {}", scanTaskStatusLog.getId());
        double total = calculatePercentage(scanTaskStatusLog.getStage(), scanTaskStatusLog.getPercentage());
        String status = scanTaskStatusLog.getScanTask().getStatus().toString();
        switch (scanTaskStatusLog.getStatus()){
            case FAILED:
                status = ScanTask.Status.FAILED.toString();
                break;
            case TERMINATED:
                status = ScanTask.Status.TERMINATED.toString();
                break;
            case COMPLETED:
                status = ScanTask.Status.COMPLETED.toString();
                break;
            default:
                break;
        }
        String scanTaskStageI18nMsgKey = CommonUtil.formatString("${{}.{}}", VariableUtil.I18N_MESSAGE_KEY_PREFIX_SCAN_TASK_STAGE,scanTaskStatusLog.getStage().name());
        String originalMessage = CommonUtil.formatString("[{}] {}",scanTaskStageI18nMsgKey,scanTaskStatusLog.getMessage());
        if(Arrays.asList(ScanTaskStatusLog.Status.FAILED, ScanTaskStatusLog.Status.TERMINATED).contains(scanTaskStatusLog.getStatus())){
            originalMessage = scanTaskStatusLog.getMessage();
        }
        String localizedMessage = i18nService.formatString(originalMessage,locale);
        return ScanStatusResponse.builder()
                .projectId(scanTaskStatusLog.getScanTask().getProject().getId())
                .scanTaskId(scanTaskStatusLog.getScanTask().getId())
                .stage(scanTaskStatusLog.getStage().toString())
                .status(status)
                .unifyErrorCode(scanTaskStatusLog.getUnifyErrorCode())
                .percentage(total)
                .message(localizedMessage)
                .scanStartAt(scanTaskStatusLog.getScanTask().getScanStartAt())
                .scanEndAt(scanTaskStatusLog.getScanTask().getScanEndAt())
                .createdBy(scanTaskStatusLog.getCreatedBy())
                .createdOn(scanTaskStatusLog.getCreatedOn()).build();
    }

    public ScanTaskStatusLog saveScanTaskStatusLog(ScanTask scanTask, ScanTask.Status scanTaskStatus, ScanTaskStatusLog.Stage stage, ScanTaskStatusLog.Status status, Double percentage, String unifyErrorCode, String message, String currentUsername) throws AppException {
        log.debug("[saveScanTaskStatusLog] scanTask, id:{}, scanTaskStatus:{}, stage:{}, status:{}, percentage:{}, unifyErrorCode:{}, message:{}, currentUsername:{}", scanTask.getId(), scanTaskStatus, stage, status, percentage, unifyErrorCode, message, currentUsername);
        scanTask.setStatus(scanTaskStatus);
        if(Arrays.asList(ScanTask.Status.COMPLETED, ScanTask.Status.FAILED, ScanTask.Status.TERMINATED).contains(scanTaskStatus) ||
                (ScanTaskStatusLog.Stage.SCAN_COMPLETE.equals(stage) && ScanTaskStatusLog.Status.COMPLETED.equals(status))) {
            scanTask.setScanEndAt(new Date());
        }

        return this.saveScanTaskStatusLog(scanTask, stage, status, percentage, unifyErrorCode, message, currentUsername);
    }

    public ScanTaskStatusLog saveScanTaskStatusLog(ScanTask scanTask, ScanTaskStatusLog.Stage stage, ScanTaskStatusLog.Status status, Double percentage, String unifyErrorCode, String message, String currentUsername) {
        log.debug("[saveScanTaskStatusLog] scanTask, id: {}, stage: {}, status: {}, percentage: {}, unifyErrorCode: {}, message: {}, username:{}", scanTask.getId(), stage, status, percentage, unifyErrorCode, message, currentUsername);

        // Set default percentage to 0 to support new client
        percentage=percentage!=null?percentage:0;


        // Get latest scanTaskStatusLog entry from db.
        // Compare its stage and percentage with the parameter stage and percentage.
        // If stage is equal and percentage / (100 / scanStageNumber) == latestScanTaskStatusLog.percentage / (100 / scanStageNumber), update the scanTaskStatusLog entry get from db.
        // Otherwise insert a new scanTaskStatusLog entry.
        Date now = new Date();
        ScanTaskStatusLog scanTaskStatusLog = null;
        Optional<ScanTaskStatusLog> optionalScanTaskStatusLog = this.scanTaskStatusLogRepository.findFirst1ByScanTask(scanTask, Sort.by(Sort.Order.desc("modifiedOn")));
        if (optionalScanTaskStatusLog.isPresent()) {
            ScanTaskStatusLog dbScanTaskStatusLog = optionalScanTaskStatusLog.get();
            if (dbScanTaskStatusLog.getStage() == stage) {
                Integer interval = 100 / scanStageNumber;
                if ((int) (percentage / interval) == (int) (dbScanTaskStatusLog.getPercentage() / interval)) {
                    log.debug("[saveScanTaskStatusLog] update scanTaskStatusLog entry: {}", dbScanTaskStatusLog);
                    dbScanTaskStatusLog.setStatus(status);
                    dbScanTaskStatusLog.setPercentage(percentage);
                    dbScanTaskStatusLog.setUnifyErrorCode(unifyErrorCode);
                    dbScanTaskStatusLog.setMessage(message);
                    dbScanTaskStatusLog.setModifiedBy(currentUsername);
                    dbScanTaskStatusLog.setModifiedOn(now);
                    scanTaskStatusLog = dbScanTaskStatusLog;
                }
            }
        }

        if(scanTaskStatusLog == null) {
            // Insert a new scanTaskStatusLog entry.
            ScanTaskStatusLog newScanTaskStatusLog = ScanTaskStatusLog.builder()
                    .scanTask(scanTask)
                    .stage(stage)
                    .status(status)
                    .percentage(percentage)
                    .unifyErrorCode(unifyErrorCode)
                    .message(message)
                    .createdBy(currentUsername)
                    .createdOn(now)
                    .modifiedBy(currentUsername)
                    .modifiedOn(now).build();
            log.debug("[saveScanTaskStatusLog] insert a new scanTaskStatusLog entry: {}", newScanTaskStatusLog);
            scanTaskStatusLog = newScanTaskStatusLog;
        }

        scanTask.setModifiedBy(currentUsername);
        scanTask.setModifiedOn(now);
        this.scanTaskRepository.saveAndFlush(scanTask);

        return this.saveScanTaskStatusLog(scanTaskStatusLog, currentUsername);
    }

    public ScanTaskStatusLog saveScanTaskStatusLog(ScanTaskStatusLog scanTaskStatusLog, String currentUsername){
        Date now = new Date();
        scanTaskStatusLog.setCreatedBy(currentUsername);
        scanTaskStatusLog.setCreatedOn(now);
        scanTaskStatusLog.setModifiedBy(currentUsername);
        scanTaskStatusLog.setModifiedOn(now);
        return this.scanTaskStatusLogRepository.saveAndFlush(scanTaskStatusLog);
    }

}
