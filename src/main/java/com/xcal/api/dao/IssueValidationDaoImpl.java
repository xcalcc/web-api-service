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

import com.xcal.api.entity.v3.IssueValidation;
import com.xcal.api.mapper.IssueValidationMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public class IssueValidationDaoImpl extends SqlSessionDaoSupport implements IssueValidationDao {

    public IssueValidationDaoImpl(SqlSessionFactory sqlSessionFactory) {this.setSqlSessionFactory(sqlSessionFactory);}

    @Override
    public int addIssueValidation(IssueValidation issueValidation) {
        IssueValidationMapper mapper = this.getSqlSession().getMapper(IssueValidationMapper.class);
        return mapper.addIssueValidation(issueValidation);
    }

    @Override
    public Page<IssueValidation> listIssueValidations(Pageable pageable) {
        IssueValidationMapper mapper = this.getSqlSession().getMapper(IssueValidationMapper.class);
        List<IssueValidation> issueValidations = mapper.listIssueValidations(pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize());
        return new PageImpl<>(
                issueValidations,
                pageable,
                issueValidations.size()
        );
    }

    @Override
    public Page<IssueValidation> searchIssueValidations(String type, String action, String scope, Pageable pageable) {
        IssueValidationMapper mapper = this.getSqlSession().getMapper(IssueValidationMapper.class);
        List<IssueValidation> issueValidations = mapper.searchIssueValidations(type, action, scope, pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize());
        return new PageImpl<>(
                issueValidations,
                pageable,
                mapper.getIssueValidationCount(type, action, scope)
        );
    }

    @Override
    public IssueValidation findIssueValidationById(UUID id) {
        IssueValidationMapper mapper = this.getSqlSession().getMapper(IssueValidationMapper.class);
        return mapper.findIssueValidationById(id);
    }

    @Override
    public int updateIssueValidation(IssueValidation issueValidation) {
        IssueValidationMapper mapper = this.getSqlSession().getMapper(IssueValidationMapper.class);
        return mapper.updateIssueValidation(issueValidation);
    }

    @Override
    public void deleteIssueValidationById(UUID id) {
        IssueValidationMapper mapper = this.getSqlSession().getMapper(IssueValidationMapper.class);
        mapper.deleteIssueValidationById(id);
    }

}
