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

import com.xcal.api.config.AppProperties;
import com.xcal.api.dao.*;
import com.xcal.api.entity.*;
import com.xcal.api.entity.v3.Issue;
import com.xcal.api.entity.v3.IssueGroup;
import com.xcal.api.entity.v3.IssueGroupCountDsrRow;
import com.xcal.api.entity.v3.IssueGroupCountRow;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.ScanResultDto;
import com.xcal.api.model.dto.v3.IssueDto;
import com.xcal.api.model.dto.v3.IssueGroupDto;
import com.xcal.api.model.dto.v3.TraceDto;
import com.xcal.api.model.payload.IssueGroupCountResponse;
import com.xcal.api.model.payload.IssueGroupCriticalityCountResponse;
import com.xcal.api.model.payload.v3.AssignIssueGroupRequest;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.model.payload.v3.TopCsvCodeRequest;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.*;
import com.xcal.api.util.CommonUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.net.HttpURLConnection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class IssueServiceV3Test {

    private UserService userService;

    private ProjectService projectService;

    private ScanTaskService scanTaskService;

    private ScanFileService scanFileService;

    private RuleServiceV3 ruleService;

    private SettingService settingService;

    private EmailService emailService;

    private IssueGroupDao issueGroupDao;

    private IssueDao issueDao;

    private IssueFileDao issueFileDao;

    private IssueServiceV3 issueService;

    private ScanTaskDao scanTaskDao;

    private IssueValidationDao issueValidationDao;

    private Project project;

    private ScanTask scanTask;

    private IssueGroup issueGroup;

    private Issue issue;

    private ScanFile scanFile;

    @BeforeEach
    void setup() {
        this.userService = mock(UserService.class);
        this.projectService = mock(ProjectService.class);
        this.scanTaskService = mock(ScanTaskService.class);
        this.scanFileService = mock(ScanFileService.class);
        this.ruleService = mock(RuleServiceV3.class);
        this.settingService = mock(SettingService.class);
        this.emailService = mock(EmailService.class);
        this.issueGroupDao = mock(IssueGroupDao.class);
        this.issueDao = mock(IssueDao.class);
        this.issueFileDao = mock(IssueFileDao.class);
        this.scanTaskDao = mock(ScanTaskDao.class);
        this.issueValidationDao = mock(IssueValidationDao.class);
        this.issueService = new IssueServiceV3(
                this.userService,
                this.projectService,
                this.scanTaskService,
                this.scanFileService,
                this.ruleService,
                this.settingService,
                this.emailService,
                this.issueGroupDao,
                this.issueDao,
                this.issueFileDao,
                this.scanTaskDao,
                this.issueValidationDao
        );
        this.project = Project.builder().id(UUID.randomUUID()).build();
        this.scanTask = ScanTask.builder().id(UUID.randomUUID()).project(this.project).build();
        this.issueGroup = IssueGroup.builder().id(UUID.randomUUID().toString())
                .id("AAAAA00001")
                .projectId(this.project.getId())
                .occurScanTaskId(this.scanTask.getId())
                .srcLineNo(0)
                .sinkLineNo(0)
                .build();
        this.issue = Issue.builder()
                .id(UUID.randomUUID())
                .issueGroupId(this.issueGroup.getId())
                .tracePath("[{\"fid\":10,\"ln\":102,\"mid\":17,\"cn\":0},{\"fid\":20,\"ln\":165,\"mid\":16,\"cn\":0}]")
                .build();
        this.scanFile = ScanFile.builder()
                .id(UUID.randomUUID())
                .projectRelativePath("/")
                .build();
    }

    @Test
    void searchIssueGroup_EmptyRequest_ThrowException() throws AppException {
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder().build();
        Pageable pageable = PageRequest.of(0, 20);
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.projectService.findById(any())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> this.issueService.searchIssueGroup(searchIssueGroupRequest, pageable, locale, userPrincipal));
    }

    @Test
    void searchIssueGroup_WithIssueGroupId_Success() throws AppException {
        IssueServiceV3 issueServiceV3Spy = spy(issueService);
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .scanTaskId(UUID.randomUUID())
                .issueGroupId(this.issueGroup.getId())
                .build();
        Pageable pageable = PageRequest.of(0, 20);
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.issueGroupDao.getIssueGroup(any(UUID.class), anyString())).thenReturn(Optional.of(this.issueGroup));
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        doReturn(IssueGroupDto.builder()
                .build()).when(issueServiceV3Spy).getIssueGroup(any(UUID.class),anyString(), any(Locale.class), any(UserPrincipal.class));
        Page<IssueGroupDto> issueGroupDtos = issueServiceV3Spy.searchIssueGroup(searchIssueGroupRequest, pageable, locale, userPrincipal);
        assertEquals(1, issueGroupDtos.getTotalElements());
    }

    @Test
    void searchIssueGroup_InvalidProject_ThrowException() throws AppException {
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(this.project.getId())
                .build();
        Pageable pageable = PageRequest.of(0, 20);
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> this.issueService.searchIssueGroup(searchIssueGroupRequest, pageable, locale, userPrincipal));
    }

    @Test
    void searchIssueGroup_InvalidScanTask_ThrowException() throws AppException {
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .scanTaskId(this.scanTask.getId())
                .build();
        Pageable pageable = PageRequest.of(0, 20);
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.scanTaskService.findById(this.scanTask.getId())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> this.issueService.searchIssueGroup(searchIssueGroupRequest, pageable, locale, userPrincipal));
    }

    @Test
    void searchIssueGroup_Success() throws AppException {
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(this.project.getId())
                .scanFileIds(Collections.singletonList(this.scanFile.getId()))
                .build();
        Pageable pageable = PageRequest.of(0, 20);
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        when(this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds())).thenReturn(Collections.singletonList(this.scanFile));
        when(this.issueGroupDao.searchIssueGroup(
                this.project.getId(),
                null,
                null,
                null,
                Collections.singletonList("/"),
                null,
                null,
                null,
                null,
                null,
                null,
                pageable)
        ).thenReturn(new PageImpl<>(
                Collections.singletonList(this.issueGroup),
                pageable,
                1
        ));

        Page<IssueGroupDto> issueGroupDtos = this.issueService.searchIssueGroup(searchIssueGroupRequest, pageable, locale, userPrincipal);
        assertEquals(1, issueGroupDtos.getTotalElements());
    }

    @Test
    void searchIssueGroup_InsufficientPrivilege_ThrowException() throws AppException {
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(this.project.getId())
                .scanFileIds(Collections.singletonList(this.scanFile.getId()))
                .build();
        Pageable pageable = PageRequest.of(0, 20);
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        when(this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds())).thenReturn(Collections.singletonList(this.scanFile));
        when(this.issueGroupDao.searchIssueGroup(
                this.project.getId(),
                null,
                null,
                null,
                Collections.singletonList("/"),
                null,
                null,
                null,
                null,
                null,
                null,
                pageable)
        ).thenReturn(new PageImpl<>(
                Collections.singletonList(this.issueGroup),
                pageable,
                1
        ));
        doThrow(new AppException(
                AppException.LEVEL_ERROR,
                AppException.ERROR_CODE_UNAUTHORIZED,
                HttpURLConnection.HTTP_FORBIDDEN,
                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString(
                        "[{}] projectId: {}",
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                        project.getId()
                )
        )).when(userService).checkAccessRightOrElseThrow(eq(project), any(), anyBoolean(), any());
        assertThrows(AppException.class, () -> this.issueService.searchIssueGroup(searchIssueGroupRequest, pageable, locale, userPrincipal));
    }

    @Test
    void assignIssueGroup_Success() throws AppException {
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(this.project.getId())
                .scanFileIds(Collections.singletonList(this.scanFile.getId()))
                .build();
        Pageable pageable = PageRequest.of(0, 20);
        Locale locale = Locale.ENGLISH;
        UserGroup userGroup = UserGroup.builder().groupType(UserGroup.Type.ROLE).groupName("admin").build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("test")
                .password("test")
                .displayName("test")
                .email("test@test.com")
                .userGroups(Collections.singletonList(userGroup))
                .build();
        UserPrincipal normalUserPrincipal = UserPrincipal.create(user);
        when(this.userService.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds())).thenReturn(Collections.singletonList(this.scanFile));
        when(this.issueGroupDao.searchIssueGroup(
                this.project.getId(),
                null,
                null,
                null,
                Collections.singletonList("/"),
                null,
                null,
                null,
                null,
                null,
                null,
                pageable)
        ).thenReturn(new PageImpl<>(
                Collections.singletonList(this.issueGroup),
                pageable,
                1
        ));
        when(issueGroupDao.getIssueGroup(any(), anyString())).thenReturn(Optional.of(issueGroup));
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        when(scanTaskService.findById(any(UUID.class))).thenReturn(Optional.of(scanTask));
        when(this.settingService.getEmailServerConfiguration()).thenReturn(AppProperties.Mail.builder().from("from XXX").prefix("[prefix]").build());

        IssueGroupDto issueGroupDto = this.issueService.assignIssueGroup("", UUID.randomUUID(), locale, normalUserPrincipal);
        assertEquals(issueGroup.getId(), issueGroupDto.getId());
        assertEquals(project.getId(), issueGroupDto.getProjectId());
        assertEquals(scanTask.getId(), issueGroupDto.getOccurScanTaskId());
    }

    @Test
    void getIssueGroup_projectNotExist_ThrowAppException() throws AppException {
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.issueGroupDao.getIssueGroup(any(), anyString())).thenReturn(Optional.empty());
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> this.issueService.getIssueGroup(UUID.randomUUID(),this.issueGroup.getId(), locale, userPrincipal));
    }

    @Test
    void getIssueGroup_InvalidIssueGroupId_ThrowException() throws AppException {
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.issueGroupDao.getIssueGroup(any(UUID.class), anyString())).thenReturn(Optional.empty());
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        assertThrows(AppException.class, () -> this.issueService.getIssueGroup(UUID.randomUUID(),this.issueGroup.getId(), locale, userPrincipal));
    }

    @Test
    void getIssueGroup_Success() throws AppException {
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.issueGroupDao.getIssueGroup(any(UUID.class),  anyString())).thenReturn(Optional.of(this.issueGroup));
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        IssueGroupDto issueGroupDto = this.issueService.getIssueGroup(UUID.randomUUID(),this.issueGroup.getId(), locale, userPrincipal);
        assertEquals(issueGroupDto.getId(), this.issueGroup.getId());
    }

    @Test
    void getIssueList_InvalidIssueGroupId_Success() throws AppException {
        Pageable pageable = PageRequest.of(0, 20);
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.issueGroupDao.getIssueGroup(any(UUID.class), anyString())).thenReturn(Optional.empty());
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        when(this.issueDao.findByIssueGroup(any(UUID.class), anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(
                new ArrayList<>(),
                pageable,
                0
        ));
        when(this.issueFileDao.getIssueFileList(this.issueGroup.getOccurScanTaskId())).thenReturn(new ArrayList<>());
        assertThrows(AppException.class, () -> this.issueService.getIssueList(UUID.randomUUID(), this.issueGroup.getId(), pageable, locale, userPrincipal));
    }

    @Test
    void getIssueList_Success() throws AppException {
        Pageable pageable = PageRequest.of(0, 20);
        Locale locale = Locale.ENGLISH;
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.issueGroupDao.getIssueGroup(any(UUID.class), anyString())).thenReturn(Optional.of(this.issueGroup));
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        when(this.issueDao.findByIssueGroup(any(UUID.class), anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(
                Collections.singletonList(this.issue),
                pageable,
                1
        ));
        when(this.issueFileDao.getIssueFileList(this.issueGroup.getOccurScanTaskId())).thenReturn(new ArrayList<>());

        Page<IssueDto> issueDtos = this.issueService.getIssueList(UUID.randomUUID(), this.issueGroup.getId(), pageable, locale, userPrincipal);
        assertEquals(1, issueDtos.getTotalElements());
    }

    @Test
    void getIssueGroupCount_searchByProjectId_getNoResult() throws AppException {
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(this.project.getId())
                .build();
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        when(this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds())).thenReturn(Collections.singletonList(this.scanFile));
        when(issueGroupDao.getIssueGroupCountWithFilter(
                eq(IssueGroupDao.FILTER_CATEGORY_CERTAINTY),
                any(UUID.class),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(new ArrayList<>());
        when(issueGroupDao.getIssueGroupCountWithFilter(
                eq(IssueGroupDao.FILTER_CATEGORY_RULE_CODE),
                any(UUID.class),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(new ArrayList<>());

        IssueGroupCountResponse issueGroupCountResponse = this.issueService.getIssueGroupCount(searchIssueGroupRequest, userPrincipal);
        Map<String, String> ruleCodeCountMap = issueGroupCountResponse.getRuleCodeCountMap();
        Map<String, String> certaintyCountMap = issueGroupCountResponse.getCertaintyCountMap();
        assertEquals(0, ruleCodeCountMap.size());
        assertEquals(0, certaintyCountMap.size());
    }

    @Test
    void getIssueGroupCount_searchByProjectId_getOneResult() throws AppException {
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(this.project.getId())
                .build();
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        when(this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds())).thenReturn(Collections.singletonList(this.scanFile));
        when(issueGroupDao.getIssueGroupCountWithFilter(
                eq(IssueGroupDao.FILTER_CATEGORY_CERTAINTY),
                any(UUID.class),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(Collections.singletonList(IssueGroupCountRow.builder().certainty("D").count("100").build()));
        when(issueGroupDao.getIssueGroupCountWithFilter(
                eq(IssueGroupDao.FILTER_CATEGORY_RULE_CODE),
                any(UUID.class),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(Collections.singletonList(IssueGroupCountRow.builder().ruleCode("AOB0").count("100").build()));

        IssueGroupCountResponse issueGroupCountResponse = this.issueService.getIssueGroupCount(searchIssueGroupRequest, userPrincipal);
        Map<String, String> ruleCodeCountMap = issueGroupCountResponse.getRuleCodeCountMap();
        Map<String, String> certaintyCountMap = issueGroupCountResponse.getCertaintyCountMap();
        assertEquals("100", ruleCodeCountMap.get("AOB0"));
        assertEquals("100", certaintyCountMap.get("D"));
    }

    @Test
    void getIssueGroupCriticalityCount_searchByProjectId_getNoResult() throws AppException {
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(this.project.getId())
                .build();
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        when(this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds())).thenReturn(Collections.singletonList(this.scanFile));
        when(issueGroupDao.getIssueGroupCountWithFilter(
                eq(IssueGroupDao.FILTER_CATEGORY_CRITICALITY),
                any(UUID.class),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(new ArrayList<>());

        IssueGroupCriticalityCountResponse issueGroupCriticalityCountResponse = this.issueService.getIssueGroupCriticalityCount(searchIssueGroupRequest, userPrincipal);
        Map<String, Map<String, String>> citicalityRuleCodeCountMap = issueGroupCriticalityCountResponse.getCriticalityRuleCodeCountMap();
        assertEquals(0, citicalityRuleCodeCountMap.size());
    }

    @Test
    void getIssueGroupCriticalityCount_searchByProjectId_getOneResult() throws AppException {
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(this.project.getId())
                .build();
        UserPrincipal userPrincipal = UserPrincipal.builder().username("test").build();
        when(this.projectService.findById(this.project.getId())).thenReturn(Optional.of(this.project));
        when(this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds())).thenReturn(Collections.singletonList(this.scanFile));
        when(issueGroupDao.getIssueGroupCountWithFilter(
                eq(IssueGroupDao.FILTER_CATEGORY_CRITICALITY),
                any(UUID.class),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(Collections.singletonList(IssueGroupCountRow.builder().criticality("H").ruleCode("AOB0").count("100").build()));

        IssueGroupCriticalityCountResponse issueGroupCriticalityCountResponse = this.issueService.getIssueGroupCriticalityCount(searchIssueGroupRequest, userPrincipal);
        Map<String, Map<String, String>> citicalityRuleCodeCountMap = issueGroupCriticalityCountResponse.getCriticalityRuleCodeCountMap();
        assertEquals("100", citicalityRuleCodeCountMap.get("H").get("AOB0"));
    }


    @Test
    void testConvertToV3_normal_valueMatch() {
        Map<Integer, String> issueFileMap = new HashMap();
        issueFileMap.put(10, "$h/src/activations.h");
        issueFileMap.put(20, "$h/src/data.h");
        IssueDto issueDto = IssueServiceV3.convertToV3(issue, issueFileMap);
        assertEquals(issueDto.getId(), issue.getId());
        assertEquals(issueDto.getIssueGroupId(), issue.getIssueGroupId());
        assertEquals(issueDto.getCertainty(), issue.getCertainty());
        assertEquals(issueDto.getTraceCount(), issue.getTraceCount());
        assertEquals(issueDto.getStatus(), issue.getStatus());
        assertEquals(issueDto.getDsr(), issue.getDsr());
        //
        List<TraceDto> traceDtoList = issueDto.getTracePath();
        String tracePathString = issue.getTracePath();
        JSONArray tracePathJsonArray = new JSONArray(tracePathString);
        assertEquals(traceDtoList.size(), tracePathJsonArray.length());
        //first object
        TraceDto traceDto = traceDtoList.get(0);
        JSONObject traceJSONObject = tracePathJsonArray.getJSONObject(0);
        assertEquals("src/activations.h", traceDto.getFile());
        assertEquals("H", traceDto.getPathCategory());
        assertEquals(0, traceDto.getColumnNo());
        assertEquals(102, traceDto.getLineNo());

    }

    @Test
    void getTopCsvCodesDsr_normal_resultNotEmpty() {
        List<IssueGroupCountRow> newList = new ArrayList<>();
        List<IssueGroupCountRow> fixedList = new ArrayList<>();
        List<IssueGroupCountRow> existingList = new ArrayList<>();
        doReturn(newList).when(issueGroupDao).getTopCsvCodes(any());
        TopCsvCodeRequest topCsvCodeRequest = TopCsvCodeRequest.builder().build();
        IssueGroupCountDsrRow issueGroupCountDsrRow = issueService.getTopCsvCodesDsr(topCsvCodeRequest);
        assertNotNull(issueGroupCountDsrRow.getN());
        assertNotNull(issueGroupCountDsrRow.getE());
        assertNotNull(issueGroupCountDsrRow.getF());
    }

    @Test
    void assignIssueGroupsToUsers_normal_noExceptionThrown() throws AppException {
        List<AssignIssueGroupRequest.AssignIssueGroup> assignIssueGroupList = new ArrayList<>();
        assignIssueGroupList.add(AssignIssueGroupRequest.AssignIssueGroup.builder().issueGroupId("aaa").userId(UUID.randomUUID()).build());
        AssignIssueGroupRequest assignIssueGroupRequest = AssignIssueGroupRequest
                .builder()
                .assignIssueGroups(assignIssueGroupList)
                .build();
        User user = User.builder().build();
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        assertDoesNotThrow(() -> {
            issueService.assignIssueGroupsToUsers(assignIssueGroupRequest, Locale.ENGLISH, userPrincipal);
        });
    }

    @Test
    void fillDefaultValueForEmptyMap_searchIssueGroupRequestNull_noChange(){
        Map<String, Map<String, String>> criticalityRuleCodeMap = new HashMap<>();
        issueService.fillDefaultValueForEmptyMap(null, criticalityRuleCodeMap);
        assertEquals(0,criticalityRuleCodeMap.size());

    }

    @Test
    void fillDefaultValueForEmptyMap_criticalityRuleCodeMapNull_noErrorThrown(){
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder().build();
        assertDoesNotThrow(()->issueService.fillDefaultValueForEmptyMap(searchIssueGroupRequest, null));
    }

    @Test
    void fillDefaultValueForEmptyMap_noInnerRuleCodeMapKey_inserted(){
        List<SearchIssueGroupRequest.RuleCode> ruleCodesFilter = new ArrayList<>();
        ruleCodesFilter.add(SearchIssueGroupRequest.RuleCode.builder().csvCode("AOB0").criticality("L").build());
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .ruleCodes(ruleCodesFilter).build();

        Map<String, String> innerMap = new HashMap<>();

        Map<String, Map<String, String>> criticalityRuleCodeMap = new HashMap<>();
        criticalityRuleCodeMap.put("LOW", innerMap);

        issueService.fillDefaultValueForEmptyMap(searchIssueGroupRequest, criticalityRuleCodeMap);
        assertEquals(1,criticalityRuleCodeMap.get("LOW").size());
        assertTrue(criticalityRuleCodeMap.get("LOW").containsKey("AOB0"));
    }

    @Test
    void fillDefaultValueForEmptyMap_noOutterCriticalityRuleCodeMapKey_inserted(){
        List<SearchIssueGroupRequest.RuleCode> ruleCodesFilter = new ArrayList<>();
        ruleCodesFilter.add(SearchIssueGroupRequest.RuleCode.builder().csvCode("AOB0").criticality("L").build());
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .ruleCodes(ruleCodesFilter).build();
        Map<String, Map<String, String>> criticalityRuleCodeMap = new HashMap<>();
        issueService.fillDefaultValueForEmptyMap(searchIssueGroupRequest, criticalityRuleCodeMap);
        assertEquals(1,criticalityRuleCodeMap.size());
        assertTrue(criticalityRuleCodeMap.containsKey("LOW"));
    }

    @Test
    void fillDefaultValueForEmptyMap_alreadyHaveValueForInnerRuleCodeMap_noChange(){
        List<SearchIssueGroupRequest.RuleCode> ruleCodesFilter = new ArrayList<>();
        ruleCodesFilter.add(SearchIssueGroupRequest.RuleCode.builder().csvCode("AOB0").criticality("L").build());
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .ruleCodes(ruleCodesFilter).build();

        Map<String, String> innerMap = new HashMap<>();
        innerMap.put("AOB0","10");

        Map<String, Map<String, String>> criticalityRuleCodeMap = new HashMap<>();
        criticalityRuleCodeMap.put("LOW", innerMap);

        issueService.fillDefaultValueForEmptyMap(searchIssueGroupRequest, criticalityRuleCodeMap);
        assertEquals(1,criticalityRuleCodeMap.get("LOW").size());
        assertTrue(criticalityRuleCodeMap.get("LOW").containsKey("AOB0"));

    }

    @Test
    void fillDefaultValueForEmptyMap_alreadyHaveValueForOutterCriticalityRuleCodeMap_noChangeToOutterMap(){
        List<SearchIssueGroupRequest.RuleCode> ruleCodesFilter = new ArrayList<>();
        ruleCodesFilter.add(SearchIssueGroupRequest.RuleCode.builder().csvCode("AOB0").criticality("L").build());
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .ruleCodes(ruleCodesFilter).build();

        Map<String, String> innerMap = new HashMap<>();

        Map<String, Map<String, String>> criticalityRuleCodeMap = new HashMap<>();
        criticalityRuleCodeMap.put("LOW", innerMap);

        issueService.fillDefaultValueForEmptyMap(searchIssueGroupRequest, criticalityRuleCodeMap);
        assertEquals(1,criticalityRuleCodeMap.get("LOW").size());

    }


}
