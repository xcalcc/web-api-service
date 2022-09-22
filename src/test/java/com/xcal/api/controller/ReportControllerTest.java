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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xcal.api.config.WithMockCustomUser;
import com.xcal.api.entity.Project;
import com.xcal.api.entity.RuleSet;
import com.xcal.api.entity.ScanEngine;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.service.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ReportControllerTest {

    @NonNull
    private MockMvc mockMvc;
    @NonNull ModelMapper modelMapper;
    @MockBean
    private ScanTaskService scanTaskService;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private ReportService reportService;
    @MockBean
    private RuleService ruleService;
    @MockBean
    private CacheService cacheService;

    private final String adminUsername = "admin";

    private ScanEngine scanEngine = ScanEngine.builder().name("scanEngineName").version("test scanEngineVersion").language("C++").provider("Test provider")
            .providerUrl("Test provider url").url("test url").build();

    private RuleSet ruleSet1 = RuleSet.builder()
            .id(UUID.randomUUID())
            .scanEngine(scanEngine)
            .name("Xcalibyte")
            .version("1")
            .revision("1.0")
            .build();
    private RuleSet ruleSet2 = RuleSet.builder()
            .id(UUID.randomUUID())
            .scanEngine(scanEngine)
            .name("Test rule 1")
            .version("1.0")
            .revision("1")
            .build();

    @BeforeEach
    void setUp() {
        cacheService = mock(CacheService.class);
        doNothing().when(cacheService).initCacheRuleInformation();
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void issueSummaryReport_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[issueSummaryReport_ScanTaskNotFound_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/report_service/v2/issue_summary/scan_task/{id}", scanTaskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void issueSummaryReport_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[issueSummaryReport_InsufficientPrivilege_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("xxx").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        mockMvc.perform(get("/api/report_service/v2/issue_summary/scan_task/{id}", scanTaskId))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void issueSummaryReport_ValidScanTaskId_Success() throws Exception {
        log.info("[issueSummaryReport_ValidScanTaskId_Success]");
        Path tempFilePath = Files.createTempFile("defect_summary_report", ".pdf");
        String content = "xxx";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("xxx").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        MockMultipartFile mockMultipartFile = new MockMultipartFile(file.getName(), file.getAbsolutePath(), MediaType.APPLICATION_OCTET_STREAM.toString(), new FileInputStream(file));
        when(reportService.generateIssueSummaryReport(any(), any(), any(), any())).thenReturn(mockMultipartFile.getResource());
        when(ruleService.findRuleSet(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(ruleSet2)));

        MvcResult result = mockMvc.perform(get("/api/report_service/v2/issue_summary/scan_task/{id}", scanTaskId)
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM.toString(), result.getResponse().getContentType());
        assertEquals(content, result.getResponse().getContentAsString());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void issueSummaryReport_ValidScanTaskIdWithMatchDefaultRuleSet_Success() throws Exception {
        log.info("[issueSummaryReport_ValidScanTaskIdWithMatchDefaultRuleSet_Success]");
        Path tempFilePath = Files.createTempFile("defect_summary_report", ".pdf");
        String content = "xxx";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("xxx").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        MockMultipartFile mockMultipartFile = new MockMultipartFile(file.getName(), file.getAbsolutePath(), MediaType.APPLICATION_OCTET_STREAM.toString(), new FileInputStream(file));
        when(reportService.generateIssueSummaryReport(any(), any(), any(), any())).thenReturn(mockMultipartFile.getResource());
        when(ruleService.findRuleSet(any())).thenReturn(new PageImpl<>(Arrays.asList(ruleSet1, ruleSet2)));
        when(ruleService.getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersion(any(), any(), any(), any())).thenReturn(Optional.of(ruleSet1));

        MvcResult result = mockMvc.perform(get("/api/report_service/v2/issue_summary/scan_task/{id}", scanTaskId)
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM.toString(), result.getResponse().getContentType());
        assertEquals(content, result.getResponse().getContentAsString());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void issueSummaryReport_NoRuleSet_NotFound() throws Exception {
        log.info("[issueSummaryReport_noRuleSet_NotFound]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("xxx").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(ruleService.findRuleSet(any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/report_service/v2/issue_summary/scan_task/{id}", scanTaskId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void issueSummaryReportWithRuleSet_ScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[issueSummaryReportWithRuleSet_ScanTaskNotFound_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/report_service/v2/issue_summary/scan_task/{id}/rule_set/{ruleSet}", scanTaskId, ruleSet1.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void issueSummaryReportWithRuleSet_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[issueSummaryReportWithRuleSet_InsufficientPrivilege_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("xxx").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        mockMvc.perform(get("/api/report_service/v2/issue_summary/scan_task/{id}/rule_set/{ruleSet}", scanTaskId, ruleSet1.getId()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void issueSummaryReportWithRuleSet_ValidScanTaskId_Success() throws Exception {
        log.info("[issueSummaryReportWithRuleSet_ValidScanTaskId_Success]");
        Resource resource = mock(Resource.class);
        when(resource.getFile()).thenReturn(new File("/home/xc5/file.pdf"));
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(IOUtils.toInputStream("test", Charset.defaultCharset()));

        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("xxx").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(reportService.generateIssueSummaryReport(any(), any(), any(), any())).thenReturn(resource);
        when(ruleService.findRuleSetById(ruleSet2.getId())).thenReturn(Optional.of(ruleSet2));

        mockMvc.perform(get("/api/report_service/v2/issue_summary/scan_task/{id}/rule_set/{ruleSet}", scanTaskId, ruleSet2.getId())
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().string("test"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void issueSummaryReportWithRuleSet_NoRuleSet_NotFound() throws Exception {
        log.info("[issueSummaryReportWithRuleSet_noRuleSet_NotFound]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("xxx").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(ruleService.findRuleSetById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/report_service/v2/issue_summary/scan_task/{id}/rule_set/{ruleSet}", scanTaskId, ruleSet1.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void exportIssueReport_WithoutRuleSetScanTaskNotFound_ThrowAppException() throws Exception {
        log.info("[exportIssueReport_WithoutRuleSetScanTaskNotFound_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();

        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(UUID.randomUUID())
                .scanTaskId(scanTaskId).build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(searchIssueGroupRequest);

        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.empty());




        mockMvc.perform(post("/api/report_service/v2/issue_report/format/csv/type/SINGLE/delta/true").content(requestJson).contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void exportIssueReport_WithoutRuleSetInsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[exportIssueReport_WithoutRuleSetInsufficientPrivilege_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("xxx").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(UUID.randomUUID())
                .scanTaskId(scanTaskId).build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(searchIssueGroupRequest);
        mockMvc.perform(post("/api/report_service/v2/issue_report/format/csv/type/SINGLE/delta/true").content(requestJson).contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void exportIssueReport_WithoutRuleSetInvalidFormat_ThrowAppException() throws Exception {
        log.info("[exportIssueReport_WithoutRuleSetInvalidFormat_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();

        Project project=Project.builder().createdBy("xxx").id(UUID.randomUUID()).build();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(project).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(projectService.findById(any())).thenReturn(Optional.of(project));

        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(UUID.randomUUID())
                .scanTaskId(scanTaskId).build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(searchIssueGroupRequest);
        mockMvc.perform(post("/api/report_service/v2/issue_report/format/csv").content(requestJson).contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_INTERNAL_ERROR))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INTERNAL_ERROR));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void exportIssueReport_WithoutRuleSetResourceIsNull_ThrowAppException() throws Exception {
        log.info("[exportIssueReport_WithoutRuleSetResourceIsNull_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("xxx").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(reportService.generateIssueCsvReport(any(), any(), any(), anyBoolean(), any(), anyBoolean(), any())).thenReturn(null);
        mockMvc.perform(get("/api/report_service/v2/issue_report/format/csv", scanTaskId))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_INTERNAL_ERROR))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INTERNAL_ERROR));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void exportIssueReport_WithoutRuleSetResourceNotExist_ThrowAppException() throws Exception {
        log.info("[exportIssueReport_WithoutRuleSetResourceNotExist_ThrowAppException]");
        UUID scanTaskId = UUID.randomUUID();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(Project.builder().createdBy("xxx").build()).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        Resource resource = mock(Resource.class);
        when(resource.exists()).thenReturn(false);
        when(reportService.generateIssueCsvReport(any(), any(), any(), anyBoolean(), any(), anyBoolean(), any())).thenReturn(resource);
        mockMvc.perform(get("/api/report_service/v2/issue_report/format/csv", scanTaskId))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_INTERNAL_ERROR))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INTERNAL_ERROR));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void exportIssueReport_WithoutRuleSetSuccess_ReturnResource() throws Exception {
        log.info("[exportIssueReport_WithoutRuleSetSuccess_ReturnResource]");
        UUID scanTaskId = UUID.randomUUID();
        Project project=Project.builder().createdBy("xxx").id(UUID.randomUUID()).build();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(project).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(projectService.findById(any())).thenReturn(Optional.of(project));

        String csvContent = "ID,File,Line,Function,Variable,Type,Certainty,Risk/Rank,Severity,Likelihood,RemediationCost,Scanned using:,Defect Category,Action,Assigned to,Description\n" +
                "00005,src/ziplist.c,1187,ziplistBlobLen,zl,NPD,M,HIGH,HIGH,LIKELY,MEDIUM,Xcalibyte,ROBUSTNESS,PENDING,Tom,Very Dangerous";
        Resource resource = new ByteArrayResource(csvContent.getBytes());
        when(reportService.generateIssueCsvReport(any(), any(), any(), anyBoolean(), any(), anyBoolean(), any())).thenReturn(resource);


        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(UUID.randomUUID())
                .scanTaskId(scanTaskId).build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(searchIssueGroupRequest);
        mockMvc.perform(post("/api/report_service/v2/issue_report/format/csv/type/SINGLE/delta/true").content(requestJson).contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().string(csvContent));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void generateResourceResponse_noExtension() throws Exception {
        log.info("[generateResourceResponse_noExtension]");
        UUID scanTaskId = UUID.randomUUID();
        Project project=Project.builder().createdBy("xxx").id(UUID.randomUUID()).build();
        ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(project).build();
        when(scanTaskService.findById(scanTaskId)).thenReturn(Optional.of(scanTask));
        when(projectService.findById(any())).thenReturn(Optional.of(project));
        Resource resource = mock(Resource.class);
        when(resource.getFile()).thenReturn(new File("/home/xc5/unknown_ext"));
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(IOUtils.toInputStream("test", Charset.defaultCharset()));
        when(reportService.generateIssueCsvReport(any(), any(), any(), anyBoolean(), any(), anyBoolean(), any())).thenReturn(resource);
        SearchIssueGroupRequest searchIssueGroupRequest = SearchIssueGroupRequest.builder()
                .projectId(UUID.randomUUID())
                .scanTaskId(scanTaskId).build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(searchIssueGroupRequest);
        mockMvc.perform(post("/api/report_service/v2/issue_report/format/csv/type/SINGLE/delta/true").content(requestJson).contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

}
