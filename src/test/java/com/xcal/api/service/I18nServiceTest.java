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

import com.xcal.api.entity.I18nMessage;
import com.xcal.api.repository.I18nMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class I18nServiceTest {
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

    private I18nService i18nService;

    @BeforeEach
    void setUp() {
        i18nMessages = Arrays.asList( enLocaleName, cnLocaleName, hkLocaleName, zhLocaleName, enTest, cnTest, hkTest, enTest_1, cnTest_1, hkTest_1, enTest_2, cnTest_2, hkTest_2, enOnlyMessage, cnOnlyMessage, englishLanguage, chineseLanguage);
        I18nMessageRepository i18nMessageRepository = mock(I18nMessageRepository.class);
        when(i18nMessageRepository.findByKeyStartsWith(anyString())).thenAnswer(
                invocation -> i18nMessages.stream().filter(i18nMessage -> StringUtils.startsWith(i18nMessage.getKey(),invocation.getArgument(0))).collect(Collectors.toList()));
        when(i18nMessageRepository.findByKeyStartsWithAndLocale(anyString(), isNull())).thenAnswer(invocation -> {
            List<I18nMessage> result;
            result = i18nMessages.stream().filter(i18nMessage -> StringUtils.startsWith(i18nMessage.getKey(),invocation.getArgument(0))).collect(Collectors.toList());
            return result;
        });
        when(i18nMessageRepository.findByKeyStartsWithAndLocale(anyString(), anyString())).thenAnswer(invocation -> {
            List<I18nMessage> result;
            result = i18nMessages.stream().filter(i18nMessage -> StringUtils.startsWith(i18nMessage.getKey(),invocation.getArgument(0))
                    && StringUtils.equalsIgnoreCase(i18nMessage.getLocale(),invocation.getArgument(1)))
                    .collect(Collectors.toList());
            return result;
        });
        when(i18nMessageRepository.findByKey(anyString())).thenAnswer(
                invocation -> i18nMessages.stream().filter(i18nMessage -> StringUtils.equals(i18nMessage.getKey(),invocation.getArgument(0))).collect(Collectors.toList()));
        when(i18nMessageRepository.findByKeyAndLocale(anyString(), anyString())).thenAnswer(invocation -> {
            List<I18nMessage> result;
            result = i18nMessages.stream().filter(i18nMessage -> StringUtils.equals(i18nMessage.getKey(),invocation.getArgument(0))
                    && StringUtils.equalsIgnoreCase(i18nMessage.getLocale(),invocation.getArgument(1)))
                    .collect(Collectors.toList());
            return result;
        });
        when(i18nMessageRepository.findByKeyIn(anyList())).thenAnswer(invocation -> {
            final List<String> keys = invocation.getArgument(0);
            List<I18nMessage> result;
            result = i18nMessages.stream().filter(i18nMessage -> keys.contains(i18nMessage.getKey())).collect(Collectors.toList());
            return result;
        });
        when(i18nMessageRepository.findByKeyInAndLocale(anyList(), anyString())).thenAnswer(invocation -> {
            final List<String> keys = invocation.getArgument(0);
            final String locale = invocation.getArgument(1);
            List<I18nMessage> result;
            result = i18nMessages.stream().filter(i18nMessage -> keys.contains(i18nMessage.getKey())
                    && StringUtils.equalsIgnoreCase(i18nMessage.getLocale(),locale))
                    .collect(Collectors.toList());
            return result;
        });
        i18nService = new I18nService(i18nMessageRepository);
    }

    @Test
    void getI18nMessageByKeyPrefix() {
        log.info("[testGetI18nMessageByKeyPrefix]");
        List<I18nMessage> messagesStartWithTest = i18nService.getI18nMessageByKeyPrefix("test");
        assertEquals(11, messagesStartWithTest.size());

        List<I18nMessage> messagesStartWithTestDot = i18nService.getI18nMessageByKeyPrefix("test.");
        assertEquals(6, messagesStartWithTestDot.size());

        List<I18nMessage> messagesStartWithTestABC = i18nService.getI18nMessageByKeyPrefix("testABC");
        assertEquals(2, messagesStartWithTestABC.size());

        List<I18nMessage> messagesStartWithLocale = i18nService.getI18nMessageByKeyPrefix("locale");
        assertEquals(4, messagesStartWithLocale.size());

        List<I18nMessage> messagesStartWithTestEN = i18nService.getI18nMessageByKeyPrefix("test", Locale.ENGLISH);
        assertEquals(4, messagesStartWithTestEN.size());
        assertEquals(Locale.ENGLISH.toLanguageTag(), messagesStartWithTestEN.get(0).getLocale());

        List<I18nMessage> messagesStartWithTestCN = i18nService.getI18nMessageByKeyPrefix("test", Locale.SIMPLIFIED_CHINESE);
        assertEquals(4, messagesStartWithTestCN.size());

        List<I18nMessage> messagesStartWithTestABCEN = i18nService.getI18nMessageByKeyPrefix("testABC", Locale.ENGLISH);
        assertEquals(1, messagesStartWithTestABCEN.size());

        List<I18nMessage> messagesStartWithTestABCCN = i18nService.getI18nMessageByKeyPrefix("testABC", Locale.SIMPLIFIED_CHINESE);
        assertEquals(1, messagesStartWithTestABCCN.size());

        List<I18nMessage> messagesStartWithTestABCHK = i18nService.getI18nMessageByKeyPrefix("testABC", Locale.TRADITIONAL_CHINESE);
        assertEquals(0, messagesStartWithTestABCHK.size());

        List<I18nMessage> messagesStartWithLocaleFR = i18nService.getI18nMessageByKeyPrefix("locale", Locale.FRANCE);
        assertEquals(0, messagesStartWithLocaleFR.size());
    }

    @Test
    void getI18nMessageByKey_NoLocale() {
        log.info("[getI18nMessageByKey]");
        List<I18nMessage> messagesWithTest = i18nService.getI18nMessageByKey("test");
        assertEquals(3, messagesWithTest.size());

        List<I18nMessage> messagesTestDot = i18nService.getI18nMessageByKey("test.");
        assertEquals(0, messagesTestDot.size());

        List<I18nMessage> messagesTestABC = i18nService.getI18nMessageByKey("testABC");
        assertEquals(0, messagesTestABC.size());

        List<I18nMessage> messagesTestABCNoChinese = i18nService.getI18nMessageByKey("testABC.noChinese");
        assertEquals(1, messagesTestABCNoChinese.size());

        List<I18nMessage> messagesLocaleName = i18nService.getI18nMessageByKey("locale.name");
        assertEquals(4, messagesLocaleName.size());

        List<I18nMessage> messagesLocale = i18nService.getI18nMessageByKey("locale");
        assertEquals(0, messagesLocale.size());
    }

    @Test
    void getI18nMessageByKey_WithLocale() {
        List<I18nMessage> messagesWithTestEn = i18nService.getI18nMessageByKey("test", Locale.ENGLISH);
        assertEquals(1, messagesWithTestEn.size());
        assertEquals(Locale.ENGLISH.toLanguageTag(), messagesWithTestEn.get(0).getLocale());

        List<I18nMessage> messagesWithTestSCn = i18nService.getI18nMessageByKey("test", Locale.SIMPLIFIED_CHINESE);
        assertEquals(1, messagesWithTestSCn.size());
        assertEquals(Locale.SIMPLIFIED_CHINESE.toLanguageTag(), messagesWithTestSCn.get(0).getLocale());

        List<I18nMessage> messagesWithTestFr = i18nService.getI18nMessageByKey("test", Locale.FRENCH);
        assertEquals(0, messagesWithTestFr.size());
    }

    @Test
    void getMessageByKey_WithLocale() {
        log.info("[getMessageByKey]");
        String testEN = I18nService.getMessageByKey("test",Locale.ENGLISH, i18nMessages);
        assertEquals("Test", testEN);

        String testCN = I18nService.getMessageByKey("test",Locale.SIMPLIFIED_CHINESE, i18nMessages);
        assertEquals("测试", testCN);

        String testHK = I18nService.getMessageByKey("test",Locale.TRADITIONAL_CHINESE, i18nMessages);
        assertEquals("測試", testHK);

        String testFR = I18nService.getMessageByKey("test",Locale.FRENCH, i18nMessages);
        assertEquals("Test", testFR);

        String testABCNoChineseEN = I18nService.getMessageByKey("testABC.noChinese",Locale.ENGLISH, i18nMessages);
        assertEquals("Message with no english", testABCNoChineseEN);

        String testABCNoChineseCN = I18nService.getMessageByKey("testABC.noChinese",Locale.SIMPLIFIED_CHINESE, i18nMessages);
        assertEquals("Message with no english", testABCNoChineseCN);

        String test1EN = I18nService.getMessageByKey("test.1",Locale.ENGLISH, i18nMessages);
        assertEquals("Test 1", test1EN);

        String testNotExist = I18nService.getMessageByKey("test.not.exist",Locale.ENGLISH, i18nMessages);
        assertEquals("test.not.exist", testNotExist);
    }
    @Test
    void getMessageByKey_WithLanguage() {
        log.info("[getMessageByKey_WithLanguage]");
        String testEN = I18nService.getMessageByKey("language",Locale.ENGLISH, i18nMessages);
        assertEquals(englishLanguage.getContent(), testEN);

        String testCN = I18nService.getMessageByKey("language",Locale.CHINESE, i18nMessages);
        assertEquals(chineseLanguage.getContent(), testCN);
    }

    @Test
    void getMessageByKey_WithDefaultLanguage() {
        log.info("[getMessageByKey_WithLanguage]");
        String testFR = I18nService.getMessageByKey("language",Locale.FRENCH, i18nMessages);
        assertEquals(englishLanguage.getContent(), testFR);
    }

    @Test
    void getMessageByKey_KeyNotFound() {
        log.info("[getMessageByKey_KeyNotFound]");
        String keyNotFound = I18nService.getMessageByKey("keyNotFound",Locale.ENGLISH, i18nMessages);
        assertEquals("keyNotFound", keyNotFound);
    }

    @Test
    void getMessageByKey_NoDefaultLanguage() {
        log.info("[getMessageByKey_NoDefaultLanguage]");
        String onlyCn = I18nService.getMessageByKey("testABC.onlyCN",Locale.FRENCH, i18nMessages);
        assertEquals("testABC.onlyCN", onlyCn);
    }

    @Test
    void getI18nMessagesByKeys_KeyExistNoLocale_MessageFound(){
        log.info("[getI18nMessagesByKeys_KeyExistNoLocale_MessageFound]");
        List<String> keys = Arrays.asList("locale.name", "test", "testABC.noChinese");
        List<I18nMessage> i18nMessages = this.i18nService.getI18nMessagesByKeys(keys, null);
        assertEquals(8, i18nMessages.size());
        assertEquals(4, i18nMessages.stream().filter(i18nMessage -> StringUtils.equals("locale.name",i18nMessage.getKey())).count());
        assertEquals(3, i18nMessages.stream().filter(i18nMessage -> StringUtils.equals("test",i18nMessage.getKey())).count());
        assertEquals(1, i18nMessages.stream().filter(i18nMessage -> StringUtils.equals("testABC.noChinese",i18nMessage.getKey())).count());
        assertEquals(3, i18nMessages.stream().filter(i18nMessage -> StringUtils.equals(Locale.ENGLISH.toLanguageTag(),i18nMessage.getLocale())).count());
        assertEquals(2, i18nMessages.stream().filter(i18nMessage -> StringUtils.equals(Locale.SIMPLIFIED_CHINESE.toLanguageTag(),i18nMessage.getLocale())).count());
        assertEquals(2, i18nMessages.stream().filter(i18nMessage -> StringUtils.equals(Locale.TRADITIONAL_CHINESE.toLanguageTag(),i18nMessage.getLocale())).count());
        assertEquals(Locale.ENGLISH.toLanguageTag(), i18nMessages.get(0).getLocale());
    }

    @Test
    void getI18nMessagesByKeys_KeyExistLocaleEnglish_MessageFound(){
        log.info("[getI18nMessagesByKeys_KeyExist_MessageFound]");
        List<String> keys = Arrays.asList("locale.name", "test", "testABC.noChinese");
        List<I18nMessage> i18nMessages = this.i18nService.getI18nMessagesByKeys(keys, Locale.ENGLISH);
        assertEquals(3, i18nMessages.size());
        assertEquals(Locale.ENGLISH.toLanguageTag(), i18nMessages.get(0).getLocale());
    }

    @Test
    void getI18nMessagesByKeys_KeyExistLocaleCN_MessageFound(){
        log.info("[getI18nMessagesByKeys_KeyExist_MessageFound]");
        List<String> keys = Arrays.asList("locale.name", "test", "testABC.noChinese");
        List<I18nMessage> i18nMessages = this.i18nService.getI18nMessagesByKeys(keys, Locale.SIMPLIFIED_CHINESE);
        assertEquals(2, i18nMessages.size());
        assertTrue(i18nMessages.stream().map(I18nMessage::getKey).collect(Collectors.toList()).containsAll(Arrays.asList("locale.name","test")));
        assertEquals(Locale.SIMPLIFIED_CHINESE.toLanguageTag(), i18nMessages.get(0).getLocale());
    }


}
