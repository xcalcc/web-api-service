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
-- BAC_BAD_APPLET_CONSTRUCTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BAC_BAD_APPLET_CONSTRUCTOR', null, 'BAC_BAD_APPLET_CONSTRUCTOR', 'java', null, 'BAC_BAD_APPLET_CONSTRUCTOR', null, null, null, null, 'This constructor calls methods in the parent Applet that rely on the AppletStub. Since the AppletStub isn''t initialized until the init() method of this applet is called, these methods will not perform correctly.', 'Bad Applet Constructor relies on uninitialized AppletStub', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BAC_BAD_APPLET_CONSTRUCTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BAC_BAD_APPLET_CONSTRUCTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BC_IMPOSSIBLE_CAST
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BC_IMPOSSIBLE_CAST', null, 'BC_IMPOSSIBLE_CAST', 'java', null, 'BC_IMPOSSIBLE_CAST', null, null, null, null, 'This cast will always throw a ClassCastException.SpotBugs tracks type information from instanceof checks,and also uses more precise information about the types of values returned from methods and loaded from fields.Thus, it may have more precise information that just the declared type of a variable, and can use this to determine that a cast will always throw an exception at runtime.', 'Impossible cast', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_CAST');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_CAST'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_CAST'),
 'STANDARD', 'CWE','570')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BC_IMPOSSIBLE_CAST_PRIMITIVE_ARRAY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BC_IMPOSSIBLE_CAST_PRIMITIVE_ARRAY', null, 'BC_IMPOSSIBLE_CAST_PRIMITIVE_ARRAY', 'java', null, 'BC_IMPOSSIBLE_CAST_PRIMITIVE_ARRAY', null, null, null, null, 'This cast will always throw a ClassCastException.', 'Impossible cast involving primitive array', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_CAST_PRIMITIVE_ARRAY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_CAST_PRIMITIVE_ARRAY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BC_IMPOSSIBLE_DOWNCAST
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BC_IMPOSSIBLE_DOWNCAST', null, 'BC_IMPOSSIBLE_DOWNCAST', 'java', null, 'BC_IMPOSSIBLE_DOWNCAST', null, null, null, null, 'This cast will always throw a ClassCastException.The analysis believes it knows the precise type of the value being cast, and the attempt to downcast it to a subtype will always fail by throwing a ClassCastException.', 'Impossible downcast', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_DOWNCAST');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_DOWNCAST'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_DOWNCAST'),
 'STANDARD', 'CWE','570')
ON CONFLICT DO NOTHING;

-- ------------------------
-- BC_IMPOSSIBLE_DOWNCAST_OF_TOARRAY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BC_IMPOSSIBLE_DOWNCAST_OF_TOARRAY', null, 'BC_IMPOSSIBLE_DOWNCAST_OF_TOARRAY', 'java', null, 'BC_IMPOSSIBLE_DOWNCAST_OF_TOARRAY', null, null, null, null, 'This code is casting the result of calling <code>toArray()</code> on a collection to a type more specific than <code>Object[]</code>, as in:<pre><code>String[] getAsArray(Collection&lt;String&gt; c) { return (String[]) c.toArray();}</code></pre>This will usually fail by throwing a ClassCastException. The <code>toArray()</code>of almost all collections return an <code>Object[]</code>. They can''t really do anything else,since the Collection object has no reference to the declared generic type of the collection.The correct way to do get an array of a specific type from a collection is to use <code>c.toArray(new String[]);</code> or <code>c.toArray(new String[c.size()]);</code> (the latter is slightly more efficient).There is one common/known exception to this. The <code>toArray()</code>method of lists returned by <code>Arrays.asList(...)</code> will return a covariantly typed array. For example, <code>Arrays.asArray(new String[] { "a" }).toArray()</code>will return a <code>String []</code>. SpotBugs attempts to detect and suppress such cases, but may miss some.', 'Impossible downcast of toArray() result', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_DOWNCAST_OF_TOARRAY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_DOWNCAST_OF_TOARRAY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_DOWNCAST_OF_TOARRAY'),
 'STANDARD', 'CWE','570')
ON CONFLICT DO NOTHING;

