-- ------------------------
-- Standard OWASP 2017
-- ------------------------
insert into xcalibyte.rule_standard_set
(name, version, revision, display_name, description, language, url, provider, provider_url, license, license_url, created_by, modified_by)
values
('OWASP', 2017, null, 'OWASP', 'OWASP', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/', 'The Open Web Application Security Project', 'https://owasp.org', null, null, 'system', 'system')
ON CONFLICT DO NOTHING;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.detectability.1', 'Difficult', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.detectability.1', 'Difficult', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.detectability.2', 'Average', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.detectability.2', 'Average', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.detectability.3', 'Easy', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.detectability.3', 'Easy', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.exploitability.1', 'Difficult', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.exploitability.1', 'Difficult', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.exploitability.2', 'Average', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.exploitability.2', 'Average', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.exploitability.3', 'Easy', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.exploitability.3', 'Easy', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.prevalence.1', 'Uncommon', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.prevalence.1', 'Uncommon', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.prevalence.2', 'Common', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.prevalence.2', 'Common', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.prevalence.3', 'Widespread', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.prevalence.3', 'Widespread', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.technical.1', 'Minor', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.technical.1', 'Minor', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.technical.2', 'Moderate', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.technical.2', 'Moderate', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.technical.3', 'Severe', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.technical.3', 'Severe', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

-- ------------------------
-- OWASP 2017 1
-- ------------------------
insert into xcalibyte.rule_standard
(rule_standard_set_id, category, code, language, url, name, detail, description, msg_template, created_by, modified_by)
values
((select rule_standard_set.id from xcalibyte."rule_standard_set" where "rule_standard_set".name = 'OWASP' and "rule_standard_set".version = '2017'), null, '01', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A1-Injection.html', '${rule.standard.OWASP.2017.01.name}', '${rule.standard.OWASP.2017.01.detail}', '${rule.standard.OWASP.2017.01.description}', null, 'system', 'system')
ON CONFLICT (rule_standard_set_id, code) DO UPDATE set category = excluded.category, language = excluded.language, url = excluded.url, name = excluded.name, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.01.description', 'The program has injection vulnerabilities', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.01.description', '????????????????????????', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.01.detail', '#### Abstract'||chr(10)||'The program has injection vulnerabilities. '||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Injection can occur when input to a query or command interpreter failed to be validated before the query. The query could be SQL, LDAP or OS commands. When elements of the query originate from an untrusted source, the unsanitized input may be maliciously formulated to trick the interpreter into executing unintended commands or accessing data without proper authentication; resulting in data loss, data corruption, and disclosure of private data. Sometimes it may lead to complete hostile takeover.'||chr(10)||'||chr(10)||'||chr(10)||'#### Example in C'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <string.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'enum { BUF_SZ = 256 };'||chr(10)||''||chr(10)||'int os_command_inject(char **argv)'||chr(10)||'{'||chr(10)||'  char buffer[BUF_SZ+1];'||chr(10)||'  '||chr(10)||'  char *s = strncat(buffer, "echo ", BUF_SZ);'||chr(10)||'  buffer[BUF_SZ] = ''\0'';    // make sure buffer is null terminated'||chr(10)||'  system(buffer);           // if user input includes any OS command'||chr(10)||'                            // that will be executed as typed'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.01.detail', '#### ??????'||chr(10)||'????????????????????????'||chr(10)||'#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????????????????????????????????????????????????????SQL???LDAP?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <string.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'enum { BUF_SZ = 256 };'||chr(10)||''||chr(10)||'int os_command_inject(char **argv)'||chr(10)||'{'||chr(10)||'  char buffer[BUF_SZ+1];'||chr(10)||'  '||chr(10)||'  char *s = strncat(buffer, "echo ", BUF_SZ);'||chr(10)||'  buffer[BUF_SZ] = ''\0'';    // make sure buffer is null terminated'||chr(10)||'  system(buffer);           // if user input includes any OS command'||chr(10)||'                            // that will be executed as typed'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.01.name', 'Injection', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.01.name', '??????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_standard_attribute rsa
where rsa.rule_standard_id =
      (select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
       where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='01');

insert into xcalibyte.rule_standard_attribute
(rule_standard_id, type, name, value)
values
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='01'),
 'BASIC', 'exploitability', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='01'),
 'BASIC', 'prevalence', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='01'),
 'BASIC', 'detectability', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='01'),
 'BASIC', 'technical', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='01'),
 'BASIC', 'score', '8.0');

