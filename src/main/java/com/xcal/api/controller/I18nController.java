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

import com.xcal.api.entity.I18nMessage;
import com.xcal.api.service.I18nService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

/**
 * The user and group relationship controller
 */
@Slf4j
@RestController
@RequestMapping("/api/i18n_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Internationalization Service")
public class I18nController {

    @NonNull MessageSource messageSource;
    @NonNull I18nService i18nService;

    /**
     * get message with locale
     *
     * @param locale locale by spring injection, request param locale or cookies
     * @param key    key
     * @return get message with prefix
     */
    @ApiOperation(value = "get localed message",
            nickname = "getLocaleMessage",
            notes = "get localed message as plain string")
    @GetMapping("/public/message/key/{key}")
    public ResponseEntity<String> getMessage(@PathVariable String key, Locale locale) {
        log.info("[getMessage] key: {}, locale: {}", key, locale);
        String message = messageSource.getMessage(key, null, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(message);
    }

    /**
     * get message with key and locale
     *
     * @param key key from path
     * @return get message with key
     */
    @ApiOperation(value = "get all i18n message with key",
            nickname = "getAllI18nMessageWithKey",
            notes = "get all i18n message with key")
    @GetMapping("/public/i18n_message/key/{key}/locale/all")
    public ResponseEntity<List<I18nMessage>> getAllI18nMessageWithKey(@PathVariable String key) {
        log.info("[getAllI18nMessageWithKey] key: {}", key);
        List<I18nMessage> i18nMessages = this.i18nService.getI18nMessageByKey(key);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(i18nMessages);
    }

    /**
     * get message with key prefix and locale
     *
     * @param prefix prefix from path
     * @param locale locale by spring injection, request param locale or cookies
     * @return get message with prefix
     */
    @ApiOperation(value = "get i18n message with prefix",
            nickname = "getI18nMessageWithPrefix",
            notes = "get i18n message with key have prefix and locale")
    @GetMapping("/public/i18n_message/key/prefix/{prefix}")
    public ResponseEntity<List<I18nMessage>> getI18nMessageWithPrefix(@PathVariable String prefix, Locale locale) {
        log.info("[getI18nMessageWithPrefix] prefix: {}, locale: {}", prefix, locale);
        List<I18nMessage> i18nMessages = this.i18nService.getI18nMessageByKeyPrefix(prefix, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(i18nMessages);
    }

    /**
     * get message with key prefix and locale
     *
     * @param prefix prefix from path
     * @return get message with prefix
     */
    @ApiOperation(value = "get all i18n message with prefix",
            nickname = "getAllI18nMessageWithPrefix",
            notes = "get all i18n message with key have prefix")
    @GetMapping("/public/i18n_message/key/prefix/{prefix}/locale/all")
    public ResponseEntity<List<I18nMessage>> getAllI18nMessageWithPrefix(@PathVariable String prefix) {
        log.info("[getAllI18nMessageWithPrefix] prefix: {}", prefix);
        List<I18nMessage> i18nMessages = this.i18nService.getI18nMessageByKeyPrefix(prefix);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(i18nMessages);
    }
}
