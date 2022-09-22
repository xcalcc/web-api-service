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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value="Import Issue Request", description = "Object for import issues")
public class ImportScanResultRequest {
    @JsonProperty("v")
    String version;

    @JsonProperty("s")
    String status;

    @JsonProperty("m")
    String message;

    @JsonProperty("eng")
    String engine;

    @JsonProperty("ev")
    String engineVersion;

    @JsonProperty("cmd")
    String scanCommand;

    @JsonProperty("env")
    String scanEnvironment;

    @JsonProperty("files")
    @Builder.Default
    List<ImportScanResultRequest.FileInfo> fileInfos = new ArrayList<>();

    @JsonProperty("rulesets")
    @Builder.Default
    List<ImportScanResultRequest.RuleSet> ruleSets = new ArrayList<>();

    @JsonProperty("issues")
    @Builder.Default
    List<ImportScanResultRequest.Issue> issues = new ArrayList<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileInfo {

        @JsonProperty("fid")
        String fileId;

        @JsonProperty("path")
        String path;

        @JsonProperty("cs")
        String checksum;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RuleSet {

        @JsonProperty("rs")
        String ruleSet;

        @JsonProperty("rv")
        String ruleSetVersion;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Issue {

        @JsonProperty("fid")
        String fileId;

        @JsonProperty("sln")
        Integer startLineNo;

        @JsonProperty("scn")
        Integer startColumnNo;

        @JsonProperty("eln")
        Integer endLineNo;

        @JsonProperty("ecn")
        Integer endColumnNo;

        @JsonProperty("k")
        String key;

        @JsonProperty("rs")
        String ruleSet;

        @JsonProperty("rc")
        String ruleCode;

        @JsonProperty("ec")
        String errorCode;

        @JsonProperty("c")
        String certainty;

        @JsonProperty("ic")
        String complexity;

        @JsonProperty("vn")
        String variableName;

        @JsonProperty("fn")
        String functionName;

        @JsonProperty("tn")
        String typeName;

        @JsonProperty("m")
        String message;

        @JsonProperty("paths")
        @Builder.Default
        List<ImportScanResultRequest.Issue.TracePath> tracePaths = new ArrayList<>();

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class TracePath {

            @JsonProperty("fid")
            String fileId;

            @JsonProperty("sln")
            Integer startLineNo;

            @JsonProperty("scn")
            Integer startColumnNo;

            @JsonProperty("eln")
            Integer endLineNo;

            @JsonProperty("ecn")
            Integer endColumnNo;

            @JsonProperty("m")
            String message;

            @JsonProperty("vn")
            String variableName;

            @JsonProperty("fn")
            String functionName;
            @JsonProperty("tn")
            String typeName;
        }
    }
}
