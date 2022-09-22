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
import com.xcal.api.exception.AppException;
import com.xcal.api.mapper.ScanTaskSummaryMapper;
import com.xcal.api.metric.ScanMetric;
import com.xcal.api.model.dto.ScanTaskDto;
import com.xcal.api.model.payload.AddScanTaskRequest;
import com.xcal.api.model.payload.UpdateScanTaskRequest;
import com.xcal.api.model.payload.ValidationResult;
import com.xcal.api.model.payload.v3.ScanTaskIdResponse;
import com.xcal.api.repository.*;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.net.HttpURLConnection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class ScanTaskServiceTest {
    private ScanTaskService scanTaskService;
    private AsyncScanService asyncScanService;
    private ScanTaskRepository scanTaskRepository;
    private ProjectService projectService;
    private ScanTaskStatusLogRepository scanTaskStatusLogRepository;
    private LicenseService licenseService;
    private ScanStatusService scanStatusService;
    private ScanFileRepository scanFileRepository;
    private FileService fileService;
    private GitlabService gitlabService;
    private GithubService githubService;
    private GerritService gerritService;
    private MeasureService measureService;
    private NotifyService notifyService;
    private final String currentUserName = "user";

    private FileStorageService fileStorageService;
    private ScanTaskSummaryMapper scanTaskSummaryMapper;
    private ScanTaskDao scanTaskDao;
    private ProjectDao projectDao;

    private final Project project = Project.builder().id(UUID.randomUUID())
            .projectId("test_project").name("testProject").status(Project.Status.ACTIVE).createdBy(currentUserName).build();

    private final ProjectConfig projectConfig = ProjectConfig.builder().id(UUID.randomUUID()).project(project).status(ProjectConfig.Status.ACTIVE).build();

    private final ScanTask scanTask1 = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PENDING).build();
    private final ScanTask scanTask2 = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PROCESSING).build();
    private final List<ScanTask> scanTaskList = Arrays.asList(scanTask1, scanTask2);

    private final ScanTaskStatusLog scanTaskStatusLog1 = ScanTaskStatusLog.builder().id(UUID.randomUUID()).scanTask(scanTask1).build();

    private final FileStorage gitLabFileStorage = FileStorage.builder().fileStorageHost("https://gitlab.com").fileStorageType(FileStorage.Type.GITLAB).name("gitlab_storage").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
    private final FileStorage agentFileStorage = FileStorage.builder().fileStorageHost("/home/xxx/test").fileStorageType(FileStorage.Type.AGENT).name("agent_storage").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
    private final FileStorage uploadFileStorage = FileStorage.builder().fileStorageHost("/share/upload").fileStorageType(FileStorage.Type.VOLUME).name("volume_upload").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
    private final FileStorage volumeFileStorage = FileStorage.builder().fileStorageHost("/share/src").fileStorageType(FileStorage.Type.VOLUME).name("volume_src").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
    private final FileStorage svnFileStorage = FileStorage.builder().fileStorageHost("svn://xxx.com").fileStorageType(FileStorage.Type.SVN).name("svn_storage").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
    private final FileStorage gitFileStorage = FileStorage.builder().fileStorageHost("https://git.com").fileStorageType(FileStorage.Type.GIT).name("git_storage").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
    private final FileStorage gitHubFileStorage = FileStorage.builder().fileStorageHost("https://github.com").fileStorageType(FileStorage.Type.GITHUB).name("github_storage").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
    private List<AddScanTaskRequest.Attribute> addScanTaskRequestAttributes;
    private List<ProjectConfigAttribute> projectConfigAttributeList;
    private ProjectConfig projectConfigWithAttributeList;


    @BeforeEach
    void setUp() {
        asyncScanService = mock(AsyncScanService.class);
        scanTaskRepository = mock(ScanTaskRepository.class);
        projectService = mock(ProjectService.class);
        scanTaskStatusLogRepository = mock(ScanTaskStatusLogRepository.class);
        licenseService = mock(LicenseService.class);
        FileStorageRepository fileStorageRepository = mock(FileStorageRepository.class);
        scanStatusService = mock(ScanStatusService.class);
        AsyncScanService asyncScanService = mock(AsyncScanService.class);
        scanFileRepository = mock(ScanFileRepository.class);
        fileService = mock(FileService.class);
        gitlabService = mock(GitlabService.class);
        githubService = mock(GithubService.class);
        gerritService = mock(GerritService.class);
        measureService = mock(MeasureService.class);
        notifyService = mock(NotifyService.class);
        PerformanceService performanceService = mock(PerformanceService.class);
        Tracer tracer = mock(Tracer.class);
        fileStorageService = mock(FileStorageService.class);
        scanTaskSummaryMapper = mock(ScanTaskSummaryMapper.class);
        scanTaskDao = mock(ScanTaskDao.class);
        projectDao = mock(ProjectDao.class);
        scanTaskService = new ScanTaskService(mock(ObjectMapper.class), fileStorageRepository, projectService, mock(ProjectConfigRepository.class), scanTaskRepository, scanTaskStatusLogRepository, scanFileRepository
                , fileService, fileStorageService, mock(ScanFileService.class), asyncScanService, scanStatusService, gitlabService, githubService, gerritService, licenseService, measureService, performanceService, notifyService, mock(ScanMetric.class), tracer, scanTaskSummaryMapper, scanTaskDao,projectDao);

        when(fileStorageService.findByName(gitLabFileStorage.getName())).thenReturn(Optional.of(gitLabFileStorage));
        when(fileStorageService.findByName(agentFileStorage.getName())).thenReturn(Optional.of(agentFileStorage));
        when(fileStorageService.findByName(uploadFileStorage.getName())).thenReturn(Optional.of(uploadFileStorage));
        when(fileStorageService.findByName(volumeFileStorage.getName())).thenReturn(Optional.of(volumeFileStorage));
        when(fileStorageService.findByName(svnFileStorage.getName())).thenReturn(Optional.of(svnFileStorage));
        when(fileStorageService.findByName(gitFileStorage.getName())).thenReturn(Optional.of(gitFileStorage));
        when(fileStorageService.findByName(gitHubFileStorage.getName())).thenReturn(Optional.of(gitHubFileStorage));

        projectConfigAttributeList = new ArrayList(Arrays.asList(ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue)
                        .value("offline_agent").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                        .value(agentFileStorage.getName()).build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_SOURCE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_SOURCE.nameValue)
                        .value("true").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                        .value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                        .value("/source/demo_benchmark/c_testcase/advance").build()
        ));

        projectConfigWithAttributeList = ProjectConfig.builder()
                .attributes(projectConfigAttributeList).build();




        addScanTaskRequestAttributes = Arrays.asList(AddScanTaskRequest.Attribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type.name())
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                        .value(gitFileStorage.getName()).build(),
                AddScanTaskRequest.Attribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.type.name())
                        .name(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.nameValue)
                        .value("https://github.com/AlynxZhou/INTANG").build(),
                AddScanTaskRequest.Attribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type.name())
                        .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                        .value("/nginx").build(),
                AddScanTaskRequest.Attribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type.name())
                        .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                        .value("/").build(),
                AddScanTaskRequest.Attribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type.name())
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue)
                        .value("offline_agent").build(),
                AddScanTaskRequest.Attribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type.name())
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                        .value(gitFileStorage.getName()).build(),
                AddScanTaskRequest.Attribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type.name())
                        .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                        .value("/nginx").build(),
                AddScanTaskRequest.Attribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type.name())
                        .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                        .value("/").build()

        );


    }

    @Test
    void findByIdTestSuccess() {
        log.info("[findByIdTestSuccess]");
        when(scanTaskRepository.findById(scanTask1.getId())).thenReturn(Optional.of(scanTask1));
        Optional<ScanTask> optionalScanTask = scanTaskService.findById(scanTask1.getId());
        assertTrue(optionalScanTask.isPresent());
        assertEquals(scanTask1.getId(), optionalScanTask.get().getId());
    }

    @Test
    void updateTestSuccess() {
        log.info("[updateTestSuccess]");
        when(scanTaskRepository.saveAndFlush(scanTask1)).thenReturn(scanTask1);
        scanTaskService.update(scanTask1);
        assertTrue(true);
    }

    @Test
    void getLatestCompletedScanTaskByProjectTestSuccess() {
        log.info("[getLatestCompletedScanTaskByProjectTestSuccess]");
        when(scanTaskRepository.findFirst1ByProjectAndStatus(project, ScanTask.Status.COMPLETED, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(scanTask1));
        Optional<ScanTask> optionalScanTask = scanTaskService.getLatestCompletedScanTaskByProject(project);
        assertTrue(optionalScanTask.isPresent());
        assertEquals(scanTask1.getId(), optionalScanTask.get().getId());
    }

    @Test
    void getScanTaskByProjectAndStatus_InputProjectAndStatusList_ReturnScanTaskList() {
        log.info("[getScanTaskByProjectAndStatus_InputProjectAndStatuses_ReturnScanTaskList]");
        Pageable pageable = PageRequest.of(0, 20);
        Page<ScanTask> pagedScanTasks = new PageImpl<>(scanTaskList);
        when(scanTaskRepository.findByProjectAndStatusIn(project, Arrays.asList(ScanTask.Status.values()), pageable)).thenReturn(pagedScanTasks);
        Page<ScanTask> resultPagedScanTask = scanTaskService.getScanTaskByProjectAndStatus(project, Arrays.asList(ScanTask.Status.values()), pageable);
        assertEquals(pagedScanTasks.getTotalPages(), resultPagedScanTask.getTotalPages());
        assertEquals(pagedScanTasks.getTotalElements(), resultPagedScanTask.getTotalElements());
    }

    @Test
    void getLatestCompletedScanTaskByProjectTestSuccess1() {
        log.info("[getLatestCompletedScanTaskByProjectTestSuccess1]");
        Pageable pageable = PageRequest.of(0, 20);
        Page<ScanTask> pagedScanTasks = new PageImpl<>(scanTaskList);
        when(scanTaskRepository.findByProjectAndStatusIn(project, Collections.singletonList(ScanTask.Status.COMPLETED), pageable)).thenReturn(pagedScanTasks);
        Page<ScanTask> resultPagedScanTask = scanTaskService.getLatestCompletedScanTaskByProject(project, pageable);
        assertEquals(pagedScanTasks.getTotalPages(), resultPagedScanTask.getTotalPages());
        assertEquals(pagedScanTasks.getTotalElements(), resultPagedScanTask.getTotalElements());
    }

    @Test
    void getLatestScanTaskByProjectIdTestSuccess() throws AppException {
        log.info("[getLatestScanTaskByProjectIdTestSuccess]");
        when(projectService.findById(project.getId())).thenReturn(Optional.of(project));
        when(scanTaskRepository.findFirst1ByProject(project, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(scanTask1));
        Optional<ScanTask> optionalScanTask = scanTaskService.getLatestScanTaskByProjectId(project.getId());
        assertTrue(optionalScanTask.isPresent());
        assertEquals(scanTask1.getId(), optionalScanTask.get().getId());
    }

    @Test
    void getLatestScanTaskByProjectIdTestFail() {
        log.info("[getLatestScanTaskByProjectIdTestFail]");
        when(projectService.findById(project.getId())).thenReturn(Optional.empty());
        when(scanTaskRepository.findFirst1ByProject(project, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(scanTask1));
        assertThrows(AppException.class, () -> scanTaskService.getLatestScanTaskByProjectId(project.getId()));
    }

    @Test
    void getLatestScanTaskTestSuccess() {
        log.info("[getLatestScanTaskTestSuccess]");
        when(scanTaskRepository.findFirst1ByProject(project, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(scanTask1));
        Optional<ScanTask> optionalScanTask = scanTaskService.getLatestScanTask(project);
        assertTrue(optionalScanTask.isPresent());
        assertEquals(scanTask1.getId(), optionalScanTask.get().getId());
    }

    @Test
    void getLatestScanTaskStatusLogTestSuccess() {
        log.info("[getLatestScanTaskStatusLogTestSuccess]");
        when(scanTaskStatusLogRepository.findFirst1ByScanTask(scanTask1, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(scanTaskStatusLog1));
        Optional<ScanTaskStatusLog> optionalScanTaskStatusLog = scanTaskService.getLatestScanTaskStatusLog(scanTask1);
        assertTrue(optionalScanTaskStatusLog.isPresent());
        assertEquals(scanTaskStatusLog1.getId(), optionalScanTaskStatusLog.get().getId());
    }

    @Test
    void getLatestRunningScanTask_ProjectWithRunningScanTask_ShouldReturnThisRunningScanTask() {
        when(scanTaskService.getLatestScanTask(project)).thenReturn(Optional.of(scanTask2));
        Optional<ScanTask> optionalScanTask = scanTaskService.getLatestRunningScanTask(project);
        assertTrue(optionalScanTask.isPresent());
        assertEquals(scanTask2.getId(), optionalScanTask.get().getId());
        assertEquals(scanTask2.getStatus(), optionalScanTask.get().getStatus());
    }

    @Test
    void getLatestRunningScanTask_ProjectWithoutRunningScanTask_ShouldReturnEmptyOptional() {
        ScanTask failedScanTask = ScanTask.builder().id(scanTask1.getId()).status(ScanTask.Status.FAILED).build();
        when(scanTaskService.getLatestScanTask(project)).thenReturn(Optional.of(failedScanTask));
        Optional<ScanTask> optionalScanTask = scanTaskService.getLatestRunningScanTask(project);
        assertFalse(optionalScanTask.isPresent());
    }

    @Test
    void getScanTaskSourceRootTestSuccess1() throws AppException {
        log.info("[getScanTaskSourceRootTestSuccess1]");

        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(gitFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.nameValue)
                                .value("https://github.com/AlynxZhou/INTANG").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("/nginx").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("/").build()
                )).build();
        String scanTaskSourceRoot = scanTaskService.getScanTaskSourceRoot(projectConfig);
        assertNull(scanTaskSourceRoot);
    }

    @Test
    void getScanTaskSourceRootTestSuccess2() throws AppException {
        log.info("[getScanTaskSourceRootTestSuccess2]");
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(svnFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                                .name("svsProjectUrl")
                                .value("svn://xxx.com/repository").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("/nginx").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("/").build()
                )).build();
        String scanTaskSourceRoot = scanTaskService.getScanTaskSourceRoot(projectConfig);
        assertNull(scanTaskSourceRoot);
    }

    @Test
    void getScanTaskSourceRootTestSuccess3() throws AppException {
        log.info("[getScanTaskSourceRootTestSuccess3]");
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(volumeFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("/nginx").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("/").build()
                )).build();
        String scanTaskSourceRoot = scanTaskService.getScanTaskSourceRoot(projectConfig);
        assertEquals("/share/src/nginx", scanTaskSourceRoot);
    }

    @Test
    void getScanTaskSourceRoot_ValidParams_ShouldSuccess() throws AppException {
        log.info("[getScanTaskSourceRoot_ValidParams_ShouldSuccess]");
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(agentFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("/nginx").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("/").build()
                )).build();
        String scanTaskSourceRoot = scanTaskService.getScanTaskSourceRoot(projectConfig);
        assertEquals("/nginx", scanTaskSourceRoot);
    }

    @Test
    void getScanTaskSourceRootTestSuccess5() throws AppException {
        log.info("[getScanTaskSourceRootTestSuccess5]");
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(gitLabFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.nameValue)
                                .value("https://gitlab.com/tianxiang_gitlab/test_gitlab.git").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("c_testcase/advance").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("c_testcase/advance").build()
                )).build();
        String scanTaskSourceRoot = scanTaskService.getScanTaskSourceRoot(projectConfig);
        assertEquals("https://gitlab.com/tianxiang_gitlab/test_gitlab.git", scanTaskSourceRoot);
    }

    @Test
    void getScanTaskSourceRootTestSuccess6() throws AppException {
        log.info("[getScanTaskSourceRootTestSuccess6]");
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(gitHubFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.nameValue)
                                .value("https://github.com/steveli1840/test_github").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("c_testcase/advance").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("c_testcase/advance").build()
                )).build();
        String scanTaskSourceRoot = scanTaskService.getScanTaskSourceRoot(projectConfig);
        assertEquals("https://github.com/steveli1840/test_github", scanTaskSourceRoot);
    }

    @Test
    void addScanTaskWithProjectAndAttributesAndStartNow_gerrit_noError() throws AppException {

        doReturn(Optional.of(projectConfigWithAttributeList)).when(projectService).getLatestActiveProjectConfigByProject(any(Project.class));
        ScanTaskService scanTaskServiceSpy = Mockito.spy(scanTaskService);
        doReturn(scanTask1).when(scanTaskServiceSpy).addScanTask(any(), anyList(),any());
        doReturn("aaa").when(scanTaskServiceSpy).getLatestCommitId(any(ProjectConfig.class),any(FileStorage.class));
        FileStorage gitFileStorage = FileStorage.builder()
                .fileStorageHost("https://gitlab.com")
                .fileStorageType(FileStorage.Type.GERRIT)
                .name("gitlab_storage").createdBy(currentUserName)
                .status(FileStorage.Status.ACTIVE).build();
        doReturn(Optional.of(gitFileStorage)).when(fileStorageService).findByName(anyString());
        assertDoesNotThrow(()-> scanTaskServiceSpy.addScanTask(project, addScanTaskRequestAttributes, false, projectConfigWithAttributeList.getCreatedBy()));

    }

    @Test
    void addScanTaskWithProjectAndAttributesAndStartNow_latestActiveProjectConfigNotFound_applicationException() throws AppException {

        doReturn(Optional.empty()).when(projectService).getLatestActiveProjectConfigByProject(any(Project.class));
        ScanTaskService scanTaskServiceSpy = Mockito.spy(scanTaskService);
        doReturn(scanTask1).when(scanTaskServiceSpy).addScanTask(any(), anyList(),any());
        assertThrows(AppException.class, ()-> scanTaskServiceSpy.addScanTask(project, addScanTaskRequestAttributes, false, projectConfigWithAttributeList.getCreatedBy()));

    }

    @Test
    void addScanTaskWithProjectAndAttributesAndStartNow_scanTypeNotFound_applicationException() throws AppException {
        List<ProjectConfigAttribute> projectConfigAttributeList = projectConfigWithAttributeList.getAttributes();
        ProjectConfigAttribute pca = null;
        int i=0;
        for(i=0;i<projectConfigAttributeList.size();i++){
            pca = projectConfigAttributeList.get(i);
            System.out.println(pca);
            if(StringUtils.equalsIgnoreCase(pca.getName(),VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue)){
                break;
            }
        }
        projectConfigAttributeList.remove(i);
        doReturn(Optional.of(projectConfigWithAttributeList)).when(projectService).getLatestActiveProjectConfigByProject(any(Project.class));
        ScanTaskService scanTaskServiceSpy = Mockito.spy(scanTaskService);
        doReturn(scanTask1).when(scanTaskServiceSpy).addScanTask(any(), anyList(),any());
        assertThrows(AppException.class, ()-> scanTaskServiceSpy.addScanTask(project, addScanTaskRequestAttributes, false, projectConfigWithAttributeList.getCreatedBy()));

    }

    @Test
    void addScanTaskWithProjectAndAttributesAndStartNow_startNowForOfflineAgent_appException() throws AppException {

        doReturn(Optional.of(projectConfigWithAttributeList)).when(projectService).getLatestActiveProjectConfigByProject(any(Project.class));
        ScanTaskService scanTaskServiceSpy = Mockito.spy(scanTaskService);
        doReturn(scanTask1).when(scanTaskServiceSpy).addScanTask(any(), anyList(),any());
        assertThrows(AppException.class, ()-> scanTaskServiceSpy.addScanTask(project, addScanTaskRequestAttributes, true, projectConfigWithAttributeList.getCreatedBy()));

    }

