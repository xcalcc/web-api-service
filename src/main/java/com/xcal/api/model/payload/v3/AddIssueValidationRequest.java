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

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "AddIssueValidationRequest")
public class AddIssueValidationRequest {
    @ApiModelProperty(notes = "project uuid", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a")
    UUID projectId;
    @ApiModelProperty(notes = "scan task uuid", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a")
    UUID scanTaskId;
    @ApiModelProperty(notes = "rule code", example = "AOB4")
    String ruleCode;
    @ApiModelProperty(notes = "file path, most is relative file path", example = "xxx/xxx/xxx.c")
    String filePath;
    @ApiModelProperty(notes = "function name", example = "xxx")
    String functionName;
    @ApiModelProperty(notes = "variable name", example = "xxx")
    String variableName;
    @ApiModelProperty(notes = "line number", example = "30")
    Integer lineNumber;
    @ApiModelProperty(notes = "validation type, such as: CUSTOM, DEFAULT", example = "DEFAULT", required = true)
    String type;
    @ApiModelProperty(notes = "validation action, such as: UNDECIDED, IGNORE, TP, FP", example = "IGNORE", required = true)
    String action;
    @ApiModelProperty(notes = "validation apply scope, such as: PROJECT, USER, GLOBAL", example = "PROJECT", required = true)
    String scope;
}
