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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.config.WithMockCustomUser;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.FileStorageDto;
import com.xcal.api.model.dto.ScanFileDto;
import com.xcal.api.model.payload.FindFileInfoRequest;
import com.xcal.api.model.payload.RestResponsePage;
import com.xcal.api.model.payload.SearchScanFileRequest;
import com.xcal.api.service.*;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.util.InMemoryResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.net.HttpURLConnection.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class FileControllerTest {

    @NonNull MockMvc mockMvc;

    @NonNull ObjectMapper om;
    @NonNull Tracer tracer;

    @MockBean
    private FileService fileService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private ScanFileService scanFileService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ScanTaskService scanTaskService;

    @MockBean
    private ScanStatusService scanStatusService;

    private Pageable defaultPageable = PageRequest.of(0, 20);

    private final String adminUsername = "admin";
    private UUID fileStorageId = UUID.randomUUID();
    private UUID fileInfoId = UUID.randomUUID();
    private String fileInfoName = "test";
    private String fileInfoPath = "/test/onlyTest.txt";

    private String fileStorageName = "test";
    private FileStorage.Type fileStorageType = FileStorage.Type.VOLUME;
    private String fileStorageHost = "/home/xcalibyte/file_storage";

    private FileStorage fileStorage;

    UUID scanTaskId=UUID.randomUUID();
    UUID projectId=UUID.randomUUID();
    ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().id(projectId).build()).build();

    @BeforeEach
    void setUp() {
        fileStorage = FileStorage.builder()
                .id(fileStorageId)
                .name(fileStorageName)
                .fileStorageType(fileStorageType)
                .fileStorageHost(fileStorageHost)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addFileInfo() throws Exception {
        FileInfo testInputFileInfo = FileInfo.builder().name(fileInfoName).relativePath(fileInfoPath).fileStorage(null).build();
        FileInfo expectedResultFileInfo = FileInfo.builder().name(fileInfoName).relativePath(fileInfoPath)
                .id(fileInfoId)
                .status(FileInfo.Status.ACTIVE)
                .fileStorage(fileStorage)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        when(fileService.addFileInfo(eq(fileStorageId), argThat(fi -> StringUtils.equalsIgnoreCase(testInputFileInfo.getName(), fi.getName())), anyString())).thenReturn(expectedResultFileInfo);
        mockMvc.perform(post("/api/file_service/v2/file_storage/{uuid}/file_info", fileStorageId).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(testInputFileInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResultFileInfo.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResultFileInfo.getName()))
                .andExpect(jsonPath("$.relativePath").value(expectedResultFileInfo.getRelativePath()))
                .andExpect(jsonPath("$.status").value(expectedResultFileInfo.getStatus().toString()))
                .andExpect(jsonPath("$.createdBy").value(expectedResultFileInfo.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(expectedResultFileInfo.getModifiedBy()))
                .andExpect(jsonPath("$.fileStorage.id").value(expectedResultFileInfo.getFileStorage().getId().toString()))
                .andExpect(jsonPath("$.fileStorage.name").value(expectedResultFileInfo.getFileStorage().getName()))
                .andExpect(jsonPath("$.fileStorage.fileStorageType").value(expectedResultFileInfo.getFileStorage().getFileStorageType().toString()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getFileInfo() throws Exception {
        FileInfo expectedResultFileInfo = FileInfo.builder().name(fileInfoName).relativePath(fileInfoPath)
                .id(fileInfoId)
                .status(FileInfo.Status.ACTIVE)
                .fileStorage(fileStorage)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        when(fileService.findFileInfoById(eq(fileInfoId))).thenReturn(Optional.of(expectedResultFileInfo));
        mockMvc.perform(get("/api/file_service/v2/file_info/{uuid}", fileInfoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResultFileInfo.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResultFileInfo.getName()))
                .andExpect(jsonPath("$.relativePath").value(expectedResultFileInfo.getRelativePath()))
                .andExpect(jsonPath("$.status").value(expectedResultFileInfo.getStatus().toString()))
                .andExpect(jsonPath("$.createdBy").value(expectedResultFileInfo.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(expectedResultFileInfo.getModifiedBy()))
                .andExpect(jsonPath("$.fileStorage.id").value(expectedResultFileInfo.getFileStorage().getId().toString()))
                .andExpect(jsonPath("$.fileStorage.name").value(expectedResultFileInfo.getFileStorage().getName()))
                .andExpect(jsonPath("$.fileStorage.fileStorageType").value(expectedResultFileInfo.getFileStorage().getFileStorageType().toString()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getFileInfoNotFound() throws Exception {
        when(fileService.findFileInfoById(eq(fileInfoId))).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/file_service/v2/file_info/{uuid}", fileInfoId)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteFileInfo() throws Exception {
        doNothing().when(fileService).deleteFileInfo(eq(fileInfoId), anyString());
        mockMvc.perform(delete("/api/file_service/v2/file_info/{uuid}", fileInfoId)).andExpect(status().isNoContent());
    }


    @Test
    @WithMockCustomUser(adminUsername)
    void addFile() throws Exception {
        FileInfo expectedResultFileInfo = FileInfo.builder().name(fileInfoName).relativePath(fileInfoPath)
                .id(fileInfoId)
                .status(FileInfo.Status.ACTIVE)
                .fileStorage(fileStorage)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.addFile(eq(file), eq(fileInfoId), anyString())).thenReturn(expectedResultFileInfo);
        when(fileService.checkIntegrityWithCrc32(any(MultipartFile.class), eq("1234"))).thenReturn(true);
        mockMvc.perform(multipart("/api/file_service/v2/file_info/{uuid}/file", fileInfoId).file(file).param("file_checksum", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResultFileInfo.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResultFileInfo.getName()))
                .andExpect(jsonPath("$.relativePath").value(expectedResultFileInfo.getRelativePath()))
                .andExpect(jsonPath("$.status").value(expectedResultFileInfo.getStatus().toString()))
                .andExpect(jsonPath("$.createdBy").value(expectedResultFileInfo.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(expectedResultFileInfo.getModifiedBy()))
                .andExpect(jsonPath("$.fileStorage.id").value(expectedResultFileInfo.getFileStorage().getId().toString()))
                .andExpect(jsonPath("$.fileStorage.name").value(expectedResultFileInfo.getFileStorage().getName()))
                .andExpect(jsonPath("$.fileStorage.fileStorageType").value(expectedResultFileInfo.getFileStorage().getFileStorageType().toString()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addFile_CheckSumIsNotIntegrity_ThrowException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.checkIntegrityWithCrc32(any(MultipartFile.class), eq("1234"))).thenReturn(false);
        mockMvc.perform(multipart("/api/file_service/v2/file_info/{uuid}/file", fileInfoId).file(file).param("file_checksum", "1234"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_CONFLICT))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_INCONSISTENT));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getFile() throws Exception {
        byte[] content = "Testing content".getBytes();
        Resource resource = new InMemoryResource(content);
        when(fileService.getFileAsResource(eq(fileInfoId))).thenReturn(resource);
        mockMvc.perform(get("/api/file_service/v2/file_info/{uuid}/file", fileInfoId))
                .andExpect(status().isOk())
                .andExpect(content().bytes(content));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteFile() throws Exception {
        FileInfo expectedResultFileInfo = FileInfo.builder().name(fileInfoName).relativePath(fileInfoPath)
                .id(fileInfoId)
                .status(FileInfo.Status.DELETED)
                .fileStorage(fileStorage)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        when(fileService.deleteFile(eq(fileInfoId), anyString())).thenReturn(expectedResultFileInfo);
        mockMvc.perform(delete("/api/file_service/v2/file_info/{uuid}/file", fileInfoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResultFileInfo.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResultFileInfo.getName()))
                .andExpect(jsonPath("$.relativePath").value(expectedResultFileInfo.getRelativePath()))
                .andExpect(jsonPath("$.status").value(expectedResultFileInfo.getStatus().toString()))
                .andExpect(jsonPath("$.createdBy").value(expectedResultFileInfo.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(expectedResultFileInfo.getModifiedBy()))
                .andExpect(jsonPath("$.fileStorage.id").value(expectedResultFileInfo.getFileStorage().getId().toString()))
                .andExpect(jsonPath("$.fileStorage.name").value(expectedResultFileInfo.getFileStorage().getName()))
                .andExpect(jsonPath("$.fileStorage.fileStorageType").value(expectedResultFileInfo.getFileStorage().getFileStorageType().toString()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteFileWhenFileNotExist() throws Exception {
        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HTTP_NOT_FOUND, null, CommonUtil.formatString("[File not found]. fileInfo id: {}, path: {}", fileInfoId, fileInfoPath))).when(fileService).deleteFile(eq(fileInfoId), any());
        mockMvc.perform(delete("/api/file_service/v2/file_info/{uuid}/file", fileInfoId)).andExpect(status().isNotFound());
    }


    @Test
    @WithMockCustomUser(adminUsername)
    void addFileStorage() throws Exception {
        FileStorage expectedResultFileStorage = FileStorage.builder()
                .id(fileStorageId)
                .name(fileStorageName)
                .fileStorageType(fileStorageType)
                .fileStorageHost(fileStorageHost)
                .status(FileStorage.Status.ACTIVE)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        when(fileStorageService.add(argThat(fs -> StringUtils.equalsIgnoreCase(fileStorage.getName(), fs.getName())), any())).thenReturn(expectedResultFileStorage);
        mockMvc.perform(post("/api/file_service/v2/file_storage").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(fileStorage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResultFileStorage.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResultFileStorage.getName()))
                .andExpect(jsonPath("$.fileStorageType").value(expectedResultFileStorage.getFileStorageType().toString()))
                .andExpect(jsonPath("$.status").value(expectedResultFileStorage.getStatus().toString()))
                .andExpect(jsonPath("$.createdBy").value(expectedResultFileStorage.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(expectedResultFileStorage.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void listFileStorage() throws Exception {
        List<FileStorage> expectedFileStorageList = Arrays.asList(
                FileStorage.builder().id(UUID.randomUUID()).name("Storage One").fileStorageType(fileStorageType).fileStorageHost(fileStorageHost)
                        .createdBy(adminUsername).modifiedBy(adminUsername).build(),
                FileStorage.builder().id(UUID.randomUUID()).name("Storage Two").fileStorageType(fileStorageType).fileStorageHost(fileStorageHost)
                        .createdBy(adminUsername).modifiedBy(adminUsername).build(),
                FileStorage.builder().id(UUID.randomUUID()).name("Storage Three").fileStorageType(fileStorageType).fileStorageHost(fileStorageHost)
                        .createdBy(adminUsername).modifiedBy(adminUsername).build()
        );
        when(fileStorageService.findAll(defaultPageable)).thenReturn(new RestResponsePage<>(expectedFileStorageList, defaultPageable, expectedFileStorageList.size()));
        mockMvc.perform(get("/api/file_service/v2/file_storages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(expectedFileStorageList.size()))
                .andExpect(jsonPath("$.content[*].name", Matchers.containsInAnyOrder("Storage One", "Storage Two", "Storage Three")));
    }


    @Test
    @WithMockCustomUser(adminUsername)
    void getFileStorage() throws Exception {
        FileStorage expectedResultFileStorage = FileStorage.builder()
                .id(fileStorageId)
                .name(fileStorageName)
                .fileStorageType(fileStorageType)
                .fileStorageHost(fileStorageHost)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        when(fileStorageService.findById(fileStorageId)).thenReturn(Optional.of(expectedResultFileStorage));
        mockMvc.perform(get("/api/file_service/v2/file_storage/{uuid}", fileStorageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResultFileStorage.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResultFileStorage.getName()))
                .andExpect(jsonPath("$.fileStorageType").value(expectedResultFileStorage.getFileStorageType().toString()))
                .andExpect(jsonPath("$.status").value(expectedResultFileStorage.getStatus()))
                .andExpect(jsonPath("$.createdBy").value(expectedResultFileStorage.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(expectedResultFileStorage.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getFileStorag_FileStorageNotFound_ThrowAppException() throws Exception {
        when(fileStorageService.findById(fileStorageId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/file_service/v2/file_storage/{uuid}", fileStorageId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getFileStorageByName() throws Exception {
        FileStorage expectedResultFileStorage = FileStorage.builder()
                .id(fileStorageId)
                .name(fileStorageName)
                .fileStorageType(fileStorageType)
                .fileStorageHost(fileStorageHost)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        when(fileStorageService.findByName(fileStorageName)).thenReturn(Optional.of(expectedResultFileStorage));
        mockMvc.perform(get("/api/file_service/v2/file_storage").param("name", fileStorageName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResultFileStorage.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResultFileStorage.getName()))
                .andExpect(jsonPath("$.fileStorageType").value(expectedResultFileStorage.getFileStorageType().toString()))
                .andExpect(jsonPath("$.status").value(expectedResultFileStorage.getStatus()))
                .andExpect(jsonPath("$.createdBy").value(expectedResultFileStorage.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(expectedResultFileStorage.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getFileStoragByName_FileStorageNotFound_ThrowAppException() throws Exception {
        when(fileStorageService.findByName(fileStorageName)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/file_service/v2/file_storage").param("name", fileStorageName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateFileStorage() throws Exception {
        FileStorageDto inputFileStorageDto = FileStorageDto.builder()
                .id(fileStorageId)
                .name(fileStorageName)
                .fileStorageType(FileStorageDto.Type.valueOf(fileStorageType.toString()))
                .fileStorageHost(fileStorageHost)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        String newFileStorageName = "New File Storage Name";
        FileStorage expectedResultFileStorage = FileStorage.builder()
                .id(fileStorageId)
                .name(newFileStorageName)
                .fileStorageType(fileStorageType)
                .fileStorageHost(fileStorageHost)
                .status(FileStorage.Status.ACTIVE)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        when(fileStorageService.update(argThat(dto -> StringUtils.equalsIgnoreCase(inputFileStorageDto.getId().toString(), dto.getId().toString())), any())).thenReturn(expectedResultFileStorage);
        mockMvc.perform(put("/api/file_service/v2/file_storage/{uuid}", fileStorageId).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(inputFileStorageDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResultFileStorage.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResultFileStorage.getName()))
                .andExpect(jsonPath("$.fileStorageType").value(expectedResultFileStorage.getFileStorageType().toString()))
                .andExpect(jsonPath("$.status").value(expectedResultFileStorage.getStatus().toString()))
                .andExpect(jsonPath("$.createdBy").value(expectedResultFileStorage.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(expectedResultFileStorage.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void inactiveFileStorage() throws Exception {
        FileStorage inputFileStorage = FileStorage.builder()
                .id(fileStorageId)
                .build();
        FileStorage expectedResultFileStorage = FileStorage.builder()
                .id(fileStorageId)
                .name(fileStorageName)
                .fileStorageType(fileStorageType)
                .fileStorageHost(fileStorageHost)
                .status(FileStorage.Status.ACTIVE)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        when(fileStorageService.findById(fileStorageId)).thenReturn(Optional.of(expectedResultFileStorage));
        when(fileStorageService.inactiveFileStorage(argThat(fs -> StringUtils.equalsIgnoreCase(inputFileStorage.getId().toString(), fs.getId().toString())), any())).thenReturn(expectedResultFileStorage);
        mockMvc.perform(delete("/api/file_service/v2/file_storage/{uuid}", fileStorageId).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(inputFileStorage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResultFileStorage.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResultFileStorage.getName()))
                .andExpect(jsonPath("$.fileStorageType").value(expectedResultFileStorage.getFileStorageType().toString()))
                .andExpect(jsonPath("$.status").value(expectedResultFileStorage.getStatus().toString()))
                .andExpect(jsonPath("$.createdBy").value(expectedResultFileStorage.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(expectedResultFileStorage.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void inactiveFileStorage_FileStorageNotFound_ThrowAppException() throws Exception {
        when(fileStorageService.findById(fileStorageId)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/file_service/v2/file_storage/{uuid}", fileStorageId).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(fileStorage)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getFileByScanFileId_ScanFileNotFound_ThrowException() throws Exception {
        UUID scanFileId = UUID.randomUUID();
        when(scanFileService.findScanFileById(scanFileId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/file_service/v2/scan_file/{id}/file", scanFileId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getFileByScanFileId_InputScanFileId_ReturnResource() throws Exception {
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        Project project = Project.builder().build();
        ProjectConfig projectConfig = ProjectConfig.builder().project(project).status(ProjectConfig.Status.ACTIVE).build();
        projectConfig.setAttributes(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .value("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .value("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .value("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .value("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .value("uploadSource").value("true").build()));

        UUID scanFileId = UUID.randomUUID();
        when(scanFileService.findScanFileById(scanFileId)).thenReturn(Optional.of(ScanFile.builder().id(scanFileId).scanTask(scanTask).build()));
        when(projectService.getLatestActiveProjectConfigByProject(any(Project.class))).thenReturn(Optional.of(projectConfig));
        MockMultipartFile mockMultipartFile = new MockMultipartFile(file.getName(), file.getAbsolutePath(), MediaType.APPLICATION_OCTET_STREAM.toString(), new FileInputStream(file));
        when(fileService.getScanFileAsResource(any(), any(), any(), any())).thenReturn(mockMultipartFile.getResource());
        MvcResult result = mockMvc.perform(get("/api/file_service/v2/scan_file/{id}/file", scanFileId)
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM.toString(), result.getResponse().getContentType());
        assertEquals(content, result.getResponse().getContentAsString());
    }


    @Test
    @WithMockCustomUser(adminUsername)
    void getPartialFileByScanFileId_FromLineNumberIsNegative_ThrowException() throws Exception {
        UUID scanFileId = UUID.randomUUID();
        when(scanFileService.findScanFileById(scanFileId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/file_service/v2/scan_file/{id}/file/from/-2/to/5", scanFileId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getPartialFileByScanFileId_ToLineNumberIsNegative_ThrowException() throws Exception {
        UUID scanFileId = UUID.randomUUID();
        when(scanFileService.findScanFileById(scanFileId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/file_service/v2/scan_file/{id}/file/from/2/to/-5", scanFileId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getPartialFileByScanFileId_FromLineNumIsBiggerThanToLineNumberIsNegative_ThrowException() throws Exception {
        UUID scanFileId = UUID.randomUUID();
        when(scanFileService.findScanFileById(scanFileId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/file_service/v2/scan_file/{id}/file/from/2/to/1", scanFileId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getPartialFileByScanFileId_ScanFileNotFound_ThrowException() throws Exception {
        UUID scanFileId = UUID.randomUUID();
        when(scanFileService.findScanFileById(scanFileId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/file_service/v2/scan_file/{id}/file/from/2/to/5", scanFileId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getPartialFileByScanFileId_Success() throws Exception {
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);

        Path tempPartialFilePath = Files.createTempFile("partialFile", ".c");
        String partialContent = "   printf(\"Hello, World!\");\n" +
                "   return 0;\n";
        File partialFile = tempPartialFilePath.toFile();
        FileUtils.writeStringToFile(partialFile, partialContent, StandardCharsets.UTF_8);

        Project project = Project.builder().build();
        ProjectConfig projectConfig = ProjectConfig.builder().project(project).status(ProjectConfig.Status.ACTIVE).build();
        projectConfig.setAttributes(Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .value("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .value("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .value("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .value("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .value("uploadSource").value("true").build()));

        UUID scanFileId = UUID.randomUUID();
        when(scanFileService.findScanFileById(scanFileId)).thenReturn(Optional.of(ScanFile.builder().id(scanFileId).scanTask(scanTask).build()));
        when(projectService.getLatestActiveProjectConfigByProject(any(Project.class))).thenReturn(Optional.of(projectConfig));
        MockMultipartFile mockMultipartFile = new MockMultipartFile(partialFile.getName(), partialFile.getAbsolutePath(), MediaType.APPLICATION_OCTET_STREAM.toString(), new FileInputStream(partialFile));
        when(fileService.getScanFileAsResource(any(), any(), any(), any())).thenReturn(mockMultipartFile.getResource());
        MvcResult result = mockMvc.perform(get("/api/file_service/v2/scan_file/{id}/file/from/4/to/5", scanFileId)
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM.toString(), result.getResponse().getContentType());
//        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void uploadFile_UploadPreprocessFile_ReturnFileInfo() throws Exception {
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        FileInfo fileInfo = FileInfo.builder().id(UUID.randomUUID()).name("testName").build();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("upload_file", file.getAbsolutePath(), MediaType.APPLICATION_OCTET_STREAM.toString(), new FileInputStream(file));
        when(fileService.addFile(any(MultipartFile.class), any(String.class), any(FileInfo.Type.class), any(String.class))).thenReturn(fileInfo);
        mockMvc.perform(multipart("/api/file_service/v2/file/file_info").file(mockMultipartFile).param("file_checksum", "1234").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fileInfo.getId().toString()))
                .andExpect(jsonPath("$.name").value(fileInfo.getName()));
    }

//    @Test
//    @WithMockCustomUser(adminUsername)
//    void importFileInfoToScanTask_CheckSumIsNotIntegrity_ThrowAppException() throws Exception {
//        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
//        when(fileService.checkIntegrityWithCrc32(any(File.class), eq("1234"))).thenReturn(false);
//        when(scanTaskService.findById(any())).thenReturn(Optional.of(scanTask));
//        mockMvc.perform(multipart("/api/file_service/v2/scan_task/{id}/file_info", scanTask.getId()).file(file).param("file_checksum", "1234"))
//                .andExpect(status().is4xxClientError())
//                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
//                .andExpect(jsonPath("$.responseCode").value(HTTP_CONFLICT))
//                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_INCONSISTENT));
//    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importFileInfoToScanTask_ScanTaskNotFound_ThrowAppException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.checkIntegrityWithCrc32(any(File.class), eq("1234"))).thenReturn(true);
        when(scanTaskService.findById(scanTask.getId())).thenReturn(Optional.empty());
        mockMvc.perform(multipart("/api/file_service/v2/scan_task/{id}/file_info", scanTask.getId()).file(file).param("file_checksum", "1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

//    @Test
//    @WithMockCustomUser(adminUsername)
//    void importFileInfoToScanTask__InvalidFile_ThrowAppException() throws Exception {
//        log.info("[importFileInfoToScanTask__InvalidFile_ThrowAppException]");
//        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
//        Path tempFilePath = Files.createTempFile("helloWorld", ".v");
//        String content = "";
//        File tempFile = tempFilePath.toFile();
//        FileUtils.writeStringToFile(tempFile, content, StandardCharsets.UTF_8);
//        when(fileService.checkIntegrityWithCrc32(any(File.class), eq("1234"))).thenReturn(true);
//        when(scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
//        when(scanStatusService.saveScanTaskStatusLog(eq(scanTask), any(), any(), any(), any(), any(), any())).thenReturn(ScanTaskStatusLog.builder().build());
//        mockMvc.perform(multipart("/api/file_service/v2/scan_task/{id}/file_info", scanTask.getId()).file(file).param("file_checksum", "1234"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
//                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
//                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INCORRECT_PARAM));
//    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importFileInfoToScanTask__ValidImportFileinfo_Success() throws Exception {
        log.info("[importFileInfoToScanTask__InconsistentScanTaskId_ThrowAppException]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".v");
        String content = "{\"osType\":\"LINUX\",\"sourceType\":\"volume_upload\",\"sourceCodeFileId\":\"11111111-1111-1111-1112-111111111113\"}";
        File tempFile = tempFilePath.toFile();
        FileUtils.writeStringToFile(tempFile, content, StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, FileUtils.readFileToByteArray(tempFile));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(true);
        when(scanTaskService.findById(any())).thenReturn(Optional.of(scanTask));
        when(scanStatusService.saveScanTaskStatusLog(eq(scanTask), any(), any(), any(), any(), any(), any())).thenReturn(ScanTaskStatusLog.builder().build());
        doNothing().when(fileService).decompressFile(any());
        mockMvc.perform(multipart("/api/file_service/v2/scan_task/{id}/file_info", "11111111-1111-1111-1112-111111111113").file(file).param("file_checksum", "1234"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void searchScanFile_ScanTaskNotEmpty_Success() throws Exception {
        log.info("[searchScanFile_ScanTaskNotEmpty_Success]");
        ScanFile scanFile1 = ScanFile.builder().id(UUID.randomUUID()).scanTask(scanTask).storePath("/usr/lib/h1.c").projectRelativePath("/usr/lib/h1.c").type(ScanFile.Type.FILE).status(ScanFile.Status.ACTIVE).depth(3).parentPath("/usr/lib").build();
        List<ScanFile> scanFileList = Collections.singletonList(scanFile1);
        ScanFileDto scanFileDto = ScanFileDto.builder().id(scanFile1.getId()).storePath("/usr/lib/h1.c").projectRelativePath("/usr/lib/h1.c").type(ScanFile.Type.FILE.toString()).status(ScanFile.Status.ACTIVE.toString()).depth(3).parentPath("/usr/lib").build();
        List<ScanFileDto> scanFileDtoList = Collections.singletonList(scanFileDto);
        Page<ScanFile> pagedScanFiles = new PageImpl<>(scanFileList);
        SearchScanFileRequest searchScanFileRequest = SearchScanFileRequest.builder()
                .scanTaskId(scanTask.getId()).scanFileIds(Collections.singletonList(scanFile1.getId())).build();
        when(scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        when(scanFileService.findByScanFileIds(searchScanFileRequest.getScanFileIds())).thenReturn(scanFileList);
        when(scanFileService.searchScanFile(any(), any(), any(), any(), any())).thenReturn(pagedScanFiles);
        when(scanFileService.convertScanFilesToDto(scanFileList)).thenReturn(scanFileDtoList);
        mockMvc.perform(post("/api/file_service/v2/scan_file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchScanFileRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(scanFile1.getId().toString()));
    }


    @Test
    @WithMockCustomUser(adminUsername)
    void searchScanFile_ScanFileIdsNotEmptyButResultScanFileIsEmpty_ReturnEmpty() throws Exception {
        log.info("[searchScanFile_ScanFileIdsNotEmptyButResultScanFileIsEmpty_ReturnEmpty]");
        ScanFile scanFile1 = ScanFile.builder().id(UUID.randomUUID()).scanTask(scanTask).storePath("/usr/lib/h1.c").projectRelativePath("/usr/lib/h1.c").type(ScanFile.Type.FILE).status(ScanFile.Status.ACTIVE).depth(3).parentPath("/usr/lib").build();
        List<ScanFile> scanFileList = Collections.singletonList(scanFile1);
        ScanFileDto scanFileDto = ScanFileDto.builder().id(scanFile1.getId()).storePath("/usr/lib/h1.c").projectRelativePath("/usr/lib/h1.c").type(ScanFile.Type.FILE.toString()).status(ScanFile.Status.ACTIVE.toString()).depth(3).parentPath("/usr/lib").build();
        List<ScanFileDto> scanFileDtoList = Collections.singletonList(scanFileDto);
        Page<ScanFile> pagedScanFiles = new PageImpl<>(scanFileList);
        SearchScanFileRequest searchScanFileRequest = SearchScanFileRequest.builder()
                .scanTaskId(scanTask.getId()).scanFileIds(Collections.singletonList(scanFile1.getId())).build();
        when(scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        when(scanFileService.findByScanFileIds(searchScanFileRequest.getScanFileIds())).thenReturn(Lists.emptyList());
        when(scanFileService.searchScanFile(any(), any(), any(), any(), any())).thenReturn(pagedScanFiles);
        when(scanFileService.convertScanFilesToDto(scanFileList)).thenReturn(scanFileDtoList);
        mockMvc.perform(post("/api/file_service/v2/scan_file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchScanFileRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void searchScanFile_ScanFileIdsAreEmpty_ReturnScanFiles() throws Exception {
        log.info("[searchScanFile_ScanFileIdsAreEmpty_ReturnScanFiles]");
        ScanFile scanFile1 = ScanFile.builder().id(UUID.randomUUID()).scanTask(scanTask).storePath("/usr/lib/h1.c").projectRelativePath("/usr/lib/h1.c").type(ScanFile.Type.FILE).status(ScanFile.Status.ACTIVE).depth(3).parentPath("/usr/lib").build();
        List<ScanFile> scanFileList = Collections.singletonList(scanFile1);
        ScanFileDto scanFileDto = ScanFileDto.builder().id(scanFile1.getId()).storePath("/usr/lib/h1.c").projectRelativePath("/usr/lib/h1.c").type(ScanFile.Type.FILE.toString()).status(ScanFile.Status.ACTIVE.toString()).depth(3).parentPath("/usr/lib").build();
        List<ScanFileDto> scanFileDtoList = Collections.singletonList(scanFileDto);
        Page<ScanFile> pagedScanFiles = new PageImpl<>(scanFileList);
        SearchScanFileRequest searchScanFileRequest = SearchScanFileRequest.builder()
                .scanTaskId(scanTask.getId()).scanFileIds(Lists.emptyList()).build();
        when(scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        when(scanFileService.searchScanFile(any(), any(), any(), any(), any())).thenReturn(pagedScanFiles);
        when(scanFileService.convertScanFilesToDto(scanFileList)).thenReturn(scanFileDtoList);
        mockMvc.perform(post("/api/file_service/v2/scan_file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchScanFileRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(scanFile1.getId().toString()));
    }


    @Test
    @WithMockCustomUser(adminUsername)
    void searchScanFile_InvalidType_ThrowAppException() throws Exception {
        log.info("[searchScanFile_InvalidType_ThrowAppException]");
        when(scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        String requestPayload = "{\"scanTaskId\":\"" + scanTask.getId() + "\",\"types\":[\"DIRECTORY\",\"FILE\",\"SYMLINK\"],\"depth\":1}";
        mockMvc.perform(post("/api/file_service/v2/scan_file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestPayload.getBytes())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void searchScanFile__ScanTaskIsNotFound_ThrowAppException() throws Exception {
        log.info("[searchScanFile__ScanTaskIsNotFound_ThrowAppException]");
        SearchScanFileRequest searchScanFileRequest = SearchScanFileRequest.builder().scanTaskId(scanTask.getId()).depth(1).build();
        when(scanTaskService.findById(scanTask.getId())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/file_service/v2/scan_file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchScanFileRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void findFileInfo_WithBlankName_ShouldThrowAppException() throws Exception {
        log.info("[findFileInfo_WithBlankName_ShouldThrowAppException]");
        FindFileInfoRequest findFileInfoRequest = FindFileInfoRequest.builder().type(FileInfo.Type.SOURCE.toString()).version("1234").status(FileInfo.Status.ACTIVE.toString()).build();
        mockMvc.perform(post("/api/file_service/v2/file_info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(findFileInfoRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void findFileInfo_WithBlankType_ShouldThrowAppException() throws Exception {
        log.info("[findFileInfo_WithBlankType_ShouldThrowAppException]");
        FindFileInfoRequest findFileInfoRequest = FindFileInfoRequest.builder().name("test").version("1234").status(FileInfo.Status.ACTIVE.toString()).build();
        mockMvc.perform(post("/api/file_service/v2/file_info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(findFileInfoRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void findFileInfo_WithBlankVersion_ShouldThrowAppException() throws Exception {
        log.info("[findFileInfo_WithBlankVersion_ShouldThrowAppException]");
        FindFileInfoRequest findFileInfoRequest = FindFileInfoRequest.builder().name("test").type(FileInfo.Type.SOURCE.toString()).status(FileInfo.Status.ACTIVE.toString()).build();
        mockMvc.perform(post("/api/file_service/v2/file_info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(findFileInfoRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void findFileInfo_WithBlankStatus_ShouldThrowAppException() throws Exception {
        log.info("[findFileInfo_WithBlankStatus_ShouldThrowAppException]");
        FindFileInfoRequest findFileInfoRequest = FindFileInfoRequest.builder().name("test").type(FileInfo.Type.SOURCE.toString()).version("1234").build();
        mockMvc.perform(post("/api/file_service/v2/file_info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(findFileInfoRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void findFileInfo_WithInvalidType_ShouldThrowAppException() throws Exception {
        log.info("[findFileInfo_WithInvalidType_ShouldThrowAppException]");
        FindFileInfoRequest findFileInfoRequest = FindFileInfoRequest.builder().name("test").type("badType").version("1234").status(FileInfo.Status.ACTIVE.toString()).build();
        mockMvc.perform(post("/api/file_service/v2/file_info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(findFileInfoRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INCORRECT_PARAM));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void findFileInfo_WithInvalidStatus_ShouldThrowAppException() throws Exception {
        log.info("[findFileInfo_WithInvalidStatus_ShouldThrowAppException]");
        FindFileInfoRequest findFileInfoRequest = FindFileInfoRequest.builder().name("test").type(FileInfo.Type.SOURCE.toString()).version("1234").status("badStatus").build();
        mockMvc.perform(post("/api/file_service/v2/file_info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(findFileInfoRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INCORRECT_PARAM));
    }
}
