package com.xcal.api.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanTaskLog {

	private UUID scanTaskId;

	private Date scanStartAt;

	private Date scanEndAt;

	private String status;

	private String commitId;

	private String baselineCommitId;

	// Repository action that support CI and CD
	private String repoAction;

	private int newCount;

	private int fixedCount;

	private String buildInfo;

}
