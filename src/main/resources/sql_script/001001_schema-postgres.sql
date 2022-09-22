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
    username                        TEXT              NULL,
    display_name                    TEXT              NULL,
    email                           TEXT              NULL,
    password                        TEXT              NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS xcalibyte."license"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    license_number                  TEXT              NULL,
    company_name                    TEXT              NULL,
    max_users                       INT               NULL,
    expires_on                      TIMESTAMP         NULL,
    license_byte                    TEXT              NULL,
    status                          TEXT              NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    product_name                    TEXT              NULL
);

CREATE TABLE IF NOT EXISTS xcalibyte."user"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    username                        TEXT              NULL UNIQUE,
    display_name                    TEXT              NULL,
    email                           TEXT              NULL UNIQUE,
    password                        TEXT              NULL,
    config_num_code_display         INT               NULL DEFAULT 10000,
    status                          TEXT              NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_username ON xcalibyte."user" (username);

CREATE TABLE IF NOT EXISTS xcalibyte."login_fail_log"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id                         UUID              REFERENCES xcalibyte."user" (id) ON DELETE CASCADE,
    reason                          TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_login_fail_log ON xcalibyte."login_fail_log" (user_id);

CREATE TABLE IF NOT EXISTS xcalibyte."user_group"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    group_type                      TEXT              NULL,
    group_name                      TEXT              NULL,
    description                     TEXT              NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
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

CREATE TABLE IF NOT EXISTS xcalibyte."setting"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    setting_key                     TEXT              NOT NULL ,
    setting_value                   TEXT              NOT NULL,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT setting_unique UNIQUE (setting_key)
);

CREATE TABLE IF NOT EXISTS xcalibyte."i18n_message"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    locale                          TEXT              NOT NULL,
    message_key                     TEXT              NOT NULL,
    content                         TEXT              NULL,
    created_by                      TEXT              NOT NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NOT NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT i18n_message_unique UNIQUE (locale, message_key)
);
CREATE INDEX IF NOT EXISTS idx_i18nMessage_locale ON xcalibyte."i18n_message" (locale);
CREATE INDEX IF NOT EXISTS idx_i18nMessage_message_key ON xcalibyte."i18n_message" (message_key);

CREATE TABLE IF NOT EXISTS xcalibyte."file_storage"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    name                            TEXT              NULL UNIQUE,
    description                     TEXT              NULL,
    file_storage_type               TEXT              NULL,
    file_storage_host               TEXT              NULL,
    credential_type                 TEXT              NULL,
    credential                      TEXT              NULL,
    status                          TEXT              NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_fileStorage_name ON xcalibyte."file_storage" (name);

CREATE TABLE IF NOT EXISTS xcalibyte."file_info"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    file_storage_id                 UUID              NULL REFERENCES xcalibyte."file_storage" (id) ON DELETE SET NULL,
    name                            TEXT              NULL,
    file_storage_extra_info         TEXT              NULL,
    relative_path                   TEXT              NULL,
    version                         TEXT              NULL,
    checksum                        TEXT              NULL,
    file_size                       BIGINT            NULL,
    no_of_lines                     INT               NULL,
    type                            TEXT              NULL,
    status                          TEXT              NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT file_info_unique UNIQUE (file_storage_id, relative_path, version)
);
CREATE INDEX IF NOT EXISTS idx_fileInfo_name_version_type_status ON xcalibyte."file_info" (name, version, type, status);
CREATE INDEX IF NOT EXISTS idx_fileInfo_fileStorage_relativePath_version_status ON xcalibyte."file_info" (file_storage_id, relative_path, version, status);
CREATE INDEX IF NOT EXISTS idx_fileInfo_fileStorage_relativePath_status ON xcalibyte."file_info" (file_storage_id, relative_path, status);

