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

package com.xcal.api.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * prometheus data model for mapping prometheus data
 */
@Data
public class PrometheusData {

    String status;
    Data data = new Data();

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        String resultType;
        @Builder.Default
        List<DataResult> result = new ArrayList<>();

    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataResult {
        Map metric;
        @Builder.Default
        List values = new ArrayList();
    }

}
