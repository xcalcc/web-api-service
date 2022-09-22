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

public final class VariableUtil {

    private VariableUtil() {
    }

    public static final String AGENT_FILE_STORAGE_NAME = "agent";
    public static final String SOURCE_STORAGE_NAME = "sourceStorageName";
    public static final String SCAN_TYPE = "scanType";
    public static final String UPLOAD_FILE_INFO_ID = "uploadFileInfoId";
    public static final String RELATIVE_SOURCE_PATH = "relativeSourcePath";
    public static final String GITLAB_V3 = "GITLAB_V3";
    public static final String VCS_TOKEN = "token";
    public static final String BRANCH = "branch";
    public static final String RELATIVE_BUILD_PATH = "relativeBuildPath";
    public static final String UPLOAD_SOURCE = "uploadSource";
    public static final String JOB_QUEUE_NAME = "jobQueueName";
    public static final String PUBLIC_QUEUE_PREFIX = "public_";
    //Valid characters for Kafka topics are the ASCII alphanumerics, '.', '_', and '-'
    //Here add 3 to 50 characters length limit
    public static final String VALID_JOB_QUEUE_NAME_PATTERN = "^[A-Za-z0-9._-]{3,50}$";
    public static final String BLACKLIST_LOGIN_TOKEN = "blacklist_login_token::{}";
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";
    public static final String USER_NAME_PATTERN = "^[A-Za-z0-9._]{3,15}$";

    public static final String JSON_DEFAULT_SUFFIX = "v";
    public static final String JSON_ABBR_SUFFIX = "vj";
    public static final String JSON_STANDAND_SUFFIX = "json";
    public static final String CBOR_ABBR_SUFFIX = "vc";
    public static final String CBOR_STANDAND_SUFFIX = "cbor";
    public static final String ZIP_STANDAND_SUFFIX = "zip";
    public static final String GZIP_STANDAND_SUFFIX = "tar.gz";
    public static final String GZIP_ABBR_SUFFIX = "tgz";
    public static final String I18N_MESSAGE_KEY_PREFIX_SCAN_TASK_STAGE = "m.scan.scanTaskStatusLog.stage";

    public static final String OFFLINE_AGENT = "offline_agent";
    public static final String VOLUME_UPLOAD = "volume_upload";
    public static final String VOLUME_TMP = "volume_tmp";
    public static final String VOLUME_LIB = "volume_lib";
    public static final String SOURCE_STORAGE_VALUE = "volume_src";
    public static final String VOLUME_DIAGNOSTIC = "volume_diagnostic";
    public static final String FILE_INFO_NAME = "fileinfo.json";
    public static final String SOURCE_CODE_PATH = "code";
    public static final String SOURCE_CODE_ARCHIVE = "source_code.zip";

    public enum IssueAttributeName {
        RULE_CODE,
        SEVERITY,
        VULNERABLE,
        LIKELIHOOD,
        REMEDIATION_COST,
        CERTAINTY,
        PRIORITY,
        CATEGORY,
        ERROR_CODE,
        NO_OF_TRACE_SET,
        COMPLEXITY,
        COMPLEXITY_MAX,
        COMPLEXITY_MIN,
        COMPLEXITY_RATE
    }

