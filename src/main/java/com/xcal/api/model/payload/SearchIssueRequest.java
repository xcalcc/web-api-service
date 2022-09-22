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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Search issue Request", description = "Request Object for search issue")
public class SearchIssueRequest {
    public enum SearchIssueType {
        ONLY_PROJECT,
        ONLY_NON_PROJECT,
        PROJECT_AND_NON_PROJECT
    }

    UUID projectId;

    UUID scanTaskId;

    UUID ruleSetId;

    String ruleSetName;

    String seq;

    @Builder.Default
    List<UUID> ruleInformationIds = new ArrayList<>();

    @Builder.Default
    List<UUID> scanFileIds = new ArrayList<>();

    @Builder.Default
    SearchIssueType searchIssueType = SearchIssueType.PROJECT_AND_NON_PROJECT;

    @Builder.Default
    List<IssueAttribute> issueAttributes = new ArrayList<>();

    @Builder.Default
    List<RuleInformationAttribute> ruleInformationAttributes = new ArrayList<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(description = "IssueAttribute")
    public static class IssueAttribute {

        String name;

        @Builder.Default
        List<String> values = new ArrayList<>();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(description = "RuleInformationAttribute")
    public static class RuleInformationAttribute {

        String Type;

        String name;

        @Builder.Default
        List<String> values = new ArrayList<>();
    }

}
