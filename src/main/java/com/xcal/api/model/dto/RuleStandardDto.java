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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Rule Standard")
public class RuleStandardDto {
    UUID id;
    UUID ruleStandardSetId;
    String ruleStandardSet;
    String ruleStandardSetVersion;
    String ruleStandardSetRevision;
    String ruleStandardSetDisplayName;
    String category;
    String code;
    String language;
    String url;
    String name;
    String detail;
    String description;
    String messageTemplate;

    @Builder.Default
    List<Attribute> attributes = new ArrayList<>();
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {
        UUID id;
        String type;
        String name;
        String value;
    }
}
