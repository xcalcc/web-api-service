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


import com.xcal.api.util.MessagesTemplate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="New Project", description = "Project and ProjectConfig related as request parameter only")
public class NewProjectRequest {

    @ApiModelProperty(notes = "project name, if not provide, will use project id as project name",  example = "7-basic")
    String projectName;

    @ApiModelProperty(notes = "project id", example = "7-basic", required = true)
    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    String projectId;

    @ApiModelProperty(notes = "config name", example = "default")
    String configName;

    @ApiModelProperty(notes = "project config info, map format", example = "{\"relativeSourcePath\": \"/home/xxx/project\", \"relativeBuildPath\": \"/home/xxx/project\"}")
    @Builder.Default
    Map<String, String> projectConfig = new LinkedHashMap<>();

    @ApiModelProperty(notes = "scan config info, map format", example = "{\"build\": \"make\", \"lang\": \"c++\"}")
    @Builder.Default
    Map<String, String> scanConfig = new LinkedHashMap<>();
}
