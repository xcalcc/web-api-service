-- ------------------------
-- Path message for General
-- ------------------------
insert into xcalibyte.i18n_message
(locale, message_key, content, created_by, modified_by)
values
('en', 'path.msg.0', 'No message', 'system', 'system'),
('zh-CN', 'path.msg.0', '无讯息', 'system', 'system'),
('en', 'path.msg.1', 'The value was copied from the right hand side to the left hand side', 'system', 'system'),
('zh-CN', 'path.msg.1', '从右侧复制了值到左侧', 'system', 'system'),
('en', 'path.msg.2', 'The function was called here', 'system', 'system'),
('zh-CN', 'path.msg.2', '在此处调用了该函数', 'system', 'system'),
('en', 'path.msg.3', 'The vulnerability occurs here', 'system', 'system'),
('zh-CN', 'path.msg.3', '在此处发生了漏洞', 'system', 'system'),
('en', 'path.msg.4', 'The symbol was declared here', 'system', 'system'),
('zh-CN', 'path.msg.4', '在此处声明了符号', 'system', 'system'),
('en', 'path.msg.5', 'The value was changed by the side effect of the call', 'system', 'system'),
('zh-CN', 'path.msg.5', '调用的附带后果改变了该值', 'system', 'system'),
('en', 'path.msg.6', 'The value might be changed by the side effect of the statement', 'system', 'system'),
('zh-CN', 'path.msg.6', '语句的附带后果可能改变了该值', 'system', 'system'),
('en', 'path.msg.7', 'The control flow was merged here', 'system', 'system'),
('zh-CN', 'path.msg.7', '在此处合并了控制流', 'system', 'system'),
('en', 'path.msg.8', 'The value was defined by an indirect store', 'system', 'system'),
('zh-CN', 'path.msg.8', '间接存储定义了该值', 'system', 'system'),
('en', 'path.msg.9', 'The memory was allocated here', 'system', 'system'),
('zh-CN', 'path.msg.9', '在此处分配了内存', 'system', 'system'),
('en', 'path.msg.10', 'The memory was freed here', 'system', 'system'),
('zh-CN', 'path.msg.10', '在此处释放了内存', 'system', 'system'),
('en', 'path.msg.11', 'The condition was checked here', 'system', 'system'),
('zh-CN', 'path.msg.11', '在此处检查了条件', 'system', 'system'),
('en', 'path.msg.12', 'The condition was checked here', 'system', 'system'),
('zh-CN', 'path.msg.12', '在此处检查了条件', 'system', 'system'),
('en', 'path.msg.13', 'The condition was checked here', 'system', 'system'),
('zh-CN', 'path.msg.13', '在此处检查了条件', 'system', 'system'),
('en', 'path.msg.14', 'The inline function was returned here', 'system', 'system'),
('zh-CN', 'path.msg.14', '在此处返回了内联函数', 'system', 'system'),
('en', 'path.msg.15', 'The value was passed by a parameter', 'system', 'system'),
('zh-CN', 'path.msg.15', '参数传递了该值', 'system', 'system'),
('en', 'path.msg.16', 'The rule was checked here', 'system', 'system'),
('zh-CN', 'path.msg.16', '在此处检查了规则', 'system', 'system'),
('en', 'path.msg.17', 'The branch was taken from here', 'system', 'system'),
('zh-CN', 'path.msg.17', '在此处采取了分支', 'system', 'system'),
('en', 'path.msg.18', 'The value was returned from the function call', 'system', 'system'),
('zh-CN', 'path.msg.18', '从函数调用里返回了该值', 'system', 'system'),
('en', 'path.msg.19', 'The exception was thrown here', 'system', 'system'),
('zh-CN', 'path.msg.19', '在此处抛出了异常', 'system', 'system'),
('en', 'path.msg.20', 'The exception was caught here', 'system', 'system'),
('zh-CN', 'path.msg.20', '在此处捕捉了异常', 'system', 'system'),
('en', 'path.msg.21', 'The related operation was identified here', 'system', 'system'),
('zh-CN', 'path.msg.21', '在此处捕捉了异常', 'system', 'system'),
('en', 'path.msg.22', 'The variable''s address was taken here', 'system', 'system'),
('zh-CN', 'path.msg.22', '在此处得到了变量的地址', 'system', 'system'),
('en', 'path.msg.23', 'The condition was evaluated to "true"', 'system', 'system'),
('zh-CN', 'path.msg.23', '条件计算为"true"', 'system', 'system'),
('en', 'path.msg.24', 'The condition was evaluated to "false"', 'system', 'system'),
('zh-CN', 'path.msg.24', '条件计算为"false"', 'system', 'system'),
('en', 'path.msg.25', 'The "then" block was taken', 'system', 'system'),
('zh-CN', 'path.msg.25', '使用了"then"块', 'system', 'system'),
('en', 'path.msg.26', 'The "else" block was taken', 'system', 'system'),
('zh-CN', 'path.msg.26', '使用了"else"块', 'system', 'system'),
('en', 'path.msg.27', 'The value was propagated here', 'system', 'system'),
('zh-CN', 'path.msg.27', '在此处传播了该值', 'system', 'system')
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