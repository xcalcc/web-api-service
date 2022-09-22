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

package com.xcal.api.model.payload;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
class RestResponsePageTest {
    @BeforeEach
    void setup() {

    }

    @Test
    void RestResponsePage_Constructor_Success() {
        log.info("[RestResponsePage_Constructor_Success]");
        List<String> content = new ArrayList<>(Arrays.asList("1", "2", "3", "4"));
        int numberOfElements = content.size();
        int pageNumber = 1;
        int pageSize = content.size();

        RestResponsePage<String> restResponsePage = new RestResponsePage<>(content, pageNumber, pageSize, (long) numberOfElements, null, true, 1, null, true, numberOfElements);

        Assertions.assertEquals(1, restResponsePage.getPageable().getPageNumber());
        Assertions.assertEquals(pageSize, restResponsePage.getPageable().getPageSize());
        Assertions.assertTrue(restResponsePage.getContent().contains("1"));
        Assertions.assertTrue(restResponsePage.getContent().contains("2"));
        Assertions.assertTrue(restResponsePage.getContent().contains("3"));
        Assertions.assertTrue(restResponsePage.getContent().contains("4"));
    }

}
