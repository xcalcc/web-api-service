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
-- DCL00-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'DCL00-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/DCL00-J.+Prevent+class+initialization+cycles', '${rule.Xcalibyte.CERT.1.DCL00-J.name}', null, 'DCL00-J', 3, 3, 'UNLIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.DCL00-J.detail}', '${rule.Xcalibyte.CERT.1.DCL00-J.description}', '${rule.Xcalibyte.CERT.1.DCL00-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.DCL00-J.description', 'The program has class initialization that forms a cycle.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.DCL00-J.description', '程序具有类的初始化会构成循环。', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.DCL00-J.detail', '#### Abstract'||chr(10)||'The program has class initialization that forms a cycle.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Static field triggers the initialization of a class. When the static field depends on the initialization of another class will create a cycle.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.util.Calendar;'||chr(10)||'public class j_dcl00_0 {'||chr(10)||'  private int elapse;'||chr(10)||'  private static final j_dcl00_0 obj = new j_dcl00_0();  // self cycle (call constructor)'||chr(10)||'  private static final int curr_year = Calendar.getInstance().get(Calendar.YEAR);'||chr(10)||'  '||chr(10)||'  public j_dcl00_0() {'||chr(10)||'    if(200 > Calendar.getInstance().get(Calendar.YEAR))'||chr(10)||'      elapse = curr_year - 2000;  // curr_year not initialized yet, due to constructor is called before curr_year assignment in line 73. '||chr(10)||'                                  // ( Remediate suggestion: swap lines 72 and line 73 will ensure initialized)'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    System.out.println("It was " + obj.elapse + " years since 2000");'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.util.Calendar;'||chr(10)||'public class j_dcl00_0 {'||chr(10)||'  private int elapse;'||chr(10)||'  private static final int curr_year = Calendar.getInstance().get(Calendar.YEAR);'||chr(10)||'  private static final j_dcl00_0 obj = new j_dcl00_0();  // curr_year is initialized before used in constructor'||chr(10)||'  '||chr(10)||'  public j_dcl00_0() {'||chr(10)||'    if(200 > Calendar.getInstance().get(Calendar.YEAR))'||chr(10)||'      elapse = curr_year - 2000;  '||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    System.out.println("It was " + obj.elapse + " years since 2000");'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.DCL00-J.detail', '#### 概要'||chr(10)||'程序具有类的初始化会构成循环。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'静态字段会触发类的初始化。当静态字段依赖于另一个类的初始化时，将构成一个循环。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'import java.util.Calendar;'||chr(10)||'public class j_dcl00_0 {'||chr(10)||'  private int elapse;'||chr(10)||'  private static final j_dcl00_0 obj = new j_dcl00_0();  // self cycle'||chr(10)||'  private static final int curr_year = Calendar.getInstance().get(Calendar.YEAR);'||chr(10)||'  '||chr(10)||'  public j_dcl00_0() {'||chr(10)||'    if(200 > Calendar.getInstance().get(Calendar.YEAR))'||chr(10)||'      elapse = curr_year - 2000;  // curr_year not initialized yet'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    System.out.println("It was " + obj.elapse + " years since 2000");'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### 示例 - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.util.Calendar;'||chr(10)||'public class j_dcl00_0 {'||chr(10)||'  private int elapse;'||chr(10)||'  private static final int curr_year = Calendar.getInstance().get(Calendar.YEAR);'||chr(10)||'  private static final j_dcl00_0 obj = new j_dcl00_0();  // curr_year is initialized before used in constructor'||chr(10)||'  '||chr(10)||'  public j_dcl00_0() {'||chr(10)||'    if(200 > Calendar.getInstance().get(Calendar.YEAR))'||chr(10)||'      elapse = curr_year - 2000;  // curr_year not initialized yet, due to constructor is called before curr_year assignment in line 73. ( Remediate suggestion: swap lines 72 and line 73 will ensure initialized)'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    System.out.println("It was " + obj.elapse + " years since 2000");'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.DCL00-J.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line}, the class ${se.var} forms an initialization cycle with that of ${ss.filename} at line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.DCL00-J.msg_template', '在${se.filename}，第${se.line}行，${se.func}里的静态类初始化会和${ss.filename}, 第${ss.line}行的类构成一个循环.', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.DCL00-J.name', 'Program should not have class initialization cycle', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.DCL00-J.name', '程序不应具有初始化构成的循环', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='DCL00-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='DCL00-J'),
 'BASIC','PRIORITY','2'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='DCL00-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ENV01-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV01-J', null, 'ENV01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV01-J.+Place+all+security-sensitive+code+in+a+single+JAR+and+sign+and+seal+it', '${rule.Xcalibyte.CERT.1.ENV01-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.ENV01-J.detail}', '${rule.Xcalibyte.CERT.1.ENV01-J.description}', '${rule.Xcalibyte.CERT.1.ENV01-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.ENV01-J.description', 'The program is code signed to authenticate the origin of the code. Such code should contain some code that performs privileged operation.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV01-J.description', '该程序经过代码签名以验证代码的来源。此类代码应该包含一些执行有权限操作的代码。', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV01-J.detail', '### NYI'||chr(10)||'#### Abstract'||chr(10)||'The program is code signed to authenticate the origin of the code. Such code should contain some code that performs privileged operation.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Code signing is usually taken as trusted and safe to execute code. Many systems are configured to be "Always trust". The implied permission can easily be exploited.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV01-J.detail', '### NYI'||chr(10)||'#### Abstract'||chr(10)||'The program is code signed to authenticate the origin of the code. Such code should contain some code that performs privileged operation.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Code signing is usually taken as trusted and safe to execute code. Many systems are configured to be "Always trust". The implied permission can easily be exploited.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'// FIX ME'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV01-J.msg_template', '<< FIX ME>>', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV01-J.msg_template', '<< FIX ME>>', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV01-J.name', '<<NYI>> Security sensitive code should be signed and sealed in a single JAR', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV01-J.name', '<<NYI>> 应在单个JAR里签名并密封对安全性敏感的代码', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV01-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV01-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV01-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;
 
insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV01-J'),
 'STANDARD','OWASP','02'),
  ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV01-J'),
 'STANDARD','OWASP','03'),
  ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV01-J'),
 'STANDARD','OWASP','05')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ENV02-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV02-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV02-J.+Do+not+trust+the+values+of+environment+variables', '${rule.Xcalibyte.CERT.1.ENV02-J.name}', null, 'ENV02-J', 3, 2, 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.ENV02-J.detail}', '${rule.Xcalibyte.CERT.1.ENV02-J.description}', '${rule.Xcalibyte.CERT.1.ENV02-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV02-J'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV02-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV02-J'),
 'STANDARD','OWASP','02'),
  ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV02-J'),
 'STANDARD','OWASP','03'),
  ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV02-J'),
 'STANDARD','OWASP','05'),
  ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV02-J'),
 'STANDARD','OWASP','06')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ENV03-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV03-J', null, 'ENV03-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV03-J.+Do+not+grant+dangerous+combinations+of+permissions', '${rule.Xcalibyte.CERT.1.ENV03-J.name}', 1, 1, 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.ENV03-J.detail}', '${rule.Xcalibyte.CERT.1.ENV03-J.description}', '${rule.Xcalibyte.CERT.1.ENV03-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.ENV03-J.description', 'The program has called methods to grant permissions, or a combination of permissions to code which is dangerous.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV03-J.description', '程序调用了危险的方法来授予权限或者权限组合给代码。', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV03-J.detail', '#### Abstract'||chr(10)||'The program has called methods to grant permissions, or a combination of permissions to code which is dangerous.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Combination of permission granting code could be dangerous. For example, when RuntimePermission is applied to createClassLoader it can create a custom class loader and assign arbitrary permissions. ReflectPermission and suppressAccessChecks together will suppress all standard checks. '||chr(10)||''||chr(10)||'#### Example - Avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.net.MalformedURLException;'||chr(10)||'import java.security.cert.Certificate;'||chr(10)||'import java.security.CodeSource;'||chr(10)||'import java.security.PermissionCollection;'||chr(10)||'import java.security.Permissions;'||chr(10)||'import java.lang.reflect.*;'||chr(10)||''||chr(10)||'public class env03_0 extends URLClassLoader {'||chr(10)||''||chr(10)||'  public env03_0(URL[] urls) {'||chr(10)||'    super(urls);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  protected PermissionCollection getPermissions(CodeSource cs) {'||chr(10)||'    PermissionCollection pc = super.getPermissions(cs);   '||chr(10)||'    pc.add(new ReflectPermission("suppressAccessChecks"));   // add permission to create a class loader'||chr(10)||'                                                             // includes granting "suppressAccessChecks"'||chr(10)||'    // ...'||chr(10)||'    // other permissions'||chr(10)||'    return pc;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static boolean checkRtPermission(URL[] urls, String tag) throws MalformedURLException{'||chr(10)||'    env03_0 loader = new env03_0(urls);'||chr(10)||'    Certificate[] cert = null;'||chr(10)||'    CodeSource cs = new CodeSource(new URL("http://abc"), cert);'||chr(10)||'    PermissionCollection pc = loader.getPermissions(cs);'||chr(10)||'    ReflectPermission rp = new ReflectPermission(tag);'||chr(10)||'    if(pc.implies(rp)) {'||chr(10)||'      System.out.println("Able to get suppressAccessChecks");'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      System.out.println("Not Able to get suppressAccessChecks");'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) throws MalformedURLException{'||chr(10)||'    URL[] urls = new URL[0];'||chr(10)||'    checkRtPermission(urls, "suppressAccessChecks");'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - Prefer'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.net.MalformedURLException;'||chr(10)||'import java.security.cert.Certificate;'||chr(10)||'import java.security.CodeSource;'||chr(10)||'import java.security.PermissionCollection;'||chr(10)||'import java.security.Permissions;'||chr(10)||'import java.lang.reflect.*;'||chr(10)||''||chr(10)||'public class env03_0 extends URLClassLoader {'||chr(10)||''||chr(10)||'  public env03_0(URL[] urls) {'||chr(10)||'    super(urls);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  protected PermissionCollection getPermissions(CodeSource cs) {'||chr(10)||'    PermissionCollection pc = super.getPermissions(cs);'||chr(10)||'    // ...'||chr(10)||'    // other permissions'||chr(10)||'    return pc;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static boolean checkRtPermission(URL[] urls, String tag) throws MalformedURLException{'||chr(10)||'    env03_0 loader = new env03_0(urls);'||chr(10)||'    Certificate[] cert = null;'||chr(10)||'    CodeSource cs = new CodeSource(new URL("http://abc"), cert);'||chr(10)||'    PermissionCollection pc = loader.getPermissions(cs);'||chr(10)||'    ReflectPermission rp = new ReflectPermission(tag);'||chr(10)||'    if(pc.implies(rp)) {'||chr(10)||'      System.out.println("Able to get suppressAccessChecks");'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      System.out.println("Not Able to get suppressAccessChecks");'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) throws MalformedURLException{'||chr(10)||'    URL[] urls = new URL[0];'||chr(10)||'    checkRtPermission(urls, "suppressAccessChecks");'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV03-J.detail', '#### 概要'||chr(10)||'程序调用了危险的方法来授予权限或者权限组合给代码，'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'权限授予代码组合是危险的。例如，当应用RunitimePermission到createClassLoader时，它会创建自定义类加载器并分配任意权限。ReflextPermission和suppressAccessChecks一起将抑制所有标准检查。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.net.MalformedURLException;'||chr(10)||'import java.security.cert.Certificate;'||chr(10)||'import java.security.CodeSource;'||chr(10)||'import java.security.PermissionCollection;'||chr(10)||'import java.security.Permissions;'||chr(10)||'import java.lang.reflect.*;'||chr(10)||''||chr(10)||'public class env03_0 extends URLClassLoader {'||chr(10)||''||chr(10)||'  public env03_0(URL[] urls) {'||chr(10)||'    super(urls);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  protected PermissionCollection getPermissions(CodeSource cs) {'||chr(10)||'    PermissionCollection pc = super.getPermissions(cs);'||chr(10)||'    pc.add(new ReflectPermission("suppressAccessChecks"));   // add permission to create a class loader'||chr(10)||'                                                             // includes granting "suppressAccessChecks"'||chr(10)||'    // ...'||chr(10)||'    // other permissions'||chr(10)||'    return pc;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static boolean checkRtPermission(URL[] urls, String tag) throws MalformedURLException{'||chr(10)||'    env03_0 loader = new env03_0(urls);'||chr(10)||'    Certificate[] cert = null;'||chr(10)||'    CodeSource cs = new CodeSource(new URL("http://abc"), cert);'||chr(10)||'    PermissionCollection pc = loader.getPermissions(cs);'||chr(10)||'    ReflectPermission rp = new ReflectPermission(tag);'||chr(10)||'    if(pc.implies(rp)) {'||chr(10)||'      System.out.println("Able to get suppressAccessChecks");'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      System.out.println("Not Able to get suppressAccessChecks");'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) throws MalformedURLException{'||chr(10)||'    URL[] urls = new URL[0];'||chr(10)||'    checkRtPermission(urls, "suppressAccessChecks");'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.net.MalformedURLException;'||chr(10)||'import java.security.cert.Certificate;'||chr(10)||'import java.security.CodeSource;'||chr(10)||'import java.security.PermissionCollection;'||chr(10)||'import java.security.Permissions;'||chr(10)||'import java.lang.reflect.*;'||chr(10)||''||chr(10)||'public class env03_0 extends URLClassLoader {'||chr(10)||''||chr(10)||'  public env03_0(URL[] urls) {'||chr(10)||'    super(urls);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  protected PermissionCollection getPermissions(CodeSource cs) {'||chr(10)||'    PermissionCollection pc = super.getPermissions(cs);'||chr(10)||'    // ...'||chr(10)||'    // other permissions'||chr(10)||'    return pc;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static boolean checkRtPermission(URL[] urls, String tag) throws MalformedURLException{'||chr(10)||'    env03_0 loader = new env03_0(urls);'||chr(10)||'    Certificate[] cert = null;'||chr(10)||'    CodeSource cs = new CodeSource(new URL("http://abc"), cert);'||chr(10)||'    PermissionCollection pc = loader.getPermissions(cs);'||chr(10)||'    ReflectPermission rp = new ReflectPermission(tag);      // the rp created is only used for validity checking'||chr(10)||'    if(pc.implies(rp)) {'||chr(10)||'      System.out.println("Able to get suppressAccessChecks");'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      System.out.println("Not Able to get suppressAccessChecks");'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) throws MalformedURLException{'||chr(10)||'    URL[] urls = new URL[0];'||chr(10)||'    checkRtPermission(urls, "suppressAccessChecks");'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV03-J.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line} with method ${se.var}, we detected a dangerous combination of permissions grant to object in file ${ss.filename} at line ${ss.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV03-J.msg_template', '在${se.filename}，第${se.line}行，${se.func}里的方法 ${se.var}，在${ss.filename},  第${ss.line}行有危险的权限组合, 可以导致越权操作', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV03-J.name', 'Permissions should be granted explicitly and judiciously', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV03-J.name', '应明确并审慎地授予权限', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV03-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV03-J'),
 'BASIC','PRIORITY','27'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV03-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV03-J'),
 'STANDARD','OWASP','01'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV03-J'),
 'STANDARD','OWASP','02'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV03-J'),
 'STANDARD','OWASP','03'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV03-J'),
 'STANDARD','OWASP','05'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV03-J'),
 'STANDARD','OWASP','06'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV03-J'),
 'STANDARD','OWASP','09')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ENV04-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV04-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV04-J.+Do+not+disable+bytecode+verification', '${rule.Xcalibyte.CERT.1.ENV04-J.name}', null, 'ENV04-J', 1, 1, 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.ENV04-J.detail}', '${rule.Xcalibyte.CERT.1.ENV04-J.description}', '${rule.Xcalibyte.CERT.1.ENV04-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV04-J'),
 'BASIC','PRIORITY','27'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV04-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV04-J'),
 'STANDARD','OWASP','10'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV04-J'),
 'STANDARD','OWASP','05'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV04-J'),
 'STANDARD','OWASP','06')
 ON CONFLICT DO NOTHING;

