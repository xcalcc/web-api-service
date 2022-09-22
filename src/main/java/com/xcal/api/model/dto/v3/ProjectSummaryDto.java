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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "ProjectSummaryDtoV3")
public class ProjectSummaryDto {

	@ApiModelProperty(notes = "project summary uuid", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e")
	private UUID id;

	@ApiModelProperty(notes = "project id", example = "7041basic19796227")
	private String projectId;

	@ApiModelProperty(notes = "project name", example = "704-1basic")
	private String name;

	@ApiModelProperty(notes = "project status", example = "ACTIVE")
	private String status;

	@ApiModelProperty(notes = "created by who", example = "alice")
	private String createdBy;

	@ApiModelProperty(notes = "created time", example = "1656919796670")
	private Date createdOn;

	@ApiModelProperty(notes = "modified by who", example = "alice")
	private String modifiedBy;

	@ApiModelProperty(notes = "modified time", example = "1656919796670")
	private Date modifiedOn;

	private Summary summary;

	@ApiModelProperty(notes = "whether has dsr scan", example = "false")
	private boolean hasDsr;

	@ApiModelProperty(notes = "scan mode", example = "SINGLE")
	private String scanMode;

	@ApiModelProperty(notes = "last scan status", example = "COMPLETED")
	private String lastScanStatus;

	private ScanTask lastScanTask;



	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@ApiModel(description = "RuleSet")
	public static class RuleSet {

		@Builder.Default
		Map<String, String> criticality = new HashMap();

		String issuesCount;

	}


	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@ApiModel(description = "Summary")
	public static class Summary {

		@ApiModelProperty(notes = "scan start time", example = "1656919796670")
		private String scanStartAt;

		@ApiModelProperty(notes = "scan end time", example = "1656919796670")
		private String scanEndAt;

		@ApiModelProperty(notes = "project programming language", example = "c++")
		private String langList;

		@ApiModelProperty(notes = "lines of code in project", example = "100")
		private Integer lineCount;

		@ApiModelProperty(notes = "number of files in project", example = "10")
		private Integer fileCount;

		@ApiModelProperty(notes = "scan task uuid", example = "8902f9bf-2b33-489e-8c62-99a2523b83d7")
		private String scanTaskId;

		@ApiModelProperty(notes = "project risk level", example = "HIGH")
		private String risk;

		@ApiModelProperty(notes = "number of scan issues", example = "56")
		private Integer issueCount;

		private Map<String, String> baselineRuleSetCount = new HashMap<>();

		@ApiModelProperty(notes = "number of baseline scan issues", example = "27")
		private Integer baselineIssueCount;

		private Map<String, RuleSet> ruleSetSummaryMap = new HashMap<>();
	}


	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@ApiModel(description = "ScanTask")
	public static class ScanTask  {

		@ApiModelProperty(notes = "scan task uuid", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e", required = true)
		UUID id;

		@ApiModelProperty(notes = "scan task status", example = "COMPLETED", required = true)
		com.xcal.api.entity.ScanTask.Status status;

		@ApiModelProperty(notes = "scan start time", example = "1656919796670", required = true)
		Date scanStartAt;

		@ApiModelProperty(notes = "scan end time", example = "1656919796670", required = true)
		Date scanEndAt;

	}
}