-- ------------------------
-- OWASP 2017 2
-- ------------------------
insert into xcalibyte.rule_standard
(rule_standard_set_id, category, code, language, url, name, detail, description, msg_template, created_by, modified_by)
values
((select rule_standard_set.id from xcalibyte."rule_standard_set" where "rule_standard_set".name = 'OWASP' and "rule_standard_set".version = '2017'), null, '02', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A2-Broken_Authentication.html', '${rule.standard.OWASP.2017.02.name}', '${rule.standard.OWASP.2017.02.detail}', '${rule.standard.OWASP.2017.02.description}', null, 'system', 'system')
ON CONFLICT (rule_standard_set_id, code) DO UPDATE set category = excluded.category, language = excluded.language, url = excluded.url, name = excluded.name, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.02.description', 'The program has authentication functions that are vulnerable to being exploited.', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.02.description', '???????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.02.detail', '#### Abstract'||chr(10)||'The program has authentication functions that are vulnerable to being exploited.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Application functions related to authentication and session management are often implemented incorrectly, allowing attackers to compromise passwords, keys, or session tokens, or to exploit other implementation flaws to assume other users??? identities temporarily or permanently.'||chr(10)||''||chr(10)||'#### Example in C'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern debug_print(char *);'||chr(10)||'extern int check_password(char *pw);'||chr(10)||''||chr(10)||'int process_password(void)'||chr(10)||'{'||chr(10)||'  // pass word hard code for debugging purpose left in code'||chr(10)||'  if (check_password("debugging") {'||chr(10)||'    debug_print("password OK");'||chr(10)||'    return 1;'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    debug_print("password invalid");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.02.detail', '#### ??????'||chr(10)||'???????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern debug_print(char *);'||chr(10)||'extern int check_password(char *pw);'||chr(10)||''||chr(10)||'int process_password(void)'||chr(10)||'{'||chr(10)||'  // pass word hard code for debugging purpose left in code'||chr(10)||'  if (check_password("debugging") {'||chr(10)||'    debug_print("password OK");'||chr(10)||'    return 1;'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    debug_print("password invalid");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.02.name', 'Broken authentication', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.02.name', '?????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_standard_attribute rsa
where rsa.rule_standard_id =
      (select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
       where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='02');

insert into xcalibyte.rule_standard_attribute
(rule_standard_id, type, name, value)
values
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='02'),
 'BASIC', 'exploitability', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='02'),
 'BASIC', 'prevalence', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='02'),
 'BASIC', 'detectability', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='02'),
 'BASIC', 'technical', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='02'),
 'BASIC', 'score', '7.0');

