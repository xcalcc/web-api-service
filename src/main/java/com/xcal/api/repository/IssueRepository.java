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

import com.xcal.api.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID>, JpaSpecificationExecutor<Issue>, IssueRepositoryCustom {

    List<Issue> findByScanTaskProject(Project project);

    List<Issue> findByScanTask(ScanTask scanTask);

    List<Issue> findByScanTaskAndScanFileIn(ScanTask scanTask, List<ScanFile> scanFiles);

    Long countByScanTaskAndActionIn(ScanTask scanTask, List<Issue.Action> actionList);

    Long countByScanTaskAndRuleInformationIn(ScanTask scanTask, List<RuleInformation> ruleInformationList);

    Long countByScanTaskAndRuleInformationRuleSetAndActionIn(ScanTask scanTask, RuleSet ruleSet, List<Issue.Action> actionList);

    Long countByScanTaskAndRuleInformationRuleSetAndRuleInformationPriorityIn(ScanTask scanTask, RuleSet ruleSet, List<RuleInformation.Priority> priorityList);

    Long countByScanTaskAndRuleInformationRuleSetAndRuleInformationPriorityAndAction(ScanTask scanTask, RuleSet ruleSet, RuleInformation.Priority priority, Issue.Action action);

    Long countByScanTaskAndAssignTo(ScanTask scanTask, User user);

    Long countByScanTaskProjectAndAssignTo(Project project, User user);

    List<Issue> findByIdIn(List<UUID> uuids);

    Long countByScanTaskAndRuleInformationInAndActionIn(ScanTask scanTask, List<RuleInformation> ruleInformation, List<Issue.Action> asList);

    Optional<Issue> findByScanTaskAndIssueKey(ScanTask scanTask, String issueKey);

}
