-- ------------------------
-- Scan Engine SpotBugs
-- ------------------------
insert into xcalibyte.scan_engine(name, version, revision, description, language, url, provider, provider_url, license, license_url, created_by, modified_by)
values
('SpotBugs', '4.0.0', '4.0.0', 'SpotBugs', 'java', 'https://spotbugs.github.io/', 'SpotBugs', 'https://spotbugs.github.io/', 'XGNU Lesser General Public License', 'https://www.gnu.org/licenses/lgpl-3.0.html', 'system', 'system')
ON CONFLICT DO NOTHING;


-- ------------------------
-- Rule set SpotBug builtin, Java
-- ------------------------
insert into xcalibyte.rule_set
(scan_engine_id, name, version, revision, display_name, description, language, url, provider, provider_url, license, license_url, created_by, modified_by)
values
((select id from xcalibyte."scan_engine" where name = 'SpotBugs' and version ='4.0.0'), 'builtin', '4.0.0', '4.0.0', 'SpotBugs', 'SpotBug builtin rules', 'java', 'https://spotbugs.readthedocs.io/en/latest/bugDescriptions.html', 'SpotBugs', 'https://spotbugs.github.io/', 'GNU Lesser General Public License', 'https://www.gnu.org/licenses/lgpl-3.0.html', 'system', 'system')
ON CONFLICT DO NOTHING;



-- ------------------------
-- NOISE_FIELD_REFERENCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'NOISE', 'NOISE_FIELD_REFERENCE', null, 'NOISE_FIELD_REFERENCE', 'java', null, 'NOISE_FIELD_REFERENCE', null, null, null, null, 'Bogus warning.', 'Bogus warning about a field reference', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NOISE_FIELD_REFERENCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NOISE_FIELD_REFERENCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NOISE_METHOD_CALL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'NOISE', 'NOISE_METHOD_CALL', null, 'NOISE_METHOD_CALL', 'java', null, 'NOISE_METHOD_CALL', null, null, null, null, 'Bogus warning.', 'Bogus warning about a method call', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NOISE_METHOD_CALL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NOISE_METHOD_CALL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NOISE_NULL_DEREFERENCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'NOISE', 'NOISE_NULL_DEREFERENCE', null, 'NOISE_NULL_DEREFERENCE', 'java', null, 'NOISE_NULL_DEREFERENCE', null, null, null, null, 'Bogus warning.', 'Bogus warning about a null pointer dereference', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NOISE_NULL_DEREFERENCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NOISE_NULL_DEREFERENCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- OISE_OPERATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
 ((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'NOISE', 'NOISE_OPERATION', null, 'NOISE_OPERATION', 'java', null, 'NOISE_OPERATION', null, null, null, null, 'Bogus warning.', 'Bogus warning about an operation', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NOISE_OPERATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NOISE_OPERATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

