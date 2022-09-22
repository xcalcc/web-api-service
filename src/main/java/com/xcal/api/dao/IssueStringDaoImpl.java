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

import com.xcal.api.entity.v3.IssueString;
import com.xcal.api.mapper.IssueStringMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class IssueStringDaoImpl extends SqlSessionDaoSupport implements IssueStringDao {

	public IssueStringDaoImpl(SqlSessionFactory sqlSessionFactory) {
		this.setSqlSessionFactory(sqlSessionFactory);
	}

	@Override
	public List<IssueString> getIssueStringList(UUID scanTaskId) {
		IssueStringMapper mapper = this.getSqlSession().getMapper(IssueStringMapper.class);
		return mapper.getIssueStringList(scanTaskId);
	}

	@Override
	public int batchInsertIssueString(List<IssueString> issueStringList) {
		IssueStringMapper mapper = this.getSqlSession().getMapper(IssueStringMapper.class);
		return mapper.batchInsertIssueString(issueStringList);
	}

}
