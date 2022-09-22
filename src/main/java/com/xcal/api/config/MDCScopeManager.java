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

import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.ThreadLocalScopeManager;
import org.slf4j.MDC;

import java.lang.management.ManagementFactory;

public class MDCScopeManager extends ThreadLocalScopeManager {

    @Override
    public Scope activate(Span span, boolean finishOnClose) {
        return new ScopeWrapper(super.activate(span, finishOnClose));
    }

    @Override
    public Scope activate(Span span) {
        return new ScopeWrapper(super.activate(span));
    }

    private static class ScopeWrapper implements Scope {

        private final Scope scope;
        private final String previousTraceId;
        private final String previousSpanId;
        private final String previousParentSpanId;
        private final String previousSampled;

        ScopeWrapper(Scope scope) {
            this.scope = scope;
            this.previousTraceId = lookup("traceId");
            this.previousSpanId = lookup("spanId");
            this.previousParentSpanId = lookup("parentSpanId");
            this.previousSampled = lookup("traceSampled");

            JaegerSpanContext ctx = (JaegerSpanContext) scope.span().context();
            String traceId = ctx.getTraceId();
            String spanId = Long.toHexString(ctx.getSpanId());
            String sampled = String.valueOf(ctx.isSampled());
            String parentSpanId = Long.toHexString(ctx.getParentId());

            replace("traceId", traceId);
            replace("spanId", spanId);
            replace("parentSpanId", parentSpanId);
            replace("traceSampled", sampled);
        }

        @Override
        public void close() {
            this.scope.close();
            replace("traceId", previousTraceId);
            replace("spanId", previousSpanId);
            replace("parentSpanId", previousParentSpanId);
            replace("traceSampled", previousSampled);
            replace("applicationName", "web-api-service-main");
            replace("processId", ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", ""));
        }

        @Override
        public Span span() {
            return this.scope.span();
        }
    }

    private static String lookup(String key) {
        return MDC.get(key);
    }

    private static void replace(String key, String value) {
        if (value == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, value);
        }
    }
}
