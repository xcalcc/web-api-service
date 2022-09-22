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

package com.xcal.api.entiry;

import com.xcal.api.entity.Issue;
import com.xcal.api.entity.IssueAttribute;
import com.xcal.api.util.VariableUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
class IssueTest {
    @BeforeEach
    void setup() {

    }

    @Test
    void getFirstAttribute_Success() {
        log.info("[getFirstAttribute_Success]");
        Issue issue = Issue.builder().attributes((Arrays.asList(
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.SEVERITY).value("HIGH").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value("TEST_CODE").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CATEGORY).value("TEST_CATEGORY").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CERTAINTY).value("M").build()
        ))).build();

        Optional<IssueAttribute> attributeOptional = issue.getFirstAttribute(VariableUtil.IssueAttributeName.SEVERITY);
        Assertions.assertTrue(attributeOptional.isPresent());
        Assertions.assertEquals("HIGH", attributeOptional.get().getValue());
    }

    @Test
    void getFirstAttribute_NotExist_Success() {
        log.info("[getFirstAttribute_NotExist_Success]");
        Issue issue = Issue.builder().attributes(Arrays.asList(
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.SEVERITY).value("HIGH").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value("TEST_CODE").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CATEGORY).value("TEST_CATEGORY").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CERTAINTY).value("M").build()
        )).build();
        Assertions.assertFalse(issue.getFirstAttribute(VariableUtil.IssueAttributeName.LIKELIHOOD).isPresent());
    }

    @Test
    void getFirstAttribute_MultipleAttributeGetFirst_Success() {
        log.info("[getFirstAttribute_MultipleAttributeGetFirst_Success]");
        Issue issue = Issue.builder().attributes(Arrays.asList(
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CATEGORY).value("TEST_CATEGORY").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value("TEST_CODE2").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value("TEST_CODE").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CERTAINTY).value("M").build()
        )).build();
        Assertions.assertEquals("TEST_CODE2", issue.getFirstAttribute(VariableUtil.IssueAttributeName.RULE_CODE).map(IssueAttribute::getValue).get());
    }

    @Test
    void getAttributes_Success() {
        log.info("[getAttributes_Success]");
        Issue issue = Issue.builder().attributes((Arrays.asList(
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.SEVERITY).value("HIGH").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value("TEST_CODE").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value("TEST_CODE2").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CATEGORY).value("TEST_CATEGORY").build(),
                IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CERTAINTY).value("M").build()
        ))).build();

        List<IssueAttribute> attributes = issue.getAttributes(VariableUtil.IssueAttributeName.RULE_CODE);
        Assertions.assertEquals(2, attributes.size());
        Assertions.assertTrue(attributes.stream().map(IssueAttribute::getValue).anyMatch("TEST_CODE2"::equals));
        Assertions.assertTrue(attributes.stream().map(IssueAttribute::getValue).anyMatch("TEST_CODE"::equals));
    }


    /***
     * For Criticality getByValue
     */

    @Test
    void Criticality_getByIntValue_null_HIGH(){
        Assertions.assertEquals(Issue.Criticality.HIGH,Issue.Criticality.getByIntValue(null));
    }

    @Test
    void Criticality_getByIntValue_1_LOW(){
        Assertions.assertEquals(Issue.Criticality.LOW,Issue.Criticality.getByIntValue(1));
    }

    @Test
    void Criticality_getByIntValue_3_LOW(){
        Assertions.assertEquals(Issue.Criticality.LOW,Issue.Criticality.getByIntValue(3));
    }

    @Test
    void Criticality_getByIntValue_4_MEDIUM(){
        Assertions.assertEquals(Issue.Criticality.MEDIUM,Issue.Criticality.getByIntValue(4));
    }

    @Test
    void Criticality_getByIntValue_6_MEDIUM(){
        Assertions.assertEquals(Issue.Criticality.MEDIUM,Issue.Criticality.getByIntValue(6));
    }

    @Test
    void Criticality_getByIntValue_7_HIGH(){
        Assertions.assertEquals(Issue.Criticality.HIGH,Issue.Criticality.getByIntValue(7));
    }

    @Test
    void Criticality_getByIntValue_9_HIGH(){
        Assertions.assertEquals(Issue.Criticality.HIGH,Issue.Criticality.getByIntValue(9));
    }

    @Test
    void Criticality_getByIntValue_other_HIGH(){
        Assertions.assertEquals(Issue.Criticality.HIGH,Issue.Criticality.getByIntValue(20));
    }

    /***
     * For Criticality getByShortName
     */

    @Test
    void Criticality_getByShortName_null_HIGH(){
        Assertions.assertEquals(Issue.Criticality.HIGH,Issue.Criticality.getByShortName(null));
    }

    @Test
    void Criticality_getByShortName_1_LOW(){
        Assertions.assertEquals(Issue.Criticality.LOW,Issue.Criticality.getByShortName("L"));
    }

    @Test
    void Criticality_getByShortName_4_MEDIUM(){
        Assertions.assertEquals(Issue.Criticality.MEDIUM,Issue.Criticality.getByShortName("M"));
    }

    @Test
    void Criticality_getByShortName_7_HIGH(){
        Assertions.assertEquals(Issue.Criticality.HIGH,Issue.Criticality.getByShortName("H"));
    }

    @Test
    void Criticality_getByShortName_other_HIGH(){
        Assertions.assertEquals(Issue.Criticality.HIGH,Issue.Criticality.getByShortName("A"));
    }

    /***
     * For Obligation Level getByIntValue
     */


    @Test
    void ObligationLevel_getByIntValue_null_MANDATORY(){
        Assertions.assertEquals(Issue.ObligationLevel.MANDATORY,Issue.ObligationLevel.getByIntValue(null));
    }

    @Test
    void ObligationLevel_getByIntValue_1_ADVISORY(){
        Assertions.assertEquals(Issue.ObligationLevel.ADVISORY,Issue.ObligationLevel.getByIntValue(1));
    }

    @Test
    void ObligationLevel_getByIntValue_3_ADVISORY(){
        Assertions.assertEquals(Issue.ObligationLevel.ADVISORY,Issue.ObligationLevel.getByIntValue(3));
    }

    @Test
    void ObligationLevel_getByIntValue_4_REQUIRED(){
        Assertions.assertEquals(Issue.ObligationLevel.REQUIRED,Issue.ObligationLevel.getByIntValue(4));
    }

    @Test
    void ObligationLevel_getByIntValue_6_REQUIRED(){
        Assertions.assertEquals(Issue.ObligationLevel.REQUIRED,Issue.ObligationLevel.getByIntValue(6));
    }

    @Test
    void ObligationLevel_getByIntValue_7_MANDATORY(){
        Assertions.assertEquals(Issue.ObligationLevel.MANDATORY,Issue.ObligationLevel.getByIntValue(7));
    }

    @Test
    void ObligationLevel_getByIntValue_9_MANDATORY(){
        Assertions.assertEquals(Issue.ObligationLevel.MANDATORY,Issue.ObligationLevel.getByIntValue(9));
    }

    @Test
    void ObligationLevel_getByIntValue_other_MANDATORY(){
        Assertions.assertEquals(Issue.ObligationLevel.MANDATORY,Issue.ObligationLevel.getByIntValue(20));
    }

}