-- ------------------------
-- OWASP 2017 3
-- ------------------------
insert into xcalibyte.rule_standard
(rule_standard_set_id, category, code, language, url, name, detail, description, msg_template, created_by, modified_by)
values
((select rule_standard_set.id from xcalibyte."rule_standard_set" where "rule_standard_set".name = 'OWASP' and "rule_standard_set".version = '2017'), null, '03', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A3-Sensitive_Data_Exposure.html', '${rule.standard.OWASP.2017.03.name}', '${rule.standard.OWASP.2017.03.detail}', '${rule.standard.OWASP.2017.03.description}', null, 'system', 'system')
ON CONFLICT (rule_standard_set_id, code) DO UPDATE set category = excluded.category, language = excluded.language, url = excluded.url, name = excluded.name, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.03.description', 'The program has sensitive data that is not properly protected.', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.03.description', '????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.03.detail', '#### Abstract'||chr(10)||'The program has sensitive data that is not properly protected.'||chr(10)||'#### Explanation'||chr(10)||'Sensitive data is not properly protected (such as weak encryption, unsalted hash, plain text etc). Such data should be protected while in storage or while being transmitted. Most sensitive information such as health records, personal data and credit card information require strong protection as defined by local privacy laws or EU GDPR regulation.'||chr(10)||'#### Example in C'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern debug_print(char *);'||chr(10)||'extern int check_password(char *pw);'||chr(10)||'enum { BUF_SZ = 256 };'||chr(10)||''||chr(10)||'int process_password(void)'||chr(10)||'{'||chr(10)||'  char passwd_str[BUF_SZ];'||chr(10)||'  '||chr(10)||'  fgets(passwd_str, BUF_SZ, stdin);  // get input password '||chr(10)||'  '||chr(10)||'  if (check_password(password_str) {'||chr(10)||'    debug_print("password OK");'||chr(10)||'    // return without erasing password in memory'||chr(10)||'    // if program was maliciously aborted, password may remain in memory '||chr(10)||'    return 1;'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    debug_print("password invalid");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.03.detail', '#### ??????'||chr(10)||'????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????GDPR????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern debug_print(char *);'||chr(10)||'extern int check_password(char *pw);'||chr(10)||'enum { BUF_SZ = 256 };'||chr(10)||''||chr(10)||'int process_password(void)'||chr(10)||'{'||chr(10)||'  char passwd_str[BUF_SZ];'||chr(10)||'  '||chr(10)||'  fgets(passwd_str, BUF_SZ, stdin);  // get input password '||chr(10)||'  '||chr(10)||'  if (check_password(password_str) {'||chr(10)||'    debug_print("password OK");'||chr(10)||'    // return without erasing password in memory'||chr(10)||'    // if program was maliciously aborted, password may remain in memory '||chr(10)||'    return 1;'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    debug_print("password invalid");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.03.name', 'Sensitive data exposure', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.03.name', '??????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_standard_attribute rsa
where rsa.rule_standard_id =
      (select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
       where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='03');

insert into xcalibyte.rule_standard_attribute
(rule_standard_id, type, name, value)
values
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='03'),
 'BASIC', 'exploitability', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='03'),
 'BASIC', 'prevalence', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='03'),
 'BASIC', 'detectability', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='03'),
 'BASIC', 'technical', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='03'),
 'BASIC', 'score', '7.0');
