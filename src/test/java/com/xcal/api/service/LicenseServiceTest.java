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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class LicenseServiceTest {
    private LicenseService licenseService;
    private LicenseRepository licenseRepository;
    private SettingService settingService;

    private String companyName = "Internal Testing License";
    private String productName = "爱科识C/C++扫描软件 [简称: XcalScan C/C++] 1.0";
    private String licenseNumber = "internal_001";
    private License license = License.builder().companyName(companyName).licenseNumber(licenseNumber).productName(productName).expiresOn(new Date(new Date().getTime()+1000000)).build();
    /*
    * {
    *   "company_name":"Internal Testing License",
    *   "max_users":"20",
    *   "expires_on":4102415999000,
    *   "license_number":"internal_001",
    *   "license_byte":"fake_byte"
    * }
    * */
    private String encryptLicense = "2j6qqzsPCYmCOtkvoI1azWx2DpmYX32FHcP/5l4onrnMNPiFe4S57OkDRygtYtddkkLVTJ9pKmsP1a8ef6hTjKGIRLVw/HjN69+ZSq9CBtPZcTmWmaGzvmfEjd+2Np0fuPJix7vSUg/3WwUu2YbAl3bj67ZE607CceP3GaVDkYzYCNwbzTZ34EWQuM9cm1ym9gYj/p6hllr4o1RYaNpRFxjtYpluRP2F3Ihr2ZSbLjs=";
    private MockMultipartFile encryptLicenseFile = new MockMultipartFile("upload_file",
            "license_encrypt1.txt",
            MediaType.TEXT_PLAIN_VALUE,
            encryptLicense.getBytes());
    private Setting licensePublic = Setting.builder()
            .settingKey(LicenseService.LICENSE_PUBLIC_KEY)
            .settingValue("MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIf2tYNZ1NjuxhjfQUJIVYwUPzU0IRnGyLdeFvPWyu3xSZuZvJ0V3eZgLTLvcqnzcXMDA/UnfpaBJ0Bol8NORz8CAwEAAQ==")
            .build();
    private Setting licenseEncryptAesKey = Setting.builder()
            .settingKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)
            .settingValue("I2rTqnHWsYzC0UdSciq3sOiTxBQrdOwmwl0mcJK/YR4XuGzhLtjZU497NRyJUiUA/sVIHerCPIO0lNinCvKYlw==")
            .build();

    @BeforeEach
    void setup() {
        licenseRepository = mock(LicenseRepository.class);
        settingService = mock(SettingService.class);
        licenseService = new LicenseService(licenseRepository, settingService);
        when(licenseRepository.save(any())).thenReturn(license);
    }

    @Test
    void findActiveLicenseTestSuccess() {
        log.info("[findActiveLicenseTestSuccess]");
        when(licenseRepository.findByStatus(License.Status.ACTIVE)).thenReturn(Optional.of(license));
        Optional<License> result = licenseService.findActiveLicense();
        assertTrue(result.isPresent());
        assertEquals(companyName, result.get().getCompanyName());
        assertEquals(licenseNumber, result.get().getLicenseNumber());
        assertEquals(productName,result.get().getProductName());
    }

    @Test
    void findActiveLicenseTestFail() {
        log.info("[findActiveLicenseTestSuccess]");
        when(licenseRepository.findByStatus(License.Status.ACTIVE)).thenReturn(Optional.empty());
        assertFalse(licenseService.findActiveLicense().isPresent());
    }

    @Test
    void updateLicenseTestSuccess() throws AppException {
        log.info("[updateLicenseTestSuccess]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.of(licensePublic));
        when(settingService.findByKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)).thenReturn(Optional.of(licenseEncryptAesKey));
        License result = licenseService.updateLicense(encryptLicenseFile, "user");
        assertEquals(companyName, result.getCompanyName());
        assertEquals(licenseNumber, result.getLicenseNumber());
        assertEquals(productName,result.getProductName());
    }

    @Test
    void updateLicenseTestLicenseExistingSuccess() throws AppException {
        log.info("[updateLicenseTestLicenseExistingSuccess]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.of(licensePublic));
        when(settingService.findByKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)).thenReturn(Optional.of(licenseEncryptAesKey));
        License result = licenseService.updateLicense(encryptLicenseFile, "user");
        assertEquals(companyName, result.getCompanyName());
        assertEquals(licenseNumber, result.getLicenseNumber());
        assertEquals(productName,result.getProductName());
    }

    @Test
    void updateLicense_LicenseExist_Success() throws AppException {
        log.info("[updateLicenseTestLicenseExistingSuccess]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.of(licensePublic));
        when(settingService.findByKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)).thenReturn(Optional.of(licenseEncryptAesKey));
        when(licenseRepository.findByStatus(License.Status.ACTIVE)).thenReturn(Optional.of(license));
        License result = licenseService.updateLicense(encryptLicenseFile, "user");
        assertEquals(companyName, result.getCompanyName());
        assertEquals(licenseNumber, result.getLicenseNumber());
        assertEquals(productName,result.getProductName());
    }

    @Test
    void updateLicense_PublicKeyNotFound_ThrowAppException() {
        log.info("[updateLicense_PublicKeyNotFound_ThrowAppException]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.empty());
        AppException appException = assertThrows(AppException.class, () -> licenseService.updateLicense(encryptLicenseFile, "user"));
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, appException.getResponseCode());
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_DATA_NOT_FOUND, appException.getErrorCode());
    }

    @Test
    void updateLicense_EncryptAESKeyNotFound_ThrowAppException() {
        log.info("[updateLicense_EncryptAESKeyNotFound_ThrowAppException]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.of(licensePublic));
        when(settingService.findByKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)).thenReturn(Optional.empty());
        AppException appException = assertThrows(AppException.class, () -> licenseService.updateLicense(encryptLicenseFile, "user"));
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, appException.getResponseCode());
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_DATA_NOT_FOUND, appException.getErrorCode());
    }

    @Test
    void updateLicenseTestLicenseExpiredException() {
        log.info("[updateLicenseTestLicenseExpiredException]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.of(licensePublic));
        when(settingService.findByKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)).thenReturn(Optional.of(licenseEncryptAesKey));
        /*
         * {
         *   "company_name":"Internal Testing License",
         *   "max_users":"20",
         *   "expires_on":1546271999000,
         *   "license_number":"internal_001",
         *   "license_byte":"fake_byte"
         * }
         * */
        String expiredLicense = "2j6qqzsPCYmCOtkvoI1azWx2DpmYX32FHcP/5l4onrnMNPiFe4S57OkDRygtYtddkkLVTJ9pKmsP1a8ef6hTjKGIRLVw/HjN69+ZSq9CBtNiyld6PlhZsB+kp88lensrYaW60G6nKJH6MjVYf0dP2iwxvcrAul5LleJjZWHAL8+z/WNoaigadK4dlq3QYS02yd8P8mXCFsESgGhWbLw0Aak/BckwmnmZoMfVUq5sXsw=";
        MockMultipartFile expiredLicenseFile = new MockMultipartFile("upload_file",
                "expired_license.txt",
                MediaType.TEXT_PLAIN_VALUE,
                expiredLicense.getBytes());
        assertThrows(AppException.class, () -> licenseService.updateLicense(expiredLicenseFile, "user"));
    }

    @Test
    void updateLicense_InvalidExpiresOn_AppException() {
        log.info("[updateLicense_InvalidExpiresOn_AppException]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.of(licensePublic));
        when(settingService.findByKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)).thenReturn(Optional.of(licenseEncryptAesKey));
        /*
         * {
         *   "company_name":"Internal Testing License",
         *   "max_users":"20",
         *   "expires_on": "not_a_time",
         *   "license_number":"internal_001",
         *   "license_byte":"fake_byte"
         * }
         * */
        String invalidLicense = "WAbqxdF7R/HeCKEAKiqaseMibDP6o6y4tFXwReK4/6co0aLbESK4LOqadaH2gBxRqDYPIY63akRzDI2HaDFXxqL0xJIVjor9gDQgUcENeidHW18ptdA1pJXSM2VPHpcWPDSeslQ3Jd+vKncTtkWznQ3mAIPcpC7mvyVZPmSUYPXcTMDobbg9Fc5qd6CpdriZg1K1dU7jChfZMxchjowHf8icikod3TWk1xKuvIa7YDw=";
        MockMultipartFile invalidLicenseFile = new MockMultipartFile("upload_file",
                "expired_license.txt",
                MediaType.TEXT_PLAIN_VALUE,
                invalidLicense.getBytes());
        AppException appException = assertThrows(AppException.class, () -> licenseService.updateLicense(invalidLicenseFile, "user"));
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, appException.getResponseCode());
        assertEquals(AppException.ERROR_CODE_BAD_REQUEST, appException.getErrorCode());
    }

    @Test
    void updateLicense_InvalidLicense_IOException() {
        log.info("[updateLicenseTestIOException]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.of(licensePublic));
        when(settingService.findByKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)).thenReturn(Optional.of(licenseEncryptAesKey));
        /* // invalid format of the content, not json
         *   "company_name":"Internal Testing License",
         *   "max_users":"20",
         *   "expires_on":1546271999000,
         *   "license_number":"internal_001",
         *   "license_byte":"fake_byte"
         * */
        String invalidLicense = "Zg/BnZeyQ0i0R84MYVLLzuMibDP6o6y4tFXwReK4/6co0aLbESK4LOqadaH2gBxRqDYPIY63akRzDI2HaDFXxqL0xJIVjor9gDQgUcENeicUSdbXGWlOa6BRaZR1X8bp44lx+f0XsV3KRyp6ldhkbFN2dhOQl8/BXypIb7GQqKjSEb/vjSfMANK6fp/UAXno83JSo5kcvLt+yMyjoXAu+g==";
        MockMultipartFile invalidLicenseFile = new MockMultipartFile("upload_file",
                "invalid_license.txt",
                MediaType.TEXT_PLAIN_VALUE,
                invalidLicense.getBytes());
        AppException appException = assertThrows(AppException.class, () -> licenseService.updateLicense(invalidLicenseFile, "user"));
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, appException.getResponseCode());
        assertEquals(AppException.ERROR_CODE_BAD_REQUEST, appException.getErrorCode());
    }

    @Test
    void updateLicense_ExpiredOnIsNull_AppException() {
        log.info("[updateLicense_ExpiredOnIsNull_AppException]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.of(licensePublic));
        when(settingService.findByKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)).thenReturn(Optional.of(licenseEncryptAesKey));
        /*
         * {
         *   "company_name":"Internal Testing License",
         *   "max_users":"20",
         *   "license_number":"internal_001",
         *   "license_byte":"fake_byte"
         * }
         * */
        String invalidLicense = "2j6qqzsPCYmCOtkvoI1azWx2DpmYX32FHcP/5l4onrnMNPiFe4S57OkDRygtYtddkkLVTJ9pKmsP1a8ef6hTjCACW4RSlcZ/a8d8HJEfHYj5K5iuOTp+ys5vOF4KqWRSsvgUd5Vb/rjbH17oudgyOBSOgL7VQP9ylCGuu99eomSlifaYUapmMncUSUmOFqoi";
        MockMultipartFile invalidLicenseFile = new MockMultipartFile("upload_file",
                "invalid_license.txt",
                MediaType.TEXT_PLAIN_VALUE,
                invalidLicense.getBytes());
        AppException appException = assertThrows(AppException.class, () -> licenseService.updateLicense(invalidLicenseFile, "user"));
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, appException.getResponseCode());
        assertEquals(AppException.ERROR_CODE_BAD_REQUEST, appException.getErrorCode());
    }

    @Test
    void updateLicense_InvalidLicenseCannotDecrypt_GeneralSecurityException() {
        log.info("[updateLicense_InvalidLicenseCannotDecrypt_GeneralSecurityException]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.of(licensePublic));
        when(settingService.findByKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)).thenReturn(Optional.of(licenseEncryptAesKey));
        String invalidLicense = "Zg/BnZeyQ0i0R84MYVLLzuMibDP6o6y4tFXwReK4/6co0aLbESK4LOqadaH2gBxRqDYPIY63akRzDI2HaDFXxqL0xJIVjor9gDQgUcENeicUSdbXGWlOa6BRaZR1X8bp44lx+f0XsV3KRyp6ldhkbFN2dhOQl8/BXypIb7GQqKjSEb/vjSfMANK6fp/UAXno83JSo5kcvg==";
        MockMultipartFile invalidLicenseFile = new MockMultipartFile("upload_file",
                "expired_license.txt",
                MediaType.TEXT_PLAIN_VALUE,
                invalidLicense.getBytes());
        assertThrows(AppException.class, () -> licenseService.updateLicense(invalidLicenseFile, "user"));
    }

    @Test
    void updateLicense_InvalidLicenseFileByteIOException_AppException() throws IOException {
        log.info("[updateLicense_InvalidLicenseFileByteIOException_AppException]");
        when(settingService.findByKey(LicenseService.LICENSE_PUBLIC_KEY)).thenReturn(Optional.of(licensePublic));
        when(settingService.findByKey(LicenseService.LICENSE_ENCRYPT_AES_KEY)).thenReturn(Optional.of(licenseEncryptAesKey));
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenThrow(new IOException());
        AppException appException = assertThrows(AppException.class, () -> licenseService.updateLicense(mockFile, "user"));
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, appException.getResponseCode());
        assertEquals(AppException.ERROR_CODE_BAD_REQUEST, appException.getErrorCode());
    }

    @Test
    void checkLicenseTestSuccess() {
        log.info("[checkLicenseTestSuccess]");
        when(licenseRepository.findByStatus(License.Status.ACTIVE)).thenReturn(Optional.of(license));
        assertDoesNotThrow(() -> licenseService.checkLicense());
    }

    @Test
    void checkLicense_NoActiveLicense_AppException() {
        log.info("[checkLicense_NoActiveLicense_AppException]");
        when(licenseRepository.findByStatus(License.Status.ACTIVE)).thenReturn(Optional.empty());
        ValidationResult validationResult = licenseService.checkLicense();
        assertEquals(AppException.LEVEL_ERROR, validationResult.getException().getLevel());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, validationResult.getException().getResponseCode());
        assertEquals(AppException.ERROR_CODE_DATA_NOT_FOUND, validationResult.getException().getErrorCode());
    }

    @Test
    void checkLicense_LicenseExpired_AppException(){
        log.info("[checkLicense_LicenseExpired_AppException]");
        when(licenseRepository.findByStatus(License.Status.ACTIVE)).thenReturn(Optional.of(License.builder().expiresOn(new Date(new Date().getTime() - 100)).build()));
        ValidationResult validationResult = licenseService.checkLicense();
        assertEquals(AppException.LEVEL_ERROR, validationResult.getException().getLevel());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,  validationResult.getException().getResponseCode());
        assertEquals(AppException.ERROR_CODE_BAD_REQUEST,  validationResult.getException().getErrorCode());
    }

    @Test
    void checkLicense_LicenseExpiresOnAttributeIsNull_AppException(){
        log.info("[checkLicense_LicenseExpiresOnAttributeIsNull_AppException]");
        when(licenseRepository.findByStatus(License.Status.ACTIVE)).thenReturn(Optional.of(License.builder().expiresOn(null).build()));
        ValidationResult validationResult = licenseService.checkLicense();
        assertEquals(AppException.LEVEL_ERROR, validationResult.getException().getLevel());
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, validationResult.getException().getResponseCode());
        assertEquals(AppException.ERROR_CODE_INTERNAL_ERROR, validationResult.getException().getErrorCode());
    }
}
