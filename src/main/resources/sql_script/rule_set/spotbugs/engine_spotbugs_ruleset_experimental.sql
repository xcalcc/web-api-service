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
-- LG_LOST_LOGGER_DUE_TO_WEAK_REFERENCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'EXPERIMENTAL', 'LG_LOST_LOGGER_DUE_TO_WEAK_REFERENCE', null, 'LG_LOST_LOGGER_DUE_TO_WEAK_REFERENCE', 'java', null, 'LG_LOST_LOGGER_DUE_TO_WEAK_REFERENCE', null, null, null, null, 'OpenJDK introduces a potential incompatibility. In particular, the java.util.logging.Logger behavior has changed. Instead of using strong references, it now uses weak references internally. That''s a reasonable change, but unfortunately some code relies on the old behavior - when changing logger configuration, it simply drops the logger reference. That means that the garbage collector is free to reclaim that memory, which means that the logger configuration is lost. For example,consider:<pre><code>public static void initLogging() throws Exception { Logger logger = Logger.getLogger("edu.umd.cs"); logger.addHandler(new FileHandler()); // call to change logger configuration logger.setUseParentHandlers(false); // another call to change logger configuration}</code></pre>The logger reference is lost at the end of the method (it doesn''tescape the method), so if you have a garbage collection cycle just after the call to initLogging, the logger configuration is lost(because Logger only keeps weak references).<pre><code>public static void main(String[] args) throws Exception { initLogging(); // adds a file handler to the logger System.gc(); // logger configuration lost Logger.getLogger("edu.umd.cs").info("Some message"); // this isn''t logged to the file as expected}</code></pre><em>Ulf Ochsenfahrt and Eric Fellheimer</em>', 'Potential lost logger changes due to weak reference in OpenJDK', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='LG_LOST_LOGGER_DUE_TO_WEAK_REFERENCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='LG_LOST_LOGGER_DUE_TO_WEAK_REFERENCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- OBL_UNSATISFIED_OBLIGATION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'EXPERIMENTAL', 'OBL_UNSATISFIED_OBLIGATION', null, 'OBL_UNSATISFIED_OBLIGATION', 'java', null, 'OBL_UNSATISFIED_OBLIGATION', null, null, null, null, 'This method may fail to clean up (close, dispose of) a stream, database object, or other resource requiring an explicit cleanup operation. In general, if a method opens a stream or other resource, the method should use a try/finally block to ensure that the stream or resource is cleaned up before the method returns. This bug pattern is essentially the same as the OS_OPEN_STREAM and ODR_OPEN_DATABASE_RESOURCE bug patterns, but is based on a different (and hopefully better) static analysis technique. We are interested is getting feedback about the usefulness of this bug pattern. For sending feedback, check: <ul> <li><a href="https://github.com/spotbugs/spotbugs/blob/master/CONTRIBUTING.md">contributing guideline</a></li> <li><a href="https://github.com/spotbugs/discuss/issues?q=">malinglist</a></li> </ul> In particular, the false-positive suppression heuristics for this bug pattern have not been extensively tuned, so reports about false positives are helpful to us. See Weimer and Necula, <i>Finding and Preventing Run-Time Error Handling Mistakes</i>, for a description of the analysis technique.', 'Method may fail to clean up stream or resource', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='OBL_UNSATISFIED_OBLIGATION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='OBL_UNSATISFIED_OBLIGATION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'EXPERIMENTAL', 'OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE', null, 'OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE', 'java', null, 'OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE', null, null, null, null, 'This method may fail to clean up (close, dispose of) a stream, database object, or other resource requiring an explicit cleanup operation. In general, if a method opens a stream or other resource, the method should use a try/finally block to ensure that the stream or resource is cleaned up before the method returns. This bug pattern is essentially the same as the OS_OPEN_STREAM and ODR_OPEN_DATABASE_RESOURCE bug patterns, but is based on a different (and hopefully better) static analysis technique. We are interested is getting feedback about the usefulness of this bug pattern. For sending feedback, check: <ul> <li><a href="https://github.com/spotbugs/spotbugs/blob/master/CONTRIBUTING.md">contributing guideline</a></li> <li><a href="https://github.com/spotbugs/discuss/issues?q=">malinglist</a></li> </ul> In particular, the false-positive suppression heuristics for this bug pattern have not been extensively tuned, so reports about false positives are helpful to us. See Weimer and Necula, <i>Finding and Preventing Run-Time Error Handling Mistakes</i>, for a description of the analysis technique.', 'Method may fail to clean up stream or resource on checked exception', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SKIPPED_CLASS_TOO_BIG
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'EXPERIMENTAL', 'SKIPPED_CLASS_TOO_BIG', null, 'SKIPPED_CLASS_TOO_BIG', 'java', null, 'SKIPPED_CLASS_TOO_BIG', null, null, null, null, 'This class is bigger than can be effectively handled, and was not fully analyzed for errors.', 'Class too big for analysis', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SKIPPED_CLASS_TOO_BIG');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SKIPPED_CLASS_TOO_BIG'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TESTING
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'EXPERIMENTAL', 'TESTING', null, 'TESTING', 'java', null, 'TESTING', null, null, null, null, 'This bug pattern is only generated by new, incompletely implemented bug detectors.', 'Testing', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TESTING');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TESTING'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TESTING1
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'EXPERIMENTAL', 'TESTING1', null, 'TESTING1', 'java', null, 'TESTING1', null, null, null, null, 'This bug pattern is only generated by new, incompletely implemented bug detectors.', 'Testing 1', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TESTING1');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TESTING1'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TESTING2
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'EXPERIMENTAL', 'TESTING2', null, 'TESTING2', 'java', null, 'TESTING2', null, null, null, null, 'This bug pattern is only generated by new, incompletely implemented bug detectors.', 'Testing 2', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TESTING2');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TESTING2'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TESTING3
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'EXPERIMENTAL', 'TESTING3', null, 'TESTING3', 'java', null, 'TESTING3', null, null, null, null, 'This bug pattern is only generated by new, incompletely implemented bug detectors.', 'Testing 3', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TESTING3');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TESTING3'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UNKNOWN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'EXPERIMENTAL', 'UNKNOWN', null, 'UNKNOWN', 'java', null, 'UNKNOWN', null, null, null, null, 'A warning was recorded, but SpotBugs can''t find the description of this bug pattern and so can''t describe it. This should occur only in cases of a bug in SpotBugs or its configuration,or perhaps if an analysis was generated using a plugin, but that plugin is not currently loaded..', 'Unknown bug pattern', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UNKNOWN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UNKNOWN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


