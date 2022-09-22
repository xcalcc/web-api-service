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

package com.xcal.api.controller;

import com.xcal.api.config.WithMockCustomUser;
import com.xcal.api.entity.I18nMessage;
import com.xcal.api.service.DBMessageSource;
import com.xcal.api.service.I18nService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Slf4j
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.MOCK)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
class I18nControllerTest {
    @NonNull private MockMvc mockMvc;
    @MockBean DBMessageSource messageSource;
    @MockBean I18nService i18nService;

    private I18nMessage enLocaleName = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.ENGLISH.toLanguageTag()).key("locale.name").content("English").build();
    private I18nMessage cnLocaleName = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.SIMPLIFIED_CHINESE.toLanguageTag()).key("locale.name").content("简体中文").build();
    private I18nMessage hkLocaleName = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.TRADITIONAL_CHINESE.toLanguageTag()).key("locale.name").content("繁體中文(香港)").build();
    private I18nMessage zhLocaleName = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.CHINESE.toLanguageTag()).key("locale.name").content("中文").build();

    private I18nMessage enTest = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.ENGLISH.toLanguageTag()).key("test").content("Test").build();
    private I18nMessage cnTest = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.SIMPLIFIED_CHINESE.toLanguageTag()).key("test").content("测试").build();
    private I18nMessage hkTest = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.TRADITIONAL_CHINESE.toLanguageTag()).key("test").content("測試").build();

    private I18nMessage enTest_1 = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.ENGLISH.toLanguageTag()).key("test.1").content("Test 1").build();
    private I18nMessage cnTest_1 = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.SIMPLIFIED_CHINESE.toLanguageTag()).key("test.1").content("测试一").build();
    private I18nMessage hkTest_1 = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.TRADITIONAL_CHINESE.toLanguageTag()).key("test.1").content("測試一").build();

    private I18nMessage enTest_2 = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.ENGLISH.toLanguageTag()).key("test.2").content("Test 2").build();
    private I18nMessage cnTest_2 = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.SIMPLIFIED_CHINESE.toLanguageTag()).key("test.2").content("测试二").build();
    private I18nMessage hkTest_2 = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.TRADITIONAL_CHINESE.toLanguageTag()).key("test.2").content("測試二").build();

    private I18nMessage enOnlyMessage = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.ENGLISH.toLanguageTag()).key("testABC.noChinese").content("Message with no english").build();

    private I18nMessage cnOnlyMessage = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.SIMPLIFIED_CHINESE.toLanguageTag()).key("testABC.onlyCN").content("只有简体中文").build();

    private I18nMessage englishLanguage = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.ENGLISH.toLanguageTag()).key("language").content("English Language").build();
    private I18nMessage chineseLanguage = I18nMessage.builder().id(UUID.randomUUID()).locale(Locale.CHINESE.toLanguageTag()).key("language").content("中文").build();

    private List<I18nMessage> i18nMessages;

    @BeforeEach
    void setUp() {
        i18nMessages = Arrays.asList(enLocaleName, cnLocaleName, hkLocaleName, zhLocaleName, enTest, cnTest, hkTest, enTest_1, cnTest_1, hkTest_1, enTest_2, cnTest_2, hkTest_2, enOnlyMessage, cnOnlyMessage, englishLanguage, chineseLanguage);
        when(i18nService.getI18nMessageByKeyPrefix(anyString())).thenAnswer(
                invocation -> i18nMessages.stream().filter(i18nMessage -> StringUtils.startsWith(i18nMessage.getKey(), invocation.getArgument(0))).collect(Collectors.toList()));
        when(i18nService.getI18nMessageByKeyPrefix(anyString(), isNull())).thenAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            List<I18nMessage> result;
            result = i18nMessages.stream().filter(i18nMessage -> StringUtils.startsWith(i18nMessage.getKey(), key)).collect(Collectors.toList());
            return result;
        });
        when(i18nService.getI18nMessageByKeyPrefix(anyString(), any(Locale.class))).thenAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            final Locale locale = invocation.getArgument(1);
            List<I18nMessage> result;
            result = i18nMessages.stream().filter(i18nMessage -> StringUtils.startsWith(i18nMessage.getKey(), key)
                    && StringUtils.equalsIgnoreCase(i18nMessage.getLocale(), locale.toLanguageTag()))
                    .collect(Collectors.toList());
            return result;
        });
        when(i18nService.getI18nMessageByKey(anyString())).thenAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            List<I18nMessage> result;
            result = i18nMessages.stream().filter(i18nMessage -> StringUtils.equals(key, i18nMessage.getKey()))
                    .collect(Collectors.toList());
            return result;
        });
        when(i18nService.getI18nMessageByKey(anyString(), any(Locale.class))).thenAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            final Locale locale = invocation.getArgument(1);
            List<I18nMessage> result;
            result = i18nMessages.stream().filter(i18nMessage -> StringUtils.equals(i18nMessage.getKey(),key)
                    && StringUtils.equalsIgnoreCase(i18nMessage.getLocale(),locale.toLanguageTag()))
                    .collect(Collectors.toList());
            return result;
        });
        when(i18nService.getI18nMessagesByKeys(anyList(), any(Locale.class))).thenAnswer(invocation -> {
            final List<String> keys = invocation.getArgument(0);
            final Locale locale = invocation.getArgument(1);
            List<I18nMessage> result;
            result = i18nMessages.stream().filter(i18nMessage -> keys.contains(i18nMessage.getKey())
                    && StringUtils.equalsIgnoreCase(i18nMessage.getLocale(),locale.toLanguageTag()))
                    .collect(Collectors.toList());
            return result;
        });
        when(messageSource.getMessage(anyString(),isNull(), any(Locale.class))).thenAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            final Locale locale = invocation.getArgument(2);
            Optional<I18nMessage> i18nMessageOptional = i18nMessages.stream().filter(i18nMessage -> StringUtils.equals(i18nMessage.getKey(),key)
                    && StringUtils.equalsIgnoreCase(i18nMessage.getLocale(),locale.toLanguageTag())).findFirst();
            String result;
            if(i18nMessageOptional.isPresent()){
                result = i18nMessageOptional.get().getContent();
            }else{
                result = key;
            }
            return result;
        });
    }

    @Test
    @WithMockCustomUser()
    void getMessage_WithKeyAndLocaleEnglish() throws Exception {
        mockMvc.perform (get("/api/i18n_service/v2/public/message/key/{key}", enLocaleName.getKey())
                .param("locale",Locale.ENGLISH.toLanguageTag())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(enLocaleName.getContent()));
    }

    @Test
    @WithMockCustomUser()
    void getAllI18nMessageWithKey_KeyLocaleName() throws Exception {
        mockMvc.perform (get("/api/i18n_service/v2/public/i18n_message/key/{key}/locale/all", "locale.name")
                .param("locale",Locale.ENGLISH.toLanguageTag())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].key").value("locale.name"))
                .andExpect(jsonPath("$[1].key").value("locale.name"))
                .andExpect(jsonPath("$[2].key").value("locale.name"))
                .andExpect(jsonPath("$[3].key").value("locale.name"));
    }

    @Test
    @WithMockCustomUser()
    void getI18nMessageWithPrefix_KeyTest() throws Exception {
        mockMvc.perform (get("/api/i18n_service/v2/public/i18n_message/key/prefix/{prefix}", "test")
                .param("locale",Locale.SIMPLIFIED_CHINESE.toLanguageTag())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$..key",hasItem("test")))
                .andExpect(jsonPath("$..key",hasItem("test.1")))
                .andExpect(jsonPath("$..key",hasItem("test.2")))
                .andExpect(jsonPath("$..key",hasItem("testABC.onlyCN")));
    }

    @Test
    @WithMockCustomUser()
    void getAllI18nMessageWithPrefix_KeyTestABC() throws Exception {
        mockMvc.perform (get("/api/i18n_service/v2/public/i18n_message/key/prefix/{prefix}/locale/all", "testABC.")
                .param("locale",Locale.ENGLISH.toLanguageTag())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$..key",hasItem("testABC.noChinese")))
                .andExpect(jsonPath("$..key",hasItem("testABC.onlyCN")))
                .andExpect(jsonPath("$[?(@.key == 'testABC.noChinese')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.key == 'testABC.noChinese')].content", hasItem("Message with no english")));
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme
