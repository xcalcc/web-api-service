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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true) //toBuilder support create builder from object
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "External Csv Report request", description = "request object for getting external csv report")
public class ExternalCsvReportRequest {

	private String projectId;

	private String commitId;

	private List<String> criticalities;

	private List<String> defectTypes;

	private List<String> dsrType;

	private String searchValue;

	private List<String> ruleSetAndStandardNames;


}
