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
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "EmailServerConfiguration")
public class EmailServerConfiguration {

    public enum Protocol {
        smtp, pop, imap // Protocol will be lower case
    }

    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    String host;

    @NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL)
    Protocol protocol;

    @Min(value = 1,message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_PORT)
    @Max(value = 65535,message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_PORT)
    int port;

    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    String username;

    String password;

    @NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL)
    Boolean starttls;

    String prefix;

    String from;
}
