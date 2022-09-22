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
public class PriorityConverter implements AttributeConverter<RuleInformation.Priority, String> {

    @Override
    public String convertToDatabaseColumn(RuleInformation.Priority priority) {
        String value = null;
        if (priority != null) {
            value = String.valueOf(priority.getValue());
        }
        return value;
    }

    @Override
    public RuleInformation.Priority convertToEntityAttribute(String value) {
        RuleInformation.Priority result = null;
        if (value != null) {
            result = Stream.of(RuleInformation.Priority.values())
                    .filter(priority -> StringUtils.equalsIgnoreCase(String.valueOf(priority.getValue()),value))
                    .findFirst().orElse(null);
        }
        return result;
    }
}
