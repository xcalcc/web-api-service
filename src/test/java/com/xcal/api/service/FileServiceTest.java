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
import com.xcal.api.entity.FileInfo;
import com.xcal.api.entity.FileStorage;
import com.xcal.api.entity.ScanFile;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.exception.AppException;
import com.xcal.api.repository.FileInfoRepository;
import com.xcal.api.repository.FileStorageRepository;
import com.xcal.api.repository.ScanTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.xcal.api.util.VariableUtil.VOLUME_UPLOAD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
class FileServiceTest {
    private FileService fileService;
    private LocalFileService localFileService;
    private FileInfoRepository fileInfoRepository;
    private FileStorageRepository fileStorageRepository;
    private GitlabService gitlabService;
    private GithubService githubService;
    private GerritService gerritService;
    private final String currentUserName = "user";
    private MultipartFile multipartFile;
    @BeforeEach
    void setUp() {
        localFileService = mock(LocalFileService.class);
        fileInfoRepository = mock(FileInfoRepository.class);
        fileStorageRepository = mock(FileStorageRepository.class);
        gitlabService = mock(GitlabService.class);
        githubService = mock(GithubService.class);
        gerritService = mock(GerritService.class);
        fileService = new FileService(localFileService, gitlabService, githubService, gerritService, mock(ProjectService.class), mock(ObjectMapper.class), fileInfoRepository, fileStorageRepository, mock(ScanTaskRepository.class));

        multipartFile = new MultipartFile() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getOriginalFilename() {
                return null;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return new byte[0];
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public Resource getResource() {
                return null;
            }

            @Override
            public void transferTo(File file) throws IOException, IllegalStateException {

            }

            @Override
            public void transferTo(Path dest) throws IOException, IllegalStateException {

            }
        };
    }

    @Test
    void deleteFileInfoBeforeMilliseconds_withoutTempTypeFileInfo_ShouldSuccess() {
        log.info("[deleteFileInfoBeforeMilliseconds_withoutTempTypeFileInfo_ShouldSuccess]");
        FileStorage fileStorage = FileStorage.builder().fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();

        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(fileStorage).type(FileInfo.Type.TEMP).status(FileInfo.Status.ACTIVE).build();
        when(fileInfoRepository.findByTypeAndCreatedOnBefore(any(), any())).thenReturn(new ArrayList<>());
        doNothing().when(fileInfoRepository).delete(fileInfo);
        fileService.deleteFileInfoBeforeMilliseconds(FileInfo.Type.TEMP, 0);
        assertTrue(true);
    }

    @Test
    void deleteFileInfoBeforeMilliseconds_withTempTypeFileInfo_ShouldSuccess() throws IOException {
        log.info("[deleteFileInfoBeforeMilliseconds_withTempTypeFileInfo_ShouldSuccess]");

        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).name(tempFilePath.getFileName().toString()).fileStorage(fileStorage).relativePath(file.getName()).type(FileInfo.Type.TEMP).status(FileInfo.Status.ACTIVE).build();
        List<FileInfo> fileInfoList = new ArrayList<>();
        fileInfoList.add(fileInfo);

