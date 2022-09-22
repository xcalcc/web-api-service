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
import com.xcal.api.config.WithMockCustomUser;
import com.xcal.api.entity.RuleInformation;
import com.xcal.api.entity.RuleSet;
import com.xcal.api.entity.ScanEngine;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.RuleInformationDto;
import com.xcal.api.model.payload.RestResponsePage;
import com.xcal.api.model.payload.RuleSetRequest;
import com.xcal.api.service.CacheService;
import com.xcal.api.service.RuleService;
import com.xcal.api.service.RuleStandardService;
import com.xcal.api.util.CommonUtil;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.HttpURLConnection;
import java.util.*;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.MOCK)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
class RuleControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean
    RuleService ruleService;

    @MockBean
    RuleStandardService ruleStandardService;

    @MockBean
    private CacheService cacheService;

    @NonNull ModelMapper modelMapper;
    @NonNull ObjectMapper om;
    @NonNull Tracer tracer;

    private ScanEngine scanEngine = ScanEngine.builder()
            .id(UUID.randomUUID())
            .name("test_rule")
            .version("1.0")
            .revision("1")
            .description("test_description")
            .language("C \\ C++)")
            .build();
    private RuleSet ruleSet = RuleSet.builder()
            .id(UUID.randomUUID())
            .scanEngine(scanEngine)
            .name("Test rule 1")
            .version("1.0")
            .revision("1")
            .build();

    private RuleSet ruleSet2 = RuleSet.builder()
            .id(UUID.randomUUID())
            .scanEngine(scanEngine)
            .name("Test rule 2")
            .version("1.0")
            .revision("1")
            .build();
    private RuleInformation ruleInformation = RuleInformation.builder()
            .id(UUID.randomUUID())
            .ruleSet(ruleSet)
            .category("TEST")
            .ruleCode("D_TEST")
            .language("C \\ C++)")
            .name("test_rule")
            .severity(RuleInformation.Severity.HIGH)
            .priority(RuleInformation.Priority.LOW)
            .build();
    private RuleInformationDto ruleInformationDto = RuleInformationDto.builder()
            .id(ruleInformation.getId())
            .ruleSet(ruleSet.getName())
            .category(ruleInformation.getCategory())
            .ruleCode(ruleInformation.getRuleCode())
            .language(ruleInformation.getLanguage())
            .name(ruleInformation.getName())
            .severity(ruleInformation.getSeverity().name())
            .priority(ruleInformation.getPriority().name())
            .build();
    private final String adminUsername = "admin";

    @BeforeEach
    void setUp() {
        doNothing().when(cacheService).initCacheRuleInformation();
    }

    @Test
    @WithMockCustomUser()
    void getRuleSetTest() throws Exception {
        when(ruleService.findRuleSetById(ruleSet.getId())).thenReturn(Optional.of(ruleSet));
        mockMvc.perform (get("/api/rule_service/v2/rule_set/{id}", ruleSet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ruleSet.getId().toString()))
                .andExpect(jsonPath("$.displayName").value((ruleSet.getDisplayName())))
                .andExpect(jsonPath("$.name").value((ruleSet.getName())));

    }
    @Test
    @WithMockCustomUser()
    void getRuleSetsTest() throws Exception {
        Page<RuleSet> expectedResult = new RestResponsePage<>(Arrays.asList(ruleSet, ruleSet2));
        when(ruleService.findRuleSet(any(Pageable.class))).thenReturn((expectedResult));
        mockMvc.perform (get(CommonUtil.formatString("/api/rule_service/v2/rule_sets"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(ruleSet.getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(ruleSet2.getId().toString()));
    }

    @Test
    @WithMockCustomUser()
    void getRuleSet_RuleSetNotFound_ThrowException() throws Exception {
        log.info("[getRuleSet_RuleSetNotFound_ThrowException]");
        when(ruleService.findRuleSetById(ruleSet.getId())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/rule_service/v2/rule_set/{id}", ruleSet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));

    }

    @Test
    @WithMockCustomUser()
    void getRuleInformation_RuleNotFound_ThrowException() throws Exception {
        log.info("[getRuleInformation_RuleNotFound_ThrowException]");
        when(ruleService.findById(ruleInformation.getId())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/rule_service/v2/rule/{id}", ruleInformation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void getRuleInformation_validRuleId_ReturnRuleInformationDto() throws Exception {
        log.info("[getRuleInformation_RuleNotFound_ThrowException]");
        when(ruleService.findById(ruleInformation.getId())).thenReturn(Optional.of(ruleInformation));
        when(ruleService.convertRuleInformationToDto(ruleInformation, Locale.ENGLISH)).thenReturn(ruleInformationDto);
        mockMvc.perform(get("/api/rule_service/v2/rule/{id}", ruleInformation.getId())
                .param("locale", "en")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleSet").value(ruleInformationDto.getRuleSet()))
                .andExpect(jsonPath("$.category").value(ruleInformationDto.getCategory()))
                .andExpect(jsonPath("$.ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.language").value(ruleInformationDto.getLanguage()))
                .andExpect(jsonPath("$.name").value(ruleInformationDto.getName()))
                .andExpect(jsonPath("$.severity").value(ruleInformationDto.getSeverity()))
                .andExpect(jsonPath("$.priority").value(ruleInformationDto.getPriority()))
                .andExpect(jsonPath("$.language").value(ruleInformationDto.getLanguage()));
    }

    @Test
    @WithMockCustomUser()
    void getRuleInformationByScanEngineNameAndRuleSetNameAndRuleSetVersion_Request_ReturnRuleInformationDto() throws Exception {
        log.info("[getRuleInformationByScanEngineNameAndRuleSetNameAndRuleSetVersion_Request_ReturnRuleInformationDto]");
        when(ruleService.getRuleInformation(any(), any(), any(), any())).thenReturn(Collections.singletonList(ruleInformation));
        when(ruleService.convertRuleInformationListToDto(Collections.singletonList(ruleInformation), Locale.ENGLISH)).thenReturn(Collections.singletonList(ruleInformationDto));
        mockMvc.perform(get("/api/rule_service/v2/rule/scan_engine/name/{scanEngineName}/rule_set/name/{ruleSetName}/version/{ruleSetVersion}", "testEngineName", "testRuleSetName", "testRuleSetVersion")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].ruleSet").value(ruleInformationDto.getRuleSet()))
                .andExpect(jsonPath("$.[0].category").value(ruleInformationDto.getCategory()))
                .andExpect(jsonPath("$.[0].ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.[0].language").value(ruleInformationDto.getLanguage()))
                .andExpect(jsonPath("$.[0].name").value(ruleInformationDto.getName()))
                .andExpect(jsonPath("$.[0].severity").value(ruleInformationDto.getSeverity()))
                .andExpect(jsonPath("$.[0].priority").value(ruleInformationDto.getPriority()))
                .andExpect(jsonPath("$.[0].language").value(ruleInformationDto.getLanguage()));
    }

    @Test
    @WithMockCustomUser()
    void getRuleInformationByRuleSetNameAndRuleSetVersion_Request_ReturnRuleInformationDto() throws Exception {
        log.info("[getRuleInformationByRuleSetNameAndRuleSetVersion_Request_ReturnRuleInformationDto]");
        when(ruleService.getRuleInformation(any(), any(), any(), any())).thenReturn(Collections.singletonList(ruleInformation));
        when(ruleService.convertRuleInformationListToDto(Collections.singletonList(ruleInformation), Locale.ENGLISH)).thenReturn(Collections.singletonList(ruleInformationDto));
        mockMvc.perform(get("/api/rule_service/v2/rule/rule_set/name/{name}/version/{version}", "testRuleSetName", "testRuleSetVersion")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].ruleSet").value(ruleInformationDto.getRuleSet()))
                .andExpect(jsonPath("$.[0].category").value(ruleInformationDto.getCategory()))
                .andExpect(jsonPath("$.[0].ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.[0].language").value(ruleInformationDto.getLanguage()))
                .andExpect(jsonPath("$.[0].name").value(ruleInformationDto.getName()))
                .andExpect(jsonPath("$.[0].severity").value(ruleInformationDto.getSeverity()))
                .andExpect(jsonPath("$.[0].priority").value(ruleInformationDto.getPriority()))
                .andExpect(jsonPath("$.[0].language").value(ruleInformationDto.getLanguage()));
    }

    @Test
    @WithMockCustomUser()
    void getRuleInformationByRuleSetName_Request_ReturnRuleInformationDto() throws Exception {
        log.info("[getRuleInformationByRuleSetName_Request_ReturnRuleInformationDto]");
        when(ruleService.getRuleInformation(any(), any(), any(), any())).thenReturn(Collections.singletonList(ruleInformation));
        when(ruleService.convertRuleInformationListToDto(Collections.singletonList(ruleInformation), Locale.ENGLISH)).thenReturn(Collections.singletonList(ruleInformationDto));
        mockMvc.perform(get("/api/rule_service/v2/rule/rule_set/name/{name}", "testRuleSetName", "testRuleSetVersion")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].ruleSet").value(ruleInformationDto.getRuleSet()))
                .andExpect(jsonPath("$.[0].category").value(ruleInformationDto.getCategory()))
                .andExpect(jsonPath("$.[0].ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.[0].language").value(ruleInformationDto.getLanguage()))
                .andExpect(jsonPath("$.[0].name").value(ruleInformationDto.getName()))
                .andExpect(jsonPath("$.[0].severity").value(ruleInformationDto.getSeverity()))
                .andExpect(jsonPath("$.[0].priority").value(ruleInformationDto.getPriority()))
                .andExpect(jsonPath("$.[0].language").value(ruleInformationDto.getLanguage()));
    }

    @Test
    @WithMockCustomUser()
    void getRuleInformationByRuleSetId_RuleSetNotFound_ThrowException() throws Exception {
        log.info("[getRuleInformationByRuleSetId_RuleSetNotFound_ThrowException]");
        when(ruleService.findRuleSetById(ruleSet.getId())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/rule_service/v2/rule/rule_set/{id}", ruleSet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void getRuleInformationByRuleSetId_InputRuleSetId_ReturnRuleInformationDto() throws Exception {
        log.info("[getRuleInformationByRuleSetId_RuleSetNotFound_ThrowException]");
        when(ruleService.findRuleSetById(ruleSet.getId())).thenReturn(Optional.of(ruleSet));
        when(ruleService.findDtoByRuleSet(ruleSet, Locale.ENGLISH)).thenReturn(Collections.singletonList(ruleInformationDto));
        mockMvc.perform(get("/api/rule_service/v2/rule/rule_set/{id}", ruleSet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .param("locale", "en")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].ruleSet").value(ruleInformationDto.getRuleSet()))
                .andExpect(jsonPath("$.[0].category").value(ruleInformationDto.getCategory()))
                .andExpect(jsonPath("$.[0].ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.[0].language").value(ruleInformationDto.getLanguage()))
                .andExpect(jsonPath("$.[0].name").value(ruleInformationDto.getName()))
                .andExpect(jsonPath("$.[0].severity").value(ruleInformationDto.getSeverity()))
                .andExpect(jsonPath("$.[0].priority").value(ruleInformationDto.getPriority()))
                .andExpect(jsonPath("$.[0].language").value(ruleInformationDto.getLanguage()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void addRuleSet_Request_ReturnRuleInformationDto() throws Exception {
        log.info("[addRuleSet_Request_ReturnRuleInformationDto]");
        when(ruleService.addRuleSets(any(), any())).thenReturn(Collections.singletonList(ruleInformation));
        when(ruleService.convertRuleInformationListToDto(Collections.singletonList(ruleInformation), Locale.ENGLISH)).thenReturn(Collections.singletonList(ruleInformationDto));
        mockMvc.perform(post("/api/rule_service/v2/rule_set")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(RuleSetRequest.builder().name("testName").build()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].ruleSet").value(ruleInformationDto.getRuleSet()))
                .andExpect(jsonPath("$.[0].category").value(ruleInformationDto.getCategory()))
                .andExpect(jsonPath("$.[0].ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.[0].language").value(ruleInformationDto.getLanguage()))
                .andExpect(jsonPath("$.[0].name").value(ruleInformationDto.getName()))
                .andExpect(jsonPath("$.[0].severity").value(ruleInformationDto.getSeverity()))
                .andExpect(jsonPath("$.[0].priority").value(ruleInformationDto.getPriority()))
                .andExpect(jsonPath("$.[0].language").value(ruleInformationDto.getLanguage()));
    }

    @Test
    @WithMockCustomUser()
    void addRuleSet_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[addRuleSet_WithNonAdminUser_ReturnAccessDeniedException]");
        when(ruleService.addRuleSets(any(), any())).thenReturn(Collections.singletonList(ruleInformation));
        when(ruleService.convertRuleInformationListToDto(Collections.singletonList(ruleInformation), Locale.ENGLISH)).thenReturn(Collections.singletonList(ruleInformationDto));
        mockMvc.perform(post("/api/rule_service/v2/rule_set")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(RuleSetRequest.builder().name("testName").build()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }
}