-- ------------------------
-- ENV05-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV05-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV05-J.+Do+not+deploy+an+application+that+can+be+remotely+monitored', '${rule.Xcalibyte.CERT.1.ENV05-J.name}', null, 'ENV05-J', 1, 1, 'PROBABLE', 'LOW', '${rule.Xcalibyte.CERT.1.ENV05-J.detail}', '${rule.Xcalibyte.CERT.1.ENV05-J.description}', '${rule.Xcalibyte.CERT.1.ENV05-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV05-J'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV05-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV05-J'),
 'STANDARD','OWASP','10')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ENV06-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ENV06-J', null, 'ENV06-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ENV06-J.+Production+code+must+not+contain+debugging+entry+points', '${rule.Xcalibyte.CERT.1.ENV06-J.name}', 1, 1, 'PROBABLE', 'LOW', '${rule.Xcalibyte.CERT.1.ENV06-J.detail}', '${rule.Xcalibyte.CERT.1.ENV06-J.description}', '${rule.Xcalibyte.CERT.1.ENV06-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.ENV06-J.description', 'The program should strip all code used for debugging and not intended to be shipped or deployed with the application.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV06-J.description', '该程序应删除所有用于调试的且不计划随应用程序一起提供或部署的代码。', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV06-J.detail', '#### Abstract'||chr(10)||'The program should strip all code used for debugging and not intended to be shipped or deployed with the application. '||chr(10)||'#### Explanation'||chr(10)||'Although it is an acceptable practice to include "main" development, leaving that in production code would leave a backdoor entry point for an attacker. Methods that include "main" should be removed from production applications.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'public class my_code {'||chr(10)||'  // DEBUG is set to false, but "main" is left behind'||chr(10)||'  private static final boolean DEBUG = false;'||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    my_code f = new my_code();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'public class my_code {'||chr(10)||'  // DEBUG is set to false, but "main" is left behind'||chr(10)||'  private static final boolean DEBUG = false;'||chr(10)||'  // public static void main(String[] args) {'||chr(10)||'  //   my_code f = new my_code();'||chr(10)||'  // }'||chr(10)||'}'||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV06-J.detail', '#### 概要'||chr(10)||'该程序应删除所有用于调试的且不计划随应用程序一起提供或部署的代码。'||chr(10)||'#### 解释'||chr(10)||'虽然包括“main”开发是一个认可的做法，将它遗留在上线代码里会给攻击者留下后门入口点。应从上线应用程序里删除包含“main”的方法'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'public class my_code {'||chr(10)||'  // DEBUG is set to false, but "main" is left behind'||chr(10)||'  private static final boolean DEBUG = false;'||chr(10)||'public static void main(String[] args) {'||chr(10)||'    my_code f = new my_code();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### 示例 - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'public class my_code {'||chr(10)||'  // DEBUG is set to false, but "main" is left behind'||chr(10)||'  private static final boolean DEBUG = false;'||chr(10)||'  // remove the following statement if DEBUG is set to FALSE'||chr(10)||'  //   public static void main(String[] args) {'||chr(10)||'  //   my_code f = new my_code();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV06-J.msg_template', 'In ${se.filename}, line ${se.line} "main" is found, possibly left over for debugging purposes.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV06-J.msg_template', '在${se.filename}，${se.func} 里第${se.line}行，有疑似调试入口"main"', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ENV06-J.name', 'Debugging entry points should not remain in product code', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ENV06-J.name', '调试入口点不应遗留在上线代码或产品可执行文件里', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV06-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV06-J'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV06-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV06-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV06-J'),
 'STANDARD','OWASP','03'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV06-J'),
 'STANDARD','OWASP','05'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ENV06-J'),
 'STANDARD','OWASP','09')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ERR00-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ERR00-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ERR00-J.+Do+not+suppress+or+ignore+checked+exceptions', '${rule.Xcalibyte.CERT.1.ERR00-J.name}', null, 'ERR00-J', 3, 3, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.ERR00-J.detail}', '${rule.Xcalibyte.CERT.1.ERR00-J.description}', '${rule.Xcalibyte.CERT.1.ERR00-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.ERR00-J.description', 'Checked exceptions must be handled appropriately', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR00-J.description', '异常必须得到适当处理', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR00-J.detail', '#### Abstract'||chr(10)||'Checked exceptions must be handled appropriately'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Exceptions disrupt the expected control flow of the application. the catch block must either recover from the exceptional condition or throw an exception that is appropriate to the context of the catch block.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class j_err00_0 {'||chr(10)||''||chr(10)||'  public void foo0(String fileName) {'||chr(10)||'    try {'||chr(10)||'      BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));'||chr(10)||'      int b;'||chr(10)||'      while ((b  = reader.read()) != -1) {'||chr(10)||'        System.out.println("byte: " + b);'||chr(10)||'      }'||chr(10)||'    } catch (IOException ioe) {'||chr(10)||'      ioe.printStackTrace();  // print will not recover nor throw exception '||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void foo1(String fileName) {'||chr(10)||'    try {'||chr(10)||'      BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));'||chr(10)||'      int b;'||chr(10)||'      while ((b  = reader.read()) != -1) {'||chr(10)||'        System.out.println("byte: " + b);'||chr(10)||'      }'||chr(10)||'    } catch (IOException ioe) {'||chr(10)||'      // do nothing  - no recovery of any kind'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class j_err00_0 {'||chr(10)||''||chr(10)||'  public void foo0(String fileName) {'||chr(10)||'    try {'||chr(10)||'      BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));'||chr(10)||'      int b;'||chr(10)||'      while ((b  = reader.read()) != -1) {'||chr(10)||'        System.out.println("byte: " + b);'||chr(10)||'      }'||chr(10)||'    } catch (IOException ioe) {'||chr(10)||'      throw new IllegalIOException(ioe.toString());  // we assume there is such exception handler here'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void foo1(String fileName) {'||chr(10)||'      // indentation preserved to better show the difference with "avoid" example'||chr(10)||'      BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));'||chr(10)||'      int b;'||chr(10)||'      while ((b  = reader.read()) != -1) {'||chr(10)||'        System.out.println("byte: " + b);'||chr(10)||'      }'||chr(10)||''||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR00-J.detail', '#### 概要'||chr(10)||'异常必须得到适当处理'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||''||chr(10)||'异常会破坏应用程序的预期控制流。 catch块必须从异常条件中恢复，或者抛出适合于catch块上下文的异常。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||''||chr(10)||'````'||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class j_err00_0 {'||chr(10)||''||chr(10)||'  public void foo0(String fileName) {'||chr(10)||'    try {'||chr(10)||'      BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));'||chr(10)||'      int b;'||chr(10)||'      while ((b  = reader.read()) != -1) {'||chr(10)||'        System.out.println("byte: " + b);'||chr(10)||'      }'||chr(10)||'    } catch (IOException ioe) {'||chr(10)||'      ioe.printStackTrace();'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void foo1(String fileName) {'||chr(10)||'    try {'||chr(10)||'      BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));'||chr(10)||'      int b;'||chr(10)||'      while ((b  = reader.read()) != -1) {'||chr(10)||'        System.out.println("byte: " + b);'||chr(10)||'      }'||chr(10)||'    } catch (IOException ioe) {'||chr(10)||'      // do nothing  - no recovery of any kind'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### 示例 - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class j_err00_0 {'||chr(10)||''||chr(10)||'  public void foo0(String fileName) {'||chr(10)||'    try {'||chr(10)||'      BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));'||chr(10)||'      int b;'||chr(10)||'      while ((b  = reader.read()) != -1) {'||chr(10)||'        System.out.println("byte: " + b);'||chr(10)||'      }'||chr(10)||'    } catch (IOException ioe) {'||chr(10)||'      throw new IllegalIOException(ioe.toString());  // we assume there is such exception handler here'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void foo1(String fileName) {'||chr(10)||''||chr(10)||'      // indentation preserved to better show the difference with "avoid" example'||chr(10)||'      BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));'||chr(10)||'      int b;'||chr(10)||'      while ((b  = reader.read()) != -1) {'||chr(10)||'        System.out.println("byte: " + b);'||chr(10)||'      }'||chr(10)||''||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR00-J.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line}, the catch block does not properly handle exception throw in ${ss.filename}, at line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR00-J.msg_template', '在${se.filename}，第${se.line}行，${se.func}里的catch块未有正确处理在${ss.filename},  第${ss.line}行异常抛出的异常.', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR00-J.name', 'Exceptions must be handled appropriately', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR00-J.name', '异常必须得到适当处理', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR00-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR00-J'),
 'BASIC','PRIORITY','4'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR00-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR00-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR00-J'),
 'STANDARD','OWASP','05')
ON CONFLICT DO NOTHING;

-- ------------------------
-- ERR08-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'ERR08-J', null, 'ERR08-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/ERR08-J.+Do+not+catch+NullPointerException+or+any+of+its+ancestors', '${rule.Xcalibyte.CERT.1.ERR08-J.name}', 2, 1, 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.ERR08-J.detail}', '${rule.Xcalibyte.CERT.1.ERR08-J.description}', '${rule.Xcalibyte.CERT.1.ERR08-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.ERR08-J.description', 'The program is catching an NullPointerException.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR08-J.description', '该程序正捕捉NullPointerException', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR08-J.detail', '#### Abstract'||chr(10)||'The program is catching an NullPointerException. '||chr(10)||'#### Explanation'||chr(10)||'A NullPointerException thrown at runtime indicates an underlying defect that must be fixed in the application code. Catching this exception (or others like RuntimeException, Exception or Throwable) adds more runtime performance overhead. Furthermore, when that happens, the program is likely to be in an unknown state and continued execution will cause unexpected results.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'public class err08_0 {'||chr(10)||'  boolean isName(String s) {'||chr(10)||'    try {'||chr(10)||'      String names = s.concat("abc");'||chr(10)||'      if (names.length() != 5) {'||chr(10)||'        return false;'||chr(10)||'      }'||chr(10)||'      return true;'||chr(10)||'    } catch (NullPointerException e) {'||chr(10)||'      // catch Null pointer exception '||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'    '||chr(10)||'  }'||chr(10)||''||chr(10)||'  boolean foo1() {'||chr(10)||'    try {'||chr(10)||'      throw new RuntimeException("xyz");'||chr(10)||'    } catch (RuntimeException e) {'||chr(10)||'      // this catch RuntimeException is ancestor of the NullPointerException'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'public class err08_0 {'||chr(10)||'  boolean isName(String s) {'||chr(10)||'    try {'||chr(10)||'      String names = s.concat("abc");'||chr(10)||'      if (names.length() != 5) {'||chr(10)||'        return false;'||chr(10)||'      }'||chr(10)||'      return true;'||chr(10)||'    } '||chr(10)||'  }'||chr(10)||''||chr(10)||'  boolean foo1() {'||chr(10)||'    try {'||chr(10)||'      throw new RuntimeException("xyz");'||chr(10)||'    } catch (RuntimeException e) {'||chr(10)||'      // this catch RuntimeException is ancestor of the NullPointerException'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR08-J.detail', '#### 概要'||chr(10)||'该程序正捕捉NullPointerException'||chr(10)||'#### 解释'||chr(10)||'运行时抛出的NullPointerException表明有底层缺陷，必须在应用程序代码里修复它。捕捉此异常（或者其它像RuntimeException、Exception或Throwable这样的异常）会增加更多的运行时性能开销。此外，当发生这种情况时，程序很可能处于未知状态，而继续执行将导致意外结果'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'public class err08_0 {'||chr(10)||'  boolean isName(String s) {'||chr(10)||'    try {'||chr(10)||'      String names = s.concat("abc");'||chr(10)||'      if (names.length() != 5) {'||chr(10)||'        return false;'||chr(10)||'      }'||chr(10)||'      return true;'||chr(10)||'    } catch (NullPointerException e) {'||chr(10)||'      // catch Null pointer exception '||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  boolean foo1() {'||chr(10)||'    try {'||chr(10)||'      throw new RuntimeException("xyz");'||chr(10)||'    } catch (RuntimeException e) {'||chr(10)||'      // this catch RuntimeException is ancestor of the NullPointerException'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'public class err08_0 {'||chr(10)||'  boolean isName(String s) {'||chr(10)||'    try {'||chr(10)||'      String names = s.concat("abc");'||chr(10)||'      if (names.length() != 5) {'||chr(10)||'        return false;'||chr(10)||'      }'||chr(10)||'      return true;'||chr(10)||'    } '||chr(10)||'  }'||chr(10)||''||chr(10)||'  boolean foo1() {'||chr(10)||'    try {'||chr(10)||'      throw new RuntimeException("xyz");'||chr(10)||'    } catch (RuntimeException e) {'||chr(10)||'      // this catch RuntimeException is ancestor of the NullPointerException'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR08-J.msg_template', 'In ${se.filename}, line ${se.line}, a NullPointerException (or possible) is caught at the try block in ${se.func}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR08-J.msg_template', '在${se.filename}，第${se.line}行，有NullPointerException 的异常在try块捕获到到', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.ERR08-J.name', 'NullPointerException or any of its ancestors should not be caught', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.ERR08-J.name', '不应捕捉NullPointerException或其任一父类', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR08-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR08-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR08-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR08-J'),
 'STANDARD','OWASP','06'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR08-J'),
 'STANDARD','OWASP','03'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='ERR08-J'),
 'STANDARD','OWASP','10')
ON CONFLICT DO NOTHING;

-- ------------------------
-- EXP02-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'EXP02-J', null, 'EXP02-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/EXP02-J.+Do+not+use+the+Object.equals%28%29+method+to+compare+two+arrays', '${rule.Xcalibyte.CERT.1.EXP02-J.name}', 3, 2, 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.EXP02-J.detail}', '${rule.Xcalibyte.CERT.1.EXP02-J.description}', '${rule.Xcalibyte.CERT.1.EXP02-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.EXP02-J.description', 'The program is comparing two arrays using Object.equals() method', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.EXP02-J.description', '该程序正使用Object.equals()方法比较两个数组', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.EXP02-J.detail', '#### Abstract'||chr(10)||'The program is comparing two arrays using Object.equals() method'||chr(10)||'#### Explanation'||chr(10)||'Two arrays are equal if they contain equivalent elements and in the same order. Using Object.equals() on arrays compares only array references, not their contents.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'// this example is modified from CERT-J site'||chr(10)||'import java.util.Arrays;'||chr(10)||''||chr(10)||'public class exp02_example {'||chr(10)||''||chr(10)||'  public static void println(int[] ar1, int[] ar2) {'||chr(10)||'    System.out.println(ar1.equals(ar2)); // prints false'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    int[] arr1 = new int[20]; // Initialized to 0'||chr(10)||'    int[] arr2 = new int[20]; // Initialized to 0'||chr(10)||'    println(arr1, arr2); '||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'// this example is modified from CERT-J site'||chr(10)||'import java.util.Arrays;'||chr(10)||''||chr(10)||'public class exp02_example {'||chr(10)||''||chr(10)||'  public static void println(int[] ar1, int[] ar2) {'||chr(10)||'    System.out.println(Arrays.equals(arr1, arr2));   // prints true'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    int[] arr1 = new int[20]; // Initialized to 0'||chr(10)||'    int[] arr2 = new int[20]; // Initialized to 0'||chr(10)||'    println(arr1, arr2); '||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.EXP02-J.detail', '#### 概要'||chr(10)||'该程序正使用Object.equals()方法比较两个数组'||chr(10)||'#### 解释'||chr(10)||'如果两个数组包含相同的元素并且它们有相同的顺序，则这两个数组是等同的。在数组上使用Object.equals()只比较数组引用，而不是其内容。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||''||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'// this example is modified from CERT-J site'||chr(10)||'import java.util.Arrays;'||chr(10)||''||chr(10)||'public class exp02_example {'||chr(10)||''||chr(10)||'  public static void println(int[] ar1, int[] ar2) {'||chr(10)||'    System.out.println(ar1.equals(ar2)); // prints false'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    int[] arr1 = new int[20]; // Initialized to 0'||chr(10)||'    int[] arr2 = new int[20]; // Initialized to 0'||chr(10)||'    println(arr1, arr2); '||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'// this example is modified from CERT-J site'||chr(10)||'import java.util.Arrays;'||chr(10)||''||chr(10)||'public class exp02_example {'||chr(10)||''||chr(10)||'  public static void println(int[] ar1, int[] ar2) {'||chr(10)||'    System.out.println(Arrays.equals(arr1, arr2));   // prints true'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    int[] arr1 = new int[20]; // Initialized to 0'||chr(10)||'    int[] arr2 = new int[20]; // Initialized to 0'||chr(10)||'    println(arr1, arr2); '||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.EXP02-J.msg_template', 'In ${se.filename}, ${se.func} at line ${se.line}, Object.equals() has been used to compare ${se.var}. This variable is declared at line ${ss.line}, ${ss.filename}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.EXP02-J.msg_template', '在${se.filename}，${se.func} 里第${se.line}行, Object.equals() 被用来比较${se.var}. 這数组是在文件${ss.filename}, 第${ss.line}行定义的', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.EXP02-J.name', 'The Object.equals() should not be used to compare two arrays', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.EXP02-J.name', '不应使用Object.equals()比较两个数组', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='EXP02-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='EXP02-J'),
 'BASIC','PRIORITY','9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='EXP02-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- FIO02-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO02-J', null, 'FIO02-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/FIO02-J.+Detect+and+handle+file-related+errors', '${rule.Xcalibyte.CERT.1.FIO02-J.name}', 2, 2, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO02-J.detail}', '${rule.Xcalibyte.CERT.1.FIO02-J.description}', '${rule.Xcalibyte.CERT.1.FIO02-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO02-J.description', 'The program is attempting to manipulate a file but failed to check validity of the method''s return values.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO02-J.description', '该程序正试图操控一个文件，但未能检查该方法返回值的有效性。', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO02-J.detail', '#### Abstract'||chr(10)||'The program is attempting to manipulate a file but failed to check validity of the method''s return values.'||chr(10)||'#### Explanation'||chr(10)||'File I/O operations in Java must check the return values of methods that perform the file I/O. For example, if not checked, File.delete() will silently fail.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````'||chr(10)||'import java.io.File;'||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class j_fio02_0 {'||chr(10)||'  void foo(boolean b) {'||chr(10)||'    try {'||chr(10)||'      File f = new File("file");'||chr(10)||'      if (b) {'||chr(10)||'        if (f.delete()) {'||chr(10)||'          System.out.println("Deletion error");'||chr(10)||'        }'||chr(10)||'      }'||chr(10)||'      else {'||chr(10)||'        f.delete(); // failed to check return value of delete()'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'    catch(Exception e) {'||chr(10)||'        System.out.println("Exception");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````'||chr(10)||'import java.io.File;'||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class j_fio02_0 {'||chr(10)||'  void foo(boolean b) {'||chr(10)||'    try {'||chr(10)||'      File f = new File("file");'||chr(10)||'      if (b) {'||chr(10)||'        if (f.delete()) {'||chr(10)||'          System.out.println("Deletion error");'||chr(10)||'        }'||chr(10)||'      }'||chr(10)||'      else {'||chr(10)||'        if (f.delete()) {'||chr(10)||'          System.out.println("Deletion error");'||chr(10)||'        }'||chr(10)||'        '||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'    catch(Exception e) {'||chr(10)||'        System.out.println("Exception");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO02-J.detail', '#### 概要'||chr(10)||'该程序正试图操控一个文件，但未能检查该方法返回值的有效性。'||chr(10)||'#### 解释'||chr(10)||'Java里的文件I/O操作必须检查执行文件I/O的方法的返回值。例如，如果不经过检查，File.delete()会悄无声息地失败。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.File;'||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class j_fio02_0 {'||chr(10)||'  void foo(boolean b) {'||chr(10)||'    try {'||chr(10)||'      File f = new File("file");'||chr(10)||'      if (b) {'||chr(10)||'        if (f.delete()) {'||chr(10)||'          System.out.println("Deletion error");'||chr(10)||'        }'||chr(10)||'      }'||chr(10)||'      else {'||chr(10)||'        f.delete(); // failed to check return value of delete()'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'    catch(Exception e) {'||chr(10)||'        System.out.println("Exception");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||'````'||chr(10)||'import java.io.File;'||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class j_fio02_0 {'||chr(10)||'  void foo(boolean b) {'||chr(10)||'    try {'||chr(10)||'      File f = new File("file");'||chr(10)||'      if (b) {'||chr(10)||'        if (f.delete()) {'||chr(10)||'          System.out.println("Deletion error");'||chr(10)||'        }'||chr(10)||'      }'||chr(10)||'      else {'||chr(10)||'        if (f.delete()) {'||chr(10)||'          System.out.println("Deletion error");'||chr(10)||'        }'||chr(10)||'        '||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'    catch(Exception e) {'||chr(10)||'        System.out.println("Exception");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO02-J.msg_template', 'In ${ss.filename}, ${ss.func} at line ${ss.line}, new File object is requested. Subsequent call to ${se.var} this file in ${se.filename} at line ${se.line} failed to check that delete is successful', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO02-J.msg_template', '在${ss.filename}，${ss.func} 里第${ss.line}行, 请求打开文件. 其后在${se.filename}, 第${ss.line}行请求 ${se.var}該文件並未有检查正确返回', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO02-J.name', 'File related errors should be checked and handled', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO02-J.name', '应该检查并处理和文件有关的错误', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO02-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO02-J'),
 'BASIC','PRIORITY','8'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO02-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO02-J'),
 'STANDARD','OWASP','03')
ON CONFLICT DO NOTHING;

-- ------------------------
-- FIO05-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO05-J', null, 'FIO05-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/FIO05-J.+Do+not+expose+buffers+created+using+the+wrap%28%29+or+duplicate%28%29+methods+to+untrusted+code', '${rule.Xcalibyte.CERT.1.FIO05-J.name}', 2, 1, 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.FIO05-J.detail}', '${rule.Xcalibyte.CERT.1.FIO05-J.description}', '${rule.Xcalibyte.CERT.1.FIO05-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO05-J.description', 'The program is using wrap() or duplicate() methods in Buffer class in java.nio package. These buffers are exposed to untrusted code in the program.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO05-J.description', '该程序正使用java.nio包里的Buffer类里的wrap()或duplicate()方法。这些缓存暴露给了程序里不受信任的代码', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO05-J.detail', '#### Abstract'||chr(10)||'The program is using wrap() or duplicate() methods in Buffer class in java.nio package. These buffers are exposed to untrusted code in the program.'||chr(10)||'#### Explanation'||chr(10)||'The methods will create a new Buffer object, backed by the given input array. Hence, the new Buffer object can be maliciously modified. It is important to use a read only Buffer or make sure the original buffer will not be modified. '||chr(10)||''||chr(10)||'#### Example 1 - avoid (modified from CERT-J example)'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.nio.CharBuffer;'||chr(10)||''||chr(10)||'public class j_fio05_0 {'||chr(10)||'  private char[] dataArray;'||chr(10)||'  private CharBuffer cb;'||chr(10)||''||chr(10)||'  public j_fio05_0() {'||chr(10)||'    dataArray = new char[10];'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy() {'||chr(10)||'    return CharBuffer.wrap(dataArray);  // dataArray (private) is exposed through this wrap'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example 1 - prefer (modified from CERT-J example)'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.nio.CharBuffer;'||chr(10)||''||chr(10)||'public class j_fio05_0 {'||chr(10)||'  private char[] dataArray;'||chr(10)||'  private CharBuffer cb;'||chr(10)||''||chr(10)||'  public j_fio05_0() {'||chr(10)||'    dataArray = new char[10];'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy() {'||chr(10)||'    return CharBuffer.wrap(dataArray).asReadOnlyBuffer();  // attemp to modify will result in exception'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example 2 - avoid'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.nio.CharBuffer;'||chr(10)||''||chr(10)||'public class j_fio05_0 {'||chr(10)||'  private char[] dataArray;'||chr(10)||'  private CharBuffer cb;'||chr(10)||''||chr(10)||'  public j_fio05_0() {'||chr(10)||'    dataArray = new char[10];'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy() {'||chr(10)||'    return CharBuffer.wrap(dataArray);  // dataArray (private) is exposed through this wrap'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void storeBuffer() {'||chr(10)||'    cb = CharBuffer.wrap(dataArray);    // dataArray (private) is exposed, see comment in return statement below'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy2() {'||chr(10)||'    storeBuffer();'||chr(10)||'    return cb;                          // dataArray is exposed through cb which is assigned in storeBuffer()'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example 2 - prefer'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.nio.CharBuffer;'||chr(10)||''||chr(10)||'public class j_fio05_0 {'||chr(10)||'  private char[] dataArray;'||chr(10)||'  private CharBuffer cb;'||chr(10)||''||chr(10)||'  public j_fio05_0() {'||chr(10)||'    dataArray = new char[10];'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy() {'||chr(10)||'    return CharBuffer.wrap(dataArray).asReadOnlyBuffer();;  // attempt to modify will result in exception'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void storeBuffer() {'||chr(10)||'    cb = CharBuffer.wrap(dataArray).asReadOnlyBuffer();;    // attempt to modify will result in exception'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy2() {'||chr(10)||'    storeBuffer();'||chr(10)||'    return cb;                          '||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO05-J.detail', '#### 概要'||chr(10)||'该程序正使用java.nio包里的Buffer类里的wrap()或duplicate()方法。这些缓存暴露给了程序里不受信任的代码'||chr(10)||'#### 解释'||chr(10)||'方法会创建从给定输入数组生成的新Buffer对象。因此，新Buffer对象可能被恶意修改。使用只读Buffer或确保原始缓存不被修改是很重要的。'||chr(10)||''||chr(10)||'#### 示例 - 1 避免 (基於 CERT-J )'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.nio.CharBuffer;'||chr(10)||''||chr(10)||'public class j_fio05_0 {'||chr(10)||'  private char[] dataArray;'||chr(10)||'  private CharBuffer cb;'||chr(10)||''||chr(10)||'  public j_fio05_0() {'||chr(10)||'    dataArray = new char[10];'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy() {'||chr(10)||'    return CharBuffer.wrap(dataArray);  // dataArray (private) is exposed through this wrap'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 1 建议 (基於 CERT-J )'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.nio.CharBuffer;'||chr(10)||''||chr(10)||'public class j_fio05_0 {'||chr(10)||'  private char[] dataArray;'||chr(10)||'  private CharBuffer cb;'||chr(10)||''||chr(10)||'  public j_fio05_0() {'||chr(10)||'    dataArray = new char[10];'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy() {'||chr(10)||'    return CharBuffer.wrap(dataArray).asReadOnlyBuffer();  // attemp to modify will result in exception'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 2 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.nio.CharBuffer;'||chr(10)||''||chr(10)||'public class j_fio05_0 {'||chr(10)||'  private char[] dataArray;'||chr(10)||'  private CharBuffer cb;'||chr(10)||''||chr(10)||'  public j_fio05_0() {'||chr(10)||'    dataArray = new char[10];'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy() {'||chr(10)||'    return CharBuffer.wrap(dataArray);  // dataArray (private) is exposed through this wrap'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void storeBuffer() {'||chr(10)||'    cb = CharBuffer.wrap(dataArray);    // dataArray (private) is exposed, see comment in return statement below'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy2() {'||chr(10)||'    storeBuffer();'||chr(10)||'    return cb;                          // dataArray is exposed through cb which is assigned in storeBuffer()'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### 示例 2 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.nio.CharBuffer;'||chr(10)||''||chr(10)||'public class j_fio05_0 {'||chr(10)||'  private char[] dataArray;'||chr(10)||'  private CharBuffer cb;'||chr(10)||''||chr(10)||'  public j_fio05_0() {'||chr(10)||'    dataArray = new char[10];'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy() {'||chr(10)||'    return CharBuffer.wrap(dataArray).asReadOnlyBuffer();;  // attempt to modify will result in exception'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void storeBuffer() {'||chr(10)||'    cb = CharBuffer.wrap(dataArray).asReadOnlyBuffer();;    // attempt to modify will result in exception'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public CharBuffer getBufferCopy2() {'||chr(10)||'    storeBuffer();'||chr(10)||'    return cb;                          '||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO05-J.msg_template', 'In file ${se.filename}, line ${se.line}, the backing array of the buffer class ${se.var} can be exposed to untrusted code through function ${se.func}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO05-J.msg_template', '在${se.filename}，${se.func} 里第${se.line}行, Buffer ${se.var}类缓冲的后备数组有可能通过函数 ${se.func} 暴露给不可信的代码', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO05-J.name', 'Buffers created with wrap() or duplicate() should not be exposed to untrusted code', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO05-J.name', '不应把用wrap()或duplicate()创建的缓存暴露给不受信任代码', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO05-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO05-J'),
 'BASIC','PRIORITY','18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO05-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO05-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO05-J'),
 'STANDARD','OWASP','08')
ON CONFLICT DO NOTHING;

-- ------------------------
-- FIO08-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO08-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/FIO08-J.+Distinguish+between+characters+or+bytes+read+from+a+stream+and+-1', '${rule.Xcalibyte.CERT.1.FIO08-J.name}', null, 'FIO08-J', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO08-J.detail}', '${rule.Xcalibyte.CERT.1.FIO08-J.description}', '${rule.Xcalibyte.CERT.1.FIO08-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO08-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO08-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO08-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO08-J'),
 'STANDARD','OWASP','06'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO08-J'),
 'STANDARD','OWASP','08')
ON CONFLICT DO NOTHING;

-- ------------------------
-- FIO14-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO14-J', null, 'FIO14-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/FIO14-J.+Perform+proper+cleanup+at+program+termination', '${rule.Xcalibyte.CERT.1.FIO14-J.name}', 2, 1, 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO14-J.detail}', '${rule.Xcalibyte.CERT.1.FIO14-J.description}', '${rule.Xcalibyte.CERT.1.FIO14-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO14-J.description', 'The program did not perform proper cleanup when the program terminates.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO14-J.description', '在程序终止时，未有進行适当的清理', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO14-J.detail', '#### Abstract'||chr(10)||'The program did not perform proper cleanup when the program terminates.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'When irrecoverable errors are detected, it is common to quickly shut down the system and allow the operator to start over in a determinate state. This should include cleanup that involves external resources. Failure to do so may leave important and sensitive data behind in the system.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class fio14_example {'||chr(10)||'  public static void reg_hook(PrintStream out) {'||chr(10)||'    // shut down hook will be called when Runtime.exit(),'||chr(10)||'    // make sure in call hierarchy,'||chr(10)||'    // there is an path from Runtime.exit() to Runtime.addShutdownHook'||chr(10)||'    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {'||chr(10)||'      public void run() {'||chr(10)||'        System.out.println("exit without closing file, need out.close()");'||chr(10)||'        out.close(); // report double close'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'    ));'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void close2()  throws FileNotFoundException {'||chr(10)||'    final PrintStream out ='||chr(10)||'      new PrintStream(new BufferedOutputStream('||chr(10)||'        new FileOutputStream("foo.txt")));'||chr(10)||'    reg_hook(out);'||chr(10)||'    out.println("register hook");'||chr(10)||'    Runtime.getRuntime().exit(1);'||chr(10)||'  }'||chr(10)||'  public static void main(String[] args)  throws FileNotFoundException{'||chr(10)||'    close2();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class fio14_example {'||chr(10)||'  public static void reg_hook(PrintStream out) {'||chr(10)||'    // shut down hook will be called when Runtime.exit(),'||chr(10)||'    // make sure in call hierarchy,'||chr(10)||'    // there is an edge from Runtime.exit() to Runtime.addShutdownHook'||chr(10)||'    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {'||chr(10)||'      public void run() {'||chr(10)||'        System.out.println("exit without closing file, need out.close()");'||chr(10)||'        out.close(); // report double close'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'    ));'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void close2()  throws FileNotFoundException {'||chr(10)||'    final PrintStream out ='||chr(10)||'      new PrintStream(new BufferedOutputStream('||chr(10)||'        new FileOutputStream("foo.txt")));'||chr(10)||'    reg_hook(out);'||chr(10)||'    out.println("hello");'||chr(10)||'    Runtime.getRuntime().exit(1);'||chr(10)||'  }'||chr(10)||'  public static void main(String[] args)  throws FileNotFoundException{'||chr(10)||'    close2();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO14-J.detail', '#### 概要'||chr(10)||'在程序终止时，未有進行适当的清理.'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||''||chr(10)||'当检测到不可恢复的错误时，通常会快速关闭系统并允许操作员在确定的状态下重新启动。这应该包括涉及外部资源的清理。否则，可能会在系统中留下重要或敏感数据'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class fio14_example {'||chr(10)||'  public static void reg_hook(PrintStream out) {'||chr(10)||'    // shut down hook will be called when Runtime.exit(),'||chr(10)||'    // make sure in call hierarchy,'||chr(10)||'    // there is an path from Runtime.exit() to Runtime.addShutdownHook'||chr(10)||'    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {'||chr(10)||'      public void run() {'||chr(10)||'        System.out.println("exit without closing file, need out.close()");'||chr(10)||'        out.close(); // report double close'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'    ));'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void close2()  throws FileNotFoundException {'||chr(10)||'    final PrintStream out ='||chr(10)||'      new PrintStream(new BufferedOutputStream('||chr(10)||'        new FileOutputStream("foo.txt")));'||chr(10)||'    reg_hook(out);'||chr(10)||'    out.println("register hook");'||chr(10)||'    Runtime.getRuntime().exit(1);'||chr(10)||'  }'||chr(10)||'  public static void main(String[] args)  throws FileNotFoundException{'||chr(10)||'    close2();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class fio14_example {'||chr(10)||'  public static void reg_hook(PrintStream out) {'||chr(10)||'    // shut down hook will be called when Runtime.exit(),'||chr(10)||'    // make sure in call hierarchy,'||chr(10)||'    // there is an edge from Runtime.exit() to Runtime.addShutdownHook'||chr(10)||'    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {'||chr(10)||'      public void run() {'||chr(10)||'        System.out.println("exit without closing file, need out.close()");'||chr(10)||'        out.close(); // report double close'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'    ));'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void close2()  throws FileNotFoundException {'||chr(10)||'    final PrintStream out ='||chr(10)||'      new PrintStream(new BufferedOutputStream('||chr(10)||'        new FileOutputStream("foo.txt")));'||chr(10)||'    reg_hook(out);'||chr(10)||'    out.println("hello");'||chr(10)||'    Runtime.getRuntime().exit(1);'||chr(10)||'  }'||chr(10)||'  public static void main(String[] args)  throws FileNotFoundException{'||chr(10)||'    close2();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO14-J.msg_template', 'In ${ss.filename}  line ${ss.line}, resource associated with  ${se.var} will not be properly cleanup when program is terminated at line ${se.line} in function ${se.func}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO14-J.msg_template', '在${se.filename}, 第${se.line}行, 程序進入终止状态. 但是在${ss.filename}, 第${ss.line}行, 函数${se.func} 中${se.var} 及其关联的资源将无法正确清理', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO14-J.name', 'At program termination, proper cleanup should be performed through Runtime.exit()', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO14-J.name', '在程序终止时，应通过Runtime.exit()执行适当的清理', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO14-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO14-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO14-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO14-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO14-J'),
 'STANDARD','OWASP','10')
ON CONFLICT DO NOTHING;

