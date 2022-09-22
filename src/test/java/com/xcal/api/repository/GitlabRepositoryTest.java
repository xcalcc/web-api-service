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

package com.xcal.api.repository;

import com.xcal.api.util.VariableUtil;
import org.gitlab4j.api.GitLabApi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitlabRepositoryTest {

    private GitlabRepository gitlabRepository = new GitlabRepository();

    @Test
    void getGitlabApiShouldReturnV4AsDefaultApiVersion() {
        String hostUrl = "https://gitlab.com";
        String type = "";
        String token = "";
        GitLabApi gitLabApi = gitlabRepository.getGitlabApi(hostUrl, type, token);
        assertEquals(GitLabApi.ApiVersion.V4, gitLabApi.getApiVersion());
    }

    @Test
    void getGitlabApiWhenTypeIsGITLABV3ShouldReturnV3AsApiVersion() {
        String hostUrl = "https://gitlab.com";
        String type = VariableUtil.GITLAB_V3;
        String token = "";
        GitLabApi gitLabApi = gitlabRepository.getGitlabApi(hostUrl, type, token);
        assertEquals(GitLabApi.ApiVersion.V3, gitLabApi.getApiVersion());
    }
}
