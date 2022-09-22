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

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Scan File")
public class ScanFileDto {
    @ApiModelProperty(example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", value = "ID")
    UUID id;

    String projectRelativePath;

    @ApiModelProperty(example = "/home/xcalibyte/git/xcalibyte/src/main/java/com/xcalibyte/api/controller/ProjectController.java", value = "File stored path")
    String storePath;

    String status;

    String type;

    String parentPath;

    Long treeLeft;

    Long treeRight;

    Integer depth;

    FileInfo fileInfo;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileInfo {
        UUID id;
        String relativePath;
        String version;
        String checksum;
        String type;
        String status;
    }
}
