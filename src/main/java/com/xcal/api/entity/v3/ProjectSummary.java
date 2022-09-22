package com.xcal.api.entity.v3;

import com.xcal.api.entity.ProjectConfig;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "project")
public class ProjectSummary {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(columnDefinition = "BINARY(16)")
	private UUID id;

	@Column(name = "project_id")
	private String projectId;

	@Column(name = "name")
	private String name;

	@Column(name = "status")
	private String status;

	@Column(name = "need_dsr")
	Boolean needDsr;

	@Column(name = "scan_mode")
	String scanMode;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn;

	@Column(name = "modified_by")
	private String modifiedBy;

	@Column(name = "modified_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedOn;

	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "project_summary", joinColumns = @JoinColumn(name = "project_id"))
	@Builder.Default
	private Map<String, String> summary = new HashMap<>();

	@OneToOne ( mappedBy = "project", fetch = FetchType.EAGER)
	@ToString.Exclude
	ProjectConfig projectConfig;


}
