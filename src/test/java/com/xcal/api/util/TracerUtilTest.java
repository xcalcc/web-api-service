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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableMap;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.noop.NoopTracerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.*;

class TracerUtilTest {

    private Logger logger = (Logger) LoggerFactory.getLogger(TracerUtil.class);
    private ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private JaegerTracer tracer;
    private JaegerSpan span;

    @BeforeEach
    void setUp() {
        listAppender.start();
        logger.addAppender(listAppender);
        tracer = Configuration.fromEnv("test")
                .withSampler(Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1))
                .getTracer();
        span = tracer.buildSpan("test").start();
    }

    @Test
    void setTag_NullTracer_LogError() {
        logger.setLevel(Level.ERROR);
        TracerUtil.setTag(null, "key", "value");
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        assertThat(logsList.get(0).getMessage(), containsString("[setTag] tracer or activeSpan is null"));
    }

    @Test
    void setTag_ActiveSpanIsNull_LogError() {
        logger.setLevel(Level.ERROR);
        TracerUtil.setTag(tracer, "key", "value");
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        assertThat(logsList.get(0).getMessage(), containsString("[setTag] tracer or activeSpan is null"));
    }

    @Test
    void setTag_ActiveSpanIsNotNull_SuccessAndLogInfo() {
        tracer.activateSpan(span);
        TracerUtil.setTag(tracer, "key", "value");
        assertTrue(span.getTags().containsKey("key"));
        assertEquals("value", span.getTags().get("key"));
    }
    @Test
    void setTag_SpanNotJaegerSpan_NoTagSet() {
        Span noopSpan = NoopTracerFactory.create().activeSpan();
        tracer.activateSpan(noopSpan);
        TracerUtil.setTag(tracer, "key", "value");
        assertFalse(span.getTags().containsKey("key"));
    }

    @Test
    void setTagsWithObjectMap_NullTracer_LogError() {
        logger.setLevel(Level.ERROR);
        TracerUtil.setTagsWithObjectMap(null, ImmutableMap.of("key", "value"));
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        assertThat(logsList.get(0).getMessage(), containsString("[setTags] tracer or activeSpan is null"));
    }

    @Test
    void setTagsWithObjectMap_ActiveSpanIsNull_LogError() {
        logger.setLevel(Level.ERROR);
        TracerUtil.setTagsWithObjectMap(tracer, ImmutableMap.of("key", "value"));
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        assertThat(logsList.get(0).getMessage(), containsString("[setTags] tracer or activeSpan is null"));
    }

    @Test
    void setTagsWithObjectMap_SpanNotJaegerSpan_NoTagSet() {
        Span noopSpan = NoopTracerFactory.create().activeSpan();
        tracer.activateSpan(noopSpan);
        TracerUtil.setTagsWithObjectMap(tracer, ImmutableMap.of("key", "value"));
        assertFalse(span.getTags().containsKey("key"));
    }

    @Test
    void setTags_nullValue_doNothing() {
        Span noopSpan = NoopTracerFactory.create().activeSpan();
        tracer.activateSpan(noopSpan);
        assertDoesNotThrow(() -> TracerUtil.setTag(tracer, "key", null));
    }
}
