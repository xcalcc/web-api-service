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

import com.xcal.api.entity.License;
import com.xcal.api.entity.Setting;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.ValidationResult;
import com.xcal.api.repository.LicenseRepository;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.MessagesTemplate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LicenseService {

    static final String LICENSE_PUBLIC_KEY = "license.public.key";
    static final String LICENSE_ENCRYPT_AES_KEY = "license.encrypt.aes.key";

    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_AES = "AES";
    private static final String ALGORITHM_RANDOM = "SHA1PRNG";
    private static final int AES_KEY_LENGTH = 128;

    @NonNull LicenseRepository licenseRepository;
    @NonNull SettingService settingService;

    public Optional<License> findActiveLicense() {
        log.trace("[findActiveLicense] ");
        return licenseRepository.findByStatus(License.Status.ACTIVE);
    }

    public License updateLicense(@NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL) MultipartFile file, String currentUserName) throws AppException {
        log.info("[updateLicense] file size: {}", file.getSize());
        //get public key
        Setting licensePublicKeySetting = settingService.findByKey(LICENSE_PUBLIC_KEY)
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_LICENSE_UPDATE_PUBLIC_KEY_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] settingKey: {}", AppException.ErrorCode.E_API_LICENSE_UPDATE_PUBLIC_KEY_NOT_FOUND.messageTemplate, LICENSE_PUBLIC_KEY)));
        String licensePublicKey = licensePublicKeySetting.getSettingValue();
        //get encrypted AES key
        Setting licenseEncryptAESKeySetting = settingService.findByKey(LICENSE_ENCRYPT_AES_KEY)
                .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_LICENSE_UPDATE_ENCRYPT_AES_KEY_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] settingKey: {}", AppException.ErrorCode.E_API_LICENSE_UPDATE_ENCRYPT_AES_KEY_NOT_FOUND.messageTemplate, LICENSE_ENCRYPT_AES_KEY)));
        String licenseEncryptAESKey = licenseEncryptAESKeySetting.getSettingValue();

        //decrypt file to license string
        byte[] decryptLicenseBytes;
        try {
            decryptLicenseBytes = file.getBytes();
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST,
                    HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.messageTemplate);
        }
        String licenseString = this.decryptLicense(new String(decryptLicenseBytes), licensePublicKey, licenseEncryptAESKey);


        Map<String, String> licenseMap;
        try {
            licenseMap = CommonUtil.convertStringContentToMap(licenseString);
        } catch (AppException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST,
                    HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.messageTemplate, e);
        }
        Date expiresOn;
        if (StringUtils.isBlank(licenseMap.get("expires_on"))) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST,
                    HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.messageTemplate);
        } else if (!NumberUtils.isParsable(licenseMap.get("expires_on"))) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST,
                    HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.messageTemplate);
        } else {
            expiresOn = new Date(NumberUtils.toLong(licenseMap.get("expires_on")));
        }
        Date now = new Date();
        if (expiresOn.before(now)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST,
                    HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_LICENSE_COMMON_EXPIRED.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_EXPIRED.messageTemplate);
        }
        License license = License.builder()
                .companyName(licenseMap.get("company_name"))
                .productName(licenseMap.get("product_name"))
                .licenseNumber(licenseMap.get("license_number"))
                .licenseByte(licenseMap.get("license_byte"))
                .maxUsers(Integer.valueOf(licenseMap.get("max_users")))
                .expiresOn(expiresOn)
                .build();
        return this.saveLicense(license, currentUserName);
    }

    private License saveLicense(License license, String currentUserName) {
        log.info("[saveLicense] license: {}, principal user: {}", license, currentUserName);
        Optional<License> activeLicenseOptional = licenseRepository.findByStatus(License.Status.ACTIVE);
        if (activeLicenseOptional.isPresent()) {
            License activeLicense = activeLicenseOptional.get();
            activeLicense.setStatus(License.Status.INACTIVE);
            licenseRepository.save(activeLicense);
        }

        license.setStatus(License.Status.ACTIVE);
        Date now = new Date();
        license.setCreatedOn(now);
        license.setModifiedOn(now);
        license.setCreatedBy(currentUserName);
        license.setModifiedBy(currentUserName);

        license = licenseRepository.save(license);
        return license;
    }

    private String decryptLicense(String encryptLicense, String publicKeyStr, String encryptAESKey) throws AppException {
        log.info("[decryptLicense] encryptLicense: {}, publicKeyStr: {}, encryptAESKey: {}", encryptLicense, publicKeyStr, encryptAESKey);
        String license;
        try {
            PublicKey publicKey = this.getPublicKey(publicKeyStr);
            byte[] aesKey = this.decryptAESKey(encryptAESKey, publicKey);
            log.debug("[decryptLicense] aesKey after decrypt: {}", Base64.getEncoder().encodeToString(aesKey));

            license = this.decryptLicenseWithAES(encryptLicense, aesKey);
            log.debug("[decryptLicense] License after decrypt: {}", license);
        } catch (GeneralSecurityException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.unifyErrorCode,
                    CommonUtil.formatString("[{}] {}", AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.messageTemplate, e.getMessage()));

        }

        return license;
    }

    public PublicKey getPublicKey(String key) throws GeneralSecurityException {
        log.info("[getPublicKey] key: {}", key);
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        return keyFactory.generatePublic(keySpec);
    }

    public byte[] decryptAESKey(String encryptAESKey, PublicKey publicKey) throws GeneralSecurityException {
        log.info("[decryptAESKey] encryptAESKey: {}", encryptAESKey);
        byte[] aesKeyByte = Base64.getDecoder().decode(encryptAESKey);
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(aesKeyByte);
    }

    private String decryptLicenseWithAES(String content, byte[] aesKey) throws GeneralSecurityException {
        log.info("[decryptLicenseWithAES] content: {}, aesKey: {}", content, new String(aesKey));
        KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM_AES);
        SecureRandom secureRandom = SecureRandom.getInstance(ALGORITHM_RANDOM);
        secureRandom.setSeed(aesKey);
        kgen.init(AES_KEY_LENGTH, secureRandom);
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, ALGORITHM_AES);
        Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] bs = Base64.getDecoder().decode(content);
        byte[] result = cipher.doFinal(bs);
        return new String(result);
    }

    public ValidationResult checkLicense() {
        log.info("[checkLicense]");
        Optional<License> licenseOptional = findActiveLicense();
        ValidationResult.Status status;
        AppException appException = null;
        if (licenseOptional.isPresent()) {
            License license = licenseOptional.get();
            Date now = new Date();
            if (license.getExpiresOn() == null) {
                status = ValidationResult.Status.FAIL;
                appException = new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR,
                        HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_INVALID_LICENSE.messageTemplate);
            } else if (license.getExpiresOn().before(now)) {
                status = ValidationResult.Status.FAIL;
                appException = new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST,
                        HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_LICENSE_COMMON_EXPIRED.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_EXPIRED.messageTemplate);
            } else {
                status = ValidationResult.Status.SUCCESS;
            }
        } else {
            status = ValidationResult.Status.FAIL;
            appException = new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND,
                    HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_LICENSE_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_NOT_FOUND.messageTemplate);
        }

        return ValidationResult.builder().status(status).exception(appException).build();
    }
}
