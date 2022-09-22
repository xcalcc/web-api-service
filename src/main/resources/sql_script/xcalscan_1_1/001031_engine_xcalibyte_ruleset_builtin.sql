/*
   Copyright (C) 2019-2022 Xcalibyte (Shenzhen) Limited.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

-- ------------------------
-- UPDATE RSX to RXS
-- ------------------------
update xcalibyte.rule_information set
vulnerable='RXS',
rule_code = 'RXS',
name = '${rule.Xcalibyte.BUILTIN.1.RXS.name}',
detail = '${rule.Xcalibyte.BUILTIN.1.RXS.detail}',
description = '${rule.Xcalibyte.BUILTIN.1.RXS.description}',
msg_template = '${rule.Xcalibyte.BUILTIN.1.RXS.msg_template}',
modified_by = 'system',
modified_on = CURRENT_TIMESTAMP
where id = (select id from xcalibyte."rule_information" where rule_code = 'RSX')
  and rule_set_id = (select rule_set.id from xcalibyte."rule_set" left join xcalibyte."scan_engine" on xcalibyte.scan_engine.id = xcalibyte.rule_set.scan_engine_id where "scan_engine".name = 'Xcalibyte' and "rule_set".name = 'BUILTIN' and "rule_set".version ='1');

delete from xcalibyte.i18n_message where xcalibyte.xcalibyte.i18n_message.message_key like 'rule.Xcalibyte.BUILTIN.1.RSX.%';

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'rule.Xcalibyte.BUILTIN.1.RXS.description', 'The program has read from external sockets which may include untrusted data.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RXS.description', '该程序已从可能包括不受信任数据的外部套接字中读取', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RXS.detail', '### RXS'||chr(10)||'#### Abstract'||chr(10)||'The program has read from external sockets which may include untrusted data.'||chr(10)||''||chr(10)||'#### Explanation'||chr(10)||'Because external sockets'||chr(10)||''||chr(10)||''||chr(10)||'#### Example'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <sys/socket.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <unistd.h>'||chr(10)||''||chr(10)||'#define BUF_SZ 256'||chr(10)||''||chr(10)||'int foo(void)'||chr(10)||'{'||chr(10)||'  int n;'||chr(10)||'  char buffer[BUF_SZ];'||chr(10)||'  sockfd = socket(AF_INET, SOCK_STREAM, 0);'||chr(10)||''||chr(10)||'  if (sockfd < 0) {'||chr(10)||'    perror("ERROR opening socket");'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  n = read(sockfd,buffer,255);'||chr(10)||''||chr(10)||'  // use buffer'||chr(10)||'  // if buffer is used as argument to system() or to setenv()'||chr(10)||'  // the buffer may contain untrusted commands or characters leading to unpredictable program behavior'||chr(10)||'  // ...'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RXS.detail', '### RXS'||chr(10)||'#### 概要'||chr(10)||'该程序已从可能包括不受信任数据的外部套接字中读取'||chr(10)||''||chr(10)||'#### 解释'||chr(10)||'Because external sockets'||chr(10)||''||chr(10)||''||chr(10)||'#### 示例'||chr(10)||'````text'||chr(10)||''||chr(10)||'#include <sys/socket.h>'||chr(10)||'#include <stdio.h>'||chr(10)||'#include <stdlib.h>'||chr(10)||'#include <unistd.h>'||chr(10)||''||chr(10)||'#define BUF_SZ 256'||chr(10)||''||chr(10)||'int foo(void)'||chr(10)||'{'||chr(10)||'  int n;'||chr(10)||'  char buffer[BUF_SZ];'||chr(10)||'  sockfd = socket(AF_INET, SOCK_STREAM, 0);'||chr(10)||''||chr(10)||'  if (sockfd < 0) {'||chr(10)||'    perror("ERROR opening socket");'||chr(10)||'    exit(1);'||chr(10)||'  }'||chr(10)||''||chr(10)||'  n = read(sockfd,buffer,255);'||chr(10)||''||chr(10)||'  // use buffer'||chr(10)||'  // if buffer is used as argument to system() or to setenv()'||chr(10)||'  // the buffer may contain untrusted commands or characters leading to unpredictable program behavior'||chr(10)||'  // ...'||chr(10)||'}'||chr(10)||''||chr(10)||'````', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RXS.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} is receiving untrusted data from external socket, the socket has been created in ${ss.filename} at line ${ss.line}.', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RXS.msg_template', 'In ${se.filename}, line ${se.line}, the function ${se.func} is receiving untrusted data from external socket, the socket has been created in ${ss.filename} at line ${ss.line}.', 'system', 'system'),
('en', 'rule.Xcalibyte.BUILTIN.1.RXS.name', 'Read From External Socket', 'system', 'system'),
('zh-CN', 'rule.Xcalibyte.BUILTIN.1.RXS.name', '从外部套接字读取', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

delete from xcalibyte.rule_information_attribute ria
where ria.rule_information_id =
      (select ri.id from xcalibyte."rule_information" ri left join xcalibyte."rule_set" rs on rs.id =ri.rule_set_id left join xcalibyte."scan_engine" se on se.id = rs.scan_engine_id
       where se.name = 'Xcalibyte' and rs.name = 'BUILTIN' and rs.version ='1' and ri.rule_code ='RXS');
