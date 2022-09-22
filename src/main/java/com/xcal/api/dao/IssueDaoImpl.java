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

import com.xcal.api.entity.v3.Issue;
import com.xcal.api.entity.v3.IssueGroup;
import com.xcal.api.mapper.IssueMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class IssueDaoImpl extends SqlSessionDaoSupport implements IssueDao {

	public IssueDaoImpl(SqlSessionFactory sqlSessionFactory) {
		this.setSqlSessionFactory(sqlSessionFactory);
	}

	@Override
	public Page<Issue> findByIssueGroup(UUID scanTaskId, String issueGroupId, Pageable pageable) {
		IssueMapper mapper = this.getSqlSession().getMapper(IssueMapper.class);
		return new PageImpl<>(
				mapper.getIssueList(
						scanTaskId,
						issueGroupId,
						pageable.getPageNumber() * pageable.getPageSize(),
						pageable.getPageSize()
				),
				pageable,
				mapper.getIssueCount(scanTaskId,issueGroupId)
		);
	}

	@Override
	public int batchSoftDeleteIssueByIssueGroup(List<IssueGroup> issueGroupList) {
		IssueMapper mapper = this.getSqlSession().getMapper(IssueMapper.class);
		return mapper.batchSoftDeleteIssueByIssueGroup(issueGroupList);
	}

	@Override
	public int batchHardDeleteIssueByIssueGroup(List<IssueGroup> issueGroupList) {
		IssueMapper mapper = this.getSqlSession().getMapper(IssueMapper.class);
		return mapper.batchHardDeleteIssueByIssueGroup(issueGroupList);
	}


	@Override
	public int batchInsertIssue(List<Issue> issueList) {
		IssueMapper mapper = this.getSqlSession().getMapper(IssueMapper.class);
		return mapper.batchInsertIssue(issueList);
	}

	@Override
	public int batchInsertIssueWithFaultTolerance(List<Issue> issueList) {
		IssueMapper mapper = this.getSqlSession().getMapper(IssueMapper.class);
		return mapper.batchInsertIssueWithFaultTolerance(issueList);
	}

	@Override
	public List<Issue> findByScanTaskId(UUID scanTaskId) {
		IssueMapper mapper = this.getSqlSession().getMapper(IssueMapper.class);
		return mapper.getIssueListByScanTaskId(scanTaskId);
	}

}
