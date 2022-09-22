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

package com.xcal.api.model.payload.v3;

import com.xcal.api.entity.v3.ReportAssigneeStatisticRow;
import com.xcal.api.entity.v3.ReportFileStatisticRow;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "PDF reponse", description = "PDF report")
public class ReportPDFResponse {

    private String projectName;

    private Date reportDate;

    private Date scanDate;

    private Date scanTime;

    private Integer fileCounts;

    private Integer lineCounts;

    private String projectOwner;

    private String language;

    private String projectCriticality;

    private Integer defectsCount;

    private Integer previousDefectsCount;

    private IssueCountGroupByCriticality issueCountGroupByCriticality;

    private CountValue defectsAssigned;

    private GroupByCriticalityCertainty groupByCriticalityCertainty;

    private List<ReportFileStatisticRow> groupByFile;

    private GroupByAssignee groupByAssignee;

    private String scanMode;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupByCriticalityCertainty {
        Map<String, ReportPDFResponse.CountValue> highD;
        Map<String, ReportPDFResponse.CountValue> highM;
        Map<String, ReportPDFResponse.CountValue> mediumD;
        Map<String, ReportPDFResponse.CountValue> mediumM;
        Map<String, ReportPDFResponse.CountValue> lowD;
        Map<String, ReportPDFResponse.CountValue> lowM;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueCountGroupByCriticality {
        Integer high;
        Integer medium;
        Integer low;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CountValue {
        Integer count;
        Float percentage;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupByAssignee {
        UnassignedData unassigned;
        AssigneeData assigned;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UnassignedData
    {
        Integer count;
        GroupByCriticalityCertainty groupByCriticalityCertainty;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AssigneeData
    {
        Integer count;
        List<ReportAssigneeStatisticRow> assignedByUser;
    }


}
