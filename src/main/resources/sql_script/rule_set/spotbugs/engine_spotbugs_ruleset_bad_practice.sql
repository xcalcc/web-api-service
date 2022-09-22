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
-- AM_CREATES_EMPTY_JAR_FILE_ENTRY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'AM_CREATES_EMPTY_JAR_FILE_ENTRY', null, 'AM_CREATES_EMPTY_JAR_FILE_ENTRY', 'java', null, 'AM_CREATES_EMPTY_JAR_FILE_ENTRY', null, null, null, null, 'The code calls <code>putNextEntry()</code>, immediately followed by a call to <code>closeEntry()</code>. This results in an empty JarFile entry. The contents of the entry should be written to the JarFile between the calls to<code>putNextEntry()</code> and<code>closeEntry()</code>.', 'Creates an empty jar file entry', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='AM_CREATES_EMPTY_JAR_FILE_ENTRY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='AM_CREATES_EMPTY_JAR_FILE_ENTRY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- AM_CREATES_EMPTY_ZIP_FILE_ENTRY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'AM_CREATES_EMPTY_ZIP_FILE_ENTRY', null, 'AM_CREATES_EMPTY_ZIP_FILE_ENTRY', 'java', null, 'AM_CREATES_EMPTY_ZIP_FILE_ENTRY', null, null, null, null, 'The code calls <code>putNextEntry()</code>, immediately followed by a call to <code>closeEntry()</code>. This results in an empty ZipFile entry. The contents of the entry should be written to the ZipFile between the calls to<code>putNextEntry()</code> and<code>closeEntry()</code>.', 'Creates an empty zip file entry', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='AM_CREATES_EMPTY_ZIP_FILE_ENTRY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='AM_CREATES_EMPTY_ZIP_FILE_ENTRY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS', null, 'BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS', 'java', null, 'BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS', null, null, null, null, 'The <code>equals(Object o)</code> method shouldn''t make any assumptions about the type of <code>o</code>. It should simply return false if <code>o</code> is not the same type as <code>this</code>.', 'Equals method should not assume anything about the type of its argument', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BIT_SIGNED_CHECK
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'BIT_SIGNED_CHECK', null, 'BIT_SIGNED_CHECK', 'java', null, 'BIT_SIGNED_CHECK', null, null, null, null, 'This method compares an expression such as<code>((event.detail &amp; SWT.SELECTED) &gt; 0)</code>.Using bit arithmetic and then comparing with the greater than operator can lead to unexpected results (of course depending on the value ofSWT.SELECTED). If SWT.SELECTED is a negative number, this is a candidate for a bug. Even when SWT.SELECTED is not negative, it seems good practice to use ''!= 0'' instead of ''&gt; 0''.', 'Check for sign of bitwise operation', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_SIGNED_CHECK');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_SIGNED_CHECK'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CNT_ROUGH_CONSTANT_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'CNT_ROUGH_CONSTANT_VALUE', null, 'CNT_ROUGH_CONSTANT_VALUE', 'java', null, 'CNT_ROUGH_CONSTANT_VALUE', null, null, null, null, 'It''s recommended to use the predefined library constant for code clarity and better precision.', 'Rough value of known constant found', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CNT_ROUGH_CONSTANT_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CNT_ROUGH_CONSTANT_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CN_IDIOM
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'CN_IDIOM', null, 'CN_IDIOM', 'java', null, 'CN_IDIOM', null, null, null, null, 'Class implements Cloneable but does not define or use the clone method.', 'Class implements Cloneable but does not define or use clone method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CN_IDIOM');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CN_IDIOM'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CN_IDIOM_NO_SUPER_CALL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'CN_IDIOM_NO_SUPER_CALL', null, 'CN_IDIOM_NO_SUPER_CALL', 'java', null, 'CN_IDIOM_NO_SUPER_CALL', null, null, null, null, 'This non-final class defines a clone() method that does not call super.clone().If this class ("<i>A</i>") is extended by a subclass ("<i>B</i>"),and the subclass <i>B</i> calls super.clone(), then it is likely that<i>B</i>''s clone() method will return an object of type <i>A</i>,which violates the standard contract for clone(). If all clone() methods call super.clone(), then they are guaranteed to use Object.clone(), which always returns an object of the correct type.', 'clone method does not call super.clone()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CN_IDIOM_NO_SUPER_CALL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CN_IDIOM_NO_SUPER_CALL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE', null, 'CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE', 'java', null, 'CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE', null, null, null, null, 'This class defines a clone() method but the class doesn''t implement Cloneable.There are some situations in which this is OK (e.g., you want to control how subclasses can clone themselves), but just make sure that this is what you intended.', 'Class defines clone() but doesn''t implement Cloneable', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CO_ABSTRACT_SELF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'CO_ABSTRACT_SELF', null, 'CO_ABSTRACT_SELF', 'java', null, 'CO_ABSTRACT_SELF', null, null, null, null, 'This class defines a covariant version of <code>compareTo()</code>.&nbsp; To correctly override the <code>compareTo()</code> method in the <code>Comparable</code> interface, the parameter of <code>compareTo()</code> must have type <code>java.lang.Object</code>.', 'Abstract class defines covariant compareTo() method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CO_ABSTRACT_SELF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CO_ABSTRACT_SELF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CO_COMPARETO_INCORRECT_FLOATING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'CO_COMPARETO_INCORRECT_FLOATING', null, 'CO_COMPARETO_INCORRECT_FLOATING', 'java', null, 'CO_COMPARETO_INCORRECT_FLOATING', null, null, null, null, 'This method compares double or float values using pattern like this: val1 &gt; val2 ? 1 : val1 &lt; val2 ? -1 : 0.This pattern works incorrectly for -0.0 and NaN values which may result in incorrect sorting result or broken collection(if compared values are used as keys). Consider using Double.compare or Float.compare static methods which handle all the special cases correctly.', 'compareTo()/compare() incorrectly handles float or double value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CO_COMPARETO_INCORRECT_FLOATING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CO_COMPARETO_INCORRECT_FLOATING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CO_COMPARETO_RESULTS_MIN_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'CO_COMPARETO_RESULTS_MIN_VALUE', null, 'CO_COMPARETO_RESULTS_MIN_VALUE', 'java', null, 'CO_COMPARETO_RESULTS_MIN_VALUE', null, null, null, null, 'In some situation, this compareTo or compare method returns the constant Integer.MIN_VALUE, which is an exceptionally bad practice. The only thing that matters about the return value of compareTo is the sign of the result. But people will sometimes negate the return value of compareTo, expecting that this will negate the sign of the result. And it will, except in the case where the value returned is Integer.MIN_VALUE. So just return -1 rather than Integer.MIN_VALUE.', 'compareTo()/compare() returns Integer.MIN_VALUE', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CO_COMPARETO_RESULTS_MIN_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CO_COMPARETO_RESULTS_MIN_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CO_SELF_NO_OBJECT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'CO_SELF_NO_OBJECT', null, 'CO_SELF_NO_OBJECT', 'java', null, 'CO_SELF_NO_OBJECT', null, null, null, null, 'This class defines a covariant version of <code>compareTo()</code>.&nbsp; To correctly override the <code>compareTo()</code> method in the <code>Comparable</code> interface, the parameter of <code>compareTo()</code> must have type <code>java.lang.Object</code>.', 'Covariant compareTo() method defined', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CO_SELF_NO_OBJECT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CO_SELF_NO_OBJECT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DE_MIGHT_DROP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'DE_MIGHT_DROP', null, 'DE_MIGHT_DROP', 'java', null, 'DE_MIGHT_DROP', null, null, null, null, 'This method might drop an exception.&nbsp; In general, exceptions should be handled or reported in some way, or they should be thrown out of the method.', 'Method might drop exception', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DE_MIGHT_DROP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DE_MIGHT_DROP'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DE_MIGHT_IGNORE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'DE_MIGHT_IGNORE', null, 'DE_MIGHT_IGNORE', 'java', null, 'DE_MIGHT_IGNORE', null, null, null, null, 'This method might ignore an exception.&nbsp; In general, exceptions should be handled or reported in some way, or they should be thrown out of the method.', 'Method might ignore exception', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DE_MIGHT_IGNORE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DE_MIGHT_IGNORE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_ENTRY_SETS_MAY_REUSE_ENTRY_OBJECTS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'DMI_ENTRY_SETS_MAY_REUSE_ENTRY_OBJECTS', null, 'DMI_ENTRY_SETS_MAY_REUSE_ENTRY_OBJECTS', 'java', null, 'DMI_ENTRY_SETS_MAY_REUSE_ENTRY_OBJECTS', null, null, null, null, 'The entrySet() method is allowed to return a view of the underlying Map in which a single Entry object is reused and returned during the iteration. As of Java 1.6, both IdentityHashMap and EnumMap did so. When iterating through such a Map, the Entry value is only valid until you advance to the next iteration. If, for example, you try to pass such an entrySet to an addAll method, things will go badly wrong.', 'Adding elements of an entry set may fail due to reuse of Entry objects', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_ENTRY_SETS_MAY_REUSE_ENTRY_OBJECTS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_ENTRY_SETS_MAY_REUSE_ENTRY_OBJECTS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_RANDOM_USED_ONLY_ONCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'DMI_RANDOM_USED_ONLY_ONCE', null, 'DMI_RANDOM_USED_ONLY_ONCE', 'java', null, 'DMI_RANDOM_USED_ONLY_ONCE', null, null, null, null, 'This code creates a java.util.Random object, uses it to generate one random number, and then discards the Random object. This produces mediocre quality random numbers and is inefficient.If possible, rewrite the code so that the Random object is created once and saved, and each time a new random number is required invoke a method on the existing Random object to obtain it.If it is important that the generated Random numbers not be guessable, you <em>must</em> not create a new Random for each random number; the values are too easily guessable. You should strongly consider using a java.security.SecureRandom instead(and avoid allocating a new SecureRandom for each random number needed).', 'Random object created and used only once', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_RANDOM_USED_ONLY_ONCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_RANDOM_USED_ONLY_ONCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_USING_REMOVEALL_TO_CLEAR_COLLECTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'DMI_USING_REMOVEALL_TO_CLEAR_COLLECTION', null, 'DMI_USING_REMOVEALL_TO_CLEAR_COLLECTION', 'java', null, 'DMI_USING_REMOVEALL_TO_CLEAR_COLLECTION', null, null, null, null, 'If you want to remove all elements from a collection <code>c</code>, use <code>c.clear</code>,not <code>c.removeAll(c)</code>. Calling <code>c.removeAll(c)</code> to clear a collection is less clear, susceptible to errors from typos, less efficient and for some collections, might throw a <code>ConcurrentModificationException</code>.', 'Don''t use removeAll to clear a collection', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_USING_REMOVEALL_TO_CLEAR_COLLECTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_USING_REMOVEALL_TO_CLEAR_COLLECTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_EXIT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'DM_EXIT', null, 'DM_EXIT', 'java', null, 'DM_EXIT', null, null, null, null, 'Invoking System.exit shuts down the entire Java virtual machine. This should only been done when it is appropriate. Such calls make it hard or impossible for your code to be invoked by other code. Consider throwing a RuntimeException instead.', 'Method invokes System.exit(...)', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_EXIT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_EXIT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_EXIT'),
 'STANDARD', 'CWE','382')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_RUN_FINALIZERS_ON_EXIT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'DM_RUN_FINALIZERS_ON_EXIT', null, 'DM_RUN_FINALIZERS_ON_EXIT', 'java', null, 'DM_RUN_FINALIZERS_ON_EXIT', null, null, null, null, '<em>Never call System.runFinalizersOnExitor Runtime.runFinalizersOnExit for any reason: they are among the most dangerous methods in the Java libraries.</em> -- Joshua Bloch', 'Method invokes dangerous method runFinalizersOnExit', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_RUN_FINALIZERS_ON_EXIT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_RUN_FINALIZERS_ON_EXIT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_ABSTRACT_SELF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'EQ_ABSTRACT_SELF', null, 'EQ_ABSTRACT_SELF', 'java', null, 'EQ_ABSTRACT_SELF', null, null, null, null, 'This class defines a covariant version of <code>equals()</code>.&nbsp; To correctly override the <code>equals()</code> method in <code>java.lang.Object</code>, the parameter of <code>equals()</code> must have type <code>java.lang.Object</code>.', 'Abstract class defines covariant equals() method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_ABSTRACT_SELF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_ABSTRACT_SELF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS', null, 'EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS', 'java', null, 'EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS', null, null, null, null, 'This equals method is checking to see if the argument is some incompatible type(i.e., a class that is neither a supertype nor subtype of the class that defines the equals method). For example, the Foo class might have an equals method that looks like:<pre><code>public boolean equals(Object o) { if (o instanceof Foo) return name.equals(((Foo)o).name); else if (o instanceof String) return name.equals(o); else return false;}</code></pre>This is considered bad practice, as it makes it very hard to implement an equals method that is symmetric and transitive. Without those properties, very unexpected behaviors are possible.', 'Equals checks for incompatible operand', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_COMPARETO_USE_OBJECT_EQUALS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'EQ_COMPARETO_USE_OBJECT_EQUALS', null, 'EQ_COMPARETO_USE_OBJECT_EQUALS', 'java', null, 'EQ_COMPARETO_USE_OBJECT_EQUALS', null, null, null, null, 'This class defines a <code>compareTo(...)</code> method but inherits its <code>equals()</code> method from <code>java.lang.Object</code>. Generally, the value of compareTo should return zero if and only if equals returns true. If this is violated, weird and unpredictable failures will occur in classes such as PriorityQueue. In Java 5 the PriorityQueue.remove method uses the compareTo method, while in Java 6 it uses the equals method.From the JavaDoc for the compareTo method in the Comparable interface:<blockquote>It is strongly recommended, but not strictly required that <code>(x.compareTo(y)==0) == (x.equals(y))</code>.Generally speaking, any class that implements the Comparable interface and violates this condition should clearly indicate this fact. The recommended language is "Note: this class has a natural ordering that is inconsistent with equals."</blockquote>', 'Class defines compareTo(...) and uses Object.equals()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_COMPARETO_USE_OBJECT_EQUALS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_COMPARETO_USE_OBJECT_EQUALS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_GETCLASS_AND_CLASS_CONSTANT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'EQ_GETCLASS_AND_CLASS_CONSTANT', null, 'EQ_GETCLASS_AND_CLASS_CONSTANT', 'java', null, 'EQ_GETCLASS_AND_CLASS_CONSTANT', null, null, null, null, 'This class has an equals method that will be broken if it is inherited by subclasses.It compares a class literal with the class of the argument (e.g., in class <code>Foo</code>it might check if <code>Foo.class == o.getClass()</code>).It is better to check if <code>this.getClass() == o.getClass()</code>.', 'equals method fails for subtypes', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_GETCLASS_AND_CLASS_CONSTANT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_GETCLASS_AND_CLASS_CONSTANT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_SELF_NO_OBJECT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'EQ_SELF_NO_OBJECT', null, 'EQ_SELF_NO_OBJECT', 'java', null, 'EQ_SELF_NO_OBJECT', null, null, null, null, 'This class defines a covariant version of <code>equals()</code>.&nbsp; To correctly override the <code>equals()</code> method in <code>java.lang.Object</code>, the parameter of <code>equals()</code> must have type <code>java.lang.Object</code>.', 'Covariant equals() method defined', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_SELF_NO_OBJECT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_SELF_NO_OBJECT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ES_COMPARING_PARAMETER_STRING_WITH_EQ
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'ES_COMPARING_PARAMETER_STRING_WITH_EQ', null, 'ES_COMPARING_PARAMETER_STRING_WITH_EQ', 'java', null, 'ES_COMPARING_PARAMETER_STRING_WITH_EQ', null, null, null, null, 'This code compares a <code>java.lang.String</code> parameter for reference equality using the == or != operators. Requiring callers to pass only String constants or interned strings to a method is unnecessarily fragile, and rarely leads to measurable performance gains. Consider using the <code>equals(Object)</code> method instead.', 'Comparison of String parameter using == or !=', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ES_COMPARING_PARAMETER_STRING_WITH_EQ');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ES_COMPARING_PARAMETER_STRING_WITH_EQ'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ES_COMPARING_STRINGS_WITH_EQ
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'ES_COMPARING_STRINGS_WITH_EQ', null, 'ES_COMPARING_STRINGS_WITH_EQ', 'java', null, 'ES_COMPARING_STRINGS_WITH_EQ', null, null, null, null, 'This code compares <code>java.lang.String</code> objects for reference equality using the == or != operators.Unless both strings are either constants in a source file, or have been interned using the <code>String.intern()</code> method, the same string value may be represented by two different String objects. Consider using the <code>equals(Object)</code> method instead.', 'Comparison of String objects using == or !=', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ES_COMPARING_STRINGS_WITH_EQ');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ES_COMPARING_STRINGS_WITH_EQ'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FI_EMPTY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'FI_EMPTY', null, 'FI_EMPTY', 'java', null, 'FI_EMPTY', null, null, null, null, 'Empty <code>finalize()</code> methods are useless, so they should be deleted.', 'Empty finalizer should be deleted', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_EMPTY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_EMPTY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FI_EXPLICIT_INVOCATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'FI_EXPLICIT_INVOCATION', null, 'FI_EXPLICIT_INVOCATION', 'java', null, 'FI_EXPLICIT_INVOCATION', null, null, null, null, 'This method contains an explicit invocation of the <code>finalize()</code> method on an object.&nbsp; Because finalizer methods are supposed to be executed once, and only by the VM, this is a bad idea. If a connected set of objects beings finalizable, then the VM will invoke the finalize method on all the finalizable object, possibly at the same time in different threads.Thus, it is a particularly bad idea, in the finalize method for a class X, invoke finalize on objects referenced by X, because they may already be getting finalized in a separate thread.', 'Explicit invocation of finalizer', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_EXPLICIT_INVOCATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_EXPLICIT_INVOCATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_EXPLICIT_INVOCATION'),
 'STANDARD', 'CWE','586')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FI_FINALIZER_NULLS_FIELDS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'FI_FINALIZER_NULLS_FIELDS', null, 'FI_FINALIZER_NULLS_FIELDS', 'java', null, 'FI_FINALIZER_NULLS_FIELDS', null, null, null, null, 'This finalizer nulls out fields. This is usually an error, as it does not aid garbage collection, and the object is going to be garbage collected anyway.', 'Finalizer nulls fields', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_FINALIZER_NULLS_FIELDS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_FINALIZER_NULLS_FIELDS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FI_FINALIZER_ONLY_NULLS_FIELDS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'FI_FINALIZER_ONLY_NULLS_FIELDS', null, 'FI_FINALIZER_ONLY_NULLS_FIELDS', 'java', null, 'FI_FINALIZER_ONLY_NULLS_FIELDS', null, null, null, null, 'This finalizer does nothing except null out fields. This is completely pointless, and requires that the object be garbage collected, finalized, and then garbage collected again. You should just remove the finalize method.', 'Finalizer only nulls fields', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_FINALIZER_ONLY_NULLS_FIELDS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_FINALIZER_ONLY_NULLS_FIELDS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FI_MISSING_SUPER_CALL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'FI_MISSING_SUPER_CALL', null, 'FI_MISSING_SUPER_CALL', 'java', null, 'FI_MISSING_SUPER_CALL', null, null, null, null, 'This <code>finalize()</code> method does not make a call to its superclass''s <code>finalize()</code> method.&nbsp; So, any finalizer actions defined for the superclass will not be performed.&nbsp; Add a call to <code>super.finalize()</code>.', 'Finalizer does not call superclass finalizer', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_MISSING_SUPER_CALL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_MISSING_SUPER_CALL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FI_NULLIFY_SUPER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'FI_NULLIFY_SUPER', null, 'FI_NULLIFY_SUPER', 'java', null, 'FI_NULLIFY_SUPER', null, null, null, null, 'This empty <code>finalize()</code> method explicitly negates the effect of any finalizer defined by its superclass.&nbsp; Any finalizer actions defined for the superclass will not be performed.&nbsp; Unless this is intended, delete this method.', 'Finalizer nullifies superclass finalizer', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_NULLIFY_SUPER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_NULLIFY_SUPER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FI_USELESS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'FI_USELESS', null, 'FI_USELESS', 'java', null, 'FI_USELESS', null, null, null, null, 'The only thing this <code>finalize()</code> method does is call the superclass''s <code>finalize()</code> method, making it redundant.&nbsp; Delete it.', 'Finalizer does nothing but call superclass finalizer', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_USELESS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FI_USELESS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- GC_UNCHECKED_TYPE_IN_GENERIC_CALL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'GC_UNCHECKED_TYPE_IN_GENERIC_CALL', null, 'GC_UNCHECKED_TYPE_IN_GENERIC_CALL', 'java', null, 'GC_UNCHECKED_TYPE_IN_GENERIC_CALL', null, null, null, null, 'This call to a generic collection method passes an argument while compile type Object where a specific type from the generic type parameters is expected. Thus, neither the standard Java type system nor static analysis can provide useful information on whether the object being passed as a parameter is of an appropriate type.', 'Unchecked type in generic call', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='GC_UNCHECKED_TYPE_IN_GENERIC_CALL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='GC_UNCHECKED_TYPE_IN_GENERIC_CALL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- HE_EQUALS_NO_HASHCODE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'HE_EQUALS_NO_HASHCODE', null, 'HE_EQUALS_NO_HASHCODE', 'java', null, 'HE_EQUALS_NO_HASHCODE', null, null, null, null, 'This class overrides <code>equals(Object)</code>, but does not override <code>hashCode()</code>.&nbsp; Therefore, the class may violate the invariant that equal objects must have equal hashcodes.', 'Class defines equals() but not hashCode()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_EQUALS_NO_HASHCODE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_EQUALS_NO_HASHCODE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- HE_EQUALS_USE_HASHCODE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'HE_EQUALS_USE_HASHCODE', null, 'HE_EQUALS_USE_HASHCODE', 'java', null, 'HE_EQUALS_USE_HASHCODE', null, null, null, null, 'This class overrides <code>equals(Object)</code>, but does not override <code>hashCode()</code>, and inherits the implementation of <code>hashCode()</code> from <code>java.lang.Object</code> (which returns the identity hash code, an arbitrary value assigned to the object by the VM).&nbsp; Therefore, the class is very likely to violate the invariant that equal objects must have equal hashcodes.If you don''t think instances of this class will ever be inserted into a HashMap/HashTable,the recommended <code>hashCode</code> implementation to use is:<pre><code>public int hashCode() { assert false : "hashCode not designed"; return 42; // any arbitrary constant will do}</code></pre>', 'Class defines equals() and uses Object.hashCode()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_EQUALS_USE_HASHCODE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_EQUALS_USE_HASHCODE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- HE_HASHCODE_NO_EQUALS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'HE_HASHCODE_NO_EQUALS', null, 'HE_HASHCODE_NO_EQUALS', 'java', null, 'HE_HASHCODE_NO_EQUALS', null, null, null, null, 'This class defines a <code>hashCode()</code> method but not an <code>equals()</code> method.&nbsp; Therefore, the class may violate the invariant that equal objects must have equal hashcodes.', 'Class defines hashCode() but not equals()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_HASHCODE_NO_EQUALS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_HASHCODE_NO_EQUALS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- HE_HASHCODE_USE_OBJECT_EQUALS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'HE_HASHCODE_USE_OBJECT_EQUALS', null, 'HE_HASHCODE_USE_OBJECT_EQUALS', 'java', null, 'HE_HASHCODE_USE_OBJECT_EQUALS', null, null, null, null, 'This class defines a <code>hashCode()</code> method but inherits its <code>equals()</code> method from <code>java.lang.Object</code> (which defines equality by comparing object references).&nbsp; Although this will probably satisfy the contract that equal objects must have equal hashcodes, it is probably not what was intended by overriding the <code>hashCode()</code> method.&nbsp; (Overriding <code>hashCode()</code> implies that the object''s identity is based on criteria more complicated than simple reference equality.)If you don''t think instances of this class will ever be inserted into a HashMap/HashTable,the recommended <code>hashCode</code> implementation to use is:<pre><code>public int hashCode() { assert false : "hashCode not designed"; return 42; // any arbitrary constant will do}</code></pre>', 'Class defines hashCode() and uses Object.equals()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_HASHCODE_USE_OBJECT_EQUALS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_HASHCODE_USE_OBJECT_EQUALS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- HE_INHERITS_EQUALS_USE_HASHCODE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'HE_INHERITS_EQUALS_USE_HASHCODE', null, 'HE_INHERITS_EQUALS_USE_HASHCODE', 'java', null, 'HE_INHERITS_EQUALS_USE_HASHCODE', null, null, null, null, 'This class inherits <code>equals(Object)</code> from an abstract superclass, and <code>hashCode()</code> from<code>java.lang.Object</code> (which returns the identity hash code, an arbitrary value assigned to the object by the VM).&nbsp; Therefore, the class is very likely to violate the invariant that equal objects must have equal hashcodes. If you don''t want to define a hashCode method, and/or don''t believe the object will ever be put into a HashMap/Hashtable, define the <code>hashCode()</code> method to throw <code>UnsupportedOperationException</code>.', 'Class inherits equals() and uses Object.hashCode()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_INHERITS_EQUALS_USE_HASHCODE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_INHERITS_EQUALS_USE_HASHCODE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IC_SUPERCLASS_USES_SUBCLASS_DURING_INITIALIZATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'IC_SUPERCLASS_USES_SUBCLASS_DURING_INITIALIZATION', null, 'IC_SUPERCLASS_USES_SUBCLASS_DURING_INITIALIZATION', 'java', null, 'IC_SUPERCLASS_USES_SUBCLASS_DURING_INITIALIZATION', null, null, null, null, 'During the initialization of a class, the class makes an active use of a subclass.That subclass will not yet be initialized at the time of this use.For example, in the following code, <code>foo</code> will be null.<pre><code>public class CircularClassInitialization { static class InnerClassSingleton extends CircularClassInitialization { static InnerClassSingleton singleton = new InnerClassSingleton(); } static CircularClassInitialization foo = InnerClassSingleton.singleton;}</code></pre>', 'Superclass uses subclass during initialization', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IC_SUPERCLASS_USES_SUBCLASS_DURING_INITIALIZATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IC_SUPERCLASS_USES_SUBCLASS_DURING_INITIALIZATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IMSE_DONT_CATCH_IMSE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'IMSE_DONT_CATCH_IMSE', null, 'IMSE_DONT_CATCH_IMSE', 'java', null, 'IMSE_DONT_CATCH_IMSE', null, null, null, null, 'IllegalMonitorStateException is generally only thrown in case of a design flaw in your code (calling wait or notify on an object you do not hold a lock on).', 'Dubious catching of IllegalMonitorStateException', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IMSE_DONT_CATCH_IMSE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IMSE_DONT_CATCH_IMSE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ISC_INSTANTIATE_STATIC_CLASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'ISC_INSTANTIATE_STATIC_CLASS', null, 'ISC_INSTANTIATE_STATIC_CLASS', 'java', null, 'ISC_INSTANTIATE_STATIC_CLASS', null, null, null, null, 'This class allocates an object that is based on a class that only supplies static methods. This object does not need to be created, just access the static methods directly using the class name as a qualifier.', 'Needless instantiation of class that only supplies static methods', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ISC_INSTANTIATE_STATIC_CLASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ISC_INSTANTIATE_STATIC_CLASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IT_NO_SUCH_ELEMENT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'IT_NO_SUCH_ELEMENT', null, 'IT_NO_SUCH_ELEMENT', 'java', null, 'IT_NO_SUCH_ELEMENT', null, null, null, null, 'This class implements the <code>java.util.Iterator</code> interface.&nbsp; However, its <code>next()</code> method is not capable of throwing <code>java.util.NoSuchElementException</code>.&nbsp; The <code>next()</code> method should be changed so it throws <code>NoSuchElementException</code> if is called when there are no more elements to return.', 'Iterator next() method can''t throw NoSuchElementException', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IT_NO_SUCH_ELEMENT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IT_NO_SUCH_ELEMENT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- J2EE_STORE_OF_NON_SERIALIZABLE_OBJECT_INTO_SESSION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'J2EE_STORE_OF_NON_SERIALIZABLE_OBJECT_INTO_SESSION', null, 'J2EE_STORE_OF_NON_SERIALIZABLE_OBJECT_INTO_SESSION', 'java', null, 'J2EE_STORE_OF_NON_SERIALIZABLE_OBJECT_INTO_SESSION', null, null, null, null, 'This code seems to be storing a non-serializable object into an HttpSession.If this session is passivated or migrated, an error will result.', 'Store of non serializable object into HttpSession', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='J2EE_STORE_OF_NON_SERIALIZABLE_OBJECT_INTO_SESSION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='J2EE_STORE_OF_NON_SERIALIZABLE_OBJECT_INTO_SESSION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='J2EE_STORE_OF_NON_SERIALIZABLE_OBJECT_INTO_SESSION'),
 'STANDARD', 'CWE','579')
