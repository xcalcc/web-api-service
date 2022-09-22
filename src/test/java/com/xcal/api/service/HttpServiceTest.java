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

import lombok.RequiredArgsConstructor;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.MOCK)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
class HttpServiceTest {

    @MockBean
    private OkHttpClient okHttpClient;


    private HttpService httpService;


    @Mock ResponseBody responseBody;

    @MockBean
    private Response response;

    @MockBean
    private Call mockCall;

    @BeforeEach
    void setUp() {
        httpService = new HttpService(okHttpClient);
    }

    @Test
    void httpGet_normal_noExceptionThrown() throws IOException {

        String url = "http://test.com";

        doReturn(mockCall).when(okHttpClient).newCall(any());
        doReturn(response).when(mockCall).execute();
        doReturn(responseBody).when(response).body();
        doNothing().when(response).close();
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("a","a");
        paramMap.put("b","b");
        assertDoesNotThrow(()->httpService.httpGet(url,paramMap));
    }

    @Test
    void httpGet_IOExceptionOnExecute_returnNull() throws IOException {

        String url = "http://test.com";

        doReturn(mockCall).when(okHttpClient).newCall(any());
        doThrow(IOException.class).when(mockCall).execute();
        doReturn(responseBody).when(response).body();
        doNothing().when(response).close();
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("a","a");
        paramMap.put("b","b");
        assertDoesNotThrow(()->httpService.httpGet(url,paramMap));
    }

}
