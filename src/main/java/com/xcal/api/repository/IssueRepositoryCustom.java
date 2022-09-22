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

package com.xcal.api.repository;

import com.xcal.api.entity.Issue;
import com.xcal.api.entity.ScanFile;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.model.CompareIssueObject;
import com.xcal.api.model.payload.IssueSummaryResponse;
import com.xcal.api.util.VariableUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssueRepositoryCustom {

    List<Issue> findByScanTask(ScanTask scanTask);

    List<IssueSummaryResponse.AssignSummary> findIssueSummaryCountByUser(UUID scanTaskId);

    Page<Issue> searchIssueOnlyProjectFile(
            ScanTask scanTask,
            UUID ruleSetId,
            String ruleSetName,
            String seq,
            Map<VariableUtil.IssueAttributeName, List<String>> issueAttributeMap,
            Map<VariableUtil.RuleAttributeTypeName, List<String>> ruleAttributeMap,
            List<UUID> ruleInformationIds,
            List<ScanFile> scanFiles,
            Pageable pageable
    );

    Page<Issue> searchIssueOnlyNonProjectFile(
            ScanTask scanTask,
            UUID ruleSetId,
            String ruleSetName,
            String seq,
            Map<VariableUtil.IssueAttributeName, List<String>> issueAttributeMap,
            Map<VariableUtil.RuleAttributeTypeName, List<String>> ruleAttributeMap,
            List<UUID> ruleInformationIds,
            Pageable pageable
    );

    Page<Issue> searchIssue(
            ScanTask scanTask,
            UUID ruleSetId,
            String ruleSetName,
            String seq,
            Map<VariableUtil.IssueAttributeName, List<String>> issueAttributeMap,
            Map<VariableUtil.RuleAttributeTypeName, List<String>> ruleAttributeMap,
            List<UUID> ruleInformationIds,
            List<ScanFile> scanFiles,
            Pageable pageable
    );

    void assignCount(ScanTask scanTask);

    Optional<Issue> findByIdWithWholeObject(UUID uuid);

    List<CompareIssueObject> findCompareIssueObjectByScanTaskId(UUID scanTaskId);

    int updateIssueAssignToNullByUserId(UUID userId);
}