CREATE TABLE IF NOT EXISTS xcalibyte."scan_engine"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    name                            TEXT              NOT NULL,
    version                         TEXT              NOT NULL,
    revision                        TEXT              NOT NULL,
    description                     TEXT              NULL,
    language                        TEXT              NULL,
    url                             TEXT              NULL,
    provider                        TEXT              NULL,
    provider_url                    TEXT              NULL,
    license                         TEXT              NULL ,
    license_url                     TEXT              NULL,
    created_by                      TEXT              NOT NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NOT NULL,
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
    name                            TEXT              NOT NULL,
    version                         TEXT              NOT NULL,
    revision                        TEXT              NOT NULL,
    display_name                    TEXT              NOT NULL,
    description                     TEXT              NULL,
    language                        TEXT              NULL,
    url                             TEXT              NULL,
    provider                        TEXT              NULL,
    provider_url                    TEXT              NULL,
    license                         TEXT              NULL ,
    license_url                     TEXT              NULL,
    created_by                      TEXT              NOT NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NOT NULL,
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
    category                        TEXT              NOT NULL,
    vulnerable                      TEXT              NULL,
    certainty                       TEXT              NULL,
    rule_code                       TEXT              NOT NULL,
    language                        TEXT              NULL,
    url                             TEXT              NULL,
    name                            TEXT              NOT NULL,
    severity                        TEXT              NULL,
    priority                        TEXT              NULL,
    likelihood                      TEXT              NULL,
    remediation_cost                TEXT              NULL,
    detail                          TEXT              NULL,
    description                     TEXT              NULL,
    msg_template                    TEXT              NULL,
    created_by                      TEXT              NOT NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NOT NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT rule_information_unique UNIQUE (rule_set_id, rule_code)
);
CREATE INDEX IF NOT EXISTS idx_ruleInformation_ruleSet ON xcalibyte."rule_information" (rule_set_id);
CREATE INDEX IF NOT EXISTS idx_ruleInformation_ruleCode ON xcalibyte."rule_information" (rule_code);
CREATE INDEX IF NOT EXISTS idx_ruleInformation_ruleSet_ruleCode ON xcalibyte."rule_information" (rule_set_id, rule_code);

CREATE TABLE IF NOT EXISTS xcalibyte."rule_information_attribute"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    rule_information_id             UUID              NOT NULL REFERENCES xcalibyte."rule_information" (id) ON DELETE CASCADE,
    type                            TEXT              NULL,
    name                            TEXT              NULL,
    value                           TEXT              NULL,
    CONSTRAINT rule_information_attribute_unique UNIQUE (rule_information_id, type, name, value)
);
CREATE INDEX IF NOT EXISTS idx_ruleInformationAttribute_ruleInformation ON xcalibyte."rule_information_attribute" (rule_information_id);
CREATE INDEX IF NOT EXISTS idx_ruleInformationAttribute_ruleInformation_name ON xcalibyte."rule_information_attribute" (rule_information_id, name);
CREATE INDEX IF NOT EXISTS idx_ruleInformationAttribute_ruleInformation_type_name ON xcalibyte."rule_information_attribute" (rule_information_id, type, name);

CREATE TABLE IF NOT EXISTS xcalibyte."rule_standard_set"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    name                            TEXT              NOT NULL,
    version                         TEXT              NULL,
    revision                        TEXT              NULL,
    display_name                    TEXT              NOT NULL,
    description                     TEXT              NULL,
    language                        TEXT              NULL,
    url                             TEXT              NULL,
    provider                        TEXT              NULL,
    provider_url                    TEXT              NULL,
    license                         TEXT              NULL ,
    license_url                     TEXT              NULL,
    created_by                      TEXT              NOT NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NOT NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT rule_standard_set_unique UNIQUE (name, version)
);
CREATE INDEX IF NOT EXISTS idx_ruleStandardSet_name ON xcalibyte."rule_standard_set" (name);
CREATE INDEX IF NOT EXISTS idx_ruleStandardSet_name_version ON xcalibyte."rule_standard_set" (name, version);