-- ------------------------
-- BC_IMPOSSIBLE_INSTANCEOF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BC_IMPOSSIBLE_INSTANCEOF', null, 'BC_IMPOSSIBLE_INSTANCEOF', 'java', null, 'BC_IMPOSSIBLE_INSTANCEOF', null, null, null, null, 'This instanceof test will always return false. Although this is safe, make sure it isn''tan indication of some misunderstanding or some other logic error.', 'instanceof will always return false', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_INSTANCEOF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_INSTANCEOF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_IMPOSSIBLE_INSTANCEOF'),
 'STANDARD', 'CWE','570')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BC_NULL_INSTANCEOF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BC_NULL_INSTANCEOF', null, 'BC_NULL_INSTANCEOF', 'java', null, 'BC_NULL_INSTANCEOF', null, null, null, null, 'This instanceof test will always return false, since the value being checked is guaranteed to be null.Although this is safe, make sure it isn''tan indication of some misunderstanding or some other logic error.', 'A known null value is checked to see if it is an instance of a type', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_NULL_INSTANCEOF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_NULL_INSTANCEOF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BIT_ADD_OF_SIGNED_BYTE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BIT_ADD_OF_SIGNED_BYTE', null, 'BIT_ADD_OF_SIGNED_BYTE', 'java', null, 'BIT_ADD_OF_SIGNED_BYTE', null, null, null, null, 'Adds a byte value and a value which is known to have the 8 lower bits clear.Values loaded from a byte array are sign extended to 32 bits before any bitwise operations are performed on the value.Thus, if <code>b[0]</code> contains the value <code>0xff</code>, and<code>x</code> is initially 0, then the code<code>((x &lt;&lt; 8) + b[0])</code> will sign extend <code>0xff</code>to get <code>0xffffffff</code>, and thus give the value<code>0xffffffff</code> as the result.In particular, the following code for packing a byte array into an int is badly wrong: <pre><code>int result = 0;for(int i = 0; i &lt; 4; i++) result = ((result &lt;&lt; 8) + b[i]);</code></pre>The following idiom will work instead: <pre><code>int result = 0;for(int i = 0; i &lt; 4; i++) result = ((result &lt;&lt; 8) + (b[i] &amp; 0xff));</code></pre>', 'Bitwise add of signed byte value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_ADD_OF_SIGNED_BYTE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_ADD_OF_SIGNED_BYTE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BIT_AND
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BIT_AND', null, 'BIT_AND', 'java', null, 'BIT_AND', null, null, null, null, 'This method compares an expression of the form (e &amp; C) to D,which will always compare unequal due to the specific values of constants C and D.This may indicate a logic error or typo.', 'Incompatible bit masks', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_AND');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_AND'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BIT_AND_ZZ
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BIT_AND_ZZ', null, 'BIT_AND_ZZ', 'java', null, 'BIT_AND_ZZ', null, null, null, null, 'This method compares an expression of the form <code>(e &amp; 0)</code> to 0,which will always compare equal.This may indicate a logic error or typo.', 'Check to see if ((...) & 0) == 0', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_AND_ZZ');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_AND_ZZ'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BIT_IOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BIT_IOR', null, 'BIT_IOR', 'java', null, 'BIT_IOR', null, null, null, null, 'This method compares an expression of the form <code>(e | C)</code> to D.which will always compare unequal due to the specific values of constants C and D.This may indicate a logic error or typo. Typically, this bug occurs because the code wants to perform a membership test in a bit set, but uses the bitwise OR operator ("|") instead of bitwise AND ("&amp;").Also such bug may appear in expressions like <code>(e &amp; A | B) == C</code>which is parsed like <code>((e &amp; A) | B) == C</code> while <code>(e &amp; (A | B)) == C</code> was intended.', 'Incompatible bit masks', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_IOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_IOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BIT_IOR_OF_SIGNED_BYTE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BIT_IOR_OF_SIGNED_BYTE', null, 'BIT_IOR_OF_SIGNED_BYTE', 'java', null, 'BIT_IOR_OF_SIGNED_BYTE', null, null, null, null, 'Loads a byte value (e.g., a value loaded from a byte array or returned by a method with return type byte) and performs a bitwise OR with that value. Byte values are sign extended to 32 bits before any bitwise operations are performed on the value.Thus, if <code>b[0]</code> contains the value <code>0xff</code>, and<code>x</code> is initially 0, then the code<code>((x &lt;&lt; 8) | b[0])</code> will sign extend <code>0xff</code>to get <code>0xffffffff</code>, and thus give the value<code>0xffffffff</code> as the result.In particular, the following code for packing a byte array into an int is badly wrong: <pre><code>int result = 0;for(int i = 0; i &lt; 4; i++) { result = ((result &lt;&lt; 8) | b[i]);}</code></pre>The following idiom will work instead: <pre><code>int result = 0;for(int i = 0; i &lt; 4; i++) { result = ((result &lt;&lt; 8) | (b[i] &amp; 0xff));}</code></pre>', 'Bitwise OR of signed byte value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_IOR_OF_SIGNED_BYTE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_IOR_OF_SIGNED_BYTE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BIT_SIGNED_CHECK_HIGH_BIT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BIT_SIGNED_CHECK_HIGH_BIT', null, 'BIT_SIGNED_CHECK_HIGH_BIT', 'java', null, 'BIT_SIGNED_CHECK_HIGH_BIT', null, null, null, null, 'This method compares a bitwise expression such as<code>((val &amp; CONSTANT) &gt; 0)</code> where CONSTANT is the negative number.Using bit arithmetic and then comparing with the greater than operator can lead to unexpected results. This comparison is unlikely to work as expected. The good practice is to use ''!= 0'' instead of ''&gt; 0''.', 'Check for sign of bitwise operation involving negative number', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_SIGNED_CHECK_HIGH_BIT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BIT_SIGNED_CHECK_HIGH_BIT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BOA_BADLY_OVERRIDDEN_ADAPTER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BOA_BADLY_OVERRIDDEN_ADAPTER', null, 'BOA_BADLY_OVERRIDDEN_ADAPTER', 'java', null, 'BOA_BADLY_OVERRIDDEN_ADAPTER', null, null, null, null, 'This method overrides a method found in a parent class, where that class is an Adapter that implements a listener defined in the java.awt.event or javax.swing.event package. As a result, this method will not get called when the event occurs.', 'Class overrides a method implemented in super class Adapter wrongly', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BOA_BADLY_OVERRIDDEN_ADAPTER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BOA_BADLY_OVERRIDDEN_ADAPTER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BRSA_BAD_RESULTSET_ACCESS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BRSA_BAD_RESULTSET_ACCESS', null, 'BRSA_BAD_RESULTSET_ACCESS', 'java', null, 'BRSA_BAD_RESULTSET_ACCESS', null, null, null, null, 'A call to getXXX or updateXXX methods of a result set was made where the field index is 0. As ResultSet fields start at index 1, this is always a mistake.', 'Method attempts to access a result set field with index 0', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BRSA_BAD_RESULTSET_ACCESS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BRSA_BAD_RESULTSET_ACCESS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BSHIFT_WRONG_ADD_PRIORITY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'BSHIFT_WRONG_ADD_PRIORITY', null, 'BSHIFT_WRONG_ADD_PRIORITY', 'java', null, 'BSHIFT_WRONG_ADD_PRIORITY', null, null, null, null, 'The code performs an operation like (x &lt;&lt; 8 + y). Although this might be correct, probably it was meant to perform (x &lt;&lt; 8) + y, but shift operation has a lower precedence, so it''s actually parsed as x &lt;&lt; (8 + y).', 'Possible bad parsing of shift operation', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BSHIFT_WRONG_ADD_PRIORITY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BSHIFT_WRONG_ADD_PRIORITY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CAA_COVARIANT_ARRAY_ELEMENT_STORE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'CAA_COVARIANT_ARRAY_ELEMENT_STORE', null, 'CAA_COVARIANT_ARRAY_ELEMENT_STORE', 'java', null, 'CAA_COVARIANT_ARRAY_ELEMENT_STORE', null, null, null, null, 'Value is stored into the array and the value type doesn''t match the array type.It''s known from the analysis that actual array type is narrower than the declared type of its variable or field and this assignment doesn''t satisfy the original array type. This assignment may cause ArrayStoreExceptionat runtime.', 'Possibly incompatible element is stored in covariant array', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CAA_COVARIANT_ARRAY_ELEMENT_STORE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CAA_COVARIANT_ARRAY_ELEMENT_STORE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DLS_DEAD_LOCAL_INCREMENT_IN_RETURN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DLS_DEAD_LOCAL_INCREMENT_IN_RETURN', null, 'DLS_DEAD_LOCAL_INCREMENT_IN_RETURN', 'java', null, 'DLS_DEAD_LOCAL_INCREMENT_IN_RETURN', null, null, null, null, 'This statement has a return such as <code>return x++;</code>.A postfix increment/decrement does not impact the value of the expression,so this increment/decrement has no effect.Please verify that this statement does the right thing.', 'Useless increment in return statement', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_LOCAL_INCREMENT_IN_RETURN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_LOCAL_INCREMENT_IN_RETURN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DLS_DEAD_STORE_OF_CLASS_LITERAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DLS_DEAD_STORE_OF_CLASS_LITERAL', null, 'DLS_DEAD_STORE_OF_CLASS_LITERAL', 'java', null, 'DLS_DEAD_STORE_OF_CLASS_LITERAL', null, null, null, null, 'This instruction assigns a class literal to a variable and then never uses it.<a href="http://www.oracle.com/technetwork/java/javase/compatibility-137462.html#literal">The behavior of this differs in Java 1.4 and in Java 5.</a>In Java 1.4 and earlier, a reference to <code>Foo.class</code> would force the static initializer for <code>Foo</code> to be executed, if it has not been executed already.In Java 5 and later, it does not.See Sun''s <a href="http://www.oracle.com/technetwork/java/javase/compatibility-137462.html#literal">article on Java SE compatibility</a>for more details and examples, and suggestions on how to force class initialization in Java 5.', 'Dead store of class literal', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_STORE_OF_CLASS_LITERAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_STORE_OF_CLASS_LITERAL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DLS_OVERWRITTEN_INCREMENT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DLS_OVERWRITTEN_INCREMENT', null, 'DLS_OVERWRITTEN_INCREMENT', 'java', null, 'DLS_OVERWRITTEN_INCREMENT', null, null, null, null, 'The code performs an increment operation (e.g., <code>i++</code>) and then immediately overwrites it. For example, <code>i = i++</code> immediately overwrites the incremented value with the original value.', 'Overwritten increment', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_OVERWRITTEN_INCREMENT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_OVERWRITTEN_INCREMENT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_ANNOTATION_IS_NOT_VISIBLE_TO_REFLECTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_ANNOTATION_IS_NOT_VISIBLE_TO_REFLECTION', null, 'DMI_ANNOTATION_IS_NOT_VISIBLE_TO_REFLECTION', 'java', null, 'DMI_ANNOTATION_IS_NOT_VISIBLE_TO_REFLECTION', null, null, null, null, 'Unless an annotation has itself been annotated with @Retention(RetentionPolicy.RUNTIME), the annotation can''t be observed using reflection(e.g., by using the isAnnotationPresent method). .', 'Can''t use reflection to check for presence of annotation without runtime retention', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_ANNOTATION_IS_NOT_VISIBLE_TO_REFLECTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_ANNOTATION_IS_NOT_VISIBLE_TO_REFLECTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_ARGUMENTS_WRONG_ORDER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_ARGUMENTS_WRONG_ORDER', null, 'DMI_ARGUMENTS_WRONG_ORDER', 'java', null, 'DMI_ARGUMENTS_WRONG_ORDER', null, null, null, null, 'The arguments to this method call seem to be in the wrong order.For example, a call <code>Preconditions.checkNotNull("message", message)</code>has reserved arguments: the value to be checked is the first argument.', 'Reversed method arguments', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_ARGUMENTS_WRONG_ORDER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_ARGUMENTS_WRONG_ORDER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_BAD_MONTH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_BAD_MONTH', null, 'DMI_BAD_MONTH', 'java', null, 'DMI_BAD_MONTH', null, null, null, null, 'This code passes a constant month value outside the expected range of 0..11 to a method.', 'Bad constant value for month', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_BAD_MONTH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_BAD_MONTH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_BIGDECIMAL_CONSTRUCTED_FROM_DOUBLE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_BIGDECIMAL_CONSTRUCTED_FROM_DOUBLE', null, 'DMI_BIGDECIMAL_CONSTRUCTED_FROM_DOUBLE', 'java', null, 'DMI_BIGDECIMAL_CONSTRUCTED_FROM_DOUBLE', null, null, null, null, 'This code creates a BigDecimal from a double value that doesn''t translate well to a decimal number.For example, one might assume that writing new BigDecimal(0.1) in Java creates a BigDecimal which is exactly equal to 0.1 (an unscaled value of 1, with a scale of 1), but it is actually equal to 0.1000000000000000055511151231257827021181583404541015625.You probably want to use the BigDecimal.valueOf(double d) method, which uses the String representation of the double to create the BigDecimal (e.g., BigDecimal.valueOf(0.1) gives 0.1).', 'BigDecimal constructed from double that isn''t represented precisely', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_BIGDECIMAL_CONSTRUCTED_FROM_DOUBLE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_BIGDECIMAL_CONSTRUCTED_FROM_DOUBLE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_CALLING_NEXT_FROM_HASNEXT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_CALLING_NEXT_FROM_HASNEXT', null, 'DMI_CALLING_NEXT_FROM_HASNEXT', 'java', null, 'DMI_CALLING_NEXT_FROM_HASNEXT', null, null, null, null, 'The hasNext() method invokes the next() method. This is almost certainly wrong,since the hasNext() method is not supposed to change the state of the iterator,and the next method is supposed to change the state of the iterator.', 'hasNext method invokes next', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_CALLING_NEXT_FROM_HASNEXT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_CALLING_NEXT_FROM_HASNEXT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_COLLECTIONS_SHOULD_NOT_CONTAIN_THEMSELVES
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_COLLECTIONS_SHOULD_NOT_CONTAIN_THEMSELVES', null, 'DMI_COLLECTIONS_SHOULD_NOT_CONTAIN_THEMSELVES', 'java', null, 'DMI_COLLECTIONS_SHOULD_NOT_CONTAIN_THEMSELVES', null, null, null, null, 'This call to a generic collection''s method would only make sense if a collection contained itself (e.g., if <code>s.contains(s)</code> were true). This is unlikely to be true and would cause problems if it were true (such as the computation of the hash code resulting in infinite recursion).It is likely that the wrong value is being passed as a parameter.', 'Collections should not contain themselves', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_COLLECTIONS_SHOULD_NOT_CONTAIN_THEMSELVES');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_COLLECTIONS_SHOULD_NOT_CONTAIN_THEMSELVES'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_DOH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_DOH', null, 'DMI_DOH', 'java', null, 'DMI_DOH', null, null, null, null, 'This partical method invocation doesn''t make sense, for reasons that should be apparent from inspection.', 'D''oh! A nonsensical method invocation', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_DOH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_DOH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_FUTILE_ATTEMPT_TO_CHANGE_MAXPOOL_SIZE_OF_SCHEDULED_THREAD_POOL_EXECUTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_FUTILE_ATTEMPT_TO_CHANGE_MAXPOOL_SIZE_OF_SCHEDULED_THREAD_POOL_EXECUTOR', null, 'DMI_FUTILE_ATTEMPT_TO_CHANGE_MAXPOOL_SIZE_OF_SCHEDULED_THREAD_POOL_EXECUTOR', 'java', null, 'DMI_FUTILE_ATTEMPT_TO_CHANGE_MAXPOOL_SIZE_OF_SCHEDULED_THREAD_POOL_EXECUTOR', null, null, null, null, '(<a href="http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ScheduledThreadPoolExecutor.html">Javadoc</a>)While ScheduledThreadPoolExecutor inherits from ThreadPoolExecutor, a few of the inherited tuning methods are not useful for it. In particular, because it acts as a fixed-sized pool using corePoolSize threads and an unbounded queue, adjustments to maximumPoolSize have no useful effect.', 'Futile attempt to change max pool size of ScheduledThreadPoolExecutor', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_FUTILE_ATTEMPT_TO_CHANGE_MAXPOOL_SIZE_OF_SCHEDULED_THREAD_POOL_EXECUTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_FUTILE_ATTEMPT_TO_CHANGE_MAXPOOL_SIZE_OF_SCHEDULED_THREAD_POOL_EXECUTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_INVOKING_HASHCODE_ON_ARRAY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_INVOKING_HASHCODE_ON_ARRAY', null, 'DMI_INVOKING_HASHCODE_ON_ARRAY', 'java', null, 'DMI_INVOKING_HASHCODE_ON_ARRAY', null, null, null, null, 'The code invokes hashCode on an array. Calling hashCode on an array returns the same value as System.identityHashCode, and ignores the contents and length of the array. If you need a hashCode that depends on the contents of an array <code>a</code>,use <code>java.util.Arrays.hashCode(a)</code>.', 'Invocation of hashCode on an array', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_INVOKING_HASHCODE_ON_ARRAY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_INVOKING_HASHCODE_ON_ARRAY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_INVOKING_TOSTRING_ON_ANONYMOUS_ARRAY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_INVOKING_TOSTRING_ON_ANONYMOUS_ARRAY', null, 'DMI_INVOKING_TOSTRING_ON_ANONYMOUS_ARRAY', 'java', null, 'DMI_INVOKING_TOSTRING_ON_ANONYMOUS_ARRAY', null, null, null, null, 'The code invokes toString on an (anonymous) array. Calling toString on an array generates a fairly useless result such as [C@16f0472. Consider using Arrays.toString to convert the array into a readableString that gives the contents of the array. See Programming Puzzlers, chapter 3, puzzle 12.', 'Invocation of toString on an unnamed array', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_INVOKING_TOSTRING_ON_ANONYMOUS_ARRAY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_INVOKING_TOSTRING_ON_ANONYMOUS_ARRAY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_INVOKING_TOSTRING_ON_ARRAY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_INVOKING_TOSTRING_ON_ARRAY', null, 'DMI_INVOKING_TOSTRING_ON_ARRAY', 'java', null, 'DMI_INVOKING_TOSTRING_ON_ARRAY', null, null, null, null, 'The code invokes toString on an array, which will generate a fairly useless result such as [C@16f0472. Consider using Arrays.toString to convert the array into a readableString that gives the contents of the array. See Programming Puzzlers, chapter 3, puzzle 12.', 'Invocation of toString on an array', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_INVOKING_TOSTRING_ON_ARRAY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_INVOKING_TOSTRING_ON_ARRAY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT', null, 'DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT', 'java', null, 'DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT', null, null, null, null, 'The Double.longBitsToDouble method is invoked, but a 32 bit int value is passed as an argument. This almost certainly is not intended and is unlikely to give the intended result.', 'Double.longBitsToDouble invoked on an int', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_SCHEDULED_THREAD_POOL_EXECUTOR_WITH_ZERO_CORE_THREADS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_SCHEDULED_THREAD_POOL_EXECUTOR_WITH_ZERO_CORE_THREADS', null, 'DMI_SCHEDULED_THREAD_POOL_EXECUTOR_WITH_ZERO_CORE_THREADS', 'java', null, 'DMI_SCHEDULED_THREAD_POOL_EXECUTOR_WITH_ZERO_CORE_THREADS', null, null, null, null, '(<a href="http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ScheduledThreadPoolExecutor.html#ScheduledThreadPoolExecutor%28int%29">Javadoc</a>)A ScheduledThreadPoolExecutor with zero core threads will never execute anything; changes to the max pool size are ignored.', 'Creation of ScheduledThreadPoolExecutor with zero core threads', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_SCHEDULED_THREAD_POOL_EXECUTOR_WITH_ZERO_CORE_THREADS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_SCHEDULED_THREAD_POOL_EXECUTOR_WITH_ZERO_CORE_THREADS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_VACUOUS_CALL_TO_EASYMOCK_METHOD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_VACUOUS_CALL_TO_EASYMOCK_METHOD', null, 'DMI_VACUOUS_CALL_TO_EASYMOCK_METHOD', 'java', null, 'DMI_VACUOUS_CALL_TO_EASYMOCK_METHOD', null, null, null, null, 'This call doesn''t pass any objects to the EasyMock method, so the call doesn''t do anything.', 'Useless/vacuous call to EasyMock method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_VACUOUS_CALL_TO_EASYMOCK_METHOD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_VACUOUS_CALL_TO_EASYMOCK_METHOD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_VACUOUS_SELF_COLLECTION_CALL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DMI_VACUOUS_SELF_COLLECTION_CALL', null, 'DMI_VACUOUS_SELF_COLLECTION_CALL', 'java', null, 'DMI_VACUOUS_SELF_COLLECTION_CALL', null, null, null, null, 'This call doesn''t make sense. For any collection <code>c</code>, calling <code>c.containsAll(c)</code> should always be true, and <code>c.retainAll(c)</code> should have no effect.', 'Vacuous call to collections', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_VACUOUS_SELF_COLLECTION_CALL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_VACUOUS_SELF_COLLECTION_CALL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_INVALID_MIN_MAX
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'DM_INVALID_MIN_MAX', null, 'DM_INVALID_MIN_MAX', 'java', null, 'DM_INVALID_MIN_MAX', null, null, null, null, 'This code tries to limit the value bounds using the construct like Math.min(0, Math.max(100, value)). However the order of the constants is incorrect: it should be Math.min(100, Math.max(0, value)). As the result this code always produces the same result (or NaN if the value is NaN).', 'Incorrect combination of Math.max and Math.min', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_INVALID_MIN_MAX');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_INVALID_MIN_MAX'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EC_ARRAY_AND_NONARRAY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EC_ARRAY_AND_NONARRAY', null, 'EC_ARRAY_AND_NONARRAY', 'java', null, 'EC_ARRAY_AND_NONARRAY', null, null, null, null, 'This method invokes the .equals(Object o) to compare an array and a reference that doesn''t seem to be an array. If things being compared are of different types, they are guaranteed to be unequal and the comparison is almost certainly an error. Even if they are both arrays, the equals method on arrays only determines of the two arrays are the same object.To compare the contents of the arrays, use java.util.Arrays.equals(Object[], Object[]).', 'equals() used to compare array and nonarray', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_ARRAY_AND_NONARRAY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_ARRAY_AND_NONARRAY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EC_BAD_ARRAY_COMPARE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EC_BAD_ARRAY_COMPARE', null, 'EC_BAD_ARRAY_COMPARE', 'java', null, 'EC_BAD_ARRAY_COMPARE', null, null, null, null, 'This method invokes the .equals(Object o) method on an array. Since arrays do not override the equals method of Object, calling equals on an array is the same as comparing their addresses. To compare the contents of the arrays, use <code>java.util.Arrays.equals(Object[], Object[])</code>.To compare the addresses of the arrays, it would be less confusing to explicitly check pointer equality using <code>==</code>.', 'Invocation of equals() on an array, which is equivalent to ==', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_BAD_ARRAY_COMPARE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_BAD_ARRAY_COMPARE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EC_INCOMPATIBLE_ARRAY_COMPARE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EC_INCOMPATIBLE_ARRAY_COMPARE', null, 'EC_INCOMPATIBLE_ARRAY_COMPARE', 'java', null, 'EC_INCOMPATIBLE_ARRAY_COMPARE', null, null, null, null, 'This method invokes the .equals(Object o) to compare two arrays, but the arrays of incompatible types (e.g., String[] and StringBuffer[], or String[] and int[]).They will never be equal. In addition, when equals(...) is used to compare arrays it only checks to see if they are the same array, and ignores the contents of the arrays.', 'equals(...) used to compare incompatible arrays', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_INCOMPATIBLE_ARRAY_COMPARE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_INCOMPATIBLE_ARRAY_COMPARE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EC_NULL_ARG
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EC_NULL_ARG', null, 'EC_NULL_ARG', 'java', null, 'EC_NULL_ARG', null, null, null, null, 'This method calls equals(Object), passing a null value as the argument. According to the contract of the equals() method,this call should always return <code>false</code>.', 'Call to equals(null)', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_NULL_ARG');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_NULL_ARG'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EC_UNRELATED_CLASS_AND_INTERFACE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EC_UNRELATED_CLASS_AND_INTERFACE', null, 'EC_UNRELATED_CLASS_AND_INTERFACE', 'java', null, 'EC_UNRELATED_CLASS_AND_INTERFACE', null, null, null, null, 'This method calls equals(Object) on two references, one of which is a class and the other an interface, where neither the class nor any of its non-abstract subclasses implement the interface.Therefore, the objects being compared are unlikely to be members of the same class at runtime(unless some application classes were not analyzed, or dynamic classloading can occur at runtime).According to the contract of equals(),objects of different classes should always compare as unequal; therefore, according to the contract defined by java.lang.Object.equals(Object),the result of this comparison will always be false at runtime.', 'Call to equals() comparing unrelated class and interface', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_UNRELATED_CLASS_AND_INTERFACE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_UNRELATED_CLASS_AND_INTERFACE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EC_UNRELATED_INTERFACES
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EC_UNRELATED_INTERFACES', null, 'EC_UNRELATED_INTERFACES', 'java', null, 'EC_UNRELATED_INTERFACES', null, null, null, null, 'This method calls equals(Object) on two references of unrelated interface types, where neither is a subtype of the other,and there are no known non-abstract classes which implement both interfaces.Therefore, the objects being compared are unlikely to be members of the same class at runtime(unless some application classes were not analyzed, or dynamic classloading can occur at runtime).According to the contract of equals(),objects of different classes should always compare as unequal; therefore, according to the contract defined by java.lang.Object.equals(Object),the result of this comparison will always be false at runtime.', 'Call to equals() comparing different interface types', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_UNRELATED_INTERFACES');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_UNRELATED_INTERFACES'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EC_UNRELATED_TYPES
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EC_UNRELATED_TYPES', null, 'EC_UNRELATED_TYPES', 'java', null, 'EC_UNRELATED_TYPES', null, null, null, null, 'This method calls equals(Object) on two references of different class types and analysis suggests they will be to objects of different classes at runtime. Further, examination of the equals methods that would be invoked suggest that either this call will always return false, or else the equals method is not be symmetric (which isa property required by the contract for equals in class Object).', 'Call to equals() comparing different types', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_UNRELATED_TYPES');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_UNRELATED_TYPES'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EC_UNRELATED_TYPES_USING_POINTER_EQUALITY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EC_UNRELATED_TYPES_USING_POINTER_EQUALITY', null, 'EC_UNRELATED_TYPES_USING_POINTER_EQUALITY', 'java', null, 'EC_UNRELATED_TYPES_USING_POINTER_EQUALITY', null, null, null, null, 'This method uses using pointer equality to compare two references that seem to be of different types. The result of this comparison will always be false at runtime.', 'Using pointer equality to compare different types', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_UNRELATED_TYPES_USING_POINTER_EQUALITY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EC_UNRELATED_TYPES_USING_POINTER_EQUALITY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_ALWAYS_FALSE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EQ_ALWAYS_FALSE', null, 'EQ_ALWAYS_FALSE', 'java', null, 'EQ_ALWAYS_FALSE', null, null, null, null, 'This class defines an equals method that always returns false. This means that an object is not equal to itself, and it is impossible to create useful Maps or Sets of this class. More fundamentally, it means that equals is not reflexive, one of the requirements of the equals method.The likely intended semantics are object identity: that an object is equal to itself. This is the behavior inherited from class <code>Object</code>. If you need to override an equals inherited from a different superclass, you can use:<pre><code>public boolean equals(Object o) { return this == o;}</code></pre>', 'equals method always returns false', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_ALWAYS_FALSE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_ALWAYS_FALSE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_ALWAYS_TRUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EQ_ALWAYS_TRUE', null, 'EQ_ALWAYS_TRUE', 'java', null, 'EQ_ALWAYS_TRUE', null, null, null, null, 'This class defines an equals method that always returns true. This is imaginative, but not very smart.Plus, it means that the equals method is not symmetric.', 'equals method always returns true', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_ALWAYS_TRUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_ALWAYS_TRUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_COMPARING_CLASS_NAMES
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EQ_COMPARING_CLASS_NAMES', null, 'EQ_COMPARING_CLASS_NAMES', 'java', null, 'EQ_COMPARING_CLASS_NAMES', null, null, null, null, 'This method checks to see if two objects are the same class by checking to see if the names of their classes are equal. You can have different classes with the same name if they are loaded by different class loaders. Just check to see if the class objects are the same.', 'equals method compares class names rather than class objects', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_COMPARING_CLASS_NAMES');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_COMPARING_CLASS_NAMES'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_DONT_DEFINE_EQUALS_FOR_ENUM
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EQ_DONT_DEFINE_EQUALS_FOR_ENUM', null, 'EQ_DONT_DEFINE_EQUALS_FOR_ENUM', 'java', null, 'EQ_DONT_DEFINE_EQUALS_FOR_ENUM', null, null, null, null, 'This class defines an enumeration, and equality on enumerations are defined using object identity. Defining a covariant equals method for an enumeration value is exceptionally bad practice, since it would likely result in having two different enumeration values that compare as equals using the covariant enum method, and as not equal when compared normally.Don''t do it.', 'Covariant equals() method defined for enum', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_DONT_DEFINE_EQUALS_FOR_ENUM');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_DONT_DEFINE_EQUALS_FOR_ENUM'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_OTHER_NO_OBJECT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EQ_OTHER_NO_OBJECT', null, 'EQ_OTHER_NO_OBJECT', 'java', null, 'EQ_OTHER_NO_OBJECT', null, null, null, null, 'This class defines an <code>equals()</code> method, that doesn''t override the normal <code>equals(Object)</code> method defined in the base <code>java.lang.Object</code> class.&nbsp; Instead, it inherits an <code>equals(Object)</code> method from a superclass. The class should probably define a <code>boolean equals(Object)</code> method.', 'equals() method defined that doesn''t override equals(Object)', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_OTHER_NO_OBJECT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_OTHER_NO_OBJECT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_OTHER_USE_OBJECT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EQ_OTHER_USE_OBJECT', null, 'EQ_OTHER_USE_OBJECT', 'java', null, 'EQ_OTHER_USE_OBJECT', null, null, null, null, 'This class defines an <code>equals()</code> method, that doesn''t override the normal <code>equals(Object)</code> method defined in the base <code>java.lang.Object</code> class.&nbsp; The class should probably define a <code>boolean equals(Object)</code> method.', 'equals() method defined that doesn''t override Object.equals(Object)', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_OTHER_USE_OBJECT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_OTHER_USE_OBJECT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC', null, 'EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC', 'java', null, 'EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC', null, null, null, null, 'This class defines an equals method that overrides an equals method in a superclass. Both equals methods methods use <code>instanceof</code> in the determination of whether two objects are equal. This is fraught with peril,since it is important that the equals method is symmetrical (in other words, <code>a.equals(b) == b.equals(a)</code>).If B is a subtype of A, and A''s equals method checks that the argument is an instanceof A, and B''s equals method checks that the argument is an instanceof B, it is quite likely that the equivalence relation defined by these methods is not symmetric.', 'equals method overrides equals in superclass and may not be symmetric', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_SELF_USE_OBJECT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'EQ_SELF_USE_OBJECT', null, 'EQ_SELF_USE_OBJECT', 'java', null, 'EQ_SELF_USE_OBJECT', null, null, null, null, 'This class defines a covariant version of the <code>equals()</code> method, but inherits the normal <code>equals(Object)</code> method defined in the base <code>java.lang.Object</code> class.&nbsp; The class should probably define a <code>boolean equals(Object)</code> method.', 'Covariant equals() method defined, Object.equals(Object) inherited', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_SELF_USE_OBJECT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_SELF_USE_OBJECT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FB_MISSING_EXPECTED_WARNING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'FB_MISSING_EXPECTED_WARNING', null, 'FB_MISSING_EXPECTED_WARNING', 'java', null, 'FB_MISSING_EXPECTED_WARNING', null, null, null, null, 'SpotBugs didn''t generate generated a warning that, according to a @ExpectedWarning annotated, is expected or desired.', 'Missing expected or desired warning from SpotBugs', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FB_MISSING_EXPECTED_WARNING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FB_MISSING_EXPECTED_WARNING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FB_UNEXPECTED_WARNING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'FB_UNEXPECTED_WARNING', null, 'FB_UNEXPECTED_WARNING', 'java', null, 'FB_UNEXPECTED_WARNING', null, null, null, null, 'SpotBugs generated a warning that, according to a @NoWarning annotated, is unexpected or undesired.', 'Unexpected/undesired warning from SpotBugs', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FB_UNEXPECTED_WARNING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FB_UNEXPECTED_WARNING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FE_TEST_IF_EQUAL_TO_NOT_A_NUMBER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'FE_TEST_IF_EQUAL_TO_NOT_A_NUMBER', null, 'FE_TEST_IF_EQUAL_TO_NOT_A_NUMBER', 'java', null, 'FE_TEST_IF_EQUAL_TO_NOT_A_NUMBER', null, null, null, null, 'This code checks to see if a floating point value is equal to the special Not A Number value (e.g., <code>if (x == Double.NaN)</code>). However, because of the special semantics of <code>NaN</code>, no value is equal to <code>Nan</code>, including <code>NaN</code>. Thus, <code>x == Double.NaN</code> always evaluates to false. To check to see if a value contained in <code>x</code> is the special Not A Number value, use <code>Double.isNaN(x)</code> (or <code>Float.isNaN(x)</code> if <code>x</code> is floating point precision).', 'Doomed test for equality to NaN', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FE_TEST_IF_EQUAL_TO_NOT_A_NUMBER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FE_TEST_IF_EQUAL_TO_NOT_A_NUMBER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FL_MATH_USING_FLOAT_PRECISION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'FL_MATH_USING_FLOAT_PRECISION', null, 'FL_MATH_USING_FLOAT_PRECISION', 'java', null, 'FL_MATH_USING_FLOAT_PRECISION', null, null, null, null, 'The method performs math operations using floating point precision. Floating point precision is very imprecise. For example, 16777216.0f + 1.0f = 16777216.0f. Consider using double math instead.', 'Method performs math using floating point precision', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FL_MATH_USING_FLOAT_PRECISION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FL_MATH_USING_FLOAT_PRECISION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- GC_UNRELATED_TYPES
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'GC_UNRELATED_TYPES', null, 'GC_UNRELATED_TYPES', 'java', null, 'GC_UNRELATED_TYPES', null, null, null, null, 'This call to a generic collection method contains an argument with an incompatible class from that of the collection''s parameter (i.e., the type of the argument is neither a supertype nor a subtype of the corresponding generic type argument). Therefore, it is unlikely that the collection contains any objects that are equal to the method argument used here. Most likely, the wrong value is being passed to the method. In general, instances of two unrelated classes are not equal. For example, if the <code>Foo</code> and <code>Bar</code> classes are not related by subtyping, then an instance of <code>Foo</code> should not be equal to an instance of <code>Bar</code>. Among other issues, doing so will likely result in an equals method that is not symmetrical. For example, if you define the <code>Foo</code> class so that a <code>Foo</code> can be equal to a <code>String</code>, your equals method isn''t symmetrical since a <code>String</code> can only be equal to a <code>String</code>. In rare cases, people do define nonsymmetrical equals methods and still manage to make their code work. Although none of the APIs document or guarantee it, it is typically the case that if you check if a <code>Collection&lt;String&gt;</code> contains a <code>Foo</code>, the equals method of argument (e.g., the equals method of the <code>Foo</code> class) used to perform the equality checks.', 'No relationship between generic parameter and method argument', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='GC_UNRELATED_TYPES');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='GC_UNRELATED_TYPES'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- HE_SIGNATURE_DECLARES_HASHING_OF_UNHASHABLE_CLASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'HE_SIGNATURE_DECLARES_HASHING_OF_UNHASHABLE_CLASS', null, 'HE_SIGNATURE_DECLARES_HASHING_OF_UNHASHABLE_CLASS', 'java', null, 'HE_SIGNATURE_DECLARES_HASHING_OF_UNHASHABLE_CLASS', null, null, null, null, 'A method, field or class declares a generic signature where a non-hashable class is used in context where a hashable class is required.A class that declares an equals method but inherits a hashCode() method from Object is unhashable, since it doesn''t fulfill the requirement that equal objects have equal hashCodes.', 'Signature declares use of unhashable class in hashed construct', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_SIGNATURE_DECLARES_HASHING_OF_UNHASHABLE_CLASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_SIGNATURE_DECLARES_HASHING_OF_UNHASHABLE_CLASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- HE_USE_OF_UNHASHABLE_CLASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'HE_USE_OF_UNHASHABLE_CLASS', null, 'HE_USE_OF_UNHASHABLE_CLASS', 'java', null, 'HE_USE_OF_UNHASHABLE_CLASS', null, null, null, null, 'A class defines an equals(Object) method but not a hashCode() method,and thus doesn''t fulfill the requirement that equal objects have equal hashCodes.An instance of this class is used in a hash data structure, making the need to fix this problem of highest importance.', 'Use of class without a hashCode() method in a hashed data structure', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_USE_OF_UNHASHABLE_CLASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HE_USE_OF_UNHASHABLE_CLASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ICAST_BAD_SHIFT_AMOUNT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'ICAST_BAD_SHIFT_AMOUNT', null, 'ICAST_BAD_SHIFT_AMOUNT', 'java', null, 'ICAST_BAD_SHIFT_AMOUNT', null, null, null, null, 'The code performs shift of a 32 bit int by a constant amount outside the range -31..31.The effect of this is to use the lower 5 bits of the integer value to decide how much to shift by (e.g., shifting by 40 bits is the same as shifting by 8 bits,and shifting by 32 bits is the same as shifting by zero bits). This probably isn''t what was expected,and it is at least confusing.', '32 bit int shifted by an amount not in the range -31..31', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_BAD_SHIFT_AMOUNT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_BAD_SHIFT_AMOUNT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ICAST_INT_2_LONG_AS_INSTANT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'ICAST_INT_2_LONG_AS_INSTANT', null, 'ICAST_INT_2_LONG_AS_INSTANT', 'java', null, 'ICAST_INT_2_LONG_AS_INSTANT', null, null, null, null, 'This code converts a 32-bit int value to a 64-bit long value, and then passes that value for a method parameter that requires an absolute time value.An absolute time value is the number of milliseconds since the standard base time known as "the epoch", namely January 1, 1970, 00:00:00 GMT.For example, the following method, intended to convert seconds since the epoch into a Date, is badly broken:<pre><code>Date getDate(int seconds) { return new Date(seconds * 1000); }</code></pre>The multiplication is done using 32-bit arithmetic, and then converted to a 64-bit value.When a 32-bit value is converted to 64-bits and used to express an absolute time value, only dates in December 1969 and January 1970 can be represented.Correct implementations for the above method are:<pre><code>// Fails for dates after 2037Date getDate(int seconds) { return new Date(seconds * 1000L); }// better, works for all datesDate getDate(long seconds) { return new Date(seconds * 1000); }</code></pre>', 'int value converted to long and used as absolute time', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_INT_2_LONG_AS_INSTANT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_INT_2_LONG_AS_INSTANT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ICAST_INT_CAST_TO_DOUBLE_PASSED_TO_CEIL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'ICAST_INT_CAST_TO_DOUBLE_PASSED_TO_CEIL', null, 'ICAST_INT_CAST_TO_DOUBLE_PASSED_TO_CEIL', 'java', null, 'ICAST_INT_CAST_TO_DOUBLE_PASSED_TO_CEIL', null, null, null, null, 'This code converts an integral value (e.g., int or long)to a double precision floating point number and then passing the result to the Math.ceil() function, which rounds a double tothe next higher integer value. This operation should always be a no-op,since the converting an integer to a double should give a number with no fractional part.It is likely that the operation that generated the value to be passed to Math.ceil was intended to be performed using double precision floating point arithmetic.', 'Integral value cast to double and then passed to Math.ceil', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_INT_CAST_TO_DOUBLE_PASSED_TO_CEIL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_INT_CAST_TO_DOUBLE_PASSED_TO_CEIL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ICAST_INT_CAST_TO_FLOAT_PASSED_TO_ROUND
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'ICAST_INT_CAST_TO_FLOAT_PASSED_TO_ROUND', null, 'ICAST_INT_CAST_TO_FLOAT_PASSED_TO_ROUND', 'java', null, 'ICAST_INT_CAST_TO_FLOAT_PASSED_TO_ROUND', null, null, null, null, 'This code converts an int value to a float precision floating point number and the npassing the result to the Math.round() function, which returns the int/long closest to the argument. This operation should always be a no-op,since the converting an integer to a float should give a number with no fractional part.It is likely that the operation that generated the value to be passed to Math.round was intended to be performed using floating point arithmetic.', 'int value cast to float and then passed to Math.round', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_INT_CAST_TO_FLOAT_PASSED_TO_ROUND');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_INT_CAST_TO_FLOAT_PASSED_TO_ROUND'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IJU_ASSERT_METHOD_INVOKED_FROM_RUN_METHOD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IJU_ASSERT_METHOD_INVOKED_FROM_RUN_METHOD', null, 'IJU_ASSERT_METHOD_INVOKED_FROM_RUN_METHOD', 'java', null, 'IJU_ASSERT_METHOD_INVOKED_FROM_RUN_METHOD', null, null, null, null, 'A JUnit assertion is performed in a run method. Failed JUnit assertions just result in exceptions being thrown.Thus, if this exception occurs in a thread other than the thread that invokes the test method, the exception will terminate the thread but not result in the test failing.', 'JUnit assertion in run method will not be noticed by JUnit', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_ASSERT_METHOD_INVOKED_FROM_RUN_METHOD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_ASSERT_METHOD_INVOKED_FROM_RUN_METHOD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IJU_BAD_SUITE_METHOD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IJU_BAD_SUITE_METHOD', null, 'IJU_BAD_SUITE_METHOD', 'java', null, 'IJU_BAD_SUITE_METHOD', null, null, null, null, 'Class is a JUnit TestCase and defines a suite() method.However, the suite method needs to be declared as either<pre><code>public static junit.framework.Test suite()</code></pre>or<pre><code>public static junit.framework.TestSuite suite()</code></pre>', 'TestCase declares a bad suite method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_BAD_SUITE_METHOD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_BAD_SUITE_METHOD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IJU_NO_TESTS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IJU_NO_TESTS', null, 'IJU_NO_TESTS', 'java', null, 'IJU_NO_TESTS', null, null, null, null, 'Class is a JUnit TestCase but has not implemented any test methods.', 'TestCase has no tests', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_NO_TESTS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_NO_TESTS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IJU_SETUP_NO_SUPER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IJU_SETUP_NO_SUPER', null, 'IJU_SETUP_NO_SUPER', 'java', null, 'IJU_SETUP_NO_SUPER', null, null, null, null, 'Class is a JUnit TestCase and implements the setUp method. The setUp method should call super.setUp(), but doesn''t.', 'TestCase defines setUp that doesn''t call super.setUp()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_SETUP_NO_SUPER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_SETUP_NO_SUPER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IJU_SUITE_NOT_STATIC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IJU_SUITE_NOT_STATIC', null, 'IJU_SUITE_NOT_STATIC', 'java', null, 'IJU_SUITE_NOT_STATIC', null, null, null, null, 'Class is a JUnit TestCase and implements the suite() method. The suite method should be declared as being static, but isn''t.', 'TestCase implements a non-static suite method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_SUITE_NOT_STATIC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_SUITE_NOT_STATIC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IJU_TEARDOWN_NO_SUPER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IJU_TEARDOWN_NO_SUPER', null, 'IJU_TEARDOWN_NO_SUPER', 'java', null, 'IJU_TEARDOWN_NO_SUPER', null, null, null, null, 'Class is a JUnit TestCase and implements the tearDown method. The tearDown method should call super.tearDown(), but doesn''t.', 'TestCase defines tearDown that doesn''t call super.tearDown()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_TEARDOWN_NO_SUPER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IJU_TEARDOWN_NO_SUPER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IL_CONTAINER_ADDED_TO_ITSELF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IL_CONTAINER_ADDED_TO_ITSELF', null, 'IL_CONTAINER_ADDED_TO_ITSELF', 'java', null, 'IL_CONTAINER_ADDED_TO_ITSELF', null, null, null, null, 'A collection is added to itself. As a result, computing the hashCode of this set will throw a StackOverflowException.', 'A collection is added to itself', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IL_CONTAINER_ADDED_TO_ITSELF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IL_CONTAINER_ADDED_TO_ITSELF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IL_INFINITE_LOOP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IL_INFINITE_LOOP', null, 'IL_INFINITE_LOOP', 'java', null, 'IL_INFINITE_LOOP', null, null, null, null, 'This loop doesn''t seem to have a way to terminate (other than by perhaps throwing an exception).', 'An apparent infinite loop', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IL_INFINITE_LOOP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IL_INFINITE_LOOP'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IL_INFINITE_RECURSIVE_LOOP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IL_INFINITE_RECURSIVE_LOOP', null, 'IL_INFINITE_RECURSIVE_LOOP', 'java', null, 'IL_INFINITE_RECURSIVE_LOOP', null, null, null, null, 'This method unconditionally invokes itself. This would seem to indicate an infinite recursive loop that will result in a stack overflow.', 'An apparent infinite recursive loop', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IL_INFINITE_RECURSIVE_LOOP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IL_INFINITE_RECURSIVE_LOOP'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IL_INFINITE_RECURSIVE_LOOP'),
 'STANDARD', 'CWE','647')
