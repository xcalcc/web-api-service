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
@ApiModel(description = "IssueSummaryResponse")
public class IssueSummaryResponse {

        String scanTaskId;

        String lastScan;

        String noOfFiles;

        String noOfLines;

        String noOfIssues;

        String noOfHighSeverity;

        String noOfMediumSeverity;

        String noOfLowSeverity;

        String riskLevel;

        @Builder.Default
        List<AssignSummary> assignSummary = new ArrayList<>();

        @Builder.Default
        List<SeveritySummary> severitySummary = new ArrayList<>();

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @ApiModel(description = "AssignSummary")
        public static class AssignSummary {

                UUID id;

                String email;

                String displayName;

                Long count;
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @ApiModel(description = "SeveritySummary")
        public static class SeveritySummary {

                String severity;

                @Builder.Default
                List<VulnerableSummary> vulnerableSummary = new ArrayList<>();

                @Data
                @Builder
                @AllArgsConstructor
                @NoArgsConstructor
                @ApiModel(description = "VulnerableSummary")
                public static class VulnerableSummary {

                        String  vulnerable;

                        Long count;
                }
        }

}
