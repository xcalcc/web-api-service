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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@EnableScheduling
public class CacheService {

    @NonNull RuleService ruleService;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
    public void initCacheRuleInformation() {
        log.debug("[initCacheRuleInformation]");
        Page<RuleSet> ruleSetPage = ruleService.findRuleSet(Pageable.unpaged());
        List<RuleSet> ruleSets = ruleSetPage.getContent();
        ruleSets.forEach(ruleSet -> {
            List<RuleInformation> ruleInformationList = ruleService.findByRuleSet(ruleSet);
            log.debug("[initCacheRuleInformation] initCache of RuleInformation for RuleSet, RuleSet Id :{}, ruleInformation list size: {}", ruleSet.getId(), ruleInformationList.size());
            List<I18nMessage> i18nMessageListZHCN = ruleService.getI18nMessagesByRuleSet(ruleSet, Locale.SIMPLIFIED_CHINESE);
            log.debug("[initCacheRuleInformation] initCache of zh_CN i18nMessage for RuleSet, RuleSet Id :{}, i18nMessage list size: {}", ruleSet.getId(), i18nMessageListZHCN.size());
            List<I18nMessage> i18nMessageListZHTW = ruleService.getI18nMessagesByRuleSet(ruleSet, Locale.TRADITIONAL_CHINESE);
            log.debug("[initCacheRuleInformation] initCache of zh_TW i18nMessage for RuleSet, RuleSet Id :{}, i18nMessage list size: {}", ruleSet.getId(), i18nMessageListZHTW.size());
            List<I18nMessage> i18nMessageListEN = ruleService.getI18nMessagesByRuleSet(ruleSet, Locale.ENGLISH);
            log.debug("[initCacheRuleInformation] initCache of en i18nMessage for RuleSet, RuleSet Id :{}, i18nMessage list size: {}", ruleSet.getId(), i18nMessageListEN.size());
        });
    }
}
