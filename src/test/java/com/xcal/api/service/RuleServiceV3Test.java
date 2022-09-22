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

import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.RuleInformationDto;
import com.xcal.api.model.payload.RestResponsePage;
import com.xcal.api.model.payload.RuleSetRequest;
import com.xcal.api.repository.RuleInformationRepository;
import com.xcal.api.repository.RuleSetRepository;
import com.xcal.api.repository.ScanEngineRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Slf4j
class RuleServiceV3Test {
    private RuleService ruleService;
    private RuleInformationRepository ruleInformationRepository;
    private RuleSetRepository ruleSetRepository;
    private ScanEngineRepository scanEngineRepository;
    private I18nService i18nService;

    private String currentUserName = "user";
    private User currentUser = User.builder().username(currentUserName).displayName("testDisplayName").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();
    private String scanEngineName = "scanEngineName";
    private String scanEngineLanguage = "C++";
    private String scanEngineProvider = "Test provider";
    private String scanEngineProviderUrl = "Test provider url";
    private String scanEngineUrl = "test url";
    private String scanEngineVersion = "test scanEngineVersion";
    private ScanEngine scanEngine = ScanEngine.builder().name(scanEngineName).version(scanEngineVersion).language(scanEngineLanguage).provider(scanEngineProvider)
            .providerUrl(scanEngineProviderUrl).url(scanEngineUrl).build();

    private UUID ruleSetId = UUID.fromString("11111111-1111-1111-1110-111111111110");
    private String ruleSetName = "ruleset name";
    private String ruleSetDisplayName = "ruleset display name";
    private String ruleSetLanguage = "C++";
    private String ruleSetProvider = "test provider";
    private String ruleSetProvideUrl = "test provider url ";
    private String ruleSetVersion = "test ruleset version ";
    private RuleSet ruleSet = RuleSet.builder().id(ruleSetId).name(ruleSetName).version(ruleSetVersion).displayName(ruleSetDisplayName).scanEngine(scanEngine).
            language(ruleSetLanguage).provider(ruleSetProvider).providerUrl(ruleSetProvideUrl).build();

    private String ruleSetName1 = "ruleset name1";
    private String ruleSetLanguage1 = "C++";
    private String ruleSetProvider1 = "test provider1";
    private String ruleSetProvideUrl1 = "test provider url1";
    private String ruleSetVersion1 = "test ruleset version1";

    private RuleSetRequest.RuleSet.Rule rule1 = RuleSetRequest.RuleSet.Rule.builder().name("ruleName1").description("rule desc1")
            .category("rule category1").code("code1").detail("detail1").language("C++").fixCost(RuleInformation.RemediationCost.HIGH.name())
            .severity(RuleInformation.Severity.HIGH.name()).priority(RuleInformation.Priority.HIGH.name()).likelihood(RuleInformation.Likelihood.LIKELY.name()).build();
    private RuleSetRequest.RuleSet.Rule rule2 = RuleSetRequest.RuleSet.Rule.builder().name("ruleName2").description("rule desc2")
            .category("rule category2").code("code2").detail("detail2").language("C++").fixCost(RuleInformation.RemediationCost.HIGH.name())
            .severity(RuleInformation.Severity.HIGH.name()).priority(RuleInformation.Priority.HIGH.name()).likelihood(RuleInformation.Likelihood.LIKELY.name()).build();
    private List<RuleSetRequest.RuleSet.Rule> ruleList1 = Arrays.asList(rule1, rule2);
    private RuleSetRequest.RuleSet ruleSet1 = RuleSetRequest.RuleSet.builder().name(ruleSetName1).version(ruleSetVersion1).
            language(ruleSetLanguage1).provider(ruleSetProvider1).providerUrl(ruleSetProvideUrl1).rules(ruleList1).build();

    private String ruleSetName2 = "ruleset name2";
    private String ruleSetLanguage2 = "C++";
    private String ruleSetProvider2 = "test provider2";
    private String ruleSetProvideUrl2 = "test provider url2";
    private String ruleSetVersion2 = "test ruleset version2";
    private RuleSetRequest.RuleSet.Rule rule3 = RuleSetRequest.RuleSet.Rule.builder().name("ruleName3").description("rule desc3")
            .category("rule category3").code("code3").detail("detail3").language("C++").fixCost(RuleInformation.RemediationCost.HIGH.name())
            .severity(RuleInformation.Severity.HIGH.name()).priority(RuleInformation.Priority.HIGH.name()).likelihood(RuleInformation.Likelihood.LIKELY.name()).build();
    private RuleSetRequest.RuleSet.Rule rule4 = RuleSetRequest.RuleSet.Rule.builder().name("ruleName4").description("rule desc4")
            .category("rule category4").code("code4").detail("detail4").language("C++").fixCost(RuleInformation.RemediationCost.HIGH.name())
            .severity(RuleInformation.Severity.HIGH.name()).priority(RuleInformation.Priority.HIGH.name()).likelihood(RuleInformation.Likelihood.LIKELY.name()).build();
    private List<RuleSetRequest.RuleSet.Rule> ruleList2 = Arrays.asList(rule3, rule4);
    private RuleSetRequest.RuleSet ruleSet2 = RuleSetRequest.RuleSet.builder().name(ruleSetName2).version(ruleSetVersion2).
            language(ruleSetLanguage2).provider(ruleSetProvider2).providerUrl(ruleSetProvideUrl2).rules(ruleList2).build();
    private List<RuleSetRequest.RuleSet> ruleSetList = Arrays.asList(ruleSet1, ruleSet2);

