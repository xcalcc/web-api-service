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

package com.xcal.api.dao;

import com.xcal.api.entity.v3.AssigneeCountRow;
import com.xcal.api.mapper.UserMapper;
import com.xcal.api.model.dto.UserCountDto;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.xcal.api.service.IssueService.SEARCH_VAL_DELIMITER;

@Slf4j
@Repository
public class UserDaoImpl extends SqlSessionDaoSupport implements UserDao {

    public UserDaoImpl(SqlSessionFactory sqlSessionFactory) {
        this.setSqlSessionFactory(sqlSessionFactory);
    }

    public List<UserCountDto> getTopAssignees(
            UUID projectId,
            UUID scanTaskId,
            List<SearchIssueGroupRequest.RuleCode> ruleCodes,
            List<String> ruleSets,
            List<String> filePaths,
            String pathCategory,
            String certainty,
            List<String> dsrType,
            String criticality,
            String searchValue,
            Pageable pageable
    ) {
        UserMapper mapper = this.getSqlSession().getMapper(UserMapper.class);
        return mapper.getTopAssignees(
                projectId,
                scanTaskId,
                ruleCodes,
                ruleSets,
                filePaths,
                pathCategory,
                certainty,
                dsrType,
                criticality,
                StringUtil.splitAndTrim(searchValue,SEARCH_VAL_DELIMITER),
                pageable.getPageNumber() * pageable.getPageSize(),
                pageable.getPageSize()
        );
    }


    @Override
    public List<AssigneeCountRow> getAssigneeRuleCodeCount(
            UUID projectId,
            boolean assigned
    ) {
        UserMapper mapper = this.getSqlSession().getMapper(UserMapper.class);
        return mapper.getAssigneeRuleCodeCount(
                projectId,
                assigned

        );
    }

    @Override
    public List<AssigneeCountRow> getAssigneeCriticalityRuleCodeCount(
            UUID projectId,
            UUID scanTaskId,
            List<SearchIssueGroupRequest.RuleCode> ruleCodes,
            List<String> ruleSets,
            List<String> filePaths,
            String pathCategory,
            String certainty,
            List<String> dsrType,
            String criticality,
            Boolean assigned,
            String searchValue
    ) {
        UserMapper mapper = this.getSqlSession().getMapper(UserMapper.class);
        return mapper.getAssigneeCriticalityRuleCodeCount(
                projectId,
                scanTaskId,
                ruleCodes,
                ruleSets,
                filePaths,
                pathCategory,
                certainty,
                dsrType,
                criticality,
                assigned,
                StringUtil.splitAndTrim(searchValue,SEARCH_VAL_DELIMITER)
        );
    }

}