-- ------------------------
-- FIO16-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO16-J', null, 'FIO16-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS17-J.+Prevent+XML+External+Entity+Attacks', '${rule.Xcalibyte.CERT.1.FIO16-J.name}', 2, 3, 'UNLIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO16-J.detail}', '${rule.Xcalibyte.CERT.1.FIO16-J.description}', '${rule.Xcalibyte.CERT.1.FIO16-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO16-J.description', 'The program is validating pathnames without first canonicalizing them', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO16-J.description', '该程序在没有首先规范化路径名的情况下对其进行验证', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO16-J.detail', '#### Abstract'||chr(10)||'The program is validating pathnames without first canonicalizing them.'||chr(10)||'#### Explanation'||chr(10)||'Pathnames may contain special characters that make validation difficult. Also, a pathname may really be symbolic links, shadows etc. which attackers could use to bypass security check(s). It is important to fully resolve/canonicalize a pathname before validation. '||chr(10)||''||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class fio16_example'||chr(10)||'{'||chr(10)||''||chr(10)||'  public void bad_1(HttpServletRequest request, HttpServletResponse response) throws IOException'||chr(10)||'  {'||chr(10)||'    String fname = request.getParameter("FileName");'||chr(10)||'    String info = request.getParameter("Info");'||chr(10)||'    File fp = new File(fname);'||chr(10)||'    '||chr(10)||'    if(fp.exists() && is_safe(fname)) { // fp validated but before calling getCanonicalPath,'||chr(10)||'      //  hacker can bypass the validateion'||chr(10)||'      String uniqPath = fp.getCanonicalPath();'||chr(10)||'      FileOutputStream writer = new FileOutputStream(uniqPath);'||chr(10)||'      writer.write(info.getBytes());'||chr(10)||'      writer.close();  // info may be write to unkown location'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  private Boolean is_safe(String path)'||chr(10)||'  {'||chr(10)||'    // do path validation'||chr(10)||'    if(path.startsWith("/share/user")) {'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class fio16_example'||chr(10)||'{'||chr(10)||''||chr(10)||'  public void bad_1(HttpServletRequest request, HttpServletResponse response) throws IOException'||chr(10)||'  {'||chr(10)||'    String fname = request.getParameter("FileName");'||chr(10)||'    String info = request.getParameter("Info");'||chr(10)||'    File fp = new File(fname);'||chr(10)||'    '||chr(10)||'    { '||chr(10)||'      String uniqPath = fp.getCanonicalPath();'||chr(10)||'      // va;odate after calling getCanonicalPath'||chr(10)||'      if (fp.exists() && is_safe(uniqPath) {    '||chr(10)||'        FileOutputStream writer = new FileOutputStream(uniqPath);'||chr(10)||'        writer.write(info.getBytes());'||chr(10)||'        writer.close();  // info write to validated location'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  private Boolean is_safe(String path)'||chr(10)||'  {'||chr(10)||'    // do path validation'||chr(10)||'    if(path.startsWith("/share/user")) {'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO16-J.detail', '#### 概要'||chr(10)||'该程序在没有首先规范化路径名的情况下对其进行验证'||chr(10)||'#### 解释'||chr(10)||'路径名可能包含特殊字符，这让验证变得困难。而且，路径名可能其实是软连接、影子等等，攻击者能利用这些绕过安全检查。在验证前完全解析/规范化路径名是很重要的。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class fio16_example'||chr(10)||'{'||chr(10)||''||chr(10)||'  public void bad_1(HttpServletRequest request, HttpServletResponse response) throws IOException'||chr(10)||'  {'||chr(10)||'    String fname = request.getParameter("FileName");'||chr(10)||'    String info = request.getParameter("Info");'||chr(10)||'    File fp = new File(fname);'||chr(10)||'    '||chr(10)||'    if(fp.exists() && is_safe(fname)) { // fp validated but before calling getCanonicalPath,'||chr(10)||'      //  hacker can bypass the validateion'||chr(10)||'      String uniqPath = fp.getCanonicalPath();'||chr(10)||'      FileOutputStream writer = new FileOutputStream(uniqPath);'||chr(10)||'      writer.write(info.getBytes());'||chr(10)||'      writer.close();  // info write to unknown location'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  private Boolean is_safe(String path)'||chr(10)||'  {'||chr(10)||'    // do path validation'||chr(10)||'    if(path.startsWith("/share/user")) {'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import java.io.*;'||chr(10)||''||chr(10)||'public class fio16_example'||chr(10)||'{'||chr(10)||''||chr(10)||'  public void bad_1(HttpServletRequest request, HttpServletResponse response) throws IOException'||chr(10)||'  {'||chr(10)||'    String fname = request.getParameter("FileName");'||chr(10)||'    String info = request.getParameter("Info");'||chr(10)||'    File fp = new File(fname);'||chr(10)||'    '||chr(10)||'    { '||chr(10)||'      String uniqPath = fp.getCanonicalPath();'||chr(10)||'      // validate after calling getCanonicalPath'||chr(10)||'      if (fp.exists() && is_safe(uniqPath) {    '||chr(10)||'        FileOutputStream writer = new FileOutputStream(uniqPath);'||chr(10)||'        writer.write(info.getBytes());'||chr(10)||'        writer.close();  // info write to validated  location'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  private Boolean is_safe(String path)'||chr(10)||'  {'||chr(10)||'    // do path validation'||chr(10)||'    if(path.startsWith("/share/user")) {'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO16-J.msg_template', 'In file ${se.filename}, line ${se.line}, the file name string ${se.var} in function ${se.func} is not canonicalized before the file is opened in ${ss.filename} at line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO16-J.msg_template', '在${se.filename}，${se.func} 里第${se.line}行, 文件名的字符串 ${se.var} 在文件打开之前未被规范化. 文件是在${ss.filename} 第${ss.line}行被打开的', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO16-J.name', 'Please canonicalize pathnames before validating them', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO16-J.name', '请在验证路径名之前将其规范化', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO16-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO16-J'),
 'BASIC','PRIORITY','4'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO16-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO16-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO16-J'),
 'STANDARD','OWASP','01'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO16-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO16-J'),
 'STANDARD','OWASP','07')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FIO52-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'FIO52-J', null, 'FIO52-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS17-J.+Prevent+XML+External+Entity+Attacks', '${rule.Xcalibyte.CERT.1.FIO52-J.name}', 2, 1, 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.FIO52-J.detail}', '${rule.Xcalibyte.CERT.1.FIO52-J.description}', '${rule.Xcalibyte.CERT.1.FIO52-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.FIO52-J.description', 'The program has stored unencrypted sensitive information on the client side', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO52-J.description', '程序在客户端存储了未加密的敏感信息', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO52-J.detail', '#### Abstract'||chr(10)||'The program has stored unencrypted sensitive information on the client side.'||chr(10)||'#### Explanation '||chr(10)||'If unencrypted sensitive information are provided and stored on the client side, an attacker could get hold of the info either directly or indirectly attack the client machine.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.ServletException;'||chr(10)||'import javax.servlet.annotation.WebServlet;'||chr(10)||'import javax.servlet.http.HttpServlet;'||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import javax.servlet.http.Cookie;'||chr(10)||'interface Login {'||chr(10)||'  public Boolean isUserValid(String userName, char[] passwd);'||chr(10)||'}'||chr(10)||''||chr(10)||'class LoginImpl implements Login {'||chr(10)||'  public Boolean isUserValid(String userName, char[] passwd) {'||chr(10)||'    // do the checking'||chr(10)||'    return true;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'public class fio52_0 {'||chr(10)||'  protected void doPost(HttpServletRequest request, HttpServletResponse response) {'||chr(10)||'   '||chr(10)||'    String username = request.getParameter("username");'||chr(10)||'    char[] password = request.getParameter("password").toCharArray();'||chr(10)||'    String userInfo = request.getParameter("userInfo");'||chr(10)||'  '||chr(10)||'    Login login = new LoginImpl();'||chr(10)||'           '||chr(10)||'    if (request.getCookies()[0] != null &&'||chr(10)||'        request.getCookies()[0].getValue() != null) {'||chr(10)||'        String[] value = request.getCookies()[0].getValue().split(";");'||chr(10)||'         '||chr(10)||'        if (!login.isUserValid(value[0], value[1].toCharArray())) {'||chr(10)||'          // Set error and return'||chr(10)||'        } else {'||chr(10)||'          // Forward to welcome page'||chr(10)||'        }'||chr(10)||'      } else {'||chr(10)||'          boolean validated = login.isUserValid(username, password);'||chr(10)||'         '||chr(10)||'          if (validated) {'||chr(10)||'            Cookie loginCookie = new Cookie("MyCookie", username + ";" + new String(password));'||chr(10)||'            Cookie infoCookie = new Cookie("userInfo", username + ":" + userInfo);'||chr(10)||'            loginCookie.setSecure(false);    // set loginCookie sent by any protocal'||chr(10)||'            response.addCookie(loginCookie); // [FIO52-J] sensitive cookie, should be encrypted or sent through secure protocal '||chr(10)||'            infoCookie.setSecure(true);      // set infoCookie sent by secure protocal'||chr(10)||'  	        response.addCookie(infoCookie);'||chr(10)||'  	  '||chr(10)||'            // ... Forward to welcome page'||chr(10)||'          } else {'||chr(10)||'            // Set error and return'||chr(10)||'          }'||chr(10)||'       }'||chr(10)||'  } '||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.ServletException;'||chr(10)||'import javax.servlet.annotation.WebServlet;'||chr(10)||'import javax.servlet.http.HttpServlet;'||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import javax.servlet.http.Cookie;'||chr(10)||'interface Login {'||chr(10)||'  public Boolean isUserValid(String userName, char[] passwd);'||chr(10)||'}'||chr(10)||''||chr(10)||'class LoginImpl implements Login {'||chr(10)||'  public Boolean isUserValid(String userName, char[] passwd) {'||chr(10)||'    // do the checking'||chr(10)||'    return true;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'public class fio52_0 {'||chr(10)||'  protected void doPost(HttpServletRequest request, HttpServletResponse response) {'||chr(10)||'   '||chr(10)||'    String username = request.getParameter("username");'||chr(10)||'    char[] password = request.getParameter("password").toCharArray();'||chr(10)||'    String userInfo = request.getParameter("userInfo");'||chr(10)||'  '||chr(10)||'    Login login = new LoginImpl();'||chr(10)||'           '||chr(10)||'    if (request.getCookies()[0] != null &&'||chr(10)||'        request.getCookies()[0].getValue() != null) {'||chr(10)||'        String[] value = request.getCookies()[0].getValue().split(";");'||chr(10)||'         '||chr(10)||'        if (!login.isUserValid(value[0], value[1].toCharArray())) {'||chr(10)||'          // Set error and return'||chr(10)||'        } else {'||chr(10)||'          // Forward to welcome page'||chr(10)||'        }'||chr(10)||'      } else {'||chr(10)||'          boolean validated = login.isUserValid(username, password);'||chr(10)||'         '||chr(10)||'          if (validated) {'||chr(10)||'            Cookie loginCookie = new Cookie("MyCookie", username + ";" + new String(password));'||chr(10)||'            Cookie infoCookie = new Cookie("userInfo", username + ":" + userInfo);'||chr(10)||'            loginCookie.setHttpOnly(true);   // use http protocol only'||chr(10)||'            loginCookie.setSecure(true);     // set loginCookie through secure protocol'||chr(10)||'            response.addCookie(loginCookie); // sensitive cookie is encrypted and sent through secure protocal '||chr(10)||'            infoCookie.setSecure(true);      // set infoCookie sent by secure protocal'||chr(10)||'  	        response.addCookie(infoCookie);'||chr(10)||'  	  '||chr(10)||'            // ... Forward to welcome page'||chr(10)||'          } else {'||chr(10)||'            // Set error and return'||chr(10)||'          }'||chr(10)||'       }'||chr(10)||'  } '||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO52-J.detail', '#### 概要'||chr(10)||'程序在客户端存储了未加密的敏感信息。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'如果在客户端提供并存储了未加密的敏感信息，则攻击者可以直接或间接地获取这些信息，从而直接或间接地攻击客户端计算机。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.ServletException;'||chr(10)||'import javax.servlet.annotation.WebServlet;'||chr(10)||'import javax.servlet.http.HttpServlet;'||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import javax.servlet.http.Cookie;'||chr(10)||'interface Login {'||chr(10)||'  public Boolean isUserValid(String userName, char[] passwd);'||chr(10)||'}'||chr(10)||''||chr(10)||'class LoginImpl implements Login {'||chr(10)||'  public Boolean isUserValid(String userName, char[] passwd) {'||chr(10)||'    // do the checking'||chr(10)||'    return true;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'public class fio52_0 {'||chr(10)||'  protected void doPost(HttpServletRequest request, HttpServletResponse response) {'||chr(10)||'   '||chr(10)||'    String username = request.getParameter("username");'||chr(10)||'    char[] password = request.getParameter("password").toCharArray();'||chr(10)||'    String userInfo = request.getParameter("userInfo");'||chr(10)||'  '||chr(10)||'    Login login = new LoginImpl();'||chr(10)||'           '||chr(10)||'    if (request.getCookies()[0] != null &&'||chr(10)||'        request.getCookies()[0].getValue() != null) {'||chr(10)||'        String[] value = request.getCookies()[0].getValue().split(";");'||chr(10)||'         '||chr(10)||'        if (!login.isUserValid(value[0], value[1].toCharArray())) {'||chr(10)||'          // Set error and return'||chr(10)||'        } else {'||chr(10)||'          // Forward to welcome page'||chr(10)||'        }'||chr(10)||'      } else {'||chr(10)||'          boolean validated = login.isUserValid(username, password);'||chr(10)||'         '||chr(10)||'          if (validated) {'||chr(10)||'            Cookie loginCookie = new Cookie("MyCookie", username + ";" + new String(password));'||chr(10)||'            Cookie infoCookie = new Cookie("userInfo", username + ":" + userInfo);'||chr(10)||'            loginCookie.setSecure(false);    // set loginCookie sent by any protocal'||chr(10)||'            response.addCookie(loginCookie); // [FIO52-J] sensitive cookie, should be encrypted or sent through secure protocal '||chr(10)||'            infoCookie.setSecure(true);      // set infoCookie sent by secure protocal'||chr(10)||'  	        response.addCookie(infoCookie);'||chr(10)||'  	  '||chr(10)||'            // ... Forward to welcome page'||chr(10)||'          } else {'||chr(10)||'            // Set error and return'||chr(10)||'          }'||chr(10)||'       }'||chr(10)||'  } '||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.ServletException;'||chr(10)||'import javax.servlet.annotation.WebServlet;'||chr(10)||'import javax.servlet.http.HttpServlet;'||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import javax.servlet.http.Cookie;'||chr(10)||'interface Login {'||chr(10)||'  public Boolean isUserValid(String userName, char[] passwd);'||chr(10)||'}'||chr(10)||''||chr(10)||'class LoginImpl implements Login {'||chr(10)||'  public Boolean isUserValid(String userName, char[] passwd) {'||chr(10)||'    // do the checking'||chr(10)||'    return true;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'public class fio52_0 {'||chr(10)||'  protected void doPost(HttpServletRequest request, HttpServletResponse response) {'||chr(10)||'   '||chr(10)||'    String username = request.getParameter("username");'||chr(10)||'    char[] password = request.getParameter("password").toCharArray();'||chr(10)||'    String userInfo = request.getParameter("userInfo");'||chr(10)||'  '||chr(10)||'    Login login = new LoginImpl();'||chr(10)||'           '||chr(10)||'    if (request.getCookies()[0] != null &&'||chr(10)||'        request.getCookies()[0].getValue() != null) {'||chr(10)||'        String[] value = request.getCookies()[0].getValue().split(";");'||chr(10)||'         '||chr(10)||'        if (!login.isUserValid(value[0], value[1].toCharArray())) {'||chr(10)||'          // Set error and return'||chr(10)||'        } else {'||chr(10)||'          // Forward to welcome page'||chr(10)||'        }'||chr(10)||'      } else {'||chr(10)||'          boolean validated = login.isUserValid(username, password);'||chr(10)||'         '||chr(10)||'          if (validated) {'||chr(10)||'            Cookie loginCookie = new Cookie("MyCookie", username + ";" + new String(password));'||chr(10)||'            Cookie infoCookie = new Cookie("userInfo", username + ":" + userInfo);'||chr(10)||'            loginCookie.setHttpOnly(true);   // use http protocol only'||chr(10)||'            loginCookie.setSecure(true);     // set loginCookie through secure protocol'||chr(10)||'            response.addCookie(loginCookie); // sensitive cookie is encrypted and sent through secure protocal '||chr(10)||'            infoCookie.setSecure(true);      // set infoCookie sent by secure protocal'||chr(10)||'  	        response.addCookie(infoCookie);'||chr(10)||'  	  '||chr(10)||'            // ... Forward to welcome page'||chr(10)||'          } else {'||chr(10)||'            // Set error and return'||chr(10)||'          }'||chr(10)||'       }'||chr(10)||'  } '||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO52-J.msg_template', 'In ${se.filename} function ${se.func}, line ${se.line}, unencrypted sensitive data is saved into client side. The tainted cookie ${se.var} starts at ${ss.filename}, line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO52-J.msg_template', '在${se.filename}，${se.func} 第${se.line}行, 未加密的敏感数据 经過${se.var} 存储在客户端. 這可被污染的cookie 在${ss.filename}, 第${ss.line}行産生', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.FIO52-J.name', 'Sensitive information should be encrypted if store on the client side', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.FIO52-J.name', '存储在客户端的敏感信息，则应进行加密', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO52-J');


insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO52-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO52-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='FIO52-J'),
 'STANDARD','OWASP','03')
ON CONFLICT DO NOTHING;

