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
import com.xcal.api.entity.Setting;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.SettingDto;
import com.xcal.api.model.payload.EmailServerConfiguration;
import com.xcal.api.model.payload.RestResponsePage;
import com.xcal.api.repository.SettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class SettingServiceTest {
    private SettingService settingService;
    private SettingRepository settingRepository;
    private AppProperties appProperties;

    private static final String SETTING_PREFIX_MAIL = "app.mail.";
    private Setting settingProtocol = Setting.builder().settingKey("app.mail.protocol").settingValue("smtp").build();
    private Setting settingHost = Setting.builder().settingKey("app.mail.host").settingValue("smtpdm.aliyun.com").build();
    private Setting settingPort = Setting.builder().settingKey("app.mail.port").settingValue("465").build();
    private Setting settingUsername = Setting.builder().settingKey("app.mail.username").settingValue("no-reply@xcalibyte.io").build();
    private Setting settingStarttls = Setting.builder().settingKey("app.mail.starttls").settingValue("false").build();
    private Setting settingPrefix = Setting.builder().settingKey("app.mail.prefix").settingValue("[XCALBYTE]").build();
    private Setting settingPassword = Setting.builder().settingKey("app.mail.password").settingValue("123456").build();
    private Setting settingMailFrom = Setting.builder().settingKey("app.mail.from").settingValue("123456@xxx.com").build();
    private Setting settingMailOtherKey = Setting.builder().settingKey("app.mail.other").settingValue("other").build();

    private Setting setting;
    private List<Setting> settingList = new ArrayList<>();
    private SettingDto settingDto;
    private String key = "Support_Email";
    private String value = "Xcal_Support@gmail.com";

    @BeforeEach
    void setup() {
        settingRepository = mock(SettingRepository.class);
        ModelMapper modelMapper = new ModelMapper();
        appProperties = mock(AppProperties.class);
        settingService = new SettingService(settingRepository, appProperties, modelMapper);
        settingDto = SettingDto.builder().settingKey(key).settingValue(value).build();
        setting = Setting.builder().settingKey(key).settingValue(value).build();
        settingList = Arrays.asList(settingProtocol, settingHost, settingPort, settingUsername, settingStarttls, settingPrefix, settingPassword, settingMailFrom);
    }

    @Test
    void findByKeyTestSuccess() {
        log.info("[findByKeyTestSuccess]");
        when(settingRepository.findBySettingKey(key)).thenReturn(Optional.of(setting));
        assertTrue(settingService.findByKey(key).isPresent());
        Setting result = settingService.findByKey(key).get();
        assertEquals(key, result.getSettingKey());
        assertEquals(value, result.getSettingValue());
    }

    @Test
    void addTestSuccess() throws AppException {
        log.info("[addTestSuccess]");
        when(settingRepository.findBySettingKey(key)).thenReturn(Optional.empty());
        when(settingRepository.save(any())).thenReturn(setting);
        Setting result = settingService.add(settingDto, anyString());
        assertEquals(key, result.getSettingKey());
        assertEquals(value, result.getSettingValue());
    }

    @Test
    void addTestFaile() {
        log.info("[addTestSuccess]");
        when(settingRepository.findBySettingKey(eq(key))).thenReturn(Optional.of(setting));

        assertThrows(AppException.class, () -> settingService.add(settingDto, "user"));
    }

    @Test
    void findAllTest() {
        log.info("[findAllTest]");
        when(settingRepository.findBySettingKey(eq(key))).thenReturn(Optional.of(setting));

        Pageable pageable = PageRequest.of(0, 20);
        List<Setting> settingList = Collections.singletonList(setting);
        when(settingRepository.findAll(pageable)).thenReturn(new RestResponsePage<>(settingList, pageable, settingList.size()));

        Page<Setting> settingPage = settingService.findAll(pageable);
        log.info(settingPage.toString());
        Setting result = settingPage.getContent().get(0);
        assertEquals(key, result.getSettingKey());
        assertEquals(value, result.getSettingValue());
    }

    @Test
    void updateTestSuccess() throws AppException {
        log.info("[updateTestSuccess]");
        when(settingRepository.findBySettingKey(key)).thenReturn(Optional.of(setting));
        when(settingRepository.save(any())).thenReturn(setting);
        Setting result = settingService.update(settingDto, anyString());
        assertEquals(key, result.getSettingKey());
        assertEquals(value, result.getSettingValue());
    }

    @Test
    void updateTestFail() {
        log.info("[updateTestSuccess]");
        when(settingRepository.findBySettingKey(key)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> settingService.update(settingDto, "user"));
    }

    @Test
    void updateEmailServerConfigurationTestSuccess() {
        log.info("[updateEmailServerConfigurationTestSuccess]");
        List<Setting> settingList = Arrays.asList(settingProtocol, settingHost, settingPort, settingUsername, settingStarttls, settingPrefix, settingPassword, settingMailFrom,settingMailOtherKey);
        when(settingRepository.findBySettingKeyStartingWith(SETTING_PREFIX_MAIL)).thenReturn(settingList);
        AppProperties.Mail mail = new AppProperties.Mail();
        when(settingService.appProperties.getMail()).thenReturn(mail);
        settingService.updateEmailServerConfiguration();
        assertEquals(settingProtocol.getSettingValue(), appProperties.getMail().getProtocol());
        assertEquals(settingHost.getSettingValue(), appProperties.getMail().getHost());
        assertEquals(settingPort.getSettingValue(), String.valueOf(appProperties.getMail().getPort()));
        assertEquals(settingUsername.getSettingValue(), appProperties.getMail().getUsername());
        assertEquals(settingStarttls.getSettingValue(), appProperties.getMail().getStarttls());
        assertEquals(settingPrefix.getSettingValue(), appProperties.getMail().getPrefix());
        assertEquals(settingPassword.getSettingValue(), appProperties.getMail().getPassword());
        assertEquals(settingMailFrom.getSettingValue(), appProperties.getMail().getFrom());
    }

    @Test
    void saveEmailServerConfigurationTestSuccess() {
        log.info("[saveEmailServerConfigurationTestSuccess]");
        when(settingRepository.findBySettingKeyStartingWith(SETTING_PREFIX_MAIL)).thenReturn(settingList);
        EmailServerConfiguration.Protocol protocol = EmailServerConfiguration.Protocol.pop;
        String host = "test.163.com";
        int port = 465;
        String username = "testuser";
        String password = "testpassword";
        Boolean starttls = true;
        String prefix = "testfrefix";
        String from = "test@163.com";
        Setting resultSettingProtocol = Setting.builder().settingKey("app.mail.protocol").settingValue(protocol.toString()).build();
        Setting resultSettingHost = Setting.builder().settingKey("app.mail.host").settingValue(host).build();
        Setting resultSettingPort = Setting.builder().settingKey("app.mail.port").settingValue(String.valueOf(port)).build();
        Setting resultSettingUsername = Setting.builder().settingKey("app.mail.username").settingValue(username).build();
        Setting resultSettingStarttls = Setting.builder().settingKey("app.mail.starttls").settingValue(String.valueOf(starttls)).build();
        Setting resultSettingPrefix = Setting.builder().settingKey("app.mail.prefix").settingValue(prefix).build();
        Setting resultSettingPassword = Setting.builder().settingKey("app.mail.password").settingValue(password).build();
        Setting resultSettingFrom = Setting.builder().settingKey("app.mail.from").settingValue(from).build();
        List<Setting> expectedSettingList = Arrays.asList(resultSettingProtocol, resultSettingHost, resultSettingPort, resultSettingUsername, resultSettingStarttls, resultSettingPrefix, resultSettingPassword,resultSettingFrom);
        EmailServerConfiguration configuration = EmailServerConfiguration.builder().protocol(EmailServerConfiguration.Protocol.pop)
                .host(host).port(port).from(from).username(username)
                .password(password).prefix(prefix).starttls(starttls)
                .build();
        when(settingRepository.saveAll(any())).thenReturn(expectedSettingList);
        List<Setting> resultSettingList = settingService.saveEmailServerConfiguration(configuration, "user");
        assertEquals(expectedSettingList.size(), resultSettingList.size());
        assertEquals(expectedSettingList, resultSettingList);
    }

    @Test
    void saveEmailServerConfiguration_WithNewSettingFromAndPasswordAndPrefix_TestSuccess() {
        log.info("[saveEmailServerConfiguration_WithNewSettingFromAndPasswordAndPrefix_TestSuccess]");
        List<Setting> settingList = Arrays.asList(settingProtocol, settingHost, settingPort, settingUsername, settingStarttls);
        when(settingRepository.findBySettingKeyStartingWith(SETTING_PREFIX_MAIL)).thenReturn(settingList);
        EmailServerConfiguration.Protocol protocol = EmailServerConfiguration.Protocol.pop;
        String host = "test.163.com";
        int port = 465;
        String username = "testuser";
        String password = "testpassword";
        Boolean starttls = true;
        String prefix = "testfrefix";
        String from = "test@163.com";
        Setting resultSettingProtocol = Setting.builder().settingKey("app.mail.protocol").settingValue(protocol.toString()).build();
        Setting resultSettingHost = Setting.builder().settingKey("app.mail.host").settingValue(host).build();
        Setting resultSettingPort = Setting.builder().settingKey("app.mail.port").settingValue(String.valueOf(port)).build();
        Setting resultSettingUsername = Setting.builder().settingKey("app.mail.username").settingValue(username).build();
        Setting resultSettingStarttls = Setting.builder().settingKey("app.mail.starttls").settingValue(String.valueOf(starttls)).build();
        Setting resultSettingPrefix = Setting.builder().settingKey("app.mail.prefix").settingValue(prefix).build();
        Setting resultSettingPassword = Setting.builder().settingKey("app.mail.password").settingValue(password).build();
        Setting resultSettingFrom = Setting.builder().settingKey("app.mail.from").settingValue(from).build();
        List<Setting> expectedSettingList = Arrays.asList(resultSettingProtocol, resultSettingHost, resultSettingPort, resultSettingUsername, resultSettingStarttls, resultSettingPrefix, resultSettingPassword,resultSettingFrom);
        EmailServerConfiguration configuration = EmailServerConfiguration.builder().protocol(EmailServerConfiguration.Protocol.pop)
                .host(host).port(port).from(from).username(username)
                .password(password).prefix(prefix).starttls(starttls)
                .build();
        when(settingRepository.saveAll(any())).thenReturn(expectedSettingList);
        List<Setting> resultSettingList = settingService.saveEmailServerConfiguration(configuration, "user");
        assertEquals(expectedSettingList.size(), resultSettingList.size());
        assertEquals(expectedSettingList, resultSettingList);
    }

    @Test
    void saveEmailServerConfiguration_TestWithoutPasswordWithoutFromWithoutPrefix_Success() {
        log.info("[saveEmailServerConfiguration_TestWithoutPasswordWithoutFromWithoutPrefix_Success]");
        when(settingRepository.findBySettingKeyStartingWith(SETTING_PREFIX_MAIL)).thenReturn(new ArrayList<>());
        EmailServerConfiguration.Protocol protocol = EmailServerConfiguration.Protocol.smtp;
        String host = "test.163.com";
        int port = 465;
        String username = "testuser";
        Boolean starttls = true;
        Setting resultSettingProtocol = Setting.builder().settingKey("app.mail.protocol").settingValue(protocol.toString()).build();
        Setting resultSettingHost = Setting.builder().settingKey("app.mail.host").settingValue(host).build();
        Setting resultSettingPort = Setting.builder().settingKey("app.mail.port").settingValue(String.valueOf(port)).build();
        Setting resultSettingUsername = Setting.builder().settingKey("app.mail.username").settingValue(username).build();
        Setting resultSettingStarttls = Setting.builder().settingKey("app.mail.starttls").settingValue(String.valueOf(starttls)).build();
        List<Setting> expectedSettingList = Arrays.asList(resultSettingProtocol, resultSettingHost, resultSettingPort, resultSettingUsername, resultSettingStarttls);
        EmailServerConfiguration configuration = EmailServerConfiguration.builder().protocol(protocol)
                .host(host).port(465).username(username)
                .starttls(starttls)
                .build();
        when(settingRepository.saveAll(any())).thenReturn(expectedSettingList);
        List<Setting> resultSettingList = settingService.saveEmailServerConfiguration(configuration, "user");
        assertEquals(expectedSettingList.size(), resultSettingList.size());
        assertEquals(expectedSettingList, resultSettingList);
    }

    @Test
    void getEmailServerConfigurationTestSuccess() {
        log.info("[getEmailServerConfigurationTestSuccess]");
        List<Setting> settingList = Arrays.asList(settingProtocol, settingHost, settingPort, settingUsername, settingStarttls, settingPrefix, settingPassword, settingMailFrom,settingMailOtherKey);
        when(settingRepository.findBySettingKeyStartingWith(SETTING_PREFIX_MAIL)).thenReturn(settingList);
        AppProperties.Mail mail = new AppProperties.Mail();
        when(settingService.appProperties.getMail()).thenReturn(mail);
        settingService.getEmailServerConfiguration();
        assertEquals(settingProtocol.getSettingValue(), appProperties.getMail().getProtocol());
        assertEquals(settingHost.getSettingValue(), appProperties.getMail().getHost());
        assertEquals(settingPort.getSettingValue(), String.valueOf(appProperties.getMail().getPort()));
        assertEquals(settingUsername.getSettingValue(), appProperties.getMail().getUsername());
        assertEquals(settingStarttls.getSettingValue(), appProperties.getMail().getStarttls());
        assertEquals(settingPrefix.getSettingValue(), appProperties.getMail().getPrefix());
        assertEquals(settingPassword.getSettingValue(), appProperties.getMail().getPassword());
        assertEquals(settingMailFrom.getSettingValue(), appProperties.getMail().getFrom());
    }
}
