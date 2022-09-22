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
import com.xcal.api.entity.User;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.LoginRequest;
import com.xcal.api.security.TokenProvider;
import com.xcal.api.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class AuthControllerTest {

    @NonNull MockMvc mockMvc;

    @NonNull ObjectMapper om;

    @MockBean
    AuthenticationManager authenticationManager;

    @MockBean
    TokenProvider tokenProvider;

    @MockBean
    UserService userService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void login_InvalidUserName_ThrowException() throws Exception {
        log.info("[login_withInValidUserName_ThrowException]");
        LoginRequest loginRequest = LoginRequest.builder().username("testUserName").password("xxx").build();
        when(userService.findByUsernameOrEmail(loginRequest.getUsername())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/auth_service/v2/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(loginRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_UNAUTHORIZED))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    void login_InvalidEmail_ThrowException() throws Exception {
        log.info("[login_InvalidEmail_ThrowException]");
        LoginRequest loginRequest = LoginRequest.builder().email("test@xxx.com").password("xxx").build();
        when(userService.findByUsernameOrEmail(loginRequest.getEmail())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/auth_service/v2/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(loginRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_UNAUTHORIZED))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    void login_UserIsLocked_ThrowException() throws Exception {
        log.info("[login_UserIsLocked_ThrowException]");
        LoginRequest loginRequest = LoginRequest.builder().username("testUserName").password("xxx").build();
        when(userService.findByUsernameOrEmail(loginRequest.getUsername())).thenReturn(Optional.of(User.builder().status(User.Status.LOCK).build()));
        mockMvc.perform(post("/api/auth_service/v2/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(loginRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_UNAUTHORIZED))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    void login_UserIsSuspended_ThrowException() throws Exception {
        log.info("[login_UserIsSuspended_ThrowException]");
        LoginRequest loginRequest = LoginRequest.builder().username("testUserName").password("xxx").build();
        when(userService.findByUsernameOrEmail(loginRequest.getUsername())).thenReturn(Optional.of(User.builder().status(User.Status.SUSPEND).build()));
        mockMvc.perform(post("/api/auth_service/v2/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(loginRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_UNAUTHORIZED))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    void login_validUserPassword_Success() throws Exception {
        log.info("[login_validUserPassword_Success]");
        LoginRequest loginRequest = LoginRequest.builder().username("testUserName").password("xxx").build();
        when(userService.findByUsernameOrEmail(loginRequest.getUsername())).thenReturn(Optional.of(User.builder().status(User.Status.ACTIVE).build()));
        doNothing().when(userService).deleteLoginFailLogs(any(User.class));
        mockMvc.perform(post("/api/auth_service/v2/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(loginRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @WithMockCustomUser()
    void logout_TokenIsNull_ThrowException() throws Exception {
        log.info("[logout_TokenIsNull_ThrowException]");
        mockMvc.perform(post("/api/auth_service/v2/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_UNAUTHORIZED))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INVALID_TOKEN));
    }

    @Test
    @WithMockCustomUser()
    void logout_tokenIsNotNull_Success() throws Exception {
        log.info("[logout_tokenIsNotNull_Success]");
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlZDRkZDczMS01YjRhLTQ1N2QtYjgyOC0xN2Q3YTY3NDE0MWEiLCJpYXQiOjE1NzQyMjMxMDksImV4cCI6MTU3NDMwOTUwOX0.HCLrTqLZVFhpxrOcBitD9q1lEjzhYndfLbVGhWqSD0jZFBVZ_0fKFD-bbh85X8ctOePOlGi1AbQK6e1AJ9B43g";
        when(tokenProvider.getTokenFromRequest(any())).thenReturn(token);
        doNothing().when(tokenProvider).invalidateToken(any(), any());
        mockMvc.perform(post("/api/auth_service/v2/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser()
    void tokenStatus_Valid_Success() throws Exception {
        log.info("[tokenStatus_Valid_Success]");
        when(tokenProvider.getTokenFromRequest(any())).thenReturn("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlZDRkZDczMS01YjRhLTQ1N2QtYjgyOC0xN2Q3YTY3NDE0MWEiLCJpYXQiOjE1NzQyMjMxMDksImV4cCI6MTU3NDMwOTUwOX0.HCLrTqLZVFhpxrOcBitD9q1lEjzhYndfLbVGhWqSD0jZFBVZ_0fKFD-bbh85X8ctOePOlGi1AbQK6e1AJ9B43g");
        when(tokenProvider.validateToken(any())).thenReturn(true);
        mockMvc.perform(get("/api/auth_service/v2/token_status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenStatus").value("VALID_TOKEN"));
    }

    @Test
    @WithMockCustomUser()
    void tokenStatus_TokenIsNull_ThrowException() throws Exception {
        log.info("[tokenStatus_TokenIsNull_ThrowException]");
        when(tokenProvider.validateToken(any())).thenReturn(true);
        mockMvc.perform(get("/api/auth_service/v2/token_status"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_UNAUTHORIZED))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INVALID_TOKEN));
    }


    @Test
    @WithMockCustomUser()
    void tokenStatus_Invalid() throws Exception {
        log.info("[tokenStatus_Invalid]");
        when(tokenProvider.validateToken(any())).thenReturn(false);
        mockMvc.perform(get("/api/auth_service/v2/token_status"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_UNAUTHORIZED))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INVALID_TOKEN));
    }

}
