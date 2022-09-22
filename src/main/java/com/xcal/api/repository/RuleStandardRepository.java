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

import com.xcal.api.entity.RuleStandard;
import com.xcal.api.entity.RuleStandardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RuleStandardRepository extends RuleStandardRepositoryCustom, JpaRepository<RuleStandard, UUID> {

    List<RuleStandard> findByRuleStandardSet(RuleStandardSet ruleStandardSet);

    List<RuleStandard> findByRuleStandardSetName(String ruleStandardSetName);

    List<RuleStandard> findByRuleStandardSetNameAndRuleStandardSetVersion(String ruleStandardSetName, String version);

    Optional<RuleStandard> findByRuleStandardSetAndCode(RuleStandardSet ruleStandardSet, String code);

    List<RuleStandard> findByIdIn(List<UUID> ids);

}