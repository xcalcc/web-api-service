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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScanTaskRepositoryCustomImpl extends RepositoryCustom implements ScanTaskRepositoryCustom {

    @NonNull EntityManager em;

    @Override
    public Page<ScanTask> searchScanTask(Project project, List<ScanTask.Status> status, List<ProjectConfigAttribute> existAttributes, List<ProjectConfigAttribute> equalAttributes, Pageable pageable) {
        log.info("[searchScanTask] project: {}, pageable: {}", Optional.ofNullable(project).map(Project::getId).orElse(null), pageable);
        // prepare where clause
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ScanTask> countScanTaskRoot = countQuery.from(ScanTask.class);
        // where project
        Expression<Boolean> whereClause = cb.equal(countScanTaskRoot.get(ScanTask_.project), project);
        // where status in
        if (status != null && status.size() > 0) {
            whereClause = cb.and(whereClause, countScanTaskRoot.get(ScanTask_.STATUS).in(status));
        }
        // where projectConfig have attribute
        if (existAttributes != null) {
            for (ProjectConfigAttribute attribute : existAttributes) {
                Subquery<UUID> sub = countQuery.subquery(UUID.class);
                Root<ProjectConfig> subProjectConfigRoot = sub.from(ProjectConfig.class);
                ListJoin<ProjectConfig, ProjectConfigAttribute> subAttribute = subProjectConfigRoot.joinList(ProjectConfig_.ATTRIBUTES);
                sub.select(subProjectConfigRoot.get(ProjectConfig_.ID));
                sub.where(cb.and(cb.equal(subProjectConfigRoot.get(ProjectConfig_.project), project)
                        , cb.equal(subAttribute.get(ProjectConfigAttribute_.TYPE), attribute.getType())
                        , cb.equal(subAttribute.get(ProjectConfigAttribute_.NAME), attribute.getName())))
                        .distinct(true);
                whereClause = cb.and(whereClause, countScanTaskRoot.get(ScanTask_.PROJECT_CONFIG).get(ProjectConfig_.ID).in(sub));
            }
        }
        // where projectConfig have exact attribute
        if (equalAttributes != null) {
            for (ProjectConfigAttribute attribute : equalAttributes) {
                Subquery<UUID> sub = countQuery.subquery(UUID.class);
                Root<ProjectConfig> subProjectConfigRoot = sub.from(ProjectConfig.class);
                ListJoin<ProjectConfig, ProjectConfigAttribute> subAttribute = subProjectConfigRoot.joinList(ProjectConfig_.ATTRIBUTES);
                sub.select(subProjectConfigRoot.get(ProjectConfig_.ID));
                sub.where(cb.and(cb.equal(subProjectConfigRoot.get(ProjectConfig_.project), project)
                        , cb.equal(subAttribute.get(ProjectConfigAttribute_.TYPE), attribute.getType())
                        , cb.equal(subAttribute.get(ProjectConfigAttribute_.NAME), attribute.getName())
                        , cb.equal(subAttribute.get(ProjectConfigAttribute_.VALUE), attribute.getValue())))
                        .distinct(true);
                whereClause = cb.and(whereClause, countScanTaskRoot.get(ScanTask_.PROJECT_CONFIG).get(ProjectConfig_.ID).in(sub));
            }
        }
        countQuery.select(cb.count(countScanTaskRoot)).where(whereClause);
        log.debug("[searchScanTask] process the count query.");
        Long count = em.createQuery(countQuery).getSingleResult();

        CriteriaQuery<ScanTask> scanTaskQuery = cb.createQuery(ScanTask.class);
        Root<ScanTask> scanTaskRoot = scanTaskQuery.from(ScanTask.class);
        Fetch<ScanTask, ProjectConfig> scanTaskProjectConfigFetch = scanTaskRoot.fetch(ScanTask_.PROJECT_CONFIG, JoinType.LEFT);
        scanTaskProjectConfigFetch.fetch(ProjectConfig_.PROJECT, JoinType.LEFT);
        scanTaskProjectConfigFetch.fetch(ProjectConfig_.ATTRIBUTES, JoinType.LEFT);
        scanTaskQuery.where(whereClause).distinct(true).orderBy(toOrders(cb, scanTaskRoot, pageable.getSort()));
        List<ScanTask> scanTasks = em.createQuery(scanTaskQuery)
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(scanTasks, pageable, count);
    }
}
