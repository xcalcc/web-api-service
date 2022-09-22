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
 import com.xcal.api.repository.GithubRepository;
 import com.xcal.api.util.CommonUtil;
 import lombok.NonNull;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.io.ByteArrayResource;
 import org.springframework.core.io.Resource;
 import org.springframework.stereotype.Service;

 import java.net.HttpURLConnection;
 import java.util.Map;


 @Slf4j
 @Service
 @RequiredArgsConstructor(onConstructor = @__(@Autowired))
 public class GithubService {


     @NonNull GithubRepository githubRepository;


     /**
      * Get latest repository commit id in a project.
      *
      * @param httpUrlToRepo the project repository URL of the GitHub server
      * @param token personal access token
      * @return the latest commit id for the specified project
      * @throws AppException For application exception
      */
     public String getLatestCommitId(String httpUrlToRepo, String token) throws AppException {
         log.info("[getLatestCommitId] httpUrlToRepo: {}", httpUrlToRepo);
         log.debug("[getLatestCommitId] token: {}", token);
         return githubRepository.getLatestCommitId(httpUrlToRepo, token);
     }


     /**
      * Get latest repository commit id in a project.
      *
      * @param httpUrlToRepo the project repository URL of the GitHub server
      * @param directory     the local place to save the clone project
      * @return the latest commit id for the specified project
      * @throws AppException For application exception
      */
     public String cloneRepositoryProjectToLocalDirectory(String httpUrlToRepo, String directory) throws AppException {
         log.info("[CloneRepositoryProjectToLocalDirectory] httpUrlToRepo: {}, directory: {}", httpUrlToRepo, directory);
         return githubRepository.cloneRepositoryProjectToLocalDirectory(httpUrlToRepo, directory);
     }


     /**
      * Get github projectUrl.
      *
      * @param fileStorageExtraInfo in fileStorage
      * @return github projectUrl
      * @throws AppException For application exception
      */
     public String getGithubProjectUrl(String fileStorageExtraInfo) throws AppException {
         log.info("[getGithubProjectUrl] fileStorageExtraInfo: {}", fileStorageExtraInfo);
         Map<String, String> fileStorageExtraInfoMap = CommonUtil.convertStringContentToMap(fileStorageExtraInfo);
         String githubProjectUrl = fileStorageExtraInfoMap.get(ScanTaskService.GIT_URL);
         if (StringUtils.isBlank(githubProjectUrl)) {
             throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_GIT_GITHUBPROJECTURL_NOT_BLANK.unifyErrorCode,
                     CommonUtil.formatString("[{}] fileStorageExtraInfo: {}", AppException.ErrorCode.E_API_GIT_GITHUBPROJECTURL_NOT_BLANK.messageTemplate, fileStorageExtraInfo));
         }
         return githubProjectUrl;
     }

     /**
      * Get file content in resource type with fileInfo
      *
      * @param fileInfo file info
      * @param accessToken personal access token
      * @return File content in Resource
      * @throws AppException For application exception
      */
     public Resource getFileContentAsResource(FileInfo fileInfo, String accessToken) throws AppException {
         log.info("[getFileContentAsResource] fileInfo: {}, accessToken: {}", fileInfo, accessToken);
         String githubProjectUrl = getGithubProjectUrl(fileInfo.getFileStorageExtraInfo());
         String repositoryFileContent = githubRepository.getRawFileFromRepository(githubProjectUrl, fileInfo.getRelativePath(), fileInfo.getVersion(), accessToken);
         Resource resource = null;
         if (StringUtils.isNotBlank(repositoryFileContent)) {
             resource = new ByteArrayResource(repositoryFileContent.getBytes());
         }
         return resource;
     }

     /**
      * Get file content in String type with fileInfo
      *
      * @param fileInfo file info
      * @param accessToken personal access token
      * @return File content in String type
      * @throws AppException For application exception
      */
     public String getFileContentAsString(FileInfo fileInfo, String accessToken) throws AppException {
         log.info("[getFileContentAsString] fileInfo: {}, accessToken: {}", fileInfo, accessToken);
         String githubProjectUrl = getGithubProjectUrl(fileInfo.getFileStorageExtraInfo());
         return githubRepository.getRawFileFromRepository(githubProjectUrl, fileInfo.getRelativePath(), fileInfo.getVersion(), accessToken);
     }

 }
