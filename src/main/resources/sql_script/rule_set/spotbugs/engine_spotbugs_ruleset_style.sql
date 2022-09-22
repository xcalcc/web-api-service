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
-- BC_BAD_CAST_TO_ABSTRACT_COLLECTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'BC_BAD_CAST_TO_ABSTRACT_COLLECTION', null, 'BC_BAD_CAST_TO_ABSTRACT_COLLECTION', 'java', null, 'BC_BAD_CAST_TO_ABSTRACT_COLLECTION', null, null, null, null, 'This code casts a Collection to an abstract collection(such as <code>List</code>, <code>Set</code>, or <code>Map</code>).Ensure that you are guaranteed that the object is of the type you are casting to. If all you need is to be able to iterate through a collection, you don''t need to cast it to a Set or List.', 'Questionable cast to abstract collection', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_BAD_CAST_TO_ABSTRACT_COLLECTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_BAD_CAST_TO_ABSTRACT_COLLECTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BC_BAD_CAST_TO_CONCRETE_COLLECTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'BC_BAD_CAST_TO_CONCRETE_COLLECTION', null, 'BC_BAD_CAST_TO_CONCRETE_COLLECTION', 'java', null, 'BC_BAD_CAST_TO_CONCRETE_COLLECTION', null, null, null, null, 'This code casts an abstract collection (such as a Collection, List, or Set)to a specific concrete implementation (such as an ArrayList or HashSet).This might not be correct, and it may make your code fragile, since it makes it harder to switch to other concrete implementations at a future point. Unless you have a particular reason to do so, just use the abstract collection class.', 'Questionable cast to concrete collection', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_BAD_CAST_TO_CONCRETE_COLLECTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_BAD_CAST_TO_CONCRETE_COLLECTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BC_UNCONFIRMED_CAST
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'BC_UNCONFIRMED_CAST', null, 'BC_UNCONFIRMED_CAST', 'java', null, 'BC_UNCONFIRMED_CAST', null, null, null, null, 'This cast is unchecked, and not all instances of the type casted from can be cast to the type it is being cast to. Check that your program logic ensures that this cast will not fail.', 'Unchecked/unconfirmed cast', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_UNCONFIRMED_CAST');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_UNCONFIRMED_CAST'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BC_UNCONFIRMED_CAST_OF_RETURN_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'BC_UNCONFIRMED_CAST_OF_RETURN_VALUE', null, 'BC_UNCONFIRMED_CAST_OF_RETURN_VALUE', 'java', null, 'BC_UNCONFIRMED_CAST_OF_RETURN_VALUE', null, null, null, null, 'This code performs an unchecked cast of the return value of a method.The code might be calling the method in such a way that the cast is guaranteed to be safe, but SpotBugs is unable to verify that the cast is safe. Check that your program logic ensures that this cast will not fail.', 'Unchecked/unconfirmed cast of return value from method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_UNCONFIRMED_CAST_OF_RETURN_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_UNCONFIRMED_CAST_OF_RETURN_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- BC_VACUOUS_INSTANCEOF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'BC_VACUOUS_INSTANCEOF', null, 'BC_VACUOUS_INSTANCEOF', 'java', null, 'BC_VACUOUS_INSTANCEOF', null, null, null, null, 'This instanceof test will always return true (unless the value being tested is null).Although this is safe, make sure it isn''tan indication of some misunderstanding or some other logic error.If you really want to test the value for being null, perhaps it would be clearer to do better to do a null test rather than an instanceof test.', 'instanceof will always return true', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_VACUOUS_INSTANCEOF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_VACUOUS_INSTANCEOF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='BC_VACUOUS_INSTANCEOF'),
 'STANDARD', 'CWE','571')
ON CONFLICT DO NOTHING;



