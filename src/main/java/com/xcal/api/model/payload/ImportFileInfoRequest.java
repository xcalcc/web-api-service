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
@ApiModel(value = "Import FileInfo and ScanFile Request", description = "Object for import FileInfo and ScanFile")
public class ImportFileInfoRequest {
    public enum OSType {
        WIN, LINUX
    }

    List<File> files;
    String sourceType;
    String gitUrl;
    UUID sourceCodeFileId;
    String osType;
    String numberOfFiles;
    String totalLineNum;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class File {
        String fileId;
        String fileName;
        String filePath;        // the whole path of the file
        String relativePath;    // project relative path
        String parentPath;
        String type;
        String depth;
        String version;
        String checksum;
        String fileSize;
        String noOfLines;
    }

}
