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
import com.xcal.api.model.payload.RestResponsePage;
import com.xcal.api.repository.FileStorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class FileStorageServiceTest {
    private FileStorageRepository fileStorageRepository;
    private FileStorageService fileStorageService;

    private String currentUsername = "xc5";
    private UUID storageId = UUID.fromString("11111111-1111-1111-1111-111111111113");
    private String storageName = "GITHUB";
    private String fileStorageHost = "/share/src";
    private FileStorage fileStorage = FileStorage.builder()
            .id(storageId)
            .name(storageName)
            .fileStorageHost(fileStorageHost)
            .status(FileStorage.Status.PENDING)
            .build();
    private FileStorageDto fileStorageDto = FileStorageDto.builder().id(storageId).name(storageName).fileStorageHost(fileStorageHost).status(FileStorageDto.Status.ACTIVE).build();

    @BeforeEach
    void setup() {
        fileStorageRepository = mock(FileStorageRepository.class);
        fileStorageService = new FileStorageService(fileStorageRepository, mock(ModelMapper.class));
    }


    @Test
    void findByIdTestSuccess() {
        log.info("[findByIdTestSuccess]");
        when(fileStorageRepository.findById(fileStorage.getId())).thenReturn(Optional.of(fileStorage));
        Optional<FileStorage> fileStorageOptional = fileStorageService.findById(fileStorage.getId());
        assertTrue(fileStorageOptional.isPresent());
        assertEquals(fileStorage.getId(), fileStorageOptional.get().getId());
        assertEquals(fileStorage.getName(), fileStorageOptional.get().getName());
    }

    @Test
    void findByIdTestFail() {
        log.info("[findByIdTestFail]");
        UUID id = UUID.randomUUID();
        when(fileStorageRepository.findById(id)).thenReturn(Optional.empty());
        assertFalse(fileStorageService.findById(id).isPresent());
    }

    @Test
    void findByNameTestSuccess() {
        log.info("[findByNameTestSuccess]");
        when(fileStorageRepository.findByName(fileStorage.getName())).thenReturn(Optional.of(fileStorage));
        Optional<FileStorage> fileStorageOptional = fileStorageService.findByName(fileStorage.getName());
        assertTrue(fileStorageOptional.isPresent());
        assertEquals(fileStorage.getId(), fileStorageOptional.get().getId());
        assertEquals(fileStorage.getName(), fileStorageOptional.get().getName());
    }

    @Test
    void findByNameTestFail() {
        log.info("[findByNameTestFail]");
        String storageName = "NOT_EXIST";
        when(fileStorageRepository.findByName(storageName)).thenReturn(Optional.empty());
        assertFalse(fileStorageService.findByName(storageName).isPresent());
    }

    @Test
    void addTestSuccess() throws AppException {
        log.info("[findByNameTestFail]");
        when(fileStorageRepository.save(any())).thenReturn(fileStorage);
        FileStorage newFileStorage = fileStorageService.add(fileStorage, currentUsername);
        assertEquals(storageId, newFileStorage.getId());
        assertEquals(storageName, newFileStorage.getName());
        assertEquals(fileStorageHost, newFileStorage.getFileStorageHost());
    }


    @Test
    void addTestFailed() {
        log.info("[addTestFailed]");
        when(fileStorageRepository.findByName(eq(storageName))).thenReturn(Optional.of(fileStorage));
        assertThrows(AppException.class, () -> fileStorageService.add(fileStorage, currentUsername));
    }


    @Test
    void updateTestSuccess() throws AppException {
        log.info("[updateTestSuccess]");
        when(fileStorageRepository.findById(storageId)).thenReturn(Optional.of(fileStorage));
        when(fileStorageRepository.save(any())).thenReturn(fileStorage);
        FileStorage result = fileStorageService.update(fileStorageDto, anyString());
        assertEquals(storageId, result.getId());
        assertEquals(storageName, result.getName());
        assertEquals(fileStorageHost, result.getFileStorageHost());
    }

    @Test
    void updateTestFail() {
        log.info("[updateTestFail]");
        when(fileStorageRepository.findById(storageId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> fileStorageService.update(fileStorageDto, currentUsername));
    }


    @Test
    void findAllTestSuccess() {
        log.info("[findAllTestSuccess]");
        when(fileStorageRepository.findById(eq(storageId))).thenReturn(Optional.of(fileStorage));
        Pageable pageable = PageRequest.of(0, 20);
        List<FileStorage> fileStorageList = Collections.singletonList(fileStorage);
        when(fileStorageRepository.findAll(pageable)).thenReturn(new RestResponsePage<>(fileStorageList, pageable, fileStorageList.size()));
        Page<FileStorage> settingPage = fileStorageService.findAll(pageable);
        log.info(settingPage.toString());
        FileStorage result = settingPage.getContent().get(0);
        assertEquals(storageId, result.getId());
        assertEquals(storageName, result.getName());
        assertEquals(fileStorageHost, result.getFileStorageHost());
    }

    @Test
    void simpleSearchTestSuccess() {
        log.info("[simpleSearchTestSuccess]");
        Page<FileStorage> pageFileStorage = new PageImpl<>(Collections.singletonList(fileStorage));
        when(fileStorageRepository.findAll(any(), any(Pageable.class))).thenReturn(pageFileStorage);
        assertEquals(pageFileStorage, fileStorageService.simpleSearch(FileStorage.builder().build(), PageRequest.of(0, 20)));
    }

    @Test
    void inactiveFileStorageTestSuccess() {
        log.info("[inactiveFileStorageTestSuccess]");
        when(fileStorageRepository.saveAndFlush(any())).thenReturn(fileStorage);
        FileStorage result = fileStorageService.inactiveFileStorage(fileStorage, currentUsername);
        assertEquals(storageId, result.getId());
        assertEquals(storageName, result.getName());
        assertEquals(fileStorageHost, result.getFileStorageHost());
    }
}