    private String correctRuleSetRequestName = "test xcalibyte ruleSetRequestName";
    private String incorrectRuleSetRequestName = "test ruleSetRequestName";
    private String ruleSetRequestDescription = "test ruleSetRequestDescription";
    private String ruleSetRequestEngineUrl = "test ruleSetRequestEngineUrl";
    private String ruleSetRequestLanguage = "test ruleSetRequestLanguage";
    private String ruleSetRequestLicense = "test ruleSetRequestLicense";
    private String ruleSetRequestLicenseUrl = "test ruleSetRequestLicenseUrl";
    private String ruleSetRequestProvider = "test ruleSetRequestProvider";
    private String ruleSetRequestProviderUrl = "test ruleSetRequestProviderUrl";
    private String ruleSetRequestVersion = "test ruleSetRequestVersion";
    private RuleSetRequest ruleSetRequest = RuleSetRequest.builder().name(correctRuleSetRequestName).description(ruleSetRequestDescription).engineUrl(ruleSetRequestEngineUrl)
            .language(ruleSetRequestLanguage).license(ruleSetRequestLicense).licenseUrl(ruleSetRequestLicenseUrl).provider(ruleSetRequestProvider).providerUrl(ruleSetRequestProviderUrl).version(ruleSetRequestVersion).ruleSets(ruleSetList).build();

    private UUID ruleInformationId = UUID.fromString("11111111-1111-1111-1111-111111111110");
    private String ruleInformationName = "ruleInformationName";
    private String ruleCode = "test rule code";
    private String category = "test category";
    private RuleInformation.Certainty certainty = RuleInformation.Certainty.D;
    private RuleInformation.Likelihood likelihood = RuleInformation.Likelihood.LIKELY;
    private RuleInformation.Priority priority = RuleInformation.Priority.HIGH;
    private RuleInformation.Severity severity = RuleInformation.Severity.HIGH;
    private String vulnerable = "test vulnerable";
    private String ruleInformationLanguage = "C++";
    private RuleInformation ruleInformation = RuleInformation.builder().id(ruleInformationId).name(ruleInformationName).ruleCode(ruleCode).ruleSet(ruleSet).category(category).severity(severity).
            certainty(certainty).likelihood(likelihood).priority(priority).vulnerable(vulnerable).language(ruleInformationLanguage).remediationCost(RuleInformation.RemediationCost.HIGH).build();

    private UUID ruleInformationId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private String ruleInformationName1 = "ruleInformationName";
    private String ruleCode1 = "test rule code";
    private String category1 = "test category";
    private RuleInformation.Certainty certainty1 = RuleInformation.Certainty.D;
    private RuleInformation.Likelihood likelihood1 = RuleInformation.Likelihood.LIKELY;
    private RuleInformation.Priority priority1 = RuleInformation.Priority.HIGH;
    private String vulnerable1 = "test vulnerable";
    private String ruleInformationLanguage1 = "C++";
    private RuleInformation ruleInformation1 = RuleInformation.builder().id(ruleInformationId1).name(ruleInformationName1).ruleCode(ruleCode1).ruleSet(ruleSet).category(category1).
            certainty(certainty1).likelihood(likelihood1).priority(priority1).vulnerable(vulnerable1).language(ruleInformationLanguage1).build();

    private UUID ruleInformationId2 = UUID.fromString("11111111-1111-1111-1111-111111111112");
    private String ruleInformationName2 = "ruleInformationName";
    private String ruleCode2 = "test rule code";
    private String category2 = "test category";
    private RuleInformation.Certainty certainty2 = RuleInformation.Certainty.D;
    private RuleInformation.Likelihood likelihood2 = RuleInformation.Likelihood.LIKELY;
    private RuleInformation.Priority priority2 = RuleInformation.Priority.HIGH;
    private String vulnerable2 = "test vulnerable";
    private String ruleInformationLanguage2 = "C++";
    private RuleInformation ruleInformation2 = RuleInformation.builder().id(ruleInformationId2).name(ruleInformationName2).ruleCode(ruleCode2).ruleSet(ruleSet).category(category2).
            certainty(certainty2).likelihood(likelihood2).priority(priority2).vulnerable(vulnerable2).language(ruleInformationLanguage2).build();
    private List<RuleInformation> ruleInformationList = Arrays.asList(ruleInformation1, ruleInformation2);

