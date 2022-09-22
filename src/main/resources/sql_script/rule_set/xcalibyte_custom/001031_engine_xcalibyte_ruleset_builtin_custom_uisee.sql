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
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'CRF', 'CRF', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.CRF.name}', 2, 1, 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.CRF.detail}', '${rule.Xcalibyte.BUILTIN.1.CRF.description}', '${rule.Xcalibyte.BUILTIN.1.CRF.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.description', 'The program has a call sequence that results in recursion at runtime', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.description', '该程序有调用序列，它在运行时造成了递归', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.detail', '### CRF'||chr(10)||'#### Abstract'||chr(10)||'The program has a call sequence that results in recursion at runtime'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Recursion happens when a function, say A calls another function in such a way that the call sequence eventually calls A again. In its most simple form, a function simply calls itself during execution. If not programed correctly, this could lead to infinite loop. It could also causes excessive use of stack space and may lead to run out of memory or stack space problem.'||chr(10)||''||chr(10)||'#### Example 1'||chr(10)||'````text'||chr(10)||'// Most simple form of recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will recurse and get into infinite loop'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||'int func_recurse(int* p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_recurse(p, 2);  // calls itself directly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example 2'||chr(10)||'````text'||chr(10)||'// Indirect recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will call func_recurse inside func_b'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||''||chr(10)||'int func_b(int *q, int j)'||chr(10)||'{'||chr(10)||'  if (q != 0) {'||chr(10)||'    return func_recurse(q, 5);'||chr(10)||'  }'||chr(10)||'  else'||chr(10)||'    return 5;'||chr(10)||'}'||chr(10)||''||chr(10)||'int func_recurse(int *p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_b(p, i);  // calls func_recurse indirectly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.detail', '### CRF'||chr(10)||'#### 概要'||chr(10)||'该程序有调用序列，它在运行时造成了递归'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'当一个函数，假定是A，如此调用另外一个函数以至于调用序列最终再次调用A时，会发生递归。其最简单的形式是函数在执行期间就调用自身。如果不正确地编程，这可能会导致无限循环。它还会造成对栈空间的过分使用，并可能导致内存不足或栈空间问题。'||chr(10)||''||chr(10)||'#### 示例 1'||chr(10)||'````text'||chr(10)||'// Most simple form of recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will recurse and get into infinite loop'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||'int func_recurse(int* p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_recurse(p, 2);  // calls itself directly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 2'||chr(10)||'````text'||chr(10)||'// Indirect recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will call func_recurse inside func_b'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||''||chr(10)||'int func_b(int *q, int j)'||chr(10)||'{'||chr(10)||'  if (q != 0) {'||chr(10)||'    return func_recurse(q, 5);'||chr(10)||'  }'||chr(10)||'  else'||chr(10)||'    return 5;'||chr(10)||'}'||chr(10)||''||chr(10)||'int func_recurse(int *p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_b(p, i);  // calls func_recurse indirectly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} is calling itself. The function declaration is in ${ss.filename}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.msg_template', '在${s2.filename}，${se.line}行上，函数 ${s2.func}在调用自身。函数声明在${ss.filename}，第${ss.line}行。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.name', 'Use recursive function', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.name', '使用了递归函数', 'system', 'system')
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

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CRF'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CRF'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CRF'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

-- ------------------------
-- CSL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'CSL', 'CSL', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.CSL.name}', 2, 1, 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.CSL.detail}', '${rule.Xcalibyte.BUILTIN.1.CSL.description}', '${rule.Xcalibyte.BUILTIN.1.CSL.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.description', 'The program has a call sequence that causes the runtime stack to exceeds call depth limit set in Xcalscan using Xcalibyte rules APIs', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.description', '该程序有调用序列太深, 超出用户设置的上限.', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.detail', '### CSL'||chr(10)||'#### Abstract'||chr(10)||'The program has a call sequence that causes the runtime stack to exceeds call depth limit set in Xcalscan using Xcalibyte rules APIs'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'A function X calls another function Y. Function Y may in turn calls another function. This call chain can go on indefinitely. In embedded systems, when the call stack is too deep, it may cause unintended side effects like run out of memory, inefficient execution time etc.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// This case does not need an example.'||chr(10)||'// The complete call level from function A to Z i.e. A() ==> B() ==> .... ==> Z(),  is too deep'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.detail', '### CSL'||chr(10)||'#### 概要'||chr(10)||'该程序有调用序列太深, 超出用户设置的上限.'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'当函数A调用另外一个函数B时, B同时也会调用C函数,等等 . 调用链太深可能会导致意外的副作用，如内存不足、执行时间不足等.'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||''||chr(10)||'// This case does not need an example.'||chr(10)||'// The complete call level from function A to Z i.e. A() ==> B() ==> .... ==> Z(),  is too deep'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} has a call sequence that exceeds call level limit set at scan configure set up time.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.msg_template', '在${se.filename}，${se.line}行上，函数 ${se.func}有一个调用序列，该调用序列超过了在扫描配置设置时设置的上限。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.name', 'Call stack level exit limit', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.name', '调用栈的深度超於设置上限', 'system', 'system')
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

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CSL'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CSL'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CSL'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