-- ------------------------
-- OWASP 2017 4
-- ------------------------
insert into xcalibyte.rule_standard
(rule_standard_set_id, category, code, language, url, name, detail, description, msg_template, created_by, modified_by)
values
((select rule_standard_set.id from xcalibyte."rule_standard_set" where "rule_standard_set".name = 'OWASP' and "rule_standard_set".version = '2017'), null, '04', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A4-XML_External_Entities_(XXE).html', '${rule.standard.OWASP.2017.04.name}', '${rule.standard.OWASP.2017.04.detail}', '${rule.standard.OWASP.2017.04.description}', null, 'system', 'system')
ON CONFLICT (rule_standard_set_id, code) DO UPDATE set category = excluded.category, language = excluded.language, url = excluded.url, name = excluded.name, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.04.description', 'The program is using entity references through an external entity specified by a URI handle or document type definition (DTD). ', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.04.description', '???????????????URI?????????????????????????????????DTD?????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.04.detail', '#### Abstract'||chr(10)||'The program is using entity references through an external entity specified by a URI handle or document type definition (DTD). '||chr(10)||'#### Explanation'||chr(10)||'Attackers can manipulate the URI during XML processing to refer to a local file system containing sensitive data, resulting in unexpected data extraction or injection attacks.'||chr(10)||''||chr(10)||'#### Example in C'||chr(10)||'````text'||chr(10)||'// if the utility that creates DTD files is written in C'||chr(10)||'// and if there are DOCTYPE that has ENTITY such as '||chr(10)||'// "file:///etc/passwd" or'||chr(10)||'// handle to objects with sensitive information'||chr(10)||'// The string should be sanitized before processing into DTD entity'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.04.detail', '#### ??????'||chr(10)||'???????????????URI?????????????????????????????????DTD?????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'??????????????????XML?????????????????????URI?????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||'// if the utility that creates DTD files is written in C'||chr(10)||'// and if there are DOCTYPE that has ENTITY such as '||chr(10)||'// "file:///etc/passwd" or'||chr(10)||'// handle to objects with sensitive information'||chr(10)||'// The string should be sanitized before processing into DTD entity'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.04.name', 'XML external entities (XXE)', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.04.name', 'XML???????????? (XXE)', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_standard_attribute rsa
where rsa.rule_standard_id =
      (select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
       where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='04');

insert into xcalibyte.rule_standard_attribute
(rule_standard_id, type, name, value)
values
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='04'),
 'BASIC', 'exploitability', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='04'),
 'BASIC', 'prevalence', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='04'),
 'BASIC', 'detectability', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='04'),
 'BASIC', 'technical', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='04'),
 'BASIC', 'score', '7.0');

-- ------------------------
-- OWASP 2017 5
-- ------------------------
insert into xcalibyte.rule_standard
(rule_standard_set_id, category, code, language, url, name, detail, description, msg_template, created_by, modified_by)
values
((select rule_standard_set.id from xcalibyte."rule_standard_set" where "rule_standard_set".name = 'OWASP' and "rule_standard_set".version = '2017'), null, '05', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A5-Broken_Access_Control.html', '${rule.standard.OWASP.2017.05.name}', '${rule.standard.OWASP.2017.05.detail}', '${rule.standard.OWASP.2017.05.description}', null, 'system', 'system')
ON CONFLICT (rule_standard_set_id, code) DO UPDATE set category = excluded.category, language = excluded.language, url = excluded.url, name = excluded.name, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.05.description', 'The program has improperly enforced access control or restriction measures that may allow trusted boundary to be bypassed.', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.05.description', '???????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.05.detail', '#### Abstract'||chr(10)||'The program has improperly enforced access control or restriction measures that may allow trusted boundary to be bypassed.'||chr(10)||'#### Explanation'||chr(10)||'Restrictions on what authenticated users are allowed to do are often not properly enforced. Attackers can exploit these flaws to access unauthorized functionality and/or data, such as access other users??? accounts, view sensitive files, modify other users??? data, change access rights, etc.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'struct stat link_info;'||chr(10)||''||chr(10)||'int func(void) '||chr(10)||'{'||chr(10)||'  // the following check alone is not sufficient. need to make sure privilege can be restored '||chr(10)||'  if (setuid(getuid() != 0)) {'||chr(10)||'    printf(" inside error set/get uid\n");'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // check for failuer to setuid call when caller is UID 0 is needed below'||chr(10)||'  if (setuid() != 0) {'||chr(10)||'    printf("inside error set uid\n");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'  return 1;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.05.detail', '#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'struct stat link_info;'||chr(10)||''||chr(10)||'int func(void) '||chr(10)||'{'||chr(10)||'  // the following check alone is not sufficient. need to make sure privilege can be restored '||chr(10)||'  if (setuid(getuid() != 0)) {'||chr(10)||'    printf(" inside error set/get uid\n");'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // check for failuer to setuid call when caller is UID 0 is needed below'||chr(10)||'  if (setuid() != 0) {'||chr(10)||'    printf("inside error set uid\n");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'  return 1;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.05.name', 'Broken access control', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.05.name', '?????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_standard_attribute rsa
where rsa.rule_standard_id =
      (select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
       where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='05');

insert into xcalibyte.rule_standard_attribute
(rule_standard_id, type, name, value)
values
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='05'),
 'BASIC', 'exploitability', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='05'),
 'BASIC', 'prevalence', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='05'),
 'BASIC', 'detectability', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='05'),
 'BASIC', 'technical', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='05'),
 'BASIC', 'score', '6.0');

