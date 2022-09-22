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

import com.xcal.api.config.AppProperties;
import com.xcal.api.entity.Setting;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.SettingDto;
import com.xcal.api.model.payload.EmailServerConfiguration;
import com.xcal.api.model.payload.MailServerConfigurationResponse;
import com.xcal.api.model.payload.RetentionPeriodConfig;
import com.xcal.api.model.payload.RetentionPeriodConfigResponse;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.SettingService;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.Dto;
import com.xcal.api.util.ValidErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.net.HttpURLConnection;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/setting_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "System Service")
public class SettingController {

    @NonNull SettingService settingService;

    @NonNull ModelMapper modelMapper;

    @ApiOperation(value = "Create setting",
            nickname = "createSetting",
            notes = "Create setting, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/setting")
    @RolesAllowed("ROLE_ADMIN")
    @Dto(SettingDto.class)
    public ResponseEntity<Setting> createSetting(@Valid @ValidErrorCode(AppException.ErrorCode.E_API_PRESET_CREATE_SETTING_VALIDATE_FAIL) @RequestBody SettingDto setting, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[createSetting] setting: {}", setting);
        Setting result = settingService.add(setting, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    @ApiOperation(value = "List Setting",
            nickname = "listSettings",
            notes = "List settings with paging, default page size 20",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/settings")
    @RolesAllowed("ROLE_ADMIN")
    @Dto(SettingDto.class)
    public ResponseEntity<Page<Setting>> listSettings(Pageable pageable) {
        log.info("[listSettings] ");
        Page<Setting> settings = this.settingService.findAll(pageable);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(settings);
    }

    @ApiOperation(value = "Get setting by key",
            nickname = "GetSetting",
            notes = "Get setting by key",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/setting/{key}")
    @RolesAllowed("ROLE_ADMIN")
    @Dto(SettingDto.class)
    public ResponseEntity<Setting> getSetting(@PathVariable String key, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getSetting] key: {}, username: {}", key, userPrincipal.getUsername());
        Setting setting = this.settingService.findByKey(key).orElseThrow (
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] settingKey: {}", AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.messageTemplate, key)));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(setting);
    }

    @ApiOperation(value = "Update setting",
            nickname = "updateSetting",
            notes = "Update setting, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping("/setting")
    @RolesAllowed("ROLE_ADMIN")
    @Dto(SettingDto.class)
    public ResponseEntity<Setting> updateSetting(@Valid @ValidErrorCode(AppException.ErrorCode.E_API_PRESET_UPDATE_SETTING_VALIDATE_FAIL) @RequestBody SettingDto setting, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateSetting] setting: {}", setting);
        Setting result = settingService.update(setting, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    @ApiOperation(value = "Get email server configuration",
            nickname = "EmailServerConfiguration",
            notes = "get Email server configuration",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/setting/email_server_configuration")
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<MailServerConfigurationResponse> getEmailServerConfiguration(@CurrentUser UserPrincipal userPrincipal) {
        log.info("[getEmailServerConfiguration] current user name: {}", userPrincipal.getUsername());
        AppProperties.Mail emailServerConfiguration = settingService.getEmailServerConfiguration();
        MailServerConfigurationResponse configuration = new MailServerConfigurationResponse();
        modelMapper.map(emailServerConfiguration, configuration);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(configuration);
    }

    @ApiOperation(value = "Save email server configuration",
            nickname = "SaveEmailServerConfiguration",
            notes = "save Email server configuration",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/setting/email_server_configuration")
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<Void> saveEmailServerConfiguration(@Valid @ValidErrorCode(AppException.ErrorCode.E_API_EMAILCONFIG_UPDATE_SETTING_VALIDATE_FAIL) @RequestBody EmailServerConfiguration configuration, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[saveEmailServerConfiguration] current user name: {}", userPrincipal.getUsername());
        settingService.saveEmailServerConfiguration(configuration, userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }


    @ApiOperation(value = "Save retention period",
            nickname = "SaveRetentionPeriod",
            notes = "save retention period",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @PostMapping("/setting/retention_period")
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<Void> saveRetentionPeriod(@RequestBody RetentionPeriodConfig retentionPeriodConfig, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[saveRetentionPeriod] current retentionPeriod: {} , user name: {}", retentionPeriodConfig, userPrincipal.getUsername());
        settingService.saveRetentionPeriod(retentionPeriodConfig.getRetentionPeriod(), userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }


    @ApiOperation(value = "Get retention period",
            nickname = "GetRetentionPeriod",
            notes = "Get retention period",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    @GetMapping("/setting/retention_period")
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<RetentionPeriodConfigResponse> getRetentionPeriod(@CurrentUser UserPrincipal userPrincipal) {
        log.info("[getRetentionPeriod]  user name: {}", userPrincipal.getUsername());
        Optional<Integer> retentionPeriodString= settingService.getRetentionPeriod();

        RetentionPeriodConfigResponse retentionPeriodConfigResponse=new RetentionPeriodConfigResponse();
        retentionPeriodConfigResponse.setRetentionPeriod(retentionPeriodString.orElse(null));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(retentionPeriodConfigResponse);
    }


}
