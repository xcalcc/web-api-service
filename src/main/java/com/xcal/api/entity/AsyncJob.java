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

package com.xcal.api.entity;

import com.xcal.api.model.payload.UpdateScanTaskRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "async_job")
public class AsyncJob implements Serializable {

	public enum Status {
		CREATED, RUNNING, COMPLETED, FAILED
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class IssueJobInfo {

		private String username;

		private String scanTask;

		private String baselineScanTask;

		private String issueFile;

		private String fixedIssueFile;

		private String newIssueFile;

		private UpdateScanTaskRequest updateScanTaskRequest;

		private Integer step;

	}

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(columnDefinition = "BINARY(16)")
	UUID id;

	@Column(name = "name")
	String name;

	@Column(name = "info")
	String info;

	@Column(name = "result")
	String result;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	Status status;

	@Column(name = "created_by")
	String createdBy;

	@Column(name = "created_on")
	@Temporal(TemporalType.TIMESTAMP)
	Date createdOn;

	@Column(name = "modified_by")
	String modifiedBy;

	@Column(name = "modified_on")
	@Temporal(TemporalType.TIMESTAMP)
	Date modifiedOn;

}
