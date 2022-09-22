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

import com.xcal.api.util.MessagesTemplate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Project Config")
public class ProjectConfigDto {
    @ApiModelProperty(example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", value = "ID")
    UUID id;
    String name;
    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    String projectConfig;
    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    String scanConfig;
    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    List<Attribute> attributes;
    Project project;
    String status;
    String createdBy;
    Date createdOn;
    String modifiedBy;
    Date modifiedOn;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {
        UUID id;
        String type;
        String name;
        String value;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(description = "Project")
    public static class Project {
        UUID id;
        String projectId;
        String name;
        String status;
        String cicdFsmState;
        String baselineCommitId;
        Integer retentionNum;
        String createdBy;
        Date createdOn;
        String modifiedBy;
        Date modifiedOn;
    }
}
