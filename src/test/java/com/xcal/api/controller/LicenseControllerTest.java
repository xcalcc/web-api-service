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
import com.xcal.api.config.WithMockCustomUser;
import com.xcal.api.entity.License;
import com.xcal.api.service.FileService;
import com.xcal.api.service.LicenseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class LicenseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private LicenseService licenseService;

    @MockBean
    private FileService fileService;

    @Autowired
    ObjectMapper om;

    private License license;
    private UUID id = UUID.fromString("11111111-1111-1111-1112-111111111113");
    private String companyName = "Xcalibyte Ltd.";
    private String licenseNumber = DigestUtils.sha256Hex(UUID.randomUUID().toString());

    @BeforeEach
    void setup() {
        license = License.builder().id(id)
                .licenseNumber(licenseNumber)
                .companyName(companyName)
                .expiresOn(DateUtils.ceiling(new Date(), Calendar.YEAR))
                .maxUsers(20)
                .status(License.Status.ACTIVE).build();

        when(licenseService.findActiveLicense()).thenReturn(Optional.of(license));
    }

    @Test
    @WithMockCustomUser()
    void updateLicenseTest() throws Exception {
        log.info("[updateLicenseTest] license: {}", license);
        String encryptLicense = "LvfZZAtSXpqAb139bf8YHYEVgv63YUvL1KmwWuR7Csx3MMMCfCfFpK/xGXAbFTbe7I4HkdxjJyxcTN4Oy7DOoPJmWQOfCt93YJtGFCsxZaRjdwNGMEvz0XtjXZ8Ws2WJ/l9rSjdwM8ROb82U1L7ABPqQOBfzwqEpXMommQUnXcvd8KGgchAxXbivqJ4kSO6xl435o/+Z8kBtI7C7pC4g6P+cQw3dFILbgzq5K/MoQGpGFamuVS0N8Q24ygVdb+4TeyrAIf4k9fM1gp6q522Yw3Ty79y7qlIoBdG5wW07SliMe3Odw2tRTTiw2OivSy4WZev6IIMxjjTVRE+qr9xqnnGZvoC2uyn2c9oiILit0rUQKI5hMtX10ArVTaKxlARne4YBbw9ZRTdivkDZwTnm8GXr+iCDMY401URPqq/cap5NyrXxfH3/EiqgGEMdj26y7u6EVlsdLlNTwmisx70328b5R/CE0wpXWavicTSstMCJcVi7QsP2a3sTDx2BPModT0G4uUDbOqBFY4QWy5zKd06ZV4Fw13W0OWiwhC65KhoKxvjZgDGax8Enjq4GnU+pnZLVHMLLUOeHloaqE0JrDkQGDyky02Fkt2rDeZNAXQRoiZOmD0U1eezHRObbkkVhnyfdPgie+Vr0vasINT94xxSs6N2vYYSWPsHIJs4/H28L8a9ms7kbt1yNInJ+8kMHkdoGph3tBk0SXDnZifqDMxHFFESeVmFAJy+1jKcx47qLP6o2InIQC4ZfiQ0d0Tw8MqpOEygFMgOnLjQzpBip0XQlRK4LgPKc8RJMxnwIPCVMkMW+i5FAElji1vEN3hmiGEQYBh9oRUNeehJUXW8itzRCJkleIWjGHh+1ElhlXBTBm3gWcvyMWgaRJmSDQlNVGbB0glUtifzTSVHSTOZi6zXlDnTpYZwbpBsXMcFlyn6pPwXJMJp5maDH1VKubF7M";
        MockMultipartFile encryptLicenseFile = new MockMultipartFile("upload_file",
                "license_encrypt1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                encryptLicense.getBytes());
        when(licenseService.updateLicense(eq(encryptLicenseFile), anyString())).thenReturn(license);
        mockMvc.perform(multipart("/api/license_service/v2/license")
                .file(encryptLicenseFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.licenseNumber").value(licenseNumber))
                .andExpect(jsonPath("$.companyName").value(companyName));
    }


    @Test
    @WithMockCustomUser()
    void updateLicense_CheckSumIsNotIntegrity_ThrowException() throws Exception {
        log.info("[updateLicenseTest] license: {}", license);
        String encryptLicense = "LvfZZAtSXpqAb139bf8YHYEVgv63YUvL1KmwWuR7Csx3MMMCfCfFpK/xGXAbFTbe7I4HkdxjJyxcTN4Oy7DOoPJmWQOfCt93YJtGFCsxZaRjdwNGMEvz0XtjXZ8Ws2WJ/l9rSjdwM8ROb82U1L7ABPqQOBfzwqEpXMommQUnXcvd8KGgchAxXbivqJ4kSO6xl435o/+Z8kBtI7C7pC4g6P+cQw3dFILbgzq5K/MoQGpGFamuVS0N8Q24ygVdb+4TeyrAIf4k9fM1gp6q522Yw3Ty79y7qlIoBdG5wW07SliMe3Odw2tRTTiw2OivSy4WZev6IIMxjjTVRE+qr9xqnnGZvoC2uyn2c9oiILit0rUQKI5hMtX10ArVTaKxlARne4YBbw9ZRTdivkDZwTnm8GXr+iCDMY401URPqq/cap5NyrXxfH3/EiqgGEMdj26y7u6EVlsdLlNTwmisx70328b5R/CE0wpXWavicTSstMCJcVi7QsP2a3sTDx2BPModT0G4uUDbOqBFY4QWy5zKd06ZV4Fw13W0OWiwhC65KhoKxvjZgDGax8Enjq4GnU+pnZLVHMLLUOeHloaqE0JrDkQGDyky02Fkt2rDeZNAXQRoiZOmD0U1eezHRObbkkVhnyfdPgie+Vr0vasINT94xxSs6N2vYYSWPsHIJs4/H28L8a9ms7kbt1yNInJ+8kMHkdoGph3tBk0SXDnZifqDMxHFFESeVmFAJy+1jKcx47qLP6o2InIQC4ZfiQ0d0Tw8MqpOEygFMgOnLjQzpBip0XQlRK4LgPKc8RJMxnwIPCVMkMW+i5FAElji1vEN3hmiGEQYBh9oRUNeehJUXW8itzRCJkleIWjGHh+1ElhlXBTBm3gWcvyMWgaRJmSDQlNVGbB0glUtifzTSVHSTOZi6zXlDnTpYZwbpBsXMcFlyn6pPwXJMJp5maDH1VKubF7M";
        MockMultipartFile encryptLicenseFile = new MockMultipartFile("upload_file",
                "license_encrypt1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                encryptLicense.getBytes());
        when(fileService.checkIntegrityWithCrc32(any(MultipartFile.class), eq("1234"))).thenReturn(false);
        when(licenseService.updateLicense(eq(encryptLicenseFile), anyString())).thenReturn(license);
        mockMvc.perform(multipart("/api/license_service/v2/license")
                .file(encryptLicenseFile).param("file_checksum", "1234")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value("ERROR"))
                .andExpect(jsonPath("$.responseCode").value("409"))
                .andExpect(jsonPath("$.errorCode").value("DATA_INCONSISTENT"));
    }

    @Test
    @WithMockCustomUser()
    void showLicenseTest() throws Exception {
        log.info("[showLicenseTest]");
        mockMvc.perform(
                get("/api/license_service/v2/license")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.licenseNumber").value(licenseNumber))
                .andExpect(jsonPath("$.companyName").value(companyName));
    }

    @Test
    @WithMockCustomUser()
    void getLicense_LicenseNotFound_ThrowException() throws Exception {
        log.info("[showLicenseTest]");
        when(licenseService.findActiveLicense()).thenReturn(Optional.empty());
        mockMvc.perform(
                get("/api/license_service/v2/license")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value("ERROR"))
                .andExpect(jsonPath("$.responseCode").value("404"))
                .andExpect(jsonPath("$.errorCode").value("DATA_NOT_FOUND"));
    }
}
