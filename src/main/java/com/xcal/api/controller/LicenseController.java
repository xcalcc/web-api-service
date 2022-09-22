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

import com.xcal.api.entity.License;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.LicenseDto;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.FileService;
import com.xcal.api.service.LicenseService;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.Dto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import java.net.HttpURLConnection;
import java.util.Optional;

/**
 * The user and group relationship controller
 */
@Slf4j
@RestController
@RequestMapping("/api/license_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "License Service")
public class LicenseController {

    @NonNull LicenseService licenseService;
    @NonNull FileService fileService;

    @ApiOperation(value = "Update license",
            nickname = "updateLicense",
            notes = "Update license file, administrator right is needed",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Dto(LicenseDto.class)
    @PostMapping("/license")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<License> updateLicense(@RequestParam("upload_file") MultipartFile file, @RequestParam(value = "file_checksum", required = false) String checksum,
                                                 @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateLicense] license: {}, checksum: {}, principal username: {}", file, checksum, userPrincipal.getUsername());

        if (StringUtils.isNotBlank(checksum)) {
            boolean isFileOk = fileService.checkIntegrityWithCrc32(file, checksum);
            if (!isFileOk) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_INCONSISTENT, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                        CommonUtil.formatString("[{}] expectedChecksum: {}", AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate, checksum));
            }
        }

        License license = licenseService.updateLicense(file, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(license);
    }

    @ApiOperation(value = "Get license",
            nickname = "getLicense",
            notes = "Get license Information",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Dto(LicenseDto.class)
    @GetMapping("/license")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<License> getLicense(@CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getLicense] principal username: {}", userPrincipal.getUsername());
        Optional<License> licenseOptional = licenseService.findActiveLicense();
        License license = licenseOptional.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND,AppException.ErrorCode.E_API_LICENSE_COMMON_NOT_FOUND.unifyErrorCode,
        CommonUtil.formatString("[{}]", AppException.ErrorCode.E_API_LICENSE_COMMON_NOT_FOUND.messageTemplate)));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(license);
    }
}