    // To reduce use projectConfigAttribute name string directly. Access with ProjectConfigAttribute.Attribute.RELATIVE_SOURCE_PATH.getValue()
    public enum ProjectConfigAttributeTypeName {
        RELATIVE_SOURCE_PATH(Type.PROJECT, "relativeSourcePath"),
        RELATIVE_BUILD_PATH(Type.PROJECT, "relativeBuildPath"),
        SOURCE_STORAGE_NAME(Type.PROJECT, "sourceStorageName"),
        SCAN_TYPE(Type.PROJECT, "scanType"),
        UPLOAD_FILE_INFO_ID(Type.PROJECT, "uploadFileInfoId"),
        GIT_URL(Type.PROJECT, "gitUrl"),
        UPLOAD_SOURCE(Type.PROJECT, "uploadSource"),
        USERNAME(Type.PROJECT, "username"),
        VCS_TOKEN(Type.PROJECT, "token"),
        BRANCH(Type.PROJECT, "branch"),
        LANGUAGE(Type.SCAN, "lang"),
        BUILD_COMMAND(Type.SCAN, "buildCommand"),
        CONFIG_COMMAND(Type.SCAN, "configureCommand"),
        EXTRA_BUILD_OPTIONS(Type.SCAN, "extraBuildOpts"),
        JOB_QUEUE_NAME(Type.SCAN, "jobQueueName"),
        BASELINE_BRANCH(Type.SCAN, "baselineBranch"),
        BASELINE_COMMIT_ID(Type.SCAN, "baselineCommitId"),
        REF(Type.SCAN, "ref"),
        COMMIT_ID(Type.SCAN, "commitId"),
        GERRIT_PROJECT_ID(Type.PROJECT, "gerritProjectId"),
        SCAN_MODE(Type.SCAN, "scanMode"),
        FILE_BLACK_LIST (Type.SCAN, "fileBlacklist"),
        RULE_WHITE_LIST (Type.SCAN, "ruleWhitelist"),
        REPO_ACTION (Type.SCAN, "repoAction"),
        NEXT_STATE_ON_SUCCESS (Type.SCAN, "nextStateOnSuccess"),
        CONFIG_ID (Type.SCAN, "configId");
        public final Type type;
        public final String nameValue;

        ProjectConfigAttributeTypeName(Type type, String nameValue) {
            this.type = type;
            this.nameValue = nameValue;
        }

        public enum Type {
            PROJECT, SCAN
        }
    }

    public enum RepoAction {
        CI, CD, TRIAL
    }

    // To reduce use name string directly. Access with RuleInformationAttribute.Attribute.OWASP.getValue()
    public enum RuleAttributeTypeName {
        LANGUAGE(Type.BASIC, "LANGUAGE"),
        SEVERITY(Type.BASIC, "SEVERITY"),
        PRIORITY(Type.BASIC, "PRIORITY"),
        REMEDIATION_COST(Type.BASIC, "REMEDIATION_COST"),
        CUSTOM(Type.BASIC, "CUSTOM"),
        OWASP(Type.STANDARD, "OWASP");

        public final Type type;
        public final String nameValue;

        RuleAttributeTypeName(Type type, String nameValue) {
            this.type = type;
            this.nameValue = nameValue;
        }

        public enum Type {
            BASIC, STANDARD
        }
    }

    public enum ScanMode {
        SINGLE("0", "-single"),
        CROSS("1", "-cross"),
        SINGLE_XSCA("2", "-single-xsca"),
        XSCA("3", "-xsca");

        public final String csfValue;
        public final String paramValue;

        ScanMode(String csfValue, String paramValue) {
            this.csfValue = csfValue;
            this.paramValue = paramValue;
        }

        public static ScanMode getEnumByCsfValue(String csfValue) {
            for (ScanMode scanMode : ScanMode.values()) {
                if (scanMode.csfValue.equals(csfValue)) {
                    return scanMode;
                }
            }
            throw new IllegalArgumentException("invalid ScanMode csf value:" + csfValue);
        }

        public static ScanMode getEnumByParamValue(String paramValue) {
            for (ScanMode scanMode : ScanMode.values()) {
                if (scanMode.paramValue.equals(paramValue)) {
                    return scanMode;
                }
            }
            throw new IllegalArgumentException("invalid ScanMode param value:" + paramValue);
        }

        public static ScanMode getEnumByName(String name) {
            return Enum.valueOf(ScanMode.class, name);
        }

    }

    public enum ReportType {
        SINGLE,
        CROSS,
        MISRA
    }
}
