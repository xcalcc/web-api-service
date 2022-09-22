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
import javax.persistence.criteria.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RuleInformationRepositoryCustomImpl extends RepositoryCustom implements RuleInformationRepositoryCustom {

    @NonNull EntityManager em;

    @Override
    public List<RuleInformation> findByRuleSetNameAndRuleSetVersion(String ruleSetName, String ruleSetVersion){
        log.info("[findByRuleSetNameAndRuleSetVersion] ruleSetName: {}, ruleSetVersion: {}", ruleSetName, ruleSetVersion);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<RuleInformation> q = cb.createQuery(RuleInformation.class);
        Root<RuleInformation> ruleInformationRoot = q.from(RuleInformation.class);

        Join<RuleInformation, RuleSet> ruleSetRoot = ruleInformationRoot.join(RuleInformation_.RULE_SET, JoinType.LEFT);
        ruleInformationRoot.<RuleInformation, RuleSet>fetch(RuleInformation_.RULE_SET, JoinType.LEFT).<RuleSet, ScanEngine>fetch(RuleSet_.SCAN_ENGINE, JoinType.LEFT);

        Expression<Boolean> whereClause;
        if(StringUtils.isEmpty(ruleSetVersion)){
            whereClause = cb.equal(ruleSetRoot.get(RuleSet_.NAME), ruleSetName);
        }else{
            whereClause = cb.and(cb.equal(ruleSetRoot.get(RuleSet_.NAME), ruleSetName), cb.equal(ruleSetRoot.get(RuleSet_.VERSION), ruleSetVersion));
        }
        q.where(whereClause).distinct(true);
        return em.createQuery(q).getResultList();
    }

    @Override
    public List<RuleInformation> findByRuleSetName(String ruleSetName){
        return this.findByRuleSetNameAndRuleSetVersion(ruleSetName, null);
    }

    @Override
    public List<RuleInformation> findByRuleSet(RuleSet ruleSet) {
        log.info("[findByRuleSet] ruleSet, id: {}", ruleSet);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<RuleInformation> q = cb.createQuery(RuleInformation.class);
        Root<RuleInformation> ruleInformationRoot = q.from(RuleInformation.class);

        Join<RuleInformation, RuleSet> ruleSetRoot = ruleInformationRoot.join(RuleInformation_.RULE_SET, JoinType.LEFT);
        ruleInformationRoot.<RuleInformation, RuleSet>fetch(RuleInformation_.RULE_SET, JoinType.LEFT).<RuleSet, ScanEngine>fetch(RuleSet_.SCAN_ENGINE, JoinType.LEFT);
        ruleInformationRoot.<RuleInformation, RuleInformationAttribute>fetch(RuleInformation_.ATTRIBUTES, JoinType.LEFT);

        Expression<Boolean> whereClause;
        whereClause = cb.equal(ruleSetRoot.get(RuleSet_.ID), ruleSet.getId());
        q.where(whereClause).distinct(true);
        return em.createQuery(q).getResultList();
    }

    @Override
    public List<RuleInformation> findAll() {
        log.info("[findAll]");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<RuleInformation> q = cb.createQuery(RuleInformation.class);
        Root<RuleInformation> ruleInformationRoot = q.from(RuleInformation.class);
        ruleInformationRoot.<RuleInformation, RuleSet>fetch(RuleInformation_.RULE_SET, JoinType.LEFT).<RuleSet, ScanEngine>fetch(RuleSet_.SCAN_ENGINE, JoinType.LEFT);
        ruleInformationRoot.<RuleInformation, RuleInformationAttribute>fetch(RuleInformation_.ATTRIBUTES, JoinType.LEFT);
        return em.createQuery(q).getResultList();
    }

    @Override
    public Page<RuleInformation> findAll(Pageable pageable) {
        log.info("[findAll] pageable: {}", pageable);
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<RuleInformation> countRuleInformationRoot = countQuery.from(RuleInformation.class);
        countQuery.select(cb.count(countRuleInformationRoot));
        log.debug("[findAll] process the count query.");
        Long count = em.createQuery(countQuery).getSingleResult();

        CriteriaQuery<RuleInformation> q = cb.createQuery(RuleInformation.class);
        Root<RuleInformation> ruleInformationRoot = q.from(RuleInformation.class);
        ruleInformationRoot.<RuleInformation, RuleSet>fetch(RuleInformation_.RULE_SET, JoinType.LEFT).<RuleSet, ScanEngine>fetch(RuleSet_.SCAN_ENGINE, JoinType.LEFT);
        ruleInformationRoot.<RuleInformation, RuleInformationAttribute>fetch(RuleInformation_.ATTRIBUTES, JoinType.LEFT);

        List<RuleInformation> ruleInformationList = em.createQuery(q)
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(ruleInformationList, pageable, count);
    }

    @Override
    public List<RuleInformation> findByAttribute(List<RuleSet> ruleSets, List<RuleInformationAttribute> existAttributes, List<RuleInformationAttribute> equalAttributes) {
        log.info("[findByAttribute] ruleSets size: {}, existAttributes size:{}, equalAttributes size: {}", ruleSets.size(), existAttributes.size(), equalAttributes.size());
        // prepare where clause
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<RuleInformation> q = cb.createQuery(RuleInformation.class);
        Root<RuleInformation> ruleInformationRoot = q.from(RuleInformation.class);
        ruleInformationRoot.<RuleInformation, RuleSet>fetch(RuleInformation_.RULE_SET, JoinType.LEFT).<RuleSet, ScanEngine>fetch(RuleSet_.SCAN_ENGINE, JoinType.LEFT);
        ruleInformationRoot.<RuleInformation, RuleInformationAttribute>fetch(RuleInformation_.ATTRIBUTES, JoinType.LEFT);
        // where project
        Expression<Boolean> whereClause = null;
        if(ruleSets.size() > 0) {
            whereClause = cb.equal(ruleInformationRoot.get(RuleInformation_.RULE_SET), ruleSets);
        }
        // where ruleInformation have attribute
        for (RuleInformationAttribute attribute : existAttributes) {
            Subquery<UUID> sub = q.subquery(UUID.class);
            Root<RuleInformation> subRuleInformationRoot = sub.from(RuleInformation.class);
            ListJoin<RuleInformation, RuleInformationAttribute> subAttribute = subRuleInformationRoot.joinList(RuleInformation_.ATTRIBUTES);
            sub.select(subRuleInformationRoot.get(RuleInformation_.ID));
            sub.where(cb.and(cb.equal(subAttribute.get(RuleInformationAttribute_.TYPE), attribute.getType())
                    , cb.equal(subAttribute.get(RuleInformationAttribute_.NAME), attribute.getName())))
                    .distinct(true);
            if(whereClause == null) {
                whereClause = ruleInformationRoot.get(RuleInformation_.ID).in(sub);
            }else{
                whereClause = cb.and(whereClause, ruleInformationRoot.get(RuleInformation_.ID).in(sub));
            }
        }
        // where ruleInformation have exact attribute
        for (RuleInformationAttribute attribute : equalAttributes) {
            Subquery<UUID> sub = q.subquery(UUID.class);
            Root<RuleInformation> subRuleInformationRoot = sub.from(RuleInformation.class);
            ListJoin<RuleInformation, RuleInformationAttribute> subAttribute = subRuleInformationRoot.joinList(RuleInformation_.ATTRIBUTES);
            sub.select(subRuleInformationRoot.get(RuleInformation_.ID));
            sub.where(cb.and(cb.equal(subAttribute.get(RuleInformationAttribute_.TYPE), attribute.getType())
                    , cb.and(cb.equal(subAttribute.get(RuleInformationAttribute_.NAME), attribute.getName()))
                    , cb.equal(subAttribute.get(RuleInformationAttribute_.VALUE), attribute.getValue())))
                    .distinct(true);
            if(whereClause == null) {
                whereClause = ruleInformationRoot.get(RuleInformation_.ID).in(sub);
            }else{
                whereClause = cb.and(whereClause, ruleInformationRoot.get(RuleInformation_.ID).in(sub));
            }
        }
        q.where(whereClause).distinct(true);
        return em.createQuery(q).getResultList();
    }

//    private Page<RuleInformation> searchRuleInformation(List<RuleSet> ruleSets, List<RuleInformationAttribute> attributes,
//                                                        List<RuleInformationAttribute> attributesWithValue, Pageable pageable) {
//        log.info("[searchIssue] ruleSets id: {}, attributes: {}, attributesWithValue:{}, pageable: {}",
//                ruleSets.stream().map(RuleSet::getId).collect(Collectors.toList()), attributes, attributesWithValue, pageable);
//        // prepare where clause
//        CriteriaBuilder cb = em.getCriteriaBuilder();
//        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
//        Root<RuleInformation> countRuleInformationRoot = countQuery.from(RuleInformation.class);
//        // where scan task
//        Expression<Boolean> whereClause = countRuleInformationRoot.get(RuleInformation_.ruleSet).in(ruleSets);
//        // where rule set id or rule set name
//        if (ruleSetId != null) {
//            whereClause = cb.and(whereClause, cb.equal(countIssueRoot.get(Issue_.RULE_INFORMATION).get(RuleInformation_.RULE_SET).get(RuleSet_.ID), ruleSetId));
//        } else if (StringUtils.isNotBlank(ruleSetName)) {
//            whereClause = cb.and(whereClause, cb.equal(countIssueRoot.get(Issue_.RULE_INFORMATION).get(RuleInformation_.RULE_SET).get(RuleSet_.NAME), ruleSetName));
//        }
//        // where rule information ids in
//        if (ruleInformationIds != null && !ruleInformationIds.isEmpty()) {
//            whereClause = cb.and(whereClause, countIssueRoot.get(Issue_.RULE_INFORMATION).get(RuleInformation_.ID).in(ruleInformationIds));
//        }
//        // where seq, which in web UI is ID that display to user
//        if(StringUtils.isNotBlank(seq)){
//            whereClause = cb.and(whereClause, cb.like(countIssueRoot.get(Issue_.SEQ), StringUtils.join('%',seq,'%')));
//        }
//        // where the issue belong to scan file
//        Predicate scanFileClause = null;
//        Predicate nonProjectFileClause = countIssueRoot.get(Issue_.SCAN_FILE).isNull();
//        Predicate projectFileClause = null;
//        if (!scanFiles.isEmpty()) {
//            List<Predicate> scanFilePredicates = new ArrayList<>();
//            for (ScanFile scanFile : scanFiles) {
//                Predicate scanFilePredicate = cb.and(cb.ge(countIssueRoot.get(Issue_.SCAN_FILE).get(ScanFile_.TREE_LEFT), scanFile.getTreeLeft()), cb.le(countIssueRoot.get(Issue_.SCAN_FILE).get(ScanFile_.TREE_RIGHT), scanFile.getTreeRight()));
//                scanFilePredicates.add(scanFilePredicate);
//            }
//            projectFileClause = cb.or(scanFilePredicates.toArray(new Predicate[0]));
//        }
//        if (includeProjectFile && includeNonProjectFile) {
//            if (projectFileClause!=null) {
//                scanFileClause = cb.or(projectFileClause, nonProjectFileClause);
//            }
//        } else if (includeProjectFile) {  // includeProjectFile: true, includeNonProjectFile: false
//            if (projectFileClause!=null) {
//                scanFileClause = projectFileClause;
//            } else {
//                scanFileClause = countIssueRoot.get(Issue_.SCAN_FILE).isNotNull();
//            }
//        } else { // includeProjectFile: false, includeNonProjectFile: true
//            scanFileClause = nonProjectFileClause;
//        }
//
//        if(scanFileClause != null){
//            whereClause = cb.and(whereClause, scanFileClause);
//        }
//
//        // subquery to get issue have attributes
//        if(issueAttributeMap!=null) {
//            for (Map.Entry<VariableUtil.IssueAttributeName, List<String>> issueAttributeKeyValues : issueAttributeMap.entrySet()) {
//                Subquery<UUID> sub = countQuery.subquery(UUID.class);
//                Root<Issue> subIssueRoot = sub.from(Issue.class);
//                ListJoin<Issue, IssueAttribute> subAttribute = subIssueRoot.joinList(Issue_.ATTRIBUTES);
//                sub.select(subIssueRoot.get(Issue_.ID));
//                sub.where(cb.and(cb.equal(subIssueRoot.get(Issue_.scanTask), scanTask)
//                        ,cb.equal(subAttribute.get(IssueAttribute_.NAME), issueAttributeKeyValues.getKey())
//                        ,subAttribute.get(IssueAttribute_.VALUE).in(issueAttributeKeyValues.getValue())));
//                whereClause = cb.and(whereClause, countIssueRoot.get(Issue_.ID).in(sub));
//            }
//        }
//        countQuery.select(cb.count(countIssueRoot)).where(whereClause);
//        log.debug("[searchIssue] process the count query.");
//        Long count = em.createQuery(countQuery).getSingleResult();
//
//        // Query issue entity and sorting in pageable, this query will create issue object without related entity, so we need another query to fetch them
//        // If fetch related entity with collection and with sorting or paging at the same time (in here is IssueAttribute),
//        // all entry will fetch out into memory and then do the sorting and paging by JPA
//        // Since have Sorting, cannot only fetch ID only, need sort by various attributes.
//        CriteriaQuery<Issue> issueQuery = cb.createQuery(Issue.class);
//        Root<Issue> issueRoot = issueQuery.from(Issue.class);
//        issueRoot.<Issue, RuleInformation>fetch(Issue_.RULE_INFORMATION, JoinType.LEFT).fetch(RuleInformation_.RULE_SET, JoinType.LEFT).fetch(RuleSet_.SCAN_ENGINE, JoinType.LEFT);
//        issueRoot.<Issue, ScanFile>fetch(Issue_.SCAN_FILE, JoinType.LEFT);
//        issueRoot.<Issue, User>fetch(Issue_.ASSIGN_TO, JoinType.LEFT);
//        issueQuery.where(whereClause).distinct(true).orderBy(toOrders(cb, issueRoot, pageable.getSort()));
//        log.debug("[searchIssue] process the first query with sorting and paging.");
//        List<Issue> issueList = em.createQuery(issueQuery)
//                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
//                .setMaxResults(pageable.getPageSize())
//                .getResultList();
//
//        //Query issue entity with relation entities by sorting
//        CriteriaQuery<Issue> issueQueryWithRelationEntity = cb.createQuery(Issue.class);
//        Root<Issue> issueRootFetchWithRelationEntity = issueQueryWithRelationEntity.from(Issue.class);
//        issueRootFetchWithRelationEntity.<Issue, RuleInformation>fetch(Issue_.RULE_INFORMATION, JoinType.LEFT).fetch(RuleInformation_.RULE_SET, JoinType.LEFT).fetch(RuleSet_.SCAN_ENGINE, JoinType.LEFT);
//        issueRootFetchWithRelationEntity.<Issue, ScanFile>fetch(Issue_.SCAN_FILE, JoinType.LEFT).<ScanFile, FileInfo>fetch(ScanFile_.FILE_INFO, JoinType.LEFT);
//        issueRootFetchWithRelationEntity.<Issue, User>fetch(Issue_.ASSIGN_TO, JoinType.LEFT);
//        issueRootFetchWithRelationEntity.<Issue, IssueAttribute>fetch(Issue_.ATTRIBUTES, JoinType.LEFT);
//        if (!issueList.isEmpty()) {
//            whereClause = cb.and(whereClause, issueRootFetchWithRelationEntity.get(Issue_.ID).in(issueList.stream().map(Issue::getId).collect(Collectors.toList())));
//        }
//        issueQueryWithRelationEntity.where(whereClause).distinct(true).orderBy(toOrders(cb, issueRootFetchWithRelationEntity, pageable.getSort()));
//        log.debug("[searchIssue] process the actual query with related entity with fetch eager.");
//        List<Issue> result = em.createQuery(issueQueryWithRelationEntity).getResultList();
//        return new PageImpl<>(result, pageable, count);
//    }
}
