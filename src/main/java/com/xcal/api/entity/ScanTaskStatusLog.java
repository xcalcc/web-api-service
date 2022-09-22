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
@Table(name="scan_task_status_log")
public class ScanTaskStatusLog {

    public enum Stage {
        PENDING                       (0.0,   1.0),
        PREPARE_SCAN_TASK_PIPELINE    (1.0,   1.0),
        SCAN_QUEUE_PRESCAN            (2.0,   1.0),
        PREPARE_WORKER_PIPELINE       (3.0,   1.0),
        PRE_SCAN_QUEUE                (4.0,   1.0),
        AGENT_START                   (5.0,   1.0),
        FETCH_SOURCE                  (6.0,   9.0),
        PRE_PROCESS                   (15.0,  10.0),
        COMPRESS_SOURCE_CODE          (25.0,  5.0),
        UPLOAD_SOURCE_CODE            (30.0,  5.0),
        COLLECT_FILE_INFO             (35.0,  5.0),
        UPLOAD_FILE_INFO              (40.0,  5.0),
        UPLOAD_PRE_PROCESS_INFO       (45.0,  3.0),
        AGENT_END                     (48.0,  2.0),
        SCAN_QUEUE_GET_PRESCAN_RESULT (50.0,  2.0),
        FETCH_PRE_PROCESS_INFO        (52.0,  8.0),
        SCAN_QUEUE_XVSA               (60.0,  2.0),
        SCAN_ENGINE_QUEUE             (62.0,  3.0),
        SCANNING                      (65.0,  15.0),
        SCAN_QUEUE_IMPORT_RESULT      (80.0,  10.0),
        IMPORT_FILE_INFO              (90.0,  3.0),
        IMPORT_RESULT_DIFF            (93.0,  2.0),
        IMPORT_RESULT                 (95.0,  5.0),
        SCAN_COMPLETE                 (100.0, 0.0);

        public final double start;
        public final double range;

        Stage (double start, double range) {
            this.start = start;
            this.range = range;
        }
    }

    public enum Status {
        PENDING, START, PROCESSING, COMPLETED, FAILED, TERMINATED
    }
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @ManyToOne
    @JoinColumn(name = "scan_task_id")
    @ToString.Exclude
    ScanTask scanTask;

    @Column(name = "stage")
    @Enumerated(EnumType.STRING)
    Stage stage;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "unify_error_code")
    String unifyErrorCode;

    @Column(name = "percentage")
    Double percentage;

    @Column(name = "message")
    String message;

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
