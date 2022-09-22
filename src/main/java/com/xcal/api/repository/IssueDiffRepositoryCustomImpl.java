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
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IssueDiffRepositoryCustomImpl extends RepositoryCustom implements IssueDiffRepositoryCustom {

    @NonNull EntityManager em;

    @Override
    public List<IssueDiff> findByScanTask(ScanTask scanTask){
        log.info("[findByScanTask] scanTaskId: {}", scanTask.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<IssueDiff> q = cb.createQuery(IssueDiff.class);
        Root<IssueDiff> issueDiffRoot = q.from(IssueDiff.class);

        issueDiffRoot.<IssueDiff, ScanTask>fetch(IssueDiff_.BASELINE_SCAN_TASK, JoinType.LEFT);
        issueDiffRoot.<IssueDiff, ScanTask>fetch(IssueDiff_.SCAN_TASK, JoinType.LEFT);
        Fetch<IssueDiff, Issue> issueFetch = issueDiffRoot.<IssueDiff, Issue>fetch(IssueDiff_.ISSUE, JoinType.LEFT);
        issueFetch.fetch(Issue_.ISSUE_TRACES, JoinType.LEFT);
        issueFetch.fetch(Issue_.SCAN_FILE, JoinType.LEFT);

        q.where(cb.equal(issueDiffRoot.get(IssueDiff_.SCAN_TASK), scanTask)).distinct(true);
        return em.createQuery(q).getResultList();
    }
}
