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

package com.xcal.api.dto;

import com.xcal.api.model.dto.IssueDto;
import com.xcal.api.util.VariableUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
class IssueDtoTest {
    @BeforeEach
    void setup() {

    }

    @Test
    void getFirstAttribute_Success() {
        log.info("[getFirstAttribute_Success]");
        IssueDto issueDto = IssueDto.builder().issueAttributes(Arrays.asList(
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.SEVERITY).value("HIGH").build(),
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value("TEST_CODE").build(),
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CATEGORY).value("TEST_CATEGORY").build(),
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CERTAINTY).value("M").build()
        )).build();

        Optional<IssueDto.IssueAttribute> attributeOptional = issueDto.getFirstAttribute(VariableUtil.IssueAttributeName.SEVERITY);
        Assertions.assertTrue(attributeOptional.isPresent());
        Assertions.assertEquals("HIGH", attributeOptional.get().getValue());
    }

    @Test
    void getFirstAttribute_NotExist_Success() {
        log.info("[getFirstAttribute_NotExist_Success]");
        IssueDto issueDto = IssueDto.builder().issueAttributes(Arrays.asList(
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.SEVERITY).value("HIGH").build(),
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value("TEST_CODE").build(),
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CATEGORY).value("TEST_CATEGORY").build(),
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CERTAINTY).value("M").build()
        )).build();
        Assertions.assertFalse(issueDto.getFirstAttribute(VariableUtil.IssueAttributeName.LIKELIHOOD).isPresent());
    }

    @Test
    void getFirstAttribute_MultipleAttributeGetFirst_Success() {
        log.info("[getFirstAttribute_MultipleAttributeGetFirst_Success]");
        IssueDto issueDto = IssueDto.builder().issueAttributes(Arrays.asList(
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CATEGORY).value("TEST_CATEGORY").build(),
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value("TEST_CODE2").build(),
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.RULE_CODE).value("TEST_CODE").build(),
                IssueDto.IssueAttribute.builder().name(VariableUtil.IssueAttributeName.CERTAINTY).value("M").build()
        )).build();
        Assertions.assertEquals("TEST_CODE2", issueDto.getFirstAttribute(VariableUtil.IssueAttributeName.RULE_CODE).map(IssueDto.IssueAttribute::getValue).get());
    }
}
