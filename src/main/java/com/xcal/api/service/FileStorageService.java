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

import com.xcal.api.entity.FileStorage;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.FileStorageDto;
import com.xcal.api.repository.FileStorageRepository;
import com.xcal.api.util.CommonUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FileStorageService {

    @NonNull FileStorageRepository fileStorageRepository;

    @NonNull ModelMapper modelMapper;

    public Optional<FileStorage> findById(UUID id){
        log.debug("[findById] id: {}", id);
        return fileStorageRepository.findById(id);
    }

    public Optional<FileStorage> findByName(String name){
        log.debug("[findByName] name: {}", name);
        return fileStorageRepository.findByName(name);
    }

    public FileStorage add(FileStorage fileStorage, String currentUsername) throws AppException {
        log.info("[add] fileStorage: {}, currentUsername: {}", fileStorage, currentUsername);
        Optional<FileStorage> optionalFileStorage= this.findByName(fileStorage.getName());
        if(optionalFileStorage.isPresent()) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_ALREADY_EXIST, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_FILESTORAGE_ADD_ALREADY_EXIST.unifyErrorCode,
                    CommonUtil.formatString("[{}] name: {}", AppException.ErrorCode.E_API_FILESTORAGE_ADD_ALREADY_EXIST.messageTemplate, fileStorage.getName()));
        }

        Date now = new Date();
        fileStorage.setStatus(FileStorage.Status.ACTIVE);
        fileStorage.setCreatedBy(currentUsername);
        fileStorage.setCreatedOn(now);
        fileStorage.setModifiedBy(currentUsername);
        fileStorage.setModifiedOn(now);

        fileStorage = fileStorageRepository.save(fileStorage);
        log.info("[add] fileStorage add successfully, fileStorage: {}", fileStorage);
        return fileStorage;
    }

    public FileStorage update(FileStorageDto fileStorageDto, String currentUsername) throws AppException {
        log.info("[update] fileStorage: {}", fileStorageDto);
        FileStorage result;
        Optional<FileStorage> fileStorageOptional = this.findById(fileStorageDto.getId());
        if(!fileStorageOptional.isPresent()) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, fileStorageDto.getId()));
        }
        FileStorage dbFileStorage = fileStorageOptional.get();
        dbFileStorage.setDescription(fileStorageDto.getDescription());
        dbFileStorage.setFileStorageHost(fileStorageDto.getFileStorageHost());
        dbFileStorage.setCredentialType(fileStorageDto.getCredentialType());
        dbFileStorage.setCredential(fileStorageDto.getCredential());
        if(fileStorageDto.getStatus() != null){
            dbFileStorage.setStatus(FileStorage.Status.valueOf(fileStorageDto.getStatus().toString()));
        }
        dbFileStorage.setModifiedOn(new Date());
        dbFileStorage.setModifiedBy(currentUsername);
        result = fileStorageRepository.save(dbFileStorage);
        return result;
    }

    public Page<FileStorage> findAll(Pageable pageable) {
        log.info("[findAll] pageable: {}", pageable);
        return fileStorageRepository.findAll(pageable);
    }

    public Page<FileStorage> simpleSearch(FileStorage searchFileStorage, Pageable pageable){
        log.info("[simpleSearch] searchFileStorage: {}, pageable: {}",searchFileStorage, pageable);

        ExampleMatcher em = ExampleMatcher.matching();
        em = em.withIgnoreCase()
                .withMatcher("name", matcher -> matcher.ignoreCase().contains())
                .withMatcher("description", matcher -> matcher.ignoreCase().contains())
                .withMatcher("fileStorage_type", matcher -> matcher.ignoreCase().contains())
                .withMatcher("fileStorage_host", matcher -> matcher.ignoreCase().contains())
                .withMatcher("status", matcher -> matcher.ignoreCase().exact());
        return this.fileStorageRepository.findAll(Example.of(searchFileStorage, em), pageable);
    }

    public FileStorage inactiveFileStorage(FileStorage fileStorage, String currentUsername) {
        log.info("[inactiveFileStorage] fileStorage: {}, currentUsername: {}", fileStorage, currentUsername);
        fileStorage.setStatus(FileStorage.Status.INACTIVE);
        fileStorage.setModifiedBy(currentUsername);
        return fileStorageRepository.saveAndFlush(fileStorage);
    }
}
