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
import com.mchange.util.AssertException;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.repository.AsyncJobRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class AsyncJobServiceTest {

    private AsyncJobRepository asyncJobRepository;
    private AsyncJobService asyncJobService;

    @BeforeEach
    void setUp() {
        asyncJobRepository = mock(AsyncJobRepository.class);
        asyncJobService = new AsyncJobService(asyncJobRepository);
    }

    @Test
    void addAsyncJob_normal_success() {
        AsyncJob inputAsyncJob=AsyncJob.builder().name("A").info("Information").id(UUID.randomUUID()).build();
        when(asyncJobRepository.save(any(AsyncJob.class))).thenReturn(inputAsyncJob);
        AsyncJob asyncJob=asyncJobService.addAsyncJob(inputAsyncJob);
        assertEquals(inputAsyncJob.getName(),asyncJob.getName());
        assertEquals(inputAsyncJob.getId(),asyncJob.getId());
        assertEquals(inputAsyncJob.getInfo(),asyncJob.getInfo());
    }

    @Test
    void addAsyncJob_exception_throwException() {
        AsyncJob inputAsyncJob=AsyncJob.builder().name("A").info("Information").id(UUID.randomUUID()).build();
        when(asyncJobRepository.save(any(AsyncJob.class))).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class,()->asyncJobService.addAsyncJob(inputAsyncJob));
    }

    @Test
    void updateAsyncJob_normal_success() {
        AsyncJob inputAsyncJob=AsyncJob.builder().name("A").info("Information").id(UUID.randomUUID()).build();
        when(asyncJobRepository.save(any(AsyncJob.class))).thenReturn(inputAsyncJob);
        AsyncJob asyncJob=asyncJobService.updateAsyncJob(inputAsyncJob);
        assertEquals(inputAsyncJob.getName(),asyncJob.getName());
        assertEquals(inputAsyncJob.getId(),asyncJob.getId());
        assertEquals(inputAsyncJob.getInfo(),asyncJob.getInfo());
    }

    @Test
    void updateAsyncJob_exception_throwException() {
        AsyncJob inputAsyncJob=AsyncJob.builder().name("A").info("Information").id(UUID.randomUUID()).build();
        when(asyncJobRepository.save(any(AsyncJob.class))).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class,()->asyncJobService.updateAsyncJob(inputAsyncJob));
    }

    @Test
    void findAsyncJobById_normal_success() {
        AsyncJob inputAsyncJob=AsyncJob.builder().name("A").info("Information").id(UUID.randomUUID()).build();
        when(asyncJobRepository.findById(any(UUID.class))).thenReturn(Optional.of(AsyncJob.builder().build()));
        Optional<AsyncJob> asyncJobOptional=asyncJobService.findAsyncJobById(UUID.randomUUID());

    }


    @Test
    void findAsyncJobById_exception_throwException() {
        AsyncJob inputAsyncJob=AsyncJob.builder().name("A").info("Information").id(UUID.randomUUID()).build();
        when(asyncJobRepository.findById(any(UUID.class))).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class,()->asyncJobService.findAsyncJobById(UUID.randomUUID()));
    }

}
