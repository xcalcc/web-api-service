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
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.security.TokenProvider;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class AsyncScanServiceTest {

    private AsyncScanService asyncScanService;
    private FileStorageService fileStorageService;
    private String currentUserName = "user";
    private UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private UUID fileId = UUID.fromString("11111111-1111-1111-1111-111111111113");
    private UUID projectUUID = UUID.fromString("11111111-1111-1111-1111-111111111114");
    private UUID projectConfigUUID = UUID.fromString("11111111-1111-1111-1111-111111111115");
    private final UUID scanTaskId = UUID.fromString("11111111-1111-1110-1111-111111111111");
    private User user = User.builder().id(userId).status(User.Status.ACTIVE).build();
    private Project project = Project.builder().id(projectUUID).projectId("12345").name("prj name").build();
    ProjectConfig projectConfig = ProjectConfig.builder().id(projectUUID).project(project)
            .attributes(Collections.singletonList(ProjectConfigAttribute.builder()
                    .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                    .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue)
                    .value("")
                    .build())).build();

    private ScanTask scanTask = ScanTask.builder().id(scanTaskId).status(ScanTask.Status.PENDING).project(project).projectConfig(projectConfig).build();
    private FileStorage gitLabFileStorage = FileStorage.builder().fileStorageHost("https://gitlab.com").fileStorageType(FileStorage.Type.GITLAB).name("gitlab_storage").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
    private FileStorage agentFileStorage = FileStorage.builder().fileStorageHost("/home/xxx/test").fileStorageType(FileStorage.Type.AGENT).name("agent_storage").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
    private FileStorage uploadFileStorage = FileStorage.builder().fileStorageHost("/share/upload").fileStorageType(FileStorage.Type.VOLUME).name("volume_upload").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
    private FileStorage volumeFileStorage = FileStorage.builder().fileStorageHost("/share/src").fileStorageType(FileStorage.Type.VOLUME).name("volume_src").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();

    @BeforeEach
    void setUp() {
        ScanStatusService scanStatusService = mock(ScanStatusService.class);
        UserService userService = mock(UserService.class);
        TokenProvider tokenProvider = mock(TokenProvider.class);
        fileStorageService = mock(FileStorageService.class);
        ObjectMapper om = new ObjectMapper();
        Tracer tracer = mock(Tracer.class);
        asyncScanService = new AsyncScanService(scanStatusService, fileStorageService, userService, tokenProvider, om, tracer);
        when(userService.findByUsernameOrEmail(currentUserName)).thenReturn(Optional.of(user));
        when(tokenProvider.createToken(user.getId().toString(), asyncScanService.scanTokenExpirationMsec)).thenReturn(user.getId().toString());
        when(fileStorageService.findByName(gitLabFileStorage.getName())).thenReturn(Optional.of(gitLabFileStorage));
        when(fileStorageService.findByName(agentFileStorage.getName())).thenReturn(Optional.of(agentFileStorage));
        when(fileStorageService.findByName(uploadFileStorage.getName())).thenReturn(Optional.of(uploadFileStorage));
        when(fileStorageService.findByName(volumeFileStorage.getName())).thenReturn(Optional.of(volumeFileStorage));
    }

    @Test
    void checkAndUpdateScanConfig_BlankJobQueueParam_ShouldThrowException() {
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Collections.singletonList(ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue)
                        .value("")
                        .build())).build();
        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.checkAndUpdateScanConfig(projectConfig, currentUserName));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, appException.getResponseCode());
    }

    @Test
    void checkAndUpdateScanConfig_ScanConfigContainsInvalidPatternParam_ShouldThrowException() {
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Collections.singletonList(ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue)
                        .value("@#job1")
                        .build())).build();
        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.checkAndUpdateScanConfig(projectConfig, currentUserName));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, appException.getResponseCode());
    }

    @Test
    void checkAndUpdateScanConfig_ValidPublicSignParams_ShouldNotUpdateScanConfig() throws AppException {
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Collections.singletonList(ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue)
                        .value("public_job1")
                        .build())).build();
        Map<String, String> processedScanConfig = asyncScanService.checkAndUpdateScanConfig(projectConfig, currentUserName);
        assertTrue(processedScanConfig.containsKey(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue));
        assertEquals(processedScanConfig.get(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue), "public_job1");
    }

    @Test
    void checkAndUpdateScanConfig_ValidNonPublicJobParams_ShouldUpdateScanConfig() throws AppException {
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Collections.singletonList(ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue)
                        .value("A_job")
                        .build()))
                .build();
        Map<String, String> processedScanConfig = asyncScanService.checkAndUpdateScanConfig(projectConfig, currentUserName);
        assertTrue(processedScanConfig.containsKey(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue));
        assertEquals(processedScanConfig.get(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue), "user_A_job");
    }

    @Test
    void constructScanParam_AgentTypeSourceStorageNameValidParams_ShouldSuccess() throws AppException {
        Path sourceCodePath = Paths.get("/abc/def");
        Path preprocessPath = Paths.get("/abc/def");
        Path scanFilePath = Paths.get("/abc/def");
        FileStorage.Type fileStorageType = FileStorage.Type.AGENT;
        boolean uploadSource = true;
        String sourceStorageName = "agent";
        String token = "1234";

        Map<String, String> scanParams = asyncScanService.constructScanParam(scanTask, sourceCodePath, preprocessPath, scanFilePath, fileStorageType, fileId, uploadSource, sourceStorageName, null, token, null, null, null, null, null, null, null);
        assertEquals(scanTaskId.toString(), scanParams.get("scanTaskId"));
        assertEquals(preprocessPath.toString(), scanParams.get("preprocessPath"));
        assertEquals(scanFilePath.toString(), scanParams.get("scanFilePath"));
        assertEquals("true", scanParams.get("uploadSource"));
        assertEquals("agent", scanParams.get("sourceStorageName"));
        assertNull(scanParams.get("configContent"));
        assertEquals(fileId.toString(), scanParams.get("sourceCodeFileId"));
        assertEquals(sourceCodePath.toString(), scanParams.get("sourceCodePath"));
        assertNull(scanParams.get("sourceCodeAddress"));
    }

    @Test
    void constructScanParam_VCSTypeSourceStorageNameValidParams_ShouldSuccess() throws AppException {
        Path sourceCodePath = Paths.get("/abc/def");
        Path preprocessPath = Paths.get("/abc/def");
        Path scanFilePath = Paths.get("/abc/def");
        FileStorage.Type fileStorageType = FileStorage.Type.GITLAB;
        boolean uploadSource = true;
        String sourceStorageName = "gitlab";
        String token = "1234";
        String vcsToken = UUID.randomUUID().toString();
        String branch = "dev";
        String baselineBranch = "dev";
        String commitId = UUID.randomUUID().toString();
        String baselineCommitId = UUID.randomUUID().toString();

        Map<String, String> scanParams = asyncScanService.constructScanParam(scanTask, sourceCodePath, preprocessPath, scanFilePath, fileStorageType, null, uploadSource, sourceStorageName, null, token, vcsToken, branch, baselineBranch, commitId, baselineCommitId, null, null);
        assertEquals(scanTaskId.toString(), scanParams.get("scanTaskId"));
        assertEquals(preprocessPath.toString(), scanParams.get("preprocessPath"));
        assertEquals(scanFilePath.toString(), scanParams.get("scanFilePath"));
        assertEquals("true", scanParams.get("uploadSource"));
        assertEquals("gitlab", scanParams.get("sourceStorageName"));
        assertNull(scanParams.get("configContent"));
        assertEquals(sourceCodePath.toString(), scanParams.get("sourceCodePath"));
        assertNull(scanParams.get("sourceCodeAddress"));
        assertEquals(vcsToken, scanParams.get("vcsToken"));
        assertEquals(branch, scanParams.get("branch"));
        assertEquals(baselineBranch, scanParams.get("baselineBranch"));
        assertEquals(commitId, scanParams.get("commitId"));
        assertEquals(baselineCommitId, scanParams.get("baselineCommitId"));
        assertNull(scanParams.get("ref"));
    }


    @Test
    void constructScanParam_InvalidPreprocessPath_ThrowException() {
        Path scanFilePath = Paths.get("/abc/def");
        FileStorage.Type fileStorageType = FileStorage.Type.AGENT;
        boolean uploadSource = true;
        String sourceType = "agent";
        String token = "1234";

        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.constructScanParam(scanTask, null, null, scanFilePath, fileStorageType,null, uploadSource, sourceType, null, token, null, null, null, null, null, null, null));
        Assertions.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, appException.getResponseCode());
    }

    @Test
    void constructScanParam_InvalidScanFilePath_ThrowException() {
        Path preprocessPath = Paths.get("/abc/def");
        FileStorage.Type fileStorageType = FileStorage.Type.AGENT;
        boolean uploadSource = true;
        String sourceType = "agent";
        String token = "1234";

        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.constructScanParam(scanTask, null, preprocessPath, null, fileStorageType,null, uploadSource, sourceType, null, token, null, null, null, null, null, null, null));
        Assertions.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, appException.getResponseCode());
    }

    @Test
    void constructScanParam_InvalidSourceType_ThrowException() {
        Path preprocessPath = Paths.get("/abc/def");
        Path scanFilePath = Paths.get("/abc/def");
        FileStorage.Type fileStorageType = FileStorage.Type.AGENT;
        boolean uploadSource = true;
        String token = "1234";
        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.constructScanParam(scanTask, null, preprocessPath, scanFilePath, fileStorageType,null, uploadSource, null, null, token, null, null, null, null, null, null, null));
        Assertions.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, appException.getResponseCode());
    }

    @Test
    void constructScanParam_InvalidToken_ThrowException() {
        Path preprocessPath = Paths.get("/abc/def");
        Path scanFilePath = Paths.get("/abc/def");
        FileStorage.Type fileStorageType = FileStorage.Type.AGENT;
        boolean uploadSource = true;
        String sourceType = "agent";
        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.constructScanParam(scanTask, null, preprocessPath, scanFilePath, fileStorageType,null, uploadSource, sourceType, null, null, null, null, null, null, null, null, null));
        Assertions.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, appException.getResponseCode());
    }

    @Test
    void prepareForScan_GitlabProjectWithValidParams_ShouldSuccess() throws AppException {
        ProjectConfig projectConfig = ProjectConfig.builder().project(project).id(projectConfigUUID)
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(gitLabFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.nameValue)
                                .value("https://gitlab.com").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("c_testcase/basic").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("c_testcase/basic").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue)
                                .value("public_job1").build()
                )).build();

        scanTask.setProjectConfig(projectConfig);
        Map<String, String> scanParams = asyncScanService.prepareForScan(scanTask, projectConfig, currentUserName);
        assertEquals(scanTaskId.toString(), scanParams.get("scanTaskId"));
        assertEquals(Paths.get("c_testcase/basic").toString(), scanParams.get("preprocessPath"));
        assertEquals(new File(asyncScanService.scanVolumePath, scanTask.getId().toString()).toString(), scanParams.get("scanFilePath"));
        assertEquals(gitLabFileStorage.getName(), scanParams.get("sourceStorageName"));
    }


    @Test
    void prepareForScan_AgentProjectWithValidParams_ShouldSuccess() throws AppException {
        ProjectConfig projectConfig = ProjectConfig.builder().project(project).id(projectConfigUUID)
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(agentFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("/home/xxx/c_testcase/basic").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("/home/xxx/c_testcase/basic").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue)
                                .value("public_job1").build()
                )).build();

        scanTask.setProjectConfig(projectConfig);
        Map<String, String> scanParams = asyncScanService.prepareForScan(scanTask, projectConfig, currentUserName);
        assertEquals(scanTaskId.toString(), scanParams.get("scanTaskId"));
        assertEquals(Paths.get("/home/xxx/c_testcase/basic").toString(), scanParams.get("preprocessPath"));
        assertEquals(new File(asyncScanService.scanVolumePath, scanTask.getId().toString()).toString(), scanParams.get("scanFilePath"));
        assertEquals(agentFileStorage.getName(), scanParams.get("sourceStorageName"));

    }

    @Test
    void prepareForScan_InvalidRelativeSourcePath_ThrowException() {
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(gitLabFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.nameValue)
                                .value("https://gitlab.com").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("/home/xxx/c_testcase/basic").build()
                )).build();
        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.prepareForScan(scanTask, projectConfig, currentUserName));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, appException.getResponseCode());
    }

    @Test
    void prepareForScan_InvalidRelativeBuildPath_ThrowException() {
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(gitLabFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.nameValue)
                                .value("https://gitlab.com").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("/home/xxx/c_testcase/basic").build()
                )).build();
        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.prepareForScan(scanTask, projectConfig, currentUserName));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, appException.getResponseCode());
    }

    @Test
    void prepareForScan_InvalidUploadFileId_ThrowException() {

        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(uploadFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("12345").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("/home/xxx/c_testcase/basic/build").build()
                )).build();
        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.prepareForScan(scanTask, projectConfig, currentUserName));
        Assertions.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, appException.getResponseCode());
    }

    @Test
    void prepareForScan_InvalidVolumeFileInfoId_ThrowException() {
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(volumeFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_FILE_INFO_ID.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_FILE_INFO_ID.nameValue)
                                .value(" ").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("12345").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("/home/xxx/c_testcase/basic/build").build()
                )).build();
        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.prepareForScan(scanTask, projectConfig, currentUserName));
        Assertions.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, appException.getResponseCode());
    }

    @Test
    void prepareForScan_InvalidFileStorageType_ThrowException() {
        FileStorage invalidFileStorage = FileStorage.builder().fileStorageHost("/share/src").fileStorageType(FileStorage.Type.SVN).name("volume_src").createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        when(fileStorageService.findByName(invalidFileStorage.getName())).thenReturn(Optional.of(invalidFileStorage));
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(Arrays.asList(ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue)
                                .value(invalidFileStorage.getName()).build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_FILE_INFO_ID.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_FILE_INFO_ID.nameValue)
                                .value(" ").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue)
                                .value("12345").build(),
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue)
                                .value("/home/xxx/c_testcase/basic/build").build()
                )).build();
        AppException appException = Assertions.assertThrows(AppException.class, () -> asyncScanService.prepareForScan(scanTask, projectConfig, currentUserName));
        Assertions.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, appException.getResponseCode());
    }

//    @Test
//    void prepareAndCallScan_ValidParams_ShouldSuccess() throws AppException {
//        FileStorage fileStorage = FileStorage.builder().fileStorageHost("/").fileStorageType(FileStorage.Type.AGENT).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
//        Map<String, String> projectConfigContent = new HashMap<String, String>() {
//            {
//                put("sourceStorageName", "agent");
//                put("githubProjectUrl", "");
//                put("uploadSource", "");
//                put("relativeSourcePath", "/home/xxx/c_testcase/basic");
//                put("relativeBuildPath", "/home/xxx/c_testcase/basic");
//            }
//        };
//        String scanConfig = null;
//        OkHttpClient client = new OkHttpClient();
//        when(client.newCall(any())).thenReturn();
//        asyncScanService.prepareAndCallScan(scanTask, fileStorage, projectConfigContent, scanConfig, currentUserName);
//    }
}