-- ------------------------
-- IDS00-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS00-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS00-J.+Prevent+SQL+injection', '${rule.Xcalibyte.CERT.1.IDS00-J.name}', null, 'IDS00-J', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS00-J.detail}', '${rule.Xcalibyte.CERT.1.IDS00-J.description}', '${rule.Xcalibyte.CERT.1.IDS00-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.IDS00-J.description', 'String input to any SQL query must be sanitized before the query', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS00-J.description', '在查询前必须净化所有SQL查询的字符串输入', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS00-J.detail', '#### Abstract'||chr(10)||'String input to any SQL query must be sanitized before the query.'||chr(10)||'#### Explanation'||chr(10)||'Strings that originated from an untrusted source must go through sanitization and validation process to avoid being used as malicious input to a SQL database. Failure to do so may cause data leakage and privacy violation.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.sql.DriverManager;'||chr(10)||'import java.sql.PreparedStatement;'||chr(10)||'import java.sql.ResultSet;'||chr(10)||'import java.sql.SQLException;'||chr(10)||'import java.sql.Statement;'||chr(10)||''||chr(10)||'public class j_ids00_2 {'||chr(10)||''||chr(10)||'  public Connection getConnection() throws SQLException {'||chr(10)||'    String dbConnection = System.getProperty("db.connection");'||chr(10)||'    return DriverManager.getConnection(dbConnection);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  String hashPwd(char[] pwd) {'||chr(10)||'    // Create hash of password'||chr(10)||'    return new String(pwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void doPrivilegedAction('||chr(10)||'    String username, char[] password    // username is unsanitized '||chr(10)||'  ) throws SQLException, SecurityException {'||chr(10)||'    Connection connection = getConnection();'||chr(10)||'    if (connection == null) {'||chr(10)||'      // Handle error'||chr(10)||'    }'||chr(10)||'    try {'||chr(10)||'      String pwd = hashPwd(password);'||chr(10)||'      // the string query is composed with unsanitized string (username) input'||chr(10)||'      String query = "SELECT * from where product username =" +'||chr(10)||'          username + " and password =" + pwd;'||chr(10)||'      '||chr(10)||'      PreparedStatement stmt = connection.prepareStatement(query);'||chr(10)||''||chr(10)||'      ResultSet result = stmt.executeQuery();'||chr(10)||'      if (!result.next()) {'||chr(10)||'        throw new SecurityException("User name/password incorrect");'||chr(10)||'      }'||chr(10)||''||chr(10)||'      // Authenticated'||chr(10)||'    } finally {'||chr(10)||'      try {'||chr(10)||'        connection.close();'||chr(10)||'      } catch (SQLException x) {'||chr(10)||'        // Forward to handler'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.sql.DriverManager;'||chr(10)||'import java.sql.PreparedStatement;'||chr(10)||'import java.sql.ResultSet;'||chr(10)||'import java.sql.SQLException;'||chr(10)||'import java.sql.Statement;'||chr(10)||''||chr(10)||'public class j_ids00_2 {'||chr(10)||''||chr(10)||'  public Connection getConnection() throws SQLException {'||chr(10)||'    String dbConnection = System.getProperty("db.connection");'||chr(10)||'    return DriverManager.getConnection(dbConnection);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  String hashPwd(char[] pwd) {'||chr(10)||'    // Create hash of password'||chr(10)||'    return new String(pwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void doPrivilegedAction('||chr(10)||'    String username, char[] password    // username is unsanitized '||chr(10)||'  ) throws SQLException, SecurityException {'||chr(10)||'    Connection connection = getConnection();'||chr(10)||'    if (connection == null) {'||chr(10)||'      // Handle error'||chr(10)||'    }'||chr(10)||'    try {'||chr(10)||'      String pwd = hashPwd(password);'||chr(10)||'      // the string query is composed with sanitized string (username) input'||chr(10)||'      PreparedStatement stmt = connection.prepareStatement("SELECT * FROM db_user WHERE username = ? AND passwd = ?");'||chr(10)||'      '||chr(10)||'      // use set*() to enforce strong type checking'||chr(10)||'      stmt.setString(1, username);     // to be sure, username may still be considered "tainted"'||chr(10)||'      stmt.setString(2, pwd);          // to be sure, pwd ma still be considered "tainted" '||chr(10)||''||chr(10)||'      ResultSet result = stmt.executeQuery();'||chr(10)||'      if (!result.next()) {'||chr(10)||'        throw new SecurityException("User name/password incorrect");'||chr(10)||'      }'||chr(10)||''||chr(10)||'      // Authenticated'||chr(10)||'    } finally {'||chr(10)||'      try {'||chr(10)||'        connection.close();'||chr(10)||'      } catch (SQLException x) {'||chr(10)||'        // Forward to handler'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS00-J.detail', '#### 概要'||chr(10)||'在查询前必须净化所有SQL查询的字符串输入'||chr(10)||'#### 解释'||chr(10)||'源于不受信任来源的字符串必须经过净化及验证流程，以避免用来作为SQL数据库的恶意输入。否则可能造成数据泄漏和隐私违反'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.sql.DriverManager;'||chr(10)||'import java.sql.PreparedStatement;'||chr(10)||'import java.sql.ResultSet;'||chr(10)||'import java.sql.SQLException;'||chr(10)||'import java.sql.Statement;'||chr(10)||''||chr(10)||'public class j_ids00_2 {'||chr(10)||''||chr(10)||'  public Connection getConnection() throws SQLException {'||chr(10)||'    String dbConnection = System.getProperty("db.connection");'||chr(10)||'    return DriverManager.getConnection(dbConnection);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  String hashPwd(char[] pwd) {'||chr(10)||'    // Create hash of password'||chr(10)||'    return new String(pwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void doPrivilegedAction('||chr(10)||'    String username, char[] password    // username is unsanitized '||chr(10)||'  ) throws SQLException, SecurityException {'||chr(10)||'    Connection connection = getConnection();'||chr(10)||'    if (connection == null) {'||chr(10)||'      // Handle error'||chr(10)||'    }'||chr(10)||'    try {'||chr(10)||'      String pwd = hashPwd(password);'||chr(10)||'      // the string query is composed with unsanitized string (username) input'||chr(10)||'      String query = "SELECT * from where product username =" +'||chr(10)||'          username + " and password =" + pwd;'||chr(10)||'      '||chr(10)||'      PreparedStatement stmt = connection.prepareStatement(query);'||chr(10)||''||chr(10)||'      ResultSet result = stmt.executeQuery();'||chr(10)||'      if (!result.next()) {'||chr(10)||'        throw new SecurityException("User name/password incorrect");'||chr(10)||'      }'||chr(10)||''||chr(10)||'      // Authenticated'||chr(10)||'    } finally {'||chr(10)||'      try {'||chr(10)||'        connection.close();'||chr(10)||'      } catch (SQLException x) {'||chr(10)||'        // Forward to handler'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.sql.DriverManager;'||chr(10)||'import java.sql.PreparedStatement;'||chr(10)||'import java.sql.ResultSet;'||chr(10)||'import java.sql.SQLException;'||chr(10)||'import java.sql.Statement;'||chr(10)||''||chr(10)||'public class j_ids00_2 {'||chr(10)||''||chr(10)||'  public Connection getConnection() throws SQLException {'||chr(10)||'    String dbConnection = System.getProperty("db.connection");'||chr(10)||'    return DriverManager.getConnection(dbConnection);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  String hashPwd(char[] pwd) {'||chr(10)||'    // Create hash of password'||chr(10)||'    return new String(pwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void doPrivilegedAction('||chr(10)||'    String username, char[] password    // username is unsanitized '||chr(10)||'  ) throws SQLException, SecurityException {'||chr(10)||'    Connection connection = getConnection();'||chr(10)||'    if (connection == null) {'||chr(10)||'      // Handle error'||chr(10)||'    }'||chr(10)||'    try {'||chr(10)||'      String pwd = hashPwd(password);'||chr(10)||'      // the string query is composed with sanitized string (username) input'||chr(10)||'      PreparedStatement stmt = connection.prepareStatement("SELECT * FROM db_user WHERE username = ? AND passwd = ?");'||chr(10)||'      '||chr(10)||'      // use set*() to enforce strong type checking'||chr(10)||'      stmt.setString(1, username);     // to be sure, username may still be considered "tainted"'||chr(10)||'      stmt.setString(2, pwd);          // to be sure, pwd may still be considered "tainted" '||chr(10)||''||chr(10)||'      ResultSet result = stmt.executeQuery();'||chr(10)||'      if (!result.next()) {'||chr(10)||'        throw new SecurityException("User name/password incorrect");'||chr(10)||'      }'||chr(10)||''||chr(10)||'      // Authenticated'||chr(10)||'    } finally {'||chr(10)||'      try {'||chr(10)||'        connection.close();'||chr(10)||'      } catch (SQLException x) {'||chr(10)||'        // Forward to handler'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS00-J.msg_template', 'In file ${se.filename}, function ${se.func} the string ${se.var} is not santized before the SQL query at line ${se.line}. The strings is formed starting from ${ss.filename) at line ${ss.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS00-J.msg_template', '在${se.filename}, 函数 ${se.func} 的字符串 ${se.var} 的SQL查询之前未有被santize. 這字符串是从${ss.filename), 第${ss.line}行开始的', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS00-J.name', 'String input to any SQL query must be sanitized before the query', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS00-J.name', '在查询前必须净化所有SQL查询的字符串输入', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS00-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS00-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS00-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS00-J'),
 'STANDARD','OWASP','01'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS00-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS00-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS00-J'),
 'STANDARD','OWASP','06'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS00-J'),
 'STANDARD','OWASP','07')
ON CONFLICT DO NOTHING;

-- ------------------------
-- IDS01-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS01-J', null, 'IDS01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS01-J.+Normalize+strings+before+validating+them', '${rule.Xcalibyte.CERT.1.IDS01-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS01-J.detail}', '${rule.Xcalibyte.CERT.1.IDS01-J.description}', '${rule.Xcalibyte.CERT.1.IDS01-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.IDS01-J.description', 'The program is validating strings that have not been normalized', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS01-J.description', '该程序正在验证尚未被规范化的字符串', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS01-J.detail', '#### Abstract'||chr(10)||'The program is validating strings that have not been normalized.'||chr(10)||'#### Explanation'||chr(10)||'Strings have many representations such as "char", "unicode", etc. Many filtering or validation mechanisms are based on the strings'' character data. Failure to filter out or normalize the string before validation could end up bypassing the validation mechanism, resulting in execution of arbitrary code.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.text.Normalizer;'||chr(10)||'import java.text.Normalizer.Form;'||chr(10)||'import java.util.regex.Matcher;'||chr(10)||'import java.util.regex.Pattern;'||chr(10)||'  '||chr(10)||'public class j_ids01_0 { // inspired by CERT-J example'||chr(10)||'  public static String filterString(String str) {'||chr(10)||' '||chr(10)||'    // Non-compliant - Validate before the string is normalized'||chr(10)||'    Pattern pattern = Pattern.compile("<script>");'||chr(10)||'    Matcher matcher = pattern.matcher(str);'||chr(10)||'    if (matcher.find()) {'||chr(10)||'      throw new IllegalArgumentException("Invalid input");'||chr(10)||'    }'||chr(10)||'    // Normalization form for validate String is NFKC'||chr(10)||'    String s = Normalizer.normalize(str, Form.NFKC); '||chr(10)||'    return s;'||chr(10)||'  }'||chr(10)||' '||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    // "\uFDEF" is a noncharacter code point'||chr(10)||'    String maliciousInput = "<scr" + "\uFDEF" + "ipt>";'||chr(10)||'    String sb = filterString(maliciousInput);'||chr(10)||'    // sb = "<script>"'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.text.Normalizer;'||chr(10)||'import java.text.Normalizer.Form;'||chr(10)||'import java.util.regex.Matcher;'||chr(10)||'import java.util.regex.Pattern;'||chr(10)||'  '||chr(10)||'public class j_ids01_0 { // inspired by CERT-J example'||chr(10)||'  public static String filterString(String str) {'||chr(10)||' '||chr(10)||'    // Normalization form for validate String is NFKC'||chr(10)||'    String s = Normalizer.normalize(str, Form.NFKC); '||chr(10)||'    // compliant - Validate after the string is normalized'||chr(10)||'    Pattern pattern = Pattern.compile("<script>");'||chr(10)||'    Matcher matcher = pattern.matcher(str);'||chr(10)||'    if (matcher.find()) {'||chr(10)||'      throw new IllegalArgumentException("Invalid input");'||chr(10)||'    }'||chr(10)||'    return s;'||chr(10)||'  }'||chr(10)||' '||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    // "\uFDEF" is a noncharacter code point'||chr(10)||'    String maliciousInput = "<scr" + "\uFDEF" + "ipt>";'||chr(10)||'    String sb = filterString(maliciousInput);'||chr(10)||'    // sb = "<script>"'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS01-J.detail', '#### 概要'||chr(10)||'该程序正在验证尚未被规范化的字符串'||chr(10)||'#### 解释'||chr(10)||'字符串有多种表现形式，例如“char”、“unicode”等等。许多过滤或验证机制是基于字符串的字符数据。未能在验证前过滤或规范化字符串可能最终致使绕过验证机制，从而造成任意代码的执行'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.text.Normalizer;'||chr(10)||'import java.text.Normalizer.Form;'||chr(10)||'import java.util.regex.Matcher;'||chr(10)||'import java.util.regex.Pattern;'||chr(10)||'  '||chr(10)||'public class j_ids01_0 {'||chr(10)||'  public static String filterString(String str) {'||chr(10)||' '||chr(10)||'    // Non-compliant - Validate before the string is normalized'||chr(10)||'    Pattern pattern = Pattern.compile("<script>");'||chr(10)||'    Matcher matcher = pattern.matcher(str);'||chr(10)||'    if (matcher.find()) {'||chr(10)||'      throw new IllegalArgumentException("Invalid input");'||chr(10)||'    }'||chr(10)||'    // Normalization form for validate String is NFKC'||chr(10)||'    String s = Normalizer.normalize(str, Form.NFKC); '||chr(10)||'    return s;'||chr(10)||'  }'||chr(10)||' '||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    // "\uFDEF" is a noncharacter code point'||chr(10)||'    String maliciousInput = "<scr" + "\uFDEF" + "ipt>";'||chr(10)||'    String sb = filterString(maliciousInput);'||chr(10)||'    // sb = "<script>"'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.text.Normalizer;'||chr(10)||'import java.text.Normalizer.Form;'||chr(10)||'import java.util.regex.Matcher;'||chr(10)||'import java.util.regex.Pattern;'||chr(10)||'  '||chr(10)||'public class j_ids01_0 { // inspired by CERT-J example'||chr(10)||'  public static String filterString(String str) {'||chr(10)||' '||chr(10)||'    // Normalization form for validate String is NFKC'||chr(10)||'    String s = Normalizer.normalize(str, Form.NFKC); '||chr(10)||'    // compliant - Validate after the string is normalized'||chr(10)||'    Pattern pattern = Pattern.compile("<script>");'||chr(10)||'    Matcher matcher = pattern.matcher(str);'||chr(10)||'    if (matcher.find()) {'||chr(10)||'      throw new IllegalArgumentException("Invalid input");'||chr(10)||'    }'||chr(10)||'    return s;'||chr(10)||'  }'||chr(10)||' '||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    // "\uFDEF" is a noncharacter code point'||chr(10)||'    String maliciousInput = "<scr" + "\uFDEF" + "ipt>";'||chr(10)||'    String sb = filterString(maliciousInput);'||chr(10)||'    // sb = "<script>"'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS01-J.msg_template', 'In file ${se.filename}, line ${se.line}, the string is validated in function ${se.var}, before being normalized in ${ss.func}, line ${ss.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS01-J.msg_template', '在${ss.filename}, 第${ss.line}行已验证了字符串. 並在函数 ${se.var} 被规范化. 可是之后在${se.filename},第${se.line}行才规范化', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS01-J.name', 'Do not validate strings that have not been normalized', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS01-J.name', '请勿验证尚未被规范化的字符串', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS01-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS01-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS01-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS01-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS01-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS01-J'),
 'STANDARD','OWASP','06')
ON CONFLICT DO NOTHING;

-- ------------------------
-- IDS07-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS07-J', null, 'IDS07-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS07-J.+Sanitize+untrusted+data+passed+to+the+Runtime.exec%28%29+method', '${rule.Xcalibyte.CERT.1.IDS07-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS07-J.detail}', '${rule.Xcalibyte.CERT.1.IDS07-J.description}', '${rule.Xcalibyte.CERT.1.IDS07-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.IDS07-J.description', 'The program has passed untrusted data as input to Runtime.exec() method', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS07-J.description', '该程序将不受信任数据作为输入传递给了Runtime.exec()方法', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS07-J.detail', '#### Abstract'||chr(10)||'The program has passed untrusted data as input to Runtime.exec() method.'||chr(10)||'#### Explanation'||chr(10)||'Untrusted data passed to Runtime.exec() will expose the system to argument injection attack. The string must be sanitized to get rid of characters such as spaces, double quotes, ''-'', etc.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.InputStream;'||chr(10)||''||chr(10)||'public class ids07_0 {  // example from CERT-J'||chr(10)||'  public static void main(String[] args) throws Exception {'||chr(10)||'  '||chr(10)||'    // dir is input from environment in which the app is running'||chr(10)||'    String dir = System.getProperty("dir");'||chr(10)||'    Runtime rt = Runtime.getRuntime();'||chr(10)||'    '||chr(10)||'    // input from environment is directly concatenated and feed to Runtime.exec'||chr(10)||'    // Subject to command line injection'||chr(10)||'    Process proc = rt.exec("bash -c ls " + dir);'||chr(10)||'    '||chr(10)||'    int result = proc.waitFor();'||chr(10)||'    if (result != 0) {'||chr(10)||'      System.out.println("process error: " + result);'||chr(10)||'    }'||chr(10)||'    // continue processing'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.InputStream;'||chr(10)||''||chr(10)||'public class ids07_0 {  // example from CERT-J'||chr(10)||'  public static void main(String[] args) throws Exception {'||chr(10)||'  '||chr(10)||'    // dir is input from environment in which the app is running'||chr(10)||'    String dir = System.getProperty("dir");'||chr(10)||'    if (!Pattern.matches("[0-9A-Za-z@.]+", dir) { // whitelist characters allowed'||chr(10)||'      // report error and exit'||chr(10)||'      ...'||chr(10)||'    }'||chr(10)||'    Runtime rt = Runtime.getRuntime();'||chr(10)||'    '||chr(10)||'    // input from environment is directly concatenated and feed to Runtime.exec'||chr(10)||'    // Subject to command line injection'||chr(10)||'    Process proc = rt.exec("bash -c ls " + dir);'||chr(10)||'    '||chr(10)||'    int result = proc.waitFor();'||chr(10)||'    if (result != 0) {'||chr(10)||'      System.out.println("process error: " + result);'||chr(10)||'    }'||chr(10)||'    // continue processing'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS07-J.detail', '#### 概要'||chr(10)||'该程序将不受信任数据作为输入传递给了Runtime.exec()方法'||chr(10)||'#### 解释'||chr(10)||'传递给Runtime.exec()的不受信任数据会暴露系统使其受到参数注入攻击。字符串必须经过净化以去除像空格、双引号、“-”这样的字符。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.InputStream;'||chr(10)||''||chr(10)||'public class ids07_fn_0 { // example from CERT-J'||chr(10)||'  public static void main(String[] args) throws Exception {'||chr(10)||'  '||chr(10)||'    // dir is input from environment in which the app is running'||chr(10)||'    String dir = System.getProperty("dir");'||chr(10)||'    Runtime rt = Runtime.getRuntime();'||chr(10)||'    '||chr(10)||'    // input from environment is directly concatenated and feed to Runtime.exec'||chr(10)||'    // Subject to command line injection'||chr(10)||'    Process proc = rt.exec("bash -c ls " + dir);'||chr(10)||'    '||chr(10)||'    int result = proc.waitFor();'||chr(10)||'    if (result != 0) {'||chr(10)||'      System.out.println("process error: " + result);'||chr(10)||'    }'||chr(10)||'    // continue processing'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.InputStream;'||chr(10)||''||chr(10)||'public class ids07_0 {  // example from CERT-J'||chr(10)||'  public static void main(String[] args) throws Exception {'||chr(10)||'  '||chr(10)||'    // dir is input from environment in which the app is running'||chr(10)||'    String dir = System.getProperty("dir");'||chr(10)||'    if (!Pattern.matches("[0-9A-Za-z@.]+", dir) { // whitelist characters allowed'||chr(10)||'      // report error and exit'||chr(10)||'      ...'||chr(10)||'    }'||chr(10)||'    Runtime rt = Runtime.getRuntime();'||chr(10)||'    '||chr(10)||'    // input from environment is directly concatenated and feed to Runtime.exec'||chr(10)||'    // Subject to command line injection'||chr(10)||'    Process proc = rt.exec("bash -c ls " + dir);'||chr(10)||'    '||chr(10)||'    int result = proc.waitFor();'||chr(10)||'    if (result != 0) {'||chr(10)||'      System.out.println("process error: " + result);'||chr(10)||'    }'||chr(10)||'    // continue processing'||chr(10)||'    // ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS07-J.msg_template', 'In file ${ss.filename}, line ${ss.line}, the string ${se.var}, is not sanitized before being passed to runtime exec() in ${se.filename},line ${se.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS07-J.msg_template', '在${ss.filename}, 第${ss.line}行,的字符串 ${se.var}, 在${se.filenname}, 第${se.line}行传递给runtime()之前未进行净化', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS07-J.name', 'Do not pass untrusted data as input to Runtime.exec() method', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS07-J.name', '请勿把不受信任数据作为输入传递给Runtime.exec()方法', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS07-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS07-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS07-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS07-J'),
 'STANDARD','OWASP','01'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS07-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS07-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS07-J'),
 'STANDARD','OWASP','06'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS07-J'),
 'STANDARD','OWASP','07')
ON CONFLICT DO NOTHING;

-- ------------------------
-- IDS11-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS11-J', null, 'IDS11-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS11-J.+Perform+any+string+modifications+before+validation', '${rule.Xcalibyte.CERT.1.IDS11-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS11-J.detail}', '${rule.Xcalibyte.CERT.1.IDS11-J.description}', '${rule.Xcalibyte.CERT.1.IDS11-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.IDS11-J.description', 'The program has changed a string that has gone through validation', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS11-J.description', '该程序修改了一个已经过验证的字符串', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS11-J.detail', '#### Abstract'||chr(10)||'The program has changed a string that has gone through validation.'||chr(10)||'#### Explanation'||chr(10)||'A string is modified after going through validation for processing, the string may in turn become untrusted again, nullifying the previous validation effort. '||chr(10)||''||chr(10)||'#### Example 1 - Avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.text.Normalizer;'||chr(10)||'import java.text.Normalizer.Form;'||chr(10)||'import java.util.regex.Matcher;'||chr(10)||'import java.util.regex.Pattern;'||chr(10)||'  '||chr(10)||'public class j_ids11_0 {'||chr(10)||'  public static String filterString(String s) {'||chr(10)||'    // Normalize input string'||chr(10)||'    String str = Normalizer.normalize(s, Form.NFKC);'||chr(10)||' '||chr(10)||'    // Validate input after normalization'||chr(10)||'    Pattern pattern = Pattern.compile("<script>");'||chr(10)||'    Matcher matcher = pattern.matcher(str);'||chr(10)||'    if (matcher.find()) {'||chr(10)||'      throw new IllegalArgumentException("Invalid input");'||chr(10)||'    }'||chr(10)||' '||chr(10)||'    // Deletes noncharacter code'||chr(10)||'    // input string may have noncharacter code'||chr(10)||'    // however, the new string formed should go through normalize and validation again'||chr(10)||'    str = str.replaceAll("[\\p{Cn}]", "");'||chr(10)||'    return str;'||chr(10)||'  }'||chr(10)||'  // ... more code '||chr(10)||'}'||chr(10)||' '||chr(10)||' '||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example 1 - perfer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.text.Normalizer;'||chr(10)||'import java.text.Normalizer.Form;'||chr(10)||'import java.util.regex.Matcher;'||chr(10)||'import java.util.regex.Pattern;'||chr(10)||'  '||chr(10)||'public class j_ids11_0 {'||chr(10)||'  public static String filterString(String s) {'||chr(10)||'    // Normalize input string'||chr(10)||'    String str = Normalizer.normalize(s, Form.NFKC);'||chr(10)||'    '||chr(10)||'    // Deletes noncharacter code'||chr(10)||'    // input string may have noncharacter code'||chr(10)||'    // however, the new string formed should go through normalize and validation again'||chr(10)||'    str = str.replaceAll("[\\p{Cn}]", "");'||chr(10)||' '||chr(10)||'    // Validate input after normalization'||chr(10)||'    Pattern pattern = Pattern.compile("<script>");'||chr(10)||'    Matcher matcher = pattern.matcher(str);'||chr(10)||'    if (matcher.find()) {'||chr(10)||'      throw new IllegalArgumentException("Invalid input");'||chr(10)||'    }'||chr(10)||' '||chr(10)||'    return str;'||chr(10)||'  }'||chr(10)||'  // ... more code '||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS11-J.detail', '#### 概要'||chr(10)||'该程序修改了一个已经过验证的字符串'||chr(10)||'#### 解释'||chr(10)||'字符串在经过验证以便处理后又经过了修改，如此该字符串可能又会变不受信任，使之前的验证措施变得无效。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.text.Normalizer;'||chr(10)||'import java.text.Normalizer.Form;'||chr(10)||'import java.util.regex.Matcher;'||chr(10)||'import java.util.regex.Pattern;'||chr(10)||'  '||chr(10)||'public class j_ids11_0 {'||chr(10)||'  public static String filterString(String s) {'||chr(10)||'    // Normalize input string'||chr(10)||'    String str = Normalizer.normalize(s, Form.NFKC);'||chr(10)||' '||chr(10)||'    // Validate input after normalization'||chr(10)||'    Pattern pattern = Pattern.compile("<script>");'||chr(10)||'    Matcher matcher = pattern.matcher(str);'||chr(10)||'    if (matcher.find()) {'||chr(10)||'      throw new IllegalArgumentException("Invalid input");'||chr(10)||'    }'||chr(10)||' '||chr(10)||'    // Deletes noncharacter code'||chr(10)||'    // input string may have noncharacter code'||chr(10)||'    // however, the new string formed should go through normalize and validation again'||chr(10)||'    str = str.replaceAll("[\\p{Cn}]", "");'||chr(10)||'    return str;'||chr(10)||'  }'||chr(10)||'  // ... more code '||chr(10)||'}'||chr(10)||' '||chr(10)||''||chr(10)||'````'||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.text.Normalizer;'||chr(10)||'import java.text.Normalizer.Form;'||chr(10)||'import java.util.regex.Matcher;'||chr(10)||'import java.util.regex.Pattern;'||chr(10)||'  '||chr(10)||'public class j_ids11_0 {'||chr(10)||'  public static String filterString(String s) {'||chr(10)||'    // Normalize input string'||chr(10)||'    String str = Normalizer.normalize(s, Form.NFKC);'||chr(10)||'    '||chr(10)||'    // Deletes noncharacter code'||chr(10)||'    // input string may have noncharacter code'||chr(10)||'    // however, the new string formed should go through normalize and validation again'||chr(10)||'    str = str.replaceAll("[\\p{Cn}]", "");'||chr(10)||' '||chr(10)||'    // Validate input after normalization'||chr(10)||'    Pattern pattern = Pattern.compile("<script>");'||chr(10)||'    Matcher matcher = pattern.matcher(str);'||chr(10)||'    if (matcher.find()) {'||chr(10)||'      throw new IllegalArgumentException("Invalid input");'||chr(10)||'    }'||chr(10)||' '||chr(10)||'    return str;'||chr(10)||'  }'||chr(10)||'  // ... more code '||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS11-J.msg_template', 'The string ${se.var} is normalized in ${ss.filename} at line ${ss.line}, and then validated. In file ${se.filename}, line ${se.line}, the strig is modified making the string unntrusted again', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS11-J.msg_template', '字符串 ${se.var} 字符串在${ss.filename},第${ss.line}行被规范化随后並验证了, 可是在${se.filename}, 函数${se.func}, 第${se.line}行被俢改，变成不可信仼的了', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS11-J.name', 'Do not modify a string that has gone through validation', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS11-J.name', '请勿修改已经过验证的字符串', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS11-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS11-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS11-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS11-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS11-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS11-J'),
 'STANDARD','OWASP','05')
ON CONFLICT DO NOTHING;

-- ------------------------
-- IDS15-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS15-J', null, 'IDS15-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS15-J.+Do+not+allow+sensitive+information+to+leak+outside+a+trust+boundary', '${rule.Xcalibyte.CERT.1.IDS15-J.name}', 2, 2, 'LIKELY', 'HIGH', '${rule.Xcalibyte.CERT.1.IDS15-J.detail}', '${rule.Xcalibyte.CERT.1.IDS15-J.description}', '${rule.Xcalibyte.CERT.1.IDS15-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.IDS15-J.description', 'Sensitive data should be kept secure, this includes input and output data', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS15-J.description', '应该要保持敏感数据的安全性，这包括输入和输出数据边界泄漏', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS15-J.detail', '#### Abstract'||chr(10)||'Sensitive data should be kept secure, this includes input and output data.'||chr(10)||'#### Explanation'||chr(10)||'Sensitive information should not be allowed to leak across trusted boundaries. This is to keep integrity and security of different subsystems within a complex system. '||chr(10)||''||chr(10)||'#### Example '||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||''||chr(10)||'public class j_ids15_0'||chr(10)||'{'||chr(10)||'  public void setSession(HttpServletRequest request, String attrName, String attrValue) {'||chr(10)||'    request.getSession().setAttribute(attrName, attrValue);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void configSession(HttpServletRequest request) {'||chr(10)||'    String value = request.getParameter("Config");'||chr(10)||'    String data = System.getenv("APP_DATA");'||chr(10)||'    if(value.startsWith("APP_DATA")) {'||chr(10)||'      setSession(request, "APP_DATA", data);           // sensitive data'||chr(10)||'    } else if(value.startsWith("SAFE_DATA")) {'||chr(10)||'      String safeData = Encryption(data);'||chr(10)||'      setSession(request, "SAFE_DATA", safeData);      // data sanitized'||chr(10)||'    } else {'||chr(10)||'      setSession(request, "attr", "value");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public String Encryption(String data) {'||chr(10)||'    // encrypt the input data'||chr(10)||'    // ...'||chr(10)||'    String safeData = data.replace("a", "z");'||chr(10)||'    return safeData;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||' '||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS15-J.detail', '#### 概要'||chr(10)||'应该要保持敏感数据的安全性，这包括输入和输出数据边界泄漏.'||chr(10)||'#### 解释'||chr(10)||'不应允许敏感信息跨不受信任边界泄漏。这是为了保持复杂系统里不同子系统的完整性和安全性。 '||chr(10)||''||chr(10)||'#### 示例 '||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||''||chr(10)||'public class j_ids15_0'||chr(10)||'{'||chr(10)||'  public void setSession(HttpServletRequest request, String attrName, String attrValue) {'||chr(10)||'    request.getSession().setAttribute(attrName, attrValue);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void configSession(HttpServletRequest request) {'||chr(10)||'    String value = request.getParameter("Config");'||chr(10)||'    String data = System.getenv("APP_DATA");'||chr(10)||'    if(value.startsWith("APP_DATA")) {'||chr(10)||'      setSession(request, "APP_DATA", data);           // sensitive data'||chr(10)||'    } else if(value.startsWith("SAFE_DATA")) {'||chr(10)||'      String safeData = Encryption(data);'||chr(10)||'      setSession(request, "SAFE_DATA", safeData);      // data sanitized'||chr(10)||'    } else {'||chr(10)||'      setSession(request, "attr", "value");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public String Encryption(String data) {'||chr(10)||'    // encrypt the input data'||chr(10)||'    // ...'||chr(10)||'    String safeData = data.replace("a", "z");'||chr(10)||'    return safeData;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||' '||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS15-J.msg_template', 'In file ${se.filename}, line ${se.line}, sensitive data ${se.var} in function ${se.func} is leaked outside a trust boundary.  The data source is from ${ss.filename} at line ${ss.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS15-J.msg_template', '敏感数据 ${se.var} 在${se.filename},第${se.line}会被泄漏. 数据源于${ss.filename}, 第${ss.line}行', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS15-J.name', 'Sensitive data (both input and output) should be sanitized/normalized', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS15-J.name', '应净化/规范化敏感数据（输入和输出）', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS15-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS15-J'),
 'BASIC','PRIORITY','6'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS15-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS15-J'),
 'STANDARD','OWASP','02')
ON CONFLICT DO NOTHING;

-- ------------------------
-- IDS16-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS16-J', null, 'IDS16-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS16-J.+Prevent+XML+Injection', '${rule.Xcalibyte.CERT.1.IDS16-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS16-J.detail}', '${rule.Xcalibyte.CERT.1.IDS16-J.description}', '${rule.Xcalibyte.CERT.1.IDS16-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.IDS16-J.description', 'The program is manipulating XMLStrings intended for XML processing without validating the string', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS16-J.description', '该程序正在没有验证字符串的情况下操作用于XML处理的XMLStrings', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS16-J.detail', '#### Abstract'||chr(10)||'The program is manipulating XMLStrings intended for XML processing without validating the string.'||chr(10)||'#### Explanation'||chr(10)||'When XMLStrings are not sanitized, the XML string could be maliciously injected and mistaken for valid XML, resulting in XML injection attack.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.BufferedOutputStream;'||chr(10)||'import java.io.ByteArrayOutputStream;'||chr(10)||'import java.io.IOException;'||chr(10)||' '||chr(10)||'public class j_ids16_1 {'||chr(10)||'  public static void createXMLStreamBad(final BufferedOutputStream outStream,'||chr(10)||'      final String number) throws IOException {'||chr(10)||'    String xmlString = "<item>\n<description>Widget</description>\n"'||chr(10)||'        + "<level>500</>\n";'||chr(10)||''||chr(10)||'    if (number != null) {'||chr(10)||'      // the string xmlString should be validated'||chr(10)||'      // to prevent XML injection'||chr(10)||'	  xmlString = xmlString + "<number>" + number'||chr(10)||'      + "</number></item>";'||chr(10)||'    }'||chr(10)||'    outStream.write(xmlString.getBytes());'||chr(10)||'    outStream.flush();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.BufferedOutputStream;'||chr(10)||'import java.io.ByteArrayOutputStream;'||chr(10)||'import java.io.IOException;'||chr(10)||' '||chr(10)||'public class j_ids16_1 {'||chr(10)||'  public static void createXMLStreamBad(final BufferedOutputStream outStream,'||chr(10)||'      final String number) throws IOException {'||chr(10)||'    String xmlString = "<item>\n<description>Widget</description>\n"'||chr(10)||'        + "<level>500</>\n";'||chr(10)||''||chr(10)||'    if (number != null) {'||chr(10)||'      int sanitizedNumber = Integer.parseUnsignedInt(number);'||chr(10)||'      // the string xmlString should be validated'||chr(10)||'      // to prevent XML injection'||chr(10)||'	  xmlString = xmlString + "<number>" + sanitizedNumber'||chr(10)||'      + "</number></item>";'||chr(10)||'    }'||chr(10)||'    outStream.write(xmlString.getBytes());'||chr(10)||'    outStream.flush();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS16-J.detail', '#### 概要'||chr(10)||'该程序正在没有验证字符串的情况下操作用于XML处理的XMLStrings'||chr(10)||'#### 解释'||chr(10)||'当XMLStrings没有经过净化时，可能会恶意注入XML字符串并将其错认为有效的XML，从而导致XML注入攻击。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.BufferedOutputStream;'||chr(10)||'import java.io.ByteArrayOutputStream;'||chr(10)||'import java.io.IOException;'||chr(10)||' '||chr(10)||'public class j_ids16_1 {'||chr(10)||'  public static void createXMLStreamBad(final BufferedOutputStream outStream,'||chr(10)||'      final String number) throws IOException {'||chr(10)||'    String xmlString = "<item>\n<description>Widget</description>\n"'||chr(10)||'        + "<level>500</>\n";'||chr(10)||''||chr(10)||'    if (number != null) {'||chr(10)||'      // the string xmlString should be validated'||chr(10)||'      // to prevent XML injection'||chr(10)||'	  xmlString = xmlString + "<number>" + number'||chr(10)||'      + "</number></item>";'||chr(10)||'    }'||chr(10)||'    outStream.write(xmlString.getBytes());'||chr(10)||'    outStream.flush();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'####  示例 - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.BufferedOutputStream;'||chr(10)||'import java.io.ByteArrayOutputStream;'||chr(10)||'import java.io.IOException;'||chr(10)||' '||chr(10)||'public class j_ids16_1 {'||chr(10)||'  public static void createXMLStreamBad(final BufferedOutputStream outStream,'||chr(10)||'      final String number) throws IOException {'||chr(10)||'    String xmlString = "<item>\n<description>Widget</description>\n"'||chr(10)||'        + "<level>500</>\n";'||chr(10)||''||chr(10)||'    if (number != null) {'||chr(10)||'      int sanitizedNumber = Integer.parseUnsignedInt(number);'||chr(10)||'      // the string xmlString should be validated'||chr(10)||'      // to prevent XML injection'||chr(10)||'	  xmlString = xmlString + "<number>" + sanitizedNumber'||chr(10)||'      + "</number></item>";'||chr(10)||'    }'||chr(10)||'    outStream.write(xmlString.getBytes());'||chr(10)||'    outStream.flush();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS16-J.msg_template', 'In file ${se.filename}, line ${se.line}, the string ${se.var} in function ${se.func} is not sanitized before being passed to XML query at line ${ss.line}, ${ss.filename}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS16-J.msg_template', '在${se.filename}, 第${se.line}行, 函数${se.func}的字符串${se.var}未有被净化便在${ss.filename}, 第${ss.line}行被传递到XML查询', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS16-J.name', 'XMLStrings intended for XML processing must be validated before processing', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS16-J.name', '用于XML处理的XMLStrings必须在处理之前通过验证', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS16-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS16-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS16-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS16-J'),
 'STANDARD','OWASP','01'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS16-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS16-J'),
 'STANDARD','OWASP','04'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS16-J'),
 'STANDARD','OWASP','06'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS16-J'),
 'STANDARD','OWASP','07')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IDS17-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'IDS17-J', null, 'IDS17-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/IDS17-J.+Prevent+XML+External+Entity+Attacks', '${rule.Xcalibyte.CERT.1.IDS17-J.name}', 2, 2, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.IDS17-J.detail}', '${rule.Xcalibyte.CERT.1.IDS17-J.description}', '${rule.Xcalibyte.CERT.1.IDS17-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.IDS17-J.description', 'An external declaration that defines an external entity (typically specified by an URI) should be properly filtered', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS17-J.description', '应该适当地过滤定义了外部实体（通常由URI指定）的外部声明', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS17-J.detail', '#### Abstract'||chr(10)||'An external declaration that defines an external entity (typically specified by an URI) should be properly filtered. '||chr(10)||'#### Explanation'||chr(10)||'The external entities should be filtered, either through a whitelist or pre-registration so that XML attacks can be contained. Failure to do so could result in denial of service or data leakage.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.IOException;'||chr(10)||'import org.xml.sax.EntityResolver;'||chr(10)||'import org.xml.sax.InputSource;'||chr(10)||'import org.xml.sax.SAXException;'||chr(10)||'import java.io.FileInputStream;'||chr(10)||'import java.io.InputStream;'||chr(10)||''||chr(10)||'import javax.xml.parsers.ParserConfigurationException;'||chr(10)||'import javax.xml.parsers.SAXParser;'||chr(10)||'import javax.xml.parsers.SAXParserFactory;'||chr(10)||'import org.xml.sax.XMLReader;'||chr(10)||'import org.xml.sax.helpers.DefaultHandler;'||chr(10)||''||chr(10)||'class ids17_CustomResolver implements EntityResolver {'||chr(10)||'  public InputSource resolveEntity(String publicId, String systemId)'||chr(10)||'      throws SAXException, IOException {'||chr(10)||'    // Check by whitelist good entity'||chr(10)||'    ....'||chr(10)||'  }'||chr(10)||'}'||chr(10)||' '||chr(10)||'public class ids17_example{'||chr(10)||'  private static void bad_receiveXMLStream(InputStream inStream,'||chr(10)||'                                           DefaultHandler defaultHandler)'||chr(10)||'      throws ParserConfigurationException, SAXException, IOException {'||chr(10)||'    SAXParserFactory factory = SAXParserFactory.newInstance();'||chr(10)||'    SAXParser saxParser = factory.newSAXParser();'||chr(10)||'    saxParser.parse(inStream, defaultHandler); // IDS17-J'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private static void bad_receiveXMLStream2(InputStream inStream,'||chr(10)||'                                            DefaultHandler defaultHandler,'||chr(10)||'                                            Boolean do_sanitize) '||chr(10)||'    throws ParserConfigurationException, SAXException, IOException {'||chr(10)||'    try {'||chr(10)||'      SAXParserFactory factory = SAXParserFactory.newInstance();'||chr(10)||'      SAXParser saxParser = factory.newSAXParser();'||chr(10)||''||chr(10)||'      XMLReader reader = saxParser.getXMLReader();'||chr(10)||'      reader.setContentHandler(defaultHandler);'||chr(10)||'      if(do_sanitize) {'||chr(10)||'        reader.setEntityResolver(new ids17_CustomResolver()); // sanitized'||chr(10)||'      }'||chr(10)||'      InputSource is = new InputSource(inStream);'||chr(10)||'      reader.parse(is);  // IDS17-J, not setEntityResolver if !do_sanitize'||chr(10)||'    } catch (java.net.MalformedURLException mue) {'||chr(10)||'      System.err.println("Malformed URL Exception: " + mue);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||' '||chr(10)||'  public static void main(String[] args) throws ParserConfigurationException,'||chr(10)||'    SAXException, IOException {'||chr(10)||'    //  invokes bad_receiveXMLStream and bad_receiveXMLStream2'||chr(10)||'    //  ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.IOException;'||chr(10)||'import org.xml.sax.EntityResolver;'||chr(10)||'import org.xml.sax.InputSource;'||chr(10)||'import org.xml.sax.SAXException;'||chr(10)||'import java.io.FileInputStream;'||chr(10)||'import java.io.InputStream;'||chr(10)||''||chr(10)||'import javax.xml.parsers.ParserConfigurationException;'||chr(10)||'import javax.xml.parsers.SAXParser;'||chr(10)||'import javax.xml.parsers.SAXParserFactory;'||chr(10)||'import org.xml.sax.XMLReader;'||chr(10)||'import org.xml.sax.helpers.DefaultHandler;'||chr(10)||''||chr(10)||'class ids17_CustomResolver implements EntityResolver {'||chr(10)||'  public InputSource resolveEntity(String publicId, String systemId)'||chr(10)||'      throws SAXException, IOException {'||chr(10)||'    // Check by whitelist good entity'||chr(10)||'    ....'||chr(10)||'  }'||chr(10)||'}'||chr(10)||' '||chr(10)||'public class ids17_example{'||chr(10)||'  private static void bad_receiveXMLStream(InputStream inStream,'||chr(10)||'                                           DefaultHandler defaultHandler)'||chr(10)||'      throws ParserConfigurationException, SAXException, IOException {'||chr(10)||'    SAXParserFactory factory = SAXParserFactory.newInstance();'||chr(10)||'    SAXParser saxParser = factory.newSAXParser();'||chr(10)||'    saxParser.parse(inStream, defaultHandler); // IDS17-J'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private static void bad_receiveXMLStream2(InputStream inStream,'||chr(10)||'                                            DefaultHandler defaultHandler,'||chr(10)||'                                            Boolean do_sanitize) '||chr(10)||'    throws ParserConfigurationException, SAXException, IOException {'||chr(10)||'    try {'||chr(10)||'      SAXParserFactory factory = SAXParserFactory.newInstance();'||chr(10)||'      SAXParser saxParser = factory.newSAXParser();'||chr(10)||''||chr(10)||'      XMLReader reader = saxParser.getXMLReader();'||chr(10)||'      reader.setContentHandler(defaultHandler);'||chr(10)||'      // always sanitize'||chr(10)||'      reader.setEntityResolver(new ids17_CustomResolver()); // sanitized'||chr(10)||'      '||chr(10)||'      InputSource is = new InputSource(inStream);'||chr(10)||'      reader.parse(is);  // no ids17 complaince issue '||chr(10)||'    } catch (java.net.MalformedURLException mue) {'||chr(10)||'      System.err.println("Malformed URL Exception: " + mue);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||' '||chr(10)||'  public static void main(String[] args) throws ParserConfigurationException,'||chr(10)||'    SAXException, IOException {'||chr(10)||'    //  invokes bad_receiveXMLStream and bad_receiveXMLStream2'||chr(10)||'    //  ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS17-J.detail', '#### 概要'||chr(10)||'应该适当地过滤定义了外部实体（通常由URI指定）的外部声明'||chr(10)||'#### 解释'||chr(10)||'应该过滤外部实体，或是通过白名单或是通过预注册，这样能控制XML攻击。未能这么做可能导致拒绝服务或数据泄漏。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.IOException;'||chr(10)||'import org.xml.sax.EntityResolver;'||chr(10)||'import org.xml.sax.InputSource;'||chr(10)||'import org.xml.sax.SAXException;'||chr(10)||'import java.io.FileInputStream;'||chr(10)||'import java.io.InputStream;'||chr(10)||''||chr(10)||'import javax.xml.parsers.ParserConfigurationException;'||chr(10)||'import javax.xml.parsers.SAXParser;'||chr(10)||'import javax.xml.parsers.SAXParserFactory;'||chr(10)||'import org.xml.sax.XMLReader;'||chr(10)||'import org.xml.sax.helpers.DefaultHandler;'||chr(10)||''||chr(10)||'class ids17_CustomResolver implements EntityResolver {'||chr(10)||'  public InputSource resolveEntity(String publicId, String systemId)'||chr(10)||'      throws SAXException, IOException {'||chr(10)||'    // Check by whitelist good entity'||chr(10)||'    ....'||chr(10)||'  }'||chr(10)||'}'||chr(10)||' '||chr(10)||'public class ids17_example{'||chr(10)||'  private static void bad_receiveXMLStream(InputStream inStream,'||chr(10)||'                                           DefaultHandler defaultHandler)'||chr(10)||'      throws ParserConfigurationException, SAXException, IOException {'||chr(10)||'    SAXParserFactory factory = SAXParserFactory.newInstance();'||chr(10)||'    SAXParser saxParser = factory.newSAXParser();'||chr(10)||'    saxParser.parse(inStream, defaultHandler); // IDS17-J'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private static void bad_receiveXMLStream2(InputStream inStream,'||chr(10)||'                                            DefaultHandler defaultHandler,'||chr(10)||'                                            Boolean do_sanitize) '||chr(10)||'    throws ParserConfigurationException, SAXException, IOException {'||chr(10)||'    try {'||chr(10)||'      SAXParserFactory factory = SAXParserFactory.newInstance();'||chr(10)||'      SAXParser saxParser = factory.newSAXParser();'||chr(10)||''||chr(10)||'      XMLReader reader = saxParser.getXMLReader();'||chr(10)||'      reader.setContentHandler(defaultHandler);'||chr(10)||'      if(do_sanitize) {'||chr(10)||'        reader.setEntityResolver(new ids17_CustomResolver()); // sanitized'||chr(10)||'      }'||chr(10)||'      InputSource is = new InputSource(inStream);'||chr(10)||'      reader.parse(is);  // IDS17-J, not setEntityResolver if !do_sanitize'||chr(10)||'    } catch (java.net.MalformedURLException mue) {'||chr(10)||'      System.err.println("Malformed URL Exception: " + mue);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||' '||chr(10)||'  public static void main(String[] args) throws ParserConfigurationException,'||chr(10)||'    SAXException, IOException {'||chr(10)||'    //  invokes bad_receiveXMLStream and bad_receiveXMLStream2'||chr(10)||'    //  ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.IOException;'||chr(10)||'import org.xml.sax.EntityResolver;'||chr(10)||'import org.xml.sax.InputSource;'||chr(10)||'import org.xml.sax.SAXException;'||chr(10)||'import java.io.FileInputStream;'||chr(10)||'import java.io.InputStream;'||chr(10)||''||chr(10)||'import javax.xml.parsers.ParserConfigurationException;'||chr(10)||'import javax.xml.parsers.SAXParser;'||chr(10)||'import javax.xml.parsers.SAXParserFactory;'||chr(10)||'import org.xml.sax.XMLReader;'||chr(10)||'import org.xml.sax.helpers.DefaultHandler;'||chr(10)||''||chr(10)||'class ids17_CustomResolver implements EntityResolver {'||chr(10)||'  public InputSource resolveEntity(String publicId, String systemId)'||chr(10)||'      throws SAXException, IOException {'||chr(10)||'    // Check by whitelist good entity'||chr(10)||'    ....'||chr(10)||'  }'||chr(10)||'}'||chr(10)||' '||chr(10)||'public class ids17_example{'||chr(10)||'  private static void bad_receiveXMLStream(InputStream inStream,'||chr(10)||'                                           DefaultHandler defaultHandler)'||chr(10)||'      throws ParserConfigurationException, SAXException, IOException {'||chr(10)||'    SAXParserFactory factory = SAXParserFactory.newInstance();'||chr(10)||'    SAXParser saxParser = factory.newSAXParser();'||chr(10)||'    saxParser.parse(inStream, defaultHandler); // IDS17-J'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private static void bad_receiveXMLStream2(InputStream inStream,'||chr(10)||'                                            DefaultHandler defaultHandler,'||chr(10)||'                                            Boolean do_sanitize) '||chr(10)||'    throws ParserConfigurationException, SAXException, IOException {'||chr(10)||'    try {'||chr(10)||'      SAXParserFactory factory = SAXParserFactory.newInstance();'||chr(10)||'      SAXParser saxParser = factory.newSAXParser();'||chr(10)||''||chr(10)||'      XMLReader reader = saxParser.getXMLReader();'||chr(10)||'      reader.setContentHandler(defaultHandler);'||chr(10)||'      // always sanitize'||chr(10)||'      reader.setEntityResolver(new ids17_CustomResolver()); // sanitized'||chr(10)||'      '||chr(10)||'      InputSource is = new InputSource(inStream);'||chr(10)||'      reader.parse(is);  // no ids17 complaince issue '||chr(10)||'    } catch (java.net.MalformedURLException mue) {'||chr(10)||'      System.err.println("Malformed URL Exception: " + mue);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||' '||chr(10)||'  public static void main(String[] args) throws ParserConfigurationException,'||chr(10)||'    SAXException, IOException {'||chr(10)||'    //  invokes bad_receiveXMLStream and bad_receiveXMLStream2'||chr(10)||'    //  ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS17-J.msg_template', 'In file ${se.filename}, line ${se.line}, the external entity string ${se.var} is not properly sanitized/normalized in function ${se.func}. The entity is created  in ${ss.filename} at line ${ss.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS17-J.msg_template', '在${se.filename}, 第${se.line}行, 函数${se.func}的外部实体字符串${se.var} 在函数 ${se.func} 中未经消毒/规范化. 此实休是在${ss.filename}, 第${ss.line}行被创建的', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS17-J.name', 'Improperly configured XML parser could cause XML external entity attack', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS17-J.name', '在XML输入里引用外部软件实体可能会导致XML攻击', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS17-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS17-J'),
 'BASIC','PRIORITY','8'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS17-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS17-J'),
 'STANDARD','OWASP','01'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS17-J'),
 'STANDARD','OWASP','04'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS17-J'),
 'STANDARD','OWASP','05')
ON CONFLICT DO NOTHING;

-- ------------------------
-- IDS51-J
-- ------------------------

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.IDS51-J.descripition', 'Sensitive input/output data should be properly normalized or sanitized and then validated/encoded accordingly', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS51-J.descripition', '应该适当地规范化/净化敏感输入/输出数据，并相应地对其进行验证/编码', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS51-J.detail', '#### Abstract'||chr(10)||'Sensitive input/output data should be properly normalized or sanitized and then validated/encoded accordingly.'||chr(10)||'#### Explanation'||chr(10)||'Input and output sanitization to another subsystem is important to keep integrity and safety of the entire system. Failure to do so may expose the system to injection attacks.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.ServletException;'||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.util.Base64;'||chr(10)||'public class j_ids51_0 {'||chr(10)||'  private Boolean doSanitize;'||chr(10)||'  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {'||chr(10)||'    String url = request.getParameter("URL");  // url is "tainted"'||chr(10)||'    badPost(url, response);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void badPost(String url, HttpServletResponse response) throws ServletException, IOException, SecurityException {'||chr(10)||'    String urlString = "<a href=" + url + "</a>";'||chr(10)||'    String replyMsg = "";'||chr(10)||'    if(doSanitize) {'||chr(10)||'      replyMsg = sanitize(replyMsg);'||chr(10)||'    } else {'||chr(10)||'      replyMsg = urlString;'||chr(10)||'    }'||chr(10)||'    response.getWriter().write(urlString); // IDS51'||chr(10)||'    response.getWriter().write(replyMsg);  // IDS51 if doSanitize == false'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public String sanitize(String msg) {'||chr(10)||'    return Base64.getUrlEncoder().encodeToString(msg.getBytes());'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.ServletException;'||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.util.Base64;'||chr(10)||'public class j_ids51_0 {'||chr(10)||'  private Boolean doSanitize;'||chr(10)||'  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {'||chr(10)||'    String url = request.getParameter("URL");  // url is "tainted"'||chr(10)||'    badPost(url, response);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void badPost(String url, HttpServletResponse response) throws ServletException, IOException, SecurityException {'||chr(10)||'    String urlString = "<a href=" + url + "</a>";'||chr(10)||'    String replyMsg = "";'||chr(10)||''||chr(10)||'    replyMsg = sanitize(replyMsg);'||chr(10)||'    string replyMsgUrl = sanitize(urlString);'||chr(10)||''||chr(10)||'    response.getWriter().write(urlString); '||chr(10)||'    response.getWriter().write(replyMsg);  '||chr(10)||'  }'||chr(10)||''||chr(10)||'  public String sanitize(String msg) {'||chr(10)||'    return Base64.getUrlEncoder().encodeToString(msg.getBytes());'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS51-J.detail', '#### 概要'||chr(10)||'应该适当地规范化/净化敏感输入/输出数据，并相应地对其进行验证/编码'||chr(10)||'#### 解释'||chr(10)||'到另一个子系统的输入和输出净化对于保持整个系统的完整性及安全性是很重要的。未能这么做可能会将系统暴露给注入攻击'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.ServletException;'||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.util.Base64;'||chr(10)||'public class j_ids51_0 {'||chr(10)||'  private Boolean doSanitize;'||chr(10)||'  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {'||chr(10)||'    String url = request.getParameter("URL");  // url is "tainted"'||chr(10)||'    badPost(url, response);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void badPost(String url, HttpServletResponse response) throws ServletException, IOException, SecurityException {'||chr(10)||'    String urlString = "<a href=" + url + "</a>";'||chr(10)||'    String replyMsg = "";'||chr(10)||'    if(doSanitize) {'||chr(10)||'      replyMsg = sanitize(replyMsg);'||chr(10)||'    } else {'||chr(10)||'      replyMsg = urlString;'||chr(10)||'    }'||chr(10)||'    response.getWriter().write(urlString); // IDS51'||chr(10)||'    response.getWriter().write(replyMsg);  // IDS51 if doSanitize == false'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public String sanitize(String msg) {'||chr(10)||'    return Base64.getUrlEncoder().encodeToString(msg.getBytes());'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````'||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.servlet.ServletException;'||chr(10)||'import javax.servlet.http.HttpServletRequest;'||chr(10)||'import javax.servlet.http.HttpServletResponse;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.util.Base64;'||chr(10)||'public class j_ids51_0 {'||chr(10)||'  private Boolean doSanitize;'||chr(10)||'  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {'||chr(10)||'    String url = request.getParameter("URL");  // url is "tainted"'||chr(10)||'    badPost(url, response);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void badPost(String url, HttpServletResponse response) throws ServletException, IOException, SecurityException {'||chr(10)||'    String urlString = "<a href=" + url + "</a>";'||chr(10)||'    String replyMsg = "";'||chr(10)||''||chr(10)||'    replyMsg = sanitize(replyMsg);'||chr(10)||'    string replyMsgUrl = sanitize(urlString);'||chr(10)||''||chr(10)||'    response.getWriter().write(urlString); '||chr(10)||'    response.getWriter().write(replyMsg);  '||chr(10)||'  }'||chr(10)||''||chr(10)||'  public String sanitize(String msg) {'||chr(10)||'    return Base64.getUrlEncoder().encodeToString(msg.getBytes());'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS51-J.msg_template', 'In file ${se.filename}, line ${se.line}, the ${se.var} is not properly sanitized in method ${se.func}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS51-J.msg_template', '在${se.filename}, 第${se.line}行, 字符串${se.var} 在函数${se.func}未经正确净化', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS51-J.name', 'Data input to substems should be properly sanitized or normalized accordingly', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS51-J.name', '应对输入到子系统的数据进行清理或标准化', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS51-J');

-- priority empty

-- ------------------------
-- IDS53-J
-- ------------------------

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.IDS53-J.description', 'Data supplied to a XPath retrieval must be sanitized', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS53-J.description', '提供给XPath检索的数据必须经过净化', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS53-J.detail', '#### Abstract'||chr(10)||'Data supplied to a XPath retrieval must be sanitized.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'XPath injection attack can occur when data supplied to an XPath retrieval routine is used without sanitisation.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.xml.xpath.*;'||chr(10)||'import org.xml.sax.SAXException;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.io.FileInputStream;'||chr(10)||'import javax.xml.parsers.DocumentBuilder;'||chr(10)||'import org.w3c.dom.Document;'||chr(10)||'import org.w3c.dom.NodeList;'||chr(10)||'public class j_ids53_0 {'||chr(10)||'  private final String accountFile = "account.xml";'||chr(10)||'  private DocumentBuilder docBuilder;'||chr(10)||'  private XPath xpath;'||chr(10)||''||chr(10)||'  // the code below trying to query account Info with given id'||chr(10)||'  public String queryAccountInfo(String id) throws XPathExpressionException, SAXException, IOException {'||chr(10)||'    Document accountDoc = docBuilder.parse(accountFile);'||chr(10)||'    String compileStr = "/Accounts/account[@id=''" + id + "'']";  // attacker can inject by provide id with Mike'' or ''1''=''1'||chr(10)||'    String res = xpath.evaluate(compileStr, accountDoc); // IDS53-J'||chr(10)||'    return res;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.xml.xpath.*;'||chr(10)||'import nux.xom.xquery.*;'||chr(10)||'import nux.xom.pool.XQueryFactory;'||chr(10)||'import nu.xom.*;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.io.File;'||chr(10)||'import java.util.Map;'||chr(10)||'import java.util.HashMap;'||chr(10)||'import javax.xml.parsers.DocumentBuilder;'||chr(10)||'public class j_ids53_fp_1 {'||chr(10)||'private final String accountFile = "account.xml";'||chr(10)||'private DocumentBuilder docBuilder;'||chr(10)||'private XPath xpath;'||chr(10)||''||chr(10)||'  // the code below trying to query account Info with given id and passwd'||chr(10)||'  public Nodes queryAccountInfo(String id, String passwd) throws ParsingException, XQueryException, IOException {'||chr(10)||'    Document accountDoc = new Builder().build(new File(accountFile));'||chr(10)||'    XQuery xquery = new XQueryFactory().createXQuery(new File("login.xq"));'||chr(10)||'    // always hash sensitive data (not in plain text)'||chr(10)||'    Map queryVars = new HashMap();'||chr(10)||'    queryVars.put("id", id);'||chr(10)||'    queryVars.put("password", passwd);'||chr(10)||'    Nodes nodes = xquery.execute(accountDoc, null, queryVars).toNodes();'||chr(10)||'    return nodes;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS53-J.detail', '#### 概要'||chr(10)||'提供给XPath检索的数据必须经过净化'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||''||chr(10)||'当数据而没有进行净化处理便提供给XPath检索例程，会容易受到XPath注入攻击'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'import javax.xml.xpath.*;'||chr(10)||'import org.xml.sax.SAXException;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.io.FileInputStream;'||chr(10)||'import javax.xml.parsers.DocumentBuilder;'||chr(10)||'import org.w3c.dom.Document;'||chr(10)||'import org.w3c.dom.NodeList;'||chr(10)||'public class j_ids53_0 {'||chr(10)||'  private final String accountFile = "account.xml";'||chr(10)||'  private DocumentBuilder docBuilder;'||chr(10)||'  private XPath xpath;'||chr(10)||''||chr(10)||'  // the code below trying to query account Info with given id'||chr(10)||'  public String queryAccountInfo(String id) throws XPathExpressionException, SAXException, IOException {'||chr(10)||'    Document accountDoc = docBuilder.parse(accountFile);'||chr(10)||'    String compileStr = "/Accounts/account[@id=''" + id + "'']";  // attacker can inject by provide id with Mike'' or ''1''=''1'||chr(10)||'    String res = xpath.evaluate(compileStr, accountDoc); // IDS53-J'||chr(10)||'    return res;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'import javax.xml.xpath.*;'||chr(10)||'import nux.xom.xquery.*;'||chr(10)||'import nux.xom.pool.XQueryFactory;'||chr(10)||'import nu.xom.*;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.io.File;'||chr(10)||'import java.util.Map;'||chr(10)||'import java.util.HashMap;'||chr(10)||'import javax.xml.parsers.DocumentBuilder;'||chr(10)||'public class j_ids53_fp_1 {'||chr(10)||'private final String accountFile = "account.xml";'||chr(10)||'private DocumentBuilder docBuilder;'||chr(10)||'private XPath xpath;'||chr(10)||''||chr(10)||'  // the code below trying to query account Info with given id and passwd'||chr(10)||'  public Nodes queryAccountInfo(String id, String passwd) throws ParsingException, XQueryException, IOException {'||chr(10)||'    Document accountDoc = new Builder().build(new File(accountFile));'||chr(10)||'    XQuery xquery = new XQueryFactory().createXQuery(new File("login.xq"));'||chr(10)||'    // always hash sensitive data (not in plain text)'||chr(10)||'    Map queryVars = new HashMap();'||chr(10)||'    queryVars.put("id", id);'||chr(10)||'    queryVars.put("password", passwd);'||chr(10)||'    Nodes nodes = xquery.execute(accountDoc, null, queryVars).toNodes();'||chr(10)||'    return nodes;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS53-J.msg_template', 'In ${se.filename}, line ${se.line}, the string ${se.var} is not sanitized before the xpath query ${se.func}. The string is originated at ${ss.filename}, line ${ss.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS53-J.msg_template', '在${se.filename}，第${se.line}行，字符串${se.var}在xpath查询 ${se.func}之前未经过净化. 這字符串源于${ss.filename}, 第${ss.line}行', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS53-J.name', 'Data supplied to a XPath retrieval must be sanitized', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS53-J.name', '提供给XPath检索的数据必须经过净化', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS53-J');

-- priority empty

-- ------------------------
-- IDS54-J
-- ------------------------

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.IDS54-J.description', 'Sanitize and validate data input to prevent LDAP injection results', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS54-J.description', '清理和验证数据输入以防止LDAP注入攻击', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS54-J.detail', '#### Abstract'||chr(10)||'Sanitize and validate data input to prevent LDAP injection results.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||''||chr(10)||'Lightweight Directory Access Procotol allows an application to remotely perform operations includinng user login, directory record modification. It is important to sanitize and validate the string to prevent LDAP injection attack.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'import javax.naming.NamingEnumeration;'||chr(10)||'import javax.naming.NamingException;'||chr(10)||'import javax.naming.directory.InitialDirContext;'||chr(10)||'import javax.naming.directory.SearchControls;'||chr(10)||'import javax.naming.directory.SearchResult;'||chr(10)||''||chr(10)||'public class j_ids54_0'||chr(10)||'{'||chr(10)||'  public NamingEnumeration<SearchResult> queryUserInfo(InitialDirContext ctx, String id) throws NamingException {'||chr(10)||'    SearchControls sc = new SearchControls();'||chr(10)||'    sc.setReturningAttributes(new String[]{"balance", "phone"});'||chr(10)||'    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);'||chr(10)||''||chr(10)||'    String searchBase = "dc=Users,dc=com";'||chr(10)||'    String filter = "(id=" + id + ")"; // attacker can inject by provide id with "*"'||chr(10)||'    return ctx.search(searchBase, filter, sc); // IDS54-J, query user info with id '||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'import javax.naming.NamingEnumeration;'||chr(10)||'import javax.naming.NamingException;'||chr(10)||'import javax.naming.directory.InitialDirContext;'||chr(10)||'import javax.naming.directory.SearchControls;'||chr(10)||'import javax.naming.directory.SearchResult;'||chr(10)||''||chr(10)||'public class j_ids54_0'||chr(10)||'{'||chr(10)||'  public NamingEnumeration<SearchResult> queryUserInfo(InitialDirContext ctx, String id) throws NamingException {'||chr(10)||'    SearchControls sc = new SearchControls();'||chr(10)||'    sc.setReturningAttributes(new String[]{"balance", "phone"});'||chr(10)||'    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);'||chr(10)||''||chr(10)||'    String searchBase = "dc=Users,dc=com";'||chr(10)||'    '||chr(10)||'    // add filter to contain only valid chaacters (different data type may require different filter)'||chr(10)||'    // the following code must be tailored accordingly'||chr(10)||'    if (is.matches"[\\w]*") {'||chr(10)||'        throw new IllegalArgumentException("Invalid input");'||chr(10)||'    }'||chr(10)||'    String filter = "(id=" + id + ")"; //  id with "*" is filtered'||chr(10)||'    '||chr(10)||'    return ctx.search(searchBase, filter, sc); '||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS54-J.detail', '#### 概要'||chr(10)||'清理和验证数据输入以防止LDAP注入攻击.'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||''||chr(10)||'轻量级目录访问协议允许应用程序远程执行操作，包括用户登录、目录记录修改等等。在查询访问之前清理和验证字符串以防止LDAP注入非常重要。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'import javax.naming.NamingEnumeration;'||chr(10)||'import javax.naming.NamingException;'||chr(10)||'import javax.naming.directory.InitialDirContext;'||chr(10)||'import javax.naming.directory.SearchControls;'||chr(10)||'import javax.naming.directory.SearchResult;'||chr(10)||''||chr(10)||'public class j_ids54_0'||chr(10)||'{'||chr(10)||'  public NamingEnumeration<SearchResult> queryUserInfo(InitialDirContext ctx, String id) throws NamingException {'||chr(10)||'    SearchControls sc = new SearchControls();'||chr(10)||'    sc.setReturningAttributes(new String[]{"balance", "phone"});'||chr(10)||'    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);'||chr(10)||''||chr(10)||'    String searchBase = "dc=Users,dc=com";'||chr(10)||'    String filter = "(id=" + id + ")"; // attacker can inject by provide id with "*"'||chr(10)||'    return ctx.search(searchBase, filter, sc); // IDS54-J, query user info with id '||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'import javax.naming.NamingEnumeration;'||chr(10)||'import javax.naming.NamingException;'||chr(10)||'import javax.naming.directory.InitialDirContext;'||chr(10)||'import javax.naming.directory.SearchControls;'||chr(10)||'import javax.naming.directory.SearchResult;'||chr(10)||''||chr(10)||'public class j_ids54_0'||chr(10)||'{'||chr(10)||'  public NamingEnumeration<SearchResult> queryUserInfo(InitialDirContext ctx, String id) throws NamingException {'||chr(10)||'    SearchControls sc = new SearchControls();'||chr(10)||'    sc.setReturningAttributes(new String[]{"balance", "phone"});'||chr(10)||'    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);'||chr(10)||''||chr(10)||'    String searchBase = "dc=Users,dc=com";'||chr(10)||'    '||chr(10)||'    // add filter to contain only valid chaacters (different data type may require different filter)'||chr(10)||'    // the following code must be tailored accordingly'||chr(10)||'    if (is.matches"[\\w]*") {'||chr(10)||'        throw new IllegalArgumentException("Invalid input");'||chr(10)||'    }'||chr(10)||'    String filter = "(id=" + id + ")"; //  id with "*" is filtered'||chr(10)||'    '||chr(10)||'    return ctx.search(searchBase, filter, sc); '||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS54-J.msg_template', 'In ${se.filename}, line ${se.line}, method ${ss.func}, the string ${se.var} is not sanitized nor validated. This query may lead to LDAP injection attack', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS54-J.msg_template', '在${se.filename}，第${se.line}行，函数 ${ss.func}裡的字符串${se.var}未经正确净化和验证. 该查询可能导致LDAP注入攻击', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.IDS54-J.name', 'Sanitize data to output subsystems to prevent LDAP injection attack', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.IDS54-J.name', '将输出子系统的数据净化, 以防止LDAP注入攻击', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='IDS54-J');
-- priority empty


-- ------------------------
-- JNI01-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'JNI01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/pages/viewpage.action?pageId=88487334', '${rule.Xcalibyte.CERT.1.JNI01-J.name}', null, 'JNI01-J', 1, 1, 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.JNI01-J.detail}', '${rule.Xcalibyte.CERT.1.JNI01-J.description}', '${rule.Xcalibyte.CERT.1.JNI01-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='JNI01-J'),
 'BASIC','PRIORITY','27'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='JNI01-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MET06-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MET06-J', null, 'MET06-J', 'java', 'https://wiki.sei.cmu.edu/confluence/pages/viewpage.action?pageId=88487921', '${rule.Xcalibyte.CERT.1.MET06-J.name}', 2, 1, 'PROBABLE', 'LOW', '${rule.Xcalibyte.CERT.1.MET06-J.detail}', '${rule.Xcalibyte.CERT.1.MET06-J.description}', '${rule.Xcalibyte.CERT.1.MET06-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MET06-J.description', 'The program has called overridable methods in clone()', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MET06-J.description', '该程序在clone()里调用了可覆盖的方法', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MET06-J.detail', '#### Abstract'||chr(10)||'The program has called overridable methods in clone().'||chr(10)||'#### Explanation'||chr(10)||'A malicious subclass could override the methods in clone() causing insecure or unpredictable behavior. Also, trusted subclasses could modify cloned objects which are in the construction process such that the object being cloned will be in an inconsistent state.'||chr(10)||''||chr(10)||'#### Example (extended from CERT site) - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.HttpCookie;'||chr(10)||'import java.util.ArrayList;'||chr(10)||'import java.util.List;'||chr(10)||''||chr(10)||'class j_met06 implements Cloneable {'||chr(10)||'  HttpCookie[] cookies;'||chr(10)||''||chr(10)||'  j_met06(HttpCookie[] c) {'||chr(10)||'    cookies = c;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public Object clone() throws CloneNotSupportedException {'||chr(10)||'    // get shallow copy of object'||chr(10)||'    final j_met06 clone = (j_met06) super.clone();'||chr(10)||'    clone.doSomething(); // Can invoke overridable method'||chr(10)||''||chr(10)||'    // the class has mutable object, need to deep copy those field'||chr(10)||'    clone.cookies = clone.deepCopy();'||chr(10)||'    return clone;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  void doSomething() { // Overridable method'||chr(10)||'    for (int i = 0; i < cookies.length; i++) {'||chr(10)||'      cookies[i].setValue("" + i);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  HttpCookie[] deepCopy() {'||chr(10)||'    if (cookies == null) {'||chr(10)||'      throw new NullPointerException();'||chr(10)||'    }'||chr(10)||''||chr(10)||'    // implements deep copy'||chr(10)||'    List<HttpCookie> cloned_cookie = new ArrayList<>();'||chr(10)||'    // ...'||chr(10)||'    return (HttpCookie[]) cloned_cookie;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'class j_met06_ext extends j_met06 {'||chr(10)||'  j_met06_ext(HttpCookie[] c) {'||chr(10)||'    super(c);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public Object clone() throws CloneNotSupportedException {'||chr(10)||'    final j_met06_ext clone = (j_met06_ext) super.clone();'||chr(10)||'    clone.doSomething();'||chr(10)||'    return clone;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  void doSomething() { // Erroneously executed'||chr(10)||'    for (int i = 0;i < cookies.length; i++) {'||chr(10)||'      cookies[i].setDomain(i + ".xxx.com");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args)'||chr(10)||'    throws CloneNotSupportedException {'||chr(10)||'    HttpCookie[] hc = new HttpCookie[20];'||chr(10)||'    for (int i = 0 ; i < hc.length; i++){'||chr(10)||'      hc[i] = new HttpCookie("cookie" + i,"" + i);'||chr(10)||'    }'||chr(10)||'    // shallow copy was invoked, the original object can be modified'||chr(10)||'    j_met06 badcookie = new j_met06_ext(hc);'||chr(10)||'    badcookie.clone();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.HttpCookie;'||chr(10)||'import java.util.ArrayList;'||chr(10)||'import java.util.List;'||chr(10)||''||chr(10)||'class j_met06 implements Cloneable {'||chr(10)||'  HttpCookie[] cookies;'||chr(10)||''||chr(10)||'  j_met06(HttpCookie[] c) {'||chr(10)||'    cookies = c;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public Object clone() throws CloneNotSupportedException {'||chr(10)||'    // get shallow copy of object'||chr(10)||'    final j_met06 clone = (j_met06) super.clone();'||chr(10)||'    clone.doSomething(); // Can invoke overridable method'||chr(10)||''||chr(10)||'    // the class has mutable object, need to deep copy those field'||chr(10)||'    clone.cookies = clone.deepCopy();'||chr(10)||'    return clone;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  final void doSomething() { // not overridable method'||chr(10)||'    for (int i = 0; i < cookies.length; i++) {'||chr(10)||'      cookies[i].setValue("" + i);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  final HttpCookie[] deepCopy() { // not overridable method'||chr(10)||'    if (cookies == null) {'||chr(10)||'      throw new NullPointerException();'||chr(10)||'    }'||chr(10)||''||chr(10)||'    // implements deep copy'||chr(10)||'    List<HttpCookie> cloned_cookie = new ArrayList<>();'||chr(10)||'    // ...'||chr(10)||'    return (HttpCookie[]) cloned_cookie;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'class j_met06_ext extends j_met06 {'||chr(10)||'  j_met06_ext(HttpCookie[] c) {'||chr(10)||'    super(c);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public Object clone() throws CloneNotSupportedException {'||chr(10)||'    final j_met06_ext clone = (j_met06_ext) super.clone();'||chr(10)||'    clone.doSomething();'||chr(10)||'    return clone;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  void doSomething() { // will not be called'||chr(10)||'    for (int i = 0;i < cookies.length; i++) {'||chr(10)||'      cookies[i].setDomain(i + ".xxx.com");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args)'||chr(10)||'    throws CloneNotSupportedException {'||chr(10)||'    HttpCookie[] hc = new HttpCookie[20];'||chr(10)||'    for (int i = 0 ; i < hc.length; i++){'||chr(10)||'      hc[i] = new HttpCookie("cookie" + i,"" + i);'||chr(10)||'    }'||chr(10)||'    // deep copy was invoked, the original object cannot be modified'||chr(10)||'    j_met06 badcookie = new j_met06_ext(hc);'||chr(10)||'    badcookie.clone();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MET06-J.detail', '#### 概要'||chr(10)||'该程序在clone()里调用了可覆盖的方法'||chr(10)||'#### 解释'||chr(10)||'恶意的子类可能会重写clone()里的方法，造成不安全或不可预测的行为。另外，受信任的子类可以修改处于构造过程的被复制的对象，这样被复制的对象将处于不一致状态。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||'import java.net.HttpCookie;'||chr(10)||'import java.util.ArrayList;'||chr(10)||'import java.util.List;'||chr(10)||''||chr(10)||'class j_met06 implements Cloneable {'||chr(10)||'  HttpCookie[] cookies;'||chr(10)||''||chr(10)||'  j_met06(HttpCookie[] c) {'||chr(10)||'    cookies = c;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public Object clone() throws CloneNotSupportedException {'||chr(10)||'    // get shallow copy of object'||chr(10)||'    final j_met06 clone = (j_met06) super.clone();'||chr(10)||'    clone.doSomething(); // can invoke overridable method'||chr(10)||''||chr(10)||'    // the class has mutable object, need to deep copy those field'||chr(10)||'    clone.cookies = clone.deepCopy();'||chr(10)||'    return clone;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  void doSomething() { // Overridable method should be declared final'||chr(10)||'    for (int i = 0; i < cookies.length; i++) {'||chr(10)||'      cookies[i].setValue("" + i);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  HttpCookie[] deepCopy() {'||chr(10)||'    if (cookies == null) {'||chr(10)||'      throw new NullPointerException();'||chr(10)||'    }'||chr(10)||''||chr(10)||'    // implements deep copy'||chr(10)||'    List<HttpCookie> cloned_cookie = new ArrayList<>();'||chr(10)||'    // ...'||chr(10)||'    return (HttpCookie[]) cloned_cookie;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'class j_met06_ext extends j_met06 {'||chr(10)||'  j_met06_ext(HttpCookie[] c) {'||chr(10)||'    super(c);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public Object clone() throws CloneNotSupportedException {'||chr(10)||'    final j_met06_ext clone = (j_met06_ext) super.clone();'||chr(10)||'    clone.doSomething();'||chr(10)||'    return clone;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  void doSomething() { // Erroneously executed'||chr(10)||'    for (int i = 0;i < cookies.length; i++) {'||chr(10)||'      cookies[i].setDomain(i + ".xxx.com");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args)'||chr(10)||'    throws CloneNotSupportedException {'||chr(10)||'    HttpCookie[] hc = new HttpCookie[20];'||chr(10)||'    for (int i = 0 ; i < hc.length; i++){'||chr(10)||'      hc[i] = new HttpCookie("cookie" + i,"" + i);'||chr(10)||'    }'||chr(10)||'    // shallow copy was invoked, the original object can be modified'||chr(10)||'    j_met06 badcookie = new j_met06_ext(hc);'||chr(10)||'    badcookie.clone();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````'||chr(10)||'text'||chr(10)||''||chr(10)||'import java.net.HttpCookie;'||chr(10)||'import java.util.ArrayList;'||chr(10)||'import java.util.List;'||chr(10)||''||chr(10)||'class j_met06 implements Cloneable {'||chr(10)||'  HttpCookie[] cookies;'||chr(10)||''||chr(10)||'  j_met06(HttpCookie[] c) {'||chr(10)||'    cookies = c;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public Object clone() throws CloneNotSupportedException {'||chr(10)||'    // get shallow copy of object'||chr(10)||'    final j_met06 clone = (j_met06) super.clone();'||chr(10)||'    clone.doSomething(); // invoke non-overridable method'||chr(10)||''||chr(10)||'    // the class has mutable object, need to deep copy those field'||chr(10)||'    clone.cookies = clone.deepCopy();'||chr(10)||'    return clone;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  final void doSomething() { // not overridable method'||chr(10)||'    for (int i = 0; i < cookies.length; i++) {'||chr(10)||'      cookies[i].setValue("" + i);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  final HttpCookie[] deepCopy() { // not overridable method'||chr(10)||'    if (cookies == null) {'||chr(10)||'      throw new NullPointerException();'||chr(10)||'    }'||chr(10)||''||chr(10)||'    // implements deep copy'||chr(10)||'    List<HttpCookie> cloned_cookie = new ArrayList<>();'||chr(10)||'    // ...'||chr(10)||'    return (HttpCookie[]) cloned_cookie;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'class j_met06_ext extends j_met06 {'||chr(10)||'  j_met06_ext(HttpCookie[] c) {'||chr(10)||'    super(c);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public Object clone() throws CloneNotSupportedException {'||chr(10)||'    final j_met06_ext clone = (j_met06_ext) super.clone();'||chr(10)||'    clone.doSomething();'||chr(10)||'    return clone;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  void doSomething() { // will not be invoked'||chr(10)||'    for (int i = 0;i < cookies.length; i++) {'||chr(10)||'      cookies[i].setDomain(i + ".xxx.com");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args)'||chr(10)||'    throws CloneNotSupportedException {'||chr(10)||'    HttpCookie[] hc = new HttpCookie[20];'||chr(10)||'    for (int i = 0 ; i < hc.length; i++){'||chr(10)||'      hc[i] = new HttpCookie("cookie" + i,"" + i);'||chr(10)||'    }'||chr(10)||'    // deep copy was invoked, the original object cannot be modified'||chr(10)||'    j_met06 badcookie = new j_met06_ext(hc);'||chr(10)||'    badcookie.clone();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MET06-J.msg_template', 'In file ${ss.filename}, line ${ss.line}, the overridable method ${ss.var}, has been overridden by clone() in ${se.filename} at line {$se.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MET06-J.msg_template', '在${ss.filename}, 第${ss.line}行,可覆盖函数${ss.var}会被 ${se.filename}, 第${se.line}行的clone()覆盖', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MET06-J.name', 'Do not called overridable methods in clone()', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MET06-J.name', '不要在clone()中调用可覆盖函数', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MET06-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MET06-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MET06-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MET06-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MET06-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MET06-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MET06-J'),
 'STANDARD','OWASP','08')
ON CONFLICT DO NOTHING;

-- ------------------------
-- MSC02-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC02-J', null, 'MSC02-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/MSC02-J.+Generate+strong+random+numbers', '${rule.Xcalibyte.CERT.1.MSC02-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.MSC02-J.detail}', '${rule.Xcalibyte.CERT.1.MSC02-J.description}', '${rule.Xcalibyte.CERT.1.MSC02-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MSC02-J.description', 'The program has used random number generators (PRNG) that are not strong', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC02-J.description', '该程序使用了不强大的随机数生成器（PRNG）', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC02-J.detail', '#### Abstract'||chr(10)||'The program has used random number generators (PRNG) that are not strong.'||chr(10)||'#### Explanation'||chr(10)||'Java API provides a PRNG in java.util.Random class which generates the same sequence when the same seed is used. For security sensitive programs, a more secure PRNG such as java.security.SecureRandom class should be used.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'// The Random() generates identical sequences in all three cases'||chr(10)||'import java.util.Random;'||chr(10)||'import java.lang.Math;'||chr(10)||''||chr(10)||'public class msc02_0 {'||chr(10)||''||chr(10)||'  public void foo0() {'||chr(10)||'    Random number = new Random(123L);'||chr(10)||'    for (int i = 0; i < 20; i++) {'||chr(10)||'      int n = number.nextInt();'||chr(10)||'      System.out.println(n);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void foo1() {'||chr(10)||'    Random number = new Random();'||chr(10)||'    System.out.println(number.nextLong());'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'// The Random() generates identical sequences in following cases'||chr(10)||'import java.util.Random;'||chr(10)||'import java.lang.Math;'||chr(10)||''||chr(10)||'public class msc02_0 {'||chr(10)||''||chr(10)||'  public void foo0() {'||chr(10)||'    Random number = new SecureRandom();  // use SecureRandom class '||chr(10)||'    for (int i = 0; i < 20; i++) {'||chr(10)||'      int n = number.nextInt();'||chr(10)||'      System.out.println(n);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void foo1() {'||chr(10)||'    Random number = new SecureRandom.getInstanceStrong();  // use strong algo '||chr(10)||'    System.out.println(number.nextLong());'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC02-J.detail', '#### 概要'||chr(10)||'该程序使用了不强大的随机数生成器（PRNG）'||chr(10)||'#### 解释'||chr(10)||'Java API在java.util.Random类里提供PRNG，当使用了相同种子时它会生成相同的序列。对于安全敏感型程序，应该使用像java.security.SecureRandom类这样的更安全的PRNG'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'// The Random() generates identical sequences in all three cases'||chr(10)||'import java.util.Random;'||chr(10)||'import java.lang.Math;'||chr(10)||''||chr(10)||'public class msc02_0 {'||chr(10)||''||chr(10)||'  public void foo0() {'||chr(10)||'    Random number = new Random(123L);'||chr(10)||'    for (int i = 0; i < 20; i++) {'||chr(10)||'      int n = number.nextInt();'||chr(10)||'      System.out.println(n);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void foo1() {'||chr(10)||'    Random number = new Random();'||chr(10)||'    System.out.println(number.nextLong());'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'// The Random() generates identical sequences in all cases'||chr(10)||'import java.util.Random;'||chr(10)||'import java.lang.Math;'||chr(10)||''||chr(10)||'public class msc02_0 {'||chr(10)||''||chr(10)||'  public void foo0() {'||chr(10)||'    Random number = new SecureRandom(); // user strong random class'||chr(10)||'    for (int i = 0; i < 20; i++) {'||chr(10)||'      int n = number.nextInt();'||chr(10)||'      System.out.println(n);'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void foo1() {'||chr(10)||'    Random number = new SecureRandom.getInstanceStrong();  // use strong algo'||chr(10)||'    System.out.println(number.nextLong());'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC02-J.msg_template', 'In file ${se.filename}, line ${se.line}, the method ${se.var} in function ${se.func} do not generate strong random numbers', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC02-J.msg_template', '在${se.filename}, 第${se.line}行,函数${se.var} in ${se.func} 不会产生强随机数', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC02-J.name', 'Use strong random number generators', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC02-J.name', '使用安全的随机数生成器', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC02-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC02-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC02-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC02-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC02-J'),
 'STANDARD','OWASP','05')
ON CONFLICT DO NOTHING;

-- ------------------------
-- MSC03-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC03-J', null, 'MSC03-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/MSC03-J.+Never+hard+code+sensitive+information', '${rule.Xcalibyte.CERT.1.MSC03-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.MSC03-J.detail}', '${rule.Xcalibyte.CERT.1.MSC03-J.description}', '${rule.Xcalibyte.CERT.1.MSC03-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MSC03-J.description', 'The program has hard coded sensitive information', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC03-J.description', '该程序硬编码了敏感信息', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC03-J.detail', '#### Abstract'||chr(10)||'The program has hard coded sensitive information.'||chr(10)||'#### Explanation'||chr(10)||'Security and privacy sensitive information should be retrieved at execution time from a secured file. Java executables are in the form of byte code and could be easily reverted back to source code form with the information in plain text.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.InetAddress;'||chr(10)||'import java.net.UnknownHostException;'||chr(10)||'import java.sql.Connection;'||chr(10)||'import java.sql.DriverManager;'||chr(10)||'import java.sql.SQLException;'||chr(10)||''||chr(10)||'public class msc03_0 {'||chr(10)||'  public final Connection getConnection() throws SQLException, UnknownHostException {'||chr(10)||'  '||chr(10)||'    // hard coded IP address in String'||chr(10)||'    String ipAddress = new String("184.15.254.1");'||chr(10)||'    if (InetAddress.getByName(ipAddress).isAnyLocalAddress()){'||chr(10)||'      // hard coded username and password'||chr(10)||'      return DriverManager.getConnection("dbhost:mysql://localhost/mydb", "7f4j9vj", "xsi9j2nn8");'||chr(10)||'    }'||chr(10)||'    return DriverManager.getConnection("dbhost:mysql://localhost/dbName", "username", "password");'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.InetAddress;'||chr(10)||'import java.net.UnknownHostException;'||chr(10)||'import java.sql.Connection;'||chr(10)||'import java.sql.DriverManager;'||chr(10)||'import java.sql.SQLException;'||chr(10)||''||chr(10)||'public class msc03_0 {'||chr(10)||'  public final Connection getConnection() throws SQLException, UnknownHostException {'||chr(10)||'  '||chr(10)||'    // IP address class to fill in at runtime. The address should be cleared immediately after use'||chr(10)||'    class IPAddress {'||chr(10)||'        // IP address in string'||chr(10)||'        char[] ipAddress = new char[128];'||chr(10)||'        try {'||chr(10)||'          // read in from some secure channel or file'||chr(10)||'          ...'||chr(10)||'        }'||chr(10)||'        finally {'||chr(10)||'          // clear after use'||chr(10)||'          Arrys.fill(ipAddress, (byte)0);'||chr(10)||'          // close file or channel'||chr(10)||'          ...'||chr(10)||'        }'||chr(10)||'        '||chr(10)||'    }'||chr(10)||'    if (InetAddress.getByName(ipAddress).isAnyLocalAddress()){'||chr(10)||'      // hard coded username and password'||chr(10)||'      String username, password;'||chr(10)||'      // Username and password should be read from a secure config file/channel at rungime'||chr(10)||'      return DriverManager.getConnection("dbhost:mysql://localhost/mydb", username, password);'||chr(10)||'    }'||chr(10)||'    return DriverManager.getConnection("dbhost:mysql://localhost/dbName", "username", "password");'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC03-J.detail', '#### 概要'||chr(10)||'该程序硬编码了敏感信息'||chr(10)||'#### 解释'||chr(10)||'安全和隐私敏感型信息应该在执行时从安全的文件里取回。Java可执行文件是字节码的形式，它能轻易地转换回有纯文本形式信息的源代码形式。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.InetAddress;'||chr(10)||'import java.net.UnknownHostException;'||chr(10)||'import java.sql.Connection;'||chr(10)||'import java.sql.DriverManager;'||chr(10)||'import java.sql.SQLException;'||chr(10)||''||chr(10)||'public class msc03_0 {'||chr(10)||'  public final Connection getConnection() throws SQLException, UnknownHostException {'||chr(10)||'  '||chr(10)||'    // hard coded IP address in String'||chr(10)||'    String ipAddress = new String("184.15.254.1");'||chr(10)||'    if (InetAddress.getByName(ipAddress).isAnyLocalAddress()){'||chr(10)||'      // hard coded username and password'||chr(10)||'      return DriverManager.getConnection("dbhost:mysql://localhost/mydb", "7f4j9vj", "xsi9j2nn8");'||chr(10)||'    }'||chr(10)||'    return DriverManager.getConnection("dbhost:mysql://localhost/dbName", "username", "password");'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.InetAddress;'||chr(10)||'import java.net.UnknownHostException;'||chr(10)||'import java.sql.Connection;'||chr(10)||'import java.sql.DriverManager;'||chr(10)||'import java.sql.SQLException;'||chr(10)||''||chr(10)||'public class msc03_0 {'||chr(10)||'  public final Connection getConnection() throws SQLException, UnknownHostException {'||chr(10)||'  '||chr(10)||'    // IP address class to fill in at runtime. The address should be cleared immediately after use'||chr(10)||'    class IPAddress {'||chr(10)||'        // IP address in string'||chr(10)||'        char[] ipAddress = new char[128];'||chr(10)||'        try {'||chr(10)||'          // read in from some secure channel or file'||chr(10)||'          ...'||chr(10)||'        }'||chr(10)||'        finally {'||chr(10)||'          // clear after use'||chr(10)||'          Arrys.fill(ipAddress, (byte)0);'||chr(10)||'          // close file or channel'||chr(10)||'          ...'||chr(10)||'        }'||chr(10)||'        '||chr(10)||'    }'||chr(10)||'    if (InetAddress.getByName(ipAddress).isAnyLocalAddress()){'||chr(10)||'      // hard coded username and password'||chr(10)||'      String username, password;'||chr(10)||'      // Username and password should be read from a secure config file/channel at rungime'||chr(10)||'      return DriverManager.getConnection("dbhost:mysql://localhost/mydb", username, password);'||chr(10)||'    }'||chr(10)||'    return DriverManager.getConnection("dbhost:mysql://localhost/dbName", "username", "password");'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC03-J.msg_template', 'In file ${se.filename}, line ${se.line}, ${se.var} in function ${se.func} has hard coded data and may contain sensitive information', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC03-J.msg_template', '在${se.filename}, 第${se.line}行,函数 ${se.func} 的变量${se.var} 有硬编码的敏感信息', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC03-J.name', 'Do not hard code sensitive information in program', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC03-J.name', '不要把敏感信息留在上线源代码或产品可执行文件里', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC03-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC03-J'),
 'BASIC','PRIORITY','12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC03-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC03-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC03-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC03-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC03-J'),
 'STANDARD','OWASP','06')
ON CONFLICT DO NOTHING;

-- ------------------------
-- MSC61-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'MSC61-J', null, 'MSC61-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/MSC61-J.+Do+not+use+insecure+or+weak+cryptographic+algorithms', '${rule.Xcalibyte.CERT.1.MSC61-J.name}', 2, 3, 'PROBABLE', 'HIGH', '${rule.Xcalibyte.CERT.1.MSC61-J.detail}', '${rule.Xcalibyte.CERT.1.MSC61-J.description}', '${rule.Xcalibyte.CERT.1.MSC61-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MSC61-J.description', 'The program is using weak cryptographic algorithms for security sensitive code', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC61-J.description', '该程序正为安全敏感型代码使用弱加密算法', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC61-J.detail', '#### Abstract'||chr(10)||'The program is using weak cryptographic algorithms for security sensitive code.'||chr(10)||'#### Explanation'||chr(10)||'Security and privacy sensitive information should be using strong encryption algorithms. Strong algorithms may include AES with Galois/Counter Mode (GCM) and AES with Cipher Block Chaining mode.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.crypto.*;'||chr(10)||'import java.io.UnsupportedEncodingException;'||chr(10)||'import java.security.InvalidKeyException;'||chr(10)||'import java.security.NoSuchAlgorithmException;'||chr(10)||''||chr(10)||'public class j_msc61_0 {'||chr(10)||'  public static byteArray[] encryption(String strToBeEncrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {'||chr(10)||'    '||chr(10)||'    // DES is a weak encryption algorithm'||chr(10)||'    // AES is also weak, however, if AES is to be used'||chr(10)||'    // do GCM (Galois/Counter Mode) to do the encryption'||chr(10)||'    SecretKey key = KeyGenerator.getInstance("DES").generateKey();'||chr(10)||'    Cipher cipher = Cipher.getInstance("DES");'||chr(10)||'    cipher.init(Cipher.ENCRYPT_MODE, key);'||chr(10)||''||chr(10)||'    // Encode bytes as UTF8; strToBeEncrypted contains'||chr(10)||'    // the input string that is to be encrypted'||chr(10)||'    byteArray[] encoded = strToBeEncrypted.getBytes("UTF8");'||chr(10)||''||chr(10)||'    // Perform encryption'||chr(10)||'    byteArray[] encrypted = cipher.doFinal(encoded);'||chr(10)||'    return encrypted;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.crypto.*;'||chr(10)||'import java.io.UnsupportedEncodingException;'||chr(10)||'import java.security.InvalidKeyException;'||chr(10)||'import java.security.NoSuchAlgorithmException;'||chr(10)||''||chr(10)||'public class j_msc61_0 {'||chr(10)||'    '||chr(10)||'  public static SecretKey genKey() {'||chr(10)||'    // DES is a weak encryption algorithm'||chr(10)||'    // AES is also weak, however, if AES is to be used'||chr(10)||'    // do GCM (Galois/Counter Mode) to do the encryption'||chr(10)||'    try {'||chr(10)||'      KeyGenerator keygen = KeyGenerator.getInstance("AES");'||chr(10)||'      keygen.init(128);'||chr(10)||'      return keygen.generateKey();'||chr(10)||'    } catch (NoSuchAlgorithmException e) {'||chr(10)||'        // handle exception'||chr(10)||'        ...'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'    '||chr(10)||'  public static byteArray[] encryption(String strToBeEncrypted, SecretKey seckey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {'||chr(10)||'    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");'||chr(10)||'    // please consult example in CERT-J site for MSC61-J'||chr(10)||'    ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC61-J.detail', '#### 概要'||chr(10)||'该程序正为安全敏感型代码使用弱加密算法'||chr(10)||'#### 解释'||chr(10)||'安全和隐私敏感型信息应使用强大的加密算法。强大的算法可包括有Galois/Counter模式（GCM）的AES和有加密块链模式的AES。'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.crypto.*;'||chr(10)||'import java.io.UnsupportedEncodingException;'||chr(10)||'import java.security.InvalidKeyException;'||chr(10)||'import java.security.NoSuchAlgorithmException;'||chr(10)||''||chr(10)||'public class j_msc61_0 {'||chr(10)||'  public static byteArray[] encryption(String strToBeEncrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {'||chr(10)||'    '||chr(10)||'    // DES is a weak encryption algorithm'||chr(10)||'    // AES is also weak, however, if AES is to be used'||chr(10)||'    // do GCM (Galois/Counter Mode) to do the encryption'||chr(10)||'    SecretKey key = KeyGenerator.getInstance("DES").generateKey();'||chr(10)||'    Cipher cipher = Cipher.getInstance("DES");'||chr(10)||'    cipher.init(Cipher.ENCRYPT_MODE, key);'||chr(10)||''||chr(10)||'    // Encode bytes as UTF8; strToBeEncrypted contains'||chr(10)||'    // the input string that is to be encrypted'||chr(10)||'    byteArray[] encoded = strToBeEncrypted.getBytes("UTF8");'||chr(10)||''||chr(10)||'    // Perform encryption'||chr(10)||'    byteArray[] encrypted = cipher.doFinal(encoded);'||chr(10)||'    return encrypted;'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import javax.crypto.*;'||chr(10)||'import java.io.UnsupportedEncodingException;'||chr(10)||'import java.security.InvalidKeyException;'||chr(10)||'import java.security.NoSuchAlgorithmException;'||chr(10)||''||chr(10)||'public class j_msc61_0 {'||chr(10)||'    '||chr(10)||'  public static SecretKey genKey() {'||chr(10)||'    // DES is a weak encryption algorithm'||chr(10)||'    // AES is also weak, however, if AES is to be used'||chr(10)||'    // do GCM (Galois/Counter Mode) to do the encryption'||chr(10)||'    try {'||chr(10)||'      KeyGenerator keygen = KeyGenerator.getInstance("AES");'||chr(10)||'      keygen.init(128);'||chr(10)||'      return keygen.generateKey();'||chr(10)||'    } catch (NoSuchAlgorithmException e) {'||chr(10)||'        // handle exception'||chr(10)||'        ...'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'    '||chr(10)||'  public static byteArray[] encryption(String strToBeEncrypted, SecretKey seckey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {'||chr(10)||'    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");'||chr(10)||'    // please consult example in CERT-J site for MSC61-J'||chr(10)||'    ...'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC61-J.msg_template', 'In file ${se.filename}, line ${se.line}, the encryption method ${se.var} in function ${se.func} used is generally weak and insecure', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC61-J.msg_template', '在${se.filename}, 第${se.line}行,函数 ${se.func} 加密算法用${se.var}是比较弱和不安全的', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC61-J.name', 'Security critical code must avoid using insecure or weak crypto algorithms', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC61-J.name', '安全关键型代码必须避免使用不安全或弱加密算法', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC61-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC61-J'),
 'BASIC','PRIORITY','4'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC61-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC61-J'),
 'STANDARD','OWASP','09')
ON CONFLICT DO NOTHING;

-- ------------------------
-- MSC62-J
-- ------------------------

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.MSC62-J.description', 'The program is storing passwords in unencrypted or insecured hash form', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC62-J.description', '该程序正存储未加密形式的密码', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC62-J.detail', '#### Abstract'||chr(10)||'The program is storing passwords in unencrypted or insecured hash form.'||chr(10)||'#### Explanation'||chr(10)||'Passwords should not be stored as plain text. They should be encrypted, with acceptable algorithms including using hash functions (computation complexity is less than typical encryption algorithms).'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.security.MessageDigest;'||chr(10)||'import java.security.NoSuchAlgorithmException;'||chr(10)||'import java.security.SecureRandom;'||chr(10)||''||chr(10)||'public class j_msc62_ex {'||chr(10)||'  private void regUser1(String userName, byte[] passwd, String regType) {'||chr(10)||'    try {'||chr(10)||'      String salt = genSalt();'||chr(10)||'      String combPasswd = salt + passwd;'||chr(10)||'      byte[] secureHash = secureHash(combPasswd.getBytes());'||chr(10)||'      if(regType.equals("NO_SALT_HASH")) {'||chr(10)||'        saveUser(userName, passwd);             // MSC62-J, no salt, no hash'||chr(10)||'      } else {'||chr(10)||'        saveUser(userName, secureHash);         // good'||chr(10)||'      }'||chr(10)||'    } catch (NoSuchAlgorithmException e) {'||chr(10)||'      e.printStackTrace();'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void saveUser(String userName, byte[] passwd) {'||chr(10)||'    // store the user info'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private String genSalt()'||chr(10)||'  {'||chr(10)||'    SecureRandom rand = new SecureRandom();'||chr(10)||'    byte[] salt = new byte[16];'||chr(10)||'    rand.nextBytes(salt);   // salt used'||chr(10)||'    return new String(salt);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private byte[] secureHash(byte [] passwd) throws NoSuchAlgorithmException{'||chr(10)||'    MessageDigest msgDigest = MessageDigest.getInstance("SHA256");  // secure hash algorithm'||chr(10)||'    return msgDigest.digest(passwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void testRegUser(String userName, String passwd, String regType) {'||chr(10)||'    regUser1(userName, passwd.getBytes(), regType);'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.security.MessageDigest;'||chr(10)||'import java.security.NoSuchAlgorithmException;'||chr(10)||'import java.security.SecureRandom;'||chr(10)||''||chr(10)||'public class j_msc62_ex {'||chr(10)||'  private void regUser1(String userName, byte[] passwd, String regType) {'||chr(10)||'    try {'||chr(10)||'      String salt = genSalt();'||chr(10)||'      String combPasswd = salt + passwd;'||chr(10)||'      byte[] secureHash = secureHash(combPasswd.getBytes());'||chr(10)||'        // always use salt'||chr(10)||'        saveUser(userName, secureHash);         // salt and secure hash used'||chr(10)||'      }'||chr(10)||'    } catch (NoSuchAlgorithmException e) {'||chr(10)||'      e.printStackTrace();'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void saveUser(String userName, byte[] passwd) {'||chr(10)||'    // store the user info'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private String genSalt()'||chr(10)||'  {'||chr(10)||'    SecureRandom rand = new SecureRandom();'||chr(10)||'    byte[] salt = new byte[16];'||chr(10)||'    rand.nextBytes(salt);   // salt used'||chr(10)||'    return new String(salt);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private byte[] secureHash(byte [] passwd) throws NoSuchAlgorithmException{'||chr(10)||'    MessageDigest msgDigest = MessageDigest.getInstance("SHA256");  // secure hash algorithm'||chr(10)||'    return msgDigest.digest(passwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void testRegUser(String userName, String passwd, String regType) {'||chr(10)||'    regUser1(userName, passwd.getBytes(), regType);'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC62-J.detail', '#### 概要'||chr(10)||'该程序正存储未加密形式的密码'||chr(10)||'#### 解释'||chr(10)||'不应将密码以纯文本形式存储。应该用认可的算法将其加密，包括使用hash函数（计算复杂度要少于一般的加密算法）'||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'import java.security.MessageDigest;'||chr(10)||'import java.security.NoSuchAlgorithmException;'||chr(10)||'import java.security.SecureRandom;'||chr(10)||''||chr(10)||'public class j_msc62_ex {'||chr(10)||'  private void regUser1(String userName, byte[] passwd, String regType) {'||chr(10)||'    try {'||chr(10)||'      String salt = genSalt();'||chr(10)||'      String combPasswd = salt + passwd;'||chr(10)||'      byte[] secureHash = secureHash(combPasswd.getBytes());'||chr(10)||'      if(regType.equals("NO_SALT_HASH")) {'||chr(10)||'        saveUser(userName, passwd);             // MSC62-J, no salt, no hash'||chr(10)||'      } else {'||chr(10)||'        saveUser(userName, secureHash);         // good'||chr(10)||'      }'||chr(10)||'    } catch (NoSuchAlgorithmException e) {'||chr(10)||'      e.printStackTrace();'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void saveUser(String userName, byte[] passwd) {'||chr(10)||'    // store the user info'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private String genSalt()'||chr(10)||'  {'||chr(10)||'    SecureRandom rand = new SecureRandom();'||chr(10)||'    byte[] salt = new byte[16];'||chr(10)||'    rand.nextBytes(salt);   // salt used'||chr(10)||'    return new String(salt);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private byte[] secureHash(byte [] passwd) throws NoSuchAlgorithmException{'||chr(10)||'    MessageDigest msgDigest = MessageDigest.getInstance("SHA256");  // secure hash algorithm'||chr(10)||'    return msgDigest.digest(passwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void testRegUser(String userName, String passwd, String regType) {'||chr(10)||'    regUser1(userName, passwd.getBytes(), regType);'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.security.MessageDigest;'||chr(10)||'import java.security.NoSuchAlgorithmException;'||chr(10)||'import java.security.SecureRandom;'||chr(10)||''||chr(10)||'public class j_msc62_ex {'||chr(10)||'  private void regUser1(String userName, byte[] passwd, String regType) {'||chr(10)||'    try {'||chr(10)||'      String salt = genSalt();'||chr(10)||'      String combPasswd = salt + passwd;'||chr(10)||'      byte[] secureHash = secureHash(combPasswd.getBytes());'||chr(10)||'        // always use salt'||chr(10)||'        saveUser(userName, secureHash);         // salt and secure hash used'||chr(10)||'      }'||chr(10)||'    } catch (NoSuchAlgorithmException e) {'||chr(10)||'      e.printStackTrace();'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void saveUser(String userName, byte[] passwd) {'||chr(10)||'    // store the user info'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private String genSalt()'||chr(10)||'  {'||chr(10)||'    SecureRandom rand = new SecureRandom();'||chr(10)||'    byte[] salt = new byte[16];'||chr(10)||'    rand.nextBytes(salt);   // salt used'||chr(10)||'    return new String(salt);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private byte[] secureHash(byte [] passwd) throws NoSuchAlgorithmException{'||chr(10)||'    MessageDigest msgDigest = MessageDigest.getInstance("SHA256");  // secure hash algorithm'||chr(10)||'    return msgDigest.digest(passwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private void testRegUser(String userName, String passwd, String regType) {'||chr(10)||'    regUser1(userName, passwd.getBytes(), regType);'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC62-J.msg_template', 'In file ${se.filename}, line ${se.line}, the string ${se.var} is using improperly seeded/salted or using insecured hash in method ${se.func}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC62-J.msg_template', '在${se.filename}, 第${se.line}行,函数 ${se.func}的变量${se.var}使用了不正确的种子/盐渍或不安全的哈希', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.MSC62-J.name', 'Passwords should be stored encrypted, preferably using hash', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.MSC62-J.name', '应该以加密形式存储密码，最好是使用hash', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='MSC62-J');
-- priority empty

-- ------------------------
-- OBJ01-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'OBJ01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/OBJ01-J.+Limit+accessibility+of+fields', '${rule.Xcalibyte.CERT.1.OBJ01-J.name}', null, 'OBJ01-J', 2, 1, 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.OBJ01-J.detail}', '${rule.Xcalibyte.CERT.1.OBJ01-J.description}', '${rule.Xcalibyte.CERT.1.OBJ01-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ01-J'),
 'BASIC','PRIORITY', '12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ01-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ01-J'),
 'STANDARD','OWASP', '02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ01-J'),
 'STANDARD','OWASP', '03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ01-J'),
 'STANDARD','OWASP', '05')
