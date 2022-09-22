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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="scan_file")
public class ScanFile {

    public enum Status{
        PENDING, ACTIVE, DELETED
    }

    public enum Type {
        FILE, DIRECTORY
    }

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_task_id")
    @ToString.Exclude
    ScanTask scanTask;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "file_info_id")
    @ToString.Exclude
    FileInfo fileInfo;

    @Column(name = "project_relative_path")
    String projectRelativePath;

    @Column(name = "store_path")
    String storePath;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    Type type;

    @Column(name = "parent_path")
    String parentPath;

    // Nested set model, store left value of a node
    @Column(name = "tree_left")
    Long treeLeft;

    // Nested set model, store right value of a node
    @Column(name = "tree_right")
    Long treeRight;

    @Column(name = "depth")
    Integer depth;

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

    @Builder.Default
    @Transient
    List<ScanFile> children = new ArrayList<>();

}
