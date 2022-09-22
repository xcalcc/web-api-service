package com.xcal.api.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueGroupCountRow {

	private String criticality;

	private String certainty;

	private String ruleCode;

	private String count;

}
