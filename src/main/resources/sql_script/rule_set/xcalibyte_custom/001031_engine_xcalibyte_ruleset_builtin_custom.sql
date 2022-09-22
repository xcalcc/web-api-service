-- ------------------------
-- Scan Engine Xcalibyte
-- ------------------------
insert into xcalibyte.scan_engine (name, version, revision, description, language, url, provider, provider_url, license, license_url, created_by, modified_by)
values
('Xcalibyte', '1', '1.0', 'Xcalibyte static analyzer', 'c,c++,java', 'http://www.xcalibyte.com', 'Xcalibyte', 'http://www.xcalibyte.com', 'Xcalibyte commercial license', '', 'system', 'system')
ON CONFLICT DO NOTHING;

-- ------------------------
-- Rule set Xcalibyte BUILTIN
-- ------------------------
insert into xcalibyte.rule_set
(scan_engine_id, name, version, revision, display_name, description, language, url, provider, provider_url, license, license_url, created_by, modified_by)
values
((select id from xcalibyte."scan_engine" where name = 'Xcalibyte' and version ='1'), 'BUILTIN', '1', '1.0', 'Xcalibyte', 'Xcalibyte static analyzer builtin ruleset', 'c,c++,java', '', 'Xcalibyte', 'http://www.xcalibyte.com', 'Xcalibyte commercial license', '', 'system', 'system')
ON CONFLICT DO NOTHING;

-- ------------------------
-- CUS-0
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'USER', 'CUS-0', 'CUS-0', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.CUS-0.name}', '3', '2', 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.CUS-0.detail}', '${rule.Xcalibyte.BUILTIN.1.CUS-0.description}', '${rule.Xcalibyte.BUILTIN.1.CUS-0.msg_template}', 'admin', 'admin')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
    (locale, message_key, content, created_by, modified_by)
values ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-0.name', 'Customized rule #0', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-0.name', '定制规则#0。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-0.description', 'This is a customized rule #0.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-0.description', '这是一条定制的规则#0。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-0.detail', 'This is a customized rule #0.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-0.detail', '这是一条定制的规则#0。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-0.msg_template', 'This is a customized rule #0.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-0.msg_template', '这是一条定制的规则#0。', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-0');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-0'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-0'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-0'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-0'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-0'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CUS-1
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'USER', 'CUS-1', 'CUS-1', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.CUS-1.name}', '3', '2', 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.CUS-1.detail}', '${rule.Xcalibyte.BUILTIN.1.CUS-1.description}', '${rule.Xcalibyte.BUILTIN.1.CUS-1.msg_template}', 'admin', 'admin')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
    (locale, message_key, content, created_by, modified_by)
values ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-1.name', 'Customized rule #1', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-1.name', '定制规则#1。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-1.description', 'This is a customized rule #1.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-1.description', '这是一条定制的规则#1。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-1.detail', 'This is a customized rule #1.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-1.detail', '这是一条定制的规则#1。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-1.msg_template', 'This is a customized rule #1.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-1.msg_template', '这是一条定制的规则#1。', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-1');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-1'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-1'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-1'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-1'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-1'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CUS-2
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'USER', 'CUS-2', 'CUS-2', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.CUS-2.name}', '3', '2', 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.CUS-2.detail}', '${rule.Xcalibyte.BUILTIN.1.CUS-2.description}', '${rule.Xcalibyte.BUILTIN.1.CUS-2.msg_template}', 'admin', 'admin')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
    (locale, message_key, content, created_by, modified_by)
values ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-2.name', 'Customized rule #2', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-2.name', '定制规则#2。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-2.description', 'This is a customized rule #2.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-2.description', '这是一条定制的规则#2。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-2.detail', 'This is a customized rule #2.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-2.detail', '这是一条定制的规则#2。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-2.msg_template', 'This is a customized rule #2.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-2.msg_template', '这是一条定制的规则#2。', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-2');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-2'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-2'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-2'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-2'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-2'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CUS-3
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'USER', 'CUS-3', 'CUS-3', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.CUS-3.name}', '3', '2', 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.CUS-3.detail}', '${rule.Xcalibyte.BUILTIN.1.CUS-3.description}', '${rule.Xcalibyte.BUILTIN.1.CUS-3.msg_template}', 'admin', 'admin')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
    (locale, message_key, content, created_by, modified_by)
values ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-3.name', 'Customized rule #3', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-3.name', '定制规则#3。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-3.description', 'This is a customized rule #3.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-3.description', '这是一条定制的规则#3。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-3.detail', 'This is a customized rule #3.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-3.detail', '这是一条定制的规则#3。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-3.msg_template', 'This is a customized rule #3.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-3.msg_template', '这是一条定制的规则#3。', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-3');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-3'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-3'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-3'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-3'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-3'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CUS-4
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'USER', 'CUS-4', 'CUS-4', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.CUS-4.name}', '3', '2', 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.CUS-4.detail}', '${rule.Xcalibyte.BUILTIN.1.CUS-4.description}', '${rule.Xcalibyte.BUILTIN.1.CUS-4.msg_template}', 'admin', 'admin')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
    (locale, message_key, content, created_by, modified_by)
values ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-4.name', 'Customized rule #4', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-4.name', '定制规则#4。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-4.description', 'This is a customized rule #4.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-4.description', '这是一条定制的规则#4。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-4.detail', 'This is a customized rule #4.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-4.detail', '这是一条定制的规则#4。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-4.msg_template', 'This is a customized rule #4.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-4.msg_template', '这是一条定制的规则#4。', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-4');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-4'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-4'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-4'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-4'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-4'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CUS-5
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'USER', 'CUS-5', 'CUS-5', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.CUS-5.name}', '3', '2', 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.CUS-5.detail}', '${rule.Xcalibyte.BUILTIN.1.CUS-5.description}', '${rule.Xcalibyte.BUILTIN.1.CUS-5.msg_template}', 'admin', 'admin')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
    (locale, message_key, content, created_by, modified_by)
values ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-5.name', 'Customized rule #5', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-5.name', '定制规则#5。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-5.description', 'This is a customized rule #5.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-5.description', '这是一条定制的规则#5。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-5.detail', 'This is a customized rule #5.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-5.detail', '这是一条定制的规则#5。', 'system', 'system'),
       ('en', 'rule.Xcalibyte.BUILTIN.1.CUS-5.msg_template', 'This is a customized rule #5.', 'system', 'system'),
       ('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CUS-5.msg_template', '这是一条定制的规则#5。', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-5');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-5'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-5'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-5'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-5'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CUS-5'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

