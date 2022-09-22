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
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "issue_v2")
public class Issue implements Serializable {

    public enum Action {
        PENDING, OPEN, CONFIRMED, FALSE_POSITIVE, WAIVED, CRITICAL
    }

    public enum Status {
        PENDING, ACTIVE, DELETED
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, RANK_1, RANK_2, RANK_3, RANK_4, RANK_5, RANK_6, RANK_7, RANK_8, RANK_9,
        RANK_10, RANK_11, RANK_12, RANK_13, RANK_14, RANK_15, RANK_16, RANK_17, RANK_18, RANK_19, RANK_20
    }


    public enum Criticality {
        LOW("LOW", "L", 1,3),
        MEDIUM("MEDIUM", "M", 4,6),
        HIGH("HIGH", "H", 7,9);

        public final String longName;
        public final String shortName;
        public final Integer lowerVal;
        public final Integer upperVal;

        Criticality(String longName, String shortName, Integer lowerVal, Integer upperVal){
            this.longName = longName;
            this.shortName = shortName;
            this.lowerVal = lowerVal;
            this.upperVal = upperVal;

        }

        public static Criticality getByIntValue(Integer intVal){
            if(intVal==null){
                return HIGH;
            }else if(intVal>=LOW.lowerVal && intVal<=LOW.upperVal){
                return LOW;
            }else if(intVal>=MEDIUM.lowerVal && intVal<=MEDIUM.upperVal){
                return MEDIUM;
            }else if(intVal>=HIGH.lowerVal && intVal<=HIGH.upperVal){
                return HIGH;
            }else{
                //other
                return HIGH;
            }
        }

        public static Criticality getByShortName(String shortName){
            if(shortName==null){
                return HIGH;
            }else if(shortName.equals(LOW.shortName)){
                return LOW;
            }else if(shortName.equals(MEDIUM.shortName)){
                return MEDIUM;
            }else if(shortName.equals(HIGH.shortName)){
                return HIGH;
            }else{
                //other
                return HIGH;
            }
        }

    }

    public enum ObligationLevel {
        ADVISORY, REQUIRED, MANDATORY;
        public static ObligationLevel getByIntValue(Integer intVal){
            if(intVal==null){
                return MANDATORY;
            }else if(intVal>=1 && intVal<=3){
                return ADVISORY;
            }else if(intVal>=4 && intVal<=6){
                return REQUIRED;
            }else if(intVal>=7 && intVal<=9){
                return MANDATORY;
            }else{
                //other
                return MANDATORY;
            }
        }
    }


    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @Column(name = "issue_key")
    String issueKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_information_id")
    RuleInformation ruleInformation;

    @Column(name = "issue_code")
    String issueCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scan_task_id")
    @ToString.Exclude
    ScanTask scanTask;

    @Column(name = "seq")
    String seq;

    @Column(name = "severity")
    @Enumerated(EnumType.STRING)
    Severity severity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scan_file_id")
    ScanFile scanFile;

    @Column(name = "file_path")
    String filePath;

    @Column(name = "line_no")
    Integer lineNo;

    @Column(name = "column_no")
    Integer columnNo;

    @Column(name = "function_name")
    String functionName;

    @Column(name = "variable_name")
    String variableName;

    @Column(name = "complexity")
    String complexity;

    @Column(name = "checksum")
    String checksum;

    @Column(name = "message")
    String message;

    @Column(name = "ignored")
    String ignored;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    Action action;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assign_to")
    User assignTo;

    @OneToMany(mappedBy = "issue", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @Builder.Default
    List<IssueAttribute> attributes = new ArrayList<>();

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

    @OneToMany(mappedBy = "issue", cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH}, fetch = FetchType.LAZY)
    @Builder.Default
    List<IssueTrace> issueTraces = new ArrayList<>();

    public Optional<IssueAttribute> getFirstAttribute(VariableUtil.IssueAttributeName attribute) {
        return this.getAttributes().stream().filter(issueAttribute -> attribute == issueAttribute.name).findFirst();
    }

    public List<IssueAttribute> getAttributes(VariableUtil.IssueAttributeName attribute) {
        return this.getAttributes().stream().filter(issueAttribute -> attribute == issueAttribute.name).collect(Collectors.toList());
    }

}
