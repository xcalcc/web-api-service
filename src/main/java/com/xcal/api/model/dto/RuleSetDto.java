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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Rule Set")
public class RuleSetDto {
    UUID id;
    ScanEngine scanEngine;
    String category;
    String name;
    String version;
    String revision;
    String displayName;
    String description;
    String language;
    String url;
    String provider;
    String providerUrl;
    String license;
    String licenseUrl;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScanEngine {
        UUID id;
        String name;
        String version;
        String revision;
        String description;
        String language;
        String url;
        String provider;
        String providerUrl;
        String license;
        String licenseUrl;
    }
}
