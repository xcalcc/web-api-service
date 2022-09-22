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

package com.xcal.api.entiry.converter;

import com.xcal.api.entity.RuleInformation;
import com.xcal.api.entity.converter.PriorityConverter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class PriorityConverterTest {
    @BeforeEach
    void setup() {

    }

    @Test
    void convertToDatabaseColumn_NotNull_Success() {
        log.info("[convertToDatabaseColumn_NotNull_Success]");
        PriorityConverter converter = new PriorityConverter();
        String valueHigh = converter.convertToDatabaseColumn(RuleInformation.Priority.HIGH);
        Assertions.assertEquals("1", valueHigh);

        String valueMedium = converter.convertToDatabaseColumn(RuleInformation.Priority.MEDIUM);
        Assertions.assertEquals("2", valueMedium);

        String valueLow = converter.convertToDatabaseColumn(RuleInformation.Priority.LOW);
        Assertions.assertEquals("3", valueLow);
    }

    @Test
    void convertToDatabaseColumn_Null_Success() {
        log.info("[convertToDatabaseColumn_Null_Success]");
        PriorityConverter converter = new PriorityConverter();
        String value = converter.convertToDatabaseColumn(null);
        Assertions.assertNull(value);
    }

    @Test
    void convertToEntityAttribute_NotNull_Success() {
        log.info("[convertToEntityAttribute_NotNull_Success]");
        PriorityConverter converter = new PriorityConverter();
        RuleInformation.Priority severityHigh = converter.convertToEntityAttribute("1");
        Assertions.assertEquals(RuleInformation.Priority.HIGH, severityHigh);

        RuleInformation.Priority severityMedium = converter.convertToEntityAttribute("2");
        Assertions.assertEquals(RuleInformation.Priority.MEDIUM, severityMedium);

        RuleInformation.Priority severityLow = converter.convertToEntityAttribute("3");
        Assertions.assertEquals(RuleInformation.Priority.LOW, severityLow);

    }

    @Test
    void convertToEntityAttribute_Null_Success() {
        log.info("[convertToEntityAttribute_Null_Success]");
        PriorityConverter converter = new PriorityConverter();
        RuleInformation.Priority severity = converter.convertToEntityAttribute(null);
        Assertions.assertNull(severity);
    }

}
