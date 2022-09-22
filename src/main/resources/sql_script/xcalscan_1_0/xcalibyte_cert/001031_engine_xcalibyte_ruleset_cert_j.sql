insert into xcalibyte.scan_engine (name, version, revision, description, language, url, provider, provider_url, license, license_url, created_by, modified_by)
 values
('Xcalibyte', '1', '1.0', 'Xcalibyte static analyzer', 'c,c++,java', 'http://www.xcalibyte.com', 'Xcalibyte', 'http://www.xcalibyte.com', 'Xcalibyte commercial license', '', 'system', 'system')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_set
 (scan_engine_id, name, version, revision, display_name, description, language, url, provider, provider_url, license, license_url, created_by, modified_by)
 values
 ((select id from xcalibyte."scan_engine" where name = 'Xcalibyte' and version ='1'), 'CERT', '1', '1.0', 'CERT', 'CERT ruleset', 'c,c++,java', 'CMU SEI CERT Coding Standards can be found at https://wiki.sei.cmu.edu/confluence/display/seccode/SEI+CERT+Coding+Standards Implemented by Xcalibyte', 'Xcalibyte', 'http://www.xcalibyte.com', 'Xcalibyte commercial license', '', 'system', 'system')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV01-J', null, 'ENV01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV01-J.+Place+all+security-sensitive+code+in+a+single+JAR+and+sign+and+seal+it', '${rule.Xcalibyte.CERT.1.ENV01-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.ENV01-J.detail}', '${rule.Xcalibyte.CERT.1.ENV01-J.description}', '${rule.Xcalibyte.CERT.1.ENV01-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV02-J', null, 'ENV02-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV02-J.+Do+not+trust+the+values+of+environment+variables', '${rule.Xcalibyte.CERT.1.ENV02-J.name}', '3', '3', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.ENV02-J.detail}', '${rule.Xcalibyte.CERT.1.ENV02-J.description}', '${rule.Xcalibyte.CERT.1.ENV02-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV03-J', null, 'ENV03-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV03-J.+Do+not+grant+dangerous+combinations+of+permissions', '${rule.Xcalibyte.CERT.1.ENV03-J.name}', '1', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.ENV03-J.detail}', '${rule.Xcalibyte.CERT.1.ENV03-J.description}', '${rule.Xcalibyte.CERT.1.ENV03-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV04-J', null, 'ENV04-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV04-J.+Do+not+disable+bytecode+verification', '${rule.Xcalibyte.CERT.1.ENV04-J.name}', '1', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.ENV04-J.detail}', '${rule.Xcalibyte.CERT.1.ENV04-J.description}', '${rule.Xcalibyte.CERT.1.ENV04-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV05-J', null, 'ENV05-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV05-J.+Do+not+deploy+an+application+that+can+be+remotely+monitored', '${rule.Xcalibyte.CERT.1.ENV05-J.name}', '1', '1', 'PROBABLE', 'LOW', '${rule.Xcalibyte.CERT.1.ENV05-J.detail}', '${rule.Xcalibyte.CERT.1.ENV05-J.description}', '${rule.Xcalibyte.CERT.1.ENV05-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV06-J', null, 'ENV06-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV06-J.+Production+code+must+not+contain+debugging+entry+points', '${rule.Xcalibyte.CERT.1.ENV06-J.name}', '1', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.ENV06-J.detail}', '${rule.Xcalibyte.CERT.1.ENV06-J.description}', '${rule.Xcalibyte.CERT.1.ENV06-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ERR08-J', null, 'ERR08-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ERR08-J.+Do+not+catch+NullPointerException+or+any+of+its+ancestors', '${rule.Xcalibyte.CERT.1.ERR08-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.ERR08-J.detail}', '${rule.Xcalibyte.CERT.1.ERR08-J.description}', '${rule.Xcalibyte.CERT.1.ERR08-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'EXP02-J', null, 'EXP02-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/EXP02-J.+Do+not+use+the+Object.equals%28%29+method+to+compare+two+arrays', '${rule.Xcalibyte.CERT.1.EXP02-J.name}', '3', '2', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.EXP02-J.detail}', '${rule.Xcalibyte.CERT.1.EXP02-J.description}', '${rule.Xcalibyte.CERT.1.EXP02-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO02-J', null, 'FIO02-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/FIO02-J.+Detect+and+handle+file-related+errors', '${rule.Xcalibyte.CERT.1.FIO02-J.name}', '2', '2', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO02-J.detail}', '${rule.Xcalibyte.CERT.1.FIO02-J.description}', '${rule.Xcalibyte.CERT.1.FIO02-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO05-J', null, 'FIO05-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/FIO05-J.+Do+not+expose+buffers+created+using+the+wrap%28%29+or+duplicate%28%29+methods+to+untrusted+code', '${rule.Xcalibyte.CERT.1.FIO05-J.name}', '2', '2', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.FIO05-J.detail}', '${rule.Xcalibyte.CERT.1.FIO05-J.description}', '${rule.Xcalibyte.CERT.1.FIO05-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO08-J', null, 'FIO08-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/FIO08-J.+Distinguish+between+characters+or+bytes+read+from+a+stream+and+-1', '${rule.Xcalibyte.CERT.1.FIO08-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO08-J.detail}', '${rule.Xcalibyte.CERT.1.FIO08-J.description}', '${rule.Xcalibyte.CERT.1.FIO08-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO14-J', null, 'FIO14-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/FIO14-J.+Perform+proper+cleanup+at+program+termination', '${rule.Xcalibyte.CERT.1.FIO14-J.name}', '2', '2', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO14-J.detail}', '${rule.Xcalibyte.CERT.1.FIO14-J.description}', '${rule.Xcalibyte.CERT.1.FIO14-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO16-J', null, 'FIO16-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS17-J.+Prevent+XML+External+Entity+Attacks', '${rule.Xcalibyte.CERT.1.FIO16-J.name}', '2', '2', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO16-J.detail}', '${rule.Xcalibyte.CERT.1.FIO16-J.description}', '${rule.Xcalibyte.CERT.1.FIO16-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO51-J', null, 'FIO51-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/FIO51-J.+Identify+files+using+multiple+file+attributes', '${rule.Xcalibyte.CERT.1.FIO51-J.name}', '2', '2', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO51-J.detail}', '${rule.Xcalibyte.CERT.1.FIO51-J.description}', '${rule.Xcalibyte.CERT.1.FIO51-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO52-J', null, 'FIO52-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS17-J.+Prevent+XML+External+Entity+Attacks', '${rule.Xcalibyte.CERT.1.FIO52-J.name}', '2', '3', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO52-J.detail}', '${rule.Xcalibyte.CERT.1.FIO52-J.description}', '${rule.Xcalibyte.CERT.1.FIO52-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS01-J', null, 'IDS01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS01-J.+Normalize+strings+before+validating+them', '${rule.Xcalibyte.CERT.1.IDS01-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS01-J.detail}', '${rule.Xcalibyte.CERT.1.IDS01-J.description}', '${rule.Xcalibyte.CERT.1.IDS01-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS07-J', null, 'IDS07-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS07-J.+Sanitize+untrusted+data+passed+to+the+Runtime.exec%28%29+method', '${rule.Xcalibyte.CERT.1.IDS07-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS07-J.detail}', '${rule.Xcalibyte.CERT.1.IDS07-J.description}', '${rule.Xcalibyte.CERT.1.IDS07-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS11-J', null, 'IDS11-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS11-J.+Perform+any+string+modifications+before+validation', '${rule.Xcalibyte.CERT.1.IDS11-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS11-J.detail}', '${rule.Xcalibyte.CERT.1.IDS11-J.description}', '${rule.Xcalibyte.CERT.1.IDS11-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS15-J', null, 'IDS15-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS15-J.+Do+not+allow+sensitive+information+to+leak+outside+a+trust+boundary', '${rule.Xcalibyte.CERT.1.IDS15-J.name}', '2', '2', 'LIKELY', 'HIGH', '${rule.Xcalibyte.CERT.1.IDS15-J.detail}', '${rule.Xcalibyte.CERT.1.IDS15-J.description}', '${rule.Xcalibyte.CERT.1.IDS15-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS16-J', null, 'IDS16-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS16-J.+Prevent+XML+Injection', '${rule.Xcalibyte.CERT.1.IDS16-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS16-J.detail}', '${rule.Xcalibyte.CERT.1.IDS16-J.description}', '${rule.Xcalibyte.CERT.1.IDS16-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS17-J', null, 'IDS17-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS17-J.+Prevent+XML+External+Entity+Attacks', '${rule.Xcalibyte.CERT.1.IDS17-J.name}', '2', '2', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS17-J.detail}', '${rule.Xcalibyte.CERT.1.IDS17-J.description}', '${rule.Xcalibyte.CERT.1.IDS17-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'JNI01-J', null, 'JNI01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/pages/viewpage.action?pageId=88487334', '${rule.Xcalibyte.CERT.1.JNI01-J.name}', '1', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.JNI01-J.detail}', '${rule.Xcalibyte.CERT.1.JNI01-J.description}', '${rule.Xcalibyte.CERT.1.JNI01-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MET06-J', null, 'MET06-J', 'java', 'https://wiki.sei.cmu.edu/confluence/pages/viewpage.action?pageId=88487921', '${rule.Xcalibyte.CERT.1.MET06-J.name}', '2', '2', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.MET06-J.detail}', '${rule.Xcalibyte.CERT.1.MET06-J.description}', '${rule.Xcalibyte.CERT.1.MET06-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC02-J', null, 'MSC02-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/MSC02-J.+Generate+strong+random+numbers', '${rule.Xcalibyte.CERT.1.MSC02-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.MSC02-J.detail}', '${rule.Xcalibyte.CERT.1.MSC02-J.description}', '${rule.Xcalibyte.CERT.1.MSC02-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC03-J', null, 'MSC03-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/MSC03-J.+Never+hard+code+sensitive+information', '${rule.Xcalibyte.CERT.1.MSC03-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.MSC03-J.detail}', '${rule.Xcalibyte.CERT.1.MSC03-J.description}', '${rule.Xcalibyte.CERT.1.MSC03-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC61-J', null, 'MSC61-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/MSC61-J.+Do+not+use+insecure+or+weak+cryptographic+algorithms', '${rule.Xcalibyte.CERT.1.MSC61-J.name}', '2', '3', 'PROBABLE', 'HIGH', '${rule.Xcalibyte.CERT.1.MSC61-J.detail}', '${rule.Xcalibyte.CERT.1.MSC61-J.description}', '${rule.Xcalibyte.CERT.1.MSC61-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'OBJ01-J', null, 'OBJ01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/OBJ01-J.+Limit+accessibility+of+fields', '${rule.Xcalibyte.CERT.1.OBJ01-J.name}', '2', '2', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.OBJ01-J.detail}', '${rule.Xcalibyte.CERT.1.OBJ01-J.description}', '${rule.Xcalibyte.CERT.1.OBJ01-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'OBJ05-J', null, 'OBJ05-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/OBJ05-J.+Do+not+return+references+to+private+mutable+class+members', '${rule.Xcalibyte.CERT.1.OBJ05-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.OBJ05-J.detail}', '${rule.Xcalibyte.CERT.1.OBJ05-J.description}', '${rule.Xcalibyte.CERT.1.OBJ05-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'OBJ11-J', null, 'OBJ11-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/OBJ11-J.+Be+wary+of+letting+constructors+throw+exceptions', '${rule.Xcalibyte.CERT.1.OBJ11-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.OBJ11-J.detail}', '${rule.Xcalibyte.CERT.1.OBJ11-J.description}', '${rule.Xcalibyte.CERT.1.OBJ11-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'OBJ13-J', null, 'OBJ13-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/OBJ13-J.+Ensure+that+references+to+mutable+objects+are+not+exposed', '${rule.Xcalibyte.CERT.1.OBJ13-J.name}', '2', '2', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.OBJ13-J.detail}', '${rule.Xcalibyte.CERT.1.OBJ13-J.description}', '${rule.Xcalibyte.CERT.1.OBJ13-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC01-J', null, 'SEC01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC01-J.+Do+not+allow+tainted+variables+in+privileged+blocks', '${rule.Xcalibyte.CERT.1.SEC01-J.name}', '1', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.SEC01-J.detail}', '${rule.Xcalibyte.CERT.1.SEC01-J.description}', '${rule.Xcalibyte.CERT.1.SEC01-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC02-J', null, 'SEC02-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC02-J.+Do+not+base+security+checks+on+untrusted+sources', '${rule.Xcalibyte.CERT.1.SEC02-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SEC02-J.detail}', '${rule.Xcalibyte.CERT.1.SEC02-J.description}', '${rule.Xcalibyte.CERT.1.SEC02-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC03-J', null, 'SEC03-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC03-J.+Do+not+load+trusted+classes+after+allowing+untrusted+code+to+load+arbitrary+classes', '${rule.Xcalibyte.CERT.1.SEC03-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SEC03-J.detail}', '${rule.Xcalibyte.CERT.1.SEC03-J.description}', '${rule.Xcalibyte.CERT.1.SEC03-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC04-J', null, 'SEC04-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC04-J.+Protect+sensitive+operations+with+security+manager+checks', '${rule.Xcalibyte.CERT.1.SEC04-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SEC04-J.detail}', '${rule.Xcalibyte.CERT.1.SEC04-J.description}', '${rule.Xcalibyte.CERT.1.SEC04-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC05-J', null, 'SEC05-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC05-J.+Do+not+use+reflection+to+increase+accessibility+of+classes%2C+methods%2C+or+fields', '${rule.Xcalibyte.CERT.1.SEC05-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SEC05-J.detail}', '${rule.Xcalibyte.CERT.1.SEC05-J.description}', '${rule.Xcalibyte.CERT.1.SEC05-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC06-J', null, 'SEC06-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC06-J.+Do+not+rely+on+the+default+automatic+signature+verification+provided+by+URLClassLoader+and+java.util.jar', '${rule.Xcalibyte.CERT.1.SEC06-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SEC06-J.detail}', '${rule.Xcalibyte.CERT.1.SEC06-J.description}', '${rule.Xcalibyte.CERT.1.SEC06-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC07-J', null, 'SEC07-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC07-J.+Call+the+superclass%27s+getPermissions%28%29+method+when+writing+a+custom+class+loader', '${rule.Xcalibyte.CERT.1.SEC07-J.name}', '1', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.SEC07-J.detail}', '${rule.Xcalibyte.CERT.1.SEC07-J.description}', '${rule.Xcalibyte.CERT.1.SEC07-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SER01-J', null, 'SER01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SER01-J.+Do+not+deviate+from+the+proper+signatures+of+serialization+methods', '${rule.Xcalibyte.CERT.1.SER01-J.name}', '1', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.SER01-J.detail}', '${rule.Xcalibyte.CERT.1.SER01-J.description}', '${rule.Xcalibyte.CERT.1.SER01-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SER04-J', null, 'SER04-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SER04-J.+Do+not+allow+serialization+and+deserialization+to+bypass+the+security+manager', '${rule.Xcalibyte.CERT.1.SER04-J.name}', '1', '2', 'PROBABLE', 'HIGH', '${rule.Xcalibyte.CERT.1.SER04-J.detail}', '${rule.Xcalibyte.CERT.1.SER04-J.description}', '${rule.Xcalibyte.CERT.1.SER04-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SER05-J', null, 'SER05-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SER05-J.+Do+not+serialize+instances+of+inner+classes', '${rule.Xcalibyte.CERT.1.SER05-J.name}', '2', '2', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SER05-J.detail}', '${rule.Xcalibyte.CERT.1.SER05-J.description}', '${rule.Xcalibyte.CERT.1.SER05-J.msg_template}', 'system', 'system'),
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SER08-J', null, 'SER08-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SER08-J.+Minimize+privileges+before+deserializing+from+a+privileged+context', '${rule.Xcalibyte.CERT.1.SER08-J.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SER08-J.detail}', '${rule.Xcalibyte.CERT.1.SER08-J.description}', '${rule.Xcalibyte.CERT.1.SER08-J.msg_template}', 'system', 'system')
ON CONFLICT DO NOTHING;