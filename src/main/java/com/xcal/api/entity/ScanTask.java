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
@Table(name="scan_task")
public class ScanTask {

    public enum Status {
        PENDING, START, PROCESSING, COMPLETED, FAILED, TERMINATED
    }

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    @ToString.Exclude
    Project project;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "project_config_id")
    @ToString.Exclude
    ProjectConfig projectConfig;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "engine")
    String engine; // xcalibyte, clang, etc.

    @Column(name = "engine_version")
    String engineVersion;

    @Column(name = "scan_mode")
    String scanMode; // analyze module

    @Column(name = "source_root")
    String sourceRoot;

    @Column(name = "scan_parameters")
    String scanParameter;

    @Column(name = "scan_remarks")
    String scanRemarks;

    @Column(name = "scan_start_at")
    @Temporal(TemporalType.TIMESTAMP)
    Date scanStartAt;

    @Column(name = "scan_end_at")
    @Temporal(TemporalType.TIMESTAMP)
    Date scanEndAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name="scan_task_summary", joinColumns=@JoinColumn(name="scan_task_id"))
    @Builder.Default
    Map<String, String> summary = new HashMap<>();

    @Column(name = "house_keep_on")
    Date houseKeepOn;

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
