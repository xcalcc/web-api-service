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
--DMI_CONSTANT_DB_PASSWORD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'DMI_CONSTANT_DB_PASSWORD', null, 'DMI_CONSTANT_DB_PASSWORD', 'java', null, 'DMI_CONSTANT_DB_PASSWORD', null, null, null, null, 'This code creates a database connect using a hardcoded, constant password. Anyone with access to either the source code or the compiled code can easily learn the password.', 'Hardcoded constant database password', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_CONSTANT_DB_PASSWORD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_CONSTANT_DB_PASSWORD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_CONSTANT_DB_PASSWORD'),
 'STANDARD', 'CWE','259')
ON CONFLICT DO NOTHING;

-- ------------------------
--DMI_EMPTY_DB_PASSWORD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'DMI_EMPTY_DB_PASSWORD', null, 'DMI_EMPTY_DB_PASSWORD', 'java', null, 'DMI_EMPTY_DB_PASSWORD', null, null, null, null, 'This code creates a database connect using a blank or empty password. This indicates that the database is not protected by a password.', 'Empty database password', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_EMPTY_DB_PASSWORD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_EMPTY_DB_PASSWORD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_EMPTY_DB_PASSWORD'),
 'STANDARD', 'CWE','259')
ON CONFLICT DO NOTHING;

-- ------------------------
--HRS_REQUEST_PARAMETER_TO_COOKIE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'HRS_REQUEST_PARAMETER_TO_COOKIE', null, 'HRS_REQUEST_PARAMETER_TO_COOKIE', 'java', null, 'HRS_REQUEST_PARAMETER_TO_COOKIE', null, null, null, null, 'This code constructs an HTTP Cookie using an untrusted HTTP parameter. If this cookie is added to an HTTP response, it will allow a HTTP response splitting vulnerability. See <a href="http://en.wikipedia.org/wiki/HTTP_response_splitting">http://en.wikipedia.org/wiki/HTTP_response_splitting</a>for more information.SpotBugs looks only for the most blatant, obvious cases of HTTP response splitting.If SpotBugs found <em>any</em>, you <em>almost certainly</em> have more vulnerabilities that SpotBugs doesn''t report. If you are concerned about HTTP response splitting, you should seriously consider using a commercial static analysis or pen-testing tool.', 'HTTP cookie formed from untrusted input', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HRS_REQUEST_PARAMETER_TO_COOKIE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HRS_REQUEST_PARAMETER_TO_COOKIE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HRS_REQUEST_PARAMETER_TO_COOKIE'),
 'STANDARD', 'CWE','113')
ON CONFLICT DO NOTHING;

-- ------------------------
--HRS_REQUEST_PARAMETER_TO_HTTP_HEADER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'HRS_REQUEST_PARAMETER_TO_HTTP_HEADER', null, 'HRS_REQUEST_PARAMETER_TO_HTTP_HEADER', 'java', null, 'HRS_REQUEST_PARAMETER_TO_HTTP_HEADER', null, null, null, null, 'This code directly writes an HTTP parameter to an HTTP header, which allows for a HTTP response splitting vulnerability. See <a href="http://en.wikipedia.org/wiki/HTTP_response_splitting">http://en.wikipedia.org/wiki/HTTP_response_splitting</a>for more information.SpotBugs looks only for the most blatant, obvious cases of HTTP response splitting.If SpotBugs found <em>any</em>, you <em>almost certainly</em> have more vulnerabilities that SpotBugs doesn''t report. If you are concerned about HTTP response splitting, you should seriously consider using a commercial static analysis or pen-testing tool.', 'HTTP Response splitting vulnerability', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HRS_REQUEST_PARAMETER_TO_HTTP_HEADER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HRS_REQUEST_PARAMETER_TO_HTTP_HEADER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HRS_REQUEST_PARAMETER_TO_HTTP_HEADER'),
 'STANDARD', 'CWE','113')
ON CONFLICT DO NOTHING;


-- ------------------------
--PT_ABSOLUTE_PATH_TRAVERSAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'PT_ABSOLUTE_PATH_TRAVERSAL', null, 'PT_ABSOLUTE_PATH_TRAVERSAL', 'java', null, 'PT_ABSOLUTE_PATH_TRAVERSAL', null, null, null, null, 'The software uses an HTTP request parameter to construct a pathname that should be within a restricted directory,but it does not properly neutralize absolute path sequences such as "/abs/path" that can resolve to a location that is outside of that directory.See <a href="http://cwe.mitre.org/data/definitions/36.html">http://cwe.mitre.org/data/definitions/36.html</a>for more information.SpotBugs looks only for the most blatant, obvious cases of absolute path traversal.If SpotBugs found <em>any</em>, you <em>almost certainly</em> have more vulnerabilities that SpotBugs doesn''t report. If you are concerned about absolute path traversal, you should seriously consider using a commercial static analysis or pen-testing tool.', 'Absolute path traversal in servlet', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PT_ABSOLUTE_PATH_TRAVERSAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PT_ABSOLUTE_PATH_TRAVERSAL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PT_ABSOLUTE_PATH_TRAVERSAL'),
 'STANDARD', 'CWE','36')
ON CONFLICT DO NOTHING;