//    @Test
//    void addScanTaskWithProjectAndAttributesAndStartNow_startNowForOfflineAgent_appException() throws AppException {
//        doReturn(Optional.of(projectConfigWithAttributeList)).when(projectService).getLatestActiveProjectConfigByProject(any(Project.class));
//        ScanTaskService scanTaskServiceSpy = Mockito.spy(scanTaskService);
//        doReturn(scanTask1).when(scanTaskServiceSpy).addScanTask(any(), anyList(),any());
//        assertThrows(AppException.class, ()-> scanTaskServiceSpy.addScanTask(project, addScanTaskRequestAttributes, true, projectConfigWithAttributeList.getCreatedBy()));
//
//    }

    @Test
    void addScanTaskWithProjectAndAttributesAndStartNow_runtimeException_runtimeException() throws AppException {

        doThrow(RuntimeException.class).when(projectService).getLatestActiveProjectConfigByProject(any(Project.class));
        ScanTaskService scanTaskServiceSpy = Mockito.spy(scanTaskService);
        doReturn(scanTask1).when(scanTaskServiceSpy).addScanTask(any(), anyList(),any());
        assertThrows(RuntimeException.class, ()-> scanTaskServiceSpy.addScanTask(project, addScanTaskRequestAttributes, false, projectConfigWithAttributeList.getCreatedBy()));

    }

    @Test
    void addScanTask_CheckLicenseFail_ShouldThrowException() {
        log.info("[addScanTask_CheckLicenseFail_ShouldThrowException]");
        ValidationResult validationResult = ValidationResult.builder().status(ValidationResult.Status.FAIL).exception(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR,
                HttpURLConnection.HTTP_INTERNAL_ERROR, null, "[license expires_on attribute should not be blank].")).build();
        when(licenseService.checkLicense()).thenReturn(validationResult);

        ProjectConfig projectConfig = ProjectConfig.builder().build();
        assertThrows(AppException.class, () -> scanTaskService.addScanTask(projectConfig, currentUserName));
    }

    @Test
    void addScanTask_WithValidParams_ShouldSuccess() throws AppException {
        log.info("[addScanTask_WithValidParams_ShouldSuccess]");
        ValidationResult validationResult = ValidationResult.builder().status(ValidationResult.Status.SUCCESS).build();
        when(licenseService.checkLicense()).thenReturn(validationResult);
        String projectConfigStr = "{\n" +
                "  \"sourceStorageName\" : \"volume_src\",\n" +
                "  \"gitUrl\" : \"\",\n" +
                "  \"relativeSourcePath\" : \"/nginx\",\n" +
                "  \"relativeBuildPath\" : \"/\",\n" +
                "  \"scanType\" : \"online_agent\"\n" +
                "}";

        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue)
                                .value("online_agent").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(volumeFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("/nginx").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("/").build()
                )).build();
        when(scanTaskRepository.saveAndFlush(any())).thenReturn(scanTask1);

        ScanTask resultScanTask = scanTaskService.addScanTask(projectConfig, currentUserName);

        assertEquals(scanTask1.getId(), resultScanTask.getId());
        assertEquals(scanTask1.getStatus(), resultScanTask.getStatus());
    }

    @Test
    void prepareAndCallScan_WithValidParams_ShouldSuccess() throws AppException {
        ProjectConfig projectConfig = ProjectConfig.builder()
                                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                                .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                                                .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue)
                                                .value("online_agent").build(),
                                        ProjectConfigAttribute.builder()
                                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                                .value(agentFileStorage.getName()).build(),
                                        ProjectConfigAttribute.builder()
                                                .type(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_SOURCE.type)
                                                .name(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_SOURCE.nameValue)
                                                .value("true").build(),
                                        ProjectConfigAttribute.builder()
                                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                                .value("/source/demo_benchmark/c_testcase/advance").build(),
                                        ProjectConfigAttribute.builder()
                                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                                .value("/source/demo_benchmark/c_testcase/advance").build()
                )).build();

        when(scanStatusService.saveScanTaskStatusLog(scanTask1, ScanTaskStatusLog.Stage.PENDING,
                ScanTaskStatusLog.Status.PENDING, 0.0, null,
                "Start preparing task", currentUserName)).thenReturn(scanTaskStatusLog1);
        doNothing().when(asyncScanService).prepareAndCallScan(any(), any(), any());
        scanTaskService.prepareAndCallScan(scanTask1, projectConfig, currentUserName);
    }

    @Test
    void updateScanTaskStatusTestInvalidStatusFail() {
        log.info("[updateScanTaskStatusTestInvalidStatusFail]");
        assertThrows(AppException.class, () -> scanTaskService.updateScanTaskStatus(scanTask1, "test status1", currentUserName));
    }

    @Test
    void updateScanTaskStatusTestSuccess() throws AppException {
        log.info("[updateScanTaskStatusTestSuccess]");
        when(scanTaskRepository.saveAndFlush(any())).thenReturn(scanTask2);
        ScanTask resultScanTask = scanTaskService.updateScanTaskStatus(scanTask1, ScanTask.Status.PROCESSING.name(), currentUserName);
        assertEquals(scanTask2.getId(), resultScanTask.getId());
        assertEquals(scanTask2.getStatus(), resultScanTask.getStatus());
    }

    @Test
    void updateScanTaskStatusTestCompletedSuccess() throws AppException {
        log.info("[updateScanTaskStatusTestCompletedSuccess]");
        Date now = new Date();
        ScanTask expectedScanTask = ScanTask.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111112")).
                status(ScanTask.Status.COMPLETED).modifiedBy(currentUserName).modifiedOn(now).scanEndAt(now).build();
        when(scanTaskRepository.saveAndFlush(any())).thenReturn(expectedScanTask);
        ScanTask resultScanTask = scanTaskService.updateScanTaskStatus(scanTask1, ScanTask.Status.COMPLETED.name(), currentUserName);
        assertEquals(expectedScanTask.getId(), resultScanTask.getId());
        assertEquals(expectedScanTask.getStatus(), resultScanTask.getStatus());
        assertEquals(expectedScanTask.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(expectedScanTask.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(expectedScanTask.getScanStartAt(), resultScanTask.getScanStartAt());
    }

    @Test
    void updateScanTaskStatusTestFailedSuccess() throws AppException {
        log.info("[updateScanTaskStatusTestFailedSuccess]");
        Date now = new Date();
        ScanTask expectedScanTask = ScanTask.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111112")).
                status(ScanTask.Status.FAILED).modifiedBy(currentUserName).modifiedOn(now).scanEndAt(now).build();
        when(scanTaskRepository.saveAndFlush(any())).thenReturn(expectedScanTask);
        ScanTask resultScanTask = scanTaskService.updateScanTaskStatus(scanTask1, ScanTask.Status.FAILED.name(), currentUserName);
        assertEquals(expectedScanTask.getId(), resultScanTask.getId());
        assertEquals(expectedScanTask.getStatus(), resultScanTask.getStatus());
        assertEquals(expectedScanTask.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(expectedScanTask.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(expectedScanTask.getScanStartAt(), resultScanTask.getScanStartAt());
    }


    @Test
    void updateScanTaskStatusTestTerminatedSuccess() throws AppException {
        log.info("[updateScanTaskStatusTestTerminatedSuccess]");
        Date now = new Date();
        ScanTask expectedScanTask = ScanTask.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111112")).
                status(ScanTask.Status.TERMINATED).modifiedBy(currentUserName).modifiedOn(now).scanEndAt(now).build();
        when(scanTaskRepository.saveAndFlush(any())).thenReturn(expectedScanTask);
        ScanTask resultScanTask = scanTaskService.updateScanTaskStatus(scanTask1, ScanTask.Status.TERMINATED.name(), currentUserName);
        assertEquals(expectedScanTask.getId(), resultScanTask.getId());
        assertEquals(expectedScanTask.getStatus(), resultScanTask.getStatus());
        assertEquals(expectedScanTask.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(expectedScanTask.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(expectedScanTask.getScanStartAt(), resultScanTask.getScanStartAt());
    }

    @Test
    void updateScanTaskStatusTestInvalidStageFail() {
        log.info("[updateScanTaskStatusTestInvalidStageFail]");
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder().stage("testInvalidStage").build();
        assertThrows(AppException.class, () -> scanTaskService.updateScanTaskStatus(updateScanTaskRequest, currentUserName));
    }

    @Test
    void updateScanTaskStatusTestInvalidStatusFail1() {
        log.info("[updateScanTaskStatusTestInvalidStatusFail1]");
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder().status("testInvalidStatus").build();
        assertThrows(AppException.class, () -> scanTaskService.updateScanTaskStatus(updateScanTaskRequest, currentUserName));
    }

    @Test
    void updateScanTaskStatusTestScanTaskNotFoundFail() {
        log.info("[updateScanTaskStatusTestScanTaskNotFoundFail]");
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder().id(UUID.randomUUID()).status("testInvalidStatus").build();
        when(scanTaskRepository.findById(updateScanTaskRequest.getId())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> scanTaskService.updateScanTaskStatus(updateScanTaskRequest, currentUserName));
    }

    @Test
    void updateScanTaskStatusTestSuccess1() throws AppException {
        log.info("[updateScanTaskStatusTestSuccess1]");
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder().id(UUID.randomUUID()).stage(ScanTaskStatusLog.Stage.PENDING.name())
                .status(ScanTaskStatusLog.Status.PROCESSING.name()).percentage(0.0).build();
        when(scanTaskRepository.findById(updateScanTaskRequest.getId())).thenReturn(Optional.of(scanTask2));
        when(scanTaskStatusLogRepository.findByScanTaskAndStageAndStatusAndPercentage(scanTask2, ScanTaskStatusLog.Stage.valueOf(updateScanTaskRequest.getStage()),
                ScanTaskStatusLog.Status.valueOf(updateScanTaskRequest.getStatus()), updateScanTaskRequest.getPercentage())).thenReturn(new ArrayList<>());
        when(scanStatusService.saveScanTaskStatusLog(scanTask2, ScanTaskStatusLog.Stage.valueOf(updateScanTaskRequest.getStage()),
                ScanTaskStatusLog.Status.valueOf(updateScanTaskRequest.getStatus()), updateScanTaskRequest.getPercentage(), null,
                updateScanTaskRequest.getMessage(), currentUserName)).thenReturn(scanTaskStatusLog1);
        ScanTask resultScanTask = scanTaskService.updateScanTaskStatus(updateScanTaskRequest, currentUserName);
        assertEquals(scanTask2.getId(), resultScanTask.getId());
        assertEquals(scanTask2.getStatus(), resultScanTask.getStatus());
        assertEquals(scanTask2.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(scanTask2.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(scanTask2.getScanStartAt(), resultScanTask.getScanStartAt());
    }

    @Test
    void updateScanTaskStatusTestSuccess2() throws AppException {
        log.info("[updateScanTaskStatusTestSuccess2]");
        Date now = new Date();
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder().id(UUID.randomUUID()).stage(ScanTaskStatusLog.Stage.SCAN_COMPLETE.name())
                .status(ScanTaskStatusLog.Status.PROCESSING.name()).percentage(0.0).build();
        ScanTask expectedScanTask = ScanTask.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111112")).
                status(ScanTask.Status.PROCESSING).modifiedBy(currentUserName).modifiedOn(now).scanEndAt(now).build();
        when(scanTaskRepository.saveAndFlush(any())).thenReturn(expectedScanTask);
        when(scanTaskRepository.findById(updateScanTaskRequest.getId())).thenReturn(Optional.of(expectedScanTask));
        when(scanTaskStatusLogRepository.findByScanTaskAndStageAndStatusAndPercentage(expectedScanTask, ScanTaskStatusLog.Stage.valueOf(updateScanTaskRequest.getStage()),
                ScanTaskStatusLog.Status.valueOf(updateScanTaskRequest.getStatus()), updateScanTaskRequest.getPercentage())).thenReturn(new ArrayList<>());
        when(scanStatusService.saveScanTaskStatusLog(expectedScanTask, ScanTaskStatusLog.Stage.valueOf(updateScanTaskRequest.getStage()),
                ScanTaskStatusLog.Status.valueOf(updateScanTaskRequest.getStatus()), updateScanTaskRequest.getPercentage(), null,
                updateScanTaskRequest.getMessage(), currentUserName)).thenReturn(scanTaskStatusLog1);
        ScanTask resultScanTask = scanTaskService.updateScanTaskStatus(updateScanTaskRequest, currentUserName);
        verify(scanTaskRepository, times(1)).saveAndFlush(argThat(s -> s.getId() == expectedScanTask.getId()));
        assertEquals(expectedScanTask.getId(), resultScanTask.getId());
        assertEquals(expectedScanTask.getStatus(), resultScanTask.getStatus());
        assertEquals(expectedScanTask.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(expectedScanTask.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(expectedScanTask.getScanStartAt(), resultScanTask.getScanStartAt());
    }

    @Test
    void updateScanTaskStatusTestSuccess3() throws AppException {
        log.info("[updateScanTaskStatusTestSuccess3]");
        Date now = new Date();
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder().id(UUID.randomUUID()).stage(ScanTaskStatusLog.Stage.PENDING.name())
                .status(ScanTaskStatusLog.Status.FAILED.name()).percentage(0.0).build();
        ScanTask expectedScanTask = ScanTask.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111112")).
                status(ScanTask.Status.PROCESSING).modifiedBy(currentUserName).modifiedOn(now).scanEndAt(now).build();
        when(scanTaskRepository.saveAndFlush(any())).thenReturn(expectedScanTask);
        when(scanTaskRepository.findById(updateScanTaskRequest.getId())).thenReturn(Optional.of(expectedScanTask));
        when(scanTaskStatusLogRepository.findByScanTaskAndStageAndStatusAndPercentage(expectedScanTask, ScanTaskStatusLog.Stage.valueOf(updateScanTaskRequest.getStage()),
                ScanTaskStatusLog.Status.valueOf(updateScanTaskRequest.getStatus()), updateScanTaskRequest.getPercentage())).thenReturn(new ArrayList<>());
        when(scanStatusService.saveScanTaskStatusLog(expectedScanTask, ScanTaskStatusLog.Stage.valueOf(updateScanTaskRequest.getStage()),
                ScanTaskStatusLog.Status.valueOf(updateScanTaskRequest.getStatus()), updateScanTaskRequest.getPercentage(), null,
                updateScanTaskRequest.getMessage(), currentUserName)).thenReturn(scanTaskStatusLog1);
        ScanTask resultScanTask = scanTaskService.updateScanTaskStatus(updateScanTaskRequest, currentUserName);
        verify(scanTaskRepository, times(1)).saveAndFlush(argThat(s -> s.getId() == expectedScanTask.getId()));
        assertEquals(expectedScanTask.getId(), resultScanTask.getId());
        assertEquals(expectedScanTask.getStatus(), resultScanTask.getStatus());
        assertEquals(expectedScanTask.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(expectedScanTask.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(expectedScanTask.getScanStartAt(), resultScanTask.getScanStartAt());
    }

    @Test
    void updateScanTaskStatusTestSuccess4() throws AppException {
        log.info("[updateScanTaskStatusTestSuccess4]");
        Date now = new Date();
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder().id(UUID.randomUUID()).stage(ScanTaskStatusLog.Stage.PENDING.name())
                .status(ScanTaskStatusLog.Status.TERMINATED.name()).percentage(0.0).build();
        ScanTask expectedScanTask = ScanTask.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111112")).
                status(ScanTask.Status.PROCESSING).modifiedBy(currentUserName).modifiedOn(now).scanEndAt(now).build();
        when(scanTaskRepository.saveAndFlush(any())).thenReturn(expectedScanTask);
        when(scanTaskRepository.findById(updateScanTaskRequest.getId())).thenReturn(Optional.of(expectedScanTask));
        when(scanTaskStatusLogRepository.findByScanTaskAndStageAndStatusAndPercentage(expectedScanTask, ScanTaskStatusLog.Stage.valueOf(updateScanTaskRequest.getStage()),
                ScanTaskStatusLog.Status.valueOf(updateScanTaskRequest.getStatus()), updateScanTaskRequest.getPercentage())).thenReturn(new ArrayList<>());
        when(scanStatusService.saveScanTaskStatusLog(expectedScanTask, ScanTaskStatusLog.Stage.valueOf(updateScanTaskRequest.getStage()),
                ScanTaskStatusLog.Status.valueOf(updateScanTaskRequest.getStatus()), updateScanTaskRequest.getPercentage(), null,
                updateScanTaskRequest.getMessage(), currentUserName)).thenReturn(scanTaskStatusLog1);
        ScanTask resultScanTask = scanTaskService.updateScanTaskStatus(updateScanTaskRequest, currentUserName);
        verify(scanTaskRepository, times(1)).saveAndFlush(argThat(s -> s.getId() == expectedScanTask.getId()));
        assertEquals(expectedScanTask.getId(), resultScanTask.getId());
        assertEquals(expectedScanTask.getStatus(), resultScanTask.getStatus());
        assertEquals(expectedScanTask.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(expectedScanTask.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(expectedScanTask.getScanStartAt(), resultScanTask.getScanStartAt());
    }

    @Test
    void updateScanTaskStatus_ScanTaskIsCompleted_IgnoreUpdate() throws AppException {
        log.info("[updateScanTaskStatus_ScanTaskIsCompleted_IgnoreUpdate]");
        Date now = new Date();
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder().id(UUID.randomUUID()).stage(ScanTaskStatusLog.Stage.PENDING.name())
                .status(ScanTaskStatusLog.Status.TERMINATED.name()).percentage(0.0).build();
        ScanTask expectedScanTask = ScanTask.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111112")).
                status(ScanTask.Status.COMPLETED).modifiedBy(currentUserName).modifiedOn(now).scanEndAt(now).build();
        when(scanTaskRepository.findById(updateScanTaskRequest.getId())).thenReturn(Optional.of(expectedScanTask));
        when(scanTaskStatusLogRepository.findByScanTaskAndStageAndStatusAndPercentage(expectedScanTask, ScanTaskStatusLog.Stage.valueOf(updateScanTaskRequest.getStage()),
                ScanTaskStatusLog.Status.valueOf(updateScanTaskRequest.getStatus()), updateScanTaskRequest.getPercentage())).thenReturn(new ArrayList<>());
        when(scanStatusService.saveScanTaskStatusLog(expectedScanTask, ScanTaskStatusLog.Stage.valueOf(updateScanTaskRequest.getStage()),
                ScanTaskStatusLog.Status.valueOf(updateScanTaskRequest.getStatus()), updateScanTaskRequest.getPercentage(), null,
                updateScanTaskRequest.getMessage(), currentUserName)).thenReturn(scanTaskStatusLog1);
        ScanTask resultScanTask = scanTaskService.updateScanTaskStatus(updateScanTaskRequest, currentUserName);
        verify(scanTaskRepository, times(0)).saveAndFlush(argThat(s -> s.getId() == expectedScanTask.getId()));
        assertEquals(expectedScanTask.getId(), resultScanTask.getId());
        assertEquals(expectedScanTask.getStatus(), resultScanTask.getStatus());
        assertEquals(expectedScanTask.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(expectedScanTask.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(expectedScanTask.getScanStartAt(), resultScanTask.getScanStartAt());
    }

    @Test
    void updateScanTaskStatusTestScanTaskNotFoundFail2() {
        log.info("[updateScanTaskStatusTestScanTaskNotFoundFail2]");
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder().id(UUID.randomUUID()).status("testInvalidStatus").build();
        when(scanTaskRepository.findFirst1ByProject(project, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> scanTaskService.updateScanTaskStatus(project, updateScanTaskRequest, currentUserName));
    }

    @Test
    void updateScanTaskStatusTestScanTaskNotFoundFail3() {
        log.info("[updateScanTaskStatusTestScanTaskNotFoundFail3]");
        UpdateScanTaskRequest updateScanTaskRequest = UpdateScanTaskRequest.builder().id(UUID.randomUUID()).status("testInvalidStatus").build();
        when(scanTaskRepository.findFirst1ByProject(project, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(scanTask1));
        assertThrows(AppException.class, () -> scanTaskService.updateScanTaskStatus(project, updateScanTaskRequest, currentUserName));
    }

    @Test
    void stopScanTestScanTaskNotFoundFail() {
        log.info("[stopScanTestScanTaskNotFoundFail]");
        UUID scanTaskId = UUID.randomUUID();
        when(scanTaskRepository.findById(scanTaskId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> scanTaskService.stopScan(scanTaskId, currentUserName));
    }

    @Test
    void stopScanTestCompletedSuccess() throws AppException {
        log.info("[stopScanTestCompletedSuccess]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask expectedScanTask = ScanTask.builder().id(scanTaskId).
                status(ScanTask.Status.COMPLETED).modifiedBy(currentUserName).build();
        when(scanTaskRepository.findById(scanTaskId)).thenReturn(Optional.of(expectedScanTask));
        ScanTask resultScanTask = scanTaskService.stopScan(scanTaskId, currentUserName);
        assertEquals(expectedScanTask.getId(), resultScanTask.getId());
        assertEquals(expectedScanTask.getStatus(), resultScanTask.getStatus());
        assertEquals(expectedScanTask.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(expectedScanTask.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(expectedScanTask.getScanStartAt(), resultScanTask.getScanStartAt());
    }


    @Test
    void stopScanTestFailedSuccess() throws AppException {
        log.info("[stopScanTestFailedSuccess]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask expectedScanTask = ScanTask.builder().id(scanTaskId).
                status(ScanTask.Status.FAILED).modifiedBy(currentUserName).build();
        when(scanTaskRepository.findById(scanTaskId)).thenReturn(Optional.of(expectedScanTask));
        ScanTask resultScanTask = scanTaskService.stopScan(scanTaskId, currentUserName);
        assertEquals(expectedScanTask.getId(), resultScanTask.getId());
        assertEquals(expectedScanTask.getStatus(), resultScanTask.getStatus());
        assertEquals(expectedScanTask.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(expectedScanTask.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(expectedScanTask.getScanStartAt(), resultScanTask.getScanStartAt());
    }

    @Test
    void stopScanTestTerminatedSuccess() throws AppException {
        log.info("[stopScanTestFailedSuccess]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask expectedScanTask = ScanTask.builder().id(scanTaskId).
                status(ScanTask.Status.TERMINATED).modifiedBy(currentUserName).build();
        when(scanTaskRepository.findById(scanTaskId)).thenReturn(Optional.of(expectedScanTask));
        ScanTask resultScanTask = scanTaskService.stopScan(scanTaskId, currentUserName);
        assertEquals(expectedScanTask.getId(), resultScanTask.getId());
        assertEquals(expectedScanTask.getStatus(), resultScanTask.getStatus());
        assertEquals(expectedScanTask.getModifiedBy(), resultScanTask.getModifiedBy());
        assertEquals(expectedScanTask.getModifiedOn(), resultScanTask.getModifiedOn());
        assertEquals(expectedScanTask.getScanStartAt(), resultScanTask.getScanStartAt());
    }


    @Test
    void findScanFileByScanTaskTestSuccess() {
        log.info("[findScanFileByScanTaskTestSuccess]");
        ScanFile scanFile1 = ScanFile.builder().id(UUID.randomUUID()).build();
        ScanFile scanFile2 = ScanFile.builder().id(UUID.randomUUID()).build();
        List<ScanFile> scanFileList = Arrays.asList(scanFile1, scanFile2);
        when(scanFileRepository.findByScanTask(scanTask1)).thenReturn(scanFileList);
        List<ScanFile> resultFileList = scanTaskService.findScanFileByScanTask(scanTask1);
        assertEquals(scanFileList.size(), resultFileList.size());
    }

    @Test
    void findFileInfoByScanTaskTestSuccess() {
        log.info("[findFileInfoByScanTaskTestSuccess]");
        FileInfo fileInfo1 = FileInfo.builder().id(UUID.randomUUID()).build();
        FileInfo fileInfo2 = FileInfo.builder().id(UUID.randomUUID()).build();
        List<FileInfo> fileInfoList = Arrays.asList(fileInfo1, fileInfo2);
        when(fileService.findByScanTask(scanTask1)).thenReturn(fileInfoList);
        List<FileInfo> resultFileInfoList = scanTaskService.findFileInfoByScanTask(scanTask1);
        assertEquals(fileInfoList.size(), resultFileInfoList.size());
    }

    @Test
    void findByProjectTestSuccess() {
        log.info("[findByProjectTestSuccess]");
        when(scanTaskRepository.findByProject(project)).thenReturn(scanTaskList);
        List<ScanTask> resultScanTaskList = scanTaskService.findByProject(project);
        assertEquals(scanTaskList.size(), resultScanTaskList.size());
    }

    @Test
    void findAllTestSuccess() {
        log.info("[findAllTestSuccess]");
        when(scanTaskRepository.findAll()).thenReturn(scanTaskList);
        List<ScanTask> resultScanTaskList = scanTaskService.findAll();
        assertEquals(scanTaskList.size(), resultScanTaskList.size());
    }

    @Test
    void convertScanTaskToDtoTestSuccess() {
        log.info("[convertScanTaskToDtoTestSuccess]");
        Date now = new Date();
        ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).project(project).sourceRoot("/share/src/nginx")
                .createdBy(currentUserName).createdOn(now).modifiedBy(currentUserName).modifiedBy(currentUserName)
                .status(ScanTask.Status.COMPLETED).build();
        ScanTaskDto scanTaskDto = ScanTaskService.convertScanTaskToDto(scanTask);
        assertEquals(scanTask.getId(), scanTaskDto.getId());
        assertEquals(project.getId(), scanTaskDto.getProjectUuid());
        assertEquals(project.getProjectId(), scanTaskDto.getProjectId());
        assertEquals(project.getName(), scanTaskDto.getProjectName());
        assertEquals(scanTask.getStatus().name(), scanTaskDto.getStatus());
        assertEquals(scanTask.getSourceRoot(), scanTaskDto.getSourceRoot());
        assertEquals(scanTask.getCreatedBy(), scanTaskDto.getCreatedBy());
        assertEquals(scanTask.getCreatedOn(), scanTaskDto.getCreatedOn());
        assertEquals(scanTask.getModifiedBy(), scanTaskDto.getModifiedBy());
        assertEquals(scanTask.getModifiedOn(), scanTaskDto.getModifiedOn());
    }

    @Test
    void getLatestCommitId_GitlabTypeFileStorage_ShouldReturnCommitId() throws AppException {
        ProjectConfig gitlabProjectConfig = ProjectConfig.builder().id(projectConfig.getId())
                .attributes(new ArrayList<>())
                .status(ProjectConfig.Status.ACTIVE).project(project).build();
        List<ProjectConfigAttribute> gitlabProjectConfigAttributes = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("gitlab").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("gitUrl").value("https://gitlab.com/xxxx").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("c_testcase/advance").build()
        );
        gitlabProjectConfig.setAttributes(gitlabProjectConfigAttributes);

        String expectedCommitId = UUID.randomUUID().toString();
        FileStorage fileStorage = FileStorage.builder().fileStorageType(FileStorage.Type.GITLAB).fileStorageHost("https://gitlab.com").build();
        when(gitlabService.getLatestCommitId(any(), any(), any(), any())).thenReturn(expectedCommitId);
        String commitId = scanTaskService.getLatestCommitId(gitlabProjectConfig, fileStorage);
        assertEquals(expectedCommitId, commitId);
    }

    @Test
    void getLatestCommitId_GitlabTypeFileStorage_ProjectConfigWithEmptyGitUrl_ShouldThrowException() throws AppException {
        ProjectConfig gitlabProjectConfig = ProjectConfig.builder().id(projectConfig.getId())
                .attributes(new ArrayList<>())
                .status(ProjectConfig.Status.ACTIVE).project(project).build();
        List<ProjectConfigAttribute> gitlabProjectConfigAttributes = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("gitlab").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("gitUrl").value("").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("c_testcase/advance").build()
        );
        gitlabProjectConfig.setAttributes(gitlabProjectConfigAttributes);

        FileStorage fileStorage = FileStorage.builder().fileStorageType(FileStorage.Type.GITLAB).fileStorageHost("https://gitlab.com").build();
        assertThrows(AppException.class, () -> scanTaskService.getLatestCommitId(gitlabProjectConfig, fileStorage));
    }

    @Test
    void getLatestCommitId_GithubTypeFileStorage_ShouldReturnCommitId() throws AppException {
        ProjectConfig githubProjectConfig = ProjectConfig.builder().id(projectConfig.getId())
                .attributes(new ArrayList<>())
                .status(ProjectConfig.Status.ACTIVE).project(project).build();
        List<ProjectConfigAttribute> githubProjectConfigAttributes = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("github").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("gitUrl").value("http://github.com/xxxx.git").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("c_testcase/advance").build()
        );
        githubProjectConfig.setAttributes(githubProjectConfigAttributes);


        String expectedCommitId = UUID.randomUUID().toString();
        FileStorage fileStorage = FileStorage.builder().fileStorageType(FileStorage.Type.GITHUB).fileStorageHost("https://github.com").build();
        when(githubService.getLatestCommitId(any(), any())).thenReturn(expectedCommitId);

        String commitId = scanTaskService.getLatestCommitId(githubProjectConfig, fileStorage);
        assertEquals(expectedCommitId, commitId);
    }

    @Test
    void getLatestCommitId_AgentTypeFileStorage_ShouldReturnNullAsCommitId() throws AppException {
        FileStorage fileStorage = FileStorage.builder().fileStorageType(FileStorage.Type.AGENT).fileStorageHost("/").build();
        String commitId = scanTaskService.getLatestCommitId(projectConfig, fileStorage);
        assertNull(commitId);
    }

    @Test
    void updateScanSummary_Success() {
        when(this.scanTaskRepository.saveAndFlush(any(ScanTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ScanTask scanTask = this.scanTaskService.updateScanSummary(scanTask1, "test", "testValue");
        assertTrue(scanTask.getSummary().containsKey("test"));
        assertEquals("testValue", scanTask.getSummary().get("test"));
    }

    @Test
    void getExtraScanTaskIdByProjectIdAndDsr_dsrProject_returnIdForDsr(){
        UUID idForDsr=UUID.randomUUID();
        UUID idForNonDsr=UUID.randomUUID();
        com.xcal.api.entity.v3.ScanTask dummyScanTaskWithBaseline = com.xcal.api.entity.v3.ScanTask.builder().build();
        com.xcal.api.entity.v3.ScanTask scanTaskForDsr = com.xcal.api.entity.v3.ScanTask.builder().id(idForDsr).build();
        com.xcal.api.entity.v3.ScanTask scanTaskForNonDsr = com.xcal.api.entity.v3.ScanTask.builder().id(idForNonDsr).build();
        doReturn(Optional.of(dummyScanTaskWithBaseline)).when(scanTaskDao).getFirstScanTaskWithBaseline(any(UUID.class));
        doReturn(Optional.of(scanTaskForDsr)).when(scanTaskDao).getLastScanTaskByScanTask(any(),any(),anyString());
        doReturn(Optional.of(scanTaskForNonDsr)).when(scanTaskDao).getLastScanTaskByProjectId(any(UUID.class),anyString());
        UUID result = scanTaskService.getExtraScanTaskIdByProjectIdAndDsr(UUID.randomUUID(),true);
        assertEquals(idForDsr, result );

    }

    @Test
    void getExtraScanTaskIdByProjectIdAndDsr_nonDsrProject_returnIdForNonDsr(){
        UUID idForDsr=UUID.randomUUID();
        UUID idForNonDsr=UUID.randomUUID();
        com.xcal.api.entity.v3.ScanTask dummyScanTaskWithBaseline = com.xcal.api.entity.v3.ScanTask.builder().build();
        com.xcal.api.entity.v3.ScanTask scanTaskForDsr = com.xcal.api.entity.v3.ScanTask.builder().id(idForDsr).build();
        com.xcal.api.entity.v3.ScanTask scanTaskForNonDsr = com.xcal.api.entity.v3.ScanTask.builder().id(idForNonDsr).build();
        doReturn(Optional.of(dummyScanTaskWithBaseline)).when(scanTaskDao).getFirstScanTaskWithBaseline(any(UUID.class));
        doReturn(Optional.of(scanTaskForDsr)).when(scanTaskDao).getLastScanTaskByScanTask(any(),any(),anyString());
        doReturn(Optional.of(scanTaskForNonDsr)).when(scanTaskDao).getLastScanTaskByProjectId(any(UUID.class),anyString());
        UUID result = scanTaskService.getExtraScanTaskIdByProjectIdAndDsr(UUID.randomUUID(),false);
        assertEquals(idForNonDsr, result );

    }

    @Test
    void getExtraScanTaskIdByProjectIdAndDsr_runtimeException_runtimeException(){
        UUID idForDsr=UUID.randomUUID();
        UUID idForNonDsr=UUID.randomUUID();
        com.xcal.api.entity.v3.ScanTask scanTaskForDsr = com.xcal.api.entity.v3.ScanTask.builder().id(idForDsr).build();
        com.xcal.api.entity.v3.ScanTask scanTaskForNonDsr = com.xcal.api.entity.v3.ScanTask.builder().id(idForNonDsr).build();
        doThrow(RuntimeException.class).when(scanTaskDao).getFirstScanTaskWithBaseline(any(UUID.class));
        doReturn(Optional.of(scanTaskForDsr)).when(scanTaskDao).getLastScanTaskByScanTask(any(),any(),anyString());
        doReturn(Optional.of(scanTaskForNonDsr)).when(scanTaskDao).getLastScanTaskByProjectId(any(UUID.class),anyString());
        assertThrows(RuntimeException.class,()-> scanTaskService.getExtraScanTaskIdByProjectIdAndDsr(UUID.randomUUID(),true));


    }

    @Test
    void getScanTaskIdResponse_normal_returnTheSameAsDao(){
        ScanTaskIdResponse scanTaskIdResponse = ScanTaskIdResponse.builder().build();
        doReturn(scanTaskIdResponse).when(scanTaskDao).getScanTaskIdResponse(any(UUID.class),anyString());
        ScanTaskIdResponse result=scanTaskService.getScanTaskIdResponse(UUID.randomUUID(),"aaa");
        assertEquals(scanTaskIdResponse, result);
    }


    @Test
    void getScanTaskIdResponse_runtimeException_runtimeException(){
        doThrow(RuntimeException.class).when(scanTaskDao).getScanTaskIdResponse(any(UUID.class),anyString());
        assertThrows(RuntimeException.class,()->scanTaskService.getScanTaskIdResponse(UUID.randomUUID(),"aaa"));

    }

    @Test
    void searchScanTask_normal_returnTheSameAsDao(){
        List<ScanTask.Status> statusList = new ArrayList<>();
        List<ProjectConfigAttribute> existAttributeList = new ArrayList<>();
        List<ProjectConfigAttribute> compareAttributeList = new ArrayList<>();
        Pageable pagable = Pageable.unpaged();

        List<ScanTask> resultScanTaskList = new ArrayList<>();
        Page<ScanTask> daoResult = new PageImpl(resultScanTaskList);

        doReturn(daoResult).when(scanTaskRepository).searchScanTask(any(), any(), any(), any(), any());
        Page<ScanTask> result=scanTaskService.searchScanTask(project, statusList, existAttributeList, compareAttributeList, pagable );
        assertEquals(daoResult, result);
    }


    @Test
    void updateScanTaskStatus_normal_returnTheSameAsDao(){

        doReturn(5).when(scanTaskRepository).updateStatus(any(), any());
        int result=scanTaskService.updateScanTaskStatus("PROCESSING","COMPLETED");
        assertEquals(5, result);
    }
}
