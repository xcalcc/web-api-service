-- DROP SCHEMA IF EXISTS xcalibyte CASCADE;
-- DROP EXTENSION IF EXISTS "uuid-ossp";
-- DROP EXTENSION IF EXISTS "pgcrypto";
-- DROP EXTENSION IF EXISTS "pg_stat_statements";
CREATE SCHEMA IF NOT EXISTS xcalibyte ;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

CREATE TABLE IF NOT EXISTS xcalibyte."oauth_user"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    username                        VARCHAR           NULL,
    display_name                    VARCHAR           NULL,
    email                           VARCHAR           NULL,
    password                        VARCHAR           NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS xcalibyte."license"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    license_number                  VARCHAR           NULL,
    company_name                    VARCHAR           NULL,
    max_users                       INT               NULL,
    expires_on                      TIMESTAMP         NULL,
    license_byte                    VARCHAR           NULL,
    status                          VARCHAR           NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS xcalibyte."user"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    username                        VARCHAR           NULL UNIQUE,
    display_name                    VARCHAR           NULL,
    email                           VARCHAR           NULL UNIQUE,
    password                        VARCHAR(255)      NULL,
    status                          VARCHAR(255)      NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_username ON xcalibyte."user" (username);

CREATE TABLE IF NOT EXISTS xcalibyte."login_fail_log"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id                         UUID              REFERENCES xcalibyte."user" (id) ON DELETE CASCADE,
    reason                          VARCHAR           NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_login_fail_log ON xcalibyte."login_fail_log" (user_id);

CREATE TABLE IF NOT EXISTS xcalibyte."user_group"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    group_type                      VARCHAR           NULL,
    group_name                      VARCHAR           NULL,
    description                     TEXT              NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_group_unique UNIQUE (group_type, group_name)

);
CREATE INDEX IF NOT EXISTS idx_userGroup_groupType_groupName ON xcalibyte."user_group" (group_type, group_name);

CREATE TABLE IF NOT EXISTS xcalibyte."user_user_group_mapping"
(
    user_id                         UUID              REFERENCES xcalibyte."user" (id) ON DELETE CASCADE,
    user_group_id                   UUID              REFERENCES xcalibyte."user_group" (id) ON DELETE CASCADE,
    CONSTRAINT user_user_group_mapping_unique UNIQUE (user_id, user_group_id)
);

CREATE TABLE IF NOT EXISTS xcalibyte."license"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    license_number                  VARCHAR           NULL,
    company_name                    TEXT              NULL,
    max_users                       INT               NULL,
    expires_on                      TIMESTAMP         NULL,
    license_byte                    TEXT              NULL,
    status                          VARCHAR           NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS xcalibyte."setting"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    setting_key                     VARCHAR           NOT NULL ,
    setting_value                   TEXT              NOT NULL,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT setting_unique UNIQUE (setting_key)
);

CREATE TABLE IF NOT EXISTS xcalibyte."i18n_message"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    locale                          VARCHAR(16)       NOT NULL,
    message_key                     VARCHAR(255)      NOT NULL,
    content                         TEXT              NULL,
    created_by                      VARCHAR(255)      NOT NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NOT NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT i18n_message_unique UNIQUE (locale, message_key)
);
CREATE INDEX IF NOT EXISTS idx_i18nMessage_locale ON xcalibyte."i18n_message" (locale);
CREATE INDEX IF NOT EXISTS idx_i18nMessage_message_key ON xcalibyte."i18n_message" (message_key);

CREATE TABLE IF NOT EXISTS xcalibyte."file_storage"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    name                            VARCHAR           NULL UNIQUE,
    description                     TEXT              NULL,
    file_storage_type               VARCHAR           NULL,
    file_storage_host               VARCHAR(1024)     NULL,
    credential_type                 VARCHAR           NULL,
    credential                      TEXT              NULL,
    status                          VARCHAR           NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_fileStorage_name ON xcalibyte."file_storage" (name);

