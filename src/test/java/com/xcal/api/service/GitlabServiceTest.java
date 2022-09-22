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
import com.xcal.api.entity.FileStorage;
import com.xcal.api.exception.AppException;
import com.xcal.api.repository.GitlabRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class GitlabServiceTest {
    private GitlabService gitlabService;
    private GitlabRepository gitlabRepository;

    @BeforeEach
    void setUp() {
        gitlabRepository = mock(GitlabRepository.class);
        gitlabService = new GitlabService(gitlabRepository);
    }

    @Test
    void getProjectIdOrPathWithGitSuffixTestShouldSuccess() throws AppException {
        String fileStorageHost = "https://gitlab.com";
        String gitUrl = "https://gitlab.com/share/src.git";
        String projectIdOrPath = gitlabService.getProjectIdOrPath(fileStorageHost, gitUrl);
        assertEquals("share/src", projectIdOrPath);
    }

    @Test
    void getProjectIdOrPathWithoutGitSuffixTestShouldSuccess() throws AppException {
        String fileStorageHost = "https://gitlab.com";
        String gitUrl = "https://gitlab.com/share/src";
        String projectIdOrPath = gitlabService.getProjectIdOrPath(fileStorageHost, gitUrl);
        assertEquals("share/src", projectIdOrPath);
    }

    @Test
    void getProjectIdOrPathWhenGitUrlNotStartsWithHostUrlShouldThrowException() {
        String fileStorageHost = "https://gitlab.com1";
        String gitUrl = "https://gitlab.com/share/src";
        assertThrows(AppException.class, () -> gitlabService.getProjectIdOrPath(fileStorageHost, gitUrl));
    }

    @Test
    void cloneRepositoryProjectToLocalDirectoryTestSuccess() throws AppException {
        log.info("[cloneRepositoryProjectToLocalDirectoryTestSuccess]");
        when(gitlabRepository.cloneRepositoryProjectToLocalDirectory("https://gitlab.com","/share/src")).thenReturn("e8bba8b87cdc36d51e0be03e6834282e207dfc92");
        String latestCommitId = gitlabService.cloneRepositoryProjectToLocalDirectory("https://gitlab.com","/share/src");
        assertEquals("e8bba8b87cdc36d51e0be03e6834282e207dfc92",latestCommitId);
    }

    @Test
    void getFileContentAsString_FileInfoParamWithBlankFileStorageExtraInfo_ShouldThrowException() {
        FileInfo fileInfo = FileInfo.builder().fileStorageExtraInfo(null)
                .fileStorage(FileStorage.builder().fileStorageHost("https://gitlab.com").build())
                .relativePath("c_testcase/basic/npd.c")
                .version("e8bba8b87cdc36d51e0be03e6834282e207dfc92")
                .build();
        assertThrows(AppException.class, () -> gitlabService.getFileContentAsString(fileInfo, null));
    }

    @Test
    void getLatestCommitId_Success() throws AppException {
        String hostUrl = "https://gitlab.com";
        String commitId = "e8bba8b87cdc36d51e0be03e6834282e207dfc92";
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(hostUrl).fileStorageType(FileStorage.Type.GITLAB).build();
        when(gitlabRepository.getLatestCommitId(hostUrl,FileStorage.Type.GITLAB.toString(), "share/src", null, null)).thenReturn(commitId);
        String latestCommitId = gitlabService.getLatestCommitId(fileStorage,"https://gitlab.com/share/src.git", null, null);
        assertEquals(commitId, latestCommitId);
    }
}
