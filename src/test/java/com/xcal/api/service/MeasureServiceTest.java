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

import com.xcal.api.dao.IssueGroupDao;
import com.xcal.api.dao.ScanTaskDao;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.mapper.ProjectSummaryMapper;
import com.xcal.api.model.payload.SummaryResponse;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.repository.IssueRepository;
import com.xcal.api.repository.ScanFileRepository;
import com.xcal.api.service.v3.ProjectServiceV3;
import com.xcal.api.util.VariableUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@Slf4j
class MeasureServiceTest {
    private MeasureService measureService;

    private final ScanEngine scanEngine = ScanEngine.builder().name(ScanEngine.EngineType.XCALIBYTE.toString()).build();
    private final RuleSet ruleSet = RuleSet.builder().id(UUID.randomUUID()).scanEngine(scanEngine).name("BUILTIN").build();
    private final RuleSet ruleSet2 = RuleSet.builder().id(UUID.randomUUID()).scanEngine(scanEngine).name("BUILTIN_2").build();
    private final RuleInformation rule1 = RuleInformation.builder().ruleSet(ruleSet).ruleCode("AOB-D").severity(RuleInformation.Severity.HIGH).likelihood(RuleInformation.Likelihood.LIKELY).priority(RuleInformation.Priority.HIGH).build();
    private final RuleInformation rule2 = RuleInformation.builder().ruleSet(ruleSet).ruleCode("UIV").severity(RuleInformation.Severity.HIGH).likelihood(RuleInformation.Likelihood.PROBABLE).priority(RuleInformation.Priority.HIGH).build();
    private final RuleInformation rule3 = RuleInformation.builder().ruleSet(ruleSet).ruleCode("NPD").severity(RuleInformation.Severity.HIGH).likelihood(RuleInformation.Likelihood.UNLIKELY).priority(RuleInformation.Priority.HIGH).build();
    private final RuleInformation rule4 = RuleInformation.builder().ruleSet(ruleSet).severity(RuleInformation.Severity.MEDIUM).likelihood(RuleInformation.Likelihood.LIKELY).build();
    private final RuleInformation rule5 = RuleInformation.builder().ruleSet(ruleSet).severity(RuleInformation.Severity.MEDIUM).likelihood(RuleInformation.Likelihood.PROBABLE).build();
    private final RuleInformation rule6 = RuleInformation.builder().ruleSet(ruleSet).severity(RuleInformation.Severity.MEDIUM).likelihood(RuleInformation.Likelihood.UNLIKELY).build();
    private final RuleInformation rule7 = RuleInformation.builder().ruleSet(ruleSet).severity(RuleInformation.Severity.LOW).likelihood(RuleInformation.Likelihood.LIKELY).build();
    private final RuleInformation rule8 = RuleInformation.builder().ruleSet(ruleSet).severity(RuleInformation.Severity.LOW).likelihood(RuleInformation.Likelihood.PROBABLE).build();
    private final RuleInformation rule9 = RuleInformation.builder().ruleSet(ruleSet).severity(RuleInformation.Severity.LOW).likelihood(RuleInformation.Likelihood.UNLIKELY).build();
    private final UUID projectId = UUID.randomUUID();
    private final Project project = Project.builder().id(projectId).needDsr(true).build();
    private final UUID scanTaskId = UUID.randomUUID();

    ScanTaskDao scanTaskDao=mock(ScanTaskDao.class);

    IssueGroupDao issueGroupDao=mock(IssueGroupDao.class);
    ProjectSummaryMapper projectSummaryMapper=mock(ProjectSummaryMapper.class);
    ScanFileRepository scanFileRepository=mock(ScanFileRepository.class);