CREATE TABLE IF NOT EXISTS xcalibyte."file_info"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    file_storage_id                 UUID              NULL REFERENCES xcalibyte."file_storage" (id) ON DELETE SET NULL,
    name                            VARCHAR           NULL,
    file_storage_extra_info         TEXT              NULL,
    relative_path                   VARCHAR(2048)     NULL,
    version                         VARCHAR(255)      NULL,
    checksum                        VARCHAR(255)      NULL,
    file_size                       BIGINT            NULL,
    no_of_lines                     INT               NULL,
    status                          VARCHAR           NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT file_info_unique UNIQUE (file_storage_id, relative_path, version)
);
CREATE INDEX IF NOT EXISTS idx_fileInfo_fileStorage ON xcalibyte."file_info" (file_storage_id);
CREATE INDEX IF NOT EXISTS idx_fileInfo_fileStorage_relativePath_version_status ON xcalibyte."file_info" (file_storage_id, relative_path, version, status);
CREATE INDEX IF NOT EXISTS idx_fileInfo_fileStorage_relativePath_status ON xcalibyte."file_info" (file_storage_id, relative_path, status);

CREATE TABLE IF NOT EXISTS xcalibyte."scan_engine"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    name                            VARCHAR(255)      NOT NULL,
    version                         VARCHAR(255)      NOT NULL,
    revision                        VARCHAR(255)      NOT NULL,
    description                     VARCHAR(255)      NULL,
    language                        VARCHAR(255)      NULL,
    url                             VARCHAR(255)      NULL,
    provider                        VARCHAR(255)      NULL,
    provider_url                    VARCHAR(1024)     NULL,
    license                         VARCHAR           NULL ,
    license_url                     VARCHAR(1024)     NULL,
    created_by                      VARCHAR(255)      NOT NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NOT NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT scan_engine_unique UNIQUE (name, version)
);
CREATE INDEX IF NOT EXISTS idx_scanEngine_name ON xcalibyte."scan_engine" (name);
CREATE INDEX IF NOT EXISTS idx_scanEngine_name_version ON xcalibyte."scan_engine" (name, version);
CREATE INDEX IF NOT EXISTS idx_scanEngine_language ON xcalibyte."scan_engine" (language);

CREATE TABLE IF NOT EXISTS xcalibyte."rule_set"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    scan_engine_id                  UUID              NOT NULL REFERENCES xcalibyte."scan_engine" (id) ON DELETE CASCADE,
    name                            VARCHAR(255)      NOT NULL,
    version                         VARCHAR(32)       NOT NULL,
    revision                        VARCHAR(255)      NOT NULL,
    display_name                    VARCHAR(32)       NOT NULL,
    description                     TEXT              NULL,
    language                        VARCHAR(32)       NULL,
    url                             VARCHAR(1024)     NULL,
    provider                        VARCHAR(255)      NULL,
    provider_url                    VARCHAR(1024)     NULL,
    license                         VARCHAR(255)      NULL ,
    license_url                     VARCHAR(1024)     NULL,
    created_by                      VARCHAR(255)      NOT NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NOT NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT rule_set_unique UNIQUE (name, version)
);
CREATE INDEX IF NOT EXISTS idx_ruleSet_scanEngine ON xcalibyte."rule_set" (scan_engine_id);
CREATE INDEX IF NOT EXISTS idx_ruleSet_name ON xcalibyte."rule_set" (name);
CREATE INDEX IF NOT EXISTS idx_ruleSet_name_version ON xcalibyte."rule_set" (name, version);

CREATE TABLE IF NOT EXISTS xcalibyte."rule_information"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    rule_set_id                     UUID              NOT NULL REFERENCES xcalibyte."rule_set" (id) ON DELETE CASCADE,
    category                        VARCHAR(255)      NOT NULL,
    vulnerable                      VARCHAR(255)      NULL,
    certainty                       VARCHAR(15)       NULL,
    rule_code                       VARCHAR(255)      NOT NULL,
    language                        VARCHAR(32)       NULL,
    url                             VARCHAR(1024)     NULL,
    name                            VARCHAR(255)      NOT NULL,
    severity                        VARCHAR(16)       NULL,
    priority                        VARCHAR(16)       NULL,
    likelihood                      VARCHAR(16)       NULL,
    remediation_cost                VARCHAR(16)       NULL,
    detail                          TEXT              NULL,
    description                     TEXT              NULL,
    msg_template                    TEXT              NULL,
    created_by                      VARCHAR(255)      NOT NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NOT NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT rule_information_unique UNIQUE (rule_set_id, rule_code)
);
CREATE INDEX IF NOT EXISTS idx_ruleInformation_ruleSet ON xcalibyte."rule_information" (rule_set_id);
CREATE INDEX IF NOT EXISTS idx_ruleInformation_ruleCode ON xcalibyte."rule_information" (rule_code);
CREATE INDEX IF NOT EXISTS idx_ruleInformation_ruleSet_ruleCode ON xcalibyte."rule_information" (rule_set_id, rule_code);

