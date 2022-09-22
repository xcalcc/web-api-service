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

import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.xcal.api.entity.FileInfo;
import com.xcal.api.entity.FileStorage;
import com.xcal.api.exception.AppException;
import com.xcal.api.repository.GerritRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class GerritServiceTest {
    private GerritService gerritService;
    private GerritRepository gerritRepository;

    private final String girUrl = "http://gerrit/test_id";
    private final String projectId = "test_id";
    private final String commitId = "dummyCommitId";
    private final String relativePath = "/src/dummy/a.b";

    private final FileInfo fileInfo = FileInfo.builder()
            .id(UUID.randomUUID())
            .fileStorage(FileStorage.builder().id(UUID.randomUUID())
                    .fileStorageHost("http://fakehost.com")
                    .name("mock")
                    .fileStorageType(FileStorage.Type.GERRIT).build())
            .noOfLines(10)
            .relativePath(relativePath)
            .version(commitId)
            .build();


    @BeforeEach
    void setUp() {
        gerritRepository = mock(GerritRepository.class);
        gerritService = new GerritService(gerritRepository);
        when(gerritRepository.getGerritRestClient(any(), any(), any())).thenReturn(mock(GerritRestClient.class));
    }

    @Test
    void getFileContentAsString_Success() throws AppException {
        String expectedString = "dummyContent";
        when(gerritRepository.getRepositoryFileContent(any(), eq(projectId), eq(commitId), eq(relativePath))).thenReturn(expectedString);
        String result = this.gerritService.getFileContentAsString(fileInfo, projectId, commitId, relativePath);
        assertEquals(expectedString, result);
    }

    @Test
    void getFileContentAsString_Succes() throws AppException {
        String expectedString = "dummyContent";
        Map<String, String> attribute = new HashMap<>();
        attribute.put("GERRIT_PROJECT_ID", projectId);
        attribute.put("GIT_URL", girUrl);
        when(gerritRepository.getRepositoryFileContent(any(), eq(projectId), eq(commitId), eq(relativePath))).thenReturn(expectedString);
        String result = this.gerritService.getFileContentAsString(fileInfo, attribute);
        assertEquals(expectedString, result);
    }
}