ON CONFLICT DO NOTHING;

-- ------------------------
-- OBJ05-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'OBJ05-J', null, 'OBJ05-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/OBJ05-J.+Do+not+return+references+to+private+mutable+class+members', '${rule.Xcalibyte.CERT.1.OBJ05-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.OBJ05-J.detail}', '${rule.Xcalibyte.CERT.1.OBJ05-J.description}', '${rule.Xcalibyte.CERT.1.OBJ05-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.OBJ05-J.description', 'The program is returning references to mutable class members that are declared private', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ05-J.description', 'The program is returning references to mutable class members that are declared private', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ05-J.detail', '#### Abstract'||chr(10)||'The program is returning references to mutable class members that are declared private.'||chr(10)||'#### Explanation'||chr(10)||'Returning reference to a private mutable class member is breaking encapsulation and also makes a private object available to be manipulated by an untrusted calling method.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.util.Date;'||chr(10)||''||chr(10)||'public class obj05_0 {'||chr(10)||'  private Data private_d;'||chr(10)||''||chr(10)||'  // this class is mutable'||chr(10)||'  public obj05_0() {'||chr(10)||'    // private_d is declared private, and is being returned '||chr(10)||'    // thus exposes the internal mutable component to untrustable caller'||chr(10)||'    private_d = new Data();'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public Data getData() {'||chr(10)||'    return private_d;'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  // a copy is returned, hence, no leakage using this method'||chr(10)||'  public Data getDataSafe() {'||chr(10)||'    return (Data)d.clone();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.util.Date;'||chr(10)||''||chr(10)||'public class obj05_0 {'||chr(10)||'  private Data private_d;'||chr(10)||''||chr(10)||'  // this class is mutable'||chr(10)||'  public obj05_0() {'||chr(10)||'    private_d = new Data();'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // a copy is returned, hence, no leakage using this method'||chr(10)||'  public Data getDataSafe() {'||chr(10)||'    return (Data)d.clone();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ05-J.detail', '#### Abstract'||chr(10)||'The program is returning references to mutable class members that are declared private.'||chr(10)||'#### Explanation'||chr(10)||'Returning reference to a private mutable class member is breaking encapsulation and also makes a private object available to be manipulated by an untrusted calling method.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.util.Date;'||chr(10)||''||chr(10)||'public class obj05_0 {'||chr(10)||'  private Data private_d;'||chr(10)||''||chr(10)||'  // this class is mutable'||chr(10)||'  public obj05_0() {'||chr(10)||'    // private_d is declared private, and is being returned '||chr(10)||'    // thus exposes the internal mutable component to untrustable caller'||chr(10)||'    private_d = new Data();'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public Data getData() {'||chr(10)||'    return private_d;'||chr(10)||'  }'||chr(10)||'  '||chr(10)||'  // a copy is returned, hence, no leakage using this method'||chr(10)||'  public Data getDataSafe() {'||chr(10)||'    return (Data)d.clone();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.util.Date;'||chr(10)||''||chr(10)||'public class obj05_0 {'||chr(10)||'  private Data private_d;'||chr(10)||''||chr(10)||'  // this class is mutable'||chr(10)||'  public obj05_0() {'||chr(10)||'    private_d = new Data();'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // a copy is returned, hence, no leakage using this method'||chr(10)||'  public Data getDataSafe() {'||chr(10)||'    return (Data)d.clone();'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ05-J.msg_template', 'Supported by SpotBug, no description', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ05-J.msg_template', 'Supported by SpotBug, no description', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ05-J.name', 'References to mutable class members that are private should not be returned and exposed', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ05-J.name', '私有及可改变的类成员的引用是不应返回并暴露的', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ05-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ05-J'),
 'BASIC','PRIORITY', '12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ05-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ05-J'),
 'STANDARD','OWASP', '02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ05-J'),
 'STANDARD','OWASP', '03'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ05-J'),
 'STANDARD','OWASP', '05'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ05-J'),
 'STANDARD','OWASP', '08')
