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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DBMessageSourceTest {

    private DBMessageSource dbMessageSource;

    @BeforeEach
    void setUp() {
        I18nService i18nService = mock(I18nService.class);
        dbMessageSource = new DBMessageSource(i18nService);
        when(i18nService.getI18nMessageByKey("name")).thenReturn(Arrays.asList(
                I18nMessage.builder().key("name").locale(Locale.ENGLISH.toLanguageTag()).content("Name").build(),
                I18nMessage.builder().key("name").locale(Locale.SIMPLIFIED_CHINESE.toLanguageTag()).content("名称").build(),
                I18nMessage.builder().key("name").locale(Locale.TRADITIONAL_CHINESE.toLanguageTag()).content("名稱").build()));

        when(i18nService.getI18nMessageByKey("test")).thenReturn(Arrays.asList(
                I18nMessage.builder().key("test").locale(Locale.ENGLISH.toLanguageTag()).content("test").build(),
                I18nMessage.builder().key("test").locale(Locale.SIMPLIFIED_CHINESE.toLanguageTag()).content("测试").build(),
                I18nMessage.builder().key("test").locale(Locale.TRADITIONAL_CHINESE.toLanguageTag()).content("測試").build()));

        when(i18nService.getI18nMessageByKey("only.english")).thenReturn(Collections.singletonList(
                I18nMessage.builder().key("only.english").locale(Locale.ENGLISH.toLanguageTag()).content("test").build()));
    }

    @Test
    void resolveCodeEnglish() {
        MessageFormat testResult = dbMessageSource.resolveCode("name", Locale.ENGLISH);
        assert testResult != null;
        assertEquals("Name",testResult.toPattern());
    }

    @Test
    void resolveCodeNonEnglish() {
        MessageFormat testResult = dbMessageSource.resolveCode("name", Locale.SIMPLIFIED_CHINESE);
        assert testResult != null;
        assertEquals("名称",testResult.toPattern());
    }

    @Test
    void resolveCodeNullLocale() {
        MessageFormat testResult = dbMessageSource.resolveCode("name", null);
        assert testResult != null;
        assertEquals("Name",testResult.toPattern());
    }
}
