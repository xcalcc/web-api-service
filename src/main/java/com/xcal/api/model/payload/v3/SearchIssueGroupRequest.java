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
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder(toBuilder = true) //toBuilder support create builder from object
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Search issue group request", description = "request object for search issue group")
public class SearchIssueGroupRequest {

	@ApiModelProperty(notes = "issue group id", example = "C74tP00004")
	protected String issueGroupId;

	@ApiModelProperty(notes = "project uuid", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e")
	protected UUID projectId;

	@ApiModelProperty(notes = "scan task uuid", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e", required = true)
	protected UUID scanTaskId;

	@ApiModelProperty(notes = "rule code list. here rule code is the combination of rule code and criticality", dataType = "RuleCode")
	protected List<RuleCode> ruleCodes;

	@ApiModelProperty(notes = "rule set list. rule set is the name of the collection of rules", example = "[\"X\", \"S\"]")
	private List<String> ruleSets;

	@ApiModelProperty(notes = "scan file uuid list", example = "[\"99aac10b-fff5-44bc-b26d-aa76ba58413e\", \"99aac10b-fff5-44bc-b26d-aa76ba584131\"]")
	protected List<UUID> scanFileIds;

	@ApiModelProperty(notes = "file path category, for example: H means host project path, T means target build path", example = "H")
	protected String pathCategory;

	@ApiModelProperty(notes = "issue certainty, D means definite, M means may", example = "D")
	protected String certainty;

	@ApiModelProperty(notes = "issue group id related to dsr scan type, N means new, E means existing, P means partial change, L means line number change", example = "[\"N\", \"E\", \"P\", \"L\"]")
	protected List<String> dsrType;

	@ApiModelProperty(notes = "issue criticality, value from 1-9, 1 means lowest, 9 means highest", example = "6")
	protected String criticality;

	protected String searchValue;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RuleCode {
		@ApiModelProperty(notes = "csv code(rule code) is used to mark which rule the issues violate", example = "UIV1")
		protected String csvCode;

		@ApiModelProperty(notes = "issue criticality, value from 1-9, 1 means lowest, 9 means highest", example = "6")
		protected String criticality;

	}
}