-- ------------------------
-- CAA_COVARIANT_ARRAY_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'CAA_COVARIANT_ARRAY_FIELD', null, 'CAA_COVARIANT_ARRAY_FIELD', 'java', null, 'CAA_COVARIANT_ARRAY_FIELD', null, null, null, null, 'Array of covariant type is assigned to a field. This is confusing and may lead to ArrayStoreException at run time if the reference of some other type will be stored in this array later like in the following code:<pre><code>Number[] arr = new Integer[10];arr[0] = 1.0;</code></pre>Consider changing the type of created array or the field type.', 'Covariant array assignment to a field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CAA_COVARIANT_ARRAY_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CAA_COVARIANT_ARRAY_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CAA_COVARIANT_ARRAY_LOCAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'CAA_COVARIANT_ARRAY_LOCAL', null, 'CAA_COVARIANT_ARRAY_LOCAL', 'java', null, 'CAA_COVARIANT_ARRAY_LOCAL', null, null, null, null, 'Array of covariant type is assigned to a local variable. This is confusing and may lead to ArrayStoreException at run time if the reference of some other type will be stored in this array later like in the following code:<pre><code>Number[] arr = new Integer[10];arr[0] = 1.0;</code></pre>Consider changing the type of created array or the local variable type.', 'Covariant array assignment to a local variable', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CAA_COVARIANT_ARRAY_LOCAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CAA_COVARIANT_ARRAY_LOCAL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CAA_COVARIANT_ARRAY_RETURN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'CAA_COVARIANT_ARRAY_RETURN', null, 'CAA_COVARIANT_ARRAY_RETURN', 'java', null, 'CAA_COVARIANT_ARRAY_RETURN', null, null, null, null, 'Array of covariant type is returned from the method. This is confusing and may lead to ArrayStoreException at run time if the calling code will try to store the reference of some other type in the returned array.Consider changing the type of created array or the method return type.', 'Covariant array is returned from the method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CAA_COVARIANT_ARRAY_RETURN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CAA_COVARIANT_ARRAY_RETURN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CD_CIRCULAR_DEPENDENCY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'CD_CIRCULAR_DEPENDENCY', null, 'CD_CIRCULAR_DEPENDENCY', 'java', null, 'CD_CIRCULAR_DEPENDENCY', null, null, null, null, 'This class has a circular dependency with other classes. This makes building these classes difficult, as each is dependent on the other to build correctly. Consider using interfaces to break the hard dependency.', 'Test for circular dependencies among classes', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CD_CIRCULAR_DEPENDENCY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CD_CIRCULAR_DEPENDENCY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- CI_CONFUSED_INHERITANCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'CI_CONFUSED_INHERITANCE', null, 'CI_CONFUSED_INHERITANCE', 'java', null, 'CI_CONFUSED_INHERITANCE', null, null, null, null, 'This class is declared to be final, but declares fields to be protected. Since the class is final, it can not be derived from, and the use of protected is confusing. The access modifier for the field should be changed to private or public to represent the true use for the field.', 'Class is final but declares protected field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CI_CONFUSED_INHERITANCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='CI_CONFUSED_INHERITANCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DB_DUPLICATE_BRANCHES
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DB_DUPLICATE_BRANCHES', null, 'DB_DUPLICATE_BRANCHES', 'java', null, 'DB_DUPLICATE_BRANCHES', null, null, null, null, 'This method uses the same code to implement two branches of a conditional branch. Check to ensure that this isn''t a coding mistake.', 'Method uses the same code for two branches', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DB_DUPLICATE_BRANCHES');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DB_DUPLICATE_BRANCHES'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DB_DUPLICATE_SWITCH_CLAUSES
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DB_DUPLICATE_SWITCH_CLAUSES', null, 'DB_DUPLICATE_SWITCH_CLAUSES', 'java', null, 'DB_DUPLICATE_SWITCH_CLAUSES', null, null, null, null, 'This method uses the same code to implement two clauses of a switch statement. This could be a case of duplicate code, but it might also indicate a coding mistake.', 'Method uses the same code for two switch clauses', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DB_DUPLICATE_SWITCH_CLAUSES');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DB_DUPLICATE_SWITCH_CLAUSES'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DLS_DEAD_LOCAL_STORE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DLS_DEAD_LOCAL_STORE', null, 'DLS_DEAD_LOCAL_STORE', 'java', null, 'DLS_DEAD_LOCAL_STORE', null, null, null, null, 'This instruction assigns a value to a local variable,but the value is not read or used in any subsequent instruction.Often, this indicates an error, because the value computed is never used.Note that Sun''s javac compiler often generates dead stores for final local variables. Because SpotBugs is a bytecode-based tool,there is no easy way to eliminate these false positives.', 'Dead store to local variable', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_LOCAL_STORE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_LOCAL_STORE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DLS_DEAD_LOCAL_STORE_IN_RETURN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DLS_DEAD_LOCAL_STORE_IN_RETURN', null, 'DLS_DEAD_LOCAL_STORE_IN_RETURN', 'java', null, 'DLS_DEAD_LOCAL_STORE_IN_RETURN', null, null, null, null, 'This statement assigns to a local variable in a return statement. This assignment has effect. Please verify that this statement does the right thing.', 'Useless assignment in return statement', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_LOCAL_STORE_IN_RETURN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_LOCAL_STORE_IN_RETURN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DLS_DEAD_LOCAL_STORE_OF_NULL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DLS_DEAD_LOCAL_STORE_OF_NULL', null, 'DLS_DEAD_LOCAL_STORE_OF_NULL', 'java', null, 'DLS_DEAD_LOCAL_STORE_OF_NULL', null, null, null, null, 'The code stores null into a local variable, and the stored value is not read. This store may have been introduced to assist the garbage collector, but as of Java SE 6.0, this is no longer needed or useful.', 'Dead store of null to local variable', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_LOCAL_STORE_OF_NULL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_LOCAL_STORE_OF_NULL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DLS_DEAD_LOCAL_STORE_SHADOWS_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DLS_DEAD_LOCAL_STORE_SHADOWS_FIELD', null, 'DLS_DEAD_LOCAL_STORE_SHADOWS_FIELD', 'java', null, 'DLS_DEAD_LOCAL_STORE_SHADOWS_FIELD', null, null, null, null, 'This instruction assigns a value to a local variable,but the value is not read or used in any subsequent instruction.Often, this indicates an error, because the value computed is never used. There is a field with the same name as the local variable. Did you mean to assign to that variable instead?', 'Dead store to local variable that shadows field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_LOCAL_STORE_SHADOWS_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DLS_DEAD_LOCAL_STORE_SHADOWS_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_HARDCODED_ABSOLUTE_FILENAME
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DMI_HARDCODED_ABSOLUTE_FILENAME', null, 'DMI_HARDCODED_ABSOLUTE_FILENAME', 'java', null, 'DMI_HARDCODED_ABSOLUTE_FILENAME', null, null, null, null, 'This code constructs a File object using a hard coded to an absolute pathname(e.g., <code>new File("/home/dannyc/workspace/j2ee/src/share/com/sun/enterprise/deployment");</code>', 'Code contains a hard coded reference to an absolute pathname', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_HARDCODED_ABSOLUTE_FILENAME');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_HARDCODED_ABSOLUTE_FILENAME'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_NONSERIALIZABLE_OBJECT_WRITTEN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DMI_NONSERIALIZABLE_OBJECT_WRITTEN', null, 'DMI_NONSERIALIZABLE_OBJECT_WRITTEN', 'java', null, 'DMI_NONSERIALIZABLE_OBJECT_WRITTEN', null, null, null, null, 'This code seems to be passing a non-serializable object to the ObjectOutput.writeObject method.If the object is, indeed, non-serializable, an error will result.', 'Non serializable object written to ObjectOutput', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_NONSERIALIZABLE_OBJECT_WRITTEN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_NONSERIALIZABLE_OBJECT_WRITTEN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_THREAD_PASSED_WHERE_RUNNABLE_EXPECTED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DMI_THREAD_PASSED_WHERE_RUNNABLE_EXPECTED', null, 'DMI_THREAD_PASSED_WHERE_RUNNABLE_EXPECTED', 'java', null, 'DMI_THREAD_PASSED_WHERE_RUNNABLE_EXPECTED', null, null, null, null, 'A Thread object is passed as a parameter to a method where a Runnable is expected. This is rather unusual, and may indicate a logic error cause unexpected behavior.', 'Thread passed where Runnable expected', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_THREAD_PASSED_WHERE_RUNNABLE_EXPECTED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_THREAD_PASSED_WHERE_RUNNABLE_EXPECTED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_UNSUPPORTED_METHOD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DMI_UNSUPPORTED_METHOD', null, 'DMI_UNSUPPORTED_METHOD', 'java', null, 'DMI_UNSUPPORTED_METHOD', null, null, null, null, 'All targets of this method invocation throw an UnsupportedOperationException.', 'Call to unsupported method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_UNSUPPORTED_METHOD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_UNSUPPORTED_METHOD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DMI_USELESS_SUBSTRING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'DMI_USELESS_SUBSTRING', null, 'DMI_USELESS_SUBSTRING', 'java', null, 'DMI_USELESS_SUBSTRING', null, null, null, null, 'This code invokes substring(0) on a String, which returns the original value.', 'Invocation of substring(0), which returns the original value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_USELESS_SUBSTRING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DMI_USELESS_SUBSTRING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_DOESNT_OVERRIDE_EQUALS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'EQ_DOESNT_OVERRIDE_EQUALS', null, 'EQ_DOESNT_OVERRIDE_EQUALS', 'java', null, 'EQ_DOESNT_OVERRIDE_EQUALS', null, null, null, null, 'This class extends a class that defines an equals method and adds fields, but doesn''tdefine an equals method itself. Thus, equality on instances of this class will ignore the identity of the subclass and the added fields. Be sure this is what is intended,and that you don''t need to override the equals method. Even if you don''t need to override the equals method, consider overriding it anyway to document the fact that the equals method for the subclass just return the result of invoking super.equals(o).', 'Class doesn''t override equals in superclass', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_DOESNT_OVERRIDE_EQUALS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_DOESNT_OVERRIDE_EQUALS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- EQ_UNUSUAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'EQ_UNUSUAL', null, 'EQ_UNUSUAL', 'java', null, 'EQ_UNUSUAL', null, null, null, null, 'This class doesn''t do any of the patterns we recognize for checking that the type of the argument is compatible with the type of the <code>this</code> object. There might not be anything wrong with this code, but it is worth reviewing.', 'Unusual equals method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_UNUSUAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='EQ_UNUSUAL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- FE_FLOATING_POINT_EQUALITY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'FE_FLOATING_POINT_EQUALITY', null, 'FE_FLOATING_POINT_EQUALITY', 'java', null, 'FE_FLOATING_POINT_EQUALITY', null, null, null, null, 'This operation compares two floating point values for equality. Because floating point calculations may involve rounding, calculated float and double values may not be accurate. For values that must be precise, such as monetary values, consider using a fixed-precision type such as BigDecimal. For values that need not be precise, consider comparing for equality within some range, for example: <code>if ( Math.abs(x - y) &lt; .0000001 )</code>. See the Java Language Specification, section 4.2.4.', 'Test for floating point equality', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FE_FLOATING_POINT_EQUALITY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='FE_FLOATING_POINT_EQUALITY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IA_AMBIGUOUS_INVOCATION_OF_INHERITED_OR_OUTER_METHOD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'IA_AMBIGUOUS_INVOCATION_OF_INHERITED_OR_OUTER_METHOD', null, 'IA_AMBIGUOUS_INVOCATION_OF_INHERITED_OR_OUTER_METHOD', 'java', null, 'IA_AMBIGUOUS_INVOCATION_OF_INHERITED_OR_OUTER_METHOD', null, null, null, null, 'An inner class is invoking a method that could be resolved to either a inherited method or a method defined in an outer class.For example, you invoke <code>foo(17)</code>, which is defined in both a superclass and in an outer method.By the Java semantics,it will be resolved to invoke the inherited method, but this may not be what you intend.If you really intend to invoke the inherited method,invoke it by invoking the method on super (e.g., invoke super.foo(17)), and thus it will be clear to other readers of your code and to SpotBugs that you want to invoke the inherited method, not the method in the outer class.If you call <code>this.foo(17)</code>, then the inherited method will be invoked. However, since SpotBugs only looks at class files, it can''t tell the difference between an invocation of <code>this.foo(17)</code> and <code>foo(17)</code>, it will still complain about a potential ambiguous invocation.', 'Potentially ambiguous invocation of either an inherited or outer method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IA_AMBIGUOUS_INVOCATION_OF_INHERITED_OR_OUTER_METHOD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IA_AMBIGUOUS_INVOCATION_OF_INHERITED_OR_OUTER_METHOD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ICAST_IDIV_CAST_TO_DOUBLE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'ICAST_IDIV_CAST_TO_DOUBLE', null, 'ICAST_IDIV_CAST_TO_DOUBLE', 'java', null, 'ICAST_IDIV_CAST_TO_DOUBLE', null, null, null, null, 'This code casts the result of an integral division (e.g., int or long division)operation to double or float.Doing division on integers truncates the result to the integer value closest to zero. The fact that the result was cast to double suggests that this precision should have been retained.What was probably meant was to cast one or both of the operands to double <em>before</em> performing the division. Here is an example:<pre><code>int x = 2;int y = 5;// Wrong: yields result 0.0double value1 = x / y;// Right: yields result 0.4double value2 = x / (double) y;</code></pre>', 'Integral division result cast to double or float', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_IDIV_CAST_TO_DOUBLE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_IDIV_CAST_TO_DOUBLE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ICAST_INTEGER_MULTIPLY_CAST_TO_LONG
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'ICAST_INTEGER_MULTIPLY_CAST_TO_LONG', null, 'ICAST_INTEGER_MULTIPLY_CAST_TO_LONG', 'java', null, 'ICAST_INTEGER_MULTIPLY_CAST_TO_LONG', null, null, null, null, 'This code performs integer multiply and then converts the result to a long,as in:<pre><code>long convertDaysToMilliseconds(int days) { return 1000*3600*24*days; }</code></pre>If the multiplication is done using long arithmetic, you can avoid the possibility that the result will overflow. For example, you could fix the above code to:<pre><code>long convertDaysToMilliseconds(int days) { return 1000L*3600*24*days; }</code></pre>or<pre><code>static final long MILLISECONDS_PER_DAY = 24L*3600*1000;long convertDaysToMilliseconds(int days) { return days * MILLISECONDS_PER_DAY; }</code></pre>', 'Result of integer multiplication cast to long', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_INTEGER_MULTIPLY_CAST_TO_LONG');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_INTEGER_MULTIPLY_CAST_TO_LONG'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ICAST_QUESTIONABLE_UNSIGNED_RIGHT_SHIFT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'ICAST_QUESTIONABLE_UNSIGNED_RIGHT_SHIFT', null, 'ICAST_QUESTIONABLE_UNSIGNED_RIGHT_SHIFT', 'java', null, 'ICAST_QUESTIONABLE_UNSIGNED_RIGHT_SHIFT', null, null, null, null, 'The code performs an unsigned right shift, whose result is then cast to a short or byte, which discards the upper bits of the result.Since the upper bits are discarded, there may be no difference between a signed and unsigned right shift (depending upon the size of the shift).', 'Unsigned right shift cast to short/byte', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_QUESTIONABLE_UNSIGNED_RIGHT_SHIFT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ICAST_QUESTIONABLE_UNSIGNED_RIGHT_SHIFT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IC_INIT_CIRCULARITY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'IC_INIT_CIRCULARITY', null, 'IC_INIT_CIRCULARITY', 'java', null, 'IC_INIT_CIRCULARITY', null, null, null, null, 'A circularity was detected in the static initializers of the two classes referenced by the bug instance.&nbsp; Many kinds of unexpected behavior may arise from such circularity.', 'Initialization circularity', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IC_INIT_CIRCULARITY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IC_INIT_CIRCULARITY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IM_AVERAGE_COMPUTATION_COULD_OVERFLOW
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'IM_AVERAGE_COMPUTATION_COULD_OVERFLOW', null, 'IM_AVERAGE_COMPUTATION_COULD_OVERFLOW', 'java', null, 'IM_AVERAGE_COMPUTATION_COULD_OVERFLOW', null, null, null, null, 'The code computes the average of two integers using either division or signed right shift,and then uses the result as the index of an array.If the values being averaged are very large, this can overflow (resulting in the computation of a negative average). Assuming that the result is intended to be nonnegative, you can use an unsigned right shift instead. In other words, rather that using <code>(low+high)/2</code>,use <code>(low+high) &gt;&gt;&gt; 1</code>This bug exists in many earlier implementations of binary search and merge sort.Martin Buchholz <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6412541">found and fixed it</a>in the JDK libraries, and Joshua Bloch<a href="http://googleresearch.blogspot.com/2006/06/extra-extra-read-all-about-it-nearly.html">widelypublicized the bug pattern</a>.', 'Computation of average could overflow', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IM_AVERAGE_COMPUTATION_COULD_OVERFLOW');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IM_AVERAGE_COMPUTATION_COULD_OVERFLOW'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IM_BAD_CHECK_FOR_ODD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'IM_BAD_CHECK_FOR_ODD', null, 'IM_BAD_CHECK_FOR_ODD', 'java', null, 'IM_BAD_CHECK_FOR_ODD', null, null, null, null, 'The code uses x % 2 == 1 to check to see if a value is odd, but this won''t work for negative numbers (e.g., (-5) % 2 == -1). If this code is intending to check for oddness, consider using x &amp; 1 == 1, or x % 2 != 0.', 'Check for oddness that won''t work for negative numbers', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IM_BAD_CHECK_FOR_ODD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IM_BAD_CHECK_FOR_ODD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- INT_BAD_REM_BY_1
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'INT_BAD_REM_BY_1', null, 'INT_BAD_REM_BY_1', 'java', null, 'INT_BAD_REM_BY_1', null, null, null, null, 'Any expression (exp % 1) is guaranteed to always return zero.Did you mean (exp &amp; 1) or (exp % 2) instead?', 'Integer remainder modulo 1', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_BAD_REM_BY_1');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_BAD_REM_BY_1'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- INT_VACUOUS_BIT_OPERATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'INT_VACUOUS_BIT_OPERATION', null, 'INT_VACUOUS_BIT_OPERATION', 'java', null, 'INT_VACUOUS_BIT_OPERATION', null, null, null, null, 'This is an integer bit operation (and, or, or exclusive or) that doesn''t do any useful work(e.g., <code>v & 0xffffffff</code>).', 'Vacuous bit mask operation on integer value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_VACUOUS_BIT_OPERATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_VACUOUS_BIT_OPERATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- INT_VACUOUS_COMPARISON
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'INT_VACUOUS_COMPARISON', null, 'INT_VACUOUS_COMPARISON', 'java', null, 'INT_VACUOUS_COMPARISON', null, null, null, null, 'There is an integer comparison that always returns the same value (e.g., x &lt;= Integer.MAX_VALUE).', 'Vacuous comparison of integer value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_VACUOUS_COMPARISON');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='INT_VACUOUS_COMPARISON'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MTIA_SUSPECT_SERVLET_INSTANCE_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'MTIA_SUSPECT_SERVLET_INSTANCE_FIELD', null, 'MTIA_SUSPECT_SERVLET_INSTANCE_FIELD', 'java', null, 'MTIA_SUSPECT_SERVLET_INSTANCE_FIELD', null, null, null, null, 'This class extends from a Servlet class, and uses an instance member variable. Since only one instance of a Servlet class is created by the J2EE framework, and used in a multithreaded way, this paradigm is highly discouraged and most likely problematic. Consider only using method local variables.', 'Class extends Servlet class and uses instance variables', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MTIA_SUSPECT_SERVLET_INSTANCE_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MTIA_SUSPECT_SERVLET_INSTANCE_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MTIA_SUSPECT_STRUTS_INSTANCE_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'MTIA_SUSPECT_STRUTS_INSTANCE_FIELD', null, 'MTIA_SUSPECT_STRUTS_INSTANCE_FIELD', 'java', null, 'MTIA_SUSPECT_STRUTS_INSTANCE_FIELD', null, null, null, null, 'This class extends from a Struts Action class, and uses an instance member variable. Since only one instance of a struts Action class is created by the Struts framework, and used in a multithreaded way, this paradigm is highly discouraged and most likely problematic. Consider only using method local variables. Only instance fields that are written outside of a monitor are reported.', 'Class extends Struts Action class and uses instance variables', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MTIA_SUSPECT_STRUTS_INSTANCE_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MTIA_SUSPECT_STRUTS_INSTANCE_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_DEREFERENCE_OF_READLINE_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NP_DEREFERENCE_OF_READLINE_VALUE', null, 'NP_DEREFERENCE_OF_READLINE_VALUE', 'java', null, 'NP_DEREFERENCE_OF_READLINE_VALUE', null, null, null, null, 'The result of invoking readLine() is dereferenced without checking to see if the result is null. If there are no more lines of text to read, readLine() will return null and dereferencing that will generate a null pointer exception.', 'Dereference of the result of readLine() without nullcheck', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_DEREFERENCE_OF_READLINE_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_DEREFERENCE_OF_READLINE_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_IMMEDIATE_DEREFERENCE_OF_READLINE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NP_IMMEDIATE_DEREFERENCE_OF_READLINE', null, 'NP_IMMEDIATE_DEREFERENCE_OF_READLINE', 'java', null, 'NP_IMMEDIATE_DEREFERENCE_OF_READLINE', null, null, null, null, 'The result of invoking readLine() is immediately dereferenced. If there are no more lines of text to read, readLine() will return null and dereferencing that will generate a null pointer exception.', 'Immediate dereference of the result of readLine()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_IMMEDIATE_DEREFERENCE_OF_READLINE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_IMMEDIATE_DEREFERENCE_OF_READLINE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_LOAD_OF_KNOWN_NULL_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NP_LOAD_OF_KNOWN_NULL_VALUE', null, 'NP_LOAD_OF_KNOWN_NULL_VALUE', 'java', null, 'NP_LOAD_OF_KNOWN_NULL_VALUE', null, null, null, null, 'The variable referenced at this point is known to be null due to an earlier check against null. Although this is valid, it might be a mistake (perhaps you intended to refer to a different variable, or perhaps the earlier check to see if the variable is null should have been a check to see if it was non-null).', 'Load of known null value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_LOAD_OF_KNOWN_NULL_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_LOAD_OF_KNOWN_NULL_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_METHOD_PARAMETER_RELAXING_ANNOTATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NP_METHOD_PARAMETER_RELAXING_ANNOTATION', null, 'NP_METHOD_PARAMETER_RELAXING_ANNOTATION', 'java', null, 'NP_METHOD_PARAMETER_RELAXING_ANNOTATION', null, null, null, null, 'A method should always implement the contract of a method it overrides. Thus, if a method takes a parameter that is marked as @Nullable, you shouldn''t override that method in a subclass with a method where that parameter is @Nonnull. Doing so violates the contract that the method should handle a null parameter.', 'Method tightens nullness annotation on parameter', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_METHOD_PARAMETER_RELAXING_ANNOTATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_METHOD_PARAMETER_RELAXING_ANNOTATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION', null, 'NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION', 'java', null, 'NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION', null, null, null, null, 'A method should always implement the contract of a method it overrides. Thus, if a method takes a parameter that is marked as @Nullable, you shouldn''t override that method in a subclass with a method where that parameter is @Nonnull. Doing so violates the contract that the method should handle a null parameter.', 'Method tightens nullness annotation on parameter', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_METHOD_RETURN_RELAXING_ANNOTATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NP_METHOD_RETURN_RELAXING_ANNOTATION', null, 'NP_METHOD_RETURN_RELAXING_ANNOTATION', 'java', null, 'NP_METHOD_RETURN_RELAXING_ANNOTATION', null, null, null, null, 'A method should always implement the contract of a method it overrides. Thus, if a method takes is annotated as returning a @Nonnull value, you shouldn''t override that method in a subclass with a method annotated as returning a @Nullable or @CheckForNull value. Doing so violates the contract that the method shouldn''t return null.', 'Method relaxes nullness annotation on return value', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_METHOD_RETURN_RELAXING_ANNOTATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_METHOD_RETURN_RELAXING_ANNOTATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE', null, 'NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE', 'java', null, 'NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE', null, null, null, null, 'The return value from a method is dereferenced without a null check,and the return value of that method is one that should generally be checked for null. This may lead to a <code>NullPointerException</code> when the code is executed.', 'Possible null pointer dereference due to return value of called method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_NULL_ON_SOME_PATH_MIGHT_BE_INFEASIBLE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NP_NULL_ON_SOME_PATH_MIGHT_BE_INFEASIBLE', null, 'NP_NULL_ON_SOME_PATH_MIGHT_BE_INFEASIBLE', 'java', null, 'NP_NULL_ON_SOME_PATH_MIGHT_BE_INFEASIBLE', null, null, null, null, 'There is a branch of statement that, <em>if executed,</em> guarantees that a null value will be dereferenced, which would generate a <code>NullPointerException</code> when the code is executed.Of course, the problem might be that the branch or statement is infeasible and that the null pointer exception can''t ever be executed; deciding that is beyond the ability of SpotBugs.Due to the fact that this value had been previously tested for nullness,this is a definite possibility.', 'Possible null pointer dereference on branch that might be infeasible', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_ON_SOME_PATH_MIGHT_BE_INFEASIBLE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_NULL_ON_SOME_PATH_MIGHT_BE_INFEASIBLE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE', null, 'NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE', 'java', null, 'NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE', null, null, null, null, 'This parameter is always used in a way that requires it to be non-null,but the parameter is explicitly annotated as being Nullable. Either the use of the parameter or the annotation is wrong.', 'Parameter must be non-null but is marked as nullable', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD', null, 'NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD', 'java', null, 'NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD', null, null, null, null, 'The program is dereferencing a public or protected field that does not seem to ever have a non-null value written to it.Unless the field is initialized via some mechanism not seen by the analysis,dereferencing this value will generate a null pointer exception.', 'Read of unwritten public or protected field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NS_DANGEROUS_NON_SHORT_CIRCUIT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NS_DANGEROUS_NON_SHORT_CIRCUIT', null, 'NS_DANGEROUS_NON_SHORT_CIRCUIT', 'java', null, 'NS_DANGEROUS_NON_SHORT_CIRCUIT', null, null, null, null, 'This code seems to be using non-short-circuit logic (e.g., &amp;or |)rather than short-circuit logic (&amp;&amp; or ||). In addition,it seem possible that, depending on the value of the left hand side, you might not want to evaluate the right hand side (because it would have side effects, could cause an exception or could be expensive.Non-short-circuit logic causes both sides of the expression to be evaluated even when the result can be inferred from knowing the left-hand side. This can be less efficient and can result in errors if the left-hand side guards cases when evaluating the right-hand side can generate an error.See <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.22.2">the JavaLanguage Specification</a> for details.', 'Potentially dangerous use of non-short-circuit logic', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NS_DANGEROUS_NON_SHORT_CIRCUIT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NS_DANGEROUS_NON_SHORT_CIRCUIT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NS_NON_SHORT_CIRCUIT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'NS_NON_SHORT_CIRCUIT', null, 'NS_NON_SHORT_CIRCUIT', 'java', null, 'NS_NON_SHORT_CIRCUIT', null, null, null, null, 'This code seems to be using non-short-circuit logic (e.g., &amp;or |)rather than short-circuit logic (&amp;&amp; or ||).Non-short-circuit logic causes both sides of the expression to be evaluated even when the result can be inferred from knowing the left-hand side. This can be less efficient and can result in errors if the left-hand side guards cases when evaluating the right-hand side can generate an error.See <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.22.2">the JavaLanguage Specification</a> for details.', 'Questionable use of non-short-circuit logic', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NS_NON_SHORT_CIRCUIT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NS_NON_SHORT_CIRCUIT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- PS_PUBLIC_SEMAPHORES
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'PS_PUBLIC_SEMAPHORES', null, 'PS_PUBLIC_SEMAPHORES', 'java', null, 'PS_PUBLIC_SEMAPHORES', null, null, null, null, 'This class uses synchronization along with wait(), notify() or notifyAll() on itself (the this reference). Client classes that use this class, may, in addition, use an instance of this class as a synchronizing object. Because two classes are using the same object for synchronization, Multithread correctness is suspect. You should not synchronize nor call semaphore methods on a public reference. Consider using a internal private member variable to control synchronization.', 'Class exposes synchronization and semaphores in its public interface', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PS_PUBLIC_SEMAPHORES');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PS_PUBLIC_SEMAPHORES'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- PZLA_PREFER_ZERO_LENGTH_ARRAYS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'PZLA_PREFER_ZERO_LENGTH_ARRAYS', null, 'PZLA_PREFER_ZERO_LENGTH_ARRAYS', 'java', null, 'PZLA_PREFER_ZERO_LENGTH_ARRAYS', null, null, null, null, 'It is often a better design to return a length zero array rather than a null reference to indicate that there are no results (i.e., an empty list of results).This way, no explicit check for null is needed by clients of the method.On the other hand, using null to indicate"there is no answer to this question" is probably appropriate.For example, <code>File.listFiles()</code> returns an empty list if given a directory containing no files, and returns null if the file is not a directory.', 'Consider returning a zero length array rather than null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PZLA_PREFER_ZERO_LENGTH_ARRAYS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='PZLA_PREFER_ZERO_LENGTH_ARRAYS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- QF_QUESTIONABLE_FOR_LOOP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'QF_QUESTIONABLE_FOR_LOOP', null, 'QF_QUESTIONABLE_FOR_LOOP', 'java', null, 'QF_QUESTIONABLE_FOR_LOOP', null, null, null, null, 'Are you sure this for loop is incrementing the correct variable? It appears that another variable is being initialized and checked by the for loop.', 'Complicated, subtle or wrong increment in for-loop', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='QF_QUESTIONABLE_FOR_LOOP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='QF_QUESTIONABLE_FOR_LOOP'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RCN_REDUNDANT_COMPARISON_OF_NULL_AND_NONNULL_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RCN_REDUNDANT_COMPARISON_OF_NULL_AND_NONNULL_VALUE', null, 'RCN_REDUNDANT_COMPARISON_OF_NULL_AND_NONNULL_VALUE', 'java', null, 'RCN_REDUNDANT_COMPARISON_OF_NULL_AND_NONNULL_VALUE', null, null, null, null, 'This method contains a reference known to be non-null with another reference known to be null.', 'Redundant comparison of non-null value to null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RCN_REDUNDANT_COMPARISON_OF_NULL_AND_NONNULL_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RCN_REDUNDANT_COMPARISON_OF_NULL_AND_NONNULL_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RCN_REDUNDANT_COMPARISON_TWO_NULL_VALUES
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RCN_REDUNDANT_COMPARISON_TWO_NULL_VALUES', null, 'RCN_REDUNDANT_COMPARISON_TWO_NULL_VALUES', 'java', null, 'RCN_REDUNDANT_COMPARISON_TWO_NULL_VALUES', null, null, null, null, 'This method contains a redundant comparison of two references known to both be definitely null.', 'Redundant comparison of two null values', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RCN_REDUNDANT_COMPARISON_TWO_NULL_VALUES');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RCN_REDUNDANT_COMPARISON_TWO_NULL_VALUES'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE', null, 'RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE', 'java', null, 'RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE', null, null, null, null, 'This method contains a redundant check of a known non-null value against the constant null.', 'Redundant nullcheck of value known to be non-null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE', null, 'RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE', 'java', null, 'RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE', null, null, null, null, 'This method contains a redundant check of a known null value against the constant null.', 'Redundant nullcheck of value known to be null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- REC_CATCH_EXCEPTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'REC_CATCH_EXCEPTION', null, 'REC_CATCH_EXCEPTION', 'java', null, 'REC_CATCH_EXCEPTION', null, null, null, null, 'This method uses a try-catch block that catches Exception objects, but Exception is not thrown within the try block, and RuntimeException is not explicitly caught. It is a common bug pattern to say try { ... } catch (Exception e) { something } as a shorthand for catching a number of types of exception each of whose catch blocks is identical, but this construct also accidentally catches RuntimeException as well, masking potential bugs. A better approach is to either explicitly catch the specific exceptions that are thrown, or to explicitly catch RuntimeException exception, rethrow it, and then catch all non-Runtime Exceptions, as shown below:<pre><code>try { ...} catch (RuntimeException e) { throw e;} catch (Exception e) { ... deal with all non-runtime exceptions ...}</code></pre>', 'Exception is caught when Exception is not thrown', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='REC_CATCH_EXCEPTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='REC_CATCH_EXCEPTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='REC_CATCH_EXCEPTION'),
 'STANDARD', 'CWE','396')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RI_REDUNDANT_INTERFACES
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RI_REDUNDANT_INTERFACES', null, 'RI_REDUNDANT_INTERFACES', 'java', null, 'RI_REDUNDANT_INTERFACES', null, null, null, null, 'This class declares that it implements an interface that is also implemented by a superclass. This is redundant because once a superclass implements an interface, all subclasses by default also implement this interface. It may point out that the inheritance hierarchy has changed since this class was created, and consideration should be given to the ownership of the interface''s implementation.', 'Class implements same interface as superclass', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RI_REDUNDANT_INTERFACES');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RI_REDUNDANT_INTERFACES'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_CHECK_FOR_POSITIVE_INDEXOF
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RV_CHECK_FOR_POSITIVE_INDEXOF', null, 'RV_CHECK_FOR_POSITIVE_INDEXOF', 'java', null, 'RV_CHECK_FOR_POSITIVE_INDEXOF', null, null, null, null, 'The method invokes String.indexOf and checks to see if the result is positive or non-positive. It is much more typical to check to see if the result is negative or non-negative. It is positive only if the substring checked for occurs at some place other than at the beginning of the String.', 'Method checks to see if result of String.indexOf is positive', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_CHECK_FOR_POSITIVE_INDEXOF');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_CHECK_FOR_POSITIVE_INDEXOF'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_DONT_JUST_NULL_CHECK_READLINE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RV_DONT_JUST_NULL_CHECK_READLINE', null, 'RV_DONT_JUST_NULL_CHECK_READLINE', 'java', null, 'RV_DONT_JUST_NULL_CHECK_READLINE', null, null, null, null, 'The value returned by readLine is discarded after checking to see if the return value is non-null. In almost all situations, if the result is non-null, you will want to use that non-null value. Calling readLine again will give you a different line.', 'Method discards result of readLine after checking if it is non-null', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_DONT_JUST_NULL_CHECK_READLINE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_DONT_JUST_NULL_CHECK_READLINE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_REM_OF_HASHCODE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RV_REM_OF_HASHCODE', null, 'RV_REM_OF_HASHCODE', 'java', null, 'RV_REM_OF_HASHCODE', null, null, null, null, 'This code computes a hashCode, and then computes the remainder of that value modulo another value. Since the hashCode can be negative, the result of the remainder operation can also be negative. Assuming you want to ensure that the result of your computation is nonnegative,you may need to change your code.If you know the divisor is a power of 2,you can use a bitwise and operator instead (i.e., instead of using <code>x.hashCode()%n</code>, use <code>x.hashCode()&amp;(n-1)</code>).This is probably faster than computing the remainder as well.If you don''t know that the divisor is a power of 2, take the absolute value of the result of the remainder operation (i.e., use<code>Math.abs(x.hashCode()%n)</code>).', 'Remainder of hashCode could be negative', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_REM_OF_HASHCODE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_REM_OF_HASHCODE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_REM_OF_RANDOM_INT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RV_REM_OF_RANDOM_INT', null, 'RV_REM_OF_RANDOM_INT', 'java', null, 'RV_REM_OF_RANDOM_INT', null, null, null, null, 'This code generates a random signed integer and then computes the remainder of that value modulo another value. Since the random number can be negative, the result of the remainder operation can also be negative. Be sure this is intended, and strongly consider using the Random.nextInt(int) method instead.', 'Remainder of 32-bit signed random integer', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_REM_OF_RANDOM_INT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_REM_OF_RANDOM_INT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_RETURN_VALUE_IGNORED_INFERRED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RV_RETURN_VALUE_IGNORED_INFERRED', null, 'RV_RETURN_VALUE_IGNORED_INFERRED', 'java', null, 'RV_RETURN_VALUE_IGNORED_INFERRED', null, null, null, null, 'This code calls a method and ignores the return value. The return value is the same type as the type the method is invoked on, and from our analysis it looks like the return value might be important (e.g., like ignoring there turn value of <code>String.toLowerCase()</code>).We are guessing that ignoring the return value might be a bad idea just from a simple analysis of the body of the method. You can use a @CheckReturnValue annotation to instruct SpotBugs as to whether ignoring the return value of this method is important or acceptable.Please investigate this closely to decide whether it is OK to ignore the return value.', 'Method ignores return value, is this OK?', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_IGNORED_INFERRED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_IGNORED_INFERRED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT', null, 'RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT', 'java', null, 'RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT', null, null, null, null, 'This code calls a method and ignores the return value. However our analysis shows that the method (including its implementations in subclasses if any) does not produce any effect other than return value. Thus this call can be removed.We are trying to reduce the false positives as much as possible, but in some cases this warning might be wrong.Common false-positive cases include:- The method is designed to be overridden and produce a side effect in other projects which are out of the scope of the analysis.- The method is called to trigger the class loading which may have a side effect.- The method is called just to get some exception.If you feel that our assumption is incorrect, you can use a @CheckReturnValue annotation to instruct SpotBugs that ignoring the return value of this method is acceptable.', 'Return value of method without side effect is ignored', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SA_FIELD_DOUBLE_ASSIGNMENT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'SA_FIELD_DOUBLE_ASSIGNMENT', null, 'SA_FIELD_DOUBLE_ASSIGNMENT', 'java', null, 'SA_FIELD_DOUBLE_ASSIGNMENT', null, null, null, null, 'This method contains a double assignment of a field; e.g.<pre><code>int x,y;public void foo() { x = x = 17;}</code></pre>Assigning to a field twice is useless, and may indicate a logic error or typo.', 'Double assignment of field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_FIELD_DOUBLE_ASSIGNMENT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_FIELD_DOUBLE_ASSIGNMENT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SA_LOCAL_DOUBLE_ASSIGNMENT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'SA_LOCAL_DOUBLE_ASSIGNMENT', null, 'SA_LOCAL_DOUBLE_ASSIGNMENT', 'java', null, 'SA_LOCAL_DOUBLE_ASSIGNMENT', null, null, null, null, 'This method contains a double assignment of a local variable; e.g.<pre><code>public void foo() { int x,y; x = x = 17;}</code></pre>Assigning the same value to a variable twice is useless, and may indicate a logic error or typo.', 'Double assignment of local variable', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_LOCAL_DOUBLE_ASSIGNMENT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_LOCAL_DOUBLE_ASSIGNMENT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SA_LOCAL_SELF_ASSIGNMENT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'SA_LOCAL_SELF_ASSIGNMENT', null, 'SA_LOCAL_SELF_ASSIGNMENT', 'java', null, 'SA_LOCAL_SELF_ASSIGNMENT', null, null, null, null, 'This method contains a self assignment of a local variable; e.g.<pre><code>public void foo() { int x = 3; x = x;}</code></pre>Such assignments are useless, and may indicate a logic error or typo.', 'Self assignment of local variable', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_LOCAL_SELF_ASSIGNMENT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SA_LOCAL_SELF_ASSIGNMENT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_PRIVATE_READ_RESOLVE_NOT_INHERITED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'SE_PRIVATE_READ_RESOLVE_NOT_INHERITED', null, 'SE_PRIVATE_READ_RESOLVE_NOT_INHERITED', 'java', null, 'SE_PRIVATE_READ_RESOLVE_NOT_INHERITED', null, null, null, null, 'This class defines a private readResolve method. Since it is private, it won''t be inherited by subclasses.This might be intentional and OK, but should be reviewed to ensure it is what is intended.', 'Private readResolve method not inherited by subclasses', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_PRIVATE_READ_RESOLVE_NOT_INHERITED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_PRIVATE_READ_RESOLVE_NOT_INHERITED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SE_TRANSIENT_FIELD_OF_NONSERIALIZABLE_CLASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'SE_TRANSIENT_FIELD_OF_NONSERIALIZABLE_CLASS', null, 'SE_TRANSIENT_FIELD_OF_NONSERIALIZABLE_CLASS', 'java', null, 'SE_TRANSIENT_FIELD_OF_NONSERIALIZABLE_CLASS', null, null, null, null, 'The field is marked as transient, but the class isn''t Serializable, so marking it as transient has absolutely no effect.This may be leftover marking from a previous version of the code in which the class was transient, or it may indicate a misunderstanding of how serialization works.', 'Transient field of class that isn''t Serializable.', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_TRANSIENT_FIELD_OF_NONSERIALIZABLE_CLASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SE_TRANSIENT_FIELD_OF_NONSERIALIZABLE_CLASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SF_SWITCH_FALLTHROUGH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'SF_SWITCH_FALLTHROUGH', null, 'SF_SWITCH_FALLTHROUGH', 'java', null, 'SF_SWITCH_FALLTHROUGH', null, null, null, null, 'This method contains a switch statement where one case branch will fall through to the next case. Usually you need to end this case with a break or return.', 'Switch statement found where one case falls through to the next case', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_SWITCH_FALLTHROUGH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_SWITCH_FALLTHROUGH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_SWITCH_FALLTHROUGH'),
 'STANDARD', 'CWE','484')
