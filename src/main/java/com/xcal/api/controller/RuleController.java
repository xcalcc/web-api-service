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

import com.xcal.api.entity.RuleInformation;
import com.xcal.api.entity.RuleSet;
import com.xcal.api.entity.RuleStandard;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.RuleInformationDto;
import com.xcal.api.model.dto.RuleSetDto;
import com.xcal.api.model.dto.RuleStandardDto;
import com.xcal.api.model.payload.RuleSetRequest;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.RuleService;
import com.xcal.api.service.RuleStandardService;
import com.xcal.api.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/rule_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Rule Service")
public class RuleController {

    @NonNull RuleService ruleService;
    @NonNull RuleStandardService ruleStandardService;
    @NonNull ModelMapper modelMapper;

    /**
     * @return List of RuleInformationDto
     */
    @GetMapping("/rules")
    @ApiOperation(value = "Get all rule information",
            nickname = "getAllRuleInformation",
            notes = "Retrieve all rule information ",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<RuleInformationDto>> getAllRuleInformation(Pageable pageable, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getAllRuleInformation] username: {}", userPrincipal.getUsername());
        Page<RuleInformation> ruleInformationList = ruleService.findAll(pageable);
        List<RuleInformationDto> result = this.ruleService.convertRuleInformationListToDto(ruleInformationList.getContent(), locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PageImpl<>(result, pageable, ruleInformationList.getTotalElements()));
    }

