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

package com.xcal.api.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xcal.api.entity.RuleInformationAttribute;
import com.xcal.api.util.VariableUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Issue")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueDto {
    @ApiModelProperty(example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", value = "ID")
    UUID id;
    String issueKey;
    String seq;
    RuleInformation ruleInformation;
    String issueCategory;
    String ruleSet;
    String vulnerable;
    String certainty;
    String issueCode;
    String issueName;
    String critical;
    String severity;
    String likelihood;
    String remediationCost;
    UUID scanFileId;
    String relativePath;
    String scanFilePath;
    @Builder.Default
    Integer lineNo = 0;
    @Builder.Default
    Integer columnNo = 0;
    String functionName;
    String variableName;
    String complexity;
    Double complexityRate;
    String checksum;
    String message;
    String ignored;
    String status;
    String action;
    AssignTo assignTo;
    String createdBy;
    Date createdOn;
    String modifiedBy;
    Date modifiedOn;
    @Builder.Default
    List<IssueTrace> issueTraces = new ArrayList<>();
    @Builder.Default
    List<IssueTraceInfo> issueTraceInfos = new ArrayList<>();
    @Builder.Default
    List<IssueAttribute> issueAttributes = new ArrayList<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueTrace {
        UUID id;
        Integer seq;
        UUID scanFileId;
        String relativePath;
        String scanFilePath;
        @Builder.Default
        Integer lineNo = 0;
        @Builder.Default
        Integer columnNo = 0;
        String functionName;
        String variableName;
        String checksum;
        String message;
        @Builder.Default
        Long scanFileSize = 0L;
        @Builder.Default
        Integer scanFileNoOfLines = 0;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueTraceInfo {
        String id; // checksum
        Integer noOfTrace;
        String message;
        Double complexity;
        Double complexityRate;
        @Builder.Default
        Map<String, String> attributes = new HashMap<>();
        @Builder.Default
        List<IssueTrace> issueTraces = new ArrayList<>();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AssignTo {
        UUID id;
        String displayName;
        String email;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RuleInformation {
        UUID id;
        String ruleSet;
        String ruleSetVersion;
        String ruleSetDisplayName;
        String scanEngineName;
        String scanEngineVersion;
        String ruleCode;
        String category;
        String vulnerable;
        String name;
        String certainty;
        String priority;
        String severity;
        String likelihood;
        String remediationCost;
        String language;
        String url;
        String detail;
        String description;
        List<RuleInformationAttribute> attributes;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueAttribute {
        VariableUtil.IssueAttributeName name;
        String value;
    }

    public Optional<IssueAttribute> getFirstAttribute(VariableUtil.IssueAttributeName attribute) {
        return this.getIssueAttributes().stream().filter(issueAttribute -> attribute == issueAttribute.name).findFirst();
    }
}
