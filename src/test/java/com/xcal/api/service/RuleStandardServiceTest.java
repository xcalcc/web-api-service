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

package com.xcal.api.service;

import com.xcal.api.entity.RuleStandard;
import com.xcal.api.entity.RuleStandardAttribute;
import com.xcal.api.entity.RuleStandardSet;
import com.xcal.api.model.dto.RuleStandardDto;
import com.xcal.api.repository.RuleStandardRepository;
import com.xcal.api.repository.RuleStandardSetRepository;
import com.xcal.api.util.VariableUtil.RuleAttributeTypeName.Type;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Slf4j
class RuleStandardServiceTest {
    private RuleStandardService ruleStandardService;
    private RuleStandardRepository ruleStandardRepository;
    private RuleStandardSetRepository ruleStandardSetRepository;
    private I18nService i18nService;

    private final RuleStandardSet ruleStandardSet1 = RuleStandardSet.builder()
            .id(UUID.randomUUID())
            .name("SET_1")
            .displayName("Test set 1")
            .description("Rule standard test set 1")
            .url("https://url1/")
            .language("c,c++,java")
            .license("Temp License")
            .provider("Temp Provider")
            .version("1")
            .createdBy("system")
            .modifiedBy("system")
            .build();

    private final RuleStandardSet ruleStandardSet2 = RuleStandardSet.builder()
            .id(UUID.randomUUID())
            .name("SET_2")
            .displayName("Test set 2")
            .description("Rule standard test set 2")
            .url("https://url2/")
            .language("c,c++,java")
            .license("Temp License")
            .provider("Temp Provider")
            .version("1")
            .createdBy("system")
            .modifiedBy("system")
            .build();

    private final RuleStandard ruleStandard11 = RuleStandard.builder()
            .ruleStandardSet(ruleStandardSet1)
            .id(UUID.randomUUID())
            .name("RuleStandard11")
            .category("TEST")
            .description("Test rule standard description 1-1")
            .attributes(Arrays.asList(
                    RuleStandardAttribute.builder().type(Type.BASIC).name("ATTR_1").value("1").build(),
                    RuleStandardAttribute.builder().type(Type.BASIC).name("ATTR_2").value("2").build()))
            .code("STD_11")
            .detail("Standard detail 1-1")
            .description("Standard description 1-1")
            .createdBy("system")
            .modifiedBy("system")
            .build();

    private final RuleStandard ruleStandard12 = RuleStandard.builder()
            .ruleStandardSet(ruleStandardSet1)
            .id(UUID.randomUUID())
            .name("RuleStandard11")
            .category("TEST")
            .description("Test rule standard description 1-2")
            .attributes(Arrays.asList(
                    RuleStandardAttribute.builder().type(Type.BASIC).name("ATTR_1").value("1").build(),
                    RuleStandardAttribute.builder().type(Type.BASIC).name("ATTR_2").value("21").build(),
                    RuleStandardAttribute.builder().type(Type.BASIC).name("ATTR_2").value("23").build()))
            .code("STD_12")
            .detail("Standard detail 1-2")
            .description("Standard description 1-2")
            .createdBy("system")
            .modifiedBy("system")
            .build();

    private final RuleStandard ruleStandard21 = RuleStandard.builder()
            .ruleStandardSet(ruleStandardSet2)
            .id(UUID.randomUUID())
            .name("RuleStandard21")
            .category("TEST")
            .description("Test rule standard description 2-1")
            .attributes(Arrays.asList(
                    RuleStandardAttribute.builder().type(Type.BASIC).name("ATTR_2").value("21").build(),
                    RuleStandardAttribute.builder().type(Type.BASIC).name("ATTR_2").value("23").build()))
            .code("STD_21")
            .detail("Standard detail 2-1")
            .description("Standard description 2-1")
            .createdBy("system")
            .modifiedBy("system")
            .build();

    @BeforeEach
    void setUp() {
        ruleStandardRepository = mock(RuleStandardRepository.class);
        ruleStandardSetRepository = mock(RuleStandardSetRepository.class);
        i18nService = mock(I18nService.class);
        UserService userService = mock(UserService.class);
        ruleStandardService = new RuleStandardService(ruleStandardRepository, ruleStandardSetRepository, userService, i18nService);
    }

    void convertRuleStandardListToDto_Success() {

    }

    void convertRuleStandardToDto_Success() {

    }





    @Test
    void findById_Success() {
        log.info("[findById_Success]");
        when(this.ruleStandardRepository.findById(ruleStandard11.getId())).thenReturn(Optional.of(ruleStandard11));
        Optional<RuleStandard> ruleStandardOptional = this.ruleStandardService.findById(ruleStandard11.getId());
        assertTrue(ruleStandardOptional.isPresent());
        assertEquals(ruleStandard11.getCode(), ruleStandardOptional.get().getCode());
        assertEquals(ruleStandard11.getCategory(), ruleStandardOptional.get().getCategory());
        assertEquals(ruleStandard11.getDescription(), ruleStandardOptional.get().getDescription());
        assertEquals(ruleStandard11.getDetail(), ruleStandardOptional.get().getDetail());
        assertEquals(ruleStandard11.getLanguage(), ruleStandardOptional.get().getLanguage());
        assertEquals(ruleStandard11.getId(), ruleStandardOptional.get().getId());
    }