-- ------------------------
-- OWASP 2017 6
-- ------------------------
insert into xcalibyte.rule_standard
(rule_standard_set_id, category, code, language, url, name, detail, description, msg_template, created_by, modified_by)
values
((select rule_standard_set.id from xcalibyte."rule_standard_set" where "rule_standard_set".name = 'OWASP' and "rule_standard_set".version = '2017'), null, '06', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A6-Security_Misconfiguration.html', '${rule.standard.OWASP.2017.06.name}', '${rule.standard.OWASP.2017.06.detail}', '${rule.standard.OWASP.2017.06.description}', null, 'system', 'system')
ON CONFLICT (rule_standard_set_id, code) DO UPDATE set category = excluded.category, language = excluded.language, url = excluded.url, name = excluded.name, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.06.description', 'The program or a component of the program (framework, libraries, OS etc) has insecure configuration.', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.06.description', '???????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.06.detail', '#### Abstract'||chr(10)||'The program or a component of the program (framework, libraries, OS etc) has insecure configuration.'||chr(10)||'#### Explanation'||chr(10)||'Security misconfiguration can happen along the entire execution stack of the program. It is important to ensure that all components are properly updated and have the proper security configuration. Failure to do so may give attackers unauthorized access to system data, files and accounts.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example in C'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <dlfcn.h>'||chr(10)||''||chr(10)||'int open_dso(char *dso_fname)'||chr(10)||'{'||chr(10)||'  void *handle;'||chr(10)||'  '||chr(10)||'  if (dso_fname == 0) {'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  // if dso_fname refers to a dso in shared directory'||chr(10)||'  // the .so opened may not be what is expected'||chr(10)||'  handle = dlopen(dso_fname, RTLD_LAZY);'||chr(10)||'  if (!handle) {'||chr(10)||'    // fail to load'||chr(10)||'    return 0;'||chr(10)||'    '||chr(10)||'  }'||chr(10)||'    '||chr(10)||'  // success '||chr(10)||'  // further program logic'||chr(10)||'  return 1;'||chr(10)||'}'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.06.detail', '#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <dlfcn.h>'||chr(10)||''||chr(10)||'int open_dso(char *dso_fname)'||chr(10)||'{'||chr(10)||'  void *handle;'||chr(10)||'  '||chr(10)||'  if (dso_fname == 0) {'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  // if dso_fname refers to a dso in shared directory'||chr(10)||'  // the .so opened may not be what is expected'||chr(10)||'  handle = dlopen(dso_fname, RTLD_LAZY);'||chr(10)||'  if (!handle) {'||chr(10)||'    // fail to load'||chr(10)||'    return 0;'||chr(10)||'    '||chr(10)||'  }'||chr(10)||'    '||chr(10)||'  // success '||chr(10)||'  // further program logic'||chr(10)||'  return 1;'||chr(10)||'}'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.06.name', 'Security misconfiguration', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.06.name', '??????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_standard_attribute rsa
where rsa.rule_standard_id =
      (select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
       where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='06');

insert into xcalibyte.rule_standard_attribute
(rule_standard_id, type, name, value)
values
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='06'),
 'BASIC', 'exploitability', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='06'),
 'BASIC', 'prevalence', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='06'),
 'BASIC', 'detectability', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='06'),
 'BASIC', 'technical', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='06'),
 'BASIC', 'score', '6.0');