ON CONFLICT DO NOTHING;

-- ------------------------
-- OBJ07-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'OBJ07-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/OBJ07-J.+Sensitive+classes+must+not+let+themselves+be+copied', '${rule.Xcalibyte.CERT.1.OBJ07-J.name}', null, 'OBJ07-J', 2, 2, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.OBJ07-J.detail}', '${rule.Xcalibyte.CERT.1.OBJ07-J.description}', '${rule.Xcalibyte.CERT.1.OBJ07-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;




insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.OBJ07-J.description', 'Classes with sensitive data should be protected against being copied or cloned', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ07-J.description', '具有敏感数据的类应受到保护, 不被复制或克隆', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ07-J.detail', '#### Abstract'||chr(10)||'Classes with sensitive data should be protected against being copied or cloned. '||chr(10)||'#### Explanation'||chr(10)||'Classes with sensitive data, when cloned or copied maliciously, may be exposed to thread-safety issues that violates invariants of critical data. '||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'public class j_obj07_sensitive'||chr(10)||'{'||chr(10)||'  private String userName;'||chr(10)||'  private char[] passwd;'||chr(10)||''||chr(10)||'  public j_obj07_sensitive(String name, char[] pass) {  // OBJ07-J: class not provide clone throw exception and Mark final'||chr(10)||'    userName = name;'||chr(10)||'    passwd = pass;'||chr(10)||'  } '||chr(10)||''||chr(10)||'  public String getUserName() {'||chr(10)||'    return userName;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public char[] getPasswd() {'||chr(10)||'    return passwd;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  protected void resetPasswd() {'||chr(10)||'    for(int i = 0; i < passwd.length; i++) {'||chr(10)||'      passwd[i] = ''a'';'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  void display() {'||chr(10)||'    System.out.println("UserName addr:" + System.identityHashCode(userName));'||chr(10)||'    System.out.println("Passwd addr:" + System.identityHashCode(passwd));'||chr(10)||'    System.out.print("User:" + userName + " passwd:");'||chr(10)||'    System.out.println(passwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'public class j_obj07_fp_1 '||chr(10)||'{'||chr(10)||'  private String userName;'||chr(10)||'  private char[] passwd;'||chr(10)||''||chr(10)||'  public j_obj07_fp_1(String name, String pass) {'||chr(10)||'    userName = name;'||chr(10)||'    passwd = pass.toCharArray();'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // [certj pages]'||chr(10)||'  // sensitive class define clone() and throws CloneNotSupportedException'||chr(10)||'  // and clone() method marked finalclone'||chr(10)||'  // Well behaved clone that prevents subclasses from being made cloneable by defining a final clone that always fails'||chr(10)||'  public final j_obj07_fp_1 clone() throws CloneNotSupportedException {'||chr(10)||'    throw new CloneNotSupportedException();'||chr(10)||'  }'||chr(10)||''||chr(10)||'  void resetPasswd() {'||chr(10)||'    for(int i = 0; i < passwd.length; i++) {'||chr(10)||'      passwd[i] = ''a'';'||chr(10)||'    }   '||chr(10)||'  }'||chr(10)||''||chr(10)||'  void display() {'||chr(10)||'    System.out.println("UserName addr:" + System.identityHashCode(userName));'||chr(10)||'    System.out.println("Passwd addr:" + System.identityHashCode(passwd));'||chr(10)||'    System.out.print("User:" + userName + " passwd:");'||chr(10)||'    System.out.println(passwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    j_obj07_fp_1 obj1= new j_obj07_fp_1("user1", "abcdef");'||chr(10)||'    try {'||chr(10)||'      j_obj07_fp_1 obj2 = (j_obj07_fp_1)obj1.clone();'||chr(10)||'      obj2.resetPasswd();'||chr(10)||'      obj1.display();'||chr(10)||'      obj2.display();'||chr(10)||'    } catch (CloneNotSupportedException e) {'||chr(10)||'      System.out.println("Error: clone is not allowed for sensitive class");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ07-J.detail', '#### 概要'||chr(10)||''||chr(10)||'具有敏感数据的类应受到保护, 不被复制或克隆.'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'具有敏感数据的类在被恶意克隆或复制时，可能会导致违反关键数据不变属性的线程安全问题。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||''||chr(10)||'public class j_obj07_sensitive'||chr(10)||'{'||chr(10)||'  private String userName;'||chr(10)||'  private char[] passwd;'||chr(10)||''||chr(10)||'  public j_obj07_sensitive(String name, char[] pass) {  // OBJ07-J: class not provide clone throw exception and Mark final'||chr(10)||'    userName = name;'||chr(10)||'    passwd = pass;'||chr(10)||'  } '||chr(10)||''||chr(10)||'  public String getUserName() {'||chr(10)||'    return userName;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public char[] getPasswd() {'||chr(10)||'    return passwd;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  protected void resetPasswd() {'||chr(10)||'    for(int i = 0; i < passwd.length; i++) {'||chr(10)||'      passwd[i] = ''a'';'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  void display() {'||chr(10)||'    System.out.println("UserName addr:" + System.identityHashCode(userName));'||chr(10)||'    System.out.println("Passwd addr:" + System.identityHashCode(passwd));'||chr(10)||'    System.out.print("User:" + userName + " passwd:");'||chr(10)||'    System.out.println(passwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````'||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'public class j_obj07_fp_1 '||chr(10)||'{'||chr(10)||'  private String userName;'||chr(10)||'  private char[] passwd;'||chr(10)||''||chr(10)||'  public j_obj07_fp_1(String name, String pass) {'||chr(10)||'    userName = name;'||chr(10)||'    passwd = pass.toCharArray();'||chr(10)||'  }'||chr(10)||''||chr(10)||'  // [certj pages]'||chr(10)||'  // sensitive class define clone() and throws CloneNotSupportedException'||chr(10)||'  // and clone() method marked finalclone'||chr(10)||'  // Well behaved clone that prevents subclasses from being made cloneable by defining a final clone that always fails'||chr(10)||'  public final j_obj07_fp_1 clone() throws CloneNotSupportedException {'||chr(10)||'    throw new CloneNotSupportedException();'||chr(10)||'  }'||chr(10)||''||chr(10)||'  void resetPasswd() {'||chr(10)||'    for(int i = 0; i < passwd.length; i++) {'||chr(10)||'      passwd[i] = ''a'';'||chr(10)||'    }   '||chr(10)||'  }'||chr(10)||''||chr(10)||'  void display() {'||chr(10)||'    System.out.println("UserName addr:" + System.identityHashCode(userName));'||chr(10)||'    System.out.println("Passwd addr:" + System.identityHashCode(passwd));'||chr(10)||'    System.out.print("User:" + userName + " passwd:");'||chr(10)||'    System.out.println(passwd);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    j_obj07_fp_1 obj1= new j_obj07_fp_1("user1", "abcdef");'||chr(10)||'    try {'||chr(10)||'      j_obj07_fp_1 obj2 = (j_obj07_fp_1)obj1.clone();'||chr(10)||'      obj2.resetPasswd();'||chr(10)||'      obj1.display();'||chr(10)||'      obj2.display();'||chr(10)||'    } catch (CloneNotSupportedException e) {'||chr(10)||'      System.out.println("Error: clone is not allowed for sensitive class");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ07-J.msg_template', 'In file ${se.filename}, line ${se.line}, the method ${se.var} is copied or clonable', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ07-J.msg_template', '在${se.filename}, 第${se.line}行,函数 ${se.var} 是可以复制或克隆的', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ07-J.name', 'Classes with sensitive data should be protected against being copied or cloned', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ07-J.name', '应保护具有敏感数据的类应受到保护，防止被复制或克隆', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ07-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ07-J'),
 'BASIC','PRIORITY', '8'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ07-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

-- ------------------------
-- OBJ09-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'OBJ09-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/OBJ09-J.+Compare+classes+and+not+class+names', '${rule.Xcalibyte.CERT.1.OBJ09-J.name}', null, 'OBJ09-J', 1, 2, 'UNLIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.OBJ09-J.detail}', '${rule.Xcalibyte.CERT.1.OBJ09-J.description}', '${rule.Xcalibyte.CERT.1.OBJ09-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.OBJ09-J.description', 'Comparing class names may cause mix and match attacks', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ09-J.description', '比较类名可能会导致混合搭配攻击', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ09-J.detail', '#### Abstract'||chr(10)||'Comparing class names may cause mix and match attacks.'||chr(10)||'#### Explanation'||chr(10)||'Two classes are the same class only if they have the same qualified names. Classes with the same name may have different package names. Also since distinct class loaders (or different instances of the class loader) will result in different classes with the same fully qualified names. '||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.lang.String;'||chr(10)||'public class j_obj09_1 {'||chr(10)||'  void callComp(Boolean v) {'||chr(10)||'    Class cls1 = getClass();'||chr(10)||'    Class cls2 = String.class;'||chr(10)||'    if(v) {'||chr(10)||'      badComp1(cls1.getName()); '||chr(10)||'    } else {'||chr(10)||'      badComp2(cls1.getName(), cls2.getName());'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'  Boolean badComp1(String clsName) {'||chr(10)||'    if(clsName.equals("j_obj09_0")) {'||chr(10)||'      return true;'||chr(10)||'    }'||chr(10)||'    else {'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }  '||chr(10)||''||chr(10)||'  Boolean badComp2(String name1, String name2)'||chr(10)||'  {'||chr(10)||'    if(name1.equals(name2)) {'||chr(10)||'      return true;'||chr(10)||'    }'||chr(10)||'    else {'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ09-J.detail', '#### 概要'||chr(10)||'比较类名可能会导致混合搭配攻击。'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'当两个类具有相同的限定名称时，它们才是同一类。 具有相同名称的类可能具有不同的包名称。 同样由于不同的类加载器（或类加载器的不同实例）将导致具有相同完全限定名称的不同类。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例 '||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.lang.String;'||chr(10)||'public class j_obj09_1 {'||chr(10)||'  void callComp(Boolean v) {'||chr(10)||'    Class cls1 = getClass();'||chr(10)||'    Class cls2 = String.class;'||chr(10)||'    if(v) {'||chr(10)||'      badComp1(cls1.getName()); '||chr(10)||'    } else {'||chr(10)||'      badComp2(cls1.getName(), cls2.getName());'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'  Boolean badComp1(String clsName) {'||chr(10)||'    if(clsName.equals("j_obj09_0")) {'||chr(10)||'      return true;'||chr(10)||'    }'||chr(10)||'    else {'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }  '||chr(10)||''||chr(10)||'  Boolean badComp2(String name1, String name2)'||chr(10)||'  {'||chr(10)||'    if(name1.equals(name2)) {'||chr(10)||'      return true;'||chr(10)||'    }'||chr(10)||'    else {'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ09-J.msg_template', 'In ${se.filename} function ${se.func}, line ${se.line}, name is used to compare classes. The class instance is created in ${ss.filename}, line ${ss.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ09-J.msg_template', '在${se.filename}, 第${se.line}行,函数 ${se.func} 使用了名称比较类. 這类的实例是在${ss.filename}, 第${ss.line}行产生的', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ09-J.name', 'Comparing class names may cause mix and match attacks', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ09-J.name', '比较类名可能会导致混搭攻击', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ09-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ09-J'),
 'BASIC','PRIORITY', '9'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ09-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ09-J'),
 'STANDARD','OWASP', '02')
ON CONFLICT DO NOTHING;


-- ------------------------
-- OBJ11-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'OBJ11-J', null, 'OBJ11-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/OBJ11-J.+Be+wary+of+letting+constructors+throw+exceptions', '${rule.Xcalibyte.CERT.1.OBJ11-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.OBJ11-J.detail}', '${rule.Xcalibyte.CERT.1.OBJ11-J.description}', '${rule.Xcalibyte.CERT.1.OBJ11-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.OBJ11-J.description', 'Constructors throwing exceptions could leave the object in a partially initialized state', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ11-J.description', '抛出异常的构造函数会使对象处于部分初始状态', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ11-J.detail', '#### Abstract'||chr(10)||'Constructors throwing exceptions could leave the object in a partially initialized state.'||chr(10)||'#### Explanation'||chr(10)||'During objection construction, the object is in a partially initialized state. Throwing exceptions during construction could leave the object in an inconsistent state.'||chr(10)||''||chr(10)||'#### Example '||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class obj11_0 {'||chr(10)||'  public obj11_0()'||chr(10)||'  {'||chr(10)||'    // by throwing an exception in constructor'||chr(10)||'    // attacker can capture a reference to the partially initialized object of the obj11_0 class since'||chr(10)||'    // the object reference remains in the garbge collector'||chr(10)||'    if (!performVerification()) {'||chr(10)||'      throw new SecurityException("Access Denied!");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private boolean performVerification() {'||chr(10)||'    return false; // Returns true if data entered is valid, else false'||chr(10)||'                  // Assume that the attacker always enters an invalid value'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void greeting() {'||chr(10)||'    System.out.println("Welcome user!");'||chr(10)||'  }'||chr(10)||'}### OBJ11'||chr(10)||'#### Abstract'||chr(10)||'Constructors throwing exceptions could leave the object in a partially initialized state.'||chr(10)||'#### Explanation'||chr(10)||'During objection construction, the object is in a partially initialized state. Throwing exceptions during construction could leave the object in an inconsistent state.'||chr(10)||''||chr(10)||'#### Example '||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class obj11_0 {'||chr(10)||'  public obj11_0()'||chr(10)||'  {'||chr(10)||'    // by throwing an exception in constructor'||chr(10)||'    // attacker can capture a reference to the partially initialized object of the obj11_0 class since'||chr(10)||'    // the object reference remains in the garbge collector'||chr(10)||'    if (!performVerification()) {'||chr(10)||'      throw new SecurityException("Access Denied!");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private boolean performVerification() {'||chr(10)||'    return false; // Returns true if data entered is valid, else false'||chr(10)||'                  // Assume that the attacker always enters an invalid value'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void greeting() {'||chr(10)||'    System.out.println("Welcome user!");'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ11-J.detail', '#### 概要'||chr(10)||'抛出异常的构造函数会使对象处于部分初始状态'||chr(10)||'#### 解释'||chr(10)||'在对象构造过程中，该对象处于部分初始状态。在构造过程中抛出异常会使对象处于不一致状态。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例 '||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class obj11_0 {'||chr(10)||'  public obj11_0()'||chr(10)||'  {'||chr(10)||'    // by throwing an exception in constructor'||chr(10)||'    // attacker can capture a reference to the partially initialized object of the obj11_0 class since'||chr(10)||'    // the object reference remains in the garbge collector'||chr(10)||'    if (!performVerification()) {'||chr(10)||'      throw new SecurityException("Access Denied!");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  private boolean performVerification() {'||chr(10)||'    return false; // Returns true if data entered is valid, else false'||chr(10)||'                  // Assume that the attacker always enters an invalid value'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void greeting() {'||chr(10)||'    System.out.println("Welcome user!");'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ11-J.msg_template', 'In file ${ss.filename}, line ${ss.line}, constructor ${se.func}, is throwing exception ${se.var} in ${se.filename}, line ${se.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ11-J.msg_template', '在${ss.filename}, 第${ss.line}行, 构造函数${se.func} 会在${se.filename}, 第${se.line}行抛出异常', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.OBJ11-J.name', 'Constructors should not throw exceptions', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.OBJ11-J.name', '构造函数不应抛出异常', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ11-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ11-J'),
 'BASIC','PRIORITY', '12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ11-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ11-J'),
 'STANDARD','OWASP', '02')
ON CONFLICT DO NOTHING;

-- ------------------------
-- OBJ13-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'OBJ13-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/OBJ13-J.+Ensure+that+references+to+mutable+objects+are+not+exposed', '${rule.Xcalibyte.CERT.1.OBJ13-J.name}', null, 'OBJ13-J', 2, 1, 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.OBJ13-J.detail}', '${rule.Xcalibyte.CERT.1.OBJ13-J.description}', '${rule.Xcalibyte.CERT.1.OBJ13-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;




insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ13-J'),
 'BASIC','PRIORITY', '18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ13-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ13-J'),
 'STANDARD','OWASP', '02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ13-J'),
 'STANDARD','OWASP', '03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='OBJ13-J'),
 'STANDARD','OWASP', '05')
ON CONFLICT DO NOTHING;

-- ------------------------
-- SEC01-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC01-J', null, 'SEC01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC01-J.+Do+not+allow+tainted+variables+in+privileged+blocks', '${rule.Xcalibyte.CERT.1.SEC01-J.name}', 1, 1, 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.SEC01-J.detail}', '${rule.Xcalibyte.CERT.1.SEC01-J.description}', '${rule.Xcalibyte.CERT.1.SEC01-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.SEC01-J.description', 'The program has untrusted data in privileged blocks', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC01-J.description', '该程序在特权代码块里有不受信任数据', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC01-J.detail', '#### Abstract'||chr(10)||'The program has untrusted data in privileged blocks.'||chr(10)||'#### Explanation'||chr(10)||'Untrusted data may contain special characters that eventually form tainted paths or file names. Attackers can take advantage of tainted data that get into privileged blocks for malicious intent.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.FileInputStream;'||chr(10)||'import java.io.FileNotFoundException;'||chr(10)||'import java.security.AccessController;'||chr(10)||'import java.security.PrivilegedActionException;'||chr(10)||'import java.security.PrivilegedExceptionAction;'||chr(10)||''||chr(10)||'public class sec01_0 { // edited from CERT-J example'||chr(10)||'  // input file name may be tainted since origin is unknown'||chr(10)||'  // should have gone through sanitization and normalization before '||chr(10)||'  // enter the doPrivileged() block'||chr(10)||'  private void privilegedMethod(final String filename)'||chr(10)||'    throws FileNotFoundException {'||chr(10)||'    try {'||chr(10)||'      FileInputStream fis ='||chr(10)||'        (FileInputStream) AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {'||chr(10)||'          return new FileInputStream(filename);'||chr(10)||'        });'||chr(10)||'      // Do something with the file and then close it'||chr(10)||'    } catch (PrivilegedActionException e) {'||chr(10)||'      // Forward to handler'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.FileInputStream;'||chr(10)||'import java.io.FileNotFoundException;'||chr(10)||'import java.security.AccessController;'||chr(10)||'import java.security.PrivilegedActionException;'||chr(10)||'import java.security.PrivilegedExceptionAction;'||chr(10)||''||chr(10)||'public class sec01_0 { // edited from CERT-J example'||chr(10)||'  // input file name gone through sanitization and normalization before '||chr(10)||'  // enter the doPrivileged() block'||chr(10)||'  }'||chr(10)||'  private void privilegedMethod(final String filename)'||chr(10)||'    throws FileNotFoundException {'||chr(10)||' '||chr(10)||'    final String sanitizeFilename;'||chr(10)||'    try {'||chr(10)||'      sanitizeFilename = sanitizeFilename(filename);'||chr(10)||'    } catch {'||chr(10)||'      // call handler appropriately '||chr(10)||'      ...'||chr(10)||'    }'||chr(10)||'    '||chr(10)||'    try {'||chr(10)||'      FileInputStream fis ='||chr(10)||'        (FileInputStream) AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {'||chr(10)||'          return new FileInputStream(filename);'||chr(10)||'        });'||chr(10)||'      // Do something with the file and then close it'||chr(10)||'    } catch (PrivilegedActionException e) {'||chr(10)||'      // Forward to handler'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC01-J.detail', '#### 概要'||chr(10)||'该程序在特权代码块里有不受信任数据'||chr(10)||'#### 解释'||chr(10)||'不受信任数据可能含有特殊字符，它最终会形成受污染的路径或文件名。攻击者能利用进入特权代码块的受污染数据以达到恶意企图。'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.FileInputStream;'||chr(10)||'import java.io.FileNotFoundException;'||chr(10)||'import java.security.AccessController;'||chr(10)||'import java.security.PrivilegedActionException;'||chr(10)||'import java.security.PrivilegedExceptionAction;'||chr(10)||''||chr(10)||'public class sec01_0 { // edited from CERT-J example'||chr(10)||'  // input file name may be tainted since origin is unknown'||chr(10)||'  // should have gone through sanitization and normalization before '||chr(10)||'  // enter the doPrivileged() block'||chr(10)||'  private void privilegedMethod(final String filename)'||chr(10)||'    throws FileNotFoundException {'||chr(10)||'    try {'||chr(10)||'      FileInputStream fis ='||chr(10)||'        (FileInputStream) AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {'||chr(10)||'          return new FileInputStream(filename);'||chr(10)||'        });'||chr(10)||'      // Do something with the file and then close it'||chr(10)||'    } catch (PrivilegedActionException e) {'||chr(10)||'      // Forward to handler'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.FileInputStream;'||chr(10)||'import java.io.FileNotFoundException;'||chr(10)||'import java.security.AccessController;'||chr(10)||'import java.security.PrivilegedActionException;'||chr(10)||'import java.security.PrivilegedExceptionAction;'||chr(10)||''||chr(10)||'public class sec01_0 { // edited from CERT-J example'||chr(10)||'  // input file name gone through sanitization and normalization before '||chr(10)||'  // enter the doPrivileged() block'||chr(10)||'  }'||chr(10)||'  private void privilegedMethod(final String filename)'||chr(10)||'    throws FileNotFoundException {'||chr(10)||' '||chr(10)||'    final String sanitizeFilename;'||chr(10)||'    try {'||chr(10)||'      sanitizeFilename = sanitizeFilename(filename);'||chr(10)||'    } catch {'||chr(10)||'      // call handler appropriately '||chr(10)||'      ...'||chr(10)||'    }'||chr(10)||'    '||chr(10)||'    try {'||chr(10)||'      FileInputStream fis ='||chr(10)||'        (FileInputStream) AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {'||chr(10)||'          return new FileInputStream(filename);'||chr(10)||'        });'||chr(10)||'      // Do something with the file and then close it'||chr(10)||'    } catch (PrivilegedActionException e) {'||chr(10)||'      // Forward to handler'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC01-J.msg_template', 'In ${se.filename}, line ${se.line}, sensitive information may be leaked throught ${se.var} inside a trusted block ${se.func}. An instance of the privileged code is at ${ss.filename} at line ${ss.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC01-J.msg_template', '在${se.filename}, 第${se.line}行, 不受信任数据可能通过函数${se.func}达到恶意攻击. 特权代码的实例是在${ss.filename}, line ${ss.line}产生的', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC01-J.name', 'Untrusted data is not allowed in privileged blocks', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC01-J.name', '不允许在特权代码块里有不受信任数据', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC01-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC01-J'),
 'BASIC','PRIORITY', '27'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC01-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC01-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC01-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC01-J'),
 'STANDARD','OWASP','05')
ON CONFLICT DO NOTHING;

-- ------------------------
-- SEC02-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC02-J', null, 'SEC02-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC02-J.+Do+not+base+security+checks+on+untrusted+sources', '${rule.Xcalibyte.CERT.1.SEC02-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SEC02-J.detail}', '${rule.Xcalibyte.CERT.1.SEC02-J.description}', '${rule.Xcalibyte.CERT.1.SEC02-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.SEC02-J.description', 'The program has security checks whose sources are possibly from an untrusted origin', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC02-J.description', '此程序中安全检查代码的来源可能不受信', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC02-J.detail', '#### Abstract'||chr(10)||'The program has security checks whose sources are possibly from an untrusted origin.'||chr(10)||'#### Explanation'||chr(10)||'Untrusted code may have been due to many different sources. The check method could be bypassed through override or bypassed. The checked object may be overridden also after the check. Even if the object is saved after passing the security check, the copy method itself may not be sufficient if the method is not thorough enough (such as shallow copy).'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.FileNotFoundException;'||chr(10)||'import java.io.RandomAccessFile;'||chr(10)||'import java.security.AccessController;'||chr(10)||'import java.security.PrivilegedAction;'||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class sec02_0 {'||chr(10)||'  public static RandomAccessFile openFile(final java.io.File f) {'||chr(10)||'    // getPath() can be extended, thus security check can pass the first time, '||chr(10)||'    // but getPath() changed the second time, bypass the good check'||chr(10)||'    if (f.getPath().contains("passwd")){'||chr(10)||'      return null;'||chr(10)||'    };'||chr(10)||'    // ...'||chr(10)||'    return (RandomAccessFile) AccessController.doPrivileged(new PrivilegedAction<Object>() {'||chr(10)||'      public Object run() {'||chr(10)||'        try {'||chr(10)||'          return new RandomAccessFile(f, "r");'||chr(10)||'        } catch (FileNotFoundException e) {'||chr(10)||'          e.printStackTrace();'||chr(10)||'        }'||chr(10)||'        return null;'||chr(10)||'      }'||chr(10)||'    });'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'  '||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.FileNotFoundException;'||chr(10)||'import java.io.RandomAccessFile;'||chr(10)||'import java.security.AccessController;'||chr(10)||'import java.security.PrivilegedAction;'||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class sec02_0 {'||chr(10)||'  public static RandomAccessFile openFile(final java.io.File f) {'||chr(10)||'    // get a copy through getPath(), but not a clone'||chr(10)||'    final java.io.File copy = new java.io.File(f.getPath());'||chr(10)||'    // use copy of path later on'||chr(10)||'    if (copy.getPath().contains("passwd")){'||chr(10)||'      return null;'||chr(10)||'    };'||chr(10)||'    // ...'||chr(10)||'    return (RandomAccessFile) AccessController.doPrivileged(new PrivilegedAction<Object>() {'||chr(10)||'      public Object run() {'||chr(10)||'        try {'||chr(10)||'          return new RandomAccessFile(copy.getPath(), "r");'||chr(10)||'        } catch (FileNotFoundException e) {'||chr(10)||'          e.printStackTrace();'||chr(10)||'        }'||chr(10)||'        return null;'||chr(10)||'      }'||chr(10)||'    });'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC02-J.detail', '#### 概要'||chr(10)||'此程序中安全检查代码的来源可能不受信'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'不受信任代码可能是由于有许多不同的来源。可能可以通过重写或bypassed绕过检查方法。在检查后也能重写经过检查的对象。即使在通过安全检查后保存了该对象，如果方法不够完全彻底（例如浅拷贝），那么拷贝方法本身可能是不够的'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.FileNotFoundException;'||chr(10)||'import java.io.RandomAccessFile;'||chr(10)||'import java.security.AccessController;'||chr(10)||'import java.security.PrivilegedAction;'||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class sec02_0 {'||chr(10)||'  public static RandomAccessFile openFile(final java.io.File f) {'||chr(10)||'    // getPath() can be extended, thus security check can pass the first time, '||chr(10)||'    // but getPath() changed the second time, bypass the good check'||chr(10)||'    if (f.getPath().contains("passwd")){'||chr(10)||'      return null;'||chr(10)||'    };'||chr(10)||'    // ...'||chr(10)||'    return (RandomAccessFile) AccessController.doPrivileged(new PrivilegedAction<Object>() {'||chr(10)||'      public Object run() {'||chr(10)||'        try {'||chr(10)||'          return new RandomAccessFile(f, "r");'||chr(10)||'        } catch (FileNotFoundException e) {'||chr(10)||'          e.printStackTrace();'||chr(10)||'        }'||chr(10)||'        return null;'||chr(10)||'      }'||chr(10)||'    });'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'  '||chr(10)||'````'||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.io.FileNotFoundException;'||chr(10)||'import java.io.RandomAccessFile;'||chr(10)||'import java.security.AccessController;'||chr(10)||'import java.security.PrivilegedAction;'||chr(10)||'import java.io.IOException;'||chr(10)||''||chr(10)||'public class sec02_0 {'||chr(10)||'  public static RandomAccessFile openFile(final java.io.File f) {'||chr(10)||'    // get a copy through getPath(), but not a clone'||chr(10)||'    final java.io.File copy = new java.io.File(f.getPath());'||chr(10)||'    // use copy of path later on'||chr(10)||'    if (copy.getPath().contains("passwd")){'||chr(10)||'      return null;'||chr(10)||'    };'||chr(10)||'    // ...'||chr(10)||'    return (RandomAccessFile) AccessController.doPrivileged(new PrivilegedAction<Object>() {'||chr(10)||'      public Object run() {'||chr(10)||'        try {'||chr(10)||'          return new RandomAccessFile(copy.getPath(), "r");'||chr(10)||'        } catch (FileNotFoundException e) {'||chr(10)||'          e.printStackTrace();'||chr(10)||'        }'||chr(10)||'        return null;'||chr(10)||'      }'||chr(10)||'    });'||chr(10)||'  }'||chr(10)||'}'||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC02-J.msg_template', 'In file ${se.filename}, line ${se.line}, the method ${se.func} is returning untrusted sources to ${se.var} under assumed checked security', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC02-J.msg_template', '在${se.filename}, 第${se.line}行, 函数${se.func} 返回来源不可信的安全检查給与${se.var}', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC02-J.name', 'Security check code should not be based on untrusted sources', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC02-J.name', '安全检查代码不应基于不受信任来源', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC02-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC02-J'),
 'BASIC','PRIORITY', '12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC02-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC02-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC02-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC02-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC02-J'),
 'STANDARD','OWASP','06')
ON CONFLICT DO NOTHING;

-- ------------------------
-- SEC03-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC03-J', null, 'SEC03-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC03-J.+Do+not+load+trusted+classes+after+allowing+untrusted+code+to+load+arbitrary+classes', '${rule.Xcalibyte.CERT.1.SEC03-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SEC03-J.detail}', '${rule.Xcalibyte.CERT.1.SEC03-J.description}', '${rule.Xcalibyte.CERT.1.SEC03-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.SEC03-J.description', 'The program may allow a class loader from an untrusted source to load trusted classes', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC03-J.description', 'The program may allow a class loader from an untrusted source to load trusted classes', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC03-J.detail', '### NYI'||chr(10)||'#### Abstract'||chr(10)||'The program may allow a class loader from an untrusted source to load trusted classes.'||chr(10)||'#### Explanation'||chr(10)||'When an untrusted class loader loads trusted classes, the trusted classes can be contaminated and malicious classes (such as trojan classes) can be loaded.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example '||chr(10)||'````text'||chr(10)||''||chr(10)||'// Fix me'||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC03-J.detail', '### NYI'||chr(10)||'#### Abstract'||chr(10)||'The program may allow a class loader from an untrusted source to load trusted classes.'||chr(10)||'#### Explanation'||chr(10)||'When an untrusted class loader loads trusted classes, the trusted classes can be contaminated and malicious classes (such as trojan classes) can be loaded.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example '||chr(10)||'````text'||chr(10)||''||chr(10)||'// Fix me'||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC03-J.msg_template', '<<Fix me>>', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC03-J.msg_template', '<<Fix me>>', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC03-J.name', '<<NYI>> Trusted classes must be loaded by trusted class loaders', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC03-J.name', '<<NYI>> 受信任类必须由受信任类加载器加载', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC03-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC03-J'),
 'BASIC','PRIORITY', '12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC03-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC03-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC03-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC03-J'),
 'STANDARD','OWASP','09')
