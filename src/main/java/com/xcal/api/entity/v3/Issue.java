package com.xcal.api.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Issue {

	private UUID id;

	private UUID scanTaskId;

	private String issueGroupId;

	private String certainty;

	private Integer traceCount;

	private String tracePath;

	private String status;

	private String dsr;

}
