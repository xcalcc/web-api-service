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

package com.xcal.api.model.payload;

import com.xcal.api.util.MessagesTemplate;
import com.xcal.api.util.VariableUtil;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="NewUserRequest", description = "User object use as request parameter only")
public class NewUserRequest {

    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    @Pattern(regexp = VariableUtil.USER_NAME_PATTERN,message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_PATTERN)
    String username;

    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    @Size(min = 1,max = 15,message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_SIZE)
    String displayName;

    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    @Email(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_EMAIL)
    String email;

    String isAdmin;

    @ToString.Exclude
    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    String password;
}
