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

import com.xcal.api.entity.v3.AssigneeCountRow;
import com.xcal.api.model.dto.UserCountDto;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface UserMapper {

    List<UserCountDto> getTopAssignees(
            @Param("projectId") UUID projectId,
            @Param("scanTaskId") UUID scanTaskId,
            @Param("ruleCodes") List<SearchIssueGroupRequest.RuleCode> ruleCodes,
            @Param("ruleSets") List<String> ruleSets,
            @Param("filePaths") List<String> filePaths,
            @Param("pathCategory") String pathCategory,
            @Param("certainty") String certainty,
            @Param("dsrType") List<String> dsrType,
            @Param("criticality") String criticality,
            @Param("validationAction") String validationAction,
            @Param("searchValues") List<String> searchValues,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    List<AssigneeCountRow> getAssigneeRuleCodeCount(
            @Param("projectId") UUID projectId,
            @Param("assigned") boolean assigned
    );

    List<AssigneeCountRow> getAssigneeCriticalityRuleCodeCount(
            @Param("projectId") UUID projectId,
            @Param("scanTaskId") UUID scanTaskId,
            @Param("ruleCodes") List<SearchIssueGroupRequest.RuleCode> ruleCodes,
            @Param("ruleSets") List<String> ruleSets,
            @Param("filePaths") List<String> filePaths,
            @Param("pathCategory") String pathCategory,
            @Param("certainty") String certainty,
            @Param("dsrType") List<String> dsrType,
            @Param("criticality") String criticality,
            @Param("assigned") Boolean assigned,
            @Param("validationAction") String validationAction,
            @Param("searchValues") List<String> searchValues
    );


}
