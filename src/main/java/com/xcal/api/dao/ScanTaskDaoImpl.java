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

import com.xcal.api.entity.v3.ScanTask;
import com.xcal.api.entity.v3.ScanTaskLog;
import com.xcal.api.mapper.ScanTaskMapper;
import com.xcal.api.model.payload.v3.ScanTaskIdResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class ScanTaskDaoImpl extends SqlSessionDaoSupport implements ScanTaskDao {

    public ScanTaskDaoImpl(SqlSessionFactory sqlSessionFactory) {
        this.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public Page<ScanTaskLog> searchScanTaskLog(
            UUID projectId,
            Date targetRangeStartDate, Date targetRangeEndDate, String commitIdPattern,
            Boolean isDsrProject, List<String> ruleSets, List<String> repoActions, Pageable pageable
    ) {
        ScanTaskMapper mapper = this.getSqlSession().getMapper(ScanTaskMapper.class);
        List<ScanTaskLog> scanTaskLogList = mapper.searchScanTaskLog(
                projectId,
                targetRangeStartDate,
                targetRangeEndDate,
                commitIdPattern,
                isDsrProject,
                ruleSets,
                repoActions,
                pageable.getPageNumber() * pageable.getPageSize(),
                pageable.getPageSize()
        );

        Long scanTaskLogCount = getScanTaskLogCount(projectId,
                targetRangeStartDate,
                targetRangeEndDate,
                commitIdPattern,
                isDsrProject,
                ruleSets,
                repoActions);
        return new PageImpl<>(scanTaskLogList, pageable, scanTaskLogCount);
    }

    @Override
    public Long getScanTaskLogCount(
            UUID projectId, Date targetRangeStartDate, Date targetRangeEndDate, String commitIdPattern, Boolean isDsrProject,
            List<String> ruleSets, List<String> repoActions
    ) {
        ScanTaskMapper mapper = this.getSqlSession().getMapper(ScanTaskMapper.class);
        return mapper.getScanTaskLogCount(
                projectId,
                targetRangeStartDate,
                targetRangeEndDate,
                commitIdPattern,
                isDsrProject,
                ruleSets,
                repoActions
        );

    }


    @Override
    public ScanTaskIdResponse getScanTaskIdResponse(
            UUID projectId,
            String commitId
    ) {
        ScanTaskMapper mapper = this.getSqlSession().getMapper(ScanTaskMapper.class);
        ScanTaskIdResponse scanTaskIdResponse = mapper.getScanTaskIdResponse(
                projectId,
                commitId
        );
        return scanTaskIdResponse;
    }


    @Override
    public Optional<ScanTask> getScanTaskById(UUID projectId) {
        ScanTaskMapper mapper = this.getSqlSession().getMapper(ScanTaskMapper.class);
        Optional<ScanTask> scanTask = mapper.getScanTaskById(
                projectId
        );
        return scanTask;
    }

    @Override
    public Optional<ScanTask> getLastScanTaskByProjectId(UUID projectId) {
        return getLastScanTaskByProjectId(projectId, null);
    }

    @Override
    public Optional<ScanTask> getLastScanTaskByProjectId(UUID projectId, String status) {
        ScanTaskMapper mapper = this.getSqlSession().getMapper(ScanTaskMapper.class);
        Optional<ScanTask> scanTask = mapper.getLastScanTaskByProjectId(
                projectId,
                status
        );
        return scanTask;
    }

    @Override
    public Optional<ScanTask> getLastScanTaskByScanTask(UUID projectId, UUID scanTaskId, String status) {
        ScanTaskMapper mapper = this.getSqlSession().getMapper(ScanTaskMapper.class);
        Optional<ScanTask> scanTask = mapper.getLastScanTaskByScanTask(
                projectId,
                scanTaskId,
                status
        );
        return scanTask;
    }

    @Override
    public Optional<ScanTask> getFirstScanTaskWithBaseline(UUID projectId) {
        ScanTaskMapper mapper = this.getSqlSession().getMapper(ScanTaskMapper.class);
        Optional<ScanTask> scanTask = mapper.getFirstScanTaskWithBaseline(
                projectId
        );
        return scanTask;
    }

    @Override
    public Optional<String> getScanTaskIdFromProjectAndCommitId(UUID projectId, String commitId, String status) {
        ScanTaskMapper mapper = this.getSqlSession().getMapper(ScanTaskMapper.class);
        Optional<String> scanTaskId = mapper.getScanTaskIdFromProjectAndCommitId(
                projectId, commitId, status
        );
        return scanTaskId;
    }

    @Override
    public Optional<String> getCommitIdByScanTaskId(@Param("scanTaskId") UUID scanTaskId){
        ScanTaskMapper mapper = this.getSqlSession().getMapper(ScanTaskMapper.class);
        return mapper.getCommitIdByScanTaskId(scanTaskId);
    }


}