ON CONFLICT DO NOTHING;



-- ------------------------
-- IM_MULTIPLYING_RESULT_OF_IREM
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IM_MULTIPLYING_RESULT_OF_IREM', null, 'IM_MULTIPLYING_RESULT_OF_IREM', 'java', null, 'IM_MULTIPLYING_RESULT_OF_IREM', null, null, null, null, 'The code multiplies the result of an integer remaining by an integer constant.Be sure you don''t have your operator precedence confused. For example i % 60 * 1000 is (i % 60) * 1000, not i % (60 * 1000).', 'Integer multiply of result of integer remainder', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IM_MULTIPLYING_RESULT_OF_IREM');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IM_MULTIPLYING_RESULT_OF_IREM'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- INT_BAD_COMPARISON_WITH_INT_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'INT_BAD_COMPARISON_WITH_INT_VALUE', null, 'INT_BAD_COMPARISON_WITH_INT_VALUE', 'java', null, 'INT_BAD_COMPARISON_WITH_INT_VALUE', null, null, null, null, 'This code compares an int value with a long constant that is outside the range of values that can be represented as an int value.This comparison is vacuous and possibly incorrect.', 'Bad comparison of int value with long constant', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_BAD_COMPARISON_WITH_INT_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_BAD_COMPARISON_WITH_INT_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE', null, 'INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE', 'java', null, 'INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE', null, null, null, null, 'This code compares a value that is guaranteed to be non-negative with a negative constant or zero.', 'Bad comparison of nonnegative value with negative constant or zero', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- INT_BAD_COMPARISON_WITH_SIGNED_BYTE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'INT_BAD_COMPARISON_WITH_SIGNED_BYTE', null, 'INT_BAD_COMPARISON_WITH_SIGNED_BYTE', 'java', null, 'INT_BAD_COMPARISON_WITH_SIGNED_BYTE', null, null, null, null, 'Signed bytes can only have a value in the range -128 to 127. Comparing a signed byte with a value outside that range is vacuous and likely to be incorrect.To convert a signed byte <code>b</code> to an unsigned value in the range 0..255,use <code>0xff &amp; b</code>.', 'Bad comparison of signed byte', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_BAD_COMPARISON_WITH_SIGNED_BYTE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_BAD_COMPARISON_WITH_SIGNED_BYTE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IO_APPENDING_TO_OBJECT_OUTPUT_STREAM
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IO_APPENDING_TO_OBJECT_OUTPUT_STREAM', null, 'IO_APPENDING_TO_OBJECT_OUTPUT_STREAM', 'java', null, 'IO_APPENDING_TO_OBJECT_OUTPUT_STREAM', null, null, null, null, 'This code opens a file in append mode and then wraps the result in an object output stream. This won''t allow you to append to an existing object output stream stored in a file. If you want to be able to append to an object output stream, you need to keep the object output stream open. The only situation in which opening a file in append mode and the writing an object output stream could work is if on reading the file you plan to open it in random access mode and seek to the byte offset where the append started. TODO: example.', 'Doomed attempt to append to an object output stream', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IO_APPENDING_TO_OBJECT_OUTPUT_STREAM');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IO_APPENDING_TO_OBJECT_OUTPUT_STREAM'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN', null, 'IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN', 'java', null, 'IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN', null, null, null, null, 'The initial value of this parameter is ignored, and the parameter is overwritten here. This often indicates a mistaken belief that the write to the parameter will be conveyed back to the caller.', 'A parameter is dead upon entry to a method but overwritten', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN'),
 'STANDARD', 'CWE','563')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MF_CLASS_MASKS_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'MF_CLASS_MASKS_FIELD', null, 'MF_CLASS_MASKS_FIELD', 'java', null, 'MF_CLASS_MASKS_FIELD', null, null, null, null, 'This class defines a field with the same name as a visible instance field in a superclass. This is confusing, and may indicate an error if methods update or access one of the fields when they wanted the other.', 'Class defines field that masks a superclass field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MF_CLASS_MASKS_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MF_CLASS_MASKS_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MF_METHOD_MASKS_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'MF_METHOD_MASKS_FIELD', null, 'MF_METHOD_MASKS_FIELD', 'java', null, 'MF_METHOD_MASKS_FIELD', null, null, null, null, 'This method defines a local variable with the same name as a field in this class or a superclass. This may cause the method to read an uninitialized value from the field, leave the field uninitialized,or both.', 'Method defines a variable that obscures a field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MF_METHOD_MASKS_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MF_METHOD_MASKS_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_BAD_EQUAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NM_BAD_EQUAL', null, 'NM_BAD_EQUAL', 'java', null, 'NM_BAD_EQUAL', null, null, null, null, 'This class defines a method <code>equal(Object)</code>.&nbsp; This method does not override the <code>equals(Object)</code> method in <code>java.lang.Object</code>,which is probably what was intended.', 'Class defines equal(Object); should it be equals(Object)?', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_BAD_EQUAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_BAD_EQUAL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_LCASE_HASHCODE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NM_LCASE_HASHCODE', null, 'NM_LCASE_HASHCODE', 'java', null, 'NM_LCASE_HASHCODE', null, null, null, null, 'This class defines a method called <code>hashcode()</code>.&nbsp; This method does not override the <code>hashCode()</code> method in <code>java.lang.Object</code>, which is probably what was intended.', 'Class defines hashcode(); should it be hashCode()?', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_LCASE_HASHCODE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_LCASE_HASHCODE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_LCASE_TOSTRING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NM_LCASE_TOSTRING', null, 'NM_LCASE_TOSTRING', 'java', null, 'NM_LCASE_TOSTRING', null, null, null, null, 'This class defines a method called <code>tostring()</code>.&nbsp; This method does not override the <code>toString()</code> method in <code>java.lang.Object</code>, which is probably what was intended.', 'Class defines tostring(); should it be toString()?', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_LCASE_TOSTRING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_LCASE_TOSTRING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_METHOD_CONSTRUCTOR_CONFUSION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NM_METHOD_CONSTRUCTOR_CONFUSION', null, 'NM_METHOD_CONSTRUCTOR_CONFUSION', 'java', null, 'NM_METHOD_CONSTRUCTOR_CONFUSION', null, null, null, null, 'This regular method has the same name as the class it is defined in. It is likely that this was intended to be a constructor. If it was intended to be a constructor, remove the declaration of a void return value. If you had accidentally defined this method, realized the mistake, defined a proper constructor but can''t get rid of this method due to backwards compatibility, deprecate the method.', 'Apparent method/constructor confusion', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_METHOD_CONSTRUCTOR_CONFUSION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_METHOD_CONSTRUCTOR_CONFUSION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_VERY_CONFUSING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NM_VERY_CONFUSING', null, 'NM_VERY_CONFUSING', 'java', null, 'NM_VERY_CONFUSING', null, null, null, null, 'The referenced methods have names that differ only by capitalization.This is very confusing because if the capitalization were identical then one of the methods would override the other.', 'Very confusing method names', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_VERY_CONFUSING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_VERY_CONFUSING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NM_WRONG_PACKAGE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NM_WRONG_PACKAGE', null, 'NM_WRONG_PACKAGE', 'java', null, 'NM_WRONG_PACKAGE', null, null, null, null,
