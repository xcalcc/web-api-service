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

import com.xcal.api.entity.Project;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "UpdateProjectRequest")
public class UpdateProjectRequest {

	@ApiModelProperty(notes = "project uuid. id or projectId must provide at least one.", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a")
	private UUID id;

	@ApiModelProperty(notes = "project id. id or projectId must provide at least one.", example = "7-basic")
	private String projectId;

	@ApiModelProperty(notes = "project name", example = "basic")
	private String projectName;

	@ApiModelProperty(notes = "project status: PENDING, ACTIVE, INACTIVE, DELETED", dataType = "Status", example = "ACTIVE")
	private Project.Status status;

	@ApiModelProperty(notes = "project config name", example = "basic_config")
	private String projectConfigName;

	@ApiModelProperty(notes = "project config info, map format", example = "{\"relativeSourcePath\": \"/home/xxx/project\", \"relativeBuildPath\": \"/home/xxx/project\"}")
	private Map<String, String> projectConfig;

	@ApiModelProperty(notes = "scan config info, map format", example = "{\"build\": \"make\", \"lang\": \"c++\"}")
	private Map<String, String> scanConfig;

}
