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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * component to read properties from config file
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    public enum FileDeleteOption {
        NONE, SCAN_FILE, ALL
    }

    public static final String ENGLISH_LANGUAGE = "en";
    public static final String CHINESE_LANGUAGE = "zh-CN";
    public static final String EMAIL_SUBJECT_PATTERN_NEW_USER = "Welcome to Xcalscan {}";
    public static final String EMAIL_SUBJECT_PATTERN_NEW_USER_ZH_CN = "欢迎来到 Xcalscan {}";
    public static final String EMAIL_TEMPLATE_PREFIX_NEW_USER = "new-user-email.html";
    public static final String EMAIL_TEMPLATE_PREFIX_NEW_USER_ZH_CN = "new-user-email-zh-CN.html";
    public static final String EMAIL_SUBJECT_PATTERN_ASSIGN_ISSUE = "{} {} has assigned a defect on project {} to you";
    public static final String EMAIL_SUBJECT_PATTERN_ASSIGN_ISSUE_ZH_CN = "{} {}指派了项目{}中的一项缺陷给你";
    public static final String EMAIL_TEMPLATE_PREFIX_ASSIGN_ISSUE = "assign-issue-email.html";
    public static final String EMAIL_TEMPLATE_PREFIX_ASSIGN_ISSUE_ZH_CN = "assign-issue-email-zh-CN.html";

    private FileDeleteOption fileDeleteOption = FileDeleteOption.ALL;
    private String uiProtocol;
    private String uiHost;
    private int uiPort;
    private final Auth auth = new Auth();
    private final Mail mail = new Mail();
    private final Performance performance = new Performance();
    private final Jaeger jaeger = new Jaeger();

    @Data
    public static class Auth {
        private String tokenSecret;
        private long tokenExpirationMsec;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class Mail {
        private String protocol;
        private String host;
        private int port;
        private String username;
        private String password;
        private String from;
        private String starttls;
        private String prefix;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class Performance {
        private String dataSavePath;
        private final Prometheus prometheus = new Prometheus();

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static final class Prometheus {
            private String queryUrl;
            private final Query query = new Query();

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static final class Query {
                String scanServiceCpu;
                String scanServiceMem;
                String webMainApiCpu;
                String webMainApiMem;
                String databaseCpu;
                String databaseMem;
                String xvsaCpu;
                String xvsaMem;
                String kafkaCpu;
                String kafkaMem;
                String jaegerCpu;
                String jaegerMem;
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class Jaeger {
        private String queryHost;
        private String queryPort;

    }

}
