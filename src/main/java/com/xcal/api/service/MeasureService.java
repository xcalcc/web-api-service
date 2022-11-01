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

package com.xcal.api.service;

import com.xcal.api.dao.IssueGroupDao;
import com.xcal.api.dao.ScanTaskDao;
import com.xcal.api.entity.*;
import com.xcal.api.entity.v3.IssueGroup;
import com.xcal.api.entity.v3.IssueGroupCountRow;
import com.xcal.api.exception.AppException;
import com.xcal.api.mapper.ProjectSummaryMapper;
import com.xcal.api.model.payload.SummaryResponse;
import com.xcal.api.model.payload.v3.ReportPDFResponse;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.repository.IssueRepository;
import com.xcal.api.repository.ScanFileRepository;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.StringUtil;
import com.xcal.api.util.VariableUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

import static com.xcal.api.service.IssueService.SEARCH_VAL_DELIMITER;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MeasureService {

    @NonNull IssueRepository issueRepository;
    @NonNull RuleService ruleService;
    @NonNull RuleStandardService ruleStandardService;
    @NonNull UserService userService;
    @NonNull ScanTaskDao scanTaskDao;
    @NonNull IssueGroupDao issueGroupDao;
    @NonNull ProjectSummaryMapper projectSummaryMapper;
    @NonNull ScanFileRepository scanFileRepository;

    public static void updateScanTaskSummary(ScanTask scanTask, Map<String, String> summary) {
        log.debug("[updateScanTaskSummary] scanTask, id: {}, summary, size: {}", scanTask.getId(), summary.size());

        if (scanTask.getSummary() != null) {
            scanTask.getSummary().putAll(summary);
        } else {
            scanTask.setSummary(summary);
        }
    }

    public static void updateScanSummary(ScanTask scanTask, String key, String value) {
        log.info("[updateScanSummary] scanTask, id: {}, key: {}, value: {}", scanTask.getId(), key, value);
        Map<String, String> summary = scanTask.getSummary();
        summary.put(key, value);
    }

    public static void updateScanSummaryFilesLines(ScanTask scanTask, List<ScanFile> scanFiles) {
        log.info("[updateScanSummaryFilesLines] scanTask, id: {}", scanTask.getId());

        List<FileInfo> fileInfos = scanFiles.stream().filter(sf -> sf.getFileInfo() != null).map(ScanFile::getFileInfo).distinct().collect(Collectors.toList());
        int totalOfLines = fileInfos.stream().mapToInt(FileInfo::getNoOfLines).sum();
        long fileCount = scanFiles.stream().filter(s -> s.getType() == ScanFile.Type.FILE).count();
        updateScanSummary(scanTask, "files", String.valueOf(fileCount));
        updateScanSummary(scanTask, "lines", String.valueOf(totalOfLines));
    }

    public static void updateScanSummaryWithIssueGroup(ScanTask scanTask, List<IssueGroup> issues) throws AppException {
        log.info("[updateScanSummary] issues size: {}", issues.size());
        Map<String, String> summary = new LinkedHashMap<>();

        long fileCount = NumberUtils.toLong(scanTask.getSummary().get("files"), 0);
        long totalOfLines = NumberUtils.toLong(scanTask.getSummary().get("lines"), 0);

        Map<String, List<IssueGroup>> issueMap = issues.stream().collect(Collectors.groupingBy(i -> i.getRuleSet()));
        List<String> ruleSetIds = issueMap.keySet().stream().collect(Collectors.toList());

        for (String ruleSetId : ruleSetIds) {
            summary.put("rule.id." + ruleSetId, "" + ruleSetId);
            summary.put("rule.version." + ruleSetId, "1");
            String prefix = ruleSetId + ".";
            updateScanSummaryBasicWithIssueGroup(summary, prefix, issueMap.get(ruleSetId), fileCount, totalOfLines);
        }
        updateScanSummaryBasicWithIssueGroup(summary, "", issues, fileCount, totalOfLines);
        updateScanTaskSummary(scanTask, summary);
    }

    public static void updateScanSummary(ScanTask scanTask, List<Issue> issues, List<RuleSet> ruleSets) throws AppException {
        log.info("[updateScanSummary] issues size: {}, ruleSets size: {}", issues.size(), ruleSets.size());
        Map<String, String> summary = new LinkedHashMap<>();

        long fileCount = NumberUtils.toLong(scanTask.getSummary().get("files"), 0);
        long totalOfLines = NumberUtils.toLong(scanTask.getSummary().get("lines"), 0);

        Map<RuleSet, List<Issue>> issueMap = issues.stream().collect(Collectors.groupingBy(i -> i.getRuleInformation().getRuleSet()));
        List<UUID> ruleSetIds = issueMap.keySet().stream().map(RuleSet::getId).collect(Collectors.toList());
        for (RuleSet ruleSet : ruleSets) {
            if (!ruleSetIds.contains(ruleSet.getId())) {
                issueMap.put(ruleSet, new ArrayList<>());
            }
        }
        for (RuleSet ruleSet : issueMap.keySet()) {
            summary.put("rule.version." + ruleSet.getName(), ruleSet.getVersion());
            summary.put("rule.id." + ruleSet.getName(), String.valueOf(ruleSet.getId()));
            String prefix = ruleSet.getName() + ".";
            updateScanSummaryBasic(summary, prefix, issueMap.get(ruleSet), fileCount, totalOfLines);
        }
        updateScanSummaryBasic(summary, "", issues, fileCount, totalOfLines);
        updateScanSummaryOwasp(summary, issues.stream().filter(i -> i.getRuleInformation().getFirstAttribute(VariableUtil.RuleAttributeTypeName.OWASP).isPresent()).collect(Collectors.toList()),
                fileCount, totalOfLines);
        updateScanTaskSummary(scanTask, summary);
    }


    private static void updateScanSummaryBasicWithIssueGroup(final Map<String, String> summary, String prefix, List<IssueGroup> issues, long fileCount, long totalOfLines) throws AppException {
        summary.put(prefix + "files", String.valueOf(fileCount));
        summary.put(prefix + "lines", String.valueOf(totalOfLines));
        int existingIssuesNumbers = Integer.parseInt(summary.getOrDefault(prefix + "issues", "0"));
        summary.put(prefix + "issues", String.valueOf(issues.size() + existingIssuesNumbers));

        Map<String, List<IssueGroup>> severityMap = issues.stream().collect(Collectors.groupingBy(IssueGroup::getSeverity));
        for (Map.Entry<String, List<IssueGroup>> entry : severityMap.entrySet()) {
            long count = entry.getValue().size();
            if (count > 0) {
                summary.put(prefix + "severity." + entry.getKey(), String.valueOf(count));
            }
        }

        Map<String, List<IssueGroup>> ruleCodeMap = issues.stream().collect(Collectors.groupingBy(i -> i.getRuleCode()));
        for (Map.Entry<String, List<IssueGroup>> ruleCodeEntry : ruleCodeMap.entrySet()) {
            long count = ruleCodeEntry.getValue().size();
            if (count > 0) {
                summary.put(prefix + "ruleCode." + ruleCodeEntry.getKey(), String.valueOf(count));
            }
        }
    }


    private static void updateScanSummaryBasic(final Map<String, String> summary, String prefix, List<Issue> issues, long fileCount, long totalOfLines) throws AppException {
        summary.put(prefix + "files", String.valueOf(fileCount));
        summary.put(prefix + "lines", String.valueOf(totalOfLines));
        int existingIssuesNumbers = Integer.parseInt(summary.getOrDefault(prefix + "issues", "0"));
        summary.put(prefix + "issues", String.valueOf(issues.size() + existingIssuesNumbers));

        Map<Issue.Severity, List<Issue>> severityMap = issues.stream().collect(Collectors.groupingBy(Issue::getSeverity));
        for (Issue.Severity severity : Issue.Severity.values()) {
            long count = severityMap.getOrDefault(severity, new ArrayList<>()).size();
            if (count > 0) {
                summary.put(prefix + "severity." + severity.name(), String.valueOf(count));
            }
        }
        Map<String, List<Issue>> ruleCodeMap = issues.stream().collect(Collectors.groupingBy(i -> i.getRuleInformation().getRuleCode()));
        for (Map.Entry<String, List<Issue>> ruleCodeEntry : ruleCodeMap.entrySet()) {
            long count = ruleCodeEntry.getValue().size();
            if (count > 0) {
                summary.put(prefix + "ruleCode." + ruleCodeEntry.getKey(), String.valueOf(count));
            }
        }
        for (RuleInformation.Certainty certainty : RuleInformation.Certainty.values()) {
            long count = issues.stream().filter(issue -> issue.getAttributes().stream().anyMatch(issueAttribute -> issueAttribute.getName() == VariableUtil.IssueAttributeName.CERTAINTY && StringUtils.equalsIgnoreCase(certainty.name(), issueAttribute.getValue()))).count();
            if (count > 0) {
                summary.put(prefix + "certainty." + certainty.name(), String.valueOf(count));
            }
        }
        for (RuleInformation.Priority priority : RuleInformation.Priority.values()) {
            long count = issues.stream().filter(issue -> issue.getAttributes().stream().anyMatch(issueAttribute -> issueAttribute.getName() == VariableUtil.IssueAttributeName.PRIORITY && StringUtils.equalsIgnoreCase(priority.name(), issueAttribute.getValue()))).count();
            if (count > 0) {
                summary.put(prefix + "priority." + priority.name(), String.valueOf(count));
            }
        }
        summary.put(prefix + "risk", String.valueOf(retrieveRiskLevel(issues, totalOfLines)));
        summary.put(prefix + "score", String.valueOf(calculatePriorityScore(issues)));

    }

    private static void updateScanSummaryOwasp(final Map<String, String> summary, List<Issue> issues, long fileCount, long totalOfLines) throws AppException {
        String prefix = "standard.OWASP.";
        summary.put(prefix + "files", String.valueOf(fileCount));
        summary.put(prefix + "lines", String.valueOf(totalOfLines));
        int existingIssuesNumbers = Integer.parseInt(summary.getOrDefault(prefix + "issues", "0"));
        summary.put(prefix + "issues", String.valueOf(issues.size() + existingIssuesNumbers));

        Map<Issue.Severity, List<Issue>> severityMap = issues.stream().collect(Collectors.groupingBy(Issue::getSeverity));
        for (Issue.Severity severity : Issue.Severity.values()) {
            long count = severityMap.getOrDefault(severity, new ArrayList<>()).size();
            if (count > 0) {
                summary.put(prefix + "severity." + severity.name(), String.valueOf(count));
            }
        }
        Map<String, List<Issue>> owaspCodeMap = new HashMap<>();
        for (Issue i : issues) {
            i.getRuleInformation().getAttributes(VariableUtil.RuleAttributeTypeName.OWASP)
                    .forEach(attr -> owaspCodeMap.computeIfAbsent(attr.getValue(), key -> new ArrayList<>()).add(i));
        }
        for (Map.Entry<String, List<Issue>> owaspCodeEntry : owaspCodeMap.entrySet()) {
            long count = owaspCodeEntry.getValue().size();
            if (count > 0) {
                summary.put(prefix + "code." + owaspCodeEntry.getKey(), String.valueOf(count));
            }
        }
        for (RuleInformation.Certainty certainty : RuleInformation.Certainty.values()) {
            long count = issues.stream().filter(issue -> issue.getAttributes().stream().anyMatch(issueAttribute -> issueAttribute.getName() == VariableUtil.IssueAttributeName.CERTAINTY && StringUtils.equalsIgnoreCase(certainty.name(), issueAttribute.getValue()))).count();
            if (count > 0) {
                summary.put(prefix + "certainty." + certainty.name(), String.valueOf(count));
            }
        }
        for (RuleInformation.Priority priority : RuleInformation.Priority.values()) {
            long count = issues.stream().filter(issue -> issue.getAttributes().stream().anyMatch(issueAttribute -> issueAttribute.getName() == VariableUtil.IssueAttributeName.PRIORITY && StringUtils.equalsIgnoreCase(priority.name(), issueAttribute.getValue()))).count();
            if (count > 0) {
                summary.put(prefix + "priority." + priority.name(), String.valueOf(count));
            }
        }
        summary.put(prefix + "risk", String.valueOf(retrieveRiskLevel(issues, totalOfLines)));
        summary.put(prefix + "score", String.valueOf(calculatePriorityScore(issues)));
    }

    /***
     * For retrieving scan summary from scan task object with filter
     * searchIssueGroupRequest.projectId is required
     * @param scanTask
     * @param searchIssueGroupRequest The filter
     * @return
     */
    public SummaryResponse retrieveScanSummary(ScanTask scanTask, SearchIssueGroupRequest searchIssueGroupRequest) {
        log.info("[retrieveScanSummary] scanTask, id: {}", scanTask.getId());
        return this.retrieveScanSummary(scanTask, scanTask, searchIssueGroupRequest);
    }

    /***
     * For retrieving scan summary from scan task object and latest scan task object with filter
     * searchIssueGroupRequest.projectId is required
     * @param scanTask
     * @param latestScanTask
     * @param searchIssueGroupRequest The filter
     * @return
     */
    public SummaryResponse retrieveScanSummary(ScanTask scanTask, ScanTask latestScanTask, SearchIssueGroupRequest searchIssueGroupRequest) {
        log.info("[retrieveScanSummary] scanTask, id: {}, latestScanTask, id: {}", scanTask.getId(), latestScanTask != null ? latestScanTask.getId() : null);

        Map<String, String> summary = scanTask.getSummary();
        String commitId = StringUtils.defaultIfEmpty(summary.get("diff.commitId"), scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID, null));
        String baselineCommitId = StringUtils.defaultIfEmpty(summary.get("diff.baselineCommitId"), scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID, null));
        String baselineScanTaskId = StringUtils.defaultIfEmpty(summary.get("diff.baselineScanTaskId"), scanTaskDao.getScanTaskIdFromProjectAndCommitId(scanTask.getProject().getId(), baselineCommitId, "COMPLETED").orElse(null));


        List<String> scanFilePaths = null;
        if ((searchIssueGroupRequest.getScanFileIds() != null) && !searchIssueGroupRequest.getScanFileIds().isEmpty()) {

            List<ScanFile> scanFiles = scanFileRepository.findAllById(searchIssueGroupRequest.getScanFileIds());
            if ((scanFiles != null) && !scanFiles.isEmpty()) {
                scanFilePaths = scanFiles.stream().map(ScanFile::getProjectRelativePath).collect(Collectors.toList());
            }
        }

        //compute and make certainty count
        Map<String, String> certaintyCountMap = getCertaintyCountMap(searchIssueGroupRequest, scanFilePaths);
        //compute and make criticality count
        //Get criticality count
        ReportPDFResponse.IssueCountGroupByCriticality groupByCriticality = getIssueCountGroupByCriticality(searchIssueGroupRequest, scanFilePaths);
        Integer totalDefectCount = groupByCriticality.getHigh() + groupByCriticality.getMedium() + groupByCriticality.getLow();


        Map<String, String> criticalityCountMap = new HashMap<>();
        criticalityCountMap.put("HIGH",Optional.of(groupByCriticality.getHigh()).orElse(0).toString());
        criticalityCountMap.put("MEDIUM",Optional.of(groupByCriticality.getMedium()).orElse(0).toString());
        criticalityCountMap.put("LOW",Optional.of(groupByCriticality.getLow()).orElse(0).toString());


        //compute and make rule code count
        Map<String, String> ruleCodeCountMap = getRuleCodeCountMap(searchIssueGroupRequest, scanFilePaths);


        String projectCriticality = getProjectCriticality(searchIssueGroupRequest, scanFilePaths);

        SummaryResponse.IssueSummary issueSummary = SummaryResponse.IssueSummary.builder()
                .fileCount(summary.get("files"))
                .lineCount(summary.get("lines"))
                .issuesCount(totalDefectCount.toString())
                .commitId(commitId)
                .baselineCommitId(baselineCommitId)
                .baselineScanTaskId(baselineScanTaskId)
                .criticality(projectCriticality)
                .ruleCodeCountMap(ruleCodeCountMap)
                .certaintyCountMap(certaintyCountMap)
                .criticalityCountMap(criticalityCountMap)
                .build();

        Map<String, SummaryResponse.FileInfo> fileInfoIdMap = new HashMap<>();
        for (Map.Entry<String, String> entry : summary.entrySet().stream().filter(entry ->
                StringUtils.startsWithIgnoreCase(entry.getKey(), "fileInfo.")).collect(Collectors.toList())) {
            String fileName = StringUtils.substringAfter(entry.getKey(), "fileInfo.id.");
            fileInfoIdMap.put(fileName, SummaryResponse.FileInfo.builder()
                    .id(UUID.fromString(entry.getValue()))
                    .name(fileName)
                    .build());
        }
        Project project = scanTask.getProject();
        Boolean isDsr = project.getNeedDsr() != null ? project.getNeedDsr() : false;

        SummaryResponse summaryResponse = SummaryResponse.builder()
                .projectUuid(scanTask.getProject().getId())
                .projectId(scanTask.getProject().getProjectId())
                .projectName(scanTask.getProject().getName())
                .scanTaskId(scanTask.getId())
                .commitId(commitId)
                .scanStartAt(scanTask.getScanStartAt() != null ? scanTask.getScanStartAt().getTime() : null)
                .scanEndAt(scanTask.getScanEndAt() != null ? scanTask.getScanEndAt().getTime() : null)
                .fileInfoMap(fileInfoIdMap)
                .status(scanTask.getStatus().name())
                .issueSummary(issueSummary)
                .hasDsr(isDsr)
                .build();

        //for every rule set
        for (Map.Entry<String, String> entry : summary.entrySet().stream().filter(entry ->
                StringUtils.startsWithIgnoreCase(entry.getKey(), "rule.version")).collect(Collectors.toList())) {
            String ruleSetName = StringUtils.substringAfter(entry.getKey(), "rule.version.");
            String ruleSetId = summary.get("rule.id." + ruleSetName);

            if(searchIssueGroupRequest.getRuleSets()!=null && !searchIssueGroupRequest.getRuleSets().contains(ruleSetId)){
                //skip this rule set
                continue;
            }

            String prefix = ruleSetName + ".";

            SearchIssueGroupRequest searchIssueGroupRequestForRuleSet = searchIssueGroupRequest.toBuilder()
                    .ruleSets(Arrays.asList(ruleSetId))
                    .build();

            //compute and make certainty count
            Map<String, String> certaintyCountMapForRuleSet = getCertaintyCountMap(searchIssueGroupRequestForRuleSet, scanFilePaths);
            //compute and make rule code count
            Map<String, String> ruleCodeCountMapForRuleSet = getRuleCodeCountMap(searchIssueGroupRequestForRuleSet, scanFilePaths);

            ReportPDFResponse.IssueCountGroupByCriticality groupByCriticalityForRuleSet = getIssueCountGroupByCriticality(searchIssueGroupRequestForRuleSet, scanFilePaths);
            Integer totalDefectCountForRuleSet = groupByCriticalityForRuleSet.getHigh() + groupByCriticalityForRuleSet.getMedium() + groupByCriticalityForRuleSet.getLow();

            SummaryResponse.IssueSummary issueSummaryRule = SummaryResponse.IssueSummary.builder()
                    .fileCount(summary.get(prefix + "files"))
                    .lineCount(summary.get(prefix + "lines"))
                    .issuesCount(totalDefectCountForRuleSet.toString())
                    .commitId(commitId)
                    .baselineCommitId(baselineCommitId)
                    .baselineScanTaskId(baselineScanTaskId)
                    .certaintyCountMap(certaintyCountMapForRuleSet)
                    .ruleCodeCountMap(ruleCodeCountMapForRuleSet)
                    .build();
            summaryResponse.getRuleSetSummaryMap().put(ruleSetName, SummaryResponse.RuleSet.builder()
                    .id(ruleSetId)
                    .name(ruleSetName)
                    .version(entry.getValue())
                    .issueSummary(issueSummaryRule)
                    .build());
        }

        // latestScanTask should not be null when only used by getLatestScanSummaryByProject.
        if (latestScanTask != null) {
            SummaryResponse.ScanTaskSummary latestCompleteScanTaskSummary = SummaryResponse.ScanTaskSummary.builder()
                    .scanTaskId(scanTask.getId())
                    .commitId(commitId)
                    .createdAt(scanTask.getCreatedOn() != null ? scanTask.getCreatedOn().getTime() : null)
                    .scanStartAt(scanTask.getScanStartAt() != null ? scanTask.getScanStartAt().getTime() : null)
                    .scanEndAt(scanTask.getScanEndAt() != null ? scanTask.getScanEndAt().getTime() : null)
                    .lastModifiedAt(scanTask.getModifiedOn() != null ? scanTask.getModifiedOn().getTime() : null)
                    .status(scanTask.getStatus().name())
                    .issueSummary(issueSummary)
                    .ruleSetSummaryMap(summaryResponse.getRuleSetSummaryMap())
                    .ruleStandardSummaryMap(summaryResponse.getRuleStandardSummaryMap())
                    .build();
            SummaryResponse.ScanTaskSummary latestScanTaskSummary;
            if (scanTask.getId().equals(latestScanTask.getId())) {
                //The project is not in scanning and latest scan is the latest completed scan
                latestScanTaskSummary = latestCompleteScanTaskSummary;
            } else {
                //The project is scanning or scan failed.
                latestScanTaskSummary = SummaryResponse.ScanTaskSummary.builder()
                        .scanTaskId(latestScanTask.getId())
                        .commitId(latestScanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID, null))
                        .createdAt(latestScanTask.getCreatedOn() != null ? latestScanTask.getCreatedOn().getTime() : null)
                        .scanStartAt(latestScanTask.getScanStartAt() != null ? latestScanTask.getScanStartAt().getTime() : null)
                        .scanEndAt(latestScanTask.getScanEndAt() != null ? latestScanTask.getScanEndAt().getTime() : null)
                        .lastModifiedAt(latestScanTask.getModifiedOn() != null ? latestScanTask.getModifiedOn().getTime() : null)
                        .status(latestScanTask.getStatus().name())
                        .build();
            }
            summaryResponse.setLatestCompleteScanTask(latestCompleteScanTaskSummary);
            summaryResponse.setLatestScanTask(latestScanTaskSummary);
        }

        return summaryResponse;
    }

    public String getProjectCriticality(SearchIssueGroupRequest searchIssueGroupRequest, List<String> scanFilePaths) {
        log.info("[getProjectCriticality] searchIssueGroupRequest: {} scanFilePaths.size:{}", searchIssueGroupRequest,scanFilePaths==null?0:scanFilePaths.size());
        String projectCriticality = projectSummaryMapper.getProjectRisk(searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePaths,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                null,
                StringUtil.splitAndTrim(searchIssueGroupRequest.getSearchValue(),SEARCH_VAL_DELIMITER));
        log.info("[getProjectCriticality] result projectCriticality:{}", projectCriticality);
        return projectCriticality;
    }

    public ReportPDFResponse.IssueCountGroupByCriticality getIssueCountGroupByCriticality(SearchIssueGroupRequest searchIssueGroupRequest, List<String> scanFilePaths) {
        log.info("[getIssueCountGroupByCriticality] searchIssueGroupRequest: {} scanFilePaths.size:{}", searchIssueGroupRequest,scanFilePaths==null?0:scanFilePaths.size());
        List<IssueGroupCountRow> groupByCriticalityList = issueGroupDao.getIssueGroupCriticalityCount(
                IssueGroupDao.FILTER_CATEGORY_CRITICALITY,
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePaths,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                null,
                searchIssueGroupRequest.getValidationAction(),
                searchIssueGroupRequest.getSearchValue());
        ReportPDFResponse.IssueCountGroupByCriticality groupByCriticality = ReportService.getIssueCountGroupByCriticality(groupByCriticalityList);
        log.info("[getIssueCountGroupByCriticality] result groupByCriticality:{}", groupByCriticality);
        return groupByCriticality;
    }

    public Map<String, String> getCertaintyCountMap(SearchIssueGroupRequest searchIssueGroupRequest, List<String> scanFilePaths) {
        log.info("[getCertaintyCountMap] searchIssueGroupRequest: {} scanFilePaths.size:{}", searchIssueGroupRequest,scanFilePaths==null?0:scanFilePaths.size());
        List<IssueGroupCountRow> issueGroupCertaintyCountRowList = this.issueGroupDao.getIssueGroupCountWithFilter(
                IssueGroupDao.FILTER_CATEGORY_CERTAINTY,
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePaths,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                null,
                searchIssueGroupRequest.getValidationAction(),
                searchIssueGroupRequest.getSearchValue()

        );

        Map<String, String> certaintyCountMap = issueGroupCertaintyCountRowList.stream()
                .collect(Collectors.toMap(IssueGroupCountRow::getCertainty, IssueGroupCountRow::getCount));
        log.info("[getCertaintyCountMap] result certaintyCountMap.size:{}", certaintyCountMap.size());
        return certaintyCountMap;
    }

    Map<String, String> getRuleCodeCountMap(SearchIssueGroupRequest searchIssueGroupRequest, List<String> scanFilePaths) {
        log.info("[getRuleCodeCountMap] searchIssueGroupRequest: {} scanFilePaths.size:{}", searchIssueGroupRequest,scanFilePaths==null?0:scanFilePaths.size());
        List<IssueGroupCountRow> issueGroupCountRowList = issueGroupDao.getIssueGroupCountWithFilter(
                IssueGroupDao.FILTER_CATEGORY_RULE_CODE,
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePaths,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                null,
                searchIssueGroupRequest.getValidationAction(),
                searchIssueGroupRequest.getSearchValue()
        );

        Map<String, String> ruleCodeCountMap = issueGroupCountRowList.stream()
                .collect(Collectors.toMap(IssueGroupCountRow::getRuleCode, IssueGroupCountRow::getCount));
        log.info("[getRuleCodeCountMap] result ruleCodeCountMap.size:{}", ruleCodeCountMap.size());
        return ruleCodeCountMap;
    }

    /**
     * Using the below formula to calculate risk level for xcalibyte engine
     * Project Risk Level (R)  =   ( Severity * Likelihood ) * sum of defects * sum of lines / 1,000
     *
     * @param issues       issue list
     * @param totalOfLines total number of lines
     * @return level in string
     */
    public static String xcalibyteRiskLevel(List<Issue> issues, long totalOfLines) {
        log.info("[xcalibyteRiskLevel] issues size: {}, totalOfLines: {}", issues.size(), totalOfLines);
        String result;
        double sumOfSeverity = 0.0;
        double sumOfLikelihood = 0.0;
        for (Issue issue : issues) {
            if (issue.getSeverity() != null) {
                switch (issue.getSeverity()) {
                    case HIGH:
                        sumOfSeverity = sumOfSeverity + 3;
                        break;
                    case MEDIUM:
                        sumOfSeverity = sumOfSeverity + 2;
                        break;
                    case LOW:
                        sumOfSeverity = sumOfSeverity + 1;
                        break;
                    default:
                        log.error("[xcalibyteRiskLevel] issue severity value is incorrect, value: {}", issue.getSeverity());
                        break;
                }
            } else {
                log.error("[xcalibyteRiskLevel] issue severity should not be null, issue, id: {}", issue.getId());
            }
            Optional<IssueAttribute> likelihoodOptional = issue.getFirstAttribute(VariableUtil.IssueAttributeName.LIKELIHOOD);
            RuleInformation.Likelihood likelihood = null;
            if (likelihoodOptional.isPresent() && EnumUtils.isValidEnumIgnoreCase(RuleInformation.Likelihood.class, likelihoodOptional.get().getValue())) {
                likelihood = EnumUtils.getEnum(RuleInformation.Likelihood.class, likelihoodOptional.get().getValue());
            }
            if (likelihood == null && issue.getRuleInformation().getLikelihood() != null) {
                likelihood = issue.getRuleInformation().getLikelihood();
            }
            if (likelihood != null) {
                switch (issue.getRuleInformation().getLikelihood()) {
                    case LIKELY:
                        sumOfLikelihood = sumOfLikelihood + 1;
                        break;
                    case PROBABLE:
                        sumOfLikelihood = sumOfLikelihood + 0.5;
                        break;
                    case UNLIKELY:
                        sumOfLikelihood = sumOfLikelihood + 0.01;
                        break;
                    default:
                        log.error("[xcalibyteRiskLevel] issue ruleInformation likelihood value is incorrect, value: {}", issue.getRuleInformation().getLikelihood());
                        break;
                }
            } else {
                log.error("[xcalibyteRiskLevel] issue ruleInformation likelihood should not be null, issue, id: {}", issue.getId());
            }
        }
        double rate = sumOfSeverity * sumOfLikelihood * issues.size() * totalOfLines * 0.001;
        log.info("[xcalibyteRiskLevel] sumOfSeverity: {}, sumOfLikelihood: {}, issues.size: {}, totalOfLines: {},  rate: {}",
                sumOfSeverity, sumOfLikelihood, issues.size(), totalOfLines, rate);

        if (rate > 90) {
            result = "HIGH";
        } else if (rate >= 45 && rate <= 90) {
            result = "MEDIUM";
        } else {
            result = "LOW";
        }

        return result;
    }

    /**
     * Using the below formula to calculate risk level for spotbugs and oclint engine
     * Project Risk Level (R) = Sum of defects / Sum of Lines
     * If R <= 0.32‰, Then, project risk is low.
     * If 0.32‰  < R < 11.95‰, Then project risk is Medium
     * If R >= 11.95‰,  then project risk is high
     *
     * @param issues       issue list
     * @param totalOfLines total number of lines
     * @return level in string
     */
    public static String otherRiskLevel(List<Issue> issues, long totalOfLines) {
        log.info("[otherRiskLevel] issues size: {}, totalOfLines: {}", issues.size(), totalOfLines);
        double issueNumber = issues.size();
        String result = "LOW";
        if (totalOfLines > 0) {
            double resultValue = issueNumber / totalOfLines;
            if (resultValue <= 0.32 * 0.001) {
                result = "LOW";
            } else if (resultValue < 11.95 * 0.001) {
                result = "MEDIUM";
            } else {
                result = "HIGH";
            }
        } else if (totalOfLines == 0) {
            if (issueNumber == 0) {
                result = "LOW";
            } else if (issueNumber > 0) {
                result = "HIGH";
            }
        }
        return result;
    }

    public static double calculatePriorityScore(List<Issue> issues) {
        log.info("[calculatePriorityScore] issues size: {}", issues.size());
        // Add up all first priority found in attribute, normally should be only 1, 0 if not found
        int totalPriority = issues.stream().mapToInt(i -> i.getRuleInformation().getFirstAttribute(VariableUtil.RuleAttributeTypeName.PRIORITY).map(attr -> NumberUtils.toInt(attr.getValue(), 0)).orElse(0)).sum();
        // multiple total with 1.0 to get double data type, avoid java truncate in int division
        log.debug("[calculatePriorityScore] totalPriority: {}", totalPriority);
        double averagePriority = totalPriority * 1.0 / issues.size();
        log.debug("[calculatePriorityScore] averagePriority: {}", averagePriority);
        // transfer the average priority 1-27 -> 0-26 -> 0.0-1.0 -> 0.0-9.0 -> 1.0-10.0
        double result = ((averagePriority - 1) / 26.0 * 9.0) + 1.0;
        log.info("[calculatePriorityScore] issues size: {}, priority score: {}", issues.size(), result);
        return result;
    }

    public static String retrieveRiskLevel(List<Issue> issues, long totalOfLines) throws AppException {
        log.info("[retrieveRiskLevel] issues size: {}, totalOfLines: {}", issues.size(), totalOfLines);
        String result;
        long highSeverityCount = issues.stream().filter(i -> Issue.Action.CRITICAL == i.getAction()).count();
        if (highSeverityCount > 0) {
            result = "HIGH";
        } else {
            if (!issues.isEmpty()) {
                Issue issue = issues.get(0);
                String engineName = issue.getRuleInformation().getRuleSet().getScanEngine().getName();

                log.info("[retrieveRiskLevel] engineName: {}", engineName);

                if (engineName.toLowerCase().contains(ScanEngine.EngineType.XCALIBYTE.toString().toLowerCase())) {
                    result = xcalibyteRiskLevel(issues, totalOfLines);
                } else if (engineName.toLowerCase().contains(ScanEngine.EngineType.SPOTBUGS.toString().toLowerCase()) ||
                        engineName.toLowerCase().contains(ScanEngine.EngineType.OCLINT.toString().toLowerCase())) {
                    result = otherRiskLevel(issues, totalOfLines);
                } else {
                    throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_RULE_INVALID_SCAN_ENGINE.unifyErrorCode,
                            CommonUtil.formatString("[{}] engineName: {}", AppException.ErrorCode.E_API_RULE_INVALID_SCAN_ENGINE.messageTemplate, engineName));
                }
            } else {
                result = "LOW";
            }
        }
        return result;
    }

    public static void updateProjectSummary(Project project, ScanTask scanTask) {
        if ((project != null) && (scanTask != null)) {
            if (scanTask.getScanStartAt() != null) {
                project.getSummary().put("scanStartAt", String.format("%d", scanTask.getScanStartAt().getTime()));
            }

            List<String> keyNames = Arrays.asList(
                    "files",
                    "lines",
                    "issues",
                    "priority.HIGH",
                    "priority.MEDIUM",
                    "priority.LOW"
            );

            for (String keyName : keyNames) {
                if (scanTask.getSummary().containsKey(keyName)) {
                    project.getSummary().put(String.format("ALL.%s", keyName), scanTask.getSummary().get(keyName));
                }
            }

            for (Map.Entry<String, String> entry : scanTask.getSummary().entrySet().stream()
                    .filter(e -> StringUtils.startsWithIgnoreCase(e.getKey(), "rule.version."))
                    .collect(Collectors.toList())) {
                String ruleSetName = StringUtils.substringAfter(entry.getKey(), "rule.version.");
                for (String keyName : keyNames) {
                    String key = String.format("%s.%s", ruleSetName, keyName);
                    if (scanTask.getSummary().containsKey(key)) {
                        project.getSummary().put(key, scanTask.getSummary().get(key));
                    }
                }
            }
        }
    }

}