-- ------------------------
-- OWASP 2017 7
-- ------------------------
insert into xcalibyte.rule_standard
(rule_standard_set_id, category, code, language, url, name, detail, description, msg_template, created_by, modified_by)
values
((select rule_standard_set.id from xcalibyte."rule_standard_set" where "rule_standard_set".name = 'OWASP' and "rule_standard_set".version = '2017'), null, '07', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A7-Cross-Site_Scripting_(XSS).html', '${rule.standard.OWASP.2017.07.name}', '${rule.standard.OWASP.2017.07.detail}', '${rule.standard.OWASP.2017.07.description}', null, 'system', 'system')
ON CONFLICT (rule_standard_set_id, code) DO UPDATE set category = excluded.category, language = excluded.language, url = excluded.url, name = excluded.name, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.07.description', 'The program is not sanitizing input data, allowing it to bypass validation. Attackers can then execute scripts in the client browser.', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.07.description', '??????????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.07.detail', '#### Abstract'||chr(10)||'The program is not sanitizing input data, allowing it to bypass validation. Attackers can then execute scripts in the client browser.'||chr(10)||'#### Explanation'||chr(10)||'Unvalidated and unescaped user input data can be used by attackers to hijack the outputting subsystem (such as the browser) to perform malicious activities including key logging, execution of arbitrary code or other malicious takeovers.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.07.detail', '#### ??????'||chr(10)||'??????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.07.name', 'Cross site scripting (XSS)', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.07.name', '??????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_standard_attribute rsa
where rsa.rule_standard_id =
      (select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
       where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='07');

insert into xcalibyte.rule_standard_attribute
(rule_standard_id, type, name, value)
values
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='07'),
 'BASIC', 'exploitability', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='07'),
 'BASIC', 'prevalence', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='07'),
 'BASIC', 'detectability', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='07'),
 'BASIC', 'technical', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='07'),
 'BASIC', 'score', '6.0');


-- ------------------------
-- OWASP 2017 8
-- ------------------------
insert into xcalibyte.rule_standard
(rule_standard_set_id, category, code, language, url, name, detail, description, msg_template, created_by, modified_by)
values
((select rule_standard_set.id from xcalibyte."rule_standard_set" where "rule_standard_set".name = 'OWASP' and "rule_standard_set".version = '2017'), null, '08', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A8-Insecure_Deserialization.html', '${rule.standard.OWASP.2017.08.name}', '${rule.standard.OWASP.2017.08.detail}', '${rule.standard.OWASP.2017.08.description}', null, 'system', 'system')
ON CONFLICT (rule_standard_set_id, code) DO UPDATE set category = excluded.category, language = excluded.language, url = excluded.url, name = excluded.name, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.08.description', 'The program has deserialization methods that may bypass security measures or have privileges downgraded before deserialization.', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.08.description', '???????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.08.detail', '#### Abstract'||chr(10)||'The program has deserialization methods that may bypass security measures or have privileges downgraded before deserialization.'||chr(10)||'#### Explanation'||chr(10)||'Unrestricted deserialization from within a privileged context allows an attacker to create an input that is otherwise not allowed. This could result in various attacks such as replay, data tampering, injection and privileged escalation.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.08.detail', '#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.08.name', 'Insecure deserialization', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.08.name', '????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_standard_attribute rsa
where rsa.rule_standard_id =
      (select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
       where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='08');

insert into xcalibyte.rule_standard_attribute
(rule_standard_id, type, name, value)
values
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='08'),
 'BASIC', 'exploitability', '1'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='08'),
 'BASIC', 'prevalence', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='08'),
 'BASIC', 'detectability', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='08'),
 'BASIC', 'technical', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='08'),
 'BASIC', 'score', '5.0');