'The method in the subclass doesn''t override a similar method in a superclass because the type of a parameter doesn''t exactly match the type of the corresponding parameter in the superclass. For example, if you have:<pre><code>import alpha.Foo;public class A { public int f(Foo x) { return 17; }}----import beta.Foo;public class B extends A { public int f(Foo x) { return 42; }}</code></pre>The <code>f(Foo)</code> method defined in class <code>B</code> doesn''toverride the<code>f(Foo)</code> method defined in class <code>A</code>, because the argument types are <code>Foo</code>''s from different packages.',
'Method doesn''t override method in superclass due to wrong package for parameter',
 null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_WRONG_PACKAGE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NM_WRONG_PACKAGE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_ALWAYS_NULL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_ALWAYS_NULL', null, 'NP_ALWAYS_NULL', 'java', null, 'NP_ALWAYS_NULL', null, null, null, null, 'A null pointer is dereferenced here.&nbsp; This will lead to a<code>NullPointerException</code> when the code is executed.', 'Null pointer dereference', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_ALWAYS_NULL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_ALWAYS_NULL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_ALWAYS_NULL_EXCEPTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_ALWAYS_NULL_EXCEPTION', null, 'NP_ALWAYS_NULL_EXCEPTION', 'java', null, 'NP_ALWAYS_NULL_EXCEPTION', null, null, null, null, 'A pointer which is null on an exception path is dereferenced here.&nbsp;This will lead to a <code>NullPointerException</code> when the code is executed.&nbsp;Note that because SpotBugs currently does not prune infeasible exception paths,this may be a false warning. Also note that SpotBugs considers the default case of a switch statement tobe an exception path, since the default case is often infeasible.', 'Null pointer dereference in method on exception path', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_ALWAYS_NULL_EXCEPTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_ALWAYS_NULL_EXCEPTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_ARGUMENT_MIGHT_BE_NULL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_ARGUMENT_MIGHT_BE_NULL', null, 'NP_ARGUMENT_MIGHT_BE_NULL', 'java', null, 'NP_ARGUMENT_MIGHT_BE_NULL', null, null, null, null, 'A parameter to this method has been identified as a value that should always be checked to see whether or not it is null, but it is being dereferenced without a preceding null check.', 'Method does not check for null argument', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_ARGUMENT_MIGHT_BE_NULL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_ARGUMENT_MIGHT_BE_NULL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_CLOSING_NULL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_CLOSING_NULL', null, 'NP_CLOSING_NULL', 'java', null, 'NP_CLOSING_NULL', null, null, null, null, 'close() is being invoked on a value that is always null. If this statement is executed,a null pointer exception will occur. But the big risk here you never close something that should be closed.', 'close() invoked on a value that is always null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_CLOSING_NULL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_CLOSING_NULL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_GUARANTEED_DEREF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_GUARANTEED_DEREF', null, 'NP_GUARANTEED_DEREF', 'java', null, 'NP_GUARANTEED_DEREF', null, null, null, null, 'There is a statement or branch that if executed guarantees that a value is null at this point, and that value that is guaranteed to be dereferenced (except on forward paths involving runtime exceptions). Note that a check such as <code>if (x == null) throw new NullPointerException();</code> is treated as a dereference of <code>x</code>.', 'Null value is guaranteed to be dereferenced', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_GUARANTEED_DEREF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_GUARANTEED_DEREF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_GUARANTEED_DEREF_ON_EXCEPTION_PATH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_GUARANTEED_DEREF_ON_EXCEPTION_PATH', null, 'NP_GUARANTEED_DEREF_ON_EXCEPTION_PATH', 'java', null, 'NP_GUARANTEED_DEREF_ON_EXCEPTION_PATH', null, null, null, null, 'There is a statement or branch on an exception path that if executed guarantees that a value is null at this point, and that value that is guaranteed to be dereferenced (except on forward paths involving runtime exceptions).', 'Value is null and guaranteed to be dereferenced on exception path', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_GUARANTEED_DEREF_ON_EXCEPTION_PATH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_GUARANTEED_DEREF_ON_EXCEPTION_PATH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR', null, 'NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR', 'java', null, 'NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR', null, null, null, null, 'The field is marked as non-null, but isn''t written to by the constructor. The field might be initialized elsewhere during constructor, or might always be initialized before use.', 'Non-null field is not initialized', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NONNULL_PARAM_VIOLATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_NONNULL_PARAM_VIOLATION', null, 'NP_NONNULL_PARAM_VIOLATION', 'java', null, 'NP_NONNULL_PARAM_VIOLATION', null, null, null, null, 'This method passes a null value as the parameter of a method which must be non-null. Either this parameter has been explicitly marked as @Nonnull, or analysis has determined that this parameter is always dereferenced.', 'Method call passes null to a non-null parameter', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NONNULL_PARAM_VIOLATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NONNULL_PARAM_VIOLATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NONNULL_RETURN_VIOLATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_NONNULL_RETURN_VIOLATION', null, 'NP_NONNULL_RETURN_VIOLATION', 'java', null, 'NP_NONNULL_RETURN_VIOLATION', null, null, null, null, 'This method may return a null value, but the method (or a superclass method which it overrides) is declared to return @Nonnull.', 'Method may return null, but is declared @Nonnull', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NONNULL_RETURN_VIOLATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NONNULL_RETURN_VIOLATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NULL_INSTANCEOF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_NULL_INSTANCEOF', null, 'NP_NULL_INSTANCEOF', 'java', null, 'NP_NULL_INSTANCEOF', null, null, null, null, 'This instanceof test will always return false, since the value being checked is guaranteed to be null.Although this is safe, make sure it isn''tan indication of some misunderstanding or some other logic error.', 'A known null value is checked to see if it is an instance of a type', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_INSTANCEOF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_INSTANCEOF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NULL_ON_SOME_PATH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_NULL_ON_SOME_PATH', null, 'NP_NULL_ON_SOME_PATH', 'java', null, 'NP_NULL_ON_SOME_PATH', null, null, null, null, 'There is a branch of statement that, <em>if executed,</em> guarantees that a null value will be dereferenced, which would generate a <code>NullPointerException</code> when the code is executed.Of course, the problem might be that the branch or statement is infeasible and that the null pointer exception can''t ever be executed; deciding that is beyond the ability of SpotBugs.', 'Possible null pointer dereference', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_ON_SOME_PATH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_ON_SOME_PATH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NULL_ON_SOME_PATH_EXCEPTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_NULL_ON_SOME_PATH_EXCEPTION', null, 'NP_NULL_ON_SOME_PATH_EXCEPTION', 'java', null, 'NP_NULL_ON_SOME_PATH_EXCEPTION', null, null, null, null, 'A reference value which is null on some exception control path is dereferenced here.&nbsp; This may lead to a <code>NullPointerException</code>when the code is executed.&nbsp;Note that because SpotBugs currently does not prune infeasible exception paths,this may be a false warning. Also note that SpotBugs considers the default case of a switch statement tobe an exception path, since the default case is often infeasible.', 'Possible null pointer dereference in method on exception path', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_ON_SOME_PATH_EXCEPTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_ON_SOME_PATH_EXCEPTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NULL_PARAM_DEREF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_NULL_PARAM_DEREF', null, 'NP_NULL_PARAM_DEREF', 'java', null, 'NP_NULL_PARAM_DEREF', null, null, null, null, 'This method call passes a null value for a non-null method parameter. Either the parameter is annotated as a parameter that should always be non-null, or analysis has shown that it will always be dereferenced.', 'Method call passes null for non-null parameter', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_PARAM_DEREF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_PARAM_DEREF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS', null, 'NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS', 'java', null, 'NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS', null, null, null, null, 'A possibly-null value is passed at a call site where all known target methods require the parameter to be non-null. Either the parameter is annotated as a parameter that should always be non-null, or analysis has shown that it will always be dereferenced.', 'Method call passes null for non-null parameter', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NULL_PARAM_DEREF_NONVIRTUAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_NULL_PARAM_DEREF_NONVIRTUAL', null, 'NP_NULL_PARAM_DEREF_NONVIRTUAL', 'java', null, 'NP_NULL_PARAM_DEREF_NONVIRTUAL', null, null, null, null, 'A possibly-null value is passed to a non-null method parameter. Either the parameter is annotated as a parameter that should always be non-null, or analysis has shown that it will always be dereferenced.', 'Non-virtual method call passes null for non-null parameter', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_PARAM_DEREF_NONVIRTUAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_PARAM_DEREF_NONVIRTUAL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_OPTIONAL_RETURN_NULL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_OPTIONAL_RETURN_NULL', null, 'NP_OPTIONAL_RETURN_NULL', 'java', null, 'NP_OPTIONAL_RETURN_NULL', null, null, null, null, 'The usage of Optional return type (java.util.Optional or com.google.common.base.Optional) always means that explicit null returns were not desired by design. Returning a null value in such case is a contract violation and will most likely break client code.', 'Method with Optional return type returns explicit null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_OPTIONAL_RETURN_NULL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_OPTIONAL_RETURN_NULL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_STORE_INTO_NONNULL_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_STORE_INTO_NONNULL_FIELD', null, 'NP_STORE_INTO_NONNULL_FIELD', 'java', null, 'NP_STORE_INTO_NONNULL_FIELD', null, null, null, null, 'A value that could be null is stored into a field that has been annotated as @Nonnull.', 'Store of null value into field annotated @Nonnull', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_STORE_INTO_NONNULL_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_STORE_INTO_NONNULL_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_UNWRITTEN_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'NP_UNWRITTEN_FIELD', null, 'NP_UNWRITTEN_FIELD', 'java', null, 'NP_UNWRITTEN_FIELD', null, null, null, null, 'The program is dereferencing a field that does not seem to ever have a non-null value written to it.Unless the field is initialized via some mechanism not seen by the analysis,dereferencing this value will generate a null pointer exception.', 'Read of unwritten field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_UNWRITTEN_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_UNWRITTEN_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- QBA_QUESTIONABLE_BOOLEAN_ASSIGNMENT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'QBA_QUESTIONABLE_BOOLEAN_ASSIGNMENT', null, 'QBA_QUESTIONABLE_BOOLEAN_ASSIGNMENT', 'java', null, 'QBA_QUESTIONABLE_BOOLEAN_ASSIGNMENT', null, null, null, null, 'This method assigns a literal boolean value (true or false) to a boolean variable inside an if or while expression. Most probably this was supposed to be a boolean comparison using ==, not an assignment using =.', 'Method assigns boolean literal in boolean expression', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='QBA_QUESTIONABLE_BOOLEAN_ASSIGNMENT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='QBA_QUESTIONABLE_BOOLEAN_ASSIGNMENT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='QBA_QUESTIONABLE_BOOLEAN_ASSIGNMENT'),
 'STANDARD', 'CWE','481')
