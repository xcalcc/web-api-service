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
public class UpdateScanTaskRequest {
    @ApiModelProperty(notes = "scan task uuid", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", required = true)
    UUID id;
    @ApiModelProperty(notes = "scan task stage, such as PENDING, SCANNING, AGENT_END, SCAN_COMPLETE", example = "PENDING", required = true)
    String stage;
    @ApiModelProperty(notes = "scan task status, such as PENDING, PROCESSING, FAILED, COMPLETED", example = "PROCESSING", required = true)
    String status;
    @ApiModelProperty(notes = "scan task progress, 0~100", example = "50")
    Double percentage;
    @ApiModelProperty(notes = "scan error code", example = "0x00001234")
    String unifyErrorCode;
    @ApiModelProperty(notes = "scan task detail message", example = "Scan task added")
    String message;
}
