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

package com.xcal.api.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.xcal.api.config.WithMockCustomUser;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.*;
import com.xcal.api.repository.IssueRepository;
import com.xcal.api.service.*;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.VariableUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class IssueControllerTest {

    @NonNull
    private final MockMvc mockMvc;

    @MockBean
    private IssueService issueService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ScanTaskService scanTaskService;

    @MockBean
    private OrchestrationService orchestrationService;

    @MockBean
    private IssueRepository issueRepository;

    @MockBean
    private FileService fileService;

    @MockBean
    private RuleService ruleService;

    @MockBean
    private CacheService cacheService;

    @MockBean
    private ImportService importService;

    @Autowired
    ObjectMapper om;

    @Autowired
    ObjectMapper cborOm = new ObjectMapper(new CBORFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String adminUsername= "admin";
    private final String xcalAdminUsername = "xcaladmin";
    private final Map<String, RuleInformation> ruleInformationMap = new HashMap<>();
    private final User currentUser = User.builder().id(UUID.randomUUID()).username("user").displayName("testDisplayName").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();
    private final User adminUser = User.builder().id(UUID.randomUUID()).username(adminUsername).displayName("adminUser").email("admin@xxxx.com").password("12345").userGroups(Collections.singletonList(UserGroup.builder().id(UUID.randomUUID()).groupType(UserGroup.Type.ROLE).groupName("ADMIN").build())).build();
    private final User user1 = User.builder().id(UUID.randomUUID()).username("user1").displayName("Dummy User").email("user1@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();
    private final Project project = Project.builder().id(UUID.randomUUID()).createdBy(currentUser.getUsername()).build();
    private final Project project2 = Project.builder().id(UUID.randomUUID()).createdBy(adminUser.getUsername()).build();
    private final UUID scanTaskId = UUID.randomUUID();
    private final UUID baselineScanTaskId = UUID.randomUUID();
    private final ProjectConfig projectConfig = ProjectConfig.builder().id(UUID.randomUUID()).name("default").project(project).status(ProjectConfig.Status.ACTIVE).build();
    private final List<ProjectConfigAttribute> attrs = Arrays.asList(
            ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                    .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue).value("online_agent").build(),
            ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.type)
                    .name(VariableUtil.ProjectConfigAttributeTypeName.SOURCE_STORAGE_NAME.nameValue).value("agent").build(),
            ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                    .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue).value("/source/demo_benchmark/c_testcase/advance").build(),
            ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.type)
                    .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH.nameValue).value("/source/demo_benchmark/c_testcase/advance").build(),
            ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_SOURCE.type)
                    .name(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_SOURCE.nameValue).value("true").build(),
            ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID.type)
                    .name(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID.nameValue).value("baselineCommitId").build(),
            ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.type)
                    .name(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue).value("commitId").build());

    private final ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(project).projectConfig(projectConfig).build();
    private final ScanTask scanTask2 = ScanTask.builder().id(UUID.randomUUID()).project(project2).build();
    private final ScanEngine scanEngine = ScanEngine.builder()
            .id(UUID.randomUUID())
            .name("scanEngineName")
            .version("1")
            .language("C++")
            .provider("Test provider")
            .providerUrl("Test provider url")
            .url("test url").build();
    private final RuleSet ruleSet = RuleSet.builder()
            .id(UUID.randomUUID())
            .name("ruleset_name")
            .version("1")
            .displayName("ruleset display name")
            .scanEngine(scanEngine)
            .language("C++")
            .provider("test provider")
            .providerUrl("test provider url ").build();

    private RuleInformation ruleInformation;
    private Issue issue1;
    private final RuleInformation ruleInformation2 = RuleInformation.builder()
            .id(UUID.randomUUID())
            .ruleSet(ruleSet)
            .ruleCode("BUILTIN-UIV")
            .category("ROBUSTNESS")
            .vulnerable("UIV")
            .name("Uninitialized variable")
            .certainty(RuleInformation.Certainty.D)
            .language("c,c++")
            .description("Address of a local variable (stack space) has been passed to the caller, causing illegal memory access")
            .build();
    private Issue issue2;
    private Issue issue3;


    @BeforeEach
    void setUp() {
        ruleInformationMap.put("AOB-D", RuleInformation.builder()
                .id(UUID.randomUUID())
                .category("BUILTIN")
                .ruleSet(RuleSet.builder().name("VULNERABILITY").build())
                .vulnerable("AOB")
                .certainty(RuleInformation.Certainty.D)
                .priority(RuleInformation.Priority.HIGH)
                .name("Array out of bound")
                .severity(RuleInformation.Severity.HIGH)
                .likelihood(RuleInformation.Likelihood.LIKELY)
                .remediationCost(RuleInformation.RemediationCost.HIGH)
                .description("While writing data to a buffer, the access overruns the buffer’s boundary into adjacent memory locations")
                .messageTemplate("Array out of bound")
                .ruleCode("AOB-D")
                .ruleSet(ruleSet)
                .build());
        ruleInformationMap.put("AOB-M", RuleInformation.builder()
                .id(UUID.randomUUID())
                .category("BUILTIN")
                .ruleSet(RuleSet.builder().name("VULNERABILITY").build())
                .vulnerable("AOB")
                .certainty(RuleInformation.Certainty.M)
                .priority(RuleInformation.Priority.HIGH)
                .name("Array out of bound")
                .severity(RuleInformation.Severity.HIGH)
                .likelihood(RuleInformation.Likelihood.LIKELY)
                .remediationCost(RuleInformation.RemediationCost.HIGH)
                .description("May is the case when the purported defect may happen due to analyser cannot proof definitively such is the case")
                .messageTemplate("Array may out of bound")
                .ruleCode("AOB-M")
                .ruleSet(ruleSet)
                .build());
        ruleInformation = RuleInformation.builder()
                .id(UUID.randomUUID())
                .ruleSet(RuleSet.builder().id(UUID.randomUUID()).name("BUILTIN").version("1").revision("1.0").build())
                .ruleCode("BUILTIN-NPD-D")
                .category("ROBUSTNESS")
                .vulnerable("NPD")
                .name("Null pointer dereference")
                .certainty(RuleInformation.Certainty.D)
                .priority(RuleInformation.Priority.HIGH)
                .severity(RuleInformation.Severity.HIGH)
                .likelihood(RuleInformation.Likelihood.LIKELY)
                .remediationCost(RuleInformation.RemediationCost.LOW)
                .language("c,c++")
                .description("Address of a local variable (stack space) has been passed to the caller, causing illegal memory access")
                .ruleSet(ruleSet)
                .build();
        issue1 = Issue.builder().id(UUID.randomUUID()).issueCode("BUILTIN-NPD-D").scanTask(scanTask)
                .seq("00011")
                .severity(Issue.Severity.HIGH)
                .lineNo(381)
                .columnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("The value of the pointer (reference) is 0 (or near zero) and is used to access memory expected to be valid")
                .status(Issue.Status.ACTIVE)
                .action(Issue.Action.CONFIRMED)
                .createdBy(currentUser.getUsername())
                .createdOn(new Date())
                .modifiedOn(new Date())
                .modifiedBy(currentUser.getUsername())
                .assignTo(currentUser)
                .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c@ngx_vslprintf@buf@NPD@@@fffffa68")
                .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c")
                .ruleInformation(ruleInformation)
                .issueTraces(new ArrayList<>())
                .build();
        issue1.setChecksum(DigestUtils.md5Hex(issue1.toString()));
        issue2 = Issue.builder().id(UUID.randomUUID()).issueCode(ruleInformation2.getRuleCode()).scanTask(scanTask)
                .seq("00002")
                .severity(Issue.Severity.HIGH)
                .lineNo(381)
                .columnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("uiv")
                .message("dummy mesage 2")
                .status(Issue.Status.ACTIVE)
                .action(Issue.Action.CONFIRMED)
                .createdBy(currentUser.getUsername())
                .createdOn(new Date())
                .modifiedOn(new Date())
                .modifiedBy(currentUser.getUsername())
                .assignTo(currentUser)
                .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c@ngx_vslprintf@buf@NPD@@@fffffa68")
                .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c")
                .ruleInformation(ruleInformation2)
                .issueTraces(new ArrayList<>())
                .build();
        issue2.setChecksum(DigestUtils.md5Hex(issue2.toString()));
        issue3 = Issue.builder().id(UUID.randomUUID()).issueCode("BUILTIN-NPD-D").scanTask(scanTask2)
                .seq("00012")
                .severity(Issue.Severity.HIGH)
                .lineNo(31)
                .columnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("The value of the pointer (reference) is 0 (or near zero) and is used to access memory expected to be valid")
                .status(Issue.Status.ACTIVE)
                .action(Issue.Action.CONFIRMED)
                .createdBy(adminUser.getUsername())
                .createdOn(new Date())
                .modifiedOn(new Date())
                .modifiedBy(adminUser.getUsername())
                .assignTo(adminUser)
                .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c@ngx_vslprintf@buf@NPD@@@fffffa68")
                .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c")
                .ruleInformation(ruleInformation)
                .issueTraces(new ArrayList<>())
                .build();
        issue3.setChecksum(DigestUtils.md5Hex(issue3.toString()));

        when(issueService.convertIssuesToDto(anyList(), any(Locale.class))).thenAnswer(invocation -> {
            List<Issue> issues = invocation.getArgument(0);
            Map<String, I18nMessage> i18nMessageMap = new HashMap<>();
            return issues.stream().map(issue -> IssueService.convertIssueToDto(issue, i18nMessageMap)).collect(Collectors.toList());
        });

        when(issueService.convertIssueToDto(any(Issue.class), any(Locale.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            Map<String, I18nMessage> i18nMessageMap = new HashMap<>();
            return IssueService.convertIssueToDto(issue, i18nMessageMap);
        });
        when(issueService.convertIssueToDto(any(Issue.class), anyList(), any(Locale.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            List<IssueTrace> issueTraces = invocation.getArgument(1);
            Map<String, I18nMessage> i18nMessageMap = new HashMap<>();
            return IssueService.convertIssueToDto(issue, issueTraces, i18nMessageMap);
        });

        when(issueService.constructImportIssueResponse(any(ScanTask.class), notNull())).thenAnswer(invocation -> {
            ScanTask scanTask = invocation.getArgument(0);
            List<Issue> issues = invocation.getArgument(1);
            ImportIssueResponse importIssueResponse = ImportIssueResponse.builder().scanTaskId(scanTask.getId()).build();
            importIssueResponse.setIssues(issues.stream().map(IssueService::convertIssueToIssueOfImportIssueResponse).collect(Collectors.toList()));
            importIssueResponse.setSummary(scanTask.getSummary());
            return importIssueResponse;
        });

        doNothing().when(cacheService).initCacheRuleInformation();

        projectConfig.setAttributes(attrs);
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getIssueSummaryCountByUser_ProjectNotFound_ThrowAppException() throws Exception {
        log.info("[getIssueSummaryCountByUser_ProjectNotFound_ThrowAppException]");
        UUID projectUuid = UUID.randomUUID();
        when(projectService.findById(projectUuid)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/issue_service/v2/project/{id}/issue_assignsummary", projectUuid)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getIssueSummaryCountByUser_ScanTaskNotFound_ReturnEmptyResponse() throws Exception {
        log.info("[getIssueSummaryCountByUser_ScanTaskNotFound_ReturnEmptyResponse]");
        UUID projectUuid = UUID.randomUUID();
        Project project = Project.builder().id(projectUuid).build();
        when(projectService.findById(projectUuid)).thenReturn(Optional.of(project));
        when(scanTaskService.getLatestCompletedScanTaskByProject(project)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/issue_service/v2/project/{id}/issue_assignsummary", projectUuid)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser()
    void getIssueSummaryCountByUser_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[getIssueSummaryCountByUser_InsufficientPrivilege_ThrowAppException]");
        UUID projectUuid = UUID.randomUUID();
        UUID scanTaskId = UUID.randomUUID();
        Project project = Project.builder().id(projectUuid).createdBy("Other User").build();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(project).build();
        when(projectService.findById(projectUuid)).thenReturn(Optional.of(project));
        when(scanTaskService.getLatestCompletedScanTaskByProject(project)).thenReturn(Optional.of(scanTask));
        when(issueRepository.countByScanTaskAndAssignTo(any(), any())).thenReturn(0L);
        mockMvc.perform(get("/api/issue_service/v2/project/{id}/issue_assignsummary", projectUuid)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getIssueSummaryCountByUser_validProjectId_Success() throws Exception {
        log.info("[getIssueSummaryCountByUser_validProjectId_Success]");
        UUID projectUuid = UUID.randomUUID();
        UUID scanTaskId = UUID.randomUUID();
        Project project = Project.builder().id(projectUuid).createdBy(currentUser.getUsername()).build();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(project).build();
        List<IssueSummaryResponse.AssignSummary> assignSummaryList = Arrays.asList(IssueSummaryResponse.AssignSummary.builder().displayName("Tom").count(2L).build()
                , IssueSummaryResponse.AssignSummary.builder().displayName("John").count(3L).build());
        when(projectService.findById(projectUuid)).thenReturn(Optional.of(project));
        when(scanTaskService.getLatestCompletedScanTaskByProject(project)).thenReturn(Optional.of(scanTask));
        when(issueService.findIssueSummaryCountByUser(scanTask.getId())).thenReturn(assignSummaryList);
        mockMvc.perform(get("/api/issue_service/v2/project/{id}/issue_assignsummary", projectUuid)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].displayName").value("Tom"))
                .andExpect(jsonPath("$.[0].count").value("2"))
                .andExpect(jsonPath("$.[1].displayName").value("John"))
                .andExpect(jsonPath("$.[1].count").value("3"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void countIssueByUser_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[countIssueByUser_ScanTaskNotFound_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/issue_assignsummary", scanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void countIssueByUser_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[countIssueByUser_InsufficientPrivilege_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("other user").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(issueRepository.countByScanTaskAndAssignTo(any(), any())).thenReturn(0L);
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/issue_assignsummary", scanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void countIssueByUser_validScanTaskId_Success() throws Exception {
        log.info("[countIssueByUser_validScanTaskId_Success]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("nonAdmin").build()).build();
        List<IssueSummaryResponse.AssignSummary> assignSummaryList = Arrays.asList(IssueSummaryResponse.AssignSummary.builder().displayName("Tom").count(2L).build()
                , IssueSummaryResponse.AssignSummary.builder().displayName("John").count(3L).build());
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(issueService.findIssueSummaryCountByUser(scanTask.getId())).thenReturn(assignSummaryList);
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/issue_assignsummary", scanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].displayName").value("Tom"))
                .andExpect(jsonPath("$.[0].count").value("2"))
                .andExpect(jsonPath("$.[1].displayName").value("John"))
                .andExpect(jsonPath("$.[1].count").value("3"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void listIssueInScanTask_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[listIssueInScanTask_ScanTaskNotFound_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/issues", scanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void listIssueInScanTask_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[listIssueInScanTask_InsufficientPrivilege_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("other user").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/issues", scanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void listIssueInScanTask_ValidScanTaskId_Success() throws Exception {
        log.info("[listIssueInScanTask_ValidScanTaskId_Success]");
        List<Issue> issueList = Collections.singletonList(issue1);
        Page<Issue> pagedIssues = new PageImpl<>(issueList);
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(issueService.listIssueInScanTask(eq(scanTask), any(Pageable.class))).thenReturn(pagedIssues);
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/issues", scanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(issue1.getId().toString()))
                .andExpect(jsonPath("$.content[0].issueKey").value(issue1.getIssueKey()))
                .andExpect(jsonPath("$.content[0].seq").value(issue1.getSeq()))
                .andExpect(jsonPath("$.content[0].issueCategory").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.content[0].ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.content[0].vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.content[0].certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.content[0].issueCode").value(issue1.getIssueCode()))
                .andExpect(jsonPath("$.content[0].issueName").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.content[0].severity").value(issue1.getSeverity().name()))
                .andExpect(jsonPath("$.content[0].likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.content[0].remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.content[0].relativePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.content[0].scanFilePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.content[0].lineNo").value(issue1.getLineNo()))
                .andExpect(jsonPath("$.content[0].columnNo").value(issue1.getColumnNo()))
                .andExpect(jsonPath("$.content[0].functionName").value(issue1.getFunctionName()))
                .andExpect(jsonPath("$.content[0].variableName").value(issue1.getVariableName()))
                .andExpect(jsonPath("$.content[0].message").value(issue1.getMessage()))
                .andExpect(jsonPath("$.content[0].status").value(issue1.getStatus().name()))
                .andExpect(jsonPath("$.content[0].action").value(issue1.getAction().name()))
                .andExpect(jsonPath("$.content[0].createdBy").value(issue1.getCreatedBy()))
                .andExpect(jsonPath("$.content[0].modifiedBy").value(issue1.getModifiedBy()))
                .andExpect(jsonPath("$.content[0].assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.content[0].assignTo.displayName").value(currentUser.getDisplayName()))
                .andExpect(jsonPath("$.content[0].assignTo.email").value(currentUser.getEmail()))
                .andExpect(jsonPath("$.content[0].assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.content[0].ruleInformation.id").value(ruleInformation.getId().toString()))
                .andExpect(jsonPath("$.content[0].ruleInformation.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.content[0].ruleInformation.ruleSetVersion").value(ruleInformation.getRuleSet().getVersion()))
                .andExpect(jsonPath("$.content[0].ruleInformation.ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.content[0].ruleInformation.category").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.content[0].ruleInformation.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.content[0].ruleInformation.name").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.content[0].ruleInformation.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.content[0].ruleInformation.priority").value(ruleInformation.getPriority().name()))
                .andExpect(jsonPath("$.content[0].ruleInformation.severity").value(ruleInformation.getSeverity().name()))
                .andExpect(jsonPath("$.content[0].ruleInformation.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.content[0].ruleInformation.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.content[0].ruleInformation.language").value(ruleInformation.getLanguage()))
                .andExpect(jsonPath("$.content[0].ruleInformation.description").value(ruleInformation.getDescription()));
    }

    @Test
    @WithMockCustomUser()
    void deleteIssueFromScanTask_WithNonXcalAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[deleteIssueFromScanTask_WithNonXcalAdminUser_ReturnAccessDeniedException]");
        mockMvc.perform(delete("/api/issue_service/v2/scan_task/{id}/issues", UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser(xcalAdminUsername)
    void deleteIssueFromScanTask_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[deleteIssueFromScanTask_ScanTaskNotFound_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/issue_service/v2/scan_task/{id}/issues", scanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(xcalAdminUsername)
    void deleteIssueFromScanTask_ValidScanTaskId_Success() throws Exception {
        log.info("[deleteIssueFromScanTask_ValidScanTaskId_Success]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        doNothing().when(orchestrationService).deleteAllInScanTask(eq(scanTask), eq(false), any(User.class));
        mockMvc.perform(delete("/api/issue_service/v2/scan_task/{id}/issues", scanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser()
    void assignIssue_IssueNotFound_ThrowAppException() throws Exception {
        log.info("[assignIssue_IssueNotFound_ThrowAppException]");
        UUID issueId = UUID.randomUUID();
        when(issueService.findById(issueId)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/user/{userId}", issueId, currentUser.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void assignIssue_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[assignIssue_InsufficientPrivilege_ThrowAppException]");
        when(issueService.findById(issue3.getId())).thenReturn(Optional.of(issue3));
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/user/{userId}", issue3.getId(), user1.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void assignIssue_InputIssueIdAndUserId_Success() throws Exception {
        log.info("[assignIssue_InputIssueIdAndUserId_Success]");
        when(issueService.findById(issue1.getId())).thenReturn(Optional.of(issue1));
        when(issueService.assignIssue(any(), any(), any())).thenReturn(issue1);
        doNothing().when(issueService).sendAssignIssueEmail(any(), any(), any(), any(), any());
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/user/{userId}", issue1.getId(), currentUser.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issue1.getId().toString()))
                .andExpect(jsonPath("$.issueKey").value(issue1.getIssueKey()))
                .andExpect(jsonPath("$.seq").value(issue1.getSeq()))
                .andExpect(jsonPath("$.issueCategory").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.issueCode").value(issue1.getIssueCode()))
                .andExpect(jsonPath("$.issueName").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.severity").value(issue1.getSeverity().name()))
                .andExpect(jsonPath("$.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.relativePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.scanFilePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.lineNo").value(issue1.getLineNo()))
                .andExpect(jsonPath("$.columnNo").value(issue1.getColumnNo()))
                .andExpect(jsonPath("$.functionName").value(issue1.getFunctionName()))
                .andExpect(jsonPath("$.variableName").value(issue1.getVariableName()))
                .andExpect(jsonPath("$.message").value(issue1.getMessage()))
                .andExpect(jsonPath("$.status").value(issue1.getStatus().name()))
                .andExpect(jsonPath("$.action").value(issue1.getAction().name()))
                .andExpect(jsonPath("$.createdBy").value(issue1.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(issue1.getModifiedBy()))
                .andExpect(jsonPath("$.assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.assignTo.displayName").value(currentUser.getDisplayName()))
                .andExpect(jsonPath("$.assignTo.email").value(currentUser.getEmail()))
                .andExpect(jsonPath("$.assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.ruleInformation.id").value(ruleInformation.getId().toString()))
                .andExpect(jsonPath("$.ruleInformation.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.ruleInformation.ruleSetVersion").value(ruleInformation.getRuleSet().getVersion()))
                .andExpect(jsonPath("$.ruleInformation.ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.ruleInformation.category").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.ruleInformation.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.ruleInformation.name").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.ruleInformation.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.ruleInformation.priority").value(ruleInformation.getPriority().name()))
                .andExpect(jsonPath("$.ruleInformation.severity").value(ruleInformation.getSeverity().name()))
                .andExpect(jsonPath("$.ruleInformation.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.ruleInformation.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.ruleInformation.language").value(ruleInformation.getLanguage()))
                .andExpect(jsonPath("$.ruleInformation.description").value(ruleInformation.getDescription()));
    }

    @Test
    @WithMockCustomUser()
    void updateIssueStatus_IssueNotFound_ThrowAppException() throws Exception {
        log.info("[updateIssueStatus_IssueNotFound_ThrowAppException]");
        when(issueService.findById(issue1.getId())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/status/{status}", issue1.getId(), "Active")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void updateIssueStatus_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[updateIssueStatus_InsufficientPrivilege_ThrowAppException]");
        when(issueService.findById(issue3.getId())).thenReturn(Optional.of(issue3));
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/status/{status}", issue3.getId(), "Active")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateIssueStatus_InputIssueIdAndStatus_Success() throws Exception {
        log.info("[updateIssueStatus_InputIssueIdAndStatus_Success]");
        when(issueService.findById(issue1.getId())).thenReturn(Optional.of(issue1));
        when(issueService.updateIssueStatus(any(), any(), any())).thenReturn(issue1);
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/status/{status}", issue1.getId(), "Active")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issue1.getId().toString()))
                .andExpect(jsonPath("$.issueKey").value(issue1.getIssueKey()))
                .andExpect(jsonPath("$.seq").value(issue1.getSeq()))
                .andExpect(jsonPath("$.issueCategory").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.issueCode").value(issue1.getIssueCode()))
                .andExpect(jsonPath("$.issueName").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.severity").value(issue1.getSeverity().name()))
                .andExpect(jsonPath("$.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.relativePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.scanFilePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.lineNo").value(issue1.getLineNo()))
                .andExpect(jsonPath("$.columnNo").value(issue1.getColumnNo()))
                .andExpect(jsonPath("$.functionName").value(issue1.getFunctionName()))
                .andExpect(jsonPath("$.variableName").value(issue1.getVariableName()))
                .andExpect(jsonPath("$.message").value(issue1.getMessage()))
                .andExpect(jsonPath("$.status").value(issue1.getStatus().name()))
                .andExpect(jsonPath("$.action").value(issue1.getAction().name()))
                .andExpect(jsonPath("$.createdBy").value(issue1.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(issue1.getModifiedBy()))
                .andExpect(jsonPath("$.assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.assignTo.displayName").value(currentUser.getDisplayName()))
                .andExpect(jsonPath("$.assignTo.email").value(currentUser.getEmail()))
                .andExpect(jsonPath("$.assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.ruleInformation.id").value(ruleInformation.getId().toString()))
                .andExpect(jsonPath("$.ruleInformation.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.ruleInformation.ruleSetVersion").value(ruleInformation.getRuleSet().getVersion()))
                .andExpect(jsonPath("$.ruleInformation.ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.ruleInformation.category").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.ruleInformation.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.ruleInformation.name").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.ruleInformation.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.ruleInformation.priority").value(ruleInformation.getPriority().name()))
                .andExpect(jsonPath("$.ruleInformation.severity").value(ruleInformation.getSeverity().name()))
                .andExpect(jsonPath("$.ruleInformation.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.ruleInformation.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.ruleInformation.language").value(ruleInformation.getLanguage()))
                .andExpect(jsonPath("$.ruleInformation.description").value(ruleInformation.getDescription()));
    }

    @Test
    @WithMockCustomUser()
    void updateIssueSeverity_IssueNotFound_ThrowAppException() throws Exception {
        log.info("[updateIssueSeverity_IssueNotFound_ThrowAppException]");
        when(issueService.findById(issue1.getId())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/severity/{severity}", issue1.getId(), "HIGH")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void updateIssueSeverity_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[updateIssueSeverity_InsufficientPrivilege_ThrowAppException]");
        when(issueService.findById(issue3.getId())).thenReturn(Optional.of(issue3));
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/severity/{severity}", issue3.getId(), "HIGH")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateIssueSeverity_InputIssueIdAndSeverity_Success() throws Exception {
        log.info("[updateIssueSeverity_InputIssueIdAndSeverity_Success]");
        when(issueService.findById(issue1.getId())).thenReturn(Optional.of(issue1));
        when(issueService.updateIssueSeverity(any(), any(), any())).thenReturn(issue1);
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/severity/{severity}", issue1.getId(), "HIGH")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issue1.getId().toString()))
                .andExpect(jsonPath("$.issueKey").value(issue1.getIssueKey()))
                .andExpect(jsonPath("$.seq").value(issue1.getSeq()))
                .andExpect(jsonPath("$.issueCategory").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.issueCode").value(issue1.getIssueCode()))
                .andExpect(jsonPath("$.issueName").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.severity").value(issue1.getSeverity().name()))
                .andExpect(jsonPath("$.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.relativePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.scanFilePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.lineNo").value(issue1.getLineNo()))
                .andExpect(jsonPath("$.columnNo").value(issue1.getColumnNo()))
                .andExpect(jsonPath("$.functionName").value(issue1.getFunctionName()))
                .andExpect(jsonPath("$.variableName").value(issue1.getVariableName()))
                .andExpect(jsonPath("$.message").value(issue1.getMessage()))
                .andExpect(jsonPath("$.status").value(issue1.getStatus().name()))
                .andExpect(jsonPath("$.action").value(issue1.getAction().name()))
                .andExpect(jsonPath("$.createdBy").value(issue1.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(issue1.getModifiedBy()))
                .andExpect(jsonPath("$.assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.assignTo.displayName").value(currentUser.getDisplayName()))
                .andExpect(jsonPath("$.assignTo.email").value(currentUser.getEmail()))
                .andExpect(jsonPath("$.assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.ruleInformation.id").value(ruleInformation.getId().toString()))
                .andExpect(jsonPath("$.ruleInformation.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.ruleInformation.ruleSetVersion").value(ruleInformation.getRuleSet().getVersion()))
                .andExpect(jsonPath("$.ruleInformation.ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.ruleInformation.category").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.ruleInformation.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.ruleInformation.name").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.ruleInformation.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.ruleInformation.priority").value(ruleInformation.getPriority().name()))
                .andExpect(jsonPath("$.ruleInformation.severity").value(ruleInformation.getSeverity().name()))
                .andExpect(jsonPath("$.ruleInformation.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.ruleInformation.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.ruleInformation.language").value(ruleInformation.getLanguage()))
                .andExpect(jsonPath("$.ruleInformation.description").value(ruleInformation.getDescription()));
    }

    @Test
    @WithMockCustomUser()
    void updateIssueAction_IssueNotFound_ThrowAppException() throws Exception {
        log.info("[updateIssueAction_IssueNotFound_ThrowAppException]");
        when(issueService.findById(issue1.getId())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/action/{action}", issue1.getId(), "CONFIRMED")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void updateIssueAction_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[updateIssueAction_InsufficientPrivilege_ThrowAppException]");
        when(issueService.findById(issue3.getId())).thenReturn(Optional.of(issue3));
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/action/{action}", issue3.getId(), "CONFIRMED")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateIssueAction_InputIssueIdAndAction_Success() throws Exception {
        log.info("[updateIssueAction_InputIssueIdAndAction_Success]");
        when(issueService.findById(issue1.getId())).thenReturn(Optional.of(issue1));
        when(issueService.updateIssueAction(any(), any(), any())).thenReturn(issue1);
        mockMvc.perform(post("/api/issue_service/v2/issue/{id}/action/{action}", issue1.getId(), "CONFIRMED")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issue1.getId().toString()))
                .andExpect(jsonPath("$.issueKey").value(issue1.getIssueKey()))
                .andExpect(jsonPath("$.seq").value(issue1.getSeq()))
                .andExpect(jsonPath("$.issueCategory").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.issueCode").value(issue1.getIssueCode()))
                .andExpect(jsonPath("$.issueName").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.severity").value(issue1.getSeverity().name()))
                .andExpect(jsonPath("$.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.relativePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.scanFilePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.lineNo").value(issue1.getLineNo()))
                .andExpect(jsonPath("$.columnNo").value(issue1.getColumnNo()))
                .andExpect(jsonPath("$.functionName").value(issue1.getFunctionName()))
                .andExpect(jsonPath("$.variableName").value(issue1.getVariableName()))
                .andExpect(jsonPath("$.message").value(issue1.getMessage()))
                .andExpect(jsonPath("$.status").value(issue1.getStatus().name()))
                .andExpect(jsonPath("$.action").value(issue1.getAction().name()))
                .andExpect(jsonPath("$.createdBy").value(issue1.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(issue1.getModifiedBy()))
                .andExpect(jsonPath("$.assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.assignTo.displayName").value(currentUser.getDisplayName()))
                .andExpect(jsonPath("$.assignTo.email").value(currentUser.getEmail()))
                .andExpect(jsonPath("$.assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.ruleInformation.id").value(ruleInformation.getId().toString()))
                .andExpect(jsonPath("$.ruleInformation.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.ruleInformation.ruleSetVersion").value(ruleInformation.getRuleSet().getVersion()))
                .andExpect(jsonPath("$.ruleInformation.ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.ruleInformation.category").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.ruleInformation.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.ruleInformation.name").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.ruleInformation.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.ruleInformation.priority").value(ruleInformation.getPriority().name()))
                .andExpect(jsonPath("$.ruleInformation.severity").value(ruleInformation.getSeverity().name()))
                .andExpect(jsonPath("$.ruleInformation.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.ruleInformation.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.ruleInformation.language").value(ruleInformation.getLanguage()))
                .andExpect(jsonPath("$.ruleInformation.description").value(ruleInformation.getDescription()));
    }

    @Test
    @WithMockCustomUser()
    void sendIssuesToUsers_IssueNotEmpty_Success() throws Exception {
        log.info("[sendIssuesToUsers_IssueNotEmpty_Success]");
        AssignIssuesRequest assignIssuesRequest = AssignIssuesRequest.builder().assignIssues(
                Collections.singletonList(AssignIssuesRequest.AssignIssue.builder().userId(UUID.randomUUID()).issueId(UUID.randomUUID()).build())).build();
        doNothing().when(issueService).sendIssuesToUsers(any(), any(), any(), any());
        mockMvc.perform(post("/api/issue_service/v2/issues/users/email")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(assignIssuesRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getIssueById() throws Exception {
        log.info("[getIssueById]");
        RuleInformation ruleInformation = this.ruleInformationMap.get("AOB-D");
        UUID issueId = UUID.randomUUID();
        Project project = Project.builder()
                .id(UUID.randomUUID())
                .projectId("test")
                .name("Test")
                .createdBy(adminUsername)
                .status(Project.Status.ACTIVE)
                .build();
        ScanFile scanFile = ScanFile.builder()
                .id(UUID.randomUUID())
                .fileInfo(FileInfo.builder()
                        .id(UUID.randomUUID())
                        .relativePath("/test/test.abc")
                        .version("testVersion")
                        .build())
                .build();
        ScanTask scanTask = ScanTask.builder()
                .id(UUID.randomUUID())
                .project(project)
                .status(ScanTask.Status.COMPLETED)
                .build();

        List<IssueTrace> issueTraces = Collections.singletonList(IssueTrace.builder()
                .id(UUID.randomUUID())
                .seq(1)
                .scanFile(scanFile)
                .build());
        final String checksum = DigestUtils.md5Hex(issueTraces.toString());
        issueTraces.forEach(it -> it.setChecksum(checksum));

        Issue issue = Issue.builder()
                .id(issueId)
                .ruleInformation(ruleInformation)
                .issueKey(RandomStringUtils.randomAlphabetic(15))
                .issueCode(ruleInformation.getRuleCode())
                .seq("00001")
                .severity(Issue.Severity.HIGH)
                .scanFile(scanFile)
                .scanTask(scanTask)
                .issueTraces(issueTraces)
                .status(Issue.Status.ACTIVE)
                .action(Issue.Action.PENDING)
                .build();

        when(issueService.retrieveRuleInformationMap(any(), any())).thenReturn(this.ruleInformationMap);
        when(issueService.findById(issueId))
                .thenReturn(Optional.of(issue));

        this.mockMvc.perform(
                get("/api/issue_service/v2/issue/{id}", issueId)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issue.getId().toString()))
                .andExpect(jsonPath("$.seq").value(issue.getSeq()))
                .andExpect(jsonPath("$.issueCode").value(ruleInformation.getRuleCode()));
    }

    @Test
    @WithMockCustomUser()
    void getIssueById_IssueNotFound_ThrowAppException() throws Exception {
        log.info("[getIssueById_IssueNotFound_ThrowAppException]");
        when(issueService.findById(issue1.getId())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/issue_service/v2/issue/{id}", issue1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser
    void getIssueById_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[getIssueById_InsufficientPrivilege_ThrowAppException]");
        when(issueService.findById(issue3.getId())).thenReturn(Optional.of(issue3));
        mockMvc.perform(get("/api/issue_service/v2/issue/{id}", issue3.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void assignIssuesToUsersTestSuccess() throws Exception {
        log.info("[assignIssuesToUsersTestSuccess]");
        List<AssignIssuesRequest.AssignIssue> assignIssues = new ArrayList<>();
        UUID issueId = UUID.fromString("d6755c0e-e853-447b-94d6-4e592c513342");
        UUID userId = UUID.fromString("a0fe3869-e57c-49c6-825b-3fb519071d18");
        AssignIssuesRequest.AssignIssue assignIssue = AssignIssuesRequest.AssignIssue.builder().issueId(issueId).userId(userId).build();
        assignIssues.add(assignIssue);

        AssignIssuesRequest assignIssueRequest = AssignIssuesRequest.builder().assignIssues(assignIssues).build();

        when(issueService.assignIssuesToUsers(any(), any())).thenReturn(new ArrayList<>());
        mockMvc.perform(post("/api/issue_service/v2/issues/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(assignIssueRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser()
    void searchIssue_ProjectNotFound_ThrowAppException() throws Exception {
        log.info("[searchIssue_ProjectNotFound_ThrowAppException]");
        SearchIssueRequest searchIssueRequest = SearchIssueRequest.builder().projectId(project.getId()).build();
        when(projectService.findById((project.getId()))).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/issue_service/v2/search_issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchIssueRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void searchIssue_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[searchIssue_ProjectNotFound_ThrowAppException]");
        SearchIssueRequest searchIssueRequest = SearchIssueRequest.builder().projectId(project2.getId()).build();
        when(projectService.findById((project2.getId()))).thenReturn(Optional.of(project2));
        when(scanTaskService.getLatestCompletedScanTaskByProject(project2)).thenReturn(Optional.of(scanTask2));
        mockMvc.perform(post("/api/issue_service/v2/search_issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchIssueRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void searchIssue_ScanTaskNotEmpty_Success() throws Exception {
        log.info("[searchIssue_ScanTaskNotEmpty_Success]");
        List<Issue> issueList = Collections.singletonList(issue1);
        Page<Issue> pagedIssues = new PageImpl<>(issueList);
        SearchIssueRequest searchIssueRequest = SearchIssueRequest.builder().scanTaskId(scanTaskId).seq("1").build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(issueService.searchIssue(any(), any(), any(), notNull(), notNull(), notNull(), notNull(), any(), any(), any())).thenReturn(pagedIssues);
        mockMvc.perform(post("/api/issue_service/v2/search_issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchIssueRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(issue1.getId().toString()))
                .andExpect(jsonPath("$.content[0].issueKey").value(issue1.getIssueKey()))
                .andExpect(jsonPath("$.content[0].seq").value(issue1.getSeq()))
                .andExpect(jsonPath("$.content[0].issueCategory").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.content[0].ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.content[0].vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.content[0].certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.content[0].issueCode").value(issue1.getIssueCode()))
                .andExpect(jsonPath("$.content[0].issueName").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.content[0].severity").value(issue1.getSeverity().name()))
                .andExpect(jsonPath("$.content[0].likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.content[0].remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.content[0].relativePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.content[0].scanFilePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.content[0].lineNo").value(issue1.getLineNo()))
                .andExpect(jsonPath("$.content[0].columnNo").value(issue1.getColumnNo()))
                .andExpect(jsonPath("$.content[0].functionName").value(issue1.getFunctionName()))
                .andExpect(jsonPath("$.content[0].variableName").value(issue1.getVariableName()))
                .andExpect(jsonPath("$.content[0].message").value(issue1.getMessage()))
                .andExpect(jsonPath("$.content[0].status").value(issue1.getStatus().name()))
                .andExpect(jsonPath("$.content[0].action").value(issue1.getAction().name()))
                .andExpect(jsonPath("$.content[0].createdBy").value(issue1.getCreatedBy()))
                .andExpect(jsonPath("$.content[0].modifiedBy").value(issue1.getModifiedBy()))
                .andExpect(jsonPath("$.content[0].assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.content[0].assignTo.displayName").value(currentUser.getDisplayName()))
                .andExpect(jsonPath("$.content[0].assignTo.email").value(currentUser.getEmail()))
                .andExpect(jsonPath("$.content[0].assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.content[0].ruleInformation.id").value(ruleInformation.getId().toString()))
                .andExpect(jsonPath("$.content[0].ruleInformation.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.content[0].ruleInformation.ruleSetVersion").value(ruleInformation.getRuleSet().getVersion()))
                .andExpect(jsonPath("$.content[0].ruleInformation.ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.content[0].ruleInformation.category").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.content[0].ruleInformation.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.content[0].ruleInformation.name").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.content[0].ruleInformation.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.content[0].ruleInformation.priority").value(ruleInformation.getPriority().name()))
                .andExpect(jsonPath("$.content[0].ruleInformation.severity").value(ruleInformation.getSeverity().name()))
                .andExpect(jsonPath("$.content[0].ruleInformation.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.content[0].ruleInformation.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.content[0].ruleInformation.language").value(ruleInformation.getLanguage()))
                .andExpect(jsonPath("$.content[0].ruleInformation.description").value(ruleInformation.getDescription()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void searchIssue_ScanTaskIsEmpty_ReturnEmptyContent() throws Exception {
        log.info("[searchIssue_ScanTaskIsEmpty_ReturnEmptyContent]");
        SearchIssueRequest searchIssueRequest = SearchIssueRequest.builder().seq("1").build();
        mockMvc.perform(post("/api/issue_service/v2/search_issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(searchIssueRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }


    @Test
    @WithMockCustomUser(adminUsername)
    void importIssueToScanTask_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[importIssueToScanTask_ScanTaskNotFound_ThrowAppException]");
        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(true);
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/issues", scanTaskId).file(file).param("file_checksum", "dummyChecksum"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

//    @Test
//    @WithMockCustomUser(adminUsername)
//    void importIssueToScanTask_ValidImportResult_ShouldSuccess() throws Exception {
//        log.info("[importIssueToScanTask_ValidImportResult_ShouldSuccess]");
//        Path tempFilePath = Files.createTempFile("helloWorld", ".v");
//        String content = "{\"id\":\"11111111-1111-1111-1112-111111111113\"}";
//        File tempFile = tempFilePath.toFile();
//        FileUtils.writeStringToFile(tempFile, content, StandardCharsets.UTF_8);
//        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, FileUtils.readFileToByteArray(tempFile));
//        FileInfo fileInfo = FileInfo.builder().id(UUID.randomUUID()).build();
//        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(true);
//        when(scanTaskService.findById(any())).thenReturn(Optional.of(scanTask));
//        when(issueService.processScanResultFile(any(), any(), any())).thenReturn(new ImportScanResultRequest());
//        when(ruleService.getRuleSetFromImportScanResultRequest(any())).thenReturn(new ArrayList<>());
//        when(issueService.importIssueToScanTask(any(), any(), any())).thenReturn(Collections.singletonList(issue1));
//        when(issueService.saveImportIssueResponseToFile(any(), any())).thenReturn(fileInfo);
//
//        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/issues", "11111111-1111-1111-1112-111111111113").file(file).param("file_checksum", "dummyChecksum"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.issues[0].id").value(issue1.getId().toString()))
//                .andExpect(jsonPath("$.issues[0].issueKey").value(issue1.getIssueKey()))
//                .andExpect(jsonPath("$.issues[0].seq").value(issue1.getSeq()))
//                .andExpect(jsonPath("$.issues[0].issueCategory").value(ruleInformation.getCategory()))
//                .andExpect(jsonPath("$.issues[0].ruleSet").value(ruleInformation.getRuleSet().getName()))
//                .andExpect(jsonPath("$.issues[0].vulnerable").value(ruleInformation.getVulnerable()))
//                .andExpect(jsonPath("$.issues[0].certainty").value(ruleInformation.getCertainty().name()))
//                .andExpect(jsonPath("$.issues[0].issueCode").value(issue1.getIssueCode()))
//                .andExpect(jsonPath("$.issues[0].severity").value(issue1.getSeverity().name()))
//                .andExpect(jsonPath("$.issues[0].likelihood").value(ruleInformation.getLikelihood().name()))
//                .andExpect(jsonPath("$.issues[0].remediationCost").value(ruleInformation.getRemediationCost().name()))
//                .andExpect(jsonPath("$.issues[0].relativePath").value(issue1.getFilePath()))
//                .andExpect(jsonPath("$.issues[0].scanFilePath").value(issue1.getFilePath()))
//                .andExpect(jsonPath("$.issues[0].lineNo").value(issue1.getLineNo()))
//                .andExpect(jsonPath("$.issues[0].columnNo").value(issue1.getColumnNo()))
//                .andExpect(jsonPath("$.issues[0].functionName").value(issue1.getFunctionName()))
//                .andExpect(jsonPath("$.issues[0].variableName").value(issue1.getVariableName()))
//                .andExpect(jsonPath("$.issues[0].message").value(issue1.getMessage()))
//                .andExpect(jsonPath("$.issues[0].ruleInformationId").value(ruleInformation.getId().toString()));
//    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importIssueToScanTask_NotArchive_ShouldSuccess() throws Exception {
        log.info("[importIssueToScanTask_NotArchive_ShouldSuccess]");
        Path tempFilePath = Files.createTempFile("helloWorld", ".v");
        String content = "{\"id\":\"11111111-1111-1111-1112-111111111113\"}";
        File tempFile = tempFilePath.toFile();
        FileUtils.writeStringToFile(tempFile, content, StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, FileUtils.readFileToByteArray(tempFile));
        FileInfo fileInfo = FileInfo.builder().id(UUID.randomUUID()).build();
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(true);
        when(scanTaskService.findById(any())).thenReturn(Optional.of(scanTask));
        when(issueService.processScanResultFile(any(), any(), any())).thenReturn(new ImportScanResultRequest());
        when(ruleService.getRuleSetFromImportScanResultRequest(any())).thenReturn(new ArrayList<>());
        when(issueService.importIssueToScanTask(any(), any(), any())).thenReturn(Collections.singletonList(issue1));
        when(issueService.saveImportIssueResponseToFile(any(), any())).thenReturn(fileInfo);
        doNothing().when(importService).syncImportScanResult(any(), any(),any(),any());
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/issues", "11111111-1111-1111-1112-111111111113").file(file).param("file_checksum", "dummyChecksum"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void retrieveScanResultFromScanSummary_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[retrieveScanResultFromScanSummary_ScanTaskNotFound_ThrowAppException]");
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/scan_summary/scan_result", scanTaskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser
    void retrieveScanResultFromScanSummary_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[retrieveScanResultFromScanSummary_InsufficientPrivilege_ThrowAppException]");
        when(scanTaskService.findById(issue3.getScanTask().getId())).thenReturn(Optional.of(issue3.getScanTask()));
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/scan_summary/scan_result", scanTaskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void asyncImportIssueToScanTask_CheckSumIsNotIntegrity_ThrowAppException() throws Exception {
        log.info("[asyncImportIssueToScanTask_CheckSumIsNotIntegrity_ThrowAppException]");
        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(false);
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/issues_async", scanTaskId).file(file).param("file_checksum", "dummyChecksum"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_CONFLICT))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_INCONSISTENT));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void asyncImportIssueToScanTask_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[asyncImportIssueToScanTask_ScanTaskNotFound_ThrowAppException]");
        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(true);
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/issues_async", scanTaskId).file(file).param("file_checksum", "dummyChecksum"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importIssueDiff_FixedIssueCheckSumIsNotIntegrity_ThrowAppException() throws Exception {
        log.info("[importIssueDiff_FixedIssue_CheckSumIsNotIntegrity_ThrowAppException]");
        MockMultipartFile file = new MockMultipartFile("fixed_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(false);
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/{baselineId}/issue_diff", scanTaskId, baselineScanTaskId).file(file).param("fixed_issue_checksum", "dummyChecksum"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_CONFLICT))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_INCONSISTENT));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importIssueDiff_NewIssueCheckSumIsNotIntegrity_ThrowAppException() throws Exception {
        log.info("[importIssueDiff_NewIssueCheckSumIsNotIntegrity_ThrowAppException]");
        MockMultipartFile file = new MockMultipartFile("new_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(false);
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/{baselineId}/issue_diff", scanTaskId, baselineScanTaskId).file(file).param("new_issue_checksum", "dummyChecksum"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_CONFLICT))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_INCONSISTENT));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importIssueDiff_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[importIssueDiff_ScanTaskNotFound_ThrowAppException]");
        MockMultipartFile file1 = new MockMultipartFile("fixed_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("new_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(false);
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/{baselineId}/issue_diff", scanTaskId, baselineScanTaskId).file(file1).file(file2))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importIssueDiff_BaselineScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[importIssueDiff_BaselineScanTaskNotFound_ThrowAppException]");
        MockMultipartFile file1 = new MockMultipartFile("fixed_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("new_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(false);
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(scanTaskService.findById(baselineScanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/{baselineId}/issue_diff", scanTaskId, baselineScanTaskId).file(file1).file(file2))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void asyncImportIssueDiff_FixedIssueCheckSumIsNotIntegrity_ThrowAppException() throws Exception {
        log.info("[asyncIimportIssueDiff_FixedIssue_CheckSumIsNotIntegrity_ThrowAppException]");
        MockMultipartFile file = new MockMultipartFile("fixed_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(false);
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/{baselineId}/issue_diff_async", scanTaskId, baselineScanTaskId).file(file).param("fixed_issue_checksum", "dummyChecksum"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_CONFLICT))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_INCONSISTENT));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void asyncImportIssueDiff_NewIssueCheckSumIsNotIntegrity_ThrowAppException() throws Exception {
        log.info("[asyncImportIssueDiff_NewIssueCheckSumIsNotIntegrity_ThrowAppException]");
        MockMultipartFile file = new MockMultipartFile("new_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(false);
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/{baselineId}/issue_diff_async", scanTaskId, baselineScanTaskId).file(file).param("new_issue_checksum", "dummyChecksum"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_CONFLICT))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_INCONSISTENT));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void asyncImportIssueDiff_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[asyncImportIssueDiff_ScanTaskNotFound_ThrowAppException]");
        MockMultipartFile file1 = new MockMultipartFile("fixed_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("new_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(false);
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/{baselineId}/issue_diff_async", scanTaskId, baselineScanTaskId).file(file1).file(file2))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void asyncImportIssueDiff_BaselineScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[asyncImportIssueDiff_BaselineScanTaskNotFound_ThrowAppException]");
        MockMultipartFile file1 = new MockMultipartFile("fixed_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("new_issue_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        when(fileService.getLocalTempFile(any(MultipartFile.class))).thenReturn(new File("test.txt"));
        when(fileService.checkIntegrityWithCrc32(any(File.class), anyString())).thenReturn(false);
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(scanTaskService.findById(baselineScanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(multipart("/api/issue_service/v2/scan_task/{id}/{baselineId}/issue_diff_async", scanTaskId, baselineScanTaskId).file(file1).file(file2))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getIssueByIds_IssueIdNotEmpty_Success() throws Exception {
        log.info("[getIssueByIds_IssueIdNotEmpty_Success]");
        List<Issue> issueList = Collections.singletonList(issue1);
        ListIssueRequest listIssueRequest = ListIssueRequest.builder().issueIds(Arrays.asList(UUID.randomUUID(), UUID.randomUUID())).build();
        when(issueService.findIssuesByIds(listIssueRequest.getIssueIds())).thenReturn(issueList);
        mockMvc.perform(post("/api/issue_service/v2/issues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(listIssueRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(issue1.getId().toString()))
                .andExpect(jsonPath("$.[0].issueKey").value(issue1.getIssueKey()))
                .andExpect(jsonPath("$.[0].seq").value(issue1.getSeq()))
                .andExpect(jsonPath("$.[0].issueCategory").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.[0].ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.[0].vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.[0].certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.[0].issueCode").value(issue1.getIssueCode()))
                .andExpect(jsonPath("$.[0].issueName").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.[0].severity").value(issue1.getSeverity().name()))
                .andExpect(jsonPath("$.[0].likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.[0].remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.[0].relativePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.[0].scanFilePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.[0].lineNo").value(issue1.getLineNo()))
                .andExpect(jsonPath("$.[0].columnNo").value(issue1.getColumnNo()))
                .andExpect(jsonPath("$.[0].functionName").value(issue1.getFunctionName()))
                .andExpect(jsonPath("$.[0].variableName").value(issue1.getVariableName()))
                .andExpect(jsonPath("$.[0].message").value(issue1.getMessage()))
                .andExpect(jsonPath("$.[0].status").value(issue1.getStatus().name()))
                .andExpect(jsonPath("$.[0].action").value(issue1.getAction().name()))
                .andExpect(jsonPath("$.[0].createdBy").value(issue1.getCreatedBy()))
                .andExpect(jsonPath("$.[0].modifiedBy").value(issue1.getModifiedBy()))
                .andExpect(jsonPath("$.[0].assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.[0].assignTo.displayName").value(currentUser.getDisplayName()))
                .andExpect(jsonPath("$.[0].assignTo.email").value(currentUser.getEmail()))
                .andExpect(jsonPath("$.[0].assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.[0].ruleInformation.id").value(ruleInformation.getId().toString()))
                .andExpect(jsonPath("$.[0].ruleInformation.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.[0].ruleInformation.ruleSetVersion").value(ruleInformation.getRuleSet().getVersion()))
                .andExpect(jsonPath("$.[0].ruleInformation.ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.[0].ruleInformation.category").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.[0].ruleInformation.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.[0].ruleInformation.name").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.[0].ruleInformation.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.[0].ruleInformation.priority").value(ruleInformation.getPriority().name()))
                .andExpect(jsonPath("$.[0].ruleInformation.severity").value(ruleInformation.getSeverity().name()))
                .andExpect(jsonPath("$.[0].ruleInformation.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.[0].ruleInformation.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.[0].ruleInformation.language").value(ruleInformation.getLanguage()))
                .andExpect(jsonPath("$.[0].ruleInformation.description").value(ruleInformation.getDescription()));
    }

    @Test
    @WithMockCustomUser()
    void getIssueByIds_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[getIssueByIds_InsufficientPrivilege_ThrowAppException]");
        List<Issue> issueList = Collections.singletonList(issue3);
        ListIssueRequest listIssueRequest = ListIssueRequest.builder().issueIds(Arrays.asList(UUID.randomUUID(), UUID.randomUUID())).build();
        when(issueService.findIssuesByIds(listIssueRequest.getIssueIds())).thenReturn(issueList);
        mockMvc.perform(post("/api/issue_service/v2/issues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(listIssueRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser()
    void getIssueByIdAndChecksum_IssueNotFound_ThrowAppException() throws Exception {
        log.info("[getIssueByIdAndChecksum_IssueNotFound_ThrowAppException]");

        when(issueService.findById(issue1.getId())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/issue_service/v2/issue/{id}/issue_trace_set//{checksum}", issue1.getId(), "1234")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser
    void getAllIssueDiffInScanTask_Success() throws Exception {
        log.info("[getAllIssueDiffInScanTask_Success]");
        List<IssueDiff> expectedIssueDiffs = Arrays.asList(
                IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(issue1).issueKey(issue1.getIssueKey()).type(IssueDiff.Type.FIXED).build(),
                IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(issue2).issueKey(issue2.getIssueKey()).type(IssueDiff.Type.NEW_PATH).build());
        when(this.scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(this.issueService.getIssueDiff(any(ScanTask.class))).thenReturn(expectedIssueDiffs);
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/issue_diff", scanTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath(CommonUtil.formatString("$[?(@.issueId=='{}')].type",issue1.getId())).value(IssueDiff.Type.FIXED.name()))
                .andExpect(jsonPath(CommonUtil.formatString("$[?(@.issueId=='{}')].scanTaskId",issue1.getId())).value(issue1.getScanTask().getId().toString()))
                .andExpect(jsonPath(CommonUtil.formatString("$[?(@.issueId=='{}')].type",issue2.getId())).value(IssueDiff.Type.NEW_PATH.name()))
                .andExpect(jsonPath(CommonUtil.formatString("$[?(@.issueId=='{}')].scanTaskId",issue2.getId())).value(issue2.getScanTask().getId().toString()));
    }

    @Test
    @WithMockCustomUser
    void getAllIssueDiffInScanTask_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[getAllIssueDiffInScanTask_InsufficientPrivilege_ThrowAppException]");
        List<IssueDiff> expectedIssueDiffs = Arrays.asList(
                IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(issue1).issueKey(issue1.getIssueKey()).type(IssueDiff.Type.FIXED).build(),
                IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(issue2).issueKey(issue2.getIssueKey()).type(IssueDiff.Type.NEW_PATH).build());
        when(this.scanTaskService.findById(scanTask2.getId())).thenReturn(Optional.of(scanTask2));
        when(this.issueService.getIssueDiff(any(ScanTask.class))).thenReturn(expectedIssueDiffs);
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/issue_diff", scanTask2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser
    void getAllIssueDiffInScanTask_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[getAllIssueDiffInScanTask_InsufficientPrivilege_ThrowAppException]");
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/issue_service/v2/scan_task/{id}/issue_diff", scanTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser
    void calcIssueStatistics_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[calcIssueStatistics_ScanTaskNotFound_ThrowAppException]");
        IssueStatisticsRequest issueStatisticsRequest = IssueStatisticsRequest.builder()
                .scanTaskId(UUID.randomUUID())
                .build();
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/issue_service/v2/issue_statistics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(issueStatisticsRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser
    void getIssueTraces_IssueNotFound_ThrowAppException() throws Exception {
        log.info("[getIssueTraces_IssueNotFound_ThrowAppException]");
        when(issueService.findById(issue1.getId())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/issue_service/v2/issue/{id}/traces", issue1.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser
    void getIssueTraces_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[getIssueTraces_InsufficientPrivilege_ThrowAppException]");
        when(issueService.findById(issue3.getId())).thenReturn(Optional.of(issue3));
        mockMvc.perform(get("/api/issue_service/v2/issue/{id}/traces", issue3.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

}
