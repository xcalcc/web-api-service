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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.metric.ScanMetric;
import com.xcal.api.model.dto.ScanFileDto;
import com.xcal.api.model.payload.ImportFileInfoRequest;
import com.xcal.api.repository.ScanFileRepository;
import com.xcal.api.repository.ScanTaskRepository;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScanFileService {
    @Value("${app.scan.volume.dbName}")
    public String scanVolumeDbName;

    @Value("${app.scan.volume.path}")
    public String scanVolumePath;

    @Value("${app.upload.volume.path}")
    public String uploadVolumePath;

    @Value("${preprocess.data.bucket.name}")
    public String preprocessDataBucketName;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    public Integer saveBatchSize;


    @NonNull FileService fileService;
    @NonNull FileStorageService fileStorageService;
    @NonNull GitlabService gitlabService;
    @NonNull GithubService githubService;
    @NonNull ProjectService projectService;
    @NonNull ScanStatusService scanStatusService;
    @NonNull MeasureService measureService;
    @NonNull ObjectMapper om;

    @NonNull ScanFileRepository scanFileRepository;
    @NonNull ScanTaskRepository scanTaskRepository;

    @NonNull Tracer tracer;
    @NonNull ScanMetric scanMetric;


    public Optional<ScanFile> findScanFileById(UUID id) {
        log.debug("[findScanFileById] id: {}", id);
        return this.scanFileRepository.findById(id);
    }

    public List<ScanFile> findScanFileByScanTaskAndType(ScanTask scanTask, ScanFile.Type type) {
        log.info("[findScanFileByScanTaskAndType] scanTaskId: {}, type: {}", scanTask.getId(), type);
        List<ScanFile> scanFiles = this.scanFileRepository.findByScanTaskAndType(scanTask, type);
        log.debug("[findScanFileByScanTaskAndType] scanFiles, size: {}", scanFiles.size());
        return scanFiles;
    }

    public Optional<ScanFile> findScanFileByScanTaskAndRelativePath(UUID scanTaskId, String relativePath) {
        log.info("[findScanFileByScanTaskAndRelativePath] scanTaskId: {}, storePath: {}", scanTaskId, relativePath);
        return this.scanFileRepository.findByScanTaskAndProjectRelativePath(ScanTask.builder().id(scanTaskId).build(), relativePath);
    }

    private Map<String, ScanFile> retrieveScanFileMap(String rootPath, ScanTask scanTask) {
        log.info("[retrieveScanFileMap] rootPath: {}, scanTask: {}", rootPath, scanTask);
        String storePath = Paths.get(rootPath, scanTask.getId().toString()).toString();
        List<ScanFile> scanFiles = this.scanFileRepository.findByScanTaskAndStorePathStartsWith(scanTask, storePath);
        return scanFiles.stream().collect(Collectors.toMap(ScanFile::getStorePath, sf -> sf));
    }

    public String getDestinationRoot() {
        log.debug("[getDestinationRoot]");
        String destinationRoot;
        Optional<FileStorage> scanFileStorageOptional = this.fileStorageService.findByName(this.scanVolumeDbName);
        if (scanFileStorageOptional.isPresent()) {
            destinationRoot = scanFileStorageOptional.get().getFileStorageHost();
        } else {
            destinationRoot = this.scanVolumePath;
        }
        return destinationRoot;
    }

    public void importFileInfoToScanTask(ScanTask scanTask, File file, UserPrincipal userPrincipal) throws AppException {
        importFileInfoToScanTask(scanTask, file, userPrincipal.getUsername());
    }

    public String getDirectoryLocationOfUploadedSourceCode(ScanTask scanTask) {
        return Paths.get(uploadVolumePath, preprocessDataBucketName, scanTask.getProject().getProjectId(),  scanTask.getId().toString()).toString();
    }

    @Async
    public void deleteSourceCodeFile(ScanTask scanTask) {
        log.info("[deleteSourceCodeFile] scanTaskId: {}", scanTask.getId());
        String sourceCodeLocation = this.getDirectoryLocationOfUploadedSourceCode(scanTask);
        File sourceCodeFile = Paths.get(sourceCodeLocation, VariableUtil.SOURCE_CODE_ARCHIVE).toFile();
        File decompressedSourceCodeFile = Paths.get(sourceCodeLocation, VariableUtil.SOURCE_CODE_PATH).toFile();
        if(sourceCodeFile.exists()) {
            fileService.deleteLocalFile(sourceCodeFile);
        }
        if(decompressedSourceCodeFile.exists()) {
            fileService.deleteLocalFile(decompressedSourceCodeFile);
        }
    }

    public void importFileInfoToScanTask(ScanTask scanTask, File file, String username) throws AppException {
        try (InputStream is = new FileInputStream(file)) {
            log.info("[importFileInfoToScanTask] file available: {}", is.available());
            ImportFileInfoRequest importFileInfoRequest = this.om.readValue(is, ImportFileInfoRequest.class);

            this.saveFileInfo(scanTask, importFileInfoRequest, username);
            if (StringUtils.equalsIgnoreCase(importFileInfoRequest.getSourceType(), VariableUtil.VOLUME_UPLOAD)) {
                // Upload and client mode need to decompress the source code archive
                try {//optional action to decompress file
                    String sourceCodeLocation = getDirectoryLocationOfUploadedSourceCode(scanTask);
                    if(Paths.get(sourceCodeLocation, VariableUtil.SOURCE_CODE_ARCHIVE).toFile().exists())
                        this.fileService.decompressFile(sourceCodeLocation, VariableUtil.SOURCE_CODE_PATH, VariableUtil.SOURCE_CODE_ARCHIVE);
                }catch(Exception e){
                    log.warn("[importFileInfoToScanTask] Error decompressing source code file");
                }
            }
        } catch (Exception e) {
            log.error("[importFileInfoToScanTask] error message: {}: {}", e.getClass(), e.getMessage());
            throw new AppException(
                    AppException.LEVEL_WARN,
                    AppException.ERROR_CODE_INCORRECT_PARAM,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    AppException.ErrorCode.E_API_FILE_IMPORT_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] fileName: {}",
                            AppException.ErrorCode.E_API_FILE_IMPORT_FILE_FAILED.messageTemplate,
                            file.getName()
                    ),
                    e
            );
        }
    }

    public List<ScanFile> saveFileInfo(
            ScanTask scanTask,
            List<ImportFileInfoRequest.File> files,
            FileStorage fileStorage,
            String fileStorageExtraInfo,
            UUID sourceCodeFileId,
            String currentUsername
    ) throws AppException {
        log.debug(
                "[saveFileInfo] scanTask, id: {}, importFileInfoRequest.files, size: {}, fileStorage, name: {}, fileStorageExtraInfo: {}, sourceCodeFileId: {}, currentUsername: {}",
                scanTask.getId(),
                files.size(),
                fileStorage.getName(),
                fileStorageExtraInfo,
                sourceCodeFileId,
                currentUsername
        );
        String destinationRoot = this.getDestinationRoot();
        List<ScanFile> scanFiles = new ArrayList<>();
        Date now = new Date();
        Map<String, FileInfo> fileInfoMap = new HashMap<>();
        if (Arrays.asList(FileStorage.Type.GITLAB, FileStorage.Type.GITLAB_V3, FileStorage.Type.GITHUB, FileStorage.Type.GERRIT)
                .contains(fileStorage.getFileStorageType()) && files.size() != 0) {
            String commitId = files.get(0).getVersion();
            fileInfoMap = this.fileService.retrieveFileInfoMapByFileStorageAndVersion(fileStorage, commitId);
        }
        Map<String, ScanFile> scanFileMap = this.retrieveScanFileMap(destinationRoot, scanTask);
        for (ImportFileInfoRequest.File file : files) {
            // volume_src and volume_upload have different relativePath
            // Attention: relativePath can only be the separator of the operating system
            Path realRelativePath = Paths.get(file.getRelativePath());
            // to avoid file info table unique constraint, AGENT_FILE_STORAGE_NAME also use this realRelativePath at this time.
            if (StringUtils.equalsIgnoreCase(fileStorage.getName(), VariableUtil.VOLUME_UPLOAD) ||
                StringUtils.equalsIgnoreCase(fileStorage.getName(), VariableUtil.AGENT_FILE_STORAGE_NAME)) {
                realRelativePath = Paths.get(preprocessDataBucketName, scanTask.getProject().getProjectId(), scanTask.getId().toString(), VariableUtil.SOURCE_CODE_PATH, FilenameUtils.separatorsToSystem(file.getRelativePath()));
            }
            // use realRelativePath, not relative path attribute of file here
            FileInfo fileInfo = Optional.ofNullable(fileInfoMap.get(realRelativePath + "#" + file.getVersion()))
                    .orElse(FileInfo.builder()
                        .fileStorage(fileStorage)
                        .name(file.getFileName())
                        .relativePath(realRelativePath.toString())
                        .version(file.getVersion())
                        .checksum(file.getChecksum())
                        .noOfLines(Integer.parseInt(file.getNoOfLines()))
                        .fileStorageExtraInfo(fileStorageExtraInfo)
                        .type(FileInfo.Type.SOURCE)
                        .status(FileInfo.Status.ACTIVE)
                        .createdBy(currentUsername).createdOn(now)
                        .modifiedBy(currentUsername).modifiedOn(now)
                        .build());

            ScanFile scanFile = Optional.ofNullable(scanFileMap.get(file.getFilePath()))
                    .orElse(ScanFile.builder()
                            .scanTask(scanTask)
                            .fileInfo(fileInfo)
                            .storePath(file.getFilePath())
                            .projectRelativePath(file.getRelativePath())
                            .status(ScanFile.Status.ACTIVE)
                            .type(EnumUtils.getEnumIgnoreCase(ScanFile.Type.class, file.getType()))
                            .depth(Integer.parseInt(file.getDepth()))
                            .parentPath(file.getParentPath())
                            .createdBy(currentUsername).createdOn(now)
                            .modifiedBy(currentUsername).modifiedOn(now)
                            .build());

            if (scanFile.getFileInfo() != null && scanFile.getFileInfo().getId() != fileInfo.getId()) {
                scanFile.setFileInfo(fileInfo);
                scanFile.setModifiedBy(currentUsername);
                scanFile.setModifiedOn(now);
            }
            scanFiles.add(scanFile);
        }
        Map<String, ScanFile> relativePathScanFileMap = scanFiles.stream().collect(Collectors.toMap(ScanFile::getProjectRelativePath, sf -> sf));
        ScanFile scanFileTree = this.buildScanFileTree(relativePathScanFileMap);
        this.buildScanFileLeftRight(scanFileTree);
        return this.saveScanFilesByBatch(scanFiles);
    }

    public ScanFile buildScanFileTree(Map<String, ScanFile> scanFileMap) throws AppException {
        log.info("[buildScanFileTree] scan file size: {}", scanFileMap.size());
        for (ScanFile scanFile : scanFileMap.values()) {
            if (scanFile.getParentPath() != null && scanFile.getDepth() != 0) {
                ScanFile parentScanFile = scanFileMap.get(scanFile.getParentPath());
                if (parentScanFile != null) {
                    parentScanFile.getChildren().add(scanFile);
                } else {
                    throw new AppException(
                            AppException.LEVEL_ERROR,
                            AppException.ERROR_CODE_DATA_INCONSISTENT,
                            HttpURLConnection.HTTP_BAD_REQUEST,
                            AppException.ErrorCode.E_API_FILE_IMPORTFILEINFO_PARENT_NOT_FOUND.unifyErrorCode,
                            CommonUtil.formatString(
                                    "[{}] scan file relative path: {}",
                                    AppException.ErrorCode.E_API_FILE_IMPORTFILEINFO_PARENT_NOT_FOUND.messageTemplate,
                                    scanFile.getProjectRelativePath()
                            )
                    );
                }
            }
        }
        List<ScanFile> rootScanFileList = scanFileMap.values().stream().filter(sf -> sf.getParentPath() == null && sf.getDepth() == 0).collect(Collectors.toList());
        if (rootScanFileList.size() != 1) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_INCONSISTENT, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_IMPORTFILEINFO_ROOT_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_FILE_IMPORTFILEINFO_ROOT_NOT_FOUND.messageTemplate);
        }
        return rootScanFileList.get(0);
    }

    public void buildScanFileLeftRight(ScanFile scanFile) {
        log.info("[buildScanFileLeftRight] scan file relativePath: {}, currentIndex: 1", scanFile.getProjectRelativePath());
        buildScanFileLeftRight(scanFile, 1);
    }

    public long buildScanFileLeftRight(ScanFile scanFile, long currentIndex) {
        log.info("[buildScanFileLeftRight] scan file relativePath: {}, currentIndex: {}", scanFile.getProjectRelativePath(), currentIndex);
        scanFile.setTreeLeft(currentIndex);
        currentIndex++;
        for (ScanFile child : scanFile.getChildren()) {
            currentIndex = buildScanFileLeftRight(child, currentIndex);
        }
        scanFile.setTreeRight(currentIndex);
        currentIndex++;
        return currentIndex;
    }

    public List<ScanFile> saveScanFilesByBatch(List<ScanFile> scanFileList) {
        log.info("[saveScanFilesByBatch] scan file size: {}", scanFileList.size());
        List<ScanFile> resultScanFileList = new ArrayList<>();
        for (List<ScanFile> saveList : ListUtils.partition(scanFileList, saveBatchSize)) {
            resultScanFileList.addAll(scanFileRepository.saveAll(saveList));
            this.scanFileRepository.flush();
        }
        return resultScanFileList;
    }

    public List<ScanFile> findByProject(Project project) {
        log.info("[findByProject] project: {}", project);
        List<ScanFile> scanFiles = this.scanFileRepository.findByScanTaskProject(project);
        log.debug("[findByProject] scanFiles, size: {}", scanFiles.size());
        return scanFiles;
    }

    public List<ScanFile> findByScanTask(ScanTask scanTask) {
        log.info("[findByScanTask] scanTask: {}", scanTask);
        List<ScanFile> scanFiles = this.scanFileRepository.findByScanTask(scanTask);
        log.debug("[findByProject] scanFiles, size: {}", scanFiles.size());
        return scanFiles;
    }

    public void deleteFileOfScanFile(ScanFile scanFile) {
        log.info("[deleteScanFile] scanFile: {}", scanFile);
        FileUtils.deleteQuietly(new File(scanFile.getStorePath()));
    }

    public void deleteFileOfProject(Project project) {
        log.info("[deleteFileOfProject] project: {}", project);
        List<ScanFile> scanFiles = this.findByProject(project);
        scanFiles.parallelStream().forEach(this::deleteFileOfScanFile);
    }

    public void deleteFileOfScanTask(ScanTask scanTask) {
        log.info("[deleteFileOfScanTask] scanTask: {}", scanTask);
        List<ScanFile> scanFiles = this.findByScanTask(scanTask);
        scanFiles.parallelStream().forEach(this::deleteFileOfScanFile);
    }

    public void deleteScanTaskFolder(ScanTask scanTask) {
        log.info("[deleteScanTaskFolder] scanTask: {}", scanTask);
        this.deleteScanTasksFolder(Collections.singletonList(scanTask));
    }

    public void deleteScanTasksFolder(List<ScanTask> scanTasks) {
        log.info("[deleteScanTasksFolder] scanTask, ids: {}", scanTasks.stream().map(ScanTask::getId).collect(Collectors.toList()));
        final String scanTaskFolderRoot = this.getDestinationRoot();
        scanTasks.parallelStream().forEach(scanTask -> FileUtils.deleteQuietly(new File(scanTaskFolderRoot, String.valueOf(scanTask.getId()))));
    }

    public void deleteProjectIdFolder(Project project) {
        log.info("[deleteProjectIdFolder] project id: {}", project.getProjectId());
        File projectFolder = Paths.get(uploadVolumePath, preprocessDataBucketName, project.getProjectId()).toFile();
        FileUtils.deleteQuietly(projectFolder);
    }

    public List<ScanFile> saveFileInfo(
            ScanTask scanTask,
            ImportFileInfoRequest importFileInfoRequest,
            String currentUsername
    ) throws AppException {
        log.info(
                "[saveFileInfo] scanTask, id: {}, importFileInfoRequest, sourceType: {}, currentUsername: {}",
                scanTask.getId(),
                importFileInfoRequest.getSourceType(),
                currentUsername
        );
        FileStorage fileStorage = this.fileStorageService.findByName(importFileInfoRequest.getSourceType())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] sourceStorageName: {}",
                                AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate,
                                importFileInfoRequest.getSourceType()
                        )
                ));
        String fileStorageExtraInfo = this.getFileStorageExtraInfo(importFileInfoRequest).orElse(null);
        log.info(
                "[saveFileInfo] scanTask, id: {}, file size: {}, importFileInfoRequest, sourceType: {}, fileStorageExtraInfo: {}, importFileInfoRequest, sourceCodeFileId: {}, currentUsername: {}",
                scanTask.getId(),
                Optional.ofNullable(importFileInfoRequest.getFiles()).map(List::size).orElse(0),
                importFileInfoRequest.getSourceType(),
                fileStorageExtraInfo,
                importFileInfoRequest.getSourceCodeFileId(),
                currentUsername
        );
        List<ScanFile> scanFiles = this.saveFileInfo(
                scanTask,
                importFileInfoRequest.getFiles(),
                fileStorage,
                fileStorageExtraInfo,
                importFileInfoRequest.getSourceCodeFileId(),
                currentUsername
        );

        MeasureService.updateScanSummary(scanTask, "files", importFileInfoRequest.getNumberOfFiles());
        MeasureService.updateScanSummary(scanTask, "lines", importFileInfoRequest.getTotalLineNum());

        this.scanTaskRepository.save(scanTask);
        return scanFiles;
    }

    private Optional<String> getFileStorageExtraInfo(ImportFileInfoRequest importFileInfoRequest) throws AppException {
        Optional<String> optionalFileStorageExtraInfo = Optional.empty();
        Optional<FileStorage> optionalFileStorage = this.fileStorageService.findByName(importFileInfoRequest.getSourceType());
        FileStorage fileStorage = optionalFileStorage.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] sourceStorageName: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, importFileInfoRequest.getSourceType())));
        Map<String, String> fileStorageExtraInfoMap = new HashMap<>();
        switch (fileStorage.getFileStorageType()) {
            case GITLAB:
            case GITLAB_V3:
            case GITHUB:
            case GERRIT:
                fileStorageExtraInfoMap.put(ScanTaskService.GIT_URL, importFileInfoRequest.getGitUrl());
                break;
            default:
                break;
        }
        if (!fileStorageExtraInfoMap.isEmpty()) {
            try {
                optionalFileStorageExtraInfo = Optional.of(om.writerWithDefaultPrettyPrinter().writeValueAsString(fileStorageExtraInfoMap));
            } catch (JsonProcessingException e) {
                throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_GETFILESTORAGE_FAILED.unifyErrorCode,
                        CommonUtil.formatString("[{}] fileStorageExtraInfoMap: {}", AppException.ErrorCode.E_API_FILE_GETFILESTORAGE_FAILED.messageTemplate, fileStorageExtraInfoMap), e);
            }
        }
        return optionalFileStorageExtraInfo;
    }

    public Page<ScanFile> searchScanFile(ScanTask scanTask, List<String> types, List<ScanFile> scanFileList, Integer depth, Pageable pageable) {
        log.info("[searchScanFile] scanTask id: {},types : {}, scanFileList size: {}, depth: {}", scanTask.getId(), types, scanFileList.size(), depth);
        List<ScanFile.Type> scanFileTypes = types.stream().map(type -> EnumUtils.getEnum(ScanFile.Type.class, type)).collect(Collectors.toList());
        return scanFileRepository.searchScanFile(scanTask, scanFileTypes, scanFileList, depth, pageable);
    }

    public List<ScanFileDto> convertScanFilesToDto(List<ScanFile> scanFiles) {
        log.debug("[convertScanFilesToDto] scan files size: {}", scanFiles.size());
        return scanFiles.stream().map(ScanFileService::convertScanFileToDto).collect(Collectors.toList());
    }

    public static ScanFileDto convertScanFileToDto(ScanFile scanFile) {
        log.debug("[convertScanFileToDto] scan file id: {}", scanFile.getId());
        return ScanFileDto.builder()
                .id(scanFile.getId())
                .projectRelativePath(scanFile.getProjectRelativePath())
                .storePath(scanFile.getStorePath())
                .status(scanFile.getStatus().toString())
                .type(scanFile.getType().toString())
                .parentPath(scanFile.getParentPath())
                .treeLeft(scanFile.getTreeLeft())
                .treeRight(scanFile.getTreeRight())
                .depth(scanFile.getDepth())
                .fileInfo(scanFile.getFileInfo() != null ?
                        ScanFileDto.FileInfo.builder()
                                .id(scanFile.getFileInfo().getId())
                                .relativePath(scanFile.getFileInfo().getRelativePath())
                                .version(scanFile.getFileInfo().getVersion())
                                .checksum(scanFile.getFileInfo().getChecksum())
                                .type(scanFile.getFileInfo().getType().toString())
                                .status(scanFile.getFileInfo().getStatus().toString())
                                .build() : null)
                .build();
    }

    public List<ScanFile> findByScanFileIds(List<UUID> scanFileIds) {
        log.info("[findByScanFileIds] scanFileIds size: {}", scanFileIds.size());
        return this.scanFileRepository.findAllById(scanFileIds);
    }

}
