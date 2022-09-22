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
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.description', '该程序有调用序列，它在运行时造成了递归', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.detail', '### CRF'||chr(10)||'#### Abstract'||chr(10)||'The program has a call sequence that results in recursion at runtime'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Recursion happens when a function, say A calls another function in such a way that the call sequence eventually calls A again. In its most simple form, a function simply calls itself during execution. If not programed correctly, this could lead to infinite loop. It could also causes excessive use of stack space and may lead to run out of memory or stack space problem.'||chr(10)||''||chr(10)||'#### Example 1'||chr(10)||'````text'||chr(10)||'// Most simple form of recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will recurse and get into infinite loop'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||'int func_recurse(int* p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_recurse(p, 2);  // calls itself directly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example 2'||chr(10)||'````text'||chr(10)||'// Indirect recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will call func_recurse inside func_b'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||''||chr(10)||'int func_b(int *q, int j)'||chr(10)||'{'||chr(10)||'  if (q != 0) {'||chr(10)||'    return func_recurse(q, 5);'||chr(10)||'  }'||chr(10)||'  else'||chr(10)||'    return 5;'||chr(10)||'}'||chr(10)||''||chr(10)||'int func_recurse(int *p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_b(p, i);  // calls func_recurse indirectly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.detail', '### CRF'||chr(10)||'#### 概要'||chr(10)||'该程序有调用序列，它在运行时造成了递归'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'当一个函数，假定是A，如此调用另外一个函数以至于调用序列最终再次调用A时，会发生递归。其最简单的形式是函数在执行期间就调用自身。如果不正确地编程，这可能会导致无限循环。它还会造成对栈空间的过分使用，并可能导致内存不足或栈空间问题。'||chr(10)||''||chr(10)||'#### 示例 1'||chr(10)||'````text'||chr(10)||'// Most simple form of recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will recurse and get into infinite loop'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||'int func_recurse(int* p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_recurse(p, 2);  // calls itself directly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 2'||chr(10)||'````text'||chr(10)||'// Indirect recursion.'||chr(10)||'// if first call to func_recurse is: func_recurse(p_glbl, 6)'||chr(10)||'// this code segment will call func_recurse inside func_b'||chr(10)||''||chr(10)||'int global = 5;'||chr(10)||'static *p_glbl = &global;'||chr(10)||''||chr(10)||'int func_b(int *q, int j)'||chr(10)||'{'||chr(10)||'  if (q != 0) {'||chr(10)||'    return func_recurse(q, 5);'||chr(10)||'  }'||chr(10)||'  else'||chr(10)||'    return 5;'||chr(10)||'}'||chr(10)||''||chr(10)||'int func_recurse(int *p, int i)'||chr(10)||'{'||chr(10)||'  if ((p != 0) && (*p != i))'||chr(10)||'    return func_b(p, i);  // calls func_recurse indirectly'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} is calling itself. The function declaration is in ${ss.filename}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.msg_template', '在${s2.filename}，${se.line}行上，函数 ${s2.func}在调用自身。函数声明在${ss.filename}，第${ss.line}行。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CRF.name', 'Use recursive function', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CRF.name', '使用递了归函数', 'system', 'system')
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
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.description', '该程序有调用序列太深, 超出用户设置的上限.', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.detail', '### CSL'||chr(10)||'#### Abstract'||chr(10)||'The program has a call sequence that causes the runtime stack to exceeds call depth limit set in Xcalscan using Xcalibyte rules APIs'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'A function X calls another function Y. Function Y may in turn calls another function. This call chain can go on indefinitely. In embedded systems, when the call stack is too deep, it may cause unintended side effects like run out of memory, inefficient execution time etc.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// This case does not need an example.'||chr(10)||'// The complete call level from function A to Z i.e. A() ==> B() ==> .... ==> Z(),  is too deep'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.detail', '### CSL'||chr(10)||'#### 概要'||chr(10)||'该程序有调用序列太深, 超出用户设置的上限.'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'当函数A调用另外一个函数B时, B同时也会调用C函数,等等 . 调用链太深可能会导致意外的副作用，如内存不足、执行时间不足等.'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||''||chr(10)||'// This case does not need an example.'||chr(10)||'// The complete call level from function A to Z i.e. A() ==> B() ==> .... ==> Z(),  is too deep'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} has a call sequence that exceeds call level limit set at scan configure set up time.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.msg_template', '在${se.filename}，${se.line}行上，函数 ${se.func}有一个调用序列，该调用序列超过了在扫描配置设置时设置的上限。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSL.name', 'Call stack level exit limit', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSL.name', '调用栈的內存超於设置上限', 'system', 'system')
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
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.description', '该程序有调用序列, 会致使运行时栈內存超出设置的上限.', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.detail', '### CSS'||chr(10)||'#### Abstract'||chr(10)||'The program has a call sequence that causes the runtime stack size to exceeds limit set in Xcalscan using Xcalibyte rules APIs'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'When function A calls another function B, parameters passed to the called function and return value from the called function to the caller function will be placed on the execution stack. Local variables will also be placed on the execution stack. The users has used Xcalibyte provided APIs create a customer scan rule that warns of such occurence.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'// stack size is 8 bytes for parameter, 4 bytes for return value (assume) 32 bit ABI'||chr(10)||'int func_callee(int* a, int i)'||chr(10)||'{'||chr(10)||'  return a[i];'||chr(10)||'}'||chr(10)||''||chr(10)||'// stack size is 0 byte (no parameter), 12 byte for local variable, 4 bytes for return value'||chr(10)||'int func_caller() {'||chr(10)||'  int a[3] = {0, 1}, b;'||chr(10)||'  b = assign(a, 1);  /* callee stack size if 12 bytes'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'// total stack size for the call sequence func_caller -> func_callee is 28 bytes'||chr(10)||'// (assume ABI specifies all parameters uses stack and not register)'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.detail', '### CSS'||chr(10)||'#### 概要'||chr(10)||'该程序有调用序列, 会致使运行时栈內存超出设置的上限.'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'当函数A调用另外一个函数B时，传递给被调用函数的参数和被调用函数给调用函数的返回值将被放置在执行栈上。局部变量也会被放置在执行栈上。用户已使用了鉴释提供的API创建了自定义扫描规则的上限，它对此类事件发出了警告'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||'// stack size is 8 bytes for parameter, 4 bytes for return value (assume) 32 bit ABI'||chr(10)||'int func_callee(int* a, int i)'||chr(10)||'{'||chr(10)||'  return a[i];'||chr(10)||'}'||chr(10)||''||chr(10)||'// stack size is 0 byte (no parameter), 12 byte for local variable, 4 bytes for return value'||chr(10)||'int func_caller() {'||chr(10)||'  int a[3] = {0, 1}, b;'||chr(10)||'  b = assign(a, 1);  /* callee stack size if 12 bytes'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'// total stack size for the call sequence func_caller -> func_callee is 28 bytes'||chr(10)||'// (assume ABI specifies all parameters uses stack and not register)'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} has a call sequence that exceeds stack size limit set at scan configure set up time.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.msg_template', '在${se.filename}，${se.line}行上，函数 ${se.func}有一个调用序列，该调用序列会致使运行时栈內存超过了在扫描配置设置时设置的上限。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.CSS.name', 'Call stack size exit limit', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.CSS.name', '调用栈的深度超於设置上限', 'system', 'system')
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