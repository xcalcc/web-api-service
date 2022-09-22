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
import com.xcal.api.metric.ScanMetric;
import com.xcal.api.model.dto.ScanFileDto;
import com.xcal.api.model.payload.ImportFileInfoRequest;
import com.xcal.api.repository.ScanFileRepository;
import com.xcal.api.repository.ScanTaskRepository;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
class ScanFileServiceTest {
    private ScanFileService scanFileService;
    private FileService fileService;
    private FileStorageService fileStorageService;
    private ScanFileRepository scanFileRepository;

    private String currentUserName = "user";
    @BeforeEach
    void setUp() {
        fileService = mock(FileService.class);
        fileStorageService = mock(FileStorageService.class);
        GitlabService gitlabService = mock(GitlabService.class);
        GithubService githubService = mock(GithubService.class);
        ProjectService projectService = mock(ProjectService.class);
        ScanStatusService scanStatusService = mock(ScanStatusService.class);
        MeasureService measureService = mock(MeasureService.class);
        ObjectMapper om = new ObjectMapper();
        scanFileRepository = mock(ScanFileRepository.class);
        ScanTaskRepository scanTaskRepository = mock(ScanTaskRepository.class);
        Tracer tracer = mock(Tracer.class);
        ScanMetric scanMetric = mock(ScanMetric.class);
        scanFileService = new ScanFileService(fileService, fileStorageService, gitlabService, githubService, projectService, scanStatusService, measureService, om, scanFileRepository, scanTaskRepository, tracer, scanMetric);
        scanFileService.saveBatchSize = 5;
        doNothing().when(scanFileRepository).flush();
        when(scanFileRepository.saveAll(any())).thenReturn(new ArrayList<>());
    }


    @Test
    void findScanFileByIdTestSuccess() {
        log.info("[findScanFileByIdTestSuccess]");
        UUID scanFileUuid = UUID.fromString("11111111-1111-1111-1111-111111111110");
        ScanFile scanFile = ScanFile.builder().id(scanFileUuid).build();
        when(scanFileRepository.findById(scanFileUuid)).thenReturn(Optional.of(scanFile));
        Optional<ScanFile> resultOptionalScanFile = scanFileService.findScanFileById(scanFileUuid);
        assertTrue(resultOptionalScanFile.isPresent());
        assertEquals(scanFile.getId(), resultOptionalScanFile.get().getId());
    }

    @Test
    void getDestinationRootTestGetFromFileStorageSuccess() {
        log.info("[getDestinationRootTestGetFromFileStorageSuccess]");
        FileStorage fileStorage = FileStorage.builder().fileStorageHost("/").fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        when(fileStorageService.findByName(any())).thenReturn(Optional.of(fileStorage));
        String resultDestinationRoot = scanFileService.getDestinationRoot();
        assertEquals("/", resultDestinationRoot);
    }

    @Test
    void getDestinationRootTestGetFromPropsSuccess() {
        log.info("[getDestinationRootTestGetFromPropsSuccess]");
        when(fileStorageService.findByName(any())).thenReturn(Optional.empty());
        scanFileService.scanVolumePath = "/Users/Shared/xc5/storage/volume_scan";
        String resultDestinationRoot = scanFileService.getDestinationRoot();
        assertEquals("/Users/Shared/xc5/storage/volume_scan", resultDestinationRoot);
    }

    @Test
    void findByProjectTestSuccess() {
        log.info("[findByProjectTestSuccess]");
        Project project = Project.builder().id(UUID.randomUUID()).build();
        ScanFile scanFile1 = ScanFile.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111")).build();
        ScanFile scanFile2 = ScanFile.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111112")).build();
        List<ScanFile> scanFileList = Arrays.asList(scanFile1, scanFile2);
        when(scanFileRepository.findByScanTaskProject(project)).thenReturn(scanFileList);
        List<ScanFile> resultScanFileList = scanFileService.findByProject(project);
        assertEquals(scanFileList.size(), resultScanFileList.size());
        assertEquals(scanFile1.getId(), resultScanFileList.get(0).getId());
        assertEquals(scanFile2.getId(), resultScanFileList.get(1).getId());
    }

