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
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.SearchIssueRequest;
import com.xcal.api.repository.IssueRepository;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.mockito.Mockito.*;

@Slf4j
class ImportServiceTest {

    private ScanTaskService scanTaskService;
    private SettingService settingService;
    private IssueRepository issueRepository;
    private ImportService importService;

    private IssueService issueService;
    Tracer tracer;



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

        scanTaskService = mock(ScanTaskService.class);
        settingService = mock(SettingService.class);


        AppProperties appProperties = mock(AppProperties.class);
        issueRepository = mock(IssueRepository.class);
        ScanFileService scanFileService = mock(ScanFileService.class);
        I18nService i18nService = mock(I18nService.class);
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

        when(mockSpan.setTag(anyString(), anyString())).thenReturn(mockSpan);
        when(mockSpan.setTag(anyString(), anyInt())).thenReturn(mockSpan);
        when(mockSpan.setTag(anyString(), anyLong())).thenReturn(mockSpan);

        SpanContext spanContext = mock(SpanContext.class);
        when(spanContext.toSpanId()).thenReturn("1111-1111");
        when(mockSpan.context()).thenReturn(spanContext);

        projectConfig.setAttributes(attrs);

        when(i18nService.getI18nMessageByKey(any(), any(Locale.class))).thenReturn(new ArrayList<>());

        issueService = mock(IssueService.class);
        importService = new ImportService(scanTaskService, issueService, tracer);

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
    void syncImportScanResult_scanTaskNotExist_AppException() {
        Assertions.assertThrows(AppException.class, () -> {
            String originalFile = "src/test/lua_simple_invalid_magic.csf";
            String scanTempFile = "src/test/temp_lua_simple_invalid_magic.csf";

            Path copied = Paths.get(scanTempFile);
            Path originalPath = Paths.get(originalFile);
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);

            File tempIssueFile = new File(scanTempFile);

            importService.syncImportScanResult(tempIssueFile, null, UUID.fromString("11111111-1111-1111-1110-111111111111"), "admin");

        });
    }

    @Test
    void syncImportScanResult_projectNotExist_AppException() {
        Assertions.assertThrows(AppException.class, () -> {
            String originalFile = "src/test/lua_simple_invalid_magic.csf";
            String scanTempFile = "src/test/temp_lua_simple_invalid_magic.csf";

            Path copied = Paths.get(scanTempFile);
            Path originalPath = Paths.get(originalFile);
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);

            File tempIssueFile = new File(scanTempFile);

            Project project = Project.builder().id(UUID.randomUUID()).projectId("test_project_id").name("Test Project").build();
            List<ProjectConfigAttribute> attrs = Arrays.asList(
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

            List<SearchIssueRequest.IssueAttribute> searchAttrs = Collections.singletonList(SearchIssueRequest.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.PRIORITY.toString()).values(Arrays.asList("RANK_8", "RANK_19")).build());
            ProjectConfig projectConfig = ProjectConfig.builder().id(UUID.randomUUID()).name("default").project(project).status(ProjectConfig.Status.ACTIVE).attributes(attrs).build();
            ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PENDING).projectConfig(projectConfig).build();


            importService.syncImportScanResult(tempIssueFile, scanTask, UUID.fromString("11111111-1111-1111-1110-111111111111"), "admin");

        });
    }


    @Test
    void syncImportScanResult_projectConfigNotExist_AppException() {
        Assertions.assertThrows(AppException.class, () -> {
            String originalFile = "src/test/lua_simple_invalid_magic.csf";
            String scanTempFile = "src/test/temp_lua_simple_invalid_magic.csf";

            Path copied = Paths.get(scanTempFile);
            Path originalPath = Paths.get(originalFile);
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);

            File tempIssueFile = new File(scanTempFile);

            Project project = Project.builder().id(UUID.randomUUID()).projectId("test_project_id").name("Test Project").build();
            List<ProjectConfigAttribute> attrs = Arrays.asList(
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

            ProjectConfig projectConfig = ProjectConfig.builder().id(UUID.randomUUID()).name("default").project(project).status(ProjectConfig.Status.ACTIVE).attributes(attrs).build();
            ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PENDING).project(project).build();


            importService.syncImportScanResult(tempIssueFile, scanTask, UUID.fromString("11111111-1111-1111-1110-111111111111"), "admin");

        });
    }


    @Test
    void syncImportScanResult_projectConfigAttributeNotExist_AppException() {
        Assertions.assertThrows(AppException.class, () -> {
            String originalFile = "src/test/lua_simple_invalid_magic.csf";
            String scanTempFile = "src/test/temp_lua_simple_invalid_magic.csf";

            Path copied = Paths.get(scanTempFile);
            Path originalPath = Paths.get(originalFile);
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);

            File tempIssueFile = new File(scanTempFile);

            Project project = Project.builder().id(UUID.randomUUID()).projectId("test_project_id").name("Test Project").build();

            ProjectConfig projectConfig = ProjectConfig.builder().id(UUID.randomUUID()).name("default").project(project).status(ProjectConfig.Status.ACTIVE).build();
            ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PENDING).project(project).projectConfig(projectConfig).build();


            importService.syncImportScanResult(tempIssueFile, scanTask, UUID.fromString("11111111-1111-1111-1110-111111111111"), "admin");

        });
    }

    @Test
    void syncImportScanResult_normal_success() throws IOException, AppException {
            String originalFile = "src/test/lua_simple_invalid_magic.csf";
            String scanTempFile = "src/test/temp_lua_simple_invalid_magic.csf";

            Path copied = Paths.get(scanTempFile);
            Path originalPath = Paths.get(originalFile);
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);

            File tempIssueFile = new File(scanTempFile);

            List<ProjectConfigAttribute> attrs = Arrays.asList(
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


            Project project = Project.builder().id(UUID.randomUUID()).projectId("test_project_id").name("Test Project").build();

            ProjectConfig projectConfig = ProjectConfig.builder().id(UUID.randomUUID()).name("default").project(project).status(ProjectConfig.Status.ACTIVE).attributes(attrs).build();
            ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).status(ScanTask.Status.PENDING).project(project).projectConfig(projectConfig).build();


            JSONObject postProcDoneKafkaMessage = new JSONObject();
            postProcDoneKafkaMessage.put("scanTaskId", scanTask.getId().toString());
            postProcDoneKafkaMessage.put("status", "FAILED");
            postProcDoneKafkaMessage.put("dateTime", (new Date().getTime()));
            postProcDoneKafkaMessage.put("source", "POSTPROC");
            when(issueService.generateKafkaJsonMessage(any(ScanTask.class), anyString())).thenReturn(postProcDoneKafkaMessage);
            importService.syncImportScanResult(tempIssueFile, scanTask, UUID.fromString("11111111-1111-1111-1110-111111111111"), "admin");

    }
}
