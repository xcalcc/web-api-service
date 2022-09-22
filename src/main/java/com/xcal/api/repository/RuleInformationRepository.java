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

import com.xcal.api.entity.RuleInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RuleInformationRepository extends RuleInformationRepositoryCustom, JpaRepository<RuleInformation, UUID> {

    Optional<RuleInformation> findOneByRuleSetScanEngineNameAndRuleSetNameAndRuleCode(String scanEngineName, String ruleSetName, String ruleCode);

    List<RuleInformation> findByIdIn(List<UUID> ids);

    List<RuleInformation> findByRuleSetScanEngineName(String scanEngineName);

    List<RuleInformation> findByRuleSetScanEngineNameAndRuleSetScanEngineVersion(String scanEngineName, String scanEngineVersion);

    List<RuleInformation> findByRuleSetScanEngineNameAndRuleSetNameAndRuleSetVersion(String scanEngineName, String ruleSetName, String ruleSetVersion);

    List<RuleInformation> findByRuleSetScanEngineNameAndRuleSetScanEngineVersionAndRuleSetNameAndRuleSetVersion(String scanEngineName, String scanEngineVersion, String ruleSetName, String ruleSetVersion);
}
