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

import com.xcal.api.util.TracerUtil;
import io.opentracing.Tracer;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @NonNull
    private TokenProvider tokenProvider;

    @NonNull
    private CustomUserDetailsService customUserDetailsService;

    @NonNull
    private Tracer tracer;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String regex = "/|/actuator/prometheus|/swagger.*|/webjars/.*|/favicon.ico|/v2/api-docs";
        return Pattern.matches(regex, request.getRequestURI());
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.trace("[TokenAuthenticationFilter:doFilterInternal] request uri: {}", request.getRequestURI());
        try {
            String token = tokenProvider.getTokenFromRequest(request);
            log.trace("[TokenAuthenticationFilter:doFilterInternal] token: {}", token);
            if (StringUtils.isNotBlank(token) && tokenProvider.validateToken(token)) {
                String userId = tokenProvider.getUserIdFromToken(token);

                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.trace("[TokenAuthenticationFilter:doFilterInternal] userDetails: {}, authentication: {}", userDetails, authentication);
                TracerUtil.setTag(tracer, TracerUtil.Tag.USERNAME, userDetails.getUsername());
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }


}
