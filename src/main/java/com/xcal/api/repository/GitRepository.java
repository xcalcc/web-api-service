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
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.lib.ObjectId;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;

 import java.io.File;
 import java.io.IOException;
 import java.net.HttpURLConnection;


 @Slf4j
 @Service
 @RequiredArgsConstructor(onConstructor = @__(@Autowired))
 public class GitRepository {

      public static final String GIT_REV_STRING = "HEAD";

      /**
       * Clone project to local directory
       *
       * @param httpUrlToRepo the project repository URL of the GitLab server
       * @param directory the local place to save the clone project
       * @return the latest commit id for the specified project
       * @throws AppException For application exception
       */
      public String cloneRepositoryProjectToLocalDirectory(String httpUrlToRepo, String directory) throws AppException {
          log.info("[CloneRepositoryProjectToLocalDirectory] httpUrlToRepo: {}, directory: {}", httpUrlToRepo, directory);
          Git git;
          try {
              git = Git.cloneRepository().setURI(httpUrlToRepo).setDirectory(new File(directory)).call();
          } catch (GitAPIException e) {
              throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_GIT_COMMON_CLONE_FAILED.unifyErrorCode,
                      CommonUtil.formatString("[{}] httpUrlToRepo: {}, directory: {}", AppException.ErrorCode.E_API_GIT_COMMON_CLONE_FAILED.messageTemplate, httpUrlToRepo, directory), e);
          }
          ObjectId latestCommitId;
          try {
              latestCommitId = git.getRepository().resolve(GIT_REV_STRING);
          } catch(IOException e) {
              throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_GIT_COMMON_LAST_COMMITID_NOT_FOUND.unifyErrorCode,
                      CommonUtil.formatString("[{}] httpUrlToRepo: {}", AppException.ErrorCode.E_API_GIT_COMMON_LAST_COMMITID_NOT_FOUND.messageTemplate, httpUrlToRepo), e);
          }
          git.close();
          return latestCommitId.getName();
      }
  }
