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

package com.xcal.api.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Rule Information")
public class RuleSetRequest {
    @JsonProperty("name")
    String name;
    @JsonProperty("version")
    String version;
    @JsonProperty("revision")
    String revision;
    @JsonProperty("description")
    String description;
    @JsonProperty("languages")
    String language;
    @JsonProperty("engine_url")
    String engineUrl;
    @JsonProperty("provider")
    String provider;
    @JsonProperty("provider_url")
    String providerUrl;
    @JsonProperty("license")
    String license;
    @JsonProperty("license_url")
    String licenseUrl;
    @JsonProperty("rulesets")
    @Builder.Default
    List<RuleSet> ruleSets = new ArrayList<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RuleSet {
        @JsonProperty("name")
        String name;
        @JsonProperty("display_name")
        String displayName;
        @JsonProperty("version")
        String version;
        @JsonProperty("revision")
        String revision;
        @JsonProperty("description")
        String description;
        @JsonProperty("languages")
        String language;
        @JsonProperty("ruleset_url")
        String ruleSetUrl;
        @JsonProperty("provider")
        String provider;
        @JsonProperty("provider_url")
        String providerUrl;
        @JsonProperty("license")
        String license;
        @JsonProperty("license_url")
        String licenseUrl;
        @JsonProperty("rules")
        @Builder.Default
        List<Rule> rules = new ArrayList<>();

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Rule {

            @JsonProperty("code")
            String code;
            @JsonProperty("name")
            String name;
            @JsonProperty("description")
            String description;
            @JsonProperty("details")
            String detail;
            @JsonProperty("message_template")
            String messageTemplate;
            @JsonProperty("languages")
            String language;
            @JsonProperty("category")
            String category;
            @JsonProperty("severity")
            String severity;
            @JsonProperty("priority")
            String priority;
            @JsonProperty("likelyhood")
            String likelihood;
            @JsonProperty("fix_cost")
            String fixCost;
            @JsonProperty("rule_url")
            String ruleUrl;
        }

    }
}
