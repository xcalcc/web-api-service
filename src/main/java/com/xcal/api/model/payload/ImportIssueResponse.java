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
@ApiModel(description = "ImportIssueResponse")
public class ImportIssueResponse {
    UUID scanTaskId;

    @Builder.Default
    List<Issue> issues = new ArrayList<>();

    @Builder.Default
    Map<String, String> summary = new HashMap<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Issue {
        @ApiModelProperty(example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", value = "ID")
        UUID id;
        String issueKey;
        String seq;
        UUID ruleInformationId;
        String issueCategory;
        String ruleSet;
        String vulnerable;
        String certainty;
        String issueCode;
        String severity;
        String priority;
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
        @Builder.Default
        List<IssueTraceInfo> issueTraceInfos = new ArrayList<>();

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
    }
}