    private final Map<String, String> scanSummary = new LinkedHashMap<String, String>() {
        {
            put("ruleCode.BUILTIN-NPD-D", "1");
            put("rule.version.BUILTIN", "1");
            put("rule.id.BUILTIN", "972c7422-01a5-4aa5-9b07-7df2c254f6f7");
            put("BUILTIN.ruleCode.BUILTIN-NPD-D", "1");
        }
    };
    private final ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(project).projectConfig(ProjectConfig.builder().build()).summary(scanSummary).status(ScanTask.Status.COMPLETED).build();
    private final FileInfo fileInfo = FileInfo.builder().noOfLines(2).build();
    private final ScanFile scanFile = ScanFile.builder().storePath("/share/scan/5f16a81b-2701-480d-b146-99c0e6b36472/java.c").fileInfo(fileInfo).build();
    private final String currentUserName = "user";
    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final User currentUser = User.builder().id(userId).username(currentUserName).displayName("testDispalyName").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();
    private final RuleInformation ruleInformation = RuleInformation.builder()
            .id(UUID.randomUUID())
            .ruleSet(ruleSet)
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
            .build();
    private final Issue issue = Issue.builder().id(UUID.randomUUID()).issueCode("BUILTIN-NPD-D").scanTask(scanTask)
            .seq("00011")
            .severity(Issue.Severity.HIGH)
            .lineNo(381)
            .columnNo(0)
            .functionName("ngx_vslprintf")
            .variableName("buf")
            .message("The value of the pointer (reference) is 0 (or near zero) and is used to access memory expected to be valid")
            .status(Issue.Status.ACTIVE)
            .action(Issue.Action.CONFIRMED)
            .createdBy(currentUserName)
            .createdOn(new Date())
            .modifiedOn(new Date())
            .modifiedBy(currentUserName)
            .assignTo(currentUser)
            .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c@ngx_vslprintf@buf@NPD@@@fffffa68")
            .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c")
            .ruleInformation(ruleInformation)
            .issueTraces(new ArrayList<>())
            .build();

    @BeforeEach
    void setUp() {
        IssueRepository issueRepository = mock(IssueRepository.class);
        UserService userService = mock(UserService.class);
        RuleService ruleService = mock(RuleService.class);
        RuleStandardService ruleStandardService = mock(RuleStandardService.class);
        measureService = new MeasureService(issueRepository, ruleService, ruleStandardService, userService,scanTaskDao,issueGroupDao,projectSummaryMapper,scanFileRepository );
    }

    @Test
    void retrieveRiskLevelWithZeroIssuesShouldReturnLowLevel() throws AppException {
        List<Issue> issues = new ArrayList<>();
        int totalOfLines = 10;
        String result = MeasureService.retrieveRiskLevel(issues, totalOfLines);
        assertEquals("LOW", result);
    }

    @Test
    void retrieveRiskLevelWithCriticalIssuesShouldReturnHighLevel() throws AppException {
        Issue issue = Issue.builder().id(UUID.randomUUID()).action(Issue.Action.CRITICAL).build();
        List<Issue> issues = new ArrayList<>();
        issues.add(issue);
        int totalOfLines = 10;
        String result = MeasureService.retrieveRiskLevel(issues, totalOfLines);
        assertEquals("HIGH", result);
    }

