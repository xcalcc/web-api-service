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
import com.xcal.api.dao.UserDao;
import com.xcal.api.entity.*;
import com.xcal.api.entity.v3.AssigneeCountRow;
import com.xcal.api.entity.v3.IssueGroup;
import com.xcal.api.entity.v3.IssueGroupCountRow;
import com.xcal.api.entity.v3.ReportAssigneeStatisticRow;
import com.xcal.api.exception.AppException;
import com.xcal.api.mapper.IssueGroupMapper;
import com.xcal.api.mapper.ProjectSummaryMapper;
import com.xcal.api.model.dto.IssueDto;
import com.xcal.api.model.dto.v3.RuleListResponseDto;
import com.xcal.api.model.payload.SearchIssueRequest;
import com.xcal.api.model.payload.SummaryResponse;
import com.xcal.api.model.payload.v3.ReportPDFResponse;
import com.xcal.api.model.payload.v3.ReportRequest;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.repository.ScanFileRepository;
import com.xcal.api.repository.ScanTaskRepository;
import com.xcal.api.service.v3.ProjectServiceV3;
import com.xcal.api.service.v3.RuleServiceV3;
import com.xcal.api.util.VariableUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.sql.DataSource;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ReportServiceTest {

    @NonNull
    private DataSource dataSource;
    private ReportService reportService;
    private MeasureService measureService = mock(MeasureService.class);
    private I18nService i18nService = mock(I18nService.class);
    private RuleService ruleService = mock(RuleService.class);
    private IssueService issueService = mock(IssueService.class);
    private ProjectService projectService = mock(ProjectService.class);
    private ProjectServiceV3 projectServiceV3 = mock(ProjectServiceV3.class);
    private ScanTaskService scanTaskService = mock(ScanTaskService.class);
    private ScanFileService scanFileService = mock(ScanFileService.class);
    private RuleServiceV3 ruleServiceV3 = mock(RuleServiceV3.class);
    private ScanTaskRepository scanTaskRepository = mock(ScanTaskRepository.class);
    private IssueGroupDao issueGroupDao;
    private UserDao userDao = mock(UserDao.class);
    private ProjectSummaryMapper projectSummaryMapper = mock(ProjectSummaryMapper.class);


    private ScanFileRepository scanFileRepository = mock(ScanFileRepository.class);
    @NonNull IssueGroupMapper issueGroupMapper=mock(IssueGroupMapper.class);

    private Project project = Project.builder().id(UUID.randomUUID()).projectId("projectId").name("projectName").scanMode(VariableUtil.ScanMode.SINGLE.name()).status(Project.Status.ACTIVE).createdBy("testUser").createdOn(new Date()).build();
    private ProjectConfigAttribute projectConfigAttribute=ProjectConfigAttribute.builder()
            .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_MODE.type)
            .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_MODE.nameValue)
            .value("-single").build();
    private List<ProjectConfigAttribute> projectConfigAttributeList = Arrays.asList(projectConfigAttribute);
    private ProjectConfig projectConfig = ProjectConfig.builder().project(project).attributes(projectConfigAttributeList).build();
    private ScanTask scanTask = ScanTask.builder().id(UUID.randomUUID()).project(project).status(ScanTask.Status.PENDING).projectConfig(projectConfig).build();
    private ScanEngine scanEngine = ScanEngine.builder().id(UUID.randomUUID()).name("Scan Engine Name Test").version("1.0").build();
    private RuleSet ruleSet = RuleSet.builder().id(UUID.randomUUID()).name("ruleSetTest").version("1.0").displayName("Rule Set Test").scanEngine(scanEngine).build();
    private User currentUser = User.builder().username("testUser").displayName("testDisplayName").email("test@xxxx.com").userGroups(new ArrayList<>()).build();

    private RuleInformation rule1 = RuleInformation.builder().ruleSet(ruleSet).vulnerable("AOB").severity(RuleInformation.Severity.HIGH).likelihood(RuleInformation.Likelihood.LIKELY).build();
    private RuleInformation rule2 = RuleInformation.builder().ruleSet(ruleSet).vulnerable("AOB").severity(RuleInformation.Severity.HIGH).likelihood(RuleInformation.Likelihood.PROBABLE).build();
    private RuleInformation rule3 = RuleInformation.builder().ruleSet(ruleSet).vulnerable("AOB").severity(RuleInformation.Severity.HIGH).likelihood(RuleInformation.Likelihood.UNLIKELY).build();
    private RuleInformation rule4 = RuleInformation.builder().ruleSet(ruleSet).vulnerable("UIV").severity(RuleInformation.Severity.MEDIUM).likelihood(RuleInformation.Likelihood.LIKELY).build();
    private RuleInformation rule5 = RuleInformation.builder().ruleSet(ruleSet).vulnerable("UIV").severity(RuleInformation.Severity.MEDIUM).likelihood(RuleInformation.Likelihood.PROBABLE).build();
    private RuleInformation rule6 = RuleInformation.builder().ruleSet(ruleSet).vulnerable("UIV").severity(RuleInformation.Severity.MEDIUM).likelihood(RuleInformation.Likelihood.UNLIKELY).build();
    private RuleInformation rule7 = RuleInformation.builder().ruleSet(ruleSet).vulnerable("UIV").severity(RuleInformation.Severity.LOW).likelihood(RuleInformation.Likelihood.LIKELY).build();
    private RuleInformation rule8 = RuleInformation.builder().ruleSet(ruleSet).vulnerable("DBF").severity(RuleInformation.Severity.LOW).likelihood(RuleInformation.Likelihood.PROBABLE).build();
    private RuleInformation rule9 = RuleInformation.builder().ruleSet(ruleSet).vulnerable("DBF").severity(RuleInformation.Severity.LOW).likelihood(RuleInformation.Likelihood.UNLIKELY).build();
    private List<I18nMessage> i18nMessageList = Arrays.asList(I18nMessage.builder().key("report.detail.csv.header.issue.ID").locale("en").content("ID").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.ID").locale("en").content("ID").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.File").locale("en").content("File").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.Line").locale("en").content("Line").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.Function").locale("en").content("Function").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.Variable").locale("en").content("Variable").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.Type").locale("en").content("Type").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.Certainty").locale("en").content("Certainty").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.RiskRank").locale("en").content("Risk/Rank").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.Severity").locale("en").content("Severity").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.Likelihood").locale("en").content("Likelihood").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.RemediationCost").locale("en").content("RemediationCost").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.ScanEngineName").locale("en").content("ScanEngineName").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.Category").locale("en").content("Category").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.Action").locale("en").content("Action").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.AssignedTo").locale("en").content("AssignedTo").build(),
            I18nMessage.builder().key("report.detail.csv.header.issue.Description").locale("en").content("Description").build()
    );
    private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private int fontCountBefore;

    @BeforeEach
    void setUp() {
        fontCountBefore = ge.getAvailableFontFamilyNames().length;
        when(measureService.retrieveScanSummary(any(ScanTask.class),any(SearchIssueGroupRequest.class))).thenReturn(SummaryResponse.builder()
                .issueSummary(SummaryResponse.IssueSummary.builder().build()).build());
        when(i18nService.getI18nMessageByKeyPrefix(any(), any())).thenReturn(new ArrayList<>());
        when(ruleService.findByRuleSet(argThat(arg -> ruleSet.getId() == arg.getId()))).thenReturn(Arrays.asList(rule1, rule2, rule3, rule4, rule5, rule6, rule7, rule8, rule9));
        issueGroupDao = mock(IssueGroupDao.class);
        reportService = new ReportService(dataSource, measureService, issueService, i18nService, ruleService, projectService, projectServiceV3, scanTaskService, scanFileService, ruleServiceV3, issueGroupDao, userDao, projectSummaryMapper, scanTaskRepository, scanFileRepository,issueGroupMapper);
    }

    @Test
    void compileIssueSummaryReport() throws AppException {
        JasperReport jasperReport = reportService.compileIssueSummaryReport();
        assertNotNull(jasperReport);
    }

    @Test
    void getInputStream_noException() {
        ReportService spy = org.mockito.Mockito.spy(reportService);
        byte[] byteArray = new byte[1];
        doReturn(new ByteArrayInputStream(byteArray)).when(spy).getInputStream(any());
        try (InputStream inputStream = spy.getInputStream("test")) {
            assertEquals(byteArray.length, inputStream.available());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void generateIssueSummaryReport_success() throws AppException, IOException {
        ReportService spy = org.mockito.Mockito.spy(reportService);
        doReturn(null).when(spy).getInputStream(any());

        ProjectConfig projectConfig = ProjectConfig.builder()
                .id(UUID.randomUUID())
                .name("TEST")
                .project(project)
                .attributes(Collections.singletonList(
                        ProjectConfigAttribute.builder()
                                .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                                .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue)
                                .value("c++").build()))
                .status(ProjectConfig.Status.ACTIVE)
                .build();
        when(projectService.getLatestActiveProjectConfigByProject(argThat(arg -> arg.getId() == project.getId()))).thenReturn(Optional.of(projectConfig));

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        spy.registerFont();
        int fontCountAfterSpy = ge.getAvailableFontFamilyNames().length;
        log.info("[generateIssueSummaryReport_success] fontCountBefore: {}", fontCountBefore);
        log.info("[generateIssueSummaryReport_success] fontCountAfter: {}", fontCountAfterSpy);
        assertEquals(fontCountBefore, fontCountAfterSpy);

        Resource englishReport = this.reportService.generateIssueSummaryReport(scanTask, ruleSet, Locale.ENGLISH, currentUser);
        assertTrue(englishReport.contentLength() > 0);

        Resource chineseReport = this.reportService.generateIssueSummaryReport(scanTask, ruleSet, Locale.SIMPLIFIED_CHINESE, currentUser);
        assertTrue(chineseReport.contentLength() > 0);

        int fontCountAfter = ge.getAvailableFontFamilyNames().length;
        log.info("[generateIssueSummaryReport_success] fontCountAfter: {}", fontCountAfter);
        assertTrue(fontCountBefore < fontCountAfter);
    }

    @Test
    void generateIssueCsvReport_searchList_Success() throws AppException, IOException {
        log.info("[generateIssueCsvReport_Success]");
        IssueDto.RuleInformation ruleInformation = IssueDto.RuleInformation.builder().remediationCost(RuleInformation.RemediationCost.HIGH.toString()).scanEngineName("xcalibyte").likelihood(RuleInformation.Likelihood.UNLIKELY.toString()).build();
        String currentUserName = "user";
        Issue issue1 = Issue.builder().id(UUID.randomUUID()).issueCode("BUILTIN-NPD-D").scanTask(scanTask)
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
                .build();
        IssueDto issueDto1 = IssueDto.builder()
                .seq(issue1.getSeq())
                .relativePath(issue1.getFilePath())
                .lineNo(issue1.getLineNo())
                .functionName(issue1.getFunctionName())
                .variableName(issue1.getVariableName())
                .vulnerable(issue1.getVariableName())
                .certainty("D")
                .issueAttributes(Collections.singletonList(IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.PRIORITY).value("HIGH").build()))
                .severity(issue1.getSeverity().toString())
                .likelihood(ruleInformation.getLikelihood())
                .remediationCost(ruleInformation.getRemediationCost())
                .ruleInformation(ruleInformation)
                .issueCategory("security")
                .action("confirmed")
                .assignTo(IssueDto.AssignTo.builder().displayName("Tom").build())
                .message("This is very dangerous")
                .build();

        ReportRequest reportRequest = ReportRequest.builder()
                .projectId(UUID.randomUUID())
                .scanTaskId(UUID.randomUUID())
                .scanFileIds(Arrays.asList(UUID.randomUUID(),UUID.randomUUID()))
                .build();
        List<Issue> issueList = Collections.singletonList(issue1);
        Page<Issue> pagedIssues = new PageImpl<>(issueList);
        when(i18nService.getI18nMessageByKeyPrefix("report.detail.csv.header", Locale.ENGLISH)).thenReturn(i18nMessageList);
        when(issueService.searchIssue(any(ScanTask.class), any(UUID.class), any(), any(), notNull(), notNull(), notNull(), notNull(), any(SearchIssueRequest.SearchIssueType.class), any(Pageable.class))).thenReturn(pagedIssues);
        when(issueService.convertIssuesToDto(issueList,Locale.ENGLISH)).thenReturn(Collections.singletonList(issueDto1));
        when(measureService.getIssueCountGroupByCriticality(any(), anyList())).thenReturn(ReportPDFResponse.IssueCountGroupByCriticality.builder().high(0).medium(0).low(0).build());
        RuleListResponseDto ruleListResponseDto = RuleListResponseDto.builder().build();
        doReturn(ruleListResponseDto).when(ruleServiceV3).getAllRuleInfo(any());


        List<ScanFile> scanFiles = Arrays.asList(ScanFile.builder().build());
        doReturn(scanFiles).when(scanFileService).findByScanFileIds(reportRequest.getScanFileIds());

        List<IssueGroup> issueGroups = Arrays.asList(IssueGroup.builder()
                .srcLineNo(10)
                .sinkLineNo(10).build());
        when(issueGroupDao.getIssueGroupList(any(UUID.class),any(UUID.class),isNull(),isNull(),anyList(),isNull(),isNull(),isNull(),isNull(),isNull(),anyInt(),anyInt()))
                .thenReturn(issueGroups)//first call
                .thenReturn(new ArrayList<>());//second call
        doReturn(2L).when(issueGroupMapper).getIssueGroupCount(any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
        Resource resource = reportService.generateIssueCsvReport(scanTask, reportRequest, "SINGLE",false,480, true, Locale.ENGLISH);

        List<String> csvLines = FileUtils.readLines(resource.getFile(), Charset.defaultCharset());
        for(String l:csvLines) {
            System.out.println(l);
        }
        assertTrue(resource.exists());
        assertTrue(resource.isFile());
        assertTrue(resource.isReadable());
        assertTrue(resource.contentLength()>1);
        assertEquals(13,csvLines.size());
    }

//    @Test
//    void getPDFReport_normal_success() throws AppException {
//
//        ProjectSummaryDto.Summary summary = ProjectSummaryDto.Summary.builder()..build();
//        ProjectSummaryDto projectSummaryDto = ProjectSummaryDto.builder().summary().build();
//
//        doReturn(new ArrayList()).when(issueGroupDao).getIssueGroupCriticalityCount(any());
//        doReturn(projectSummaryDto).when(projectServiceV3).getProjectSummary(any());
//        doReturn(new ArrayList()).when(userDao).getAssigneeCriticalityRuleCodeCount(any(), anyBoolean());
//        reportService.getPDFReport(UUID.randomUUID());
//
//    }

    @Test
    void getCertaintyCriticalityMap_normal_success(){
        List<IssueGroupCountRow> highDefiniteList = Arrays.asList(
                IssueGroupCountRow.builder().ruleCode("AOB").count("10").build(),
                IssueGroupCountRow.builder().ruleCode("UIV").count("10").build()

        );
        doReturn(highDefiniteList).when(issueGroupDao).getIssueGroupCountWithFilter(any(),any(),isNull(),isNull(),isNull(),isNull(),isNull(),any(),isNull(),any(),any(),isNull());
        Map<String, ReportPDFResponse.CountValue> certaintyCriticalityMap = reportService.getCertaintyCriticalityMap(SearchIssueGroupRequest.builder().build(),"H","H",true,20);
        assertEquals(10 ,certaintyCriticalityMap.get("AOB").getCount());
        assertEquals(50f ,certaintyCriticalityMap.get("AOB").getPercentage());
        assertEquals(10 ,certaintyCriticalityMap.get("UIV").getCount());
        assertEquals(50f ,certaintyCriticalityMap.get("UIV").getPercentage());
    }

    @Test
    void getReportAssigneeStatisticRowMap_normal_success(){
        UUID user1Id=UUID.randomUUID();
        String user1Name="User 1";
        UUID user2Id=UUID.randomUUID();
        String user2Name="User 2";

        List<AssigneeCountRow> assignedCountRows = Arrays.asList(
                AssigneeCountRow.builder()
                        .ruleCode("UIV")
                        .criticality("H")
                        .count(2)
                        .id(user1Id)
                        .username(user1Name)
                        .build(),
                AssigneeCountRow.builder()
                        .ruleCode("UIV")
                        .criticality("M")
                        .count(3)
                        .username(user1Name)
                        .id(user1Id)
                        .build(),
                AssigneeCountRow.builder()
                        .ruleCode("UIV")
                        .criticality("L")
                        .count(5)
                        .username(user1Name)
                        .id(user1Id)
                        .build(),
                AssigneeCountRow.builder()
                        .ruleCode("AOB")
                        .criticality("M")
                        .count(18)
                        .username(user2Name)
                        .id(user2Id)
                        .build()
        );
        Map<String, ReportAssigneeStatisticRow> assigneeStatisticRowMap = reportService.getReportAssigneeStatisticRowMap(assignedCountRows);
        //username
        assertEquals(user1Name,assigneeStatisticRowMap
                .get(user1Id.toString())
                .getUser()
                .getName());
        assertEquals(user2Name,assigneeStatisticRowMap
                .get(user2Id.toString())
                .getUser()
                .getName());
        //total count
        assertEquals(10, assigneeStatisticRowMap.get(user1Id.toString()).getCounts());
        assertEquals(18, assigneeStatisticRowMap.get(user2Id.toString()).getCounts());
    }


    @Test
    void convertToReportAssigneeStatisticRow_normal_success(){
        List<AssigneeCountRow> unassignedCountRows = Arrays.asList(
                AssigneeCountRow.builder()
                        .ruleCode("UIV")
                        .count(10)
                        .build(),
                AssigneeCountRow.builder()
                        .ruleCode("AOB")
                        .count(18)
                        .build()
        );
        ReportAssigneeStatisticRow unassigned = reportService.convertToReportAssigneeStatisticRow(unassignedCountRows);
        Map<String, Integer> breakdownByCsvCode = unassigned.getBreakdownByCsvCode();
        assertEquals(10, breakdownByCsvCode.get("UIV"));
        assertEquals(18, breakdownByCsvCode.get("AOB"));
        assertEquals(28, unassigned.getCounts());
    }

    @Test
    void validateReportType_getSingleReportWithSingleScanMode_ok(){
        assertDoesNotThrow(()->{reportService.validateReportType(VariableUtil.ReportType.SINGLE.name(), VariableUtil.ScanMode.SINGLE.name());});
    }

    @Test
    void validateReportType_getSingleReportWithSingleXscaScanMode_ok(){
        assertDoesNotThrow(()->{reportService.validateReportType(VariableUtil.ReportType.SINGLE.name(), VariableUtil.ScanMode.SINGLE_XSCA.name());});
    }

    @Test
    void validateReportType_getSingleReportWithCrossScanMode_appException(){
        assertThrows(AppException.class,()->{
            reportService.validateReportType(VariableUtil.ReportType.SINGLE.name(), VariableUtil.ScanMode.CROSS.name());
        });
    }

    @Test
    void validateReportType_getSingleReportWithXscaScanMode_appException(){
        assertThrows(AppException.class,()->{
            reportService.validateReportType(VariableUtil.ReportType.SINGLE.name(), VariableUtil.ScanMode.XSCA.name());
        });
    }


    @Test
    void validateReportType_getCrossReportWithCrossScanMode_ok(){
        assertDoesNotThrow(()->{reportService.validateReportType(VariableUtil.ReportType.CROSS.name(), VariableUtil.ScanMode.CROSS.name());});
    }


    @Test
    void validateReportType_getCrossReportWithSingleScanMode_appException(){
        assertThrows(AppException.class,()->{
            reportService.validateReportType(VariableUtil.ReportType.CROSS.name(), VariableUtil.ScanMode.SINGLE.name());
        });
    }

    @Test
    void validateReportType_getCrossReportWithSingleXscaScanMode_appException(){
        assertThrows(AppException.class,()->{
            reportService.validateReportType(VariableUtil.ReportType.CROSS.name(), VariableUtil.ScanMode.SINGLE_XSCA.name());
        });
    }

    @Test
    void validateReportType_getCrossReportWithXscaScanMode_appException(){
        assertThrows(AppException.class,()->{
            reportService.validateReportType(VariableUtil.ReportType.CROSS.name(), VariableUtil.ScanMode.XSCA.name());
        });
    }



    @Test
    void validateReportType_getMisraReportWithXscaScanMode_ok(){
        assertDoesNotThrow(()->{reportService.validateReportType(VariableUtil.ReportType.MISRA.name(), VariableUtil.ScanMode.XSCA.name());});
    }

    @Test
    void validateReportType_getMisraReportWithSingleXscaScanMode_ok(){
        assertDoesNotThrow(()->{reportService.validateReportType(VariableUtil.ReportType.MISRA.name(), VariableUtil.ScanMode.SINGLE_XSCA.name());});
    }

    @Test
    void validateReportType_getMisraReportWithSingleScanMode_appException(){
        assertThrows(AppException.class,()->{
            reportService.validateReportType(VariableUtil.ReportType.MISRA.name(), VariableUtil.ScanMode.SINGLE.name());
        });
    }

    @Test
    void validateReportType_getMisraReportWithCrossScanMode_appException(){
        assertThrows(AppException.class,()->{
            reportService.validateReportType(VariableUtil.ReportType.MISRA.name(), VariableUtil.ScanMode.CROSS.name());
        });
    }

    @Test
    void getPercentage_0Divisor_0(){
        float result=reportService.getPercentage(0,0);
        assertEquals(0f,result);
    }

    @Test
    void getPercentage_1Div100_10000percent(){ // 100/1
        float result=reportService.getPercentage(1,100);
        assertEquals(10000f,result);
    }

    @Test
    void getPercentage_1Div1_100percent(){ // 1/1
        float result=reportService.getPercentage(1,1);
        assertEquals(100f,result);
    }

    @Test
    void getPercentage_100Div1_1percent(){ // 1/100
        float result=reportService.getPercentage(100,1);
        assertEquals(1f,result);
    }
}
