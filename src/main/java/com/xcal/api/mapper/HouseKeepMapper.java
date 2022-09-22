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

package com.xcal.api.mapper;

import com.xcal.api.entity.v3.SourceCodeInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface HouseKeepMapper {

    List<SourceCodeInfo> getHouseKeepSourceCodeInfoList(@Param("projectId") UUID projectId, @Param("retentionPeriod") int retentionPeriod);

    void removeNotReferencedFileInfo();

    void removeScanData(@Param("scanTaskId") UUID scanTaskId);
}
