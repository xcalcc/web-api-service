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

package com.xcal.api.model.dto.v3;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "IssueGroupDtoV3")
public class IssueGroupDto {

    private String id;

    private UUID projectId;

    private UUID occurScanTaskId;

    private String occurCommitId;

    private UUID fixedScanTaskId;

    private String fixedCommitId;

    private String ruleCode;

    private String ruleSet;

    private String srcPathCategory;

    private String srcFilePath;

    private String srcRelativePath;

    private Integer srcLineNo;

    private Integer srcColumnNo;

    private Integer srcMessageId;

    private String sinkPathCategory;

    private String sinkFilePath;

    private String sinkRelativePath;

    private Integer sinkLineNo;

    private Integer sinkColumnNo;

    private Integer sinkMessageId;

    private String functionName;

    private String variableName;

    private String severity;

    private String likelihood;

    private String remediationCost;

    private Integer complexity;

    private String priority;

    private String criticality;

    private String category;

    private String certainty;

    private Integer issueCount;

    private Integer avgTraceCount;

    private String status;

    private String dsr;

    private Date occurTime;

    private Date fixedTime;

    private UUID assigneeId;

    private String assigneeDisplayName;

    private String assigneeEmail;

    private List<Validation> validations;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Validation {
        UUID id;
        UUID projectId;
        UUID scanTaskId;
        String ruleCode;
        String filePath;
        String functionName;
        String variableName;
        Integer lineNumber;
        String type;
        String action;
        String scope;
        String createdBy;
        Date createdOn;
        String modifiedBy;
        Date modifiedOn;
    }
}