-- ------------------------
-- CSS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'CSS', 'CSS', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.CSS.name}', 2, 1, 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.CSS.detail}', '${rule.Xcalibyte.BUILTIN.1.CSS.description}', '${rule.Xcalibyte.BUILTIN.1.CSS.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.description', 'The program has a call sequence that causes the runtime stack size to exceeds limit set in Xcalscan using Xcalibyte rules APIs', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.description', '该程序有调用序列, 会致使运行时栈內存超出设置的上限.', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.detail', '### CSS'||chr(10)||'#### Abstract'||chr(10)||'The program has a call sequence that causes the runtime stack size to exceeds limit set in Xcalscan using Xcalibyte rules APIs'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'When function A calls another function B, parameters passed to the called function and return value from the called function to the caller function will be placed on the execution stack. Local variables will also be placed on the execution stack. The users has used Xcalibyte provided APIs create a customer scan rule that warns of such occurence.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'// stack size is 8 bytes for parameter, 4 bytes for return value (assume) 32 bit ABI'||chr(10)||'int func_callee(int* a, int i)'||chr(10)||'{'||chr(10)||'  return a[i];'||chr(10)||'}'||chr(10)||''||chr(10)||'// stack size is 0 byte (no parameter), 12 byte for local variable, 4 bytes for return value'||chr(10)||'int func_caller() {'||chr(10)||'  int a[3] = {0, 1}, b;'||chr(10)||'  b = assign(a, 1);  /* callee stack size if 12 bytes'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'// total stack size for the call sequence func_caller -> func_callee is 28 bytes'||chr(10)||'// (assume ABI specifies all parameters uses stack and not register)'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.detail', '### CSS'||chr(10)||'#### 概要'||chr(10)||'该程序有调用序列, 会致使运行时栈內存超出设置的上限.'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'当函数A调用另外一个函数B时，传递给被调用函数的参数和被调用函数给调用函数的返回值将被放置在执行栈上。局部变量也会被放置在执行栈上。用户已使用了鉴释提供的API创建了自定义扫描规则的上限，它对此类事件发出了警告'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||'// stack size is 8 bytes for parameter, 4 bytes for return value (assume) 32 bit ABI'||chr(10)||'int func_callee(int* a, int i)'||chr(10)||'{'||chr(10)||'  return a[i];'||chr(10)||'}'||chr(10)||''||chr(10)||'// stack size is 0 byte (no parameter), 12 byte for local variable, 4 bytes for return value'||chr(10)||'int func_caller() {'||chr(10)||'  int a[3] = {0, 1}, b;'||chr(10)||'  b = assign(a, 1);  /* callee stack size if 12 bytes'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'// total stack size for the call sequence func_caller -> func_callee is 28 bytes'||chr(10)||'// (assume ABI specifies all parameters uses stack and not register)'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} has a call sequence that exceeds stack size limit set at scan configure set up time.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.msg_template', '在${se.filename}，${se.line}行上，函数 ${se.func}有一个调用序列，该调用序列会致使运行时栈內存超过了在扫描配置设置时设置的上限。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.name', 'Call stack size exit limit', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.name', '调用栈的內存超於设置上限', 'system', 'system')
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

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CSS'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CSS'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='CSS'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

