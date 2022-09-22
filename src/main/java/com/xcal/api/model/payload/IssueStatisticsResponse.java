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

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Calc issue statistics response", description = "Calc issue statistics response for scan task or scan file")
public class IssueStatisticsResponse {

	@Data
	public static class RuleSetStatistics {

		Integer issueCount = 0;

		Map<String, Integer> priority = new HashMap<>();

		Map<String, RuleStatistics> rule = new HashMap<>();

		public void addPriority(String priorityName, Integer issueCount) {
			if (!this.priority.containsKey(priorityName)) {
				this.priority.put(priorityName, 0);
			}
			this.priority.put(priorityName, this.priority.get(priorityName) + issueCount);
			this.issueCount += issueCount;
		}

	}

	@Data
	public static class RuleStatistics {

		Integer issueCount = 0;

		Map<String, Integer> priority = new HashMap<>();

		public void addPriority(String priorityName, Integer issueCount) {
			if (!this.priority.containsKey(priorityName)) {
				this.priority.put(priorityName, 0);
			}
			this.priority.put(priorityName, this.priority.get(priorityName) + issueCount);
			this.issueCount += issueCount;
		}

	}

	Integer issueCount = 0;

	Map<String, RuleSetStatistics> ruleSet;

}
