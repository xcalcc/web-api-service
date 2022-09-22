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
-- AOB
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'VUL', 'AOB', 'AOB', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.AOB.name}', '1', '2', 'LIKELY', 'HIGH', '${rule.Xcalibyte.BUILTIN.1.AOB.detail}', '${rule.Xcalibyte.BUILTIN.1.AOB.description}', '${rule.Xcalibyte.BUILTIN.1.AOB.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.AOB.description', 'The program is accessing data outside the declared boundary (before or after) of the intended buffer.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.AOB.description', '该程序正在预期缓存的已声明的界限外（界限之前或之后）访问数据。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.AOB.detail', '#### Abstract'||chr(10)||'The program is accessing data outside the declared boundary (before or after) of the intended buffer.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Typically, this can allow attackers to cause a crash during program execution. A crash can occur when the code reads read sensitive information from other memory locations or causeble amount of data and assumes that a sentinel exists to stop the read operation, such as a NUL in a string. The expected sentinel might not be located in the out-of-bounds memory, causinfg excessive data to be read, leading to a segmentation fault or a buffer overflow. The software may modify an index or perform pointer arithmetic that references a memory location that is outside of the boundaries of the buffer. A subsequent read operation then produces undefined or unexpected results.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||'int assign(int* a, int i)'||chr(10)||'{'||chr(10)||'  return a[i];  /* called by main'||chr(10)||'                   a only has 2 elements but i is 2 */'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int a[2] = {0, 1}, b;'||chr(10)||'  b = assign(a, 2);  // call assign with a and i'||chr(10)||'                     // a has two elements and i is 2'||chr(10)||'                     // a[2] is out-of-bound and also uninitialized'||chr(10)||'  printf("value of b = %d\n", b);'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||'int assign(int* a, int i)'||chr(10)||'{'||chr(10)||'  return a[i];  /* called by main'||chr(10)||'                   a only has 2 elements but i is 2 */'||chr(10)||'}'||chr(10)||''||chr(10)||'#define ARR_SZ 2'||chr(10)||'int main() {'||chr(10)||'  int a[ARR_SZ] = {0, 1}, b;'||chr(10)||'  if ()'||chr(10)||'  b = assign(a, (ARR_SZ-1));  // call assign with a and i'||chr(10)||'                     // a has two elements and i is 2'||chr(10)||'                     // a[2] is out-of-bound and also uninitialized'||chr(10)||'  printf("value of b = %d\n", b);'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.AOB.detail', '#### 概要'||chr(10)||'该程序正在预期缓存的已声明的界限外（界限之前或之后）访问数据。'||chr(10)||'#### 解释'||chr(10)||'通常情况下，这会允许攻击者在程序执行期间造成程序崩溃。当代码从其他内存位置读取敏感数据或可疑数据量并假定存在前哨来阻止读操作时（例如字符串里的空值），可能会发生崩溃。预期的前哨可能不位于越界内存里，致使读取过量数据，从而导致段错误或缓存溢出。软件可能修改引用在缓存界限之外的内存位置的索引或执行指针运算。随后的读运算便会产生未定义或意外结果。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||''||chr(10)||'````text'||chr(10)||'int assign(int* a, int i)'||chr(10)||'{'||chr(10)||'  return a[i];  /* called by main'||chr(10)||'                   a only has 2 elements but i is 2 */'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int a[2] = {0, 1}, b;'||chr(10)||'  b = assign(a, 2);  // call assign with a and i'||chr(10)||'                     // a has two elements and i is 2'||chr(10)||'                     // a[2] is out-of-bound and also uninitialized'||chr(10)||'  printf("value of b = %d\n", b);'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||'int assign(int* a, int i)'||chr(10)||'{'||chr(10)||'  return a[i];  /* called by main'||chr(10)||'                   a only has 2 elements but i is 2 */'||chr(10)||'}'||chr(10)||''||chr(10)||'#define ARR_SZ 2'||chr(10)||'int main() {'||chr(10)||'  int a[ARR_SZ] = {0, 1}, b;'||chr(10)||'  if ()'||chr(10)||'  b = assign(a, (ARR_SZ-1));  // call assign with a and i'||chr(10)||'                     // a has two elements and i is 2'||chr(10)||'                     // a[2] is out-of-bound and also uninitialized'||chr(10)||'  printf("value of b = %d\n", b);'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.AOB.msg_template', 'In ${se.filename}, line ${se.line}, an array out of bound has been detected for variable ${se.var} in ${se.func}. ${se.var} is declared in ${ss.file} at line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.AOB.msg_template', '在${se.filename}，第${se.line}行，已经检测到 ${se.func} 里的变量${se.var}有数组越界。${se.var}的声明是在${ss.file}第${ss.line}行。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.AOB.name', 'Array Out Of Bound', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.AOB.name', '数组越界', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='AOB');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='AOB'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='AOB'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='AOB'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='AOB'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- DBF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'VUL', 'DBF', 'DBF', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.DBF.name}', '2', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.DBF.detail}', '${rule.Xcalibyte.BUILTIN.1.DBF.description}', '${rule.Xcalibyte.BUILTIN.1.DBF.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.DBF.description', 'The program has freed some resources (e.g. heap memory, I/O stream object etc) multiple times.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DBF.description', '该程序已多次释放了某些资源（例如，堆内存、I/O流对象等）。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DBF.detail', '#### Abstract'||chr(10)||'The program has freed some resources (e.g. heap memory, I/O stream object etc) multiple times.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'The program has called functions such as free(), close() etc multiple times to release the same resource object. This could cause inconsistent system such as corruption of the system''s heap management data strutures or I/O stream subclasses etc. This in turn may allow malicious users to access arbitrary memory or cause an IOException.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||'int func_1(void *p) {'||chr(10)||'  if (p != NULL) {'||chr(10)||'    free(p);   // free p'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'int func_2(void *p) {'||chr(10)||'  if (p != NULL) {'||chr(10)||'    free(p);  // free p'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int i, *p, *q;'||chr(10)||'  p = malloc(10 * sizeof(int));'||chr(10)||'  if (p == NULL)'||chr(10)||'    return 1;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    p[i] = i;'||chr(10)||'  q = p;'||chr(10)||'  func_1(p);  // free p the first time'||chr(10)||'  func_2(q);  // free p the second time due to value of p copied into q'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||'int func_1(void *p) {'||chr(10)||'  if (p != NULL) {'||chr(10)||'    free(p);   // free p'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'int func_2(void *p) {'||chr(10)||'  if (p != NULL) {'||chr(10)||'    free(p);  // free p'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int i, *p, *q;'||chr(10)||'  p = malloc(10 * sizeof(int));'||chr(10)||'  if (p == NULL)'||chr(10)||'    return 1;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    p[i] = i;'||chr(10)||'  q = p;'||chr(10)||'  func_1(p);  // free p the first time and only time'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DBF.detail', '#### 概要'||chr(10)||'该程序已多次释放了某些资源（例如，堆内存、I/O流对象等）。'||chr(10)||''||chr(10)||'### 解释'||chr(10)||'该程序已多次调用了像free()、close()这样的函数来释放同一个资源对象。这会导致不一致的系统，例如系统的堆管理数据结构或I/O流子类等的损坏。这从而可能允许恶意用户访问任意内存或造成IOException。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||'int func_1(void *p) {'||chr(10)||'  if (p != NULL) {'||chr(10)||'    free(p);   // free p'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'int func_2(void *p) {'||chr(10)||'  if (p != NULL) {'||chr(10)||'    free(p);  // free p'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int i, *p, *q;'||chr(10)||'  p = malloc(10 * sizeof(int));'||chr(10)||'  if (p == NULL)'||chr(10)||'    return 1;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    p[i] = i;'||chr(10)||'  q = p;'||chr(10)||'  func_1(p);  // free p the first time'||chr(10)||'  func_2(q);  // free p the second time due to value of p copied into q'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||'int func_1(void *p) {'||chr(10)||'  if (p != NULL) {'||chr(10)||'    free(p);   // free p'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'int func_2(void *p) {'||chr(10)||'  if (p != NULL) {'||chr(10)||'    free(p);  // free p'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int i, *p, *q;'||chr(10)||'  p = malloc(10 * sizeof(int));'||chr(10)||'  if (p == NULL)'||chr(10)||'    return 1;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    p[i] = i;'||chr(10)||'  q = p;'||chr(10)||'  func_1(p);  // free p the first time and only time'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DBF.msg_template', 'In ${se.filename}, line ${se.line}, the resource (variable) ${se.var} at ${se.func} has been released multiple times. This resource ${se.var} is first released at ${ss.file}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DBF.msg_template', '在${se.filename}，第${se.line}行，${se.func}的资源（变量）${s2.var}已被多次释放。该资源${s2.var} 在${s1.file} 第${ss.line}行被首次释放。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DBF.name', 'Resource Was Freed Multiple Times', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DBF.name', '资源被多次释放', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DBF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DBF'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DBF'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DBF'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

-- ------------------------
-- DBZ
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'CORRECTNESS', 'DBZ', 'DBZ', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.DBZ.name}', '3', '3', 'UNLIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.DBZ.detail}', '${rule.Xcalibyte.BUILTIN.1.DBZ.description}', '${rule.Xcalibyte.BUILTIN.1.DBZ.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.DBZ.description', 'The program is trying to divide a value by zero.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DBZ.description', '该程序正试图将值除以零。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DBZ.detail', '#### Abstract'||chr(10)||'The program is trying to divide a value by zero.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'This can occur when an unexpected constant zero is assigned to the divisor, or if an error occurs that is not properly detected (such as return value of a function call).'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int result = 0;'||chr(10)||'    if (guard != 0)'||chr(10)||'      result = a / x;  // did not check for zero'||chr(10)||'    printf("result is %d", result);'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DBZ.detail', '#### 概要'||chr(10)||'该程序正试图将值除以零。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'当把意外的常数零赋值给除数，或者发生了未适当检测到的错误（例如函数调用的返回值）时，这可能会发生。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int result = 0;'||chr(10)||'    if (guard != 0)'||chr(10)||'      result = a / x;  // did not check for zero'||chr(10)||'    printf("result is %d", result);'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DBZ.msg_template', 'In ${se.filename}, line ${se.line}, variable ${se.var} in ${se.func}, a division by zero has been detected. This variable ${se.var} has value zero at ${ss.file}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DBZ.msg_template', '在${se.filename}，第${se.line}行，${se.func} 里的变量 ${se.var}，已检测到了被除以零。该变量在${ss.file}，第${ss.line}行$的值是零。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DBZ.name', 'Division By Zero', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DBZ.name', '被零除', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DBZ');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DBZ'),
 'BASIC','PRIORITY','3'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DBZ'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DBZ'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DBZ'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- DDV
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'PFM', 'DDV', 'DDV', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.DDV.name}', '3', '3', 'UNLIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.DDV.detail}', '${rule.Xcalibyte.BUILTIN.1.DDV.description}', '${rule.Xcalibyte.BUILTIN.1.DDV.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.DDV.description', 'Execution of this statement will be nullified by another statement following it, or the result of this statement is never used.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DDV.description', '这个语句之后的另一个语句将使此语句的执行无效，或者从未使用过此语句的结果。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DDV.detail', '#### Abstract'||chr(10)||'Execution of this statement will be nullified by another statement following it, or the result of this statement is never used.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Such error usually indicates either a typo or that some statement has been removed through time.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'void assign(int input)'||chr(10)||'{'||chr(10)||'    int a = 0; // dead code'||chr(10)||'    a = input; // result of "a = 0" will be nullified by this statement'||chr(10)||'    printf("a value: %d", a);'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DDV.detail', '#### 概要'||chr(10)||'这个语句之后的另一个语句将使此语句的执行无效，或者从未使用过此语句的结果。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'此类错误通常表明或是有输入错误，或者过了一段时间后删除了某些语句。'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||'void assign(int input)'||chr(10)||'{'||chr(10)||'    int a = 0; // dead code'||chr(10)||'    a = input; // result of "a = 0" will be nullified by this statement'||chr(10)||'    printf("a value: %d", a);'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DDV.msg_template', 'In ${se.filename}, line ${se.line}, variable ${se.var} in ${se.func}, an dead assignment has been detected.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DDV.msg_template', '在${se.filename}，第${se.line}行，${se.func}里的变量 ${se.var} 是闲置赋值。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.DDV.name', 'Dead Variable', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.DDV.name', '闲置变量', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDV');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDV'),
 'BASIC','PRIORITY','3'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDV'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDV'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='DDV'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ECB
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'BAD_PRACTICE', 'ECB', 'c++,java', null, '${rule.Xcalibyte.BUILTIN.1.ECB.name}', 'ECB', 3, 2, 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.ECB.detail}', '${rule.Xcalibyte.BUILTIN.1.ECB.description}', '${rule.Xcalibyte.BUILTIN.1.ECB.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.ECB.description', 'The program has an exception construct with an empty catch block.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.ECB.description', '该程序有带空catch块的异常构造。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.ECB.detail', '#### Abstract'||chr(10)||'The program has an exception construct with an empty catch block.'||chr(10)||'#### Explanation'||chr(10)||'Empty catch block effectively suppressed an exception from correctly handling by another try block. The "uncatched" exception may cause unintended program behavior.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||'#include <iostream>'||chr(10)||''||chr(10)||'int integer_divide(int a, int b) {'||chr(10)||'    if (b == 0) {'||chr(10)||'        throw "division by zero error";'||chr(10)||'    }'||chr(10)||'    else return (a/b);'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo(int x, int y) {'||chr(10)||'    try {'||chr(10)||'        int z = integer_divide(x, y);'||chr(10)||'        std::cout << z << std::endl;'||chr(10)||'    }'||chr(10)||'    catch (const char* msg) {'||chr(10)||'        // empty body, zero as divisor notice suppressed'||chr(10)||'    }'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||'#include <iostream>'||chr(10)||''||chr(10)||'int integer_divide(int a, int b) {'||chr(10)||'    if (b == 0) {'||chr(10)||'        throw "division by zero error";'||chr(10)||'    }'||chr(10)||'    else return (a/b);'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo(int x, int y) {'||chr(10)||'    try {'||chr(10)||'        int z = integer_divide(x, y);'||chr(10)||'        std::cout << z << std::endl;'||chr(10)||'    }'||chr(10)||'    catch (const char* msg) {'||chr(10)||'        // non-empty catch body and return error'||chr(10)||'        std::cout << "division exception" << std::endl;'||chr(10)||'        return 1;'||chr(10)||'    }'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.ECB.detail', '#### 概要'||chr(10)||'该程序有带空catch块的异常构造。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'空的catch块会抑制了另一个try块无法正确处理的异常。 “未捕获”的异常可能会导致程序意外行为。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||''||chr(10)||'````text'||chr(10)||'#include <iostream>'||chr(10)||''||chr(10)||'int integer_divide(int a, int b) {'||chr(10)||'    if (b == 0) {'||chr(10)||'        throw "division by zero error";'||chr(10)||'    }'||chr(10)||'    else return (a/b);'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo(int x, int y) {'||chr(10)||'    try {'||chr(10)||'        int z = integer_divide(x, y);'||chr(10)||'        std::cout << z << std::endl;'||chr(10)||'    }'||chr(10)||'    catch (const char* msg) {'||chr(10)||'        // empty body,  zero as divisor notice suppressed'||chr(10)||'    }'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <iostream>'||chr(10)||''||chr(10)||'int integer_divide(int a, int b) {'||chr(10)||'    if (b == 0) {'||chr(10)||'        throw "division by zero error";'||chr(10)||'    }'||chr(10)||'    else return (a/b);'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo(int x, int y) {'||chr(10)||'    try {'||chr(10)||'        int z = integer_divide(x, y);'||chr(10)||'        std::cout << z << std::endl;'||chr(10)||'    }'||chr(10)||'    catch (const char* msg) {'||chr(10)||'        // non-empty catch body and return error'||chr(10)||'        std::cout << "division exception" << std::endl;'||chr(10)||'        return 1;'||chr(10)||'    }'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.ECB.msg_template', 'In ${se.filename}, ${se.func}, the catch block in line ${se.line} is empty.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.ECB.msg_template', '在${se.filename}，${se.func} 里第${se.line}行的catch块是空的。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.ECB.name', 'Empty catch block', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.ECB.name', '空的异常捕获块', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='ECB');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='ECB'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='ECB'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='ECB'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- FAM
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'VUL', 'FAM', 'FAM', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.FAM.name}', '1', '2', 'UNLIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.FAM.detail}', '${rule.Xcalibyte.BUILTIN.1.FAM.description}', '${rule.Xcalibyte.BUILTIN.1.FAM.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.FAM.description', 'The program is calling a function with number of paramter(s) used different from that of the prototype declaration.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FAM.description', '该程序正调用函数，其使用的参数个数与原型声明的参数个数不同。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.FAM.detail', '#### Abstract'||chr(10)||'The program is calling a function with number of paramter(s) used different from that of the prototype declaration.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'In a call site, the number of actual parameter passed is different from that of the function declaration. If the actuals passed is less than that of the declaration, the missing actual will ended up as being "wild" and could cause unpreditable behavior.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int result = 0;'||chr(10)||'    if (guard != 0)'||chr(10)||'      result = a * x;'||chr(10)||'    foo(result); // missing one parameter, and "guard" will have random value during execution at this point'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FAM.detail', '#### 概要'||chr(10)||'该程序正调用函数，其使用的参数个数与原型声明的参数个数不同。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'在调用点里，传递的实际参数个数与函数声明里的参数个数不同。如果传递的实际参数比声明的要少，缺失的实际参数最终会变成"野的"，并会造成不可预测的行为。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||''||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int result = 0;'||chr(10)||'    if (guard != 0)'||chr(10)||'      result = a * x;'||chr(10)||'    foo(result); // missing one parameter, and "guard" will have random value during execution at this point'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.FAM.msg_template', 'In ${se.filename}, line ${se.line}, the arguments in the function ${se.func} does not match the function declaration in file ${ss.filename}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FAM.msg_template', '在${se.filename}，第${se.line}行，${se.func}的参数与文件${ss.filename}，第${ss.line}行里的函数声明不匹配。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.FAM.name', 'Formal And Actual Parameter Mismatch', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FAM.name', '形式和实际参数不匹配', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FAM');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FAM'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FAM'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FAM'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FAM'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- FMT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'VUL', 'FMT', 'FMT', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.FMT.name}', '1', '2', 'UNLIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.FMT.detail}', '${rule.Xcalibyte.BUILTIN.1.FMT.description}', '${rule.Xcalibyte.BUILTIN.1.FMT.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.FMT.description', 'The program is calling one of printf family with number (or type) of paramter(s) used different from format string declaration.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FMT.description', '该程序正调用printf函数族中的一个，其使用的参数个数（或类型）与格式字符串声明的不同。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.FMT.detail', '#### Abstract'||chr(10)||'The program is calling one of printf family with number (or type) of paramter(s) used different from format string declaration.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'In a printf (or family of this kinds of system calls), the format string specifier inconsistent with the actual parameters or that specifiers has unsupported character in the format string. When the actual passed is different, what is actually printed will be unpredictable.'||chr(10)||''||chr(10)||'#### Example 1'||chr(10)||'````text'||chr(10)||''||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int int_result = 0;'||chr(10)||''||chr(10)||'    if (guard != 0)'||chr(10)||'      int_result = a * x;'||chr(10)||''||chr(10)||'    // The format string specified for a "%c", but the parameter is of size int, result is truncated and unexpected'||chr(10)||'    printf("result %c is truncated\n", int_result);'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example 2'||chr(10)||'````text'||chr(10)||''||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int int_result = 0;'||chr(10)||''||chr(10)||'    if (guard != 0)'||chr(10)||'      int_result = a * x;'||chr(10)||''||chr(10)||'    // The format string specified two values to be printed, only one actual parameter is passed. The second output is unpredictable'||chr(10)||'    printf("result %d, guard = %d\n", int_result);'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'`````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FMT.detail', '#### 概要'||chr(10)||'该程序正调用printf函数族中的一个，其使用的参数个数（或类型）与格式字符串声明的不同。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'在printf（或此类系统调用的家族）里，使用的实际参数的个数或类型与格式化字符串说明符的不同。当传递的实际参数不同时，则实际打印的内容将不可预测。'||chr(10)||''||chr(10)||'#### 示例 1'||chr(10)||'````text'||chr(10)||''||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int int_result = 0;'||chr(10)||''||chr(10)||'    if (guard != 0)'||chr(10)||'      int_result = a * x;'||chr(10)||''||chr(10)||'    // The format string specified for a "%c", but the parameter is of size int, result is truncated and unexpected'||chr(10)||'    printf("result %c is truncated\n", int_result);'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 2'||chr(10)||'````text'||chr(10)||''||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int int_result = 0;'||chr(10)||''||chr(10)||'    if (guard != 0)'||chr(10)||'      int_result = a * x;'||chr(10)||''||chr(10)||'    // The format string specified two values to be printed, only one actual parameter is passed. The second output is unpredictable'||chr(10)||'    printf("result %d, guard = %d\n", int_result);'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'`````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.FMT.msg_template', 'In ${se.filename}, line ${se.line}, the format specification in ${se.func} has different number of arguments from that in the format string declaration.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FMT.msg_template', '在${se.filename}，第${se.line}行，${se.func} 里的格式规范的参数个数与格式字符串声明的参数个数不同。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.FMT.name', 'Format String Overflow', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FMT.name', '格式字符串溢出', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FMT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FMT'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FMT'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FMT'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'VUL', 'FMT', 'FMT-2', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.FMT-2.name}', '1', '2', 'UNLIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.FMT-2.detail}', '${rule.Xcalibyte.BUILTIN.1.FMT-2.description}', '${rule.Xcalibyte.BUILTIN.1.FMT-2.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.FMT-2.description', 'The program is calling one of printf family with number (or type) of paramter(s) used different from format string declaration.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FMT-2.description', '该程序正调用printf函数族中的一个，其使用的参数个数（或类型）与格式字符串声明的不同。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.FMT-2.detail', '#### Abstract'||chr(10)||'The program is calling one of printf family with number (or type) of paramter(s) used different from format string declaration.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'In a printf (or family of this kinds of system calls), the format string specifier inconsistent with the actual parameters or that specifiers has unsupported character in the format string. When the actual passed is different, what is actually printed will be unpredictable.'||chr(10)||''||chr(10)||'#### Example 1'||chr(10)||'````text'||chr(10)||''||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int int_result = 0;'||chr(10)||''||chr(10)||'    if (guard != 0)'||chr(10)||'      int_result = a * x;'||chr(10)||''||chr(10)||'    // The format string specified for a "%c", but the parameter is of size int, result is truncated and unexpected'||chr(10)||'    printf("result %c is truncated\n", int_result);'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example 2'||chr(10)||'````text'||chr(10)||''||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int int_result = 0;'||chr(10)||''||chr(10)||'    if (guard != 0)'||chr(10)||'      int_result = a * x;'||chr(10)||''||chr(10)||'    // The format string specified two values to be printed, only one actual parameter is passed. The second output is unpredictable'||chr(10)||'    printf("result %d, guard = %d\n", int_result);'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'`````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FMT-2.detail', '#### 概要'||chr(10)||'该程序正调用printf函数族中的一个，其使用的参数个数（或类型）与格式字符串声明的不同。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'在printf（或此类系统调用的家族）里，使用的实际参数的个数或类型与格式化字符串说明符的不同。当传递的实际参数不同时，则实际打印的内容将不可预测。'||chr(10)||''||chr(10)||'#### 示例 1'||chr(10)||'````text'||chr(10)||''||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int int_result = 0;'||chr(10)||''||chr(10)||'    if (guard != 0)'||chr(10)||'      int_result = a * x;'||chr(10)||''||chr(10)||'    // The format string specified for a "%c", but the parameter is of size int, result is truncated and unexpected'||chr(10)||'    printf("result %c is truncated\n", int_result);'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 2'||chr(10)||'````text'||chr(10)||''||chr(10)||'int foo(int a, int guard)'||chr(10)||'{'||chr(10)||'    int x = 0;'||chr(10)||'    int int_result = 0;'||chr(10)||''||chr(10)||'    if (guard != 0)'||chr(10)||'      int_result = a * x;'||chr(10)||''||chr(10)||'    // The format string specified two values to be printed, only one actual parameter is passed. The second output is unpredictable'||chr(10)||'    printf("result %d, guard = %d\n", int_result);'||chr(10)||'    return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'`````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.FMT-2.msg_template', 'In ${se.filename}, line ${se.line}, the format specification in ${se.func}, argument ${se.num} has unknown format character in the format string declaration.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FMT-2.msg_template', '在${se.filename}，第${se.line}行 ${se.func}里的格式语句，参数的类型与格式字符串声明里的参数类型不同。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.FMT-2.name', 'Format String Overflow', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.FMT-2.name', '格式字符串溢出', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FMT-2');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FMT-2'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FMT-2'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='FMT-2'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

-- ------------------------
-- MSF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'ROBUSTNESS', 'MSF', 'MSF', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.MSF.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.MSF.detail}', '${rule.Xcalibyte.BUILTIN.1.MSF.description}', '${rule.Xcalibyte.BUILTIN.1.MSF.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.MSF.description', 'The program has allocated heap memory but failed to free that piece of memory.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.MSF.description', '该程序已分配了堆内存，但未能释放那块内存。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.MSF.detail', '#### Abstract'||chr(10)||'The program has allocated heap memory but failed to free that piece of memory.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Heap memory are allocated and the allocated memory address is stored in a variable of reference (pointer) type. That heap memory has nevered been released after its lifetime has effectively ended. However, its corresponding reference still point to that memory space. This could result in sensitive data leakage or unexpected program behavior such as denial of service.'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'struct S {'||chr(10)||'  int *p;'||chr(10)||'};'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int *p;'||chr(10)||'  struct S *s;'||chr(10)||'  p = malloc(10 * sizeof(int));  // heap memory allocated and pointed to by p'||chr(10)||'  if (p == NULL)'||chr(10)||'    return 1;'||chr(10)||'  s = (struct S*)malloc(sizeof(struct S));  // heap memory allocated and pointed to by s'||chr(10)||'  if (s == NULL) {'||chr(10)||'    free(p);'||chr(10)||'    return 1;'||chr(10)||'  }'||chr(10)||'  s->p = p;'||chr(10)||'  free(s);    // only s is freed'||chr(10)||'              // s->p, which is copied from p is not freed'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.MSF.detail', '#### 概要'||chr(10)||'该程序已分配了堆内存，但未能释放那块内存。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'分配了堆内存并且分配的内存地址存储在引用（指针）类型的变量里。该堆内存在其生命周期实际结束后从未被释放。然而，其对应的引用仍然指向该内存空间。这可能会导致敏感数据泄漏或意外程序行为（例如拒绝服务）。'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||'struct S {'||chr(10)||'  int *p;'||chr(10)||'};'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int *p;'||chr(10)||'  struct S *s;'||chr(10)||'  p = malloc(10 * sizeof(int));  // heap memory allocated and pointed to by p'||chr(10)||'  if (p == NULL)'||chr(10)||'    return 1;'||chr(10)||'  s = (struct S*)malloc(sizeof(struct S));  // heap memory allocated and pointed to by s'||chr(10)||'  if (s == NULL) {'||chr(10)||'    free(p);'||chr(10)||'    return 1;'||chr(10)||'  }'||chr(10)||'  s->p = p;'||chr(10)||'  free(s);    // only s is freed'||chr(10)||'              // s->p, which is copied from p is not freed'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.MSF.msg_template', 'In ${se.filename}, line ${se.line}, the variable ${se.var} in ${se.func} has not been freed. ${se.var} is first assigned heap memory acquired in ${ss.file} at line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.MSF.msg_template', '在${se.filename}，第${se.line}行，没有释放 ${se.func}里的变量${se.var}。${se.var}是在${ss.file}, 第${ss.line}行获得首个堆内存的分配。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.MSF.name', 'Missing Free', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.MSF.name', '没有释放', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='MSF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='MSF'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='MSF'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='MSF'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

-- ------------------------
-- NPD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'ROBUSTNESS', 'NPD', 'NPD', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.NPD.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.NPD.detail}', '${rule.Xcalibyte.BUILTIN.1.NPD.description}', '${rule.Xcalibyte.BUILTIN.1.NPD.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.NPD.description', 'The program is accessing memory through a pointer with NULL value.  This will cause a segmentation fault or unpredictable program behavior. This vulnerability is equivalent to EXP34-C', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.NPD.description', '该程序正通过有空值的指针访问内存。这可能会导致段错误或不可预测的程序行为。该漏洞与EXP34-C等同', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.NPD.detail', '### NPD (EXP34-C)'||chr(10)||'#### Abstract'||chr(10)||'The program is accessing memory through a pointer with NULL value.  This will cause a segmentation fault or unpredictable program behavior. This vulnerability is equivalent to EXP34-C'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'On a system with memory protection, such as Linux, dereferencing a null pointer will cause a segmentation fault. For embedded systems, it will cause unpredicted program behavior. In Java, a null pointer dereference will trigger a null pointer exception.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - C - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern int bar(int);'||chr(10)||''||chr(10)||'int assign(int* a)'||chr(10)||'{'||chr(10)||'  int i = bar(*a) // dereference a'||chr(10)||'  return i;'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo(void)'||chr(10)||'{'||chr(10)||'  int *p = 0;  // p as a pointer has been initialized to 0 (null)'||chr(10)||'  assign(p);'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - C - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern int bar(int);'||chr(10)||''||chr(10)||'int assign(int* a)'||chr(10)||'{'||chr(10)||'  int i;'||chr(10)||'  if (a != 0)'||chr(10)||'    i = bar(*a);  // dereference a'||chr(10)||'  else {'||chr(10)||'    // handle error and exit gracefully (such exit program)'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||'  return i;'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo(void)'||chr(10)||'{'||chr(10)||'  int *p = 0;  // p as a pointer has been initialized to 0 (null)'||chr(10)||'  assign(p);'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - Java - avoid'||chr(10)||'````text'||chr(10)||'class User {'||chr(10)||'  public String getName() {'||chr(10)||'    return null;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'class Bar {'||chr(10)||'  private User findUser(String uid) {'||chr(10)||'    if (user.containsKey(uid)) {'||chr(10)||'      return user.get(uid);'||chr(10)||'    }'||chr(10)||'    else'||chr(10)||'      return null;'||chr(10)||'  }'||chr(10)||'  public void Npd(String uid) {'||chr(10)||'    // do something'||chr(10)||'    // ...'||chr(10)||'    User user = findUser(uid); // Throws NPE if "user" has not been properly initialized'||chr(10)||'    String getName() {'||chr(10)||'      return null;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - Java - prefer'||chr(10)||'````text'||chr(10)||'class User {'||chr(10)||'  public String getName() {'||chr(10)||'    return null;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'class Bar {'||chr(10)||'  private User findUser(String uid) {'||chr(10)||'    if (user.containsKey(uid)) {'||chr(10)||'      return user.get(uid);'||chr(10)||'    }'||chr(10)||'    else'||chr(10)||'      return null;'||chr(10)||'  }'||chr(10)||'  public void Npd(String uid) {'||chr(10)||'    // do something'||chr(10)||'    // ...'||chr(10)||'    User user = findUser(uid); '||chr(10)||'    if (user == null) {'||chr(10)||'      throw new RuntimeException("Null String");'||chr(10)||'    }'||chr(10)||'    String getName() {'||chr(10)||'      return null;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.NPD.detail', '### NPD (EXP34-C)'||chr(10)||'#### 概要'||chr(10)||'该程序正通过有空值的指针访问内存。这可能会导致段错误或不可预测的程序行为。该漏洞与EXP34-C等同'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'在有内存保护的系统里（例如Linux），解引用空指针会导致段错误。 对于嵌入系统来说，它会造成不可预测的程序行为。在Java里，空指针解引用会触发空指针异常。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例 - C - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern int bar(int);'||chr(10)||''||chr(10)||'int assign(int* a)'||chr(10)||'{'||chr(10)||'  int i = bar(*a) // dereference a'||chr(10)||'  return i;'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo(void)'||chr(10)||'{'||chr(10)||'  int *p = 0;  // p as a pointer has been initialized to 0 (null)'||chr(10)||'  assign(p);'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - C - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern int bar(int);'||chr(10)||''||chr(10)||'int assign(int* a)'||chr(10)||'{'||chr(10)||'  int i;'||chr(10)||'  if (a != 0)'||chr(10)||'    i = bar(*a);  // dereference a'||chr(10)||'  else {'||chr(10)||'    // handle error and exit gracefully (such exit program)'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||'  return i;'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo(void)'||chr(10)||'{'||chr(10)||'  int *p = 0;  // p as a pointer has been initialized to 0 (null)'||chr(10)||'  assign(p);'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - Java'||chr(10)||'````text'||chr(10)||'class User {'||chr(10)||'  public String getName() {'||chr(10)||'    return null;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'class Bar {'||chr(10)||'  private User findUser(String uid) {'||chr(10)||'    if (user.containsKey(uid)) {'||chr(10)||'      return user.get(uid);'||chr(10)||'    }'||chr(10)||'    else'||chr(10)||'      return null;'||chr(10)||'  }'||chr(10)||'  public void Npd(String uid) {'||chr(10)||'    // do something'||chr(10)||'    // ...'||chr(10)||'    User user = findUser(uid); // Throws NPE if "user" has not been properly initialized'||chr(10)||'    String getName() {'||chr(10)||'      return null;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例 - Java - 建议'||chr(10)||'````text'||chr(10)||'class User {'||chr(10)||'  public String getName() {'||chr(10)||'    return null;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'class Bar {'||chr(10)||'  private User findUser(String uid) {'||chr(10)||'    if (user.containsKey(uid)) {'||chr(10)||'      return user.get(uid);'||chr(10)||'    }'||chr(10)||'    else'||chr(10)||'      return null;'||chr(10)||'  }'||chr(10)||'  public void Npd(String uid) {'||chr(10)||'    // do something'||chr(10)||'    // ...'||chr(10)||'    User user = findUser(uid); '||chr(10)||'    if (user == null) {'||chr(10)||'      throw new RuntimeException("Null String");'||chr(10)||'    }'||chr(10)||'    String getName() {'||chr(10)||'      return null;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.NPD.msg_template', 'In ${se.filename}, line ${se.line}, a NPD defect has been detected in ${se.func} for ${se.var}. The ${se.var} has value "0" in ${ss.file} at line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.NPD.msg_template', '在${se.filename}，第${se.line}行，${se.func} 里对变量${se.var} 检测到了NPD缺陷。变量${se.var}在${se.file} 第${ss.line}行 是"0"的值。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.NPD.name', 'Null Pointer Dereference', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.NPD.name', '空指针解引用', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='NPD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='NPD'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='NPD'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='NPD'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='NPD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- RAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'VUL', 'RAL', 'RAL', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.RAL.name}', '1', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.RAL.detail}', '${rule.Xcalibyte.BUILTIN.1.RAL.description}', '${rule.Xcalibyte.BUILTIN.1.RAL.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.RAL.description', 'The function returns the address of a stack variable and will cause unintended program behavior.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RAL.description', '该函数返回了栈变量的地址，这会导致意外程序行为。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RAL.detail', '#### Abstract'||chr(10)||'The function returns the address of a stack variable and will cause unintended program behavior.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Because local variables are allocated on the stack, when a function returns to the caller, the callee''s stack address is no longer valid. A subsequent function call is likely to re-use this same stack address, thereby overwriting the previous value.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'int foo()'||chr(10)||'{'||chr(10)||'  int *c;'||chr(10)||'  return &c; //return a local address to caller'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RAL.detail', '#### 概要'||chr(10)||'该函数返回了栈变量的地址，这会导致意外程序行为。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'由于局部变量分配在栈上，当函数返回至调用者时，被调用者的栈地址不再有效。随后的函数调用很可能重用这个相同的栈地址，从而覆盖先前的值。'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||'int foo()'||chr(10)||'{'||chr(10)||'  int *c;'||chr(10)||'  return &c; //return a local address to caller'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RAL.msg_template', 'In ${se.filename}, line ${se.line}, address of the local variable ${se.var} has been returned to the caller.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RAL.msg_template', '在${se.filename}，第${se.line}行，已返回了局部变量${se.var}的地址给调用者。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RAL.name', 'Return Address of Local', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RAL.name', '局部变量的返回地址', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RAL'),
 'BASIC','PRIORITY','27'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RAL'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RAL'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

-- ------------------------
-- RXS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'VUL', 'RXS', 'RXS', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.RXS.name}', '2', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.RXS.detail}', '${rule.Xcalibyte.BUILTIN.1.RXS.description}', '${rule.Xcalibyte.BUILTIN.1.RXS.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.RXS.description', 'The program has read from external sockets which may include untrusted data.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RXS.description', '该程序已从可能包括不受信任数据的外部套接字中读取', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RXS.detail', '#### Abstract'||chr(10)||'The program has read from external sockets which may include untrusted data.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Because external sockets'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <sys/socket.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <unistd.h>'||chr(10)||''||chr(10)||'#define BUF_SZ 256'||chr(10)||''||chr(10)||'int foo(void)'||chr(10)||'{'||chr(10)||'  int n;'||chr(10)||'  char buffer[BUF_SZ];'||chr(10)||'  sockfd = socket(AF_INET, SOCK_STREAM, 0);'||chr(10)||''||chr(10)||'  if (sockfd < 0) {'||chr(10)||'    perror("ERROR opening socket");'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  n = read(sockfd,buffer,255);'||chr(10)||''||chr(10)||'  // use buffer'||chr(10)||'  // if buffer is used as argument to system() or to setenv()'||chr(10)||'  // the buffer may contain untrusted commands or characters leading to unpredictable program behavior'||chr(10)||'  // ...'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RXS.detail', '#### 概要'||chr(10)||'该程序已从可能包括不受信任数据的外部套接字中读取'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'Because external sockets'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <sys/socket.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <unistd.h>'||chr(10)||''||chr(10)||'#define BUF_SZ 256'||chr(10)||''||chr(10)||'int foo(void)'||chr(10)||'{'||chr(10)||'  int n;'||chr(10)||'  char buffer[BUF_SZ];'||chr(10)||'  sockfd = socket(AF_INET, SOCK_STREAM, 0);'||chr(10)||''||chr(10)||'  if (sockfd < 0) {'||chr(10)||'    perror("ERROR opening socket");'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  n = read(sockfd,buffer,255);'||chr(10)||''||chr(10)||'  // use buffer'||chr(10)||'  // if buffer is used as argument to system() or to setenv()'||chr(10)||'  // the buffer may contain untrusted commands or characters leading to unpredictable program behavior'||chr(10)||'  // ...'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RXS.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} is receiving untrusted data from external socket, the socket has been created in ${ss.filename} at line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RXS.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} is receiving untrusted data from external socket, the socket has been created in ${ss.filename} at line ${ss.line}.', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RXS.name', 'Read From External Socket', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RXS.name', '从外部套接字读取', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RXS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RXS'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RXS'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RXS'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

