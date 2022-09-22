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
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="file_info")
public class FileInfo {

    public enum Type {
        LIB,
        SOURCE,
        TEMP
    }

    public enum Status{
        PENDING, ACTIVE, DELETED
    }
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_storage_id")
    @ToString.Exclude
    FileStorage fileStorage;

    @Column(name = "name")
    String name;

    @Column(name = "file_storage_extra_info")
    String fileStorageExtraInfo;

    @Column(name = "relative_path")
    String relativePath;

    @Column(name = "version")
    String version;

    @Column(name = "checksum")
    String checksum;

    @Column(name = "file_size")
    Long fileSize;

    @Column(name = "no_of_lines")
    Integer noOfLines;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    Type type;

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

    @OneToMany(mappedBy = "fileInfo")
    List<ScanFile> scanFiles;
}
