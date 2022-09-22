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
-- AT_OPERATION_SEQUENCE_ON_CONCURRENT_ABSTRACTION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'AT_OPERATION_SEQUENCE_ON_CONCURRENT_ABSTRACTION', null, 'AT_OPERATION_SEQUENCE_ON_CONCURRENT_ABSTRACTION', 'java', null, 'AT_OPERATION_SEQUENCE_ON_CONCURRENT_ABSTRACTION', null, null, null, null, 'This code contains a sequence of calls to a concurrent abstraction (such as a concurrent hash map). These calls will not be executed atomically.', 'Sequence of calls to concurrent abstraction may not be atomic', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='AT_OPERATION_SEQUENCE_ON_CONCURRENT_ABSTRACTION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='AT_OPERATION_SEQUENCE_ON_CONCURRENT_ABSTRACTION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DC_DOUBLECHECK
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'DC_DOUBLECHECK', null, 'DC_DOUBLECHECK', 'java', null, 'DC_DOUBLECHECK', null, null, null, null, 'This method may contain an instance of double-checked locking.&nbsp; This idiom is not correct according to the semantics of the Java memory model.&nbsp; For more information, see the web page <a href="http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html" >http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html</a>.', 'Possible double check of field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DC_DOUBLECHECK');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DC_DOUBLECHECK'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DC_DOUBLECHECK'),
 'STANDARD', 'CWE','609')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DC_PARTIALLY_CONSTRUCTED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'DC_PARTIALLY_CONSTRUCTED', null, 'DC_PARTIALLY_CONSTRUCTED', 'java', null, 'DC_PARTIALLY_CONSTRUCTED', null, null, null, null, 'Looks like this method uses lazy field initialization with double-checked locking. While the field is correctly declared as volatile, it''s possible that the internal structure of the object is changed after the field assignment, thus another thread may see the partially initialized object. To fix this problem consider storing the object into the local variable first and save it to the volatile field only after it''s fully constructed.', 'Possible exposure of partially initialized object', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DC_PARTIALLY_CONSTRUCTED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DC_PARTIALLY_CONSTRUCTED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DC_PARTIALLY_CONSTRUCTED'),
 'STANDARD', 'CWE','609')
ON CONFLICT DO NOTHING;



