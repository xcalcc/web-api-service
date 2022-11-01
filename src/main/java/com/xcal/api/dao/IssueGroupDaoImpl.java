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

import com.xcal.api.entity.v3.IssueGroup;
import com.xcal.api.entity.v3.IssueGroupCountRow;
import com.xcal.api.entity.v3.IssueGroupSrcSinkFilePath;
import com.xcal.api.entity.v3.ReportFileStatisticRow;
import com.xcal.api.mapper.IssueGroupMapper;
import com.xcal.api.model.dto.v3.SearchIssueSuggestionDto;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.model.payload.v3.TopCsvCodeRequest;
import com.xcal.api.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.xcal.api.service.IssueService.SEARCH_VAL_DELIMITER;

@Slf4j
@Repository
public class IssueGroupDaoImpl extends SqlSessionDaoSupport implements IssueGroupDao {

    public IssueGroupDaoImpl(SqlSessionFactory sqlSessionFactory) {
        this.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public Page<IssueGroup> searchIssueGroup(
            UUID projectId,
            UUID scanTaskId,
            List<SearchIssueGroupRequest.RuleCode> ruleCodes,
            List<String> ruleSets,
            List<String> filePaths,
            String pathCategory,
            String certainty,
            List<String> dsrType,
            String criticality,
            String validationAction,
            String searchValue,
            Pageable pageable
    ) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return new PageImpl<>(
                mapper.getIssueGroupList(
                        projectId,
                        scanTaskId,
                        ruleCodes,
                        ruleSets,
                        filePaths,
                        pathCategory,
                        certainty,
                        dsrType,
                        criticality,
                        validationAction,
                        StringUtil.splitAndTrim(searchValue,SEARCH_VAL_DELIMITER),
                        pageable.getPageNumber() * pageable.getPageSize(),
                        pageable.getPageSize()
                ),
                pageable,
                mapper.getIssueGroupCount(
                        projectId,
                        scanTaskId,
                        ruleCodes,
                        ruleSets,
                        filePaths,
                        pathCategory,
                        certainty,
                        dsrType,
                        criticality,
                        validationAction,
                        StringUtil.splitAndTrim(searchValue,SEARCH_VAL_DELIMITER)
                )
        );
    }

