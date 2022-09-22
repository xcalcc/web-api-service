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
public class IssueGroup {

	private String id;

	/***
	 * The project this issue group belongs to
	 */

	private UUID projectId;

	/***
	 * The selected scan task
	 */

	private UUID scanTaskId;

	/** The id of scan task which the issue group occurred the first time.
	 * It may not be required for current product features and kept now for backward compatibility
	 * But please consider coming features before removing the field.
	 */
	private UUID occurScanTaskId;

	private Date occurTime;

	/** The id of scan task which the issue group was fixed.
	 * It may not be required for current product features and kept now for backward compatibility
	 * But please consider coming features before removing the field.
	 */
	private UUID fixedScanTaskId;

	private Date fixedTime;

	private String ruleCode;

	private String ruleSet;


	/***
	 * Data for source node
	 */
	private Integer srcFilePathId;

	private String srcFilePath;

	private String srcRelativePath;

	private Integer srcLineNo;

	private Integer srcColumnNo;

	private Integer srcMessageId;


	/***
	 * Data for sink node
	 */
	private Integer sinkFilePathId;

	private String sinkFilePath;

	private String sinkRelativePath;

	private Integer sinkLineNo;

	private Integer sinkColumnNo;

	private Integer sinkMessageId;

	/***
	 * Location information
	 */

	private Integer functionNameId;

	private String functionName;

	private Integer variableNameId;

	private String variableName;

	/***
	 * Metrics of the issue group
	 */
	private String severity;

	private String likelihood;

	private String remediationCost;

	private Integer complexity;

	/***
	 * TODO: This field is not used already, please remove on refactoring
	 */
	@Deprecated
	private String priority;

	/***
	 * This field shows whether the issue group is "D" which means Definite or "M" as May be
	 */
	private String certainty;

	/***
	 * The criticality value from 1-9
	 * This field is used as "risk" in UI
	 */
	private Integer criticality;

	/***
	 * The criticality level which can be "H", "M", "L" which represent high(7-9), medium(4-6) and low(1-3)
	 * This field is used as "risk" in UI
	 */
	private String criticalityLevel;

	/***
	 * Category of issue group. eg. "Vul" as Vulnerability
	 */
	private String category;

	/***
	 * How many issues belong to this issue group
	 */
	private Integer issueCount;

	/***
	 * Average count of issue trace
	 */
	private Integer avgTraceCount;

	/***
	 * Status of issue group eg. "ACTIVE"
	 */
	private String status;

	/***
	 * The DSR state which can be "N" for new, "E" for existing, "L" for line number change, "F" for fixed
	 */
	private String dsr;

	/***
	 * Assignee information
	 */
	private UUID assigneeId;

	private String assigneeDisplayName;

	private String assigneeEmail;

}