    @Test
    void findById_NotFound() {
        log.info("[findById_NotFound]");
        when(this.ruleStandardRepository.findById(ruleStandard11.getId())).thenReturn(Optional.empty());
        Optional<RuleStandard> ruleStandardOptional = this.ruleStandardService.findById(ruleStandard11.getId());
        assertFalse(ruleStandardOptional.isPresent());
    }

    @Test
    void findByIds_Success() {
        log.info("[findByIds_Success]");
        when(this.ruleStandardRepository.findByIdIn(anyList())).thenReturn(Arrays.asList(ruleStandard11, ruleStandard21));
        List<RuleStandard> result = this.ruleStandardService.findByIds(Arrays.asList(ruleStandard11.getId(), ruleStandard21.getId()));
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(rs -> StringUtils.equalsIgnoreCase(ruleStandard11.getCode(), rs.getCode()) &&
                StringUtils.equalsIgnoreCase(ruleStandard11.getCategory(), rs.getCategory()) &&
                StringUtils.equalsIgnoreCase(ruleStandard11.getDescription(), rs.getDescription()) &&
                StringUtils.equalsIgnoreCase(ruleStandard11.getDetail(), rs.getDetail()) &&
                StringUtils.equalsIgnoreCase(ruleStandard11.getMessageTemplate(), rs.getMessageTemplate()) &&
                ruleStandard11.getId() == rs.getId()));
        assertTrue(result.stream().anyMatch(rs -> StringUtils.equalsIgnoreCase(ruleStandard21.getCode(), rs.getCode()) &&
                StringUtils.equalsIgnoreCase(ruleStandard21.getCategory(), rs.getCategory()) &&
                StringUtils.equalsIgnoreCase(ruleStandard21.getDescription(), rs.getDescription()) &&
                StringUtils.equalsIgnoreCase(ruleStandard21.getDetail(), rs.getDetail()) &&
                StringUtils.equalsIgnoreCase(ruleStandard21.getMessageTemplate(), rs.getMessageTemplate()) &&
                ruleStandard21.getId() == rs.getId()));
    }

    @Test
    void findByRuleStandardSet_Success() {
        log.info("[findByRuleStandardSet_Success]");
        when(this.ruleStandardRepository.findByRuleStandardSet(argThat(rs -> rs.getId() == ruleStandardSet1.getId()))).thenReturn(Arrays.asList(ruleStandard11, ruleStandard12));
        List<RuleStandard> result = this.ruleStandardService.findByRuleStandardSet(ruleStandardSet1);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(rs -> StringUtils.equalsIgnoreCase(ruleStandard11.getCode(), rs.getCode()) &&
                StringUtils.equalsIgnoreCase(ruleStandard11.getCategory(), rs.getCategory()) &&
                StringUtils.equalsIgnoreCase(ruleStandard11.getDescription(), rs.getDescription()) &&
                StringUtils.equalsIgnoreCase(ruleStandard11.getDetail(), rs.getDetail()) &&
                StringUtils.equalsIgnoreCase(ruleStandard11.getMessageTemplate(), rs.getMessageTemplate()) &&
                ruleStandard11.getId() == rs.getId()));
        assertTrue(result.stream().anyMatch(rs -> StringUtils.equalsIgnoreCase(ruleStandard12.getCode(), rs.getCode()) &&
                StringUtils.equalsIgnoreCase(ruleStandard12.getCategory(), rs.getCategory()) &&
                StringUtils.equalsIgnoreCase(ruleStandard12.getDescription(), rs.getDescription()) &&
                StringUtils.equalsIgnoreCase(ruleStandard12.getDetail(), rs.getDetail()) &&
                StringUtils.equalsIgnoreCase(ruleStandard12.getMessageTemplate(), rs.getMessageTemplate()) &&
                ruleStandard12.getId() == rs.getId()));
    }

    @Test
    void findRuleStandardSetByNameAndVersion_haveRuleStandardSetVersion_Success() {
        RuleStandardSet ruleStandardSet=RuleStandardSet.builder().build();
        doReturn(Optional.of(ruleStandardSet)).when(ruleStandardSetRepository).findByNameAndVersion(any(),any());
        Optional<RuleStandardSet> optionalRuleStandardSet=ruleStandardService.findRuleStandardSetByNameAndVersion("","NPD");
        assertTrue(optionalRuleStandardSet.isPresent());
        assertEquals(ruleStandardSet,optionalRuleStandardSet.get());
    }

    @Test
    void findRuleStandardSetByNameAndVersion_noRuleStandardSetVersion_Success() {
        RuleStandardSet ruleStandardSet=RuleStandardSet.builder().build();
        doReturn(Optional.of(ruleStandardSet)).when(ruleStandardSetRepository).findByName(any());
        Optional<RuleStandardSet> optionalRuleStandardSet=ruleStandardService.findRuleStandardSetByNameAndVersion("","");
        assertTrue(optionalRuleStandardSet.isPresent());
        assertEquals(ruleStandardSet,optionalRuleStandardSet.get());
    }

    @Test
    void findRuleStandardSetByNameAndVersion_ExceptionOnfindByNameAndVersion_Exception() {
        RuleStandardSet ruleStandardSet=RuleStandardSet.builder().build();
        doThrow(new RuntimeException()).when(ruleStandardSetRepository).findByNameAndVersion(any(),any());
        assertThrows(Exception.class,()->ruleStandardService.findRuleStandardSetByNameAndVersion("","NPD"));
    }

    @Test
    void findRuleStandardSetByNameAndVersion_ExceptionOnfindByName_Exception() {
        RuleStandardSet ruleStandardSet=RuleStandardSet.builder().build();
        doThrow(new RuntimeException()).when(ruleStandardSetRepository).findByName(any());
        assertThrows(Exception.class,()->ruleStandardService.findRuleStandardSetByNameAndVersion("",""));
    }

    @Test
    void findRuleStandardSetById_Success() {
        RuleStandardSet ruleStandardSet=RuleStandardSet.builder().build();
        doReturn(Optional.of(ruleStandardSet)).when(ruleStandardSetRepository).findById(any());
        Optional<RuleStandardSet> optionalRuleStandardSet =ruleStandardService.findRuleStandardSetById(UUID.randomUUID());
        assertEquals(ruleStandardSet,optionalRuleStandardSet.get());
    }

    @Test
    void findRuleStandardSetById_Exception_Exception() {
        assertThrows(Exception.class,()->{
            doThrow(new RuntimeException()).when(ruleStandardSetRepository).findById(any());
            ruleStandardService.findRuleStandardSetById(UUID.randomUUID());
        });
    }

    @Test
    void findAllRuleStandardSet_Success() {
        Page<RuleStandardSet> resultRuleStandardSet=new PageImpl(Arrays.asList(RuleStandardSet.builder().build()));
        doReturn(resultRuleStandardSet).when(ruleStandardSetRepository).findAll(any(Pageable.class));
        Page<RuleStandardSet> actualRuleStandardSet=ruleStandardService.findAllRuleStandardSet(PageRequest.of(0, 20));
        assertEquals(resultRuleStandardSet,actualRuleStandardSet);
    }

    @Test
    void findAllRuleStandardSet_Exception_Exception() {
        assertThrows(Exception.class,()->{
            doThrow(new RuntimeException()).when(ruleStandardSetRepository).findAll(any(Pageable.class));
            ruleStandardService.findAllRuleStandardSet(PageRequest.of(0, 20));
        });
    }

    @Test
    void findAllRuleStandard_Success() {
        Page<RuleStandard> resultRuleStandardSet=new PageImpl(Arrays.asList(RuleStandard.builder().build()));
        doReturn(resultRuleStandardSet).when(ruleStandardRepository).findAll(any(Pageable.class));
        Page<RuleStandard> actualRuleStandard =ruleStandardService.findAllRuleStandard(PageRequest.of(0, 20));
        assertEquals(resultRuleStandardSet,actualRuleStandard);
    }

    @Test
    void SetfindAllRuleStandard_Exception_Exception() {
        assertThrows(Exception.class,()->{
            doThrow(new RuntimeException()).when(ruleStandardRepository).findAll(any(Pageable.class));
            ruleStandardService.findAllRuleStandard(PageRequest.of(0, 20));
        });
    }

    @Test
    void findDtoByRuleStandardSet_Success() {
        RuleStandardSet rulStandardSet=RuleStandardSet.builder()
                .id(UUID.randomUUID())
                .name("name")
                .version("version")
                .revision("555")
                .displayName("Display name").build();
        RuleStandard ruleStandard=RuleStandard.builder()
                .id(UUID.randomUUID())
                .name("name")
                .category("category")
                .code("code")
                .language("lang")
                .url("url")
                .ruleStandardSet(rulStandardSet)
                .build();
        RuleStandardService spyRuleStandardService=spy(ruleStandardService);
        List<RuleStandard> resultRuleStandard=Arrays.asList(ruleStandard);
        doReturn(resultRuleStandard).when(spyRuleStandardService).findByRuleStandardSet(any());
        List<RuleStandardDto> actualRuleStandardDtoList =spyRuleStandardService.findDtoByRuleStandardSet(RuleStandardSet.builder().build(), Locale.ENGLISH);
        assertEquals("name",actualRuleStandardDtoList.get(0).getName());

    }

    @Test
    void findByRuleStandardSetNameAndVersion_Success() {

    }

    @Test
    void findRuleStandardSetByNameAndVersion_Success() {

    }

    @Test
    void getI18nMessagesByRuleStandardSet_Success() {

    }

    @Test
    void retrieveI18nMessageMapByRuleStandardList_Success() {

    }
}
