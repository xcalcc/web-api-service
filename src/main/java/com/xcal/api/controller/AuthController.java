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

import com.xcal.api.entity.Setting;
import com.xcal.api.entity.User;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.SettingDto;
import com.xcal.api.model.payload.*;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.TokenProvider;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.SettingService;
import com.xcal.api.service.UserService;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.Dto;
import com.xcal.api.util.ValidErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Authentication Service")
public class AuthController {

    @NonNull AuthenticationManager authenticationManager;

    @NonNull TokenProvider tokenProvider;

    @NonNull UserService userService;

    @NonNull SettingService settingService;

    static final long ONE_DAY_MILLISECOND = 86400000L;
    static final String ACCESS_TOKEN_PREFIX = "accessToken.";

    /**
     * @param loginRequest the login request
     * @return ResponseEntity<AuthResponse>
     */
    @ApiOperation(value = "Login",
            nickname = "login",
            notes = "Login with username/email and password",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @ValidErrorCode(AppException.ErrorCode.E_API_USER_LOGIN_VALIDATE_FAIL) @RequestBody LoginRequest loginRequest) throws AppException {
        log.info("[login] loginRequest, username: {}, email: {}", loginRequest.getUsername(), loginRequest.getEmail());
        String loginName = loginRequest.getUsername();
        if (StringUtils.isBlank(loginName)) {
            loginName = loginRequest.getEmail();
        }
        User user = this.userService.findByUsernameOrEmail(loginName).orElseThrow(
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_UNAUTHORIZED, AppException.ErrorCode.E_API_USER_AUTH_USERNAME_PASSWORD_NOT_CORRECT.unifyErrorCode, AppException.ErrorCode.E_API_USER_AUTH_USERNAME_PASSWORD_NOT_CORRECT.messageTemplate));
        switch (user.getStatus()) {
            case LOCK:
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_UNAUTHORIZED, AppException.ErrorCode.E_API_USER_COMMON_LOCKED.unifyErrorCode, AppException.ErrorCode.E_API_USER_COMMON_LOCKED.messageTemplate);
            case SUSPEND:
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_UNAUTHORIZED, AppException.ErrorCode.E_API_USER_COMMON_SUSPENDED.unifyErrorCode, AppException.ErrorCode.E_API_USER_COMMON_SUSPENDED.messageTemplate);
            default:
                break;
        }
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginName,
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            Integer failCount = this.userService.handleLoginFail(loginName);
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_UNAUTHORIZED, AppException.ErrorCode.E_API_USER_AUTH_USERNAME_PASSWORD_NOT_CORRECT.unifyErrorCode,
                    CommonUtil.formatString("[{}]: {}", AppException.ErrorCode.E_API_USER_AUTH_USERNAME_PASSWORD_NOT_CORRECT.messageTemplate, failCount),e);
        }
        this.userService.deleteLoginFailLogs(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.createToken(authentication);

        log.info("[login] Login Success. Username: {}", loginName);
        return ResponseEntity.ok(AuthResponse.builder().accessToken(token).build());
    }

    /**
     * @param generateTokenRequest generate token request
     * @return ResponseEntity<AuthResponse>
     */
    @ApiOperation(value = "generateAccessToken",
            nickname = "generateAccessToken",
            notes = "return new generated access token",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/generate_access_token")
    public ResponseEntity<AuthResponse> generateAccessToken(@RequestBody GenerateTokenRequest generateTokenRequest, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[generateAccessToken] generateTokenRequest: {}, user: {}", generateTokenRequest, userPrincipal != null ? userPrincipal.getUsername(): null);
        if(settingService.isKeyExist(ACCESS_TOKEN_PREFIX + generateTokenRequest.getTokenName())) {
            log.error("[generateAccessToken] token name already exist: {}", generateTokenRequest.getTokenName());
            // TODO: need to add new error code here
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.unifyErrorCode,
                    CommonUtil.formatString("[{}] token name already exist: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.messageTemplate, generateTokenRequest.getTokenName()));
        }

        if ((generateTokenRequest.getExpireDays() < 1) || (generateTokenRequest.getExpireDays() > Integer.MAX_VALUE)) {
            log.error("[generateAccessToken] the value of expire days should between 1 and the max value of int");
            // TODO: need to add new error code here
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.unifyErrorCode,
                    CommonUtil.formatString("[{}] invalid expireDays value: {}, valid value range: [1, 0x7fffffff]", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.messageTemplate, generateTokenRequest.getExpireDays()));
        }

        String token = tokenProvider.createToken(userPrincipal.getId().toString(), generateTokenRequest.getExpireDays()*ONE_DAY_MILLISECOND);
        settingService.add(ACCESS_TOKEN_PREFIX + generateTokenRequest.getTokenName(), token, userPrincipal.getUsername());
        return ResponseEntity.ok(AuthResponse.builder().accessToken(token).build());
    }

    @ApiOperation(value = "listAccessTokens",
            nickname = "listAccessToken",
            notes = "return access tokens",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/access_tokens")
    public ResponseEntity<List<TokenResponse>> listAccessTokens(@CurrentUser UserPrincipal userPrincipal) {
        log.info("[listAccessTokens] user: {}", userPrincipal != null ? userPrincipal.getUsername(): null);
        List<Setting> settings = this.settingService.findByKeyPrefix(ACCESS_TOKEN_PREFIX);
        List<TokenResponse> tokenResponseList = new ArrayList<>();
        for(Setting setting: settings) {
            TokenResponse tokenResponse = TokenResponse.builder().id(setting.getId())
                    .tokenName(StringUtils.removeStart(setting.getSettingKey(), ACCESS_TOKEN_PREFIX))
                    .token(setting.getSettingValue())
                    .expireDate(tokenProvider.getExpirationDateFromTokenIgnoreExpiredException(setting.getSettingValue()))
                    .createdBy(setting.getModifiedBy())
                    .createdOn(setting.getModifiedOn()).build();
            tokenResponseList.add(tokenResponse);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(tokenResponseList);
    }

    @ApiOperation(value = "deleteAccessTokenById",
            nickname = "deleteAccessTokenById",
            notes = "delete access token by id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping("/access_token/{id}")
    @Dto(SettingDto.class)
    public ResponseEntity<Page<Setting>> deleteAccessToken(@ApiParam(value = "uuid of access token", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a") @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[deleteAccessToken] id: {}, user: {}", id, userPrincipal != null ? userPrincipal.getUsername(): null);
        Setting setting = settingService.findById(id).orElseThrow (
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.messageTemplate, id)));
        boolean tokenStatus = tokenProvider.validateToken(setting.getSettingValue());
        log.info("[deleteAccessToken] : {}", tokenStatus);
        if (tokenStatus) {
            tokenProvider.invalidateToken(setting.getSettingValue(), CommonUtil.formatString("Delete access token"));
        }
        settingService.deleteSetting(setting);
        return ResponseEntity.noContent().build();
    }

    /**
     * logout and add token to blacklist
     *
     * @param request The http request
     * @return ResponseEntity
     */
    @ApiOperation(value = "Logout",
            nickname = "logout",
            notes = "user logout and add token to blacklist",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[logout] user: {}", userPrincipal != null ? userPrincipal.getUsername(): null);
        String token = tokenProvider.getTokenFromRequest(request);

        if(StringUtils.isBlank(token)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INVALID_TOKEN, HttpURLConnection.HTTP_UNAUTHORIZED, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.messageTemplate);
        }

        tokenProvider.invalidateToken(token, CommonUtil.formatString("User logout. user name: {}", userPrincipal.getUsername()));
        log.info("[logout] Logout Success. Username: {}", userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * @param request the http request
     * @return result the validation result
     */
    @ApiOperation(value = "Token Status",
            nickname = "tokenStatus",
            notes = "Get the status of a token",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/token_status")
    public ResponseEntity<TokenStatusResponse> tokenStatus(HttpServletRequest request) throws AppException {
        log.info("[tokenStatus] request: {}", request);
        String token = tokenProvider.getTokenFromRequest(request);
        if(StringUtils.isBlank(token)) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INVALID_TOKEN, HttpURLConnection.HTTP_UNAUTHORIZED, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.messageTemplate);
        }
        boolean tokenStatus = tokenProvider.validateToken(token);
        log.info("[tokenStatus] : {}", tokenStatus);
        if (tokenStatus)
            return ResponseEntity.ok(TokenStatusResponse.builder().tokenStatus("VALID_TOKEN").build());
        else
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INVALID_TOKEN, HttpURLConnection.HTTP_UNAUTHORIZED, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.messageTemplate);
    }

}
