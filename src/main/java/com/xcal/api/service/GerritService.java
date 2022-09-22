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
 import com.xcal.api.exception.AppException;
 import com.xcal.api.repository.GerritRepository;
 import com.xcal.api.util.VariableUtil;
 import lombok.NonNull;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;

 import java.util.Map;
 import java.util.Optional;


 @Slf4j
 @Service
 @RequiredArgsConstructor(onConstructor = @__(@Autowired))
 public class GerritService {

     @NonNull GerritRepository gerritRepository;

     /**
      * Get file content in resource type with fileInfo
      *
      * @param fileInfo file info
      * @param username username
      * @param password Gerrit password for the user
      * @return File content in Resource
      * @throws AppException For application exception
      */
     public String getFileContentAsString(FileInfo fileInfo, String projectId, String username, String password) throws AppException {
         log.debug("[getFileContentAsResource] fileInfo: {}, username: {}, blank password: {}", fileInfo, username, (StringUtils.isBlank(password)));
         String repoUrl = fileInfo.getFileStorage().getFileStorageHost();
         GerritRestClient gerritRestClient = this.gerritRepository.getGerritRestClient(repoUrl, username, password);
         String repositoryFileContent = this.gerritRepository.getRepositoryFileContent(gerritRestClient, projectId, fileInfo.getVersion(), fileInfo.getRelativePath());
         log.trace("[getFileContentAsResource] size: {}", repositoryFileContent.length());
         return repositoryFileContent;
     }

     /**
      * Get file content in resource type with fileInfo
      *
      * @param fileInfo file info
      * @param hostUrl gerrit server host url
      * @param username username
      * @param password Gerrit password for the user
      * @return File content in Resource
      * @throws AppException For application exception
      */
     public String getFileContentAsString(FileInfo fileInfo, String hostUrl, String projectId, String username, String password) throws AppException {
         log.debug("[getFileContentAsResource] fileInfo: {}, hostUrl: {}, username: {}, blank password: {}", fileInfo, hostUrl, username, (StringUtils.isBlank(password)));
         GerritRestClient gerritRestClient = this.gerritRepository.getGerritRestClient(hostUrl, username, password);
         String repositoryFileContent = this.gerritRepository.getRepositoryFileContent(gerritRestClient, projectId, fileInfo.getVersion(), fileInfo.getRelativePath());
         log.trace("[getFileContentAsResource] size: {}", repositoryFileContent.length());
         return repositoryFileContent;
     }

     public String getFileContentAsString(FileInfo fileInfo, Map<String, String> attributes) throws AppException {
         log.info("[getFileContentAsResource] fileInfo: {}", fileInfo);
         String gitUrl = attributes.get(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL.name());
         String projectId = gitUrl.substring(gitUrl.lastIndexOf("/")+1);
         String hostUrl = StringUtils.removeEnd(gitUrl, "/"+projectId);
         String username = attributes.get(VariableUtil.ProjectConfigAttributeTypeName.USERNAME.name());
         String password = attributes.get(VariableUtil.ProjectConfigAttributeTypeName.VCS_TOKEN.name());
         return this.getFileContentAsString(fileInfo, hostUrl, projectId, username, password);
     }

     public String getLatestCommitId(String repoUrl, Map<String, String> attributes) throws AppException {
         log.info("[getLatestCommitId] repoUrl: {}", repoUrl);
         String projectId = attributes.get(VariableUtil.ProjectConfigAttributeTypeName.GERRIT_PROJECT_ID.name());
         String username = attributes.get(VariableUtil.ProjectConfigAttributeTypeName.USERNAME.name());
         String password = attributes.get(VariableUtil.ProjectConfigAttributeTypeName.VCS_TOKEN.name());
         String branch = attributes.get(VariableUtil.ProjectConfigAttributeTypeName.BRANCH.name());
         String baselineBranch = attributes.get(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_BRANCH.name());
         return this.getLatestCommitId(repoUrl, projectId, Optional.ofNullable(baselineBranch).orElse(branch), username, password);
     }

     public String getLatestCommitId(String repoUrl, String projectId, String branch, String username, String password) throws AppException {
         log.info("[getLatestCommitId] repoUrl: {}, projectId: {}, branch: {}, username: {}, blank password: {}", repoUrl, projectId, branch, username, (StringUtils.isBlank(password)));
         return this.gerritRepository.getLatestCommitId(repoUrl, projectId, branch, username, password);
     }
 }
