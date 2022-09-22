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
 import com.xcal.api.util.VariableUtil;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang3.StringUtils;
 import org.gitlab4j.api.GitLabApi;
 import org.gitlab4j.api.GitLabApiException;
 import org.gitlab4j.api.models.Commit;
 import org.gitlab4j.api.models.Project;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;

 import java.net.HttpURLConnection;


 @Slf4j
 @Service
 @RequiredArgsConstructor(onConstructor = @__(@Autowired))
 public class GitlabRepository extends GitRepository {

     /**
      * Get gitlab api object
      * @param hostUrl the URL of the GitLab server
      * @param type file storage type
      * @param token  personal access token
      * @return the gitlab api object
      */
     public GitLabApi getGitlabApi(String hostUrl, String type, String token) {
         log.info("[getGitlabApi] hostUrl: {}, type: {}", hostUrl, type);
         log.debug("[getGitlabApi] token: {}", token);

         GitLabApi.ApiVersion apiVersion = GitLabApi.ApiVersion.V4;
         if(StringUtils.equalsIgnoreCase(VariableUtil.GITLAB_V3, type)) {
             apiVersion = GitLabApi.ApiVersion.V3;
         }
         GitLabApi gitLabApi = new GitLabApi(apiVersion, hostUrl, token);
         return gitLabApi;
     }

      /**
       * Get latest repository commit id in a project.
       *
       * @param hostUrl the URL of the GitLab server
       * @param type file storage type
       * @param projectIdOrPath the project in the form of an Integer(ID), String(path), or Project instance
       * @param sha a commit hash or name of a branch or tag
       * @param token personal access token
       * @return the latest commit id for the specified project ID
       * @throws AppException For application exception
       */
      public String getLatestCommitId(String hostUrl, String type, Object projectIdOrPath, String sha, String token) throws AppException {
          log.info("[getLatestCommitId] hostUrl: {}, type: {}, projectIdOrPath: {}, sha: {}", hostUrl, type, projectIdOrPath, sha);
          log.debug("[getLatestCommitId] token: {}", token);

          GitLabApi gitLabApi = getGitlabApi(hostUrl, type, token);
          Commit commit = gitLabApi.getCommitsApi().getOptionalCommit(projectIdOrPath, sha).orElseThrow(
                  () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_GIT_COMMON_COMMIT_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_GIT_COMMON_COMMIT_NOT_FOUND.messageTemplate));
          return commit.getId();
      }

      /**
       * Get a specific project.
       *
       * @param gitLabApi a GitLabApi instance
       * @param projectIdOrPath the project in the form of an Integer(ID), String(path), or Project instance
       * @return the specified project
       * @throws AppException For application exception
       */
      public Project getRepositoryProject(GitLabApi gitLabApi, Object projectIdOrPath) throws AppException {
          log.info("[getRepositoryProject] gitLabApi: {}, projectIdOrPath: {}", gitLabApi, projectIdOrPath);
          if(StringUtils.isBlank(projectIdOrPath.toString())) {
              throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_GIT_COMMON_PROJECTIDORPATH_NOT_BLANK.unifyErrorCode,
                      CommonUtil.formatString("[{}] projectIdOrPath: {}", AppException.ErrorCode.E_API_GIT_COMMON_PROJECTIDORPATH_NOT_BLANK.messageTemplate, projectIdOrPath));
          }
          Project project;
          try {
              project = gitLabApi.getProjectApi().getProject(projectIdOrPath);
          } catch (GitLabApiException e) {
              throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_GIT_GETREPO_FAILED.unifyErrorCode,
                      CommonUtil.formatString("[{}] projectIdOrPath: {}", AppException.ErrorCode.E_API_GIT_GETREPO_FAILED.messageTemplate, projectIdOrPath), e);
          }
          return project;
      }

      /**
       * Get fileContent from repository.
       *
       * @param hostUrl the URL of the GitLab server
       * @param type file storage type
       * @param projectIdOrPath the id, path of the project, or a Project instance holding the project ID or path
       * @param filePath (required) - Full path to the file. Ex. lib/class.rb
       * @param ref (required) - The name of branch, tag or commit
       * @param token version control system personal access token
       * @return repository file content
       * @throws AppException For application exception
       */
      public String getRepositoryFileContent(String hostUrl, String type, Object projectIdOrPath, String filePath, String ref, String token) throws AppException {
          log.info("[getRepositoryFileContent] hostUrl: {}, type: {}, projectIdOrPath: {}, filePath: {}, ref: {}", hostUrl, type, projectIdOrPath, filePath, ref);
          log.debug("[getRepositoryFileContent] token: {}" , token);

          GitLabApi gitLabApi = getGitlabApi(hostUrl, type, token);
          Project project = getRepositoryProject(gitLabApi, projectIdOrPath);
          String fileContent;
          try {
              fileContent = gitLabApi.getRepositoryFileApi().getFile(filePath, project.getId(), ref).getDecodedContentAsString();
          } catch (GitLabApiException e) {
              throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.unifyErrorCode,
                      CommonUtil.formatString("[{}] filePath: {}, ref: {}", AppException.ErrorCode.E_API_GIT_GETRAWFILE_FAILED.messageTemplate, filePath, ref), e);
          }
          return fileContent;
      }
  }
