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

import com.xcal.api.model.payload.ImportScanResultRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Scan Result")
public class ScanResultDto {
    @ApiModelProperty(example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", value = "ID")
    UUID scanTaskId;
    String status;
    String message;
    Long scanStartAt;
    Long scanEndAt;

    List<Issue> issues;
    List<ScanFile> scanFiles;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScanFile {
        UUID id;
        String name;
        String relativePath;
        String version;
        String checksum;
        Integer noOfLines;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Issue {
        UUID id;
        String issueCategory;
        String issueCode;
        String severity;
        String issueKey;
        String filePath;
        String lineNo;
        String columnNo;
        String functionName;
        String variableName;
        String errorCode;
        String message;
        List<ImportScanResultRequest.Issue.TracePath> tracePaths;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class TracePath {
            UUID id;
            Integer sequence;
            String columnNo;
            String functionName;
            String variableName;
            String message;
        }
    }
}
