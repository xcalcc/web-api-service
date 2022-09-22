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
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "issue_diff_v2")
public class IssueDiff {

    public enum Type {
        NEW, FIXED, NEW_PATH, FIXED_PATH
    }

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scan_task_id")
    @ToString.Exclude
    ScanTask scanTask;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "baseline_scan_task_id")
    @ToString.Exclude
    ScanTask baselineScanTask;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "issue_id")
    @ToString.Exclude
    Issue issue;

    @Column(name = "baseline_issue_key")
    String baselineIssueKey;

    @Column(name = "issue_key")
    String issueKey;

    @Column(name = "checksum")
    String checksum;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    IssueDiff.Type type;

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
