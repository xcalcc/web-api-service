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
@Table(name="file_storage")
public class FileStorage {

    public enum Type {
        GIT, SVN, VOLUME, GITLAB, GITHUB, AGENT, GERRIT, GITLAB_V3
    }
    public enum Status{
        PENDING, ACTIVE, INACTIVE, DELETED
    }
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @Column(name = "name", unique = true)
    String name;

    @Column(name = "description")
    String description;

    /**
     * Git, SVN, fs, nfs, volume
     */
    @Column(name = "file_storage_type")
    @Enumerated(EnumType.STRING)
    Type fileStorageType;

    /**
     * when type is fs or nfs, the host is the path of the root
     */
    @Column(name = "file_storage_host")
    String fileStorageHost;

    @Column(name = "credential_type")
    String credentialType;

    @Column(name = "credential")
    @ToString.Exclude
    String credential;

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