CREATE TABLE IF NOT EXISTS xcalibyte."rule_information_attribute"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    rule_information_id             UUID              NULL REFERENCES xcalibyte."rule_information" (id) ON DELETE CASCADE,
    type                            VARCHAR(255)      NULL,
    name                            VARCHAR(255)      NULL,
    value                           VARCHAR(255)      NULL,
    CONSTRAINT rule_information_attribute_unique UNIQUE (rule_information_id, type, name, value)
);
CREATE INDEX IF NOT EXISTS idx_ruleInformationAttribute_ruleInformation ON xcalibyte."rule_information_attribute" (rule_information_id);
CREATE INDEX IF NOT EXISTS idx_ruleInformationAttribute_ruleInformation_name ON xcalibyte."rule_information_attribute" (rule_information_id, name);
CREATE INDEX IF NOT EXISTS idx_ruleInformationAttribute_ruleInformation_type_name ON xcalibyte."rule_information_attribute" (rule_information_id, type, name);

CREATE TABLE IF NOT EXISTS xcalibyte."project"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    project_id                      VARCHAR           NULL,
    name                            VARCHAR           NULL,
    status                          VARCHAR(255)      NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_project_projectId ON xcalibyte."project" (project_id);

CREATE TABLE IF NOT EXISTS xcalibyte."project_config"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    name                            VARCHAR           NULL,
    project_config                  TEXT              NULL,
    scan_config                     TEXT              NULL,
    project_uuid                    UUID              NULL REFERENCES xcalibyte."project" (id) ON DELETE CASCADE,
    status                          VARCHAR(255)      NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_projectConfig_projectUuid ON xcalibyte."project_config" (project_uuid);
CREATE INDEX IF NOT EXISTS idx_projectConfig_projectUuid_name ON xcalibyte."project_config" (project_uuid, name);

CREATE TABLE IF NOT EXISTS xcalibyte."project_config_attribute"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    project_config_id               UUID              NULL REFERENCES xcalibyte."project_config" (id) ON DELETE CASCADE,
    type                            VARCHAR(255)      NULL,
    name                            VARCHAR(255)      NULL,
    value                           VARCHAR(255)      NULL,
    CONSTRAINT project_config_attribute_unique UNIQUE (project_config_id, type, name, value)
);
CREATE INDEX IF NOT EXISTS idx_projectConfigAttribute_projectConfig ON xcalibyte."project_config_attribute" (project_config_id);
CREATE INDEX IF NOT EXISTS idx_projectConfigAttribute_projectConfig_name ON xcalibyte."project_config_attribute" (project_config_id, name);
CREATE INDEX IF NOT EXISTS idx_projectConfigAttribute_projectConfig_type_name ON xcalibyte."project_config_attribute" (project_config_id, type, name);

CREATE TABLE IF NOT EXISTS xcalibyte."scan_task"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    project_id                      UUID              NULL REFERENCES xcalibyte."project" (id) ON DELETE CASCADE,
    project_config_id               UUID              NULL REFERENCES xcalibyte."project_config" (id) ON DELETE SET NULL,
    status                          VARCHAR(255)      NULL,
    engine                          VARCHAR(255)      NULL,
    engine_version                  VARCHAR(255)      NULL,
    scan_mode                       VARCHAR(255)      NULL,
    source_root                     VARCHAR(255)      NULL,
    scan_parameters                 VARCHAR(2048)     NULL,
    scan_remarks                    VARCHAR(255)      NULL,
    scan_start_at                   TIMESTAMP         NULL,
    scan_end_at                     TIMESTAMP         NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_scanTask_project ON xcalibyte."scan_task" (project_id);

CREATE TABLE IF NOT EXISTS xcalibyte."scan_task_summary"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    scan_task_id                    UUID              NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    name                            VARCHAR(255)      NULL,
    value                           VARCHAR(255)      NULL
);
CREATE INDEX IF NOT EXISTS idx_scanTaskSummary_scanTask ON xcalibyte."scan_task_summary" (scan_task_id);
CREATE INDEX IF NOT EXISTS idx_scanTaskSummary_scanTask_name ON xcalibyte."scan_task_summary" (scan_task_id, name);

