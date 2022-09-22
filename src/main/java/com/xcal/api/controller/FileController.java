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
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.FileCacheDto;
import com.xcal.api.model.dto.FileInfoDto;
import com.xcal.api.model.dto.FileStorageDto;
import com.xcal.api.model.dto.ScanFileDto;
import com.xcal.api.model.payload.*;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.*;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.Dto;
import com.xcal.api.util.MessagesTemplate;
import com.xcal.api.util.TracerUtil;
import io.opentracing.Tracer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/file_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "File Service")
public class FileController {

    @NonNull FileService fileService;
    @NonNull ScanFileService scanFileService;
    @NonNull FileStorageService fileStorageService;
    @NonNull ScanTaskService scanTaskService;
    @NonNull MeasureService measureService;
    @NonNull ProjectService projectService;
    @NonNull FileCacheService fileCacheService;

    @NonNull Tracer tracer;

    @NonNull ObjectMapper om;
    @Value("${app.scan.volume.path}")
    String scanVolumePath;

    @Value("${scan-task-log-file-name}")
    String scanTaskLogFileName;

    /**
     * @param id       file storage id
     * @param fileInfo file info to be add
     * @return fileInfo
     * @throws AppException For application exception
     */
    @PostMapping("/file_storage/{id}/file_info")
    @ApiOperation(value = "Add file info",
            nickname = "addFileInfo",
            notes = "Add file info and link with corresponding file storage",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(FileInfoDto.class)
    public ResponseEntity<FileInfo> addFileInfo(@PathVariable UUID id, @Dto(FileInfoDto.class) @RequestBody FileInfo fileInfo, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[addFile] fileStorageId:{}, fileInfo: {}, principal username: {}", id, fileInfo, userPrincipal.getUsername());
        FileInfo result = this.fileService.addFileInfo(id, fileInfo, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.FILE_ID, result.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * @param id file info id
     * @return fileInfo
     * @throws AppException For application exception
     */
    @GetMapping("/file_info/{id}")
    @ApiOperation(value = "Get file info",
            nickname = "getFileInfo",
            notes = "Get file info with corresponding id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(FileInfoDto.class)
    public ResponseEntity<FileInfo> getFileInfo(@PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getFileInfo] id: {}, principal username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.FILE_ID, id);

        FileInfo fileInfo = this.fileService.findFileInfoById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.messageTemplate, id)));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileInfo);
    }

    /**
     * @param id file info
     * @return fileInfo
     * @throws AppException For application exception
     */
    @DeleteMapping("/file_info/{id}")
    @ApiOperation(value = "Delete file info",
            nickname = "deleteFileInfo",
            notes = "Delete file info with corresponding id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(FileInfoDto.class)
    public ResponseEntity<Void> deleteFileInfo(@PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[deleteFileInfo] id: {}, principal username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.FILE_ID, id);
        this.fileService.deleteFileInfo(id, userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * @param file          file need to upload
     * @param id            id of the file info
     * @param userPrincipal user principal
     * @return fileInfo
     * @throws AppException For application exception
     */
    @PostMapping("/file_info/{id}/file")
    @ApiOperation(value = "Add new file",
            nickname = "addFile",
            notes = "Add file with corresponding fileInfo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Dto(FileInfoDto.class)
    public ResponseEntity<FileInfo> addFile(@RequestParam("upload_file") MultipartFile file, @RequestParam(value = "file_checksum", required = false) String checksum,
                                            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[addFile] file size: {}, checksum: {}, id: {}, principal username: {}", file != null ? file.getSize() : 0, checksum, id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.FILE_ID, id);
        ResponseEntity<FileInfo> response;
        if (StringUtils.isNotBlank(checksum) && file != null) {
            boolean isFileOk = fileService.checkIntegrityWithCrc32(file, checksum);
            if (!isFileOk) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_INCONSISTENT, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                        CommonUtil.formatString("[{}] expectedChecksum: {}", AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate, checksum));
            }

            FileInfo result = this.fileService.addFile(file, id, userPrincipal.getUsername());
            response = ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        } else {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.messageTemplate, id));
        }

        return response;
    }


    /**
     * @param id            file info id
     * @param request       request
     * @param userPrincipal user principal
     * @return Resource
     * @throws AppException For application exception
     */
    @GetMapping("/file_info/{id}/file")
    @ApiOperation(value = "Get file",
            nickname = "getFile",
            notes = "Retrieve the file with corresponding id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource> getFile(
            @ApiParam(value = "id of the file", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, HttpServletRequest request, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getFile] id: {}, principal username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.FILE_ID, id);
        Resource resource = fileService.getFileAsResource(id);

        return this.generateResourceResponse(request, resource);
    }

    /**
     * @param id            file info id
     * @param userPrincipal user principal
     * @return fileInfo
     * @throws AppException For application exception
     */
    @DeleteMapping("/file_info/{id}/file")
    @ApiOperation(value = "Delete file by id",
            nickname = "deleteFile",
            notes = "Delete file with corresponding id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(FileInfoDto.class)
    public ResponseEntity<FileInfo> deleteFile(
            @ApiParam(value = "id of the file", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[deleteFile] id: {}, principal username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.FILE_ID, id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.fileService.deleteFile(id, userPrincipal.getUsername()));
    }

    /**
     * @param id            scan file id
     * @param userPrincipal user principal
     * @return file content in Resource type
     * @throws AppException For application exception
     */
    @GetMapping("/scan_file/{id}/file")
    @ApiOperation(value = "Get file by scan file id",
            nickname = "getFileByScanFileId",
            notes = "Get file by scan file id")
    public ResponseEntity<Resource> getFileByScanFileId(
            @ApiParam(value = "id of the scan file", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, HttpServletRequest request, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getFileByScanFileId] id: {}, principal username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.FILE_ID, id);

        ScanFile scanFile = this.scanFileService.findScanFileById(id).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.messageTemplate, id)));
        ScanTask scanTask = scanFile.getScanTask();
        TracerUtil.setScanTaskTag(tracer, scanTask);
        ProjectConfig projectConfig = projectService.getLatestActiveProjectConfigByProject(scanTask.getProject()).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate, scanTask.getProject().getId())));
        Map<String, String> attributes = ProjectService.prepareAttributeMapFromProjectConfig(projectConfig);
        Resource resource = fileService.getScanFileAsResource(scanFile, attributes, null, null);
        return this.generateResourceResponse(request, resource);
    }

    @ApiOperation(
            value = "File file by scan file path",
            nickname = "findFileByScanFilePath",
            notes = "File file by scan file path"
    )
    @PostMapping("/scan_file/file")
    public ResponseEntity<Resource> findFileByScanFilePath(
            HttpServletRequest request,
            @RequestBody FindScanFileRequest findScanFileRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[findFileByScanFilePath] findScanFileRequest: {}, principal username: {}", findScanFileRequest, userPrincipal.getUsername());

        // check input parameters when both fromLineNo and toLineNo are not null
        if ((findScanFileRequest.getFromLineNo() != null) && (findScanFileRequest.getToLineNo() != null)) {
            if ((findScanFileRequest.getFromLineNo() <= 0) || (findScanFileRequest.getToLineNo() <= 0)) {
                throw new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_BAD_REQUEST,
                        HttpURLConnection.HTTP_BAD_REQUEST,
                        AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] line number must be positive number. fromLineNo: {}, toLineNo: {}",
                                AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.messageTemplate,
                                findScanFileRequest.getFromLineNo(),
                                findScanFileRequest.getToLineNo()
                        )
                );
            }
            if (findScanFileRequest.getFromLineNo() > findScanFileRequest.getToLineNo()) {
                throw new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_BAD_REQUEST,
                        HttpURLConnection.HTTP_BAD_REQUEST,
                        AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] fromLineNo must be less than or equal to toLineNo. fromLineNo: {}, toLineNo: {}",
                                AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.messageTemplate,
                                findScanFileRequest.getFromLineNo(),
                                findScanFileRequest.getToLineNo()
                        )
                );
            }
        } else {
            findScanFileRequest.setFromLineNo(null);
            findScanFileRequest.setToLineNo(null);
        }

        ScanTask srcScanTask = this.scanTaskService.findById(findScanFileRequest.getScanTaskId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] srcScanTaskId: {}",
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                findScanFileRequest.getScanTaskId()
                        )
                ));

        ScanTask scanTask;
        // when dsrType is "F", srcScanTask is from the fixedScanTaskId, so find previous scan task
        if (StringUtils.compare(findScanFileRequest.getDsrType(), "F") == 0) {
            scanTask = this.scanTaskService.getPreviousCompletedScanTaskByScanTask(srcScanTask)
                    .orElseThrow(() -> new AppException(
                            AppException.LEVEL_WARN,
                            AppException.ERROR_CODE_DATA_NOT_FOUND,
                            HttpURLConnection.HTTP_NOT_FOUND,
                            AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                            CommonUtil.formatString(
                                    "[{}] scanTaskId: {}",
                                    AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                    srcScanTask.getId()
                            )
                    ));
        } else {
            // TODO: will change to use latest scan task when file service refactor is done
            // otherwise, when dsrType is "E" or "N", srcScanTask is from the occurScanTaskId
            scanTask = srcScanTask;
        }

        ScanFile scanFile = this.scanFileService.findScanFileByScanTaskAndRelativePath(scanTask.getId(), findScanFileRequest.getRelativePath())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}, relativePath: {}",
                                AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate,
                                findScanFileRequest.getScanTaskId(),
                                findScanFileRequest.getRelativePath()
                        )
                ));

        if ((findScanFileRequest.getFromLineNo() == null) && (findScanFileRequest.getToLineNo() == null)
                && (findScanFileRequest.getLinesLimit() != null) && (scanFile.getFileInfo().getNoOfLines() > findScanFileRequest.getLinesLimit())) {
            return ResponseEntity.noContent().build();
        }

        ProjectConfig projectConfig = scanFile.getScanTask().getProjectConfig();
        if(projectConfig == null) {
            throw new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate,
                                findScanFileRequest.getScanTaskId())
                );
        }

        Map<String, String> attributes = ProjectService.prepareAttributeMapFromProjectConfig(projectConfig);
        Resource resource = this.fileService.getScanFileAsResource(scanFile, attributes, findScanFileRequest.getFromLineNo(), findScanFileRequest.getToLineNo());
        return this.generateResourceResponse(request, resource);
    }

    /**
     * @param id            scan file id
     * @param fromLineNum   range boundary
     * @param toLineNum     range boundary
     * @param userPrincipal user principal
     * @return partial file content in Resource type, range [fromLineNum, toLineNum]
     * @throws AppException For application exception
     */
    @GetMapping("/scan_file/{id}/file/from/{fromLineNum}/to/{toLineNum}")
    @ApiOperation(value = "Get partial file content by scan file id",
            nickname = "getPartialFileByScanFileId",
            notes = "Get partial file by scan file id")
    public ResponseEntity<Resource> getPartialFileByScanFileId(
            @ApiParam(value = "id of the scan file", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid") @PathVariable UUID id,
            @ApiParam(value = "line number", example = "5", format = "long") @PathVariable Long fromLineNum,
            @ApiParam(value = "line number", example = "9", format = "long") @PathVariable Long toLineNum,
            HttpServletRequest request, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getPartialFileByScanFileId] id: {}, fromLineNum: {}, toLineNum: {}, principal username: {}", id, fromLineNum, toLineNum, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.FILE_ID, id);
        if (fromLineNum < 0 || toLineNum < 0) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.unifyErrorCode,
                    CommonUtil.formatString("[{}] line number should not be negative number. fromLineNum: {}, toLineNum: {}", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.messageTemplate, fromLineNum, toLineNum));
        }

        if (fromLineNum > toLineNum) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.unifyErrorCode,
                    CommonUtil.formatString("[{}] fromLineNum must be less than or equal to toLineNum. fromLineNum: {}, toLineNum: {}", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.messageTemplate, fromLineNum, toLineNum));
        }

        ScanFile scanFile = this.scanFileService.findScanFileById(id).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND.messageTemplate, id)));
        ScanTask scanTask = scanFile.getScanTask();

        TracerUtil.setScanTaskTag(tracer, scanTask);

        ProjectConfig projectConfig = projectService.getLatestActiveProjectConfigByProject(scanTask.getProject()).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate, scanTask.getProject().getId())));
        Map<String, String> attributes = ProjectService.prepareAttributeMapFromProjectConfig(projectConfig);
        Resource resource = fileService.getScanFileAsResource(scanFile, attributes, fromLineNum, toLineNum);
        return this.generateResourceResponse(request, resource);
    }


    private ResponseEntity<Resource> generateResourceResponse(HttpServletRequest request, Resource resource) {
        MediaType mediaType;
        try {
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (StringUtils.isBlank(contentType)) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            } else {
                mediaType = MediaType.parseMediaType(contentType);
            }
        } catch (IOException ex) {
            log.warn("[generateResourceResponse] Could not determine file type. filename: {}", resource.getFilename());
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * @param fileStorage   fileStorage to be add
     * @param userPrincipal user principal
     * @return FileStorage data transfer object
     * @throws AppException For application exception
     */
    @PostMapping("/file_storage")
    @ApiOperation(value = "Add fileStorage",
            nickname = "addFileStorage",
            notes = "Add a new fileStorage",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(FileStorageDto.class)
    public ResponseEntity<FileStorage> addFileStorage(@Dto(FileStorageDto.class) @RequestBody FileStorage fileStorage, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[addFileStorage] fileStorage: {}, principal username: {}", fileStorage, userPrincipal.getUsername());
        FileStorage result = this.fileStorageService.add(fileStorage, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * @param pageable      for paging propose
     * @param userPrincipal user principal
     * @return FileStorage data transfer object
     */
    @GetMapping("/file_storages")
    @ApiOperation(value = "List FileStorage",
            nickname = "listFileStorage",
            notes = "List fileStorage with pageable",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(FileStorageDto.class)
    public ResponseEntity<Page<FileStorage>> listFileStorage(Pageable pageable, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[listFileStorage] principal username: {}", userPrincipal.getUsername());
        Page<FileStorage> fileStoragePage = fileStorageService.findAll(pageable);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileStoragePage);
    }

    /**
     * @param id            fileStorage id
     * @param userPrincipal user principal
     * @return FileStorage data transfer object
     * @throws AppException For application exception
     */
    @GetMapping("/file_storage/{id}")
    @ApiOperation(value = "Get FileStorage by id",
            nickname = "getFileStorage",
            notes = "Retrieve the fileStorage with corresponding id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(FileStorageDto.class)
    public ResponseEntity<FileStorage> getFileStorage(
            @ApiParam(value = "id of the fileStorage", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getFileStorage] id: {}, principal username: {}", id, userPrincipal.getUsername());

        FileStorage fileStorage = fileStorageService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, id)));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileStorage);
    }

    /**
     * @param name          fileStorage name
     * @param userPrincipal user principal
     * @return FileStorage data transfer object
     * @throws AppException For application exception
     */
    @GetMapping("/file_storage")
    @ApiOperation(value = "Get FileStorage by name",
            nickname = "getFileStorageByName",
            notes = "Retrieve the fileStorage with corresponding name",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(FileStorageDto.class)
    public ResponseEntity<FileStorage> getFileStorageByName(@RequestParam String name, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getFileStorageByName] name: {}, principal username: {}", name, userPrincipal.getUsername());

        FileStorage fileStorage = this.fileStorageService.findByName(name).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] name: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, name)));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileStorage);
    }

    /**
     * @param id             fileStorage id
     * @param fileStorageDto File storage new information
     * @param userPrincipal  user principal
     * @return FileStorage data transfer object
     * @throws AppException For application exception
     */
    @PutMapping("/file_storage/{id}")
    @ApiOperation(value = "Update fileStorage by id",
            nickname = "updateFileStorage",
            notes = "Update fileStorage with corresponding id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(FileStorageDto.class)
    public ResponseEntity<FileStorage> updateFileStorage(
            @ApiParam(value = "id of the fileStorage", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @RequestBody FileStorageDto fileStorageDto, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateFileStorage] id: {}, fileStorage: {}, principal username: {}", id, fileStorageDto, userPrincipal.getUsername());
        FileStorage result = this.fileStorageService.update(fileStorageDto, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }


    /**
     * @param id            fileStorage id
     * @param userPrincipal user principal
     * @return FileStorage data transfer object
     * @throws AppException For application exception
     */
    @DeleteMapping("/file_storage/{id}")
    @ApiOperation(value = "Inactive fileStorage by id",
            nickname = "inactiveFileStorage",
            notes = "Inactive fileStorage with corresponding id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public FileStorage inactiveFileStorage(@PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[inactiveFileStorage] id: {}, principal username: {}", id, userPrincipal.getUsername());

        FileStorage fileStorage = this.fileStorageService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, id)));
        return this.fileStorageService.inactiveFileStorage(fileStorage, userPrincipal.getUsername());
    }

    /**
     * @param id            scan task id
     * @param file          file need to upload
     * @param checksum      checksum
     * @param userPrincipal user principal
     * @return Void
     * @throws AppException For application exception
     */
    @ApiOperation(
            value = "Import file info to scan task",
            nickname = "importFileInfoToScanTask",
            notes = "Import file info to scan task",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PostMapping("/scan_task/{id}/file_info")
    public ResponseEntity<Void> importFileInfoToScanTask(
            @PathVariable UUID id,
            @RequestParam("upload_file") MultipartFile file,
            @RequestParam(value = "checksum", required = false) String checksum,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[importFileInfoToScanTask] id: {}, file isEmpty: {}, checksum: {}, principal username: {}",
                id,
                file.isEmpty(),
                checksum,
                userPrincipal.getUsername()
        );

        ScanTask scanTask = this.scanTaskService.findById(id)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] scanTaskId: {}",
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                id
                        )
                ));
        File inputFile = FileService.getTempFile(file);
        if (StringUtils.isNotBlank(checksum)) {
            if (!this.fileService.checkIntegrityWithCrc32(inputFile, checksum)) {
                throw new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_INCONSISTENT,
                        HttpURLConnection.HTTP_CONFLICT,
                        AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] expectedCheckSum: {}",
                                AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate,
                                checksum
                        )
                );
            }
        }

        TracerUtil.setScanTaskTag(this.tracer, scanTask);
        try {
            this.scanFileService.importFileInfoToScanTask(scanTask, inputFile, userPrincipal);
        } catch (Exception e) {
            this.scanTaskService.updateScanTaskStatus(
                    scanTask,
                    ScanTaskStatusLog.Stage.IMPORT_FILE_INFO,
                    ScanTaskStatusLog.Status.FAILED,
                    100.0,
                    AppException.ErrorCode.E_API_FILE_GETFILESTORAGE_FAILED.unifyErrorCode,
                    AppException.ErrorCode.E_API_FILE_GETFILESTORAGE_FAILED.messageTemplate,
                    userPrincipal.getUsername()
            );
            throw e;
        }

        log.info("[importFileInfoToScanTask] begin to delete file {}", inputFile.getPath());
        boolean isFileDeleted = FileUtils.deleteQuietly(inputFile);
        log.info("[importFileInfoToScanTask] is file {} deleted: {}", inputFile.getPath(), isFileDeleted);

        return ResponseEntity.noContent().build();
    }

    /**
     * @param file          file need to upload
     * @param checksum      CRC32 value of the file
     * @param type          type of the file: SOURCE, LIB, TEMP
     * @param userPrincipal user principal
     * @return Void
     * @throws AppException For application exception
     */
    @ApiOperation(value = "Upload file",
            nickname = "uploadFile",
            notes = "upload file to file system",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PostMapping("/file/file_info")
    @Dto(FileInfoDto.class)
    public ResponseEntity<FileInfo> uploadFile(@RequestParam("upload_file") @NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL) MultipartFile file, @RequestParam(value = "file_checksum", required = false) String checksum,
                                               @RequestParam(value = "type", required = false, defaultValue = "SOURCE") String type, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[uploadFile] file size: {}, file type: {}, checksum: {}, type: {}, principal username: {}", file.getSize(), file.getContentType(), checksum, type, userPrincipal.getUsername());

        FileInfo.Type fileType = EnumUtils.getEnumIgnoreCase(FileInfo.Type.class, type);
        if (fileType == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_TYPE.unifyErrorCode,
                    CommonUtil.formatString("[{}] type: {}", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_TYPE.messageTemplate, type));
        }
        FileInfo fileInfo = fileService.addFile(file, checksum, fileType, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.FILE_ID, fileInfo.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileInfo);
    }


    /**
     * @param searchScanFileRequest Search scan file for scanTask
     * @param userPrincipal         userPrincipal user principal
     * @return page of ScanFileDto
     * @throws AppException For application exception
     */
    @PostMapping("/scan_file")
    @ApiOperation(value = "Search scan files for scanTask",
            nickname = "searchScanFile",
            notes = "Search scan files for scanTask",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(ScanFileDto.class)
    public ResponseEntity<Page<ScanFileDto>> searchScanFile(@RequestBody SearchScanFileRequest searchScanFileRequest, Pageable pageable, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[searchScanFile]scan task id: {}, types: {}, scan file ids: {}, depth: {},username: {}",
                searchScanFileRequest.getScanTaskId(), searchScanFileRequest.getTypes(), searchScanFileRequest.getScanFileIds(), searchScanFileRequest.getDepth(), userPrincipal.getUsername());
        ScanTask scanTask = scanTaskService.findById(searchScanFileRequest.getScanTaskId())
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, searchScanFileRequest.getScanTaskId())));
        TracerUtil.setScanTaskTag(tracer, scanTask);
        List<String> invalidTypes = searchScanFileRequest.getTypes().stream().filter(type -> !EnumUtils.isValidEnum(ScanFile.Type.class, type)).collect(Collectors.toList());
        if (!invalidTypes.isEmpty()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_TYPE.unifyErrorCode,
                    CommonUtil.formatString("[{}] types {} must have any value among {}", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_TYPE.messageTemplate, invalidTypes, ScanFile.Type.values()));
        }
        Page<ScanFileDto> result;
        Integer depth = searchScanFileRequest.getDepth();
        List<ScanFile> scanFileList = new ArrayList<>();

        if (!searchScanFileRequest.getScanFileIds().isEmpty()) {
            scanFileList.addAll(scanFileService.findByScanFileIds(searchScanFileRequest.getScanFileIds()));
        }
        Page<ScanFile> scanFiles;
        if ((!searchScanFileRequest.getScanFileIds().isEmpty()) && scanFileList.isEmpty()) {
            scanFiles = Page.empty();
        } else {
            scanFiles = scanFileService.searchScanFile(scanTask, searchScanFileRequest.getTypes(), scanFileList, depth, pageable);
        }
        result = RestResponsePage.<ScanFileDto>builder()
                .content(scanFileService.convertScanFilesToDto(scanFiles.getContent()))
                .pageable(scanFiles.getPageable())
                .total(scanFiles.getTotalElements())
                .build();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
    }


    /**
     * @param findFileInfoRequest findFileInfoRequest
     * @param userPrincipal       userPrincipal user principal
     * @return page of FileInfoDto
     * @throws AppException For application exception
     */
    @PostMapping("/file_info")
    @ApiOperation(value = "Find file info",
            nickname = "findFileInfo",
            notes = "Find file info by FileInfo attributes",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(FileInfoDto.class)
    public ResponseEntity<Page<FileInfo>> findFileInfo(@RequestBody FindFileInfoRequest findFileInfoRequest, Pageable pageable, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[findFileInfo]findFileInfoRequest, name: {}, type: {}, version: {}, status: {}, username: {}", findFileInfoRequest.getName(), findFileInfoRequest.getType(), findFileInfoRequest.getVersion(), findFileInfoRequest.getStatus(), userPrincipal.getUsername());

        if (StringUtils.isBlank(findFileInfoRequest.getName())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.unifyErrorCode,
                    CommonUtil.formatString("[{}] name should not be blank", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.messageTemplate));
        }

        if (StringUtils.isBlank(findFileInfoRequest.getType())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.unifyErrorCode,
                    CommonUtil.formatString("[{}] type should not be blank", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.messageTemplate));
        }

        if (StringUtils.isBlank(findFileInfoRequest.getVersion())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.unifyErrorCode,
                    CommonUtil.formatString("[{}] version should not be blank", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.messageTemplate));
        }

        if (StringUtils.isBlank(findFileInfoRequest.getStatus())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.unifyErrorCode,
                    CommonUtil.formatString("[{}] status should not be blank", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.messageTemplate));
        }

        FileInfo.Type fileType = EnumUtils.getEnumIgnoreCase(FileInfo.Type.class, findFileInfoRequest.getType());
        if (fileType == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_TYPE.unifyErrorCode,
                    CommonUtil.formatString("[{}] invalid type: {}", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_TYPE.messageTemplate, findFileInfoRequest.getType()));
        }

        FileInfo.Status fileInfoStatus = EnumUtils.getEnumIgnoreCase(FileInfo.Status.class, findFileInfoRequest.getStatus());
        if (fileInfoStatus == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.unifyErrorCode,
                    CommonUtil.formatString("[{}] invalid status: {}", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_VALUE.messageTemplate, findFileInfoRequest.getStatus()));
        }

        Page<FileInfo> fileInfoPage = this.fileService.findFileInfo(findFileInfoRequest.getName(), fileType, findFileInfoRequest.getVersion(), fileInfoStatus, pageable);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileInfoPage);
    }

    @ApiOperation(
            value = "Create file cache",
            nickname = "createFileCache",
            notes = "Create file cache",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/file_cache")
    @Dto(FileCacheDto.class)
    public ResponseEntity<FileCache> createFileCache(
            @Valid @RequestBody FileCacheDto fileCacheDto,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[createFileCache] file cache: {}", fileCacheDto);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(this.fileCacheService.addFileCache(fileCacheDto, userPrincipal.getUsername()));
    }

    @ApiOperation(
            value = "Update file cache",
            nickname = "updateFileCache",
            notes = "Update file cache",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PutMapping("/file_cache")
    @Dto(FileCacheDto.class)
    public ResponseEntity<FileCache> updateFileCache(
            @Valid @RequestBody FileCacheDto fileCacheDto,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[updateFileCache] file cache: {}", fileCacheDto);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(this.fileCacheService.updateFileCache(fileCacheDto, userPrincipal.getUsername()));
    }

    @ApiOperation(
            value = "List file cache",
            nickname = "listFileCache",
            notes = "List file cache",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping("/file_cache_list")
    @Dto(FileCacheDto.class)
    public ResponseEntity<List<FileCache>> listFileCache() {
        log.info("[listFileCache]");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(this.fileCacheService.findAllFileCache());
    }

    /**
     * @param scanTaskId    scan Task id
     * @param fileName      file name
     * @param checksum      CRC32 value of the file
     * @param type          type of the file: SOURCE, LIB, TEMP
     * @param userPrincipal user principal
     * @return Void
     * @throws AppException For application exception
     */
    @ApiOperation(value = "Upload file",
            nickname = "uploadFile",
            notes = "upload file from minio to file system",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PostMapping("/file/file_system")
    @Dto(FileInfoDto.class)
    public ResponseEntity<FileInfo> addFileFromFileSystem(@RequestParam(value = "scan_task_id") String scanTaskId,
                                                          @RequestParam(value = "file_name") String fileName,
                                                          @RequestParam(value = "file_checksum", required = false) String checksum,
                                                          @RequestParam(value = "type", required = false, defaultValue = "SOURCE") String type,
                                                          @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[uploadFileFromFileSystem] ScanTaskId: {}, File Name: {}, checksum: {}, type: {}, principal username: {}",
                scanTaskId, fileName, checksum, type, userPrincipal.getUsername());

        FileInfo.Type fileType = EnumUtils.getEnumIgnoreCase(FileInfo.Type.class, type);
        if (fileType == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_TYPE.unifyErrorCode,
                    CommonUtil.formatString("[{}] type: {}", AppException.ErrorCode.E_API_FILE_COMMON_INVALID_TYPE.messageTemplate, type));
        }
        FileInfo fileInfo = fileService.addFileFromFileSystem(scanTaskId, fileName, checksum, fileType, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.FILE_ID, fileInfo.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileInfo);
    }


    /**
     * @param id            scan task id
     * @param request       request
     * @param userPrincipal user principal
     * @return Resource
     * @throws AppException For application exception
     */
    @GetMapping("/log/scan_task/{id}")
    @ApiOperation(value = "Get scan task log",
            nickname = "getFile",
            notes = "Get scan task log",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LogFileResponse> getScanTaskLog(
            @ApiParam(value = "id of can task", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, HttpServletRequest request, @CurrentUser UserPrincipal userPrincipal) throws AppException, IOException {
        log.info("[getScanTaskLog] id: {}, principal username: {}", id, userPrincipal.getUsername());

        Optional<ScanTask> scanTaskOptional = scanTaskService.findById(id);
        String username = "";
        if (scanTaskOptional.isPresent()) {
            ScanTask scanTask = scanTaskOptional.get();
            username = scanTask.getCreatedBy();
        }

        File file = Paths.get(scanVolumePath, id.toString(), scanTaskLogFileName).toFile();
        String logContent = FileUtils.readFileToString(file, "UTF-8");
        LogFileResponse logFileResponse = LogFileResponse.builder()
                .logContent(logContent)
                .username(username)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(logFileResponse);
    }

}