ON CONFLICT DO NOTHING;



-- ------------------------
-- RANGE_ARRAY_INDEX
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RANGE_ARRAY_INDEX', null, 'RANGE_ARRAY_INDEX', 'java', null, 'RANGE_ARRAY_INDEX', null, null, null, null, 'Array operation is performed, but array index is out of bounds, which will result in ArrayIndexOutOfBoundsException at runtime.', 'Array index is out of bounds', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RANGE_ARRAY_INDEX');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RANGE_ARRAY_INDEX'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RANGE_ARRAY_LENGTH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RANGE_ARRAY_LENGTH', null, 'RANGE_ARRAY_LENGTH', 'java', null, 'RANGE_ARRAY_LENGTH', null, null, null, null, 'Method is called with array parameter and length parameter, but the length is out of bounds. This will result in IndexOutOfBoundsException at runtime.', 'Array length is out of bounds', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RANGE_ARRAY_LENGTH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RANGE_ARRAY_LENGTH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RANGE_ARRAY_OFFSET
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RANGE_ARRAY_OFFSET', null, 'RANGE_ARRAY_OFFSET', 'java', null, 'RANGE_ARRAY_OFFSET', null, null, null, null, 'Method is called with array parameter and offset parameter, but the offset is out of bounds. This will result in IndexOutOfBoundsException at runtime.', 'Array offset is out of bounds', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RANGE_ARRAY_OFFSET');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RANGE_ARRAY_OFFSET'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RANGE_STRING_INDEX
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RANGE_STRING_INDEX', null, 'RANGE_STRING_INDEX', 'java', null, 'RANGE_STRING_INDEX', null, null, null, null, 'String method is called and specified string index is out of bounds. This will result in StringIndexOutOfBoundsException at runtime.', 'String index is out of bounds', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RANGE_STRING_INDEX');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RANGE_STRING_INDEX'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE', null, 'RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE', 'java', null, 'RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE', null, null, null, null, 'A value is checked here to see whether it is null, but this value can''tbe null because it was previously dereferenced and if it were null a null pointer exception would have occurred at the earlier dereference.Essentially, this code and the previous dereference disagree as to whether this value is allowed to be null. Either the check is redundant or the previous dereference is erroneous.', 'Nullcheck of value previously dereferenced', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RC_REF_COMPARISON
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RC_REF_COMPARISON', null, 'RC_REF_COMPARISON', 'java', null, 'RC_REF_COMPARISON', null, null, null, null, 'This method compares two reference values using the == or != operator,where the correct way to compare instances of this type is generally with the equals() method.It is possible to create distinct instances that are equal but do not compare as == since they are different objects.Examples of classes which should generally not be compared by reference are java.lang.Integer, java.lang.Float, etc.', 'Suspicious reference comparison', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RC_REF_COMPARISON');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RC_REF_COMPARISON'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RE_BAD_SYNTAX_FOR_REGULAR_EXPRESSION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RE_BAD_SYNTAX_FOR_REGULAR_EXPRESSION', null, 'RE_BAD_SYNTAX_FOR_REGULAR_EXPRESSION', 'java', null, 'RE_BAD_SYNTAX_FOR_REGULAR_EXPRESSION', null, null, null, null, 'The code here uses a regular expression that is invalid according to the syntax for regular expressions. This statement will throw a PatternSyntaxException when executed.', 'Invalid syntax for regular expression', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RE_BAD_SYNTAX_FOR_REGULAR_EXPRESSION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RE_BAD_SYNTAX_FOR_REGULAR_EXPRESSION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RE_CANT_USE_FILE_SEPARATOR_AS_REGULAR_EXPRESSION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RE_CANT_USE_FILE_SEPARATOR_AS_REGULAR_EXPRESSION', null, 'RE_CANT_USE_FILE_SEPARATOR_AS_REGULAR_EXPRESSION', 'java', null, 'RE_CANT_USE_FILE_SEPARATOR_AS_REGULAR_EXPRESSION', null, null, null, null, 'The code here uses <code>File.separator</code>where a regular expression is required. This will fail on Windows platforms, where the <code>File.separator</code> is a backslash, which is interpreted in a regular expression as an escape character. Among other options, you can just use<code>File.separatorChar==''\\'' ? "\\\\" : File.separator</code> instead of<code>File.separator</code>', 'File.separator used for regular expression', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RE_CANT_USE_FILE_SEPARATOR_AS_REGULAR_EXPRESSION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RE_CANT_USE_FILE_SEPARATOR_AS_REGULAR_EXPRESSION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RE_POSSIBLE_UNINTENDED_PATTERN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RE_POSSIBLE_UNINTENDED_PATTERN', null, 'RE_POSSIBLE_UNINTENDED_PATTERN', 'java', null, 'RE_POSSIBLE_UNINTENDED_PATTERN', null, null, null, null, 'A String function is being invoked and "." or "|" is being passed to a parameter that takes a regular expression as an argument. Is this what you intended?For example<ul><li>s.replaceAll(".", "/") will return a String in which <em>every</em> character has been replaced by a ''/'' character</li><li>s.split(".") <em>always</em> returns a zero length array of String</li><li>"ab|cd".replaceAll("|", "/") will return "/a/b/|/c/d/"</li><li>"ab|cd".split("|") will return array with six (!) elements: [, a, b, |, c, d]</li></ul>', '"." or "|" used for regular expression', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RE_POSSIBLE_UNINTENDED_PATTERN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RE_POSSIBLE_UNINTENDED_PATTERN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_01_TO_INT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RV_01_TO_INT', null, 'RV_01_TO_INT', 'java', null, 'RV_01_TO_INT', null, null, null, null, 'A random value from 0 to 1 is being coerced to the integer value 0. You probably want to multiply the random value by something else before coercing it to an integer, or use the <code>Random.nextInt(n)</code> method.', 'Random value from 0 to 1 is coerced to the integer 0', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_01_TO_INT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_01_TO_INT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_ABSOLUTE_VALUE_OF_HASHCODE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RV_ABSOLUTE_VALUE_OF_HASHCODE', null, 'RV_ABSOLUTE_VALUE_OF_HASHCODE', 'java', null, 'RV_ABSOLUTE_VALUE_OF_HASHCODE', null, null, null, null, 'This code generates a hashcode and then computes the absolute value of that hashcode. If the hashcode is <code>Integer.MIN_VALUE</code>, then the result will be negative as well (since<code>Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE</code>).One out of 2^32 strings have a hashCode of Integer.MIN_VALUE,including "polygenelubricants" "GydZG_" and ""DESIGNING WORKHOUSES".', 'Bad attempt to compute absolute value of signed 32-bit hashcode', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_ABSOLUTE_VALUE_OF_HASHCODE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_ABSOLUTE_VALUE_OF_HASHCODE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_ABSOLUTE_VALUE_OF_RANDOM_INT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RV_ABSOLUTE_VALUE_OF_RANDOM_INT', null, 'RV_ABSOLUTE_VALUE_OF_RANDOM_INT', 'java', null, 'RV_ABSOLUTE_VALUE_OF_RANDOM_INT', null, null, null, null, 'This code generates a random signed integer and then computes the absolute value of that random integer. If the number returned by the random number generator is <code>Integer.MIN_VALUE</code>, then the result will be negative as well (since<code>Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE</code>). (Same problem arises for long values as well).', 'Bad attempt to compute absolute value of signed random integer', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_ABSOLUTE_VALUE_OF_RANDOM_INT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_ABSOLUTE_VALUE_OF_RANDOM_INT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_CHECK_COMPARETO_FOR_SPECIFIC_RETURN_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RV_CHECK_COMPARETO_FOR_SPECIFIC_RETURN_VALUE', null, 'RV_CHECK_COMPARETO_FOR_SPECIFIC_RETURN_VALUE', 'java', null, 'RV_CHECK_COMPARETO_FOR_SPECIFIC_RETURN_VALUE', null, null, null, null, 'This code invoked a compareTo or compare method, and checks to see if the return value is a specific value,such as 1 or -1. When invoking these methods, you should only check the sign of the result, not for any specific non-zero value. While many or most compareTo and compare methods only return -1, 0 or 1, some of them will return other values.', 'Code checks for specific values returned by compareTo', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_CHECK_COMPARETO_FOR_SPECIFIC_RETURN_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_CHECK_COMPARETO_FOR_SPECIFIC_RETURN_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_EXCEPTION_NOT_THROWN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RV_EXCEPTION_NOT_THROWN', null, 'RV_EXCEPTION_NOT_THROWN', 'java', null, 'RV_EXCEPTION_NOT_THROWN', null, null, null, null, 'This code creates an exception (or error) object, but doesn''t do anything with it. For example,something like <pre><code>if (x &lt; 0) { new IllegalArgumentException("x must be nonnegative");}</code></pre>It was probably the intent of the programmer to throw the created exception:<pre><code>if (x &lt; 0) { throw new IllegalArgumentException("x must be nonnegative");}</code></pre>', 'Exception created and dropped rather than thrown', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_EXCEPTION_NOT_THROWN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_EXCEPTION_NOT_THROWN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_RETURN_VALUE_IGNORED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RV_RETURN_VALUE_IGNORED', null, 'RV_RETURN_VALUE_IGNORED', 'java', null, 'RV_RETURN_VALUE_IGNORED', null, null, null, null, 'The return value of this method should be checked. One common cause of this warning is to invoke a method on an immutable object,thinking that it updates the object. For example, in the following code fragment,<pre><code>String dateString = getHeaderField(name);dateString.trim();</code></pre>the programmer seems to be thinking that the trim() method will update the String referenced by dateString. But since Strings are immutable, the trim()function returns a new String value, which is being ignored here. The code should be corrected to: <pre><code>String dateString = getHeaderField(name);dateString = dateString.trim();</code></pre>', 'Method ignores return value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_IGNORED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_IGNORED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RpC_REPEATED_CONDITIONAL_TEST
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'RpC_REPEATED_CONDITIONAL_TEST', null, 'RpC_REPEATED_CONDITIONAL_TEST', 'java', null, 'RpC_REPEATED_CONDITIONAL_TEST', null, null, null, null, 'The code contains a conditional test is performed twice, one right after the other(e.g., <code>x == 0 || x == 0</code>). Perhaps the second occurrence is intended to be something else(e.g., <code>x == 0 || y == 0</code>).', 'Repeated conditional tests', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RpC_REPEATED_CONDITIONAL_TEST');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RpC_REPEATED_CONDITIONAL_TEST'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SA_FIELD_SELF_ASSIGNMENT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SA_FIELD_SELF_ASSIGNMENT', null, 'SA_FIELD_SELF_ASSIGNMENT', 'java', null, 'SA_FIELD_SELF_ASSIGNMENT', null, null, null, null, 'This method contains a self assignment of a field; e.g.<pre><code>int x;public void foo() { x = x;}</code></pre>Such assignments are useless, and may indicate a logic error or typo.', 'Self assignment of field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_FIELD_SELF_ASSIGNMENT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_FIELD_SELF_ASSIGNMENT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SA_FIELD_SELF_COMPARISON
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SA_FIELD_SELF_COMPARISON', null, 'SA_FIELD_SELF_COMPARISON', 'java', null, 'SA_FIELD_SELF_COMPARISON', null, null, null, null, 'This method compares a field with itself, and may indicate a typo ora logic error. Make sure that you are comparing the right things.', 'Self comparison of field with itself', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_FIELD_SELF_COMPARISON');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_FIELD_SELF_COMPARISON'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SA_FIELD_SELF_COMPUTATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SA_FIELD_SELF_COMPUTATION', null, 'SA_FIELD_SELF_COMPUTATION', 'java', null, 'SA_FIELD_SELF_COMPUTATION', null, null, null, null, 'This method performs a nonsensical computation of a field with another reference to the same field (e.g., x&x or x-x). Because of the nature of the computation, this operation doesn''t seem to make sense,and may indicate a typo ora logic error. Double check the computation.', 'Nonsensical self computation involving a field (e.g., x & x)', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_FIELD_SELF_COMPUTATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_FIELD_SELF_COMPUTATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SA_LOCAL_SELF_ASSIGNMENT_INSTEAD_OF_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SA_LOCAL_SELF_ASSIGNMENT_INSTEAD_OF_FIELD', null, 'SA_LOCAL_SELF_ASSIGNMENT_INSTEAD_OF_FIELD', 'java', null, 'SA_LOCAL_SELF_ASSIGNMENT_INSTEAD_OF_FIELD', null, null, null, null, 'This method contains a self assignment of a local variable, and there is a field with an identical name.assignment appears to have been ; e.g.<pre><code> int foo; public void setFoo(int foo) { foo = foo; }</code></pre>The assignment is useless. Did you mean to assign to the field instead?', 'Self assignment of local rather than assignment to field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_LOCAL_SELF_ASSIGNMENT_INSTEAD_OF_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_LOCAL_SELF_ASSIGNMENT_INSTEAD_OF_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SA_LOCAL_SELF_COMPARISON
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SA_LOCAL_SELF_COMPARISON', null, 'SA_LOCAL_SELF_COMPARISON', 'java', null, 'SA_LOCAL_SELF_COMPARISON', null, null, null, null, 'This method compares a local variable with itself, and may indicate a typo ora logic error. Make sure that you are comparing the right things.', 'Self comparison of value with itself', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_LOCAL_SELF_COMPARISON');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_LOCAL_SELF_COMPARISON'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SA_LOCAL_SELF_COMPUTATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SA_LOCAL_SELF_COMPUTATION', null, 'SA_LOCAL_SELF_COMPUTATION', 'java', null, 'SA_LOCAL_SELF_COMPUTATION', null, null, null, null, 'This method performs a nonsensical computation of a local variable with another reference to the same variable (e.g., x&x or x-x). Because of the nature of the computation, this operation doesn''t seem to make sense,and may indicate a typo ora logic error. Double check the computation.', 'Nonsensical self computation involving a variable (e.g., x & x)', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_LOCAL_SELF_COMPUTATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_LOCAL_SELF_COMPUTATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_METHOD_MUST_BE_PRIVATE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SE_METHOD_MUST_BE_PRIVATE', null, 'SE_METHOD_MUST_BE_PRIVATE', 'java', null, 'SE_METHOD_MUST_BE_PRIVATE', null, null, null, null, 'This class implements the <code>Serializable</code> interface, and defines a method for custom serialization/deserialization. But since that method isn''t declared private, it will be silently ignored by the serialization/deserialization API.', 'Method must be private in order for serialization to work', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_METHOD_MUST_BE_PRIVATE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_METHOD_MUST_BE_PRIVATE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_READ_RESOLVE_IS_STATIC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SE_READ_RESOLVE_IS_STATIC', null, 'SE_READ_RESOLVE_IS_STATIC', 'java', null, 'SE_READ_RESOLVE_IS_STATIC', null, null, null, null, 'In order for the readResolve method to be recognized by the serialization mechanism, it must not be declared as a static method.', 'The readResolve method must not be declared as a static method.', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_READ_RESOLVE_IS_STATIC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_READ_RESOLVE_IS_STATIC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH', null, 'SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH', 'java', null, 'SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH', null, null, null, null, 'A value stored in the previous switch case is overwritten here due to a switch fall through. It is likely that you forgot to put a break or return at the end of the previous case.', 'Dead store due to switch statement fall through', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH'),
 'STANDARD', 'CWE','484')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH_TO_THROW
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH_TO_THROW', null, 'SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH_TO_THROW', 'java', null, 'SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH_TO_THROW', null, null, null, null, 'A value stored in the previous switch case is ignored here due to a switch fall through to a place where an exception is thrown. It is likely that you forgot to put a break or return at the end of the previous case.', 'Dead store due to switch statement fall through to throw', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH_TO_THROW');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH_TO_THROW'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH_TO_THROW'),
 'STANDARD', 'CWE','484')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SIC_THREADLOCAL_DEADLY_EMBRACE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SIC_THREADLOCAL_DEADLY_EMBRACE', null, 'SIC_THREADLOCAL_DEADLY_EMBRACE', 'java', null, 'SIC_THREADLOCAL_DEADLY_EMBRACE', null, null, null, null, 'This class is an inner class, but should probably be a static inner class. As it is, there is a serious danger of a deadly embrace between the inner class and the thread local in the outer class. Because the inner class isn''t static, it retains a reference to the outer class. If the thread local contains a reference to an instance of the inner class, the inner and outer instance will both be reachable and not eligible for garbage collection.', 'Deadly embrace of non-static inner class and thread local', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SIC_THREADLOCAL_DEADLY_EMBRACE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SIC_THREADLOCAL_DEADLY_EMBRACE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SIO_SUPERFLUOUS_INSTANCEOF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SIO_SUPERFLUOUS_INSTANCEOF', null, 'SIO_SUPERFLUOUS_INSTANCEOF', 'java', null, 'SIO_SUPERFLUOUS_INSTANCEOF', null, null, null, null, 'Type check performed using the instanceof operator where it can be statically determined whether the object is of the type requested.', 'Unnecessary type check done using instanceof operator', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SIO_SUPERFLUOUS_INSTANCEOF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SIO_SUPERFLUOUS_INSTANCEOF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SQL_BAD_PREPARED_STATEMENT_ACCESS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SQL_BAD_PREPARED_STATEMENT_ACCESS', null, 'SQL_BAD_PREPARED_STATEMENT_ACCESS', 'java', null, 'SQL_BAD_PREPARED_STATEMENT_ACCESS', null, null, null, null, 'A call to a setXXX method of a prepared statement was made where the parameter index is 0. As parameter indexes start at index 1, this is always a mistake.', 'Method attempts to access a prepared statement parameter with index 0', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SQL_BAD_PREPARED_STATEMENT_ACCESS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SQL_BAD_PREPARED_STATEMENT_ACCESS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SQL_BAD_RESULTSET_ACCESS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'SQL_BAD_RESULTSET_ACCESS', null, 'SQL_BAD_RESULTSET_ACCESS', 'java', null, 'SQL_BAD_RESULTSET_ACCESS', null, null, null, null, 'A call to getXXX or updateXXX methods of a result set was made where the field index is 0. As ResultSet fields start at index 1, this is always a mistake.', 'Method attempts to access a result set field with index 0', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SQL_BAD_RESULTSET_ACCESS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SQL_BAD_RESULTSET_ACCESS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- STI_INTERRUPTED_ON_CURRENTTHREAD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'STI_INTERRUPTED_ON_CURRENTTHREAD', null, 'STI_INTERRUPTED_ON_CURRENTTHREAD', 'java', null, 'STI_INTERRUPTED_ON_CURRENTTHREAD', null, null, null, null, 'This method invokes the Thread.currentThread() call, just to call the interrupted() method. As interrupted() is a static method, is more simple and clear to use Thread.interrupted().', 'Unneeded use of currentThread() call, to call interrupted()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STI_INTERRUPTED_ON_CURRENTTHREAD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STI_INTERRUPTED_ON_CURRENTTHREAD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- STI_INTERRUPTED_ON_UNKNOWNTHREAD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'STI_INTERRUPTED_ON_UNKNOWNTHREAD', null, 'STI_INTERRUPTED_ON_UNKNOWNTHREAD', 'java', null, 'STI_INTERRUPTED_ON_UNKNOWNTHREAD', null, null, null, null, 'This method invokes the Thread.interrupted() method on a Thread object that appears to be a Thread object that is not the current thread. As the interrupted() method is static, the interrupted method will be called on a different object than the one the author intended.', 'Static Thread.interrupted() method invoked on thread instance', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STI_INTERRUPTED_ON_UNKNOWNTHREAD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STI_INTERRUPTED_ON_UNKNOWNTHREAD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TQ_ALWAYS_VALUE_USED_WHERE_NEVER_REQUIRED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'TQ_ALWAYS_VALUE_USED_WHERE_NEVER_REQUIRED', null, 'TQ_ALWAYS_VALUE_USED_WHERE_NEVER_REQUIRED', 'java', null, 'TQ_ALWAYS_VALUE_USED_WHERE_NEVER_REQUIRED', null, null, null, null, 'A value specified as carrying a type qualifier annotation is consumed in a location or locations requiring that the value not carry that annotation. More precisely, a value annotated with a type qualifier specifying when=ALWAYS is guaranteed to reach a use or uses where the same type qualifier specifies when=NEVER. For example, say that @NonNegative is a nickname for the type qualifier annotation @Negative(when=When.NEVER). The following code will generate this warning because the return statement requires a @NonNegative value, but receives one that is marked as @Negative. <pre><code>public @NonNegative Integer example(@Negative Integer value) { return value;}</code></pre>', 'Value annotated as carrying a type qualifier used where a value that must not carry that qualifier is required', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_ALWAYS_VALUE_USED_WHERE_NEVER_REQUIRED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_ALWAYS_VALUE_USED_WHERE_NEVER_REQUIRED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TQ_COMPARING_VALUES_WITH_INCOMPATIBLE_TYPE_QUALIFIERS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'TQ_COMPARING_VALUES_WITH_INCOMPATIBLE_TYPE_QUALIFIERS', null, 'TQ_COMPARING_VALUES_WITH_INCOMPATIBLE_TYPE_QUALIFIERS', 'java', null, 'TQ_COMPARING_VALUES_WITH_INCOMPATIBLE_TYPE_QUALIFIERS', null, null, null, null, 'A value specified as carrying a type qualifier annotation is compared with a value that doesn''t ever carry that qualifier. More precisely, a value annotated with a type qualifier specifying when=ALWAYS is compared with a value that where the same type qualifier specifies when=NEVER. For example, say that @NonNegative is a nickname for the type qualifier annotation @Negative(when=When.NEVER). The following code will generate this warning because the return statement requires a @NonNegative value, but receives one that is marked as @Negative. <pre><code>public boolean example(@Negative Integer value1, @NonNegative Integer value2) { return value1.equals(value2);}</code></pre>', 'Comparing values with incompatible type qualifiers', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_COMPARING_VALUES_WITH_INCOMPATIBLE_TYPE_QUALIFIERS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_COMPARING_VALUES_WITH_INCOMPATIBLE_TYPE_QUALIFIERS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TQ_MAYBE_SOURCE_VALUE_REACHES_ALWAYS_SINK
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'TQ_MAYBE_SOURCE_VALUE_REACHES_ALWAYS_SINK', null, 'TQ_MAYBE_SOURCE_VALUE_REACHES_ALWAYS_SINK', 'java', null, 'TQ_MAYBE_SOURCE_VALUE_REACHES_ALWAYS_SINK', null, null, null, null, 'A value that is annotated as possibility not being an instance of the values denoted by the type qualifier, and the value is guaranteed to be used in a way that requires values denoted by that type qualifier.', 'Value that might not carry a type qualifier is always used in a way requires that type qualifier', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_MAYBE_SOURCE_VALUE_REACHES_ALWAYS_SINK');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_MAYBE_SOURCE_VALUE_REACHES_ALWAYS_SINK'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TQ_MAYBE_SOURCE_VALUE_REACHES_NEVER_SINK
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'TQ_MAYBE_SOURCE_VALUE_REACHES_NEVER_SINK', null, 'TQ_MAYBE_SOURCE_VALUE_REACHES_NEVER_SINK', 'java', null, 'TQ_MAYBE_SOURCE_VALUE_REACHES_NEVER_SINK', null, null, null, null, 'A value that is annotated as possibility being an instance of the values denoted by the type qualifier, and the value is guaranteed to be used in a way that prohibits values denoted by that type qualifier.', 'Value that might carry a type qualifier is always used in a way prohibits it from having that type qualifier', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_MAYBE_SOURCE_VALUE_REACHES_NEVER_SINK');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_MAYBE_SOURCE_VALUE_REACHES_NEVER_SINK'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED', null, 'TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED', 'java', null, 'TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED', null, null, null, null, 'A value specified as not carrying a type qualifier annotation is guaranteed to be consumed in a location or locations requiring that the value does carry that annotation. More precisely, a value annotated with a type qualifier specifying when=NEVER is guaranteed to reach a use or uses where the same type qualifier specifies when=ALWAYS. TODO: example', 'Value annotated as never carrying a type qualifier used where value carrying that qualifier is required', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TQ_UNKNOWN_VALUE_USED_WHERE_ALWAYS_STRICTLY_REQUIRED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'TQ_UNKNOWN_VALUE_USED_WHERE_ALWAYS_STRICTLY_REQUIRED', null, 'TQ_UNKNOWN_VALUE_USED_WHERE_ALWAYS_STRICTLY_REQUIRED', 'java', null, 'TQ_UNKNOWN_VALUE_USED_WHERE_ALWAYS_STRICTLY_REQUIRED', null, null, null, null, 'A value is being used in a way that requires the value be annotation with a type qualifier. The type qualifier is strict, so the tool rejects any values that do not have the appropriate annotation. To coerce a value to have a strict annotation, define an identity function where the return value is annotated with the strict annotation. This is the only way to turn a non-annotated value into a value with a strict type qualifier annotation.', 'Value without a type qualifier used where a value is required to have that qualifier', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_UNKNOWN_VALUE_USED_WHERE_ALWAYS_STRICTLY_REQUIRED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_UNKNOWN_VALUE_USED_WHERE_ALWAYS_STRICTLY_REQUIRED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS', null, 'UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS', 'java', null, 'UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS', null, null, null, null, 'This anonymous class defines a method that is not directly invoked and does not override a method in a superclass. Since methods in other classes cannot directly invoke methods declared in an anonymous class, it seems that this method is uncallable. The method might simply be dead code, but it is also possible that the method is intended to override a method declared in a superclass, and due to a typo or other error the method does not,in fact, override the method it is intended to.', 'Uncallable method defined in anonymous class', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UR_UNINIT_READ
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'UR_UNINIT_READ', null, 'UR_UNINIT_READ', 'java', null, 'UR_UNINIT_READ', null, null, null, null, 'This constructor reads a field which has not yet been assigned a value.&nbsp; This is often caused when the programmer mistakenly uses the field instead of one of the constructor''s parameters.', 'Uninitialized read of field in constructor', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UR_UNINIT_READ');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UR_UNINIT_READ'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR', null, 'UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR', 'java', null, 'UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR', null, null, null, null, 'This method is invoked in the constructor of the superclass. At this point, the fields of the class have not yet initialized.To make this more concrete, consider the following classes:<pre><code>abstract class A { int hashCode; abstract Object getValue(); A() { hashCode = getValue().hashCode(); }}class B extends A { Object value; B(Object v) { this.value = v; } Object getValue() { return value; }}</code></pre>When a <code>B</code> is constructed,the constructor for the <code>A</code> class is invoked<em>before</em> the constructor for <code>B</code> sets <code>value</code>.Thus, when the constructor for <code>A</code> invokes <code>getValue</code>,an uninitialized value is read for <code>value</code>.', 'Uninitialized read of field method called from constructor of superclass', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UWF_NULL_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'UWF_NULL_FIELD', null, 'UWF_NULL_FIELD', 'java', null, 'UWF_NULL_FIELD', null, null, null, null, 'All writes to this field are of the constant value null, and thus all reads of the field will return null.Check for errors, or remove it if it is useless.', 'Field only ever set to null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UWF_NULL_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UWF_NULL_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UWF_UNWRITTEN_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'UWF_UNWRITTEN_FIELD', null, 'UWF_UNWRITTEN_FIELD', 'java', null, 'UWF_UNWRITTEN_FIELD', null, null, null, null, 'This field is never written.&nbsp; All reads of it will return the default value. Check for errors (should it have been initialized?), or remove it if it is useless.', 'Unwritten field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UWF_UNWRITTEN_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UWF_UNWRITTEN_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- VA_PRIMITIVE_ARRAY_PASSED_TO_OBJECT_VARARG
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'VA_PRIMITIVE_ARRAY_PASSED_TO_OBJECT_VARARG', null, 'VA_PRIMITIVE_ARRAY_PASSED_TO_OBJECT_VARARG', 'java', null, 'VA_PRIMITIVE_ARRAY_PASSED_TO_OBJECT_VARARG', null, null, null, null, 'This code passes a primitive array to a function that takes a variable number of object arguments.This creates an array of length one to hold the primitive array and passes it to the function.', 'Primitive array passed to function expecting a variable number of object arguments', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='VA_PRIMITIVE_ARRAY_PASSED_TO_OBJECT_VARARG');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='VA_PRIMITIVE_ARRAY_PASSED_TO_OBJECT_VARARG'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- VR_UNRESOLVABLE_REFERENCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'CORRECTNESS', 'VR_UNRESOLVABLE_REFERENCE', null, 'VR_UNRESOLVABLE_REFERENCE', 'java', null, 'VR_UNRESOLVABLE_REFERENCE', null, null, null, null, 'This class makes a reference to a class or method that can not be resolved using against the libraries it is being analyzed with.', 'Class makes reference to unresolvable class or method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='VR_UNRESOLVABLE_REFERENCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='VR_UNRESOLVABLE_REFERENCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


