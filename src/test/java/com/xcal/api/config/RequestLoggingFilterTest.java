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

package com.xcal.api.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;

public class RequestLoggingFilterTest {
    @Test
    public void testRequestLoggingFilter() throws IOException, ServletException {
        RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter();
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(httpServletRequest) {
            @Override
            public String getHeader(String name) {
                return "curl/7.64.0";
            }
        };
        requestLoggingFilter.doFilterInternal(wrapper, httpServletResponse, filterChain);
    }

    @Test
    public void testRequestLoggingFilterSetters() throws IOException, ServletException {
        RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter();
        requestLoggingFilter.setIncludeClientInfo(true);
        requestLoggingFilter.setIncludeHeaders(true);
        requestLoggingFilter.setIncludePayload(true);
        requestLoggingFilter.setIncludeQueryString(true);
        requestLoggingFilter.setMaxPayloadLength(9999);

        Assertions.assertTrue(requestLoggingFilter.isIncludeClientInfo());
        Assertions.assertTrue(requestLoggingFilter.isIncludeHeaders());
        Assertions.assertTrue(requestLoggingFilter.isIncludePayload());
        Assertions.assertTrue(requestLoggingFilter.isIncludeQueryString());
        Assertions.assertTrue(requestLoggingFilter.getMaxPayloadLength() == 9999);
    }
}
