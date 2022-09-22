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
import com.xcal.api.repository.SettingRepository;
import com.xcal.api.util.CommonUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SettingService {

    @NonNull SettingRepository settingRepository;

    @NonNull AppProperties appProperties;

    @NonNull ModelMapper modelMapper;

    public static final String SETTING_PREFIX_MAIL = "app.mail.";

    public static final String SETTING_KEY_MAIL_PROTOCOL = "protocol";
    public static final String SETTING_KEY_MAIL_HOST = "host";
    public static final String SETTING_KEY_MAIL_PORT = "port";
    public static final String SETTING_KEY_MAIL_USERNAME = "username";
    public static final String SETTING_KEY_MAIL_PASSWORD = "password";
    public static final String SETTING_KEY_MAIL_FROM = "from";
    public static final String SETTING_KEY_MAIL_STARTTLS = "starttls";
    public static final String SETTING_KEY_MAIL_PREFIX = "prefix";

    public static final String SETTING_KEY_RETENTION_PERIOD="retention_period";
    public static final String SETTING_KEY_RETENTION_NUM="retention_num";

    public Optional<Setting> findByKey(String key) {
        log.debug("[findByKey] settingKey: {}", key);
        return settingRepository.findBySettingKey(key);
    }

    public boolean isKeyExist(String key) {
        log.debug("[isKeyExist] key: {}", key);
        return settingRepository.existsBySettingKey(key);
    }

    public Optional<Setting> findById(UUID id) {
        log.debug("[findById] id: {}", id);
        return settingRepository.findById(id);
    }

    public void deleteSetting(Setting setting) {
        settingRepository.delete(setting);
    }

    public Setting add(String settingKey, String settingValue, String currentUsername) throws AppException {
        log.info("[add] settingKey: {}, settingValue: {}", settingKey, settingValue);
        Date now = new Date();
        Optional<Setting> optionalSetting = settingRepository.findBySettingKey(settingKey);
        if (optionalSetting.isPresent()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_INCONSISTENT, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_SETTING_ADD_ALREADY_EXIST.unifyErrorCode,
                    CommonUtil.formatString("[{}] settingKey: {}", AppException.ErrorCode.E_API_SETTING_ADD_ALREADY_EXIST.messageTemplate, settingKey));
        }
        Setting setting = Setting.builder().settingKey(settingKey).settingValue(settingValue).build();
        setting.setModifiedBy(currentUsername);
        setting.setModifiedOn(now);
        return settingRepository.save(setting);
    }


    public List<Setting> findByKeyPrefix(String keyPrefix) {
        log.info("[findByKeyPrefix] keyPrefix: {}", keyPrefix);
        return settingRepository.findBySettingKeyStartingWith(keyPrefix);
    }


    public Setting add(SettingDto settingDto, String currentUsername) throws AppException {
        log.info("[add] setting: {}", settingDto);
        Date now = new Date();
        Optional<Setting> optionalSetting = settingRepository.findBySettingKey(settingDto.getSettingKey());
        if (optionalSetting.isPresent()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_INCONSISTENT, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_SETTING_ADD_ALREADY_EXIST.unifyErrorCode,
                    CommonUtil.formatString("[{}] settingKey: {}", AppException.ErrorCode.E_API_SETTING_ADD_ALREADY_EXIST.messageTemplate, settingDto.getSettingKey()));
        }
        Setting setting = Setting.builder().settingKey(settingDto.getSettingKey()).settingValue(settingDto.getSettingValue()).build();
        setting.setModifiedBy(currentUsername);
        setting.setModifiedOn(now);
        return settingRepository.save(setting);
    }

    public Page<Setting> findAll(Pageable pageable) {
        log.info("[findAll] pageable: {}", pageable);
        return settingRepository.findAll(pageable);
    }

    void checkSettingValue(String value) throws AppException {
        log.info("[checkSettingValue] value:{}", value);
        Integer settingValue;
        try {
            settingValue = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            log.error("[checkSettingValue] {}", Arrays.toString(e.getStackTrace()));
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] invalid setting value: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, value));
        }
        if(settingValue < 0) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] invalid setting value: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, value));
        }
    }

    public Setting update(SettingDto settingDto, String currentUsername) throws AppException {
        log.info("[update] setting: {}, currentUsername: {}", settingDto, currentUsername);
        Setting setting = this.findByKey(settingDto.getSettingKey()).orElseThrow (
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.unifyErrorCode,
        CommonUtil.formatString("[{}] settingKey: {}", AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.messageTemplate, settingDto.getSettingKey())));

        if(StringUtils.equals(setting.getSettingKey(), SETTING_KEY_RETENTION_PERIOD)) {
           checkSettingValue(settingDto.getSettingValue());
        }
        else if(StringUtils.equals(setting.getSettingKey(), SETTING_KEY_RETENTION_NUM)) {
            checkSettingValue(settingDto.getSettingValue());
        }

        setting.setSettingValue(settingDto.getSettingValue());
        setting.setModifiedOn(new Date());
        setting.setModifiedBy(currentUsername);

        return settingRepository.save(setting);
    }

    public void updateEmailServerConfiguration() {
        log.info("[updateEmailServerConfiguration]");
        List<Setting> settingList = settingRepository.findBySettingKeyStartingWith(SETTING_PREFIX_MAIL);
        settingList.forEach( setting -> {
            String key = StringUtils.substringAfter(setting.getSettingKey(), SETTING_PREFIX_MAIL);
            switch (key) {
                case SETTING_KEY_MAIL_PROTOCOL:
                    appProperties.getMail().setProtocol(setting.getSettingValue());
                    break;
                case SETTING_KEY_MAIL_HOST:
                    appProperties.getMail().setHost(setting.getSettingValue());
                    break;
                case SETTING_KEY_MAIL_PORT:
                    appProperties.getMail().setPort(NumberUtils.toInt(setting.getSettingValue(),25));
                    break;
                case SETTING_KEY_MAIL_USERNAME:
                    appProperties.getMail().setUsername(setting.getSettingValue());
                    break;
                case SETTING_KEY_MAIL_PASSWORD:
                    appProperties.getMail().setPassword(setting.getSettingValue());
                    break;
                case SETTING_KEY_MAIL_FROM:
                    appProperties.getMail().setFrom(setting.getSettingValue());
                    break;
                case SETTING_KEY_MAIL_STARTTLS:
                    appProperties.getMail().setStarttls(setting.getSettingValue());
                    break;
                case SETTING_KEY_MAIL_PREFIX:
                    appProperties.getMail().setPrefix(setting.getSettingValue());
                    break;
                default:
                    break;
            }
        });
    }

    public List<Setting> saveEmailServerConfiguration(EmailServerConfiguration configuration, String currentUsername) {
        log.info("[saveEmailServerConfiguration] currentUsername: {}", currentUsername);
        Date date = new Date();
        List<Setting> settingList = new ArrayList<>();
        List<Setting> settings = settingRepository.findBySettingKeyStartingWith(SETTING_PREFIX_MAIL);

        Setting setting = this.getSettingByKey(settings, SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_PROTOCOL);
        if (setting != null) {
            setting.setSettingValue(configuration.getProtocol().toString());
            setting.setModifiedBy(currentUsername);
            setting.setModifiedOn(date);
            settingList.add(setting);
        } else {
            settingList.add(Setting.builder().settingKey(SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_PROTOCOL).settingValue(configuration.getProtocol().toString()).modifiedBy(currentUsername).modifiedOn(date).build());
        }

        setting = this.getSettingByKey(settings, SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_HOST);
        if (setting != null) {
            setting.setSettingValue(configuration.getHost());
            setting.setModifiedBy(currentUsername);
            setting.setModifiedOn(date);
            settingList.add(setting);
        } else {
            settingList.add(Setting.builder().settingKey(SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_HOST).settingValue(configuration.getHost()).modifiedBy(currentUsername).modifiedOn(date).build());
        }

        setting = this.getSettingByKey(settings, SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_PORT);
        if (setting != null) {
            setting.setSettingValue(configuration.getPort() + "");
            setting.setModifiedBy(currentUsername);
            setting.setModifiedOn(date);
            settingList.add(setting);
        } else {
            settingList.add(Setting.builder().settingKey(SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_PORT).settingValue(configuration.getPort() + "").modifiedBy(currentUsername).modifiedOn(date).build());
        }

        setting = this.getSettingByKey(settings, SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_USERNAME);
        if (setting != null) {
            setting.setSettingValue(configuration.getUsername());
            setting.setModifiedBy(currentUsername);
            setting.setModifiedOn(date);
            settingList.add(setting);
        } else {
            settingList.add(Setting.builder().settingKey(SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_USERNAME).settingValue(configuration.getUsername()).modifiedBy(currentUsername).modifiedOn(date).build());
        }

        setting = this.getSettingByKey(settings, SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_STARTTLS);
        if (setting != null) {
            setting.setSettingValue(configuration.getStarttls().toString());
            setting.setModifiedBy(currentUsername);
            setting.setModifiedOn(date);
            settingList.add(setting);
        } else {
            settingList.add(Setting.builder().settingKey(SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_STARTTLS).settingValue(configuration.getStarttls().toString()).modifiedBy(currentUsername).modifiedOn(date).build());
        }

        if (StringUtils.isNotBlank(configuration.getPassword())) {
            setting = this.getSettingByKey(settings, SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_PASSWORD);
            if (setting != null) {
                setting.setSettingValue(configuration.getPassword());
                setting.setModifiedBy(currentUsername);
                setting.setModifiedOn(date);
                settingList.add(setting);
            } else {
                settingList.add(Setting.builder().settingKey(SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_PASSWORD).settingValue(configuration.getPassword()).modifiedBy(currentUsername).modifiedOn(date).build());
            }
        }

        if (StringUtils.isNotBlank(configuration.getPrefix())) {
            setting = this.getSettingByKey(settings, SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_PREFIX);
            if (setting != null) {
                setting.setSettingValue(configuration.getPrefix());
                setting.setModifiedBy(currentUsername);
                setting.setModifiedOn(date);
                settingList.add(setting);
            } else {
                settingList.add(Setting.builder().settingKey(SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_PREFIX).settingValue(configuration.getPrefix()).modifiedBy(currentUsername).modifiedOn(date).build());
            }
        }

        if (StringUtils.isNotBlank(configuration.getFrom())) {
            setting = this.getSettingByKey(settings, SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_FROM);
            if (setting != null) {
                setting.setSettingValue(configuration.getFrom());
                setting.setModifiedBy(currentUsername);
                setting.setModifiedOn(date);
                settingList.add(setting);
            } else {
                settingList.add(Setting.builder().settingKey(SETTING_PREFIX_MAIL+SETTING_KEY_MAIL_FROM).settingValue(configuration.getFrom()).modifiedBy(currentUsername).modifiedOn(date).build());
            }
        }

        settingList = settingRepository.saveAll(settingList);
        return settingList;
    }

    private Setting getSettingByKey(List<Setting> settings, String key) {
        Setting resultSetting = null;
        for (Setting setting : settings) {
            if (StringUtils.equalsIgnoreCase(setting.getSettingKey(), key)) {
                resultSetting = setting;
                break;
            }
        }
        return resultSetting;
    }

    public AppProperties.Mail getEmailServerConfiguration() {
        this.updateEmailServerConfiguration();
        return appProperties.getMail();
    }


    public void saveRetentionPeriod(Integer retentionPeriod, String currentUsername) {
        log.info("[saveRetentionPeriod] currentUsername: {}", currentUsername);

        if(retentionPeriod<0){
           throw new IllegalArgumentException("Invalid retentionPeriod:"+retentionPeriod);
        }

        //get setting
        Optional<Setting> retentionPeriodSettingOptional = settingRepository.findBySettingKey(SETTING_KEY_RETENTION_PERIOD);
        Setting retentionPeriodSetting=retentionPeriodSettingOptional.orElse( Setting.builder()
                .settingKey(SETTING_KEY_RETENTION_PERIOD)
                .build());

        //assign value
        retentionPeriodSetting.setSettingValue(String.valueOf(retentionPeriod));
        retentionPeriodSetting.setModifiedBy(currentUsername);
        retentionPeriodSetting.setModifiedOn(new Date());

        //insert/update to database
        settingRepository.save(retentionPeriodSetting);

    }

    public Optional<Integer> getRetentionPeriod(){
        Optional<Setting> setting = settingRepository.findBySettingKey(SETTING_KEY_RETENTION_PERIOD);
        if(setting.isPresent()){
            return Optional.of(Integer.parseInt(setting.get().getSettingValue()));
        }else{
            return Optional.empty();
        }

    }

}
