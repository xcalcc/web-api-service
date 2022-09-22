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

package com.xcal.api.service.v3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.model.dto.v3.RuleInfoDto;
import com.xcal.api.model.dto.v3.RuleListResponseDto;
import com.xcal.api.model.dto.v3.RuleStandardDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RuleServiceV3 {

    private static final String API_GET_RULE_INFO_URL = "http://%s/api/rule_service/v3/rule/rule_info/%s?locale=%s";

    private static final String API_GET_RULE_LIST_URL = "http://%s/api/rule_service/v3/rule/rule_list?locale=%s";

    private static final String API_GET_RULE_STANDARD_URL = "http://%s/api/rule_service/v3/rule/standards?locale=%s";


    @Value("${app.rule.service.url}")
    public String ruleServiceUrl;

    @NonNull
    final RestTemplate restTemplate;

    final ObjectMapper objectMapper = new ObjectMapper();

    public RuleStandardDto getRuleStandard(Locale locale) {
        log.info("[getRuleStandard] locale:{}", locale);
        try {
            String url = String.format(API_GET_RULE_STANDARD_URL, ruleServiceUrl, locale.toLanguageTag());
            String res = this.restTemplate.getForObject(url, String.class);
            return this.objectMapper.readValue(res, RuleStandardDto.class);
        } catch (Exception e) {
            log.warn("[getRuleStandard] failed",e);
        }
        return null;
    }

    public RuleInfoDto getRuleInfo(String ruleCode, Locale locale) {
        log.info("[getRuleInfo] ruleCode:{} , locale:{}",ruleCode,locale);
        try {
            String url = String.format(API_GET_RULE_INFO_URL, this.ruleServiceUrl, ruleCode, locale.toLanguageTag());
            String res = this.restTemplate.getForObject(url, String.class);
            JsonNode root = this.objectMapper.readTree(res);
            return this.objectMapper.readValue(root.get("data").toString(), RuleInfoDto.class);
        } catch (Exception e) {
            log.warn("get rule info failed, ruleCode: {}, message: {}", ruleCode, e.getMessage(),e);
        }
        return null;
    }

    public RuleListResponseDto getAllRuleInfo(Locale locale) {
        try {
            String url = String.format(API_GET_RULE_LIST_URL, this.ruleServiceUrl, locale.toLanguageTag());
            String res = this.restTemplate.getForObject(url, String.class);
            JsonNode root = this.objectMapper.readTree(res);
            JavaType type = this.objectMapper.getTypeFactory().constructParametricType(List.class, RuleInfoDto.class);
            List<RuleInfoDto> ruleInfoDtoList = this.objectMapper.readValue(root.get("rules").toString(), type);


            TypeReference<HashMap<String, Integer>> hashMapTypeReference
                    = new TypeReference<HashMap<String, Integer>>() {
            };
            Map<String, Integer> csvCodeMap = objectMapper.readValue(root.get("csvCodeMap").toString(), hashMapTypeReference);

            return RuleListResponseDto
                    .builder()
                    .ruleInfoDtoList(ruleInfoDtoList)
                    .csvCodeMap(csvCodeMap)
                    .build();


        } catch (Exception e) {
            log.warn("get rule info failed, message: {}", e.getMessage());
        }
        return null;
    }

}
