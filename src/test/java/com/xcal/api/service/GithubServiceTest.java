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
import com.xcal.api.repository.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class GithubServiceTest {
    private GithubService githubService;
    private GithubRepository githubRepository;

    @BeforeEach
    void setUp() {
        githubRepository = mock(GithubRepository.class);
        githubService = new GithubService(githubRepository);
    }

    @Test
    void cloneRepositoryProjectToLocalDirectory() throws AppException {
        log.info("[cloneRepositoryProjectToLocalDirectoryTestSuccess]");
        when(githubRepository.cloneRepositoryProjectToLocalDirectory("https://github.com","/share/src")).thenReturn("e8bba8b87cdc36d51e0be03e6834282e207dfc92");
        String latestCommitId = githubService.cloneRepositoryProjectToLocalDirectory("https://github.com","/share/src");
        assertEquals("e8bba8b87cdc36d51e0be03e6834282e207dfc92",latestCommitId);
    }

    @Test
    void getGithubProjectUrlTestSuccess() throws AppException {
        log.info("[getGithubProjectUrlTestSuccess]");
        String fileStorageExtraInfo = "{\n" +
                "  \"gitUrl\" : \"https://github.com/AlynxZhou/INTANG\"\n" +
                "}";
        String resultGitlabProjectId = githubService.getGithubProjectUrl(fileStorageExtraInfo);
        assertEquals("https://github.com/AlynxZhou/INTANG",resultGitlabProjectId);
    }


    @Test
    void getGithubProjectUrlTestGitUrlIsBlankFail() {
        log.info("[getGithubProjectUrlTestGitUrlIsBlankFail]");
        String fileStorageExtraInfo = "{\n" +
                "  \"gitUrl\" : \"\"\n" +
                "}";
        assertThrows(AppException.class, () -> githubService.getGithubProjectUrl(fileStorageExtraInfo));
    }

    @Test
    void getGithubProjectUrl_fileStorageExtraInfoIsEmpty_ThrowException() {
        log.info("[getGithubProjectUrl_fileStorageExtraInfoIsEmpty_ThrowException]");
        String fileStorageExtraInfo = "";
        AppException appException = assertThrows(AppException.class, () -> githubService.getGithubProjectUrl(fileStorageExtraInfo));
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, appException.getResponseCode());
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_INCORRECT_PARAM, appException.getErrorCode());
    }

    @Test
    void getFileContentAsResourceTestSuccess() throws AppException, IOException {
        log.info("[getFileContentAsResourceTestSuccess]");
        String fileStorageExtraInfo = "{\n" +
                "  \"gitUrl\" : \"https://github.com/steveli1840/test_github\"\n" +
                "}";
        FileInfo fileInfo = FileInfo.builder().fileStorageExtraInfo(fileStorageExtraInfo)
                .fileStorage(FileStorage.builder().fileStorageHost("https://github.com").build())
                .relativePath("c_testcase/basic/npd.c")
                .version("e8bba8b87cdc36d51e0be03e6834282e207dfc92")
                .build();
        String content = "#include <stdio.h>\n" +
                "\n" +
                "int assign(int* a)\n" +
                "{\n" +
                "  return *a;  /* dereference a */\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "  int *a=NULL, b;\n" +
                "  b = assign(a);  /* call assign with NULL pointer\n" +
                "                     dereference a in assign is a\n" +
                "                     Null-Pointer-Dereference issue */\n" +
                "  printf(\"value of b = %d\\n\", b);\n" +
                "  return 0;\n" +
                "}";
        when(githubRepository.getRawFileFromRepository("https://github.com/steveli1840/test_github", fileInfo.getRelativePath(), fileInfo.getVersion(), null)).thenReturn(content);
        Resource fileContentAsResource = githubService.getFileContentAsResource(fileInfo, null);
        Reader reader = new InputStreamReader(fileContentAsResource.getInputStream(), UTF_8);
        String resultContent = FileCopyUtils.copyToString(reader);
        assertEquals(content,resultContent.trim());
    }

    @Test
    void getLatestCommitId_Success() throws AppException {
        String repoUrl = "https://github.com/xxx.git";
        String commitId = "e8bba8b87cdc36d51e0be03e6834282e207dfc92";
        when(githubRepository.getLatestCommitId(repoUrl, null)).thenReturn(commitId);
        String latestCommitId = githubService.getLatestCommitId(repoUrl,null);
        assertEquals(commitId, latestCommitId);
    }
}