CREATE TABLE IF NOT EXISTS xcalibyte."rule_standard"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    rule_standard_set_id            UUID              NOT NULL REFERENCES xcalibyte."rule_standard_set" (id) ON DELETE CASCADE,
    category                        TEXT              NULL,
    code                            TEXT              NOT NULL,
    language                        TEXT              NULL,
    url                             TEXT              NULL,
    name                            TEXT              NOT NULL,
    detail                          TEXT              NULL,
    description                     TEXT              NULL,
    msg_template                    TEXT              NULL,
    created_by                      TEXT              NOT NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NOT NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT rule_standard_unique UNIQUE (rule_standard_set_id, code)
);
CREATE INDEX IF NOT EXISTS idx_ruleStandard_ruleStandardSet ON xcalibyte."rule_standard" (rule_standard_set_id);
CREATE INDEX IF NOT EXISTS idx_ruleStandard_code ON xcalibyte."rule_standard" (code);
CREATE INDEX IF NOT EXISTS idx_ruleStandard_ruleStandardSet_code ON xcalibyte."rule_standard" (rule_standard_set_id, code);

CREATE TABLE IF NOT EXISTS xcalibyte."rule_standard_attribute"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    rule_standard_id                UUID              NOT NULL REFERENCES xcalibyte."rule_standard" (id) ON DELETE CASCADE,
    type                            TEXT              NULL,
    name                            TEXT              NULL,
    value                           TEXT              NULL,
    CONSTRAINT rule_standard_attribute_unique UNIQUE (rule_standard_id, type, name, value)
);
CREATE INDEX IF NOT EXISTS idx_ruleStandardAttribute_ruleStandard ON xcalibyte."rule_standard_attribute" (rule_standard_id);
CREATE INDEX IF NOT EXISTS idx_ruleStandardAttribute_ruleStandard_name ON xcalibyte."rule_standard_attribute" (rule_standard_id, name);
CREATE INDEX IF NOT EXISTS idx_ruleStandardAttribute_ruleStandard_type_name ON xcalibyte."rule_standard_attribute" (rule_standard_id, type, name);

CREATE TABLE IF NOT EXISTS xcalibyte."project"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    project_id                      TEXT              NULL,
    name                            TEXT              NULL,
    status                          TEXT              NULL,
    need_dsr                        bool              NULL           DEFAULT false,
    scan_mode                       TEXT              NULL default 'CROSS',
    cicd_fsm_state                  TEXT              NULL default 'START',
    baseline_commit_id              TEXT              NULL,
    retention_num                   INT               NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_project_projectId ON xcalibyte."project" (project_id);

CREATE TABLE IF NOT EXISTS xcalibyte."project_summary"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    project_id                      UUID              NULL REFERENCES xcalibyte."project" (id) ON DELETE CASCADE,
    name                            TEXT              NULL,
    value                           TEXT              NULL
);
CREATE INDEX IF NOT EXISTS idx_projectSummary_project ON xcalibyte."project_summary" (project_id);
CREATE INDEX IF NOT EXISTS idx_projectSummary_project_name ON xcalibyte."project_summary" (project_id, name);

CREATE TABLE IF NOT EXISTS xcalibyte."project_config"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    name                            TEXT              NULL,
    project_config                  TEXT              NULL,
    scan_config                     TEXT              NULL,
    project_uuid                    UUID              NULL REFERENCES xcalibyte."project" (id) ON DELETE CASCADE,
    status                          TEXT              NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_projectConfig_projectUuid ON xcalibyte."project_config" (project_uuid);
CREATE INDEX IF NOT EXISTS idx_projectConfig_projectUuid_name ON xcalibyte."project_config" (project_uuid, name);

