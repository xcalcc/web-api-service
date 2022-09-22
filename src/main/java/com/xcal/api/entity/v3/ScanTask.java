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

package com.xcal.api.entity.v3;


import com.xcal.api.entity.Project;
import com.xcal.api.entity.ProjectConfig;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanTask {

    UUID id;

    UUID projectId;

    UUID projectConfigId;

    String status;

    String engine; // xcalibyte, clang, etc.

    String engineVersion;

    String scanMode; // analyze module

    String sourceRoot;

    String scanParameter;

    String scanRemarks;

    Date scanStartAt;

    Date scanEndAt;

    Date houseKeepOn;

    String createdBy;

    Date createdOn;

    String modifiedBy;

    Date modifiedOn;

}