-- ------------------------
-- DL_SYNCHRONIZATION_ON_BOOLEAN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'DL_SYNCHRONIZATION_ON_BOOLEAN', null, 'DL_SYNCHRONIZATION_ON_BOOLEAN', 'java', null, 'DL_SYNCHRONIZATION_ON_BOOLEAN', null, null, null, null, 'The code synchronizes on a boxed primitive constant, such as a Boolean.<pre><code>private static Boolean inited = Boolean.FALSE;...synchronized(inited) { if (!inited) { init(); inited = Boolean.TRUE; }}...</code></pre>Since there normally exist only two Boolean objects, this code could be synchronizing on the same object as other, unrelated code, leading to unresponsiveness and possible deadlock.See CERT <a href="https://www.securecoding.cert.org/confluence/display/java/CON08-J.+Do+not+synchronize+on+objects+that+may+be+reused">CON08-J. Do not synchronize on objects that may be reused</a> for more information.', 'Synchronization on Boolean', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DL_SYNCHRONIZATION_ON_BOOLEAN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DL_SYNCHRONIZATION_ON_BOOLEAN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE', null, 'DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE', 'java', null, 'DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE', null, null, null, null, 'The code synchronizes on a boxed primitive constant, such as an Integer.<pre><code>private static Integer count = 0;...synchronized(count) { count++;}...</code></pre>Since Integer objects can be cached and shared,this code could be synchronizing on the same object as other, unrelated code, leading to unresponsiveness and possible deadlock.See CERT <a href="https://www.securecoding.cert.org/confluence/display/java/CON08-J.+Do+not+synchronize+on+objects+that+may+be+reused">CON08-J. Do not synchronize on objects that may be reused</a> for more information.', 'Synchronization on boxed primitive', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DL_SYNCHRONIZATION_ON_SHARED_CONSTANT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'DL_SYNCHRONIZATION_ON_SHARED_CONSTANT', null, 'DL_SYNCHRONIZATION_ON_SHARED_CONSTANT', 'java', null, 'DL_SYNCHRONIZATION_ON_SHARED_CONSTANT', null, null, null, null, 'The code synchronizes on interned String.<pre><code>private static String LOCK = "LOCK";...synchronized(LOCK) { ...}...</code></pre>Constant Strings are interned and shared across all other classes loaded by the JVM. Thus, this code is locking on something that other code might also be locking. This could result in very strange and hard to diagnose blocking and deadlock behavior. See <a href="http://www.javalobby.org/java/forums/t96352.html">http://www.javalobby.org/java/forums/t96352.html</a> and <a href="http://jira.codehaus.org/browse/JETTY-352">http://jira.codehaus.org/browse/JETTY-352</a>.See CERT <a href="https://www.securecoding.cert.org/confluence/display/java/CON08-J.+Do+not+synchronize+on+objects+that+may+be+reused">CON08-J. Do not synchronize on objects that may be reused</a> for more information.', 'Synchronization on interned String', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DL_SYNCHRONIZATION_ON_SHARED_CONSTANT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DL_SYNCHRONIZATION_ON_SHARED_CONSTANT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DL_SYNCHRONIZATION_ON_UNSHARED_BOXED_PRIMITIVE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'DL_SYNCHRONIZATION_ON_UNSHARED_BOXED_PRIMITIVE', null, 'DL_SYNCHRONIZATION_ON_UNSHARED_BOXED_PRIMITIVE', 'java', null, 'DL_SYNCHRONIZATION_ON_UNSHARED_BOXED_PRIMITIVE', null, null, null, null, 'The code synchronizes on an apparently unshared boxed primitive,such as an Integer.<pre><code>private static final Integer fileLock = new Integer(1);...synchronized(fileLock) { .. do something ..}...</code></pre>It would be much better, in this code, to redeclare fileLock as<pre><code>private static final Object fileLock = new Object();</code></pre>The existing code might be OK, but it is confusing and a future refactoring, such as the "Remove Boxing" refactoring in IntelliJ,might replace this with the use of an interned Integer object shared throughout the JVM, leading to very confusing behavior and potential deadlock.', 'Synchronization on boxed primitive values', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DL_SYNCHRONIZATION_ON_UNSHARED_BOXED_PRIMITIVE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DL_SYNCHRONIZATION_ON_UNSHARED_BOXED_PRIMITIVE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_MONITOR_WAIT_ON_CONDITION
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'DM_MONITOR_WAIT_ON_CONDITION', null, 'DM_MONITOR_WAIT_ON_CONDITION', 'java', null, 'DM_MONITOR_WAIT_ON_CONDITION', null, null, null, null, 'This method calls <code>wait()</code> on a <code>java.util.concurrent.locks.Condition</code> object.&nbsp; Waiting for a <code>Condition</code> should be done using one of the <code>await()</code> methods defined by the <code>Condition</code> interface.', 'Monitor wait() called on Condition', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_MONITOR_WAIT_ON_CONDITION');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_MONITOR_WAIT_ON_CONDITION'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- DM_USELESS_THREAD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'DM_USELESS_THREAD', null, 'DM_USELESS_THREAD', 'java', null, 'DM_USELESS_THREAD', null, null, null, null, 'This method creates a thread without specifying a run method either by deriving from the Thread class, or by passing a Runnable object. This thread, then, does nothing but waste time.', 'A thread was created using the default empty run method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_USELESS_THREAD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='DM_USELESS_THREAD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ESync_EMPTY_SYNC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'ESync_EMPTY_SYNC', null, 'ESync_EMPTY_SYNC', 'java', null, 'ESync_EMPTY_SYNC', null, null, null, null, 'The code contains an empty synchronized block:<pre><code>synchronized() {}</code></pre>Empty synchronized blocks are far more subtle and hard to use correctly than most people recognize, and empty synchronized blocks are almost never a better solution than less contrived solutions.', 'Empty synchronized block', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ESync_EMPTY_SYNC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ESync_EMPTY_SYNC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ESync_EMPTY_SYNC'),
 'STANDARD', 'CWE','585')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IS2_INCONSISTENT_SYNC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'IS2_INCONSISTENT_SYNC', null, 'IS2_INCONSISTENT_SYNC', 'java', null, 'IS2_INCONSISTENT_SYNC', null, null, null, null, 'The fields of this class appear to be accessed inconsistently with respect to synchronization.&nbsp; This bug report indicates that the bug pattern detector judged that <ul> <li> The class contains a mix of locked and unlocked accesses,</li> <li> The class is <b>not</b> annotated as javax.annotation.concurrent.NotThreadSafe,</li> <li> At least one locked access was performed by one of the class''s own methods, and</li> <li> The number of unsynchronized field accesses (reads and writes) was no more than one third of all accesses, with writes being weighed twice as high as reads</li> </ul> A typical bug matching this bug pattern is forgetting to synchronize one of the methods in a class that is intended to be thread-safe. You can select the nodes labeled "Unsynchronized access" to show the code locations where the detector believed that a field was accessed without synchronization. Note that there are various sources of inaccuracy in this detector; for example, the detector cannot statically detect all situations in which a lock is held.&nbsp; Also, even when the detector is accurate in distinguishing locked vs. unlocked accesses, the code in question may still be correct.', 'Inconsistent synchronization', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IS2_INCONSISTENT_SYNC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IS2_INCONSISTENT_SYNC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IS_FIELD_NOT_GUARDED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'IS_FIELD_NOT_GUARDED', null, 'IS_FIELD_NOT_GUARDED', 'java', null, 'IS_FIELD_NOT_GUARDED', null, null, null, null, 'This field is annotated with net.jcip.annotations.GuardedBy or javax.annotation.concurrent.GuardedBy,but can be accessed in a way that seems to violate those annotations.', 'Field not guarded against concurrent access', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IS_FIELD_NOT_GUARDED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IS_FIELD_NOT_GUARDED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- IS_INCONSISTENT_SYNC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'IS_INCONSISTENT_SYNC', null, 'IS_INCONSISTENT_SYNC', 'java', null, 'IS_INCONSISTENT_SYNC', null, null, null, null, 'The fields of this class appear to be accessed inconsistently with respect to synchronization.&nbsp; This bug report indicates that the bug pattern detector judged that <ul> <li> The class contains a mix of locked and unlocked accesses,</li> <li> At least one locked access was performed by one of the class''s own methods, and</li> <li> The number of unsynchronized field accesses (reads and writes) was no more than one third of all accesses, with writes being weighed twice as high as reads</li> </ul> A typical bug matching this bug pattern is forgetting to synchronize one of the methods in a class that is intended to be thread-safe. Note that there are various sources of inaccuracy in this detector; for example, the detector cannot statically detect all situations in which a lock is held.&nbsp; Also, even when the detector is accurate in distinguishing locked vs. unlocked accesses, the code in question may still be correct.', 'Inconsistent synchronization', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IS_INCONSISTENT_SYNC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='IS_INCONSISTENT_SYNC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- JLM_JSR166_LOCK_MONITORENTER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'JLM_JSR166_LOCK_MONITORENTER', null, 'JLM_JSR166_LOCK_MONITORENTER', 'java', null, 'JLM_JSR166_LOCK_MONITORENTER', null, null, null, null, 'This method performs synchronization on an object that implements java.util.concurrent.locks.Lock. Such an object is locked/unlocked using<code>acquire()</code>/<code>release()</code> rather than using the <code>synchronized (...)</code> construct.', 'Synchronization performed on Lock', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='JLM_JSR166_LOCK_MONITORENTER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='JLM_JSR166_LOCK_MONITORENTER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- JLM_JSR166_UTILCONCURRENT_MONITORENTER
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'JLM_JSR166_UTILCONCURRENT_MONITORENTER', null, 'JLM_JSR166_UTILCONCURRENT_MONITORENTER', 'java', null, 'JLM_JSR166_UTILCONCURRENT_MONITORENTER', null, null, null, null, 'This method performs synchronization on an object that is an instance ofa class from the java.util.concurrent package (or its subclasses). Instances of these classes have their own concurrency control mechanisms that are orthogonal to the synchronization provided by the Java keyword <code>synchronized</code>. For example,synchronizing on an <code>AtomicBoolean</code> will not prevent other threads from modifying the <code>AtomicBoolean</code>.Such code may be correct, but should be carefully reviewed and documented,and may confuse people who have to maintain the code at a later date.', 'Synchronization performed on util.concurrent instance', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='JLM_JSR166_UTILCONCURRENT_MONITORENTER');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='JLM_JSR166_UTILCONCURRENT_MONITORENTER'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- JML_JSR166_CALLING_WAIT_RATHER_THAN_AWAIT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'JML_JSR166_CALLING_WAIT_RATHER_THAN_AWAIT', null, 'JML_JSR166_CALLING_WAIT_RATHER_THAN_AWAIT', 'java', null, 'JML_JSR166_CALLING_WAIT_RATHER_THAN_AWAIT', null, null, null, null, 'This method calls<code>wait()</code>,<code>notify()</code> or<code>notifyAll()()</code>on an object that also provides an<code>await()</code>,<code>signal()</code>,<code>signalAll()</code> method (such as util.concurrent Condition objects).This probably isn''t what you want, and even if you do want it, you should consider changing your design, as other developers will find it exceptionally confusing.', 'Using monitor style wait methods on util.concurrent abstraction', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='JML_JSR166_CALLING_WAIT_RATHER_THAN_AWAIT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='JML_JSR166_CALLING_WAIT_RATHER_THAN_AWAIT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- LI_LAZY_INIT_INSTANCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'LI_LAZY_INIT_INSTANCE', null, 'LI_LAZY_INIT_INSTANCE', 'java', null, 'LI_LAZY_INIT_INSTANCE', null, null, null, null, 'This method contains an unsynchronized lazy initialization of a non-volatile field.Because the compiler or processor may reorder instructions,threads are not guaranteed to see a completely initialized object,<em>if the method can be called by multiple threads</em>.You can make the field volatile to correct the problem.For more information, see the<a href="http://www.cs.umd.edu/~pugh/java/memoryModel/">Java Memory Model web site</a>.', 'Incorrect lazy initialization of instance field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='LI_LAZY_INIT_INSTANCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='LI_LAZY_INIT_INSTANCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- LI_LAZY_INIT_STATIC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'LI_LAZY_INIT_STATIC', null, 'LI_LAZY_INIT_STATIC', 'java', null, 'LI_LAZY_INIT_STATIC', null, null, null, null, 'This method contains an unsynchronized lazy initialization of a non-volatile static field.Because the compiler or processor may reorder instructions,threads are not guaranteed to see a completely initialized object,<em>if the method can be called by multiple threads</em>.You can make the field volatile to correct the problem.For more information, see the<a href="http://www.cs.umd.edu/~pugh/java/memoryModel/">Java Memory Model web site</a>.', 'Incorrect lazy initialization of static field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='LI_LAZY_INIT_STATIC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='LI_LAZY_INIT_STATIC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='LI_LAZY_INIT_STATIC'),
 'STANDARD', 'CWE','543')
