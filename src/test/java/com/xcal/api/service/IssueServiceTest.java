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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.xcal.api.config.AppProperties;
import com.xcal.api.dao.*;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.CompareIssueObject;
import com.xcal.api.model.dto.IssueDto;
import com.xcal.api.model.dto.v3.RuleListResponseDto;
import com.xcal.api.model.payload.*;
import com.xcal.api.repository.IssueDiffRepository;
import com.xcal.api.repository.IssueRepository;
import com.xcal.api.repository.IssueTraceRepository;
import com.xcal.api.repository.ProjectRepository;
import com.xcal.api.service.v3.RuleServiceV3;
import com.xcal.api.util.CsfReader;
import com.xcal.api.util.CsfReaderV08;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class IssueServiceTest {

    private RuleService ruleService;
    private UserService userService;
    private ScanTaskService scanTaskService;
    private ProjectService projectService;
    private FileService fileService;
    private EmailService emailService;
    private SettingService settingService;
    private IssueRepository issueRepository;

    private IssueTraceRepository issueTraceRepository;
    private IssueDiffRepository issueDiffRepository;
    private ProjectRepository projectRepository;

    private IssueService issueService;
    private AsyncJobService asyncJobService;
    private RuleServiceV3 ruleServiceV3;
    Tracer tracer;

    private IssueFileDao issueFileDao;
    private IssueStringDao issueStringDao;
    private IssueGroupDao issueGroupDao;
    private IssueDao issueDao;
    private ScanTaskDao scanTaskDao;

    private KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);

    private Span mockSpan;

    @NonNull
    private final ObjectMapper om = new ObjectMapper();
    private final ObjectMapper cborOm = new ObjectMapper(new CBORFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final User currentUser = User.builder().id(UUID.randomUUID()).username("user").displayName("testDispalyName").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();

    private final ScanEngine scanEngine = ScanEngine.builder().id(UUID.randomUUID()).name("scanEngineName").version("test scanEngineVersion").language("C++").provider("Test provider")
            .providerUrl("Test provider url").url("test url").build();

    private final ScanEngine scanEngineXcalibyte = ScanEngine.builder().id(UUID.randomUUID()).name("Xcalibyte").version("test scanEngineVersion").language("C++").provider("Test provider")
            .providerUrl("Test provider url").url("test url").build();
    private final RuleSet ruleSet = RuleSet.builder().id(UUID.randomUUID()).name("ruleset name").version("test ruleset version ").displayName("ruleset display name").scanEngine(scanEngine).
            language("C++").provider("test provider").providerUrl("test provider url ").build();

    private final RuleSet ruleSet4 = RuleSet.builder().id(UUID.randomUUID()).name("CERT").version("1").displayName("CERT").scanEngine(scanEngineXcalibyte).
            language("C++").provider("test provider").providerUrl("test provider url ").build();

    private final RuleInformation ruleInformation1 = RuleInformation.builder()
            .id(UUID.randomUUID())
            .ruleSet(ruleSet)
            .ruleCode("BUILTIN-NPD")
            .category("ROBUSTNESS")
            .vulnerable("NPD")
            .name("Null pointer dereference")
            .priority(RuleInformation.Priority.HIGH)
            .severity(RuleInformation.Severity.HIGH)
            .likelihood(RuleInformation.Likelihood.LIKELY)
            .remediationCost(RuleInformation.RemediationCost.LOW)
            .language("c,c++")
            .description("Address of a local variable (stack space) has been passed to the caller, causing illegal memory access")
            .build();
    private final RuleInformation ruleInformation2 = RuleInformation.builder()
            .id(UUID.randomUUID())
            .ruleSet(ruleSet)
            .ruleCode("BUILTIN-UIV")
            .category("ROBUSTNESS")
            .vulnerable("UIV")
            .name("Uninitialized variable")
            .priority(RuleInformation.Priority.MEDIUM)
            .severity(RuleInformation.Severity.MEDIUM)
            .language("c,c++")
            .description("Address of a local variable (stack space) has been passed to the caller, causing illegal memory access")
            .build();
    private final RuleInformation ruleInformation3 = RuleInformation.builder()
            .id(UUID.randomUUID())
            .ruleSet(ruleSet)
            .ruleCode("BUILTIN-AOB")
            .category("ROBUSTNESS")
            .vulnerable("AOB")
            .name("Array out of bound")
            .priority(RuleInformation.Priority.LOW)
            .severity(RuleInformation.Severity.LOW)
            .language("c,c++")
            .description("Address of a local variable (stack space) has been passed to the caller, causing illegal memory access")
            .build();

    private final RuleInformation ruleInformation4 = RuleInformation.builder()
            .id(UUID.randomUUID())
            .ruleSet(ruleSet4)
            .ruleCode("FIO42-C")
            .category("ROBUSTNESS")
            .vulnerable("NPD")
            .name("Null pointer dereference")
            .priority(RuleInformation.Priority.HIGH)
            .severity(RuleInformation.Severity.HIGH)
            .likelihood(RuleInformation.Likelihood.LIKELY)
            .remediationCost(RuleInformation.RemediationCost.LOW)
            .language("c,c++")
            .description("Address of a local variable (stack space) has been passed to the caller, causing illegal memory access")
            .build();

    private final Project project = Project.builder().id(UUID.randomUUID()).projectId("test_project_id").name("Test Project").build();
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

    private final List<SearchIssueRequest.IssueAttribute> searchAttrs = Collections.singletonList(SearchIssueRequest.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.PRIORITY.toString()).values(Arrays.asList("RANK_8", "RANK_19")).build());
    private final List<UUID> scanFileIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
    private final List<UUID> ruleInformationIds = Arrays.asList(UUID.fromString("11111111-1111-1111-1110-111111111113"), UUID.fromString("11111111-1111-1111-1110-111111111114"));
    private final ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PENDING).project(project).projectConfig(projectConfig).build();

    private final Issue issue1 = Issue.builder().id(UUID.randomUUID()).issueCode(ruleInformation1.getRuleCode()).scanTask(scanTask)
            .seq("00001")
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
            .ruleInformation(ruleInformation1)
            .build();
    private List<IssueTrace> issueTraces1;
    private IssueTrace issueTrace11;
    private final Issue issue2 = Issue.builder().id(UUID.randomUUID()).issueCode(ruleInformation2.getRuleCode()).scanTask(scanTask)
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
            .build();
    private List<IssueTrace> issueTraces2;
    private List<IssueTrace> issueTraces21;
    private final IssueTrace issueTrace211 = IssueTrace.builder().
            scanFile(ScanFile.builder().id(UUID.randomUUID()).projectRelativePath("src/protocol.h").storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/src/protocol.h")
                    .fileInfo(FileInfo.builder().noOfLines(100).fileSize(10000L).relativePath("src/protocol.h").build())
                    .build())
            .filePath("/share/scan/a140d21d-948d-454c-bb42-061d784b4016/src/core/ngx_palloc.c")
            .id(UUID.randomUUID())
            .issue(issue2)
            .seq(1)
            .lineNo(381)
            .columnNo(0)
            .complexity("3")
            .functionName("ngx_vslprintf")
            .variableName("buf")
            .message("Then block is taken")
            .build();
    private final IssueTrace issueTrace212 = IssueTrace.builder().
            scanFile(ScanFile.builder().id(UUID.randomUUID()).projectRelativePath("src/protocol.h").storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/src/protocol.h").build())
            .filePath("/share/scan/a140d21d-948d-454c-bb42-061d784b4016/src/core/ngx_palloc.c")
            .id(UUID.randomUUID())
            .issue(issue2)
            .seq(2)
            .lineNo(38)
            .columnNo(1)
            .complexity("10")
            .functionName("ngx_vslprintf")
            .variableName("buf")
            .message("Then block is taken")
            .build();
    private List<IssueTrace> issueTraces22;
    private final IssueTrace issueTrace221 = IssueTrace.builder()
            .filePath("/share/scan/a140d21d-948d-454c-bb42-061d784b4016/src/core/ngx_palloc.c")
            .id(UUID.randomUUID())
            .issue(issue2)
            .seq(1)
            .lineNo(38)
            .columnNo(1)
            .functionName("ngx_vslprintf")
            .variableName("buf")
            .message("Then block is taken")
            .build();
    private final Issue issue3 = Issue.builder().id(UUID.randomUUID()).issueCode(ruleInformation3.getRuleCode()).scanTask(scanTask)
            .seq("00003")
            .severity(Issue.Severity.HIGH)
            .lineNo(31)
            .columnNo(0)
            .functionName("ngx_vslprintf")
            .variableName("uiv")
            .message("dummy mesage 3")
            .status(Issue.Status.ACTIVE)
            .action(Issue.Action.CONFIRMED)
            .createdBy(currentUser.getUsername())
            .createdOn(new Date())
            .modifiedOn(new Date())
            .modifiedBy(currentUser.getUsername())
            .assignTo(currentUser)
            .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c@ngx_vslprintf@buf@NPD@@@fffffa68")
            .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c")
            .ruleInformation(ruleInformation3)
            .build();

    private List<Issue> issueList;
    ScanTask baselineScanTask = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PENDING).project(project).build();

    private final IssueDiff newIssue1 = IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(issue1).baselineScanTask(baselineScanTask).issueKey(issue1.getIssueKey()).type(IssueDiff.Type.NEW).build();
    private final IssueDiff newIssue3 = IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(issue3).baselineScanTask(baselineScanTask).issueKey(issue3.getIssueKey()).type(IssueDiff.Type.NEW).build();
    private final IssueDiff newIssuePath21 = IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(issueTrace211.getIssue()).baselineScanTask(baselineScanTask).issueKey(issueTrace211.getIssue().getIssueKey()).checksum(issueTrace211.getChecksum()).type(IssueDiff.Type.NEW_PATH).build();

    private final Issue baselineIssue1 = Issue.builder().id(UUID.randomUUID()).issueCode(ruleInformation1.getRuleCode()).scanTask(baselineScanTask)
            .seq("00001")
            .severity(Issue.Severity.HIGH)
            .lineNo(3)
            .columnNo(0)
            .functionName("ngx")
            .variableName("uiv")
            .message("dummy baseline mesage 1")
            .status(Issue.Status.ACTIVE)
            .action(Issue.Action.CONFIRMED)
            .createdBy(currentUser.getUsername())
            .createdOn(new Date())
            .modifiedOn(new Date())
            .modifiedBy(currentUser.getUsername())
            .assignTo(currentUser)
            .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b777/src/core/ngx_string.c@ngx_vslprintf@buf@NPD@@@fffffa68")
            .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b777/src/core/ngx_string.c")
            .ruleInformation(ruleInformation1)
            .build();
    private final Issue baselineIssue2 = Issue.builder().id(UUID.randomUUID()).issueCode(ruleInformation2.getRuleCode()).scanTask(baselineScanTask)
            .seq("00002")
            .severity(Issue.Severity.LOW)
            .lineNo(44)
            .columnNo(0)
            .functionName("ngx-123")
            .variableName("dummy")
            .message("dummy baseline mesage 2")
            .status(Issue.Status.ACTIVE)
            .action(Issue.Action.CRITICAL)
            .createdBy(currentUser.getUsername())
            .createdOn(new Date())
            .modifiedOn(new Date())
            .modifiedBy(currentUser.getUsername())
            .assignTo(currentUser)
            .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b417/src/core/ngx_string.c@ngx_vslprintf@buf@NPD@@@fffffa68")
            .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b417/src/core/ngx_string.c")
            .ruleInformation(ruleInformation2)
            .build();

    private final IssueDiff fixedIssue1 = IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(baselineIssue1).baselineScanTask(baselineScanTask).issueKey(baselineIssue1.getIssueKey()).type(IssueDiff.Type.FIXED).build();
    private final IssueDiff fixedIssuepath2 = IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(baselineIssue2).baselineScanTask(baselineScanTask).issueKey(baselineIssue2.getIssueKey()).type(IssueDiff.Type.FIXED_PATH).build();

    @BeforeEach
    void setUp() {
        ruleService = mock(RuleService.class);
        userService = mock(UserService.class);
        scanTaskService = mock(ScanTaskService.class);
        projectService = mock(ProjectService.class);
        emailService = mock(EmailService.class);
        settingService = mock(SettingService.class);
        AppProperties appProperties = mock(AppProperties.class);
        issueRepository = mock(IssueRepository.class);
        issueTraceRepository = mock(IssueTraceRepository.class);
        ScanFileService scanFileService = mock(ScanFileService.class);
        fileService = mock(FileService.class);
        issueDiffRepository = mock(IssueDiffRepository.class);
        projectRepository = mock(ProjectRepository.class);

        I18nService i18nService = mock(I18nService.class);
        asyncJobService = mock(AsyncJobService.class);
        ruleServiceV3 = mock(RuleServiceV3.class);
        tracer = mock(Tracer.class);
        Tracer.SpanBuilder builder = mock(Tracer.SpanBuilder.class);
        when(this.tracer.buildSpan(anyString())).thenReturn(builder);

        when(builder.withTag(anyString(), anyString())).thenReturn(builder);
        when(builder.withTag(anyString(), anyInt())).thenReturn(builder);
        when(builder.withTag(anyString(), anyLong())).thenReturn(builder);
        when(builder.asChildOf(any(Span.class))).thenReturn(builder);
        when(builder.asChildOf(any(SpanContext.class))).thenReturn(builder);
        mockSpan = mock(Span.class);
        when(builder.start()).thenReturn(mockSpan);
        when(this.tracer.activeSpan()).thenReturn(mockSpan);

        issueFileDao = mock(IssueFileDao.class);
        issueStringDao = mock(IssueStringDao.class);
        issueGroupDao = mock(IssueGroupDao.class);
        issueDao = mock(IssueDao.class);

        scanTaskDao = mock(ScanTaskDao.class);
        when(mockSpan.setTag(anyString(), anyString())).thenReturn(mockSpan);
        when(mockSpan.setTag(anyString(), anyInt())).thenReturn(mockSpan);
        when(mockSpan.setTag(anyString(), anyLong())).thenReturn(mockSpan);

        SpanContext spanContext=mock(SpanContext.class);
        when(spanContext.toSpanId()).thenReturn("1111-1111");
        when(mockSpan.context()).thenReturn(spanContext);

        projectConfig.setAttributes(attrs);

        when(i18nService.getI18nMessageByKey(any(), any(Locale.class))).thenReturn(new ArrayList<>());
        issueService = new IssueService(ruleService, userService, scanTaskService, projectService, emailService, settingService, appProperties, i18nService, issueRepository, issueTraceRepository, issueDiffRepository, projectRepository, scanFileService, fileService, asyncJobService,ruleServiceV3, tracer, issueFileDao, issueStringDao, issueGroupDao, issueDao, scanTaskDao, om, kafkaTemplate);
        issueService.saveBatchSize = 500;

        when(settingService.getEmailServerConfiguration()).thenReturn(AppProperties.Mail.builder()
                .host("smtpdm.aliyun.com")
                .port(465)
                .username("no-reply@xcalibyte.io")
                .password("xxxxxx")
                .protocol("smtp")
                .prefix("[XCALBYTE]")
                .starttls("false")
                .build());
        when(appProperties.getUiHost()).thenReturn("http://localhost");

        List<IssueAttribute> issue1Attrs = Collections.singletonList(IssueAttribute.builder().issue(issue1).name(VariableUtil.IssueAttributeName.RULE_CODE).value(ruleInformation1.getRuleCode()).build());
        issue1.setAttributes(issue1Attrs);

        List<IssueAttribute> issue2Attrs = Arrays.asList(
                IssueAttribute.builder().issue(issue2).name(VariableUtil.IssueAttributeName.COMPLEXITY_RATE).value("0.4").build(),
                IssueAttribute.builder().issue(issue2).name(VariableUtil.IssueAttributeName.COMPLEXITY).value("31").build(),
                IssueAttribute.builder().issue(issue2).name(VariableUtil.IssueAttributeName.RULE_CODE).value(ruleInformation2.getRuleCode()).build(),
                IssueAttribute.builder().issue(issue2).name(VariableUtil.IssueAttributeName.CERTAINTY).value("M").build()
        );
        issue2.setAttributes(issue2Attrs);

        issueTrace11 = IssueTrace.builder().
                scanFile(ScanFile.builder().id(UUID.randomUUID()).projectRelativePath("src/protocol.h").storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/src/protocol.h")
                        .fileInfo(FileInfo.builder().noOfLines(100).fileSize(10000L).relativePath("src/protocol.h").build())
                        .build())
                .filePath("/share/scan/a140d21d-948d-454c-bb42-061d784b4016/src/core/ngx_palloc.c")
                .id(UUID.randomUUID())
                .issue(issue1)
                .seq(1)
                .lineNo(381)
                .columnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("Then block is taken")
                .build();
        issueTraces1 = Collections.singletonList(issueTrace11);
        final String checksum1 = DigestUtils.md5Hex(issueTraces1.toString());
        issueTraces1.forEach(it -> it.setChecksum(checksum1));
        issue1.setIssueTraces(issueTraces1);

        issueTraces21 = Arrays.asList(issueTrace211, issueTrace212);
        final String checksum21 = DigestUtils.md5Hex(issueTraces21.toString());
        issueTraces21.forEach(it -> it.setChecksum(checksum21));
        issueTraces22 = Collections.singletonList(issueTrace221);
        final String checksum22 = DigestUtils.md5Hex(issueTraces22.toString());
        issueTraces22.forEach(it -> it.setChecksum(checksum22));

        issueTraces2 = Arrays.asList(issueTrace211, issueTrace212, issueTrace221);

        issue2.setIssueTraces(issueTraces2);

        issueList = Arrays.asList(issue1, issue2);

        when(issueRepository.findByIdWithWholeObject(issue1.getId())).thenReturn(Optional.of(issue1));
        when(issueRepository.findByIdWithWholeObject(issue2.getId())).thenReturn(Optional.of(issue2));
        when(issueRepository.findByIdWithWholeObject(issue3.getId())).thenReturn(Optional.of(issue3));
        when(issueRepository.findByIdWithWholeObject(baselineIssue1.getId())).thenReturn(Optional.of(baselineIssue1));
        when(issueRepository.findByIdWithWholeObject(baselineIssue2.getId())).thenReturn(Optional.of(baselineIssue2));
    }

    @Test
    void processScanResultFile_WithCorrectJsonFile_ShouldSuccess() throws IOException, AppException {
        File tempFile = Files.createTempFile("hello", ".v").toFile();
        String content = "{\"v\": \"1\"}";
        FileUtils.writeStringToFile(tempFile, content, StandardCharsets.UTF_8);
        ImportScanResultRequest importScanResultRequest = issueService.processScanResultFile(tempFile, scanTask, currentUser.getUsername());
        assertEquals("1", importScanResultRequest.getVersion());
    }

    @Test
    void processScanResultFile_InvalidContentAndJsonSuffixFile_ShouldThrowException() throws IOException {
        File tempFile = Files.createTempFile("hello", ".v").toFile();
        String content = "{\"v\"}";
        FileUtils.writeStringToFile(tempFile, content, StandardCharsets.UTF_8);
        assertThrows(AppException.class, () -> issueService.processScanResultFile(tempFile, scanTask, currentUser.getUsername()));
    }

    @Test
    void processScanResultFile_EmptyJsonSuffixFile_ShouldThrowException() throws IOException {
        File tempFile = Files.createTempFile("hello", ".v").toFile();
        FileUtils.writeStringToFile(tempFile, "", StandardCharsets.UTF_8);
        assertThrows(AppException.class, () -> issueService.processScanResultFile(tempFile, scanTask, currentUser.getUsername()));
    }

    @Test
    void processScanResultFile_WithCorrectCborFile_ShouldSuccess() throws IOException, AppException {
        File tempFile = Files.createTempFile("hello", ".cbor").toFile();
        ImportScanResultRequest scanResult = ImportScanResultRequest.builder().version("1").build();
        byte[] cborData = cborOm.writeValueAsBytes(scanResult);
        FileUtils.writeByteArrayToFile(tempFile, cborData);
        ImportScanResultRequest importScanResultRequest = issueService.processScanResultFile(tempFile, scanTask, currentUser.getUsername());
        assertEquals("1", importScanResultRequest.getVersion());
    }

    @Test
    void processScanResultFile_InvalidContentAndCborSuffixFile_ShouldThrowException() throws IOException {
        File tempFile = Files.createTempFile("hello", ".cbor").toFile();
        String content = "{\"v\": \"1\"}";
        FileUtils.writeStringToFile(tempFile, content, StandardCharsets.UTF_8);
        assertThrows(AppException.class, () -> issueService.processScanResultFile(tempFile, scanTask, currentUser.getUsername()));
    }

    @Test
    void processScanResultFile_WithMoreThanOneFileInGZIP_ShouldThrowException() throws IOException, AppException {
        File file = Files.createTempFile("hello", ".tgz").toFile();
        File tempFolder = Files.createTempDirectory("hello").toFile();
        Files.createTempFile(tempFolder.toPath(), "hello1", "v").toFile();
        Files.createTempFile(tempFolder.toPath(), "hello2", "v").toFile();
        when(fileService.decompressFile(any(), any(), any())).thenReturn(tempFolder);

        assertThrows(AppException.class, () -> issueService.processScanResultFile(file, scanTask, currentUser.getUsername()));
    }

    @Test
    void processScanResultFile_WithValidFileInGZIP_ShouldSuccess() throws IOException, AppException {
        File file = Files.createTempFile("hello", ".tgz").toFile();
        File tempFolder = Files.createTempDirectory("hello").toFile();

        File tempFile = Files.createTempFile(tempFolder.toPath(), "hello", ".cbor").toFile();
        ImportScanResultRequest scanResult = ImportScanResultRequest.builder().version("1").build();
        byte[] cborData = cborOm.writeValueAsBytes(scanResult);
        FileUtils.writeByteArrayToFile(tempFile, cborData);

        when(fileService.decompressFile(any(), any(), any())).thenReturn(tempFolder);

        ImportScanResultRequest importScanResultRequest = issueService.processScanResultFile(file, scanTask, currentUser.getUsername());
        assertEquals("1", importScanResultRequest.getVersion());
    }

    @Test
    void processScanResultFile_WithMoreThanOneFileInZIP_ShouldThrowException() throws IOException, AppException {
        File file = Files.createTempFile("hello1", ".zip").toFile();
        File tempFolder = Files.createTempDirectory("hello").toFile();
        Files.createTempFile(tempFolder.toPath(), "hello1", "v").toFile();
        Files.createTempFile(tempFolder.toPath(), "hello2", "v").toFile();
        when(fileService.decompressFile(any(), any(), any())).thenReturn(tempFolder);

        assertThrows(AppException.class, () -> issueService.processScanResultFile(file, scanTask, currentUser.getUsername()));
    }

    @Test
    void processScanResultFile_WithValidFileInZIP_ShouldSuccess() throws IOException, AppException {
        File file = Files.createTempFile("hello", ".zip").toFile();
        File tempFolder = Files.createTempDirectory("hello").toFile();

        File tempFile = Files.createTempFile(tempFolder.toPath(), "hello", ".cbor").toFile();
        ImportScanResultRequest scanResult = ImportScanResultRequest.builder().version("1").build();
        byte[] cborData = cborOm.writeValueAsBytes(scanResult);
        FileUtils.writeByteArrayToFile(tempFile, cborData);

        when(fileService.decompressFile(any(), any(), any())).thenReturn(tempFolder);

        ImportScanResultRequest importScanResultRequest = issueService.processScanResultFile(file, scanTask, currentUser.getUsername());
        assertEquals("1", importScanResultRequest.getVersion());
    }

    @Test
    void processScanResultFile_WithInvalidSuffixFile_ShouldThrowException() throws IOException {
        File tempFile = Files.createTempFile("hello", ".xxx").toFile();
        assertThrows(AppException.class, () -> issueService.processScanResultFile(tempFile, scanTask, currentUser.getUsername()));
    }

    @Test
    void importIssueToScanTaskTestSuccess() {
        log.info("[importIssueToScanTaskTestSuccess]");
        List<ScanFile> scanFileList = Arrays.asList(
                ScanFile.builder().storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c").fileInfo(FileInfo.builder().build()).build(),
                ScanFile.builder().storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/java.c").fileInfo(FileInfo.builder().build()).build()
        );
        ImportScanResultRequest.Issue.TracePath tracePath = ImportScanResultRequest.Issue.TracePath.builder()
                .fileId("fid1")
                .startLineNo(381)
                .endLineNo(0)
                .startColumnNo(21)
                .endColumnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("Then block is taken")
                .build();
        ImportScanResultRequest.Issue requestIssue = ImportScanResultRequest.Issue.builder().key("/share/scan/22d11179-dda6-45fd-848d-556ec98c25d1/src/core/ngx_string.c@ngx_vslprintf@last@AOB@@@f6")
                .ruleSet(ruleSet.getName()).ruleCode(ruleInformation1.getRuleCode()).fileId("fid1").certainty("D")
                .tracePaths(Collections.singletonList(tracePath)).build();
        Issue issue = Issue.builder().issueKey("/share/scan/22d11179-dda6-45fd-848d-556ec98c25d1/src/core/ngx_string.c@ngx_vslprintf@last@AOB@@@f6").build();
        ImportScanResultRequest importScanResultRequest = ImportScanResultRequest.builder()
                .ruleSets(Collections.singletonList(ImportScanResultRequest.
                        RuleSet.builder().ruleSet(ruleSet.getName()).ruleSetVersion(ruleSet.getVersion()).build()))
                .engine(scanEngine.getName())
                .engineVersion(scanEngine.getVersion())
                .issues(Collections.singletonList(requestIssue))
                .fileInfos(Collections.singletonList(ImportScanResultRequest.FileInfo.builder().fileId("fid1").path("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c").build()))
                .build();
        when(scanTaskService.findScanFileByScanTask(scanTask)).thenReturn(scanFileList);
        when(ruleService.getRuleInformation(scanEngine.getName(), scanEngine.getVersion(), null, null)).thenReturn(Collections.singletonList(ruleInformation1));
        when(issueRepository.saveAll(any())).thenReturn(Collections.singletonList(issue));
        doNothing().when(issueRepository).flush();
        List<Issue> resultIssues = issueService.importIssueToScanTask(scanTask, importScanResultRequest, currentUser.getUsername());
        assertEquals(requestIssue.getKey(), resultIssues.get(0).getIssueKey());
        assertEquals(ruleInformation1, resultIssues.get(0).getRuleInformation());
        assertEquals(ruleInformation1.getRuleCode(), resultIssues.get(0).getIssueCode());
        assertEquals(scanTask, resultIssues.get(0).getScanTask());
        assertEquals("00001", resultIssues.get(0).getSeq());
        assertEquals(ruleInformation1.getSeverity().name(), resultIssues.get(0).getSeverity().name());
        assertEquals(scanFileList.get(0), resultIssues.get(0).getScanFile());
        assertEquals(Issue.Status.ACTIVE, resultIssues.get(0).getStatus());
        assertEquals(Issue.Action.PENDING, resultIssues.get(0).getAction());
        assertEquals(currentUser.getUsername(), resultIssues.get(0).getCreatedBy());
        assertEquals(currentUser.getUsername(), resultIssues.get(0).getModifiedBy());
    }

    @Test
    void retrieveIssuesFromImportScanResultRequestTestSuccess() {
        log.info("[retrieveIssuesFromImportScanResultRequestTestSuccess]");
        String scanEngineName = "scanEngineName";
        String scanEngineLanguage = "C++";
        String scanEngineProvider = "Test provider";
        String scanEngineProviderUrl = "Test provider url";
        String scanEngineUrl = "test url";
        String scanEngineVersion = "test scanEngineVerion";
        ScanEngine scanEngine = ScanEngine.builder().name(scanEngineName).version(scanEngineVersion).language(scanEngineLanguage).provider(scanEngineProvider)
                .providerUrl(scanEngineProviderUrl).url(scanEngineUrl).build();
        UUID ruleSetId = UUID.fromString("11111111-1111-1111-1110-111111111110");
        String ruleSetName = "ruleset name";
        String ruleSetDisplayName = "ruleset display name";
        String ruleSetLanguage = "C++";
        String ruleSetProvide = "test provider";
        String ruleSetProvideUrl = "test provider url ";
        String ruleSetVersion = "test ruleset vcersion ";
        RuleSet ruleSet = RuleSet.builder().id(ruleSetId).name(ruleSetName).version(ruleSetVersion).displayName(ruleSetDisplayName).scanEngine(scanEngine).
                language(ruleSetLanguage).provider(ruleSetProvide).providerUrl(ruleSetProvideUrl).build();
        UUID ruleInformationId = UUID.fromString("11111111-1111-1111-1111-111111111110");
        String ruleInformationName = "ruleInformationName";
        String ruleCode = "test rule code";
        String category = "test category";
        RuleInformation.Certainty certainty = RuleInformation.Certainty.D;
        RuleInformation.Likelihood likelihood = RuleInformation.Likelihood.LIKELY;
        RuleInformation.Priority priority = RuleInformation.Priority.HIGH;
        RuleInformation.Severity severity = RuleInformation.Severity.HIGH;
        String vulnerable = "test vulnerable";
        String ruleInformationLanguage = "C++";
        RuleInformation ruleInformation = RuleInformation.builder().id(ruleInformationId).name(ruleInformationName).ruleCode(ruleCode).ruleSet(ruleSet).category(category).severity(severity).
                certainty(certainty).likelihood(likelihood).priority(priority).vulnerable(vulnerable).language(ruleInformationLanguage).remediationCost(RuleInformation.RemediationCost.HIGH).build();
        ImportScanResultRequest.FileInfo fileInfo1 = ImportScanResultRequest.FileInfo.builder().fileId("fid1").path("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c").build();
        ImportScanResultRequest.FileInfo fileInfo2 = ImportScanResultRequest.FileInfo.builder().fileId("fid2").path("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/java.c").build();
        FileInfo fileInfo_1 = FileInfo.builder().id(UUID.randomUUID()).relativePath(fileInfo1.getPath()).build();
        FileInfo fileInfo_2 = FileInfo.builder().id(UUID.randomUUID()).relativePath(fileInfo2.getPath()).build();
        ScanFile scanFile1 = ScanFile.builder().fileInfo(fileInfo_1).storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c").build();
        ScanFile scanFile2 = ScanFile.builder().fileInfo(fileInfo_2).storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/java.c").build();
        List<ScanFile> scanFileList = Arrays.asList(
                ScanFile.builder().storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c").build(),
                ScanFile.builder().storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/java.c").build()
        );
        ImportScanResultRequest.Issue.TracePath tracePath1 = ImportScanResultRequest.Issue.TracePath.builder()
                .fileId("fid1")
                .startLineNo(381)
                .endLineNo(0)
                .startColumnNo(21)
                .endColumnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("Then block is taken")
                .build();
        ImportScanResultRequest.Issue.TracePath tracePath2 = ImportScanResultRequest.Issue.TracePath.builder()
                .fileId("fid1")
                .startLineNo(388)
                .endLineNo(0)
                .startColumnNo(0)
                .endColumnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("Then block is taken")
                .build();
        ImportScanResultRequest.Issue requestIssue1 = ImportScanResultRequest.Issue.builder().key("/ngx_string.c@ngx_vslprintf@last@AOB@@@f6")
                .ruleSet(ruleSetName).ruleCode(ruleCode).fileId("fid1").certainty("D")
                .tracePaths(Collections.singletonList(tracePath1)).build();
        ImportScanResultRequest.Issue requestIssue2 = ImportScanResultRequest.Issue.builder().key("/ngx_string.c@ngx_vslprintf@last@AOB@@@f6")
                .ruleSet(ruleSetName).ruleCode(ruleCode).fileId("fid1").certainty("D")
                .tracePaths(Collections.singletonList(tracePath1)).build();
        ImportScanResultRequest.Issue requestIssue3 = ImportScanResultRequest.Issue.builder().key("/ngx_string.c@ngx_vslprintf@last@AOB@@@f6")
                .ruleSet(ruleSetName).ruleCode(ruleCode).fileId("fid1").certainty("D")
                .tracePaths(Arrays.asList(tracePath1, tracePath2)).build();
        ImportScanResultRequest.Issue requestIssue4 = ImportScanResultRequest.Issue.builder().key("/ngx_string.c@ngx_vslprintf@last@AOB@@@f6")
                .ruleSet(ruleSetName).ruleCode(ruleCode).fileId("fid1").certainty("D")
                .build();
        ImportScanResultRequest.Issue requestIssue5 = ImportScanResultRequest.Issue.builder().key("/ngx_string.c@ngx_vslprintf@last@AOB@@@f6")
                .ruleSet(ruleSetName).ruleCode(ruleCode).fileId("fid3").certainty("D")
                .build();
        ImportScanResultRequest.Issue requestIssue6 = ImportScanResultRequest.Issue.builder().key("/ngx_string.c@ngx_vslprintf@last@AOB@@@f6")
                .ruleSet(ruleSetName).ruleCode("NotExist").fileId("fid3").certainty("D")
                .build();
        ImportScanResultRequest.Issue requestIssue7 = ImportScanResultRequest.Issue.builder().key("/ngx_string.c@ngx_vslprintf@last@AOB@@@f6")
                .ruleSet("NotExist").ruleCode("NotExist").fileId("fid3").certainty("D")
                .build();
        ImportScanResultRequest importScanResultRequest = ImportScanResultRequest.builder()
                .engine(scanEngineName)
                .engineVersion(scanEngineVersion)
                .ruleSets(Collections.singletonList(ImportScanResultRequest.RuleSet.builder().ruleSet(ruleSetName).ruleSetVersion(ruleSetVersion).build()))
                .fileInfos(Arrays.asList(fileInfo1, fileInfo2))
                .issues(Arrays.asList(requestIssue1, requestIssue2, requestIssue3, requestIssue4, requestIssue5, requestIssue6, requestIssue7))
                .build();

        when(ruleService.getRuleInformation(scanEngineName, scanEngineVersion, null, null)).thenReturn(Collections.singletonList(ruleInformation));
        when(scanTaskService.findScanFileByScanTask(argThat(arg -> arg.getId() == scanTask.getId()))).thenReturn(Arrays.asList(scanFile1, scanFile2));
        List<Issue> resultIssues = issueService.retrieveIssuesFromImportScanResultRequest(scanTask, importScanResultRequest, currentUser.getUsername());
        assertEquals(requestIssue1.getKey(), resultIssues.get(0).getIssueKey());
        assertEquals(ruleInformation, resultIssues.get(0).getRuleInformation());
        assertEquals(ruleInformation.getRuleCode(), resultIssues.get(0).getIssueCode());
        assertEquals(scanTask, resultIssues.get(0).getScanTask());
        assertEquals("00001", resultIssues.get(0).getSeq());
        assertEquals(ruleInformation.getSeverity().name(), resultIssues.get(0).getSeverity().name());
        assertEquals(scanFileList.get(0).getId(), resultIssues.get(0).getScanFile().getId());
        assertEquals(Issue.Status.ACTIVE, resultIssues.get(0).getStatus());
        assertEquals(Issue.Action.PENDING, resultIssues.get(0).getAction());
        assertEquals(currentUser.getUsername(), resultIssues.get(0).getCreatedBy());
        assertEquals(currentUser.getUsername(), resultIssues.get(0).getModifiedBy());
    }

    @Test
    void retrieveRuleInformationMapTestSuccess() {
        log.info("[retrieveRuleInformationMapTestSuccess]");
        String scanEngineName = "scanEngineName";
        String scanEngineLanguage = "C++";
        String scanEngineProvider = "Test provider";
        String scanEngineProviderUrl = "Test provider url";
        String scanEngineUrl = "test url";
        String scanEngineVerion = "test scanEngineVerion";
        ScanEngine scanEngine = ScanEngine.builder().name(scanEngineName).version(scanEngineVerion).language(scanEngineLanguage).provider(scanEngineProvider)
                .providerUrl(scanEngineProviderUrl).url(scanEngineUrl).build();

        UUID ruleSetId = UUID.fromString("11111111-1111-1111-1110-111111111110");
        String ruleSetName = "ruleset name";
        String ruleSetDisplayname = "ruleset display name";
        String ruleSetLanguage = "C++";
        String ruletSetProvide = "test provider";
        String ruleSetProvideUrl = "test provider url ";
        String ruleSetVersion = "test ruleset vcersion ";
        RuleSet ruleSet = RuleSet.builder().id(ruleSetId).name(ruleSetName).version(ruleSetVersion).displayName(ruleSetDisplayname).scanEngine(scanEngine).
                language(ruleSetLanguage).provider(ruletSetProvide).providerUrl(ruleSetProvideUrl).build();

        UUID ruleInformationId = UUID.fromString("11111111-1111-1111-1111-111111111110");
        String ruleInformationName = "ruleInformationName";
        String ruleCode = "test rule code";
        String category = "test category";
        RuleInformation.Certainty certainty = RuleInformation.Certainty.D;
        RuleInformation.Likelihood likelihood = RuleInformation.Likelihood.LIKELY;
        RuleInformation.Priority priority = RuleInformation.Priority.HIGH;
        RuleInformation.Severity severity = RuleInformation.Severity.HIGH;
        String vulnerable = "test vulnerable";
        String ruleInformationLanguage = "C++";
        RuleInformation ruleInformation = RuleInformation.builder().id(ruleInformationId).name(ruleInformationName).ruleCode(ruleCode).ruleSet(ruleSet).category(category).severity(severity).
                certainty(certainty).likelihood(likelihood).priority(priority).vulnerable(vulnerable).language(ruleInformationLanguage).remediationCost(RuleInformation.RemediationCost.HIGH).build();
        when(ruleService.getRuleInformation(scanEngineName, scanEngineVerion, null, null)).thenReturn(Collections.singletonList(ruleInformation));
        Map<String, RuleInformation> ruleInformationMap = issueService.retrieveRuleInformationMap(scanEngineName, scanEngineVerion);
        String expectedRuleCode = scanEngineName + "-" + ruleSetName + "-" + ruleCode;
        assertEquals("11111111-1111-1111-1111-111111111110", ruleInformationMap.get(expectedRuleCode).getId().toString());
        assertEquals(ruleInformation.getName(), ruleInformationMap.get(expectedRuleCode).getName());
        assertEquals(ruleInformation.getRuleCode(), ruleInformationMap.get(expectedRuleCode).getRuleCode());
        assertEquals(ruleInformation.getRuleSet(), ruleInformationMap.get(expectedRuleCode).getRuleSet());
        assertEquals(ruleInformation.getCategory(), ruleInformationMap.get(expectedRuleCode).getCategory());
        assertEquals(ruleInformation.getSeverity(), ruleInformationMap.get(expectedRuleCode).getSeverity());
        assertEquals(ruleInformation.getLikelihood(), ruleInformationMap.get(expectedRuleCode).getLikelihood());
        assertEquals(ruleInformation.getPriority(), ruleInformationMap.get(expectedRuleCode).getPriority());
        assertEquals(ruleInformation.getVulnerable(), ruleInformationMap.get(expectedRuleCode).getVulnerable());
        assertEquals(ruleInformation.getLanguage(), ruleInformationMap.get(expectedRuleCode).getLanguage());
        assertEquals(ruleInformation.getRemediationCost(), ruleInformationMap.get(expectedRuleCode).getRemediationCost());
    }

    @Test
    void retrieveTracePathFromImportScanResultRequestTestSuccess() {
        log.info("[retrieveTracePathFromImportScanResultRequestTestSuccess]");
        Map<String, ImportScanResultRequest.FileInfo> fileInfoMap = new HashMap<>();
        fileInfoMap.put("fid1", ImportScanResultRequest.FileInfo.builder().fileId("fid1").path("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c").build());
        fileInfoMap.put("fid2", ImportScanResultRequest.FileInfo.builder().fileId("fid2").path("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/java.c").build());
        Map<String, ScanFile> scanFileMap = new HashMap<>();
        scanFileMap.put("fid1", ScanFile.builder().storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c").build());
        scanFileMap.put("fid2", ScanFile.builder().storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/java.c").build());

        ImportScanResultRequest.Issue.TracePath tracePath = ImportScanResultRequest.Issue.TracePath.builder()
                .fileId("fid1")
                .startLineNo(381)
                .endLineNo(0)
                .startColumnNo(21)
                .endColumnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("Then block is taken")
                .build();
        ImportScanResultRequest.Issue requestIssue = ImportScanResultRequest.Issue.builder().tracePaths(Collections.singletonList(tracePath)).build();
        Issue issue = Issue.builder().issueKey("/share/scan/22d11179-dda6-45fd-848d-556ec98c25d1/src/core/ngx_string.c@ngx_vslprintf@last@AOB@@@f6").build();
        List<IssueTrace> issueTraceList = issueService.retrieveTracePathFromImportScanResultRequest(fileInfoMap, scanFileMap, requestIssue, issue, currentUser.getUsername());
        assertEquals(issue.getIssueKey(), issueTraceList.get(0).getIssue().getIssueKey());
        assertEquals(1, issueTraceList.get(0).getSeq());
        assertEquals("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c", issueTraceList.get(0).getScanFile().getStorePath());
        assertEquals(tracePath.getStartLineNo(), issueTraceList.get(0).getLineNo());
        assertEquals(tracePath.getStartColumnNo(), issueTraceList.get(0).getColumnNo());
        assertEquals(tracePath.getFunctionName(), issueTraceList.get(0).getFunctionName());
        assertEquals(tracePath.getVariableName(), issueTraceList.get(0).getVariableName());
        assertEquals(tracePath.getMessage(), issueTraceList.get(0).getMessage());
    }

    @Test
    void retrieveScanFileMapFromResultTestSuccess() {
        log.info("[retrieveScanFileMapFromResultTestSuccess]");
        List<ImportScanResultRequest.FileInfo> requestFileInfos = Arrays.asList(
                ImportScanResultRequest.FileInfo.builder().fileId("fid1").path("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c").build(),
                ImportScanResultRequest.FileInfo.builder().fileId("fid2").path("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/java.c").build()
        );
        List<ScanFile> scanFileList = Arrays.asList(
                ScanFile.builder().storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c").fileInfo(FileInfo.builder().build()).build(),
                ScanFile.builder().storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/java.c").fileInfo(FileInfo.builder().build()).build()
        );
        when(scanTaskService.findScanFileByScanTask(scanTask)).thenReturn(scanFileList);
        Map<String, ScanFile> resultScanFileMap = issueService.retrieveScanFileMapFromResult(scanTask, requestFileInfos);
        assertEquals("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/go.c", resultScanFileMap.get("fid1").getStorePath());
        assertEquals("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/java.c", resultScanFileMap.get("fid2").getStorePath());
    }

    @Test
    void retrieveFileInfoMapFromResultTestSuccess() {
        log.info("[retrieveFileInfoMapFromResultTestSuccess]");
        List<ImportScanResultRequest.FileInfo> requestFileInfos = Arrays.asList(
                ImportScanResultRequest.FileInfo.builder().fileId("fid1").path("go.c").build(), ImportScanResultRequest.FileInfo.builder().fileId("fid2").path("java.c").build()
        );
        Map<String, ImportScanResultRequest.FileInfo> fileInfoMap = issueService.retrieveFileInfoMapFromResult(requestFileInfos);
        assertEquals("fid1", fileInfoMap.get("fid1").getFileId());
        assertEquals("fid2", fileInfoMap.get("fid2").getFileId());
        assertEquals("go.c", fileInfoMap.get("fid1").getPath());
        assertEquals("java.c", fileInfoMap.get("fid2").getPath());
    }

    @Test
    void listIssueInScanTaskTestSuccess() {
        log.info("[listIssueInScanTaskTestSuccess]");
        List<Issue> issueList = Arrays.asList(issue1, issue2);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> pagedIssues = new PageImpl<>(issueList);
        when(issueRepository.searchIssue(any(ScanTask.class), any(), any(), any(), notNull(), notNull(), notNull(), notNull(), any(Pageable.class))).thenReturn(pagedIssues);
        Page<Issue> pagedResultIssues = issueService.listIssueInScanTask(scanTask, pageable);
        assertEquals(pagedIssues.getTotalPages(), pagedResultIssues.getTotalPages());
        assertEquals(pagedIssues.getTotalElements(), pagedResultIssues.getTotalElements());
    }

    @Test
    void assignIssueTestUserNotFoundFail() {
        log.info("[assignIssueTestUserNotFoundFail]");
        UUID issueUuid = UUID.randomUUID();
        Issue issue = Issue.builder().id(issueUuid).build();
        when(userService.findById(currentUser.getId())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> issueService.assignIssue(issue, currentUser.getId(), currentUser.getUsername()));
    }

    @Test
    void assignIssueTestSuccess() throws AppException {
        log.info("[assignIssueTestSuccess]");
        when(userService.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(issueRepository.saveAndFlush(issue1)).thenReturn(issue1);
        Issue resultIssue = issueService.assignIssue(issue1, currentUser.getId(), currentUser.getUsername());
        assertEquals(issue1.getId(), resultIssue.getId());
        assertEquals(issue1.getAssignTo(), resultIssue.getAssignTo());
        assertEquals(issue1.getModifiedBy(), resultIssue.getModifiedBy());
        assertEquals(issue1.getModifiedOn(), resultIssue.getModifiedOn());
    }


    @Test
    void findByIdTestSuccess() {
        log.info("[findByIdTestSuccess]");
        UUID issueUuid = UUID.randomUUID();
        Issue issue = Issue.builder().id(issueUuid).build();
        when(issueRepository.findById(issueUuid)).thenReturn(Optional.of(issue));
        Optional<Issue> resultOptionalIssue = issueService.findById(issueUuid);
        assertTrue(resultOptionalIssue.isPresent());
        assertEquals(issue.getId(), resultOptionalIssue.get().getId());
    }


    @Test
    void findByScanTaskTestSuccess() {
        log.info("[findByScanTaskTestSuccess]");
        when(issueRepository.findByScanTask(scanTask)).thenReturn(issueList);
        List<Issue> resultIssueList = issueService.findByScanTask(scanTask);
        assertEquals(2, resultIssueList.size());
        assertEquals(issue1.getId(), resultIssueList.get(0).getId());
        assertEquals(issue2.getId(), resultIssueList.get(1).getId());
    }

    @Test
    void findByProjectTestSuccess() {
        log.info("[findByProjectTestSuccess]");
        Project project = Project.builder().id(UUID.randomUUID()).build();
        issueService.findByProject(project);
        Issue issue1 = Issue.builder().id(UUID.randomUUID()).build();
        Issue issue2 = Issue.builder().id(UUID.randomUUID()).build();
        List<Issue> issueList = Arrays.asList(issue1, issue2);
        when(issueRepository.findByScanTaskProject(project)).thenReturn(issueList);
        List<Issue> resultIssueList = issueService.findByProject(project);
        assertEquals(2, resultIssueList.size());
        assertEquals(issue1.getId(), resultIssueList.get(0).getId());
        assertEquals(issue2.getId(), resultIssueList.get(1).getId());
    }

    @Test
    void updateIssueStatusTestStatusIsNullFail() {
        log.info("[updateIssueStatusTestStatusIsNullFail]");
        Issue issue = Issue.builder().id(UUID.randomUUID()).build();
        assertThrows(AppException.class, () -> issueService.updateIssueStatus(issue, null, currentUser.getUsername()));
    }

    @Test
    void updateIssueStatusTestSuccess() throws AppException {
        log.info("[updateIssueStatusTestSuccess]");
        Date modifyDate = new Date();
        Issue issue = Issue.builder().id(UUID.randomUUID()).modifiedBy(currentUser.getUsername()).status(Issue.Status.PENDING).modifiedOn(modifyDate).build();
        when(issueRepository.saveAndFlush(issue)).thenReturn(issue);
        Issue resultIssue = issueService.updateIssueStatus(issue, Issue.Status.PENDING.name(), currentUser.getUsername());
        assertEquals(issue.getId(), resultIssue.getId());
        assertEquals(issue.getStatus(), resultIssue.getStatus());
        assertEquals(issue.getModifiedBy(), resultIssue.getModifiedBy());
        assertEquals(issue.getModifiedOn(), resultIssue.getModifiedOn());
    }

    @Test
    void updateIssueSeverityTestSeverityIsNullFail() {
        log.info("[updateIssueSeverityTestSeverityIsNullFail]");
        Issue issue = Issue.builder().id(UUID.randomUUID()).build();
        assertThrows(AppException.class, () -> issueService.updateIssueSeverity(issue, null, currentUser.getUsername()));
    }


    @Test
    void updateIssueSeverityTestSuccess() throws AppException {
        log.info("[updateIssueSeverityTestSuccess]");
        Date modifyDate = new Date();
        Issue issue = Issue.builder().id(UUID.randomUUID()).modifiedBy(currentUser.getUsername()).severity(Issue.Severity.HIGH).modifiedOn(modifyDate).build();
        when(issueRepository.saveAndFlush(issue)).thenReturn(issue);
        Issue resultIssue = issueService.updateIssueSeverity(issue, Issue.Severity.HIGH.name(), currentUser.getUsername());
        assertEquals(issue.getId(), resultIssue.getId());
        assertEquals(issue.getSeverity(), resultIssue.getSeverity());
        assertEquals(issue.getModifiedBy(), resultIssue.getModifiedBy());
        assertEquals(issue.getModifiedOn(), resultIssue.getModifiedOn());
    }

    @Test
    void updateIssueActionTestActionIsNullFail() {
        log.info("[updateIssueActionTestActionIsNullFail]");
        Issue issue = Issue.builder().id(UUID.randomUUID()).build();
        assertThrows(AppException.class, () -> issueService.updateIssueAction(issue, null, currentUser.getUsername()));
    }


    @Test
    void updateIssueActionTestSuccess() throws AppException {
        log.info("[updateIssueActionTestSuccess]");
        Date modifyDate = new Date();
        Issue issue = Issue.builder().id(UUID.randomUUID()).modifiedBy(currentUser.getUsername()).action(Issue.Action.CONFIRMED).modifiedOn(modifyDate).build();
        when(issueRepository.saveAndFlush(issue)).thenReturn(issue);
        Issue resultIssue = issueService.updateIssueAction(issue, Issue.Action.CONFIRMED.name(), currentUser.getUsername());
        assertEquals(issue.getId(), resultIssue.getId());
        assertEquals(issue.getSeverity(), resultIssue.getSeverity());
        assertEquals(issue.getModifiedBy(), resultIssue.getModifiedBy());
        assertEquals(issue.getModifiedOn(), resultIssue.getModifiedOn());
    }


    @Test
    void updateIssueTestSuccess() {
        log.info("[updateIssueTestSuccess]");
        Date modifyDate = new Date();
        Issue issue = Issue.builder().id(UUID.randomUUID()).modifiedBy(currentUser.getUsername()).action(Issue.Action.CONFIRMED).modifiedOn(modifyDate).build();
        when(issueRepository.saveAndFlush(issue)).thenReturn(issue);
        Issue resultIssue = issueService.updateIssue(issue, currentUser.getUsername());
        assertEquals(issue.getId(), resultIssue.getId());
        assertEquals(issue.getSeverity(), resultIssue.getSeverity());
        assertEquals(issue.getModifiedBy(), resultIssue.getModifiedBy());
        assertEquals(issue.getModifiedOn(), resultIssue.getModifiedOn());
    }

    @Test
    void sendIssuesToUsersTestAssignedToIsNullFail() {
        log.info("[sendIssuesToUsersTestAssignedToIsNullFail]");
        AssignIssuesRequest.AssignIssue assignIssue1 = AssignIssuesRequest.AssignIssue.builder().issueId(issue1.getId()).userId(currentUser.getId()).build();
        AssignIssuesRequest.AssignIssue assignIssue2 = AssignIssuesRequest.AssignIssue.builder().issueId(issue2.getId()).userId(currentUser.getId()).build();
        List<AssignIssuesRequest.AssignIssue> assignIssues = Arrays.asList(assignIssue1, assignIssue2);
        AssignIssuesRequest assignIssuesRequest = AssignIssuesRequest.builder().assignIssues(assignIssues).build();
        when(issueRepository.findByIdWithWholeObject(eq(issue1.getId()))).thenReturn(Optional.of(issue1));
        when(issueRepository.findByIdWithWholeObject(eq(issue2.getId()))).thenReturn(Optional.of(issue2));
        assertThrows(AppException.class, () -> issueService.sendIssuesToUsers(assignIssuesRequest, currentUser, Locale.ENGLISH, currentUser.getUsername()));
    }

    @Test
    void sendIssuesToUsersTestSuccess() throws AppException {
        log.info("[sendIssuesToUsersTestSuccess]");
        AssignIssuesRequest.AssignIssue assignIssue1 = AssignIssuesRequest.AssignIssue.builder().issueId(issue1.getId()).userId(currentUser.getId()).build();
        AssignIssuesRequest.AssignIssue assignIssue2 = AssignIssuesRequest.AssignIssue.builder().issueId(issue2.getId()).userId(currentUser.getId()).build();
        List<AssignIssuesRequest.AssignIssue> assignIssues = Arrays.asList(assignIssue1, assignIssue2);
        AssignIssuesRequest assignIssuesRequest = AssignIssuesRequest.builder().assignIssues(assignIssues).build();
        when(issueRepository.findByIdWithWholeObject(eq(issue1.getId()))).thenReturn(Optional.of(issue1));
        when(issueRepository.findByIdWithWholeObject(eq(issue2.getId()))).thenReturn(Optional.of(issue2));
        when(userService.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(settingService.getEmailServerConfiguration()).thenReturn(AppProperties.Mail.builder()
                .host("smtpdm.aliyun.com")
                .port(465)
                .username("no-reply@xcalibyte.io")
                .password("xxxxxx")
                .protocol("smtp")
                .prefix("[XCALBYTE]")
                .starttls("false")
                .build());
        doNothing().when(emailService).sendTemplateMail(any(), any());
        issueService.sendIssuesToUsers(assignIssuesRequest, currentUser, Locale.ENGLISH, currentUser.getUsername());
        assertTrue(true);
    }

    @Test
    void assignIssuesToUsersTestIssueNotFoundFail() {
        log.info("[assignIssuesToUsersTestIssueNotFoundFail]");
        UUID issueId1 = UUID.fromString("11111111-1111-1111-1110-111111111111");
        UUID issueId2 = UUID.fromString("11111111-1111-1111-1110-111111111112");
        UUID userId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID userId2 = UUID.fromString("11111111-1111-1111-1111-111111111112");
        AssignIssuesRequest.AssignIssue assignIssue1 = AssignIssuesRequest.AssignIssue.builder().issueId(issueId1).userId(userId1).build();
        AssignIssuesRequest.AssignIssue assignIssue2 = AssignIssuesRequest.AssignIssue.builder().issueId(issueId2).userId(userId2).build();
        List<AssignIssuesRequest.AssignIssue> assignIssues = Arrays.asList(assignIssue1, assignIssue2);
        AssignIssuesRequest assignIssuesRequest = AssignIssuesRequest.builder().assignIssues(assignIssues).build();
        when(issueRepository.findById(issueId1)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> issueService.assignIssuesToUsers(assignIssuesRequest, currentUser.getUsername()));
    }

    @Test
    void assignIssuesToUsersTestUserNotFoundFail() {
        log.info("[assignIssuesToUsersTestUserNotFoundFail]");
        UUID issueId1 = UUID.fromString("11111111-1111-1111-1110-111111111111");
        UUID issueId2 = UUID.fromString("11111111-1111-1111-1110-111111111112");
        UUID userId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID userId2 = UUID.fromString("11111111-1111-1111-1111-111111111112");
        AssignIssuesRequest.AssignIssue assignIssue1 = AssignIssuesRequest.AssignIssue.builder().issueId(issueId1).userId(userId1).build();
        AssignIssuesRequest.AssignIssue assignIssue2 = AssignIssuesRequest.AssignIssue.builder().issueId(issueId2).userId(userId2).build();
        List<AssignIssuesRequest.AssignIssue> assignIssues = Arrays.asList(assignIssue1, assignIssue2);
        AssignIssuesRequest assignIssuesRequest = AssignIssuesRequest.builder().assignIssues(assignIssues).build();
        when(userService.findById(userId1)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> issueService.assignIssuesToUsers(assignIssuesRequest, currentUser.getUsername()));
    }

    @Test
    void assignIssuesToUsersTestSuccess() throws AppException {
        log.info("[assignIssuesToUsersTestSuccess]");
        UUID issueId1 = UUID.fromString("11111111-1111-1111-1110-111111111111");
        UUID issueId2 = UUID.fromString("11111111-1111-1111-1110-111111111112");
        UUID userId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID userId2 = UUID.fromString("11111111-1111-1111-1111-111111111112");
        User user1 = User.builder().id(userId1).username(currentUser.getUsername()).displayName("testDispalyName1").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();
        User user2 = User.builder().id(userId2).username(currentUser.getUsername()).displayName("testDispalyName2").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();
        AssignIssuesRequest.AssignIssue assignIssue1 = AssignIssuesRequest.AssignIssue.builder().issueId(issueId1).userId(userId1).build();
        AssignIssuesRequest.AssignIssue assignIssue2 = AssignIssuesRequest.AssignIssue.builder().issueId(issueId2).userId(userId2).build();
        List<AssignIssuesRequest.AssignIssue> assignIssues = Arrays.asList(assignIssue1, assignIssue2);
        AssignIssuesRequest assignIssuesRequest = AssignIssuesRequest.builder().assignIssues(assignIssues).build();
        Issue issue1 = Issue.builder().id(issueId1).build();
        Issue issue2 = Issue.builder().id(issueId2).build();
        List<Issue> issueList = Arrays.asList(issue1, issue2);
        when(issueRepository.findById(issueId1)).thenReturn(Optional.of(issue1));
        when(issueRepository.findById(issueId2)).thenReturn(Optional.of(issue2));
        when(userService.findById(userId1)).thenReturn(Optional.of(user1));
        when(userService.findById(userId2)).thenReturn(Optional.of(user2));
        when(issueRepository.saveAll(any())).thenReturn(issueList);
        doNothing().when(issueRepository).flush();
        List<Issue> resultIssueList = issueService.assignIssuesToUsers(assignIssuesRequest, currentUser.getUsername());
        assertEquals(2, resultIssueList.size());
        assertEquals(issue1.getId(), resultIssueList.get(0).getId());
        assertEquals(issue2.getId(), resultIssueList.get(1).getId());
    }

    @Test
    void sendAssignIssueEmailTestSuccess() throws AppException {
        log.info("[sendAssignIssueEmailTestSuccess]");
        UUID issueId1 = UUID.fromString("11111111-1111-1111-1110-111111111111");
        UUID issueId2 = UUID.fromString("11111111-1111-1111-1110-111111111112");
        UUID userId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID userId2 = UUID.fromString("11111111-1111-1111-1111-111111111112");
        User user1 = User.builder().id(userId1).username(currentUser.getUsername()).displayName("testDispalyName1").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();
        User user2 = User.builder().id(userId2).username(currentUser.getUsername()).displayName("testDispalyName2").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();
        List<Issue> issueList = Arrays.asList(issue1, issue2);
        when(issueRepository.findById(issueId1)).thenReturn(Optional.of(issue1));
        when(issueRepository.findById(issueId2)).thenReturn(Optional.of(issue2));
        when(userService.findById(userId1)).thenReturn(Optional.of(user1));
        when(userService.findById(userId2)).thenReturn(Optional.of(user2));
        when(issueRepository.saveAll(any())).thenReturn(issueList);
        doNothing().when(issueRepository).flush();
        when(userService.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        doNothing().when(emailService).sendTemplateMail(any(), any());
        issueService.sendAssignIssueEmail(issue1, userId1, currentUser, Locale.ENGLISH, currentUser.getUsername());
        assertTrue(true);
    }

    @Test
    void sendAssignIssueEmail_UserNotFound_ThrowAppException() {
        log.info("[sendAssignIssueEmail_UserNotFound_ThrowAppException]");
        when(userService.findById(currentUser.getId())).thenReturn(Optional.empty());
        AppException appException = assertThrows(AppException.class, () -> issueService.sendAssignIssueEmail(issue1, currentUser.getId(), currentUser, Locale.ENGLISH, currentUser.getUsername()));
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, appException.getResponseCode());
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_DATA_NOT_FOUND, appException.getErrorCode());
    }

    @Test
    void sendAssignIssueEmailTestSuccess1() throws AppException {
        log.info("[sendAssignIssueEmailTestSuccess1]");
        List<Issue> issueList = Arrays.asList(issue1, issue2);
        when(issueRepository.findById(issue1.getId())).thenReturn(Optional.of(issue1));
        when(issueRepository.findById(issue2.getId())).thenReturn(Optional.of(issue2));
        when(userService.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(issueRepository.saveAll(any())).thenReturn(issueList);
        doNothing().when(issueRepository).flush();
        when(userService.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(settingService.getEmailServerConfiguration()).thenReturn(AppProperties.Mail.builder()
                .host("smtpdm.aliyun.com")
                .port(465)
                .username("no-reply@xcalibyte.io")
                .password("xxxxxx")
                .protocol("smtp")
                .prefix("[XCALBYTE]")
                .starttls("false")
                .build());
        doNothing().when(emailService).sendTemplateMail(any(), any());
        issueService.sendAssignIssueEmail(scanTask, issueList, currentUser.getId(), currentUser, Locale.ENGLISH, currentUser.getUsername());
        assertTrue(true);
    }

    @Test
    void convertIssueToDtoTestIgnoreTraceSuccess() {
        log.info("[convertIssueToDtoTestIgnoreTraceSuccess]");
        Map<String, I18nMessage> i18nMessageMap = new HashMap<>();
        IssueDto resultIssueDto = IssueService.convertIssueToDto(issue1, i18nMessageMap);
        assertEquals(issue1.getId(), resultIssueDto.getId());
        assertEquals(issue1.getIssueKey(), resultIssueDto.getIssueKey());
        assertEquals(issue1.getSeq(), resultIssueDto.getSeq());
        assertEquals(issue1.getIssueCode(), resultIssueDto.getIssueCode());
        assertEquals(issue1.getSeverity().name(), resultIssueDto.getSeverity());
        assertEquals(issue1.getLineNo(), resultIssueDto.getLineNo());
        assertEquals(issue1.getColumnNo(), resultIssueDto.getColumnNo());
        assertEquals(issue1.getFunctionName(), resultIssueDto.getFunctionName());
        assertEquals(issue1.getVariableName(), resultIssueDto.getVariableName());
        assertEquals(issue1.getChecksum(), resultIssueDto.getChecksum());
        assertEquals(issue1.getMessage(), resultIssueDto.getMessage());
        assertEquals(issue1.getIgnored(), resultIssueDto.getIgnored());
        assertEquals(issue1.getStatus().name(), resultIssueDto.getStatus());
        assertEquals(issue1.getAction().name(), resultIssueDto.getAction());
        assertEquals(issue1.getCreatedBy(), resultIssueDto.getCreatedBy());
        assertEquals(issue1.getCreatedOn(), resultIssueDto.getCreatedOn());
        assertEquals(issue1.getModifiedBy(), resultIssueDto.getModifiedBy());
        assertEquals(issue1.getModifiedOn(), resultIssueDto.getModifiedOn());
        assertEquals(issue1.getAssignTo().getId(), resultIssueDto.getAssignTo().getId());
        assertEquals(issue1.getAssignTo().getDisplayName(), resultIssueDto.getAssignTo().getDisplayName());
        assertEquals(issue1.getAssignTo().getEmail(), resultIssueDto.getAssignTo().getEmail());
        assertEquals(ruleInformation1.getId(), resultIssueDto.getRuleInformation().getId());
        assertEquals(ruleInformation1.getRuleSet().getName(), resultIssueDto.getRuleSet());
        assertEquals(ruleInformation1.getRuleCode(), resultIssueDto.getRuleInformation().getRuleCode());
        assertEquals(ruleInformation1.getCategory(), resultIssueDto.getRuleInformation().getCategory());
        assertEquals(ruleInformation1.getVulnerable(), resultIssueDto.getRuleInformation().getVulnerable());
        assertEquals(ruleInformation1.getName(), resultIssueDto.getRuleInformation().getName());
        assertEquals(ruleInformation1.getSeverity().name(), resultIssueDto.getRuleInformation().getSeverity());
        assertEquals(ruleInformation1.getPriority().name(), resultIssueDto.getRuleInformation().getPriority());
        assertEquals(ruleInformation1.getLikelihood().name(), resultIssueDto.getRuleInformation().getLikelihood());
        assertEquals(ruleInformation1.getLanguage(), resultIssueDto.getRuleInformation().getLanguage());
        assertEquals(ruleInformation1.getUrl(), resultIssueDto.getRuleInformation().getUrl());
        assertEquals(ruleInformation1.getDetail(), resultIssueDto.getRuleInformation().getDetail());
        assertEquals(ruleInformation1.getDescription(), resultIssueDto.getRuleInformation().getDescription());
    }

    @Test
    void convertIssueToDtoTestNotIgnoreTraceSuccess() {
        log.info("[convertIssueToDtoTestNotIgnoreTraceSuccess]");
        Map<String, I18nMessage> i18nMessageMap = new HashMap<>();
        IssueDto resultIssueDto = IssueService.convertIssueToDto(issue1, issue1.getIssueTraces(), i18nMessageMap);
        assertEquals(issue1.getId(), resultIssueDto.getId());
        assertEquals(issue1.getIssueKey(), resultIssueDto.getIssueKey());
        assertEquals(issue1.getSeq(), resultIssueDto.getSeq());
        assertEquals(issue1.getIssueCode(), resultIssueDto.getIssueCode());
        assertEquals(issue1.getSeverity().name(), resultIssueDto.getSeverity());
        assertEquals(issue1.getLineNo(), resultIssueDto.getLineNo());
        assertEquals(issue1.getColumnNo(), resultIssueDto.getColumnNo());
        assertEquals(issue1.getFunctionName(), resultIssueDto.getFunctionName());
        assertEquals(issue1.getVariableName(), resultIssueDto.getVariableName());
        assertEquals(issue1.getChecksum(), resultIssueDto.getChecksum());
        assertEquals(issue1.getMessage(), resultIssueDto.getMessage());
        assertEquals(issue1.getIgnored(), resultIssueDto.getIgnored());
        assertEquals(issue1.getStatus().name(), resultIssueDto.getStatus());
        assertEquals(issue1.getAction().name(), resultIssueDto.getAction());
        assertEquals(issue1.getCreatedBy(), resultIssueDto.getCreatedBy());
        assertEquals(issue1.getCreatedOn(), resultIssueDto.getCreatedOn());
        assertEquals(issue1.getModifiedBy(), resultIssueDto.getModifiedBy());
        assertEquals(issue1.getModifiedOn(), resultIssueDto.getModifiedOn());
        assertEquals(issue1.getAssignTo().getId(), resultIssueDto.getAssignTo().getId());
        assertEquals(issue1.getAssignTo().getDisplayName(), resultIssueDto.getAssignTo().getDisplayName());
        assertEquals(issue1.getAssignTo().getEmail(), resultIssueDto.getAssignTo().getEmail());
        assertEquals(ruleInformation1.getId(), resultIssueDto.getRuleInformation().getId());
        assertEquals(ruleInformation1.getRuleSet().getName(), resultIssueDto.getRuleSet());
        assertEquals(ruleInformation1.getRuleCode(), resultIssueDto.getRuleInformation().getRuleCode());
        assertEquals(ruleInformation1.getCategory(), resultIssueDto.getRuleInformation().getCategory());
        assertEquals(ruleInformation1.getVulnerable(), resultIssueDto.getRuleInformation().getVulnerable());
        assertEquals(ruleInformation1.getName(), resultIssueDto.getRuleInformation().getName());
        assertEquals(ruleInformation1.getSeverity().name(), resultIssueDto.getRuleInformation().getSeverity());
        assertEquals(ruleInformation1.getPriority().name(), resultIssueDto.getRuleInformation().getPriority());
        assertEquals(ruleInformation1.getLikelihood().name(), resultIssueDto.getRuleInformation().getLikelihood());
        assertEquals(ruleInformation1.getLanguage(), resultIssueDto.getRuleInformation().getLanguage());
        assertEquals(ruleInformation1.getUrl(), resultIssueDto.getRuleInformation().getUrl());
        assertEquals(ruleInformation1.getDetail(), resultIssueDto.getRuleInformation().getDetail());
        assertEquals(ruleInformation1.getDescription(), resultIssueDto.getRuleInformation().getDescription());
        assertEquals(issueTrace11.getId(), resultIssueDto.getIssueTraces().get(0).getId());
        assertEquals(issueTrace11.getScanFile().getProjectRelativePath(), resultIssueDto.getIssueTraces().get(0).getRelativePath());
        assertEquals(issueTrace11.getScanFile().getId(), resultIssueDto.getIssueTraces().get(0).getScanFileId());
        assertEquals(issueTrace11.getScanFile().getStorePath(), resultIssueDto.getIssueTraces().get(0).getScanFilePath());
        assertEquals(issueTrace11.getSeq(), resultIssueDto.getIssueTraces().get(0).getSeq());
        assertEquals(issueTrace11.getLineNo(), resultIssueDto.getIssueTraces().get(0).getLineNo());
        assertEquals(issueTrace11.getColumnNo(), resultIssueDto.getIssueTraces().get(0).getColumnNo());
        assertEquals(issueTrace11.getFunctionName(), resultIssueDto.getIssueTraces().get(0).getFunctionName());
        assertEquals(issueTrace11.getVariableName(), resultIssueDto.getIssueTraces().get(0).getVariableName());
        assertEquals(issueTrace11.getChecksum(), resultIssueDto.getIssueTraces().get(0).getChecksum());
        assertEquals(issueTrace11.getMessage(), resultIssueDto.getIssueTraces().get(0).getMessage());
        assertEquals(issueTrace11.getScanFile().getFileInfo().getFileSize(), resultIssueDto.getIssueTraces().get(0).getScanFileSize());
        assertEquals(issueTrace11.getScanFile().getFileInfo().getNoOfLines(), resultIssueDto.getIssueTraces().get(0).getScanFileNoOfLines());
    }

    @Test
    void convertIssueToDto_TestNotIgnoreTrace_Success2() {
        log.info("[convertIssueToDto_TestNotIgnoreTrace_Success2]");

        Map<String, I18nMessage> i18nMessageMap = new HashMap<>();
        IssueDto resultIssueDto = IssueService.convertIssueToDto(issue1, issue1.getIssueTraces(), i18nMessageMap);
        assertEquals(issue1.getId(), resultIssueDto.getId());
        assertEquals(issue1.getIssueKey(), resultIssueDto.getIssueKey());
        assertEquals(issue1.getSeq(), resultIssueDto.getSeq());
        assertEquals(issue1.getIssueCode(), resultIssueDto.getIssueCode());
        assertEquals(issue1.getSeverity().name(), resultIssueDto.getSeverity());
        assertEquals(issue1.getLineNo(), resultIssueDto.getLineNo());
        assertEquals(issue1.getColumnNo(), resultIssueDto.getColumnNo());
        assertEquals(issue1.getFunctionName(), resultIssueDto.getFunctionName());
        assertEquals(issue1.getVariableName(), resultIssueDto.getVariableName());
        assertEquals(issue1.getChecksum(), resultIssueDto.getChecksum());
        assertEquals(issue1.getMessage(), resultIssueDto.getMessage());
        assertEquals(issue1.getIgnored(), resultIssueDto.getIgnored());
        assertEquals(issue1.getStatus().name(), resultIssueDto.getStatus());
        assertEquals(issue1.getAction().name(), resultIssueDto.getAction());
        assertEquals(issue1.getCreatedBy(), resultIssueDto.getCreatedBy());
        assertEquals(issue1.getCreatedOn(), resultIssueDto.getCreatedOn());
        assertEquals(issue1.getModifiedBy(), resultIssueDto.getModifiedBy());
        assertEquals(issue1.getModifiedOn(), resultIssueDto.getModifiedOn());
        assertEquals(issue1.getAssignTo().getId(), resultIssueDto.getAssignTo().getId());
        assertEquals(issue1.getAssignTo().getDisplayName(), resultIssueDto.getAssignTo().getDisplayName());
        assertEquals(issue1.getAssignTo().getEmail(), resultIssueDto.getAssignTo().getEmail());
        assertEquals(ruleInformation1.getId(), resultIssueDto.getRuleInformation().getId());
        assertEquals(ruleInformation1.getRuleSet().getName(), resultIssueDto.getRuleSet());
        assertEquals(ruleInformation1.getRuleCode(), resultIssueDto.getRuleInformation().getRuleCode());
        assertEquals(ruleInformation1.getCategory(), resultIssueDto.getRuleInformation().getCategory());
        assertEquals(ruleInformation1.getVulnerable(), resultIssueDto.getRuleInformation().getVulnerable());
        assertEquals(ruleInformation1.getName(), resultIssueDto.getRuleInformation().getName());
        assertEquals(ruleInformation1.getSeverity().name(), resultIssueDto.getRuleInformation().getSeverity());
        assertEquals(ruleInformation1.getPriority().name(), resultIssueDto.getRuleInformation().getPriority());
        assertEquals(ruleInformation1.getLikelihood().name(), resultIssueDto.getRuleInformation().getLikelihood());
        assertEquals(ruleInformation1.getLanguage(), resultIssueDto.getRuleInformation().getLanguage());
        assertEquals(ruleInformation1.getUrl(), resultIssueDto.getRuleInformation().getUrl());
        assertEquals(ruleInformation1.getDetail(), resultIssueDto.getRuleInformation().getDetail());
        assertEquals(ruleInformation1.getDescription(), resultIssueDto.getRuleInformation().getDescription());
        assertEquals(issueTrace11.getId(), resultIssueDto.getIssueTraces().get(0).getId());
        assertEquals(issueTrace11.getScanFile().getProjectRelativePath(), resultIssueDto.getIssueTraces().get(0).getRelativePath());
        assertEquals(issueTrace11.getScanFile().getId(), resultIssueDto.getIssueTraces().get(0).getScanFileId());
        assertEquals(issueTrace11.getScanFile().getStorePath(), resultIssueDto.getIssueTraces().get(0).getScanFilePath());
        assertEquals(issueTrace11.getSeq(), resultIssueDto.getIssueTraces().get(0).getSeq());
        assertEquals(issueTrace11.getLineNo(), resultIssueDto.getIssueTraces().get(0).getLineNo());
        assertEquals(issueTrace11.getColumnNo(), resultIssueDto.getIssueTraces().get(0).getColumnNo());
        assertEquals(issueTrace11.getFunctionName(), resultIssueDto.getIssueTraces().get(0).getFunctionName());
        assertEquals(issueTrace11.getVariableName(), resultIssueDto.getIssueTraces().get(0).getVariableName());
        assertEquals(issueTrace11.getChecksum(), resultIssueDto.getIssueTraces().get(0).getChecksum());
        assertEquals(issueTrace11.getMessage(), resultIssueDto.getIssueTraces().get(0).getMessage());
        assertEquals(issueTrace11.getScanFile().getFileInfo().getFileSize(), resultIssueDto.getIssueTraces().get(0).getScanFileSize());
        assertEquals(issueTrace11.getScanFile().getFileInfo().getNoOfLines(), resultIssueDto.getIssueTraces().get(0).getScanFileNoOfLines());
    }


    @Test
    void convertIssuesToDtoTestSuccess() {
        log.info("[convertIssuesToDtoTestSuccess]");
        List<IssueDto> resultIssueDtoList = this.issueService.convertIssuesToDto(Arrays.asList(issue1, issue2, issue3), Locale.ENGLISH);
        assertEquals(issue1.getId(), resultIssueDtoList.get(0).getId());
        assertEquals(issue1.getIssueKey(), resultIssueDtoList.get(0).getIssueKey());
        assertEquals(issue1.getSeq(), resultIssueDtoList.get(0).getSeq());
        assertEquals(issue1.getIssueCode(), resultIssueDtoList.get(0).getIssueCode());
        assertEquals(issue1.getSeverity().name(), resultIssueDtoList.get(0).getSeverity());
        assertEquals(issue1.getLineNo(), resultIssueDtoList.get(0).getLineNo());
        assertEquals(issue1.getColumnNo(), resultIssueDtoList.get(0).getColumnNo());
        assertEquals(issue1.getFunctionName(), resultIssueDtoList.get(0).getFunctionName());
        assertEquals(issue1.getVariableName(), resultIssueDtoList.get(0).getVariableName());
        assertEquals(issue1.getChecksum(), resultIssueDtoList.get(0).getChecksum());
        assertEquals(issue1.getMessage(), resultIssueDtoList.get(0).getMessage());
        assertEquals(issue1.getIgnored(), resultIssueDtoList.get(0).getIgnored());
        assertEquals(issue1.getStatus().name(), resultIssueDtoList.get(0).getStatus());
        assertEquals(issue1.getAction().name(), resultIssueDtoList.get(0).getAction());
        assertEquals(issue1.getCreatedBy(), resultIssueDtoList.get(0).getCreatedBy());
        assertEquals(issue1.getCreatedOn(), resultIssueDtoList.get(0).getCreatedOn());
        assertEquals(issue1.getModifiedBy(), resultIssueDtoList.get(0).getModifiedBy());
        assertEquals(issue1.getModifiedOn(), resultIssueDtoList.get(0).getModifiedOn());
        assertEquals(issue1.getAssignTo().getId(), resultIssueDtoList.get(0).getAssignTo().getId());
        assertEquals(issue1.getAssignTo().getDisplayName(), resultIssueDtoList.get(0).getAssignTo().getDisplayName());
        assertEquals(issue1.getAssignTo().getEmail(), resultIssueDtoList.get(0).getAssignTo().getEmail());
        assertEquals(ruleInformation1.getId(), resultIssueDtoList.get(0).getRuleInformation().getId());
        assertEquals(ruleInformation1.getRuleSet().getName(), resultIssueDtoList.get(0).getRuleSet());
        assertEquals(ruleInformation1.getRuleCode(), resultIssueDtoList.get(0).getRuleInformation().getRuleCode());
        assertEquals(ruleInformation1.getCategory(), resultIssueDtoList.get(0).getRuleInformation().getCategory());
        assertEquals(ruleInformation1.getVulnerable(), resultIssueDtoList.get(0).getRuleInformation().getVulnerable());
        assertEquals(ruleInformation1.getName(), resultIssueDtoList.get(0).getRuleInformation().getName());
        assertEquals(ruleInformation1.getSeverity().name(), resultIssueDtoList.get(0).getRuleInformation().getSeverity());
        assertEquals(ruleInformation1.getPriority().name(), resultIssueDtoList.get(0).getRuleInformation().getPriority());
        assertEquals(ruleInformation1.getLikelihood().name(), resultIssueDtoList.get(0).getRuleInformation().getLikelihood());
        assertEquals(ruleInformation1.getLanguage(), resultIssueDtoList.get(0).getRuleInformation().getLanguage());
        assertEquals(ruleInformation1.getUrl(), resultIssueDtoList.get(0).getRuleInformation().getUrl());
        assertEquals(ruleInformation1.getDetail(), resultIssueDtoList.get(0).getRuleInformation().getDetail());
        assertEquals(ruleInformation1.getDescription(), resultIssueDtoList.get(0).getRuleInformation().getDescription());
        // issue 3, rule information 3
        assertEquals(issue3.getId(), resultIssueDtoList.get(2).getId());
        assertEquals(issue3.getIssueKey(), resultIssueDtoList.get(2).getIssueKey());
        assertEquals(issue3.getSeq(), resultIssueDtoList.get(2).getSeq());
        assertEquals(issue3.getIssueCode(), resultIssueDtoList.get(2).getIssueCode());
        assertEquals(issue3.getSeverity().name(), resultIssueDtoList.get(2).getSeverity());
        assertEquals(issue3.getRuleInformation().getPriority().name(), resultIssueDtoList.get(2).getRuleInformation().getPriority());
        assertEquals(issue3.getLineNo(), resultIssueDtoList.get(2).getLineNo());
        assertEquals(issue3.getColumnNo(), resultIssueDtoList.get(2).getColumnNo());
        assertEquals(issue3.getFunctionName(), resultIssueDtoList.get(2).getFunctionName());
        assertEquals(issue3.getVariableName(), resultIssueDtoList.get(2).getVariableName());
        assertEquals(issue3.getChecksum(), resultIssueDtoList.get(2).getChecksum());
        assertEquals(issue3.getMessage(), resultIssueDtoList.get(2).getMessage());
        assertEquals(issue3.getIgnored(), resultIssueDtoList.get(2).getIgnored());
        assertEquals(issue3.getStatus().name(), resultIssueDtoList.get(2).getStatus());
        assertEquals(issue3.getAction().name(), resultIssueDtoList.get(2).getAction());
        assertEquals(issue3.getCreatedBy(), resultIssueDtoList.get(2).getCreatedBy());
        assertEquals(issue3.getCreatedOn(), resultIssueDtoList.get(2).getCreatedOn());
        assertEquals(issue3.getModifiedBy(), resultIssueDtoList.get(2).getModifiedBy());
        assertEquals(issue3.getModifiedOn(), resultIssueDtoList.get(2).getModifiedOn());
        assertEquals(issue3.getAssignTo().getId(), resultIssueDtoList.get(2).getAssignTo().getId());
        assertEquals(issue3.getAssignTo().getDisplayName(), resultIssueDtoList.get(2).getAssignTo().getDisplayName());
        assertEquals(issue3.getAssignTo().getEmail(), resultIssueDtoList.get(2).getAssignTo().getEmail());
        assertEquals(ruleInformation3.getId(), resultIssueDtoList.get(2).getRuleInformation().getId());
        assertEquals(ruleInformation3.getRuleSet().getName(), resultIssueDtoList.get(2).getRuleSet());
        assertEquals(ruleInformation3.getRuleCode(), resultIssueDtoList.get(2).getRuleInformation().getRuleCode());
        assertEquals(ruleInformation3.getCategory(), resultIssueDtoList.get(2).getRuleInformation().getCategory());
        assertEquals(ruleInformation3.getVulnerable(), resultIssueDtoList.get(2).getRuleInformation().getVulnerable());
        assertEquals(ruleInformation3.getName(), resultIssueDtoList.get(2).getRuleInformation().getName());
        assertEquals(issue3.getSeverity().name(), resultIssueDtoList.get(2).getRuleInformation().getSeverity());
        assertNull(resultIssueDtoList.get(2).getRuleInformation().getCertainty());
        assertNull(resultIssueDtoList.get(2).getRuleInformation().getLikelihood());
        assertNull(resultIssueDtoList.get(2).getRuleInformation().getRemediationCost());
    }

    @Test
    void findIssueSummaryCountByUserTestSuccess() {
        log.info("[findIssueSummaryCountByUserTestSuccess]");
        IssueSummaryResponse.AssignSummary assignSummary1 = IssueSummaryResponse.AssignSummary.builder().id(UUID.randomUUID()).displayName("test name1").email("xxx1@xx.com").count(2L).build();
        IssueSummaryResponse.AssignSummary assignSummary2 = IssueSummaryResponse.AssignSummary.builder().id(UUID.randomUUID()).displayName("test name2").email("xxx2@xx.com").count(2L).build();
        when(issueRepository.findIssueSummaryCountByUser(scanTask.getId())).thenReturn(Arrays.asList(assignSummary1, assignSummary2));
        List<IssueSummaryResponse.AssignSummary> assignSummaries = issueService.findIssueSummaryCountByUser(scanTask.getId());
        assertEquals(2, assignSummaries.size());
        assertEquals(assignSummary1.getId(), assignSummaries.get(0).getId());
        assertEquals(assignSummary1.getEmail(), assignSummaries.get(0).getEmail());
        assertEquals(assignSummary1.getDisplayName(), assignSummaries.get(0).getDisplayName());
        assertEquals(assignSummary1.getCount(), assignSummaries.get(0).getCount());
    }

    @Test
    void searchIssueTestSuccess() {
        log.info("[searchIssueTestSuccess]");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> pagedIssues = new PageImpl<>(issueList);
        when(issueRepository.searchIssue(any(ScanTask.class), any(UUID.class), any(), any(), notNull(), notNull(), notNull(), notNull(), any(Pageable.class))).thenReturn(pagedIssues);
        Page<Issue> resultPageIssue = issueService.searchIssue(scanTask, ruleSet.getId(), ruleSet.getName(), "00001", searchAttrs, new ArrayList<>(), ruleInformationIds, scanFileIds, SearchIssueRequest.SearchIssueType.PROJECT_AND_NON_PROJECT, pageable);
        assertEquals(pagedIssues.getTotalPages(), resultPageIssue.getTotalPages());
        assertEquals(pagedIssues.getTotalElements(), resultPageIssue.getTotalElements());
    }

    @Test
    void searchIssue_ScanFileIdsIsNotEmptyAndSearchIssueTypeIsOnlyNonProject_ReturnEmptyList() {
        log.info("[searchIssue_ScanFileIdsIsEmptyAndSearchIssueTypeIsOnlyNonProject_ReturnEmptyList]");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> resultPageIssue = issueService.searchIssue(scanTask, ruleSet.getId(), ruleSet.getName(), "00001", searchAttrs, new ArrayList<>(), ruleInformationIds, scanFileIds, SearchIssueRequest.SearchIssueType.ONLY_NON_PROJECT, pageable);
        assertEquals(0, resultPageIssue.getTotalElements());
    }

    @Test
    void searchIssue_ScanFileIdsIsNotEmptyAndSearchIssueTypeIsOnlyProject_ReturnIssueList() {
        log.info("[searchIssue_ScanFileIdsIsNotEmptyAndSearchIssueTypeIsOnlyProject_ReturnEmptyList]");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> pagedIssues = new PageImpl<>(issueList);
        when(issueRepository.searchIssueOnlyProjectFile(any(), any(), any(), any(), notNull(), notNull(), notNull(), notNull(), any())).thenReturn(pagedIssues);
        Page<Issue> resultPageIssue = issueService.searchIssue(scanTask, ruleSet.getId(), ruleSet.getName(), "00001", searchAttrs, new ArrayList<>(), ruleInformationIds, scanFileIds, SearchIssueRequest.SearchIssueType.ONLY_PROJECT, pageable);
        assertEquals(pagedIssues.getTotalPages(), resultPageIssue.getTotalPages());
        assertEquals(pagedIssues.getTotalElements(), resultPageIssue.getTotalElements());
    }

    @Test
    void searchIssue_ScanFileIdsIsNotEmptyAndSearchIssueTypeIsProjectAndNonProject_ReturnIssueList() {
        log.info("[searchIssue_ScanFileIdsIsNotEmptyAndSearchIssueTypeIsOnlyProject_ReturnEmptyList]");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> pagedIssues = new PageImpl<>(issueList);
        when(issueRepository.searchIssue(any(ScanTask.class), any(UUID.class), any(), any(), notNull(), notNull(), notNull(), notNull(), any(Pageable.class))).thenReturn(pagedIssues);
        Page<Issue> resultPageIssue = issueService.searchIssue(scanTask, ruleSet.getId(), ruleSet.getName(), "00001", searchAttrs, new ArrayList<>(), ruleInformationIds, scanFileIds, SearchIssueRequest.SearchIssueType.PROJECT_AND_NON_PROJECT, pageable);
        assertEquals(pagedIssues.getTotalPages(), resultPageIssue.getTotalPages());
        assertEquals(pagedIssues.getTotalElements(), resultPageIssue.getTotalElements());
    }

    @Test
    void searchIssue_ScanFileIdsIsEmptyAndSearchIssueTypeIsOnlyNonProject_ReturnIssueList() {
        log.info("[searchIssue_ScanFileIdsIsNotEmptyAndSearchIssueTypeIsOnlyProject_ReturnEmptyList]");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> pagedIssues = new PageImpl<>(issueList);
        when(issueRepository.searchIssueOnlyNonProjectFile(any(), any(), any(), any(), notNull(), notNull(), notNull(), any())).thenReturn(pagedIssues);
        Page<Issue> resultPageIssue = issueService.searchIssue(scanTask, ruleSet.getId(), ruleSet.getName(), "00001", searchAttrs, new ArrayList<>(), ruleInformationIds, new ArrayList<>(), SearchIssueRequest.SearchIssueType.ONLY_NON_PROJECT, pageable);
        assertEquals(pagedIssues.getTotalPages(), resultPageIssue.getTotalPages());
        assertEquals(pagedIssues.getTotalElements(), resultPageIssue.getTotalElements());
    }

    @Test
    void searchIssue_ScanFileIdsIsEmptyAndSearchIssueTypeIsOnlyProject_ReturnIssueList() {
        log.info("[searchIssue_ScanFileIdsIsNotEmptyAndSearchIssueTypeIsOnlyProject_ReturnEmptyList]");
        List<Issue> issueList = Arrays.asList(issue1, issue2);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> pagedIssues = new PageImpl<>(issueList);
        when(issueRepository.searchIssueOnlyProjectFile(any(), any(), any(), any(), notNull(), notNull(), notNull(), notNull(), any())).thenReturn(pagedIssues);
        Page<Issue> resultPageIssue = issueService.searchIssue(scanTask, ruleSet.getId(), ruleSet.getName(), "00001", searchAttrs, new ArrayList<>(), ruleInformationIds, new ArrayList<>(), SearchIssueRequest.SearchIssueType.ONLY_PROJECT, pageable);
        assertEquals(pagedIssues.getTotalPages(), resultPageIssue.getTotalPages());
        assertEquals(pagedIssues.getTotalElements(), resultPageIssue.getTotalElements());
    }

    @Test
    void searchIssue_ScanFileIdsIsEmptyAndSearchIssueTypeIsProjectAndNonProject_ReturnIssueList() {
        log.info("[searchIssue_ScanFileIdsIsNotEmptyAndSearchIssueTypeIsOnlyProject_ReturnEmptyList]");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> pagedIssues = new PageImpl<>(issueList);
        when(issueRepository.searchIssue(any(ScanTask.class), any(UUID.class), any(), any(), notNull(), notNull(), notNull(), notNull(), any(Pageable.class))).thenReturn(pagedIssues);
        Page<Issue> resultPageIssue = issueService.searchIssue(scanTask, ruleSet.getId(), ruleSet.getName(), "00001", searchAttrs, new ArrayList<>(), ruleInformationIds, new ArrayList<>(), SearchIssueRequest.SearchIssueType.PROJECT_AND_NON_PROJECT, pageable);
        assertEquals(pagedIssues.getTotalPages(), resultPageIssue.getTotalPages());
        assertEquals(pagedIssues.getTotalElements(), resultPageIssue.getTotalElements());
    }

    @Test
    void calcIssueStatistics_ScanFileIdsIsEmpty_ReturnIssueStatistics() {
        log.info("[calcIssueStatistics_ScanFileIdsIsEmpty_ReturnIssueStatistics]");
        when(this.issueRepository.findByScanTask(any(ScanTask.class))).thenReturn(issueList);
        List<ScanFile> scanFileList = new ArrayList<>();
        IssueStatisticsResponse response = this.issueService.calcIssueStatistics(scanTask, scanFileList);
        assertEquals(response.getIssueCount(), 2);
        assertEquals(response.getRuleSet().size(), 1);
        assertEquals(response.getRuleSet().get(ruleSet.getName()).getIssueCount(), 2);
        assertEquals(response.getRuleSet().get(ruleSet.getName()).getPriority().get(ruleInformation1.getPriority().toString()), 1);
        assertEquals(response.getRuleSet().get(ruleSet.getName()).getPriority().get(ruleInformation2.getPriority().toString()), 1);
        assertEquals(response.getRuleSet().get(ruleSet.getName()).getRule().get(ruleInformation1.getRuleCode()).getIssueCount(), 1);
        assertEquals(response.getRuleSet().get(ruleSet.getName()).getRule().get(ruleInformation2.getRuleCode()).getIssueCount(), 1);
        assertEquals(response.getRuleSet().get(ruleSet.getName()).getRule().get(ruleInformation1.getRuleCode()).getPriority().get(ruleInformation1.getPriority().toString()), 1);
        assertEquals(response.getRuleSet().get(ruleSet.getName()).getRule().get(ruleInformation2.getRuleCode()).getPriority().get(ruleInformation2.getPriority().toString()), 1);
    }

    @Test
    void countIssueByActionTestSuccess() {
        log.info("[countIssueByActionTestSuccess]");
        when(issueRepository.countByScanTaskAndActionIn(scanTask, Arrays.asList(Issue.Action.CONFIRMED, Issue.Action.CRITICAL))).thenReturn(2L);
        Long result = issueService.countIssueByAction(scanTask, Issue.Action.CONFIRMED, Issue.Action.CRITICAL);
        assertEquals(2L, result);
    }

    @Test
    void countIssueByActionTestSuccess1() {
        log.info("[countIssueByActionTestSuccess1]");
        when(issueRepository.countByScanTaskAndRuleInformationRuleSetAndActionIn(scanTask, ruleSet, Arrays.asList(Issue.Action.CONFIRMED, Issue.Action.CRITICAL))).thenReturn(2L);
        Long result = issueService.countIssueByAction(scanTask, ruleSet, Issue.Action.CONFIRMED, Issue.Action.CRITICAL);
        assertEquals(2L, result);
    }

    @Test
    void findByIdsWithWholeObject_Success() {
        log.info("[findByIdsWithWholeObject_Success]");
        UUID id = UUID.randomUUID();
        RuleInformation ruleInformation = RuleInformation.builder().id(UUID.randomUUID()).build();
        Issue issue = Issue.builder().id(id).ruleInformation(ruleInformation).build();
        when(issueRepository.findByIdWithWholeObject(id)).thenReturn(Optional.of(issue));
        Optional<Issue> issueOptional = issueService.findByIdsWithWholeObject(id);
        assertTrue(issueOptional.isPresent());
        Issue issueWithRuleInformation = issueOptional.get();
        assertEquals(id, issueWithRuleInformation.getId());
        assertNotNull(issueWithRuleInformation.getRuleInformation());
    }

    @Test
    void compareScanResult_InputTwoScanTaskId_ReturnCompareResult() {
        log.info("[compareScanResult_InputTwoScanTaskId_ReturnCompareResult]");
        UUID fromScanTaskId = UUID.randomUUID();
        UUID toScanTaskId = UUID.randomUUID();
        UUID issueId1 = UUID.randomUUID();
        String issueKey1 = "/home/xc5/xcal/xcal-agent/xcal-agent-c/workdir/src/c_testcase/basic/dbf.c@func_1@p@UDR@@@2";
        UUID issueId2 = UUID.randomUUID();
        String issueKey2 = "/home/xc5/xcal/xcal-agent/xcal-agent-c/workdir/src/c_testcase/basic/uiv.c@main@b@UIV@@@3";
        UUID issueId3 = UUID.randomUUID();
        String issueKey3 = "/home/xc5/xcal/xcal-agent/xcal-agent-c/workdir/src/c_testcase/basic/dbf.c@func_1@@RBC@CERT@MSC37-C@2";
        UUID issueId4 = UUID.randomUUID();
        String issueKey4 = "/home/xc5/xcal/xcal-agent/xcal-agent-c/workdir/src/c_testcase/basic/uiv.c@main@b@UIV@@@3";
        List<CompareIssueObject> fromCompareIssueObjects = Arrays.asList(CompareIssueObject.builder().id(issueId1).issueKey(issueKey1).build(), CompareIssueObject.builder().id(issueId2).issueKey(issueKey2).build());
        List<CompareIssueObject> toCompareIssueObjects = Arrays.asList(CompareIssueObject.builder().id(issueId3).issueKey(issueKey3).build(), CompareIssueObject.builder().id(issueId4).issueKey(issueKey4).build());
        when(issueRepository.findCompareIssueObjectByScanTaskId(fromScanTaskId)).thenReturn(fromCompareIssueObjects);
        when(issueRepository.findCompareIssueObjectByScanTaskId(toScanTaskId)).thenReturn(toCompareIssueObjects);
        CompareScanResultResponse compareScanResultResponse = issueService.compareScanResult(fromScanTaskId, toScanTaskId);
        assertEquals(1, compareScanResultResponse.getNewIssueIds().size());
        assertEquals(issueId3, compareScanResultResponse.getNewIssueIds().get(0));
        assertEquals(1, compareScanResultResponse.getFixedIssueIds().size());
        assertEquals(issueId1, compareScanResultResponse.getFixedIssueIds().get(0));
    }

    @Test
    void findIssuesByIds_InputIssueIds_ReturnIssueList() {
        log.info("[findIssuesByIds_InputIssueIds_ReturnIssueList]");
        UUID issueId1 = UUID.randomUUID();
        UUID issueId2 = UUID.randomUUID();
        Issue issue1 = Issue.builder().id(issueId1).build();
        Issue issue2 = Issue.builder().id(issueId2).build();
        List<Issue> issueList = Arrays.asList(issue1, issue2);
        when(issueRepository.findByIdIn(Arrays.asList(issueId1, issueId2))).thenReturn(issueList);
        List<Issue> resultIssueList = issueService.findIssuesByIds(Arrays.asList(issueId1, issueId2));
        assertEquals(2, resultIssueList.size());
        assertEquals(issue1.getId(), resultIssueList.get(0).getId());
        assertEquals(issue2.getId(), resultIssueList.get(1).getId());
    }

    @Test
    void constructImportIssueResponse_Success() {
        ImportIssueResponse importIssueResponse = issueService.constructImportIssueResponse(scanTask, issueList);
        assertEquals(scanTask.getId(), importIssueResponse.getScanTaskId());
        assertEquals(scanTask.getSummary(), importIssueResponse.getSummary());
        assertEquals(issueList.get(0).getId(), importIssueResponse.getIssues().get(0).getId());
        assertEquals(issueList.get(1).getId(), importIssueResponse.getIssues().get(1).getId());
    }

    @Test
    void convertIssueTraceToIssueTraceOfIssueOfImportIssueResponse_Success() {
        UUID issueTraceId = UUID.randomUUID();
        UUID scanFileId = UUID.randomUUID();
        IssueTrace issueTrace = IssueTrace.builder().
                scanFile(ScanFile.builder().id(scanFileId).projectRelativePath("src/protocol.h").storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/src/protocol.h")
                        .fileInfo(FileInfo.builder().noOfLines(100).fileSize(10000L).relativePath("src/protocol.h").build())
                        .build())
                .filePath("/share/scan/a140d21d-948d-454c-bb42-061d784b4016/src/core/ngx_palloc.c")
                .id(issueTraceId)
                .seq(21)
                .lineNo(381)
                .columnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("Then block is taken")
                .build();

        ImportIssueResponse.Issue.IssueTrace issueTraceOfImportIssueResponse = IssueService.convertIssueTraceToIssueTraceOfIssueOfImportIssueResponse(issueTrace);
        assertEquals(issueTraceId, issueTraceOfImportIssueResponse.getId());
        assertEquals(scanFileId, issueTraceOfImportIssueResponse.getScanFileId());
    }

    @Test
    void convertIssueTraceToIssueTraceOfIssueOfImportIssueResponse_NoScanFile_Success() {
        ImportIssueResponse.Issue.IssueTrace issueTraceOfImportIssueResponse = IssueService.convertIssueTraceToIssueTraceOfIssueOfImportIssueResponse(issueTrace221);
        assertEquals(issueTrace221.getId(), issueTraceOfImportIssueResponse.getId());
        assertNull(issueTraceOfImportIssueResponse.getScanFileId());
        assertEquals(issueTrace221.getFilePath(), issueTraceOfImportIssueResponse.getRelativePath());
        assertEquals(issueTrace221.getFilePath(), issueTraceOfImportIssueResponse.getScanFilePath());
    }

    @Test
    void convertIssueTraceToIssueTraceOfIssueOfImportIssueResponse_NoFileInfo_Success() {
        ImportIssueResponse.Issue.IssueTrace issueTraceOfImportIssueResponse = IssueService.convertIssueTraceToIssueTraceOfIssueOfImportIssueResponse(issueTrace212);
        assertEquals(issueTrace212.getId(), issueTraceOfImportIssueResponse.getId());
        assertEquals(issueTrace212.getScanFile().getId(), issueTraceOfImportIssueResponse.getScanFileId());
        assertEquals(issueTrace212.getScanFile().getProjectRelativePath(), issueTraceOfImportIssueResponse.getRelativePath());
        assertEquals(issueTrace212.getScanFile().getStorePath(), issueTraceOfImportIssueResponse.getScanFilePath());
    }

    @Test
    void convertIssueToIssueOfImportIssueResponse_Success() {
        ImportIssueResponse.Issue issueOfImportIssueResponse = IssueService.convertIssueToIssueOfImportIssueResponse(issue1);
        assertEquals(issue1.getId(), issueOfImportIssueResponse.getId());
    }

    @Test
    void convertIssueToDto_Success() {
        IssueDto issueDto = issueService.convertIssueToDto(issue1, Locale.ENGLISH);
        assertEquals(issue1.getId(), issueDto.getId());
        assertEquals(issue1.getIssueKey(), issueDto.getIssueKey());
        assertEquals(issue1.getRuleInformation().getId(), issueDto.getRuleInformation().getId());
        // convertIssueToDto do not convert traces
        assertEquals(0, issueDto.getIssueTraces().size());
    }

    @Test
    void convertIssueToDto_Issue2_Success() {
        IssueDto issueDto = issueService.convertIssueToDto(issue2, Locale.ENGLISH);
        assertEquals(issue2.getId(), issueDto.getId());
        assertEquals(issue2.getIssueKey(), issueDto.getIssueKey());
        assertEquals(issue2.getRuleInformation().getId(), issueDto.getRuleInformation().getId());
        // convertIssueToDto do not convert traces
        assertEquals(0, issueDto.getIssueTraces().size());
    }

    @Test
    void convertIssueToDto_Issue3_Success() {
        IssueDto issueDto = issueService.convertIssueToDto(issue3, Locale.ENGLISH);
        assertEquals(issue3.getId(), issueDto.getId());
        assertEquals(issue3.getIssueKey(), issueDto.getIssueKey());
        assertEquals(issue3.getRuleInformation().getId(), issueDto.getRuleInformation().getId());
        // convertIssueToDto do not convert traces
        assertEquals(0, issueDto.getIssueTraces().size());
    }

    @Test
    void convertScanTaskToImportIssueDiffResponseScanTask_Success() {
        ImportIssueDiffResponse.ScanTask scanTaskResult = IssueService.convertScanTaskToImportIssueDiffResponseScanTask(scanTask);
        assertEquals(scanTask.getId(), scanTaskResult.getId());
        assertEquals(scanTask.getProject().getId(), scanTaskResult.getProjectUuid());
        assertEquals(scanTask.getProject().getProjectId(), scanTaskResult.getProjectId());
        assertEquals(scanTask.getProject().getName(), scanTaskResult.getProjectName());
        assertEquals(scanTask.getStatus().name(), scanTaskResult.getStatus());
        assertEquals(scanTask.getSourceRoot(), scanTaskResult.getSourceRoot());
    }

    @Test
    void getIssueDiff_Success() {
        List<IssueDiff> expectedIssueDiffs = Arrays.asList(
                IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(issue1).issueKey(issue1.getIssueKey()).type(IssueDiff.Type.FIXED).build(),
                IssueDiff.builder().id(UUID.randomUUID()).scanTask(scanTask).issue(issue2).issueKey(issue2.getIssueKey()).type(IssueDiff.Type.NEW_PATH).build());
        when(issueDiffRepository.findByScanTask(any(ScanTask.class))).thenReturn(expectedIssueDiffs);
        List<IssueDiff> result = issueService.getIssueDiff(scanTask);
        assertEquals(expectedIssueDiffs, result);
    }

    @Test
    void findByIssueAndChecksum_Success() {
        when(issueTraceRepository.findByIssueAndChecksum(any(Issue.class), eq(issueTraces21.get(0).getChecksum()))).thenReturn(issueTraces21);
        when(issueTraceRepository.findByIssueAndChecksum(any(Issue.class), eq(issueTraces22.get(0).getChecksum()))).thenReturn(issueTraces22);
        List<IssueTrace> result1 = this.issueService.findByIssueAndChecksum(issue2, issueTraces21.get(0).getChecksum());
        assertEquals(2, result1.size());
        assertTrue(issueTraces21.stream().map(IssueTrace::getId).anyMatch(id -> id.equals(issueTrace211.getId())));
        assertTrue(issueTraces21.stream().map(IssueTrace::getId).anyMatch(id -> id.equals(issueTrace212.getId())));

        List<IssueTrace> result2 = this.issueService.findByIssueAndChecksum(issue2, issueTraces22.get(0).getChecksum());
        assertEquals(1, result2.size());
        assertTrue(issueTraces22.stream().map(IssueTrace::getId).anyMatch(id -> id.equals(issueTrace221.getId())));
    }

    @Test
    void countIssueByRuleInformation_Success() {
        Long expectedValue = 3L;
        when(this.issueRepository.countByScanTaskAndRuleInformationIn(eq(scanTask), anyList())).thenReturn(expectedValue);
        Long result = this.issueService.countIssueByRuleInformation(scanTask, Collections.singletonList(ruleInformation1));
        assertEquals(expectedValue, result);
    }

    @Test
    void _Success() {
        Long expectedValue = 3L;
        when(this.issueRepository.countByScanTaskAndRuleInformationRuleSetAndRuleInformationPriorityAndAction(eq(scanTask), eq(ruleSet), any(), any())).thenReturn(expectedValue);
        Long result = this.issueService.countIssueByPriorityAndAction(scanTask, ruleSet, RuleInformation.Priority.HIGH, null);
        assertEquals(expectedValue, result);
    }

    @Test
    void countIssueByPriority_Success() {
        Long expectedValue1 = 3L;
        Long expectedValue2 = 30L;
        when(this.issueRepository.countByScanTaskAndRuleInformationRuleSetAndRuleInformationPriorityIn(eq(scanTask), eq(ruleSet), argThat(l -> l.size() == 1))).thenReturn(expectedValue1);
        when(this.issueRepository.countByScanTaskAndRuleInformationRuleSetAndRuleInformationPriorityIn(eq(scanTask), eq(ruleSet), argThat(l -> l.size() == 2))).thenReturn(expectedValue2);
        Long result1 = this.issueService.countIssueByPriority(scanTask, ruleSet, RuleInformation.Priority.HIGH);
        assertEquals(expectedValue1, result1);

        Long result2 = this.issueService.countIssueByPriority(scanTask, ruleSet, RuleInformation.Priority.HIGH, RuleInformation.Priority.LOW);
        assertEquals(expectedValue2, result2);
    }

    @Test
    void saveImportIssueResponseToFile_Success() throws AppException {
        FileInfo expected = FileInfo.builder().id(UUID.randomUUID()).relativePath("/test/dummy/path.abc").build();
        when(fileService.writeObjectToFile(any(ImportIssueResponse.class), anyString(), eq(".view"), anyString())).thenReturn(expected);
        ImportIssueResponse importIssueResponse = ImportIssueResponse.builder().scanTaskId(scanTask.getId()).build();
        FileInfo result = this.issueService.saveImportIssueResponseToFile(importIssueResponse, currentUser.getUsername());
        assertEquals(expected, result);
    }

    @Test
    void processIssueDiffFile_FileNotExists_Success() throws AppException {
        ImportIssueDiffRequest request = this.issueService.processIssueDiffFile(null, null, baselineScanTask, scanTask, "test");
        assertEquals(request.getBaselineScanTaskId(), baselineScanTask.getId());
        assertEquals(request.getScanTaskId(), scanTask.getId());
    }

    @Test
    void importIssueDiff_Success() throws AppException {
        final Date now = new Date();
        final Issue issue11 = Issue.builder()
                .id(UUID.randomUUID())
                .scanTask(baselineScanTask)
                .issueCode(ruleInformation1.getRuleCode())
                .seq("00001")
                .severity(Issue.Severity.HIGH)
                .lineNo(381)
                .columnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("testIssueMessage")
                .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c@ngx_vslprintf@buf@NPD@@@fffffa68")
                .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c")
                .checksum("testChecksum1")
                .ruleInformation(ruleInformation1)
                .action(Issue.Action.CONFIRMED)
                .assignTo(currentUser)
                .createdBy(currentUser.getUsername())
                .createdOn(now)
                .modifiedBy(currentUser.getUsername())
                .modifiedOn(now)
                .status(Issue.Status.ACTIVE)
                .build();
        final Issue issue12 = Issue.builder()
                .id(UUID.randomUUID())
                .scanTask(baselineScanTask)
                .issueCode(ruleInformation1.getRuleCode())
                .seq("00002")
                .severity(Issue.Severity.HIGH)
                .lineNo(381)
                .columnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("uiv")
                .message("testIssueMessage")
                .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c@ngx_vslprintf@uiv@NPD@@@fffffa68")
                .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c")
                .checksum("testChecksum2")
                .ruleInformation(ruleInformation1)
                .action(Issue.Action.CONFIRMED)
                .assignTo(currentUser)
                .createdBy(currentUser.getUsername())
                .createdOn(now)
                .modifiedBy(currentUser.getUsername())
                .modifiedOn(now)
                .status(Issue.Status.ACTIVE)
                .build();
        final Issue issue21 = Issue.builder()
                .id(UUID.randomUUID())
                .scanTask(scanTask)
                .issueCode(ruleInformation1.getRuleCode())
                .seq("00002")
                .severity(Issue.Severity.HIGH)
                .lineNo(381)
                .columnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("uiv")
                .message("testIssueMessage")
                .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c@ngx_vslprintf@uiv@NPD@@@fffffa68")
                .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c")
                .checksum("testChecksum2")
                .ruleInformation(ruleInformation1)
                .action(Issue.Action.CONFIRMED)
                .assignTo(currentUser)
                .createdBy(currentUser.getUsername())
                .createdOn(now)
                .modifiedBy(currentUser.getUsername())
                .modifiedOn(now)
                .status(Issue.Status.ACTIVE)
                .build();
        final Issue issue22 = Issue.builder()
                .id(UUID.randomUUID())
                .scanTask(scanTask)
                .issueCode(ruleInformation1.getRuleCode())
                .seq("00003")
                .severity(Issue.Severity.HIGH)
                .lineNo(381)
                .columnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("testIssueMessage")
                .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c@ngx_vslprintf@buf@NPD@@@fffffa68")
                .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c")
                .checksum("testChecksum3")
                .ruleInformation(ruleInformation1)
                .action(Issue.Action.CONFIRMED)
                .assignTo(currentUser)
                .createdBy(currentUser.getUsername())
                .createdOn(now)
                .modifiedBy(currentUser.getUsername())
                .modifiedOn(now)
                .status(Issue.Status.ACTIVE)
                .build();
        final IssueDiff issueDiff1 = IssueDiff.builder()
                .id(UUID.randomUUID())
                .baselineScanTask(baselineScanTask)
                .scanTask(scanTask)
                .issue(issue11)
                .issueKey(issue11.getIssueKey())
                .checksum(issue11.getChecksum())
                .type(IssueDiff.Type.FIXED)
                .build();
        final IssueDiff issuePathDiff1 = IssueDiff.builder()
                .id(UUID.randomUUID())
                .baselineScanTask(baselineScanTask)
                .scanTask(scanTask)
                .issue(issue11)
                .issueKey(issue11.getIssueKey())
                .checksum(issue11.getChecksum())
                .type(IssueDiff.Type.FIXED_PATH)
                .build();
        final IssueDiff issueDiff2 = IssueDiff.builder()
                .id(UUID.randomUUID())
                .baselineScanTask(baselineScanTask)
                .scanTask(scanTask)
                .issue(issue22)
                .issueKey(issue22.getIssueKey())
                .checksum(issue22.getChecksum())
                .type(IssueDiff.Type.NEW)
                .build();
        final IssueDiff issuePathDiff2 = IssueDiff.builder()
                .id(UUID.randomUUID())
                .baselineScanTask(baselineScanTask)
                .scanTask(scanTask)
                .issue(issue22)
                .issueKey(issue22.getIssueKey())
                .checksum(issue22.getChecksum())
                .type(IssueDiff.Type.NEW_PATH)
                .build();

        List<ImportIssueDiffRequest.IssueDiff> fixedIssueDiff = Collections.singletonList(
                ImportIssueDiffRequest.IssueDiff.builder()
                        .issueKey(issue11.getIssueKey())
                        .checksum(issue11.getChecksum())
                        .build()
        );
        List<ImportIssueDiffRequest.IssueDiff> fixedIssuePathDiff = Collections.singletonList(
                ImportIssueDiffRequest.IssueDiff.builder()
                        .issueKey(issue11.getIssueKey())
                        .checksum(issue11.getChecksum())
                        .build()
        );
        List<ImportIssueDiffRequest.IssueDiff> newIssueDiff = Collections.singletonList(
                ImportIssueDiffRequest.IssueDiff.builder()
                        .issueKey(issue22.getIssueKey())
                        .checksum(issue22.getChecksum())
                        .build()
        );
        List<ImportIssueDiffRequest.IssueDiff> newIssuePathDiff = Collections.singletonList(
                ImportIssueDiffRequest.IssueDiff.builder()
                        .issueKey(issue22.getIssueKey())
                        .checksum(issue22.getChecksum())
                        .build()
        );

        ImportIssueDiffRequest importIssueDiffRequest = ImportIssueDiffRequest.builder()
                .baselineScanTaskId(baselineScanTask.getId())
                .scanTaskId(scanTask.getId())
                .newIssue(newIssueDiff)
                .newIssuePath(newIssuePathDiff)
                .fixedIssue(fixedIssueDiff)
                .fixedIssuePath(fixedIssuePathDiff)
                .build();

        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        when(this.scanTaskService.findById(baselineScanTask.getId())).thenReturn(Optional.of(baselineScanTask));

        when(this.issueRepository.findByScanTaskAndIssueKey(scanTask, issue21.getIssueKey())).thenReturn(Optional.of(issue21));
        when(this.issueRepository.findByScanTaskAndIssueKey(scanTask, issue22.getIssueKey())).thenReturn(Optional.of(issue22));
        when(this.issueRepository.findByScanTaskAndIssueKey(baselineScanTask, issue11.getIssueKey())).thenReturn(Optional.of(issue11));
        when(this.issueRepository.findByScanTaskAndIssueKey(baselineScanTask, issue12.getIssueKey())).thenReturn(Optional.of(issue12));

        when(this.issueDiffRepository.save(any(IssueDiff.class))).thenAnswer(invocation -> {
            IssueDiff issueDiff = invocation.getArgument(0);
            if ((issueDiff.getScanTask().getId() == issueDiff1.getScanTask().getId())
                    && (issueDiff.getIssue().getIssueKey().equals(issueDiff1.getIssue().getIssueKey()))
                    && (issueDiff.getIssue().getChecksum().equals(issueDiff1.getIssue().getChecksum()))) {
                return issueDiff1;
            }
            if ((issueDiff.getScanTask().getId() == issuePathDiff1.getScanTask().getId())
                    && (issueDiff.getIssue().getIssueKey().equals(issuePathDiff1.getIssue().getIssueKey()))
                    && (issueDiff.getIssue().getChecksum().equals(issuePathDiff1.getIssue().getChecksum()))) {
                return issuePathDiff1;
            }
            if ((issueDiff.getScanTask().getId() == issueDiff2.getScanTask().getId())
                    && (issueDiff.getIssue().getIssueKey().equals(issueDiff2.getIssue().getIssueKey()))
                    && (issueDiff.getIssue().getChecksum().equals(issueDiff2.getIssue().getChecksum()))) {
                return issueDiff2;
            }
            if ((issueDiff.getScanTask().getId() == issuePathDiff2.getScanTask().getId())
                    && (issueDiff.getIssue().getIssueKey().equals(issuePathDiff2.getIssue().getIssueKey()))
                    && (issueDiff.getIssue().getChecksum().equals(issuePathDiff2.getIssue().getChecksum()))) {
                return issuePathDiff2;
            }
            throw new InvalidUseOfMatchersException("Unexpected parameter");
        });

        ImportIssueDiffResponse result = this.issueService.importIssueDiff(importIssueDiffRequest, currentUser.getUsername());
        assertEquals(baselineScanTask.getId(), result.getBaselineScanTask().getId());
        assertEquals(scanTask.getId(), result.getScanTask().getId());
        assertEquals(fixedIssueDiff.size(), result.getFixedIssue().stream().filter(issueDiff -> StringUtils.equals("SUCCESS", issueDiff.getResult())).count());
        assertEquals(fixedIssuePathDiff.size(), result.getFixedIssuePath().stream().filter(issueDiff -> StringUtils.equals("SUCCESS", issueDiff.getResult())).count());
        assertEquals(newIssueDiff.size(), result.getNewIssue().stream().filter(issueDiff -> StringUtils.equals("SUCCESS", issueDiff.getResult())).count());
        assertEquals(newIssuePathDiff.size(), result.getNewIssuePath().stream().filter(issueDiff -> StringUtils.equals("SUCCESS", issueDiff.getResult())).count());
    }

    @Test
    void asyncImportIssueToScanTask_AsyncJobNotFound_Success() throws IOException, AppException {
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.empty());
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
        assertEquals(asyncJob.getStatus(), AsyncJob.Status.CREATED);
    }

    @Test
    void asyncImportIssueToScanTask_AsyncJobIsRunning_Success() throws IOException, AppException {
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .status(AsyncJob.Status.RUNNING)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
        assertEquals(asyncJob.getStatus(), AsyncJob.Status.RUNNING);
    }

    @Test
    void asyncImportIssueToScanTask_AsyncJobIsCompleted_Success() throws IOException, AppException {
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .status(AsyncJob.Status.COMPLETED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
        assertEquals(asyncJob.getStatus(), AsyncJob.Status.COMPLETED);
    }

    @Test
    void asyncImportIssueToScanTask_ScanTaskNotFound_Success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask("")
                .issueFile("")
                .fixedIssueFile("")
                .newIssueFile("")
                .step(0)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.empty());
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
        assertEquals(asyncJob.getResult(), "invalid scan task");
        assertEquals(asyncJob.getStatus(), AsyncJob.Status.FAILED);
    }

    @Test
    void asyncImportIssueToScanTask_IssueFileIsEmpty_Success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask("")
                .issueFile("")
                .fixedIssueFile("")
                .newIssueFile("")
                .step(0)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
        assertEquals(asyncJob.getResult(), "invalid issue file path");
        assertEquals(asyncJob.getStatus(), AsyncJob.Status.FAILED);
    }

    @Test
    void asyncImportIssueToScanTask_IssueFileNotExists_Success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask("")
                .issueFile("test.txt")
                .fixedIssueFile("")
                .newIssueFile("")
                .step(0)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
//        assertEquals("CSF only mode. Skipped", asyncJob.getResult());
//        assertEquals(AsyncJob.Status.COMPLETED, asyncJob.getStatus());
        assertEquals("issue file not exists", asyncJob.getResult());
        assertEquals(AsyncJob.Status.FAILED, asyncJob.getStatus());
    }

    @Test
    void asyncImportIssueToScanTask_BaselineScanTaskNotFound_Success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask(baselineScanTask.getId().toString())
                .issueFile("")
                .fixedIssueFile("")
                .newIssueFile("")
                .step(1)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        when(this.scanTaskService.findById(baselineScanTask.getId())).thenReturn(Optional.empty());
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
        assertEquals(asyncJob.getResult(), "invalid baseline scan task");
        assertEquals(asyncJob.getStatus(), AsyncJob.Status.FAILED);
    }

    @Test
    void asyncImportIssueToScanTask_FixedIssueFileNotExists_Success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask(baselineScanTask.getId().toString())
                .issueFile("")
                .fixedIssueFile("test.txt")
                .newIssueFile("")
                .step(1)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        when(this.scanTaskService.findById(baselineScanTask.getId())).thenReturn(Optional.of(baselineScanTask));
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
        assertEquals(asyncJob.getResult(), "fixed issue file not exists");
        assertEquals(asyncJob.getStatus(), AsyncJob.Status.FAILED);
    }

    @Test
    void asyncImportIssueToScanTask_NewIssueFileNotExists_Success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask(baselineScanTask.getId().toString())
                .issueFile("")
                .fixedIssueFile("")
                .newIssueFile("test.txt")
                .step(1)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        when(this.scanTaskService.findById(baselineScanTask.getId())).thenReturn(Optional.of(baselineScanTask));
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
        assertEquals(asyncJob.getResult(), "new issue file not exists");
        assertEquals(asyncJob.getStatus(), AsyncJob.Status.FAILED);
    }

    @Test
    void asyncImportIssueToScanTask_UpdateScanTaskStatus_Success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UpdateScanTaskRequest request = UpdateScanTaskRequest.builder()
                .id(scanTask.getId())
                .stage("SCAN_COMPLETE")
                .status("COMPLETED")
                .message("test")
                .build();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask("")
                .issueFile("")
                .fixedIssueFile("")
                .newIssueFile("")
                .updateScanTaskRequest(request)
                .step(2)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        when(this.scanTaskService.updateScanTaskStatus(request, info.getUsername())).thenReturn(scanTask);
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
        assertEquals(asyncJob.getResult(), "import issue to scan task succeed");
        assertEquals(asyncJob.getStatus(), AsyncJob.Status.COMPLETED);
    }

    @Test
    void asyncImportIssueToScanTask_InvalidCsfMagic_AppException() {
        Assertions.assertThrows(AppException.class, () -> {
            String originalFile = "src/test/lua_simple_invalid_magic.csf";
            String scanTempFile = "src/test/temp_lua_simple_invalid_magic.csf";

            Path copied = Paths.get(scanTempFile);
            Path originalPath = Paths.get(originalFile);
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);


            ObjectMapper mapper = new ObjectMapper();
            AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                    .username("test")
                    .scanTask(scanTask.getId().toString())
                    .baselineScanTask("")
                    .issueFile(scanTempFile)
                    .fixedIssueFile("")
                    .newIssueFile("")
                    .step(0)
                    .build();
            AsyncJob asyncJob = AsyncJob.builder()
                    .id(UUID.randomUUID())
                    .info(mapper.writeValueAsString(info))
                    .status(AsyncJob.Status.CREATED)
                    .build();

            RandomAccessFile issueRandomAccessFile = mock(RandomAccessFile.class);

            when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
            when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
            when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
            IssueService issueServiceSpy = spy(issueService);
            doThrow(IOException.class).when(issueServiceSpy).insertIssueFile(any(), any(), any(), anyLong(), anyLong(), any(String[].class),anySet());

            issueServiceSpy.enableCsvImport = true;
            issueServiceSpy.asyncImportIssueToScanTask(asyncJob.getId().toString());

        });
    }

    //TODO:Raymond, add back when failure reason is found. Successful in some env and fail in others
    @Test
    void asyncImportIssueToScanTask_Csv_IOException() throws Exception {
        String originalFile = "src/test/lua_simple.csf";
        String scanTempFile = "src/test/temp_lua_simple.csf";

        Path copied = Paths.get(scanTempFile);
        Path originalPath = Paths.get(originalFile);
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);


        ObjectMapper mapper = new ObjectMapper();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask("")
                .issueFile(scanTempFile)
                .fixedIssueFile("")
                .newIssueFile("")
                .step(0)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();

        RandomAccessFile issueRandomAccessFile = new RandomAccessFile(scanTempFile, "r");

        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        IssueService issueServiceSpy=spy(issueService);
        doThrow(IOException.class).when(issueServiceSpy).insertIssueFile(any(),any(),any(),anyLong(),anyLong(), any(),anySet());

        issueServiceSpy.enableCsvImport=true;
        assertThrows(IOException.class,()->{
            issueServiceSpy.asyncImportIssueToScanTask(asyncJob.getId().toString());
        });
    }


    @Test
    void asyncImportIssueToScanTask_Csf_Success() throws Exception {
        String originalFile = "src/test/lua_simple.csf";
        String scanTempFile = "src/test/temp_lua_simple.csf";

        Path copied = Paths.get(scanTempFile);
        Path originalPath = Paths.get(originalFile);
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);

        ObjectMapper mapper = new ObjectMapper();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask("")
                .issueFile(scanTempFile)
                .fixedIssueFile("")
                .newIssueFile("")
                .step(0)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        this.issueService.enableCsvImport=true;
        issueService.executionMode = "DEBUG";

        IssueService issueServiceSpy = Mockito.spy(issueService);
        doNothing().when(issueServiceSpy).importCsf(any(), any(),any(),any(),any(),any(),any());
        issueServiceSpy.asyncImportIssueToScanTask(asyncJob.getId().toString());

        assertEquals("import issue to scan task succeed",asyncJob.getResult());
        assertEquals( AsyncJob.Status.COMPLETED,asyncJob.getStatus());

    }


    //TODO:Raymond, add back when failure reason is found. Successful in some env and fail in others
    @Test
    void asyncImportIssueToScanTask_DotV_Success() throws Exception {
        String originalFile = "src/test/tnpd_1.v";
        String scanTempFile = "src/test/temp_tnpd_1.v";

        Path copied = Paths.get(scanTempFile);
        Path originalPath = Paths.get(originalFile);
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);


        ObjectMapper mapper = new ObjectMapper();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask("")
                .issueFile(scanTempFile)
                .fixedIssueFile("")
                .newIssueFile("")
                .step(0)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());
        assertEquals("import issue to scan task succeed", asyncJob.getResult());
        assertEquals(AsyncJob.Status.COMPLETED, asyncJob.getStatus());
    }

    //TODO: Raymond, add back when new small case is provided
    @Test
    void asyncImportIssueToScanTask_CsvWithRuleInformation_Success() throws Exception {
        String[] dummyHeader = new String[]{};
        CsfReader csfReader = CsfReaderV08.getInstance();
        String originalFile = "src/test/test_v081.csf";
        String scanTempFile = "src/test/test_v081.csf";

        Path copied = Paths.get(scanTempFile);
        Path originalPath = Paths.get(originalFile);
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);


        ObjectMapper mapper = new ObjectMapper();
        AsyncJob.IssueJobInfo info = AsyncJob.IssueJobInfo.builder()
                .username("test")
                .scanTask(scanTask.getId().toString())
                .baselineScanTask("")
                .issueFile(scanTempFile)
                .fixedIssueFile("")
                .newIssueFile("")
                .step(0)
                .build();
        AsyncJob asyncJob = AsyncJob.builder()
                .id(UUID.randomUUID())
                .info(mapper.writeValueAsString(info))
                .status(AsyncJob.Status.CREATED)
                .build();
        when(this.asyncJobService.findAsyncJobById(asyncJob.getId())).thenReturn(Optional.of(asyncJob));
        when(this.asyncJobService.updateAsyncJob(asyncJob)).thenReturn(asyncJob);
        when(this.scanTaskService.findById(scanTask.getId())).thenReturn(Optional.of(scanTask));
        when(this.ruleServiceV3.getAllRuleInfo(any())).thenReturn(RuleListResponseDto.builder()
                .build());

        issueService.enableCsvImport = true;
        issueService.executionMode = "DEBUG";
        this.issueService.asyncImportIssueToScanTask(asyncJob.getId().toString());

        assertEquals("import issue to scan task succeed", asyncJob.getResult());
        assertEquals(AsyncJob.Status.COMPLETED, asyncJob.getStatus());
    }

    //Success
    @Test
    void asyncUpdateProcStatus_success_noError() throws AppException {

        doReturn(scanTask).when(scanTaskService).updateScanTaskStatus(any(UpdateScanTaskRequest.class), anyString());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("scanTaskId",UUID.randomUUID());
        jsonObject.put("status","SUCC");
        assertDoesNotThrow(() -> {
            issueService.asyncUpdateProcStatus(jsonObject.toString());
        });
    }

    //Fail case
    @Test
    void asyncUpdateProcStatus_fail_noError() throws AppException {

        doReturn(scanTask).when(scanTaskService).updateScanTaskStatus(any(UpdateScanTaskRequest.class), anyString());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("scanTaskId",UUID.randomUUID());
        jsonObject.put("status","FAILED");
        assertDoesNotThrow(() -> {
            issueService.asyncUpdateProcStatus(jsonObject.toString());
        });
    }

    //Terminated case
    @Test
    void asyncUpdateProcStatus_cancel_noError() throws AppException {

        doReturn(scanTask).when(scanTaskService).updateScanTaskStatus(any(UpdateScanTaskRequest.class), anyString());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("scanTaskId",UUID.randomUUID());
        jsonObject.put("status","CANCEL");
        assertDoesNotThrow(() -> {
            issueService.asyncUpdateProcStatus(jsonObject.toString());
        });
    }
    //Exception

    @Test
    void asyncUpdateProcStatus_exceptionWhileUpdate_noError() throws AppException {

        doThrow(new RuntimeException("Test Exception")).when(scanTaskService).updateScanTaskStatus(any(UpdateScanTaskRequest.class), anyString());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("scanTaskId",UUID.randomUUID());
        jsonObject.put("status","CANCEL");
        assertDoesNotThrow(() -> {
            issueService.asyncUpdateProcStatus(jsonObject.toString());
        });
    }


}
