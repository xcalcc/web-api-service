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

import com.xcal.api.exception.AppException;
import com.xcal.api.util.MessagesTemplate;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ValidationResult", description = "")
public class ValidationResult {
    public enum Status {
        SUCCESS, FAIL, UNDECIDABLE
    }

    @NotBlank(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK)
    Status status;

    String message;

    String detailMessage;

    AppException exception;

}
