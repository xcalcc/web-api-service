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

import com.xcal.api.entity.Project;
import com.xcal.api.entity.ScanTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScanTaskRepository extends JpaRepository<ScanTask, UUID>, ScanTaskRepositoryCustom {

	Optional<ScanTask> findFirst1ByProjectAndStatusAndModifiedOnLessThan(Project project, ScanTask.Status status, Date modifiedOn, Sort sort);

	Optional<ScanTask> findFirst1ByProjectAndStatus(Project project, ScanTask.Status status, Sort sort);

	@Query(value = "select * from scan_task st " +
			"where st.created_on < (select created_on from scan_task st2 where st2.id= CAST(:scanTaskId as uuid) and st2.project_id = CAST(:projectUUID as uuid) ) " +
			"and st.project_id = CAST(:projectUUID as uuid) " +
			"and status= :#{#status.name()} " +
			"order by st.created_on desc " +
			"limit 1", nativeQuery = true)
	Optional<ScanTask> findPreviousByProjectAndScanTaskAndStatus(UUID projectUUID, UUID scanTaskId, ScanTask.Status status);

	Optional<ScanTask> findFirst1ByProject(Project project, Sort sort);

	Page<ScanTask> findByProjectAndStatusIn(Project project, List<ScanTask.Status> statusList, Pageable pageable);

	List<ScanTask> findByProject(Project project);

	@Query(value = "select * from scan_task where project_id = ?1 offset 0 limit 1", nativeQuery = true)
	Optional<ScanTask> findLatestScanTaskByProject(UUID projectId);

	@Modifying
	@Query(value = "update scan_task set status = :toStatus where status = :fromStatus", nativeQuery = true)
	int updateStatus(@Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus);

	@Query(value = "select * from scan_task st " +
			"where st.id not in (select st2.id from scan_task as st2 where st2.status = 'COMPLETED' and st2.project_id = st.project_id order by st2.created_on desc limit :retentionNum) " +
			"and date_part('day', (date_trunc('day', now()) + interval '1 day') - st.created_on) >= :retentionPeriod", nativeQuery = true)
	List<ScanTask> findExpiredScanTaskByAgeAndHouseKeepMinNum(@Param("retentionPeriod")int retentionPeriod, @Param("retentionNum")int retentionNum);

	@Query(value = "select * from scan_task st " +
			"where st.project_id = :projectUUID and st.status = 'COMPLETED' " +
			"and date_part('day', (date_trunc('day', now()) + interval '1 day') - st.created_on) >= :retentionPeriod order by st.created_on limit :Num", nativeQuery = true)
	List<ScanTask> findExpiredScanTaskInProjectByAgeAndNum(@Param("projectUUID")UUID projectUUID, @Param("retentionPeriod")int retentionPeriod, @Param("Num")int Num);
}