ON CONFLICT DO NOTHING;

-- ------------------------
-- SEC04-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC04-J', null, 'SEC04-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC04-J.+Protect+sensitive+operations+with+security+manager+checks', '${rule.Xcalibyte.CERT.1.SEC04-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SEC04-J.detail}', '${rule.Xcalibyte.CERT.1.SEC04-J.description}', '${rule.Xcalibyte.CERT.1.SEC04-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.SEC04-J.description', '<<Fix me>>', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC04-J.description', '<<Fix me>>', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC04-J.detail', '### NYI'||chr(10)||'#### Abstract'||chr(10)||'Fix me'||chr(10)||'#### Explanation'||chr(10)||'Fix me'||chr(10)||''||chr(10)||''||chr(10)||'#### Example '||chr(10)||'````text'||chr(10)||''||chr(10)||'// Fix me'||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC04-J.detail', '### NYI'||chr(10)||'#### Abstract'||chr(10)||'Fix me'||chr(10)||'#### Explanation'||chr(10)||'Fix me'||chr(10)||''||chr(10)||''||chr(10)||'#### Example '||chr(10)||'````text'||chr(10)||''||chr(10)||'// Fix me'||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC04-J.msg_template', '<<Fix me>>', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC04-J.msg_template', '<<Fix me>>', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC04-J.name', 'Sensitive operations should be protected with the proper security manager check', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC04-J.name', '应该用适当的security manager来检查及保护敏感操作', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC04-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC04-J'),
 'BASIC','PRIORITY', '12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC04-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC04-J'),
 'STANDARD','OWASP', '03')