    @Override
    public Page<IssueGroup> searchOccurIssueGroupsByFixedIssueGroups(
            List<IssueGroup> issueGroups,
            Pageable pageable
    ) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return new PageImpl<>(
                mapper.getOccurIssueGroupsByFixedIssueGroups(
                        issueGroups,
                        pageable.getPageNumber() * pageable.getPageSize(),
                        pageable.getPageSize()
                ),
                pageable,
                issueGroups.size()
        );
    }

    @Override
    public Page<SearchIssueSuggestionDto> searchIssueGroupSuggestion(
            UUID projectId,
            UUID scanTaskId,
            List<SearchIssueGroupRequest.RuleCode> ruleCodes,
            List<String> ruleSets,
            List<String> filePaths,
            String pathCategory,
            String certainty,
            List<String> dsrType,
            String criticality,
            String validationAction,
            String searchValue,
            Pageable pageable
    ) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return new PageImpl<>(
                mapper.getIssueGroupSuggestion(
                        projectId,
                        scanTaskId,
                        ruleCodes,
                        ruleSets,
                        filePaths,
                        pathCategory,
                        certainty,
                        dsrType,
                        criticality,
                        validationAction,
                        StringUtil.splitAndTrim(searchValue,SEARCH_VAL_DELIMITER),
                        pageable.getPageNumber() * pageable.getPageSize(),
                        pageable.getPageSize()
                ),
                pageable,
                mapper.getIssueGroupSuggestionCount(
                        projectId,
                        scanTaskId,
                        ruleCodes,
                        ruleSets,
                        filePaths,
                        pathCategory,
                        certainty,
                        dsrType,
                        criticality,
                        validationAction,
                        StringUtil.splitAndTrim(searchValue,SEARCH_VAL_DELIMITER)
                )
        );
    }


    @Override
    public List<IssueGroup> getIssueGroupList(
            UUID projectId,
            UUID scanTaskId,
            List<SearchIssueGroupRequest.RuleCode> ruleCodes,
            List<String> ruleSets,
            List<String> filePaths,
            String pathCategory,
            String certainty,
            List<String> dsrType,
            String criticality,
            String issueValidation,
            String searchValue,
            int offset,
            int limit
    ) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return
                mapper.getIssueGroupList(
                        projectId,
                        scanTaskId,
                        ruleCodes,
                        ruleSets,
                        filePaths,
                        pathCategory,
                        certainty,
                        dsrType,
                        criticality,
                        issueValidation,
                        StringUtil.splitAndTrim(searchValue,SEARCH_VAL_DELIMITER),
                        offset,
                        limit
                );
    }

    @Override
    public List<IssueGroupSrcSinkFilePath> getIssueGroupSrcSinkFilePathListByScanTaskId(UUID scanTaskId) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.getIssueGroupSrcSinkFilePathListByScanTaskId(scanTaskId);
    }

    @Override
    public List<IssueGroup> getIssueGroupList(UUID projectId, List<String> ruleSets, int offset, int limit) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.getIssueGroupList(
                projectId,
                null,
                null,
                ruleSets,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                offset,
                limit
        );
    }

    @Override
    public Optional<IssueGroup> getIssueGroup(UUID scanTaskId, String issueGroupId) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return Optional.ofNullable(mapper.getIssueGroup(scanTaskId, issueGroupId));
    }

    @Override
    public Optional<IssueGroup> getIssueGroupByIdAndDsrType(UUID projectId, String issueGroupId, List<String> dsrType) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return Optional.ofNullable(mapper.getIssueGroupByIdAndDsrType(projectId, issueGroupId, dsrType));
    }

    @Override
    public void assignIssueGroupToUser(String issueGroupId, UUID userId) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        mapper.assignIssueGroupToUser(issueGroupId, userId);
    }


    @Override
    public int batchInsertIssueGroup(List<IssueGroup> issueGroupList) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.batchInsertIssueGroup(issueGroupList);
    }

    @Override
    public int batchUpdateIssueGroup(UUID projectId, List<IssueGroup> issueGroupList) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.batchUpdateIssueGroup(projectId, issueGroupList);
    }

    @Override
    public int batchUpdateIssueGroupToFixed(UUID projectId, List<IssueGroup> issueGroupList) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.batchUpdateIssueGroupToFixed(projectId, issueGroupList);
    }

    @Override
    public int batchUpsertIssueGroupForLine(UUID projectId, List<IssueGroup> issueGroupList) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.batchUpsertIssueGroupForLine(projectId, issueGroupList);
    }

    @Override
    public int batchUpsertIssueGroupForFixed(UUID projectId, List<IssueGroup> issueGroupList) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.batchUpsertIssueGroupForFixed(projectId, issueGroupList);
    }

    @Override
    public int deleteIssueGroupByProjectId(UUID projectId) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.deleteIssueGroupByProjectId(projectId);
    }

    @Override
    public int deleteFixedIssueGroupByAge(String ageString) {
        log.trace("[deleteFixedIssueGroupByAge] ageString: {}", ageString);
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.deleteFixedIssueGroupByAge(ageString);
    }

    @Override
    public List<IssueGroupCountRow> getIssueGroupCountWithFilter(
            String filterCategory,
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
            String validationAction,
            String searchValue
    ) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.getIssueGroupCountWithFilter(
                filterCategory,
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
                validationAction,
                StringUtil.splitAndTrim(searchValue,SEARCH_VAL_DELIMITER)
        );
    }


    @Override
    public List<IssueGroupCountRow> getIssueGroupCriticalityCount(
            String filterCategory,
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
            String validationAction,
            String searchValue
    ) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.getIssueGroupCriticalityCount(filterCategory,
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
                validationAction,
                StringUtil.splitAndTrim(searchValue,SEARCH_VAL_DELIMITER));
    }

    @Override
    public List<IssueGroupCountRow> getTopCsvCodes(
            TopCsvCodeRequest topCsvCodeRequest
    ) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.getTopCsvCodes(
                topCsvCodeRequest.getProjectId(),
                topCsvCodeRequest.getScanTaskId(),
                topCsvCodeRequest.getRuleCodes(),
                topCsvCodeRequest.getRuleSets(),
                topCsvCodeRequest.getDsrType(),
                topCsvCodeRequest.getTop()
        );
    }


    @Override
    public List<ReportFileStatisticRow> getReportFileStatisticRow(
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
            String validationAction,
            String searchValue
    ) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.getReportFileStatisticRow(
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
                validationAction,
                StringUtil.splitAndTrim(searchValue,SEARCH_VAL_DELIMITER)
        );
    }

    @Override
    public Optional<IssueGroup> findIssueGroupStartWith(String prefix) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return Optional.ofNullable(mapper.findIssueGroupStartWith(prefix));
    }

    @Override
    public int assignAllFromBaseline(
            UUID currentScanTaskId,
            UUID baselineScanTaskId
    ) {
        IssueGroupMapper mapper = this.getSqlSession().getMapper(IssueGroupMapper.class);
        return mapper.assignAllFromBaseline(currentScanTaskId,baselineScanTaskId);
    }


}
