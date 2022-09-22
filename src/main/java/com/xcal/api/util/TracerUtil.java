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

package com.xcal.api.util;

import com.xcal.api.entity.ScanTask;
import io.jaegertracing.internal.JaegerSpan;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.StringTag;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class TracerUtil {

    private TracerUtil(){}
    public enum Tag{
        PROJECT_ID("project_id"),
        SCAN_TASK_ID("scan_task_id"),
        SCAN_TASK_STATUS("scan_task_status"),
        SCAN_END_AT ("scan_end_at"),
        FILE_ID("file_id"),
        USERNAME("username"),
        ERROR("error");

        public final String value;
        Tag(String value){
            this.value = value;
        }
    }
    public static void setScanTaskTag(Tracer tracer, ScanTask scanTask) {
        log.debug("[setScanTaskTag] scanTask, id: {}", scanTask.getId());
        setTag(tracer, Tag.SCAN_TASK_ID, scanTask.getId());
        setTag(tracer, Tag.PROJECT_ID, scanTask.getProject().getId());
    }
    public static void setTag(Tracer tracer, Tag tag, Object value) {
        log.trace("[setTag] tag: {}", tag);
        setTag(tracer, tag.value, value);
    }
    public static void setTags(Tracer tracer, Map<Tag, Object> tags) {
        log.trace("[setTag] tags: {}", tags.keySet());
        Map<String, Object> newTags = new HashMap<>();
        for(Map.Entry<Tag, Object> entry : tags.entrySet()){
            newTags.put(entry.getKey().value, entry.getValue());
        }
        setTagsWithObjectMap(tracer, newTags);
    }

    public static void setTag(Tracer tracer, String key, Object value) {
        if (tracer == null || tracer.activeSpan() == null) {
            log.error("[setTag] tracer or activeSpan is null, tag key: {}, tag value: {}", key, value);
        } else {
            Span span = tracer.activeSpan();
            if(span instanceof JaegerSpan){
                JaegerSpan jaegerSpan = (JaegerSpan)span;
                setTag(jaegerSpan, key, value);
            }
        }
    }

    public static void setTagsWithObjectMap(Tracer tracer, Map<String, Object> tags) {
        if (tracer == null || tracer.activeSpan() == null) {
            log.error("[setTags] tracer or activeSpan is null, tag keys: {}", tags.keySet());
        } else {
            Span span = tracer.activeSpan();
            if(span instanceof JaegerSpan){
                JaegerSpan jaegerSpan = (JaegerSpan)span;
                for(Map.Entry<String, Object> entry: tags.entrySet()){
                    setTag(jaegerSpan, entry.getKey(),entry.getValue());
                }
            }
            log.trace("[setTag] span id: {}, trace id: {}", span.context().toSpanId(), span.context().toTraceId());
        }
    }

    private static void setTag(JaegerSpan jaegerSpan, String key, Object value) {
        if(value != null){
            log.trace("[setTag] span id: {}, trace id: {}, key: {}, value: {}", jaegerSpan.context().toSpanId(), jaegerSpan.context().toTraceId(), key, value);
            jaegerSpan.setTag(new StringTag(key), String.valueOf(value));
        }
    }
}
