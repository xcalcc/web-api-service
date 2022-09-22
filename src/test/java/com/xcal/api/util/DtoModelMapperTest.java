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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.WebDataBinder;

import javax.persistence.EntityManager;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

class DtoModelMapperTest {

    private DtoModelMapper dtoModelMapper;
    private EntityManager entityManager = mock(EntityManager.class);
    private ObjectMapper om = new ObjectMapper();
    private WebDataBinder webDataBinder = mock(WebDataBinder.class);

    @BeforeEach
    void setUp() {
        dtoModelMapper = new DtoModelMapper(om, entityManager);
    }

    @Test
    void validateIfApplicable() {
        doNothing().when(webDataBinder).validate();
        this.dtoModelMapper.validateIfApplicable(webDataBinder, null);
    }
}
