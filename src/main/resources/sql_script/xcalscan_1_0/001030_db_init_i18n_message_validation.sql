INSERT INTO xcalibyte.i18n_message 
(locale, message_key, content, created_by, modified_by)
 VALUES
('en', 'e.api.validation.constraints.Email', 'Must be a correct email address', 'system', 'system'),
('zh-CN', 'e.api.validation.constraints.Email', '必须是正确的电子邮件地址', 'system', 'system'),
('en', 'e.api.validation.constraints.Min', 'Must be greater than or equal to ${value}', 'system', 'system'),
('zh-CN', 'e.api.validation.constraints.Min', '必须大于或等于${value}', 'system', 'system'),
('en', 'e.api.validation.constraints.NotBlank', 'Must not be blank', 'system', 'system'),
('zh-CN', 'e.api.validation.constraints.NotBlank', '不能是空白的', 'system', 'system'),
('en', 'e.api.validation.constraints.NotNull', 'Must not be null', 'system', 'system'),
('zh-CN', 'e.api.validation.constraints.NotNull', '不能为空', 'system', 'system'),
('en', 'e.api.validation.constraints.Pattern', 'Must match "${regexp}"', 'system', 'system'),
('zh-CN', 'e.api.validation.constraints.Pattern', '必须匹配 "${regexp}"', 'system', 'system'),
('en', 'e.api.validation.constraints.Port', 'Must be between 1 and 65535', 'system', 'system'),
('zh-CN', 'e.api.validation.constraints.Port', '必须介于1和65535之间', 'system', 'system'),
('en', 'e.api.validation.constraints.Size', 'Size must be between ${min} and ${max}', 'system', 'system'),
('zh-CN', 'e.api.validation.constraints.Size', '大小必须介于${min}和${max}之间', 'system', 'system')
ON CONFLICT (locale, message_key) DO UPDATE set content = excluded.content;