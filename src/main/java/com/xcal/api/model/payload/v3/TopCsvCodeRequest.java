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

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Top Csv Code Request", description = "Top Csv Code Request")
public class TopCsvCodeRequest {

	@ApiModelProperty(notes = "project uuid", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e")
	private UUID projectId;

	@ApiModelProperty(notes = "scan task uuid", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e")
	private UUID scanTaskId;

	@ApiModelProperty(notes = "rule code list. rule code is used to mark which rule the issues violate", example = "[\"UIV1\", \"MSR_21_3\"]")
	private List<String> ruleCodes;

	@ApiModelProperty(notes = "rule set list. rule set is the name of the collection of rules", example = "[\"X\", \"S\"]")
	private List<String> ruleSets;

	@ApiModelProperty(notes = "dsr scan type, N means new, E means existing, P means partial change, L means line number change", example = "N")
	private String dsrType;

	@ApiModelProperty(notes = "the number of top data will be returned", example = "4")
	private Integer top;


}
