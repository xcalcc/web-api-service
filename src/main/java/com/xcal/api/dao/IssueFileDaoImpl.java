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

import com.xcal.api.entity.v3.IssueFile;
import com.xcal.api.mapper.IssueFileMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class IssueFileDaoImpl extends SqlSessionDaoSupport implements IssueFileDao {

	public IssueFileDaoImpl(SqlSessionFactory sqlSessionFactory) {
		this.setSqlSessionFactory(sqlSessionFactory);
	}

	@Override
	public List<IssueFile> getIssueFileList(UUID scanTaskId) {
		IssueFileMapper mapper = this.getSqlSession().getMapper(IssueFileMapper.class);
		return mapper.getIssueFileList(scanTaskId);
	}

	@Override
	public int batchInsertIssueFile(List<IssueFile> issueFileList) {
		IssueFileMapper mapper = this.getSqlSession().getMapper(IssueFileMapper.class);
		return mapper.batchInsertIssueFile(issueFileList);
	}

}
