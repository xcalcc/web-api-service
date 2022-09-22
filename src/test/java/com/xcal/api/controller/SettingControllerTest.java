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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.config.AppProperties;
import com.xcal.api.config.WithMockCustomUser;
import com.xcal.api.entity.Setting;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.SettingDto;
import com.xcal.api.model.payload.EmailServerConfiguration;
import com.xcal.api.model.payload.RestResponsePage;
import com.xcal.api.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SettingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private SettingService settingService;

    @Autowired
    private ObjectMapper om;

    private SettingDto settingDto;
    private Setting setting;
    private String settingKey = "Support_Email";
    private String settingValue = "Xcal_Support@gmail.com";
    private AppProperties.Mail emailServerConfiguration;
    private final String adminUsername = "admin";

    @BeforeEach
    void setup() {
        settingDto = SettingDto.builder().settingKey(settingKey).settingValue(settingValue).build();
        setting = Setting.builder().settingKey(settingKey).settingValue(settingValue).build();
        emailServerConfiguration = AppProperties.Mail.builder().protocol("smtp")
                .host("test.163.com").port(465).from("test@from.com").username("testuser")
                .password("testpassword").prefix("testprefix").starttls("true")
                .build();
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createSettingTestSuccess() throws Exception {
        log.info("[createSettingTestSuccess]");
        when(settingService.add(argThat(p -> StringUtils.equalsIgnoreCase(p.getSettingKey(), settingDto.getSettingKey())), anyString())).thenReturn(setting);
        mockMvc.perform (post("/api/setting_service/v2/setting")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(settingDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settingKey").value(settingKey))
                .andExpect(jsonPath("$.settingValue").value(settingValue));
    }


    @Test
    @WithMockCustomUser()
    void createSetting_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[createSetting_WithNonAdminUser_ReturnAccessDeniedException]");
        mockMvc.perform(post("/api/setting_service/v2/setting")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(settingDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createSettingTestFail() throws Exception {
        log.info("[createSettingTestFail]");
        SettingDto noSettingKey = SettingDto.builder().settingValue("no_key_value").build();
        mockMvc.perform (post("/api/setting_service/v2/setting")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(noSettingKey))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        SettingDto noSettingValue = SettingDto.builder().settingKey("key_no_value").build();
        mockMvc.perform (post("/api/setting_service/v2/setting")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(noSettingValue))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockCustomUser(adminUsername)
    void settingsListTestSuccess() throws Exception {
        log.info("[settingsListTestSuccess]");
        Pageable pageable = PageRequest.of(0, 20);
        List<Setting> settingList = Collections.singletonList(setting);
        when(settingService.findAll(pageable)).thenReturn(new RestResponsePage<>(settingList, pageable, settingList.size()));

        mockMvc.perform (get("/api/setting_service/v2/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].settingKey").value(settingKey))
                .andExpect(jsonPath("$.content[0].settingValue").value(settingValue));
    }

    @Test
    @WithMockCustomUser()
    void listSettings_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[listSettings_WithNonAdminUser_ReturnAccessDeniedException]");
        mockMvc.perform(get("/api/setting_service/v2/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateSettingTestSuccess() throws Exception {
        log.info("[updateSettingTestSuccess]");
        when(settingService.update(argThat(p -> StringUtils.equalsIgnoreCase(p.getSettingKey(), settingDto.getSettingKey())), anyString())).thenReturn(setting);
        mockMvc.perform (put("/api/setting_service/v2/setting")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(settingDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settingKey").value(settingKey))
                .andExpect(jsonPath("$.settingValue").value(settingValue));
    }

    @Test
    @WithMockCustomUser()
    void updateSetting_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[updateSetting_WithNonAdminUser_ReturnAccessDeniedException]");
        mockMvc.perform(put("/api/setting_service/v2/setting")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(settingDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateSettingTestFail() throws Exception {
        log.info("[updateSettingTestFail]");

        SettingDto noSettingKey = SettingDto.builder().settingValue("no_key_value").build();
        mockMvc.perform (put("/api/setting_service/v2/setting")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(noSettingKey))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        SettingDto noSettingValue = SettingDto.builder().settingKey("key_no_value").build();
        mockMvc.perform (put("/api/setting_service/v2/setting")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(noSettingValue))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void getEmailServerConfiguration_Request_ReturnMailServerConfigurationResponse() throws Exception {
        log.info("[getEmailServerConfiguration_Request_ReturnMailServerConfigurationResponse]");
        when(settingService.getEmailServerConfiguration()).thenReturn(emailServerConfiguration);
        mockMvc.perform(get("/api/setting_service/v2/setting/email_server_configuration")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocol").value(emailServerConfiguration.getProtocol()))
                .andExpect(jsonPath("$.host").value(emailServerConfiguration.getHost()))
                .andExpect(jsonPath("$.username").value(emailServerConfiguration.getUsername()))
                .andExpect(jsonPath("$.from").value(emailServerConfiguration.getFrom()))
                .andExpect(jsonPath("$.starttls").value(emailServerConfiguration.getStarttls()))
                .andExpect(jsonPath("$.prefix").value(emailServerConfiguration.getPrefix()));
    }

    @Test
    @WithMockCustomUser()
    void getEmailServerConfiguration_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[getEmailServerConfiguration_WithNonAdminUser_ReturnAccessDeniedException]");
        mockMvc.perform(get("/api/setting_service/v2/setting/email_server_configuration")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void saveEmailServerConfiguration_InputConfiguration_ReturnHttpSuccessAndNothing() throws Exception {
        log.info("[saveEmailServerConfiguration_InputConfiguration_ReturnHttpSuccessAndNothing]");
        EmailServerConfiguration emailServerConfiguration = EmailServerConfiguration.builder().protocol(EmailServerConfiguration.Protocol.smtp)
                .host("test.163.com").port(465).from("test@from.com").username("testuser")
                .password("testpassword").prefix("testprefix").starttls(true)
                .build();
        when(settingService.saveEmailServerConfiguration(emailServerConfiguration, adminUsername)).thenReturn(new ArrayList<>());
        mockMvc.perform(post("/api/setting_service/v2/setting/email_server_configuration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(emailServerConfiguration)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser()
    void saveEmailServerConfiguration_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[saveEmailServerConfiguration_WithNonAdminUser_ReturnAccessDeniedException]");
        EmailServerConfiguration emailServerConfiguration = EmailServerConfiguration.builder().protocol(EmailServerConfiguration.Protocol.smtp)
                .host("test.163.com").port(465).from("test@from.com").username("testuser")
                .password("testpassword").prefix("testprefix").starttls(true)
                .build();
        mockMvc.perform(post("/api/setting_service/v2/setting/email_server_configuration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(emailServerConfiguration)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }
}