ON CONFLICT DO NOTHING;


-- ------------------------
-- LI_LAZY_INIT_UPDATE_STATIC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'LI_LAZY_INIT_UPDATE_STATIC', null, 'LI_LAZY_INIT_UPDATE_STATIC', 'java', null, 'LI_LAZY_INIT_UPDATE_STATIC', null, null, null, null, 'This method contains an unsynchronized lazy initialization of a static field.After the field is set, the object stored into that location is further updated or accessed.The setting of the field is visible to other threads as soon as it is set. If the further accesses in the method that set the field serve to initialize the object, then you have a <em>very serious</em> multithreading bug, unless something else prevents any other thread from accessing the stored object until it is fully initialized.Even if you feel confident that the method is never called by multiple threads, it might be better to not set the static field until the value you are setting it to is fully populated/initialized.', 'Incorrect lazy initialization and update of static field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='LI_LAZY_INIT_UPDATE_STATIC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='LI_LAZY_INIT_UPDATE_STATIC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='LI_LAZY_INIT_UPDATE_STATIC'),
 'STANDARD', 'CWE','543')
ON CONFLICT DO NOTHING;



-- ------------------------
-- ML_SYNC_ON_FIELD_TO_GUARD_CHANGING_THAT_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'ML_SYNC_ON_FIELD_TO_GUARD_CHANGING_THAT_FIELD', null, 'ML_SYNC_ON_FIELD_TO_GUARD_CHANGING_THAT_FIELD', 'java', null, 'ML_SYNC_ON_FIELD_TO_GUARD_CHANGING_THAT_FIELD', null, null, null, null, 'This method synchronizes on a field in what appears to be an attempt to guard against simultaneous updates to that field. But guarding a field gets a lock on the referenced object, not on the field. This may not provide the mutual exclusion you need, and other threads might be obtaining locks on the referenced objects (for other purposes). An example of this pattern would be:<pre><code>private Long myNtfSeqNbrCounter = new Long(0);private Long getNotificationSequenceNumber() { Long result = null; synchronized(myNtfSeqNbrCounter) { result = new Long(myNtfSeqNbrCounter.longValue() + 1); myNtfSeqNbrCounter = new Long(result.longValue()); } return result;}</code></pre>', 'Synchronization on field in futile attempt to guard that field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ML_SYNC_ON_FIELD_TO_GUARD_CHANGING_THAT_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ML_SYNC_ON_FIELD_TO_GUARD_CHANGING_THAT_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- ML_SYNC_ON_UPDATED_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'ML_SYNC_ON_UPDATED_FIELD', null, 'ML_SYNC_ON_UPDATED_FIELD', 'java', null, 'ML_SYNC_ON_UPDATED_FIELD', null, null, null, null, 'This method synchronizes on an object referenced from a mutable field. This is unlikely to have useful semantics, since different threads may be synchronizing on different objects.', 'Method synchronizes on an updated field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ML_SYNC_ON_UPDATED_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='ML_SYNC_ON_UPDATED_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MSF_MUTABLE_SERVLET_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'MSF_MUTABLE_SERVLET_FIELD', null, 'MSF_MUTABLE_SERVLET_FIELD', 'java', null, 'MSF_MUTABLE_SERVLET_FIELD', null, null, null, null, 'A web server generally only creates one instance of servlet or JSP class (i.e., treats the class as a Singleton),and will have multiple threads invoke methods on that instance to service multiple simultaneous requests.Thus, having a mutable instance field generally creates race conditions.', 'Mutable servlet field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MSF_MUTABLE_SERVLET_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MSF_MUTABLE_SERVLET_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MWN_MISMATCHED_NOTIFY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'MWN_MISMATCHED_NOTIFY', null, 'MWN_MISMATCHED_NOTIFY', 'java', null, 'MWN_MISMATCHED_NOTIFY', null, null, null, null, 'This method calls Object.notify() or Object.notifyAll() without obviously holding a lock on the object.&nbsp; Calling notify() or notifyAll() without a lock held will result in an <code>IllegalMonitorStateException</code> being thrown.', 'Mismatched notify()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MWN_MISMATCHED_NOTIFY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MWN_MISMATCHED_NOTIFY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- MWN_MISMATCHED_WAIT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'MWN_MISMATCHED_WAIT', null, 'MWN_MISMATCHED_WAIT', 'java', null, 'MWN_MISMATCHED_WAIT', null, null, null, null, 'This method calls Object.wait() without obviously holding a lock on the object.&nbsp; Calling wait() without a lock held will result in an <code>IllegalMonitorStateException</code> being thrown.', 'Mismatched wait()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MWN_MISMATCHED_WAIT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='MWN_MISMATCHED_WAIT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NN_NAKED_NOTIFY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'NN_NAKED_NOTIFY', null, 'NN_NAKED_NOTIFY', 'java', null, 'NN_NAKED_NOTIFY', null, null, null, null, 'A call to <code>notify()</code> or <code>notifyAll()</code> was made without any (apparent) accompanying modification to mutable object state.&nbsp; In general, calling a notify method on a monitor is done because some condition another thread is waiting for has become true.&nbsp; However, for the condition to be meaningful, it must involve a heap object that is visible to both threads. This bug does not necessarily indicate an error, since the change to mutable object state may have taken place in a method which then called the method containing the notification.', 'Naked notify', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NN_NAKED_NOTIFY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NN_NAKED_NOTIFY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NO_NOTIFY_NOT_NOTIFYALL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'NO_NOTIFY_NOT_NOTIFYALL', null, 'NO_NOTIFY_NOT_NOTIFYALL', 'java', null, 'NO_NOTIFY_NOT_NOTIFYALL', null, null, null, null, 'This method calls <code>notify()</code> rather than <code>notifyAll()</code>.&nbsp; Java monitors are often used for multiple conditions.&nbsp; Calling <code>notify()</code> only wakes up one thread, meaning that the thread woken up might not be the one waiting for the condition that the caller just satisfied.', 'Using notify() rather than notifyAll()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NO_NOTIFY_NOT_NOTIFYALL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NO_NOTIFY_NOT_NOTIFYALL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- NP_SYNC_AND_NULL_CHECK_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'NP_SYNC_AND_NULL_CHECK_FIELD', null, 'NP_SYNC_AND_NULL_CHECK_FIELD', 'java', null, 'NP_SYNC_AND_NULL_CHECK_FIELD', null, null, null, null, 'Since the field is synchronized on, it seems not likely to be null.If it is null and then synchronized on a NullPointerException will be thrown and the check would be pointless. Better to synchronize on another field.', 'Synchronize and null check on the same field.', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_SYNC_AND_NULL_CHECK_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_SYNC_AND_NULL_CHECK_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='NP_SYNC_AND_NULL_CHECK_FIELD'),
 'STANDARD', 'CWE','585')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RS_READOBJECT_SYNC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'RS_READOBJECT_SYNC', null, 'RS_READOBJECT_SYNC', 'java', null, 'RS_READOBJECT_SYNC', null, null, null, null, 'This serializable class defines a <code>readObject()</code> which is synchronized.&nbsp; By definition, an object created by deserialization is only reachable by one thread, and thus there is no need for <code>readObject()</code> to be synchronized.&nbsp; If the <code>readObject()</code> method itself is causing the object to become visible to another thread, that is an example of very dubious coding style.', 'Class''s readObject() method is synchronized', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RS_READOBJECT_SYNC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RS_READOBJECT_SYNC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RU_INVOKE_RUN
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'RU_INVOKE_RUN', null, 'RU_INVOKE_RUN', 'java', null, 'RU_INVOKE_RUN', null, null, null, null, 'This method explicitly invokes <code>run()</code> on an object.&nbsp; In general, classes implement the <code>Runnable</code> interface because they are going to have their <code>run()</code> method invoked in a new thread, in which case <code>Thread.start()</code> is the right method to call.', 'Invokes run on a thread (did you mean to start it instead?)', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RU_INVOKE_RUN');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RU_INVOKE_RUN'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RU_INVOKE_RUN'),
 'STANDARD', 'CWE','572')
