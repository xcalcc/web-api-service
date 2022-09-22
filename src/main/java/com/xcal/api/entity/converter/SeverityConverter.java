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

package com.xcal.api.entity.converter;

import com.xcal.api.entity.RuleInformation;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter
public class SeverityConverter implements AttributeConverter<RuleInformation.Severity, String> {

    @Override
    public String convertToDatabaseColumn(RuleInformation.Severity severity) {
        String value = null;
        if (severity != null) {
            value = String.valueOf(severity.getValue());
        }
        return value;
    }

    @Override
    public RuleInformation.Severity convertToEntityAttribute(String value) {
        RuleInformation.Severity result = null;
        if (value != null) {
            result = Stream.of(RuleInformation.Severity.values())
                    .filter(severity -> StringUtils.equalsIgnoreCase(String.valueOf(severity.getValue()),value))
                    .findFirst().orElse(null);
        }
        return result;
    }
}
