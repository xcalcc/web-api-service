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
 import com.xcal.api.util.CommonUtil;
 import lombok.NonNull;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;

 import java.net.HttpURLConnection;
 import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GitlabService {

     @NonNull GitlabRepository gitlabRepository;


    /**
     * Get gitlab project path
     *
     * @param hostUrl gitlab server host url
     * @param gitUrl the project git url
     * @return project path
     * @throws AppException For application exception
     */
     public String getProjectIdOrPath(String hostUrl, String gitUrl) throws AppException {
         log.debug("[getProjectIdOrPath] hostUrl: {}, gitUrl: {}", hostUrl, gitUrl);
         String temp = StringUtils.removeEnd(gitUrl, ".git");
         if(!gitUrl.startsWith(hostUrl)) {
             throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_GIT_COMMON_GITURL_MISMATCH.unifyErrorCode,
                     CommonUtil.formatString("[{}] hostUrl: {}, gitUrl: {}", AppException.ErrorCode.E_API_GIT_COMMON_GITURL_MISMATCH.messageTemplate, hostUrl, gitUrl));
         }
         temp = StringUtils.removeStart(temp, hostUrl);
         String projectIdOrPath = StringUtils.removeStart(temp, "/");
         return projectIdOrPath;
     }

    /**
     * Get latest repository commit id in a project.
     *
     * @param fileStorage fileStorage
     * @param gitUrl the project git url
     * @param sha a commit hash or name of a branch or tag
     * @param token personal access token
     * @return the latest commit id for the specified project
     * @throws AppException For application exception
     */
    public String getLatestCommitId(FileStorage fileStorage, String gitUrl, String sha, String token) throws AppException {
        log.info("[getLatestCommitId] fileStorage: {}, gitUrl: {}, sha: {}", fileStorage, gitUrl, sha);
        log.debug("[getLatestCommitId] token: {}", token);

        String fileStorageHost = fileStorage.getFileStorageHost();
        String projectIdOrPath = getProjectIdOrPath(fileStorageHost, gitUrl);
        return gitlabRepository.getLatestCommitId(fileStorageHost, fileStorage.getFileStorageType().toString(), projectIdOrPath, sha, token);
    }

     /**
      * Clone repository project to local directory
      *
      * @param httpUrlToRepo the project repository URL of the GitLab server
      * @param directory the local place to save the clone project
      * @return the latest commit id for the specified project
      * @throws AppException For application exception
      */
     public String cloneRepositoryProjectToLocalDirectory(String httpUrlToRepo, String directory) throws AppException {
         log.info("[CloneRepositoryProjectToLocalDirectory] httpUrlToRepo: {}, directory: {}", httpUrlToRepo, directory);
         return gitlabRepository.cloneRepositoryProjectToLocalDirectory(httpUrlToRepo, directory);
     }

    /**
     * Get file content in String type with fileInfo
     * @param fileInfo file info
     * @param token version control system personal access token
     * @return File content in String type
     * @throws AppException For application exception
     */
    public String getFileContentAsString(FileInfo fileInfo, String token) throws AppException {
        log.info("[getFileContentAsString] fileInfo: {}", fileInfo);
        log.debug("[getFileContentAsString] token: {}", token);

        if(StringUtils.isBlank(fileInfo.getFileStorageExtraInfo())) {
            log.error("[getFileContentAsString] fileStorageExtraInfo should not be blank. fileInfo, id: {}", fileInfo.getId());
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] fileInfo, id: {}, fileStorageExtraInfo: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, fileInfo.getId(), fileInfo.getFileStorageExtraInfo()));
        }

        Map<String, String> fileStorageExtraInfoMap = CommonUtil.convertStringContentToMap(fileInfo.getFileStorageExtraInfo());
        String gitUrl = fileStorageExtraInfoMap.get(ScanTaskService.GIT_URL);
        String fileStorageHost = fileInfo.getFileStorage().getFileStorageHost();
        String projectIdOrPath = getProjectIdOrPath(fileStorageHost, gitUrl);

        return gitlabRepository.getRepositoryFileContent(fileStorageHost, fileInfo.getFileStorage().getFileStorageType().toString(), projectIdOrPath, fileInfo.getRelativePath(), fileInfo.getVersion(), token);
    }

 }
