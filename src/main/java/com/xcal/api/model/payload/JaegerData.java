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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jaeger dto to mapping Jaeger log
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JaegerData {
    int total;
    int limit;
    int offset;
    Object errors;

    @Builder.Default
    List<Data> data = new ArrayList<>();

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        String traceID;
        @Builder.Default
        List<Span> spans = new ArrayList<>();
        @Builder.Default
        Map<String,Process> processes = new HashMap<>();
        String warnings;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Span {
        String traceID;
        String spanID;
        int flags;
        String operationName;
        @Builder.Default
        List<Reference> references = new ArrayList<>();
        long startTime;
        int duration;
        @Builder.Default
        List<Tag> tags = new ArrayList<>();
        @Builder.Default
        List<Log> logs = new ArrayList<>();
        String processID;
        String warnings;

    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Tag {
        String key;
        String type;
        String value;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Log {
        long timestamp;
        @Builder.Default
        List<Tag> fields = new ArrayList<>();
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Process {
        String serviceName;
        @Builder.Default
        List<Tag> tags = new ArrayList<>();
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Reference {
        String refType;
        String traceID;
        String spanID;
    }

}