ON CONFLICT DO NOTHING;



-- ------------------------
-- SF_SWITCH_NO_DEFAULT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'SF_SWITCH_NO_DEFAULT', null, 'SF_SWITCH_NO_DEFAULT', 'java', null, 'SF_SWITCH_NO_DEFAULT', null, null, null, null, 'This method contains a switch statement where default case is missing. Usually you need to provide a default case. Because the analysis only looks at the generated bytecode, this warning can be incorrect triggered if the default case is at the end of the switch statement and the switch statement doesn''t contain break statements for other cases.', 'Switch statement found where default case is missing', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_SWITCH_NO_DEFAULT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SF_SWITCH_NO_DEFAULT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD', null, 'ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD', 'java', null, 'ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD', null, null, null, null, 'This instance method writes to a static field. This is tricky to get correct if multiple instances are being manipulated,and generally bad practice.', 'Write to static field from instance method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_ALWAYS_SINK
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_ALWAYS_SINK', null, 'TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_ALWAYS_SINK', 'java', null, 'TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_ALWAYS_SINK', null, null, null, null, 'A value is used in a way that requires it to be always be a value denoted by a type qualifier, but there is an explicit annotation stating that it is not known where the value is required to have that type qualifier. Either the usage or the annotation is incorrect.', 'Value required to have type qualifier, but marked as unknown', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_ALWAYS_SINK');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_ALWAYS_SINK'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_NEVER_SINK
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_NEVER_SINK', null, 'TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_NEVER_SINK', 'java', null, 'TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_NEVER_SINK', null, null, null, null, 'A value is used in a way that requires it to be never be a value denoted by a type qualifier, but there is an explicit annotation stating that it is not known where the value is prohibited from having that type qualifier. Either the usage or the annotation is incorrect.', 'Value required to not have type qualifier, but marked as unknown', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_NEVER_SINK');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TQ_EXPLICIT_UNKNOWN_SOURCE_VALUE_REACHES_NEVER_SINK'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UCF_USELESS_CONTROL_FLOW
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'UCF_USELESS_CONTROL_FLOW', null, 'UCF_USELESS_CONTROL_FLOW', 'java', null, 'UCF_USELESS_CONTROL_FLOW', null, null, null, null, 'This method contains a useless control flow statement, where control flow continues onto the same place regardless of whether or not the branch is taken. For example,this is caused by having an empty statement block for an <code>if</code> statement:<pre><code>if (argv.length == 0) { // TODO: handle this case}</code></pre>', 'Useless control flow', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UCF_USELESS_CONTROL_FLOW');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UCF_USELESS_CONTROL_FLOW'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UCF_USELESS_CONTROL_FLOW_NEXT_LINE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'UCF_USELESS_CONTROL_FLOW_NEXT_LINE', null, 'UCF_USELESS_CONTROL_FLOW_NEXT_LINE', 'java', null, 'UCF_USELESS_CONTROL_FLOW_NEXT_LINE', null, null, null, null, 'This method contains a useless control flow statement in which control flow follows to the same or following line regardless of whether or not the branch is taken.Often, this is caused by inadvertently using an empty statement as the body of an <code>if</code> statement, e.g.:<pre><code>if (argv.length == 1); System.out.println("Hello, " + argv[0]);</code></pre>', 'Useless control flow to next line', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UCF_USELESS_CONTROL_FLOW_NEXT_LINE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UCF_USELESS_CONTROL_FLOW_NEXT_LINE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UC_USELESS_CONDITION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'UC_USELESS_CONDITION', null, 'UC_USELESS_CONDITION', 'java', null, 'UC_USELESS_CONDITION', null, null, null, null, 'This condition always produces the same result as the value of the involved variable that was narrowed before.Probably something else was meant or the condition can be removed.', 'Condition has no effect', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UC_USELESS_CONDITION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UC_USELESS_CONDITION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UC_USELESS_CONDITION_TYPE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'UC_USELESS_CONDITION_TYPE', null, 'UC_USELESS_CONDITION_TYPE', 'java', null, 'UC_USELESS_CONDITION_TYPE', null, null, null, null, 'This condition always produces the same result due to the type range of the involved variable.Probably something else was meant or the condition can be removed.', 'Condition has no effect due to the variable type', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UC_USELESS_CONDITION_TYPE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UC_USELESS_CONDITION_TYPE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UC_USELESS_OBJECT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'UC_USELESS_OBJECT', null, 'UC_USELESS_OBJECT', 'java', null, 'UC_USELESS_OBJECT', null, null, null, null, 'Our analysis shows that this object is useless.It''s created and modified, but its value never go outside of the method or produce any side-effect.Either there is a mistake and object was intended to be used or it can be removed.This analysis rarely produces false-positives. Common false-positive cases include:- This object used to implicitly throw some obscure exception.- This object used as a stub to generalize the code.- This object used to hold strong references to weak/soft-referenced objects.', 'Useless object created', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UC_USELESS_OBJECT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UC_USELESS_OBJECT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UC_USELESS_OBJECT_STACK
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'UC_USELESS_OBJECT_STACK', null, 'UC_USELESS_OBJECT_STACK', 'java', null, 'UC_USELESS_OBJECT_STACK', null, null, null, null, 'This object is created just to perform some modifications which don''t have any side-effect.Probably something else was meant or the object can be removed.', 'Useless object created on stack', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UC_USELESS_OBJECT_STACK');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UC_USELESS_OBJECT_STACK'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UC_USELESS_VOID_METHOD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'UC_USELESS_VOID_METHOD', null, 'UC_USELESS_VOID_METHOD', 'java', null, 'UC_USELESS_VOID_METHOD', null, null, null, null, 'Our analysis shows that this non-empty void method does not actually perform any useful work.Please check it: probably there''s a mistake in its code or its body can be fully removed.We are trying to reduce the false positives as much as possible, but in some cases this warning might be wrong.Common false-positive cases include:<ul><li>The method is intended to trigger loading of some class which may have a side effect.</li><li>The method is intended to implicitly throw some obscure exception.</li></ul>', 'Useless non-empty void method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UC_USELESS_VOID_METHOD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UC_USELESS_VOID_METHOD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD', null, 'URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD', 'java', null, 'URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD', null, null, null, null, 'This field is never read.&nbsp;The field is public or protected, so perhaps it is intended to be used with classes not seen as part of the analysis. If not,consider removing it from the class.', 'Unread public/protected field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- USM_USELESS_ABSTRACT_METHOD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'USM_USELESS_ABSTRACT_METHOD', null, 'USM_USELESS_ABSTRACT_METHOD', 'java', null, 'USM_USELESS_ABSTRACT_METHOD', null, null, null, null, 'This abstract method is already defined in an interface that is implemented by this abstract class. This method can be removed, as it provides no additional value.', 'Abstract Method is already defined in implemented interface', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='USM_USELESS_ABSTRACT_METHOD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='USM_USELESS_ABSTRACT_METHOD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- USM_USELESS_SUBCLASS_METHOD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'USM_USELESS_SUBCLASS_METHOD', null, 'USM_USELESS_SUBCLASS_METHOD', 'java', null, 'USM_USELESS_SUBCLASS_METHOD', null, null, null, null, 'This derived method merely calls the same superclass method passing in the exact parameters received. This method can be removed, as it provides no additional value.', 'Method superfluously delegates to parent class method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='USM_USELESS_SUBCLASS_METHOD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='USM_USELESS_SUBCLASS_METHOD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD', null, 'UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD', 'java', null, 'UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD', null, null, null, null, 'This field is never used.&nbsp;The field is public or protected, so perhaps it is intended to be used with classes not seen as part of the analysis. If not,consider removing it from the class.', 'Unused public or protected field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR', null, 'UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR', 'java', null, 'UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR', null, null, null, null, 'This field is never initialized within any constructor, and is therefore could be null after the object is constructed. Elsewhere, it is loaded and dereferenced without a null check.This could be a either an error or a questionable design, since it means a null pointer exception will be generated if that field is dereferenced before being initialized.', 'Field not initialized in constructor but dereferenced without null check', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD', null, 'UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD', 'java', null, 'UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD', null, null, null, null, 'No writes were seen to this public/protected field.&nbsp; All reads of it will return the default value. Check for errors (should it have been initialized?), or remove it if it is useless.', 'Unwritten public or protected field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- XFB_XML_FACTORY_BYPASS
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'STYLE', 'XFB_XML_FACTORY_BYPASS', null, 'XFB_XML_FACTORY_BYPASS', 'java', null, 'XFB_XML_FACTORY_BYPASS', null, null, null, null, 'This method allocates a specific implementation of an xml interface. It is preferable to use the supplied factory classes to create these objects so that the implementation can be changed at runtime. See <ul> <li>javax.xml.parsers.DocumentBuilderFactory</li> <li>javax.xml.parsers.SAXParserFactory</li> <li>javax.xml.transform.TransformerFactory</li> <li>org.w3c.dom.Document.create<i>XXXX</i></li> </ul> for details.', 'Method directly allocates a specific implementation of xml interfaces', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='XFB_XML_FACTORY_BYPASS');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='XFB_XML_FACTORY_BYPASS'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


