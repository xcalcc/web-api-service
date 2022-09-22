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

package com.xcal.api.service.v3;

import com.xcal.api.dao.ProjectDao;
import com.xcal.api.entity.*;
import com.xcal.api.entity.v3.ProjectSummary;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.v3.ProjectSummaryDto;
import com.xcal.api.model.dto.v3.RuleInfoDto;
import com.xcal.api.model.dto.v3.RuleListResponseDto;
import com.xcal.api.repository.ProjectRepository;
import com.xcal.api.repository.ScanTaskRepository;
import com.xcal.api.repository.v3.ProjectSummaryRepository;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.UserService;
import com.xcal.api.util.VariableUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RuleServiceV3Test {

    RuleServiceV3 ruleServiceV3;
    RestTemplate restTemplate;

    String aobResponse = "{\n" +
            "    \"status\": \"success\",\n" +
            "    \"dataVersion\": {\n" +
            "        \"xcalscan_rule_version\": \"V1.92-0511\",\n" +
            "        \"copyright\": \"(C) 2021 Xcalibyte Inc.\"\n" +
            "    },\n" +
            "    \"data\": {\n" +
            "        \"master_id\": 11,\n" +
            "        \"category\": \"VUL\",\n" +
            "        \"language\": \"c,c++,java\",\n" +
            "        \"code\": \"AOB\",\n" +
            "        \"name\": \"Array out of bounds\",\n" +
            "        \"desc\": \"The program is accessing data outside the declared boundary (before or after) of the intended buffer.\",\n" +
            "        \"msg_templ\": \"In file ${si.filename}, line ${si.line}, an array out of bound has been detected for variable ${si.var} in function ${si.func}.\",\n" +
            "        \"severity\": \"H\",\n" +
            "        \"likelihood\": \"L\",\n" +
            "        \"cost\": \"H\",\n" +
            "        \"standards\": {\n" +
            "            \"owasp\": [\n" +
            "                \"A1\",\n" +
            "                \"A2\",\n" +
            "                \"A3\",\n" +
            "                \"A5\"\n" +
            "            ],\n" +
            "            \"cwe\": [\n" +
            "                \"787\",\n" +
            "                \"125\",\n" +
            "                \"121\",\n" +
            "                \"122\",\n" +
            "                \"126\"\n" +
            "            ]\n" +
            "        },\n" +
            "        \"csv_string\": [\n" +
            "            \"AOB4\",\n" +
            "            \"AOB3\",\n" +
            "            \"AOB2\",\n" +
            "            \"AOB1\",\n" +
            "            \"AOB0\"\n" +
            "        ],\n" +
            "        \"ruleSet\": {\n" +
            "            \"id\": \"X\",\n" +
            "            \"code\": \"BUILTIN\",\n" +
            "            \"displayName\": \"XCALIBYTE\"\n" +
            "        },\n" +
            "        \"details\": \"#### Abstract\\nThe program is accessing data outside (i.e. before or after) the declared boundary of the intended buffer.\\n\\n#### Explanation\\nTypically, this can allow attackers to cause a crash during program execution. A crash can occur when the code reads sensitive information from other memory locations or causable amount of data and assumes that a sentinel exists to stop the read operation, such as a NUL in a string. The expected sentinel might not be located in the out-of-bounds memory, causing excessive data to be read, leading to a segmentation fault or a buffer overflow. The software may modify an index or perform pointer arithmetic that references a memory location that is outside of the boundaries of the buffer. A subsequent read operation then produces undefined or unexpected results.\\n\",\n" +
            "        \"examples\": {\n" +
            "            \"good\": {\n" +
            "                \"general\": [\n" +
            "                    \"int assign(int* a, int i)\\n{\\n return a[i]; /* called by main\\n a only has 2 elements but i is 2 */\\n}\\n\\n#define ARR_SZ 2\\nint main() {\\n int a[ARR_SZ] = {0, 1}, b;\\n if ()\\n b = assign(a, (ARR_SZ-1)); // call assign with a and i\\n // a has two elements and i is 2\\n // a[2] is out-of-bound and also uninitialized\\n printf(\\\"value of b = %d\\\\\\\\\\\", b);\\n return 0;\\n}\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"bad\": {\n" +
            "                \"general\": [\n" +
            "                    \"int assign(int* a, int i)\\n{\\n return a[i]; /* called by main\\n a only has 2 elements but i is 2 */\\n}\\n\\nint main() {\\n int a[2] = {0, 1}, b;\\n b = assign(a, 2); // call assign with a and i\\n // a has two elements and i is 2\\n // a[2] is out-of-bound and also uninitialized\\n printf(\\\"value of b = %d\\\\\\\\\\\", b);\\n return 0;\\n}\"\n" +
            "                ]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

    String ruleInfoListResponse = "{\n" +
            "    \"status\": \"success\",\n" +
            "    \"dataVersion\": {\n" +
            "        \"xcalscan_rule_version\": \"V1.92-0511\",\n" +
            "        \"copyright\": \"(C) 2021 Xcalibyte Inc.\"\n" +
            "    },\n" +
            "    \"counts\": 127,\n" +
            "    \"rules\": [\n" +
            "        {\n" +
            "            \"master_id\": 1,\n" +
            "            \"category\": \"BAD_PRACTICE\",\n" +
            "            \"language\": \"c,c++\",\n" +
            "            \"code\": \"CSL\",\n" +
            "            \"name\": \"Call stack level exits limit\",\n" +
            "            \"desc\": \"The program has a call sequence that causes the runtime stack to exceed the call depth limit set by the user.\",\n" +
            "            \"msg_templ\": \"In file ${si.filename}, line ${si.line}, function ${si.func} has a call sequence that exceeds call level limit set from scan configuration.\",\n" +
            "            \"severity\": \"M\",\n" +
            "            \"likelihood\": \"L\",\n" +
            "            \"cost\": \"M\",\n" +
            "            \"standards\": {\n" +
            "                \"cwe\": [\n" +
            "                    \"121\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"csv_string\": [\n" +
            "                \"CSL0\"\n" +
            "            ],\n" +
            "            \"ruleSet\": {\n" +
            "                \"id\": \"X\",\n" +
            "                \"code\": \"BUILTIN\",\n" +
            "                \"displayName\": \"XCALIBYTE\"\n" +
            "            },\n" +
            "            \"details\": \"#### Abstract\\nThe program has a call sequence that causes the runtime stack to exceed the call depth limit set by the user.\\n\\n#### Explanation\\nFunction A calls another function B. Function B may in turn call another function C. This call chain can go on infinitely. In embedded systems, when the call stack is too deep, it may cause unintended side effects like running out of memory, inefficient execution time, etc.\\n\",\n" +
            "            \"examples\": {\n" +
            "                \"good\": {},\n" +
            "                \"bad\": {\n" +
            "                    \"general\": [\n" +
            "                        \"// This case does not need an example.\\n// The complete call level from function A to Z, i.e. A() ==> B() ==> .... ==> Z(), is too deep.\"\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    ],\n" +
            "    \"csvCodeMap\": {\n" +
            "        \"S02C1\": 42,\n" +
            "        \"S0200\": 42,\n" +
            "        \"A38C2\": 81,\n" +
            "        \"A38C1\": 81\n" +
            "    }\n" +
            "}";

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        this.ruleServiceV3 = new RuleServiceV3(restTemplate);


    }

    @Test
    void getRuleInfo_AOB0_Success() throws AppException, IOException {

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(aobResponse);
        RuleInfoDto ruleInfoDto = ruleServiceV3.getRuleInfo("AOB0", Locale.CHINESE);
        assertEquals("11", ruleInfoDto.getId());
        assertEquals("AOB", ruleInfoDto.getRuleCode());
        assertEquals("X", ruleInfoDto.getRuleSet().getId());
        assertEquals("BUILTIN", ruleInfoDto.getRuleSet().getName());
        assertEquals("XCALIBYTE", ruleInfoDto.getRuleSet().getDisplayName());
        assertEquals("VUL", ruleInfoDto.getCategory());
        assertEquals("c,c++,java", ruleInfoDto.getLanguage());
        assertEquals("Array out of bounds", ruleInfoDto.getName());
        assertEquals("H", ruleInfoDto.getSeverity());
        assertEquals("L", ruleInfoDto.getLikelihood());
        assertEquals("H", ruleInfoDto.getRemediationCost());
        assertEquals(4, ruleInfoDto.getStandard().get("owasp").size());
        assertEquals(5, ruleInfoDto.getStandard().get("cwe").size());
        assertEquals(5, ruleInfoDto.getCodes().size());

    }

    @Test
    void getRuleInfo_restClientException_returnNull() throws AppException, IOException {

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RestClientException(""));
        RuleInfoDto ruleInfoDto = ruleServiceV3.getRuleInfo("AOB0", Locale.CHINESE);
        assertEquals(null, ruleInfoDto);

    }

    @Test
    void getAllRuleInfo_normal_Success() throws AppException, IOException {

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(ruleInfoListResponse);
        RuleListResponseDto ruleListResponseDto = ruleServiceV3.getAllRuleInfo(Locale.CHINESE);
        assertEquals("BAD_PRACTICE", ruleListResponseDto.getRuleInfoDtoList().get(0).getCategory());
        assertEquals("c,c++", ruleListResponseDto.getRuleInfoDtoList().get(0).getLanguage());
        assertEquals("CSL", ruleListResponseDto.getRuleInfoDtoList().get(0).getRuleCode());
        assertEquals("Call stack level exits limit", ruleListResponseDto.getRuleInfoDtoList().get(0).getName());
        assertEquals("M", ruleListResponseDto.getRuleInfoDtoList().get(0).getSeverity());
        assertEquals("L", ruleListResponseDto.getRuleInfoDtoList().get(0).getLikelihood());
        assertEquals("M", ruleListResponseDto.getRuleInfoDtoList().get(0).getRemediationCost());
        assertEquals(1, ruleListResponseDto.getRuleInfoDtoList().get(0).getStandard().size());
        assertEquals(1, ruleListResponseDto.getRuleInfoDtoList().get(0).getCodes().size());
        assertEquals("X", ruleListResponseDto.getRuleInfoDtoList().get(0).getRuleSet().getId());
        assertEquals("BUILTIN", ruleListResponseDto.getRuleInfoDtoList().get(0).getRuleSet().getName());
        assertEquals("XCALIBYTE", ruleListResponseDto.getRuleInfoDtoList().get(0).getRuleSet().getDisplayName());

        assertEquals(4, ruleListResponseDto.getCsvCodeMap().size());


    }


    @Test
    void getAllRuleInfo_restClientException_returnNull() throws AppException, IOException {

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RestClientException(""));
        RuleListResponseDto ruleListResponseDto = ruleServiceV3.getAllRuleInfo(Locale.CHINESE);
        assertEquals(null, ruleListResponseDto);

    }

}
