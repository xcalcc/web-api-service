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
import com.xcal.api.entity.RuleStandard;
import com.xcal.api.entity.RuleStandardAttribute;
import com.xcal.api.entity.RuleStandardSet;
import com.xcal.api.model.dto.RuleStandardDto;
import com.xcal.api.repository.RuleStandardRepository;
import com.xcal.api.repository.RuleStandardSetRepository;
import com.xcal.api.util.CommonUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RuleStandardService {

    @NonNull RuleStandardRepository ruleStandardRepository;
    @NonNull RuleStandardSetRepository ruleStandardSetRepository;
    @NonNull UserService userService;
    @NonNull I18nService i18nService;

    public Optional<RuleStandardSet> findRuleStandardSetByNameAndVersion(String ruleStandardSetName, String ruleStandardSetVersion) {
        Optional<RuleStandardSet> result;
        log.debug("[findRuleStandardSetByNameAndVersion] ruleStandardSetName: {}, ruleStandardSetVersion: {}", ruleStandardSetName, ruleStandardSetVersion);
        if (StringUtils.isNotBlank(ruleStandardSetVersion)) {
            result = this.ruleStandardSetRepository.findByNameAndVersion(ruleStandardSetName, ruleStandardSetVersion);
        } else {
            result = this.ruleStandardSetRepository.findByName(ruleStandardSetName);
        }
        return result;
    }

    public Optional<RuleStandardSet> findRuleStandardSetById(UUID id) {
        log.debug("[findRuleStandardSetById] id: {}", id);
        return this.ruleStandardSetRepository.findById(id);
    }

    public Page<RuleStandardSet> findAllRuleStandardSet(Pageable pageable) {
        log.debug("[findAllRuleStandardSet] pageable: {}", pageable);
        return this.ruleStandardSetRepository.findAll(pageable);
    }

    public List<RuleStandard> findByRuleStandardSet(RuleStandardSet ruleStandardSet) {
        log.debug("[findByRuleStandardSet] ruleStandardSet, id: {}", ruleStandardSet.getId());
        return this.ruleStandardRepository.findByRuleStandardSet(ruleStandardSet);
    }

    public Page<RuleStandard> findAllRuleStandard(Pageable pageable) {
        log.debug("[findAllRuleStandard] pageable: {}", pageable);
        return this.ruleStandardRepository.findAll(pageable);
    }

    @Cacheable(cacheNames = {"ruleStandardDto"}, key = "'ruleStandardSet#' + #ruleStandardSet.id + '#'+ #locale")
    public List<RuleStandardDto> findDtoByRuleStandardSet(RuleStandardSet ruleStandardSet, Locale locale) {
        log.debug("[findDtoByRuleStandardSet] ruleStandardSet, id: {}", ruleStandardSet.getId());
        List<RuleStandard> ruleStandardList = this.findByRuleStandardSet(ruleStandardSet);
        List<I18nMessage> i18nMessages = this.getI18nMessagesByRuleStandardSet(ruleStandardSet, locale);
        Map<String, I18nMessage> i18nMessageMap = i18nMessages.stream().collect(Collectors.toMap(I18nMessage::getKey, message -> message));
        List<RuleStandardDto> RuleStandardDtoList = ruleStandardList.stream().map(ruleStandard -> this.convertRuleStandardToDto(ruleStandard, i18nMessageMap)).collect(Collectors.toList());
        log.trace("[findDtoByRuleStandardSet] RuleStandardDtoList size: {}", RuleStandardDtoList.size());
        return RuleStandardDtoList;
    }

    public Optional<RuleStandard> findByRuleStandardSetAndCode(String ruleStandardSetName, String code) {
        log.debug("[findByRuleStandardSetAndCode] ruleStandardSetName: {}, code: {}", ruleStandardSetName, code);
        Optional<RuleStandard> result;
        Optional<RuleStandardSet> ruleStandardSetOptional = this.findRuleStandardSetByNameAndVersion(ruleStandardSetName, null);
        if (ruleStandardSetOptional.isPresent()) {
            result = this.ruleStandardRepository.findByRuleStandardSetAndCode(ruleStandardSetOptional.get(), code);
        } else {
            result = Optional.empty();
        }
        return result;
    }

    public Optional<RuleStandard> findById(UUID id) {
        log.debug("[findById] id: {}", id);
        return this.ruleStandardRepository.findById(id);
    }

    public List<RuleStandard> findByIds(List<UUID> ids) {
        log.debug("[findById] ids size: {}", ids.size());
        return this.ruleStandardRepository.findByIdIn(ids);
    }

    public List<RuleStandard> findByRuleStandardSetNameAndVersion(String ruleStandardSetName, String ruleStandardSetVersion) {
        List<RuleStandard> result;
        log.debug("[findByRuleStandardSetNameAndVersion] ruleStandardSetName: {}, ruleStandardSetVersion: {}", ruleStandardSetName, ruleStandardSetVersion);
        if (StringUtils.isNotBlank(ruleStandardSetVersion)) {
            result = this.ruleStandardRepository.findByRuleStandardSetNameAndRuleStandardSetVersion(ruleStandardSetName, ruleStandardSetVersion);
        } else {
            result = this.ruleStandardRepository.findByRuleStandardSetName(ruleStandardSetName);
        }
        return result;
    }

    public RuleStandardDto convertRuleStandardToDto(RuleStandard ruleStandard, Locale locale) {
        log.debug("[convertRuleStandardToDto] ruleStandard code: {}, locale: {}", ruleStandard.getCode(), locale);

        Map<String, I18nMessage> i18nMessageMap = this.retrieveI18nMessageMapByRuleStandardList(Collections.singletonList(ruleStandard), locale);
        return this.convertRuleStandardToDto(ruleStandard, i18nMessageMap);
    }

    public List<RuleStandardDto> convertRuleStandardListToDto(List<RuleStandard> ruleStandardList, Locale locale) {
        Map<String, I18nMessage> i18nMessageMap = this.retrieveI18nMessageMapByRuleStandardList(ruleStandardList, locale);
        return ruleStandardList.stream().map(ruleStandard -> this.convertRuleStandardToDto(ruleStandard, i18nMessageMap)).collect(Collectors.toList());
    }

    public Map<String, I18nMessage> retrieveI18nMessageMapByRuleStandardList(List<RuleStandard> ruleStandardList, Locale locale) {
        log.debug("[retrieveI18nMessageMapByRuleStandardList] ruleStandardList size: {}, locale: {}", ruleStandardList.size(), locale);
        List<I18nMessage> i18nMessages = this.getI18nMessagesByRuleStandardList(ruleStandardList, locale);
        return i18nMessages.stream().collect(Collectors.toMap(I18nMessage::getKey, message -> message));
    }

    private List<I18nMessage> getI18nMessagesByRuleStandardList(List<RuleStandard> ruleStandardList, Locale locale) {
        log.debug("[getI18nMessagesByRuleStandardList] ruleStandardList size: {}, locale: {}", ruleStandardList.size(), locale);
        List<String> keyList = new ArrayList<>();
        List<String> suffixList = ruleStandardList.stream().map(ruleStandard -> CommonUtil.formatString("{}.{}.{}",
                ruleStandard.getRuleStandardSet().getName(),
                ruleStandard.getRuleStandardSet().getVersion(),
                ruleStandard.getCode())).distinct().collect(Collectors.toList());
        keyList.addAll(suffixList.stream().map(suffix -> "rule.standard." + suffix + ".name").collect(Collectors.toList()));
        keyList.addAll(suffixList.stream().map(suffix -> "rule.standard." + suffix + ".description").collect(Collectors.toList()));
        keyList.addAll(suffixList.stream().map(suffix -> "rule.standard." + suffix + ".detail").collect(Collectors.toList()));
        keyList.addAll(suffixList.stream().map(suffix -> "rule.standard." + suffix + ".msg_template").collect(Collectors.toList()));
        return i18nService.getI18nMessagesByKeys(keyList, locale);
    }

    @Cacheable(cacheNames = {"i18nMessageOfRuleStandardSet"}, key = "'RuleStandardSet#' + #RuleStandardSet.id + '#'+ #locale")
    public List<I18nMessage> getI18nMessagesByRuleStandardSet(RuleStandardSet RuleStandardSet, Locale locale) {
        log.debug("[getI18nMessagesByRuleStandardSet] RuleStandardSet id: {}, RuleStandardSet name: {}, locale: {}", RuleStandardSet.getId(), RuleStandardSet.getName(), locale);
        String prefix = CommonUtil.formatString("rule.standard.{}.{}",
                RuleStandardSet.getName(),
                RuleStandardSet.getVersion());
        return i18nService.getI18nMessageByKeyPrefix(prefix, locale);
    }

    public RuleStandardDto convertRuleStandardToDto(RuleStandard ruleStandard, Map<String, I18nMessage> i18nMessageMap) {
        log.trace("[convertRuleStandardToDto] ruleStandard: {}", ruleStandard);
        String ruleStandardName = I18nService.formatString(ruleStandard.getName(), null, i18nMessageMap);
        String ruleStandardDescription = I18nService.formatString(ruleStandard.getDescription(), null, i18nMessageMap);
        String ruleStandardDetail = I18nService.formatString(ruleStandard.getDetail(), null, i18nMessageMap);
        String messageTemplate = I18nService.formatString(ruleStandard.getMessageTemplate(), null, i18nMessageMap, 1);
        RuleStandardDto result = RuleStandardDto.builder()
                .id(ruleStandard.getId())
                .ruleStandardSetId(ruleStandard.getRuleStandardSet().getId())
                .ruleStandardSet(ruleStandard.getRuleStandardSet().getName())
                .ruleStandardSetVersion(ruleStandard.getRuleStandardSet().getVersion())
                .ruleStandardSetRevision(ruleStandard.getRuleStandardSet().getRevision())
                .ruleStandardSetDisplayName(ruleStandard.getRuleStandardSet().getDisplayName())
                .category(ruleStandard.getCategory())
                .code(ruleStandard.getCode())
                .language(ruleStandard.getLanguage())
                .url(ruleStandard.getUrl())
                .name(ruleStandardName)
                .detail(ruleStandardDetail)
                .description(ruleStandardDescription)
                .messageTemplate(messageTemplate)
                .build();

        for (RuleStandardAttribute attribute : ruleStandard.getAttributes()) {
            RuleStandardDto.Attribute attr = RuleStandardDto.Attribute.builder().id(attribute.getId()).type(attribute.getType().name()).name(attribute.getName()).value(attribute.getValue()).build();
            result.getAttributes().add(attr);
        }
        log.trace("[convertRuleStandardToDto] result: {}", result);
        return result;
    }
}
