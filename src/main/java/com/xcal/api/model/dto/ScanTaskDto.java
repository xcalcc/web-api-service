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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Scan Task")
public class ScanTaskDto {
    @ApiModelProperty(example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", value = "ID")
    UUID id;
    UUID projectUuid;
    String projectName;
    String projectId;
    UUID projectConfigId;
    String status;
    String message;
    String sourceRoot;
    String createdBy;
    Date createdOn;
    String modifiedBy;
    Date modifiedOn;
}
