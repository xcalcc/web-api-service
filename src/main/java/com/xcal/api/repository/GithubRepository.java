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


 import com.xcal.api.exception.AppException;
 import com.xcal.api.util.CommonUtil;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.eclipse.egit.github.core.Repository;
 import org.eclipse.egit.github.core.RepositoryCommit;
 import org.eclipse.egit.github.core.RepositoryId;
 import org.eclipse.egit.github.core.client.GitHubClient;
 import org.eclipse.egit.github.core.client.GitHubRequest;
 import org.eclipse.egit.github.core.service.CommitService;
 import org.eclipse.egit.github.core.service.ContentsService;
 import org.eclipse.egit.github.core.service.RepositoryService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;

 import java.io.*;
 import java.net.HttpURLConnection;
 import java.util.Collections;
 import java.util.List;


 @Slf4j
 @Service
 @Transactional
 @RequiredArgsConstructor(onConstructor = @__(@Autowired))
 public class GithubRepository extends GitRepository {

     private static final String UTF8ENCODING = "utf-8";
     private static final String SEGMENT_REPOS = "/repos";
     private static final String SEGMENT_CONTENTS = "/contents";
     private static final RawGitHubClient client = new RawGitHubClient();


     public String getLatestCommitId(String repoUrl, String token) throws AppException {
         log.info("[getLatestCommitId] repoUrl: {}", repoUrl);
         log.debug("[getLatestCommitId] token: {}", token);

         client.setOAuth2Token(token);
         CommitService commitService = new CommitService(client);
         List<RepositoryCommit> commits;
         try {
             String githubUrl = StringUtils.removeEnd(repoUrl, ".git");
             log.trace("[getLatestCommitId] githubUrl: {}", githubUrl);
             commits = commitService.getCommits(RepositoryId.createFromUrl(githubUrl));
         } catch (IOException e) {
             log.error("[getLatestCommitId] {}: {}", e.getClass(), e.getMessage());
             log.error("[getLatestCommitId] {}", ExceptionUtils.getStackTrace(e));
             throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_GIT_COMMON_GITHUBERROR.unifyErrorCode,
                     CommonUtil.formatString("[{}] repoUrl: {}", AppException.ErrorCode.E_API_GIT_COMMON_GITHUBERROR.messageTemplate, repoUrl), e);
         }
         log.info("[getLatestCommitId] commits size: {}", commits.size());
         if(commits.isEmpty()) {
             throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_GIT_COMMON_COMMIT_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_GIT_COMMON_COMMIT_NOT_FOUND.messageTemplate);
         }
         return commits.get(0).getSha();
     }


     /**
      * Get Raw data from a file from github repository
      *
      * @param repoUrl     github repository url
      * @param path        github file relative path
      * @param ref         commit id
      * @param token personal access Token
      * @return File content in string format
      * @throws AppException For application exception
      */
     public String getRawFileFromRepository(String repoUrl, String path, String ref, String token) throws AppException {
         log.info("[getRawFileFromRepository] repoUrl: {}, path: {}, ref: {}", repoUrl, path, ref);
         log.debug("[getRawFileFromRepository] token: {}", token);

         client.setOAuth2Token(token);
         RawContentsService rService = new RawContentsService(client);
         Repository repo;
         try {
             String githubUrl = StringUtils.removeEnd(repoUrl, ".git");
             log.trace("[getRawFileFromRepository] githubUrl: {}", githubUrl);
             repo = new RepositoryService(client).getRepository(RepositoryId.createFromUrl(githubUrl));
         } catch (IOException e) {
             throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_GIT_GETREPO_FAILED.unifyErrorCode,
                     CommonUtil.formatString("[{}] RepositoryUrl: {}, Path: {}, CommitId: {}, GithubErrorMsg: {}", AppException.ErrorCode.E_API_GIT_GETREPO_FAILED.messageTemplate, repoUrl, path, ref,e.getMessage()));
         }
         log.info("[getRawFileFromRepository] RepositoryUrl: {}, Path: {}, CommitId: {}", repo.getName(), path, ref);
         String fileContent;
         try {
             fileContent = rService.getRawFileAsString(repo, path, ref, UTF8ENCODING);
         } catch (AppException e) {
             throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.unifyErrorCode,
                     CommonUtil.formatString("[{}] RepositoryUrl: {}, Path: {}, CommitId: {}", AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.messageTemplate, repoUrl, path, ref), e);
         }
         return fileContent;
     }


     static class RawGitHubClient extends GitHubClient {
         String accept = "application/vnd.github.v3.raw";

         RawGitHubClient() {
             super();
         }

         public void setAccept(String accept) {
             this.accept = accept;
         }

         @Override
         protected HttpURLConnection configureRequest(final HttpURLConnection request) {
             HttpURLConnection parentRequest = super.configureRequest(request);
             parentRequest.setRequestProperty(HEADER_ACCEPT,
                     accept);
             return parentRequest;
         }

     }

     static class RawContentsService extends ContentsService {

         RawContentsService(GitHubClient client) {
             super(client);
         }

         InputStream getRawFileAsStream(Repository repo, String path, String ref) throws Exception {
             String id = repo.generateId();
             StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
             uri.append('/').append(id);
             uri.append(SEGMENT_CONTENTS);
             if (path != null && path.length() > 0) {
                 if (path.charAt(0) != '/')
                     uri.append('/');
                 uri.append(path);
             }
             GitHubRequest request = createRequest();
             request.setUri(uri);
             if (ref != null && ref.length() > 0)
                 request.setParams(Collections.singletonMap("ref", ref));
             return client.getStream(request);
         }

         String getRawFileAsString(Repository repo, String path, String ref, String encoding) throws AppException {
             try (InputStream rawStream = getRawFileAsStream(repo, path, ref)) {
                 return dumpInputStreamIntoString(rawStream, encoding);
             } catch (Exception e) {
                 throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.unifyErrorCode,
                         CommonUtil.formatString("[{}] RepositoryUrl: {}, Path: {}, CommitId: {}", AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.messageTemplate, repo, path, ref), e);
             }
         }

         String dumpInputStreamIntoString(InputStream f, String encoding) throws AppException {
             ByteArrayOutputStream byteStream;
             BufferedInputStream fileStream;
             byteStream = new ByteArrayOutputStream();
             fileStream = new BufferedInputStream((f));
             int data;
             try {
                 while ((data = fileStream.read()) != -1) {
                     byteStream.write(data);
                 }
             } catch (IOException e) {
                 throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.unifyErrorCode,
                         CommonUtil.formatString("[{}] {}: {} ", AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.messageTemplate, e.getClass(), e.getMessage()), e);
             }
             byte[] raw = byteStream.toByteArray();
             String outputStr;
             try {
                 outputStr = new String(raw, encoding);
             } catch (UnsupportedEncodingException e) {
                 throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.unifyErrorCode,
                         CommonUtil.formatString("[{}] {}: {} ", AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.messageTemplate, e.getClass(), e.getMessage()), e);
             }
             return outputStr;
         }

     }

 }
