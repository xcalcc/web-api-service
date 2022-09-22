-- ------------------------
-- Path message for General
-- ------------------------
insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'path.msg.0', 'No message', 'system', 'system'),
('zh-CN', 'path.msg.0', '无讯息', 'system', 'system'),
('en', 'path.msg.1', 'Value copied', 'system', 'system'),
('zh-CN', 'path.msg.1', '值被复制', 'system', 'system'),
('en', 'path.msg.2', 'Function called', 'system', 'system'),
('zh-CN', 'path.msg.2', '进入函数', 'system', 'system'),
('en', 'path.msg.3', 'Vulnerable point', 'system', 'system'),
('zh-CN', 'path.msg.3', '漏洞触发点', 'system', 'system'),
('en', 'path.msg.4', 'Symbol declared', 'system', 'system'),
('zh-CN', 'path.msg.4', '变量声明处', 'system', 'system'),
('en', 'path.msg.5', 'Value changed due to call side', 'system', 'system'),
('zh-CN', 'path.msg.5', '函数会改变该值', 'system', 'system'),
('en', 'path.msg.6', 'Statement may change value', 'system', 'system'),
('zh-CN', 'path.msg.6', '语句会改变该值', 'system', 'system'),
('en', 'path.msg.7', 'Control flow merged', 'system', 'system'),
('zh-CN', 'path.msg.7', '控制流合并点', 'system', 'system'),
('en', 'path.msg.8', 'Value set by indirect store', 'system', 'system'),
('zh-CN', 'path.msg.8', '间接写入', 'system', 'system'),
('en', 'path.msg.9', 'Memory allocated', 'system', 'system'),
('zh-CN', 'path.msg.9', '内存被分配', 'system', 'system'),
('en', 'path.msg.10', 'Memory freed', 'system', 'system'),
('zh-CN', 'path.msg.10', '内存释放点', 'system', 'system'),
('en', 'path.msg.11', 'Check condition ', 'system', 'system'),
('zh-CN', 'path.msg.11', '条件检查点', 'system', 'system'),
('en', 'path.msg.12', 'Function exit point', 'system', 'system'),
('zh-CN', 'path.msg.12', '函数退出', 'system', 'system'),
('en', 'path.msg.13', 'Inline function called', 'system', 'system'),
('zh-CN', 'path.msg.13', '进入内联函数', 'system', 'system'),
('en', 'path.msg.14', 'Inline function returned', 'system', 'system'),
('zh-CN', 'path.msg.14', '退出内联函数', 'system', 'system'),
('en', 'path.msg.15', 'Parameter value', 'system', 'system'),
('zh-CN', 'path.msg.15', '参数传递值', 'system', 'system'),
('en', 'path.msg.16', 'Rule checked here', 'system', 'system'),
('zh-CN', 'path.msg.16', '规则检查点', 'system', 'system'),
('en', 'path.msg.17', 'Branch taken', 'system', 'system'),
('zh-CN', 'path.msg.17', '采取分支', 'system', 'system'),
('en', 'path.msg.18', 'Value returned from call', 'system', 'system'),
('zh-CN', 'path.msg.18', '函数返回值', 'system', 'system'),
('en', 'path.msg.19', 'Exception thrown', 'system', 'system'),
('zh-CN', 'path.msg.19', '抛出异常', 'system', 'system'),
('en', 'path.msg.20', 'Exception caught', 'system', 'system'),
('zh-CN', 'path.msg.20', '捕捉异常', 'system', 'system'),
('en', 'path.msg.21', 'Object property changed by function call', 'system', 'system'),
('zh-CN', 'path.msg.21', '对象属性被函数调用改变', 'system', 'system'),
('en', 'path.msg.22', 'Address taken', 'system', 'system'),
('zh-CN', 'path.msg.22', '变量地址被寄取', 'system', 'system'),
('en', 'path.msg.23', 'Condition set true', 'system', 'system'),
('zh-CN', 'path.msg.23', '条件计算为"true"', 'system', 'system'),
('en', 'path.msg.24', 'Condition set false', 'system', 'system'),
('zh-CN', 'path.msg.24', '条件计算为"false"', 'system', 'system'),
('en', 'path.msg.25', '"then" block taken', 'system', 'system'),
('zh-CN', 'path.msg.25', '采取"then"块', 'system', 'system'),
('en', 'path.msg.26', '"else" block taken', 'system', 'system'),
('zh-CN', 'path.msg.26', '采取"else"块', 'system', 'system'),
('en', 'path.msg.27', 'Value propagated', 'system', 'system'),
('zh-CN', 'path.msg.27', '值被转发', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'path.msg.wrf.1', 'file opened with "r" attribute and assigned to file handle', 'system', 'system'),
('zh-CN', 'path.msg.wrf.1', '文件用"r"属性打开了并赋给文件句柄', 'system', 'system'),
('en', 'path.msg.wrf.2', 'file open successful', 'system', 'system'),
('zh-CN', 'path.msg.wrf.2', '文件打開成功', 'system', 'system'),
('en', 'path.msg.wrf.3', 'fwrite() operation was called with file handle', 'system', 'system'),
('zh-CN', 'path.msg.wrf.3', 'fwrite()使用了文件句柄', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'path.msg.fio42-c.1', 'file opened with "r" attribute and assigned to file handle', 'system', 'system'),
('zh-CN', 'path.msg.fio42-c.1', '打开文件', 'system', 'system'),
('en', 'path.msg.fio42-c.2', 'program exit', 'system', 'system'),
('zh-CN', 'path.msg.fio42-c.2', '程序终止', 'system', 'system'),
('en', 'path.msg.fio45-c.1', 'file opened ', 'system', 'system'),
('zh-CN', 'path.msg.fio45-c.1', '打开文件', 'system', 'system'),
('en', 'path.msg.fio45-c.2', 'file handle checked and validated', 'system', 'system'),
('zh-CN', 'path.msg.fio45-c.2', '文件句柄通过检查并验证', 'system', 'system'),
('en', 'path.msg.fio45-c.3', 'write to file', 'system', 'system'),
('zh-CN', 'path.msg.fio45-c.3', '写入文件', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'path.msg.env03-j.1', 'dangerous combined permission granted', 'system', 'system'),
('zh-CN', 'path.msg.env03-j.1', '有危险的权限组合', 'system', 'system'),
('en', 'path.msg.env03-j.2', 'permission added to object', 'system', 'system'),
('zh-CN', 'path.msg.env03-j.2', '权限添加到对象', 'system', 'system'),
('en', 'path.msg.fio02-j.1', 'new a file', 'system', 'system'),
('zh-CN', 'path.msg.fio02-j.1', '打开文件', 'system', 'system'),
('en', 'path.msg.fio02-j.2', 'delete() called', 'system', 'system'),
('zh-CN', 'path.msg.fio02-j.2', '执行delete() ', 'system', 'system'),
('en', 'path.msg.fio02-j.3', 'exit method', 'system', 'system'),
('zh-CN', 'path.msg.fio02-j.3', '退出函数', 'system', 'system'),
('en', 'path.msg.fio52-j.1', 'tainted cookie instance created', 'system', 'system'),
('zh-CN', 'path.msg.fio52-j.1', '创建可受污染的cookie实例', 'system', 'system'),
('en', 'path.msg.fio52-j.2', 'secure flag set to unencrypted', 'system', 'system'),
('zh-CN', 'path.msg.fio52-j.2', '安全标志未设置为加密', 'system', 'system'),
('en', 'path.msg.fio52-j.3', 'unencrypted sensitive data stored on client side ', 'system', 'system'),
('zh-CN', 'path.msg.fio52-j.3', '未加密的敏感数据 存储在客户端', 'system', 'system'),
('en', 'path.msg.ids01-j.1', 'validated', 'system', 'system'),
('zh-CN', 'path.msg.ids01-j.1', '通过验证', 'system', 'system'),
('en', 'path.msg.ids01-j.2', 'normalized', 'system', 'system'),
('zh-CN', 'path.msg.ids01-j.2', '规范化', 'system', 'system'),
('en', 'path.msg.ids11-j.1', 'normalized', 'system', 'system'),
('zh-CN', 'path.msg.ids11-j.1', '规范化', 'system', 'system'),
('en', 'path.msg.ids11-j.2', 'validated', 'system', 'system'),
('zh-CN', 'path.msg.ids11-j.2', '通过验证', 'system', 'system'),
('en', 'path.msg.ids11-j.3', 'modified', 'system', 'system'),
('zh-CN', 'path.msg.ids11-j.3', '被俢改', 'system', 'system'),
('en', 'path.msg.ids16-j.1', 'XML string source', 'system', 'system'),
('zh-CN', 'path.msg.ids16-j.1', 'XML字符串源', 'system', 'system'),
('en', 'path.msg.ids16-j.2', 'send to server', 'system', 'system'),
('zh-CN', 'path.msg.ids16-j.2', '发送到服务器端', 'system', 'system'),
('en', 'path.msg.ids17-j.1', 'create parse instance of entity', 'system', 'system'),
('zh-CN', 'path.msg.ids17-j.1', '创建实体的解析器实例', 'system', 'system'),
('en', 'path.msg.ids17-j.2', 'create XML reader', 'system', 'system'),
('zh-CN', 'path.msg.ids17-j.2', '创建XML读取器', 'system', 'system'),
('en', 'path.msg.ids17-j.3', 'read and parse XML', 'system', 'system'),
('zh-CN', 'path.msg.ids17-j.3', '分析XML', 'system', 'system'),
('en', 'path.msg.msc62-j.1', 'set sensitive data for hash', 'system', 'system'),
('zh-CN', 'path.msg.msc62-j.1', '注册敏感数据', 'system', 'system'),
('en', 'path.msg.msc62-j.2', 'insecured seed, salt or hash used', 'system', 'system'),
('zh-CN', 'path.msg.msc62-j.2', '使用了不安全的哈希算法或者没有使用salt', 'system', 'system'),
('en', 'path.msg.obj09-j.1', 'object instance created', 'system', 'system'),
('zh-CN', 'path.msg.obj09-j.1', '産生类的实例', 'system', 'system'),
('en', 'path.msg.obj09-j.2', 'obtain object name', 'system', 'system'),
('zh-CN', 'path.msg.obj09-j.2', '获取对象名', 'system', 'system'),
('en', 'path.msg.obj09-j.3', 'use name to compare', 'system', 'system'),
('zh-CN', 'path.msg.obj09-j.3', '用名称进行比较', 'system', 'system'),
('en', 'path.msg.sec06-j.1', 'class is loaded', 'system', 'system'),
('zh-CN', 'path.msg.sec06-j.1', 'class加载', 'system', 'system'),
('en', 'path.msg.sec06-j.2', 'method accquired', 'system', 'system'),
('zh-CN', 'path.msg.sec06-j.2', '方法获取', 'system', 'system'),
('en', 'path.msg.sec06-j.3', 'method invoked', 'system', 'system'),
('zh-CN', 'path.msg.sec06-j.3', '方法调用', 'system', 'system'),
('en', 'path.msg.sec07-j.1', 'exit without getting permission from superclass', 'system', 'system'),
('zh-CN', 'path.msg.sec07-j.1', '未经超类许可而退出', 'system', 'system'),
('en', 'path.msg.sec07-j.2', 'call getPermission()', 'system', 'system'),
('zh-CN', 'path.msg.sec07-j.2', 'getPermission()被调用', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;

-- ------------------------
-- message template for unknown information
-- ------------------------
insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'msg_template.unknown_file', 'unknown_file', 'system', 'system'),
('zh-CN', 'msg_template.unknown_file', '未知文件', 'system', 'system'),
('en', 'msg_template.unknown_filename', 'unknown_filename', 'system', 'system'),
('zh-CN', 'msg_template.unknown_filename', '未知文件', 'system', 'system'),
('en', 'msg_template.unknown_function', 'unknown_function', 'system', 'system'),
('zh-CN', 'msg_template.unknown_function', '未知函数名', 'system', 'system'),
('en', 'msg_template.unknown_variable', 'unknown_variable', 'system', 'system'),
('zh-CN', 'msg_template.unknown_variable', '未知变量名', 'system', 'system'),
('en', 'msg_template.unknown_line', 'unknown_line', 'system', 'system'),
('zh-CN', 'msg_template.unknown_line', '未知行數', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;