-- ------------------------
-- UAF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'VUL', 'UAF', 'UAF', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.UAF.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.UAF.detail}', '${rule.Xcalibyte.BUILTIN.1.UAF.description}', '${rule.Xcalibyte.BUILTIN.1.UAF.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.UAF.description', 'The program has referenced memory after it has been freed. It can cause the program to crash or unexpected program behavior.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UAF.description', '该程序在已释放了内存后引用了该内存。这可能导致程序崩溃或意外程序行为。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UAF.detail', '#### Abstract'||chr(10)||'The program has referenced memory after it has been freed. It can cause the program to crash or unexpected program behavior.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Use after free is a variation of dangling pointer reference. It typically occurs when the pointer was not updated after the memory object it points to has been freed. This pointer will be pointing to inappropriate memory leading to unauthorized access when the pointer is used.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'int g = 2;'||chr(10)||''||chr(10)||'void my_free(void *p) {'||chr(10)||'  if (p != NULL)'||chr(10)||'    free(p);  // free p'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int i, j, *p, *q;'||chr(10)||'  p = malloc(10 * sizeof(int));'||chr(10)||'  if (p == NULL)'||chr(10)||'    return 1;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    p[i] = i;'||chr(10)||'  q = p;'||chr(10)||'  my_free(p);  //  p is freed'||chr(10)||'  j = 0;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    j += q[i]; // Use after free here (note the statement q = p), read of q[i] is illegal'||chr(10)||'  return j;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UAF.detail', '#### 概要'||chr(10)||'该程序在已释放了内存后引用了该内存。这可能导致程序崩溃或意外程序行为。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'释放后重用是悬空指针引用的一种形式。它通常发生在指针所指向的内存对象释放后指针未更新时。该指针将指向不恰当的内存，这在使用指针时会导致未经授权的访问。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||'int g = 2;'||chr(10)||''||chr(10)||'void my_free(void *p) {'||chr(10)||'  if (p != NULL)'||chr(10)||'    free(p);  // free p'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int i, j, *p, *q;'||chr(10)||'  p = malloc(10 * sizeof(int));'||chr(10)||'  if (p == NULL)'||chr(10)||'    return 1;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    p[i] = i;'||chr(10)||'  q = p;'||chr(10)||'  my_free(p);  //  p is freed'||chr(10)||'  j = 0;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    j += q[i]; // Use after free here (note the statement q = p), read of q[i] is illegal'||chr(10)||'  return j;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UAF.msg_template', 'In ${se.filename}, line ${se.line}, the variable ${se.var} in ${se.func} was used, however, it has been freed at line ${ss.line} in ${ss.file}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UAF.msg_template', '在${se.filename}，第${se.line}行，使用了 ${se.func} 里的变量${se.var}。然而，它在${ss.file} 第${ss.line}行被释放。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UAF.name', 'Use After Free', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UAF.name', '释放后重用', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UAF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UAF'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UAF'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UAF'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

-- ------------------------
-- UDR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'VUL', 'UDR', 'UDR', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.UDR.name}', '1', '2', 'UNLIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.UDR.detail}', '${rule.Xcalibyte.BUILTIN.1.UDR.description}', '${rule.Xcalibyte.BUILTIN.1.UDR.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.UDR.description', 'Dangling pointer has been used to refer to an invalid memory resource.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UDR.description', '已使用了悬空指针来引用无效的内存资源。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UDR.detail', '#### Abstract'||chr(10)||'Dangling pointer has been used to refer to an invalid memory resource.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Dangling pointers are pointers that refers to invalid or inappropriate memory resource.  Referencing these memory resources may create memory corruption, result in unpredictable program behavior or system instablities.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'int *assign()'||chr(10)||'{'||chr(10)||'  int *c;'||chr(10)||'  c = 1;'||chr(10)||'  return &c;'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo()'||chr(10)||'{'||chr(10)||'  int *a;'||chr(10)||'  a = assign();  // return a stack address and assign to a'||chr(10)||'  printf("assigned value: %d", a); // use dangling pointer'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UDR.detail', '#### 概要'||chr(10)||'已使用了悬空指针来引用无效的内存资源。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'悬空指针是引用无效或不恰当的内存资源的指针。引用这些内存资源可能会造成内存损坏，从而导致不可预测的程序行为或系统不稳定。'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||'int assign()'||chr(10)||'{'||chr(10)||'  int *c;'||chr(10)||'  c = 1;'||chr(10)||'  return &c;'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo()'||chr(10)||'{'||chr(10)||'  int *a;'||chr(10)||'  a = assign();  // return a stack address and assign to a'||chr(10)||'  printf("assigned value: %d", a); // use dangling pointer'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UDR.msg_template', 'In ${se.filename}, line ${se.line}, the memory reference variable ${se.var} in ${se.func} is used. This variable ${se.var} is first assigned as a memory reference that may not be valid in ${ss.file}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UDR.msg_template', '在${s2.filename}，第${se.line}行，使用了${se.func} 里的内存引用变量。该变量${se.var}首次被赋值为内存引用，这在${ss.file}的第${ss.line}行可能无效。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UDR.name', 'Use Dangling Reference', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UDR.name', '使用悬空指针', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UDR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UDR'),
 'BASIC','PRIORITY','6'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UDR'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UDR'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;

-- ------------------------
-- UIV
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'CORRECTNESS', 'UIV', 'UIV', 'c,c++,java', null, '${rule.Xcalibyte.BUILTIN.1.UIV.name}', '2', '2', 'UNLIKELY', 'LOW', '${rule.Xcalibyte.BUILTIN.1.UIV.detail}', '${rule.Xcalibyte.BUILTIN.1.UIV.description}', '${rule.Xcalibyte.BUILTIN.1.UIV.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.UIV.description', 'The program is using a variable before it has been initialized.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UIV.description', '该程序正在变量初始化前使用该变量。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UIV.detail', '#### Abstract'||chr(10)||'The program is using a variable before it has been initialized.'||chr(10)||'#### Explanation'||chr(10)||'Stack variables in C and C++ are not initialized by default. Non-static global variables are not guaranteed to be zero''d either. Their initial values are determined by whatever happens to be in their location on memory at the time the function is invoked. Doing so will cause unexpected program behavior.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'int assign(int* a)'||chr(10)||'{'||chr(10)||'  return *a;  // dereference a'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int a, b;'||chr(10)||'  b = assign(&a);  // call assign with a uninitialized'||chr(10)||'  printf("value of b = %d\n", b);'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UIV.detail', '#### 概要'||chr(10)||'该程序正在变量初始化前使用该变量。'||chr(10)||'#### 解释'||chr(10)||'C和C++里的栈变量默认为非初始化。也不会保证设置非静态全局变量为零。它们的初始值取决于调用函数时它们在内存中的位置里恰好有的值。这么做会导致意外程序行为。'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||'int assign(int* a)'||chr(10)||'{'||chr(10)||'  return *a;  // dereference a'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int a, b;'||chr(10)||'  b = assign(&a);  // call assign with a uninitialized'||chr(10)||'  printf("value of b = %d\n", b);'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UIV.msg_template', 'In ${se.filename}, line ${se.line}, the variable ${se.var} in ${se.func} has been used but never assigned a value. ${se.var} is declared in ${ss.file} at line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UIV.msg_template', '在${se.filename}，第${se.line}行，已经使用了${se.func}里的变量${se.var}但从未对其赋值。${se.var}在${ss.file}第${ss.line}行声明。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.UIV.name', 'Uninitialised Variable', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.UIV.name', '未初始化的变量', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIV');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIV'),
 'BASIC','PRIORITY','6'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIV'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIV'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='UIV'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- WRF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1'), 'VUL', 'WRF', 'WRF', 'c,c++', null, '${rule.Xcalibyte.BUILTIN.1.WRF.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.BUILTIN.1.WRF.detail}', '${rule.Xcalibyte.BUILTIN.1.WRF.description}', '${rule.Xcalibyte.BUILTIN.1.WRF.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.WRF.description', 'The program is performing write operation to a file that is available for read only', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.WRF.description', '该程序正对只读文件执行写操作', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.WRF.detail', '#### Abstract'||chr(10)||'The program is performing write operation to a file that is available for read only'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'When the file that is opened for read and later on being written upon, the fwrite will report error. If the result of fwrite is not checked, the program will continue and the file may not be updated as expected.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||'int file_operation(two_level *p, two_level *q)'||chr(10)||'{'||chr(10)||'  struct stat statBefore, statAfter;'||chr(10)||''||chr(10)||'  lstat("/tmp/x", &statBefore);'||chr(10)||''||chr(10)||'  // file open for read'||chr(10)||'  FILE *fp_open_readonly = fopen("/tmp/x", "r");'||chr(10)||'  lstat("/tmp/x", &statAfter);'||chr(10)||''||chr(10)||'  if (statAfter.st_ino == statBefore.st_ino) {'||chr(10)||'    if (fp_open_readonly != NULL) {'||chr(10)||'      // writing to the file'||chr(10)||'      fwrite("HELLO!", 1, 5, fp_open_readonly);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  fclose(fp_open_readonly);'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.WRF.detail', '#### 概要'||chr(10)||'该程序正对只读文件执行写操作'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'当打开文件进行读取并随后写入时，fwrite会报告错误。如果不检查fwrite的结果，则该程序会继续进行，而文件可能不会按预期得到更新。'||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||'int file_operation(two_level *p, two_level *q)'||chr(10)||'{'||chr(10)||'  struct stat statBefore, statAfter;'||chr(10)||''||chr(10)||'  lstat("/tmp/x", &statBefore);'||chr(10)||''||chr(10)||'  // file open for read'||chr(10)||'  FILE *fp_open_readonly = fopen("/tmp/x", "r");'||chr(10)||'  lstat("/tmp/x", &statAfter);'||chr(10)||''||chr(10)||'  if (statAfter.st_ino == statBefore.st_ino) {'||chr(10)||'    if (fp_open_readonly != NULL) {'||chr(10)||'      // writing to the file'||chr(10)||'      fwrite("HELLO!", 1, 5, fp_open_readonly);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  fclose(fp_open_readonly);'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.WRF.msg_template', 'In ${se.file} function ${se.func}, line ${se.line}, we detected a ${rule.Xcalibyte.BUILTIN.1.WRF.msg_template.se.1}. ${rule.Xcalibyte.BUILTIN.1.WRF.msg_template.s5.1} ${s5.var} at line ${s5.line} in function ${s5.func}. ${rule.Xcalibyte.BUILTIN.1.WRF.msg_template.se.2} ${se.var} at line ${se.line}, function ${se.func}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.WRF.msg_template', '在${se.file} 第${se.line}行, 函数 ${se.func}，我们检测到了${rule.Xcalibyte.BUILTIN.1.WRF.msg_template.se.1}。'||chr(10)||'${rule.Xcalibyte.BUILTIN.1.WRF.msg_template.s5.1} 第${se.line}行, 函数${s5.func} 第${s5.line}的变量 ${s5.var}。'||chr(10)||'${rule.Xcalibyte.BUILTIN.1.WRF.msg_template.se.2} 第${se.line}行, 函数 ${se.func}的变量 ${se.var}。', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.WRF.msg_template.s5.1', 'The file was opened with "r" attribute and assigned to file handle', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.WRF.msg_template.s5.1', '使用"r"属性打开了文件并赋给文件句柄', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.WRF.msg_template.se.1', 'write to readonly file defect', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.WRF.msg_template.se.1', '写入只读文件缺陷', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.WRF.msg_template.se.2', 'A fwrite() operation was called with file handle', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.WRF.msg_template.se.2', '使用文件句柄调用了fwrite()操作', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.WRF.name', 'Write To File For Read Only', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.WRF.name', '写入只读文件', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='WRF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='WRF'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='WRF'),
 'BASIC','LANG','c++'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='WRF'),
 'BASIC','LANG','c')
ON CONFLICT DO NOTHING;