-- ------------------------
-- DDC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'DDC', 'DDC', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.DDC.name}', 3, 2, 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.DDC.detail}', '${rule.Xcalibyte.BUILTIN.1.DDC.description}', '${rule.Xcalibyte.BUILTIN.1.DDC.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.DDC.description', 'Unreachable code after a jump statement', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DDC.description', '跳转语句后有无法访问的代码', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DDC.detail', '#### Abstract'||chr(10)||'Unreachable code after a jump statement'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Execution will never reach statements that come immediately after a jump statement or function calls that do not return. This is could be due unintended edits and is a code quality issue.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'void abort();'||chr(10)||'void exit();'||chr(10)||'int foo();'||chr(10)||''||chr(10)||'int f1() {'||chr(10)||'  abort();'||chr(10)||'  foo();    // unreachable'||chr(10)||'}'||chr(10)||'int f2() {'||chr(10)||'  exit();'||chr(10)||'  foo();    // unreachable'||chr(10)||'}'||chr(10)||''||chr(10)||'int f3() {'||chr(10)||'  foo();'||chr(10)||'  goto L;'||chr(10)||'  foo();    // unreachable'||chr(10)||'L:'||chr(10)||'  foo();'||chr(10)||'}'||chr(10)||''||chr(10)||'int f4(int x) {'||chr(10)||'  foo();'||chr(10)||'  goto L;'||chr(10)||'  if (x>5) {    // unreachable'||chr(10)||'L:'||chr(10)||'    foo();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DDC.detail', '#### 概要'||chr(10)||'跳转语句后有无法访问的代码'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||''||chr(10)||'达跳转语句后面是永远不会执行的. 不会返回的函数之后的语句也是。这可能是由于意外编辑，这是一个代码质量问题。'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||''||chr(10)||'````text'||chr(10)||'void abort();'||chr(10)||'void exit();'||chr(10)||'int foo();'||chr(10)||''||chr(10)||'int f1() {'||chr(10)||'  abort();'||chr(10)||'  foo();    // unreachable'||chr(10)||'}'||chr(10)||'int f2() {'||chr(10)||'  exit();'||chr(10)||'  foo();    // unreachable'||chr(10)||'}'||chr(10)||''||chr(10)||'int f3() {'||chr(10)||'  foo();'||chr(10)||'  goto L;'||chr(10)||'  foo();    // unreachable'||chr(10)||'L:'||chr(10)||'  foo();'||chr(10)||'}'||chr(10)||''||chr(10)||'int f4(int x) {'||chr(10)||'  foo();'||chr(10)||'  goto L;'||chr(10)||'  if (x>5) {    // unreachable'||chr(10)||'L:'||chr(10)||'    foo();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DDC.msg_template', 'In ${se.filename}, function ${se.func} the statement at line ${se.line} is unreachable during execution.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DDC.msg_template', '在${se.filename}，函数 ${se.func}, 第${se.line}行, 在执行期间是无法到达的。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DDC.name', 'Unreachable code after jump', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DDC.name', '跳转语句后有无法访问的代码', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDC'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDC'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDC'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDC'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- RCD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'RCD', 'RCD', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.RCD.name}', 3, 2, 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.RCD.detail}', '${rule.Xcalibyte.BUILTIN.1.RCD.description}', '${rule.Xcalibyte.BUILTIN.1.RCD.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.RCD.description', 'Redundant statements that are control dependent.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RCD.description', '冗余的控制语句', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RCD.detail', '#### Abstract'||chr(10)||'Redundant statements that are control dependent.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'When two conditional statements that are dependent in execution flow, one conditional can logically subsume the other conditional statement. In that case, the other conditional is redundant and unnecessary. This could be due to editing error and is a code quality issue.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'int foo();'||chr(10)||'int bar();'||chr(10)||''||chr(10)||'int f1(int x) {'||chr(10)||'  if (x>15 && x>16)    //  x>16 will subsume x>15'||chr(10)||'    foo();'||chr(10)||'}'||chr(10)||''||chr(10)||'int f2(int x) {'||chr(10)||'  if (x>16 && x>15)    //  x>16 implies x>15, hence x>15 is redundant'||chr(10)||'    foo();'||chr(10)||'}'||chr(10)||''||chr(10)||'int f3(int x) {'||chr(10)||'  if (x>16) {'||chr(10)||'    foo();'||chr(10)||'    if (x>15)    // x>16 implies >15, hence this check in unnecessary'||chr(10)||'      bar();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'int f4(int x) {'||chr(10)||'  if (x>15) {'||chr(10)||'    foo();'||chr(10)||'    if (x>16)    // x>15 does not imply x>16, this check is not redundant'||chr(10)||'      bar();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RCD.detail', '#### 概要'||chr(10)||'冗余的控制语句  '||chr(10)||''||chr(10)||'#### 解释'||chr(10)||''||chr(10)||''||chr(10)||'当两个条件语句在执行流中有直接依赖时，一个条件可以在逻辑上包含另一个条件语句。在这种情况下，另一个条件是多余和不必要的。'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||''||chr(10)||'````text'||chr(10)||'int foo();'||chr(10)||'int bar();'||chr(10)||''||chr(10)||'int f1(int x) {'||chr(10)||'  if (x>15 && x>16)    //  x>16 will subsume x>15'||chr(10)||'    foo();'||chr(10)||'}'||chr(10)||''||chr(10)||'int f2(int x) {'||chr(10)||'  if (x>16 && x>15)    //  x>16 implies x>15, hence x>15 is redundant'||chr(10)||'    foo();'||chr(10)||'}'||chr(10)||''||chr(10)||'int f3(int x) {'||chr(10)||'  if (x>16) {'||chr(10)||'    foo();'||chr(10)||'    if (x>15)    // x>16 implies >15, hence this check in unnecessary'||chr(10)||'      bar();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'int f4(int x) {'||chr(10)||'  if (x>15) {'||chr(10)||'    foo();'||chr(10)||'    if (x>16)    // x>15 does not imply x>16, this check is not redundant'||chr(10)||'      bar();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RCD.msg_template', 'In ${se.filename}, function ${se.func}, the statement at line ${se.line} is redundant due to statement at ${ss.filename}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RCD.msg_template', '因为在${ss.filename}, 第${ss.line}行的语句, 在${se.filename}，函数 ${se.func}, 第${se.line}行的语句是多余的.', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RCD.name', 'Redundant control dependency', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RCD.name', '冗余的控制语句', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RCD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RCD'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RCD'),
 'BASIC','PRIORITY','6'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RCD'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RCD'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RCD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- SCB
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'SCB', 'SCB', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.SCB.name}', 3, 2, 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.SCB.detail}', '${rule.Xcalibyte.BUILTIN.1.SCB.description}', '${rule.Xcalibyte.BUILTIN.1.SCB.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.SCB.description', 'Same code block inside conditional statements', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.SCB.description', '相同的代码块', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.SCB.detail', '#### Abstract'||chr(10)||'Same code block inside conditional statements'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Two code blocks are the same inside a conditional statement. This is likely due to error in editing. It is a code quality issue.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'int foo();'||chr(10)||'int a, b;'||chr(10)||''||chr(10)||'int f1(int x) {'||chr(10)||'  if (x)      // in this condition (if-then-else, block 1 and block 2 are the same. Effectively making the conditional statement useless)'||chr(10)||'    foo();    // block 1'||chr(10)||'  else'||chr(10)||'    foo();    // block 2'||chr(10)||'}'||chr(10)||''||chr(10)||'int f2(int x) {'||chr(10)||'  return x ? foo() : foo();  // similar code written differently'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.SCB.detail', '#### 概要'||chr(10)||'相同的代码块'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'条件语句中的两个代码块是相同的。这可能是由于编辑错误造成的。这是一个代码质量问题。'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||''||chr(10)||'````text'||chr(10)||'int foo();'||chr(10)||'int a, b;'||chr(10)||''||chr(10)||'int f1(int x) {'||chr(10)||'  if (x)      // in this condition (if-then-else, block 1 and block 2 are the same. Effectively making the conditional statement useless)'||chr(10)||'    foo();    // block 1'||chr(10)||'  else'||chr(10)||'    foo();    // block 2'||chr(10)||'}'||chr(10)||''||chr(10)||'int f2(int x) {'||chr(10)||'  return x ? foo() : foo();  // similar code written differently'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.SCB.msg_template', 'In ${se.filename}, function ${se.func} the code blocks after either side of the conditional statement at line ${se.line} are the same.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.SCB.msg_template', '在${se.filename}，函数 ${se.func}, 第${se.line}行的条件语句, 其两边后面的代码块是相同的。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.SCB.name', 'Same code block', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.SCB.name', '相同的代码块', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SCB');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SCB'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SCB'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SCB'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SCB'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SCB'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- SSE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'SSE', 'SSE', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.SSE.name}', 3, 2, 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.SSE.detail}', '${rule.Xcalibyte.BUILTIN.1.SSE.description}', '${rule.Xcalibyte.BUILTIN.1.SSE.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.SSE.description', 'The program has same sub-expressions.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.SSE.description', '程序有相同的子表达式', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.SSE.detail', '#### Abstract'||chr(10)||'The program has same sub-expressions.  '||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'The same expressions are found in a statement, where the value of variables in the expression has not changed in value. It could be due to typo in the program. This is a code quality issue.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'int foo();'||chr(10)||'int a, b;'||chr(10)||''||chr(10)||''||chr(10)||'int f3(int x, int *p) {'||chr(10)||'  if (*p && *p)        // same sub-expression'||chr(10)||'    foo();'||chr(10)||'}'||chr(10)||''||chr(10)||'int f4(int x, int y) {'||chr(10)||'  if ((x+y++) && (y+x)) // not same sub-expression due to y++'||chr(10)||'    foo();'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.SSE.detail', '#### 概要'||chr(10)||'程序有相同的子表达式'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||''||chr(10)||'在语句中有相同的表达式，其中表达式中变量的值也没有更改。 这可能是由于程序中的错字。 这是一个代码质量问题。'||chr(10)||''||chr(10)||'。'||chr(10)||'#### 示例'||chr(10)||''||chr(10)||'````text'||chr(10)||'int foo();'||chr(10)||'int a, b;'||chr(10)||''||chr(10)||''||chr(10)||'int f3(int x, int *p) {'||chr(10)||'  if (*p && *p)        // same sub-expression'||chr(10)||'    foo();'||chr(10)||'}'||chr(10)||''||chr(10)||'int f4(int x, int y) {'||chr(10)||'  if ((x+y++) && (y+x)) // not same sub-expression due to y++'||chr(10)||'    foo();'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.SSE.msg_template', 'In ${se.filename}, function ${se.func}, line ${se.line} has sub-expressions that in the conditional that are the same.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.SSE.msg_template', '在${se.filename}，函数 ${se.func}, 第${se.line}行, 条件语句中有相同的子表达式。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.SSE.name', 'Same sub-expression', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.SSE.name', '相同的子表达式', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SSE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SSE'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SSE'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SSE'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SSE'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='SSE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- UIC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'UIC', 'UIC', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.UIC.name}', 3, 2, 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.UIC.detail}', '${rule.Xcalibyte.BUILTIN.1.UIC.description}', '${rule.Xcalibyte.BUILTIN.1.UIC.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.UIC.description', 'A field of a class is not initialized in its constructor.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UIC.description', '构造函数中有未初始化字段', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UIC.detail', '#### Abstract'||chr(10)||'A field of a class is not initialized in its constructor.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Constructor of a class typically includes initialization of fields inside the class. When a field did not get initialized, it is likely due to editing error and is a code quality issue.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'class Construct_Init{'||chr(10)||'private:'||chr(10)||'  int a;'||chr(10)||'  int b;'||chr(10)||'public:'||chr(10)||'  Construct_Init();'||chr(10)||'  Construct_Init(int i): a(i) {};    // did not initialize b'||chr(10)||'  Construct_Init(int i, int j) { a = i; b = j; }'||chr(10)||''||chr(10)||'  void CI_Run() { printf("%d:%d\n", a, b); }'||chr(10)||'};'||chr(10)||''||chr(10)||'int main(int argc, char**argv)'||chr(10)||'{'||chr(10)||'  Construct_Init ci;'||chr(10)||'  ci.CI_Run();'||chr(10)||'  Construct_Init ci1(1);'||chr(10)||'  ci1.CI_Run();'||chr(10)||'  Construct_Init ci2(1,2);'||chr(10)||'  ci2.CI_Run();'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UIC.detail', '#### 概要'||chr(10)||'构造函数中有未初始化字段 '||chr(10)||''||chr(10)||'#### 解释'||chr(10)||''||chr(10)||'类的构造函数通常包括类内字段的初始化。如果字段没有初始化，很可能是由于编辑错误，这是一个代码质量问题。'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||''||chr(10)||'````text'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'class Construct_Init{'||chr(10)||'private:'||chr(10)||'  int a;'||chr(10)||'  int b;'||chr(10)||'public:'||chr(10)||'  Construct_Init();'||chr(10)||'  Construct_Init(int i): a(i) {};    // did not initialize b'||chr(10)||'  Construct_Init(int i, int j) { a = i; b = j; }'||chr(10)||''||chr(10)||'  void CI_Run() { printf("%d:%d\n", a, b); }'||chr(10)||'};'||chr(10)||''||chr(10)||'int main(int argc, char**argv)'||chr(10)||'{'||chr(10)||'  Construct_Init ci;'||chr(10)||'  ci.CI_Run();'||chr(10)||'  Construct_Init ci1(1);'||chr(10)||'  ci1.CI_Run();'||chr(10)||'  Construct_Init ci2(1,2);'||chr(10)||'  ci2.CI_Run();'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UIC.msg_template', 'In ${se.filename}, constructor ${se.func} at line ${se.line} has a field ${se.var} that is not initialized.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UIC.msg_template', '在${se.filename}，函数 ${se.func}, 第${se.line}行, 有一个字段${se.var}尚未初始化.', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UIC.name', 'Uninitialized field in constructor', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UIC.name', '构造函数中有未初始化字段', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIC'),
 'BASIC','CUSTOM','xcalibyte')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIC'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIC'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIC'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;