-- ------------------------
--PT_RELATIVE_PATH_TRAVERSAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'PT_RELATIVE_PATH_TRAVERSAL', null, 'PT_RELATIVE_PATH_TRAVERSAL', 'java', null, 'PT_RELATIVE_PATH_TRAVERSAL', null, null, null, null, 'The software uses an HTTP request parameter to construct a pathname that should be within a restricted directory, but it does not properly neutralize sequences such as ".." that can resolve to a location that is outside of that directory.See <a href="http://cwe.mitre.org/data/definitions/23.html">http://cwe.mitre.org/data/definitions/23.html</a>for more information.SpotBugs looks only for the most blatant, obvious cases of relative path traversal.If SpotBugs found <em>any</em>, you <em>almost certainly</em> have more vulnerabilities that SpotBugs doesn''t report. If you are concerned about relative path traversal, you should seriously consider using a commercial static analysis or pen-testing tool.', 'Relative path traversal in servlet', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PT_RELATIVE_PATH_TRAVERSAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PT_RELATIVE_PATH_TRAVERSAL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PT_RELATIVE_PATH_TRAVERSAL'),
 'STANDARD', 'CWE','23')
ON CONFLICT DO NOTHING;


-- ------------------------
--SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE', null, 'SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE', 'java', null, 'SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE', null, null, null, null, 'The method invokes the execute or addBatch method on an SQL statement with a String that seems to be dynamically generated. Consider using a prepared statement instead. It is more efficient and less vulnerable toSQL injection attacks.', 'Nonconstant string passed to execute or addBatch method on an SQL statement', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE'),
 'STANDARD', 'CWE','89')
ON CONFLICT DO NOTHING;


-- ------------------------
--SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING', null, 'SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING', 'java', null, 'SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING', null, null, null, null, 'The code creates an SQL prepared statement from a nonconstant String.If unchecked, tainted data from a user is used in building this String, SQL injection could be used to make the prepared statement do something unexpected and undesirable.', 'A prepared statement is generated from a nonconstant String', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING'),
 'STANDARD', 'CWE','89')
ON CONFLICT DO NOTHING;

-- ------------------------
--XSS_REQUEST_PARAMETER_TO_JSP_WRITER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'XSS_REQUEST_PARAMETER_TO_JSP_WRITER', null, 'XSS_REQUEST_PARAMETER_TO_JSP_WRITER', 'java', null, 'XSS_REQUEST_PARAMETER_TO_JSP_WRITER', null, null, null, null, 'This code directly writes an HTTP parameter to JSP output, which allows for a cross site scripting vulnerability. See <a href="http://en.wikipedia.org/wiki/Cross-site_scripting">http://en.wikipedia.org/wiki/Cross-site_scripting</a>for more information.SpotBugs looks only for the most blatant, obvious cases of cross site scripting.If SpotBugs found <em>any</em>, you <em>almost certainly</em> have more cross site scripting vulnerabilities that SpotBugs doesn''t report. If you are concerned about cross site scripting, you should seriously consider using a commercial static analysis or pen-testing tool.', 'JSP reflected cross site scripting vulnerability', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='XSS_REQUEST_PARAMETER_TO_JSP_WRITER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='XSS_REQUEST_PARAMETER_TO_JSP_WRITER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
--XSS_REQUEST_PARAMETER_TO_SEND_ERROR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'XSS_REQUEST_PARAMETER_TO_SEND_ERROR', null, 'XSS_REQUEST_PARAMETER_TO_SEND_ERROR', 'java', null, 'XSS_REQUEST_PARAMETER_TO_SEND_ERROR', null, null, null, null, 'This code directly writes an HTTP parameter to a Server error page (using HttpServletResponse.sendError). Echoing this untrusted input allows for a reflected cross site scripting vulnerability. See <a href="http://en.wikipedia.org/wiki/Cross-site_scripting">http://en.wikipedia.org/wiki/Cross-site_scripting</a>for more information.SpotBugs looks only for the most blatant, obvious cases of cross site scripting.If SpotBugs found <em>any</em>, you <em>almost certainly</em> have more cross site scripting vulnerabilities that SpotBugs doesn''t report. If you are concerned about cross site scripting, you should seriously consider using a commercial static analysis or pen-testing tool.', 'Servlet reflected cross site scripting vulnerability in error page', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='XSS_REQUEST_PARAMETER_TO_SEND_ERROR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='XSS_REQUEST_PARAMETER_TO_SEND_ERROR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='XSS_REQUEST_PARAMETER_TO_SEND_ERROR'),
 'STANDARD', 'CWE','81')
ON CONFLICT DO NOTHING;


-- ------------------------
--XSS_REQUEST_PARAMETER_TO_SERVLET_WRITER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'SECURITY', 'XSS_REQUEST_PARAMETER_TO_SERVLET_WRITER', null, 'XSS_REQUEST_PARAMETER_TO_SERVLET_WRITER', 'java', null, 'XSS_REQUEST_PARAMETER_TO_SERVLET_WRITER', null, null, null, null, 'This code directly writes an HTTP parameter to Servlet output, which allows for a reflected cross site scripting vulnerability. See <a href="http://en.wikipedia.org/wiki/Cross-site_scripting">http://en.wikipedia.org/wiki/Cross-site_scripting</a>for more information.SpotBugs looks only for the most blatant, obvious cases of cross site scripting.If SpotBugs found <em>any</em>, you <em>almost certainly</em> have more cross site scripting vulnerabilities that SpotBugs doesn''t report. If you are concerned about cross site scripting, you should seriously consider using a commercial static analysis or pen-testing tool.', 'Servlet reflected cross site scripting vulnerability', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='XSS_REQUEST_PARAMETER_TO_SERVLET_WRITER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='XSS_REQUEST_PARAMETER_TO_SERVLET_WRITER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