ON CONFLICT DO NOTHING;



-- ------------------------
-- JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS', null, 'JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS', 'java', null, 'JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS', null, null, null, null, 'The class is annotated with net.jcip.annotations.Immutable or javax.annotation.concurrent.Immutable, and the rules for those annotations require that all fields are final. .', 'Fields of immutable classes should be final', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ME_ENUM_FIELD_SETTER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'ME_ENUM_FIELD_SETTER', null, 'ME_ENUM_FIELD_SETTER', 'java', null, 'ME_ENUM_FIELD_SETTER', null, null, null, null, 'This public method declared in public enum unconditionally sets enum field, thus this field can be changed by malicious code or by accident from another package. Though mutable enum fields may be used for lazy initialization, it''s a bad practice to expose them to the outer world. Consider removing this method or declaring it package-private.', 'Public enum method unconditionally sets its field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ME_ENUM_FIELD_SETTER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ME_ENUM_FIELD_SETTER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ME_MUTABLE_ENUM_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'ME_MUTABLE_ENUM_FIELD', null, 'ME_MUTABLE_ENUM_FIELD', 'java', null, 'ME_MUTABLE_ENUM_FIELD', null, null, null, null, 'A mutable public field is defined inside a public enum, thus can be changed by malicious code or by accident from another package. Though mutable enum fields may be used for lazy initialization, it''s a bad practice to expose them to the outer world. Consider declaring this field final and/or package-private.', 'Enum field is public and mutable', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ME_MUTABLE_ENUM_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ME_MUTABLE_ENUM_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_CLASS_NAMING_CONVENTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_CLASS_NAMING_CONVENTION', null, 'NM_CLASS_NAMING_CONVENTION', 'java', null, 'NM_CLASS_NAMING_CONVENTION', null, null, null, null, 'Class names should be nouns, in mixed case with the first letter of each internal word capitalized. Try to keep your class names simple and descriptive. Use whole words-avoid acronyms and abbreviations (unless the abbreviation is much more widely used than the long form, such as URL or HTML).', 'Class names should start with an upper case letter', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_CLASS_NAMING_CONVENTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_CLASS_NAMING_CONVENTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_CLASS_NOT_EXCEPTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_CLASS_NOT_EXCEPTION', null, 'NM_CLASS_NOT_EXCEPTION', 'java', null, 'NM_CLASS_NOT_EXCEPTION', null, null, null, null, 'This class is not derived from another exception, but ends with ''Exception''. This will be confusing to users of this class.', 'Class is not derived from an Exception, even though it is named as such', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_CLASS_NOT_EXCEPTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_CLASS_NOT_EXCEPTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_CONFUSING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_CONFUSING', null, 'NM_CONFUSING', 'java', null, 'NM_CONFUSING', null, null, null, null, 'The referenced methods have names that differ only by capitalization.', 'Confusing method names', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_CONFUSING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_CONFUSING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_FIELD_NAMING_CONVENTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_FIELD_NAMING_CONVENTION', null, 'NM_FIELD_NAMING_CONVENTION', 'java', null, 'NM_FIELD_NAMING_CONVENTION', null, null, null, null, 'Names of fields that are not final should be in mixed case with a lowercase first letter and the first letters of subsequent words capitalized.', 'Field names should start with a lower case letter', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_FIELD_NAMING_CONVENTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_FIELD_NAMING_CONVENTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_FUTURE_KEYWORD_USED_AS_IDENTIFIER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_FUTURE_KEYWORD_USED_AS_IDENTIFIER', null, 'NM_FUTURE_KEYWORD_USED_AS_IDENTIFIER', 'java', null, 'NM_FUTURE_KEYWORD_USED_AS_IDENTIFIER', null, null, null, null, 'The identifier is a word that is reserved as a keyword in later versions of Java, and your code will need to be changed in order to compile it in later versions of Java.', 'Use of identifier that is a keyword in later versions of Java', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_FUTURE_KEYWORD_USED_AS_IDENTIFIER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_FUTURE_KEYWORD_USED_AS_IDENTIFIER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_FUTURE_KEYWORD_USED_AS_MEMBER_IDENTIFIER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_FUTURE_KEYWORD_USED_AS_MEMBER_IDENTIFIER', null, 'NM_FUTURE_KEYWORD_USED_AS_MEMBER_IDENTIFIER', 'java', null, 'NM_FUTURE_KEYWORD_USED_AS_MEMBER_IDENTIFIER', null, null, null, null, 'This identifier is used as a keyword in later versions of Java. This code, and any code that references this API,will need to be changed in order to compile it in later versions of Java.', 'Use of identifier that is a keyword in later versions of Java', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_FUTURE_KEYWORD_USED_AS_MEMBER_IDENTIFIER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_FUTURE_KEYWORD_USED_AS_MEMBER_IDENTIFIER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_METHOD_NAMING_CONVENTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_METHOD_NAMING_CONVENTION', null, 'NM_METHOD_NAMING_CONVENTION', 'java', null, 'NM_METHOD_NAMING_CONVENTION', null, null, null, null, 'Methods should be verbs, in mixed case with the first letter lowercase, with the first letter of each internal word capitalized.', 'Method names should start with a lower case letter', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_METHOD_NAMING_CONVENTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_METHOD_NAMING_CONVENTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_SAME_SIMPLE_NAME_AS_INTERFACE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_SAME_SIMPLE_NAME_AS_INTERFACE', null, 'NM_SAME_SIMPLE_NAME_AS_INTERFACE', 'java', null, 'NM_SAME_SIMPLE_NAME_AS_INTERFACE', null, null, null, null, 'This class/interface has a simple name that is identical to that of an implemented/extended interface, except that the interface is in a different package (e.g., <code>alpha.Foo</code> extends <code>beta.Foo</code>).This can be exceptionally confusing, create lots of situations in which you have to look at import statements to resolve references and creates many opportunities to accidentally define methods that do not override methods in their superclasses.', 'Class names shouldn''t shadow simple name of implemented interface', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_SAME_SIMPLE_NAME_AS_INTERFACE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_SAME_SIMPLE_NAME_AS_INTERFACE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_SAME_SIMPLE_NAME_AS_SUPERCLASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_SAME_SIMPLE_NAME_AS_SUPERCLASS', null, 'NM_SAME_SIMPLE_NAME_AS_SUPERCLASS', 'java', null, 'NM_SAME_SIMPLE_NAME_AS_SUPERCLASS', null, null, null, null, 'This class has a simple name that is identical to that of its superclass, except that its superclass is in a different package (e.g., <code>alpha.Foo</code> extends <code>beta.Foo</code>).This can be exceptionally confusing, create lots of situations in which you have to look at import statements to resolve references and creates many opportunities to accidentally define methods that do not override methods in their superclasses.', 'Class names shouldn''t shadow simple name of superclass', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_SAME_SIMPLE_NAME_AS_SUPERCLASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_SAME_SIMPLE_NAME_AS_SUPERCLASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_VERY_CONFUSING_INTENTIONAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_VERY_CONFUSING_INTENTIONAL', null, 'NM_VERY_CONFUSING_INTENTIONAL', 'java', null, 'NM_VERY_CONFUSING_INTENTIONAL', null, null, null, null, 'The referenced methods have names that differ only by capitalization.This is very confusing because if the capitalization were identical then one of the methods would override the other. From the existence of other methods, it seems that the existence of both of these methods is intentional, but is sure is confusing.You should try hard to eliminate one of them, unless you are forced to have both due to frozen APIs.', 'Very confusing method names (but perhaps intentional)', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_VERY_CONFUSING_INTENTIONAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_VERY_CONFUSING_INTENTIONAL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_WRONG_PACKAGE_INTENTIONAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NM_WRONG_PACKAGE_INTENTIONAL', null, 'NM_WRONG_PACKAGE_INTENTIONAL', 'java', null, 'NM_WRONG_PACKAGE_INTENTIONAL', null, null, null, null,
 'The method in the subclass doesn''t override a similar method in a superclass because the type of a parameter doesn''t exactly match the type of the corresponding parameter in the superclass. For example, if you have:<pre><code>import alpha.Foo;public class A { public int f(Foo x) { return 17; }}----import beta.Foo;public class B extends A { public int f(Foo x) { return 42; } public int f(alpha.Foo x) { return 27; }}</code></pre>The <code>f(Foo)</code> method defined in class <code>B</code> doesn''toverride the<code>f(Foo)</code> method defined in class <code>A</code>, because the argument types are <code>Foo</code>''s from different packages.In this case, the subclass does define a method with a signature identical to the method in the superclass,so this is presumably understood. However, such methods are exceptionally confusing. You should strongly consider removing or deprecating the method with the similar but not identical signature.',
 'Method doesn''t override method in superclass due to wrong package for parameter', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_WRONG_PACKAGE_INTENTIONAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_WRONG_PACKAGE_INTENTIONAL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_BOOLEAN_RETURN_NULL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NP_BOOLEAN_RETURN_NULL', null, 'NP_BOOLEAN_RETURN_NULL', 'java', null, 'NP_BOOLEAN_RETURN_NULL', null, null, null, null, 'A method that returns either Boolean.TRUE, Boolean.FALSE or null is an accident waiting to happen. This method can be invoked as though it returned a value of type boolean, and the compiler will insert automatic unboxing of the Boolean value. If a null value is returned, this will result in a NullPointerException.', 'Method with Boolean return type returns explicit null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_BOOLEAN_RETURN_NULL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_BOOLEAN_RETURN_NULL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_CLONE_COULD_RETURN_NULL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NP_CLONE_COULD_RETURN_NULL', null, 'NP_CLONE_COULD_RETURN_NULL', 'java', null, 'NP_CLONE_COULD_RETURN_NULL', null, null, null, null, 'This clone method seems to return null in some circumstances, but clone is never allowed to return a null value. If you are convinced this path is unreachable, throw an AssertionError instead.', 'Clone method may return null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_CLONE_COULD_RETURN_NULL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_CLONE_COULD_RETURN_NULL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT', null, 'NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT', 'java', null, 'NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT', null, null, null, null, 'This implementation of equals(Object) violates the contract defined by java.lang.Object.equals() because it does not check for null being passed as the argument. All equals() methods should return false if passed a null value.', 'equals() method does not check for null argument', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_TOSTRING_COULD_RETURN_NULL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'NP_TOSTRING_COULD_RETURN_NULL', null, 'NP_TOSTRING_COULD_RETURN_NULL', 'java', null, 'NP_TOSTRING_COULD_RETURN_NULL', null, null, null, null, 'This toString method seems to return null in some circumstances. A liberal reading of the spec could be interpreted as allowing this, but it is probably a bad idea and could cause other code to break. Return the empty string or some other appropriate string rather than null.', 'toString method may return null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_TOSTRING_COULD_RETURN_NULL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_TOSTRING_COULD_RETURN_NULL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ODR_OPEN_DATABASE_RESOURCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'ODR_OPEN_DATABASE_RESOURCE', null, 'ODR_OPEN_DATABASE_RESOURCE', 'java', null, 'ODR_OPEN_DATABASE_RESOURCE', null, null, null, null, 'The method creates a database resource (such as a database connection or row set), does not assign it to any fields, pass it to other methods, or return it, and does not appear to close the object on all paths out of the method.&nbsp; Failure to close database resources on all paths out of a method may result in poor performance, and could cause the application to have problems communicating with the database.', 'Method may fail to close database resource', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ODR_OPEN_DATABASE_RESOURCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ODR_OPEN_DATABASE_RESOURCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ODR_OPEN_DATABASE_RESOURCE_EXCEPTION_PATH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'ODR_OPEN_DATABASE_RESOURCE_EXCEPTION_PATH', null, 'ODR_OPEN_DATABASE_RESOURCE_EXCEPTION_PATH', 'java', null, 'ODR_OPEN_DATABASE_RESOURCE_EXCEPTION_PATH', null, null, null, null, 'The method creates a database resource (such as a database connection or row set), does not assign it to any fields, pass it to other methods, or return it, and does not appear to close the object on all exception paths out of the method.&nbsp; Failure to close database resources on all paths out of a method may result in poor performance, and could cause the application to have problems communicating with the database.', 'Method may fail to close database resource on exception', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ODR_OPEN_DATABASE_RESOURCE_EXCEPTION_PATH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ODR_OPEN_DATABASE_RESOURCE_EXCEPTION_PATH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- OS_OPEN_STREAM
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'OS_OPEN_STREAM', null, 'OS_OPEN_STREAM', 'java', null, 'OS_OPEN_STREAM', null, null, null, null, 'The method creates an IO stream object, does not assign it to any fields, pass it to other methods that might close it,or return it, and does not appear to close the stream on all paths out of the method.&nbsp; This may result ina file descriptor leak.&nbsp; It is generally a good idea to use a <code>finally</code> block to ensure that streams are closed.', 'Method may fail to close stream', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='OS_OPEN_STREAM');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='OS_OPEN_STREAM'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- OS_OPEN_STREAM_EXCEPTION_PATH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'OS_OPEN_STREAM_EXCEPTION_PATH', null, 'OS_OPEN_STREAM_EXCEPTION_PATH', 'java', null, 'OS_OPEN_STREAM_EXCEPTION_PATH', null, null, null, null, 'The method creates an IO stream object, does not assign it to any fields, pass it to other methods, or return it, and does not appear to close it on all possible exception paths out of the method.&nbsp;This may result in a file descriptor leak.&nbsp; It is generally a good idea to use a <code>finally</code> block to ensure that streams are closed.', 'Method may fail to close stream on exception', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='OS_OPEN_STREAM_EXCEPTION_PATH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='OS_OPEN_STREAM_EXCEPTION_PATH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- PZ_DONT_REUSE_ENTRY_OBJECTS_IN_ITERATORS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'PZ_DONT_REUSE_ENTRY_OBJECTS_IN_ITERATORS', null, 'PZ_DONT_REUSE_ENTRY_OBJECTS_IN_ITERATORS', 'java', null, 'PZ_DONT_REUSE_ENTRY_OBJECTS_IN_ITERATORS', null, null, null, null, 'The entrySet() method is allowed to return a view of the underlying Map in which an Iterator and Map.Entry. This clever idea was used in several Map implementations, but introduces the possibility of nasty coding mistakes. If a map <code>m</code> returns such an iterator for an entrySet, then <code>c.addAll(m.entrySet())</code> will go badly wrong. All of the Map implementations in OpenJDK 1.7 have been rewritten to avoid this, you should to.', 'Don''t reuse entry objects in iterators', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PZ_DONT_REUSE_ENTRY_OBJECTS_IN_ITERATORS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PZ_DONT_REUSE_ENTRY_OBJECTS_IN_ITERATORS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RC_REF_COMPARISON_BAD_PRACTICE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'RC_REF_COMPARISON_BAD_PRACTICE', null, 'RC_REF_COMPARISON_BAD_PRACTICE', 'java', null, 'RC_REF_COMPARISON_BAD_PRACTICE', null, null, null, null, 'This method compares a reference value to a constant using the == or != operator,where the correct way to compare instances of this type is generally with the equals() method.It is possible to create distinct instances that are equal but do not compare as == since they are different objects.Examples of classes which should generally not be compared by reference are java.lang.Integer, java.lang.Float, etc.', 'Suspicious reference comparison to constant', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RC_REF_COMPARISON_BAD_PRACTICE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RC_REF_COMPARISON_BAD_PRACTICE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN', null, 'RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN', 'java', null, 'RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN', null, null, null, null, 'This method compares two Boolean values using the == or != operator.Normally, there are only two Boolean values (Boolean.TRUE and Boolean.FALSE),but it is possible to create other Boolean objects using the <code>new Boolean(b)</code>constructor. It is best to avoid such objects, but if they do exist,then checking Boolean objects for equality using == or != will give results than are different than you would get using <code>.equals(...)</code>.', 'Suspicious reference comparison of Boolean values', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RR_NOT_CHECKED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'RR_NOT_CHECKED', null, 'RR_NOT_CHECKED', 'java', null, 'RR_NOT_CHECKED', null, null, null, null, 'This method ignores the return value of one of the variants of <code>java.io.InputStream.read()</code> which can return multiple bytes.&nbsp; If the return value is not checked, the caller will not be able to correctly handle the case where fewer bytes were read than the caller requested.&nbsp; This is a particularly insidious kind of bug, because in many programs, reads from input streams usually do read the full amount of data requested, causing the program to fail only sporadically.', 'Method ignores results of InputStream.read()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RR_NOT_CHECKED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RR_NOT_CHECKED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_NEGATING_RESULT_OF_COMPARETO
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'RV_NEGATING_RESULT_OF_COMPARETO', null, 'RV_NEGATING_RESULT_OF_COMPARETO', 'java', null, 'RV_NEGATING_RESULT_OF_COMPARETO', null, null, null, null, 'This code negatives the return value of a compareTo or compare method.This is a questionable or bad programming practice, since if the return value is Integer.MIN_VALUE, negating the return value won''tnegate the sign of the result. You can achieve the same intended result by reversing the order of the operands rather than by negating the results.', 'Negating the result of compareTo()/compare()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_NEGATING_RESULT_OF_COMPARETO');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_NEGATING_RESULT_OF_COMPARETO'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_RETURN_VALUE_IGNORED_BAD_PRACTICE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'RV_RETURN_VALUE_IGNORED_BAD_PRACTICE', null, 'RV_RETURN_VALUE_IGNORED_BAD_PRACTICE', 'java', null, 'RV_RETURN_VALUE_IGNORED_BAD_PRACTICE', null, null, null, null, 'This method returns a value that is not checked. The return value should be checked since it can indicate an unusual or unexpected function execution. For example, the <code>File.delete()</code> method returns false if the file could not be successfully deleted (rather than throwing an Exception).If you don''t check the result, you won''t notice if the method invocation signals unexpected behavior by returning an atypical return value.', 'Method ignores exceptional return value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_IGNORED_BAD_PRACTICE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_IGNORED_BAD_PRACTICE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_IGNORED_BAD_PRACTICE'),
 'STANDARD', 'CWE','253')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_BAD_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_BAD_FIELD', null, 'SE_BAD_FIELD', 'java', null, 'SE_BAD_FIELD', null, null, null, null, 'This Serializable class defines a non-primitive instance field which is neither transient,Serializable, or <code>java.lang.Object</code>, and does not appear to implement the <code>Externalizable</code> interface or the<code>readObject()</code> and <code>writeObject()</code> methods.&nbsp;Objects of this class will not be deserialized correctly if a non-Serializable object is stored in this field.', 'Non-transient non-serializable instance field in serializable class', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_BAD_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_BAD_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_BAD_FIELD_INNER_CLASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_BAD_FIELD_INNER_CLASS', null, 'SE_BAD_FIELD_INNER_CLASS', 'java', null, 'SE_BAD_FIELD_INNER_CLASS', null, null, null, null, 'This Serializable class is an inner class of a non-serializable class.Thus, attempts to serialize it will also attempt to associate instance of the outer class with which it is associated, leading to a runtime error.If possible, making the inner class a static inner class should solve the problem. Making the outer class serializable might also work, but that wouldmean serializing an instance of the inner class would always also serialize the instanceof the outer class, which it often not what you really want.', 'Non-serializable class has a serializable inner class', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_BAD_FIELD_INNER_CLASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_BAD_FIELD_INNER_CLASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_BAD_FIELD_STORE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_BAD_FIELD_STORE', null, 'SE_BAD_FIELD_STORE', 'java', null, 'SE_BAD_FIELD_STORE', null, null, null, null, 'A non-serializable value is stored into a non-transient field of a serializable class.', 'Non-serializable value stored into instance field of a serializable class', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_BAD_FIELD_STORE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_BAD_FIELD_STORE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_COMPARATOR_SHOULD_BE_SERIALIZABLE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_COMPARATOR_SHOULD_BE_SERIALIZABLE', null, 'SE_COMPARATOR_SHOULD_BE_SERIALIZABLE', 'java', null, 'SE_COMPARATOR_SHOULD_BE_SERIALIZABLE', null, null, null, null, 'This class implements the <code>Comparator</code> interface. You should consider whether or not it should also implement the <code>Serializable</code>interface. If a comparator is used to construct an ordered collection such as a <code>TreeMap</code>, then the <code>TreeMap</code>will be serializable only if the comparator is also serializable.As most comparators have little or no state, making them serializable is generally easy and good defensive programming.', 'Comparator doesn''t implement Serializable', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_COMPARATOR_SHOULD_BE_SERIALIZABLE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_COMPARATOR_SHOULD_BE_SERIALIZABLE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_INNER_CLASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_INNER_CLASS', null, 'SE_INNER_CLASS', 'java', null, 'SE_INNER_CLASS', null, null, null, null, 'This Serializable class is an inner class. Any attempt to serialize it will also serialize the associated outer instance. The outer instance is serializable,so this won''t fail, but it might serialize a lot more data than intended.If possible, making the inner class a static inner class (also known as a nested class) should solve the problem.', 'Serializable inner class', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_INNER_CLASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_INNER_CLASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_NONFINAL_SERIALVERSIONID
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_NONFINAL_SERIALVERSIONID', null, 'SE_NONFINAL_SERIALVERSIONID', 'java', null, 'SE_NONFINAL_SERIALVERSIONID', null, null, null, null, 'This class defines a <code>serialVersionUID</code> field that is not final.&nbsp; The field should be made final if it is intended to specify the version UID for purposes of serialization.', 'serialVersionUID isn''t final', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NONFINAL_SERIALVERSIONID');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NONFINAL_SERIALVERSIONID'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_NONLONG_SERIALVERSIONID
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_NONLONG_SERIALVERSIONID', null, 'SE_NONLONG_SERIALVERSIONID', 'java', null, 'SE_NONLONG_SERIALVERSIONID', null, null, null, null, 'This class defines a <code>serialVersionUID</code> field that is not long.&nbsp; The field should be made long if it is intended to specify the version UID for purposes of serialization.', 'serialVersionUID isn''t long', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NONLONG_SERIALVERSIONID');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NONLONG_SERIALVERSIONID'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_NONSTATIC_SERIALVERSIONID
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_NONSTATIC_SERIALVERSIONID', null, 'SE_NONSTATIC_SERIALVERSIONID', 'java', null, 'SE_NONSTATIC_SERIALVERSIONID', null, null, null, null, 'This class defines a <code>serialVersionUID</code> field that is not static.&nbsp; The field should be made static if it is intended to specify the version UID for purposes of serialization.', 'serialVersionUID isn''t static', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NONSTATIC_SERIALVERSIONID');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NONSTATIC_SERIALVERSIONID'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_NO_SERIALVERSIONID
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_NO_SERIALVERSIONID', null, 'SE_NO_SERIALVERSIONID', 'java', null, 'SE_NO_SERIALVERSIONID', null, null, null, null, 'This class implements the <code>Serializable</code> interface, but does not define a <code>serialVersionUID</code> field.&nbsp; A change as simple as adding a reference to a .class object will add synthetic fields to the class, which will unfortunately change the implicit serialVersionUID (e.g., adding a reference to <code>String.class</code> will generate a static field <code>class$java$lang$String</code>). Also, different source code to bytecode compilers may use different naming conventions for synthetic variables generated for references to class objects or inner classes. To ensure interoperability of Serializable across versions, consider adding an explicit serialVersionUID.', 'Class is Serializable, but doesn''t define serialVersionUID', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NO_SERIALVERSIONID');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NO_SERIALVERSIONID'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_NO_SUITABLE_CONSTRUCTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_NO_SUITABLE_CONSTRUCTOR', null, 'SE_NO_SUITABLE_CONSTRUCTOR', 'java', null, 'SE_NO_SUITABLE_CONSTRUCTOR', null, null, null, null, 'This class implements the <code>Serializable</code> interface and its superclass does not. When such an object is deserialized, the fields of the superclass need to be initialized by invoking the void constructor of the superclass. Since the superclass does not have one, serialization and deserialization will fail at runtime.', 'Class is Serializable but its superclass doesn''t define a void constructor', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NO_SUITABLE_CONSTRUCTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NO_SUITABLE_CONSTRUCTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_NO_SUITABLE_CONSTRUCTOR_FOR_EXTERNALIZATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_NO_SUITABLE_CONSTRUCTOR_FOR_EXTERNALIZATION', null, 'SE_NO_SUITABLE_CONSTRUCTOR_FOR_EXTERNALIZATION', 'java', null, 'SE_NO_SUITABLE_CONSTRUCTOR_FOR_EXTERNALIZATION', null, null, null, null, 'This class implements the <code>Externalizable</code> interface, but does not define a void constructor. When Externalizable objects are deserialized, they first need to be constructed by invoking the void constructor. Since this class does not have one, serialization and deserialization will fail at runtime.', 'Class is Externalizable but doesn''t define a void constructor', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NO_SUITABLE_CONSTRUCTOR_FOR_EXTERNALIZATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_NO_SUITABLE_CONSTRUCTOR_FOR_EXTERNALIZATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_READ_RESOLVE_MUST_RETURN_OBJECT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_READ_RESOLVE_MUST_RETURN_OBJECT', null, 'SE_READ_RESOLVE_MUST_RETURN_OBJECT', 'java', null, 'SE_READ_RESOLVE_MUST_RETURN_OBJECT', null, null, null, null, 'In order for the readResolve method to be recognized by the serialization mechanism, it must be declared to have a return type of Object.', 'The readResolve method must be declared with a return type of Object.', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_READ_RESOLVE_MUST_RETURN_OBJECT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_READ_RESOLVE_MUST_RETURN_OBJECT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_TRANSIENT_FIELD_NOT_RESTORED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SE_TRANSIENT_FIELD_NOT_RESTORED', null, 'SE_TRANSIENT_FIELD_NOT_RESTORED', 'java', null, 'SE_TRANSIENT_FIELD_NOT_RESTORED', null, null, null, null, 'This class contains a field that is updated at multiple places in the class, thus it seems to be part of the state of the class. However, since the field is marked as transient and not set in readObject or readResolve, it will contain the default value in any deserialized instance of the class.', 'Transient field that isn''t set by deserialization.', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_TRANSIENT_FIELD_NOT_RESTORED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_TRANSIENT_FIELD_NOT_RESTORED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SI_INSTANCE_BEFORE_FINALS_ASSIGNED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SI_INSTANCE_BEFORE_FINALS_ASSIGNED', null, 'SI_INSTANCE_BEFORE_FINALS_ASSIGNED', 'java', null, 'SI_INSTANCE_BEFORE_FINALS_ASSIGNED', null, null, null, null, 'The class''s static initializer creates an instance of the class before all of the static final fields are assigned.', 'Static initializer creates instance before all static final fields assigned', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SI_INSTANCE_BEFORE_FINALS_ASSIGNED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SI_INSTANCE_BEFORE_FINALS_ASSIGNED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SR_NOT_CHECKED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SR_NOT_CHECKED', null, 'SR_NOT_CHECKED', 'java', null, 'SR_NOT_CHECKED', null, null, null, null, 'This method ignores the return value of <code>java.io.InputStream.skip()</code> which can skip multiple bytes.&nbsp; If the return value is not checked, the caller will not be able to correctly handle the case where fewer bytes were skipped than the caller requested.&nbsp; This is a particularly insidious kind of bug, because in many programs, skips from input streams usually do skip the full amount of data requested, causing the program to fail only sporadically. With Buffered streams, however, skip() will only skip data in the buffer, and will routinely fail to skip the requested number of bytes.', 'Method ignores results of InputStream.skip()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SR_NOT_CHECKED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SR_NOT_CHECKED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SW_SWING_METHODS_INVOKED_IN_SWING_THREAD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'SW_SWING_METHODS_INVOKED_IN_SWING_THREAD', null, 'SW_SWING_METHODS_INVOKED_IN_SWING_THREAD', 'java', null, 'SW_SWING_METHODS_INVOKED_IN_SWING_THREAD', null, null, null, null, '(<a href="http://web.archive.org/web/20090526170426/http://java.sun.com/developer/JDCTechTips/2003/tt1208.html">From JDC Tech Tip</a>): The Swing methods show(), setVisible(), and pack() will create the associated peer for the frame.With the creation of the peer, the system creates the event dispatch thread.This makes things problematic because the event dispatch thread could be notifying listeners while pack and validate are still processing. This situation could result in two threads going through the Swing component-based GUI -- it''s a serious flaw that could result in deadlocks or other related threading issues. A pack call causes components to be realized. As they are being realized (that is, not necessarily visible), they could trigger listener notification on the event dispatch thread.', 'Certain swing methods needs to be invoked in Swing thread', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SW_SWING_METHODS_INVOKED_IN_SWING_THREAD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SW_SWING_METHODS_INVOKED_IN_SWING_THREAD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UI_INHERITANCE_UNSAFE_GETRESOURCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'UI_INHERITANCE_UNSAFE_GETRESOURCE', null, 'UI_INHERITANCE_UNSAFE_GETRESOURCE', 'java', null, 'UI_INHERITANCE_UNSAFE_GETRESOURCE', null, null, null, null, 'Calling <code>this.getClass().getResource(...)</code> could give results other than expected if this class is extended by a class in another package.', 'Usage of GetResource may be unsafe if class is extended', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UI_INHERITANCE_UNSAFE_GETRESOURCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UI_INHERITANCE_UNSAFE_GETRESOURCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- VA_FORMAT_STRING_USES_NEWLINE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'BAD_PRACTICE', 'VA_FORMAT_STRING_USES_NEWLINE', null, 'VA_FORMAT_STRING_USES_NEWLINE', 'java', null, 'VA_FORMAT_STRING_USES_NEWLINE', null, null, null, null, 'This format string includes a newline character (\n). In format strings, it is generally preferable to use %n, which will produce the platform-specific line separator.', 'Format string should use %n rather than \n', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='VA_FORMAT_STRING_USES_NEWLINE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='VA_FORMAT_STRING_USES_NEWLINE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


