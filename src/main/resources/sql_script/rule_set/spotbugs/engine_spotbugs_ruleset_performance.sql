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
-- BX_BOXING_IMMEDIATELY_UNBOXED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'BX_BOXING_IMMEDIATELY_UNBOXED', null, 'BX_BOXING_IMMEDIATELY_UNBOXED', 'java', null, 'BX_BOXING_IMMEDIATELY_UNBOXED', null, null, null, null, 'A primitive is boxed, and then immediately unboxed. This probably is due to a manual boxing in a place where an unboxed value is required, thus forcing the compiler to immediately undo the work of the boxing.', 'Primitive value is boxed and then immediately unboxed', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BX_BOXING_IMMEDIATELY_UNBOXED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BX_BOXING_IMMEDIATELY_UNBOXED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION', null, 'BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION', 'java', null, 'BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION', null, null, null, null, 'A primitive boxed value constructed and then immediately converted into a different primitive type(e.g., <code>new Double(d).intValue()</code>). Just perform direct primitive coercion (e.g., <code>(int) d</code>).', 'Primitive value is boxed then unboxed to perform primitive coercion', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION'),
 'STANDARD', 'CWE','192')
ON CONFLICT DO NOTHING;



-- ------------------------
-- BX_UNBOXED_AND_COERCED_FOR_TERNARY_OPERATOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'BX_UNBOXED_AND_COERCED_FOR_TERNARY_OPERATOR', null, 'BX_UNBOXED_AND_COERCED_FOR_TERNARY_OPERATOR', 'java', null, 'BX_UNBOXED_AND_COERCED_FOR_TERNARY_OPERATOR', null, null, null, null, 'A wrapped primitive value is unboxed and converted to another primitive type as part of the evaluation of a conditional ternary operator (the <code> b ? e1 : e2</code> operator). The semantics of Java mandate that if <code>e1</code> and <code>e2</code> are wrapped numeric values, the values are unboxed and converted/coerced to their common type (e.g,if <code>e1</code> is of type <code>Integer</code>and <code>e2</code> is of type <code>Float</code>, then <code>e1</code> is unboxed,converted to a floating point value, and boxed. See JLS Section 15.25.', 'Primitive value is unboxed and coerced for ternary operator', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BX_UNBOXED_AND_COERCED_FOR_TERNARY_OPERATOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BX_UNBOXED_AND_COERCED_FOR_TERNARY_OPERATOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BX_UNBOXING_IMMEDIATELY_REBOXED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'BX_UNBOXING_IMMEDIATELY_REBOXED', null, 'BX_UNBOXING_IMMEDIATELY_REBOXED', 'java', null, 'BX_UNBOXING_IMMEDIATELY_REBOXED', null, null, null, null, 'A boxed value is unboxed and then immediately reboxed.', 'Boxed value is unboxed and then immediately reboxed', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BX_UNBOXING_IMMEDIATELY_REBOXED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BX_UNBOXING_IMMEDIATELY_REBOXED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_BLOCKING_METHODS_ON_URL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DMI_BLOCKING_METHODS_ON_URL', null, 'DMI_BLOCKING_METHODS_ON_URL', 'java', null, 'DMI_BLOCKING_METHODS_ON_URL', null, null, null, null, 'The equals and hashCode method of URL perform domain name resolution, this can result in a big performance hit.See <a href="http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html">http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html</a> for more information.Consider using <code>java.net.URI</code> instead.', 'The equals and hashCode methods of URL are blocking', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_BLOCKING_METHODS_ON_URL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_BLOCKING_METHODS_ON_URL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_COLLECTION_OF_URLS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DMI_COLLECTION_OF_URLS', null, 'DMI_COLLECTION_OF_URLS', 'java', null, 'DMI_COLLECTION_OF_URLS', null, null, null, null, 'This method or field is or uses a Map or Set of URLs. Since both the equals and hashCode method of URL perform domain name resolution, this can result in a big performance hit.See <a href="http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html">http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html</a> for more information.Consider using <code>java.net.URI</code> instead.', 'Maps and sets of URLs can be performance hogs', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_COLLECTION_OF_URLS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_COLLECTION_OF_URLS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_BOOLEAN_CTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_BOOLEAN_CTOR', null, 'DM_BOOLEAN_CTOR', 'java', null, 'DM_BOOLEAN_CTOR', null, null, null, null, 'Creating new instances of <code>java.lang.Boolean</code> wastes memory, since <code>Boolean</code> objects are immutable and there are only two useful values of this type.&nbsp; Use the <code>Boolean.valueOf()</code> method (or Java 1.5 autoboxing) to create <code>Boolean</code> objects instead.', 'Method invokes inefficient Boolean constructor; use Boolean.valueOf(...) instead', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_BOOLEAN_CTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_BOOLEAN_CTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_BOXED_PRIMITIVE_FOR_COMPARE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_BOXED_PRIMITIVE_FOR_COMPARE', null, 'DM_BOXED_PRIMITIVE_FOR_COMPARE', 'java', null, 'DM_BOXED_PRIMITIVE_FOR_COMPARE', null, null, null, null, 'A boxed primitive is created just to call compareTo method. It''s more efficient to use static compare method (for double and float since Java 1.4, for other primitive types since Java 1.7) which works on primitives directly.', 'Boxing a primitive to compare', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_BOXED_PRIMITIVE_FOR_COMPARE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_BOXED_PRIMITIVE_FOR_COMPARE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_BOXED_PRIMITIVE_FOR_PARSING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_BOXED_PRIMITIVE_FOR_PARSING', null, 'DM_BOXED_PRIMITIVE_FOR_PARSING', 'java', null, 'DM_BOXED_PRIMITIVE_FOR_PARSING', null, null, null, null, 'A boxed primitive is created from a String, just to extract the unboxed primitive value. It is more efficient to just call the static parseXXX method.', 'Boxing/unboxing to parse a primitive', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_BOXED_PRIMITIVE_FOR_PARSING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_BOXED_PRIMITIVE_FOR_PARSING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_BOXED_PRIMITIVE_TOSTRING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_BOXED_PRIMITIVE_TOSTRING', null, 'DM_BOXED_PRIMITIVE_TOSTRING', 'java', null, 'DM_BOXED_PRIMITIVE_TOSTRING', null, null, null, null, 'A boxed primitive is allocated just to call toString(). It is more effective to just use the static form of toString which takes the primitive value. So, <table> <tr><th>Replace...</th><th>With this...</th></tr> <tr><td>new Integer(1).toString()</td><td>Integer.toString(1)</td></tr> <tr><td>new Long(1).toString()</td><td>Long.toString(1)</td></tr> <tr><td>new Float(1.0).toString()</td><td>Float.toString(1.0)</td></tr> <tr><td>new Double(1.0).toString()</td><td>Double.toString(1.0)</td></tr> <tr><td>new Byte(1).toString()</td><td>Byte.toString(1)</td></tr> <tr><td>new Short(1).toString()</td><td>Short.toString(1)</td></tr> <tr><td>new Boolean(true).toString()</td><td>Boolean.toString(true)</td></tr> </table>', 'Method allocates a boxed primitive just to call toString', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_BOXED_PRIMITIVE_TOSTRING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_BOXED_PRIMITIVE_TOSTRING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_FP_NUMBER_CTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_FP_NUMBER_CTOR', null, 'DM_FP_NUMBER_CTOR', 'java', null, 'DM_FP_NUMBER_CTOR', null, null, null, null, 'Using <code>new Double(double)</code> is guaranteed to always result in a new object whereas <code>Double.valueOf(double)</code> allows caching of values to be done by the compiler, class library, or JVM. Using of cached values avoids object allocation and the code will be faster. Unless the class must be compatible with JVMs predating Java 1.5, use either autoboxing or the <code>valueOf()</code> method when creating instances of <code>Double</code> and <code>Float</code>.', 'Method invokes inefficient floating-point Number constructor; use static valueOf instead', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_FP_NUMBER_CTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_FP_NUMBER_CTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_GC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_GC', null, 'DM_GC', 'java', null, 'DM_GC', null, null, null, null, 'Code explicitly invokes garbage collection. Except for specific use in benchmarking, this is very dubious. In the past, situations where people have explicitly invoked the garbage collector in routines such as close or finalize methods has led to huge performance black holes. Garbage collection can be expensive. Any situation that forces hundreds or thousands of garbage collections will bring the machine to a crawl.', 'Explicit garbage collection; extremely dubious except in benchmarking code', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_GC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_GC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_NEW_FOR_GETCLASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_NEW_FOR_GETCLASS', null, 'DM_NEW_FOR_GETCLASS', 'java', null, 'DM_NEW_FOR_GETCLASS', null, null, null, null, 'This method allocates an object just to call getClass() on it, in order to retrieve the Class object for it. It is simpler to just access the .class property of the class.', 'Method allocates an object, only to get the class object', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_NEW_FOR_GETCLASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_NEW_FOR_GETCLASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_NEXTINT_VIA_NEXTDOUBLE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_NEXTINT_VIA_NEXTDOUBLE', null, 'DM_NEXTINT_VIA_NEXTDOUBLE', 'java', null, 'DM_NEXTINT_VIA_NEXTDOUBLE', null, null, null, null, 'If <code>r</code> is a <code>java.util.Random</code>, you can generate a random number from <code>0</code> to <code>n-1</code>using <code>r.nextInt(n)</code>, rather than using <code>(int)(r.nextDouble() * n)</code>.The argument to nextInt must be positive. If, for example, you want to generate a random value from -99 to 0, use <code>-r.nextInt(100)</code>.', 'Use the nextInt method of Random rather than nextDouble to generate a random integer', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_NEXTINT_VIA_NEXTDOUBLE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_NEXTINT_VIA_NEXTDOUBLE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_NUMBER_CTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_NUMBER_CTOR', null, 'DM_NUMBER_CTOR', 'java', null, 'DM_NUMBER_CTOR', null, null, null, null, 'Using <code>new Integer(int)</code> is guaranteed to always result in a new object whereas <code>Integer.valueOf(int)</code> allows caching of values to be done by the compiler, class library, or JVM. Using of cached values avoids object allocation and the code will be faster. Values between -128 and 127 are guaranteed to have corresponding cached instances and using <code>valueOf</code> is approximately 3.5 times faster than using constructor. For values outside the constant range the performance of both styles is the same. Unless the class must be compatible with JVMs predating Java 1.5, use either autoboxing or the <code>valueOf()</code> method when creating instances of <code>Long</code>, <code>Integer</code>, <code>Short</code>, <code>Character</code>, and <code>Byte</code>.', 'Method invokes inefficient Number constructor; use static valueOf instead', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_NUMBER_CTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_NUMBER_CTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_STRING_CTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_STRING_CTOR', null, 'DM_STRING_CTOR', 'java', null, 'DM_STRING_CTOR', null, null, null, null, 'Using the <code>java.lang.String(String)</code> constructor wastes memory because the object so constructed will be functionally indistinguishable from the <code>String</code> passed as a parameter.&nbsp; Just use the argument <code>String</code> directly.', 'Method invokes inefficient new String(String) constructor', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_STRING_CTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_STRING_CTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_STRING_TOSTRING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_STRING_TOSTRING', null, 'DM_STRING_TOSTRING', 'java', null, 'DM_STRING_TOSTRING', null, null, null, null, 'Calling <code>String.toString()</code> is just a redundant operation. Just use the String.', 'Method invokes toString() method on a String', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_STRING_TOSTRING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_STRING_TOSTRING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_STRING_VOID_CTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'DM_STRING_VOID_CTOR', null, 'DM_STRING_VOID_CTOR', 'java', null, 'DM_STRING_VOID_CTOR', null, null, null, null, 'Creating a new <code>java.lang.String</code> object using the no-argument constructor wastes memory because the object so created will be functionally indistinguishable from the empty string constant <code>""</code>.&nbsp; Java guarantees that identical string constants will be represented by the same <code>String</code> object.&nbsp; Therefore, you should just use the empty string constant directly.', 'Method invokes inefficient new String() constructor', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_STRING_VOID_CTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_STRING_VOID_CTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- HSC_HUGE_SHARED_STRING_CONSTANT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'HSC_HUGE_SHARED_STRING_CONSTANT', null, 'HSC_HUGE_SHARED_STRING_CONSTANT', 'java', null, 'HSC_HUGE_SHARED_STRING_CONSTANT', null, null, null, null, 'A large String constant is duplicated across multiple class files. This is likely because a final field is initialized to a String constant, and the Java language mandates that all references to a final field from other classes be inlined into that classfile. See <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6447475">JDK bug 6447475</a> for a description of an occurrence of this bug in the JDK and how resolving it reduced the size of the JDK by 1 megabyte.', 'Huge string constants is duplicated across multiple class files', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HSC_HUGE_SHARED_STRING_CONSTANT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='HSC_HUGE_SHARED_STRING_CONSTANT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IIL_ELEMENTS_GET_LENGTH_IN_LOOP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'IIL_ELEMENTS_GET_LENGTH_IN_LOOP', null, 'IIL_ELEMENTS_GET_LENGTH_IN_LOOP', 'java', null, 'IIL_ELEMENTS_GET_LENGTH_IN_LOOP', null, null, null, null, 'The method calls NodeList.getLength() inside the loop and NodeList was produced by getElementsByTagName call.This NodeList doesn''t store its length, but computes it every time in not very optimal way.Consider storing the length to the variable before the loop.', 'NodeList.getLength() called in a loop', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIL_ELEMENTS_GET_LENGTH_IN_LOOP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIL_ELEMENTS_GET_LENGTH_IN_LOOP'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IIL_PATTERN_COMPILE_IN_LOOP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'IIL_PATTERN_COMPILE_IN_LOOP', null, 'IIL_PATTERN_COMPILE_IN_LOOP', 'java', null, 'IIL_PATTERN_COMPILE_IN_LOOP', null, null, null, null, 'The method calls Pattern.compile inside the loop passing the constant arguments.If the Pattern should be used several times there''s no reason to compile it for each loop iteration.Move this call outside of the loop or even into static final field.', 'Method calls Pattern.compile in a loop', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIL_PATTERN_COMPILE_IN_LOOP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIL_PATTERN_COMPILE_IN_LOOP'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IIL_PATTERN_COMPILE_IN_LOOP_INDIRECT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'IIL_PATTERN_COMPILE_IN_LOOP_INDIRECT', null, 'IIL_PATTERN_COMPILE_IN_LOOP_INDIRECT', 'java', null, 'IIL_PATTERN_COMPILE_IN_LOOP_INDIRECT', null, null, null, null, 'The method creates the same regular expression inside the loop, so it will be compiled every iteration.It would be more optimal to precompile this regular expression using Pattern.compile outside of the loop.', 'Method compiles the regular expression in a loop', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIL_PATTERN_COMPILE_IN_LOOP_INDIRECT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIL_PATTERN_COMPILE_IN_LOOP_INDIRECT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IIL_PREPARE_STATEMENT_IN_LOOP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'IIL_PREPARE_STATEMENT_IN_LOOP', null, 'IIL_PREPARE_STATEMENT_IN_LOOP', 'java', null, 'IIL_PREPARE_STATEMENT_IN_LOOP', null, null, null, null, 'The method calls Connection.prepareStatement inside the loop passing the constant arguments.If the PreparedStatement should be executed several times there''s no reason to recreate it for each loop iteration.Move this call outside of the loop.', 'Method calls prepareStatement in a loop', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIL_PREPARE_STATEMENT_IN_LOOP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIL_PREPARE_STATEMENT_IN_LOOP'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IIO_INEFFICIENT_INDEX_OF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'IIO_INEFFICIENT_INDEX_OF', null, 'IIO_INEFFICIENT_INDEX_OF', 'java', null, 'IIO_INEFFICIENT_INDEX_OF', null, null, null, null, 'This code passes a constant string of length 1 to String.indexOf().It is more efficient to use the integer implementations of String.indexOf().f. e. call <code>myString.indexOf(''.'')</code> instead of <code>myString.indexOf(".")</code>', 'Inefficient use of String.indexOf(String)', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIO_INEFFICIENT_INDEX_OF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIO_INEFFICIENT_INDEX_OF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IIO_INEFFICIENT_LAST_INDEX_OF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'IIO_INEFFICIENT_LAST_INDEX_OF', null, 'IIO_INEFFICIENT_LAST_INDEX_OF', 'java', null, 'IIO_INEFFICIENT_LAST_INDEX_OF', null, null, null, null, 'This code passes a constant string of length 1 to String.lastIndexOf().It is more efficient to use the integer implementations of String.lastIndexOf().f. e. call <code>myString.lastIndexOf(''.'')</code> instead of <code>myString.lastIndexOf(".")</code>', 'Inefficient use of String.lastIndexOf(String)', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIO_INEFFICIENT_LAST_INDEX_OF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IIO_INEFFICIENT_LAST_INDEX_OF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IMA_INEFFICIENT_MEMBER_ACCESS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'IMA_INEFFICIENT_MEMBER_ACCESS', null, 'IMA_INEFFICIENT_MEMBER_ACCESS', 'java', null, 'IMA_INEFFICIENT_MEMBER_ACCESS', null, null, null, null, 'This method of an inner class reads from or writes to a private member variable of the owning class, or calls a private method of the owning class. The compiler must generate a special method to access this private member, causing this to be less efficient. Relaxing the protection of the member variable or method will allow the compiler to treat this as a normal access.', 'Method accesses a private member variable of owning class', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IMA_INEFFICIENT_MEMBER_ACCESS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IMA_INEFFICIENT_MEMBER_ACCESS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ITA_INEFFICIENT_TO_ARRAY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'ITA_INEFFICIENT_TO_ARRAY', null, 'ITA_INEFFICIENT_TO_ARRAY', 'java', null, 'ITA_INEFFICIENT_TO_ARRAY', null, null, null, null, 'This method uses the toArray() method of a collection derived class, and passes in a zero-length prototype array argument. It is more efficient to use<code>myCollection.toArray(new Foo[myCollection.size()])</code>If the array passed in is big enough to store all of the elements of the collection, then it is populated and returned directly. This avoids the need to create a second array(by reflection) to return as the result.', 'Method uses toArray() with zero-length array argument', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ITA_INEFFICIENT_TO_ARRAY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ITA_INEFFICIENT_TO_ARRAY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SBSC_USE_STRINGBUFFER_CONCATENATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'SBSC_USE_STRINGBUFFER_CONCATENATION', null, 'SBSC_USE_STRINGBUFFER_CONCATENATION', 'java', null, 'SBSC_USE_STRINGBUFFER_CONCATENATION', null, null, null, null, 'The method seems to be building a String using concatenation in a loop.In each iteration, the String is converted to a StringBuffer/StringBuilder, appended to, and converted back to a String. This can lead to a cost quadratic in the number of iterations, as the growing string is recopied in each iteration. Better performance can be obtained by using a StringBuffer (or StringBuilder in Java 1.5) explicitly. For example:<pre><code>// This is badString s = "";for (int i = 0; i &lt; field.length; ++i) { s = s + field[i];}// This is betterStringBuffer buf = new StringBuffer();for (int i = 0; i &lt; field.length; ++i) { buf.append(field[i]);}String s = buf.toString();</code></pre>', 'Method concatenates strings using + in a loop', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SBSC_USE_STRINGBUFFER_CONCATENATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SBSC_USE_STRINGBUFFER_CONCATENATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SIC_INNER_SHOULD_BE_STATIC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'SIC_INNER_SHOULD_BE_STATIC', null, 'SIC_INNER_SHOULD_BE_STATIC', 'java', null, 'SIC_INNER_SHOULD_BE_STATIC', null, null, null, null, 'This class is an inner class, but does not use its embedded reference to the object which created it.&nbsp; This reference makes the instances of the class larger, and may keep the reference to the creator object alive longer than necessary.&nbsp; If possible, the class should be made static.', 'Should be a static inner class', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SIC_INNER_SHOULD_BE_STATIC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SIC_INNER_SHOULD_BE_STATIC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SIC_INNER_SHOULD_BE_STATIC_ANON
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'SIC_INNER_SHOULD_BE_STATIC_ANON', null, 'SIC_INNER_SHOULD_BE_STATIC_ANON', 'java', null, 'SIC_INNER_SHOULD_BE_STATIC_ANON', null, null, null, null, 'This class is an inner class, but does not use its embedded reference to the object which created it.&nbsp; This reference makes the instances of the class larger, and may keep the reference to the creator object alive longer than necessary.&nbsp; If possible, the class should be made into a <em>static</em> inner class. Since anonymous inner classes cannot be marked as static, doing this will require refactoring the inner class so that it is a named inner class.', 'Could be refactored into a named static inner class', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SIC_INNER_SHOULD_BE_STATIC_ANON');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SIC_INNER_SHOULD_BE_STATIC_ANON'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SIC_INNER_SHOULD_BE_STATIC_NEEDS_THIS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'SIC_INNER_SHOULD_BE_STATIC_NEEDS_THIS', null, 'SIC_INNER_SHOULD_BE_STATIC_NEEDS_THIS', 'java', null, 'SIC_INNER_SHOULD_BE_STATIC_NEEDS_THIS', null, null, null, null, 'This class is an inner class, but does not use its embedded reference to the object which created it except during construction of the inner object.&nbsp; This reference makes the instances of the class larger, and may keep the reference to the creator object alive longer than necessary.&nbsp; If possible, the class should be made into a <em>static</em> inner class. Since the reference to the outer object is required during construction of the inner instance, the inner class will need to be refactored so as to pass a reference to the outer instance to the constructor for the inner class.', 'Could be refactored into a static inner class', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SIC_INNER_SHOULD_BE_STATIC_NEEDS_THIS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SIC_INNER_SHOULD_BE_STATIC_NEEDS_THIS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SS_SHOULD_BE_STATIC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'SS_SHOULD_BE_STATIC', null, 'SS_SHOULD_BE_STATIC', 'java', null, 'SS_SHOULD_BE_STATIC', null, null, null, null, 'This class contains an instance final field that is initialized to a compile-time static value. Consider making the field static.', 'Unread field: should this field be static?', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SS_SHOULD_BE_STATIC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SS_SHOULD_BE_STATIC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UM_UNNECESSARY_MATH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'UM_UNNECESSARY_MATH', null, 'UM_UNNECESSARY_MATH', 'java', null, 'UM_UNNECESSARY_MATH', null, null, null, null, 'This method uses a static method from java.lang.Math on a constant value. This method''sresult in this case, can be determined statically, and is faster and sometimes more accurate to just use the constant. Methods detected are:<table><tr> <th>Method</th> <th>Parameter</th></tr><tr> <td>abs</td> <td>-any-</td></tr><tr> <td>acos</td> <td>0.0 or 1.0</td></tr><tr> <td>asin</td> <td>0.0 or 1.0</td></tr><tr> <td>atan</td> <td>0.0 or 1.0</td></tr><tr> <td>atan2</td> <td>0.0</td></tr><tr> <td>cbrt</td> <td>0.0 or 1.0</td></tr><tr> <td>ceil</td> <td>-any-</td></tr><tr> <td>cos</td> <td>0.0</td></tr><tr> <td>cosh</td> <td>0.0</td></tr><tr> <td>exp</td> <td>0.0 or 1.0</td></tr><tr> <td>expm1</td> <td>0.0</td></tr><tr> <td>floor</td> <td>-any-</td></tr><tr> <td>log</td> <td>0.0 or 1.0</td></tr><tr> <td>log10</td> <td>0.0 or 1.0</td></tr><tr> <td>rint</td> <td>-any-</td></tr><tr> <td>round</td> <td>-any-</td></tr><tr> <td>sin</td> <td>0.0</td></tr><tr> <td>sinh</td> <td>0.0</td></tr><tr> <td>sqrt</td> <td>0.0 or 1.0</td></tr><tr> <td>tan</td> <td>0.0</td></tr><tr> <td>tanh</td> <td>0.0</td></tr><tr> <td>toDegrees</td> <td>0.0 or 1.0</td></tr><tr> <td>toRadians</td> <td>0.0</td></tr></table>', 'Method calls static Math class method on a constant value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UM_UNNECESSARY_MATH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UM_UNNECESSARY_MATH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UPM_UNCALLED_PRIVATE_METHOD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'UPM_UNCALLED_PRIVATE_METHOD', null, 'UPM_UNCALLED_PRIVATE_METHOD', 'java', null, 'UPM_UNCALLED_PRIVATE_METHOD', null, null, null, null, 'This private method is never called. Although it is possible that the method will be invoked through reflection,it is more likely that the method is never used, and should be removed.', 'Private method is never called', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UPM_UNCALLED_PRIVATE_METHOD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UPM_UNCALLED_PRIVATE_METHOD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- URF_UNREAD_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'URF_UNREAD_FIELD', null, 'URF_UNREAD_FIELD', 'java', null, 'URF_UNREAD_FIELD', null, null, null, null, 'This field is never read.&nbsp; Consider removing it from the class.', 'Unread field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='URF_UNREAD_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='URF_UNREAD_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UUF_UNUSED_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'UUF_UNUSED_FIELD', null, 'UUF_UNUSED_FIELD', 'java', null, 'UUF_UNUSED_FIELD', null, null, null, null, 'This field is never used.&nbsp; Consider removing it from the class.', 'Unused field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UUF_UNUSED_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UUF_UNUSED_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- WMI_WRONG_MAP_ITERATOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'PERFORMANCE', 'WMI_WRONG_MAP_ITERATOR', null, 'WMI_WRONG_MAP_ITERATOR', 'java', null, 'WMI_WRONG_MAP_ITERATOR', null, null, null, null, 'This method accesses the value of a Map entry, using a key that was retrieved from a keySet iterator. It is more efficient to use an iterator on the entrySet of the map, to avoid theMap.get(key) lookup.', 'Inefficient use of keySet iterator instead of entrySet iterator', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='WMI_WRONG_MAP_ITERATOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='WMI_WRONG_MAP_ITERATOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


