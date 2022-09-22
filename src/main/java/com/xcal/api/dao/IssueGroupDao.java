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
import com.xcal.api.entity.v3.ReportFileStatisticRow;
import com.xcal.api.model.dto.v3.SearchIssueSuggestionDto;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.model.payload.v3.TopCsvCodeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IssueGroupDao {

    String FILTER_CATEGORY_CRITICALITY = "CRITICALITY";

    String FILTER_CATEGORY_CERTAINTY = "CERTAINTY";

    String FILTER_CATEGORY_RULE_CODE = "RULE_CODE";


    Page<IssueGroup> searchIssueGroup(
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
    );

    Page<IssueGroup> searchOccurIssueGroupsByFixedIssueGroups(List<IssueGroup> issueGroups, Pageable pageable);

    Page<SearchIssueSuggestionDto> searchIssueGroupSuggestion(
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
    );

    List<IssueGroup> getIssueGroupList(
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
            int offset,
            int limit
    );


    List<IssueGroup> getIssueGroupList(UUID projectId, List<String> ruleSets, int offset, int limit);

    Optional<IssueGroup> getIssueGroup(UUID scanTaskId, String issueGroupId);

    Optional<IssueGroup> getIssueGroupByIdAndDsrType(UUID projectId, String issueGroupId, List<String> dsrType);

    void assignIssueGroupToUser(String issueGroupId, UUID userId);

    int batchInsertIssueGroup(List<IssueGroup> issueGroupList);

    int batchUpdateIssueGroup(UUID projectId, List<IssueGroup> issueGroupList);

    int batchUpdateIssueGroupToFixed(UUID projectId, List<IssueGroup> issueGroupList);

    int batchUpsertIssueGroupForLine(UUID projectId, List<IssueGroup> issueGroupList);

    int batchUpsertIssueGroupForFixed(UUID projectId, List<IssueGroup> issueGroupList);

    int deleteIssueGroupByProjectId(UUID projectId);

    int deleteFixedIssueGroupByAge(String ageString);

    /***
     * This is for getting issue group count
     * @param filterCategory
     * @param projectId
     * @param scanTaskId
     * @param ruleCodes
     * @param ruleSets
     * @param filePaths
     * @param pathCategory
     * @param certainty
     * @param dsrType
     * @param criticality
     * @param assigned
     * @param searchValue
     * @return
     */
    List<IssueGroupCountRow> getIssueGroupCountWithFilter(
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
            String searchValue
    );

    List<IssueGroupCountRow> getIssueGroupCriticalityCount(
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
            String searchValue
    );

    List<IssueGroupCountRow> getTopCsvCodes(TopCsvCodeRequest topCsvCodeRequest);

    List<ReportFileStatisticRow> getReportFileStatisticRow(
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
    );

    Optional<IssueGroup> findIssueGroupStartWith(String prefix);

    int assignAllFromBaseline(
            UUID currentScanTaskId,
            UUID baselineScanTaskId
    );

}
