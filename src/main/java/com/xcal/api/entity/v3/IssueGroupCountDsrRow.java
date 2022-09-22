package com.xcal.api.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueGroupCountDsrRow {

	private List<IssueGroupCountRow> N;

	private List<IssueGroupCountRow> E;

	private List<IssueGroupCountRow> F;

}