    /**
     * @param id uuid of scanTask
     * @return RuleInformationDto
     */
    @GetMapping("/rule/{id}")
    @ApiOperation(value = "Get rule information by id",
            nickname = "getRuleInformation",
            notes = "Retrieve the rule information with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RuleInformationDto> getRuleInformation(
            @ApiParam(value = "uuid of the rule", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getRuleInformation] id: {}, username: {}", id, userPrincipal.getUsername());
        Optional<RuleInformation> ruleInformationOptional = ruleService.findById(id);
        RuleInformation ruleInformation = ruleInformationOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_RULE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_RULE_COMMON_NOT_FOUND.messageTemplate, id)));
        RuleInformationDto result = this.ruleService.convertRuleInformationToDto(ruleInformation, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * @param scanEngineName scan engine name
     * @param ruleSetName    rule set name
     * @param ruleSetVersion rule set version
     * @return List of RuleInformation with corresponding information
     */
    @GetMapping("/rule/scan_engine/name/{scanEngineName}/rule_set/name/{ruleSetName}/version/{ruleSetVersion}")
    @ApiOperation(value = "Get rule information by scan engine name, rule set name and rule set version",
            nickname = "getRuleInformationByScanEngineNameAndRuleSetNameAndRuleSetVersion",
            notes = "Get rule information by scan engine name, rule set name and rule set version",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RuleInformationDto>> getRuleInformationByScanEngineNameAndRuleSetNameAndRuleSetVersion(
            @PathVariable String scanEngineName, @PathVariable String ruleSetName, @PathVariable String ruleSetVersion,
            Locale locale, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[getRuleInformationByScanEngineNameAndRuleSetNameAndRuleSetVersion] scanEngineName: {}, ruleSetName: {}, ruleSetVersion: {}, username: {}", scanEngineName, ruleSetName, ruleSetVersion, userPrincipal.getUsername());
        List<RuleInformation> ruleInformationList = ruleService.getRuleInformation(scanEngineName, null, ruleSetName, ruleSetVersion);
        List<RuleInformationDto> ruleInformationDtoList = this.ruleService.convertRuleInformationListToDto(ruleInformationList, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ruleInformationDtoList);
    }

    /**
     * @param name    rule set name
     * @param version rule set version
     * @return List of ruleInformationDto
     */
    @GetMapping("/rule/rule_set/name/{name}/version/{version}")
    @ApiOperation(value = "Get rule information by rule set name and rule set version",
            nickname = "getRuleInformationByRuleSetNameAndRuleSetVersion",
            notes = "Get rule information by rule set name and rule set version",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RuleInformationDto>> getRuleInformationByRuleSetNameAndRuleSetVersion(
            @PathVariable String name, @PathVariable String version,
            Locale locale, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[getRuleInformationByRuleSetNameAndRuleSetVersion] rule set, name: {}, version: {}, username: {}", name, version, userPrincipal.getUsername());
        List<RuleInformation> ruleInformationList = ruleService.getRuleInformation(null, null, name, version);
        List<RuleInformationDto> ruleInformationDtoList = this.ruleService.convertRuleInformationListToDto(ruleInformationList, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ruleInformationDtoList);
    }

    /**
     * @param name rule set name
     * @return List of ruleInformationDto
     */
    @GetMapping("/rule/rule_set/name/{name}")
    @ApiOperation(value = "Get rule information by rule set name",
            nickname = "getRuleSetByName",
            notes = "Get rule set by name",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RuleInformationDto>> getRuleInformationByRuleSetName(
            @PathVariable String name, Locale locale, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[getRuleInformationByRuleSetName] name: {}, username: {}", name, userPrincipal.getUsername());
        List<RuleInformation> ruleInformationList = ruleService.getRuleInformation(null, null, null, name);
        List<RuleInformationDto> ruleInformationDtoList = this.ruleService.convertRuleInformationListToDto(ruleInformationList, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ruleInformationDtoList);
    }

    /**
     * @param id rule set id
     * @return rule information dto list
     */
    @GetMapping("/rule/rule_set/{id}")
    @ApiOperation(value = "Get rule information by rule set id",
            nickname = "getRuleInformationByRuleSetId",
            notes = "Get rule information by rule set id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RuleInformationDto>> getRuleInformationByRuleSetId(
            @PathVariable UUID id, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getRuleInformationByRuleSetId] id: {}, username: {}", id, userPrincipal.getUsername());
        RuleSet ruleSet = this.ruleService.findRuleSetById(id).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_RULE_COMMON_RULESET_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_RULE_COMMON_RULESET_NOT_FOUND.messageTemplate, id)));
        List<RuleInformationDto> ruleInformationDtoList = ruleService.findDtoByRuleSet(ruleSet, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ruleInformationDtoList);
    }

    /**
     * @param id rule set id
     * @return ruleSetDto
     */
    @GetMapping("/rule_set/{id}")
    @ApiOperation(value = "Get rule set by id",
            nickname = "getRuleSet",
            notes = "Get rule set by id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RuleSetDto> getRuleSet(
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getRuleInformationByRuleSetId] id: {}, username: {}", id, userPrincipal.getUsername());
        Optional<RuleSet> ruleSetOptional = this.ruleService.findRuleSetById(id);
        RuleSet ruleSet = ruleSetOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_RULE_COMMON_RULESET_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_RULE_COMMON_RULESET_NOT_FOUND.messageTemplate, id)));
        RuleSetDto ruleSetDto = modelMapper.map(ruleSet, RuleSetDto.class);
        ruleSetDto.setScanEngine(modelMapper.map(ruleSet.getScanEngine(), RuleSetDto.ScanEngine.class));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ruleSetDto);
    }

    /**
     * @return List of ruleSetDto in paging
     */
    @GetMapping("/rule_sets")
    @ApiOperation(value = "Get rule sets",
            nickname = "getRuleSets",
            notes = "Get rule set by id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<RuleSetDto>> getRuleSets(Pageable pageable, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[getRuleInformationByRuleSetId] pageable: {}, username: {}", pageable, userPrincipal.getUsername());
        Page<RuleSet> ruleSets = this.ruleService.findRuleSet(pageable);
        Page<RuleSetDto> ruleSetDtoPage = ruleSets.map(ruleSet -> modelMapper.map(ruleSet, RuleSetDto.class));
        for (RuleSet ruleSet : ruleSets) {
            ruleSetDtoPage.filter(ruleSetDto -> ruleSet.getId().equals(ruleSetDto.getId())).stream().findFirst().ifPresent(ruleSetDto -> ruleSetDto.setScanEngine(modelMapper.map(ruleSet.getScanEngine(), RuleSetDto.ScanEngine.class)));
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ruleSetDtoPage);
    }

    /**
     * @param ruleSetRequest Add rule set request
     * @return List of ruleInformationDto
     */
    @PostMapping("/rule_set")
    @ApiOperation(value = "Import RuleSet",
            nickname = "addRuleSet",
            notes = "Add rule set",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<List<RuleInformationDto>> addRuleSet(
            @RequestBody RuleSetRequest ruleSetRequest, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[addRuleSet] ruleSetRequest, name: {}, version: {}, username: {}", ruleSetRequest.getName(), ruleSetRequest.getVersion(), userPrincipal.getUsername());
        List<RuleInformation> ruleInformationList = ruleService.addRuleSets(ruleSetRequest, userPrincipal.getUser());
        List<RuleInformationDto> ruleInformationDtoList = this.ruleService.convertRuleInformationListToDto(ruleInformationList, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ruleInformationDtoList);
    }

    /**
     * @param id uuid of rule standard
     * @return RuleStandardDto
     */
    @GetMapping("/rule_standard/{id}")
    @ApiOperation(value = "Get rule standard by id",
            nickname = "getRuleStandard",
            notes = "Retrieve the rule standard with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RuleStandardDto> getRuleStandard(
            @ApiParam(value = "uuid of the rule standard", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getRuleStandard] id: {}, username: {}", id, userPrincipal.getUsername());
        Optional<RuleStandard> ruleStandardOptional = ruleStandardService.findById(id);
        RuleStandard ruleStandard = ruleStandardOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_RULE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_RULE_COMMON_NOT_FOUND.messageTemplate, id)));
        RuleStandardDto result = this.ruleStandardService.convertRuleStandardToDto(ruleStandard, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * @param name rule standard set name
     * @return List of RuleStandardDto
     */
    @GetMapping("/rule_standard/rule_standard_set/name/{name}")
    @ApiOperation(value = "Get rule standard by rule standard set name",
            nickname = "getRuleStandardBySetName",
            notes = "Get rule standard by rule standard set name",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RuleStandardDto>> getRuleStandardBySetName(
            @PathVariable String name, Locale locale, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[getRuleStandardBySetName] name: {}, username: {}", name, userPrincipal.getUsername());
        List<RuleStandard> ruleStandardList = ruleStandardService.findByRuleStandardSetNameAndVersion(name, null);
        List<RuleStandardDto> ruleStandardDtoList = this.ruleStandardService.convertRuleStandardListToDto(ruleStandardList, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ruleStandardDtoList);
    }
}
