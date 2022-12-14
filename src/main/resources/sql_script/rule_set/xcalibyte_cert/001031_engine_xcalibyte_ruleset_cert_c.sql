-- ------------------------
-- Scan Engine Xcalibyte
-- ------------------------
insert into xcalibyte.scan_engine (name, version, revision, description, language, url, provider, provider_url, license, license_url, created_by, modified_by)
 values
('Xcalibyte', '1', '1.0', 'Xcalibyte static analyzer', 'c,c++,java', 'http://www.xcalibyte.com', 'Xcalibyte', 'http://www.xcalibyte.com', 'Xcalibyte commercial license', '', 'system', 'system')
ON CONFLICT DO NOTHING;


-- ------------------------
-- Rule set Xcalibyte CERT, C/C++
-- ------------------------
insert into xcalibyte.rule_set
 (scan_engine_id, name, version, revision, display_name, description, language, url, provider, provider_url, license, license_url, created_by, modified_by)
 values
 ((select id from xcalibyte."scan_engine" where name = 'Xcalibyte' and version ='1'), 'CERT', '1', '1.0', 'CERT', 'CERT ruleset', 'c,c++,java', 'CMU SEI CERT Coding Standards can be found at https://wiki.sei.cmu.edu/confluence/display/seccode/SEI+CERT+Coding+Standards Implemented by Xcalibyte', 'Xcalibyte', 'http://www.xcalibyte.com', 'Xcalibyte commercial license', '', 'system', 'system')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ARR38-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ARR38-C', null, 'ARR38-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/ARR38-C.+Guarantee+that+library+functions+do+not+form+invalid+pointers', '${rule.Xcalibyte.CERT.1.ARR38-C.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.ARR38-C.detail}', '${rule.Xcalibyte.CERT.1.ARR38-C.description}', '${rule.Xcalibyte.CERT.1.ARR38-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.ARR38-C.description', 'The program is calling a library function with a pointer and a size parameter. The two parameters, when combined, will be outside of legal range of the object.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ARR38-C.description', '?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ARR38-C.detail', '#### Abstract'||chr(10)||'The program is calling a library function with a pointer and a size parameter. The two parameters, when combined, will be outside of legal range of the object.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'The program is passing a pointer to an object and a size parameter to the library function. The two parameters, when combined, will cause the library function to access this object but outside its valid range, resulting in undefined behavior.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||'#define ARRAY_SZ 2'||chr(10)||'int main()'||chr(10)||'{'||chr(10)||'  int a[ARRAY_SZ] = {0, 1}, dest[ARRAY_SZ];'||chr(10)||''||chr(10)||'  int *p_dest = &dest[1];  // p points to beginning of dest[1];'||chr(10)||'  memset(p_dest, a, sizeof(a)); // a + sizeof(a) will be one passed a[ARRAY_SZ]'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||'#define ARRAY_SZ 2'||chr(10)||'int main()'||chr(10)||'{'||chr(10)||'  int a[ARRAY_SZ] = {0, 1}, dest[ARRAY_SZ];'||chr(10)||''||chr(10)||'  int *p_dest = &dest[1];  // p points to beginning of dest[1];'||chr(10)||'  memset(p_dest, a, sizeof(a)); // a + sizeof(a) will be one passed a[ARRAY_SZ]'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||'#define ARRAY_SZ 2'||chr(10)||'int main()'||chr(10)||'{'||chr(10)||'  int a[ARRAY_SZ] = {0, 1}, dest[ARRAY_SZ];'||chr(10)||''||chr(10)||'  int *p_dest = &dest[1];  // p points to beginning of dest[1];'||chr(10)||'  if (sizeof(dest[1] < sizeof(a)) {'||chr(10)||'    // report error and/exit with proper errno set'||chr(10)||'  }'||chr(10)||'  else '||chr(10)||'    memset(p_dest, a, sizeof(a)); // a + sizeof(a) will be one passed a[ARRAY_SZ]'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ARR38-C.detail', '#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||'````text'||chr(10)||'#define ARRAY_SZ 2'||chr(10)||'int main()'||chr(10)||'{'||chr(10)||'  int a[ARRAY_SZ] = {0, 1}, dest[ARRAY_SZ];'||chr(10)||''||chr(10)||'  int *p_dest = &dest[1];  // p points to beginning of dest[1];'||chr(10)||'  memset(p_dest, a, sizeof(a)); // a + sizeof(a) will be one passed a[ARRAY_SZ]'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### ?????? - ??????'||chr(10)||'````text'||chr(10)||'#define ARRAY_SZ 2'||chr(10)||'int main()'||chr(10)||'{'||chr(10)||'  int a[ARRAY_SZ] = {0, 1}, dest[ARRAY_SZ];'||chr(10)||''||chr(10)||'  int *p_dest = &dest[1];  // p points to beginning of dest[1];'||chr(10)||'  if (sizeof(dest[1] < sizeof(a)) {'||chr(10)||'    // report error and/exit with proper errno set'||chr(10)||'  }'||chr(10)||'  else '||chr(10)||'    memset(p_dest, a, sizeof(a)); // a + sizeof(a) will be one passed a[ARRAY_SZ]'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ARR38-C.msg_template', 'In ${se.filename}, line ${se.line} the library call ${se.func} where the memory pointed to plus the size parameters will cause an array out of bound access.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ARR38-C.msg_template', '???${se.filename}???${se.line}??????????????????${se.func}????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ARR38-C.name', 'Array out of bound with the use of library functions', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ARR38-C.name', '?????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
(select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ARR38-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ARR38-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ARR38-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ARR38-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ENV32-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV32-C', null, 'ENV32-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/ENV32-C.+All+exit+handlers+must+return+normally', '${rule.Xcalibyte.CERT.1.ENV32-C.name}', '2', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.ENV32-C.detail}', '${rule.Xcalibyte.CERT.1.ENV32-C.description}', '${rule.Xcalibyte.CERT.1.ENV32-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.ENV32-C.description', 'The program is using some exit handler that does not return normally.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV32-C.description', '???????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV32-C.detail', '#### Abstract'||chr(10)||'The program is using some exit handler that does not return normally.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'The following three functions _Exit(), exit(), quick_exit() are C standard exit functions. exit() and quick_exit() will call exit handlers atexit() and at_quick_exit() respectively for cleanup purposes, while _Exit() will not. User can define their own handlers and register with the system provided handlers. These exit handlers must return normally so that all exit handlers (thus all cleanup tasks) are properly performed. Furthermore, all exit handlers should not call exit().'||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'extern int file_opened;'||chr(10)||''||chr(10)||'int my_exit1(void)'||chr(10)||'{'||chr(10)||'  // some clean up code for opened file'||chr(10)||'  fprintf(stderr, "abnormal exit from my_exit1() with file closed\n");'||chr(10)||'  exit(0);  // this exit handler does not return normally. exit() is called the second time when value of file_opened is 1.'||chr(10)||'}'||chr(10)||''||chr(10)||'int my_exit2(void)'||chr(10)||'{'||chr(10)||'  if (file_opened == 1) {'||chr(10)||'      my_exit1();'||chr(10)||'  }'||chr(10)||'  fprintf(stderr, "abnormal exit with errno %d", errno());'||chr(10)||'  // other cleanup code'||chr(10)||'  // if file_opened is 1, the clean up code here will not be executed'||chr(10)||'  // ...'||chr(10)||'  return 1;'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  // ...'||chr(10)||'  if (atexit(my_exit2) != 0) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````'||chr(10)||'#### Example - Perfer'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'static int file_opened;'||chr(10)||''||chr(10)||'void my_exit1(void)'||chr(10)||'{'||chr(10)||'  // some clean up code for opened file'||chr(10)||'  fprintf(stderr, "abnormal exit from my_exit1() with file closed\n");'||chr(10)||'  exit(0);  // this exit handler does not return normally. exit() is called the second time when value of file_opened is 1.'||chr(10)||'}'||chr(10)||''||chr(10)||'void my_exit2(void)'||chr(10)||'{'||chr(10)||'  if (file_opened == 1) {'||chr(10)||'      my_exit1();'||chr(10)||'  }'||chr(10)||'  fprintf(stderr, "abnormal exit with errno %d", errno());'||chr(10)||'  // other cleanup code'||chr(10)||'  // if file_opened is 1, the clean up code here will not be executed'||chr(10)||'  // ...'||chr(10)||'  return;'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  // ...'||chr(10)||'  file_opened = 1;'||chr(10)||'  if (atexit(my_exit2) != 0) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV32-C.detail', '#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'??????????????????_Exit()???exit()???quick_exit()???C?????????????????????exit()???quick_exit()???????????????????????????????????????????????????atexit()???at_quick_exit()???_Exit()???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????exit()???'||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'static int file_opened;'||chr(10)||''||chr(10)||'void my_exit1(void)'||chr(10)||'{'||chr(10)||'  // some clean up code for opened file'||chr(10)||'  fprintf(stderr, "abnormal exit from my_exit1() with file closed\n");'||chr(10)||'  exit(0);  // this exit handler does not return normally. exit() is called the second time when value of file_opened is 1.'||chr(10)||'}'||chr(10)||''||chr(10)||'void my_exit2(void)'||chr(10)||'{'||chr(10)||'  if (file_opened == 1) {'||chr(10)||'      my_exit1();'||chr(10)||'  }'||chr(10)||'  fprintf(stderr, "abnormal exit with errno %d", errno());'||chr(10)||'  // other cleanup code'||chr(10)||'  // if file_opened is 1, the clean up code here will not be executed'||chr(10)||'  // ...'||chr(10)||'  return 1;'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  // ...'||chr(10)||'  file_opened = 1;'||chr(10)||'  if (atexit(my_exit2) != 0) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV32-C.msg_template', 'In ${se.filename}, ${se.func} is registered at line ${se.line} as exit handler. In ${ss.filename}, line ${ss.line}, the program will exit and not return normally.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV32-C.msg_template', '???${se.filename}??????${se.line}????????????${se.func} ????????????????????????????????????${ss.filename}??????${ss.line}???????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV32-C.name', 'Exit handlers must return normally', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV32-C.name', '????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV32-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV32-C'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV32-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV32-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ENV33-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV33-C', null, 'ENV33-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/pages/viewpage.action?pageId=87152177', '${rule.Xcalibyte.CERT.1.ENV33-C.name}', '1', '1', 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.ENV33-C.detail}', '${rule.Xcalibyte.CERT.1.ENV33-C.description}', '${rule.Xcalibyte.CERT.1.ENV33-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.ENV33-C.description', 'The program is calling the C standard system() function. When not properly protected, it could lead to various exploitations.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV33-C.description', '??????????????????C??????system()????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV33-C.detail', '#### Abstract'||chr(10)||'The program is calling the C standard system() function. When not properly protected, it could lead to various exploitations.'||chr(10)||'#### Explanation'||chr(10)||'The program is calling the system() function where the parameter is a character string. Such a string must be properly protected such as sanitized string, normalized path etc. Failure to do so will cause arbitrary program execution, privilege elevation or other unpredictable behavior.'||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'void func_call_sys(const char *in)'||chr(10)||'{'||chr(10)||'  // system() is called with a string "in" passed from outside of this function'||chr(10)||'  // There is no evidence that "in" has been sanitized'||chr(10)||'  // for example, if "in" is the string "rm *"'||chr(10)||'  // executing the system() call could be devastating'||chr(10)||'  system(in);'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - Prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'void func_call_sys(const char *in)'||chr(10)||'{'||chr(10)||'  // system() is called with a string "in" passed from outside of this function'||chr(10)||'  // There is no evidence that "in" has been sanitized'||chr(10)||'  // Sanitize input to system()'||chr(10)||'  // use full path name for directory for example'||chr(10)||'  sanitise(in);  // e.g. for file or directory name, do not include "../" substring'||chr(10)||'  system(in);'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV33-C.detail', '#### ??????'||chr(10)||'??????????????????C??????system()????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'???????????????????????????????????????system()????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'void func_call_sys(const char *in)'||chr(10)||'{'||chr(10)||'  // system() is called with a string "in" passed from outside of this function'||chr(10)||'  // There is no evidence that "in" has been sanitized'||chr(10)||'  // for example, if "in" is the string "rm *"'||chr(10)||'  // executing the system() call could be devastating'||chr(10)||'  system(in);'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example  - ??????'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'void func_call_sys(const char *in)'||chr(10)||'{'||chr(10)||'  // system() is called with a string "in" passed from outside of this function'||chr(10)||'  // There is no evidence that "in" has been sanitized'||chr(10)||'  // Sanitize input to system()'||chr(10)||'  // use full path name for directory for example'||chr(10)||'  sanitise(in);  // e.g. for file or directory name, do not include "../" substring'||chr(10)||'  system(in);'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV33-C.msg_template', 'In ${se.filename}, ${se.func}, system() at line ${se.line} with input parameter that has not been sanitized.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV33-C.msg_template', '???${se.filename}??????${se.line}??????$??????{se.func}??????system()?????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV33-C.name', 'Please pay attention to any direct call to the system()', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV33-C.name', '????????????system()???????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV33-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV33-C'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV33-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV33-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ERR33-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ERR33-C', null, 'ERR33-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/ERR33-C.+Detect+and+handle+standard+library+errors', '${rule.Xcalibyte.CERT.1.ERR33-C.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.ERR33-C.detail}', '${rule.Xcalibyte.CERT.1.ERR33-C.description}', '${rule.Xcalibyte.CERT.1.ERR33-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.ERR33-C.description', 'The program is calling the standard library function but failed to check and handle the function error returns.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR33-C.description', '?????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR33-C.detail', '#### Abstract'||chr(10)||'The program is calling the standard library function but failed to check and handle the function error returns.'||chr(10)||'#### Explanation'||chr(10)||'The program is calling standard library function(s). These functions typically return a valid value, or some form of value that indicates an error. Failure to check that the call is successful or failure may lead to unexpected or undefined behavior. Please refer to the language or system specification for a full description of the standard interface. Please note that there are system functions for which return values do not need to be checked. The list of these functions can also be found in system or language specification. Notably the more popular ones are printf, vprintf, memcpy, memmove, strcpy, strcat, memset (and their wide char versions).'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <string.h>'||chr(10)||''||chr(10)||'typedef struct {'||chr(10)||'  int len;'||chr(10)||'  int *data;'||chr(10)||'} vec_rec, *vec_rec_ptr;'||chr(10)||''||chr(10)||'enum { VEC_SZ = 32 };'||chr(10)||''||chr(10)||'vec_rec vr[VEC_SZ] = {  0 }; // initialize table content'||chr(10)||'vec_rec_ptr func_call_stdlib(int len, vec_rec_ptr in_vec)'||chr(10)||'{'||chr(10)||'  vec_rec_ptr vrp = (vec_rec_ptr)(malloc(sizeof(vec_rec) * len));'||chr(10)||'  // vrp may be NULL if malloc failed'||chr(10)||'  // this code did not check that vrp may be invalid'||chr(10)||''||chr(10)||'  // this memcpy may enable attacker to access memory causing remote code execution'||chr(10)||'  memcpy(vrp, in_vec, len);'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR33-C.detail', '#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????printf???vprintf???memcpy???memmove???strcpy???strcat???memset?????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <string.h>'||chr(10)||''||chr(10)||'typedef struct {'||chr(10)||'  int len;'||chr(10)||'  int *data;'||chr(10)||'} vec_rec, *vec_rec_ptr;'||chr(10)||''||chr(10)||'enum { VEC_SZ = 32 };'||chr(10)||''||chr(10)||'vec_rec vr[VEC_SZ] = {  0 }; // initialize table content'||chr(10)||'vec_rec_ptr func_call_stdlib(int len, vec_rec_ptr in_vec)'||chr(10)||'{'||chr(10)||'  vec_rec_ptr vrp = (vec_rec_ptr)(malloc(sizeof(vec_rec) * len));'||chr(10)||'  // vrp may be NULL if malloc failed'||chr(10)||'  // this code did not check that vrp may be invalid'||chr(10)||''||chr(10)||'  // this memcpy may enable attacker to access memory causing remote code execution'||chr(10)||'  memcpy(vrp, in_vec, len);'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR33-C.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line}, the system library call ${se.var} is used without checking for error return.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR33-C.msg_template', '???${se.filename}??????${se.line}?????? ${se.func}?????????????????????????????????????????????????????????${se.var}???', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR33-C.name', 'Please check and handle standard library return errors', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR33-C.name', '???????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR33-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR33-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR33-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR33-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

-- ------------------------
-- EXP34-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'EXP34-C', null, 'EXP34-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/EXP34-C.+Do+not+dereference+null+pointers', '${rule.Xcalibyte.CERT.1.EXP34-C.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.EXP34-C.detail}', '${rule.Xcalibyte.CERT.1.EXP34-C.description}', '${rule.Xcalibyte.CERT.1.EXP34-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.EXP34-C.description', 'The program is accessing illegal memory through a pointer with value. This vulnerability is the same as the Xcalibyte NPD rule.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.EXP34-C.description', '????????????????????????????????????????????????????????????????????????NPD???????????????', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.EXP34-C.detail', '#### ??????'||chr(10)||'????????????????????????????????????????????????????????????????????????NPD???????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????NPD??????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern int bar(int);'||chr(10)||''||chr(10)||'int assign(int* a)'||chr(10)||'{'||chr(10)||'  int i = bar(*a) // dereference a'||chr(10)||'  return i;'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo(void)'||chr(10)||'{'||chr(10)||'  int *p = 0;  // p as a pointer has been initialized to 0 (null)'||chr(10)||'  assign(p);'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### ?????? - Java'||chr(10)||'````text'||chr(10)||'class User {'||chr(10)||'  public String getName() {'||chr(10)||'    return null;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'class Bar {'||chr(10)||'  private User findUser(String uid) {'||chr(10)||'    if (user.containsKey(uid)) {'||chr(10)||'      return user.get(uid);'||chr(10)||'    }'||chr(10)||'    else'||chr(10)||'      return null;'||chr(10)||'  }'||chr(10)||'  public void Npd(String uid) {'||chr(10)||'    // do something'||chr(10)||'    // ...'||chr(10)||'    User user = findUser(uid); // Throws NPE if "user" has not been properly initialized'||chr(10)||'    String getName() {'||chr(10)||'      return null;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
 ('en', 'rule.Xcalibyte.CERT.1.EXP34-C.detail', '#### Abstract'||chr(10)||'The program is accessing illegal memory through a pointer with value. This vulnerability is the same as the Xcalibyte NPD rule.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Please refer to rule NPD in the Xcalibyte vulnerability list for detailed explanation.'||chr(10)||''||chr(10)||'#### Example - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern int bar(int);'||chr(10)||''||chr(10)||'int assign(int* a)'||chr(10)||'{'||chr(10)||'  int i = bar(*a) // dereference a'||chr(10)||'  return i;'||chr(10)||'}'||chr(10)||''||chr(10)||'int foo(void)'||chr(10)||'{'||chr(10)||'  int *p = 0;  // p as a pointer has been initialized to 0 (null)'||chr(10)||'  assign(p);'||chr(10)||'}'||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - Java'||chr(10)||'````text'||chr(10)||'class User {'||chr(10)||'  public String getName() {'||chr(10)||'    return null;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'class Bar {'||chr(10)||'  private User findUser(String uid) {'||chr(10)||'    if (user.containsKey(uid)) {'||chr(10)||'      return user.get(uid);'||chr(10)||'    }'||chr(10)||'    else'||chr(10)||'      return null;'||chr(10)||'  }'||chr(10)||'  public void Npd(String uid) {'||chr(10)||'    // do something'||chr(10)||'    // ...'||chr(10)||'    User user = findUser(uid); // Throws NPE if "user" has not been properly initialized'||chr(10)||'    String getName() {'||chr(10)||'      return null;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.EXP34-C.msg_template', 'In ${se.filename}, line ${se.line}, a NPD (EXP34) defect has been detected in ${se.func} for ${se.var}. The ${se.var} has value "0" in ${ss.filename} at line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.EXP34-C.msg_template', '???${se.filename}??????${se.line}??????${se.func} ????????????${se.var} ????????????NPD(EXP34)???????????????${se.var}???${ss.filename} ???${ss.line}??? ???"0"?????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.EXP34-C.name', 'Do not perform a dereference using a null pointer', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.EXP34-C.name', '????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='EXP34-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='EXP34-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='EXP34-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='EXP34-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

-- ------------------------
-- FIO30-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO30-C', null, 'FIO30-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/FIO30-C.+Exclude+user+input+from+format+strings', '${rule.Xcalibyte.CERT.1.FIO30-C.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO30-C.detail}', '${rule.Xcalibyte.CERT.1.FIO30-C.description}', '${rule.Xcalibyte.CERT.1.FIO30-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO30-C.description', 'The program has a format specification that contains a parameter of char * type (aka string) with an untrusted source and is considered tainted.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO30-C.description', '????????????????????????????????????????????????????????????char *??????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO30-C.detail', '#### Abstract'||chr(10)||'The program has a format specification that contains a parameter of char * type (aka string) with an untrusted source and is considered tainted.'||chr(10)||'#### Explanation'||chr(10)||'Formatted I/O functions can be tricked into overriding or reading the value of any arbitrary memory location. When the input source is untrusted, it is better not to use format strings and related functions to perform I/O operation.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'int main(int argc, char *argv[])'||chr(10)||'{'||chr(10)||'  int var_to_hold_content;  // the variable where printf will store content of an address'||chr(10)||'  if (argc > 2) {'||chr(10)||'      scanf(argv[1]);  // get a string from stdin (user input)'||chr(10)||'      printf(argv[1]); // user can read any address to a variable using a format specification'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - Prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'int main(int argc, char *argv[])'||chr(10)||'{'||chr(10)||'  int var_to_hold_content;  // the variable where printf will store content of an address'||chr(10)||'  if (argc > 2) {'||chr(10)||'      scanf(argv[1]);  // get a string from stdin (user input)'||chr(10)||'      fputs(argv[1, stdout]); // do not use printf with format specification'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO30-C.detail', '#### ??????'||chr(10)||'????????????????????????????????????????????????????????????char *??????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'?????????????????????I/O???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????I/O?????????'||chr(10)||''||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'int main(int argc, char *argv[])'||chr(10)||'{'||chr(10)||'  int var_to_hold_content;  // the variable where printf will store content of an address'||chr(10)||'  if (argc > 2) {'||chr(10)||'      scanf(argv[1]);  // get a string from stdin (user input)'||chr(10)||'      printf(argv[1]); // user can read any address to a variable using a format specification'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - ??????'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'int main(int argc, char *argv[])'||chr(10)||'{'||chr(10)||'  int var_to_hold_content;  // the variable where printf will store content of an address'||chr(10)||'  if (argc > 2) {'||chr(10)||'      scanf(argv[1]);  // get a string from stdin (user input)'||chr(10)||'      fputs(argv[1, stdout]); // do not use printf with format specification'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO30-C.msg_template', 'In ${se.filename}, ${se.func}, line ${se.line} the printf family of calls were invoked at ${se.var} with format string from user input without being sanitized.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO30-C.msg_template', '???${se.filename}??????${se.line}????????????${se.func}??????${se.var}?????????printf??????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO30-C.name', 'Format specifications should not have parameters that are strings from an untrusted source', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO30-C.name', '?????????????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO30-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO30-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO30-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO30-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO30-C'),
 'STANDARD','OWASP','01')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FIO34-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO34-C', null, 'FIO34-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/FIO34-C.+Distinguish+between+characters+read+from+a+file+and+EOF+or+WEOF', '${rule.Xcalibyte.CERT.1.FIO34-C.name}', '1', '1', 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO34-C.detail}', '${rule.Xcalibyte.CERT.1.FIO34-C.description}', '${rule.Xcalibyte.CERT.1.FIO34-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO34-C.description', 'The program is using return value of file read (such as getchar(), getc(), getwc(),..) to check for EOF/WEOF as end of file read operation.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO34-C.description', '????????????????????????????????????getchar()???getc()???getwc()......????????????????????????EOF/WEOF??????????????????????????????', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO34-C.detail', '#### ??????'||chr(10)||'????????????????????????????????????getchar()???getc()???getwc()......????????????????????????EOF/WEOF??????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'??????????????????????????????????????????int????????????????????????????????????EOF/WEOF???????????????????????????????????????EOF???WEOF????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'void read_char(void)'||chr(10)||'{'||chr(10)||'  FILE *fp = fopen("/tmp/myfile", "+r");'||chr(10)||'  // check fp valid'||chr(10)||'  ...'||chr(10)||''||chr(10)||'  int c = getc(fp);'||chr(10)||'  while (c != EOF)  // this check does not guarantee read has reached end of file'||chr(10)||'  {'||chr(10)||'    putchar(c);   // echo what is read in'||chr(10)||'    c = getc(fp);'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### ?????? - ??????'||chr(10)||'````````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'void read_char(void)'||chr(10)||'{'||chr(10)||'  FILE *fp = fopen("/tmp/myfile", "+r");'||chr(10)||'  // check fp valid'||chr(10)||'  ...'||chr(10)||''||chr(10)||'  int c = getc(fp);'||chr(10)||''||chr(10)||'  // make sure it is really an EOF character and that the EOF is due to end-of-file  '||chr(10)||'  while (c != EOF || (feof(stdin) && !ferror(stdin))    '||chr(10)||'  {'||chr(10)||'    putchar(c);   // echo what is read in'||chr(10)||'    c = getc(fp);'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO34-C.detail', '#### Abstract'||chr(10)||'The program is using return value of file read (such as getchar(), getc(), getwc(),..) to check for EOF/WEOF as end of file read operation.'||chr(10)||'#### Explanation'||chr(10)||'The functions that reads characters from a file return an int type. Directly comparing the character read with EOF/WEOF may cause an unexpected result because EOF and WEOF are implementation defined.'||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'void read_char(void)'||chr(10)||'{'||chr(10)||'  FILE *fp = fopen("/tmp/myfile", "+r");'||chr(10)||'  // check fp valid'||chr(10)||'  ...'||chr(10)||''||chr(10)||'  int c = getc(fp);'||chr(10)||'  while (c != EOF)  // this check does not guarantee read has reached end of file'||chr(10)||'  {'||chr(10)||'    putchar(c);   // echo what is read in'||chr(10)||'    c = getc(fp);'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````'||chr(10)||'#### Example - Prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'void read_char(void)'||chr(10)||'{'||chr(10)||'  FILE *fp = fopen("/tmp/myfile", "+r");'||chr(10)||'  // check fp valid'||chr(10)||'  ...'||chr(10)||''||chr(10)||'  int c = getc(fp);'||chr(10)||''||chr(10)||'  // make sure it is really an EOF character and that the EOF is due to end-of-file '||chr(10)||'  while (c != EOF || (feof(stdin) && !ferror(stdin))  '||chr(10)||'  {'||chr(10)||'    putchar(c);   // echo what is read in'||chr(10)||'    c = getc(fp);'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO34-C.msg_template', 'In ${se.filename}, ${se.func}, line ${se.line} the I/O call return is being checked against EOF (${ss.var}) with the wrong type at line ${ss.line} in ${ss.func}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO34-C.msg_template', '???${se.filename}??????${se.line}????????????${se.func}??????I/O?????????????????????${ss.line}????????????${ss.func} ?????????????????????EOF(${ss.var})?????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO34-C.name', 'EOF or WEOF as "char" size objects are different from any "char" read from a file', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO34-C.name', '??????"char"???????????????EOF???WEOF????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO34-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO34-C'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO34-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO34-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO34-C'),
 'STANDARD','OWASP','01')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FIO37-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO37-C', null, 'FIO37-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/FIO37-C.+Do+not+assume+that+fgets%28%29+or+fgetws%28%29+returns+a+nonempty+string+when+successful', '${rule.Xcalibyte.CERT.1.FIO37-C.name}', '1', '1', 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO37-C.detail}', '${rule.Xcalibyte.CERT.1.FIO37-C.description}', '${rule.Xcalibyte.CERT.1.FIO37-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO37-C.description', 'The program is calling fgets() or fgetws() and assuming the string returned an non-empty string.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO37-C.description', '??????????????????fgets()???fgetws()???????????????????????????????????????????????????', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO37-C.detail', '#### ??????'||chr(10)||'??????????????????fgets()???fgetws()???????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'fgets() ??? fgetws()???????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'void read_string(void)'||chr(10)||'{'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||'  FILE *fp = fopen("/myfile", "+r");'||chr(10)||'  // check fp valid'||chr(10)||'  // ...'||chr(10)||''||chr(10)||'  if (fgets(buf, BUF_SZ, fp) != 0) {'||chr(10)||'    // if the first character of buf is ''\0'', a large number will be printed'||chr(10)||'    printf("Size of string read = %d", strlen(buf)-1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - ??????'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'void read_string(void)'||chr(10)||'{'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||'  FILE *fp = fopen("/myfile", "+r");'||chr(10)||'  // check fp valid'||chr(10)||'  // ...'||chr(10)||''||chr(10)||'  if (fgets(buf, BUF_SZ, fp) != 0) {'||chr(10)||'    // replace newline character if it is there'||chr(10)||'    char *pchar = strchr(buf, ''\n'');'||chr(10)||'    if (pchar) *pchar = ''\0'';'||chr(10)||''||chr(10)||'    printf("Size of string read = %d", strlen(buf)-1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO37-C.detail', '#### Abstract'||chr(10)||'The program is calling fgets() or fgetws() and assuming the string returned an non-empty string.'||chr(10)||'#### Explanation'||chr(10)||'The two functions may return a null string (e.g. the file may be a binary file).'||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'void read_string(void)'||chr(10)||'{'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||'  FILE *fp = fopen("/myfile", "+r");'||chr(10)||'  // check fp valid'||chr(10)||'  // ...'||chr(10)||''||chr(10)||'  if (fgets(buf, BUF_SZ, fp) != 0) {'||chr(10)||'    // if the first character of buf is ''\0'', a large number will be printed'||chr(10)||'    printf("Size of string read = %d", strlen(buf)-1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||'````'||chr(10)||'#### Example - Prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'void read_string(void)'||chr(10)||'{'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||'  FILE *fp = fopen("/myfile", "+r");'||chr(10)||'  // check fp valid'||chr(10)||'  // ...'||chr(10)||''||chr(10)||'  if (fgets(buf, BUF_SZ, fp) != 0) {'||chr(10)||'    // replace newline character if it is there'||chr(10)||'    char *pchar = strchr(buf, ''\n'');'||chr(10)||'    if (pchar) *pchar = ''\0'';'||chr(10)||''||chr(10)||'    printf("Size of string read = %d", strlen(buf)-1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO37-C.msg_template', 'In ${se.filename}, ${se.func}, line ${se.line} has an illegal access due to empty string although the string function in ${ss.filename}, line ${ss.line} returns successfully.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO37-C.msg_template', '???${se.filename}??????${se.line}????????????${se.func}??????????????????????????????????????????????????????${ss.filename},???${ss.line}????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO37-C.name', 'fgets() and fgetws() functions may return an empty string', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO37-C.name', 'fgets()???fgetws()????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO37-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO37-C'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO37-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO37-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FIO42-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO42-C', null, 'FIO42-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/FIO42-C.+Close+files+when+they+are+no+longer+needed', '${rule.Xcalibyte.CERT.1.FIO42-C.name}', '2', '3', 'UNLIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO42-C.detail}', '${rule.Xcalibyte.CERT.1.FIO42-C.description}', '${rule.Xcalibyte.CERT.1.FIO42-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO42-C.description', 'The program has opened a file but failed to close the file when done with it or before the program terminates.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO42-C.description', '??????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO42-C.detail', '#### Abstract'||chr(10)||'The program has opened a file but failed to close the file when done with it or before the program terminates.'||chr(10)||'#### Explanation'||chr(10)||'It is important to close a file when it is no longer needed. Failure to do so may expose resource (such as non-flushed data) associated with the file to attackers.'||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||'````'||chr(10)||'text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'int open_file(const char *fname)'||chr(10)||'{'||chr(10)||'  FILE *fp = fopen(fname, "+r");'||chr(10)||'  if (fp == NULL) return -1;'||chr(10)||''||chr(10)||'  // do something'||chr(10)||'  return 0;       // file is not properly closed on return'||chr(10)||''||chr(10)||'}'||chr(10)||'````'||chr(10)||'#### Example - Prefer'||chr(10)||'````'||chr(10)||'text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'int open_file(const char *fname)'||chr(10)||'{'||chr(10)||'  FILE *fp = fopen(fname, "+r");'||chr(10)||'  if (fp == NULL) return -1;'||chr(10)||''||chr(10)||'  // do something'||chr(10)||''||chr(10)||'  if (fclose(fp) == EOF) {'||chr(10)||'    // handle error'||chr(10)||'    ...'||chr(10)||'  }'||chr(10)||'  return 0;       // file is properly closed on return'||chr(10)||''||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO42-C.detail', '#### ??????'||chr(10)||'??????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||''||chr(10)||'````'||chr(10)||'text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'int open_file(const char *fname)'||chr(10)||'{'||chr(10)||'  FILE *fp = fopen(fname, "+r");'||chr(10)||'  if (fp == NULL) return -1;'||chr(10)||''||chr(10)||'  // do something'||chr(10)||'  return 0;       // file is not properly closed on return'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### ?????? - ??????'||chr(10)||''||chr(10)||'````'||chr(10)||'text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'int open_file(const char *fname)'||chr(10)||'{'||chr(10)||'  FILE *fp = fopen(fname, "+r");'||chr(10)||'  if (fp == NULL) return -1;'||chr(10)||''||chr(10)||'  // do something'||chr(10)||''||chr(10)||'  if (fclose(fp) == EOF) {'||chr(10)||'    // handle error'||chr(10)||'    ...'||chr(10)||'  }'||chr(10)||'  return 0;       // file is properly closed on return'||chr(10)||''||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO42-C.msg_template', 'In ${se.filename} function ${se.func} line ${se.line},  File ${se.var} file not closed. The file was opened in ${ss.filenameat}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO42-C.msg_template', '???${se.file} ???${se.line}??????????????? ${se.func}?????????${se.var}????????????. ????????????${ss.filename}, ???${ss.line}????????????????????????????????????.', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO42-C.name', 'Close files when they are no longer required', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO42-C.name', '???????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO42-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO42-C'),
 'BASIC','PRIORITY','4'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO42-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO42-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FIO45-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO45-C', null, 'FIO45-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/FIO45-C.+Avoid+TOCTOU+race+conditions+while+accessing+files', '${rule.Xcalibyte.CERT.1.FIO45-C.name}', '1', '2', 'PROBABLE', 'HIGH', '${rule.Xcalibyte.CERT.1.FIO45-C.detail}', '${rule.Xcalibyte.CERT.1.FIO45-C.description}', '${rule.Xcalibyte.CERT.1.FIO45-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO45-C.description', 'Race conditions while accessing files (Time of check, time of use) may happen and should b avoided', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO45-C.description', '??????????????????Time of check???time of use???????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO45-C.detail', '#### Abstract'||chr(10)||'Race conditions while accessing files (Time of check, time of use) may happen and should b avoided'||chr(10)||'#### Explanation'||chr(10)||'In a shared file system, two or more processes may access the same file, causing race condition is possible. Attackers can change the file between two accesses by different processes. Or replace the file (symbolic or hard linked) to a different file.'||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h> '||chr(10)||'#include <unistd.h>'||chr(10)||'#include <sys/types.h>'||chr(10)||'#include <fcntl.h>'||chr(10)||''||chr(10)||'int file_s1(char *p, char *q)'||chr(10)||'{'||chr(10)||'  struct stat statBefore, statAfter;'||chr(10)||''||chr(10)||'  lstat("/tmp/x", &statBefore);'||chr(10)||'  FILE *f = fopen("/tmp/x", "w");'||chr(10)||'  lstat("/tmp/x", &statAfter);'||chr(10)||'  if (statAfter.st_ino == statBefore.st_ino) {'||chr(10)||'    if (f != NULL)'||chr(10)||'      fwrite("HELLO!", 1, 5, f);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  fclose(f);'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO45-C.detail', '#### ??????'||chr(10)||'??????????????????Time of check???time of use???????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'#include <stdio.h> '||chr(10)||'#include <unistd.h>'||chr(10)||'#include <sys/types.h>'||chr(10)||'#include <fcntl.h>'||chr(10)||''||chr(10)||'int file_s1(char *p, char *q)'||chr(10)||'{'||chr(10)||'  struct stat statBefore, statAfter;'||chr(10)||''||chr(10)||'  lstat("/tmp/x", &statBefore);'||chr(10)||'  FILE *f = fopen("/tmp/x", "w");'||chr(10)||'  lstat("/tmp/x", &statAfter);'||chr(10)||'  if (statAfter.st_ino == statBefore.st_ino) {'||chr(10)||'    if (f != NULL)'||chr(10)||'      fwrite("HELLO!", 1, 5, f);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  fclose(f);'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO45-C.msg_template', 'In ${se.filename} function ${se.func}, line ${se.line}, the file ${se.var} has TOCTOU problem. The file was opened at ${ss.filename} line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO45-C.msg_template', '???${se.filename} ???${se.line}???????????? ${se.func}?????????${se.var}???TOCTOU??????????????????????????? ${ss.filename}, ???${ss.line}????????????.', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO45-C.name', 'When accessing files, race conditions may exist and should be avoided', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO45-C.name', '???????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO45-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO45-C'),
 'BASIC','PRIORITY','6'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO45-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO45-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FIO47-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO47-C', null, 'FIO47-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/FIO47-C.+Use+valid+format+strings', '${rule.Xcalibyte.CERT.1.FIO47-C.name}', '1', '2', 'UNLIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO47-C.detail}', '${rule.Xcalibyte.CERT.1.FIO47-C.description}', '${rule.Xcalibyte.CERT.1.FIO47-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO47-C.description', '<< FIX ME >>', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO47-C.description', '<< FIX ME >>', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO47-C.detail', '### NYI'||chr(10)||'#### Abstract'||chr(10)||'FIX ME'||chr(10)||'#### Explanation'||chr(10)||'FIX ME'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'FIX ME'||chr(10)||''||chr(10)||'#### Template Desc'||chr(10)||''||chr(10)||'FIX ME'||chr(10)||'', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO47-C.detail', '### NYI'||chr(10)||'#### Abstract'||chr(10)||'FIX ME'||chr(10)||'#### Explanation'||chr(10)||'FIX ME'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'FIX ME'||chr(10)||''||chr(10)||'#### Template Desc'||chr(10)||''||chr(10)||'FIX ME'||chr(10)||'', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO47-C.msg_template', '<< FIX ME >>', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO47-C.msg_template', '<< FIX ME >>', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO47-C.name', '<< NYI >>', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO47-C.name', '<< NYI >>', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO47-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO47-C'),
 'BASIC','PRIORITY','6'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO47-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO47-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO47-C'),
 'STANDARD','OWASP','01'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO47-C'),
 'STANDARD','OWASP','04')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO47-C'),
 'BASIC','NYI','TRUE')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MEM30-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MEM30-C', null, 'MEM30-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/MEM30-C.+Do+not+access+freed+memory', '${rule.Xcalibyte.CERT.1.MEM30-C.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.MEM30-C.detail}', '${rule.Xcalibyte.CERT.1.MEM30-C.description}', '${rule.Xcalibyte.CERT.1.MEM30-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MEM30-C.description', 'The program is accessing memory that has been freed. This vulnerability is the same as the Xcalibyte UAF rule.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM30-C.description', '????????????????????????????????????????????????????????????UAF???????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM30-C.detail', '#### Abstract'||chr(10)||'The program is accessing memory that has been freed. This vulnerability is the same as the Xcalibyte UAF rule.'||chr(10)||'#### Explanation'||chr(10)||'Please refer to rule UAF in Xcalibyte vulnerability list for detailed explanation and example.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'int g = 2;'||chr(10)||''||chr(10)||'void my_free(void *p) {'||chr(10)||'  if (p != NULL)'||chr(10)||'    free(p);  // free p'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int i, j, *p, *q;'||chr(10)||'  p = malloc(10 * sizeof(int));'||chr(10)||'  if (p == NULL)'||chr(10)||'    return 1;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    p[i] = i;'||chr(10)||'  q = p;'||chr(10)||'  my_free(p);  //  p is freed'||chr(10)||'  j = 0;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    j += q[i]; // Use after free here (note the statement q = p), read of q[i] is illegal'||chr(10)||'  return j;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM30-C.detail', '#### ??????'||chr(10)||'????????????????????????????????????????????????????????????UAF???????????????'||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????UAF??????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'int g = 2;'||chr(10)||''||chr(10)||'void my_free(void *p) {'||chr(10)||'  if (p != NULL)'||chr(10)||'    free(p);  // free p'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() {'||chr(10)||'  int i, j, *p, *q;'||chr(10)||'  p = malloc(10 * sizeof(int));'||chr(10)||'  if (p == NULL)'||chr(10)||'    return 1;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    p[i] = i;'||chr(10)||'  q = p;'||chr(10)||'  my_free(p);  //  p is freed'||chr(10)||'  j = 0;'||chr(10)||'  for (i=0; i < 10; ++i)'||chr(10)||'    j += q[i]; // Use after free here (note the statement q = p), read of q[i] is illegal'||chr(10)||'  return j;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM30-C.msg_template', 'In ${se.filename}, line ${se.line}, the variable ${se.var} in ${se.func} was used, however, it has been freed at line ${ss.line} in ${ss.filename}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM30-C.msg_template', '???${se.filename}??????${se.line}??????????????? ${se.func} ????????????${se.var}??????????????????${ss.filename} ???${ss.line}???????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM30-C.name', 'Freed memory should not be accessed or used again', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM30-C.name', '?????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM30-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM30-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM30-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM30-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MEM35-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MEM35-C', null, 'MEM35-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/MEM35-C.+Allocate+sufficient+memory+for+an+object', '${rule.Xcalibyte.CERT.1.MEM35-C.name}', '1', '2', 'PROBABLE', 'HIGH', '${rule.Xcalibyte.CERT.1.MEM35-C.detail}', '${rule.Xcalibyte.CERT.1.MEM35-C.description}', '${rule.Xcalibyte.CERT.1.MEM35-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MEM35-C.description', 'The program has used malloc family of functions to allocate memory that is smaller than the memory required by the program.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM35-C.description', '??????????????????malloc????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM35-C.detail', '#### Abstract'||chr(10)||'The program has used malloc family of functions to allocate memory that is smaller than the memory required by the program.'||chr(10)||'#### Explanation'||chr(10)||'The parameter corresponding to size arguments to the malloc functions should have sufficient range to represent the size of objects to be stored. Failure to do so will cause a buffer overflow leading to unexpected program behavior.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'typedef struct {'||chr(10)||'  size_t len;'||chr(10)||'  int   *data;'||chr(10)||'} vect_record, *vect_ptr;'||chr(10)||''||chr(10)||''||chr(10)||'vect_record *vector_new(size_t len)'||chr(10)||'{'||chr(10)||'  vect_record *vect;'||chr(10)||'  vect_ptr result = (vect_ptr)malloc(sizeof(vect));'||chr(10)||'  if (result == NULL) {'||chr(10)||'    // handle error and return'||chr(10)||'    return NULL;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // initialize vector'||chr(10)||'  result[0].len = 1;'||chr(10)||'  result[1].len = 2;'||chr(10)||'  // ...'||chr(10)||'  return result;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM35-C.detail', '#### ??????'||chr(10)||'??????????????????malloc????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'??????malloc??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'typedef struct {'||chr(10)||'  size_t len;'||chr(10)||'  int   *data;'||chr(10)||'} vect_record, *vect_ptr;'||chr(10)||''||chr(10)||''||chr(10)||'vect_record *vector_new(size_t len)'||chr(10)||'{'||chr(10)||'  vect_record *vect;'||chr(10)||'  vect_ptr result = (vect_ptr)malloc(sizeof(vect));'||chr(10)||'  if (result == NULL) {'||chr(10)||'    // handle error and return'||chr(10)||'    return NULL;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // initialize vector'||chr(10)||'  result[0].len = 1;'||chr(10)||'  result[1].len = 2;'||chr(10)||'  // ...'||chr(10)||'  return result;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM35-C.msg_template', 'In ${se.filename}, ${se.func}, line ${se.line} is access memory outside of range allocated. The memory has been allocated in ${ss.func} at line ${ss.line},', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM35-C.msg_template', '???${se.filename}??????${se.line}??????${se.func} ?????????????????????????????????????????????????????????????????????${ss.line}?????? ${ss.func}??????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM35-C.name', 'Make sure sufficient memory has been allocated for an object', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM35-C.name', '??????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM35-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM35-C'),
 'BASIC','PRIORITY','6'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM35-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM35-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;



-- ------------------------
-- MEM36-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MEM36-C', null, 'MEM36-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/pages/viewpage.action?pageId=87152255', '${rule.Xcalibyte.CERT.1.MEM36-C.name}', '3', '3', 'PROBABLE', 'HIGH', '${rule.Xcalibyte.CERT.1.MEM36-C.detail}', '${rule.Xcalibyte.CERT.1.MEM36-C.description}', '${rule.Xcalibyte.CERT.1.MEM36-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MEM36-C.description', 'The program has used realloc() that causes a pointer with alignment which is less restrictive than before the memory reallocation.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM36-C.description', '??????????????????realloc()???????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM36-C.detail', '### MEM36 (NYI)'||chr(10)||'#### Abstract'||chr(10)||'The program has used realloc() that causes a pointer with alignment which is less restrictive than before the memory reallocation.'||chr(10)||'#### Explanation'||chr(10)||'realloc() will allocate new memory and set the previously allocated memory pointer to the newly allocated area. If the previously allocated memory has more restricted alignment (e.g. 16B), the newly pointed to memory will not satisfy this restriction resulting in unexpected program behavior.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'int *vector_calc(size_t vec_sz, int elements)'||chr(10)||'{'||chr(10)||'  size_t align_vec = 1 << vec_sz;'||chr(10)||'  int *ptr_align;'||chr(10)||''||chr(10)||'  if ((ptr_align = (int *)aligned_alloc(align_vec, sizeof(int)*elements)) == NULL) {'||chr(10)||'    // handle error and return;'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // do something with vector objects pointed to by ptr_align'||chr(10)||'  // ...'||chr(10)||'  if ((ptr1 = (int *) realloc(ptr_align, sizeof(int)*elements * 2)) == NULL) {'||chr(10)||'    // handle error and return'||chr(10)||'    //...'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // do something that uses ptr1'||chr(10)||'  // but ptr1 is not guaranteed to properly aligned with the vector objects previous allocated'||chr(10)||''||chr(10)||'  return ptr1;'||chr(10)||''||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM36-C.detail', '### MEM36???NYI???'||chr(10)||'#### ??????'||chr(10)||'??????????????????realloc()???????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'realloc()?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????16byte???????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'int *vector_calc(size_t vec_sz, int elements)'||chr(10)||'{'||chr(10)||'  size_t align_vec = 1 << vec_sz;'||chr(10)||'  int *ptr_align;'||chr(10)||''||chr(10)||'  if ((ptr_align = (int *)aligned_alloc(align_vec, sizeof(int)*elements)) == NULL) {'||chr(10)||'    // handle error and return;'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // do something with vector objects pointed to by ptr_align'||chr(10)||'  // ...'||chr(10)||'  if ((ptr1 = (int *) realloc(ptr_align, sizeof(int)*elements * 2)) == NULL) {'||chr(10)||'    // handle error and return'||chr(10)||'    //...'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // do something that uses ptr1'||chr(10)||'  // but ptr1 is not guaranteed to properly aligned with the vector objects previous allocated'||chr(10)||''||chr(10)||'  return ptr1;'||chr(10)||''||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM36-C.msg_template', '<< FIX ME >>', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM36-C.msg_template', '<< FIX ME >>', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM36-C.name', '<< NYI >> Calling realloc() may cause misalignment for previously aligned object', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM36-C.name', '<< NYI >> ??????realloc()?????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM36-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM36-C'),
 'BASIC','PRIORITY','2'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM36-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM36-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM36-C'),
 'BASIC','NYI','TRUE')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MSC32-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC32-C', null, 'MSC32-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/MSC32-C.+Properly+seed+pseudorandom+number+generators', '${rule.Xcalibyte.CERT.1.MSC32-C.name}', '2', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.MSC32-C.detail}', '${rule.Xcalibyte.CERT.1.MSC32-C.description}', '${rule.Xcalibyte.CERT.1.MSC32-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MSC32-C.description', 'The program is using a pseudo random number generator with seeding (initial state) that will produce a deterministic sequence of numbers.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC32-C.description', '???????????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC32-C.detail', '#### Abstract'||chr(10)||'The program is using a pseudo random number generator with seeding (initial state) that will produce a deterministic sequence of numbers.'||chr(10)||'#### Explanation'||chr(10)||'A properly seeded PRNG will generate a different number sequence each time it is run (e.g. call srandom() before invoking the random function). This prevents potential attackers to predict the number sequence generated. Or use random number generators that cannot be seeded.'||chr(10)||''||chr(10)||'#### Example 1'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'// this function calls random() withouth proper seeding it'||chr(10)||'// output of this function will be the same each time it is called'||chr(10)||'void print_rand(void)'||chr(10)||'{'||chr(10)||'  for (int i = 0; i < 20; i++) {'||chr(10)||'    printf("%lx ", random());'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC32-C.detail', '#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'????????????????????????PRNG????????????????????????????????????????????????????????????????????????random???????????????srandom()?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? 1'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'// this function calls random() withouth proper seeding it'||chr(10)||'// output of this function will be the same each time it is called'||chr(10)||'void print_rand(void)'||chr(10)||'{'||chr(10)||'  for (int i = 0; i < 20; i++) {'||chr(10)||'    printf("%lx ", random());'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC32-C.msg_template', 'In ${se.filename}, ${se.func}, line ${se.line} is calling "${se.var}". This generator will produce a deterministic sequence.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC32-C.msg_template', '???${se.filename}??????${se.line}????????????${se.func}??????????????????"${se.var}"?????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC32-C.name', 'Pseudo-Random number generators should be properly seeded before use', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC32-C.name', '????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC32-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC32-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC32-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC32-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;



-- ------------------------
-- MSC33-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC33-C', null, 'MSC33-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/MSC33-C.+Do+not+pass+invalid+data+to+the+asctime%28%29+function', '${rule.Xcalibyte.CERT.1.MSC33-C.name}', '1', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.MSC33-C.detail}', '${rule.Xcalibyte.CERT.1.MSC33-C.description}', '${rule.Xcalibyte.CERT.1.MSC33-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MSC33-C.description', 'The program is calling asctime() function, however the input parameter for that function is from an untrusted source.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC33-C.description', '??????????????????asctime()??????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC33-C.detail', '#### Abstract'||chr(10)||'The program is calling asctime() function, however the input parameter for that function is from an untrusted source.'||chr(10)||'#### Explanation'||chr(10)||'The function asctime() does not validate the value or range of its input parameter. Subsequent use of result from this function may cause buffer overflow and other security violations when trying to print to a string.'||chr(10)||''||chr(10)||'#### Example 1'||chr(10)||'````text'||chr(10)||'#include <time.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||''||chr(10)||'void read_time(struct tm *time_info)'||chr(10)||'{'||chr(10)||'  char *time = asctime(time_info);'||chr(10)||'  printf("time is %s\n", time);'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC33-C.detail', '#### ??????'||chr(10)||'??????????????????asctime()??????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'??????asctime()????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? 1'||chr(10)||'````text'||chr(10)||'#include <time.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||''||chr(10)||'void read_time(struct tm *time_info)'||chr(10)||'{'||chr(10)||'  char *time = asctime(time_info);'||chr(10)||'  printf("time is %s\n", time_info);'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC33-C.msg_template', 'In ${se.filename}, line ${se.line} function asctime is called with ${se.var} as parameter. This ${se.var} may not have been sanitized.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC33-C.msg_template', '???${se.filename}, ???${se.line}????????????asctime????????????????????????${se.var}????????????${se.var}???????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC33-C.name', 'Use of asctime() function must pass the parameter needed with valid ranges', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC33-C.name', 'asctime()??????????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC33-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC33-C'),
 'BASIC','PRIORITY','27'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC33-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC33-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MSC37-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC37-C', null, 'MSC37-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/MSC37-C.+Ensure+that+control+never+reaches+the+end+of+a+non-void+function', '${rule.Xcalibyte.CERT.1.MSC37-C.name}', '1', '2', 'UNLIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.MSC37-C.detail}', '${rule.Xcalibyte.CERT.1.MSC37-C.description}', '${rule.Xcalibyte.CERT.1.MSC37-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MSC37-C.description', 'The program has an execution path that might reach the exit point of a non-void function without going through a "return" statement.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC37-C.description', '???????????????????????????????????????????????????"return"??????????????????void?????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC37-C.detail', '#### Abstract'||chr(10)||'The program has an execution path that might reach the exit point of a non-void function without going through a "return" statement.'||chr(10)||'#### Explanation'||chr(10)||'When the program exits that function on an execution path without a return statement, the return value will be non-deterministic causing unexpected program behavior. If the function happens to be "main", or if the function is marked with "no-return" attribute, then there is no problem.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <string.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'int has_char(const char *s)'||chr(10)||'{'||chr(10)||'  if (s != NULL) {'||chr(10)||'    int l = strlen(s);'||chr(10)||'    int i;'||chr(10)||'    for (i = 0; i<l; i++) {'||chr(10)||'      if (s[i] == ''c'')'||chr(10)||'        return i;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'  // missing return here, return value will be undefined'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC37-C.detail', '#### ??????'||chr(10)||'???????????????????????????????????????????????????"return"??????????????????void?????????????????????'||chr(10)||'#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????"main"??????????????????????????????"no-return"?????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <string.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'int has_char(const char *s)'||chr(10)||'{'||chr(10)||'  if (s != NULL) {'||chr(10)||'    int l = strlen(s);'||chr(10)||'    int i;'||chr(10)||'    for (i = 0; i<l; i++) {'||chr(10)||'      if (s[i] == ''c'')'||chr(10)||'        return i;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'  // missing return here, return value will be undefined'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC37-C.msg_template', 'In ${se.filename} at line ${se.line} the function ${se.func} is declared to return non-void but has reached end of function without a return statement.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC37-C.msg_template', '???${se.filename} ???${se.line}?????????????????????${se.func}????????????void??????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC37-C.name', 'Non-void functions should always exit this function through a return statement', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC37-C.name', '???void????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC37-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC37-C'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC37-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC37-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MSC41-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC41-C', null, 'MSC41-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/MSC41-C.+Never+hard+code+sensitive+information', '${rule.Xcalibyte.CERT.1.MSC41-C.name}', '1', '1', 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.MSC41-C.detail}', '${rule.Xcalibyte.CERT.1.MSC41-C.description}', '${rule.Xcalibyte.CERT.1.MSC41-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MSC41-C.description', 'The program has hard coded sensitive information (such as password, keys) in readable form.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC41-C.description', '?????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC41-C.detail', '### MSC41 (NYI, need user tag for what func is related to sensitive info)'||chr(10)||'#### Abstract'||chr(10)||'The program has hard coded sensitive information (such as password, keys) in readable form.'||chr(10)||'#### Explanation'||chr(10)||'Sensitive information in strings or other readable forms can be examined even inside executables.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'int check_passwd(const char *s);'||chr(10)||''||chr(10)||'int func(void)'||chr(10)||'{'||chr(10)||'  // program logic'||chr(10)||'  // ...'||chr(10)||'  if (check_passwd("admin")) {'||chr(10)||'      // ... continue'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    // handle error'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC41-C.detail', '### MSC41??????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'int check_passwd(const char *s);'||chr(10)||''||chr(10)||'int func(void)'||chr(10)||'{'||chr(10)||'  // program logic'||chr(10)||'  // ...'||chr(10)||'  if (check_passwd("admin")) {'||chr(10)||'      // ... continue'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    // handle error'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC41-C.msg_template', '<< FIX ME>>', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC41-C.msg_template', '<< FIX ME>>', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC41-C.name', '<< NYI >> Do not expose hard coded sensitive information in the program', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC41-C.name', '<< NYI >> ????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC41-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC41-C'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC41-C'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC41-C'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC41-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC41-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC41-C'),
 'BASIC','NYI','TRUE')
ON CONFLICT DO NOTHING;


-- ------------------------
-- POS30-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'POS30-C', null, 'POS30-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/POS30-C.+Use+the+readlink%28%29+function+properly', '${rule.Xcalibyte.CERT.1.POS30-C.name}', '1', '1', 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.POS30-C.detail}', '${rule.Xcalibyte.CERT.1.POS30-C.description}', '${rule.Xcalibyte.CERT.1.POS30-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.POS30-C.description', 'The program has called function readlink() and the number of characters written on the buffer (second argument) is not enough to hold a valid string.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS30-C.description', '??????????????????readlink()?????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS30-C.detail', '#### Abstract'||chr(10)||'The program has called function readlink() and the number of characters written on the buffer (second argument) is not enough to hold a valid string.'||chr(10)||'#### Explanation'||chr(10)||'The number of characters written by readlink() may overflow the buffer specified in the second parameter. Also one needs to ensure that the end of the string is null terminated.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'int func(char *env_str)'||chr(10)||'{'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||''||chr(10)||'  ssize_t len = readlink("/usr/somedir/symlinkfile", buf, sizeof(buf));  // if len is >= sizeof(buf), up till the last byte of buf is non-null'||chr(10)||''||chr(10)||'  buf[len] = ''\0'';    // len is outside of buf size range (i may be -1 or i >= sizeof(buf))'||chr(10)||'  return;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS30-C.detail', '#### ??????'||chr(10)||'??????????????????readlink()?????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'???readlink()???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'int func(char *env_str)'||chr(10)||'{'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||''||chr(10)||'  ssize_t len = readlink("/usr/somedir/symlinkfile", buf, sizeof(buf));  // if len is >= sizeof(buf), up till the last byte of buf is non-null'||chr(10)||''||chr(10)||'  buf[len] = ''\0'';    // len is outside of buf size range (i may be -1 or i >= sizeof(buf))'||chr(10)||'  return;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS30-C.msg_template', 'In ${se.filename} at line ${se.line} the function readlink() is called and assigned to ${se.var}. The string read are not null terminated.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS30-C.msg_template', '???${se.filename} ???${se.line}?????????????????????readlink()????????????????????????${se.var}???????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS30-C.name', 'The readlink() function will only fill the buffer in the second argument not including the null terminator', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS30-C.name', 'readlink()????????????????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS30-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS30-C'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS30-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS30-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- POS34-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'POS34-C', null, 'POS34-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/POS34-C.+Do+not+call+putenv%28%29+with+a+pointer+to+an+automatic+variable+as+the+argument', '${rule.Xcalibyte.CERT.1.POS34-C.name}', '1', '2', 'UNLIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.POS34-C.detail}', '${rule.Xcalibyte.CERT.1.POS34-C.description}', '${rule.Xcalibyte.CERT.1.POS34-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.POS34-C.description', 'The program has called putenv() with an argument that points to a local object.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS34-C.description', '??????????????????putenv()???????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS34-C.detail', '#### Abstract'||chr(10)||'The program has called putenv() with an argument that points to a local object.'||chr(10)||'#### Explanation'||chr(10)||'The system call putenv() will save the argument (which is a pointer to the intended string) into the environment array. When the function returns to the caller, the local object with the environment string may be overwritten. This will cause unpredictable program behavior.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <string.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'int func(char *env_str)'||chr(10)||'{'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||'  // ...'||chr(10)||'  if (strlen(env_str) < BUF_SZ) {'||chr(10)||'    strcpy(buf, env_str);'||chr(10)||'    return putenv(buf);'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    // handle error'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS34-C.detail', '#### ??????'||chr(10)||'??????????????????putenv()???????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'????????????putenv()?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <string.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'int func(char *env_str)'||chr(10)||'{'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||'  // ...'||chr(10)||'  if (strlen(env_str) < BUF_SZ) {'||chr(10)||'    strcpy(buf, env_str);'||chr(10)||'    return putenv(buf);'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    // handle error'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS34-C.msg_template', 'In ${se.filename} at line ${se.line} the function ${se.func} is calling the system function putenv() with ${se.var} as parameter. This variable is a local variable declared at ${ss.filename}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS34-C.msg_template', '???${se.filename}??????${se.line}???????????? ${se.func} ????????????${se.var}??????????????????????????????putenv()??????????????????${ss.filename}, ???${ss.line}?????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS34-C.name', 'The pointer argument in putenv() call should not point to local objects', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS34-C.name', 'putenv()????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS34-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS34-C'),
 'BASIC','PRIORITY','6'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS34-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS34-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- POS35-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'POS35-C', null, 'POS35-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/POS35-C.+Avoid+race+conditions+while+checking+for+the+existence+of+a+symbolic+link', '${rule.Xcalibyte.CERT.1.POS35-C.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.POS35-C.detail}', '${rule.Xcalibyte.CERT.1.POS35-C.description}', '${rule.Xcalibyte.CERT.1.POS35-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.POS35-C.description', 'The program has called function lstat() to check the named file for symbolic linkage. Result of this check may not be valid when the file is later accessed.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS35-C.description', '????????????????????????lstat()?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS35-C.detail', '#### Abstract'||chr(10)||'The program has called function lstat() to check the named file for symbolic linkage. Result of this check may not be valid when the file is later accessed.'||chr(10)||'#### Explanation'||chr(10)||'Checking that a file is a symbolic link suffers from time of call, time of use (TOCTOU) problem. In other words, when the file is finally opened for access, that the file is of symbolic link status may not be valid anymore.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <sys/stat.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'struct stat link_info;'||chr(10)||''||chr(10)||'int func(char *fname)'||chr(10)||'{'||chr(10)||'  FILE *fp;'||chr(10)||'  if (lstat(fname, &link_info) == -1) {'||chr(10)||'    // handle error'||chr(10)||'    // ...'||chr(10)||'    printf("lstat return error\n");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'  else if (S_ISLNK(link_info.st_mode)) {'||chr(10)||''||chr(10)||'    // both fopen and lstat operate on a file name'||chr(10)||'    // the name can be manipulated asynchronously such that'||chr(10)||'    // lstat and fopen referred to different files in reality'||chr(10)||'    fp = fopen(fname, "O_RDWR")'||chr(10)||'    if (fp == 0); {'||chr(10)||'      // handle error'||chr(10)||'      return 0;'||chr(10)||'    }'||chr(10)||''||chr(10)||'    size_t i = fread(&val, sizeof(int), 1, fp);'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  return 1;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS35-C.detail', '#### ??????'||chr(10)||'????????????????????????lstat()?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'????????????????????????????????????time of call???time of use???TOCTOU???????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <sys/stat.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'struct stat link_info;'||chr(10)||''||chr(10)||'int func(char *fname)'||chr(10)||'{'||chr(10)||'  FILE *fp;'||chr(10)||'  if (lstat(fname, &link_info) == -1) {'||chr(10)||'    // handle error'||chr(10)||'    // ...'||chr(10)||'    printf("lstat return error\n");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'  else if (S_ISLNK(link_info.st_mode)) {'||chr(10)||''||chr(10)||'    // both fopen and lstat operate on a file name'||chr(10)||'    // the name can be manipulated asynchronously such that'||chr(10)||'    // lstat and fopen referred to different files in reality'||chr(10)||'    fp = fopen(fname, "O_RDWR")'||chr(10)||'    if (fp == 0); {'||chr(10)||'      // handle error'||chr(10)||'      return 0;'||chr(10)||'    }'||chr(10)||''||chr(10)||'    size_t i = fread(&val, sizeof(int), 1, fp);'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  return 1;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS35-C.msg_template', 'In ${se.filename}, function ${se.func} at line ${se.line}, operations of file with file descriptor ${se.var} has a potential race condition defect with the function call in ${ss.func}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS35-C.msg_template', '???${se.filename}??????${se.line}???????????? ${s2.func}???????????????????????????${se.var}????????????????????????${ss.line}???????????????${ss.func}?????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS35-C.name', 'When checking for validity of a symbolic link, there could be a race condition that may nullify the checked result', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS35-C.name', '???????????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS35-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS35-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS35-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS35-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- POS37-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'POS37-C', null, 'POS37-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/POS37-C.+Ensure+that+privilege+relinquishment+is+successful', '${rule.Xcalibyte.CERT.1.POS37-C.name}', '1', '1', 'PROBABLE', 'LOW', '${rule.Xcalibyte.CERT.1.POS37-C.detail}', '${rule.Xcalibyte.CERT.1.POS37-C.description}', '${rule.Xcalibyte.CERT.1.POS37-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.POS37-C.description', 'The program has one of the get/set uid/euid set of functions which manipulates user id privileges. The result of calling such functions is not checked which will cause the program''s privilege to be in an unexpected state.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS37-C.description', '??????????????????get/set uid/euid?????????????????????id??????????????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS37-C.detail', '#### Abstract'||chr(10)||'The program has one of the get/set uid/euid set of functions which manipulates user id privileges. The result of calling such functions is not checked which will cause the program''s privilege to be in an unexpected state.'||chr(10)||'#### Explanation'||chr(10)||'Privilege capabilities are implementation defined. To ensure that the privileges are set and relinquished as expected, it is important to check the error conditions on return. Failure to do this may result in the program being run with root permission.'||chr(10)||''||chr(10)||'#### Example (proper usage)'||chr(10)||'````text'||chr(10)||''||chr(10)||'  // ...'||chr(10)||''||chr(10)||'  // the following check alone is not sufficient. need to make sure privilege can be restored'||chr(10)||'  if (setuid(getuid()) != 0) {'||chr(10)||'    // Handle error'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // check for failure to setuid when the caller is UID 0'||chr(10)||'  // failure to do this will result in serious security risk'||chr(10)||'  if (setuid(0) != -1) {'||chr(10)||'    // privilege can be restored. Handle error hee'||chr(10)||'  }'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS37-C.detail', '#### ??????'||chr(10)||'??????????????????get/set uid/euid?????????????????????id??????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ????????????????????????'||chr(10)||'````text'||chr(10)||''||chr(10)||'  // ...'||chr(10)||''||chr(10)||'  // the following check alone is not sufficient. need to make sure privilege can be restored'||chr(10)||'  if (setuid(getuid()) != 0) {'||chr(10)||'    // Handle error'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // check for failure to setuid when the caller is UID 0'||chr(10)||'  // failure to do this will result in serious security risk'||chr(10)||'  if (setuid(0) != -1) {'||chr(10)||'    // privilege can be restored. Handle error hee'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS37-C.msg_template', 'In ${se.filename}, function ${se.func} at line ${se.line}, privilege has not be properly restored. The privilege was reliquished in ${ss.filename}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS37-C.msg_template', '???${se.filename}??????${se.line}???????????? ${se.func}?????????????????????????????????????????????${ss.filename}, ???${ss.line}???????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS37-C.name', 'When manipulating system privilege through the set/set uid/euid functions, please make sure that privileges are relinquished successfully', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS37-C.name', '?????????set/set uid/euid???????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS37-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS37-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS37-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS37-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS37-C'),
 'STANDARD','OWASP','05')
ON CONFLICT DO NOTHING;

-- ------------------------
-- POS54-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'POS54-C', null, 'POS54-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/POS54-C.+Detect+and+handle+POSIX+library+errors', '${rule.Xcalibyte.CERT.1.POS54-C.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.POS54-C.detail}', '${rule.Xcalibyte.CERT.1.POS54-C.description}', '${rule.Xcalibyte.CERT.1.POS54-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.POS54-C.description', 'The program has called some POSIX library function but failed to check and handle the return error condition.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS54-C.description', '????????????????????????POSIX?????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS54-C.detail', '#### Abstract'||chr(10)||'The program has called some POSIX library function but failed to check and handle the return error condition.'||chr(10)||'#### Explanation'||chr(10)||'Each POSIX library has well defined error return. Failure to check the return value for indication of an error condition and handle the error appropriately will lead to unexpected program behavior.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example 1'||chr(10)||'````text'||chr(10)||''||chr(10)||'  // drop privileges'||chr(10)||'  // this check failed to check for error (-1)'||chr(10)||'  if (setuid(getuid()) != 0) {'||chr(10)||'      // ...'||chr(10)||'  }'||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example 2'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <sys/types.h>'||chr(10)||'#include <sys/mman.h>'||chr(10)||'#include <err.h>'||chr(10)||'#include <fcntl.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <unistd.h>'||chr(10)||''||chr(10)||''||chr(10)||'int main(void)'||chr(10)||'{'||chr(10)||'  const char str1[] = "string 1";'||chr(10)||'  int fd = -1;'||chr(10)||'  void *rwrite;'||chr(10)||''||chr(10)||'  if ((fd = open("/usr/somefile", O_R, 0)) == -1)'||chr(10)||'    err(1, "open");'||chr(10)||''||chr(10)||'    rwrite = (char*)mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0);'||chr(10)||''||chr(10)||'    // rwrite may not be valid pointer since mmap may have failed'||chr(10)||'    strcpy(rwrite, str1);'||chr(10)||''||chr(10)||'    // ... continue'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS54-C.detail', '#### ??????'||chr(10)||'????????????????????????POSIX?????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'?????????POSIX????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||''||chr(10)||'#### ?????? 1'||chr(10)||'````text'||chr(10)||''||chr(10)||'  // drop privileges'||chr(10)||'  // this check failed to check for error (-1)'||chr(10)||'  if (setuid(getuid()) != 0) {'||chr(10)||'      // ...'||chr(10)||'  }'||chr(10)||''||chr(10)||'````'||chr(10)||'#### ?????? 2'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <sys/types.h>'||chr(10)||'#include <sys/mman.h>'||chr(10)||'#include <err.h>'||chr(10)||'#include <fcntl.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <unistd.h>'||chr(10)||''||chr(10)||''||chr(10)||'int main(void)'||chr(10)||'{'||chr(10)||'  const char str1[] = "string 1";'||chr(10)||'  int fd = -1;'||chr(10)||'  void *rwrite;'||chr(10)||''||chr(10)||'  if ((fd = open("/usr/somefile", O_R, 0)) == -1)'||chr(10)||'    err(1, "open");'||chr(10)||''||chr(10)||'    rwrite = (char*)mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0);'||chr(10)||''||chr(10)||'    // rwrite may not be valid pointer since mmap may have failed'||chr(10)||'    strcpy(rwrite, str1);'||chr(10)||''||chr(10)||'    // ... continue'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS54-C.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line} a POSIX function is called and the call result was used at ${ss.filename}, line ${ss.line} without checking for the validity of the return.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS54-C.msg_template', '???${s2.filename}??????${s2.line}????????????${s2.func}????????????POSIX??????????????????${ss.filename}, ???${ss.line}????????????????????????, ?????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.POS54-C.name', 'when using POSIX library, please make sure to detect and appropriately handle errors on function return', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.POS54-C.name', '?????????POSIX???????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS54-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS54-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS54-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='POS54-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SIG30-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SIG30-C', null, 'SIG30-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/SIG30-C.+Call+only+asynchronous-safe+functions+within+signal+handlers', '${rule.Xcalibyte.CERT.1.SIG30-C.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SIG30-C.detail}', '${rule.Xcalibyte.CERT.1.SIG30-C.description}', '${rule.Xcalibyte.CERT.1.SIG30-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.SIG30-C.description', 'The program is calling non-asynchronous-safe functions inside signal handlers.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SIG30-C.description', '??????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SIG30-C.detail', '#### Abstract'||chr(10)||'The program is calling non-asynchronous-safe functions inside signal handlers.'||chr(10)||'#### Explanation'||chr(10)||'The table of asynchronous-safe functions are functions that can be called safely without side effects by a signal handler. Non-reentrant functions are typically not safe. Users can check your system user manual for a full list of all safe functions to use.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'#include <signal.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'char *messg = NULL;'||chr(10)||'int  errflag;'||chr(10)||''||chr(10)||'#define BUF_SZ 1024'||chr(10)||''||chr(10)||'void put_messg(void)'||chr(10)||'{'||chr(10)||'  fputs(messg, stderr);'||chr(10)||'}'||chr(10)||''||chr(10)||'void handler(int signum)'||chr(10)||'{'||chr(10)||'  put_messg();    // this is not asynchronous-safe'||chr(10)||'  free(messg);    // this is not asynchronous-safe'||chr(10)||'  errflag = 1;'||chr(10)||'}'||chr(10)||''||chr(10)||'int main(void)'||chr(10)||'{'||chr(10)||'  if (signal(SIGINT, handler) == SIG_ERR) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||'  messg = (char *)malloc(BUF_SZ);'||chr(10)||'  if (messg == 0) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||''||chr(10)||'  if (!errflag) {'||chr(10)||'    put_messg();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SIG30-C.detail', '#### ??????'||chr(10)||'??????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'#include <signal.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'char *messg = NULL;'||chr(10)||'int  errflag;'||chr(10)||''||chr(10)||'#define BUF_SZ 1024'||chr(10)||''||chr(10)||'void put_messg(void)'||chr(10)||'{'||chr(10)||'  fputs(messg, stderr);'||chr(10)||'}'||chr(10)||''||chr(10)||'void handler(int signum)'||chr(10)||'{'||chr(10)||'  put_messg();    // this is not asynchronous-safe'||chr(10)||'  free(messg);    // this is not asynchronous-safe'||chr(10)||'  errflag = 1;'||chr(10)||'}'||chr(10)||''||chr(10)||'int main(void)'||chr(10)||'{'||chr(10)||'  if (signal(SIGINT, handler) == SIG_ERR) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||'  messg = (char *)malloc(BUF_SZ);'||chr(10)||'  if (messg == 0) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||''||chr(10)||'  if (!errflag) {'||chr(10)||'    put_messg();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SIG30-C.msg_template', 'In ${se.filename}, ${se.func} at ${se.line}, a call to signal with handler "{se.var}". This handler, declared in ${ss.filename} at ${ss.line} will call functions that are not asynchronous safe.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SIG30-C.msg_template', '???${se.filename}??????${s2.line}????????????${s2.func}?????????signal??????.???????????????????????????"${s2.var}"???????????????????????????${ss.filename} ???$(ss.iine}??? ????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SIG30-C.name', 'Signal handlers can only invoke asynchronous safe functions', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SIG30-C.name', '???????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SIG30-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SIG30-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SIG30-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SIG30-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SIG31-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values

((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SIG31-C', null, 'SIG31-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/SIG31-C.+Do+not+access+shared+objects+in+signal+handlers', '${rule.Xcalibyte.CERT.1.SIG31-C.name}', '1', '2', 'LIKELY', 'HIGH', '${rule.Xcalibyte.CERT.1.SIG31-C.detail}', '${rule.Xcalibyte.CERT.1.SIG31-C.description}', '${rule.Xcalibyte.CERT.1.SIG31-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.SIG31-C.description', 'The program is accessing shared variable or object inside a signal handler.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SIG31-C.description', '??????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SIG31-C.detail', '#### Abstract'||chr(10)||'The program is accessing shared variable or object inside a signal handler.'||chr(10)||'#### Explanation'||chr(10)||'Other than variables of type "volatile sig_atomic_t", accessing any other type of objects from a signal handler may cause race condition resulting in undefined behavior.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <signal.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <sys/socket.h>'||chr(10)||''||chr(10)||'char *messg = NULL;'||chr(10)||'#define BUF_SZ 1024'||chr(10)||'int sockfd;   // for simplicity of this example, we assume sockfd has been properly initialized and connected'||chr(10)||''||chr(10)||'int errflag;  // this is a shared variable'||chr(10)||''||chr(10)||'ssize_t put_messg(void)'||chr(10)||'{'||chr(10)||'  ssize_t err = send(sockfd, (const void *)messg, BUF_SZ, MSG_DONTWAIT);'||chr(10)||'  return err;'||chr(10)||'}'||chr(10)||''||chr(10)||'void handler(int signum)'||chr(10)||'{'||chr(10)||'  errflag = 1;  // access shared variable inside handler'||chr(10)||'}'||chr(10)||''||chr(10)||'int main(void)'||chr(10)||'{'||chr(10)||'  messg = (char *)malloc(1024);'||chr(10)||'  if (messg == 0) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||''||chr(10)||'  if (signal(SIGINT, handler) == SIG_ERR) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||''||chr(10)||'  if (!errflag) {'||chr(10)||'    if (put_messg() >= 0) {'||chr(10)||'      // messg sent'||chr(10)||'      // ...'||chr(10)||'    }'||chr(10)||'    else {'||chr(10)||'      // ...'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SIG31-C.detail', '#### ??????'||chr(10)||'??????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'???????????????"volatile sig_atomic_t"????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <signal.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <sys/socket.h>'||chr(10)||''||chr(10)||'char *messg = NULL;'||chr(10)||'#define BUF_SZ 1024'||chr(10)||'int sockfd;   // for simplicity of this example, we assume sockfd has been properly initialized and connected'||chr(10)||''||chr(10)||'int errflag;  // this is a shared variable'||chr(10)||''||chr(10)||'ssize_t put_messg(void)'||chr(10)||'{'||chr(10)||'  ssize_t err = send(sockfd, (const void *)messg, BUF_SZ, MSG_DONTWAIT);'||chr(10)||'  return err;'||chr(10)||'}'||chr(10)||''||chr(10)||'void handler(int signum)'||chr(10)||'{'||chr(10)||'  errflag = 1;  // access shared variable inside handler'||chr(10)||'}'||chr(10)||''||chr(10)||'int main(void)'||chr(10)||'{'||chr(10)||'  messg = (char *)malloc(1024);'||chr(10)||'  if (messg == 0) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||''||chr(10)||'  if (signal(SIGINT, handler) == SIG_ERR) {'||chr(10)||'    // handle error'||chr(10)||'  }'||chr(10)||''||chr(10)||'  if (!errflag) {'||chr(10)||'    if (put_messg() >= 0) {'||chr(10)||'      // messg sent'||chr(10)||'      // ...'||chr(10)||'    }'||chr(10)||'    else {'||chr(10)||'      // ...'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SIG31-C.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line} a call to signal with handler "{se.var}" is made. This handler eventually will access a sharable memory object at line ${ss.line}, ${ss.filename}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SIG31-C.msg_template', '???${s2.filename}??????${se.line}????????????${s2.func}????????????????????????????????????"${se.var}"???????????????????????????????????????????????????????????????$(ss.line}?????? ${ss.filename} ???????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SIG31-C.name', 'Signal handlers accessing shared variables or objects may result in race conditions', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SIG31-C.name', '???????????????????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SIG31-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SIG31-C'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SIG31-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SIG31-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- STR02-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'STR02-C', null, 'STR02-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/STR02-C.+Sanitize+data+passed+to+complex+subsystems', '${rule.Xcalibyte.CERT.1.STR02-C.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.STR02-C.detail}', '${rule.Xcalibyte.CERT.1.STR02-C.description}', '${rule.Xcalibyte.CERT.1.STR02-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.STR02-C.description', 'The program is passing string data to external packages or subsystems, these data as strings need to be sanitized.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR02-C.description', '?????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR02-C.detail', '#### Abstract'||chr(10)||'The program is passing string data to external packages or subsystems, these data as strings need to be sanitized.'||chr(10)||'#### Explanation'||chr(10)||'When data containing sensitive data is passed to system calls, databases or other external third party components, it is important to ensure that only data acceptable to both producer and consumer is being passed.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'char *messg = NULL;'||chr(10)||'#define BUF_SZ 1024'||chr(10)||''||chr(10)||'void foo(char *argv)'||chr(10)||'{'||chr(10)||'  int len;'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||''||chr(10)||'  if (*argv != 0)'||chr(10)||'    len = snprintf(buf,  BUF_SZ, "%s", *argv);  // buf will be feed into system call later. "argv" needs to be sanitized'||chr(10)||'  if (len < 0) {'||chr(10)||'    // report error'||chr(10)||'    return;'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    if (system(buf) == -1) {'||chr(10)||'      // report error'||chr(10)||'      return;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR02-C.detail', '#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'char *messg = NULL;'||chr(10)||'#define BUF_SZ 1024'||chr(10)||''||chr(10)||'void foo(char *argv)'||chr(10)||'{'||chr(10)||'  int len;'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||''||chr(10)||'  if (*argv != 0)'||chr(10)||'    len = snprintf(buf,  BUF_SZ, "%s", *argv);  // buf will be feed into system call later. "argv" needs to be sanitized'||chr(10)||'  if (len < 0) {'||chr(10)||'    // report error'||chr(10)||'    return;'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    if (system(buf) == -1) {'||chr(10)||'      // report error'||chr(10)||'      return;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR02-C.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line} "{se.var}" is passed to a system call. This variable is declared at line ${ss.line} and considered unsanitized through the program flow.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR02-C.msg_template', '???${s2.filename}??????${ss.line)????????????${s2.func}????????????"${s2.var}"?????????????????????????????????${ss.line}?????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR02-C.name', 'Data passed to external packages or subsystems should be sanitized', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR02-C.name', '??????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR02-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR02-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR02-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR02-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR02-C'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR02-C'),
 'STANDARD','OWASP','04'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR02-C'),
 'STANDARD','OWASP','01')
ON CONFLICT DO NOTHING;


-- ------------------------
-- STR31-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'STR31-C', null, 'STR31-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/STR31-C.+Guarantee+that+storage+for+strings+has+sufficient+space+for+character+data+and+the+null+terminator', '${rule.Xcalibyte.CERT.1.STR31-C.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.STR31-C.detail}', '${rule.Xcalibyte.CERT.1.STR31-C.description}', '${rule.Xcalibyte.CERT.1.STR31-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.STR31-C.description', 'Ensure that the destination storage of a string is sufficiently large that it includes the terminating null.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR31-C.description', '???????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR31-C.detail', '#### Abstract'||chr(10)||'Ensure that the destination storage of a string is sufficiently large that it includes the terminating null.'||chr(10)||'#### Explanation'||chr(10)||'A string is terminated by the null character and should be part of the string size/length. If the storage for the string is not sufficiently large, it will cause buffer overflow or other spurious errors.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <string.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'void get_passwd(void)'||chr(10)||'{'||chr(10)||'  char buf[256];'||chr(10)||'  char *passwd = getenv("PASSWORD");'||chr(10)||'  if (passwd == NULL) {'||chr(10)||'    printf("Error getting password\n");'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||'  //'||chr(10)||'  // copy environmental string to a fixed-length can cause buffer overflow'||chr(10)||'  //'||chr(10)||'  strcpy(buf, passwd);'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR31-C.detail', '#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????????????????????????????/???????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <string.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'void get_passwd(void)'||chr(10)||'{'||chr(10)||'  char buf[256];'||chr(10)||'  char *passwd = getenv("PASSWORD");'||chr(10)||'  if (passwd == NULL) {'||chr(10)||'    printf("Error getting password\n");'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||'  //'||chr(10)||'  // copy environmental string to a fixed-length can cause buffer overflow'||chr(10)||'  //'||chr(10)||'  strcpy(buf, passwd);'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR31-C.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line} "{se.var}" a pointer to character was used to another fill another character array/pointer. ${se.var} is not null terminnated and may cause memory access error.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR31-C.msg_template', '???${se.filename}??????${se.line}????????????${se.func}????????????????????????${se.var}?????????????????????????????????/???????????????${se.var}??????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR31-C.name', 'Strings should have sufficient storage for all characters including the null terminator', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR31-C.name', '??????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR31-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR31-C'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR31-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR31-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- STR32-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'STR32-C', null, 'STR32-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/STR32-C.+Do+not+pass+a+non-null-terminated+character+sequence+to+a+library+function+that+expects+a+string', '${rule.Xcalibyte.CERT.1.STR32-C.name}', '1', '1', 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.STR32-C.detail}', '${rule.Xcalibyte.CERT.1.STR32-C.description}', '${rule.Xcalibyte.CERT.1.STR32-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.STR32-C.description', 'The program is calling a library function with a string parameter. That string may not be properly terminated.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR32-C.description', '?????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR32-C.detail', '#### Abstract'||chr(10)||'The program is calling a library function with a string parameter. That string may not be properly terminated.'||chr(10)||'#### Explanation'||chr(10)||'Library functions that operate on a string that is not null terminated can result in accessing memory outside of the boundary of the string object. This in turn will cause unexpected program behavior.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'char *messg = NULL;'||chr(10)||'#define BUF_SZ 1024'||chr(10)||''||chr(10)||'void foo(void)'||chr(10)||'{'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||''||chr(10)||'  buf[0] = ''1'';'||chr(10)||'  buf[1] = ''2'';'||chr(10)||'  buf[3] = ''3'';'||chr(10)||''||chr(10)||'  printf("string is %s\n", buf);  // buf may not be null terminated since the array is a local array'||chr(10)||''||chr(10)||'  // ...'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR32-C.detail', '#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <stdio.h>'||chr(10)||''||chr(10)||'char *messg = NULL;'||chr(10)||'#define BUF_SZ 1024'||chr(10)||''||chr(10)||'void foo(void)'||chr(10)||'{'||chr(10)||'  char buf[BUF_SZ];'||chr(10)||''||chr(10)||'  buf[0] = ''1'';'||chr(10)||'  buf[1] = ''2'';'||chr(10)||'  buf[3] = ''3'';'||chr(10)||''||chr(10)||'  printf("string is %s\n", buf);  // buf may not be null terminated since the array is a local array'||chr(10)||''||chr(10)||'  // ...'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR32-C.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line} "{se.var}" is passed to a library function. This variable is not null terminated.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR32-C.msg_template', '???${s2.filename}??????${se.line}????????????${s2.func}????????????"${se.var}"??????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR32-C.name', 'String parameters to library functions should be properly null terminated', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR32-C.name', '????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR32-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR32-C'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR32-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR32-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- STR38-C
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'STR38-C', null, 'STR38-C', 'c,c++', 'https://wiki.sei.cmu.edu/confluence/display/c/STR38-C.+Do+not+confuse+narrow+and+wide+character+strings+and+functions', '${rule.Xcalibyte.CERT.1.STR38-C.name}', '1', '1', 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.STR38-C.detail}', '${rule.Xcalibyte.CERT.1.STR38-C.description}', '${rule.Xcalibyte.CERT.1.STR38-C.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.STR38-C.description', 'The program is using functions and parameter with mismatching character size or type for the size of characters in the string.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR38-C.description', '?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR38-C.detail', '#### Abstract'||chr(10)||'The program is using functions and parameter with mismatching character size or type for the size of characters in the string.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'The program is passing a pointer to an object and a size parameter to the library function. The two parameters, when combined, will cause the library function to access this object but outside its valid range, resulting in undefined behavior.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stddef.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <wchar.h>'||chr(10)||''||chr(10)||'#define ARRAY_SZ 2'||chr(10)||'int mismatch_char_type()'||chr(10)||'{'||chr(10)||'  char char_array[] = "0123456789";'||chr(10)||'  wchar_t wchar_array[] = "0123456789";'||chr(10)||''||chr(10)||'  // size of size of char_array is smaller than wchar_array due to different char type'||chr(10)||'  memcpy(&char_array[0], &wchar_array[0], sizeof(wchar_array));'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR38-C.detail', '#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stddef.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <wchar.h>'||chr(10)||''||chr(10)||'#define ARRAY_SZ 2'||chr(10)||'int mismatch_char_type()'||chr(10)||'{'||chr(10)||'  char char_array[] = "0123456789";'||chr(10)||'  wchar_t wchar_array[] = "0123456789";'||chr(10)||''||chr(10)||'  // size of size of char_array is smaller than wchar_array due to different char type'||chr(10)||'  memcpy(&char_array[0], &wchar_array[0], sizeof(wchar_array));'||chr(10)||'  return 0;'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR38-C.msg_template', 'In ${se.filename}, at line ${se.line} "{se.var}" is erroneously used for the function ${s2.func}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR38-C.msg_template', '???${se.filename}??????${se.line}????????????"${se.var}"??????????????????${se.func}??????.', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.STR38-C.name', 'Narrow and wide character strings and functions should have the proper type for null terminator and length', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.STR38-C.name', '??????????????????????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR38-C');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR38-C'),
 'BASIC','PRIORITY','27'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR38-C'),
 'BASIC','LANG','c'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='STR38-C'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ERR54-CPP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ERR54-CPP', null, 'ERR54-CPP', 'c++', 'https://wiki.sei.cmu.edu/confluence/display/cplusplus/ERR54-CPP.+Catch+handlers+should+order+their+parameter+types+from+most+derived+to+least+derived', '${rule.Xcalibyte.CERT.1.ERR54-CPP.name}', '2', '1', 'LIKELY', 'HIGH', '${rule.Xcalibyte.CERT.1.ERR54-CPP.detail}', '${rule.Xcalibyte.CERT.1.ERR54-CPP.description}', '${rule.Xcalibyte.CERT.1.ERR54-CPP.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.ERR54-CPP.description', 'Program fails to manage the catch handlers for exception in the correct order after try statement. The current order is from least derived (more general) to most derived (more specific), hence catch handlers for the most derived will never be executed.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR54-CPP.description', '???try?????????????????????????????????????????????????????????????????????????????? ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR54-CPP.detail', '#### ??????'||chr(10)||' '||chr(10)||'???try?????????????????????????????????????????????????????????????????????????????? ??????????????????????????????????????????????????????????????????????????????'||chr(10)||' '||chr(10)||'#### ??????'||chr(10)||' '||chr(10)||'????????????????????????try????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||' '||chr(10)||'#### ?????? - ??????'||chr(10)||' '||chr(10)||'```cpp'||chr(10)||' '||chr(10)||'// Classes used for exception handling'||chr(10)||'#include <iostream>'||chr(10)||' '||chr(10)||'class Base {};'||chr(10)||' '||chr(10)||'class Derived1 : public Base {};'||chr(10)||'class Derived2 : public Derived1 {};'||chr(10)||' '||chr(10)||'void foo();'||chr(10)||'void bar();'||chr(10)||' '||chr(10)||'void foo()'||chr(10)||'{'||chr(10)||'    std::cout << "foo" << std::endl;'||chr(10)||'}'||chr(10)||'void bar()'||chr(10)||'{'||chr(10)||'    std::cout << "bar" << std::endl;'||chr(10)||'}'||chr(10)||' '||chr(10)||'void f()'||chr(10)||'{'||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base &) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived1 &)  { // violation here in Line 19'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base &) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived2 &) // violation here in Line 31'||chr(10)||'  {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||' '||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base *) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived1 *) { // violation here in Line 44'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||' '||chr(10)||'int main() {'||chr(10)||'    f();'||chr(10)||'}'||chr(10)||'```'||chr(10)||'####  ?????? - ??????'||chr(10)||' '||chr(10)||'```cpp'||chr(10)||' '||chr(10)||'// Classes used for exception handling'||chr(10)||'#include <iostream>'||chr(10)||' '||chr(10)||'class Base {};'||chr(10)||' '||chr(10)||'class Derived1 : public Base {};'||chr(10)||'class Derived2 : public Derived1 {};'||chr(10)||' '||chr(10)||'void foo();'||chr(10)||'void bar();'||chr(10)||' '||chr(10)||'void foo()'||chr(10)||'{'||chr(10)||'    std::cout << "foo" << std::endl;'||chr(10)||'}'||chr(10)||'void bar() {'||chr(10)||'    std::cout << "bar" << std::endl;'||chr(10)||'}'||chr(10)||' '||chr(10)||'void f()'||chr(10)||'{'||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived1 &) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base &)'||chr(10)||'  {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived2 &) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base &) //'||chr(10)||'  {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||' '||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived1 *) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base *) { //'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||' '||chr(10)||'int main() {'||chr(10)||'    f();'||chr(10)||'}'||chr(10)||'```', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR54-CPP.detail', '#### Abstract'||chr(10)||'Program fails to manage the catch handlers for exception in the correct order after try statement. The current order is from least derived(more general) to most derived(more specific).'||chr(10)||' '||chr(10)||'#### Explanation'||chr(10)||'Catch handlers go in order of declaration after try statement. The correct order should from most derived to least derived, from more specific to less specific.'||chr(10)||'If the catch is started from the least derived, it may possibly catch a problem from most derived. This way, the catch handler for most derived will never be executed, and that behaviour is not desirable.'||chr(10)||' '||chr(10)||'#### Example - Avoid'||chr(10)||' '||chr(10)||'```cpp'||chr(10)||' '||chr(10)||'// Classes used for exception handling'||chr(10)||'#include <iostream>'||chr(10)||' '||chr(10)||'class Base {};'||chr(10)||' '||chr(10)||'class Derived1 : public Base {};'||chr(10)||'class Derived2 : public Derived1 {};'||chr(10)||' '||chr(10)||'void foo();'||chr(10)||'void bar();'||chr(10)||' '||chr(10)||'void foo()'||chr(10)||'{'||chr(10)||'    std::cout << "foo" << std::endl;'||chr(10)||'}'||chr(10)||'void bar()'||chr(10)||'{'||chr(10)||'    std::cout << "bar" << std::endl;'||chr(10)||'}'||chr(10)||' '||chr(10)||'void f()'||chr(10)||'{'||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base &) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived1 &)  { // violation here in Line 19'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base &) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived2 &) // violation here in Line 31'||chr(10)||'  {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||' '||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base *) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived1 *) { // violation here in Line 44'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||' '||chr(10)||'int main() {'||chr(10)||'    f();'||chr(10)||'}'||chr(10)||'```'||chr(10)||''||chr(10)||' '||chr(10)||'#### Example - Prefer'||chr(10)||'```cpp'||chr(10)||' '||chr(10)||'// Classes used for exception handling'||chr(10)||'#include <iostream>'||chr(10)||' '||chr(10)||'class Base {};'||chr(10)||' '||chr(10)||'class Derived1 : public Base {};'||chr(10)||'class Derived2 : public Derived1 {};'||chr(10)||' '||chr(10)||'void foo();'||chr(10)||'void bar();'||chr(10)||' '||chr(10)||'void foo()'||chr(10)||'{'||chr(10)||'    std::cout << "foo" << std::endl;'||chr(10)||'}'||chr(10)||'void bar() {'||chr(10)||'    std::cout << "bar" << std::endl;'||chr(10)||'}'||chr(10)||' '||chr(10)||'void f()'||chr(10)||'{'||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived1 &) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base &)'||chr(10)||'  {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived2 &) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base &) //'||chr(10)||'  {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||' '||chr(10)||'  try {'||chr(10)||'   foo();'||chr(10)||'   bar();'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Derived1 *) {'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'  catch (Base *) { //'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||' '||chr(10)||'int main() {'||chr(10)||'    f();'||chr(10)||'}'||chr(10)||' '||chr(10)||'```', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR54-CPP.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line}, the order of catch handler is not properly arranged. It should come before $(ss.line).', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR54-CPP.msg_template', '???${se.filename}??????${se.line}??????${se.func} ?????????????????????????????????????????????????????????$(ss.line)????????????.', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR54-CPP.name', 'Catch handlers should order their parameter types from the most derived to the least derived', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR54-CPP.name', '????????????????????????????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR54-CPP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR54-CPP'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR54-CPP'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

-- ------------------------
-- MEM55-CPP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MEM55-CPP', null, 'MEM55-CPP', 'c++', 'https://wiki.sei.cmu.edu/confluence/display/cplusplus/MEM55-CPP.+Honor+replacement+dynamic+storage+management+requirements', '${rule.Xcalibyte.CERT.1.MEM55-CPP.name}', '1', '1', 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.MEM55-CPP.detail}', '${rule.Xcalibyte.CERT.1.MEM55-CPP.description}', '${rule.Xcalibyte.CERT.1.MEM55-CPP.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MEM55-CPP.description', 'The program has replaced the function for dynamic memory allocation or deallocation that does not meet the semantic requirements specified by the C++ standard.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM55-CPP.description', '?????????????????????????????????C++?????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM55-CPP.detail', '#### Abstract'||chr(10)||'The program has replaced the function for dynamic memory allocation or deallocation that does not meet the semantic requirements specified by the C++ standard.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'The replacement function for allocation contains an extern declaration that may return nullptr. The function "new" will instead return a nullptr instead of the supposed "std::bad_alloc". '||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||''||chr(10)||'```cpp'||chr(10)||''||chr(10)||'#include <cstdio>'||chr(10)||'#include <cstdlib>'||chr(10)||'#include <new>'||chr(10)||'#include <iostream>'||chr(10)||''||chr(10)||'extern void* cust_allocator(std::size_t);'||chr(10)||''||chr(10)||'class Person {'||chr(10)||'    int age; '||chr(10)||'    std::string name;'||chr(10)||'    '||chr(10)||'public:'||chr(10)||'    Person(){}'||chr(10)||'    void* operator new(size_t s)'||chr(10)||'    {'||chr(10)||'        return cust_allocator(s);'||chr(10)||'    }'||chr(10)||'};'||chr(10)||''||chr(10)||'```'||chr(10)||''||chr(10)||'#### Example - Prefer'||chr(10)||''||chr(10)||'```cpp'||chr(10)||''||chr(10)||'#include <cstdio>'||chr(10)||'#include <cstdlib>'||chr(10)||'#include <new>'||chr(10)||'#include <iostream>'||chr(10)||''||chr(10)||'extern void* cust_allocator(std::size_t);'||chr(10)||''||chr(10)||'class Person {'||chr(10)||'    int age; '||chr(10)||'    std::string name;'||chr(10)||'    '||chr(10)||'public:'||chr(10)||'    Person(){}'||chr(10)||'    void* operator new(size_t s)'||chr(10)||'    {'||chr(10)||'        if (((void*) ret = cust_allocator) == 0)'||chr(10)||'            return ret;'||chr(10)||'        return cust_allocator(s);'||chr(10)||'    }'||chr(10)||'};'||chr(10)||''||chr(10)||''||chr(10)||'```', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM55-CPP.detail', '#### ??????'||chr(10)||'?????????????????????????????????C++?????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'???????????????????????????????????????extern??????????????????????????????nullptr??? ?????? "new " ?????????nullptr????????????????????? "std :: bad_alloc "???'||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||'```cpp'||chr(10)||''||chr(10)||'#include <cstdio>'||chr(10)||'#include <cstdlib>'||chr(10)||'#include <new>'||chr(10)||'#include <iostream>'||chr(10)||''||chr(10)||'extern void* cust_allocator(std::size_t);'||chr(10)||''||chr(10)||'class Person {'||chr(10)||'    int age; '||chr(10)||'    std::string name;'||chr(10)||'    '||chr(10)||'public:'||chr(10)||'    Person(){}'||chr(10)||'    void* operator new(size_t s)'||chr(10)||'    {'||chr(10)||'        return cust_allocator(s);'||chr(10)||'    }'||chr(10)||'};'||chr(10)||''||chr(10)||''||chr(10)||'```'||chr(10)||''||chr(10)||'#### Example - ??????'||chr(10)||''||chr(10)||'```cpp'||chr(10)||''||chr(10)||'#include <cstdio>'||chr(10)||'#include <cstdlib>'||chr(10)||'#include <new>'||chr(10)||'#include <iostream>'||chr(10)||''||chr(10)||''||chr(10)||'extern void* cust_allocator(std::size_t);'||chr(10)||''||chr(10)||'class Person {'||chr(10)||'    int age; '||chr(10)||'    std::string name;'||chr(10)||'    '||chr(10)||'public:'||chr(10)||'    Person(){}'||chr(10)||'    void* operator new(size_t s)'||chr(10)||'    {'||chr(10)||'        if (((void*) ret = cust_allocator) == 0)'||chr(10)||'            return ret;'||chr(10)||'        return cust_allocator(s);'||chr(10)||'    }'||chr(10)||'};'||chr(10)||''||chr(10)||'```', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM55-CPP.msg_template', 'In ${se.filename}, ${se.func} line ${se.line}, the replacement function for allocation/deallocation violates the required semantics specified by the C++ standard.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM55-CPP.msg_template', '???${se.filename}??????${se.line}??????${se.func} ????????????????????????/??????????????????????????????C++????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MEM55-CPP.name', 'Honor replacement dynamic storage management requirements', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MEM55-CPP.name', '???????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM55-CPP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM55-CPP'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MEM55-CPP'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

-- ------------------------
-- MSC51-CPP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC51-CPP', null, 'MSC51-CPP', 'c++', 'https://wiki.sei.cmu.edu/confluence/display/cplusplus/MSC51-CPP.+Ensure+your+random+number+generator+is+properly+seeded', '${rule.Xcalibyte.CERT.1.MSC51-CPP.name}', '2', '1', 'LIKELY', 'HIGH', '${rule.Xcalibyte.CERT.1.MSC51-CPP.detail}', '${rule.Xcalibyte.CERT.1.MSC51-CPP.description}', '${rule.Xcalibyte.CERT.1.MSC51-CPP.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MSC51-CPP.description', 'The program doesn''t properly seed the pseudorandom number generator. It has used a constant seed OR a non-constant seed that is predictable (i.e. time). ', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC51-CPP.description', '??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC51-CPP.detail', '#### ??????'||chr(10)||''||chr(10)||'??????????????????????????????????????????????????????????????????????????????????????????????????????????????????, ?????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'????????????????????????????????????????????????????????????PRNG??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - ??????'||chr(10)||''||chr(10)||'```cpp'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <iostream>'||chr(10)||'#include <vector>'||chr(10)||''||chr(10)||'#define MAX_VALUE 100'||chr(10)||'#define VECTOR_SIZE 10'||chr(10)||'int main() '||chr(10)||'{   '||chr(10)||'    std::vector<int> vec;'||chr(10)||'    '||chr(10)||'    for(int i = 0 ; i < VECTOR_SIZE; ++i) {'||chr(10)||'        vec.push_back(rand()%MAX_VALUE);  // using the same initial seed '||chr(10)||'    }'||chr(10)||''||chr(10)||'    // printing'||chr(10)||'    for(int j=0 ; j < vec.size() ; ++j ) {'||chr(10)||'        std::cout << vec[j] << " ";'||chr(10)||'    }'||chr(10)||'    std::cout << ''\n'';'||chr(10)||'}'||chr(10)||'```'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - ??????'||chr(10)||''||chr(10)||'```cpp'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <iostream>'||chr(10)||'#include <vector>'||chr(10)||''||chr(10)||'#define MAX_VALUE 100'||chr(10)||'#define VECTOR_SIZE 10'||chr(10)||''||chr(10)||'int main() '||chr(10)||'{'||chr(10)||'    std::vector<int> vec;'||chr(10)||'    std::random_device rd;'||chr(10)||''||chr(10)||'    srand(rd());  // seed is randomized again for every execution'||chr(10)||'    for(int i = 0 ; i < VECTOR_SIZE; ++i) {'||chr(10)||'        vec.push_back(rand()%MAX_VALUE);  // no repeated sequence   '||chr(10)||'    }'||chr(10)||''||chr(10)||'    for(int j=0 ; j < vec.size() ; ++j ) {'||chr(10)||'        std::cout << vec[j] << " ";'||chr(10)||'    }'||chr(10)||'    std::cout << ''\n'';'||chr(10)||'}'||chr(10)||''||chr(10)||'```'||chr(10)||'', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC51-CPP.detail', '#### Abstract'||chr(10)||'The program doesn''t properly seed the pseudorandom number generator. It has used a constant seed OR it has used a seed that is predictable creating the same sequence over and over.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'When calling pseudorandom number generator (PRNG) with the same initial state, like using the same seed, it will always generate the exact same sequence of numbers. An attacker will be able to easily predict the sequence of numbers and causes security issues.'||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||''||chr(10)||'```cpp'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <iostream>'||chr(10)||'#include <vector>'||chr(10)||''||chr(10)||'#define MAX_VALUE 100'||chr(10)||'#define VECTOR_SIZE 10'||chr(10)||'int main()'||chr(10)||'{   '||chr(10)||'    std::vector<int> vec;'||chr(10)||'    '||chr(10)||'    for(int i = 0 ; i < VECTOR_SIZE; ++i) {'||chr(10)||'        vec.push_back(rand()%MAX_VALUE);  // using the same initial seed '||chr(10)||'    }'||chr(10)||''||chr(10)||'    // printing'||chr(10)||'    for(int j=0 ; j < vec.size() ; ++j ) {'||chr(10)||'        std::cout << vec[j] << " ";'||chr(10)||'    }'||chr(10)||'    std::cout << ''\n'';'||chr(10)||'}'||chr(10)||'```'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - Prefer'||chr(10)||'```cpp'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <iostream>'||chr(10)||'#include <vector>'||chr(10)||''||chr(10)||'#define MAX_VALUE 100'||chr(10)||'#define VECTOR_SIZE 10'||chr(10)||''||chr(10)||'int main() '||chr(10)||'{'||chr(10)||'    std::vector<int> vec;'||chr(10)||'    std::random_device rd;'||chr(10)||''||chr(10)||'    srand(rd());  // seed is randomized again for every execution'||chr(10)||'    for(int i = 0 ; i < VECTOR_SIZE; ++i) {'||chr(10)||'        vec.push_back(rand()%MAX_VALUE);  // no repeated sequence   '||chr(10)||'    }'||chr(10)||''||chr(10)||'    for(int j=0 ; j < vec.size() ; ++j ) {'||chr(10)||'        std::cout << vec[j] << " ";'||chr(10)||'    }'||chr(10)||'    std::cout << ''\n'';'||chr(10)||'}'||chr(10)||''||chr(10)||'```', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC51-CPP.msg_template', 'In ${se.filename}, ${se.func}, line ${se.line} uses a pseudorandom number generator that is not properly seeded. Seed is either constant or predictable.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC51-CPP.msg_template', '???${se.filename}??????${se.line}??????${se.func} ???????????????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC51-CPP.name', 'Ensure your random number generator is properly seeded each time it is run', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC51-CPP.name', '?????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC51-CPP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC51-CPP'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC51-CPP'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MSC54-CPP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC54-CPP', null, 'MSC54-CPP', 'c++', 'https://wiki.sei.cmu.edu/confluence/display/cplusplus/MSC54-CPP.+A+signal+handler+must+be+a+plain+old+function', '${rule.Xcalibyte.CERT.1.MSC54-CPP.name}', '1', '2', 'PROBABLE', 'LOW', '${rule.Xcalibyte.CERT.1.MSC54-CPP.detail}', '${rule.Xcalibyte.CERT.1.MSC54-CPP.description}', '${rule.Xcalibyte.CERT.1.MSC54-CPP.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MSC54-CPP.description', 'The signal handler function in the program is not a plain old function. The particular signal handler function uses pure c++ linkage with no linkage with c.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC54-CPP.description', '????????????????????????????????????????????????????????????(POF)????????????????????????????????????????????????C????????????C++?????????', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC54-CPP.detail', '#### ??????'||chr(10)||'??????????????????????????????????????????????????????????????? ????????????????????????????????????c++ ?????????????????????c????????????'||chr(10)||''||chr(10)||'#### ??????'||chr(10)||'POF??????????????????????????????C???C ++?????????????????????????????????????????????????????????c++????????????????????????????????????????????????????????????c????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||''||chr(10)||''||chr(10)||'```cpp'||chr(10)||''||chr(10)||'#include <iostream>'||chr(10)||'#include <csignal>'||chr(10)||'using namespace std;'||chr(10)||''||chr(10)||'void handle_signal(int signum) '||chr(10)||'{'||chr(10)||' cout << "Signal to interrupt " << signum << endl;'||chr(10)||' exit(signum);'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() '||chr(10)||'{'||chr(10)||'   signal(SIGINT, handle_signal);'||chr(10)||'   // ....'||chr(10)||''||chr(10)||'}'||chr(10)||'```'||chr(10)||''||chr(10)||''||chr(10)||'#### ?????? - ??????'||chr(10)||''||chr(10)||'```cpp'||chr(10)||'#include <iostream>'||chr(10)||'#include <csignal>'||chr(10)||'using namespace std;'||chr(10)||''||chr(10)||'// use C signature'||chr(10)||'extern "C" void handle_signal(int signum) '||chr(10)||'{'||chr(10)||' cout << "Signal to interrupt " << signum << endl;'||chr(10)||' exit(signum);'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() '||chr(10)||'{'||chr(10)||'   signal(SIGINT, handle_signal);'||chr(10)||'   // ....'||chr(10)||''||chr(10)||'}'||chr(10)||'```'||chr(10)||'', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC54-CPP.detail', '#### Abstract'||chr(10)||'The signal handler function in the program is not a plain old function. The particular signal handler function uses pure c++ linkage and no linkage with c. '||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'POF or ''Plain Old Function'' is a function that uses feature on the common subset between C and C++ language. All signal handlers declared in a c++ source code must have a C linkage, otherwise it may cause undefined behaviour.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||'```cpp'||chr(10)||''||chr(10)||'#include <iostream>'||chr(10)||'#include <csignal>'||chr(10)||'using namespace std;'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'void handle_signal(int signum) '||chr(10)||'{'||chr(10)||' cout << "Signal to interrupt " << signum << endl;'||chr(10)||' exit(signum);'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() '||chr(10)||'{'||chr(10)||'   signal(SIGINT, handle_signal);'||chr(10)||'   // ....'||chr(10)||''||chr(10)||'}'||chr(10)||'```'||chr(10)||''||chr(10)||'#### Example - Prefer'||chr(10)||''||chr(10)||'```cpp'||chr(10)||'#include <iostream>'||chr(10)||'#include <csignal>'||chr(10)||'using namespace std;'||chr(10)||''||chr(10)||'// user C signature'||chr(10)||'extern "C" void handle_signal(int signum)'||chr(10)||'{'||chr(10)||' cout << "Signal to interrupt " << signum << endl;'||chr(10)||' exit(signum);'||chr(10)||'}'||chr(10)||''||chr(10)||'int main() '||chr(10)||'{'||chr(10)||'   signal(SIGINT, handle_signal);'||chr(10)||'   // ....'||chr(10)||''||chr(10)||'}'||chr(10)||'```'||chr(10)||'', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC54-CPP.msg_template', 'In ${se.filename}, ${se.func}, it is not a POF (plain old function) even though it exhibits a signal handler behaviour.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC54-CPP.msg_template', '???${se.filename}??????${se.line}??????${se.func} ??????????????????POF?????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC54-CPP.name', 'All signal handlers must be a plain old function', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC54-CPP.name', '????????????????????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC54-CPP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC54-CPP'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC54-CPP'),
 'BASIC','LANG','c++')
ON CONFLICT DO NOTHING;

-- ------------------------
-- END
-- ------------------------
