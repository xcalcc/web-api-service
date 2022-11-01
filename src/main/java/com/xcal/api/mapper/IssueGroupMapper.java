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

import com.xcal.api.entity.v3.IssueGroup;
import com.xcal.api.entity.v3.IssueGroupCountRow;
import com.xcal.api.entity.v3.IssueGroupSrcSinkFilePath;
import com.xcal.api.entity.v3.ReportFileStatisticRow;
import com.xcal.api.model.dto.v3.SearchIssueSuggestionDto;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface IssueGroupMapper {

    /***
     *
     * @param projectId
     * @param scanTaskId
     * @param ruleCodes
     * @param ruleSets
     * @param filePaths
     * @param pathCategory
     * @param certainty
     * @param dsrType
     * @param criticality
     * @param validationAction
     * @param searchValues
     * @param offset
     * @param limit
     * @return
     */
    List<IssueGroup> getIssueGroupList(
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

    List<IssueGroupSrcSinkFilePath> getIssueGroupSrcSinkFilePathListByScanTaskId(@Param("scanTaskId") UUID scanTaskId);

    long getIssueGroupCount(
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
            @Param("searchValues") List<String> searchValues
    );

    List<SearchIssueSuggestionDto> getIssueGroupSuggestion(
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

    long getIssueGroupSuggestionCount(
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
            @Param("searchValues") List<String> searchValues
    );



    IssueGroup getIssueGroup(@Param("scanTaskId") UUID scanTaskId, @Param("issueGroupId") String issueGroupId);

    /***
     *
     * @param issueGroups fixed issue groups
     * @param offset
     * @param limit
     * @return
     */
    List<IssueGroup> getOccurIssueGroupsByFixedIssueGroups(@Param("issueGroups") List<IssueGroup> issueGroups, @Param("offset") int offset, @Param("limit") int limit);

    IssueGroup getIssueGroupByIdAndDsrType(@Param("projectId") UUID projectId, @Param("issueGroupId") String issueGroupId, @Param("dsrType") List<String> dsrType);

    void assignIssueGroupToUser(
            @Param("issueGroupId") String issueGroupId,
            @Param("userId") UUID userId
    );

    int batchInsertIssueGroup(@Param("issueGroupList") List<IssueGroup> issueGroupList);

    int batchUpdateIssueGroup(@Param("projectId") UUID projectId, @Param("issueGroupList") List<IssueGroup> issueGroupList);

    int batchUpdateIssueGroupToFixed(@Param("projectId") UUID projectId, @Param("issueGroupList") List<IssueGroup> issueGroupList);

    int batchUpsertIssueGroupForLine(@Param("projectId") UUID projectId, @Param("issueGroupList") List<IssueGroup> issueGroupList);

    int batchUpsertIssueGroupForFixed(@Param("projectId") UUID projectId, @Param("issueGroupList") List<IssueGroup> issueGroupList);

    int deleteIssueGroupByProjectId(@Param("projectId") UUID projectId);

    int deleteFixedIssueGroupByAge(@Param("ageString") String ageString);

    List<IssueGroupCountRow> getIssueGroupCountWithFilter(
            @Param("filterCategory") String filterCategory,
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

    List<IssueGroupCountRow> getIssueGroupCriticalityCount(
            @Param("filterCategory") String filterCategory,
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

    List<IssueGroupCountRow> getTopCsvCodes(
            @Param("projectId") UUID projectId,
            @Param("scanTaskId") UUID scanTaskId,
            @Param("ruleCodes") List<String> ruleCodes,
            @Param("ruleSets") List<String> ruleSets,
            @Param("dsrType") String dsrType,
            @Param("top") Integer top
    );

    List<ReportFileStatisticRow> getReportFileStatisticRow(
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

    IssueGroup findIssueGroupStartWith(@Param("prefix") String prefix);

    int assignAllFromBaseline(@Param("currentScanTaskId") UUID currentScanTaskId, @Param("baselineScanTaskId") UUID baselineScanTaskId);
}
