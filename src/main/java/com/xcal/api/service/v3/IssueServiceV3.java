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

package com.xcal.api.service.v3;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.config.AppProperties;
import com.xcal.api.dao.IssueDao;
import com.xcal.api.dao.IssueFileDao;
import com.xcal.api.dao.IssueGroupDao;
import com.xcal.api.dao.ScanTaskDao;
import com.xcal.api.entity.Issue.Criticality;
import com.xcal.api.entity.Project;
import com.xcal.api.entity.ScanFile;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.entity.User;
import com.xcal.api.entity.v3.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.exception.UnexpectedPrefixException;
import com.xcal.api.model.dto.v3.*;
import com.xcal.api.model.payload.IssueGroupCountResponse;
import com.xcal.api.model.payload.IssueGroupCriticalityCountResponse;
import com.xcal.api.model.payload.SendEmailRequest;
import com.xcal.api.model.payload.v3.AssignIssueGroupRequest;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.model.payload.v3.TopCsvCodeRequest;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.*;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IssueServiceV3 {

    @Value("${app.ui-protocol}")
    private String apiProtocol;

    @Value("${app.ui-host}")
    private String apiHost;

    @Value("${app.ui-port}")
    private String apiPort;

    private final UserService userService;

    private final ProjectService projectService;

    private final ScanTaskService scanTaskService;

    private final ScanFileService scanFileService;

    private final RuleServiceV3 ruleService;

    private final SettingService settingService;

    private final EmailService emailService;

    private final IssueGroupDao issueGroupDao;

    private final IssueDao issueDao;

    private final IssueFileDao issueFileDao;

    private final ScanTaskDao scanTaskDao;

    public Page<IssueGroupDto> searchIssueGroup(
            SearchIssueGroupRequest searchIssueGroupRequest,
            Pageable pageable,
            Locale locale,
            UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[searchIssueGroup] searchIssueGroupRequest: {}, pageable: {}, username: {}",
                searchIssueGroupRequest,
                pageable,
                userPrincipal.getUsername()
        );

        //Filter by issue group id
        if (searchIssueGroupRequest.getIssueGroupId() != null) {
            List<IssueGroupDto> issueGroupDtos = new ArrayList<>();
            try {
                issueGroupDtos.add(this.getIssueGroup(searchIssueGroupRequest.getScanTaskId(), searchIssueGroupRequest.getIssueGroupId(), locale, userPrincipal));
            } catch (Exception ignored) {

            }
            return new PageImpl<>(issueGroupDtos, pageable, issueGroupDtos.size());
        }

        //Prepare projectId if not provided
        if (searchIssueGroupRequest.getProjectId() == null) {
            if (searchIssueGroupRequest.getScanTaskId() != null) {
                ScanTask scanTask = this.scanTaskService.findById(searchIssueGroupRequest.getScanTaskId())
                        .orElseThrow(() -> new AppException(
                                AppException.LEVEL_WARN,
                                AppException.ERROR_CODE_DATA_NOT_FOUND,
                                HttpURLConnection.HTTP_NOT_FOUND,
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                                CommonUtil.formatString(
                                        "[{}] scanTaskId: {}",
                                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                        searchIssueGroupRequest.getScanTaskId()
                                )
                        ));
                searchIssueGroupRequest.setProjectId(scanTask.getProject().getId());
            }
        }

        Project project = this.projectService.findById(searchIssueGroupRequest.getProjectId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                                searchIssueGroupRequest.getProjectId()
                        )
                ));

        this.userService.checkAccessRightOrElseThrow(
                project,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                project.getId()
                        )
                )
        );

        List<String> scanFilePaths = null;
        if ((searchIssueGroupRequest.getScanFileIds() != null) && !searchIssueGroupRequest.getScanFileIds().isEmpty()) {
            List<ScanFile> scanFiles = this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds());
            if ((scanFiles != null) && !scanFiles.isEmpty()) {
                scanFilePaths = scanFiles.stream().map(ScanFile::getProjectRelativePath).collect(Collectors.toList());
            }
        }

        Page<IssueGroup> issueGroups = this.issueGroupDao.searchIssueGroup(
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePaths,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                searchIssueGroupRequest.getSearchValue(),
                pageable
        );
        // create a mutable list
        List<IssueGroup> issueGroupList = new ArrayList<>(issueGroups.getContent());
        for(int i=0; i < issueGroupList.size(); i++) {
            if(StringUtils.equalsIgnoreCase(issueGroupList.get(i).getDsr(), "F")) {
                issueGroupList.set(i, this.fillFixedIssueGroupInfo(issueGroupList.get(i)));
            }
        }

        return new PageImpl<>(
                issueGroupList.stream().map(issueGroup -> IssueServiceV3.convertToV3(issueGroup, null, null)).collect(Collectors.toList()),
                issueGroups.getPageable(),
                issueGroups.getTotalElements()
        );
    }


    public Page<SearchIssueSuggestionDto> searchIssueGroupSuggestion(
            SearchIssueGroupRequest searchIssueGroupRequest,
            Pageable pageable,
            Locale locale,
            UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[searchIssueGroupSuggestion] searchIssueGroupRequest: {}, pageable: {}, username: {}",
                searchIssueGroupRequest,
                pageable,
                userPrincipal.getUsername()
        );

        //Prepare projectId if not provided
        if (searchIssueGroupRequest.getProjectId() == null) {
            if (searchIssueGroupRequest.getScanTaskId() != null) {
                ScanTask scanTask = this.scanTaskService.findById(searchIssueGroupRequest.getScanTaskId())
                        .orElseThrow(() -> new AppException(
                                AppException.LEVEL_WARN,
                                AppException.ERROR_CODE_DATA_NOT_FOUND,
                                HttpURLConnection.HTTP_NOT_FOUND,
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                                CommonUtil.formatString(
                                        "[{}] scanTaskId: {}",
                                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                        searchIssueGroupRequest.getScanTaskId()
                                )
                        ));
                searchIssueGroupRequest.setProjectId(scanTask.getProject().getId());
            }
        }

        Project project = this.projectService.findById(searchIssueGroupRequest.getProjectId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                                searchIssueGroupRequest.getProjectId()
                        )
                ));

        this.userService.checkAccessRightOrElseThrow(
                project,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                project.getId()
                        )
                )
        );

        List<String> scanFilePaths = null;
        if ((searchIssueGroupRequest.getScanFileIds() != null) && !searchIssueGroupRequest.getScanFileIds().isEmpty()) {
            List<ScanFile> scanFiles = this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds());
            if ((scanFiles != null) && !scanFiles.isEmpty()) {
                scanFilePaths = scanFiles.stream().map(ScanFile::getProjectRelativePath).collect(Collectors.toList());
            }
        }

        Page<SearchIssueSuggestionDto> issueGroups = this.issueGroupDao.searchIssueGroupSuggestion(
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePaths,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                searchIssueGroupRequest.getSearchValue(),
                pageable
        );

        //remove placeholder
        issueGroups.forEach(ig->{
            if(ig!=null) {
                ig.setSearchResult(ig.getSearchResult().replaceAll("^(\\$[ht])?/", ""));
            }
        });

        return issueGroups;
    }

    public IssueGroupDto getIssueGroup(
            String issueGroupId,
            Locale locale,
            UserPrincipal userPrincipal
    ) throws AppException {
        return getIssueGroup(null, issueGroupId, locale, userPrincipal);
    }

    public IssueGroup fillFixedIssueGroupInfo(IssueGroup issueGroup) throws AppException {
        // when issue group is fixed issue group, fill missing issue info with occur issue group's info
        if(StringUtils.equalsIgnoreCase(issueGroup.getDsr(), "F")) {
            log.debug("[fillFixedIssueGroupInfo] fixedIssueGroup, id: {}", issueGroup.getId());
            IssueGroup occurIssueGroup = this.issueGroupDao.getIssueGroup(issueGroup.getOccurScanTaskId(), issueGroup.getId())
                    .orElseThrow(() -> new AppException(
                            AppException.LEVEL_ERROR,
                            AppException.ERROR_CODE_DATA_NOT_FOUND,
                            HttpURLConnection.HTTP_NOT_FOUND,
                            AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                            CommonUtil.formatString(
                                    "[{}] issueGroupId: {}",
                                    AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate,
                                    issueGroup.getId()
                            )
                    ));
            // fill fixed issue group info from occur issue group
            issueGroup.setSrcFilePath(occurIssueGroup.getSrcFilePath());
            issueGroup.setSrcRelativePath(occurIssueGroup.getSrcRelativePath());
            issueGroup.setSrcLineNo(occurIssueGroup.getSrcLineNo());
            issueGroup.setSrcColumnNo(occurIssueGroup.getSrcColumnNo());
            issueGroup.setSrcMessageId(occurIssueGroup.getSrcMessageId());
            issueGroup.setSinkFilePath(occurIssueGroup.getSinkFilePath());
            issueGroup.setSinkRelativePath(occurIssueGroup.getSinkRelativePath());
            issueGroup.setSinkLineNo(occurIssueGroup.getSinkLineNo());
            issueGroup.setSinkColumnNo(occurIssueGroup.getSinkColumnNo());
            issueGroup.setSinkMessageId(occurIssueGroup.getSinkMessageId());
            issueGroup.setFunctionName(occurIssueGroup.getFunctionName());
            issueGroup.setVariableName(occurIssueGroup.getVariableName());
            issueGroup.setSeverity(occurIssueGroup.getSeverity());
            issueGroup.setAssigneeId(issueGroup.getAssigneeId()!= null? issueGroup.getAssigneeId():occurIssueGroup.getAssigneeId());
            issueGroup.setAssigneeDisplayName(issueGroup.getAssigneeDisplayName()!= null? issueGroup.getAssigneeDisplayName():occurIssueGroup.getAssigneeDisplayName());
            issueGroup.setAssigneeEmail(issueGroup.getAssigneeEmail() != null? issueGroup.getAssigneeEmail():occurIssueGroup.getAssigneeEmail());
        }
        return issueGroup;
    }

    public IssueGroup getIssueGroupByScanTaskIdAndIssueGroupId(
            UUID scanTaskId,
            String issueGroupId,
            Locale locale,
            UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[getIssueGroupByScanTaskIdAndIssueGroupId] scanTaskId:{}, issueGroupId: {}, username: {}", scanTaskId, issueGroupId, userPrincipal.getUsername());
        IssueGroup issueGroup = this.issueGroupDao.getIssueGroup(scanTaskId, issueGroupId)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] issueGroupId: {}",
                                AppException.ErrorCode.E_API_ISSUE_COMMON_NOT_FOUND.messageTemplate,
                                issueGroupId
                        )
                ));
        IssueGroup finalIssueGroup = issueGroup;
        Project project = this.projectService.findById(issueGroup.getProjectId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                                finalIssueGroup.getProjectId()
                        )
                ));
        this.userService.checkAccessRightOrElseThrow(
                project,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                project.getId()
                        )
                )
        );
        // when issue group is fixed issue group, fill missing issue info with occur issue group's info
        if(StringUtils.equalsIgnoreCase(issueGroup.getDsr(), "F")) {
            issueGroup = fillFixedIssueGroupInfo(issueGroup);
        }
        return issueGroup;
    }

    public IssueGroupDto getIssueGroup(
            UUID scanTaskId,
            String issueGroupId,
            Locale locale,
            UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[getIssueGroup] scanTaskId:{}, issueGroupId: {}, username: {}", scanTaskId, issueGroupId, userPrincipal.getUsername());
        IssueGroup issueGroup = getIssueGroupByScanTaskIdAndIssueGroupId(scanTaskId, issueGroupId, locale, userPrincipal);

        String occurCommitId = null;
        UUID occurScanTaskId = issueGroup.getOccurScanTaskId();
        if (occurScanTaskId != null) {
            occurCommitId = scanTaskDao.getCommitIdByScanTaskId(occurScanTaskId).orElse(null);
        }

        String fixedCommitId = null;
        UUID fixedScanTaskId = issueGroup.getFixedScanTaskId();
        if (fixedScanTaskId != null) {
            fixedCommitId = scanTaskDao.getCommitIdByScanTaskId(fixedScanTaskId).orElse(null);
        }

        return IssueServiceV3.convertToV3(issueGroup, occurCommitId, fixedCommitId);
    }

    public Page<IssueDto> getIssueList(
            UUID scanTaskId,
            String issueGroupId,
            Pageable pageable,
            Locale locale,
            UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[getIssueList] issueGroupId: {}, pageable: {}, username: {}",
                issueGroupId,
                pageable,
                userPrincipal.getUsername()
        );
        IssueGroupDto issueGroupDto = this.getIssueGroup(scanTaskId, issueGroupId, locale, userPrincipal);
        Page<Issue> issues = this.issueDao.findByIssueGroup(scanTaskId, issueGroupDto.getId(), pageable);
        List<IssueFile> issueFiles = this.issueFileDao.getIssueFileList(issueGroupDto.getOccurScanTaskId());
        Map<Integer, String> issueFileMap = issueFiles.stream().collect(Collectors.toMap(IssueFile::getId, IssueFile::getPath));
        return new PageImpl<>(
                issues.stream().map(issue -> convertToV3(issue, issueFileMap))
                        .collect(Collectors.toList()),
                issues.getPageable(),
                issues.getTotalElements()
        );
    }

    public IssueGroupDto assignIssueGroup(
            String issueGroupId,
            UUID userId,
            Locale locale,
            UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[assignIssueGroup] issueGroupId: {}, userId: {}, username: {}",
                issueGroupId,
                userId,
                userPrincipal.getUsername()
        );
        IssueGroupDto issueGroupDto = this.getIssueGroup(issueGroupId, locale, userPrincipal);
        User user = this.userService.findById(userId)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] userId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate,
                                userId
                        )
                ));
        this.issueGroupDao.assignIssueGroupToUser(issueGroupId, user.getId());
        this.sendAssignIssueGroupEmail(issueGroupDto, user, locale, userPrincipal);
        return issueGroupDto;
    }

    public void assignIssueGroupsToUsers(
            AssignIssueGroupRequest assignIssueGroupRequest,
            Locale locale,
            UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[assignIssueGroupsToUsers] assignIssueGroupRequest: {}, username: {}",
                assignIssueGroupRequest,
                userPrincipal.getUsername()
        );
        for (AssignIssueGroupRequest.AssignIssueGroup aig : assignIssueGroupRequest.getAssignIssueGroups()) {
            try {
                this.assignIssueGroup(aig.getIssueGroupId(), aig.getUserId(), locale, userPrincipal);
            } catch (Exception e) {
                log.warn("[assignIssueGroupToUsers] can not assign issue group {} to user {}", aig.getIssueGroupId(), aig.getUserId());
            }
        }
    }

    private void sendAssignIssueGroupEmail(
            IssueGroupDto issueGroupDto,
            User user,
            Locale locale,
            UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[sendAssignIssueGroupEmail] issueGroupId: {}, locale: {}, currentUserName: {}",
                issueGroupDto.getId(),
                locale,
                userPrincipal.getUsername()
        );

        Optional<ScanTask> scanTaskOptional = this.scanTaskService.findById(issueGroupDto.getOccurScanTaskId());
        if (scanTaskOptional.isPresent()) {
            ScanTask scanTask = scanTaskOptional.get();

            RuleInfoDto ruleInfoDto = Optional.ofNullable(this.ruleService.getRuleInfo(issueGroupDto.getRuleCode(), locale))
                    .orElse(RuleInfoDto.builder().ruleCode("").name("").build());

            Map<String, Object> model = new HashMap<>();
            model.put("userDisplayName", user.getDisplayName());
            model.put("assignerDisplayName", userPrincipal.getUser().getDisplayName());
            model.put("projectName", scanTask.getProject().getName());
            model.put("scanStartAt", scanTask.getScanStartAt());
            model.put("issueGroup", issueGroupDto);
            model.put("ruleInfo", ruleInfoDto);
            model.put("uiHost", String.format("%s://%s:%s", this.apiProtocol, this.apiHost, this.apiPort));

            AppProperties.Mail mailSetting = this.settingService.getEmailServerConfiguration();
            SendEmailRequest request = SendEmailRequest.builder()
                    .from(mailSetting.getFrom())
                    .to(user.getEmail())
                    .subject(CommonUtil.formatString(
                            (locale == Locale.SIMPLIFIED_CHINESE) ?
                                    AppProperties.EMAIL_SUBJECT_PATTERN_ASSIGN_ISSUE_ZH_CN :
                                    AppProperties.EMAIL_SUBJECT_PATTERN_ASSIGN_ISSUE,
                            mailSetting.getPrefix(),
                            userPrincipal.getUser().getDisplayName(),
                            scanTask.getProject().getName()
                    ))
                    .templateName((locale == Locale.SIMPLIFIED_CHINESE) ?
                            AppProperties.EMAIL_TEMPLATE_PREFIX_ASSIGN_ISSUE_ZH_CN :
                            AppProperties.EMAIL_TEMPLATE_PREFIX_ASSIGN_ISSUE)
                    .model(model)
                    .build();

            this.emailService.sendTemplateMail(request, userPrincipal.getUsername());
        }
    }

    /***
     *
     * @param searchIssueGroupRequest The request with
     * remark: searchIssueGroupRequest.ruleCodes is for filtering csv codes. If no result for the rules, result with 0 count as value will be given.
     * @param userPrincipal
     * @return
     * @throws AppException
     */
    public IssueGroupCriticalityCountResponse getIssueGroupCriticalityCount(
            SearchIssueGroupRequest searchIssueGroupRequest,
            UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[getIssueGroupCriticalityCount] searchIssueGroupRequest: {}, username: {}",
                searchIssueGroupRequest,
                userPrincipal.getUsername()
        );

        if (searchIssueGroupRequest.getProjectId() == null) {
            if (searchIssueGroupRequest.getScanTaskId() != null) {
                ScanTask scanTask = this.scanTaskService.findById(searchIssueGroupRequest.getScanTaskId())
                        .orElseThrow(() -> new AppException(
                                AppException.LEVEL_WARN,
                                AppException.ERROR_CODE_DATA_NOT_FOUND,
                                HttpURLConnection.HTTP_NOT_FOUND,
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                                CommonUtil.formatString(
                                        "[{}] scanTaskId: {}",
                                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                        searchIssueGroupRequest.getScanTaskId()
                                )
                        ));
                searchIssueGroupRequest.setProjectId(scanTask.getProject().getId());
            }
        }

        Project project = this.projectService.findById(searchIssueGroupRequest.getProjectId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                                searchIssueGroupRequest.getProjectId()
                        )
                ));

        this.userService.checkAccessRightOrElseThrow(
                project,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                project.getId()
                        )
                )
        );

        List<String> scanFilePaths = null;
        if ((searchIssueGroupRequest.getScanFileIds() != null) && !searchIssueGroupRequest.getScanFileIds().isEmpty()) {
            List<ScanFile> scanFiles = this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds());
            if ((scanFiles != null) && !scanFiles.isEmpty()) {
                scanFilePaths = scanFiles.stream().map(ScanFile::getProjectRelativePath).collect(Collectors.toList());
            }
        }

        List<IssueGroupCountRow> issueGroupCriticalityCountRowList = this.issueGroupDao.getIssueGroupCountWithFilter(
                IssueGroupDao.FILTER_CATEGORY_CRITICALITY,
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePaths,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                null,
                searchIssueGroupRequest.getSearchValue()
        );

        Map<String, Map<String, String>> criticalityRuleCodeMap = new HashMap<>();
        for (IssueGroupCountRow row : issueGroupCriticalityCountRowList) {
            if (!criticalityRuleCodeMap.containsKey(row.getCriticality())) {
                criticalityRuleCodeMap.put(row.getCriticality(), new HashMap<>());
            }
            criticalityRuleCodeMap.get(row.getCriticality()).put(row.getRuleCode(), row.getCount());
        }

        // Fill in key and values for rules with no result
        fillDefaultValueForEmptyMap(searchIssueGroupRequest, criticalityRuleCodeMap);

        return IssueGroupCriticalityCountResponse.builder()
                .criticalityRuleCodeCountMap(criticalityRuleCodeMap)
                .build();
    }

    /***
     * Fill in key and default value from searchIssueGroupRequest if no data is in the map.
     * @param searchIssueGroupRequest
     * @param criticalityRuleCodeMap This map will be filled in the default value for selected rule filter
     */
    void fillDefaultValueForEmptyMap(SearchIssueGroupRequest searchIssueGroupRequest, Map<String, Map<String, String>> criticalityRuleCodeMap) {
        log.info(
                "[fillDefaultValueForEmptyMap] searchIssueGroupRequest: {}, criticalityRuleCodeMap.size: {}",
                searchIssueGroupRequest,
                criticalityRuleCodeMap!=null?criticalityRuleCodeMap.size():null
        );

        if(searchIssueGroupRequest==null || searchIssueGroupRequest.getRuleCodes()==null || criticalityRuleCodeMap==null){
            return;
        }

        List<SearchIssueGroupRequest.RuleCode> ruleCodesFilter = searchIssueGroupRequest.getRuleCodes();
        for( SearchIssueGroupRequest.RuleCode ruleCodeFilter: ruleCodesFilter){
            String responseCriticalityKey = Criticality.getByShortName(ruleCodeFilter.getCriticality()).longName;
            if (!criticalityRuleCodeMap.containsKey(responseCriticalityKey)) {
                criticalityRuleCodeMap.put(responseCriticalityKey, new HashMap<>());
            }

            Map<String, String> ruleCodeMap= criticalityRuleCodeMap.get(responseCriticalityKey);
            if(!ruleCodeMap.containsKey(ruleCodeFilter.getCsvCode())){
                ruleCodeMap.put(ruleCodeFilter.getCsvCode(), "0");
            }
        }
    }

    public IssueGroupCountResponse getIssueGroupCount(
            SearchIssueGroupRequest searchIssueGroupRequest,
            UserPrincipal userPrincipal
    ) throws AppException {
        log.info(
                "[getIssueGroupCount] searchIssueGroupRequest: {}, username: {}",
                searchIssueGroupRequest,
                userPrincipal.getUsername()
        );

        if (searchIssueGroupRequest.getProjectId() == null) {
            if (searchIssueGroupRequest.getScanTaskId() != null) {
                ScanTask scanTask = this.scanTaskService.findById(searchIssueGroupRequest.getScanTaskId())
                        .orElseThrow(() -> new AppException(
                                AppException.LEVEL_WARN,
                                AppException.ERROR_CODE_DATA_NOT_FOUND,
                                HttpURLConnection.HTTP_NOT_FOUND,
                                AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                                CommonUtil.formatString(
                                        "[{}] scanTaskId: {}",
                                        AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate,
                                        searchIssueGroupRequest.getScanTaskId()
                                )
                        ));
                searchIssueGroupRequest.setProjectId(scanTask.getProject().getId());
            }
        }

        Project project = this.projectService.findById(searchIssueGroupRequest.getProjectId())
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_WARN,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                                searchIssueGroupRequest.getProjectId()
                        )
                ));

        this.userService.checkAccessRightOrElseThrow(
                project,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                project.getId()
                        )
                )
        );

        List<String> scanFilePaths = null;
        if ((searchIssueGroupRequest.getScanFileIds() != null) && !searchIssueGroupRequest.getScanFileIds().isEmpty()) {
            List<ScanFile> scanFiles = this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds());
            if ((scanFiles != null) && !scanFiles.isEmpty()) {
                scanFilePaths = scanFiles.stream().map(ScanFile::getProjectRelativePath).collect(Collectors.toList());
            }
        }

        List<IssueGroupCountRow> issueGroupCertaintyCountRowList = this.issueGroupDao.getIssueGroupCountWithFilter(
                IssueGroupDao.FILTER_CATEGORY_CERTAINTY,
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePaths,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                null,
                searchIssueGroupRequest.getSearchValue()

        );

        //compute and make rule code count
        List<IssueGroupCountRow> issueGroupCountRowList = issueGroupDao.getIssueGroupCountWithFilter(
                IssueGroupDao.FILTER_CATEGORY_RULE_CODE,
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePaths,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                null,
                searchIssueGroupRequest.getSearchValue()
        );

        return IssueGroupCountResponse.builder()
                .certaintyCountMap(issueGroupCertaintyCountRowList.stream()
                        .collect(Collectors.toMap(IssueGroupCountRow::getCertainty, IssueGroupCountRow::getCount)))
                .ruleCodeCountMap(issueGroupCountRowList.stream()
                        .collect(Collectors.toMap(IssueGroupCountRow::getRuleCode, IssueGroupCountRow::getCount)))
                .build();
    }

    public List<IssueGroupCountRow> getTopCsvCodes(
            TopCsvCodeRequest topCsvCodeRequest
    ) {
        log.info("[getTopCsvCodes] topCsvCodeRequest: {}", topCsvCodeRequest);
        return issueGroupDao.getTopCsvCodes(topCsvCodeRequest);
    }


    public IssueGroupCountDsrRow getTopCsvCodesDsr(
            TopCsvCodeRequest topCsvCodeRequest
    ) {
        log.info("[getTopCsvCodesDsr] topCsvCodeRequest: {}", topCsvCodeRequest);

        IssueGroupCountDsrRow issueGroupCountDsrRow = IssueGroupCountDsrRow.builder()
                .build();

        topCsvCodeRequest.setDsrType("N");
        issueGroupCountDsrRow.setN(issueGroupDao.getTopCsvCodes(topCsvCodeRequest));

        topCsvCodeRequest.setDsrType("E");
        issueGroupCountDsrRow.setE(issueGroupDao.getTopCsvCodes(topCsvCodeRequest));

        topCsvCodeRequest.setDsrType("F");
        issueGroupCountDsrRow.setF(issueGroupDao.getTopCsvCodes(topCsvCodeRequest));

        return issueGroupCountDsrRow;
    }

    private static IssueGroupDto convertToV3(IssueGroup issueGroup, String occurCommitId, String fixedCommitId) {
        if (issueGroup != null) {
            String srcPathCategory = "";
            try {
                if (issueGroup.getSrcFilePath() != null) {
                    srcPathCategory = PathUtil.getPathCategory(issueGroup.getSrcFilePath());
                }
            } catch (UnexpectedPrefixException e) {
                log.warn("[convertToV3]Unexpected prefix while converting Issue Group: {}", e.getMessage());
            }

            String sinkPathCategory = "";
            try {
                if (issueGroup.getSinkFilePath() != null) {
                    sinkPathCategory = PathUtil.getPathCategory(issueGroup.getSinkFilePath());
                }
            } catch (UnexpectedPrefixException e) {
                log.warn("[convertToV3]Unexpected prefix while converting Issue Group: {}", e.getMessage());
            }

            return IssueGroupDto.builder()
                    .id(issueGroup.getId())
                    .projectId(issueGroup.getProjectId())
                    .occurScanTaskId(issueGroup.getOccurScanTaskId())
                    .occurCommitId(occurCommitId)
                    .fixedScanTaskId(issueGroup.getFixedScanTaskId())
                    .fixedCommitId(fixedCommitId)
                    .ruleCode(issueGroup.getRuleCode())
                    .ruleSet(issueGroup.getRuleSet())
                    .srcPathCategory(srcPathCategory)
                    .srcFilePath(issueGroup.getSrcFilePath())
                    .srcRelativePath(Optional.ofNullable(issueGroup.getSrcRelativePath())
                            .map(path -> path.replaceAll("^(\\$[ht])?/", ""))
                            .orElse(null))
                    .srcLineNo(issueGroup.getSrcLineNo())
                    .srcColumnNo(issueGroup.getSrcColumnNo())
                    .srcMessageId(issueGroup.getSrcMessageId())
                    .sinkPathCategory(sinkPathCategory)
                    .sinkFilePath(issueGroup.getSinkFilePath())
                    .sinkRelativePath(Optional.ofNullable(issueGroup.getSinkRelativePath())
                            .map(path -> path.replaceAll("^(\\$[ht])?/", ""))
                            .orElse(null))
                    .sinkLineNo(issueGroup.getSinkLineNo())
                    .sinkColumnNo(issueGroup.getSinkColumnNo())
                    .sinkMessageId(issueGroup.getSinkMessageId())
                    .functionName(issueGroup.getFunctionName())
                    .variableName(issueGroup.getVariableName())
                    .severity(issueGroup.getSeverity())
                    .likelihood(issueGroup.getLikelihood())
                    .remediationCost(issueGroup.getRemediationCost())
                    .complexity(issueGroup.getComplexity())
                    .priority(issueGroup.getPriority())
                    .criticality(issueGroup.getCriticalityLevel())
                    .category(issueGroup.getCategory())
                    .certainty(issueGroup.getCertainty())
                    .issueCount(issueGroup.getIssueCount())
                    .avgTraceCount(issueGroup.getAvgTraceCount())
                    .status(issueGroup.getStatus())
                    .dsr(issueGroup.getDsr())
                    .occurTime(issueGroup.getOccurTime())
                    .fixedTime(issueGroup.getFixedTime())
                    .assigneeId(issueGroup.getAssigneeId())
                    .assigneeDisplayName(issueGroup.getAssigneeDisplayName())
                    .assigneeEmail(issueGroup.getAssigneeEmail())
                    .build();
        }
        return null;
    }


    /***
     * Take an issue and issueFileMap to convert to a IssueDto
     * @param issue An issue entity
     * @param issueFileMap A map with issue_file.id as key and file path as value
     * @return a dto of issue
     */
    public static IssueDto convertToV3(Issue issue, Map<Integer, String> issueFileMap) {
        if (issue != null) {
            try {
                ObjectMapper om = new ObjectMapper();
                JavaType type = om.getTypeFactory().constructParametricType(List.class, Trace.class);

                //prepare data
                List<Trace> tracePath = om.readValue(issue.getTracePath(), type);
                List<TraceDto> traceDtos = tracePath.stream().map(node -> {
                    String pathCategory = "";//default value
                    try {
                        pathCategory = PathUtil.getPathCategory(issueFileMap.getOrDefault(node.getFileId(), ""));
                    } catch (UnexpectedPrefixException e) {
                        log.warn("UnexpectedPrefixException: {}", e.getMessage());
                    }
                    return TraceDto.builder()
                            .pathCategory(pathCategory)
                            .file(issueFileMap.getOrDefault(node.getFileId(), "").replaceAll("^(\\$[ht])?/", ""))
                            .lineNo(node.getLineNo())
                            .columnNo(node.getColumnNo())
                            .msgId(node.getMsgId())
                            .build();
                }).collect(Collectors.toList());

                //build IssueDto
                return IssueDto.builder()
                        .id(issue.getId())
                        .issueGroupId(issue.getIssueGroupId())
                        .certainty(issue.getCertainty())
                        .traceCount(issue.getTraceCount())
                        .tracePath(traceDtos)
                        .status(issue.getStatus())
                        .dsr(issue.getDsr())
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
