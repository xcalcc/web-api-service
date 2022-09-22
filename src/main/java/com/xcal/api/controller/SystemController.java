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

import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.ContactUsRequest;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.EmailService;
import com.xcal.api.service.IssueService;
import com.xcal.api.service.ScanTaskService;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.ValidErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/system_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "System Service")
public class SystemController {

    @Value("${app.scan.service.url}")
    public String scanServiceUrl;

    @NonNull EmailService emailService;
    @NonNull ScanTaskService scanTaskService;
    @NonNull IssueService issueService;
    @NonNull OkHttpClient okHttpClient;

    @GetMapping("/public/ping")
    @ApiOperation(value = "Ping the application",
            nickname = "ping",
            notes = "Ping the application, response pong to show alive")
    public String ping() {
        log.info("[ping] someone ping me.");
        return "pong";
    }

    @GetMapping("/public/ping/scan_task_service")
    @ApiOperation(value = "Ping python web services",
            nickname = "pingScanService",
            notes = "Ping scan services, response web services version")
    @ResponseBody
    public String pingScanService() throws AppException {
        String scanServiceLink = CommonUtil.formatString("http://{}/api/scan_task_service/v2", scanServiceUrl);
        String result = null;
        Request request = new Request.Builder().url(scanServiceLink).get().build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.body() != null) {
                result = response.body().toString();
            }
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND,
                    HttpStatus.SC_NOT_FOUND, AppException.ErrorCode.E_API_SYSTEM_PING_NOT_AVAILABLE.unifyErrorCode, CommonUtil.formatString("[{}] {}", AppException.ErrorCode.E_API_SYSTEM_PING_NOT_AVAILABLE.messageTemplate,scanServiceLink),e);
        }
        return result;
    }

    @PostMapping("/contact_us")
    @ApiOperation(value = "ContactUs",
            nickname = "contactUs",
            notes = "Send message to contact and feedback")
    public ResponseEntity<Void> contactUs(@Valid @ValidErrorCode(AppException.ErrorCode.E_API_CONTACTUS_VALIDATE_FAIL) @RequestBody ContactUsRequest contactUsRequest, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[contactUs] contactUsRequest: {}", contactUsRequest);
        emailService.contactUs(contactUsRequest, userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
