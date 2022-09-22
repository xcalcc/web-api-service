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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "SummaryResponse")
public class SummaryResponse {

    UUID projectUuid;
    String projectId;
    String projectName;
    UUID scanTaskId;
    String commitId;
    String status;
    Long scanStartAt;
    Long scanEndAt;
    String language;
    ScanTaskSummary latestScanTask;
    ScanTaskSummary latestCompleteScanTask;
    boolean hasDsr;
    @Builder.Default
    Map<String, FileInfo> fileInfoMap = new HashMap<>();

    IssueSummary issueSummary;


    @Builder.Default
    Map<String, RuleSet> ruleSetSummaryMap = new HashMap<>();
    @Builder.Default
    Map<String, RuleStandard> ruleStandardSummaryMap = new HashMap<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueSummary {
        String fileCount;
        String lineCount;
        String issuesCount;
        String criticality;

        String baselineScanTaskId;
        String commitId;
        String baselineCommitId;

        @Builder.Default
        Map<String, String> ruleCodeCountMap = new HashMap<>();

        @Builder.Default
        Map<String, String> certaintyCountMap = new HashMap<>();

        @Builder.Default
        Map<String, String> criticalityCountMap = new HashMap<>();

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)    //  ignore all null fields
    public static class DiffInfoSummary {
        String newIssueCount;
        String newIssuePathCount;
        String fixedIssueCount;
        String fixedIssuePathCount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RuleSet {
        String id;
        String name;
        String version;
        IssueSummary issueSummary;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RuleStandard {
        String id;
        String name;
        String version;
        IssueSummary issueSummary;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScanTaskSummary {
        UUID scanTaskId;
        String commitId;
        String status;
        Long createdAt;
        Long scanStartAt;
        Long scanEndAt;
        Long lastModifiedAt;
        String language;
        IssueSummary issueSummary;
        @Builder.Default
        Map<String, RuleSet> ruleSetSummaryMap = new HashMap<>();
        @Builder.Default
        Map<String, RuleStandard> ruleStandardSummaryMap = new HashMap<>();
        //<RuleCode, <Level, Count >>
        @Builder.Default
        Map<String, Map<String, String>> ruleCodeSeverityMap = new HashMap<>();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileInfo {
        UUID id;
        String name;
    }
}
