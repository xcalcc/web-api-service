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

package com.xcal.api.service.v3;

import com.xcal.api.dao.ProjectDao;
import com.xcal.api.dao.ScanTaskDao;
import com.xcal.api.entity.Issue;
import com.xcal.api.entity.Project;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.entity.ScanTask_;
import com.xcal.api.entity.v3.ProjectSummary;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.v3.ProjectSummaryDto;
import com.xcal.api.repository.ProjectRepository;
import com.xcal.api.repository.ScanTaskRepository;
import com.xcal.api.repository.v3.ProjectSummaryRepository;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.OrchestrationService;
import com.xcal.api.service.UserService;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.VariableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProjectServiceV3 {

    private final UserService userService;

    private final OrchestrationService orchestrationService;

    private final ProjectRepository projectRepository;

    private final ProjectDao projectDao;

    private final ProjectSummaryRepository projectSummaryRepository;

    private final ScanTaskRepository scanTaskRepository;

    private final ScanTaskDao scanTaskDao;

    @Value("${app.scan.csf-file-name}")
    public String csfFileName;

    @Value("${app.scan.volume.path}")
    public String scanVolumePath;

    @Value("${app.scan.scan-result-folder-name}")
    public  String scanResultFolderName;

    public void deleteProject(UUID id, UserPrincipal userPrincipal) throws AppException {
        log.info(
                "[deleteProject] project, id: {}, principal username: {}",
                id, userPrincipal.getUsername()
        );

        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                                id
                        )
                ));

        this.userService.checkAccessRightOrElseThrow(
                project,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] projectId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                project.getId()
                        )
                )
        );

        this.orchestrationService.deleteAllFilesRelatedToProject(project);
        this.projectDao.deleteProject(id);
    }

    public Page<ProjectSummaryDto> getProjectSummaryList(Pageable pageable, UserPrincipal userPrincipal) throws AppException {
        log.info("[getProjectSummaryList] principal username: {}", userPrincipal.getUsername());
        Page<ProjectSummary> summaries = UserService.isAdmin(userPrincipal.getUser()) ?
                this.projectSummaryRepository.findAll(pageable) :
                this.projectSummaryRepository.findByCreatedByOrderByModifiedOnDesc(userPrincipal.getUsername(), pageable);
        return new PageImpl<>(
                summaries.getContent().stream().map(summary -> convertToV3(summary)).collect(Collectors.toList()),
                summaries.getPageable(),
                summaries.getTotalElements()
        );
    }

    public ProjectSummaryDto getProjectSummary(UUID projectUUID) throws AppException {
        Optional<ProjectSummary> projectSummary = this.projectSummaryRepository.findById(projectUUID);
        if (projectSummary.isPresent()) {
            return convertToV3(projectSummary.get());
        } else {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_DATA_NOT_FOUND,
                    HttpURLConnection.HTTP_NOT_FOUND,
                    AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] projectId: {}",
                            AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                            projectUUID
                    ));
        }
    }

    public ProjectSummaryDto convertToV3(ProjectSummary projectSummary) {
        if (projectSummary == null) {
            return null;
        }

        Map<String, String> summary = projectSummary.getSummary();
        Map<String, ProjectSummaryDto.RuleSet> ruleSetSummaryMap = new HashMap<>();

        //For all data
        {
            ProjectSummaryDto.RuleSet ruleSet = getRuleSet(summary, "");
            ruleSetSummaryMap.put("all", ruleSet);
        }

        //for each rule set
        for (Map.Entry<String, String> entry : summary.entrySet().stream().filter(entry ->
                StringUtils.startsWithIgnoreCase(entry.getKey(), "rule.version")).collect(Collectors.toList())) {
            String ruleSetName = StringUtils.substringAfter(entry.getKey(), "rule.version.");
            String prefix = ruleSetName + ".";

            ProjectSummaryDto.RuleSet ruleSet = getRuleSet(summary, prefix);

            ruleSetSummaryMap.put(ruleSetName, ruleSet);

        }

        Map<String, String> baselineRuleSetCountMap = getBaselineRuleSetCountMap(summary);

        int baselineIssueCount = 0;
        for (Map.Entry<String, String> entry: baselineRuleSetCountMap.entrySet()){
            baselineIssueCount+=Integer.valueOf(entry.getValue());
        }

            ProjectSummaryDto.Summary projectSummaryObj = ProjectSummaryDto.Summary.builder()
                .langList(summary.get("lang"))
                .ruleSetSummaryMap(ruleSetSummaryMap)
                .scanTaskId(summary.get("scanTaskId"))
                .fileCount(summary.get("files") == null ? null : Integer.parseInt(summary.get("files")))
                .lineCount(summary.get("lines") == null ? null : Integer.parseInt(summary.get("lines")))
                .issueCount(summary.get("issues") == null ? null : Integer.parseInt(summary.get("issues")))
                .baselineRuleSetCount(baselineRuleSetCountMap)
                .baselineIssueCount(baselineIssueCount)
                .risk(summary.get("criticality"))
                .scanStartAt(summary.get("scanStartAt"))
                .scanEndAt(summary.get("scanEndAt"))
                .build();

        Project projectForFilter = Project.builder().id(projectSummary.getId()).build();

        //get last scan status
        Optional<ScanTask> lastScanTaskOptional = scanTaskRepository.findFirst1ByProject(projectForFilter, Sort.by(Sort.Order.desc(ScanTask_.MODIFIED_ON)));
        ProjectSummaryDto.ScanTask lastScanTask = ProjectSummaryDto.ScanTask.builder().build();
        String lastScanStatus = "FAILED";
        if (lastScanTaskOptional.isPresent()) {
            ScanTask lastScanTaskEntity = lastScanTaskOptional.get();

            BeanUtils.copyProperties(lastScanTaskEntity, lastScanTask);

            lastScanStatus = lastScanTaskEntity.getStatus().toString();
        }

        boolean isDsr = projectSummary.getNeedDsr() != null ? projectSummary.getNeedDsr() : false;

        return ProjectSummaryDto.builder()
                .id(projectSummary.getId())
                .projectId(projectSummary.getProjectId())
                .name(projectSummary.getName())
                .status(projectSummary.getStatus())
                .createdBy(projectSummary.getCreatedBy())
                .createdOn(projectSummary.getCreatedOn())
                .modifiedBy(projectSummary.getModifiedBy())
                .modifiedOn(projectSummary.getModifiedOn())
                .summary(projectSummaryObj)
                .hasDsr(isDsr)
                .scanMode(projectSummary.getScanMode())
                .lastScanStatus(lastScanStatus)
                .lastScanTask(lastScanTask)
                .build();

    }

    Map<String, String> getBaselineRuleSetCountMap(Map<String, String> summary) {
        Map<String, String> baselineRuleSetCountMap = new HashMap<>();
        for (Map.Entry<String, String> entry : summary.entrySet().stream().filter(entry ->
                StringUtils.startsWithIgnoreCase(entry.getKey(), "baseline.") && StringUtils.substringAfter(entry.getKey(), "baseline.").endsWith(".issues")).collect(Collectors.toList())) {
            String ruleSetName = StringUtils.substringBefore(StringUtils.substringAfter(entry.getKey(), "baseline."),".issues");
            baselineRuleSetCountMap.put(ruleSetName, entry.getValue());
        }
        return baselineRuleSetCountMap;
    }

    private ProjectSummaryDto.RuleSet getRuleSet(Map<String, String> summary, String prefix) {
        Map<String, String> criticalityCountMapRule = new HashMap<>();
        for (Issue.Criticality criticality : Issue.Criticality.values()) {
            if (summary.containsKey(prefix + "criticality." + criticality.name())) {
                criticalityCountMapRule.put(criticality.name(), summary.get(prefix + "criticality." + criticality.name()));
            }
        }

        String issueCount = summary.get(prefix + "issues");

        ProjectSummaryDto.RuleSet ruleSet = ProjectSummaryDto.RuleSet.builder()
                .criticality(criticalityCountMapRule)
                .issuesCount(issueCount)
                .build();
        return ruleSet;
    }


    /***
     * Check if the project has to perform dsr.
     * If either one of the scan task is null or don't have commit id, return false.
     * Otherwise, returntrue.
     * @param scanTask
     * @return
     */

    public static boolean isScanTaskDsr(ScanTask scanTask) {
        if (scanTask == null) { //if no list provided, must not be dsr project
            return false;
        }

        String commitId = scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID, null);
        String baselineCommitId = scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID, null);

        if (commitId == null || baselineCommitId == null) {
            return false;
        }

        return true;
    }

    public boolean getFirstTimeScan(UUID projectId, UserPrincipal userPrincipal) {
        log.debug("[getFirstTimeScan] projectId: {}", projectId);
        Optional<com.xcal.api.entity.v3.ScanTask> scanTaskOptional = scanTaskDao.getLastScanTaskByProjectId(projectId,ScanTask.Status.COMPLETED.name());
        boolean isFirstTimeScan = !scanTaskOptional.isPresent();
        log.debug("[getFirstTimeScan] isFirstTimeScan: {}", isFirstTimeScan);
        return isFirstTimeScan;
    }
}