CREATE TABLE IF NOT EXISTS xcalibyte."project_config_attribute"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    project_config_id               UUID              NULL REFERENCES xcalibyte."project_config" (id) ON DELETE CASCADE,
    type                            TEXT              NULL,
    name                            TEXT              NULL,
    value                           TEXT              NULL,
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
    status                          TEXT              NULL,
    engine                          TEXT              NULL,
    engine_version                  TEXT              NULL,
    scan_mode                       TEXT              NULL,
    source_root                     TEXT              NULL,
    scan_parameters                 TEXT              NULL,
    scan_remarks                    TEXT              NULL,
    scan_start_at                   TIMESTAMP         NULL,
    scan_end_at                     TIMESTAMP         NULL,
    created_by                      TEXT              NULL,
    house_keep_on                   TIMESTAMP         NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_scanTask_project ON xcalibyte."scan_task" (project_id);
CREATE INDEX IF NOT EXISTS scan_task_project_config_id_idx ON xcalibyte.scan_task (project_config_id);

CREATE TABLE IF NOT EXISTS xcalibyte."scan_task_summary"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    scan_task_id                    UUID              NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    name                            TEXT              NULL,
    value                           TEXT              NULL
);
CREATE INDEX IF NOT EXISTS idx_scanTaskSummary_scanTask ON xcalibyte."scan_task_summary" (scan_task_id);
CREATE INDEX IF NOT EXISTS idx_scanTaskSummary_scanTask_name ON xcalibyte."scan_task_summary" (scan_task_id, name);

CREATE TABLE IF NOT EXISTS xcalibyte."scan_task_status_log"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    scan_task_id                    UUID              NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    stage                           TEXT              NULL,
    status                          TEXT              NULL,
    unify_error_code                TEXT              NULL,
    percentage                      DOUBLE PRECISION  NULL,
    message                         TEXT              NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_scanTaskStatusLog_scanTaskId ON xcalibyte.scan_task_status_log (scan_task_id);

CREATE TABLE IF NOT EXISTS xcalibyte."scan_file"
(
    id                    UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    scan_task_id          UUID          NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    file_info_id          UUID          NULL REFERENCES xcalibyte."file_info" (id) ON DELETE SET NULL,
    project_relative_path TEXT          NULL,
    store_path            TEXT          NULL,
    status                TEXT          NULL,
    type                  TEXT          NULL,
    parent_path           TEXT          NULL,
    tree_left             BIGINT        NULL,
    tree_right            BIGINT        NULL,
    depth                 INT           NULL,
    created_by            TEXT          NULL,
    created_on            TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by           TEXT          NULL,
    modified_on           TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_scanFile_fileInfo ON xcalibyte."scan_file" (file_info_id);
CREATE INDEX IF NOT EXISTS idx_scanFile_scanTask ON xcalibyte."scan_file" (scan_task_id);
CREATE INDEX IF NOT EXISTS idx_scan_file_scan_task_id ON xcalibyte."scan_file" (scan_task_id,project_relative_path);


CREATE TABLE IF NOT EXISTS xcalibyte."issue_v2"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    issue_key                       TEXT              NULL,
    issue_code                      TEXT              NULL,
    rule_information_id             UUID              NULL REFERENCES xcalibyte."rule_information" (id) ON DELETE SET NULL,
    scan_task_id                    UUID              NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    seq                             TEXT              NULL,
    severity                        TEXT              NULL,
    scan_file_id                    UUID              NULL REFERENCES xcalibyte."scan_file" (id) ON DELETE SET NULL,
    file_path                       TEXT              NULL,
    line_no                         INT               NULL,
    column_no                       INT               NULL,
    function_name                   TEXT              NULL,
    variable_name                   TEXT              NULL,
    complexity                      TEXT              NULL,
    checksum                        TEXT              NULL,
    message                         TEXT              NULL,
    ignored                         TEXT              NULL,
    status                          TEXT              NULL,
    action                          TEXT              NULL,
    assign_to                       UUID              NULL REFERENCES xcalibyte."user" (id) ON DELETE SET NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_issue_v2_scanTask ON xcalibyte."issue_v2" (scan_task_id);
CREATE INDEX IF NOT EXISTS idx_issue_v2_scanFile ON xcalibyte."issue_v2" (scan_file_id);
CREATE INDEX IF NOT EXISTS idx_issue_v2_ruleInformation ON xcalibyte."issue_v2" (rule_information_id);

CREATE TABLE IF NOT EXISTS xcalibyte."issue_attribute_v2"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    issue_id                        UUID              NULL REFERENCES xcalibyte."issue_v2" (id) ON DELETE CASCADE,
    type                            TEXT              NULL,
    name                            TEXT              NULL,
    value                           TEXT              NULL,
    CONSTRAINT issue_attribute_unique UNIQUE (issue_id, type, name, value)
);
CREATE INDEX IF NOT EXISTS idx_issueAttribute_v2_issue_v2 ON xcalibyte."issue_attribute_v2" (issue_id);
CREATE INDEX IF NOT EXISTS idx_issueAttribute_v2_issue_v2_type_name ON xcalibyte."issue_attribute_v2" (issue_id, type, name);

