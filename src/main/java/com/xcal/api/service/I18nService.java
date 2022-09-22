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

import com.xcal.api.config.AppProperties;
import com.xcal.api.entity.I18nMessage;
import com.xcal.api.repository.I18nMessageRepository;
import com.xcal.api.util.MessagesTemplate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class I18nService {
    private static final String DEFAULT_LANGUAGE = AppProperties.ENGLISH_LANGUAGE;

    @NonNull I18nMessageRepository i18nMessageRepository;

    private static final String regWithKeySpecifier = "\\$\\{([a-zA-Z.0-9_\\-]+)}";

    public static final Pattern regWithKeyPattern = Pattern.compile(regWithKeySpecifier);

    public List<I18nMessage> getI18nMessageByKeyPrefix(String prefix){
        log.trace("[getI18nMessageByKeyPrefix] prefix: {}",  prefix);
        return this.getI18nMessageByKeyPrefix(prefix, null);
    }

    public List<I18nMessage> getI18nMessageByKeyPrefix(String prefix, Locale locale){
        log.trace("[getI18nMessageByKeyPrefix] prefix: {}, locale: {}",  prefix, locale);
        List<I18nMessage> i18nMessages;
        // escape wildcard character _ with \_
        prefix = RegExUtils.replaceAll(prefix,"_","\\_");
        if(locale==null){
            i18nMessages = i18nMessageRepository.findByKeyStartsWith(RegExUtils.replaceAll(prefix,"_","\\_"));
        }else{
            i18nMessages = i18nMessageRepository.findByKeyStartsWithAndLocale(prefix, locale.toLanguageTag());
        }
        return i18nMessages;
    }

    public List<I18nMessage> getI18nMessageByKey(String key){
        log.trace("[getI18nMessageByKey] key: {}",  key);
        return this.getI18nMessageByKey(key, null);
    }

    public List<I18nMessage> getI18nMessageByKey(String key, Locale locale){
        log.trace("[getI18nMessageByKey] key: {}, locale: {}",  key, locale);
        List<I18nMessage> i18nMessages;
        if(locale == null){
            i18nMessages = i18nMessageRepository.findByKey(key);
        }else{
            i18nMessages = i18nMessageRepository.findByKeyAndLocale(key, locale.toLanguageTag());
        }
        return i18nMessages;
    }

    public List<I18nMessage> getI18nMessagesByKeys(List<String> keys, Locale locale){
        log.trace("[getI18nMessagesByKeys] keys size: {}, locale: {}", keys.size(), locale);
        List<I18nMessage> i18nMessages;
        if(locale == null){
            i18nMessages = i18nMessageRepository.findByKeyIn(keys);
        }else{
            i18nMessages = i18nMessageRepository.findByKeyInAndLocale(keys, locale.toLanguageTag());
        }
        return i18nMessages;

    }

    public String getMessageByKey(String key, Locale locale) {
        List<I18nMessage> i18nMessageList = getI18nMessageByKey(key, locale);
        return I18nService.getMessageByKey(key, locale, i18nMessageList);
    }

    public static String getMessageByKey(String key, Locale locale, @NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL) List<I18nMessage> i18nMessages){
        log.trace("[getMessageByKey] key: {}, locale: {}, i18nMessages size: {}",  key, locale, i18nMessages.size());
        String message;
        Optional<I18nMessage> i18nMessageOptional;
        if(locale == null){
            log.trace("[getMessageByKey] locale is null, try default locale, key: {}, DEFAULT_LANGUAGE: {}", key, DEFAULT_LANGUAGE);
            i18nMessageOptional = i18nMessages.stream().filter(i18nMessage -> StringUtils.equalsIgnoreCase(DEFAULT_LANGUAGE, i18nMessage.getLocale())).findFirst();
        }else{
            log.trace("[getMessageByKey] try to resolve, key: {}, locale LanguageTag: {}", key, locale.toLanguageTag());
            i18nMessageOptional = i18nMessages.stream().filter(i18nMessage -> StringUtils.equals(key, i18nMessage.getKey()) && StringUtils.equalsIgnoreCase(locale.toLanguageTag(), i18nMessage.getLocale())).findFirst();
            if(!i18nMessageOptional.isPresent()){
                log.trace("[getMessageByKey] failed to resolve, try default locale, key: {}, DEFAULT_LANGUAGE: {}", key, DEFAULT_LANGUAGE);
                i18nMessageOptional = i18nMessages.stream().filter(i18nMessage -> StringUtils.equals(key, i18nMessage.getKey()) && StringUtils.equalsIgnoreCase(DEFAULT_LANGUAGE, i18nMessage.getLocale())).findFirst();
            }
        }
        message = i18nMessageOptional.map(I18nMessage::getContent).orElse(key);
        log.trace("[getMessageByKey] key: {}, locale: {}, message: {}", key, locale, message);
        return message;

    }

    public String formatString(String originalText, Locale locale) {
        Matcher matcher = regWithKeyPattern.matcher(originalText);
        List<String> keys = new ArrayList<>();
        while (matcher.find()) {
           keys.add(matcher.group(1));
        }
        List<I18nMessage> i18nMessageList = getI18nMessagesByKeys(keys, locale);
        return formatString(originalText, i18nMessageList.stream().collect(Collectors.toMap(I18nMessage::getKey, message -> message)));
    }

    public static String formatString(String originalText,  Map<String, I18nMessage> i18nMessageMap) {
        return formatString(originalText, null, i18nMessageMap);
    }

    public static String formatString(String originalText, Map<String, String> contentMap, Map<String, I18nMessage> i18nMessageMap) {
        // replacementCountLeft, maximum N times (5)
        return formatString(originalText, contentMap, i18nMessageMap, 5);
    }

    public static String formatString(String originalText, Map<String, String> contentMap, Map<String, I18nMessage> i18nMessageMap, int replacementCountLeft) {
        String result;
        if (originalText == null) {
            result = null;
        } else if (replacementCountLeft <= 0) {
            result = originalText;
        } else {
            if (contentMap == null) {
                contentMap = new HashMap<>();
            }
            if (i18nMessageMap == null) {
                i18nMessageMap = new HashMap<>();
            }
            Matcher matcher = regWithKeyPattern.matcher(originalText);
            StringBuffer sb = new StringBuffer();
            // This method running with recursive with maximum 5 times
            while (matcher.find()) {
                String key = matcher.group(1);
                // try to get replacement from contentMap first
                String message = contentMap.get(key);
                // query the i18NMessage no value in contentMap
                if (message == null && i18nMessageMap.containsKey(key)) {
                    message = i18nMessageMap.get(key).getContent();
                }
                message = StringUtils.defaultString(message, key);
                // replace if any match only
                matcher.appendReplacement(sb, Matcher.quoteReplacement(message));
            }
            matcher.appendTail(sb);
            result = sb.toString();
            // recursive to replace pattern
            result = formatString(result, contentMap, i18nMessageMap, replacementCountLeft - 1);
        }
        return result;
    }
}
