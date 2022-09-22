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

package com.xcal.api.model.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "RuleInfoDtoV3")
public class RuleInfoDto {

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RuleSetDto {

		@JsonProperty("id")
		private String id;

		@JsonProperty("code")
		private String name;

		@JsonProperty("displayName")
		private String displayName;

	}

	@JsonProperty("master_id")
	private String Id;

	@JsonProperty("code")
	private String ruleCode;

	@JsonProperty("ruleSetId")
	private String ruleSetId;

	@JsonProperty("ruleSet")
	private RuleSetDto ruleSet;

	@JsonProperty("category")
	private String category;

	@JsonProperty("language")
	private String language;

	@JsonProperty("name")
	private String name;

	@JsonProperty("desc")
	private String description;

	@JsonProperty("detail")
	private String detail;

	@JsonProperty("msg_templ")
	private String messageTemplate;

	@JsonProperty("severity")
	private String severity;

	@JsonProperty("likelihood")
	private String likelihood;

	@JsonProperty("compliance")
	private String compliance;

	@JsonProperty("cost")
	private String remediationCost;

	@JsonProperty("standards")
	private Map<String,List<String>> standard;

	@JsonProperty("csv_string")
	private List<String> codes;

}
