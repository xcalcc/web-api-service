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


import com.xcal.api.util.VariableUtil;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "project_config")
public class ProjectConfig {

    public enum Status {
        PENDING, ACTIVE, INACTIVE, DELETED, ONE_OFF
    }

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @Column(name = "name")
    String name;

    @OneToMany(mappedBy = "projectConfig", cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @Builder.Default
    List<ProjectConfigAttribute> attributes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_uuid")
    @ToString.Exclude
    Project project;

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

    public String getFirstAttributeValue(final VariableUtil.ProjectConfigAttributeTypeName typeName, final String defaultValue) {
        return this.getFirstAttribute(typeName.type, typeName.nameValue).map(ProjectConfigAttribute::getValue).orElse(defaultValue);
    }

    public Optional<ProjectConfigAttribute> getFirstAttribute(final VariableUtil.ProjectConfigAttributeTypeName typeName) {
        return this.getFirstAttribute(typeName.type, typeName.nameValue);
    }

    public Optional<ProjectConfigAttribute> getFirstAttribute(final VariableUtil.ProjectConfigAttributeTypeName.Type type, final String nameValue) {
        return this.getAttributes(type, nameValue).stream().findFirst();
    }

    public List<ProjectConfigAttribute> getAttributes(final VariableUtil.ProjectConfigAttributeTypeName typeName) {
        return this.getAttributes(typeName.type, typeName.nameValue);
    }

    public List<ProjectConfigAttribute> getAttributes(final VariableUtil.ProjectConfigAttributeTypeName.Type type, final String nameValue) {
        List<ProjectConfigAttribute> result;
        if (attributes == null) {
            result = new ArrayList<>();
        } else {
            result = this.attributes.stream().filter(attribute ->
                    type == attribute.getType() && StringUtils.equalsIgnoreCase(nameValue, attribute.getName()))
                    .collect(Collectors.toList());
        }
        return result;
    }

    public void addAttribute(VariableUtil.ProjectConfigAttributeTypeName.Type type, VariableUtil.ProjectConfigAttributeTypeName name , final String value){
        attributes.add(ProjectConfigAttribute.builder().type(type).name(name.nameValue).value(value).build());
    }
}
