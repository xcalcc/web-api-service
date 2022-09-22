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
public class RuleStandardRepositoryCustomImpl extends RepositoryCustom implements RuleStandardRepositoryCustom {

    @NonNull EntityManager em;

    @Override
    public List<RuleStandard> findAll() {
        log.info("[findAll]");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<RuleStandard> q = cb.createQuery(RuleStandard.class);
        Root<RuleStandard> ruleStandardRoot = q.from(RuleStandard.class);

        Join<RuleStandard, RuleStandardSet> ruleStandardSetRoot = ruleStandardRoot.join(RuleStandard_.RULE_STANDARD_SET, JoinType.LEFT);
        ruleStandardRoot.<RuleInformation, RuleSet>fetch(RuleInformation_.ATTRIBUTES, JoinType.LEFT);
        return em.createQuery(q).getResultList();
    }
}
