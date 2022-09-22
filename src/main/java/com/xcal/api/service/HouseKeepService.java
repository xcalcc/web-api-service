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

import com.xcal.api.dao.IssueGroupDao;
import com.xcal.api.entity.Project;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.entity.Setting;
import com.xcal.api.entity.v3.SourceCodeInfo;
import com.xcal.api.exception.AppException;
import com.xcal.api.mapper.HouseKeepMapper;
import com.xcal.api.repository.FileInfoRepository;
import com.xcal.api.repository.ScanFileRepository;
import com.xcal.api.repository.ScanTaskRepository;
import com.xcal.api.util.CommonUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.xcal.api.service.SettingService.SETTING_KEY_RETENTION_NUM;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HouseKeepService {

    @NonNull IssueGroupDao issueGroupDao;

    @NonNull SettingService settingService;

    @NonNull ProjectService projectService;

    @NonNull HouseKeepMapper houseKeepDao;

    @NonNull ScanFileRepository scanFileRepository;

    @NonNull FileInfoRepository fileInfoRepository;

    @NonNull ScanTaskRepository scanTaskRepository;

    @Value("${app.scan.volume.path}")
    public String scanVolumePath;

    @Value("${housekeep.scan-result-files}")
    public String scanResultFilesString;

    @NonNull ScanTaskService scanTaskService;

    public int houseKeepScanTasks(String fromStatus, String toStatus) {
        return scanTaskService.updateScanTaskStatus(fromStatus, toStatus);
    }


    public void houseKeepFileSystemByAge() {
        Optional<Integer> retentionPeriod = settingService.getRetentionPeriod();
        if (retentionPeriod.isPresent()) {
            houseKeepFileSystemByAge(retentionPeriod.get());
        } else {
            log.info("[houseKeepFileSystemByAge] Configured as never house keep. House keep process skipped.");
        }
    }

    public void houseKeepFileSystemByAge(Integer retentionPeriod) {
        log.debug("[houseKeepFileSystemByAge] retentionPeriod: {} ", retentionPeriod);
        Date currentDateTime = new Date();
        //Get source code info which need to be removed
        List<SourceCodeInfo> sourceCodeInfoList = houseKeepDao.getHouseKeepSourceCodeInfoList(null, retentionPeriod);
        log.debug("[houseKeepFileSystemByAge] sourceCodeInfoList.size() {}", sourceCodeInfoList.size());
        //Remove source code folder base on file_info.id(same as folder name) if not latest scan task
        for (SourceCodeInfo sourceCodeInfo : sourceCodeInfoList) {
            try {
                forceDeleteSourceCodeFolder(sourceCodeInfo);
                forceDeleteScanResultFolder(sourceCodeInfo);

                //Delete file info and scan file based on scan task
                fileInfoRepository.deleteByScanTask(sourceCodeInfo.getScanTaskId());
                scanFileRepository.deleteByScanTask(sourceCodeInfo.getScanTaskId());

                Optional<ScanTask> scanTaskOptional = scanTaskRepository.findById(sourceCodeInfo.getScanTaskId());
                if (scanTaskOptional.isPresent()) {
                    //Update scan task status
                    log.debug("[houseKeepFileSystemByAge] update scan task house keep status");
                    ScanTask scanTask = scanTaskOptional.get();
                    scanTask.setHouseKeepOn(currentDateTime);
                    scanTaskRepository.save(scanTask);
                } else {
                    log.debug("[houseKeepFileSystemByAge] scan task does not exist: {}", sourceCodeInfo.getScanTaskId());
                }
            } catch (IOException e) {
                log.warn("[houseKeepFileSystemByAge] source code or scan task cannot be removed", e);
            }
        }//end for each source code info
    }

    public String[] getRemoveRelativeFilePathList() {
        return scanResultFilesString.trim().split(";");
    }

    private void forceDeleteIgnoreNotFound(File fileOrFolder) throws IOException {
        log.debug("[forceDeleteIgnoreNotFound] begin to delete: {}", fileOrFolder.getPath());
        try {
            FileUtils.forceDelete(fileOrFolder);
        } catch (FileNotFoundException e) {
            log.warn("[forceDeleteIgnoreNotFound] file not found.", e.getMessage());
        }
        log.debug("[forceDeleteIgnoreNotFound] finish delete");
    }

    public void forceDeleteScanResultFolder(SourceCodeInfo sourceCodeInfo) throws IOException {
        log.debug("[forceDeleteScanResultFolder] sourceCodeInfo: {}", sourceCodeInfo);
        File scanResultFolder = Paths.get(scanVolumePath, sourceCodeInfo.getScanTaskId().toString()).toFile();

        if (scanResultFolder.getPath().equals("/")) {
            log.error("[forceDeleteScanResultFolder] Should not delete root folder. skipped.");
            return;
        }

        String[] removeRelativeFilePathList = getRemoveRelativeFilePathList();
        for (String relativeFilePath : removeRelativeFilePathList) {
            //replace values
            String replacedRelativeFilePath = relativeFilePath.replaceAll("<scan_task_id>", sourceCodeInfo.getScanTaskId().toString());

            if(replacedRelativeFilePath.equals("/")){
                log.error("[forceDeleteScanResultFolder] This should not be deleted, skipped: {}",replacedRelativeFilePath);
                continue;
            }

            File fileToBeRemoved = Paths.get(scanResultFolder.getPath(), replacedRelativeFilePath).toFile();

            //validation
            if (fileToBeRemoved.getAbsolutePath().isEmpty() || fileToBeRemoved.getAbsolutePath().equals("/") ) {
                log.error("[forceDeleteScanResultFolder] This should not be deleted, skipped: {}",fileToBeRemoved.getAbsolutePath() );
                continue;
            }

            log.info("[forceDeleteScanResultFolder] Removing: {}",fileToBeRemoved.getAbsolutePath());
            forceDeleteIgnoreNotFound(fileToBeRemoved);
        }
    }

    private void forceDeleteSourceCodeFolder(SourceCodeInfo sourceCodeInfo) throws IOException {
        //remove source code archive base on file_info.relative_path
        log.debug("[forceDeleteSourceCodeFolder] sourceCodeInfo: {}", sourceCodeInfo);
        File sourceFolder = Paths.get(sourceCodeInfo.getFileStorageHost(), sourceCodeInfo.getRelativePath()).toFile();

        String fullPath = sourceFolder.getPath();
        if (fullPath.equals("/")) {
            log.error("[forceDeleteSourceCodeFolder] Should not delete root folder. skipped.");
            return;
        }

        forceDeleteIgnoreNotFound(sourceFolder);
    }

    public void houseKeepIssueGroupByAge(String retentionPeriod) {
        if (retentionPeriod == null) {
            throw new IllegalArgumentException("Invalid retentionPeriod:" + retentionPeriod + " .Possibly have invalid setting in database");
        }

        if (retentionPeriod.equals("NEVER")) {
            log.info("[houseKeepIssueGroupByAge] Configured as never house keep. House keep process skipped.");
            return;
        }

        issueGroupDao.deleteFixedIssueGroupByAge(retentionPeriod);
    }

    public void houseKeepScanDataByAge(Integer retentionPeriod) throws AppException {
        log.info("[houseKeepScanDataByAge] retentionPeriod: {}", retentionPeriod);
        Setting retentionNumSetting = settingService.findByKey(SETTING_KEY_RETENTION_NUM)
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] settingKey: {}", AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.messageTemplate, SETTING_KEY_RETENTION_NUM)));
        Integer globalRetentionNum = Integer.parseInt(retentionNumSetting.getSettingValue());
        log.info("[houseKeepScanDataByAge] global retentionNum: {}", globalRetentionNum);

        log.info("[houseKeepScanDataByAge] begin to remove expired scan data");
        List<Project> projectList = projectService.findAll();
        for(Project project: projectList) {
            Integer retentionNum = globalRetentionNum;
            if((project.getRetentionNum() != null) && (project.getRetentionNum() != 0)) {
                log.debug("[houseKeepScanDataByAge] project {}, retentionNum: {}, will override global retentionNum", project.getProjectId(), project.getRetentionNum());
                retentionNum = project.getRetentionNum();
            }
            Integer numberOfScanTasks = scanTaskRepository.findByProject(project).size();
            if(numberOfScanTasks <= retentionNum) {
                continue;
            }

            List<ScanTask> scanTasks = scanTaskRepository.findExpiredScanTaskInProjectByAgeAndNum(project.getId(), retentionPeriod, numberOfScanTasks - retentionNum);
            log.info("[houseKeepScanDataByAge] project {} has {} expired scan task data need to be removed", project.getProjectId(), scanTasks.size());
            if(scanTasks.size() > 0) {
                log.warn("[houseKeepScanDataByAge] begin to remove expired scan task data for project {}", project.getProjectId());
                for (ScanTask scanTask : scanTasks) {
                    houseKeepDao.removeScanData(scanTask.getId());
                }
                log.warn("[houseKeepScanDataByAge] end to remove expired scan task data for project {}", project.getProjectId());
            }
        }

        log.info("[houseKeepScanDataByAge] end to remove expired scan data");
    }

    public void houseKeepScanData() throws AppException {
        Optional<Integer> retentionPeriod = settingService.getRetentionPeriod();
        if (retentionPeriod.isPresent()) {
            houseKeepScanDataByAge(retentionPeriod.get());
        } else {
            log.warn("[houseKeepScanData] Configured as never house keep. House keep process skipped.");
        }
    }

    public void houseKeepNotReferencedFileInfo() {
        try {
            log.debug("[houseKeepNotReferencedFileInfo] begin to remove not referenced file info");
            this.houseKeepDao.removeNotReferencedFileInfo();
            log.debug("[houseKeepNotReferencedFileInfo] end to remove not referenced file info");
        } catch (Exception e) {
            log.warn("[houseKeepNotReferencedFileInfo] remove not referenced file info failed", e);
        }
    }

}
