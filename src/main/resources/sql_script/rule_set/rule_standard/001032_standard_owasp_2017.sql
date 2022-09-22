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
('zh-CN', 'rule.standard.OWASP.2017.01.description', '程序具有注入漏洞', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.01.detail', '#### Abstract'||chr(10)||'The program has injection vulnerabilities. '||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Injection can occur when input to a query or command interpreter failed to be validated before the query. The query could be SQL, LDAP or OS commands. When elements of the query originate from an untrusted source, the unsanitized input may be maliciously formulated to trick the interpreter into executing unintended commands or accessing data without proper authentication; resulting in data loss, data corruption, and disclosure of private data. Sometimes it may lead to complete hostile takeover.'||chr(10)||'||chr(10)||'||chr(10)||'#### Example in C'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <string.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'enum { BUF_SZ = 256 };'||chr(10)||''||chr(10)||'int os_command_inject(char **argv)'||chr(10)||'{'||chr(10)||'  char buffer[BUF_SZ+1];'||chr(10)||'  '||chr(10)||'  char *s = strncat(buffer, "echo ", BUF_SZ);'||chr(10)||'  buffer[BUF_SZ] = ''\0'';    // make sure buffer is null terminated'||chr(10)||'  system(buffer);           // if user input includes any OS command'||chr(10)||'                            // that will be executed as typed'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.01.detail', '#### 摘要'||chr(10)||'程序具有注入漏洞'||chr(10)||'#### 解释'||chr(10)||'当查询或者命令解释器的输入在查询前未能被验证时，可能会发生注入。查询可以是SQL、LDAP或者操作系统命令。当查询的元素来自于不受信任来源时，未净化的输入可能被恶意地公式化，用以欺骗解释器执行非预期的命令或者未经正当认证授权访问数据，导致数据丢失、数据损坏和隐私数据泄露。有时这可能会导致完全的恶意接管。'||chr(10)||''||chr(10)||'#### 示例 - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <string.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'enum { BUF_SZ = 256 };'||chr(10)||''||chr(10)||'int os_command_inject(char **argv)'||chr(10)||'{'||chr(10)||'  char buffer[BUF_SZ+1];'||chr(10)||'  '||chr(10)||'  char *s = strncat(buffer, "echo ", BUF_SZ);'||chr(10)||'  buffer[BUF_SZ] = ''\0'';    // make sure buffer is null terminated'||chr(10)||'  system(buffer);           // if user input includes any OS command'||chr(10)||'                            // that will be executed as typed'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.01.name', 'Injection', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.01.name', '注入', 'system', 'system')
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
('zh-CN', 'rule.standard.OWASP.2017.02.description', '程序具有易被利用缺陷的身份认证功能', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.02.detail', '#### Abstract'||chr(10)||'The program has authentication functions that are vulnerable to being exploited.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Application functions related to authentication and session management are often implemented incorrectly, allowing attackers to compromise passwords, keys, or session tokens, or to exploit other implementation flaws to assume other users’ identities temporarily or permanently.'||chr(10)||''||chr(10)||'#### Example in C'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern debug_print(char *);'||chr(10)||'extern int check_password(char *pw);'||chr(10)||''||chr(10)||'int process_password(void)'||chr(10)||'{'||chr(10)||'  // pass word hard code for debugging purpose left in code'||chr(10)||'  if (check_password("debugging") {'||chr(10)||'    debug_print("password OK");'||chr(10)||'    return 1;'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    debug_print("password invalid");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.02.detail', '#### 摘要'||chr(10)||'程序具有易被利用缺陷的身份认证功能'||chr(10)||'#### 解释'||chr(10)||'通过错误使用应用程序的身份认证和会话管理功能，攻击者能够破译密码、密钥或会话令牌，或者利用其它开发缺陷来暂时性或永久性冒充其它用户的身份。'||chr(10)||''||chr(10)||'#### 示例 - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern debug_print(char *);'||chr(10)||'extern int check_password(char *pw);'||chr(10)||''||chr(10)||'int process_password(void)'||chr(10)||'{'||chr(10)||'  // pass word hard code for debugging purpose left in code'||chr(10)||'  if (check_password("debugging") {'||chr(10)||'    debug_print("password OK");'||chr(10)||'    return 1;'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    debug_print("password invalid");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.02.name', 'Broken authentication', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.02.name', '失效的身份认证', 'system', 'system')
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
('zh-CN', 'rule.standard.OWASP.2017.03.description', '程序包含未得到适当保护的敏感数据', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.03.detail', '#### Abstract'||chr(10)||'The program has sensitive data that is not properly protected.'||chr(10)||'#### Explanation'||chr(10)||'Sensitive data is not properly protected (such as weak encryption, unsalted hash, plain text etc). Such data should be protected while in storage or while being transmitted. Most sensitive information such as health records, personal data and credit card information require strong protection as defined by local privacy laws or EU GDPR regulation.'||chr(10)||'#### Example in C'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern debug_print(char *);'||chr(10)||'extern int check_password(char *pw);'||chr(10)||'enum { BUF_SZ = 256 };'||chr(10)||''||chr(10)||'int process_password(void)'||chr(10)||'{'||chr(10)||'  char passwd_str[BUF_SZ];'||chr(10)||'  '||chr(10)||'  fgets(passwd_str, BUF_SZ, stdin);  // get input password '||chr(10)||'  '||chr(10)||'  if (check_password(password_str) {'||chr(10)||'    debug_print("password OK");'||chr(10)||'    // return without erasing password in memory'||chr(10)||'    // if program was maliciously aborted, password may remain in memory '||chr(10)||'    return 1;'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    debug_print("password invalid");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.03.detail', '#### 摘要'||chr(10)||'程序包含未得到适当保护的敏感数据'||chr(10)||'#### 解释'||chr(10)||'敏感数据未得到适当保护（如弱加密、未加密的哈希，纯文本等）。此类数据在存储或传输时应受到保护。大多数敏感信息，如健康记录、个人数据和信用卡数据，都需要受到当地隐私法或欧盟GDPR法规的严格保护。'||chr(10)||''||chr(10)||'#### 示例 - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'extern debug_print(char *);'||chr(10)||'extern int check_password(char *pw);'||chr(10)||'enum { BUF_SZ = 256 };'||chr(10)||''||chr(10)||'int process_password(void)'||chr(10)||'{'||chr(10)||'  char passwd_str[BUF_SZ];'||chr(10)||'  '||chr(10)||'  fgets(passwd_str, BUF_SZ, stdin);  // get input password '||chr(10)||'  '||chr(10)||'  if (check_password(password_str) {'||chr(10)||'    debug_print("password OK");'||chr(10)||'    // return without erasing password in memory'||chr(10)||'    // if program was maliciously aborted, password may remain in memory '||chr(10)||'    return 1;'||chr(10)||'  }'||chr(10)||'  else {'||chr(10)||'    debug_print("password invalid");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.03.name', 'Sensitive data exposure', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.03.name', '敏感数据泄露', 'system', 'system')
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
('zh-CN', 'rule.standard.OWASP.2017.04.description', '程序通过由URI句柄或者文档类型定义（DTD）指定的外部实体对实体进行引用', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.04.detail', '#### Abstract'||chr(10)||'The program is using entity references through an external entity specified by a URI handle or document type definition (DTD). '||chr(10)||'#### Explanation'||chr(10)||'Attackers can manipulate the URI during XML processing to refer to a local file system containing sensitive data, resulting in unexpected data extraction or injection attacks.'||chr(10)||''||chr(10)||'#### Example in C'||chr(10)||'````text'||chr(10)||'// if the utility that creates DTD files is written in C'||chr(10)||'// and if there are DOCTYPE that has ENTITY such as '||chr(10)||'// "file:///etc/passwd" or'||chr(10)||'// handle to objects with sensitive information'||chr(10)||'// The string should be sanitized before processing into DTD entity'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.04.detail', '#### 摘要'||chr(10)||'程序通过由URI句柄或者文档类型定义（DTD）指定的外部实体对实体进行引用'||chr(10)||'#### 解释'||chr(10)||'攻击者可以在XML处理过程中操纵URI来引用包含敏感数据的本地文件系统，从而导致意外的数据提取或者注入攻击。'||chr(10)||''||chr(10)||'#### 示例 - C'||chr(10)||'````text'||chr(10)||'// if the utility that creates DTD files is written in C'||chr(10)||'// and if there are DOCTYPE that has ENTITY such as '||chr(10)||'// "file:///etc/passwd" or'||chr(10)||'// handle to objects with sensitive information'||chr(10)||'// The string should be sanitized before processing into DTD entity'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.04.name', 'XML external entities (XXE)', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.04.name', 'XML外部实体 (XXE)', 'system', 'system')
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
('zh-CN', 'rule.standard.OWASP.2017.05.description', '程序不适当的实施了访问控制或限制措施，可能允许绕过可信边界', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.05.detail', '#### Abstract'||chr(10)||'The program has improperly enforced access control or restriction measures that may allow trusted boundary to be bypassed.'||chr(10)||'#### Explanation'||chr(10)||'Restrictions on what authenticated users are allowed to do are often not properly enforced. Attackers can exploit these flaws to access unauthorized functionality and/or data, such as access other users’ accounts, view sensitive files, modify other users’ data, change access rights, etc.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'struct stat link_info;'||chr(10)||''||chr(10)||'int func(void) '||chr(10)||'{'||chr(10)||'  // the following check alone is not sufficient. need to make sure privilege can be restored '||chr(10)||'  if (setuid(getuid() != 0)) {'||chr(10)||'    printf(" inside error set/get uid\n");'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // check for failuer to setuid call when caller is UID 0 is needed below'||chr(10)||'  if (setuid() != 0) {'||chr(10)||'    printf("inside error set uid\n");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'  return 1;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.05.detail', '#### 摘要'||chr(10)||'程序不适当的实施了访问控制或限制措施，可能允许绕过可信边界'||chr(10)||'#### 解释'||chr(10)||'未对通过身份验证的用户实施恰当的访问控制。攻击者可以利用这些缺陷访问未经授权的功能或者数据，例如访问其他用户的账户、查看敏感数据、修改其他用户的数据、更改访问权限等'||chr(10)||''||chr(10)||'#### 示例 - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||''||chr(10)||'#define BUF_SZ  1024'||chr(10)||''||chr(10)||'struct stat link_info;'||chr(10)||''||chr(10)||'int func(void) '||chr(10)||'{'||chr(10)||'  // the following check alone is not sufficient. need to make sure privilege can be restored '||chr(10)||'  if (setuid(getuid() != 0)) {'||chr(10)||'    printf(" inside error set/get uid\n");'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // check for failuer to setuid call when caller is UID 0 is needed below'||chr(10)||'  if (setuid() != 0) {'||chr(10)||'    printf("inside error set uid\n");'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'  return 1;'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.05.name', 'Broken access control', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.05.name', '失效的访问控制', 'system', 'system')
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
('zh-CN', 'rule.standard.OWASP.2017.06.description', '程序或程序组件（框架，库，操作系统等）的配置不安全', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.06.detail', '#### Abstract'||chr(10)||'The program or a component of the program (framework, libraries, OS etc) has insecure configuration.'||chr(10)||'#### Explanation'||chr(10)||'Security misconfiguration can happen along the entire execution stack of the program. It is important to ensure that all components are properly updated and have the proper security configuration. Failure to do so may give attackers unauthorized access to system data, files and accounts.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example in C'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <dlfcn.h>'||chr(10)||''||chr(10)||'int open_dso(char *dso_fname)'||chr(10)||'{'||chr(10)||'  void *handle;'||chr(10)||'  '||chr(10)||'  if (dso_fname == 0) {'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  // if dso_fname refers to a dso in shared directory'||chr(10)||'  // the .so opened may not be what is expected'||chr(10)||'  handle = dlopen(dso_fname, RTLD_LAZY);'||chr(10)||'  if (!handle) {'||chr(10)||'    // fail to load'||chr(10)||'    return 0;'||chr(10)||'    '||chr(10)||'  }'||chr(10)||'    '||chr(10)||'  // success '||chr(10)||'  // further program logic'||chr(10)||'  return 1;'||chr(10)||'}'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.06.detail', '#### 摘要'||chr(10)||'程序或程序组件（框架，库，操作系统等）的配置不安全'||chr(10)||'#### 解释'||chr(10)||'安全配置错误可能发生在程序的整个执行栈中。确保所有组件都得到正确更新并具有正确的安全配置非常重要，否则，攻击者可能会未经授权访问到系统数据、文件和账户。'||chr(10)||''||chr(10)||'#### 示例 - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <dlfcn.h>'||chr(10)||''||chr(10)||'int open_dso(char *dso_fname)'||chr(10)||'{'||chr(10)||'  void *handle;'||chr(10)||'  '||chr(10)||'  if (dso_fname == 0) {'||chr(10)||'    return 0;'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  // if dso_fname refers to a dso in shared directory'||chr(10)||'  // the .so opened may not be what is expected'||chr(10)||'  handle = dlopen(dso_fname, RTLD_LAZY);'||chr(10)||'  if (!handle) {'||chr(10)||'    // fail to load'||chr(10)||'    return 0;'||chr(10)||'    '||chr(10)||'  }'||chr(10)||'    '||chr(10)||'  // success '||chr(10)||'  // further program logic'||chr(10)||'  return 1;'||chr(10)||'}'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.06.name', 'Security misconfiguration', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.06.name', '安全配置错误', 'system', 'system')
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
('zh-CN', 'rule.standard.OWASP.2017.07.description', '程序不净化输入数据并允许它绕过验证，攻击者可以在客户浏览器中执行脚本', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.07.detail', '#### Abstract'||chr(10)||'The program is not sanitizing input data, allowing it to bypass validation. Attackers can then execute scripts in the client browser.'||chr(10)||'#### Explanation'||chr(10)||'Unvalidated and unescaped user input data can be used by attackers to hijack the outputting subsystem (such as the browser) to perform malicious activities including key logging, execution of arbitrary code or other malicious takeovers.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.07.detail', '#### 摘要'||chr(10)||'程序不净化输入数据并允许它绕过验证，攻击者可以在客户浏览器中执行脚本'||chr(10)||'#### 解释'||chr(10)||'未经验证和转义的用户输入数据可以被攻击者用来劫持输出子系统（例如浏览器），以执行恶意活动，包括记录密钥、执行任意代码或其他恶意接管。'||chr(10)||''||chr(10)||'#### 示例 - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.07.name', 'Cross site scripting (XSS)', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.07.name', '跨站脚本攻击', 'system', 'system')
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
('zh-CN', 'rule.standard.OWASP.2017.08.description', '程序具有反序列化方法，这些方法可绕过安全措施或者在反序列化之前降权', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.08.detail', '#### Abstract'||chr(10)||'The program has deserialization methods that may bypass security measures or have privileges downgraded before deserialization.'||chr(10)||'#### Explanation'||chr(10)||'Unrestricted deserialization from within a privileged context allows an attacker to create an input that is otherwise not allowed. This could result in various attacks such as replay, data tampering, injection and privileged escalation.'||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.08.detail', '#### 摘要'||chr(10)||'程序具有反序列化方法，这些方法可绕过安全措施或者在反序列化之前降权'||chr(10)||'#### 解释'||chr(10)||'特权上下文中不受限制的反序列化会允许攻击者创建不被允许的输入，这可能导致各种攻击，如重播、数据篡改、注入和特权升级。'||chr(10)||''||chr(10)||'#### 示例 - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.08.name', 'Insecure deserialization', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.08.name', '不安全的反序列化', 'system', 'system')
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
('zh-CN', 'rule.standard.OWASP.2017.09.description', '程序正在使用已知漏洞的组件', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.09.detail', '#### Abstract'||chr(10)||'The program is using components with know vulnerabilities.'||chr(10)||'#### Explanation'||chr(10)||'Components used in the program should include the latest fixes or patches for vulnerabilities. This also includes using the right level of encryption protection and security configuration needed for your program.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.09.detail', '#### 摘要'||chr(10)||'程序正在使用已知漏洞的组件'||chr(10)||'#### 解释'||chr(10)||'程序中使用的组件应该包括针对漏洞的最新修复或修补程序，这还包括使用程序所需的适当级别的加密保护和安全配置。'||chr(10)||''||chr(10)||'#### 示例 - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.09.name', 'Using components with known vulnerabilities', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.09.name', '使用含有已知漏洞的组件', 'system', 'system')
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
('zh-CN', 'rule.standard.OWASP.2017.10.description', '程序缺乏日志记录和监控，无法检测违规行为或者禁用运行时完整性验证', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.10.detail', '#### Abstract'||chr(10)||'The program is lacking in logging and monitoring that allows for detection of breaches, or has disabled verification of runtime integrity. '||chr(10)||'#### Explanation'||chr(10)||'It is important to enable all internal measures at runtime that verify, check and monitor the system. Failure to do so results in failed or delayed detection of a data breach and/ or the discovery of destroyed data. Businesses should expect to detect, escalate and alert for active attacks in near or real time. '||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.10.detail', '#### 摘要'||chr(10)||'程序缺乏日志记录和监控，无法检测违规行为或者禁用运行时完整性验证'||chr(10)||'#### 解释'||chr(10)||'在运行期启用验证、检查和监控系统的所有内部措施是非常重要的，否则将导致数据泄露检测失败或延迟，和/或发现被破坏的数据。企业应期望在接近或实时的情况下检测、升级和警报主动攻击。'||chr(10)||''||chr(10)||'#### 示例 - C'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````''', 'system', 'system'),
('en', 'rule.standard.OWASP.2017.10.name', 'Insufficient logging and monitoring', 'system', 'system'),
('zh-CN', 'rule.standard.OWASP.2017.10.name', '不充足的记录日志及监控', 'system', 'system')
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