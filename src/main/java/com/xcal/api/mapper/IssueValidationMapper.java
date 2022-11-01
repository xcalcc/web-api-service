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

import com.xcal.api.entity.v3.IssueValidation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface IssueValidationMapper {

    int addIssueValidation(@Param("issueValidation") IssueValidation issueValidation);

    List<IssueValidation> listIssueValidations(@Param("offset") int offset, @Param("limit") int limit);

    List<IssueValidation> searchIssueValidations(@Param("type")String type, @Param("action")String action, @Param("scope")String scope, @Param("offset") int offset, @Param("limit") int limit);

    long getIssueValidationCount(@Param("type")String type, @Param("action")String action, @Param("scope")String scope);

    IssueValidation findIssueValidationById(@Param("id") UUID id);

    int updateIssueValidation(@Param("issueValidation")IssueValidation issueValidation);

    void deleteIssueValidationById(@Param("id") UUID id);
}
