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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "SearchIssueValidationRequest")
public class SearchIssueValidationRequest {
    @ApiModelProperty(notes = "validation type, such as: CUSTOM, DEFAULT", example = "DEFAULT")
    String type;
    @ApiModelProperty(notes = "validation action, such as: UNDECIDED, IGNORE, TP, FP", example = "IGNORE")
    String action;
    @ApiModelProperty(notes = "validation apply scope, such as: PROJECT, USER, GLOBAL", example = "PROJECT")
    String scope;
}