CREATE TABLE IF NOT EXISTS xcalibyte."issue_string_v2" (
	id bigserial NOT NULL,
	value text NOT NULL,
	project_id uuid not null,
	CONSTRAINT issue_path_pk PRIMARY KEY (id)
)
TABLESPACE pg_default;


CREATE TABLE IF NOT EXISTS xcalibyte."issue_trace_v2"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    issue_id                        UUID              NULL REFERENCES xcalibyte."issue_v2" (id) ON DELETE CASCADE,
    seq                             INT               NULL,
    scan_file_id                    UUID              NULL REFERENCES xcalibyte."scan_file" (id) ON DELETE SET NULL,
    file_path                       TEXT              NULL,
    file_path_id                    bigint            NULL,
    line_no                         INT               NULL,
    column_no                       INT               NULL,
    function_name                   TEXT              NULL,
    function_name_id                bigint            NULL,
    variable_name                   TEXT              NULL,
    variable_name_id                bigint            NULL,
    complexity                      TEXT              NULL,
    checksum                        TEXT              NULL,
    message                         TEXT              NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_issueTrace_v2_issue_v2 ON xcalibyte."issue_trace_v2" (issue_id);
CREATE INDEX IF NOT EXISTS idx_issueTrace_v2_scanFile ON xcalibyte."issue_trace_v2" (scan_file_id);

CREATE TABLE IF NOT EXISTS xcalibyte."issue_diff_v2"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    scan_task_id                    UUID              NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    baseline_scan_task_id           UUID              NULL REFERENCES xcalibyte."scan_task" (id) ON DELETE CASCADE,
    issue_id                        UUID              NULL REFERENCES xcalibyte."issue_v2" (id) ON DELETE CASCADE,
    baseline_issue_key              TEXT              NULL,
    issue_key                       TEXT              NULL,
    checksum                        TEXT              NULL,
    type                            TEXT              NULL,
    created_by                      TEXT              NULL,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_issueDiff_v2_scanTask ON xcalibyte."issue_diff_v2" (scan_task_id);
CREATE INDEX IF NOT EXISTS idx_issueDiff_v2_scanTaskBaseline ON xcalibyte."issue_diff_v2" (baseline_scan_task_id);
CREATE INDEX IF NOT EXISTS idx_issueDiff_v2_issueChecksum ON xcalibyte."issue_diff_v2" (issue_id, checksum);
CREATE INDEX IF NOT EXISTS idx_issueDiff_v2_issueKey ON xcalibyte."issue_diff_v2" (issue_key);

CREATE TABLE IF NOT EXISTS xcalibyte."async_job"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    name                            TEXT,
    info                            TEXT,
    result                          TEXT,
    status                          TEXT,
    created_by                      TEXT,
    created_on                      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    modified_by                     TEXT,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS xcalibyte."file_cache"
(
    id                              UUID              DEFAULT uuid_generate_v4() PRIMARY KEY,
    cache_key                       TEXT              NOT NULL,
    cache_value                     TEXT              NOT NULL,
    modified_by                     TEXT              NULL,
    modified_on                     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT file_cache_unique UNIQUE (cache_key)
);


