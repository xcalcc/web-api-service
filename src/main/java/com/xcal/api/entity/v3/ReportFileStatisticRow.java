package com.xcal.api.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportFileStatisticRow {

	private String path;

	private int counts;

	private float percentage;

	private CriticalityBreakDown criticalityBreakDown;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CriticalityBreakDown {
		private Integer high;
		private Integer medium;
		private Integer low;
	}
}