ON CONFLICT DO NOTHING;


-- ------------------------
-- RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED', null, 'RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED', 'java', null, 'RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED', null, null, null, null, 'The <code>putIfAbsent</code> method is typically used to ensure that a single value is associated with a given key (the first value for which put if absent succeeds). If you ignore the return value and retain a reference to the value passed in, you run the risk of retaining a value that is not the one that is associated with the key in the map. If it matters which one you use and you use the one that isn''t stored in the map, your program will behave incorrectly.', 'Return value of putIfAbsent ignored, value passed to putIfAbsent reused', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SC_START_IN_CTOR
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'SC_START_IN_CTOR', null, 'SC_START_IN_CTOR', 'java', null, 'SC_START_IN_CTOR', null, null, null, null, 'The constructor starts a thread. This is likely to be wrong if the class is ever extended/subclassed, since the thread will be started before the subclass constructor is started.', 'Constructor invokes Thread.start()', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SC_START_IN_CTOR');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SC_START_IN_CTOR'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SP_SPIN_ON_FIELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'SP_SPIN_ON_FIELD', null, 'SP_SPIN_ON_FIELD', 'java', null, 'SP_SPIN_ON_FIELD', null, null, null, null, 'This method spins in a loop which reads a field.&nbsp; The compiler may legally hoist the read out of the loop, turning the code into an infinite loop.&nbsp; The class should be changed so it uses proper synchronization (including wait and notify calls).', 'Method spins on field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SP_SPIN_ON_FIELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SP_SPIN_ON_FIELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- STCAL_INVOKE_ON_STATIC_CALENDAR_INSTANCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'STCAL_INVOKE_ON_STATIC_CALENDAR_INSTANCE', null, 'STCAL_INVOKE_ON_STATIC_CALENDAR_INSTANCE', 'java', null, 'STCAL_INVOKE_ON_STATIC_CALENDAR_INSTANCE', null, null, null, null, 'Even though the JavaDoc does not contain a hint about it, Calendars are inherently unsafe for multithreaded use.The detector has found a call to an instance of Calendar that has been obtained via a static field. This looks suspicious.For more information on this see <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6231579">JDK Bug #6231579</a>and <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6178997">JDK Bug #6178997</a>.', 'Call to static Calendar', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STCAL_INVOKE_ON_STATIC_CALENDAR_INSTANCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STCAL_INVOKE_ON_STATIC_CALENDAR_INSTANCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- STCAL_INVOKE_ON_STATIC_DATE_FORMAT_INSTANCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'STCAL_INVOKE_ON_STATIC_DATE_FORMAT_INSTANCE', null, 'STCAL_INVOKE_ON_STATIC_DATE_FORMAT_INSTANCE', 'java', null, 'STCAL_INVOKE_ON_STATIC_DATE_FORMAT_INSTANCE', null, null, null, null, 'As the JavaDoc states, DateFormats are inherently unsafe for multithreaded use.The detector has found a call to an instance of DateFormat that has been obtained via a static field. This looks suspicious.For more information on this see <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6231579">JDK Bug #6231579</a>and <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6178997">JDK Bug #6178997</a>.', 'Call to static DateFormat', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STCAL_INVOKE_ON_STATIC_DATE_FORMAT_INSTANCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STCAL_INVOKE_ON_STATIC_DATE_FORMAT_INSTANCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- STCAL_STATIC_CALENDAR_INSTANCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'STCAL_STATIC_CALENDAR_INSTANCE', null, 'STCAL_STATIC_CALENDAR_INSTANCE', 'java', null, 'STCAL_STATIC_CALENDAR_INSTANCE', null, null, null, null, 'Even though the JavaDoc does not contain a hint about it, Calendars are inherently unsafe for multithreaded use.Sharing a single instance across thread boundaries without proper synchronization will result in erratic behavior of the application. Under 1.4 problems seem to surface less often than under Java 5 where you will probably see random ArrayIndexOutOfBoundsExceptions or IndexOutOfBoundsExceptions in sun.util.calendar.BaseCalendar.getCalendarDateFromFixedDate().You may also experience serialization problems.Using an instance field is recommended.For more information on this see <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6231579">JDK Bug #6231579</a>and <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6178997">JDK Bug #6178997</a>.', 'Static Calendar field', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STCAL_STATIC_CALENDAR_INSTANCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STCAL_STATIC_CALENDAR_INSTANCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- STCAL_STATIC_SIMPLE_DATE_FORMAT_INSTANCE
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'STCAL_STATIC_SIMPLE_DATE_FORMAT_INSTANCE', null, 'STCAL_STATIC_SIMPLE_DATE_FORMAT_INSTANCE', 'java', null, 'STCAL_STATIC_SIMPLE_DATE_FORMAT_INSTANCE', null, null, null, null, 'As the JavaDoc states, DateFormats are inherently unsafe for multithreaded use.Sharing a single instance across thread boundaries without proper synchronization will result in erratic behavior of the application.You may also experience serialization problems.Using an instance field is recommended.For more information on this see <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6231579">JDK Bug #6231579</a>and <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6178997">JDK Bug #6178997</a>.', 'Static DateFormat', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STCAL_STATIC_SIMPLE_DATE_FORMAT_INSTANCE');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='STCAL_STATIC_SIMPLE_DATE_FORMAT_INSTANCE'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- SWL_SLEEP_WITH_LOCK_HELD
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'SWL_SLEEP_WITH_LOCK_HELD', null, 'SWL_SLEEP_WITH_LOCK_HELD', 'java', null, 'SWL_SLEEP_WITH_LOCK_HELD', null, null, null, null, 'This method calls Thread.sleep() with a lock held. This may result in very poor performance and scalability, or a deadlock, since other threads may be waiting to acquire the lock. It is a much better idea to call wait() on the lock, which releases the lock and allows other threads to run.', 'Method calls Thread.sleep() with a lock held', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SWL_SLEEP_WITH_LOCK_HELD');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='SWL_SLEEP_WITH_LOCK_HELD'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TLW_TWO_LOCK_NOTIFY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'TLW_TWO_LOCK_NOTIFY', null, 'TLW_TWO_LOCK_NOTIFY', 'java', null, 'TLW_TWO_LOCK_NOTIFY', null, null, null, null, 'The code calls notify() or notifyAll() while two locks are held. If this notification is intended to wake up a wait() that is holding the same locks, it may deadlock, since the wait will only give up one lock and the notify will be unable to get both locks, and thus the notify will not succeed. &nbsp; If there is also a warning about a two lock wait, the probably of a bug is quite high.', 'Notify with two locks held', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TLW_TWO_LOCK_NOTIFY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TLW_TWO_LOCK_NOTIFY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- TLW_TWO_LOCK_WAIT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'TLW_TWO_LOCK_WAIT', null, 'TLW_TWO_LOCK_WAIT', 'java', null, 'TLW_TWO_LOCK_WAIT', null, null, null, null, 'Waiting on a monitor while two locks are held may cause deadlock. &nbsp; Performing a wait only releases the lock on the object being waited on, not any other locks. &nbsp;This not necessarily a bug, but is worth examining closely.', 'Wait with two locks held', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TLW_TWO_LOCK_WAIT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='TLW_TWO_LOCK_WAIT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UG_SYNC_SET_UNSYNC_GET
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'UG_SYNC_SET_UNSYNC_GET', null, 'UG_SYNC_SET_UNSYNC_GET', 'java', null, 'UG_SYNC_SET_UNSYNC_GET', null, null, null, null, 'This class contains similarly-named get and set methods where the set method is synchronized and the get method is not.&nbsp; This may result in incorrect behavior at runtime, as callers of the get method will not necessarily see a consistent state for the object.&nbsp; The get method should be made synchronized.', 'Unsynchronized get method, synchronized set method', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UG_SYNC_SET_UNSYNC_GET');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UG_SYNC_SET_UNSYNC_GET'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UL_UNRELEASED_LOCK
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'UL_UNRELEASED_LOCK', null, 'UL_UNRELEASED_LOCK', 'java', null, 'UL_UNRELEASED_LOCK', null, null, null, null, 'This method acquires a JSR-166 (<code>java.util.concurrent</code>) lock,but does not release it on all paths out of the method. In general, the correct idiom for using a JSR-166 lock is:<pre><code>Lock l = ...;l.lock();try { // do something} finally { l.unlock();}</code></pre>', 'Method does not release lock on all paths', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UL_UNRELEASED_LOCK');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UL_UNRELEASED_LOCK'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UL_UNRELEASED_LOCK_EXCEPTION_PATH
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'UL_UNRELEASED_LOCK_EXCEPTION_PATH', null, 'UL_UNRELEASED_LOCK_EXCEPTION_PATH', 'java', null, 'UL_UNRELEASED_LOCK_EXCEPTION_PATH', null, null, null, null, 'This method acquires a JSR-166 (<code>java.util.concurrent</code>) lock,but does not release it on all exception paths out of the method. In general, the correct idiom for using a JSR-166 lock is:<pre><code>Lock l = ...;l.lock();try { // do something} finally { l.unlock();}</code></pre>', 'Method does not release lock on all exception paths', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UL_UNRELEASED_LOCK_EXCEPTION_PATH');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UL_UNRELEASED_LOCK_EXCEPTION_PATH'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- UW_UNCOND_WAIT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'UW_UNCOND_WAIT', null, 'UW_UNCOND_WAIT', 'java', null, 'UW_UNCOND_WAIT', null, null, null, null, 'This method contains a call to <code>java.lang.Object.wait()</code> which is not guarded by conditional control flow.&nbsp; The code should verify that condition it intends to wait for is not already satisfied before calling wait; any previous notifications will be ignored.', 'Unconditional wait', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UW_UNCOND_WAIT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='UW_UNCOND_WAIT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- VO_VOLATILE_INCREMENT
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'VO_VOLATILE_INCREMENT', null, 'VO_VOLATILE_INCREMENT', 'java', null, 'VO_VOLATILE_INCREMENT', null, null, null, null, 'This code increments a volatile field. Increments of volatile fields aren''tatomic. If more than one thread is incrementing the field at the same time,increments could be lost.', 'An increment to a volatile field isn''t atomic', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='VO_VOLATILE_INCREMENT');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='VO_VOLATILE_INCREMENT'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- VO_VOLATILE_REFERENCE_TO_ARRAY
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'VO_VOLATILE_REFERENCE_TO_ARRAY', null, 'VO_VOLATILE_REFERENCE_TO_ARRAY', 'java', null, 'VO_VOLATILE_REFERENCE_TO_ARRAY', null, null, null, null, 'This declares a volatile reference to an array, which might not be what you want. With a volatile reference to an array, reads and writes of the reference to the array are treated as volatile, but the array elements are non-volatile. To get volatile array elements, you will need to use one of the atomic array classes in java.util.concurrent (provided in Java 5.0).', 'A volatile reference to an array doesn''t treat the array elements as volatile', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='VO_VOLATILE_REFERENCE_TO_ARRAY');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='VO_VOLATILE_REFERENCE_TO_ARRAY'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- WA_AWAIT_NOT_IN_LOOP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'WA_AWAIT_NOT_IN_LOOP', null, 'WA_AWAIT_NOT_IN_LOOP', 'java', null, 'WA_AWAIT_NOT_IN_LOOP', null, null, null, null, 'This method contains a call to <code>java.util.concurrent.await()</code> (or variants) which is not in a loop.&nbsp; If the object is used for multiple conditions, the condition the caller intended to wait for might not be the one that actually occurred.', 'Condition.await() not in loop', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='WA_AWAIT_NOT_IN_LOOP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='WA_AWAIT_NOT_IN_LOOP'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- WA_NOT_IN_LOOP
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'WA_NOT_IN_LOOP', null, 'WA_NOT_IN_LOOP', 'java', null, 'WA_NOT_IN_LOOP', null, null, null, null, 'This method contains a call to <code>java.lang.Object.wait()</code> which is not in a loop.&nbsp; If the monitor is used for multiple conditions, the condition the caller intended to wait for might not be the one that actually occurred.', 'Wait not in loop', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='WA_NOT_IN_LOOP');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='WA_NOT_IN_LOOP'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- WL_USING_GETCLASS_RATHER_THAN_CLASS_LITERAL
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'WL_USING_GETCLASS_RATHER_THAN_CLASS_LITERAL', null, 'WL_USING_GETCLASS_RATHER_THAN_CLASS_LITERAL', 'java', null, 'WL_USING_GETCLASS_RATHER_THAN_CLASS_LITERAL', null, null, null, null, 'This instance method synchronizes on <code>this.getClass()</code>. If this class is subclassed, subclasses will synchronize on the class object for the subclass, which isn''t likely what was intended. For example, consider this code from java.awt.Label:<pre><code>private static final String base = "label";private static int nameCounter = 0;String constructComponentName() { synchronized (getClass()) { return base + nameCounter++; }}</code></pre> Subclasses of <code>Label</code> won''t synchronize on the same subclass, giving rise to a data race. Instead, this code should be synchronizing on <code>Label.class</code><pre><code>private static final String base = "label";private static int nameCounter = 0;String constructComponentName() { synchronized (Label.class) { return base + nameCounter++; }}</code></pre> Bug pattern contributed by Jason Mehrens', 'Synchronization on getClass rather than class literal', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='WL_USING_GETCLASS_RATHER_THAN_CLASS_LITERAL');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='WL_USING_GETCLASS_RATHER_THAN_CLASS_LITERAL'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


-- ------------------------
-- WS_WRITEOBJECT_SYNC
-- ------------------------
insert into xcalibyte.rule_information
(rule_set_id, category, vulnerable, certainty, rule_code, language, url, name, severity, priority, likelihood, remediation_cost, detail, description, msg_template, created_by, modified_by)
values
((select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'SpotBugs' and "rule_set".name = 'builtin' and "rule_set".version ='4.0.0'), 'MT_CORRECTNESS', 'WS_WRITEOBJECT_SYNC', null, 'WS_WRITEOBJECT_SYNC', 'java', null, 'WS_WRITEOBJECT_SYNC', null, null, null, null, 'This class has a <code>writeObject()</code> method which is synchronized; however, no other method of the class is synchronized.', 'Class''s writeObject() method is synchronized but nothing else is', null, 'system', 'system')
ON CONFLICT DO NOTHING;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='WS_WRITEOBJECT_SYNC');

insert into xcalibyte.rule_information_attribute
(rule_information_id, type, name, value)
values
((select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
  where se.name = 'SpotBugs' and rs.name = 'builtin' and rs.version ='4.0.0' and ri.rule_code ='WS_WRITEOBJECT_SYNC'),
 'BASIC','LANG','java')
ON CONFLICT DO NOTHING;


