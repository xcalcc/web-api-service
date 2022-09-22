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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "AssignIssueGroupRequest")
public class AssignIssueGroupRequest {

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@ApiModel(description = "AssignIssueGroup")
	public static class AssignIssueGroup {

		@ApiModelProperty(notes = "issue group id", example = "C74tP00004")
		private String issueGroupId;

		@ApiModelProperty(notes = "user id", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e")
		private UUID userId;

	}

	@Builder.Default
	@ApiModelProperty(notes = "assignIssueGroup list. assignIssueGroup contains user id and issue group id.", dataType = "AssignIssueGroup")
	private List<AssignIssueGroup> assignIssueGroups = new ArrayList<>();

}
