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
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.FileUtil;
import com.xcal.api.util.VariableUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xcal.api.util.VariableUtil.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FileService {
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    public Integer saveBatchSize;

    @Value("${app.upload.delete-archive-file:true}")
    public void setDeleteArchiveFile(boolean value) {
        deleteArchiveFile = value;
    }

    public static boolean deleteArchiveFile;

    @NonNull LocalFileService localFileService;
    @NonNull GitlabService gitlabService;
    @NonNull GithubService githubService;
    @NonNull GerritService gerritService;
    @NonNull ProjectService projectService;
    @NonNull ObjectMapper om;

    @NonNull FileInfoRepository fileInfoRepository;
    @NonNull FileStorageRepository fileStorageRepository;
    @NonNull ScanTaskRepository scanTaskRepository;

    public void deleteFileInfoBeforeMilliseconds(FileInfo.Type type, int milliseconds) {
        log.debug("[deleteFileInfoBeforeMilliseconds] type: {}, milliseconds: {}", type, milliseconds);
        Date now = new Date();
        // use the negative value of milliseconds
        Date date = DateUtils.addMilliseconds(now, -milliseconds);
        List<FileInfo> fileInfoList = fileInfoRepository.findByTypeAndCreatedOnBefore(type, date);
        log.debug("[deleteFileInfoBeforeMilliseconds] fileInfo list size: {}", fileInfoList.size());
        for (FileInfo fileInfo : fileInfoList) {
            File file = Paths.get(fileInfo.getFileStorage().getFileStorageHost(), fileInfo.getRelativePath()).toFile();
            log.debug("[deleteFileInfoBeforeMilliseconds] begin to delete file {}", file.getPath());
            boolean deleteResult = FileUtils.deleteQuietly(file);
            if (deleteResult) {
                fileInfoRepository.delete(fileInfo);
            } else {
                log.warn("[deleteFileInfoBeforeMilliseconds] delete file failed, file: {}", file.getPath());
            }
        }
    }

    /**
     * Get the file in resource type with corresponding fileInfo id
     *
     * @param id id of the fileInfo
     * @return File in Resource
     * @throws AppException For application exception
     */
    public Resource getFileAsResource(UUID id) throws AppException {
        log.debug("[getFileAsResource] id: {}", id);

        FileInfo fileInfo = this.fileInfoRepository.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.messageTemplate, id)));
        if (!Arrays.asList(FileInfo.Status.ACTIVE).contains(fileInfo.getStatus())) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_NOT_AVAILABLE.unifyErrorCode,
                    CommonUtil.formatString("[{}] id: {}, status: {}", AppException.ErrorCode.E_API_FILE_COMMON_NOT_AVAILABLE.messageTemplate, id, fileInfo.getStatus()));
        }
        return this.getFileAsResource(fileInfo, new HashMap<>(), null, null);
    }


    public File writeStringToTempFile(FileInfo fileInfo, String content) throws AppException {
        log.debug("[writeStringToTempFile] fileInfo, id: {}", fileInfo.getId());
        return CommonUtil.writeStringToTempFile(fileInfo.getId().toString(), fileInfo.getName(), content);
    }


    /**
     * Get the file in path type with fileInfo
     *
     * @param fileInfo   FileInfo
     * @param attributes extra information for the fileInfo
     * @return Path object
     * @throws AppException For application exception
     */
    public Optional<Path> getFileAsPath(FileInfo fileInfo, Map<String, String> attributes) throws AppException {
        log.debug("[getFileAsPath] fileInfo, id: {}", fileInfo.getId());
        String vcsToken = attributes.get("VCS_TOKEN");
        Path path;
        log.debug("[getFileAsPath] fileStorage type is {}", fileInfo.getFileStorage().getFileStorageType());
        switch (fileInfo.getFileStorage().getFileStorageType()) {
            case VOLUME:
                path = this.localFileService.getLocalFilePath(fileInfo);
                break;
            case GITLAB:
            case GITLAB_V3:
                String fileContent = this.gitlabService.getFileContentAsString(fileInfo, vcsToken);
                path = writeStringToTempFile(fileInfo, fileContent).toPath();
                break;
            case GITHUB:
                fileContent = this.githubService.getFileContentAsString(fileInfo, vcsToken);
                path = writeStringToTempFile(fileInfo, fileContent).toPath();
                break;
            case AGENT:
                throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_SOURCE_CODE_NOT_UPLOAD, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_SOURCE_CODE_NOT_UPLOAD.unifyErrorCode,
                        CommonUtil.formatString("[{}] type: {}", AppException.ErrorCode.E_API_FILE_COMMON_SOURCE_CODE_NOT_UPLOAD.messageTemplate, fileInfo.getFileStorage().getFileStorageType()));
            case GERRIT:
                fileContent = this.gerritService.getFileContentAsString(fileInfo, attributes);
                path = writeStringToTempFile(fileInfo, fileContent).toPath();
                break;
            case GIT:
            case SVN:
            default:
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_STORAGE_TYPE.unifyErrorCode,
                        CommonUtil.formatString("[{}] type: {}", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_STORAGE_TYPE.messageTemplate, fileInfo.getFileStorage().getFileStorageType()));
        }

        if (path == null) {
            log.error("[getFileAsPath] failed. fileStorageType: {}, fileInfo, id: {}", fileInfo.getFileStorage().getFileStorageType(), fileInfo.getId());
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMMON_GET_PATH_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] fileStorageType: {}, fileInfo, id: {}", AppException.ErrorCode.E_API_FILE_COMMON_GET_PATH_FAILED.messageTemplate, fileInfo.getFileStorage().getFileStorageType(), fileInfo.getId()));
        }
        Optional<Path> result;
        if (path.toFile().exists()) {
            result = Optional.of(path);
        } else {
            result = Optional.empty();
        }
        return result;
    }


    /**
     * Get the file in resource type with fileInfo
     *
     * @param fileInfo   FileInfo
     * @param attributes Extra attribute for the fileInfo
     * @return File in Resource
     * @throws AppException For application exception
     */
    public Resource getFileAsResource(FileInfo fileInfo, Map<String, String> attributes, Long fromLineNumber, Long toLineNumber) throws AppException {
        log.debug("[getFileAsResource] fileInfo, id: {}", fileInfo.getId());

        Path path = getFileAsPath(fileInfo, attributes).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND,
                AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.unifyErrorCode, CommonUtil.formatString("[{}] fileInfo, id: {}, relativePath: {}", AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.messageTemplate, fileInfo.getId(), fileInfo.getRelativePath())));
        // Either fromLineNumber or toLineNumber is null, return the whole file content in Resource format
        // If the stream contains fewer than fromLineNumber elements then an empty stream will be returned.
        // If the stream contains fewer than toLineNumber elements then only truncated stream will be returned.
        if (fromLineNumber != null && toLineNumber != null) {
            long begin = fromLineNumber - 1;
            long end = toLineNumber - fromLineNumber + 1;
            Charset charset;
            try {
                charset = FileUtil.detectCharset(Files.newInputStream(path));
            } catch (IOException e) {
                log.error("[getFileAsResource] unable to detect charset,use UTF_8. error message: {}", e.getMessage());
                charset = StandardCharsets.UTF_8;
            }
            try (Stream<String> stringStream = Files.lines(path, charset)) {
                List<String> content = stringStream.skip(begin).limit(end).collect(Collectors.toList());
                File tmpFile = File.createTempFile("partial" + "-" + fromLineNumber.toString() + "-" + toLineNumber.toString(), path.getFileName().toString());
                Files.write(tmpFile.toPath(), content);
                path = tmpFile.toPath();
            } catch (IOException e) {
                log.error("[getFileAsResource] process file failed. error message: {}", e.getMessage());
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMMON_OBTAIN_FAILED.unifyErrorCode,
                        CommonUtil.formatString("[{}] fileName: {}", AppException.ErrorCode.E_API_FILE_COMMON_OBTAIN_FAILED.messageTemplate, path.getFileName()),e);
            }
        }

        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.messageTemplate, e);
        }

        if (!resource.exists()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.unifyErrorCode, CommonUtil.formatString("[{}] fileInfo, id: {}", AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.messageTemplate, fileInfo.getId()));
        }

        return resource;
    }


    /**
     * Get the file in resource type with scanFile
     *
     * @param scanFile    scanFile
     * @param attributes  Extra attribute for the file info
     * @param fromLineNum range boundary
     * @param toLineNum   range boundary
     * @return File in Resource
     * @throws AppException For application exception
     */
    public Resource getScanFileAsResource(ScanFile scanFile, Map<String, String> attributes, Long fromLineNum, Long toLineNum) throws AppException {
        log.debug("[getScanFileAsResource] scanFile, id: {}, fromLineNum: {}, toLineNum: {}", scanFile.getId(), fromLineNum, toLineNum);
        return this.getFileAsResource(scanFile.getFileInfo(), attributes, fromLineNum, toLineNum);
    }

    public FileInfo addFileInfo(UUID fileStorageId, FileInfo fileInfo, String currentUsername) throws AppException {
        log.debug("[addFileInfo] fileStorageId: {}, fileInfo, id: {}, currentUsername: {}", fileStorageId, fileInfo.getId(), currentUsername);
        log.trace("[addFileInfo] fileInfo: {}", fileInfo);

        FileStorage fileStorage = this.fileStorageRepository.findById(fileStorageId).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id:{}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, fileStorageId)));

        List<FileInfo> fileInfos = this.fileInfoRepository.findByFileStorageAndRelativePathAndVersionAndStatusIn(fileStorage, fileInfo.getRelativePath(), fileInfo.getVersion(), Arrays.asList(FileInfo.Status.ACTIVE, FileInfo.Status.PENDING));
        if (!fileInfos.isEmpty()) {
            FileInfo existFileInfo = fileInfos.get(0);
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_ALREADY_EXIST, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_ALREADY_EXIST.unifyErrorCode,
                    CommonUtil.formatString("[{}] id:{}, file storage: {}, path: {}, version: {}, status: {}",
                            AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_ALREADY_EXIST.messageTemplate, existFileInfo.getId(), existFileInfo.getFileStorage().getName(), existFileInfo.getRelativePath(), existFileInfo.getVersion(), existFileInfo.getStatus()));
        }

        Date now = new Date();
        fileInfo.setFileStorage(fileStorage);
        fileInfo.setStatus(FileInfo.Status.PENDING);
        fileInfo.setCreatedBy(currentUsername);
        fileInfo.setCreatedOn(now);
        fileInfo.setModifiedBy(currentUsername);
        fileInfo.setModifiedOn(now);

        fileInfo = this.fileInfoRepository.saveAndFlush(fileInfo);
        return fileInfo;
    }

    public Optional<FileInfo> findFileInfoById(UUID id) {
        log.debug("[findFileInfoById] id: {}", id);
        return this.fileInfoRepository.findById(id);
    }

    public FileInfo addFile(MultipartFile file, UUID id, String currentUsername) throws AppException {
        if (file == null) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_MISSING_FILE.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_MISSING_FILE.messageTemplate);
        }
        log.debug("[addFile] file size: {}, id: {}, currentUsername: {}", file.getSize(), id, currentUsername);

        Optional<FileInfo> fileInfoOptional = this.findFileInfoById(id);
        if (!fileInfoOptional.isPresent()) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.messageTemplate, id));
        }
        return this.addFile(file, fileInfoOptional.get(), currentUsername);
    }

    /**
     * Add file to the system
     *
     * @param file file to be add
     * @return fileInfo of the result
     * @throws AppException For file is null and IOException when create file.
     */
    public FileInfo addFile(MultipartFile file, FileInfo fileInfo, String currentUsername) throws AppException {
        if (file == null) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_MISSING_FILE.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_MISSING_FILE.messageTemplate);
        }
        log.debug("[addFile] file size: {}, fileInfo, id:{}, currentUsername: {}", file.getSize(), fileInfo.getId(), currentUsername);
        log.trace("[addFile] fileInfo: {}", fileInfo);
        switch (fileInfo.getFileStorage().getFileStorageType()) {
            case VOLUME:
                this.localFileService.storeFile(file, fileInfo);
                break;
            case GIT:
            case SVN:
            default:
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_STORAGE_TYPE.unifyErrorCode,
                        CommonUtil.formatString("[{}] type: {}", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_STORAGE_TYPE.messageTemplate, fileInfo.getFileStorage().getFileStorageType()));
        }

        Date now = new Date();
        fileInfo.setStatus(FileInfo.Status.ACTIVE);
        fileInfo.setModifiedBy(currentUsername);
        fileInfo.setModifiedOn(now);
        fileInfo = this.fileInfoRepository.saveAndFlush(fileInfo);

        return fileInfo;
    }

    /**
     * Delete file
     *
     * @param id file to be delete
     * @return fileInfo of the result
     * @throws AppException
     */
    public FileInfo deleteFile(UUID id, String currentUsername) throws AppException {
        log.debug("[deleteFile] id: {}, currentUsername: {}", id, currentUsername);

        Optional<FileInfo> fileInfoOptional = this.fileInfoRepository.findById(id);
        FileInfo fileInfo = fileInfoOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.messageTemplate, id)));
        return this.deleteFile(fileInfo, currentUsername);
    }

    /**
     * Delete file
     *
     * @param fileInfo file to be delete
     * @return fileInfo of the result
     * @throws AppException
     */
    public FileInfo deleteFile(FileInfo fileInfo, String currentUsername) throws AppException {
        log.info("[deleteFile] fileInfo, id: {}, currentUsername: {}", fileInfo.getId(), currentUsername);
        log.trace("[deleteFile] fileInfo: {}", fileInfo);
        switch (fileInfo.getFileStorage().getFileStorageType()) {
            case VOLUME:
                this.localFileService.deleteFile(fileInfo);
                break;
            case GIT:
            case SVN:
            default:
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_STORAGE_TYPE.unifyErrorCode,
                        CommonUtil.formatString("[{}] type: {}", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_STORAGE_TYPE.messageTemplate, fileInfo.getFileStorage().getFileStorageType()));
        }

        Date now = new Date();
        fileInfo.setStatus(FileInfo.Status.DELETED);
        fileInfo.setModifiedBy(currentUsername);
        fileInfo.setModifiedOn(now);
        fileInfo = this.fileInfoRepository.saveAndFlush(fileInfo);

        return fileInfo;
    }

    public void deleteLocalFile(File file) {
        log.info("[deleteLocalFile] file path: {}", file.getAbsolutePath());
        this.localFileService.deleteFile(file);
    }

    /**
     * Delete file from the system
     *
     * @param id id of the FileInfo to be delete
     * @throws AppException For file is null and IOException when create file.
     */
    public void deleteFileInfo(UUID id, String currentUsername) throws AppException {
        log.info("[deleteFileInfo] id: {}, currentUsername: {}", id, currentUsername);

        FileInfo fileInfo = this.findFileInfoById(id).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.messageTemplate, id)));
        this.fileInfoRepository.delete(fileInfo);
    }

    public Map<String, FileInfo> retrieveFileInfoMap(FileStorage fileStorage, String relativePath) {
        log.info("[retrieveFileInfoMap] fileStorage: {}, relativePath: {}", fileStorage, relativePath);
        relativePath = StringUtils.removeStartIgnoreCase(StringUtils.removeStart(relativePath, "/"), "/");
        List<FileInfo> fileInfos = this.fileInfoRepository.findByFileStorageAndRelativePathStartsWithAndStatusIn(fileStorage, relativePath, Collections.singletonList(FileInfo.Status.ACTIVE));
        return fileInfos.stream().collect(Collectors.toMap(fi -> fi.getRelativePath() + "#" + fi.getVersion(), fi -> fi));
    }

    public Map<String, FileInfo> retrieveFileInfoMap(FileStorage fileStorage) {
        log.info("[retrieveFileInfoMap] fileStorage: {}", fileStorage);
        List<FileInfo> fileInfos = this.fileInfoRepository.findByFileStorageAndStatusIn(fileStorage, Collections.singletonList(FileInfo.Status.ACTIVE));
        return fileInfos.stream().collect(Collectors.toMap(fi -> fi.getRelativePath() + "#" + fi.getVersion(), fi -> fi));
    }

    public Map<String, FileInfo> retrieveFileInfoMapByFileStorageAndVersion(FileStorage fileStorage, String version) {
        log.info("[retrieveFileInfoMapByFileStorageAndVersion] fileStorage: {}, version: {}", fileStorage, version);
        List<FileInfo> fileInfos = this.fileInfoRepository.findByFileStorageAndVersionAndStatusIn(fileStorage, version, Collections.singletonList(FileInfo.Status.ACTIVE));
        return fileInfos.stream().collect(Collectors.toMap(fi -> fi.getRelativePath() + "#" + fi.getVersion(), fi -> fi));
    }

    public File compressFile(String rootPath, String relativePath, String archive, String format) throws AppException {
        log.info("[compressFile] rootPath: {}, relativePath: {}, archive: {}, format: {}", rootPath, relativePath, archive, format);
        File sourceFile = Paths.get(rootPath, relativePath).toFile();
        if (!sourceFile.exists()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMPRESSFILE_FILE_OR_DIRECTORY_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] filePath: {}", AppException.ErrorCode.E_API_FILE_COMPRESSFILE_FILE_OR_DIRECTORY_NOT_FOUND.messageTemplate, sourceFile.getPath()));
        }

        //To make sure extract the compress file can get the whole path of the source code.
        File prepareSourceFile = Paths.get(rootPath, archive, relativePath).toFile();
        try {
            log.info("[compressFile] copy {} to {}", sourceFile.getPath(), prepareSourceFile.getPath());
            FileUtils.copyDirectory(sourceFile, prepareSourceFile);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMPRESSFILE_COPYCODE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] sourceFile: {}, prepareSourceFile: {}", AppException.ErrorCode.E_API_FILE_COMPRESSFILE_COPYCODE_FAILED.messageTemplate, sourceFile.getName(), prepareSourceFile.getName()),e);
        }

        File finalSourceFile = Paths.get(rootPath, archive).toFile();
        File destinationFile = Paths.get(rootPath).toFile();

        Archiver archiver;
        if (StringUtils.isNotBlank(format)) {
            archiver = ArchiverFactory.createArchiver(format);
        } else {
            archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP);
        }

        File archiveFile;
        try {
            archiveFile = archiver.create(archive, destinationFile, finalSourceFile);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMPRESSFILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] prepareSourceFile: {}, destinationFile: {}, archive: {}", AppException.ErrorCode.E_API_FILE_COMPRESSFILE_FAILED.messageTemplate, prepareSourceFile.getName(), destinationFile.getName(), archive),e);
        }

        if (!archiveFile.exists()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMPRESSFILE_FILE_NOT_GENERATED.unifyErrorCode,
                    CommonUtil.formatString("[{}] archiveFile: {}", AppException.ErrorCode.E_API_FILE_COMPRESSFILE_FILE_NOT_GENERATED.messageTemplate, archiveFile.getPath()));
        }

        log.debug("[compressFile] begin to delete file {}", finalSourceFile.getPath());
        boolean isFileDeleted = FileUtils.deleteQuietly(finalSourceFile);
        log.debug("[compressFile] is file {} deleted: {}", finalSourceFile.getPath(), isFileDeleted);

        return archiveFile.getAbsoluteFile();
    }

    public File decompressFile(String rootSrcPath, String destinationPath, String relativeCompressFilePath) throws AppException {
        return decompress(rootSrcPath, destinationPath, relativeCompressFilePath);
    }

    public static File decompress(String rootSrcPath, String destinationPath, String relativeCompressFilePath) throws AppException {
        log.info("[decompressFile] rootSrcPath: {}, destinationPath: {}, relativeCompressFilePath: {}", rootSrcPath, destinationPath, relativeCompressFilePath);
        File decompressFolder = Paths.get(rootSrcPath, destinationPath).toFile();
        if (decompressFolder.exists()) {
            FileUtils.deleteQuietly(decompressFolder);
            if (decompressFolder.exists()) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMPRESSFILE_DELETE_EXISTING_FAILED.unifyErrorCode,
                        CommonUtil.formatString("[{}] folderName: {}", AppException.ErrorCode.E_API_FILE_COMPRESSFILE_DELETE_EXISTING_FAILED.messageTemplate, decompressFolder.getName()));
            }
        }
        try {
            FileUtils.forceMkdir(decompressFolder);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMPRESSFILE_DECOMPRESS_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] folderName: {}", AppException.ErrorCode.E_API_FILE_COMPRESSFILE_DECOMPRESS_FAILED.messageTemplate, destinationPath), e);
        }

        File compressFile;
        if (StringUtils.isNotBlank(relativeCompressFilePath)) {
            compressFile = Paths.get(rootSrcPath, relativeCompressFilePath).toFile();
        } else {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMPRESSFILE_DECOMPRESS_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] archive file not found", AppException.ErrorCode.E_API_FILE_COMPRESSFILE_DECOMPRESS_FAILED.messageTemplate));
        }

        extractArchive(compressFile, decompressFolder);
        if (deleteArchiveFile) {
            FileUtils.deleteQuietly(compressFile);
        }
        return decompressFolder;
    }

    public static void extractArchive(File archive, File destination) throws AppException {
        log.info("[extractArchive] archive: {}, destination: {}", archive, destination);
        Archiver archiver = ArchiverFactory.createArchiver(archive);
        try {
            archiver.extract(archive, destination);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMPRESSFILE_DECOMPRESS_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] fileName: {}", AppException.ErrorCode.E_API_FILE_COMPRESSFILE_DECOMPRESS_FAILED.messageTemplate, archive.getName()), e);
        }
    }

    public List<FileInfo> findByScanTask(ScanTask scanTask) {
        log.info("[findByScanTask] scanTask, id: {}", scanTask.getId());
        return this.fileInfoRepository.findByScanFilesScanTask(scanTask);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileAttributes {
        Integer numberOfLines;
        String checksum;
        Long fileSize;
    }

    public FileAttributes getFileAttributes(Path path) throws IOException {
        log.info("[getFileAttributes] path: {}", path);
        return FileAttributes.builder().numberOfLines(FileUtils.readLines(path.toFile(), Charset.defaultCharset()).size())
                .checksum(String.valueOf(FileUtils.checksumCRC32(path.toFile())))
                .fileSize(FileUtils.sizeOf(path.toFile())).build();
    }

    public Path checkAndSaveFile(Path filePath, String expectedChecksum, String checksum, FileStorage fileStorage) throws AppException {
        log.info("[checkAndSaveFile] filePath: {}, expectedChecksum: {}, checksum: {}, fileStorage, name: {}", filePath, expectedChecksum, checksum, fileStorage.getName());

        if (StringUtils.isNotBlank(expectedChecksum)) {
            if (!NumberUtils.isParsable(expectedChecksum)) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_CHECKSUM_NOT_PARSABLE.unifyErrorCode,
                        CommonUtil.formatString("[{}], checksum: {}", AppException.ErrorCode.E_API_FILE_CHECKSUM_NOT_PARSABLE.messageTemplate, checksum));
            }

            if (!checkIntegrity(checksum, expectedChecksum)) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_INCONSISTENT, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                        CommonUtil.formatString("[{}] fileName: {}, expectedChecksum: {}, checksum: {}", AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate, filePath.getFileName(), expectedChecksum, checksum));
            }
        }

        return this.saveFileToDisk(filePath, checksum, fileStorage);
    }

    public long getCrc32Checksum(File file) throws AppException {
        long crc32;
        try {
            crc32 = FileUtils.checksumCRC32(file);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] filePath: {}", AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate, file.getPath()),e);
        }
        return crc32;
    }

    public String getSha256(File file) throws AppException {
        String result;
        try {
            result = DigestUtils.sha256Hex(FileUtils.openInputStream(file));
        } catch (IOException e) {
            log.error("[getSha256] error message: {}: {}", e.getClass(), e.getMessage());
            log.error("[getSha256] error stack trace info: {}", Arrays.toString(e.getStackTrace()));
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_CALCULATE_HASH_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] file path: {}", AppException.ErrorCode.E_API_FILE_CALCULATE_HASH_FAILED.messageTemplate, file.getPath()),e);
        }
        return result;
    }

    public FileInfo addFile(MultipartFile file, String expectedChecksum, FileInfo.Type type, String currentUsername) throws AppException {
        log.info("[addFile] file size: {}, expectedChecksum: {}, type: {}, currentUsername: {}", file.getSize(), expectedChecksum, type, currentUsername);

        File tempFile = getTempFile(file);
        String fileStorageName = VOLUME_UPLOAD;
        if (type == FileInfo.Type.LIB) {
            fileStorageName = VOLUME_LIB;
        }
        return addFile(tempFile, file.getOriginalFilename(), expectedChecksum, fileStorageName, true, type, currentUsername);
    }

    public FileInfo writeObjectToFile(Object object, String fileNamePrefix, String fileNameSuffix, String currentUsername) throws AppException {
        log.info("[writeObjectToFile] class of object: {}, fileNamePrefix: {}, fileNameSuffix: {}, currentUsername: {}", object.getClass().getName(), fileNamePrefix, fileNameSuffix, currentUsername);

        String importIssueResponseStrContent = CommonUtil.writeObjectToJsonStringSilently(om, object);
        File resultFile = CommonUtil.writeStringToTempFile(fileNamePrefix, fileNameSuffix, importIssueResponseStrContent);
        FileInfo fileInfo = this.addFile(resultFile, resultFile.getName(), null, VariableUtil.VOLUME_DIAGNOSTIC, true, FileInfo.Type.SOURCE, currentUsername);
        log.trace("[writeObjectToFile] fileInfo: {}", fileInfo);
        return fileInfo;
    }

    public FileInfo addFile(File file, String filename, String expectedChecksum, String fileStorageName, boolean needSaveToDisk, FileInfo.Type type, String currentUsername) throws AppException {
        log.info("[addFile] filePath: {}, expectedChecksum: {}, fileStorageName: {}, needSaveToDisk: {}, type: {}, currentUsername: {}", file.getPath(), expectedChecksum, fileStorageName, needSaveToDisk, type, currentUsername);

        if (StringUtils.isBlank(fileStorageName)) {
            fileStorageName = VOLUME_UPLOAD;
        }

        String finalFileStorageName = fileStorageName;
        FileStorage fileStorage = this.fileStorageRepository.findByName(fileStorageName).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] fileStorageName: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, finalFileStorageName)));

        String version;
        try {
            version = String.valueOf(Files.getLastModifiedTime(file.toPath()).toMillis());
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMMON_OBTAIN_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] filename: {}", AppException.ErrorCode.E_API_FILE_COMMON_OBTAIN_FAILED.messageTemplate, file.getName()),e);
        }

        long checksum = getCrc32Checksum(file);

        Date now = new Date();
        FileInfo fileInfo = FileInfo.builder()
                .fileStorage(fileStorage)
                .name(filename)
                .version(type == FileInfo.Type.LIB ? getSha256(file) : version)
                .checksum(Long.toString(checksum))
                .fileSize(FileUtils.sizeOf(file))
                .noOfLines(0)
                .fileStorageExtraInfo(null)
                .type(type == FileInfo.Type.TEMP ? FileInfo.Type.TEMP : FileInfo.Type.SOURCE)
                .status(FileInfo.Status.ACTIVE)
                .createdBy(currentUsername).createdOn(now)
                .modifiedBy(currentUsername).modifiedOn(now)
                .build();

        String relativePath;
        if (needSaveToDisk) {   // for upload file usage
            Path savePath = this.checkAndSaveFile(file.toPath(), expectedChecksum, Long.toString(checksum), fileStorage);
            relativePath = StringUtils.substringAfter(savePath.toString(), fileStorage.getFileStorageHost());
            relativePath = StringUtils.substringAfter(relativePath, "/");
        } else {
            relativePath = StringUtils.substringAfter(file.getAbsolutePath(), fileStorage.getFileStorageHost());
        }

        fileInfo.setRelativePath(relativePath);

        return this.fileInfoRepository.saveAndFlush(fileInfo);
    }


    public Path saveFileToDisk(Path filePath, String checksum, FileStorage fileStorage) throws AppException {
        log.info("[saveFileToDisk] fileName: {}, checksum: {}, fileStorage, name: {}", filePath.getFileName(), checksum, fileStorage.getName());

        Path resultPath;
        String rootSrcPath = fileStorage.getFileStorageHost();
        try {
            resultPath = Paths.get(rootSrcPath, checksum + "_" + filePath.getFileName().toString());
            Files.deleteIfExists(resultPath);
            FileUtils.moveFile(filePath.toFile(), resultPath.toFile());
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_UPLOAD_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}]", AppException.ErrorCode.E_API_FILE_UPLOAD_FILE_FAILED.messageTemplate), e);
        }
        return resultPath;
    }

    //For now, only support md5 algorithm
    public String getMd5Checksum(MultipartFile file) throws AppException {
        String checksum;
        try {
            checksum = DigestUtils.md5Hex(file.getInputStream());
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] fileName: {}", AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate, file.getOriginalFilename()), e);
        }
        return checksum;
    }

    public boolean checkIntegrity(String calculatedChecksum, String expectedChecksum) {
        boolean isIntegrity = false;
        if (StringUtils.isNotBlank(expectedChecksum)) {
            isIntegrity = expectedChecksum.equals(calculatedChecksum);
        }
        return isIntegrity;
    }

    public boolean checkIntegrityWithCrc32(File file, String expectedChecksum) throws AppException {
        log.info("[checkIntegrityWithCrc32] filePath: {}, expectedChecksum: {}", file.getPath(), expectedChecksum);
        long crc32;
        try {
            crc32 = FileUtils.checksumCRC32(file);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] file path: {}", AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate, file.getPath()),e);
        }

        return checkIntegrity(Long.toString(crc32), expectedChecksum);
    }

    public boolean checkIntegrityWithCrc32(MultipartFile file, String expectedChecksum) throws AppException {
        log.info("[checkIntegrityWithCrc32] filename: {}, expectedChecksum: {}", file.getName(), expectedChecksum);
        File tempFile = getTempFile(file);
        long crc32;
        try {
            crc32 = FileUtils.checksumCRC32(tempFile);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] tempFile path: {}", AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate, tempFile.getPath()),e);
        }

        log.debug("[checkIntegrityWithCrc32] begin to delete file {}", tempFile.getPath());
        boolean isFileDeleted = FileUtils.deleteQuietly(tempFile);
        log.debug("[checkIntegrityWithCrc32] is file {} deleted: {}", tempFile.getPath(), isFileDeleted);

        return checkIntegrity(Long.toString(crc32), expectedChecksum);
    }

    public boolean checkIntegrityWithMd5(MultipartFile file, String expectedChecksum) throws AppException {
        String calculatedChecksum = getMd5Checksum(file);
        return checkIntegrity(calculatedChecksum, expectedChecksum);
    }

    public static File getTempFile(MultipartFile file) throws AppException {
        Path tempPath = null;
        try {
            tempPath = Files.createTempFile("api-upload", file.getOriginalFilename());
            file.transferTo(tempPath);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] tempFile path: {}", AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.messageTemplate, tempPath),e);
        }

        if (!tempPath.toFile().exists()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] tempFile path: {}", AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.messageTemplate, tempPath));
        }

        return tempPath.toFile();
    }

    public File getLocalTempFile(MultipartFile file) throws AppException {
        log.info("[getLocalTempFile] fileName: {}", file.getOriginalFilename());

        FileStorage fileStorage = this.fileStorageRepository.findByName(VOLUME_TMP)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_BAD_REQUEST,
                        AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] name: {}",
                                AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate,
                                VOLUME_TMP
                        )
                ));

        String tempName = String.format("api-upload-%d-%s", System.currentTimeMillis(), file.getOriginalFilename());

        try {
            Path tempPath = Paths.get(fileStorage.getFileStorageHost(), tempName);
            file.transferTo(tempPath);
            return tempPath.toFile();
        } catch (Exception e) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_INTERNAL_ERROR,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] tempFile path: {}",
                            AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.messageTemplate,
                            tempName
                    ),
                    e
            );
        }
    }

    public void decompressFile(UUID fileInfoId) throws AppException {
        log.info("[decompressFile] fileInfoId: {}", fileInfoId);
        FileInfo fileInfo = this.fileInfoRepository.findById(fileInfoId).orElseThrow(
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] fileInfoId: {}", AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.messageTemplate, fileInfoId)));
        this.decompressFile(fileInfo.getFileStorage().getFileStorageHost(), fileInfoId.toString(), fileInfo.getRelativePath());
    }

    public Page<FileInfo> findFileInfo(String name, FileInfo.Type type, String version, FileInfo.Status status, Pageable pageable) {
        log.info("[findFileInfo] name: {}, type: {}, version: {}, status: {}", name, type, version, status);
        return this.fileInfoRepository.findByNameAndTypeAndVersionAndStatusIn(name, type, version, Arrays.asList(status), pageable);
    }

    /**
     * Copy and rename the file from upload/$scanTaskId/$fileName folder to upload/xxxx_api-uploadxxxx$fileName
     * And store the record to database as fileinfo with $type
     *
     * @param scanTaskId The scan id from client
     * @param fileName   Name of the file
     * @param checksum   Checksum of the file for verifying file integrity
     * @param type       The type of file info
     *                   (eg.LIB,SOURCE, TEMP)
     * @param userName
     * @return
     * @throws AppException
     */
    public FileInfo addFileFromFileSystem(String scanTaskId, String fileName, String checksum, FileInfo.Type type, String userName) throws AppException {
        log.info("[addFileFromFileSyetem] scanTaskId: {}, fileName: {}, checksum: {}, type: {} user: {}", scanTaskId, fileName, checksum, type, userName);
        File file = null;
        Path tempPath = null;
        MultipartFile multipartFile = null;

        file = new File("/share/upload/" + scanTaskId.toLowerCase() + "/" + fileName);
        if (!file.exists()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] tempFile path: {}", AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.messageTemplate, file.toPath()));
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile(file.getName(), file.getName(), "File", fileInputStream);

            tempPath = Files.createTempFile("api-upload", file.getName());
            multipartFile.transferTo(tempPath);

        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] tempFile path: {}", AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.messageTemplate, tempPath),e);
        }

        String fileStorageName = VOLUME_UPLOAD;
        if (type == FileInfo.Type.LIB) {
            fileStorageName = VOLUME_LIB;
        }
        return addFile(tempPath.toFile(), file.getName(), checksum, fileStorageName, true, FileInfo.Type.TEMP, userName);
    }

}
