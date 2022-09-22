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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

@Component("messageSource")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DBMessageSource extends AbstractMessageSource {

    @NonNull
    private I18nService i18nService;

    public String resolveMessage(String message, Locale locale) {
        List<I18nMessage> i18nMessages = this.i18nService.getI18nMessageByKey(message);
        return I18nService.getMessageByKey(message, locale, i18nMessages);
    }

    @Override
    protected MessageFormat resolveCode(String key, Locale locale) {
        log.debug("[resolveCode] key: {}, locale: {}", key, locale);
        String message;
        List<I18nMessage> i18nMessages = this.i18nService.getI18nMessageByKey(key);
        message = I18nService.getMessageByKey(key, locale, i18nMessages);
        return new MessageFormat(message, locale);
    }
}
