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

package com.xcal.api.security;

import com.xcal.api.entity.User;
import com.xcal.api.entity.UserGroup;
import com.xcal.api.exception.AppException;
import com.xcal.api.util.CommonUtil;
import io.jaegertracing.internal.JaegerSpan;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@Slf4j
class TokenAuthenticationFilterTest {
    private TokenProvider tokenProvider;
    private CustomUserDetailsService customUserDetailsService;
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(TokenProvider.class);
        customUserDetailsService = mock(CustomUserDetailsService.class);
        Tracer tracer = mock(Tracer.class);
        when(tracer.activeSpan()).thenReturn(mock(JaegerSpan.class));
        tokenAuthenticationFilter = new TokenAuthenticationFilter(tokenProvider, customUserDetailsService, tracer);
    }

    @Test
    void shouldNotFilter_ApiRequest_ReturnFalse() {
        log.info("[shouldNotFilter_ApiRequest_ReturnFalse]");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test_service/v1/test");
        boolean result = tokenAuthenticationFilter.shouldNotFilter(request);
        Assertions.assertFalse(result);
    }

    @Test
    void shouldNotFilter_PassRequest_ReturnTrue() {
        log.info("[shouldNotFilter_PassRequest_ReturnTrue]");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/actuator/prometheus");
        boolean result = tokenAuthenticationFilter.shouldNotFilter(request);
        Assertions.assertTrue(result);
    }

    @Test
    void doFilterInternal_HeaderHaveBearTokenValidateTokenTrue_DoNothing() throws AppException, ServletException, IOException {
        when(tokenProvider.getUserIdFromToken(anyString())).thenReturn("anyUserId");
        when(tokenProvider.validateToken(anyString())).thenReturn(true);
        when(customUserDetailsService.loadUserById(anyString())).thenReturn(
                UserPrincipal.create(
                        User.builder()
                                .userGroups(Arrays.asList(
                                        UserGroup.builder().groupName("admin").groupType(UserGroup.Type.ROLE).build(),
                                        UserGroup.builder().groupName("userDefine").groupType(UserGroup.Type.USER).build()
                                )).build()));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test_service/v1/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer abcTesting");
        when(request.getParameter("token")).thenReturn("");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        doNothing().when(filterChain).doFilter(any(HttpServletRequest.class),any(HttpServletResponse.class));
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);
    }

    @Test
    void doFilterInternal_HeaderHaveBearTokenValidateTokenFalse_DoNothing() throws AppException, ServletException, IOException {
        when(tokenProvider.getUserIdFromToken(anyString())).thenReturn("anyUserId");
        when(tokenProvider.validateToken(anyString())).thenReturn(false);
        when(customUserDetailsService.loadUserById(anyString())).thenReturn(
                UserPrincipal.create(
                        User.builder()
                                .userGroups(Arrays.asList(
                                        UserGroup.builder().groupName("admin").groupType(UserGroup.Type.ROLE).build(),
                                        UserGroup.builder().groupName("userDefine").groupType(UserGroup.Type.USER).build()
                                )).build()));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test_service/v1/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer abcTesting");
        when(request.getParameter("token")).thenReturn("");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        doNothing().when(filterChain).doFilter(any(HttpServletRequest.class),any(HttpServletResponse.class));
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);
    }

    @Test
    void doFilterInternal_InvalidHeaderHaveParamTokenValidateTokenTrue_DoNothing() throws AppException, ServletException, IOException {
        when(tokenProvider.getUserIdFromToken(anyString())).thenReturn("anyUserId");
        when(tokenProvider.validateToken(anyString())).thenReturn(true);
        when(customUserDetailsService.loadUserById(anyString())).thenReturn(
                UserPrincipal.create(
                        User.builder()
                                .userGroups(Arrays.asList(
                                        UserGroup.builder().groupName("admin").groupType(UserGroup.Type.ROLE).build(),
                                        UserGroup.builder().groupName("userDefine").groupType(UserGroup.Type.USER).build()
                                )).build()));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test_service/v1/test");
        when(request.getHeader("Authorization")).thenReturn("incorrectBearer abcTesting");
        when(request.getParameter("token")).thenReturn("testingToken");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        doNothing().when(filterChain).doFilter(any(HttpServletRequest.class),any(HttpServletResponse.class));
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);
    }

    @Test
    void doFilterInternal_UserNotFound_Exception() throws AppException, ServletException, IOException {
        when(tokenProvider.getUserIdFromToken(anyString())).thenReturn("anyUserId");
        when(tokenProvider.validateToken(anyString())).thenReturn(true);
        when(customUserDetailsService.loadUserById("anyUserId")).thenThrow(
                new UsernameNotFoundException(CommonUtil.formatString("[User not found]. username: {}" ,"anyUserId")));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test_service/v1/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer abcTesting");
        when(request.getParameter("token")).thenReturn("");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        doNothing().when(filterChain).doFilter(any(HttpServletRequest.class),any(HttpServletResponse.class));
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);
    }
}
