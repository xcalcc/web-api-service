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


import com.xcal.api.util.MessagesTemplate;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="project")
public class Project {

    public enum Status{
        PENDING, ACTIVE, INACTIVE, DELETED
    }

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    @Column(name = "project_id")
    String projectId;

    @Column(name = "name")
    String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "project_summary", joinColumns = @JoinColumn(name = "project_id"))
    @Builder.Default
    Map<String, String> summary = new HashMap<>();

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "need_dsr")
    Boolean needDsr;

    @Column(name = "scan_mode")
    String scanMode;

    @Column(name = "cicd_fsm_state")
    String cicdFsmState;

    @Column(name = "baseline_commit_id")
    String baselineCommitId;

    // how many scan records will be saved
    @Column(name = "retention_num")
    Integer retentionNum;

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
