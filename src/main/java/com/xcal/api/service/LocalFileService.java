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

import com.xcal.api.entity.FileInfo;
import com.xcal.api.exception.AppException;
import com.xcal.api.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalFileService {

    /**
     * Get file in resource type with fileInfo
     * @param fileInfo file info
     * @return File in Resource
     * @throws AppException For application exception
     */
    public Resource getFileAsResource(FileInfo fileInfo) throws AppException {
        log.trace("[getFileAsResource] fileInfo, id: {}", fileInfo.getId());
        Resource resource;
        try {
            Path path = this.getLocalFilePath(fileInfo);
            resource = new UrlResource(path.toUri());
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.messageTemplate, e);
        }
        return resource;
    }

    /**
     * @param fileInfo file info
     * @return Real Path of the file in fileInfo
     */
    public Path getLocalFilePath(FileInfo fileInfo) {
        log.trace("[getLocalFilePath] fileInfo, id: {}", fileInfo.getId());
        Path path = new File(fileInfo.getFileStorage().getFileStorageHost(),fileInfo.getRelativePath()).toPath();
        log.trace("[getLocalFilePath] return path: {}", path);
        return path;
    }

    /**
     * Add file to the system
     *
     * @param file     file to be store
     * @param fileInfo file info
     * @throws AppException For file is null and IOException when create file.
     */
    public void storeFile(MultipartFile file, FileInfo fileInfo) throws AppException {
        log.trace("[storeFile] fileInfo, id: {}, path: {}", fileInfo.getId(), fileInfo.getRelativePath());
        if(file == null){
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_MISSING_FILE.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_MISSING_FILE.messageTemplate);
        }
        log.trace("[storeFile] file size: {}, fileInfo, id: {}", file.getSize(), fileInfo.getId());
        try {
            Path path = this.getLocalFilePath(fileInfo);
            if(path.toFile().exists()){
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_ALREADY_EXIST, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_FILE_ADD_ALREADY_EXIST.unifyErrorCode, AppException.ErrorCode.E_API_FILE_ADD_ALREADY_EXIST.messageTemplate);
            }
            log.debug("[storeFile] path: {}", path);
            Files.createDirectories(path.getParent());
            file.transferTo(path);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.messageTemplate, e);
        }
    }

    /**
     * Delete file
     * @param fileInfo file to be delete
     * @throws AppException
     */
    public void deleteFile(FileInfo fileInfo) throws AppException {
        log.debug("[deleteFile] fileInfo, id: {}, path: {}", fileInfo.getId(), fileInfo.getRelativePath());
        Path path = this.getLocalFilePath(fileInfo);
        if(!path.toFile().exists()){
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] fileInfo, id: {}, path: {}", AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.messageTemplate, fileInfo.getId(),path));
        }
        FileUtils.deleteQuietly(path.toFile());
    }

    /**
     * Delete file
     * @param file file or directory to delete, must not be null
     */
    public void deleteFile(File file) {
        log.debug("[deleteFile] file or directory: {}", file);
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            log.error("[deleteFile] delete file {} failed, error message: {}", file, e.getMessage());
            log.error("[deleteFile] error stack trace info: {}", Arrays.toString(e.getStackTrace()));
        }
        log.debug("[deleteFile] file or directory is deleted: {}", file);
    }
}
