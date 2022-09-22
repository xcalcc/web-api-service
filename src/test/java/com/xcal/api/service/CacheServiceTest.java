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

import com.xcal.api.entity.I18nMessage;
import com.xcal.api.entity.RuleInformation;
import com.xcal.api.entity.RuleSet;
import com.xcal.api.entity.ScanEngine;
import com.xcal.api.model.payload.RestResponsePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class CacheServiceTest {

    private RuleService ruleService;
    private CacheService cacheService;

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

    @BeforeEach
    void setUp() {
        ruleService = mock(RuleService.class);
        cacheService = new CacheService(ruleService);
    }

    @Test
    void initCacheRuleInformation_Success() {
        log.info("[initCacheRuleInformation_Success]");
        Page<RuleSet> expectedResult = new RestResponsePage<>(Collections.singletonList(ruleSet));
        when(ruleService.findRuleSet(any())).thenReturn((expectedResult));
        when(ruleService.findByRuleSet(ruleSet)).thenReturn(Collections.singletonList(ruleInformation));
        when(ruleService.getI18nMessagesByRuleSet(ruleSet, Locale.SIMPLIFIED_CHINESE)).thenReturn(Collections.singletonList(I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.SIMPLIFIED_CHINESE.toLanguageTag()).key("test.1").content("测试一").build()));
        when(ruleService.getI18nMessagesByRuleSet(ruleSet, Locale.TRADITIONAL_CHINESE)).thenReturn(Collections.singletonList(I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.TRADITIONAL_CHINESE.toLanguageTag()).key("test.1").content("測試一").build()));
        when(ruleService.getI18nMessagesByRuleSet(ruleSet, Locale.ENGLISH)).thenReturn(Collections.singletonList(I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.ENGLISH.toLanguageTag()).key("test.1").content("Test 1").build()));
        assertDoesNotThrow(() -> cacheService.initCacheRuleInformation());
    }
}
