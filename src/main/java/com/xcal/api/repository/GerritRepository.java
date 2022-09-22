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


 import com.google.gerrit.extensions.restapi.BinaryResult;
 import com.google.gerrit.extensions.restapi.RestApiException;
 import com.urswolfer.gerrit.client.rest.GerritAuthData;
 import com.urswolfer.gerrit.client.rest.GerritRestApi;
 import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
 import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
 import com.urswolfer.gerrit.client.rest.http.HttpRequestExecutor;
 import com.urswolfer.gerrit.client.rest.http.HttpStatusException;
 import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
 import com.xcal.api.exception.AppException;
 import com.xcal.api.util.CommonUtil;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.http.HttpResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Base64Utils;

 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 import java.nio.charset.StandardCharsets;


 @Slf4j
 @Service
 @Transactional
 @RequiredArgsConstructor(onConstructor = @__(@Autowired))
 public class GerritRepository extends GitRepository {

     public GerritRestClient getGerritRestClient(String repoUrl, String username, String password) {
         GerritRestClient gerritRestClient;
         GerritAuthData.Basic authData = this.getAuthData(repoUrl, username, password);
         gerritRestClient = this.getGerritRestClient(authData);
         log.trace("[getGerritRestClient] getGerritApi created");
         return gerritRestClient;
     }

     private GerritRestClient getGerritRestClient(GerritAuthData.Basic authData) {
         return new GerritRestClient(authData, new HttpRequestExecutor());
     }

     private GerritAuthData.Basic getAuthData(String repoUrl, String username, String password) {
         GerritAuthData.Basic authData;
         if (StringUtils.isAllBlank(username, password)) {
             authData = new GerritAuthData.Basic(repoUrl);
         } else {
             authData = new GerritAuthData.Basic(repoUrl, username, password);
         }
         return authData;
     }

     public String getRepositoryFileContent(GerritRestClient gerritRestClient, String projectName, String commitId, String fileId) throws AppException {
         String result;
         try {
             String request = CommonUtil.formatString("/projects/{}/commits/{}/files/{}/content", projectName, commitId, URLEncoder.encode(fileId, StandardCharsets.UTF_8.name()));
             HttpResponse response = gerritRestClient.request(request, null, GerritRestClient.HttpVerb.GET);
             BinaryResult binaryResult = BinaryResultUtils.createBinaryResult(response);
             if (binaryResult.isBase64()) {
                 byte[] content = Base64Utils.decodeFromString(binaryResult.asString());
                 result = new String(content, StandardCharsets.UTF_8);
             } else {
                 result = binaryResult.asString();
             }
         } catch (HttpStatusException | IOException e) {
             throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.unifyErrorCode,
                     CommonUtil.formatString("[{}] projectName: {}, commitId: {}, fileId: {}", AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.messageTemplate,
                             projectName, commitId, fileId), e);
         }
         return result;
     }

     public String getLatestCommitId(String repoUrl, String projectId, String branch, String username, String password) throws AppException {
         String commitId;
         log.info("[getLatestCommitId] repoUrl: {}, projectId: {}, branch: {}, username: {}, blank password: {}", repoUrl, projectId, branch, username, (StringUtils.isBlank(password)));
         GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
         GerritRestApi gerritRestApi = gerritRestApiFactory.create(this.getAuthData(repoUrl, username, password));
         try {
             commitId = gerritRestApi.projects().name(projectId).branch(branch).get().revision;
         } catch (RestApiException e) {
             throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_GIT_COMMON_COMMIT_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_GIT_COMMON_COMMIT_NOT_FOUND.messageTemplate);
         }
         return commitId;
     }
 }
