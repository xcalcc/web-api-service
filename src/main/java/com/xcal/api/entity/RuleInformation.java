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


import com.xcal.api.entity.converter.PriorityConverter;
import com.xcal.api.entity.converter.SeverityConverter;
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
@Table(name = "rule_information")
public class RuleInformation {

    public enum Likelihood {
        LIKELY, PROBABLE, UNLIKELY
    }

    public enum RemediationCost {
        LOW, MEDIUM, HIGH
    }

    // normal severity is 1-HIGH,2-MEDIUM,3-LOW
    // RANK severity is for spotbugs, added 100 for difference set
    public enum Severity {
        LOW(3), MEDIUM(2), HIGH(1),
        RANK_1(101), RANK_2(102), RANK_3(103), RANK_4(104), RANK_5(105),
        RANK_6(106), RANK_7(107), RANK_8(108), RANK_9(109), RANK_10(110),
        RANK_11(111), RANK_12(112), RANK_13(113), RANK_14(114), RANK_15(115),
        RANK_16(116), RANK_17(117), RANK_18(118), RANK_19(119), RANK_20(120);

        @Getter
        Integer value;

        Severity(int value) {
            this.value = value;
        }
    }

    public enum Priority {
        LOW(3), MEDIUM(2), HIGH(1);

        @Getter
        Integer value;

        Priority(int value) {
            this.value = value;
        }
    }

    public enum Certainty {
        M, D, CONFIDENCE_1, CONFIDENCE_2, CONFIDENCE_3
    }

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "rule_set_id")
    RuleSet ruleSet;

    @Column(name = "category")
    String category;

    @Column(name = "vulnerable")
    String vulnerable;

    @Column(name = "certainty")
    @Enumerated(EnumType.STRING)
    Certainty certainty;

    @Column(name = "rule_code")
    String ruleCode;

    @Column(name = "language")
    String language;

    @Column(name = "url")
    String url;

    @Column(name = "name")
    String name;

    @Column(name = "severity")
    @Convert(converter = SeverityConverter.class)
    Severity severity;

    @Column(name = "priority")
    @Convert(converter = PriorityConverter.class)
    Priority priority;

    @Column(name = "likelihood")
    @Enumerated(EnumType.STRING)
    Likelihood likelihood;

    @Column(name = "remediation_cost")
    @Enumerated(EnumType.STRING)
    RemediationCost remediationCost;

    @Column(name = "detail")
    String detail;

    @Column(name = "description")
    String description;

    @Column(name = "msg_template")
    String messageTemplate;

    @OneToMany(mappedBy = "ruleInformation", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @Builder.Default
    List<RuleInformationAttribute> attributes = new ArrayList<>();

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

    public String getFirstAttributeValue(final VariableUtil.RuleAttributeTypeName typeName, final String defaultValue) {
        return this.getFirstAttribute(typeName.type, typeName.nameValue).map(RuleInformationAttribute::getValue).orElse(defaultValue);
    }

    public Optional<RuleInformationAttribute> getFirstAttribute(final VariableUtil.RuleAttributeTypeName typeName) {
        return this.getFirstAttribute(typeName.type, typeName.nameValue);
    }

    public Optional<RuleInformationAttribute> getFirstAttribute(final VariableUtil.RuleAttributeTypeName.Type type, final String nameValue) {
        return this.getAttributes(type, nameValue).stream().findFirst();
    }

    public List<RuleInformationAttribute> getAttributes(final VariableUtil.RuleAttributeTypeName typeName) {
        return this.getAttributes(typeName.type, typeName.nameValue);
    }

    public List<RuleInformationAttribute> getAttributes(final VariableUtil.RuleAttributeTypeName.Type type, final String nameValue) {
        List<RuleInformationAttribute> result;
        if (attributes == null) {
            result = new ArrayList<>();
        } else {
            result = this.attributes.stream().filter(attribute ->
                    type == attribute.getType() && StringUtils.equalsIgnoreCase(nameValue, attribute.getName()))
                    .collect(Collectors.toList());
        }
        return result;
    }
}