ON CONFLICT DO NOTHING;

-- ------------------------
-- SEC05-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC05-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC05-J.+Do+not+use+reflection+to+increase+accessibility+of+classes%2C+methods%2C+or+fields', '${rule.Xcalibyte.CERT.1.SEC05-J.name}', null, 'SEC05-J', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SEC05-J.detail}', '${rule.Xcalibyte.CERT.1.SEC05-J.description}', '${rule.Xcalibyte.CERT.1.SEC05-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC05-J'),
 'BASIC','PRIORITY', '12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC05-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC05-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC05-J'),
 'STANDARD','OWASP','05')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SEC06-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC06-J', null, 'SEC06-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC06-J.+Do+not+rely+on+the+default+automatic+signature+verification+provided+by+URLClassLoader+and+java.util.jar', '${rule.Xcalibyte.CERT.1.SEC06-J.name}', 1, 1, 'PROBABLE', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SEC06-J.detail}', '${rule.Xcalibyte.CERT.1.SEC06-J.description}', '${rule.Xcalibyte.CERT.1.SEC06-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.SEC06-J.description', 'The program is using the default automatic signature verifier provided by URLClassLoader and java.util.jar', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC06-J.description', '该程序正使用URLClassLoader和java.util.jar提供的默认自动签名检验器', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC06-J.detail', '#### Abstract'||chr(10)||'The program is using the default automatic signature verifier provided by URLClassLoader and java.util.jar.'||chr(10)||'#### Explanation'||chr(10)||'Using these packages as default automatic signature verification is not sufficient when the signature is used for privilege elevation purpose. It only performs an integrity check. Also it only uses public key which may have been maliciously modified for authentication of loaded classes.'||chr(10)||''||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'package io.xc5.cert;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.lang.reflect.InvocationTargetException;'||chr(10)||'import java.lang.reflect.Method;'||chr(10)||'import java.lang.reflect.Modifier;'||chr(10)||'import java.net.JarURLConnection;'||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.util.jar.Attributes;'||chr(10)||'import java.security.GeneralSecurityException;'||chr(10)||'import java.security.KeyStore;'||chr(10)||'import java.security.cert.Certificate;'||chr(10)||'import java.io.FileInputStream;'||chr(10)||''||chr(10)||'public class j_sec06_1 extends URLClassLoader {'||chr(10)||'  private URL url;'||chr(10)||'  public j_sec06_1(URL url) {'||chr(10)||'    super(new URL[] { url });'||chr(10)||'    this.url = url;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void invokeClass(String name, String[] args)'||chr(10)||'      throws ClassNotFoundException, NoSuchMethodException,'||chr(10)||'      InvocationTargetException , GeneralSecurityException, IOException {'||chr(10)||'    Class c = loadClass(name);'||chr(10)||'    Method m = c.getMethod("getValue", new Class[] { args.getClass() });'||chr(10)||'    m.setAccessible(true);'||chr(10)||'    try {'||chr(10)||'      m.invoke(null, new Object[] { args });'||chr(10)||'    } catch (IllegalAccessException e) {'||chr(10)||'      System.out.println("Access denied");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'  '||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'package io.xc5.cert;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.lang.reflect.InvocationTargetException;'||chr(10)||'import java.lang.reflect.Method;'||chr(10)||'import java.lang.reflect.Modifier;'||chr(10)||'import java.net.JarURLConnection;'||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.util.jar.Attributes;'||chr(10)||'import java.security.GeneralSecurityException;'||chr(10)||'import java.security.KeyStore;'||chr(10)||'import java.security.cert.Certificate;'||chr(10)||'import java.io.FileInputStream;'||chr(10)||''||chr(10)||'public class j_sec06_fp_1 extends URLClassLoader {'||chr(10)||'  private URL url;'||chr(10)||'  public j_sec06_fp_1(URL url) {'||chr(10)||'    super(new URL[] { url });'||chr(10)||'    this.url = url;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  Boolean verifyClass(Class c) throws ClassNotFoundException, NoSuchMethodException,'||chr(10)||'      InvocationTargetException, GeneralSecurityException,'||chr(10)||'      IOException {'||chr(10)||'    Certificate[] certs = c.getProtectionDomain().getCodeSource().getCertificates();'||chr(10)||'    if (certs == null) {'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'    else { '||chr(10)||'      KeyStore ks = KeyStore.getInstance("JKS");'||chr(10)||'      ks.load(new FileInputStream("sec06.jks"), "loadkeystorepassword".toCharArray());'||chr(10)||'      Certificate pubCert = ks.getCertificate("sec06");'||chr(10)||'      // Check with the trusted public key, else throws exception'||chr(10)||'      certs[0].verify(pubCert.getPublicKey());'||chr(10)||'      return true;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void invokeClass(String name, String[] args)'||chr(10)||'      throws ClassNotFoundException, NoSuchMethodException,'||chr(10)||'      InvocationTargetException , GeneralSecurityException, IOException {'||chr(10)||'    Class c = loadClass(name);'||chr(10)||'    Method m = c.getMethod("getValue", new Class[] { args.getClass() });'||chr(10)||'    m.setAccessible(true);'||chr(10)||'    '||chr(10)||'    // use customized, non-default verification method declared above'||chr(10)||'    if(verifyClass(c)) {'||chr(10)||'      try {'||chr(10)||'        m.invoke(null, new Object[] { args });'||chr(10)||'      } catch (IllegalAccessException e) {'||chr(10)||'        System.out.println("Access denied");'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC06-J.detail', '#### 概要'||chr(10)||'该程序正使用URLClassLoader和java.util.jar提供的默认自动签名检验器'||chr(10)||'#### 解释'||chr(10)||'当签名用于权限提升目的时，把这些包当作默认自动签名检验来使用是不够的。它只会执行完整性检查。并且它只使用公钥来对加载的类进行认证，此公钥可能已被恶意修改。'||chr(10)||''||chr(10)||'####  示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'package io.xc5.cert;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.lang.reflect.InvocationTargetException;'||chr(10)||'import java.lang.reflect.Method;'||chr(10)||'import java.lang.reflect.Modifier;'||chr(10)||'import java.net.JarURLConnection;'||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.util.jar.Attributes;'||chr(10)||'import java.security.GeneralSecurityException;'||chr(10)||'import java.security.KeyStore;'||chr(10)||'import java.security.cert.Certificate;'||chr(10)||'import java.io.FileInputStream;'||chr(10)||''||chr(10)||'public class j_sec06_1 extends URLClassLoader {'||chr(10)||'  private URL url;'||chr(10)||'  public j_sec06_1(URL url) {'||chr(10)||'    super(new URL[] { url });'||chr(10)||'    this.url = url;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void invokeClass(String name, String[] args)'||chr(10)||'      throws ClassNotFoundException, NoSuchMethodException,'||chr(10)||'      InvocationTargetException , GeneralSecurityException, IOException {'||chr(10)||'    Class c = loadClass(name);'||chr(10)||'    Method m = c.getMethod("getValue", new Class[] { args.getClass() });'||chr(10)||'    m.setAccessible(true);'||chr(10)||'    try {'||chr(10)||'      m.invoke(null, new Object[] { args });'||chr(10)||'    } catch (IllegalAccessException e) {'||chr(10)||'      System.out.println("Access denied");'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||''||chr(10)||'  '||chr(10)||'````'||chr(10)||''||chr(10)||'####  示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||'package io.xc5.cert;'||chr(10)||'import java.io.IOException;'||chr(10)||'import java.lang.reflect.InvocationTargetException;'||chr(10)||'import java.lang.reflect.Method;'||chr(10)||'import java.lang.reflect.Modifier;'||chr(10)||'import java.net.JarURLConnection;'||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.util.jar.Attributes;'||chr(10)||'import java.security.GeneralSecurityException;'||chr(10)||'import java.security.KeyStore;'||chr(10)||'import java.security.cert.Certificate;'||chr(10)||'import java.io.FileInputStream;'||chr(10)||''||chr(10)||'public class j_sec06_fp_1 extends URLClassLoader {'||chr(10)||'  private URL url;'||chr(10)||'  public j_sec06_fp_1(URL url) {'||chr(10)||'    super(new URL[] { url });'||chr(10)||'    this.url = url;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  Boolean verifyClass(Class c) throws ClassNotFoundException, NoSuchMethodException,'||chr(10)||'      InvocationTargetException, GeneralSecurityException,'||chr(10)||'      IOException {'||chr(10)||'    Certificate[] certs = c.getProtectionDomain().getCodeSource().getCertificates();'||chr(10)||'    if (certs == null) {'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'    else { '||chr(10)||'      KeyStore ks = KeyStore.getInstance("JKS");'||chr(10)||'      ks.load(new FileInputStream("sec06.jks"), "loadkeystorepassword".toCharArray());'||chr(10)||'      Certificate pubCert = ks.getCertificate("sec06");'||chr(10)||'      // Check with the trusted public key, else throws exception'||chr(10)||'      certs[0].verify(pubCert.getPublicKey());'||chr(10)||'      return true;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public void invokeClass(String name, String[] args)'||chr(10)||'      throws ClassNotFoundException, NoSuchMethodException,'||chr(10)||'      InvocationTargetException , GeneralSecurityException, IOException {'||chr(10)||'    Class c = loadClass(name);'||chr(10)||'    Method m = c.getMethod("getValue", new Class[] { args.getClass() });'||chr(10)||'    m.setAccessible(true);'||chr(10)||'    '||chr(10)||'    // use customized, non-default verification method declared above'||chr(10)||'    if(verifyClass(c)) {'||chr(10)||'      try {'||chr(10)||'        m.invoke(null, new Object[] { args });'||chr(10)||'      } catch (IllegalAccessException e) {'||chr(10)||'        System.out.println("Access denied");'||chr(10)||'      }'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC06-J.msg_template', 'In ${se.filename} function ${se.func}, line ${se.line}, method ${se.var} invoked relies on default automatic signature verification by URLClassLoader with the default automatic signature. The class is loaded at ${ss.filename}, line ${ss.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC06-J.msg_template', '在${se.filename}, 第${se.line}行, 在函数${se.func}里, method ${se.var} 调用默认自动签名验证器的ClassLoader. ${se.var}是在${ss.filename}, 第${ss.line}行加载的', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC06-J.name', 'The default automatic signature verifier provided by URLClassLoader and java.util.jar should not be used', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC06-J.name', '不应使用URLClassLoader和java.util.jar提供的默认自动签名检验器', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC06-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC06-J'),
 'BASIC','PRIORITY', '12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC06-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC06-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC06-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC06-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC06-J'),
 'STANDARD','OWASP','06')
ON CONFLICT DO NOTHING;

-- ------------------------
-- SEC07-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SEC07-J', null, 'SEC07-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SEC07-J.+Call+the+superclass%27s+getPermissions%28%29+method+when+writing+a+custom+class+loader', '${rule.Xcalibyte.CERT.1.SEC07-J.name}', 1, 1, 'PROBABLE', 'LOW', '${rule.Xcalibyte.CERT.1.SEC07-J.detail}', '${rule.Xcalibyte.CERT.1.SEC07-J.description}', '${rule.Xcalibyte.CERT.1.SEC07-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.CERT.1.SEC07-J.description', 'The program has a custom class loader and failed to call super.getPermission() method', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC07-J.description', '该程序有自定义类加载器，并未能调用super.getPermission()方法', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC07-J.detail', '#### Abstract'||chr(10)||'The program has a custom class loader and failed to call super.getPermission() method. '||chr(10)||'#### Explanation'||chr(10)||'The program has a custom class loader to override the getPermission() method. The implementation should invoke the superclass''s getPermission() to get the default system policy before assigning custom permission to the program. This way, both system wide security policies are also applied.'||chr(10)||''||chr(10)||'#### Example - avoid'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.security.CodeSource;'||chr(10)||'import java.security.PermissionCollection;'||chr(10)||'import java.security.Permissions;'||chr(10)||''||chr(10)||'public class j_sec07_0 extends URLClassLoader { '||chr(10)||''||chr(10)||'  public j_sec07_0(URL[] urls) {'||chr(10)||'    super(urls);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  protected PermissionCollection getPermissions(CodeSource cs) {'||chr(10)||'    PermissionCollection pc = new Permissions();'||chr(10)||'    // Allow exit from the VM anytime'||chr(10)||'    pc.add(new RuntimePermission("exitVM"));'||chr(10)||'    return pc;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static boolean checkRtPermission(URL[] urls, String tag) {'||chr(10)||'    j_sec07_0 loader = new j_sec07_0(urls);'||chr(10)||'    PermissionCollection pc = loader.getPermissions(null);'||chr(10)||'    RuntimePermission rp = new RuntimePermission(tag);'||chr(10)||'    if(pc.implies(rp)) {'||chr(10)||'      System.out.println("Able to exit vm");'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      System.out.println("Not Able to exit VM");'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    URL[] urls = new URL[0];'||chr(10)||'    checkRtPermission(urls, "exitVM");'||chr(10)||'    checkRtPermission(urls, "stopVM");'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````'||chr(10)||''||chr(10)||'#### Example - prefer'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.security.CodeSource;'||chr(10)||'import java.security.PermissionCollection;'||chr(10)||'import java.security.Permissions;'||chr(10)||''||chr(10)||'public class j_sec07_0 extends URLClassLoader { '||chr(10)||''||chr(10)||'  public j_sec07_0(URL[] urls) {'||chr(10)||'    super(urls);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  protected PermissionCollection getPermissions(CodeSource cs) {'||chr(10)||'    // apply default system wide security policy'||chr(10)||'    PermissionCollection pc = superPermissions(cs);'||chr(10)||'    // Allow exit from the VM anytime'||chr(10)||'    pc.add(new RuntimePermission("exitVM"));'||chr(10)||'    return pc;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static boolean checkRtPermission(URL[] urls, String tag) {'||chr(10)||'    j_sec07_0 loader = new j_sec07_0(urls);'||chr(10)||'    PermissionCollection pc = loader.getPermissions(null);'||chr(10)||'    RuntimePermission rp = new RuntimePermission(tag);'||chr(10)||'    if(pc.implies(rp)) {'||chr(10)||'      System.out.println("Able to exit vm");'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      System.out.println("Not Able to exit VM");'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    URL[] urls = new URL[0];'||chr(10)||'    checkRtPermission(urls, "exitVM");'||chr(10)||'    checkRtPermission(urls, "stopVM");'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC07-J.detail', '#### 概要'||chr(10)||'该程序有自定义类加载器，并未能调用super.getPermission()方法。'||chr(10)||'#### 解释'||chr(10)||'该程序有自定义加载器来重写getPermission()方法。该实现应调用父类的getPermission()以便把自定义权限赋给程序前获得默认的系统策略。这样一来，系统全局里的安全策略也能得到应用。'||chr(10)||'#### 示例 - 避免'||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.security.CodeSource;'||chr(10)||'import java.security.PermissionCollection;'||chr(10)||'import java.security.Permissions;'||chr(10)||''||chr(10)||'public class j_sec07_0 extends URLClassLoader {'||chr(10)||''||chr(10)||'  public j_sec07_0(URL[] urls) {'||chr(10)||'    super(urls);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  protected PermissionCollection getPermissions(CodeSource cs) {'||chr(10)||'    PermissionCollection pc = new Permissions();'||chr(10)||'    // Allow exit from the VM anytime'||chr(10)||'    pc.add(new RuntimePermission("exitVM"));'||chr(10)||'    return pc;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static boolean checkRtPermission(URL[] urls, String tag) {'||chr(10)||'    j_sec07_0 loader = new j_sec07_0(urls);'||chr(10)||'    PermissionCollection pc = loader.getPermissions(null);'||chr(10)||'    RuntimePermission rp = new RuntimePermission(tag);'||chr(10)||'    if(pc.implies(rp)) {'||chr(10)||'      System.out.println("Able to exit vm");'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      System.out.println("Not Able to exit VM");'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    URL[] urls = new URL[0];'||chr(10)||'    checkRtPermission(urls, "exitVM");'||chr(10)||'    checkRtPermission(urls, "stopVM");'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````'||chr(10)||''||chr(10)||'#### 示例 - 建议'||chr(10)||''||chr(10)||'````text'||chr(10)||''||chr(10)||'import java.net.URL;'||chr(10)||'import java.net.URLClassLoader;'||chr(10)||'import java.security.CodeSource;'||chr(10)||'import java.security.PermissionCollection;'||chr(10)||'import java.security.Permissions;'||chr(10)||''||chr(10)||'public class j_sec07_0 extends URLClassLoader { '||chr(10)||''||chr(10)||'  public j_sec07_0(URL[] urls) {'||chr(10)||'    super(urls);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  protected PermissionCollection getPermissions(CodeSource cs) {'||chr(10)||'    // apply default system wide security policy'||chr(10)||'    PermissionCollection pc = superPermissions(cs);'||chr(10)||'    // Allow exit from the VM anytime'||chr(10)||'    pc.add(new RuntimePermission("exitVM"));'||chr(10)||'    return pc;'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static boolean checkRtPermission(URL[] urls, String tag) {'||chr(10)||'    j_sec07_0 loader = new j_sec07_0(urls);'||chr(10)||'    PermissionCollection pc = loader.getPermissions(null);'||chr(10)||'    RuntimePermission rp = new RuntimePermission(tag);'||chr(10)||'    if(pc.implies(rp)) {'||chr(10)||'      System.out.println("Able to exit vm");'||chr(10)||'      return true;'||chr(10)||'    } else {'||chr(10)||'      System.out.println("Not Able to exit VM");'||chr(10)||'      return false;'||chr(10)||'    }'||chr(10)||'  }'||chr(10)||''||chr(10)||'  public static void main(String[] args) {'||chr(10)||'    URL[] urls = new URL[0];'||chr(10)||'    checkRtPermission(urls, "exitVM");'||chr(10)||'    checkRtPermission(urls, "stopVM");'||chr(10)||'  }'||chr(10)||''||chr(10)||'}'||chr(10)||''||chr(10)||'  '||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC07-J.msg_template', 'In file ${se.filename} at line ${se.line}, function ${se.func}, ${se.var} overrode getPermission() without invoking super''s permission. The customer class loading happend at ${ss.filename} at line ${ss.line}', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC07-J.msg_template', '在${se.filename}, 第${se.line}行, 在函数${se.func}裡, ${se.var}自定义加载器覆盖getPermission(). 未有采用系统全局里的安全策略.自定义加载在${ss.filename}, 第${ss.line}行产生', 'system', 'system'),
('en', 'rule.Xcalibyte.CERT.1.SEC07-J.name', 'Call the superclass''s getPermissions() method to implement a custom class loader', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.CERT.1.SEC07-J.name', '请调用父类的getPermissions()方法实现自定义类加载器', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
        where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC07-J');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC07-J'),
 'BASIC','PRIORITY', '18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC07-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC07-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC07-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC07-J'),
 'STANDARD','OWASP','06'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SEC07-J'),
 'STANDARD','OWASP','09')
ON CONFLICT DO NOTHING;

-- ------------------------
-- SER01-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SER01-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SER01-J.+Do+not+deviate+from+the+proper+signatures+of+serialization+methods', '${rule.Xcalibyte.CERT.1.SER01-J.name}', null, 'SER01-J', 1, 1, 'LIKELY', 'LOW', '${rule.Xcalibyte.CERT.1.SER01-J.detail}', '${rule.Xcalibyte.CERT.1.SER01-J.description}', '${rule.Xcalibyte.CERT.1.SER01-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER01-J'),
 'BASIC','PRIORITY', '27'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER01-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER01-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER01-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER01-J'),
 'STANDARD','OWASP','08')
ON CONFLICT DO NOTHING;
-- ------------------------
-- SER04-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SER04-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SER04-J.+Do+not+allow+serialization+and+deserialization+to+bypass+the+security+manager', '${rule.Xcalibyte.CERT.1.SER04-J.name}', null, 'SER04-J', 1, 2, 'PROBABLE', 'HIGH', '${rule.Xcalibyte.CERT.1.SER04-J.detail}', '${rule.Xcalibyte.CERT.1.SER04-J.description}', '${rule.Xcalibyte.CERT.1.SER04-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;



insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER04-J'),
 'BASIC','PRIORITY', '6'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER04-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER04-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER04-J'),
 'STANDARD','OWASP','03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER04-J'),
 'STANDARD','OWASP','08')
ON CONFLICT DO NOTHING;
-- ------------------------
-- SER05-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SER05-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SER05-J.+Do+not+serialize+instances+of+inner+classes', '${rule.Xcalibyte.CERT.1.SER05-J.name}', null, 'SER05-J', 2, 1, 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SER05-J.detail}', '${rule.Xcalibyte.CERT.1.SER05-J.description}', '${rule.Xcalibyte.CERT.1.SER05-J.msg_template}', 'system', 'system')
ON CONFLICT (rule_set_id, rule_code) DO UPDATE set category = excluded.category, vulnerable = excluded.vulnerable, certainty = excluded.certainty, language = excluded.language, url = excluded.url, name = excluded.name, severity = excluded.severity, priority = excluded.priority, likelihood = excluded.likelihood, remediation_cost = excluded.remediation_cost, detail = excluded.detail, description = excluded.description, msg_template = excluded.msg_template, modified_by = excluded.modified_by;


insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER05-J'),
 'BASIC','PRIORITY', '12'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER05-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER05-J'),
 'STANDARD','OWASP', '03'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER05-J'),
 'STANDARD','OWASP', '08')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SER08-J
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, language, url, name, certainty, rule_code, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'CERT' and "rule_set".version ='1'), 'VUL', 'SER08-J', 'java', 'https://wiki.sei.cmu.edu/confluence/display/java/SER08-J.+Minimize+privileges+before+deserializing+from+a+privileged+context', '${rule.Xcalibyte.CERT.1.SER08-J.name}', null, 'SER08-J', 1, 1, 'LIKELY', 'MEDIUM', '${rule.Xcalibyte.CERT.1.SER08-J.detail}', '${rule.Xcalibyte.CERT.1.SER08-J.description}', '${rule.Xcalibyte.CERT.1.SER08-J.msg_template}', 'system', 'system')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER08-J'),
 'BASIC','PRIORITY', '18'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER08-J'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER08-J'),
 'STANDARD','OWASP','02'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER08-J'),
 'STANDARD','OWASP','03'),
 ((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER08-J'),
 'STANDARD','OWASP','05'),
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'Xcalibyte' and rs.name = 'CERT' and rs.version ='1' and ri.rule_code ='SER08-J'),
 'STANDARD','OWASP','08')
ON CONFLICT DO NOTHING;

select * from xcalibyte.rule_information_attribute;