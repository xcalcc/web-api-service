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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "SearchScanTaskRequest", description = "Search scan task request")
public class SearchScanTaskRequest {
    @ApiModelProperty(notes = "project uuid", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", required = true)
    UUID projectId;

    @ApiModelProperty(notes = "scan task status list, such as PENDING, PROCESSING, FAILED, COMPLETED", example = "[\"PROCESSING\", \"COMPLETED\"]", required = true)
    @Builder.Default
    List<String> status = new ArrayList<>();

    @ApiModelProperty(notes = "exist attribute list", dataType = "Attribute")
    @Builder.Default
    List<Attribute> existAttributes = new ArrayList<>();

    @ApiModelProperty(notes = "equal attribute list", dataType = "Attribute")
    @Builder.Default
    List<Attribute> equalAttributes = new ArrayList<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {
        @ApiModelProperty(notes = "attribute type", example = "SCAN", required = true)
        String type;
        @ApiModelProperty(notes = "attribute name", example = "repoAction", required = true)
        String name;
        @ApiModelProperty(notes = "attribute value", example = "TRIAL", required = true)
        String value;
    }
}
