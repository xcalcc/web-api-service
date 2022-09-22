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

package com.xcal.api.util;

public final class MessagesTemplate {

    private MessagesTemplate() {
    }

    public static final String E_API_VALIDATION_CONSTRAINTS_NOTBLANK = "${e.api.validation.constraints.NotBlank}";
    public static final String E_API_VALIDATION_CONSTRAINTS_NOTNULL = "${e.api.validation.constraints.NotNull}";
    public static final String E_API_VALIDATION_CONSTRAINTS_PATTERN = "${e.api.validation.constraints.Pattern}";
    public static final String E_API_VALIDATION_CONSTRAINTS_EMAIL = "${e.api.validation.constraints.Email}";
    public static final String E_API_VALIDATION_CONSTRAINTS_SIZE = "${e.api.validation.constraints.Size}";
    public static final String E_API_VALIDATION_CONSTRAINTS_PORT = "${e.api.validation.constraints.Port}";
    public static final String E_API_VALIDATION_CONSTRAINTS_MIN = "${e.api.validation.constraints.Min}";
}