-- ------------------------
-- OWASP 2017 9
-- ------------------------
insert into xcalibyte.rule_standard
(rule_standard_set_id, category, code, language, url, name, detail, description, msg_template, created_by, modified_by)
values
((select rule_standard_set.id from xcalibyte."rule_standard_set" where "rule_standard_set".name = 'OWASP' and "rule_standard_set".version = '2017'), null, '09', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A9-Using_Components_with_Known_Vulnerabilities.html', '${rule.standard.OWASP.2017.09.name}', '${rule.standard.OWASP.2017.09.detail}', '${rule.standard.OWASP.2017.09.description}', null, 'system', 'system')
ON CONFLICT (rule_standard_set_id, code) DO UPDATE set category = excluded.category, language = excluded.language, url = excluded.url, name = excluded.name, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.09.description', 'The program is using components with know vulnerabilities.', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.09.description', '???????????????????????????????????????', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.09.detail', '#### Abstract'||chr(10)||'The program is using components with know vulnerabilities.'||chr(10)||'#### Explanation'||chr(10)||'Components used in the program should include the latest fixes or patches for vulnerabilities. This also includes using the right level of encryption protection and security configuration needed for your program.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.09.detail', '#### ??????'||chr(10)||'???????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.09.name', 'Using components with known vulnerabilities', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.09.name', '?????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_standard_attribute rsa
where rsa.rule_standard_id =
      (select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
       where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='09');

insert into xcalibyte.rule_standard_attribute
(rule_standard_id, type, name, value)
values
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='09'),
 'BASIC', 'exploitability', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='09'),
 'BASIC', 'prevalence', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='09'),
 'BASIC', 'detectability', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='09'),
 'BASIC', 'technical', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='09'),
 'BASIC', 'score', '4.7');

-- ------------------------
-- OWASP 2017 10
-- ------------------------
insert into xcalibyte.rule_standard
(rule_standard_set_id, category, code, language, url, name, detail, description, msg_template, created_by, modified_by)
values
((select rule_standard_set.id from xcalibyte."rule_standard_set" where "rule_standard_set".name = 'OWASP' and "rule_standard_set".version = '2017'), null, '10', null, 'https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A10-Insufficient_Logging%252526Monitoring.html', '${rule.standard.OWASP.2017.10.name}', '${rule.standard.OWASP.2017.10.detail}', '${rule.standard.OWASP.2017.10.description}', null, 'system', 'system')
ON CONFLICT (rule_standard_set_id, code) DO UPDATE set category = excluded.category, language = excluded.language, url = excluded.url, name = excluded.name, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.standard.OWASP.2017.10.description', 'The program is lacking in logging and monitoring that allows for detection of breaches, or has disabled verification of runtime integrity.', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.10.description', '????????????????????????????????????????????????????????????????????????????????????????????????', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.10.detail', '#### Abstract'||chr(10)||'The program is lacking in logging and monitoring that allows for detection of breaches, or has disabled verification of runtime integrity. '||chr(10)||'#### Explanation'||chr(10)||'It is important to enable all internal measures at runtime that verify, check and monitor the system. Failure to do so results in failed or delayed detection of a data breach and/ or the discovery of destroyed data. Businesses should expect to detect, escalate and alert for active attacks in near or real time. '||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.10.detail', '#### ??????'||chr(10)||'????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||'#### ??????'||chr(10)||'????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????/??????????????????????????????????????????????????????????????????????????????????????????????????????????????????'||chr(10)||''||chr(10)||'#### ?????? - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.10.name', 'Insufficient logging and monitoring', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.10.name', '?????????????????????????????????', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_standard_attribute rsa
where rsa.rule_standard_id =
      (select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
       where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='10');

insert into xcalibyte.rule_standard_attribute
(rule_standard_id, type, name, value)
values
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='10'),
 'BASIC', 'exploitability', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='10'),
 'BASIC', 'prevalence', '3'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='10'),
 'BASIC', 'detectability', '1'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='10'),
 'BASIC', 'technical', '2'),
((select rs.id from xcalibyte."rule_standard" rs left join xcalibyte."rule_standard_set" rss on rss.id =rs.rule_standard_set_id
  where rss.name = 'OWASP' and rss.version ='2017' and rs.code ='10'),
 'BASIC', 'score', '4.0');