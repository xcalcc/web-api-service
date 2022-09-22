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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScanFileRepositoryCustomImpl extends RepositoryCustom implements ScanFileRepositoryCustom {

    @NonNull EntityManager em;

    @Override
    public List<ScanFile> findByScanTaskProject(Project project) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ScanFile> q = cb.createQuery(ScanFile.class);
        Root<ScanFile> scanFileRoot = q.from(ScanFile.class);

        scanFileRoot.<ScanFile, FileInfo>fetch(ScanFile_.FILE_INFO, JoinType.LEFT).<FileInfo, FileStorage>fetch(FileInfo_.FILE_STORAGE, JoinType.LEFT);
        scanFileRoot.<ScanFile, ScanTask>fetch(ScanFile_.SCAN_TASK).<ScanTask, Project>fetch(ScanTask_.PROJECT, JoinType.LEFT);

        q.where(cb.equal(scanFileRoot.get(ScanFile_.SCAN_TASK).get(ScanTask_.PROJECT), project)).distinct(true);
        return em.createQuery(q).getResultList();
    }

    @Override
    public List<ScanFile> findByScanTask(ScanTask scanTask) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ScanFile> q = cb.createQuery(ScanFile.class);
        Root<ScanFile> scanFileRoot = q.from(ScanFile.class);

        scanFileRoot.<ScanFile, FileInfo>fetch(ScanFile_.FILE_INFO, JoinType.LEFT).<FileInfo, FileStorage>fetch(FileInfo_.FILE_STORAGE, JoinType.LEFT);
        scanFileRoot.<ScanFile, ScanTask>fetch(ScanFile_.SCAN_TASK).<ScanTask, Project>fetch(ScanTask_.PROJECT, JoinType.LEFT);

        q.where(cb.equal(scanFileRoot.get(ScanFile_.SCAN_TASK), scanTask)).distinct(true);
        return em.createQuery(q).getResultList();
    }

    @Override
    public Page<ScanFile> searchScanFile(ScanTask scanTask, List<ScanFile.Type> types, List<ScanFile> scanFileList, Integer depth, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ScanFile> countScanFileRoot = countQuery.from(ScanFile.class);
        countQuery.select(cb.count(countScanFileRoot));
        Expression<Boolean> whereClause;

        if (types.isEmpty()) {
            whereClause = cb.equal(countScanFileRoot.get(ScanFile_.SCAN_TASK), scanTask);
        } else {
            whereClause = cb.and(cb.equal(countScanFileRoot.get(ScanFile_.SCAN_TASK), scanTask), countScanFileRoot.get(ScanFile_.TYPE).in(types));
        }

        if (!scanFileList.isEmpty()) {
            List<Predicate> scanFilePredicates = new ArrayList<>();
            for (ScanFile scanFile : scanFileList) {
                Predicate scanFileClause = cb.and(cb.gt(countScanFileRoot.get(ScanFile_.TREE_LEFT), scanFile.getTreeLeft()), cb.lt(countScanFileRoot.get(ScanFile_.TREE_RIGHT), scanFile.getTreeRight()),
                        cb.gt(countScanFileRoot.get(ScanFile_.DEPTH), scanFile.getDepth()));
                if (depth != null) {
                    scanFileClause = cb.and(scanFileClause, cb.le(countScanFileRoot.get(ScanFile_.DEPTH), scanFile.getDepth() + depth));
                }
                scanFilePredicates.add(scanFileClause);
            }
            Predicate scanFileOrClause = cb.or(scanFilePredicates.toArray(new Predicate[0]));
            whereClause = cb.and(whereClause, scanFileOrClause);
        } else {
            if (depth != null) {
                whereClause = cb.and(whereClause, cb.le(countScanFileRoot.get(ScanFile_.DEPTH), depth));
            }
        }
        countQuery.where(whereClause);
        Long count = em.createQuery(countQuery).getSingleResult();

        CriteriaQuery<ScanFile> q = cb.createQuery(ScanFile.class);
        Root<ScanFile> scanFileRoot = q.from(ScanFile.class);
        scanFileRoot.<ScanFile, FileInfo>fetch(ScanFile_.FILE_INFO, JoinType.LEFT).<FileInfo, FileStorage>fetch(FileInfo_.FILE_STORAGE, JoinType.LEFT);
        scanFileRoot.<ScanFile, ScanTask>fetch(ScanFile_.SCAN_TASK).<ScanTask, Project>fetch(ScanTask_.PROJECT, JoinType.LEFT);
        q.where(whereClause).distinct(true).orderBy(toOrders(cb, scanFileRoot, pageable.getSort()));
        List<ScanFile> result;
        if (pageable.isUnpaged()) {
            result = em.createQuery(q)
                    .getResultList();
        } else {
            result = em.createQuery(q)
                    .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
        }
        return new PageImpl<>(result, pageable, count);
    }
}