        when(fileInfoRepository.findByTypeAndCreatedOnBefore(any(), any())).thenReturn(fileInfoList);
        doNothing().when(fileInfoRepository).delete(fileInfo);
        fileService.deleteFileInfoBeforeMilliseconds(FileInfo.Type.TEMP, 0);
        assertTrue(true);
    }

    @Test
    void getFileAsResource_FileNotFound_ShouldThrowException() {
        log.info("[getFileAsResource_FileNotFound_ShouldThrowException]");
        UUID fileInfoId = UUID.randomUUID();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> fileService.getFileAsResource(fileInfoId));
    }

    @Test
    void getFileAsResource_FileInfoIsNotActive_ShouldThrowException() {
        log.info("[getFileAsResource_FileInfoIsNotActive_ShouldThrowException]");
        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(fileStorage).status(FileInfo.Status.PENDING).build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        assertThrows(AppException.class, () -> fileService.getFileAsResource(fileInfoId));
    }


    @Test
    void getFileAsResource_Success() throws IOException, AppException {
        log.info("[getFileAsResource_Success]");
        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).name("tempFile").fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        when(localFileService.getLocalFilePath(fileInfo)).thenReturn(file.toPath());
        Resource resource = fileService.getFileAsResource(fileInfoId);
        assertEquals(file.length(), resource.getFile().length());
    }

    @Test
    void getFileAsResource_GitlabTypeFileStorage_Success() throws IOException, AppException {
        log.info("[getFileAsResource_GitlabTypeFileStorage_Success]");
        FileStorage fileStorage = FileStorage.builder().fileStorageHost("https://gitlab.com").fileStorageType(FileStorage.Type.GITLAB).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        when(gitlabService.getFileContentAsString(fileInfo, null)).thenReturn(content);
        Resource resource = fileService.getFileAsResource(fileInfo, new HashMap<>(), null, null);
        assertEquals(file.length(), resource.getFile().length());
    }

    @Test
    void getFileAsResource_GithubTypeFileStorage_Success() throws IOException, AppException {
        log.info("[getFileAsResource_GithubTypeFileStorage_Success]");
        FileStorage fileStorage = FileStorage.builder().fileStorageHost("https://github.com").fileStorageType(FileStorage.Type.GITHUB).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        when(githubService.getFileContentAsString(fileInfo, null)).thenReturn(content);
        Resource resource = fileService.getFileAsResource(fileInfo, new HashMap<>(), null, null);
        assertEquals(file.length(), resource.getFile().length());
    }


    @Test
    void getFileAsResource_AgentTypeFileStorage_Success() throws IOException, AppException {
        log.info("[getFileAsResource_AgentTypeFileStorage_Success]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpdir).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).name("tempFile").fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        when(localFileService.getLocalFilePath(fileInfo)).thenReturn(file.toPath());
        Resource resource = fileService.getFileAsResource(fileInfo, new HashMap<>(), null, null);
        assertEquals(file.length(), resource.getFile().length());
    }

    @Test
    void getFileAsResource_GITTypeFileStorage_ShouldThrowException() throws IOException {
        log.info("[getFileAsResource_GITTypeFileStorage_ShouldThrowException]");
        FileStorage fileStorage = FileStorage.builder().fileStorageHost("Default").fileStorageType(FileStorage.Type.GIT).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        assertThrows(AppException.class, () -> fileService.getFileAsResource(fileInfo, new HashMap<>(), null, null));
    }

    @Test
    void getFileAsResource_BothFromLineNumAndToLineNumIsNull_ShouldReturnWholeFileContentAsResource() throws IOException, AppException {
        log.info("[getFileAsResource_BothFromLineNumAndToLineNumIsNull_ShouldReturnWholeFileContentAsResource]");
        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        when(localFileService.getLocalFilePath(fileInfo)).thenReturn(file.toPath());
        Resource resource = fileService.getFileAsResource(fileInfo, new HashMap<>(), null, null);
        assertEquals(file.length(), resource.getFile().length());
    }

    @Test
    void getFileAsResource_FromLineNumIsNull_ShouldReturnWholeFileContentAsResource() throws IOException, AppException {
        log.info("[getFileAsResource_BothFromLineNumAndToLineNumIsNull_ShouldReturnWholeFileContentAsResource]");
        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).name("tempFile").fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        when(localFileService.getLocalFilePath(fileInfo)).thenReturn(file.toPath());
        Resource resource = fileService.getFileAsResource(fileInfo, new HashMap<>(), null, null);
        assertEquals(file.length(), resource.getFile().length());
    }

    @Test
    void getFileAsResource_FromLineNumLagerThanWholeFileLineNumber_ShouldReturnEmptyInfoAsResource() throws IOException, AppException {
        log.info("[getFileAsResource_FromLineNumLagerThanWholeFileLineNumber_ShouldReturnEmptyInfoAsResource]");
        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).name("tempFile").fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        when(localFileService.getLocalFilePath(fileInfo)).thenReturn(file.toPath());
        Resource resource = fileService.getFileAsResource(fileInfo, new HashMap<>(), 100L, 120L);
        assertEquals(0, resource.getFile().length());
    }

    @Test
    void getFileAsResource_ToLineNumLagerThanWholeFileLineNumber_ShouldReturnTruncatedInfoAsResource() throws IOException, AppException {
        log.info("[getFileAsResource_FromLineNumLagerThanWholeFileLineNumber_ShouldReturnEmptyInfoAsResource]");
        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);

        Path partialFilePath = Files.createTempFile("partialHelloWorld", ".c");
        String partialContent = "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File partialFile = partialFilePath.toFile();
        FileUtils.writeStringToFile(partialFile, partialContent, StandardCharsets.UTF_8);

        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        when(localFileService.getLocalFilePath(fileInfo)).thenReturn(file.toPath());
        Resource resource = fileService.getFileAsResource(fileInfo, new HashMap<>(), 3L, 10L);
        assertTrue(FileUtils.contentEqualsIgnoreEOL(resource.getFile(), partialFile, null));
    }

    @Test
    void getFileAsPath_StorageIsAgent_ShouldThrowException() {
        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.AGENT).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        File file = Paths.get(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString()).toFile();
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).name("tempFile").fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        when(localFileService.getLocalFilePath(any())).thenReturn(null);
        AppException appException = assertThrows(AppException.class, () -> fileService.getFileAsPath(fileInfo, new HashMap<>()));
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, appException.getResponseCode());
        assertEquals(AppException.LEVEL_WARN, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_SOURCE_CODE_NOT_UPLOAD, appException.getErrorCode());
    }

    @Test
    void getScanFileAsResource_Success() throws IOException, AppException {
        log.info("[getScanFileAsResource_Success]");
        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).name("tempFile").fileStorage(fileStorage).relativePath(file.getName()).status(FileInfo.Status.ACTIVE).build();
        ScanFile scanFile = ScanFile.builder().fileInfo(fileInfo).build();
        when(localFileService.getLocalFilePath(fileInfo)).thenReturn(file.toPath());
        Map<String, String> attribute = new HashMap<>();
        Resource resource = fileService.getScanFileAsResource(scanFile, attribute, null, null);
        assertEquals(file.length(), resource.getFile().length());
    }

    @Test
    void addFileInfoTestFileStorageNotFoundFail() {
        log.info("[addFileInfoTestFileStorageNotFoundFail]");
        UUID fileStorageId = UUID.randomUUID();
        FileStorage fileStorage = FileStorage.builder().id(fileStorageId).build();
        FileInfo fileInfo = FileInfo.builder().fileStorage(fileStorage).status(FileInfo.Status.ACTIVE).build();
        when(fileStorageRepository.findById(fileStorageId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> fileService.addFileInfo(fileStorageId, fileInfo, currentUserName));
    }

    @Test
    void addFileInfoTestFileInfoAlreadyExistFail() {
        log.info("[addFileInfoTestFileInfoAlreadyExistFail]");
        UUID fileStorageId = UUID.randomUUID();
        FileStorage fileStorage = FileStorage.builder().id(fileStorageId).build();
        FileInfo fileInfo = FileInfo.builder().fileStorage(fileStorage).relativePath("demo_benchmark/c_testcase/Makefile").version("1557409724000").status(FileInfo.Status.ACTIVE).build();
        when(fileStorageRepository.findById(fileStorageId)).thenReturn(Optional.of(fileStorage));
        when(fileInfoRepository.findByFileStorageAndRelativePathAndVersionAndStatusIn(any(FileStorage.class), eq(fileInfo.getRelativePath()), eq(fileInfo.getVersion()), anyList())).thenReturn(Collections.singletonList(fileInfo));
        assertThrows(AppException.class, () -> fileService.addFileInfo(fileStorageId, fileInfo, currentUserName));
    }

    @Test
    void addFileInfoTestSuccess() throws AppException {
        log.info("[addFileInfoTestSuccess]");
        UUID fileStorageId = UUID.randomUUID();
        FileStorage fileStorage = FileStorage.builder().id(fileStorageId).build();
        FileInfo fileInfo = FileInfo.builder().fileStorage(fileStorage).relativePath("demo_benchmark/c_testcase/Makefile").version("1557409724000").status(FileInfo.Status.ACTIVE).build();
        when(fileStorageRepository.findById(fileStorageId)).thenReturn(Optional.of(fileStorage));
        when(fileInfoRepository.findByFileStorageAndRelativePathAndVersionAndStatusIn(fileStorage, fileInfo.getRelativePath(), fileInfo.getVersion(), Arrays.asList(FileInfo.Status.ACTIVE, FileInfo.Status.PENDING))).thenReturn(new ArrayList<>());
        when(fileInfoRepository.saveAndFlush(fileInfo)).thenReturn(fileInfo);
        FileInfo resultFileInfo = fileService.addFileInfo(fileStorageId, fileInfo, currentUserName);
        assertEquals(fileInfo, resultFileInfo);
    }

    @Test
    void findFileInfoByIdTestSuccess() {
        log.info("[findFileInfoByIdTestSuccess]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).status(FileInfo.Status.ACTIVE).build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        Optional<FileInfo> resultOptionalFileInfo = fileService.findFileInfoById(fileInfoId);
        assertTrue(resultOptionalFileInfo.isPresent());
        assertEquals(fileInfo, resultOptionalFileInfo.get());
    }

    @Test
    void addFileTestFileIsNullFail() {
        log.info("[addFileTestFileIsNullFail]");
        assertThrows(AppException.class, () -> fileService.addFile(null, UUID.randomUUID(), currentUserName));
    }

    @Test
    void addFileTestFileInfoNotFoundFail() {
        log.info("[addFileTestFileInfoNotFoundFail]");
        UUID fileInfoId = UUID.randomUUID();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> fileService.addFile(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes()), UUID.randomUUID(), currentUserName));
    }

    @Test
    void addFileTestVolumeSuccess() throws AppException {
        log.info("[addFileTestVolumeSuccess]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.VOLUME).build()).status(FileInfo.Status.ACTIVE).build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        doNothing().when(localFileService).storeFile(any(), any());
        when(fileInfoRepository.saveAndFlush(fileInfo)).thenReturn(fileInfo);
        FileInfo resultFileInfo = fileService.addFile(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes()), fileInfoId, currentUserName);
        assertEquals(fileInfo, resultFileInfo);
    }


    @Test
    void addFileTestFileIsNullFail1() {
        log.info("[addFileTestFileIsNullFail]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).status(FileInfo.Status.ACTIVE).build();
        assertThrows(AppException.class, () -> fileService.addFile(null, fileInfo, currentUserName));
    }

    @Test
    void addFileTestVolumeSuccess1() throws AppException {
        log.info("[addFileTestVolumeSuccess]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.VOLUME).build()).status(FileInfo.Status.ACTIVE).build();
        doNothing().when(localFileService).storeFile(any(), any());
        when(fileInfoRepository.saveAndFlush(fileInfo)).thenReturn(fileInfo);
        FileInfo resultFileInfo = fileService.addFile(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes()), fileInfo, currentUserName);
        assertEquals(fileInfo, resultFileInfo);
    }

    @Test
    void addFileTestGitFail() {
        log.info("[addFileTestGitFail]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.GIT).build()).status(FileInfo.Status.ACTIVE).build();
        assertThrows(AppException.class, () -> fileService.addFile(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes()), fileInfo, currentUserName));
    }

    @Test
    void addFileTestSVNFail() {
        log.info("[addFileTestSVNFail]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.SVN).build()).status(FileInfo.Status.ACTIVE).build();
        assertThrows(AppException.class, () -> fileService.addFile(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes()), fileInfo, currentUserName));
    }

    @Test
    void addFileTestDefaultFail() {
        log.info("[addFileTestDefaultFail]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.GITLAB).build()).status(FileInfo.Status.ACTIVE).build();
        assertThrows(AppException.class, () -> fileService.addFile(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes()), fileInfo, currentUserName));
    }

    @Test
    void addFileTestSuccess4() throws AppException {
        log.info("[addFileTestSuccess4]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.VOLUME).build()).status(FileInfo.Status.ACTIVE).build();
        doNothing().when(localFileService).storeFile(any(), any());
        when(fileInfoRepository.saveAndFlush(any())).thenReturn(fileInfo);
        FileInfo resultFileInfo = fileService.addFile(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes()), fileInfo, currentUserName);
        assertEquals(fileInfo, resultFileInfo);
    }

    @Test
    void deleteFileTestFileInfoNotFoundFail() {
        log.info("[deleteFileTestFileInfoNotFoundFail]");
        UUID fileInfoId = UUID.randomUUID();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> fileService.deleteFile(fileInfoId, currentUserName));
    }

    @Test
    void deleteFileTestVolumeSuccess() throws AppException {
        log.info("[deleteFileTestVolumeSuccess]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.VOLUME).build()).status(FileInfo.Status.ACTIVE).build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        doNothing().when(localFileService).deleteFile(fileInfo);
        when(fileInfoRepository.saveAndFlush(fileInfo)).thenReturn(fileInfo);
        FileInfo resultFileInfo = fileService.deleteFile(fileInfoId, currentUserName);
        assertEquals(fileInfo, resultFileInfo);
    }

    @Test
    void deleteFileTestGitFail() {
        log.info("[deleteFileTestGitFail]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.GIT).build()).status(FileInfo.Status.ACTIVE).build();
        assertThrows(AppException.class, () -> fileService.deleteFile(fileInfo, currentUserName));
    }

    @Test
    void deleteFileTestSVNFail() {
        log.info("[deleteFileTestSVNFail]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.SVN).build()).status(FileInfo.Status.ACTIVE).build();
        assertThrows(AppException.class, () -> fileService.deleteFile(fileInfo, currentUserName));
    }

    @Test
    void deleteFileTestDefaultFail() {
        log.info("[deleteFileTestDefaultFail]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.AGENT).build()).status(FileInfo.Status.ACTIVE).build();
        assertThrows(AppException.class, () -> fileService.deleteFile(fileInfo, currentUserName));
    }

    @Test
    void deleteFileInfoTestFileInfoNotFoundFail() {
        log.info("[deleteFileInfoTestFileInfoNotFoundFail]");
        UUID fileInfoId = UUID.randomUUID();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> fileService.deleteFileInfo(fileInfoId, currentUserName));
    }

    @Test
    void deleteFileInfoTestSuccess() throws AppException {
        log.info("[deleteFileInfoTestSuccess]");
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().id(fileInfoId).fileStorage(FileStorage.builder().fileStorageType(FileStorage.Type.AGENT).build()).status(FileInfo.Status.ACTIVE).build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        doNothing().when(fileInfoRepository).delete(any());
        fileService.deleteFileInfo(fileInfoId, currentUserName);
        assertTrue(true);
    }

    @Test
    void retrieveFileInfoMapTestSuccess() {
        log.info("[retrieveFileInfoMapTestSuccess]");
        FileInfo fileInfo1 = FileInfo.builder().id(UUID.randomUUID()).relativePath("demo_benchmark/c_testcase/Makefile").version("1").build();
        FileInfo fileInfo2 = FileInfo.builder().id(UUID.randomUUID()).relativePath("demo_benchmark/c_testcase/Makefile").version("2").build();
        List<FileInfo> fileInfos = Arrays.asList(fileInfo1, fileInfo2);
        when(fileInfoRepository.findByFileStorageAndRelativePathStartsWithAndStatusIn(any(), any(), any())).thenReturn(fileInfos);
        Map<String, FileInfo> resultFileInfoMap = fileService.retrieveFileInfoMap(FileStorage.builder().build(), "demo_benchmark/c_testcase/Makefile");
        assertEquals(fileInfo1, resultFileInfoMap.get("demo_benchmark/c_testcase/Makefile#1"));
        assertEquals(fileInfo2, resultFileInfoMap.get("demo_benchmark/c_testcase/Makefile#2"));
    }


    @Test
    void retrieveFileInfoMapTestSuccess1() {
        log.info("[retrieveFileInfoMapTestSuccess1]");
        FileInfo fileInfo1 = FileInfo.builder().id(UUID.randomUUID()).relativePath("demo_benchmark/c_testcase/Makefile").version("1").build();
        FileInfo fileInfo2 = FileInfo.builder().id(UUID.randomUUID()).relativePath("demo_benchmark/c_testcase/Makefile").version("2").build();
        List<FileInfo> fileInfos = Arrays.asList(fileInfo1, fileInfo2);
        when(fileInfoRepository.findByFileStorageAndStatusIn(any(), any())).thenReturn(fileInfos);
        Map<String, FileInfo> resultFileInfoMap = fileService.retrieveFileInfoMap(FileStorage.builder().build());
        assertEquals(fileInfo1, resultFileInfoMap.get("demo_benchmark/c_testcase/Makefile#1"));
        assertEquals(fileInfo2, resultFileInfoMap.get("demo_benchmark/c_testcase/Makefile#2"));
    }

    @Test
    void compressFileTestFileNotFoundFail() {
        log.info("[compressFileTestFileNotFoundFail]");
        assertThrows(AppException.class, () -> fileService.compressFile(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString(), null, ""));
    }


    @Test
    void compressFileTestDefaultSuccess() throws AppException, IOException {
        log.info("[compressFileTestDefaultSuccess]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        Path sourcePath = Files.createTempDirectory("testSource");
        Path relativePath = sourcePath.getFileName();
        Path tempFilePath = Files.createTempFile(sourcePath, "helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID scanTaskId = UUID.randomUUID();
        File resultFile = fileService.compressFile(tmpdir, relativePath.toString(), scanTaskId.toString(), "");
        assertTrue(resultFile.exists());
        assertEquals(scanTaskId.toString() + ".zip", resultFile.getName());
    }

    @Test
    void compressFileTestTarSuccess() throws AppException, IOException {
        log.info("[compressFileTestTarSuccess]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        Path sourcePath = Files.createTempDirectory("testSource");
        Path relativePath = sourcePath.getFileName();
        Path tempFilePath = Files.createTempFile(sourcePath, "helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID scanTaskId = UUID.randomUUID();
        File resultFile = fileService.compressFile(tmpdir, relativePath.toString(), scanTaskId.toString(), ArchiveFormat.TAR.getName());
        assertTrue(resultFile.exists());
        assertEquals(scanTaskId.toString() + ".tar", resultFile.getName());
    }


    @Test
    void decompressFileTestIOExceptionFail() throws IOException {
        log.info("[decompressFileTestIOExceptionFail]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        Path sourcePath = Files.createTempDirectory("testSource");
        Path relativePath = sourcePath.getFileName();
        assertThrows(AppException.class, () -> fileService.decompressFile(tmpdir, relativePath.toString(), "content.zip"));
    }

    @Test
    void findByScanTaskTestSuccess() {
        log.info("[findByScanTaskTestSuccess]");
        FileInfo fileInfo1 = FileInfo.builder().id(UUID.randomUUID()).relativePath("demo_benchmark/c_testcase/Makefile").version("1").build();
        FileInfo fileInfo2 = FileInfo.builder().id(UUID.randomUUID()).relativePath("demo_benchmark/c_testcase/Makefile").version("2").build();
        List<FileInfo> fileInfos = Arrays.asList(fileInfo1, fileInfo2);
        ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).build();
        when(fileInfoRepository.findByScanFilesScanTask(scanTask)).thenReturn(fileInfos);
        List<FileInfo> resultFileInfos = fileService.findByScanTask(scanTask);
        assertEquals(fileInfo1, resultFileInfos.get(0));
        assertEquals(fileInfo2, resultFileInfos.get(1));
    }

    @Test
    void getFileAttributesTestSuccess() throws IOException {
        log.info("[getFileAttributesTestSuccess]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        FileService.FileAttributes fileAttributes = fileService.getFileAttributes(tempFilePath);
        assertEquals("2983426903", fileAttributes.checksum);
        assertEquals(6, fileAttributes.numberOfLines);
        assertEquals(74, fileAttributes.fileSize);
    }

    @Test
    void checkAndSaveFileTestNotParsableFail() throws IOException {
        log.info("[checkAndSaveFileTestNotParsableFail]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        assertThrows(AppException.class, () -> fileService.checkAndSaveFile(tempFilePath, "123c", "123c", fileStorage));
    }

    @Test
    void checkAndSaveFileTestCheckSumNotEqualFail() throws IOException {
        log.info("[checkAndSaveFileTestCheckSumNotEqualFail]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        assertThrows(AppException.class, () -> fileService.checkAndSaveFile(tempFilePath, "1234", "123", fileStorage));
    }

    @Test
    void checkAndSaveFileTestSuccess() throws IOException, AppException {
        log.info("[checkAndSaveFileTestSuccess]");

        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        String tmpdir = FileUtils.getTempDirectoryPath();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        when(fileStorageRepository.findByName(VOLUME_UPLOAD)).thenReturn(Optional.of(FileStorage.builder().fileStorageHost(tmpdir).build()));
        Path resultPath = fileService.checkAndSaveFile(tempFilePath, "123", "123", fileStorage);
        assertEquals(tempFilePath.getFileName().toString(), resultPath.getFileName().toString().substring(4));
    }

    @Test
    void getCrc32ChecksumTestSuccess() throws IOException, AppException {
        log.info("[getCrc32ChecksumTestSuccess]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        long crc32Checksum = fileService.getCrc32Checksum(file);
        assertEquals(2983426903L, crc32Checksum);
    }

    @Test
    void getCrc32ChecksumTest_WhenFileNotExist_ShouldThrowException() throws IOException {
        log.info("[getCrc32ChecksumTest_WhenFileNotExist_ShouldThrowException]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        Files.deleteIfExists(tempFilePath);
        assertThrows(AppException.class, () -> fileService.getCrc32Checksum(file));
    }

    @Test
    void addFileTestSuccess() throws AppException {
        log.info("[addFileTestSuccess]");
        String tmpPath = FileUtils.getTempDirectoryPath();
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        FileInfo fileInfo = FileInfo.builder().id(UUID.randomUUID()).build();
        when(fileStorageRepository.findByName(VOLUME_UPLOAD)).thenReturn(Optional.of(FileStorage.builder().fileStorageHost(tmpPath).build()));
        when(fileInfoRepository.saveAndFlush(any())).thenReturn(fileInfo);
        FileInfo resultFileInfo = fileService.addFile(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, content.getBytes()), "2983426903", FileInfo.Type.SOURCE, currentUserName);
        assertEquals(fileInfo, resultFileInfo);
    }

    @Test
    void addFileTestFileStorageNotFoundFail() throws IOException {
        log.info("[addFileTestFileStorageNotFoundFail]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        when(fileStorageRepository.findByName(VOLUME_UPLOAD)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> fileService.addFile(tempFilePath.toFile(), tempFilePath.toFile().getName(), "2983426903", "", true, FileInfo.Type.SOURCE, currentUserName));
    }

    @Test
    void addFileTestFileStorageNotFoundFail1() throws IOException {
        log.info("[addFileTestFileStorageNotFoundFail]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        when(fileStorageRepository.findByName("testStorage")).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> fileService.addFile(tempFilePath.toFile(), tempFilePath.toFile().getName(), "2983426903", "testStorage", true, FileInfo.Type.SOURCE, currentUserName));
    }

    @Test
    void addFileTestSuccess1() throws IOException, AppException {
        log.info("[addFileTestSuccess1]");
        String tmpPath = FileUtils.getTempDirectoryPath();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        FileInfo fileInfo = FileInfo.builder().id(UUID.randomUUID()).build();
        when(fileStorageRepository.findByName(VOLUME_UPLOAD)).thenReturn(Optional.of(FileStorage.builder().fileStorageHost(tmpPath).build()));
        when(fileInfoRepository.saveAndFlush(any())).thenReturn(fileInfo);
        FileInfo resultFileInfo = fileService.addFile(tempFilePath.toFile(), tempFilePath.toFile().getName(), "2983426903", "", true, FileInfo.Type.SOURCE, currentUserName);
        assertEquals(fileInfo, resultFileInfo);
    }

    @Test
    void addFileTestSuccess2() throws IOException, AppException {
        log.info("[addFileTestSuccess2]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        FileInfo fileInfo = FileInfo.builder().id(UUID.randomUUID()).build();
        when(fileStorageRepository.findByName(VOLUME_UPLOAD)).thenReturn(Optional.of(FileStorage.builder().fileStorageHost(tmpdir).build()));
        when(fileInfoRepository.saveAndFlush(any())).thenReturn(fileInfo);
        FileInfo resultFileInfo = fileService.addFile(tempFilePath.toFile(), tempFilePath.toFile().getName(), "2983426903", "", false, FileInfo.Type.SOURCE, currentUserName);
        assertEquals(fileInfo, resultFileInfo);
    }

    @Test
    void saveFileToDiskTestSuccess() throws IOException, AppException {
        log.info("[saveFileToDiskTestSuccess]");

        String tmpPath = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpPath).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        String tmpdir = FileUtils.getTempDirectoryPath();
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        when(fileStorageRepository.findByName(VOLUME_UPLOAD)).thenReturn(Optional.of(FileStorage.builder().fileStorageHost(tmpdir).build()));
        Path resultPath = fileService.saveFileToDisk(tempFilePath, "2983426903", fileStorage);
        assertEquals(tempFilePath.getFileName().toString(), resultPath.getFileName().toString().substring(11));
    }

    @Test
    void getMd5ChecksumTestSuccess() throws AppException {
        log.info("[getMd5ChecksumTestSuccess]");
        String resultMd5 = fileService.getMd5Checksum(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes()));
        assertEquals("b91a4b2655c770f90410dc67dc407633", resultMd5);
    }

    @Test
    void checkIntegrityIsOk() {
        boolean result = fileService.checkIntegrity("1234", "1234");
        assertTrue(result);
    }

    @Test
    void checkIntegrityWithDifferentChecksumValue() {
        boolean result = fileService.checkIntegrity("1234", "12345");
        assertFalse(result);
    }

    @Test
    void checkIntegrityWithEmptyCorrectChecksum() {
        boolean result = fileService.checkIntegrity("1234", null);
        assertFalse(result);
    }

    @Test
    void checkIntegrityWithCrc32TestSuccess() throws IOException, AppException {
        log.info("[checkIntegrityWithCrc32TestSuccess]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        assertTrue(fileService.checkIntegrityWithCrc32(file, "2983426903"));
    }

    @Test
    void checkIntegrityWithCrc32TestSuccess1() throws AppException {
        log.info("[checkIntegrityWithCrc32TestSuccess1]");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        assertTrue(fileService.checkIntegrityWithCrc32(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, content.getBytes()), "2983426903"));
    }

    @Test
    void checkIntegrityWithMd5TestSuccess() throws AppException {
        log.info("[checkIntegrityWithMd5TestSuccess]");
        assertTrue(fileService.checkIntegrityWithMd5(new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes())
                , "b91a4b2655c770f90410dc67dc407633"));
    }

    @Test
    void getTempFilePathTestSuccess() throws AppException {
        log.info("[getTempFilePathTestSuccess]");
        MockMultipartFile upload_file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        File tempFile = FileService.getTempFile(upload_file);
        assertEquals(tempFile.length(), upload_file.getSize());
    }

    @Test
    void decompressFileTestAPPExceptionFail() {
        log.info("[decompressFileTestAPPExceptionFail]");
        UUID fileInfoId = UUID.randomUUID();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> fileService.decompressFile(fileInfoId));
    }

    @Test
    void decompressFileTestIOExceptionFail1() {
        log.info("[decompressFileTestSuccess]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        UUID fileInfoId = UUID.randomUUID();
        FileInfo fileInfo = FileInfo.builder().fileStorage(FileStorage.builder().fileStorageHost(tmpdir).build()).id(fileInfoId).name("content.zip").build();
        when(fileInfoRepository.findById(fileInfoId)).thenReturn(Optional.of(fileInfo));
        assertThrows(AppException.class, () -> fileService.decompressFile(fileInfoId));
    }

    @Test
    void getSha256_Success() throws IOException, AppException {
        log.info("[getSha256_Success]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        String result = fileService.getSha256(file);
        assertEquals("6189f73ce62bc933dea735d43ca64e063fab9da11dc49877413980f04559dc5a", result);
    }

    @Test
    void getSha256_WhenFileNotExist_ShouldThrowException() throws IOException {
        log.info("[getSha256_WhenFileNotExist_ShouldThrowException]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        Files.deleteIfExists(tempFilePath);
        assertThrows(AppException.class, () -> fileService.getSha256(file));
    }

    @Test
    void addFileFromSystem_folderNotExist_AppException() throws AppException {


        assertThrows(AppException.class, () -> {
            fileService.addFileFromFileSystem("00001", "abc.txt", "0", FileInfo.Type.SOURCE, "user");
        });

    }


    @Test
    void getLocalTempFile_fileStrageNotFound_AppException(){

        doReturn(Optional.empty()).when(fileStorageRepository).findByName(anyString());
        assertThrows(AppException.class,()->fileService.getLocalTempFile(multipartFile));
    }
}
