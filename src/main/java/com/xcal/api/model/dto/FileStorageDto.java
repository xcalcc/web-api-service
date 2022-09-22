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

package com.xcal.api.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xcal.api.util.MessagesTemplate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "File storage")
public class FileStorageDto {
    public enum Type {
        GIT, SVN, VOLUME, GITLAB, GITHUB, AGENT, GERRIT, GITLAB_V3
    }
    public enum Status{
        PENDING, ACTIVE, INACTIVE, DELETED
    }
    @ApiModelProperty(example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", value = "ID")
    UUID id;
    @NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL)
    String name;
    Type fileStorageType;
    String fileStorageHost;
    String credentialType;

    @ToString.Exclude
    @JsonIgnore
    String credential;
    @JsonProperty
    public void setCredential(String credential) {
        this.credential = credential;
    }
    @JsonIgnore
    public String getCredential() {
        return this.credential;
    }
    Status status;
    String description;
    String createdBy;
    Date createdOn;
    String modifiedBy;
    Date modifiedOn;
}