    private I18nMessage enTest = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.ENGLISH.toLanguageTag()).key("test").content("Test").build();

    @BeforeEach
    void setUp() {
        ruleInformationRepository = mock(RuleInformationRepository.class);
        ruleSetRepository = mock(RuleSetRepository.class);
        scanEngineRepository = mock(ScanEngineRepository.class);
        i18nService = mock(I18nService.class);
        UserService userService = mock(UserService.class);
        ruleService = new RuleService(ruleInformationRepository, ruleSetRepository, scanEngineRepository, userService, i18nService);
    }

    @Test
    void findByRuleSetTestSuccess() {
        log.info("[findByRuleSetTestSuccess]");
        when(ruleInformationRepository.findByRuleSet(ruleSet)).thenReturn(ruleInformationList);
        List<RuleInformation> resultRuleInformationList = ruleService.findByRuleSet(ruleSet);
        assertEquals(ruleInformationList.size(), resultRuleInformationList.size());
        assertEquals(ruleInformationList.get(0).getName(), resultRuleInformationList.get(0).getName());
        assertEquals(ruleInformationList.get(0).getRuleCode(), resultRuleInformationList.get(0).getRuleCode());
        assertEquals(ruleInformationList.get(0).getRuleSet(), resultRuleInformationList.get(0).getRuleSet());
        assertEquals(ruleInformationList.get(0).getCategory(), resultRuleInformationList.get(0).getCategory());
        assertEquals(ruleInformationList.get(0).getCertainty(), resultRuleInformationList.get(0).getCertainty());
        assertEquals(ruleInformationList.get(0).getLikelihood(), resultRuleInformationList.get(0).getLikelihood());
        assertEquals(ruleInformationList.get(0).getPriority(), resultRuleInformationList.get(0).getPriority());
        assertEquals(ruleInformationList.get(0).getVulnerable(), resultRuleInformationList.get(0).getVulnerable());
        assertEquals(ruleInformationList.get(0).getLanguage(), resultRuleInformationList.get(0).getLanguage());
        assertEquals(ruleInformationList.get(1).getName(), resultRuleInformationList.get(1).getName());
        assertEquals(ruleInformationList.get(1).getRuleCode(), resultRuleInformationList.get(1).getRuleCode());
        assertEquals(ruleInformationList.get(1).getRuleSet(), resultRuleInformationList.get(1).getRuleSet());
        assertEquals(ruleInformationList.get(1).getCategory(), resultRuleInformationList.get(1).getCategory());
        assertEquals(ruleInformationList.get(1).getCertainty(), resultRuleInformationList.get(1).getCertainty());
        assertEquals(ruleInformationList.get(1).getLikelihood(), resultRuleInformationList.get(1).getLikelihood());
        assertEquals(ruleInformationList.get(1).getPriority(), resultRuleInformationList.get(1).getPriority());
        assertEquals(ruleInformationList.get(1).getVulnerable(), resultRuleInformationList.get(1).getVulnerable());
        assertEquals(ruleInformationList.get(1).getLanguage(), resultRuleInformationList.get(1).getLanguage());
    }

    @Test
    void findDtoByRuleSetTest_Success(){
        log.info("[findDtoByRuleSetTest_Success]");
        when(ruleInformationRepository.findByRuleSet(any(RuleSet.class))).thenReturn(Collections.singletonList(ruleInformation));
        when(ruleInformationRepository.findByRuleSet(ruleSet)).thenReturn(Collections.singletonList(ruleInformation));
        when(i18nService.getI18nMessageByKeyPrefix(any(), any(Locale.class))).thenReturn(Collections.singletonList(enTest));
        Locale locale = Locale.ENGLISH;
        List<RuleInformationDto> ruleInformationDtoList = ruleService.findDtoByRuleSet(ruleSet, locale);
        assertEquals(1, ruleInformationDtoList.size());
        assertEquals(ruleInformationDtoList.get(0).getLanguage(), ruleInformation.getLanguage());
        assertEquals(ruleInformationDtoList.get(0).getRuleCode(), ruleInformation.getRuleCode());
        assertEquals(ruleInformationDtoList.get(0).getName(), ruleInformation.getName());
        assertEquals(ruleInformationDtoList.get(0).getRuleSetDisplayName(), ruleInformation.getRuleSet().getDisplayName());

    }

    @Test
    void getByRuleCodeTestSuccess() {
        log.info("[getByRuleCodeTestSuccess]");
        when(ruleInformationRepository.findOneByRuleSetScanEngineNameAndRuleSetNameAndRuleCode(scanEngineName, ruleSetName, ruleCode)).thenReturn(Optional.of(ruleInformation));
        Optional<RuleInformation> resultRuleInformationOptional = ruleService.getByRuleCode(scanEngineName, ruleSetName, ruleCode);
        assertTrue(resultRuleInformationOptional.isPresent());
        assertEquals(ruleInformation.getName(), resultRuleInformationOptional.get().getName());
        assertEquals(ruleInformation.getRuleCode(), resultRuleInformationOptional.get().getRuleCode());
        assertEquals(ruleInformation.getRuleSet(), resultRuleInformationOptional.get().getRuleSet());
        assertEquals(ruleInformation.getCategory(), resultRuleInformationOptional.get().getCategory());
        assertEquals(ruleInformation.getCertainty(), resultRuleInformationOptional.get().getCertainty());
        assertEquals(ruleInformation.getLikelihood(), resultRuleInformationOptional.get().getLikelihood());
        assertEquals(ruleInformation.getPriority(), resultRuleInformationOptional.get().getPriority());
        assertEquals(ruleInformation.getVulnerable(), resultRuleInformationOptional.get().getVulnerable());
        assertEquals(ruleInformation.getLanguage(), resultRuleInformationOptional.get().getLanguage());
    }

    @Test
    void findByIdTestSuccess() {
        log.info("[findByIdTestSuccess]");
        when(ruleInformationRepository.findById(ruleInformationId)).thenReturn(Optional.of(ruleInformation));
        Optional<RuleInformation> optionalRuleInformation = ruleService.findById(ruleInformationId);
        assertTrue(optionalRuleInformation.isPresent());
        assertEquals(ruleInformation.getName(), optionalRuleInformation.get().getName());
        assertEquals(ruleInformation.getRuleCode(), optionalRuleInformation.get().getRuleCode());
        assertEquals(ruleInformation.getRuleSet(), optionalRuleInformation.get().getRuleSet());
        assertEquals(ruleInformation.getCategory(), optionalRuleInformation.get().getCategory());
        assertEquals(ruleInformation.getCertainty(), optionalRuleInformation.get().getCertainty());
        assertEquals(ruleInformation.getLikelihood(), optionalRuleInformation.get().getLikelihood());
        assertEquals(ruleInformation.getPriority(), optionalRuleInformation.get().getPriority());
        assertEquals(ruleInformation.getVulnerable(), optionalRuleInformation.get().getVulnerable());
        assertEquals(ruleInformation.getLanguage(), optionalRuleInformation.get().getLanguage());
    }

    @Test
    void getRuleInformation_AllNonEmpty_WillRunFindByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion() {
        log.info("[getRuleInformation_AllNonEmpty_WillRunFindByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion]");
        when(ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetNameAndRuleSetVersion(any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetName(any(String.class))).thenReturn(ruleInformationList);
        ruleService.getRuleInformation(scanEngineName, scanEngineVersion, ruleSetName, ruleSetVersion);
        verify(ruleInformationRepository, times(1)).findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetNameAndRuleSetVersion(any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetName(any(String.class));
    }

    @Test
    void getRuleInformationTestSuccess_ScanEngineVersionIsEmpty_WillRunFindByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion() {
        log.info("[getRuleInformationTestSuccess_ScanEngineVersionIsEmpty_WillRunFindByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion]");
        when(ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetNameAndRuleSetVersion(any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetName(any(String.class))).thenReturn(ruleInformationList);
        ruleService.getRuleInformation(scanEngineName, "", ruleSetName, ruleSetVersion);
        verify(ruleInformationRepository, times(0)).findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class), any(String.class));
        verify(ruleInformationRepository, times(1)).findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetNameAndRuleSetVersion(any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetName(any(String.class));
    }

    @Test
    void getRuleInformationTestSuccess_ScanEngineNameAndScanEngineVersionAreEmpty_WillRunFindByRuleSetNameAndRuleSetVersion() {
        log.info("[getRuleInformationTestSuccess_ScanEngineNameAndScanEngineVersionAreEmpty_WillRunFindByRuleSetNameAndRuleSetVersion]");
        when(ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetNameAndRuleSetVersion(any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetName(any(String.class))).thenReturn(ruleInformationList);
        ruleService.getRuleInformation("", "", ruleSetName, ruleSetVersion);
        verify(ruleInformationRepository, times(0)).findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class));
        verify(ruleInformationRepository, times(1)).findByRuleSetNameAndRuleSetVersion(any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetName(any(String.class));
    }

    @Test
    void getRuleInformationTestSuccess_ScanEngineNameAndScanEngineVersionAndRuleSetVersionAreEmpty_WillRunFindByRuleSetName() {
        log.info("[getRuleInformationTestSuccess_ScanEngineNameAndScanEngineVersionAndRuleSetVersionAreEmpty_WillRunFindByRuleSetName]");
        when(ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetNameAndRuleSetVersion(any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetName(any(String.class))).thenReturn(ruleInformationList);
        ruleService.getRuleInformation("", "", ruleSetName, "");
        verify(ruleInformationRepository, times(0)).findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetNameAndRuleSetVersion(any(String.class), any(String.class));
        verify(ruleInformationRepository, times(1)).findByRuleSetName(any(String.class));
    }

    @Test
    void getRuleInformationTestSuccess_RuleSetNameIsEmpty_WillRunFindByRuleSetScanEngineNameAndRuleSetScanEngineVersion() {
        log.info("[getRuleInformationTestSuccess_RuleSetNameIsEmpty_WillRunFindByRuleSetScanEngineNameAndRuleSetScanEngineVersion]");
        when(ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetNameAndRuleSetVersion(any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class), any(String.class))).thenReturn(ruleInformationList);
        when(ruleInformationRepository.findByRuleSetName(any(String.class))).thenReturn(ruleInformationList);
        ruleService.getRuleInformation(scanEngineName, scanEngineVersion, "", ruleSetVersion);
        verify(ruleInformationRepository, times(1)).findByRuleSetScanEngineNameAndRuleSetScanEngineVersion(any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(any(String.class), any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetNameAndRuleSetVersion(any(String.class), any(String.class));
        verify(ruleInformationRepository, times(0)).findByRuleSetName(any(String.class));
    }


    @Test
    void getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersionTestSuccess() {
        log.info("[getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersionTestSuccess]");
        when(ruleSetRepository.findByScanEngineNameAndScanEngineVersionAndNameAndVersion(any(String.class), argThat(sev -> sev.contains("-")), any(String.class), any(String.class))).thenReturn(Optional.empty());
        when(ruleSetRepository.findByScanEngineNameAndScanEngineVersionAndNameAndVersion(any(String.class), argThat(sev -> !sev.contains("-")), any(String.class), any(String.class))).thenReturn(Optional.of(ruleSet));
        Optional<RuleSet> resultOptionalRuleSet = ruleService.getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersion(scanEngineName, "testVersion-1", ruleSetName, ruleSetVersion);
        verify(ruleSetRepository, times(2)).findByScanEngineNameAndScanEngineVersionAndNameAndVersion(any(String.class), any(String.class), any(String.class), any(String.class));
        assertTrue(resultOptionalRuleSet.isPresent());
        assertEquals(ruleSet, resultOptionalRuleSet.get());
    }

    @Test
    void getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersionTestSuccess1() {
        log.info("[getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersionTestSuccess1]");
        when(ruleSetRepository.findByScanEngineNameAndScanEngineVersionAndNameAndVersion(any(String.class), argThat(sev -> sev.contains("-")), any(String.class), any(String.class))).thenReturn(Optional.empty());
        when(ruleSetRepository.findByScanEngineNameAndScanEngineVersionAndNameAndVersion(any(String.class), argThat(sev -> !sev.contains("-")), any(String.class), any(String.class))).thenReturn(Optional.of(ruleSet));
        Optional<RuleSet> resultOptionalRuleSet = ruleService.getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersion(scanEngineName, "testVersion", ruleSetName, ruleSetVersion);
        verify(ruleSetRepository, times(1)).findByScanEngineNameAndScanEngineVersionAndNameAndVersion(any(String.class), any(String.class), any(String.class), any(String.class));
        assertTrue(resultOptionalRuleSet.isPresent());
        assertEquals(ruleSet, resultOptionalRuleSet.get());
    }

    @Test
    void getRuleSetByNameAndVersionTestSuccess() {
        log.info("[getRuleSetByNameAndVersionTestSuccess]");
        when(ruleSetRepository.findByNameAndVersion(ruleSetName, ruleSetVersion)).thenReturn(Optional.of(ruleSet));
        Optional<RuleSet> resultOptionalRuleSet = ruleService.getRuleSetByNameAndVersion(ruleSetName, ruleSetVersion);
        assertTrue(resultOptionalRuleSet.isPresent());
        assertEquals(ruleSet, resultOptionalRuleSet.get());
    }

    @Test
    void findRuleSetByIdTestSuccess() {
        log.info("[findRuleSetByIdTestSuccess]");
        when(ruleSetRepository.findById(ruleSetId)).thenReturn(Optional.of(ruleSet));
        Optional<RuleSet> resultOptionalRuleSet = ruleService.findRuleSetById(ruleSetId);
        assertTrue(resultOptionalRuleSet.isPresent());
        assertEquals(ruleSet, resultOptionalRuleSet.get());
    }

    @Test
    void convertRuleInformationToDtoTestSuccess() {
        log.info("[convertRuleInformationToDtoTestSuccess]");
        RuleInformationDto resultRuleInformationDto = ruleService.convertRuleInformationToDto(ruleInformation, Locale.ENGLISH);
        assertEquals(ruleInformation.getId(), resultRuleInformationDto.getId());
        assertEquals(ruleSet.getName(), resultRuleInformationDto.getRuleSet());
        assertEquals(ruleSet.getVersion(), resultRuleInformationDto.getRuleSetVersion());
        assertEquals(ruleSet.getDisplayName(), resultRuleInformationDto.getRuleSetDisplayName());
        assertEquals(ruleInformation.getCategory(), resultRuleInformationDto.getCategory());
        assertEquals(ruleInformation.getVulnerable(), resultRuleInformationDto.getVulnerable());
        assertEquals(ruleInformation.getCertainty().name(), resultRuleInformationDto.getCertainty());
        assertEquals(ruleInformation.getRuleCode(), resultRuleInformationDto.getRuleCode());
        assertEquals(ruleInformation.getLanguage(), resultRuleInformationDto.getLanguage());
        assertEquals(ruleInformation.getUrl(), resultRuleInformationDto.getUrl());
        assertEquals(ruleInformation.getName(), resultRuleInformationDto.getName());
        assertEquals(ruleInformation.getSeverity().name(), resultRuleInformationDto.getSeverity());
        assertEquals(ruleInformation.getPriority().name(), resultRuleInformationDto.getPriority());
        assertEquals(ruleInformation.getLikelihood().name(), resultRuleInformationDto.getLikelihood());
        assertEquals(ruleInformation.getRemediationCost().name(), resultRuleInformationDto.getRemediationCost());
        assertEquals(ruleInformation.getDetail(), resultRuleInformationDto.getDetail());
        assertEquals(ruleInformation.getDescription(), resultRuleInformationDto.getDescription());
        assertEquals(ruleInformation.getMessageTemplate(), resultRuleInformationDto.getMessageTemplate());
    }

    @Test
    void addRuleSetsTestSuccess() throws AppException {
        log.info("[addRuleSetsTestSuccess]");
        when(scanEngineRepository.findByNameAndVersion(ruleSetRequest.getName(), ruleSetRequest.getVersion())).thenReturn(Optional.of(scanEngine));
        when(ruleSetRepository.findByNameAndVersion(any(), any())).thenReturn(Optional.empty());
        when(ruleInformationRepository.saveAll(any())).thenReturn(ruleInformationList);
        doNothing().when(ruleInformationRepository).flush();
        List<RuleInformation> resultRuleInformationList = ruleService.addRuleSets(ruleSetRequest, currentUser);
        assertEquals(ruleInformationList.size(), resultRuleInformationList.size());
        assertEquals(ruleInformationList.get(0).getName(), resultRuleInformationList.get(0).getName());
        assertEquals(ruleInformationList.get(0).getRuleCode(), resultRuleInformationList.get(0).getRuleCode());
        assertEquals(ruleInformationList.get(0).getRuleSet(), resultRuleInformationList.get(0).getRuleSet());
        assertEquals(ruleInformationList.get(0).getCategory(), resultRuleInformationList.get(0).getCategory());
        assertEquals(ruleInformationList.get(0).getCertainty(), resultRuleInformationList.get(0).getCertainty());
        assertEquals(ruleInformationList.get(0).getLikelihood(), resultRuleInformationList.get(0).getLikelihood());
        assertEquals(ruleInformationList.get(0).getPriority(), resultRuleInformationList.get(0).getPriority());
        assertEquals(ruleInformationList.get(0).getVulnerable(), resultRuleInformationList.get(0).getVulnerable());
        assertEquals(ruleInformationList.get(0).getLanguage(), resultRuleInformationList.get(0).getLanguage());
        assertEquals(ruleInformationList.get(1).getName(), resultRuleInformationList.get(1).getName());
        assertEquals(ruleInformationList.get(1).getRuleCode(), resultRuleInformationList.get(1).getRuleCode());
        assertEquals(ruleInformationList.get(1).getRuleSet(), resultRuleInformationList.get(1).getRuleSet());
        assertEquals(ruleInformationList.get(1).getCategory(), resultRuleInformationList.get(1).getCategory());
        assertEquals(ruleInformationList.get(1).getCertainty(), resultRuleInformationList.get(1).getCertainty());
        assertEquals(ruleInformationList.get(1).getLikelihood(), resultRuleInformationList.get(1).getLikelihood());
        assertEquals(ruleInformationList.get(1).getPriority(), resultRuleInformationList.get(1).getPriority());
        assertEquals(ruleInformationList.get(1).getVulnerable(), resultRuleInformationList.get(1).getVulnerable());
        assertEquals(ruleInformationList.get(1).getLanguage(), resultRuleInformationList.get(1).getLanguage());
    }

    @Test
    void prepareRuleInformationFromRuleSetRequestTestSuccess() throws AppException {
        log.info("[prepareRuleInformationFromRuleSetRequestTestSuccess]");
        when(scanEngineRepository.findByNameAndVersion(ruleSetRequest.getName(), ruleSetRequest.getVersion())).thenReturn(Optional.of(scanEngine));
        when(ruleSetRepository.findByNameAndVersion(any(), any())).thenReturn(Optional.empty());
        List<RuleInformation> resultRuleInformationList = ruleService.prepareRuleInformationFromRuleSetRequest(ruleSetRequest, currentUser);
        assertEquals(rule1.getName(), resultRuleInformationList.get(0).getName());
        assertEquals(rule1.getCode(), resultRuleInformationList.get(0).getRuleCode());
        assertEquals(ruleInformationList.get(0).getRuleSet().getScanEngine().getName(), resultRuleInformationList.get(0).getRuleSet().getScanEngine().getName());
        assertEquals(ruleInformationList.get(0).getRuleSet().getScanEngine().getVersion(), resultRuleInformationList.get(0).getRuleSet().getScanEngine().getVersion());
        assertEquals(ruleInformationList.get(0).getRuleSet().getScanEngine().getLanguage(), resultRuleInformationList.get(0).getRuleSet().getScanEngine().getLanguage());
        assertEquals(ruleInformationList.get(0).getRuleSet().getScanEngine().getUrl(), resultRuleInformationList.get(0).getRuleSet().getScanEngine().getUrl());
        assertEquals(ruleInformationList.get(0).getRuleSet().getScanEngine().getProvider(), resultRuleInformationList.get(0).getRuleSet().getScanEngine().getProvider());
        assertEquals(ruleInformationList.get(0).getRuleSet().getScanEngine().getProviderUrl(), resultRuleInformationList.get(0).getRuleSet().getScanEngine().getProviderUrl());
        assertEquals(StringUtils.upperCase(rule1.getCategory()), resultRuleInformationList.get(0).getCategory());
        assertEquals(ruleInformationList.get(0).getCertainty(), resultRuleInformationList.get(0).getCertainty());
        assertEquals(ruleInformationList.get(0).getLikelihood(), resultRuleInformationList.get(0).getLikelihood());
        assertEquals(ruleInformationList.get(0).getPriority(), resultRuleInformationList.get(0).getPriority());
        assertEquals(rule1.getCode(), resultRuleInformationList.get(0).getVulnerable());
        assertEquals(ruleInformationList.get(0).getLanguage(), resultRuleInformationList.get(0).getLanguage());
    }

    @Test
    void prepareRuleInformationFromRuleSetRequestTestWithoutNameFail1() {
        log.info("[prepareRuleInformationFromRuleSetRequestTestFail1]");
        RuleSetRequest.RuleSet.Rule rule1 = RuleSetRequest.RuleSet.Rule.builder().description("rule desc1")
                .category("rule category1").code("code1").detail("detail1").language("C++").build();
        List<RuleSetRequest.RuleSet.Rule> ruleList = Collections.singletonList(rule1);
        RuleSetRequest.RuleSet ruleSet = RuleSetRequest.RuleSet.builder().name(ruleSetName).version(ruleSetVersion).
                language(ruleSetLanguage).provider(ruleSetProvider).providerUrl(ruleSetProvideUrl).rules(ruleList).build();
        List<RuleSetRequest.RuleSet> ruleSetList = Collections.singletonList(ruleSet);
        RuleSetRequest ruleSetRequest = RuleSetRequest.builder().name(correctRuleSetRequestName).description(ruleSetRequestDescription).engineUrl(ruleSetRequestEngineUrl)
                .language(ruleSetRequestLanguage).license(ruleSetRequestLicense).licenseUrl(ruleSetRequestLicenseUrl).provider(ruleSetRequestProvider).providerUrl(ruleSetRequestProviderUrl).version(ruleSetRequestVersion).ruleSets(ruleSetList).build();
        when(ruleSetRepository.findByNameAndVersion(any(), any())).thenReturn(Optional.empty());
        when(scanEngineRepository.findByNameAndVersion(ruleSetRequest.getName(), ruleSetRequest.getVersion())).thenReturn(Optional.of(scanEngine));
        assertThrows(AppException.class, () -> ruleService.prepareRuleInformationFromRuleSetRequest(ruleSetRequest, currentUser));
    }

    @Test
    void prepareRuleInformationFromRuleSetRequestWithoutCodeTestFail2() {
        log.info("[prepareRuleInformationFromRuleSetRequestTestFail2]");
        RuleSetRequest.RuleSet.Rule rule1 = RuleSetRequest.RuleSet.Rule.builder().name("ruleName1").description("rule desc1")
                .category("rule category1").detail("detail1").language("C++").severity(RuleInformation.Severity.HIGH.name()).build();
        List<RuleSetRequest.RuleSet.Rule> ruleList = Collections.singletonList(rule1);
        RuleSetRequest.RuleSet ruleSet = RuleSetRequest.RuleSet.builder().name(ruleSetName).version(ruleSetVersion).
                language(ruleSetLanguage).provider(ruleSetProvider).providerUrl(ruleSetProvideUrl).rules(ruleList).build();
        List<RuleSetRequest.RuleSet> ruleSetList = Collections.singletonList(ruleSet);
        RuleSetRequest ruleSetRequest = RuleSetRequest.builder().name(correctRuleSetRequestName).description(ruleSetRequestDescription).engineUrl(ruleSetRequestEngineUrl)
                .language(ruleSetRequestLanguage).license(ruleSetRequestLicense).licenseUrl(ruleSetRequestLicenseUrl).provider(ruleSetRequestProvider).providerUrl(ruleSetRequestProviderUrl).version(ruleSetRequestVersion).ruleSets(ruleSetList).build();
        when(ruleSetRepository.findByNameAndVersion(any(), any())).thenReturn(Optional.empty());
        when(scanEngineRepository.findByNameAndVersion(ruleSetRequest.getName(), ruleSetRequest.getVersion())).thenReturn(Optional.of(scanEngine));
        assertThrows(AppException.class, () -> ruleService.prepareRuleInformationFromRuleSetRequest(ruleSetRequest, currentUser));
    }

    @Test
    void prepareRuleInformationFromRuleSetRequestTestWithWrongScanEngineNameFail3() {
        log.info("[prepareRuleInformationFromRuleSetRequestTestFail3]");
        RuleSetRequest.RuleSet.Rule rule1 = RuleSetRequest.RuleSet.Rule.builder().name("ruleName1").description("rule desc1")
                .category("rule category1").code("code1").detail("detail1").language("C++").severity(RuleInformation.Severity.HIGH.name()).likelihood(RuleInformation.Likelihood.LIKELY.name()).build();
        List<RuleSetRequest.RuleSet.Rule> ruleList = Collections.singletonList(rule1);
        RuleSetRequest.RuleSet ruleSet = RuleSetRequest.RuleSet.builder().name(ruleSetName).version(ruleSetVersion).
                language(ruleSetLanguage).provider(ruleSetProvider).providerUrl(ruleSetProvideUrl).rules(ruleList).build();
        List<RuleSetRequest.RuleSet> ruleSetList = Collections.singletonList(ruleSet);
        RuleSetRequest ruleSetRequest = RuleSetRequest.builder().name(incorrectRuleSetRequestName).description(ruleSetRequestDescription).engineUrl(ruleSetRequestEngineUrl)
                .language(ruleSetRequestLanguage).license(ruleSetRequestLicense).licenseUrl(ruleSetRequestLicenseUrl).provider(ruleSetRequestProvider).providerUrl(ruleSetRequestProviderUrl).version(ruleSetRequestVersion).ruleSets(ruleSetList).build();
        when(ruleSetRepository.findByNameAndVersion(any(), any())).thenReturn(Optional.empty());
        when(scanEngineRepository.findByNameAndVersion(ruleSetRequest.getName(), ruleSetRequest.getVersion())).thenReturn(Optional.of(scanEngine));
        assertThrows(AppException.class, () -> ruleService.prepareRuleInformationFromRuleSetRequest(ruleSetRequest, currentUser));
    }

    @Test
    void prepareRuleInformationFromRuleSetRequestTestWithoutCategoryFail4() {
        log.info("[prepareRuleInformationFromRuleSetRequestTestFail5]");
        RuleSetRequest.RuleSet.Rule rule1 = RuleSetRequest.RuleSet.Rule.builder().name("ruleName1").description("rule desc1")
                .code("code1").detail("detail1").language("C++").severity(RuleInformation.Severity.HIGH.name()).
                        likelihood(RuleInformation.Likelihood.LIKELY.name())
                .fixCost(RuleInformation.RemediationCost.HIGH.name())
                .priority(RuleInformation.Priority.HIGH.name())
                .build();
        List<RuleSetRequest.RuleSet.Rule> ruleList = Collections.singletonList(rule1);
        RuleSetRequest.RuleSet ruleSet = RuleSetRequest.RuleSet.builder().name(ruleSetName).version(ruleSetVersion).
                language(ruleSetLanguage).provider(ruleSetProvider).providerUrl(ruleSetProvideUrl).rules(ruleList).build();
        List<RuleSetRequest.RuleSet> ruleSetList = Collections.singletonList(ruleSet);
        RuleSetRequest ruleSetRequest = RuleSetRequest.builder().name(incorrectRuleSetRequestName).description(ruleSetRequestDescription).engineUrl(ruleSetRequestEngineUrl)
                .language(ruleSetRequestLanguage).license(ruleSetRequestLicense).licenseUrl(ruleSetRequestLicenseUrl).provider(ruleSetRequestProvider).providerUrl(ruleSetRequestProviderUrl).version(ruleSetRequestVersion).ruleSets(ruleSetList).build();
        when(ruleSetRepository.findByNameAndVersion(any(), any())).thenReturn(Optional.empty());
        when(scanEngineRepository.findByNameAndVersion(ruleSetRequest.getName(), ruleSetRequest.getVersion())).thenReturn(Optional.of(scanEngine));
        assertThrows(AppException.class, () -> ruleService.prepareRuleInformationFromRuleSetRequest(ruleSetRequest, currentUser));
    }

    @Test
    void getRuleCodeTestSuccess() {
        log.info("[getRuleCodeTestSuccess]");
        String resultRuleCode = RuleService.getRuleCodeKey(ruleInformation);
        assertEquals(ruleInformation.getRuleSet().getScanEngine().getName() + "-" + ruleInformation.getRuleSet().getName() + "-" + ruleInformation.getRuleCode(), resultRuleCode);
    }

    @Test
    void getRuleCodeTestSuccess1() {
        log.info("[getRuleCodeTestSuccess1]");
        String resultRuleCode = RuleService.getRuleCodeKey(scanEngineName, ruleSetName, vulnerable);
        assertEquals(scanEngineName + "-" + ruleSetName + "-" + vulnerable, resultRuleCode);
    }

    @Test
    void findRuleSetTest() {
        when(ruleSetRepository.findAll(any(Pageable.class))).thenReturn(new RestResponsePage<>(Collections.singletonList(ruleSet)));
        Page<RuleSet> ruleSets = this.ruleService.findRuleSet(PageRequest.of(10, 1));
        assertTrue(ruleSets.get().findFirst().isPresent());
        assertSame(ruleSets.getContent().get(0).getId(), ruleSet.getId());
    }

    @Test
    void getI18nMessagesByRuleSet_InputRuleSetAndLocal_ReturnI18nMessageList() {
        log.info("[getI18nMessagesByRuleSet_InputRuleSetAndLocal_ReturnI18nMessageList]");
        when(ruleInformationRepository.findByRuleSet(ruleSet)).thenReturn(Collections.singletonList(ruleInformation));
        when(i18nService.getI18nMessageByKeyPrefix(any(), any(Locale.class))).thenReturn((Collections.singletonList(enTest)));
        List<I18nMessage> i18nMessagesList = ruleService.getI18nMessagesByRuleSet(ruleSet, Locale.ENGLISH);
        assertEquals(1, i18nMessagesList.size());
        assertEquals(enTest.getContent(), i18nMessagesList.get(0).getContent());
        assertEquals(enTest.getCreatedBy(), i18nMessagesList.get(0).getCreatedBy());
        assertEquals(enTest.getCreatedOn(), i18nMessagesList.get(0).getCreatedOn());
        assertEquals(enTest.getModifiedOn(), i18nMessagesList.get(0).getModifiedOn());
        assertEquals(enTest.getModifiedBy(), i18nMessagesList.get(0).getCreatedBy());
        assertEquals(enTest.getKey(), i18nMessagesList.get(0).getKey());
        assertEquals(enTest.getLocale(), i18nMessagesList.get(0).getLocale());
        assertEquals(enTest.getId(), i18nMessagesList.get(0).getId());
    }

    @Test
    void convertRuleInformationListToDto_InputRuleInformationListAndLocal_ReturnI18nMessageList() {
        log.info("[convertRuleInformationListToDto_InputRuleInformationListAndLocal_ReturnI18nMessageList]");
        when(i18nService.getI18nMessagesByKeys(any(), any(Locale.class))).thenReturn((Collections.singletonList(enTest)));
        Map<String, I18nMessage> i18nMessageMap = ruleService.retrieveI18nMessageMapByRuleInformationList(Collections.singletonList(ruleInformation), Locale.ENGLISH);
        assertEquals(1, i18nMessageMap.size());
        I18nMessage resultI18nMessage = i18nMessageMap.get(enTest.getKey());
        assertEquals(enTest.getContent(), resultI18nMessage.getContent());
        assertEquals(enTest.getCreatedBy(), resultI18nMessage.getCreatedBy());
        assertEquals(enTest.getCreatedOn(), resultI18nMessage.getCreatedOn());
        assertEquals(enTest.getModifiedOn(), resultI18nMessage.getModifiedOn());
        assertEquals(enTest.getModifiedBy(), resultI18nMessage.getCreatedBy());
        assertEquals(enTest.getKey(), resultI18nMessage.getKey());
        assertEquals(enTest.getLocale(), resultI18nMessage.getLocale());
        assertEquals(enTest.getId(), resultI18nMessage.getId());
    }
}