CREATE TABLE IF NOT EXISTS xcalibyte.issue_file (
	id int4 NOT NULL,
	scan_task_id uuid NOT NULL,
	"path" text NULL,
	CONSTRAINT issue_file_pkey PRIMARY KEY (id, scan_task_id),
	CONSTRAINT issue_file_scan_task_id_fkey FOREIGN KEY (scan_task_id) REFERENCES xcalibyte.scan_task(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS xcalibyte.issue_string (
	id int4 NOT NULL,
	scan_task_id uuid NOT NULL,
	str text NULL,
	CONSTRAINT issue_string_pkey PRIMARY KEY (id, scan_task_id),
	CONSTRAINT issue_string_scan_task_id_fkey FOREIGN KEY (scan_task_id) REFERENCES xcalibyte.scan_task(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS xcalibyte.issue_group (
	id text NOT NULL ,
	project_id uuid NULL,
	scan_task_id uuid NULL,
	occur_scan_task_id uuid NULL,
	fixed_scan_task_id uuid NULL,
	rule_code text NULL,
	rule_set text NULL,
	src_file_path_id int4 NULL,
	src_line_no int4 NULL,
	src_column_no int4 NULL,
	src_message_id int4 NULL,
	sink_file_path_id int4 NULL,
	sink_line_no int4 NULL,
	sink_column_no int4 NULL,
	sink_message_id int4 NULL,
	function_name_id int4 NULL,
	variable_name_id int4 NULL,
	severity text NULL,
	likelihood text NULL,
	remediation_cost text NULL,
	complexity int4 NULL,
	priority text NULL,
	certainty text NULL,
	criticality int4 NULL,
	category text NULL,
	issue_count int4 NULL,
	avg_trace_count int4 NULL,
	status text NULL,
	dsr text NULL,
	occur_time timestamp NULL,
	fixed_time timestamp NULL,
	assign_to uuid NULL,
	CONSTRAINT issue_group_pkey PRIMARY KEY (scan_task_id,id),
	CONSTRAINT issue_group_assign_to_fkey FOREIGN KEY (assign_to) REFERENCES xcalibyte."user"(id) ON DELETE SET NULL,
	CONSTRAINT issue_group_scan_task_id_fkey FOREIGN KEY (scan_task_id) REFERENCES xcalibyte.scan_task(id) ON DELETE SET NULL,
	CONSTRAINT issue_group_occur_scan_task_id_fkey FOREIGN KEY (occur_scan_task_id) REFERENCES xcalibyte.scan_task(id) ON DELETE SET NULL,
	CONSTRAINT issue_group_fixed_scan_task_id_fkey FOREIGN KEY (fixed_scan_task_id) REFERENCES xcalibyte.scan_task(id) ON DELETE SET NULL,
	CONSTRAINT issue_group_project_id_fkey FOREIGN KEY (project_id) REFERENCES xcalibyte.project(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_issue_group_scan_task ON xcalibyte.issue_group USING btree (scan_task_id);
CREATE INDEX IF NOT EXISTS idx_issue_group_first_scan_task ON xcalibyte.issue_group USING btree (occur_scan_task_id);
CREATE INDEX IF NOT EXISTS idx_issue_group_fixed_scan_task ON xcalibyte.issue_group USING btree (fixed_scan_task_id);
CREATE INDEX IF NOT EXISTS issue_group_project_id_idx ON xcalibyte.issue_group (project_id,rule_code,criticality);

CREATE TABLE IF NOT EXISTS xcalibyte.issue  (
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	scan_task_id uuid NULL,
	issue_group_id text NULL,
	certainty text NULL,
	trace_count int4 NULL,
	trace_path text NULL,
	status text NULL,
	dsr text NULL,
	CONSTRAINT issue_pkey PRIMARY KEY (id),
	CONSTRAINT issue_issue_group_id_fkey FOREIGN KEY (scan_task_id,issue_group_id) REFERENCES xcalibyte.issue_group(scan_task_id,id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_issue_issue_group ON xcalibyte.issue USING btree (issue_group_id);