CREATE TABLE IF NOT EXISTS xcalibyte."scan_task_status_log"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    scan_task_id                    UUID              NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    stage                           VARCHAR(255)      NULL,
    status                          VARCHAR(255)      NULL,
    unify_error_code                VARCHAR(255)      NULL,
    percentage                      DOUBLE PRECISION  NULL,
    message                         TEXT              NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_scanTaskStatusLog_scanTaskId ON xcalibyte.scan_task_status_log (scan_task_id);

CREATE TABLE IF NOT EXISTS xcalibyte."scan_file"
(
    id                    UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    scan_task_id          UUID          NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    file_info_id          UUID          NULL REFERENCES xcalibyte."file_info" (id) ON DELETE SET NULL,
    project_relative_path VARCHAR(2048) NULL,
    store_path            TEXT          NULL,
    status                VARCHAR       NULL,
    type                  VARCHAR       NULL,
    parent_path           VARCHAR(2048) NULL,
    tree_left             BIGINT        NULL,
    tree_right            BIGINT        NULL,
    depth                 INT           NULL,
    created_by            VARCHAR(255)  NULL,
    created_on            TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by           VARCHAR(255)  NULL,
    modified_on           TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_scanFile_fileInfo ON xcalibyte."scan_file" (file_info_id);
CREATE INDEX IF NOT EXISTS idx_scanFile_scanTask ON xcalibyte."scan_file" (scan_task_id);

CREATE TABLE IF NOT EXISTS xcalibyte."issue"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    issue_key                       VARCHAR(2048)     NULL,
    issue_code                      VARCHAR(255)      NULL,
    rule_information_id             UUID              NULL REFERENCES xcalibyte."rule_information" (id) ON DELETE SET NULL,
    scan_task_id                    UUID              NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    seq                             VARCHAR(8)        NULL,
    severity                        VARCHAR(255)      NULL,
    scan_file_id                    UUID              NULL REFERENCES xcalibyte."scan_file" (id) ON DELETE SET NULL,
    file_path                       VARCHAR(2048)     NULL,
    line_no                         INT               NULL,
    column_no                       INT               NULL,
    function_name                   TEXT              NULL,
    variable_name                   TEXT              NULL,
    complexity                      VARCHAR(255)      NULL,
    checksum                        VARCHAR(255)      NULL,
    message                         TEXT              NULL,
    ignored                         VARCHAR(255)      NULL,
    status                          VARCHAR(255)      NULL,
    action                          VARCHAR(255)      NULL,
    assign_to                       UUID              NULL REFERENCES xcalibyte."user" (id) ON DELETE SET NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_issue_scanTask ON xcalibyte."issue" (scan_task_id);
CREATE INDEX IF NOT EXISTS idx_issue_scanFile ON xcalibyte."issue" (scan_file_id);
CREATE INDEX IF NOT EXISTS idx_issue_ruleInformation ON xcalibyte."issue" (rule_information_id);

CREATE TABLE IF NOT EXISTS xcalibyte."issue_attribute"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    issue_id                        UUID              NULL REFERENCES xcalibyte."issue" (id) ON DELETE CASCADE,
    type                            VARCHAR(255)      NULL,
    name                            VARCHAR(255)      NULL,
    value                           VARCHAR(255)      NULL,
    CONSTRAINT issue_attribute_unique UNIQUE (issue_id, type, name, value)
);
CREATE INDEX IF NOT EXISTS idx_issueAttribute_issue ON xcalibyte."issue_attribute" (issue_id);
CREATE INDEX IF NOT EXISTS idx_issueAttribute_issue_type_name ON xcalibyte."issue_attribute" (issue_id, type, name);

CREATE TABLE IF NOT EXISTS xcalibyte."issue_trace"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    issue_id                        UUID              NULL REFERENCES xcalibyte."issue" (id) ON DELETE CASCADE,
    seq                             INT               NULL,
    scan_file_id                    UUID              NULL REFERENCES xcalibyte."scan_file" (id) ON DELETE SET NULL,
    file_path                       VARCHAR(2048)     NULL,
    line_no                         INT               NULL,
    column_no                       INT               NULL,
    function_name                   TEXT              NULL,
    variable_name                   TEXT              NULL,
    complexity                      VARCHAR(255)      NULL,
    checksum                        VARCHAR(255)      NULL,
    message                         TEXT              NULL,
    created_by                      VARCHAR(255)      NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     VARCHAR(255)      NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_issueTrace_issue ON xcalibyte."issue_trace" (issue_id);
CREATE INDEX IF NOT EXISTS idx_issueTrace_scanFile ON xcalibyte."issue_trace" (scan_file_id);
