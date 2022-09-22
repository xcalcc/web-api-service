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
import com.xcal.api.model.payload.ImportScanResultRequest;
import com.xcal.api.model.payload.RuleSetRequest;
import com.xcal.api.repository.RuleInformationRepository;
import com.xcal.api.repository.RuleSetRepository;
import com.xcal.api.repository.ScanEngineRepository;
import com.xcal.api.util.CommonUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RuleService {

    @NonNull RuleInformationRepository ruleInformationRepository;
    @NonNull RuleSetRepository ruleSetRepository;
    @NonNull ScanEngineRepository scanEngineRepository;
    @NonNull UserService userService;
    @NonNull I18nService i18nService;

    public List<RuleInformation> findByRuleSet(RuleSet ruleSet) {
        log.debug("[getByRuleSet] ruleSet, id: {}", ruleSet.getId());
        return this.ruleInformationRepository.findByRuleSet(ruleSet);
    }

    @Cacheable(cacheNames = {"ruleInformationDto"}, key = "'ruleSet#' + #ruleSet.id + '#'+ #locale")
    public List<RuleInformationDto> findDtoByRuleSet(RuleSet ruleSet, Locale locale) {
        log.debug("[findDtoByRuleSet] ruleSet, id: {}", ruleSet.getId());
        List<RuleInformation> ruleInformationList = this.ruleInformationRepository.findByRuleSet(ruleSet);
        List<I18nMessage> i18nMessages = this.getI18nMessagesByRuleSet(ruleSet, locale);
        Map<String, I18nMessage> i18nMessageMap = i18nMessages.stream().collect(Collectors.toMap(I18nMessage::getKey, message -> message));
        List<RuleInformationDto> ruleInformationDtoList = ruleInformationList.stream().map(ruleInformation -> this.convertRuleInformationToDto(ruleInformation, i18nMessageMap)).collect(Collectors.toList());
        log.trace("[findDtoByRuleSet] ruleInformationDtoList size: {}", ruleInformationDtoList.size());
        return ruleInformationDtoList;
    }

    public Optional<RuleInformation> getByRuleCode(String engineName, String ruleSetName, String ruleCode) {
        log.debug("[getByRuleCode] engineName :{}, ruleSetName: {}, ruleCode: {}", engineName, ruleSetName, ruleCode);
        return this.ruleInformationRepository.findOneByRuleSetScanEngineNameAndRuleSetNameAndRuleCode(engineName, ruleSetName, ruleCode);
    }
    public List<RuleInformation> findByRuleInformationAttribute(List<RuleSet> ruleSet, List<RuleInformationAttribute> existAttributes, List<RuleInformationAttribute> equalAttributes){
        log.debug("[getByRuleCode] engineName :{}, ruleSetName: {}, ruleCode: {}", ruleSet.size(), existAttributes.size(), equalAttributes.size());
        return this.ruleInformationRepository.findByAttribute(ruleSet, existAttributes, equalAttributes);
    }

    public Optional<RuleInformation> findById(UUID id) {
        log.debug("[findById] id: {}", id);
        return this.ruleInformationRepository.findById(id);
    }

    public List<RuleInformation> findAll() {
        log.debug("[findAll]");
        return this.ruleInformationRepository.findAll();
    }

    public Page<RuleInformation> findAll(Pageable pageable) {
        log.debug("[findAll] pageable: {}", pageable);
        return this.ruleInformationRepository.findAll(pageable);
    }

    public List<RuleInformation> findByIds(List<UUID> ids) {
        log.debug("[findById] ids size: {}", ids.size());
        return this.ruleInformationRepository.findByIdIn(ids);
    }

    public List<RuleInformation> getRuleInformation(String scanEngineName, String scanEngineVersion, String ruleSetName, String ruleSetVersion) {
        List<RuleInformation> result;
        log.debug("[getRuleInformation] scanEngineName: {}, scanEngineVersion: {}, ruleSetName: {}, ruleSetVersion: {}", scanEngineName, scanEngineVersion, ruleSetName, ruleSetVersion);
        if (StringUtils.isNotBlank(scanEngineName)) {
            if (StringUtils.isNotBlank(scanEngineVersion)) {
                if (StringUtils.isBlank(ruleSetName)) {
                    result = this.ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetScanEngineVersion(scanEngineName, scanEngineVersion);
                } else {
                    result = this.ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(scanEngineName, scanEngineVersion, ruleSetName, ruleSetVersion);
                }
            } else {
                result = this.ruleInformationRepository.findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(scanEngineName, ruleSetName, ruleSetVersion);
            }
        } else {
            if (StringUtils.isNotBlank(ruleSetVersion)) {
                result = this.ruleInformationRepository.findByRuleSetNameAndRuleSetVersion(ruleSetName, ruleSetVersion);
            } else {
                result = this.ruleInformationRepository.findByRuleSetName(ruleSetName);
            }
        }
        return result;
    }

    public Optional<RuleSet> getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersion(String scanEngineName, String scanEngineVersion, String name, String version) {
        log.debug("[getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersion] scanEngineName: {}, scanEngineVersion: {}, name: {}, version: {}", scanEngineName, scanEngineVersion, name, version);
        Optional<RuleSet> ruleSetOptional = this.ruleSetRepository.findByScanEngineNameAndScanEngineVersionAndNameAndVersion(scanEngineName, scanEngineVersion, name, version);
        if (!ruleSetOptional.isPresent() && StringUtils.contains(scanEngineVersion, "-")) {
            scanEngineVersion = StringUtils.substringBefore(scanEngineVersion, "-");
            ruleSetOptional = getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersion(scanEngineName, scanEngineVersion, name, version);
        }
        return ruleSetOptional;
    }

    public Optional<RuleSet> getRuleSetByNameAndVersion(String name, String version) {
        log.debug("[getRuleSetByNameAndVersion] name: {}, version: {}", name, version);
        return this.ruleSetRepository.findByNameAndVersion(name, version);
    }

    public Optional<RuleSet> findRuleSetById(UUID id) {
        log.debug("[findRuleSetById] id: {}", id);
        return this.ruleSetRepository.findById(id);
    }

    public Page<RuleSet> findRuleSet(Pageable pageable) {
        log.debug("[findRuleSet] pageable: {}", pageable);
        return this.ruleSetRepository.findAll(pageable);
    }

    public RuleInformationDto convertRuleInformationToDto(RuleInformation ruleInformation, Locale locale) {
        log.debug("[convertRuleInformationToDto] ruleInformation code: {}, locale: {}", ruleInformation.getRuleCode(), locale);

        Map<String, I18nMessage> i18nMessageMap = this.retrieveI18nMessageMapByRuleInformationList(Collections.singletonList(ruleInformation), locale);
        return this.convertRuleInformationToDto(ruleInformation, i18nMessageMap);
    }
    public List<RuleInformationDto> convertRuleInformationListToDto(List<RuleInformation> ruleInformationList, Locale locale) {
        Map<String, I18nMessage> i18nMessageMap = this.retrieveI18nMessageMapByRuleInformationList(ruleInformationList, locale);
        return ruleInformationList.stream().map(ruleInformation -> this.convertRuleInformationToDto(ruleInformation, i18nMessageMap)).collect(Collectors.toList());
    }

    public Map<String, I18nMessage> retrieveI18nMessageMapByRuleInformationList(List<RuleInformation> ruleInformationList, Locale locale){
        log.debug("[retrieveI18nMessageMapByRuleInformationList] ruleInformationList size: {}, locale: {}", ruleInformationList.size(), locale);
        List<I18nMessage> i18nMessages = this.getI18nMessagesByRuleInformationList(ruleInformationList, locale);
        return i18nMessages.stream().collect(Collectors.toMap(I18nMessage::getKey, message -> message));
    }

    List<I18nMessage> getI18nMessagesByRuleInformationList(List<RuleInformation> ruleInformationList, Locale locale) {
        log.debug("[getI18nMessagesByRuleInformationList] ruleInformationList size: {}, locale: {}", ruleInformationList.size(), locale);
        List<String> keyList = new ArrayList<>();
        List<String> suffixList = ruleInformationList.stream().map(ruleInformation -> CommonUtil.formatString("{}.{}.{}.{}",
                ruleInformation.getRuleSet().getScanEngine().getName(),
                ruleInformation.getRuleSet().getName(),
                ruleInformation.getRuleSet().getVersion(),
                ruleInformation.getRuleCode())).distinct().collect(Collectors.toList());
        keyList.addAll(suffixList.stream().map(suffix -> "rule." + suffix + ".name").collect(Collectors.toList()));
        keyList.addAll(suffixList.stream().map(suffix -> "rule." + suffix + ".description").collect(Collectors.toList()));
        keyList.addAll(suffixList.stream().map(suffix -> "rule." + suffix + ".detail").collect(Collectors.toList()));
        keyList.addAll(suffixList.stream().map(suffix -> "rule." + suffix + ".msg_template").collect(Collectors.toList()));
        return i18nService.getI18nMessagesByKeys(keyList, locale);
    }

    @Cacheable(cacheNames = {"i18nMessageOfRuleSet"}, key = "'ruleSet#' + #ruleSet.id + '#'+ #locale")
    public List<I18nMessage> getI18nMessagesByRuleSet(RuleSet ruleSet, Locale locale) {
        log.debug("[getI18nMessagesByRuleSet] ruleSet id: {}, ruleSet name: {}, locale: {}", ruleSet.getId(), ruleSet.getName(), locale);
        String prefix = CommonUtil.formatString("rule.{}.{}.{}",
                ruleSet.getScanEngine().getName(),
                ruleSet.getName(),
                ruleSet.getVersion());
        return i18nService.getI18nMessageByKeyPrefix(prefix, locale);
    }

    public RuleInformationDto convertRuleInformationToDto(RuleInformation ruleInformation, Map<String, I18nMessage> i18nMessageMap) {
        log.trace("[convertRuleInformationToDto] ruleInformation: {}", ruleInformation);
        String ruleInformationName =  I18nService.formatString(ruleInformation.getName(), null, i18nMessageMap);
        String ruleInformationDescription = I18nService.formatString(ruleInformation.getDescription(), null, i18nMessageMap);
        String ruleInformationDetail = I18nService.formatString(ruleInformation.getDetail(), null, i18nMessageMap);
        String messageTemplate = I18nService.formatString(ruleInformation.getMessageTemplate(), null, i18nMessageMap, 1);
        RuleInformationDto result = RuleInformationDto.builder()
                .id(ruleInformation.getId())
                .ruleSetId(ruleInformation.getRuleSet().getId())
                .ruleSet(ruleInformation.getRuleSet().getName())
                .ruleSetVersion(ruleInformation.getRuleSet().getVersion())
                .ruleSetRevision(ruleInformation.getRuleSet().getRevision())
                .ruleSetDisplayName(ruleInformation.getRuleSet().getDisplayName())
                .category(ruleInformation.getCategory())
                .vulnerable(ruleInformation.getVulnerable())
                .certainty(ruleInformation.getCertainty() != null ? ruleInformation.getCertainty().name() : null)
                .ruleCode(ruleInformation.getRuleCode())
                .language(ruleInformation.getLanguage())
                .url(ruleInformation.getUrl())
                .name(ruleInformationName)
                .severity(ruleInformation.getSeverity() != null ? ruleInformation.getSeverity().name() : null)
                .priority(ruleInformation.getPriority() != null ? ruleInformation.getPriority().name() : null)
                .likelihood(ruleInformation.getLikelihood() != null ? ruleInformation.getLikelihood().name() : null)
                .remediationCost(ruleInformation.getRemediationCost() != null ? ruleInformation.getRemediationCost().name() : null)
                .detail(ruleInformationDetail)
                .description(ruleInformationDescription)
                .messageTemplate(messageTemplate)
                .build();

        for (RuleInformationAttribute attribute : ruleInformation.getAttributes()) {
            RuleInformationDto.Attribute attr = RuleInformationDto.Attribute.builder().id(attribute.getId()).type(attribute.getType().name()).name(attribute.getName()).value(attribute.getValue()).build();
            result.getAttributes().add(attr);
        }
        log.trace("[convertRuleInformationToDto] result: {}", result);
        return result;
    }

    @CacheEvict(cacheNames = {"ruleInformationDto"}, allEntries = true)
    public List<RuleInformation> addRuleSets(RuleSetRequest ruleSetRequest, User user) throws AppException {
        log.debug("[addRuleSets] ruleSetRequest, name: {}, version: {}", ruleSetRequest.getName(), ruleSetRequest.getVersion());
        List<RuleInformation> ruleInformationList = new ArrayList<>();
        List<RuleInformation> ruleInfos = this.prepareRuleInformationFromRuleSetRequest(ruleSetRequest, user);
        log.info("[addRuleSets] prepare to save ruleInfo, size: {}", ruleInfos.size());
        if (!ruleInfos.isEmpty()) {
            ruleInformationList.addAll(this.saveRuleInformation(ruleInfos));
        }
        return ruleInformationList;
    }

    public List<RuleInformation> prepareRuleInformationFromRuleSetRequest(RuleSetRequest ruleSetRequest, User user) throws AppException {
        log.info("[prepareRuleInformationFromRuleSetRequest] ruleSetRequest, name: {}, version: {}", ruleSetRequest.getName(), ruleSetRequest.getVersion());
        Date now = new Date();
        List<RuleInformation> ruleInfos = new ArrayList<>();

        Optional<ScanEngine> scanEngineOptional = this.scanEngineRepository.findByNameAndVersion(ruleSetRequest.getName(), ruleSetRequest.getVersion());
        ScanEngine scanEngine = scanEngineOptional.orElse(ScanEngine.builder()
                .name(ruleSetRequest.getName())
                .version(ruleSetRequest.getVersion())
                .revision(ruleSetRequest.getRevision())
                .description(ruleSetRequest.getDescription())
                .language(ruleSetRequest.getLanguage())
                .url(ruleSetRequest.getEngineUrl())
                .provider(ruleSetRequest.getProvider())
                .providerUrl(ruleSetRequest.getProviderUrl())
                .license(ruleSetRequest.getLicense())
                .licenseUrl(ruleSetRequest.getLicenseUrl())
                .createdBy(user.getUsername())
                .createdOn(now)
                .modifiedBy(user.getUsername())
                .modifiedOn(now)
                .build());

        // only update the engine information if the revision is alphabetic order larger than the revision in database
        if(scanEngineOptional.isPresent() && (StringUtils.compareIgnoreCase(scanEngine.getRevision(), ruleSetRequest.getRevision()) < 0)) {
            scanEngine.setDescription(ruleSetRequest.getDescription());
            scanEngine.setLanguage(ruleSetRequest.getLanguage());
            scanEngine.setRevision(ruleSetRequest.getRevision());
            scanEngine.setUrl(ruleSetRequest.getEngineUrl());
            scanEngine.setProvider(ruleSetRequest.getProvider());
            scanEngine.setProviderUrl(ruleSetRequest.getProviderUrl());
            scanEngine.setLicense(ruleSetRequest.getLicense());
            scanEngine.setLicenseUrl(ruleSetRequest.getLicenseUrl());
            scanEngine.setModifiedBy(user.getUsername());
            scanEngine.setModifiedOn(now);
        }

        for (RuleSetRequest.RuleSet rs : ruleSetRequest.getRuleSets()) {
            Optional<RuleSet> ruleSetOptional = this.getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersion(scanEngine.getName(), scanEngine.getVersion(), rs.getName(), rs.getVersion());
            RuleSet ruleSet;
            Map<String, RuleInformation> existingRuleMap = new HashMap<>();
            if (ruleSetOptional.isPresent() ){
                ruleSet = ruleSetOptional.get();
                // 2020/12/03 Don't check the revision as the UI needs to consume this api to update rule set
                // compare the revision, if the revision is smaller or equal to the revision in database in alphabetic order, throw exception
                // if (StringUtils.compareIgnoreCase(ruleSet.getRevision(), rs.getRevision()) >= 0) {
                // throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_ALREADY_EXIST, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_RULE_COMMON_ALREADY_EXIST.unifyErrorCode,
                // CommonUtil.formatString("[{}] name: {}, version: {}, revision: {}", AppException.ErrorCode.E_API_RULE_COMMON_ALREADY_EXIST.messageTemplate, ruleSet.getName(), ruleSet.getVersion(), ruleSet.getRevision()));
                // }else{
                existingRuleMap.putAll(this.findByRuleSet(ruleSet).stream().collect(Collectors.toMap(RuleInformation::getRuleCode, ruleInformation -> ruleInformation)));
                ruleSet.setName(rs.getName());
                ruleSet.setRevision(rs.getRevision());
                ruleSet.setDisplayName(rs.getDisplayName());
                ruleSet.setDescription(rs.getDescription());
                ruleSet.setLanguage(rs.getLanguage());
                ruleSet.setUrl(rs.getRuleSetUrl());
                ruleSet.setProvider(rs.getProvider());
                ruleSet.setProviderUrl(rs.getProviderUrl());
                ruleSet.setLicense(rs.getLicense());
                ruleSet.setLicenseUrl(rs.getLicenseUrl());
                ruleSet.setModifiedBy(user.getUsername());
                ruleSet.setModifiedOn(now);
                // }
            }else{
                ruleSet = RuleSet.builder()
                        .scanEngine(scanEngine)
                        .name(rs.getName())
                        .version(rs.getVersion())
                        .revision(rs.getRevision())
                        .displayName(rs.getDisplayName())
                        .description(rs.getDescription())
                        .language(rs.getLanguage())
                        .url(rs.getRuleSetUrl())
                        .provider(rs.getProvider())
                        .providerUrl(rs.getProviderUrl())
                        .license(rs.getLicense())
                        .licenseUrl(rs.getLicenseUrl())
                        .createdBy(user.getUsername())
                        .createdOn(now)
                        .modifiedBy(user.getUsername())
                        .modifiedOn(now)
                        .build();
            }

            for (RuleSetRequest.RuleSet.Rule rule : rs.getRules()) {
                if(StringUtils.isBlank(rule.getCategory())){
                    throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_RULE_COMMON_CATEGORY_NULL.unifyErrorCode,
                            CommonUtil.formatString("[{}] rule: {}", AppException.ErrorCode.E_API_RULE_COMMON_CATEGORY_NULL.messageTemplate, rule));
                }else if(StringUtils.isBlank(rule.getName())){
                    throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_RULE_COMMON_NAME_NULL.unifyErrorCode,
                            CommonUtil.formatString("[{}] rule: {}", AppException.ErrorCode.E_API_RULE_COMMON_NAME_NULL.messageTemplate, rule));
                }else if(StringUtils.isBlank(rule.getCode())){
                    throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_RULE_COMMON_CODE_NULL.unifyErrorCode,
                            CommonUtil.formatString("[{}] rule: {}", AppException.ErrorCode.E_API_RULE_COMMON_CODE_NULL.messageTemplate, rule));
                }

                if(ruleSetRequest.getName().toLowerCase().contains(ScanEngine.EngineType.XCALIBYTE.toString().toLowerCase())) {
                    RuleInformation ruleInfo = this.getRuleInformationForRuleSetRequest(rule.getCode(), rule, RuleInformation.Certainty.D, ruleSet, existingRuleMap, user);
                    ruleInfos.add(ruleInfo);
                } else if (ruleSetRequest.getName().toLowerCase().contains(ScanEngine.EngineType.SPOTBUGS.toString().toLowerCase())) {
                    RuleInformation ruleInfo = this.getRuleInformationForRuleSetRequest(rule.getCode(), rule, null, ruleSet, existingRuleMap, user);
                    ruleInfos.add(ruleInfo);
                } else {
                    throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_NOT_IMPLEMENT.unifyErrorCode,
                            CommonUtil.formatString("[{}] name: {}", AppException.ErrorCode.E_API_COMMON_COMMON_NOT_IMPLEMENT.messageTemplate, ruleSetRequest.getName()));
                }
            }
        }
        return ruleInfos;
    }

    private RuleInformation getRuleInformationForRuleSetRequest(String ruleCode, RuleSetRequest.RuleSet.Rule rule, RuleInformation.Certainty certainty, RuleSet ruleSet, Map<String, RuleInformation> existingRuleMap, User user){
        RuleInformation ruleInformation;
        Date now = new Date();
        if (existingRuleMap.containsKey(ruleCode)) {
            ruleInformation = existingRuleMap.get(ruleCode);
        }else{
            ruleInformation = RuleInformation.builder().createdBy(user.getUsername()).createdOn(now).build();
        }
        ruleInformation.setRuleSet(ruleSet);
        ruleInformation.setCategory(StringUtils.upperCase(rule.getCategory()));
        ruleInformation.setVulnerable(rule.getCode());
        ruleInformation.setCertainty(certainty);
        ruleInformation.setRuleCode(ruleCode);
        ruleInformation.setLanguage(rule.getLanguage());
        ruleInformation.setUrl(rule.getRuleUrl());
        ruleInformation.setName(rule.getName());
        ruleInformation.setSeverity(EnumUtils.getEnumIgnoreCase(RuleInformation.Severity.class, rule.getSeverity()));
        ruleInformation.setPriority((EnumUtils.getEnumIgnoreCase(RuleInformation.Priority.class, rule.getPriority())));
        ruleInformation.setLikelihood(EnumUtils.getEnumIgnoreCase(RuleInformation.Likelihood.class, rule.getLikelihood()));
        ruleInformation.setRemediationCost(EnumUtils.getEnumIgnoreCase(RuleInformation.RemediationCost.class, rule.getFixCost()));
        ruleInformation.setDetail(rule.getDetail());
        ruleInformation.setDescription(rule.getDescription());
        ruleInformation.setMessageTemplate(rule.getMessageTemplate());
        ruleInformation.setModifiedBy(user.getUsername());
        ruleInformation.setModifiedOn(now);
        return ruleInformation;
    }

    private List<RuleInformation> saveRuleInformation(List<RuleInformation> ruleInformationList) {
        List<RuleInformation> ruleInfos = this.ruleInformationRepository.saveAll(ruleInformationList);
        this.ruleInformationRepository.flush();
        return ruleInfos;
    }

    static String getRuleCodeKey(RuleInformation ruleInformation) {
        log.debug("[getRuleCodeKey] ruleInformation, id: {}", ruleInformation.getId());
        return getRuleCodeKey(ruleInformation.getRuleSet().getScanEngine().getName(), ruleInformation.getRuleSet().getName(), ruleInformation.getRuleCode());
    }

    static String getRuleCodeKey(String engineName, String ruleSetName, String ruleCode) {
        String result;
        log.debug("[getRuleCodeKey] engineName: {}, ruleSetName: {}, ruleCode: {}", engineName, ruleSetName, ruleCode);
        result = engineName + "-" + ruleSetName + "-" + ruleCode;
        log.debug("[getRuleCodeKey] result: {}", result);
        return result;
    }

    public List<RuleSet> getRuleSetFromImportScanResultRequest(ImportScanResultRequest importScanResultRequest) {
        log.info("[getRuleSetFromImportScanResultRequest] importScanResultRequest, engine: {}, engineVersion: {}", importScanResultRequest.getEngine(), importScanResultRequest.getEngineVersion());
        return importScanResultRequest.getRuleSets().stream()
                .map(rs -> this.getRuleSetByScanEngineNameAndScanEngineVersionNameAndVersion(importScanResultRequest.getEngine(), importScanResultRequest.getEngineVersion(),rs.getRuleSet(), rs.getRuleSetVersion()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}
