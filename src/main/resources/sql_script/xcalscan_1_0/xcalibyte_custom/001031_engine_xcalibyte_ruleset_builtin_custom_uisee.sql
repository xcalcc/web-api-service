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
-- CRF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'CRF', 'CRF', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.CRF.name}', '2', '2', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.CRF.detail}', '${rule.Xcalibyte.BUILTIN.1.CRF.description}', '${rule.Xcalibyte.BUILTIN.1.CRF.msg_template}', 'system', 'system')
ON CONFLICT DO NOTHING;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.description', 'The program has a call sequence that results in recursion at runtime', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.description', '?????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.detail', '### CRF'||chr(10)||'#### Abstract'||chr(10)||'The program has a call sequence that results in recursion at runtime'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Recursion happens when a function, say A calls another function in such a way that the call sequence eventually calls A again. In its most simple form, a function simply calls itself during execution. If not programed correctly, this could lead to infinite loop. It could also causes excessive use of stack space and may lead to run out of memory or stack space problem.'||chr(10)||''||chr(10)||'#### Example 1'||chr(10)||'````text'||chr(10)||'// Most simple form of recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will recurse and get into infinite loop'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||'int func_recurse(int* p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_recurse(p, 2);  // calls itself directly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example 2'||chr(10)||'````text'||chr(10)||'// Indirect recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will call func_recurse inside func_b'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||''||chr(10)||'int func_b(int *q, int j)'||chr(10)||'{'||chr(10)||'  if (q != 0) {'||chr(10)||'    return func_recurse(q, 5);'||chr(10)||'  }'||chr(10)||'  else'||chr(10)||'    return 5;'||chr(10)||'}'||chr(10)||''||chr(10)||'int func_recurse(int *p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_b(p, i);  // calls func_recurse indirectly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.detail', '### CRF'||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'???????????????????????????A????????????????????????????????????????????????????????????????????????A????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? 1'||chr(10)||'````text'||chr(10)||'// Most simple form of recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will recurse and get into infinite loop'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||'int func_recurse(int* p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_recurse(p, 2);  // calls itself directly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### ?????? 2'||chr(10)||'````text'||chr(10)||'// Indirect recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will call func_recurse inside func_b'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||''||chr(10)||'int func_b(int *q, int j)'||chr(10)||'{'||chr(10)||'  if (q != 0) {'||chr(10)||'    return func_recurse(q, 5);'||chr(10)||'  }'||chr(10)||'  else'||chr(10)||'    return 5;'||chr(10)||'}'||chr(10)||''||chr(10)||'int func_recurse(int *p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_b(p, i);  // calls func_recurse indirectly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} is calling itself. The function declaration is in ${ss.filename}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.msg_template', '???${s2.filename}???${se.line}??????????????? ${s2.func}?????????????????????????????????${ss.filename}??????${ss.line}??????', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.name', 'Use recursive function', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.name', '?????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CRF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CRF'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CSL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'CSL', 'CSL', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.CSL.name}', '2', '2', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.CSL.detail}', '${rule.Xcalibyte.BUILTIN.1.CSL.description}', '${rule.Xcalibyte.BUILTIN.1.CSL.msg_template}', 'system', 'system')
ON CONFLICT DO NOTHING;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.description', 'The program has a call sequence that causes the runtime stack to exceeds call depth limit set in Xcalscan using Xcalibyte rules APIs', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.description', '??????????????????????????????, ???????????????????????????.', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.detail', '### CSL'||chr(10)||'#### Abstract'||chr(10)||'The program has a call sequence that causes the runtime stack to exceeds call depth limit set in Xcalscan using Xcalibyte rules APIs'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'A function X calls another function Y. Function Y may in turn calls another function. This call chain can go on indefinitely. In embedded systems, when the call stack is too deep, it may cause unintended side effects like run out of memory, inefficient execution time etc.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// This case does not need an example.'||chr(10)||'// The complete call level from function A to Z i.e. A() ==> B() ==> .... ==> Z(),  is too deep'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.detail', '### CSL'||chr(10)||'#### ??????'||chr(10)||'??????????????????????????????, ???????????????????????????.'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'?????????A????????????????????????B???, B??????????????????C??????,?????? . ??????????????????????????????????????????????????????????????????????????????????????????.'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'// This case does not need an example.'||chr(10)||'// The complete call level from function A to Z i.e. A() ==> B() ==> .... ==> Z(),  is too deep'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} has a call sequence that exceeds call level limit set at scan configure set up time.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.msg_template', '???${se.filename}???${se.line}??????????????? ${se.func}??????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.name', 'Call stack level exit limit', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.name', '????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CSL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CSL'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CSS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'CSS', 'CSS', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.CSS.name}', '2', '2', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.CSS.detail}', '${rule.Xcalibyte.BUILTIN.1.CSS.description}', '${rule.Xcalibyte.BUILTIN.1.CSS.msg_template}', 'system', 'system')
ON CONFLICT DO NOTHING;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.description', 'The program has a call sequence that causes the runtime stack size to exceeds limit set in Xcalscan using Xcalibyte rules APIs', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.description', '????????????????????????, ????????????????????????????????????????????????.', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.detail', '### CSS'||chr(10)||'#### Abstract'||chr(10)||'The program has a call sequence that causes the runtime stack size to exceeds limit set in Xcalscan using Xcalibyte rules APIs'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'When function A calls another function B, parameters passed to the called function and return value from the called function to the caller function will be placed on the execution stack. Local variables will also be placed on the execution stack. The users has used Xcalibyte provided APIs create a customer scan rule that warns of such occurence.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'// stack size is 8 bytes for parameter, 4 bytes for return value (assume) 32 bit ABI'||chr(10)||'int func_callee(int* a, int i)'||chr(10)||'{'||chr(10)||'  return a[i];'||chr(10)||'}'||chr(10)||''||chr(10)||'// stack size is 0 byte (no parameter), 12 byte for local variable, 4 bytes for return value'||chr(10)||'int func_caller() {'||chr(10)||'  int a[3] = {0, 1}, b;'||chr(10)||'  b = assign(a, 1);  /* callee stack size if 12 bytes'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'// total stack size for the call sequence func_caller -> func_callee is 28 bytes'||chr(10)||'// (assume ABI specifies all parameters uses stack and not register)'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.detail', '### CSS'||chr(10)||'#### ??????'||chr(10)||'????????????????????????, ????????????????????????????????????????????????.'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'?????????A????????????????????????B????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????API???????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||'// stack size is 8 bytes for parameter, 4 bytes for return value (assume) 32 bit ABI'||chr(10)||'int func_callee(int* a, int i)'||chr(10)||'{'||chr(10)||'  return a[i];'||chr(10)||'}'||chr(10)||''||chr(10)||'// stack size is 0 byte (no parameter), 12 byte for local variable, 4 bytes for return value'||chr(10)||'int func_caller() {'||chr(10)||'  int a[3] = {0, 1}, b;'||chr(10)||'  b = assign(a, 1);  /* callee stack size if 12 bytes'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'// total stack size for the call sequence func_caller -> func_callee is 28 bytes'||chr(10)||'// (assume ABI specifies all parameters uses stack and not register)'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} has a call sequence that exceeds stack size limit set at scan configure set up time.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.msg_template', '???${se.filename}???${se.line}??????????????? ${se.func}?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.name', 'Call stack size exit limit', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.name', '????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CSS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CSS'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;