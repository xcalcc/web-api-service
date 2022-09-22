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

package com.xcal.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommonUtilTest {

    @Test
    void formatMsgInSquareBrackets_NoBrackets_ReturnResultWithBrackets() {
        String originalMsg = "test";
        String result = "[test]";
        assertEquals(result, CommonUtil.formatMsgInSquareBrackets(originalMsg));
    }

    @Test
    void formatMsgInSquareBrackets_IfContainBrackets_ReturnOriginalMsg() {
        String originalMsg = "a[test]b";
        String result = "a[test]b";
        assertEquals(result, CommonUtil.formatMsgInSquareBrackets(originalMsg));
    }

    @Test
    void writeObjectToJsonStringSilently_InputObject_InternalJsonProcessingException_NoException() throws JsonProcessingException {
        ObjectMapper om = mock(ObjectMapper.class);
        when(om.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
        User user = User.builder().id(UUID.randomUUID()).username("test").build();
        String result = CommonUtil.writeObjectToJsonStringSilently(om, user);
        assertEquals("{}", result);
    }

    @Test
    void writeObjectToJsonStringSilently_InputString_InternalJsonProcessingException_NoException() throws JsonProcessingException {
        ObjectMapper om = mock(ObjectMapper.class);
        when(om.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
        String result = CommonUtil.writeObjectToJsonStringSilently(om, "dummyString");
        assertEquals("", result);
    }
}