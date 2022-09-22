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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

/**
 * http request service
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HttpService {

    @NonNull
    OkHttpClient okHttpClient;
    /**
     * a http call and return string result
     * @param url url to be request
     * @param parameter request parameter in string string map
     * @return response
     */
    public String httpGet(String url, Map<String, String> parameter) {
        log.trace("[httpCall] url: {}, parameter: {}", url, parameter);
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();

        if (parameter != null) {
            for(Map.Entry<String, String> entry: parameter.entrySet()){
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        HttpUrl httpUrl = builder.build();
        log.info(httpUrl.encodedQuery());
        Request request = new Request.Builder().url(httpUrl).build();

        String result = null;
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null){
                result = responseBody.string();
            }
            response.close();
        } catch (IOException e) {
            log.error("[httpCall] exception: {}", e.getMessage());
        }

        return result;
    }

}
