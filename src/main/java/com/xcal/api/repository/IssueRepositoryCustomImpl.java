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
import com.xcal.api.model.CompareIssueObject;
import com.xcal.api.model.payload.IssueSummaryResponse;
import com.xcal.api.util.VariableUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IssueRepositoryCustomImpl extends RepositoryCustom implements IssueRepositoryCustom {

    @NonNull EntityManager em;

    @Override
    public List<Issue> findByScanTask(ScanTask scanTask) {
        log.info("[findByScanTask] scanTaskId: {}", scanTask.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Issue> q = cb.createQuery(Issue.class);
        Root<Issue> issueRoot = q.from(Issue.class);

        issueRoot.<Issue, RuleInformation>fetch(Issue_.RULE_INFORMATION, JoinType.LEFT).fetch(RuleInformation_.RULE_SET, JoinType.LEFT);
        issueRoot.<Issue, ScanFile>fetch(Issue_.SCAN_FILE, JoinType.LEFT).fetch(ScanFile_.FILE_INFO, JoinType.LEFT);
        issueRoot.<Issue, User>fetch(Issue_.ASSIGN_TO, JoinType.LEFT);

        q.where(cb.equal(issueRoot.get(Issue_.SCAN_TASK), scanTask)).distinct(true);
        return em.createQuery(q).getResultList();
    }

    @Override
    public List<IssueSummaryResponse.AssignSummary> findIssueSummaryCountByUser(UUID scanTaskId) {
        log.info("[findIssueSummaryCountByUser] scanTaskId: {}", scanTaskId);
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
        Root<Issue> issueRoot = query.from(Issue.class);
        query.multiselect(issueRoot.get(Issue_.ASSIGN_TO).get(User_.ID), issueRoot.get(Issue_.ASSIGN_TO).get(User_.EMAIL), issueRoot.get(Issue_.ASSIGN_TO).get(User_.DISPLAY_NAME), criteriaBuilder.count(issueRoot.get(Issue_.ASSIGN_TO)));
        query.where(criteriaBuilder.equal(issueRoot.get(Issue_.SCAN_TASK).get(ScanTask_.ID), scanTaskId));
        query.groupBy(issueRoot.get(Issue_.ASSIGN_TO), issueRoot.get(Issue_.ASSIGN_TO).get(User_.EMAIL), issueRoot.get(Issue_.ASSIGN_TO).get(User_.DISPLAY_NAME));
        query.orderBy(criteriaBuilder.desc(criteriaBuilder.count(issueRoot.get(Issue_.ASSIGN_TO))));
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        List<Object[]> resultList = typedQuery.getResultList();
        return resultList.stream().map(result -> IssueSummaryResponse.AssignSummary.builder()
                .id((UUID) result[0])
                .email((String) result[1])
                .displayName((String) result[2])
                .count((Long) result[3]).build())
                .collect(Collectors.toList());
    }

    @Override
    public Page<Issue> searchIssueOnlyProjectFile(
            ScanTask scanTask,
            UUID ruleSetId,
            String ruleSetName,
            String seq,
            Map<VariableUtil.IssueAttributeName, List<String>> issueAttributeMap,
            Map<VariableUtil.RuleAttributeTypeName, List<String>> ruleAttributeMap,
            List<UUID> ruleInformationIds,
            List<ScanFile> scanFiles,
            Pageable pageable
    ) {
        return this.searchIssue(
                scanTask,
                ruleSetId,
                ruleSetName,
                seq,
                issueAttributeMap,
                ruleAttributeMap,
                ruleInformationIds,
                scanFiles,
                true,
                false,
                pageable
        );
    }

    @Override
    public Page<Issue> searchIssueOnlyNonProjectFile(
            ScanTask scanTask,
            UUID ruleSetId,
            String ruleSetName,
            String seq,
            Map<VariableUtil.IssueAttributeName, List<String>> issueAttributeMap,
            Map<VariableUtil.RuleAttributeTypeName, List<String>> ruleAttributeMap,
            List<UUID> ruleInformationIds,
            Pageable pageable
    ) {
        return this.searchIssue(
                scanTask,
                ruleSetId,
                ruleSetName,
                seq,
                issueAttributeMap,
                ruleAttributeMap,
                ruleInformationIds,
                new ArrayList<>(),
                false,
                true,
                pageable
        );
    }

    @Override
    public Page<Issue> searchIssue(
            ScanTask scanTask,
            UUID ruleSetId,
            String ruleSetName,
            String seq,
            Map<VariableUtil.IssueAttributeName, List<String>> issueAttributeMap,
            Map<VariableUtil.RuleAttributeTypeName, List<String>> ruleAttributeMap,
            List<UUID> ruleInformationIds,
            List<ScanFile> scanFiles,
            Pageable pageable
    ) {
        return this.searchIssue(
                scanTask,
                ruleSetId,
                ruleSetName,
                seq,
                issueAttributeMap,
                ruleAttributeMap,
                ruleInformationIds,
                scanFiles,
                true,
                true,
                pageable
        );
    }

    private Page<Issue> searchIssue(
            ScanTask scanTask,
            UUID ruleSetId,
            String ruleSetName,
            String seq,
            Map<VariableUtil.IssueAttributeName, List<String>> issueAttributeMap,
            Map<VariableUtil.RuleAttributeTypeName, List<String>> ruleAttributeMap,
            List<UUID> ruleInformationIds,
            List<ScanFile> scanFiles,
            boolean includeProjectFile,
            boolean includeNonProjectFile,
            Pageable pageable
    ) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Issue> countIssueRoot = countQuery.from(Issue.class);
        Expression<Boolean> whereClause = this.createSearchIssueWhereClause(
                cb,
                countQuery,
                countIssueRoot,
                scanTask,
                ruleSetId,
                ruleSetName,
                seq,
                issueAttributeMap,
                ruleAttributeMap,
                ruleInformationIds,
                scanFiles,
                includeProjectFile,
                includeNonProjectFile
        );
        countQuery.select(cb.count(countIssueRoot)).where(whereClause);
        Long count = this.em.createQuery(countQuery).getSingleResult();

        // Query issue entity and sorting in pageable, this query will create issue object without related entity, so we need another query to fetch them
        // If fetch related entity with collection and with sorting or paging at the same time (in here is IssueAttribute),
        // all entry will fetch out into memory and then do the sorting and paging by JPA
        // Since have Sorting, cannot only fetch ID only, need sort by various attributes.
        CriteriaQuery<Issue> issueQuery = cb.createQuery(Issue.class);
        Root<Issue> issueRoot = issueQuery.from(Issue.class);
        issueRoot.<Issue, RuleInformation>fetch(Issue_.RULE_INFORMATION, JoinType.LEFT).fetch(RuleInformation_.RULE_SET, JoinType.LEFT).fetch(RuleSet_.SCAN_ENGINE, JoinType.LEFT);
        issueRoot.<Issue, ScanFile>fetch(Issue_.SCAN_FILE, JoinType.LEFT);
        issueRoot.<Issue, User>fetch(Issue_.ASSIGN_TO, JoinType.LEFT);
        issueQuery.where(whereClause).distinct(true).orderBy(toOrders(cb, issueRoot, pageable.getSort()));
        List<Issue> issueList = this.em.createQuery(issueQuery)
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        //Query issue entity with relation entities by sorting
        CriteriaQuery<Issue> issueQueryWithRelationEntity = cb.createQuery(Issue.class);
        Root<Issue> issueRootFetchWithRelationEntity = issueQueryWithRelationEntity.from(Issue.class);
        issueRootFetchWithRelationEntity.<Issue, RuleInformation>fetch(Issue_.RULE_INFORMATION, JoinType.LEFT).fetch(RuleInformation_.RULE_SET, JoinType.LEFT).fetch(RuleSet_.SCAN_ENGINE, JoinType.LEFT);
        issueRootFetchWithRelationEntity.<Issue, ScanFile>fetch(Issue_.SCAN_FILE, JoinType.LEFT).<ScanFile, FileInfo>fetch(ScanFile_.FILE_INFO, JoinType.LEFT);
        issueRootFetchWithRelationEntity.<Issue, User>fetch(Issue_.ASSIGN_TO, JoinType.LEFT);
        issueRootFetchWithRelationEntity.<Issue, IssueAttribute>fetch(Issue_.ATTRIBUTES, JoinType.LEFT);
        if (!issueList.isEmpty()) {
            whereClause = cb.and(whereClause, issueRootFetchWithRelationEntity.get(Issue_.ID).in(issueList.stream().map(Issue::getId).collect(Collectors.toList())));
        }
        issueQueryWithRelationEntity.where(whereClause).distinct(true).orderBy(toOrders(cb, issueRootFetchWithRelationEntity, pageable.getSort()));
        List<Issue> result = this.em.createQuery(issueQueryWithRelationEntity).getResultList();

        return new PageImpl<>(result, pageable, count);
    }

    private Expression<Boolean> createSearchIssueWhereClause(
            CriteriaBuilder cb,
            CriteriaQuery<Long> countQuery,
            Root<Issue> countIssueRoot,
            ScanTask scanTask,
            UUID ruleSetId,
            String ruleSetName,
            String seq,
            Map<VariableUtil.IssueAttributeName, List<String>> issueAttributeMap,
            Map<VariableUtil.RuleAttributeTypeName, List<String>> ruleAttributeMap,
            List<UUID> ruleInformationIds,
            List<ScanFile> scanFiles,
            boolean includeProjectFile,
            boolean includeNonProjectFile
    ) {
        Expression<Boolean> whereClause = cb.equal(countIssueRoot.get(Issue_.SCAN_TASK), scanTask);

        // where rule set id or rule set name
        if (ruleSetId != null) {
            whereClause = cb.and(whereClause, cb.equal(countIssueRoot.get(Issue_.RULE_INFORMATION).get(RuleInformation_.RULE_SET).get(RuleSet_.ID), ruleSetId));
        } else if (!StringUtils.isEmpty(ruleSetName)) {
            whereClause = cb.and(whereClause, cb.equal(countIssueRoot.get(Issue_.RULE_INFORMATION).get(RuleInformation_.RULE_SET).get(RuleSet_.NAME), ruleSetName));
        }

        // where rule information ids in
        if (!ruleInformationIds.isEmpty()) {
            whereClause = cb.and(whereClause, countIssueRoot.get(Issue_.RULE_INFORMATION).get(RuleInformation_.ID).in(ruleInformationIds));
        }

        // where seq, which in web UI is ID that display to user
        if (!StringUtils.isEmpty(seq)) {
            whereClause = cb.and(whereClause, cb.like(countIssueRoot.get(Issue_.SEQ), StringUtils.join('%', seq, '%')));
        }

        // where the issue belong to scan file
        Predicate projectFileClause = null;
        Predicate nonProjectFileClause = countIssueRoot.get(Issue_.SCAN_FILE).isNull();
        Predicate scanFileClause = null;

        if (!scanFiles.isEmpty()) {
            List<Predicate> scanFilePredicates = new ArrayList<>();
            for (ScanFile scanFile : scanFiles) {
                scanFilePredicates.add(cb.and(
                        cb.ge(countIssueRoot.get(Issue_.SCAN_FILE).get(ScanFile_.TREE_LEFT), scanFile.getTreeLeft()),
                        cb.le(countIssueRoot.get(Issue_.SCAN_FILE).get(ScanFile_.TREE_RIGHT), scanFile.getTreeRight())
                ));
            }
            projectFileClause = cb.or(scanFilePredicates.toArray(new Predicate[0]));
        }

        if (includeProjectFile && includeNonProjectFile) {
            if (projectFileClause != null) {
                scanFileClause = cb.or(projectFileClause, nonProjectFileClause);
            }
        } else if (includeProjectFile) { // includeProjectFile: true, includeNonProjectFile: false
            if (projectFileClause != null) {
                scanFileClause = projectFileClause;
            } else {
                scanFileClause = countIssueRoot.get(Issue_.SCAN_FILE).isNotNull();
            }
        } else { // includeProjectFile: false, includeNonProjectFile: true
            scanFileClause = nonProjectFileClause;
        }

        if (scanFileClause != null) {
            whereClause = cb.and(whereClause, scanFileClause);
        }

        // subquery to get issue have attributes
        for (Map.Entry<VariableUtil.IssueAttributeName, List<String>> issueAttributeNameListEntry : issueAttributeMap.entrySet()) {
            if (issueAttributeNameListEntry.getValue().size() > 0) {
                Subquery<UUID> sub = countQuery.subquery(UUID.class);
                Root<Issue> subIssueRoot = sub.from(Issue.class);
                ListJoin<Issue, IssueAttribute> subAttribute = subIssueRoot.joinList(Issue_.ATTRIBUTES);
                sub.select(subIssueRoot.get(Issue_.ID));
                sub.where(cb.and(
                        cb.equal(subIssueRoot.get(Issue_.SCAN_TASK), scanTask),
                        cb.equal(subAttribute.get(IssueAttribute_.NAME), issueAttributeNameListEntry.getKey()),
                        subAttribute.get(IssueAttribute_.VALUE).in(issueAttributeNameListEntry.getValue())
                ));
                whereClause = cb.and(whereClause, countIssueRoot.get(Issue_.ID).in(sub));
            }
        }

        // subquery to get rule information have attributes
        for (Map.Entry<VariableUtil.RuleAttributeTypeName, List<String>> ruleAttributeTypeNameListEntry : ruleAttributeMap.entrySet()) {
            if (ruleAttributeTypeNameListEntry.getValue().size() > 0) {
                Subquery<UUID> sub = countQuery.subquery(UUID.class);
                Root<RuleInformation> subRuleInformationRoot = sub.from(RuleInformation.class);
                ListJoin<RuleInformation, RuleInformationAttribute> subAttribute = subRuleInformationRoot.joinList(RuleInformation_.ATTRIBUTES);
                sub.select(subRuleInformationRoot.get(RuleInformation_.ID));
                sub.where(cb.and(
                        cb.equal(subRuleInformationRoot.get(RuleInformationAttribute_.TYPE), ruleAttributeTypeNameListEntry.getKey().type),
                        cb.equal(subRuleInformationRoot.get(RuleInformationAttribute_.NAME), ruleAttributeTypeNameListEntry.getKey().nameValue),
                        subAttribute.get(RuleInformationAttribute_.VALUE).in(ruleAttributeTypeNameListEntry.getValue())
                ));
                whereClause = cb.and(whereClause, countIssueRoot.get(Issue_.RULE_INFORMATION).get(RuleInformation_.ID).in(sub));
            }
        }

        return whereClause;
    }

    @Override
    public void assignCount(ScanTask scanTask) {
        log.info("[assignCount] scanTask: {}", scanTask.getId());
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteria = criteriaBuilder.createQuery( Tuple.class );
        Root<Issue> root = criteria.from( Issue.class );
        criteria.groupBy(root.get(Issue_.ASSIGN_TO));
        criteria.multiselect(root.get(Issue_.ASSIGN_TO).get(User_.ID), criteriaBuilder.count(root));
        criteria.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(Issue_.SCAN_TASK), scanTask), criteriaBuilder.isNotNull(root.get(Issue_.ASSIGN_TO))));
        TypedQuery<Tuple> query = em.createQuery(criteria);
        List<Tuple> tuples = query.getResultList();
        for(Tuple tuple: tuples){
            log.info("id :{}", tuple.get(0));
            log.info("count :{}", tuple.get(1));
        }
    }

    @Override
    public Optional<Issue> findByIdWithWholeObject(UUID uuid) {
        log.info("[findByIdWithWholeObject] uuid: {}", uuid);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Issue> q = cb.createQuery(Issue.class);
        Root<Issue> issueRoot = q.from(Issue.class);

        Fetch<Issue, RuleInformation> ruleInformationFetch = issueRoot.<Issue, RuleInformation>fetch(Issue_.RULE_INFORMATION, JoinType.LEFT);
        ruleInformationFetch.<RuleInformation, RuleSet>fetch(RuleInformation_.RULE_SET, JoinType.LEFT);
        ruleInformationFetch.<RuleInformation, RuleInformationAttribute>fetch(RuleInformation_.ATTRIBUTES, JoinType.LEFT);

        issueRoot.<Issue, ScanFile>fetch(Issue_.SCAN_FILE, JoinType.LEFT).<ScanFile,FileInfo>fetch(ScanFile_.FILE_INFO, JoinType.LEFT);
        issueRoot.<Issue, User>fetch(Issue_.ASSIGN_TO, JoinType.LEFT);

        q.where(cb.equal(issueRoot.get(Issue_.ID), uuid)).distinct(true);
        Optional<Issue> result;
        try {
            Issue issue = em.createQuery(q).getSingleResult();
            result = Optional.of(issue);
        } catch (NoResultException e) {
            result = Optional.empty();
        }
        return result;
    }

    @Override
    public List<CompareIssueObject> findCompareIssueObjectByScanTaskId(UUID scanTaskId) {
        log.info("[findCompareIssueObjectByScanTaskId] scanTaskId: {}", scanTaskId);
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
        Root<Issue> issueRoot = query.from(Issue.class);
        query.multiselect(issueRoot.get(Issue_.ID), issueRoot.get(Issue_.ISSUE_KEY));
        query.where(criteriaBuilder.equal(issueRoot.get(Issue_.SCAN_TASK).get(ScanTask_.ID), scanTaskId));
        TypedQuery<Object[]> typedQuery = em.createQuery(query);
        List<Object[]> resultList = typedQuery.getResultList();
        return resultList.stream().map(result -> CompareIssueObject.builder()
                .id((UUID) result[0])
                .issueKey((String) result[1]).build())
                .collect(Collectors.toList());
    }

    @Override
    public int updateIssueAssignToNullByUserId(UUID userId) {
        log.info("[setIssueAssignToNullByUserId] userId: {}", userId);
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaUpdate<Issue> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Issue.class);
        Root<Issue> issueRoot = criteriaUpdate.from(Issue.class);
        criteriaUpdate.set(Issue_.ASSIGN_TO, null).where(criteriaBuilder.equal(issueRoot.get(Issue_.ASSIGN_TO).get(User_.ID), userId));
        return em.createQuery(criteriaUpdate).executeUpdate();
    }
}
