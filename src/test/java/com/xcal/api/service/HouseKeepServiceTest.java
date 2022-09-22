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
import com.xcal.api.entity.ScanTask;
import com.xcal.api.entity.v3.SourceCodeInfo;
import com.xcal.api.mapper.HouseKeepMapper;
import com.xcal.api.repository.FileInfoRepository;
import com.xcal.api.repository.ScanFileRepository;
import com.xcal.api.repository.ScanTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
class HouseKeepServiceTest {

    private IssueGroupDao issueGroupDao;
    private SettingService settingService;
    private ProjectService projectService;
    private HouseKeepMapper houseKeepDao;
    private ScanFileRepository scanFileRepository;
    private FileInfoRepository fileInfoRepository;
    private ScanTaskRepository scanTaskRepository;
    private ScanTaskService scanTaskService;
    HouseKeepService houseKeepService;


    @BeforeEach
    void setup() {
        issueGroupDao = mock(IssueGroupDao.class);
        settingService = mock(SettingService.class);
        projectService = mock(ProjectService.class);
        houseKeepDao = mock(HouseKeepMapper.class);
        scanFileRepository = mock(ScanFileRepository.class);
        fileInfoRepository = mock(FileInfoRepository.class);
        scanTaskRepository = mock(ScanTaskRepository.class);
        scanTaskService = mock(ScanTaskService.class);
        when(scanTaskService.updateScanTaskStatus("PROCESSING", "FAILED")).thenReturn(5);
        when(scanTaskService.updateScanTaskStatus("PENDING", "FAILED")).thenReturn(10);
        houseKeepService = new HouseKeepService(issueGroupDao, settingService, projectService, houseKeepDao, scanFileRepository, fileInfoRepository, scanTaskRepository, scanTaskService);
        houseKeepService.scanResultFilesString="<scan_task_id>.start.log;fileinfo.json;scan_task.log;status-sse.txt;scm_diff.txt;.scan_log/VTXTDIFF.log;.scan_log/convert.log;.scan_log/scan.log;.scan_log/scan_failed_list";
    }

    @Test
    void houseKeepScanTasks_processingToFailed_expectUpdatedCount() {
        int count = houseKeepService.houseKeepScanTasks("PROCESSING", "FAILED");
        assertEquals(5, count);
    }

    @Test
    void houseKeepScanTasks_pendingToFailed_expectUpdatedCount() {
        int count = houseKeepService.houseKeepScanTasks("PENDING", "FAILED");
        assertEquals(10, count);
    }

    @Test
    void houseKeepScanTasks_exception_expectException() {
        when(scanTaskService.updateScanTaskStatus("PROCESSING", "FAILED")).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, () -> houseKeepService.houseKeepScanTasks("PROCESSING", "FAILED"));
    }


    @Test
    void houseKeepFileSystemByAge_scanTaskExist_success() {
        when(settingService.getRetentionPeriod()).thenReturn(Optional.of(7));
        when(houseKeepDao.getHouseKeepSourceCodeInfoList(isNull(), anyInt())).thenReturn(Collections.singletonList(SourceCodeInfo.builder().scanTaskId(UUID.randomUUID()).fileStorageHost("abcd").relativePath("/").build()));
        when(scanTaskRepository.findById(any(UUID.class))).thenReturn(Optional.of(ScanTask.builder().status(ScanTask.Status.COMPLETED).build()));
        houseKeepService.houseKeepFileSystemByAge();

    }

    @Test
    void houseKeepFileSystemByAge_scanTaskNotExist_success() {
        when(settingService.getRetentionPeriod()).thenReturn(Optional.of(7));
        when(houseKeepDao.getHouseKeepSourceCodeInfoList(isNull(), anyInt())).thenReturn(Collections.singletonList(SourceCodeInfo.builder().scanTaskId(UUID.randomUUID()).fileStorageHost("abcd").relativePath("/").build()));
        houseKeepService.houseKeepFileSystemByAge();

    }

    @Test
    void houseKeepFileSystemByAge_IOException_ignore() throws IOException {
        HouseKeepService spyHouseKeepService=spy(houseKeepService);
        when(settingService.getRetentionPeriod()).thenReturn(Optional.of(7));
        doThrow(new IOException("test")).when(spyHouseKeepService).forceDeleteScanResultFolder(any());
        spyHouseKeepService.houseKeepFileSystemByAge();

    }

    @Test
    void houseKeepIssueGroupByAge_normal_success() {
        houseKeepService.houseKeepIssueGroupByAge("7 day");
    }

    @Test
    void houseKeepIssueGroupByAge_never_skipDeleteAction() {
        houseKeepService.houseKeepIssueGroupByAge("NEVER");
    }


    @Test
    void houseKeepIssueGroupByAge_null_throwIllArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> houseKeepService.houseKeepIssueGroupByAge(null));
    }

    @Test
    void houseKeepNotReferencedFileInfo_normal_success(){
        doNothing().when(houseKeepDao).removeNotReferencedFileInfo();
        houseKeepService.houseKeepNotReferencedFileInfo();
    }

    @Test
    void houseKeepNotReferencedFileInfo_Exception_ignoreException(){
        doThrow(new RuntimeException()).when(houseKeepDao).removeNotReferencedFileInfo();
        houseKeepService.houseKeepNotReferencedFileInfo();
    }

    @Test
    void getRemoveRelativeFilePathList_normalsuccess() {

        String[] list = houseKeepService.getRemoveRelativeFilePathList();
        assertEquals("<scan_task_id>.start.log", list[0]);
        assertEquals("fileinfo.json", list[1]);
        assertEquals("scan_task.log", list[2]);
        assertEquals("status-sse.txt", list[3]);
        assertEquals("scm_diff.txt", list[4]);
        assertEquals(".scan_log/VTXTDIFF.log", list[5]);
        assertEquals(".scan_log/convert.log", list[6]);
        assertEquals(".scan_log/scan.log", list[7]);
        assertEquals(".scan_log/scan_failed_list", list[8]);

    }

}