    @Test
    void findByScanTaskTestSuccess() {
        log.info("[findByScanTaskTestSuccess]");
        ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PENDING).build();
        ScanFile scanFile1 = ScanFile.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111")).build();
        ScanFile scanFile2 = ScanFile.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111112")).build();
        List<ScanFile> scanFileList = Arrays.asList(scanFile1, scanFile2);
        when(scanFileRepository.findByScanTask(scanTask)).thenReturn(scanFileList);
        List<ScanFile> resultScanFileList = scanFileService.findByScanTask(scanTask);
        assertEquals(scanFileList.size(), resultScanFileList.size());
        assertEquals(scanFile1.getId(), resultScanFileList.get(0).getId());
        assertEquals(scanFile2.getId(), resultScanFileList.get(1).getId());
    }

    @Test
    void deleteFileOfScanFileTestSuccess() throws IOException {
        log.info("[deleteFileOfScanFileTestSuccess]");
        Path tempFile = Files.createTempFile("hellowold", ".c");
        ScanFile scanFile = ScanFile.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111")).storePath(tempFile.toAbsolutePath().toString()).build();
        scanFileService.deleteFileOfScanFile(scanFile);
        assertFalse(Files.exists(tempFile));
    }

    @Test
    void deleteFileOfProjectTestSuccess() throws IOException {
        log.info("[deleteFileOfScanFileTestSuccess]");
        Project project = Project.builder().id(UUID.randomUUID()).build();
        Path tempFile1 = Files.createTempFile("hellowold1", ".c");
        ScanFile scanFile1 = ScanFile.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111")).storePath(tempFile1.toAbsolutePath().toString()).build();
        Path tempFile2 = Files.createTempFile("hellowold2", ".c");
        ScanFile scanFile2 = ScanFile.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111")).storePath(tempFile2.toAbsolutePath().toString()).build();
        List<ScanFile> scanFileList = Arrays.asList(scanFile1, scanFile2);
        when(scanFileRepository.findByScanTaskProject(project)).thenReturn(scanFileList);
        scanFileService.deleteFileOfProject(project);
        assertFalse(Files.exists(tempFile1));
        assertFalse(Files.exists(tempFile2));
    }

    @Test
    void deleteFileOfScanTaskTestSuccess() throws IOException {
        log.info("[deleteFileOfScanTaskTestSuccess]");
        ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PENDING).build();
        Path tempFile1 = Files.createTempFile("hellowold1", ".c");
        ScanFile scanFile1 = ScanFile.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111")).storePath(tempFile1.toAbsolutePath().toString()).build();
        Path tempFile2 = Files.createTempFile("hellowold2", ".c");
        ScanFile scanFile2 = ScanFile.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111")).storePath(tempFile2.toAbsolutePath().toString()).build();
        List<ScanFile> scanFileList = Arrays.asList(scanFile1, scanFile2);
        when(scanFileRepository.findByScanTask(scanTask)).thenReturn(scanFileList);
        scanFileService.deleteFileOfScanTask(scanTask);
        assertFalse(Files.exists(tempFile1));
        assertFalse(Files.exists(tempFile2));
    }

    @Test
    void deleteScanTaskFolderTestSuccess() throws IOException {
        log.info("[deleteScanTaskFolderTestSuccess]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        UUID scanTaskUuid = UUID.randomUUID();
        Path scanTaskFolderPath = Paths.get(tmpdir, scanTaskUuid.toString());
        Files.createDirectory(Paths.get(tmpdir, scanTaskUuid.toString()));
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpdir).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        ScanTask scanTask = ScanTask.builder().id(scanTaskUuid).status(ScanTask.Status.PENDING).build();
        when(fileStorageService.findByName(any())).thenReturn(Optional.of(fileStorage));
        assertTrue(Files.exists(scanTaskFolderPath));
        scanFileService.deleteScanTaskFolder(scanTask);
        assertFalse(Files.exists(scanTaskFolderPath));
    }

    @Test
    void deleteScanTasksFolderTestSucces() throws IOException {
        log.info("[deleteScanTasksFolderTestSucces]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        UUID scanTaskUuid1 = UUID.randomUUID();
        Path scanTaskFolderPath1 = Paths.get(tmpdir, scanTaskUuid1.toString());
        Files.createDirectory(Paths.get(tmpdir, scanTaskUuid1.toString()));
        ScanTask scanTask1 = ScanTask.builder().id(scanTaskUuid1).status(ScanTask.Status.PENDING).build();
        UUID scanTaskUuid2 = UUID.randomUUID();
        Path scanTaskFolderPath2 = Paths.get(tmpdir, scanTaskUuid2.toString());
        Files.createDirectory(Paths.get(tmpdir, scanTaskUuid2.toString()));
        ScanTask scanTask2 = ScanTask.builder().id(scanTaskUuid2).status(ScanTask.Status.PENDING).build();
        List<ScanTask> scanTaskList = Arrays.asList(scanTask1, scanTask2);
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpdir).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        when(fileStorageService.findByName(any())).thenReturn(Optional.of(fileStorage));
        assertTrue(Files.exists(scanTaskFolderPath1));
        assertTrue(Files.exists(scanTaskFolderPath2));
        scanFileService.deleteScanTasksFolder(scanTaskList);
        assertFalse(Files.exists(scanTaskFolderPath1));
        assertFalse(Files.exists(scanTaskFolderPath2));
    }

    @Test
    void saveFileInfo_FileStorageNotFound_ThrowAppException() throws IOException, AppException {
        log.info("[saveFileInfo_FileStorageNotFound_ThrowAppException]");
        ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PENDING).build();
        when(fileStorageService.findByName(any())).thenReturn(Optional.empty());
        ImportFileInfoRequest importFileInfoRequest = ImportFileInfoRequest.builder()
                .files(Collections.singletonList(ImportFileInfoRequest.File.builder().relativePath("demo_benchmark/c_testcase/main.c").noOfLines("10").build()))
                .osType(ImportFileInfoRequest.OSType.LINUX.toString())
                .gitUrl("https://gitlab.com/xxx")
                .sourceCodeFileId(UUID.randomUUID())
                .build();
        AppException appException = assertThrows(AppException.class, () -> scanFileService.saveFileInfo(scanTask, importFileInfoRequest, currentUserName));
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, appException.getResponseCode());
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_DATA_NOT_FOUND, appException.getErrorCode());
    }

    @Test
    void saveFileInfo_ValidScanTaskAndImportFileInfoRequestForGitlabProject_Success() throws IOException, AppException {
        log.info("[saveFileInfo_ValidScanTaskAndImportFileInfoRequestForGitlabProject_Success]");
        FileStorage fileStorage = FileStorage.builder().fileStorageHost("/").fileStorageType(FileStorage.Type.GITLAB).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Project project = Project.builder().id(UUID.randomUUID()).projectId("test123").build();
        ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).project(project).status(ScanTask.Status.PENDING).build();
        when(fileStorageService.findByName(any())).thenReturn(Optional.of(fileStorage));
        ImportFileInfoRequest importFileInfoRequest = ImportFileInfoRequest.builder()
                .files(Arrays.asList(ImportFileInfoRequest.File.builder().relativePath("demo_benchmark/c_testcase/main.c").depth("3").parentPath("demo_benchmark/c_testcase").type("FILE").noOfLines("10").build(),
                        ImportFileInfoRequest.File.builder().relativePath("demo_benchmark/c_testcase").depth("2").parentPath("demo_benchmark").type("DIRECTORY").noOfLines("0").build(),
                        ImportFileInfoRequest.File.builder().relativePath("demo_benchmark").depth("1").parentPath("/").type("DIRECTORY").noOfLines("0").build(),
                        ImportFileInfoRequest.File.builder().relativePath("/").depth("0").parentPath(null).type("DIRECTORY").noOfLines("0").build()
                ))
                .gitUrl("https://gitlab.com/xxx")
                .osType(ImportFileInfoRequest.OSType.LINUX.toString())
                .sourceCodeFileId(UUID.randomUUID())
                .build();
        assertDoesNotThrow(() -> scanFileService.saveFileInfo(scanTask, importFileInfoRequest, currentUserName));
    }

    @Test
    void saveFileInfo_ValidScanTaskAndImportFileInfoRequestForGithubProject_Success() {
        log.info("[saveFileInfo_ValidScanTaskAndImportFileInfoRequestForGithubProject_Success]");
        FileStorage fileStorage = FileStorage.builder().fileStorageHost("/").fileStorageType(FileStorage.Type.GITHUB).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Project project = Project.builder().id(UUID.randomUUID()).projectId("test123").build();
        ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).project(project).status(ScanTask.Status.PENDING).build();
        when(fileStorageService.findByName(any())).thenReturn(Optional.of(fileStorage));
        ImportFileInfoRequest importFileInfoRequest = ImportFileInfoRequest.builder()
                .files(Arrays.asList(ImportFileInfoRequest.File.builder().relativePath("demo_benchmark/c_testcase/main.c").depth("3").parentPath("demo_benchmark/c_testcase").type("FILE").noOfLines("10").build(),
                        ImportFileInfoRequest.File.builder().relativePath("demo_benchmark/c_testcase").depth("2").parentPath("demo_benchmark").type("DIRECTORY").noOfLines("0").build(),
                        ImportFileInfoRequest.File.builder().relativePath("demo_benchmark").depth("1").parentPath("/").type("DIRECTORY").noOfLines("0").build(),
                        ImportFileInfoRequest.File.builder().relativePath("/").depth("0").parentPath(null).type("DIRECTORY").noOfLines("0").build()
                ))
                .gitUrl("https://github.com/steveli1840/test_github")
                .osType(ImportFileInfoRequest.OSType.LINUX.toString())
                .sourceCodeFileId(UUID.randomUUID())
                .build();
        assertDoesNotThrow(() -> scanFileService.saveFileInfo(scanTask, importFileInfoRequest, currentUserName));
    }

    @Test
    void convertScanFilesToDto_InputDirectoryScanFile_ReturnScanFileDtoList() {
        log.info("[convertScanFilesToDto_InputScanFileList_ReturnScanFileDtoList]");
        ScanFile scanFile1 = ScanFile.builder()
                .id(UUID.randomUUID())
                .projectRelativePath("src/test")
                .storePath("src/test")
                .status(ScanFile.Status.ACTIVE)
                .type(ScanFile.Type.DIRECTORY)
                .parentPath("src")
                .depth(1).build();
        List<ScanFile> scanFileList = Collections.singletonList(scanFile1);
        List<ScanFileDto> scanFileDtos = scanFileService.convertScanFilesToDto(scanFileList);
        assertEquals(1, scanFileDtos.size());
        assertEquals(scanFile1.getId(), scanFileDtos.get(0).getId());
        assertEquals(scanFile1.getProjectRelativePath(), scanFileDtos.get(0).getProjectRelativePath());
        assertEquals(scanFile1.getStorePath(), scanFileDtos.get(0).getStorePath());
        assertEquals(scanFile1.getStatus().toString(), scanFileDtos.get(0).getStatus());
        assertEquals(scanFile1.getType().toString(), scanFileDtos.get(0).getType());
        assertEquals(scanFile1.getParentPath(), scanFileDtos.get(0).getParentPath());
        assertEquals(scanFile1.getDepth(), scanFileDtos.get(0).getDepth());
    }

    @Test
    void convertScanFileToDto_InputScanFileList_ReturnScanFileDtoList() {
        log.info("[convertScanFileToDto_InputScanFileList_ReturnScanFileDtoList]");
        ScanFile scanFile1 = ScanFile.builder()
                .id(UUID.randomUUID())
                .projectRelativePath("src/test")
                .storePath("src/test")
                .status(ScanFile.Status.ACTIVE)
                .type(ScanFile.Type.FILE)
                .parentPath("src")
                .depth(1)
                .fileInfo(FileInfo.builder()
                        .id(UUID.randomUUID())
                        .relativePath("src/test")
                        .version("1")
                        .checksum("12345")
                        .type(FileInfo.Type.SOURCE)
                        .status(FileInfo.Status.ACTIVE).build())
                .build();
        ScanFileDto scanFileDto = ScanFileService.convertScanFileToDto(scanFile1);
        assertEquals(scanFile1.getId(), scanFileDto.getId());
        assertEquals(scanFile1.getProjectRelativePath(), scanFileDto.getProjectRelativePath());
        assertEquals(scanFile1.getStorePath(), scanFileDto.getStorePath());
        assertEquals(scanFile1.getStatus().toString(), scanFileDto.getStatus());
        assertEquals(scanFile1.getType().toString(), scanFileDto.getType());
        assertEquals(scanFile1.getParentPath(), scanFileDto.getParentPath());
        assertEquals(scanFile1.getDepth(), scanFileDto.getDepth());
        assertEquals(scanFile1.getFileInfo().getId(), scanFileDto.getFileInfo().getId());
        assertEquals(scanFile1.getFileInfo().getRelativePath(), scanFileDto.getFileInfo().getRelativePath());
        assertEquals(scanFile1.getFileInfo().getVersion(), scanFileDto.getFileInfo().getVersion());
        assertEquals(scanFile1.getFileInfo().getChecksum(), scanFileDto.getFileInfo().getChecksum());
        assertEquals(scanFile1.getFileInfo().getStatus().toString(), scanFileDto.getFileInfo().getStatus());
    }

    @Test
    void searchScanFile_DirIsNotEmpty_ReturnPageScanFile() {
        log.info("[searchScanFile_DirIsEmpty_ReturnPageScanFile]");
        ScanTask scanTask = ScanTask.builder().build();
        Pageable pageable = PageRequest.of(0, 20);
        List<ScanFile> scanFileList = Collections.singletonList(ScanFile.builder().id(UUID.randomUUID()).build());
        PageImpl<ScanFile> pageScanFiles = new PageImpl<>(scanFileList);
        when(scanFileRepository.searchScanFile(any(), any(), any(), any(), any())).thenReturn(pageScanFiles);
        Page<ScanFile> actualScanFiles = scanFileService.searchScanFile(scanTask, Collections.singletonList(ScanFile.Type.DIRECTORY.toString()), scanFileList, 2, pageable);
        assertEquals(pageScanFiles.getTotalPages(), actualScanFiles.getTotalPages());
        assertEquals(pageScanFiles.getTotalElements(), actualScanFiles.getTotalElements());
        assertEquals(pageScanFiles.getContent().get(0).getId(), actualScanFiles.getContent().get(0).getId());
    }

    @Test
    void buildScanFileTree_InputScanFileMap_ReturnRootScanFile() throws AppException {
        log.info("[buildScanFileTree_inputScanFileMap_ReturnRootScanFile]");
        ScanFile scanFile1 = ScanFile.builder().projectRelativePath("/").storePath("/").type(ScanFile.Type.DIRECTORY).depth(0).build();
        ScanFile scanFile2 = ScanFile.builder().projectRelativePath("src").storePath("src").type(ScanFile.Type.DIRECTORY).parentPath("/").depth(1).build();
        ScanFile scanFile3 = ScanFile.builder().projectRelativePath("main.c").storePath("main.c").type(ScanFile.Type.FILE).parentPath("/").depth(1).build();
        ScanFile scanFile4 = ScanFile.builder().projectRelativePath("src/app.c").storePath("src/util.c").type(ScanFile.Type.FILE).parentPath("src").depth(2).build();
        ScanFile scanFile5 = ScanFile.builder().projectRelativePath("src/util.c").storePath("src/util.c").type(ScanFile.Type.FILE).parentPath("src").depth(2).build();
        HashMap<String, ScanFile> scanFileHashMap = new HashMap<>();
        scanFileHashMap.put(scanFile1.getProjectRelativePath(), scanFile1);
        scanFileHashMap.put(scanFile2.getProjectRelativePath(), scanFile2);
        scanFileHashMap.put(scanFile3.getProjectRelativePath(), scanFile3);
        scanFileHashMap.put(scanFile4.getProjectRelativePath(), scanFile4);
        scanFileHashMap.put(scanFile5.getProjectRelativePath(), scanFile5);
        ScanFile rootScanFile = scanFileService.buildScanFileTree(scanFileHashMap);
        assertEquals(2, rootScanFile.getChildren().size());
        assertTrue(rootScanFile.getChildren().contains(scanFile2));
        assertTrue(rootScanFile.getChildren().contains(scanFile3));
        assertEquals(2, scanFile2.getChildren().size());
        assertTrue(scanFile2.getChildren().contains(scanFile4));
        assertTrue(scanFile2.getChildren().contains(scanFile5));
    }

    @Test
    void buildScanFileTree__NoRootScanFile_ThrowAppException() throws AppException {
        log.info("[buildScanFileTree__NoRootScanFile_ThrowAppException]");
        ScanFile scanFile1 = ScanFile.builder().projectRelativePath("src").storePath("src").type(ScanFile.Type.DIRECTORY).parentPath("/").depth(1).build();
        ScanFile scanFile2 = ScanFile.builder().projectRelativePath("main.c").storePath("main.c").type(ScanFile.Type.FILE).parentPath("/").depth(1).build();
        ScanFile scanFile3 = ScanFile.builder().projectRelativePath("src/app.c").storePath("src/util.c").type(ScanFile.Type.FILE).parentPath("src").depth(2).build();
        ScanFile scanFile4 = ScanFile.builder().projectRelativePath("src/util.c").storePath("src/util.c").type(ScanFile.Type.FILE).parentPath("src").depth(2).build();
        HashMap<String, ScanFile> scanFileHashMap = new HashMap<>();
        scanFileHashMap.put(scanFile1.getProjectRelativePath(), scanFile1);
        scanFileHashMap.put(scanFile2.getProjectRelativePath(), scanFile2);
        scanFileHashMap.put(scanFile3.getProjectRelativePath(), scanFile3);
        scanFileHashMap.put(scanFile4.getProjectRelativePath(), scanFile4);
        AppException appException = Assertions.assertThrows(AppException.class, () -> scanFileService.buildScanFileTree(scanFileHashMap));
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_DATA_INCONSISTENT, appException.getErrorCode());
    }
}
