insert into xcalibyte.user_group
(group_type, group_name, description, created_by, modified_by) values
('ROLE', 'admin', 'Administrator', 'system', 'system'),
('ROLE', 'super_admin', 'Super Administrator', 'system', 'system')
ON CONFLICT DO NOTHING;

insert into xcalibyte.user
(username, display_name, email, password, created_by, modified_by, status) values
('admin', 'Administrator', 'admin@xcalibyte.com', crypt('admin', gen_salt('bf')), 'system', 'system', 'ACTIVE')
ON CONFLICT DO NOTHING;

insert into xcalibyte.user_user_group_mapping
(user_id, user_group_id) values
((select id from xcalibyte."user" where username = 'admin'), (select id from xcalibyte."user_group" where group_type = 'ROLE' and group_name = 'admin'))
ON CONFLICT DO NOTHING;

insert into xcalibyte.file_storage
(name, description, file_storage_type, file_storage_host, credential_type, credential, status, created_by, modified_by) values
('volume_src', 'docker volume for source', 'VOLUME', '/share/src', null, null, 'ACTIVE', 'system', 'system'),
('volume_scan', 'docker volume for scan path', 'VOLUME', '/share/scan', null, null, 'ACTIVE', 'system', 'system'),
('gitlab', 'GitLab.com', 'GITLAB', 'https://gitlab.com', null, null, 'ACTIVE', 'system', 'system'),
('github', 'GitHub.com', 'GITHUB', 'https://github.com', null, null, 'ACTIVE', 'system', 'system'),
('volume_upload', 'docker volume for upload files', 'VOLUME', '/share/upload', null, null, 'ACTIVE', 'system', 'system'),
('volume_tmp', 'docker volume for tmp files', 'VOLUME', '/share/tmp', null, null, 'ACTIVE', 'system', 'system'),
('volume_lib', 'docker volume for upload lib files', 'VOLUME', '/share/lib', null, null, 'ACTIVE', 'system', 'system'),
('agent', 'source code exist on agent machine', 'AGENT', '/', null, null, 'ACTIVE', 'system', 'system'),
('gerrit', 'a code review and project management tool for Git based projects', 'GERRIT', '/', null, null, 'ACTIVE', 'system', 'system'),
('volume_diagnostic', 'docker volume for diagnostic path', 'VOLUME', '/share/diagnostic', null, null, 'ACTIVE', 'system', 'system')
ON CONFLICT DO NOTHING;

INSERT INTO xcalibyte.setting (setting_key, setting_value, modified_by) VALUES
('xcal.support.email.title', '[Xcalscan] xcal support email', 'system'),
('license.public.key', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIf2tYNZ1NjuxhjfQUJIVYwUPzU0IRnGyLdeFvPWyu3xSZuZvJ0V3eZgLTLvcqnzcXMDA/UnfpaBJ0Bol8NORz8CAwEAAQ==', 'system'),
('license.encrypt.aes.key', 'I2rTqnHWsYzC0UdSciq3sOiTxBQrdOwmwl0mcJK/YR4XuGzhLtjZU497NRyJUiUA/sVIHerCPIO0lNinCvKYlw==', 'system'),
('xcal.support.email', 'support@xcalibyte.com', 'system'),
('retention_period', '7', 'system'),
('retention_num', '5', 'system')
ON CONFLICT DO NOTHING;

insert into xcalibyte.i18n_message
 (locale, message_key, content, created_by, modified_by)
 values
 ('en', 'locale.name', 'English', 'system', 'system'),
 ('zh-HK', 'locale.name', '繁體中文(香港)', 'system', 'system'),
 ('zh-CN', 'locale.name', '简体中文', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;