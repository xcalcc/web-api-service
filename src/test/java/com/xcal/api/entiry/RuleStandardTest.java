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

import com.xcal.api.entity.RuleStandard;
import com.xcal.api.entity.RuleStandardAttribute;
import com.xcal.api.util.VariableUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
class RuleStandardTest {
    @BeforeEach
    void setup() {

    }

    @Test
    void getFirstAttribute_Success() {
        log.info("[getFirstAttribute_Success]");
        RuleStandard ruleStandard = RuleStandard.builder().attributes((Arrays.asList(
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2019-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2020-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.SEVERITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.SEVERITY.nameValue).value("HIGH").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.PRIORITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.PRIORITY.nameValue).value("LOW").build()
        ))).build();

        Optional<RuleStandardAttribute> attributeOptional = ruleStandard.getFirstAttribute(VariableUtil.RuleAttributeTypeName.SEVERITY);
        Assertions.assertTrue(attributeOptional.isPresent());
        Assertions.assertEquals("HIGH", attributeOptional.get().getValue());
    }

    @Test
    void getFirstRuleStandardAttribute_NotExist_Success() {
        log.info("[getFirstRuleStandardAttribute_NotExist_Success]");
        RuleStandard ruleStandard = RuleStandard.builder().attributes((Arrays.asList(
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2019-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2020-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.SEVERITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.SEVERITY.nameValue).value("HIGH").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.PRIORITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.PRIORITY.nameValue).value("LOW").build()
        ))).build();
        Assertions.assertFalse(ruleStandard.getFirstAttribute(VariableUtil.RuleAttributeTypeName.REMEDIATION_COST).isPresent());
    }

    @Test
    void getFirstRuleStandardAttribute_MultipleAttributeGetFirst_Success() {
        log.info("[getFirstRuleStandardAttribute_MultipleAttributeGetFirst_Success]");
        RuleStandard ruleStandard = RuleStandard.builder().attributes((Arrays.asList(
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2019-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2020-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.SEVERITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.SEVERITY.nameValue).value("HIGH").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.PRIORITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.PRIORITY.nameValue).value("LOW").build()
        ))).build();
        Assertions.assertEquals("2019-01", ruleStandard.getFirstAttribute(VariableUtil.RuleAttributeTypeName.OWASP).map(RuleStandardAttribute::getValue).get());
    }

    @Test
    void getRuleStandardAttributes_Success() {
        log.info("[getAttributes_Success]");
        RuleStandard ruleStandard = RuleStandard.builder().attributes((Arrays.asList(
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2019-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2020-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.SEVERITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.SEVERITY.nameValue).value("HIGH").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.PRIORITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.PRIORITY.nameValue).value("LOW").build()
        ))).build();

        List<RuleStandardAttribute> attributes = ruleStandard.getAttributes(VariableUtil.RuleAttributeTypeName.OWASP);
        Assertions.assertEquals(2, attributes.size());
        Assertions.assertTrue(attributes.stream().map(RuleStandardAttribute::getValue).anyMatch("2019-01"::equals));
        Assertions.assertTrue(attributes.stream().map(RuleStandardAttribute::getValue).anyMatch("2020-01"::equals));
    }
    @Test
    void getRuleStandardAttributes_NullAttribute() {
        log.info("[getRuleStandardAttributes_NullAttribute]");
        RuleStandard ruleStandard = RuleStandard.builder().attributes(null).build();

        List<RuleStandardAttribute> attributes = ruleStandard.getAttributes(VariableUtil.RuleAttributeTypeName.LANGUAGE);
        Assertions.assertEquals(0, attributes.size());
    }

    @Test
    void getFirstRuleStandardAttributeValue_Success() {
        log.info("[getFirstRuleStandardAttributeValue_Success]");
        RuleStandard ruleStandard = RuleStandard.builder().attributes((Arrays.asList(
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2019-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2020-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.SEVERITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.SEVERITY.nameValue).value("HIGH").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.PRIORITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.PRIORITY.nameValue).value("LOW").build()
        ))).build();

        String attribute = ruleStandard.getFirstAttributeValue(VariableUtil.RuleAttributeTypeName.SEVERITY, null);
        Assertions.assertEquals("HIGH", attribute);
    }

    @Test
    void getFirstRuleStandardAttributeValue_NotExistDefaultExist_Success() {
        log.info("[getFirstRuleStandardAttributeValue_Success]");
        RuleStandard ruleStandard = RuleStandard.builder().attributes((Arrays.asList(
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2019-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2020-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.SEVERITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.SEVERITY.nameValue).value("HIGH").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.PRIORITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.PRIORITY.nameValue).value("LOW").build()
        ))).build();

        String attribute = ruleStandard.getFirstAttributeValue(VariableUtil.RuleAttributeTypeName.REMEDIATION_COST, "NO_COMMAND");
        Assertions.assertEquals("NO_COMMAND", attribute);
    }

    @Test
    void getFirstRuleStandardAttributeValue_NotExistDefaultNull_Success() {
        log.info("[getFirstRuleStandardAttributeValue_Success]");
        RuleStandard ruleStandard = RuleStandard.builder().attributes((Arrays.asList(
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2019-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.OWASP.type)
                        .name(VariableUtil.RuleAttributeTypeName.OWASP.nameValue).value("2020-01").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.SEVERITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.SEVERITY.nameValue).value("HIGH").build(),
                RuleStandardAttribute.builder()
                        .type(VariableUtil.RuleAttributeTypeName.PRIORITY.type)
                        .name(VariableUtil.RuleAttributeTypeName.PRIORITY.nameValue).value("LOW").build()
        ))).build();

        String attribute = ruleStandard.getFirstAttributeValue(VariableUtil.RuleAttributeTypeName.REMEDIATION_COST, null);
        Assertions.assertNull(attribute);
    }
}
