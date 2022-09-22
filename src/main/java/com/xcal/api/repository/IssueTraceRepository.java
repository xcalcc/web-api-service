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

import com.xcal.api.entity.Issue;
import com.xcal.api.entity.IssueTrace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueTraceRepository extends JpaRepository<IssueTrace, UUID>, IssueTraceRepositoryCustom {

    List<IssueTrace> findByIssueAndChecksum(Issue issue, String checksum);

    @Query(value = "select * from issue_trace where issue_id = ?1 and checksum in (select checksum from issue_trace where issue_id = ?1 group by checksum offset ?2 limit ?3)", nativeQuery = true)
    List<IssueTrace> findByIssueGroupByChecksum(UUID issueId, long start, long count);

    @Query(value = "select count(1) from (select checksum from issue_trace where issue_id = ?1 group by checksum) as t", nativeQuery = true)
    long countByIssueGroupByChecksum(UUID issueId);

}
