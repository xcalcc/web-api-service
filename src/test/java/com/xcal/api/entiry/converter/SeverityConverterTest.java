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
import com.xcal.api.entity.converter.SeverityConverter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class SeverityConverterTest {
    @BeforeEach
    void setup() {

    }

    @Test
    void convertToDatabaseColumn_NotNull_Success() {
        log.info("[convertToDatabaseColumn_NotNull_Success]");
        SeverityConverter converter = new SeverityConverter();
        String valueHigh = converter.convertToDatabaseColumn(RuleInformation.Severity.HIGH);
        Assertions.assertEquals("1", valueHigh);

        String valueMedium = converter.convertToDatabaseColumn(RuleInformation.Severity.MEDIUM);
        Assertions.assertEquals("2", valueMedium);

        String valueLow = converter.convertToDatabaseColumn(RuleInformation.Severity.LOW);
        Assertions.assertEquals("3", valueLow);
    }

    @Test
    void convertToDatabaseColumn_Null_Success() {
        log.info("[convertToDatabaseColumn_Null_Success]");
        SeverityConverter converter = new SeverityConverter();
        String value = converter.convertToDatabaseColumn(null);
        Assertions.assertNull(value);
    }

    @Test
    void convertToEntityAttribute_NotNull_Success() {
        log.info("[convertToEntityAttribute_NotNull_Success]");
        SeverityConverter converter = new SeverityConverter();
        RuleInformation.Severity severityHigh = converter.convertToEntityAttribute("1");
        Assertions.assertEquals(RuleInformation.Severity.HIGH, severityHigh);

        RuleInformation.Severity severityMedium = converter.convertToEntityAttribute("2");
        Assertions.assertEquals(RuleInformation.Severity.MEDIUM, severityMedium);

        RuleInformation.Severity severityLow = converter.convertToEntityAttribute("3");
        Assertions.assertEquals(RuleInformation.Severity.LOW, severityLow);

    }

    @Test
    void convertToEntityAttribute_Null_Success() {
        log.info("[convertToEntityAttribute_Null_Success]");
        SeverityConverter converter = new SeverityConverter();
        RuleInformation.Severity severity = converter.convertToEntityAttribute(null);
        Assertions.assertNull(severity);
    }

}