    @Test
    void retrieveRiskLevelForXcalibyte() throws AppException {
        List<Issue> issues = new ArrayList<>();

        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule1).severity(Issue.Severity.HIGH).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule2).severity(Issue.Severity.HIGH).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule2).severity(Issue.Severity.HIGH).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule3).severity(Issue.Severity.HIGH).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule4).severity(Issue.Severity.MEDIUM).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule5).severity(Issue.Severity.MEDIUM).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule6).severity(Issue.Severity.MEDIUM).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule6).severity(Issue.Severity.MEDIUM).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule6).severity(Issue.Severity.MEDIUM).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule7).severity(Issue.Severity.LOW).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule8).severity(Issue.Severity.LOW).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule8).severity(Issue.Severity.LOW).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule9).severity(Issue.Severity.LOW).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule9).severity(Issue.Severity.LOW).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule9).severity(Issue.Severity.LOW).build());

        String result1 = MeasureService.retrieveRiskLevel(issues, 1);
        assertEquals("LOW", result1);

        String result2 = MeasureService.retrieveRiskLevel(issues, 19);
        assertEquals("LOW", result2);

        String result3 = MeasureService.retrieveRiskLevel(issues, 20);
        assertEquals("MEDIUM", result3);

        String result4 = MeasureService.retrieveRiskLevel(issues, 38);
        assertEquals("MEDIUM", result4);

        String result5 = MeasureService.retrieveRiskLevel(issues, 39);
        assertEquals("HIGH", result5);

        String result6 = MeasureService.retrieveRiskLevel(issues, 1000);
        assertEquals("HIGH", result6);
    }

    @Test
    void retrieveRiskLevelForSpotbugs() throws AppException {
        rule1.getRuleSet().getScanEngine().setName(ScanEngine.EngineType.SPOTBUGS.toString());
        Issue issue = Issue.builder().id(UUID.randomUUID()).ruleInformation(rule1).build();
        List<Issue> issues = new ArrayList<>();
        issues.add(issue);
        // for non xcalibyte risk level, each issue, average 0 line to 83 is high, 84 to 3124 is medium, over 3124 line is low
        String result1 = MeasureService.retrieveRiskLevel(issues, 1);
        assertEquals("HIGH", result1);

        String result2 = MeasureService.retrieveRiskLevel(issues, 83);
        assertEquals("HIGH", result2);

        String result3 = MeasureService.retrieveRiskLevel(issues, 84);
        assertEquals("MEDIUM", result3);

        String result4 = MeasureService.retrieveRiskLevel(issues, 3124);
        assertEquals("MEDIUM", result4);

        String result5 = MeasureService.retrieveRiskLevel(issues, 3125);
        assertEquals("LOW", result5);

        String result6 = MeasureService.retrieveRiskLevel(issues, 9999);
        assertEquals("LOW", result6);
    }

    @Test
    void retrieveRiskLevelForSpotbugsHaveCritical() throws AppException {
        ScanEngine scanEngine1 = ScanEngine.builder().name(ScanEngine.EngineType.SPOTBUGS.toString()).build();
        RuleSet ruleSet = RuleSet.builder().scanEngine(scanEngine1).build();
        RuleInformation ruleInformation = RuleInformation.builder().ruleSet(ruleSet).build();
        Issue issue = Issue.builder().id(UUID.randomUUID()).ruleInformation(ruleInformation).action(Issue.Action.CRITICAL).build();
        List<Issue> issues = new ArrayList<>();
        issues.add(issue);
        // for spotbugs risk level, each issue, average 0 line to 83 is high, 84 to 3124 is medium, over 3124 line is low
        // control test case with retrieveRiskLevelForSpotbugs
        String result1 = MeasureService.retrieveRiskLevel(issues, 1);
        assertEquals("HIGH", result1);

        String result2 = MeasureService.retrieveRiskLevel(issues, 83);
        assertEquals("HIGH", result2);

        String result3 = MeasureService.retrieveRiskLevel(issues, 84);
        assertEquals("HIGH", result3);

        String result4 = MeasureService.retrieveRiskLevel(issues, 3124);
        assertEquals("HIGH", result4);

        String result5 = MeasureService.retrieveRiskLevel(issues, 3125);
        assertEquals("HIGH", result5);

        String result6 = MeasureService.retrieveRiskLevel(issues, 9999);
        assertEquals("HIGH", result6);
    }

    @Test
    void retrieveRiskLevelForOclint() throws AppException {
        rule1.getRuleSet().getScanEngine().setName(ScanEngine.EngineType.OCLINT.toString());
        Issue issue = Issue.builder().id(UUID.randomUUID()).ruleInformation(rule1).build();
        List<Issue> issues = new ArrayList<>();
        issues.add(issue);
        // for non xcalibyte risk level, each issue, average 0 line to 83 is high, 84 to 3124 is medium, over 3124 line is low
        String result1 = MeasureService.retrieveRiskLevel(issues, 1);
        assertEquals("HIGH", result1);

        String result2 = MeasureService.retrieveRiskLevel(issues, 83);
        assertEquals("HIGH", result2);

        String result3 = MeasureService.retrieveRiskLevel(issues, 84);
        assertEquals("MEDIUM", result3);

        String result4 = MeasureService.retrieveRiskLevel(issues, 3124);
        assertEquals("MEDIUM", result4);

        String result5 = MeasureService.retrieveRiskLevel(issues, 3125);
        assertEquals("LOW", result5);

        String result6 = MeasureService.retrieveRiskLevel(issues, 9999);
        assertEquals("LOW", result6);
    }

    @Test
    void retrieveRiskLevelWithInvalidEngineNameShouldThrowException() {
        ScanEngine scanEngine = ScanEngine.builder().name("incorrectName").build();
        RuleSet ruleSet = RuleSet.builder().scanEngine(scanEngine).build();
        RuleInformation ruleInformation = RuleInformation.builder().ruleSet(ruleSet).build();
        Issue issue = Issue.builder().id(UUID.randomUUID()).ruleInformation(ruleInformation).build();
        List<Issue> issues = new ArrayList<>();
        issues.add(issue);
        int totalOfLines = 10;
        assertThrows(AppException.class, () -> MeasureService.retrieveRiskLevel(issues, totalOfLines));
    }

    @Test
    void retrieveScanSummary_ValidScanTask_Success() {
        log.info("[retrieveScanSummary_ValidScanTask_Success]");
        SearchIssueGroupRequest searchIssueGroupRequest=SearchIssueGroupRequest.builder().scanTaskId(scanTask.getId()).build();

        Map<String,String> ruleCodeCountMap=new HashMap<>();
        ruleCodeCountMap.put("BUILTIN-NPD-D","1");

        MeasureService measureServiceSpy=spy(measureService);
        doReturn(ruleCodeCountMap).when(measureServiceSpy).getRuleCodeCountMap(any(), any());

        SummaryResponse summaryResponse = measureServiceSpy.retrieveScanSummary(scanTask,searchIssueGroupRequest);
        assertEquals(scanTaskId, summaryResponse.getScanTaskId());
        assertEquals(scanTask.getStatus().name(), summaryResponse.getStatus());
        System.out.print(summaryResponse.getIssueSummary().getRuleCodeCountMap());
        assertEquals("1", summaryResponse.getIssueSummary().getRuleCodeCountMap().get("BUILTIN-NPD-D"));
        assertEquals("972c7422-01a5-4aa5-9b07-7df2c254f6f7", summaryResponse.getRuleSetSummaryMap().get("BUILTIN").getId());
        assertEquals("BUILTIN", summaryResponse.getRuleSetSummaryMap().get("BUILTIN").getName());
        assertEquals("1", summaryResponse.getRuleSetSummaryMap().get("BUILTIN").getVersion());
    }

    @Test
    void retrieveScanSummary_ScanTaskAndLatestScanTaskHasDifferentScanTaskId_Success() {
        log.info("[retrieveScanSummary_ScanTaskAndLatestScanTaskHasDifferentScanTaskId_Success]");

        SummaryResponse.ScanTaskSummary latestScanTaskSummary= SummaryResponse.ScanTaskSummary.builder()
                .scanTaskId(scanTask.getId())
                .build();

        UUID latestCompletedScanTaskId = UUID.randomUUID();
        SummaryResponse.ScanTaskSummary latestCompletedScanTaskSummary= SummaryResponse.ScanTaskSummary.builder()
                .scanTaskId(latestCompletedScanTaskId)
                .build();

        Map<String,String> ruleCodeCountMap=new HashMap<>();
        ruleCodeCountMap.put("BUILTIN-NPD-D","1");

        SummaryResponse.IssueSummary issueSummary = SummaryResponse.IssueSummary.builder()
                .ruleCodeCountMap(ruleCodeCountMap)
                .build();


        Map<String, SummaryResponse.RuleSet> ruleSetSummaryMap = new HashMap<>();
        ruleSetSummaryMap.put("BUILTIN", SummaryResponse.RuleSet.builder()
                .id("972c7422-01a5-4aa5-9b07-7df2c254f6f7")
                .name("BUILTIN")
                .version("1")
                .build());

        SummaryResponse expectedSummaryResponse=SummaryResponse.builder()
                .scanTaskId(scanTask.getId())
                .latestScanTask(latestScanTaskSummary)
                .latestCompleteScanTask(latestCompletedScanTaskSummary)
                .status(scanTask.getStatus().name())
                .issueSummary(issueSummary)
                .ruleSetSummaryMap(ruleSetSummaryMap)
                .build();

        MeasureService measureServiceSpy=spy(measureService);
        doReturn(expectedSummaryResponse).when(measureServiceSpy).retrieveScanSummary(any(), any(), any());

        SearchIssueGroupRequest searchIssueGroupRequest=SearchIssueGroupRequest.builder().scanTaskId(scanTask.getId()).build();
        SummaryResponse summaryResponse = measureServiceSpy.retrieveScanSummary(scanTask,searchIssueGroupRequest);

        assertEquals(scanTaskId, summaryResponse.getScanTaskId());
        assertEquals(scanTaskId, summaryResponse.getLatestScanTask().getScanTaskId());
        assertEquals(latestCompletedScanTaskId, summaryResponse.getLatestCompleteScanTask().getScanTaskId());
        assertEquals(scanTask.getStatus().name(), summaryResponse.getStatus());
        assertEquals("1", summaryResponse.getIssueSummary().getRuleCodeCountMap().get("BUILTIN-NPD-D"));
        assertEquals("972c7422-01a5-4aa5-9b07-7df2c254f6f7", summaryResponse.getRuleSetSummaryMap().get("BUILTIN").getId());
        assertEquals("BUILTIN", summaryResponse.getRuleSetSummaryMap().get("BUILTIN").getName());
        assertEquals("1", summaryResponse.getRuleSetSummaryMap().get("BUILTIN").getVersion());
    }

    @Test
    void updateScanSummary_Success() throws AppException {
        List<Issue> issues = new ArrayList<>();

        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule1).severity(Issue.Severity.HIGH).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule2).severity(Issue.Severity.HIGH).build());
        issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule3).severity(Issue.Severity.HIGH).build());

        MeasureService.updateScanSummary(scanTask, issues, Arrays.asList(ruleSet, ruleSet2));
        assertEquals(3, Integer.valueOf(scanTask.getSummary().get("issues")));
        assertEquals(3, Integer.valueOf(scanTask.getSummary().get("BUILTIN.issues")));
    }

    @Test
    void calculatePriorityScore_Success() {
        // rule, priority, number
        // AOB  27    9
        // NPD  18    133
        // ERR08-J  12    70
        // MET06-J  12    7
        // MSC02-J  12    1
        // OBJ11-J  12    10
        List<Issue> issues = new ArrayList<>();
        RuleInformation rule_aob = RuleInformation.builder().id(UUID.randomUUID()).attributes(Collections.singletonList(
                RuleInformationAttribute.builder().type(VariableUtil.RuleAttributeTypeName.PRIORITY.type).name(VariableUtil.RuleAttributeTypeName.PRIORITY.name()).value("27").build())).build();
        RuleInformation rule_npd = RuleInformation.builder().id(UUID.randomUUID()).attributes(Collections.singletonList(
                RuleInformationAttribute.builder().type(VariableUtil.RuleAttributeTypeName.PRIORITY.type).name(VariableUtil.RuleAttributeTypeName.PRIORITY.name()).value("18").build())).build();
        RuleInformation rule_err08 = RuleInformation.builder().id(UUID.randomUUID()).attributes(Collections.singletonList(
                RuleInformationAttribute.builder().type(VariableUtil.RuleAttributeTypeName.PRIORITY.type).name(VariableUtil.RuleAttributeTypeName.PRIORITY.name()).value("12").build())).build();
        RuleInformation rule_met06 = RuleInformation.builder().id(UUID.randomUUID()).attributes(Collections.singletonList(
                RuleInformationAttribute.builder().type(VariableUtil.RuleAttributeTypeName.PRIORITY.type).name(VariableUtil.RuleAttributeTypeName.PRIORITY.name()).value("12").build())).build();
        RuleInformation rule_msc02 = RuleInformation.builder().id(UUID.randomUUID()).attributes(Collections.singletonList(
                RuleInformationAttribute.builder().type(VariableUtil.RuleAttributeTypeName.PRIORITY.type).name(VariableUtil.RuleAttributeTypeName.PRIORITY.name()).value("12").build())).build();
        RuleInformation rule_obj11 = RuleInformation.builder().id(UUID.randomUUID()).attributes(Collections.singletonList(
                RuleInformationAttribute.builder().type(VariableUtil.RuleAttributeTypeName.PRIORITY.type).name(VariableUtil.RuleAttributeTypeName.PRIORITY.name()).value("12").build())).build();

        for (int i = 0; i < 9; i++) {
            issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule_aob).build());
        }
        for (int i = 0; i < 133; i++) {
            issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule_npd).build());
        }
        for (int i = 0; i < 70; i++) {
            issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule_err08).build());
        }
        for (int i = 0; i < 7; i++) {
            issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule_met06).build());
        }
        for (int i = 0; i < 1; i++) {
            issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule_msc02).build());
        }
        for (int i = 0; i < 10; i++) {
            issues.add(Issue.builder().id(UUID.randomUUID()).ruleInformation(rule_obj11).build());
        }
        double result = MeasureService.calculatePriorityScore(issues);
        // 4.78812709
        assertTrue(result > 6.2118 && result < 6.212);
    }

    @Test
    void updateProjectSummary() {
        Project project = Project.builder().build();
        Date startTime = new Date();
        Map<String, String> summary = new HashMap<>();
        summary.put("files", "100");
        summary.put("lines", "10000");
        summary.put("issues", "10");
        summary.put("priority.HIGH", "5");
        summary.put("priority.MEDIUM", "3");
        summary.put("priority.LOW", "2");
        summary.put("rule.version.BUILTIN", "1");
        summary.put("BUILTIN.files", "100");
        summary.put("BUILTIN.lines", "10000");
        summary.put("BUILTIN.issues", "10");
        summary.put("BUILTIN.priority.HIGH", "5");
        summary.put("BUILTIN.priority.MEDIUM", "3");
        summary.put("BUILTIN.priority.LOW", "2");
        ScanTask scanTask = ScanTask.builder().scanStartAt(startTime).summary(summary).build();
        MeasureService.updateProjectSummary(project, scanTask);
        assertEquals(project.getSummary().get("scanStartAt"), String.format("%d", scanTask.getScanStartAt().getTime()));
        assertEquals(project.getSummary().get("ALL.files"), summary.get("files"));
        assertEquals(project.getSummary().get("ALL.lines"), summary.get("lines"));
        assertEquals(project.getSummary().get("ALL.issues"), summary.get("issues"));
        assertEquals(project.getSummary().get("ALL.priority.HIGH"), summary.get("priority.HIGH"));
        assertEquals(project.getSummary().get("ALL.priority.MEDIUM"), summary.get("priority.MEDIUM"));
        assertEquals(project.getSummary().get("ALL.priority.LOW"), summary.get("priority.LOW"));
        assertEquals(project.getSummary().get("BUILTIN.files"), summary.get("BUILTIN.files"));
        assertEquals(project.getSummary().get("BUILTIN.lines"), summary.get("BUILTIN.lines"));
        assertEquals(project.getSummary().get("BUILTIN.issues"), summary.get("BUILTIN.issues"));
        assertEquals(project.getSummary().get("BUILTIN.priority.HIGH"), summary.get("BUILTIN.priority.HIGH"));
        assertEquals(project.getSummary().get("BUILTIN.priority.MEDIUM"), summary.get("BUILTIN.priority.MEDIUM"));
        assertEquals(project.getSummary().get("BUILTIN.priority.LOW"), summary.get("BUILTIN.priority.LOW"));
    }

}
